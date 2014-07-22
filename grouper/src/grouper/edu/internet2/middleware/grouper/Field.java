/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.FieldHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksFieldBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.AddFieldToGroupTypeValidator;
import edu.internet2.middleware.grouper.xml.export.XmlExportField;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;


/** 
 * Schema specification for a Group attribute or list.
 * Reference to members list is: Group.getDefaultList()
 * <p/>
 * @author  blair christensen.
 * @version $Id: Field.java,v 1.48 2009-09-24 18:07:16 shilen Exp $    
 */
public class Field extends GrouperAPI implements Comparable<Field>, GrouperHasContext, Hib3GrouperVersioned, XmlImportable<Field> {

  /**
   * get the fields that this field implies by inheritance
   * @return the fields, note, dont change the list after you get it
   */
  public Collection<Field> getImpliedFields() {

    if (this.isPrivilege()) {
      Privilege privilege = Privilege.listToPriv(this.name, true);
      return Privilege.convertPrivilegesToFields(privilege.getImpliedPrivileges());
    }
    
    throw new RuntimeException("Not expecting field: " + this.name);
  }

  /**
   * 
   * @return if this field is a privilege
   */
  public boolean isPrivilege() {
    
    if (this.isAttributeDefListField() || this.isGroupAccessField() || this.isStemListField()) {
      return true;
    }
    return false;
  }

  /**
   * return the uuid
   * @return uuid
   */
  public String getId() {
    return this.getUuid();
  }
  
  /**
   * see if there are inherited privileges to also include
   * @return the inherited fields
   */
  public static Collection<Field> calculateInheritedPrivileges(Collection<Field> fields, boolean includeInheritedPrivileges) {
    if (!includeInheritedPrivileges || GrouperUtil.length(fields) == 0) {
      return fields;
    }
    
    Set<Field> additionalFields = new HashSet<Field>();
    
    for (Field field : GrouperUtil.nonNull(fields)) {
      
      if (field.isAttributeDefListField() || field.isGroupAccessField() || field.isStemListField()) {
        Privilege privilege = Privilege.listToPriv(field.getName(), true);
        Collection<Privilege> privileges = privilege.getInheritedPrivileges();
        Collection<Field> theFields = Privilege.convertPrivilegesToFields(privileges);
        additionalFields.addAll(theFields);
      }
      
    }
    
    return additionalFields;
  }

  /** field name for creators */
  public static final String FIELD_NAME_CREATORS = "creators";
  
  /** field name for stemmers */
  public static final String FIELD_NAME_STEMMERS = "stemmers";
  
  /** field name for viewers */
  public static final String FIELD_NAME_VIEWERS = "viewers";
  
  /** field name for attr viewers */
  public static final String FIELD_NAME_ATTR_VIEWERS = "attrViewers";
  
  /** field name for admins */
  public static final String FIELD_NAME_ADMINS = "admins";
  
  /** field name for attr admins */
  public static final String FIELD_NAME_ATTR_ADMINS = "attrAdmins";
  
  /** field name for readers */
  public static final String FIELD_NAME_READERS = "readers";
  
  /** field name for attr readers */
  public static final String FIELD_NAME_ATTR_READERS = "attrReaders";
  
  /** field name for updaters */
  public static final String FIELD_NAME_UPDATERS = "updaters";
  
  /** field name for attr updaters */
  public static final String FIELD_NAME_ATTR_UPDATERS = "attrUpdaters";
  
  /** field name for optins */
  public static final String FIELD_NAME_OPTINS = "optins";
  
  /** field name for attr optins */
  public static final String FIELD_NAME_ATTR_OPTINS = "attrOptins";
  
  /** field name for optouts */
  public static final String FIELD_NAME_OPTOUTS = "optouts";

  /** field name for attr optouts */
  public static final String FIELD_NAME_ATTR_OPTOUTS = "attrOptouts";

  /** field name for groupAttrReaders */
  public static final String FIELD_NAME_GROUP_ATTR_READERS = "groupAttrReaders";
  
  /** field name for groupAttrUpdaters */
  public static final String FIELD_NAME_GROUP_ATTR_UPDATERS = "groupAttrUpdaters";

  /** field name for attrDefAttrReaders */
  public static final String FIELD_NAME_ATTR_DEF_ATTR_READERS = "attrDefAttrReaders";
  
  /** field name for attrDefAttrUpdaters */
  public static final String FIELD_NAME_ATTR_DEF_ATTR_UPDATERS = "attrDefAttrUpdaters";

  /** field name for stemAttrReaders */
  public static final String FIELD_NAME_STEM_ATTR_READERS = "stemAttrReaders";
  
  /** field name for stemAttrUpdaters */
  public static final String FIELD_NAME_STEM_ATTR_UPDATERS = "stemAttrUpdaters";
  
  /** col */
  public static final String COLUMN_ID = "id";
  
  /** col */
  public static final String COLUMN_NAME = "name";
  
  /** col */
  public static final String COLUMN_READ_PRIVILEGE = "read_privilege";
  
  /** col */
  public static final String COLUMN_TYPE = "type";
  
  /** col */
  public static final String COLUMN_WRITE_PRIVILEGE = "write_privilege";
  
  /** col */
  public static final String COLUMN_CONTEXT_ID = "context_id";
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Field.class);
  
  /**
   * print out a collection of fields
   * @param collection
   * @return the field names comma separated
   */
  public static String fieldNames(Collection<Field> collection) {
    StringBuilder result = new StringBuilder();
    for (Field field : GrouperUtil.nonNull(collection)) {
      result.append(field.getName()).append(", ");
    }
    if (result.length() >= 2) {
      //take off the last comma and space
      result.delete(result.length()-2, result.length());
    }
    return result.toString();
  }

  /** table name for fields */
  public static final String TABLE_GROUPER_FIELDS = "grouper_fields";
  
  /** uuid col in db */
  public static final String COLUMN_FIELD_UUID = "field_uuid";
  
  /** old id col for id conversion */
  public static final String COLUMN_OLD_ID = "old_id";
  
  /** old uuid id col for id conversion */
  public static final String COLUMN_OLD_FIELD_UUID = "old_field_uuid";
  
  
  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: readPrivilege */
  public static final String FIELD_READ_PRIVILEGE = "readPrivilege";

  /** constant for field name for: type */
  public static final String FIELD_TYPE = "type";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /** constant for field name for: writePrivilege */
  public static final String FIELD_WRITE_PRIVILEGE = "writePrivilege";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_NAME, FIELD_READ_PRIVILEGE, 
      FIELD_TYPE, FIELD_UUID, FIELD_WRITE_PRIVILEGE);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DB_VERSION, FIELD_HIBERNATE_VERSION_NUMBER, 
      FIELD_NAME, FIELD_READ_PRIVILEGE, FIELD_TYPE, FIELD_UUID, 
      FIELD_WRITE_PRIVILEGE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for property name for: uuid */
  public static final String PROPERTY_UUID = "uuid";


  /** context id of the transaction */
  private String contextId;

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

  /** */
  private String    name;
  /** */
  private String    readPrivilege;
  /** */
  private String    type;
  /** */
  private String    uuid;
  /** */
  private String    writePrivilege;
  /** */
  public  static final  long      serialVersionUID  = 2072790175332537149L;


  /**
   * see if this is a list of members field for stems
   * @return true if stem list field
   */
  public boolean isStemListField() {
    return StringUtils.equals("naming", this.type);
  }
  
  /**
   * see if this is a list of members field for attributeDefs
   * @return true if attribute def list field
   */
  public boolean isAttributeDefListField() {
    return StringUtils.equals("attributeDef", this.type);
  }
  
  /**
   * see if this is a list of members field for groups
   * @return true if group list field
   */
  public boolean isGroupListField() {
    return StringUtils.equals("list", this.type)
      || StringUtils.equals("access", this.type);
  }
  
  /**
   * see if this is privilege field for groups
   * @return true if group access list field
   */
  public boolean isGroupAccessField() {
    return StringUtils.equals("access", this.type);
  }
  
  /**
   * see if this is a list of members field for groups
   * @return true if group list field
   */
  public boolean isEntityListField() {
    return StringUtils.equals("access", this.type)
      && (StringUtils.equals(Field.FIELD_NAME_ADMINS, this.name)
          || StringUtils.equals(Field.FIELD_NAME_VIEWERS, this.name));
  }
  
  /**
   * @return field type
   */
  public FieldType getType() {
    return FieldType.getInstance( this.getTypeString() );
  }

  /**
   * @return privilege
   */
  public Privilege getReadPriv() {
    return Privilege.getInstance( this.getReadPrivilege() ); 
  } // public Privilege getReadPriv()

  
  /**
   * @return privilege
   */
  public Privilege getWritePriv() {
    return Privilege.getInstance( this.getWritePrivilege() );
  } // public Privilege getWritePriv()

  /**
   * @param other 
   * @return if equals
   * @since   1.2.0
   */
  public boolean equals(Object other) {
    if (this == other) { 
      return true;
    }
    if ( !(other instanceof Field) ) {
      return false;
    }
    Field that = (Field) other;
    return new EqualsBuilder()
      .append( this.name, that.name )
      .append( this.type, that.type )
     .isEquals();
  } // public boolean equals(other)

  /**
   * @return name
   * @since   1.2.0
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return read privilege
   * @since   1.2.0
   */
  public String getReadPrivilege() {
    return this.readPrivilege;
  }

  /**
   * @return type string
   * @since   1.2.0
   */
  public String getTypeString() {
    return this.type;
  }

  /**
   * @return uuid
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @return write privilege
   * @since   1.2.0
   */
  public String getWritePrivilege() {
    return this.writePrivilege;
  }

  /**
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.name )
      .append( this.type )
      .toHashCode();
  } // public int hashCode()

  /**
   * @param name 
   * @since   1.2.0
   */
  public void setName(String name) {
    this.name = name;
  
  }

  /**
   * @param readPrivilege 
   * @since   1.2.0
   */
  public void setReadPrivilege(Privilege readPrivilege) {
    this.readPrivilege = readPrivilege.getName();
  
  }

  /**
   * @param readPrivilege 
   * @since   1.2.0
   */
  public void setReadPrivilege(String readPrivilege) {
    this.readPrivilege = readPrivilege;
  
  }

  /**
   * @param type 
   * @since   1.2.0
   */
  public void setType(FieldType type) {
    this.type = type.toString();
  
  }

  /**
   * @param type 
   * @since   1.2.0
   */
  public void setTypeString(String type) {
    this.type = type;
  
  }

  /**
   * see if this field is an attribute name
   * @return true if so
   * @deprecated
   */
  public boolean isAttributeName() {
    return false;
  }

  /**
   * @param uuid 
   * @since   1.2.0
   */
  public void setUuid(String uuid) {
    this.uuid = uuid;
  
  }

  /**
   * @param writePrivilege 
   * @since   1.2.0
   */
  public void setWritePrivilege(Privilege writePrivilege) {
    this.writePrivilege = writePrivilege.getName();
  
  }

  /**
   * @param writePrivilege 
   * @since   1.2.0
   */
  public void setWritePrivilege(String writePrivilege) {
    this.writePrivilege = writePrivilege;
  
  }

  /**
   * @return string
   * @since   1.2.0
   */
  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append( "name",           this.getName()           )
      .append( "readPrivilege",  this.getReadPrivilege()  )
      .append( "type",           this.getType()           )
      .append( "uuid",           this.getUuid()           )
      .append( "writePrivilege", this.getWritePrivilege() )
      .toString();
  } // public String toString()

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {
    super.onPostDelete(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.FIELD, 
        FieldHooks.METHOD_FIELD_POST_COMMIT_DELETE, HooksFieldBean.class, 
        this, Field.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.FIELD, 
        FieldHooks.METHOD_FIELD_POST_DELETE, HooksFieldBean.class, 
        this, Field.class, VetoTypeGrouper.FIELD_POST_DELETE, false, true);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    super.onPostSave(hibernateSession);
    
    // add group sets
    if (this.type.equals("list") && !Group.getDefaultList().getUuid().equals(this.getUuid())) {
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findAllByType(this.getGroupType());
      for (Group group : groups) {
        if (group.getTypeOfGroup() != null && group.getTypeOfGroup().supportsField(this)) {
          GroupSet groupSet = new GroupSet();
          groupSet.setId(GrouperUuid.getUuid());
          groupSet.setCreatorId(GrouperSession.staticGrouperSession().getMemberUuid());
          groupSet.setDepth(0);
          groupSet.setMemberGroupId(group.getUuid());
          groupSet.setOwnerGroupId(group.getUuid());
          groupSet.setParentId(groupSet.getId());
          groupSet.setFieldId(this.getUuid());
          GrouperDAOFactory.getFactory().getGroupSet().save(groupSet);
        }
      }
    }
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.FIELD, 
        FieldHooks.METHOD_FIELD_POST_INSERT, HooksFieldBean.class, 
        this, Field.class, VetoTypeGrouper.FIELD_POST_INSERT, true, false);

    //do these second so the right object version is set, and dbVersion is ok
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.FIELD, 
        FieldHooks.METHOD_FIELD_POST_COMMIT_INSERT, HooksFieldBean.class, 
        this, Field.class);


  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {
    super.onPostUpdate(hibernateSession);

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(GrouperHookType.FIELD, 
        FieldHooks.METHOD_FIELD_POST_COMMIT_UPDATE, HooksFieldBean.class, 
        this, Field.class);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.FIELD, 
        FieldHooks.METHOD_FIELD_POST_UPDATE, HooksFieldBean.class, 
        this, Field.class, VetoTypeGrouper.FIELD_POST_UPDATE, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    
    // remove group sets
    if (this.type.equals("list") && !Group.getDefaultList().getUuid().equals(this.getUuid())) {
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findAllByType(this.getGroupType());
      for (Group group : groups) {
        GrouperDAOFactory.getFactory().getGroupSet().deleteSelfByOwnerGroupAndField(group.getUuid(), this.getUuid());
      }
    }
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.FIELD, 
        FieldHooks.METHOD_FIELD_PRE_DELETE, HooksFieldBean.class, 
        this, Field.class, VetoTypeGrouper.FIELD_PRE_DELETE, false, false);
  
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_FIELD_DELETE, 
        ChangeLogLabels.GROUP_FIELD_DELETE.id.name(), 
        this.getUuid(), ChangeLogLabels.GROUP_FIELD_DELETE.name.name(), 
        this.getName(), null, 
        null,
        null, 
        null,
        ChangeLogLabels.GROUP_FIELD_DELETE.type.name(), this.getTypeString()
    ).save();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
        
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.FIELD, 
        FieldHooks.METHOD_FIELD_PRE_INSERT, HooksFieldBean.class, 
        this, Field.class, VetoTypeGrouper.FIELD_PRE_INSERT, false, false);
  
    //change log into temp table
    new ChangeLogEntry(true, ChangeLogTypeBuiltin.GROUP_FIELD_ADD, 
        ChangeLogLabels.GROUP_FIELD_ADD.id.name(), 
        this.getUuid(), ChangeLogLabels.GROUP_FIELD_ADD.name.name(), 
        this.getName(), null, 
        null,
        null, 
        null,
        ChangeLogLabels.GROUP_FIELD_ADD.type.name(), this.getTypeString()
    ).save();
    
   

  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.FIELD, 
        FieldHooks.METHOD_FIELD_PRE_UPDATE, HooksFieldBean.class, 
        this, Field.class, VetoTypeGrouper.FIELD_PRE_UPDATE, false, false);

    //change log into temp table
    ChangeLogEntry.saveTempUpdates(ChangeLogTypeBuiltin.GROUP_FIELD_UPDATE, 
        this, this.dbVersion(),
        GrouperUtil.toList(ChangeLogLabels.GROUP_FIELD_UPDATE.id.name(),this.getUuid(), 
            ChangeLogLabels.GROUP_FIELD_UPDATE.name.name(), this.getName(),
            null, 
            null,
            null, 
            null,
            ChangeLogLabels.GROUP_FIELD_ADD.type.name(), this.getTypeString()),
        GrouperUtil.toList(FIELD_NAME, FIELD_TYPE, FIELD_READ_PRIVILEGE, FIELD_WRITE_PRIVILEGE),
        GrouperUtil.toList(ChangeLogLabels.GROUP_FIELD_UPDATE.name.name(),
            ChangeLogLabels.GROUP_FIELD_UPDATE.type.name(), ChangeLogLabels.GROUP_FIELD_UPDATE.readPrivilege.name(),
            ChangeLogLabels.GROUP_FIELD_UPDATE.writePrivilege.name()));    
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public Field dbVersion() {
    return (Field)this.dbVersion;
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
  public Field clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }

  /**
   * store this object to the DB.
   */
  public void store() {    
    GrouperDAOFactory.getFactory().getField().update(this);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlCopyBusinessPropertiesToExisting(java.lang.Object)
   */
  public void xmlCopyBusinessPropertiesToExisting(Field existingRecord) {
    existingRecord.name = this.name;
    existingRecord.readPrivilege = this.readPrivilege;
    existingRecord.type = this.type;
    existingRecord.setUuid(this.getUuid());
    existingRecord.writePrivilege = this.writePrivilege;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentBusinessProperties(java.lang.Object)
   */
  public boolean xmlDifferentBusinessProperties(Field other) {
    if (!StringUtils.equals(this.name, other.name)) {
      return true;
    }
    if (!StringUtils.equals(this.readPrivilege, other.readPrivilege)) {
      return true;
    }
    if (!StringUtils.equals(this.type, other.type)) {
      return true;
    }
    if (!StringUtils.equals(this.uuid, other.uuid)) {
      return true;
    }
    if (!StringUtils.equals(this.writePrivilege, other.writePrivilege)) {
      return true;
    }

    return false;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlDifferentUpdateProperties(java.lang.Object)
   */
  public boolean xmlDifferentUpdateProperties(Field other) {
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
  public Field xmlRetrieveByIdOrKey() {
    return GrouperDAOFactory.getFactory().getField().findByUuidOrName(this.uuid, this.name, false,
        new QueryOptions().secondLevelCache(false));
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public Field xmlSaveBusinessProperties(Field existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      existingRecord = Field.internal_addField(GrouperSession.staticGrouperSession(), this.name, this.getType(), this.getReadPriv(), this.getWritePriv(), true, false, null, this.uuid);
    }

    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.store();
    FieldFinder.clearCache();
    GroupTypeFinder.clearCache();
    return existingRecord;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveUpdateProperties()
   */
  public void xmlSaveUpdateProperties() {
    GrouperDAOFactory.getFactory().getField().saveUpdateProperties(this);
    FieldFinder.clearCache();

  }

  /**
   * convert to xml bean for export
   * @param grouperVersion
   * @return xml bean
   */
  public XmlExportField xmlToExportField(GrouperVersion grouperVersion) {
    if (grouperVersion == null) {
      throw new RuntimeException();
    }

    XmlExportField xmlExportField = new XmlExportField();
    
    xmlExportField.setContextId(this.getContextId());
    xmlExportField.setHibernateVersionNumber(this.getHibernateVersionNumber());
    
    xmlExportField.setName(this.getName());
    xmlExportField.setReadPrivilege(this.getReadPrivilege());
    xmlExportField.setType(this.getTypeString());
    xmlExportField.setUuid(this.getUuid());
    xmlExportField.setWritePrivilege(this.getWritePrivilege());
    return xmlExportField;
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlGetId()
   */
  public String xmlGetId() {
    return this.getUuid();
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSetId(java.lang.String)
   */
  public void xmlSetId(String theId) {
    this.setUuid(theId);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportableBase#xmlToString()
   */
  public String xmlToString() {
    StringWriter stringWriter = new StringWriter();
    
    stringWriter.write("Field: " + this.getUuid() + ", " + this.getName());

    return stringWriter.toString();
    
  }

  /**
   * @param exceptionIfNoGroupType 
   * @return group type
   */
  public GroupType getGroupType(boolean exceptionIfNoGroupType) {
    if (this.getType() == FieldType.LIST && !this.getUuid().equals(Group.getDefaultList().getUuid())) {
      return GroupTypeFinder.internal_findGroupTypeByField(this, true);
    }
    
    if (exceptionIfNoGroupType) {
      throw new RuntimeException("Field " + this.getName() + " does not have a group type.");
    }
    
    return null;
  }
  
  /**
   * @return group type
   */
  public GroupType getGroupType() {
    return getGroupType(true);
  }
  
  /**
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Field that) {
    if (that==null) {
      return 1;
    }
    String thisName = StringUtils.defaultString(this.getName());
    String thatName = StringUtils.defaultString(that.getName());
    return thisName.compareTo(thatName);
  }

  /**
   * add a field if it is not already there
   * @param s
   * @param name
   * @param type
   * @param read
   * @param write
   * @param exceptionIfExists
   * @param updateIfExists 
   * @param changedArray is an array of 1 if you want to know if this method changed anything, else null
   * @param uuid 
   * @return the field
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   */
  public static Field internal_addField(
    final GrouperSession s, final String name, final FieldType type, final Privilege read, 
    final Privilege write, final boolean exceptionIfExists, final boolean updateIfExists,
    final boolean[] changedArray, String uuid) throws  InsufficientPrivilegeException, SchemaException {

    //these are reserved words:
    if (Group.INTERNAL_FIELD_ATTRIBUTES.contains(name)) {
      throw new RuntimeException("You cannot add a field which is a reserved word '" 
          + name + "', reserved words are : " + GrouperUtil.toStringForLog(Group.INTERNAL_FIELD_ATTRIBUTES));
    }
    
    final String UUID = StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid;
    
    return (Field)HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        
        try {
          //note, no need for GrouperSession inverse of control
          StopWatch sw  = new StopWatch();
          sw.start();
          AddFieldToGroupTypeValidator v = AddFieldToGroupTypeValidator.validate(name, !exceptionIfExists);
          if (v.isInvalid()) {
            throw new SchemaException( v.getErrorMessage() );
          }
          Field field = FieldFinder.find(name, false);

          if (field != null) {
            boolean changed = false;
            if (!type.equals(field.getType())) {
              //dont want to change types, that could be bad!
              throw new SchemaException("field '" + name + "' does not have type: " + type + ", it has: " + field.getType());
            }
            if (!read.equals(field.getReadPriv())) {
              if (exceptionIfExists) {
                throw new SchemaException("field '" + name + "' does not have read privilege: " + read + ", it has: " + field.getReadPrivilege());
              }
              if (updateIfExists) {
                changed = true;
                field.setReadPrivilege(read);
              }
            }
            if (!write.equals(field.getWritePriv())) {
              if (exceptionIfExists) {
                throw new SchemaException("field '" + name + "' does not have write privilege: " + write + ", it has: " + field.getWritePrivilege());
              }
              if (updateIfExists) {
                changed = true;
                field.setWritePrivilege(write);
              }
            }
            if (exceptionIfExists) {
              throw new SchemaException("field exists: '" + name + "'");
            }
            //store minor changes to db
            if (changed && updateIfExists) {
              changed = true;
              
              String differences = GrouperUtil.dbVersionDescribeDifferences(field.dbVersion(), 
                  field, field.dbVersion() != null ? field.dbVersionDifferentFields() : Field.CLONE_FIELDS);
              
              GrouperDAOFactory.getFactory().getField().createOrUpdate(field);
  
              if (!hibernateHandlerBean.isCallerWillCreateAudit()) {
                //audit the update
                AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_FIELD_UPDATE, "id", 
                    field.getUuid(), "name", field.getName(), "groupTypeId", null, 
                    "groupTypeName", null, "type", type.getType());
                
                String description = "Updated group field: " + name + ", id: " + field.getUuid() 
                    + ", type: " + type + ".\n" + differences;
                auditEntry.setDescription(description);
                
                auditEntry.saveOrUpdate(true);
              }
              
              if (GrouperUtil.length(changedArray) > 0) {
                changedArray[0] = true;
              }
            } else {
              if (GrouperUtil.length(changedArray) > 0) {
                changedArray[0] = false;
              }
            }
            FieldFinder.internal_updateKnownFields();
            return field;
          }
          if (GrouperUtil.length(changedArray) > 0) {
            changedArray[0] = true;
          }
          try {
            field = new Field();
            field.setName(name);
            field.setReadPrivilege(read);
            field.setType(type);
            field.setUuid(UUID);
            field.setWritePrivilege(write);
              
            GrouperDAOFactory.getFactory().getField().createOrUpdate(field);
            
            sw.stop();
            EventLog.info(
              s, 
              M.GROUPTYPE_ADDFIELD + Quote.single(field.getName()) + " ftype=" + Quote.single(type.toString()),
              sw
            );
            FieldFinder.internal_updateKnownFields();
          }
          catch (GrouperDAOException eDAO) {
            String msg = E.GROUPTYPE_FIELDADD + name + ": " + eDAO.getMessage();
            LOG.error( msg);
            throw new SchemaException(msg, eDAO);
          }
          
          
          //only audit if actually changed the type
          AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.GROUP_FIELD_ADD, "id", 
              field.getUuid(), "name", field.getName(), "groupTypeId", null, "groupTypeName", null, "type", type.getType());
          auditEntry.setDescription("Added group field: " + name + ", id: " + field.getUuid() + ", type: " + type);
          auditEntry.saveOrUpdate(true);
          
          return field;
        } catch (GrouperDAOException eDAO) {
          String msg = E.GROUPTYPE_FIELDADD + name + ": " + eDAO.getMessage();
          LOG.error( msg);
          throw new SchemaException(msg, eDAO);
        }
      }
      
    });
    
    
    
    
  }
} // public class Field extends GrouperAPI implements Serializable

