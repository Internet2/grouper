/*
 * @author mchyzer
 * $Id: GrouperRequestWrapper.java,v 1.1 2009-08-10 21:35:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.j2ee;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;


/**
 *
 */
public class GrouperRequestWrapper extends HttpServletRequestWrapper {

  /** wrapper around session */
  private GrouperSessionWrapper grouperSessionWrapper = null;
  
  /**
   * <pre>
   * problem with tomcat and request wrappers, so stash this and
   * get back later:
   * http://brian.pontarelli.com/2008/01/16/httpservletrequestwrapper-tomcat-and-forwards/
   * </pre>
   */
  private StringBuffer requestURL = null;
  
  /**
   * @param request
   */
  public GrouperRequestWrapper(HttpServletRequest request) {
    super(request);
    this.requestURL = request.getRequestURL();
  }

  /**
   * 
   * @see javax.servlet.http.HttpServletRequestWrapper#getSession()
   */
  @Override
  public HttpSession getSession() {
    HttpSession session = super.getSession();
    
    if (this.grouperSessionWrapper == null 
        || this.grouperSessionWrapper.getHttpSession() != session) {
      this.grouperSessionWrapper = new GrouperSessionWrapper(session);
    }
    
    return this.grouperSessionWrapper;
  }

  /**
   * 
   * @see javax.servlet.http.HttpServletRequestWrapper#getSession(boolean)
   */
  @Override
  public HttpSession getSession(boolean create) {
    HttpSession session = super.getSession(create);
    
    if (this.grouperSessionWrapper == null 
        || this.grouperSessionWrapper.getHttpSession() != session) {
      this.grouperSessionWrapper = new GrouperSessionWrapper(session);
    }
    
    return this.grouperSessionWrapper;
  }

  /**
   * <pre>
   * problem with tomcat and request wrappers, so stash this and
   * get back later:
   * http://brian.pontarelli.com/2008/01/16/httpservletrequestwrapper-tomcat-and-forwards/
   * </pre>
   * @see javax.servlet.http.HttpServletRequestWrapper#getRequestURL()
   */
  @Override
  public StringBuffer getRequestURL() {
    return this.requestURL;
  }

  
  
  
}
