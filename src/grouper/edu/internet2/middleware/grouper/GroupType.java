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
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.internet2.middleware.grouper.annotations.GrouperIgnoreClone;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreDbVersion;
import edu.internet2.middleware.grouper.annotations.GrouperIgnoreFieldConstant;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupHooks;
import edu.internet2.middleware.grouper.hooks.GroupTypeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.FieldDAO;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.log.EventLog;
import edu.internet2.middleware.grouper.misc.E;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.M;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.validator.AddFieldToGroupTypeValidator;
import edu.internet2.middleware.grouper.validator.DeleteFieldFromGroupTypeValidator;
import edu.internet2.middleware.grouper.validator.ModifyGroupTypeValidator;

/** 
 * Schema specification for a Group type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupType.java,v 1.64 2008-07-27 07:37:24 mchyzer Exp $
 */
public class GroupType extends GrouperAPI implements Serializable {

  //*****  START GENERATED WITH GenerateFieldConstants.java *****//

  /** constant for field name for: createTime */
  public static final String FIELD_CREATE_TIME = "createTime";

  /** constant for field name for: creatorUUID */
  public static final String FIELD_CREATOR_UUID = "creatorUUID";

  /** constant for field name for: dbVersion */
  public static final String FIELD_DB_VERSION = "dbVersion";

  /** constant for field name for: id */
  public static final String FIELD_ID = "id";

  /** constant for field name for: isAssignable */
  public static final String FIELD_IS_ASSIGNABLE = "isAssignable";

  /** constant for field name for: isInternal */
  public static final String FIELD_IS_INTERNAL = "isInternal";

  /** constant for field name for: name */
  public static final String FIELD_NAME = "name";

  /** constant for field name for: uuid */
  public static final String FIELD_UUID = "uuid";

  /**
   * fields which are included in db version
   */
  private static final Set<String> DB_VERSION_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_ID, FIELD_IS_ASSIGNABLE, 
      FIELD_IS_INTERNAL, FIELD_NAME, FIELD_UUID);

  /**
   * fields which are included in clone method
   */
  private static final Set<String> CLONE_FIELDS = GrouperUtil.toSet(
      FIELD_CREATE_TIME, FIELD_CREATOR_UUID, FIELD_DB_VERSION, FIELD_ID,
      FIELD_IS_ASSIGNABLE, FIELD_IS_INTERNAL, FIELD_NAME, FIELD_UUID);

  //*****  END GENERATED WITH GenerateFieldConstants.java *****//
  
  // PUBLIC CLASS CONSTANTS //
  public static final long serialVersionUID = 8214760621248803096L;


  // PUBLIC CLASS METHODS //

  /*
   * Create a new {@link GroupType}.  
   * <p/>
   * Create a new custom group type that can be assigned to existing or
   * new groups.  If the type already exists, a {@link SchemaException}
   * will be thrown.  If the subject is not root-like, an 
   * {@link InsufficientPrivilegeException} will be thrown.
   * <pre class="eg">
   * try {
   *   GroupType type = GroupType.createType(s, "my custom type");
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Subject not privileged to add group types.
   * }
   * catch (SchemaException eS) {
   *   // Type not created
   * }
   * </pre>
   * @param   s     Create type within this session context.
   * @param   name  Create type with this name.
   * @return  New {@link GroupType}.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public static GroupType createType(GrouperSession s, String name) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    StopWatch sw = new StopWatch();
    sw.start();
    GroupType type = internal_createType(s, name, true, false, true);
    sw.stop();
    EventLog.info(s, M.GROUPTYPE_ADD + Quote.single( type.getName() ), sw);
    return type;
  }


  private long    createTime;
  // PRIVATE INSTANCE VARIABLES //
  private String  creatorUUID;
  
  @GrouperIgnoreDbVersion
  @GrouperIgnoreFieldConstant
  @GrouperIgnoreClone
  private Set     fields;
  
  private String  id;
  private boolean isAssignable  = true;
  private boolean isInternal    = false;
  private String  name;
  private String  uuid; 


  // PUBLIC INSTANCE METHODS //

  /**
   * Add a custom attribute {@link Field} to a custom {@link GroupType}.
   * try {
   *   Field myAttr = type.addAttribute(
   *     "my attribute", AccessPrivilege.VIEW, AccessPrivilege.UPDATE, false
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add attribute
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Add attribute within this session context.
   * @param   name      Name of attribute.
   * @param   read      {@link Privilege} required to write to this {@link Field}.
   * @param   write     {@link Privilege} required to write to this {@link Field}.
   * @param   required  Is this attribute required.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public Field addAttribute(
    GrouperSession s, String name, Privilege read, Privilege write, boolean required
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    ModifyGroupTypeValidator v = ModifyGroupTypeValidator.validate(s, this);
    if (v.isInvalid()) {
      throw new InsufficientPrivilegeException( v.getErrorMessage() );
    }
    if (!Privilege.isAccess(read)) {
      throw new SchemaException(E.FIELD_READ_PRIV_NOT_ACCESS + read);
    }
    if (!Privilege.isAccess(write)) {
      throw new SchemaException(E.FIELD_WRITE_PRIV_NOT_ACCESS + write);
    }
    return this.internal_addField(s, name, FieldType.ATTRIBUTE, read, write, required, true);
  } // public Field addAttribute(s, name, read, write, required)

  /**
   * Add a custom list {@link Field} to a custom {@link GroupType}.
   * try {
   *   Field myList = type.addList(
   *     s, "my list", AccessPrivilege.VIEW, AccessPrivilege.UPDATE
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add list
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Add list within this session context.
   * @param   name      Name of list.
   * @param   read      {@link Privilege} required to write to this {@link Field}.
   * @param   write     {@link Privilege} required to write to this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public Field addList(
    GrouperSession s, String name, Privilege read, Privilege write
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    ModifyGroupTypeValidator v = ModifyGroupTypeValidator.validate(s, this);
    if (v.isInvalid()) {
      throw new InsufficientPrivilegeException( v.getErrorMessage() );
    }
    if (!Privilege.isAccess(read)) {
      throw new SchemaException(E.FIELD_READ_PRIV_NOT_ACCESS + read);
    }
    if (!Privilege.isAccess(write)) {
      throw new SchemaException(E.FIELD_WRITE_PRIV_NOT_ACCESS + write);
    }
    return this.internal_addField(s, name, FieldType.LIST, read, write, false, true);
  } // public Field addList(s, name, read, write)

  /*
   * Delete a custom {@link GroupType} definition.
   * <p/>
   * <pre class="eg">
   * try {
   *   aGroupType.delete(s);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Subject not privileged to delete group type.
   * }
   * catch (SchemaException eS) {
   *   // Type could not be deleted
   * }
   * </pre>
   * @param   s     Delete type within this session context.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   * @since   1.0
   */
  public void delete(GrouperSession s) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    StopWatch sw = new StopWatch();
    sw.start();
    if ( this.isSystemType() ) {
      String msg = E.GROUPTYPE_NODELSYS + this.getName();
      LOG.error( msg);
      throw new SchemaException(msg);
    } 
    if (!PrivilegeHelper.isRoot(s)) {
      String msg = E.GROUPTYPE_NODEL;
      LOG.error( msg);
      throw new InsufficientPrivilegeException(msg);
    }
    try {
      if ( GrouperDAOFactory.getFactory().getGroup().findAllByType( this ).size() > 0 ) {
        String msg = E.GROUPTYPE_DELINUSE;
        LOG.error( msg);
        throw new SchemaException(msg);
      }
      // Now delete the type
      String typeName = this.getName(); // For logging purposes
      GrouperDAOFactory.getFactory().getGroupType().delete( this, this.getFields() );
      sw.stop();
      EventLog.info(s, M.GROUPTYPE_DEL + Quote.single(typeName), sw);
      // Now update the cached types + fields
      GroupTypeFinder.internal_updateKnownTypes();
      FieldFinder.internal_updateKnownFields();
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUPTYPE_DEL + eDAO.getMessage();
      LOG.error(msg);
      throw new SchemaException(msg, eDAO);
    }
  } // public void delete(s)

  /** logger */
  private static final Log LOG = LogFactory.getLog(GroupType.class);

  /**
   * Delete a custom {@link Field} from a custom {@link GroupType}.
   * <p/>
   * Delete a field from this group type.  If the field does not exist
   * in this type a {@link SchemaException} will be thrown.  If the
   * subject is not root-like, an {@link InsufficientPrivilegeException}
   * will be thrown.
   * <pre class="eg">
   * try {
   *   type.deleteField(s, "my field");
   *   );
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete field
   * }
   * catch (SchemaException eS) {
   *   // Invalid schema
   * }
   * </pre>
   * @param   s         Delete field within this session context.
   * @param   name      Name of field to delete.
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void deleteField(GrouperSession s, String name)
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    StopWatch sw  = new StopWatch();
    sw.start();
    Field     f   = FieldFinder.find(name);  
    ModifyGroupTypeValidator          vModify = ModifyGroupTypeValidator.validate(s, this);
    if (vModify.isInvalid()) {
      throw new InsufficientPrivilegeException( vModify.getErrorMessage() );
    }
    DeleteFieldFromGroupTypeValidator vDelete = DeleteFieldFromGroupTypeValidator.validate(this, f);
    if (vDelete.isInvalid()) {
      throw new SchemaException( vDelete.getErrorMessage() );
    }
    if ( GrouperDAOFactory.getFactory().getField().isInUse(f) ) {
      String msg = E.GROUPTYPE_FIELDNODELINUSE + name;
      LOG.error( msg);
      throw new SchemaException(msg);
    }
    // With validation complete, delete the field
    try {
      Set fields = this.getFields();
      if ( fields.remove( f ) ) {
        this.setFields(fields);
        GrouperDAOFactory.getFactory().getGroupType().deleteField( f);
        sw.stop();
        EventLog.info(
          s,
          M.GROUPTYPE_DELFIELD + Quote.single(f.getName()) + " type=" + Quote.single( this.getName() ),
          sw
        );
      }
      else {
        String msg = E.GROUPTYPE_FIELDNODELMISS;
        LOG.error( msg);
        throw new SchemaException(msg);
      }
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUPTYPE_FIELDDEL + eDAO.getMessage();
      LOG.error( msg);
      throw new SchemaException(msg, eDAO);
    }
  } // public void deleteField(s, name)

  // @since   1.2.0
  public boolean isSystemType() {
    if ( "base".equals( this.getName() ) || "naming".equals( this.getName() ) ) {
      return true;
    }
    return false;
  } // public boolean isSystemType()
  

  /**
   * 
   * @param s
   * @param name
   * @param isAssignable
   * @param isInternal
   * @param exceptionIfExists
   * @return
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   */
  public static GroupType internal_createType(
    GrouperSession s, String name, boolean isAssignable, boolean isInternal, boolean exceptionIfExists)
      throws  InsufficientPrivilegeException,
              SchemaException
  { 
    //note, no need for GrouperSession inverse of control
    if (!PrivilegeHelper.isRoot(s)) {
      String msg = E.GROUPTYPE_NOADD;
      LOG.error( msg);
      throw new InsufficientPrivilegeException(msg);
    }
    GroupTypeDAO dao = GrouperDAOFactory.getFactory().getGroupType();
    if ( dao.existsByName(name) ) {
      if (exceptionIfExists) {
        String msg = E.GROUPTYPE_EXISTS + name;
        LOG.error( msg);
        throw new SchemaException(msg);
      }
      return GroupTypeFinder.find(name);
    }
    GroupType _gt = new GroupType();
    _gt.setCreateTime( new Date().getTime() );
      _gt.setCreatorUuid( s.getMember().getUuid() );
      _gt.setFields( new LinkedHashSet() );
      _gt.setIsAssignable(isAssignable);
      _gt.setIsInternal(isInternal);
      _gt.setName(name);
      _gt.setUuid( GrouperUuid.getUuid() );
      
    try {
      dao.create(_gt) ;
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUPTYPE_ADD + name + ": " + eDAO.getMessage();
      LOG.error( msg);
      throw new SchemaException(msg, eDAO);
    }
    return _gt;
  } // protected static GroupType internal_createType(s, name, isAssignable, isInternal)


  /**
   * 
   * @param s
   * @param name
   * @param type
   * @param read
   * @param write
   * @param required
   * @param exceptionIfExists
   * @return the field
   * @throws InsufficientPrivilegeException
   * @throws SchemaException
   */
  public Field internal_addField(
    GrouperSession s, String name, FieldType type, Privilege read, 
    Privilege write, boolean required, boolean exceptionIfExists
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    StopWatch sw  = new StopWatch();
    sw.start();
    AddFieldToGroupTypeValidator v = AddFieldToGroupTypeValidator.validate(name, !exceptionIfExists);
    if (v.isInvalid()) {
      throw new SchemaException( v.getErrorMessage() );
    }
    Field field = null;
    try {
      field = FieldFinder.find(name);
    } catch (SchemaException e) {
      //ignore, it probably doesnt exist
    }
    if (field != null) {
      if (!type.equals(field.getType())) {
        throw new SchemaException("field '" + name + "' does not have type: " + type + ", it has: " + field.getType());
      }
      if (field.getRequired() != required) {
        throw new SchemaException("field '" + name + "' does not match required flag: " + required);
      }
      if (!read.equals(field.getReadPriv())) {
        throw new SchemaException("field '" + name + "' does not have read privilege: " + read + ", it has: " + field.getReadPrivilege());
      }
      if (!write.equals(field.getWritePriv())) {
        throw new SchemaException("field '" + name + "' does not have write privilege: " + write + ", it has: " + field.getWritePrivilege());
      }
      if (exceptionIfExists) {
        throw new SchemaException("field exists: '" + name + "'");
      }
      return field;
    }
    try {
      boolean nullable = true;
      if (required == true) {
        nullable = false;
      }
      field = new Field();
      field.setGroupTypeUuid( this.getUuid() );
      field.setIsNullable(nullable);
      field.setName(name);
      field.setReadPrivilege(read);
      field.setType(type);
      field.setUuid( GrouperUuid.getUuid() );
      field.setWritePrivilege(write);
        
      GrouperDAOFactory.getFactory().getGroupType().createField(field);

      Set fields = this.getFields();
      fields.add( field );

      sw.stop();
      EventLog.info(
        s, 
        M.GROUPTYPE_ADDFIELD + Quote.single(field.getName()) + " ftype=" + Quote.single(type.toString()) 
        + " gtype=" + Quote.single( this.getName() ),
        sw
      );
      return field;
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUPTYPE_FIELDADD + name + ": " + eDAO.getMessage();
      LOG.error( msg);
      throw new SchemaException(msg, eDAO);
    }
  } // protected Field internal_addField(s, name, type, read, write, required)

  
  /**
   * @since   1.2.0
   */
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GroupType)) {
      return false;
    }
    GroupType that = (GroupType) other;
    return new EqualsBuilder()
      .append( this.getUuid(), that.getUuid() )
      .isEquals();
  } // public boolean equals(other)


  /**
   * @since   1.2.0
   */ 
  public long getCreateTime() {
    return this.createTime;
  }


  /**
   * @since   1.2.0
   */ 
  public String getCreatorUuid() {
    return this.creatorUUID;
  }


  // PROTECTED INSTANCE METHODS //
  
  /**
   * @since   1.2.0
   */ 
  public Set<Field> getFields() {
    if (this.fields == null) {
      this.fields = GrouperDAOFactory.getFactory().getField().findAllFieldsByGroupType( this.getUuid() );
    }
    return this.fields;
  }


  /**
   * @since   1.2.0
   */ 
  public String getId() {
    return this.id;
  }


  /**
   * @since   1.2.0
   */ 
  public boolean getIsAssignable() {
    return this.isAssignable;
  }


  /**
   * @since   1.2.0
   */ 
  public boolean getIsInternal() {
    return this.isInternal;
  }


  /**
   * @since   1.2.0
   */ 
  public String getName() {
    return this.name;
  }


  /**
   * @since   1.2.0
   */ 
  public String getUuid() {
    return this.uuid;
  }


  /**
   * @since   1.2.0
   */
  public int hashCode() {
    return new HashCodeBuilder()
      .append( this.getUuid() )
      .toHashCode();
  } // public int hashCode()


  /**
   * @since   1.2.0
   */ 
  public void setCreateTime(long createTime) {
    this.createTime = createTime;
  
  }


  /**
   * @since   1.2.0
   */ 
  public void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  
  }


  /**
   * @since   1.2.0
   */ 
  public void setFields(Set fields) {
    this.fields = fields;
  
  }


  /**
   * @since   1.2.0
   */ 
  public void setId(String id) {
    this.id = id;
  
  }


  /**
   * @since   1.2.0
   */ 
  public void setIsAssignable(boolean isAssignable) {
    this.isAssignable = isAssignable;
  
  }


  /**
   * @since   1.2.0
   */ 
  public void setIsInternal(boolean isInternal) {
    this.isInternal = isInternal;
  
  }


  /**
   * @since   1.2.0
   */ 
  public void setName(String name) {
    this.name = name;
  
  }


  /**
   * @since   1.2.0
   */ 
  public void setUuid(String uuid) {
    this.uuid = uuid;
  
  }


  /**
   * @since   1.2.0
   */
  public String toString() {
    return new ToStringBuilder(this)
      .append( "creatorUuid",  this.getCreatorUuid()  )
      .append( "createTime",   this.getCreateTime()   )
      .append( "fields",       this.getFields()       )
      .append( "isAssignable", this.getIsAssignable() )
      .append( "isInternal",   this.getIsInternal()   )
      .append( "name",         this.getName()         )
      .append( "uuid",         this.getUuid()         )
      .toString();
  } // public String toString() 

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostDelete(HibernateSession hibernateSession) {

    super.onPostDelete(hibernateSession);
    
    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_COMMIT_DELETE, HooksGroupTypeBean.class, 
        this, GroupType.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_DELETE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_DELETE, false, true);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {

    super.onPostSave(hibernateSession);

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_COMMIT_INSERT, HooksGroupTypeBean.class, 
        this, GroupType.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_INSERT, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_INSERT, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {

    super.onPostUpdate(hibernateSession);

    GrouperHooksUtils.schedulePostCommitHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_COMMIT_UPDATE, HooksGroupTypeBean.class, 
        this, GroupType.class);

    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_UPDATE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_UPDATE, true, false);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {

    super.onPreDelete(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_PRE_DELETE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_DELETE, false, false);
  
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_PRE_INSERT, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_INSERT, false, false);
  
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(this, GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_PRE_UPDATE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_UPDATE, false, false);
  }

  /**
   * save the state when retrieving from DB
   * @return the dbVersion
   */
  @Override
  public GroupType dbVersion() {
    return (GroupType)this.dbVersion;
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
  public GroupType clone() {
    return GrouperUtil.clone(this, CLONE_FIELDS);
  }


}
