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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.ByObject;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
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
 * @version $Id: Hib3MembershipDAO.java,v 1.24.2.1 2009-04-07 16:21:08 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3MembershipDAO extends Hib3DAO implements MembershipDAO {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3MembershipDAO.class);

  // PRIVATE CLASS CONSTANTS //
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
   * @since   @HEAD@ 
   */
  public boolean exists(String ownerUUID, String memberUUID, String listName, String msType)
    throws  GrouperDAOException {
    Object id = HibernateSession.byHqlStatic()
      .createQuery("select ms.id from Membership as ms, Field as field where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  ms.type       = :type             "
        )
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
  public Set<Membership> findAllByCreatedAfter(Date d, Field f) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery("select ms, m from Membership as ms,Member as m, Field as field where  "
        + "     ms.createTimeLong > :time             "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
      .setLong(   "time",  d.getTime()            )
      .setString( "fname", f.getName()            )
      .setString( "ftype", f.getType().toString() ).listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByCreatedBefore(Date d, Field f) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.createTimeLong < :time             "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
      .setLong(   "time",  d.getTime()            )
      .setString( "fname", f.getName()            )
      .setString( "ftype", f.getType().toString() ).listSet(Object[].class);
	  
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
    
  } 

  /**
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
   * @since   @HEAD@
   */
  public Set<Membership> findAllByMemberAndVia(String memberUUID, String viaUUID) 
    throws  GrouperDAOException {

	  Set<Object[]> mships =  HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.viaUuid     = :via             "
        + "and  ms.memberUuid  = m.uuid         "
      )
      .setString( "member", memberUUID )
      .setString( "via",    viaUUID    ).listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByOwnerAndField(String ownerUUID, Field f) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.ownerUuid   = :owner            "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwnerAndField")
      .setString( "owner", ownerUUID                )
      .setString( "fname", f.getName()            )
      .setString( "ftype", f.getType().toString() ) 
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByOwnerAndFieldAndType(String ownerUUID, Field f, String type) 
    throws  GrouperDAOException {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery("select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.ownerUuid   = :owner            "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.type = :type             "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMembershipsByType")
      .setString( "owner" , ownerUUID                 )
      .setString( "fname" , f.getName()             )
      .setString( "ftype" , f.getType().toString()  )
      .setString( "type"  , type                    )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f) 
    throws  GrouperDAOException {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.ownerUuid   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.memberUuid  = m.uuid         ")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .listSet(Object[].class);
       return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @see     MembershipDAO#findAllMembersByOwnerAndField(String, Field)
   * @since   @HEAD@
   */
  public Set<Member> findAllMembersByOwnerAndField(String ownerUUID, Field f)
    throws  GrouperDAOException
  {
    return findAllMembersByOwnerAndField(ownerUUID, f, null);
  }

  /**
   * @see     MembershipDAO#findAllMembersByOwnerAndField(String, Field, QueryOptions)
   */
  public Set<Member> findAllMembersByOwnerAndField(String ownerUUID, Field f, QueryOptions queryOptions)
    throws  GrouperDAOException
  {
    return HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery(
          "select m"
        + " from Member m, Membership ms where"
        + " ms.ownerUuid      = :owner "
        + "and  ms.fieldId = :fieldId "
        + " and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembersByOwnerAndField")
      .setString( "owner", ownerUUID ) 
      .setString( "fieldId", f.getUuid() )
      .listSet(Member.class);
  }

  /**
   * @see     MembershipDAO#findAllMembersByOwnerAndField(String, Field, QueryOptions)
   */
  public Set<Member> findAllMembersByOwnerAndFieldAndType(String ownerUUID, Field f, String type, QueryOptions queryOptions)
    throws  GrouperDAOException {
    String query = "select m"
      + " from Member m, Membership ms where"
      + " ms.ownerUuid      = :owner "
      + "and  ms.fieldId = :fieldId "
      + " and ms.memberUuid = m.uuid" 
      +	" and ms.type = :type";
    if (LOG.isDebugEnabled()) {
      LOG.debug("Before query: " + query + ", owner: " + ownerUUID + ", field: " + f.getUuid() + ", type: " + type);
    }
    try {
      return HibernateSession.byHqlStatic().options(queryOptions)
        .createQuery(
            query)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllMembersByOwnerAndField")
        .setString( "owner", ownerUUID ) 
        .setString( "fieldId", f.getUuid() )
        .setString( "type", type )
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
   * @since   @HEAD@
   */
  public Membership findByOwnerAndMemberAndFieldAndType(String ownerUUID, String memberUUID, Field f, String type)
    throws  GrouperDAOException,
            MembershipNotFoundException {
    Object[] result = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.type       = :type             "
        + "and  ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByOwnerAndMemberAndFieldAndType")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() ) 
      .setString( "type",   type                   )
      .uniqueResult(Object[].class);
    if (result==null || result[0]==null) {
      throw new MembershipNotFoundException();
    }
    Membership ms = (Membership)result[0];
    Member m = (Member)result[1];
    ms.setMember(m);
    return ms;
  } 

  /**
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
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffective(String ownerUUID, String memberUUID, Field f, String viaUUID, int depth) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.type       = :type             "
        + "and  ms.viaUuid    = :via              "
        + "and  ms.depth      = :depth            "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffective")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.EFFECTIVE   )
      .setString( "via",    viaUUID                )
      .setInteger("depth",  depth                  )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.memberUuid  = :member          "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.type = :type                   "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByMemberAndField")
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.EFFECTIVE   )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByOwnerAndMemberAndField(String ownerUUID, String memberUUID, Field f)
    throws  GrouperDAOException {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.ownerUuid  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.type       = :type             "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllEffectiveByOwnerAndMemberAndField")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.EFFECTIVE   )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 
  
  /**
   * @since   1.2.1
   */
  public Set<Membership> findAllByOwnerAndMember(String ownerUUID, String memberUUID) 
    throws  GrouperDAOException {
    
    //Added by Gary Brown 2007-11-01 so that getPrivs can do one query rather than 6

	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerUuid   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and ms.memberUuid = m.uuid"
      ).setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwnerAndMemberAndField")
      .setString( "owner",  ownerUUID              )
      .setString( "member", memberUUID             )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
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
   * @since   @HEAD@
   */
  public List<Membership> findAllByOwnerAsList(String ownerUUID)
    throws  GrouperDAOException
  {
	  List<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where ms.ownerUuid = :owner "
      + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByOwner")
      .setString("owner", ownerUUID)
      .list(Object[].class);
    return _getMembershipsFromMembershipAndMemberQueryAsList(mships);
  }


  /**
   * @since   @HEAD@
   */
  public List<Membership> findAllMembershipsWithInvalidOwners()
    throws  GrouperDAOException
  {
	  List<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m where  "
        + "     ms.ownerUuid not in " 
        + "        (select g.uuid from Group g) "
        + "     and ms.ownerUuid not in "
        + "        (select ns.uuid from Stem ns)"
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembershipsWithInvalidOwners")
      .list(Object[].class);
	  return new ArrayList<Membership>(_getMembershipsFromMembershipAndMemberQuery(mships));
  }

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findAllImmediateByMemberAndField(String memberUUID, Field f) 
    throws  GrouperDAOException
  {
	  Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and  ms.type       = :type             "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField")
      .setString( "member", memberUUID             )
      .setString( "fname",  f.getName()            )
      .setString( "ftype",  f.getType().toString() )
      .setString( "type",   Membership.IMMEDIATE   )
      .listSet(Object[].class);
	  return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @since   @HEAD@
   */
  public Membership findByUuid(String uuid) 
    throws  GrouperDAOException,
            MembershipNotFoundException 
  {
    Object[] result = HibernateSession.byHqlStatic()
      .createQuery("select ms, m from Membership as ms, Member as m where ms.uuid = :uuid "
    		     + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByUuid")
      .setString("uuid", uuid)
      .uniqueResult(Object[].class);
    if (result==null || result[0] == null) {
      throw new MembershipNotFoundException("could not find membership with uuid: " + Quote.single(uuid));
    }
    Membership ms = (Membership)result[0];
    Member m = (Member)result[1];
    ms.setMember(m);
    return ms;
  } 

  /**
   * @since   @HEAD@
   */
  public Set<Membership> findMembershipsByMemberAndField(String memberUUID, Field f)
    throws  GrouperDAOException
  {
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(
        "select ms, m from Membership as ms, Member as m, Field as field where  "
        + "     ms.memberUuid = :member           "
        + "and  ms.fieldId = field.uuid "
        + "and  field.name   = :fname            "
        + "and  field.typeString       = :ftype             "
        + "and ms.memberUuid = m.uuid")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindMemberships")
      .setString( "member", memberUUID             )
      .setString( "fname" , f.getName()            )
      .setString( "ftype" , f.getType().toString() )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param mof 
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public void update(final DefaultMemberOf mof) 
    throws  GrouperDAOException {
    // TODO 20070404 this is incredibly ugly
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        new HibernateHandler() {

          public Object callback(HibernateSession hibernateSession) {
            
            ByObject byObject = hibernateSession.byObject();
            byObject.delete(mof.getDeletes());

            byObject.saveOrUpdate(mof.getSaves());

            byObject.saveOrUpdate(mof.getModifiedGroups());
            
            byObject.saveOrUpdate(mof.getModifiedStems());
            
            return null;
          }
      
    });
  } 


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
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
  
//@since 1.4.0
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
      

  } // private Set<MembershipDAO> _getMembershipsFromMembershipAndmemberQuery(Set<Object[]>)

//@since 1.4.0
  private List<Membership> _getMembershipsFromMembershipAndMemberQueryAsList(Collection<Object[]> mships)
    throws  HibernateException
  {
    List<Membership> memberships = new ArrayList<Membership>();
    
    for(Object[] tuple:mships) {
      Membership currMembership = (Membership)tuple[0];
      Member currMember = (Member)tuple[1];
      currMembership.setMember(currMember);
      memberships.add(currMembership);
    }
    return memberships;
      

  } // private List<MembershipDAO> _getMembershipsFromMembershipAndmemberQueryAsList(Collection<Object[]>)

} 

 

