package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.ldap.LdapSearchScope;
import edu.internet2.middleware.grouper.ldap.LdapSessionUtils;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class LdapGrouperExternalSystem extends GrouperExternalSystem {
  
  public static void main(String[] args) {
    GrouperStartup.startup();
    // ldap://ldap.andrew.cmu.edu:389/dc=cmu,dc=edu
    LdapGrouperExternalSystem ldapGrouperExternalSystem = new LdapGrouperExternalSystem();
    ldapGrouperExternalSystem.setConfigId("sdfasdf");
    
    System.out.println(ldapGrouperExternalSystem.test());
  }
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "ldap." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(ldap)\\.([^.]+)\\.(.*)$";
  }

  @Override
  public List<String> test() throws UnsupportedOperationException {
    
    String uiTestSearchDn = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestSearchDn");
    String uiTestSearchScope = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestSearchScope");
    String uiTestFilter = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestFilter");
    String uiTestAttributeName = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestAttributeName");
    String uiTestExpectedValue = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestExpectedValue");
    
    if (!LdapSessionUtils.ldapSession().testConnection(this.getConfigId())) {
      return GrouperUtil.toList("Invalid config");
    }
    
    if (StringUtils.isNotBlank(uiTestSearchDn) || StringUtils.isNotBlank(uiTestSearchScope) ||
        StringUtils.isNotBlank(uiTestAttributeName) || StringUtils.isNotBlank(uiTestFilter) ||
        StringUtils.isNotBlank(uiTestExpectedValue)) {
      
      LdapSearchScope ldapSearchScope = LdapSearchScope.valueOfIgnoreCase(uiTestSearchScope, true);
      
      List<String> list = LdapSessionUtils.ldapSession().list(String.class, this.getConfigId(), uiTestSearchDn, ldapSearchScope, uiTestFilter, uiTestAttributeName);
      int length = GrouperUtil.length(list);
      if (length != 1) {
        String errorMessage = "Expected 1 result but received "+ GrouperUtil.length(list);
        if (length > 1) {
          errorMessage += ", e.g. ";
          for (int i=0; i<5; i++) {
            
            if (i>length-1) {
              break;
            }
            if (i>0) {
              errorMessage += ", ";
            }
            errorMessage += "'"+list.get(i)+"'";
          }
        }
        return GrouperUtil.toList(errorMessage);
      }
      String result = list.get(0);
      if (!StringUtils.equals(uiTestExpectedValue, result)) {
        String errorMessage = GrouperTextContainer.textOrNull("grouperConfigurationTestExpectedNotMatchingResult");
        errorMessage = errorMessage.replace("$$expectedValue$$", uiTestExpectedValue);
        errorMessage = errorMessage.replace("$$receivedValue$$", result);
        return GrouperUtil.toList(errorMessage);
      }
      return null;
    }
    
    return null;
  }

  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "personLdap";
  }
  
  @Override
  public void validatePreSave(boolean isInsert, boolean fromUi, List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, fromUi, errorsToDisplay, validationErrorsToDisplay);

    String uiTestSearchDn = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestSearchDn");
    String uiTestSearchScope = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestSearchScope");
    String uiTestFilter = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestFilter");
    String uiTestAttributeName = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestAttributeName");
    String uiTestExpectedValue = GrouperLoaderConfig.retrieveConfig().propertyValueString("ldap." + this.getConfigId() + ".uiTestExpectedValue");
  
    if (StringUtils.isNotBlank(uiTestSearchDn) || StringUtils.isNotBlank(uiTestSearchScope) ||
        StringUtils.isNotBlank(uiTestAttributeName) || StringUtils.isNotBlank(uiTestFilter) ||
        StringUtils.isNotBlank(uiTestExpectedValue)) {
    
      if (StringUtils.isBlank(uiTestSearchDn)) {
          validationErrorsToDisplay.put("#uiTestSearchDn",
              GrouperTextContainer.textOrNull("grouperConfigurationValidationLdapUiTestConfigMissing"));
      }
      
      if (StringUtils.isBlank(uiTestSearchScope)) {
        validationErrorsToDisplay.put("#uiTestSearchScope",
            GrouperTextContainer.textOrNull("grouperConfigurationValidationLdapUiTestConfigMissing"));
      }
      
      if (StringUtils.isBlank(uiTestAttributeName)) {
        validationErrorsToDisplay.put("#uiTestAttributeName",
            GrouperTextContainer.textOrNull("grouperConfigurationValidationLdapUiTestConfigMissing"));
      }
      
      if (StringUtils.isBlank(uiTestFilter)) {
        validationErrorsToDisplay.put("#uiTestFilter",
            GrouperTextContainer.textOrNull("grouperConfigurationValidationLdapUiTestConfigMissing"));
      }
      
      if (StringUtils.isBlank(uiTestExpectedValue)) {
        validationErrorsToDisplay.put("#uiTestExpectedValue",
            GrouperTextContainer.textOrNull("grouperConfigurationValidationLdapUiTestConfigMissing"));
      }
      
   }
      
    
    
    
    
  }
  
  public void refreshConnectionsIfNeeded() throws UnsupportedOperationException {
    LdapSessionUtils.ldapSession().refreshConnectionsIfNeeded(this.getConfigId());
  }
}
