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
