package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoDeleteMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoInsertMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoUpdateMembershipsResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * dao which records the order that the framework sends operations, so a test can assert the order.
 * Only implements the individual per object type operations, so the ordering decisions are made by
 * GrouperProvisionerTargetDaoAdapter.sendChangesToTarget()
 */
public class CrudOperationOrderTestDao extends GrouperProvisionerTargetDaoBase {

  /**
   * operations in the order the framework sent them, e.g. "deleteMemberships"
   */
  private static List<String> operations = Collections.synchronizedList(new ArrayList<String>());

  public static List<String> retrieveOperations() {
    return operations;
  }

  public static void clearOperations() {
    operations.clear();
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    grouperProvisionerDaoCapabilities.setCanInsertGroups(true);
    grouperProvisionerDaoCapabilities.setCanUpdateGroups(true);
    grouperProvisionerDaoCapabilities.setCanDeleteGroups(true);
    grouperProvisionerDaoCapabilities.setCanInsertEntities(true);
    grouperProvisionerDaoCapabilities.setCanUpdateEntities(true);
    grouperProvisionerDaoCapabilities.setCanDeleteEntities(true);
    grouperProvisionerDaoCapabilities.setCanInsertMemberships(true);
    grouperProvisionerDaoCapabilities.setCanUpdateMemberships(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMemberships(true);
  }

  @Override
  public TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    operations.add("insertGroups");
    markGroupsProvisioned(targetDaoInsertGroupsRequest.getTargetGroups());
    return new TargetDaoInsertGroupsResponse();
  }

  @Override
  public TargetDaoUpdateGroupsResponse updateGroups(TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest) {
    operations.add("updateGroups");
    markGroupsProvisioned(targetDaoUpdateGroupsRequest.getTargetGroups());
    return new TargetDaoUpdateGroupsResponse();
  }

  @Override
  public TargetDaoDeleteGroupsResponse deleteGroups(TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest) {
    operations.add("deleteGroups");
    markGroupsProvisioned(targetDaoDeleteGroupsRequest.getTargetGroups());
    return new TargetDaoDeleteGroupsResponse();
  }

  @Override
  public TargetDaoInsertEntitiesResponse insertEntities(TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {
    operations.add("insertEntities");
    markEntitiesProvisioned(targetDaoInsertEntitiesRequest.getTargetEntityInserts());
    return new TargetDaoInsertEntitiesResponse();
  }

  @Override
  public TargetDaoUpdateEntitiesResponse updateEntities(TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
    operations.add("updateEntities");
    markEntitiesProvisioned(targetDaoUpdateEntitiesRequest.getTargetEntities());
    return new TargetDaoUpdateEntitiesResponse();
  }

  @Override
  public TargetDaoDeleteEntitiesResponse deleteEntities(TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {
    operations.add("deleteEntities");
    markEntitiesProvisioned(targetDaoDeleteEntitiesRequest.getTargetEntities());
    return new TargetDaoDeleteEntitiesResponse();
  }

  @Override
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    operations.add("insertMemberships");
    markMembershipsProvisioned(targetDaoInsertMembershipsRequest.getTargetMemberships());
    return new TargetDaoInsertMembershipsResponse();
  }

  @Override
  public TargetDaoUpdateMembershipsResponse updateMemberships(TargetDaoUpdateMembershipsRequest targetDaoUpdateMembershipsRequest) {
    operations.add("updateMemberships");
    markMembershipsProvisioned(targetDaoUpdateMembershipsRequest.getTargetMemberships());
    return new TargetDaoUpdateMembershipsResponse();
  }

  @Override
  public TargetDaoDeleteMembershipsResponse deleteMemberships(TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {
    operations.add("deleteMemberships");
    markMembershipsProvisioned(targetDaoDeleteMembershipsRequest.getTargetMemberships());
    return new TargetDaoDeleteMembershipsResponse();
  }

  private static void markGroupsProvisioned(List<ProvisioningGroup> provisioningGroups) {
    for (ProvisioningGroup provisioningGroup : GrouperUtil.nonNull(provisioningGroups)) {
      provisioningGroup.setProvisioned(true);
    }
  }

  private static void markEntitiesProvisioned(List<ProvisioningEntity> provisioningEntities) {
    for (ProvisioningEntity provisioningEntity : GrouperUtil.nonNull(provisioningEntities)) {
      provisioningEntity.setProvisioned(true);
    }
  }

  private static void markMembershipsProvisioned(List<ProvisioningMembership> provisioningMemberships) {
    for (ProvisioningMembership provisioningMembership : GrouperUtil.nonNull(provisioningMemberships)) {
      provisioningMembership.setProvisioned(true);
    }
  }

}
