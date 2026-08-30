package edu.internet2.middleware.grouper.app.github;

import java.sql.Types;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Models a GitHub team membership (a login in a team). A plain association bean:
 * the JSON round-tripping for memberships is done inline in
 * {@link GithubApiCommands} and {@link GithubTargetDao}, so there are no
 * fromJson/toJson methods here (mirrors the Datadog membership model).
 */
public class GithubMembership {

  /**
   * DDL for the mock table used by the mock service handler in tests. Not used
   * by real provisioning.
   * @param ddlVersionBean the ddl version bean
   * @param database the database
   */
  public static void createTableGithubMembership(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_github_membership";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "org", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "team_slug", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "team_id", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_login", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "role", Types.VARCHAR, "32", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "state", Types.VARCHAR, "16", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_gh_mship_idx", true, "org", "team_slug", "user_login");
    }

  }

  /**
   * random id for the mock table row
   */
  private String id;

  /**
   * org login
   */
  private String org;

  /**
   * team slug (operational key for the membership URL)
   */
  private String teamSlug;

  /**
   * team numeric id (as a string), informational
   */
  private String teamId;

  /**
   * member's login
   */
  private String userLogin;

  /**
   * "member" or "maintainer"
   */
  private String role;

  /**
   * "active" or "pending"
   */
  private String state;

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

  public String getTeamSlug() {
    return teamSlug;
  }

  public void setTeamSlug(String teamSlug) {
    this.teamSlug = teamSlug;
  }

  public String getTeamId() {
    return teamId;
  }

  public void setTeamId(String teamId) {
    this.teamId = teamId;
  }

  public String getUserLogin() {
    return userLogin;
  }

  public void setUserLogin(String userLogin) {
    this.userLogin = userLogin;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

}
