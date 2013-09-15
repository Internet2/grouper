/**
 * @author mchyzer
 * $Id: GrouperUiTextConfig.java,v 1.2 2013/06/20 06:02:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.config;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.text.TextBundleBean;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * save the text bundle for the session.  Determine it based on 
 * the language and locale of the browser
 */
public class GrouperUiTextConfig extends ConfigPropertiesCascadeBase {

  /**
   * the name of the file on classpath, e.g. grouperText/grouper.text.en.us.properties
   */
  private String mainConfigClasspath;
  
  /**
   * e.g. en_us
   */
  private String languageCountry;
  
  /**
   * the name of the file on classpath, e.g. grouperText/grouper.text.en.us.base.properties
   */
  private String mainExampleConfigClasspath;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUiTextConfig.class);
  
  /**
   * constructor with the correct files
   * @param theMainConfigClasspath 
   * @param theMainExampleConfigClasspath 
   * @param theLanguageCountry e.g. en_us
   */
  private GrouperUiTextConfig(String theMainConfigClasspath, String theMainExampleConfigClasspath,  String theLanguageCountry) {
    this.mainConfigClasspath = theMainConfigClasspath;
    this.mainExampleConfigClasspath = theMainExampleConfigClasspath;
    this.languageCountry = theLanguageCountry;
  }
  
  /**
   * 
   */
  public GrouperUiTextConfig() {
    
  }
  
  /**
   * text config for this user's locale
   * @return the config for this user's locale
   */
  public static GrouperUiTextConfig retrieveTextConfig() {
    
    HttpServletRequest httpServletRequest = GrouperUiFilter.retrieveHttpServletRequest();
    
    //cache this in the request
    GrouperUiTextConfig grouperUiTextConfig = (GrouperUiTextConfig)httpServletRequest.getAttribute("grouperUiTextConfig");
    
    if (grouperUiTextConfig == null) {
      
      synchronized(httpServletRequest) {
        
        if (grouperUiTextConfig == null) {
          
          Locale locale = httpServletRequest.getLocale();
          
          grouperUiTextConfig = retrieveText(locale);
          
          httpServletRequest.setAttribute("grouperTextConfig", grouperUiTextConfig);
        
        }        
      }
      
    }
    
    return grouperUiTextConfig;
    
  }
  
  /**
   * text config for a certain locale
   * @param locale
   * @return the config for this user's locale
   */
  public static GrouperUiTextConfig retrieveText(Locale locale) {

    TextBundleBean textBundleBean = retrieveTextBundleBean(locale);
    
    GrouperUiTextConfig grouperTextConfig = new GrouperUiTextConfig(textBundleBean.getFileNamePrefix() + ".properties",
          textBundleBean.getFileNamePrefix() + ".base.properties", textBundleBean.getLanguage() + "_" + textBundleBean.getCountry());
    grouperTextConfig = (GrouperUiTextConfig)grouperTextConfig.retrieveFromConfigFileOrCache();

    return grouperTextConfig;
    
  }
  
  /**
   * text config for a certain locale
   * @param locale
   * @return the config for this user's locale
   */
  private static TextBundleBean retrieveTextBundleBean(Locale locale) {
    
    if (locale == null) {
      //return the default
      return GrouperUiConfig.retrieveConfig().textBundleDefault();

    }
    
    String language = StringUtils.defaultString(locale.getLanguage()).toLowerCase();
    String country = StringUtils.defaultString(locale.getCountry()).toLowerCase();
    
    //see if there is a match by language and country
    TextBundleBean textBundleBean = GrouperUiConfig.retrieveConfig()
      .textBundleFromLanguageAndCountry().get(language + "_" + country);
    
    if (textBundleBean != null) {
      return textBundleBean;
    }
    
    //see if there is a match by language
    textBundleBean = GrouperUiConfig.retrieveConfig()
      .textBundleFromLanguage().get(language);
  
    if (textBundleBean != null) {
      return textBundleBean;
    }
    
    //see if there is a match by country
    textBundleBean = GrouperUiConfig.retrieveConfig()
      .textBundleFromCountry().get(country);
  
    if (textBundleBean != null) {
      return textBundleBean;
    }
    
    //do the default
    return GrouperUiConfig.retrieveConfig().textBundleDefault();

  }
  
  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getHierarchyConfigKey()
   */
  @Override
  protected String getHierarchyConfigKey() {
    //"grouperServer.config.hierarchy"
    return "text.config.hierarchy";
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getMainConfigClasspath()
   */
  @Override
  protected String getMainConfigClasspath() {
    //"grouper.server.properties"
    return this.mainConfigClasspath;
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getMainExampleConfigClasspath()
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    //"grouper.server.base.properties"
    return this.mainExampleConfigClasspath;
  }

  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey()
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    //"grouperServer.config.secondsBetweenUpdateChecks"
    return "text.config.secondsBetweenUpdateChecks";
  }
  
  /**
   * config file cache
   */
  private static Map<String, ConfigPropertiesCascadeBase> configFileCache = null;

  /**
   * pattern to find where the variables are in the textm, e.g. $$something$$
   */
  private static Pattern substitutePattern = Pattern.compile("\\$\\$([^\\s\\$]+?)\\$\\$");
  
  
  /**
   * @see edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase#retrieveFromConfigFiles()
   */
  @Override
  protected ConfigPropertiesCascadeBase retrieveFromConfigFiles() {
    
    ConfigPropertiesCascadeBase configPropertiesCascadeBase = super.retrieveFromConfigFiles();

    Properties properties = configPropertiesCascadeBase.internalProperties();
    
    Set<String> propertyNamesToCheck = new LinkedHashSet<String>();
    
    for (Object propertyName : properties.keySet()) {
      propertyNamesToCheck.add((String)propertyName);
    }

    Set<String> nextPropertyNamesToCheck = new LinkedHashSet<String>();
    
    //lets resolve variables
    for (int i=0;i<20;i++) {
      
      boolean foundVariable = false;
      
      for (Object propertyNameObject : properties.keySet()) {
        
        String propertyName = (String)propertyNameObject;
        
        String value = properties.getProperty(propertyName);
        String newValue = substituteVariables(properties, value);
        
        //next run, dont do the ones that dont change...
        if (!StringUtils.equals(value, newValue)) {
          nextPropertyNamesToCheck.add(value);
          foundVariable = true;
          properties.put(propertyName, newValue);
        }
      }
      
      if (!foundVariable) {
        break;
      }

      //keep track of ones to check
      propertyNamesToCheck = nextPropertyNamesToCheck;
      nextPropertyNamesToCheck = new LinkedHashSet<String>();
      
    }
    return configPropertiesCascadeBase;
  }

  /**
   * 
   * @param properties to get data from
   * @param value 
   * @return the subsituted string
   */
  protected String substituteVariables(Properties properties, String value) {

    Matcher matcher = substitutePattern.matcher(value);
    
    StringBuilder result = new StringBuilder();
    
    int index = 0;
    
    //loop through and find each script
    while(matcher.find()) {
      result.append(value.substring(index, matcher.start()));
      
      //here is the script inside the dollars
      String variable = matcher.group(1);
      
      index = matcher.end();

      String variableText = properties.getProperty(variable);
      
      if (StringUtils.isBlank(variableText)) {
        LOG.error("Cant find text for variable: '" + variable + "'");
        variableText = "$$not found: " + variable + "$$";
      }
      
      result.append(variableText);
    }
    
    result.append(value.substring(index, value.length()));
    return result.toString();
    
  }

  /**
   * see if there is one in cache, if so, use it, if not, get from config files
   * @return the config from file or cache
   */
  @Override
  protected ConfigPropertiesCascadeBase retrieveFromConfigFileOrCache() {
    
    if (configFileCache == null) {
      configFileCache = 
        new HashMap<String, ConfigPropertiesCascadeBase>();
    }
    ConfigPropertiesCascadeBase configObject = configFileCache.get(this.languageCountry);
    
    if (configObject == null) {
      
      if (LOG != null && LOG.isDebugEnabled()) {
        LOG.debug("Config file has not be created yet, will create now: " + this.getMainConfigClasspath());
      }
      
      configObject = retrieveFromConfigFiles();
      configFileCache.put(this.languageCountry, configObject);
      
    } else {
      
      //see if that much time has passed
      if (configObject.needToCheckIfFilesNeedReloading()) {
        
        synchronized (configObject) {
          
          configObject = configFileCache.get(this.languageCountry);
          
          //check again in case another thread did it
          if (configObject.needToCheckIfFilesNeedReloading()) {
            
            if (configObject.filesNeedReloadingBasedOnContents()) {
              configObject = retrieveFromConfigFiles();
              configFileCache.put(this.languageCountry, configObject);
            }
          }
        }
      }
    }
    
    return configObject;
  }
}
