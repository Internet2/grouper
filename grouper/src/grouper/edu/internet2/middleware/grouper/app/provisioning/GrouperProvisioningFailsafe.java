package edu.internet2.middleware.grouper.app.provisioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
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

  /** GRP-7013: diagnostic logger so admins can see why failsafe did/didn't trip */
  private static final Log LOG = GrouperUtil.getLog(GrouperProvisioningFailsafe.class);

  /**
   * reference back up to the provisioner
   */
  private GrouperProvisioner grouperProvisioner = null;
  
  /**
   * 
   */
  private GrouperFailsafeBean grouperFailsafeBean = new GrouperFailsafeBean();

  // GRP-7013: one-line outcome put in the debug map so admins can see why
  // failsafe did or did not fire.  No commas (debug map values are joined
  // with commas downstream).
  private String failsafeDisabledReason = null;
  private int maxGroupPercentSeen = -1;
  private String maxGroupPercentGroupName = null;
  private int groupsEvaluatedForPercentRemove = 0;

  /**
   * see if there is a failsafe issue and throw a failsafe error
   */
  public void processFailsafes() {

    processFailsafesMinOverallNumberOfMembers();

    processFailsafesMaxGroupPercentRemove();

    processFailsafesMinManagedGroups();

    // GRP-7013: if none of the above tripped (no OtherJobException thrown),
    // write the passing summary.  Trip paths write their own summary before
    // throwing.
    writeFailsafeSummaryPassed();
  }

  /**
   * GRP-7013: append the resolved thresholds (those that are not -1 / null)
   * to the summary line.  No commas - downstream joiner uses ',' as a
   * separator.
   */
  private void appendActiveThresholds(StringBuilder sb) {
    if (this.grouperFailsafeBean.getMinGroupSize() != -1) {
      sb.append(" minGroupSize=").append(this.grouperFailsafeBean.getMinGroupSize());
    }
    if (this.grouperFailsafeBean.getMaxGroupPercentRemove() != -1) {
      sb.append(" maxPercent=").append(this.grouperFailsafeBean.getMaxGroupPercentRemove());
    }
    if (this.grouperFailsafeBean.getMinManagedGroups() != null && this.grouperFailsafeBean.getMinManagedGroups() != -1) {
      sb.append(" minManagedGroups=").append(this.grouperFailsafeBean.getMinManagedGroups());
    }
    if (this.grouperFailsafeBean.getMaxOverallPercentGroupsRemove() != -1) {
      sb.append(" maxOverallPercentGroupsRemove=").append(this.grouperFailsafeBean.getMaxOverallPercentGroupsRemove());
    }
    if (this.grouperFailsafeBean.getMaxOverallPercentMembershipsRemove() != -1) {
      sb.append(" maxOverallPercentMembershipsRemove=").append(this.grouperFailsafeBean.getMaxOverallPercentMembershipsRemove());
    }
    if (this.grouperFailsafeBean.getMinOverallNumberOfMembers() != null && this.grouperFailsafeBean.getMinOverallNumberOfMembers() != -1) {
      sb.append(" minOverallNumberOfMembers=").append(this.grouperFailsafeBean.getMinOverallNumberOfMembers());
    }
  }

  /**
   * GRP-7013: write the one-line "passed" or "disabled" summary into the
   * provisioner debug map.  Called after all checks complete without tripping
   * (or in place of any checks when failsafe is disabled at setup time).
   */
  private void writeFailsafeSummaryPassed() {
    StringBuilder sb = new StringBuilder();
    if (this.failsafeDisabledReason != null) {
      sb.append("disabled (").append(this.failsafeDisabledReason).append(")");
    } else if (!this.grouperFailsafeBean.isUseFailsafe()) {
      sb.append("disabled");
    } else {
      sb.append("enabled");
      appendActiveThresholds(sb);
      sb.append(": ");
      if (this.groupsEvaluatedForPercentRemove == 0) {
        sb.append("no group deletions; passed");
      } else {
        sb.append(this.groupsEvaluatedForPercentRemove).append(" groups evaluated");
        if (this.maxGroupPercentGroupName != null) {
          sb.append("; highest ").append(this.maxGroupPercentSeen).append("% in '")
              .append(this.maxGroupPercentGroupName).append("'");
        }
        sb.append("; passed");
      }
    }
    this.getGrouperProvisioner().getDebugMap().put("failsafe", sb.toString());
  }

  /**
   * GRP-7013: write the one-line "TRIPPED" summary right before throwing so
   * the debug map records which check tripped and why.
   */
  private void writeFailsafeSummaryTripped(String which, String detail) {
    StringBuilder sb = new StringBuilder("enabled");
    appendActiveThresholds(sb);
    sb.append(": TRIPPED on ").append(which).append(" - ").append(detail);
    this.getGrouperProvisioner().getDebugMap().put("failsafe", sb.toString());
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
        // GRP-7013: record trip in the debug map and the daemon log
        String detail = "would empty " + groupUuidsToDelete.size() + " of " + this.groupCountWithMembers + " managed groups";
        writeFailsafeSummaryTripped("minManagedGroups", detail);
        LOG.info("Failsafe TRIPPED minManagedGroups for job '" + this.grouperFailsafeBean.getJobName() + "': " + detail);
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
        if (!GrouperFailsafe.isApproved(theJobName) && GrouperFailsafe.isFailsafeIssue(theJobName)) {
          this.grouperFailsafeBean.setJobName(theJobName);
          this.grouperFailsafeBean.notifyEmailAboutFailsafe();
          throw new RuntimeException("Failsafe error from '" + theJobName + "' prevents the incremental from running");
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
          if (totalCount < this.grouperFailsafeBean.getMinGroupSize()) {
            continue;
          }
          int deleteCount = GrouperUtil.intValue(groupUuidToMembershipDeleteCount.get(groupUuid), 0);
          int addCount = GrouperUtil.intValue(groupUuidToMembershipAddCount.get(groupUuid), 0);

          // GRP-7013: track the worst-case observation so the passing summary
          // can show "highest X% in 'someGroup'".  Percent of current group
          // size that would be deleted (matches what shouldAbort considers).
          if (totalCount > 0 && deleteCount > 0) {
            int percent = (int) ((100L * deleteCount) / totalCount);
            this.groupsEvaluatedForPercentRemove++;
            if (percent > this.maxGroupPercentSeen) {
              this.maxGroupPercentSeen = percent;
              this.maxGroupPercentGroupName = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex()
                  .getGroupUuidToProvisioningGroupWrapper().get(groupUuid).getGrouperProvisioningGroup().getName();
            }
          }

          if (this.grouperFailsafeBean.shouldAbortDueToTooManyMembersRemoved(totalCount, deleteCount, addCount)) {
            String groupName = this.getGrouperProvisioner().retrieveGrouperProvisioningDataIndex().getGroupUuidToProvisioningGroupWrapper().get(groupUuid).getGrouperProvisioningGroup().getName();
            int percent = totalCount > 0 ? (int) ((100L * deleteCount) / totalCount) : 0;
            String detail = "'" + groupName + "' currentCount=" + totalCount + " deletes=" + deleteCount
                + " adds=" + addCount + " (" + percent + "%)";
            writeFailsafeSummaryTripped("maxGroupPercentRemove", detail);
            LOG.info("Failsafe TRIPPED maxGroupPercentRemove for job '" + this.grouperFailsafeBean.getJobName() + "': " + detail);
            this.getGrouperProvisioner().getGcGrouperSyncLog().setStatus(GcGrouperSyncLogState.ERROR_FAILSAFE);
            GrouperFailsafe.assignFailed(this.grouperFailsafeBean.getJobName());
            this.grouperFailsafeBean.notifyEmailAboutFailsafe();
            // TODO consider inserts?
            throw new OtherJobException(GrouperLoaderStatus.ERROR_FAILSAFE, "Failsafe error on group: '"
            + groupName
            + "' current mship count: " + totalCount + ", assumed deletions: " + deleteCount
                + " unless data problem is fixed, failsafe is approved, or failsafe settings changed");
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
        // GRP-7013: record trip in the debug map and the daemon log
        String detail = "currentMembershipCount=" + this.overallMemberships
            + " deletes=" + membershipDeletes + " adds=" + membershipAdds;
        writeFailsafeSummaryTripped("minOverallNumberOfMembers", detail);
        LOG.info("Failsafe TRIPPED minOverallNumberOfMembers for job '" + this.grouperFailsafeBean.getJobName() + "': " + detail);
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
  
    this.grouperFailsafeBean.setJobName(jobName);

    // GRP-7012: provisioner failsafe is self-contained.  loader.failsafe.use is a
    // SQL/LDAP loader-job concept and is not consulted here.  Reset the bean's
    // useFailsafe (seeded from loader.failsafe.use in the constructor) before
    // reading provisioner config.  A provisioner enforces failsafe only when
    // showFailsafe=true AND failsafeUse=true on this provisioner (or on a
    // provisioner default config).
    this.grouperFailsafeBean.setUseFailsafe(false);

    Boolean showFailsafe = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigBoolean("showFailsafe", false);

    if (showFailsafe == null || !showFailsafe) {
      // GRP-7013: per-provisioner failsafe not enabled at all
      this.failsafeDisabledReason = "showFailsafe not set";
      writeFailsafeSummaryPassed();
      return;
    }

    {
      Boolean failsafeUse = this.getGrouperProvisioner().retrieveGrouperProvisioningConfiguration().retrieveConfigBoolean("failsafeUse", false);
      this.grouperFailsafeBean.assignUseFailsafeOverride(failsafeUse);
      if (!this.grouperFailsafeBean.isUseFailsafe()) {
        // GRP-7013: surface the most common misconfig - showFailsafe on but failsafeUse not enabled
        if (failsafeUse == null) {
          this.failsafeDisabledReason = "misconfigured: showFailsafe=true but failsafeUse blank - not enforcing";
          LOG.warn("Failsafe is configured for provisioner job '" + jobName + "' (showFailsafe=true) but failsafeUse is blank;"
              + " failsafe will NOT enforce. Set failsafeUse=true on the provisioner (or on a provisioner default config) to enable.");
        } else {
          this.failsafeDisabledReason = "failsafeUse=false";
        }
        writeFailsafeSummaryPassed();
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
    {
      String failsafeSendEmailToAddresses = GrouperLoaderConfig.retrieveConfig().propertyValueString("loader.failsafe.sendEmailToAddresses");
      String failsafeSendEmailToGroup = GrouperLoaderConfig.retrieveConfig().propertyValueString("loader.failsafe.sendEmailToGroup");
      grouperFailsafeBean.assignEmailAddressesOverride(failsafeSendEmailToAddresses, failsafeSendEmailToGroup);
    }

    // GRP-7013: write a baseline "enabled but no checks yet run" summary so
    // the debug map always has a failsafe key when the bean was set up.  If
    // processFailsafes() runs later it overwrites this with the actual
    // passed/TRIPPED outcome.  If it never runs (e.g. a code path that
    // bypasses checks) the baseline tells us that.
    StringBuilder sb = new StringBuilder("enabled");
    appendActiveThresholds(sb);
    sb.append(": configured (checks not yet run)");
    this.getGrouperProvisioner().getDebugMap().put("failsafe", sb.toString());
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
