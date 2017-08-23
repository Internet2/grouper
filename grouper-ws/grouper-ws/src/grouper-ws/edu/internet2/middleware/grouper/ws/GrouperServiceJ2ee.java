/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.grouper.ws;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;
import java.util.Vector;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.morphString.apache.codec.binary.Base64;
import edu.internet2.middleware.subject.SubjectNotFoundException;

import org.apache.axis2.context.MessageContext;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.NDC;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationThread;
import edu.internet2.middleware.grouper.j2ee.ServletRequestUtils;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.coresoap.GrouperService;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.grouper.ws.security.WsCustomAuthentication;
import edu.internet2.middleware.grouper.ws.security.WsGrouperDefaultAuthentication;
import edu.internet2.middleware.subject.Subject;

/**
 * Extend the servlet to get user info
 * 
 * @author mchyzer
 * 
 */
public class GrouperServiceJ2ee implements Filter {

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperServiceJ2ee.class);

  /**
   * if in request, get the start time
   * @return the start time
   */
  public static long retrieveRequestStartMillis() {
    Long requestStartMillis = threadLocalRequestStartMillis.get();
    return GrouperUtil.longValue(requestStartMillis, 0);
  }

  /**
   * get a single parameter value for key.  If multiple exist, throw error
   * @param paramMap is the map of params.  will get value from here if no request object
   * @param httpServletRequest optional.  if there, will make sure no dupes
   * @param key to lookup
   * @return the value
   */
  public static String parameterValue(Map<String, String> paramMap,
      HttpServletRequest httpServletRequest, String key) {
    //if no servlet (probably just testing), get from map
    if (httpServletRequest == null) {
      return paramMap == null ? null : paramMap.get(key);
    }
    String[] values = httpServletRequest.getParameterValues(key);
    if (values == null || values.length == 0) {
      return null;
    }
    //there is probably something wrong if multiple values detected
    if (values.length > 1) {
      throw new RuntimeException(
          "Multiple request parameter values where detected for key: " + key
              + ", when only one is expected: " + GrouperUtil.toStringForLog(values));
    }
    return values[0];
  }

  /**
   * retrieve the user principal (who is authenticated) from the (threadlocal)
   * request object
   * 
   * @return the user principal name
   */
  public static String retrieveUserPrincipalNameFromRequest() {

    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
    GrouperUtil
        .assertion(httpServletRequest != null,
            "HttpServletRequest is null, is the GrouperServiceServlet mapped in the web.xml?");
    Principal principal = httpServletRequest.getUserPrincipal();
    String principalName = null;
    if (principal == null) {
      principalName = httpServletRequest.getRemoteUser();
      if (StringUtils.isBlank(principalName)) {
        principalName = (String)httpServletRequest.getAttribute("REMOTE_USER");
      }
    } else {
      principalName = principal.getName();
    }
    GrouperUtil.assertion(StringUtils.isNotBlank(principalName),
        "There is no user logged in, make sure the container requires authentication");
    return principalName;
  }

  /**
   * retrieve the subject logged in to web service
   * If there are four colons, then this is the source and subjectId since
   * overlap in namespace
   * 
   * @return the subject
   */
  @SuppressWarnings({ "unchecked", "deprecation" })
  public static Subject retrieveSubjectLoggedIn() {
    String authenticationClassName = GrouperWsConfig.getPropertyString(
        GrouperWsConfig.WS_SECURITY_NON_RAMPART_AUTHENTICATION_CLASS,
        WsGrouperDefaultAuthentication.class.getName());
    String userIdLoggedIn = null;
    if (wssecServlet()) {

      MessageContext msgCtx = MessageContext.getCurrentMessageContext();
      Vector results = null;
      if ((results = (Vector) msgCtx.getProperty(WSHandlerConstants.RECV_RESULTS)) == null) {
        throw new RuntimeException("No Rampart security results!");
      }
      LOG.debug("Number of rampart results: " + results.size());
      OUTER: for (int i = 0; i < results.size(); i++) {
        WSHandlerResult rResult = (WSHandlerResult) results.get(i);
        Vector wsSecEngineResults = rResult.getResults();

        for (int j = 0; j < wsSecEngineResults.size(); j++) {
          WSSecurityEngineResult wser = (WSSecurityEngineResult) wsSecEngineResults
              .get(j);
          if (wser.getAction() == WSConstants.UT && wser.getPrincipal() != null) {

            //Extract the principal
            WSUsernameTokenPrincipal principal = (WSUsernameTokenPrincipal) wser
                .getPrincipal();

            //Get user
            userIdLoggedIn = principal.getName();
            break OUTER;

          }
        }
      }
      GrouperUtil.assertion(StringUtils.isNotBlank(userIdLoggedIn),
          "There is no Rampart user logged in, make sure the container requires authentication");
    } else {
      //this is for container auth (or custom auth, non-rampart)
      //get an instance
      Class<? extends WsCustomAuthentication> theClass = GrouperUtil
          .forName(authenticationClassName);

      WsCustomAuthentication wsAuthentication = GrouperUtil.newInstance(theClass);

      userIdLoggedIn = wsAuthentication
          .retrieveLoggedInSubjectId(retrieveHttpServletRequest());
    }

    // cant be blank!
    if (StringUtils.isBlank(userIdLoggedIn)) {
      //server is having trouble if got this far, but also the user's fault
      throw new WsInvalidQueryException("No user is logged in");
    }

    //null means dont look in a certain source
    String sourceId = null;

    //see if we need to split with 4 colons in login name
    if (StringUtils.contains(userIdLoggedIn, GrouperWsConfig.WS_SEPARATOR)) {
      String[] sourceSubjectId = GrouperUtil.splitTrim(userIdLoggedIn,
          GrouperWsConfig.WS_SEPARATOR);
      sourceId = sourceSubjectId[0];
      userIdLoggedIn = sourceSubjectId[1];
    } else {
      //see if there is a default source for all users to web service
      sourceId = StringUtils.trimToNull(GrouperWsConfig.retrieveConfig().propertyValueString(GrouperWsConfig.WS_LOGGED_IN_SUBJECT_DEFAULT_SOURCE));
    }

    String subjectIdPrefix = GrouperWsConfig
        .getPropertyString("ws.security.prependToUserIdForSubjectLookup");
    if (!StringUtils.isBlank(subjectIdPrefix)) {
      userIdLoggedIn = subjectIdPrefix + userIdLoggedIn;
    }
    
    //puts it in the log4j ndc context so userid is logged
    if (NDC.getDepth() == 0) {
      StringBuilder ndcBuilder = new StringBuilder("< ");
      if (!StringUtils.isBlank(sourceId)) {
        ndcBuilder.append(sourceId).append(" - ");
      }
      ndcBuilder.append(userIdLoggedIn).append(" - ");
      HttpServletRequest request = retrieveHttpServletRequest();
      if (request != null) {
        ndcBuilder.append(request.getRemoteAddr());
      }
      ndcBuilder.append(" >");
      NDC.push(ndcBuilder.toString());
    }
    
    Subject caller = null;
    GrouperSession grouperSession = GrouperSession.startRootSession(false);
    try {
      final String SOURCE_ID = sourceId;
      final String USER_ID_LOGGED_IN = userIdLoggedIn;
      caller = (Subject)GrouperSession.callbackGrouperSession(grouperSession, new GrouperSessionHandler() {

        public Object callback(GrouperSession theGrouperSession)
            throws GrouperSessionException {

          try {
            //see if across all sources
            if (SOURCE_ID == null) {
              return SubjectFinder.findByIdOrIdentifier(USER_ID_LOGGED_IN, true);
            }
            //see if in specified sources
            String[] sourceIds = GrouperUtil.splitTrim(SOURCE_ID, ",");
            for (String curSource : sourceIds) {
              Subject s = SubjectFinder.findByIdOrIdentifierAndSource(USER_ID_LOGGED_IN, curSource, false);
              if (s != null) {
                return s;
              }
            }
            throw new SubjectNotFoundException("Unable to find subject in source type(s): " + SOURCE_ID);
          } catch (Exception e) {
            //this is probably a system error...  not a user error
            throw new RuntimeException("Cant find subject from login id: " + USER_ID_LOGGED_IN, e);
          }
        }
      });
      
      caller = retrieveSubjectGrouperActAsHelper(caller);
      
      //this is set in filter
      GrouperContext grouperContext = GrouperContext.retrieveDefaultContext();
      
      Member member = MemberFinder.findBySubject(grouperSession, caller, true);
      
      grouperContext.setLoggedInMemberId(member.getUuid());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    
    return caller;

  }

  /**
   * see if there is a grouper act as in play here
   * @param loggedInSubject
   * @return the subject
   */
  private static Subject retrieveSubjectGrouperActAsHelper(final Subject loggedInSubject) {
    
    //see if we are acting as someone else
    String grouperActAsGroup = GrouperWsConfig.retrieveConfig().propertyValueString("ws.grouper.act.as.group");
    if (StringUtils.isBlank(grouperActAsGroup)
        || loggedInSubject == null) {
      LOG.debug("No grouperActAs configured");
      return loggedInSubject;
    }

    GrouperSession session = null;
    
    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();

    String grouperActAsSubjectId = httpServletRequest.getHeader("X-Grouper-actAsSubjectId");
    String grouperActAsSubjectIdentifier = httpServletRequest.getHeader("X-Grouper-actAsSubjectIdentifier");
    String grouperActAsSubjectSource = httpServletRequest.getHeader("X-Grouper-actAsSourceId");

    if (!StringUtils.isBlank(grouperActAsSubjectSource)) {

      if (!StringUtils.isBlank(grouperActAsSubjectId) 
          && !StringUtils.isBlank(grouperActAsSubjectIdentifier)) {
        throw new RuntimeException("You can only have one of X-Grouper-actAsSubjectId or X-Grouper-actAsSubjectIdentifier set!");
      }

      if (StringUtils.isBlank(grouperActAsSubjectId) 
          && StringUtils.isBlank(grouperActAsSubjectIdentifier)) {
        throw new RuntimeException("You must have one of X-Grouper-actAsSubjectId or X-Grouper-actAsSubjectIdentifier set if X-Grouper-actAsSourceId is set!");
      }

      try {
        grouperActAsSubjectSource = new String(new Base64().decode(grouperActAsSubjectSource.getBytes("UTF-8")), "UTF-8");
  
        if (!StringUtils.isBlank(grouperActAsSubjectId)) {
          grouperActAsSubjectId = new String(new Base64().decode(grouperActAsSubjectId.getBytes("UTF-8")), "UTF-8");
        } else if (!StringUtils.isBlank(grouperActAsSubjectIdentifier)) {
          grouperActAsSubjectIdentifier = new String(new Base64().decode(grouperActAsSubjectIdentifier.getBytes("UTF-8")), "UTF-8");
        }
      } catch (Exception e) {
        throw new RuntimeException("Problem with: source: '" + grouperActAsSubjectSource + "', id: '" 
            + grouperActAsSubjectId + "', or identifier: '" + grouperActAsSubjectIdentifier + "'", e);
      }
    } else {
      if (!StringUtils.isBlank(grouperActAsSubjectId) 
          || !StringUtils.isBlank(grouperActAsSubjectIdentifier)) {
        throw new RuntimeException("If X-Grouper-actAsSubjectId or X-Grouper-actAsSubjectIdentifier is set, then you must have a X-Grouper-actAsSourceId!");
      }
      //there is nothing there, go back
      return loggedInSubject;
    }
    
    // get the all powerful user
    Subject rootSubject = SubjectFinder.findRootSubject();

    try {
      session = GrouperSession.start(rootSubject);

      Subject actAsSubject = null;

      if (!StringUtils.isBlank(grouperActAsSubjectId)) {
        actAsSubject = SubjectFinder.findByIdAndSource(grouperActAsSubjectId, grouperActAsSubjectSource, true);
      } else if (!StringUtils.isBlank(grouperActAsSubjectIdentifier)) {
        actAsSubject = SubjectFinder.findByIdentifierAndSource(grouperActAsSubjectIdentifier, grouperActAsSubjectSource, true);
      } else {
        throw new RuntimeException("Why am I here?");
      }

      final Subject ACT_AS_SUBJECT = actAsSubject;
      
      //cache key to get or set if a user can act as another
      final MultiKey cacheKey = new MultiKey(loggedInSubject.getId(), loggedInSubject.getSource()
          .getId(), actAsSubject.getId(), actAsSubject.getSource().getId());

      Boolean inCache = null;
      
      if (actAsCacheMinutes() > 0) {
        inCache = grouperActAsCache().get(cacheKey);
      } else {
        inCache = false;
      }

      if (inCache != null && Boolean.TRUE.equals(inCache)) {
        LOG.debug("grouperActAs retrieved from cache");
        //if in cache and true, then allow
        return actAsSubject;
      }
      
      {
        //see if root or wheel group
        Subject rootAllowedSubject = (Subject)GrouperSession.callbackGrouperSession(session, new GrouperSessionHandler() {

          public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
            if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
              actAsCache().put(cacheKey, Boolean.TRUE);
              if (LOG.isDebugEnabled()) {
                LOG.debug("grouperActAs allowed since logged in user is wheel or root: " + GrouperUtil.subjectToString(loggedInSubject));
              }
              return ACT_AS_SUBJECT;
            }
            return null;
          }
        });
  
        if (rootAllowedSubject != null) {
          return rootAllowedSubject;
        }
      }
      
      //first separate by comma
      String[] groupEntries = GrouperUtil.splitTrim(grouperActAsGroup, ",");

      //see if all throw exceptions
      int countNoExceptions = 0;

      //we could also cache which entries the user is in...  not sure how many entries will be here
      for (String groupEntry : groupEntries) {

        //each entry should be failsafe
        try {
          //now see if it is a multi input
          if (StringUtils.contains(groupEntry, GrouperWsConfig.WS_SEPARATOR)) {

            //it is the group the user is in, and the group the act as has to be in
            String[] groupEntryArray = GrouperUtil.splitTrim(groupEntry,
                GrouperWsConfig.WS_SEPARATOR);
            String userMustBeInGroupName = groupEntryArray[0];
            String actAsMustBeInGroupName = groupEntryArray[1];

            Group userMustBeInGroup = GroupFinder.findByName(session,
                userMustBeInGroupName, true);
            Group actAsMustBeInGroup = GroupFinder.findByName(session,
                actAsMustBeInGroupName, true);

            if (userMustBeInGroup.hasMember(loggedInSubject)
                && actAsMustBeInGroup.hasMember(actAsSubject)) {
              if (LOG.isDebugEnabled()) {
                LOG.debug("grouperActAs allowed since logged in user is in group: " + userMustBeInGroupName + ", and act as user is in group: " + actAsMustBeInGroupName);
              }
              //its ok, lets add to cache
              actAsCache().put(cacheKey, Boolean.TRUE);
              return actAsSubject;
            }

          } else {
            //else this is a straightforward rule where the logged in user just has to be in a group and
            //can act as anyone
            Group actAsGroup = GroupFinder.findByName(session, grouperActAsGroup, true);

            // if the logged in user is a member of the actAs group, then allow
            // the actAs
            if (actAsGroup.hasMember(loggedInSubject)) {
              if (LOG.isDebugEnabled()) {
                LOG.debug("grouperActAs allowed since logged in user is in group: " + grouperActAsGroup);
              }
              //its ok, lets add to cache
              actAsCache().put(cacheKey, Boolean.TRUE);
              // this is the subject the web service wants to use
              return actAsSubject;
            }
          }
          countNoExceptions++;
        } catch (Exception e) {
          //just log and dont act since other entries could be fine
          LOG.error("Problem with groupEntry: " + groupEntry + ", loggedInUser: "
              + loggedInSubject + ", actAsSubject: " + actAsSubject, e);
        }

      }

      if (countNoExceptions == 0) {
        throw new RuntimeException("Problems seeing if web service user '"
            + loggedInSubject + "' can actAs the other subject: '" + actAsSubject + "'");
      }
      // if not an effective member
      throw new RuntimeException(
          "A web service is specifying an actAsUser, but the groups specified in "
              + GrouperWsConfig.WS_ACT_AS_GROUP + " in the grouper-ws.properties "
              + " does not have a valid rule for member: '" + loggedInSubject
              + "', and actAs: '" + actAsSubject + "'");
    } catch (SessionException se) {
      throw new RuntimeException(se);
    } finally {
      GrouperSession.stopQuietly(session);
    }
    
  }
  
  /** cache the actAs */
  private static GrouperCache<MultiKey, Boolean> actAsCache = null;

  /** cache the grouper actAs */
  private static GrouperCache<MultiKey, Boolean> grouperActAsCache = null;
  
  /** cache the actAs */
  private static GrouperCache<MultiKey, Boolean> subjectAllowedCache = null;

  /**
   * get the actAsCache, and init if not initted
   * @return the actAsCache
   */
  private static GrouperCache<MultiKey, Boolean> actAsCache() {
    if (actAsCache == null) {
      int actAsTimeoutMinutes = actAsCacheMinutes();

      synchronized(GrouperServiceJ2ee.class) {
        if (actAsCache == null) {
          actAsCache = new GrouperCache<MultiKey, Boolean>(GrouperServiceJ2ee.class.getName() + "grouperWsActAsCache", 10000, false, 60*60*24, actAsTimeoutMinutes*60, false);
        }
      }
    }
    return actAsCache;
  }

  /**
   * get the grouperActAsCache, and init if not initted
   * @return the grouperActAsCache
   */
  private static GrouperCache<MultiKey, Boolean> grouperActAsCache() {
    if (grouperActAsCache == null) {
      int actAsTimeoutMinutes = actAsCacheMinutes();

      synchronized(GrouperServiceJ2ee.class) {
        if (grouperActAsCache == null) {
          grouperActAsCache = new GrouperCache<MultiKey, Boolean>(GrouperServiceJ2ee.class.getName() + "grouperGrouperWsActAsCache", 10000, false, 60*60*24, actAsTimeoutMinutes*60, false);
        }
      }
    }
    return grouperActAsCache;
  }

  /**
   * @return act as cache minutes
   */
  private static int actAsCacheMinutes() {
    int actAsTimeoutMinutes = GrouperWsConfig.retrieveConfig().propertyValueInt(
        GrouperWsConfig.WS_ACT_AS_CACHE_MINUTES, 5);
    return actAsTimeoutMinutes;
  }

  /**
   * get the subjectAllowedCache, and init if not initted
   * @return the subjectAllowedCache
   */
  private static GrouperCache<MultiKey, Boolean> subjectAllowedCache() {
    if (subjectAllowedCache == null) {
      int subjectAllowedTimeoutMinutes = GrouperWsConfig.retrieveConfig().propertyValueInt(
          GrouperWsConfig.WS_CLIENT_USER_GROUP_CACHE_MINUTES, 5);
      
      synchronized(GrouperServiceJ2ee.class) {
        if (subjectAllowedCache == null) {
          subjectAllowedCache = new GrouperCache<MultiKey, Boolean>(GrouperServiceJ2ee.class.getName() + "grouperWsAllowedCache", 10000, false, 60*60*24, subjectAllowedTimeoutMinutes*60, false);
        }
      }
    }
    return subjectAllowedCache;
  }

  /**
   * retrieve the subject to act as
   * 
   * @param actAsLookup that the caller wants to act as
   * @return the subject
   * @throws WsInvalidQueryException if there is a problem
   */
  public static Subject retrieveSubjectActAs(WsSubjectLookup actAsLookup)
      throws WsInvalidQueryException {
    Subject actAsSubject = retrieveSubjectActAsHelper(actAsLookup);
    HooksContext.assignSubjectActAs(actAsSubject);

    //this is set in filter
    GrouperContext grouperContext = GrouperContext.retrieveDefaultContext();

    GrouperSession grouperSession = GrouperSession.staticGrouperSession(false);
    GrouperSession rootSession = grouperSession == null ? 
        GrouperSession.startRootSession(false) : grouperSession.internal_getRootSession();

    
    Member member = MemberFinder.findBySubject(rootSession, actAsSubject, true);
    
    grouperContext.setLoggedInMemberIdActAs(member.getUuid());
    
    return actAsSubject;
  }

  /**
   * retrieve the subject to act as
   * 
   * @param actAsLookup that the caller wants to act as
   * @return the subject
   * @throws WsInvalidQueryException if there is a problem
   */
  private static Subject retrieveSubjectActAsHelper(WsSubjectLookup actAsLookup)
      throws WsInvalidQueryException {

    Subject loggedInSubject = retrieveSubjectLoggedIn();

    HooksContext.assignSubjectLoggedIn(loggedInSubject);
    
    //make sure allowed
    String userGroupName = GrouperWsConfig.retrieveConfig().propertyValueString(GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME);
    
    String loggedInSubjectId = loggedInSubject.getId();
    if (!StringUtils.isBlank(userGroupName)) {
      GrouperSession grouperSession = null;
      
      try {
        //cache key to get or set if a user can act as another
        MultiKey cacheKey = new MultiKey(loggedInSubjectId, 
            loggedInSubject.getSource().getId());

        Boolean allowedInCache = subjectAllowedCache().get(cacheKey);

        //if not in cache
        if (allowedInCache == null) {
          grouperSession = GrouperSession.startRootSession();

          Group group = GroupFinder.findByName(grouperSession, userGroupName, true);
          if (!group.hasMember(loggedInSubject)) {
            //not allowed, cache it
            subjectAllowedCache().put(cacheKey, false);
            throw new RuntimeException("User is not authorized: " + loggedInSubject + ", " + group);
          }
          subjectAllowedCache().put(cacheKey, true);
        } else {
          //if in cache, reflect that
          if (!allowedInCache) {
            throw new RuntimeException("User is not authorized: " + loggedInSubject);
          }
        }
      } catch (Exception e) {
        LOG.error("user: '" + loggedInSubjectId + "' is not a member of group: '" + userGroupName 
            + "', and therefore is not authorized to use the app (configured in local media.properties penn.uiGroup", e);
        throw new RuntimeException("User is not authorized", e);
      } finally {
        GrouperSession.stopQuietly(grouperSession);
      }
    }

    
    // if there is no actAs specified, then just use the logged in user
    if (actAsLookup == null || actAsLookup.blank()) {
      return loggedInSubject;
    }
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Subject actAsSubject = null;
    try {
      actAsSubject = actAsLookup.retrieveSubject("actAsSubject");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    
    //see if same:
    if (StringUtils.equals(loggedInSubjectId, actAsSubject.getId())
        && StringUtils.equals(loggedInSubject.getSource().getId(), actAsSubject.getSource().getId())) {
      return loggedInSubject;
    }
    
    //lets see if in cache    

    //cache key to get or set if a user can act as another
    MultiKey cacheKey = new MultiKey(loggedInSubjectId, loggedInSubject.getSource()
        .getId(), actAsSubject.getId(), actAsSubject.getSource().getId());

    Boolean inCache = null;
    
    if (actAsCacheMinutes() > 0) {
      inCache = actAsCache().get(cacheKey);
    } else {
      inCache = false;
    }

    if (inCache != null && Boolean.TRUE.equals(inCache)) {
      //if in cache and true, then allow
      return actAsSubject;
    }
    
    //see if root or wheel group
    GrouperSession session = null;
    try {
      session = GrouperSession.start(loggedInSubject);
      if (PrivilegeHelper.isRoot(session)) {
        actAsCache().put(cacheKey, Boolean.TRUE);
      return actAsSubject;
    }
    } catch (SessionException se) {
      throw new RuntimeException(se);
    } finally {
      GrouperSession.stopQuietly(session);
    }

    // so there is an actAs specified, lets see if we are allowed to use it
    // first lets get the group you have to be in if you are going to
    String actAsGroupName = GrouperWsConfig.retrieveConfig().propertyValueString(GrouperWsConfig.WS_ACT_AS_GROUP);

    // make sure there is one there
    if (StringUtils.isBlank(actAsGroupName)) {

      //if none configured, then probably a caller problem
      throw new WsInvalidQueryException(
          "A web service is specifying an actAsUser, but there is no '"
              + GrouperWsConfig.WS_ACT_AS_GROUP
              + "' specified in the grouper-ws.properties");
    }

    session = null;
    // get the all powerful user
    Subject rootSubject = SubjectFinder.findRootSubject();

    try {
      session = GrouperSession.start(rootSubject);

      //first separate by comma
      String[] groupEntries = GrouperUtil.splitTrim(actAsGroupName, ",");

      //see if all throw exceptions
      int countNoExceptions = 0;

      //we could also cache which entries the user is in...  not sure how many entries will be here
      for (String groupEntry : groupEntries) {

        //each entry should be failsafe
        try {
          //now see if it is a multi input
          if (StringUtils.contains(groupEntry, GrouperWsConfig.WS_SEPARATOR)) {

            //it is the group the user is in, and the group the act as has to be in
            String[] groupEntryArray = GrouperUtil.splitTrim(groupEntry,
                GrouperWsConfig.WS_SEPARATOR);
            String userMustBeInGroupName = groupEntryArray[0];
            String actAsMustBeInGroupName = groupEntryArray[1];

            Group userMustBeInGroup = GroupFinder.findByName(session,
                userMustBeInGroupName, true);
            Group actAsMustBeInGroup = GroupFinder.findByName(session,
                actAsMustBeInGroupName, true);

            if (userMustBeInGroup.hasMember(loggedInSubject)
                && actAsMustBeInGroup.hasMember(actAsSubject)) {
              //its ok, lets add to cache
              actAsCache().put(cacheKey, Boolean.TRUE);
              return actAsSubject;
            }

          } else {
            //else this is a straightforward rule where the logged in user just has to be in a group and
            //can act as anyone
            Group actAsGroup = GroupFinder.findByName(session, actAsGroupName, true);

            // if the logged in user is a member of the actAs group, then allow
            // the actAs
            if (actAsGroup.hasMember(loggedInSubject)) {
              //its ok, lets add to cache
              actAsCache().put(cacheKey, Boolean.TRUE);
              // this is the subject the web service wants to use
              return actAsSubject;
            }
          }
          countNoExceptions++;
        } catch (Exception e) {
          //just log and dont act since other entries could be fine
          LOG.error("Problem with groupEntry: " + groupEntry + ", loggedInUser: "
              + loggedInSubject + ", actAsSubject: " + actAsSubject, e);
        }

      }

      if (countNoExceptions == 0) {
        throw new RuntimeException("Problems seeing if web service user '"
            + loggedInSubject + "' can actAs the other subject: '" + actAsSubject + "'");
      }
      // if not an effective member
      throw new RuntimeException(
          "A web service is specifying an actAsUser, but the groups specified in "
              + GrouperWsConfig.WS_ACT_AS_GROUP + " in the grouper-ws.properties "
              + " does not have a valid rule for member: '" + loggedInSubject
              + "', and actAs: '" + actAsSubject + "'");
    } catch (SessionException se) {
      throw new RuntimeException(se);
    } finally {
      GrouperSession.stopQuietly(session);
    }

  }

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * thread local for servlet
   */
  private static ThreadLocal<HttpServlet> threadLocalServlet = new ThreadLocal<HttpServlet>();

  /**
   * thread local for request
   */
  private static ThreadLocal<HttpServletRequest> threadLocalRequest = new ThreadLocal<HttpServletRequest>();

  /**
   * thread local for request
   */
  private static ThreadLocal<Long> threadLocalRequestStartMillis = new ThreadLocal<Long>();

  /**
   * thread local for response
   */
  private static ThreadLocal<HttpServletResponse> threadLocalResponse = new ThreadLocal<HttpServletResponse>();

  /**
   * public method to get the http servlet request
   * 
   * @return the http servlet request
   */
  public static HttpServletRequest retrieveHttpServletRequest() {
    return threadLocalRequest.get();
  }

  /**
   * public method to get the http servlet
   * 
   * @return the http servlet
   */
  public static HttpServlet retrieveHttpServlet() {
    return threadLocalServlet.get();
  }

  /**
   * is this a wssec servlet?  must have servlet init param
   * @return true if wssec
   */
  public static boolean wssecServlet() {
    String wssecValue = retrieveHttpServlet().getServletConfig()
        .getInitParameter("wssec");
    return GrouperUtil.booleanValue(wssecValue, false);
  }

  /**
   * public method to get the http servlet
   * 
   * @param httpServlet is servlet to assign
   */
  public static void assignHttpServlet(HttpServlet httpServlet) {
    threadLocalServlet.set(httpServlet);
  }

  /**
   * public method to get the http servlet request
   * 
   * @return the http servlet request
   */
  public static HttpServletResponse retrieveHttpServletResponse() {
    return threadLocalResponse.get();
  }

  /**
   * filter method
   */
  public void destroy() {
    InstrumentationThread.shutdownThread();
  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    try {
      
      request.setCharacterEncoding("UTF-8");
      response.setCharacterEncoding("UTF-8");
      
      //make sure nulls are not returned for params for Axis bug where
      //empty strings work, but nulls make things off a bit
      request = new WsHttpServletRequest((HttpServletRequest) request);
  
      NDC.clear();
      
      //servlet will set this...
      threadLocalServlet.remove();
      threadLocalRequest.set((HttpServletRequest) request);
      threadLocalResponse.set((HttpServletResponse) response);
      threadLocalRequestStartMillis.set(System.currentTimeMillis());
      
      GrouperContextTypeBuiltIn.setDefaultContext(GrouperContextTypeBuiltIn.GROUPER_WS);
  
      //lets add the request, session, and response
      HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_REQUEST, request, false);
      HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SESSION, 
          ((HttpServletRequest)request).getSession(), false);
      HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_RESPONSE, response, false);
  
      GrouperContext grouperContext = GrouperContext.createNewDefaultContext(
          GrouperEngineBuiltin.WS, false, false);
      
      grouperContext.setCallerIpAddress(request.getRemoteAddr());

    
      filterChain.doFilter(request, response);
    } catch (RuntimeException re) {
      LOG.info("error in request", re);
      throw re;
    } finally {
      threadLocalRequest.remove();
      threadLocalResponse.remove();
      threadLocalRequestStartMillis.remove();
      threadLocalServlet.remove();
      
      HooksContext.clearThreadLocal();
      ServletRequestUtils.requestEnd();

    }

  }

  /**
   * filter method
   */
  public void init(FilterConfig arg0) throws ServletException {
    InstrumentationThread.startThread(GrouperEngineBuiltin.WS, null);
  }

}
