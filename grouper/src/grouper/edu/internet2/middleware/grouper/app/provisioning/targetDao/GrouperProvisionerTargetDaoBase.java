package edu.internet2.middleware.grouper.app.provisioning.targetDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningLists;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningMembership;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;


/**
 * generally when you deal with insert/update/delete/retrieve
 * you should use the "matchingId" of the parameter to the method.
 * Note: when you retrieve the data, you should not set the matchingId,
 * you should let the provisioning framework set that.
 * 
 * The results of retrieving a few objects should be in the same format
 * as when you retrieve all at once.
 * 
 */
public abstract class GrouperProvisionerTargetDaoBase {
  
  /**
   * start logging the source low level actions
   */
  public void loggingStart() {
    
  }

  /**
   * stop logging and get the output
   */
  public String loggingStop() {
    return null;
  }

  private GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities = new GrouperProvisionerDaoCapabilities();
  
  
  public GrouperProvisionerDaoCapabilities getGrouperProvisionerDaoCapabilities() {
    return grouperProvisionerDaoCapabilities;
  }
  
  public void setGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    this.grouperProvisionerDaoCapabilities = grouperProvisionerDaoCapabilities;
  }

  public abstract void registerGrouperProvisionerDaoCapabilities(GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities);
  
  private List<TargetDaoTimingInfo> targetDaoTimingInfos = new ArrayList<TargetDaoTimingInfo>();
  
  public void addTargetDaoTimingInfo(TargetDaoTimingInfo targetDaoTimingInfo) {
    this.targetDaoTimingInfos.add(targetDaoTimingInfo);
  }
  
  
  public List<TargetDaoTimingInfo> getTargetDaoTimingInfos() {
    return targetDaoTimingInfos;
  }

  
  public void setTargetDaoTimingInfos(List<TargetDaoTimingInfo> targetDaoTimingInfos) {
    this.targetDaoTimingInfos = targetDaoTimingInfos;
  }

  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * dont return null
   * @param targetDaoRetrieveAllEntitiesRequest
   * @return
   */
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    throw new UnsupportedOperationException();
  }
  
 
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningGrouproup
   */
  public TargetDaoDeleteGroupResponse deleteGroup(TargetDaoDeleteGroupRequest targetDaoDeleteGroupRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningGrouproup
   */
  public TargetDaoInsertGroupResponse insertGroup(TargetDaoInsertGroupRequest targetDaoInsertGroupRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;

  /**
   * reference back up to the provisioner
   * @return the provisioner
   */
  public GrouperProvisioner getGrouperProvisioner() {
    return this.grouperProvisioner;
  }

  /**
   * reference back up to the provisioner
   * @param grouperProvisioner1
   */
  public void setGrouperProvisioner(GrouperProvisioner grouperProvisioner1) {
    this.grouperProvisioner = grouperProvisioner1;
  }


  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetDaoSendChangesToTargetRequest
   * @return
   */
  public TargetDaoSendChangesToTargetResponse sendChangesToTarget(TargetDaoSendChangesToTargetRequest targetDaoSendChangesToTargetRequest) {

    throw new UnsupportedOperationException();

  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetDaoSendGroupChangesToTargetRequest
   * @return
   */
  public TargetDaoSendGroupChangesToTargetResponse sendGroupChangesToTarget(TargetDaoSendGroupChangesToTargetRequest targetDaoSendGroupChangesToTargetRequest) {

    throw new UnsupportedOperationException();

  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * insert all these groups and either throw exception for all or mark each one with an exception
   */
  public TargetDaoUpdateGroupsResponse updateGroups(TargetDaoUpdateGroupsRequest targetDaoUpdateGroupsRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * delete all these Memberships and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   */
  public TargetDaoDeleteMembershipsResponse deleteMemberships(TargetDaoDeleteMembershipsRequest targetDaoDeleteMembershipsRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * retrieve all data from the target
   */
  public TargetDaoRetrieveAllDataResponse retrieveAllData(TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * retrieve all incremental data from the target from the matching ids of the grouper translated and indexed target groups
   */
  public TargetDaoRetrieveIncrementalDataResponse retrieveIncrementalData(
      TargetDaoRetrieveIncrementalDataRequest targetDaoRetrieveIncementalDataRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * bulk retrieve target provisioning groups, generally use the matching Ids in the targetGroups
   * @return the target provisioning groups
   */
  public TargetDaoRetrieveGroupsResponse retrieveGroups(TargetDaoRetrieveGroupsRequest targetDaoRetrieveGroupsRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * bulk retrieve target provisioning Memberships, generally use the matching Ids in the targetMemberships
   * @return the target provisioning Memberships
   */
  public TargetDaoRetrieveMembershipsBulkResponse retrieveMembershipsBulk(
      TargetDaoRetrieveMembershipsBulkRequest targetDaoRetrieveMembershipsBulkRequest) {
    throw new UnsupportedOperationException();

  }
  
  /**
   * bulk retrieve all target provisioning Memberships related to these groups, generally use the matching Ids in the targetGroups
   * @return the target provisioning memberships
   */
  public TargetDaoRetrieveMembershipsByGroupsResponse retrieveMembershipsByGroups(TargetDaoRetrieveMembershipsByGroupsRequest targetDaoRetrieveMembershipsByGroupsRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * bulk retrieve all target provisioning Memberships related to these group
   * @param targetGroup
   * @return the memberships
   */
  public TargetDaoRetrieveMembershipsByGroupResponse retrieveMembershipsByGroup(
      TargetDaoRetrieveMembershipsByGroupRequest targetDaoRetrieveMembershipsByGroupRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * bulk retrieve target provisioning Memberships, generally use the matching Ids in the targetEntities
   * @return the target provisioning memberships
   */
  public TargetDaoRetrieveMembershipsByEntitiesResponse retrieveMembershipsByEntities(
      TargetDaoRetrieveMembershipsByEntitiesRequest targetDaoRetrieveMembershipsByEntitiesRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * bulk retrieve all target provisioning Memberships related to these entity
   * @param targetEntity
   * @return the memberships
   */
  public TargetDaoRetrieveMembershipsByEntityResponse retrieveMembershipsByEntity(TargetDaoRetrieveMembershipsByEntityRequest targetDaoRetrieveMembershipsByEntityRequest) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * bulk retrieve target provisioning Memberships, generally use the matching Ids in the targetMemberships
   * @ptouperTargetMemberships
   * @return the target provisioning Memberships
   */
  public TargetDaoRetrieveMembershipsResponse retrieveMemberships(TargetDaoRetrieveMembershipsRequest targetDaoRetrieveMembershipsRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * bulk retrieve target provisioning Entities, generally use the matching Ids in the targetEntities
   * @param targetEntities
   * @return the target provisioning Entities
   */
  public TargetDaoRetrieveEntitiesResponse retrieveEntities(TargetDaoRetrieveEntitiesRequest targetDaoRetrieveEntitiesRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * return a group by matching id of grouper target group, or null if not found
   * @param targetGroup
   * @return the target provisioning group or null if not found
   */
  public TargetDaoRetrieveGroupResponse retrieveGroup(TargetDaoRetrieveGroupRequest targetDaoRetrieveGroupRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * return a Entity by matching id of grouper target Entity, or null if not found
   * @param targetEntity
   * @return the target provisioning Entity or null if not found
   */
  public TargetDaoRetrieveEntityResponse retrieveEntity(TargetDaoRetrieveEntityRequest targetDaoRetrieveEntityRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * return a Membership by matching id of grouper target Membership, or null if not found
   * @param targetMembership
   * @return the target provisioning Membership or null if not found
   */
  public TargetDaoRetrieveMembershipResponse retrieveMembership(TargetDaoRetrieveMembershipRequest targetDaoRetrieveMembershipRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningGrouproup
   */
  public TargetDaoUpdateGroupResponse updateGroup(TargetDaoUpdateGroupRequest targetDaoUpdateGroupRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these groups and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   */
  public TargetDaoInsertGroupsResponse insertGroups(TargetDaoInsertGroupsRequest targetDaoInsertGroupsRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningEntity
   */
  public TargetDaoDeleteEntityResponse deleteEntity(TargetDaoDeleteEntityRequest targetDaoDeleteEntityRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * delete all these entities and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   */
  public TargetDaoDeleteEntitiesResponse deleteEntities(TargetDaoDeleteEntitiesRequest targetDaoDeleteEntitiesRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningGrouproup
   */
  public TargetDaoInsertEntityResponse insertEntity(TargetDaoInsertEntityRequest targetDaoInsertEntityRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these groups and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetEntityInserts
   */
  public TargetDaoInsertEntitiesResponse insertEntities(TargetDaoInsertEntitiesRequest targetDaoInsertEntitiesRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningEntity
   */
  public TargetDaoUpdateEntityResponse updateEntity(TargetDaoUpdateEntityRequest targetDaoUpdateEntityRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * insert all these Entities and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetEntityInserts
   */
  public TargetDaoUpdateEntitiesResponse updateEntities(TargetDaoUpdateEntitiesRequest targetDaoUpdateEntitiesRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningMembership
   */
  public TargetDaoDeleteMembershipResponse deleteMembership(TargetDaoDeleteMembershipRequest targetDaoDeleteMembershipRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * delete all these groups and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetGroupDeletes
   */
  public TargetDaoDeleteGroupsResponse deleteGroups(TargetDaoDeleteGroupsRequest targetDaoDeleteGroupsRequest ) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningMembership
   */
  public TargetDaoInsertMembershipResponse insertMembership(TargetDaoInsertMembershipRequest targetDaoInsertMembershipRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * replace a groups memberships with this list
   * @param targetDaoReplaceGroupMembershipsRequest
   * @return the response
   */
  public TargetDaoReplaceGroupMembershipsResponse replaceGroupMemberships(TargetDaoReplaceGroupMembershipsRequest targetDaoReplaceGroupMembershipsRequest) {
    throw new UnsupportedOperationException();
  }
  
  /**
   * insert all these Memberships and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   */
  public TargetDaoInsertMembershipsResponse insertMemberships(TargetDaoInsertMembershipsRequest targetDaoInsertMembershipsRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @paProvisioningMembership
   */
  public TargetDaoUpdateMembershipResponse updateMembership(TargetDaoUpdateMembershipRequest targetDaoUpdateMembershipRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * update all these Memberships and either throw exception for all or mark each one with an exception
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   */
  public TargetDaoUpdateMembershipsResponse updateMemberships(TargetDaoUpdateMembershipsRequest targetDaoUpdateMembershipsRequest) {
    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param ta
   * @return 
   */
  public TargetDaoSendEntityChangesToTargetResponse sendEntityChangesToTarget(TargetDaoSendEntityChangesToTargetRequest targetDaoSendEntityChangesToTargetRequest) {

    throw new UnsupportedOperationException();
  }

  /**
   * set each provisioning object as "provisioned" after the insert/update/delete is done
   * e.g. targetObject.setProvisioned(true)
   * @param targetDaoSendMembershipChangesToTargetRequest
   * @return
   */
  public TargetDaoSendMembershipChangesToTargetResponse sendMembershipChangesToTarget(
      TargetDaoSendMembershipChangesToTargetRequest targetDaoSendMembershipChangesToTargetRequest) {
    
    throw new UnsupportedOperationException();

  }
  
}
