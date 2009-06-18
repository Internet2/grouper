package edu.internet2.middleware.grouper.attr;

import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * 
 * @author mchyzer $Id: AttributeDefSet.java,v 1.1 2009-06-18 11:35:13 mchyzer Exp $
 *
 */
@SuppressWarnings("serial")
public class AttributeDefSet extends GrouperAPI implements Hib3GrouperVersioned {
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorId */
  public static final String FIELD_CREATOR_ID = "creatorId";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: memberAttributeDefNameId */
  public static final String FIELD_MEMBER_ATTRIBUTE_DEF_NAME_ID = "memberAttributeDefNameId";

  /** constant for field name for: ownerAttributeDefNameId */
  public static final String FIELD_OWNER_ATTRIBUTE_DEF_NAME_ID = "ownerAttributeDefNameId";

  /** constant for field name for: parentId */
  public static final String FIELD_PARENT_ID = "parentId";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /** constant for field name for: verb */
  public static final String FIELD_VERB = "verb";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_ID, FIELD_DEPTH, 
      FIELD_ID, FIELD_MEMBER_ATTRIBUTE_DEF_NAME_ID, FIELD_OWNER_ATTRIBUTE_DEF_NAME_ID, FIELD_PARENT_ID, 
      FIELD_TYPE, FIELD_VERB);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATE_TIME, FIELD_CREATOR_ID, FIELD_DEPTH, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_MEMBER_ATTRIBUTE_DEF_NAME_ID, FIELD_OWNER_ATTRIBUTE_DEF_NAME_ID, 
      FIELD_PARENT_ID, FIELD_TYPE, FIELD_VERB);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /**
   * name of the group set table in the database.
   */
  public static final String TABLE_ATTRIBUTE_SET = "grouper_attribute_def_set";

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** verb associated with this record */
  private String verb;
  
  /** membership type -- immediate, or effective */
  private AttributeDefAssignmentType type = AttributeDefAssignmentType.immediate;

  /** parent record, for immediate it is this record id, for effective, it is 
   * the id in this table which is the reason this effective membership exists 
   */
  private String parentId;
  
  /** creator */
  private String creatorId;
  
  /** create time */
  private Long createTime;
  
  /** attribute def name id of the parent */
  private String ownerAttributeDefNameId;
  
  /** attribute def name id of the child */
  private String memberAttributeDefNameId;

  /**
   * depth - 0 for self records, 1 for immediate memberships, > 1 for effective 
   */
  private int depth;

  /**
   * business equals based on fields
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof AttributeDefSet)) {
      return false;
    }
    
    AttributeDefSet that = (AttributeDefSet) other;
    return new EqualsBuilder()
      .append(this.verb, that.verb)
      .append(this.type, that.type)
      .append(this.depth, that.depth)
      .append(this.parentId, that.parentId)
      .append(this.ownerAttributeDefNameId, that.ownerAttributeDefNameId)
      .append(this.memberAttributeDefNameId, that.memberAttributeDefNameId)
      .isEquals();
  } 

  /**
   * business hashcode based on fields
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.verb)
      .append(this.type)
      .append(this.depth)
      .append(this.parentId)
      .append(this.ownerAttributeDefNameId)
      .append(this.memberAttributeDefNameId)
      .toHashCode();
  }
  
  /**
   * 
   */
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
   * @return the parent group set or null if none
   */
  public AttributeDefSet getParentAttributeDefSet() {
    if (depth == 0) {
      return null;
    }
    
    AttributeDefSet parent = null; 
      //TODO GrouperDAOFactory.getFactory().getAttributeDefSet().findParentGroupSet(this) ;
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
   * @return verb
   */
  public String getVerb() {
    return this.verb;
  }

  /**
   * return verb
   * @param theVerb
   */
  public void setVerb(String theVerb) {
    this.verb = theVerb;
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
   * @return attribute def id for the owner
   */
  public String getOwnerAttributeDefId() {
    return this.ownerAttributeDefNameId;
  }

  /**
   * Set attribute def id for the owner
   * @param ownerAttributeDefId
   */
  public void setOwnerAttributeDefId(String ownerAttributeDefId) {
    this.ownerAttributeDefNameId = ownerAttributeDefId;
  }

  /**
   * @return member attribute def name id for the child
   */
  public String getMemberAttributeDefNameId() {
    return this.memberAttributeDefNameId;
  }

  
  /**
   * Set attribute def name id for the child
   * @param memberAttributeDefNameId
   */
  public void setMemberAttributeDefNameId(String memberAttributeDefNameId) {
    this.memberAttributeDefNameId = memberAttributeDefNameId;
  }
  
  
  /**
   * @return membership type (immediate, effective, or composite)
   */
  public AttributeDefAssignmentType getType() {
    return this.type;
  }

  /**
   * get string value of type for hibernate
   * @return type
   */
  public String getTypeDb() {
    return this.type == null ? null : this.type.name();
  }
  
  /**
   * set group set assignment type
   * @param type1
   */
  public void setType(AttributeDefAssignmentType type1) {
    this.type = type1;
  }

  /**
   * set group set assignment type
   * @param type1
   */
  public void setTypeDb(String type1) {
    this.type = AttributeDefAssignmentType.valueOfIgnoreCase(type1, false);
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

}
