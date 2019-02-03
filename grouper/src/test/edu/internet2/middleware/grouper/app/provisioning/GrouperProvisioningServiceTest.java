package edu.internet2.middleware.grouper.app.provisioning;

import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_DIRECT_ASSIGNMENT;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_STEM_SCOPE;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.PROVISIONING_TARGET;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeNames.retrieveAttributeDefNameBase;
import static edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings.provisioningConfigStemName;

import java.util.Arrays;
import java.util.List;

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
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouperClientExt.org.apache.commons.lang3.BooleanUtils;

public class GrouperProvisioningServiceTest extends GrouperTest {
  
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    
    GrouperProvisioningTarget target1 = new GrouperProvisioningTarget("ldapKey", "ldap");
    GrouperProvisioningSettings.getTargets().put("ldap", target1);
    
    GrouperProvisioningTarget target2 = new GrouperProvisioningTarget("boxKey", "box");
    GrouperProvisioningSettings.getTargets().put("box", target2);
    
  }
  
  public void testGetProvisioningAttributeValue() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    
    //When
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(stem0, "ldap");
    
    //Then
    assertTrue(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    
  }
  
  public void testGetProvisioningAttributeValues() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    saveProvisioningAttributeMetadata(stem0, true, "box");
    
    //When
    List<GrouperProvisioningAttributeValue> attributeValues = GrouperProvisioningService.getProvisioningAttributeValues(stem0);
    
    //Then
    assertEquals(attributeValues.size(), 2);
    String targetName1 = attributeValues.get(0).getTargetName();
    String targetName2 = attributeValues.get(1).getTargetName();
    
    List<String> validNames = Arrays.asList("ldap", "box");
    
    assertTrue(validNames.contains(targetName1));
    assertTrue(validNames.contains(targetName2));
    
  }
  
  public void testSaveOrUpdateProvisioningAttributes() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    attributeValue.setDirectAssignment(true);
    attributeValue.setTargetName("ldap");
    
    //When
    GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem0);
    
    //Then
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(stemTest1, "ldap");
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
  }
  
  public void testCopyConfigFromParent() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    saveProvisioningAttributeMetadata(stem0, true, "ldap");
    
    //When
    GrouperProvisioningService.copyConfigFromParent(stemTest1);
    
    //Then
    GrouperProvisioningAttributeValue attributeValue1 = GrouperProvisioningService.getProvisioningAttributeValue(stemTest1, "ldap");
    assertFalse(attributeValue1.isDirectAssignment());
    assertEquals("ldap", attributeValue1.getTargetName());
    
  }
  
  public void testTargetNotEditableWhenReadOnlyIsTrue() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperProvisioningTarget target1 = new GrouperProvisioningTarget("ldapReadOnlyKey", "ldapReadOnly");
    target1.setReadOnly(true);
    GrouperProvisioningSettings.getTargets().put("ldapReadOnly", target1);
    
    saveProvisioningAttributeMetadata(stem0, true, "ldapReadOnly");
    
    Stem etc = new StemSave(grouperSession).assignStemNameToEdit("etc").assignName("etc").save();
    Group wheel = etc.addChildGroup("wheel","wheel");
    wheel.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    
    //When
    boolean isEditable = GrouperProvisioningService.isTargetEditable(target1, SubjectTestHelper.SUBJ0, stem0);
    
    // Then
    assertFalse(isEditable);
    
  }
  
  public void testTargetEditableWhenReadOnlyIsFalse() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    GrouperProvisioningTarget target1 = new GrouperProvisioningTarget("ldapEditableKey", "ldapEditable");
    target1.setReadOnly(false);
    GrouperProvisioningSettings.getTargets().put("ldapEditable", target1);
    
    saveProvisioningAttributeMetadata(stem0, true, "ldapEditable");
    
    Stem etc = new StemSave(grouperSession).assignStemNameToEdit("etc").assignName("etc").save();
    Group wheel = etc.addChildGroup("wheel","wheel");
    wheel.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    
    //When
    boolean isEditable = GrouperProvisioningService.isTargetEditable(target1, SubjectTestHelper.SUBJ0, stem0);
    
    // Then
    assertTrue(isEditable);
    
  }
  
  public void testTargetNotEditableWhenSubjectNotMemberOfGroup() {
    
    //Given
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    
    
    GrouperProvisioningTarget target1 = new GrouperProvisioningTarget("ldapTargetKey", "ldapTarget");
    target1.setGroupAllowedToAssign(group0.getName());
    GrouperProvisioningSettings.getTargets().put("ldapTarget", target1);
    
    saveProvisioningAttributeMetadata(stem0, true, "ldapTarget");
    
    //When
    boolean isEditable = GrouperProvisioningService.isTargetEditable(target1, SubjectTestHelper.SUBJ0, stem0);
    
    // Then
    assertFalse(isEditable);
    
  }
  
  
  private static void saveProvisioningAttributeMetadata(Stem stem, boolean isDirect, String targetName) {
    
    AttributeAssign attributeAssign = stem.getAttributeDelegate().addAttribute(retrieveAttributeDefNameBase()).getAttributeAssign();
    
    AttributeDefName attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_DIRECT_ASSIGNMENT, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), BooleanUtils.toStringTrueFalse(isDirect));
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_TARGET, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), targetName);
    
    attributeDefName = AttributeDefNameFinder.findByName(provisioningConfigStemName()+":"+PROVISIONING_STEM_SCOPE, true);
    attributeAssign.getAttributeValueDelegate().assignValue(attributeDefName.getName(), Stem.Scope.SUB.name());
    
    attributeAssign.saveOrUpdate();
    
  }

}
