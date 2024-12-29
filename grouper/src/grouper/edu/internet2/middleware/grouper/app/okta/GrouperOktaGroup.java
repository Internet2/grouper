package edu.internet2.middleware.grouper.app.okta;

import java.sql.Types;
import java.util.Set;

import org.jsoup.internal.StringUtil;

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

public class GrouperOktaGroup {
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableOktaGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String groupTableName = "mock_okta_group";

    try {
      new GcDbAccess().sql("select count(*) from " + groupTableName).select(int.class);
    } catch (Exception e) {
    
      
      Table groupTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, groupTableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "id", Types.VARCHAR, "40", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupTableName, "mock_okta_group_name_idx", true, "name");
    }
            
  }

  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);
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
  public static GrouperOktaGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperOktaGroup grouperOktaGroup = new GrouperOktaGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) { 
      grouperOktaGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperOktaGroup.setName(targetGroup.getName());
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      grouperOktaGroup.setId(targetGroup.getId());
    }
    
    return grouperOktaGroup;

  }
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  private String id;
  private String name;
  private String description;
  
  public String getDescription() {
    return description;
  }

  
  public void setDescription(String description) {
    this.description = description;
  }

  
  public String getId() {
    return id;
  }

  
  public void setId(String groupId) {
    this.id = groupId;
  }

  public String getName() {
    return name;
  }

  
  public void setName(String name) {
    this.name = name;
  }
  

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static GrouperOktaGroup fromJson(JsonNode groupNode) {
    
    GrouperOktaGroup grouperOktaGroup = new GrouperOktaGroup();
    grouperOktaGroup.id = GrouperUtil.jsonJacksonGetString(groupNode, "id");
    
    JsonNode profileNode = GrouperUtil.jsonJacksonGetNode(groupNode, "profile");
    
    grouperOktaGroup.description = GrouperUtil.jsonJacksonGetString(profileNode, "description");
    if (StringUtil.isBlank(grouperOktaGroup.description)) {
      grouperOktaGroup.description = null;
    }
    
    grouperOktaGroup.name = GrouperUtil.jsonJacksonGetString(profileNode, "name");
    
    return grouperOktaGroup;
  }
  
  
  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJsonGroupOnly(Set<String> fieldNamesToSet) {
    
    ObjectNode result = GrouperUtil.jsonJacksonNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {      
      result.put("id", this.id);
    }
    
    ObjectNode profileNode = GrouperUtil.jsonJacksonNode();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      profileNode.put("name", this.name);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {      
      profileNode.put("description", this.description);
    }
    
    result.set("profile", profileNode);
    
    return result;
  }

}
