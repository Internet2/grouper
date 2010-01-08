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

import java.io.IOException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.grouperUi.beans.ContextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.ui.tags.TagUtils;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

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
   * 
   * @return the user principal name
   */
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
  @SuppressWarnings({ "unchecked" })
  public static Subject retrieveSubjectLoggedIn() {
    
    GrouperSession grouperSession = SessionInitialiser.getGrouperSession(retrieveHttpServletRequest().getSession());
    if (grouperSession != null && grouperSession.getSubject() != null) {
      return grouperSession.getSubject();
    }
    
    SessionContainer sessionContainer = SessionContainer.retrieveFromSession();
    
    Subject subjectLoggedIn = sessionContainer.getSubjectLoggedIn();

    if (subjectLoggedIn != null) {
      return subjectLoggedIn;
    }

    
    //currently assumes user is in getUserPrincipal
    HttpServletRequest request = retrieveHttpServletRequest();
    String userIdLoggedIn = request.getRemoteUser();
    
    if (StringUtils.isBlank(userIdLoggedIn)) {
      if (request.getUserPrincipal() != null) {
        userIdLoggedIn = request.getUserPrincipal().getName();
      }
    }

    if (StringUtils.isBlank(userIdLoggedIn)) {
      userIdLoggedIn = (String)request.getAttribute("REMOTE_USER");
    }
    
    if (StringUtils.isBlank(userIdLoggedIn)) {
      throw new RuntimeException("Cant find logged in user");
    }
    
    try {
      try {
        subjectLoggedIn = SubjectFinder.findById(userIdLoggedIn);
      } catch (SubjectNotFoundException snfe) {
        // if not found, then try any identifier
        subjectLoggedIn = SubjectFinder.findByIdentifier(userIdLoggedIn);
      }
    } catch (Exception e) {
      //this is probably a system error...  not a user error
      throw new RuntimeException("Cant find subject from login id: " + userIdLoggedIn, e);
    }
    
    //see if member of login group
    String groupToRequire = TagUtils.mediaResourceString("require.group.for.logins");
    if (!StringUtils.isBlank(groupToRequire)) {

      //get a session, close it if you started it
      boolean startedSession = false;
      grouperSession = null;
      try {
        grouperSession = GrouperSession.staticGrouperSession(false);
        if (grouperSession == null) {
          grouperSession = GrouperSession.startRootSession();
          startedSession = true;
        }
        if (!PrivilegeHelper.isWheelOrRoot(grouperSession.getSubject())) {
          grouperSession = grouperSession.internal_getRootSession();
        }
        Group group = GroupFinder.findByName(grouperSession, groupToRequire);
        if (!group.hasMember(subjectLoggedIn)) {
          String error = "User not in ui group: " + groupToRequire + ", " + subjectLoggedIn;
          LOG.error(error);
          GrouperUiUtils.appendErrorToRequest(error);
          GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
          guiResponseJs.addAction(GuiScreenAction.newAlert(
              GrouperUiUtils.message("simpleMembershipUpdate.notAllowedInUi")));
          throw new ControllerDone();
          
        }
        
      } catch (ControllerDone cd) {
        throw cd;
      } catch (Exception e) {
        throw new RuntimeException("Problem with user: " + userIdLoggedIn + ", " + groupToRequire, e);
      } finally {
        if (startedSession) {
          GrouperSession.stopQuietly(grouperSession);
        }
      }
    }
    
    sessionContainer.setSubjectLoggedIn(subjectLoggedIn);
    
    return subjectLoggedIn;

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
   * 
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest arg0, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    GrouperRequestWrapper httpServletRequest = new GrouperRequestWrapper((HttpServletRequest) arg0);
    
    //servlet will set this...
    threadLocalServlet.remove();
    threadLocalRequest.set(httpServletRequest);
    threadLocalResponse.set((HttpServletResponse) response);
    threadLocalRequestStartMillis.set(System.currentTimeMillis());
    
    httpServletRequest.init();
    
    GrouperContextTypeBuiltIn.setDefaultContext(GrouperContextTypeBuiltIn.GROUPER_UI);

    HttpSession session = httpServletRequest.getSession();
    
    String remoteUser = httpServletRequest.getRemoteUser();
    if (remoteUser == null || remoteUser.length() == 0) {
      remoteUser = (String)(session == null ? null : session.getAttribute("authUser"));
    }

    HooksContext.clearThreadLocal();
    
    GrouperContextTypeBuiltIn.setDefaultContext(GrouperContextTypeBuiltIn.GROUPER_UI);

    Subject subject = null;

    GrouperSession grouperSession = SessionInitialiser.getGrouperSession(session);
    
    if (grouperSession != null) {
      subject = grouperSession.getSubject();
    }
    
    if (subject == null && !StringUtils.isBlank(remoteUser)) {
      try {
        subject = SubjectFinder.findByIdOrIdentifier(remoteUser, true);
      } catch (Exception e) {
        //this is not really ok, but cant do much about it
        String error = "Cant find login subject: " + remoteUser;
        LOG.error(error, e);
        throw new RuntimeException(error);
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

    try {


      filterChain.doFilter(httpServletRequest, response);
      
    } catch (RuntimeException re) {
      GrouperUiUtils.appendErrorToRequest(ExceptionUtils.getFullStackTrace(re));
      LOG.error(re);
      throw re;
    } finally {
      sendErrorEmailIfNeeded();
      threadLocalRequest.remove();
      threadLocalResponse.remove();
      threadLocalRequestStartMillis.remove();
      threadLocalServlet.remove();
      
      HooksContext.clearThreadLocal();
      GrouperContext.deleteDefaultContext();
    }

  }

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