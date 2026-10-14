package edu.internet2.middleware.grouper.app.smartsheet;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Runtime configuration for the Smartsheet provisioner. Values are read from
 * grouper-loader.properties under provisioner.&lt;configId&gt;.* by
 * {@link #configureSpecificSettings()}.
 */
public class SmartsheetProvisionerConfiguration extends GrouperProvisioningConfiguration {

  /**
   * required: the external system config id (a WsBearerToken external system) that holds the
   * Smartsheet API base URL (e.g. https://api.smartsheet.com) and the API access token of a
   * System Admin.
   */
  private String smartsheetExternalSystemConfigId;

  /**
   * comma-separated list of user emails to ignore. Ignored users are filtered out of reads, and
   * are never created, updated, deactivated, removed, or added to / removed from a group.
   */
  private String smartsheetIgnoreUserEmails;

  /**
   * comma-separated list of group names to ignore. Ignored groups are filtered out of reads, and
   * are never updated, deleted, or have their members changed.
   */
  private String smartsheetIgnoreGroupNames;

  /**
   * when true (default), a "delete" deactivates the user (POST /users/{id}/deactivate) instead of
   * removing them from the organization, deactivated users are filtered out of reads, and an insert
   * of the same person reactivates the user rather than adding a duplicate.
   */
  private boolean disableEntitiesInsteadOfDelete;

  /**
   * when Grouper adds a user to the organization, whether Smartsheet should email them
   * (the sendEmail query param). Defaults to false.
   */
  private boolean smartsheetSendEmailOnAddUser;

  /**
   * when removing a user from the organization (disableEntitiesInsteadOfDelete=false), the
   * Smartsheet user id who receives ownership of the removed user's groups and sheets
   * (transferTo + transferSheets). Blank means no transfer.
   */
  private String smartsheetRemoveUserTransferToUserId;

  /**
   * when removing a user from the organization, also remove their access to all assets in the
   * organization (removeFromSharing). Defaults to false.
   */
  private boolean smartsheetRemoveUserFromSharing;

  /**
   * number of retries after the first attempt when Smartsheet answers 429 (rate limited,
   * errorCode 4003). Defaults to 5.
   */
  private int smartsheetRetryCount;

  /**
   * milliseconds to wait before the first rate-limit retry; doubles on each further retry, capped
   * at 60 seconds. Defaults to 2000.
   */
  private int smartsheetRetrySleepMillis;

  public String getSmartsheetExternalSystemConfigId() {
    return smartsheetExternalSystemConfigId;
  }

  public void setSmartsheetExternalSystemConfigId(String smartsheetExternalSystemConfigId) {
    this.smartsheetExternalSystemConfigId = smartsheetExternalSystemConfigId;
  }

  public String getSmartsheetIgnoreUserEmails() {
    return smartsheetIgnoreUserEmails;
  }

  public void setSmartsheetIgnoreUserEmails(String smartsheetIgnoreUserEmails) {
    this.smartsheetIgnoreUserEmails = smartsheetIgnoreUserEmails;
  }

  public String getSmartsheetIgnoreGroupNames() {
    return smartsheetIgnoreGroupNames;
  }

  public void setSmartsheetIgnoreGroupNames(String smartsheetIgnoreGroupNames) {
    this.smartsheetIgnoreGroupNames = smartsheetIgnoreGroupNames;
  }

  public boolean isDisableEntitiesInsteadOfDelete() {
    return disableEntitiesInsteadOfDelete;
  }

  public void setDisableEntitiesInsteadOfDelete(boolean disableEntitiesInsteadOfDelete) {
    this.disableEntitiesInsteadOfDelete = disableEntitiesInsteadOfDelete;
  }

  public boolean isSmartsheetSendEmailOnAddUser() {
    return smartsheetSendEmailOnAddUser;
  }

  public void setSmartsheetSendEmailOnAddUser(boolean smartsheetSendEmailOnAddUser) {
    this.smartsheetSendEmailOnAddUser = smartsheetSendEmailOnAddUser;
  }

  public String getSmartsheetRemoveUserTransferToUserId() {
    return smartsheetRemoveUserTransferToUserId;
  }

  public void setSmartsheetRemoveUserTransferToUserId(String smartsheetRemoveUserTransferToUserId) {
    this.smartsheetRemoveUserTransferToUserId = smartsheetRemoveUserTransferToUserId;
  }

  public boolean isSmartsheetRemoveUserFromSharing() {
    return smartsheetRemoveUserFromSharing;
  }

  public void setSmartsheetRemoveUserFromSharing(boolean smartsheetRemoveUserFromSharing) {
    this.smartsheetRemoveUserFromSharing = smartsheetRemoveUserFromSharing;
  }

  public int getSmartsheetRetryCount() {
    return smartsheetRetryCount;
  }

  public void setSmartsheetRetryCount(int smartsheetRetryCount) {
    this.smartsheetRetryCount = smartsheetRetryCount;
  }

  public int getSmartsheetRetrySleepMillis() {
    return smartsheetRetrySleepMillis;
  }

  public void setSmartsheetRetrySleepMillis(int smartsheetRetrySleepMillis) {
    this.smartsheetRetrySleepMillis = smartsheetRetrySleepMillis;
  }

  @Override
  public void configureSpecificSettings() {

    this.smartsheetExternalSystemConfigId = this.retrieveConfigString("smartsheetExternalSystemConfigId", true);

    this.smartsheetIgnoreUserEmails = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("smartsheetIgnoreUserEmails", false), "");

    this.smartsheetIgnoreGroupNames = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("smartsheetIgnoreGroupNames", false), "");

    // default true: removing a Smartsheet user needs a sheet-ownership transfer and cannot be
    // undone, so the softer deactivate is the safe default
    this.disableEntitiesInsteadOfDelete = GrouperUtil.booleanValue(
        this.retrieveConfigBoolean("disableEntitiesInsteadOfDelete", false), true);

    this.smartsheetSendEmailOnAddUser = GrouperUtil.booleanValue(
        this.retrieveConfigBoolean("smartsheetSendEmailOnAddUser", false), false);

    this.smartsheetRemoveUserTransferToUserId = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("smartsheetRemoveUserTransferToUserId", false), "");

    this.smartsheetRemoveUserFromSharing = GrouperUtil.booleanValue(
        this.retrieveConfigBoolean("smartsheetRemoveUserFromSharing", false), false);

    this.smartsheetRetryCount = Math.min(10, Math.max(0, GrouperUtil.intValue(
        this.retrieveConfigInt("smartsheetRetryCount", false), 5)));

    this.smartsheetRetrySleepMillis = Math.max(0, GrouperUtil.intValue(
        this.retrieveConfigInt("smartsheetRetrySleepMillis", false), 2000));
  }

}
