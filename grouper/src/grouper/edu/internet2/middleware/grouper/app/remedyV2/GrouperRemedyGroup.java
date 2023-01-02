package edu.internet2.middleware.grouper.app.remedyV2;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperRemedyGroup {
  
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
   * "Permission Group": "2000000001",
   */
  private String permissionGroup;

  /**
   * "Permission Group ID": 2000000001
   */
  private Long permissionGroupId;
  
  /**
   * "Permission Group": "2000000001",
   * @return the permissionGroup
   */
  public String getPermissionGroup() {
    return this.permissionGroup;
  }

  
  /**
   * "Permission Group": "2000000001",
   * @param permissionGroup1 the permissionGroup to set
   */
  public void setPermissionGroup(String permissionGroup1) {
    this.permissionGroup = permissionGroup1;
  }

  
  /**
   * "Permission Group ID": 2000000001
   * @return the permissionGroupId
   */
  public Long getPermissionGroupId() {
    return this.permissionGroupId;
  }

  
  /**
   * "Permission Group ID": 2000000001
   * @param permissionGroupId1 the permissionGroupId to set
   */
  public void setPermissionGroupId(Long permissionGroupId1) {
    this.permissionGroupId = permissionGroupId1;
  }
  
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableRemedyGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_remedy_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
    
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "status", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "permission_group", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "permission_group_id", Types.BIGINT, "40", true, true);

    }
            
  }

  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    
    targetGroup.assignAttributeValue("permissionGroup", this.permissionGroup);
    targetGroup.assignAttributeValue("status", this.status);
    targetGroup.assignAttributeValue("permissionGroupId", this.permissionGroupId);
    
    //TODO do we need this?
    //targetGroup.setId(this.permissionGroupId.toString());
    
    return targetGroup;
  }
  
  /**
   * 
   * @param targetGroup
   * @param fieldNamesToSet
   * @return
   */
  public static GrouperRemedyGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperRemedyGroup grouperRemedyGroup = new GrouperRemedyGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("status")) {      
      grouperRemedyGroup.setStatus(targetGroup.retrieveAttributeValueString("status"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("permissionGroup")) {      
      grouperRemedyGroup.setPermissionGroup(targetGroup.retrieveAttributeValueString("permissionGroup"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("permissionGroupId")) {      
      grouperRemedyGroup.setPermissionGroupId(Long.valueOf(targetGroup.retrieveAttributeValueString("permissionGroupId")));
    }
    
    return grouperRemedyGroup;

  }
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  public static final String fieldsToSelect="Status,Permission Group,Permission Group ID";
  
  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static GrouperRemedyGroup fromJson(JsonNode groupNode) {
    
    if (groupNode == null || !groupNode.has("Permission Group ID")) {
      return null;
    }
    
    GrouperRemedyGroup grouperRemedyGroup = new GrouperRemedyGroup();
    
    grouperRemedyGroup.permissionGroupId = GrouperUtil.jsonJacksonGetLong(groupNode, "Permission Group ID");
    grouperRemedyGroup.permissionGroup = GrouperUtil.jsonJacksonGetString(groupNode, "Permission Group");
    grouperRemedyGroup.status = GrouperUtil.jsonJacksonGetString(groupNode, "Status");
    
    return grouperRemedyGroup;
  }

  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("Status")) {      
      result.put("Status", this.status);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Permission Group")) {      
      result.put("Permission Group", this.permissionGroup);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Permission Group ID")) {      
      result.put("Permission Group ID", this.permissionGroupId);
    }
    
    return result;
  }
  
}
