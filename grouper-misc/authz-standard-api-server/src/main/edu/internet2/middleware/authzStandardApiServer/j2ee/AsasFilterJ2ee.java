/**
 * 
 */
package edu.internet2.middleware.authzStandardApiServer.j2ee;

import java.io.IOException;
import java.security.Principal;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.authzStandardApiServer.util.StandardApiServerUtils;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.logging.Log;
import edu.internet2.middleware.authzStandardApiServerExt.org.apache.commons.logging.LogFactory;

/**
 * Extend the servlet to get user info
 * 
 * @author mchyzer
 * 
 */
public class AsasFilterJ2ee implements Filter {

  /** logger */
  private static final Log LOG = LogFactory.getLog(AsasFilterJ2ee.class);

  /**
   * if in request, get the start time
   * @return the start time
   */
  public static long retrieveRequestStartMillis() {
    Long requestStartMillis = threadLocalRequestStartMillis.get();
    return StandardApiServerUtils.longValue(requestStartMillis, 0);
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
      return paramMap.get(key);
    }
    String[] values = httpServletRequest.getParameterValues(key);
    if (values == null || values.length == 0) {
      return null;
    }
    //there is probably something wrong if multiple values detected
    if (values.length > 1) {
      throw new RuntimeException(
          "Multiple request parameter values where detected for key: " + key
              + ", when only one is expected: " + StandardApiServerUtils.toStringForLog(values));
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
    StandardApiServerUtils
        .assertion(httpServletRequest != null,
            "HttpServletRequest is null, is the AsasRestServlet mapped in the web.xml?");
    Principal principal = httpServletRequest.getUserPrincipal();
    String principalName = null;
    if (principal == null) {
      principalName = httpServletRequest.getRemoteUser();
      if (StandardApiServerUtils.isBlank(principalName)) {
        principalName = (String)httpServletRequest.getAttribute("REMOTE_USER");
      }
    } else {
      principalName = principal.getName();
    }
    StandardApiServerUtils.assertion(StandardApiServerUtils.isNotBlank(principalName),
        "There is no user logged in, make sure the container requires authentication");
    return principalName;
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
    // not needed

  }

  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain filterChain) throws IOException, ServletException {

    try {
  
      //servlet will set this...
      threadLocalServlet.remove();
      threadLocalRequest.set((HttpServletRequest) request);
      threadLocalResponse.set((HttpServletResponse) response);
      threadLocalRequestStartMillis.set(System.currentTimeMillis());
      
    
      filterChain.doFilter(request, response);
    } catch (RuntimeException re) {
      LOG.info("error in request", re);
      throw re;
    } finally {
      threadLocalRequest.remove();
      threadLocalResponse.remove();
      threadLocalRequestStartMillis.remove();
      threadLocalServlet.remove();
      
    }

  }

  /**
   * filter method
   */
  public void init(FilterConfig arg0) throws ServletException {
    // not needed
  }

}
