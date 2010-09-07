/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupTypeTupleHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportGroupTypeTuple;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;

/**
 * Basic Hibernate <code>Group</code> and <code>GroupType</code> tuple DTO implementation.
 * @author  blair christensen.
 * @version $Id: GroupTypeTuple.java,v 1.12 2009-09-24 18:07:16 shilen Exp $
 * @since   @HEAD@
 */
@SuppressWarnings("serial")
public class GroupTypeTuple extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, XmlImportable<GroupTypeTuple> {

  /**
   * 
   */
  public static final String TABLE_GROUPER_GROUPS_TYPES = "grouper_groups_types";

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: groupUUID */
  public static final String FIELD_GROUP_UUID = "groupUUID";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: typeUUID */
  public static final String FIELD_TYPE_UUID = "typeUUID";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_GROUP_UUID, FIELD_ID, FIELD_TYPE_UUID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DB_VERSION, FIELD_GROUP_UUID, FIELD_ID, FIELD_TYPE_UUID, FIELD_HIBERNATE_VERSION_NUMBER);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** */
  private String  groupUUID;
  
  /** */
  private String  id;

  /** */
  private String  typeUUID;

  /** context id of the transaction */
  private String contextId;

  /** store a reference to the group for hooks or whatnot */
  private Group group;
  
  /**
   * context id of the transaction
   * @return context id
   */
  public String getContextId() {
    return this.contextId;
  }

  /**
   * context id of the transaction
   * @param contextId1
   */
  public void setContextId(String contextId1) {
    this.contextId = contextId1;
  }

  /**
   * try to get the current group if it is available (if this object
   * is cloned, then it might be null)
   * @param retrieveIfNull true to get from DB if null
   * @return the current group
   */
  public Group retrieveGroup(boolean retrieveIfNull) {
    if (retrieveIfNull && this.group==null) {
      this.group = GroupFinder.findByUuid(
          GrouperSession.staticGrouperSession(), this.groupUUID, true);
    }
    return this.group;
  }


  // PUBLIC CLASS METHODS //

  /**
   * @param other 
   * @return if equals
   * @since   @HEAD@
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GroupTypeTuple)) {
      return false;
    }
    GroupTypeTuple that = (GroupTypeTuple) other;
    return new EqualsBuilder()
      .append( this.groupUUID, that.groupUUID )
      .append( this.typeUUID,  that.typeUUID  )
      .isEquals();
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.groupUUID )
      .append( this.typeUUID  )
      .toHashCode();
  }

  /**
   * 
   * @see java.lang.Object#toString()
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "groupUuid", this.getGroupUuid() )
      .append( "typeUuid",  this.getTypeUuid()  )
      .toString();
  }


  /**
   * 
   * @return uuid
   */
  public String getGroupUuid() {
    return this.groupUUID;
  }
  
  /**
   * 
   * @return id
   */
  public String getId() {
    return this.id;
  }
  
  /**
   * type uuid
   * @return uuid
   */
  public String getTypeUuid() {
    return this.typeUUID;
  }


  /**
   * 
   * @param groupUUID1
   */
  public void setGroupUuid(String groupUUID1) {
    this.assignGroupUuid(groupUUID1, null);
  }

  /**
   * 
   * @param groupUUID1
   * @param group1 
   */
  public void assignGroupUuid(String groupUUID1, Group group1) {
    this.groupUUID = groupUUID1;
    
    //see if we need to wipe out to null
    if (group1 == null && this.group != null 
        && StringUtils.equals(this.group.getUuid(), groupUUID1)) {
      group1 = this.group;
    }

    this.group = group1;
  }

  /**
   * 
   * @param id1
   */
  public void setId(String id1) {
    this.id = id1;
  }
  
  /**
   * 
   * @param typeUUID1
   * @return tuple
   */
  public GroupTypeTuple setTypeUuid(String typeUUID1) {
    this.typeUUID = typeUUID1;
    return this;
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {

    super.onPostDelete(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE_TUPLE, 
        GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_COMMIT_DELETE, HooksGroupTypeTupleBean.class, 
        this, GroupTypeTuple.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE_TUPLE, 
        GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_DELETE, HooksGroupTypeTupleBean.class, 
        this, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_POST_DELETE, false, true);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {

    super.onPostSave(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE_TUPLE, 
        GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_INSERT, HooksGroupTypeTupleBean.class, 
        this, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_POST_INSERT, true, false);

    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE_TUPLE, 
        GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_COMMIT_INSERT, HooksGroupTypeTupleBean.class, 
        this, GroupTypeTuple.class);

  }

  /**
   * delete this record
   */
  public void delete() {
    GrouperDAOFactory.getFactory().getGroupTypeTuple().delete(this);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {

    super.onPostUpdate(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.GROUP_TYPE_TUPLE, 
        GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_COMMIT_UPDATE, HooksGroupTypeTupleBean.class, 
        this, GroupTypeTuple.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE_TUPLE, 
        GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_POST_UPDATE, HooksGroupTypeTupleBean.class, 
        this, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_POST_UPDATE, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    
    // remove group sets
    Set<Field> fields = FieldFinder.findAllByGroupType(this.getTypeUuid());
    Iterator<Field> iter = fields.iterator();
    
    while (iter.hasNext()) {
      Field field = iter.next();
      if (field.isGroupListField()) {
        GrouperDAOFactory.getFactory().getGroupSet().deleteSelfByOwnerGroupAndField(this.getGroupUuid(), field.getUuid());
      }
    }
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE_TUPLE, 
        GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_PRE_DELETE, HooksGroupTypeTupleBean.class, 
        this, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_PRE_DELETE, false, false);
  
    GroupType groupType = GroupTypeFinder.findByUuid(this.getTypeUuid(), true);
    
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_TYPE_UNASSIGN, 
        ChangeLogLabels.GROUP_TYPE_UNASSIGN.id.name(), this.getId(), 
        ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupId.name(), this.getGroupUuid(), 
        ChangeLogLabels.GROUP_TYPE_UNASSIGN.groupName.name(), this.retrieveGroup(true).getName(),
        ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeId.name(), this.getTypeUuid(),
        ChangeLogLabels.GROUP_TYPE_UNASSIGN.typeName.name(), groupType.getName()).save();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
      
    // add group sets
    Set<Field> fields = FieldFinder.findAllByGroupType(this.getTypeUuid());
    Iterator<Field> iter = fields.iterator();
    
    while (iter.hasNext()) {
      Field field = iter.next();
      
      if (field.isGroupListField()) {
        GroupSet groupSet = new GroupSet();
        groupSet.setId(GrouperUuid.getUuid());
        groupSet.setCreatorId(GrouperSession.staticGrouperSession().getMemberUuid());
        groupSet.setDepth(0);
        groupSet.setMemberGroupId(this.getGroupUuid());
        groupSet.setOwnerGroupId(this.getGroupUuid());
        groupSet.setParentId(groupSet.getId());
        groupSet.setFieldId(field.getUuid());
        GrouperDAOFactory.getFactory().getGroupSet().save(groupSet);
      }
    }
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE_TUPLE, 
        GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_PRE_INSERT, HooksGroupTypeTupleBean.class, 
        this, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_PRE_INSERT, false, false);

    GroupType groupType = GroupTypeFinder.findByUuid(this.getTypeUuid(), true);
    
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_TYPE_ASSIGN, 
        ChangeLogLabels.GROUP_TYPE_ASSIGN.id.name(), this.getId(), 
        ChangeLogLabels.GROUP_TYPE_ASSIGN.groupId.name(), this.getGroupUuid(), 
        ChangeLogLabels.GROUP_TYPE_ASSIGN.groupName.name(), this.retrieveGroup(true).getName(),
        ChangeLogLabels.GROUP_TYPE_ASSIGN.typeId.name(), this.getTypeUuid(),
        ChangeLogLabels.GROUP_TYPE_ASSIGN.typeName.name(), groupType.getName()).save();
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE_TUPLE, 
        GroupTypeTupleHooks.METHOD_GROUP_TYPE_TUPLE_PRE_UPDATE, HooksGroupTypeTupleBean.class, 
        this, GroupTypeTuple.class, VetoTypeGrouper.GROUP_TYPE_TUPLE_PRE_UPDATE, false, false);
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public GroupTypeTuple dbVersion() {
    return (GroupTypeTuple)this.dbVersion;
  }

  /**
   * note, these are massaged so that name, extension, etc look like normal fields.
   * access with fieldValue()
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

  /**
   * take a snapshot of the data since this is what is in the db
   */
  @Override
  public void dbVersionReset() {
    //lets get the state from the db so we know what has changed
    this.dbVersion = GrouperUtil.clone(this, DB_VERSION_FIELDS);
  }

  /**
   * deep clone the fields in this object
   */
  @Override
  public GroupTypeTuple clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * store this object to the DB.
   */
  public void store() {    
    GrouperDAOFactory.getFactory().getGroupTypeTuple().update(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(GroupTypeTuple existingRecord) {
    existingRecord.setGroupUuid(this.getGroupUuid());
    existingRecord.setId(this.getId());
    existingRecord.setTypeUuid(this.getTypeUuid());

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(GroupTypeTuple other) {
    if (!StringUtils.equals(this.groupUUID, other.groupUUID)) {
      return true;
    }
    if (!StringUtils.equals(this.id, other.id)) {
      return true;
    }
    if (!StringUtils.equals(this.typeUUID, other.typeUUID)) {
      return true;
    }

    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(GroupTypeTuple other) {
    if (!StringUtils.equals(this.contextId, other.contextId)) {
      return true;
    }
    if (!GrouperUtil.equals(this.getHibernateVersionNumber(), other.getHibernateVersionNumber())) {
      return true;
    }
    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlRetrieveByIdOrKey()
   */
  public GroupTypeTuple xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getGroupTypeTuple().findByUuidOrKey(this.id, this.groupUUID, this.typeUUID, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public GroupTypeTuple xmlSaveBusinessProperties(GroupTypeTuple existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      existingRecord = this.clone();
      GrouperDAOFactory.getFactory().getGroupTypeTuple().save(existingRecord);
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.store();
    return existingRecord;

  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getGroupTypeTuple().saveUpdateProperties(this);
  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportGroupTypeTuple xmlToExportGroup(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }
    
    XmlExportGroupTypeTuple xmlExportGroupTypeTuple = new XmlExportGroupTypeTuple();
    
    xmlExportGroupTypeTuple.setContextId(this.getContextId());
    xmlExportGroupTypeTuple.setGroupId(this.getGroupUuid());
    xmlExportGroupTypeTuple.setHibernateVersionNumber(this.getHibernateVersionNumber());
    xmlExportGroupTypeTuple.setTypeId(this.getTypeUuid());
    xmlExportGroupTypeTuple.setUuid(this.getId());
    return xmlExportGroupTypeTuple;
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
    
    stringWriter.write("GroupTypeTuple: " + this.getId() + ", ");

//    XmlExportUtils.toStringGroupTypeTuple(stringWriter, this, false);
    
    return stringWriter.toString();
    
  }

} 

