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
 * @version $Id: HibernateMembershipDAO.java,v 1.19 2007-02-14 17:34:14 blair Exp $
 * @since   1.2.0
 */
class HibernateMembershipDAO extends HibernateDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = HibernateMembershipDAO.class.getName();


  // HIBERNATE PROPERTIES //
  private long    createTime;
  private String  creatorUUID;
  private int     depth;
  private String  id;
  private String  listName;
  private String  listType;
  private String  memberUUID;
  private String  ownerUUID;
  private String  parentUUID;
  private String  type;
  private String  membershipUUID;
  private String  viaUUID;


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static Set findAllByCreatedAfter(Date d, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateMembershipDAO as ms where  "
        + "     ms.createTime > :time             "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong(   "time",  d.getTime()            );
      qry.setString( "fname", f.getName()            );
      qry.setString( "ftype", f.getType().toString() );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
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
        "from HibernateMembershipDAO as ms where  "
        + "     ms.createTime < :time             "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByCreatedAfter");
      qry.setLong(   "time",  d.getTime()            );
      qry.setString( "fname", f.getName()            );
      qry.setString( "ftype", f.getType().toString() );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
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
      Query   qry = hs.createQuery("from HibernateMembershipDAO as ms where ms.memberUuid = :member");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByMember");
      qry.setString("member", mUUID);
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
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
        "from HibernateMembershipDAO as ms where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.viaUuid     = :via             "
      );
      qry.setCacheable(false);  // TODO 20061219 Comment was "Don't cache".  Why not?
      qry.setString( "member", mUUID   );
      qry.setString( "via",    viaUUID );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
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
        "from HibernateMembershipDAO as ms where  "
        + "     ms.ownerUuid   = :owner            "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByOwnerAndField");
      qry.setString( "owner", ownerUUID                ); 
      qry.setString( "fname", f.getName()            );
      qry.setString( "ftype", f.getType().toString() ); 
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
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
        "from HibernateMembershipDAO as ms where  "
        + "     ms.ownerUuid   = :owner            "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            "
        + "and  ms.type = :type             "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMembershipsByType");
      qry.setString( "owner" , ownerUUID                 );
      qry.setString( "fname" , f.getName()             );
      qry.setString( "ftype" , f.getType().toString()  );
      qry.setString( "type"  , type                    );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
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
        "from HibernateMembershipDAO as ms where  "
        + "     ms.ownerUuid   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllByOwnerAndMemberAndField(ownerUUID, mUUID, f)

  // @since   1.2.0
  protected static Set findAllChildMemberships(MembershipDTO dto) 
    throws  GrouperDAOException
  {
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateMembershipDAO as ms where ms.parentUuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindChildMemberships");
      qry.setString( "uuid", dto.getMembershipUuid() );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected sdtatic Set findAllChildMemberships(dto)

  // @since   1.2.0
  protected static Set findAllEffective(String ownerUUID, String mUUID, Field f, String viaUUID, int depth) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateMembershipDAO as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             "
        + "and  ms.viaUuid    = :via              "
        + "and  ms.depth      = :depth            "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllEffective");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      qry.setString( "type",   Membership.EFFECTIVE   );
      qry.setString( "via",    viaUUID                );
      qry.setInteger("depth",  depth                  );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
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
        "from HibernateMembershipDAO as ms where  "
        + "     ms.memberUuid  = :member           "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            "
        + "and  ms.type = :type             "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllEffectiveByMemberAndField");
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      qry.setString( "type",   Membership.EFFECTIVE   );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
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
        "from HibernateMembershipDAO as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllEffectiveByOwnerAndMemberAndField");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      qry.setString( "type",   Membership.EFFECTIVE   );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
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
        "from HibernateMembershipDAO as ms where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField");      
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      qry.setString( "type",   Membership.IMMEDIATE   );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findAllImmediateByMemberAndField(mUUID, f)

  // @since   1.2.0
  protected static MembershipDTO findByOwnerAndMemberAndFieldAndType(String ownerUUID, String mUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException // TODO 20061219 should throw/return something else.  null?
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateMembershipDAO as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByOwnerAndMemberAndFieldAndType");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", mUUID                  );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() ); 
      qry.setString( "type",   type                   );
      HibernateMembershipDAO dao = (HibernateMembershipDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new MembershipNotFoundException(); // TODO 20070104 null or ex?
      }
      return MembershipDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } // protected static MembershipDTO findByOwnerAndMemberAndFieldAndType(ownerUUID, mUUID, f, type)

  // @since   1.2.0
  protected static MembershipDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateMembershipDAO as ms where ms.membershipUuid = :uuid");
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      HibernateMembershipDAO dao = (HibernateMembershipDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        // TODO 20070104 null or ex?
        throw new MembershipNotFoundException("could not find membership with uuid: " + U.internal_q(uuid));
      }
      return MembershipDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }

  } // protected static MembershipDTO findByUuid(uuid)

  // @since   1.2.0  
  protected static Set findMemberships(String mUUID, Field f) // TODO 20061219 rename
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateMembershipDAO as ms where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
      );
      qry.setCacheable(true);
      qry.setCacheRegion(KLASS + ".FindMemberships");
      qry.setString( "member", mUUID                  );
      qry.setString( "fname" , f.getName()            );
      qry.setString( "ftype" , f.getType().toString() );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } // protected static Set findMemberships(mUUID, f)

  // @since   1.2.0
  // TODO 20070124 refactor usage of this method
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
          hs.delete( Rosetta.getDAO( it.next() ) );
        }
        it = mof.internal_getSaves().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( Rosetta.getDAO( it.next() ) );
        }
        it = mof.getModifiedGroups().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( Rosetta.getDAO( it.next() ) );
        }
        it = mof.getModifiedStems().iterator();
        while (it.hasNext()) {
          hs.saveOrUpdate( Rosetta.getDAO( it.next() ) );
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

  
  // GETTERS //
  
  protected long getCreateTime() {
    return this.createTime;
  }
  protected String getCreatorUuid() {
    return this.creatorUUID;
  }
  protected int getDepth() {
    return this.depth;
  }
  protected String getId() {
    return this.id;
  }
  protected String getListName() {
    return this.listName;
  }
  protected String getListType() {
    return this.listType;
  }
  protected String getMemberUuid() {
    return this.memberUUID;
  }
  protected String getOwnerUuid() {
    return this.ownerUUID;
  }
  protected String getParentUuid() {
    return this.parentUUID;
  }
  protected String getType() {
    return this.type;
  }
  protected String getMembershipUuid() {
    return this.membershipUUID;
  }
  protected String getViaUuid() {
    return this.viaUUID;
  }


  // SETTERS //

  protected void setCreateTime(long createTime) {
    this.createTime = createTime;
  }
  protected void setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
  }
  protected void setDepth(int depth) {
    this.depth = depth;
  }
  protected void setId(String id) {
    this.id = id;
  }
  protected void setListName(String listName) {
    this.listName = listName;
  }
  protected void setListType(String listType) {
    this.listType = listType;
  }
  protected void setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
  }
  protected void setOwnerUuid(String ownerUUID) {
    this.ownerUUID = ownerUUID;
  }
  protected void setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
  }
  protected void setType(String type) {
    this.type = type;
  }
  protected void setMembershipUuid(String membershipUUID) {
    this.membershipUUID = membershipUUID;
  }
  protected void setViaUuid(String viaUUID) {
    this.viaUUID = viaUUID;
  }

} // class HibernateMembershipDAO extends HibernateDAO


