package edu.internet2.middleware.grouperRemedy.digitalMarketplace;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;


/**
 * grouper box user
 * @author mchyzer
 *
 */
public class GrouperDigitalMarketplaceUser {

  /**
   * json for this user
   */
  private JSONObject jsonObject;
  
  /**
   * json for this user
   * @return the json
   */
  public JSONObject getJsonObject() {
    return this.jsonObject;
  }
  
  /**
   * json for this user
   * @param jsonObject1 the json to set
   */
  public void setJsonObject(JSONObject jsonObject1) {
    this.jsonObject = jsonObject1;
  }

  /**
   * cache group
   */
  private static ExpirableCache<Boolean, Map<String, GrouperDigitalMarketplaceUser>> retrieveUsersCache = new ExpirableCache<Boolean, Map<String, GrouperDigitalMarketplaceUser>>(60*12);
  
  /**
   * groupName, netId
   * cache memberships
   */
  private static ExpirableCache<Boolean, Map<MultiKey, GrouperDigitalMarketplaceMembership>> retrieveMembershipsCache = new ExpirableCache<Boolean, Map<MultiKey, GrouperDigitalMarketplaceMembership>>(1);
  
  /**
   * groupName, netId
   * @return memberships
   */
  public static Map<MultiKey, GrouperDigitalMarketplaceMembership> retrieveDigitalMarketplaceMemberships() {
    
    Map<MultiKey, GrouperDigitalMarketplaceMembership> grouperDigitialMarketplaceMemberships = retrieveMembershipsCache.get(Boolean.TRUE);
    
    if (grouperDigitialMarketplaceMemberships == null) {
      
      Map<String, GrouperDigitalMarketplaceUser> grouperDigitialMarketplaceUsers = retrieveUsers();
      
      // groupName, netId
      grouperDigitialMarketplaceMemberships = new LinkedHashMap<MultiKey, GrouperDigitalMarketplaceMembership>();

      // go through users
      for (GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser : GrouperClientUtils.nonNull(grouperDigitialMarketplaceUsers).values()) {
        
        String loginName = grouperDigitalMarketplaceUser.getLoginName();
        
        // go through groups in each user
        for (String groupName : GrouperClientUtils.nonNull(grouperDigitalMarketplaceUser.getGroups())) {

          MultiKey multiKey = new MultiKey(groupName, grouperDigitalMarketplaceUser.getLoginName());
          
          GrouperDigitalMarketplaceMembership grouperDigitalMarketplaceMembership = new GrouperDigitalMarketplaceMembership();
          grouperDigitalMarketplaceMembership.setGroupName(groupName);
          grouperDigitalMarketplaceMembership.setLoginName(loginName);
          
          grouperDigitialMarketplaceMemberships.put(multiKey, grouperDigitalMarketplaceMembership);
          
        }
      }
      
    }

    return grouperDigitialMarketplaceMemberships;

  }
  
  /**
   * 
   * @return box api connection never null
   */
  public synchronized static Map<String, GrouperDigitalMarketplaceUser> retrieveUsers() {
    
    Map<String, GrouperDigitalMarketplaceUser> usersMap = retrieveUsersCache.get(Boolean.TRUE);
    
    if (usersMap == null) {
      
      usersMap = GrouperDigitalMarketplaceCommands.retrieveDigitalMarketplaceUsers();
      
      retrieveUsersCache.put(Boolean.TRUE, usersMap);
    }
    
    return usersMap;
  }

  /**
   * remedy id for a person
   */
  private String userId;

  /**
   * netId of user
   */
  private String loginName;

  /**
   * extensions of groups the user is in
   */
  private Set<String> groups = new LinkedHashSet<String>();
  
  /**
   * extensions of groups the user is in
   * @return the groups
   */
  public Set<String> getGroups() {
    return this.groups;
  }

  /**
   * remedy id for a person
   * @return the personId
   */
  public String getUserId() {
    return this.userId;
  }

  
  /**
   * remedy id for a person
   * @param personId1 the personId to set
   */
  public void setUserId(String personId1) {
    this.userId = personId1;
  }

  
  /**
   * netId of user
   * @return the remedyLoginId
   */
  public String getLoginName() {
    return this.loginName;
  }

  
  /**
   * netId of user
   * @param remedyLoginId1 the remedyLoginId to set
   */
  public void setLoginName(String remedyLoginId1) {
    this.loginName = remedyLoginId1;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    result.append("loginName: ").append(this.loginName).append(", userId: ").append(this.userId).append(", groups: ");
    boolean first = true;
    for (String group : GrouperClientUtils.nonNull(this.groups)) {
      if (!first) {
        result.append(",");
      }
      result.append(group);
      first = false;
    }
    return result.toString();
  }
  
}
