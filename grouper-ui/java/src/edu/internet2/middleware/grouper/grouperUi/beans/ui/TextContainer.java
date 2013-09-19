/**
 * @author mchyzer
 * $Id: TextContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.GrouperUiTextConfig;


/**
 * text container in request for user
 */
public class TextContainer {

  protected static final Log LOG = LogFactory.getLog(TextContainer.class);

  /**
   * retrieve the container from the request or create a new one if not there
   * @return the container
   */
  public static TextContainer retrieveFromRequest() {
    
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();

    
    TextContainer textContainer = 
        (TextContainer)httpServletRequest.getAttribute("textContainer");
    
    if (textContainer == null) {
      textContainer = new TextContainer();
      httpServletRequest.setAttribute(
          "textContainer", textContainer);
    }
    
    return textContainer;
    
  }

  /**
   * text value for key
   * @param key
   * @return text value
   */
  private static String textValue(String key) {
    GrouperUiTextConfig grouperTextConfig = GrouperUiTextConfig.retrieveTextConfig();
    String value = grouperTextConfig.propertyValueString(key);
    value = massageText(key, value);
    return value;

  }

  /**
   * massage text with substitutions etc
   * @param key
   * @param value
   * @return the text
   */
  public static String massageText(String key, String value) {
    if (StringUtils.isBlank(value)) {
      LOG.error("Cant find text for variable: '" + key + "'");
      return "$$not found: " + key + "$$";
    }
    
    //if there might be a scriptlet
    if (value.contains("${")) {
      Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
      substituteMap.put("grouperRequestContainer", GrouperRequestContainer.retrieveFromRequestOrCreate());
      substituteMap.put("request", GrouperUiFilter.retrieveHttpServletRequest());
      substituteMap.put("textContainer", TextContainer.retrieveFromRequest());
      value = GrouperUtil.substituteExpressionLanguage(value, substituteMap, true, false, false);
    }
    return value;
  }
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return textValue((String)key);
    }

  };
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textEscapeSingleMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return GrouperUtil.escapeSingleQuotes(textValue((String)key));
    }

  };
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textEscapeXmlMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return GrouperUtil.xmlEscape(textValue((String)key), true);
    }

  };
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textEscapeDoubleMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return GrouperUtil.escapeDoubleQuotes(textValue((String)key));
    }

  };
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textEscapeSingleDoubleMap = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      return GrouperUtil.escapeSingleQuotes(GrouperUtil.escapeDoubleQuotes(textValue((String)key)));
    }

  };
  

  /**
   * text map
   * @return the text object
   */
  public Map<String, String> getText() {
    return this.textMap;
  }
  
  /**
   * text map, escape single quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeSingle() {
    return this.textEscapeSingleMap;
  }
  
  
  /**
   * text map, escape xml
   * @return the text object
   */
  public Map<String, String> getTextEscapeXml() {
    return this.textEscapeXmlMap;
  }
  
  /**
   * text map, escape double quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeDouble() {
    return this.textEscapeDoubleMap;
  }
  
  /**
   * text map, escape single and double quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeSingleDouble() {
    return this.textEscapeSingleDoubleMap;
  }
  

  
  
  
  
}
