package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.util.Set;

import org.junit.Assert;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import org.apache.commons.lang3.BooleanUtils;
import junit.textui.TestRunner;

public class ProvisionableGroupFinderTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    setupProvisioningConfig();
    this.grouperSession = GrouperSession.startRootSession();
  }
  
  private GrouperSession grouperSession = null;

  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperSession.stopQuietly(this.grouperSession);
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
  public ProvisionableGroupFinderTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new ProvisionableGroupFinderTest("testFindProvisionableGroupSubjectDoesNotHaveProperPermissions"));
  }

  public void testFindProvisionableGroupLookupByGroup() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
    
    ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableGroupFinder.assignGroup(group).assignTargetName("ldapProvTest")
        .findProvisionableGroupAttributeValue();
    
    Assert.assertEquals("ldapProvTest", attributeValue.getTargetName());
    Assert.assertEquals("ldapProvTest", attributeValue.getDoProvision());
    Assert.assertEquals(1, attributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", attributeValue.getMetadataNameValues().get("md_testInput"));
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindProvisionableGroupLookupByGroupId() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
    
    ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableGroupFinder.assignGroupId(group.getId()).assignTargetName("ldapProvTest")
        .findProvisionableGroupAttributeValue();
    
    Assert.assertEquals("ldapProvTest", attributeValue.getTargetName());
    Assert.assertEquals("ldapProvTest", attributeValue.getDoProvision());
    
    Assert.assertEquals(1, attributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", attributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindProvisionableGroupLookupByGroupName() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
    
    ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableGroupFinder.assignGroupName(group.getName()).assignTargetName("ldapProvTest")
        .findProvisionableGroupAttributeValue();
    
    Assert.assertEquals("ldapProvTest", attributeValue.getTargetName());
    Assert.assertEquals("ldapProvTest", attributeValue.getDoProvision());
    Assert.assertEquals(1, attributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", attributeValue.getMetadataNameValues().get("md_testInput"));
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindProvisionableGroupReturnsNull() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableGroupFinder.assignGroupName(group.getName()).assignTargetName("ldapProvTest")
        .findProvisionableGroupAttributeValue();
    
    Assert.assertNull(attributeValue);
    
  }
  
  public void testFindProvisionableGroupReturnsMultiple() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.configureMetadata", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.numberOfMetadata", "1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.formElementType", "text");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.name", "md_testInput");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.showForGroup", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.showForFolder", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.required", "true");
    
    ProvisionableGroupSave provisionableGroupSave1 = new ProvisionableGroupSave();
    provisionableGroupSave1.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
    
    ProvisionableGroupSave provisionableGroupSave2 = new ProvisionableGroupSave();
    provisionableGroupSave2.assignTargetName("ldapProvTest1")
        .assignMetadataString("md_testInput", "testValue1").assignGroup(group).save();
    
    ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
    Set<GrouperProvisioningAttributeValue> provisionableStemAttributeValues = provisionableGroupFinder.assignGroupName(group.getName()).findProvisionableGroupAttributeValues();
    
    Assert.assertEquals(2, provisionableStemAttributeValues.size());
    
  }
  
  public void testFindProvisionableGroupDirectOptions() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.class", LdapSync.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.configureMetadata", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.ldapExternalSystemConfigId", "personLdap");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.numberOfMetadata", "1");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.formElementType", "text");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.name", "md_testInput");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.showForGroup", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.showForFolder", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.ldapProvTest1.metadata.0.required", "true");
    
    
    AttributeAssign attributeAssign = group.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(false));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "ldapProvTest1");
    
    attributeAssign.saveOrUpdate();
    
    ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
    Set<GrouperProvisioningAttributeValue> provisionableStemAttributeValues = provisionableGroupFinder.assignGroupName(group.getName())
        .assignDirectAssignment(true)
        .findProvisionableGroupAttributeValues();
    
    Assert.assertEquals(1, provisionableStemAttributeValues.size());
    
    Assert.assertTrue(provisionableStemAttributeValues.iterator().next().isDirectAssignment());
    
    GrouperProvisioningSettings.clearTargetsCache();
    
    provisionableGroupFinder = new ProvisionableGroupFinder();
    provisionableStemAttributeValues = provisionableGroupFinder.assignGroupName(group.getName())
        .assignDirectAssignment(null)
        .findProvisionableGroupAttributeValues();
    
    Assert.assertEquals(2, provisionableStemAttributeValues.size());
    
    provisionableGroupFinder = new ProvisionableGroupFinder();
    provisionableStemAttributeValues = provisionableGroupFinder.assignGroupName(group.getName())
        .assignDirectAssignment(false)
        .findProvisionableGroupAttributeValues();
    
    Assert.assertEquals(1, provisionableStemAttributeValues.size());
    Assert.assertFalse(provisionableStemAttributeValues.iterator().next().isDirectAssignment());
  }
  
  
  public void testFindProvisionableGroupInvalidTargetName() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    boolean exceptionThrown = false;
    try {
      ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
      provisionableGroupFinder.assignGroupName(group.getName())
          .assignTargetName("invalid_target_naem")
          .findProvisionableGroupAttributeValues();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("must be one of the valid targets"));
    }

    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testFindProvisionableGroupRunAsRoot() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableGroupFinder.assignGroupId(group.getId()).assignTargetName("ldapProvTest")
        .assignRunAsRoot(true)
        .findProvisionableGroupAttributeValue();
    
    Assert.assertEquals("ldapProvTest", attributeValue.getTargetName());
    Assert.assertEquals("ldapProvTest", attributeValue.getDoProvision());
    
    Assert.assertEquals(1, attributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", attributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindProvisionableGroupSubjectDoesNotHaveProperPermissions() {
    
    Group group = new GroupSave().assignName("test:test-group").assignCreateParentStemsIfNotExist(true).save();
    
    ProvisionableGroupSave provisionableGroupSave = new ProvisionableGroupSave();
    provisionableGroupSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignGroup(group).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    try {
      ProvisionableGroupFinder provisionableGroupFinder = new ProvisionableGroupFinder();
      provisionableGroupFinder.assignGroupId(group.getId()).assignTargetName("ldapProvTest")
          .findProvisionableGroupAttributeValue();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
    }
    Assert.assertTrue(exceptionThrown);
    
  }
  

}
