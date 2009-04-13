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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.misc.DefaultMemberOf;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Basic Hibernate <code>Membership</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3MembershipDAO.java,v 1.33 2009-04-13 16:53:08 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3MembershipDAO extends Hib3DAO implements MembershipDAO {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3MembershipDAO.class);

  /** */
  private static final String KLASS = Hib3MembershipDAO.class.getName();

  /**
   * find all memberships that have this member or have this creator
   * @param member
   * @return the memberships
   */
  public Set<Membership> findAllByCreatorOrMember(Member member) {
    if (member == null || StringUtils.isBlank(member.getUuid())) {
      throw new RuntimeException("Need to pass in a member");
    }
    Set<Membership> memberships = HibernateSession.byHqlStatic()
      .createQuery("select distinct m from Membership as m where m.creatorUuid = :uuid1 or m.memberUuid = :uuid2")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByCreatorOrMember")
      .setString( "uuid1", member.getUuid() ).setString("uuid2", member.getUuid())
      .listSet(Membership.class);
    return memberships;

  }

  /**
   * <p/>
   * @param ownerGroupId 
   * @param memberUUID 
   * @param listName 
   * @param msType 
   * @return if exists
   * @throws GrouperDAOException 
   * @since   @HEAD@ 
   */
  public boolean existsByGroupOwner(String ownerGroupId, String memberUUID, String listName, String msType)
    throws  GrouperDAOException {
    Object id = HibernateSession.byHqlStatic()
      .createQuery("select ms.id from Membership as ms, Field as field where  "
        + "     ms.ownerGroupId  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  ms.type       = :type             "
        )
      .setCacheable(false)
      .setCacheRegion(KLASS + ".ExistsByGroupOwner")
      .setString( "owner",  ownerGroupId  )
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
   * <p/>
   * @param ownerStemId 
   * @param memberUUID 
   * @param listName 
   * @param msType 
   * @return if exists
   * @throws GrouperDAOException 
   * @since   @HEAD@ 
   */
  public boolean existsByStemOwner(String ownerStemId, String memberUUID, String listName, String msType)
    throws  GrouperDAOException {
    Object id = HibernateSession.byHqlStatic()
      .createQuery("select ms.id from Membership as ms, Field as field where  "
        + "     ms.ownerStemId  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  ms.type       = :type             "
        )
      .setCacheable(false)
      .setCacheRegion(KLASS + ".ExistsByStemOwner")
      .setString( "owner",  ownerStemId  )
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
   * @param d 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByCreatedAfter(Date d, Field f) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery("select ms, m from Membership as ms,Member as m where  "
        + "     ms.createTimeLong > :time             "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
      .setLong(   "time",  d.getTime()            )
      .setString( "fuuid", f.getUuid()            )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param d 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByCreatedBefore(Date d, Field f) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.createTimeLong < :time             "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
      .setLong(   "time",  d.getTime()            )
      .setString( "fuuid", f.getUuid()            )
      .listSet(Object[].class);
	  
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
    
  } 

  /**
   * @param memberUUID 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByMember(String memberUUID) 
    throws  GrouperDAOException {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery("select ms, m from Membership as ms, Member as m where ms.memberUuid = :member "
    		     + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByMember")
      .setString("member", memberUUID)
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param memberUUID 
   * @param viaGroupId 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByMemberAndViaGroup(String memberUUID, String viaGroupId) 
    throws  GrouperDAOException {

	  Set<Object[]> mships =  HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.viaGroupId     = :via             "
        + "and  ms.memberUuid  = m.uuid         "
      )
      .setString( "member", memberUUID )
      .setString( "via",    viaGroupId    ).listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByGroupOwnerAndField(String ownerGroupId, Field f) 
    throws  GrouperDAOException {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerGroupId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByGroupOwnerAndField")
      .setString( "owner", ownerGroupId                )
      .setString( "fuuid", f.getUuid()            )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerStemId 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByStemOwnerAndField(String ownerStemId, Field f) 
    throws  GrouperDAOException {
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerStemId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByStemOwnerAndField")
      .setString( "owner", ownerStemId                )
      .setString( "fuuid" , f.getUuid()             )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerStemId 
   * @param f 
   * @param type 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByStemOwnerAndFieldAndType(String ownerStemId, Field f, String type) 
    throws  GrouperDAOException {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery("select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerStemId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.type = :type             "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMembershipsByStemOwnerType")
      .setString( "owner" , ownerStemId                 )
      .setString( "fuuid" , f.getUuid()             )
       .setString( "type"  , type                    )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param f 
   * @param type 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByGroupOwnerAndFieldAndType(String ownerGroupId, Field f, String type) 
    throws  GrouperDAOException {
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery("select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerGroupId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.type = :type             "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMembershipsByGroupOwnerType")
      .setString( "owner" , ownerGroupId                 )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "type"  , type                    )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByStemOwnerAndMemberAndField(String ownerStemId, String memberUUID, Field f) 
    throws  GrouperDAOException {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerStemId   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByStemOwnerAndMemberAndField")
      .setString( "owner",  ownerStemId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .listSet(Object[].class);
       return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByGroupOwnerAndMemberAndField(String ownerGroupId, String memberUUID, Field f) 
    throws  GrouperDAOException {
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.ownerGroupId   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByGroupOwnerAndMemberAndField")
      .setString( "owner",  ownerGroupId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .listSet(Object[].class);
       return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 
  
  /**
   * @param ownerGroupId 
   * @param f 
   * @param type 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Member> findAllMembersByGroupOwnerAndFieldAndType(String ownerGroupId, Field f, String type) 
    throws  GrouperDAOException {
    return HibernateSession.byHqlStatic()
    .createQuery(
        "select m "
      + "from Member m, Membership ms where "
      + "ms.ownerGroupId = :owner "
        + "and  ms.fieldId = :fieldId "
      + "and ms.type = :type "
      + "and ms.memberUuid = m.uuid ")
    .setCacheable(false)
    .setCacheRegion(KLASS + ".FindAllMembersByGroupOwnerAndFieldTypeAndType")
    .setString("owner", ownerGroupId)
    .setString( "fieldId", f.getUuid() )
    .setString("type", type)
    .listSet(Member.class);
  } 
  
  /**
   * @param ownerStemId 
   * @param f 
   * @param type 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Member> findAllMembersByStemOwnerAndFieldAndType(String ownerStemId, Field f, String type) 
    throws  GrouperDAOException {
    return HibernateSession.byHqlStatic()
    .createQuery(
        "select m "
      + "from Member m, Membership ms where "
      + "ms.ownerStemId = :owner "
      + "and  ms.fieldId = :fieldId "
      + "and ms.type = :type "
      + "and ms.memberUuid = m.uuid ")
    .setCacheable(false)
    .setCacheRegion(KLASS + ".FindAllMembersByStemOwnerAndFieldTypeAndType")
    .setString("owner", ownerStemId)
    .setString( "fieldId", f.getUuid() )
    .setString("type", type)
    .listSet(Member.class);
  } 

  /**
   * @see     MembershipDAO#findAllMembersByGroupOwnerAndField(String, Field)
   * @since   @HEAD@
   */
  public Set<Member> findAllMembersByGroupOwnerAndField(String groupOwnerId, Field f)
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery(
          "select m"
        + " from Member m, Membership ms where"
        + " ms.ownerGroupId      = :owner "
        + "and  ms.fieldId = :fieldId "
        + " and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembersByGroupOwnerAndField")
      .setString( "owner", groupOwnerId ) 
      .setString( "fieldId", f.getUuid() )
      .listSet(Member.class);
    } finally {
      if (LOG.isDebugEnabled()) {
        LOG.debug("After query: " + query + ", owner: " + ownerUUID + ", field: " + f.getUuid() + ", type: " + type);
      }
    }
  }

  /** batch size for memberships (setable for testing) */
  static int batchSize = 50;

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByOwnerAndFieldAndMembers(java.lang.String, edu.internet2.middleware.grouper.Field, java.util.Collection)
   */
  public Set<Membership> findAllByOwnerAndFieldAndMembersAndType(String ownerUUID,
      Field f, Collection<Member> members, String type) throws GrouperDAOException {
    
    if (members == null) {
      return null;
    }
    if (members.size() == 0) {
      return new LinkedHashSet<Membership>();
    }

    //lets page through these
    int pages = GrouperUtil.batchNumberOfBatches(members, batchSize);
    
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    for (int i=0; i<pages; i++) {
      List<Member> memberList = GrouperUtil.batchList(members, batchSize, i);
      
      List<String> uuids = GrouperUtil.propertyList(memberList, Member.PROPERTY_UUID, String.class);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select ms"
          + " from Membership ms where"
          + " ms.ownerUuid      = :owner "
          + "and  ms.fieldId = :fieldId "
          + "and  ms.type = :theType "
          + " and ms.memberUuid in (");
      byHqlStatic.setString( "owner", ownerUUID ) 
        .setString( "fieldId", f.getUuid() )
        .setString( "theType", type );
      //add all the uuids
      byHqlStatic.setCollectionInClause(query, uuids);
      query.append(")");
      List<Membership> currentList = byHqlStatic.createQuery(query.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByOwnerAndFieldAndMembers")
        .list(Membership.class);
      memberships.addAll(currentList);
    }
    return memberships;
      
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByOwnerAndFieldAndMembers(java.lang.String, edu.internet2.middleware.grouper.Field, java.util.Collection)
   */
  public Set<Membership> findAllByOwnerAndFieldAndMembers(String ownerUUID,
      Field f, Collection<Member> members) throws GrouperDAOException {
    
    if (members == null) {
      return null;
    }
    if (members.size() == 0) {
      return new LinkedHashSet<Membership>();
    }

    //lets page through these
    int pages = GrouperUtil.batchNumberOfBatches(members, batchSize);
    
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    for (int i=0; i<pages; i++) {
      List<Member> memberList = GrouperUtil.batchList(members, batchSize, i);
      
      List<String> uuids = GrouperUtil.propertyList(memberList, Member.PROPERTY_UUID, String.class);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select ms"
          + " from Membership ms where"
          + " ms.ownerUuid      = :owner "
          + "and  ms.fieldId = :fieldId "
          + " and ms.memberUuid in (");
      byHqlStatic.setString( "owner", ownerUUID ) 
        .setString( "fieldId", f.getUuid() );
      //add all the uuids
      byHqlStatic.setCollectionInClause(query, uuids);
      query.append(")");
      List<Membership> currentList = byHqlStatic.createQuery(query.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByOwnerAndFieldAndMembers")
        .list(Membership.class);
      memberships.addAll(currentList);
    }
    return memberships;
      
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByOwnerAndCompositeAndMembers(java.lang.String, java.util.Collection)
   */
  public Set<Membership> findAllByOwnerAndCompositeAndMembers(String ownerUUID,
      Collection<Member> members) throws GrouperDAOException {
    
    if (members == null) {
      return null;
    }
    if (members.size() == 0) {
      return new LinkedHashSet<Membership>();
    }
    
    //lets page through these
    int pages = GrouperUtil.batchNumberOfBatches(members, batchSize);
    
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    for (int i=0; i<pages; i++) {
      List<Member> memberList = GrouperUtil.batchList(members, batchSize, i);
      
      List<String> uuids = GrouperUtil.propertyList(memberList, Member.PROPERTY_UUID, String.class);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select ms"
          + " from Membership ms where"
          + " ms.ownerUuid      = :owner "
          + "and  ms.type = 'composite' "
          + " and ms.memberUuid in (");
      byHqlStatic.setString( "owner", ownerUUID ) ;
      //add all the uuids
      byHqlStatic.setCollectionInClause(query, uuids);
      query.append(")");
      List<Membership> currentList = byHqlStatic.createQuery(query.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByOwnerAndCompositeAndMembers")
        .list(Membership.class);
      memberships.addAll(currentList);
    }
    return memberships;
      
  }

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param type 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   * @since   @HEAD@
   * @deprecated
   */
  @Deprecated 
  public Membership findByGroupOwnerAndMemberAndFieldAndType(String ownerGroupId, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException {
    return findByGroupOwnerAndMemberAndFieldAndType(ownerGroupId, memberUUID, f, type, true);
  }

  /**
   * @param ownerGroupId
   * @param memberUUID
   * @param f
   * @param type
   * @param exceptionIfNull 
   */
  public Membership findByGroupOwnerAndMemberAndFieldAndType(String ownerGroupId,
      String memberUUID, Field f, String type, boolean exceptionIfNull)
      throws GrouperDAOException, MembershipNotFoundException {
    Object[] result = HibernateSession.byHqlStatic().createQuery(
        "select ms, m from Membership as ms, Member as m where  "
            + "     ms.ownerGroupId  = :owner            "
            + "and  ms.memberUuid = :member           " 
            + "and  ms.fieldId = :fuuid "
            + "and  ms.type       = :type             " + "and  ms.memberUuid = m.uuid")
        .setCacheable(false).setCacheRegion(
            KLASS + ".FindByGroupOwnerAndMemberAndFieldAndType").setString("owner",
            ownerGroupId).setString("member", memberUUID)      
            .setString( "fuuid",  f.getUuid()            )
            .setString("type", type).uniqueResult(
            Object[].class);
    if (result == null || result[0] == null) {
      if (exceptionIfNull) {
        throw new MembershipNotFoundException();
      }
      return null;
    }
    Membership ms = (Membership) result[0];
    Member m = (Member) result[1];
    ms.setMember(m);
    return ms;
  }


  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @param type 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   * @since   @HEAD@
   * @deprecated
   */
  @Deprecated
  public Membership findByStemOwnerAndMemberAndFieldAndType(String ownerStemId, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException {
    return findByStemOwnerAndMemberAndFieldAndType(ownerStemId, memberUUID, f, type, true);
  } 

  /**
   * @param ownerStemId
   * @param memberUUID
   * @param f
   * @param type
   * @param exceptionIfNull
   */
  public Membership findByStemOwnerAndMemberAndFieldAndType(String ownerStemId,
      String memberUUID, Field f, String type, boolean exceptionIfNull)
      throws GrouperDAOException, MembershipNotFoundException {
    Object[] result = HibernateSession.byHqlStatic()
    .createQuery(
      "select ms, m from Membership as ms, Member as m, Field as field where  "
      + "     ms.ownerStemId  = :owner            "
      + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = :fuuid "
      + "and  ms.type       = :type             "
      + "and  ms.memberUuid = m.uuid")
    .setCacheable(false)
    .setCacheRegion(KLASS + ".FindByStemOwnerAndMemberAndFieldAndType")
    .setString( "owner",  ownerStemId              )
    .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
    .setString( "type",   type                   )
    .uniqueResult(Object[].class);
  if (result==null || result[0]==null) {
    if (exceptionIfNull) {
      throw new MembershipNotFoundException();
    } 
    return null;
  }
  Membership ms = (Membership)result[0];
  Member m = (Member)result[1];
  ms.setMember(m);
  return ms;
  }


  /**
   * @param _ms 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllChildMemberships(Membership _ms) 
    throws  GrouperDAOException
  {
    Set<Object[]> mships =  HibernateSession.byHqlStatic()
      .createQuery("select ms, m from Membership as ms, Member as m where ms.parentUuid = :uuid "
    		     + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindChildMemberships")
      .setString( "uuid", _ms.getUuid() )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param viaGroupId 
   * @param depth 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByGroupOwner(String ownerGroupId, String memberUUID, Field f, String viaGroupId, int depth) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerGroupId  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.type       = :type             "
        + "and  ms.viaGroupId    = :via              "
        + "and  ms.depth      = :depth            "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByGroupOwner")
      .setString( "owner",  ownerGroupId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "type",   Membership.EFFECTIVE   )
      .setString( "via",    viaGroupId                )
      .setInteger("depth",  depth                  )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @param viaGroupId 
   * @param depth 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByStemOwner(String ownerStemId, String memberUUID, Field f, String viaGroupId, int depth) 
    throws  GrouperDAOException
  {
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.ownerStemId  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.type       = :type             "
        + "and  ms.viaGroupId    = :via              "
        + "and  ms.depth      = :depth            "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByStemOwner")
      .setString( "owner",  ownerStemId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "type",   Membership.EFFECTIVE   )
      .setString( "via",    viaGroupId                )
      .setInteger("depth",  depth                  )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.type = :type                   "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByMemberAndField")
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "type",   Membership.EFFECTIVE   )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByGroupOwnerAndMemberAndField(String ownerGroupId, String memberUUID, Field f)
    throws  GrouperDAOException {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.ownerGroupId  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.type       = :type             "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByGroupOwnerAndMemberAndField")
      .setString( "owner",  ownerGroupId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "type",   Membership.EFFECTIVE   )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 
  
  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  public Set<Membership> findAllByGroupOwnerAndMember(String ownerGroupId, String memberUUID) 
    throws  GrouperDAOException {
    
    //Added by Gary Brown 2007-11-01 so that getPrivs can do one query rather than 6

	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerGroupId   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and ms.memberUuid = m.uuid"
      ).setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByGroupOwnerAndMemberAndField")
      .setString( "owner",  ownerGroupId              )
      .setString( "member", memberUUID             )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @param memberUUID 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllImmediateByMember(String memberUUID) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.type       = :type             "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllImmediateByMember")
      .setString( "member", memberUUID             )
      .setString( "type",   Membership.IMMEDIATE   )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @return list
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public List<Membership> findAllByGroupOwnerAsList(String ownerGroupId)
    throws  GrouperDAOException
  {
	  List<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where ms.ownerGroupId = :owner "
      + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByGroupOwner")
      .setString("owner", ownerGroupId)
      .list(Object[].class);
    return _getMembershipsFromMembershipAndMemberQueryAsList(mships);
  }

  /**
   * @param ownerStemId 
   * @return list
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public List<Membership> findAllByStemOwnerAsList(String ownerStemId)
    throws  GrouperDAOException
  {
    List<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where ms.ownerStemId = :owner "
      + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByStemOwner")
      .setString("owner", ownerStemId)
      .list(Object[].class);
    return _getMembershipsFromMembershipAndMemberQueryAsList(mships);
  }


  /**
   * @return list
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public List<Membership> findAllMembershipsWithInvalidOwners()
    throws  GrouperDAOException
  {
	  List<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerGroupId not in " 
        + "        (select g.uuid from Group g) "
        + "     or ms.ownerStemId not in "
        + "        (select ns.uuid from Stem ns)"
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembershipsWithInvalidOwners")
      .list(Object[].class);
	  return new ArrayList<Membership>(_getMembershipsFromMembershipAndMemberQuery(mships));
  }

  /**
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllImmediateByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.type       = :type             "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField")
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "type",   Membership.IMMEDIATE   )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param memberUUID 
   * @param fieldType
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllImmediateByMemberAndFieldType(String memberUUID, String fieldType) 
    throws  GrouperDAOException
  {
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.typeString       = :ftype             "
        + "and  ms.type       = :type             "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllImmediateByMemberAndFieldType")
      .setString( "member", memberUUID             )
      .setString( "ftype",  fieldType )
      .setString( "type",   Membership.IMMEDIATE   )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param uuid 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   * @deprecated
   */
  @Deprecated
  public Membership findByUuid(String uuid) 
    throws  GrouperDAOException, MembershipNotFoundException {
    return findByUuid(uuid, true);
  } 

  /**
   * @param uuid
   * @param exceptionIfNull
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   */
  public Membership findByUuid(String uuid, boolean exceptionIfNull)
      throws GrouperDAOException, MembershipNotFoundException {
    Object[] result = HibernateSession.byHqlStatic()
      .createQuery("select ms, m from Membership as ms, Member as m where ms.uuid = :uuid "
             + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid)
      .uniqueResult(Object[].class);
    if (result==null || result[0] == null) {
      if (exceptionIfNull) {
        throw new MembershipNotFoundException("could not find membership with uuid: " + Quote.single(uuid));
      }
      return null;
    }
    Membership ms = (Membership)result[0];
    Member m = (Member)result[1];
    ms.setMember(m);
    return ms;
    
  }


  
  /**
   * @param memberUUID 
   * @param f 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findMembershipsByMemberAndField(String memberUUID, Field f)
    throws  GrouperDAOException {
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMemberships")
      .setString( "member", memberUUID             )
      .setString( "fuuid" , f.getUuid()            )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findMembershipsByMemberAndFieldSecure(edu.internet2.middleware.grouper.GrouperSession, java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public Set<Membership> findMembershipsByMemberAndFieldSecure(GrouperSession grouperSession, 
        String memberUUID, Field f)
      throws  GrouperDAOException {
    
    StringBuilder sql = new StringBuilder("select distinct ms, m from Membership as ms, Member as m ");
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    
    //see if we are adding more to the query
    boolean changedQuery = false;
    boolean securableField = FieldType.ACCESS.equals(f.getType()) || FieldType.LIST.equals(f.getType());
    
    if (securableField) {
      changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSession.getSubject(), byHqlStatic, 
          sql, "ms.ownerUuid", AccessPrivilege.VIEW_PRIVILEGES);
    }

    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
    }

    sql.append("     ms.memberUuid = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and ms.memberUuid = m.uuid");
    
    Set<Object[]> mships = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMemberships")
      .setString( "member", memberUUID)
      .setString( "fuuid" , f.getUuid())
      .listSet(Object[].class);
    Set<Membership> memberships = _getMembershipsFromMembershipAndMemberQuery(mships);

    Member member = null;
    Subject subject = null;
    try {
      member = MemberFinder.findByUuid(grouperSession, memberUUID);
      subject = member.getSubject();
    } catch (SubjectNotFoundException snfe) {
      throw new RuntimeException("Cant find subject: " + memberUUID, snfe);
    } catch (MemberNotFoundException mnfe) {
      throw new RuntimeException("Cant find member: " + memberUUID, mnfe);
    }
    
    //maybe we need to filter these out
    //if the hql didnt filter, this will
    Set<Membership> filteredMemberships = memberships;
    
    //see if the access resolver can help
    if (securableField) {
      filteredMemberships = grouperSession.getAccessResolver().postHqlFilterMemberships(subject, memberships);
    } else {
      //just manually do this
      filteredMemberships = PrivilegeHelper.canViewMemberships(grouperSession, memberships);
    }
      
    return filteredMemberships;
  } 

  /**
   * @param mof 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void update(final DefaultMemberOf mof) 
    throws  GrouperDAOException {
    // TODO 20070404 this is incredibly ugly
    HibernateSession.callbackHibernateSession(
        GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, AuditControl.WILL_NOT_AUDIT,
        new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {
            HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();
            
            ByObject byObject = hibernateSession.byObject();
            byObject.delete(mof.getDeletes());
            
            byObject.saveOrUpdate(mof.getSaves());
            hibernateSession.misc().flush();
            try { 
              byObject.saveOrUpdate(mof.getModifiedGroups());
            } catch (GrouperDAOException gde) {
              throw gde;
            }
            
            byObject.saveOrUpdate(mof.getModifiedStems());
            
            long now = System.currentTimeMillis();
            
            if (GrouperConfig.getPropertyBoolean("groups.updateLastMembershipTime", false)) {
              Iterator<String> membershipChanges = mof.getGroupIdsWithNewMemberships()
                  .iterator();
              while (membershipChanges.hasNext()) {
                String groupId = membershipChanges.next();

                HibernateSession.bySqlStatic().executeSql(
                    "update grouper_groups set last_membership_change = ? where id = ?",
                    GrouperUtil.toList((Object) now, groupId));
              }
            }
            
            if (GrouperConfig.getPropertyBoolean("stems.updateLastMembershipTime", false)) {
              Iterator<String> membershipChanges = mof.getStemIdsWithNewMemberships()
                  .iterator();
              while (membershipChanges.hasNext()) {
                String stemId = membershipChanges.next();

                HibernateSession.bySqlStatic().executeSql(
                    "update grouper_stems set last_membership_change = ? where id = ?",
                    GrouperUtil.toList((Object) now, stemId));
              }
            }
            return null;
          }
      
    });
  } 

  /**
   * @param hibernateSession
   * @throws HibernateException
   */
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    Session hs = hibernateSession.getSession();
    
    hs.createQuery("update Membership set parentUuid = null").executeUpdate();
    
    List<Membership> memberships = 
      hs.createQuery("from Membership as ms order by createTimeLong desc")
      .list()
      ;

    // Deleting each membership from the time created in descending order. 
    // This is necessary to prevent deleting parent memberships before child 
    // memberships which causes integrity constraint violations on some databases. 
    for (Membership membership : memberships) {
      hs.createQuery("delete from Membership ms where ms.uuid=:uuid")
      .setString("uuid", membership.getUuid())
      .executeUpdate();
    }

  }
  
  /**
   * 
   * @param mships
   * @return set
   * @throws HibernateException
   */
  private Set<Membership> _getMembershipsFromMembershipAndMemberQuery(Collection<Object[]> mships)
    throws  HibernateException
  {
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    for(Object[] tuple:mships) {
      Membership currMembership = (Membership)tuple[0];
      Member currMember = (Member)tuple[1];
      currMembership.setMember(currMember);
      memberships.add(currMembership);
    }
    return memberships;
      

  } 

  /**
   * 
   * @param mships
   * @return list
   * @throws HibernateException
   */
  private List<Membership> _getMembershipsFromMembershipAndMemberQueryAsList(Collection<Object[]> mships)
    throws  HibernateException {
    List<Membership> memberships = new ArrayList<Membership>();
    
    for(Object[] tuple:mships) {
      Membership currMembership = (Membership)tuple[0];
      Member currMember = (Member)tuple[1];
      currMembership.setMember(currMember);
      memberships.add(currMembership);
    }
    return memberships;
      

  }

} 

 

