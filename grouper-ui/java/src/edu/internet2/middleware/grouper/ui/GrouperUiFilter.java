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
/*
Copyright 2004-2008 University Corporation for Advanced Internet Development, Inc.
Copyright 2004-2008 The University Of Bristol

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package edu.internet2.middleware.grouper.ui;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.ContextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.RequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2ExternalSubjectSelfRegister;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationDataBuiltinTypes;
import edu.internet2.middleware.grouper.instrumentation.InstrumentationThread;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.j2ee.ServletRequestUtils;
import edu.internet2.middleware.grouper.j2ee.status.GrouperStatusServlet;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperThreadLocalState;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;
import edu.internet2.middleware.subject.SubjectNotUniqueException;

/**
 * Generic filter for ui for grouper (e.g. set hooks context)
 * 
 * @author Chris Hyzer.
 * @version $Id: GrouperUiFilter.java,v 1.8 2009-10-11 22:04:18 mchyzer Exp $
 */

public class GrouperUiFilter implements Filter {

  static {
    GrouperStatusServlet.registerStartup();
  }

  /** keep 100k in memory, why not */
  private static FileItemFactory fileItemFactory = new DiskFileItemFactory(100000, null);

  /** Create a new file upload handler */
  private static ServletFileUpload upload = new ServletFileUpload(fileItemFactory);

  /**
   * get the request locale or null if there is no request is scope...
   * @return the locale
   */
  public static Locale retrieveLocale() {
    
    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
    if (httpServletRequest == null) {
      return null;
    }
    
    return httpServletRequest.getLocale();
    
  }
  
  /**
   * get the nav resource bundle from session
   * @return the nav resource bundle
   */
  public static ResourceBundle retrieveSessionNavResourceBundle() {
    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
    HttpSession session = httpServletRequest.getSession();
    LocalizationContext attribute = (LocalizationContext)session.getAttribute("nav");
    return attribute.getResourceBundle();
  }
  
  /**
   * get the media resource bundle from session
   * @return the media resource bundle
   */
  public static ResourceBundle retrieveSessionMediaResourceBundle() {
    HttpSession session = retrieveHttpServletRequest().getSession(false);
    LocalizationContext localizationContext = null;
    if (session != null) {
      localizationContext = (LocalizationContext)session.getAttribute("media");      
    }
    if (localizationContext != null) {
      return localizationContext.getResourceBundle();
    }
    //note, call retrieveMediaProperties() if session properites are null...
    throw new RuntimeException("Cant find media bundle");
  }
  
  /**
   * get the media resource bundle from session
   * @return the media resource bundle
   */
  @SuppressWarnings("unchecked")
  public static Map<String, String> retrieveSessionMediaNullMapResourceBundle() {
    HttpSession session = retrieveHttpServletRequest().getSession(false);
    if (session != null) {
      return (Map<String, String>)session.getAttribute("mediaNullMap");
    }
    //note, call retrieveMediaProperties() if session properites are null...
    throw new RuntimeException("Cant find media bundle");
  }
  
  /**
   * if the media resource bundle is null, use this
   * @return properties
   */
  public static Properties retrieveMediaProperties() {
    //cant find in session, this should be a special case, like during startup
    Properties propertiesSettings = new Properties();
    propertiesSettings.putAll(GrouperUtil
      .propertiesFromResourceName("resources/grouper/media.properties"));
    
    //if there is a custom, add it in
    if (GrouperUtil.computeUrl("resources/custom/media.properties", true) != null) {
      propertiesSettings.putAll(GrouperUtil
          .propertiesFromResourceName("resources/custom/media.properties"));
    }
    
    propertiesSettings.putAll(GrouperUiConfig.retrieveConfig().properties());
    return propertiesSettings;
  }
  
  /**
   * get the list of file items, cache these in request
   * @return the list of file items
   */
  @SuppressWarnings("unchecked")
  public static List<FileItem> fileItems() {
    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
    List<FileItem> fileItems = (List<FileItem>)httpServletRequest.getAttribute("fileItems");
    if (fileItems == null) {
      try {
        fileItems = upload.parseRequest(httpServletRequest);
        httpServletRequest.setAttribute("fileItems", fileItems);
      } catch (Exception e) {
       throw new RuntimeException(e);
      }
    }
    return fileItems;
  }

  /**
   * find the request parameter names by prefix
   * @param prefix
   * @return the set, never null
   */
  @SuppressWarnings("unchecked")
  public static Set<String> requestParameterNamesByPrefix(String prefix) {
    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
    Set<String> result = new LinkedHashSet<String>();
    Enumeration<String> paramNames = httpServletRequest.getParameterNames();
    
    //cycle through all
    while(paramNames.hasMoreElements()) {
      String paramName = paramNames.nextElement();
      
      //see if starts with
      if (paramName.startsWith(prefix)) {
        result.add(paramName);
      }
    }
    
    
    return result;
  }

  /** logger */
  private static Log LOG = LogFactory.getLog(GrouperUiFilter.class);

  /**
   * if in request, get the start time
   * @return the start time
   */
  public static long retrieveRequestStartMillis() {
    Long requestStartMillis = threadLocalRequestStartMillis.get();
    return GrouperUtil.longValue(requestStartMillis, 0);
  }

  /**
   * retrieve the user principal (who is authenticated) from the (threadlocal)
   * request object
   * @deprecated this is not used, use GrouperUiFilter.remoteUser(request) instead
   * @return the user principal name
   */
  @Deprecated
  public static String retrieveUserPrincipalNameFromRequest() {

    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
    GrouperUtil
        .assertion(httpServletRequest != null,
            "HttpServletRequest is null, is the GrouperServiceServlet mapped in the web.xml?");
    Principal principal = httpServletRequest.getUserPrincipal();
    GrouperUtil.assertion(principal != null,
        "There is no user logged in, make sure the container requires authentication");
    return principal.getName();
  }

  /**
   * retrieve the subject logged in
   * 
   * @return the subject
   */
  public static Subject retrieveSubjectLoggedIn() {
    return retrieveSubjectLoggedIn(false, null);
  }

  /**
   * retrieve the subject logged in
   * @param allowNoUserLoggedIn true if allowed to have no user, false if expecting a user
   * @param httpServletResponse 
   * @return the subject
   */
  public static Subject retrieveSubjectLoggedIn(boolean allowNoUserLoggedIn, HttpServletResponse httpServletResponse) {
    Subject subject = retrieveSubjectLoggedInHelper(allowNoUserLoggedIn);

    /* todo cer28-is this redundant? It's already in doFilter */
//    UiSection uiSectionForRequest = uiSectionForRequest();
//
//    ensureUserAllowedInSection(uiSectionForRequest, subject, httpServletResponse);

    if (subject != null) {
      SessionContainer sessionContainer = SessionContainer.retrieveFromSession();
      
      sessionContainer.setSubjectLoggedIn(subject);
    }
    
    return subject;
  }


  /**
   * retrieve the subject logged in
   * @param allowNoUserLoggedIn true if allowed to have no user, false if expecting a user
   * @return the subject
   */
  private static Subject retrieveSubjectLoggedInHelper(boolean allowNoUserLoggedIn) {

    //currently assumes user is in getUserPrincipal
    HttpServletRequest request = retrieveHttpServletRequest();

    String userIdLoggedIn = remoteUser(request);

    //this cant change!
    String previousRemoteUser = (String)request.getSession().getAttribute("authUser");
    
    if (!StringUtils.isBlank(previousRemoteUser) && !StringUtils.equals(previousRemoteUser, userIdLoggedIn)) {
      request.getSession().invalidate();
      throw new RuntimeException("New user logged in!  was: '" + previousRemoteUser + "', and now is: '" + userIdLoggedIn + "'" );
    }

    GrouperSession grouperSession = SessionInitialiser.getGrouperSession(request.getSession());
    if (grouperSession != null && grouperSession.getSubject() != null) {
      
      return grouperSession.getSubject();
    }
    
    SessionContainer sessionContainer = SessionContainer.retrieveFromSession();
    
    Subject subjectLoggedIn = sessionContainer.getSubjectLoggedIn();
    
    if (subjectLoggedIn != null) {
      return subjectLoggedIn;
    }
  
    if (StringUtils.isBlank(userIdLoggedIn)) {
      if (allowNoUserLoggedIn) {
        return null;
      }
      throw new NoUserAuthenticatedException("Cant find logged in user");
    }
    
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      String sourceIds = GrouperUiConfig.retrieveConfig().propertyValueString("grouper.ui.authentication.sourceIds");
      if (StringUtils.isBlank(sourceIds)) {
        subjectLoggedIn = SubjectFinder.findByIdOrIdentifier(userIdLoggedIn, true);
      } else {
        Subject theSubjectLoggedIn = null;
        for (String sourceId : GrouperUtil.splitTrim(sourceIds, ",")) {
          Subject tempSubject = SubjectFinder.findByIdOrIdentifierAndSource(userIdLoggedIn, sourceId, false);
          if (tempSubject != null) {
            if (theSubjectLoggedIn == null) {
              theSubjectLoggedIn = tempSubject;
            } else {
              throw new SubjectNotUniqueException("Found multiple matching subjects: '" + userIdLoggedIn + "'");
            }
          }
        }
        subjectLoggedIn = theSubjectLoggedIn;
        if (subjectLoggedIn == null) {
          throw new SubjectNotFoundException("Cannot find subject by id or identifier: '" + userIdLoggedIn + "'");
        }
      }
    } catch (RuntimeException re) {
      if (re instanceof SubjectNotFoundException && allowNoUserLoggedIn) {
        return null;
      }
      //this is probably a system error...  not a user error
      GrouperUtil.injectInException(re, "Cant find subject from login id: " + userIdLoggedIn);
      throw re;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
        
    return subjectLoggedIn;

  }

  /**
   * make sure user is allowed in this section
   * @param uiSection
   * @param subjectLoggedIn
   * @param response
   */
  private static void ensureUserAllowedInSection(UiSection uiSection, Subject subjectLoggedIn, HttpServletResponse response) {

    if (subjectLoggedIn == null && uiSection.isAnonymous()) {
      return;
    }

    //if the user is allowed in the admin ui, we are all good
    Set<UiSection> uiSectionsThatAllowThisSection = GrouperUtil.nonNull(uiSection.getUiSectionsThatAllowThisSection());
    for (UiSection currentSection : uiSectionsThatAllowThisSection) {
      if (SessionContainer.retrieveFromSession().getAllowedUiSections().contains(currentSection)) {
        return;
      }
    }
    StringBuilder groups = new StringBuilder();
    for (UiSection currentSection : uiSectionsThatAllowThisSection) {
      String mediaKey = currentSection.getMediaKey();
      if (!StringUtils.isBlank(mediaKey)) {
        
        if (subjectLoggedIn == null) {
          try {
            response.sendRedirect(GrouperUiFilter.retrieveServletContext() + "/grouperExternal/public/UiV2Public.index?operation=UiV2Public.postIndex&function=UiV2Public.error&code=anonymousSessionNotAllowed");
          } catch (IOException ioe) {
            throw new RuntimeException("Error", ioe);
          }
          throw new ControllerDone();
        }
        
        String thisError = requireUiGroup(mediaKey, subjectLoggedIn, true);
        if (!StringUtils.isBlank(thisError)) {
          groups.append(thisError).append(", ");
        } else {
          //this means allowed, we are all good
          groups = new StringBuilder();
          break;
        }
      }
    }
    
    //if there is an error or more, that is bad
    if (groups.length() > 0) {
      //strip last comma
      String groupsString = groups.substring(0,groups.length()-2);
      String errorsString = GrouperUiUtils.message("ui.error.not.in.required.group", false, true, 
          GrouperUtil.subjectToString(subjectLoggedIn), groupsString);
      LOG.error(errorsString);
      GrouperUiUtils.appendErrorToRequest(errorsString);
      
      if (RequestContainer.retrieveFromRequest().isAjaxRequest()) {
        GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
        guiResponseJs.addAction(GuiScreenAction.newAlert(
            GrouperUiUtils.message("simpleMembershipUpdate.notAllowedInUi")));
        throw new ControllerDone();
        
      }
      //ui request, just throw exception
      throw new RuntimeException(errorsString);
      
    }
    
    //keep this in session so we dont have to keep checking
    SessionContainer.retrieveFromSession().getAllowedUiSections().add(uiSection);
  }
  
  /**
   * use the media properties key to see if a group is required, then make sure the user is in that group
   * @param mediaKeyOfGroup
   * @param subjectLoggedIn
   * @param allowUserIfGroupNotConfigured is true if user is allowed if group is not configured, default to true
   * for previous behavior
   * @return the error message group name
   */
  public static String requireUiGroup(String mediaKeyOfGroup, Subject subjectLoggedIn, boolean allowUserIfGroupNotConfigured) {
    
    //see if member of login group
    String groupToRequire = GrouperUiConfig.retrieveConfig().propertyValueString(mediaKeyOfGroup);
    if (LOG.isDebugEnabled()) {
      LOG.debug("mediaKeyOfGroup: " + mediaKeyOfGroup + ", groupToRequire: " + groupToRequire + ", subject: " + GrouperUtil.subjectToString(subjectLoggedIn));
    }
    if (!StringUtils.isBlank(groupToRequire)) {
      
      GrouperSession grouperSession = null;
      
      //get a session, close it if you started it
      boolean startedSession = false;
      try {
        grouperSession = GrouperSession.staticGrouperSession(false);
        if (grouperSession == null) {
          grouperSession = GrouperSession.startRootSession();
          startedSession = true;
        }
        if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
          grouperSession = grouperSession.internal_getRootSession();
        }
        Group group = GroupFinder.findByName(grouperSession, groupToRequire, true);
        if (subjectLoggedIn == null || !group.hasMember(subjectLoggedIn)) {
          
          String error = groupToRequire;
          return error;
        }
        return null;
      } catch (Exception e) {
        throw new RuntimeException("Problem with user: " + GrouperUtil.subjectToString(subjectLoggedIn) + ", " + groupToRequire, e);
      } finally {
        if (startedSession) {
          GrouperSession.stopQuietly(grouperSession);
        }
      }
    }
    if (!allowUserIfGroupNotConfigured) {
      return mediaKeyOfGroup;
    }
    return null;
  }

  /**
   * which UI section we are in
   */
  public static enum UiSection implements Serializable {
    
    /** doesnt require login yet */
    ANONYMOUS(null, null) {

      /**
       * @see UiSection#isAnonymous()
       */
      @Override
      public boolean isAnonymous() {
        return true;
      }
      
    },

    /** doesnt require login yet */
    EXTERNAL(null, null) {

      /**
       * @see UiSection#isAnonymous()
       */
      @Override
      public boolean isAnonymous() {
        return true;
      }
      
    },

    /** normal admin ui */
    ADMIN_UI("require.group.for.logins", null),

    /** simple membership update */
    INVITE_EXTERNAL_SUBJECTS("require.group.for.inviteExternalSubjects.logins", null);

    /** media properties key */
    private String mediaKey;

    /** set of sections that allow this section */
    private Set<UiSection> uiSectionsThatAllowThisSection;

    /**
     * if anonymous or requires login
     * @return anonymous
     */
    public boolean isAnonymous() {
      return false;
    }
    
    /**
     * get sections that if allowed in there, you are allowed in here.  includes "this"
     * @return the sections
     */
    public Set<UiSection> getUiSectionsThatAllowThisSection() {
      if (this.uiSectionsThatAllowThisSection == null) {
        this.uiSectionsThatAllowThisSection = new LinkedHashSet<UiSection>();
      }
      if (!this.uiSectionsThatAllowThisSection.contains(this)) {
        //insert this in the front of the list
        Set<UiSection> newSet = new LinkedHashSet<UiSection>();
        newSet.add(this);
        newSet.addAll(GrouperUtil.nonNull(this.uiSectionsThatAllowThisSection));
        this.uiSectionsThatAllowThisSection = newSet;
      }
      return this.uiSectionsThatAllowThisSection;
    }
    
    /**
     * 
     * @param theMediaKey
     * @param uiSections 
     */
    private UiSection(String theMediaKey, Set<UiSection> uiSections) {
      this.mediaKey = theMediaKey;
      this.uiSectionsThatAllowThisSection = uiSections;
    }
    
    /**
     * getter for media key
     * @return media key
     */
    public String getMediaKey() {
      return this.mediaKey;
    }
    
  }

  /**
   * 
   * @param httpServletRequest
   * @return user name
   */
  public static String remoteUser(HttpServletRequest httpServletRequest) {
    
    Map<String, Object> debugLog = LOG.isDebugEnabled() ? new LinkedHashMap<String, Object>() : null;
    try {
      String remoteUser = httpServletRequest.getRemoteUser();
      
      if (LOG.isDebugEnabled()) {
        debugLog.put("httpServletRequest.getRemoteUser()", remoteUser);
      }
      
      if (StringUtils.isBlank(remoteUser)) {
        String userHeaderName = GrouperUiConfig.retrieveConfig().propertyValueString(
            "grouper.ui.authentication.http.header");
        if (StringUtils.isNotBlank(userHeaderName)) {
          remoteUser = httpServletRequest.getHeader(userHeaderName);
          if (LOG.isDebugEnabled()) {
            debugLog.put(userHeaderName + " header", remoteUser);
          }
        }
      }
      
      if (StringUtils.isBlank(remoteUser)) {
        //this is how mod_jk passes env vars
        remoteUser = (String)httpServletRequest.getAttribute("REMOTE_USER");
        if (LOG.isDebugEnabled()) {
          debugLog.put("REMOTE_USER attribute", remoteUser);
        }
      }
      
      if (StringUtils.isBlank(remoteUser) && httpServletRequest.getUserPrincipal() != null) {
        //this is how mod_jk passes env vars
        remoteUser = httpServletRequest.getUserPrincipal().getName();
        if (LOG.isDebugEnabled()) {
          debugLog.put("httpServletRequest.getUserPrincipal().getName()", remoteUser);
        }
      }
      if (StringUtils.isBlank(remoteUser)) {
        HttpSession session = httpServletRequest.getSession(false);
        remoteUser = (String)(session == null ? null : session.getAttribute("authUser"));
        if (LOG.isDebugEnabled()) {
          debugLog.put("session.getAttribute(authUser)", remoteUser);
        }
      }
      
      remoteUser = StringUtils.trim(remoteUser);
      
      httpServletRequest.getSession().setAttribute("grouperLoginId", remoteUser);

      if (LOG.isDebugEnabled()) {
        debugLog.put("remoteUser overall", remoteUser);
      }

      return remoteUser;
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugLog));
      }
    }
  }
  
  /**
   * servlet context will be /grouper or whatever or empty string if no context
   * @return context
   */
  public static String retrieveServletContext() {
    HttpServletRequest request = retrieveHttpServletRequest();
    if (request == null) {
      throw new NullPointerException("No request");
    }
    String servletContext = request.getContextPath();

    //since this is just a prefix of a path, then if there is no servlet context, it is the empty string
    if (servletContext == null || "/".equals(servletContext)) {
      servletContext = "";
    }
    return servletContext;
  }
  
  /**
   * get the ui section we are in
   * @return true if allowed anonymous
   */
  public static UiSection uiSectionForRequest() {
    
    HttpServletRequest httpServletRequest = retrieveHttpServletRequest();
    UiSection uiSection = (UiSection)httpServletRequest.getAttribute("uiSectionForRequest");
    if (uiSection == null) {
      uiSection = uiSectionForRequestHelper(httpServletRequest);
      httpServletRequest.setAttribute("uiSectionForRequest", uiSection);
    }
    
    return uiSection;
    
  }
  
  /**
   * get the ui section we are in
   * @param httpServletRequest
   * @return true if allowed anonymous
   */
  private static UiSection uiSectionForRequestHelper(HttpServletRequest httpServletRequest) {
    
    String uri = httpServletRequest.getRequestURI();
    
    //TODO might want to check the servlet param name from web.xml: ignore
    if (uri.matches("^/[^/]+/$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/index\\.jsp$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/grouperExternal/app/[^/]+$")) {
      return UiSection.ANONYMOUS;
    }

    if (uri.matches("^/[^/]+/grouperExternal/public/UiV2Public\\.index$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/grouperExternal/public/UiV2Public\\.postIndex$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/grouperUi/app/UiV2Public\\.postIndex$")) {
      return UiSection.ANONYMOUS;
    }
    
    boolean externalServlet = uri.matches("^/[^/]+/grouperExternal/app/[^/]+$");

    String operation = httpServletRequest.getParameter("operation");

    String theClass = null;
    
    if (!StringUtils.isBlank(operation)) {
      theClass = GrouperUtil.prefixOrSuffix(operation, ".", true);
    }
    
    if (externalServlet) {
      
      if (!StringUtils.isBlank(theClass)) {
        if (theClass.startsWith(UiV2ExternalSubjectSelfRegister.class.getSimpleName())) {
          return UiSection.EXTERNAL;
        }
      } else {
        //this means we arent quite to the operation yet
        return UiSection.ANONYMOUS;
      }
      throw new RuntimeException("Cannot use the external servlet for non external operations! '" + uri + "', '" + theClass + "'");
    }
    
    if (!StringUtils.isBlank(operation)) {
      
      //anyone can see the menu...
      if (theClass.equals("Misc") || theClass.equals("MiscMenu")) {
        return UiSection.ANONYMOUS;
      }
    }

    //must be admin UI
    return UiSection.ADMIN_UI;
    
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final long serialVersionUID = 1L;

  /**
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
    GrouperStartup.startup();
    
    InstrumentationThread.startThread(GrouperEngineBuiltin.UI, null);
    
    initOnce();
  }

  /**
   * if initted
   */
  private static boolean inittedOnce = false;

  /**
   * 
   */
  private static void initOnce() {
    
    if (!inittedOnce) {
      synchronized (GrouperUiFilter.class) {
        if (!inittedOnce) {

          //  # If debug is on then restrict to named group. Disable if group does not exist
          //  browser.debug.group=
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("browser.debug.group"));
          
          //  require.group.for.logins
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("require.group.for.logins"));

          //  #users must be in this group to be able to login to the lite membership update UI (if not in require.group.for.logins)
          //  require.group.for.membershipUpdateLite.logins=
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("require.group.for.membershipUpdateLite.logins"));

          //  #users must be in this group to be able to login to the subjectPicker UI (if not in require.group.for.logins or require.group.for.membershipUpdateLite.logins)
          //  require.group.for.subjectPicker.logins=
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("require.group.for.subjectPicker.logins"));

          //  #users must be in this group to invite external users to grouper
          //  require.group.for.inviteExternalSubjects.logins=
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("require.group.for.inviteExternalSubjects.logins"));

          //  #users must be in this group to assign/create/etc attributes in the UI (new attribute framework) (if not in require.group.for.logins)
          //  require.group.for.attributeUpdateLite.logins=
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("require.group.for.attributeUpdateLite.logins"));

          //  grouperUi.autoCreateUserFolderName=
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("grouperUi.autoCreateUserFolderName"));

          //  #if results must be in a certain group, or blank for no check.  e.g. must be in employee group
          //  simpleMembershipUpdate.subjectSearchRequireGroup = 
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("simpleMembershipUpdate.subjectSearchRequireGroup"));

          //  #if results must be in group, or blank for no check.  e.g. put your active employee group here
          //  subjectPicker.defaultSettings.resultsMustBeInGroup = 
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("subjectPicker.defaultSettings.resultsMustBeInGroup"));

          //  # require admin (GrouperSysAdmin or wheel group) to update inherited privileges
          //  uiV2.privilegeInheritanceUpdateRequireGroup = 
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.privilegeInheritanceUpdateRequireGroup"));

          //  # require admin (GrouperSysAdmin or wheel group) to read inherited privileges
          //  uiV2.privilegeInheritanceReadRequireGroup = 
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.privilegeInheritanceReadRequireGroup"));

          //  ###################################
          //  ## V2 UI provisioning settings
          //  ###################################
          //
          //  # put in a group here if you want to restrict the provisioning tab to certin users.  
          //  # note, admins can always see the tab
          //  uiV2.provisioning.must.be.in.group=
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.provisioning.must.be.in.group"));

          //  ###################################
          //  ## V2 UI loader settings
          //  ###################################
          //
          //  # put in a group here if you want to restrict the loader tab to certin users.  
          //  # note, admins can always see the tab
          //  uiV2.loader.must.be.in.group =
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.loader.must.be.in.group"));

          //  # put a group here if you want these users to be able to edit loader jobs
          //  uiV2.loader.edit.if.in.group = 
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.loader.edit.if.in.group"));

          //  # restrict access based on this group, by default only admins can see
          //  # note, admins can always see the screen
          //  uiV2.admin.instrumentation.must.be.in.group =
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.admin.instrumentation.must.be.in.group"));
          
          //  # restrict access based on this group, by default only admins can see
          //  # note, admins can always see the screen
          //  uiV2.admin.daemonJobs.must.be.in.group =
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.admin.daemonJobs.must.be.in.group"));

          //  ###################################
          //  ## V2 UI admin settings
          //  ###################################
          //
          //  # should show subject api diagnostics?
          //  uiV2.admin.subjectApiDiagnostics.show = true
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.admin.subjectApiDiagnostics.show"));

          //  # put in a group here if you want to allow the subject API diagnostics to certin users.  
          //  # note, admins can always see the screen
          //  uiV2.admin.subjectApiDiagnostics.must.be.in.group =
          initGroup(GrouperUiConfig.retrieveConfig().propertyValueString("uiV2.admin.subjectApiDiagnostics.must.be.in.group"));
          
          inittedOnce = true;
        }
      }
    }
  }
  
  /**
   * mark group as cacheable
   * should we auto-create?
   * @param name
   */
  private static void initGroup(final String name) {
    if (StringUtils.isBlank(name)) {
      return;
    }
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        
        Group group = GroupFinder.findByName(grouperSession, name, false);
        if (group != null) {
          
          GroupFinder.groupCacheAsRootAddSystemGroup(group);
          
        }
        
        return null;
      }
    });
  }
  
  /**
   * init request part 1
   * @param httpServletRequest 
   * @param response
   * @return the request wrapper
   */
  public static GrouperRequestWrapper initRequest(GrouperRequestWrapper httpServletRequest, ServletResponse response) {
    
    boolean alreadyInInit = threadLocalInInit.get() != null && threadLocalInInit.get();
    
    threadLocalInInit.set(true);
    
    try {
  
      //servlet will set this...
      threadLocalServlet.remove();
      threadLocalRequest.set(httpServletRequest);
      threadLocalResponse.set((HttpServletResponse) response);
      threadLocalRequestStartMillis.set(System.currentTimeMillis());
      
      httpServletRequest.init();
  
      String uri = httpServletRequest.getRequestURI();
      
      if (uri.matches("^/[^/]+/grouper(Ui|External)/app/[^/]+$")
          && !uri.endsWith("/UiV2Main.index")
          && !uri.endsWith("/UiV2Public.index")) {
        
        RequestContainer.retrieveFromRequest().setAjaxRequest(true);
      }
  
      HooksContext.clearThreadLocal();
      
      GrouperContextTypeBuiltIn.setDefaultContext(GrouperContextTypeBuiltIn.GROUPER_UI);
  
      HttpSession session = httpServletRequest.getSession();
      
      final String remoteUser = remoteUser(httpServletRequest);
    
      Subject subject = null;
  
      GrouperSession grouperSession = SessionInitialiser.getGrouperSession(session);
      
      if (grouperSession != null) {
        subject = grouperSession.getSubject();
      }
      
      UiSection uiSection = uiSectionForRequest();
      
      if (subject == null && !StringUtils.isBlank(remoteUser)) {
        try {
          
          subject = retrieveSubjectLoggedIn();
        } catch (Exception e) {
          if (!uiSection.isAnonymous()) {
            //this is not really ok, but cant do much about it
            String error = "Cant find login subject: " + remoteUser + ", " + uiSection;
            LOG.error(error, e);
            //redirect to the error screen if loginid did not resolve to a subject
            try {
              ((HttpServletResponse)response).sendRedirect(GrouperUiFilter.retrieveServletContext() + "/grouperExternal/public/UiV2Public.index?operation=UiV2Public.postIndex&function=UiV2Public.error&code=authenticatedSubjectNotFound");
            } catch (IOException ioe) {
              throw new RuntimeException("Cant redirect", ioe);
            }
            throw new ControllerDone();
          }
        }
      }
      
      HooksContext.assignSubjectLoggedIn(subject);
      
      //lets add the request, session, and response
      HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_REQUEST, httpServletRequest, false);
      HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SESSION, session, false);
      HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_RESPONSE, response, false);
  
      final GrouperContext grouperContext = GrouperContext.createNewDefaultContext(
          GrouperEngineBuiltin.UI, false, false);
      
      grouperContext.setCallerIpAddress(httpServletRequest.getRemoteAddr());

      
      if (subject != null) {
        final Subject SUBJECT = subject;
        GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
          
          public Object callback(GrouperSession grouperSession2) throws GrouperSessionException {
            //TODO also put this at the login step...
            Member member = MemberFinder.findBySubject(grouperSession2, SUBJECT, true);
            
            grouperContext.setLoggedInMemberId(member.getUuid());
            return null;
          }
        }); 
      }
  
      InstrumentationThread.addCount(InstrumentationDataBuiltinTypes.UI_REQUESTS.name());
      
      return httpServletRequest;
    } catch (RuntimeException re) {
      //log always since might get preempted
      LOG.error("error in init", re);
      if (alreadyInInit) {
        //dont rethrow to reduce looping
        return null;
      }
      throw re;
    } finally {
      threadLocalInInit.remove();
    }
  }
  
  /**
   * put this in a request finally block
   */
  public static void finallyRequest() {
    threadLocalRequest.remove();
    threadLocalResponse.remove();
    threadLocalRequestStartMillis.remove();
    threadLocalServlet.remove();
    
    HooksContext.clearThreadLocal();
    GrouperContext.deleteDefaultContext();
    GrouperThreadLocalState.removeCurrentThreadLocals();
  }

  /**
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @SuppressWarnings("unchecked")
  public void doFilter(ServletRequest servletRequest, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    GrouperRequestWrapper httpServletRequest = null;
    HttpServletResponse httpServletResponse = (HttpServletResponse)response;
    try {
      
      servletRequest.setCharacterEncoding("UTF-8");
      response.setCharacterEncoding("UTF-8");
      
      httpServletRequest = new GrouperRequestWrapper((HttpServletRequest) servletRequest);

      // Set standard HTTP/1.1 no-cache headers.
      ((HttpServletResponse)response).setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");

      // Set standard HTTP/1.0 no-cache header.
      ((HttpServletResponse)response).setHeader("Pragma", "no-cache");

      // belt and suspenders
      ((HttpServletResponse)response).setHeader("Expires", "0");

      httpServletRequest = initRequest(httpServletRequest, response);
  
      try {
        boolean debugSessionSerialization = GrouperUiConfig.retrieveConfig().propertyValueBoolean("debugSessionSerialization", false);

        if (debugSessionSerialization) {
          //TEMPORARY TO SEE WHAT IS NOT SERIALIZIBLE
          HttpSession httpSession = httpServletRequest.getSession();
          
          Enumeration attributeNames = httpSession.getAttributeNames();
          while (attributeNames.hasMoreElements()) {
            String attributeName = (String)attributeNames.nextElement();
            Object object = httpSession.getAttribute(attributeName);
            try {
              ByteArrayOutputStream baos = new ByteArrayOutputStream();
              ObjectOutputStream    out  = new ObjectOutputStream(baos);
              out.writeObject(object);
  
            } catch (Exception e) {
              LOG.error("Error serializing: " + attributeName, e);
              if (object instanceof Map) {
                Map<String, Object> map = (Map)object;
                Set<String> set = map.keySet();
                for (String key : set) {
                  Object current = map.get(key);
                  try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream    out  = new ObjectOutputStream(baos);
                    out.writeObject(current);

                  } catch (Exception e2) {
                    LOG.error("Error serializing in map: " + key, e2);
                  }

                }
              }
            }
          }
          
        }
        
      } catch (Exception e) {
        LOG.error("Error checking debugSessionSerialization", e);
      }
      
      //this makes sure allowed in section
      Subject subjectLoggedIn = retrieveSubjectLoggedIn(true, httpServletResponse);
      UiSection uiSection = uiSectionForRequest();
      ensureUserAllowedInSection(uiSection, subjectLoggedIn, httpServletResponse);

      TextContainer.retrieveFromRequest();

      ensureUserFolder(subjectLoggedIn, httpServletRequest);
      
      filterChain.doFilter(httpServletRequest, response);
      
    } catch (ControllerDone cd) {
      //ignore
    } catch (Throwable t) {      
      
      GrouperUiUtils.appendErrorToRequest(ExceptionUtils.getFullStackTrace(t));
      LOG.error("UI error", t);

      //make a friendly response if not ajax
      if (!RequestContainer.retrieveFromRequest().isAjaxRequest()) {
//todo remove Lite UI
//
//        String msg = t.getMessage();
//        httpServletRequest.setAttribute("seriousError",msg);
//        //for some reason this has to be able the getRequestDispatcher...
//        RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/filterError.do");
//        try {
//          if (!response.isCommitted()) {
//            response.setContentType("text/html");
//            rd.forward(httpServletRequest, response);
//          } else {
//            rd.include(httpServletRequest, response);
//          }
//        }catch(Throwable tt) {
//          LOG.error("Failed to include error page:", tt);
//          ((HttpServletResponse)response).sendError(500);
//        }

//errorCode_ajaxError
        try {
          httpServletResponse.sendRedirect(GrouperUiFilter.retrieveServletContext() + "/grouperExternal/public/UiV2Public.index?operation=UiV2Public.postIndex&function=UiV2Public.error&code=ajaxError");
        } catch (IOException ioe) {
          throw new RuntimeException("Error", ioe);
        }
        //throw new ControllerDone();
      }
    } finally {
      sendErrorEmailIfNeeded();
     
      finallyRequest();
      
      ServletRequestUtils.requestEnd();
    }
  }

  /**
   * if configured to create user folders, do that
   * @param subjectLoggedIn
   * @param httpServletRequest 
   */
  private static void ensureUserFolder(final Subject subjectLoggedIn, final HttpServletRequest httpServletRequest) {
    
    if (subjectLoggedIn != null && GrouperUiConfig.retrieveConfig().propertyValueBoolean("grouperUi.autoCreateUserFolderOnLogin")) {

      //see if done for session
      Boolean done = (Boolean)httpServletRequest.getSession().getAttribute("autoCreatedUserFolderOnLoginDone");
      
      if (done == null || !done) {
      
        //even if not successful, dont try again
        httpServletRequest.getSession().setAttribute("autoCreatedUserFolderOnLoginDone", true);
        
        String folderName = GrouperUiConfig.retrieveConfig().propertyValueStringRequired("grouperUi.autoCreateUserFolderName");
  
        GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
        GrouperSession grouperSession = grouperSessionResult.getGrouperSession();

        try {
                
          Map<String, Object> variableMap = new HashMap<String, Object>();
          variableMap.put("subject", subjectLoggedIn);
          
          folderName = GrouperUtil.substituteExpressionLanguage(folderName, variableMap, true, false, false);
      
          final String FOLDER_NAME = folderName;
          
          //make sure no more vars
          if (!folderName.contains("$")) {
            
            Stem stem = StemFinder.findByName(grouperSession, FOLDER_NAME, false);
            
            if (stem != null) {
              if (LOG.isDebugEnabled()) {
                LOG.debug("Folder " + FOLDER_NAME + ", exists for user: " + GrouperUtil.subjectToString(subjectLoggedIn));
              }
              return;
            }
            
            boolean autoCreateParents = GrouperUiConfig.retrieveConfig()
                .propertyValueBoolean("grouperUi.autoCreateUserFolderCreateParentFoldersIfNotExist", false);
            
            
            //stem doesnt exist, lets create
            stem = new StemSave(grouperSession).assignName(FOLDER_NAME).assignCreateParentStemsIfNotExist(autoCreateParents).save();
            
            //grant to user
            stem.grantPriv(subjectLoggedIn, NamingPrivilege.CREATE, false);
            stem.grantPriv(subjectLoggedIn, NamingPrivilege.STEM, false);
            
            //take out the system priv to remove clutter
            stem.revokePriv(grouperSession.getSubject(), NamingPrivilege.STEM, false);
            
            LOG.warn("Created user folder: " + FOLDER_NAME + ", for subject: " + GrouperUtil.subjectToString(subjectLoggedIn));
            return;
            
          }
          
  
        } catch (Exception e) {
          //dont kill the login
          LOG.error("Cannot create user folder for: " + folderName + ", " + GrouperUtil.subjectToString(subjectLoggedIn), e);
          return;
        } finally {
          if (grouperSessionResult.isCreated()) {
            GrouperSession.stopQuietly(grouperSession);
          }
        }
        
      }
    }
  }
  
  /**
   * thread local for servlet
   */
  private static ThreadLocal<HttpServlet> threadLocalServlet = new ThreadLocal<HttpServlet>();

  /**
   * thread local for servlet
   */
  private static ThreadLocal<Boolean> threadLocalInInit = new ThreadLocal<Boolean>();

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
   * public method to get the http servlet
   * 
   * @param httpServlet is servlet to assign
   */
  public static void assignHttpServlet(HttpServlet httpServlet) {
    threadLocalServlet.set(httpServlet);
    ContextContainer.instance().storeToContext();
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
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
    InstrumentationThread.shutdownThread();
  }

  
  /**
   * send error email if needed
   */
  public void sendErrorEmailIfNeeded() {
    try {
      HttpServletRequest httpServletRequest = retrieveHttpServletRequest();

      String error = httpServletRequest == null ? null : (String)httpServletRequest.getAttribute("error");
      if (!StringUtils.isBlank(error)) {
        
        String errorMailAddresses = GrouperUiConfig.retrieveConfig().propertyValueString("errorMailAddresses");

        if (!StringUtils.isBlank(errorMailAddresses)) {
          
          String loggedInSubjectString = "dont know";
          try {
            Subject loggedInSubject = retrieveSubjectLoggedIn();
            if (loggedInSubject == null) {
              loggedInSubjectString = "none";
            } else {
              loggedInSubjectString = loggedInSubject.getSource().getId() + " - " + loggedInSubject.getId();
            }
          } catch (RuntimeException re) {
            LOG.error(re);
          }
          
          String requestParams = GrouperUiUtils.requestParams();
          error = "Server name: " + GrouperUtil.hostname() + "\n" 
            + "IP Address: " + httpServletRequest.getRemoteAddr() + "\n"
            + "User: " + loggedInSubjectString + "\n"
            + "URL: " + httpServletRequest.getRequestURL() + "\n"
            + "Request params: " + requestParams + "\n"
            + "\n\nError: " + error; 
          
          new GrouperEmail().setTo(errorMailAddresses).setSubject("grouperUi error").setBody(error).send();
        }
      }
      
    } catch (Exception e) {
      LOG.error("Error sending email", e);
    }
  }


}
