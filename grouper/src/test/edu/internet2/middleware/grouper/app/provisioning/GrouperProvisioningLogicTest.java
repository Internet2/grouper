package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlMembershipProvisioner;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouperClient.collections.MultiKey;

public class GrouperProvisioningLogicTest extends GrouperTest {
  
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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlMembershipProvisioner.class.getName());
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
    
    grouperProvisioner.setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
    
    grouperProvisioner.retrieveProvisioningConfiguration().configureProvisioner();
    
    GrouperProvisioningTranslatorBase translator = new GrouperProvisioningTranslatorBase();
    translator.setGrouperProvisioner(grouperProvisioner);
    GrouperProvisioningData grouperProvisioningData = new GrouperProvisioningData();
    grouperProvisioner.setGrouperProvisioningData(grouperProvisioningData);
    
    GrouperProvisioningLists grouperProvisioningObjects = grouperProvisioningData.getGrouperProvisioningObjects();
    
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
    
    assertEquals(1, grouperProvisioningData.getGrouperTargetObjects().getProvisioningGroups().size());
    ProvisioningGroup targetGroup = grouperProvisioningData.getGrouperTargetObjects().getProvisioningGroups().get(0);
    assertEquals("testName", targetGroup.getId());
    
    assertEquals(1, grouperProvisioningData.getGrouperTargetObjects().getProvisioningEntities().size());
    ProvisioningEntity targetEntity = grouperProvisioningData.getGrouperTargetObjects().getProvisioningEntities().get(0);
    assertEquals("testSubjectId", targetEntity.getId());
    
    assertEquals(1, grouperProvisioningData.getGrouperTargetObjects().getProvisioningMemberships().size());
    ProvisioningMembership targetMembership = grouperProvisioningData.getGrouperTargetObjects().getProvisioningMemberships().get(0);
    assertEquals("testName", targetMembership.getAttributes().get("group_name").getValue().toString());
    assertEquals("testSubjectId", targetMembership.getAttributes().get("subject_id").getValue().toString());
  }
  
  public void testTargetIdGrouperObjects() {
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.class", SqlMembershipProvisioner.class.getName());
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.dbExternalSystemConfigId", "grouper");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.subjectSourcesToProvision", "jdbc");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetGroupIdExpression", 
        "${targetGroup.retrieveAttributeValueString('groupName')}");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetMembershipIdExpression", 
        "new('edu.internet2.middleware.grouperClient.collections.MultiKey', targetMembership.retrieveAttributeValueString('groupName'), "
        + "targetMembership.retrieveAttributeValueString('subjectId'))");
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.targetEntityIdExpression", 
        "${targetEntity.retrieveAttributeValueString('subjectId')}");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipTableName", "testgrouper_prov_mship0");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipAttributeNames", "group_name, subject_id");
 
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    grouperProvisioner.setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
    
    grouperProvisioner.retrieveProvisioningConfiguration().configureProvisioner();
    
    GrouperProvisioningTranslatorBase translator = new GrouperProvisioningTranslatorBase();
    translator.setGrouperProvisioner(grouperProvisioner);
    GrouperProvisioningData grouperProvisioningData = new GrouperProvisioningData();
    grouperProvisioner.setGrouperProvisioningData(grouperProvisioningData);
    
    GrouperProvisioningLists grouperTargetObjects = grouperProvisioningData.getGrouperTargetObjects();
    
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
    translator.targetIdGrouperObjects();
    
    // then
    assertEquals(1, grouperProvisioningData.getGrouperTargetObjects().getProvisioningGroups().size());
    ProvisioningGroup targetGroup = grouperProvisioningData.getGrouperTargetObjects().getProvisioningGroups().get(0);
    assertEquals("testName", targetGroup.getTargetId());
    
    assertEquals(1, grouperProvisioningData.getGrouperTargetObjects().getProvisioningEntities().size());
    ProvisioningEntity targetEntity = grouperProvisioningData.getGrouperTargetObjects().getProvisioningEntities().get(0);
    assertEquals("testSubjectId", targetEntity.getTargetId());
    
    assertEquals(1, grouperProvisioningData.getGrouperTargetObjects().getProvisioningMemberships().size());
    ProvisioningMembership targetMembership = grouperProvisioningData.getGrouperTargetObjects().getProvisioningMemberships().get(0);
    assertEquals("testName", ((MultiKey)targetMembership.getTargetId()).getKey(0));
    assertEquals("testSubjectId", ((MultiKey)targetMembership.getTargetId()).getKey(1));
    
  }
  

}
