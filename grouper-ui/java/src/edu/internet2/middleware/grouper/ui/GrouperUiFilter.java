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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.grouperUi.beans.ContextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.RequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.ExternalSubjectSelfRegister;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.InviteExternalSubjects;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Generic filter for ui for grouper (e.g. set hooks context)
 * 
 * @author Chris Hyzer.
 * @version $Id: GrouperUiFilter.java,v 1.8 2009-10-11 22:04:18 mchyzer Exp $
 */

public class GrouperUiFilter implements Filter {

  /** keep 100k in memory, why not */
  private static FileItemFactory fileItemFactory = new DiskFileItemFactory(100000, null);

  /** Create a new file upload handler */
  private static ServletFileUpload upload = new ServletFileUpload(fileItemFactory);

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
   * if the media resource bundle is null, use this
   * @return properties
   */
  public static Properties retrieveMediaProperties() {
    //cant find in session, this should be a special case, like during startup
    Properties propertiesSettings = GrouperUtil
      .propertiesFromResourceName("resources/grouper/media.properties");
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
    
    GrouperSession grouperSession = SessionInitialiser.getGrouperSession(retrieveHttpServletRequest().getSession());
    if (grouperSession != null && grouperSession.getSubject() != null) {
      return grouperSession.getSubject();
    }
    
    SessionContainer sessionContainer = SessionContainer.retrieveFromSession();
    
    Subject subjectLoggedIn = sessionContainer.getSubjectLoggedIn();
    
    HttpServletRequest request = retrieveHttpServletRequest();

    UiSection uiSectionForRequest = uiSectionForRequest();

    if (subjectLoggedIn != null) {
      return subjectLoggedIn;
    }
  
  
    //currently assumes user is in getUserPrincipal
    String userIdLoggedIn = remoteUser(request);

    if (StringUtils.isBlank(userIdLoggedIn) && uiSectionForRequest.isAnonymous()) {
      return null;
    }

    if (StringUtils.isBlank(userIdLoggedIn)) {
      throw new RuntimeException("Cant find logged in user");
    }

    try {
      subjectLoggedIn = SubjectFinder.findByIdOrIdentifier(userIdLoggedIn, true);
    } catch (RuntimeException re) {
      //this is probably a system error...  not a user error
      GrouperUtil.injectInException(re, "Cant find subject from login id: " + userIdLoggedIn);
      throw re;
    }
    
    ensureUserAllowedInSection(uiSectionForRequest, subjectLoggedIn);
    
    sessionContainer.setSubjectLoggedIn(subjectLoggedIn);
    
    return subjectLoggedIn;

  }

  /**
   * make sure user is allowed in this section
   * @param uiSection
   * @param subjectLoggedIn
   */
  private static void ensureUserAllowedInSection(UiSection uiSection, Subject subjectLoggedIn) {

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
        String thisError = requireUiGroup(mediaKey, subjectLoggedIn);
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
   * @return the error message group name
   */
  private static String requireUiGroup(String mediaKeyOfGroup, Subject subjectLoggedIn) {
    
    //see if member of login group
    String groupToRequire = TagUtils.mediaResourceString(mediaKeyOfGroup);
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
        if (!group.hasMember(subjectLoggedIn)) {
          
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
    INVITE_EXTERNAL_SUBJECTS("require.group.for.inviteExternalSubjects.logins", null),
    
    /** simple membership update */
    SIMPLE_MEMBERSHIP_UPDATE("require.group.for.membershipUpdateLite.logins", GrouperUtil.toSet(ADMIN_UI)),
    
    /** simple membership update */
    SIMPLE_ATTRIBUTE_UPDATE("require.group.for.attributeUpdateLite.logins", GrouperUtil.toSet(ADMIN_UI)),
    
    /** subject picker */
    SUBJECT_PICKER("require.group.for.subjectPicker.logins", GrouperUtil.toSet(ADMIN_UI, SIMPLE_MEMBERSHIP_UPDATE));
    
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
    String remoteUser = httpServletRequest.getRemoteUser();
    
    if (StringUtils.isBlank(remoteUser)) {
      //this is how mod_jk passes env vars
      remoteUser = (String)httpServletRequest.getAttribute("REMOTE_USER");
    }
    
    if (StringUtils.isBlank(remoteUser) && httpServletRequest.getUserPrincipal() != null) {
      //this is how mod_jk passes env vars
      remoteUser = httpServletRequest.getUserPrincipal().getName();
    }
    if (StringUtils.isBlank(remoteUser)) {
      HttpSession session = httpServletRequest.getSession(false);
      remoteUser = (String)(session == null ? null : session.getAttribute("authUser"));
    }
    
    remoteUser = StringUtils.trim(remoteUser);
    
    httpServletRequest.getSession().setAttribute("grouperLoginId", remoteUser);
    
    return remoteUser;
  }
  
  
  /**
   * get the ui section we are in
   * @param httpServletRequest
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
    if (uri.matches("^/[^/]+/index\\.jsp$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/populateIndex\\.do$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/callLogin\\.do$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/error\\.do$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/logout\\.do$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/grouperExternal[/]?.*/$")) {
      return UiSection.ANONYMOUS;
    }
    if (uri.matches("^/[^/]+/grouperExternal[/]?.*/index.html$")) {
      return UiSection.ANONYMOUS;
    }
    
    boolean externalServlet = uri.matches("^/[^/]+/grouperExternal/appHtml/grouper\\.html$")
        || uri.matches("^/[^/]+/grouperExternal/app/[^/]+$");

    String operation = null;

    if (uri.matches("^/[^/]+/grouper(Ui|External)/appHtml/grouper\\.html$")) {

      //must be in simple membership update or subject picker
      operation = httpServletRequest.getParameter("operation");

      if (StringUtils.isBlank(operation) && !externalServlet) {

        return UiSection.SIMPLE_MEMBERSHIP_UPDATE;
      }

    } else if (uri.matches("^/[^/]+/grouper(Ui|External)/app/[^/]+$")) {
      
      //must be in simple membership update or subject picker
      int lastLastIndex = uri.lastIndexOf('/');
      operation = uri.substring(lastLastIndex+1);
    }

    String theClass = null;
    
    if (!StringUtils.isBlank(operation)) {
      
      theClass = GrouperUtil.prefixOrSuffix(operation, ".", true);
    }
    
    if (externalServlet) {
      
      if (!StringUtils.isBlank(theClass)) {
        if (theClass.startsWith(ExternalSubjectSelfRegister.class.getSimpleName())) {
          return UiSection.EXTERNAL;
        }
      } else {
        //this means we arent quite to the operation yet
        return UiSection.ANONYMOUS;
      }
      throw new RuntimeException("Cannot use the external servlet for non external operations! '" + uri + "', '" + theClass + "'");
    }
    
    if (!StringUtils.isBlank(operation)) {
      
      if (theClass.startsWith("SimpleAttributeUpdate")) {
        return UiSection.SIMPLE_ATTRIBUTE_UPDATE;
      }
      if (theClass.equals("Misc") || theClass.startsWith("SimpleMembershipUpdate")) {
        return UiSection.SIMPLE_MEMBERSHIP_UPDATE;
      }
      if (theClass.startsWith("SubjectPicker") || theClass.startsWith("AttributeDefNamePicker")) {
        return UiSection.SUBJECT_PICKER;
      }
      if (theClass.startsWith(InviteExternalSubjects.class.getSimpleName())) {
        return UiSection.INVITE_EXTERNAL_SUBJECTS;
      }
    }

    //must be admin UI
    return UiSection.ADMIN_UI;
    
  }
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
    GrouperStartup.startup();
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
      
      if (uri.matches("^/[^/]+/grouper(Ui|External)/app/[^/]+$")) {
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
        GrouperSession rootSession = null;
        try {
          
          rootSession = GrouperSession.startRootSession(false);
          subject = (Subject)GrouperSession.callbackGrouperSession(rootSession, new GrouperSessionHandler() {
            
            /**
             * we need a grouper session since subject searching also looks at groups
             * @param callbackGrouperSession
             */
            @Override
            public Object callback(GrouperSession callbackGrouperSession) throws GrouperSessionException {
              
              return SubjectFinder.findByIdOrIdentifier(remoteUser, true);
            }
          });
        } catch (Exception e) {
          if (!uiSection.isAnonymous()) {
            //this is not really ok, but cant do much about it
            String error = "Cant find login subject: " + remoteUser + ", " + uiSection;
            LOG.error(error, e);
            throw new RuntimeException(error);
          }
        } finally {
          GrouperSession.stopQuietly(rootSession);
  
        }
      }
      
      HooksContext.assignSubjectLoggedIn(subject);
      
      //lets add the request, session, and response
      HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_REQUEST, httpServletRequest, false);
      HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SESSION, session, false);
      HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_RESPONSE, response, false);
  
      GrouperContext grouperContext = GrouperContext.createNewDefaultContext(
          GrouperEngineBuiltin.UI, false, false);
      
      grouperContext.setCallerIpAddress(httpServletRequest.getRemoteAddr());
      
      GrouperSession rootSession = grouperSession == null ? 
          GrouperSession.startRootSession(false) : grouperSession.internal_getRootSession();
      
      if (subject != null) {
        //TODO also put this at the login step...
        Member member = MemberFinder.findBySubject(rootSession, subject, true);
        
        grouperContext.setLoggedInMemberId(member.getUuid());
      }
  
  
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

  }

  /**
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @SuppressWarnings("unchecked")
  public void doFilter(ServletRequest servletRequest, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    GrouperRequestWrapper httpServletRequest = null;
    
    try {
      
      servletRequest.setCharacterEncoding("UTF-8");
      
      httpServletRequest = new GrouperRequestWrapper((HttpServletRequest) servletRequest);
      
      httpServletRequest = initRequest(httpServletRequest, response);
  
      try {
        boolean debugSessionSerialization = TagUtils.mediaResourceBoolean("debugSessionSerialization", false);

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
      
      filterChain.doFilter(httpServletRequest, response);
      
    } catch (ControllerDone cd) {
      //ignore
    } catch (Throwable t) {      
      
      GrouperUiUtils.appendErrorToRequest(ExceptionUtils.getFullStackTrace(t));
      LOG.error("UI error", t);

      //make a friendly response if not ajax
      if (!RequestContainer.retrieveFromRequest().isAjaxRequest()) {
      
        String msg = t.getMessage();
        httpServletRequest.setAttribute("seriousError",msg);
        //for some reason this has to be able the getRequestDispatcher...
        RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/filterError.do");
        try {
          if (!response.isCommitted()) {
            response.setContentType("text/html");
            rd.forward(httpServletRequest, response);
          } else {
            rd.include(httpServletRequest, response);
          }
        }catch(Throwable tt) {
          LOG.error("Failed to include error page:", tt);
          ((HttpServletResponse)response).sendError(500);
        }
      }      
    } finally {
      sendErrorEmailIfNeeded();
     
      finallyRequest();
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
    // not needed

  }

  
  /**
   * send error email if needed
   */
  public void sendErrorEmailIfNeeded() {
    try {
      HttpServletRequest httpServletRequest = retrieveHttpServletRequest();

      String error = (String)httpServletRequest.getAttribute("error");
      if (!StringUtils.isBlank(error)) {
        
        String errorMailAddresses = TagUtils.mediaResourceString("errorMailAddresses");

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