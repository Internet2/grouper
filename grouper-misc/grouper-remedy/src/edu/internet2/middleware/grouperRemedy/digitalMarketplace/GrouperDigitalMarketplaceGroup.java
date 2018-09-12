package edu.internet2.middleware.grouperRemedy.digitalMarketplace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * grouper box group
 * @author mchyzer
 *
 */
public class GrouperDigitalMarketplaceGroup {

  /**
   */
  public GrouperDigitalMarketplaceGroup() {
    
  }
  
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
   * memberships
   */
  private List<GrouperDigitalMarketplaceMembership> memberships;
  
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
   * lazy load memberships
   * @return memberships 
   */
  public Collection<GrouperDigitalMarketplaceMembership> getMemberships() {
    if (this.memberships == null) {
      
      List<GrouperDigitalMarketplaceMembership> theMemberships = new ArrayList<GrouperDigitalMarketplaceMembership>();
      
      for (GrouperDigitalMarketplaceMembership grouperDigitalMarketplaceMembership : GrouperClientUtils.nonNull(
          GrouperDigitalMarketplaceUser.retrieveDigitalMarketplaceMemberships()).values()) {
        if (GrouperClientUtils.equals(this.groupName, grouperDigitalMarketplaceMembership.getGroupName())) {
          this.memberships.add(grouperDigitalMarketplaceMembership);
        }
      }
      
      this.memberships = theMemberships;
    }
    return this.memberships;
  }
  
//  /**
//   * 
//   * @param grouperRemedyUser 
//   * @param isIncremental 
//   * @return true if added, false if already exists, null if enabled a past disabled memberships
//   */
//  public Boolean assignUserToGroup(GrouperRemedyUser grouperRemedyUser, boolean isIncremental) {
//    this.memberships = null;
//    return GrouperRemedyCommands.assignUserToRemedyGroup(grouperRemedyUser, this, isIncremental);
//  }

  /**
   * get a membership from a group
   * @param grouperDigitalMarketplaceUser
   * @return the membership
   */
  public GrouperDigitalMarketplaceMembership retrieveGrouperRemedyMemership(GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser) {
    for (GrouperDigitalMarketplaceMembership grouperRemedyMembership : this.getMemberships()) {
      if (GrouperClientUtils.equals(grouperRemedyMembership.getLoginName(), grouperDigitalMarketplaceUser.getLoginName())) {
        return grouperRemedyMembership;
      }
    }
    return null;
  }
  
//  /**
//   * 
//   * @param grouperRemedyUser 
//   * @param isIncremental 
//   * @return true if disabled, false if didnt exist, null if disabled a past enabled membership
//   */
//  public Boolean removeUserFromGroup(GrouperRemedyUser grouperRemedyUser, boolean isIncremental) {
//    this.memberships = null;
//    return GrouperRemedyCommands.removeUserFromRemedyGroup(grouperRemedyUser, this, isIncremental);
//  }

  /**
   * 
   * @return the map of loginids to user objects never return null.  do not include disabled memberships
   */
  public Map<String, GrouperDigitalMarketplaceUser> getMemberUsers() {
    
    Map<String, GrouperDigitalMarketplaceUser> results = new HashMap<String, GrouperDigitalMarketplaceUser>();
    
    for (GrouperDigitalMarketplaceMembership grouperDigitalMarketplaceMembership : this.getMemberships()) {
      
      GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser = GrouperDigitalMarketplaceUser
          .retrieveUsers().get(grouperDigitalMarketplaceMembership.getLoginName());
      
      results.put(grouperDigitalMarketplaceUser.getLoginName(), grouperDigitalMarketplaceUser);
    }      
    
    return results;
  }

  /**
   * 
   * @param grouperDigitalMarketplaceUser 
   * @param isIncremental 
   * @return true if added, false if already exists, null if enabled a past disabled memberships
   */
  public Boolean assignUserToGroup(GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser, boolean isIncremental) {
    this.memberships = null;
    return GrouperDigitalMarketplaceCommands.assignUserToDigitalMarketplaceGroup(grouperDigitalMarketplaceUser, this, isIncremental);
  }

  /**
   * 
   * @param grouperDigitalMarketplaceUser 
   * @param isIncremental 
   * @return true if disabled, false if didnt exist, null if disabled a past enabled membership
   */
  public Boolean removeUserFromGroup(GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser, boolean isIncremental) {
    this.memberships = null;
    return GrouperDigitalMarketplaceCommands.removeUserFromDigitalMarketplaceGroup(grouperDigitalMarketplaceUser, this, isIncremental);
  }
  
}
