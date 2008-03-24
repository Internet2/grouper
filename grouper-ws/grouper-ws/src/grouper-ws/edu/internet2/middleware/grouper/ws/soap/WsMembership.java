/**
 * 
 */
package edu.internet2.middleware.grouper.ws.soap;

import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupNotFoundException;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberNotFoundException;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipNotFoundException;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemNotFoundException;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * Result of one member being retrieved from a group.
 * 
 * @author mchyzer
 */
public class WsMembership {

  /**
   * result code of a request
   */
  public enum WsGetMembershipResultCode {

    /** found the stem (or not) */
    SUCCESS,

    /** member not found */
    MEMBER_NOT_FOUND,

    /** cant find subject */
    SUBJECT_NOT_FOUND,

    /** duplicate subject records found */
    SUBJECT_DUPLICATE,

    /** source was unavailable */
    SOURCE_UNAVAILABLE,

    /** subject is in member table, but cant be found from subject source */
    UNRESOLVABLE;

    /**
     * if this is a successful result
     * 
     * @return true if success
     */
    public boolean isSuccess() {
      return this == SUCCESS;
    }
  }

  /** subject of membership */
  private WsSubject subject;

  /** stem name this membership is realted to */
  private String stemName = null;

  /** id of the membership */
  private String membershipId = null;

  /** list name of the membership */
  private String listName = null;

  /** list type of the membership */
  private String listType = null;

  /** membership type of the membership */
  private String membershipType = null;

  /** depth of the membership */
  private int depth = -1;

  /** if there is a parent, this is the id */
  private String parentMembershipUuid;

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

  /** group name this membership is related to */
  private String groupName = null;

  /**
   * construct with member to set internal fields
   * 
   * @param membership
   * @param subjectAttributeNames are the attribute names the user is receiving (either requested or from config)
   * @param retrieveExtendedSubjectDataBoolean
   *            true to retrieve subject info (more than just the id)
   */
  public WsMembership(Membership membership, String[] subjectAttributeNames) {
    try {
      Membership parent = membership.getParentMembership();
      this.setParentMembershipUuid(parent.getUuid());
    } catch (MembershipNotFoundException mnfe) {
      //its ok, no parent
    }
    this.setMembershipId(membership.getUuid());
    this.setMembershipType(membership.getType());
    this.setCreateTime(GrouperServiceUtils.dateToString(membership.getCreateTime()));
    this.setDepth(membership.getDepth());
    Group group = null;
    try {
      group = membership.getGroup();
    } catch (GroupNotFoundException gnfe) {
      // group info is null if not there
    }
    this.setGroupName(group == null ? null : group.getName());
    Field listField = membership.getList();
    FieldType listFieldType = listField == null ? null : listField.getType();
    this.setListType(listFieldType == null ? null : listFieldType.toString());
    this.setListName(listField == null ? null : listField.getName());
    Stem stem = null;
    try {
      stem = membership.getStem();
    } catch (StemNotFoundException snfe) {
      // stem fields will be null if this happens
    }
    this.setStemName(stem == null ? null : stem.getName());
    Member member = null;
    try {
      member = membership.getMember();
    } catch (MemberNotFoundException mnfe) {
      // member fields will be null if this happens
    }
    if (member != null) {
      this.subject = new WsSubject(member, subjectAttributeNames);
      //propagate the result code back up to this object
      if (!GrouperUtil.booleanValue(this.subject.getSuccess())) {
        this.assignResultCode(WsGetMembershipResultCode.valueOf(this.subject
            .getResultCode()));
      }
    } else {
      this.assignResultCode(WsGetMembershipResultCode.SUBJECT_NOT_FOUND);
    }
  }

  /**
   * assign the code from the enum
   * 
   * @param wsGetMembershipResultCode
   */
  public void assignResultCode(WsGetMembershipResultCode wsGetMembershipResultCode) {
    this.getResultMetadata().assignResultCode(
        wsGetMembershipResultCode == null ? null : wsGetMembershipResultCode.name());
    this.getResultMetadata()
        .assignSuccess(
            GrouperServiceUtils.booleanToStringOneChar(wsGetMembershipResultCode
                .isSuccess()));
  }

  /** timestamp it was created: yyyy/MM/dd HH:mm:ss.SSS */
  private String createTime;

  /**
   * metadata about the result
   */
  private WsResultMeta resultMetadata = new WsResultMeta();

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
   * @return the depth
   */
  public int getDepth() {
    return this.depth;
  }

  /**
   * @param depth1
   *            the depth to set
   */
  public void setDepth(int depth1) {
    this.depth = depth1;
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
   * stem name this membership is realted to
   * 
   * @return the stemName
   */
  public String getStemName() {
    return this.stemName;
  }

  /**
   * stem name this membership is realted to
   * 
   * @param stemName1
   *            the stemName to set
   */
  public void setStemName(String stemName1) {
    this.stemName = stemName1;
  }

  /**
   * group name this membership is related to
   * 
   * @return the groupName
   */
  public String getGroupName() {
    return this.groupName;
  }

  /**
   * group name this membership is related to
   * 
   * @param groupName1
   *            the groupName to set
   */
  public void setGroupName(String groupName1) {
    this.groupName = groupName1;
  }

  /**
   * @return the subject
   */
  public WsSubject getSubject() {
    return this.subject;
  }

  /**
   * @param subject1 the subject to set
   */
  public void setSubject(WsSubject subject1) {
    this.subject = subject1;
  }

  /**
   * if there is a parent, this is the id
   * @return the parentMembershipId
   */
  public String getParentMembershipUuid() {
    return this.parentMembershipUuid;
  }

  /**
   * if there is a parent, this is the id
   * @param parentMembershipUuid1 the parentMembershipId to set
   */
  public void setParentMembershipUuid(String parentMembershipUuid1) {
    this.parentMembershipUuid = parentMembershipUuid1;
  }

  /**
   * @return the resultMetadata
   */
  public WsResultMeta getResultMetadata() {
    return this.resultMetadata;
  }

  /**
   * convert members to subject results
   * @param attributeNames to get from subjects
   * @param membershipSet
   * @return the subject results
   */
  public static WsMembership[] convertMembers(Set<Membership> membershipSet,
      String[] attributeNames) {
    int memberSetLength = GrouperUtil.length(membershipSet);
    if (memberSetLength == 0) {
      return null;
    }

    WsMembership[] wsGetMembershipsResultArray = new WsMembership[memberSetLength];
    int index = 0;
    for (Membership membership : membershipSet) {
      wsGetMembershipsResultArray[index++] = new WsMembership(membership, attributeNames);
    }
    return wsGetMembershipsResultArray;
  }

}
