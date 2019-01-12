package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;

public class GrouperProvisioningJobTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }
  
  
  public void testUpdateMetadata() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stem1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test1").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stemTest2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2").save();
    Stem stemTest11a = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1a").save();
    Stem stemTest22a = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2:test2a").save();
    Group groupDirectAssigned = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2:test2a:group0").save();
    
    saveProvisioningAttributeMetadata(stem0, true);
    saveProvisioningAttributeMetadata(stem1, false);
    
    //When
    GrouperProvisioningJob.updateMetadataOnDirectStemsChildren();
    GrouperProvisioningJob.updateMetadataOnIndirectGrouperObjects();
    
    //Then - All the children of stem0 should have metadata coming from parent
    GrouperProvisioningAttributeValue grouperProvisioningAttributeValue = GrouperProvisioningConfiguration.getProvisioningAttributeValue(stemTest1, "ldap");
    assertEquals(stem0.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    
    grouperProvisioningAttributeValue = GrouperProvisioningConfiguration.getProvisioningAttributeValue(stemTest2, "ldap");
    assertEquals(stem0.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    
    grouperProvisioningAttributeValue = GrouperProvisioningConfiguration.getProvisioningAttributeValue(stemTest11a, "ldap");
    assertEquals(stem0.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    
    grouperProvisioningAttributeValue = GrouperProvisioningConfiguration.getProvisioningAttributeValue(stemTest22a, "ldap");
    assertEquals(stem0.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    
    grouperProvisioningAttributeValue = GrouperProvisioningConfiguration.getProvisioningAttributeValue(groupDirectAssigned, "ldap");
    assertEquals(stem0.getUuid(), grouperProvisioningAttributeValue.getOwnerStemId());
    assertFalse(grouperProvisioningAttributeValue.isDirectAssignment());
    
    //stem1 shouldn't have anything assigned
    grouperProvisioningAttributeValue = GrouperProvisioningConfiguration.getProvisioningAttributeValue(stem1, "ldap");
    assertNull(grouperProvisioningAttributeValue);
  }
  
  private static void saveProvisioningAttributeMetadata(Stem stem, boolean isDirect) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(isDirect));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), "ldap");
    
    attributeAssign.saveOrUpdate();
    
  }

}
