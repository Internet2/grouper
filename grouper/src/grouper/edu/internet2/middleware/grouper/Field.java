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
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.changeLog.ChangeLogEntry;
import edu.internet2.middleware.grouper.changeLog.ChangeLogLabels;
import edu.internet2.middleware.grouper.changeLog.ChangeLogTypeBuiltin;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.FieldHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksFieldBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.hib3.Hib3GrouperVersioned;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperHasContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.xml.export.XmlExportField;
import edu.internet2.middleware.grouper.xml.export.XmlImportable;


/** 
 * Schema specification for a Group attribute or list.
 * Reference to members list is: Group.getDefaultList()
 * <p/>
 * @author  blair christensen.
 * @version $Id: Field.java,v 1.48 2009-09-24 18:07:16 shilen Exp $    
 */
public class Field extends GrouperAPI implements GrouperHasContext, Hib3GrouperVersioned, XmlImportable<Field> {

  /** col */
  public static final String COLUMN_ID = "id";

  /** col */
  public static final String COLUMN_GROUPTYPE_UUID = "grouptype_uuid";

  /** col */
  public static final String COLUMN_IS_NULLABLE = "is_nullable";
  
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

  /** constant for field name for: groupTypeUUID */
  public static final String FIELD_GROUP_TYPE_UUID = "groupTypeUuid";

  /** constant for field name for: isNullable */
  public static final String FIELD_IS_NULLABLE = "isNullable";

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
      FIELD_GROUP_TYPE_UUID, FIELD_IS_NULLABLE, FIELD_NAME, FIELD_READ_PRIVILEGE, 
      FIELD_TYPE, FIELD_UUID, FIELD_WRITE_PRIVILEGE);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_DB_VERSION, FIELD_GROUP_TYPE_UUID, FIELD_HIBERNATE_VERSION_NUMBER, FIELD_IS_NULLABLE, 
      FIELD_NAME, FIELD_READ_PRIVILEGE, FIELD_TYPE, FIELD_UUID, 
      FIELD_WRITE_PRIVILEGE);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for property name for: uuid */
  public static final String PROPERTY_UUID = "uuid";

  /** */
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private GroupType cachedGroupType   = null;


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
  private String    groupTypeUuid;
  /** */
  private boolean   isNullable;
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
   * @return group type
   * @throws IllegalStateException 
   */
  public GroupType getGroupType() 
    throws  IllegalStateException
  {
    if ( this.cachedGroupType == null ) {
      try {
//        GroupType type = GrouperDAOFactory.getFactory().getGroupType().findByUuid( this.getGroupTypeUuid() ) ;
        GroupType type = GroupTypeFinder.findByUuid(this.getGroupTypeUuid(), true);
        this.cachedGroupType = type;
      }
      catch (SchemaException eS) {
        throw new IllegalStateException( "unable to fetch GroupType: '" 
            + this.getGroupTypeUuid() + "', " + eS.getMessage() );
      }
    }
    return this.cachedGroupType;
  } 

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
   * @return if required
   */
  public boolean getRequired() {
    return !this.getIsNullable();
  } // public boolean isRequired()

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
      .append( this.groupTypeUuid, that.groupTypeUuid )
      .append( this.name, that.name )
      .append( this.type, that.type )
     .isEquals();
  } // public boolean equals(other)

  /**
   * @return string
   * @since   1.2.0
   */
  public String getGroupTypeUuid() {
    return this.groupTypeUuid;
  }

  /**
   * @return if nullable
   * @since   1.2.0
   */
  public boolean getIsNullable() {
    return this.isNullable;
  }

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
      .append( this.groupTypeUuid )
      .append( this.name )
      .append( this.type )
      .toHashCode();
  } // public int hashCode()

  /**
   * @param groupTypeUUID 
   * @since   1.2.0
   */
  public void setGroupTypeUuid(String groupTypeUUID) {
    this.groupTypeUuid = groupTypeUUID;
  }

  /**
   * @param isNullable 
   * @since   1.2.0
   */
  public void setIsNullable(boolean isNullable) {
    this.isNullable = isNullable;
  }

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
   */
  public boolean isAttributeName() {
    return FieldType.ATTRIBUTE.equals(this.getType());
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
      .append( "groupTypeUuid",  this.getGroupTypeUuid()  )
      .append( "isNullable",     this.getIsNullable()     )
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
    if (this.isGroupListField()) {
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findAllByType(this.getGroupType());
      Iterator<Group> iter = groups.iterator();
      
      while (iter.hasNext()) {
        Group group = iter.next();
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
    if (this.isGroupListField()) {
      Set<Group> groups = GrouperDAOFactory.getFactory().getGroup().findAllByType(this.getGroupType());
      Iterator<Group> iter = groups.iterator();
      
      while (iter.hasNext()) {
        Group group = iter.next();
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
        this.getName(), ChangeLogLabels.GROUP_FIELD_DELETE.groupTypeId.name(), 
        this.getGroupTypeUuid(),
        ChangeLogLabels.GROUP_FIELD_DELETE.groupTypeName.name(), 
        this.getGroupType().getName(),
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
        this.getName(), ChangeLogLabels.GROUP_FIELD_ADD.groupTypeId.name(), 
        this.getGroupTypeUuid(),
        ChangeLogLabels.GROUP_FIELD_ADD.groupTypeName.name(), 
        this.getGroupType().getName(),
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
            ChangeLogLabels.GROUP_FIELD_ADD.groupTypeId.name(), 
            this.getGroupTypeUuid(),
            ChangeLogLabels.GROUP_FIELD_ADD.groupTypeName.name(), 
            this.getGroupType().getName(),
            ChangeLogLabels.GROUP_FIELD_ADD.type.name(), this.getTypeString()),
        GrouperUtil.toList(FIELD_NAME, FIELD_GROUP_TYPE_UUID, FIELD_TYPE, FIELD_READ_PRIVILEGE, FIELD_WRITE_PRIVILEGE, FIELD_IS_NULLABLE),
        GrouperUtil.toList(ChangeLogLabels.GROUP_FIELD_UPDATE.name.name(),
            ChangeLogLabels.GROUP_FIELD_UPDATE.groupTypeId.name(),
            ChangeLogLabels.GROUP_FIELD_UPDATE.type.name(), ChangeLogLabels.GROUP_FIELD_UPDATE.readPrivilege.name(),
            ChangeLogLabels.GROUP_FIELD_UPDATE.writePrivilege.name(), ChangeLogLabels.GROUP_FIELD_UPDATE.isNullable.name()
            ));    
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
    existingRecord.groupTypeUuid = this.groupTypeUuid;
    existingRecord.isNullable = this.isNullable;
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
    if (!StringUtils.equals(this.groupTypeUuid, other.groupTypeUuid)) {
      return true;
    }
    if (this.isNullable != other.isNullable) {
      return true;
    }
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
    return GrouperDAOFactory.getFactory().getField().findByUuidOrName(this.uuid, this.name, this.groupTypeUuid, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.xml.export.XmlImportable#xmlSaveBusinessProperties(java.lang.Object)
   */
  public Field xmlSaveBusinessProperties(Field existingRecord) {
    //if its an insert, call the business method
    if (existingRecord == null) {
      GroupType groupType = GroupTypeFinder.findByUuid(this.groupTypeUuid, true);
      existingRecord = groupType.internal_addField(GrouperSession.staticGrouperSession(), this.name, this.getType(), this.getReadPriv(), this.getWritePriv(), !this.isNullable, true, false, null, this.uuid);
    }
    this.xmlCopyBusinessPropertiesToExisting(existingRecord);
    //if its an insert or update, then do the rest of the fields
    existingRecord.store();
    FieldFinder.clearCache();
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
    xmlExportField.setGroupTypeUuid(this.getGroupTypeUuid());
    xmlExportField.setHibernateVersionNumber(this.getHibernateVersionNumber());
    
    xmlExportField.setName(this.getName());
    xmlExportField.setNullable(this.getIsNullable() ? "T" : "F");
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

} // public class Field extends GrouperAPI implements Serializable

