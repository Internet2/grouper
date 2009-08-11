/*
 * @author mchyzer
 * $Id: GrouperRequestWrapper.java,v 1.2 2009-08-11 13:44:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.j2ee;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;

import edu.internet2.middleware.grouper.grouperUi.GrouperUiJ2ee;


/**
 *
 */
public class GrouperRequestWrapper extends HttpServletRequestWrapper {

  /** wrapper around session */
  private GrouperSessionWrapper grouperSessionWrapper = null;
  
  /**
   * get a param from file request as string (normal param)
   * @param name
   * @return the param
   */
  public String getParameterFileItemString(String name) {
    List<FileItem> fileItems = GrouperUiJ2ee.fileItems();
    for (FileItem fileItem : fileItems) {
      if (name.equals(fileItem.getFieldName())) {
        return fileItem.getString();
      }
    }
    return null;
  }
  
  /**
   * get a param from file request as fileItem
   * @param name
   * @return the param
   */
  public FileItem getParameterFileItem(String name) {
    List<FileItem> fileItems = GrouperUiJ2ee.fileItems();
    for (FileItem fileItem : fileItems) {
      if (name.equals(fileItem.getFieldName())) {
        return fileItem;
      }
    }
    return null;
  }
  
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
