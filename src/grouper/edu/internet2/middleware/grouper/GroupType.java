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
import  java.io.Serializable;
import  java.util.Date;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  org.apache.commons.lang.time.*;

/** 
 * Schema specification for a Group type.
 * <p/>
 * @author  blair christensen.
 * @version $Id: GroupType.java,v 1.42 2007-03-14 19:10:00 blair Exp $
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
    StopWatch sw = new StopWatch();
    sw.start();
    GroupType type = internal_createType(s, name, true, false);
    sw.stop();
    EventLog.info(s, M.GROUPTYPE_ADD + U.internal_q(type.toString()), sw);
    return type;
  } // public static GroupType createType(s, name)


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
    StopWatch sw = new StopWatch();
    sw.start();
    if ( internal_isSystemType(this) ) {
      String msg = E.GROUPTYPE_NODELSYS + this.getDTO().getName();
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg);
    } 
    if (!RootPrivilegeResolver.internal_isRoot(s)) {
      String msg = E.GROUPTYPE_NODEL;
      ErrorLog.error(GroupType.class, msg);
      throw new InsufficientPrivilegeException(msg);
    }
    try {
      if ( HibernateGroupDAO.findAllByType(this).size() > 0 ) {
        String msg = E.GROUPTYPE_DELINUSE;
        ErrorLog.error(GroupType.class, msg);
        throw new SchemaException(msg);
      }
      // Now delete the type
      String typeName = this.getDTO().getName(); // For logging purposes
      HibernateGroupTypeDAO.delete( this.getDTO(), this.getDTO().getFields() );
      sw.stop();
      EventLog.info(s, M.GROUPTYPE_DEL + U.internal_q(typeName), sw);
      // TODO 20061011 Now update the cached types + fields
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
    if ( HibernateFieldDAO.isInUse(f) ) {
      String msg = E.GROUPTYPE_FIELDNODELINUSE + name;
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg);
    }
    // With validation complete, delete the field
    try {
      Set fields = this.getDTO().getFields();
      if ( fields.remove(f) ) {
        this.getDTO().setFields(fields);
        HibernateGroupTypeDAO.deleteField( f.getDTO() );
        sw.stop();
        EventLog.info(
          s,
          M.GROUPTYPE_DELFIELD + U.internal_q(f.getName()) + " type=" + U.internal_q(this.getDTO().getName()),
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

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof GroupType)) {
      return false;
    }
    return this.getDTO().equals( ( (GroupType) other ).getDTO() );
  } // public boolean equals(other)

  /**
   * Get group fields for this group type.
   * @return  A set of {@link Field} objects.
   */
  public Set getFields() {
    return new LinkedHashSet( Rosetta.getAPI( this.getDTO().getFields() ) );
  } // public Set getFields()

  /**
   * Get group type name.
   * @return  group type name.
   */
  public String getName() {
    return this.getDTO().getName();
  } // public String getName()

  /**
   */
  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  /**
   */
  public String toString() {
    return this.getDTO().toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static GroupType internal_createType(
    GrouperSession s, String name, boolean isAssignable, boolean isInternal)
      throws  InsufficientPrivilegeException,
              SchemaException
  { 
    if (!RootPrivilegeResolver.internal_isRoot(s)) {
      String msg = E.GROUPTYPE_NOADD;
      ErrorLog.error(GroupType.class, msg);
      throw new InsufficientPrivilegeException(msg);
    }
    GroupType type = null;
    try {
      // TODO 20070206 call the dao method
      type = GroupTypeFinder.find(name);  // type already exists
    }
    catch (SchemaException eS) {
      // Type not found.  This is what we want.
    } 
    // TODO 20070206 yuck
    if (type != null) {
      String msg = E.GROUPTYPE_EXISTS + name;
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg);
    }
    GroupTypeDTO dto = new GroupTypeDTO();
    dto.setCreateTime( new Date().getTime() );
    dto.setCreatorUuid( s.getMember().getUuid() );
    dto.setFields( new LinkedHashSet() );
    dto.setIsAssignable(isAssignable);  
    dto.setIsInternal(isInternal);
    dto.setName(name);
    dto.setTypeUuid( GrouperUuid.internal_getUuid() );
    try {
      dto.setId( HibernateGroupTypeDAO.create(dto) );
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUPTYPE_ADD + name + ": " + eDAO.getMessage();
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg, eDAO);
    }
    type = new GroupType();
    type.setDTO(dto);
    return type;
  } // protected static GroupType internal_createType(s, name, isAssignable, isInternal)

  // @since   1.2.0
  // TODO 20070206 rename|relocate
  protected static boolean internal_isSystemType(GroupType type) {
    String name = type.getName();
    if ( (name.equals("base")) || (name.equals("naming")) ) {
      return true;
    }
    return false;
  } // protected static boolean internal_isSystemType(type)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  // TODO 20070206 rename|relocate
  protected Field internal_addField(
    GrouperSession s, String name, FieldType type, Privilege read, Privilege write, boolean required
  )
    throws  InsufficientPrivilegeException,
            SchemaException
  {
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
      FieldDTO dto = new FieldDTO();
      dto.setFieldUuid( GrouperUuid.internal_getUuid() );
      dto.setGroupTypeUuid( this.getDTO().getTypeUuid() );
      dto.setIsNullable(nullable);
      dto.setName(name);
      dto.setReadPrivilege(read);
      dto.setType(type);
      dto.setWritePrivilege(write);

      dto.setId( HibernateGroupTypeDAO.createField(dto) );

      Field f = new Field();
      f.setDTO(dto);

      Set fields = this.getDTO().getFields();
      fields.add(f);
      this.getDTO().setFields(fields);

      sw.stop();
      EventLog.info(
        s, 
        M.GROUPTYPE_ADDFIELD + U.internal_q(f.getName()) + " ftype=" + U.internal_q(type.toString()) 
        + " gtype=" + U.internal_q(this.getDTO().getName()),
        sw
      );
      return f;
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUPTYPE_FIELDADD + name + ": " + eDAO.getMessage();
      ErrorLog.error(GroupType.class, msg);
      throw new SchemaException(msg, eDAO);
    }
  } // protected Field internal_addField(s, name, type, read, write, required)

  // @since   1.2.0
  protected GroupTypeDTO getDTO() {
    return (GroupTypeDTO) super.getDTO();
  } // protected GroupTypeDTO getDTO()


} // public class GroupType extends GrouperAPI implements Serializable

