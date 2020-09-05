package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlMembershipProvisioner;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;

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

    //#translate from group auto translated to the common format
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.script", "${grouperTargetGroup.setId(grouperProvisioningGroup.getName())}");
    // # could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.0.for", "group");
    //#translate from group auto translated to the common format
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.script", "${grouperTargetEntity.setId(grouperProvisioningEntity.retrieveAttributeValue(\"subjectId\"))}");
    //# could be group, membership, or entity
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.grouperToTargetTranslation.1.for", "entity");

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipTableName", "testgrouper_prov_mship0");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner.sqlProvTest.membershipAttributeNames", "group_name, subject_id");
    
    // # if provisioning in ui should be enabled
    //# {valueType: "boolean", required: true}
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("sqlProvTest");
    
    grouperProvisioner.setGrouperProvisioningType(GrouperProvisioningType.fullProvisionFull);
    
    grouperProvisioner.retrieveProvisioningConfiguration().configureProvisioner();
    
    //GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull); 

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
    grouperProvisioningGroup.assignAttribute("description", "testDescription");
    grouperProvisioningGroups.add(grouperProvisioningGroup);
    
    grouperProvisioningObjects.setProvisioningGroups(grouperProvisioningGroups);
    
    List<ProvisioningEntity> grouperProvisioningEntities = new ArrayList<ProvisioningEntity>();
    
    ProvisioningEntity grouperProvisioningEntity = new ProvisioningEntity();
    grouperProvisioningEntity.assignAttribute("subjectId", "testSubjectId");
    grouperProvisioningEntities.add(grouperProvisioningEntity);
    
    grouperProvisioningObjects.setProvisioningEntities(grouperProvisioningEntities);
    
    grouperProvisioner.retrieveGrouperDao().processWrappers();
    
    translator.translateGrouperToTarget();
    
    assertEquals(1, grouperProvisioningData.getGrouperTargetObjects().getProvisioningGroups().size());
    ProvisioningGroup grouperTargetGroup = grouperProvisioningData.getGrouperTargetObjects().getProvisioningGroups().get(0);
    assertEquals("testName", grouperTargetGroup.getId());
    
    assertEquals(1, grouperProvisioningData.getGrouperTargetObjects().getProvisioningEntities().size());
    ProvisioningEntity grouperTargetEntity = grouperProvisioningData.getGrouperTargetObjects().getProvisioningEntities().get(0);
    
    assertEquals("testSubjectId", grouperTargetEntity.getId());
  }
  

}
