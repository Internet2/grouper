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
 * @version $Id: HibernateMembershipDAO.java,v 1.25 2007-04-05 14:28:28 blair Exp $
 * @since   1.2.0
 */
class HibernateMembershipDAO extends HibernateDAO implements MembershipDAO {

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
  private String  uuid;
  private String  viaUUID;


  // PUBLIC INSTANCE METHODS //

  /**
   * <p/>
   * @since   1.2.0 
   */
  public boolean exists(String ownerUUID, String memberUUID, String listName, String msType)
    throws  GrouperDAOException
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "select ms.id from HibernateMembershipDAO as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.type       = :type             "
      );
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".Exists");
      qry.setString( "owner",  ownerUUID  );
      qry.setString( "member", memberUUID );
      qry.setString( "fname",  listName   );
      qry.setString( "type",   msType     );
      boolean rv  = false;
      if ( qry.uniqueResult() != null ) {
        rv = true;
      }
      hs.close();
      return rv;
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedAfter(Date d, Field f) 
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
      qry.setCacheable(false);
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
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByCreatedBefore(Date d, Field f) 
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
      qry.setCacheable(false);
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
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByMember(String memberUUID) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateMembershipDAO as ms where ms.memberUuid = :member");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByMember");
      qry.setString("member", memberUUID);
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByMemberAndVia(String memberUUID, String viaUUID) 
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
      qry.setString( "member", memberUUID );
      qry.setString( "via",    viaUUID    );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByOwnerAndField(String ownerUUID, Field f) 
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
      qry.setCacheable(false);
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
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByOwnerAndFieldAndType(String ownerUUID, Field f, String type) 
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
      qry.setCacheable(false);
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
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f) 
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
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", memberUUID             );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } 

  /**
   * @since   1.2.0
   */
  public MembershipDTO findByOwnerAndMemberAndFieldAndType(String ownerUUID, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException
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
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByOwnerAndMemberAndFieldAndType");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", memberUUID             );
      qry.setString( "fname",  f.getName()            );
      qry.setString( "ftype",  f.getType().toString() ); 
      qry.setString( "type",   type                   );
      HibernateMembershipDAO dao = (HibernateMembershipDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new MembershipNotFoundException();
      }
      return MembershipDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllChildMemberships(MembershipDTO _ms) 
    throws  GrouperDAOException
  {
    Set mships  = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateMembershipDAO as ms where ms.parentUuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindChildMemberships");
      qry.setString( "uuid", _ms.getUuid() );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllEffective(String ownerUUID, String memberUUID, Field f, String viaUUID, int depth) 
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
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllEffective");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", memberUUID             );
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
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllEffectiveByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery(
        "from HibernateMembershipDAO as ms where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.listName  = :fname             "
        + "and  ms.listType  = :ftype             "
        + "and  ms.type = :type                   "
      );
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllEffectiveByMemberAndField");
      qry.setString( "member", memberUUID             );
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
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllEffectiveByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f)
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
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllEffectiveByOwnerAndMemberAndField");
      qry.setString( "owner",  ownerUUID              );
      qry.setString( "member", memberUUID             );
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
  } 

  /**
   * @since   1.2.0
   */
  public Set findAllImmediateByMemberAndField(String memberUUID, Field f) 
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
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField");      
      qry.setString( "member", memberUUID             );
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
  } 

  /**
   * @since   1.2.0
   */
  public MembershipDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
  {
    try {
      Session hs  = HibernateDAO.getSession();
      Query   qry = hs.createQuery("from HibernateMembershipDAO as ms where ms.uuid = :uuid");
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindByUuid");
      qry.setString("uuid", uuid);
      HibernateMembershipDAO dao = (HibernateMembershipDAO) qry.uniqueResult();
      hs.close();
      if (dao == null) {
        throw new MembershipNotFoundException("could not find membership with uuid: " + U.internal_q(uuid));
      }
      return MembershipDTO.getDTO(dao);
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }

  } 

  /**
   * @since   1.2.0
   */
  public Set findMembershipsByMemberAndField(String memberUUID, Field f)
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
      qry.setCacheable(false);
      qry.setCacheRegion(KLASS + ".FindMemberships");
      qry.setString( "member", memberUUID             );
      qry.setString( "fname" , f.getName()            );
      qry.setString( "ftype" , f.getType().toString() );
      mships.addAll( MembershipDTO.getDTO( qry.list() ) );
      hs.close();
    }
    catch (HibernateException eH) {
      throw new GrouperDAOException( eH.getMessage(), eH );
    }
    return mships;
  } 

  /**
   * @since   1.2.0
   */ 
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @since   1.2.0
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   1.2.0
   */
  public int getDepth() {
    return this.depth;
  }

  /**
   * @since   1.2.0
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   1.2.0
   */
  public String getListName() {
    return this.listName;
  }

  /**
   * @since   1.2.0
   */
  public String getListType() {
    return this.listType;
  }

  /**
   * @since   1.2.0
   */
  public String getMemberUuid() {
    return this.memberUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getOwnerUuid() {
    return this.ownerUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getParentUuid() {
    return this.parentUUID;
  }

  /**
   * @since   1.2.0
   */
  public String getType() {
    return this.type;
  }

  /**
   * @since   1.2.0
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @since   1.2.0
   */
  public String getViaUuid() {
    return this.viaUUID;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setDepth(int depth) {
    this.depth = depth;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setListName(String listName) {
    this.listName = listName;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setListType(String listType) {
    this.listType = listType;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setOwnerUuid(String ownerUUID) {
    this.ownerUUID = ownerUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public HibernateMembershipDAO setViaUuid(String viaUUID) {
    this.viaUUID = viaUUID;
    return this;
  }

  /**
   * @since   1.2.0
   */
  public void update(MemberOf mof) 
    throws  GrouperDAOException
  {
    // TODO 20070404 this is incredibly ugly
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
  } 


  // PROTECTED CLASS METHODS //

  // @since   1.2.0
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    hs.delete("from HibernateMembershipDAO");
  } // protected static void reset(hs)

} // class HibernateMembershipDAO extends HibernateDAO implements MembershipDAO 

