package edu.internet2.middleware.grouperRemedy;

import java.util.Map;

import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * grouper box user
 * @author mchyzer
 *
 */
public class GrouperRemedyMembership {

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
   * groupId, netId
   * cache connections
   */
  private static ExpirableCache<Boolean, Map<MultiKey, GrouperRemedyMembership>> retrieveMembershipsCache = new ExpirableCache<Boolean, Map<MultiKey, GrouperRemedyMembership>>(5);

  /**
   * 
   * @return box api connection never null
   */
  public synchronized static Map<MultiKey, GrouperRemedyMembership> retrieveMemberships() {
    
    Map<MultiKey, GrouperRemedyMembership> membershipsMap = retrieveMembershipsCache.get(Boolean.TRUE);
    
    if (membershipsMap == null) {
      
      membershipsMap = GrouperRemedyCommands.retrieveRemedyMemberships();
      
      retrieveMembershipsCache.put(Boolean.TRUE, membershipsMap);
    }
    
    return membershipsMap;
  }

  //          "": 2000000001,
  //          "Permission Group2": "Sample Entitlement Group 1"

  /**
   * Permission Group ID
   */
  private Long permissionGroupId;
  
  
  /**
   * Permission Group ID
   * @return the permissionGroupId
   */
  public Long getPermissionGroupId() {
    return this.permissionGroupId;
  }

  
  /**
   * Permission Group ID
   * @param permissionGroupId1 the permissionGroupId to set
   */
  public void setPermissionGroupId(Long permissionGroupId1) {
    this.permissionGroupId = permissionGroupId1;
  }

  /**
   * permission group
   */
  private String permissionGroup;
  
  /**
   * permission group
   * @return the permissionGroup
   */
  public String getPermissionGroup() {
    return this.permissionGroup;
  }
  
  /**
   * permission group
   * @param permissionGroup1 the permissionGroup to set
   */
  public void setPermissionGroup(String permissionGroup1) {
    this.permissionGroup = permissionGroup1;
  }

  /**
   * People Permission Group ID
   */
  private String peoplePermissionGroupId;
  
  /**
   * People Permission Group ID
   * @return the peoplePermissionGroupId
   */
  public String getPeoplePermissionGroupId() {
    return this.peoplePermissionGroupId;
  }
  
  /**
   * People Permission Group ID
   * @param peoplePermissionGroupId1 the peoplePermissionGroupId to set
   */
  public void setPeoplePermissionGroupId(String peoplePermissionGroupId1) {
    this.peoplePermissionGroupId = peoplePermissionGroupId1;
  }

  /**
   * remedy id for a person
   */
  private String personId;

  /**
   * netId of user
   */
  private String remedyLoginId;

  
  /**
   * remedy id for a person
   * @return the personId
   */
  public String getPersonId() {
    return this.personId;
  }

  
  /**
   * remedy id for a person
   * @param personId1 the personId to set
   */
  public void setPersonId(String personId1) {
    this.personId = personId1;
  }

  
  /**
   * netId of user
   * @return the remedyLoginId
   */
  public String getRemedyLoginId() {
    return this.remedyLoginId;
  }

  
  /**
   * netId of user
   * @param remedyLoginId1 the remedyLoginId to set
   */
  public void setRemedyLoginId(String remedyLoginId1) {
    this.remedyLoginId = remedyLoginId1;
  }
  
  
}
