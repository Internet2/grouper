package edu.internet2.middleware.grouper.app.remedy;

import java.sql.Types;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ddlutils.model.Database;
import org.apache.ddlutils.model.Table;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.ProvisioningGroup;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * grouper box group
 * @author mchyzer
 *
 */
public class GrouperRemedyGroup {

  /**
   * memberships
   */
  private List<GrouperRemedyMembership> memberships;
  
  /**
   */
  public GrouperRemedyGroup() {
    
  }
  
  /**
   * status of group, Enabled or Delete
   */
  private String statusString;
  
  /**
   * status of group, Enabled or Delete
   * @return the statusString
   */
  public String getStatusString() {
    return this.statusString;
  }
  
  /**
   * status of group, Enabled or Delete
   * @param statusString1 the statusString to set
   */
  public void setStatusString(String statusString1) {
    this.statusString = statusString1;
  }

  /**
   * see if status is Enabled
   * @return if enabled
   */
  public boolean isEnabled() {
    return GrouperClientUtils.equals("Enabled", this.statusString);
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
   * lazy load memberships
   * @return memberships
   */
  public Collection<GrouperRemedyMembership> getMemberships() {
    if (this.memberships == null) {
      this.memberships = GrouperClientUtils.nonNull(GrouperRemedyCommands.retrieveRemedyMembershipsForGroup(this));
    }
    return this.memberships;
  }
  
  /**
   * 
   * @param grouperRemedyUser 
   * @param isIncremental 
   * @return true if added, false if already exists, null if enabled a past disabled memberships
   */
  public Boolean assignUserToGroup(GrouperRemedyUser grouperRemedyUser, boolean isIncremental) {
    this.memberships = null;
    return GrouperRemedyCommands.assignUserToRemedyGroup(grouperRemedyUser, this, isIncremental);
  }

  /**
   * get a membership from a group
   * @param grouperRemedyUser
   * @return the membership
   */
  public GrouperRemedyMembership retrieveGrouperRemedyMemership(GrouperRemedyUser grouperRemedyUser) {
    for (GrouperRemedyMembership grouperRemedyMembership : this.getMemberships()) {
      if (GrouperClientUtils.equals(grouperRemedyMembership.getPersonId(), grouperRemedyUser.getPersonId())) {
        return grouperRemedyMembership;
      }
    }
    return null;
  }
  
  /**
   * 
   * @param grouperRemedyUser 
   * @param isIncremental 
   * @return true if disabled, false if didnt exist, null if disabled a past enabled membership
   */
  public Boolean removeUserFromGroup(GrouperRemedyUser grouperRemedyUser, boolean isIncremental) {
    this.memberships = null;
    return GrouperRemedyCommands.removeUserFromRemedyGroup(grouperRemedyUser, this, isIncremental);
  }

  /**
   * 
   * @return the map of loginids to user objects never return null.  do not include disabled memberships
   */
  public Map<String, GrouperRemedyUser> getMemberUsers() {
    
    Map<String, GrouperRemedyUser> results = new HashMap<String, GrouperRemedyUser>();
    
    for (GrouperRemedyMembership grouperRemedyMembership : this.getMemberships()) {
      
      if (grouperRemedyMembership.isEnabled()) {
        GrouperRemedyUser grouperRemedyUser = GrouperRemedyUser.retrieveUsers().get(grouperRemedyMembership.getRemedyLoginId());
        
        results.put(grouperRemedyMembership.getRemedyLoginId(), grouperRemedyUser);
      }      
    }
    
    return results;
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
      
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "description", Types.VARCHAR, "1024", false, false);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "name", Types.VARCHAR, "256", false, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(loaderTable, "group_id", Types.VARCHAR, "40", true, true);
      
      GrouperDdlUtils.ddlutilsFindOrCreateIndex(database, tableName, "mock_remedy_group_name_idx", true, "name");
    }
            
  }
  
  public static GrouperRemedyGroup fromJson(JsonNode groupNode) {
    GrouperRemedyGroup grouperRemedyGroup = new GrouperRemedyGroup();
//    grouperDuoGroup.desc = GrouperUtil.jsonJacksonGetString(groupNode, "desc");
//    grouperDuoGroup.name = GrouperUtil.jsonJacksonGetString(groupNode, "name");
//    
//    grouperDuoGroup.group_id = GrouperUtil.jsonJacksonGetString(groupNode, "group_id");
    
    return grouperRemedyGroup;
  }
  
  public ProvisioningGroup toProvisioningGroup() {
    ProvisioningGroup targetGroup = new ProvisioningGroup();
//    targetGroup.assignAttributeValue("description", this.desc);
    targetGroup.setName(this.permissionGroup);
    targetGroup.setId(String.valueOf(this.permissionGroupId));
    return targetGroup;
  }
  
}
