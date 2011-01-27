package edu.internet2.middleware.grouper.permissions.role;

import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.GrouperAPI;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.grouperSet.GrouperSet;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetElement;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportRoleSet;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;

/**
 * @author mchyzer $Id: RoleSet.java,v 1.1 2009-10-02 05:57:58 mchyzer Exp $
 */
@SuppressWarnings("serial")
public class RoleSet extends GrouperAPI 
    implements Hib3GrouperVersioned, GrouperSet, GrouperHasContext, XmlImportable<RoleSet> {
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    if (this.createdOnDb == null) {
      this.createdOnDb = System.currentTimeMillis();
    }
    
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ROLE_SET_ADD, 
        ChangeLogLabels.ROLE_SET_ADD.id.name(), this.getId(), 
        ChangeLogLabels.ROLE_SET_ADD.type.name(), this.getTypeDb(),
        ChangeLogLabels.ROLE_SET_ADD.ifHasRoleId.name(), this.getIfHasRoleId(), 
        ChangeLogLabels.ROLE_SET_ADD.thenHasRoleId.name(), this.getThenHasRoleId(),
        ChangeLogLabels.ROLE_SET_ADD.parentRoleSetId.name(), this.getParentRoleSetId(), 
        ChangeLogLabels.ROLE_SET_ADD.depth.name(), "" + this.getDepth()).save();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.ROLE_SET_DELETE, 
        ChangeLogLabels.ROLE_SET_DELETE.id.name(), this.getId(), 
        ChangeLogLabels.ROLE_SET_DELETE.type.name(), this.getTypeDb(),
        ChangeLogLabels.ROLE_SET_DELETE.ifHasRoleId.name(), this.getIfHasRoleId(), 
        ChangeLogLabels.ROLE_SET_DELETE.thenHasRoleId.name(), this.getThenHasRoleId(),
        ChangeLogLabels.ROLE_SET_DELETE.parentRoleSetId.name(), this.getParentRoleSetId(), 
        ChangeLogLabels.ROLE_SET_DELETE.depth.name(), "" + this.getDepth()).save();
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    this.lastUpdatedDb = System.currentTimeMillis();
    
    if (this.dbVersionDifferentFields().contains(FIELD_DEPTH)) {
      throw new RuntimeException("cannot update depth");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_IF_HAS_ROLE_ID)) {
      throw new RuntimeException("cannot update ifHasRoleId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_THEN_HAS_ROLE_ID)) {
      throw new RuntimeException("cannot update thenHasRoleId");
    }
    
    if (this.dbVersionDifferentFields().contains(FIELD_PARENT_ROLE_SET_ID) && parentRoleSetId != null) {
      throw new RuntimeException("cannot update parentRoleSetId");
    }
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
  public static final String FIELD_IF_HAS_ROLE_ID = "ifHasRoleId";

  /** constant for field name for: lastUpdatedDb */
  public static final String FIELD_LAST_UPDATED_DB = "lastUpdatedDb";

  /** constant for field name for: thenHasRoleId */
  public static final String FIELD_THEN_HAS_ROLE_ID = "thenHasRoleId";
  
  /** constant for field name for: parentRoleSetId */
  public static final String FIELD_PARENT_ROLE_SET_ID = "parentRoleSetId";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_ID, FIELD_IF_HAS_ROLE_ID,  FIELD_LAST_UPDATED_DB, 
      FIELD_THEN_HAS_ROLE_ID, FIELD_TYPE, FIELD_PARENT_ROLE_SET_ID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CONTEXT_ID, FIELD_CREATED_ON_DB, FIELD_DEPTH,  
      FIELD_HIBERNATE_VERSION_NUMBER, FIELD_ID, FIELD_IF_HAS_ROLE_ID,  
      FIELD_LAST_UPDATED_DB, FIELD_THEN_HAS_ROLE_ID, FIELD_TYPE);

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

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(RoleSet existingRecord) {
    existingRecord.setDepth(this.depth);
    existingRecord.setId(this.id);
    existingRecord.setIfHasRoleId(this.ifHasRoleId);
    existingRecord.setThenHasRoleId(this.thenHasRoleId);
    existingRecord.setType(this.type);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(RoleSet other) {
    if (this.depth != other.depth) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!StringUtils.equals(this.ifHasRoleId, other.ifHasRoleId)) {
      return true;
    }
    if (!StringUtils.equals(this.thenHasRoleId, other.thenHasRoleId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.type, other.type)) {
      return true;
    }

    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(RoleSet other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.createdOnDb, other.createdOnDb)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    if (!GrouperUtil.equals(this.lastUpdatedDb, other.lastUpdatedDb)) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public RoleSet xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getRoleSet().findByUuidOrKey(this.id, this.ifHasRoleId, this.thenHasRoleId, this.parentRoleSetId, this.depth, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public RoleSet xmlSaveBusinessProperties(RoleSet existingRecord) {

    //if its an insert, call the business method
    if (existingRecord == null) {
      
      if (this.depth != 1) {
        throw new RuntimeException("Why are we doing a depth not equal to 1????");
      }
      
      Role ifHasRole = GrouperDAOFactory.getFactory().getRole().findById(this.ifHasRoleId, true);
      Role thenHasRole = GrouperDAOFactory.getFactory().getRole().findById(this.thenHasRoleId, true);
      
      ifHasRole.getRoleInheritanceDelegate().internal_addRoleToInheritFromThis(thenHasRole, this.id);
      existingRecord = GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(
          this.ifHasRoleId, this.thenHasRoleId, true);
    }
    
    //basically the set should not be updated.... the id's wont match since the self referential records arent
    //exported/imported
    
    //    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //    //if its an insert or update, then do the rest of the fields
    //    existingRecord.saveOrUpdate();
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getRoleSet().saveUpdateProperties(this);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportRoleSet xmlToExportRoleSet(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportRoleSet xmlExportRoleSet = new XmlExportRoleSet();
    
    xmlExportRoleSet.setContextId(this.getContextId());
    xmlExportRoleSet.setCreateTime(GrouperUtil.dateStringValue(this.getCreatedOnDb()));
    xmlExportRoleSet.setDepth(this.getDepth());
    xmlExportRoleSet.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportRoleSet.setIfHasRoleId(this.getIfHasRoleId());
    xmlExportRoleSet.setModifierTime(GrouperUtil.dateStringValue(this.getLastUpdatedDb()));
    xmlExportRoleSet.setThenHasRoleId(this.getThenHasRoleId());
    xmlExportRoleSet.setType(this.getTypeDb());
    xmlExportRoleSet.setUuid(this.getId());
    
    return xmlExportRoleSet;
    
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlGetId()
   */
  public String xmlGetId() {
    return this.getId();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setId(theId);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("RoleSet: " + this.getId());

//    XmlExportUtils.toStringRoleSet(stringWriter, this, false);
    
    return stringWriter.toString();
    
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public RoleSet dbVersion() {
    return (RoleSet)this.dbVersion;
  }
  
  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }


  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#dbVersionDifferentFields()
   */
  @Override
  public Set<String> dbVersionDifferentFields() {
    if (this.dbVersion == null) {
      throw new RuntimeException("State was never stored from db");
    }
    //easier to unit test if everything is ordered
    Set<String> result = GrouperUtil.compareObjectFields(this, this.dbVersion,
        DB_VERSION_FIELDS, null);
    return result;
  }
}
