/*******************************************************************************
 * Copyright 2016 Internet2
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
 *******************************************************************************/
/**
 * 
 */
package edu.internet2.middleware.tierApiAuthzServer.j2ee;

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

import edu.internet2.middleware.tierApiAuthzServer.util.StandardApiServerUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Extend the servlet to get user info
 * 
 * @author mchyzer
 * 
 */
public class TaasFilterJ2ee implements Filter {

  /** logger */
  private static final Log LOG = LogFactory.getLog(TaasFilterJ2ee.class);

  /**
   * if in request, get the start time
   * @return the start time
   */
  public static long retrieveRequestStartMillis() {
    Long requestStartMillis = threadLocalRequestStartMillis.get();
    return StandardApiServerUtils.longValue(requestStartMillis, 0);
  }

  /**
   * client ip address
   * @return the address
   */
  public static String clientIp() {
    HttpServletRequest request = retrieveHttpServletRequest();
    String ipAddress = request.getHeader("X-FORWARDED-FOR");  
    if (ipAddress == null) {  
      ipAddress = request.getRemoteAddr();  
    }
    return ipAddress;
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
   * thread local for original request
   */
  private static ThreadLocal<HttpServletRequest> threadLocalOriginalRequest = new ThreadLocal<HttpServletRequest>();

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
   * public method to get the original http servlet request
   * 
   * @return the original http servlet request
   */
  public static HttpServletRequest retrieveOriginalHttpServletRequest() {
    return threadLocalOriginalRequest.get();
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
  
      threadLocalOriginalRequest.set((HttpServletRequest) request);

      //wrap this for single valued params or whatever
      request = new AsasHttpServletRequest((HttpServletRequest)request);
      
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
      threadLocalOriginalRequest.remove();
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
