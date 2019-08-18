package edu.internet2.middleware.grouper.grouperUi.beans.config;

import java.io.File;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadataType;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ConfigurationContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Configure;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * utils for config stuff
 * @author mchyzer
 *
 */
public class ConfigUtils {

  /**
   * 
   */
  protected static Log LOG = LogFactory.getLog(ConfigUtils.class);

  public ConfigUtils() {
  }

  /**
   * see if password based on various factors
   * @param configFileName if known or null if not
   * @param configItemMetadata if known or null if not
   * @param key or null if not known
   * @param value if there is one at this point or null if not
   * @param hasValue true if there is a value, false if not
   * @param userSelectedPassword true if the user selected that this is a password.   null if NA
   * @return true if password
   */
  public static boolean isPassword(ConfigFileName configFileName, ConfigItemMetadata configItemMetadata, String key, String value, boolean hasValue, Boolean userSelectedPassword) {
    return isPasswordHelper(configFileName, configItemMetadata, key, value, hasValue, userSelectedPassword);
  }

  /**
   * see if password based on various factors
   * @param configFileName
   * @param configItemMetadata
   * @param key
   * @param value
   * @param hasValue
   * @param userSelectedPassword
   * @return true if password
   */
  private static boolean isPasswordHelper(ConfigFileName configFileName, ConfigItemMetadata configItemMetadata, 
      String key, String value, boolean hasValue, Boolean userSelectedPassword) {

    // if there is a value, and it is a file, then its not a password
    if (hasValue && !StringUtils.isBlank(value)) {
      File theFile = new File(value);
      if (theFile.exists() && theFile.isFile()) {
        return false;
      }
    }
    
    // if the configured metadata is not null then check that
    if (isPasswordHelper(configItemMetadata)) {
      return true;
    }
    
    // look for a key with certain words inside
    if (key != null) {
      key = key.toLowerCase();

      if (key != null && (key.contains("pass") || key.contains("secret"))) {
        return true;
      }
    
      //lets try to find the config item metadata by key to be sure
      configItemMetadata = ConfigFileName.findConfigItemMetdata(key);
      if (isPasswordHelper(configItemMetadata)) {
        return true;
      }
    }

    // if the user selected that this is a password, then i guess it is
    if (userSelectedPassword != null && userSelectedPassword) {
      return true;
    }
    
    return false;
  }

  /**
   * see if password based on various factors
   * @param configItemMetadata
   * @return true if password
   */
  private static boolean isPasswordHelper(ConfigItemMetadata configItemMetadata) {
    
    if (configItemMetadata != null) {
      return configItemMetadata.isSensitive() || configItemMetadata.getValueType() == ConfigItemMetadataType.PASSWORD;
    }
    return false;
  }

  /**
   * return if the edit should continue
   * @return true if ok, false if should stop now
   */
  public static boolean validateConfigEdit(ConfigurationContainer configurationContainer, ConfigFileName configFileName, 
      String propertyNameString, String valueString, boolean isExpressionLanguage, StringBuilder message, GrouperConfigHibernate[] grouperConfigHibernateToReturn) {

    Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(null, null, propertyNameString);
    Set<GrouperConfigHibernate> grouperConfigHibernatesEl = GrouperDAOFactory.getFactory().getConfig().findAll(null, null, propertyNameString + ".elConfig");
    
    GrouperConfigHibernate grouperConfigHibernate = null;
    GrouperConfigHibernate grouperConfigHibernateEl = null;
    
    for (GrouperConfigHibernate current : GrouperUtil.nonNull(grouperConfigHibernates)) {
      if (configFileName.getConfigFileName().equals(current.getConfigFileNameDb()) && "INSTITUTION".equals(current.getConfigFileHierarchyDb())) {
        if (grouperConfigHibernate != null) {
          // why are there two???
          LOG.error("Why are there two configs in db with same key and config file???? " + current.getConfigFileNameDb() 
            + ", " + current.getConfigKey() + ", " + current.getConfigValue());
          current.delete();
        }
        grouperConfigHibernate = current;
      }
    }
    
    for (GrouperConfigHibernate current : GrouperUtil.nonNull(grouperConfigHibernatesEl)) {
      if (configFileName.getConfigFileName().equals(current.getConfigFileNameDb()) && "INSTITUTION".equals(current.getConfigFileHierarchyDb())) {
        if (grouperConfigHibernate != null) {
          // why are there two???
          LOG.error("Why are there two configs in db with same key and config file???? " + current.getConfigFileNameDb() 
            + ", " + current.getConfigKey() + ", " + current.getConfigValue());
          current.delete();
        }
        grouperConfigHibernateEl = current;
      }
    }
    
    //why would both be there?
    if (grouperConfigHibernate != null && grouperConfigHibernateEl != null) {
      
      if (isExpressionLanguage) {
        grouperConfigHibernate.delete();
        grouperConfigHibernate = null;
      } else {
        grouperConfigHibernateEl.delete();
        grouperConfigHibernateEl = null;
      }
      
    }
    
    // get down to one grouper config hibernate
    if (grouperConfigHibernate == null) {
      grouperConfigHibernate = grouperConfigHibernateEl;
      grouperConfigHibernateEl = null;
    }

    
    for (ConfigFileName current : ConfigFileName.values()) {
      
      if (current == configFileName) {
        continue;
      }
      
      ConfigPropertiesCascadeBase configPropertiesCascadeBase = current.getConfig();
      
      if (configPropertiesCascadeBase.containsKey(GrouperClientUtils.stripEnd(propertyNameString, ".elConfig"))) {
        
        configurationContainer.setCurrentConfigPropertyName(propertyNameString);
        configurationContainer.setCurrentConfigFileName(current.getConfigFileName());
        
        message.append(TextContainer.retrieveFromRequest().getText().get("configurationFilesPropertyExistsInAnother")).append("<br />");
        
      }
      
    }

    
    return true;
  }  

}
