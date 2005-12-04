/*
  Copyright 2004-2005 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2005 The University Of Chicago

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
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;


/** 
 * A namespace within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Stem.java,v 1.26 2005-12-04 22:52:49 blair Exp $
 *     
*/
public class Stem implements Serializable {

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
  private String  stem_description;
  private String  stem_extension;
  private String  stem_id;
  private String  stem_name;


  // Private Transient Instance Variables
  private transient GrouperSession s;


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
    this.setStem_name(        ""                    );
    this.setDisplay_name(     ""                    );
    this.setStem_extension(   ""                    );
    this.setDisplay_extension(""                    );
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
    PrivilegeResolver.getInstance().canCREATE(
      GrouperSessionFinder.getRootSession(), this, this.s.getSubject()
    );
    if (this.equals(StemFinder.findRootStem(this.s))) {
      throw new GroupAddException(
        "cannot create groups at root stem level"
      );
    } 
    try {
      Group g = GroupFinder.findByName(
        this.constructName(this.getName(), extension)
      );
      throw new GroupAddException("group already exists");
    }
    catch (GroupNotFoundException eGNF) {
      // Ignore.  This is what we want.
    }

    try {
      Group child = new Group(this.s, this, extension, displayExtension);
      // Set parent
      child.setParent_stem(this);
      // Add to children 
      Set children  = this.getChild_groups();
      children.add(child);
      this.setChild_groups(children);
      HibernateHelper.save(this);
      try {
        // Now grant ADMIN (as root) to the creator of the child group.
        //
        // Ideally this would be wrapped up in the broader transaction
        // of adding the child stem but as the interfaces may be
        // outside of our control, I don't think we can do that.  
        child.setSession(GrouperSessionFinder.getRootSession());
        PrivilegeResolver.getInstance().grantPriv(
          GrouperSessionFinder.getRootSession(), child, 
          s.getSubject(), AccessPrivilege.ADMIN
        );
        child.setSession(this.s);
      }
      catch (Exception e) {
        throw new GroupAddException(
          "group created but unable to grant ADMIN to creator: " + e.getMessage()
        );
      }


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
    PrivilegeResolver.getInstance().canSTEM(
      GrouperSessionFinder.getRootSession(), this, this.s.getSubject()
    );
    try {
      Stem ns = StemFinder.findByName(
        this.s, this.constructName(this.getName(), extension)
      );
      throw new StemAddException("stem already exists");
    }
    catch (StemNotFoundException eSNF) {
      // Ignore.  This is what we want.
    }

    Stem child = new Stem(this.s);

    // Set naming attributes
    child.setStem_extension(extension);
    child.setDisplay_extension(displayExtension);
    child.setStem_name( 
      this.constructName(this.getName(), extension)
    );
    child.setDisplay_name( 
      this.constructName(this.getDisplayName(), displayExtension)
    );
    // Set parent
    child.setParent_stem(this);
    // Add to children 
    Set children  = this.getChild_stems();
    children.add(child);
    this.setChild_stems(children);
    try {
      HibernateHelper.save(this);
      try {
        // Now grant STEM (as root) to the creator on the child stem.
        //
        // Ideally this would be wrapped up in the broader transaction
        // of adding the child stem but as the interfaces may be
        // outside of our control, I don't think we can do that.  
        PrivilegeResolver.getInstance().grantPriv(
          GrouperSessionFinder.getRootSession(), child, 
          s.getSubject(), NamingPrivilege.STEM
        );
      }
      catch (Exception e) {
        throw new StemAddException(
          "stem created but unable to grant STEM to creator: " + e.getMessage()
        );
      }
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
    if ( (this == other ) ) return true;
    if ( !(other instanceof Stem) ) return false;
    Stem castOther = (Stem) other;
    return new EqualsBuilder()
           .append(this.getStem_id(), castOther.getStem_id())
           .append(this.getCreator_id(), castOther.getCreator_id())
           .append(this.getModifier_id(), castOther.getModifier_id())
           .isEquals();
  }

  /**
   * Get child groups of this stem.
   * <pre class="eg">
   * // Get child groups 
   * Set childGroups = ns.getChildGroups();
   * </pre>
   * @return  Set of {@link Group} objects
   */
  public Set getChildGroups() {
    // TODO Filter through canVIEW()
    Set       children  = new LinkedHashSet();
    Iterator  iter      = this.getChild_groups().iterator();
    while (iter.hasNext()) {
      Group child = (Group) iter.next();
      child.setSession(this.s);
      children.add(child);
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
    Set       children  = new LinkedHashSet();
    Iterator  iter      = this.getChild_stems().iterator();
    while (iter.hasNext()) {
      Stem child = (Stem) iter.next();
      child.setSession(this.s);
      children.add(child);
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
    throw new RuntimeException("Not implemented");
  }
  
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
    throws SubjectNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }
  
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
    return PrivilegeResolver.getInstance().getSubjectsWithPriv(
      this.s, this, NamingPrivilege.CREATE
    );
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
    return this.getDisplay_extension();
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
    return this.getDisplay_name();
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
    return this.getStem_extension();
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
    throw new RuntimeException("Not implemented");
  }
  
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
    throws SubjectNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }
  
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
    return this.getStem_name();
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
   * Get subjects with STEM privilege on this stem.
   * <pre class="eg">
   * Set stemmers = ns.getStemmers();
   * </pre>
   * @return  Set of {@link Subject} objects
   */
  public Set getStemmers() {
    return PrivilegeResolver.getInstance().getSubjectsWithPriv(
      this.s, this, NamingPrivilege.STEM
    );
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
   */
  public void grantPriv(Subject subj, Privilege priv)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException
  {
    PrivilegeResolver.getInstance().grantPriv(
      this.s, this, subj, priv
    );
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
           .append(getUuid()        )
           .append(getCreator_id()  )
           .append(getModifier_id() )
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
   */
  public void revokePriv(Privilege priv) 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException
  {
    PrivilegeResolver.getInstance().revokePriv(
      this.s, this, priv
    );
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
   */
  public void revokePriv(Subject subj, Privilege priv)
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException
  {
    PrivilegeResolver.getInstance().revokePriv(
      this.s, this, subj, priv
    );
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
/* TODO
    if (!this.hasStem(this.s.getSubject())) {
      throw new InsufficientPrivilegeException("does not have STEM");
    }
*/
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
/* TODO
    if (!this.hasStem(this.s.getSubject())) {
      throw new InsufficientPrivilegeException("does not have STEM");
    }
*/
    PrivilegeResolver.getInstance().canSTEM(
      GrouperSessionFinder.getRootSession(), this, this.s.getSubject()
    );
    try {
      Set objects = new HashSet();
      this.setDisplay_extension(value);
      this.setModified();
      try {
        this.setDisplay_name(
          this.constructName(
            this.getParentStem().getDisplayName(), value
          )
        );
        // TODO Iterate through child stems + groups. Ugh.
      }
      catch (StemNotFoundException eSNF) {
        // I guess we're the root stem
        this.setDisplay_name(value);
      }
      objects.add(this);
      HibernateHelper.save(objects);
    }
    catch (Exception e) {
      throw new StemModifyException(
        "unable to set description: " + e.getMessage()
      );
    }
  } // public void setDisplayExtension(value)

  public String toString() {
    return new ToStringBuilder(this)
           .append("display_name", getDisplay_name())
           .append("name", getName())
           .append("uuid", getStem_id())
           .append("creator_id", getCreator_id())
           .append("modifier_id", getModifier_id())
           .toString();
  }


  // Protected Class Methods

  protected static void addRootStem(GrouperSession s) {
    Stem root = new Stem(s);
    try {
      HibernateHelper.save(root);
    }
    catch (HibernateException eH) {
      throw new RuntimeException(
        "unable to add root stem: " + eH.getMessage()
      );
    }
  } // protected static void addRootStem(GrouperSession s)

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


  // Protected Instance Methods
  protected String constructName(String stem, String extn) {
    // TODO This should probably end up in a "naming" utility class
    if (stem.equals("")) {
      return extn;
    }
    return stem + ":" + extn;
  } // protected String constructName(stem, extn)

  protected void setModified() {
    this.setModifier_id( s.getMember()        );
    this.setModify_time( new Date().getTime() );
  } // protected void setModified()

  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
  } // protected void setSession(s)


  // Private Instance Methods
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

  private void setModify_source(String modify_source) {
    this.modify_source = modify_source;
  }

  private long getModify_time() {
    return this.modify_time;
  }

  private void setModify_time(long modify_time) {
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
/* TODO
  private Stem getStem() {
    return this.stem;
  }
  private void setStem(Stem stem) {
    this.stem = stem;
  }
*/
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

  private void setModifier_id(Member modifier_id) {
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

}

