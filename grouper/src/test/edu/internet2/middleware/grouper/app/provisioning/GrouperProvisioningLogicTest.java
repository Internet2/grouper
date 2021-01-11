package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioningType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import junit.textui.TestRunner;

public class GrouperProvisioningLogicTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String args[]) {
    TestRunner.run(new GrouperProvisioningLogicTest("testConfigurationValidationGroups"));
  }
  
  public GrouperProvisioningLogicTest() {
    super();
    // TODO Auto-generated constructor stub
  }

  public GrouperProvisioningLogicTest(String name) {
    super(name);
    // TODO Auto-generated constructor stub
  }

  /**
   * grouper session
   */
  private GrouperSession grouperSession = null;

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    
    try {

      this.grouperSession = GrouperSession.startRootSession();
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }
  
  public void testTranslateGrouperToTarget() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", "${grouperTargetGroup.setId(grouperProvisioningGroup.getName())}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "group");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", "${grouperTargetEntity.setId(grouperProvisioningEntity.retrieveAttributeValue(\"subjectId\"))}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "entity");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.script", 
        "${grouperTargetMembership.assignAttributeValue('group_name', grouperProvisioningMembership.getProvisioningGroup().getName())}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.2.for", "membership");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.3.script", 
        "${grouperTargetMembership.assignAttributeValue('subject_id', grouperProvisioningMembership.getProvisioningEntity().retrieveAttributeValueString('subjectId'))}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.3.for", "membership");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipTableName", "testgrouper_prov_mship0");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipAttributeNames", "group_name, subject_id");
 
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
    
    grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();
    
    GrouperProvisioningTranslatorBase translator = new GrouperProvisioningTranslatorBase();
    translator.setGrouperProvisioner(grouperProvisioner);
    GrouperProvisioningDataGrouper grouperProvisioningDataGrouper = grouperProvisioner.retrieveGrouperProvisioningDataGrouper();
    GrouperProvisioningDataGrouperTarget grouperProvisioningDataGrouperTarget = grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget();
    
    GrouperProvisioningLists grouperProvisioningObjects = grouperProvisioningDataGrouper.getGrouperProvisioningObjects();
    
    List<ProvisioningGroup> grouperProvisioningGroups = new ArrayList<ProvisioningGroup>();
    ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
    grouperProvisioningGroup.setId("testId");
    grouperProvisioningGroup.setName("testName");
    grouperProvisioningGroup.setDisplayName("testDisplayName");
    grouperProvisioningGroup.setIdIndex(Long.parseLong("2313122331"));
    grouperProvisioningGroup.assignAttributeValue("description", "testDescription");
    grouperProvisioningGroups.add(grouperProvisioningGroup);
    
    grouperProvisioningObjects.setProvisioningGroups(grouperProvisioningGroups);
    
    List<ProvisioningEntity> grouperProvisioningEntities = new ArrayList<ProvisioningEntity>();
    
    ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity();
    grouperProvisioningEntity.assignAttributeValue("subjectId", "testSubjectId");
    grouperProvisioningEntities.add(grouperProvisioningEntity);
    
    grouperProvisioningObjects.setProvisioningEntities(grouperProvisioningEntities);
    
    List<ProvisioningMembership> grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    ProvisioningMembership grouperProvisioningMembership = new ProvisioningMembership();
    grouperProvisioningMembership.setProvisioningGroup(grouperProvisioningGroup);
    grouperProvisioningMembership.setProvisioningEntity(grouperProvisioningEntity);
    
    grouperProvisioningMemberships.add(grouperProvisioningMembership);
    grouperProvisioningObjects.setProvisioningMemberships(grouperProvisioningMemberships);
    
    grouperProvisioner.retrieveGrouperDao().processWrappers();
    
    translator.translateGrouperToTarget();
    
    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningGroups().size());
    ProvisioningGroup targetGroup = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningGroups().get(0);
    assertEquals("testName", targetGroup.getId());
    
    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningEntities().size());
    ProvisioningEntity targetEntity = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningEntities().get(0);
    assertEquals("testSubjectId", targetEntity.getId());
    
    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningMemberships().size());
    ProvisioningMembership targetMembership = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningMemberships().get(0);
    assertEquals("testName", targetMembership.getAttributes().get("group_name").getValue().toString());
    assertEquals("testSubjectId", targetMembership.getAttributes().get("subject_id").getValue().toString());
  }
  
  public void testMatchingIdGrouperObjects() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.groupMatchingIdExpression", 
        "${targetGroup.retrieveAttributeValueString('groupName')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipMatchingIdExpression", 
        "new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('groupName'), "
        + "targetMembership.retrieveAttributeValueString('subjectId'))");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.entityMatchingIdExpression", 
        "${targetEntity.retrieveAttributeValueString('subjectId')}");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipTableName", "testgrouper_prov_mship0");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipAttributeNames", "group_name, subject_id");
 
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    grouperProvisioner.retrieveGrouperProvisioningBehavior().setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
    
    grouperProvisioner.retrieveGrouperProvisioningConfiguration().configureProvisioner();
    
    GrouperProvisioningTranslatorBase translator = new GrouperProvisioningTranslatorBase();
    translator.setGrouperProvisioner(grouperProvisioner);
    GrouperProvisioningDataGrouper grouperProvisioningDataGrouper = grouperProvisioner.retrieveGrouperProvisioningDataGrouper();
    GrouperProvisioningDataGrouperTarget grouperProvisioningDataGrouperTarget = grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget();
    
    GrouperProvisioningLists grouperTargetObjects = grouperProvisioner.retrieveGrouperProvisioningDataGrouperTarget().getGrouperTargetObjects();
    
    List<ProvisioningGroup> grouperProvisioningGroups = new ArrayList<ProvisioningGroup>();
    ProvisioningGroup grouperProvisioningGroup = new ProvisioningGroup();
    grouperProvisioningGroup.setId("testId");
    grouperProvisioningGroup.assignAttributeValue("groupName", "testName");
    
    grouperProvisioningGroups.add(grouperProvisioningGroup);
    
    grouperTargetObjects.setProvisioningGroups(grouperProvisioningGroups);
    
    List<ProvisioningEntity> grouperProvisioningEntities = new ArrayList<ProvisioningEntity>();
    
    ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity();
    grouperProvisioningEntity.assignAttributeValue("subjectId", "testSubjectId");
    grouperProvisioningEntities.add(grouperProvisioningEntity);
    
    grouperTargetObjects.setProvisioningEntities(grouperProvisioningEntities);
    
    List<ProvisioningMembership> grouperProvisioningMemberships = new ArrayList<ProvisioningMembership>();
    
    ProvisioningMembership grouperProvisioningMembership = new ProvisioningMembership();
    grouperProvisioningMembership.assignAttributeValue("groupName", "testName");
    grouperProvisioningMembership.assignAttributeValue("subjectId", "testSubjectId");
    
    grouperProvisioningMemberships.add(grouperProvisioningMembership);
    grouperTargetObjects.setProvisioningMemberships(grouperProvisioningMemberships);
    
    grouperProvisioner.retrieveGrouperDao().processWrappers();
    
    // when
    translator.matchingIdGrouperObjects();
    
    // then
    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningGroups().size());
    ProvisioningGroup targetGroup = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningGroups().get(0);
    assertEquals("testName", targetGroup.getMatchingId());
    
    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningEntities().size());
    ProvisioningEntity targetEntity = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningEntities().get(0);
    assertEquals("testSubjectId", targetEntity.getMatchingId());
    
    assertEquals(1, grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningMemberships().size());
    ProvisioningMembership targetMembership = grouperProvisioningDataGrouperTarget.getGrouperTargetObjects().getProvisioningMemberships().get(0);
    assertEquals("testName", ((MultiKey)targetMembership.getMatchingId()).getKey(0));
    assertEquals("testSubjectId", ((MultiKey)targetMembership.getMatchingId()).getKey(1));
    
  }

  public void testConfigurationValidationGroups() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.groupAttributes.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.sqlProvisioningType", SqlProvisioningType.sqlLikeLdapGroupMemberships.name());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.numberOfGroupAttributes", "4");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.update", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.matchingId", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.multiValued", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateExpression", "${'cn=' + javax.naming.ldap.Rdn.escapeValue(grouperProvisioningGroup.getName()) + ',ou=Groups,dc=example,dc=edu'}");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateFromGroupSyncField", "groupIdIndex");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.name", "description");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.insert", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.update", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.select", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.matchingId", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.multiValued", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.membershipAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.1.translateFromMemberSyncField", "subjectId");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroups", "true");
    // deleteGroupsIfNotExistInGrouper
    // deleteGroupsIfGrouperDeleted
    // deleteGroupsIfGrouperCreated
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    List<MultiKey> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneGroupDeleteType"), "deleteGroups")));
      
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroupsIfNotExistInGrouper", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneGroupDeleteType"), "deleteGroups")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteGroupsIfGrouperCreated", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneGroupDeleteType"), "deleteGroups")));

    // if there is entity link something else must be set
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.hasTargetEntityLink", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig"), "hasTargetEntityLink")));
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.translateToMemberSyncField", "memberFromId2");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig"), "hasTargetEntityLink")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.targetEntityAttribute.0.translateToMemberSyncField");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig"), "hasTargetEntityLink")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.common.entityLink.memberFromId2", "${targetEntity.name}");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig"), "hasTargetEntityLink")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityAttribute.0.translateToMemberSyncField", "memberFromId2");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig"), "hasTargetGroupLink")));
    assertTrue(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkMultipleToSameBucket"), "targetEntityAttribute.0.translateToMemberSyncField")));
    assertTrue(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkMultipleToSameBucket"), "common.entityLink.memberFromId2")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.common.entityLink.memberFromId2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.hasTargetEntityLink");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkNeedsConfig"), "hasTargetEntityLink")));
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkMultipleToSameBucket"), "targetEntityAttribute.0.translateToMemberSyncField")));
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetEntityLinkMultipleToSameBucket"), "common.entityLink.memberFromId2")));

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.isFieldElseAttribute", "false");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.valueType", "string");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();

    //provisioning.configuration.validation.multipleAttributesSameName = Error: two ${type} ${fieldType}s have the same name '${attributeName}'
    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    GrouperTextContainer.assignThreadLocalVariable("fieldType", "attribute");
    GrouperTextContainer.assignThreadLocalVariable("attributeName", "description");
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName"), "targetGroupAttribute.2.name"})));
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.attributeNameRequired"), "targetGroupAttribute.2.isFieldElseAttribute"})));
    GrouperTextContainer.resetThreadLocalVariableMap();

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.2.name", "description");

    
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    GrouperTextContainer.assignThreadLocalVariable("fieldType", "attribute");
    GrouperTextContainer.assignThreadLocalVariable("attributeName", "description");
    // provisioning.configuration.validation.attributeNameRequired = Error: ${type} ${fieldType} name is required
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName"), "targetGroupAttribute.2.name"})));
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.attributeNameRequired"), "targetGroupAttribute.2.isFieldElseAttribute"})));
    GrouperTextContainer.resetThreadLocalVariableMap();

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.targetGroupAttribute.2.isFieldElseAttribute");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.targetGroupAttribute.2.name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.targetGroupAttribute.2.valueType");

    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();

    GrouperTextContainer.assignThreadLocalVariable("type", "group");
    GrouperTextContainer.assignThreadLocalVariable("fieldType", "attribute");
    GrouperTextContainer.assignThreadLocalVariable("attributeName", "description");
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.multipleAttributesSameName"), "targetGroupAttribute.2.name"})));
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.attributeNameRequired"), "targetGroupAttribute.2.isFieldElseAttribute"})));
    GrouperTextContainer.resetThreadLocalVariableMap();

  }
  
  public void testConfigurationValidationMemberships() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.membershipObjects.name());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.sqlProvisioningType", SqlProvisioningType.membershipsTable.name());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperMemberships", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMemberships", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");

    // deleteGroupsIfNotExistInGrouper
    // deleteGroupsIfGrouperDeleted
    // deleteGroupsIfGrouperCreated
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    List<MultiKey> errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneMembershipDeleteType"), "deleteMemberships")));
      
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMembershipsIfNotExistInGrouper", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneMembershipDeleteType"), "deleteMemberships")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteMembershipsIfGrouperCreated", "true");
    errorsAndSuffixes = grouperProvisioner.retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneMembershipDeleteType"), "deleteMemberships")));

  }

  public void testConfigurationValidationEntities() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.provisioningType", GrouperProvisioningBehaviorMembershipType.entityAttributes.name());
    //GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.sqlProvisioningType", SqlProvisioningType.sqlLikeLdapUserAttributes.name());
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.operateOnGrouperMemberships", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipTableName", "membership_table");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteEntities", "true");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
  
    // deleteGroupsIfNotExistInGrouper
    // deleteGroupsIfGrouperDeleted
    // deleteGroupsIfGrouperCreated
    
    List<MultiKey> errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType"), "deleteEntities")));
      
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteEntitiesIfNotExistInGrouper", "true");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType"), "deleteEntities")));
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.deleteEntitiesIfGrouperCreated", "true");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType"), "deleteEntities")));
  
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.deleteEntitiesIfGrouperCreated");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.oneEntityDeleteType"), "deleteEntities")));
    
    // if there is group link something else must be set
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.hasTargetGroupLink", "true");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig"), "hasTargetGroupLink")));
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.fieldName", "name");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.isFieldElseAttribute", "true");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.valueType", "string");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateToGroupSyncField", "groupFromId2");

    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig"), "hasTargetGroupLink")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.targetGroupAttribute.0.translateToGroupSyncField");

    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig"), "hasTargetGroupLink")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.common.groupLink.groupFromId2", "${targetGroup.name}");

    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig"), "hasTargetGroupLink")));

    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupAttribute.0.translateToGroupSyncField", "groupFromId2");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig"), "hasTargetGroupLink")));
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkMultipleToSameBucket"), "targetGroupAttribute.0.translateToGroupSyncField")));
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkMultipleToSameBucket"), "common.groupLink.groupFromId2")));
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.common.groupLink.groupFromId2");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.hasTargetGroupLink");

    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkNeedsConfig"), "hasTargetGroupLink")));
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkMultipleToSameBucket"), "targetGroupAttribute.0.translateToGroupSyncField")));
    assertFalse(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("provisioning.configuration.validation.targetGroupLinkMultipleToSameBucket"), "common.groupLink.groupFromId2")));

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.abc123", "def456");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    GrouperTextContainer.assignThreadLocalVariable("extraneousConfig", "abc123");
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(new Object[] {GrouperTextContainer.textOrNull("provisioning.configuration.validation.extraneousConfigs")})));
    GrouperTextContainer.resetThreadLocalVariableMap();
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.abc123");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.hasTargetGroupLink", "true1");
    errorsAndSuffixes = GrouperProvisioner.retrieveProvisioner("sqlProvTest").retrieveGrouperProvisioningConfigurationValidation().validateFromConfig();
    GrouperTextContainer.assignThreadLocalVariable("configFieldLabel", "Has target group link");
    assertTrue(GrouperUtil.toStringForLog(errorsAndSuffixes, true), errorsAndSuffixes.contains(new MultiKey(GrouperTextContainer.textOrNull("grouperConfigurationValidationInvalidBoolean"), "#config_hasTargetGroupLink_spanid")));
    GrouperTextContainer.resetThreadLocalVariableMap();
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().remove("provisioner.sqlProvTest.hasTargetGroupLink");
    
    
  }
  

}
