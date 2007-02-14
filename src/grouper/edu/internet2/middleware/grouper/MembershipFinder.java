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
 * Find memberships within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipFinder.java,v 1.75 2007-02-14 17:34:14 blair Exp $
 */
public class MembershipFinder {
  
  // PUBLIC CLASS METHODS //

  /**
   * Return the composite membership if it exists.
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Composite membership has this group.
   * @param   subj  Composite membership has this subject.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   * @since   1.0
   */
  public static Membership findCompositeMembership(
    GrouperSession s, Group g, Subject subj
  )
    throws  MembershipNotFoundException,
            SchemaException
  {
     // @filtered  true
     // @session   true
    GrouperSessionValidator.internal_validate(s);
    try {
      Field       f   = Group.getDefaultList();
      Member      m   = MemberFinder.findBySubject(s, subj);
      Membership  ms  = new Membership();
      ms.setDTO( HibernateMembershipDAO.findByOwnerAndMemberAndFieldAndType( g.getUuid(), m.getUuid(), f, Membership.COMPOSITE ) );
      ms.setSession(s);
      PrivilegeResolver.internal_canPrivDispatch(
        s, ms.getGroup(), s.getSubject(), f.getReadPriv()
      );
      return ms;
    }
    catch (GroupNotFoundException eGNF)         {
      throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MembershipNotFoundException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MembershipNotFoundException(eMNF.getMessage(), eMNF);
    }
  } // public static Membership findCompositeMembership(s, g, m)

  /**
   * Return effective memberships.
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Effective membership has this group.
   * @param   subj  Effective membership has this subject.
   * @param   f     Effective membership has this list.
   * @param   via   Effective membership has this via group.
   * @param   depth Effective membership has this depth.
   * @return  A set of {@link Membership} objects.
   * @throws  MembershipNotFoundException
   * @throws  SchemaException
   */
  public static Set findEffectiveMemberships(
    GrouperSession s, Group g, Subject subj, Field f, Group via, int depth
  )
    throws  MembershipNotFoundException,
            SchemaException
  {
     // @filtered  true
     // @session   true
    GrouperSessionValidator.internal_validate(s);
    Set mships = new LinkedHashSet();
    try {
      Member m = MemberFinder.findBySubject(s, subj);
      try {
        PrivilegeResolver.internal_canPrivDispatch( s, g, s.getSubject(), f.getReadPriv() );
        Iterator  it    = HibernateMembershipDAO.findAllEffective(
          g.getUuid(), m.getUuid(), f, via.getUuid(), depth
        ).iterator();
        Membership eff;
        while (it.hasNext()) {
          eff = new Membership();
          eff.setDTO( (MembershipDTO) it.next() );
          eff.setSession(s);
          mships.add(eff);
        }
      }
      catch (InsufficientPrivilegeException eIP) {
        // ??? ignore
      }
    }
    catch (MemberNotFoundException eMNF) {
      throw new MembershipNotFoundException(eMNF.getMessage(), eMNF);
    }
    return mships;
  } // public static Membership findEffectiveMembership(s, g, subj, f, via, depth)

  /**
   * Return the immediate membership if it exists.
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
   * @param   f     Immediate membership has this list.
   * @return  A {@link Membership} object
   * @throws  MembershipNotFoundException 
   * @throws  SchemaException
   */
  public static Membership findImmediateMembership(
    GrouperSession s, Group g, Subject subj, Field f
  )
    throws  MembershipNotFoundException,
            SchemaException
  {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.internal_validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj);
      Membership  ms  = new Membership();
      ms.setDTO( HibernateMembershipDAO.findByOwnerAndMemberAndFieldAndType( g.getUuid(), m.getUuid(), f, Membership.IMMEDIATE ) );
      ms.setSession(s);
      PrivilegeResolver.internal_canPrivDispatch(
        s, ms.getGroup(), s.getSubject(), f.getReadPriv()
      );
      return ms;
    }
    catch (GroupNotFoundException eGNF)         {
      throw new MembershipNotFoundException(eGNF.getMessage(), eGNF);
    }
    catch (InsufficientPrivilegeException eIP)  {
      throw new MembershipNotFoundException(eIP.getMessage(), eIP);
    }
    catch (MemberNotFoundException eMNF)        {
      throw new MembershipNotFoundException(eMNF.getMessage(), eMNF);
    }
  } // public static Membership findImmediateMembership(s, g, m, f)



  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set internal_findAllChildrenNoPriv(MembershipDTO dto) {
    Set           children  = new LinkedHashSet();
    MembershipDTO child;
    Iterator      it        = HibernateMembershipDAO.findAllChildMemberships(dto).iterator();
    while (it.hasNext()) {
      child = (MembershipDTO) it.next();
      children.addAll( internal_findAllChildrenNoPriv(child) );
      children.add(child);
    }
    return children;
  } // protected static Set internal_findAllChildrenNoPriv(dto)

  // TODO 20061019 Smaller!  Also: What does this even do?  I can't even remember.
  // @since   1.2.0
  protected static Set internal_findAllForwardMembershipsNoPriv(
    GrouperSession s, MembershipDTO dto, Set children
  )  
    throws  SchemaException 
  {
    // @filtered  false
    // @session   false
    Set         mships      = new LinkedHashSet();
    Iterator    it          = HibernateMembershipDAO.findAllByMemberAndVia( dto.getMemberUuid(), dto.getOwnerUuid() ).iterator();
    Iterator    childIter;
    Iterator    newChildIter;
    while (it.hasNext()) {
      MembershipDTO eff = (MembershipDTO) it.next();
      childIter = children.iterator();
      while (childIter.hasNext()) {
        MembershipDTO child = (MembershipDTO) childIter.next();
        newChildIter        = HibernateMembershipDAO.findAllEffective(
          eff.getOwnerUuid(), child.getMemberUuid(), FieldFinder.find( eff.getListName() ), 
          child.getViaUuid(), eff.getDepth() + child.getDepth()
        ).iterator();
        while (newChildIter.hasNext()) {
          MembershipDTO newChild = (MembershipDTO) newChildIter.next();
          mships.add(newChild);
        }
      }
      mships.add(eff);
    }
    return mships;
  } // protected static Set internal_findAllForwardMembershipsNoPriv(s, dto, children)

  // @since   1.2.0
  protected static Set internal_findMembers(GrouperSession s, Group g, Field f) {
     // @filtered  true  MembershipFinder.findMemberships(s, g, f) 
     // @session   true  MembershipFinder.findMemberships(s, g, f)
    GrouperSessionValidator.internal_validate(s);
    Set         members = new LinkedHashSet();
    Membership  ms;
    Iterator    iter    = internal_findMemberships(s, g, f).iterator();
    while (iter.hasNext()) {
      ms = (Membership) iter.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // protected static Set internal_findMembers(s, g, f)

  // @since   1.2.0
  protected static Set internal_findSubjects(GrouperSession s, Owner o, Field f) {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.internal_validate(s);
    Set         subjs = new LinkedHashSet();
    Membership  ms;
    Iterator    iter  = internal_findMemberships(s, o, f).iterator();
    while (iter.hasNext()) {
      ms = (Membership) iter.next();
      try {
        subjs.add(ms.getMember().getSubject());
      }
      catch (Exception e) {
        // @exception MemberNotFoundException
        // @exception SubjectNotFoundException
        ErrorLog.error(MembershipFinder.class, E.MSF_FINDSUBJECTS + e.getMessage());
      }
    }
    return subjs;
  } // protected static Set internal_findSubjects(s, o, f)

  // @since   1.2.0
  protected static Set internal_findSubjectsNoPriv(GrouperSession s, Owner o, Field f) {
     // @filtered  false
     // @session   true 
    GrouperSessionValidator.internal_validate(s);
    Set           subjs = new LinkedHashSet();
    MembershipDTO ms;
    MemberDTO     m;
    Iterator      it    = HibernateMembershipDAO.findAllByOwnerAndField( o.getUuid(), f ).iterator();
    while (it.hasNext()) {
      ms = (MembershipDTO) it.next();
      try {
        m = HibernateMemberDAO.findByUuid( ms.getMemberUuid() );
        subjs.add( SubjectFinder.findById( m.getSubjectId(), m.getSubjectTypeId(), m.getSubjectSourceId() ) );
      }
      catch (Exception e) {
        // @exception MemberNotFoundException
        // @exception SubjectNotFoundException
        ErrorLog.error(MembershipFinder.class, E.MSF_FINDSUBJECTS + e.getMessage());
      }
    }
    return subjs;
  } // protected static Set internal_findSubjectsNoPriv(s, o, f)

  // @since   1.2.0
  // TODO 20070124 kill!
  protected static Set internal_findMemberships(GrouperSession s, Owner o, Field f) {
     // @filtered true
     // @session  true
    return new LinkedHashSet( 
      PrivilegeResolver.internal_canViewMemberships( 
        s, HibernateMembershipDAO.findAllByOwnerAndField( o.getUuid(), f )
      )
    );
  } // protected static Set internal_findMemberships(s, o, f)

  // @since   1.2.0
  protected static Set internal_findMembersByType(GrouperSession s, Group g, Field f, String type) {
     // @filtered  true  MembershipFinder.findMembershipsByType(s, o, f) 
     // @session   true  MembershipFinder.findMembershipsByType(s, o, f)
    GrouperSessionValidator.internal_validate(s);
    Set         members = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = internal_findAllByOwnerAndFieldAndType(s, g, f, type).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // protected static Set internal_findMembersByType(s, g, f, type)

  // @since   1.2.0
  protected static Set internal_findAllByCreatedAfter(GrouperSession s, Date d, Field f) 
    throws QueryException 
  {
    // @filtered  false
    // @session   true
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = HibernateMembershipDAO.findAllByCreatedAfter(d, f).iterator();
    while (it.hasNext()) {
      ms = new Membership();
      ms.setDTO( (MembershipDTO) it.next() );
      ms.setSession(s);
      mships.add(ms);
    }
    return mships;
  } // protected static Set internal_findAllByCreatedAfter(s, d, f)

  // @since   1.2.0
  protected static Set internal_findAllByCreatedBefore(GrouperSession s, Date d, Field f) 
    throws QueryException 
  {
    // @filtered  false
    // @session   true
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = HibernateMembershipDAO.findAllByCreatedBefore(d, f).iterator();
    while (it.hasNext()) {
      ms = new Membership();
      ms.setDTO( (MembershipDTO) it.next() );
      ms.setSession(s);
      mships.add(ms);
    }
    return mships;
  } // protected static Set internal_findAllByCreatedBefore(s, d, f)

  // @since   1.2.0
  protected static Set internal_findAllByOwnerAndFieldAndType(GrouperSession s, Owner o, Field f, String type) {
     // @filtered  true
     // @session   true
    GrouperSessionValidator.internal_validate(s);
    return PrivilegeResolver.internal_canViewMemberships(
      s, HibernateMembershipDAO.findAllByOwnerAndFieldAndType(o.getUuid(), f, type)
    );
  } // protected static Set internal_findAllByOwnerAndFieldAndType(s, o, f, type)

  // @since   1.2.0
  protected static Set internal_findAllEffectiveByMemberAndField(
    GrouperSession s, Member m, Field f
  ) 
  {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.internal_validate(s);
    return PrivilegeResolver.internal_canViewMemberships( 
      s, HibernateMembershipDAO.findAllEffectiveByMemberAndField( m.getUuid(), f ) 
    );
  } // protected static Set internal_findAllEffectiveByMemberAndField(s, m, f)

  // @since   1.2.0
  protected static Set internal_findAllEffectiveByOwnerAndMemberAndField(
    Owner o, Member m, Field f
  ) 
  {
    // @filtered  false
    // @session   false
    return HibernateMembershipDAO.findAllEffectiveByOwnerAndMemberAndField( o.getUuid(), m.getUuid(), f );
  } // protected static Set internal_findAllEffectiveByOwnerAndMemberAndField(o, m, f)

  // @since   1.2.0
  protected static Set internal_findAllImmediateByMemberAndField(GrouperSession s, Member m, Field f) {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.internal_validate(s);
    return PrivilegeResolver.internal_canViewMemberships( 
      s, HibernateMembershipDAO.findAllImmediateByMemberAndField( m.getUuid(), f ) 
    );
  } // protected static Set internal_findAllImmediateByMemberAndField(s, m, f)

  // @since   1.2.0
  protected static Set internal_findMemberships(GrouperSession s, Member m, Field f) {
     // @filtered  true
     // @session   true
    GrouperSessionValidator.internal_validate(s);
    Set mships = new LinkedHashSet();
    mships.addAll( HibernateMembershipDAO.findMemberships( m.getUuid(), f ) );
    if ( !m.equals( MemberFinder.internal_findAllMember() ) ) {
      mships.addAll( HibernateMembershipDAO.findMemberships( MemberFinder.internal_findAllMember().getUuid(), f ) );
    }
    return PrivilegeResolver.internal_canViewMemberships(s, mships);
  } // protected static Set internal_findMemberships(s, m, f)

} // public class MembershipFinder

