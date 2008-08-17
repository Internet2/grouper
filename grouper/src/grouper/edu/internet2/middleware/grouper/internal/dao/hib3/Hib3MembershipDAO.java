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

package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.DefaultMemberOf;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipNotFoundException;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dto.MemberDTO;
import edu.internet2.middleware.grouper.internal.dto.MembershipDTO;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.internal.util.Rosetta;

/**
 * Basic Hibernate <code>Membership</code> DAO interface.
 * <p><b>WARNING: THIS IS AN ALPHA INTERFACE THAT MAY CHANGE AT ANY TIME.</b></p>
 * @author  blair christensen.
 * @version $Id: Hib3MembershipDAO.java,v 1.10.4.4 2008-08-17 23:52:57 shilen Exp $
 * @since   @HEAD@
 */
public class Hib3MembershipDAO extends Hib3DAO implements MembershipDAO {

  // PRIVATE CLASS CONSTANTS //
  private static final String KLASS = Hib3MembershipDAO.class.getName();


  // HIBERNATE PROPERTIES //
  private long    createTime;
  private String  creatorUUID;
  private int     depth;
  private String  id;
  private String  listName;
  private String  listType;
  private String  memberUUID;
  private MemberDAO  memberDAO;
  private String  ownerUUID;
  private String  parentUUID;
  private String  type;
  private String  uuid;
  private String  viaUUID;


  // PUBLIC INSTANCE METHODS //

  /**
   * <p/>
   * @since   @HEAD@ 
   */
  public boolean exists(String ownerUUID, String memberUUID, String listName, String msType)
    throws  GrouperDAOException {
    Object id = HibernateSession.byHqlStatic()
      .createQuery("select ms.id from Hib3MembershipDAO as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.type       = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".Exists")
      .setString( "owner",  ownerUUID  )
      .setString( "member", memberUUID )
      .setString( "fname",  listName   )
      .setString( "type",   msType     )
      .uniqueResult(Object.class);
    boolean rv  = false;
    if ( id != null ) {
      rv = true;
    }
    return rv;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByCreatedAfter(Date d, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery("from Hib3MembershipDAO as ms where  "
        + "     ms.createTime > :time             "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
      .setLong(   "time",  d.getTime()            )
      .setString( "fname", f.getName()            )
      .setString( "ftype", f.getType().toString() ).list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
        mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByCreatedBefore(Date d, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.createTime < :time             "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
      .setLong(   "time",  d.getTime()            )
      .setString( "fname", f.getName()            )
      .setString( "ftype", f.getType().toString() ).list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByMember(String memberUUID) 
    throws  GrouperDAOException {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery("from Hib3MembershipDAO as ms where ms.memberUuid = :member")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByMember")
      .setString("member", memberUUID)
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByMemberAndVia(String memberUUID, String viaUUID) 
    throws  GrouperDAOException {

    Set mships = new LinkedHashSet();

    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.viaUuid     = :via             "
      )
      .setString( "member", memberUUID )
      .setString( "via",    viaUUID    ).list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByOwnerAndField(String ownerUUID, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.ownerUuid   = :owner            "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwnerAndField")
      .setString( "owner", ownerUUID                )
      .setString( "fname", f.getName()            )
      .setString( "ftype", f.getType().toString() ) 
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByOwnerAndFieldAndType(String ownerUUID, Field f, String type) 
    throws  GrouperDAOException {
    Set mships  = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery("from Hib3MembershipDAO as ms where  "
        + "     ms.ownerUuid   = :owner            "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            "
        + "and  ms.type = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMembershipsByType")
      .setString( "owner" , ownerUUID                 )
      .setString( "fname" , f.getName()             )
      .setString( "ftype" , f.getType().toString()  )
      .setString( "type"  , type                    )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f) 
    throws  GrouperDAOException {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.ownerUuid   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and  ms.listName  = :fname            "
        + "and  ms.listType  = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @see     MembershipDAO#findAllMembersByOwnerAndField(String, Field)
   * @since   @HEAD@
   */
  public Set<MemberDTO> findAllMembersByOwnerAndField(String ownerUUID, Field f)
    throws  GrouperDAOException
  {
    Set<MemberDTO> members = new LinkedHashSet();
    List<Hib3MemberDAO> hib3MemberDAOs = HibernateSession.byHqlStatic()
      .createQuery(
          "select m"
        + " from Hib3MemberDAO m, Hib3MembershipDAO ms where"
        + " ms.ownerUuid      = :owner"
        + " and ms.listName   = :fname"
        + " and ms.listType   = :ftype"
        + " and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembersByOwnerAndField")
      .setString( "owner", ownerUUID ) 
      .setString( "fname", f.getName() )
      .setString( "ftype", f.getType().toString() )
      .list(Hib3MemberDAO.class);
    for (Hib3MemberDAO hib3MemberDAO : hib3MemberDAOs) {
      members.add( MemberDTO.getDTO( hib3MemberDAO ) );
    }
    return members;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDTO findByOwnerAndMemberAndFieldAndType(String ownerUUID, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException {
    Hib3MembershipDAO dao = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByOwnerAndMemberAndFieldAndType")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() ) 
      .setString( "type",   type                   )
      .uniqueResult(Hib3MembershipDAO.class);
    if (dao == null) {
      throw new MembershipNotFoundException();
    }
    return MembershipDTO.getDTO(dao);
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllChildMemberships(MembershipDTO _ms) 
    throws  GrouperDAOException
  {
    Set mships  = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery("from Hib3MembershipDAO as ms where ms.parentUuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindChildMemberships")
      .setString( "uuid", _ms.getUuid() )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllEffective(String ownerUUID, String memberUUID, Field f, String viaUUID, int depth) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             "
        + "and  ms.viaUuid    = :via              "
        + "and  ms.depth      = :depth            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffective")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.EFFECTIVE   )
      .setString( "via",    viaUUID                )
      .setInteger("depth",  depth                  )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllEffectiveByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.listName  = :fname             "
        + "and  ms.listType  = :ftype             "
        + "and  ms.type = :type                   ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByMemberAndField")
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.EFFECTIVE   )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllEffectiveByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f)
    throws  GrouperDAOException {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByOwnerAndMemberAndField")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.EFFECTIVE   )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 
  
  /**
   * @since   1.2.1
   */
  public Set findAllByOwnerAndMember(String ownerUUID, String memberUUID) 
    throws  GrouperDAOException {
    
    //Added by Gary Brown 2007-11-01 so that getPrivs can do one query rather than 6
    Set mships = new LinkedHashSet();

    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.ownerUuid   = :owner            "  
        + "and  ms.memberUuid  = :member           "
      ).setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  }

  /**
   * @since   @HEAD@
   */
  public Set findAllImmediateByMember(String memberUUID) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.type       = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllImmediateByMember")
      .setString( "member", memberUUID             )
      .setString( "type",   Membership.IMMEDIATE   )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public Set findAllImmediateByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            "
        + "and  ms.type       = :type             ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField")
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.IMMEDIATE   )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */
  public MembershipDTO findByUuid(String uuid) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
  {
    Hib3MembershipDAO dao = HibernateSession.byHqlStatic()
      .createQuery("from Hib3MembershipDAO as ms where ms.uuid = :uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid)
      .uniqueResult(Hib3MembershipDAO.class);
    if (dao == null) {
      throw new MembershipNotFoundException("could not find membership with uuid: " + Quote.single(uuid));
    }
    return MembershipDTO.getDTO(dao);
  } 

  /**
   * @since   @HEAD@
   */
  public Set findMembershipsByMemberAndField(String memberUUID, Field f)
    throws  GrouperDAOException
  {
    Set mships = new LinkedHashSet();
    List<Hib3MembershipDAO> hib3MembershipDAOs = HibernateSession.byHqlStatic()
      .createQuery(
        "from Hib3MembershipDAO as ms where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.listName   = :fname            "
        + "and  ms.listType   = :ftype            ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMemberships")
      .setString( "member", memberUUID             )
      .setString( "fname" , f.getName()            )
      .setString( "ftype" , f.getType().toString() )
      .list(Hib3MembershipDAO.class);
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      mships.add( MembershipDTO.getDTO( hib3MembershipDAO ) );
    }
    return mships;
  } 

  /**
   * @since   @HEAD@
   */ 
  public long getCreateTime() {
    return this.createTime;
  }

  /**
   * @since   @HEAD@
   */
  public String getCreatorUuid() {
    return this.creatorUUID;
  }

  /**
   * @since   @HEAD@
   */
  public int getDepth() {
    return this.depth;
  }

  /**
   * @since   @HEAD@
   */
  public String getId() {
    return this.id;
  }

  /**
   * @since   @HEAD@
   */
  public String getListName() {
    return this.listName;
  }

  /**
   * @since   @HEAD@
   */
  public String getListType() {
    return this.listType;
  }

  /**
   * @since   @HEAD@
   */
  public String getMemberUuid() {
    return this.memberUUID;
  }
  
  /**
   * @since   @HEAD@
   */
  public MemberDAO getMemberDAO() {
    return this.memberDAO;
  }

  /**
   * @since   @HEAD@
   */
  public String getOwnerUuid() {
    return this.ownerUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getParentUuid() {
    return this.parentUUID;
  }

  /**
   * @since   @HEAD@
   */
  public String getType() {
    return this.type;
  }

  /**
   * @since   @HEAD@
   */
  public String getUuid() {
    return this.uuid;
  }

  /**
   * @since   @HEAD@
   */
  public String getViaUuid() {
    return this.viaUUID;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setCreateTime(long createTime) {
    this.createTime = createTime;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setCreatorUuid(String creatorUUID) {
    this.creatorUUID = creatorUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setDepth(int depth) {
    this.depth = depth;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setId(String id) {
    this.id = id;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setListName(String listName) {
    this.listName = listName;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setListType(String listType) {
    this.listType = listType;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setMemberUuid(String memberUUID) {
    this.memberUUID = memberUUID;
    return this;
  }
  
  /**
   * @since   @HEAD@
   */
  public MembershipDAO setMemberDAO(MemberDAO memberDAO) {
    this.memberDAO = memberDAO;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setOwnerUuid(String ownerUUID) {
    this.ownerUUID = ownerUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setParentUuid(String parentUUID) {
    this.parentUUID = parentUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setType(String type) {
    this.type = type;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setUuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public MembershipDAO setViaUuid(String viaUUID) {
    this.viaUUID = viaUUID;
    return this;
  }

  /**
   * @since   @HEAD@
   */
  public void update(final DefaultMemberOf mof) 
    throws  GrouperDAOException {
    // TODO 20070404 this is incredibly ugly
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            Session     hs  = hibernateSession.getSession();
            Iterator it = mof.getDeletes().iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.delete( grouperDAO );
            }
            it = mof.getSaves().iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.saveOrUpdate( grouperDAO );
            }
            it = mof.getModifiedGroups().iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.saveOrUpdate( grouperDAO );
            }
            it = mof.getModifiedStems().iterator();
            while (it.hasNext()) {
              GrouperDAO grouperDAO = Rosetta.getDAO( it.next() );
              hs.saveOrUpdate( grouperDAO );
            }
            return null;
          }
      
    });
  } 


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(Session hs) 
    throws  HibernateException
  {
    
    hs.createQuery("update Hib3MembershipDAO set parentUuid = null").executeUpdate();
    
    List<Hib3MembershipDAO> hib3MembershipDAOs = 
      hs.createQuery("from Hib3MembershipDAO as ms order by createTime desc")
      .list()
      ;

    // Deleting each membership from the time created in descending order. 
    // This is necessary to prevent deleting parent memberships before child 
    // memberships which causes integrity constraint violations on some databases. 
    for (Hib3MembershipDAO hib3MembershipDAO : hib3MembershipDAOs) {
      hs.createQuery("delete from Hib3MembershipDAO ms where ms.uuid=:uuid")
      .setString("uuid", hib3MembershipDAO.getUuid())
      .executeUpdate();
    }

  } 
  
//PRIVATE CLASS METHODS //
//@since 1.3.0
  private Set<MembershipDTO> _getMembershipsFromMembershipAndMemberQuery(Session session, Query qry)
    throws  HibernateException
  {
    Set<MembershipDTO> memberships = new LinkedHashSet<MembershipDTO>();
    Iterator it = qry.list().iterator();
    
    while (it.hasNext()) {
      Object[] tuple = (Object[])it.next();
      Hib3MembershipDAO currMembershipDAO = (Hib3MembershipDAO)tuple[0];
      HibUtils.evict(null, session,currMembershipDAO, false);
      
      Hib3MemberDAO currMemberDAO = (Hib3MemberDAO)tuple[1];
      HibUtils.evict(null, session,currMemberDAO, false);
      
      currMembershipDAO.setMemberDAO(currMemberDAO);
      memberships.add(MembershipDTO.getDTO(currMembershipDAO));
    }
    return memberships;
      

  } // private Set<MembershipDAO> _getMembershipsFromMembershipAndmemberQuery(qry)


} 

