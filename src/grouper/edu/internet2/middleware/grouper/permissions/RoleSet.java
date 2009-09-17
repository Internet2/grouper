package edu.internet2.middleware.grouper.permissions;

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
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author mchyzer $Id: RoleSet.java,v 1.1 2009-09-17 04:19:15 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class RoleSet extends GrouperAPI 
    implements Hib3GrouperVersioned, GrouperSet, GrouperHasContext {
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    this.createdOnDb = System.currentTimeMillis();
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    this.lastUpdatedDb = System.currentTimeMillis();
  }

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(RoleSet.class);

  /** name of the groups role table in the db */
  public static final String TABLE_GROUPER_ROLE_SET = "grouper_role_set";

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
  public static final String COLUMN_IF_HAS_ROLE_ID = "if_has_role_id";

  /** column */
  public static final String COLUMN_THEN_HAS_ROLE_ID = "then_has_role_id";

  /** column */
  public static final String COLUMN_PARENT_ROLE_SET_ID = "parent_role_set_id";

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

  /** constant for field name for: ifHasRoleId */
  public static final String FIELD_IF_HAS_ATTRIBUTE_DEF_NAME_ID = "ifHasRoleId";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: thenHasRoleId */
  public static final String FIELD_THEN_HAS_ATTRIBUTE_DEF_NAME_ID = "thenHasRoleId";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /**
   * fields which are included in db version
   */
  @SuppressWarnings("unused")
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_ID, FIELD_IF_HAS_ATTRIBUTE_DEF_NAME_ID,  FIELD_LAST_UPDATED_DB, 
      FIELD_THEN_HAS_ATTRIBUTE_DEF_NAME_ID, FIELD_TYPE);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_IF_HAS_ATTRIBUTE_DEF_NAME_ID,  
      FIELD_LAST_UPDATED_DB, FIELD_THEN_HAS_ATTRIBUTE_DEF_NAME_ID, FIELD_TYPE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** id of this type */
  private String id;
  
  /** context id ties multiple db changes */
  private String contextId;
  
  /** membership type -- immediate, or effective */
  private RoleHierarchyType type = RoleHierarchyType.immediate;

  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   */
  private String parentRoleSetId;

  /** role id of the parent */
  private String thenHasRoleId;
  
  /** role id of the child */
  private String ifHasRoleId;

  /**
   * depth - 0 for self records, 1 for immediate memberships, > 1 for effective 
   */
  private int depth;

  /**
   * time in millis when this role set was created
   */
  private Long createdOnDb;

  /**
   * time in millis when this role set was last modified
   */
  private Long lastUpdatedDb;

  /**
   * find role set, better be here
   * @param roleSets 
   * @param ifHasId 
   * @param thenHasId 
   * @param depth is the depth expecting
   * @param exceptionIfNull 
   * @return the def name set
   */
  public static RoleSet findInCollection(
      Collection<RoleSet> roleSets, String ifHasId, 
      String thenHasId, int depth, boolean exceptionIfNull) {

    //are we sure we are getting the right one here???
    for (RoleSet roleSet : GrouperUtil.nonNull(roleSets)) {
      if (StringUtils.equals(ifHasId, roleSet.getIfHasRoleId())
          && StringUtils.equals(thenHasId, roleSet.getThenHasRoleId())
          && depth == roleSet.getDepth()) {
        return roleSet;
      }
    }
    if (exceptionIfNull) {
      throw new RuntimeException("Cant find role set with id: " + ifHasId + ", " + thenHasId + ", " + depth);
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
    
    if (!(other instanceof RoleSet)) {
      return false;
    }
    
    RoleSet that = (RoleSet) other;
    return new EqualsBuilder()
      .append(this.parentRoleSetId, that.parentRoleSetId)
      .append(this.thenHasRoleId, that.thenHasRoleId)
      .append(this.ifHasRoleId, that.ifHasRoleId)
      .isEquals();

  }
  
  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.parentRoleSetId)
      .append(this.thenHasRoleId)
      .append(this.ifHasRoleId)
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
  public RoleSet getParentRoleSet() {
    if (this.depth == 0) {
      return this;
    }
    
    RoleSet parent = GrouperDAOFactory.getFactory().getRoleSet()
      .findById(this.getParentRoleSetId(), true) ;
    return parent;
  }
  
  /**
   * @return the parent group set or null if none
   */
  public Role getIfHasRole() {
    Role ifHasRole = 
      GrouperDAOFactory.getFactory().getRole()
        .findById(this.getIfHasRoleId(), true) ;
    return ifHasRole;
  }
  
  /**
   * @return the parent group set or null if none
   */
  public Role getThenHasRole() {
    Role thenHasRole = 
      GrouperDAOFactory.getFactory().getRole()
      .findById(this.getThenHasRoleId(), true) ;
    return thenHasRole;
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
  public String getParentRoleSetId() {
    return parentRoleSetId;
  }

  
  /**
   * for self, or immediate, just use this id.
   * for effective, this is the first hop on the directed graph
   * to get to this membership. 
   * @param parentId1
   */
  public void setParentRoleSetId(String parentId1) {
    this.parentRoleSetId = parentId1;
  }

  
  /**
   * @return role id for then
   */
  public String getThenHasRoleId() {
    return this.thenHasRoleId;
  }

  /**
   * Set role id for the then
   * @param thenHasRoleId
   */
  public void setThenHasRoleId(String thenHasRoleId) {
    this.thenHasRoleId = thenHasRoleId;
  }

  /**
   * @return member role id for the child
   */
  public String getIfHasRoleId() {
    return this.ifHasRoleId;
  }

  
  /**
   * Set role id for the child
   * @param memberRoleId
   */
  public void setIfHasRoleId(String memberRoleId) {
    this.ifHasRoleId = memberRoleId;
  }
  
  
  /**
   * @return membership type (immediate, effective, or composite)
   */
  public RoleHierarchyType getType() {
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
  public void setType(RoleHierarchyType type1) {
    this.type = type1;
  }

  /**
   * set group set assignment type
   * @param type1
   */
  public void setTypeDb(String type1) {
    this.type = RoleHierarchyType.valueOfIgnoreCase(type1, false);
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
    GrouperDAOFactory.getFactory().getRoleSet().saveOrUpdate(this);
  }

  /**
   * save or update this object
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getRoleSet().delete(this);
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
    return this.getIfHasRoleId();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElementId()
   */
  public String __getThenHasElementId() {
    return this.getThenHasRoleId();
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
    return this.getIfHasRole();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getThenHasElement()
   */
  public GrouperSetElement __getThenHasElement() {
    return this.getThenHasRole();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__setParentGrouperSetId(java.lang.String)
   */
  public void __setParentGrouperSetId(String grouperSetId) {
    this.setParentRoleSetId(grouperSetId);
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getParentGrouperSet()
   */
  public GrouperSet __getParentGrouperSet() {
    return this.getParentRoleSet();
  }

  /**
   * @see edu.internet2.middleware.grouper.grouperSet.GrouperSet#__getParentGrouperSetId()
   */
  public String __getParentGrouperSetId() {
    return this.getParentRoleSetId();
  }

}
