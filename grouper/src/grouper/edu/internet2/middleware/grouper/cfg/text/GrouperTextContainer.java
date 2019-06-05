/**
 * @author mchyzer
 * $Id: TextContainer.java,v 1.1 2013/06/20 06:02:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.cfg.text;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.GrouperUiApiTextConfig;


/**
 * text container in request for user
 */
public class GrouperTextContainer {

  /** logger */
  protected static final Log LOG = LogFactory.getLog(GrouperTextContainer.class);

  /**
   * get the text or null if not found
   * @param key
   * @return the text or null if not found
   */
  public static String textOrNull(String key) {
    GrouperUiApiTextConfig grouperTextConfig = GrouperUiApiTextConfig.retrieveTextConfig();
    String value = grouperTextConfig.propertyValueString(key);
    value = massageText(key, value, false);
    return value;
  }

  /**
   * keep servlet request in threadlocal during request from filter
   */
  private static ThreadLocal<ServletRequest> servletRequestThreadLocal = new InheritableThreadLocal<ServletRequest>();
  
  /**
   * assign thread local from filter
   * @param servletRequest
   */
  public static void servletRequestThreadLocalAssign(ServletRequest servletRequest) {
    servletRequestThreadLocal.set(servletRequest);
  }

  /**
   * clea thread local
   */
  public static void servletRequestThreadLocalClear() {
    servletRequestThreadLocal.remove();
  }
  
  /**
   * keep grouper requestcontainer in threadlocal during request from filter
   */
  private static ThreadLocal<Object> grouperRequestContainerThreadLocal = new InheritableThreadLocal<Object>();
  
  /**
   * keep grouper requestcontainer in threadlocal during request from filter
   * @param grouperRequestContainer
   */
  public static void grouperRequestContainerThreadLocalAssign(Object grouperRequestContainer) {
    grouperRequestContainerThreadLocal.set(grouperRequestContainer);
  }

  /**
   * keep grouper requestcontainer in threadlocal during request from filter
   */
  public static void grouperRequestContainerThreadLocalClear() {
    grouperRequestContainerThreadLocal.remove();
  }
  
  /**
   * if there is no request, use this one
   */
  private static GrouperTextContainer textContainerIfNoRequest = new GrouperTextContainer();
  
  /**
   * retrieve the container from the request or create a new one if not there
   * @return the container
   */
  public static GrouperTextContainer retrieveFromRequest() {
    
    ServletRequest servletRequest = servletRequestThreadLocal.get();
    GrouperTextContainer textContainer = servletRequest == null ? null : 
        (GrouperTextContainer)servletRequest.getAttribute("textContainer");
    
    if (textContainer == null) {
      textContainer = new GrouperTextContainer();
      if (servletRequest != null) {
        servletRequest.setAttribute(
            "textContainer", textContainer);
      }
    }
  
    return textContainer;
  }

  /**
   * text value for key
   * @param key
   * @return text value
   */
  private static String textValue(String key) {
    GrouperUiApiTextConfig grouperTextConfig = GrouperUiApiTextConfig.retrieveTextConfig();
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
    return massageText(key, value, true);
  }

  /**
   * massage text with substitutions etc
   * @param key
   * @param value
   * @param errorIfNotFound true if error text if not found
   * @return the text
   */
  public static String massageText(String key, String value, boolean errorIfNotFound) {
    if (StringUtils.isBlank(value)) {
      if (!errorIfNotFound) {
        return null;
      }
      LOG.error("Cant find text for variable: '" + key + "'");
      return "$$not found: " + StringEscapeUtils.escapeHtml(key) + "$$";
    }
    try {
      //if there might be a scriptlet
      if (value.contains("${")) {
        Map<String, Object> substituteMap = new LinkedHashMap<String, Object>();
        Object grouperRequestContainer = grouperRequestContainerThreadLocal.get();
        if (grouperRequestContainer != null) {
          substituteMap.put("grouperRequestContainer", grouperRequestContainer);
        }
        
        ServletRequest servletRequest = servletRequestThreadLocal.get();
        
        substituteMap.put("request", servletRequest);
        substituteMap.put("textContainer", GrouperTextContainer.retrieveFromRequest());
        value = GrouperUtil.substituteExpressionLanguage(value, substituteMap, true, false, true);
      }
      return value;
    } catch (RuntimeException re) {
      GrouperUtil.injectInException(re, "Problem with key: '" + key + "', value: '" + value + "'");
      throw re;
    }
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
      String textValue = textValue((String)key);
      return textValue;
    }

  };
  
  /**
   * text map
   */
  @SuppressWarnings("serial")
  private Map<String, String> textMapWithTooltip = new HashMap<String, String>() {

    /**
     * @see java.util.HashMap#get(java.lang.Object)
     */
    @Override
    public String get(Object key) {
      String textValue = textValue((String)key);
      String textValueWithTooltip = convertTextToTooltip((String)key, textValue);
      return textValueWithTooltip;
    }

  };
  
  /**
   * convert text to tooltip
   * @param key
   * @param value
   * @return the tooltip html
   */
  public String convertTextToTooltip(String key, String value) {
    String targettedTooltipKey = GrouperUiApiTextConfig.TOOLTIP_TARGETTED_PREFIX + key;
    String targettedTooltipRefKey = GrouperUiApiTextConfig.TOOLTIP_TARGETTED_REF_PREFIX + key;

    GrouperUiApiTextConfig grouperUiTextConfig = GrouperUiApiTextConfig.retrieveTextConfig();

    String targettedTooltipValue = null;

    if (grouperUiTextConfig != null) {
      String targettedTooltipRefValue = grouperUiTextConfig
          .propertyValueString(targettedTooltipRefKey);

      targettedTooltipValue = grouperUiTextConfig
          .propertyValueString(targettedTooltipKey);

      if (StringUtils.isNotBlank(targettedTooltipRefValue)
          && StringUtils.isNotBlank(targettedTooltipValue)) {
        LOG.warn("Duplicate tooltip target and ref in text config: "
            + targettedTooltipKey
            + ", " + targettedTooltipRefKey);
      }

      if (StringUtils.isBlank(targettedTooltipValue)) {

        //first priority is a targetted ref, next priority is a target.  not sure why both would be there
        targettedTooltipValue = StringUtils.isBlank(targettedTooltipRefValue) ?
            targettedTooltipValue : grouperUiTextConfig
                .propertyValueString(targettedTooltipRefValue);

        //if there is a ref, but it doesnt exist, that is a problem
        if (StringUtils.isNotBlank(targettedTooltipRefValue)
            && StringUtils.isBlank(targettedTooltipValue)) {
          LOG.error("Missing tooltip targetted ref in nav.properties: "
              + targettedTooltipRefValue);
        }

      }
    }
    boolean hasTooltip = StringUtils.isNotBlank(targettedTooltipValue);

    if (LOG.isDebugEnabled()) {
      LOG.debug("Tooltip key: " + targettedTooltipKey + " has tooltip? " + hasTooltip);
    }

    if (hasTooltip && !StringUtils.equals(targettedTooltipValue, value)) {

      //replace the whole message with tooltip
      value = convertTooltipTextToHtml(targettedTooltipValue, value, false);

    } else {

      //CH 20080129 at this point we need to make the tooltip substitutions
      value = substituteTooltips(value, false);

    }
    return value;
  }  

  /**
   * substitute tooltips
   * @param message
   * @param isIgnoreTooltipStyle true if should ignore tooltip style
   * @return the substituted strings
   */
  public String substituteTooltips(String message, boolean isIgnoreTooltipStyle) {
    
    //first step, get the map of substitutions
    GrouperUiApiTextConfig grouperUiTextConfig = GrouperUiApiTextConfig.retrieveTextConfig();
    List<String> theTooltipKeys = grouperUiTextConfig.tooltipKeys();
    List<String> theTooltipValues = grouperUiTextConfig.tooltipValues();
    
    //substitute, and remove if replaced
    String result = GrouperUtil.replace(message, theTooltipKeys, theTooltipValues, false, true);
    
    //maybe take out styles
    if (isIgnoreTooltipStyle) {
      result = StringUtils.replace(result, "class=\"grouperTooltip\" ", "");
    }
    
    return result;
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
    //substitute single quotes for javascript
    //tooltipText = tooltipText.replace("'", "\\'");
    //put it in the tooltip code
    //the class="tooltip" is substituted later, so if this is changed, change in other places as well
    String escapedTooltipText = StringUtils.replace(tooltipText, "'", "&#39;");
    escapedTooltipText = GrouperUtil.escapeHtml(escapedTooltipText, true, true);
    tooltipText = "<span " + (isIgnoreTooltipStyle ? "" : "class=\"grouperTooltip\" ") 
      + "onmouseover=\"grouperTooltip('" 
      + escapedTooltipText + "');\" onmouseout=\"UnTip()\">" + term + "</span>";
    return tooltipText;
  }
  
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
   * text map escape XML including double quotes
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
   * text map with tooltips if applicable
   * @return the text object with tooltips
   */
  public Map<String, String> getTextWithTooltip() {
    return this.textMapWithTooltip;
  }
  
  /**
   * text map, escape single quotes
   * @return the text object
   */
  public Map<String, String> getTextEscapeSingle() {
    return this.textEscapeSingleMap;
  }
  
  
  /**
   * text map, escape xml including double quotes
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
