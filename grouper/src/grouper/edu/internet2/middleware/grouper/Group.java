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
 * A group within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Group.java,v 1.1.2.20 2005-11-07 20:39:13 blair Exp $
 */
public class Group implements Serializable {

  // Hibernate Properties
  private String  create_source;
  private Date    create_time;
  private Member  creator_id;
  private String  display_extension;
  private String  display_name;
  private Set     group_attributes;
  private String  group_description;
  private String  group_extension;
  private String  group_id;
  private Set     group_memberships;
  private String  group_name;
  private String  id;
  private Member  modifier_id;
  private String  modify_source;
  private Date    modify_time;
  // TODO private Stem    parent_stem;
  private String  parent_stem;


  // Public Static Constants
  public static final String LIST = "members";


  // Transient Instance Methods
  private transient GrouperSession s;


  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public Group() {
    // Nothing
  }

  // Return a group with an attached session
  protected Group(
    GrouperSession s, Stem ns, String extn, String displayExtn
  ) 
  {
    this.s = s;
    // Set create information
    this._setCreated();
    // Assign UUID
    this.setGroup_id( GrouperUuid.getUuid() );
    // Set naming information
    this.setGroup_name( ns.constructName(ns.getName(), extn) );
    this.setDisplay_name( ns.constructName(ns.getDisplayName(), displayExtn) );
    this.setGroup_extension(extn);
    this.setDisplay_extension(displayExtn);
  } // protected Group(s, ns, extn, displayExtn)
 
 
  // Public Instance Methods

  /**
   * Add a member to the group.
   * <pre class="eg">
   * try {
   *   g.addMember(m);
   * }
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to add members 
   * }
   * catch (MemberAddException e1) {
   *   // Unable to add member
   * } 
   * </pre>
   * @param   m   Add this {@link Member}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberAddException
   */
  public void addMember(Member m) 
    throws InsufficientPrivilegeException, MemberAddException
  {
    // TODO refactor into smaller components
    try {
      // Does the membership already exist?
      Membership ms = MembershipFinder.getImmediateMembership(
        this, m, Group.LIST
      );
      throw new MemberAddException(
        "membership already exists"
      );
    }
    catch (MembershipNotFoundException eMNF) {
      try {
        // Create the membership
        Membership ms = new Membership(this.s, this, m, Group.LIST);
        // Update group modify time
        this._setModified();
        // And then save group and membership
        Set objects = new HashSet();
        objects.add(ms);
        objects.add(this);

/*
        Set gMships = g.getAsMember().getMemberships();
        if (gMships.size() != ) {
          throw new RuntimeException("Not implemented!");
        }
*/
        // TODO g as member

        // Add m's members to g
/*
        if (m.getSubjectTypeId().equals("group")) {
          try {
            Group gm = m.toGroup();  
            Set gmMships = gm.getMemberships();
System.err.println("M: " + m);
System.err.println("AS G: " + gm);
System.err.println("HAS MEMBERS: " + gmMships.size());
          }
          catch (GroupNotFoundException eGNF) {
            throw new MemberAddException(
              "could not add member: " + eGNF.getMessage()
            );
          }
        }
*/

        HibernateHelper.save(objects);
      }
      catch (HibernateException eH) {
        throw new MemberAddException("could not add member: " + eH.getMessage());
      }
    }
  } // public void addMember(m)

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
    throws GroupDeleteException, InsufficientPrivilegeException
  {
    try {
      HibernateHelper.delete(this);
    }
    catch (HibernateException e) {
      throw new GroupDeleteException(
        "Unable to delete group: " + e.getMessage()
      );
    }
  }

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
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void deleteAttribute(String attr) 
    throws GroupModifyException, InsufficientPrivilegeException
  {
    throw new RuntimeException("Not implemented");
  }

  /** 
   * Delete a member from the group.
   * <pre class="eg">
   * try {
   *   g.deleteMember(m);
   * } 
   * catch (InsufficientPrivilegeException e0) {
   *   // Not privileged to delete this member
   * }
   * catch (MemberDeleteException e1) {
   *   // Unable to delete member
   * }
   * </pre>
   * @param   m   Delete this {@link Member}
   * @throws  InsufficientPrivilegeException
   * @throws  MemberDeleteException
   */
  public void deleteMember(Member m) 
    throws InsufficientPrivilegeException, MemberDeleteException
  {
    try {
      Membership ms = MembershipFinder.getImmediateMembership(this, m, Group.LIST);
      HibernateHelper.delete(ms);
    }
    catch (HibernateException e) {
      throw new MemberDeleteException(
        "unable to delete membership: " + e.getMessage()
      );
    }
    catch (MembershipNotFoundException e) {
      throw new MemberDeleteException("membership does not exist");
    }
  }

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Group)) {
      return false;
    }
    Group otherGroup = (Group) other;
    // TODO Include parent stem check
    return new EqualsBuilder()
           .append(this.getUuid(),        otherGroup.getUuid()      )
           .append(this.getExtension(),   otherGroup.getExtension() )
           .append(this.getName(),        otherGroup.getName()      )
           .append(this.getCreator_id(),  otherGroup.getCreator_id())
           .isEquals();
  } // public boolean equals(other)

  /**
   * Get subjects with the ADMIN privilege on this group.
   * <pre class="eg">
   * Set admins = g.getAdmins();
   * </pre>
   * @return  Set of subjects with ADMIN
   */
  public Set getAdmins() {
    throw new RuntimeException("Not implemented");
  }

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
    throws AttributeNotFoundException
  {
    throw new RuntimeException("Not implemented");
  }  

  /**
   * Get all attributes and values.
   * <pre class="eg">
   * Map attributes = g.getAttributes();
   * </pre>
   * @return  A map of attributes and values.
   */
  public Map getAttributes() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get (optional and questionable) create source for this group.
   * <pre class="eg">
   * // Get create source
`  * String source = g.getCreateSource();
   * </pre>
   * @return  Create source for this group.
   */
  public String getCreateSource() {
    throw new RuntimeException("Not implemented");
  }
  
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
    throw new RuntimeException("Not implemented");
  }
  
  /**
   * Get creation time for this group.
   * <pre class="eg">
   * // Get create time.
   * Date created = g.getCreateTime();
   * </pre>
   * @return  {@link Date} that this group was created.
   */
  public Date getCreateTime() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get group description.
   * <pre class="eg">
   * String description = g.getDescription();
   * </pre>
   * @return  Group description.
   */
  public String getDescription() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get group displayExtension.
   * <pre class="eg">
   * String displayExtn = g.getDisplayExtension();
   * </pre>
   * @return  Gruop displayExtension.
   */
  public String getDisplayExtension() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get group displayName.
   * <pre class="eg">
   * String displayName = g.getDisplayName();
   * </pre>
   * @return  Group displayName.
   */
  public String getDisplayName() {
    return this.getDisplay_name();
  }

  /**
   * Get effective members of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   */
  public Set getEffectiveMembers() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get effective memberships of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   */
  public Set getEffectiveMemberships() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get group extension.
   * <pre class="eg">
   * String extension = g.getExtension();
   * </pre>
   * @return  Group extension.
   */
  public String getExtension() {
    return this.getGroup_extension();
  }
 
  /**
   * Get immediate members of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   */
  public Set getImmediateMembers() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get immediate memberships of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   */
  public Set getImmediateMemberships() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   */
  public Set getMembers() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get memberships of this group.
   * <pre class="eg">
   * Set memberships = g.getMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   */
  public Set getMemberships() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get (optional and questionable) modify source for this group.
   * <pre class="eg">
   * // Get modify source
`  * String source = g.getModifySource();
   * </pre>
   * @return  Modify source for this group.
   */
  public String getModifySource() {
    throw new RuntimeException("Not implemented");
  }
  
  /**
   * Get subject that last modified this group.
   * <pre class="eg">
   * // Get last modifier of this group.
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
    throw new RuntimeException("Not implemented");
  }
  
  /**
   * Get last modified time for this group.
   * <pre class="eg">
   * // Get last modified time.
   * Date modified = g.getModifyTime();
   * </pre>
   * @return  {@link Date} that this group was last modified.
   */
  public Date getModifyTime() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get group name.
   * <pre class="eg">
   * String name = g.getName();
   * </pre>
   * @return  Group name.
   */
  public String getName() {
    return this.getGroup_name();
  }

  /**
   * Get subjects with the OPTIN privilege on this group.
   * <pre class="eg">
   * Set optins = g.getOptins();
   * </pre>
   * @return  Set of subjects with OPTIN
   */
  public Set getOptins() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get subjects with the OPTOUT privilege on this group.
   * <pre class="eg">
   * Set admins = g.getOptouts();
   * </pre>
   * @return  Set of subjects with OPTOUT
   */
  public Set getOptouts() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get parent stem.
   * <pre class="eg">
   * Stem parent = g.getParentStem();
   * </pre>
   * @return  Parent {@link Stem}.
   */
  public Stem getParentStem() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get privileges for the specified member on this group.
   * <pre class="eg">
   * Set privileges = g.getPrivileges(m);
   * </pre>
   * @param   m   Get privileges for this {@link Member}.
   * @return  Set of privileges.
   */
  public Set getPrivileges(Member m) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get subjects with the READ privilege on this group.
   * <pre class="eg">
   * Set readers = g.getReaders();
   * </pre>
   * @return  Set of subjects with READ
   */
  public Set getReaders() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get group types for this group.
   * <pre class="eg">
   * Set types = g.getTypes();
   * </pre>
   * @return  Set of group types.
   */
  public Set getTypes() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get subjects with the UPDATE privilege on this group.
   * <pre class="eg">
   * Set updaters = g.getUpdaters();
   * </pre>
   * @return  Set of subjects with UPDATE
   */
  public Set getUpdaters() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Get group UUID.
   * <pre class="eg">
   * String uuid = g.getUuid();
   * </pre>
   * @return  Group UUID.
   */
  public String getUuid() {
    return this.getGroup_id();
  }

  /**
   * Get subjects with the VIEW privilege on this group.
   * <pre class="eg">
   * Set viewers = g.getViewers();
   * </pre>
   * @return  Set of subjects with VIEW
   */
  public Set getViewers() {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Grant privilege to a member on this group.
   * <pre class="eg">
   * try {
   *   g.grantPriv(m, Privilege.ADMIN);
   * }
   * catch (GrantPrivilegeException e0) {
   *   // Not privileged to grant this privilege
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Unable to grant this privilege
   * }
   * </pre>
   * @param   m     Grant privilege to this member.
   * @param   priv  Grant this privilege.
   * @throws  GrantPrivilegeException
   * @throws  InsufficientPrivilegeException
   */
  public void grantPrivilege(Member m, String priv) 
    throws GrantPrivilegeException, InsufficientPrivilegeException
  {
    throw new RuntimeException("Not implemented");
  } 

  /**
   * Check whether the member has ADMIN on this group.
   * <pre class="eg">
   * if (g.hasAdmin(m)) {
   *   // m has ADMIN
   * }
   * else {
   *   // m does not have ADMIN
   * }
   * </pre>
   * @param   m   Check this member.
   * @return  Boolean true if member has ADMIN.
   */
  public boolean hasAdmin(Member m) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Check whether the member is an effective member of this group.
   * <pre class="eg">
   * if (g.hasEffectiveMember(m)) {
   *   // m is an effective member of this group
   * }
   * else {
   *   // m is not an effective member of this group
   * } 
   * </pre>
   * @param   m   Check this member.
   * @return  Boolean true if member belongs to this group.
   */
  public boolean hasEffectiveMember(Member m) {
    if (
      MembershipFinder.findEffectiveMemberships(
        this, m, Group.LIST
      ).size() > 0
    )
    {
      return true;
    }
    return false;
  } // public boolean hasEffectiveMember(m)

  /**
   * Check whether the member is an immediate member of this group.
   * <pre class="eg">
   * if (g.hasImmediateMember(m)) {
   *   // m is an immediate member of this group
   * }
   * else {
   *   // m is not a immediate member of this group
   * } 
   * </pre>
   * @param   m   Check this member.
   * @return  Boolean true if member belongs to this group.
   */
  public boolean hasImmediateMember(Member m) {
    try {
      Membership ms = MembershipFinder.getImmediateMembership(
        this, m, Group.LIST
      );
      return true;
    }
    catch (MembershipNotFoundException e) {
      return false;
    }
  } // public boolean hasImmediateMember(m)

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getUuid())
           .append(getCreator_id())
           .append(getModifier_id())
           .toHashCode();
  }

  /**
   * Check whether the member has OPTIN on this group.
   * <pre class="eg">
   * if (g.hasOptin(m)) {
   *   // m has OPTIN
   * }
   * else {
   *   // m does not have OPTIN
   * }
   * </pre>
   * @param   m   Check this member.
   * @return  Boolean true if member has OPTIN.
   */
  public boolean hasOptin(Member m) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Check whether the member has OPTOUT on this group.
   * <pre class="eg">
   * if (g.hasOptout(m)) {
   *   // m has OPTOUT
   * }
   * else {
   *   // m does not have OPTOUT
   * }
   * </pre>
   * @param   m   Check this member.
   * @return  Boolean true if member has OPTOUT.
   */
  public boolean hasOptout(Member m) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Check whether the member is a member of this group.
   * <pre class="eg">
   * if (g.hasMember(m)) {
   *   // m is a member of this group
   * }
   * else {
   *   // m is not a member of this group
   * } 
   * </pre>
   * @param   m   Check this member.
   * @return  Boolean true if member belongs to this group.
   */
  public boolean hasMember(Member m) {
    if (MembershipFinder.findMemberships(this, m, Group.LIST).size() > 0) {
      return true;
    }
    return false;
  }

  /**
   * Check whether the member has READ on this group.
   * <pre class="eg">
   * if (g.hasRead(m)) {
   *   // m has READ
   * }
   * else {
   *   // m does not have READ
   * }
   * </pre>
   * @param   m   Check this member.
   * @return  Boolean true if member has READ.
   */
  public boolean hasRead(Member m) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Check whether the member has UPDATE on this group.
   * <pre class="eg">
   * if (g.hasUpdate(m)) {
   *   // m has UPDATE
   * }
   * else {
   *   // m does not have UPDATE
   * }
   * </pre>
   * @param   m   Check this member.
   * @return  Boolean true if member has UPDATE.
   */
  public boolean hasUpdate(Member m) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Check whether the member has VIEW on this group.
   * <pre class="eg">
   * if (g.hasView(m)) {
   *   // m has VIEW
   * }
   * else {
   *   // m does not have VIEW
   * }
   * </pre>
   * @param   m   Check this member.
   * @return  Boolean true if member has VIEW.
   */
  public boolean hasView(Member m) {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Revoke all privileges of the specified type on this group.
   * <pre class="eg">
   * try {
   *   g.revokePriv(Privilege.OPTIN);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to revoke this privilege
   * }
   * </pre>
   * @param   priv  Revoke all instances of this privilege.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void revokePriv(String priv)
    throws GroupModifyException, InsufficientPrivilegeException
  {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Revoke a privilege from the specified member.
   * <pre class="eg">
   * try {
   *   g.revokePriv(m, Privilege.OPTIN);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to revoke this privilege
   * }
   * </pre>
   * @param   m     Revoke this member's privilege.
   * @param   priv  Revoke this privilege.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void revokePriv(Member m, String priv)
    throws GroupModifyException, InsufficientPrivilegeException
  {
    throw new RuntimeException("Not implemented");
  }

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
    throws AttributeNotFoundException, GroupModifyException, 
           InsufficientPrivilegeException
  {
    throw new RuntimeException("Not implemented");
  }

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
    throws GroupModifyException, InsufficientPrivilegeException
  {
    throw new RuntimeException("Not implemented");
  }
 
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
    throws GroupModifyException, InsufficientPrivilegeException
  {
    throw new RuntimeException("Not implemented");
  }

  /**
   * Set group type.
   * <pre class="eg">
   * try {
   *   g.setTypes(types);
   * }
   * catch (GroupModifyException e0) {
   *   // Unable to modify group
   * }
   * catch (InsufficientPrivilegeException e1) {
   *   // Not privileged to modify group type
   * }
   * </pre>
   * @param   types   Set of group types.
   * @throws  GroupModifyException
   * @throws  InsufficientPrivilegeException
   */
  public void setTypes(Set types) 
    throws GroupModifyException, InsufficientPrivilegeException
  {
    throw new RuntimeException("Not implemented");
  }
 
  /**
   * Convert this group to a {@link Member} object.
   * <p/>
   * <pre class="eg">
   * Member m = g.toMember();
   * </pre>
   * @return  {@link Group} as a {@link Member}
   */
  public Member toMember() {
    try {
      return MemberFinder.findBySubject(
        this.s,
        SubjectFinder.findById(
          this.getUuid(), "group"
        )
      );
    }
    catch (MemberNotFoundException eMNF) {
      throw new RuntimeException(
        "could not find group as member: " + eMNF.getMessage()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(
        "could not find group as subject: " + eSNF.getMessage()
      );
    }
  } // public Member toMember()

  public String toString() {
    return new ToStringBuilder(this)
           .append("display_name", getDisplay_name())
           .append("name", getName())
           .append("uuid", getUuid())
           .append("creator_id", getCreator_id())
           .append("modifier_id", getModifier_id())
           .toString();
  }


  // Protected Instance Methods
  protected void setSession(GrouperSession s) {
    this.s = s;
  } // protected void setSession(s)


  // Private Instance Methods
  
  private void _setCreated() {
    this.setCreator_id( s.getMember()         );
    this.setCreate_time( new java.util.Date() );
  } // private void _setCreated()

  private void _setModified() {
    this.setModifier_id( s.getMember()        );
    this.setModify_time( new java.util.Date() );
  } // private void _setModified()


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

  private String getGroup_description() {
    return this.group_description;
  }

  private void setGroup_description(String group_description) {
    this.group_description = group_description;
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

  private String getGroup_extension() {
    return this.group_extension;
  }

  private void setGroup_extension(String group_extension) {
    this.group_extension = group_extension;
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

  private String getGroup_name() {
    return this.group_name;
  }

  private void setGroup_name(String group_name) {
    this.group_name = group_name;
  }

  private String getGroup_id() {
    return this.group_id;
  }

  private void setGroup_id(String group_id) {
    this.group_id = group_id;
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

  // TODO private Stem getParent_stem() {
  private String getParent_stem() {
    return this.parent_stem;
  }

  // TODO protected void setParent_stem(Stem parent_stem) {
  protected void setParent_stem(String parent_stem) {
    // TODO I should just be able to use a _Stem_ object
    // TODO this.parent_stem = parent_stem.getId();
    this.parent_stem = parent_stem;
  }

  private Set getGroup_attributes() {
    return this.group_attributes;
  }

  private void setGroup_attributes(Set group_attributes) {
    this.group_attributes = group_attributes;
  }

  private Set getGroup_memberships() {
    return this.group_memberships;
  }

  private void setGroup_memberships(Set group_memberships) {
    this.group_memberships = group_memberships;
  }

}
