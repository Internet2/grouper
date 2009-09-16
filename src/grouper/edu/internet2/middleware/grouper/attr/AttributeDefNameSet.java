package edu.internet2.middleware.grouper.attr;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.grouperSet.GrouperSet;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;

//select gg.name, gadn.name
//from grouper_attribute_assign gaa, grouper_attribute_def_name_set gadns, grouper_groups gg, 
//grouper_attribute_def_name gadn
//where gaa.owner_group_id = gg.id and gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id
//and gadn.id = gadns.then_has_attribute_def_name_id


//select gaa.id attribute_assign_id, gaa.owner_group_id, gaa.owner_membership_id, gadn.name, gadn.id attribute_def_name_id
//from grouper_attribute_assign gaa, grouper_attribute_def_name_set gadns,
//grouper_attribute_def_name gadn
//where gaa.attribute_def_name_id = gadns.if_has_attribute_def_name_id
//and gadn.id = gadns.then_has_attribute_def_name_id;

/**
 * @author mchyzer $Id: AttributeDefNameSet.java,v 1.5 2009-09-16 08:52:22 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class AttributeDefNameSet extends GrouperAPI 
    implements Hib3GrouperVersioned, GrouperSet {
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeAssignValue.class);

  /** name of the groups attribute def table in the db */
  public static final String TABLE_GROUPER_ATTRIBUTE_DEF_NAME_SET = "grouper_attribute_def_name_set";

  /** column */
  public static final String COLUMN_CONTEXT_ID = "context_id";

  /** column */
  public static final String COLUMN_CREATED_ON = "created_on";

  /** column */
  public static final String COLUMN_LAST_UPDATED = "last_updated";

  /** column */
  public static final String COLUMN_ID = "id";

  /** column */
  public static final String COLUMN_DEPTH = "depth";

  /** column */
  public static final String COLUMN_IF_HAS_ATTRIBUTE_DEF_NAME_ID = "if_has_attribute_def_name_id";

  /** column */
  public static final String COLUMN_THEN_HAS_ATTRIBUTE_DEF_NAME_ID = "then_has_attribute_def_name_id";

  /** column */
  public static final String COLUMN_PARENT_ATTR_DEF_NAME_SET_ID = "parent_attr_def_name_set_id";

  /** column */
  public static final String COLUMN_TYPE = "type";



  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: firstHopAttrDefNameSetId */
  public static final String FIELD_FIRST_HOP_ATTR_DEF_NAME_SET_ID = "firstHopAttrDefNameSetId";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasAttributeDefNameId */
  public static final String FIELD_IF_HAS_ATTRIBUTE_DEF_NAME_ID = "ifHasAttributeDefNameId";

  /** constant for field name for: lastHopAttrDefNameSetId */
  public static final String FIELD_LAST_HOP_ATTR_DEF_NAME_SET_ID = "lastHopAttrDefNameSetId";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: thenHasAttributeDefNameId */
  public static final String FIELD_THEN_HAS_ATTRIBUTE_DEF_NAME_ID = "thenHasAttributeDefNameId";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH, FIELD_FIRST_HOP_ATTR_DEF_NAME_SET_ID, 
      FIELD_ID, FIELD_IF_HAS_ATTRIBUTE_DEF_NAME_ID, FIELD_LAST_HOP_ATTR_DEF_NAME_SET_ID, FIELD_LAST_UPDATED_DB, 
      FIELD_THEN_HAS_ATTRIBUTE_DEF_NAME_ID, FIELD_TYPE);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH, FIELD_FIRST_HOP_ATTR_DEF_NAME_SET_ID, 
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_IF_HAS_ATTRIBUTE_DEF_NAME_ID, FIELD_LAST_HOP_ATTR_DEF_NAME_SET_ID, 
      FIELD_LAST_UPDATED_DB, FIELD_THEN_HAS_ATTRIBUTE_DEF_NAME_ID, FIELD_TYPE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** membership type -- immediate, or effective */
  private AttributeDefAssignmentType type = AttributeDefAssignmentType.immediate;

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   */
  private String parentAttrDefNameSetId;

  /** attribute def name id of the parent */
  private String thenHasAttributeDefNameId;
  
  /** attribute def name id of the child */
  private String ifHasAttributeDefNameId;

  /**
   * depth - 0 for self records, 1 for immediate memberships, > 1 for effective 
   */
  private int depth;

  /**
   * time in millis when this attribute was created
   */
  private Long createdOnDb;

  /**
   * time in millis when this attribute was last modified
   */
  private Long lastUpdatedDb;

  /**
   * find an attribute def name set, better be here
   * @param attributeDefNameSets 
   * @param ifHasId 
   * @param thenHasId 
   * @param depth is the depth expecting
   * @param exceptionIfNull 
   * @return the def name set
   */
  public static AttributeDefNameSet findInCollection(
      Collection<AttributeDefNameSet> attributeDefNameSets, String ifHasId, 
      String thenHasId, int depth, boolean exceptionIfNull) {

    //are we sure we are getting the right one here???
    for (AttributeDefNameSet attributeDefNameSet : GrouperUtil.nonNull(attributeDefNameSets)) {
      if (StringUtils.equals(ifHasId, attributeDefNameSet.getIfHasAttributeDefNameId())
          && StringUtils.equals(thenHasId, attributeDefNameSet.getThenHasAttributeDefNameId())
          && depth == attributeDefNameSet.getDepth()) {
        return attributeDefNameSet;
      }
    }
    if (exceptionIfNull) {
      throw new RuntimeException("Cant find attribute def name set with id: " + ifHasId + ", " + thenHasId + ", " + depth);
    }
    return null;
  }

  /**
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    
    if (!(other instanceof AttributeDefNameSet)) {
      return false;
    }
    
    AttributeDefNameSet that = (AttributeDefNameSet) other;
    return new EqualsBuilder()
      .append(this.parentAttrDefNameSetId, that.parentAttrDefNameSetId)
      .append(this.thenHasAttributeDefNameId, that.thenHasAttributeDefNameId)
      .append(this.ifHasAttributeDefNameId, that.ifHasAttributeDefNameId)
      .isEquals();

  }
  
  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.parentAttrDefNameSetId)
      .append(this.thenHasAttributeDefNameId)
      .append(this.ifHasAttributeDefNameId)
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
   * @return the parent group set or null if none
   */
  public AttributeDefNameSet getParentAttributeDefSet() {
    if (this.depth == 0) {
      return this;
    }
    
    AttributeDefNameSet parent = GrouperDAOFactory.getFactory().getAttributeDefNameSet()
      .findById(this.getParentAttrDefNameSetId(), true) ;
    return parent;
  }
  
  /**
   * @return the parent group set or null if none
   */
  public AttributeDefName getIfHasAttributeDefName() {
    AttributeDefName ifHasAttributeDefName = 
      GrouperDAOFactory.getFactory().getAttributeDefName()
      .findById(this.getIfHasAttributeDefNameId(), true) ;
    return ifHasAttributeDefName;
  }
  
  /**
   * @return the parent group set or null if none
   */
  public AttributeDefName getThenHasAttributeDefName() {
    AttributeDefName thenHasAttributeDefName = 
      GrouperDAOFactory.getFactory().getAttributeDefName()
      .findById(this.getThenHasAttributeDefNameId(), true) ;
    return thenHasAttributeDefName;
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
   * set context id
   * @param contextId
   */
  public void setContextId(String contextId) {
    this.contextId = contextId;
  }

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   * @return parent id
   */
  public String getParentAttrDefNameSetId() {
    return parentAttrDefNameSetId;
  }

  
  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   * @param parentId1
   */
  public void setParentAttrDefNameSetId(String parentId1) {
    this.parentAttrDefNameSetId = parentId1;
  }

  
  /**
   * @return attribute def id for the owner
   */
  public String getThenHasAttributeDefNameId() {
    return this.thenHasAttributeDefNameId;
  }

  /**
   * Set attribute def id for the owner
   * @param ownerAttributeDefId
   */
  public void setThenHasAttributeDefNameId(String ownerAttributeDefId) {
    this.thenHasAttributeDefNameId = ownerAttributeDefId;
  }

  /**
   * @return member attribute def name id for the child
   */
  public String getIfHasAttributeDefNameId() {
    return this.ifHasAttributeDefNameId;
  }

  
  /**
   * Set attribute def name id for the child
   * @param memberAttributeDefNameId
   */
  public void setIfHasAttributeDefNameId(String memberAttributeDefNameId) {
    this.ifHasAttributeDefNameId = memberAttributeDefNameId;
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

  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * when created
   * @return timestamp
   */
  public Timestamp getCreatedOn() {
    return this.createdOnDb == null ? null : new Timestamp(this.createdOnDb);
  }

  /**
   * when created
   * @return timestamp
   */
  public Long getCreatedOnDb() {
    return this.createdOnDb;
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Timestamp getLastUpdated() {
    return this.lastUpdatedDb == null ? null : new Timestamp(this.lastUpdatedDb);
  }

  /**
   * when last updated
   * @return timestamp
   */
  public Long getLastUpdatedDb() {
    return this.lastUpdatedDb;
  }

  /**
   * save or update this object
   */
  public void saveOrUpdate() {
    GrouperDAOFactory.getFactory().getAttributeDefNameSet().saveOrUpdate(this);
  }

  /**
   * save or update this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getAttributeDefNameSet().delete(this);
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOnDb(Long createdOn1) {
    this.createdOnDb = createdOn1;
  }

  /**
   * when created
   * @param createdOn1
   */
  public void setCreatedOn(Timestamp createdOn1) {
    this.createdOnDb = createdOn1 == null ? null : createdOn1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdated(Timestamp lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1 == null ? null : lastUpdated1.getTime();
  }

  /**
   * when last updated
   * @param lastUpdated1
   */
  public void setLastUpdatedDb(Long lastUpdated1) {
    this.lastUpdatedDb = lastUpdated1;
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getId()
   */
  public String __getId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getIfHasElementId()
   */
  public String __getIfHasElementId() {
    return this.getIfHasAttributeDefNameId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElementId()
   */
  public String __getThenHasElementId() {
    return this.getThenHasAttributeDefNameId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getDepth()
   */
  public int __getDepth() {
    return this.getDepth();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getIfHasElement()
   */
  public GrouperSetElement __getIfHasElement() {
    return this.getIfHasAttributeDefName();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElement()
   */
  public GrouperSetElement __getThenHasElement() {
    return this.getThenHasAttributeDefName();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__setParentGrouperSetId(java.lang.String)
   */
  public void __setParentGrouperSetId(String grouperSetId) {
    this.setParentAttrDefNameSetId(grouperSetId);
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getParentGrouperSet()
   */
  public GrouperSet __getParentGrouperSet() {
    return this.getParentAttributeDefSet();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getParentGrouperSetId()
   */
  public String __getParentGrouperSetId() {
    return this.getParentAttrDefNameSetId();
  }

}
