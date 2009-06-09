package edu.internet2.middleware.grouper.group;

import java.util.Date;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.GroupSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author shilen $Id: GroupSet.java,v 1.1 2009-06-09 22:55:39 shilen Exp $
 *
 */
@SuppressWarnings("serial")
public class GroupSet extends GrouperAPI implements Hib3GrouperVersioned {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorId */
  public static final String FIELD_CREATOR_ID = "creatorId";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";
  
  /** constant for field name for: viaGroupId */
  public static final String FIELD_VIA_GROUP_ID = "viaGroupId";

  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";

  /** constant for field name for: type */
  public static final String FIELD_MSHIP_TYPE = "type";
  
  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: memberGroupId */
  public static final String FIELD_MEMBER_GROUP_ID = "memberGroupId";

  /** constant for field name for: memberGroupIdNull */
  public static final String FIELD_MEMBER_GROUP_ID_NULL = "memberGroupIdNull";

  /** constant for field name for: memberStemId */
  public static final String FIELD_MEMBER_STEM_ID = "memberStemId";

  /** constant for field name for: memberStemIdNull */
  public static final String FIELD_MEMBER_STEM_ID_NULL = "memberStemIdNull";

  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerGroupIdNull */
  public static final String FIELD_OWNER_GROUP_ID_NULL = "ownerGroupIdNull";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";

  /** constant for field name for: ownerStemIdNull */
  public static final String FIELD_OWNER_STEM_ID_NULL = "ownerStemIdNull";

  /** constant for field name for: parentId */
  public static final String FIELD_PARENT_ID = "parentId";

  /**
   * fields which are included in db version
   */
  /*
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_ID, FIELD_DEPTH, FIELD_VIA_GROUP_ID, 
      FIELD_FIELD_ID, FIELD_MSHIP_TYPE, FIELD_ID, FIELD_MEMBER_GROUP_ID, FIELD_MEMBER_GROUP_ID_NULL, 
      FIELD_MEMBER_STEM_ID, FIELD_MEMBER_STEM_ID_NULL, FIELD_OWNER_GROUP_ID, FIELD_OWNER_GROUP_ID_NULL, 
      FIELD_OWNER_STEM_ID, FIELD_OWNER_STEM_ID_NULL, FIELD_PARENT_ID);
  */

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_ID, FIELD_DEPTH, FIELD_VIA_GROUP_ID,
      FIELD_FIELD_ID, FIELD_MSHIP_TYPE, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_MEMBER_GROUP_ID, 
      FIELD_MEMBER_GROUP_ID_NULL, FIELD_MEMBER_STEM_ID, FIELD_MEMBER_STEM_ID_NULL, FIELD_OWNER_GROUP_ID, 
      FIELD_OWNER_GROUP_ID_NULL, FIELD_OWNER_STEM_ID, FIELD_OWNER_STEM_ID_NULL, FIELD_PARENT_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//


  /**
   * name of the group set table in the database.
   */
  public static final String TABLE_GROUPER_GROUP_SET = "grouper_group_set";

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** field associated with this record */
  private String fieldId;
  
  /** membership type -- immediate, effective, or composite */
  private String type = Membership.IMMEDIATE;

  /** depth - 0 for self records, 1 for immediate memberships, > 1 for effective */
  private int depth;
  
  /** parent record */
  private String parentId;
  
  /** creator */
  private String creatorId;
  
  /** create time */
  private Long createTime = new Date().getTime();
  
  /** group id for group memberships.  this is the owner. */
  private String ownerGroupId;
  
  /** ownerGroupId except nulls are replaced with a string so we can use this in a unique constraint */
  private String ownerGroupIdNull = GroupSet.nullColumnValue;

  /** stem id for stem memberships.  this is the owner. */
  private String ownerStemId;
  
  /** ownerStemId except nulls are replaced with a string so we can use this in a unique constraint */
  private String ownerStemIdNull = GroupSet.nullColumnValue;
  
  /** group id for group memberships.  this is the member. */
  private String memberGroupId;
  
  /** memberGroupId except nulls are replaced with a string so we can use this in a unique constraint */
  private String memberGroupIdNull = GroupSet.nullColumnValue;

  /** stem id for stem memberships.  this is the member. */
  private String memberStemId;
  
  /** memberStemId except nulls are replaced with a string so we can use this in a unique constraint */
  private String memberStemIdNull = GroupSet.nullColumnValue;

  /**
   * the value we're storing in the db for nulls that need a value so that we can add a unique constraint.
   */
  public static final String nullColumnValue = "<NULL>";

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof GroupSet)) {
      return false;
    }
    
    GroupSet that = (GroupSet) other;
    return new EqualsBuilder()
      .append(this.fieldId, that.fieldId)
      .append(this.type, that.type)
      .append(this.depth, that.depth)
      .append(this.parentId, that.parentId)
      .append(this.ownerGroupId, that.ownerGroupId)
      .append(this.ownerStemId, that.ownerStemId)
      .append(this.memberGroupId, that.memberGroupId)
      .append(this.memberStemId, that.memberStemId)
      .isEquals();
  } 

  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.fieldId)
      .append(this.type)
      .append(this.depth)
      .append(this.parentId)
      .append(this.ownerGroupId)
      .append(this.ownerStemId)
      .append(this.memberGroupId)
      .append(this.memberStemId)
      .toHashCode();
  }
  

  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    if (this.createTime == null) {
      this.createTime = System.currentTimeMillis();
    }
    
    if (this.creatorId == null) {
      this.creatorId = GrouperSession.staticGrouperSession().getMember().getUuid();
    }
  }
  
  /**
   * @return the parent group set
   */
  public GroupSet getParentGroupSet() {
    if (depth == 0) {
      throw new GroupSetNotFoundException("no parent");
    }
    
    GroupSet parent = GrouperDAOFactory.getFactory().getGroupSet().findParentGroupSet(this) ;
    return parent;
  }
  
  
  /**
   * @return id
   */
  public String getId() {
    return id;
  }

  
  /**
   * set id
   * @param id
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return context id
   */
  public String getContextId() {
    return contextId;
  }

  
  /**
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * @return field id
   */
  public String getFieldId() {
    return fieldId;
  }

  /**
   * return field id
   * @param fieldId
   */
  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }

  
  /**
   * @return depth
   */
  public int getDepth() {
    return depth;
  }

  
  /**
   * set depth
   * @param depth
   */
  public void setDepth(int depth) {
    this.depth = depth;
  }

  /**
   * @return via group id
   */
  public String getViaGroupId() {
    if (depth == 0) {
      return null;
    }
    
    return memberGroupId;
  }

  
  /**
   * Set via group id.  This is for internal use only.
   * @param viaGroupId
   */
  public void setViaGroupId(String viaGroupId) {
    // not used
  }
  
  /**
   * @return parent id
   */
  public String getParentId() {
    return parentId;
  }

  
  /**
   * set parent id
   * @param parentId
   */
  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  
  /**
   * @return creator
   */
  public String getCreatorId() {
    return creatorId;
  }

  
  /**
   * set creator
   * @param creatorId
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }


  /**
   * @return create time
   */
  public Long getCreateTime() {
    return createTime;
  }

  
  /**
   * set create time
   * @param createTime
   */
  public void setCreateTime(Long createTime) {
    this.createTime = createTime;
  }

  
  /**
   * @return group id for the owner if this is a group membership
   */
  public String getOwnerGroupId() {
    return ownerGroupId;
  }

  /**
   * Set group id for the owner if this is a group membership
   * @param ownerGroupId
   */
  public void setOwnerGroupId(String ownerGroupId) {
    this.ownerGroupId = ownerGroupId;
    setOwnerGroupIdNull(ownerGroupId);
    if (ownerGroupId == null) {
      setOwnerGroupIdNull(GroupSet.nullColumnValue);
    }
  }

  /**
   * This is for internal use only.  This is the same as getOwnerGroupId() except nulls are replaced with
   * a constant string.
   * @return group id for the owner if this is a group membership
   */
  public String getOwnerGroupIdNull() {
    return ownerGroupIdNull;
  }

  
  /**
   * Set group id for the owner if this is a group membership.  This is for internal use only.
   * @param ownerGroupIdNull
   */
  public void setOwnerGroupIdNull(String ownerGroupIdNull) {
    this.ownerGroupIdNull = ownerGroupIdNull;
  }

  /**
   * @return stem id for the owner if this is a stem membership
   */
  public String getOwnerStemId() {
    return ownerStemId;
  }

  
  /**
   * Set stem id for the owner if this is a stem membership
   * @param ownerStemId
   */
  public void setOwnerStemId(String ownerStemId) {
    this.ownerStemId = ownerStemId;
    setOwnerStemIdNull(ownerStemId);
    if (ownerStemId == null) {
      setOwnerStemIdNull(GroupSet.nullColumnValue);
    }
  }

  
  /**
   * This is for internal use only.  This is the same as getOwnerStemId() except nulls are replaced with
   * a constant string.
   * @return stem id for the owner if this is a stem membership
   */
  public String getOwnerStemIdNull() {
    return ownerStemIdNull;
  }

  /**
   * Set stem id for the owner if this is a stem membership.  This is for internal use only.
   * @param ownerStemIdNull
   */
  public void setOwnerStemIdNull(String ownerStemIdNull) {
    this.ownerStemIdNull = ownerStemIdNull;
  }

  /**
   * @return group id for the member if the member is a group
   */
  public String getMemberGroupId() {
    return memberGroupId;
  }

  
  /**
   * Set group id for the member if the member is a group
   * @param memberGroupId
   */
  public void setMemberGroupId(String memberGroupId) {
    this.memberGroupId = memberGroupId;
    setMemberGroupIdNull(memberGroupId);
    if (memberGroupId == null) {
      setMemberGroupIdNull(GroupSet.nullColumnValue);
    }
  }

  /**
   * This is for internal use only.  This is the same as getMemberGroupId() except nulls are replaced with
   * a constant string.
   * @return group id for the member if the member is a group
   */  
  public String getMemberGroupIdNull() {
    return memberGroupIdNull;
  }

  /**
   * Set group id for the member if the member is a group.  This is for internal use only.
   * @param memberGroupIdNull
   */  
  public void setMemberGroupIdNull(String memberGroupIdNull) {
    this.memberGroupIdNull = memberGroupIdNull;
  }

  
  /**
   * @return stem id for the member if the member is a stem
   */
  public String getMemberStemId() {
    return memberStemId;
  }

  
  /**
   * Set stem id for the member if the member is a stem
   * @param memberStemId
   */
  public void setMemberStemId(String memberStemId) {
    this.memberStemId = memberStemId;
    setMemberStemIdNull(memberStemId);
    if (memberStemId == null) {
      setMemberStemIdNull(GroupSet.nullColumnValue);
    }
  }

  /**
   * This is for internal use only.  This is the same as getMemberStemId() except nulls are replaced with
   * a constant string.
   * @return stem id for the member if the member is a stem
   */  
  public String getMemberStemIdNull() {
    return memberStemIdNull;
  }

  /**
   * Set stem id for the member if the member is a stem.  This is for internal use only.
   * @param memberStemIdNull
   */  
  public void setMemberStemIdNull(String memberStemIdNull) {
    this.memberStemIdNull = memberStemIdNull;
  }
  

  
  /**
   * @return membership type (immediate, effective, or composite)
   */
  public String getType() {
    return type;
  }

  
  /**
   * set membership type
   * @param type
   */
  public void setType(String type) {
    this.type = type;
  }

}
