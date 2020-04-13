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

import edu.internet2.middleware.grouper.app.azure.GrouperExternalSystemAzure;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigItemFormElement;
import edu.internet2.middleware.grouper.cfg.dbConfig.DbConfigEngine;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;


/**
 * @author mchyzer
 *
 */
public class GrouperExternalSystemTest extends GrouperTest {

  public static void main(String[] args) {
    
//    TestRunner.run(new GrouperExternalSystemTest("testExternalSystemAzure"));
    TestRunner.run(new GrouperExternalSystemTest("testExternalSystemAzureInsertEditDelete"));
  }
  
  /**
   * @param name
   */
  public GrouperExternalSystemTest(String name) {
    super(name);
  }

  public void testNoExternalSystemAzure() {
    
    GrouperExternalSystemAzure grouperExternalSystemAzure = new GrouperExternalSystemAzure();
    assertEquals(0, GrouperUtil.length(grouperExternalSystemAzure.retrieveConfigurationConfigIds()));
    
  }
  
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


    
    GrouperExternalSystemAzure grouperExternalSystemAzure = new GrouperExternalSystemAzure();
    
    Set<String> propertyNames = grouperExternalSystemAzure.retrieveConfigurationKeysByPrefix("grouper.azureConnector.testAzure.");
    
    assertEquals(11, GrouperUtil.length(propertyNames));
    assertTrue(propertyNames.contains("grouper.azureConnector.testAzure.loginEndpoint"));
    assertTrue(propertyNames.contains("grouper.azureConnector.testAzure.subjectIdValueFormat"));
    
    assertEquals(1, GrouperUtil.length(grouperExternalSystemAzure.retrieveConfigurationConfigIds()));
    assertEquals("testAzure", grouperExternalSystemAzure.retrieveConfigurationConfigIds().iterator().next());
    
    grouperExternalSystemAzure.setConfigId("testAzure");
    
    Map<String, GrouperExternalSystemAttribute> grouperExternalSystemAttributes = grouperExternalSystemAzure.retrieveAttributes();

    assertEquals(12, GrouperUtil.length(grouperExternalSystemAttributes));

    {
      GrouperExternalSystemAttribute loginEndpointAttribute = grouperExternalSystemAttributes.get("loginEndpoint");
      assertEquals("loginEndpoint", loginEndpointAttribute.getConfigSuffix());
      assertEquals(false, loginEndpointAttribute.isExpressionLanguage());
      assertEquals(ConfigItemFormElement.TEXT, loginEndpointAttribute.getFormElement());
    }
    {
      GrouperExternalSystemAttribute clientSecretAttribute = grouperExternalSystemAttributes.get("client_secret");
      assertEquals("client_secret", clientSecretAttribute.getConfigSuffix());
      assertEquals(false, clientSecretAttribute.isExpressionLanguage());
      assertEquals(true, clientSecretAttribute.isPassword());
      assertEquals(ConfigItemFormElement.PASSWORD, clientSecretAttribute.getFormElement());
    }
    
    
    {
      GrouperExternalSystemAttribute directoryIdAttribute = grouperExternalSystemAttributes.get("DirectoryID");
      assertEquals("DirectoryID", directoryIdAttribute.getConfigSuffix());
      assertEquals(true, directoryIdAttribute.isExpressionLanguage());
      assertEquals("${'someDirectoryId'}", directoryIdAttribute.getExpressionLanguageScript());
      assertEquals(ConfigItemFormElement.TEXT, directoryIdAttribute.getFormElement());
    }
    
    {
      GrouperExternalSystemAttribute enabledAttribute = grouperExternalSystemAttributes.get("enabled");
      assertTrue(StringUtils.isBlank(enabledAttribute.getValue()));
      assertEquals("true", enabledAttribute.getDefaultValue());
      assertEquals(ConfigItemFormElement.DROPDOWN, enabledAttribute.getFormElement());
    }
  }

  public void testExternalSystemAzureInsertEditDelete() {

    GrouperExternalSystemAzure grouperExternalSystemAzure = new GrouperExternalSystemAzure();
    
    grouperExternalSystemAzure.setConfigId("azureConnector2");
    
    Map<String, GrouperExternalSystemAttribute> suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
    
    suffixToAttribute.get("loginEndpoint").setValue("https://test.whatever.com");
    suffixToAttribute.get("DirectoryID").setExpressionLanguage(true);
    suffixToAttribute.get("DirectoryID").setExpressionLanguageScript("${'someDirectoryId'}");
    suffixToAttribute.get("client_secret").setValue("someSecret");
    suffixToAttribute.get("graphEndpoint").setValue("myGraphEndpoint");

    boolean fail = false;
    try {
      grouperExternalSystemAzure.deleteConfig(false);
      fail = true;
    } catch (RuntimeException re) {
      // good
    }
    assertFalse("cant delete something not there", fail);

    StringBuilder message = new StringBuilder();
    List<String> errorsToDisplay = new ArrayList<String>();
    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
    
    fail = false;
    try {
      grouperExternalSystemAzure.editConfig(false, suffixToAttribute, message, errorsToDisplay, validationErrorsToDisplay);
      fail = true;
    } catch (RuntimeException re) {
      // good
    }
    assertFalse("cant edit something not there", fail);

    grouperExternalSystemAzure.insertConfig(false, suffixToAttribute, message, errorsToDisplay, validationErrorsToDisplay);

    
    message.setLength(0);
    errorsToDisplay.clear();
    validationErrorsToDisplay.clear();
    
    grouperExternalSystemAzure = new GrouperExternalSystemAzure();
    grouperExternalSystemAzure.setConfigId("azureConnector2");
    suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
    
    assertEquals("https://test.whatever.com", suffixToAttribute.get("loginEndpoint").getValue());
    assertEquals("someDirectoryId", suffixToAttribute.get("DirectoryID").getValue());
    assertEquals("${'someDirectoryId'}", suffixToAttribute.get("DirectoryID").getExpressionLanguageScript());
    assertEquals(DbConfigEngine.ESCAPED_PASSWORD, suffixToAttribute.get("client_secret").getValue());
    
    // lets test by adding, deleting editing
    
    suffixToAttribute.get("resource").setValue("myResource");
    suffixToAttribute.get("loginEndpoint").setValue(null);
    suffixToAttribute.get("graphEndpoint").setValue("myGraphEndpoint1");
    
    grouperExternalSystemAzure.editConfig(false, suffixToAttribute, message, errorsToDisplay, validationErrorsToDisplay);
    
    message.setLength(0);
    errorsToDisplay.clear();
    validationErrorsToDisplay.clear();
    
    grouperExternalSystemAzure = new GrouperExternalSystemAzure();
    grouperExternalSystemAzure.setConfigId("azureConnector2");
    suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();
    
    assertEquals("myResource", suffixToAttribute.get("resource").getValue());
    assertNull(suffixToAttribute.get("loginEndpoint").getValue());
    assertEquals("myGraphEndpoint1", suffixToAttribute.get("graphEndpoint").getValue());
    
    // delete
    grouperExternalSystemAzure.deleteConfig(false);
    
    message.setLength(0);
    errorsToDisplay.clear();
    validationErrorsToDisplay.clear();
    
    grouperExternalSystemAzure = new GrouperExternalSystemAzure();
    grouperExternalSystemAzure.setConfigId("azureConnector2");
    suffixToAttribute = grouperExternalSystemAzure.retrieveAttributes();

    assertNull(suffixToAttribute.get("resource").getValue());
    assertNull(suffixToAttribute.get("loginEndpoint").getValue());
    assertNull(suffixToAttribute.get("graphEndpoint").getValue());
    
    assertFalse(grouperExternalSystemAzure.retrieveConfigurationConfigIds().contains("azureConnector2"));

  }

}
