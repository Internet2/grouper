package edu.internet2.middleware.grouperBox;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.box.sdk.BoxGroup;
import com.box.sdk.BoxGroupMembership;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * grouper box group
 * @author mchyzer
 *
 */
public class GrouperBoxGroup {

  /**
   * memberships
   */
  private Collection<BoxGroupMembership.Info> memberships;
  
  /**
   * 
   * @param boxGroup1
   */
  public GrouperBoxGroup(BoxGroup boxGroup1) {
    this.boxGroup = boxGroup1;
  }
  
  /**
   * 
   * @param boxGroup1
   * @param boxGroupInfo1
   */
  public GrouperBoxGroup(BoxGroup boxGroup1, BoxGroup.Info boxGroupInfo1) {
    this.boxGroup = boxGroup1;
    this.boxGroupInfo = boxGroupInfo1;
  }
  
  /**
   * box user
   */
  private BoxGroup boxGroup;
  
  /**
   * box group info
   */
  private BoxGroup.Info boxGroupInfo;

  /**
   * 
   * @return box group
   */
  public BoxGroup getBoxGroup() {
    return this.boxGroup;
  }

  /**
   * lazy load this
   * @return box group info
   */
  public BoxGroup.Info getBoxGroupInfo() {
    if (this.boxGroupInfo == null) {
      this.boxGroupInfo = GrouperBoxCommands.retrieveBoxGroupInfo(this.boxGroup);
    }
    return this.boxGroupInfo;
  }

  /**
   * lazy load memberships
   * @return memberships
   */
  public Collection<BoxGroupMembership.Info> getMemberships() {
    if (this.memberships == null) {
      this.memberships = GrouperClientUtils.nonNull(GrouperBoxCommands.retrieveMembershipsForGroup(this));
    }
    return this.memberships;
  }
  
  /**
   * 
   * @return membership
   */
  public BoxGroupMembership.Info assignUserToGroup(GrouperBoxUser grouperBoxUser, boolean isIncremental) {
    this.memberships = null;
    return GrouperBoxCommands.assignUserToGroup(grouperBoxUser, this, isIncremental);
  }

  /**
   * 
   * @return membership
   */
  public BoxGroupMembership.Info removeUserFromGroup(GrouperBoxUser grouperBoxUser, boolean isIncremental) {
    this.memberships = null;
    return GrouperBoxCommands.removeUserFromGroup(grouperBoxUser, this, isIncremental);
  }

  /**
   * 
   * @return the map of loginids to user objects
   */
  public Map<String, GrouperBoxUser> getMemberUsers() {
    
    Map<String, GrouperBoxUser> results = new HashMap<String, GrouperBoxUser>();
    
    for (BoxGroupMembership.Info boxGroupMembershipInfo : this.getMemberships()) {
      
      GrouperBoxUser grouperBoxUser = new GrouperBoxUser(boxGroupMembershipInfo.getUser().getResource(), 
          boxGroupMembershipInfo.getUser());
      
      results.put(grouperBoxUser.getBoxUserInfo().getLogin(), grouperBoxUser);
      
    }
    
    return results;
  }
  
}
