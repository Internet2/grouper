package edu.internet2.middleware.grouper.app.freshServiceAgent;

import java.sql.Types;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class FreshAgentGroup {
  
  
  public static void createTableFreshGroup(DdlVersionBean ddlVersionBean, Database database) {
    
    final String groupTableName = "mock_freshagent_group";
    
    try {
      new GcDbAccess().sql("select count(*) from " + groupTableName).select(int.class);
    } catch (Exception e) {
      
      Table groupTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, groupTableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "id", Types.BIGINT, "20", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupTableName, "mock_freshagent_group_name_idx", true, "name");
    }
    
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static FreshAgentGroup fromJson(JsonNode groupNode) {
    if (groupNode == null) {
      return null;
    }
    
    FreshAgentGroup grouperAgentGroup = new FreshAgentGroup();
    grouperAgentGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "name");
    grouperAgentGroup.description = GrouperUtil.jsonJacksonGetString(groupNode, "description");
    
    grouperAgentGroup.id = GrouperUtil.jsonJacksonGetLong(groupNode, "id");
    
    return grouperAgentGroup;
  }
  
  /**
   * convert to jackson json
   * @param fieldNamesToSet the field names we'll be setting
   * @return a jackson ObjectNode representing the GrouperAgentGroup object
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {
      result.put("name", this.name);
    }
//    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {
//      result.put("id", this.id);
//    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {
      result.put("description", this.description);
    }
    return result;
  }
  
  
  /**
   * Convert to a provisioning group
   * @return a Provisioning Group object
   */
  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup();
    if (this.id != null) {
      targetGroup.setId(Long.toString(this.id));
    }
    targetGroup.assignAttributeValue("description", this.description);
    targetGroup.assignAttributeValue("name", this.name);
    // a Grouper group can provision to an agent group or a role (FreshAgentRole), this is an agent group
    targetGroup.assignAttributeValue("groupType", FreshAgentRole.GROUP_TYPE_GROUP);
    return targetGroup;
  }
  
  /**
   * Convert a provisioning group to a AgentGroup
   * @param targetGroup the provisioning group
   * @param fieldNamesToSet the field names in AgentGroup to set
   * @return the converted AgentGroup
   */
  public static FreshAgentGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    FreshAgentGroup grouperAgentGroup = new FreshAgentGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {
      if (targetGroup.getId() == null) {
        grouperAgentGroup.setId(null);
      } else {
        grouperAgentGroup.setId(Long.parseLong(targetGroup.getId()));
      }
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperAgentGroup.setName(targetGroup.retrieveAttributeValueString("name"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {      
      grouperAgentGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }
    
    return grouperAgentGroup;
  }
  
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  private Long id;
  private String name;
  private String description;
  
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String agentGroupName) {
    this.name = agentGroupName;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }

}