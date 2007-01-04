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
 * @version $Id: MembershipFinder.java,v 1.64 2007-01-04 17:17:45 blair Exp $
 */
public class MembershipFinder {
  
  // PUBLIC CLASS METHODS //

  /**
   * Return the composite membership if it exists.
   * <p/>
   * <pre class="eg">
   * </pre>
   * @param   s     Get membership within this session context.
   * @param   g     Immediate membership has this group.
   * @param   subj  Immediate membership has this subject.
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
    GrouperSessionValidator.validate(s);
    try {
      Field       f   = Group.getDefaultList();
      Member      m   = MemberFinder.findBySubject(s, subj);
      Membership  ms  = internal_findByOwnerAndMemberAndFieldAndType(g, m, f, MembershipType.C);
      ms.setSession(s);
      PrivilegeResolver.canPrivDispatch(
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
    /* 
     * @filtered  true
     * @session   true
     */
    GrouperSessionValidator.validate(s);
    Set mships = new LinkedHashSet();
    try {
      Member      m     = MemberFinder.findBySubject(s, subj);
      Set         effs  = internal_findAllEffective(g, m, f, via, depth);
      if (effs.size() > 0) {
        try {
          PrivilegeResolver.canPrivDispatch(
            s, g, s.getSubject(), f.getReadPriv()
          );
          Membership  eff;
          Iterator    effsIter  = effs.iterator();
          while (effsIter.hasNext()) {
            eff = (Membership) effsIter.next();
            eff.setSession(s);
            mships.add(eff);
          }
        }
        catch (InsufficientPrivilegeException eIP) {
          // ignore
        }
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
    GrouperSessionValidator.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj);
      Membership  ms  = internal_findByOwnerAndMemberAndFieldAndType(g, m, f, MembershipType.I);
      ms.setSession(s);
      PrivilegeResolver.canPrivDispatch(
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

  // @since 1.1.0
  protected static Set findAllChildrenNoPriv(Membership ms) {
    Set             children  = new LinkedHashSet();
    GrouperSession  root      = ms.getSession().getRootSession();
    Membership      child;
    Iterator        it        = internal_findChildMemberships(root, ms).iterator();
    while (it.hasNext()) {
      child = (Membership) it.next();
      children.addAll(findAllChildrenNoPriv(child));
      children.add(child);
    }
    return children;
  } // protected static Set findAllChildrenNoPriv(ms)

  // @since 1.0
  // TODO 20061019 Smaller!  Also: What does this even do?  I can't even remember.
  protected static Set findAllForwardMembershipsNoPriv(
    GrouperSession s, Membership ms, Set children
  ) {
    // @filtered  false
    // @session   true
    Set         mships      = new LinkedHashSet();
    Iterator    it          = HibernateMembershipDAO.findAllByMemberAndVia( 
      ms.getMember_id(), ms.getOwner_id() 
    ).iterator();
    Membership  child;
    Set         childEffs;
    Iterator    childIter;
    Membership  eff;  
    Membership  newChild;
    Iterator    newChildIter;
    while (it.hasNext()) {
      eff = (Membership) it.next();
      eff.setSession(s);
      childIter = children.iterator();
      while (childIter.hasNext()) {
        child = (Membership) childIter.next();
        child.setSession(s);  
        try {
          childEffs = internal_findAllEffective(
            eff.getOwner_id(), child.getMember_id(), eff.getField(), child.getVia_id(), 
            eff.getDepth() + child.getDepth()
          );
          newChildIter = childEffs.iterator();
          while (newChildIter.hasNext()) {
            newChild = (Membership) newChildIter.next();
            newChild.setSession(s);
            mships.add(newChild);
          }
        }
        catch (MembershipNotFoundException eMSNF) {
        }
      }
      mships.add(eff);
    }
    return mships;
  } // protected static Set findAllForwardMembershipsNoPriv(s, ms, children)

  protected static Set findMembers(GrouperSession s, Group g, Field f) {
     // @filtered  true  MembershipFinder.findMemberships(s, g, f) 
     // @session   true  MembershipFinder.findMemberships(s, g, f)
    GrouperSessionValidator.validate(s);
    Set         members = new LinkedHashSet();
    Membership  ms;
    Iterator    iter    = findMemberships(s, g, f).iterator();
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
  } // protected static Set findMembers(s, g, f)

  protected static Set findSubjects(GrouperSession s, Owner o, Field f) {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.validate(s);
    Set         subjs = new LinkedHashSet();
    Membership  ms;
    Iterator    iter  = findMemberships(s, o, f).iterator();
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
  } // protected static Set findSubjects(s, o, f)

  // @since 1.0.1 
  protected static Set findSubjectsNoPriv(GrouperSession s, Owner o, Field f) {
     // @filtered  false
     // @session   true 
    GrouperSessionValidator.validate(s);
    Set         subjs = new LinkedHashSet();
    Membership  ms;
    Iterator    iter  = internal_findAllByOwnerAndField(s, o, f).iterator();
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
  } // protected static Set findSubjectsNoPriv(s, o, f)

  protected static Set findMemberships(GrouperSession s, Owner o, Field f) {
     // @filtered true
     // @session  true
    return new LinkedHashSet( 
      PrivilegeResolver.canViewMemberships( s, internal_findAllByOwnerAndField(s, o, f) ) 
    );
  } // protected static Set findMemberships(s, o, f)

  // @since 1.0
  protected static Set findMembersByType(
    GrouperSession s, Group g, Field f, MembershipType type
  ) 
  {
     // @filtered  true  MembershipFinder.findMembershipsByType(s, o, f) 
     // @session   true  MembershipFinder.findMembershipsByType(s, o, f)
    GrouperSessionValidator.validate(s);
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
  } // protected static Set findMembersByType(s, g, f, type)

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
      ms = (Membership) it.next();
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
      ms = (Membership) it.next();
      ms.setSession(s);
      mships.add(ms);
    }
    return mships;
  } // protected static Set internal_findAllByCreatedBefore(s, d, f)

  // @since   1.2.0
  protected static Set internal_findAllByMember(GrouperSession s, Member m) {
     // @filtered  false
     // @session   true
    GrouperSessionValidator.validate(s);
    return PrivilegeResolver.canViewMemberships( s, HibernateMembershipDAO.findAllByMember(m) );
  } // protected static Set internal_findAllByMember(s, m)

  // @since   1.2.0
  protected static Set internal_findAllByOwnerAndField(GrouperSession s, Owner o, Field f) {
     // @filtered false
     // @session  true
    GrouperSessionValidator.validate(s);
    Set         mships  = new LinkedHashSet();
    Membership  ms;
    Iterator    it      = HibernateMembershipDAO.findAllByOwnerAndField(o, f).iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      ms.setSession(s);
      mships.add(ms);
    }
    return mships;
  } // protected static Set internal_findAllByOwnerAndField(s, o, f)

  // @since   1.2.0
  protected static Set internal_findAllByOwnerAndFieldAndType(
    GrouperSession s, Owner o, Field f, MembershipType type
  )
  {
     // @filtered  true
     // @session   true
    GrouperSessionValidator.validate(s);
    return PrivilegeResolver.canViewMemberships(
      s, HibernateMembershipDAO.findAllByOwnerAndFieldAndType(o, f, type)
    );
  } // protected static Set internal_findAllByOwnerAndFieldAndType(s, o, f, type)

  // @since   1.2.0
  protected static Set internal_findAllByOwnerAndMemberAndField(Owner o, Member m, Field f) {
    // @filtered  false
    // @session   false
    return HibernateMembershipDAO.findAllByOwnerAndMemberAndField(o, m, f);
  } // protected static Set internal_findAllByOwnerAndMemberAndField(o, m, f)

  // @since   1.2.0
  protected static Set internal_findAllEffective(
    Owner o, Member m, Field f, Owner via, int depth
  )
    throws MembershipNotFoundException
  {
     // @filtered  false
     // @session   false
    return HibernateMembershipDAO.findAllEffective(o, m, f, via, depth);
  } // protected static Set internal_findAllEffective(o, m, field, via, depth)

  // @since   1.2.0
  protected static Set internal_findAllEffectiveByMemberAndField(
    GrouperSession s, Member m, Field f
  ) 
  {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.validate(s);
    return PrivilegeResolver.canViewMemberships( 
      s, HibernateMembershipDAO.findAllEffectiveByMemberAndField(m, f) 
    );
  } // protected static Set internal_findAllEffectiveByMemberAndField(s, m, f)

  // @since   1.2.0
  protected static Set internal_findAllEffectiveByOwnerAndMemberAndField(
    Owner o, Member m, Field f
  ) 
  {
    // @filtered  false
    // @session   false
    return HibernateMembershipDAO.findAllEffectiveByOwnerAndMemberAndField(o, m, f);
  } // protected static Set internal_findAllEffectiveByOwnerAndMemberAndField(o, m, f)

  // @since   1.2.0
  protected static Set internal_findAllImmediateByMemberAndField(GrouperSession s, Member m, Field f) {
    // @filtered  true
    // @session   true
    GrouperSessionValidator.validate(s);
    return PrivilegeResolver.canViewMemberships( 
      s, HibernateMembershipDAO.findAllImmediateByMemberAndField(m, f) 
    );
  } // protected static Set internal_findAllImmediateByMemberAndField(s, m, f)

  // @since   1.2.0
  protected static Membership internal_findByOwnerAndMemberAndFieldAndType(
    Owner o, Member m, Field f, MembershipType type
  )
    throws  MembershipNotFoundException
  {
    // @filtered  false
    // @session   false
    return HibernateMembershipDAO.findByOwnerAndMemberAndFieldAndType(o, m, f, type);
  } // protected static Membership internal_findByOwnerAndMemberAndFieldAndType(o, m, f, type)

  // @since   1.2.0
  protected static Membership internal_findByUuid(GrouperSession s, String uuid) 
    throws MembershipNotFoundException
  {
    // @filtered  false
    // @session   true
    GrouperSessionValidator.validate(s);
    Membership ms = HibernateMembershipDAO.findByUuid(uuid);
    ms.setSession(s);
    return ms;
  } // protected static Membership internal_findByUuid(s, uuid)

  // @since   1.2.0
  protected static Set internal_findChildMemberships(GrouperSession s, Membership ms) { 
     // @filtered  true
     // @session   true
    GrouperSessionValidator.validate(s);
    return PrivilegeResolver.canViewMemberships( s, HibernateMembershipDAO.findChildMemberships(ms) );
  } // protected static Set internal_findChildMemberships(s, ms)
 
  // @since   1.2.0
  protected static Set internal_findMemberships(GrouperSession s, Member m, Field f) {
     // @filtered  true
     // @session   true
    GrouperSessionValidator.validate(s);
    Set mships = new LinkedHashSet();
    mships.addAll( HibernateMembershipDAO.findMemberships(m, f) );
    if ( !m.equals( MemberFinder.findAllMember() ) ) {
      mships.addAll( HibernateMembershipDAO.findMemberships( MemberFinder.findAllMember(), f ) );
    }
    return PrivilegeResolver.canViewMemberships(s, mships);
  } // protected static Set internal_findMemberships(s, m, f)

} // public class MembershipFinder

