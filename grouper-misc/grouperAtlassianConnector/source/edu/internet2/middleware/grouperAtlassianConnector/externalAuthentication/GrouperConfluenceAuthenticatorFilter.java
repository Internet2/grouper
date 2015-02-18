/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.externalAuthentication;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;


/**
 *
 */
public class GrouperConfluenceAuthenticatorFilter implements Filter {

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
    
    ServletRequest httpServletRequestWrapper = servletRequest;

    //dont have endless loops
    if (!(httpServletRequestWrapper instanceof GrouperConfluenceAuthenticatorRequestWrapper)) {
      httpServletRequestWrapper = new GrouperConfluenceAuthenticatorRequestWrapper((HttpServletRequest)servletRequest);
    }

    filterChain.doFilter(httpServletRequestWrapper, servletResponse);
    
  }

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig arg0) throws ServletException {
  }

}
