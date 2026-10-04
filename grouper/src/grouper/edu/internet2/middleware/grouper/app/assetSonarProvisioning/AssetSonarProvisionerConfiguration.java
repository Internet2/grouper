package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Runtime configuration for the AssetSonar provisioner. Values are read from
 * grouper-loader.properties under provisioner.&lt;configId&gt;.* by
 * {@link #configureSpecificSettings()}.
 */
public class AssetSonarProvisionerConfiguration extends GrouperProvisioningConfiguration {

  /**
   * required: the WsBearerToken external system config id (grouper.wsBearerToken.&lt;id&gt;.*) that
   * holds the tenant url (endpoint) and the REST token (accessTokenPassword, sent with
   * httpHeader=token and prependBearerTokenPrefix=false)
   */
  private String assetSonarExternalSystemConfigId;

  /**
   * role_id of the non-login custodian tier (Staff User), the default tier; required when role_id is
   * a configured target attribute (the translator enforces it). Role ids are
   * tenant-specific records (Agent was 1734 in one tenant), so they are config, never code
   */
  private String assetSonarStaffUserRoleId;

  /**
   * role_id of the login-capable operator tier (Agent). Configure it even if no agent group is
   * configured, so existing Agents are recognized and left alone
   */
  private String assetSonarAgentRoleId;

  /**
   * role_id of the Administrator tier. Configure it even if no administrator group is configured,
   * so existing Administrators are recognized and never demoted or deactivated
   */
  private String assetSonarAdministratorRoleId;

  /**
   * optional full Grouper group name whose members get the Administrator tier. If blank, Grouper
   * never assigns Administrator AND never demotes an existing Administrator.
   */
  private String assetSonarAdministratorGroupName;

  /**
   * optional full Grouper group name whose members get the Agent tier. If blank, Grouper never
   * assigns Agent AND never demotes an existing Agent.
   */
  private String assetSonarAgentGroupName;

  /**
   * when true (default) the full select reads the inactive members too
   * (filter=status&amp;filter_val=inactive). Without them a deactivated member looks absent and its
   * sync row (and member id) is dropped after removeSyncRowsAfterSecondsOutOfTarget.
   */
  private boolean assetSonarSelectInactiveMembers = true;

  public String getAssetSonarExternalSystemConfigId() {
    return assetSonarExternalSystemConfigId;
  }

  public void setAssetSonarExternalSystemConfigId(String assetSonarExternalSystemConfigId) {
    this.assetSonarExternalSystemConfigId = assetSonarExternalSystemConfigId;
  }

  public String getAssetSonarStaffUserRoleId() {
    return assetSonarStaffUserRoleId;
  }

  public void setAssetSonarStaffUserRoleId(String assetSonarStaffUserRoleId) {
    this.assetSonarStaffUserRoleId = assetSonarStaffUserRoleId;
  }

  public String getAssetSonarAgentRoleId() {
    return assetSonarAgentRoleId;
  }

  public void setAssetSonarAgentRoleId(String assetSonarAgentRoleId) {
    this.assetSonarAgentRoleId = assetSonarAgentRoleId;
  }

  public String getAssetSonarAdministratorRoleId() {
    return assetSonarAdministratorRoleId;
  }

  public void setAssetSonarAdministratorRoleId(String assetSonarAdministratorRoleId) {
    this.assetSonarAdministratorRoleId = assetSonarAdministratorRoleId;
  }

  public String getAssetSonarAdministratorGroupName() {
    return assetSonarAdministratorGroupName;
  }

  public void setAssetSonarAdministratorGroupName(String assetSonarAdministratorGroupName) {
    this.assetSonarAdministratorGroupName = assetSonarAdministratorGroupName;
  }

  public String getAssetSonarAgentGroupName() {
    return assetSonarAgentGroupName;
  }

  public void setAssetSonarAgentGroupName(String assetSonarAgentGroupName) {
    this.assetSonarAgentGroupName = assetSonarAgentGroupName;
  }

  public boolean isAssetSonarSelectInactiveMembers() {
    return assetSonarSelectInactiveMembers;
  }

  public void setAssetSonarSelectInactiveMembers(boolean assetSonarSelectInactiveMembers) {
    this.assetSonarSelectInactiveMembers = assetSonarSelectInactiveMembers;
  }

  @Override
  public void configureSpecificSettings() {

    this.assetSonarExternalSystemConfigId = this.retrieveConfigString("assetSonarExternalSystemConfigId", true);

    this.assetSonarStaffUserRoleId = StringUtils.trimToNull(this.retrieveConfigString("assetSonarStaffUserRoleId", false));
    this.assetSonarAgentRoleId = StringUtils.trimToNull(this.retrieveConfigString("assetSonarAgentRoleId", false));
    this.assetSonarAdministratorRoleId = StringUtils.trimToNull(this.retrieveConfigString("assetSonarAdministratorRoleId", false));

    this.assetSonarAdministratorGroupName = this.retrieveConfigString("assetSonarAdministratorGroupName", false);

    this.assetSonarAgentGroupName = this.retrieveConfigString("assetSonarAgentGroupName", false);

    this.assetSonarSelectInactiveMembers = GrouperUtil.booleanValue(
        this.retrieveConfigBoolean("assetSonarSelectInactiveMembers", false), true);
  }

}
