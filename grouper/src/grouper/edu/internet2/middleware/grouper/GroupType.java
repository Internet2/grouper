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

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.hooks.GroupTypeHooks;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHooksUtils;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GroupTypeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.Quote;

/** 
 * Schema specification for a Group type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupType.java,v 1.59 2008-06-29 17:42:41 mchyzer Exp $
 */
public class GroupType extends GrouperAPI implements Serializable {

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
    GroupType type = internal_createType(s, name, true, false);
    sw.stop();
    EventLog.info(s, M.GROUPTYPE_ADD + Quote.single( type.getName() ), sw);
    return type;
  }


  private long    createTime;
  // PRIVATE INSTANCE VARIABLES //
  private String  creatorUUID;
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
    return this.internal_addField(s, name, FieldType.ATTRIBUTE, read, write, required);
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
    return this.internal_addField(s, name, FieldType.LIST, read, write, false);
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
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg);
    } 
    if (!PrivilegeHelper.isRoot(s)) {
      String msg = E.GROUPTYPE_NODEL;
      ErrorLog.error(GroupType.class, msg);
      throw new InsufficientPrivilegeException(msg);
    }
    try {
      if ( GrouperDAOFactory.getFactory().getGroup().findAllByType( this ).size() > 0 ) {
        String msg = E.GROUPTYPE_DELINUSE;
        ErrorLog.error(GroupType.class, msg);
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
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg, eDAO);
    }
  } // public void delete(s)

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
      ErrorLog.error(GroupType.class, msg);
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
        ErrorLog.error(GroupType.class, msg);
        throw new SchemaException(msg);
      }
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUPTYPE_FIELDDEL + eDAO.getMessage();
      ErrorLog.error(GroupType.class, msg);
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
  

  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static GroupType internal_createType(
    GrouperSession s, String name, boolean isAssignable, boolean isInternal)
      throws  InsufficientPrivilegeException,
              SchemaException
  { 
    //note, no need for GrouperSession inverse of control
    if (!PrivilegeHelper.isRoot(s)) {
      String msg = E.GROUPTYPE_NOADD;
      ErrorLog.error(GroupType.class, msg);
      throw new InsufficientPrivilegeException(msg);
    }
    GroupTypeDAO dao = GrouperDAOFactory.getFactory().getGroupType();
    if ( dao.existsByName(name) ) {
      String msg = E.GROUPTYPE_EXISTS + name;
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg);
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
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg, eDAO);
    }
    return _gt;
  } // protected static GroupType internal_createType(s, name, isAssignable, isInternal)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected Field internal_addField(
    GrouperSession s, String name, FieldType type, Privilege read, Privilege write, boolean required
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    //note, no need for GrouperSession inverse of control
    StopWatch sw  = new StopWatch();
    sw.start();
    AddFieldToGroupTypeValidator v = AddFieldToGroupTypeValidator.validate(name);
    if (v.isInvalid()) {
      throw new SchemaException( v.getErrorMessage() );
    }

    try {
      boolean nullable = true;
      if (required == true) {
        nullable = false;
      }
      Field _f = new Field();
      _f.setGroupTypeUuid( this.getUuid() );
      _f.setIsNullable(nullable);
      _f.setName(name);
      _f.setReadPrivilege(read);
      _f.setType(type);
      _f.setUuid( GrouperUuid.getUuid() );
      _f.setWritePrivilege(write);
        
      GrouperDAOFactory.getFactory().getGroupType().createField(_f);

      Set fields = this.getFields();
      fields.add( _f );

      sw.stop();
      EventLog.info(
        s, 
        M.GROUPTYPE_ADDFIELD + Quote.single(_f.getName()) + " ftype=" + Quote.single(type.toString()) 
        + " gtype=" + Quote.single( this.getName() ),
        sw
      );
      return _f;
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUPTYPE_FIELDADD + name + ": " + eDAO.getMessage();
      ErrorLog.error(GroupType.class, msg);
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
    GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_DELETE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_DELETE);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostSave(HibernateSession hibernateSession) {
    super.onPostSave(hibernateSession);
    GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_INSERT, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_INSERT);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPostUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPostUpdate(HibernateSession hibernateSession) {
    super.onPostUpdate(hibernateSession);
    GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_POST_UPDATE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_POST_UPDATE);
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreDelete(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreDelete(HibernateSession hibernateSession) {
    super.onPreDelete(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_PRE_DELETE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_DELETE);
  
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreSave(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreSave(HibernateSession hibernateSession) {
    super.onPreSave(hibernateSession);
    
    
    GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_PRE_INSERT, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_INSERT);
  
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperAPI#onPreUpdate(edu.internet2.middleware.grouper.hibernate.HibernateSession)
   */
  @Override
  public void onPreUpdate(HibernateSession hibernateSession) {
    super.onPreUpdate(hibernateSession);
    
    GrouperHooksUtils.callHooksIfRegistered(GrouperHookType.GROUP_TYPE, 
        GroupTypeHooks.METHOD_GROUP_TYPE_PRE_UPDATE, HooksGroupTypeBean.class, 
        this, GroupType.class, VetoTypeGrouper.GROUP_TYPE_PRE_UPDATE);
  }

}
