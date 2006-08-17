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
import  edu.internet2.middleware.subject.provider.*;
import  java.io.Serializable;
import  java.util.*;
import  net.sf.hibernate.*;
import  org.apache.commons.lang.builder.*;
import  org.apache.commons.lang.time.*;

/** 
 * A member within the Groups Registry.
 * @author  blair christensen.
 * @version $Id: Member.java,v 1.56 2006-08-17 19:17:46 blair Exp $
 */
public class Member implements Serializable {

  // PUBLIC CLASS CONSTANTS //
  public static final long serialVersionUID = 2348656645982471668L;


  // HIBERNATE PROPERTIES //
  private String  id;
  private String  subject_id;
  private String  subject_source;
  private String  subject_type;
  private String  uuid;


  // PRIVATE TRANSIENT INSTANCE PROPERTIES //
  private transient GrouperSession  s     = null;
  private transient Subject         subj  = null;


  // CONSTRUCTORS //

  protected Member(Subject subj) {
    // Persistent Properties
    this.setSubject_id( subj.getId() );
    this.setSubject_source( subj.getSource().getId() );
    this.setSubject_type( subj.getType().getName() );
    this.setMember_id( GrouperUuid.getUuid() );

    // Transient Properties  
    this.subj = subj;
  } // protected Member()

  // Default constructor for Hibernate.
  // @since   1.0
  private Member() {
    super();
  } // private Member()


  // PUBLIC INSTANCE METHODS //

  /**
   * Can this {@link Member} <b>ADMIN</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canAdmin(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canAdmin(Group g) 
    throws  IllegalArgumentException
  {
    Validator.argNotNull(g, E.GROUP_NULL);
    try {
      return PrivilegeResolver.canADMIN(
        this.getSession(), g, this.getSubject()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  } // public boolean canAdmin(g)

  /**
   * Can this {@link Member} <b>CREATE</b> on this {@link Stem}.
   * <pre class="eg">
   * boolean rv = m.canCreate(ns);
   * </pre>
   * @param   ns  Check privileges on this {@link Stem}.
   * @throws  IllegalArgumentException if null {@link Stem}
   * @since   1.0
   */
  public boolean canCreate(Stem ns) 
    throws  IllegalArgumentException
  {
    Validator.argNotNull(ns, E.STEM_NULL);
    try {
      return PrivilegeResolver.canCREATE(
        this.getSession(), ns, this.getSubject()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  } // public boolean canCreate(ns)

  /**
   * Can this {@link Member} <b>OPTIN</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canAdmin(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canOptin(Group g) 
    throws  IllegalArgumentException
  {
    Validator.argNotNull(g, E.GROUP_NULL);
    try {
      return PrivilegeResolver.canOPTIN(
        this.getSession(), g, this.getSubject()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } // public boolean canOptin(g)

  /**
   * Can this {@link Member} <b>OPTOUT</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canOptout(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canOptout(Group g) 
    throws  IllegalArgumentException
  {
    Validator.argNotNull(g, E.GROUP_NULL);
    try {
      return PrivilegeResolver.canOPTOUT(
        this.getSession(), g, this.getSubject()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } // public boolean canOptout(g)

  /**
   * Can this {@link Member} <b>READ</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canRead(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canRead(Group g)
    throws  IllegalArgumentException
  {
    Validator.argNotNull(g, E.GROUP_NULL);
    try {
      return PrivilegeResolver.canREAD(
        this.getSession(), g, this.getSubject()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } // public boolean canRead(g)

  /**
   * Can this {@link Member} <b>STEM</b> on this {@link Stem}.
   * <pre class="eg">
   * boolean rv = m.canStem(ns);
   * </pre>
   * @param   ns  Check privileges on this {@link Stem}.
   * @throws  IllegalArgumentException if null {@link Stem}
   * @since   1.0
   */
  public boolean canStem(Stem ns) 
    throws  IllegalArgumentException
  {
    Validator.argNotNull(ns, E.STEM_NULL);
    try {
      return PrivilegeResolver.canSTEM(this.getSession(), ns, this.getSubject());
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } // public boolean canStem(ns)

  /**
   * Can this {@link Member} <b>UPDATE</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canUpdate(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canUpdate(Group g) 
    throws  IllegalArgumentException
  {
    Validator.argNotNull(g, E.GROUP_NULL);
    try {
      return PrivilegeResolver.canUPDATE(
        this.getSession(), g, this.getSubject()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      return false;
    }
  } // public boolean canUPDATE(g)

  /**
   * Can this {@link Member} <b>VIEW</b> on this {@link Group}.
   * <pre class="eg">
   * boolean rv = m.canView(g);
   * </pre>
   * @param   g   Check privileges on this {@link Group}.
   * @throws  IllegalArgumentException if null {@link Group}
   * @since   1.0
   */
  public boolean canView(Group g) 
    throws  IllegalArgumentException
  {
    Validator.argNotNull(g, E.GROUP_NULL);
    try {
      return PrivilegeResolver.canVIEW(
        this.getSession(), g, this.getSubject()
      );
    }
    catch (SubjectNotFoundException eSNF) {
      return false; 
    }
  } // public boolean canView(g)

  /**
   * Get groups where this member has an effective membership.
   * <pre class="eg">
   * // Get groups where this member is an effective member.
   * Set effectives = m.getEffectiveGroups();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set getEffectiveGroups() {
    Set         groups  = new LinkedHashSet();
    Membership  ms;
    Group       g;
    Iterator    iter    = this.getEffectiveMemberships().iterator();
    while (iter.hasNext()) {
      ms = (Membership) iter.next();
      try {
        g = ms.getGroup();
        g.setSession(this.getSession());
        groups.add(g);
      }
      catch (GroupNotFoundException eGNF) {
        ErrorLog.error(
          Member.class, E.MEMBER_NOGROUP + U.q(this.getUuid()) + " membership="
          + U.q(ms.getUuid()) + " " + eGNF.getMessage()
        );
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
   * @throws  GrouperRuntimeException
   */
  public Set getEffectiveMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getEffectiveMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getEffectiveMemberships()

  /**
   * Get effective memberships.
   * <pre class="eg">
   * Set effectives = m.getEffectiveMemberships(f);
   * </pre>
   * @param   f   Get effective memberships in this list field.
   * @return  Set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getEffectiveMemberships(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.findEffectiveMemberships(
      this.getSession(), this, f
    );
  } // public Set getEffectiveMemberships(f)

  /**
   * Get groups where this member is a member.
   * <pre class="eg">
   * // Get groups where this member is a member.
   * Set groups = m.getGroups();
   * </pre>
   * @return  Set of {@link Group} objects.
   */
  public Set getGroups() {
    Set         groups  = new LinkedHashSet();
    Membership  ms;
    Group       g;
    Iterator    iter    = this.getMemberships().iterator();
    while (iter.hasNext()) {
      ms = (Membership) iter.next();
      try {
        g = ms.getGroup();
        g.setSession(this.getSession());
        groups.add(g);
      }
      catch (GroupNotFoundException eGNF) {
        ErrorLog.error(
          Member.class, E.MEMBER_NOGROUP + U.q(this.getUuid()) + " membership="
          + U.q(ms.getUuid()) + " " + eGNF.getMessage()
        );
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
    Set         groups  = new LinkedHashSet();
    Membership  ms;
    Group       g;
    Iterator    iter    = this.getImmediateMemberships().iterator();
    while (iter.hasNext()) {
      ms = (Membership) iter.next();
      try {
        g = ms.getGroup();
        g.setSession(this.getSession());
        groups.add(g);
      }
      catch (GroupNotFoundException eGNF) {
        ErrorLog.error(
          Member.class, E.MEMBER_NOGROUP + U.q(this.getUuid()) + " membership="
          + U.q(ms.getUuid()) + " " + eGNF.getMessage()
        );
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
   * @throws  GrouperRuntimeException
   */
  public Set getImmediateMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getImmediateMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getImmediateMemberships()

  /**
   * Get immediate memberships.
   * <pre class="eg">
   * Set immediates = m.getImmediateMemberships(f);
   * </pre>
   * @param   f   Get immediate memberships in this list field.
   * @return  Set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getImmediateMemberships(Field f) 
    throws  SchemaException
  {
    return MembershipFinder.findImmediateMemberships(
      this.getSession(), this, f
    );
  } // public Set getImmediateMemberships(f)

  /**
   * Get memberships.
   * <pre class="eg">
   * Set groups = m.getMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   * @throws  GrouperRuntimeException
   */
  public Set getMemberships() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getMemberships(Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public Set getMemberships()

  /**
   * Get memberships.
   * <pre class="eg">
   * Set groups = m.getMemberships(f);
   * </pre>
   * @param   f   Get memberships in this list field.
   * @return  Set of {@link Membership} objects.
   * @throws  SchemaException
   */
  public Set getMemberships(Field f) 
    throws  SchemaException
  {
    if (!f.getType().equals(FieldType.LIST)) {
      throw new SchemaException(f + " is not type " + FieldType.LIST);
    }
    return MembershipFinder.findMemberships(
      this.getSession(), this, f
    );
  } // public Set getMemberships(f)

  /**
   * Find access privileges held by this member on a {@link Group}.
   * <pre class="eg">
   * Set access = m.getPrivs(g);
   * </pre>
   * @param   g   Find Access Privileges on this {@link Group}
   * @return  A set of {@link AccessPrivilege} objects.
   */
  public Set getPrivs(Group g) {
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getPrivs(this.getSession(), g, this.getSubject());
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getPrivs(this.getSession(), ns, this.getSubject());
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
    throws  SubjectNotFoundException
  {
    if (this.subj == null) {
      try {
        this.subj = SubjectFinder.findById(
          this.getSubject_id(), this.getSubject_type(), this.getSubjectSourceId()
        );
      }
      catch (SourceUnavailableException eSU) {
        throw new SubjectNotFoundException(eSU.getMessage(), eSU);
      }
      catch (SubjectNotUniqueException eSNU) {
        throw new SubjectNotFoundException(eSNU.getMessage(), eSNU);
      }
    }
    return this.subj;
  } // public Subject getSubject()

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
   * Source sa = m.getSubjectSource();
   * </pre>
   * @return  Subject's {@link Source}
   * @throws  GrouperRuntimeException
   */ 
  public Source getSubjectSource() 
    throws  GrouperRuntimeException
  {
    try {
      return this.getSubject().getSource();
    }
    catch (SubjectNotFoundException eSNF) {
      String msg = E.MEMBER_SUBJNOTFOUND + eSNF.getMessage();
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eSNF);
    }
  } // public Source getSubjectSource()

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
   * Set admin = m.hasAdmin();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set hasAdmin() 
    throws  GrouperRuntimeException
{
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.getSession(), this.getSubject(), AccessPrivilege.ADMIN
      );
    }
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.ADMIN;
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
   * Set create = m.hasCreate();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperRuntimeException
   */
  public Set hasCreate() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getInstance().getStemsWhereSubjectHasPriv(
        this.getSession(), this.getSubject(), NamingPrivilege.CREATE
      );
    } 
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + NamingPrivilege.CREATE;
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
   * Set optin = m.hasOptin();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set hasOptin() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.getSession(), this.getSubject(), AccessPrivilege.OPTIN
      );
    }
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.OPTIN;
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
   * Set optout = m.hasOptout();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set hasOptout() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.getSession(), this.getSubject(), AccessPrivilege.OPTOUT
      );
    }
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.OPTOUT;
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
   * Set read = m.hasRead();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set hasRead() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.getSession(), this.getSubject(), AccessPrivilege.READ
      );
    }
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.READ;
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
   * Set stem = m.hasStem();
   * </pre>
   * @return  Set of {@link Stem} objects.
   * @throws  GrouperRuntimeException
   */
  public Set hasStem()
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getInstance().getStemsWhereSubjectHasPriv(
        this.getSession(), this.getSubject(), NamingPrivilege.STEM
      );
    }
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + NamingPrivilege.STEM;
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
   * Set update = m.hasUpdate();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set hasUpdate() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.getSession(), this.getSubject(), AccessPrivilege.UPDATE
      );
    }
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.UPDATE;
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
   * Set view = m.hasView();
   * </pre>
   * @return  Set of {@link Group} objects.
   * @throws  GrouperRuntimeException
   */
  public Set hasView() 
    throws  GrouperRuntimeException
  {
    Set privs = new LinkedHashSet();
    try {
      privs = PrivilegeResolver.getInstance().getGroupsWhereSubjectHasPriv(
        this.getSession(), this.getSubject(), AccessPrivilege.VIEW
      );
    }
    catch (SchemaException eS) { 
      String msg = E.FIELD_REQNOTFOUND + AccessPrivilege.VIEW;
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return privs;
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
   * if (m.isEffectiveMember(g)) {
   *   // Is an effective member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   * @throws  GrouperRuntimeException
   */
  public boolean isEffectiveMember(Group g) 
    throws  GrouperRuntimeException
  {
    try {
      return this.isEffectiveMember(g, Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public boolean isEffectiveMember(g);

  /**
   * Test whether a member effectively belongs to a group.
   * <pre class="eg">
   * // Does this member effectively belong to the specified group?
   * if (m.isEffectiveMember(g, f)) {
   *   // Is an effective member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @param   f   Test for membership in this list field.
   * @return  Boolean true if is a member.
   * @throws  SchemaException
   */
  public boolean isEffectiveMember(Group g, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    if (
      MembershipFinder.findEffectiveMemberships(g, this, f).size() > 0
    ) 
    {
      rv = true;
    }
    else if (
      MembershipFinder.findEffectiveMemberships(
        g, MemberFinder.findAllMember(), f
      ).size() > 0
    )
    {
      // TODO I wonder about this.
      rv = true;
    }
    return rv;
  } // public boolean isEffectiveMember(g, f)

  /**
   * Test whether a member immediately belongs to a group.
   * <pre class="eg">
   * if (m.isImmediateMember(g)) {
   *   // Is an immediate member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   * @throws  GrouperRuntimeException
   */
  public boolean isImmediateMember(Group g) 
    throws  GrouperRuntimeException
  {
    try {
      return this.isImmediateMember(g, Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
  } // public boolean isImmediateMember(g)

  /**
   * Test whether a member immediately belongs to a group.
   * <pre class="eg">
   * // Does this member immediately belong to the specified group?
   * if (m.isImmediateMember(g, f)) {
   *   // Is an immediate member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @param   f   Test for memberhip in this list field.
   * @return  Boolean true if is a member.
   * @throws  SchemaException
   */
  public boolean isImmediateMember(Group g, Field f) 
    throws  SchemaException
  {
    boolean rv = false;
    try {
      Subject subj = this.getSubject();
      try {
        MembershipFinder.findImmediateMembership(this.getSession(), g, subj, f);
        rv = true;
      }
      catch (MembershipNotFoundException eMNF) {
        try {
          MembershipFinder.findMembershipByTypeNoPrivNoSession(
            g, MemberFinder.findAllMember(), f, MembershipType.I
          );
          rv = true;
        }
        catch (MembershipNotFoundException anotherMNF) {
          // ignore
        }
      }
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return rv;
  } // public boolean isImmediateMember(g, f)

  /**
   * Test whether a member belongs to a group.
   * <pre class="eg">
   * if (m.isMember(g)) {
   *   // Is a member
   * }
   * </pre>
   * @param   g   Test for membership in this group.
   * @return  Boolean true if is a member.
   * @throws  GrouperRuntimeException
   */
  public boolean isMember(Group g) 
    throws  GrouperRuntimeException
  {
    try {
      return this._isMember(g, Group.getDefaultList());
    }
    catch (SchemaException eS) {
      // If we don't have "members" we have serious issues
      String msg = E.GROUP_NODEFAULTLIST + eS.getMessage();
      ErrorLog.fatal(Member.class, msg);
      throw new GrouperRuntimeException(msg, eS);
    }
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
   * @throws  SchemaException
   */
  public boolean isMember(Group g, Field f) 
    throws  SchemaException
  {
    return this._isMember(g, f);
  } // public boolean isMember(g, f)

  /**
   * Change subject id.
   * <p>
   * You must be a root-like {@link Subject} to use this method.
   * </p>
   * <pre class="eg">
   * try {
   *   m.setSubjectId("new id");
   * }
   * catch (InsufficientPrivilegeException eIP) {
   *   // not privileged to change subject id
   * }
   * </pre>
   * @param   id  Set subject id to this.
   * @throws  InsufficientPrivilegeException
   */
  public void setSubjectId(String id) 
    throws  InsufficientPrivilegeException
  {
    StopWatch sw = new StopWatch();
    sw.start();

    // Don't let ALL and root be updated
    if (this.subject_type.equals(GrouperConfig.IST)) {
      if (this.subject_source.equals(InternalSourceAdapter.ID)) {
        if (this.subject_id.equals(GrouperConfig.ROOT)) {
          String err = "cannot change root subject id";
          throw new InsufficientPrivilegeException(err);
        }
        if (this.subject_id.equals(GrouperConfig.ALL)) {
          String err = "cannot change ALL subject id";
          throw new InsufficientPrivilegeException(err);
        }
      }
    } 
    String oid = this.getSubject_id();
    String msg = "not privileged to change subjectId";
    if (PrivilegeResolver.isRoot(this.getSession().getSubject())) {
      try {
        this.setSubject_id(id);
        HibernateHelper.save(this);
      }
      catch (Exception e) {
        msg = e.getMessage();
      }
    }
    // TODO WTF?
    if (this.getSubject_id().equals(oid)) {
      throw new InsufficientPrivilegeException(msg);
    }
    sw.stop();
    EventLog.info(
      this.getSession(),
      M.MEMBER_CHANGESID + U.q(this.getUuid()) + " old=" + U.q(oid) + " new=" + U.q(id),
      sw
    );
  } // public void setSubjectId(id)

  public String toString() {
    try {
      return SubjectHelper.getPretty(this.getSubject());
    }
    catch (SubjectNotFoundException eSNF) {
      return new ToStringBuilder(this).toString();
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
      .append(this.getSubject_id()    , otherMember.getSubject_id()     )
      .append(this.getSubject_source(), otherMember.getSubject_source() )
      .append(this.getSubject_type()  , otherMember.getSubject_type()   )
      .append(this.getUuid()          , otherMember.getUuid()           )
      .isEquals();
  } // public boolean equals(other)

  public int hashCode() {
    return new HashCodeBuilder()
      .append(getSubject_id()     )
      .append(getSubject_source() )
      .append(getSubject_type()   )
      .append(getUuid()           )
      .toHashCode();
  } // public int hashCode()

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
    return GroupFinder.findByUuid(this.getSession(), this.getSubjectId());
  } // public Group toGroup()


  // PROTECTED CLASS METHODS //

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
    catch (HibernateException eH) {
      throw new MemberNotFoundException(
        "unable to save member: " + eH.getMessage(), eH
      );
    }
  } // protected static Member addMember(subj)


  // PROTECTED INSTANCE METHODS //
 
  // Find *all* memberships for this member 
  // @filtered  no
  // @session   yes
  protected Set getAllMemberships() {
    return MembershipFinder.findAllMemberships(this.getSession(), this);
  } // protected Set getAllMemberships()

  protected GrouperSession getSession() {
    GrouperSession.validate(this.s);
    return this.s;
  } // protected GrouperSession getSession()
  
  protected boolean isMember(Stem ns, Field f) 
    throws  SchemaException
  {
    return this._isMember(ns, f);
  } // protected boolean isMember(ns, f)

  // Assign Session
  protected void setSession(GrouperSession s) {
    GrouperSession.validate(s);
    this.s = s;
  } // protected void setSession(s)

  // Assign Subject
  protected void setSubject(Subject subj) {
    this.subj = subj;
  } // protected void setSubject(subj)


  // PRIVATE INSTANCE METHODS //
  private boolean _hasPriv(Group g, Privilege priv) {
    boolean rv = false;
    try {
      rv = PrivilegeResolver.getInstance().hasPriv(
        this.getSession(), g, this.getSubject(), priv
      );
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return rv;
  } // private boolean _hasPriv(g, priv)

  private boolean _hasPriv(Stem ns, Privilege priv) {
    boolean rv = false;
    try {
      rv = PrivilegeResolver.getInstance().hasPriv(
        this.getSession(), ns, this.getSubject(), priv
      );
    }
    catch (SubjectNotFoundException eSNF) {
      ErrorLog.error(Member.class, E.MEMBER_SUBJNOTFOUND + eSNF.getMessage());
    }
    return rv;
  } // private boolean _hasPriv(ns, priv)

  private boolean _isMember(Owner o, Field f)
    throws  SchemaException
  {
    boolean rv  = false;
    Set mships = MembershipFinder.findMembershipsNoPrivsNoSession(o, this, f);
    if (mships.size() > 0) {
      rv = true;
    }
    else {
      mships = MembershipFinder.findMembershipsNoPrivsNoSession(
        o, MemberFinder.findAllMember(), f
      );
      if (mships.size() > 0) {
        rv = true;
      }
    }
    return rv;
  } // private boolean _isMember(o, f);


  // GETTERS //
  // TODO Make private
  protected String getId() {
    return this.id;
  } 
  private String getMember_id() {
    return this.uuid;
  }
  private String getSubject_id() {
    return this.subject_id;
  }
  private String getSubject_source() {
    return this.subject_source;
  }
  private String getSubject_type() {
    return this.subject_type;
  }


  // SETTERS //
  private void setId(String id) {
    this.id = id;
  }
  private void setMember_id(String uuid) {
    this.uuid = uuid;
  }
  private void setSubject_id(String subject_id) {
    this.subject_id = subject_id;
  }
  private void setSubject_source(String subject_source) {
    this.subject_source = subject_source;
  }
  private void setSubject_type(String subject_type) {
    this.subject_type = subject_type;
  }

}

