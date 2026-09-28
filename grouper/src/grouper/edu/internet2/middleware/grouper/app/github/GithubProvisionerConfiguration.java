package edu.internet2.middleware.grouper.app.github;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Runtime configuration bean for the GitHub provisioner. Holds the
 * provisioner-specific config properties read from
 * <code>provisioner.&lt;configId&gt;.*</code>. Generic: no institution-specific
 * values live here; deployers set these for their environment.
 */
public class GithubProvisionerConfiguration extends GrouperProvisioningConfiguration {

  /**
   * config id of the WsBearerToken external system that holds the GitHub bearer
   * token and API endpoint (https://api.github.com)
   */
  private String githubExternalSystemConfigId;

  /**
   * comma/newline separated list of org logins this provisioner manages. Drives
   * which orgs are queried for teams and members. (The TargetDao is a target-only
   * adapter and does not have clean access to the Grouper-side group list, so the
   * managed orgs are configured explicitly rather than derived from the groups.)
   */
  private String githubOrgs;

  /**
   * optional GitHub enterprise slug. When set, entities are enriched with their
   * SAML nameId from the enterprise external-identities map so deployers can
   * match subjects by SSO identity. Blank disables the SAML lookup.
   */
  private String githubEnterpriseSlug;

  /**
   * comma/newline separated logins to never manage (org owners, bots, break-glass)
   */
  private String githubIgnoreLogins;

  /**
   * comma/newline separated team slugs to never manage
   */
  private String githubIgnoreTeamSlugs;

  public String getGithubExternalSystemConfigId() {
    return githubExternalSystemConfigId;
  }

  public void setGithubExternalSystemConfigId(String githubExternalSystemConfigId) {
    this.githubExternalSystemConfigId = githubExternalSystemConfigId;
  }

  public String getGithubOrgs() {
    return githubOrgs;
  }

  public void setGithubOrgs(String githubOrgs) {
    this.githubOrgs = githubOrgs;
  }

  public String getGithubEnterpriseSlug() {
    return githubEnterpriseSlug;
  }

  public void setGithubEnterpriseSlug(String githubEnterpriseSlug) {
    this.githubEnterpriseSlug = githubEnterpriseSlug;
  }

  public String getGithubIgnoreLogins() {
    return githubIgnoreLogins;
  }

  public void setGithubIgnoreLogins(String githubIgnoreLogins) {
    this.githubIgnoreLogins = githubIgnoreLogins;
  }

  public String getGithubIgnoreTeamSlugs() {
    return githubIgnoreTeamSlugs;
  }

  public void setGithubIgnoreTeamSlugs(String githubIgnoreTeamSlugs) {
    this.githubIgnoreTeamSlugs = githubIgnoreTeamSlugs;
  }

  @Override
  public void configureSpecificSettings() {
    this.githubExternalSystemConfigId = this.retrieveConfigString("githubExternalSystemConfigId", true);
    this.githubOrgs = GrouperUtil.defaultIfBlank(this.retrieveConfigString("githubOrgs", false), "");
    this.githubEnterpriseSlug = GrouperUtil.defaultIfBlank(this.retrieveConfigString("githubEnterpriseSlug", false), "");
    this.githubIgnoreLogins = GrouperUtil.defaultIfBlank(this.retrieveConfigString("githubIgnoreLogins", false), "");
    this.githubIgnoreTeamSlugs = GrouperUtil.defaultIfBlank(this.retrieveConfigString("githubIgnoreTeamSlugs", false), "");
  }

}
