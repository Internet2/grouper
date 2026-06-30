package edu.internet2.middleware.grouper.app.interfolio;

import java.sql.Types;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Model object for an Interfolio user, used to back the mock service.
 *
 * Interfolio identifies a person by two ids: the stable cross-product "pid" (returned by the IAM
 * create and the byc users/search), and a byc-internal "id".  The provisioner keys on the pid.
 *
 * The user also carries the immutable institution_user_id (UID/PennKey), saml_id (pennkey@upenn.edu),
 * user_type, name and email, plus two booleans tracking whether the user is subscribed to the RPT and
 * FS products (toggled by the byc subscribe/unsubscribe endpoints).
 */
public class InterfolioUser {

  /** stable cross-product person id (returned as "pid") - the provisioner key */
  private String pid;

  /** byc-internal user id (returned as "id" by users/search) */
  private String bycId;

  /** UID / PennKey - immutable once set */
  private String institutionUserId;

  /** pennkey@upenn.edu */
  private String samlId;

  /** typically "internal" */
  private String userType;

  private String firstName;

  private String lastName;

  private String email;

  /** subscribed to RPT (byc-tenure) - stored as "T"/"F" */
  private String rpt;

  /** subscribed to FS (byc-search) - stored as "T"/"F" */
  private String fs;

  public String getPid() {
    return pid;
  }

  public void setPid(String pid) {
    this.pid = pid;
  }

  public String getBycId() {
    return bycId;
  }

  public void setBycId(String bycId) {
    this.bycId = bycId;
  }

  public String getInstitutionUserId() {
    return institutionUserId;
  }

  public void setInstitutionUserId(String institutionUserId) {
    this.institutionUserId = institutionUserId;
  }

  public String getSamlId() {
    return samlId;
  }

  public void setSamlId(String samlId) {
    this.samlId = samlId;
  }

  public String getUserType() {
    return userType;
  }

  public void setUserType(String userType) {
    this.userType = userType;
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

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getRpt() {
    return rpt;
  }

  public void setRpt(String rpt) {
    this.rpt = rpt;
  }

  public String getFs() {
    return fs;
  }

  public void setFs(String fs) {
    this.fs = fs;
  }

  public boolean isSubscribedRpt() {
    return GrouperUtil.booleanValue(this.rpt, false);
  }

  public void setSubscribedRpt(boolean subscribed) {
    this.rpt = subscribed ? "T" : "F";
  }

  public boolean isSubscribedFs() {
    return GrouperUtil.booleanValue(this.fs, false);
  }

  public void setSubscribedFs(boolean subscribed) {
    this.fs = subscribed ? "T" : "F";
  }

  /**
   * Populate the user's account attributes from a JSON body (the IAM create/update body).
   * @param jsonNode parsed request body
   */
  public void assignAttributesFromIamJson(JsonNode jsonNode) {
    this.institutionUserId = GrouperUtil.jsonJacksonGetString(jsonNode, "institution_user_id");
    this.samlId = GrouperUtil.jsonJacksonGetString(jsonNode, "saml_id");
    this.userType = GrouperUtil.jsonJacksonGetString(jsonNode, "user_type");
    this.firstName = GrouperUtil.jsonJacksonGetString(jsonNode, "first_name");
    this.lastName = GrouperUtil.jsonJacksonGetString(jsonNode, "last_name");
    this.email = GrouperUtil.jsonJacksonGetString(jsonNode, "email");
  }

  /**
   * The IAM create/update response shape: { pid, first_name, last_name, email, institution_user_id,
   * user_type }.  Note saml_id is intentionally not echoed (matches the real API).
   * @return the IAM user JSON
   */
  public ObjectNode toIamUserJson() {
    ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
    objectNode.put("pid", GrouperUtil.longValue(this.pid, -1L));
    objectNode.put("first_name", this.firstName);
    objectNode.put("last_name", this.lastName);
    objectNode.put("email", this.email);
    objectNode.put("institution_user_id", this.institutionUserId);
    objectNode.put("user_type", this.userType);
    return objectNode;
  }

  /**
   * The byc users/search result shape for one user.  Includes id and pid, name, email, and the
   * (empty) role/unit/title arrays the real endpoint returns; does NOT include institution_user_id or
   * saml_id (matches the real API).
   * @return the search-result JSON
   */
  public ObjectNode toSearchResultJson() {
    ObjectNode objectNode = GrouperUtil.jsonJacksonNode();
    objectNode.put("id", GrouperUtil.longValue(this.bycId, -1L));
    objectNode.put("pid", this.pid);
    objectNode.put("first_name", this.firstName);
    objectNode.put("last_name", this.lastName);
    objectNode.put("email", this.email);
    objectNode.put("external_user", false);
    objectNode.putNull("role");
    objectNode.set("administrator_unit_names", GrouperUtil.jsonJacksonArrayNode());
    objectNode.set("administrator_institution_ids", GrouperUtil.jsonJacksonArrayNode());
    objectNode.set("administrator_unit_ids", GrouperUtil.jsonJacksonArrayNode());
    objectNode.set("evaluator_unit_names", GrouperUtil.jsonJacksonArrayNode());
    objectNode.set("evaluator_unit_ids", GrouperUtil.jsonJacksonArrayNode());
    ArrayNode titles = GrouperUtil.jsonJacksonArrayNode();
    objectNode.set("titles", titles);
    return objectNode;
  }

  /**
   * Does this user match the given byc search term?  A blank term matches everyone (the real endpoint
   * returns the whole roster when no term is passed); otherwise match (case-insensitive substring)
   * against email, institution_user_id, first or last name.
   * @param searchTerm the term, possibly blank/null
   * @return true if the user matches
   */
  public boolean matchesSearchTerm(String searchTerm) {
    if (GrouperUtil.isBlank(searchTerm)) {
      return true;
    }
    String term = searchTerm.toLowerCase();
    for (String value : new String[] {this.email, this.institutionUserId, this.firstName, this.lastName}) {
      if (value != null && value.toLowerCase().contains(term)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Convert to a Grouper provisioning entity.  The id is the pid; the account attributes become
   * target entity attributes.  Note: when built from a byc users/search result, institution_user_id /
   * saml_id / user_type are not populated (that endpoint omits them).
   * @return the provisioning entity
   */
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity(false);
    targetEntity.setId(this.pid);
    targetEntity.assignAttributeValue("institution_user_id", this.institutionUserId);
    targetEntity.assignAttributeValue("saml_id", this.samlId);
    targetEntity.assignAttributeValue("user_type", this.userType);
    targetEntity.assignAttributeValue("first_name", this.firstName);
    targetEntity.assignAttributeValue("last_name", this.lastName);
    targetEntity.assignAttributeValue("email", this.email);
    return targetEntity;
  }

  /**
   * Build from a Grouper provisioning entity (the target entity attributes).
   * @param targetEntity the provisioning entity
   * @return the Interfolio user
   */
  public static InterfolioUser fromProvisioningEntity(ProvisioningEntity targetEntity) {
    InterfolioUser user = new InterfolioUser();
    user.setPid(targetEntity.getId());
    user.setInstitutionUserId(targetEntity.retrieveAttributeValueString("institution_user_id"));
    user.setSamlId(targetEntity.retrieveAttributeValueString("saml_id"));
    user.setUserType(targetEntity.retrieveAttributeValueString("user_type"));
    user.setFirstName(targetEntity.retrieveAttributeValueString("first_name"));
    user.setLastName(targetEntity.retrieveAttributeValueString("last_name"));
    user.setEmail(targetEntity.retrieveAttributeValueString("email"));
    return user;
  }

  /**
   * DDL for the mock table.
   * @param ddlVersionBean ddl version bean
   * @param database database
   */
  public static void createTableInterfolioUser(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_interfolio_user";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);

      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "pid", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "byc_id", Types.VARCHAR, "40", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "institution_user_id", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "saml_id", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "user_type", Types.VARCHAR, "64", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "first_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "last_name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "rpt", Types.VARCHAR, "1", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "fs", Types.VARCHAR, "1", false, false);
    }
  }

  @Override
  public String toString() {
    return "InterfolioUser[pid=" + pid + ", institutionUserId=" + institutionUserId
        + ", email=" + email + ", rpt=" + rpt + ", fs=" + fs + "]";
  }

}
