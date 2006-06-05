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
import  java.util.*;
import  net.sf.hibernate.*;
import  net.sf.hibernate.type.*;

/**
 * Find memberships within the Groups Registry.
 * <p />
 * @author  blair christensen.
 * @version $Id: MembershipFinder.java,v 1.33 2006-06-05 19:54:40 blair Exp $
 */
public class MembershipFinder {

  // PUBLIC CLASS METHODS //

  /**
   * Return the effective membership if it exists.
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
    GrouperSession.validate(s);
    Set mships = new LinkedHashSet();
    try {
      Member      m     = MemberFinder.findBySubject(s, subj);
      Set         effs  = findEffectiveMemberships(g, m.getId(), f, via, depth);
      if (effs.size() > 0) {
        try {
          PrivilegeResolver.getInstance().canPrivDispatch(
            s, g, s.getSubject(), f.getReadPriv()
          );
          Iterator effsIter = effs.iterator();
          while (effsIter.hasNext()) {
            Membership eff = (Membership) effsIter.next();
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
    /* 
     * @filtered  true
     * @session   true
     */
    GrouperSession.validate(s);
    try {
      Member      m   = MemberFinder.findBySubject(s, subj);
      Membership  ms  = findImmediateMembership(g, m, f);
      ms.setSession(s);
      PrivilegeResolver.getInstance().canPrivDispatch(
        s, ms.getGroup(), s.getSubject(), f.getReadPriv()
      );
      return ms;
    }
    catch (Exception e) {
      // @exception GroupNotFoundException
      // @exception InsufficientPrivilegeException
      // @exception MemberNotFoundException
      throw new MembershipNotFoundException(e.getMessage(), e);
    }
  } // public static Membership findImmediateMembership(s, g, m, f)



  // PROTECTED CLASS METHODS //

  protected static Set findAllMemberships(GrouperSession s, Member m) {
    /*
     * @filtered  false
     * @session   true
     */
    GrouperSession.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session   hs    = HibernateHelper.getSession();
      Query     qry   = hs.createQuery(
        "from Membership as ms where ms.member_id = :mid"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FAM);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FAM);
      qry.setString("mid", m.getId());
      Iterator  iter  = qry.iterate();
      while (iter.hasNext()) {
        Membership ms = (Membership) iter.next();
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

  // FIXME  Does this even work?
  protected static Membership findByUuid(GrouperSession s, String uuid) 
    throws MembershipNotFoundException
  {
    GrouperSession.validate(s);
    try {
      Membership  ms  = null;
      Session     hs  = HibernateHelper.getSession();
      Query       qry = hs.createQuery(
        "from Membership as ms where ms.uuid = :uuid"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FBU);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FBU);
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
    GrouperSession.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where ms.parent_membership = :msid"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FCM);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FCM);
      qry.setString("msid", ms.getId());
      List    l   = qry.list();
      hs.close();
      mships.addAll( _filterMemberships(s, ms.getList(), l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findChildMemberships(s, ms)
 
  protected static Set findEffectiveMembers(GrouperSession s, Group g, Field f) {
    /*
     * @filtered  true  MembershipFinder.findEffectiveMemberships(s, g, f) 
     * @session   true  MembershipFinder.findEffectiveMemberships(s, g, f)
     */
    GrouperSession.validate(s);
    Set       members = new LinkedHashSet();
    Iterator  iter    = findEffectiveMemberships(s, g, f).iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // protected static Set findEffectiveMembers(s, g, f)

  protected static Set findEffectiveMemberships(
    Owner o, String mid, Field f, Owner via, int depth
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
        + "and  ms.member_id    = :mid    "
        + "and  ms.field.name   = :fname  "
        + "and  ms.field.type   = :ftype  "
        + "and  ms.via_id       = :via    "
        + "and  ms.depth        = :depth"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FEM);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FEM);
      qry.setParameter( "owner"   , o                     );
      qry.setString(    "mid"     , mid                   ); // FIXME
      qry.setString(    "fname"   , f.getName()           );
      qry.setString(    "ftype"   , f.getType().toString());
      qry.setParameter( "via"     , via                   );
      qry.setInteger(   "depth"   , depth                 );
      mships.addAll(qry.list());
      hs.close();
    }
    catch (HibernateException eH) {
      throw new MembershipNotFoundException(eH.getMessage(), eH);
    }
    return mships;
  } // protected static Set findEffectiveMembership(o, mid, field, via, depth)

  protected static Set findEffectiveMemberships(
    GrouperSession s, Group g, Field f
  )
  {
    /*
     * @filtered  true
     * @session   true
     */
    GrouperSession.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.depth      > 0" 
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FEMO);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FEMO);
      qry.setParameter( "owner" , g                     );
      qry.setString(    "fname" , f.getName()           );
      qry.setString(    "ftype" , f.getType().toString());
      List    l     = qry.list();
      hs.close();
      mships.addAll( _filterMemberships(s, f, l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findEffectiveMemberships(s, g, f)

  protected static Set findEffectiveMemberships(
    GrouperSession s, Member m, Field f
  )
  {
    /*
     * @filtered  true
     * @session   true
     */
    GrouperSession.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :mid    "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.depth      > 0"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FEMM);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FEMM);
      qry.setString("mid"   , m.getId()             );
      qry.setString("fname" , f.getName()           );
      qry.setString("ftype" , f.getType().toString());
      List    l   = qry.list();
      hs.close();
      mships.addAll( _filterMemberships(s, f, l) );
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
        + "and  ms.member_id  = :mid    "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.depth      > 0"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FEMOM);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FEMOM);
      qry.setParameter( "owner" , o                     );
      qry.setString(    "mid"   , m.getId()             ); // FIXME
      qry.setString(    "fname" , f.getName()           );
      qry.setString(    "ftype" , f.getType().toString());
      mships.addAll(qry.list());
      hs.close();
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Membership findEffectiveMemberships(o, m, f)

  protected static Set findImmediateMembers(GrouperSession s, Group g, Field f) {
    /*
     * @filtered  true  MembershipFinder.findImmediateMemberships(s, g, f) 
     * @session   true  MembershipFinder.findImmediateMemberships(s, g, f)
     */
    GrouperSession.validate(s);
    Set       members = new LinkedHashSet();
    Iterator  iter    = findImmediateMemberships(s, g, f).iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        members.add(ms.getMember());
      }
      catch (MemberNotFoundException eMNF) {
        // Ignore
      }
    }
    return members;
  } // protected static Set findImmediateMembers(s, g, f)

  protected static Membership findImmediateMembership(Owner o, Member m, Field f)
    throws  MembershipNotFoundException
  {
    /*
     * @filtered  false
     * @session   false
     */
    List mships = new ArrayList();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.member_id  = :mid    "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.depth      = 0"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FIM);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FIM);
      qry.setParameter( "owner" , o                     );
      qry.setString(    "mid"   , m.getId()             ); // FIXME Why not just setParameter()?
      qry.setString(    "fname" , f.getName()           );
      qry.setString(    "ftype" , f.getType().toString()); 
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
  } // protected static Membership findImmediateMembership(o, m, f)

  protected static Set findImmediateMemberships(
    GrouperSession s, Owner o, Field f
  )
  {
    /*
     * @filtered  true
     * @session   true
     */
    GrouperSession.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.depth      = 0"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FIMO);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FIMO);
      qry.setParameter( "owner" , o                     );
      qry.setString(    "fname" , f.getName()           );
      qry.setString(    "ftype" , f.getType().toString());
      List    l   = qry.list();
      hs.close();
      mships.addAll( _filterMemberships(s, f, l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findImmediateMemberships(s, o, f)

  protected static Set findImmediateMemberships(
    GrouperSession s, Member m, Field f
  )
  {
    /*
     * @filtered  true
     * @session   true
     */
    GrouperSession.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :mid    "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.depth      = 0"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FIMM);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FIMM);      
      qry.setString("mid"   , m.getId()             );
      qry.setString("fname" , f.getName()           );
      qry.setString("ftype" , f.getType().toString());
      List    l   = qry.list();
      hs.close();
      mships.addAll( _filterMemberships(s, f, l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findImmediateMemberships(s, m, f)

  protected static Set findMembers(GrouperSession s, Group g, Field f) {
    /*
     * @filtered  true  MembershipFinder.findMemberships(s, g, f) 
     * @session   true  MembershipFinder.findMemberships(s, g, f)
     */
    GrouperSession.validate(s);
    Set       members = new LinkedHashSet();
    Iterator  iter    = findMemberships(s, g, f).iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
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
    /*
     * @filtered  true  MembershipFinder.findMemberships(s, oid, f)
     * @session   true  MembershipFinder.findMemberships(s, oid, f)
     */
    GrouperSession.validate(s);
    Set       subjs = new LinkedHashSet();
    Iterator  iter  = findMemberships(s, o, f).iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      try {
        subjs.add(ms.getMember().getSubject());
      }
      catch (Exception e) {
        // @exception MemberNotFoundException
        // @exception SubjectNotFoundException
        // Ignore
      }
    }
    return subjs;
  } // protected static Set findMembers(s, o, f)

  protected static Set findMemberships(
    GrouperSession s, Member m, Field f
  )
  {
    /*
     * @filtered  true
     * @session   true
     */
    GrouperSession.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :mid    "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FM);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FM);
      qry.setString("mid"   , m.getId()             );
      qry.setString("fname" , f.getName()           );
      qry.setString("ftype" , f.getType().toString());
      List    l   = qry.list();

      Member all = MemberFinder.findAllMember();
      qry.setString("mid", all.getId());
      l.addAll(qry.list());
      hs.close();

      // If the session's member is equivalent to the member that we
      // are searching for, don't filter the results - but still attach
      // session, otherwise a member that has OPTIN, OPTOUT, etc
      // will have those results filtered out.
      if (s.getMember().equals(m)) {
        Iterator iter = l.iterator();
        while (iter.hasNext()) {
          Membership ms = (Membership) iter.next();
          ms.setSession(s);
          mships.add(ms);
        }
      }
      else {
        mships.addAll( _filterMemberships(s, f, l) );
      }
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findMemberships(s, m, f)

  protected static Set findMemberships(GrouperSession s, Owner o, Field f) {
    /*
     * @filtered  true
     * @session   true
     */
    GrouperSession.validate(s);
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateHelper.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype"
      );
      qry.setCacheable(   GrouperConfig.QRY_MSF_FMO );
      qry.setCacheRegion( GrouperConfig.QCR_MSF_FMO );
      qry.setParameter( "owner" , o                     );
      qry.setString(    "fname" , f.getName()           );
      qry.setString(    "ftype" , f.getType().toString());
      List    l   = qry.list();
      hs.close();
      mships.addAll( _filterMemberships(s, f, l) );
    }
    catch (HibernateException eH) {
      ErrorLog.error(MembershipFinder.class, E.HIBERNATE + eH.getMessage());
    }
    return mships;
  } // protected static Set findMemberships(s, o, f)

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
        + "and  ms.member_id  = :mid    "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype"
      );
      qry.setCacheable(GrouperConfig.QRY_MSF_FMOM);
      qry.setCacheRegion(GrouperConfig.QCR_MSF_FMOM);
      qry.setParameter( "owner" , o                     );
      qry.setString(    "mid"   , m.getId()             ); // FIXME
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
  private static Set _filterMemberships(GrouperSession s, Field f, List l) {
    GrouperSession.validate(s);
    Set       mships  = new LinkedHashSet();
    Iterator  iter    = l.iterator();
    while (iter.hasNext()) {
      Membership ms = (Membership) iter.next();
      ms.setSession(s);
      try {
        if (f.getType().equals(FieldType.NAMING)) {
          PrivilegeResolver.getInstance().canPrivDispatch(
            s, ms.getStem(), s.getSubject(), f.getReadPriv()
          );
        }
        else {
          PrivilegeResolver.getInstance().canPrivDispatch(
            s, ms.getGroup(), s.getSubject(), f.getReadPriv()
          );
        }
        mships.add(ms);
      }
      catch (Exception e) {
        // @exception GroupNotFoundException
        // @exception InsufficientPrivilegeException
        // @exception SchemaException
        // @exception StemNotFoundException
        // ignore
      }
    }
    return mships;
  } // private static Set _filterMemberships(s, f, l)

}

