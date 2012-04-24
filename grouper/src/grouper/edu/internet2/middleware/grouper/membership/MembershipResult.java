/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.membership;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * wrapper around membership result so that it can easily be processed
 * @author mchyzer
 *
 */
public class MembershipResult {

  /**
   * membership
   */
  private Set<Membership> memberships;
  
  /**
   * groups by id
   */
  private Map<String, Group> groups;

  /**
   * member
   */
  private Map<String, Member> members;

  /** field id we are looking for */
  private String fieldId;

  /**
   * 
   * @param theMembershipsGroupsMembers is the list of arrays of membership, group, member
   * @param theFieldId is null for members, or specify if something else
   */
  public MembershipResult(Set<Object[]> theMembershipsGroupsMembers, String theFieldId) {
    this.memberships = new HashSet<Membership>();
    this.groups = new HashMap<String, Group>();
    this.members = new HashMap<String, Member>();
    this.fieldId = StringUtils.defaultString(theFieldId, defaultListFieldId());

    //separate out all the results
    for (Object[] theMembershipGroupMember : GrouperUtil.nonNull(theMembershipsGroupsMembers)) {
      this.memberships.add((Membership)theMembershipGroupMember[0]);
      this.groups.put(((Group)theMembershipGroupMember[1]).getId(),(Group)theMembershipGroupMember[1]) ;
      this.members.put(((Member)theMembershipGroupMember[2]).getUuid(),(Member)theMembershipGroupMember[2]) ;
    }
    
  }

  /**
   * find a group id or null from results
   * @param groupName
   * @return the group id
   */
  private String findGroupId(String groupName) {
    //get the group id
    for (Group group : this.groups.values()) {
      if (StringUtils.equals(groupName, group.getName()) || group.getAlternateNames().contains(groupName)) {
        return group.getId();
      }
    }
    return null;
  }
  
  /**
   * find a member id or null from results
   * @param subject to look for
   * @return the member id
   */
  private String findMemberId(Subject subject) {
    //get the member id
    for (Member member : this.members.values()) {
      if (StringUtils.equals(subject.getId(), member.getSubjectId()) && StringUtils.equals(subject.getSourceId(), member.getSubjectSourceId())) {
        return member.getUuid();
      }
    }
    return null;
  }
  
  /**
   * 
   * @param groupName
   * @param subject
   * @return if the memberships have this group
   */
  public boolean hasGroupMembership(String groupName, Subject subject) {
    String groupId = findGroupId(groupName);
    String memberId = findMemberId(subject);
    
    //if any of them arent there, we are done
    if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId)) {
      return false;
    }
    
    //now see if that subject has a membership
    for (Membership membership : this.memberships) {
      if (StringUtils.equals(membership.getOwnerGroupId(), groupId) 
          && StringUtils.equals(membership.getMemberUuid(), memberId)
          && StringUtils.equals(this.fieldId, membership.getFieldId())) {
        return true;
      }
    }
    return false;
  }
  
  
  /** cache this so it is fast */
  private static String defaultListFieldId = null;
  
  /**
   * use this to lazy load the default list field id
   * @return
   */
  private static String defaultListFieldId() {
    
    if (StringUtils.isBlank(defaultListFieldId)) {
      defaultListFieldId = Group.getDefaultList().getUuid();
    }
    
    return defaultListFieldId;
    
  }
  
  /**
   * find the group names for the membership result in a certain stem
   * @param subject
   * @param stemName
   * @return the group names
   */
  public Set<String> groupNamesInStem(Subject subject, String stemName) {
    
    //get the memberId
    String memberId = findMemberId(subject);
    
    //result
    Set<String> groupNames = new TreeSet<String>();
    
    //add colon to stem name so it doesnt match stem prefixes
    stemName += ":";
    
    //loop through memberships
    for (Membership membership : this.memberships) {
      
      if (StringUtils.equals(membership.getMemberUuid(), memberId)
          && StringUtils.equals(this.fieldId, membership.getFieldId())) {
        
        String groupName = this.groups.get(membership.getOwnerGroupId()).getName();
        
        //make sure the group name starts with the stem name
        if (groupName.startsWith(stemName)) {
          groupNames.add(groupName);
        }
        
      }
      
    }
    
    return groupNames;
  }
}
