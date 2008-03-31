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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.time.StopWatch;

import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dto.GroupDTO;
import edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import edu.internet2.middleware.grouper.internal.dto.StemDTO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.internal.util.ParameterHelper;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.internal.util.U;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.SourceUnavailableException;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;

/** 
 * A namespace within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Stem.java,v 1.149 2008-03-31 07:19:48 mchyzer Exp $
 */
public class Stem extends GrouperAPI implements Owner {

  /** param helper */
  private ParameterHelper param = new ParameterHelper();



  /**
   * Search scope: one-level or subtree.
   * @since   1.2.1
   */
  public enum Scope { 
    /** one level (direct children) */
    ONE, 
    
    /** all decendents */
    SUB 
  }; // TODO 20070802 is this the right location?

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
  /** root int */
  protected static final String ROOT_INT = ":"; // Appease Oracle, et. al.


  // PRIVATE CLASS CONSTANTS //
  /** event log */
  private static final EventLog EL = new EventLog();


  // PRIVATE INSTANCE VARIABLES //
  /** creator of stem */
  private Subject creator;
  /** modifier of stem */
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
    if ( !PrivilegeHelper.canStem( this, this.getSession().getSubject() ) ) {
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
   * Get groups that are immediate children of this stem.
   * @return  Set of {@link Group} objects.
   * @see     Stem#getChildGroups(Scope)
   */
  public Set getChildGroups() {
    return this.getChildGroups(Scope.ONE);
  }

  /**
   * Get groups that are children of this stem.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @return  Child groups.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Group> getChildGroups(Scope scope) 
    throws  IllegalArgumentException
  {
    if (scope == null) { // TODO 20070815 ParameterHelper
      throw new IllegalArgumentException("null Scope");
    }
    Subject     subj    = this.getSession().getSubject();
    Group       child;
    Set<Group>  groups  = new LinkedHashSet();
    for ( GroupDTO dto : GrouperDAOFactory.getFactory().getStem().findAllChildGroups( this._getDTO(), scope ) ) {
      child = new Group();
      child.setDTO(dto);
      child.setSession( this.getSession() ); 
      
      if ( PrivilegeHelper.canView( this.getSession().internal_getRootSession(), child, subj ) ) {
        groups.add(child);
      }
    }
    return groups;
  }

  /**
   * get child groups
   * @param privileges privs 
   * @param scope all or direct
   * @return  Child groups where current subject has any of the specified <i>privileges</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public Set<Group> getChildGroups(Privilege[] privileges, Scope scope)
    throws  IllegalArgumentException 
  {
    this.param.notNullPrivilegeArray(privileges);

    Set<Group> groups = new LinkedHashSet();
    // TODO 20070814 this is hideously ugly and far from optimal
    for ( Group group : this.getChildGroups(scope) ) {
      for ( Privilege priv : PrivilegeHelper.getAccessPrivileges(privileges) ) {
        try {
          PrivilegeHelper.dispatch( this.getSession(), group, this.getSession().getSubject(), priv );
          groups.add(group);
          break; // we only care that one privilege matches
        }
        catch (InsufficientPrivilegeException eIP) {
          // ignore
        }
        catch (SchemaException eSchema) {
          // ignore
        }
      }
    }
    return groups;
  }

  /**
   * Get stems that are immediate children of this stem.
   * @return  Set of {@link Stem} objects.
   * @see     Stem#getChildStems(Scope)
   */
  public Set getChildStems() {
    return this.getChildStems(Scope.ONE);
  }

  /**
   * Get stems that are children of this stem.
   * @param   scope of search: <code>Scope.ONE</code> or <code>Scope.SUB</code>
   * @return  Child stems.
   * @throws  IllegalArgumentException if null scope.
   * @since   1.2.1
   */
  public Set<Stem> getChildStems(Scope scope) 
    throws  IllegalArgumentException
  {
    if (scope == null) { // TODO 20070815 ParameterHelper
      throw new IllegalArgumentException("null Scope");
    }
    Stem      child;
    Set<Stem> stems = new LinkedHashSet();
    for ( StemDTO dto : GrouperDAOFactory.getFactory().getStem().findAllChildStems( this._getDTO(), scope ) ) {
      child = new Stem();
      child.setDTO(dto);
      child.setSession( this.getSession() ); 
      stems.add(child);
    }
    return stems;
  }

  /**
   * get child stems
   * @param privileges privs
   * @param scope all or direct
   * @return  Child (or deeper) stems where current subject has any of the specified <i>privileges</i>.  Parent stems of grandchild (or deeper) groups where the current subject has any of the specified <i>privileges</i>.
   * @throws  IllegalArgumentException if any parameter is null.
   * @since   1.2.1
   */
  public Set<Stem> getChildStems(Privilege[] privileges, Scope scope)
    throws  IllegalArgumentException 
  {
    this.param.notNullPrivilegeArray(privileges); 

    Set<Stem> stems = new LinkedHashSet();
    // TODO 20070824 this could be a lot prettier
    for ( Stem stem : this.getChildStems(scope) ) {

      for ( Privilege priv : PrivilegeHelper.getNamingPrivileges(privileges) ) {
        try {
          PrivilegeHelper.dispatch( this.getSession(), stem, this.getSession().getSubject(), priv );
          stems.add(stem);
          break; // we only care that one privilege matches
        }
        catch (InsufficientPrivilegeException eIP) {
          // ignore
        }
        catch (SchemaException eSchema) {
          // ignore
        }
      }

      if ( !stems.contains(stem) ) { // no matching naming privileges so checking access privilegees
        // filtering out naming privileges will happen in "#getChildGroups(Privilege[], Scope)"
        for ( Group group : stem.getChildGroups(privileges, scope) ) {
          stems.add( group.getParentStem() );
        }
      }

    }
    return stems;
  }

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
    return this.getSession().getNamingResolver().getSubjectsWithPrivilege(this, NamingPrivilege.CREATE);
  }

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
   * @throws StemNotFoundException if stem not found
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
    return this.getSession().getNamingResolver().getPrivileges(this, subj);
  } 

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
    return this.getSession().getNamingResolver().getSubjectsWithPrivilege(this, NamingPrivilege.STEM);
  } 

  /**
   */
  public String getUuid() {
    return this._getDTO().getUuid();
  } // public String getUuid()

  /**
   * Grant a privilege on this stem.
   * <pre class="eg">
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
    throws  GrantPrivilegeException,        // TODO 20070820 stop throwing
            InsufficientPrivilegeException, // TODO 20070820 stop throwing
            SchemaException                 // TODO 20070820 stop throwing
  {
    StopWatch sw = new StopWatch();
    sw.start();
    try {
      this.getSession().getNamingResolver().grantPrivilege(this, subj, priv);
    }
    catch (UnableToPerformException eUTP) {
      throw new GrantPrivilegeException( eUTP.getMessage(), eUTP );
    }
    sw.stop();
    EL.stemGrantPriv(this.getSession(), this.getName(), subj, priv, sw);
  } 

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
    return this.getSession().getNamingResolver().hasPrivilege(this, subj, NamingPrivilege.CREATE);
  } 
 
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
    return this.getSession().getNamingResolver().hasPrivilege(this, subj, NamingPrivilege.STEM);
  } 
 
  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  /**
   * TODO 20070813 make public?
   * @param group group
   * @return  True if <i>group</i> is child, at any depth, of this stem.
   * @throws  IllegalArgumentException if <i>group</i> is null.
   * @since   1.2.1
   */
  protected boolean isChildGroup(Group group)
    throws  IllegalArgumentException
  {
    if (group == null) { // TODO 20070813 ParameterHelper
      throw new IllegalArgumentException("null Group");
    }

    if (this.isRootStem()) {
      return true;
    } 

    String stemName = this.getName();
    String groupName = group.getName();

    if (groupName.length() <= (stemName.length() + DELIM.length())) {
      return false;
    }
    
    if ((stemName + DELIM).equals(groupName.substring(0, stemName.length() + DELIM.length()))) {
      return true;
    }

    return false;
  }

  /**
   * TODO 20070813 make public?
   * @param stem stem
   * @return  True if <i>stem</i> is child, at any depth, of this stem.
   * @throws  IllegalArgumentException if <i>stem</i> is null.
   * @since   1.2.1
   */
  protected boolean isChildStem(Stem stem) 
    throws  IllegalArgumentException
  {
    if (stem == null) { // TODO 20070813 ParameterHelper
      throw new IllegalArgumentException("null Stem");
    }

    String thisName = this.getName();
    String stemName = stem.getName();

    if (
         ( thisName.equals( stemName ) )  // can't be child of self
         ||
         stem.isRootStem()                            // root stem can't be child
       )
    {
      return false;
    }
    if ( this.isRootStem() ) {
      return true; // all stems are children
    }

    if (stemName.length() <= (thisName.length() + DELIM.length())) {
      return false;
    }
    
    if ((thisName + DELIM).equals(stemName.substring(0, thisName.length() + DELIM.length()))) {
      return true;
    }

    return false;
  }

  /**
   * @return  Boolean true if this is the root stem of the Groups Registry.
   * @since   1.2.0
   */
  public boolean isRootStem() {
    return ROOT_INT.equals( this._getDTO().getName() );
  } 

  /**
   * Revoke all privileges of the specified type on this stem.
   * <pre class="eg">
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
    throws  InsufficientPrivilegeException, // TODO 20070820 stop throwing this
            RevokePrivilegeException,
            SchemaException                 // TODO 20070820 stop throwing this
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( Privilege.isAccess(priv) ) {
      throw new SchemaException("attempt to use access privilege");
    }
    try {
      this.getSession().getNamingResolver().revokePrivilege(this, priv);
    }
    catch (UnableToPerformException e) {
      throw new RevokePrivilegeException( e.getMessage(), e );
    }
    sw.stop();
    EL.stemRevokePriv(this.getSession(), this.getName(), priv, sw);
  }
 
  /**
   * Revoke a privilege on this stem.
   * <pre class="eg">
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
    throws  InsufficientPrivilegeException, // TODO 20070820 stop throwing this
            RevokePrivilegeException,
            SchemaException                 // TODO 20070820 stop throwing this
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( Privilege.isAccess(priv) ) {
      throw new SchemaException("attempt to use access privilege");
    }
    try {
      this.getSession().getNamingResolver().revokePrivilege(this, subj, priv);
    }
    catch (UnableToPerformException e) {
      throw new RevokePrivilegeException( e.getMessage(), e );
    }
    sw.stop();
    EL.stemRevokePriv(this.getSession(), this.getName(), subj, priv, sw);
  } 
 
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
    if ( !PrivilegeHelper.canStem( this, this.getSession().getSubject() ) ) {
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
  }

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
    if ( !PrivilegeHelper.canStem( this, this.getSession().getSubject() ) ) {
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
    if ( !PrivilegeHelper.canStem( this, this.getSession().getSubject() ) ) {
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

  /**
   * add root stem
   * @param s session
   * @since   1.2.0
   * @return stem
   * @throws GrouperRuntimeException is problem
   */
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

  /**
   * set modified
   * @since   1.2.0
   */
  protected void internal_setModified() {
    this._getDTO().setModifierUuid( this.getSession().getMember().getUuid() );
    this._getDTO().setModifyTime(  new Date().getTime()    );
  } // protected void internal_setModified()


  // PROTECTED INSTANCE METHODS //

  /**
   * add child group with uuid
   * @param extn extension
   * @param dExtn display extension
   * @param uuid uuid
   * @return group 
   * @throws GroupAddException if problem 
   * @throws InsufficientPrivilegeException if problem 
   * @since   1.2.0
   */
  protected Group internal_addChildGroup(String extn, String dExtn, String uuid) 
    throws  GroupAddException,
            InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( !PrivilegeHelper.canCreate( this.getSession(), this, this.getSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_CREATE);
    } 
    GrouperValidator v = AddGroupValidator.validate(this, extn, dExtn);
    if (v.isInvalid()) {
      throw new GroupAddException( v.getErrorMessage() );
    }
    try {
      Map<String, String> attrs = new HashMap<String, String>();
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
      //CH 20080220: this will start saving the stem
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

  /**
   * add child stem with uuid
   * @since   1.2.0
   * @param extn extension
   * @param dExtn display extension
   * @param uuid uuid
   * @return the new stem
   * @throws StemAddException if problem
   * @throws InsufficientPrivilegeException if problem
   */
  protected Stem internal_addChildStem(String extn, String dExtn, String uuid) 
    throws  StemAddException,
            InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();
    if ( !PrivilegeHelper.canStem( this, this.getSession().getSubject() ) ) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    } 
    GrouperValidator v = AddStemValidator.validate(this, extn, dExtn);
    if (v.isInvalid()) {
      throw new StemAddException( "Problem with stem extension: '" + extn 
          + "', displayExtension: '" + dExtn + "', " + v.getErrorMessage() );
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
  /**
   * get data transfer object
   * @return the dto
   */
  private StemDTO _getDTO() {
    return (StemDTO) super.getDTO();
  }
  
  /**
   * <pre>
   * Now grant ADMIN (as root) to the creator of the child group.
   *
   * Ideally this would be wrapped up in the broader transaction
   * of adding the child stem but as the interfaces may be
   * outside of our control, I don't think we can do that.  
   *
   * Possibly a bug. The modify* attrs get set when granting ADMIN at creation.
   * </pre>
   * @param g group
   * @throws GroupAddException if problem
   */
  private void _grantDefaultPrivsUponCreate(Group g)
    throws  GroupAddException
  {
    try {
      this.getSession().internal_getRootSession().getAccessResolver().grantPrivilege(
        g, this.getSession().getSubject(), AccessPrivilege.ADMIN       
      );

      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate( g, AccessPrivilege.ADMIN, GrouperConfig.GCGAA );
      this._grantOptionalPrivUponCreate( g, AccessPrivilege.OPTIN, GrouperConfig.GCGAOI );
      this._grantOptionalPrivUponCreate( g, AccessPrivilege.OPTOUT, GrouperConfig.GCGAOO );
      this._grantOptionalPrivUponCreate( g, AccessPrivilege.READ, GrouperConfig.GCGAR );
      this._grantOptionalPrivUponCreate( g, AccessPrivilege.UPDATE, GrouperConfig.GCGAU );
      this._grantOptionalPrivUponCreate( g, AccessPrivilege.VIEW, GrouperConfig.GCGAV );
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
    catch (UnableToPerformException eUTP) {
      throw new GroupAddException( eUTP.getMessage(), eUTP );
    }
  } 
  /**
   * Now grant STEM (as root) to the creator of the child stem.
   *
   * Ideally this would be wrapped up in the broader transaction
   * of adding the child stem but as the interfaces may be
   * outside of our control, I don't think we can do that.  
   *
   * Possibly a bug. The modify* attrs get set when granting privs at creation.
   * 
   * @param ns stem
   * @throws StemAddException if problem
   */
  private void _grantDefaultPrivsUponCreate(Stem ns)
    throws  StemAddException
  {
    try {
      this.getSession().internal_getRootSession().getNamingResolver().grantPrivilege(
        ns, this.getSession().getSubject(), NamingPrivilege.STEM
      );

      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate(
        ns, NamingPrivilege.CREATE, GrouperConfig.SCGAC
      );
      this._grantOptionalPrivUponCreate(
        ns, NamingPrivilege.STEM, GrouperConfig.SCGAS
      );
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
    catch (UnableToPerformException eUTP) {
      throw new StemAddException( eUTP.getMessage(), eUTP );
    }
  } 

  /**
   * grant optional priv upon create
   * @param o object
   * @param p prov
   * @param opt opt
   * @throws GrantPrivilegeException if problem 
   * @throws  IllegalStateException if <i>o</i> is neither group nor stem.
   * @throws InsufficientPrivilegeException if not privs
   * @throws SchemaException if problem
   * @throws UnableToPerformException if problem
   * @since   1.2.1
   */
  private void _grantOptionalPrivUponCreate(Object o, Privilege p, String opt) 
    throws  GrantPrivilegeException,
            IllegalStateException,
            InsufficientPrivilegeException,
            SchemaException,
            UnableToPerformException
  {
    Subject       all = SubjectFinder.findAllSubject();
    if (GrouperConfig.getProperty(opt).equals(GrouperConfig.BT)) {
      StopWatch sw = new StopWatch();
      sw.start();
      if      (o instanceof Group) {
        Group g = (Group) o;
        this.getSession().getAccessResolver().grantPrivilege(g, all, p);
        sw.stop();
        EL.groupGrantPriv(this.getSession(), g.getName(), all, p, sw);
      }
      else if (o instanceof Stem) {
        Stem ns = (Stem) o;
        this.getSession().getNamingResolver().grantPrivilege(ns, all, p);
        sw.stop();
        EL.stemGrantPriv(this.getSession(), ns.getName(), all, p, sw);
      }
      else {
        throw new IllegalStateException("unexpected condition: object is not group or stem: " + o);
      }
    }
  } 

  /**
   * rename child groups
   * @since   1.2.0
   * @param attr
   * @param modifier
   * @param modifyTime
   * @return the set of GroupDTO's
   */
  private Set _renameChildGroups(String attr, String modifier, long modifyTime) {
    Map<String, String> attrs;
    GroupDTO            _g;
    Set                 groups  = new LinkedHashSet();
    Iterator            it      = GrouperDAOFactory.getFactory().getStem().findAllChildGroups( this._getDTO(), Stem.Scope.ONE ).iterator();
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

  /**
   * rename children.
   * @since   1.2.0
   * @param attr attr
   * @return the set of StemDTO
   * @throws StemModifyException if problem
   */
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

  /**
   * rename child stems and groups.
   * @param attr sttr
   * @param modifier modifier
   * @param modifyTime modify time
   * @return the set of StemDTO's
   * @throws IllegalStateException if problem
   */
  private Set _renameChildStemsAndGroups(String attr, String modifier, long modifyTime) 
    throws  IllegalStateException
  {
    Set       children  = new LinkedHashSet();
    Stem      child;
    Iterator  it        = GrouperDAOFactory.getFactory().getStem().findAllChildStems( this._getDTO(), Scope.ONE ).iterator();
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

  /**
   * revoke naming privs
   * @throws InsufficientPrivilegeException if problem
   * @throws RevokePrivilegeException if problem
   * @throws SchemaException if problem
   */
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

  /**
   * create stems and parents if not exist.
   * @param stemName
   * @param grouperSession 
   * @return the resulting stem
   * @throws InsufficientPrivilegeException 
   * @throws StemNotFoundException 
   * @throws StemAddException 
   */
  static Stem _createStemAndParentStemsIfNotExist(GrouperSession grouperSession, String stemName)
     throws InsufficientPrivilegeException, StemNotFoundException, StemAddException {
    String[] stems = StringUtils.split(stemName, ':');
    Stem currentStem = StemFinder.findRootStem(grouperSession);
    String currentName = stems[0];
    for (int i=0;i<stems.length;i++) {
      try {
        currentStem = StemFinder.findByName(grouperSession, currentName);
      } catch (StemNotFoundException snfe1) {
        //this isnt ideal, but just use the extension as the display extension
        currentStem = currentStem.addChildStem(stems[i], stems[i]);
      }
      //increment the name, dont worry if on the last one, we are done
      if (i < stems.length-1) {
        currentName += ":" + stems[i+1];
      }
    }
    //at this point the stem should be there (and is equal to currentStem), just to be sure, query again
    Stem parentStem = StemFinder.findByName(grouperSession, stemName);
    return parentStem;

  }
  
  /**
   * <pre>
   * create or update a stem.  Note this will not rename a stem at this time (might in future)
   * 
   * This is a static method since setters to Stem objects persist to the DB
   * 
   * Steps:
   * 
   * 1. Find the stem by stemNameToEdit (if not there then its an insert)
   * 2. Internally set all the fields of the stem (no need to reset if already the same)
   * 3. Store the stem (insert or update) if needed
   * 4. Return the stem object
   * 
   * This occurs in a transaction, so if a part of it fails, it rolls back, and potentially
   * rolls back outer transactions too
   * </pre>
   * @param grouperSession to act as
   * @param stemNameToEdit is the name of the stem to edit (or null if insert)
   * @param description new description for stem
   * @param displayExtension display friendly name for this stem only
   * (parent stems are not specified)
   * @param name this is required, and is the full name of the stem
   * including the names of parent stems.  e.g. stem1:stem2:stem3
   * the parent stem must exist unless createParentStemsIfNotExist.  
   * Can rename a stem extension, but not the parent stem name (move)
   * @param uuid of the stem.  uuid for an inserted stem
   * @param saveMode to constrain if insert only or update only, if null defaults to INSERT_OR_UPDATE
   * @param createParentStemsIfNotExist true if the stems should be created if they dont exist, false
   * for StemNotFoundException if not exist.  Note, the display extension on created stems
   * will equal the extension.  This could be dangerous and should probably only be used for testing
   * @return the stem that was updated or created
   * @throws StemNotFoundException 
   * @throws InsufficientPrivilegeException 
   * @throws StemAddException 
   * @throws StemModifyException 
   */
  public static Stem saveStem(final GrouperSession grouperSession, final String stemNameToEdit,
      final String uuid, final String name, final String displayExtension, final String description, 
      SaveMode saveMode, final boolean createParentStemsIfNotExist) 
        throws StemNotFoundException,
      InsufficientPrivilegeException,
      StemAddException, StemModifyException {
  
    //default to insert or update
    saveMode = (SaveMode)ObjectUtils.defaultIfNull(saveMode, SaveMode.INSERT_OR_UPDATE);
    final SaveMode SAVE_MODE = saveMode;
    try {
      //do this in a transaction
      Stem stem = (Stem)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
        public Object callback(GrouperTransaction grouperTransaction)
            throws GrouperDAOException {
          try {
            String stemNameForError = GrouperUtil.defaultIfBlank(stemNameToEdit, name);
            
            int lastColonIndex = name.lastIndexOf(':');
            boolean topLevelStem = lastColonIndex < 0;
    
            //empty is root stem
            String parentStemNameNew = GrouperUtil.parentStemNameFromName(name);
            String extensionNew = GrouperUtil.extensionFromName(name);
            
            //lets find the stem
            Stem parentStem = null;
            
            try {
              parentStem = topLevelStem ? StemFinder.findRootStem(grouperSession) 
                  : StemFinder.findByName(grouperSession, parentStemNameNew);
            } catch (StemNotFoundException snfe) {
              
              //see if we should fix this problem
              if (createParentStemsIfNotExist) {
                
                //at this point the stem should be there (and is equal to currentStem), 
                //just to be sure, query again
                parentStem = _createStemAndParentStemsIfNotExist(grouperSession, parentStemNameNew);
              } else {
                throw new StemNotFoundException("Cant find stem: '" + parentStemNameNew 
                    + "' (from update on stem name: '" + stemNameForError + "')");
              }
            }
            
            Stem theStem = null;
            //see if update
            boolean isUpdate = SAVE_MODE.isUpdate(stemNameToEdit);
    
            if (isUpdate) {
              String parentStemNameLookup = GrouperUtil.parentStemNameFromName(stemNameToEdit);
              if (!StringUtils.equals(parentStemNameLookup, parentStemNameNew)) {
                throw new StemModifyException("Can't move a stem.  Existing parentStem: '"
                    + parentStemNameLookup + "', new stem: '" + parentStemNameNew + "'");
            }    
            try {
                theStem = StemFinder.findByName(grouperSession, stemNameToEdit);
                
                //while we are here, make sure uuid's match if passed in
                if (!StringUtils.isBlank(uuid) && !StringUtils.equals(uuid, theStem.getUuid())) {
                  throw new RuntimeException("UUID stem changes are not supported: new: " + uuid + ", old: " 
                      + theStem.getUuid() + ", " + stemNameForError);
                }
                
              } catch (StemNotFoundException snfe) {
                //if update we have a problem
                if (SAVE_MODE.equals(SaveMode.INSERT_OR_UPDATE) || SAVE_MODE.equals(SaveMode.INSERT)) {
                  isUpdate = false;
                } else {
                    throw snfe;
                }
              }
            }
            
            //if inserting
            if (!isUpdate) {
              if (StringUtils.isBlank(uuid)) {
                //if no uuid
                theStem = parentStem.addChildStem(extensionNew, displayExtension);
              } else {
                //if uuid
                theStem = parentStem.internal_addChildStem(extensionNew, displayExtension, uuid);
              }
            } else {
              //check if different so it doesnt make unneeded queries
              if (!StringUtils.equals(theStem.getExtension(), extensionNew)) {
                theStem.setExtension(extensionNew);
              }
              if (!StringUtils.equals(theStem.getDisplayExtension(), displayExtension)) {
                theStem.setDisplayExtension(displayExtension);
              }
            }
            
            //now compare and put all attributes (then store if needed)
            if (!StringUtils.equals(theStem.getDescription(), description)) {
              theStem.setDescription(description);
            }
            
            return theStem;
            //wrap checked exceptions inside unchecked, and rethrow outside
          } catch (StemNotFoundException snfe) {
            throw new RuntimeException(snfe);
          } catch (InsufficientPrivilegeException ipe) {
            throw new RuntimeException(ipe);
          } catch (StemAddException sae) {
            throw new RuntimeException(sae);
          } catch (StemModifyException sme) {
            throw new RuntimeException(sme);
          }
        }
        
      });
      return stem;
    } catch (RuntimeException re) {
      
      Throwable throwable = re.getCause();
      if (throwable instanceof StemNotFoundException) {
        throw (StemNotFoundException)throwable;
      }
      if (throwable instanceof InsufficientPrivilegeException) {
        throw (InsufficientPrivilegeException)throwable;
      }
      if (throwable instanceof StemAddException) {
        throw (StemAddException)throwable;
      }
      if (throwable instanceof StemModifyException) {
        throw (StemModifyException)throwable;
      }
      //must just be runtime
      throw re;
    }
    
  }

} // public class Stem extends GrouperAPI implements Owner

