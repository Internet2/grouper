package edu.internet2.middleware.grouper.cfg.dbConfig;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.morphString.Morph;

public class DbConfigEngine {

  public static final String ESCAPED_PASSWORD = "*******";

  /**
   * 
   * @param configFileString
   * @param propertyNameString
   * @param expressionLanguageString
   * @param valueString
   * @param userSelectedPassword
   * @param message
   * @param added (first index) will be true if added, false if updated, and null if no change
   * @param error true if fatal error, false if warning, null if no error
   * @param fromUi true if from UI false if not from UI
   * @param comment notes about settings
   * @param clearCache should always be true unless you are doing multiple things at once, then false
   * @return true if ok, false if not
   */
  public static boolean configurationFileAddEditHelper2(String configFileString, String propertyNameString,
      String expressionLanguageString, String valueString, Boolean userSelectedPassword,
      StringBuilder message, Boolean[] added, Boolean[] error, boolean fromUi, String comment, 
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay, final boolean clearCache) {
    
    try {
      
      Map<String, Object> textReplaceMap = new HashMap<String, Object>();
      GrouperTextContainer.assignThreadLocalVariableMap(textReplaceMap);

      ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);

      
      // if not sent, thats a problem
      if (StringUtils.isBlank(configFileString)) {
        final String errorMessage = GrouperTextContainer.retrieveFromRequest().getText().get("configurationFileRequired");
        if (fromUi) {
          validationErrorsToDisplay.put("#configFileSelect", errorMessage);
        } else {
          System.out.println(errorMessage);
        }
        return false;
      }
      textReplaceMap.put("currentConfigFileName", configFileName.getConfigFileName());
      
      if (StringUtils.isBlank(propertyNameString)) {
        final String errorMessage = GrouperTextContainer.retrieveFromRequest().getText().get("configurationFilesAddEntryPropertyNameRequired");
        if (fromUi) {
          validationErrorsToDisplay.put("#propertyNameId", errorMessage);
        } else {
          System.out.println(errorMessage);
        }
        return false;
      }
      
      if (StringUtils.isBlank(expressionLanguageString)) {
        throw new RuntimeException("Expression language should never be null!");
      }
      boolean isExpressionLanguage = GrouperUtil.booleanValue(expressionLanguageString);
  
      if (propertyNameString.endsWith(".elConfig") && !isExpressionLanguage) {
        
        final String errorMessage = GrouperTextContainer.retrieveFromRequest().getText().get("configurationFilesAddEntryPropertyNameElConfig");
        if (fromUi) {
          validationErrorsToDisplay.put("#propertyNameId", errorMessage);
        } else {
          System.out.println(errorMessage);
        }
        return false;
      }
      
      valueString = valueString == null ? null : StringUtils.trim(valueString);
  
      String propertyNameToUse = (isExpressionLanguage && !propertyNameString.endsWith(".elConfig")) ? (propertyNameString + ".elConfig") : propertyNameString;
      
      textReplaceMap.put("currentConfigPropertyName", propertyNameToUse);

      GrouperConfigHibernate[] grouperConfigHibernateToReturn = new GrouperConfigHibernate[1];
  
      // standard validation
      if (!validateConfigEdit(configFileName, propertyNameString, 
          valueString, isExpressionLanguage, message, grouperConfigHibernateToReturn, textReplaceMap)) {
        
        if (fromUi) {
          errorsToDisplay.add(message.toString());
        } else {
          System.out.println(message.toString());
        }
        
        error[0] = true;
        
        return false;
      }
      
      GrouperConfigHibernate grouperConfigHibernate = grouperConfigHibernateToReturn[0];
  
      boolean isPassword = DbConfigEngine.isPassword(configFileName, null, propertyNameString, valueString, true, userSelectedPassword);
      
      boolean isAlreadyEncrypted = false;
      if (!StringUtils.isBlank(valueString)) {
        try {
          Morph.decrypt(valueString);
          isAlreadyEncrypted = true;
        } catch (Exception e) {
          // ignore
        }
      }
      
      if (isPassword || isAlreadyEncrypted) {
        if (!isAlreadyEncrypted) {
          valueString = Morph.encrypt(valueString);
        }
      }
  
      // see if we are creating a new one
      if (grouperConfigHibernate == null) {
        grouperConfigHibernate = new GrouperConfigHibernate();
        added[0] = true;
      } else {
        
        if (StringUtils.equals(valueString, grouperConfigHibernate.getConfigValueDb())) {
          added[0] = null;
        } else {
          added[0] = false;
        }
      }
  
      grouperConfigHibernate.setConfigEncrypted(isPassword || isAlreadyEncrypted);
      grouperConfigHibernate.setConfigFileHierarchyDb("INSTITUTION");
      grouperConfigHibernate.setConfigFileNameDb(configFileName.getConfigFileName());
      // this will switch to or from .elConfig
      grouperConfigHibernate.setConfigKey(propertyNameToUse);
      
  //    grouperConfigHibernate.setConfigComment(comment);
      
      grouperConfigHibernate.setConfigValue(valueString);
      if (added[0] != null) {
        grouperConfigHibernate.saveOrUpdate();
        if (clearCache) {
          // get the latest and greatest
          ConfigPropertiesCascadeBase.clearCache();
        }
      }
      
      if (added[0] == null) {
        message.append(GrouperTextContainer.retrieveFromRequest().getText().get("configurationFilesEditedNotChanged")).append(fromUi ? "<br />" : "\n");
      } else if (added[0]) {
        message.append(GrouperTextContainer.retrieveFromRequest().getText().get("configurationFilesAdded")).append(fromUi ? "<br />" : "\n");
      } else {
        message.append(GrouperTextContainer.retrieveFromRequest().getText().get("configurationFilesEdited")).append(fromUi ? "<br />" : "\n");
      }
  
      String valueForAudit = grouperConfigHibernate.isConfigEncrypted() ? DbConfigEngine.ESCAPED_PASSWORD : grouperConfigHibernate.getConfigValueDb();
      if (added[0] != null) {
  
        AuditTypeBuiltin auditTypeBuiltin = added[0] ? AuditTypeBuiltin.CONFIGURATION_ADD : AuditTypeBuiltin.CONFIGURATION_UPDATE;
        
        AuditEntry auditEntry = new AuditEntry(auditTypeBuiltin, "id", 
            grouperConfigHibernate.getId(), "configFile", grouperConfigHibernate.getConfigFileNameDb(), 
            "key", grouperConfigHibernate.getConfigKey(), "value", 
            valueForAudit, "configHierarchy", grouperConfigHibernate.getConfigFileHierarchyDb());
        
        auditEntry.setDescription((added[0] ? "Add" : "Update") + " config entry: " + grouperConfigHibernate.getConfigFileNameDb() 
          + ", " + grouperConfigHibernate.getConfigKey() + " = " + valueForAudit);
        auditEntry.saveOrUpdate(true);
      }
    } finally {
      GrouperTextContainer.resetThreadLocalVariableMap();
    }

    return true;

  }

  /**
   * @param configFileString
   * @param propertyNameStringInput 
   * @param clearCache should be true unless doing multiple, in which case clear at end
   */
  public static String configurationFileItemDeleteHelper(final String configFileString, 
      final String propertyNameStringInput, final boolean fromUi, final boolean clearCache) {
    
    try {
    
      Map<String, Object> textReplaceMap = new HashMap<String, Object>();
      GrouperTextContainer.assignThreadLocalVariableMap(textReplaceMap);
      
      final ConfigFileName configFileName = ConfigFileName.valueOfIgnoreCase(configFileString, false);
  
      if (StringUtils.isBlank(configFileString)) {
      
        throw new RuntimeException("configFile is not being sent!");
  
      }

      textReplaceMap.put("currentConfigFileName", configFileName.getConfigFileName());
      
  
      String result = (String)HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {
        
        @Override
        public Object callback(HibernateHandlerBean hibernateHandlerBean)
            throws GrouperDAOException {
  
          if (StringUtils.isBlank(propertyNameStringInput)) {
            throw new RuntimeException("Property name does not exist");
          }
          
          String propertyNameString = GrouperUtil.stripSuffix(propertyNameStringInput, ".elConfig");
          
          Set<GrouperConfigHibernate> grouperConfigHibernates = GrouperDAOFactory.getFactory().getConfig().findAll(configFileName, null, propertyNameString);
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
                configurationFileItemDeleteHelper(current, configFileName, fromUi);
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
          boolean deleted = false;
          String message = null;
          if (grouperConfigHibernate != null) {
            textReplaceMap.put("currentConfigPropertyName", grouperConfigHibernate.getConfigKey());
            message = configurationFileItemDeleteHelper(grouperConfigHibernate, configFileName, fromUi);
            deleted = true;
          }
          if (grouperConfigHibernateEl != null) {
            if (textReplaceMap.get("currentConfigPropertyName") == null) {
              textReplaceMap.put("currentConfigPropertyName", GrouperUtil.stripSuffix(grouperConfigHibernateEl.getConfigKey(), ".elConfig"));
            }
            if (message!=null) {
              message += "<br />";
            }
            message = configurationFileItemDeleteHelper(grouperConfigHibernateEl, configFileName, fromUi);
            deleted = true;
          }
          if (!deleted) {
            return GrouperTextContainer.retrieveFromRequest().getText().get("configurationFilesDeletedNotChanged");
          } else {
            if (clearCache) {
              ConfigPropertiesCascadeBase.clearCache();
            }
          }
          return message;
        }
      });
      
      return result;
    } finally {
      GrouperTextContainer.resetThreadLocalVariableMap();
    }

  }



  /**
   * configuration file item delete helper
   * @param grouperConfigHibernate
   * @param configFileName 
   */
  public static String configurationFileItemDeleteHelper(GrouperConfigHibernate grouperConfigHibernate, 
      ConfigFileName configFileName, boolean fromUi) {

    // see if we are creating a new one
    if (grouperConfigHibernate == null) {
      return null;
    }

    grouperConfigHibernate.delete();
    
    boolean isValueEncrypted = isPassword(configFileName, null, grouperConfigHibernate.getConfigKey(), 
        grouperConfigHibernate.getConfigValueDb(), true, grouperConfigHibernate.isConfigEncrypted());

    String valueForAudit = isValueEncrypted ? ESCAPED_PASSWORD : grouperConfigHibernate.getConfigValueDb();

    AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.CONFIGURATION_DELETE, "id", 
        grouperConfigHibernate.getId(), "configFile", grouperConfigHibernate.getConfigFileNameDb(), 
        "key", grouperConfigHibernate.getConfigKey(), "previousValue", 
        valueForAudit, 
            "configHierarchy", grouperConfigHibernate.getConfigFileHierarchyDb());
    auditEntry.setDescription("Delete config entry: " + grouperConfigHibernate.getConfigFileNameDb() 
      + ", " + grouperConfigHibernate.getConfigKey() + " = " + valueForAudit);
    auditEntry.saveOrUpdate(true);

    final String message = GrouperTextContainer.retrieveFromRequest().getText().get("configurationFilesDeleted");
    if (!fromUi) {
      System.out.println(message);
    }
    return message;
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
  
    if (key != null && key.endsWith(".elConfig")) {
      // this is a script, not a password
      return false;
    }
    
    if (hasValue && !StringUtils.isBlank(value)) {
      try {
        Morph.decrypt(value);
        return true;
      } catch (Exception e) {
        // ignore
      }
    }
    
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
      String lowerKey = key.toLowerCase();
  
      if (lowerKey.contains("pass") || lowerKey.contains("secret") || lowerKey.contains("private")) {
        return true;
      }
    
      //lets try to find the config item metadata by key to be sure
      if (configItemMetadata == null) {
        configItemMetadata = ConfigFileName.findConfigItemMetdata(key);
      }
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
  public static boolean isPasswordHelper(ConfigItemMetadata configItemMetadata) {
    
    if (configItemMetadata != null) {
      return configItemMetadata.isSensitive() || configItemMetadata.getValueType() == ConfigItemMetadataType.PASSWORD;
    }
    return false;
  }

  /**
   * 
   */
  protected static Log LOG = LogFactory.getLog(DbConfigEngine.class);

  /**
   * return if the edit should continue
   * @return true if ok, false if should stop now
   */
  public static boolean validateConfigEdit(ConfigFileName configFileName, 
      String propertyNameString, String valueString, boolean isExpressionLanguage, 
      StringBuilder message, GrouperConfigHibernate[] grouperConfigHibernateToReturn, Map<String, Object> textReplaceMap) {
  
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
  
    grouperConfigHibernateToReturn[0] = grouperConfigHibernate;
    
    for (ConfigFileName current : ConfigFileName.values()) {
      
      if (current == configFileName) {
        continue;
      }
      
      if (current == ConfigFileName.GROUPER_TEXT_FR_FR_PROPERTIES && !GrouperConfig.retrieveConfig().textBundleFromLanguageAndCountry().containsKey("fr_fr")) {
        continue;
      }
      
      ConfigPropertiesCascadeBase configPropertiesCascadeBase = current.getConfig();
      
      if (configPropertiesCascadeBase.containsKey(GrouperClientUtils.stripEnd(propertyNameString, ".elConfig"))) {
        
        textReplaceMap.put("currentConfigPropertyName", propertyNameString);
        textReplaceMap.put("currentConfigFileName", current.getConfigFileName());

        message.append(GrouperTextContainer.retrieveFromRequest().getText().get("configurationFilesPropertyExistsInAnother")).append("<br />");
      }
      
    }
  
    
    return true;
  }

  /**
   * if the value is a file and it exists, then this is not a password to be escaped
   * @param configItemMetadata
   * @param propertyValueString
   * @return true if should be escaped
   */
  public static boolean isPasswordHelper(ConfigItemMetadata configItemMetadata,
      String propertyValueString) {
    if (StringUtils.isBlank(propertyValueString)) {
      return false;
    }
    if (propertyValueString.length() > 5 && new File(propertyValueString).exists()) {
      return false;
    }
    return isPasswordHelper(configItemMetadata);
  }
  

}
