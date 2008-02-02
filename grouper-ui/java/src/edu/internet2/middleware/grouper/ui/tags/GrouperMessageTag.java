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
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.util.RequestUtils;
import org.apache.taglibs.standard.tag.common.fmt.BundleSupport;
import org.apache.taglibs.standard.tag.common.fmt.SetLocaleSupport;
import org.apache.taglibs.standard.tag.el.fmt.MessageTag;

import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.PropertiesConfiguration;
import edu.internet2.middleware.grouper.ui.util.GrouperUtils;

/**
 * <p>
 * A handler for &lt;message&gt; that accepts attributes as Strings and
 * evaluates them as expressions at runtime. Substitutes keywords into
 * underlined tooltips
 * </p>
 * 
 * @author Chris Hyzer
 */
public class GrouperMessageTag extends MessageTag {

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

	/**
	 * init vars
	 */
	private void init() {
		this.tooltipDisable = false;
		this.tooltipKeys = null;
		this.tooltipValues = null;
		this.useNewTermContext = false;
	}

	/** 
	 * terms only display once per page.  but if a term is in something hidden (e.g. infodot target),
	 * then it might be used on page, but user cant see unless they open that infodot.  So this 
	 * will reset the context so that in this text it will use all terms and they can be used later 
	 * in page as well. 
	 */
	private boolean useNewTermContext = false;
	
	/** if we should not do tooltips for this tag */
	public boolean tooltipDisable = false;
	
	/**
	 * cache the properties file
	 */
	private static Map<String,PropertiesConfiguration> tooltipConfigs = 
		new HashMap<String, PropertiesConfiguration>();

	/**
	 * lazy load the properties config
	 * 
	 * @return the properties config
	 */
	private static PropertiesConfiguration tooltipConfig(String path) {
		PropertiesConfiguration propertiesConfiguration = tooltipConfigs.get(path);
		if (propertiesConfiguration == null) {
			propertiesConfiguration = new PropertiesConfiguration(path);
			tooltipConfigs.put(path, propertiesConfiguration);
		}
		return propertiesConfiguration;
	}
	
	/**
	 * this is overridden to put the tooltips in the text
	 */
	@SuppressWarnings("unchecked")
	public int doEndTag() throws JspException {

		// see if we are using tooltips
		if (this.tooltipDisable || !GrouperConfig.getPropertyBoolean(
				GrouperConfig.MESSAGES_USE_TOOLTIPS, true)) {
			return super.doEndTag();
		}

		// now duplicate the super method
		String keyInput = null;

		// determine the message key by...
		if (keySpecified) {
			// ... reading 'key' attribute
			keyInput = key;
		} else {
			// ... retrieving and trimming our body
			if (bodyContent != null && bodyContent.getString() != null)
				keyInput = bodyContent.getString().trim();
		}

		if ((keyInput == null) || keyInput.equals("")) {
			try {
				pageContext.getOut().print("??????");
			} catch (IOException ioe) {
				throw new JspTagException(ioe.getMessage());
			}
			return EVAL_PAGE;
		}

		String prefix = null;
		if (locCtxt == null) {
			Tag t = findAncestorWithClass(this, BundleSupport.class);
			if (t != null) {
				// use resource bundle from parent <bundle> tag
				BundleSupport parent = (BundleSupport) t;
				locCtxt = parent.getLocalizationContext();
				prefix = parent.getPrefix();
			} else {
				locCtxt = BundleSupport.getLocalizationContext(pageContext);
			}
		} else {
			// localization context taken from 'bundle' attribute
			if (locCtxt.getLocale() != null) {
				GrouperUtils.callMethod(SetLocaleSupport.class,
						"setResponseLocale", new Class[] { PageContext.class,
								Locale.class }, new Object[] {
								this.pageContext, locCtxt.getLocale() });
				/*
				 * SetLocaleSupport.setResponseLocale(pageContext,
				 * locCtxt.getLocale());
				 */
			}
		}

		String message = UNDEFINED_KEY + keyInput + UNDEFINED_KEY;
		if (locCtxt != null) {
			ResourceBundle bundle = locCtxt.getResourceBundle();
			if (bundle != null) {
				try {
					// prepend 'prefix' attribute from parent bundle
					if (prefix != null)
						keyInput = prefix + keyInput;
					message = bundle.getString(keyInput);
					// Perform parametric replacement if required
					List params = (List) GrouperUtils
							.fieldValue(this, "params");
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
		String var = (String) GrouperUtils.fieldValue(this, "var");

		//CH 20080129 at this point we need to make the tooltip subsitutions
		message = substituteTooltips(message);
		
		if (var != null) {
			int scope = (Integer) GrouperUtils.fieldValue(this, "scope");
			pageContext.setAttribute(var, message, scope);
		} else {
			try {
				pageContext.getOut().print(message);
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
		
		if (this.tooltipKeys == null || this.useNewTermContext) {
			
			//add the tooltips:
			PropertiesConfiguration propertiesConfiguration = tooltipConfig("/resources/grouper/tooltips.properties");
			PropertiesConfiguration propertiesLocalConfiguration = tooltipConfig("/resources/grouper/local.tooltips.properties");
			Locale locale = RequestUtils.getUserLocale((HttpServletRequest)this.pageContext.getRequest(), null);
			String localeString = locale == null ? "" : locale.toString();
			PropertiesConfiguration propertiesLocaleConfiguration = StringUtils.isBlank(localeString) ? null 
					: tooltipConfig("/resources/grouper/tooltips_" + localeString + ".properties");
			
			//add properties to map
			Map<String, String> propertiesConfigurationMap = convertPropertiesToMap(null, propertiesConfiguration, true);
			//add in local ones without failing on error
			convertPropertiesToMap(propertiesConfigurationMap, propertiesLocalConfiguration, false);
			//add in locale
			//TODO consider the grouper.properties default.module ? default.locale
			convertPropertiesToMap(propertiesConfigurationMap, propertiesLocaleConfiguration, false);
			Set<String> propertiesKeys = propertiesConfigurationMap == null ? null : 
				propertiesConfigurationMap.keySet();
			Map<String, String> propertiesMap = null;

			//loop through the tooltips
			if (propertiesKeys != null) {
				propertiesMap = new HashMap<String, String>();
				for (String propertiesKey : propertiesKeys) {
					
					//see if a key
					boolean isTerm = propertiesKey.startsWith("term.");
					boolean isValue = !isTerm && propertiesKey.startsWith("tooltip.");
					
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
					String termId = propertiesKey.substring("term.".length());
					
					//strip off the .1, .2, etc if it exists
					if (termId.matches("^.*\\.[0-9]+$")) {
						int lastDot = termId.lastIndexOf('.');
						termId = termId.substring(0,lastDot);
					}
					
					String tooltipKey = "tooltip." + termId;
					String tooltip = propertiesConfigurationMap.get(tooltipKey);
					
					//tooltipKeys.add(term);

					//substitute single quotes for javascript
					tooltip = tooltip.replace("'", "\\'");
					//put it in the tooltip code
					tooltip = "<span class=\"tooltip\" onmouseover=\"grouperTooltip('" 
						+ tooltip + "');\">" + term + "</span>";
					//tooltipValues.add(tooltip);
					propertiesMap.put(term, tooltip);
				}
				
				propertiesMap = sortMapBySubstringFirst(propertiesMap);
				//convert back to lists
				this.tooltipKeys = new ArrayList<String>(propertiesMap.keySet());
				this.tooltipValues = new ArrayList<String>(propertiesMap.values());
				
				if (!useNewTermContext) {
					this.pageContext.getRequest().setAttribute("tooltipKeys", this.tooltipKeys);
					this.pageContext.getRequest().setAttribute("tooltipValues", this.tooltipValues);
				}
			}
			
		}
		return this.tooltipKeys;
	}
	
	/**
	 * convert the properties configuration object to a map object (tree map)
	 * @param resultMapOptional is null if create new, or if exists, overwrite
	 * @param propertiesConfiguration
	 * @param throwExceptionIfProblem true if throw error if the problem with properties 
	 * @return the map 
	 */
	static Map<String, String> convertPropertiesToMap(Map<String, String> resultMapOptional, 
			PropertiesConfiguration propertiesConfiguration, boolean throwExceptionIfProblem) {
		if (propertiesConfiguration == null) {
			return null;
		}
		if (resultMapOptional == null) {
			resultMapOptional = new HashMap<String,String>();
		}
		try {
			for (String key : propertiesConfiguration.keySet()) {
				resultMapOptional.put(key, propertiesConfiguration.getProperty(key));
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
	 * @param propertiesConfiguration
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
			 * @return
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
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public String substituteTooltips(String message) {
		
		//first step, get the map of substitutions
		List<String> tooltipKeys = tooltipKeys();
		List<String> tooltipValues = tooltipValues();
		
		//substitute, and remove if replaced
		String result = GrouperUtils.replace(message, tooltipKeys, tooltipValues, false, true);
		
		//TODO cache this result if possible, check in properties file if contains vars
		return result;
		
	}

	/**
	 * if we should not do tooltips for this tag
	 * @return the tooltipDisable
	 */
	public boolean isTooltipDisable() {
		return tooltipDisable;
	}

	/**
	 * if we should not do tooltips for this tag
	 * @param tooltipDisable the tooltipDisable to set
	 */
	public void setTooltipDisable(boolean tooltipDisable) {
		this.tooltipDisable = tooltipDisable;
	}

	/**
	 * terms only display once per page.  but if a term is in something hidden (e.g. infodot target),
	 * then it might be used on page, but user cant see unless they open that infodot.  So this 
	 * will reset the context so that in this text it will use all terms and they can be used later 
	 * in page as well. 
	 * @param useNewTermContext the useNewTermContext to set
	 */
	public void setUseNewTermContext(boolean useNewTermContext) {
		this.useNewTermContext = useNewTermContext;
	}
	
}
