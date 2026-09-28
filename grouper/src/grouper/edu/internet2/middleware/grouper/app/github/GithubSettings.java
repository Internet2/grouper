package edu.internet2.middleware.grouper.app.github;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

/**
 * Runtime settings for the GitHub provisioner, passed to the API command
 * methods. This is a generic provisioner: nothing here is institution specific.
 * Deployers configure the behavior through provisioner config properties, which
 * are loaded into this holder via
 * {@link #loadFromGithubProvisionerConfiguration(GithubProvisionerConfiguration)}.
 */
public class GithubSettings {

  /**
   * org logins this provisioner manages (queried for teams and members). Order
   * preserved, case preserved (GitHub org logins are case-insensitive but the
   * canonical casing is kept).
   */
  private Set<String> managedOrgs = new LinkedHashSet<String>();

  /**
   * Optional GitHub enterprise slug. When set, the provisioner reads the
   * enterprise SAML external-identities map (SAML nameId -&gt; login) so entities
   * can carry a matchable samlNameId in addition to their login. When blank, the
   * provisioner matches on login alone and skips the SAML lookup, so this works
   * for enterprises without SAML SSO too.
   */
  private String enterpriseSlug;

  /**
   * logins to ignore entirely: never invite, add, remove, or count them. Use for
   * org owners, break-glass accounts, and bots that must not be managed by Grouper.
   */
  private Set<String> ignoreLogins = new LinkedHashSet<String>();

  /**
   * team slugs to ignore: never manage membership of these teams even if a
   * Grouper group maps to them.
   */
  private Set<String> ignoreTeamSlugs = new LinkedHashSet<String>();

  /**
   * @return org logins this provisioner manages
   */
  public Set<String> getManagedOrgs() {
    return managedOrgs;
  }

  /**
   * @param managedOrgs org logins this provisioner manages
   */
  public void setManagedOrgs(Set<String> managedOrgs) {
    this.managedOrgs = managedOrgs;
  }

  /**
   * @return optional enterprise slug, or blank/null if SAML identity resolution is not used
   */
  public String getEnterpriseSlug() {
    return enterpriseSlug;
  }

  /**
   * @param enterpriseSlug the enterprise slug
   */
  public void setEnterpriseSlug(String enterpriseSlug) {
    this.enterpriseSlug = enterpriseSlug;
  }

  /**
   * @return set of logins to ignore
   */
  public Set<String> getIgnoreLogins() {
    return ignoreLogins;
  }

  /**
   * @param ignoreLogins set of logins to ignore
   */
  public void setIgnoreLogins(Set<String> ignoreLogins) {
    this.ignoreLogins = ignoreLogins;
  }

  /**
   * @return set of team slugs to ignore
   */
  public Set<String> getIgnoreTeamSlugs() {
    return ignoreTeamSlugs;
  }

  /**
   * @param ignoreTeamSlugs set of team slugs to ignore
   */
  public void setIgnoreTeamSlugs(Set<String> ignoreTeamSlugs) {
    this.ignoreTeamSlugs = ignoreTeamSlugs;
  }

  /**
   * @param login the GitHub login to check
   * @return true if this login should be ignored (never managed)
   */
  public boolean isIgnoredLogin(String login) {
    if (StringUtils.isBlank(login)) {
      return false;
    }
    return ignoreLogins.contains(login.toLowerCase());
  }

  /**
   * @param teamSlug the team slug to check
   * @return true if this team should be ignored
   */
  public boolean isIgnoredTeamSlug(String teamSlug) {
    if (StringUtils.isBlank(teamSlug)) {
      return false;
    }
    return ignoreTeamSlugs.contains(teamSlug.toLowerCase());
  }

  /**
   * Load settings from the GitHub provisioner configuration bean.
   * @param githubConfiguration the provisioner configuration
   */
  public void loadFromGithubProvisionerConfiguration(GithubProvisionerConfiguration githubConfiguration) {
    this.enterpriseSlug = githubConfiguration.getGithubEnterpriseSlug();
    this.managedOrgs = parseCommaSeparatedOrNewlineSet(githubConfiguration.getGithubOrgs(), false);
    this.ignoreLogins = parseCommaSeparatedOrNewlineSet(githubConfiguration.getGithubIgnoreLogins(), true);
    this.ignoreTeamSlugs = parseCommaSeparatedOrNewlineSet(githubConfiguration.getGithubIgnoreTeamSlugs(), true);
  }

  /**
   * Parse a comma or newline separated string into a set of trimmed values,
   * ignoring blanks.
   * @param input the raw config string
   * @param lowerCase true to lowercase each value (for case-insensitive matching)
   * @return set of trimmed values (never null)
   */
  private static Set<String> parseCommaSeparatedOrNewlineSet(String input, boolean lowerCase) {
    Set<String> result = new LinkedHashSet<String>();
    if (StringUtils.isBlank(input)) {
      return result;
    }
    String[] parts = input.split("[,\\n\\r]+");
    for (String part : parts) {
      String trimmed = part.trim();
      if (StringUtils.isNotBlank(trimmed)) {
        result.add(lowerCase ? trimmed.toLowerCase() : trimmed);
      }
    }
    return result;
  }

}
