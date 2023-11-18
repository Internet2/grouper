package edu.internet2.middleware.grouper.changeLog.esb.consumer;

/**
 * membership to analyze
 * @author mchyzer
 *
 */
public class ProvisioningMembershipMessage {

  /**
   * 
   */
  public ProvisioningMembershipMessage() {
  }

  /**
   * 
   * @param groupId1
   * @param memberId1
   */
  public ProvisioningMembershipMessage(String groupId1, String memberId1) {
    super();
    this.groupId = groupId1;
    this.memberId = memberId1;
  }

  /**
   * 
   * @param groupId1
   * @param memberId1
   */
  public ProvisioningMembershipMessage(String groupId1, String memberId1, String fieldName1) {
    this(groupId1, memberId1);
    this.fieldId = fieldName1;
  }

  /**
   * e.g. uuid for members
   */
  private String fieldId;
  
  /**
   * e.g. uuid for members
   * @return field name
   */
  public String getFieldId() {
    return this.fieldId;
  }

  /**
   * e.g. uuid for members
   * @param fieldName1
   */
  public void setFieldId(String fieldName1) {
    this.fieldId = fieldName1;
  }

  /**
   * member id of membership
   */
  private String memberId;

  /**
   * member id of membership
   * @return member id
   */
  public String getMemberId() {
    return this.memberId;
  }

  /**
   * member id of membership
   * @param memberId1
   */
  public void setMemberId(String memberId1) {
    this.memberId = memberId1;
  }
  
  /**
   * group id of membership
   */
  private String groupId;


  /**
   * member id of membership
   * @return
   */
  public String getGroupId() {
    return this.groupId;
  }


  /**
   * member id of membership
   * @param groupId1
   */
  public void setGroupId(String groupId1) {
    this.groupId = groupId1;
  }
  
  
}
