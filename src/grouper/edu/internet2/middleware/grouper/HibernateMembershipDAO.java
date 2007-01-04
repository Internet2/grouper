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
import  java.util.Date;
import  java.util.Iterator;
import  java.util.LinkedHashSet;
import  java.util.Set;
import  net.sf.hibernate.*;

/**
 * Stub Hibernate {@link Membership} DAO.
 * <p/>
 * @author  blair christensen.
 * @version $Id: HibernateMembershipDAO.java,v 1.8 2007-01-04 17:50:51 blair Exp $
 * @since   1.2.0
 */
class HibernateMembershipDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateMembershipDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Membership find(String id) 
    throws  MembershipNotFoundException
  {
    try {
      Session     hs = HibernateDAO.getSession();
      Membership  ms = (Membership) hs.load(Membership.class, id);
      hs.close();
      return ms;
    }
    catch (HibernateException eH) {
      throw new MembershipNotFoundException( eH.getMessage(), eH );
    }
  } // protected static Membership find(id)

  // @since   1.2.0
  protected static Set findAllByCreatedAfter(Date d, Field f) {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where      "
        + "     ms.create_time  > :time   "
        + "and  ms.field.name   = :fname  "
        + "and  ms.field.type   = :ftype  "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong(   "time",  d.getTime()            );
      qry.setString( "fname", f.getName()            );
      qry.setString( "ftype", f.getType().toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllByCreatedAfter(d, f)

  // @since   1.2.0
  protected static Set findAllByCreatedBefore(Date d, Field f) {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where      "
        + "     ms.create_time  < :time   "
        + "and  ms.field.name   = :fname  "
        + "and  ms.field.type   = :ftype  "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong(   "time",  d.getTime()            );
      qry.setString( "fname", f.getName()            );
      qry.setString( "ftype", f.getType().toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllByCreatedBefore(d, f)

  // @since   1.2.0
  protected static Set findAllByMember(Member m) {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Membership as ms where ms.member_id = :member");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByMember");
      qry.setParameter("member", m);
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllByMember(m)

  // @since   1.2.0
  protected static Set findAllByMemberAndVia(Member m, Owner via) {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.via_id     = :via    "
      );
      qry.setCacheable(false);  // TODO 20061219 Comment was "Don't cache".  Why not?
      qry.setParameter( "member", m   );
      qry.setParameter( "via",    via );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllByMemberAndVia(m, via)

  // @since   1.2.0
  protected static Set findAllByOwnerAndField(Owner o, Field f) {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByOwnerAndField");
      qry.setEntity( "owner", o                      ); 
      qry.setString( "fname", f.getName()            );
      qry.setString( "ftype", f.getType().toString() ); 
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllByOwnerAndField(o, f)

  // @since   1.2.0
  protected static Set findAllByOwnerAndFieldAndType(Owner o, Field f, MembershipType type) {
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
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
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllByOwnerAndFieldAndType(o, f, type)

  // @since   1.2.0
  protected static Set findAllByOwnerAndMemberAndField(Owner o, Member m, Field f) {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  " 
        + "and  ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField");
      qry.setParameter( "owner",  o                      );
      qry.setParameter( "member", m                      );
      qry.setString(    "fname",  f.getName()            );
      qry.setString(    "ftype",  f.getType().toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllByOwnerAndMemberAndField(o, m, f)

  // @since   1.2.0
  protected static Set findAllEffective(Owner o, Member m, Field f, Owner via, int depth) {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
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
      qry.setCacheRegion(KLASS + ".FindAllEffective");
      qry.setParameter( "owner",  o                           );
      qry.setParameter( "member", m                           );
      qry.setString(    "fname",  f.getName()                 );
      qry.setString(    "ftype",  f.getType().toString()      );
      qry.setString(    "type",   MembershipType.E.toString() );
      qry.setParameter( "via",    via                         );
      qry.setInteger(   "depth",  depth                       );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllEffective(o, m, f, via, depth)

  // @since   1.2.0
  protected static Set findAllEffectiveByMemberAndField(Member m, Field f) {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllEffectiveByMemberAndField");
      qry.setParameter( "member", m                           );
      qry.setString(    "fname",  f.getName()                 );
      qry.setString(    "ftype",  f.getType().toString()      );
      qry.setString(    "type",   MembershipType.E.toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findEffectiveByMemberAndField(m, f)

  // @since   1.2.0 
  protected static Set findAllEffectiveByOwnerAndMemberAndField(
    Owner o, Member m, Field f
  )
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllEffectiveByOwnerAndMemberAndField");
      qry.setParameter( "owner",  o                           );
      qry.setParameter( "member", m                           );
      qry.setString(    "fname",  f.getName()                 );
      qry.setString(    "ftype",  f.getType().toString()      );
      qry.setString(    "type",   MembershipType.E.toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllEffectiveByOwnerAndMemberAndField(o, m, f)

  // @since   1.2.0
  protected static Set findAllImmediateByMemberAndField(Member m, Field f) {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField");      
      qry.setParameter( "member", m                           );
      qry.setString(    "fname",  f.getName()                 );
      qry.setString(    "ftype",  f.getType().toString()      );
      qry.setString(    "type",   MembershipType.I.toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061219 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findAllImmediateByMemberAndField(m, f)

  // @since   1.2.0
  protected static Membership findByOwnerAndMemberAndFieldAndType(
    Owner o, Member m, Field f, MembershipType type
  )
    throws  MembershipNotFoundException // TODO 20061219 should throw/return something else.  null?
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByOwnerAndMemberAndFieldAndType");
      qry.setParameter( "owner",  o                      );
      qry.setParameter( "member", m                      );
      qry.setString(    "fname",  f.getName()            );
      qry.setString(    "ftype",  f.getType().toString() ); 
      qry.setString(    "type",   type.toString()        );
      Membership ms = (Membership) qry.uniqueResult();
      hs.close();
      if (ms == null) {
        throw new MembershipNotFoundException();
      }
      return ms;
    }
    catch (HibernateException eH) {
      throw new MembershipNotFoundException( eH.getMessage(), eH );
    }
  } // protected static Membership findByOwnerAndMemberAndFieldAndType(o, m, f, type)

  // @since   1.2.0
  protected static Membership findByUuid(String uuid) 
    throws  MembershipNotFoundException 
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Membership as ms where ms.uuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      Membership ms = (Membership) qry.uniqueResult();
      hs.close();
      if (ms == null) {
        throw new MembershipNotFoundException("could not find membership with uuid: " + U.q(uuid));
      }
      return ms;
    }
    catch (HibernateException eH) {
      throw new MembershipNotFoundException( eH.getMessage(), eH );
    }

  } // protected static Membership findByUuid(uuid)

  // @since   1.2.0
  protected static Set findChildMemberships(Membership ms) { // TODO 20061219 rename
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Membership as ms where ms.parent_membership = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildMemberships");
      qry.setString( "uuid", ms.getUuid() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061214 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected sdtatic Set findChildMemberships(ms)

  // @since   1.2.0  
  protected static Set findMemberships(Member m, Field f) { // TODO 20061219 rename
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.field.name = :fname  "
        + "and  ms.field.type = :ftype"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMemberships");
      qry.setParameter( "member", m                      );
      qry.setString(    "fname" , f.getName()            );
      qry.setString(    "ftype" , f.getType().toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      // TODO 20061214 this should throw some flavor of exception
      ErrorLog.error( HibernateMembershipDAO.class, eH.getMessage() );
    }
    return mships;
  } // protected static Set findMemberships(m, f)

  // @since   1.2.0
  protected static void update(MemberOf mof) 
    throws  MemberAddException  // TODO 20061221 which exception?  
  {
    // TODO 20061221 just passing in mof is ugly, especially given what mof returns
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = mof.getDeletes().iterator();
        while (it.hasNext()) {
          hs.delete( it.next() );
        }
        it = mof.getSaves().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( it.next() );
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw new MemberAddException( eH.getMessage(), eH );
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      throw new MemberAddException( eH.getMessage(), eH );
    }
  } // protected static void update(mof)

} // class HibernateMembershipDAO

