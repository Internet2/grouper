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
 * A member within the Groups Registry.
 * @author  blair christensen.
 * @version $Id: Member.java,v 1.16 2005-12-04 22:52:49 blair Exp $
 */
public class Member implements Serializable {

  // Private Class Constants
  private static final Log LOG = LogFactory.getLog(Member.class);


  // Hibernate Properties
  private String  id;
  private String  subject_id;
  private String  subject_source;
  private String  subject_type;
  private String  uuid;


  // Private Transient Instance Properties
  private transient GrouperSession  s     = null;
  private transient Subject         subj  = null;

  // Constructors

  /**
   * Default constructor for Hibernate.
   */
  public Member() {
    // Nothing
  }

  protected Member(Subject subj) {
    // Persistent Properties
    this.setSubject_id( subj.getId() );
    this.setSubject_source( subj.getSource().getId() );
    this.setSubject_type( subj.getType().getName() );
    this.setMember_id( GrouperUuid.getUuid() );

    // Transient Properties  
    this.subj = subj;
  } // protected Member()


  // Public Instance Methods

  /**
   * Get groups where this member has an effective membership.
   * <pre class="eg">
   * // Get groups where this member is an effective member.
   * Set effectives = m.getEffectiveGroups();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set getEffectiveGroups() {
    Set groups = new LinkedHashSet();
    Iterator iter = this.getEffectiveMemberships().iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        Group g = ms.getGroup();
        groups.add(g);
      }
      catch (GroupNotFoundException eGNF) {
        // TODO Ignore?  Kvetch?
      }
    }
    return groups;
  } // public Set getEffectiveGroups()

  /**
   * Get effective memberships.
   * <pre class="eg">
   * Set effectives = m.getEffectiveMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   */
  public Set getEffectiveMemberships() {
    return MembershipFinder.findEffectiveMemberships(
      this.s, this, Group.getDefaultList()
    );
  } // public Set getEffectiveMemberships()

  /**
   * Get groups where this member is a member.
   * <pre class="eg">
   * // Get groups where this member is a member.
   * Set groups = m.getGroups();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set getGroups() {
    Set groups = new LinkedHashSet();
    Iterator iter = this.getMemberships().iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        Group g = ms.getGroup();
        groups.add(g);
      }
      catch (GroupNotFoundException eGNF) {
        // TODO Ignore?  Kvetch?
      }
    }
    return groups;
  } // public Set getGroups()

  /**
   * Get groups where this member has an immediate membership.
   * <pre class="eg">
   * // Get groups where this member is an immediate member.
   * Set immediates = m.getImmediateGroups();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set getImmediateGroups() {
    Set groups = new LinkedHashSet();
    Iterator iter = this.getImmediateMemberships().iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        Group g = ms.getGroup();
        groups.add(g);
      }
      catch (GroupNotFoundException eGNF) {
        // TODO Ignore?  Kvetch?
      }
    }
    return groups;
  } // public Set getImmediateGroups()

  /**
   * Get immediate memberships.
   * <pre class="eg">
   * Set immediates = m.getImmediateMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   */
  public Set getImmediateMemberships() {
    return MembershipFinder.findImmediateMemberships(
      this.s, this, Group.getDefaultList()
    );
  } // public Set getImmediateMemberships()

  /**
   * Get memberships.
   * <pre class="eg">
   * Set groups = m.getMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   */
  public Set getMemberships() {
    return MembershipFinder.findMemberships(
      this, Group.getDefaultList()
    );
  } // public Set getMemberships()

  /**
   * Find access privileges held by this member on a {@link Group}.
   * <pre class="eg">
   * Set access = m.getPrivs(g);
   * </pre>
   * @param   g   Find Access Privileges on this {@link Group}
   * @return  A set of {@link AccessPrivilege} objects.
   */
  public Set getPrivs(Group g) {
    try {
      return PrivilegeResolver.getInstance().getPrivs(
        this.s, g, this.getSubject()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      // TODO Bah
      throw new RuntimeException(
        "unable to get privs on " + g.getName() + ": " + eSNF.getMessage()
      );
    }
  } // public Set getPrivs(g)

  /**
   * Find naming privileges held by this member on a {@link Stem}.
   * <pre class="eg">
   * Set naming = m.getPrivs(ns);
   * </pre>
   * @param   ns  Find Naming Privileges on this {@link Stem}
   * @return  A set of {@link NamingPrivilege} objects.
   */
  public Set getPrivs(Stem ns) {
    try {
      return PrivilegeResolver.getInstance().getPrivs(
        this.s, ns, this.getSubject()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      // TODO Bah
      throw new RuntimeException(
        "unable to get privs on " + ns.getName() + ": " + eSNF.getMessage()
      );
    }
  } // public Set getPrivs(ns)

  /**
   * Get {@link Subject} that maps to this member.
   * <pre class="eg">
   * // Convert a member back into a subject
   * try {
   *   Subject subj = m.getSubject();
   * }
   * catch (SubjectNotFoundException e) {
   *   // Subject not found
   * }
   * </pre>
   * @return  A {@link Subject} object.
   * @throws  SubjectNotFoundException
   */ 
  public Subject getSubject() 
    throws SubjectNotFoundException
  {
    if (this.subj == null) {
      this.subj = SubjectFinder.findById(
        this.getSubject_id(), this.getSubject_type()
      );
    }
    return this.subj;
  }

  /**
   * Get the subject id of the subject that maps to this member.
   * <pre class="eg">
   * // Get this member's subject id.
   * String id = m.getSubjectId();
   * </pre>
   * @return  Subject id
   */ 
  public String getSubjectId() {
    return this.getSubject_id();
  }

  /**
   * Get the {@link Source} of the subject that maps to this member.
   * <pre class="eg">
   * // Get this member's source.
   * Source sa = m.getSubjectSource();
   * </pre>
   * @return  Subject's {@link Source}
   */ 
  public Source getSubjectSource() {
    throw new RuntimeException("Not implemented");
  }

  /** Get the {@link Source} id of the subject that maps to this
   * member.
   * <pre class="eg">
   * String id = m.getSubjectSourceId();
   * </pre>
   * @return  Subject's {@link Source} id
   */
  public String getSubjectSourceId() {
    return this.getSubject_source();
  } 

  /**
   * Get the {@link SubjectType} of the subject that maps to this member.
   * <pre class="eg">
   * // Get this member's subject type.
   * SubjectType type = m.getSubjectType();
   * </pre>
   * @return  Subject's {@link SubjectType}
   */ 
  public SubjectType getSubjectType() {
    return SubjectTypeEnum.valueOf(this.getSubject_type());
  } // public SubjectType getSubjectType()

  /**
   * Get the subject type id of the subject that maps to this member.
   * <pre class="eg">
   * // Get this member's subject type id.
   * String type = m.getSubjectTypeId();
   * </pre>
   * @return  Subject's type id.
   */ 
  public String getSubjectTypeId() {
    return this.getSubject_type();
  }

  /**
   * Get member's UUID.
   * <pre class="eg">
   * // Get UUID of member.
   * String uuid = m.getUuid();
   * </pre>
   * @return  Member's UUID.
   */
  public String getUuid() {
    return this.getMember_id();
  }

  /**
   * Get groups where this member has the ADMIN privilege.
   * <pre class="eg">
   * // Get groups where this member has the ADMIN privilege.
   * Set admin = m.hasAdmin();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set hasAdmin() {
    try {
      return PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.s, this.getSubject(), AccessPrivilege.ADMIN
      );
    }
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(eSNF.getMessage());
    }
  } // public Set hasAdmin()

  /**
   * Report whether this member has ADMIN on the specified group.
   * <pre class="eg">
   * // Check whether this member has ADMIN on the specified group.
   * if (m.hasAdmin(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasAdmin(Group g) {
    return this._hasPriv(g, AccessPrivilege.ADMIN);
  } // public boolean hasAdmin(g)

  /**
   * Get stems where this member has the CREATE privilege.
   * <pre class="eg">
   * // Get stems where this member has the CREATE privilege.
   * Set create = m.hasCreate();
   * </pre>
   * @return  Set of {@link Stem} objects.
   */
  public Set hasCreate() {
    try {
      return PrivilegeResolver.getInstance().getStemsWhereSubjectHasPriv(
        this.s, this.getSubject(), NamingPrivilege.CREATE
      );
    } 
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(eSNF.getMessage());
    }
  } // public Set hasCreate()

  /**
   * Report whether this member has CREATE on the specified stem.
   * <pre class="eg">
   * // Check whether this member has CREATE on the specified stem.
   * if (m.hasCreate(ns)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   ns  Test for privilege on this {@link Stem}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasCreate(Stem ns) {
    return this._hasPriv(ns, NamingPrivilege.CREATE);
  } // public boolean hasCreate(ns)

  /**
   * Get groups where this member has the OPTIN privilege.
   * <pre class="eg">
   * // Get groups where this member has the OPTIN privilege.
   * Set optin = m.hasOptin();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set hasOptin() {
    try {
      return PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.s, this.getSubject(), AccessPrivilege.OPTIN
      );
    }
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(eSNF.getMessage());
    }
  } // public Set hasOptin()

  /**
   * Report whether this member has OPTIN on the specified group.
   * <pre class="eg">
   * // Check whether this member has OPTIN on the specified group.
   * if (m.hasOptin(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasOptin(Group g) {
    return this._hasPriv(g, AccessPrivilege.OPTIN);
  } // public boolean hasOptin(g)

  /**
   * Get groups where this member has the OPTOUT privilege.
   * <pre class="eg">
   * // Get groups where this member has the OPTOUT privilege.
   * Set optout = m.hasOptout();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set hasOptout() {
    try {
      return PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.s, this.getSubject(), AccessPrivilege.OPTOUT
      );
    }
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(eSNF.getMessage());
    }
  } // public Set hasOptout()

  /**
   * Report whether this member has OPTOUT on the specified group.
   * <pre class="eg">
   * // Check whether this member has OPTOUT on the specified group.
   * if (m.hasOptout(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasOptout(Group g) {
    return this._hasPriv(g, AccessPrivilege.OPTOUT);
  } // public boolean hasOptout(g)

  /**
   * Get groups where this member has the READ privilege.
   * <pre class="eg">
   * // Get groups where this member has the READ privilege.
   * Set read = m.hasRead();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set hasRead() {
    try {
      return PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.s, this.getSubject(), AccessPrivilege.READ
      );
    }
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(eSNF.getMessage());
    }
  } // public Set hasRead()

  /**
   * Report whether this member has READ on the specified group.
   * <pre class="eg">
   * // Check whether this member has READ on the specified group.
   * if (m.hasRead(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasRead(Group g) {
    return this._hasPriv(g, AccessPrivilege.READ);
  } // public boolean _hasPriv(g)

  /**
   * Get stems where this member has the STEM privilege.
   * <pre class="eg">
   * // Get stems where this member has the STEM privilege.
   * Set stem = m.hasStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   */
  public Set hasStem() {
    try {
      return PrivilegeResolver.getInstance().getStemsWhereSubjectHasPriv(
        this.s, this.getSubject(), NamingPrivilege.STEM
      );
    }
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(eSNF.getMessage());
    }
  } // public Set hasStem()

  /**
   * Report whether this member has STEM on the specified stem.
   * <pre class="eg">
   * if (m.hasStem(ns)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   ns  Test for privilege on this {@link Stem}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasStem(Stem ns) {
    return this._hasPriv(ns, NamingPrivilege.STEM);
  } // public boolean hasStem(ns)

  /**
   * Get groups where this member has the UPDATE privilege.
   * <pre class="eg">
   * // Get groups where this member has the UPDATE privilege.
   * Set update = m.hasUpdate();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set hasUpdate() {
    try {
      return PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.s, this.getSubject(), AccessPrivilege.UPDATE
      );
    }
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(eSNF.getMessage());
    }
  } // public Set hasUpdate()

  /**
   * Report whether this member has UPDATE on the specified group.
   * <pre class="eg">
   * // Check whether this member has UPDATE on the specified group.
   * if (m.hasUpdate(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasUpdate(Group g) {
    return this._hasPriv(g, AccessPrivilege.UPDATE);
  } // public boolean hasUpdate(g)

  /**
   * Get groups where this member has the VIEW privilege.
   * <pre class="eg">
   * // Get groups where this member has the VIEW privilege.
   * Set view = m.hasView();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set hasView() {
    try {
      return PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.s, this.getSubject(), AccessPrivilege.VIEW
      );
    }
    catch (SubjectNotFoundException eSNF) {
      throw new RuntimeException(eSNF.getMessage());
    }
  } // public Set hasView()

  /**
   * Report whether this member has VIEW on the specified group.
   * <pre class="eg">
   * // Check whether this member has VIEW on the specified group.
   * if (m.hasView(g)) {
   *   // Member has privilege
   * }
   * </pre>
   * @param   g   Test for privilege on this {@link Group}
   * @return  Boolean true if the member has the privilege.
   */
  public boolean hasView(Group g) {
    return this._hasPriv(g, AccessPrivilege.VIEW);
  } // public boolean hasview(g)

  /**
   * Test whether a member effectively belongs to a group.
   * <pre class="eg">
   * // Does this member effectively belong to the specified group?
   * if (m.isEffectiveMember(g)) {
   *   // Is an effective member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   */
  public boolean isEffectiveMember(Group g) {
    if 
    (
      MembershipFinder.findEffectiveMemberships(
        g, this, Group.getDefaultList()
      ).size() > 0
    )
    {
      return true;
    }
    return false;
  } // public boolean isEffectiveMember(g)

  /**
   * Test whether a member immediately belongs to a group.
   * <pre class="eg">
   * // Does this member immediately belong to the specified group?
   * if (m.isImmediateMember(g)) {
   *   // Is an immediate member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   */
  public boolean isImmediateMember(Group g) {
    try {
      Membership ms = MembershipFinder.findImmediateMembership(
        this.s, g, this.getSubject(), Group.getDefaultList()
      );
      return true;
    }
    catch (MembershipNotFoundException eMNF) {
      return false;
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } // public boolean isImmediateMember(g)

  /**
   * Test whether a member belongs to a group.
   * <pre class="eg">
   * // Does this member belong to the specified group?
   * if (m.isMember(g)) {
   *   // Is a member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   */
  // TODO isEffectiveMember() and isImmediateMember()?
  public boolean isMember(Group g) {
    return this.isMember(g, Group.getDefaultList());
  } // public boolean isMember(g)

  /**
   * Test whether a member belongs to the specified group list.
   * <pre class="eg">
   * // Does this member belong to the specified group?
   * if (m.isMember(g, f)) {
   *   // Is a member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @param   f   Test for membership in this list {@link Field}.
   * @return  Boolean true if is a member.
   */
  // TODO isEffectiveMember() and isImmediateMember()?
  public boolean isMember(Group g, Field f) {
    boolean rv = false;
    if (
      MembershipFinder.findMemberships(g.getUuid(), this, f).size() > 0
    ) 
    {
      rv = true;
    }
    GrouperLog.debug(LOG, this.s, "isMember '" + this + "': " + rv);
    return rv;
  } // public boolean isMember(g, f)

  public String toString() {
    try {
      String  who   = this.getSubject().getId();
      String  type  = this.getSubject().getType().getName();
      if (type.equals("group")) {
        who = this.getSubject().getName();
      } 
      // Prettier
      return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
        .append("subject_id"    , who )
        .append("subject_type"  , type)
        .toString();
    }
    catch (SubjectNotFoundException eSNF) {
      // But better than nothing
      return new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE)
        .append("subject_id"    , this.getSubject_id()  )  
        .append("subject_type"  , this.getSubject_type())
        .toString();
    }
  } // public String toString()

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Member)) {
      return false;
    }
    Member otherMember = (Member) other;
    return new EqualsBuilder()
           .append(this.getSubject_id(),      otherMember.getSubject_id()     )
           .append(this.getSubject_source(),  otherMember.getSubject_source() )
           .append(this.getSubject_type(),    otherMember.getSubject_type()   )
           .append(this.getUuid(),            otherMember.getUuid()           )
           .isEquals();
  } // public boolean equals(other)

  public int hashCode() {
    return new HashCodeBuilder()
           .append(getSubject_id())
           .append(getSubject_source())
           .append(getSubject_type())
           .append(getUuid())
           .toHashCode();
  }

  /**
   * Convert this member back to a {@link Group} object.
   * <p/>
   * <pre class="eg">
   * try {
   *   Group g = m.toGroup();
   * }
   * catch (GroupNotFoundException e) {
   *   // unable to convert member back to group
   * }
   * </pre>
   * @return  {@link Member} as a {@link Group}
   */
  public Group toGroup() 
    throws GroupNotFoundException 
  {
    // TODO TEST Check for group type 
    return GroupFinder.findByUuid(this.s, this.getSubjectId());
  } // public Group toGroup()


  // Protected Class Methods

  // Add a new Member to the Registry
  protected static Member addMember(Subject subj) 
    throws MemberNotFoundException 
  {
    try {
      Member m = new Member(subj);
      HibernateHelper.save(m);
      m.setSubject(subj);
      return m;
    }
    catch (HibernateException e) {
      throw new MemberNotFoundException(
        "unable to save member: " + e.getMessage()
      );
    }
  } // protected static Member addMember(subj)



  // Protected Instance Methods
  
  // Assign Session
  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
  } // protected void setSession(s)

  // Assign Subject
  protected void setSubject(Subject subj) {
    this.subj = subj;
  } // protected void setSubject(subj)


  // Private Instance Methods
  private boolean _hasPriv(Group g, Privilege priv) {
    try {
      return PrivilegeResolver.getInstance().hasPriv(
        this.s, g, this.getSubject(), priv
      );
    }
    catch (SubjectNotFoundException eSNF) {
      // TODO Bah
      throw new RuntimeException(
        "unable to get privs on " + g.getName() + ": " + eSNF.getMessage()
      );
    }
  } // private boolean _hasPriv(g, priv)

  private boolean _hasPriv(Stem ns, Privilege priv) {
    try {
      return PrivilegeResolver.getInstance().hasPriv(
        this.s, ns, this.getSubject(), priv
      );
    }
    catch (SubjectNotFoundException eSNF) {
      // TODO Bah
      throw new RuntimeException(
        "unable to get privs on " + ns.getName() + ": " + eSNF.getMessage()
      );
    }
  } // private boolean _hasPriv(ns, priv)


  // Hibernate Accessors
  private String getId() {
    return this.id;
  } 

  private void setId(String id) {
    this.id = id;
  }

  private String getMember_id() {
    return this.uuid;
  }

  private void setMember_id(String uuid) {
    this.uuid = uuid;
  }

  private String getSubject_id() {
    return this.subject_id;
  }

  private void setSubject_id(String subject_id) {
    this.subject_id = subject_id;
  }

  private String getSubject_source() {
    return this.subject_source;
  }

  private void setSubject_source(String subject_source) {
    this.subject_source = subject_source;
  }

  private String getSubject_type() {
    return this.subject_type;
  }

  private void setSubject_type(String subject_type) {
    this.subject_type = subject_type;
  }

}

