package edu.internet2.middleware.grouper.app.jamf;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Runtime configuration for the Jamf provisioner. Values are read from
 * grouper-loader.properties under provisioner.&lt;configId&gt;.* by
 * {@link #configureSpecificSettings()}.
 */
public class JamfProvisionerConfiguration extends GrouperProvisioningConfiguration {

  /**
   * required: the external system config id (a WsBearerToken external system) that holds
   * the Jamf Pro base URL and the OAuth client-credentials token endpoint/credentials.
   */
  private String jamfExternalSystemConfigId;

  /**
   * access_level to stamp on accounts Grouper creates. Jamf accounts inherit their
   * privileges from the account groups (roles) they belong to, so newly-created accounts
   * carry no inline privileges. Defaults to "Group Access".
   */
  private String jamfNewAccountAccessLevel;

  /**
   * comma-separated list of account names (EPPNs, e.g. jdoe@upenn.edu) to ignore during
   * provisioning. Ignored accounts are never created, and are never added to or removed
   * from a role by Grouper. Use this to protect break-glass / service admin accounts.
   */
  private String jamfIgnoreAccountNames;

  /**
   * comma-separated list of account group (role) names to ignore during provisioning.
   * Ignored roles are filtered out of retrieve and their membership is never touched.
   */
  private String jamfIgnoreRoleNames;

  /**
   * when true, a "delete" disables the account (enabled=Disabled) instead of hard-deleting it, and
   * disabled accounts are filtered out of reads (so they look absent). A subsequent insert of the
   * same person re-enables the disabled account and resets its name/full_name/email instead of
   * creating a duplicate. Defaults to false (hard delete), matching the SCIM provisioner's code
   * default -- deployments that want the softer behavior set it true in the config.
   */
  private boolean disableEntitiesInsteadOfDelete;

  public String getJamfExternalSystemConfigId() {
    return jamfExternalSystemConfigId;
  }

  public void setJamfExternalSystemConfigId(String jamfExternalSystemConfigId) {
    this.jamfExternalSystemConfigId = jamfExternalSystemConfigId;
  }

  public String getJamfNewAccountAccessLevel() {
    return jamfNewAccountAccessLevel;
  }

  public void setJamfNewAccountAccessLevel(String jamfNewAccountAccessLevel) {
    this.jamfNewAccountAccessLevel = jamfNewAccountAccessLevel;
  }

  public String getJamfIgnoreAccountNames() {
    return jamfIgnoreAccountNames;
  }

  public void setJamfIgnoreAccountNames(String jamfIgnoreAccountNames) {
    this.jamfIgnoreAccountNames = jamfIgnoreAccountNames;
  }

  public String getJamfIgnoreRoleNames() {
    return jamfIgnoreRoleNames;
  }

  public void setJamfIgnoreRoleNames(String jamfIgnoreRoleNames) {
    this.jamfIgnoreRoleNames = jamfIgnoreRoleNames;
  }

  public boolean isDisableEntitiesInsteadOfDelete() {
    return disableEntitiesInsteadOfDelete;
  }

  public void setDisableEntitiesInsteadOfDelete(boolean disableEntitiesInsteadOfDelete) {
    this.disableEntitiesInsteadOfDelete = disableEntitiesInsteadOfDelete;
  }

  @Override
  public void configureSpecificSettings() {

    this.jamfExternalSystemConfigId = this.retrieveConfigString("jamfExternalSystemConfigId", true);

    this.jamfNewAccountAccessLevel = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("jamfNewAccountAccessLevel", false), "Group Access");

    this.jamfIgnoreAccountNames = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("jamfIgnoreAccountNames", false), "");

    this.jamfIgnoreRoleNames = GrouperUtil.defaultIfBlank(
        this.retrieveConfigString("jamfIgnoreRoleNames", false), "");

    // default false (hard delete) to match the SCIM provisioner's code default
    this.disableEntitiesInsteadOfDelete = GrouperUtil.booleanValue(
        this.retrieveConfigBoolean("disableEntitiesInsteadOfDelete", false), false);

  }

}
