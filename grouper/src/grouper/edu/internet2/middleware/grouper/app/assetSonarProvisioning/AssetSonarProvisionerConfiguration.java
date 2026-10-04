package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.util.LinkedHashSet;
import java.util.Set;

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

  /**
   * retries after the first attempt when a call gets 502/503/504 (default 3). The gateway
   * occasionally loses the response of a committed write under load; see AssetSonarApiCommands
   */
  private int assetSonarRetryCount = AssetSonarApiCommands.DEFAULT_RETRY_COUNT;

  /** first retry sleep in millis (default 1000), doubling each retry: 1s, 2s, 4s */
  private int assetSonarRetrySleepMillis = AssetSonarApiCommands.DEFAULT_RETRY_SLEEP_MILLIS;

  /**
   * lowercased emails of members Grouper must never manage (equipment logins, lab and service
   * accounts). From assetSonarExcludeEmails, comma or whitespace separated. Never null
   */
  private Set<String> assetSonarExcludeEmails = new LinkedHashSet<String>();

  public Set<String> getAssetSonarExcludeEmails() {
    return assetSonarExcludeEmails;
  }

  public void setAssetSonarExcludeEmails(Set<String> assetSonarExcludeEmails) {
    this.assetSonarExcludeEmails = assetSonarExcludeEmails;
  }

  /**
   * @param email a member email (any case)
   * @return true if the email is in assetSonarExcludeEmails
   */
  public boolean isExcludedEmail(String email) {
    return !StringUtils.isBlank(email) && this.assetSonarExcludeEmails.contains(email.trim().toLowerCase());
  }

  /**
   * @param raw the config value: emails separated by commas, spaces or new lines
   * @return the lowercased emails (never null)
   */
  static Set<String> parseExcludeEmails(String raw) {
    Set<String> result = new LinkedHashSet<String>();
    if (!StringUtils.isBlank(raw)) {
      for (String email : raw.split("[,\\s]+")) {
        if (!StringUtils.isBlank(email)) {
          result.add(email.trim().toLowerCase());
        }
      }
    }
    return result;
  }

  public int getAssetSonarRetryCount() {
    return assetSonarRetryCount;
  }

  public void setAssetSonarRetryCount(int assetSonarRetryCount) {
    this.assetSonarRetryCount = assetSonarRetryCount;
  }

  public int getAssetSonarRetrySleepMillis() {
    return assetSonarRetrySleepMillis;
  }

  public void setAssetSonarRetrySleepMillis(int assetSonarRetrySleepMillis) {
    this.assetSonarRetrySleepMillis = assetSonarRetrySleepMillis;
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

    this.assetSonarExcludeEmails = parseExcludeEmails(this.retrieveConfigString("assetSonarExcludeEmails", false));

    this.assetSonarRetryCount = GrouperUtil.intValue(
        this.retrieveConfigInt("assetSonarRetryCount", false), AssetSonarApiCommands.DEFAULT_RETRY_COUNT);

    this.assetSonarRetrySleepMillis = GrouperUtil.intValue(
        this.retrieveConfigInt("assetSonarRetrySleepMillis", false), AssetSonarApiCommands.DEFAULT_RETRY_SLEEP_MILLIS);
  }

}
