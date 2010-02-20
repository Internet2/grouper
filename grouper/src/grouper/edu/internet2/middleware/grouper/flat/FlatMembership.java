package edu.internet2.middleware.grouper.flat;

import java.util.Set;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * @author shilen 
 * $Id$
 */
@SuppressWarnings("serial")
public class FlatMembership extends GrouperAPI implements Hib3GrouperVersioned {
  
  /** db id for this row */
  public static final String COLUMN_ID = "id";
  
  /** Context id links together multiple operations into one high level action */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** field represented by this flattened membership record */
  public static final String COLUMN_FIELD_ID = "field_id";

  /** owner id */
  public static final String COLUMN_OWNER_ID = "owner_id";
  
  /** owner group if applicable */
  public static final String COLUMN_OWNER_GROUP_ID = "owner_group_id";
  
  /** owner attribute def if applicable */
  public static final String COLUMN_OWNER_ATTR_DEF_ID = "owner_attr_def_id";
  
  /** owner stem if applicable */
  public static final String COLUMN_OWNER_STEM_ID = "owner_stem_id";
  
  /** member id */
  public static final String COLUMN_MEMBER_ID = "member_id";
  
  /** hibernate version */
  public static final String COLUMN_HIBERNATE_VERSION_NUMBER = "hibernate_version_number";
  
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: fieldId */
  public static final String FIELD_FIELD_ID = "fieldId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: memberId */
  public static final String FIELD_MEMBER_ID = "memberId";

  /** constant for field name for: ownerAttrDefId */
  public static final String FIELD_OWNER_ATTR_DEF_ID = "ownerAttrDefId";

  /** constant for field name for: ownerGroupId */
  public static final String FIELD_OWNER_GROUP_ID = "ownerGroupId";

  /** constant for field name for: ownerId */
  public static final String FIELD_OWNER_ID = "ownerId";

  /** constant for field name for: ownerStemId */
  public static final String FIELD_OWNER_STEM_ID = "ownerStemId";

  /**
   * fields which are included in db version
   */
  /*
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_FIELD_ID, FIELD_ID, FIELD_MEMBER_ID, 
      FIELD_OWNER_ATTR_DEF_ID, FIELD_OWNER_GROUP_ID, FIELD_OWNER_ID, FIELD_OWNER_STEM_ID);
  */
  
  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_FIELD_ID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, 
      FIELD_MEMBER_ID, FIELD_OWNER_ATTR_DEF_ID, FIELD_OWNER_GROUP_ID, FIELD_OWNER_ID, 
      FIELD_OWNER_STEM_ID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  

  /**
   * name of the table in the database.
   */
  public static final String TABLE_GROUPER_FLAT_MEMBERSHIPS = "grouper_flat_memberships";

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** field associated with this record */
  private String fieldId;
  
  /** owner id */
  private String ownerId;
  
  /** group id for group memberships.  this is the owner. */
  private String ownerGroupId;
  
  /** stem id for stem memberships.  this is the owner. */
  private String ownerStemId;
  
  /** attr def id for attr def memberships.  this is the owner. */
  private String ownerAttrDefId;

  /** member id */
  private String memberId;
  
  /** member */
  private Member member;
  
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#clone()
   */
  @Override
  public GrouperAPI clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
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
   * @return field id
   */
  public String getFieldId() {
    return fieldId;
  }

  /**
   * @param fieldId
   */
  public void setFieldId(String fieldId) {
    this.fieldId = fieldId;
  }
  
  /**
   * @return owner id
   */
  public String getOwnerId() {
    return ownerId;
  }

  /**
   * Set owner id
   * @param ownerId
   */
  public void setOwnerId(String ownerId) {
    this.ownerId = ownerId;
  }

  /**
   * @return member id
   */
  public String getMemberId() {
    return memberId;
  }

  /**
   * Set member id
   * @param memberId
   */
  public void setMemberId(String memberId) {
    this.memberId = memberId;
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
    if (ownerGroupId != null) {
      setOwnerId(ownerGroupId);
    }
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
    if (ownerStemId != null) {
      setOwnerId(ownerStemId);
    }
  }
  
  /**
   * @return attrdef id for the owner if this is a attrdef membership
   */
  public String getOwnerAttrDefId() {
    return this.ownerAttrDefId;
  }
  

  /**
   * Set attrdef id for the owner if this is a attrdef membership
   * @param ownerAttrDefId
   */
  public void setOwnerAttrDefId(String ownerAttrDefId) {
    this.ownerAttrDefId = ownerAttrDefId;
    if (ownerAttrDefId != null) {
      setOwnerId(ownerAttrDefId);
    }
  }

  /**
   * @return member
   */
  public Member getMember() {
    
    if (member != null) {
      return member;
    }

    String uuid = this.getMemberId();
    if (uuid == null) {
      throw new MemberNotFoundException("flat membership does not have a member!");
    }
    
    member = GrouperDAOFactory.getFactory().getMember().findByUuid(uuid, true) ;
    return member;
  } 
  
  /**
   * set the member of this flat membership
   * @param member
   */
  public void setMember(Member member) {
    this.member = member;
  }
  
  /**
   * save this object
   */
  public void save() {
    GrouperDAOFactory.getFactory().getFlatMembership().save(this);
  }
  
  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getFlatMembership().delete(this);
  }
}
