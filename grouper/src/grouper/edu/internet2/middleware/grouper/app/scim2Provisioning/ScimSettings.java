package edu.internet2.middleware.grouper.app.scim2Provisioning;


import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.util.GrouperUtil;

public class ScimSettings {

  /**
   * Update-conflict channel between the low-level api commands and the target dao.  When a PATCH to
   * rename a user collides with a different, already-existing target user (HTTP 409 scimType
   * "uniqueness"), patchScimUser cannot rename the linked account, but the target already holds the
   * desired identity on that other user.  Rather than throw (which would fail the entity every run
   * and log an error storm), the commands layer stashes {oldTargetId -> the existing colliding user}
   * here.  The dao drains this after the call and hands it to the generic framework to re-link the
   * entity to the existing user and dispose of the now-orphaned old account per provisioner settings.
   * scimSettings is created fresh per dao call, so this map round-trips within a single update only.
   * Keyed by the old (linked) target id so the dao can correlate to the entity it just patched.
   */
  private Map<String, GrouperScim2User> updateConflictOldTargetIdToExistingUser = new LinkedHashMap<String, GrouperScim2User>();

  /**
   * record a rename-uniqueness conflict for the dao to drain: the old linked target id and the
   * pre-existing target user that already holds the desired identity.
   * @param oldTargetId the target id of the currently-linked account we failed to rename
   * @param existingUser the already-existing target user that owns the desired userName/email
   */
  public void addUpdateConflict(String oldTargetId, GrouperScim2User existingUser) {
    this.updateConflictOldTargetIdToExistingUser.put(oldTargetId, existingUser);
  }

  /**
   * @return the stashed rename-uniqueness conflicts, keyed by old linked target id (never null)
   */
  public Map<String, GrouperScim2User> getUpdateConflictOldTargetIdToExistingUser() {
    return this.updateConflictOldTargetIdToExistingUser;
  }


  public void loadFromScimProvisionerConfiguration(GrouperScim2ProvisionerConfiguration scimConfiguration) {
    String scimNamePatchStrategy = scimConfiguration.getScimNamePatchStrategy();
    
    this.setScimNamePatchStrategy(scimNamePatchStrategy);
    this.setScimEmailPatchStrategy(scimConfiguration.getScimEmailPatchStrategy());
    this.setAcceptHeader(scimConfiguration.getAcceptHeader());
    this.setScimContentType(scimConfiguration.getScimContentType());
    this.setScimIgnorePagingMetadata(scimConfiguration.isScimIgnorePagingMetadata());
    this.setScimMembershipBatchSize(scimConfiguration.getScimMembershipBatchSize());
    this.setScimEmailFilterStrategy(scimConfiguration.getScimEmailFilterStrategy());
  }
  
  private int scimMembershipBatchSize = 100;
  
  private String orgName;
  
  private String scimNamePatchStrategy = "nonqualified";
  
  private String scimEmailPatchStrategy = "pathEmails";
  
  private String acceptHeader;

  private String scimContentType = "application/json";
  
  private boolean scimIgnorePagingMetadata = false;

  private String scimEmailFilterStrategy = "email";


  public int getScimMembershipBatchSize() {
    return scimMembershipBatchSize;
  }

  
  public void setScimMembershipBatchSize(int scimMembershipBatchSize) {
    this.scimMembershipBatchSize = scimMembershipBatchSize;
  }

  public boolean isScimIgnorePagingMetadata() {
    return scimIgnorePagingMetadata;
  }
  
  public void setScimIgnorePagingMetadata(boolean scimIgnoreTotalResults) {
    this.scimIgnorePagingMetadata = scimIgnoreTotalResults;
  }


  public String getOrgName() {
    return orgName;
  }

  
  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  
  public String getScimNamePatchStrategy() {
    return scimNamePatchStrategy;
  }

  
  public void setScimNamePatchStrategy(String scimNamePatchStrategy) {
    if (StringUtils.isBlank(scimNamePatchStrategy)) {
      scimNamePatchStrategy = "nonqualified";
    }
    GrouperUtil.assertion(StringUtils.equalsAny(scimNamePatchStrategy, "nonqualified", "qualified", "nested"), "scimNamePatchStrategy needs to be 'qualified', 'nonqualified', or 'nested'. You provided: '"+scimNamePatchStrategy+"'");
    this.scimNamePatchStrategy = scimNamePatchStrategy;
  }

  public String getAcceptHeader() {
    return acceptHeader;
  }

  public void setAcceptHeader(String acceptHeader) {
    this.acceptHeader = acceptHeader;
  }

  public String getScimContentType() {
    return scimContentType;
  }

  public void setScimContentType(String scimContentType) {
    if (StringUtils.isBlank(scimContentType)) {
      scimContentType = "application/json";
    }
    this.scimContentType = scimContentType;
  }


  
  public String getScimEmailPatchStrategy() {
    return scimEmailPatchStrategy;
  }


  
  public void setScimEmailPatchStrategy(String scimEmailPatchStrategy) {
    if (StringUtils.isBlank(scimEmailPatchStrategy)) {
      scimEmailPatchStrategy = "pathEmails";
    }
    GrouperUtil.assertion(StringUtils.equalsAny(scimEmailPatchStrategy, "pathEmails", "noPath", "pathEmailsQualified"), "scimEmailPatchStrategy needs to be 'pathEmails' or 'noPath' or 'pathEmailsQualified'. You provided: '"+scimEmailPatchStrategy+"'");
    this.scimEmailPatchStrategy = scimEmailPatchStrategy;
  }

  public String getScimEmailFilterStrategy() {
    return scimEmailFilterStrategy;
  }

  public void setScimEmailFilterStrategy(String scimEmailFilterStrategy) {
    if (StringUtils.isBlank(scimEmailFilterStrategy)) {
      scimEmailFilterStrategy = "email";
    }
    GrouperUtil.assertion(StringUtils.equalsAny(scimEmailFilterStrategy, "email", "emails.value", "emails[value]", "emails[typeWork and value]"), "scimEmailFilterStrategy needs to be 'email', 'emails.value', 'emails[value]', or 'emails[typeWork and value]'. You provided: '"+scimEmailFilterStrategy+"'");
    this.scimEmailFilterStrategy = scimEmailFilterStrategy;
  }

}
