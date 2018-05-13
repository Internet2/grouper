package edu.internet2.middleware.grouper.app.deprovisioning;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * test get and store configuration to the database
 * @author mchyzer
 *
 */
public class GrouperDeprovisioningOverallConfigurationTest extends GrouperTest {

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDeprovisioningOverallConfigurationTest("testRetrieveConfiguration"));
  }
  
  /**
   */
  public GrouperDeprovisioningOverallConfigurationTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GrouperDeprovisioningOverallConfigurationTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testRetrieveConfiguration() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group someGroup = new GroupSave(grouperSession).assignName("a:b:c")
        .assignCreateParentStemsIfNotExist(true).save();

    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = 
        GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(someGroup);
    
    assertEquals(someGroup, grouperDeprovisioningOverallConfiguration.getOriginalOwner());
    
    assertEquals(0, GrouperUtil.length(grouperDeprovisioningOverallConfiguration.getRealmToConfiguration()));
    
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
    
    grouperDeprovisioningOverallConfiguration.getRealmToConfiguration().put("student", grouperDeprovisioningConfiguration);
    
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
    grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

    grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString("true");
    grouperDeprovisioningAttributeValue.setAutoChangeLoaderString("false");
    grouperDeprovisioningAttributeValue.setAutoselectForRemoval("abc");
    grouperDeprovisioningAttributeValue.setDeprovisionString("def");
    grouperDeprovisioningAttributeValue.setDirectAssignmentString("ghi");
    grouperDeprovisioningAttributeValue.setEmailAddressesString("jkl");
    grouperDeprovisioningAttributeValue.setEmailBodyString("mno");
    grouperDeprovisioningAttributeValue.setEmailSubjectString("pqr");
    grouperDeprovisioningAttributeValue.setInheritedFromFolderIdString("hgn");
    grouperDeprovisioningAttributeValue.setMailToGroupString("stu");
    grouperDeprovisioningAttributeValue.setRealmString("vwx");
    grouperDeprovisioningAttributeValue.setSendEmailString("ace");
    grouperDeprovisioningAttributeValue.setShowForRemovalString("bdf");
    grouperDeprovisioningAttributeValue.setStemScopeString("gjo");
    
    grouperDeprovisioningOverallConfiguration.storeConfigurationForRealm("student");
    
    EhcacheController.ehcacheController().flushCache();
    
    grouperDeprovisioningOverallConfiguration = 
        GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(someGroup);
    
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration2 = grouperDeprovisioningOverallConfiguration.getRealmToConfiguration().get("student");
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue2 = grouperDeprovisioningConfiguration2.getOriginalConfig();
    
    assertEquals(grouperDeprovisioningAttributeValue.getAllowAddsWhileDeprovisionedString(), grouperDeprovisioningAttributeValue2.getAllowAddsWhileDeprovisionedString());
    assertEquals(grouperDeprovisioningAttributeValue.getAutoChangeLoaderString(), grouperDeprovisioningAttributeValue2.getAutoChangeLoaderString());
    assertEquals(grouperDeprovisioningAttributeValue.getAutoselectForRemoval(), grouperDeprovisioningAttributeValue2.getAutoselectForRemoval());
    assertEquals(grouperDeprovisioningAttributeValue.getDeprovisionString(), grouperDeprovisioningAttributeValue2.getDeprovisionString());
    assertEquals(grouperDeprovisioningAttributeValue.getDirectAssignmentString(), grouperDeprovisioningAttributeValue2.getDirectAssignmentString());
    assertEquals(grouperDeprovisioningAttributeValue.getEmailAddressesString(), grouperDeprovisioningAttributeValue2.getEmailAddressesString());
    assertEquals(grouperDeprovisioningAttributeValue.getEmailBodyString(), grouperDeprovisioningAttributeValue2.getEmailBodyString());
    assertEquals(grouperDeprovisioningAttributeValue.getEmailSubjectString(), grouperDeprovisioningAttributeValue2.getEmailSubjectString());
    assertEquals(grouperDeprovisioningAttributeValue.getInheritedFromFolderIdString(), grouperDeprovisioningAttributeValue2.getInheritedFromFolderIdString());
    assertEquals(grouperDeprovisioningAttributeValue.getMailToGroupString(), grouperDeprovisioningAttributeValue2.getMailToGroupString());
    assertEquals(grouperDeprovisioningAttributeValue.getRealmString(), grouperDeprovisioningAttributeValue2.getRealmString());
    assertEquals(grouperDeprovisioningAttributeValue.getSendEmailString(), grouperDeprovisioningAttributeValue2.getSendEmailString());
    assertEquals(grouperDeprovisioningAttributeValue.getShowForRemovalString(), grouperDeprovisioningAttributeValue2.getShowForRemovalString());
    assertEquals(grouperDeprovisioningAttributeValue.getStemScopeString(), grouperDeprovisioningAttributeValue2.getStemScopeString());
    assertEquals("gjo", grouperDeprovisioningAttributeValue2.getStemScopeString());
    
  }
  
  
}



