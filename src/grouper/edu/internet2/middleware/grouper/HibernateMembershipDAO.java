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
 * @version $Id: HibernateMembershipDAO.java,v 1.17 2007-01-11 20:28:05 blair Exp $
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
        + "and  ms.list_name    = :fname  "
        + "and  ms.list_type    = :ftype  "
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
        + "and  ms.list_name    = :fname  "
        + "and  ms.list_type    = :ftype  "
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
  protected static Set findAllByMember(String mUUID) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from Membership as ms where ms.member_id = :member");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByMember");
      qry.setString("member", mUUID);
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByMember(mUUID)

  // @since   1.2.0
  protected static Set findAllByMemberAndVia(String mUUID, String viaUUID) 
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
      qry.setString( "member", mUUID   );
      qry.setString( "via",    viaUUID );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByMemberAndVia(mUUID, viaUUID)

  // @since   1.2.0
  protected static Set findAllByOwnerAndField(String ownerUUID, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.list_name  = :fname  "
        + "and  ms.list_type  = :ftype  "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByOwnerAndField");
      qry.setString( "owner", ownerUUID                ); 
      qry.setString( "fname", f.getName()            );
      qry.setString( "ftype", f.getType().toString() ); 
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByOwnerAndField(ownerUUID, f)

  // @since   1.2.0
  protected static Set findAllByOwnerAndFieldAndType(String ownerUUID, Field f, String type) 
    throws  GrouperDAOException
  {
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.list_name  = :fname  "
        + "and  ms.list_type  = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMembershipsByType");
      qry.setString( "owner" , ownerUUID                 );
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
  } // protected static Set findAllByOwnerAndFieldAndType(ownerUUID, f, type)

  // @since   1.2.0
  protected static Set findAllByOwnerAndMemberAndField(String ownerUUID, String mUUID, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  " 
        + "and  ms.member_id  = :member "
        + "and  ms.list_name  = :fname  "
        + "and  ms.list_type  = :ftype"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByOwnerAndMemberAndField(ownerUUID, mUUID, f)

  // @since   1.2.0
  protected static Set findAllEffective(String ownerUUID, String mUUID, Field f, String viaUUID, int depth) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where      "
        + "     ms.owner_id     = :owner  "
        + "and  ms.member_id    = :member "
        + "and  ms.list_name    = :fname  "
        + "and  ms.list_type    = :ftype  "
        + "and  ms.mship_type   = :type   "
        + "and  ms.via_id       = :via    "
        + "and  ms.depth        = :depth"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllEffective");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      qry.setString( "type",   Membership.INTERNAL_TYPE_E.toString() );
      qry.setString( "via",    viaUUID                );
      qry.setInteger("depth",  depth                  );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllEffective(ownerUUID, mUUID, f, via, depth)

  // @since   1.2.0
  protected static Set findAllEffectiveByMemberAndField(String mUUID, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.list_name  = :fname  "
        + "and  ms.list_type  = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllEffectiveByMemberAndField");
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      qry.setString( "type",   Membership.INTERNAL_TYPE_E.toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findEffectiveByMemberAndField(mUUID, f)

  // @since   1.2.0 
  protected static Set findAllEffectiveByOwnerAndMemberAndField(String ownerUUID, String mUUID, Field f)
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.member_id  = :member "
        + "and  ms.list_name  = :fname  "
        + "and  ms.list_type  = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllEffectiveByOwnerAndMemberAndField");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      qry.setString( "type",   Membership.INTERNAL_TYPE_E.toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllEffectiveByOwnerAndMemberAndField(ownerUUID, mUUID, f)

  // @since   1.2.0
  protected static Set findAllImmediateByMemberAndField(String mUUID, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.list_name  = :fname  "
        + "and  ms.list_type  = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField");      
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      qry.setString( "type",   Membership.INTERNAL_TYPE_I.toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllImmediateByMemberAndField(mUUID, f)

  // @since   1.2.0
  protected static Membership findByOwnerAndMemberAndFieldAndType(String ownerUUID, String mUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException // TODO 20061219 should throw/return something else.  null?
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.owner_id   = :owner  "
        + "and  ms.member_id  = :member "
        + "and  ms.list_name  = :fname  "
        + "and  ms.list_type  = :ftype  "
        + "and  ms.mship_type = :type   "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByOwnerAndMemberAndFieldAndType");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() ); 
      qry.setString( "type",   type                   );
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
  } // protected static Membership findByOwnerAndMemberAndFieldAndType(ownerUUID, mUUID, f, type)

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
  protected static Set findMemberships(String mUUID, Field f) // TODO 20061219 rename
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from Membership as ms where    "
        + "     ms.member_id  = :member "
        + "and  ms.list_name  = :fname  "
        + "and  ms.list_type  = :ftype"
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMemberships");
      qry.setString( "member", mUUID                  );
      qry.setString( "fname" , f.getName()            );
      qry.setString( "ftype" , f.getType().toString() );
      mships.addAll( qry.list() );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findMemberships(mUUID, f)

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

