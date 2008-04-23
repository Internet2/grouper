/*
 * @author mchyzer
 * $Id: WebsecFilter.java,v 1.1 2008-04-23 20:32:14 mchyzer Exp $
 */
package edu.upenn.isc.grouper_ui.security;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 *
 */
public class WebsecFilter implements Filter {

  /**
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
      throws IOException, ServletException {
    
    HttpServletRequest request  = (HttpServletRequest)req;
    HttpServletResponse response = (HttpServletResponse)res;
    
    PennWebsecRequestWrapper pennWebsecRequestWrapper = new PennWebsecRequestWrapper(request, response);
    
    filterChain.doFilter(pennWebsecRequestWrapper, res);
  }

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig arg0) throws ServletException {
  }

}
