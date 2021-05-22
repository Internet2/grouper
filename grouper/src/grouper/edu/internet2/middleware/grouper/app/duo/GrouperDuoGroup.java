package edu.internet2.middleware.grouper.app.duo;

import java.sql.Types;
import java.util.Set;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperDuoGroup {

  public static void main(String[] args) {
    
    GrouperDuoGroup grouperDuoGroup = new GrouperDuoGroup();
    
    grouperDuoGroup.setDesc("desc");
    grouperDuoGroup.setName("name");
    grouperDuoGroup.setGroup_id("groupId");

    String json = GrouperUtil.jsonJacksonToString(grouperDuoGroup.toJson());
    System.out.println(json);
    
    grouperDuoGroup = GrouperDuoGroup.fromJson(GrouperUtil.jsonJacksonNode(json));
    
    System.out.println(grouperDuoGroup.toString());
    
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableDuoGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_duo_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
    
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", Types.VARCHAR, "40", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_duo_group_name_idx", false, "name");
    }
            
  }

  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    targetGroup.assignAttributeValue("description", this.desc);
    targetGroup.setName(this.name);
    targetGroup.setId(this.group_id);
    return targetGroup;
  }
  
  /**
   * 
   * @param targetGroup
   * @return
   */
  public static GrouperDuoGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperDuoGroup grouperDuoGroup = new GrouperDuoGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("desc")) {      
      grouperDuoGroup.setDesc(targetGroup.retrieveAttributeValueString("description"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperDuoGroup.setName(targetGroup.getName());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupId")) {      
      grouperDuoGroup.setGroup_id(targetGroup.getId());
    }
    
    return grouperDuoGroup;

  }
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }



  private String group_id;
  private String name;
  private String desc;
  
  //private Set<GrouperDuoUser> users;

  
  public String getGroup_id() {
    return group_id;
  }

  
  public void setGroup_id(String group_id) {
    this.group_id = group_id;
  }

  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public String getDesc() {
    return desc;
  }

  
  public void setDesc(String desc) {
    this.desc = desc;
  }
  
//  public Set<GrouperDuoUser> getUsers() {
//    return users;
//  }
//  
//  public void setUsers(Set<GrouperDuoUser> users) {
//    this.users = users;
//  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static GrouperDuoGroup fromJson(JsonNode groupNode) {
    GrouperDuoGroup grouperDuoGroup = new GrouperDuoGroup();
    grouperDuoGroup.desc = GrouperUtil.jsonJacksonGetString(groupNode, "desc");
    grouperDuoGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "name");
    
    grouperDuoGroup.group_id = GrouperUtil.jsonJacksonGetString(groupNode, "group_id");
    
    return grouperDuoGroup;
  }

  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJson() {
    ObjectMapper objectMapper = new ObjectMapper();
    ObjectNode result = objectMapper.createObjectNode();

    result.put("desc", this.desc);
    result.put("name", this.name);
    result.put("group_id", this.group_id);
    result.put("mobile_otp_enabled", false);
    result.put("push_enabled", false);
    result.put("sms_enabled", false);
    result.put("voice_enabled", false);
    result.put("status", "active");
    
    return result;
  }

}
