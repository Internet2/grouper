package edu.internet2.middleware.grouper.app.provisioning;

import org.junit.Assert;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import junit.textui.TestRunner;

public class ProvisionableStemSaveTest extends GrouperTest {
  
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
  public ProvisionableStemSaveTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new ProvisionableStemSaveTest("testSaveProvisioningReplaceAllSettingsFalse"));
  }

  
  public void testSaveProvisioningLookupByStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest").assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.SUB, grouperProvisioningAttributeValue.getStemScope());
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveProvisioningLookupByStemId() {
      
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStemId(stem.getId()).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.SUB, grouperProvisioningAttributeValue.getStemScope());
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
      
  }

  public void testSaveProvisioningLookupByStemName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStemName(stem.getName()).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.SUB, grouperProvisioningAttributeValue.getStemScope());
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveProvisioningStemNotFound() {
    
    boolean exceptionThrown = false;
    try {
      ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
      provisionableStemSave.assignTargetName("ldapProvTest")
          .assignMetadataString("md_testInput", "testValue").assignStemName("non_existing_stem").save();
      fail();
    } catch (Exception e) {
      exceptionThrown = true;
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveProvisioningRunAsRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStem(stem)
        .assignRunAsRoot(true)
        .save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.SUB, grouperProvisioningAttributeValue.getStemScope());
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
  }
  
  public void testSaveProvisioningSubjectIsNotWheelOrRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    
    try {
      ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
      provisionableStemSave.assignTargetName("ldapProvTest")
          .assignMetadataString("md_testInput", "testValue").assignStem(stem)
          .save();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("is not wheel or root"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveProvisioningInvalidTarget() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    
    try {
      ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
      provisionableStemSave.assignTargetName("invalid_target")
          .assignMetadataString("md_testInput", "testValue").assignStem(stem)
          .save();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("must be one of the valid targets"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveProvisioningTargetNotAssigned() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    
    try {
      ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
      provisionableStemSave
          .assignMetadataString("md_testInput", "testValue").assignStem(stem)
          .save();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("target is required"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testDeleteProvisioning() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest").assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.SUB, grouperProvisioningAttributeValue.getStemScope());
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
    provisionableStemSave = new ProvisionableStemSave();
    grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignSaveMode(SaveMode.DELETE).assignStem(stem).save();
    
    Assert.assertEquals(SaveResultType.DELETE, provisionableStemSave.getSaveResultType());
    Assert.assertNull(grouperProvisioningAttributeValue);
    
  }
  
  public void testSaveProvisioningUpdateSettings() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignProvision(false)
        .assignStemScope(Scope.ONE)
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableStemSave.getSaveResultType());
    Assert.assertNull(grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.ONE, grouperProvisioningAttributeValue.getStemScope());
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
    provisionableStemSave = new ProvisionableStemSave();
    grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignProvision(false)
        .assignStemScope(Scope.ONE)
        .assignMetadataString("md_testInput", "testValue1").assignStem(stem).save();
    
    Assert.assertNull(grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals(SaveResultType.UPDATE, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.ONE, grouperProvisioningAttributeValue.getStemScope());
    
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue1", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
  }
  
  public void testSaveProvisioningNoChange() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignProvision(false)
        .assignStemScope(Scope.ONE)
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableStemSave.getSaveResultType());
    Assert.assertNull(grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.ONE, grouperProvisioningAttributeValue.getStemScope());
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
    provisionableStemSave = new ProvisionableStemSave();
    grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignProvision(false)
        .assignStemScope(Scope.ONE)
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    Assert.assertNull(grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals(SaveResultType.NO_CHANGE, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.ONE, grouperProvisioningAttributeValue.getStemScope());
    
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
  }
  
  public void testSaveProvisioningReplaceAllSettingsFalse() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignProvision(false)
        .assignStemScope(Scope.ONE)
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableStemSave.getSaveResultType());
    Assert.assertNull(grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.ONE, grouperProvisioningAttributeValue.getStemScope());
   
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
    provisionableStemSave = new ProvisionableStemSave();
    grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignPolicyGroupOnly(true)
        .assignReplaceAllSettings(false)
        .assignStem(stem).save();
    
    Assert.assertNull(grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals(SaveResultType.UPDATE, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.ONE, grouperProvisioningAttributeValue.getStemScope());
    
    Assert.assertEquals(2, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    Assert.assertEquals(true, grouperProvisioningAttributeValue.getMetadataNameValues().get("md_grouper_allowPolicyGroupOverride"));
    
  }
  
  
  public void testSaveProvisioningReplaceAllSettingsFalseRemoveMetadata() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_grouper_allowPolicyGroupOverride", "true")
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    Assert.assertEquals(SaveResultType.INSERT, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.SUB, grouperProvisioningAttributeValue.getStemScope());
   
    Assert.assertEquals(2, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    Assert.assertEquals(true, grouperProvisioningAttributeValue.getMetadataNameValues().get("md_grouper_allowPolicyGroupOverride"));
    
    Assert.assertTrue(grouperProvisioningAttributeValue.isDirectAssignment());
    
    provisionableStemSave = new ProvisionableStemSave();
    grouperProvisioningAttributeValue = provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataBoolean("md_grouper_allowPolicyGroupOverride", null)
        .assignReplaceAllSettings(false)
        .assignStem(stem).save();
    
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getDoProvision());
    Assert.assertEquals(SaveResultType.UPDATE, provisionableStemSave.getSaveResultType());
    Assert.assertEquals("ldapProvTest", grouperProvisioningAttributeValue.getTargetName());
    Assert.assertEquals(Scope.SUB, grouperProvisioningAttributeValue.getStemScope());
    
    Assert.assertEquals(1, grouperProvisioningAttributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", grouperProvisioningAttributeValue.getMetadataNameValues().get("md_testInput"));
    
  }
  
  public void testSaveProvisioningRequiredMetadataFieldNotAssigned() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    boolean exceptionThrown = false;
    try {
      ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
      provisionableStemSave.assignTargetName("ldapProvTest")
          .assignStem(stem).save();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("md_testInput is a required field"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testSaveProvisioningInvalidMetadataFieldAssigned() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    boolean exceptionThrown = false;
    try {
      ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
      provisionableStemSave.assignTargetName("ldapProvTest")
      .assignMetadataString("md_testInput", "testValue")
      .assignMetadataString("md_testInput1", "testValue")
      .assignStem(stem).save();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("md_testInput1"));
    }
    
    Assert.assertTrue(exceptionThrown);
    
  }
  
}
