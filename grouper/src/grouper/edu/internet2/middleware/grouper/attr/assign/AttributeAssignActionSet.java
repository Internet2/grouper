package edu.internet2.middleware.grouper.attr.assign;

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
 * @author mchyzer $Id: AttributeAssignActionSet.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class AttributeAssignActionSet extends GrouperAPI 
    implements Hib3GrouperVersioned, GrouperSet {
  
  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(AttributeAssignActionSet.class);

  /** name of the groups attribute assign action set */
  public static final String TABLE_GROUPER_ATTR_ASSIGN_ACTION_SET = "grouper_attr_assign_action_set";

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
  public static final String COLUMN_IF_HAS_ATTR_ASSN_ACTION_ID = "if_has_attr_assn_action_id";

  /** column */
  public static final String COLUMN_THEN_HAS_ATTR_ASSN_ACTION_ID = "then_has_attr_assn_action_id";

  /** column */
  public static final String COLUMN_PARENT_ATTR_ASSN_ACTION_ID = "parent_attr_assn_action_id";

  /** column */
  public static final String COLUMN_TYPE = "type";



  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: contextId */
  public static final String FIELD_CONTEXT_ID = "contextId";

  /** constant for field name for: createdOnDb */
  public static final String FIELD_CREATED_ON_DB = "createdOnDb";

  /** constant for field name for: depth */
  public static final String FIELD_DEPTH = "depth";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: ifHasAttributeAssignActionId */
  public static final String FIELD_IF_HAS_ATTR_ASSN_ACTION_ID = "ifHasAttrAssnActionId";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: parentAttrAssignActionSetId */
  public static final String FIELD_PARENT_ATTR_ASSIGN_ACTION_SET_ID = "parentAttrAssignActionSetId";

  /** constant for field name for: thenHasAttributeAssignActionId */
  public static final String FIELD_THEN_HAS_ATTR_ASSN_ACTION_ID = "thenHasAttrAssnActionId";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_ID, FIELD_IF_HAS_ATTR_ASSN_ACTION_ID,  FIELD_LAST_UPDATED_DB, 
      FIELD_THEN_HAS_ATTR_ASSN_ACTION_ID, FIELD_TYPE);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_IF_HAS_ATTR_ASSN_ACTION_ID,  
      FIELD_LAST_UPDATED_DB, FIELD_THEN_HAS_ATTR_ASSN_ACTION_ID, FIELD_TYPE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** membership type -- immediate, or effective */
  private AttributeAssignActionType type = AttributeAssignActionType.immediate;

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   */
  private String parentAttrAssignActionSetId;

  /** attribute def name id of the parent */
  private String thenHasAttrAssignActionId;
  
  /** attribute def name id of the child */
  private String ifHasAttrAssignActionId;

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
  public static AttributeAssignActionSet findInCollection(
      Collection<AttributeAssignActionSet> attributeDefNameSets, String ifHasId, 
      String thenHasId, int depth, boolean exceptionIfNull) {

    //are we sure we are getting the right one here???
    for (AttributeAssignActionSet attributeDefNameSet : GrouperUtil.nonNull(attributeDefNameSets)) {
      if (StringUtils.equals(ifHasId, attributeDefNameSet.getIfHasAttrAssignActionId())
          && StringUtils.equals(thenHasId, attributeDefNameSet.getThenHasAttrAssignActionId())
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
    
    if (!(other instanceof AttributeAssignActionSet)) {
      return false;
    }
    
    AttributeAssignActionSet that = (AttributeAssignActionSet) other;
    return new EqualsBuilder()
      .append(this.parentAttrAssignActionSetId, that.parentAttrAssignActionSetId)
      .append(this.thenHasAttrAssignActionId, that.thenHasAttrAssignActionId)
      .append(this.ifHasAttrAssignActionId, that.ifHasAttrAssignActionId)
      .isEquals();

  }
  
  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.parentAttrAssignActionSetId)
      .append(this.thenHasAttrAssignActionId)
      .append(this.ifHasAttrAssignActionId)
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
  public AttributeAssignActionSet getParentAttributeDefSet() {
    if (this.depth == 0) {
      return this;
    }
    
    AttributeAssignActionSet parent = GrouperDAOFactory.getFactory().getAttributeAssignActionSet()
      .findById(this.getParentAttrAssignActionSetId(), true) ;
    return parent;
  }
  
  /**
   * @return the parent group set or null if none
   */
  public AttributeAssignAction getIfHasAttributeAssignAction() {
    AttributeAssignAction ifHasAttributeAssignAction = 
      GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(this.getIfHasAttrAssignActionId(), true) ;
    return ifHasAttributeAssignAction;
  }
  
  /**
   * @return the parent group set or null if none
   */
  public AttributeAssignAction getThenHasAttributeAssignAction() {
    AttributeAssignAction thenHasAttributeAssignAction = 
      GrouperDAOFactory.getFactory().getAttributeAssignAction()
      .findById(this.getThenHasAttrAssignActionId(), true) ;
    return thenHasAttributeAssignAction;
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
  public String getParentAttrAssignActionSetId() {
    return parentAttrAssignActionSetId;
  }

  
  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   * @param parentId1
   */
  public void setParentAttrAssignActionSetId(String parentId1) {
    this.parentAttrAssignActionSetId = parentId1;
  }

  
  /**
   * @return attribute def id for the owner
   */
  public String getThenHasAttrAssignActionId() {
    return this.thenHasAttrAssignActionId;
  }

  /**
   * Set attribute def id for the owner
   * @param ownerAttributeDefId
   */
  public void setThenHasAttrAssignActionId(String ownerAttributeDefId) {
    this.thenHasAttrAssignActionId = ownerAttributeDefId;
  }

  /**
   * @return member attribute def name id for the child
   */
  public String getIfHasAttrAssignActionId() {
    return this.ifHasAttrAssignActionId;
  }

  
  /**
   * Set attribute def name id for the child
   * @param memberAttributeAssignActionId
   */
  public void setIfHasAttrAssignActionId(String memberAttributeAssignActionId) {
    this.ifHasAttrAssignActionId = memberAttributeAssignActionId;
  }
  
  
  /**
   * @return membership type (immediate, effective, or composite)
   */
  public AttributeAssignActionType getType() {
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
  public void setType(AttributeAssignActionType type1) {
    this.type = type1;
  }

  /**
   * set group set assignment type
   * @param type1
   */
  public void setTypeDb(String type1) {
    this.type = AttributeAssignActionType.valueOfIgnoreCase(type1, false);
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
    GrouperDAOFactory.getFactory().getAttributeAssignActionSet().saveOrUpdate(this);
  }

  /**
   * delete this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getAttributeAssignActionSet().delete(this);
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
    return this.getIfHasAttrAssignActionId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElementId()
   */
  public String __getThenHasElementId() {
    return this.getThenHasAttrAssignActionId();
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
    return this.getIfHasAttributeAssignAction();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElement()
   */
  public GrouperSetElement __getThenHasElement() {
    return this.getThenHasAttributeAssignAction();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__setParentGrouperSetId(java.lang.String)
   */
  public void __setParentGrouperSetId(String grouperSetId) {
    this.setParentAttrAssignActionSetId(grouperSetId);
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
    return this.getParentAttrAssignActionSetId();
  }

}
