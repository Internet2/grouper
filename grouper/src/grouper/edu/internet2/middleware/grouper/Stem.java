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
import  java.io.Serializable;
import  java.util.*;
import  java.util.regex.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.time.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * A namespace within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Stem.java,v 1.49 2006-03-10 18:03:15 blair Exp $
 *     
*/
public class Stem implements Serializable {

  // Protected Class Constants
  protected static final String ROOT_EXT  = "";   // Appease Oracle
  protected static final String ROOT_INT  = ":";  // Appease Oracle


  // Private Class Constants
  // TODO use one in GrouperConfig
  private static final String   BT          = "true";
  // TODO move to GrouperConfig
  private static final String   CFG_GCGAA   = "groups.create.grant.all.admin";
  private static final String   CFG_GCGAOI  = "groups.create.grant.all.optin";
  private static final String   CFG_GCGAOO  = "groups.create.grant.all.optout";
  private static final String   CFG_GCGAR   = "groups.create.grant.all.read";
  private static final String   CFG_GCGAU   = "groups.create.grant.all.update";
  private static final String   CFG_GCGAV   = "groups.create.grant.all.view";
  private static final String   CFG_SCGAC   = "stems.create.grant.all.create";
  private static final String   CFG_SCGAS   = "stems.create.grant.all.stem";
  private static final EventLog EL          = new EventLog();
  private static final String   ERR_ARS     = "unable to install root stem: ";
  private static final String   ERR_FNF     = "field not found: ";
  private static final Log      LOG         = LogFactory.getLog(Stem.class);
  private static final Pattern  RE_COLON    = Pattern.compile(":");
  private static final Pattern  RE_WS       = Pattern.compile("^\\s*$");


  // Hibernate Properties
  private String  id;
  private Set     child_groups        = new LinkedHashSet();
  private Set     child_stems         = new LinkedHashSet();
  private String  create_source;
  private long    create_time;
  private Member  creator_id;
  private String  display_extension;
  private String  display_name;
  private Member  modifier_id;
  private String  modify_source;
  private long    modify_time;
  private Stem    parent_stem;
  private Status  status;
  private String  stem_description;
  private String  stem_extension;
  private String  stem_id;
  private String  stem_name;
  private int     version;


  // Transient Instance Variables
  private transient Subject         creator;
  private transient Subject         modifier;
  private transient GrouperSession  s;


  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public Stem() {
    // Nothing
  }

  // Return a stem with an attached session
  protected Stem(GrouperSession s) {
    this.s = s;
    this._setCreated();
    this.setStem_id(          GrouperUuid.getUuid() );
    this.setStem_name(        ROOT_INT              );
    this.setDisplay_name(     ROOT_INT              );
    this.setStem_extension(   ROOT_INT              );
    this.setDisplay_extension(ROOT_INT              );
  } // protected Stem(s)
  

  // Public Instance Methods

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
      validateName(extension);
      validateName(displayExtension);
    }
    catch (IllegalArgumentException eIA) {
      throw new GroupAddException(eIA.getMessage());
    }
    PrivilegeResolver.getInstance().canCREATE(
      this.s, this, this.s.getSubject()
    );
    if (this.equals(StemFinder.findRootStem(this.s))) {
      throw new GroupAddException(
        "cannot create groups at root stem level"
      );
    } 
    try {
      Group g = GroupFinder.findByName(
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
      throw new GroupAddException(eH.getMessage());
    }
    try {
      Group child = new Group(this.s, this, extension, displayExtension);
      // Set parent
      child.setParent_stem(this);
      // Add to children 
      Set children  = this.getChild_groups();
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
      EL.stemAddChildGroup(this.s, child.getName(), sw);
      _grantDefaultPrivsUponCreate(child);
      // And return the newly created group
      return child;
    }
    catch (Exception e) {
      throw new GroupAddException(
        "Unable to add group " + this.getName() + ":" + extension + ": " 
        + e.getMessage()
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
      validateName(extension);
      validateName(displayExtension);
    }
    catch (IllegalArgumentException eIA) {
      throw new StemAddException(eIA.getMessage());
    }
    PrivilegeResolver.getInstance().canSTEM(
      //GrouperSessionFinder.getRootSession(), this, this.s.getSubject()
      this.s, this, this.s.getSubject()
    );
    try {
      Stem ns = StemFinder.findByName(
        this.s, constructName(this.getName(), extension)
      );
      throw new StemAddException("stem already exists");
    }
    catch (StemNotFoundException eSNF) {
      // Ignore.  This is what we want.
    }

    try {
      _initializeChildGroupsAndStems(this);
    }
    catch (HibernateException eH) {
      throw new StemAddException(eH.getMessage());
    }

    try {
      Stem child = new Stem(this.s);

      // Set naming attributes
      child.setStem_extension(extension);
      child.setDisplay_extension(displayExtension);
      child.setStem_name( 
        constructName(this.getName(), extension)
      );
      child.setDisplay_name( 
        constructName(this.getDisplayName(), displayExtension)
      );
      // Set parent
      child.setParent_stem(this);
      // Add to children 
      Set children  = this.getChild_stems();
      children.add(child);
      this.setChild_stems(children);
      // And save
      Set objects = new LinkedHashSet();
      objects.add(child);
      objects.add(this);
      HibernateHelper.save(objects);
      sw.stop();
      EL.stemAddChildStem(this.s, child.getName(), sw);
      _grantDefaultPrivsUponCreate(child);
      return child;
    }
    catch (HibernateException eH) {
      throw new StemAddException(
        "Unable to add stem " + this.getName() + ":" + extension + ": " 
        + eH.getMessage()
      );
    }
  } // public Stem addChildStem(extension, displayExtension)

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Stem)) {
      return false;
    }
    Stem otherStem = (Stem) other;
    return new EqualsBuilder()
      .append(this.getStem_id()     , otherStem.getStem_id()    )
      .append(this.getCreator_id()  , otherStem.getCreator_id() )
      .append(this.getStem_id()     , otherStem.getStem_id()    )
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
    GrouperSession.validate(this.s);
    Set children  = new LinkedHashSet();
    try {
      _initializeChildGroupsAndStems(this);
      Iterator iter = this.getChild_groups().iterator();
      // Perform check as root
      GrouperSession root = GrouperSessionFinder.getTransientRootSession();
      while (iter.hasNext()) {
        Group child = (Group) iter.next();
        child.setSession(root);
        try {
          PrivilegeResolver.getInstance().canVIEW(
            root, child, s.getSubject()
          );
          child.setSession(this.s);
          children.add(child);
        }
        catch (InsufficientPrivilegeException eIP) {
          // ignore
        }
      }
      root.stop();
    }
    catch (Exception e) {
      // @exception HibernateException
      // @exception SessionException
      LOG.error(e.getMessage());
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
    GrouperSession.validate(s);
    Set children = new LinkedHashSet();
    try {
      _initializeChildGroupsAndStems(this); 
      Iterator iter = this.getChild_stems().iterator();
      while (iter.hasNext()) {
        Stem child = (Stem) iter.next();
        child.setSession(this.s);
        children.add(child);
      }
    }
    catch (HibernateException eH) {
      GrouperLog.error(LOG, s, eH.getMessage());
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
      source = new String();
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
   */
  public Set getCreators() {
    try {
      return PrivilegeResolver.getInstance().getSubjectsWithPriv(
        this.s, this, NamingPrivilege.CREATE
      );
    }
    catch (SchemaException eS) {
      String err = ERR_FNF + NamingPrivilege.CREATE;
      LOG.fatal(err);
      throw new RuntimeException(err);
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
      desc = new String();
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
      source = new String();
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
    parent.setSession(this.s);
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
    return PrivilegeResolver.getInstance().getPrivs(
      this.s, this, subj
    );
  } // public Set getPrivs(subj)

  /**
   * Get {@link GrouperSession} associated with this {@link Stem}.
   * <pre class="eg">
   * GrouperSession s = ns.getSession();
   * </pre>
   * @return  {@GrouperSession} associated this this stem.
   * @throws  RuntimeException if session not valid.
   */
  public GrouperSession getSession() {
    GrouperSession.validate(this.s);
    return this.s;
  } // public GrouperSession getSession()

  /**
   * Get subjects with STEM privilege on this stem.
   * <pre class="eg">
   * Set stemmers = ns.getStemmers();
   * </pre>
   * @return  Set of {@link Subject} objects
   */
  public Set getStemmers() {
    try {
      return PrivilegeResolver.getInstance().getSubjectsWithPriv(
        this.s, this, NamingPrivilege.STEM
      );
    }
    catch (SchemaException eS) {
      String err = ERR_FNF + NamingPrivilege.STEM;
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
  } // public Set getStemmers()

  /**
   * Get stem UUID.
   * <pre class="eg">
   * // Get UUID
   * String uuid = ns.getUuid();
   * </pre>
   * @return  Stem UUID.
   */
  public String getUuid() {
    return this.getStem_id();
  }

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
    String msg = "grantPriv: " + SubjectHelper.getPretty(subj) 
      + " '" + priv.toString().toUpperCase() + "'";
    GrouperLog.debug(LOG, this.s, msg);
    try {
      PrivilegeResolver.getInstance().grantPriv(
        this.s, this, subj, priv
      );
      sw.stop();
      EL.stemGrantPriv(this.s, this.getName(), subj, priv, sw);
    }
    catch (GrantPrivilegeException eGP) {
      GrouperLog.debug(LOG, this.s, eGP.getMessage());
      throw new GrantPrivilegeException(eGP.getMessage());
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, this.s, eIP.getMessage());
      throw new InsufficientPrivilegeException(eIP.getMessage());
    }
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
    return PrivilegeResolver.getInstance().hasPriv(
      this.s, this, subj, NamingPrivilege.CREATE
    );
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
    return PrivilegeResolver.getInstance().hasPriv(
      this.s, this, subj, NamingPrivilege.STEM
    );
  } // public boolean hasStem(subj)
 
  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getStem_id()     )
      .append(this.getCreator_id()  )
      .append(this.getStem_id()     )
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
    PrivilegeResolver.getInstance().revokePriv(
      this.s, this, priv
    );
    sw.stop();
    EL.stemRevokePriv(this.s, this.getName(), priv, sw);
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
    PrivilegeResolver.getInstance().revokePriv(
      this.s, this, subj, priv
    );
    sw.stop();
    EL.stemRevokePriv(this.s, this.getName(), subj, priv, sw);
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
    PrivilegeResolver.getInstance().canSTEM(
      GrouperSessionFinder.getRootSession(), this, this.s.getSubject()
    );
    try {
      this.setStem_description(value);
      this.setModified();
      HibernateHelper.save(this);
    }
    catch (Exception e) {
      throw new StemModifyException(
        "unable to set description: " + e.getMessage()
      );
    }
    sw.stop();
    EL.stemSetAttr(this.s, this.getName(), "description", value, sw);
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
      validateName(value);
    }
    catch (IllegalArgumentException eIA) {
      if (!(this.getStem_name().equals(ROOT_INT) && value.equals(ROOT_EXT))) {
        throw new StemModifyException(eIA.getMessage());
      }
      // Appease Oracle
      value = ROOT_INT;
    }
    PrivilegeResolver.getInstance().canSTEM(
      GrouperSessionFinder.getRootSession(), this, this.s.getSubject()
    );
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
      GrouperSession orig = this.s;
      GrouperSession root = GrouperSessionFinder.getTransientRootSession();
      this.setSession(root);
      objects.addAll( this._renameChildren() );
      objects.add(this);
      HibernateHelper.save(objects);
      this.setSession(orig);
      root.stop();
    }
    catch (Exception e) {
      throw new StemModifyException(
        "unable to set displayExtension: " + e.getMessage()
      );
    }
    sw.stop();
    // Reset for logging purposes
    if (value.equals(ROOT_INT)) {
      value = ROOT_EXT;
    }
    EL.stemSetAttr(this.s, this.getName(), "displayExtension", value, sw);
  } // public void setDisplayExtension(value)

  public String toString() {
    return new ToStringBuilder(this)
      .append("displayName" , getDisplay_name() )
      .append("name"        , getName()         )
      .append("uuid"        , getStem_id()      )
      .append("creator"     , getCreator_id()   )
      .append("modifier"    , getModifier_id()  )
      .toString();
  } // public String toString()


  // Protected Class Methods

  protected static Stem addRootStem(GrouperSession s) {
    Stem root = new Stem(s);
    try {
      HibernateHelper.save(root);
    }
    catch (HibernateException eH) {
      String err = ERR_ARS + eH.getMessage();
      LOG.fatal(err);
      throw new RuntimeException(err);
    }
    return root;
  } // protected static Stem addRootStem(GrouperSession s)

  protected static String constructName(String stem, String extn) {
    // TODO This should probably end up in a "naming" utility class
    // TODO Why don't I do validation here?
    if (stem.equals(ROOT_EXT)) {
      return extn;
    }
    return stem + ":" + extn;
  } // protected static String constructName(stem, extn)

  protected void setModified() {
    this.setModifier_id( s.getMember()        );
    this.setModify_time( new Date().getTime() );
  } // protected void setModified()

  protected static List setSession(GrouperSession s, List l) {
    List      stems = new ArrayList();
    Iterator  iter  = l.iterator();
    while (iter.hasNext()) {
      Stem ns = (Stem) iter.next();
      ns.setSession(s);
      stems.add(ns);
    }
    return stems;
  } // protected static List setSession(s, l)

  protected static void validateName(String name)  
    throws  IllegalArgumentException
  {
    // TODO I should reuse the same patterns
    if (name == null) {
      throw new IllegalArgumentException("null name");
    }
    Matcher m = RE_COLON.matcher(name);
    if (m.find()) {
      throw new IllegalArgumentException("name contains colon");
    }
    m = RE_WS.matcher(name);
    if (m.find()) {
      throw new IllegalArgumentException("empty name");
    }
  } // protected static void validateName(name)
    

  // Protected Instance Methods
  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
  } // protected void setSession(s)


  // Private Class Methods

  // The child_groups and child_stems collections have lazy
  // associations so we need to manually initialize as needed.
  private static void _initializeChildGroupsAndStems(Stem ns) 
    throws  HibernateException
  {
    Session hs = HibernateHelper.getSession();
    hs.load(ns, ns.getId());
    Hibernate.initialize( ns.getChild_stems() );
    Hibernate.initialize( ns.getChild_groups() );
    hs.close();
  } // private static void _initializeChildGroupsAndStems(ns) 

  // Private Instance Methods
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
      GrouperSession  root  = GrouperSessionFinder.getTransientRootSession();
      g.setSession(root);
      PrivilegeResolver.getInstance().grantPriv(
        root, g, orig.getSubject(), AccessPrivilege.ADMIN
      );

      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate(
        orig, root, g, AccessPrivilege.ADMIN  , CFG_GCGAA
      );
      this._grantOptionalPrivUponCreate(
        orig, root, g, AccessPrivilege.OPTIN  , CFG_GCGAOI
      );
      this._grantOptionalPrivUponCreate(
        orig, root, g, AccessPrivilege.OPTOUT , CFG_GCGAOO
      );
      this._grantOptionalPrivUponCreate(
        orig, root, g, AccessPrivilege.READ   , CFG_GCGAR
      );
      this._grantOptionalPrivUponCreate(
        orig, root, g, AccessPrivilege.UPDATE , CFG_GCGAU
      );
      this._grantOptionalPrivUponCreate(
        orig, root, g, AccessPrivilege.VIEW   , CFG_GCGAV
      );

      g.setSession(orig);
      root.stop();
    }
    catch (Exception e) {
      throw new GroupAddException(
        "group created but unable to grant ADMIN to creator: " + e.getMessage()
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
      GrouperSession  root  = GrouperSessionFinder.getTransientRootSession();
      ns.setSession(root);
      PrivilegeResolver.getInstance().grantPriv(
        root, ns, orig.getSubject(), NamingPrivilege.STEM
      );

      // Now optionally grant other privs
      this._grantOptionalPrivUponCreate(
        orig, root, ns, NamingPrivilege.CREATE, CFG_SCGAC
      );
      this._grantOptionalPrivUponCreate(
        orig, root, ns, NamingPrivilege.STEM  , CFG_SCGAS
      );

      ns.setSession(orig);
      root.stop();
    }
    catch (Exception e) {
      throw new StemAddException(
        "stem created but unable to grant STEM to creator: " + e.getMessage()
      );
    }
  } // private void _grantDefaultPrivsUponCreate(ns)

  private void _grantOptionalPrivUponCreate(
    GrouperSession orig, GrouperSession root, Object o, Privilege p, String opt
  ) 
    throws  Exception
  {
    GrouperConfig cfg = GrouperConfig.getInstance();
    Subject       all = SubjectFinder.findAllSubject();
    if (cfg.getProperty(opt).equals(BT)) {
      StopWatch sw = new StopWatch();
      sw.start();
      String msg = " granted " + p.getName() + " to " + SubjectHelper.getPretty(all);
      if      (o.getClass().equals(Group.class)) {
        Group g = (Group) o;
        PrivilegeResolver.getInstance().grantPriv(root, g, all, p);
        sw.stop();
        EL.groupGrantPriv(this.s, g.getName(), all, p, sw);
      }
      else if (o.getClass().equals(Stem.class)) {
        Stem ns = (Stem) o;
        PrivilegeResolver.getInstance().grantPriv(root, ns, all, p);
        sw.stop();
        EL.stemGrantPriv(this.s, ns.getName(), all, p, sw);
      }
    }
  } // private void _grantOptionalPrivUponCreate(orig, root, o, p, opt)

  private Set _renameChildGroups() 
    throws  HibernateException
  {
    Set objects = new LinkedHashSet();
    Iterator iter = this.getChild_groups().iterator();
    while (iter.hasNext()) {
      Group child = (Group) iter.next();
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
      String err = eH.getMessage();
      GrouperLog.error(LOG, this.s, err); 
      throw new StemModifyException(err);
    }
    return objects;
  } // private Set _renameChildren()

  private Set _renameChildStemsAndGroups() 
    throws  HibernateException
  {
    Set objects = new LinkedHashSet();
    Iterator iter = this.getChild_stems().iterator();
    while (iter.hasNext()) {
      Stem child = (Stem) iter.next();    
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

  private void _setCreated() {
    this.setCreator_id( s.getMember()         );
    this.setCreate_time( new Date().getTime() );
  } // private void _setCreated()


  // Hibernate Accessors
  private String getId() {
    return this.id;
  }

  private void setId(String id) {
    this.id = id;
  }

  private String getCreate_source() {
    return this.create_source;
  }

  private void setCreate_source(String create_source) {
    this.create_source = create_source;
  }

  private long getCreate_time() {
    return this.create_time;
  }

  private void setCreate_time(long create_time) {
    this.create_time = create_time;
  }

  private String getStem_description() {
    return this.stem_description;
  }

  private void setStem_description(String stem_description) {
    this.stem_description = stem_description;
  }

  private String getDisplay_extension() {
    return this.display_extension;
  }

  private void setDisplay_extension(String display_extension) {
    this.display_extension = display_extension;
  }

  private String getDisplay_name() {
    return this.display_name;
  }

  private void setDisplay_name(String display_name) {
    this.display_name = display_name;
  }

  private String getStem_extension() {
    return this.stem_extension;
  }

  private void setStem_extension(String stem_extension) {
    this.stem_extension = stem_extension;
  }

  private String getModify_source() {
    return this.modify_source;
  }

  // used by RegistryReset
  protected void setModify_source(String modify_source) {
    this.modify_source = modify_source;
  }

  private long getModify_time() {
    return this.modify_time;
  }

  // used by RegistryReset
  protected void setModify_time(long modify_time) {
    this.modify_time = modify_time;
  }

  private String getStem_name() {
    return this.stem_name;
  }

  private void setStem_name(String stem_name) {
    this.stem_name = stem_name;
  }

  private String getStem_id() {
    return this.stem_id;
  }
  
  private void setStem_id(String stem_id) {
    this.stem_id = stem_id;
  }

  private Member getCreator_id() {
    return this.creator_id;
  }

  private void setCreator_id(Member creator_id) {
    this.creator_id = creator_id;
  }

  private Member getModifier_id() {
    return this.modifier_id;
  }

  // used by RegistryReset
  protected void setModifier_id(Member modifier_id) {
      this.modifier_id = modifier_id;
  }

  private Stem getParent_stem() {
    return this.parent_stem;
  }

  private void setParent_stem(Stem parent_stem) {
    this.parent_stem = parent_stem;
  }

  private Set getChild_groups() {
    return this.child_groups;
  }

  private void setChild_groups(Set child_groups) {
    this.child_groups = child_groups;
  }

  private Set getChild_stems() {
    return this.child_stems;
  }

  private void setChild_stems(Set child_stems) {
    this.child_stems = child_stems;
  }

  private int getVersion() {
    return this.version;
  }

  private void setVersion(int version) {
    this.version = version;
  }

  private Status getStatus() {
    return this.status;
  }
  private void setStatus(Status s) {
    this.status = s;
  }

}

