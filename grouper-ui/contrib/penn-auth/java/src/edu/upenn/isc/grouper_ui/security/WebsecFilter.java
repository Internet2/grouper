/*
 * @author mchyzer
 * $Id: WebsecFilter.java,v 1.3 2009-11-25 20:01:26 mchyzer Exp $
 */
package edu.upenn.isc.grouper_ui.security;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;


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
    
    //if no user, redirect to the login page
    if (StringUtils.isBlank(pennWebsecRequestWrapper.getRemoteUser())) {
      
      Properties mediaProperties = GrouperUiUtils
        .propertiesFromResourceName("resources/custom/media.properties");
      
      String loginUrl = StringUtils.defaultIfEmpty((String)mediaProperties.get("pennWebsecLoginUrl"), 
          "https://rosetta.upenn.edu/cgi-bin/websec/websec_authform?app=StudentHome&websec_page=$thisUrl$");
      
      //find out this url
      String thisUrl = pennWebsecRequestWrapper.getRequestURL().toString();
      loginUrl = StringUtils.replace(loginUrl, "$thisUrl$", thisUrl);
      
      response.sendRedirect(loginUrl);
      
      //note, this doesnt continue calling the chain
      return;
      
    }
    
    filterChain.doFilter(pennWebsecRequestWrapper, res);
  }

  /**
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig arg0) throws ServletException {
  }

}
