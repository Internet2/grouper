package edu.internet2.middleware.grouper.app.dropbox;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Provisioner-specific configuration for the Dropbox provisioner, read from
 * grouper-loader.properties (provisioner.&lt;configId&gt;.*).
 */
public class DropboxProvisionerConfiguration extends GrouperProvisioningConfiguration {

  /**
   * required: the external system config id (WsBearerToken) holding the Dropbox
   * team API bearer token
   */
  private String dropboxExternalSystemConfigId;

  /**
   * optional: Grouper folder name (path) whose child groups are admin-role groups.
   * Each such group's extension must be one of the 8 Dropbox admin role names
   * (Team_Admin, User_Management_Admin, ...).  Membership in these groups drives
   * each member's Dropbox admin role (highest tier wins).  If BLANK, the provisioner
   * does not pull or manage admin roles at all.
   */
  private String dropboxAdminRoleFolderName;

  /**
   * optional: Grouper folder name (path) whose child groups are lifecycle-state markers.
   * Recognized child group extensions: "Suspended" (member is suspended rather than fully
   * active) and "Downgrade" (on deprovision the member is converted to a free Basic account
   * with keep_account=true instead of being deleted).  If BLANK, the provisioner does not
   * manage suspend/downgrade lifecycle states.
   */
  private String dropboxLifecycleFolderName;

  /**
   * optional: the subject-source attribute name holding the address to set on the account just before
   * a downgrade (e.g. an alumni forwarding address), resolved from the member's subject at delete time
   * (a departing entity is no longer translated, so this is read from the subject, not a provisioner
   * attribute).  If blank, a downgrade keeps the member's current email.
   */
  private String dropboxDowngradeEmailSubjectAttribute;

  /**
   * comma-separated list of member emails to ignore during provisioning.
   * these members are filtered out of retrieve operations and never created,
   * updated, or deleted.
   */
  private String dropboxIgnoreUserEmails;

  /**
   * comma-separated list of group names to ignore during provisioning.
   * these groups are filtered out of retrieve operations and never created,
   * updated, or deleted.
   */
  private String dropboxIgnoreGroupNames;

  /**
   * whether to wipe the member's Dropbox data when removing them from the team.
   * defaults to true (standard offboarding).
   */
  private boolean dropboxWipeDataOnRemove;

  /**
   * whether to keep the account (convert to a personal Basic account) when removing
   * a member rather than fully deleting.  defaults to false.
   */
  private boolean dropboxKeepAccountOnRemove;

  public String getDropboxExternalSystemConfigId() {
    return dropboxExternalSystemConfigId;
  }

  public void setDropboxExternalSystemConfigId(String dropboxExternalSystemConfigId) {
    this.dropboxExternalSystemConfigId = dropboxExternalSystemConfigId;
  }

  public String getDropboxAdminRoleFolderName() {
    return dropboxAdminRoleFolderName;
  }

  public void setDropboxAdminRoleFolderName(String dropboxAdminRoleFolderName) {
    this.dropboxAdminRoleFolderName = dropboxAdminRoleFolderName;
  }

  public String getDropboxIgnoreUserEmails() {
    return dropboxIgnoreUserEmails;
  }

  public void setDropboxIgnoreUserEmails(String dropboxIgnoreUserEmails) {
    this.dropboxIgnoreUserEmails = dropboxIgnoreUserEmails;
  }

  public String getDropboxIgnoreGroupNames() {
    return dropboxIgnoreGroupNames;
  }

  public void setDropboxIgnoreGroupNames(String dropboxIgnoreGroupNames) {
    this.dropboxIgnoreGroupNames = dropboxIgnoreGroupNames;
  }

  public boolean isDropboxWipeDataOnRemove() {
    return dropboxWipeDataOnRemove;
  }

  public void setDropboxWipeDataOnRemove(boolean dropboxWipeDataOnRemove) {
    this.dropboxWipeDataOnRemove = dropboxWipeDataOnRemove;
  }

  public boolean isDropboxKeepAccountOnRemove() {
    return dropboxKeepAccountOnRemove;
  }

  public void setDropboxKeepAccountOnRemove(boolean dropboxKeepAccountOnRemove) {
    this.dropboxKeepAccountOnRemove = dropboxKeepAccountOnRemove;
  }

  public String getDropboxLifecycleFolderName() {
    return dropboxLifecycleFolderName;
  }

  public void setDropboxLifecycleFolderName(String dropboxLifecycleFolderName) {
    this.dropboxLifecycleFolderName = dropboxLifecycleFolderName;
  }

  public String getDropboxDowngradeEmailSubjectAttribute() {
    return dropboxDowngradeEmailSubjectAttribute;
  }

  public void setDropboxDowngradeEmailSubjectAttribute(String dropboxDowngradeEmailSubjectAttribute) {
    this.dropboxDowngradeEmailSubjectAttribute = dropboxDowngradeEmailSubjectAttribute;
  }

  /**
   * @return true if admin-role management is enabled (the admin-role folder is configured)
   */
  public boolean isManageAdminRoles() {
    return !GrouperUtil.isBlank(this.dropboxAdminRoleFolderName);
  }

  /**
   * @return true if lifecycle (suspend / downgrade) management is enabled (the lifecycle folder is configured)
   */
  public boolean isManageLifecycle() {
    return !GrouperUtil.isBlank(this.dropboxLifecycleFolderName);
  }

  @Override
  public void configureSpecificSettings() {

    this.dropboxExternalSystemConfigId = this.retrieveConfigString("dropboxExternalSystemConfigId", true);
    this.dropboxAdminRoleFolderName = this.retrieveConfigString("dropboxAdminRoleFolderName", false);
    this.dropboxLifecycleFolderName = this.retrieveConfigString("dropboxLifecycleFolderName", false);
    this.dropboxDowngradeEmailSubjectAttribute = this.retrieveConfigString("dropboxDowngradeEmailSubjectAttribute", false);
    this.dropboxIgnoreUserEmails = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("dropboxIgnoreUserEmails", false), "");
    this.dropboxIgnoreGroupNames = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("dropboxIgnoreGroupNames", false), "");
    this.dropboxWipeDataOnRemove = GrouperUtil.booleanValue(
        this.retrieveConfigString("dropboxWipeDataOnRemove", false), true);
    this.dropboxKeepAccountOnRemove = GrouperUtil.booleanValue(
        this.retrieveConfigString("dropboxKeepAccountOnRemove", false), false);

  }

}
