package edu.internet2.middleware.grouper.app.externalSystem;

import edu.internet2.middleware.grouper.app.azure.AzureGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemMetadata;
import edu.internet2.middleware.grouper.helper.GrouperTest;

public class GrouperConfigurationModuleAttributeTest extends GrouperTest {
  
  /**
   * @param name
   */
  public GrouperConfigurationModuleAttributeTest(String name) {
    super(name);
  }
  
  public void testGetValueOrExpressionEvaluation() {
    
    // expression script is returned when expression language is true
    GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
    attribute.setExpressionLanguage(true);
    attribute.setValue("abc");
    attribute.setExpressionLanguageScript("script");
    String value = attribute.getValueOrExpressionEvaluation();
    assertEquals("script", value);
    
    // value is returned when expression language is not set or is false
    attribute = new GrouperConfigurationModuleAttribute();
    attribute.setValue("abc");
    attribute.setExpressionLanguageScript("script");
    value = attribute.getValueOrExpressionEvaluation();
    assertEquals("abc", value);
    
  }
  
  public void testGetHtmlForElementIdHandle() {
    
    GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
    attribute.setConfigSuffix("testConfigSuffix");
    
    String htmlElementId = attribute.getHtmlForElementIdHandle();
    
    assertEquals("#config_testConfigSuffix_id", htmlElementId);
    
  }
  
  public void testIsShow() {
    
    GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
    attribute.setConfigSuffix("azureConnector2");
    
    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
    
    grouperExternalSystemAzure.setConfigId("azureConnector2");
    
    GrouperExternalSystem azureExtension = new AzureGrouperExternalSystem() {
      
      @Override
      public Boolean showAttributeOverride(String suffix) {
        return true;
      }
    };
    
    attribute.setGrouperConfigModule(azureExtension);
    boolean isShow = attribute.isShow();
    assertEquals(true, isShow);
    
    
    // test when config item doesn't have showEL
    attribute.setGrouperConfigModule(grouperExternalSystemAzure);
    
    ConfigItemMetadata configItemMetadata = new ConfigItemMetadata();
    configItemMetadata.setShowEl("");
    attribute.setConfigItemMetadata(configItemMetadata);
    isShow = attribute.isShow();
    assertEquals(true, isShow);
    
    // jexl expression is show
//    #optional (note, time limit is for search operations, timeout is for connection timeouts), 
//    #most of these default to ldaptive defaults.  times are in millis
//    # {valueType: "integer", regex: "^ldap\\.([^.]+)\\.minPoolSize$", subSection: "pooling", showEl: "${customizePooling}"}
//    #ldap.personLdap.minPoolSize =
    LdapGrouperExternalSystem ldapGrouperExternalSystem = new LdapGrouperExternalSystem();
    ldapGrouperExternalSystem.setConfigId("ldapConnector");
    attribute = new GrouperConfigurationModuleAttribute();
    attribute.setConfigSuffix("ldapConnector");
    attribute.setGrouperConfigModule(ldapGrouperExternalSystem);
    configItemMetadata = new ConfigItemMetadata();
    configItemMetadata.setShowEl("${customizePooling}");
    attribute.setConfigItemMetadata(configItemMetadata);
    isShow = attribute.isShow();
    assertEquals(false, isShow);
    
  }
  
}
