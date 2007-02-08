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
import  net.sf.hibernate.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.lang.builder.*;

/** 
 * A namespace within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Stem.java,v 1.98 2007-02-08 16:25:25 blair Exp $
 */
public class Stem extends GrouperAPI implements Owner {

  // PROTECTED CLASS CONSTANTS //
  protected static final String ROOT_EXT  = GrouperConfig.EMPTY_STRING; // Appease Oracle
  protected static final String ROOT_INT  = ":";                        // Appease Oracle


  // PRIVATE CLASS CONSTANTS //
  private static final EventLog EL = new EventLog();


  // PRIVATE INSTANCE VARIABLES //
  private Subject creator;
  private Subject modifier;


  // PUBLIC INSTANCE METHODS //

  /**
   * Add a new group to the registry.
   * <pre class="eg">
   * // Add a group with the extension "edu" beneath this stem.
   * try {
   *   Group edu = ns.addChildGroup("edu", "edu domain");
   * }
   * catch (GroupAddException eGA) {
   *   // Group not added
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to add group
   * }
   * </pre>
   * @param   extension         Group's extension
   * @param   displayExtension  Groups' displayExtension
   * @return  The added {@link Group}
   * @throws  GroupAddException 
   * @throws  InsufficientPrivilegeException
   */
  public Group addChildGroup(String extension, String displayExtension) 
    throws  GroupAddException,
            InsufficientPrivilegeException
  {
    return internal_addChildGroup(extension, displayExtension, null);
  } // public Group addChildGroup(extension, displayExtension)

  /**
   * Add a new stem to the registry.
   * <pre class="eg">
   * // Add a stem with the extension "edu" beneath this stem.
   * try {
   *   Stem edu = ns.addChildStem("edu", "edu domain");
   * }
   * catch (StemAddException e) {
   *   // Stem not added
   * }
   * </pre>
   * @param   extension         Stem's extension
   * @param   displayExtension  Stem' displayExtension
   * @return  The added {@link Stem}
   * @throws  InsufficientPrivilegeException
   * @throws  StemAddException 
   */
  public Stem addChildStem(String extension, String displayExtension) 
    throws  InsufficientPrivilegeException,
            StemAddException 
  {
    return internal_addChildStem(extension, displayExtension, null);
  } // public Stem addChildStem(extension, displayExtension)
  
  /**
   * Delete this stem from the Groups Registry.
   * <pre class="eg">
   * try {
   *   ns.delete();
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to delete stem
   * }
   * catch (StemDeleteException eSD) {
   *   // unable to delete stem
   * }
   * </pre>
   * @throws  InsufficientPrivilegeException
   * @throws  StemDeleteException
   */
  public void delete() 
    throws  InsufficientPrivilegeException,
            StemDeleteException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    GrouperSessionValidator.internal_validate(this.getSession());
    StemValidator.internal_canDeleteStem(this);
    try {
      String name = this.getName();   // Preserve name for logging
      this._revokeAllNamingPrivs();   // Revoke privs
      HibernateStemDAO.delete(this);  // And delete
      sw.stop();
      EventLog.info(this.getSession(), M.STEM_DEL + U.internal_q(name), sw);
    }
    catch (GrouperDAOException eDAO)      {
      throw new StemDeleteException( eDAO.getMessage(), eDAO );
    }
    catch (RevokePrivilegeException eRP)  {
      throw new StemDeleteException(eRP.getMessage(), eRP);
    }
    catch (SchemaException eS)            {
      throw new StemDeleteException(eS.getMessage(), eS);
    }
  } // public void delete()

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Stem)) {
      return false;
    }
    return this.getDTO().equals( ( (Stem) other ).getDTO() );
  } // public boolean equals(other)

  /**
   * Get child groups of this stem.
   * <pre class="eg">
   * // Get child groups 
   * Set childGroups = ns.getChildGroups();
   * </pre>
   * @return  Set of {@link Group} objects
   */
  public Set getChildGroups() {
    Subject   subj    = this.getSession().getSubject();
    Set       groups  = new LinkedHashSet();
    Iterator  it      = HibernateStemDAO.findChildGroups(this).iterator();
    while (it.hasNext()) {
      Group child = new Group();
      child.setDTO( (GroupDTO) it.next() );
      child.setSession( this.getSession() );
      if ( RootPrivilegeResolver.internal_canVIEW(child, subj) ) {
        groups.add(child);
      }
    }
    return groups;
  } // public Set getChildGroups()

  /**
   * Get child stems of this stem.
   * <pre class="eg">
   * // Get child stems 
   * Set childStems = ns.getChildStems();
   * </pre>
   * @return  Set of {@link Stem} objects
   */
  public Set getChildStems() {
    Set       stems = new LinkedHashSet();
    Iterator  it    = HibernateStemDAO.findChildStems(this).iterator();
    while (it.hasNext()) {
      Stem child = new Stem();
      child.setDTO( (StemDTO) it.next() );
      child.setSession( this.getSession() ); 
      stems.add(child);
    }
    return stems;
  } // public Set getChildStems()

  /**
   * Get (optional and questionable) create source for this stem.
   * <pre class="eg">
   * // Get create source
`  * String source = ns.getCreateSource();
   * </pre>
   * @return  Create source for this stem.
   */
  public String getCreateSource() {
    return GrouperConfig.EMPTY_STRING;
  } // public String getCreateSource()
  
  /**
   * Get subject that created this stem.
   * <pre class="eg">
   * // Get creator of this stem.
   * try {
   *   Subject creator = ns.getCreateSubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that created this stem.
   * @throws  SubjectNotFoundException
   */
  public Subject getCreateSubject() 
    throws  SubjectNotFoundException
  {
    if (this.creator == null) {
      try {
        this.creator = MemberFinder.findByUuid( this.getSession(), this.getDTO().getCreatorUuid() ).getSubject();
      }
      catch (MemberNotFoundException eMNF) {
        throw new SubjectNotFoundException( eMNF.getMessage(), eMNF );
      }
    }
    return this.creator; 
  } // public Subject getCreateSubject()
  
  /**
   * Get creation time for this stem.
   * <pre class="eg">
   * // Get create time.
   * Date created = ns.getCreateTime();
   * </pre>
   * @return  {@link Date} that this stem was created.
   */
  public Date getCreateTime() {
    return new Date(this.getDTO().getCreateTime());
  } // public Date getCreateTime()

  /**
   * Get subjects with CREATE privilege on this stem.
   * <pre class="eg">
   * Set creators = ns.getCreators();
   * </pre>
   * @return  Set of {@link Subject} objects
   * @throws  GrouperRuntimeException
   */
  public Set getCreators() 
    throws  GrouperRuntimeException
  {
    try {
      return PrivilegeResolver.internal_getSubjectsWithPriv(
        this.getSession(), this, NamingPrivilege.CREATE
      );
    }
    catch (SchemaException eS) {
      String msg = E.FIELD_REQNOTFOUND + NamingPrivilege.CREATE;
      ErrorLog.fatal(Stem.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getCreators()

  /**
   * Get stem description.
   * <pre class="eg">
   * // Get description
   * String description = ns.getDescription();
   * </pre>
   * @return  Stem description.
   */
  public String getDescription() {
    String desc = this.getDTO().getDescription();
    if (desc == null) {
      desc = GrouperConfig.EMPTY_STRING;
    }
    return desc;
  } // public String getDescription()
 
  /**
   * Get stem displayExtension.
   * <pre class="eg">
   * // Get displayExtension
   * String displayExtn = ns.getDisplayExtension();
   * </pre>
   * @return  Stem displayExtension.
   */
  public String getDisplayExtension() {
    String val = this.getDTO().getDisplayExtension();
    if (val.equals(ROOT_INT)) {
      return ROOT_EXT;
    }
    return val;
  }
 
  /**
   * Get stem displayName.
   * <pre class="eg">
   * // Get displayName
   * String displayName = ns.getDisplayName();
   * </pre>
   * @return  Stem displayName.
   */
  public String getDisplayName() {
    String val = this.getDTO().getDisplayName();
    if (val.equals(ROOT_INT)) {
      return ROOT_EXT;
    }
    return val;
  }
 
  /**
   * Get stem extension.
   * <pre class="eg">
   * // Get extension
   * String extension = ns.getExtension();
   * </pre>
   * @return  Stem extension.
   */
  public String getExtension() {
    String val = this.getDTO().getExtension();
    if (val.equals(ROOT_INT)) {
      return ROOT_EXT;
    }
    return val;
  }
 
  /**
   * Get (optional and questionable) modify source for this stem.
   * <pre class="eg">
   * // Get modify source
`  * String source = ns.getModifySource();
   * </pre>
   * @return  Modify source for this stem.
   */
  public String getModifySource() {
    return GrouperConfig.EMPTY_STRING;
  } // public String getModifySource()
  
  /**
   * Get subject that last modified this stem.
   * <pre class="eg">
   * // Get last modifier of this stem.
   * try {
   *   Subject modifier = ns.getModifySubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Couldn't find subject
   * }
   * </pre>
   * @return  {@link Subject} that last modified this stem.
   * @throws  SubjectNotFoundException
   */
  public Subject getModifySubject() 
    throws  SubjectNotFoundException
  {
    if (this.modifier == null) {
      if ( this.getDTO().getModifierUuid() == null) {
        throw new SubjectNotFoundException("stem has not been modified");
      }
      try {
        this.modifier = MemberFinder.findByUuid( this.getSession(), this.getDTO().getModifierUuid() ).getSubject();
      }
      catch (MemberNotFoundException eMNF) {
        throw new SubjectNotFoundException( eMNF.getMessage(), eMNF );
      }
    }
    return this.modifier; 
  } // public Subject getModifySubject()
  
  /**
   * Get last modified time for this stem.
   * <pre class="eg">
   * // Get last modified time.
   * Date modified = ns.getModifyTime();
   * </pre>
   * @return  {@link Date} that this stem was last modified.
   */
  public Date getModifyTime() {
    return new Date(this.getDTO().getModifyTime());
  } // public Date getModifyTime()

  /**
   * Get stem name.
   * <pre class="eg">
   * // Get name
   * String name = ns.getName();
   * </pre>
   * @return  Stem name.
   */ 
  public String getName() {
    String val = this.getDTO().getName();
    if (val.equals(ROOT_INT)) {
      return ROOT_EXT;
    }
    return val;
  }

  /**
   * Get parent stem.
   * <pre class="eg">
   * // Get parent
   * Stem parent = ns.getParentStem();
   * </pre>
   * @return  Parent {@link Stem}.
   */
  public Stem getParentStem() 
    throws StemNotFoundException
  {
    String uuid = this.getDTO().getParentUuid();
    if (uuid == null) {
      throw new StemNotFoundException();
    }
    Stem parent = new Stem();
    parent.setDTO( HibernateStemDAO.findByUuid(uuid) );
    parent.setSession( this.getSession() );
    return parent;
  } // public Stem getParentStem()

  /**
   * Get privileges that the specified subject has on this stem.
   * <pre class="eg">
   * Set privs = ns.getPrivs(subj);
   * </pre>
   * @param   subj  Get privileges for this subject.
   * @return  Set of {@link NamingPrivilege} objects.
   */
  public Set getPrivs(Subject subj) {
    return PrivilegeResolver.internal_getPrivs(this.getSession(), this, subj);
  } // public Set getPrivs(subj)

  /**
   * Get subjects with STEM privilege on this stem.
   * <pre class="eg">
   * Set stemmers = ns.getStemmers();
   * </pre>
   * @return  Set of {@link Subject} objects
   * @throws  GrouperRuntimeException
   */
  public Set getStemmers() 
    throws  GrouperRuntimeException
  {
    try {
      return PrivilegeResolver.internal_getSubjectsWithPriv(
        this.getSession(), this, NamingPrivilege.STEM
      );
    }
    catch (SchemaException eS) {
      String msg = E.FIELD_REQNOTFOUND + NamingPrivilege.STEM;
      ErrorLog.fatal(Stem.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getStemmers()

  /**
   */
  public String getUuid() {
    return this.getDTO().getUuid();
  } // public String getUuid()

  /**
   * Grant a privilege on this stem.
   * <pre class="eg">
   * // Grant CREATE to the specified subject
   * try {
   *   ns.grantPriv(subj, NamingPrivilege.CREATE);
   * }
   * catch (GrantPrivilegeException e) {
   *   // Error granting privilege
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
    EL.stemGrantPriv(this.getSession(), this.getName(), subj, priv, sw);
  } // public void grantPriv(subj, priv)

  /**
   * Check whether a subject has the CREATE privilege on this stem.
   * <pre class="eg">
   * if (ns.hasCreate(subj)) {
   *   // Has CREATE
   * }
   *   // Does not have CREATE
   * } 
   * </pre>
   * @param   subj  Check whether this subject has CREATE.
   * @return  Boolean true if the subject has CREATE.
   */
  public boolean hasCreate(Subject subj) {
    return PrivilegeResolver.internal_hasPriv(this.getSession(), this, subj, NamingPrivilege.CREATE);
  } // public boolean hasCreate(subj)
 
  /**
   * Check whether a member has the STEM privilege on this stem.
   * <pre class="eg">
   * if (ns.hasStem(subj)) {
   *   // Has STEM
   * }
   *   // Does not have STEM
   * } 
   * </pre>
   * @param   subj  heck whether this subject has STEM.
   * @return  Boolean true if the subject has STEM.
   */
  public boolean hasStem(Subject subj) {
    return PrivilegeResolver.internal_hasPriv(this.getSession(), this, subj, NamingPrivilege.STEM);
  } // public boolean hasStem(subj)
 
  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  /**
   * Revoke all privileges of the specified type on this stem.
   * <pre class="eg">
   * // Revoke CREATE from everyone on this stem.
   * try {
   *   ns.revokePriv(NamingPrivilege.CREATE);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to revoke this privilege
   * }
   * catch (RevokePrivilegeException eRP) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   priv  Revoke this privilege.
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
    EL.stemRevokePriv(this.getSession(), this.getName(), priv, sw);
  } // public void revokePriv(priv)
 
  /**
   * Revoke a privilege on this stem.
   * <pre class="eg">
   * // Revoke CREATE from the specified subject
   * try {
   *   ns.revokePriv(subj, NamingPrivilege.CREATE);
   * }
   * catch (InsufficientPrivilegeException eIP) {
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
    EL.stemRevokePriv(this.getSession(), this.getName(), subj, priv, sw);
  } // public void revokePriv(subj, priv)
 
  /**
   * Set stem description.
   * <pre class="eg">
   * // Set description
   * try {
   *  ns.setDescription(value);
   * }
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to set description
   * catch (StemModifyException e1) {
   *   // Error setting description
   * }
   * </pre>
   * @param   value   Set description to this value.
   * @throws  InsufficientPrivilegeException
   * @throws  StemModifyException
   */
  public void setDescription(String value) 
    throws  InsufficientPrivilegeException,
            StemModifyException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if (!RootPrivilegeResolver.internal_canSTEM(this, this.getSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    }
    try {
      this.getDTO().setDescription(value);
      this.internal_setModified();
      HibernateStemDAO.update(this);
      sw.stop();
      EL.stemSetAttr(this.getSession(), this.getName(), GrouperConfig.ATTR_D, value, sw);
    }
    catch (GrouperDAOException eDAO) {
      throw new StemModifyException( "unable to set description: " + eDAO.getMessage(), eDAO );
    }
  } // public void setDescription(value)

  /**
   * Set stem displayExtension.
   * <pre class="eg">
   * // Set displayExtension
   * try {
   *  ns.setDisplayExtension(value);
   * }
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to set displayExtension
   * catch (StemModifyException e1) {
   *   // Error setting displayExtension
   * }
   * </pre>
   * @param   value   Set displayExtension to this value.
   * @throws  InsufficientPrivilegeException
   * @throws  StemModifyException
   */
  public void setDisplayExtension(String value) 
    throws  InsufficientPrivilegeException,
            StemModifyException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    try {
      AttributeValidator.internal_namingValue(value);
    }
    catch (ModelException eM) {
      if (!(this.getDTO().getName().equals(ROOT_INT) && value.equals(ROOT_EXT))) {
        throw new StemModifyException(eM.getMessage(), eM);
      }
      // Appease Oracle
      value = ROOT_INT;
    }
    if (!RootPrivilegeResolver.internal_canSTEM(this, this.getSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    }
    try {
      this.getDTO().setDisplayExtension(value);
      this.internal_setModified();
      try {
        this.getDTO().setDisplayName( U.internal_constructName( this.getParentStem().getDisplayName(), value ) );
      }
      catch (StemNotFoundException eSNF) {
        this.getDTO().setDisplayName(value); // I guess we're the root stem
      }
      // Now iterate through all child groups and stems (as root), renaming each.
      GrouperSession  orig  = this.getSession();
      this.setSession( orig.getDTO().getRootSession() );
      HibernateStemDAO.renameStemAndChildren( this, this._renameChildren() );
      this.setSession(orig);
    }
    catch (GrouperDAOException eDAO) {
      throw new StemModifyException( "unable to set displayExtension: " + eDAO.getMessage(), eDAO );
    }
    sw.stop();
    // Reset for logging purposes
    if (value.equals(ROOT_INT)) {
      value = ROOT_EXT;
    }
    EL.stemSetAttr(this.getSession(), this.getName(), GrouperConfig.ATTR_DE, value, sw);
  } // public void setDisplayExtension(value)

  public String toString() {
    // TODO 20070125 replace with call to DTO?
    return new ToStringBuilder(this)
      .append( GrouperConfig.ATTR_DN, this.getDTO().getDisplayName()  )
      .append( GrouperConfig.ATTR_N,  this.getDTO().getName()         )
      .append( "uuid",                this.getDTO().getUuid()         )
      .append( "creator",             this.getDTO().getCreatorUuid()  )
      .append( "modifier",            this.getDTO().getModifierUuid() )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // TODO 20070125 revisit these methods once initial daoification is complete

  // @since   1.2.0
  protected static Stem internal_addRootStem(GrouperSession s) 
    throws  GrouperRuntimeException
  {
    try {
      StemDTO dto = new StemDTO();
      dto.setCreatorUuid( s.getMember().getUuid() );
      dto.setCreateTime( new Date().getTime() );
      // TODO 20070131 ROOT_INT should be moved to DAO layer
      dto.setDisplayExtension(ROOT_INT);
      dto.setDisplayName(ROOT_INT);
      dto.setExtension(ROOT_INT);
      dto.setName(ROOT_INT);
      dto.setUuid( GrouperUuid.internal_getUuid() );
      
      dto.setId( HibernateStemDAO.createRootStem(dto) );
      Stem root = new Stem();
      root.setDTO(dto);
      root.setSession(s);
      return root;
    }
    catch (GrouperDAOException eDAO) {
      String msg = E.STEM_ROOTINSTALL + eDAO.getMessage();
      ErrorLog.fatal(Stem.class, msg);
      throw new GrouperRuntimeException(msg, eDAO);
    }
  } // protected static Stem internal_addRootStem(GrouperSession s)

  // @since   1.2.0
  protected void internal_setModified() {
    this.getDTO().setModifierUuid( s.getMember().getUuid() );
    this.getDTO().setModifyTime(  new Date().getTime()    );
  } // protected void internal_setModified()


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected StemDTO getDTO() {
    return (StemDTO) super.getDTO();
  } // protected StemDTO getDTO()
 
  // TODO 20070125 revisit these methods once initial daoification is complete

  // @since   1.2.0
  protected Group internal_addChildGroup(String extn, String dExtn, String uuid) 
    throws  GroupAddException,
            InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if (!StemValidator.internal_canAddChildGroup(this, extn, dExtn)) {
      throw new GroupAddException();
    }
    try {
      // TODO 20070130 refactor!
      GroupDTO  dto   = new GroupDTO();    
      // XXX
      Map       attrs = new HashMap();
      attrs.put( GrouperConfig.ATTR_DE, dExtn );
      attrs.put( GrouperConfig.ATTR_DN, U.internal_constructName( this.getDisplayName(), dExtn ) );
      attrs.put( GrouperConfig.ATTR_E,  extn );
      attrs.put( GrouperConfig.ATTR_N,  U.internal_constructName( this.getName(), extn ) );
      dto.setAttributes(attrs);
      dto.setCreateTime( new Date().getTime() );
      dto.setCreatorUuid( this.getSession().getMember().getUuid() );
      dto.setParentUuid( this.getDTO().getUuid() );
      Set       types = new LinkedHashSet();
      types.add( GroupTypeFinder.find("base").getDTO() ); 
      dto.setTypes(types);
      if (uuid == null) {
        dto.setUuid( GrouperUuid.internal_getUuid() );
      }
      else {
        dto.setUuid(uuid);
      }

      GrouperSubject  subj  = new GrouperSubject(dto);
      MemberDTO       m     = new MemberDTO();
      // TODO 20070123 this could be more elegant
      m.setMemberUuid( GrouperUuid.internal_getUuid() );
      m.setSubjectId( subj.getId() );
      m.setSubjectSourceId( subj.getSource().getId() );
      m.setSubjectTypeId( subj.getType().getName() );

      dto.setId( HibernateStemDAO.createChildGroup( this.getDTO(), dto, m) );
      Group child = new Group();
      child.setDTO(dto);
      child.setSession( this.getSession() );

      sw.stop();
      EventLog.info(s, M.GROUP_ADD + U.internal_q(child.getName()), sw);
      _grantDefaultPrivsUponCreate(child);
      return child;
    }
    catch (GrouperDAOException eDAO)        {
      throw new GroupAddException( E.CANNOT_CREATE_GROUP + eDAO.getMessage(), eDAO );
    }
    catch (SchemaException eS)              {
      throw new GroupAddException(E.CANNOT_CREATE_GROUP + eS.getMessage(), eS);
    }
    catch (SourceUnavailableException eSU)  {
      throw new GroupAddException(E.CANNOT_CREATE_GROUP + eSU.getMessage(), eSU);
    }
  } // protected Group internal_addChildGroup(extn, dExtn, uuid)

  // @since   1.2.0
  protected Stem internal_addChildStem(String extn, String dExtn, String uuid) 
    throws  StemAddException,
            InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( !StemValidator.internal_canAddChildStem(this, extn, dExtn) ) {
      throw new StemAddException();
    }
    try {
      StemDTO dto = new StemDTO();
      dto.setCreatorUuid( this.getSession().getMember().getUuid() );
      dto.setCreateTime( new Date().getTime() );
      dto.setDisplayExtension(dExtn);
      dto.setDisplayName( U.internal_constructName( this.getDisplayName(), dExtn ) );
      dto.setExtension(extn);
      dto.setName( U.internal_constructName( this.getName(), extn ) );
      dto.setParentUuid( this.getDTO().getUuid() );
      if (uuid == null) {
        dto.setUuid( GrouperUuid.internal_getUuid() );
      }
      else {
        dto.setUuid(uuid);
      }

      dto.setId( HibernateStemDAO.createChildStem( this.getDTO(), dto ) );
      Stem child = new Stem();
      child.setDTO(dto);
      child.setSession( this.getSession() );

      sw.stop();
      EventLog.info(s, M.STEM_ADD + U.internal_q( child.getName() ), sw);
      _grantDefaultPrivsUponCreate(child);
      return child;
    }
    catch (GrouperDAOException eDAO) {
      throw new StemAddException( E.CANNOT_CREATE_STEM + eDAO.getMessage(), eDAO );
    }
  }  // protected Stem internal_addChildStem(extn, dExtn, uuid)

  // @since   1.2.0
  protected boolean internal_isRootStem() {
    if (this.getName().equals(ROOT_EXT)) {
      return true;
    }
    return false;
  } // protected boolean internal_isRootStem()


  // PRIVATE INSTANCE METHODS //

  private void _grantDefaultPrivsUponCreate(Group g)
    throws  GroupAddException
  {
    // Now grant ADMIN (as root) to the creator of the child group.
    //
    // Ideally this would be wrapped up in the broader transaction
    // of adding the child stem but as the interfaces may be
    // outside of our control, I don't think we can do that.  
    //
    // Possibly a bug. The modify* attrs get set when granting ADMIN at creation.
    try {
      GrouperSession  orig  = this.s;
      GrouperSession  root  = orig.getDTO().getRootSession();
      g.setSession(root);
      PrivilegeResolver.internal_grantPriv(root, g, orig.getSubject(), AccessPrivilege.ADMIN);

      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate(root, g, AccessPrivilege.ADMIN , GrouperConfig.GCGAA );
      this._grantOptionalPrivUponCreate(root, g, AccessPrivilege.OPTIN , GrouperConfig.GCGAOI);
      this._grantOptionalPrivUponCreate(root, g, AccessPrivilege.OPTOUT, GrouperConfig.GCGAOO);
      this._grantOptionalPrivUponCreate(root, g, AccessPrivilege.READ  , GrouperConfig.GCGAR );
      this._grantOptionalPrivUponCreate(root, g, AccessPrivilege.UPDATE, GrouperConfig.GCGAU );
      this._grantOptionalPrivUponCreate(root, g, AccessPrivilege.VIEW  , GrouperConfig.GCGAV );

      g.setSession(orig);
    }
    catch (GrantPrivilegeException eGP)         {
      throw new GroupAddException(eGP.getMessage(), eGP);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new GroupAddException(eIP.getMessage(), eIP);
    }
    catch (SchemaException eS)                  {
      throw new GroupAddException(eS.getMessage(), eS);
    }
  } // private void _grantDefaultPrivsUponCreate(g)

  private void _grantDefaultPrivsUponCreate(Stem ns)
    throws  StemAddException
  {
    // Now grant STEM (as root) to the creator of the child stem.
    //
    // Ideally this would be wrapped up in the broader transaction
    // of adding the child stem but as the interfaces may be
    // outside of our control, I don't think we can do that.  
    //
    // Possibly a bug. The modify* attrs get set when granting privs at creation.
    try {
      GrouperSession  orig  = this.s;
      GrouperSession  root  = orig.getDTO().getRootSession();
      ns.setSession(root);
      PrivilegeResolver.internal_grantPriv(root, ns, orig.getSubject(), NamingPrivilege.STEM);

      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate(
        root, ns, NamingPrivilege.CREATE, GrouperConfig.SCGAC
      );
      this._grantOptionalPrivUponCreate(
        root, ns, NamingPrivilege.STEM  , GrouperConfig.SCGAS
      );

      ns.setSession(orig);
    }
    catch (GrantPrivilegeException eGP)         {
      throw new StemAddException(eGP.getMessage(), eGP);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new StemAddException(eIP.getMessage(), eIP);
    }
    catch (SchemaException eS)                  {
      throw new StemAddException(eS.getMessage(), eS);
    }
  } // private void _grantDefaultPrivsUponCreate(ns)

  private void _grantOptionalPrivUponCreate(
    GrouperSession root, Object o, Privilege p, String opt
  ) 
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException,
            SchemaException
  {
    Subject       all = SubjectFinder.findAllSubject();
    if (GrouperConfig.getProperty(opt).equals(GrouperConfig.BT)) {
      StopWatch sw = new StopWatch();
      sw.start();
      if      (o.getClass().equals(Group.class)) {
        Group g = (Group) o;
        PrivilegeResolver.internal_grantPriv(root, g, all, p);
        sw.stop();
        EL.groupGrantPriv(this.getSession(), g.getName(), all, p, sw);
      }
      else if (o.getClass().equals(Stem.class)) {
        Stem ns = (Stem) o;
        PrivilegeResolver.internal_grantPriv(root, ns, all, p);
        sw.stop();
        EL.stemGrantPriv(this.getSession(), ns.getName(), all, p, sw);
      }
    }
  } // private void _grantOptionalPrivUponCreate(root, o, p, opt)

  private Set _renameChildGroups() 
    throws  HibernateException
  {
    Map       attrs;
    GroupDTO  child;
    Set       groups  = new LinkedHashSet();
    Iterator  it      = HibernateStemDAO.findChildGroups(this).iterator();
    while (it.hasNext()) {
      child = (GroupDTO) it.next();
      attrs = child.getAttributes();
      attrs.put( GrouperConfig.ATTR_DN, U.internal_constructName( this.getDisplayName(), (String) attrs.get(GrouperConfig.ATTR_DE) ) );
      child.setModifierUuid( this.getSession().getMember().getUuid() );
      child.setModifyTime( new Date().getTime() );
      child.setAttributes(attrs);
      groups.add(child);
    }
    return groups;
  } // private Set _renameChildGroups()

  private Set _renameChildren() 
    throws  StemModifyException
  {
    Set objects = new LinkedHashSet();
    try {
      objects.addAll( this._renameChildStemsAndGroups() );
      objects.addAll( this._renameChildGroups() );
    }
    catch (HibernateException eH) {
      String msg = E.HIBERNATE + eH.getMessage();
      ErrorLog.error(Stem.class, msg);
      throw new StemModifyException(msg, eH);
    }
    return objects;
  } // private Set _renameChildren()

  private Set _renameChildStemsAndGroups() 
    throws  HibernateException
  {
    Set       children  = new LinkedHashSet();
    Iterator  it        = HibernateStemDAO.findChildStems(this).iterator();
    while (it.hasNext()) {
      Stem child = new Stem();
      child.setDTO( (StemDTO) it.next() );
      child.setSession( this.getSession() );
      child.getDTO().setDisplayName( U.internal_constructName( this.getDisplayName(), child.getDisplayExtension() ) );

      children.addAll( child._renameChildGroups() );

      child.internal_setModified();
      children.add( child.getDTO() );
      children.addAll( child._renameChildStemsAndGroups() );
    }
    return children;
  } // private Set _renameChildStemsAndGroups()

  private void _revokeAllNamingPrivs() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, 
            SchemaException
  {
    GrouperSession orig = this.getSession();
    this.setSession( orig.getDTO().getRootSession() ); // proxy as root
    this.revokePriv(NamingPrivilege.CREATE);
    this.revokePriv(NamingPrivilege.STEM);
    this.setSession(orig);
  } // private void _revokeAllNamingPrivs()

} // public class Stem extends GrouperAPI implements Owner

