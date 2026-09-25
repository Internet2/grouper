/**
 * @author shilen
 * $Id$
 */
package edu.internet2.middleware.grouper.ws;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.SessionException;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.exceptions.GrouperWsException;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.subject.Subject;

/**
 * working out which subject a request is acting as.
 *
 * <p>this used to live on GrouperServiceJ2ee, but none of it needs the servlet: it takes the
 * logged in subject, which the request context supplies, and everything after that is group
 * membership and configuration.  the servlet dependent part of working out who is logged in
 * stays in grouper-ws behind {@link GrouperWsSubjectResolver}.</p>
 */
public class GrouperWsSubjectUtils {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperWsSubjectUtils.class);

  /** cache the actAs */
  private static GrouperCache<MultiKey, Boolean> actAsCache = null;

  /** cache of whether a subject is allowed to use the web service at all */
  private static GrouperCache<MultiKey, Boolean> subjectAllowedCache = null;

  /**
   * get the actAsCache, and init if not initted
   * @return the actAsCache
   */
  public static GrouperCache<MultiKey, Boolean> actAsCache() {
    if (actAsCache == null) {
      int actAsTimeoutMinutes = actAsCacheMinutes();

      synchronized(GrouperWsSubjectUtils.class) {
        if (actAsCache == null) {
          actAsCache = new GrouperCache<MultiKey, Boolean>(GrouperWsSubjectUtils.class.getName() + "grouperWsActAsCache", 10000, false, 60*60*24, actAsTimeoutMinutes*60, false);
        }
      }
    }
    return actAsCache;
  }

  /**
   * @return act as cache minutes
   */
  public static int actAsCacheMinutes() {
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
          GrouperWsConfig.WS_CLIENT_USER_GROUP_CACHE_MINUTES, 2);

      synchronized(GrouperWsSubjectUtils.class) {
        if (subjectAllowedCache == null) {
          subjectAllowedCache = new GrouperCache<MultiKey, Boolean>(GrouperWsSubjectUtils.class.getName() + "grouperWsAllowedCache", 10000, false, 60*60*24, subjectAllowedTimeoutMinutes*60, false);
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

    final String USER_IS_NOT_AUTHORIZED = "User is not authorized: ";

    final Subject loggedInSubject = GrouperWsRequestContext.retrieveSubjectLoggedIn();

    HooksContext.assignSubjectLoggedIn(loggedInSubject);

    //make sure allowed
    final String userGroupName = GrouperWsConfig.retrieveConfig().propertyValueString(GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME);

    final String loggedInSubjectId = loggedInSubject.getId();
    if (!StringUtils.isBlank(userGroupName)) {
      GrouperSession grouperSession = null;

      try {
        //cache key to get or set if a user can act as another
        final MultiKey cacheKey = new MultiKey(loggedInSubjectId,
            loggedInSubject.getSource().getId());

        Boolean allowedInCache = subjectAllowedCache().get(cacheKey);

        //if not in cache
        if (allowedInCache == null) {
          grouperSession = GrouperSession.startRootSession();
          GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

            public Object callback(GrouperSession rootGrouperSession) throws GrouperSessionException {
              Group group = null;
              try {
                group = GroupFinder.findByName(rootGrouperSession, userGroupName, true);
              } catch (GroupNotFoundException gnfe) {
                group = new GroupSave().assignName(userGroupName).assignCreateParentStemsIfNotExist(true).save();
              }
              if (!group.hasMember(loggedInSubject)) {
                //not allowed, cache it
                subjectAllowedCache().put(cacheKey, false);
                throw new RuntimeException(USER_IS_NOT_AUTHORIZED + loggedInSubject + ", " + group);
              }
              subjectAllowedCache().put(cacheKey, true);
              return null;
            }
          });
        } else {
          //if in cache, reflect that
          if (!allowedInCache) {
            throw new RuntimeException(USER_IS_NOT_AUTHORIZED + loggedInSubject);
          }
        }
      } catch (Exception e) {
        String errorMessage = "user: '" + loggedInSubjectId + "' is not a member of group: '" + userGroupName
            + "', and therefore is not authorized to use the app (configured in local grouper-ws.properties ws.client.user.group.name";
        if (e.getMessage().startsWith(USER_IS_NOT_AUTHORIZED)) {
          LOG.error(errorMessage);
          throw new GrouperWsException("User is not authorized", e).assignLogStack(false);

        }
        LOG.error(errorMessage, e);
        throw new GrouperWsException("User is not authorized", e);
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

}
