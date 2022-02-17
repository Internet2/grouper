package edu.internet2.middleware.grouper.app.remedy;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouper.app.duo.GrouperDuoApiCommands;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoConfiguration;
import edu.internet2.middleware.grouper.app.duo.GrouperDuoUser;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerDaoCapabilities;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllDataResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllEntitiesResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllGroupsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsRequest;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoRetrieveAllMembershipsResponse;
import edu.internet2.middleware.grouper.app.provisioning.targetDao.TargetDaoTimingInfo;

public class GrouperRemedyTargetDao extends GrouperProvisionerTargetDaoBase {
  
  @Override
  public TargetDaoRetrieveAllGroupsResponse retrieveAllGroups(
      TargetDaoRetrieveAllGroupsRequest targetDaoRetrieveAllGroupsRequest) {
   
    long startNanos = System.nanoTime();

    try {

      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this
          .getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningGroup> results = new ArrayList<ProvisioningGroup>();

      Map<Long, GrouperRemedyGroup> grouperRemedyGroups = GrouperRemedyApiCommands
          .retrieveRemedyGroups(remedyConfiguration.getRemedyExternalSystemConfigId());

      for (GrouperRemedyGroup grouperRemedyGroup : grouperRemedyGroups.values()) {
        ProvisioningGroup targetGroup = grouperRemedyGroup.toProvisioningGroup();
        results.add(targetGroup);
      }

      return new TargetDaoRetrieveAllGroupsResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllGroups", startNanos));
    }
    
  }

  @Override
  public TargetDaoRetrieveAllEntitiesResponse retrieveAllEntities(
      TargetDaoRetrieveAllEntitiesRequest targetDaoRetrieveAllEntitiesRequest) {
    
    long startNanos = System.nanoTime();

    try {

      GrouperRemedyConfiguration remedyConfiguration = (GrouperRemedyConfiguration) this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration();

      List<ProvisioningEntity> results = new ArrayList<ProvisioningEntity>();

      Map<String, GrouperRemedyUser> grouperRemedyUsers = GrouperRemedyApiCommands.retrieveRemedyUsers(remedyConfiguration.getRemedyExternalSystemConfigId());

      for (GrouperRemedyUser grouperRemedyUser : grouperRemedyUsers.values()) {
        ProvisioningEntity targetEntity = grouperRemedyUser.toProvisioningEntity();
        results.add(targetEntity);
      }

      return new TargetDaoRetrieveAllEntitiesResponse(results);
    } finally {
      this.addTargetDaoTimingInfo(
          new TargetDaoTimingInfo("retrieveAllEntities", startNanos));
    }
    
  }

  @Override
  public TargetDaoRetrieveAllMembershipsResponse retrieveAllMemberships(
      TargetDaoRetrieveAllMembershipsRequest targetDaoRetrieveAllMembershipsRequest) {
    // TODO Auto-generated method stub
    return super.retrieveAllMemberships(targetDaoRetrieveAllMembershipsRequest);
  }

  @Override
  public TargetDaoRetrieveAllDataResponse retrieveAllData(
      TargetDaoRetrieveAllDataRequest targetDaoRetrieveAllDataRequest) {
    // TODO Auto-generated method stub
    return super.retrieveAllData(targetDaoRetrieveAllDataRequest);
  }

  @Override
  public void registerGrouperProvisionerDaoCapabilities(
      GrouperProvisionerDaoCapabilities grouperProvisionerDaoCapabilities) {
    
//    grouperProvisionerDaoCapabilities.setCanDeleteEntity(true);
//    grouperProvisionerDaoCapabilities.setCanDeleteGroup(true);
    grouperProvisionerDaoCapabilities.setCanDeleteMembership(true);
//    grouperProvisionerDaoCapabilities.setCanInsertEntity(true);
//    grouperProvisionerDaoCapabilities.setCanInsertGroup(true);
    grouperProvisionerDaoCapabilities.setCanInsertMembership(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllData(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllEntities(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllGroups(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveAllMemberships(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveGroup(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByEntity(true);
    grouperProvisionerDaoCapabilities.setCanRetrieveMembershipsByGroup(true);
    
//    grouperProvisionerDaoCapabilities.setCanUpdateEntity(true);
//    grouperProvisionerDaoCapabilities.setCanUpdateGroup(true);
    
  }

}
