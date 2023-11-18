package edu.internet2.middleware.grouper.app.remedyV2;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperRemedyMembership {
  
  /**
   * status of group, Enabled or Delete
   */
  private String status;
  
  public String getStatus() {
    return status;
  }


  
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * Permission Group ID
   */
  private Long permissionGroupId;
  
  
  /**
   * Permission Group ID
   * @return the permissionGroupId
   */
  public Long getPermissionGroupId() {
    return this.permissionGroupId;
  }

  
  /**
   * Permission Group ID
   * @param permissionGroupId1 the permissionGroupId to set
   */
  public void setPermissionGroupId(Long permissionGroupId1) {
    this.permissionGroupId = permissionGroupId1;
  }

  /**
   * permission group
   */
  private String permissionGroup;
  
  /**
   * permission group
   * @return the permissionGroup
   */
  public String getPermissionGroup() {
    return this.permissionGroup;
  }
  
  /**
   * permission group
   * @param permissionGroup1 the permissionGroup to set
   */
  public void setPermissionGroup(String permissionGroup1) {
    this.permissionGroup = permissionGroup1;
  }

  /**
   * People Permission Group ID
   */
  private String peoplePermissionGroupId;
  
  /**
   * People Permission Group ID
   * @return the peoplePermissionGroupId
   */
  public String getPeoplePermissionGroupId() {
    return this.peoplePermissionGroupId;
  }
  
  /**
   * People Permission Group ID
   * @param peoplePermissionGroupId1 the peoplePermissionGroupId to set
   */
  public void setPeoplePermissionGroupId(String peoplePermissionGroupId1) {
    this.peoplePermissionGroupId = peoplePermissionGroupId1;
  }

  /**
   * remedy id for a person
   */
  private String personId;

  /**
   * netId of user
   */
  private String remedyLoginId;

  
  /**
   * remedy id for a person
   * @return the personId
   */
  public String getPersonId() {
    return this.personId;
  }

  
  /**
   * remedy id for a person
   * @param personId1 the personId to set
   */
  public void setPersonId(String personId1) {
    this.personId = personId1;
  }

  
  /**
   * netId of user
   * @return the remedyLoginId
   */
  public String getRemedyLoginId() {
    return this.remedyLoginId;
  }

  
  /**
   * netId of user
   * @param remedyLoginId1 the remedyLoginId to set
   */
  public void setRemedyLoginId(String remedyLoginId1) {
    this.remedyLoginId = remedyLoginId1;
  }

  
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableRemedyMembership(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_remedy_membership";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {

      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "remedy_login_id", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "person_id", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "permission_group_id", Types.BIGINT, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "permission_group", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "people_permission_group_id", Types.VARCHAR, "40", true, true);
      
    }
    
  }

  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }
  
  /**
   * see if status is Enabled
   * @return if enabled
   */
  public boolean isEnabled() {
    return GrouperClientUtils.equals("Enabled", this.status);
  }
  
  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();
    
//    "People Permission Group ID": "EPG000000000101",
    //          "Permission Group": "2000000001",
    //          "Permission Group ID": 2000000001,
    //          "Person ID": "PPL000000000616",
    //          "Remedy Login ID": "benoff",
    //          "Status": "Enabled"

    if (fieldNamesToSet == null || fieldNamesToSet.contains("Status")) {      
      result.put("Status", this.status);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Permission Group")) {      
      result.put("Permission Group", this.permissionGroup);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Permission Group ID")) {      
      result.put("Permission Group ID", this.permissionGroupId);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("People Permission Group ID")) {      
      result.put("People Permission Group ID", this.peoplePermissionGroupId);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Remedy Login ID")) {      
      result.put("Remedy Login ID", this.remedyLoginId);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Person ID")) {      
      result.put("Person ID", this.personId);
    }
    
    return result;
  }

}
