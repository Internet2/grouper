package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Runtime configuration for the AssetSonar provisioner. Values are read from
 * grouper-loader.properties under provisioner.&lt;configId&gt;.* by
 * {@link #configureSpecificSettings()}.
 */
public class AssetSonarProvisionerConfiguration extends GrouperProvisioningConfiguration {

  /**
   * required: the AssetSonar external system config id (grouper.assetSonarConnector.&lt;id&gt;.*),
   * which holds the base url, REST token, and the tenant's role ids
   */
  private String assetSonarExternalSystemConfigId;

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

    this.assetSonarAdministratorGroupName = this.retrieveConfigString("assetSonarAdministratorGroupName", false);

    this.assetSonarAgentGroupName = this.retrieveConfigString("assetSonarAgentGroupName", false);

    this.assetSonarSelectInactiveMembers = GrouperUtil.booleanValue(
        this.retrieveConfigBoolean("assetSonarSelectInactiveMembers", false), true);
  }

}
