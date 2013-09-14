package edu.internet2.middleware.grouper.ui.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.ui.text.TextBundleBean;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;

/**
 * hierarchical config class for grouper-ui.properties
 * @author mchyzer
 *
 */
public class GrouperUiConfig extends ConfigPropertiesCascadeBase {

  /**
   * use the factory
   */
  private GrouperUiConfig() {
    
  }

  /**
   * retrieve a config from the config file or from cache
   * @return the config object
   */
  public static GrouperUiConfig retrieveConfig() {
    return retrieveConfig(GrouperUiConfig.class);
  }

  /**
   * @see ConfigPropertiesCascadeBase#clearCachedCalculatedValues()
   */
  @Override
  public void clearCachedCalculatedValues() {
    
  }

  /**
   * @see ConfigPropertiesCascadeBase#getHierarchyConfigKey
   */
  @Override
  protected String getHierarchyConfigKey() {
    return "grouperUi.config.hierarchy";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getMainConfigClasspath
   */
  @Override
  protected String getMainConfigClasspath() {
    return "grouper-ui.properties";
  }
  
  /**
   * @see ConfigPropertiesCascadeBase#getMainExampleConfigClasspath
   */
  @Override
  protected String getMainExampleConfigClasspath() {
    return "grouper-ui.base.properties";
  }

  /**
   * @see ConfigPropertiesCascadeBase#getSecondsToCheckConfigKey
   */
  @Override
  protected String getSecondsToCheckConfigKey() {
    return "grouperUi.config.secondsBetweenUpdateChecks";
  }

  
  /**
   * default bundle
   */
  private TextBundleBean textBundleDefault = null;
  
  
  
  
  /**
   * default bundle
   * @return the textBundleDefault
   */
  public TextBundleBean textBundleDefault() {
    if (this.textBundleDefault == null) {
      this.textBundleFromCountry();
    }
    return this.textBundleDefault;
  }

  /**
   * country to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromCountry  = null;
  
  /**
   * language_country to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromLanguageAndCountry  = null;
  
  /**
   * language to text bundle
   */
  private Map<String, TextBundleBean> textBundleFromLanguage  = null;

  /** logger */
  protected static final Log LOG = LogFactory.getLog(GrouperUiConfig.class);
  
  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromLanguage() {
    //init
    Map<String, TextBundleBean> theTextBundleFromLanguage = this.textBundleFromLanguage;
    if (theTextBundleFromLanguage == null) {
      //init here
      this.textBundleFromCountry();
      theTextBundleFromLanguage = this.textBundleFromLanguage;
    }
    if (theTextBundleFromLanguage == null) {
      throw new RuntimeException("Why is textBundleFromLanguage map null????");
    }
    return theTextBundleFromLanguage;
  }

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromLanguageAndCountry() {
    //init
    Map<String, TextBundleBean> theTextBundleFromLanguageAndCountry = this.textBundleFromLanguageAndCountry;
    if (theTextBundleFromLanguageAndCountry == null) {
      //init here
      this.textBundleFromCountry();
      theTextBundleFromLanguageAndCountry = this.textBundleFromLanguageAndCountry;
    }
    if (theTextBundleFromLanguageAndCountry == null) {
      throw new RuntimeException("Why is textBundleFromLanguage map null????");
    }
    return theTextBundleFromLanguageAndCountry;
  }

  /**
   * country to text bundle
   * @return the map
   */
  public Map<String, TextBundleBean> textBundleFromCountry() {
    if (this.textBundleFromCountry == null) {
      
      synchronized (this) {
        
        if (this.textBundleFromCountry == null) {
          
          Map<String, TextBundleBean> tempBundleFromCountry = new HashMap<String, TextBundleBean>();
          Map<String, TextBundleBean> tempBundleFromLanguage = new HashMap<String, TextBundleBean>();
          Map<String, TextBundleBean> tempBundleFromLanguageAndCountry = new HashMap<String, TextBundleBean>();
          
          Pattern pattern = Pattern.compile("^grouper\\.text\\.bundle\\.(.*)\\.fileNamePrefix$");
          
          boolean foundDefault = false;
          
          for (Object keyObject : this.properties().keySet()) {
            String key = (String)keyObject;
            Matcher matcher = pattern.matcher(key);
            if (matcher.matches()) {
              
              String bundleKey = matcher.group(1);

              String fileNamePrefix = this.propertyValueString(key);
              String language = StringUtils.defaultString(this.propertyValueString("grouper.text.bundle." + bundleKey + ".language")).toLowerCase();
              String country = StringUtils.defaultString(this.propertyValueString("grouper.text.bundle." + bundleKey + ".country")).toLowerCase();
              
              TextBundleBean textBundleBean = new TextBundleBean();
              
              textBundleBean.setCountry(country);
              textBundleBean.setLanguage(language);
              textBundleBean.setFileNamePrefix(fileNamePrefix);

              if (StringUtils.equals(bundleKey, propertyValueStringRequired("grouper.text.defaultBundleIndex"))) {
                foundDefault = true;
                this.textBundleDefault = textBundleBean;
              }
              
              //first in wins
              if (!tempBundleFromCountry.containsKey(country)) {
                tempBundleFromCountry.put(country, textBundleBean);
              }
              if (!tempBundleFromLanguage.containsKey(language)) {
                tempBundleFromLanguage.put(language, textBundleBean);
              }
              String languageAndCountry = language + "_" + country;
              if (tempBundleFromLanguageAndCountry.containsKey(languageAndCountry)) {
                LOG.error("Language and country already defined! " + languageAndCountry);
              }
              tempBundleFromLanguageAndCountry.put(languageAndCountry, textBundleBean);
            }
          }
          
          if (!foundDefault) {
            throw new RuntimeException("Cant find default bundle index: '" 
                + propertyValueStringRequired("grouper.text.defaultBundleIndex") + "', should have a key: grouper.text.bundle."
                + propertyValueStringRequired("grouper.text.defaultBundleIndex") + ".fileNamePrefix");
          }
          
          this.textBundleFromCountry = Collections.unmodifiableMap(tempBundleFromCountry);
          this.textBundleFromLanguage = Collections.unmodifiableMap(tempBundleFromLanguage);
          this.textBundleFromLanguageAndCountry = Collections.unmodifiableMap(tempBundleFromLanguageAndCountry);
          
        }
      }
    }
    return this.textBundleFromCountry;
  }
  

}
