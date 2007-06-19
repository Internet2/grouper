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
import  edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import  edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import  edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import  edu.internet2.middleware.grouper.internal.dto.StemDTO;
import  edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import  edu.internet2.middleware.grouper.internal.util.Quote;
import  edu.internet2.middleware.grouper.internal.util.U;
import  edu.internet2.middleware.subject.*;
import  java.util.Date;
import  java.util.HashMap;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Map;
import  java.util.Set;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.lang.builder.*;

/** 
 * A namespace within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Stem.java,v 1.128 2007-06-19 18:04:17 blair Exp $
 */
public class Stem extends GrouperAPI implements Owner {

  // PUBLIC CLASS CONSTANTS //
  
  /**
   * Hierarchy delimiter.
   */
  public static final String DELIM      = ":";
  /**
   * Default name of root stem.
   */
  public static final String ROOT_NAME  = GrouperConfig.EMPTY_STRING;
  
  
  // PROTECTED CLASS CONSTANTS //
  // TODO 20070419 how can i get rid of this?
  protected static final String ROOT_INT = ":"; // Appease Oracle, et. al.


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
    return this.internal_addChildGroup(extension, displayExtension, null);
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
    GrouperSession.validate(this.getSession());
    if ( !PrivilegeResolver.internal_canSTEM( this, this.getSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    }
    DeleteStemValidator v = DeleteStemValidator.validate(this);
    if (v.isInvalid()) {
      throw new StemDeleteException( v.getErrorMessage() );
    }
    try {
      String name = this.getName(); // Preserve name for logging
      this._revokeAllNamingPrivs();
      GrouperDAOFactory.getFactory().getStem().delete( this._getDTO() );
      sw.stop();
      EventLog.info(this.getSession(), M.STEM_DEL + Quote.single(name), sw);
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
    Iterator  it      = GrouperDAOFactory.getFactory().getStem().findAllChildGroups(this).iterator();
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
    Iterator  it    = GrouperDAOFactory.getFactory().getStem().findAllChildStems(this).iterator();
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
        this.creator = MemberFinder.findByUuid( this.getSession(), this._getDTO().getCreatorUuid() ).getSubject();
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
    return new Date( this._getDTO().getCreateTime() );
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
    String desc = this._getDTO().getDescription();
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
    String val = this._getDTO().getDisplayExtension();
    if (val.equals(ROOT_INT)) {
      return ROOT_NAME;
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
    String val = this._getDTO().getDisplayName();
    if (val.equals(ROOT_INT)) {
      return ROOT_NAME;
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
    String val = this._getDTO().getExtension();
    if (val.equals(ROOT_INT)) {
      return ROOT_NAME;
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
      if ( this._getDTO().getModifierUuid() == null) {
        throw new SubjectNotFoundException("stem has not been modified");
      }
      try {
        this.modifier = MemberFinder.findByUuid( this.getSession(), this._getDTO().getModifierUuid() ).getSubject();
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
    return new Date( this._getDTO().getModifyTime() );
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
    String val = this._getDTO().getName();
    if (val.equals(ROOT_INT)) {
      return ROOT_NAME;
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
    String uuid = this._getDTO().getParentUuid();
    if (uuid == null) {
      throw new StemNotFoundException();
    }
    Stem parent = new Stem();
    parent.setDTO( GrouperDAOFactory.getFactory().getStem().findByUuid(uuid) );
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
    return this._getDTO().getUuid();
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
   * @since   1.2.0
   * @return  Boolean true if this is the root stem of the Groups Registry.
   */
  public boolean isRootStem() {
    return ROOT_INT.equals( this._getDTO().getName() );
  } // public boolean isRootStem()

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
      this._getDTO().setDescription(value);
      this.internal_setModified();
      GrouperDAOFactory.getFactory().getStem().update( this._getDTO() );
      sw.stop();
      EL.stemSetAttr(this.getSession(), this.getName(), GrouperConfig.ATTR_D, value, sw);
    }
    catch (GrouperDAOException eDAO) {
      throw new StemModifyException( "unable to set description: " + eDAO.getMessage(), eDAO );
    }
  } // public void setDescription(value)

  /**
   * Set <i>displayExtension</i>.
   * <p>This will also update the <i>displayName</i> of all child stems and groups.</p>
   * <pre class="eg">
   * try {
   *  ns.setDisplayExtension(value);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to set displayExtension
   * catch (StemModifyException eNSM) {
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
    NamingValidator nv = NamingValidator.validate(value);
    if (nv.isInvalid()) {
      if ( this.isRootStem() && value.equals(ROOT_NAME) ) {
        // Appease Oracle
        value = ROOT_INT;   
      }
      else {
        throw new StemModifyException( nv.getErrorMessage() );
      }
    }
    if (!RootPrivilegeResolver.internal_canSTEM(this, this.getSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    }
    try {
      this._getDTO().setDisplayExtension(value);
      this.internal_setModified();
      if (this.isRootStem()) {
        this._getDTO().setDisplayName(value);
      }
      else {
        try {
          this._getDTO().setDisplayName( U.constructName( this.getParentStem().getDisplayName(), value ) );
        }
        catch (StemNotFoundException eShouldNeverHappen) {
          throw new IllegalStateException( 
            "this should never happen: non-root stem without parent: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen 
          );
        }
      }
      // Now iterate through all child groups and stems, renaming each.
      GrouperDAOFactory.getFactory().getStem().renameStemAndChildren( this._getDTO(), this._renameChildren(GrouperConfig.ATTR_DE) );
    }
    catch (GrouperDAOException eDAO) {
      throw new StemModifyException( "unable to set displayExtension: " + eDAO.getMessage(), eDAO );
    }
    sw.stop();
    // Reset for logging purposes
    if (value.equals(ROOT_INT)) {
      value = ROOT_NAME;
    }
    EL.stemSetAttr(this.getSession(), this.getName(), GrouperConfig.ATTR_DE, value, sw);
  } // public void setDisplayExtension(value)

  /**
   * Set <i>extension</i>.
   * <p>This will also update the <i>name</i> of all child stems and groups.</p>
   * <pre class="eg">
   * try {
   *  ns.setExtension(value);
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // Not privileged to set "extension"
   * catch (StemModifyException eNSM) {
   *   // Error setting "extension"
   * }
   * </pre>
   * @param   value   Set <i>extension</i> to this value.
   * @throws  InsufficientPrivilegeException
   * @throws  StemModifyException
   */
  public void setExtension(String value) 
    throws  InsufficientPrivilegeException,
            StemModifyException
  {
    // TODO 20070531 DRY w/ "setDisplayExtension"
    StopWatch sw = new StopWatch();
    sw.start();
    NamingValidator nv = NamingValidator.validate(value);
    if (nv.isInvalid()) {
      if ( this.isRootStem() && value.equals(ROOT_NAME) ) {
        // Appease Oracle
        value = ROOT_INT;   
      }
      else {
        throw new StemModifyException( nv.getErrorMessage() );
      }
    }
    if (!RootPrivilegeResolver.internal_canSTEM(this, this.getSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    }
    try {
      this._getDTO().setExtension(value);
      this.internal_setModified();
      if (this.isRootStem()) {
        this._getDTO().setName(value);
      }
      else {
        try {
          this._getDTO().setName( U.constructName( this.getParentStem().getName(), value ) );
        }
        catch (StemNotFoundException eShouldNeverHappen) {
          throw new IllegalStateException( 
            "this should never happen: non-root stem without parent: " + eShouldNeverHappen.getMessage(), eShouldNeverHappen 
          );
        }
      }
      // Now iterate through all child groups and stems, renaming each.
      GrouperDAOFactory.getFactory().getStem().renameStemAndChildren( this._getDTO(), this._renameChildren(GrouperConfig.ATTR_E) );
    }
    catch (GrouperDAOException eDAO) {
      throw new StemModifyException( "unable to set extension: " + eDAO.getMessage(), eDAO );
    }
    sw.stop();
    // Reset for logging purposes
    if (value.equals(ROOT_INT)) {
      value = ROOT_NAME;
    }
    EL.stemSetAttr( this.getSession(), this.getName(), GrouperConfig.ATTR_E, value, sw );
  } // public void setExtension(value)

  public String toString() {
    return new ToStringBuilder(this)
      .append( GrouperConfig.ATTR_DN, this._getDTO().getDisplayName()  )
      .append( GrouperConfig.ATTR_N,  this._getDTO().getName()         )
      .append( "uuid",                this._getDTO().getUuid()         )
      .append( "creator",             this._getDTO().getCreatorUuid()  )
      .append( "modifier",            this._getDTO().getModifierUuid() )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Stem internal_addRootStem(GrouperSession s) 
    throws  GrouperRuntimeException
  {
    try {
      StemDTO _ns = new StemDTO()
        .setCreatorUuid( s.getMember().getUuid() )
        .setCreateTime( new Date().getTime() )
        .setDisplayExtension(ROOT_INT)
        .setDisplayName(ROOT_INT)
        .setExtension(ROOT_INT)
        .setName(ROOT_INT)
        .setUuid( GrouperUuid.getUuid() )
        ;
      _ns.setId( GrouperDAOFactory.getFactory().getStem().createRootStem(_ns) );
      Stem root = new Stem();
      root.setDTO(_ns);
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
    this._getDTO().setModifierUuid( this.getSession().getMember().getUuid() );
    this._getDTO().setModifyTime(  new Date().getTime()    );
  } // protected void internal_setModified()


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected Group internal_addChildGroup(String extn, String dExtn, String uuid) 
    throws  GroupAddException,
            InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( !RootPrivilegeResolver.internal_canCREATE( this.getSession(), this, this.getSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_CREATE);
    } 
    GrouperValidator v = AddGroupValidator.validate(this, extn, dExtn);
    if (v.isInvalid()) {
      throw new GroupAddException( v.getErrorMessage() );
    }
    try {
      Map attrs = new HashMap();
      attrs.put( GrouperConfig.ATTR_DE, dExtn );
      attrs.put( GrouperConfig.ATTR_DN, U.constructName( this.getDisplayName(), dExtn ) );
      attrs.put( GrouperConfig.ATTR_E,  extn );
      attrs.put( GrouperConfig.ATTR_N,  U.constructName( this.getName(), extn ) );
      Set types = new LinkedHashSet();
      types.add( GroupTypeFinder.find("base").getDTO() ); 
      GroupDTO _g = new GroupDTO()
        .setAttributes(attrs)
        .setCreateTime( new Date().getTime() )
        .setCreatorUuid( this.getSession().getMember().getUuid() )
        .setParentUuid( this._getDTO().getUuid() )
        .setTypes(types)
        ;
      v = NotNullOrEmptyValidator.validate(uuid);
      if (v.isInvalid()) {
        _g.setUuid( GrouperUuid.getUuid() );
      }
      else {
        _g.setUuid(uuid);
      }

      GrouperSubject  subj  = new GrouperSubject(_g);
      MemberDTO       _m    = new MemberDTO()
        .setSubjectId( subj.getId() )
        .setSubjectSourceId( subj.getSource().getId() )
        .setSubjectTypeId( subj.getType().getName() );
      // TODO 20070328 this is incredibly ugly.  making it even worse is that i am also checking
      //               for existence in the dao as well.
      if (uuid == null) {
        _m.setUuid( GrouperUuid.getUuid() ); // assign a new uuid
      }
      else {
        try {
          // member already exists.  use existing uuid.
          _m.setUuid( GrouperDAOFactory.getFactory().getMember().findBySubject(subj).getUuid() );
        }
        catch (MemberNotFoundException eMNF) {
          // couldn't find member.  assign new uuid.
          _m.setUuid( GrouperUuid.getUuid() ); 
        }
      }

      Group child = new Group();
      child.setDTO( 
        _g.setId( GrouperDAOFactory.getFactory().getStem().createChildGroup( this._getDTO(), _g, _m ) ) 
      );
      child.setSession( this.getSession() );
        
      sw.stop();
      EventLog.info(s, M.GROUP_ADD + Quote.single(child.getName()), sw);
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
  } 

  // @since   1.2.0
  protected Stem internal_addChildStem(String extn, String dExtn, String uuid) 
    throws  StemAddException,
            InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if (!RootPrivilegeResolver.internal_canSTEM( this, this.getSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    } 
    GrouperValidator v = AddStemValidator.validate(this, extn, dExtn);
    if (v.isInvalid()) {
      throw new StemAddException( v.getErrorMessage() );
    }
    try {
      StemDTO _ns = new StemDTO()
        .setCreatorUuid( this.getSession().getMember().getUuid() )
        .setCreateTime( new Date().getTime() )
        .setDisplayExtension(dExtn)
        .setDisplayName( U.constructName( this.getDisplayName(), dExtn ) )
        .setExtension(extn)
        .setName( U.constructName( this.getName(), extn ) )
        .setParentUuid( this._getDTO().getUuid() )
        ;
      v = NotNullOrEmptyValidator.validate(uuid);
      if (v.isInvalid()) {
        _ns.setUuid( GrouperUuid.getUuid() );
      }
      else {
        _ns.setUuid(uuid);
      }
      _ns.setId( GrouperDAOFactory.getFactory().getStem().createChildStem( this._getDTO(), _ns ) );

      Stem child = new Stem();
      child.setDTO(_ns);
      child.setSession( this.getSession() );

      sw.stop();
      EventLog.info(s, M.STEM_ADD + Quote.single( child.getName() ), sw);
      _grantDefaultPrivsUponCreate(child);
      return child;
    }
    catch (GrouperDAOException eDAO) {
      throw new StemAddException( E.CANNOT_CREATE_STEM + eDAO.getMessage(), eDAO );
    }
  }


  // PRIVATE INSTANCE METHODS //

  // @since   1.2.0
  private StemDTO _getDTO() {
    return (StemDTO) super.getDTO();
  }
  
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
      GrouperSession  root  = orig.internal_getRootSession();
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
      GrouperSession  root  = orig.internal_getRootSession();
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

  // @since   1.2.0
  private Set _renameChildGroups(String attr, String modifier, long modifyTime) {
    Map       attrs;
    GroupDTO  _g;
    Set       groups  = new LinkedHashSet();
    Iterator  it      = GrouperDAOFactory.getFactory().getStem().findAllChildGroups(this).iterator();
    while (it.hasNext()) {
      _g = (GroupDTO) it.next();
      attrs = _g.getAttributes();
      if      ( attr.equals(GrouperConfig.ATTR_DE) )  {
        attrs.put( 
          GrouperConfig.ATTR_DN, 
          U.constructName( this.getDisplayName(), (String) attrs.get(GrouperConfig.ATTR_DE) ) 
        );
      }
      else if ( attr.equals(GrouperConfig.ATTR_E) )   {
        attrs.put(  
          GrouperConfig.ATTR_N, 
          U.constructName( this.getName(), (String) attrs.get(GrouperConfig.ATTR_E) ) 
        );
      }
      else {
        throw new IllegalStateException( "attempt to update invalid naming attribute: " + attr);
      }
      groups.add( _g.setModifierUuid(modifier).setModifyTime(modifyTime).setAttributes(attrs) );
    }
    return groups;
  } 

  // @since   1.2.0
  private Set _renameChildren(String attr) 
    throws  StemModifyException
  {
    Set     children    = new LinkedHashSet();
    String  modifier    = this.getSession().getMember().getUuid();
    long    modifyTime  = new Date().getTime();
    children.addAll( this._renameChildStemsAndGroups(attr, modifier, modifyTime) );
    children.addAll( this._renameChildGroups(attr, modifier, modifyTime) );
    return children;
  } 

  // @since   1.2.0
  private Set _renameChildStemsAndGroups(String attr, String modifier, long modifyTime) 
    throws  IllegalStateException
  {
    Set       children  = new LinkedHashSet();
    Stem      child;
    Iterator  it        = GrouperDAOFactory.getFactory().getStem().findAllChildStems(this).iterator();
    while (it.hasNext()) {
      child = new Stem();
      child.setDTO( (StemDTO) it.next() );
      child.setSession( this.getSession() );
      if      ( attr.equals(GrouperConfig.ATTR_DE) )  {
        child._getDTO().setDisplayName(
          U.constructName( this._getDTO().getDisplayName(), child._getDTO().getDisplayExtension() ) 
        );
      }
      else if ( attr.equals(GrouperConfig.ATTR_E) )   {
        child._getDTO().setName(
          U.constructName( this._getDTO().getName(), child._getDTO().getExtension() ) 
        );
      }
      else {
        throw new IllegalStateException( "attempt to update invalid naming attribute: " + attr);
      }

      children.addAll( child._renameChildGroups(attr, modifier, modifyTime) );

      children.add( child._getDTO().setModifierUuid(modifier).setModifyTime(modifyTime) );
      children.addAll( child._renameChildStemsAndGroups(attr, modifier, modifyTime) );
    }
    return children;
  } 

  private void _revokeAllNamingPrivs() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, 
            SchemaException
  {
    GrouperSession orig = this.getSession();
    this.setSession( orig.internal_getRootSession() ); // proxy as root
    this.revokePriv(NamingPrivilege.CREATE);
    this.revokePriv(NamingPrivilege.STEM);
    this.setSession(orig);
  } // private void _revokeAllNamingPrivs()

} // public class Stem extends GrouperAPI implements Owner

