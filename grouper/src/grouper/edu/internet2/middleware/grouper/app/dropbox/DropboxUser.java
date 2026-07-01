package edu.internet2.middleware.grouper.app.dropbox;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Domain object for a Dropbox Business team member (the "entity").
 *
 * <p>Maps to the Dropbox Team API member profile as returned by
 * /2/team/members/list_v2 and /2/team/members/get_info_v2.  Grouper matches
 * members by external_id (which Grouper sets, e.g. to the pennname) and uses the
 * native team_member_id for group membership operations.</p>
 */
public class DropboxUser {

  /** member status: active member of the team */
  public static final String STATUS_ACTIVE = "active";

  /** member status: invited but not yet joined */
  public static final String STATUS_INVITED = "invited";

  /** member status: suspended (deprovisioned but account retained) */
  public static final String STATUS_SUSPENDED = "suspended";

  /** member status: removed from the team */
  public static final String STATUS_REMOVED = "removed";

  /** lifecycle state: a normal active team member (default) */
  public static final String LIFECYCLE_STATE_ACTIVE = "active";

  /** lifecycle state: suspended (deactivated, account retained on the team) */
  public static final String LIFECYCLE_STATE_SUSPENDED = "suspended";

  /** lifecycle marker group extension that suspends the member (under the lifecycle folder) */
  public static final String LIFECYCLE_MARKER_SUSPENDED = "Suspended";

  /**
   * lifecycle marker group extension that, on deprovision, converts the member to a free Basic
   * account (keep_account=true) instead of deleting it (under the lifecycle folder)
   */
  public static final String LIFECYCLE_MARKER_DOWNGRADE = "Downgrade";

  /**
   * The 8 built-in Dropbox admin roles, ordered highest-privilege first.  When a
   * member belongs to more than one admin-role group, full sync resolves to the
   * highest tier in this list.  A member in no admin-role group is member_only
   * (a regular member with no admin rights).  These names match the group
   * extension under the configured admin-role folder.
   */
  public static final java.util.List<String> ADMIN_ROLE_HIERARCHY = java.util.Collections.unmodifiableList(
      java.util.Arrays.asList(
          "Team_Admin",
          "User_Management_Admin",
          "Support_Admin",
          "Billing_Admin",
          "Content_Admin",
          "Security_Admin",
          "Reporting_Admin",
          "Compliance_Admin"));

  /**
   * Resolve the highest-privilege admin role among a set of role names, per
   * ADMIN_ROLE_HIERARCHY.  Returns null if the set is empty or contains no known role.
   * @param roleNames candidate admin role names
   * @return the highest-tier role name, or null
   */
  public static String highestAdminRole(java.util.Collection<String> roleNames) {
    if (roleNames == null) {
      return null;
    }
    for (String candidate : ADMIN_ROLE_HIERARCHY) {
      for (String roleName : roleNames) {
        if (candidate.equalsIgnoreCase(roleName)) {
          return candidate;
        }
      }
    }
    return null;
  }

  /**
   * Create the mock database table that simulates the Dropbox member store for tests.
   * @param ddlVersionBean ddl version bean
   * @param database the database model
   */
  public static void createTableDropboxUser(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_dropbox_user";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      // id is the native Dropbox team_member_id (e.g. "dbmid:abc123")
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "100", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "255", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "external_id", Types.VARCHAR, "255", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "given_name", Types.VARCHAR, "255", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "surname", Types.VARCHAR, "255", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", Types.VARCHAR, "20", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "account_id", Types.VARCHAR, "100", false, false);
      // admin_role stores the member's current Dropbox admin role name (one of the 8
      // built-in roles) or null for member_only; used by the mock to simulate
      // set_admin_permissions / get_info roles
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "admin_role", Types.VARCHAR, "40", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_dbx_user_email_idx", true, "email");
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_dbx_user_extid_idx", false, "external_id");
    }

  }

  /** native Dropbox team_member_id (e.g. "dbmid:abc123") */
  private String id;

  /** member email address */
  private String email;

  /** external_id — Grouper sets this to a stable match key (e.g. pennname) */
  private String externalId;

  /** given (first) name */
  private String givenName;

  /** surname (last name) */
  private String surname;

  /** member status: active, invited, suspended, or removed */
  private String status;

  /** Dropbox account_id (distinct from team_member_id; read-only) */
  private String accountId;

  /**
   * effective admin role name (one of ADMIN_ROLE_HIERARCHY), or null/member_only
   * for a regular member.  Only populated/managed when the provisioner is
   * configured with an admin-role folder.
   */
  private String adminRole;

  /**
   * desired lifecycle state (active / suspended), resolved from the lifecycle marker groups.
   * Only populated/managed when the provisioner is configured with a lifecycle folder.
   */
  private String lifecycleState;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getGivenName() {
    return givenName;
  }

  public void setGivenName(String givenName) {
    this.givenName = givenName;
  }

  public String getSurname() {
    return surname;
  }

  public void setSurname(String surname) {
    this.surname = surname;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getAccountId() {
    return accountId;
  }

  public void setAccountId(String accountId) {
    this.accountId = accountId;
  }

  public String getAdminRole() {
    return adminRole;
  }

  public void setAdminRole(String adminRole) {
    this.adminRole = adminRole;
  }

  public String getLifecycleState() {
    return lifecycleState;
  }

  public void setLifecycleState(String lifecycleState) {
    this.lifecycleState = lifecycleState;
  }

  /**
   * @return true if the desired lifecycle state is suspended
   */
  public boolean isLifecycleSuspended() {
    return LIFECYCLE_STATE_SUSPENDED.equals(this.lifecycleState);
  }

  /**
   * @return true if the member is active on the team
   */
  public boolean isActive() {
    return STATUS_ACTIVE.equals(this.status);
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Convert from a Dropbox member profile JSON node.  Accepts either a bare
   * TeamMemberProfile or a member_info wrapper containing a "profile" object.
   * @param memberNode a Dropbox member JSON object
   * @return the DropboxUser, or null if memberNode is null
   */
  public static DropboxUser fromJson(JsonNode memberNode) {
    if (memberNode == null) {
      return null;
    }

    // some endpoints wrap the profile in a "profile" object (e.g. members/list_v2
    // returns { "profile": {...}, "roles": [...] })
    JsonNode profileNode = GrouperUtil.jsonJacksonGetNode(memberNode, "profile");
    if (profileNode == null) {
      profileNode = memberNode;
    }

    DropboxUser dropboxUser = new DropboxUser();
    dropboxUser.id = GrouperUtil.jsonJacksonGetString(profileNode, "team_member_id");
    dropboxUser.email = GrouperUtil.jsonJacksonGetString(profileNode, "email");
    dropboxUser.externalId = GrouperUtil.jsonJacksonGetString(profileNode, "external_id");
    dropboxUser.accountId = GrouperUtil.jsonJacksonGetString(profileNode, "account_id");

    // status is a Dropbox union: { ".tag": "active" }
    JsonNode statusNode = GrouperUtil.jsonJacksonGetNode(profileNode, "status");
    if (statusNode != null) {
      dropboxUser.status = GrouperUtil.jsonJacksonGetString(statusNode, ".tag");
    }

    // name is a Name object with given_name/surname/display_name
    JsonNode nameNode = GrouperUtil.jsonJacksonGetNode(profileNode, "name");
    if (nameNode != null) {
      dropboxUser.givenName = GrouperUtil.jsonJacksonGetString(nameNode, "given_name");
      dropboxUser.surname = GrouperUtil.jsonJacksonGetString(nameNode, "surname");
    }

    // roles is a sibling of profile (members/list_v2 / get_info_v2):
    // "roles": [ { "role_id": "...", "name": "Team_Admin", "description": "..." } ]
    // resolve to the single highest-tier admin role name
    JsonNode rolesNode = GrouperUtil.jsonJacksonGetNode(memberNode, "roles");
    if (rolesNode != null && rolesNode.isArray()) {
      java.util.List<String> roleNames = new java.util.ArrayList<String>();
      for (int i = 0; i < rolesNode.size(); i++) {
        String roleName = GrouperUtil.jsonJacksonGetString(rolesNode.get(i), "name");
        if (!GrouperUtil.isBlank(roleName)) {
          roleNames.add(roleName);
        }
      }
      dropboxUser.adminRole = highestAdminRole(roleNames);
    }

    return dropboxUser;
  }

  /**
   * Build the member entry for POST /2/team/members/add_v2 (new_members array element).
   * @return the new-member request object
   */
  public ObjectNode toAddJson() {
    ObjectNode memberNode = GrouperUtil.jsonJacksonNode();
    memberNode.put("member_email", this.email);
    memberNode.put("member_given_name",
        GrouperUtil.defaultIfBlank(this.givenName, "Unknown"));
    memberNode.put("member_surname",
        GrouperUtil.defaultIfBlank(this.surname, "Unknown"));
    if (!GrouperUtil.isBlank(this.externalId)) {
      memberNode.put("member_external_id", this.externalId);
    }
    memberNode.put("send_welcome_email", false);
    return memberNode;
  }

  /**
   * Build the request body for POST /2/team/members/set_profile_v2.  The member
   * is selected by team_member_id; only the supplied new_* fields are changed.
   * @param fieldNamesToSet field names to set, or null for all
   * @return the set_profile request body
   */
  public ObjectNode toSetProfileJson(Set<String> fieldNamesToSet) {
    ObjectNode bodyNode = GrouperUtil.jsonJacksonNode();

    bodyNode.set("user", toUserSelectorJson());

    if ((fieldNamesToSet == null || fieldNamesToSet.contains("email")) && this.email != null) {
      bodyNode.put("new_email", this.email);
    }
    if ((fieldNamesToSet == null || fieldNamesToSet.contains("externalId")) && this.externalId != null) {
      bodyNode.put("new_external_id", this.externalId);
    }
    if ((fieldNamesToSet == null || fieldNamesToSet.contains("givenName")) && this.givenName != null) {
      bodyNode.put("new_given_name", this.givenName);
    }
    if ((fieldNamesToSet == null || fieldNamesToSet.contains("surname")) && this.surname != null) {
      bodyNode.put("new_surname", this.surname);
    }
    return bodyNode;
  }

  /**
   * Build a UserSelectorArg union selecting this member by team_member_id.
   * @return the user selector JSON
   */
  public ObjectNode toUserSelectorJson() {
    ObjectNode userNode = GrouperUtil.jsonJacksonNode();
    userNode.put(".tag", "team_member_id");
    userNode.put("team_member_id", this.id);
    return userNode;
  }

  /**
   * Build the request body for POST /2/team/members/set_admin_permissions_v2.
   * The member is selected by team_member_id.  new_roles holds the resolved
   * Dropbox role ids; an empty list demotes the member to member_only (no admin
   * rights).
   * @param roleIds the Dropbox role ids to assign (empty for member_only)
   * @return the set_admin_permissions request body
   */
  public ObjectNode toSetAdminPermissionsJson(java.util.List<String> roleIds) {
    ObjectNode bodyNode = GrouperUtil.jsonJacksonNode();
    bodyNode.set("user", toUserSelectorJson());
    com.fasterxml.jackson.databind.node.ArrayNode rolesArray = GrouperUtil.jsonJacksonArrayNode();
    for (String roleId : GrouperUtil.nonNull(roleIds)) {
      rolesArray.add(roleId);
    }
    bodyNode.set("new_roles", rolesArray);
    return bodyNode;
  }

  /**
   * Convert to a Grouper provisioning entity.  The provisioning entity id is the
   * native team_member_id; matching is configured on the externalId attribute.
   * @return the converted target entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);

    targetEntity.assignAttributeValue("id", this.id);
    if (this.email != null) {
      targetEntity.assignAttributeValue("email", this.email);
    }
    if (this.externalId != null) {
      targetEntity.assignAttributeValue("externalId", this.externalId);
    }
    if (this.givenName != null) {
      targetEntity.assignAttributeValue("givenName", this.givenName);
    }
    if (this.surname != null) {
      targetEntity.assignAttributeValue("surname", this.surname);
    }
    if (this.status != null) {
      targetEntity.assignAttributeValue("status", this.status);
      // expose the lifecycle state derived from status so the framework can diff it against the
      // desired lifecycleState the translator stamps (suspended vs active)
      targetEntity.assignAttributeValue("lifecycleState",
          STATUS_SUSPENDED.equals(this.status) ? LIFECYCLE_STATE_SUSPENDED : LIFECYCLE_STATE_ACTIVE);
    }
    if (this.accountId != null) {
      targetEntity.assignAttributeValue("accountId", this.accountId);
    }
    if (this.adminRole != null) {
      targetEntity.assignAttributeValue("adminRole", this.adminRole);
    }

    return targetEntity;
  }

  /**
   * Convert from a Grouper provisioning entity to a DropboxUser.
   * @param targetEntity the Grouper provisioning entity
   * @param fieldNamesToSet field names to set, or null for all
   * @return the DropboxUser
   */
  public static DropboxUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    DropboxUser dropboxUser = new DropboxUser();

    // always carry the native team_member_id (needed for selectors)
    dropboxUser.setId(targetEntity.getId());

    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {
      dropboxUser.setEmail(targetEntity.retrieveAttributeValueString("email"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("externalId")) {
      dropboxUser.setExternalId(targetEntity.retrieveAttributeValueString("externalId"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("givenName")) {
      dropboxUser.setGivenName(targetEntity.retrieveAttributeValueString("givenName"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("surname")) {
      dropboxUser.setSurname(targetEntity.retrieveAttributeValueString("surname"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("status")) {
      dropboxUser.setStatus(targetEntity.retrieveAttributeValueString("status"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("adminRole")) {
      dropboxUser.setAdminRole(targetEntity.retrieveAttributeValueString("adminRole"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("lifecycleState")) {
      dropboxUser.setLifecycleState(targetEntity.retrieveAttributeValueString("lifecycleState"));
    }

    return dropboxUser;
  }

}
