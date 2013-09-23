/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Pennsylvania

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.ui.tags;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.taglibs.standard.tag.common.fmt.BundleSupport;
import org.apache.taglibs.standard.tag.common.fmt.SetLocaleSupport;
import org.apache.taglibs.standard.tag.el.fmt.MessageTag;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.j2ee.GrouperRequestWrapper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiUtils;
import edu.internet2.middleware.grouper.ui.util.MapBundleWrapper;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <p>
 * A handler for &lt;message&gt; that accepts attributes as Strings and
 * evaluates them as expressions at runtime. Substitutes keywords into
 * underlined tooltips.  The default bundle is "${nav}".  If the value is
 * provided, use that instead of looking up in a properties file, and perhaps
 * do a tooltip lookup
 * </p>
 * 
 * @author Chris Hyzer
 */
public class GrouperMessageTag extends MessageTag {

  /** logger */
  private static final Log LOG = LogFactory.getLog(GrouperMessageTag.class);

  /** in the nav.properties, terms must start with this prefix.  multiple terms can exist for one tooltip */
  private static final String TERM_PREFIX = "term.";

  /** in the nav.properties, tooltips must start with this prefix.  there should be one and only one tooltip for a term.
   * tooltips and terms are linked by the common name, which is the suffix of the nav.properties key.
   * e.g. tooltip.group=A group is a collection
   * term.group=Group
   * term.group=group
   * 
   * these are linked because they all end in "group"
   */
  public static final String TOOLTIP_PREFIX = "tooltip.";

  /**
   * target a tooltip on a certain message
   */
  public static final String TOOLTIP_TARGETTED_PREFIX = "tooltipTargetted.";
  
  /**
   * target a tooltip on a certain message, and make the value of the tooltip
   * a reference to another tooltip
   */
  public static final String TOOLTIP_TARGETTED_REF_PREFIX = "tooltipTargettedRef.";
  
  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /**
   * 
   */
  public GrouperMessageTag() {
    this.init();

    
  }

  /**
   * @see org.apache.taglibs.standard.tag.el.fmt.MessageTag#release()
   */
  @Override
  public void release() {
    super.release();
    this.init();
  }
  
  /** if directly putting in the tooltip, do so here */
  private String valueTooltip = null;
  
  /**
   * if directly putting in the tooltip, do so here
   * @param valueTooltip1
   */
  public void setValueTooltip(String valueTooltip1) {
    this.valueTooltip = valueTooltip1;
  }

  /** if specified use this value and not lookup in resource file */
  private String value = null;

  /** if using the value, this is the key of the tooltip (could also use ref).
   * Actually, there is still the tooltipTargetted or tooltipTargettedRef prefix */
  private String valueTooltipKey = null;
  
  /**
   * init vars
   */
  private void init() {
    this.tooltipDisable = null;
    this.tooltipKeys = null;
    this.tooltipValues = null;
    this.useNewTermContext = null;
    this.escapeSingleQuotes = null;
    this.tooltipRef = null;
    this.ignoreTooltipStyle = null;
    this.value = null;
    this.valueTooltip = null;
    this.valueTooltipKey = null;
  }

  /** 
   * terms only display once per page.  but if a term is in something hidden (e.g. infodot target),
   * then it might be used on page, but user cant see unless they open that infodot.  So this 
   * will reset the context so that in this text it will use all terms and they can be used later 
   * in page as well. 
   */
  private String useNewTermContext = null;

  /**
   * if tooltip style should be ignored
   */
  private String ignoreTooltipStyle = null;
  
  /**
   * for a tooltip on this message (similar to a targetted tooltip), this is name
   * from nav.properties
   */
  private String tooltipRef = null;
  
  /**
   * see if use new term context (and validate the boolean, default to false if not set)
   * @return if use new term context
   */
  public boolean useNewTermContext() {
    return GrouperUtil.booleanValue(this.useNewTermContext, false);
  }
  
  /** if we should not do tooltips for this tag */
  private String tooltipDisable = null;
  
  /** if we should escape single quotes with \' for javascript, or if escaping html, escape to &apos; */
  private String escapeSingleQuotes = null;
  
  /**
   * if escaping HTML chars
   */
  private boolean escapeHtml = false;
  
  
  /**
   * @param escapeHtml1 the escapeHtml to set
   */
  public void setEscapeHtml(boolean escapeHtml1) {
    this.escapeHtml = escapeHtml1;
  }

  /**
   * see if disabled (and validate the boolean, default to false if not set)
   * @return if disabled
   */
  public boolean tooltipDisable() {
    return GrouperUtil.booleanValue(this.tooltipDisable, false);
  }
  
  /**
   * this is overridden to put the tooltips in the text
   */
  @SuppressWarnings("unchecked")
  @Override
  public int doEndTag() throws JspException {

    // see if we are using tooltips
    boolean dontDoTooltips = (this.tooltipDisable() || !GrouperConfig.getPropertyBoolean(
        GrouperConfig.MESSAGES_USE_TOOLTIPS, true));

    //maybe there is a value
    String message = this.value;
    String tooltipKey = StringUtils.isBlank(this.valueTooltipKey) ? null : this.valueTooltipKey;
    LocalizationContext locCtxt = null;
    if (StringUtils.isBlank(message)) { 
      // now duplicate the super method
      String keyInput = null;
  
      // determine the message key by...
      if (this.keySpecified) {
        // ... reading 'key' attribute
        keyInput = this.keyAttrValue;
      } else {
        // ... retrieving and trimming our body
        if (this.bodyContent != null && this.bodyContent.getString() != null)
          keyInput = this.bodyContent.getString().trim();
      }
  
      if ((keyInput == null) || keyInput.equals("")) {
        try {
          this.pageContext.getOut().print("??????");
        } catch (IOException ioe) {
          throw new JspTagException(ioe.getMessage());
        }
        return EVAL_PAGE;
      }
  
      String prefix = null;
      
      if (!this.bundleSpecified) {
        Tag t = findAncestorWithClass(this, BundleSupport.class);
        if (t != null) {
          // use resource bundle from parent <bundle> tag
          BundleSupport parent = (BundleSupport) t;
          locCtxt = parent.getLocalizationContext();
          prefix = parent.getPrefix();
        } else {
          locCtxt = BundleSupport.getLocalizationContext(this.pageContext);
        }
      } else {
        // localization context taken from 'bundle' attribute
        locCtxt = this.bundleAttrValue;
        if (locCtxt != null && locCtxt.getLocale() != null) {
          GrouperUtil.callMethod(SetLocaleSupport.class,
              "setResponseLocale", new Class[] { PageContext.class,
                  Locale.class }, new Object[] {
                  this.pageContext, locCtxt.getLocale() });
          /*
           * SetLocaleSupport.setResponseLocale(pageContext,
           * locCtxt.getLocale());
           */
        }
      }
  
      // Perform parametric replacement if required
      List params = (List) GrouperUtil.fieldValue(this, "params");
  
      message = UNDEFINED_KEY + keyInput + UNDEFINED_KEY;
      if (locCtxt != null) {
        ResourceBundle bundle = locCtxt.getResourceBundle();
        if (bundle != null) {
          try {
            // prepend 'prefix' attribute from parent bundle
            if (prefix != null)
              keyInput = prefix + keyInput;
            message = bundle.getString(keyInput);
            if (!params.isEmpty()) {
              Object[] messageArgs = params.toArray();
              MessageFormat formatter = new MessageFormat("");
              if (locCtxt.getLocale() != null) {
                formatter.setLocale(locCtxt.getLocale());
              }
              formatter.applyPattern(message);
              message = formatter.format(messageArgs);
            }
          } catch (MissingResourceException mre) {
            message = UNDEFINED_KEY + keyInput + UNDEFINED_KEY;
          }
        }
      }
      //use the tooltip key as the keyInput
      tooltipKey = keyInput;
    }
    
    if (!StringUtils.isBlank(tooltipKey) || !StringUtils.isBlank(this.valueTooltip)) {
      String targettedTooltipKey = TOOLTIP_TARGETTED_PREFIX + tooltipKey;
      String targettedTooltipRefKey = TOOLTIP_TARGETTED_REF_PREFIX + tooltipKey;
      
      MapBundleWrapper mapBundleWrapper = (MapBundleWrapper)((HttpServletRequest)this
          .pageContext.getRequest()).getSession().getAttribute("navNullMap");
      
      String targettedTooltipValue = null;
      
      if (mapBundleWrapper != null) {
        String targettedTooltipRefValue = (String)mapBundleWrapper.get(targettedTooltipRefKey);
    
        targettedTooltipValue = StringUtils.isNotBlank(this.tooltipRef) ?
            (String)mapBundleWrapper.get(this.tooltipRef) :(String)mapBundleWrapper.get(targettedTooltipKey);
        
        if (StringUtils.isNotBlank(targettedTooltipRefValue) && StringUtils.isNotBlank(targettedTooltipValue)) {
          LOG.warn("Duplicate tooltip target and ref in nav.properties: " + targettedTooltipKey 
              + ", " + targettedTooltipRefKey);
        }
        if ((StringUtils.isNotBlank(targettedTooltipRefValue) 
            || StringUtils.isNotBlank((String)mapBundleWrapper.get(targettedTooltipKey)))
            && StringUtils.isNotBlank(this.tooltipRef)) {
          LOG.warn("targettedTooltip and tooltipRef set at once! '" + targettedTooltipKey 
              + "', '" + targettedTooltipRefKey + "', '" + this.tooltipRef + "'");
        }
        
        //first priority is value
        targettedTooltipValue = StringUtils.isBlank(this.valueTooltip) ? targettedTooltipValue : this.valueTooltip;
        
        if (StringUtils.isBlank(targettedTooltipValue)) {
        
          //first priority is a targetted ref, next priority is a target.  not sure why both would be there
          targettedTooltipValue = StringUtils.isBlank(targettedTooltipRefValue) ? 
              targettedTooltipValue : (String)mapBundleWrapper.get(targettedTooltipRefValue);
        
          //if there is a ref, but it doesnt exist, that is a problem
          if (StringUtils.isNotBlank(targettedTooltipRefValue) && StringUtils.isBlank(targettedTooltipValue)) {
            LOG.error("Missing tooltip targetted ref in nav.properties: " + targettedTooltipRefValue);
          }
      
  
        }
      }
      boolean isIgnoreTooltipStyle = GrouperUtil.booleanValue(this.ignoreTooltipStyle, false);
      boolean hasTooltip = StringUtils.isNotBlank(targettedTooltipValue);
      
      LOG.debug("Tooltip key: " + targettedTooltipKey + " has tooltip? " + hasTooltip);
      
      if (!dontDoTooltips) {
        if (hasTooltip) {
          
          //replace the whole message with tooltip
          message = convertTooltipTextToHtml(targettedTooltipValue, message, isIgnoreTooltipStyle);
          
        } else {
          
          //CH 20080129 at this point we need to make the tooltip substitutions
          message = substituteTooltips(message, isIgnoreTooltipStyle);
          
        }
      }
    }
    
    boolean isEscapeSingleQuotes = GrouperUtil.booleanValue(this.escapeSingleQuotes, false);
    if (this.escapeHtml) {
      message = GrouperUiUtils.escapeHtml(message, true, isEscapeSingleQuotes);
    } else {
      //maybe this is a javascript message and should be escaped (e.g. tooltip)
      if (isEscapeSingleQuotes) {
        message = StringUtils.replace(message, "'", "\\'");
      }
    }
    String var = (String) GrouperUtil.fieldValue(this, "var");
    if (var != null) {
      int scope = (Integer) GrouperUtil.fieldValue(this, "scope");
      this.pageContext.setAttribute(var, message, scope);
    } else {
      try {
        this.pageContext.getOut().print(message);
      } catch (IOException ioe) {
        throw new JspTagException(ioe.getMessage());
      }
    }

    return EVAL_PAGE;
  }

  /**
   * init and return the tooltip keys
   * @return the list of keys
   */
  @SuppressWarnings("unchecked")
  private List<String> tooltipValues() {
    //init everything
    tooltipKeys();
    return this.tooltipValues;
  }
  
  /** tooltip keys */
  private List<String> tooltipKeys = null;
  
  /** tooltip values in order of keys */
  private List<String> tooltipValues = null;

  /**
   * init and return the tooltip keys
   * @return the list of keys
   */
  @SuppressWarnings("unchecked")
  private List<String> tooltipKeys() {
    //first step, get the map of substitutions
    this.tooltipKeys = (List<String>)this.pageContext
      .getRequest().getAttribute("tooltipKeys");
    this.tooltipValues = (List<String>)this.pageContext
    .getRequest().getAttribute("tooltipValues");
    
    if (this.tooltipKeys == null || this.useNewTermContext()) {
      
      //add the tooltips:
      ResourceBundle resourceBundle = GrouperUiUtils.getNavResourcesStatic(GrouperUiFilter.retrieveHttpServletRequest().getSession());
      
      //add properties to map
      Map<String, String> propertiesConfigurationMap = convertPropertiesToMap(null, resourceBundle, true);

      Set<String> propertiesKeys = propertiesConfigurationMap == null ? null : 
        propertiesConfigurationMap.keySet();
      Map<String, String> propertiesMap = null;

      //loop through the tooltips
      if (propertiesKeys != null) {
        propertiesMap = new HashMap<String, String>();
        for (String propertiesKey : propertiesKeys) {
          
          //see if a key
          boolean isTerm = propertiesKey.startsWith(GrouperMessageTag.TERM_PREFIX);
          boolean isValue = !isTerm && propertiesKey.startsWith(GrouperMessageTag.TOOLTIP_PREFIX);
          
          //validate
          if (!isTerm && !isValue) {
            throw new RuntimeException("Illegal entry in tooltips.properties (or local or localized), " +
                "must start with term. or tooltip. : '" + propertiesKey + "'");
          }
          
          //if isValue, continue on
          if (isValue) {
            continue;
          }
          
          //if term then get the term
          String term = propertiesConfigurationMap.get(propertiesKey);
          String termId = propertiesKey.substring(GrouperMessageTag.TERM_PREFIX.length());
          
          //strip off the .1, .2, etc if it exists
          if (termId.matches("^.*\\.[0-9]+$")) {
            int lastDot = termId.lastIndexOf('.');
            termId = termId.substring(0,lastDot);
          }
          
          String tooltipKey = GrouperMessageTag.TOOLTIP_PREFIX + termId;
          String tooltip = propertiesConfigurationMap.get(tooltipKey);
          
          //tooltipKeys.add(term);
          tooltip = convertTooltipTextToHtml(tooltip, term, false);

          //tooltipValues.add(tooltip);
          propertiesMap.put(term, tooltip);
        }
        
        propertiesMap = sortMapBySubstringFirst(propertiesMap);
        //convert back to lists
        this.tooltipKeys = new ArrayList<String>(propertiesMap.keySet());
        this.tooltipValues = new ArrayList<String>(propertiesMap.values());
        
        if (!this.useNewTermContext()) {
          this.pageContext.getRequest().setAttribute("tooltipKeys", this.tooltipKeys);
          this.pageContext.getRequest().setAttribute("tooltipValues", this.tooltipValues);
        }
      }
      
    }
    return this.tooltipKeys;
  }
  
  /**
   * convert tooltip text to html
   * @param tooltipText 
   * @param term
   * @param isIgnoreTooltipStyle if tooltip style should be ignored
   * @return the html tooltip text
   */
  private static String convertTooltipTextToHtml(String tooltipText, String term, 
      boolean isIgnoreTooltipStyle) {
    //substitute single quotes for javascript
    //tooltipText = tooltipText.replace("'", "\\'");
    //put it in the tooltip code
    //the class="tooltip" is substituted later, so if this is changed, change in other places as well
    String escapedTooltipText = StringUtils.replace(tooltipText, "'", "&#39;");
    escapedTooltipText = GrouperUiUtils.escapeHtml(escapedTooltipText, true, true);
    tooltipText = "<span " + (isIgnoreTooltipStyle ? "" : "class=\"grouperTooltip\" ") 
      + "onmouseover=\"grouperTooltip('" 
      + escapedTooltipText + "');\" onmouseout=\"UnTip()\">" + term + "</span>";
    return tooltipText;
  }
  
  /**
   * convert the properties configuration object to a map object (tree map)
   * @param resultMapOptional is null if create new, or if exists, overwrite
   * @param resourceBundle
   * @param throwExceptionIfProblem true if throw error if the problem with properties 
   * @return the map 
   */
  static Map<String, String> convertPropertiesToMap(Map<String, String> resultMapOptional, 
      ResourceBundle resourceBundle, boolean throwExceptionIfProblem) {
    if (resourceBundle == null) {
      return null;
    }
    if (resultMapOptional == null) {
      resultMapOptional = new HashMap<String,String>();
    }
    try {
      Enumeration<String> keysEnumeration = resourceBundle.getKeys();
      while (keysEnumeration.hasMoreElements()) {
        String key = keysEnumeration.nextElement();
        //the nav.properties has all sorts of stuff in there, only be concerned with tooltips and terms
        if (key.startsWith(TOOLTIP_PREFIX) || key.startsWith(TERM_PREFIX)) {
          resultMapOptional.put(key, resourceBundle.getString(key));
        }
      }
    } catch (Exception e) {
      //only propagate exceptions if in that mode
      if (throwExceptionIfProblem) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException)e;
        }
        throw new RuntimeException(e);
      }
    }
    return resultMapOptional;
  }
  /**
   * convert the properties configuration object to a map object (tree map)
   * @param inputMap 
   * @return the 
   */
  static Map<String, String> sortMapBySubstringFirst(Map<String, String> inputMap) {
    if (inputMap == null) {
      return null;
    }
    Map<String, String> resultMap = new TreeMap<String, String>(new Comparator<String>() {

      /**
       * comparator that puts substrings last
       * @param o1
       * @param o2
       * @return comparison
       */
      public int compare(String o1, String o2) {
        if (o1 == o2) {
          return 0;
        }
        if (o1 == null) {
          return -1;
        }
        if (o2 == null) {
          return 1;
        }
        if (o1.equals(o2)) {
          return 0;
        }
        if (o1.contains(o2)) {
          return -1;
        }
        if (o2.contains(o1)) {
          return 1;
        }
        return o1.compareTo(o2);
      }
      
    });
    resultMap.putAll(inputMap);
    return resultMap;
  }
  
  /**
   * substitute tooltips
   * @param message
   * @param isIgnoreTooltipStyle true if should ignore tooltip style
   * @return the substituted strings
   */
  @SuppressWarnings("unchecked")
  public String substituteTooltips(String message, boolean isIgnoreTooltipStyle) {
    
    //first step, get the map of substitutions
    List<String> theTooltipKeys = tooltipKeys();
    List<String> theTooltipValues = tooltipValues();
    
    //substitute, and remove if replaced
    String result = GrouperUtil.replace(message, theTooltipKeys, theTooltipValues, false, true);
    
    //maybe take out styles
    if (isIgnoreTooltipStyle) {
      result = StringUtils.replace(result, "class=\"grouperTooltip\" ", "");
    }
    
    //TODO cache this result if possible, check in properties file if contains vars
    return result;
    
  }

  /**
   * if we should not do tooltips for this tag
   * @return the tooltipDisable
   */
  public String getTooltipDisable() {
    return this.tooltipDisable;
  }

  /**
   * if we should not do tooltips for this tag
   * @param tooltipDisable1 the tooltipDisable to set
   */
  public void setTooltipDisable(String tooltipDisable1) {
    this.tooltipDisable = tooltipDisable1;
  }

  /**
   * terms only display once per page.  but if a term is in something hidden (e.g. infodot target),
   * then it might be used on page, but user cant see unless they open that infodot.  So this 
   * will reset the context so that in this text it will use all terms and they can be used later 
   * in page as well. 
   * @param useNewTermContext1 the useNewTermContext to set
   */
  public void setUseNewTermContext(String useNewTermContext1) {
    this.useNewTermContext = useNewTermContext1;
  }

  /**
   * @see org.apache.taglibs.standard.tag.el.fmt.MessageTag#doStartTag()
   */
  @Override
  public int doStartTag() throws JspException {
    
    boolean needsThreadLocalInit = GrouperUiFilter.retrieveHttpServletRequest() == null;
    if (needsThreadLocalInit) {
      GrouperRequestWrapper grouperRequestWrapper = new GrouperRequestWrapper((HttpServletRequest) this.pageContext.getRequest());
      GrouperUiFilter.initRequest(grouperRequestWrapper, this.pageContext.getResponse());
    }

    //set default bundle to "${nav}"
    String bundle = (String)GrouperUtil.fieldValue(this, "bundle_");
    if (StringUtils.isBlank(bundle)) {
      this.setBundle("${nav}");
    }
    return super.doStartTag();
  }

  
  /**
   * if we should escape single quotes with \' for javascript
   * @param escapeSingleQuotes1 the escapeSingleQuotes to set
   */
  public void setEscapeSingleQuotes(String escapeSingleQuotes1) {
    this.escapeSingleQuotes = escapeSingleQuotes1;
  }

  
  /**
   * for a tooltip on this message (similar to a targetted tooltip), this is name
   * from nav.properties
   * @param tooltipRef1 the tooltipRef to set
   */
  public void setTooltipRef(String tooltipRef1) {
    this.tooltipRef = tooltipRef1;
  }

  
  /**
   * @param ignoreTooltipStyle1 the ignoreTooltipStyle to set
   */
  public void setIgnoreTooltipStyle(String ignoreTooltipStyle1) {
    this.ignoreTooltipStyle = ignoreTooltipStyle1;
  }

  /**
   * if specified use this value and not lookup in resource file
   * @param value1
   */
  public void setValue(String value1) {
    this.value = value1;
  }

  /**
   * set value tooltip
   * @param valueTooltipKey1
   */
  public void setValueTooltipKey(String valueTooltipKey1) {
    this.valueTooltipKey = valueTooltipKey1;
  }
  
}
