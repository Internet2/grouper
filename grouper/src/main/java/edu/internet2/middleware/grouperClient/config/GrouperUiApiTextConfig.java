/**
 * @author mchyzer
 * $Id: GrouperUiTextConfig.java,v 1.2 2013/06/20 06:02:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.cfg.text.TextBundleBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;



/**
 * save the text bundle for the session.  Determine it based on 
 * the language and locale of the browser
 */
public class GrouperUiApiTextConfig extends ConfigPropertiesCascadeBase {

  public static void clearCache() {
    Map<String, ConfigPropertiesCascadeBase> theConfigFileCache = configFileCache;
    if (theConfigFileCache != null) {
      theConfigFileCache.clear();
    }
  }
  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    
    //some code to print out accented chars, just put institutionName to have accented char
    //make a grouper.text.fr.fr.base.properties and grouper.text.fr.fr.properties
    //config new locale in grouper-ui.properties
    // grouper.text.bundle.1.language = fr
    // grouper.text.bundle.1.country = fr
    // grouper.text.bundle.1.fileNamePrefix = grouperText/grouper.text.fr.fr
    
    Locale locale = new Locale("fr", "FR");
    GrouperUiApiTextConfig grouperUiTextConfig = GrouperUiApiTextConfig.retrieveText(locale); 
    System.out.println("1: " + grouperUiTextConfig.propertyValueString("institutionName"));
    
    Properties properties = GrouperUtil.propertiesFromResourceName("grouperText/grouper.text.fr.fr.properties"); 
    
    System.out.println("2: " + properties.get("institutionName"));
    
    URL url = ConfigPropertiesCascadeUtils.computeUrl("grouperText/grouper.text.fr.fr.properties", true);

    InputStream inputStream = url.openStream();

    String contents = ConfigPropertiesCascadeUtils.toString(inputStream, "UTF-8");
    
    System.out.println("3: " + contents);
    
    File file = new File("C:\\Users\\mchyzer\\Documents\\GitHub\\grouper_v2_2\\grouper-ui\\conf\\grouperText\\grouper.text.fr.fr.properties");

    contents = GrouperUtil.readFileIntoString(file);
    
    System.out.println("4: " + contents);
    
    System.out.println("5: " + new String(contents.getBytes("UTF-8"), "ISO-8859-1"));

    InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(file), "UTF-8");
    
    StringWriter writer = new StringWriter();
    IOUtils.copy(inputStreamReader, writer);
    contents = writer.toString();
  
    System.out.println("6: " + contents);
    
    
  }
  
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

  /** tooltip keys */
  private List<String> tooltipKeys = null;

  /** tooltip values in order of keys */
  private List<String> tooltipValues = null;

  /**
   * keep servlet request in threadlocal during request from filter
   */
  private static ThreadLocal<ServletRequest> servletRequestThreadLocal = new InheritableThreadLocal<ServletRequest>();

  /** in the text file, terms must start with this prefix.  multiple terms can exist for one tooltip */
  public static final String TERM_PREFIX = "term.";

  /** in the text file, tooltips must start with this prefix.  there should be one and only one tooltip for a term.
   * tooltips and terms are linked by the common name, which is the suffix of the text file key.
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

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperUiApiTextConfig.class);
  
  /**
   * constructor with the correct files
   * @param theMainConfigClasspath 
   * @param theMainExampleConfigClasspath 
   * @param theLanguageCountry e.g. en_us
   */
  private GrouperUiApiTextConfig(String theMainConfigClasspath, String theMainExampleConfigClasspath,  String theLanguageCountry) {
    this.mainConfigClasspath = theMainConfigClasspath;
    this.mainExampleConfigClasspath = theMainExampleConfigClasspath;
    this.languageCountry = theLanguageCountry;
  }
  
  /**
   * 
   */
  public GrouperUiApiTextConfig() {
    
  }

  /**
   * text config for this user's locale
   * @return the config for this user's locale
   */
  public static GrouperUiApiTextConfig retrieveTextConfigEnUs() {
    
    return retrieveText(Locale.US);
    
  }

  /**
   * text config for this user's locale
   * @return the config for this user's locale
   */
  public static GrouperUiApiTextConfig retrieveTextConfigFrFr() {
    
    return retrieveText(Locale.FRANCE);
    
  }

  /**
   * text config for this user's locale
   * @return the config for this user's locale
   */
  public static GrouperUiApiTextConfig retrieveTextConfig() {
    
    HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequestThreadLocal.get(); 
    
    //cache this in the request
    GrouperUiApiTextConfig grouperUiTextConfig = httpServletRequest == null ? null : (GrouperUiApiTextConfig)httpServletRequest.getAttribute("grouperUiTextConfig");
    
    if (grouperUiTextConfig == null) {
      Object synchronizedOnThis = httpServletRequest == null ? GrouperUiApiTextConfig.class : httpServletRequest;
      synchronized(synchronizedOnThis) {
        
        grouperUiTextConfig = httpServletRequest == null ? null : (GrouperUiApiTextConfig)httpServletRequest.getAttribute("grouperUiTextConfig");
        
        if (grouperUiTextConfig == null) {
          
          Locale locale = httpServletRequest == null ? null : httpServletRequest.getLocale();
          
          grouperUiTextConfig = retrieveText(locale);
          
          if (httpServletRequest != null) {
            httpServletRequest.setAttribute("grouperUiTextConfig", grouperUiTextConfig);
          }
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
  public static GrouperUiApiTextConfig retrieveText(Locale locale) {

    TextBundleBean textBundleBean = retrieveTextBundleBean(locale);
    
    final String theMainConfigClasspath = StringUtils.replace(textBundleBean.getFileNamePrefix(), "textNg.", "text.") + ".properties";
    
    // replace to textNg to accomodate moving config to API
    final String theMainExampleConfigClasspath = StringUtils.replace(textBundleBean.getFileNamePrefix(), "text.", "textNg.") + ".base.properties";
    
    GrouperUiApiTextConfig grouperTextConfig = new GrouperUiApiTextConfig(theMainConfigClasspath,
          theMainExampleConfigClasspath, textBundleBean.getLanguage() + "_" + textBundleBean.getCountry());
    grouperTextConfig = (GrouperUiApiTextConfig)grouperTextConfig.retrieveFromConfigFileOrCache();

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
      return GrouperConfig.retrieveConfig().textBundleDefault();

    }
    
    String language = StringUtils.defaultString(locale.getLanguage()).toLowerCase();
    String country = StringUtils.defaultString(locale.getCountry()).toLowerCase();
    
    //see if there is a match by language and country
    TextBundleBean textBundleBean = GrouperConfig.retrieveConfig()
      .textBundleFromLanguageAndCountry().get(language + "_" + country);
    
    if (textBundleBean != null) {
      return textBundleBean;
    }
    
    //see if there is a match by language
    textBundleBean = GrouperConfig.retrieveConfig()
      .textBundleFromLanguage().get(language);
  
    if (textBundleBean != null) {
      return textBundleBean;
    }
    
    //see if there is a match by country
    textBundleBean = GrouperConfig.retrieveConfig()
      .textBundleFromCountry().get(country);
  
    if (textBundleBean != null) {
      return textBundleBean;
    }
    
    //do the default
    return GrouperConfig.retrieveConfig().textBundleDefault();

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
  
  @Override
  protected ConfigPropertiesCascadeBase retrieveFromConfigFiles() {
    ConfigPropertiesCascadeBase configObject = super.retrieveFromConfigFiles();
    ((GrouperUiApiTextConfig)configObject).languageCountry = this.languageCountry;
    ((GrouperUiApiTextConfig)configObject).mainConfigClasspath = this.mainConfigClasspath;
    ((GrouperUiApiTextConfig)configObject).mainExampleConfigClasspath = this.mainExampleConfigClasspath;
    return configObject;
  }
  /**
   * init and return the tooltip keys
   * @return the list of keys
   */
  public List<String> tooltipKeys() {

    if (this.tooltipKeys == null) {

      //add properties to map
      Properties propertiesFromConfig = properties();

      Set<Object> propertiesKeys = propertiesFromConfig.keySet();
      Map<String, String> propertiesMap = null;

      //loop through the tooltips
      if (propertiesKeys != null) {
        propertiesMap = new HashMap<String, String>();
        for (Object propertiesKeyObject : propertiesKeys) {
          
          String propertiesKey = (String)propertiesKeyObject;
          
          //see if a key
          boolean isTerm = propertiesKey.startsWith(TERM_PREFIX);
                    
          //if isValue, continue on
          if (!isTerm) {
            continue;
          }
          
          //if term then get the term
          String term = propertiesFromConfig.getProperty(propertiesKey);
          String termId = propertiesKey.substring(TERM_PREFIX.length());
          
          //strip off the .1, .2, etc if it exists
          if (termId.matches("^.*\\.[0-9]+$")) {
            int lastDot = termId.lastIndexOf('.');
            termId = termId.substring(0,lastDot);
          }
          
          String tooltipKey = "tooltip." + termId;
          String tooltip = propertiesFromConfig.getProperty(tooltipKey);
          
          //tooltipKeys.add(term);
          tooltip = GrouperTextContainer.convertTooltipTextToHtml(tooltip, term, false);

          //tooltipValues.add(tooltip);
          propertiesMap.put(term, tooltip);
        }
        
        propertiesMap = sortMapBySubstringFirst(propertiesMap);
        //convert back to lists
        this.tooltipKeys = new ArrayList<String>(propertiesMap.keySet());
        this.tooltipValues = new ArrayList<String>(propertiesMap.values());
        
      }
      
    }
    return this.tooltipKeys;
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
   * init and return the tooltip keys
   * @return the list of keys
   */
  public List<String> tooltipValues() {
    //init everything
    tooltipKeys();
    return this.tooltipValues;
  }

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

  
}
