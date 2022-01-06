package edu.internet2.middleware.grouper.misc;

import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperEmail;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperFailsafeBean {

  
  public GrouperFailsafeBean() {
    
    Boolean theSendEmail = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.failsafe.sendEmail");

    if (theSendEmail == null) {
      theSendEmail = !StringUtils.isBlank(this.emailAddresses) || !StringUtils.isBlank(this.emailGroupName);
    }
  }

  /**
   * see if too many members are being removed and if we should abort this job
   * @param originalGroupSize
   * @param membersToRemoveSize
   * @param memberToAddSize
   * @return true if should abort
   */
  public boolean shouldAbortDueToTooManyMembersRemoved(int originalGroupSize, int membersToRemoveSize, int memberToAddSize) {
    
    //maybe dont use this feature
    if (!this.useFailsafe) {
      return false;
    }

    // if approved, dont abort
    if (GrouperFailsafe.isApproved(jobName)) {
      return false;
    }
    
    //if not above the min size then dont check
    if (this.minGroupSize != -1 && originalGroupSize < this.minGroupSize) {
      return false;
    }
    
    //see if max percent removed
    if (this.maxGroupPercentRemove != -1 && ((membersToRemoveSize * 100)/originalGroupSize)  > this.maxGroupPercentRemove) {
      return true;
    }
    
    //see if group size is too small
    if (this.minGroupNumberOfMembers != null && (originalGroupSize + memberToAddSize - membersToRemoveSize) < this.minGroupNumberOfMembers ) {
      return true;
    }
    
    return false;
  }
  
  /**
   * group list failsafe
   * @param originalTotalMembershipSize
   * @param overallMembersToRemoveCount
   * @param overallMembersToAddCount
   * @return true if should abort or false if not
   */
  public boolean shouldAbortDueToTooManyOverallMembersRemoved(int originalManagedGroupsWithMembersCount, int originalTotalMembershipSize, int overallMembersToRemoveCount, int overallMembersToAddCount) {
    //maybe dont use this feature
    if (!this.useFailsafe) {
      return false;
    }
    
    // if approved, dont abort
    if (GrouperFailsafe.isApproved(jobName)) {
      return false;
    }

    //must be a group of a minimum size, and not so many members removed
    if (this.minManagedGroups != null && originalManagedGroupsWithMembersCount < this.minManagedGroups) {
      return false;
    }

    //see if max percent removed
    if (this.maxOverallPercentMembershipsRemove != -1 && ((overallMembersToRemoveCount * 100)/originalTotalMembershipSize)  > this.maxOverallPercentMembershipsRemove) {
      return true;
    }
    
    //see if group size is too small
    if (this.minOverallNumberOfMembers != null && (originalTotalMembershipSize + overallMembersToAddCount - overallMembersToRemoveCount) < this.minOverallNumberOfMembers ) {
      return true;
    }
    
    return false;
    
  }
  
  /**
   * Group list fail safe.
   * See if too many groups managed by the loader via grouperLoaderGroupsLike used to have members but now all would be deleted.
   * @param originalManagedGroupsWithMembersCount
   * @param groupsBeingClearedCount
   * @return true if should abort
   */
  public boolean shouldAbortDueToTooManyGroupListManagedGroupsBeingCleared(int originalManagedGroupsWithMembersCount, 
      int groupsBeingClearedCount) {
    
    //maybe dont use this feature
    if (!this.useFailsafe) {
      return false;
    }
    
    // if approved, dont abort
    if (GrouperFailsafe.isApproved(jobName)) {
      return false;
    }

    //must be a group of a minimum size, and not so many members removed
    if (this.minManagedGroups != null && originalManagedGroupsWithMembersCount < this.minManagedGroups) {
      return false;
    }
    
    if (this.maxOverallPercentGroupsRemove != -1 && ((groupsBeingClearedCount * 100)/originalManagedGroupsWithMembersCount) > this.maxOverallPercentGroupsRemove) {
      return true;
    }
    
    return false;
  }

  /**
   * send email about failsafe (assuming there was a problem)
   */
  public boolean notifyEmailAboutFailsafe() {
    
    //  # you can use the variables $jobName$
    //  # {valueType: "string"}
    //  loader.failsafe.email.subject = Grouper failsafe caused job to not run: $jobName$
    if (!this.useFailsafe || !this.sendEmail) {
      return false;
    }
    
    if (StringUtils.isBlank(this.jobName)) {
      throw new RuntimeException("jobName is blank");
    }
    
    String subject = GrouperConfig.retrieveConfig().propertyValueString("loader.failsafe.email.subject", "Grouper failsafe caused job to not run: $jobName$");
    subject = GrouperUtil.replace(subject, "$jobName$", this.jobName);

    if (StringUtils.isBlank(subject)) {
      return false;
    }

    //  # you can use the variables $newline$, $jobName$.
    //  # {valueType: "string"}
    //  loader.failsafe.email.body = Hello,$newline$$newline$This is a notification that Grouper job $jobName$ did not run due to a failsafe condition.  Approve the failsafe in the UI if this is expected.$newline$$newline$${edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertyValueString("grouper.ui.url")}$newline$$newline$Regards.
    String body = GrouperConfig.retrieveConfig().propertyValueString("loader.failsafe.email.body", "Hello,$newline$$newline$This is a notification that Grouper job $jobName$ did not run due to a failsafe condition.  Approve the failsafe in the UI if this is expected.$newline$$newline$${edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertyValueString(\"grouper.ui.url\")}$newline$$newline$Regards.");
    body = GrouperUtil.replace(body, "$newline$", "\n");
    body = GrouperUtil.replace(body, "$jobName$", this.jobName);

    if (StringUtils.isBlank(body)) {
      return false;
    }

    String emailAddresses = this.retrieveEmailAddressesFromAddressesAndOrGroupName();
    if (StringUtils.isBlank(emailAddresses)) {
      return false;
    }
    
    new GrouperEmail().setTo(emailAddresses).setSubject(subject).setBody(body).send();
    return true;
  }

  /**
   * take the email list and the group name to email and combine into one list
   * @return the email list
   */
  public String retrieveEmailAddressesFromAddressesAndOrGroupName() {
    StringBuilder emails = new StringBuilder();
    if (!StringUtils.isBlank(this.emailAddresses)) {
      emails.append(GrouperUtil.stripLastSlashIfExists(this.emailAddresses));
    }
    if (!StringUtils.isBlank(this.emailGroupName)) {
      Set<String> emailAddresses = GrouperEmail.retrieveEmailAddresses(this.emailGroupName, false, false);
      if (GrouperUtil.length(emailAddresses) > 0) {
        for (String emailAddress : emailAddresses) {
          if (emails.length() > 0) {
            emails.append(",");
          }
          emails.append(emailAddress);
        }
      }
    }
    return emails.toString();
  }
  
  /**
   * send email to this group name
   */
  private String emailGroupName;
  
  
  /**
   * send email to this group name
   * @return
   */
  public String getEmailGroupName() {
    return emailGroupName;
  }

  /**
   * send email to this group name
   * @param emailGroupName
   */
  public void setEmailGroupName(String emailGroupName) {
    this.emailGroupName = emailGroupName;
  }

  /**
   * email addresses to send failsafe errors to
   */
  private String emailAddresses;
    
  /**
   * email addresses to send failsafe errors to
   * @return
   */
  public String getEmailAddresses() {
    return emailAddresses;
  }

  /**
   * email addresses to send failsafe errors to
   * @param failsafeEmailAddresses
   */
  public void setEmailAddresses(String failsafeEmailAddresses) {
    this.emailAddresses = failsafeEmailAddresses;
  }

  /**
   * if the override is not null then override the config default
   * T or F if using failsafe.  If blank use the global defaults
   * @param theUseFailsafe
   */
  public void assignEmailAddressesOverride(String emailAddressesOverride, String emailGroupNameOverride) {
    // if overriding one, override both
    if (!StringUtils.isBlank(emailAddressesOverride) || !StringUtils.isBlank(emailGroupNameOverride)) {
      this.emailAddresses = emailAddressesOverride;
      this.emailGroupName = emailGroupNameOverride;
    }
  }
  

  /**
   * T or F if using failsafe.  If blank use the global defaults
   */
  private boolean useFailsafe = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("loader.failsafe.use", false);

  
  /**
   * T or F if using failsafe.  If blank use the global defaults
   * @return
   */
  public boolean getUseFailsafe() {
    return useFailsafe;
  }

  /**
   * if the override is not null then override the config default
   * T or F if using failsafe.  If blank use the global defaults
   * @param theUseFailsafe
   */
  public void assignUseFailsafeOverride(Boolean theUseFailsafe) {
    if (theUseFailsafe != null) {
      this.useFailsafe = theUseFailsafe;
    }
  }
  
  /**
   * if the override is not null then override the config default
   * T or F if using failsafe.  If blank use the global defaults
   * @param theSendEmailOveride
   */
  public void assignSendEmailOverride(Boolean theSendEmailOveride) {
    if (theSendEmailOveride != null) {
      this.sendEmail = theSendEmailOveride;
    }
  }
  
  /**
   * integer from 0 to 100 which specifies the maximum percent of a group which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.maxPercentRemove = 30
   * @param theMaxPercentRemoveOverride
   */
  public void assignMaxGroupPercentRemoveOverride(Integer theMaxPercentRemoveOverride) {
    if (theMaxPercentRemoveOverride != null) {
      this.maxGroupPercentRemove = theMaxPercentRemoveOverride;
    }
  }
  
  /**
   * T or F if using failsafe.  If blank use the global defaults
   * @param failsafeUse
   */
  public void setUseFailsafe(boolean failsafeUse) {
    this.useFailsafe = failsafeUse;
  }

  /**
   * If an email should be sent out when a failsafe alert happens.
   * The email will be sent to the list or group configured in grouper-loader.properties:
   * loader.failsafe.sendEmailToAddresses, or loader.failsafe.sendEmailToGroup 
   */
  private boolean sendEmail = false;
  
  /**
   * If an email should be sent out when a failsafe alert happens.
   * The email will be sent to the list or group configured in grouper-loader.properties:
   * loader.failsafe.sendEmailToAddresses, or loader.failsafe.sendEmailToGroup 
   * @return send
   */
  public boolean isSendEmail() {
    return sendEmail;
  }

  /**
   * If an email should be sent out when a failsafe alert happens.
   * The email will be sent to the list or group configured in grouper-loader.properties:
   * loader.failsafe.sendEmailToAddresses, or loader.failsafe.sendEmailToGroup 
   * @param failsafeSendEmail
   */
  public void setSendEmail(boolean failsafeSendEmail) {
    this.sendEmail = failsafeSendEmail;
  }

  /**
   * integer from 0 to 100 which specifies the maximum percent of a group which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.maxPercentRemove = 30
   */
  private int maxGroupPercentRemove = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.failsafe.maxPercentRemove", 30);
  
  /**
   * integer from 0 to 100 which specifies the maximum percent of a group which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.maxPercentRemove = 30
   * @return max
   */
  public int getMaxGroupPercentRemove() {
    return maxGroupPercentRemove;
  }

  /**
   * integer from 0 to 100 which specifies the maximum percent of a group which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.maxPercentRemove = 30
   * @param maxGroupPercentRemove
   */
  public void setMaxGroupPercentRemove(int maxGroupPercentRemove) {
    this.maxGroupPercentRemove = maxGroupPercentRemove;
  }

  /**
   * If the group list meets the criteria above and the percentage of memberships that are managed by
   * the loader (i.e. match the groupLikeString) that currently have members in Grouper but 
   * wouldn't after the job runs is greater than this percentage, then don't remove members,
   * log it as an error and fail the job.  An admin would need to approve the failsafe or change this param in the config,
   * and run the job manually, then change this config back.
   * loader.failsafe.groupList.managedGroups.maxPercentMembershipsRemove
   */
  private int maxOverallPercentMembershipsRemove = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.failsafe.groupList.managedGroups.maxPercentMembershipsRemove", 30);
  
  /**
   * If the group list meets the criteria above and the percentage of memberships that are managed by
   * the loader (i.e. match the groupLikeString) that currently have members in Grouper but 
   * wouldn't after the job runs is greater than this percentage, then don't remove members,
   * log it as an error and fail the job.  An admin would need to approve the failsafe or change this param in the config,
   * and run the job manually, then change this config back.
   * loader.failsafe.groupList.managedGroups.maxPercentMembershipsRemove
   * @return
   */
  public int getMaxOverallPercentMembershipsRemove() {
    return maxOverallPercentMembershipsRemove;
  }

  /**
   * If the group list meets the criteria above and the percentage of memberships that are managed by
   * the loader (i.e. match the groupLikeString) that currently have members in Grouper but 
   * wouldn't after the job runs is greater than this percentage, then don't remove members,
   * log it as an error and fail the job.  An admin would need to approve the failsafe or change this param in the config,
   * and run the job manually, then change this config back.
   * loader.failsafe.groupList.managedGroups.maxPercentMembershipsRemove
   * @param maxOverallPercentMembershipsRemove
   */
  public void setMaxOverallPercentMembershipsRemove(
      int maxOverallPercentMembershipsRemove) {
    this.maxOverallPercentMembershipsRemove = maxOverallPercentMembershipsRemove;
  }

  /**
   * integer from 0 to 100 which specifies the maximum percent of all loaded groups in the job
   * which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.groupList.managedGroups.maxPercentGroupsRemove = 30
   */
  private int maxOverallPercentGroupsRemove = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.failsafe.groupList.managedGroups.maxPercentGroupsRemove", 30);
  
  /**
   * integer from 0 to 100 which specifies the maximum percent of all loaded groups in the job
   * which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.groupList.managedGroups.maxPercentGroupsRemove = 30
   * @return max
   */
  public int getMaxOverallPercentGroupsRemove() {
    return maxOverallPercentGroupsRemove;
  }

  /**
   * integer from 0 to 100 which specifies the maximum percent of all loaded groups in the job
   * which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.groupList.managedGroups.maxPercentGroupsRemove = 30
   * @param maxOverallPercentGroupsRemove
   */
  public void setMaxOverallPercentGroupsRemove(int maxOverallPercentGroupsRemove) {
    this.maxOverallPercentGroupsRemove = maxOverallPercentGroupsRemove;
  }

  /**
   * minimum number of members for the group to be tracked by failsafe
   * defaults to grouper-loader.base.properties: loader.failsafe.minGroupSize
   */
  private int minGroupSize = GrouperLoaderConfig.retrieveConfig().propertyValueInt("loader.failsafe.minGroupSize", 200);
  
  /**
   * minimum number of members for the group to be tracked by failsafe
   * defaults to grouper-loader.base.properties: loader.failsafe.minGroupSize
   * @return
   */
  public int getMinGroupSize() {
    return minGroupSize;
  }

  /**
   * minimum number of members for the group to be tracked by failsafe
   * defaults to grouper-loader.base.properties: loader.failsafe.minGroupSize
   * @param minGroupSize
   */
  public void setMinGroupSize(int minGroupSize) {
    this.minGroupSize = minGroupSize;
  }

  /**
   * The minimum number of managed groups for this loader job, a failsafe alert will trigger if the number
   * of managed groups is smaller than this amount
   */
  private Integer minManagedGroups;
  
  /**
   * The minimum number of managed groups for this loader job, a failsafe alert will trigger if the number
   * of managed groups is smaller than this amount
   * @return
   */
  public Integer getMinManagedGroups() {
    return minManagedGroups;
  }

  /**
   * The minimum number of managed groups for this loader job, a failsafe alert will trigger if the number
   * of managed groups is smaller than this amount
   * @param minManagedGroups
   */
  public void setMinManagedGroups(Integer minManagedGroups) {
    this.minManagedGroups = minManagedGroups;
  }

  /**
   * The minimum group number of members for this group, a failsafe alert will trigger if the group is smaller than this amount
   */
  private Integer minGroupNumberOfMembers;
  
  /**
   * The minimum group number of members for this group, a failsafe alert will trigger if the group is smaller than this amount
   * @return
   */
  public Integer getMinGroupNumberOfMembers() {
    return minGroupNumberOfMembers;
  }

  /**
   * The minimum group number of members for this group, a failsafe alert will trigger if the group is smaller than this amount
   * @param minGroupNumberOfMembers
   */
  public void setMinGroupNumberOfMembers(Integer minGroupNumberOfMembers) {
    this.minGroupNumberOfMembers = minGroupNumberOfMembers;
  }

  /**
   * The minimum overall number of members for this job across all managed groups, 
   * a failsafe alert will trigger if the job's overall membership count is smaller than this amount
   */
  private Integer minOverallNumberOfMembers;
  
  /**
   * The minimum overall number of members for this job across all managed groups, 
   * a failsafe alert will trigger if the job's overall membership count is smaller than this amount
   * @return
   */
  public Integer getMinOverallNumberOfMembers() {
    return minOverallNumberOfMembers;
  }

  /**
   * The minimum overall number of members for this job across all managed groups, 
   * a failsafe alert will trigger if the job's overall membership count is smaller than this amount
   * @param minOverallNumberOfMembers
   */
  public void setMinOverallNumberOfMembers(Integer minOverallNumberOfMembers) {
    this.minOverallNumberOfMembers = minOverallNumberOfMembers;
  }

  /**
   * integer from 0 to 100 which specifies the maximum percent of all loaded groups in the job
   * which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.groupList.managedGroups.maxPercentGroupsRemove = 30
   * @param theMaxOverallPercentRemoveOverride
   */
  public void assignMaxOverallPercentGroupsRemoveOverride(Integer theMaxOverallPercentGroupsRemoveOverride) {
    if (theMaxOverallPercentGroupsRemoveOverride != null) {
      this.maxOverallPercentGroupsRemove = theMaxOverallPercentGroupsRemoveOverride.intValue();
    }
  }
  
  /**
   * integer from 0 to 100 which specifies the maximum percent of all loaded groups in the job
   * which can be removed in a loader run.
   * If not specified will use the global default grouper-loader.properties config setting:
   * loader.failsafe.groupList.managedGroups.maxPercentGroupsRemove = 30
   * @param theMaxOverallPercentRemoveOverride
   */
  public void assignMaxOverallPercentMembershipsRemoveOverride(Integer theMaxOverallPercentMembershipsRemoveOverride) {
    if (theMaxOverallPercentMembershipsRemoveOverride != null) {
      this.maxOverallPercentMembershipsRemove = theMaxOverallPercentMembershipsRemoveOverride.intValue();
    }
  }
  
  /**
   * minimum number of members for the group to be tracked by failsafe
   * defaults to grouper-loader.base.properties: loader.failsafe.minGroupSize
   * @param theMinGroupSizeOverride
   */
  public void assignMinGroupSizeOverride(Integer theMinGroupSizeOverride) {
    if (theMinGroupSizeOverride != null) {
      this.minGroupSize = theMinGroupSizeOverride;
    }
  }
  
  /**
   * The minimum number of managed groups for this loader job, a failsafe alert will trigger if the number
   * of managed groups is smaller than this amount
   * @param theMinManagedGroupsOverride
   */
  public void assignMinManagedGroupsOverride(Integer theMinManagedGroupsOverride) {
    if (theMinManagedGroupsOverride != null) {
      this.minManagedGroups = theMinManagedGroupsOverride;
    }
  }

  /**
   * The minimum group number of members for this group, a failsafe alert will trigger if the group is smaller than this amount
   * @param theMinGroupNumberOfMembers
   */
  public void assignMinGroupNumberOfMembersOverride(Integer theMinGroupNumberOfMembers) {
    if (theMinGroupNumberOfMembers != null) {
      this.minGroupNumberOfMembers = theMinGroupNumberOfMembers;
    }
  }

  /**
   * The minimum overall number of members for this job across all managed groups, 
   * a failsafe alert will trigger if the job's overall membership count is smaller than this amount
   * @param theMinOverallNumberOfMembers
   */
  public void assignMinOverallNumberOfMembersOverride(Integer theMinOverallNumberOfMembers) {
    if (theMinOverallNumberOfMembers != null) {
      this.minOverallNumberOfMembers = theMinOverallNumberOfMembers;
    }
  }

  /**
   * job name for this failsafe (note, it could be a subjob name)
   */
  private String jobName;


  /**
   * job name for this failsafe (note, it could be a subjob name)
   * @return
   */
  public String getJobName() {
    return jobName;
  }

  /**
   * job name for this failsafe (note, it could be a subjob name)
   * @param jobName
   */
  public void setJobName(String jobName) {
    this.jobName = jobName;
  }
  
  
  
}
