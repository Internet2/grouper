/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;

/**
 * Result of one member being retrieved from a group.
 * 
 * @author mchyzer
 */
public class WsMembership implements Comparable<WsMembership> {

  /** id of the membership */
  private String membershipId = null;

  /** immediate id of the membership */
  private String immediateMembershipId = null;
  
  /**
   * immediate id of the membership
   * @return immediate id
   */
  public String getImmediateMembershipId() {
    return this.immediateMembershipId;
  }

  /**
   * immediate id of the membership
   * @param immediateMembershipId1
   */
  public void setImmediateMembershipId(String immediateMembershipId1) {
    this.immediateMembershipId = immediateMembershipId1;
  }

  /** list name of the membership */
  private String listName = null;

  /** list type of the membership */
  private String listType = null;

  /** membership type of the membership */
  private String membershipType = null;

  /** if the membership is enabled, T or F */
  private String enabled = null;

  /** timestamp this membership is enabled: yyyy/MM/dd HH:mm:ss.SSS */
  private String enabledTime = null;

  /** timestamp this membership is disabled: yyyy/MM/dd HH:mm:ss.SSS */
  private String disabledTime = null;

  /** member id of the member */
  private String memberId = null;
  
  /** group uuid of the group */
  private String groupId = null;
  
  /** subject id of the subject involved */
  private String subjectId = null;
  
  /** sourceId of the subject involved */
  private String subjectSourceId = null;
  
  /** groupName of the group involved */
  private String groupName = null;
  
  /**
   * member id of the subject involved
   * @return member id
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * member id of the subject involved
   * @param memberId1
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }

  /**
   * group id of the group involved
   * @return the group id
   */
  public String getGroupId() {
    return this.groupId;
  }

  /**
   * group id of the group involved
   * @param groupUuid
   */
  public void setGroupId(String groupUuid) {
    this.groupId = groupUuid;
  }

  /**
   * subject id of the subject involved
   * @return the subject id
   */
  public String getSubjectId() {
    return this.subjectId;
  }

  /**
   * subject id of the subject involved
   * @param subjectId1
   */
  public void setSubjectId(String subjectId1) {
    this.subjectId = subjectId1;
  }

  /**
   * source if of the subject involved
   * @return the source id
   */
  public String getSubjectSourceId() {
    return this.subjectSourceId;
  }

  /**
   * source id of the subject involved
   * @param subjectSourceId1
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
  }

  /**
   * name of the group involved
   * @return the group name
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * name of the group involved
   * @param groupName1
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * enabled time: yyyy/MM/dd HH:mm:ss.SSS
   * @return enabled time
   */
  public String getEnabledTime() {
    return this.enabledTime;
  }

  /**
   * enabled time: yyyy/MM/dd HH:mm:ss.SSS
   * @param enabledTime1
   */
  public void setEnabledTime(String enabledTime1) {
    this.enabledTime = enabledTime1;
  }

  /**
   * disabled time: yyyy/MM/dd HH:mm:ss.SSS
   * @return disabled time
   */
  public String getDisabledTime() {
    return this.disabledTime;
  }

  /**
   * abled time: yyyy/MM/dd HH:mm:ss.SSS
   * @param disabledTime1
   */
  public void setDisabledTime(String disabledTime1) {
    this.disabledTime = disabledTime1;
  }

  /**
   * enabled
   * @return if enabled T or F
   */
  public String getEnabled() {
    return this.enabled;
  }

  /**
   * if enabled T or F
   * @param enabled1
   */
  public void setEnabled(String enabled1) {
    this.enabled = enabled1;
  }

  /**
   * id of the membership
   * 
   * @return the membershipId
   */
  public String getMembershipId() {
    return this.membershipId;
  }

  /**
   * id of the membership
   * 
   * @param membershipId1
   *            the membershipId to set
   */
  public void setMembershipId(String membershipId1) {
    this.membershipId = membershipId1;
  }

  /**
   * no-arg constructor
   */
  public WsMembership() {
    // nothing
  }

  /**
   * 
   * @param membership
   */
  public WsMembership(Membership membership) {
    this(membership, null, null);
  }
  
  /**
   * construct with membership and other objects to set internal fields
   * 
   * @param membership
   * @param group 
   * @param member 
   */
  public WsMembership(Membership membership, Group group, Member member) {
    this.setMembershipId(membership.getUuid());
    this.setMembershipType(membership.getType());
    this.setCreateTime(GrouperServiceUtils.dateToString(membership.getCreateTime()));
    Field listField = membership.getList();
    FieldType listFieldType = listField == null ? null : listField.getType();
    this.setListType(listFieldType == null ? null : listFieldType.toString());
    this.setListName(listField == null ? null : listField.getName());
    if (membership.isImmediate()) {
      this.setDisabledTime(GrouperServiceUtils.dateToString(membership.getDisabledTime()));
      this.setEnabledTime(GrouperServiceUtils.dateToString(membership.getEnabledTime()));
      this.setEnabled(membership.isEnabled() ? "T" : "F");
    } else {
      this.setEnabled("T");
    }
    this.setGroupId(membership.getOwnerGroupId());
    
    group = group == null ? membership.getGroup() : group;
    
    this.setGroupName(group.getName());
    this.setMemberId(membership.getMemberUuid());
    
    member = member == null ? membership.getMember() : member;
    
    this.setSubjectId(member.getSubjectId());
    this.setSubjectSourceId(member.getSubjectSourceId());
    
    GrouperVersion clientVersion = GrouperWsVersionUtils.retrieveCurrentClientVersion();
    if (clientVersion != null && GrouperVersion.valueOfIgnoreCase("v1_6_000").lessThanArg(clientVersion, true)) {
      this.setImmediateMembershipId(membership.getImmediateMembershipId());
    }
    
  }

  /** timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS */
  private String createTime;

  /**
   * list name of the membership
   * 
   * @return the listName
   */
  public String getListName() {
    return this.listName;
  }

  /**
   * list name of the membership
   * 
   * @param listName1
   *            the listName to set
   */
  public void setListName(String listName1) {
    this.listName = listName1;
  }

  /**
   * list type of the membership
   * 
   * @return the listType
   */
  public String getListType() {
    return this.listType;
  }

  /**
   * list type of the membership
   * 
   * @param listType1
   *            the listType to set
   */
  public void setListType(String listType1) {
    this.listType = listType1;
  }

  /**
   * @return the membershipType
   */
  public String getMembershipType() {
    return this.membershipType;
  }

  /**
   * @param membershipType1
   *            the membershipType to set
   */
  public void setMembershipType(String membershipType1) {
    this.membershipType = membershipType1;
  }

  /**
   * timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS
   * 
   * @return the createTime
   */
  public String getCreateTime() {
    return this.createTime;
  }

  /**
   * timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS
   * 
   * @param createTime1
   *            the createTime to set
   */
  public void setCreateTime(String createTime1) {
    this.createTime = createTime1;
  }

  /**
   * convert members to subject results
   * @param attributeNames to get from subjects
   * @param membershipSet should be the membership, group, and member objects in a row
   * @param returnedGroups pass in a set for groups, add any groups in there which arent
   * there already
   * @param returnedMembers psas in a set for members, add any members in there which arent
   * there already 
   * @param includeSubjectDetail 
   * @return the subject results
   */
  public static WsMembership[] convertMembers(Set<Object[]> membershipSet,
      Set<Group> returnedGroups, Set<Member> returnedMembers) {
    int memberSetLength = GrouperUtil.length(membershipSet);
    if (memberSetLength == 0) {
      return null;
    }

    WsMembership[] wsGetMembershipsResultArray = new WsMembership[memberSetLength];
    int index = 0;
    for (Object[] objects : membershipSet) {
      
      Membership membership = (Membership)objects[0];
      Group group = (Group)objects[1];
      Member member = (Member)objects[2];
      
      wsGetMembershipsResultArray[index++] = new WsMembership(membership, group, 
          member);
      
      if (!returnedGroups.contains(group)) {
        returnedGroups.add(group);
      }
      
      if (!returnedMembers.contains(member)) {
        returnedMembers.add(member);
      }
      
    }
    return wsGetMembershipsResultArray;
  }

  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(WsMembership o2) {
    if (this == o2) {
      return 0;
    }
    //lets by null safe here
    if (o2 == null) {
      return 1;
    }
    int compare = GrouperUtil.compare(this.getGroupName(), o2.getGroupName());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getSubjectSourceId(), o2.getSubjectSourceId());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getSubjectId(), o2.getSubjectId());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getListType(), o2.getListType());
    if (compare != 0) {
      return compare;
    }
    compare = GrouperUtil.compare(this.getListName(), o2.getListName());
    if (compare != 0) {
      return compare;
    }
    return GrouperUtil.compare(this.getMembershipType(), o2.getMembershipType());
  }

}
