/**
 * @author mchyzer
 * $Id: TextContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.Map;

import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;


/**
 * text container in request for user
 */
public class TextContainer {

  /**
   * get the text or null if not found
   * @param key
   * @return the text or null if not found
   */
  public static String textOrNull(String key) {
    return GrouperTextContainer.textOrNull(key);
  }
  
  /**
   * delegate
   */
  private GrouperTextContainer grouperTextContainer = null;
  
  /**
   * retrieve the container from the request or create a new one if not there
   * @return the container
   */
  public static TextContainer retrieveFromRequest() {
    TextContainer textContainer = new TextContainer();
    textContainer.grouperTextContainer = GrouperTextContainer.retrieveFromRequest();
    return textContainer;
  }

  /**
   * massage text with substitutions etc
   * @param key
   * @param value
   * @return the text
   */
  public static String massageText(String key, String value) {
    return GrouperTextContainer.massageText(key, value);
  }

  /**
   * massage text with substitutions etc
   * @param key
   * @param value
   * @param errorIfNotFound true if error text if not found
   * @return the text
   */
  public static String massageText(String key, String value, boolean errorIfNotFound) {
    return GrouperTextContainer.massageText(key, value, errorIfNotFound);
  }
  
  /**
   * convert text to tooltip
   * @param key
   * @param value
   * @return the tooltip html
   */
  public String convertTextToTooltip(String key, String value) {
    return this.grouperTextContainer.convertTextToTooltip(key, value);
  }  

  /**
   * substitute tooltips
   * @param message
   * @param isIgnoreTooltipStyle true if should ignore tooltip style
   * @return the substituted strings
   */
  public String substituteTooltips(String message, boolean isIgnoreTooltipStyle) {
    return this.grouperTextContainer.substituteTooltips(message, isIgnoreTooltipStyle);
  }

  /**
   * convert tooltip text to html
   * @param tooltipText 
   * @param term
   * @param isIgnoreTooltipStyle if tooltip style should be ignored
   * @return the html tooltip text
   */
  public static String convertTooltipTextToHtml(String tooltipText, String term, 
      boolean isIgnoreTooltipStyle) {
    return GrouperTextContainer.convertTooltipTextToHtml(tooltipText, term, isIgnoreTooltipStyle);
  }
  
  /**
   * text map
   * @return the text object
   */
  public Map<String, String> getText() {
    return this.grouperTextContainer.getText();
  }
  
  /**
   * text map with tooltips if applicable
   * @return the text object with tooltips
   */
  public Map<String, String> getTextWithTooltip() {
    return this.grouperTextContainer.getTextWithTooltip();
  }
  
  /**
   * text map, escape single quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeSingle() {
    return this.grouperTextContainer.getTextEscapeSingle();
  }
  
  
  /**
   * text map, escape xml including double quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeXml() {
    return this.grouperTextContainer.getTextEscapeXml();
  }
  
  /**
   * text map, escape double quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeDouble() {
    return this.grouperTextContainer.getTextEscapeDouble();
  }
  
  /**
   * text map, escape single and double quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeSingleDouble() {
    return this.grouperTextContainer.getTextEscapeSingleDouble();
  }
  
}
