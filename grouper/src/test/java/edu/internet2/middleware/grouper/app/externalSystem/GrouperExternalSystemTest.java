/**
 * 
 */
package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
//import edu.internet2.middleware.grouper.azure.AzureGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperConfigHibernate;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


/**
 * @author mchyzer
 *
 */
public class GrouperExternalSystemTest extends GrouperTest {

  public static void main(String[] args) {
    
    TestRunner.run(new GrouperExternalSystemTest("testExternalSystemAzure"));
  }
  
  /**
   * @param name
   */
  public GrouperExternalSystemTest(String name) {
    super(name);
  }

//  public void testNoExternalSystemAzure() {
//
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//    assertEquals(0, GrouperUtil.length(grouperExternalSystemAzure.retrieveConfigurationConfigIds()));
//
//  }
  
  public void testExternalSystemAzure() {

    //  # login endpoint to get a token
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.loginEndpoint$"}
    //  # grouper.azureConnector.myAzure.loginEndpoint = https://login.microsoftonline.com
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.loginEndpoint", "https://test.whatever.com");

    //  # azure directory id
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.DirectoryID$"}
    //  # grouper.azureConnector.myAzure.DirectoryID = 6c4dxxx0d
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.DirectoryID.elConfig", "${'someDirectoryId'}");

    //  # client id
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.client_id$"}
    //  # grouper.azureConnector.myAzure.client_id = fd805xxxxdfb
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.client_id", "someClientId");

    //  # client secret
    //  # {valueType: "password", sensitive: true, regex: "^db\\.([^.]+)\\.client_secret$"}
    //  #grouper.azureConnector.myAzure.client_secret = ******************
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.client_secret", "someSecret");

    //  # resource.  generally same as graph endpoint
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.resource$"}
    //  # grouper.azureConnector.myAzure.resource = https://graph.microsoft.com
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.resource", "someResource");

    //  # graph endpoint
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.graphEndpoint$"}
    //  # grouper.azureConnector.myAzure.graphEndpoint = https://graph.microsoft.com
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.graphEndpoint", "someGraphEndpoint");

    //  # graph version
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.graphVersion$"}
    //  # grouper.azureConnector.myAzure.graphVersion = v1.0
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.graphVersion", "someGraphVersion");

    //  # group lookup attribute
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.groupLookupAttribute$"}
    //  # grouper.azureConnector.myAzure.groupLookupAttribute = displayName
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.groupLookupAttribute", "displayName");

    //  # group lookup value format
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.groupLookupValueFormat$"}
    //  # grouper.azureConnector.myAzure.groupLookupValueFormat = ${group.getName()}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.groupLookupValueFormat", "${group.getName()}");

    //  # require subject attribute
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.requireSubjectAttribute$"}
    //  # grouper.azureConnector.myAzure.requireSubjectAttribute = netId
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.requireSubjectAttribute", "netId");

    //  # subject id value format
    //  # {valueType: "string", required: true, regex: "^grouper\\.azureConnector\\.([^.]+)\\.subjectIdValueFormat$"}
    //  # grouper.azureConnector.myAzure.subjectIdValueFormat = ${subject.getAttributeValue("netId")}@school.edu
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.subjectIdValueFormat", "${subject.getAttributeValue(\"netId\")}@school.edu");

    //  # if this azure connector is enabled
    //  # {valueType: "boolean", regex: "^grouper\\.azureConnector\\.([^.]+)\\.enabled$", defaultValue: "true"}
    //  # grouper.azureConnector.myAzure.enabled = 


    
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    Set<String> propertyNames = grouperExternalSystemAzure.retrieveConfigurationKeysByPrefix("grouper.azureConnector.testAzure.");
//
//    assertEquals(11, GrouperUtil.length(propertyNames));
//    assertTrue(propertyNames.contains("grouper.azureConnector.testAzure.loginEndpoint"));
//    assertTrue(propertyNames.contains("grouper.azureConnector.testAzure.subjectIdValueFormat"));
//
//    assertEquals(1, GrouperUtil.length(grouperExternalSystemAzure.retrieveConfigurationConfigIds()));
//    assertEquals("testAzure", grouperExternalSystemAzure.retrieveConfigurationConfigIds().iterator().next());
//
//    grouperExternalSystemAzure.setConfigId("testAzure");
//
//    Map<String, GrouperConfigurationModuleAttribute> grouperExternalSystemAttributes = grouperExternalSystemAzure.retrieveAttributes();
//
//    assertEquals(12, GrouperUtil.length(grouperExternalSystemAttributes));

//    {
//      GrouperConfigurationModuleAttribute loginEndpointAttribute = grouperExternalSystemAttributes.get("loginEndpoint");
//      assertEquals("loginEndpoint", loginEndpointAttribute.getConfigSuffix());
//      assertEquals(false, loginEndpointAttribute.isExpressionLanguage());
//      assertEquals(ConfigItemFormElement.TEXT, loginEndpointAttribute.getFormElement());
//    }
//    {
//      GrouperConfigurationModuleAttribute clientSecretAttribute = grouperExternalSystemAttributes.get("client_secret");
//      assertEquals("client_secret", clientSecretAttribute.getConfigSuffix());
//      assertEquals(false, clientSecretAttribute.isExpressionLanguage());
//      assertEquals(true, clientSecretAttribute.isPassword());
//      assertEquals(ConfigItemFormElement.PASSWORD, clientSecretAttribute.getFormElement());
//    }
//
//    {
//      GrouperConfigurationModuleAttribute directoryIdAttribute = grouperExternalSystemAttributes.get("DirectoryID");
//      assertEquals("DirectoryID", directoryIdAttribute.getConfigSuffix());
//      assertEquals(true, directoryIdAttribute.isExpressionLanguage());
//      assertEquals("${'someDirectoryId'}", directoryIdAttribute.getExpressionLanguageScript());
//      assertEquals(ConfigItemFormElement.TEXT, directoryIdAttribute.getFormElement());
//    }
//
//    {
//      GrouperConfigurationModuleAttribute enabledAttribute = grouperExternalSystemAttributes.get("enabled");
//      assertTrue(StringUtils.isBlank(enabledAttribute.getValue()));
//      assertEquals("true", enabledAttribute.getDefaultValue());
//      assertEquals(ConfigItemFormElement.DROPDOWN, enabledAttribute.getFormElement());
//    }
  }

//  public void testExternalSystemAzureInsertEditDelete() {
//
//    GrouperSession.startRootSession();
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//
//    Map<String, GrouperConfigurationModuleAttribute> suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    suffixToAttribute.get("loginEndpoint").setValue("https://test.whatever.com");
//    suffixToAttribute.get("DirectoryID").setExpressionLanguage(true);
//    suffixToAttribute.get("DirectoryID").setExpressionLanguageScript("${'someDirectoryId'}");
//    suffixToAttribute.get("client_secret").setValue("someSecret");
//    suffixToAttribute.get("client_id").setValue("someClientId");
//    suffixToAttribute.get("graphEndpoint").setValue("myGraphEndpoint");
//    suffixToAttribute.get("groupLookupValueFormat").setValue("group lookup value format");
//    suffixToAttribute.get("resource").setValue("resource");
//    suffixToAttribute.get("requireSubjectAttribute").setValue("require subject attribute");
//    suffixToAttribute.get("subjectIdValueFormat").setValue("subject id value format");
//    suffixToAttribute.get("groupLookupAttribute").setValue("group lookup attribute");
//    suffixToAttribute.get("graphVersion").setValue("5.9");
//
//    StringBuilder message = new StringBuilder();
//    List<String> errorsToDisplay = new ArrayList<String>();
//    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
//
//    grouperExternalSystemAzure.insertConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
//
//    message.setLength(0);
//    errorsToDisplay.clear();
//    validationErrorsToDisplay.clear();
//
//    grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//    suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    assertEquals("https://test.whatever.com", suffixToAttribute.get("loginEndpoint").getValue());
//    assertEquals("someDirectoryId", suffixToAttribute.get("DirectoryID").getValue());
//    assertEquals("${'someDirectoryId'}", suffixToAttribute.get("DirectoryID").getExpressionLanguageScript());
//    assertEquals(GrouperConfigHibernate.ESCAPED_PASSWORD, suffixToAttribute.get("client_secret").getValue());
//
//    // lets test by adding, deleting editing
//
//    suffixToAttribute.get("resource").setValue("myResource");
//    suffixToAttribute.get("loginEndpoint").setValue("loginEndpoint");
//    suffixToAttribute.get("graphEndpoint").setValue("myGraphEndpoint1");
//
//    grouperExternalSystemAzure.editConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
//
//    message.setLength(0);
//    errorsToDisplay.clear();
//    validationErrorsToDisplay.clear();
//
//    grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//    suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    assertEquals("myResource", suffixToAttribute.get("resource").getValue());
//    assertEquals("loginEndpoint", suffixToAttribute.get("loginEndpoint").getValue());
//    assertEquals("myGraphEndpoint1", suffixToAttribute.get("graphEndpoint").getValue());
//
//    // delete
//    grouperExternalSystemAzure.deleteConfig(false);
//
//    message.setLength(0);
//    errorsToDisplay.clear();
//    validationErrorsToDisplay.clear();
//
//    grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//    suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    assertEquals(suffixToAttribute.get("resource").getValue(), suffixToAttribute.get("resource").getConfigItemMetadata().getSampleValue());
//    assertEquals(suffixToAttribute.get("loginEndpoint").getValue(), suffixToAttribute.get("loginEndpoint").getConfigItemMetadata().getSampleValue());
//    assertEquals(suffixToAttribute.get("graphEndpoint").getValue(), suffixToAttribute.get("graphEndpoint").getConfigItemMetadata().getSampleValue());
//
//    assertFalse(grouperExternalSystemAzure.retrieveConfigurationConfigIds().contains("azureConnector2"));
//
//  }
  
//  public void testChangeStatus() {
//
//    GrouperSession.startRootSession();
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//
//    Map<String, GrouperConfigurationModuleAttribute> suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    suffixToAttribute.get("enabled").setValue("false");
//
//    grouperExternalSystemAzure.changeStatus(true, new StringBuilder(), new ArrayList<String>(), new HashMap<String, String>());
//
//    grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//    suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    assertEquals("true", suffixToAttribute.get("enabled").getValue());
//
//    grouperExternalSystemAzure.changeStatus(false, new StringBuilder(), new ArrayList<String>(), new HashMap<String, String>());
//
//    grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//    suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    assertEquals("false", suffixToAttribute.get("enabled").getValue());
//
//  }
  
//  public void testIsEnabled() {
//
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//
//    Map<String, GrouperConfigurationModuleAttribute> suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    suffixToAttribute.get("enabled").setValue("true");
//
//    assertTrue(grouperExternalSystemAzure.isEnabled());
//
//    grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//
//    suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    suffixToAttribute.get("enabled").setValue("false");
//
//    assertFalse(grouperExternalSystemAzure.isEnabled());
//
//  }
  
  
//  public void testValidatePreSaveInvalidId() {
//
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("!#$@#$!#");
//
//    List<String> errorsToDisplay = new ArrayList<String>();
//    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
//
//    grouperExternalSystemAzure.validatePreSave(false, true, errorsToDisplay, validationErrorsToDisplay);
//
//    assertTrue(validationErrorsToDisplay.containsKey("#externalSystemConfigId"));
//
//  }
  
//  public void testValidatePreSaveELScriptRequired() {
//
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//
//    Map<String, GrouperConfigurationModuleAttribute> suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    suffixToAttribute.get("DirectoryID").setExpressionLanguage(true);
//    suffixToAttribute.get("DirectoryID").setExpressionLanguageScript(null);
//
//    StringBuilder message = new StringBuilder();
//    List<String> errorsToDisplay = new ArrayList<String>();
//    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
//
//    grouperExternalSystemAzure.insertConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
//
//    assertTrue(validationErrorsToDisplay.containsKey("#config_DirectoryID_id"));
//    assertEquals(validationErrorsToDisplay.get("#config_DirectoryID_id"), "Error: 'Directory ID' is selected for 'EL' (Expression Language) with a blank script.  The EL script is required.");
//
//  }
  
//  public void testValidatePreSaveRequiredValue() {
//
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//
//    Map<String, GrouperConfigurationModuleAttribute> suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//
//    suffixToAttribute.get("client_id").setValue(null);
//
//    StringBuilder message = new StringBuilder();
//    List<String> errorsToDisplay = new ArrayList<String>();
//    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
//
//    grouperExternalSystemAzure.insertConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
//
//    assertTrue(validationErrorsToDisplay.containsKey("#config_client_id_id"));
//    assertEquals(validationErrorsToDisplay.get("#config_client_id_id"), "Error: 'Client ID' is required");
//
//  }
  
//  public void testValidatePreSaveMustExtendClass() {
//
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//
//    Map<String, GrouperConfigurationModuleAttribute> suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//    suffixToAttribute.get("client_id").setValue("java.lang.Long");
//    suffixToAttribute.get("client_id").getConfigItemMetadata().setMustExtendClass("java.lang.String");
//
//    StringBuilder message = new StringBuilder();
//    List<String> errorsToDisplay = new ArrayList<String>();
//    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
//
//    grouperExternalSystemAzure.insertConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
//
//    assertTrue(validationErrorsToDisplay.containsKey("#config_client_id_id"));
//    assertEquals(validationErrorsToDisplay.get("#config_client_id_id"), "Error: 'Client ID' does not extend java.lang.String");
//
//  }
  
//  public void testValidatePreSaveMustImplementInterface() {
//
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("azureConnector2");
//    Map<String, GrouperConfigurationModuleAttribute> suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
//    suffixToAttribute.get("client_id").setValue("java.lang.Long");
//    suffixToAttribute.get("client_id").getConfigItemMetadata().setMustImplementInterface("java.util.List");
//
//    StringBuilder message = new StringBuilder();
//    List<String> errorsToDisplay = new ArrayList<String>();
//    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
//
//    grouperExternalSystemAzure.insertConfig(false, message, errorsToDisplay, validationErrorsToDisplay);
//
//    assertTrue(validationErrorsToDisplay.containsKey("#config_client_id_id"));
//    assertEquals(validationErrorsToDisplay.get("#config_client_id_id"), "Error: 'Client ID' does not implement java.util.List");
//
//  }
  
//  public void testRetrieveExtraAttributes() {
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.testAzure.customProperty", "custom value");
//
//    AzureGrouperExternalSystem grouperExternalSystemAzure = new AzureGrouperExternalSystem();
//
//    grouperExternalSystemAzure.setConfigId("testAzure");
//
//    Map<String, GrouperConfigurationModuleAttribute> grouperExternalSystemAttributes = grouperExternalSystemAzure.retrieveAttributes();
//
//    assertEquals("custom value", grouperExternalSystemAttributes.get("customProperty").getValue());
//
//  }
  
}
