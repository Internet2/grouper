package edu.internet2.middleware.grouper.app.truefoundry;

import java.util.Set;

/**
 * Settings for TrueFoundry API commands, bundling provisioner-specific configuration
 * so method signatures don't need individual parameters for each setting.
 * Similar pattern to ScimSettings in the SCIM provisioner.
 */
public class TrueFoundrySettings {

  private String tenantName;

  private String ssoId;

  private String defaultTeamMemberEmail;

  private Set<String> ignoreRoleNames;

  private Set<String> ignoreUserEmails;

  /**
   * Load settings from a TrueFoundryProvisionerConfiguration.
   * @param config the provisioner configuration
   */
  public void loadFromConfiguration(TrueFoundryProvisionerConfiguration config) {
    this.tenantName = config.getTrueFoundryScimTenantName();
    this.ssoId = config.getTrueFoundryScimSsoId();
    this.defaultTeamMemberEmail = config.getTrueFoundryDefaultTeamMemberEmail();
    this.ignoreRoleNames = TrueFoundryApiCommands.parseIgnoreSet(config.getTrueFoundryIgnoreRoles());
    this.ignoreUserEmails = TrueFoundryApiCommands.parseIgnoreSet(config.getTrueFoundryIgnoreUserEmails());
  }

  public String getTenantName() {
    return tenantName;
  }

  public void setTenantName(String tenantName) {
    this.tenantName = tenantName;
  }

  public String getSsoId() {
    return ssoId;
  }

  public void setSsoId(String ssoId) {
    this.ssoId = ssoId;
  }

  public String getDefaultTeamMemberEmail() {
    return defaultTeamMemberEmail;
  }

  public void setDefaultTeamMemberEmail(String defaultTeamMemberEmail) {
    this.defaultTeamMemberEmail = defaultTeamMemberEmail;
  }

  public Set<String> getIgnoreRoleNames() {
    return ignoreRoleNames;
  }

  public void setIgnoreRoleNames(Set<String> ignoreRoleNames) {
    this.ignoreRoleNames = ignoreRoleNames;
  }

  public Set<String> getIgnoreUserEmails() {
    return ignoreUserEmails;
  }

  public void setIgnoreUserEmails(Set<String> ignoreUserEmails) {
    this.ignoreUserEmails = ignoreUserEmails;
  }

}
