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
import  edu.internet2.middleware.subject.provider.*;
import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.logging.*;


/** 
 * A group within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: Group.java,v 1.26 2005-12-05 01:43:40 blair Exp $
 */
public class Group implements Serializable {

  // Private Class Constants
  private static final String ERR_AM  = "unable to add member: ";
  private static final String ERR_DM  = "unable to delete member: ";
  private static final String ERR_GA  = "attribute not found: ";
  private static final String ERR_S2G = "could not convert subject to group: ";
  private static final Log    LOG     = LogFactory.getLog(Group.class);


  // Hibernate Properties
  private String  create_source;
  private long    create_time;
  private Member  creator_id;
  private Set     group_attributes;
  private String  group_id;
  private Set     group_memberships;
  private String  id;
  private Member  modifier_id;
  private String  modify_source;
  private long    modify_time;
  private Stem    parent_stem;


  // Transient Instance Variables
  private transient Map             attrs     = new HashMap();
  private transient Subject         creator;
  private transient Subject         modifier;
  private transient GrouperSession  s;


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
    throws SchemaException
  {
    this.s = s;
    // Set create information
    this._setCreated();
    // Assign UUID
    this.setGroup_id( GrouperUuid.getUuid() );
    // Set naming information
    Set attributes = new LinkedHashSet();
    attributes.add( 
      new Attribute(
        this, FieldFinder.find("name"),
        ns.constructName(ns.getName(), extn)
      )
    );
    attributes.add( 
      new Attribute(
        this, FieldFinder.find("displayName"),
        ns.constructName(ns.getDisplayName(), displayExtn)
      )
    );
    attributes.add( 
      new Attribute(
        this, FieldFinder.find("extension"), extn
      )
    );
    attributes.add( 
      new Attribute(
        this, FieldFinder.find("displayExtension"), displayExtn
      )
    );
    this.setGroup_attributes(attributes);
  } // protected Group(s, ns, extn, displayExtn)
 

  // Public Class Methods
  /**
   * Retrieve default members {@link Field}.
   * <pre class="eg">
   * Field members = Group.getDefaultList();
   * </pre>
   * @return  
   */
  public static Field getDefaultList() {
    try {
      return FieldFinder.find("members");
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      throw new RuntimeException(eS.getMessage());
    }
  } // public static Field getDefaultList()

 
  // Public Instance Methods

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
      this.addMember(
        subj, getDefaultList()
      );
    }
    catch (SchemaException eS) {
      throw new MemberAddException(eS.getMessage());
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
    this._canWriteList(f, ERR_AM);
    try {
      // The objects that will need saving
      Set     objects = new LinkedHashSet();
      // Who we're adding
      Member  m       = this._canViewSubject(subj, ERR_AM);

      // Conditionally update group modify time.  Because the granting
      // of ADMIN to the creator takes place *after* the group has been
      // created, the modify* attributes would be set when using the 
      // _GrouperAccessAdapter_.  However, we don't want to consider
      // that a modification.  As such, if the modify time is equal to
      // the start of the epoch, don't set the group's modify* attrs.
      if (this.getModifyTime().equals(new Date())) {
        this.setModified();
      }
      objects.add(this);

      // Create the immediate membership
      objects.add( Membership.addMembership(this.s, this, m, f) ); 

      // Find effective memberships
      Set effs = MemberOf.doMemberOf(this.s, this, m);
      objects.addAll(effs);

      // And then save group and memberships
      HibernateHelper.save(objects);
      // TODO make INFO + (conditionally?) log each membership added
      GrouperLog.debug(
        LOG, this.s, 
        "added members to '"+ this.getName() + "'/'" + f.getName() 
        + "': " + SubjectHelper.getPretty(subj) + " and " + effs.size() 
        + " effs"
      );
    }
    catch (GroupNotFoundException eGNF) {
      throw new MemberAddException(ERR_AM + eGNF.getMessage());
    }
    catch (HibernateException eH) {
      throw new MemberAddException(ERR_AM + eH.getMessage());
    }
    catch (MemberNotFoundException eMNF) {
      throw new MemberAddException(ERR_AM + eMNF.getMessage());
    }
  } // public void addMember(subj, f)

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
    // TODO Revoke access privs
    // TODO Iterate through all lists, not just the default
    // TODO Could we run into priv issues?
    try {
      Set       deletes = new LinkedHashSet();
      deletes.add(this);

      // Deal with where this group is an immediate member
      Iterator  iterIs  = this.toMember().getImmediateGroups().iterator();
      while (iterIs.hasNext()) {
        Group g = (Group) iterIs.next();
        deletes.addAll( 
          this._membershipsToDelete(
            g, this.toSubject(), Group.getDefaultList()
          )
        );
      }
      // Deal with this group's immediate members
      Iterator  iterHas = this.getImmediateMembers().iterator();
      while (iterHas.hasNext()) {
        Member m = (Member) iterHas.next();
        deletes.addAll( 
          this._membershipsToDelete(
            this, m.getSubject(), Group.getDefaultList()
          )
        );
      }

      // And then commit changes to registry
      HibernateHelper.delete(deletes);
    }
    catch (Exception e) {
      throw new GroupDeleteException(
        "unable to delete group: " + e.getMessage()
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
      this.deleteMember(subj, Group.getDefaultList());
    }
    catch (SchemaException eS) {
      throw new MemberDeleteException(eS.getMessage());
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
    this._canWriteList(f, ERR_DM);
    try {
      // The objects that will need saving and deleting
      Set     deletes = new LinkedHashSet();
      Set     saves   = new LinkedHashSet();
      // Who we're adding
      Member  m       = this._canViewSubject(subj, ERR_DM);
      // Update group modify time
      this.setModified();
      saves.add(this);
      // Find memberships to delete
      deletes.addAll( this._membershipsToDelete(this, subj, f) );
      // And then commit changes to registry
      HibernateHelper.saveAndDelete(saves, deletes);
    }
    catch (GroupNotFoundException eGNF) {
      throw new MemberDeleteException(ERR_DM + eGNF.getMessage());
    }
    catch (HibernateException eH) {
      throw new MemberDeleteException(ERR_DM + eH.getMessage());
    }
    catch (MemberNotFoundException eMNF) {
      throw new MemberDeleteException(ERR_DM + eMNF.getMessage());
    }
  } // public void deleteMember(subj, f)

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
    return PrivilegeResolver.getInstance().getSubjectsWithPriv(
      this.s, this, AccessPrivilege.ADMIN
    );
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
    String val = null;
    try {
      Attribute a = (Attribute) this._getAttributes().get(attr);
      PrivilegeResolver.getInstance().canPrivDispatch(
        this.s, this, this.s.getSubject(), a.getField().getReadPriv()
      );
      val = a.getValue();
    }
    catch (Exception e) {
      // Ignore
    }
    if (val == null) {
      throw new AttributeNotFoundException(ERR_GA + attr);  
    }
    return val;
  } // public String getAttribute(attr)

  /**
   * Get all attributes and values.
   * <pre class="eg">
   * Map attributes = g.getAttributes();
   * </pre>
   * @return  A map of attributes and values.
   */
  public Map getAttributes() {
    // TODO Cache results
    Map       filtered  = new HashMap();
    Iterator  iter      = this._getAttributes().values().iterator();
    while (iter.hasNext()) {
      Attribute attr = (Attribute) iter.next();
      try {
        // TODO Does this actually work?
        PrivilegeResolver.getInstance().canPrivDispatch(
          this.s, this, this.s.getSubject(), attr.getField().getReadPriv()
        );
        filtered.put( attr.getField().getName(), attr.getValue() );
      }
      catch (Exception e) {
        // Nothing
      }
    }
    return filtered;
  } // public Map getAttributes()

  /**
   * Get (optional and questionable) create source for this group.
   * <pre class="eg">
   * // Get create source
`  * String source = g.getCreateSource();
   * </pre>
   * @return  Create source for this group.
   */
  public String getCreateSource() {
    String source = this.getCreate_source();
    if (source == null) {
      source = new String();
    }
    return source;
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
    if (creator == null) {
      creator = this.getCreator_id().getSubject();
    }
    return creator; 
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
    return new Date(this.getCreate_time());
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
      return (String) this.getAttribute("description");
    }
    catch (AttributeNotFoundException eANF) {
      // Lack of a description is acceptable
      return new String();
    }
  } // public String getDescription()

  /**
   * Get group displayExtension.
   * <pre class="eg">
   * String displayExtn = g.getDisplayExtension();
   * </pre>
   * @return  Gruop displayExtension.
   */
  public String getDisplayExtension() {
    try {
      return (String) this.getAttribute("displayExtension");
    }
    catch (AttributeNotFoundException eANF) {
      throw new RuntimeException("group without displayExtension");
    }
  } // public String getDisplayExtension()

  /**
   * Get group displayName.
   * <pre class="eg">
   * String displayName = g.getDisplayName();
   * </pre>
   * @return  Group displayName.
   */
  public String getDisplayName() {
    try {
      return (String) this.getAttribute("displayName");
    }
    catch (AttributeNotFoundException eANF) {
      throw new RuntimeException("group without displayName");
    }
  } // public String getDisplayName()

  /**
   * Get effective members of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   */
  public Set getEffectiveMembers() {
    return MembershipFinder.findEffectiveMembers(
      this.s, this, getDefaultList()
    );
  }  // public Set getEffectiveMembers()

  /**
   * Get effective memberships of this group.
   * <pre class="eg">
   * Set effectives = g.getEffectiveMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   */
  public Set getEffectiveMemberships() {
    return MembershipFinder.findEffectiveMemberships(
      this.s, this, getDefaultList()
    );
  } // public Set getEffectiveMembership()

  /**
   * Get group extension.
   * <pre class="eg">
   * String extension = g.getExtension();
   * </pre>
   * @return  Group extension.
   */
  public String getExtension() {
    try {
      return (String) this.getAttribute("extension");
    }
    catch (AttributeNotFoundException eANF) {
      throw new RuntimeException("group without extension");
    }
  } // public String getExtension()
 
  /**
   * Get immediate members of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   */
  public Set getImmediateMembers() {
    return MembershipFinder.findImmediateMembers(
      this.s, this, getDefaultList()
    );
  } // public Set getImmediateMembers()

  /**
   * Get immediate memberships of this group.
   * <pre class="eg">
   * Set immediates = g.getImmediateMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   */
  public Set getImmediateMemberships() {
    return MembershipFinder.findImmediateMemberships(
      this.s, this, getDefaultList()
    );
  } // public Set getImmediateMemberships()

  /**
   * Get members of this group.
   * <pre class="eg">
   * Set members = g.getMembers();
   * </pre>
   * @return  A set of {@link Member} objects.
   */
  public Set getMembers() {
    return MembershipFinder.findMembers(
      this.s, this, getDefaultList()
    );
  } // public Set getMembers()

  /**
   * Get memberships of this group.
   * <pre class="eg">
   * Set memberships = g.getMemberships();
   * </pre>
   * @return  A set of {@link Membership} objects.
   */
  public Set getMemberships() {
    return MembershipFinder.findMemberships(
      this.s, this, getDefaultList()
    );
  } // public Set getMemberships()

  /**
   * Get (optional and questionable) modify source for this group.
   * <pre class="eg">
   * // Get modify source
`  * String source = g.getModifySource();
   * </pre>
   * @return  Modify source for this group.
   */
  public String getModifySource() {
    String source = this.getModify_source();
    if (source == null) {
      source = new String();
    }
    return source;
  } // public String getModifySource()
  
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
    if (modifier == null) {
      Member m = this.getModifier_id();
      if (m == null) {
        throw new SubjectNotFoundException(
          "group has not been modified"
        );
      }
      modifier = m.getSubject();
    }
    return modifier; 
  } // public Subject getModifySubject()
  
  /**
   * Get last modified time for this group.
   * <pre class="eg">
   * // Get last modified time.
   * Date modified = g.getModifyTime();
   * </pre>
   * @return  {@link Date} that this group was last modified.
   */
  public Date getModifyTime() {
    return new Date(this.getModify_time());
  }

  /**
   * Get group name.
   * <pre class="eg">
   * String name = g.getName();
   * </pre>
   * @return  Group name.
   */
  public String getName() {
    try {
      return (String) this.getAttribute("name");
    }
    catch (AttributeNotFoundException eANF) {
      throw new RuntimeException("group without name");
    }
  } // public String getName()

  /**
   * Get subjects with the OPTIN privilege on this group.
   * <pre class="eg">
   * Set optins = g.getOptins();
   * </pre>
   * @return  Set of subjects with OPTIN
   */
  public Set getOptins() {
    return PrivilegeResolver.getInstance().getSubjectsWithPriv(
      this.s, this, AccessPrivilege.OPTIN
    );
  } // public Set getOptins()

  /**
   * Get subjects with the OPTOUT privilege on this group.
   * <pre class="eg">
   * Set admins = g.getOptouts();
   * </pre>
   * @return  Set of subjects with OPTOUT
   */
  public Set getOptouts() {
    return PrivilegeResolver.getInstance().getSubjectsWithPriv(
      this.s, this, AccessPrivilege.OPTOUT
    );
  } // public Set getOptouts()

  /**
   * Get parent stem.
   * <pre class="eg">
   * Stem parent = g.getParentStem();
   * </pre>
   * @return  Parent {@link Stem}.
   */
  public Stem getParentStem() {
    Stem parent = this.getParent_stem();
    parent.setSession(this.s);
    return parent;
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
    return PrivilegeResolver.getInstance().getPrivs(
      this.s, this, subj
    );
  } // public Set getPrivs(subj)


  /**
   * Get subjects with the READ privilege on this group.
   * <pre class="eg">
   * Set readers = g.getReaders();
   * </pre>
   * @return  Set of subjects with READ
   */
  public Set getReaders() {
    return PrivilegeResolver.getInstance().getSubjectsWithPriv(
      this.s, this, AccessPrivilege.READ
    );
  } // public Set getReaders()

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
    return PrivilegeResolver.getInstance().getSubjectsWithPriv(
      this.s, this, AccessPrivilege.UPDATE
    );
  } // public set getUpdateres()

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
    return PrivilegeResolver.getInstance().getSubjectsWithPriv(
      this.s, this, AccessPrivilege.VIEW
    );
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
   */
  public void grantPriv(Subject subj, Privilege priv)
    throws  GrantPrivilegeException,
            InsufficientPrivilegeException
  {
    PrivilegeResolver.getInstance().grantPriv(
      this.s, this, subj, priv
    );
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
    return PrivilegeResolver.getInstance().hasPriv(
      this.s, this, subj, AccessPrivilege.ADMIN
    );
  } // public boolean hasAdmin(subj)

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
   */
  public boolean hasEffectiveMember(Subject subj) {
    try {
      Member m = MemberFinder.findBySubject(this.s, subj);
      return m.isEffectiveMember(this);
    }
    catch (MemberNotFoundException eMNF) {
      // TODO Is silence the proper response?
    }
    return false;
  } // public boolean hasEffectiveMember(subj)

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
   */
  public boolean hasImmediateMember(Subject subj) {
    try {
      Member m = MemberFinder.findBySubject(this.s, subj);
      return m.isImmediateMember(this);
    }
    catch (MemberNotFoundException eMNF) {
      // TODO Is silence the proper response?
    }
    return false;
  } // public boolean hasImmediateMember(subj)

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getUuid())
           .append(getCreator_id())
           .append(getModifier_id())
           .toHashCode();
  }

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
    return PrivilegeResolver.getInstance().hasPriv(
      this.s, this, subj, AccessPrivilege.OPTIN
    );
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
    return PrivilegeResolver.getInstance().hasPriv(
      this.s, this, subj, AccessPrivilege.OPTOUT
    );
  } // public boolean hasOptout(subj)

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
   */
  public boolean hasMember(Subject subj) {
    return this.hasMember(subj, Group.getDefaultList());
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
   */
  public boolean hasMember(Subject subj, Field f) {
    // TODO I should probably have _Member_ call _Group_ and not the inverse
    boolean rv = false;
    try {
      Member m = MemberFinder.findBySubject(this.s, subj);
      rv = m.isMember(this, f);
    }
    catch (MemberNotFoundException eMNF) {
      // TODO Fail silently?
    }
    GrouperLog.debug(LOG, this.s, "hasMember '" + subj.getId() + "': " + rv);
    return rv;
  } // public boolean hasMember(subj, f)

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
    return PrivilegeResolver.getInstance().hasPriv(
      this.s, this, subj, AccessPrivilege.READ
    );
  } // public boolean hasRead(subj)

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
    return PrivilegeResolver.getInstance().hasPriv(
      this.s, this, subj, AccessPrivilege.UPDATE
    );
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
    return PrivilegeResolver.getInstance().hasPriv(
      this.s, this, subj, AccessPrivilege.VIEW
    );
  } // public boolean hasView(subj)

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
    // TODO s,AttributeNotFoundException,SchemaException,?
    throw new RuntimeException("Group.setAttribute(attr,value) not implemented");
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
      this.setAttribute("description", value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set description: " + eANF.getMessage()
      );
    }
  } // public void setDescription(value)
 
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
    try {
      this.setAttribute("displayExtension", value);
    }
    catch (AttributeNotFoundException eANF) {
      throw new GroupModifyException(
        "unable to set displayExtension: " + eANF.getMessage()
      );
    }
  } // public void setDisplayExtension(value)

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
  // TODO Does this need to be public?
    try {
      return MemberFinder.findBySubject(
        this.s, this.toSubject()
      );
    }
    catch (MemberNotFoundException eMNF) {
      throw new RuntimeException(
        "could not find group as member: " + eMNF.getMessage()
      );
    }
  } // public Member toMember()

  /**
   * Convert this group to a {@link Subject} object.
   * <p/>
   * <pre class="eg">
   * Subject subj = g.toSubject();
   * </pre>
   * @return  {@link Group} as a {@link Subject}
   */
  public Subject toSubject() {
    try {
      return SubjectFinder.findById(
        this.getUuid(), "group"
      );
    }
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(
        "could not find group as subject: " + eSNF.getMessage()
      );
    }
  } // public Subject toSubject()

  public String toString() {
    return new ToStringBuilder(this)
           .append("name"         , this.getName()        )
           .append("displayName"  , this.getDisplayName() )
           .append("uuid"         , this.getUuid()        )
           .append("creator_id"   , getCreator_id()       )
           .append("modifier_id"  , getModifier_id()      )
           .toString();
  } // public String toString()


  // Protected Class Methods
  protected static List setSession(GrouperSession s, List l) {
    List      groups  = new ArrayList();
    Iterator  iter    = l.iterator();
    while (iter.hasNext()) {
      Group g = (Group) iter.next();
      g.setSession(s);
      groups.add(g);
    }
    return groups;
  } // protected static List setSession(s, l)


  // Protected Instance Methods
  protected void setModified() {
    this.setModifier_id( s.getMember()        );
    this.setModify_time( new Date().getTime() );
  } // private void setModified()

  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
  } // protected void setSession(s)


  // Private Instance Methods

  private Member _canViewSubject(Subject subj, String msg) 
    throws  GroupNotFoundException,
            InsufficientPrivilegeException,
            MemberNotFoundException
  {
    Member  m = MemberFinder.findBySubject(s, subj);
    // If the subject being added is a group, verify that we can VIEW it
    if (m.getSubjectType().equals(SubjectTypeEnum.valueOf("group"))) {
      Subject who         = this.s.getSubject();
      Group   what        = m.toGroup();
      String  debug_msg   = "'" + who.getName() + "'/'" + who.getType().getName() 
        + "' can VIEW '" + what.getName() + "': ";
      try {
        PrivilegeResolver.getInstance().canVIEW(s, what, who);
        GrouperLog.debug(LOG, this.s, debug_msg + "true");
      }
      catch (InsufficientPrivilegeException eIP) {
        GrouperLog.debug(LOG, this.s, debug_msg + "false");
        throw new InsufficientPrivilegeException(eIP.getMessage());
      }
    }
    return m;
  } // private Member _canViewSubject(subj, msg)

  private void _canWriteList(Field f, String msg) 
    throws  InsufficientPrivilegeException,
            SchemaException
  {
    // See if we can write to the desired list
    try {
      PrivilegeResolver.getInstance().canPrivDispatch(
        this.s, this, this.s.getSubject(), f.getWritePriv()
      );
    }
    catch (InsufficientPrivilegeException eIP) {
      GrouperLog.debug(LOG, this.s, msg + eIP.getMessage());
      throw new InsufficientPrivilegeException(msg + eIP.getMessage());
    }
  } // private void _canWriteList(f, msg)

  private Map _getAttributes() {
    Iterator iter = this.getGroup_attributes().iterator();
    while (iter.hasNext()) {
      Attribute attr = (Attribute) iter.next();
      this.attrs.put(attr.getField().getName(), attr);
      //this.attrs.put( attr.getField().getName(), attr.getValue() );
    }
    return this.attrs;
  } // private Map _getAttributes()

  private Set _membershipsToDelete(Group g, Subject subj, Field f) 
    throws  MemberDeleteException
  {
    Set memberships = new LinkedHashSet();
    try {
      Member m = MemberFinder.findBySubject(this.s, subj);
      // Find the immediate membership that is to be deleted
      Membership imm = MembershipFinder.findImmediateMembership(this.s, g, subj, f);
      memberships.add(imm);
      // Find effective memberships
      // As many of the memberships are likely to be transient, we
      // need to retrieve the persistent version of each before
      // passing it along to be deleted by HibernateHelper.  
      Session   hs    = HibernateHelper.getSession();
      Iterator  iter  = MemberOf.doMemberOf(this.s, g, m).iterator();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
        memberships.add( 
          MembershipFinder.findEffectiveMembership(
            ms.getOwner_id(), ms.getMember_id(), 
            ms.getList(), ms.getVia_id(), ms.getDepth()
          )
        );
      }
      hs.close();
    }
    catch (Exception e) {
      throw new MemberDeleteException(ERR_DM + e.getMessage());
    }
    return memberships;
  } // private Set _membershipsToDelete(g, subj, f)

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

  private Stem getParent_stem() {
    return this.parent_stem;
  }

  protected void setParent_stem(Stem parent_stem) {
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
