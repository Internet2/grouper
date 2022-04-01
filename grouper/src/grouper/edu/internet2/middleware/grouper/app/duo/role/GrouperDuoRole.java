package edu.internet2.middleware.grouper.app.duo.role;

import java.sql.Types;
import java.util.Set;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperDuoRole {

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
  public static GrouperDuoRole fromJson(JsonNode groupNode) {
    GrouperDuoRole grouperDuoGroup = new GrouperDuoRole();
    grouperDuoGroup.desc = GrouperUtil.jsonJacksonGetString(groupNode, "desc");
    grouperDuoGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "name");
    
    grouperDuoGroup.group_id = GrouperUtil.jsonJacksonGetString(groupNode, "group_id");
    
    return grouperDuoGroup;
  }

}
