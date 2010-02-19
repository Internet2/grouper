/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;


/**
 * filter HTML
 */
public interface HtmlFilter {

  /**
   * filter html from a string
   * @param html
   * @return the html to filter
   */
  public String filterHtml(String html);
  
}
