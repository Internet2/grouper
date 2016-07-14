/**
 * 
 */
package edu.internet2.middleware.grouper.ws.scim;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author vsachdeva
 *
 */
@WebFilter(filterName = "TierFilter", urlPatterns = {"/*"})
public class TierFilter implements Filter {
  
  /**
   * thread local to store request start time in miilis
   */
  private static ThreadLocal<Long> threadLocalRequestStartMillis = new ThreadLocal<Long>();

  /**
   * if in request, get the start time
   * @return the start time
   */
  public static long retrieveRequestStartMillis() {
    Long requestStartMillis = threadLocalRequestStartMillis.get();
    return GrouperUtil.longValue(requestStartMillis, 0);
  }
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
    
    try {
      threadLocalRequestStartMillis.set(System.currentTimeMillis());
      chain.doFilter(request, response);
    } finally {
      threadLocalRequestStartMillis.remove();
    }
    
  }

  @Override
  public void destroy() {}
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

}
