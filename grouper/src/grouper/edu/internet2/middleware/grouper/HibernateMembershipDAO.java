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
 * @version $Id: HibernateMembershipDAO.java,v 1.14 2007-01-09 19:33:57 blair Exp $
 * @since   1.2.0
 */
class HibernateMembershipDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateMembershipDAO.class.getName();


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set findAllByCreatedAfter(Date d, Field f) 
    throws  GrouperDAOException
  {
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
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByCreatedAfter(d, f)

  // @since   1.2.0
  protected static Set findAllByCreatedBefore(Date d, Field f) 
    throws  GrouperDAOException
  {
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
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByCreatedBefore(d, f)

  // @since   1.2.0
  protected static Set findAllByMember(Member m) 
    throws  GrouperDAOException
  {
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
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByMember(m)

  // @since   1.2.0
  protected static Set findAllByMemberAndVia(Member m, Owner via) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.via_id     = :via    "
      );
      qry.setCacheable(false);  // TODO 20061219 Comment was "Don't cache".  Why not?
      qry.setParameter( "member", m             );
      qry.setParameter( "via",    via.getUuid() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByMemberAndVia(m, via)

  // @since   1.2.0
  protected static Set findAllByOwnerAndField(String ownerID, Field f) 
    throws  GrouperDAOException
  {
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
      qry.setString( "owner", ownerID                ); 
      qry.setString( "fname", f.getName()            );
      qry.setString( "ftype", f.getType().toString() ); 
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByOwnerAndField(ownerID, f)

  // @since   1.2.0
  protected static Set findAllByOwnerAndFieldAndType(String ownerID, Field f, String type) 
    throws  GrouperDAOException
  {
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
      qry.setString( "owner" , ownerID                 );
      qry.setString( "fname" , f.getName()             );
      qry.setString( "ftype" , f.getType().toString()  );
      qry.setString( "type"  , type                    );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByOwnerAndFieldAndType(ownerID, f, type)

  // @since   1.2.0
  protected static Set findAllByOwnerAndMemberAndField(String ownerID, Member m, Field f) 
    throws  GrouperDAOException
  {
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
      qry.setString(    "owner",  ownerID                );
      qry.setParameter( "member", m                      );
      qry.setString(    "fname",  f.getName()            );
      qry.setString(    "ftype",  f.getType().toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByOwnerAndMemberAndField(ownerID, m, f)

  // @since   1.2.0
  protected static Set findAllEffective(String ownerID, Member m, Field f, String viaUUID, int depth) 
    throws  GrouperDAOException
  {
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
      qry.setString(    "owner",  ownerID                     );
      qry.setParameter( "member", m                           );
      qry.setString(    "fname",  f.getName()                 );
      qry.setString(    "ftype",  f.getType().toString()      );
      qry.setString(    "type",   Membership.INTERNAL_TYPE_E.toString() );
      qry.setString(    "via",    viaUUID                     );
      qry.setInteger(   "depth",  depth                       );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllEffective(ownerID, m, f, via, depth)

  // @since   1.2.0
  protected static Set findAllEffectiveByMemberAndField(Member m, Field f) 
    throws  GrouperDAOException
  {
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
      qry.setString(    "type",   Membership.INTERNAL_TYPE_E.toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findEffectiveByMemberAndField(m, f)

  // @since   1.2.0 
  protected static Set findAllEffectiveByOwnerAndMemberAndField(String ownerID, Member m, Field f)
    throws  GrouperDAOException
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
      qry.setString(    "owner",  ownerID                     );
      qry.setParameter( "member", m                           );
      qry.setString(    "fname",  f.getName()                 );
      qry.setString(    "ftype",  f.getType().toString()      );
      qry.setString(    "type",   Membership.INTERNAL_TYPE_E.toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllEffectiveByOwnerAndMemberAndField(ownerID, m, f)

  // @since   1.2.0
  protected static Set findAllImmediateByMemberAndField(Member m, Field f) 
    throws  GrouperDAOException
  {
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
      qry.setString(    "type",   Membership.INTERNAL_TYPE_I.toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllImmediateByMemberAndField(m, f)

  // @since   1.2.0
  protected static Membership findByOwnerAndMemberAndFieldAndType(String ownerID, Member m, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException // TODO 20061219 should throw/return something else.  null?
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
      qry.setString(    "owner",  ownerID                );
      qry.setParameter( "member", m                      );
      qry.setString(    "fname",  f.getName()            );
      qry.setString(    "ftype",  f.getType().toString() ); 
      qry.setString(    "type",   type                   );
      Membership ms = (Membership) qry.uniqueResult();
      hs.close();
      if (ms == null) {
        throw new MembershipNotFoundException(); // TODO 20070104 null or ex?
      }
      return ms;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static Membership findByOwnerAndMemberAndFieldAndType(ownerID, m, f, type)

  // @since   1.2.0
  protected static Membership findByUuid(String uuid) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
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
        // TODO 20070104 null or ex?
        throw new MembershipNotFoundException("could not find membership with uuid: " + U.internal_q(uuid));
      }
      return ms;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }

  } // protected static Membership findByUuid(uuid)

  // @since   1.2.0
  protected static Set findChildMemberships(Membership ms) // TODO 20061219 rename
    throws  GrouperDAOException
  {
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
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected sdtatic Set findChildMemberships(ms)

  // @since   1.2.0  
  protected static Set findMemberships(Member m, Field f) // TODO 20061219 rename
    throws  GrouperDAOException
  {
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
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findMemberships(m, f)

  // @since   1.2.0
  protected static void update(MemberOf mof) 
    throws  GrouperDAOException
  {
    // TODO 20061221 just passing in mof is ugly, especially given what mof returns
    try {
      Session     hs  = HibernateDAO.getSession();
      Transaction tx  = hs.beginTransaction();
      try {
        Iterator it = mof.internal_getDeletes().iterator();
        while (it.hasNext()) {
          hs.delete( it.next() );
        }
        it = mof.internal_getSaves().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( it.next() );
        }
        tx.commit();
      }
      catch (HibernateException eH) {
        tx.rollback();
        throw eH;
      }
      finally {
        hs.close();
      }
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static void update(mof)

} // class HibernateMembershipDAO

