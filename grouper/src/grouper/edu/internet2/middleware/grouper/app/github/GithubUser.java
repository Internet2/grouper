package edu.internet2.middleware.grouper.app.github;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Models a GitHub account (entity). The stable membership key is the
 * {@link #login}. GitHub cannot create accounts synchronously, so a subject with
 * no known login is provisioned by inviting {@link #email}; the login only
 * materializes after the person accepts (and, where SAML is enforced, links via
 * SSO). Where an enterprise slug is configured, {@link #samlNameId} is populated
 * from the enterprise SAML external-identities map so deployers can match
 * subjects on the nameId instead of (or in addition to) the login.
 */
public class GithubUser {

  /**
   * DDL for the mock table used by the mock service handler in tests. Not used
   * by real provisioning.
   * @param ddlVersionBean the ddl version bean
   * @param database the database
   */
  public static void createTableGithubUser(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_github_user";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "login", Types.VARCHAR, "256", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "saml_name_id", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "org_state", Types.VARCHAR, "16", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "role", Types.VARCHAR, "16", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "invitation_id", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_gh_user_samlid_idx", false, "saml_name_id");
    }

  }

  /**
   * GitHub username (login). The stable key for all membership operations. Null
   * on a pending invitation that has not been accepted yet.
   */
  private String login;

  /**
   * GitHub numeric account id (as a string)
   */
  private String id;

  /**
   * SAML NameID from the enterprise external-identities map; the join key when a
   * deployer matches subjects by SSO identity. Null when no enterprise slug is
   * configured or the account is not SAML-linked.
   */
  private String samlNameId;

  /**
   * email address, used only to bootstrap an org invitation (not returned by
   * list calls)
   */
  private String email;

  /**
   * org membership state: "active" or "pending"
   */
  private String orgState;

  /**
   * org role: "member" or "admin"
   */
  private String role;

  /**
   * invitation id, set on a pending invitation record so it can be cancelled
   */
  private String invitationId;

  public String getLogin() {
    return login;
  }

  public void setLogin(String login) {
    this.login = login;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getSamlNameId() {
    return samlNameId;
  }

  public void setSamlNameId(String samlNameId) {
    this.samlNameId = samlNameId;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getOrgState() {
    return orgState;
  }

  public void setOrgState(String orgState) {
    this.orgState = orgState;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  public String getInvitationId() {
    return invitationId;
  }

  public void setInvitationId(String invitationId) {
    this.invitationId = invitationId;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Build a GithubUser from a user object (as returned by GET /orgs/{org}/members
   * or a team members list). Only login + id are present there; email, samlNameId,
   * and state are populated by other calls.
   * @param userNode the user JSON object
   * @return the GithubUser, or null if userNode is null
   */
  public static GithubUser fromJson(JsonNode userNode) {
    if (userNode == null) {
      return null;
    }
    GithubUser githubUser = new GithubUser();
    githubUser.login = GithubApiCommands.jsonText(userNode, "login");
    githubUser.id = GithubApiCommands.jsonText(userNode, "id");
    return githubUser;
  }

  /**
   * Convert to a Grouper provisioning entity. The provisioning id is the login;
   * login, samlNameId, email, and id are all attributes so a deployer can match
   * on whichever they configure (typically login, or samlNameId where SAML is used).
   * @return the provisioning entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);

    if (this.login != null) {
      targetEntity.setId(this.login);
    }
    targetEntity.assignAttributeValue("login", this.login);
    if (this.samlNameId != null) {
      targetEntity.assignAttributeValue("samlNameId", this.samlNameId);
    }
    if (this.email != null) {
      targetEntity.assignAttributeValue("email", this.email);
    }
    if (this.id != null) {
      targetEntity.assignAttributeValue("githubId", this.id);
    }

    return targetEntity;
  }

  /**
   * Convert from a provisioning entity. Login is the operational key; email is
   * needed to invite an as-yet-unmapped subject.
   * @param targetEntity the provisioning entity
   * @param fieldNamesToSet the field names to set, or null for all
   * @return the GithubUser
   */
  public static GithubUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    GithubUser githubUser = new GithubUser();

    String login = targetEntity.retrieveAttributeValueString("login");
    githubUser.setLogin(login != null ? login : targetEntity.getId());
    githubUser.setSamlNameId(targetEntity.retrieveAttributeValueString("samlNameId"));

    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {
      githubUser.setEmail(targetEntity.retrieveAttributeValueString("email"));
    }

    return githubUser;
  }

}
