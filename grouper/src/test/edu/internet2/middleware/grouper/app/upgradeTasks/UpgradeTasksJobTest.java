package edu.internet2.middleware.grouper.app.upgradeTasks;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTempToEntity;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.exception.GroupSetNotFoundException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import junit.textui.TestRunner;

/**
 * 
 */
public class UpgradeTasksJobTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UpgradeTasksJobTest("test_v8_provisioningLdapDnAttributeChange"));
  }
  
  /**
   * @param name
   */
  public UpgradeTasksJobTest(String name) {
    super(name);
  }
  
  
  public static void v8configure() {
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.class").value(SqlProvisioner.class.getName()).store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.dbExternalSystemConfigId").value("grouper").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.subjectSourcesToProvision").value("jdbc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.logAllObjectsVerbose").value("true").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.provisionerName").value("One prod LDAP flat").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.provisioningType").value("groupAttributes").store();

    //TODO make an attribute config for this
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.hasTargetEntityLink").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.numberOfGroupAttributes").value("6").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.name").value("cn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.multiValued").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.storageType").value("separateAttributesTable").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.translateFromGrouperProvisioningGroupField").value("name").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.name").value("dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.valueType").value("string").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.multiValued").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.translateExpression").value("${'cn=' + grouperProvisioningGroup.getName() + ',OU=Grouper,OU=365Groups,DC=one,DC=upenn,DC=edu'}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.translateGrouperToGroupSyncField").value("groupToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.1.storageType").value("separateAttributesTable").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.name").value("gidNumber").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.valueType").value("long").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.multiValued").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.translateFromGrouperProvisioningGroupField").value("idIndex").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.2.storageType").value("separateAttributesTable").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.name").value("objectClass").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.translateExpressionType").value("translationScript").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.translateExpression").value("${grouperUtil.toSet('group')}").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.3.storageType").value("separateAttributesTable").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.name").value("member").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.update").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.delete").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.membershipAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.multiValued").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.translateFromMemberSyncField").value("memberToId2").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.4.storageType").value("separateAttributesTable").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.name").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.insert").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.translateExpressionType").value("grouperProvisioningGroupField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.translateFromGrouperProvisioningGroupField").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.5.storageType").value("groupTableColumn").store();
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttributeCount").value("3").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.name").value("dn").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.storageType").value("separateAttributesTable").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.0.translateToMemberSyncField").value("memberToId2").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.name").value("employeeID").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.searchAttribute").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.matchingId").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.translateExpressionType").value("grouperProvisioningEntityField").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.translateFromGrouperProvisioningEntityField").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.1.storageType").value("separateAttributesTable").store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.2.name").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.2.select").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetEntityAttribute.2.storageType").value("entityTableColumn").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.operateOnGrouperGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.operateOnGrouperMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.operateOnGrouperEntities").value("true").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.insertEntities").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteEntities").value("false").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.insertGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.updateGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteGroupsNotInGrouper").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.selectGroups").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.selectEntities").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.selectMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.insertMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteMemberships").value("true").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteMembershipsIfNotExistInGrouper").value("true").store();
//      new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.deleteGroupsDeletedGrouper").value("true").store();
    
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupTableName").value("testgrouper_prov_ldap_group").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupTableIdColumn").value("uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupAttributesTableName").value("testgrouper_pro_ldap_group_attr").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupAttributesGroupForeignKeyColumn").value("group_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupAttributesAttributeNameColumn").value("attribute_name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.groupAttributesAttributeValueColumn").value("attribute_value").store();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.userTableName").value("testgrouper_prov_ldap_entity").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.userPrimaryKey").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.entityAttributesTableName").value("testgrouper_pro_dap_entity_attr").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.entityAttributesEntityForeignKeyColumn").value("entity_uuid").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.entityAttributesAttributeNameColumn").value("entity_attribute_name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.entityAttributesAttributeValueColumn").value("entity_attribute_value").store();


  }
    
  
  public void test_v8_provisioningFieldNameToAttributeChange() {

    // GRP-3927: There is no provisioning concept of field anymore, only attribute
    
    v8configure();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.name").delete();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.fieldName").value("abc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.isFieldElseAttribute").value("true").store();
    ConfigPropertiesCascadeBase.clearCache();

    assertTrue(UpgradeTasks.v8_provisioningFieldNameToAttributeChange());

    assertEquals("abc", GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.targetGroupAttribute.0.name"));
    assertTrue(StringUtils.isBlank(GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.targetGroupAttribute.0.fieldName")));
    assertTrue(StringUtils.isBlank(GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.targetGroupAttribute.0.isFieldElseAttribute")));

    assertFalse(UpgradeTasks.v8_provisioningFieldNameToAttributeChange());
    
  }
  
  /**
   */
  public void test_v8_provisioningLdapDnAttributeChange() {

    // GRP-3931: change ldap DN from field name to attribute ldap_dn
    
    v8configure();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.class").value(LdapSync.class.getName()).store();

    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.name").delete();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.fieldName").value("name").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.targetGroupAttribute.0.isFieldElseAttribute").value("true").store();
    ConfigPropertiesCascadeBase.clearCache();

    assertTrue(UpgradeTasks.v8_provisioningLdapDnAttributeChange());

    assertEquals("ldap_dn", GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.targetGroupAttribute.0.name"));
    assertTrue(StringUtils.isBlank(GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.targetGroupAttribute.0.fieldName")));
    assertEquals("false", GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.targetGroupAttribute.0.isFieldElseAttribute"));

    assertFalse(UpgradeTasks.v8_provisioningLdapDnAttributeChange());

  }

  /**
   * 
   */
  public void test_v8_provisioningEntityResolverRefactor() {
    
    // FROM provisioner.genericProvisioner.entityAttributesNotInSubjectSource
    // TO provisioner.genericProvisioner.entityResolver.entityAttributesNotInSubjectSource
    
    // GRP-3939: Refactor entity attribute resolver config
    
    v8configure();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.filterPart").value("abc").store();
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.globalLDAPResolver").value("def").store();
    ConfigPropertiesCascadeBase.clearCache();

    assertTrue(UpgradeTasks.v8_provisioningEntityResolverRefactor());

    assertEquals(GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.entityResolver.filterPart"), "abc");
    assertTrue(StringUtils.isBlank(GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.filterPart")));

    assertEquals(GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.entityResolver.globalLDAPResolver"), "def");
    assertTrue(StringUtils.isBlank(GrouperLoaderConfig.retrieveConfig().propertyValueString("provisioner.pspng_oneprod.globalLDAPResolver")));

    assertFalse(UpgradeTasks.v8_provisioningEntityResolverRefactor());
  }
  
  
  /**
   * 
   */
  public void test_v8_provisioningSubjectSourcesInEntity() {
    
    v8configure();
    
    new GrouperDbConfig().configFileName("grouper-loader.properties").propertyName("provisioner.pspng_oneprod.operateOnGrouperEntities").value("false").store();
    ConfigPropertiesCascadeBase.clearCache();

    assertTrue(UpgradeTasks.v8_provisioningSubjectSourcesInEntity());

    assertTrue(GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("provisioner.pspng_oneprod.operateOnGrouperEntities", false));

    assertFalse(UpgradeTasks.v8_provisioningSubjectSourcesInEntity());
    
  }

  /**
   * 
   */
  public void testVersion1() {
    GrouperSession grouperSession = GrouperSession.startRootSession();

    Entity testEntity1 = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testEntity1").save();
    Entity testEntity2 = new EntitySave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:testEntity2").save();
        
    GroupSet gs1 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrReaders", true).getId());
    GroupSet gs2 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
    GroupSet gs3 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrReaders", true).getId());
    GroupSet gs4 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
    
    gs1.delete(false);
    gs2.delete(false);
    gs3.delete(false);
    gs4.delete(false);
    
    ChangeLogTempToEntity.convertRecords();
    
    try {
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrReaders", true).getId());
      fail("didn't throw exception");
    } catch (GroupSetNotFoundException e) {
      // good
    }

    try {
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
      fail("didn't throw exception");
    } catch (GroupSetNotFoundException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrReaders", true).getId());
      fail("didn't throw exception");
    } catch (GroupSetNotFoundException e) {
      // good
    }
    
    try {
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
      fail("didn't throw exception");
    } catch (GroupSetNotFoundException e) {
      // good
    }

    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs1.getId(), false).size(), 0);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs2.getId(), false).size(), 0);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs3.getId(), false).size(), 0);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs4.getId(), false).size(), 0);

    UpgradeTasksJob.runDaemonStandalone();
    
    gs1 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrReaders", true).getId());
    gs2 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity1.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
    gs3 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrReaders", true).getId());
    gs4 = GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(testEntity2.getId(), FieldFinder.find("groupAttrUpdaters", true).getId());
        
    assertNotNull(gs1);
    assertNotNull(gs2);
    assertNotNull(gs3);
    assertNotNull(gs4);

    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs1.getId(), false).size(), 1);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs2.getId(), false).size(), 1);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs3.getId(), false).size(), 1);
    assertEquals(GrouperDAOFactory.getFactory().getPITGroupSet().findBySourceId(gs4.getId(), false).size(), 1);
  }
}
