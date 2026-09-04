package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoSendChangesToTargetRequest;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import junit.textui.TestRunner;

/**
 * test that provisioner.<configId>.crudOperationOrder controls the order that
 * GrouperProvisionerTargetDaoAdapter.sendChangesToTarget() sends operations to the target
 */
public class GrouperProvisioningCrudOperationOrderTest extends GrouperProvisioningBaseTest {

  public GrouperProvisioningCrudOperationOrderTest() {
    super();
  }

  public GrouperProvisioningCrudOperationOrderTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperProvisioningCrudOperationOrderTest("testInsertsFirst"));
  }

  @Override
  public String defaultConfigId() {
    return "testCrudOperationOrder";
  }

  @Override
  protected void setUp() {
    super.setUp();
    CrudOperationOrderTestDao.clearOperations();
  }

  private void configureProvisionerSuffix(String suffix, String value) {
    new GrouperDbConfig().configFileName("grouper-loader.properties")
      .propertyName("provisioner." + defaultConfigId() + "." + suffix).value(value).store();
  }

  /**
   * a generic provisioner pointed at the recording dao.  The dao only implements the individual
   * insert/update/delete operations, so the framework decides the order.
   * @param crudOperationOrder null to leave the config unset
   */
  private void configureProvisioner(String crudOperationOrder) {
    configureProvisionerSuffix("class", "edu.internet2.middleware.grouper.app.genericProvisioner.GrouperGenericProvisioner");
    configureProvisionerSuffix("genericProvisionerDaoClassName", CrudOperationOrderTestDao.class.getName());
    configureProvisionerSuffix("provisioningType", "membershipObjects");
    configureProvisionerSuffix("operateOnGrouperGroups", "true");
    configureProvisionerSuffix("operateOnGrouperEntities", "true");
    configureProvisionerSuffix("operateOnGrouperMemberships", "true");
    configureProvisionerSuffix("numberOfGroupAttributes", "1");
    configureProvisionerSuffix("targetGroupAttribute.0.name", "name");
    configureProvisionerSuffix("targetGroupAttribute.0.translateExpressionType", "grouperProvisioningGroupField");
    configureProvisionerSuffix("targetGroupAttribute.0.translateFromGrouperProvisioningGroupField", "name");
    configureProvisionerSuffix("groupMatchingAttributeCount", "1");
    configureProvisionerSuffix("groupMatchingAttribute0name", "name");
    configureProvisionerSuffix("numberOfEntityAttributes", "1");
    configureProvisionerSuffix("targetEntityAttribute.0.name", "subjectId");
    configureProvisionerSuffix("targetEntityAttribute.0.translateExpressionType", "grouperProvisioningEntityField");
    configureProvisionerSuffix("targetEntityAttribute.0.translateFromGrouperProvisioningEntityField", "subjectId");
    configureProvisionerSuffix("entityMatchingAttributeCount", "1");
    configureProvisionerSuffix("entityMatchingAttribute0name", "subjectId");
    configureProvisionerSuffix("threadPoolSize", "1");
    configureProvisionerSuffix("showAdvanced", "true");
    if (crudOperationOrder != null) {
      configureProvisionerSuffix("crudOperationOrder", crudOperationOrder);
    }
  }

  /**
   * build a request which has an insert, an update, and a delete of a group, an entity, and a
   * membership, so every operation the framework can send is present
   */
  private TargetDaoSendChangesToTargetRequest buildRequest() {

    TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest = new TargetDaoSendChangesToTargetRequest();

    targetDaoSendChangesToTargetRequest.setTargetObjectInserts(buildLists("insert"));
    targetDaoSendChangesToTargetRequest.setTargetObjectUpdates(buildLists("update"));
    targetDaoSendChangesToTargetRequest.setTargetObjectDeletes(buildLists("delete"));

    GrouperProvisioningReplacesObjects grouperProvisioningReplacesObjects = new GrouperProvisioningReplacesObjects();
    grouperProvisioningReplacesObjects.setProvisioningMemberships(new HashMap<ProvisioningGroup, List<ProvisioningMembership>>());
    targetDaoSendChangesToTargetRequest.setTargetObjectReplaces(grouperProvisioningReplacesObjects);

    return targetDaoSendChangesToTargetRequest;
  }

  private GrouperProvisioningLists buildLists(String prefix) {

    ProvisioningGroup provisioningGroup = new ProvisioningGroup();
    provisioningGroup.setName(prefix + "Group");
    provisioningGroup.assignAttributeValue("name", prefix + "Group");

    ProvisioningEntity provisioningEntity = new ProvisioningEntity();
    provisioningEntity.setSubjectId(prefix + "SubjectId");
    provisioningEntity.assignAttributeValue("subjectId", prefix + "SubjectId");

    ProvisioningMembership provisioningMembership = new ProvisioningMembership();
    provisioningMembership.setProvisioningGroupId(prefix + "Group");
    provisioningMembership.setProvisioningEntityId(prefix + "SubjectId");

    GrouperProvisioningLists grouperProvisioningLists = new GrouperProvisioningLists();
    grouperProvisioningLists.setProvisioningGroups(new ArrayList<ProvisioningGroup>(Arrays.asList(provisioningGroup)));
    grouperProvisioningLists.setProvisioningEntities(new ArrayList<ProvisioningEntity>(Arrays.asList(provisioningEntity)));
    grouperProvisioningLists.setProvisioningMemberships(new ArrayList<ProvisioningMembership>(Arrays.asList(provisioningMembership)));

    return grouperProvisioningLists;
  }

  private List<String> sendChanges(String crudOperationOrder) {

    configureProvisioner(crudOperationOrder);

    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner(defaultConfigId());
    grouperProvisioner.initialize(GrouperProvisioningType.fullProvisionFull);

    grouperProvisioner.retrieveGrouperProvisioningTargetDaoAdapter().sendChangesToTarget(buildRequest());

    return CrudOperationOrderTestDao.retrieveOperations();
  }

  /**
   * with no config, deletes go first.  This is the pre-existing behavior so existing configurations
   * are not affected.
   */
  public void testDefaultIsDeletesFirst() {

    List<String> operations = sendChanges(null);

    assertEquals(operations.toString(), Arrays.asList(
        "deleteMemberships",
        "deleteGroups", "insertGroups", "updateGroups",
        "deleteEntities", "insertEntities", "updateEntities",
        "insertMemberships", "updateMemberships"), operations);
  }

  /**
   * deletesFirst is the same as leaving the config unset
   */
  public void testDeletesFirst() {

    List<String> operations = sendChanges("deletesFirst");

    assertEquals(operations.toString(), Arrays.asList(
        "deleteMemberships",
        "deleteGroups", "insertGroups", "updateGroups",
        "deleteEntities", "insertEntities", "updateEntities",
        "insertMemberships", "updateMemberships"), operations);
  }

  /**
   * insertsFirst runs every creating operation before every removing operation.  Membership inserts
   * are still after the group and entity inserts since they depend on them, and membership deletes
   * are still ahead of the group and entity deletes so a group is emptied before it is removed.
   */
  public void testInsertsFirst() {

    List<String> operations = sendChanges("insertsFirst");

    assertEquals(operations.toString(), Arrays.asList(
        "insertGroups", "updateGroups",
        "insertEntities", "updateEntities",
        "insertMemberships", "updateMemberships",
        "deleteMemberships",
        "deleteGroups",
        "deleteEntities"), operations);
  }

}
