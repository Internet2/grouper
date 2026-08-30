package edu.internet2.middleware.grouper.app.jamf;

import java.sql.Types;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * A Jamf Pro admin account (the object under <code>/JSSResource/accounts/userid/{id}</code>).
 * This is the provisioner's TARGET ENTITY.
 *
 * <p>Accounts are matched and created by {@link #name} = lowercased EPPN
 * (<code>pennkey@upenn.edu</code>). Grouper creates an account when it needs to add someone to a
 * role and no account exists yet; it never updates or deletes accounts. New accounts are created
 * with access_level "Group Access" (privileges come from role membership) and a random password
 * (console login is via SSO, but the Classic API requires a password on create).</p>
 */
public class JamfAccount {

  /** access_level value for accounts Grouper creates -- privileges are inherited from roles */
  public static final String ACCESS_LEVEL_GROUP_ACCESS = "Group Access";

  /** enabled value string as returned/accepted by the Jamf Classic API */
  public static final String ENABLED = "Enabled";

  /** disabled value string as returned/accepted by the Jamf Classic API */
  public static final String DISABLED = "Disabled";

  /**
   * Create the mock DB table used by the test mock service to simulate Jamf accounts.
   * @param ddlVersionBean ddl bean (unused but part of the createTable contract)
   * @param database the ddlutils database to add the table to
   */
  public static void createTableJamfAccount(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_jamf_account";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "full_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "access_level", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "privilege_set", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "enabled", Types.VARCHAR, "20", false, false);
      // "T"/"F" -- whether the account is directory-linked; Grouper never touches directory accounts
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "directory_user", Types.VARCHAR, "1", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_jamf_acct_name_idx", true, "name");
    }

  }

  /** native Jamf account id (numeric, held as a string); null until created */
  private String id;

  /** account name = EPPN (pennkey@upenn.edu), the match/create key */
  private String name;

  /** full display name */
  private String fullName;

  /** email address (same as name in this tenant) */
  private String email;

  /**
   * the Jamf {@code email_address} field. Jamf carries two email elements ({@code email} and
   * {@code email_address}); Grouper writes the same value to both, but reads keep this one separate
   * so the ignore list can match against either.
   */
  private String emailAddress;

  /** "Full Access", "Site Access", or "Group Access" */
  private String accessLevel;

  /** "Administrator", "Auditor", "Enrollment Only", or "Custom" (null for Group Access) */
  private String privilegeSet;

  /** "Enabled" or "Disabled" */
  private String enabled;

  /** true if the account is directory-linked; such accounts are never modified by Grouper */
  private Boolean directoryUser;

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

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String emailAddress) {
    this.emailAddress = emailAddress;
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

  public String getEnabled() {
    return enabled;
  }

  public void setEnabled(String enabled) {
    this.enabled = enabled;
  }

  /**
   * @return true if this account is disabled in Jamf. A null/blank enabled value is treated as
   *   enabled (the Classic API returns "Enabled"/"Disabled"; some payloads use "true"/"false").
   */
  public boolean isDisabled() {
    if (GrouperClientUtils.isBlank(this.enabled)) {
      return false;
    }
    return DISABLED.equalsIgnoreCase(this.enabled) || "false".equalsIgnoreCase(this.enabled);
  }

  public Boolean getDirectoryUser() {
    return directoryUser;
  }

  public void setDirectoryUser(Boolean directoryUser) {
    this.directoryUser = directoryUser;
  }

  /** mock-DB accessor: directory_user stored as "T"/"F" */
  public String getDirectoryUserString() {
    return booleanToTf(this.directoryUser);
  }

  /** mock-DB accessor: directory_user stored as "T"/"F" */
  public void setDirectoryUserString(String directoryUserString) {
    this.directoryUser = GrouperUtil.booleanObjectValue(directoryUserString);
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Parse a Jamf account from JSON. Accepts either a bare account object or the
   * <code>{"account": {...}}</code> wrapper returned by the account GET endpoints.
   * @param node the JSON node (account object or {"account":...} wrapper)
   * @return the JamfAccount, or null if node is null
   */
  public static JamfAccount fromJson(JsonNode node) {
    if (node == null) {
      return null;
    }

    JsonNode accountNode = node.has("account") ? node.get("account") : node;

    JamfAccount account = new JamfAccount();
    account.id = GrouperUtil.jsonJacksonGetString(accountNode, "id");
    account.name = GrouperUtil.jsonJacksonGetString(accountNode, "name");
    account.fullName = GrouperUtil.jsonJacksonGetString(accountNode, "full_name");
    account.email = GrouperUtil.jsonJacksonGetString(accountNode, "email");
    account.emailAddress = GrouperUtil.jsonJacksonGetString(accountNode, "email_address");
    account.accessLevel = GrouperUtil.jsonJacksonGetString(accountNode, "access_level");
    account.privilegeSet = GrouperUtil.jsonJacksonGetString(accountNode, "privilege_set");
    account.enabled = GrouperUtil.jsonJacksonGetString(accountNode, "enabled");
    account.directoryUser = GrouperUtil.jsonJacksonGetBoolean(accountNode, "directory_user");

    return account;
  }

  /**
   * Convert to a Grouper provisioning entity. The account name (EPPN) is the matching attribute;
   * id is the native Jamf id (null until the account has been created).
   * @return the converted provisioning entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);

    if (this.id != null) {
      targetEntity.setId(this.id);
    }
    targetEntity.assignAttributeValue("name", this.name);
    if (this.fullName != null) {
      targetEntity.assignAttributeValue("fullName", this.fullName);
    }
    if (this.email != null) {
      targetEntity.assignAttributeValue("email", this.email);
    }
    if (this.accessLevel != null) {
      targetEntity.assignAttributeValue("accessLevel", this.accessLevel);
    }

    return targetEntity;
  }

  /**
   * Build a JamfAccount from a Grouper provisioning entity for a create. The account name is used
   * exactly as Grouper computed it (from the configured entity attribute) so it matches the value
   * the framework compares against -- do NOT normalize case here, or a created account will not
   * link back to its Grouper entity. In production the name comes from the EPPN
   * (subjectIdentifier2), which is already lowercase.
   * @param targetEntity the Grouper provisioning entity
   * @return the JamfAccount ready to create
   */
  public static JamfAccount fromProvisioningEntity(ProvisioningEntity targetEntity) {
    JamfAccount account = new JamfAccount();

    account.setId(targetEntity.getId());
    account.setName(targetEntity.retrieveAttributeValueString("name"));

    account.setFullName(targetEntity.retrieveAttributeValueString("fullName"));
    account.setEmail(targetEntity.retrieveAttributeValueString("email"));
    // Grouper manages one email attribute; it maps to both Jamf email fields, so mirror it here so
    // the ignore list can match a to-be-created account on either email element
    account.setEmailAddress(account.getEmail());
    account.setAccessLevel(targetEntity.retrieveAttributeValueString("accessLevel"));

    return account;
  }

  private static String booleanToTf(Boolean value) {
    if (value == null) {
      return null;
    }
    return value.booleanValue() ? "T" : "F";
  }

}
