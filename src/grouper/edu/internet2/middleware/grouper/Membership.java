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
import  edu.internet2.middleware.subject.*;
import  java.util.Date;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  org.apache.commons.lang.builder.*;

/** 
 * A list membership in the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Membership.java,v 1.68 2007-01-11 19:49:16 blair Exp $
 */
public class Membership {

  // PROTECTED CLASS CONSTANTS //
  protected static final String INTERNAL_TYPE_C = "composite";
  protected static final String INTERNAL_TYPE_E = "effective";
  protected static final String INTERNAL_TYPE_I = "immediate";


  // PRIVATE CLASS CONSTANTS //
  private static final EventLog EL = new EventLog();


  // HIBERNATE PROPERTIES //
  private String  creator_id;
  private long    create_time;
  private int     depth;
  private Field   field;
  private String  id;
  private String  member_id;
  private String  owner_id;
  private String  parent_membership;  // UUID of parent membership
  private String  type;
  private String  uuid;
  private String  via_id;

  
  // PRIVATE INSTANCE VARIABLES //
  private GrouperSession s;


  // CONSTRUCTORS //
  private Membership() {
    // Default constructor for Hibernate.
  }

  // Immediate
  protected Membership(GrouperSession orig, Owner o, Member m, Field f)
    throws  ModelException
  {
    GrouperSession s = o.internal_getSession();
    this.setOwner_id( o.getUuid() );
    this.setMember_id( m.getUuid() );
    this.setField(f);
    this.setMship_type(INTERNAL_TYPE_I);
    this.setUuid( GrouperUuid.internal_getUuid() );
    this.setDepth(0);
    this.setVia_id(null);
    this.setParent_membership(null);
    this.internal_setSession(s);
    this.setCreator_id( orig.getMember().getUuid() );
    this.setCreate_time( new Date().getTime() );
    MembershipValidator.internal_validateImmediate(this);
  } // protected Membership(o, m, f)

  // Effective
  protected Membership(
    GrouperSession s, Membership ms, Membership hasMS, int offset
  )
    throws  ModelException
  { 
    this.setOwner_id( ms.getOwner_id() );
    try {
      this.setMember_id( hasMS.getMember().getUuid() );  // hasMember m
    }
    catch (MemberNotFoundException eMNF) {
      throw new ModelException(eMNF);
    }
    this.setField( ms.getList() );  // original f
    this.setMship_type(INTERNAL_TYPE_E);
    this.setUuid( GrouperUuid.internal_getUuid() );
    this.setDepth( ms.getDepth() + hasMS.getDepth() + offset ); // increment depth with proper offset
    if (hasMS.getDepth() == 0) {
      this.setVia_id( hasMS.getOwner_id() );  // hasMember m was immediate
      this.setParent_membership( ms.getUuid() );
    }
    else {
      this.setVia_id( hasMS.getVia_id() );  // hasMember m was effective
      // TODO 20061011 I have no idea what is going on here
      if ( hasMS.getParent_membership() != null ) {
        this.setParent_membership( hasMS.getParent_membership() );
      }
      else {
        this.setParent_membership( hasMS.getUuid() );
      }
    } 
    this.internal_setSession(s);
    this.setCreator_id( s.getMember().getUuid() );
    this.setCreate_time( new Date().getTime() );
    MembershipValidator.internal_validateEffective(this);
  } // protected static Membership newEffectiveMembership(s, ms, hasMS)

  // Composite
  protected Membership(Owner o, Member m, Field f, Composite via, GrouperSession orig)
    throws  ModelException
  {
    this.setOwner_id( o.getUuid() );
    this.setMember_id( m.getUuid() );
    this.setField(f);
    this.setMship_type(INTERNAL_TYPE_C);
    this.setUuid( GrouperUuid.internal_getUuid() );
    this.setDepth(0);
    this.setVia_id( via.getUuid() );
    this.setParent_membership(null);
    this.internal_setSession(  o.internal_getSession() );
    this.setCreator_id( orig.getMember().getUuid() );
    this.setCreate_time( new Date().getTime() );
    MembershipValidator.internal_validateComposite(this);
  } // protected Membership(o, m, f, via, orig)


  // PUBLIC INSTANCE METHODS //

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Membership)) {
      return false;
    }
    Membership otherMembership = (Membership) other;
    return new EqualsBuilder()
      .append(this.getUuid()      , otherMembership.getUuid()     )
      .append(this.getOwner_id()  , otherMembership.getOwner_id() )
      .append(this.getMember_id() , otherMembership.getMember_id())
      .append(this.getField()     , otherMembership.getField()    )
      .append(this.getVia_id()    , otherMembership.getVia_id()   )
      .append(this.getDepth()     , otherMembership.getDepth()    )
      .isEquals();
  } // public boolean equals(other)

  /** 
   * Get child memberships of this membership. 
   * <pre class="eg"> 
   * Set children = ms.getChildMemberships();
   * </pre>
   * @return  Set of {@link Membership} objects.
   */
  public Set getChildMemberships() {
    // Ideally I would use a Hibernate mapping for this, but...
    //   * It wasn't working and I didn't have time to debug it at the time.
    //   * I still need to filter
    return MembershipFinder.internal_findChildMemberships( this.internal_getSession(), this );
  } // public Set getChildMemberships()


  /**
   * Get this membership's group.
   * <pre class="eg">
   * Group g = ms.getGroup();
   * </pre>
   * @return  A {@link Group}
   */
  public Group getGroup() 
    throws  GroupNotFoundException
  {
    String uuid = this.getOwner_id();
    if (uuid == null) {
      throw new GroupNotFoundException();
    }
    Group g = HibernateGroupDAO.findByUuid(uuid);
    g.internal_setSession( this.internal_getSession() );
    return g;
  } // public Group getGroup()

  /**
   * Get this membership's list.
   * <pre class="eg">
   * String list = g.getList();
   * </pre>
   * @return  The {@link Field} type of this membership.
   */
  public Field getList() {
    return this.getField();
  } // public Field getList()

  /**
   * Get this membership's member.
   * <pre class="eg">
   * Member m = ms.getMember();
   * </pre>
   * @return  A {@link Member}
   * @throws  MemberNotFoundException
   */
  public Member getMember() 
    throws MemberNotFoundException
  {
    String uuid = this.getMember_id();
    if (uuid == null) {
      throw new MemberNotFoundException("membership does not have a member!");
    }
    Member m = HibernateMemberDAO.findByUuid(uuid);
    m.internal_setSession( this.internal_getSession() );
    return m;
  } // public Member getMember()

  /**
   * Get parent membership of this membership.
   * <pre class="eg">
   * try {
   *   Membership parent = ms.getParentMembership();
   * }
   * catch (MembershipNotFoundException e) {
   *   // Unable to retrieve parent membership
   * }
   * </pre>
   * @return  A {@link Membership}
   * @throws  MembershipNotFoundException
   */
  public Membership getParentMembership() 
    throws MembershipNotFoundException
  {
    String uuid = this.getParent_membership();
    if (uuid == null) {
      throw new MembershipNotFoundException("no parent");
    }
    Membership parent = HibernateMembershipDAO.findByUuid(uuid);
    parent.internal_setSession( this.internal_getSession() );
    return parent;
  } // public Membership getParentMembership()

  /**
   * Get this membership's via.
   * <pre class="eg">
   * try {
   *   Owner via = ms.getVia();
   * }
   * catch (OwnerNotFoundException eONF) {
   *   // Unable to retrieve via
   * }
   * </pre>
   * @return  An {@link Owner}.
   * @throws  OwnerNotFoundException
   */
  public Owner getVia()
    throws  OwnerNotFoundException
  {
    String uuid = this.getVia_id();
    if (uuid == null) {
      throw new OwnerNotFoundException();
    }
    Owner via = HibernateOwnerDAO.findByUuid(uuid);
    via.internal_setSession( this.internal_getSession() );
    return via;
  } // public Owner getVia()
 
  /**
   * Get this membership's via group.
   * <p>{@link Group}s with {@link Composite} memberships will <b>not</b> have a
   * via group.  Use the {@link #getVia() getVia()} method instead.</p>
   * <pre class="eg">
   * try {
   *   Group via = ms.getViaGroup();
   * }
   * catch (GroupNotFoundException e) {
   *   // Unable to retrieve via group
   * }
   * </pre>
   * @return  A {@link Group}
   * @throws  GroupNotFoundException
   */
  public Group getViaGroup() 
    throws GroupNotFoundException
  {
    String uuid = this.getVia_id();
    if (uuid == null) {
      throw new GroupNotFoundException();
    }
    Group via = HibernateGroupDAO.findByUuid(uuid);
    via.internal_setSession( this.internal_getSession() );
    return via;
  } // public Group getViaGroup()

  public int hashCode() {
    return new HashCodeBuilder()
      .append(this.getOwner_id()  )
      .append(this.getMember_id() )
      .append(this.getField()     )
      .append(this.getVia_id()    )
      .append(this.getDepth()     )
      .toHashCode();
  } // public int hashCode()

  public String toString() {
    return MembershipHelper.internal_getPretty(this);
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void internal_addImmediateMembership(
    GrouperSession s, Owner o, Subject subj, Field f
  )
    throws  MemberAddException
  {
    try {
      GrouperSessionValidator.internal_validate(s);
      Member      m   = PrivilegeResolver.internal_canViewSubject(s, subj);
      Membership  imm = new Membership(s, o, m, f);
      MemberOf    mof = MemberOf.internal_addImmediate(s, o, imm, m);
      HibernateMembershipDAO.update(mof);
      EL.addEffMembers( s, o, subj, f, mof.internal_getEffSaves() );
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberAddException(eIP.getMessage(), eIP);
    }
    catch (ModelException eM)                   {
      throw new MemberAddException(eM.getMessage(), eM);
    }    
  } // protected static void internal_addImmediateMembership(s, o, subj, f)

  // @since   1.2.0
  protected static MemberOf internal_delImmediateMembership(
    GrouperSession s, Owner o, Subject subj, Field f
  )
    throws  MemberDeleteException
  {
    try {
      GrouperSessionValidator.internal_validate(s); 
      // Who we're deleting
      Member      m   = PrivilegeResolver.internal_canViewSubject(s, subj);
      Membership  imm = MembershipFinder.internal_findByOwnerAndMemberAndFieldAndType( o, m.getUuid(), f, INTERNAL_TYPE_I );
      imm.internal_setSession(s);
      return MemberOf.internal_delImmediate(s, o, imm, m);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberDeleteException(eIP.getMessage(), eIP);
    }
    catch (MembershipNotFoundException eMSNF)   {
      throw new MemberDeleteException(eMSNF.getMessage(), eMSNF);
    }
    catch (ModelException eM)                   {
      throw new MemberDeleteException(eM.getMessage(), eM);
    } 
  } // protected static void internal_delImmediateMembership(s, o, subj, f)

  // @since   1.2.0
  protected static Set internal_deleteAllField(GrouperSession s, Owner o, Field f)
    throws  MemberDeleteException,
            SchemaException
  {
    try {
      GrouperSessionValidator.internal_validate(s);
      GrouperSession  orig  = s;
      GrouperSession  root  = orig.internal_getRootSession();
      o.internal_setSession(root);

      Set deletes = new LinkedHashSet();

      if (o instanceof Group) { // then deal with its immediate membership
        Membership  msG;
        MemberOf    mofG;
        Iterator    iterIs  = ( (Group) o).toMember().getImmediateMemberships(f).iterator();
        try { // TODO 20070109 i'm not happy about the try/catch and call to `internal_getOwner()`
          while (iterIs.hasNext()) {
            msG   = (Membership) iterIs.next();
            mofG  = Membership.internal_delImmediateMembership(
              s, msG.internal_getOwner(), msG.getMember().getSubject(), msG.getField()
            );
            deletes.addAll( mofG.internal_getDeletes() );
          }
        }
        catch (OwnerNotFoundException eONF) {
          throw new MemberDeleteException( eONF.getMessage(), eONF );
        }
      }

      // Now deal with immediate members
      Membership  msM;
      MemberOf    mofM;
      Iterator    iterHas = MembershipFinder.internal_findAllByOwnerAndFieldAndType(
        root, o, f, INTERNAL_TYPE_I
      ).iterator();
      while (iterHas.hasNext()) {
        msM   = (Membership) iterHas.next();
        mofM  = Membership.internal_delImmediateMembership(s, o, msM.getMember().getSubject(), f);
        deletes.addAll( mofM.internal_getDeletes() );
      }

      o.internal_setSession(orig);
      return deletes;
    }
    catch (MemberNotFoundException eMNF) {
      throw new MemberDeleteException(eMNF);
    }
    catch (SubjectNotFoundException eSNF) {
      throw new MemberDeleteException(eSNF);
    }
  } // protected static Set internal_deleteAllField(s, o, f)

  // @since   1.2.0
  protected static Set internal_deleteAllFieldType(GrouperSession s, Owner o, FieldType type) 
    throws  MemberDeleteException,
            SchemaException
  {
    GrouperSessionValidator.internal_validate(s);
    GrouperSession orig = s;
    o.internal_setSession( orig.internal_getRootSession() );

    Set deletes = new LinkedHashSet();

    Field     f;
    Iterator  iter  = FieldFinder.findAllByType(type).iterator();
    while (iter.hasNext()) {
      f = (Field) iter.next();
      deletes.addAll( internal_deleteAllField(s, o, f) );
    }

    o.internal_setSession(orig);
    return deletes;
  } // protected static Set internal_deleteAllFieldType(s, o, f)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected Date internal_getCreateTime() {
    return new Date( this.getCreate_time() );
  } // protected Date internal_getCreateTime()

  // @since   1.2.0
  protected Member internal_getCreator() 
    throws  MemberNotFoundException
  {
    try {
      return HibernateMemberDAO.findByUuid( this.getCreator_id() );
    }
    catch (GrouperDAOException eDAO) {
      throw new GrouperRuntimeException( eDAO.getMessage(), eDAO );
    }
  } // protected Member internal_getCreator()

  // @since   1.2.0
  protected Owner internal_getOwner() 
    throws  OwnerNotFoundException
  { 
    String uuid = this.getOwner_id();
    if (uuid == null) {
      throw new OwnerNotFoundException();
    }
    Owner o = HibernateOwnerDAO.findByUuid(uuid);
    o.internal_setSession( this.internal_getSession() );
    return o;
  } // protected Owner internal_getOwner()

  // @since   1.2.0 
  protected GrouperSession internal_getSession() {
    GrouperSessionValidator.internal_validate(this.s);
    return this.s;
  } // protected GrouperSession internal_getSession()

  // @since   1.2.0
  protected void internal_setSession(GrouperSession s) {
    GrouperSessionValidator.internal_validate(s);
    this.s = s;
  } // protected void internal_setSession(s)

  // @since   1.2.0
  protected Stem internal_getStem() 
    throws StemNotFoundException
  {
    String uuid = this.getOwner_id();
    if (uuid == null) {
      throw new StemNotFoundException();
    }
    Stem ns = HibernateStemDAO.findByUuid(uuid);
    ns.internal_setSession( this.internal_getSession() );
    return ns;
  } // public Stem internal_getStem()


  // GETTERS // 
  private long getCreate_time() {
    return this.create_time;
  }
  protected String getCreator_id() {
    return this.creator_id;
  }
  public int getDepth() {
    return this.depth;
  }
  protected Field getField() {
    return this.field;
  }
  private String getId() {
    return this.id;
  }
  protected String getMember_id() {
    return this.member_id;
  }
  protected String getMship_type() {
    return this.type;
  }
  protected String getOwner_id() {
    return this.owner_id;
  }
  protected String getParent_membership() {
    return this.parent_membership;
  }
  protected String getUuid() {
    return this.uuid;
  }
  protected String getVia_id() {
    return this.via_id;
  }


  // SETTERS //
  private void setDepth(int depth) {
    this.depth = depth;
  }
  private void setField(Field f) {
    this.field = f;
  }
  private void setId(String id) {
    this.id = id;
  }
  private void setMember_id(String member_id) {
    this.member_id = member_id;
  }
  private void setMship_type(String type) {
    this.type = type;
  }
  private void setOwner_id(String o) {
    this.owner_id = o;
  }
  private void setParent_membership(String parent) {
    this.parent_membership = parent;
  }
  private void setCreate_time(long time) {
    this.create_time = time;
  }
  private void setCreator_id(String m) {
    this.creator_id = m;
  }
  private void setVia_id(String via) {
    this.via_id = via;
  }
  private void setUuid(String uuid) {
    this.uuid = uuid;
  }

}
