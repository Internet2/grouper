/**
 * 
 */
package edu.internet2.middleware.grouper.j2ee.status;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author mchyzer
 *
 */
public class GrouperStatusFilter implements Filter {

  /**
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
      throws IOException, ServletException {
    
    HttpServletRequest request = (HttpServletRequest)req;
    HttpServletResponse response = (HttpServletResponse)res;
    if (GrouperUtil.defaultString(request.getRequestURI()).endsWith("/status")) {
      new GrouperStatusServlet().doGet(request, response);
      return;
    }
    filterChain.doFilter(request, response);
    
  }

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig arg0) throws ServletException {
  }

}
