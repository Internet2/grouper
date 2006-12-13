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
import  java.util.*;
import  net.sf.hibernate.*;

/**
 * Find memberships within the Groups Registry.
 * <p/>
 * @author  blair christensen.
 * @version $Id: MembershipFinder.java,v 1.58 2006-12-13 23:02:41 blair Exp $
 */
public class MembershipFinder {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = MembershipFinder.class.getName();
  
  
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
      Membership  ms  = findMembershipByTypeNoPrivNoSession(g, m, f, MembershipType.C);
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
      Set         effs  = findEffectiveMemberships(g, m, f, via, depth);
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
      Membership  ms  = findMembershipByTypeNoPrivNoSession(g, m, f, MembershipType.I);
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
    Iterator        iter      = findChildMemberships(root, ms).iterator();
    while (iter.hasNext()) {
      child = (Membership) iter.next();
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
    Set         mships        = new LinkedHashSet();
    Iterator    iter          = findAllViaNoSessionNoPriv(ms).iterator();
    Membership  child;
    Set         childEffs;
    Iterator    childIter;
    Membership  eff;  
    Membership  newChild;
    Iterator    newChildIter;
    while (iter.hasNext()) {
      eff = (Membership) iter.next();
      eff.setSession(s);
      childIter = children.iterator();
      while (childIter.hasNext()) {
        child = (Membership) childIter.next();
        child.setSession(s);  
        try {
          childEffs = findEffectiveMemberships(
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

  protected static Set findAllMemberships(GrouperSession s, Member m) {
    /*
     * @filtered  false
     * @session   true
     */
    GrouperSessionValidator.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session   hs    = HibernateHelper.getSession();
      Query     qry   = hs.createQuery(
        "from Membership as ms where ms.member_id = :member"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllMemberships");
      qry.setParameter("member", m);
      Membership  ms;
      Iterator    iter  = qry.iterate();
      while (iter.hasNext()) {
        ms = (Membership) iter.next();
        ms.setSession(s);
        mships.add(ms);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findAllMemberships(s, m)

  // @since 1.0
  protected static Set findAllViaNoSessionNoPriv(Membership ms) {
    // @filtered  false
    // @session   false
    Set via = new LinkedHashSet();
    try {
      Session   hs  = HibernateHelper.getSession();
      Query     qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.via_id     = :via    "
      );
      qry.setCacheable(false);  // Don't cache
      qry.setParameter( "member"  , ms.getMember_id() );
      qry.setParameter( "via"     , ms.getOwner_id()  );
      Membership  eff;
      Iterator    iter  = qry.iterate();
      while (iter.hasNext()) {
        eff = (Membership) iter.next();
        via.add(eff);
      }
      hs.close();
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.MSF_FINDALLVIA + eH.getMessage());
    }
    return via;
  } // protected static Set findAllViaNoSessionNoPriv(ms)

  // @since   1.1.0
  protected static Set findByCreatedAfter(GrouperSession s, Date d, Field f) 
    throws QueryException 
  {
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where      "
        + "     ms.create_time  > :time   "
        + "and  ms.field.name   = :fname  "
        + "and  ms.field.type   = :ftype  "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByCreatedAfter");
      return _findByDate(s, hs, qry, d, f);
    }
    catch (HibernateException eH) {
      throw new QueryException("error finding memberships: " + eH.getMessage(), eH);  
    }
  } // protected static Set findByCreatedAfter(s, d, f)

  // @since   1.1.0
  protected static Set findByCreatedBefore(GrouperSession s, Date d, Field f) 
    throws QueryException 
  {
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where      "
        + "     ms.create_time  < :time   "
        + "and  ms.field.name   = :fname  "
        + "and  ms.field.type   = :ftype  "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByCreatedBefore");
      return _findByDate(s, hs, qry, d, f);
    }
    catch (HibernateException eH) {
      throw new QueryException("error finding memberships: " + eH.getMessage(), eH);  
    }
  } // protected static Set findByCreatedAfter(s, d, f)

  protected static Membership findByUuid(GrouperSession s, String uuid) 
    throws MembershipNotFoundException
  {
    GrouperSessionValidator.validate(s);
    try {
      Membership  ms  = null;
      Session     hs  = HibernateHelper.getSession();
      Query       qry = hs.createQuery(
        "from Membership as ms where ms.uuid = :uuid"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      List mships  = qry.list();
      if (mships.size() == 1) {
        ms = (Membership) mships.get(0);
        ms.setSession(s);
      }
      hs.close();
      if (ms == null) {
        throw new MembershipNotFoundException("membership not found");
      }
      return ms; 
    }
    catch (HibernateException eH) {
      throw new MembershipNotFoundException(
        "error finding membership: " + eH.getMessage(), eH
      );  
    }
  } // protected static Membership findByUuid(s, uuid)

  protected static Set findChildMemberships(GrouperSession s, Membership ms) { 
    /*
     * @filtered  true
     * @session   true
     */
    GrouperSessionValidator.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where ms.parent_membership = :msid"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildMemberships");
      qry.setString("msid", ms.getId());
      List    l   = qry.list();
      hs.close();
      mships.addAll( PrivilegeResolver.canViewMemberships(s, l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findChildMemberships(s, ms)
 
  protected static Set findEffectiveMemberships(
    Owner o, Member m, Field f, Owner via, int depth
  )
    throws MembershipNotFoundException
  {
    /*
     * @filtered  false
     * @session   false
     */
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where      "
        + "     ms.owner_id     = :owner  "
        + "and  ms.member_id    = :member "
        + "and  ms.field.name   = :fname  "
        + "and  ms.field.type   = :ftype  "
        + "and  ms.mship_type   = :type   "
        + "and  ms.via_id       = :via    "
        + "and  ms.depth        = :depth"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindEffectiveMemberships");
      qry.setParameter( "owner"   , o                           );
      qry.setParameter( "member"  , m                           );
      qry.setString(    "fname"   , f.getName()                 );
      qry.setString(    "ftype"   , f.getType().toString()      );
      qry.setString(    "type"    , MembershipType.E.toString() );
      qry.setParameter( "via"     , via                         );
      qry.setInteger(   "depth"   , depth                       );
      mships.addAll(qry.list());
      hs.close();
    }
    catch (HibernateException eH) {
      throw new MembershipNotFoundException(eH.getMessage(), eH);
    }
    return mships;
  } // protected static Set findEffectiveMembership(o, m, field, via, depth)

  protected static Set findEffectiveMemberships(
    GrouperSession s, Member m, Field f
  )
  {
    /*
     * @filtered  true
     * @session   true
     */
    GrouperSessionValidator.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindEffectiveMembershipsMember");
      qry.setParameter( "member", m                           );
      qry.setString(    "fname" , f.getName()                 );
      qry.setString(    "ftype" , f.getType().toString()      );
      qry.setString(    "type"  , MembershipType.E.toString() );
      List    l   = qry.list();
      hs.close();
      mships.addAll( PrivilegeResolver.canViewMemberships(s, l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findEffectiveMemberships(s, m, f)

  protected static Set findEffectiveMemberships(
    Owner o, Member m, Field f
  )
  {
    /*
     * @filtered  false
     * @session   false
     */
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindEffectiveMembershipsOwnerMember");
      qry.setParameter( "owner" , o                           );
      qry.setParameter( "member", m                           );
      qry.setString(    "fname" , f.getName()                 );
      qry.setString(    "ftype" , f.getType().toString()      );
      qry.setString(    "type"  , MembershipType.E.toString() );
      mships.addAll(qry.list());
      hs.close();
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Membership findEffectiveMemberships(o, m, f)

  protected static Set findImmediateMemberships(
    GrouperSession s, Member m, Field f
  )
  {
    /*
     * @filtered  true
     * @session   true
     */
    GrouperSessionValidator.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindImmediateMembershipsMember");      
      qry.setParameter( "member", m                           );
      qry.setString(    "fname" , f.getName()                 );
      qry.setString(    "ftype" , f.getType().toString()      );
      qry.setString(    "type"  , MembershipType.I.toString() );
      List    l   = qry.list();
      hs.close();
      mships.addAll( PrivilegeResolver.canViewMemberships(s, l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findImmediateMemberships(s, m, f)

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
    Iterator    iter  = findMembershipsNoPriv(s, o, f).iterator();
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

  protected static Set findMemberships(GrouperSession s, Member m, Field f) {
     // @filtered  true
     // @session   true
    GrouperSessionValidator.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMemberships");
      qry.setParameter( "member", m                     );
      qry.setString(    "fname" , f.getName()           );
      qry.setString(    "ftype" , f.getType().toString());
      List    l   = qry.list();

      // Don't repeat ourselves
      if ( !m.equals( MemberFinder.findAllMember() ) ) {
        qry.setParameter( "member", MemberFinder.findAllMember() );
        l.addAll( qry.list() );
      }
      hs.close();

      // If the session's member is equivalent to the member that we
      // are searching for, don't filter the results - but still attach
      // session, otherwise a member that has OPTIN, OPTOUT, etc
      // will have those results filtered out.
      // TODO 20061213 the above comment makes no sense to me at the moment
      if (s.getMember().equals(m)) {
        Membership  ms;
        Iterator    iter  = l.iterator();
        while (iter.hasNext()) {
          ms = (Membership) iter.next();
          ms.setSession(s);
          mships.add(ms);
        }
      }
      else {
        mships.addAll( PrivilegeResolver.canViewMemberships(s, l) );
      }
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findMemberships(s, m, f)

  protected static Set findMemberships(GrouperSession s, Owner o, Field f) {
     // @filtered true
     // @session  true
    return new LinkedHashSet( PrivilegeResolver.canViewMemberships( s, findMembershipsNoPriv(s, o, f) ) );
  } // protected static Set findMemberships(s, o, f)

  // @since 1.0.1
  protected static Set findMembershipsNoPriv(GrouperSession s, Owner o, Field f) {
     // @filtered false
     // @session  true
    GrouperSessionValidator.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMembershipsOwner");
      qry.setEntity( "owner" , o ); 
      qry.setString(    "fname"   , f.getName()             );
      qry.setString(    "ftype"   , f.getType().toString()  ); 
      List l = qry.list();
      hs.close();
      mships.addAll( U.setMembershipSessions(s, l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findMembershipsNoPriv(s, o, f)

  // @since 1.0
  protected static Membership findMembershipByTypeNoPrivNoSession(
    Owner o, Member m, Field f, MembershipType type
  )
    throws  MembershipNotFoundException
  {
     // @filtered  false
     // @session   false
    List mships = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMembershipByType");
      qry.setParameter( "owner"   , o                       );
      qry.setParameter( "member"  , m                       );
      qry.setString(    "fname"   , f.getName()             );
      qry.setString(    "ftype"   , f.getType().toString()  ); 
      qry.setString(    "type"    , type.toString()         );
      mships.addAll(qry.list());
      hs.close();
    }
    catch (HibernateException eH) {
      throw new MembershipNotFoundException(eH.getMessage(), eH);
    }
    if (mships.size() == 1) {
      Membership ms = (Membership) mships.get(0);
      return ms;
    }
    throw new MembershipNotFoundException();
  } // protected static Membership findMembershipByTypeNoPrivNoSession(o, m, f, type)

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
    Iterator    iter    = findMembershipsByType(s, g, f, type).iterator();
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
  } // protected static Set findMembersByType(s, g, f, type)

  // @since 1.0
  protected static Set findMembershipsByType(
    GrouperSession s, Owner o, Field f, MembershipType type
  )
  {
     // @filtered  true
     // @session   true
    GrouperSessionValidator.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMembershipsByType");
      qry.setParameter( "owner" , o                       );
      qry.setString(    "fname" , f.getName()             );
      qry.setString(    "ftype" , f.getType().toString()  );
      qry.setString(    "type"  , type.toString()         );
      List    l   = qry.list();
      hs.close();
      mships.addAll( PrivilegeResolver.canViewMemberships(s, l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findMembershipsByType(s, o, f)

  protected static Set findMembershipsNoPrivsNoSession(
    Owner o, Member m, Field f
  )
  {
     // @filtered  false
     // @session   false
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  " 
        + "and  ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMembershipsOwnerMember");
      qry.setParameter( "owner" , o                     );
      qry.setParameter( "member", m                     );
      qry.setString(    "fname" , f.getName()           );
      qry.setString(    "ftype" , f.getType().toString());
      mships.addAll(qry.list());
      hs.close();
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Membership findMembershipsNoPrivsNoSession(o, m, f)


  // PRIVATE CLASS METHODS //

  // @since   1.1.0
  private static Set _findByDate(
    GrouperSession s, Session hs, Query qry, Date d, Field f
  )
    throws  HibernateException
  {
    qry.setLong(    "time"  , d.getTime()             );
    qry.setString(  "fname" , f.getName()             );
    qry.setString(  "ftype" , f.getType().toString()  );
    List        l       = qry.list();
    hs.close();
    Membership  ms;
    Set         mships  = new LinkedHashSet();
    Iterator    it      = l.iterator();
    while (it.hasNext()) {
      ms = (Membership) it.next();
      ms.setSession(s);
      mships.add(ms);
    }
    return mships;
  } // private static Set _findByDate(s, hs, qry, d, f)

} // public class MembershipFinder

