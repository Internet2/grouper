package edu.internet2.middleware.grouper.app.duo.role;

import java.sql.Types;
import java.util.Set;

import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningEntity;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperDuoRoleUser {

  // email, name, role
  
  private String email;
  
  private String name;
  
  private String role;
  
  private String id; // admin_id

  /**
   * @param targetEntity
   * @param fieldNamesToSet - these are the grouper names in the duo provisioner wiki. They are: id, name, email, role
   * @return
   */
  public static GrouperDuoRoleUser fromProvisioningEntity(ProvisioningEntity targetEntity, Set<String> fieldNamesToSet) {
    
    GrouperDuoRoleUser grouperDuoUser = new GrouperDuoRoleUser();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperDuoUser.setName(targetEntity.getName());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperDuoUser.setId(targetEntity.getId());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("email")) {      
      grouperDuoUser.setEmail(targetEntity.getEmail());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("role")) { 
      Set<?> roles = targetEntity.retrieveAttributeValueSet("role");
      if (GrouperUtil.length(roles) > 1) {
        throw new RuntimeException("Only one role is allowed: "+targetEntity);
      }
      grouperDuoUser.setRole(GrouperUtil.length(roles) == 1? (String)roles.iterator().next(): null);
    }
    
    return grouperDuoUser;

  }
  
  public ProvisioningEntity toProvisioningEntity() {
    ProvisioningEntity targetEntity = new ProvisioningEntity();
    
    targetEntity.addAttributeValue("role", this.role);
    targetEntity.setId(this.id);
    targetEntity.setEmail(this.email);
    targetEntity.setName(this.name);
    return targetEntity;
  }

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
  

  
  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }

  
  public String getRole() {
    return role;
  }

  
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * convert from jackson json
   * @param entityNode
   * @return the user
   */
  public static GrouperDuoRoleUser fromJson(JsonNode entityNode) {
    GrouperDuoRoleUser grouperDuoRoleUser = new GrouperDuoRoleUser();
    
    grouperDuoRoleUser.role = GrouperUtil.jsonJacksonGetString(entityNode, "role");
    grouperDuoRoleUser.name = GrouperUtil.jsonJacksonGetString(entityNode, "name");
    grouperDuoRoleUser.email = GrouperUtil.jsonJacksonGetString(entityNode, "email");
    grouperDuoRoleUser.id = GrouperUtil.jsonJacksonGetString(entityNode, "admin_id");
    
    return grouperDuoRoleUser;
  }

  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableDuoUser(DdlVersionBean ddlVersionBean, Database database) {
  
    final String tableName = "mock_duo_role_user";
  
    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
          
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "email", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "role", Types.VARCHAR, "256", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_duo_role_user_unique_email", true, "email");
      
    }
    
  }

}
