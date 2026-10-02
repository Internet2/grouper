package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.sql.Types;

import org.apache.commons.lang3.StringUtils;

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
 * An AssetSonar member (the object under <code>/members/&lt;id&gt;.api</code>). This is the
 * provisioner's TARGET ENTITY.
 *
 * <p>A member is both an account and an asset custodian: equipment is checked out to a member,
 * so member records carry history that must never be destroyed. That is why the provisioner
 * deactivates ({@code status=0}) instead of deleting.</p>
 *
 * <p>Only the attributes Grouper manages are modeled. The real payload carries ~110 fields;
 * everything else is ignored. There is deliberately no {@code external_id} field: sending
 * {@code user[external_id]} to AssetSonar nulls the member's email (see
 * {@link AssetSonarApiCommands}).</p>
 */
public class AssetSonarMember {

  /** target attribute names (also the user[...] form parameter names and JSON keys) */
  public static final String ATTR_ID = "id";
  public static final String ATTR_EMAIL = "email";
  public static final String ATTR_FIRST_NAME = "first_name";
  public static final String ATTR_LAST_NAME = "last_name";
  public static final String ATTR_EMPLOYEE_ID = "employee_id";
  public static final String ATTR_EMPLOYEE_IDENTIFICATION_NUMBER = "employee_identification_number";
  public static final String ATTR_ROLE_ID = "role_id";
  public static final String ATTR_STATUS = "status";

  /** status value the API stores for an active member */
  public static final String STATUS_ACTIVE = "1";

  /** status value the API stores for a deactivated member */
  public static final String STATUS_INACTIVE = "0";

  /**
   * Create the mock DB table used by the test mock service to simulate AssetSonar members.
   * @param ddlVersionBean ddl bean (unused but part of the createTable contract)
   * @param database the ddlutils database to add the table to
   */
  public static void createTableAssetSonarMember(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_asset_sonar_member";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      // email is nullable on purpose: the mock reproduces user[external_id] nulling it
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "first_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "employee_id", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "employee_identification_number", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "role_id", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", Types.VARCHAR, "1", false, false);
      // never written by the real REST API (only its LDAP integration); kept so the mock can show
      // that user[external_id] sets nothing
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "external_id", Types.VARCHAR, "256", false, false);

      // not unique: the mock enforces email uniqueness itself (case-insensitively) and must be able
      // to hold several null emails after the external_id bug
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_asset_sonar_mem_email_idx", false, "email");
    }

  }

  /** native numeric member id, held as a string; null until created */
  private String id;

  /** email: the login identity and the ONLY attribute the target enforces uniqueness on */
  private String email;

  private String firstName;

  private String lastName;

  /** in an LDAP-fed tenant this holds the account name. NOT unique, NOT a match key */
  private String employeeId;

  /** writable free-form identifier; the durable cross-reference (e.g. institutional id) */
  private String employeeIdentificationNumber;

  /** tenant-specific access tier id, see the external system role id config */
  private String roleId;

  /** "1" active, "0" deactivated. Authoritative (deactivated_at is not set by REST) */
  private String status;

  /** mock only, see the class javadoc */
  private String externalId;

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

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public void setEmployeeId(String employeeId) {
    this.employeeId = employeeId;
  }

  public String getEmployeeIdentificationNumber() {
    return employeeIdentificationNumber;
  }

  public void setEmployeeIdentificationNumber(String employeeIdentificationNumber) {
    this.employeeIdentificationNumber = employeeIdentificationNumber;
  }

  public String getRoleId() {
    return roleId;
  }

  public void setRoleId(String roleId) {
    this.roleId = roleId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  /**
   * @return true if the member is deactivated. Only an explicit 0 counts; a missing status is
   *   treated as active since the API always returns one.
   */
  public boolean isInactive() {
    return STATUS_INACTIVE.equals(StringUtils.trim(this.status));
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Parse a member from the JSON the API returns: a bare member object, both from
   * <code>GET /members/&lt;id&gt;.api</code> and as each entry of the list's "members" array.
   * Numeric fields (id, role_id, status) are kept as strings.
   * @param node the member JSON object
   * @return the member, or null if node is null
   */
  public static AssetSonarMember fromJson(JsonNode node) {
    if (node == null) {
      return null;
    }
    AssetSonarMember member = new AssetSonarMember();
    member.id = GrouperUtil.jsonJacksonGetString(node, ATTR_ID);
    member.email = GrouperUtil.jsonJacksonGetString(node, ATTR_EMAIL);
    member.firstName = GrouperUtil.jsonJacksonGetString(node, ATTR_FIRST_NAME);
    member.lastName = GrouperUtil.jsonJacksonGetString(node, ATTR_LAST_NAME);
    member.employeeId = GrouperUtil.jsonJacksonGetString(node, ATTR_EMPLOYEE_ID);
    member.employeeIdentificationNumber = GrouperUtil.jsonJacksonGetString(node, ATTR_EMPLOYEE_IDENTIFICATION_NUMBER);
    member.roleId = GrouperUtil.jsonJacksonGetString(node, ATTR_ROLE_ID);
    member.status = GrouperUtil.jsonJacksonGetString(node, ATTR_STATUS);
    return member;
  }

  /**
   * Convert to a Grouper provisioning entity. Blank values are not assigned so they compare as
   * absent rather than as an empty string.
   * @return the provisioning entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);
    if (!StringUtils.isBlank(this.id)) {
      targetEntity.setId(this.id);
    }
    assignIfNotBlank(targetEntity, ATTR_EMAIL, this.email);
    assignIfNotBlank(targetEntity, ATTR_FIRST_NAME, this.firstName);
    assignIfNotBlank(targetEntity, ATTR_LAST_NAME, this.lastName);
    assignIfNotBlank(targetEntity, ATTR_EMPLOYEE_ID, this.employeeId);
    assignIfNotBlank(targetEntity, ATTR_EMPLOYEE_IDENTIFICATION_NUMBER, this.employeeIdentificationNumber);
    assignIfNotBlank(targetEntity, ATTR_ROLE_ID, this.roleId);
    assignIfNotBlank(targetEntity, ATTR_STATUS, this.status);
    return targetEntity;
  }

  /**
   * Build a member from a Grouper provisioning entity.
   * @param targetEntity the provisioning entity
   * @return the member
   */
  public static AssetSonarMember fromProvisioningEntity(ProvisioningEntity targetEntity) {
    AssetSonarMember member = new AssetSonarMember();
    member.id = targetEntity.getId();
    member.email = targetEntity.retrieveAttributeValueString(ATTR_EMAIL);
    member.firstName = targetEntity.retrieveAttributeValueString(ATTR_FIRST_NAME);
    member.lastName = targetEntity.retrieveAttributeValueString(ATTR_LAST_NAME);
    member.employeeId = targetEntity.retrieveAttributeValueString(ATTR_EMPLOYEE_ID);
    member.employeeIdentificationNumber = targetEntity.retrieveAttributeValueString(ATTR_EMPLOYEE_IDENTIFICATION_NUMBER);
    member.roleId = targetEntity.retrieveAttributeValueString(ATTR_ROLE_ID);
    member.status = targetEntity.retrieveAttributeValueString(ATTR_STATUS);
    return member;
  }

  /**
   * @param attributeName one of the ATTR_ constants
   * @return the value of that attribute on this member
   */
  public String attributeValue(String attributeName) {
    if (ATTR_EMAIL.equals(attributeName)) {
      return this.email;
    }
    if (ATTR_FIRST_NAME.equals(attributeName)) {
      return this.firstName;
    }
    if (ATTR_LAST_NAME.equals(attributeName)) {
      return this.lastName;
    }
    if (ATTR_EMPLOYEE_ID.equals(attributeName)) {
      return this.employeeId;
    }
    if (ATTR_EMPLOYEE_IDENTIFICATION_NUMBER.equals(attributeName)) {
      return this.employeeIdentificationNumber;
    }
    if (ATTR_ROLE_ID.equals(attributeName)) {
      return this.roleId;
    }
    if (ATTR_STATUS.equals(attributeName)) {
      return this.status;
    }
    return null;
  }

  private static void assignIfNotBlank(ProvisioningEntity targetEntity, String attributeName, String value) {
    if (!StringUtils.isBlank(value)) {
      targetEntity.assignAttributeValue(attributeName, value);
    }
  }

}
