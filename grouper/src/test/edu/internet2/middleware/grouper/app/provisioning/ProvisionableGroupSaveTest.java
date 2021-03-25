package edu.internet2.middleware.grouper.app.provisioning;

import org.junit.Assert;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import junit.textui.TestRunner;

public class ProvisionableGroupSaveTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    setupProvisioningConfig();
    
  }
  
  private void setupProvisioningConfig() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.url", "ldap://localhost:389");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("ldap.personLdap.pass", "secret");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.configureMetadata", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.numberOfMetadata", "1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.formElementType", "text");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.name", "md_testInput");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.showForGroup", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.showForFolder", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest.metadata.0.required", "true");

  }
  
  /**
   * @param name
   */
  public ProvisionableGroupSaveTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new ProvisionableGroupSaveTest("testSaveProvisioningLookupByGroup"));
  }

  
  public void testSaveProvisioningLookupByGroup() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest").assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveProvisioningLookupByGroupId() {
      
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroupId(group.getId()).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
      
  }

  public void testSaveProvisioningLookupByGroupName() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroupName(group.getName()).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveProvisioningGroupNotFound() {
    
    boolean exceptionThrown = false;
    try {
      ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
      provisionableGroupSave.assignTargetName("ldapProvTest")
          .assignMetadataString("md_testInput", "testValue").assignGroupName("non_existing_stem").save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveProvisioningRunAsRoot() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroup(group)
        .assignRunAsRoot(true)
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveProvisioningSubjectIsNotWheelOrRoot() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    
    try {
      ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
      provisionableGroupSave.assignTargetName("ldapProvTest")
          .assignMetadataString("md_testInput", "testValue").assignGroup(group)
          .save();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("is not wheel or root"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveProvisioningInvalidTarget() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    
    try {
      ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
      provisionableGroupSave.assignTargetName("invalid_target")
          .assignMetadataString("md_testInput", "testValue").assignGroup(group)
          .save();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("must be one of the valid targets"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveProvisioningTargetNotAssigned() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    
    try {
      ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
      provisionableGroupSave
          .assignMetadataString("md_testInput", "testValue").assignGroup(group)
          .save();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("target is required"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testDeleteProvisioning() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroupName(group.getName()).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
    provisionableGroupSave = new ProvisionableGroupSave();
    grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignSaveMode(SaveMode.DELETE).assignGroupName(group.getName()).save();
    
    Assert.assertEquals(SaveResultType.DELETE, provisionableGroupSave.getSaveResultType());
    Assert.assertNull(grouperProvisioningAttributeValue);
    
  }
  
  public void testSaveProvisioningUpdateSettings() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroupName(group.getName()).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
    new ProvisionableGroupSave();
    grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignProvision(false)
        .assignMetadataString("md_testInput", "testValue1").assignGroupName(group.getName()).save();
    
    Assert.assertNull(grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals(SaveResultType.UPDATE, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue1", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
  }
  
  public void testSaveProvisioningNoChange() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroupName(group.getName()).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
    provisionableGroupSave = new ProvisionableGroupSave();
    grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
    .assignMetadataString("md_testInput", "testValue").assignGroupName(group.getName()).save();
    
    Assert.assertEquals(SaveResultType.NO_CHANGE, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
  }
  
  public void testSaveProvisioningReplaceAllSettingsFalse() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignProvision(false)
        .assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableGroupSave.getSaveResultType());
    Assert.assertNull(grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
    provisionableGroupSave = new ProvisionableGroupSave();
    grouperProvisioningAttributeValue = provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignProvision(true)
        .assignReplaceAllSettings(false)
        .assignGroup(group).save();
    
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals(SaveResultType.UPDATE, provisionableGroupSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    
    Assert.assertEquals(0, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    
  }
  
  public void testSaveProvisioningRequiredMetadataFieldNotAssigned() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    boolean exceptionThrown = false;
    try {
      ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
      provisionableGroupSave.assignTargetName("ldapProvTest")
          .assignGroup(group).save();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("md_testInput is a required field"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveProvisioningNotValidMetadataFieldAssigned() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    boolean exceptionThrown = false;
    try {
      ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
      provisionableGroupSave.assignTargetName("ldapProvTest")
      .assignMetadataString("md_testInput", "testValue")
      .assignMetadataString("md_testInput1", "testValue")
      .assignGroup(group).save();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("md_testInput1"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
}
