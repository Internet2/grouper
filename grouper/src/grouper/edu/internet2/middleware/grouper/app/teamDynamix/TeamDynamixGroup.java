package edu.internet2.middleware.grouper.app.teamDynamix;

import java.sql.Types;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class TeamDynamixGroup {
  
  private String id;
  
  private String name;
  
  private String description;

  
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
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableTeamDynamixGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_teamdynamix_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
    
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "id", Types.VARCHAR, "40", true, true);

      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_teamdynamix_group_name_idx", false, "name");
    }
            
  }
  
  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    targetGroup.assignAttributeValue("description", this.description);
    targetGroup.setName(this.name);
    targetGroup.setId(this.id);
    return targetGroup;
  }
  
  /**
   * 
   * @param targetGroup
   * @return
   */
  public static TeamDynamixGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    TeamDynamixGroup teamDynamixGroup = new TeamDynamixGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Description")) {      
      teamDynamixGroup.setDescription(targetGroup.retrieveAttributeValueString("Description"));
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Name")) {      
      teamDynamixGroup.setName(targetGroup.retrieveAttributeValueString("Name"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      teamDynamixGroup.setId(targetGroup.getId());
    }
    
    return teamDynamixGroup;

  }
  
  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static TeamDynamixGroup fromJson(JsonNode groupNode) {
    if (groupNode == null || !groupNode.has("Name")) {
      return null;
    }
    TeamDynamixGroup teamDynamixGroup = new TeamDynamixGroup();
    teamDynamixGroup.description = GrouperUtil.jsonJacksonGetString(groupNode, "description");
    teamDynamixGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "Name");
    teamDynamixGroup.id = GrouperUtil.jsonJacksonGetString(groupNode, "ID");
    
    return teamDynamixGroup;
  }
  
  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("Description")) {      
      result.put("Description", this.description);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Name")) {      
      result.put("Name", this.name);
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("Id")) {
      if (!StringUtils.isBlank(this.id)) {
        result.put("ID", this.id);
      }
    }
    
    result.put("IsActive", true);
    
    return result;
  }
  
}
