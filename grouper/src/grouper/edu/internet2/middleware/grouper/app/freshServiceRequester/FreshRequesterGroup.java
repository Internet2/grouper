package edu.internet2.middleware.grouper.app.freshServiceRequester;

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

public class FreshRequesterGroup {
  
  
  public static void createTableFreshGroup(DdlVersionBean ddlVersionBean, Database database) {
    
    final String groupTableName = "mock_freshreq_group";
    
    try {
      new GcDbAccess().sql("select count(*) from " + groupTableName).select(int.class);
    } catch (Exception e) {
      
      Table groupTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, groupTableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(groupTable, "id", Types.BIGINT, "20", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, groupTableName, "mock_freshreq_group_name_idx", true, "name");
    }
    
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static FreshRequesterGroup fromJson(JsonNode groupNode) {
    if (groupNode == null) {
      return null;
    }
    
    FreshRequesterGroup grouperRequesterGroup = new FreshRequesterGroup();
    grouperRequesterGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "name");
    grouperRequesterGroup.description = GrouperUtil.jsonJacksonGetString(groupNode, "description");
    
    grouperRequesterGroup.id = GrouperUtil.jsonJacksonGetLong(groupNode, "id");
    
    return grouperRequesterGroup;
  }
  
  /**
   * convert to jackson json
   * @param fieldNamesToSet the field names we'll be setting
   * @return a jackson ObjectNode representing the GrouperRequesterGroup object
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
    return targetGroup;
  }
  
  /**
   * Convert a provisioning group to a RequesterGroup
   * @param targetGroup the provisioning group
   * @param fieldNamesToSet the field names in RequesterGroup to set
   * @return the converted RequesterGroup
   */
  public static FreshRequesterGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    FreshRequesterGroup grouperRequesterGroup = new FreshRequesterGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("id")) {
      if (targetGroup.getId() == null) {
        grouperRequesterGroup.setId(null);
      } else {
        grouperRequesterGroup.setId(Long.parseLong(targetGroup.getId()));
      }
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("name")) {      
      grouperRequesterGroup.setName(targetGroup.retrieveAttributeValueString("name"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("description")) {      
      grouperRequesterGroup.setDescription(targetGroup.retrieveAttributeValueString("description"));
    }
    
    return grouperRequesterGroup;
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
  
  public void setName(String requesterGroupName) {
    this.name = requesterGroupName;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }

}