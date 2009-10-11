/**
 * 
 */
package edu.internet2.middleware.grouper.grouperUi.j2ee;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.ContextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.exceptions.ControllerDone;
import edu.internet2.middleware.grouper.grouperUi.tags.TagUtils;
import edu.internet2.middleware.grouper.grouperUi.util.GuiUtils;
import edu.internet2.middleware.grouper.hooks.beans.GrouperContextTypeBuiltIn;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/**
 * Extend the servlet to get user info
 * 
 * @author mchyzer
 * 
 */
public class GrouperUiJ2ee implements Filter {

  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(GrouperUiJ2ee.class);

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
   * retrieve the subject logged in to web service
   * If there are four colons, then this is the source and subjectId since
   * overlap in namespace
   * 
   * @return the subject
   */
  @SuppressWarnings({ "unchecked" })
  public static Subject retrieveSubjectLoggedIn() {
    
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
      GrouperSession grouperSession = null;
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
          String error = "User not in ui group: " + groupToRequire;
          LOG.error(error);
          GuiUtils.appendErrorToRequest(error);
          GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();
          guiResponseJs.addAction(GuiScreenAction.newAlert(
              GuiUtils.message("simpleMembershipUpdate.notAllowedInUi")));
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
   * execute logic and pass up the line
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

    //lets add the request, session, and response
    HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_REQUEST, httpServletRequest, false);
    HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SESSION, 
        httpServletRequest.getSession(), false);
    HooksContext.setAttributeThreadLocal(HooksContext.KEY_HTTP_SERVLET_RESPONSE, response, false);

    try {

      filterChain.doFilter(httpServletRequest, response);
      
    } catch (RuntimeException re) {
      GuiUtils.appendErrorToRequest(ExceptionUtils.getFullStackTrace(re));
      LOG.error(re);
      throw re;
    } finally {
      sendErrorEmailIfNeeded();
      threadLocalRequest.remove();
      threadLocalResponse.remove();
      threadLocalRequestStartMillis.remove();
      threadLocalServlet.remove();
      
      HooksContext.clearThreadLocal();
    }

  }

  /**
   * filter method
   * @param arg0 
   * @throws ServletException 
   */
  public void init(FilterConfig arg0) throws ServletException {
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
          
          String requestParams = GuiUtils.requestParams();
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
