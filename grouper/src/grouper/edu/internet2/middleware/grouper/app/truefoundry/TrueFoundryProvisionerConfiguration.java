package edu.internet2.middleware.grouper.app.truefoundry;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class TrueFoundryProvisionerConfiguration extends GrouperProvisioningConfiguration {

  /**
   * required: the external system config id (WsBearerToken) for TrueFoundry
   */
  private String trueFoundryExternalSystemConfigId;

  /**
   * default role to assign to users who are not a member of any role group.
   * defaults to "read-only-member"
   */
  private String trueFoundryDefaultRole;

  /**
   * default resourceType to use when assigning the default role.
   * defaults to "account"
   */
  private String trueFoundryDefaultRoleResourceType;

  /**
   * whether to support team manager metadata (a membership metadata attribute
   * that marks a user as a team manager rather than a regular member).
   * defaults to false
   */
  private boolean trueFoundryAddTeamManagerMetadata;

  /**
   * the membership metadata attribute name that marks a user as a team manager.
   * only used when trueFoundryAddTeamManagerMetadata=true.
   * defaults to "md_trueFoundryTeamManager"
   */
  private String trueFoundryTeamManagerMetadataName;

  /**
   * the group metadata attribute name that holds the Grouper group path of the managers group.
   * only used when trueFoundryAddTeamManagerMetadata=true.
   * defaults to "md_trueFoundryManagerGroupName"
   */
  private String trueFoundryManagerGroupMetadataName;

  /**
   * comma-separated list of user emails to ignore during provisioning.
   * these users will be filtered out of retrieve operations and will not be created, updated, or deleted.
   */
  private String trueFoundryIgnoreUserEmails;

  /**
   * comma-separated list of role names to ignore during provisioning.
   * these roles will be filtered out of retrieve operations and will not be created, updated, or deleted.
   */
  private String trueFoundryIgnoreRoles;

  /**
   * TrueFoundry tenant name (e.g. "upenn-prod") — required for SCIM display name updates.
   * if blank, display name updates are skipped
   */
  private String trueFoundryScimTenantName;

  /**
   * TrueFoundry SCIM SSO ID (the ssoId segment in the SCIM URL) — required for SCIM display name updates.
   * if blank, display name updates are skipped
   */
  private String trueFoundryScimSsoId;

  /**
   * email address of the default team member (e.g. a service account).
   * TrueFoundry requires at least one member per team at all times.
   * This email is added when a team is created and kept when all other members are removed.
   */
  private String trueFoundryDefaultTeamMemberEmail;

  public String getTrueFoundryExternalSystemConfigId() {
    return trueFoundryExternalSystemConfigId;
  }

  public void setTrueFoundryExternalSystemConfigId(String trueFoundryExternalSystemConfigId) {
    this.trueFoundryExternalSystemConfigId = trueFoundryExternalSystemConfigId;
  }

  public String getTrueFoundryDefaultRole() {
    return trueFoundryDefaultRole;
  }

  public void setTrueFoundryDefaultRole(String trueFoundryDefaultRole) {
    this.trueFoundryDefaultRole = trueFoundryDefaultRole;
  }

  public String getTrueFoundryDefaultRoleResourceType() {
    return trueFoundryDefaultRoleResourceType;
  }

  public void setTrueFoundryDefaultRoleResourceType(String trueFoundryDefaultRoleResourceType) {
    this.trueFoundryDefaultRoleResourceType = trueFoundryDefaultRoleResourceType;
  }

  public boolean isTrueFoundryAddTeamManagerMetadata() {
    return trueFoundryAddTeamManagerMetadata;
  }

  public void setTrueFoundryAddTeamManagerMetadata(boolean trueFoundryAddTeamManagerMetadata) {
    this.trueFoundryAddTeamManagerMetadata = trueFoundryAddTeamManagerMetadata;
  }

  public String getTrueFoundryTeamManagerMetadataName() {
    return trueFoundryTeamManagerMetadataName;
  }

  public void setTrueFoundryTeamManagerMetadataName(String trueFoundryTeamManagerMetadataName) {
    this.trueFoundryTeamManagerMetadataName = trueFoundryTeamManagerMetadataName;
  }

  public String getTrueFoundryManagerGroupMetadataName() {
    return trueFoundryManagerGroupMetadataName;
  }

  public void setTrueFoundryManagerGroupMetadataName(String trueFoundryManagerGroupMetadataName) {
    this.trueFoundryManagerGroupMetadataName = trueFoundryManagerGroupMetadataName;
  }

  public String getTrueFoundryIgnoreUserEmails() {
    return trueFoundryIgnoreUserEmails;
  }

  public void setTrueFoundryIgnoreUserEmails(String trueFoundryIgnoreUserEmails) {
    this.trueFoundryIgnoreUserEmails = trueFoundryIgnoreUserEmails;
  }

  public String getTrueFoundryIgnoreRoles() {
    return trueFoundryIgnoreRoles;
  }

  public void setTrueFoundryIgnoreRoles(String trueFoundryIgnoreRoles) {
    this.trueFoundryIgnoreRoles = trueFoundryIgnoreRoles;
  }

  public String getTrueFoundryScimTenantName() {
    return trueFoundryScimTenantName;
  }

  public void setTrueFoundryScimTenantName(String trueFoundryScimTenantName) {
    this.trueFoundryScimTenantName = trueFoundryScimTenantName;
  }

  public String getTrueFoundryScimSsoId() {
    return trueFoundryScimSsoId;
  }

  public void setTrueFoundryScimSsoId(String trueFoundryScimSsoId) {
    this.trueFoundryScimSsoId = trueFoundryScimSsoId;
  }

  public String getTrueFoundryDefaultTeamMemberEmail() {
    return trueFoundryDefaultTeamMemberEmail;
  }

  public void setTrueFoundryDefaultTeamMemberEmail(String trueFoundryDefaultTeamMemberEmail) {
    this.trueFoundryDefaultTeamMemberEmail = trueFoundryDefaultTeamMemberEmail;
  }

  /**
   * returns true if SCIM display name updates are configured
   * (both tenantName and ssoId must be set)
   */
  public boolean isScimDisplayNameConfigured() {
    return !GrouperUtil.isBlank(trueFoundryScimTenantName) && !GrouperUtil.isBlank(trueFoundryScimSsoId);
  }

  @Override
  public void configureSpecificSettings() {

    this.trueFoundryExternalSystemConfigId = this.retrieveConfigString("trueFoundryExternalSystemConfigId", true);
    this.trueFoundryDefaultRole = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("trueFoundryDefaultRole", false), "read-only-member");
    this.trueFoundryDefaultRoleResourceType = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("trueFoundryDefaultRoleResourceType", false), "account");
    this.trueFoundryIgnoreUserEmails = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("trueFoundryIgnoreUserEmails", false), "");
    this.trueFoundryIgnoreRoles = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("trueFoundryIgnoreRoles", false), "");
    this.trueFoundryAddTeamManagerMetadata = GrouperUtil.booleanValue(
        this.retrieveConfigString("trueFoundryAddTeamManagerMetadata", false), false);
    this.trueFoundryTeamManagerMetadataName = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("trueFoundryTeamManagerMetadataName", false), "md_trueFoundryTeamManager");
    this.trueFoundryManagerGroupMetadataName = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("trueFoundryManagerGroupMetadataName", false), "md_trueFoundryManagerGroupName");
    this.trueFoundryScimTenantName = this.retrieveConfigString("trueFoundryScimTenantName", false);
    this.trueFoundryScimSsoId = this.retrieveConfigString("trueFoundryScimSsoId", false);
    this.trueFoundryDefaultTeamMemberEmail = this.retrieveConfigString("trueFoundryDefaultTeamMemberEmail", false);

  }

}
