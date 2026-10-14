package edu.internet2.middleware.grouper.app.smartsheet;

import java.sql.Types;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

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
 * A Smartsheet organization user (the object under <code>/2.0/users/{userId}</code>). This is the
 * provisioner's TARGET ENTITY.
 *
 * <p>Users are matched by the native Smartsheet user id (stable, cached once linked) with email as
 * the backup for the first link. Grouper adds users to the organization (POST /users), updates the
 * name and the account flags (admin, licensedSheetCreator, groupAdmin, resourceViewer), and on
 * delete either deactivates the user (default) or removes them from the organization.</p>
 *
 * <p>The account flags and status are only returned to a System Admin caller, so the token used by
 * the external system should belong to a System Admin.</p>
 */
public class SmartsheetUser {

  /** status of an active user */
  public static final String STATUS_ACTIVE = "ACTIVE";

  /** status of a user who was invited but has not accepted yet */
  public static final String STATUS_PENDING = "PENDING";

  /** status of a user who declined the invitation */
  public static final String STATUS_DECLINED = "DECLINED";

  /** status of a deactivated user (cannot use Smartsheet; sharing is unchanged) */
  public static final String STATUS_DEACTIVATED = "DEACTIVATED";

  /** seat type: paid seat (user subscription model) */
  public static final String SEAT_MEMBER = "MEMBER";

  /** seat type: temporary paid seat granted by Smartsheet; cannot be set by the API */
  public static final String SEAT_PROVISIONAL_MEMBER = "PROVISIONAL_MEMBER";

  /** seat type: free seat, can edit what is shared with them but not create or own */
  public static final String SEAT_CONTRIBUTOR = "CONTRIBUTOR";

  /** seat type: external users only */
  public static final String SEAT_GUEST = "GUEST";

  /** seat type: deprecated name for the free seat, treated as CONTRIBUTOR */
  public static final String SEAT_VIEWER = "VIEWER";

  /** entity attribute holding the computed / current seat type */
  public static final String ATTR_SEAT_TYPE = "seatType";

  /**
   * entity attribute (not configured, informational) holding "false" for a user outside the org, so
   * a user reconstructed from the sync-back cache is still recognized as external
   */
  public static final String ATTR_IS_INTERNAL = "isInternal";

  /**
   * @param seatType a seat type from Smartsheet or config
   * @return the upper-cased seat type, with the deprecated VIEWER mapped to CONTRIBUTOR; null if blank
   */
  public static String normalizeSeatType(String seatType) {
    if (StringUtils.isBlank(seatType)) {
      return null;
    }
    String result = seatType.trim().toUpperCase();
    return SEAT_VIEWER.equals(result) ? SEAT_CONTRIBUTOR : result;
  }

  /** the boolean account flags Grouper can manage on a user */
  public static final Set<String> BOOLEAN_FLAG_ATTRIBUTES = GrouperUtil.toSet(
      "admin", "licensedSheetCreator", "groupAdmin", "resourceViewer");

  /**
   * Create the mock DB table used by the test mock service to simulate Smartsheet users.
   * @param ddlVersionBean ddl bean (unused but part of the createTable contract)
   * @param database the ddlutils database to add the table to
   */
  public static void createTableSmartsheetUser(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_smartsheet_user";

    try {
      // select the newest column so a table from an older version gets the columns added
      new GcDbAccess().sql("select count(seat_type) from " + tableName).select(int.class);
    } catch (Exception e) {
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "first_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", Types.VARCHAR, "40", false, false);
      // account flags stored as "T"/"F"
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "admin", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "licensed_sheet_creator", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_admin", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "resource_viewer", Types.VARCHAR, "1", false, false);
      // seat type and internal ("T"/"F"), only returned when a planId is passed
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "seat_type", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "is_internal", Types.VARCHAR, "1", false, false);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_smartsheet_user_email_idx", true, "email");
    }

  }

  /** native Smartsheet user id (a long, held as a string); null until created */
  private String id;

  /** primary email address, the backup match key */
  private String email;

  /** first name */
  private String firstName;

  /** last name */
  private String lastName;

  /** ACTIVE, PENDING, DECLINED, or DEACTIVATED (System Admin callers only) */
  private String status;

  /** true if the user is a System Admin */
  private Boolean admin;

  /** true if the user can create and own sheets */
  private Boolean licensedSheetCreator;

  /** true if the user can create and edit groups */
  private Boolean groupAdmin;

  /** true if the user can access resource views */
  private Boolean resourceViewer;

  /** MEMBER, PROVISIONAL_MEMBER, CONTRIBUTOR, GUEST (only returned when a planId is passed) */
  private String seatType;

  /**
   * false for a user outside the organization (e.g. an external collaborator on the plan); only
   * returned when a planId is passed, null otherwise
   */
  private Boolean internal;

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

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Boolean getAdmin() {
    return admin;
  }

  public void setAdmin(Boolean admin) {
    this.admin = admin;
  }

  public Boolean getLicensedSheetCreator() {
    return licensedSheetCreator;
  }

  public void setLicensedSheetCreator(Boolean licensedSheetCreator) {
    this.licensedSheetCreator = licensedSheetCreator;
  }

  public Boolean getGroupAdmin() {
    return groupAdmin;
  }

  public void setGroupAdmin(Boolean groupAdmin) {
    this.groupAdmin = groupAdmin;
  }

  public Boolean getResourceViewer() {
    return resourceViewer;
  }

  public void setResourceViewer(Boolean resourceViewer) {
    this.resourceViewer = resourceViewer;
  }

  public String getSeatType() {
    return seatType;
  }

  public void setSeatType(String seatType) {
    this.seatType = seatType;
  }

  public Boolean getInternal() {
    return internal;
  }

  public void setInternal(Boolean internal) {
    this.internal = internal;
  }

  /**
   * @return true if Smartsheet says this user is outside the organization (only known when read with a planId)
   */
  public boolean isExternal() {
    return this.internal != null && !this.internal;
  }

  /**
   * @return true if this user is deactivated in Smartsheet
   */
  public boolean isDeactivated() {
    return STATUS_DEACTIVATED.equalsIgnoreCase(this.status);
  }

  // mock-DB accessors: the boolean flags are stored as "T"/"F"

  public String getAdminDb() {
    return booleanToTf(this.admin);
  }

  public void setAdminDb(String adminDb) {
    this.admin = GrouperUtil.booleanObjectValue(adminDb);
  }

  public String getLicensedSheetCreatorDb() {
    return booleanToTf(this.licensedSheetCreator);
  }

  public void setLicensedSheetCreatorDb(String licensedSheetCreatorDb) {
    this.licensedSheetCreator = GrouperUtil.booleanObjectValue(licensedSheetCreatorDb);
  }

  public String getGroupAdminDb() {
    return booleanToTf(this.groupAdmin);
  }

  public void setGroupAdminDb(String groupAdminDb) {
    this.groupAdmin = GrouperUtil.booleanObjectValue(groupAdminDb);
  }

  public String getInternalDb() {
    return booleanToTf(this.internal);
  }

  public void setInternalDb(String internalDb) {
    this.internal = GrouperUtil.booleanObjectValue(internalDb);
  }

  public String getResourceViewerDb() {
    return booleanToTf(this.resourceViewer);
  }

  public void setResourceViewerDb(String resourceViewerDb) {
    this.resourceViewer = GrouperUtil.booleanObjectValue(resourceViewerDb);
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * Parse a Smartsheet user from JSON (an element of GET /users data[], the GET /users/{id}
   * profile, or the result of a POST/PUT). Numeric ids come back as strings.
   * @param node the user JSON object
   * @return the SmartsheetUser, or null if node is null
   */
  public static SmartsheetUser fromJson(JsonNode node) {
    if (node == null || node.isNull()) {
      return null;
    }

    SmartsheetUser user = new SmartsheetUser();
    user.id = GrouperUtil.jsonJacksonGetString(node, "id");
    user.email = GrouperUtil.jsonJacksonGetString(node, "email");
    user.firstName = GrouperUtil.jsonJacksonGetString(node, "firstName");
    user.lastName = GrouperUtil.jsonJacksonGetString(node, "lastName");
    user.status = GrouperUtil.jsonJacksonGetString(node, "status");
    user.admin = GrouperUtil.jsonJacksonGetBoolean(node, "admin");
    user.licensedSheetCreator = GrouperUtil.jsonJacksonGetBoolean(node, "licensedSheetCreator");
    user.groupAdmin = GrouperUtil.jsonJacksonGetBoolean(node, "groupAdmin");
    user.resourceViewer = GrouperUtil.jsonJacksonGetBoolean(node, "resourceViewer");
    user.seatType = GrouperUtil.jsonJacksonGetString(node, "seatType");
    user.internal = GrouperUtil.jsonJacksonGetBoolean(node, "isInternal");
    return user;
  }

  /**
   * Build the JSON body for POST /users (add user to the organization). Smartsheet requires the
   * admin and licensedSheetCreator flags on create, so they default to false when Grouper does not
   * manage them. Note: on user subscription model plans Smartsheet forces licensedSheetCreator to
   * true regardless of the value sent.
   * @return the JSON object
   */
  public ObjectNode toJsonForCreate() {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("email", this.email);
    if (this.firstName != null) {
      result.put("firstName", this.firstName);
    }
    if (this.lastName != null) {
      result.put("lastName", this.lastName);
    }
    result.put("admin", this.admin != null && this.admin);
    // Smartsheet refuses an unlicensed Group Admin / Resource Viewer, so when Grouper does not manage
    // the license flag, license those users
    boolean needsLicense = (this.groupAdmin != null && this.groupAdmin) || (this.resourceViewer != null && this.resourceViewer);
    result.put("licensedSheetCreator", this.licensedSheetCreator != null ? this.licensedSheetCreator : needsLicense);
    if (this.groupAdmin != null) {
      result.put("groupAdmin", this.groupAdmin);
    }
    if (this.resourceViewer != null) {
      result.put("resourceViewer", this.resourceViewer);
    }
    return result;
  }

  /**
   * Build the JSON body for PUT /users/{id}. Only the fields in fieldNamesToUpdate are sent so
   * Grouper never overwrites fields it does not manage. Email is not updatable via this endpoint.
   * @param fieldNamesToUpdate target attribute names that changed; null sends all managed fields
   * @return the JSON object
   */
  public ObjectNode toJsonForUpdate(Set<String> fieldNamesToUpdate) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    if (fieldNamesToUpdate == null || fieldNamesToUpdate.contains("firstName")) {
      result.put("firstName", StringUtils.defaultString(this.firstName));
    }
    if (fieldNamesToUpdate == null || fieldNamesToUpdate.contains("lastName")) {
      result.put("lastName", StringUtils.defaultString(this.lastName));
    }
    putBooleanIfUpdating(result, fieldNamesToUpdate, "admin", this.admin);
    putBooleanIfUpdating(result, fieldNamesToUpdate, "licensedSheetCreator", this.licensedSheetCreator);
    putBooleanIfUpdating(result, fieldNamesToUpdate, "groupAdmin", this.groupAdmin);
    putBooleanIfUpdating(result, fieldNamesToUpdate, "resourceViewer", this.resourceViewer);
    return result;
  }

  /**
   * Put a boolean flag in an update body if it is being updated and has a value. A null (unset) flag
   * is never sent: an attribute with no value means Grouper does not manage it, and sending false
   * would quietly strip e.g. System Admin from the user.
   */
  private static void putBooleanIfUpdating(ObjectNode result, Set<String> fieldNamesToUpdate,
      String fieldName, Boolean value) {
    if (value == null) {
      return;
    }
    if (fieldNamesToUpdate == null || fieldNamesToUpdate.contains(fieldName)) {
      result.put(fieldName, value);
    }
  }

  /**
   * Convert to a Grouper provisioning entity. id is the native user id, email is the backup
   * matching attribute. The boolean flags are held as the strings "true" / "false" (the provisioning
   * framework has no boolean attribute type), matching what translations and the group-driven flags
   * produce, so an unchanged flag compares equal.
   * @return the converted provisioning entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);

    if (this.id != null) {
      targetEntity.setId(this.id);
    }
    targetEntity.assignAttributeValue("email", this.email);
    if (this.firstName != null) {
      targetEntity.assignAttributeValue("firstName", this.firstName);
    }
    if (this.lastName != null) {
      targetEntity.assignAttributeValue("lastName", this.lastName);
    }
    if (this.status != null) {
      targetEntity.assignAttributeValue("status", this.status);
    }
    if (this.admin != null) {
      targetEntity.assignAttributeValue("admin", this.admin.toString());
    }
    if (this.licensedSheetCreator != null) {
      targetEntity.assignAttributeValue("licensedSheetCreator", this.licensedSheetCreator.toString());
    }
    if (this.groupAdmin != null) {
      targetEntity.assignAttributeValue("groupAdmin", this.groupAdmin.toString());
    }
    if (this.resourceViewer != null) {
      targetEntity.assignAttributeValue("resourceViewer", this.resourceViewer.toString());
    }
    if (this.seatType != null) {
      targetEntity.assignAttributeValue(ATTR_SEAT_TYPE, normalizeSeatType(this.seatType));
    }
    if (this.internal != null) {
      targetEntity.assignAttributeValue(ATTR_IS_INTERNAL, this.internal.toString());
    }

    return targetEntity;
  }

  /**
   * Build a SmartsheetUser from a Grouper provisioning entity. The email is used exactly as
   * Grouper computed it so a created user links back to its Grouper entity.
   * @param targetEntity the Grouper provisioning entity
   * @return the SmartsheetUser
   */
  public static SmartsheetUser fromProvisioningEntity(ProvisioningEntity targetEntity) {
    SmartsheetUser user = new SmartsheetUser();

    user.setId(targetEntity.getId());
    user.setEmail(targetEntity.retrieveAttributeValueString("email"));
    user.setFirstName(targetEntity.retrieveAttributeValueString("firstName"));
    user.setLastName(targetEntity.retrieveAttributeValueString("lastName"));
    user.setAdmin(booleanAttribute(targetEntity, "admin"));
    user.setLicensedSheetCreator(booleanAttribute(targetEntity, "licensedSheetCreator"));
    user.setGroupAdmin(booleanAttribute(targetEntity, "groupAdmin"));
    user.setResourceViewer(booleanAttribute(targetEntity, "resourceViewer"));
    user.setSeatType(normalizeSeatType(targetEntity.retrieveAttributeValueString(ATTR_SEAT_TYPE)));

    return user;
  }

  /**
   * Read a boolean entity attribute that may be held as a Boolean or as a "true"/"false" string
   * (depending on whether a valueType is configured on the attribute).
   * @param targetEntity the entity
   * @param attributeName the attribute
   * @return the boolean, or null if not set
   */
  private static Boolean booleanAttribute(ProvisioningEntity targetEntity, String attributeName) {
    Object value = targetEntity.retrieveAttributeValue(attributeName);
    if (value == null || (value instanceof String && StringUtils.isBlank((String) value))) {
      return null;
    }
    return GrouperUtil.booleanObjectValue(value);
  }

  private static String booleanToTf(Boolean value) {
    if (value == null) {
      return null;
    }
    return value.booleanValue() ? "T" : "F";
  }

}
