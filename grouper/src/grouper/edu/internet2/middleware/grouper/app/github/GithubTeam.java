package edu.internet2.middleware.grouper.app.github;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Models a GitHub team, which a Grouper group maps to. There are two flavors,
 * distinguished by {@link #teamType}: "organization" teams (the ones this
 * provisioner manages in v1, keyed by org + slug) and "enterprise" teams
 * (read-only / inherited in v1, keyed by numeric id under an enterprise). Both
 * are returned by GET /orgs/{org}/teams.
 *
 * <p>The membership operations for an org team address it by org + slug, so both
 * are carried on the provisioning group (org as an attribute, slug as the
 * provisioning id) rather than the numeric id.</p>
 */
public class GithubTeam {

  /**
   * DDL for the mock table used by the mock service handler in tests. Not used
   * by real provisioning.
   * @param ddlVersionBean the ddl version bean
   * @param database the database
   */
  public static void createTableGithubTeam(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_github_team";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "org", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "slug", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "team_type", Types.VARCHAR, "32", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "privacy", Types.VARCHAR, "32", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_gh_team_org_slug_idx", true, "org", "slug");
    }

  }

  /**
   * GitHub numeric team id (as a string). Globally unique. For enterprise teams
   * this is the path key; for org teams the path key is the slug instead.
   */
  private String id;

  /**
   * org login this team belongs to (the request context, not part of the team JSON body).
   */
  private String org;

  /**
   * url-safe team key. For an org team this is the path key used in membership URLs.
   * Enterprise team slugs are prefixed "ent:".
   */
  private String slug;

  /**
   * team display name
   */
  private String name;

  /**
   * "organization" or "enterprise"
   */
  private String teamType;

  /**
   * "closed" or "secret"
   */
  private String privacy;

  /**
   * team description
   */
  private String description;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getOrg() {
    return org;
  }

  public void setOrg(String org) {
    this.org = org;
  }

  public String getSlug() {
    return slug;
  }

  public void setSlug(String slug) {
    this.slug = slug;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTeamType() {
    return teamType;
  }

  public void setTeamType(String teamType) {
    this.teamType = teamType;
  }

  public String getPrivacy() {
    return privacy;
  }

  public void setPrivacy(String privacy) {
    this.privacy = privacy;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * @return true if this is an enterprise team (read-only / inherited in v1)
   */
  public boolean isEnterpriseTeam() {
    return "enterprise".equals(this.teamType);
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Build a GithubTeam from a single team object out of GET /orgs/{org}/teams
   * (or GET /orgs/{org}/teams/{slug}). The org is not part of the JSON body, so
   * the caller must set it via {@link #setOrg(String)} using the request context.
   * @param teamNode the team JSON object
   * @return the GithubTeam, or null if teamNode is null
   */
  public static GithubTeam fromJson(JsonNode teamNode) {
    if (teamNode == null) {
      return null;
    }

    GithubTeam githubTeam = new GithubTeam();
    githubTeam.id = GithubApiCommands.jsonText(teamNode, "id");
    githubTeam.slug = GithubApiCommands.jsonText(teamNode, "slug");
    githubTeam.name = GithubApiCommands.jsonText(teamNode, "name");
    githubTeam.privacy = GithubApiCommands.jsonText(teamNode, "privacy");
    githubTeam.description = GithubApiCommands.jsonText(teamNode, "description");

    // "type" is present as "enterprise" for enterprise teams; org teams may omit
    // it, in which case treat them as organization teams.
    String type = GithubApiCommands.jsonText(teamNode, "type");
    githubTeam.teamType = "enterprise".equals(type) ? "enterprise" : "organization";

    return githubTeam;
  }

  /**
   * Convert to a Grouper provisioning group. The provisioning id is the slug
   * (the operational key for org-team membership URLs); org, numeric id, and the
   * rest are attributes so a deployer can match on whichever they configure.
   * @return the provisioning group
   */
  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);

    if (this.slug != null) {
      targetGroup.setId(this.slug);
    }
    targetGroup.assignAttributeValue("slug", this.slug);
    targetGroup.assignAttributeValue("name", this.name);
    if (this.id != null) {
      targetGroup.assignAttributeValue("githubId", this.id);
    }
    if (this.org != null) {
      targetGroup.assignAttributeValue("org", this.org);
    }
    if (this.teamType != null) {
      targetGroup.assignAttributeValue("teamType", this.teamType);
    }
    if (this.privacy != null) {
      targetGroup.assignAttributeValue("privacy", this.privacy);
    }
    if (this.description != null) {
      targetGroup.assignAttributeValue("description", this.description);
    }

    return targetGroup;
  }

  /**
   * Convert from a provisioning group. Always pulls org + slug (needed to build
   * membership URLs); other fields are gated by fieldNamesToSet.
   * @param targetGroup the provisioning group
   * @param fieldNamesToSet the field names to set, or null for all
   * @return the GithubTeam
   */
  public static GithubTeam fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    GithubTeam githubTeam = new GithubTeam();

    // slug is the operational key; fall back to the provisioning id
    String slug = targetGroup.retrieveAttributeValueString("slug");
    githubTeam.setSlug(slug != null ? slug : targetGroup.getId());
    githubTeam.setOrg(targetGroup.retrieveAttributeValueString("org"));
    githubTeam.setId(targetGroup.retrieveAttributeValueString("githubId"));
    githubTeam.setTeamType(targetGroup.retrieveAttributeValueString("teamType"));

    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {
      githubTeam.setName(targetGroup.retrieveAttributeValueString("name"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {
      githubTeam.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("privacy")) {
      githubTeam.setPrivacy(targetGroup.retrieveAttributeValueString("privacy"));
    }

    return githubTeam;
  }

}
