package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.util.Arrays;
import java.util.List;

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

public class GrouperProvisioningConfigurationTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }
  
  public void testGetProvisioningAttributeValue() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    
    //When
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningConfiguration.getProvisioningAttributeValue(stem0, "ldap");
    
    //Then
    assertTrue(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTarget());
    
  }
  
  public void testGetProvisioningAttributeValues() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    saveProvisioningAttributeMetadata(stem0, true, "box");
    
    //When
    List<GrouperProvisioningAttributeValue> attributeValues = GrouperProvisioningConfiguration.getProvisioningAttributeValues(stem0);
    
    //Then
    assertEquals(attributeValues.size(), 2);
    String targetName1 = attributeValues.get(0).getTarget();
    String targetName2 = attributeValues.get(1).getTarget();
    
    List<String> validNames = Arrays.asList("ldap", "box");
    
    assertTrue(validNames.contains(targetName1));
    assertTrue(validNames.contains(targetName2));
    
  }
  
  public void testSaveOrUpdateProvisioningAttributes() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setTarget("ldap");
    
    //When
    GrouperProvisioningConfiguration.saveOrUpdateProvisioningAttributes(attributeValue, stem0);
    
    //Then
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningConfiguration.getProvisioningAttributeValue(stemTest1, "ldap");
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTarget());
  }
  
  public void testCopyConfigFromParent() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    
    //When
    GrouperProvisioningConfiguration.copyConfigFromParent(stemTest1);
    
    //Then
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningConfiguration.getProvisioningAttributeValue(stemTest1, "ldap");
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTarget());
    
  }
  
  
  private static void saveProvisioningAttributeMetadata(Stem stem, boolean isDirect, String targetName) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(isDirect));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), targetName);
    
    attributeAssign.saveOrUpdate();
    
  }

}
