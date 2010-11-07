/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperKimConnector.misc;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.kuali.rice.kew.edl.RequestParser;

/**
 * 
 */
public class HttpRequestThreadLocalFilter implements Filter {

  /** keep the request here */
  private static ThreadLocal<HttpServletRequest> threadLocalHttpServletRequest = new ThreadLocal<HttpServletRequest>();
  
  /**
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    
    HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
    threadLocalHttpServletRequest.set(httpServletRequest);
    try {
      filterChain.doFilter(httpServletRequest, servletResponse);
    } finally {
      threadLocalHttpServletRequest.remove();
    }
  }

  /**
   * 
   * @return the request
   */
  public static HttpServletRequest httpServletRequest() {
    return threadLocalHttpServletRequest.get();
  }
  
  /**
   * see if this is a post
   * @return if this is a post
   */
  public static boolean isPost() {
    HttpServletRequest httpServletRequest = threadLocalHttpServletRequest.get();
    if (httpServletRequest != null) {
      if (httpServletRequest.getMethod() == "POST") {
        return true;
      }
    }
    return false;
  }
  
  /**
   * parameter value
   * @param parameterName
   * @return the parameter value
   */
  public static String getParameterValue(String parameterName) {
    HttpServletRequest httpServletRequest = threadLocalHttpServletRequest.get();
    if (httpServletRequest != null) {
      RequestParser requestParser = new RequestParser(httpServletRequest);
      String value = requestParser.getParameterValue(parameterName);
      return value;
    }
    return null;
  }
  
  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig arg0) throws ServletException {
  }

}
