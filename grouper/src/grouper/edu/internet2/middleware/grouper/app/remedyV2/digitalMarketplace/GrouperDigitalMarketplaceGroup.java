package edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace;

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

public class GrouperDigitalMarketplaceGroup {
  
  /**
   * extension
   */
  private String groupName;

  /**
   * display extension
   */
  private String longGroupName;
  
  /**
   * group description
   */
  private String comments;
  
  /**
   * group description
   * @return the comments
   */
  public String getComments() {
    return this.comments;
  }
  
  /**
   * group description
   * @param comments1 the comments to set
   */
  public void setComments(String comments1) {
    this.comments = comments1;
  }

  /**
   * extension
   * @return the permissionGroup
   */
  public String getGroupName() {
    return this.groupName;
  }

  
  /**
   * extension
   * @param groupName1 the permissionGroup to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  
  /**
   * display extension
   * @return the permissionGroupId
   */
  public String getLongGroupName() {
    return this.longGroupName;
  }

  
  /**
   * display extension
   * @param longGroupName1 the permissionGroupId to set
   */
  public void setLongGroupName(String longGroupName1) {
    this.longGroupName = longGroupName1;
  }
  
  
  /**
   * e.g. com.bmc.arsys.rx.services.group.domain.RegularGroup
   */
  private String resourceType;
  
  /**
   * e.g. com.bmc.arsys.rx.services.group.domain.RegularGroup
   * @return the resourceType
   */
  public String getResourceType() {
    return this.resourceType;
  }
  
  /**
   * e.g. com.bmc.arsys.rx.services.group.domain.RegularGroup
   * @param resourceType1 the resourceType to set
   */
  public void setResourceType(String resourceType1) {
    this.resourceType = resourceType1;
  }

  /**
   * e.g. Change
   */
  private String groupType;

  /**
   * e.g. Change
   * @return the groupType
   */
  public String getGroupType() {
    return this.groupType;
  }
  
  /**
   * e.g. Change
   * @param groupType1 the groupType to set
   */
  public void setGroupType(String groupType1) {
    this.groupType = groupType1;
  }
  
  /**
   * @param ddlVersionBean
   * @param database
   */
  public static void createTableDigitalMarketplaceGroup(DdlVersionBean ddlVersionBean, Database database) {

    final String tableName = "mock_digital_marketplace_group";

    try {
      new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
    } catch (Exception e) {
    
      
      Table loaderTable = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, tableName);
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "comments", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_name", Types.VARCHAR, "40", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_type", Types.VARCHAR, "40", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "long_group_name", Types.VARCHAR, "255", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "resource_type", Types.VARCHAR, "255", false, true);

    }
            
  }

  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup(false);
    
    targetGroup.assignAttributeValue("comments", this.comments);
    targetGroup.assignAttributeValue("groupName", this.groupName);
    targetGroup.assignAttributeValue("groupType", this.groupType);
    targetGroup.assignAttributeValue("longGroupName", this.longGroupName);
    targetGroup.assignAttributeValue("resourceType", this.resourceType);
    
    return targetGroup;
  }
  
  /**
   * 
   * @param targetGroup
   * @param fieldNamesToSet
   * @return
   */
  public static GrouperDigitalMarketplaceGroup fromProvisioningGroup(ProvisioningGroup targetGroup, Set<String> fieldNamesToSet) {
    
    GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup = new GrouperDigitalMarketplaceGroup();
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("comments")) {      
      grouperDigitalMarketplaceGroup.setComments(targetGroup.retrieveAttributeValueString("comments"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupName")) {      
      grouperDigitalMarketplaceGroup.setGroupName(targetGroup.retrieveAttributeValueString("groupName"));
    }

    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupType")) {      
      grouperDigitalMarketplaceGroup.setGroupType(targetGroup.retrieveAttributeValueString("groupType"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("longGroupName")) {      
      grouperDigitalMarketplaceGroup.setLongGroupName(targetGroup.retrieveAttributeValueString("longGroupName"));
    }
    
    if (fieldNamesToSet == null || fieldNamesToSet.contains("resourceType")) {      
      grouperDigitalMarketplaceGroup.setResourceType(targetGroup.retrieveAttributeValueString("resourceType"));
    }
    
    return grouperDigitalMarketplaceGroup;

  }
  
  @Override
  public String toString() {
    return GrouperClientUtils.toStringReflection(this);
  }

  /**
   * convert from jackson json
   * @param groupNode
   * @return the group
   */
  public static GrouperDigitalMarketplaceGroup fromJson(JsonNode groupNode) {
    
    if (groupNode == null || !groupNode.has("groupName")) {
      return null;
    }
    
    GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup = new GrouperDigitalMarketplaceGroup();
    
    grouperDigitalMarketplaceGroup.groupName = GrouperUtil.jsonJacksonGetString(groupNode, "groupName");
    grouperDigitalMarketplaceGroup.longGroupName = GrouperUtil.jsonJacksonGetString(groupNode, "longGroupName");
    grouperDigitalMarketplaceGroup.groupType = GrouperUtil.jsonJacksonGetString(groupNode, "groupType");
    grouperDigitalMarketplaceGroup.comments = GrouperUtil.jsonJacksonGetString(groupNode, "comments");
    grouperDigitalMarketplaceGroup.resourceType = GrouperUtil.jsonJacksonGetString(groupNode, "resourceType");
    
    return grouperDigitalMarketplaceGroup;
  }

  /**
   * convert from jackson json
   * @param fieldNamesToSet
   * @return the group
   */
  public ObjectNode toJson(Set<String> fieldNamesToSet) {
    
    ObjectNode result = GrouperUtil.jsonJacksonNode();

    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupName")) {      
      result.put("groupName", this.groupName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("groupType")) {      
      result.put("groupType", this.groupType);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("comments")) {      
      result.put("comments", this.comments);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("longGroupName")) {      
      result.put("longGroupName", this.longGroupName);
    }
    if (fieldNamesToSet == null || fieldNamesToSet.contains("resourceType")) {      
      result.put("resourceType", this.resourceType);
    }
    
    return result;
  }

  

}
