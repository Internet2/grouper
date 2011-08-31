/**
 * @author mchyzer
 * $Id: HttpContentType.java,v 1.1 2009-10-11 22:04:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ui.util;


/**
 * http content type
 */
public enum HttpContentType {

  /** plain text content type */
  TEXT_PLAIN("text/plain"),
  
  /** xml content type */
  TEXT_XML("text/xml"),
  
  /** text html content type */
  TEXT_HTML("text/html"),
  
  /** application json content type TODO, should this be UTF-8? */
  APPLICATION_JSON("application/json;charset=iso-8859-1"),
  
  /** text comma separated values */
  TEXT_CSV("text/csv");
  
  /**
   * content type for HTTP
   */
  private String contentType;
  
  /**
   * construct with content type
   * @param theContentType
   */
  private HttpContentType(String theContentType) {
    this.contentType = theContentType;
  }
  
  /**
   * getter for contentType
   * @return contentType
   */
  public String getContentType() {
    return this.contentType;
  }
  
  /**
   * 
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString() {
    return this.getContentType();
  }
}
