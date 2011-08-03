package edu.internet2.middleware.grouper.pit;

import java.io.Serializable;
import java.sql.Timestamp;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.permissions.PermissionEntryBase;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen
 * $Id$
 */
@SuppressWarnings("serial")
public class PITPermissionAllView extends PermissionEntryBase implements Serializable {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: action */
  public static final String FIELD_ACTION = "action";

  /** constant for field name for: actionId */
  public static final String FIELD_ACTION_ID = "actionId";

  /** constant for field name for: actionSetActiveDb */
  public static final String FIELD_ACTION_SET_ACTIVE_DB = "actionSetActiveDb";

  /** constant for field name for: actionSetEndTimeDb */
  public static final String FIELD_ACTION_SET_END_TIME_DB = "actionSetEndTimeDb";

  /** constant for field name for: actionSetStartTimeDb */
  public static final String FIELD_ACTION_SET_START_TIME_DB = "actionSetStartTimeDb";

  /** constant for field name for: attributeAssignActionSetDepth */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ACTION_SET_DEPTH = "attributeAssignActionSetDepth";

  /** constant for field name for: attributeAssignActiveDb */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ACTIVE_DB = "attributeAssignActiveDb";

  /** constant for field name for: attributeAssignEndTimeDb */
  public static final String FIELD_ATTRIBUTE_ASSIGN_END_TIME_DB = "attributeAssignEndTimeDb";

  /** constant for field name for: attributeAssignId */
  public static final String FIELD_ATTRIBUTE_ASSIGN_ID = "attributeAssignId";

  /** constant for field name for: attributeAssignStartTimeDb */
  public static final String FIELD_ATTRIBUTE_ASSIGN_START_TIME_DB = "attributeAssignStartTimeDb";

  /** constant for field name for: attributeDefId */
  public static final String FIELD_ATTRIBUTE_DEF_ID = "attributeDefId";

  /** constant for field name for: attributeDefNameId */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_ID = "attributeDefNameId";

  /** constant for field name for: attributeDefNameName */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_NAME = "attributeDefNameName";

  /** constant for field name for: attributeDefNameSetActiveDb */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_SET_ACTIVE_DB = "attributeDefNameSetActiveDb";

  /** constant for field name for: attributeDefNameSetDepth */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_SET_DEPTH = "attributeDefNameSetDepth";

  /** constant for field name for: attributeDefNameSetEndTimeDb */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_SET_END_TIME_DB = "attributeDefNameSetEndTimeDb";

  /** constant for field name for: attributeDefNameSetStartTimeDb */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_SET_START_TIME_DB = "attributeDefNameSetStartTimeDb";

  /** constant for field name for: groupSetActiveDb */
  public static final String FIELD_GROUP_SET_ACTIVE_DB = "groupSetActiveDb";

  /** constant for field name for: groupSetEndTimeDb */
  public static final String FIELD_GROUP_SET_END_TIME_DB = "groupSetEndTimeDb";

  /** constant for field name for: groupSetId */
  public static final String FIELD_GROUP_SET_ID = "groupSetId";

  /** constant for field name for: groupSetStartTimeDb */
  public static final String FIELD_GROUP_SET_START_TIME_DB = "groupSetStartTimeDb";

  /** constant for field name for: memberId */
  public static final String FIELD_MEMBER_ID = "memberId";

  /** constant for field name for: membershipActiveDb */
  public static final String FIELD_MEMBERSHIP_ACTIVE_DB = "membershipActiveDb";

  /** constant for field name for: membershipDepth */
  public static final String FIELD_MEMBERSHIP_DEPTH = "membershipDepth";

  /** constant for field name for: membershipEndTimeDb */
  public static final String FIELD_MEMBERSHIP_END_TIME_DB = "membershipEndTimeDb";

  /** constant for field name for: membershipId */
  public static final String FIELD_MEMBERSHIP_ID = "membershipId";

  /** constant for field name for: membershipStartTimeDb */
  public static final String FIELD_MEMBERSHIP_START_TIME_DB = "membershipStartTimeDb";

  /** constant for field name for: roleId */
  public static final String FIELD_ROLE_ID = "roleId";

  /** constant for field name for: roleName */
  public static final String FIELD_ROLE_NAME = "roleName";

  /** constant for field name for: roleSetActiveDb */
  public static final String FIELD_ROLE_SET_ACTIVE_DB = "roleSetActiveDb";

  /** constant for field name for: roleSetDepth */
  public static final String FIELD_ROLE_SET_DEPTH = "roleSetDepth";

  /** constant for field name for: roleSetEndTimeDb */
  public static final String FIELD_ROLE_SET_END_TIME_DB = "roleSetEndTimeDb";

  /** constant for field name for: roleSetStartTimeDb */
  public static final String FIELD_ROLE_SET_START_TIME_DB = "roleSetStartTimeDb";

  /** constant for field name for: subjectId */
  public static final String FIELD_SUBJECT_ID = "subjectId";

  /** constant for field name for: subjectSourceId */
  public static final String FIELD_SUBJECT_SOURCE_ID = "subjectSourceId";
  
  /** constant for field name for: actionSetId */
  public static final String FIELD_ACTION_SET_ID = "actionSetId";  

  /** constant for field name for: roleSetId */
  public static final String FIELD_ROLE_SET_ID = "roleSetId";  

  /** constant for field name for: attributeDefNameSetId */
  public static final String FIELD_ATTRIBUTE_DEF_NAME_SET_ID = "attributeDefNameSetId";  
  
  
  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** */
  private String actionSetId;
  
  /** */
  private String roleSetId;
  
  /** */
  private String attributeDefNameSetId;

  /** */
  private String groupSetId;
  
  /** */
  private String groupSetActiveDb;
  
  /** */
  private Long groupSetStartTimeDb;
  
  /** */
  private Long groupSetEndTimeDb;
  
  /** */
  private String membershipActiveDb;
  
  /** */
  private Long membershipStartTimeDb;
  
  /** */
  private Long membershipEndTimeDb;
  
  /** */
  private String roleSetActiveDb;
  
  /** */
  private Long roleSetStartTimeDb;
  
  /** */
  private Long roleSetEndTimeDb;
  
  /** */
  private String actionSetActiveDb;
  
  /** */
  private Long actionSetStartTimeDb;
  
  /** */
  private Long actionSetEndTimeDb;
  
  /** */
  private String attributeDefNameSetActiveDb;
  
  /** */
  private Long attributeDefNameSetStartTimeDb;
  
  /** */
  private Long attributeDefNameSetEndTimeDb;
  
  /** */
  private String attributeAssignActiveDb;
  
  /** */
  private Long attributeAssignStartTimeDb;
  
  /** */
  private Long attributeAssignEndTimeDb;
  
  /**
   * @return groupSetId
   */
  public String getGroupSetId() {
    return groupSetId;
  }

  /**
   * @param groupSetId
   */
  public void setGroupSetId(String groupSetId) {
    this.groupSetId = groupSetId;
  }
  
  /**
   * @return the groupSetActiveDb
   */
  public String getGroupSetActiveDb() {
    return groupSetActiveDb;
  }

  
  /**
   * @param groupSetActiveDb the groupSetActiveDb to set
   */
  public void setGroupSetActiveDb(String groupSetActiveDb) {
    this.groupSetActiveDb = groupSetActiveDb;
  }

  
  /**
   * @return the groupSetStartTimeDb
   */
  public Long getGroupSetStartTimeDb() {
    return groupSetStartTimeDb;
  }

  
  /**
   * @param groupSetStartTimeDb the groupSetStartTimeDb to set
   */
  public void setGroupSetStartTimeDb(Long groupSetStartTimeDb) {
    this.groupSetStartTimeDb = groupSetStartTimeDb;
  }

  
  /**
   * @return the groupSetEndTimeDb
   */
  public Long getGroupSetEndTimeDb() {
    return groupSetEndTimeDb;
  }

  
  /**
   * @param groupSetEndTimeDb the groupSetEndTimeDb to set
   */
  public void setGroupSetEndTimeDb(Long groupSetEndTimeDb) {
    this.groupSetEndTimeDb = groupSetEndTimeDb;
  }

  
  /**
   * @return the membershipActiveDb
   */
  public String getMembershipActiveDb() {
    return membershipActiveDb;
  }

  
  /**
   * @param membershipActiveDb the membershipActiveDb to set
   */
  public void setMembershipActiveDb(String membershipActiveDb) {
    this.membershipActiveDb = membershipActiveDb;
  }

  
  /**
   * @return the membershipStartTimeDb
   */
  public Long getMembershipStartTimeDb() {
    return membershipStartTimeDb;
  }

  
  /**
   * @param membershipStartTimeDb the membershipStartTimeDb to set
   */
  public void setMembershipStartTimeDb(Long membershipStartTimeDb) {
    this.membershipStartTimeDb = membershipStartTimeDb;
  }

  
  /**
   * @return the membershipEndTimeDb
   */
  public Long getMembershipEndTimeDb() {
    return membershipEndTimeDb;
  }

  
  /**
   * @param membershipEndTimeDb the membershipEndTimeDb to set
   */
  public void setMembershipEndTimeDb(Long membershipEndTimeDb) {
    this.membershipEndTimeDb = membershipEndTimeDb;
  }

  
  /**
   * @return the roleSetActiveDb
   */
  public String getRoleSetActiveDb() {
    return roleSetActiveDb;
  }

  
  /**
   * @param roleSetActiveDb the roleSetActiveDb to set
   */
  public void setRoleSetActiveDb(String roleSetActiveDb) {
    this.roleSetActiveDb = roleSetActiveDb;
  }

  
  /**
   * @return the roleSetStartTimeDb
   */
  public Long getRoleSetStartTimeDb() {
    return roleSetStartTimeDb;
  }

  
  /**
   * @param roleSetStartTimeDb the roleSetStartTimeDb to set
   */
  public void setRoleSetStartTimeDb(Long roleSetStartTimeDb) {
    this.roleSetStartTimeDb = roleSetStartTimeDb;
  }

  
  /**
   * @return the roleSetEndTimeDb
   */
  public Long getRoleSetEndTimeDb() {
    return roleSetEndTimeDb;
  }

  
  /**
   * @param roleSetEndTimeDb the roleSetEndTimeDb to set
   */
  public void setRoleSetEndTimeDb(Long roleSetEndTimeDb) {
    this.roleSetEndTimeDb = roleSetEndTimeDb;
  }

  
  /**
   * @return the actionSetActiveDb
   */
  public String getActionSetActiveDb() {
    return actionSetActiveDb;
  }

  
  /**
   * @param actionSetActiveDb the actionSetActiveDb to set
   */
  public void setActionSetActiveDb(String actionSetActiveDb) {
    this.actionSetActiveDb = actionSetActiveDb;
  }

  
  /**
   * @return the actionSetStartTimeDb
   */
  public Long getActionSetStartTimeDb() {
    return actionSetStartTimeDb;
  }

  
  /**
   * @param actionSetStartTimeDb the actionSetStartTimeDb to set
   */
  public void setActionSetStartTimeDb(Long actionSetStartTimeDb) {
    this.actionSetStartTimeDb = actionSetStartTimeDb;
  }

  
  /**
   * @return the actionSetEndTimeDb
   */
  public Long getActionSetEndTimeDb() {
    return actionSetEndTimeDb;
  }

  
  /**
   * @param actionSetEndTimeDb the actionSetEndTimeDb to set
   */
  public void setActionSetEndTimeDb(Long actionSetEndTimeDb) {
    this.actionSetEndTimeDb = actionSetEndTimeDb;
  }

  
  /**
   * @return the attributeDefNameSetActiveDb
   */
  public String getAttributeDefNameSetActiveDb() {
    return attributeDefNameSetActiveDb;
  }

  
  /**
   * @param attributeDefNameSetActiveDb the attributeDefNameSetActiveDb to set
   */
  public void setAttributeDefNameSetActiveDb(String attributeDefNameSetActiveDb) {
    this.attributeDefNameSetActiveDb = attributeDefNameSetActiveDb;
  }

  
  /**
   * @return the attributeDefNameSetStartTimeDb
   */
  public Long getAttributeDefNameSetStartTimeDb() {
    return attributeDefNameSetStartTimeDb;
  }

  
  /**
   * @param attributeDefNameSetStartTimeDb the attributeDefNameSetStartTimeDb to set
   */
  public void setAttributeDefNameSetStartTimeDb(Long attributeDefNameSetStartTimeDb) {
    this.attributeDefNameSetStartTimeDb = attributeDefNameSetStartTimeDb;
  }

  
  /**
   * @return the attributeDefNameSetEndTimeDb
   */
  public Long getAttributeDefNameSetEndTimeDb() {
    return attributeDefNameSetEndTimeDb;
  }

  
  /**
   * @param attributeDefNameSetEndTimeDb the attributeDefNameSetEndTimeDb to set
   */
  public void setAttributeDefNameSetEndTimeDb(Long attributeDefNameSetEndTimeDb) {
    this.attributeDefNameSetEndTimeDb = attributeDefNameSetEndTimeDb;
  }

  
  /**
   * @return the attributeAssignActiveDb
   */
  public String getAttributeAssignActiveDb() {
    return attributeAssignActiveDb;
  }

  
  /**
   * @param attributeAssignActiveDb the attributeAssignActiveDb to set
   */
  public void setAttributeAssignActiveDb(String attributeAssignActiveDb) {
    this.attributeAssignActiveDb = attributeAssignActiveDb;
  }

  
  /**
   * @return the attributeAssignStartTimeDb
   */
  public Long getAttributeAssignStartTimeDb() {
    return attributeAssignStartTimeDb;
  }

  
  /**
   * @param attributeAssignStartTimeDb the attributeAssignStartTimeDb to set
   */
  public void setAttributeAssignStartTimeDb(Long attributeAssignStartTimeDb) {
    this.attributeAssignStartTimeDb = attributeAssignStartTimeDb;
  }

  
  /**
   * @return the attributeAssignEndTimeDb
   */
  public Long getAttributeAssignEndTimeDb() {
    return attributeAssignEndTimeDb;
  }

  
  /**
   * @param attributeAssignEndTimeDb the attributeAssignEndTimeDb to set
   */
  public void setAttributeAssignEndTimeDb(Long attributeAssignEndTimeDb) {
    this.attributeAssignEndTimeDb = attributeAssignEndTimeDb;
  }
  
  /**
   * @return true if active
   */
  public boolean isActive() {
    if (membershipActiveDb == null || groupSetActiveDb == null || roleSetActiveDb == null ||
        actionSetActiveDb == null || attributeDefNameSetActiveDb == null || attributeAssignActiveDb == null) {
      throw new RuntimeException("active flags should not be null.");
    }
    
    if (membershipActiveDb.equals("T") && groupSetActiveDb.equals("T") && roleSetActiveDb.equals("T") && 
        actionSetActiveDb.equals("T") && attributeDefNameSetActiveDb.equals("T") && attributeAssignActiveDb.equals("T")) {
      return true;
    }
    
    return false;
  }
  
  /**
   * @return start time
   */
  public Timestamp getStartTime() {
    if (membershipStartTimeDb == null || groupSetStartTimeDb == null || roleSetStartTimeDb == null ||
        actionSetStartTimeDb == null || attributeDefNameSetStartTimeDb == null || attributeAssignStartTimeDb == null) {
      throw new RuntimeException("start times should not be null.");
    }
    
    Long startTime = GrouperUtil.getMaxLongValue(membershipStartTimeDb, groupSetStartTimeDb, roleSetStartTimeDb,
        actionSetStartTimeDb, attributeDefNameSetStartTimeDb, attributeAssignStartTimeDb);
    
    return new Timestamp(startTime / 1000);
  }
  
  /**
   * @return end time
   */
  public Timestamp getEndTime() {
    if (membershipEndTimeDb == null && groupSetEndTimeDb == null && roleSetEndTimeDb == null &&
        actionSetEndTimeDb == null && attributeDefNameSetEndTimeDb == null && attributeAssignEndTimeDb == null) {
      return null;
    }
    
    Long endTime = GrouperUtil.getMinLongValue(membershipEndTimeDb, groupSetEndTimeDb, roleSetEndTimeDb, 
        actionSetEndTimeDb, attributeDefNameSetEndTimeDb, attributeAssignEndTimeDb);

    return new Timestamp(endTime / 1000);
  }

  
  /**
   * @return the actionSetId
   */
  public String getActionSetId() {
    return actionSetId;
  }

  
  /**
   * @param actionSetId the actionSetId to set
   */
  public void setActionSetId(String actionSetId) {
    this.actionSetId = actionSetId;
  }

  
  /**
   * @return the roleSetId
   */
  public String getRoleSetId() {
    return roleSetId;
  }

  
  /**
   * @param roleSetId the roleSetId to set
   */
  public void setRoleSetId(String roleSetId) {
    this.roleSetId = roleSetId;
  }

  
  /**
   * @return the attributeDefNameSetId
   */
  public String getAttributeDefNameSetId() {
    return attributeDefNameSetId;
  }

  
  /**
   * @param attributeDefNameSetId the attributeDefNameSetId to set
   */
  public void setAttributeDefNameSetId(String attributeDefNameSetId) {
    this.attributeDefNameSetId = attributeDefNameSetId;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {

    if (this == obj) {
      return true;
    }
    
    if (!(obj instanceof PITPermissionAllView)) {
      return false;
    }
    
    PITPermissionAllView other = (PITPermissionAllView)obj;
    
    return new EqualsBuilder().append(this.getRoleId(), other.getRoleId())
      .append(this.getMemberId(), other.getMemberId())
      .append(this.getAction(), other.getAction())
      .append(this.getAttributeDefNameId(), other.getAttributeDefNameId())
      .append(this.getMembershipStartTimeDb(), other.getMembershipStartTimeDb())
      .append(this.getGroupSetStartTimeDb(), other.getGroupSetStartTimeDb())
      .append(this.getRoleSetStartTimeDb(), other.getRoleSetStartTimeDb())
      .append(this.getActionSetStartTimeDb(), other.getActionSetStartTimeDb())
      .append(this.getAttributeDefNameSetStartTimeDb(), other.getAttributeDefNameSetStartTimeDb())
      .append(this.getAttributeAssignStartTimeDb(), other.getAttributeAssignStartTimeDb())
      .append(this.getPermissionType(), other.getPermissionType())
      .append(this.getAttributeAssignId(), other.getAttributeAssignId())
      .isEquals();
  }


  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getRoleId())
      .append(this.getMemberId())
      .append(this.getAction())
      .append(this.getAttributeDefNameId())
      .append(this.getMembershipStartTimeDb())
      .append(this.getGroupSetStartTimeDb())
      .append(this.getRoleSetStartTimeDb())
      .append(this.getActionSetStartTimeDb())
      .append(this.getAttributeDefNameSetStartTimeDb())
      .append(this.getAttributeAssignStartTimeDb())
      .append(this.getPermissionType())
      .append(this.getAttributeAssignId())
      .toHashCode();
  }
}