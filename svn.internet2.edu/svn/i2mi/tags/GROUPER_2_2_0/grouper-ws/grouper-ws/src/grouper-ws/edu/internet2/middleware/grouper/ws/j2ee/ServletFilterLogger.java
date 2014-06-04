/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouper.ws.j2ee;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * log requests and responses
 */
public class ServletFilterLogger implements Filter {

  /**
   * logger 
   */
  @SuppressWarnings("unused")
  private static final Log LOG = LogFactory.getLog(ServletFilterLogger.class);

  /**
   * @see javax.servlet.Filter#destroy()
   */
  @Override
  public void destroy() {
    //empty
  }

  /**
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
      FilterChain filterChain)
      throws IOException, ServletException {

    //see if logging
    if (!LOG.isDebugEnabled()) {
      filterChain.doFilter(servletRequest, servletResponse);
      return;
    }

    HttpServletRequestCopier requestCopier = new HttpServletRequestCopier(
        (HttpServletRequest) servletRequest);
    HttpServletResponseCopier responseCopier = new HttpServletResponseCopier(
        (HttpServletResponse) servletResponse);

    try {
      filterChain.doFilter(requestCopier, responseCopier);
    } finally {
      logStuff(requestCopier, responseCopier);
    }

  }

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig arg0) throws ServletException {
    //nothing
  }

  /**
   * @param servletRequest
   * @param servletResponse
   */
  @SuppressWarnings("unchecked")
  public static void logStuff(HttpServletRequestCopier servletRequest, HttpServletResponseCopier servletResponse) {
    if (LOG.isDebugEnabled()) {
      try {
        HttpServletRequestCopier requestCopier = servletRequest;
        HttpServletResponseCopier responseCopier = servletResponse;
        
        servletRequest.getParameterNames();
        StringBuilder requestParams = new StringBuilder();
        Enumeration enumeration = servletRequest.getParameterNames();
        while (enumeration.hasMoreElements()) {
          String name = (String) enumeration.nextElement();
          requestParams.append(name + " = " + servletRequest.getParameter(name) + ", ");
        }
        
        requestCopier.finishReading();
        responseCopier.flushBuffer();
        byte[] requestCopy = requestCopier.getCopy();
        byte[] responseCopy = responseCopier.getCopy();
        LOG.debug("IP: " + ((HttpServletRequest) servletRequest).getRemoteAddr() 
            + ", url: " + ((HttpServletRequest) servletRequest).getRequestURI()
            + ", queryString: " + ((HttpServletRequest) servletRequest).getQueryString()
            + ", method: " + ((HttpServletRequest) servletRequest).getMethod()
            + ", content-type: " + ((HttpServletRequest) servletRequest).getContentType()
            + "\nrequest params: " + requestParams.toString()
            + "\nrequest body: " + new String(requestCopy) 
            + "\nrespone headers: " + responseCopier.getHeaders() 
            + "response: " + new String(responseCopy, servletResponse.getCharacterEncoding()));
      } catch (Exception e) {
        LOG.error("Error logging request/response", e);
      }
    }
  }


}
