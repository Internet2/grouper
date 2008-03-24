/*
 * @author mchyzer $Id: WsLiteUtils.java,v 1.1 2008-03-24 20:19:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws.lite;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee;

/**
 * utility methods for gruoper web services lite
 */
public class WsLiteUtils {

  /**
   * return the current act as subject id
   * @return the id
   */
  public static String actAsSubjectId() {
    HttpServletRequest request = GrouperServiceJ2ee.retrieveHttpServletRequest();
    String actAsSubjectId = request.getParameter("actAsSubjectId");
    return actAsSubjectId;

  }

  /**
   * return the current act as subject id
   * @return the id
   */
  public static String actAsSubjectSource() {
    HttpServletRequest request = GrouperServiceJ2ee.retrieveHttpServletRequest();
    String actAsSubjectSource = request.getParameter("actAsSubjectSource");
    return actAsSubjectSource;

  }

  /**
   * return the current act as subject identifier
   * @return the identifier
   */
  public static String actAsSubjectIdentifier() {
    HttpServletRequest request = GrouperServiceJ2ee.retrieveHttpServletRequest();
    String actAsSubjectIdentifier = request.getParameter("actAsSubjectIdentifier");
    return actAsSubjectIdentifier;

  }

}
