package edu.internet2.middleware.grouper.app.smartsheet;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttribute;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfigurationAttributeValueType;
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

  /**
   * the Smartsheet plan id. Required when seat types are managed (any seat group or a default seat):
   * reading seat types and the upgrade / downgrade calls are per plan.
   */
  private String smartsheetPlanId;

  /** Grouper group whose members get the MEMBER seat (blank: Grouper never assigns MEMBER) */
  private String smartsheetSeatTypeMemberGroupName;

  /** Grouper group whose members get the CONTRIBUTOR seat (blank: Grouper never assigns CONTRIBUTOR from a group) */
  private String smartsheetSeatTypeContributorGroupName;

  /** Grouper group whose members get the GUEST seat, external users only (blank: never assigns GUEST from a group) */
  private String smartsheetSeatTypeGuestGroupName;

  /**
   * seat for a provisioned user who is in none of the seat groups (MEMBER, CONTRIBUTOR, or GUEST).
   * Blank (default): Grouper leaves that user's seat alone.
   */
  private String smartsheetSeatTypeDefault;

  /**
   * whether Grouper may downgrade a PROVISIONAL_MEMBER to CONTRIBUTOR or GUEST. Defaults to false: a
   * user downgraded by the API can never be provisional again, so Grouper leaves provisional
   * members to Smartsheet's own expiry unless this is set.
   */
  private boolean smartsheetDowngradeProvisionalMembers;

  /**
   * account flag entity attribute name to the Grouper group whose members get the flag. Only flags
   * with a group configured are here; Grouper does not manage the others.
   */
  private Map<String, String> flagAttributeToGroupName = new LinkedHashMap<String, String>();

  public String getSmartsheetPlanId() {
    return smartsheetPlanId;
  }

  public void setSmartsheetPlanId(String smartsheetPlanId) {
    this.smartsheetPlanId = smartsheetPlanId;
  }

  public String getSmartsheetSeatTypeMemberGroupName() {
    return smartsheetSeatTypeMemberGroupName;
  }

  public void setSmartsheetSeatTypeMemberGroupName(String smartsheetSeatTypeMemberGroupName) {
    this.smartsheetSeatTypeMemberGroupName = smartsheetSeatTypeMemberGroupName;
  }

  public String getSmartsheetSeatTypeContributorGroupName() {
    return smartsheetSeatTypeContributorGroupName;
  }

  public void setSmartsheetSeatTypeContributorGroupName(String smartsheetSeatTypeContributorGroupName) {
    this.smartsheetSeatTypeContributorGroupName = smartsheetSeatTypeContributorGroupName;
  }

  public String getSmartsheetSeatTypeGuestGroupName() {
    return smartsheetSeatTypeGuestGroupName;
  }

  public void setSmartsheetSeatTypeGuestGroupName(String smartsheetSeatTypeGuestGroupName) {
    this.smartsheetSeatTypeGuestGroupName = smartsheetSeatTypeGuestGroupName;
  }

  public String getSmartsheetSeatTypeDefault() {
    return smartsheetSeatTypeDefault;
  }

  public void setSmartsheetSeatTypeDefault(String smartsheetSeatTypeDefault) {
    this.smartsheetSeatTypeDefault = smartsheetSeatTypeDefault;
  }

  public boolean isSmartsheetDowngradeProvisionalMembers() {
    return smartsheetDowngradeProvisionalMembers;
  }

  public void setSmartsheetDowngradeProvisionalMembers(boolean smartsheetDowngradeProvisionalMembers) {
    this.smartsheetDowngradeProvisionalMembers = smartsheetDowngradeProvisionalMembers;
  }

  /**
   * @return flag attribute name (admin, groupAdmin, licensedSheetCreator, resourceViewer) to group name,
   *   only for flags whose group is configured
   */
  public Map<String, String> getFlagAttributeToGroupName() {
    return flagAttributeToGroupName;
  }

  /**
   * @return true if Grouper manages seat types (a seat group or a default seat is configured)
   */
  public boolean isManageSeatType() {
    return !StringUtils.isBlank(this.smartsheetSeatTypeMemberGroupName)
        || !StringUtils.isBlank(this.smartsheetSeatTypeContributorGroupName)
        || !StringUtils.isBlank(this.smartsheetSeatTypeGuestGroupName)
        || !StringUtils.isBlank(this.smartsheetSeatTypeDefault);
  }

  /**
   * @return the plan id to read seat types with, or null if seat types are not managed
   */
  public String planIdForReads() {
    return this.isManageSeatType() ? this.smartsheetPlanId : null;
  }

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

    // seat types
    this.smartsheetPlanId = StringUtils.trimToNull(this.retrieveConfigString("smartsheetPlanId", false));
    this.smartsheetSeatTypeMemberGroupName = StringUtils.trimToNull(this.retrieveConfigString("smartsheetSeatTypeMemberGroupName", false));
    this.smartsheetSeatTypeContributorGroupName = StringUtils.trimToNull(this.retrieveConfigString("smartsheetSeatTypeContributorGroupName", false));
    this.smartsheetSeatTypeGuestGroupName = StringUtils.trimToNull(this.retrieveConfigString("smartsheetSeatTypeGuestGroupName", false));
    this.smartsheetSeatTypeDefault = SmartsheetUser.normalizeSeatType(this.retrieveConfigString("smartsheetSeatTypeDefault", false));
    this.smartsheetDowngradeProvisionalMembers = GrouperUtil.booleanValue(
        this.retrieveConfigBoolean("smartsheetDowngradeProvisionalMembers", false), false);

    if (this.smartsheetSeatTypeDefault != null && !GrouperUtil.toSet(SmartsheetUser.SEAT_MEMBER,
        SmartsheetUser.SEAT_CONTRIBUTOR, SmartsheetUser.SEAT_GUEST).contains(this.smartsheetSeatTypeDefault)) {
      throw new RuntimeException("provisioner." + this.getGrouperProvisioner().getConfigId()
          + ".smartsheetSeatTypeDefault must be MEMBER, CONTRIBUTOR, GUEST, or blank: '" + this.smartsheetSeatTypeDefault + "'");
    }
    if (this.isManageSeatType() && this.smartsheetPlanId == null) {
      throw new RuntimeException("provisioner." + this.getGrouperProvisioner().getConfigId()
          + ".smartsheetPlanId is required when a seat type group or smartsheetSeatTypeDefault is configured");
    }

    // account flag groups
    this.flagAttributeToGroupName = new LinkedHashMap<String, String>();
    putFlagGroup("admin", "smartsheetAdminGroupName");
    putFlagGroup("groupAdmin", "smartsheetGroupAdminGroupName");
    putFlagGroup("licensedSheetCreator", "smartsheetLicensedSheetCreatorGroupName");
    putFlagGroup("resourceViewer", "smartsheetResourceViewerGroupName");

    // the seat and flag values are computed from groups by SmartsheetProvisioningTranslator; register
    // them as entity attributes so the framework compares and updates them without the institution
    // also having to list them as targetEntityAttribute.N
    if (this.isManageSeatType()) {
      registerComputedEntityAttribute(SmartsheetUser.ATTR_SEAT_TYPE, GrouperProvisioningConfigurationAttributeValueType.STRING);
    }
    for (String flagAttribute : this.flagAttributeToGroupName.keySet()) {
      // "true" / "false" strings: the framework has no boolean attribute value type
      registerComputedEntityAttribute(flagAttribute, GrouperProvisioningConfigurationAttributeValueType.STRING);
    }
  }

  /**
   * Remember the group for an account flag if its config is set.
   * @param flagAttribute the entity attribute (e.g. admin)
   * @param configSuffix the provisioner config suffix holding the group name
   */
  private void putFlagGroup(String flagAttribute, String configSuffix) {
    String groupName = StringUtils.trimToNull(this.retrieveConfigString(configSuffix, false));
    if (groupName != null) {
      this.flagAttributeToGroupName.put(flagAttribute, groupName);
    }
  }

  /**
   * Add an entity attribute computed by the translator, unless it is already configured. An attribute
   * that is configured with its own translation conflicts with the group setting, so that is an error.
   * @param attributeName the entity attribute
   * @param valueType its value type
   */
  private void registerComputedEntityAttribute(String attributeName, GrouperProvisioningConfigurationAttributeValueType valueType) {

    Map<String, GrouperProvisioningConfigurationAttribute> nameToConfig = this.getTargetEntityAttributeNameToConfig();
    GrouperProvisioningConfigurationAttribute existing = nameToConfig.get(attributeName);
    if (existing != null) {
      if (existing.getTranslateExpressionType() != null) {
        throw new RuntimeException("provisioner." + this.getGrouperProvisioner().getConfigId() + ": entity attribute '"
            + attributeName + "' has a translation and is also driven by a Smartsheet group setting; configure one or the other");
      }
      existing.setValueType(valueType);
      return;
    }

    GrouperProvisioningConfigurationAttribute attribute = new GrouperProvisioningConfigurationAttribute();
    attribute.setGrouperProvisioner(this.getGrouperProvisioner());
    attribute.setGrouperProvisioningConfigurationAttributeType(GrouperProvisioningConfigurationAttributeType.entity);
    attribute.setName(attributeName);
    attribute.setConfigIndex(nameToConfig.size());
    attribute.setSelect(this.isSelectEntities());
    attribute.setInsert(this.isInsertEntities());
    attribute.setUpdate(this.isUpdateEntities());
    attribute.setValueType(valueType);
    nameToConfig.put(attributeName, attribute);

    // configureGenericSettings already built the select attribute set from the configured attributes
    if (attribute.isSelect() && this.getEntitySelectAttributes() != null) {
      this.getEntitySelectAttributes().add(attributeName);
    }
  }

}
