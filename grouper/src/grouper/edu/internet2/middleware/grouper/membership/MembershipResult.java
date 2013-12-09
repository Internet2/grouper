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

import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
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
   * stems by id
   */
  private Map<String, Stem> stems;

  /**
   * attributeDefs by id
   */
  private Map<String, AttributeDef> attributeDefs;

  /**
   * member uuid to member
   */
  private Map<String, Member> members;

  /** field id we are looking for */
  private String fieldId;


  /**
   * 
   */
  public MembershipResult() {

  }
  
  /**
   * original output of the query
   */
  private Set<Object[]> membershipsOwnersMembers;
  
  /**
   * original output of query
   * @return result
   */
  public Set<Object[]> getMembershipsOwnersMembers() {
    return this.membershipsOwnersMembers;
  }

  /**
   * lazy load the calculation of reach subject/member and how they relate to the result set
   */
  private Set<MembershipSubjectContainer> membershipSubjectContainers;
  
  /**
   * lazy load the calculation of reach subject/member and how they relate to the result set
   * @return lazy load the calculations
   */
  public Set<MembershipSubjectContainer> getMembershipSubjectContainers() {
    if (this.membershipSubjectContainers == null) {
      
      //only do this for one owner
      if (GrouperUtil.length(this.groups) > 1 || GrouperUtil.length(this.stems) > 1) {
        throw new RuntimeException("Cant have membership subject containers for more than one owner: " 
            + GrouperUtil.length(this.groups) + ", " + GrouperUtil.length(this.stems));
      }

      this.membershipSubjectContainers = MembershipSubjectContainer.convertFromMembershipsOwnersMembers(this.membershipsOwnersMembers);
      
    }
    return this.membershipSubjectContainers;
  }

  
  
  /**
   * 
   * @param theMembershipsGroupsMembers is the list of arrays of membership, group, member
   * @param theFieldId is null for members, or specify if something else
   */
  public MembershipResult(Set<Object[]> theMembershipsGroupsMembers, String theFieldId) {
    
    this.membershipsOwnersMembers = theMembershipsGroupsMembers;
    
    this.memberships = new HashSet<Membership>();
    this.groups = new HashMap<String, Group>();
    this.stems = new HashMap<String, Stem>();
    this.members = new HashMap<String, Member>();
    this.fieldId = StringUtils.defaultString(theFieldId, defaultListFieldId());

    //separate out all the results
    for (Object[] theMembershipGroupMember : GrouperUtil.nonNull(theMembershipsGroupsMembers)) {
      this.memberships.add((Membership)theMembershipGroupMember[0]);
      if (theMembershipGroupMember[1] instanceof Group) {
        this.groups.put(((Group)theMembershipGroupMember[1]).getId(),(Group)theMembershipGroupMember[1]) ;
      } else if (theMembershipGroupMember[1] instanceof Stem) {
        this.stems.put(((Stem)theMembershipGroupMember[1]).getId(),(Stem)theMembershipGroupMember[1]) ;
      } else {
        throw new RuntimeException("Not expecting owner type: " + theMembershipGroupMember[1].getClass());
      }
      this.members.put(((Member)theMembershipGroupMember[2]).getUuid(),(Member)theMembershipGroupMember[2]) ;
    }

    
    
  }

  /**
   * 
   * @param theMembershipsStemsMembers is the list of arrays of membership, stem, member
   * @param theFieldId is null for members, or specify if something else
   */
  public void initResultStems(Set<Object[]> theMembershipsStemsMembers, String theFieldId) {
    this.memberships = new HashSet<Membership>();
    this.stems = new HashMap<String, Stem>();
    this.members = new HashMap<String, Member>();
    this.fieldId = StringUtils.defaultString(theFieldId, defaultListFieldId());

    //separate out all the results
    for (Object[] theMembershipStemMember : GrouperUtil.nonNull(theMembershipsStemsMembers)) {
      this.memberships.add((Membership)theMembershipStemMember[0]);
      this.stems.put(((Stem)theMembershipStemMember[1]).getUuid(),(Stem)theMembershipStemMember[1]) ;
      this.members.put(((Member)theMembershipStemMember[2]).getUuid(),(Member)theMembershipStemMember[2]) ;
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
   * find a stem id or null from results
   * @param stemName
   * @return the stem id
   */
  private String findStemId(String stemName) {
    //get the stem id
    for (Stem stem : this.stems.values()) {
      if (StringUtils.equals(stemName, stem.getName()) || stem.getAlternateNames().contains(stemName)) {
        return stem.getUuid();
      }
    }
    return null;
  }
  
  /**
   * find a attributeDef id or null from results
   * @param nameOfattributeDef
   * @return the attributeDef id
   */
  private String findAttributeDefId(String nameOfAttributeDef) {
    //get the attributeDef id
    for (AttributeDef attributeDef : this.attributeDefs.values()) {
      if (StringUtils.equals(nameOfAttributeDef, attributeDef.getName())) {
        return attributeDef.getUuid();
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
    return hasGroupMembership(groupName, subject, null);
  }

  /**
   * 
   * @param groupName
   * @param subject
   * @return if the memberships have this group
   */
  public boolean hasGroupMembership(String groupName, Subject subject, String fieldName) {
    String groupId = findGroupId(groupName);
    String memberId = findMemberId(subject);
    
    String fieldId = StringUtils.isBlank(fieldName) ? this.fieldId : FieldFinder.find(fieldName, true).getUuid();
    
    //if any of them arent there, we are done
    if (StringUtils.isBlank(groupId) || StringUtils.isBlank(memberId)) {
      return false;
    }
    
    //now see if that subject has a membership
    for (Membership membership : this.memberships) {
      if (StringUtils.equals(membership.getOwnerGroupId(), groupId) 
          && StringUtils.equals(membership.getMemberUuid(), memberId)
          && StringUtils.equals(fieldId, membership.getFieldId())) {
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @param stemName
   * @param subject
   * @return if the memberships have this stem
   */
  public boolean hasStemMembership(String stemName, Subject subject) {
    return hasStemMembership(stemName, subject, null);
  }

  /**
   * 
   * @param stemName
   * @param subject
   * @return if the memberships have this stem
   */
  public boolean hasStemMembership(String stemName, Subject subject, String fieldName) {
    String stemId = findStemId(stemName);
    String memberId = findMemberId(subject);
    
    String fieldId = StringUtils.isBlank(fieldName) ? this.fieldId : FieldFinder.find(fieldName, true).getUuid();
    
    //if any of them arent there, we are done
    if (StringUtils.isBlank(stemId) || StringUtils.isBlank(memberId)) {
      return false;
    }
    
    //now see if that subject has a membership
    for (Membership membership : this.memberships) {
      if (StringUtils.equals(membership.getOwnerStemId(), stemId) 
          && StringUtils.equals(membership.getMemberUuid(), memberId)
          && StringUtils.equals(fieldId, membership.getFieldId())) {
        return true;
      }
    }
    return false;
  }
  
  
  /**
   * 
   * @param attributeDefName
   * @param subject
   * @return if the memberships have this stem
   */
  public boolean hasAttributeDefMembership(String attributeDefName, Subject subject, String fieldName) {
    String attributeDefId = findAttributeDefId(attributeDefName);
    String memberId = findMemberId(subject);
    
    String fieldId = StringUtils.isBlank(fieldName) ? this.fieldId : FieldFinder.find(fieldName, true).getUuid();
    
    //if any of them arent there, we are done
    if (StringUtils.isBlank(attributeDefId) || StringUtils.isBlank(memberId)) {
      return false;
    }
    
    //now see if that subject has a membership
    for (Membership membership : this.memberships) {
      if (StringUtils.equals(membership.getOwnerAttrDefId(), attributeDefId) 
          && StringUtils.equals(membership.getMemberUuid(), memberId)
          && StringUtils.equals(fieldId, membership.getFieldId())) {
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
   * get the members from the result
   * @return the members
   */
  public Set<Member> members() {
    return new HashSet(this.members.values());
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
