/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

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
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.lang.builder.*;

/** 
 * A namespace within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Stem.java,v 1.77 2006-09-06 15:30:40 blair Exp $
 */
public class Stem extends Owner {

  // PROTECTED CLASS CONSTANTS //
  protected static final String ROOT_EXT  = GrouperConfig.EMPTY_STRING; // Appease Oracle
  protected static final String ROOT_INT  = ":";                        // Appease Oracle


  // PRIVATE CLASS CONSTANTS //
  private static final EventLog EL = new EventLog();


  // HIBERNATE PROPERITES //
  private Set     child_groups        = new LinkedHashSet();
  private Set     child_stems         = new LinkedHashSet();
  private String  display_extension;
  private String  display_name;
  private Stem    parent_stem;
  private String  stem_description;
  private String  stem_extension;
  private String  stem_name;


  // PRIVATE TRANSIENT ISNTANCE VARIABLES //
  private transient Subject creator;
  private transient Subject modifier;


  // CONSTRUCTORS //

   // Default constructor for Hibernate.
   // @since    1.0
  protected Stem() {
    super();
  } // protected Stem()

  // Return a stem with an attached session
  protected Stem(GrouperSession s) {
    this.setSession(s);
    this._setCreated();
    this.setUuid(             GrouperUuid.getUuid() );
    this.setStem_name(        ROOT_INT              );
    this.setDisplay_name(     ROOT_INT              );
    this.setStem_extension(   ROOT_INT              );
    this.setDisplay_extension(ROOT_INT              );
  } // protected Stem(s)
  

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
    StopWatch sw = new StopWatch();
    sw.start();
    try {
      AttributeValidator.namingValue(extension);
      AttributeValidator.namingValue(displayExtension);
    }
    catch (ModelException eM) {
      throw new GroupAddException(eM.getMessage(), eM);
    }
    if (
      !PrivilegeResolver.canCREATE(
        this.getSession(), this, this.getSession().getSubject()
      )
    )
    {
      throw new InsufficientPrivilegeException(E.CANNOT_CREATE);
    }
    if (this.equals(StemFinder.findRootStem(this.getSession()))) {
      throw new GroupAddException(
        "cannot create groups at root stem level"
      );
    } 
    try {
      GroupFinder.findByName(
        constructName(this.getName(), extension)
      );
      throw new GroupAddException("group already exists");
    }
    catch (GroupNotFoundException eGNF) {
      // Ignore.  This is what we want.
    }
    try {
      _initializeChildGroupsAndStems(this);
    }
    catch (HibernateException eH) {
      throw new GroupAddException(eH.getMessage(), eH);
    }
    try {
      Group child = new Group(this.getSession(), this, extension, displayExtension);
      // Set parent
      child.setParent_stem(this);
      // Add to children 
      Set children  = this.getChildGroupsNpHi();
      children.add(child);
      this.setChild_groups(children);
      // Now create as member
      Member  m   = new Member( new GrouperSubject(child) );

      // And save
      Set objects = new LinkedHashSet();
      objects.add(child);
      objects.add(this);
      objects.add(m);
      HibernateHelper.save(objects);

      sw.stop();
      EventLog.info(s, M.GROUP_ADD + U.q(child.getName()), sw);
      _grantDefaultPrivsUponCreate(child);

      return child; // And return the newly created group
    }
    catch (Exception e) {
      throw new GroupAddException(
        "Unable to add group " + this.getName() + ":" + extension + ": " 
        + e.getMessage(), e
      );
    }
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
    StopWatch sw = new StopWatch();
    sw.start();
    try {
      AttributeValidator.namingValue(extension);
      AttributeValidator.namingValue(displayExtension);
    }
    catch (ModelException eM) {
      throw new StemAddException(eM.getMessage(), eM);
    }
    if (!RootPrivilegeResolver.canSTEM(this, this.getSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    } 
    String  name  = constructName(this.getName(), extension);
    String  dName = constructName(this.getDisplayName(), displayExtension);
    try {
      StemFinder.findByName(this.getSession(), name);
      throw new StemAddException(E.STEM_EXISTS + U.q(name));
    }
    catch (StemNotFoundException eSNF) {
      // Stem does not exist.  This is what we want.  Now create it.

      try {
        _initializeChildGroupsAndStems(this);
      }
      catch (HibernateException eH) {
        throw new StemAddException(eH.getMessage(), eH);
      }

      try {
        Stem child = new Stem(this.getSession());

        // Set naming attributes
        child.setStem_extension(extension);
        child.setDisplay_extension(displayExtension);
        child.setStem_name(name);
        child.setDisplay_name(dName);
        // Set parent
        child.setParent_stem(this);
        // Add to children 
        Set children  = this.getChildStemsNpHi();
        children.add(child);
        this.setChild_stems(children);
        // And save
        Set objects = new LinkedHashSet();
        objects.add(child);
        objects.add(this);
        HibernateHelper.save(objects);
        sw.stop();
        EventLog.info(s, M.STEM_ADD + U.q(child.getName()), sw);
        _grantDefaultPrivsUponCreate(child);
        return child;
      }
      catch (HibernateException eH) {
        throw new StemAddException(
          "Unable to add stem " + this.getName() + ":" + extension + ": " 
          + eH.getMessage(), eH
        );
      }
    }
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
    GrouperSessionValidator.validate(this.getSession());
    StemValidator.canDeleteStem(this);
    try {
      String name = this.getName();   // Preserve name for logging
      this._revokeAllNamingPrivs();   // Revoke privs
      HibernateHelper.delete(this);   // And delete
      sw.stop();
      EventLog.info(this.getSession(), M.STEM_DEL + U.q(name), sw);
    }
    catch (Exception e) {
      throw new StemDeleteException(e.getMessage(), e);
    }
  } // public void delete()

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Stem)) {
      return false;
    }
    Stem otherStem = (Stem) other;
    return new EqualsBuilder()
      .append(this.getUuid()        , otherStem.getUuid()       )
      .append(this.getCreator_id()  , otherStem.getCreator_id() )
      .isEquals();
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
    Set children = new LinkedHashSet();
    try {
      GrouperSession  s       = this.getSession();
      Subject         subj    = s.getSubject(); 
      Group           child;
      Iterator        iter    = this.getChildGroupsNpHi().iterator();
      while (iter.hasNext()) {
        child = (Group) iter.next();
        child.setSession(s);
        if (RootPrivilegeResolver.canVIEW(child, subj)) {
          children.add(child);
        }
      }
    }
    catch (Exception e) {
      // @exception HibernateException
      // @exception SessionException
      ErrorLog.error(Stem.class, e.getMessage());
    }
    return children;
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
    GrouperSessionValidator.validate(s);
    Set children = new LinkedHashSet();
    try {
      Stem      child;
      Iterator  iter  = this.getChildStemsNpHi().iterator();
      while (iter.hasNext()) {
        child = (Stem) iter.next();
        child.setSession(this.getSession());
        children.add(child);
      }
    }
    catch (HibernateException eH) {
      ErrorLog.error(Stem.class, E.STEM_GETCHILDSTEMS + eH.getMessage());
    }
    return children;
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
    String source = this.getCreate_source();
    if (source == null) {
      source = GrouperConfig.EMPTY_STRING;
    }
    return source;
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
      this.creator = this.getCreator_id().getSubject();
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
    return new Date(this.getCreate_time());
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
      return PrivilegeResolver.getSubjectsWithPriv(
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
    String desc = this.getStem_description();
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
    String val = this.getDisplay_extension();
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
    String val = this.getDisplay_name();
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
    String val = this.getStem_extension();
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
    String source = this.getModify_source();
    if (source == null) {
      source = GrouperConfig.EMPTY_STRING;
    }
    return source;
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
      Member m = this.getModifier_id();
      if (m == null) {
        throw new SubjectNotFoundException(
          "stem has not been modified"
        );
      }
      this.modifier = m.getSubject();
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
    return new Date(this.getModify_time());
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
    String val = this.getStem_name();
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
    Stem parent = this.getParent_stem();
    if (parent == null) {
      throw new StemNotFoundException();
    }
    parent.setSession(this.getSession());
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
    return PrivilegeResolver.getPrivs(this.getSession(), this, subj);
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
      return PrivilegeResolver.getSubjectsWithPriv(
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
    PrivilegeResolver.grantPriv(this.getSession(), this, subj, priv);
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
    return PrivilegeResolver.hasPriv(this.getSession(), this, subj, NamingPrivilege.CREATE);
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
    return PrivilegeResolver.hasPriv(this.getSession(), this, subj, NamingPrivilege.STEM);
  } // public boolean hasStem(subj)
 
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getUuid()        )
      .append(this.getCreator_id()  )
      .toHashCode()
      ;
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
    PrivilegeResolver.revokePriv(this.getSession(), this, priv);
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
    PrivilegeResolver.revokePriv(this.getSession(), this, subj, priv);
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
    if (!RootPrivilegeResolver.canSTEM(this, this.getSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    }
    try {
      this.setStem_description(value);
      this.setModified();
      HibernateHelper.save(this);
    }
    catch (Exception e) {
      throw new StemModifyException(
        "unable to set description: " + e.getMessage(), e
      );
    }
    sw.stop();
    EL.stemSetAttr(this.getSession(), this.getName(), "description", value, sw);
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
      AttributeValidator.namingValue(value);
    }
    catch (ModelException eM) {
      if (!(this.getStem_name().equals(ROOT_INT) && value.equals(ROOT_EXT))) {
        throw new StemModifyException(eM.getMessage(), eM);
      }
      // Appease Oracle
      value = ROOT_INT;
    }
    if (!RootPrivilegeResolver.canSTEM(this, this.getSession().getSubject())) {
      throw new InsufficientPrivilegeException(E.CANNOT_STEM);
    }
    try {
      Set objects = new HashSet();
      _initializeChildGroupsAndStems(this);
      this.setDisplay_extension(value);
      this.setModified();
      try {
        this.setDisplay_name(
          constructName(
            this.getParentStem().getDisplayName(), value
          )
        );
      }
      catch (StemNotFoundException eSNF) {
        // I guess we're the root stem
        this.setDisplay_name(value);
      }
      // Now iterate through all child groups and stems (as root),
      // renaming each.
      GrouperSession  orig  = this.getSession();
      this.setSession( orig.getRootSession() );
      objects.addAll( this._renameChildren() );
      objects.add(this);
      HibernateHelper.save(objects);
      this.setSession(orig);
    }
    catch (Exception e) {
      throw new StemModifyException(
        "unable to set displayExtension: " + e.getMessage(), e
      );
    }
    sw.stop();
    // Reset for logging purposes
    if (value.equals(ROOT_INT)) {
      value = ROOT_EXT;
    }
    EL.stemSetAttr(this.getSession(), this.getName(), "displayExtension", value, sw);
  } // public void setDisplayExtension(value)

  public String toString() {
    return new ToStringBuilder(this)
      .append("displayName" , getDisplay_name() )
      .append("name"        , getName()         )
      .append("uuid"        , getUuid()         )
      .append("creator"     , getCreator_id()   )
      .append("modifier"    , getModifier_id()  )
      .toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //
  protected static Stem addRootStem(GrouperSession s) 
    throws  GrouperRuntimeException
  {
    Stem root = new Stem(s);
    try {
      HibernateHelper.save(root);
    }
    catch (HibernateException eH) {
      String msg = E.STEM_ROOTINSTALL + eH.getMessage();
      ErrorLog.fatal(Stem.class, msg);
      throw new GrouperRuntimeException(msg, eH);
    }
    return root;
  } // protected static Stem addRootStem(GrouperSession s)

  protected static String constructName(String stem, String extn) {
    // TODO This should probably end up in a "naming" utility class
    // TODO Why don't I do validation here?
    if (stem.equals(ROOT_EXT)) {
      return extn;
    }
    return stem + ROOT_INT + extn;
  } // protected static String constructName(stem, extn)


  // PROTECTED INSTANCE METHODS //
  
  // @since 1.0
  protected Set getChildGroupsNpHi() 
    throws  HibernateException
  {
    _initializeChildGroupsAndStems(this);
    return this.getChild_groups();
  } // protected Set getChildGroupsNpHi()

  // @since 1.0
  protected Set getChildStemsNpHi() 
    throws  HibernateException
  {
    _initializeChildGroupsAndStems(this);
    return this.getChild_stems();
  } // protected Set getChildStemsNpHi()

  protected void setModified() {
    this.setModifier_id( s.getMember()        );
    this.setModify_time( new Date().getTime() );
  } // protected void setModified()


  // PRIVATE CLASS METHODS //

  // The child_groups and child_stems collections have lazy
  // associations so we need to manually initialize as needed.
  // TODO Deprecate in favor of `getChildGroupsNpHi()` and `getChildStemsNpHi()`?
  private static void _initializeChildGroupsAndStems(Stem ns) 
    throws  HibernateException
  {
    Session     hs  = HibernateHelper.getSession();
    Transaction tx  = hs.beginTransaction();
    hs.load(ns, ns.getId());
    Hibernate.initialize( ns.getChild_stems() );
    Hibernate.initialize( ns.getChild_groups() );
    tx.commit();
    hs.close();
  } // private static void _initializeChildGroupsAndStems(ns) 


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
    // TODO Unfortunately this sets the modify* attrs
    try {
      GrouperSession  orig  = this.s;
      GrouperSession  root  = orig.getRootSession();
      g.setSession(root);
      PrivilegeResolver.grantPriv(root, g, orig.getSubject(), AccessPrivilege.ADMIN);

      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate(
        root, g, AccessPrivilege.ADMIN  , GrouperConfig.GCGAA
      );
      this._grantOptionalPrivUponCreate(
        root, g, AccessPrivilege.OPTIN  , GrouperConfig.GCGAOI
      );
      this._grantOptionalPrivUponCreate(
        root, g, AccessPrivilege.OPTOUT , GrouperConfig.GCGAOO
      );
      this._grantOptionalPrivUponCreate(
        root, g, AccessPrivilege.READ   , GrouperConfig.GCGAR
      );
      this._grantOptionalPrivUponCreate(
        root, g, AccessPrivilege.UPDATE , GrouperConfig.GCGAU
      );
      this._grantOptionalPrivUponCreate(
        root, g, AccessPrivilege.VIEW   , GrouperConfig.GCGAV
      );

      g.setSession(orig);
    }
    catch (Exception e) {
      throw new GroupAddException(
        "group created but unable to grant ADMIN to creator: " + e.getMessage(), e
      );
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
    // TODO Unfortunately this sets the modify* attrs
    try {
      GrouperSession  orig  = this.s;
      GrouperSession  root  = orig.getRootSession();
      ns.setSession(root);
      PrivilegeResolver.grantPriv(root, ns, orig.getSubject(), NamingPrivilege.STEM);

      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate(
        root, ns, NamingPrivilege.CREATE, GrouperConfig.SCGAC
      );
      this._grantOptionalPrivUponCreate(
        root, ns, NamingPrivilege.STEM  , GrouperConfig.SCGAS
      );

      ns.setSession(orig);
    }
    catch (Exception e) {
      throw new StemAddException(
        "stem created but unable to grant STEM to creator: " + e.getMessage(), e
      );
    }
  } // private void _grantDefaultPrivsUponCreate(ns)

  private void _grantOptionalPrivUponCreate(
    GrouperSession root, Object o, Privilege p, String opt
  ) 
    throws  Exception
  {
    Subject       all = SubjectFinder.findAllSubject();
    if (GrouperConfig.getProperty(opt).equals(GrouperConfig.BT)) {
      StopWatch sw = new StopWatch();
      sw.start();
      if      (o.getClass().equals(Group.class)) {
        Group g = (Group) o;
        PrivilegeResolver.grantPriv(root, g, all, p);
        sw.stop();
        EL.groupGrantPriv(this.getSession(), g.getName(), all, p, sw);
      }
      else if (o.getClass().equals(Stem.class)) {
        Stem ns = (Stem) o;
        PrivilegeResolver.grantPriv(root, ns, all, p);
        sw.stop();
        EL.stemGrantPriv(this.getSession(), ns.getName(), all, p, sw);
      }
    }
  } // private void _grantOptionalPrivUponCreate(root, o, p, opt)

  private Set _renameChildGroups() 
    throws  HibernateException
  {
    Set       objects = new LinkedHashSet();
    Group     child;
    Iterator  iter    = this.getChild_groups().iterator();
    while (iter.hasNext()) {
      child = (Group) iter.next();
      child.setSession(this.s);
      child.setDisplayName(
        constructName( this.getDisplayName(), child.getDisplayExtension() )
      );
      child.setModified();
      objects.add(child);
    }
    return objects;
  } // private Set _renameChildGroups()

  private Set _renameChildren() 
    throws  StemModifyException
  {
    Set objects = new LinkedHashSet();
    try {
      objects.addAll(this._renameChildStemsAndGroups());
      objects.addAll(this._renameChildGroups());
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
    Set       objects = new LinkedHashSet();
    Stem      child;
    Iterator  iter    = this.getChild_stems().iterator();
    while (iter.hasNext()) {
      child = (Stem) iter.next();    
      child.setSession(this.s);
      _initializeChildGroupsAndStems(child);
      child.setDisplay_name(
        constructName( this.getDisplayName(), child.getDisplayExtension() )
      );
      objects.addAll( child._renameChildGroups() );
      child.setModified();
      objects.add(child);
      objects.addAll( child._renameChildStemsAndGroups() );
    }
    return objects;
  } // private Set _renameChildStemsAndGroups()

  private void _revokeAllNamingPrivs() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException, 
            SchemaException
  {
    GrouperSession orig = this.getSession();
    this.setSession( orig.getRootSession() ); // proxy as root
    this.revokePriv(NamingPrivilege.CREATE);
    this.revokePriv(NamingPrivilege.STEM);
    this.setSession(orig);
  } // private void _revokeAllNamingPrivs()

  private void _setCreated() {
    this.setCreator_id( s.getMember()         );
    this.setCreate_time( new Date().getTime() );
  } // private void _setCreated()


  // GETTERS //
  private Set getChild_groups() { 
    return this.child_groups;
  }
  private Set getChild_stems() { 
    return this.child_stems;
  }
  private String getDisplay_extension() {
    return this.display_extension;
  }
  private String getDisplay_name() {
    return this.display_name;
  }
  private Stem getParent_stem() {
    return this.parent_stem;
  }
  private String getStem_description() {
    return this.stem_description;
  }
  private String getStem_extension() {
    return this.stem_extension;
  }
  private String getStem_name() {
    return this.stem_name;
  }


  // SETTERS //
  private void setChild_groups(Set child_groups) {
    this.child_groups = child_groups;
  }
  private void setChild_stems(Set child_stems) {
    this.child_stems = child_stems;
  }
  private void setDisplay_extension(String display_extension) {
    this.display_extension = display_extension;
  }
  private void setDisplay_name(String display_name) {
    this.display_name = display_name;
  }
  private void setParent_stem(Stem parent_stem) {
    this.parent_stem = parent_stem;
  }
  private void setStem_description(String stem_description) {
    this.stem_description = stem_description;
  }
  private void setStem_extension(String stem_extension) {
    this.stem_extension = stem_extension;
  }
  private void setStem_name(String stem_name) {
    this.stem_name = stem_name;
  }

}

