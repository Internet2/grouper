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
 * @version $Id: Membership.java,v 1.80 2007-04-05 14:28:28 blair Exp $
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
      this.getSession(), GrouperDAOFactory.getFactory().getMembership().findAllChildMemberships( this.getDTO() )
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
      m.setDTO( GrouperDAOFactory.getFactory().getMember().findByUuid( this.getDTO().getCreatorUuid() ) );
      m.setSession( this.getSession() );
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
    g.setDTO( GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid) );
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
    Member m = new Member();
    m.setDTO( GrouperDAOFactory.getFactory().getMember().findByUuid(uuid) );
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
    parent.setDTO( GrouperDAOFactory.getFactory().getMembership().findByUuid(uuid) );
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
    ns.setDTO( GrouperDAOFactory.getFactory().getStem().findByUuid(uuid) );
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
    return this.getDTO().getUuid();
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
    via.setDTO( GrouperDAOFactory.getFactory().getComposite().findByUuid(uuid) );
    via.setSession( this.getSession() );
    return via;
  } // public Composite getViaComposite()

  /**
   * Get this membership's via group.
   * <p>{@link Group}s with {@link Composite} memberships will <b>not</b> have a
   * via group.  Use the {@link #getViaComposite() getViaComposite()} method instead.</p>
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
    via.setDTO( GrouperDAOFactory.getFactory().getGroup().findByUuid(uuid) );
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

  // @since   1.2.0
  protected static void internal_addImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f
  )
    throws  MemberAddException
  {
    try {
      GrouperSession.validate(s);
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj);
      MemberOf  mof = new MemberOf();
      mof.addImmediate( s, g, f, m.getDTO() );
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      EL.addEffMembers( s, g, subj, f, mof.internal_getEffSaves() );
    }
    catch (IllegalStateException eIS)           {
      throw new MemberAddException( eIS.getMessage(), eIS );
    }    
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberAddException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberAddException( eMNF.getMessage(), eMNF );
    }
  } // protected static void internal_addImmediateMembership(s, g, subj, f)

  // @since   1.2.0
  protected static void internal_addImmediateMembership(
    GrouperSession s, Stem ns, Subject subj, Field f
  )
    throws  MemberAddException
  {
    try {
      GrouperSession.validate(s);
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj);
      MemberOf  mof = new MemberOf();
      mof.addImmediate( s, ns, f, m.getDTO() );
      GrouperDAOFactory.getFactory().getMembership().update(mof);
      EL.addEffMembers( s, ns, subj, f, mof.internal_getEffSaves() );
    }
    catch (IllegalStateException eIS)           {
      throw new MemberAddException( eIS.getMessage(), eIS );
    }    
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberAddException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberAddException( eMNF.getMessage(), eMNF );
    }
  } // protected static void internal_addImmediateMembership(s, ns, subj, f)

  // @since   1.2.0
  protected static MemberOf internal_delImmediateMembership(GrouperSession s, Group g, Subject subj, Field f)
    throws  MemberDeleteException
  {
    try {
      GrouperSession.validate(s); 
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj);
      MemberOf  mof = new MemberOf();
      mof.deleteImmediate(
        s, g, 
        GrouperDAOFactory.getFactory().getMembership().findByOwnerAndMemberAndFieldAndType( 
          g.getUuid(), m.getUuid(), f, IMMEDIATE
        ), 
        m.getDTO()
      );
      return mof;
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberDeleteException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberDeleteException( eMNF.getMessage(), eMNF );
    }
    catch (MembershipNotFoundException eMSNF)   {
      throw new MemberDeleteException(eMSNF.getMessage(), eMSNF);
    }
  } // protected static void internal_delImmediateMembership(s, g, subj, f)

  // @since   1.2.0
  protected static MemberOf internal_delImmediateMembership(GrouperSession s, Stem ns, Subject subj, Field f)
    throws  MemberDeleteException
  {
    try {
      GrouperSession.validate(s); 
      // Who we're deleting
      //Member m = PrivilegeResolver.internal_canViewSubject(s, subj);
      Member    m   = MemberFinder.internal_findViewableMemberBySubject(s, subj);
      MemberOf  mof = new MemberOf();
      mof.deleteImmediate(
        s, ns,
        GrouperDAOFactory.getFactory().getMembership().findByOwnerAndMemberAndFieldAndType( 
          ns.getUuid(), m.getUuid(), f, IMMEDIATE 
        ), 
        m.getDTO()
      );
      return mof;
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MemberDeleteException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MemberDeleteException( eMNF.getMessage(), eMNF );
    }
    catch (MembershipNotFoundException eMSNF)   {
      throw new MemberDeleteException(eMSNF.getMessage(), eMSNF);
    }
  } // protected static void internal_delImmediateMembership(s, ns, subj, f)

  // @since   1.2.0
  protected static Set internal_deleteAllField(GrouperSession s, Group g, Field f)
    throws  MemberDeleteException,
            SchemaException
  {
    try {
      GrouperSession.validate(s);

      Set           deletes = new LinkedHashSet();
      MemberOf      mof;
      Membership    ms;
      MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();

      // Deal with where group is a member
      Iterator itIs = g.toMember().getImmediateMemberships(f).iterator();
      while (itIs.hasNext()) {
        ms   = (Membership) itIs.next();
        ms.setSession(s);
        mof  = new MemberOf();
        mof.deleteImmediate(
          s, ms.getGroup(),
          dao.findByOwnerAndMemberAndFieldAndType( 
            ms.getGroup().getUuid(), ms.getMember().getUuid(), ms.getList(), IMMEDIATE
          ),
          ms.getMember().getDTO()
        );
        deletes.addAll( mof.internal_getDeletes() );
      }

      // Deal with group's members
      Iterator itHas = dao.findAllByOwnerAndFieldAndType( g.getUuid(), f, IMMEDIATE ).iterator();
      while (itHas.hasNext()) {
        ms = new Membership();
        ms.setSession(s);
        ms.setDTO( (MembershipDTO) itHas.next() );
        mof = new MemberOf();
        mof.deleteImmediate(
          s, g,
          dao.findByOwnerAndMemberAndFieldAndType(
            g.getUuid(), ms.getMember().getUuid(), ms.getList(), IMMEDIATE
          ),
          ms.getMember().getDTO()
        );
        deletes.addAll( mof.internal_getDeletes() );
      }

      return deletes;
    }
    catch (GroupNotFoundException eGNF) {
      throw new MemberDeleteException( eGNF.getMessage(), eGNF );
    }
    catch (MemberNotFoundException eMNF) {
      throw new MemberDeleteException(eMNF);
    }
    catch (MembershipNotFoundException eMSNF) {
      throw new MemberDeleteException( eMSNF.getMessage(), eMSNF );
    }
  } // protected static Set internal_deleteAllField(s, g, f)

  // @since   1.2.0
  protected static Set internal_deleteAllField(GrouperSession s, Stem ns, Field f)
    throws  MemberDeleteException,
            SchemaException
  {
    try {
      GrouperSession.validate(s);

      Set           deletes = new LinkedHashSet();
      MemberOf      mof;
      Membership    ms;
      MembershipDAO dao     = GrouperDAOFactory.getFactory().getMembership();

      // Deal with stem's members
      Iterator itHas = dao.findAllByOwnerAndFieldAndType( ns.getUuid(), f, IMMEDIATE ).iterator();
      while (itHas.hasNext()) {
        ms = new Membership();
        ms.setSession(s);
        ms.setDTO( (MembershipDTO) itHas.next() );
        mof = new MemberOf();
        mof.deleteImmediate(
          s, ns,
          dao.findByOwnerAndMemberAndFieldAndType(
            ns.getUuid(), ms.getMember().getUuid(), ms.getList(), IMMEDIATE
          ),
          ms.getMember().getDTO()
        );
        deletes.addAll( mof.internal_getDeletes() );
      }

      return deletes;
    }
    catch (MemberNotFoundException eMNF) {
      throw new MemberDeleteException( eMNF.getMessage(), eMNF );
    }
    catch (MembershipNotFoundException eMSNF) {
      throw new MemberDeleteException( eMSNF.getMessage(), eMSNF );
    }
  } // protected static Set internal_deleteAllField(s, ns, f)

  // @since   1.2.0
  protected static Set internal_deleteAllFieldType(GrouperSession s, Group g, FieldType type) 
    throws  MemberDeleteException,
            SchemaException
  {
    GrouperSession.validate(s);
    Set       deletes = new LinkedHashSet();
    Field     f;
    Iterator  it      = FieldFinder.findAllByType(type).iterator();
    while (it.hasNext()) {
      f = (Field) it.next();
      deletes.addAll( internal_deleteAllField(s, g, f) );
    }
    return deletes;
  } // protected static Set internal_deleteAllFieldType(s, g, f)

  // @since   1.2.0
  protected static Set internal_deleteAllFieldType(GrouperSession s, Stem ns, FieldType type) 
    throws  MemberDeleteException,
            SchemaException
  {
    GrouperSession.validate(s);
    Set       deletes = new LinkedHashSet();
    Field     f;
    Iterator  it      = FieldFinder.findAllByType(type).iterator();
    while (it.hasNext()) {
      f = (Field) it.next();
      deletes.addAll( internal_deleteAllField(s, ns, f) );
    }
    return deletes;
  } // protected static Set internal_deleteAllFieldType(s, ns, f)


  // PROTECTED INSTANCE METHODS //

  // @since   1.2.0
  protected MembershipDTO getDTO() {
    return (MembershipDTO) super.getDTO();
  } // protected MembershipDTO getDTO()


} // public class Membership extends GrouperAPI

