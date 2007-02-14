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

/** 
 * A list membership in the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Membership.java,v 1.71 2007-02-14 17:06:28 blair Exp $
 */
public class Membership extends GrouperAPI {

  // PUBLIC CLASS CONSTANTS //
  public static final String COMPOSITE = "composite";
  public static final String EFFECTIVE = "effective";
  public static final String IMMEDIATE = "immediate";


  // PRIVATE CLASS CONSTANTS //
  private static final EventLog EL = new EventLog();


  // PUBLIC INSTANCE METHODS //

  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof Membership)) {
      return false;
    }
    return this.getDTO().equals( ( (Membership) other ).getDTO() );
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
    return PrivilegeResolver.internal_canViewMemberships(
      this.getSession(), HibernateMembershipDAO.findAllChildMemberships( this.getDTO() )
    );
  } // public Set getChildMemberships()

  /**
   * @since   1.2.0
   */
  public Date getCreateTime() {
    return new Date( this.getDTO().getCreateTime() );
  } // public Date getCreateTime()

  /**
   * @since   1.2.0
   */
  public Member getCreator() 
    throws  MemberNotFoundException
  {
    try {
      Member m = new Member();
      m.setDTO( HibernateMemberDAO.findByUuid( this.getDTO().getCreatorUuid() ) );
      return m;
    }
    catch (GrouperDAOException eDAO) {
      throw new GrouperRuntimeException( eDAO.getMessage(), eDAO );
    }
  } // public Member getCreator()

  /**
   */
  public int getDepth() {
    return this.getDTO().getDepth();
  } // public int getDepth()
   
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
    String uuid = this.getDTO().getOwnerUuid();
    if (uuid == null) {
      throw new GroupNotFoundException();
    }
    Group g = new Group();
    g.setDTO( HibernateGroupDAO.findByUuid(uuid) );
    g.setSession( this.getSession() );
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
    try {
      return FieldFinder.find( this.getDTO().getListName() );
    }
    catch (SchemaException eS) {
      throw new GrouperRuntimeException( eS.getMessage(), eS );
    }
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
    String uuid = this.getDTO().getMemberUuid();
    if (uuid == null) {
      throw new MemberNotFoundException("membership does not have a member!");
    }
    MemberDTO dto = HibernateMemberDAO.findByUuid(uuid);
    Member    m   = new Member();
    m.setDTO(dto);
    m.setSession( this.getSession() );
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
    String uuid = this.getDTO().getParentUuid();
    if (uuid == null) {
      throw new MembershipNotFoundException("no parent");
    }
    Membership parent = new Membership();
    parent.setDTO( HibernateMembershipDAO.findByUuid(uuid) );
    parent.setSession( this.getSession() );
    return parent;
  } // public Membership getParentMembership()

  /** 
   * @since   1.2.0
   */
  public Stem getStem() 
    throws StemNotFoundException
  {
    String uuid = this.getDTO().getOwnerUuid();
    if (uuid == null) {
      throw new StemNotFoundException("membership stem not found");
    }
    Stem ns = new Stem();
    ns.setDTO( HibernateStemDAO.findByUuid(uuid) );
    ns.setSession( this.getSession() );
    return ns;
  } // public Stem getStem()

  /**
   * @since   1.2.0
   */
  public String getType() {
    return this.getDTO().getType();
  } // public String getType()

  /**
   */
  public String getUuid() {
    return this.getDTO().getMembershipUuid();
  } // public String getUuid()

  /**
   * @since   1.2.0
   */
  public Composite getViaComposite() 
    throws  CompositeNotFoundException
  {
    String uuid = this.getDTO().getViaUuid();
    if (uuid == null) {
      throw new CompositeNotFoundException();
    }
    Composite via = new Composite();
    via.setDTO( HibernateCompositeDAO.findByUuid(uuid) );
    via.setSession( this.getSession() );
    return via;
  } // public Composite getViaComposite()

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
    String uuid = this.getDTO().getViaUuid();
    if (uuid == null) {
      throw new GroupNotFoundException();
    }
    Group via = new Group();
    via.setDTO( HibernateGroupDAO.findByUuid(uuid) );
    via.setSession( this.getSession() );
    return via;
  } // public Group getViaGroup()

  public int hashCode() {
    return this.getDTO().hashCode();
  } // public int hashCode()

  public String toString() {
    return this.getDTO().toString();
  } // public String toString()


  // PROTECTED CLASS METHODS //

  // TODO 20070123 figure out what to do with these protected class methods

  // @since   1.2.0
  protected static void internal_addImmediateMembership(
    GrouperSession s, Owner o, Subject subj, Field f
  )
    throws  MemberAddException
  {
    try {
      GrouperSessionValidator.internal_validate(s);
      Member m = PrivilegeResolver.internal_canViewSubject(s, subj);

      MembershipDTO dto = new MembershipDTO();
      dto.setCreateTime( new Date().getTime() );
      dto.setCreatorUuid( s.getMember().getUuid() );
      dto.setDepth(0);
      dto.setListName( f.getName() );
      dto.setListType( f.getType().toString() );
      dto.setMemberUuid( m.getUuid() );
      dto.setMembershipUuid( GrouperUuid.internal_getUuid() );
      dto.setOwnerUuid( o.getUuid() );
      dto.setParentUuid(null);
      dto.setType(IMMEDIATE);
      dto.setViaUuid(null);
      MembershipValidator.internal_validateImmediate(dto);

      MemberOf mof = MemberOf.internal_addImmediate(s, o, dto, m);
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
      Member m = PrivilegeResolver.internal_canViewSubject(s, subj);
      return MemberOf.internal_delImmediate(
        s, o, HibernateMembershipDAO.findByOwnerAndMemberAndFieldAndType( o.getUuid(), m.getUuid(), f, IMMEDIATE ), m
      );
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
      GrouperSession  root  = orig.getDTO().getRootSession();
      // TODO 20070130 bah
      if (o instanceof Group) {
        ( (Group) o ).setSession(root);
      }
      else {
        ( (Stem) o ).setSession(root);
      }

      Set deletes = new LinkedHashSet();

      if (o instanceof Group) { // then deal with its immediate membership
        Membership  msG;
        MemberOf    mofG;
        Iterator    iterIs  = ( (Group) o).toMember().getImmediateMemberships(f).iterator();
        // TODO 20070109 i'm not happy about the try/catch and call to `getOwner()`
        while (iterIs.hasNext()) {
          msG   = (Membership) iterIs.next();
          mofG  = Membership.internal_delImmediateMembership(
            s, MembershipHelper.getOwner(msG), msG.getMember().getSubject(), msG.getList()
          );
          deletes.addAll( mofG.internal_getDeletes() );
        }
      }

      // Now deal with immediate members
      Membership  msM;
      MemberOf    mofM;
      Iterator    iterHas = MembershipFinder.internal_findAllByOwnerAndFieldAndType(
        root, o, f, IMMEDIATE
      ).iterator();
      while (iterHas.hasNext()) {
        msM   = (Membership) iterHas.next();
        mofM  = Membership.internal_delImmediateMembership(s, o, msM.getMember().getSubject(), f);
        deletes.addAll( mofM.internal_getDeletes() );
      }

      // TODO 20070130 bah
      if (o instanceof Group) {
        ( (Group) o ).setSession(orig);
      }
      else {
        ( (Stem) o ).setSession(orig);
      }
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
    // TODO 20070130 bah
    if (o instanceof Group) {
      ( (Group) o ).setSession( orig.getDTO().getRootSession() );
    }
    else {
      ( (Stem) o ).setSession( orig.getDTO().getRootSession() );
    }

    Set deletes = new LinkedHashSet();

    Field     f;
    Iterator  iter  = FieldFinder.findAllByType(type).iterator();
    while (iter.hasNext()) {
      f = (Field) iter.next();
      deletes.addAll( internal_deleteAllField(s, o, f) );
    }

    // TODO 20070130 bah
    if (o instanceof Group) {
      ( (Group) o ).setSession(orig);
    }
    else {
      ( (Stem) o ).setSession(orig);
    }
    return deletes;
  } // protected static Set internal_deleteAllFieldType(s, o, f)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected MembershipDTO getDTO() {
    return (MembershipDTO) super.getDTO();
  } // protected MembershipDTO getDTO()


} // public class Membership extends GrouperAPI

