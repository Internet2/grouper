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
import  edu.internet2.middleware.subject.*;
import  java.util.Date;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * A group within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Group.java,v 1.149 2007-04-05 14:28:28 blair Exp $
 */
public class Group extends GrouperAPI implements Owner {

  // PRIVATE CLASS CONSTANTS //
  private static final EventLog EL            = new EventLog();
  private static final String   KEY_CREATOR   = "creator";  // for state caching // TODO 20070322 deprecate
  private static final String   KEY_MEMBER    = "member";   // for state caching  
  private static final String   KEY_MODIFIER  = "modifier"; // for state caching // TODO 20070322 deprecate
  private static final String   KEY_SUBJECT   = "subject";  // for state caching


  // PRIVATE INSTANCE VARIABLES //
  private SimpleCache stateCache = new SimpleCache();

  
  // PUBLIC CLASS METHODS //
  
  /**
   * Retrieve default members {@link Field}.
   * <pre class="eg">
   * Field members = Group.getDefaultList();
   * </pre>
   * @return  The "members" {@link Field}
   * @throws  GrouperRuntimeException
   */
  public static Field getDefaultList() 
    throws  GrouperRuntimeException
  {
    try {
      return FieldFinder.find(GrouperConfig.LIST);
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public static Field getDefaultList()

 
  // PUBLIC INSTANCE METHODS //

  /**
   * Add a composite membership to this group.
   * <pre class="eg">
   * try {
   *   g.addCompositeMember(CompositeType.UNION, leftGroup, rightGroup);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add composite membership
   * } 
   * </pre>
   * @param   type  Add membership of this {@link CompositeType}.
   * @param   left  {@link Group} that is left factor of of composite membership.
   * @param   right {@link Group} that is right factor of composite membership.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @since   1.0
   */
  public void addCompositeMember(CompositeType type, Group left, Group right)
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    try {
      StopWatch sw  = new StopWatch();
      sw.start();

      PrivilegeResolver.internal_canPrivDispatch( 
        this.getSession(), this, this.getSession().getSubject(), Group.getDefaultList().getWritePriv() 
      );

      Composite     c   = new Composite();
      CompositeDTO  _c  = new CompositeDTO();
      _c.setCreateTime( new Date().getTime() );
      _c.setCreatorUuid( this.getSession().getMember().getUuid() );
      _c.setFactorOwnerUuid( this.getDTO().getUuid() );
      _c.setLeftFactorUuid( left.getDTO().getUuid() );
      _c.setRightFactorUuid( right.getDTO().getUuid() );
      _c.setType( type.toString() );
      _c.setUuid( GrouperUuid.internal_getUuid() );
      CompositeValidator vComp = CompositeValidator.validate(_c);
      if (vComp.isInvalid()) {
        throw new MemberAddException( vComp.getErrorMessage() );
      }
      c.setDTO(_c);
      c.setSession( this.getSession() );

      AddCompositeMemberValidator vAdd = AddCompositeMemberValidator.validate(this);
      if (vAdd.isInvalid()) {
        throw new MemberAddException( vAdd.getErrorMessage() );
      }

      MemberOf mof = new MemberOf();
      mof.addComposite( this.getSession(), this, c );
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      EventLog.groupAddComposite( this.getSession(), c, mof, sw );
      Composite.internal_update(this);
      sw.stop();
    }
    catch (GrouperDAOException eDAO) {
      throw new MemberAddException( eDAO.getMessage(), eDAO );
    }
    catch (SchemaException eS) {
      throw new MemberAddException(eS);
    }
  } // public void addCompositeMember(type, left, right)

  /**
   * Add a subject to this group.
   * <pre class="eg">
   * try {
   *   g.addMember(subj);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add subject
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public void addMember(Subject subj) 
    throws  InsufficientPrivilegeException,
            MemberAddException
  {
    try {
      this.addMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      throw new MemberAddException(eS.getMessage(), eS);
    }
  } // public void addMember(subj)

  /**
   * Add a subject to this group.
   * <pre class="eg">
   * try {
   *   g.addMember(subj, f);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException eMA) {
   *   // Unable to add member
   * } 
   * catch (SchemaException eS) {
   *   // Invalid Field
   * } 
   * </pre>
   * @param   subj  Add this {@link Subject}
   * @param   f     Add subject to this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   * @throws  SchemaException
   */
  public void addMember(Subject subj, Field f)
    throws  InsufficientPrivilegeException,
            MemberAddException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( !FieldType.LIST.equals( f.getType() ) ) {
      throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
    }
    if ( !this.canWriteField(f) ) { 
      GrouperValidator v = CanOptinValidator.validate(this, subj, f);
      if (v.isInvalid()) {
        throw new InsufficientPrivilegeException();
      }
    }
    if ( ( Group.getDefaultList().equals(f) ) && ( this.hasComposite() ) ) {
      throw new MemberAddException(E.GROUP_AMTC);
    }
    Membership.internal_addImmediateMembership( this.getSession(), this, subj, f );
    EL.groupAddMember(this.getSession(), this.getName(), subj, f, sw);
    Composite.internal_update(this);
    sw.stop();
  } // public void addMember(subj, f)

  /**
   * Add an additional group type.
   * <pre class="eg">
   * try {
   *   GroupType custom = GroupTypeFinder.find("custom type");
   *   g.addType(custom);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to add type 
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add type
   * }
   * catch (SchemaException eS) {
   *   // Cannot add system-maintained types
   * }
   * </pre>
   * @param   type  The {@link GroupType} to add.
   * @throws  GroupModifyException if unable to add type.
   * @throws  InsufficientPrivilegeException if subject not root-like.
   * @throws  SchemaException if attempting to add a system group type.
   */
  public void addType(GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( this.hasType(type ) ) {
      throw new GroupModifyException(E.GROUP_HAS_TYPE);
    }
    if ( type.isSystemType() ) {
      throw new SchemaException("cannot edit system group types");
    }
    if ( !PrivilegeResolver.internal_canADMIN( this.getSession(), this, this.getSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
    }
    try {
      Set types = this.getDTO().getTypes();
      types.add( type.getDTO() );
      this.getDTO().setTypes(types);

      this.internal_setModified();

      GrouperDAOFactory.getFactory().getGroup().addType(this, type);
      sw.stop();
      EventLog.info(
        this.getSession(),
        M.GROUP_ADDTYPE + U.internal_q(this.getName()) + " type=" + U.internal_q(type.toString()),
        sw
      );
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.GROUP_TYPEADD + type + ": " + eDAO.getMessage();
      ErrorLog.error(Group.class, msg);
      throw new GroupModifyException(msg, eDAO); 
    }
  } // public void addType(type)

  /**
   * Check whether the {@link Subject} that loaded this {@link Group} can
   * read the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canReadField(f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   f   Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can read {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Field}
   * @throws  SchemaException if invalid {@link Field}
   * @since   1.0
   */
  public boolean canReadField(Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    return this.canReadField(this.getSession().getSubject(), f);
  } // public boolean canReadField(f)

  /**
   * Check whether the specified {@link Subject} can read the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canReadField(subj, f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   subj  Check privileges for this {@link Subject}.
   * @param   f     Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can read {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Subject} or {@link Field}
   * @throws  SchemaException if invalid {@link Field} or {@link Subject}.
   * @since   1.2.0
   */
  public boolean canReadField(Subject subj, Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    GrouperValidator v = NotNullValidator.validate(subj);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "subject: " + v.getErrorMessage() );
    }
    v = NotNullValidator.validate(f);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "field: " + v.getErrorMessage() );
    }
    v = FieldTypeValidator.validate(f);
    if (v.isInvalid()) {
      throw new SchemaException( v.getErrorMessage() );
    }
    if ( !this.hasType( f.getGroupType() ) ) {
      throw new SchemaException(E.INVALID_GROUP_TYPE + f.getGroupType().toString());
    }
    try {
      PrivilegeResolver.internal_canPrivDispatch( this.getSession(), this, subj, f.getReadPriv() );
      return true;
    }
    catch (InsufficientPrivilegeException eIP) {
      return false;
    }
  } // public boolean canReadField(subj, f)

  /**
   * Check whether the {@link Subject} that loaded this {@link Group} can
   * write the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canWriteField(f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   f   Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can write {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Field}
   * @throws  SchemaException if invalid {@link Field}
   * @since   1.0
   */
  public boolean canWriteField(Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    return this.canWriteField(this.getSession().getSubject(), f);
  } // public boolean canWriteField(f)

  /**
   * Check whether the specified {@link Subject} can write the specified {@link Field}.
   * <pre class="eg">
   * try {
   *   boolean rv = g.canWriteField(subj, f);
   * }
   * catch (SchemaException eS) {
   *   // invalid field
   * }
   * </pre>
   * @param   subj  Check privileges for this {@link Subject}.
   * @param   f     Check privileges on this {@link Field}.
   * @return  True if {@link Subject} can write {@link Field}, false otherwise.
   * @throws  IllegalArgumentException if null {@link Subject} or {@link Field}
   * @throws  SchemaException if invalid {@link Field}
   * @since   1.0
   */
  public boolean canWriteField(Subject subj, Field f) 
    throws  IllegalArgumentException,
            SchemaException
  {
    return this.internal_canWriteField(subj, f, FieldType.LIST);
  } // public boolean canWriteField(subj, f)

  /**
   * Delete this group from the Groups Registry.
   * <pre class="eg">
   * try {
   *   g.delete();
   * }
   * catch (GroupDeleteException e0) {
   *   // Unable to delete group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this group
   * }
   * </pre>
   * @throws  GroupDeleteException
   * @throws  InsufficientPrivilegeException
   */
  public void delete() 
    throws  GroupDeleteException,
            InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    GrouperSession.validate( this.getSession() );
    if ( !PrivilegeResolver.internal_canADMIN( this.getSession(), this, this.getSession().getSubject() ) )
    {
      throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
    }
    try {
      // Revoke all access privs
      this._revokeAllAccessPrivs();
      // ... And delete composite mship if it exists
      if (this.hasComposite()) {
        this.deleteCompositeMember();
      }
      // ... And delete all memberships - as root
      Set deletes = new LinkedHashSet(
        Membership.internal_deleteAllFieldType(
          this.getSession().getDTO().getRootSession(), this, FieldType.LIST
        )
      );
      //deletes.add(this);            // ... And add the group last for good luck    
      String name = this.getName(); // Preserve name for logging
      GrouperDAOFactory.getFactory().getGroup().delete( this.getDTO(), deletes );
      //HibernateGroupDAO.delete(deletes);
      sw.stop();
      EventLog.info(this.getSession(), M.GROUP_DEL + U.internal_q(name), sw);
    }
    catch (GrouperDAOException eDAO) {
      throw new GroupDeleteException( eDAO.getMessage(), eDAO );
    }
    catch (MemberDeleteException eMD) {
      throw new GroupDeleteException(eMD.getMessage(), eMD);
    }
    catch (RevokePrivilegeException eRP) {
      throw new GroupDeleteException(eRP.getMessage(), eRP);
    }
    catch (SchemaException eS) {
      throw new GroupDeleteException(eS.getMessage(), eS);
    }
  } // public void delete()

  /**
   * Delete a group attribute.
   * <pre class="eg">
   * try {
   *   g.deleteAttribute(attribute);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to delete this attribute
   * }
   * </pre>
   * @param   attr  Delete this attribute.
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void deleteAttribute(String attr) 
    throws  AttributeNotFoundException,
            GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      StopWatch sw = new StopWatch();
      sw.start();
      NotNullOrEmptyValidator v = NotNullOrEmptyValidator.validate(attr);
      if (v.isInvalid()) {
        throw new AttributeNotFoundException(E.INVALID_ATTR_NAME + attr);
      }
      Field f = FieldFinder.find(attr);
      if (f.getRequired()) {
        throw new GroupModifyException( E.GROUP_DRA + f.getName() );
      }
      if ( !this.canWriteField(f) ) {
        throw new InsufficientPrivilegeException();
      }

      Map attrs = this.getDTO().getAttributes();
      if (attrs.containsKey(attr)) {
        String val = (String) attrs.get(attr); // for logging
        attrs.remove(attr);
        this.getDTO().setAttributes(attrs);
        this.internal_setModified();
        HibernateGroupDAO.update(this); // TODO 20070316 this should probably call a smarter method
        sw.stop();
        EL.groupDelAttr(this.getSession(), this.getName(), attr, val, sw);
      }
      else {
        throw new AttributeNotFoundException();
      }
    }
    catch (GrouperDAOException eDAO) {
      throw new GroupModifyException( eDAO.getMessage(), eDAO );
    }
    catch (InsufficientPrivilegeException eIP) {
      throw eIP;
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException(eS.getMessage(), eS);
    }
  } // public void deleteAttribute(attr)
  
  /**
   * Delete a {@link Composite} membership from this group.
   * <pre class="eg">
   * try {
   *   g.deleteCompositeMember();
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete members
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete composite membership
   * } 
   * </pre>
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @since   1.0
   */
  public void deleteCompositeMember()
    throws  InsufficientPrivilegeException,
            MemberDeleteException
  {
    try {
      StopWatch sw  = new StopWatch();
      sw.start();
      if ( !this.canWriteField( this.getSession().getSubject(), Group.getDefaultList() ) ) {
        throw new InsufficientPrivilegeException();
      }
      if ( !this.hasComposite() ) {
        throw new MemberDeleteException(E.GROUP_DCFC); 
      }
      Composite c   = new Composite();
      c.setDTO( GrouperDAOFactory.getFactory().getComposite().findAsOwner( this.getDTO() ) );
      MemberOf  mof = new MemberOf();
      mof.deleteComposite( this.getSession(), this, c );
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      EventLog.groupDelComposite( this.getSession(), c, mof, sw );
      Composite.internal_update(this);
      sw.stop();
    }
    catch (CompositeNotFoundException eCNF) {
      throw new MemberDeleteException(eCNF);
    }
    catch (GrouperDAOException eDAO) {
      throw new MemberDeleteException( eDAO.getMessage(), eDAO );
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS);
    }
  } // public void deleteCompositeMember()

  /** 
   * Delete a subject from this group.
   * <pre class="eg">
   * try {
   *   g.deleteMember(subj);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public void deleteMember(Subject subj)
    throws  InsufficientPrivilegeException,
            MemberDeleteException
  {
    try {
      this.deleteMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS.getMessage(), eS);
    }
  } // public void deleteMember(subj)

  /** 
   * Delete a subject from this group.
   * <pre class="eg">
   * try {
   *   g.deleteMember(m, f);
   * } 
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to delete this subject
   * }
   * catch (MemberDeleteException eMD) {
   *   // Unable to delete subject
   * }
   * </pre>
   * @param   subj  Delete this {@link Subject}.
   * @param   f     Delete subject from this {@link Field}.
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   * @throws  SchemaException
   */
  public void deleteMember(Subject subj, Field f) 
    throws  InsufficientPrivilegeException, 
            MemberDeleteException,
            SchemaException
  {
    StopWatch sw  = new StopWatch();
    sw.start();
    if ( !FieldType.LIST.equals( f.getType() ) ) {
      throw new SchemaException( E.FIELD_INVALID_TYPE + f.getType() );
    }
    if ( !this.canWriteField(f) ) {
      GrouperValidator v = CanOptoutValidator.validate(this, subj, f);
      if (v.isInvalid()) {
        throw new InsufficientPrivilegeException();
      }
    }
    if ( (f.equals( Group.getDefaultList() ) ) && ( this.hasComposite() ) ) {
      throw new MemberDeleteException(E.GROUP_DMFC);
    }
    MemberOf  mof = Membership.internal_delImmediateMembership( this.getSession(), this, subj, f );
    try {
      GrouperDAOFactory.getFactory().getMembership().update(mof);
    }
    catch (GrouperDAOException eDAO) {
      throw new MemberDeleteException( eDAO.getMessage(), eDAO );
    }
    sw.stop();
    EL.groupDelMember(this.getSession(), this.getName(), subj, f, sw);
    EL.delEffMembers(this.getSession(), this, subj, f, mof.internal_getEffDeletes());
    Composite.internal_update(this);
  } // public void deleteMember(subj, f)

  /**
   * Delete a group type.
   * <pre class="eg">
   * try {
   *   GroupType custom = GroupTypeFinder.find("custom type");
   *   g.deleteType(custom);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to delete type 
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add type
   * }
   * catch (SchemaException eS) {
   *   // Cannot delete system-maintained types
   * }
   * </pre>
   * @param   type  The {@link GroupType} to add.
   * @throws  GroupModifyException if unable to delete type.
   * @throws  InsufficientPrivilegeException if subject not root-like.
   * @throws  SchemaException if attempting to delete a system group type.
   */
  public void deleteType(GroupType type) 
    throws  GroupModifyException,
            InsufficientPrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    String msg = E.GROUP_TYPEDEL + type + ": "; 
    try {
      if ( !this.hasType(type) ) {
        throw new GroupModifyException("does not have type");
      }
      if ( type.isSystemType() ) {
        throw new SchemaException("cannot edit system group types");
      }
      if ( !PrivilegeResolver.internal_canADMIN( this.getSession(), this, this.getSession().getSubject() ) ) {
        throw new InsufficientPrivilegeException(E.CANNOT_ADMIN);
      }

      Set types = this.getDTO().getTypes();
      types.remove( type.getDTO() );
      this.getDTO().setTypes(types);

      this.internal_setModified();

      GrouperDAOFactory.getFactory().getGroup().deleteType(this, type);
      sw.stop();
      EventLog.info(
        this.getSession(),
        M.GROUP_DELTYPE + U.internal_q(this.getName()) + " type=" + U.internal_q(type.toString()),
        sw
      );
    }
    catch (GrouperDAOException eDAO) {
      msg += eDAO.getMessage();
      ErrorLog.error(Group.class, msg);
      throw new GroupModifyException(msg, eDAO);
    }
  } // public void deleteType(type)

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Group)) {
      return false;
    }
    return this.getDTO().equals( ( (Group) other ).getDTO() );
  } // public boolean equals(other)

  /**
   * Get subjects with the ADMIN privilege on this group.
   * <pre class="eg">
   * Set admins = g.getAdmins();
   * </pre>
   * @return  Set of subjects with ADMIN
   * @throws  GrouperRuntimeException
   */
  public Set getAdmins() 
    throws  GrouperRuntimeException
  {
    try {
      return PrivilegeResolver.internal_getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.ADMIN
      );
    }
    catch (SchemaException eS) {
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.ADMIN;
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getAdmins()

  /**
   * Get attribute value.
   * <pre class="eg">
   * try {
   *   String value = g.getAttribute(attribute);
   * }
   * catch (AttributeNotFoundException e) {
   *   // Group doesn't have attribute
   * }
   * </pre>
   * @param   attr  Get value of this attribute.
   * @return  Attribute value.
   * @throws  AttributeNotFoundException
   */
  public String getAttribute(String attr) 
    throws  AttributeNotFoundException
  {
    try {
      Field f = FieldFinder.find(attr);
      if ( !FieldType.ATTRIBUTE.equals( f.getType() ) ) {
        throw new AttributeNotFoundException( E.FIELD_INVALID_TYPE + f.getType() );
      }
      GrouperValidator v = GetGroupAttributeValidator.validate(this, f);
      if (v.isInvalid()) {
        throw new AttributeNotFoundException( v.getErrorMessage() );
      }
      String  val   = GrouperConfig.EMPTY_STRING;
      Map     attrs = this.getDTO().getAttributes();
      if (attrs.containsKey(attr)) {
        return (String) attrs.get(attr);
      }
      return val;
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException( eS.getMessage() );
    }
  } // public String getAttribute(attr)

  /**
   * Get all attributes and values.
   * <pre class="eg">
   * Map attributes = g.getAttributes();
   * </pre>
   * @return  A map of attributes and values.
   */
  public Map getAttributes() {
    Map       filtered  = new HashMap();
    Map.Entry kv;
    Iterator  it        = this.getDTO().getAttributes().entrySet().iterator();
    while (it.hasNext()) {
      kv = (Map.Entry) it.next();
      if ( this._canReadField( (String) kv.getKey() ) ) {
        filtered.put( (String) kv.getKey(), (String) kv.getValue() );
      }
    }
    return filtered;
  } // public Map getAttributes()

  /**
   * Get {@link Composite} {@link Member}s of this group.
   * <pre class="eg">
   * Set members = g.getCompositeMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @since   1.0
   */
  public Set getCompositeMembers() {
    return MembershipFinder.internal_findMembersByType(
      this.getSession(), this, Group.getDefaultList(), Membership.COMPOSITE
    );
  } // public Set getCompositeMembers()

  /**
   * Get {@link Composite} {@link Membership}s of this group.
   * <pre class="eg">
   * Set mships = g.getCompositeMembers();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @since   1.0
   */
  public Set getCompositeMemberships() {
    return MembershipFinder.internal_findAllByOwnerAndFieldAndType(
      this.getSession(), this, Group.getDefaultList(), Membership.COMPOSITE
    );
  } // public Set getCompositeMemberships()

  /**
   * Get (optional and questionable) create source for this group.
   * <pre class="eg">
   * // Get create source
`  * String source = g.getCreateSource();
   * </pre>
   * @return  Create source for this group.
   */
  public String getCreateSource() {
    return GrouperConfig.EMPTY_STRING;
  } // public String getCreateSource()
  
  /**
   * Get subject that created this group.
   * <pre class="eg">
   * // Get creator of this group.
   * try {
   *   Subject creator = g.getCreateSubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that created this group.
   * @throws  SubjectNotFoundException
   */
  public Subject getCreateSubject() 
    throws SubjectNotFoundException
  {
    if ( this.stateCache.containsKey(KEY_CREATOR) ) {
      return (Subject) this.stateCache.get(KEY_CREATOR);
    }
    try {
      // when called from "GrouperSubject" there is no attached session
      MemberDTO _m = GrouperDAOFactory.getFactory().getMember().findByUuid( this.getDTO().getCreatorUuid() );
      this.stateCache.put(
        KEY_CREATOR, SubjectFinder.findById( _m.getSubjectId(), _m.getSubjectTypeId(), _m.getSubjectSourceId() )
      );
      return (Subject) this.stateCache.get(KEY_CREATOR);
    }
    catch (MemberNotFoundException eMNF) {
      throw new SubjectNotFoundException( eMNF.getMessage(), eMNF );
    }
    catch (SourceUnavailableException eSU) {
      throw new SubjectNotFoundException( eSU.getMessage(), eSU );
    }
    catch (SubjectNotUniqueException eSNU) {
      throw new SubjectNotFoundException( eSNU.getMessage(), eSNU );
    }
  } // public Subject getCreateSubject() 
  
  /**
   * Get creation time for this group.
   * <pre class="eg">
   * // Get create time.
   * Date created = g.getCreateTime();
   * </pre>
   * @return  {@link Date} that this group was created.
   */
  public Date getCreateTime() {
    return new Date(this.getDTO().getCreateTime());
  } // public Date getCreateTime()

  /**
   * Get group description.
   * <pre class="eg">
   * String description = g.getDescription();
   * </pre>
   * @return  Group description.
   */
  public String getDescription() {
    try {
      return (String) this.getAttribute(GrouperConfig.ATTR_D);
    }
    catch (AttributeNotFoundException eANF) {
      // Lack of a description is acceptable
      return GrouperConfig.EMPTY_STRING;
    }
  } // public String getDescription()

  /**
   * Get group displayExtension.
   * <pre class="eg">
   * String displayExtn = g.getDisplayExtension();
   * </pre>
   * @return  Gruop displayExtension.
   * @throws  GrouperRuntimeException
   */
  public String getDisplayExtension() 
    throws  GrouperRuntimeException
  {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = (String) this.getDTO().getAttributes().get(GrouperConfig.ATTR_DE);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      ErrorLog.fatal(Group.class, E.GROUP_NODE);
      throw new GrouperRuntimeException(E.GROUP_NODE);
    }
    return val;
  } // public String getDisplayExtension()

  /**
   * Get group displayName.
   * <pre class="eg">
   * String displayName = g.getDisplayName();
   * </pre>
   * @return  Group displayName.
   * @throws  GrouperRuntimeException
   */
  public String getDisplayName() 
    throws  GrouperRuntimeException
  {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = (String) this.getDTO().getAttributes().get(GrouperConfig.ATTR_DN);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      ErrorLog.fatal(Group.class, E.GROUP_NODN);
      throw new GrouperRuntimeException(E.GROUP_NODN);
    }
    return val;
  } // public String getDisplayName()

  /**
   * Get effective members of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getEffectiveMembers() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getEffectiveMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getEffectiveMembership()


  /**
   * Get effective members of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set getEffectiveMembers(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findMembersByType(this.getSession(), this, f, Membership.EFFECTIVE);
  }  // public Set getEffectiveMembers(f)

  /**
   * Get effective memberships of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getEffectiveMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getEffectiveMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getEffectiveMembership()

  /**
   * Get effective memberships of this group.
   * <pre class="eg">
   * Set memberships = g.getEffectiveMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getEffectiveMemberships(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findAllByOwnerAndFieldAndType(
      this.getSession(), this, f, Membership.EFFECTIVE
    );
  } // public Set getEffectiveMemberships(f)

  /**
   * Get group extension.
   * <pre class="eg">
   * String extension = g.getExtension();
   * </pre>
   * @return  Group extension.
   * @throws  GrouperRuntimeException
   */
  public String getExtension() {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = (String) this.getDTO().getAttributes().get(GrouperConfig.ATTR_E);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      ErrorLog.error(Group.class, E.GROUP_NOE);
      throw new GrouperRuntimeException(E.GROUP_NOE);
    }
    return val;
  } // public String getExtension()
 
  /**
   * Get immediate members of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getImmediateMembers() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getImmediateMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getImmediateMembers()

  /**
   * Get immediate members of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set getImmediateMembers(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findMembersByType(
      this.getSession(), this, f, Membership.IMMEDIATE
    );
  } // public Set getImmediateMembers(f)

  /**
   * Get immediate memberships of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getImmediateMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getImmediateMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getImmediateMemberships()

  /**
   * Get immediate memberships of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getImmediateMemberships(Field f) 
    throws  SchemaException
  {
    GrouperSession.validate(this.getSession());
    return MembershipFinder.internal_findAllByOwnerAndFieldAndType(
      this.getSession(), this, f, Membership.IMMEDIATE
    );
  } // public Set getImmediateMemberships(f)

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getMembers() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getMembers(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getMembers()

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers(f);
   * </pre>
   * @param   f Get members in this list field.
   * @return  A set of {@link Member} objects.
   * @throws  SchemaException
   */
  public Set getMembers(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.internal_findMembers( this.getSession(), this, f );
  } // public Set getMembers(f)

  /**
   * Get memberships of this group.
   * <pre class="eg">
   * Set memberships = g.getMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getMemberships(getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getMemberships()

  /**
   * Get memberships of this group.
   * <pre class="eg">
   * Set memberships = g.getMemberships(f);
   * </pre>
   * @param   f Get memberships in this list field.
   * @return  A set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getMemberships(Field f) 
    throws  SchemaException
  {
    return new LinkedHashSet( 
      PrivilegeResolver.internal_canViewMemberships( 
        this.getSession(), GrouperDAOFactory.getFactory().getMembership().findAllByOwnerAndField( this.getUuid(), f )
      )
    );
  } // public Set getMemberships(f)

  /**
   * Get (optional and questionable) modify source for this group.
   * <pre class="eg">
`  * String source = g.getModifySource();
   * </pre>
   * @return  Modify source for this group.
   */
  public String getModifySource() {
    return GrouperConfig.EMPTY_STRING;
  } // public String getModifySource()
  
  /**
   * Get subject that last modified this group.
   * <pre class="eg">
   * try {
   *   Subject modifier = g.getModifySubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that last modified this group.
   * @throws  SubjectNotFoundException
   */
  public Subject getModifySubject() 
    throws SubjectNotFoundException
  {
    if ( this.stateCache.containsKey(KEY_MODIFIER) ) {
      return (Subject) this.stateCache.get(KEY_MODIFIER);
    }
    if ( this.getDTO().getModifierUuid() == null ) {
      throw new SubjectNotFoundException("group has not been modified");
    }
    try {
      // when called from "GrouperSubject" there is no attached session
      MemberDTO _m = GrouperDAOFactory.getFactory().getMember().findByUuid( this.getDTO().getModifierUuid() );
      this.stateCache.put(
        KEY_MODIFIER, SubjectFinder.findById( _m.getSubjectId(), _m.getSubjectTypeId(), _m.getSubjectSourceId() )
      );
      return (Subject) this.stateCache.get(KEY_MODIFIER);
    }
    catch (MemberNotFoundException eMNF) {
      throw new SubjectNotFoundException( eMNF.getMessage(), eMNF );
    }
    catch (SourceUnavailableException eSU) {
      throw new SubjectNotFoundException( eSU.getMessage(), eSU );
    }
    catch (SubjectNotUniqueException eSNU) {
      throw new SubjectNotFoundException( eSNU.getMessage(), eSNU );
    }  
  } // public Subject getModifySubject()
  
  /**
   * Get last modified time for this group.
   * <pre class="eg">
   * Date modified = g.getModifyTime();
   * </pre>
   * @return  {@link Date} that this group was last modified.
   */
  public Date getModifyTime() {
    return new Date(this.getDTO().getModifyTime());
  }

  /**
   * Get group name.
   * <pre class="eg">
   * String name = g.getName();
   * </pre>
   * @return  Group name.
   * @throws  GrouperRuntimeException
   */
  public String getName() 
    throws  GrouperRuntimeException
  {
    // We don't validate privs here because if one has retrieved a group then one
    // has at least VIEW.
    String val = (String) this.getDTO().getAttributes().get(GrouperConfig.ATTR_N);
    if ( val == null || GrouperConfig.EMPTY_STRING.equals(val) ) {
      //  A group without this attribute is VERY faulty
      ErrorLog.error(Group.class, E.GROUP_NON);
      throw new GrouperRuntimeException(E.GROUP_NON);
    }
    return val;
  } // public String getName()

  /**
   * Get subjects with the OPTIN privilege on this group.
   * <pre class="eg">
   * Set optins = g.getOptins();
   * </pre>
   * @return  Set of subjects with OPTIN
   * @throws  GrouperRuntimeException
   */
  public Set getOptins() 
    throws  GrouperRuntimeException
  {
    try {
      return PrivilegeResolver.internal_getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.OPTIN
      );
    } 
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.OPTIN;
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getOptins()

  /**
   * Get subjects with the OPTOUT privilege on this group.
   * <pre class="eg">
   * Set admins = g.getOptouts();
   * </pre>
   * @return  Set of subjects with OPTOUT
   * @throws  GrouperRuntimeException
   */
  public Set getOptouts() 
    throws  GrouperRuntimeException
  {
    try {
      return PrivilegeResolver.internal_getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.OPTOUT
      );
    } 
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.OPTOUT;
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getOptouts()

  /**
   * Get parent stem.
   * <pre class="eg">
   * Stem parent = g.getParentStem();
   * </pre>
   * @return  Parent {@link Stem}.
   */
  public Stem getParentStem() 
    throws  IllegalStateException
  {
    String uuid = this.getDTO().getParentUuid();
    if (uuid == null) {
      throw new IllegalStateException("group has no parent stem");
    }
    try {
      Stem parent = new Stem();
      parent.setDTO( GrouperDAOFactory.getFactory().getStem().findByUuid(uuid) );
      parent.setSession( this.getSession() );
      return parent;
    }
    catch (StemNotFoundException eShouldNeverHappen) {
      throw new IllegalStateException( 
        "this should never happen: group has no parent stem: " + eShouldNeverHappen.getMessage(), 
        eShouldNeverHappen 
      );
    }
  } // public Stem getParentStem()

  /**
   * Get privileges that the specified subject has on this group.
   * <pre class="eg">
   * Set privs = g.getPrivs(subj);
   * </pre>
   * @param   subj  Get privileges for this subject.
   * @return  Set of {@link AccessPrivilege} objects.
   */
  public Set getPrivs(Subject subj) {
    return PrivilegeResolver.internal_getPrivs(
      this.getSession(), this, subj
    );
  } // public Set getPrivs(subj)


  /**
   * Get subjects with the READ privilege on this group.
   * <pre class="eg">
   * Set readers = g.getReaders();
   * </pre>
   * @return  Set of subjects with READ
   * @throws  GrouperRuntimeException
   */
  public Set getReaders() 
    throws  GrouperRuntimeException
  {
    try {
      return PrivilegeResolver.internal_getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.READ
      );
    } 
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.READ;
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getReaders()

  /**
   * Get removable group types for this group.
   * <pre class="eg">
   * Set types = g.getRemovableTypes();
   * </pre>
   * @return  Set of removable group types.
   * @since   1.0
   */
  public Set getRemovableTypes() {
    Set types = new LinkedHashSet();
    // Must have ADMIN to remove types.
    if (PrivilegeResolver.internal_canADMIN(this.s, this, this.s.getSubject())) {
      GroupType t;
      Iterator  iter  = this.getTypes().iterator();
      while (iter.hasNext()) {
        t = (GroupType) iter.next();
        if ( t.getDTO().getIsAssignable() ) {
          types.add(t);
        }
      }
    }
    return types;
  } // public Set getRemovableTypes()

  /**
   * Get group types for this group.
   * <pre class="eg">
   * Set types = g.getTypes();
   * </pre>
   * @return  Set of group types.
   */
  public Set getTypes() {
    Set       types = new LinkedHashSet();
    Iterator  it    = this.getDTO().getTypes().iterator();
    while (it.hasNext()) {
      GroupTypeDTO dto = (GroupTypeDTO) it.next();
      if ( !dto.getIsInternal() ) {
        GroupType type = new GroupType();
        type.setDTO(dto);
        types.add(type);
      }
    }
    return types;
  } // public Set getTypes()

  /**
   * Get subjects with the UPDATE privilege on this group.
   * <pre class="eg">
   * Set updaters = g.getUpdaters();
   * </pre>
   * @return  Set of subjects with UPDATE
   * @throws  GrouperRuntimeException
   */
  public Set getUpdaters() 
    throws  GrouperRuntimeException
  {
    try {
      return PrivilegeResolver.internal_getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.UPDATE
      );
    } 
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.UPDATE;
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public set getUpdateres()

  /**
   */
  public String getUuid() {
    return this.getDTO().getUuid();
  } // public String getUuid()

  /**
   * Get subjects with the VIEW privilege on this group.
   * <pre class="eg">
   * Set viewers = g.getViewers();
   * </pre>
   * @return  Set of subjects with VIEW
   * @throws  GrouperRuntimeException
   */
  public Set getViewers() 
    throws  GrouperRuntimeException
  {
    try {
      return PrivilegeResolver.internal_getSubjectsWithPriv(
        this.getSession(), this, AccessPrivilege.VIEW
      );
    } 
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.VIEW;
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getViewers()

  /**
   * Grant privilege to a subject on this group.
   * <pre class="eg">
   * try {
   *   g.grantPriv(subj, AccessPrivilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Not privileged to grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   subj  Grant privilege to this subject.
   * @param   priv  Grant this privilege.
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   * @throws  SchemaException
   */
  public void grantPriv(Subject subj, Privilege priv)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    PrivilegeResolver.internal_grantPriv(this.getSession(), this, subj, priv);
    sw.stop();
    EL.groupGrantPriv(this.getSession(), this.getName(), subj, priv, sw);
  }  // public void grantPriv(subj, priv)

  /**
   * Check whether the subject has ADMIN on this group.
   * <pre class="eg">
   * if (g.hasAdmin(subj)) {
   *   // Has ADMIN
   * }
   * else {
   *   // Does not have ADMIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has ADMIN.
   */
  public boolean hasAdmin(Subject subj) {
    return PrivilegeResolver.internal_hasPriv(this.getSession(), this, subj, AccessPrivilege.ADMIN);
  } // public boolean hasAdmin(subj)

  /**
   * Does this {@link Group} have a {@link Composite} membership.
   * <pre class="eg">
   * if (g.hasComposite()) {
   *   // this group has a composite membership
   * }
   * </pre>
   * @return  Boolean true if group has a composite membership.
   */
  public boolean hasComposite() {
    try {
      GrouperDAOFactory.getFactory().getComposite().findAsOwner( this.getDTO() );
      return true;
    }
    catch (CompositeNotFoundException eCNF) {
      return false;
    }
  } // public boolean hasComposite()

  /**
   * Check whether the subject is an effective member of this group.
   * <pre class="eg">
   * if (g.hasEffectiveMember(subj)) {
   *   // Subject is an effective member of this group
   * }
   * else {
   *   // Subject is not an effective member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperRuntimeException
   */
  public boolean hasEffectiveMember(Subject subj) 
    throws  GrouperRuntimeException
  {
    try {
      return this.hasEffectiveMember(subj, getDefaultList());
    } 
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public boolean hasEffectiveMember(Subject subj)

  /**
   * Check whether the subject is an effective member of this group.
   * <pre class="eg">
   * if (g.hasEffectiveMember(subj, f)) {
   *   // Subject is an effective member of this group
   * }
   * else {
   *   // Subject is not an effective member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Check for membership in this list field.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasEffectiveMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(this.getSession(), subj);
      rv = m.isEffectiveMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      ErrorLog.error(Group.class, E.GROUP_HEM + eMNF.getMessage());
    }
    return rv;
  } // public boolean hasEffectiveMember(subj, f)

  /**
   * Check whether the subject is an immediate member of this group.
   * <pre class="eg">
   * if (g.hasImmediateMember(subj)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperRuntimeException
   */
  public boolean hasImmediateMember(Subject subj) 
    throws  GrouperRuntimeException
  {
    try {
      return this.hasImmediateMember(subj, getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public boolean hasImmediateMember(subj)

  /**
   * Check whether the subject is an immediate member of this group.
   * <pre class="eg">
   * if (g.hasImmediateMember(subj, f)) {
   *   // Subject is an immediate member of this group
   * }
   * else {
   *   // Subject is not a immediate member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Check for membership in this list field.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasImmediateMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(this.getSession(), subj);
      rv = m.isImmediateMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      ErrorLog.error(Group.class, E.GROUP_HIM + eMNF.getMessage());
    }
    return rv;
  } // public boolean hasImmediateMember(subj, f)

  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  /**
   * Check whether the subject is a member of this group.
   * <pre class="eg">
   * if (g.hasMember(subj)) {
   *   // Subject is a member of this group
   * }
   * else {
   *   // Subject is not a member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject belongs to this group.
   * @throws  GrouperRuntimeException
   */
  public boolean hasMember(Subject subj) 
    throws  GrouperRuntimeException
  {
    try {
      return this.hasMember(subj, getDefaultList());
    }
    catch (SchemaException eShouldNeverHappen) {
      // If we don't have "members" we have serious issues
      String msg = "this should never happen: default group list not found: " + eShouldNeverHappen.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen);
    }
  } // public boolean hasMember(subj)

  /**
   * Check whether the subject is a member of this list on this group.
   * <pre class="eg">
   * if (g.hasMember(subj, f)) {
   *   // Subject is a member of this group
   * }
   * else {
   *   // Subject is not a member of this group
   * } 
   * </pre>
   * @param   subj  Check this subject.
   * @param   f     Is subject a member of this list {@link Field}.
   * @return  Boolean true if subject belongs to this group.
   * @throws  SchemaException
   */
  public boolean hasMember(Subject subj, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(this.getSession(), subj);
      rv = m.isMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      ErrorLog.error(Group.class, E.GROUP_HM + eMNF.getMessage());
    }
    return rv;
  } // public boolean hasMember(subj, f)

  /**
   * Check whether the subject has OPTIN on this group.
   * <pre class="eg">
   * if (g.hasOptin(subj)) {
   *   // Has OPTIN
   * }
   * else {
   *   // Does not have OPTIN
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has OPTIN.
   */
  public boolean hasOptin(Subject subj) {
    return PrivilegeResolver.internal_hasPriv(this.getSession(), this, subj, AccessPrivilege.OPTIN);
  } // public boolean hasOption(subj)

  /**
   * Check whether the subject has OPTOUT on this group.
   * <pre class="eg">
   * if (g.hasOptout(subj)) {
   *   // has OPTOUT
   * }
   * else {
   *   // Does not have OPTOUT
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has OPTOUT.
   */
  public boolean hasOptout(Subject subj) {
    return PrivilegeResolver.internal_hasPriv(this.getSession(), this, subj, AccessPrivilege.OPTOUT);
  } // public boolean hasOptout(subj)

  /**
   * Check whether the subject has READ on this group.
   * <pre class="eg">
   * if (g.hasRead(subj)) {
   *   // Has READ
   * }
   * else {
   *   // Does not have READ
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has READ.
   */
  public boolean hasRead(Subject subj) {
    return PrivilegeResolver.internal_hasPriv( this.getSession(), this, subj, AccessPrivilege.READ );
  } // public boolean hasRead(subj)

  /**
   * Check whether group has the specified type.
   * <pre class="eg">
   * GroupType custom = GroupTypeFinder.find("custom type");
   * if (g.hasType(custom)) {
   *   // Group has type
   * }
   * </pre>
   * @param   type  The {@link GroupType} to check.
   */
  public boolean hasType(GroupType type) {
    return this.getDTO().getTypes().contains( type.getDTO() );
  } // public boolean hasType(type)

  /**
   * Check whether the subject has UPDATE on this group.
   * <pre class="eg">
   * if (g.hasUpdate(subj)) {
   *   // Has UPDATE
   * }
   * else {
   *   // Does not have UPDATE
   * }
   * </pre>
   * @param   subj  Check this subject.
   * @return  Boolean true if subject has UPDATE.
   */
  public boolean hasUpdate(Subject subj) {
    return PrivilegeResolver.internal_hasPriv(this.getSession(), this, subj, AccessPrivilege.UPDATE);
  } // public boolean hasUpdate(subj)

  /**
   * Check whether the subject has VIEW on this group.
   * <pre class="eg">
   * if (g.hasView(subj)) {
   *   // Has VIEW
   * }
   * else {
   *   // Does not have VIEW
   * }
   * </pre>
   * @param   subj  Check this member.
   * @return  Boolean true if subject has VIEW.
   */
  public boolean hasView(Subject subj) {
    return PrivilegeResolver.internal_hasPriv(this.getSession(), this, subj, AccessPrivilege.VIEW);
  } // public boolean hasView(subj)

  /**
   * Is this {@link Group} a factor in a {@link Composite} membership.
   * <pre class="eg">
   * if (g.isComposite()) {
   *   // this group is a factor in one-or-more composite memberships.
   * }
   * </pre>
   * @return  Boolean true if group is a factor in a composite membership.
   */
  public boolean isComposite() {
    if ( GrouperDAOFactory.getFactory().getComposite().findAsFactor( this.getDTO() ).size() > 0 ) {
      return true;
    }
    return false;
  } // public boolean isComposite()

  /**
   * Revoke all privileges of the specified type on this group.
   * <pre class="eg">
   * try {
   *   g.revokePriv(AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Unable to modify group
   * }
   * </pre>
   * @param   priv  Revoke all instances of this privilege.
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    PrivilegeResolver.internal_revokePriv(this.getSession(), this, priv);
    sw.stop();
    EL.groupRevokePriv(this.getSession(), this.getName(), priv, sw);
  } // public void revokePriv(priv)

  /**
   * Revoke a privilege from the specified subject.
   * <pre class="eg">
   * try {
   *   g.revokePriv(subj, AccessPrivilege.OPTIN);
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   subj  Revoke privilege from this subject.
   * @param   priv  Revoke this privilege.
   * @throws  InsufficientPrivilegeException
   * @throws  RevokePrivilegeException
   * @throws  SchemaException
   */
  public void revokePriv(Subject subj, Privilege priv) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException,
            SchemaException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    PrivilegeResolver.internal_revokePriv(this.getSession(), this, subj, priv);
    sw.stop();
    EL.groupRevokePriv(this.getSession(), this.getName(), subj, priv, sw);
  } // public void revokePriv(subj, priv)

  /**
   * Set an attribute value.
   * <pre class="eg">
   * try {
   *   g.attribute(attribute, value);
   * } 
   * catch (AttributeNotFoundException e0) {
   *   // Attribute doesn't exist
   * }
   * catch (GroupModifyException e1) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e2) {
   *   // Not privileged to modify this attribute
   * }
   * </pre>
   * @param   attr  Set this attribute.
   * @param   value Set to this value.
   * @throws  AttributeNotFoundException
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setAttribute(String attr, String value) 
    throws  AttributeNotFoundException, 
            GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      StopWatch sw = new StopWatch();
      sw.start();
      Field f = FieldFinder.find(attr);
      if ( !FieldType.ATTRIBUTE.equals( f.getType() ) ) {
        throw new AttributeNotFoundException( E.FIELD_INVALID_TYPE + f.getType() );
      }

      // TODO 20070306 this is all rather nasty
      GrouperValidator v = NotNullOrEmptyValidator.validate(attr);
      if (v.isInvalid()) {
        throw new AttributeNotFoundException(E.INVALID_ATTR_NAME + attr);
      }
      v = NotNullOrEmptyValidator.validate(value);
      if (v.isInvalid()) {
        throw new GroupModifyException(E.INVALID_ATTR_VALUE + value);
      }
      if (
            attr.equals(GrouperConfig.ATTR_DE)
        ||  attr.equals(GrouperConfig.ATTR_DN)
        ||  attr.equals(GrouperConfig.ATTR_E)
      )
      {
        v = NamingValidator.validate(value);
        if (v.isInvalid()) {
          throw new GroupModifyException( v.getErrorMessage() );
        }
      }
      if ( !this.canWriteField( FieldFinder.find(attr) ) ) {
        throw new InsufficientPrivilegeException();
      }

      Map attrs = this.getDTO().getAttributes();
      attrs.put(attr, value);
      if      ( attr.equals(GrouperConfig.ATTR_E) )   {
        attrs.put( GrouperConfig.ATTR_N, U.internal_constructName( this.getParentStem().getName(), value ) );
      }
      else if ( attr.equals(GrouperConfig.ATTR_DE) )  {
        attrs.put( GrouperConfig.ATTR_DN, U.internal_constructName( this.getParentStem().getDisplayName(), value ) );
      }
      this.getDTO().setAttributes(attrs);
      this.internal_setModified();
      HibernateGroupDAO.update(this);
      sw.stop();
      EL.groupSetAttr(this.getSession(), this.getName(), attr, value, sw);
    }
    catch (GrouperDAOException eDAO) {
      throw new GroupModifyException( eDAO.getMessage(), eDAO );
    }
    catch (InsufficientPrivilegeException eIP) {
      throw eIP;
    }
    catch (SchemaException eS) {
      throw new AttributeNotFoundException(eS.getMessage(), eS);
    }
  } // public void setAttribute(attr, value)

  /**
   * Set group description.
   * <pre class="eg">
   * try {
   *   g.setDescription(value);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to modify description
   * }
   * </pre>
   * @param   value   Set description to this value.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setDescription(String value) 
    throws  GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      this.setAttribute(GrouperConfig.ATTR_D, value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set description: " + eANF.getMessage(), eANF
      );
    }
  } // public void setDescription(value)
 
  /**
   * Set group <i>extension</i>.
   * <pre class="eg">
   * try {
   *   g.setExtension(value);
   * }
   * catch (GroupModifyException eGM) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to modify group
   * }
   * </pre>
   * @param   value   Set <i>extension</i> to this value.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setExtension(String value) 
    throws  GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      this.setAttribute(GrouperConfig.ATTR_E, value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set extension: " + eANF.getMessage(), eANF
      );
    }
  } // public void setExtension(value)

  /**
   * Set group displayExtension.
   * <pre class="eg">
   * try {
   *   g.setDisplayExtension(value);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to modify displayExtension
   * }
   * </pre>
   * @param   value   Set displayExtension to this value.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setDisplayExtension(String value) 
    throws  GroupModifyException, 
            InsufficientPrivilegeException
  {
    try {
      this.setAttribute(GrouperConfig.ATTR_DE, value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set displayExtension: " + eANF.getMessage(), eANF
      );
    }
  } // public void setDisplayExtension(value)

  /**
   * Convert this group to a {@link Member} object.
   * <p/>
   * <pre class="eg">
   * Member m = g.toMember();
   * </pre>
   * @return  {@link Group} as a {@link Member}
   * @throws  GrouperRuntimeException
   */
  public Member toMember() 
    throws  GrouperRuntimeException
  {
    if ( this.stateCache.containsKey(KEY_MEMBER) ) {
      return (Member) this.stateCache.get(KEY_MEMBER);
    }
    try {
      GrouperSession.validate( this.getSession() );
      Member m = new Member();
      m.setDTO( GrouperDAOFactory.getFactory().getMember().findBySubject( this.toSubject() ) );
      m.setSession( this.getSession() );
      this.stateCache.put(KEY_MEMBER, m);
      return m;
    }  
    catch (MemberNotFoundException eMNF) {
      // If we can't convert a group to a member we have major issues
      // and should probably just give up
      String msg = E.GROUP_G2M + eMNF.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eMNF);
    }
  } // public Member toMember()

  /**
   * Convert this group to a {@link Subject} object.
   * <p/>
   * <pre class="eg">
   * Subject subj = g.toSubject();
   * </pre>
   * @return  {@link Group} as a {@link Subject}
   * @throws  GrouperRuntimeException
   */
  public Subject toSubject() 
    throws  GrouperRuntimeException
  {
    if ( this.stateCache.containsKey(KEY_SUBJECT) ) {
      return (Subject) this.stateCache.get(KEY_SUBJECT);
    }
    try {
      this.stateCache.put( 
        KEY_SUBJECT, SubjectFinder.findById( this.getUuid(), "group", SubjectFinder.internal_getGSA().getId() )
      );
      return (Subject) this.stateCache.get(KEY_SUBJECT);
    }
    catch (SourceUnavailableException eShouldNeverHappen0)  {
      String msg = E.GROUP_G2S + eShouldNeverHappen0.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen0);
    }
    catch (SubjectNotFoundException eShouldNeverHappen1)    {
      String msg = E.GROUP_G2S + eShouldNeverHappen1.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen1);
    }
    catch (SubjectNotUniqueException eShouldNeverHappen2)   {
      String msg = E.GROUP_G2S + eShouldNeverHappen2.getMessage();
      ErrorLog.fatal(Group.class, msg);
      throw new GrouperRuntimeException(msg, eShouldNeverHappen2);
    }
  } // public Subject toSubject()

  public String toString() {
    // Bypass privilege checks.  If the group is loaded it is viewable.
    return new ToStringBuilder(this)
      .append( GrouperConfig.ATTR_N, (String) this.getDTO().getAttributes().get(GrouperConfig.ATTR_N) )
      .append( "uuid", this.getUuid() )
      .toString();
  } // public String toString()


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected GroupDTO getDTO() {
    return (GroupDTO) super.getDTO();
  } // protected GroupDTO getDTO()

  // @since   1.2.0
  // TODO 20070305 make into a validator?
  protected boolean internal_canWriteField(Subject subj, Field f, FieldType type)
    throws  IllegalArgumentException,
            SchemaException
  {
    GrouperValidator v = NotNullValidator.validate(subj);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "subject: " + v.getErrorMessage() );
    } 
    v = NotNullValidator.validate(f);
    if (v.isInvalid()) {
      throw new IllegalArgumentException( "field: " + v.getErrorMessage() );
    }
    v = FieldTypeValidator.validate(f);
    if (v.isInvalid()) {
      throw new SchemaException( v.getErrorMessage() );
    }
    if ( !this.hasType( f.getGroupType() ) ) {
      throw new SchemaException( E.INVALID_GROUP_TYPE + f.getGroupType().toString() );
    }
    try {
      PrivilegeResolver.internal_canPrivDispatch( this.getSession(), this, subj, f.getWritePriv() );
      return true;
    }
    catch (InsufficientPrivilegeException eIP) {
      return false;
    }
  } // protected boolean internal_canWriteField(subj, f, type)

  // @since   1.2.0 
  // i dislike this method
  protected void internal_setModified() {
    this.getDTO().setModifierUuid( this.getSession().getMember().getUuid() );
    this.getDTO().setModifyTime( new Date().getTime() );
  } // protected void internal_setModified()


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private boolean _canReadField(String name) {
    boolean rv = false;
    try {
      PrivilegeResolver.internal_canPrivDispatch( 
        this.getSession(), this, this.getSession().getSubject(), FieldFinder.find(name).getReadPriv()
      );
      rv = true;
    }
    catch (InsufficientPrivilegeException eIP) {
      return false ;
    }
    catch (SchemaException eS) {
      return false;
    }
    return rv;
  } // private boolean _canReadField(name)

  private void _revokeAllAccessPrivs() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, 
            SchemaException
  {
    GrouperSession orig = this.getSession();
    this.setSession( orig.getDTO().getRootSession() ); // proxy as root

    this.revokePriv(AccessPrivilege.ADMIN);
    this.revokePriv(AccessPrivilege.OPTIN);
    this.revokePriv(AccessPrivilege.OPTOUT);
    this.revokePriv(AccessPrivilege.READ);
    this.revokePriv(AccessPrivilege.UPDATE);
    this.revokePriv(AccessPrivilege.VIEW);

    this.setSession(orig);
  } // private void _revokeAllAccessPrivs()

} // public class Group extends GrouperAPI implements Owner

