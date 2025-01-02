package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;

public class GrouperScim2MembershipCache {

  public GrouperScim2MembershipCache() {
  }

  private Set<String> userIdsRetrievedMemberships = Collections.synchronizedSet(new HashSet<>());
  
  private Set<String> groupIdsRetrievedMemberships = Collections.synchronizedSet(new HashSet<>());
  
  private Map<String, Set<String>> groupIdToMembershipUserIds = new ConcurrentHashMap<>();
  
  private Map<String, Set<String>> userIdToMembershipGroupIds = new ConcurrentHashMap<>();
  
  
  
  
  public Set<String> getUserIdsRetrievedMemberships() {
    return userIdsRetrievedMemberships;
  }

  
  public Set<String> getGroupIdsRetrievedMemberships() {
    return groupIdsRetrievedMemberships;
  }

  
  public Map<String, Set<String>> getGroupIdToMembershipUserIds() {
    return groupIdToMembershipUserIds;
  }

  
  public Map<String, Set<String>> getUserIdToMembershipGroupIds() {
    return userIdToMembershipGroupIds;
  }

  public void addMembershipsForGroup(String groupId) {
    if (StringUtils.isBlank(groupId)) {
      return;
    }
    groupIdsRetrievedMemberships.add(groupId);
  }

  public void addMembershipsForUser(String userId) {
    if (StringUtils.isBlank(userId)) {
      return;
    }
    userIdsRetrievedMemberships.add(userId);
  }

  public void addMembership(String groupId, String userId) {

    if (StringUtils.isBlank(groupId) || StringUtils.isBlank(userId)) {
      return;
    }
    
    {
      // set the group whether it has memberships or not
      Set<String> userIds = groupIdToMembershipUserIds.get(groupId);
      if (userIds == null) {
        userIds = Collections.synchronizedSet(new HashSet<>());
        Set<String> oldUserIds = groupIdToMembershipUserIds.put(groupId, userIds);
        if (oldUserIds != null) {
          userIds.addAll(oldUserIds);
        }
      }
      userIds.add(userId);
    }
    
    {
      // set the group whether it has memberships or not
      Set<String> groupIds = userIdToMembershipGroupIds.get(userId);
      if (groupIds == null) {
        groupIds = Collections.synchronizedSet(new HashSet<>());
        Set<String> oldGroupIds = userIdToMembershipGroupIds.put(userId, groupIds);
        if (oldGroupIds != null) {
          groupIds.addAll(oldGroupIds);
        }
      }
      groupIds.add(userId);
    }
  
  }
  
}
