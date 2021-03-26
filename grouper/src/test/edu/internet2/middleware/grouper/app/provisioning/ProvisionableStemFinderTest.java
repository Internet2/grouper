package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.util.Set;

import org.junit.Assert;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;
import junit.textui.TestRunner;

public class ProvisionableStemFinderTest extends GrouperTest {

  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
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
  public ProvisionableStemFinderTest(String name) {
    super(name);
  }

  /**
   * Method main.
   * @param args String[]
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    TestRunner.run(new ProvisionableStemFinderTest("testFindProvisionableStemDirectOptions"));
  }

  public void testFindProvisionableStemLookupByStem() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableStemFinder.assignStem(stem).assignTargetName("ldapProvTest")
        .findProvisionableStemAttributeValue();
    
    Assert.assertEquals("ldapProvTest", attributeValue.getTargetName());
    Assert.assertEquals("ldapProvTest", attributeValue.getDoProvision());
    Assert.assertEquals(Scope.SUB, attributeValue.getStemScope());
    
    
    Assert.assertEquals(1, attributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", attributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindProvisionableStemLookupByStemId() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableStemFinder.assignStemId(stem.getId()).assignTargetName("ldapProvTest")
        .findProvisionableStemAttributeValue();
    
    Assert.assertEquals("ldapProvTest", attributeValue.getTargetName());
    Assert.assertEquals("ldapProvTest", attributeValue.getDoProvision());
    Assert.assertEquals(Scope.SUB, attributeValue.getStemScope());
    
    
    Assert.assertEquals(1, attributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", attributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindProvisionableStemLookupByStemName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableStemFinder.assignStemName(stem.getName()).assignTargetName("ldapProvTest")
        .findProvisionableStemAttributeValue();
    
    Assert.assertEquals("ldapProvTest", attributeValue.getTargetName());
    Assert.assertEquals("ldapProvTest", attributeValue.getDoProvision());
    Assert.assertEquals(Scope.SUB, attributeValue.getStemScope());
    
    
    Assert.assertEquals(1, attributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", attributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindProvisionableStemReturnsNull() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableStemFinder.assignStemName(stem.getName()).assignTargetName("ldapProvTest")
        .findProvisionableStemAttributeValue();
    
    Assert.assertNull(attributeValue);
    
  }
  
  public void testFindProvisionableStemReturnsMultiple() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
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
    
    ProvisionableStemSave provisionableStemSave1 = new ProvisionableStemSave();
    provisionableStemSave1.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    ProvisionableStemSave provisionableStemSave2 = new ProvisionableStemSave();
    provisionableStemSave2.assignTargetName("ldapProvTest1")
        .assignMetadataString("md_testInput", "testValue1").assignStem(stem).save();
    
    ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder();
    Set<GrouperProvisioningAttributeValue> provisionableStemAttributeValues = provisionableStemFinder.assignStemName(stem.getName()).findProvisionableStemAttributeValues();
    
    Assert.assertEquals(2, provisionableStemAttributeValues.size());
    
  }
  
  public void testFindProvisionableStemDirectOptions() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
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
    
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(false));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "ldapProvTest1");
    
    attributeAssign.saveOrUpdate();
    
    ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder();
    Set<GrouperProvisioningAttributeValue> provisionableStemAttributeValues = provisionableStemFinder.assignStemName(stem.getName())
        .assignDirectAssignment(true)
        .findProvisionableStemAttributeValues();
    
    Assert.assertEquals(1, provisionableStemAttributeValues.size());
    
    Assert.assertTrue(provisionableStemAttributeValues.iterator().next().isDirectAssignment());
    
    GrouperProvisioningSettings.clearTargetsCache();
    
    provisionableStemFinder = new ProvisionableStemFinder();
    provisionableStemAttributeValues = provisionableStemFinder.assignStemName(stem.getName())
        .assignDirectAssignment(null)
        .findProvisionableStemAttributeValues();
    
    Assert.assertEquals(2, provisionableStemAttributeValues.size());
    
    provisionableStemFinder = new ProvisionableStemFinder();
    provisionableStemAttributeValues = provisionableStemFinder.assignStemName(stem.getName())
        .assignDirectAssignment(false)
        .findProvisionableStemAttributeValues();
    
    Assert.assertEquals(1, provisionableStemAttributeValues.size());
    Assert.assertFalse(provisionableStemAttributeValues.iterator().next().isDirectAssignment());
  }
  
  
  public void testFindProvisionableStemInvalidTargetName() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    boolean exceptionThrown = false;
    try {
      ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder();
      provisionableStemFinder.assignStemName(stem.getName())
          .assignTargetName("invalid_target_naem")
          .findProvisionableStemAttributeValues();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
      Assert.assertTrue(e.getMessage().contains("must be one of the valid targets"));
    }

    Assert.assertTrue(exceptionThrown);
    
  }
  
  public void testFindProvisionableStemRunAsRoot() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder();
    GrouperProvisioningAttributeValue attributeValue = provisionableStemFinder.assignStemId(stem.getId()).assignTargetName("ldapProvTest")
        .assignRunAsRoot(true)
        .findProvisionableStemAttributeValue();
    
    Assert.assertEquals("ldapProvTest", attributeValue.getTargetName());
    Assert.assertEquals("ldapProvTest", attributeValue.getDoProvision());
    Assert.assertEquals(Scope.SUB, attributeValue.getStemScope());
    
    Assert.assertEquals(1, attributeValue.getMetadataNameValues().size());
    Assert.assertEquals("testValue", attributeValue.getMetadataNameValues().get("md_testInput"));
    
    Assert.assertTrue(attributeValue.isDirectAssignment());
    
  }
  
  public void testFindProvisionableStemSubjectDoesNotHaveProperPermissions() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    ProvisionableStemSave provisionableStemSave = new ProvisionableStemSave();
    provisionableStemSave.assignTargetName("ldapProvTest")
        .assignMetadataString("md_testInput", "testValue").assignStem(stem).save();
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    boolean exceptionThrown = false;
    try {
      ProvisionableStemFinder provisionableStemFinder = new ProvisionableStemFinder();
      provisionableStemFinder.assignStemId(stem.getId()).assignTargetName("ldapProvTest")
          .findProvisionableStemAttributeValue();
      fail();
    } catch(Exception e) {
      exceptionThrown = true;
    }
    Assert.assertTrue(exceptionThrown);
    
  }
  
  
}
