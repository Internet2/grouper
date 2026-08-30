package edu.internet2.middleware.grouper.app.jamf;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

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
 * A Jamf Pro account group (a.k.a. a "role"): the object under
 * <code>/JSSResource/accounts/groupid/{id}</code> that holds a privilege set and a list of
 * member accounts. This is the provisioner's TARGET GROUP.
 *
 * <p>Roles are read-only to Grouper -- their privileges are owned by Jamf admins. Grouper only
 * resolves a role by {@link #name} and manages its {@link #members} list. The member list is the
 * account {@code name} (EPPN) of each member; the Jamf API has no atomic add/remove for account
 * groups, so membership is applied by writing the whole list back (see {@link JamfApiCommands}).</p>
 */
public class JamfAccountGroup {

  /**
   * Create the mock DB table used by the test mock service to simulate Jamf account groups.
   * Membership is stored separately in {@link JamfMembership}, so this table holds only the
   * role's own attributes.
   * @param ddlVersionBean ddl bean (unused but part of the createTable contract)
   * @param database the ddlutils database to add the table to
   */
  public static void createTableJamfAccountGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_jamf_account_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "access_level", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "privilege_set", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "site_id", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "site_name", Types.VARCHAR, "256", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_jamf_grp_name_idx", true, "name");
    }

  }

  /** native Jamf account-group id (numeric, held as a string) */
  private String id;

  /** role name -- the link key between a Grouper group and the Jamf role */
  private String name;

  /** "Full Access", "Site Access", or "Group Access" */
  private String accessLevel;

  /** "Administrator", "Auditor", "Enrollment Only", or "Custom" */
  private String privilegeSet;

  /** site id ("-1" means NONE / not site-scoped) */
  private String siteId;

  /** site name ("NONE" when not site-scoped) */
  private String siteName;

  /** member account names (EPPNs); only populated when the full role detail was fetched */
  private List<String> members;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAccessLevel() {
    return accessLevel;
  }

  public void setAccessLevel(String accessLevel) {
    this.accessLevel = accessLevel;
  }

  public String getPrivilegeSet() {
    return privilegeSet;
  }

  public void setPrivilegeSet(String privilegeSet) {
    this.privilegeSet = privilegeSet;
  }

  public String getSiteId() {
    return siteId;
  }

  public void setSiteId(String siteId) {
    this.siteId = siteId;
  }

  public String getSiteName() {
    return siteName;
  }

  public void setSiteName(String siteName) {
    this.siteName = siteName;
  }

  public List<String> getMembers() {
    return members;
  }

  public void setMembers(List<String> members) {
    this.members = members;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Parse a Jamf account group from JSON. Accepts either a bare group object or the
   * <code>{"group": {...}}</code> wrapper returned by
   * <code>GET /JSSResource/accounts/groupid/{id}</code>. When present, the nested
   * <code>members</code> array is parsed into {@link #members} (by account name).
   * @param node the JSON node (group object or {"group":...} wrapper)
   * @return the JamfAccountGroup, or null if node is null
   */
  public static JamfAccountGroup fromJson(JsonNode node) {
    if (node == null) {
      return null;
    }

    // unwrap {"group": {...}} if needed
    JsonNode groupNode = node.has("group") ? node.get("group") : node;

    JamfAccountGroup group = new JamfAccountGroup();
    group.id = GrouperUtil.jsonJacksonGetString(groupNode, "id");
    group.name = GrouperUtil.jsonJacksonGetString(groupNode, "name");
    group.accessLevel = GrouperUtil.jsonJacksonGetString(groupNode, "access_level");
    group.privilegeSet = GrouperUtil.jsonJacksonGetString(groupNode, "privilege_set");

    JsonNode siteNode = GrouperUtil.jsonJacksonGetNode(groupNode, "site");
    if (siteNode != null) {
      group.siteId = GrouperUtil.jsonJacksonGetString(siteNode, "id");
      group.siteName = GrouperUtil.jsonJacksonGetString(siteNode, "name");
    }

    JsonNode membersNode = GrouperUtil.jsonJacksonGetNode(groupNode, "members");
    if (membersNode != null && membersNode.isArray()) {
      List<String> memberList = new ArrayList<String>();
      for (int i = 0; i < membersNode.size(); i++) {
        String memberName = GrouperUtil.jsonJacksonGetString(membersNode.get(i), "name");
        if (!GrouperUtil.isBlank(memberName)) {
          memberList.add(memberName);
        }
      }
      group.members = memberList;
    }

    return group;
  }

  /**
   * Convert to a Grouper provisioning group. The role name is the matching attribute; the other
   * attributes are informational (Grouper never writes them back).
   * @return the converted provisioning group
   */
  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);

    if (this.id != null) {
      targetGroup.setId(this.id);
    }
    targetGroup.assignAttributeValue("name", this.name);
    if (this.accessLevel != null) {
      targetGroup.assignAttributeValue("accessLevel", this.accessLevel);
    }
    if (this.privilegeSet != null) {
      targetGroup.assignAttributeValue("privilegeSet", this.privilegeSet);
    }
    if (this.siteName != null) {
      targetGroup.assignAttributeValue("site", this.siteName);
    }

    return targetGroup;
  }

  /**
   * Build a JamfAccountGroup from a Grouper provisioning group. Only id and name are meaningful --
   * they identify which Jamf role to resolve; Grouper never sends role attributes to Jamf.
   * @param targetGroup the Grouper provisioning group
   * @return the JamfAccountGroup
   */
  public static JamfAccountGroup fromProvisioningGroup(ProvisioningGroup targetGroup) {
    JamfAccountGroup group = new JamfAccountGroup();
    group.setId(targetGroup.getId());
    group.setName(targetGroup.retrieveAttributeValueString("name"));
    return group;
  }

}
