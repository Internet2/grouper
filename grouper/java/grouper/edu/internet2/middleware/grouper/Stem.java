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
 * @version $Id: Stem.java,v 1.1.2.10 2005-11-03 18:19:54 blair Exp $
 *     
*/
public class Stem implements Serializable {

  // Hibernate Properties
  private String  id;
  private Set     child_groups        = new HashSet();
  private Set     child_stems         = new HashSet();
  private String  create_source;
  private Date    create_time;
  private Member  creator_id;
  private String  display_extension;
  private String  display_name;
  private Member  modifier_id;
  private String  modify_source;
  private Date    modify_time;
  private Stem    parent_stem;
  private String  stem_description;
  private String  stem_extension;
  private String  stem_id;
  private String  stem_name;
  private Integer version;


  // Transient Instance Variables
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
    this.setCreator_id( s.getMember() );
    this.setCreate_time( new java.util.Date() );
    this.setStem_id( GrouperUuid.getUuid() );
    this.setStem_name("");
    this.setDisplay_name("");
    this.setStem_extension("");
    this.setDisplay_extension("");
  } // protected Stem(s)
  

  // Public Instance Methods

  /**
   * Add a new group to the registry.
   * <pre class="eg">
   * // Add a group with the extension "edu" beneath this stem.
   * try {
   *   Group edu = ns.addChildGroup("edu", "edu domain");
   * }
   * catch (GroupAddException e) {
   *   // Group not added
   * }
   * </pre>
   * @param   extension         Group's extension
   * @param   displayExtension  Groups' displayExtension
   * @return  The added {@link Group}
   * @throws  GroupAddException 
   */
  public Group addChildGroup(String extension, String displayExtension) 
    throws GroupAddException 
  {
    throw new RuntimeException("Not implemented");
  }

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
   * @throws  StemAddException 
   */
  public Stem addChildStem(String extension, String displayExtension) 
    throws StemAddException 
  {
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
    // TODO this.setChild_stems( this.getChild_stems().add(child) );
    try {
      // Save and cascade
      HibernateUtil.save(this);
      return child;
    }
    catch (HibernateException e) {
      throw new StemAddException(
        "Unable to add " + this.getName() + ":" + extension + ": " 
        + e.getMessage()
      );
    }
  }

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
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get child stems of this stem.
   * <pre class="eg">
   * // Get child stems 
   * Set childStems = ns.getChildStems();
   * </pre>
   * @return  Set of {@link Stem} objects
   */
  public Set getChildStems() {
    throw new RuntimeException("Not implemented");
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
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get subjects with CREATE privilege on this stem.
   * <pre class="eg">
   * Set creators = ns.getCreators();
   * </pre>
   * @return  Set of {@link Subject} objects
   */
  public Set getCreators() {
    throw new RuntimeException("Not implemented");
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
    throw new RuntimeException("Not implemented");
  }
 
  /**
   * Get stem displayExtension.
   * <pre class="eg">
   * // Get displayExtension
   * String displayExtn = ns.getDisplayExtension();
   * </pre>
   * @return  Stem displayExtension.
   */
  public String getDisplayExtension() {
    throw new RuntimeException("Not implemented");
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
    throw new RuntimeException("Not implemented");
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
    throw new RuntimeException("Not implemented");
  }

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
  public Stem getParentStem() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get privileges that the specified member has on this stem.
   * <pre class="eg">
   * Set privileges = ns.getPrivileges(member);
   * </pre>
   * @param   m   Get privileges for this member.
   * @return  Set of privileges.
   */
  public Set getPrivileges(Member m) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get subjects with STEM privilege on this stem.
   * <pre class="eg">
   * Set stemmers = ns.getStemmers();
   * </pre>
   * @return  Set of {@link Subject} objects
   */
  public Set getStemmers() {
    throw new RuntimeException("Not implemented");
  }

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
   * // Grant CREATE to the specified member
   * try {
   *   ns.grantPriv(m, Privilege.CREATE);
   * }
   * catch (GrantPrivilegeException e) {
   *   // Error granting privilege
   * }
   * </pre>
   * @param   m     Grant privilege to this member.
   * @param   priv  Grant this privilege.
   * @throws  GrantPrivilegeException
   */
  public void grantPriv(Member m, String priv) 
    throws GrantPrivilegeException
  {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Check whether a member has the CREATE privilege on this stem.
   * <pre class="eg">
   * if (ns.hasCreate(m)) {
   *   // Has CREATE
   * }
   *   // Does not have CREATE
   * } 
   * </pre>
   * @param   m   Check whether this member has CREATE.
   * @return  Boolean true if the member has CREATE.
   */
  public boolean hasCreate(Member m) {
    throw new RuntimeException("Not implemented");
  }
 
  /**
   * Check whether a member has the STEM privilege on this stem.
   * <pre class="eg">
   * if (ns.hasStem(m)) {
   *   // Has STEM
   * }
   *   // Does not have STEM
   * } 
   * </pre>
   * @param   m   Check whether this member has STEM.
   * @return  Boolean true if the member has STEM.
   */
  public boolean hasStem(Member m) {
    throw new RuntimeException("Not implemented");
  }
 
  public int hashCode() {
    return new HashCodeBuilder()
           .append(getUuid())
           .append(getCreator_id())
           .append(getModifier_id())
           .toHashCode();
  }

  /**
   * Revoke all privileges of the specified type on this stem.
   * <pre class="eg">
   * // Revoke CREATE from everyone on this stem.
   * try {
   *   ns.revokePriv(Privilege.CREATE);
   * }
   * catch (RevokePrivilegeException e) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   priv  Revoke this privilege.
   * @throws  RevokePrivilegeException
   */
  public void revokePriv(String priv) 
    throws RevokePrivilegeException
  {
    throw new RuntimeException("Not implemented");
  }
 
  /**
   * Revoke a privilege on this stem.
   * <pre class="eg">
   * // Revoke CREATE from the specified member
   * try {
   *   ns.revokePriv(m, Privilege.CREATE);
   * }
   * catch (RevokePrivilegeException e) {
   *   // Error revoking privilege
   * }
   * </pre>
   * @param   m     Revoke privilege from this member.
   * @param   priv  Revoke this privilege.
   * @throws  RevokePrivilegeException
   */
  public void revokePriv(Member m, String priv) 
    throws RevokePrivilegeException
  {
    throw new RuntimeException("Not implemented");
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
    throws InsufficientPrivilegeException, StemModifyException
  {
    throw new RuntimeException("Not implemented");
  }

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
    throws InsufficientPrivilegeException, StemModifyException
  {
    throw new RuntimeException("Not implemented");
  }

  public String toString() {
    return new ToStringBuilder(this)
           .append("display_name", getDisplay_name())
           .append("name", getName())
           .append("uuid", getStem_id())
           .append("creator_id", getCreator_id())
           .append("modifier_id", getModifier_id())
           .toString();
  }


  // Private Instance Methods

  private String constructName(String stem, String extn) {
    // TODO This should probably end up in a "naming" utility class
    if (stem.equals("")) {
      return extn;
    }
    return stem + ":" + extn;
  } // private String constructName(stem, extn)


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

  private Date getCreate_time() {
    return this.create_time;
  }

  private void setCreate_time(Date create_time) {
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

  private Date getModify_time() {
    return this.modify_time;
  }

  private void setModify_time(Date modify_time) {
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

  private Integer getVersion() {
    return this.version;
  }

  private void setVersion(Integer version) {
    this.version = version;
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

