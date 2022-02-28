package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderStatus;
import edu.internet2.middleware.grouper.app.loader.OtherJobException;
import edu.internet2.middleware.grouper.misc.GrouperFailsafe;
import edu.internet2.middleware.grouper.misc.GrouperFailsafeBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncLogState;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * 
 * @author mchyzer
 *
 */
public class GrouperProvisioningFailsafe {

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;
  
  /**
   * 
   */
  private GrouperFailsafeBean grouperFailsafeBean = new GrouperFailsafeBean();

  /**
   * see if there is a failsafe issue and throw a failsafe error
   */
  public void processFailsafes() {
      
    processFailsafesMinOverallNumberOfMembers();
    
    processFailsafesMaxGroupPercentRemove();

    processFailsafesMinManagedGroups();
    
  }

  /**
   * 
   */
  public void processFailsafesMinManagedGroups() {
    
    if (this.grouperFailsafeBean.getMinManagedGroups() != null && this.grouperFailsafeBean.getMinManagedGroups() > -1) {
      this.processFailsafesSetupGroupTotals();
      this.processFailsafesSetupGroupCount();
      
      Set<String> groupUuidsToDelete = new HashSet<String>(this.getGrouperProvisioner().retrieveGrouperProvisioningCompare().getGroupUuidsToDelete());
      
      Map<String, Integer> groupUuidToMembershipDeleteCount = new HashMap<String, Integer>(this.getGrouperProvisioner().retrieveGrouperProvisioningCompare().getGroupUuidToMembershipDeleteCount());
      Map<String, Integer> groupUuidToMembershipAddCount = new HashMap<String, Integer>(this.getGrouperProvisioner().retrieveGrouperProvisioningCompare().getGroupUuidToMembershipAddCount());
      
      for (String groupUuid : groupUuidsToDelete) {

        groupUuidToMembershipDeleteCount.remove(groupUuid);
        groupUuidToMembershipAddCount.remove(groupUuid);
      }
      
      for (String groupUuid : this.groupUuidToGroupMembershipCount.keySet()) {
        
        // already accounted for
        if (groupUuidsToDelete.contains(groupUuid)) {
          continue;
        }
        
        int currentCount = this.groupUuidToGroupMembershipCount.get(groupUuid);
        currentCount += GrouperUtil.intValue(groupUuidToMembershipAddCount.get(groupUuid), 0);
        currentCount -= GrouperUtil.intValue(groupUuidToMembershipDeleteCount.get(groupUuid), 0);
        
        // not sure why it would be less than zero but...
        if (currentCount <= 0) {
          groupUuidsToDelete.add(groupUuid);
        }
        
      }
        
      if (this.grouperFailsafeBean.shouldAbortDueToTooManyGroupListManagedGroupsBeingCleared(this.groupCountWithMembers, groupUuidsToDelete.size())) {
        this.getGrouperProvisioner().getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.ERROR_FAILSAFE);
        GrouperFailsafe.assignFailed(this.grouperFailsafeBean.getJobName());
        this.grouperFailsafeBean.notifyEmailAboutFailsafe();
        throw new OtherJobException(GrouperLoaderStatus.ERROR_FAILSAFE, "Can't clear out "
            + groupUuidsToDelete.size() + " groups (totalManagedGroupsWithMembersCount: "
            + this.groupCountWithMembers + ")"
            + " unless data problem is fixed, failsafe is approved, or failsafe settings changed" );
      }
    }
  }

  /**
   * see if there is a failsafe issue and throw a failsafe error
   */
  public void processFailsafesAtStart() {
    String jobName = this.getGrouperProvisioner().getJobName();
    
    if (StringUtils.isBlank(jobName)) {
      return;
    }
    
    processFailsafesSetupBean();
    
    processFailsafesFailIncrementalIfFullFailsafeIssue();
  
  }

  /**
   * 
   */
  public void processFailsafesFailIncrementalIfFullFailsafeIssue() {
    // if we are incremental, and there is a failsafe issue, then dont even try...
    if (this.getGrouperProvisioner().retrieveGrouperProvisioningBehavior().getGrouperProvisioningType().isIncrementalSync()) {
      for (String theJobName : GrouperUtil.nonNull(this.getGrouperProvisioner().getJobNames())) {
        if (GrouperFailsafe.isFailsafeIssue(theJobName)) {
          this.grouperFailsafeBean.setJobName(theJobName);
          this.grouperFailsafeBean.notifyEmailAboutFailsafe();
          throw new RuntimeException("Failsafe error from full sync '" + theJobName + "' prevents the incremental from running");
        }
      }
    }
  }

  /**
   * 
   */
  private Map<String, Integer> groupUuidToGroupMembershipCount = null;

  /**
   * @return group to membership count
   */
  public Map<String, Integer> getGroupUuidToGroupMembershipCount() {
    return this.groupUuidToGroupMembershipCount;
  }

  /**
   * only call this with groups that have removes... so call this after compare...
   */
  public void processFailsafesSetupGroupTotals() {
    if (this.groupUuidToGroupMembershipCount == null) {
      this.groupUuidToGroupMembershipCount = new HashMap<String, Integer>();
      Map<String, Integer> groupUuidToMembershipDeleteCount = this.getGrouperProvisioner().retrieveGrouperProvisioningCompare().getGroupUuidToMembershipDeleteCount();
      
      if (GrouperUtil.length(groupUuidToMembershipDeleteCount) > 0) {
        int batchSize = 900;
        int numberOfBatches = GrouperUtil.batchNumberOfBatches(groupUuidToMembershipDeleteCount.size(), batchSize);
        List<String> uuids = new ArrayList<String>(groupUuidToMembershipDeleteCount.keySet());
        for (int i=0;i<numberOfBatches;i++) {
          
          List<String> batchUuids = GrouperUtil.batchList(uuids, batchSize, i);
          
          // we need to full count of the group...  lets try to do that in few queries
          GcDbAccess gcDbAccess = new GcDbAccess().sql("select gsg.group_id, count(*) from grouper_sync_group gsg, grouper_sync_membership gsm "
              + " where gsg.id = gsm.grouper_sync_group_id and gsm.in_target = 'T' and gsg.grouper_sync_id = ? and gsg.group_id in ("
              + GrouperClientUtils.appendQuestions(GrouperUtil.length(batchUuids)) + ") group by gsg.group_id ");
          
          gcDbAccess.addBindVar(this.getGrouperProvisioner().getGcGrouperSync().getId());
          for (String batchUuid : batchUuids) {
            gcDbAccess.addBindVar(batchUuid);
          }
          List<Object[]> groupUuidAndCounts = gcDbAccess.selectList(Object[].class);
          for (Object[] groupUuidAndCount : groupUuidAndCounts) {
            this.groupUuidToGroupMembershipCount.put((String)groupUuidAndCount[0], GrouperUtil.intValue(groupUuidAndCount[1]));
          }
        }
      }
    }
  }
  
  /**
   * how many groups have at least one member
   */
  private Integer groupCountWithMembers;
  
  /**
   * how many memberships are provisioned
   */
  private Integer overallMemberships;
  
  /**
   * how many memberships are provisioned
   * @return overall memberships
   */
  public Integer getOverallMemberships() {
    return this.overallMemberships;
  }

  /**
   * how many memberships are provisioned
   * @param overallMemberships1
   */
  public void setOverallMemberships(Integer overallMemberships1) {
    this.overallMemberships = overallMemberships1;
  }

  /**
   * 
   * @param groupCountWithMembers1
   */
  public void setGroupCountWithMembers(Integer groupCountWithMembers1) {
    this.groupCountWithMembers = groupCountWithMembers1;
  }

  /**
   * how many groups have at least one member
   * @return how many groups with member
   */
  public Integer getGroupCountWithMembers() {
    return this.groupCountWithMembers;
  }

  /**
   * only call this with groups that have removes... so call this after compare...
   */
  public void processFailsafesSetupGroupCount() {
    if (this.groupCountWithMembers == null) {
      
      // we need to full count of the group...  lets try to do that in few queries
      GcDbAccess gcDbAccess = new GcDbAccess().sql("select count(1) from grouper_sync_group gsg where gsg.grouper_sync_id = ? "
          + " and exists (select 1 from grouper_sync_membership gsm " 
          + " where gsg.id = gsm.grouper_sync_group_id and gsm.in_target = 'T' ) ");
      
      gcDbAccess.addBindVar(this.getGrouperProvisioner().getGcGrouperSync().getId());
      this.groupCountWithMembers = gcDbAccess.select(int.class);
    }
  }
  
  /**
   * only call this with groups that have removes... so call this after compare...
   */
  public void processFailsafesSetupMembershipCount() {
    if (this.overallMemberships == null) {
      
      // we need to full count of the group...  lets try to do that in few queries
      GcDbAccess gcDbAccess = new GcDbAccess().sql("select count(1) from grouper_sync_membership gsm where gsm.grouper_sync_id = ? and gsm.in_target = 'T' ");
      
      gcDbAccess.addBindVar(this.getGrouperProvisioner().getGcGrouperSync().getId());
      this.overallMemberships = gcDbAccess.select(int.class);
    }
  }
  
  /**
   * 
   */
  public void processFailsafesMaxGroupPercentRemove() {
    if (this.grouperFailsafeBean.getMaxGroupPercentRemove() != -1) {
      
      Map<String, Integer> groupUuidToMembershipDeleteCount = this.getGrouperProvisioner().retrieveGrouperProvisioningCompare().getGroupUuidToMembershipDeleteCount();
      Map<String, Integer> groupUuidToMembershipAddCount = this.getGrouperProvisioner().retrieveGrouperProvisioningCompare().getGroupUuidToMembershipAddCount();
      
      if (GrouperUtil.length(groupUuidToMembershipDeleteCount) > 0) {
        
        this.processFailsafesSetupGroupTotals();
        
        for (String groupUuid : this.groupUuidToGroupMembershipCount.keySet()) {
          Integer totalCount = this.groupUuidToGroupMembershipCount.get(groupUuid);
          if (totalCount >= this.grouperFailsafeBean.getMinGroupSize()) {
            int deleteCount = groupUuidToMembershipDeleteCount.get(groupUuid);
            int addCount = GrouperUtil.intValue(groupUuidToMembershipAddCount.get(groupUuid), 0);
            if (this.grouperFailsafeBean.shouldAbortDueToTooManyMembersRemoved(totalCount, deleteCount, addCount)) {
              this.getGrouperProvisioner().getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.ERROR_FAILSAFE);
              GrouperFailsafe.assignFailed(this.grouperFailsafeBean.getJobName());
              this.grouperFailsafeBean.notifyEmailAboutFailsafe();
              // TODO consider inserts?
              throw new OtherJobException(GrouperLoaderStatus.ERROR_FAILSAFE, "Failsafe error on group: '" 
              + this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupUuid).getGrouperProvisioningGroup().getName() 
              + "' current mship count: " + totalCount + ", assumed deletions: " + deleteCount 
                  + " unless data problem is fixed, failsafe is approved, or failsafe settings changed");
            }
          }
        }
      }      
    }
  }

  /**
   * 
   */
  public void processFailsafesMinOverallNumberOfMembers() {
    if ((this.grouperFailsafeBean.getMinOverallNumberOfMembers() != null && this.grouperFailsafeBean.getMinOverallNumberOfMembers() != -1)
        || this.grouperFailsafeBean.getMaxOverallPercentMembershipsRemove() != -1 ) {

      this.processFailsafesSetupMembershipCount();
      
      int membershipAdds = this.getGrouperProvisioner().retrieveGrouperProvisioningCompare().getMembershipAddCount();
      int membershipDeletes = this.getGrouperProvisioner().retrieveGrouperProvisioningCompare().getMembershipDeleteCount();

      if (this.grouperFailsafeBean.shouldAbortDueToTooManyOverallMembersRemoved(this.overallMemberships, membershipDeletes, membershipAdds)) {
        this.getGrouperProvisioner().getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.ERROR_FAILSAFE);
        GrouperFailsafe.assignFailed(this.grouperFailsafeBean.getJobName());
        this.grouperFailsafeBean.notifyEmailAboutFailsafe();
        throw new OtherJobException(GrouperLoaderStatus.ERROR_FAILSAFE, "Failsafe error current mship count: " + this.overallMemberships + ", assumed deletions: " + membershipDeletes + ", assumedInserts: " + membershipAdds
            + " unless data problem is fixed, failsafe is approved, or failsafe settings changed");
      }
    }
  }

  /**
   * 
   */
  public void processFailsafesSetupBean() {
    
    String jobName = this.getGrouperProvisioner().getJobName();
    if (StringUtils.isBlank(jobName)) {
      return;
    }
  
    // lets see if we are configured to do failsafes
    Boolean showFailsafe = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigBoolean("showFailsafe", false);
    
    this.grouperFailsafeBean.setJobName(jobName);
    
    if (showFailsafe != null && showFailsafe) {
      {
        Boolean failsafeUse = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigBoolean("failsafeUse", false);
        this.grouperFailsafeBean.assignUseFailsafeOverride(failsafeUse);
        if (!this.grouperFailsafeBean.isUseFailsafe()) {
          return;
        }
      }
      
      {
        Boolean failsafeSendEmail = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigBoolean("failsafeSendEmail", false);
        this.grouperFailsafeBean.assignSendEmailOverride(failsafeSendEmail);
      }
      {
        Integer failsafeMinGroupSize = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigInt("failsafeMinGroupSize", false);
        this.grouperFailsafeBean.assignMinGroupSizeOverride(failsafeMinGroupSize);
      }
      {
        Integer failsafeMaxPercentRemove = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigInt("failsafeMaxPercentRemove", false);
        this.grouperFailsafeBean.assignMaxGroupPercentRemoveOverride(failsafeMaxPercentRemove);
      }
      {
        Integer failsafeMinManagedGroups = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigInt("failsafeMinManagedGroups", false);
        this.grouperFailsafeBean.assignMinManagedGroupsOverride(failsafeMinManagedGroups);
      }
      {
        Integer failsafeMaxOverallPercentGroupsRemove = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigInt("failsafeMaxOverallPercentGroupsRemove", false);
        this.grouperFailsafeBean.assignMaxOverallPercentGroupsRemoveOverride(failsafeMaxOverallPercentGroupsRemove);
      }
      {
        Integer failsafeMaxOverallPercentMembershipsRemove = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigInt("failsafeMaxOverallPercentMembershipsRemove", false);
        this.grouperFailsafeBean.assignMaxOverallPercentMembershipsRemoveOverride(failsafeMaxOverallPercentMembershipsRemove);
      }
      {
        Integer failsafeMinOverallNumberOfMembers = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigInt("failsafeMinOverallNumberOfMembers", false);
        this.grouperFailsafeBean.assignMinOverallNumberOfMembersOverride(failsafeMinOverallNumberOfMembers);
      }
    }
  }

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
   * @return bean
   */
  public GrouperFailsafeBean getGrouperFailsafeBean() {
    return this.grouperFailsafeBean;
  }

  /**
   * @param grouperFailsafeBean1
   */
  public void setGrouperFailsafeBean(GrouperFailsafeBean grouperFailsafeBean1) {
    this.grouperFailsafeBean = grouperFailsafeBean1;
  }

}
