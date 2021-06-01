/**
 * Copyright 2014 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.Session;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.member.SearchStringEnum;
import edu.internet2.middleware.grouper.member.SortStringEnum;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.service.ServiceRole;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * Basic Hibernate <code>Membership</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3MembershipDAO.java,v 1.52 2009-12-17 06:57:57 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3MembershipDAO extends Hib3DAO implements MembershipDAO {

  /** logger */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3MembershipDAO.class);

  /** */
  private static final String KLASS = Hib3MembershipDAO.class.getName();

  /**
   * get all memberships
   * @param enabledOnly
   * @return set
   */
  public Set<Membership> findAll(boolean enabledOnly) {
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where "
        + "ms.memberUuid  = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 
  
  /**
   * find all memberships that have this member or have this creator
   * @param member
   * @param enabledOnly
   * @return the memberships
   */
  public Set<Membership> findAllByCreatorOrMember(Member member, boolean enabledOnly) {
    if (member == null || StringUtils.isBlank(member.getUuid())) {
      throw new RuntimeException("Need to pass in a member");
    }
    
    StringBuilder sql = new StringBuilder("select distinct m from MembershipEntry as m where " +
        " (m.creatorUuid = :uuid or m.memberUuid = :uuid or m.groupSetCreatorUuid = :uuid) ");
    if (enabledOnly) {
      sql.append(" and m.enabledDb = 'T'");
    }
    
    Set<Membership> memberships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("uuid", member.getUuid())
      .listSet(Membership.class);
    return memberships;

  }

  /**
   * @param d 
   * @param f 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByCreatedAfter(Date d, Field f, boolean enabledOnly) 
    throws  GrouperDAOException
  {

    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + "  (   ms.createTimeLong > :time   or    ms.groupSetCreateTimeLong > :time   )      "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid  ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setLong(   "time",  d.getTime()            )
      .setString( "fuuid", f.getUuid()            )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param d 
   * @param f 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByCreatedBefore(Date d, Field f, boolean enabledOnly) 
    throws  GrouperDAOException
  {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + "  (   ms.createTimeLong < :time   and    ms.groupSetCreateTimeLong < :time   )      "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid  ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setLong(   "time",  d.getTime()            )
      .setString( "fuuid", f.getUuid()            )
      .listSet(Object[].class);
    
    return _getMembershipsFromMembershipAndMemberQuery(mships);
    
  } 

  /**
   * @param memberUUID 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByMember(String memberUUID, boolean enabledOnly) 
    throws  GrouperDAOException {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where ms.memberUuid = :member "
             + "and  ms.memberUuid  = m.uuid  ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("member", memberUUID)
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param f 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByGroupOwnerAndField(String ownerGroupId, Field f, boolean enabledOnly) 
    throws  GrouperDAOException {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + "     ms.ownerGroupId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid  ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner", ownerGroupId                )
      .setString( "fuuid", f.getUuid()            )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerStemId 
   * @param f 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByStemOwnerAndField(String ownerStemId, Field f, boolean enabledOnly) 
    throws  GrouperDAOException {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + "     ms.ownerStemId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid  ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner", ownerStemId                )
      .setString( "fuuid" , f.getUuid()             )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerStemId 
   * @param f 
   * @param type 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByStemOwnerAndFieldAndType(String ownerStemId, Field f, String type, boolean enabledOnly) 
    throws  GrouperDAOException {
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
          + "     ms.ownerStemId   = :owner            "
          + "and  ms.fieldId = :fuuid "
          + "and  ms.memberUuid  = m.uuid         "
          + "and  ms.type " + membershipType.queryClause());
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
        .createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS)
        .setString( "owner" , ownerStemId                 )
        .setString( "fuuid" , f.getUuid()             )
        .listSet(Object[].class);   

    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param f 
   * @param type 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByGroupOwnerAndFieldAndType(String ownerGroupId, Field f, String type, boolean enabledOnly) 
    throws  GrouperDAOException {
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + "     ms.ownerGroupId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid         "
        + "and  ms.type  " + membershipType.queryClause());
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner" , ownerGroupId                 )
      .setString( "fuuid",  f.getUuid()            )
      .listSet(Object[].class);

    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 
  

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerAndFieldAndDepth(java.lang.String, edu.internet2.middleware.grouper.Field, int, boolean)
   */
  public Set<Membership> findAllByGroupOwnerAndFieldAndDepth(String ownerGroupId, Field f, int depth, boolean enabledOnly) {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where "
        + "ms.ownerGroupId = :owner "
        + "and ms.fieldId = :fuuid "
        + "and ms.memberUuid = m.uuid "
        + "and ms.depth = :depth ");
    
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("owner", ownerGroupId)
      .setString("fuuid", f.getUuid())
      .setInteger("depth", depth)
      .listSet(Object[].class);

    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerAndDepth(java.lang.String, int, boolean)
   */
  public Set<Membership> findAllByGroupOwnerAndDepth(String ownerGroupId, int depth, boolean enabledOnly) {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where "
        + "ms.ownerGroupId = :owner "
        + "and ms.memberUuid = m.uuid "
        + "and ms.depth = :depth ");
    
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("owner", ownerGroupId)
      .setInteger("depth", depth)
      .listSet(Object[].class);

    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }
  
  /**
   * (non-Javadoc)
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByMemberAndDepth(java.lang.String, int, boolean)
   */
  public Set<Membership> findAllByMemberAndDepth(String memberId, int depth, boolean enabledOnly) {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where "
        + "ms.memberUuid = :member "
        + "and ms.memberUuid = m.uuid "
        + "and ms.depth = :depth ");
    
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("member", memberId)
      .setInteger("depth", depth)
      .listSet(Object[].class);

    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @param ownerStemId 
   * @param memberUUID 
   * @param f 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByStemOwnerAndMemberAndField(String ownerStemId, String memberUUID, Field f, boolean enabledOnly) 
    throws  GrouperDAOException {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + "     ms.ownerStemId   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid   ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
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
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllByGroupOwnerAndMemberAndField(String ownerGroupId, String memberUUID, Field f, boolean enabledOnly) 
    throws  GrouperDAOException {
    
    StringBuilder sql = new StringBuilder("select distinct ms, m from MembershipEntry as ms, Member as m, Field as field where  "
        + "     ms.ownerGroupId   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid    ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(true)
      .setCacheRegion(KLASS)
      .setString( "owner",  ownerGroupId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .listSet(Object[].class);
       return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllMembersByGroupOwnerAndField(java.lang.String, edu.internet2.middleware.grouper.Field, boolean)
   */
  public Set<Member> findAllMembersByGroupOwnerAndField(String groupOwnerId, Field f, boolean enabledOnly)
    throws  GrouperDAOException
  {
    return findAllMembersByGroupOwnerAndField(groupOwnerId, f, null, enabledOnly);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllMembersByGroupOwnerAndField(java.lang.String, edu.internet2.middleware.grouper.Field, Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, boolean)
   */
  public Set<Member> findAllMembersByGroupOwnerAndField(String groupOwnerId, Field f, Set<Source> sources, QueryOptions queryOptions, boolean enabledOnly)
    throws  GrouperDAOException {
    StringBuilder sql = new StringBuilder("select distinct m"
        + " from Member m, MembershipEntry ms where"
        + " ms.ownerGroupId      = :owner "
        + "and  ms.fieldId = :fieldId "
        + " and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }

    if (sources != null && sources.size() > 0) {
      sql.append(" and m.subjectSourceIdDb in ").append(HibUtils.convertSourcesToSqlInString(sources));
    }
    if (queryOptions != null) {
      Hib3MemberDAO.massageMemberSortFields(queryOptions.getQuerySort());
    }
    return HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner", groupOwnerId ) 
      .setString( "fieldId", f.getUuid() )
      .listSet(Member.class);

  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllMembersByGroupOwnerAndField(java.lang.String, edu.internet2.middleware.grouper.Field, edu.internet2.middleware.grouper.internal.dao.QueryOptions, boolean)
   */
  public Set<Member> findAllMembersByGroupOwnerAndField(String groupOwnerId, Field f, QueryOptions queryOptions, boolean enabledOnly)
    throws  GrouperDAOException
  {
    return findAllMembersByGroupOwnerAndField(groupOwnerId, f, null, queryOptions, enabledOnly);
  }

  /**
   * @param ownerGroupId 
   * @param f 
   * @param type 
   * @param queryOptions 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Member> findAllMembersByGroupOwnerAndFieldAndType(
      String ownerGroupId, Field f, String type, QueryOptions queryOptions, boolean enabledOnly) {
    return findAllMembersByGroupOwnerAndFieldAndType(ownerGroupId, f, type, null, queryOptions, enabledOnly);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean)
   */
  @Override
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled) {
    
    return findAllByGroupOwnerOptions(totalGroupIds, totalMemberIds, totalMembershipIds, membershipType, field, 
        sources, scope, stem, stemScope, enabled, null);
    
  }
    
  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, Boolean)
   */
  @Override
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity) {
    
    return findAllByGroupOwnerOptionsHelper(totalGroupIds, totalMemberIds, totalMembershipIds, membershipType, GrouperUtil.toSet(field), null, sources, 
        scope, stem, stemScope, enabled, checkSecurity, null, null, null, null, null, false, 
        false, false, null, null, false, false, false, null, null, null, null, null, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, Boolean, FieldType, String, ServiceRole)
   */
  @Override
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity, 
      FieldType fieldType) {
    
    return findAllByGroupOwnerOptionsHelper(totalGroupIds, totalMemberIds, totalMembershipIds, membershipType, GrouperUtil.toSet(field), null, sources, 
        scope, stem, stemScope, enabled, checkSecurity, fieldType, null, null, null, null, false, false, false, 
        null, null, false, false, false, null, null, null, null, null, null);
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Collection, java.util.Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, java.lang.Boolean, edu.internet2.middleware.grouper.FieldType, java.lang.String, edu.internet2.middleware.grouper.service.ServiceRole, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, edu.internet2.middleware.grouper.Member)
   */
  @Override
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Collection<Field> fields,  Collection<Privilege> privilegesTheUserHas,
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity, FieldType fieldType,
      String serviceId, ServiceRole serviceRole, QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForGroup, 
      String scopeForGroup, boolean splitScopeForGroup, boolean hasFieldForGroup,
      boolean hasMembershipTypeForGroup, Member memberHasMembershipForGroup) {
    return findAllByGroupOwnerOptionsHelper(totalGroupIds, totalMemberIds,
        totalMembershipIds, membershipType,
        fields, privilegesTheUserHas, sources, scope, stem, stemScope, enabled, checkSecurity, fieldType,
        serviceId, serviceRole, queryOptionsForMember, filterForMember, splitScopeForMember, 
        hasFieldForMember, hasMembershipTypeForMember, queryOptionsForGroup, scopeForGroup, 
        splitScopeForGroup, hasFieldForGroup, hasMembershipTypeForGroup, memberHasMembershipForGroup, null, null, null, null, null);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Collection, java.util.Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, java.lang.Boolean, edu.internet2.middleware.grouper.FieldType, java.lang.String, edu.internet2.middleware.grouper.service.ServiceRole, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, edu.internet2.middleware.grouper.Member, java.lang.Boolean, java.lang.Boolean)
   */
  @Override
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Collection<Field> fields,  Collection<Privilege> privilegesTheUserHas,
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity, FieldType fieldType,
      String serviceId, ServiceRole serviceRole, QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForGroup, 
      String scopeForGroup, boolean splitScopeForGroup, boolean hasFieldForGroup,
      boolean hasMembershipTypeForGroup, Member memberHasMembershipForGroup, Boolean hasEnabledDate, Boolean hasDisabledDate,
      CompositeType customCompositeType, Group customCompositeGroup) {
    return findAllByGroupOwnerOptionsHelper(totalGroupIds, totalMemberIds,
        totalMembershipIds, membershipType,
        fields, privilegesTheUserHas, sources, scope, stem, stemScope, enabled, checkSecurity, fieldType,
        serviceId, serviceRole, queryOptionsForMember, filterForMember, splitScopeForMember, 
        hasFieldForMember, hasMembershipTypeForMember, queryOptionsForGroup, scopeForGroup, 
        splitScopeForGroup, hasFieldForGroup, hasMembershipTypeForGroup, memberHasMembershipForGroup, hasEnabledDate, hasDisabledDate,
        customCompositeType, customCompositeGroup, null);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Collection, java.util.Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, java.lang.Boolean, edu.internet2.middleware.grouper.FieldType, java.lang.String, edu.internet2.middleware.grouper.service.ServiceRole, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, edu.internet2.middleware.grouper.Member, java.lang.Boolean, java.lang.Boolean)
   */
  @Override
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Collection<Field> fields,  Collection<Privilege> privilegesTheUserHas,
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity, FieldType fieldType,
      String serviceId, ServiceRole serviceRole, QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForGroup, 
      String scopeForGroup, boolean splitScopeForGroup, boolean hasFieldForGroup,
      boolean hasMembershipTypeForGroup, Member memberHasMembershipForGroup, Boolean hasEnabledDate, Boolean hasDisabledDate,
      CompositeType customCompositeType, Group customCompositeGroup, QueryOptions queryOptionsForMembership) {
    return findAllByGroupOwnerOptionsHelper(totalGroupIds, totalMemberIds,
        totalMembershipIds, membershipType,
        fields, privilegesTheUserHas, sources, scope, stem, stemScope, enabled, checkSecurity, fieldType,
        serviceId, serviceRole, queryOptionsForMember, filterForMember, splitScopeForMember, 
        hasFieldForMember, hasMembershipTypeForMember, queryOptionsForGroup, scopeForGroup, 
        splitScopeForGroup, hasFieldForGroup, hasMembershipTypeForGroup, memberHasMembershipForGroup, hasEnabledDate, hasDisabledDate,
        customCompositeType, customCompositeGroup, queryOptionsForMembership);
  }

  /**
   * 
   * @param totalGroupIds 
   * @param totalMemberIds 
   * @param totalMembershipIds 
   * @param membershipType 
   * @param fields 
   * @param privilegesTheUserHas 
   * @param sources 
   * @param scope 
   * @param stem 
   * @param stemScope 
   * @param enabled 
   * @param checkSecurity 
   * @param fieldType 
   * @param serviceId 
   * @param serviceRole 
   * @param queryOptionsForMember 
   * @param filterForMember 
   * @param splitScopeForMember 
   * @param hasFieldForMember 
   * @param hasMembershipTypeForMember 
   * @param queryOptionsForGroup 
   * @param scopeForGroup 
   * @param splitScopeForGroup 
   * @param hasFieldForGroup 
   * @param hasMembershipTypeForGroup 
   * @param memberHasMembershipForGroup 
   * @param hasEnabledDate
   * @param hasDisabledDate
   * @param customCompositeType
   * @param customCompositeGroup
   * @param queryOptionsForMembership
   * @return results
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(Collection, Collection, Collection, MembershipType, Collection, Collection, Set, String, Stem, Scope, Boolean, Boolean, FieldType, String, ServiceRole, QueryOptions, String, boolean, boolean, boolean, QueryOptions, String, boolean, boolean, boolean, Member)
   */
  private Set<Object[]> findAllByGroupOwnerOptionsHelper(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Collection<Field> fields, final Collection<Privilege> privilegesTheUserHas,
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity, FieldType fieldType,
      String serviceId, ServiceRole serviceRole, QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForGroup, 
      String scopeForGroup, boolean splitScopeForGroup, boolean hasFieldForGroup,
      boolean hasMembershipTypeForGroup, Member memberHasMembershipForGroup, Boolean hasEnabledDate, Boolean hasDisabledDate,
      CompositeType customCompositeType, final Group customCompositeGroup, QueryOptions queryOptionsForMembership) {

    QueryOptions.initTotalCount(queryOptionsForGroup);
    QueryOptions.initTotalCount(queryOptionsForMember);
    
    if (checkSecurity == null) {
      checkSecurity = Boolean.TRUE;
    }
        
    if ((stem == null) != (stemScope == null)) {
      throw new RuntimeException("If stem is set, then stem scope must be set.  If stem isnt set, then stem scope must not be set: " + stem + ", " + stemScope);
    }
    
    if ((customCompositeType == null) != (customCompositeGroup == null)) {
      throw new RuntimeException("If customCompositeType is set, then customCompositeGroup must be set.  If customCompositeType isnt set, then customCompositeGroup must not be set: " + customCompositeType + ", " + customCompositeGroup);
    }
    
    if (StringUtils.isBlank(serviceId) != (serviceRole == null)) {
      throw new RuntimeException("If serviceId is set, then serviceRole needs to be set, and vice versa");
    }
    
    final List<String> totalGroupIdsList = GrouperUtil.listFromCollection(totalGroupIds);
    List<String> totalMemberIdsList = GrouperUtil.listFromCollection(totalMemberIds);
    List<String> totalMembershipIdsList = GrouperUtil.listFromCollection(totalMembershipIds);

    Set<Object[]> totalResults = new LinkedHashSet<Object[]>();
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    final Subject grouperSessionSubject = grouperSession.getSubject();
    
    if (customCompositeType != null) {
      if (customCompositeType != CompositeType.INTERSECTION && customCompositeType != CompositeType.COMPLEMENT) {
        throw new RuntimeException("Unsupported custom composite type: " + customCompositeType);
      }
      
      if (checkSecurity) {
        // make sure read on group
        boolean allowed = (Boolean)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

          /**
           * 
           */
          @Override
          public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            return PrivilegeHelper.canRead(theGrouperSession, customCompositeGroup, grouperSessionSubject);
          }
        });

        //if there is one stem, and checking security, and the user is not allowed to STEM it, then no results
        if (!allowed) {
          return totalResults;
        }
      }
    }
    
    final Set<Privilege> privilegesTheUserHasFinal = new HashSet<Privilege>();
    
    if (GrouperUtil.length(privilegesTheUserHas) > 0) {
      privilegesTheUserHasFinal.addAll(privilegesTheUserHas);
    } else if (GrouperUtil.length(fields) == 0 || !fields.iterator().next().isGroupAccessField()) {
      privilegesTheUserHasFinal.addAll(AccessPrivilege.READ_PRIVILEGES);
    } else {
      privilegesTheUserHasFinal.addAll(AccessPrivilege.ADMIN_PRIVILEGES);
    }
    
    //just check security on one stem to help performance
    if (checkSecurity && GrouperUtil.length(totalGroupIds) == 1) {
      boolean allowed = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

        /**
         * 
         */
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          Group theGroup = GroupFinder.findByUuid(theGrouperSession, totalGroupIdsList.get(0), false);
          if (theGroup == null) {
            return false;
          }
          for (Privilege privilegeTheUserHas : privilegesTheUserHasFinal) {
             if (theGroup.canHavePrivilege(grouperSessionSubject, privilegeTheUserHas.getName(), false)) {
               return true;
             }
          }
          return false;
             
        }
      });

      //if there is one stem, and checking security, and the user is not allowed to STEM it, then no results
      if (!allowed) {
        return totalResults;
      }

      //we dont need to check security if there is one stem and allowed on that stem
      checkSecurity = false;

    }

    int groupBatches = GrouperUtil.batchNumberOfBatches(totalGroupIds, 100);

    for (int groupIndex = 0; groupIndex < groupBatches; groupIndex++) {
      
      List<String> groupIds = GrouperUtil.batchList(totalGroupIdsList, 100, groupIndex);
      
      int memberBatches = GrouperUtil.batchNumberOfBatches(totalMemberIds, 100);

      for (int memberIndex = 0; memberIndex < memberBatches; memberIndex++) {
        
        List<String> memberIds = GrouperUtil.batchList(totalMemberIdsList, 100, memberIndex);
        int membershipBatches = GrouperUtil.batchNumberOfBatches(totalMembershipIds, 100);
        
        for (int membershipIndex = 0; membershipIndex < membershipBatches; membershipIndex++) {
          
          List<String> membershipIds = GrouperUtil.batchList(totalMembershipIdsList, 100, membershipIndex);
          
          int groupIdsSize = GrouperUtil.length(groupIds);
          int memberIdsSize = GrouperUtil.length(memberIds);
          int membershipIdsSize = GrouperUtil.length(membershipIds);
          
          if (groupIdsSize == 0 && memberIdsSize == 0 && membershipIdsSize == 0 && stem == null && serviceRole == null) {
            throw new RuntimeException("Must pass in group(s), member(s), stem, and/or membership(s)");
          }

          ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

          String selectPrefix = "select distinct ms, g, m ";
          
          //note: mysql wont let you do count distinct of multiple columns
          String countPrefix = "select count(*) ";

          StringBuilder sql = new StringBuilder(" from Member m, MembershipEntry ms, Group g, Field f  ");
          
          if (serviceRole == null != StringUtils.isBlank(serviceId)) {
            throw new RuntimeException("If you pass in the serviceRole you must pass in the serviceId and visa versa");
          }
          
          if (serviceRole != null) {
            sql.append(", ServiceRoleView theServiceRoleView ");

          }

          //maybe we are checking security, maybe not
          boolean changedQuery = false;
          
          if (checkSecurity) { 
            changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
              grouperSessionSubject, byHqlStatic, 
              sql, "ms.ownerGroupId", privilegesTheUserHasFinal);
          }

          if (changedQuery && sql.toString().contains(" where ")) {
            sql.append(" and ");
          } else {
            sql.append(" where ");
          }
          
          sql.append(" ms.ownerGroupId = g.uuid "
              + " and ms.memberUuid = m.uuid ");
          
          if (serviceRole != null) {
            sql.append(" and ms.ownerGroupId = theServiceRoleView.groupId ");
            sql.append(" and f.uuid = theServiceRoleView.fieldId ");
            HibUtils.convertFieldsToSqlInString(serviceRole.fieldsForGroupQuery(), byHqlStatic, sql, "theServiceRoleView.fieldId");
            sql.append(" and theServiceRoleView.serviceNameId = :serviceNameId ");
            byHqlStatic.setString("serviceNameId", serviceId);
          }
          
          if (customCompositeGroup != null) {
            if (customCompositeType == CompositeType.INTERSECTION) {
              sql.append(" and exists ");
            } else {
              sql.append(" and not exists ");
            }
            
            sql.append("(select 1 from MembershipEntry mscc " +
                "where mscc.ownerGroupId = '" + customCompositeGroup.getId() + "' " +
                "and mscc.memberUuid = m.uuid " +
                "and mscc.fieldId = '" + Group.getDefaultList().getId() + "' " +
                "and mscc.enabledDb = 'T') ");
          }
          
          if (enabled != null && enabled) {
            sql.append(" and ms.enabledDb = 'T' ");
          }
          if (enabled != null && !enabled) {
            sql.append(" and ms.enabledDb = 'F' ");
          }
          if (hasEnabledDate != null && hasEnabledDate) {
            sql.append(" and ms.enabledTimeDb is not null ");
          }
          if (hasEnabledDate != null && !hasEnabledDate) {
            sql.append(" and ms.enabledTimeDb is null ");
          }
          if (hasDisabledDate != null && hasDisabledDate) {
            sql.append(" and ms.disabledTimeDb is not null ");
          }
          if (hasDisabledDate != null && !hasDisabledDate) {
            sql.append(" and ms.disabledTimeDb is null ");
          }
          if (sources != null && sources.size() > 0) {
            sql.append(" and m.subjectSourceIdDb in ").append(HibUtils.convertSourcesToSqlInString(sources));
          }
          boolean hasScope = StringUtils.isNotBlank(scope);
          if (hasScope) {
            sql.append(" and g.nameDb like :scope ");
            byHqlStatic.setString("scope", scope + "%");
          }
          if (stem != null) {
            switch (stemScope) {
              case ONE:
                
                sql.append(" and g.parentUuid = :stemId ");
                byHqlStatic.setString("stemId", stem.getUuid());
                break;
              case SUB:
                
                sql.append(" and g.nameDb like :stemSub ");
                byHqlStatic.setString("stemSub", stem.getName() + ":%");
                
                break;
              default:
                throw new RuntimeException("Not expecting scope: " + stemScope);
            }
          }
          //immediate or effective, etc
          if (membershipType != null) {
            sql.append(" and ms.type ").append(membershipType.queryClause()).append(" ");
          }
          
          sql.append(" and ms.fieldId = f.uuid ");
          if (GrouperUtil.length(fields) > 0) {
            if (serviceRole != null) {
              throw new RuntimeException("If you specify the field, you cannot specify the serviceRole (and vice versa)");
            }
            //needs to be a members field
            //if (!StringUtils.equals("list",field.getTypeString())) {
            //  throw new RuntimeException("This method only works with members fields: " + field);
            //}
            sql.append(" and ms.fieldId in ( ");
            Set<String> fieldStrings = new HashSet<String>();
            for (Field field : fields) {
              fieldStrings.add(field.getUuid());
            }
            sql.append(HibUtils.convertToInClause(fieldStrings, byHqlStatic));
            sql.append(" ) ");
          }
          //service role will deal with its own fields
          if (serviceRole == null && GrouperUtil.length(fields) == 0 && (fieldType == null || fieldType == FieldType.LIST)) {
            
            //add on the column
            sql.append(" and f.typeString = 'list' ");
          }
          if (fieldType == FieldType.ACCESS) {
            if (serviceRole == null) {
              //add on the column
              //this was changed to list for some reason, but i changed it back to access 2013/10/21
              sql.append(" and f.typeString = 'access' ");
            }
          }
          if (groupIdsSize > 0) {
            sql.append(" and ms.ownerGroupId in (");
            sql.append(HibUtils.convertToInClause(groupIds, byHqlStatic));
            sql.append(") ");
          }
          if (memberIdsSize > 0) {
            sql.append(" and ms.memberUuid in (");
            sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
            sql.append(") ");
          }
          if (membershipIdsSize > 0) {
            sql.append(" and ( ");
            int count = 0;
            for (String membershipId : membershipIds) {
              
              if (count > 0) {
                sql.append(" or ");
              }

              //membershipId should have a colon in it
              String immediateMembershipId = GrouperUtil.prefixOrSuffix(membershipId, ":", true);
              String groupSetId = GrouperUtil.prefixOrSuffix(membershipId, ":", false);
              String immediateMembershipIdVar = "immediateMembershipId" + count;
              String groupSetIdVar = "groupSetId" + count;
              
              sql.append(" ( ms.immediateMembershipId = :" + immediateMembershipIdVar + " and ms.groupSetId = :" + groupSetIdVar + " )");
              byHqlStatic.setString(immediateMembershipIdVar, immediateMembershipId);
              byHqlStatic.setString(groupSetIdVar, groupSetId);
              
              count++;
            }
            sql.append(" ) ");

            // dont do this!!!!!
            // GRP-1304: delete membership from UI has major performance problem
            //sql.append(" and ms.uuid in (");
            //sql.append(HibUtils.convertToInClause(membershipIds, byHqlStatic));
            //sql.append(") ");
          }
          
          if (!StringUtils.isBlank(filterForMember)) {

            filterForMember = filterForMember.toLowerCase();

            String[] filtersForMember = splitScopeForMember ? GrouperUtil.splitTrim(filterForMember, " ") 
                : new String[]{filterForMember};

            if (sql.length() > 0) {
              sql.append(" and ");
            }
            sql.append(" ( ");

            int index = 0;
            
            String searchFieldName = SearchStringEnum.getDefaultSearchString().getFieldName();

            for (String theFilter : filtersForMember) {
              if (index != 0) {
                sql.append(" and ");
              }
              sql.append(" ( m." + searchFieldName + " like :filterString" + index + " ) ");

              if (!theFilter.endsWith("%")) {
                theFilter += "%";
              }
              if (!theFilter.startsWith("%")) {
                theFilter = "%" + theFilter;
              }
              byHqlStatic.setString("filterString" + index, theFilter);
              index++;
            }
            sql.append(" ) ");
          }

          if (!StringUtils.isBlank(scopeForGroup)) {

            scopeForGroup = scopeForGroup.toLowerCase();

            String[] scopesForGroup = splitScopeForGroup ? GrouperUtil.splitTrim(scopeForGroup, " ") 
                : new String[]{scopeForGroup};

            if (sql.length() > 0) {
              sql.append(" and ");
            }
            sql.append(" ( ");

            int index = 0;
            
            for (String theScopeForGroup : scopesForGroup) {
              if (index != 0) {
                sql.append(" and ");
              }
              
              sql.append(" ( lower(g.nameDb) like :scopeForGroup" + index 
                  + " or lower(g.alternateNameDb) like :scopeForGroup" + index 
                  + " or lower(g.displayNameDb) like :scopeForGroup" + index 
                  + " or lower(g.descriptionDb) like :scopeForGroup" + index + " ) ");
              
              if (!theScopeForGroup.endsWith("%")) {
                theScopeForGroup += "%";
              }
              if (!theScopeForGroup.startsWith("%")) {
                theScopeForGroup = "%" + theScopeForGroup;
              }
              byHqlStatic.setString("scopeForGroup" + index, theScopeForGroup);
              index++;
            }
            sql.append(" ) ");
          }

          
          byHqlStatic
            .setCacheable(false)
            .setCacheRegion(KLASS);

          int maxMemberships = GrouperConfig.retrieveConfig().propertyValueInt("ws.getMemberships.maxResultSize", 30000);
          int maxPageSize = GrouperConfig.retrieveConfig().propertyValueInt("ws.getMemberships.maxPageSize", 500);

          {
            
            boolean pageMembers = queryOptionsForMember != null && queryOptionsForMember.getQueryPaging() != null;
            
            if (pageMembers) {

              //cant page too much...
              if (queryOptionsForMember.getQueryPaging().getPageSize() > maxPageSize) {
                throw new RuntimeException("Cant get a page size greater then " + maxPageSize + "! " 
                    + queryOptionsForMember.getQueryPaging().getPageSize());
              }
  
              if (groupBatches > 1) {
                throw new RuntimeException("Cant have more than 1 groupBatch if paging members");
              }
              
              if (memberBatches > 1) {
                throw new RuntimeException("Cant have more than 1 memberBatch if paging members");
              }
              
              if (membershipBatches > 1) {
                throw new RuntimeException("Cant have more than 1 membershipBatch if paging members");
              }
              
            }
  
            // should be fine to filter on member without paging
            //if (!StringUtils.isBlank(filterForMember) && !pageMembers) {
            //  throw new RuntimeException("If you are filtering by member, then you must page members");
            //}
  
            //if -1, lets not check
            if (maxMemberships >= 0 && !pageMembers && queryOptionsForGroup == null && queryOptionsForMembership == null) {
    
              long size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
              
              //see if too many
              if (size > maxMemberships) {
                
                if (queryOptionsForMember != null && queryOptionsForMember.isRetrieveCount() && !queryOptionsForMember.isRetrieveResults()) {
                  queryOptionsForMember.setCount(size + GrouperUtil.defaultIfNull(queryOptionsForMember.getCount(), 0L));
                  return totalResults;
                }
                
                throw new RuntimeException("Too many results: " + size);
              }
              
            }
  
            //if paging by members, get the members, then do the same query using those members...
            if (pageMembers) {

              if (queryOptionsForMember != null && queryOptionsForMember.getQuerySort() != null) {
                Hib3MemberDAO.massageMemberSortFields(queryOptionsForMember.getQuerySort());
              }
              
              //sort by default search string if not specified
              if (queryOptionsForMember != null && queryOptionsForMember.getQuerySort() == null) {
                queryOptionsForMember.sortAsc("m." + SortStringEnum.getDefaultSortString().getFieldName());
              }
  
              byHqlStatic.options(queryOptionsForMember);
     
              String memberPrefix = "select distinct m ";
              
              Set<Member> members = byHqlStatic.createQuery(memberPrefix + sql.toString()).listSet(Member.class);
  
              //no need to do another query if no members
              if (GrouperUtil.length(members) == 0) {
                return totalResults;
              }
              
              Set<String> theMemberIds = new LinkedHashSet<String>();
              
              for (Member member : members) {
                theMemberIds.add(member.getUuid());
              }
              
              //dont pass for people with membership type or field... we already filtered by that...
              Set<Object[]> tempResults = findAllByGroupOwnerOptionsHelper(totalGroupIds, theMemberIds,
                  totalMembershipIds, hasMembershipTypeForMember ? null : membershipType, hasFieldForMember ? null : fields,  privilegesTheUserHas,
                  sources, scope, stem, stemScope, enabled, checkSecurity, fieldType, null, null, 
                  null, null, false, false, false, null, null, false, false, false, null, hasEnabledDate, hasDisabledDate, customCompositeType, customCompositeGroup, null);
              
              //lets sort these by member
              Set<Object[]> sortedResults = new LinkedHashSet<Object[]>();
              
              for (Member member : members) {
                Iterator<Object[]> iterator = tempResults.iterator();
                while(iterator.hasNext()) {
                  
                  Object[] tempResult = iterator.next();
                  //if the member is the same, put it in the sortedResults, and remove
                  if (StringUtils.equals(((Member)tempResult[2]).getUuid(), member.getUuid())) {
                    
                    sortedResults.add(tempResult);
                    iterator.remove();
                    
                  }
                }
              }
              return sortedResults;
              
            }
          }
          {
            //sort for groups
            boolean pageGroups = queryOptionsForGroup != null;
            
            if (pageGroups) {

              if (queryOptionsForGroup.getQueryPaging() == null) {
                throw new RuntimeException("If paging by group, then paging must be set in the query options");
              }

              //cant page too much...
              if (queryOptionsForGroup.getQueryPaging().getPageSize() > maxPageSize) {
                throw new RuntimeException("Cant get a page size greater then " + maxPageSize + "! " 
                    + queryOptionsForGroup.getQueryPaging().getPageSize());
              }

              if (groupBatches > 1) {
                throw new RuntimeException("Cant have more than 1 groupBatch if paging groups");
              }
              
              if (memberBatches > 1) {
                throw new RuntimeException("Cant have more than 1 memberBatch if paging groups");
              }
              
              if (membershipBatches > 1) {
                throw new RuntimeException("Cant have more than 1 membershipBatch if paging groups");
              }
              
            }

            if (!StringUtils.isBlank(scopeForGroup) && !pageGroups) {
              throw new RuntimeException("If you are filtering by group, then you must page groups");
            }
            
            //note, put in the size query conditional above

            //if paging by members, get the members, then do the same query using those members...
            if (pageGroups) {

              if (memberHasMembershipForGroup != null) {
                
                if (sql.length() > 0) {
                  sql.append(" and ");
                }

                sql.append(" exists (select 1 from MembershipEntry fieldMembership where fieldMembership.ownerGroupId = g.uuid " +
                    " and fieldMembership.fieldId = :fieldId2 " +
                    " and fieldMembership.memberUuid = :fieldMembershipMemberUuid2 and fieldMembership.enabledDb = 'T' ) ");
                byHqlStatic.setString("fieldId2", Group.getDefaultList().getUuid());
                byHqlStatic.setString("fieldMembershipMemberUuid2", memberHasMembershipForGroup.getUuid());
              }
              
              if (queryOptionsForGroup.getQuerySort()!= null) {
                Hib3GroupDAO.massageSortFields(queryOptionsForGroup.getQuerySort(), "g");
              }
              
              //sort by default search string if not specified
              if (queryOptionsForGroup.getQuerySort() == null) {
                queryOptionsForGroup.sortAsc("g.displayNameDb");
              }

              byHqlStatic.options(queryOptionsForGroup);
              
              String groupPrefix = "select distinct g ";

              Set<Group> groups = byHqlStatic.createQuery(groupPrefix + sql.toString()).listSet(Group.class);

              //no need to do another query if no groups
              if (GrouperUtil.length(groups) == 0) {
                return totalResults;
              }
              
              Set<String> theGroupIds = new LinkedHashSet<String>();
              
              for (Group group : groups) {
                theGroupIds.add(group.getUuid());
              }
              
              //if we are only getting rows where there is a membership, and certain fields, then
              //for the membership query, add the default list to the list of fields
              if (memberHasMembershipForGroup != null && GrouperUtil.length(fields) > 0) {
                fields = new HashSet<Field>(fields);
                fields.add(Group.getDefaultList());
              }
              
              //dont pass for people with membership type or field... we already filtered by that...
              Set<Object[]> tempResults = findAllByGroupOwnerOptionsHelper(theGroupIds, totalMemberIds,
                  totalMembershipIds, hasMembershipTypeForGroup ? null : membershipType, hasFieldForGroup ? null : fields, privilegesTheUserHas, 
                  sources, scope, stem, stemScope, enabled, checkSecurity, fieldType, null, null, null, null, false, false, false, 
                  null, null, false, false, false, null, hasEnabledDate, hasDisabledDate, customCompositeType, customCompositeGroup, null);
              
              //lets sort these by member
              Set<Object[]> sortedResults = new LinkedHashSet<Object[]>();
              
              for (Group group : groups) {
                Iterator<Object[]> iterator = tempResults.iterator();
                while(iterator.hasNext()) {
                  
                  Object[] tempResult = iterator.next();
                  //if the member is the same, put it in the sortedResults, and remove
                  if (StringUtils.equals(((Group)tempResult[1]).getUuid(), group.getUuid())) {
                    
                    sortedResults.add(tempResult);
                    iterator.remove();
                    
                  }
                }
              }
              return sortedResults;
              
            }

            
          }
          
          boolean pageMemberships = queryOptionsForMembership != null && queryOptionsForGroup == null && queryOptionsForMember == null;

          {
            //sort for memberships            
            if (pageMemberships) {

              if (queryOptionsForMembership.getQueryPaging() == null) {
                throw new RuntimeException("If paging by membership, then paging must be set in the query options");
              }
              
              if (queryOptionsForMembership.getQuerySort() == null) {
                throw new RuntimeException("If paging by membership, then sorting must be set in the query options");
              }

              //cant page too much...
              if (queryOptionsForMembership.getQueryPaging().getPageSize() > maxPageSize) {
                throw new RuntimeException("Cant get a page size greater then " + maxPageSize + "! " 
                    + queryOptionsForMembership.getQueryPaging().getPageSize());
              }

              if (groupBatches > 1) {
                throw new RuntimeException("Cant have more than 1 groupBatch if paging memberships");
              }
              
              if (memberBatches > 1) {
                throw new RuntimeException("Cant have more than 1 memberBatch if paging memberships");
              }
              
              if (membershipBatches > 1) {
                throw new RuntimeException("Cant have more than 1 membershipBatch if paging memberships");
              }              
            }
          }
          
          //if -1, lets not check
          if (maxMemberships >= 0) {

            long size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
            
            if (queryOptionsForMember != null && queryOptionsForMember.isRetrieveCount()) {
              queryOptionsForMember.setCount(size + GrouperUtil.defaultIfNull(queryOptionsForMember.getCount(), 0L));
            }

            //see if too many
            if (size > maxMemberships) {
              if (queryOptionsForMember != null && queryOptionsForMember.isRetrieveCount() && !queryOptionsForMember.isRetrieveResults()) {
                return totalResults;
              }
              throw new RuntimeException("Too many results: " + size);
            }
            
          }
                   
          if (queryOptionsForMember == null || queryOptionsForMember.isRetrieveResults()) {
            if (pageMemberships) {
              byHqlStatic.options(queryOptionsForMembership);
            }
            
            Set<Object[]> results = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(Object[].class);

            totalResults.addAll(results);
          }
        }
      }
    }
    
    
    //nothing to filter
    if (GrouperUtil.length(totalResults) == 0) {
      return totalResults;
    }
    
    //if the hql didnt filter, we need to do that here
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    for (Object[] objects : totalResults) {
      memberships.add((Membership)objects[0]);
    }
    int origMembershipsSize = memberships.size();
    Set<Membership> filteredMemberships = grouperSession.getAccessResolver().postHqlFilterMemberships(grouperSessionSubject, memberships);
    if (origMembershipsSize != filteredMemberships.size()) {
      
      //we have work to do
      Iterator<Object[]> iterator = totalResults.iterator();
      while (iterator.hasNext()) {
        Object[] row = iterator.next();
        Membership currentMembership = (Membership)row[0];
        //if not in the allowed list
        if (!filteredMemberships.contains(currentMembership)) {
          //remove the object row
          iterator.remove();
        }
      }
    }
    
    assignMembersOwnersToMemberships(totalResults);
    //we should be down to the cesure list
    return totalResults;
    
  }
  
  /**
   * @param ownerGroupId 
   * @param f 
   * @param type 
   * @param sources 
   * @param queryOptions 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Member> findAllMembersByGroupOwnerAndFieldAndType(
      String ownerGroupId, Field f, String type, Set<Source> sources, QueryOptions queryOptions, boolean enabledOnly) 
    throws  GrouperDAOException {
    return findAllMembersByOwnerAndFieldAndType(ownerGroupId, f, type, sources, queryOptions, enabledOnly, null, null, null);
  } 
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllMembersByOwnerAndFieldAndType(java.lang.String, edu.internet2.middleware.grouper.Field, java.lang.String, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions, boolean, edu.internet2.middleware.grouper.member.SortStringEnum, edu.internet2.middleware.grouper.member.SearchStringEnum, java.lang.String)
   */
  public Set<Member> findAllMembersByOwnerAndFieldAndType(String ownerId,
      Field f, String type, Set<Source> sources, QueryOptions queryOptions,
      boolean enabledOnly, SortStringEnum memberSortStringEnum,
      SearchStringEnum memberSearchStringEnum, String memberSearchStringValue)
      throws GrouperDAOException {

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    StringBuilder sql = new StringBuilder("select distinct m "
            + "from Member m, MembershipEntry ms where "
            + "ms.ownerId = :owner "
            + "and ms.fieldId = :fieldId ");
        if(type != null) {
          MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
            sql.append("and ms.type  " + membershipType.queryClause());
        }

    sql.append(" and ms.memberUuid = m.uuid  ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    if (sources != null && sources.size() > 0) {
      sql.append(" and m.subjectSourceIdDb in ").append(HibUtils.convertSourcesToSqlInString(sources));
      }
    
    
    if (memberSearchStringEnum != null) {
      if (!memberSearchStringEnum.hasAccess()) {
        throw new RuntimeException("Not allowed to access " + memberSearchStringEnum.getFieldName());
      }
      
      if (memberSearchStringValue == null) {
        sql.append(" and m." + memberSearchStringEnum.getFieldName() + " is null ");
      } else {
        String[] parts = memberSearchStringValue.trim().toLowerCase().split("\\s+");
        for (int i = 0; i < parts.length; i++) {
          sql.append(" and m." + memberSearchStringEnum.getFieldName() + " like :searchString" + i + " ");
          byHqlStatic.setString("searchString" + i, "%" + parts[i] + "%");
        }
      }
    }
    
    if (memberSortStringEnum != null) {      
      if (queryOptions == null) {
        queryOptions = new QueryOptions();
      }
      
      queryOptions.sortAsc(memberSortStringEnum.getFieldName());
    }

    if (queryOptions != null) {
      Hib3MemberDAO.massageMemberSortFields(queryOptions.getQuerySort());
    }

    return byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
          .setCacheRegion(KLASS).options(queryOptions)
      .setString("owner", ownerId)
      .setString( "fieldId", f.getUuid() )
      .listSet(Member.class);

  } 
  
  /**
   * note, dont change this signature, Arnaud is using it
   * @param ownerStemId 
   * @param f 
   * @param type 
   * @param queryOptions 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Member> findAllMembersByStemOwnerAndFieldAndType(
      String ownerStemId, Field f, String type, QueryOptions queryOptions, boolean enabledOnly) 
    throws  GrouperDAOException {
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);

    StringBuilder sql = new StringBuilder("select m "
      + "from Member m, MembershipEntry ms where "
      + "ms.ownerStemId = :owner "
      + "and ms.fieldId = :fieldId "
      + "and ms.type  " + membershipType.queryClause()
      + " and ms.memberUuid = m.uuid  ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    if (queryOptions != null) {
      Hib3MemberDAO.massageMemberSortFields(queryOptions.getQuerySort());
    }
    return HibernateSession.byHqlStatic()
    .createQuery(sql.toString())
    .setCacheable(false)
    .setCacheRegion(KLASS)
    .options(queryOptions)
    .setString("owner", ownerStemId)
    .setString( "fieldId", f.getUuid() )
    .listSet(Member.class);
  } 

  /** batch size for memberships (setable for testing) */
  static int batchSize = 50;

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerAndFieldAndMembersAndType(java.lang.String, edu.internet2.middleware.grouper.Field, java.util.Collection, java.lang.String, boolean)
   */
  public Set<Membership> findAllByGroupOwnerAndFieldAndMembersAndType(String ownerGroupId,
      Field f, Collection<Member> members, String type, boolean enabledOnly) throws GrouperDAOException {
    
    List<String> memberIds = GrouperUtil.propertyList(members, Member.PROPERTY_UUID, String.class);
    
    return findAllByGroupOwnerAndFieldAndMemberIdsAndType(ownerGroupId, f, memberIds, type, enabledOnly);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerAndFieldAndMemberIdsAndType(java.lang.String, edu.internet2.middleware.grouper.Field, java.util.Collection, java.lang.String, boolean)
   */
  public Set<Membership> findAllByGroupOwnerAndFieldAndMemberIdsAndType(String ownerGroupId,
      Field f, Collection<String> memberIds, String type, boolean enabledOnly) throws GrouperDAOException {
    
    if (memberIds == null) {
      return null;
    }
    if (memberIds.size() == 0) {
      return new LinkedHashSet<Membership>();
    }

    List<String> memberIdsList = GrouperUtil.listFromCollection(memberIds);
    
    //lets page through these
    int pages = GrouperUtil.batchNumberOfBatches(memberIds, batchSize);
    
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, false);
    
    for (int i=0; i<pages; i++) {
      List<String> currentMemberIdList = GrouperUtil.batchList(memberIdsList, batchSize, i);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select ms"
            + " from MembershipEntry ms where"
            + " ms.ownerGroupId      = :owner "
            + " and  ms.fieldId = :fieldId ");
      if (membershipType != null) {
        query.append(" and  ms.type  " + membershipType.queryClause());
      }

      query.append(" and ms.memberUuid in (");
      byHqlStatic.setString( "owner", ownerGroupId ) 
        .setString( "fieldId", f.getUuid() );

      //add all the uuids
      byHqlStatic.setCollectionInClause(query, currentMemberIdList);
      query.append(")");
      
      if (enabledOnly) {
        query.append(" and ms.enabledDb = 'T'");
      }
      
      List<Membership> currentList = byHqlStatic.createQuery(query.toString())
        .setCacheable(true)
        .setCacheRegion(KLASS)
        .list(Membership.class);
      memberships.addAll(currentList);
    }
    return memberships;
      
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerAndFieldAndMembers(java.lang.String, edu.internet2.middleware.grouper.Field, java.util.Collection, boolean)
   */
  public Set<Membership> findAllByGroupOwnerAndFieldAndMembers(String ownerGroupId,
      Field f, Collection<Member> members, boolean enabledOnly) throws GrouperDAOException {
    
    if (members == null) {
      return null;
    }
    if (members.size() == 0) {
      return new LinkedHashSet<Membership>();
    }

    //lets page through these
    List<Member> totalMembersList = GrouperUtil.listFromCollection(members);
    int pages = GrouperUtil.batchNumberOfBatches(totalMembersList, batchSize);
    
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    for (int i=0; i<pages; i++) {
      List<Member> memberList = GrouperUtil.batchList(totalMembersList, batchSize, i);
      
      List<String> uuids = GrouperUtil.propertyList(memberList, Member.PROPERTY_UUID, String.class);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select ms"
          + " from MembershipEntry ms where"
          + " ms.ownerGroupId      = :owner "
          + "and  ms.fieldId = :fieldId "
          + " and ms.memberUuid in (");
      byHqlStatic.setString( "owner", ownerGroupId ) 
        .setString( "fieldId", f.getUuid() );
      //add all the uuids
      byHqlStatic.setCollectionInClause(query, uuids);
      query.append(")");
      
      if (enabledOnly) {
        query.append(" and ms.enabledDb = 'T'");
      }
      
      List<Membership> currentList = byHqlStatic.createQuery(query.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS)
        .list(Membership.class);
      memberships.addAll(currentList);
    }
    return memberships;
      
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerAndCompositeAndMembers(java.lang.String, java.util.Collection, boolean)
   */
  public Set<Membership> findAllByGroupOwnerAndCompositeAndMembers(String ownerGroupId,
      Collection<Member> members, boolean enabledOnly) throws GrouperDAOException {
    
    if (members == null) {
      return null;
    }
    if (members.size() == 0) {
      return new LinkedHashSet<Membership>();
    }
    
    //lets page through these
    List<Member> totalMembersList = GrouperUtil.listFromCollection(members);
    int pages = GrouperUtil.batchNumberOfBatches(totalMembersList, batchSize);
    
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    for (int i=0; i<pages; i++) {
      List<Member> memberList = GrouperUtil.batchList(totalMembersList, batchSize, i);
      
      List<String> uuids = GrouperUtil.propertyList(memberList, Member.PROPERTY_UUID, String.class);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select ms"
          + " from MembershipEntry ms where"
          + " ms.ownerGroupId      = :owner "
          + "and  ms.type = 'composite' "
          + " and ms.memberUuid in (");
      byHqlStatic.setString( "owner", ownerGroupId ) ;
      //add all the uuids
      byHqlStatic.setCollectionInClause(query, uuids);
      query.append(")");
      
      if (enabledOnly) {
        query.append(" and ms.enabledDb = 'T'");
      }
      
      List<Membership> currentList = byHqlStatic.createQuery(query.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS)
        .list(Membership.class);
      memberships.addAll(currentList);
    }
    return memberships;
      
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findByGroupOwnerAndMemberAndFieldAndType(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field, java.lang.String, boolean, boolean)
   */
  public Membership findByGroupOwnerAndMemberAndFieldAndType(String ownerGroupId,
      String memberUUID, Field f, String type, boolean exceptionIfNull, boolean enabledOnly)
      throws GrouperDAOException, MembershipNotFoundException {
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where  "
            + "     ms.ownerGroupId  = :owner            "
            + "and  ms.memberUuid = :member           " 
            + "and  ms.fieldId = :fuuid "
            + "and  ms.memberUuid = m.uuid  " 
            + "and  ms.type " + membershipType.queryClause());
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
      
    Object[] result = HibernateSession.byHqlStatic().createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS)
        .setString("owner",
            ownerGroupId).setString("member", memberUUID)      
            .setString( "fuuid",  f.getUuid()            )
            .uniqueResult(
            Object[].class);
    
    if (result == null || result[0] == null) {
      if (exceptionIfNull) {
        throw new MembershipNotFoundException();
      }
      return null;
    }
    assignMemberOwnerToMembership(result);
    Membership ms = (Membership) result[0];
    return ms;
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findByStemOwnerAndMemberAndFieldAndType(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field, java.lang.String, boolean, boolean)
   */
  public Membership findByStemOwnerAndMemberAndFieldAndType(String ownerStemId,
      String memberUUID, Field f, String type, boolean exceptionIfNull, boolean enabledOnly)
      throws GrouperDAOException, MembershipNotFoundException {

    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
    StringBuilder sql = new StringBuilder(
        "select distinct ms, m from MembershipEntry as ms, Member as m, Field as field where  "
        + "     ms.ownerStemId  = :owner            "
        + "and  ms.memberUuid = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid = m.uuid  "
        + "and  ms.type " + membershipType.queryClause());
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Object result[] = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner",  ownerStemId              )
      .setString( "member", memberUUID             )
        .setString( "fuuid",  f.getUuid()            )
      .uniqueResult(Object[].class);

  if (result==null || result[0]==null) {
    if (exceptionIfNull) {
      throw new MembershipNotFoundException();
    } 
    return null;
  }
  assignMemberOwnerToMembership(result);
  Membership ms = (Membership)result[0];
  return ms;
  }


  /**
   * @param _ms 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllChildMemberships(Membership _ms, boolean enabledOnly) 
    throws  GrouperDAOException
  {
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where ms.groupSetParentId = :parentId "
             + "and ms.viaGroupId = :viaGroupId and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships =  HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("parentId", _ms.getGroupSetId())
      .setString("viaGroupId", _ms.getMember().getSubjectId())
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param viaGroupId 
   * @param depth
   * @param enabledOnly 
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByGroupOwner(String ownerGroupId, String memberUUID, Field f, String viaGroupId, int depth, boolean enabledOnly) 
    throws  GrouperDAOException
  {
    if (depth <= 0) {
      return new LinkedHashSet<Membership>();
    }
    
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where  "
          + "     ms.ownerGroupId  = :owner            "
          + "and  ms.memberUuid = :member           "
          + "and  ms.fieldId = :fuuid "
          + "and  ms.viaGroupId    = :via              "
          + "and  ms.depth      = :depth            "
          + "and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner",  ownerGroupId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
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
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByStemOwner(String ownerStemId, String memberUUID, Field f, String viaGroupId, int depth, boolean enabledOnly) 
    throws  GrouperDAOException
  {
    if (depth <= 0) {
      return new LinkedHashSet<Membership>();
    }
    
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m, Field as field where  "
          + "     ms.ownerStemId  = :owner            "
          + "and  ms.memberUuid = :member           "
          + "and  ms.fieldId = :fuuid "
          + "and  ms.viaGroupId    = :via              "
          + "and  ms.depth      = :depth            "
          + "and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner",  ownerStemId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "via",    viaGroupId                )
      .setInteger("depth",  depth                  )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param memberUUID 
   * @param f 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByMemberAndField(String memberUUID, Field f, boolean enabledOnly) 
    throws  GrouperDAOException
  {
    // I'm adding a check for the immediate field id to help with performance 
    // in cases where the member has a lot of non-default list immediate memberships.
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where ms.memberUuid = m.uuid and ");
    sql.append("ms.memberUuid = :member and ms.immediateFieldId = :defaultMembersField and ms.fieldId = :fuuid and ms.type = 'effective'");

    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString("defaultMembersField", Group.getDefaultList().getUuid())
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param f 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllEffectiveByGroupOwnerAndMemberAndField(String ownerGroupId, String memberUUID, Field f, boolean enabledOnly)
    throws  GrouperDAOException {
    
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m, Field as field where  "
          + "     ms.ownerGroupId  = :owner            "
          + "and  ms.memberUuid = :member           "
          + "and  ms.fieldId = :fuuid "
          + "and ms.memberUuid = m.uuid "
          + "and  ms.type = 'effective' ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner",  ownerGroupId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 
  
  /**
   * @param ownerGroupId 
   * @param memberUUID 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   1.2.1
   */
  public Set<Membership> findAllByGroupOwnerAndMember(String ownerGroupId, String memberUUID, boolean enabledOnly) 
    throws  GrouperDAOException {
    
    //Added by Gary Brown 2007-11-01 so that getPrivs can do one query rather than 6

    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where  "
          + "     ms.ownerGroupId   = :owner            "  
          + "and  ms.memberUuid  = :member           "
          + "and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner",  ownerGroupId              )
      .setString( "member", memberUUID             )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @param memberUUID 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllImmediateByMember(String memberUUID, boolean enabledOnly) 
    throws  GrouperDAOException {
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where  "
          + "     ms.memberUuid = :member           "
          + "and  ms.type       = :type             "
          + "and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "member", memberUUID             )
      .setString( "type",   MembershipType.IMMEDIATE.getTypeString()   )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param ownerGroupId 
   * @param enabledOnly
   * @return list
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public List<Membership> findAllByGroupOwnerAsList(String ownerGroupId, boolean enabledOnly)
    throws  GrouperDAOException
  {
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where ms.ownerGroupId = :owner "
          + "and ms.memberUuid = m.uuid");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }

    List<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("owner", ownerGroupId)
      .list(Object[].class);
    return _getMembershipsFromMembershipAndMemberQueryAsList(mships);
  }

  /**
   * @param ownerStemId 
   * @param enabledOnly
   * @return list
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public List<Membership> findAllByStemOwnerAsList(String ownerStemId, boolean enabledOnly)
    throws  GrouperDAOException
  {
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where ms.ownerStemId = :owner "
          + "and ms.memberUuid = m.uuid");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    List<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("owner", ownerStemId)
      .list(Object[].class);
    return _getMembershipsFromMembershipAndMemberQueryAsList(mships);
  }


  /**
   * @param memberUUID 
   * @param f 
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllImmediateByMemberAndField(String memberUUID, Field f, boolean enabledOnly) 
    throws  GrouperDAOException
  {
    // I'm adding a check for the immediate field id to help with performance 
    // in cases where the member has a lot of memberships but with different fields
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where ms.memberUuid = m.uuid and ");
    sql.append("ms.memberUuid = :member and ms.immediateFieldId = :fuuid and ms.fieldId = :fuuid and ms.type = :type");

    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "type",   MembershipType.IMMEDIATE.getTypeString()   )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param memberUUID 
   * @param fieldType
   * @param enabledOnly
   * @return set
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Set<Membership> findAllImmediateByMemberAndFieldType(String memberUUID, String fieldType, boolean enabledOnly) 
    throws  GrouperDAOException
  {
    
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m, Field as field where  "
          + "     ms.memberUuid = :member           "
          + "and  ms.fieldId = field.uuid "
          + "and  field.typeString       = :ftype             "
          + "and  ms.type       = :type             "
          + "and ms.memberUuid = m.uuid");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "member", memberUUID             )
      .setString( "ftype",  fieldType )
      .setString( "type",   MembershipType.IMMEDIATE.getTypeString()   )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  } 

  /**
   * @param uuid
   * @param exceptionIfNull
   * @param enabledOnly
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   */
  public Membership findByUuid(String uuid, boolean exceptionIfNull, boolean enabledOnly)
      throws GrouperDAOException, MembershipNotFoundException {
    //TODO CH 20120316 do not disable cache for this
    return findByUuid(uuid, exceptionIfNull, enabledOnly, new QueryOptions().secondLevelCache(false));
  }
    
  /**
   * @param uuid
   * @param exceptionIfNull
   * @param enabledOnly
   * @param queryOptions 
   * @return membership
   * @throws GrouperDAOException 
   * @throws MembershipNotFoundException 
   */
  public Membership findByUuid(String uuid, boolean exceptionIfNull, boolean enabledOnly, QueryOptions queryOptions)
      throws GrouperDAOException, MembershipNotFoundException {
    
    int index = uuid.indexOf(Membership.membershipIdSeparator);
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic().setCacheable(false)
      .setCacheRegion(KLASS);

    StringBuilder sql = new StringBuilder();
    
    if (index != -1) {
      String immediateMembershipId = uuid.substring(0, index);
      String groupSetId = uuid.substring(index + 1);
      sql.append("select ms, m from MembershipEntry as ms, Member as m where ms.immediateMembershipId = :immediateMembershipId "
               + "and ms.groupSetId = :groupSetId and ms.memberUuid = m.uuid");
      byHqlStatic.setString("immediateMembershipId", immediateMembershipId).setString("groupSetId", groupSetId);

    } else {
      //this is an immediate membership
      sql.append("select ms, m from MembershipEntry as ms, Member as m where ms.immediateMembershipId = :immediateMembershipId " +
          "and ms.memberUuid = m.uuid and ms.type = 'immediate'");
      byHqlStatic.setString("immediateMembershipId", uuid);
    }
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    byHqlStatic.createQuery(sql.toString()).options(queryOptions).setCacheable(true).setCacheRegion(KLASS);
    
    Object[] result = byHqlStatic.uniqueResult(Object[].class);
    if (result==null || result[0] == null) {
      if (exceptionIfNull) {
        throw new MembershipNotFoundException("could not find membership with uuid: " + Quote.single(uuid));
      }
      return null;
    }
    assignMemberOwnerToMembership(result);
    Membership ms = (Membership)result[0];
    return ms;
    
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findMembershipsByMemberAndFieldSecure(edu.internet2.middleware.grouper.GrouperSession, java.lang.String, edu.internet2.middleware.grouper.Field, boolean)
   */
  public Set<Membership> findMembershipsByMemberAndFieldSecure(GrouperSession grouperSession, 
        String memberUUID, Field f, boolean enabledOnly)
      throws  GrouperDAOException {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m ");
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    
    //see if we are adding more to the query
    boolean changedQuery = false;
    boolean securableField = FieldType.ACCESS.equals(f.getType()) || FieldType.LIST.equals(f.getType());
    
    if (securableField) {
      changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
          grouperSession.getSubject(), byHqlStatic, 
          sql, "ms.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);
    }

    if (changedQuery && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }

    // I'm adding a check for the immediate field id and type to help with performance 
    // in cases where the member has a lot of memberships but with different fields
    
    sql.append(" ms.memberUuid = m.uuid and ");
    
    sql.append("((ms.memberUuid = :member and ms.immediateFieldId = :fuuid and ms.fieldId = :fuuid and ms.depth = '0') "
        + "or (ms.memberUuid = :member and ms.immediateFieldId = :defaultMembersField and ms.fieldId = :fuuid and ms.type = 'effective'))");
    
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "member", memberUUID)
      .setString( "fuuid" , f.getUuid())
      .setString("defaultMembersField", Group.getDefaultList().getUuid())
      .listSet(Object[].class);
    Set<Membership> memberships = _getMembershipsFromMembershipAndMemberQuery(mships);

    Member member = MemberFinder.findByUuid(grouperSession, memberUUID, true);
    Subject subject = member.getSubject();
      
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
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#save(edu.internet2.middleware.grouper.Membership)
   */
  public void save(Membership ms) {
    HibernateSession.byObjectStatic().setEntityName("ImmediateMembershipEntry").save(ms);
    Hib3DAO.evictEntity("MembershipEntry");
    Hib3DAO.evictQueries(KLASS);
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#save(java.util.Set)
   */
  public void save(Set<Membership> mships) {
    HibernateSession.byObjectStatic().setEntityName("ImmediateMembershipEntry").saveBatch(mships);
    Hib3DAO.evictEntity("MembershipEntry");
    Hib3DAO.evictQueries(KLASS);
  }
  

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#delete(edu.internet2.middleware.grouper.Membership)
   */
  public void delete(Membership ms) {
    HibernateSession.byObjectStatic().setEntityName("ImmediateMembershipEntry").delete(ms);
    Hib3DAO.evictEntity("MembershipEntry");
    Hib3DAO.evictQueries(KLASS);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#update(edu.internet2.middleware.grouper.Membership)
   */
  public void update(Membership ms) {
    HibernateSession.byObjectStatic().setEntityName("ImmediateMembershipEntry").update(ms);
    Hib3DAO.evictEntity("MembershipEntry");
    Hib3DAO.evictQueries(KLASS);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#delete(java.util.Set)
   */
  public void delete(Set<Membership> mships) {
    
    HibernateSession.byObjectStatic().setEntityName(
        "ImmediateMembershipEntry").deleteBatch(mships);
    Hib3DAO.evictEntity("MembershipEntry");
    Hib3DAO.evictQueries(KLASS);

  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#update(java.util.Set)
   */
  public void update(Set<Membership> mships) {
    HibernateSession.byObjectStatic().setEntityName("ImmediateMembershipEntry").updateBatch(mships);
    Hib3DAO.evictEntity("MembershipEntry");
    Hib3DAO.evictQueries(KLASS);
  }

  /**
   * @param hibernateSession
   * @throws HibernateException
   */
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    Session hs = hibernateSession.getSession();

    hs.createQuery("delete from ImmediateMembershipEntry").executeUpdate();

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
      assignMemberOwnerToMembership(tuple);
      Membership currMembership = (Membership)tuple[0];
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
      assignMemberOwnerToMembership(tuple);
      Membership currMembership = (Membership)tuple[0];
      memberships.add(currMembership);
    }
    return memberships;
      

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findParentMembership(edu.internet2.middleware.grouper.Membership)
   */
  public Membership findParentMembership(Membership _ms) throws GrouperDAOException {
    Object result[] = HibernateSession
        .byHqlStatic()
        .createQuery(
            "select ms, m from MembershipEntry as ms, Member as m where ms.groupSetId = :groupSetId "
                + "and m.subjectIdDb = :groupId and ms.memberUuid = m.uuid")
        .setCacheable(false)     
        .setCacheRegion(KLASS)
        .setString("groupSetId", _ms.getGroupSetParentId())
        .setString("groupId", _ms.getViaGroupId())
        .uniqueResult(Object[].class);

    if (result == null || result[0] == null) {
      throw new MembershipNotFoundException();
    }
    
    assignMemberOwnerToMembership(result);
    Membership ms = (Membership) result[0];
    return ms;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findMissingImmediateGroupSetsForGroupOwners()
   */
  public Set<Membership> findMissingImmediateGroupSetsForGroupOwners() {
    String sql = "select ms, m from ImmediateMembershipEntry as ms, Member as m " +
        "where ms.ownerGroupId is not null and ms.type = 'immediate' " +
        "and ms.enabledDb = 'T' and ms.memberUuid = m.uuid and m.subjectTypeId = 'group' " +
        "and not exists ( " +
        "select gs.ownerGroupId from GroupSet as gs where gs.ownerGroupId = ms.ownerGroupId " +
        "and gs.memberGroupId = m.subjectIdDb and gs.fieldId = ms.fieldId and gs.depth='1' " +
        ")";
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .listSet(Object[].class);
    
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findMissingImmediateGroupSetsForStemOwners()
   */
  public Set<Membership> findMissingImmediateGroupSetsForStemOwners() {
    String sql = "select ms, m from ImmediateMembershipEntry as ms, Member as m " +
        "where ms.ownerStemId is not null and ms.type = 'immediate' " +
        "and ms.enabledDb = 'T' and ms.memberUuid = m.uuid and m.subjectTypeId = 'group' " +
        "and not exists ( " +
        "select gs.ownerStemId from GroupSet as gs where gs.ownerStemId = ms.ownerStemId " +
        "and gs.memberGroupId = m.subjectIdDb and gs.fieldId = ms.fieldId and gs.depth='1' " +
        ")";
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .listSet(Object[].class);
    
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findByAttrDefOwnerAndMemberAndFieldAndType(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field, java.lang.String, boolean, boolean)
   */
  public Membership findByAttrDefOwnerAndMemberAndFieldAndType(String ownerAttrDefId,
      String memberUUID, Field f, String type, boolean exceptionIfNull,
      boolean enabledOnly) throws GrouperDAOException, MembershipNotFoundException {
    
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
    
    StringBuilder sql = new StringBuilder(
        "select distinct ms, m from MembershipEntry as ms, Member as m, Field as field where  "
            + "     ms.ownerAttrDefId  = :owner            "
            + "and  ms.memberUuid = :member           "
            + "and  ms.fieldId = :fuuid "
            + "and  ms.memberUuid = m.uuid  "
            + "and  ms.type  " + membershipType.queryClause());
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }

    Object result[] = HibernateSession.byHqlStatic()
        .createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS)
        .setString("owner", ownerAttrDefId)
        .setString("member", memberUUID)
        .setString("fuuid", f.getUuid())
        .uniqueResult(Object[].class);

    if (result == null || result[0] == null) {
      if (exceptionIfNull) {
        throw new MembershipNotFoundException();
      }
      return null;
    }
    assignMemberOwnerToMembership(result);
    Membership ms = (Membership) result[0];
    return ms;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllEffectiveByAttrDefOwner(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field, java.lang.String, int, boolean)
   */
  public Set<Membership> findAllEffectiveByAttrDefOwner(String ownerAttrDefId,
      String memberUUID, Field f, String viaGroupId, int depth, boolean enabledOnly)
      throws GrouperDAOException {
    if (depth <= 0) {
      return new LinkedHashSet<Membership>();
    }
    
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m, Field as field where  "
          + "     ms.ownerAttrDefId  = :owner            "
          + "and  ms.memberUuid = :member           "
          + "and  ms.fieldId = :fuuid "
          + "and  ms.viaGroupId    = :via              "
          + "and  ms.depth      = :depth            "
          + "and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner",  ownerAttrDefId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "via",    viaGroupId                )
      .setInteger("depth",  depth                  )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllMembersByAttrDefOwnerAndFieldAndType(java.lang.String, edu.internet2.middleware.grouper.Field, java.lang.String, edu.internet2.middleware.grouper.internal.dao.QueryOptions, boolean)
   */
  public Set<Member> findAllMembersByAttrDefOwnerAndFieldAndType(String ownerAttrDefId,
      Field f, String type, QueryOptions queryOptions, boolean enabledOnly)
      throws GrouperDAOException {
    
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
    
    StringBuilder sql = new StringBuilder("select distinct m "
        + "from Member m, MembershipEntry ms where "
        + "ms.ownerAttrDefId = :owner "
        + "and ms.fieldId = :fieldId "
        + "and ms.type " + membershipType.queryClause()
        + " and ms.memberUuid = m.uuid  ");
      if (enabledOnly) {
        sql.append(" and ms.enabledDb = 'T'");
      }
      
      if (queryOptions != null) {
        Hib3MemberDAO.massageMemberSortFields(queryOptions.getQuerySort());
      }

      return HibernateSession.byHqlStatic()
        .createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS)
        .options(queryOptions)
        .setString("owner", ownerAttrDefId)
        .setString("fieldId", f.getUuid() )
        .listSet(Member.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findMissingImmediateGroupSetsForAttrDefOwners()
   */
  public Set<Membership> findMissingImmediateGroupSetsForAttrDefOwners() {
    String sql = "select ms, m from ImmediateMembershipEntry as ms, Member as m " +
        "where ms.ownerAttrDefId is not null and ms.type = 'immediate' " +
        "and ms.enabledDb = 'T' and ms.memberUuid = m.uuid and m.subjectTypeId = 'group' " +
        "and not exists ( " +
        "select gs.ownerAttrDefId from GroupSet as gs where gs.ownerAttrDefId = ms.ownerAttrDefId " +
        "and gs.memberGroupId = m.subjectIdDb and gs.fieldId = ms.fieldId and gs.depth='1' " +
        ")";
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .listSet(Object[].class);
    
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByAttrDefOwnerAndField(java.lang.String, edu.internet2.middleware.grouper.Field, boolean)
   */
  public Set<Membership> findAllByAttrDefOwnerAndField(String ownerAttrDefId, Field f,
      boolean enabledOnly) throws GrouperDAOException {
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + "     ms.ownerAttrDefId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid  ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner", ownerAttrDefId                )
      .setString( "fuuid" , f.getUuid()             )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByAttrDefOwnerAndFieldAndType(java.lang.String, edu.internet2.middleware.grouper.Field, java.lang.String, boolean)
   */
  public Set<Membership> findAllByAttrDefOwnerAndFieldAndType(String ownerAttrDefId,
      Field f, String type, boolean enabledOnly) throws GrouperDAOException {
    
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + "     ms.ownerAttrDefId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid         "
        + "and  ms.type " + membershipType.queryClause());
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
        .createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS)
        .setString( "owner" , ownerAttrDefId                 )
        .setString( "fuuid" , f.getUuid()             )
        .listSet(Object[].class);   
  
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByAttrDefOwnerAndMemberAndField(java.lang.String, java.lang.String, edu.internet2.middleware.grouper.Field, boolean)
   */
  public Set<Membership> findAllByAttrDefOwnerAndMemberAndField(String ownerAttrDefId,
      String memberUUID, Field f, boolean enabledOnly) throws GrouperDAOException {
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + "     ms.ownerAttrDefId   = :owner            "  
        + "and  ms.memberUuid  = :member           "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.memberUuid  = m.uuid   ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner",  ownerAttrDefId              )
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .listSet(Object[].class);
       return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByAttrDefOwnerAsList(java.lang.String, boolean)
   */
  public List<Membership> findAllByAttrDefOwnerAsList(String attrDefId,
      boolean enabledOnly) throws GrouperDAOException {
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where ms.ownerAttrDefId = :owner "
          + "and ms.memberUuid = m.uuid");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    List<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("owner", attrDefId)
      .list(Object[].class);
    return _getMembershipsFromMembershipAndMemberQueryAsList(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllImmediateByAttrDefOwnerAsList(java.lang.String, boolean)
   */
  public List<Membership> findAllImmediateByAttrDefOwnerAsList(String attrDefId,
      boolean enabledOnly) throws GrouperDAOException {
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where ms.ownerAttrDefId = :owner and ms.type = :type "
          + "and ms.memberUuid = m.uuid");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    List<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString("owner", attrDefId)
      .setString( "type",   MembershipType.IMMEDIATE.getTypeString()   )
      .list(Object[].class);
    return _getMembershipsFromMembershipAndMemberQueryAsList(mships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByAttrDefOwnerAndMember(java.lang.String, java.lang.String, boolean)
   */
  public Set<Membership> findAllByAttrDefOwnerAndMember(String ownerAttrDefId,
      String memberUUID, boolean enabledOnly) throws GrouperDAOException {

    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where  "
          + "     ms.ownerAttrDefId   = :owner            "  
          + "and  ms.memberUuid  = :member           "
          + "and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "owner",  ownerAttrDefId              )
      .setString( "member", memberUUID             )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllEnabledDisabledMismatch()
   */
  public Set<Membership> findAllEnabledDisabledMismatch() {
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    String adminFieldId = AccessPrivilege.ADMIN.getField().getId();
    // dividing up into 4 queries instead of a single large query with outer joins
    
    long now = System.currentTimeMillis();

    {
      // owner and member are both groups
      StringBuilder sql = new StringBuilder(
          "select ms from ImmediateMembershipEntry as ms, Group as g, Member as m, Group as mg where ms.ownerGroupId = g.uuid and ms.memberUuid = m.uuid and m.subjectIdDb = mg.uuid and m.subjectSourceIdDb in ('g:gsa', 'grouperEntities') and ("
            + "(ms.enabledDb = 'F' and (ms.enabledTimeDb is null or ms.enabledTimeDb < :now) and (ms.disabledTimeDb is null or ms.disabledTimeDb > :now) and (g.enabledDb = 'T' or ms.fieldId = '" + adminFieldId + "') and (mg.enabledDb = 'T' or (g.uuid = mg.uuid and ms.fieldId = '" + adminFieldId + "'))) "
            + " or (ms.enabledDb = 'T' and g.enabledDb = 'F' and ms.fieldId <> '" + adminFieldId + "') "
            + " or (ms.enabledDb = 'T' and mg.enabledDb = 'F' and (g.uuid <> mg.uuid or ms.fieldId <> '" + adminFieldId + "') ) "
            + " or (ms.enabledDb = 'T' and ms.disabledTimeDb < :now) "
            + " or (ms.enabledDb = 'T' and ms.enabledTimeDb > :now) "
            + " or (ms.enabledDb <> 'T' and ms.enabledDb <> 'F') "
            + " or (ms.enabledDb is null)) "
       );
  
      memberships.addAll(HibernateSession.byHqlStatic()
        .createQuery(sql.toString())
        .setCacheable(false)
        .setLong( "now",  now )
        .listSet(Membership.class));
    }
    
    {
      // owner is a group, member is not a group
      StringBuilder sql = new StringBuilder(
          "select ms from ImmediateMembershipEntry as ms, Group as g, Member as m where ms.ownerGroupId = g.uuid and ms.memberUuid = m.uuid and m.subjectSourceIdDb not in ('g:gsa', 'grouperEntities') and ("
            + "(ms.enabledDb = 'F' and (ms.enabledTimeDb is null or ms.enabledTimeDb < :now) and (ms.disabledTimeDb is null or ms.disabledTimeDb > :now) and (g.enabledDb = 'T' or ms.fieldId = '" + adminFieldId + "')) "
            + " or (ms.enabledDb = 'T' and g.enabledDb = 'F' and ms.fieldId <> '" + adminFieldId + "') "
            + " or (ms.enabledDb = 'T' and ms.disabledTimeDb < :now) "
            + " or (ms.enabledDb = 'T' and ms.enabledTimeDb > :now) "
            + " or (ms.enabledDb <> 'T' and ms.enabledDb <> 'F') "
            + " or (ms.enabledDb is null)) "
       );
  
      memberships.addAll(HibernateSession.byHqlStatic()
        .createQuery(sql.toString())
        .setCacheable(false)
        .setLong( "now",  now )
        .listSet(Membership.class));
    }
    
    {
      // owner is not a group, member is a group
      StringBuilder sql = new StringBuilder(
          "select ms from ImmediateMembershipEntry as ms, Member as m, Group as mg where ms.memberUuid = m.uuid and ms.ownerGroupId is null and m.subjectIdDb = mg.uuid and m.subjectSourceIdDb in ('g:gsa', 'grouperEntities') and ("
            + "(ms.enabledDb = 'F' and (ms.enabledTimeDb is null or ms.enabledTimeDb < :now) and (ms.disabledTimeDb is null or ms.disabledTimeDb > :now) and mg.enabledDb = 'T') "
            + " or (ms.enabledDb = 'T' and mg.enabledDb = 'F') "
            + " or (ms.enabledDb = 'T' and ms.disabledTimeDb < :now) "
            + " or (ms.enabledDb = 'T' and ms.enabledTimeDb > :now) "
            + " or (ms.enabledDb <> 'T' and ms.enabledDb <> 'F') "
            + " or (ms.enabledDb is null)) "
       );
  
      memberships.addAll(HibernateSession.byHqlStatic()
        .createQuery(sql.toString())
        .setCacheable(false)
        .setLong( "now",  now )
        .listSet(Membership.class));
    }
    
    {
      // neither owner nor member are groups
      StringBuilder sql = new StringBuilder(
          "select ms from ImmediateMembershipEntry as ms, Member as m where ms.memberUuid = m.uuid and ms.ownerGroupId is null and m.subjectSourceIdDb not in ('g:gsa', 'grouperEntities') and ("
            + "(ms.enabledDb = 'F' and (ms.enabledTimeDb is null or ms.enabledTimeDb < :now) and (ms.disabledTimeDb is null or ms.disabledTimeDb > :now)) "
            + " or (ms.enabledDb = 'T' and ms.disabledTimeDb < :now) "
            + " or (ms.enabledDb = 'T' and ms.enabledTimeDb > :now) "
            + " or (ms.enabledDb <> 'T' and ms.enabledDb <> 'F') "
            + " or (ms.enabledDb is null)) "
       );
  
      memberships.addAll(HibernateSession.byHqlStatic()
        .createQuery(sql.toString())
        .setCacheable(false)
        .setLong( "now",  now )
        .listSet(Membership.class));
    }
    
    return memberships;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllNonImmediateByMember(java.lang.String, boolean)
   */
  public Set<Membership> findAllNonImmediateByMember(String memberUUID, boolean enabledOnly)
      throws GrouperDAOException {
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m where  "
          + "     ms.memberUuid = :member           "
          + "and  ms.type       != :type             "
          + "and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "member", memberUUID             )
      .setString( "type",   MembershipType.IMMEDIATE.getTypeString()   )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllNonImmediateByMemberAndField(java.lang.String, edu.internet2.middleware.grouper.Field, boolean)
   */
  public Set<Membership> findAllNonImmediateByMemberAndField(String memberUUID, Field f,
      boolean enabledOnly) throws GrouperDAOException {
    // I'm adding a check for the immediate field id to help with performance 
    // in cases where the member has a lot of memberships but with different fields
    
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where ms.memberUuid = m.uuid and ");
    sql.append("ms.memberUuid = :member and ms.immediateFieldId = :fuuid and ms.fieldId = :fuuid and ms.type != :type");

    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "member", memberUUID             )
      .setString( "fuuid",  f.getUuid()            )
      .setString( "type",   MembershipType.IMMEDIATE.getTypeString()   )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllNonImmediateByMemberAndFieldType(java.lang.String, java.lang.String, boolean)
   */
  public Set<Membership> findAllNonImmediateByMemberAndFieldType(String memberUUID,
      String fieldType, boolean enabledOnly) throws GrouperDAOException {
    StringBuilder sql = new StringBuilder(
        "select ms, m from MembershipEntry as ms, Member as m, Field as field where  "
          + "     ms.memberUuid = :member           "
          + "and  ms.fieldId = field.uuid "
          + "and  field.typeString       = :ftype             "
          + "and  ms.type       != :type             "
          + "and ms.memberUuid = m.uuid");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }
    
    Set<Object[]> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "member", memberUUID             )
      .setString( "ftype",  fieldType )
      .setString( "type",   MembershipType.IMMEDIATE.getTypeString()   )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findByImmediateUuidOrKey(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public Membership findByImmediateUuidOrKey(String uuid, String memberUUID, String fieldId,
      String ownerAttrDefId, String ownerGroupId, String ownerStemId,
      boolean exceptionIfNull) throws GrouperDAOException {
    try {
      String theHqlQuery = "from ImmediateMembershipEntry as theMembership where theMembership.immediateMembershipId = :uuid or " +
          " ( theMembership.fieldId = :theFieldId and theMembership.memberUuid = :theMemberId " +
          " and theMembership.ownerGroupId " + HibUtils.equalsOrIs(ownerGroupId, "theOwnerGroupId") + " and theMembership.ownerStemId " +
              HibUtils.equalsOrIs(ownerStemId, "theOwnerStemId") +
          " and theMembership.ownerAttrDefId " + HibUtils.equalsOrIs(ownerAttrDefId, "theOwnerAttrDefId") + " )";
        
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
        .createQuery(theHqlQuery)
        .setCacheable(true)
        .setCacheRegion(KLASS)
        .setString("uuid", uuid)
        .setString("theFieldId", fieldId)
        .setString("theMemberId", memberUUID);

      //dont attach these if null
      if (ownerGroupId != null) {
        byHqlStatic.setString("theOwnerGroupId", ownerGroupId);
      }
      if (ownerStemId != null) {
        byHqlStatic.setString("theOwnerStemId", ownerStemId);
      }
      if (ownerAttrDefId != null) {
        byHqlStatic.setString("theOwnerAttrDefId", ownerAttrDefId);
      }
      Membership membership = byHqlStatic.uniqueResult(Membership.class);

      if (membership == null && exceptionIfNull) {
        throw new RuntimeException("Can't find membership by uuid: '" + uuid + "' or memberUUID '" + memberUUID 
            + "', fieldId: '" + fieldId + "', ownerAttrDefId: '" + ownerAttrDefId + "', ownerGroupId: '"
            + ownerGroupId + "', ownerStemId: '" + ownerStemId + "'");
      }
      return membership;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find membership by uuid: '" + uuid + "' or memberUUID '" + memberUUID 
            + "', fieldId: '" + fieldId + "', ownerAttrDefId: '" + ownerAttrDefId + "', ownerGroupId: '"
            + ownerGroupId + "', ownerStemId: '" + ownerStemId + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#saveUpdateProperties(edu.internet2.middleware.grouper.Membership)
   */
  public void saveUpdateProperties(Membership membership) {

    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update ImmediateMembershipEntry " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "creatorUuid = :theCreatorUuid, " +
        "createTimeLong = :theCreateTimeLong " +
        "where immediateMembershipId = :theImmediateMembershipId")
        .setLong("theHibernateVersionNumber", membership.getHibernateVersionNumber())
        .setString("theCreatorUuid", membership.getCreatorUuid())
        .setLong("theCreateTimeLong", membership.getCreateTimeLong())
        .setString("theContextId", membership.getContextId())
        .setString("theImmediateMembershipId", membership.getImmediateMembershipId())
        .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findSourceIdsByGroupOwnerOptions(java.lang.String, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, java.lang.Boolean)
   */
  public Set<String> findSourceIdsByGroupOwnerOptions(String groupId,
      MembershipType membershipType, Field field, Boolean enabled) {
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Group group = GroupFinder.findByUuid(grouperSession, groupId, true);
    
    if (field == null) {
      field = Group.getDefaultList();
    }

    //needs to be a members field
    if (!StringUtils.equals("list",field.getTypeString())) {
      throw new RuntimeException("This method only works with list fields: " + field);
    }

    PrivilegeHelper.dispatch( grouperSession, group, grouperSession.getSubject(), field.getReadPriv() );
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder("select distinct m.subjectSourceIdDb from Member m, MembershipEntry ms ");
    
    sql.append(" where ms.memberUuid = m.uuid and ms.ownerGroupId = :ownerGroupId ");
    
    if (enabled != null && enabled) {
      sql.append(" and ms.enabledDb = 'T' ");
    }
    if (enabled != null && !enabled) {
      sql.append(" and ms.enabledDb = 'F' ");
    }
    //immediate or effective, etc
    if (membershipType != null) {
      sql.append(" and ms.type ").append(membershipType.queryClause()).append(" ");
    }
    sql.append(" and ms.fieldId = :fieldId ");
    byHqlStatic.setString("fieldId", field.getUuid());
    byHqlStatic.setString("ownerGroupId", groupId);
    
    byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS);
    
    Set<String> results = byHqlStatic.createQuery(sql.toString()).listSet(String.class);

    return results;
    
  }

  /**
   * @see  MembershipDAO#findAllByStemParentOfGroupOwnerAndFieldAndType(Stem, Scope, Field, MembershipType, Boolean, String)
   */
  public Set<Membership> findAllByStemParentOfGroupOwnerAndFieldAndType(
      Stem stem, Stem.Scope stemScope, Field field, MembershipType membershipType, Boolean enabledOnly, String memberId)
      throws GrouperDAOException {
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m, Group as g where  "
        + "     ms.ownerGroupId   = g.uuid            "
        + " and  ms.fieldId = :fuuid "
        + " and  ms.memberUuid  = m.uuid         "
        + " and ms.memberUuid = :memberId ");
    
    if (stemScope == Scope.ONE) {
      sql.append(" and g.parentUuid = :stemId ");
      byHqlStatic.setString( "stemId" , stem.getUuid());
      
    } else if (stemScope == Scope.SUB) {
      sql.append(" and g.nameDb like :ownerStemName ");
      byHqlStatic.setString( "ownerStemName" , stem.getName() + ":%");
    } else {
      throw new RuntimeException("Cant find scope: " + stemScope.name()); 
    }
    
    if (membershipType != null) {
      sql.append(" and  ms.type  " + membershipType.queryClause());
    }
    
    if (enabledOnly != null && enabledOnly) {
      sql.append(" and ms.enabledDb = 'T' ");
    }
    if (enabledOnly != null && !enabledOnly) {
      sql.append(" and ms.enabledDb = 'F' ");
    }
    
    Set<Object[]> mships = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "fuuid",  field.getUuid())
      .setString( "memberId",  memberId)
      .listSet(Object[].class);

    return _getMembershipsFromMembershipAndMemberQuery(mships);

  }

  
  
  /**
   * @see MembershipDAO#findAllMembersInOneGroupNotOtherAndType(String, String, String, String, QueryOptions, Boolean, boolean)
   */
  public Set<Member> findAllMembersInOneGroupNotOtherAndType(String ownerInGroupId,
      String ownerNotInGroupId, String typeIn, String typeNotIn,
      QueryOptions queryOptions, Boolean enabled, boolean disabledOwnerNull) throws GrouperDAOException {
    
    MembershipType typeInEnum = MembershipType.valueOfIgnoreCase(typeIn, false);
    MembershipType typeNotInEnum = MembershipType.valueOfIgnoreCase(typeNotIn, false);
    StringBuilder sql = new StringBuilder("select distinct m from MembershipEntry as inMembershipEntry, Member as m where  "
        + " inMembershipEntry.ownerGroupId   = :ownerInGroupId            ");
    
    if (disabledOwnerNull) {
      sql.append(" and inMembershipEntry.disabledTimeDb is null ");
    }
    
    sql.append(" and inMembershipEntry.memberUuid   = m.uuid            "
        + " and  inMembershipEntry.fieldId = '" + Group.getDefaultList().getUuid() + "' ");

    if (typeInEnum != null) {
      sql.append(" and  inMembershipEntry.type  " + typeInEnum.queryClause());
    }
    if (enabled != null) {
      if (enabled) {
        sql.append(" and inMembershipEntry.enabledDb = 'T' ");
      } else {
        sql.append(" and inMembershipEntry.enabledDb = 'F' ");
      }
    }
    sql.append(" and  m.uuid not in ( select notInMembershipEntry.memberUuid from MembershipEntry as notInMembershipEntry " +
            " where notInMembershipEntry.ownerGroupId = :ownerNotInGroupId "
            + " and notInMembershipEntry.fieldId = '" + Group.getDefaultList().getUuid() + "' ");
    if (typeNotInEnum != null) {
      sql.append(" and notInMembershipEntry.type  " + typeNotInEnum.queryClause() );
    }
    if (enabled != null) {
      if (enabled) {
        sql.append(" and notInMembershipEntry.enabledDb = 'T' ");
      } else {
        sql.append(" and notInMembershipEntry.enabledDb = 'F' ");
      }
    }
    sql.append(" ) ");
            
    if (queryOptions != null) {
      Hib3MemberDAO.massageMemberSortFields(queryOptions.getQuerySort());
    }

    Set<Member> members = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .options(queryOptions)
      .setString( "ownerInGroupId" , ownerInGroupId                 )
      .setString( "ownerNotInGroupId" , ownerNotInGroupId                 )
      .listSet(Member.class);

    return members;

    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllMembershipsByGroupOwnerFieldDisabledRange(java.lang.String, edu.internet2.middleware.grouper.Field, java.sql.Timestamp, java.sql.Timestamp)
   */
  public Set<Membership> findAllMembershipsByGroupOwnerFieldDisabledRange (
      String ownerGroupId, Field f, Timestamp disabledDateFrom,
      Timestamp disabledDateTo) {

    if (disabledDateFrom == null && disabledDateTo == null) {
      throw new RuntimeException("Need to pass in disabledFrom or disabledTo");
    }
    
    //if they got it backwards, then fix it for them
    if (disabledDateFrom != null && disabledDateTo != null 
        && disabledDateFrom.getTime() > disabledDateTo.getTime()) {
      
      Timestamp temp = disabledDateFrom;
      disabledDateFrom = disabledDateTo;
      disabledDateTo = temp;
      
    }
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder(
        "select theMembershipEntry from MembershipEntry as theMembershipEntry where  "
        + " theMembershipEntry.ownerGroupId   = :ownerInGroupId            "
        + " and  theMembershipEntry.fieldId = :theFieldId and theMembershipEntry.enabledDb = 'T' ");
    
    if (disabledDateFrom != null) {
      sql.append(" and theMembershipEntry.disabledTimeDb >= :disabledDateFrom ");
      byHqlStatic.setLong( "disabledDateFrom" , disabledDateFrom.getTime() );
    }
    if (disabledDateTo != null) {
      sql.append(" and theMembershipEntry.disabledTimeDb <= :disabledDateTo ");
      byHqlStatic.setLong( "disabledDateTo" , disabledDateTo.getTime() );
    }

    sql.append(
        " and not exists ( select validMembershipEntry.uuid from MembershipEntry as validMembershipEntry " +
        " where validMembershipEntry.ownerGroupId = theMembershipEntry.ownerGroupId " +
        " and validMembershipEntry.fieldId = theMembershipEntry.fieldId " +
        " and validMembershipEntry.memberUuid = theMembershipEntry.memberUuid " +
        " and validMembershipEntry.enabledDb = 'T' and ( validMembershipEntry.disabledTimeDb is null ");

    if (disabledDateTo != null) {
      sql.append(" or validMembershipEntry.disabledTimeDb > :disabledDateTo ");
    } else if (disabledDateFrom != null) {
      sql.append(" or validMembershipEntry.disabledTimeDb < :disabledDateFrom ");
    }
    
    
    sql.append(") )");
    
    Set<Membership> memberships = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "ownerInGroupId" , ownerGroupId )
      .setString( "theFieldId" , Group.getDefaultList().getUuid() )
      .listSet(Membership.class);

    return memberships;

  }

  /**
   * @see MembershipDAO#findAllMembersInOneGroupNotStem(String, Stem, Scope, String, QueryOptions)
   */
  public Set<Member> findAllMembersInOneGroupNotStem(String ownerInGroupId,
      Stem ownerNotInStem, Stem.Scope stemScope, String typeIn, QueryOptions queryOptions) {

    MembershipType typeInEnum = MembershipType.valueOfIgnoreCase(typeIn, false);

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

    StringBuilder sql = new StringBuilder("select m from MembershipEntry as inMembershipEntry, Member as m where  "
        + " inMembershipEntry.ownerGroupId   = :ownerInGroupId            "
        + " and inMembershipEntry.memberUuid   = m.uuid            "
        + " and  inMembershipEntry.fieldId = '" + Group.getDefaultList().getUuid() + "' ");
    if (typeInEnum != null) {
      sql.append(" and  inMembershipEntry.type  " + typeInEnum.queryClause());
    }

    sql.append(" and  not exists ( select notInMembershipEntry.memberUuid " +
        " from MembershipEntry as notInMembershipEntry, Group as theStemGroup " +
            " where notInMembershipEntry.ownerGroupId = theStemGroup.uuid "
            + " and notInMembershipEntry.memberUuid = m.uuid "
            + " and notInMembershipEntry.fieldId = '" + Group.getDefaultList().getUuid() + "' ");
    
    switch (stemScope) {
      case ONE:
        
        sql.append(" and theStemGroup.parentUuid = :stemId ");
        byHqlStatic.setString("stemId", ownerNotInStem.getUuid());
        break;

      case SUB:
        
        sql.append(" and theStemGroup.nameDb like :stemSub ");
        byHqlStatic.setString("stemSub", ownerNotInStem.getName() + ":%");
        
        break;
      default:
        throw new RuntimeException("Not expecting scope: " + stemScope);
    }
    
    sql.append(" ) ");
            
    if (queryOptions != null) {
      Hib3MemberDAO.massageMemberSortFields(queryOptions.getQuerySort());
    }

    Set<Member> members = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS)
      .setString( "ownerInGroupId" , ownerInGroupId )
      .listSet(Member.class);

    return members;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByAttributeDefOwnerOptions(java.lang.String, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Set, java.lang.Boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Object[]> findAllByAttributeDefOwnerOptions(String attributeDefId,
      MembershipType membershipType, Collection<Field> fields, Set<Source> sources,
      Boolean enabled, QueryOptions queryOptions) {
    
    //first get the 
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
        + " ms.ownerAttrDefId   = :owner "
        + " and  ms.memberUuid  = m.uuid   "
        );

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .setCacheable(true)
      .setCacheRegion(KLASS)
      .setString("owner", attributeDefId)
      .options(queryOptions);

    HibUtils.convertFieldsToSqlInString(fields, byHqlStatic, sql, "ms.fieldId");
    HibUtils.convertSourcesToSqlInString(sources, byHqlStatic, sql, "m.subjectSourceIdDb");
    
    if (membershipType != null) {
      sql.append("and  ms.type ").append(membershipType.queryClause());
    }
    
    if (enabled != null) {
      sql.append(" and ms.enabledDb = '" + (enabled ? 'T' : 'F') + "'");
    }
    byHqlStatic.createQuery(sql.toString());

    Set<Object[]> mships = byHqlStatic
      .listSet(Object[].class);
    return mships;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByAttributeDefOwnerOptions(java.lang.String, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Set, java.lang.Boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Object[]> findAllByAttributeDefOwnerOptions(String attributeDefId,
      Collection<String> totalMemberIds, MembershipType membershipType,
      Collection<Field> fields, Set<Source> sources, Boolean enabled,
      QueryOptions queryOptions) {

    
    Set<Object[]> totalResults = new LinkedHashSet<Object[]>();

    List<String> totalMemberIdsList = GrouperUtil.listFromCollection(totalMemberIds);
    
    int memberIdsSize = GrouperUtil.length(totalMemberIds);

    if (memberIdsSize == 0) {
      throw new RuntimeException("Must pass in group(s), member(s), and/or membership(s)");
    }
    
    int memberBatches = GrouperUtil.batchNumberOfBatches(totalMemberIdsList, 100);

    for (int memberIndex = 0; memberIndex < memberBatches; memberIndex++) {
      
      List<String> memberIds = GrouperUtil.batchList(totalMemberIdsList, 100, memberIndex);

      StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m where  "
          + " ms.ownerAttrDefId   = :owner "
          + " and  ms.memberUuid  = m.uuid   "
          );
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
        .setCacheable(true)
        .setCacheRegion(KLASS)
        .setString("owner", attributeDefId)
        .options(queryOptions);

      HibUtils.convertFieldsToSqlInString(fields, byHqlStatic, sql, "ms.fieldId");
      HibUtils.convertSourcesToSqlInString(sources, byHqlStatic, sql, "m.subjectSourceIdDb");
      
      if (membershipType != null) {
        sql.append("and  ms.type ").append(membershipType.queryClause());
      }
      
      if (enabled != null) {
        sql.append(" and ms.enabledDb = '" + (enabled ? 'T' : 'F') + "'");
      }
      sql.append(" and ms.memberUuid in (");
      sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
      sql.append(") ");
      
      byHqlStatic.createQuery(sql.toString());
      Set<Object[]> mships = byHqlStatic.listSet(Object[].class);
      
      totalResults.addAll(mships);
          
    }
    
    return totalResults;
  }

  /**
   * generally you will order by m.subjectSourceIdDb, m.subjectIdDb, and page to the first 100
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllMembersByAttributeDefOwnerOptions(java.lang.String, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Set, java.lang.Boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public List<Member> findAllMembersByAttributeDefOwnerOptions(String attributeDefId,
      MembershipType membershipType, Collection<Field> fields, Set<Source> sources,
      Boolean enabled, QueryOptions queryOptions) {
    
    //first get the 
    StringBuilder sql = new StringBuilder("select distinct m from MembershipEntry as ms, Member as m where  "
        + " ms.ownerAttrDefId   = :owner "
        + " and  ms.memberUuid  = m.uuid   "
        );

    if (queryOptions != null) {
      Hib3MemberDAO.massageMemberSortFields(queryOptions.getQuerySort());
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .setCacheable(true)
      .setCacheRegion(KLASS)
      .setString("owner", attributeDefId)
      .options(queryOptions);

    HibUtils.convertFieldsToSqlInString(fields, byHqlStatic, sql, "ms.fieldId");
    HibUtils.convertSourcesToSqlInString(sources, byHqlStatic, sql, "m.subjectSourceIdDb");
    
    if (membershipType != null) {
      sql.append("and  ms.type ").append(membershipType.queryClause());
    }
    
    if (enabled != null) {
      sql.append(" and ms.enabledDb = '" + (enabled ? 'T' : 'F') + "'");
    }
    
    byHqlStatic.createQuery(sql.toString());

    List<Member> members = byHqlStatic
      .list(Member.class);
    return members;
  }

  /**
   * generally you will order by m.subjectSourceIdDb, m.subjectIdDb, and page to the first 100
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllMembersByGroupOwnerOptions(java.lang.String, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Set, java.lang.Boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public List<Member> findAllMembersByGroupOwnerOptions(String groupId,
      MembershipType membershipType, Collection<Field> fields, Set<Source> sources,
      Boolean enabled, QueryOptions queryOptions) {
    
    //first get the 
    StringBuilder sql = new StringBuilder("select distinct m from MembershipEntry as ms, Member as m, Field as f where  "
        + " ms.ownerGroupId   = :owner "
        + " and  ms.memberUuid  = m.uuid   "
        + " and  ms.fieldId  = f.uuid   "
        + " and  f.typeString  = 'access'   "
        );
  
    if (queryOptions != null) {
      Hib3MemberDAO.massageMemberSortFields(queryOptions.getQuerySort());
    }
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .setCacheable(true)
      .setCacheRegion(KLASS)
      .setString("owner", groupId)
      .options(queryOptions);
  
    HibUtils.convertFieldsToSqlInString(fields, byHqlStatic, sql, "ms.fieldId");
    HibUtils.convertSourcesToSqlInString(sources, byHqlStatic, sql, "m.subjectSourceIdDb");
    
    if (membershipType != null) {
      sql.append("and  ms.type ").append(membershipType.queryClause());
    }
    
    if (enabled != null) {
      sql.append(" and ms.enabledDb = '" + (enabled ? 'T' : 'F') + "'");
    }
    
    byHqlStatic.createQuery(sql.toString());
  
    List<Member> members = byHqlStatic
      .list(Member.class);
    return members;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.lang.String, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Set, java.lang.Boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Object[]> findAllByGroupOwnerOptions(String groupId,
      Collection<String> totalMemberIds, MembershipType membershipType,
      Collection<Field> fields, Set<Source> sources, Boolean enabled,
      QueryOptions queryOptions) {
    
    Set<Object[]> totalResults = new LinkedHashSet<Object[]>();
  
    int memberIdsSize = GrouperUtil.length(totalMemberIds);
  
    if (memberIdsSize == 0) {
      throw new RuntimeException("Must pass in group(s), member(s), and/or membership(s)");
    }
    
    List<String> totalMemberIdsList = GrouperUtil.listFromCollection(totalMemberIds);
    
    int memberBatches = GrouperUtil.batchNumberOfBatches(totalMemberIdsList, 100);
  
    for (int memberIndex = 0; memberIndex < memberBatches; memberIndex++) {
      
      List<String> memberIds = GrouperUtil.batchList(totalMemberIdsList, 100, memberIndex);
  
      StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m, Field as f where  "
          + " ms.ownerGroupId   = :owner "
          + " and  ms.memberUuid  = m.uuid   "
          + " and  ms.fieldId  = f.uuid   "
          + " and  f.typeString  = 'access'   "
          );
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
        .setCacheable(true)
        .setCacheRegion(KLASS)
        .setString("owner", groupId)
        .options(queryOptions);
  
      HibUtils.convertFieldsToSqlInString(fields, byHqlStatic, sql, "ms.fieldId");
      HibUtils.convertSourcesToSqlInString(sources, byHqlStatic, sql, "m.subjectSourceIdDb");
      
      if (membershipType != null) {
        sql.append("and  ms.type ").append(membershipType.queryClause());
      }
      
      if (enabled != null) {
        sql.append(" and ms.enabledDb = '" + (enabled ? 'T' : 'F') + "'");
      }
      sql.append(" and ms.memberUuid in (");
      sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
      sql.append(") ");
      
      byHqlStatic.createQuery(sql.toString());
      Set<Object[]> mships = byHqlStatic.listSet(Object[].class);
      
      totalResults.addAll(mships);
          
    }
    
    return totalResults;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByGroupOwnerOptions(java.lang.String, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Set, java.lang.Boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Object[]> findAllByGroupOwnerOptions(String groupId,
      MembershipType membershipType, Collection<Field> fields, Set<Source> sources,
      Boolean enabled, QueryOptions queryOptions) {
    
    //first get the 
    StringBuilder sql = new StringBuilder("select ms, m from MembershipEntry as ms, Member as m, Field as f where  "
        + " ms.ownerGroupId   = :owner "
        + " and  ms.memberUuid  = m.uuid   "
        + " and  ms.fieldId  = f.uuid   "
        + " and  f.typeString  = 'access'   "
        );
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .setCacheable(true)
      .setCacheRegion(KLASS)
      .setString("owner", groupId)
      .options(queryOptions);
  
    HibUtils.convertFieldsToSqlInString(fields, byHqlStatic, sql, "ms.fieldId");
    HibUtils.convertSourcesToSqlInString(sources, byHqlStatic, sql, "m.subjectSourceIdDb");
    
    if (membershipType != null) {
      sql.append("and  ms.type ").append(membershipType.queryClause());
    }
    
    if (enabled != null) {
      sql.append(" and ms.enabledDb = '" + (enabled ? 'T' : 'F') + "'");
    }
    byHqlStatic.createQuery(sql.toString());
  
    Set<Object[]> mships = byHqlStatic
      .listSet(Object[].class);
    return mships;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllMembershipEntriesByGroupOwnerAndFieldAndType(java.lang.String, edu.internet2.middleware.grouper.Field, java.lang.String, boolean)
   */
  public Set<Membership> findAllMembershipEntriesByGroupOwnerAndFieldAndType(String ownerGroupId, Field f, String type, boolean enabledOnly) {
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
    StringBuilder sql = new StringBuilder("select ms from ImmediateMembershipEntry as ms where "
        + "     ms.ownerGroupId   = :owner            "
        + "and  ms.fieldId = :fuuid "
        + "and  ms.type  " + membershipType.queryClause());
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }

    Set<Membership> mships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembershipEntriesByGroupOwnerAndFieldAndType")
      .setString("owner" , ownerGroupId)
      .setString("fuuid",  f.getUuid())
      .listSet(Membership.class);

    return mships;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findMissingCompositeComplement()
   */
  public Set<Object[]> findMissingComplementMemberships() {
    String sql = "select distinct c.factorOwnerUuid, c.uuid, m.uuid from MembershipEntry ms, Member m, Composite c " +
    		"where c.typeDb = 'complement' " +
    		"and c.leftFactorUuid = ms.ownerGroupId " +
    		"and ms.fieldId = :fieldId " +
    		"and ms.enabledDb = 'T' " +
    		"and ms.memberUuid = m.uuid " +
    		"and m.subjectSourceIdDb <> 'g:gsa' " +
    		"and not exists " +
    		"    (select 1 from MembershipEntry ms2 " +
    		"     where ms2.ownerGroupId = c.rightFactorUuid " +
    		"     and ms2.memberUuid = m.uuid " +
    		"     and ms2.fieldId = :fieldId " +
    		"     and ms2.enabledDb = 'T') " +
        "and not exists " +
        "    (select 1 from ImmediateMembershipEntry ms3 " +
        "     where ms3.ownerGroupId = c.factorOwnerUuid " +
        "     and ms3.memberUuid = m.uuid " +
        "     and ms3.fieldId = :fieldId " +
        "     and ms3.type = 'composite' " +
        "     and ms3.enabledDb = 'T') ";
    
    Set<Object[]> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(Object[].class);
    
    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findMissingUnionMemberships()
   */
  public Set<Object[]> findMissingUnionMemberships() {
    String sql = "select distinct c.factorOwnerUuid, c.uuid, m.uuid from MembershipEntry ms, Member m, Composite c " +
        "where c.typeDb = 'union' " +
        "and (c.leftFactorUuid = ms.ownerGroupId or c.rightFactorUuid = ms.ownerGroupId) " +
        "and ms.fieldId = :fieldId " +
        "and ms.enabledDb = 'T' " +
        "and ms.memberUuid = m.uuid " +
        "and m.subjectSourceIdDb <> 'g:gsa' " +
        "and not exists " +
        "    (select 1 from ImmediateMembershipEntry ms2 " +
        "     where ms2.ownerGroupId = c.factorOwnerUuid " +
        "     and ms2.memberUuid = m.uuid " +
        "     and ms2.fieldId = :fieldId " +
        "     and ms2.type = 'composite' " +
        "     and ms2.enabledDb = 'T') ";
    
    Set<Object[]> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(Object[].class);
    
    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findMissingIntersectionMemberships()
   */
  public Set<Object[]> findMissingIntersectionMemberships() {
    String sql = "select distinct c.factorOwnerUuid, c.uuid, m.uuid from MembershipEntry ms, Member m, Composite c " +
        "where c.typeDb = 'intersection' " +
        "and c.leftFactorUuid = ms.ownerGroupId " +
        "and ms.fieldId = :fieldId " +
        "and ms.enabledDb = 'T' " +
        "and ms.memberUuid = m.uuid " +
        "and m.subjectSourceIdDb <> 'g:gsa' " +
        "and exists " +
        "    (select 1 from MembershipEntry ms2 " +
        "     where ms2.ownerGroupId = c.rightFactorUuid " +
        "     and ms2.memberUuid = m.uuid " +
        "     and ms2.fieldId = :fieldId " +
        "     and ms2.enabledDb = 'T') " +
        "and not exists " +
        "    (select 1 from ImmediateMembershipEntry ms3 " +
        "     where ms3.ownerGroupId = c.factorOwnerUuid " +
        "     and ms3.memberUuid = m.uuid " +
        "     and ms3.fieldId = :fieldId " +
        "     and ms3.type = 'composite' " +
        "     and ms3.enabledDb = 'T') ";
    
    Set<Object[]> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(Object[].class);
    
    return results;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findBadComplementMemberships()
   */
  public Set<Membership> findBadComplementMemberships() {
    String sql = "select distinct ms from ImmediateMembershipEntry ms, Member m, Composite c " +
        "where c.typeDb = 'complement' " +
        "and c.factorOwnerUuid = ms.ownerGroupId " +
        "and ms.fieldId = :fieldId " +
        "and ms.enabledDb = 'T' " +
        "and ms.memberUuid = m.uuid " +
        "and ms.type = 'composite' " +
        "and (exists " +
        "    (select 1 from MembershipEntry ms2 " +
        "     where ms2.ownerGroupId = c.rightFactorUuid " +
        "     and ms2.memberUuid = m.uuid " +
        "     and ms2.fieldId = :fieldId " +
        "     and ms2.enabledDb = 'T') " +
        "  or not exists " +
        "    (select 1 from MembershipEntry ms3 " +
        "     where ms3.ownerGroupId = c.leftFactorUuid " +
        "     and ms3.memberUuid = m.uuid " +
        "     and ms3.fieldId = :fieldId " +
        "     and ms3.enabledDb = 'T')) ";
    
    Set<Membership> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(Membership.class);
    
    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findBadUnionMemberships()
   */
  public Set<Membership> findBadUnionMemberships() {
    String sql = "select distinct ms from ImmediateMembershipEntry ms, Member m, Composite c " +
        "where c.typeDb = 'union' " +
        "and c.factorOwnerUuid = ms.ownerGroupId " +
        "and ms.fieldId = :fieldId " +
        "and ms.enabledDb = 'T' " +
        "and ms.memberUuid = m.uuid " +
        "and ms.type = 'composite' " +
        "and not exists " +
        "    (select 1 from MembershipEntry ms2 " +
        "     where ms2.ownerGroupId = c.rightFactorUuid " +
        "     and ms2.memberUuid = m.uuid " +
        "     and ms2.fieldId = :fieldId " +
        "     and ms2.enabledDb = 'T') " +
        "and not exists " +
        "    (select 1 from MembershipEntry ms3 " +
        "     where ms3.ownerGroupId = c.leftFactorUuid " +
        "     and ms3.memberUuid = m.uuid " +
        "     and ms3.fieldId = :fieldId " +
        "     and ms3.enabledDb = 'T') ";
    
    Set<Membership> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(Membership.class);
    
    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findBadUnionMemberships()
   */
  public Set<Membership> findBadIntersectionMemberships() {
    String sql = "select distinct ms from ImmediateMembershipEntry ms, Member m, Composite c " +
        "where c.typeDb = 'intersection' " +
        "and c.factorOwnerUuid = ms.ownerGroupId " +
        "and ms.fieldId = :fieldId " +
        "and ms.enabledDb = 'T' " +
        "and ms.memberUuid = m.uuid " +
        "and ms.type = 'composite' " +
        "and (not exists " +
        "    (select 1 from MembershipEntry ms2 " +
        "     where ms2.ownerGroupId = c.rightFactorUuid " +
        "     and ms2.memberUuid = m.uuid " +
        "     and ms2.fieldId = :fieldId " +
        "     and ms2.enabledDb = 'T') " +
        "  or not exists " +
        "    (select 1 from MembershipEntry ms3 " +
        "     where ms3.ownerGroupId = c.leftFactorUuid " +
        "     and ms3.memberUuid = m.uuid " +
        "     and ms3.fieldId = :fieldId " +
        "     and ms3.enabledDb = 'T')) ";
    
    Set<Membership> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(Membership.class);
    
    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findBadImmediateMembershipsOnCompositeGroup()
   */
  public Set<Membership> findBadMembershipsOnCompositeGroup() {
    String sql = "select distinct ms from ImmediateMembershipEntry ms, Composite c " +
        "where c.factorOwnerUuid = ms.ownerGroupId " +
        "and ms.fieldId = :fieldId " +
        "and ms.enabledDb = 'T' " +
        "and (ms.type = 'immediate' or ms.viaCompositeId is null or c.uuid <> ms.viaCompositeId) ";
    
    Set<Membership> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .setString("fieldId", Group.getDefaultList().getUuid())
      .listSet(Membership.class);
    
    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findBadMembershipsDeletedGroupAsMember()
   */
  public Set<Membership> findBadMembershipsDeletedGroupAsMember() {
    String sql = "select distinct ms from ImmediateMembershipEntry ms, Member m " +
        "where ms.memberUuid = m.uuid " +
        "and m.subjectSourceIdDb = 'g:gsa' " +
        "and not exists (select 1 from Group g where g.uuid=m.subjectIdDb) ";
    
    Set<Membership> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .listSet(Membership.class);
    
    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findBadImmediateMembershipsOnCompositeGroup()
   */
  public Set<Membership> findBadCompositeMembershipsOnNonCompositeGroup() {
    String sql = "select distinct ms from ImmediateMembershipEntry ms " +
        "where ms.type = 'composite' " +
        "and ms.enabledDb = 'T' " +
        "and ms.ownerGroupId not in (select c.factorOwnerUuid from Composite c) ";
    
    Set<Membership> results = HibernateSession.byHqlStatic()
      .createQuery(sql)
      .setCacheable(false)
      .listSet(Membership.class);
    
    return results;
  }
  
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findByImmediateUuid(java.lang.String, boolean)
   */
  public Membership findByImmediateUuid(String uuid, boolean exceptionIfNull) {
    return findByImmediateUuid(uuid, exceptionIfNull, new QueryOptions().secondLevelCache(false));
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findByImmediateUuid(java.lang.String, boolean, QueryOptions)
   */
  public Membership findByImmediateUuid(String uuid, boolean exceptionIfNull, QueryOptions queryOptions) {
    String theHqlQuery = "from ImmediateMembershipEntry as theMembership where theMembership.immediateMembershipId = :uuid";
        
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic()
      .createQuery(theHqlQuery)
      .options(queryOptions)
      .setCacheable(true)
      .setCacheRegion(KLASS)
      .setString("uuid", uuid);

    Membership membership = byHqlStatic.uniqueResult(Membership.class);

    if (membership == null && exceptionIfNull) {
      throw new RuntimeException("Can't find membership by uuid: " + uuid);
    }
    
    return membership;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByStemOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, Boolean)
   */
  @Override
  public Set<Object[]> findAllByStemOwnerOptions(Collection<String> totalStemIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity) {
    return findAllByStemOwnerOptionsHelper(totalStemIds, totalMemberIds, totalMembershipIds, membershipType, 
        GrouperUtil.toSet(field), 
        sources, scope, stem, stemScope, enabled, checkSecurity, null, null, false, false, false, null, null, false, false, false, null, null, null, null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByStemOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, edu.internet2.middleware.grouper.Field, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, Boolean, QueryOptions, String, boolean, Field, MembershipType)
   */
  @Override
  public Set<Object[]> findAllByStemOwnerOptions(Collection<String> totalStemIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Collection<Field> fields,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity, 
      QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForStem, 
      String scopeForStem, boolean splitScopeForStem, boolean hasFieldForStem,
      boolean hasMembershipTypeForStem) {

    return findAllByStemOwnerOptionsHelper(totalStemIds, totalMemberIds, totalMembershipIds, membershipType, fields, 
        sources, scope, stem, stemScope, enabled, checkSecurity, queryOptionsForMember, filterForMember, splitScopeForMember,
        hasFieldForMember, hasMembershipTypeForMember, queryOptionsForStem, scopeForStem, 
        splitScopeForStem, hasFieldForStem, hasMembershipTypeForStem, null, null, null, null);
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByStemOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, java.lang.Boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, java.lang.Boolean, java.lang.Boolean)
   */
  @Override
  public Set<Object[]> findAllByStemOwnerOptions(Collection<String> totalStemIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Collection<Field> fields,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity, 
      QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForStem, 
      String scopeForStem, boolean splitScopeForStem, boolean hasFieldForStem,
      boolean hasMembershipTypeForStem, Boolean hasEnabledDate, Boolean hasDisabledDate,
      CompositeType customCompositeType, final Group customCompositeGroup) {

    return findAllByStemOwnerOptionsHelper(totalStemIds, totalMemberIds, totalMembershipIds, membershipType, fields, 
        sources, scope, stem, stemScope, enabled, checkSecurity, queryOptionsForMember, filterForMember, splitScopeForMember,
        hasFieldForMember, hasMembershipTypeForMember, queryOptionsForStem, scopeForStem, 
        splitScopeForStem, hasFieldForStem, hasMembershipTypeForStem, hasEnabledDate, hasDisabledDate, customCompositeType, customCompositeGroup);
    
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByStemOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, Set, Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, Boolean, QueryOptions, String, boolean, boolean, boolean, QueryOptions, String, boolean, boolean, boolean )
   */
  private Set<Object[]> findAllByStemOwnerOptionsHelper(Collection<String> totalStemIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Collection<Field> fields,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled, Boolean checkSecurity, 
      QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForStem, 
      String scopeForStem, boolean splitScopeForStem, boolean hasFieldForStem,
      boolean hasMembershipTypeForStem, Boolean hasEnabledDate, Boolean hasDisabledDate,
      CompositeType customCompositeType, final Group customCompositeGroup) {

    QueryOptions.initTotalCount(queryOptionsForStem);
    QueryOptions.initTotalCount(queryOptionsForMember);

    if (checkSecurity == null) {
      checkSecurity = Boolean.TRUE;
    }

    if ((stem == null) != (stemScope == null)) {
      throw new RuntimeException("If stem is set, then stem scope must be set.  If stem isnt set, then stem scope must not be set: " + stem + ", " + stemScope);
    }
    
    if ((customCompositeType == null) != (customCompositeGroup == null)) {
      throw new RuntimeException("If customCompositeType is set, then customCompositeGroup must be set.  If customCompositeType isnt set, then customCompositeGroup must not be set: " + customCompositeType + ", " + customCompositeGroup);
    }

    final List<String> totalStemIdsList = GrouperUtil.listFromCollection(totalStemIds);
    List<String> totalMemberIdsList = GrouperUtil.listFromCollection(totalMemberIds);
    List<String> totalMembershipIdsList = GrouperUtil.listFromCollection(totalMembershipIds);
    
    Set<Object[]> totalResults = new HashSet<Object[]>();

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    final Subject grouperSessionSubject = grouperSession.getSubject();
    
    if (customCompositeType != null) {
      if (customCompositeType != CompositeType.INTERSECTION && customCompositeType != CompositeType.COMPLEMENT) {
        throw new RuntimeException("Unsupported custom composite type: " + customCompositeType);
      }

      if (checkSecurity) {
        // make sure read on group
        boolean allowed = (Boolean)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

          /**
           *
           */
          @Override
          public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            return PrivilegeHelper.canRead(theGrouperSession, customCompositeGroup, grouperSessionSubject);
          }
        });

        //if there is one stem, and checking security, and the user is not allowed to STEM it, then no results
        if (!allowed) {
          return totalResults;
        }
      }
    }
  
    //just check security on one stem to help performance
    if (checkSecurity && GrouperUtil.length(totalStemIds) == 1) {
      boolean allowed = (Boolean)GrouperSession.callbackGrouperSession(
          GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

        /**
         * 
         */
        @Override
        public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
          Stem theStem = StemFinder.findByUuid(theGrouperSession, totalStemIdsList.get(0), false);
          if (theStem == null) {
            return false;
          }
          return theStem.hasStem(grouperSessionSubject);
        }
      });

      //if there is one stem, and checking security, and the user is not allowed to STEM it, then no results
      if (!allowed) {
        return totalResults;
      }

      //we dont need to check security if there is one stem and allowed on that stem
      checkSecurity = false;

    }

    int batchSize = 100;

    int stemBatches = GrouperUtil.batchNumberOfBatches(totalStemIds, batchSize);
  
    for (int stemIndex = 0; stemIndex < stemBatches; stemIndex++) {
      
      List<String> stemIds = GrouperUtil.batchList(totalStemIdsList, batchSize, stemIndex);
      
      int memberBatches = GrouperUtil.batchNumberOfBatches(totalMemberIds, batchSize);
  
      for (int memberIndex = 0; memberIndex < memberBatches; memberIndex++) {
        
        List<String> memberIds = GrouperUtil.batchList(totalMemberIdsList, batchSize, memberIndex);
        int membershipBatches = GrouperUtil.batchNumberOfBatches(totalMembershipIds, batchSize);
        
        for (int membershipIndex = 0; membershipIndex < membershipBatches; membershipIndex++) {
          
          List<String> membershipIds = GrouperUtil.batchList(totalMembershipIdsList, batchSize, membershipIndex);
          
          int stemIdsSize = GrouperUtil.length(stemIds);
          int memberIdsSize = GrouperUtil.length(memberIds);
          int membershipIdsSize = GrouperUtil.length(membershipIds);
          
          if (stemIdsSize == 0 && memberIdsSize == 0 && membershipIdsSize == 0 && stem == null) {
            throw new RuntimeException("Must pass in stem(s), member(s), stem, and/or membership(s)");
          }
  
          ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
          String selectPrefix = "select ms, s, m ";
          String countPrefix = "select count(*) ";
          String memberPrefix = "select distinct m ";
          
          StringBuilder sql = new StringBuilder(" from Member m, MembershipEntry ms, Stem s ");
          
          //we need to make sure it is a list type field
          boolean hasFieldJoin = false;
          
          //we need to make sure it is a list type field if the field ID is not sent in
          if (GrouperUtil.length(fields) == 0) {
            sql.append(", Field f ");
            hasFieldJoin = true;
          }
          
          //maybe we are checking security, maybe not
          boolean changedQuery = false;
          
          if (checkSecurity) { 
            changedQuery = grouperSession.getNamingResolver().hqlFilterStemsWhereClause(
              grouperSessionSubject, byHqlStatic, 
              sql, "ms.ownerStemId", NamingPrivilege.ADMIN_PRIVILEGES);
          }
          
          if (changedQuery && sql.toString().contains(" where ")) {
            sql.append(" and ");
          } else {
            sql.append(" where ");
          }
          
          sql.append(" ms.ownerStemId = s.uuid "
              + " and ms.memberUuid = m.uuid ");
          
          if (customCompositeGroup != null) {
            if (customCompositeType == CompositeType.INTERSECTION) {
              sql.append(" and exists ");
            } else {
              sql.append(" and not exists ");
            }

            sql.append("(select 1 from MembershipEntry mscc " +
                "where mscc.ownerGroupId = '" + customCompositeGroup.getId() + "' " +
                "and mscc.memberUuid = m.uuid " +
                "and mscc.fieldId = '" + Group.getDefaultList().getId() + "' " +
                "and mscc.enabledDb = 'T') ");
          }

          if (enabled != null && enabled) {
            sql.append(" and ms.enabledDb = 'T' ");
          }
          if (enabled != null && !enabled) {
            sql.append(" and ms.enabledDb = 'F' ");
          }
          if (hasEnabledDate != null && hasEnabledDate) {
            sql.append(" and ms.enabledTimeDb is not null ");
          }
          if (hasEnabledDate != null && !hasEnabledDate) {
            sql.append(" and ms.enabledTimeDb is null ");
          }
          if (hasDisabledDate != null && hasDisabledDate) {
            sql.append(" and ms.disabledTimeDb is not null ");
          }
          if (hasDisabledDate != null && !hasDisabledDate) {
            sql.append(" and ms.disabledTimeDb is null ");
          }
          if (sources != null && sources.size() > 0) {
            sql.append(" and m.subjectSourceIdDb in ").append(HibUtils.convertSourcesToSqlInString(sources));
          }
          boolean hasScope = StringUtils.isNotBlank(scope);
          if (hasScope) {
            sql.append(" and s.nameDb like :scope ");
            byHqlStatic.setString("scope", scope + "%");
          }
          if (stem != null) {
            switch (stemScope) {
              case ONE:
                
                sql.append(" and s.parentUuid = :stemId ");
                byHqlStatic.setString("stemId", stem.getUuid());
                break;
              case SUB:
                
                sql.append(" and s.nameDb like :stemSub ");
                byHqlStatic.setString("stemSub", stem.getName() + ":%");
                
                break;
              default:
                throw new RuntimeException("Not expecting scope: " + stemScope);
            }
          }
          //immediate or effective, etc
          if (membershipType != null) {
            sql.append(" and ms.type ").append(membershipType.queryClause()).append(" ");
          }
          if (!hasFieldJoin) {
            //needs to be a members field
            //if (!StringUtils.equals("list",field.getTypeString())) {
            //  throw new RuntimeException("This method only works with members fields: " + field);
            //}
            sql.append(" and ms.fieldId in ( ");
            Set<String> fieldStrings = new HashSet<String>();
            for (Field field : fields) {
              fieldStrings.add(field.getUuid());
            }
            sql.append(HibUtils.convertToInClause(fieldStrings, byHqlStatic));
            sql.append(" ) ");
          } else {
            //add on the column
            sql.append(" and ms.fieldId = f.uuid and f.typeString = 'naming' ");
          }
          if (stemIdsSize > 0) {
            sql.append(" and ms.ownerStemId in (");
            sql.append(HibUtils.convertToInClause(stemIds, byHqlStatic));
            sql.append(") ");
          }
          if (memberIdsSize > 0) {
            sql.append(" and ms.memberUuid in (");
            sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
            sql.append(") ");
          }
          if (membershipIdsSize > 0) {
            sql.append(" and ms.uuid in (");
            sql.append(HibUtils.convertToInClause(membershipIds, byHqlStatic));
            sql.append(") ");
          }
          
          if (!StringUtils.isBlank(filterForMember)) {

            filterForMember = filterForMember.toLowerCase();

            String[] filtersForMember = splitScopeForMember ? GrouperUtil.splitTrim(filterForMember, " ") 
                : new String[]{filterForMember};

            if (sql.length() > 0) {
              sql.append(" and ");
            }
            sql.append(" ( ");

            int index = 0;
            
            String searchFieldName = SearchStringEnum.getDefaultSearchString().getFieldName();

            for (String theFilter : filtersForMember) {
              if (index != 0) {
                sql.append(" and ");
              }
              sql.append(" ( m." + searchFieldName + " like :filterString" + index + " ) ");

              if (!theFilter.endsWith("%")) {
                theFilter += "%";
              }
              if (!theFilter.startsWith("%")) {
                theFilter = "%" + theFilter;
              }
              byHqlStatic.setString("filterString" + index, theFilter);
              index++;
            }
            sql.append(" ) ");
          }

          if (!StringUtils.isBlank(scopeForStem)) {

            scopeForStem = scopeForStem.toLowerCase();

            String[] scopesForStem = splitScopeForStem ? GrouperUtil.splitTrim(scopeForStem, " ") 
                : new String[]{scopeForStem};

            if (sql.length() > 0) {
              sql.append(" and ");
            }
            sql.append(" ( ");

            int index = 0;
            
            for (String theScopeForStem : scopesForStem) {
              if (index != 0) {
                sql.append(" and ");
              }
              
              sql.append(" ( lower(s.nameDb) like :scopeForStem" + index 
                  + " or lower(s.alternateNameDb) like :scopeForStem" + index 
                  + " or lower(s.displayNameDb) like :scopeForStem" + index 
                  + " or lower(s.descriptionDb) like :scopeForStem" + index + " ) ");
              
              if (!theScopeForStem.endsWith("%")) {
                theScopeForStem += "%";
              }
              if (!theScopeForStem.startsWith("%")) {
                theScopeForStem = "%" + theScopeForStem;
              }
              byHqlStatic.setString("scopeForStem" + index, theScopeForStem);
              index++;
            }
            sql.append(" ) ");
          }

          
          byHqlStatic
            .setCacheable(false)
            .setCacheRegion(KLASS);
  
          int maxMemberships = GrouperConfig.retrieveConfig().propertyValueInt("ws.getMemberships.maxResultSize", 30000);
          int maxPageSize = GrouperConfig.retrieveConfig().propertyValueInt("ws.getMemberships.maxPageSize", 500);

          boolean pageMembers = queryOptionsForMember != null;
          
          if (pageMembers) {

            if (queryOptionsForMember.getQueryPaging() == null) {
              throw new RuntimeException("If paging by member, then paging must be set in the query options");
            }

            //cant page too much...
            if (queryOptionsForMember.getQueryPaging().getPageSize() > maxPageSize) {
              throw new RuntimeException("Cant get a page size greater then " + maxPageSize + "! " 
                  + queryOptionsForMember.getQueryPaging().getPageSize());
            }

            if (stemBatches > 1) {
              throw new RuntimeException("Cant have more than 1 stemBatch if paging members");
            }
            
            if (memberBatches > 1) {
              throw new RuntimeException("Cant have more than 1 memberBatch if paging members");
            }
            
            if (membershipBatches > 1) {
              throw new RuntimeException("Cant have more than 1 membershipBatch if paging members");
            }
            
          }

          if (!StringUtils.isBlank(filterForMember) && !pageMembers) {
            throw new RuntimeException("If you are filtering by member, then you must page members");
          }

          //sort for stems
          boolean pageStems = queryOptionsForStem != null;

          //if -1, lets not check
          if (maxMemberships >= 0 && !pageMembers && !pageStems) {
  
            long size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
            
            //see if too many
            if (size > maxMemberships) {
              throw new RuntimeException("Too many results: " + size);
            }
            
          }

          //if paging by members, get the members, then do the same query using those members...
          if (pageMembers) {
            

            if (queryOptionsForMember != null && queryOptionsForMember.getQuerySort() != null) {
              Hib3MemberDAO.massageMemberSortFields(queryOptionsForMember.getQuerySort());
            }

            //sort by default search string if not specified
            if (queryOptionsForMember.getQuerySort() == null) {
              queryOptionsForMember.sortAsc("m." + SortStringEnum.getDefaultSortString().getFieldName());
            }

            byHqlStatic.options(queryOptionsForMember);
   
            Set<Member> members = byHqlStatic.createQuery(memberPrefix + sql.toString()).listSet(Member.class);

            //no need to do another query if no members
            if (GrouperUtil.length(members) == 0) {
              return totalResults;
            }
            
            Set<String> theMemberIds = new LinkedHashSet<String>();
            
            for (Member member : members) {
              theMemberIds.add(member.getUuid());
            }
            
            //dont pass for people with membership type or field... we already filtered by that...
            Set<Object[]> tempResults = findAllByStemOwnerOptionsHelper(totalStemIds, theMemberIds,
                totalMembershipIds, hasMembershipTypeForMember ? null : membershipType, 
                    hasFieldForMember ? null : fields,  
                sources, scope, stem, stemScope, enabled, checkSecurity, null, null, false, false, false,
                null, null, false, false, false, hasEnabledDate, hasDisabledDate, customCompositeType, customCompositeGroup);
            
            //lets sort these by member
            Set<Object[]> sortedResults = new LinkedHashSet<Object[]>();
            
            for (Member member : members) {
              Iterator<Object[]> iterator = tempResults.iterator();
              while(iterator.hasNext()) {
                
                Object[] tempResult = iterator.next();
                //if the member is the same, put it in the sortedResults, and remove
                if (StringUtils.equals(((Member)tempResult[2]).getUuid(), member.getUuid())) {
                  
                  sortedResults.add(tempResult);
                  iterator.remove();
                  
                }
              }
            }
            return sortedResults;
            
          }

          {
            //sort for stems
            if (pageStems) {

              if (queryOptionsForStem.getQueryPaging() == null) {
                throw new RuntimeException("If paging by stem, then paging must be set in the query options");
              }

              //cant page too much...
              if (queryOptionsForStem.getQueryPaging().getPageSize() > maxPageSize) {
                throw new RuntimeException("Cant get a page size greater then " + maxPageSize + "! " 
                    + queryOptionsForStem.getQueryPaging().getPageSize());
              }

              if (stemBatches > 1) {
                throw new RuntimeException("Cant have more than 1 stemBatch if paging stems");
              }
              
              if (memberBatches > 1) {
                throw new RuntimeException("Cant have more than 1 memberBatch if paging stems");
              }
              
              if (membershipBatches > 1) {
                throw new RuntimeException("Cant have more than 1 membershipBatch if paging stems");
              }
              
            }

            if (!StringUtils.isBlank(scopeForStem) && !pageStems) {
              throw new RuntimeException("If you are filtering by stem, then you must page stems");
            }
            
            //note, put in the size query conditional above

            //if paging by members, get the members, then do the same query using those members...
            if (pageStems) {
              
              if (queryOptionsForStem.getQuerySort()!= null) {
                Hib3StemDAO.massageSortFields(queryOptionsForStem.getQuerySort(), "s");
              }

              //sort by default search string if not specified
              if (queryOptionsForStem.getQuerySort() == null) {
                queryOptionsForStem.sortAsc("s.displayNameDb");
              }

              byHqlStatic.options(queryOptionsForStem);
              
              String stemPrefix = "select distinct s ";

              Set<Stem> stems = byHqlStatic.createQuery(stemPrefix + sql.toString()).listSet(Stem.class);

              //no need to do another query if no stems
              if (GrouperUtil.length(stems) == 0) {
                return totalResults;
              }
              
              Set<String> theStemIds = new LinkedHashSet<String>();
              
              //MCH 20140811 this was in the group and attrDef one, adding it here
              for (Stem theStem : stems) {
                theStemIds.add(theStem.getUuid());
              }
                            
              //dont pass for people with membership type or field... we already filtered by that...
              Set<Object[]> tempResults = findAllByStemOwnerOptionsHelper(theStemIds, totalMemberIds,
                  totalMembershipIds, hasMembershipTypeForStem ? null : membershipType, hasFieldForStem ? null : fields,  
                  sources, scope, stem, stemScope, enabled, checkSecurity, null, null, false, false, false, 
                  null, null, false, false, false, hasEnabledDate, hasDisabledDate, customCompositeType, customCompositeGroup);
              
              //lets sort these by member
              Set<Object[]> sortedResults = new LinkedHashSet<Object[]>();
              
              for (Stem theStem : stems) {
                Iterator<Object[]> iterator = tempResults.iterator();
                while(iterator.hasNext()) {
                  
                  Object[] tempResult = iterator.next();
                  //if the member is the same, put it in the sortedResults, and remove
                  if (StringUtils.equals(((Stem)tempResult[1]).getUuid(), theStem.getUuid())) {
                    
                    sortedResults.add(tempResult);
                    iterator.remove();
                    
                  }
                }
              }

              return sortedResults;
              
            }

            
          }
          
          Set<Object[]> results = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(Object[].class);
  
          totalResults.addAll(results);
          
        }
      }
    }
    
    
    //nothing to filter
    if (GrouperUtil.length(totalResults) == 0) {
      return totalResults;
    }
    
    //if the hql didnt filter, we need to do that here
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    for (Object[] objects : totalResults) {
      memberships.add((Membership)objects[0]);
    }
    int origMembershipsSize = memberships.size();
    Set<Membership> filteredMemberships = grouperSession.getAccessResolver().postHqlFilterMemberships(grouperSessionSubject, memberships);
    if (origMembershipsSize != filteredMemberships.size()) {
      
      //we have work to do
      Iterator<Object[]> iterator = totalResults.iterator();
      while (iterator.hasNext()) {
        Object[] row = iterator.next();
        Membership currentMembership = (Membership)row[0];
        //if not in the allowed list
        if (!filteredMemberships.contains(currentMembership)) {
          //remove the object row
          iterator.remove();
        }
      }
    }

    assignMembersOwnersToMemberships(totalResults);

    //we should be down to the cesure list
    return totalResults;
    
  }

  /**
   * @see MembershipDAO#findAllByAttributeDefOwnerOptions(Collection, Collection, Collection, MembershipType, Field, Set, String, Stem, Scope, Boolean, Boolean)
   */
  @Override
  public Set<Object[]> findAllByAttributeDefOwnerOptions(
      Collection<String> totalAttributeDefIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType, Field field,
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled,
      Boolean checkSecurity) {
    return findAllByAttributeDefOwnerOptionsHelper(totalAttributeDefIds, totalMemberIds,
        totalMembershipIds, membershipType, GrouperUtil.toSet(field), sources, scope, 
        stem, stemScope, enabled, checkSecurity, null, null, false, false, false, null, null, false, false, false, null, null, null, null);
  }

  /**
   * @see MembershipDAO#findAllByAttributeDefOwnerOptions(Collection, Collection, Collection, MembershipType, Field, Set, String, Stem, Scope, Boolean, Boolean)
   */
  private Set<Object[]> findAllByAttributeDefOwnerOptionsHelper(
      Collection<String> totalAttributeDefIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType, 
      Collection<Field> fields,
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled,
      Boolean checkSecurity, QueryOptions queryOptionsForMember, String filterForMember, 
      boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForAttributeDef,
      String scopeForAttributeDef, boolean splitScopeForAttributeDef,
      boolean hasFieldForAttributeDef, boolean hasMembershipTypeForAttributeDef, Boolean hasEnabledDate, Boolean hasDisabledDate,
      CompositeType customCompositeType, final Group customCompositeGroup) {

    QueryOptions.initTotalCount(queryOptionsForAttributeDef);
    QueryOptions.initTotalCount(queryOptionsForMember);

    if (checkSecurity == null) {
      checkSecurity = Boolean.TRUE;
    }
    
    if ((stem == null) != (stemScope == null)) {
      throw new RuntimeException("If stem is set, then stem scope must be set.  If stem isnt set, then stem scope must not be set: " + stem + ", " + stemScope);
    }
    
    if ((customCompositeType == null) != (customCompositeGroup == null)) {
      throw new RuntimeException("If customCompositeType is set, then customCompositeGroup must be set.  If customCompositeType isnt set, then customCompositeGroup must not be set: " + customCompositeType + ", " + customCompositeGroup);
    }

    List<String> totalAttributeDefIdsList = GrouperUtil.listFromCollection(totalAttributeDefIds);
    List<String> totalMemberIdsList = GrouperUtil.listFromCollection(totalMemberIds);
    List<String> totalMembershipIdsList = GrouperUtil.listFromCollection(totalMembershipIds);
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    final Subject grouperSessionSubject = grouperSession.getSubject();

    Set<Object[]> totalResults = new HashSet<Object[]>();
    
    if (customCompositeType != null) {
      if (customCompositeType != CompositeType.INTERSECTION && customCompositeType != CompositeType.COMPLEMENT) {
        throw new RuntimeException("Unsupported custom composite type: " + customCompositeType);
      }

      if (checkSecurity) {
        // make sure read on group
        boolean allowed = (Boolean)GrouperSession.callbackGrouperSession(
            GrouperSession.staticGrouperSession().internal_getRootSession(), new GrouperSessionHandler() {

          /**
           *
           */
          @Override
          public Object callback(GrouperSession theGrouperSession) throws GrouperSessionException {
            return PrivilegeHelper.canRead(theGrouperSession, customCompositeGroup, grouperSessionSubject);
          }
        });

        //if there is one stem, and checking security, and the user is not allowed to STEM it, then no results
        if (!allowed) {
          return totalResults;
        }
      }
    }
        
    int attrDefBatches = GrouperUtil.batchNumberOfBatches(totalAttributeDefIds, 100);
    
    for (int attrDefIndex = 0; attrDefIndex < attrDefBatches; attrDefIndex++) {
      
      List<String> attributeDefIds = GrouperUtil.batchList(totalAttributeDefIdsList, 100, attrDefIndex);
      
      int memberBatches = GrouperUtil.batchNumberOfBatches(totalMemberIds, 100);

      for (int memberIndex = 0; memberIndex < memberBatches; memberIndex++) {
        
        List<String> memberIds = GrouperUtil.batchList(totalMemberIdsList, 100, memberIndex);
        int membershipBatches = GrouperUtil.batchNumberOfBatches(totalMembershipIds, 100);
        
        for (int membershipIndex = 0; membershipIndex < membershipBatches; membershipIndex++) {
          
          List<String> membershipIds = GrouperUtil.batchList(totalMembershipIdsList, 100, membershipIndex);
          
          int attributeDefIdsSize = GrouperUtil.length(attributeDefIds);
          int memberIdsSize = GrouperUtil.length(memberIds);
          int membershipIdsSize = GrouperUtil.length(membershipIds);
          
          if (attributeDefIdsSize == 0 && memberIdsSize == 0 && membershipIdsSize == 0 && stem == null) {
            throw new RuntimeException("Must pass in attributeDef(s), member(s), stem, and/or membership(s)");
          }

          ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

          String selectPrefix = "select ms, a, m ";
          String countPrefix = "select count(*) ";
          String memberPrefix = "select distinct m ";
          
          StringBuilder sqlTables = new StringBuilder(" from AttributeDef a, MembershipEntry ms, Member m ");
          
          //we need to make sure it is a list type field
          boolean hasFieldTable = false;
          
          //we need to make sure it is a list type field if the field ID is not sent in
          if (GrouperUtil.length(fields) == 0) {
            sqlTables.append(", Field f ");
            hasFieldTable = true;
          }
          
          //maybe we are checking security, maybe not
          StringBuilder sqlWhereClause = new StringBuilder(" ms.ownerAttrDefId = a.id "
              + " and ms.memberUuid = m.uuid ");
          
          if (checkSecurity) { 
            
            grouperSession.getAttributeDefResolver().hqlFilterAttrDefsWhereClause(
                grouperSessionSubject, byHqlStatic, 
                sqlTables, sqlWhereClause, "ms.ownerAttrDefId", AttributeDefPrivilege.ATTR_ADMIN_PRIVILEGES);
            
          }
          
          StringBuilder sql;
          sql = sqlTables.append(" where ").append(sqlWhereClause);
          
          if (customCompositeGroup != null) {
            if (customCompositeType == CompositeType.INTERSECTION) {
              sql.append(" and exists ");
            } else {
              sql.append(" and not exists ");
            }
            
            sql.append("(select 1 from MembershipEntry mscc " +
                "where mscc.ownerGroupId = '" + customCompositeGroup.getId() + "' " +
                "and mscc.memberUuid = m.uuid " +
                "and mscc.fieldId = '" + Group.getDefaultList().getId() + "' " +
                "and mscc.enabledDb = 'T') ");
          }
          
          if (enabled != null && enabled) {
            sql.append(" and ms.enabledDb = 'T' ");
          }
          if (enabled != null && !enabled) {
            sql.append(" and ms.enabledDb = 'F' ");
          }
          if (hasEnabledDate != null && hasEnabledDate) {
            sql.append(" and ms.enabledTimeDb is not null ");
          }
          if (hasEnabledDate != null && !hasEnabledDate) {
            sql.append(" and ms.enabledTimeDb is null ");
          }
          if (hasDisabledDate != null && hasDisabledDate) {
            sql.append(" and ms.disabledTimeDb is not null ");
          }
          if (hasDisabledDate != null && !hasDisabledDate) {
            sql.append(" and ms.disabledTimeDb is null ");
          }
          if (sources != null && sources.size() > 0) {
            sql.append(" and m.subjectSourceIdDb in ").append(HibUtils.convertSourcesToSqlInString(sources));
          }
          boolean hasScope = StringUtils.isNotBlank(scope);
          if (hasScope) {
            sql.append(" and a.nameDb like :scope ");
            byHqlStatic.setString("scope", scope + "%");
          }
          if (stem != null) {
            switch (stemScope) {
              case ONE:
                
                sql.append(" and a.stemId = :stemId ");
                byHqlStatic.setString("stemId", stem.getUuid());
                break;
              case SUB:
                
                sql.append(" and a.nameDb like :stemSub ");
                byHqlStatic.setString("stemSub", stem.getName() + ":%");
                
                break;
              default:
                throw new RuntimeException("Not expecting scope: " + stemScope);
            }
          }
          //immediate or effective, etc
          if (membershipType != null) {
            sql.append(" and ms.type ").append(membershipType.queryClause()).append(" ");
          }
          if (!hasFieldTable) {
            //needs to be a members field
            //if (!StringUtils.equals("list",field.getTypeString())) {
            //  throw new RuntimeException("This method only works with members fields: " + field);
            //}
            sql.append(" and ms.fieldId in ( ");
            Set<String> fieldStrings = new HashSet<String>();
            for (Field field : fields) {
              fieldStrings.add(field.getUuid());
            }
            sql.append(HibUtils.convertToInClause(fieldStrings, byHqlStatic));
            sql.append(" ) ");
            
          } else {
            //add on the column
            sql.append(" and ms.fieldId = f.uuid and f.typeString = 'attributeDef' ");
          }
          if (attributeDefIdsSize > 0) {
            sql.append(" and ms.ownerAttrDefId in (");
            sql.append(HibUtils.convertToInClause(attributeDefIds, byHqlStatic));
            sql.append(") ");
          }
          if (memberIdsSize > 0) {
            sql.append(" and ms.memberUuid in (");
            sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
            sql.append(") ");
          }
          if (membershipIdsSize > 0) {
            sql.append(" and ms.uuid in (");
            sql.append(HibUtils.convertToInClause(membershipIds, byHqlStatic));
            sql.append(") ");
          }
          
          if (!StringUtils.isBlank(filterForMember)) {

            filterForMember = filterForMember.toLowerCase();

            String[] filtersForMember = splitScopeForMember ? GrouperUtil.splitTrim(filterForMember, " ") 
                : new String[]{filterForMember};

            if (sql.length() > 0) {
              sql.append(" and ");
            }
            sql.append(" ( ");

            int index = 0;
            
            String searchFieldName = SearchStringEnum.getDefaultSearchString().getFieldName();

            for (String theFilter : filtersForMember) {
              if (index != 0) {
                sql.append(" and ");
              }
              sql.append(" ( m." + searchFieldName + " like :filterString" + index + " ) ");

              if (!theFilter.endsWith("%")) {
                theFilter += "%";
              }
              if (!theFilter.startsWith("%")) {
                theFilter = "%" + theFilter;
              }
              byHqlStatic.setString("filterString" + index, theFilter);
              index++;
            }
            sql.append(" ) ");
          }

          if (!StringUtils.isBlank(scopeForAttributeDef)) {

            scopeForAttributeDef = scopeForAttributeDef.toLowerCase();

            String[] scopesForAttributeDef = splitScopeForAttributeDef ? GrouperUtil.splitTrim(scopeForAttributeDef, " ") 
                : new String[]{scopeForAttributeDef};

            if (sql.length() > 0) {
              sql.append(" and ");
            }
            sql.append(" ( ");

            int index = 0;
            
            for (String theScopeForAttributeDef : scopesForAttributeDef) {
              if (index != 0) {
                sql.append(" and ");
              }
              
              sql.append(" ( lower(a.nameDb) like :scopeForAttributeDef" + index 
                  + " or lower(a.description) like :scopeForAttributeDef" + index + " ) ");
              
              if (!theScopeForAttributeDef.endsWith("%")) {
                theScopeForAttributeDef += "%";
              }
              if (!theScopeForAttributeDef.startsWith("%")) {
                theScopeForAttributeDef = "%" + theScopeForAttributeDef;
              }
              byHqlStatic.setString("scopeForAttributeDef" + index, theScopeForAttributeDef);
              index++;
            }
            sql.append(" ) ");
          }

          
          byHqlStatic
            .setCacheable(false)
            .setCacheRegion(KLASS);

          int maxMemberships = GrouperConfig.retrieveConfig().propertyValueInt("ws.getMemberships.maxResultSize", 30000);
          int maxPageSize = GrouperConfig.retrieveConfig().propertyValueInt("ws.getMemberships.maxPageSize", 500);

          boolean pageMembers = queryOptionsForMember != null;
          
          if (pageMembers) {

            if (queryOptionsForMember.getQueryPaging() == null) {
              throw new RuntimeException("If paging by member, then paging must be set in the query options");
            }

            //cant page too much...
            if (queryOptionsForMember.getQueryPaging().getPageSize() > maxPageSize) {
              throw new RuntimeException("Cant get a page size greater then " + maxPageSize + "! " 
                  + queryOptionsForMember.getQueryPaging().getPageSize());
            }

            if (attrDefBatches > 1) {
              throw new RuntimeException("Cant have more than 1 attributeDefBatch if paging members");
            }
            
            if (memberBatches > 1) {
              throw new RuntimeException("Cant have more than 1 memberBatch if paging members");
            }
            
            if (membershipBatches > 1) {
              throw new RuntimeException("Cant have more than 1 membershipBatch if paging members");
            }
            
          }

          if (!StringUtils.isBlank(filterForMember) && !pageMembers) {
            throw new RuntimeException("If you are filtering by member, then you must page members");
          }

          //sort for groups
          boolean pageAttributeDefs = queryOptionsForAttributeDef != null;
            
          //if -1, lets not check
          if (maxMemberships >= 0 && !pageMembers && !pageAttributeDefs) {
  
            long size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
            
            //see if too many
            if (size > maxMemberships) {
              throw new RuntimeException("Too many results: " + size);
            }
            
          }

          //if paging by members, get the members, then do the same query using those members...
          if (pageMembers) {
            
            if (queryOptionsForMember != null && queryOptionsForMember.getQuerySort() != null) {
              Hib3MemberDAO.massageMemberSortFields(queryOptionsForMember.getQuerySort());
            }
            
            //sort by default search string if not specified
            if (queryOptionsForMember.getQuerySort() == null) {
              queryOptionsForMember.sortAsc("m." + SortStringEnum.getDefaultSortString().getFieldName());
            }

            byHqlStatic.options(queryOptionsForMember);
   
            Set<Member> members = byHqlStatic.createQuery(memberPrefix + sql.toString()).listSet(Member.class);

            //no need to do another query if no members
            if (GrouperUtil.length(members) == 0) {
              return totalResults;
            }
            
            Set<String> theMemberIds = new LinkedHashSet<String>();
            
            for (Member member : members) {
              theMemberIds.add(member.getUuid());
            }
            
            //dont pass for people with membership type or field... we already filtered by that...
            Set<Object[]> tempResults = findAllByAttributeDefOwnerOptionsHelper(totalAttributeDefIds, theMemberIds,
                totalMembershipIds, hasMembershipTypeForMember ? null : membershipType, 
                    hasFieldForMember ? null : fields,  
                sources, scope, stem, stemScope, enabled, checkSecurity, null, null, false, false, false,
                null, null, false, false, false, hasEnabledDate, hasDisabledDate, customCompositeType, customCompositeGroup);
            
            //lets sort these by member
            Set<Object[]> sortedResults = new LinkedHashSet<Object[]>();
            
            for (Member member : members) {
              Iterator<Object[]> iterator = tempResults.iterator();
              while(iterator.hasNext()) {
                
                Object[] tempResult = iterator.next();
                //if the member is the same, put it in the sortedResults, and remove
                if (StringUtils.equals(((Member)tempResult[2]).getUuid(), member.getUuid())) {
                  
                  sortedResults.add(tempResult);
                  iterator.remove();
                  
                }
              }
            }
            return sortedResults;
            
          }
          {
            if (pageAttributeDefs) {

              if (queryOptionsForAttributeDef.getQueryPaging() == null) {
                throw new RuntimeException("If paging by attributeDef, then paging must be set in the query options");
              }

              //cant page too much...
              if (queryOptionsForAttributeDef.getQueryPaging().getPageSize() > maxPageSize) {
                throw new RuntimeException("Cant get a page size greater then " + maxPageSize + "! " 
                    + queryOptionsForAttributeDef.getQueryPaging().getPageSize());
              }

              if (attrDefBatches > 1) {
                throw new RuntimeException("Cant have more than 1 groupBatch if paging attributeDefs");
              }
              
              if (memberBatches > 1) {
                throw new RuntimeException("Cant have more than 1 memberBatch if paging attributeDefs");
              }
              
              if (membershipBatches > 1) {
                throw new RuntimeException("Cant have more than 1 membershipBatch if paging attributeDefs");
              }
              
            }

            if (!StringUtils.isBlank(scopeForAttributeDef) && !pageAttributeDefs) {
              throw new RuntimeException("If you are filtering by attributeDef, then you must page attributeDefs");
            }
            
            //note, put in the size query conditional above

            //if paging by attributeDefs, get the attributeDefs, then do the same query using those members...
            if (pageAttributeDefs) {
              
              if (queryOptionsForAttributeDef.getQuerySort()!= null) {
                Hib3AttributeDefDAO.massageSortFields(queryOptionsForAttributeDef.getQuerySort(), "a");
              }

              //sort by default search string if not specified
              if (queryOptionsForAttributeDef.getQuerySort() == null) {
                queryOptionsForAttributeDef.sortAsc("a.nameDb");
              }

              byHqlStatic.options(queryOptionsForAttributeDef);
              
              String attributeDefPrefix = "select distinct a ";

              Set<AttributeDef> attributeDefs = byHqlStatic.createQuery(attributeDefPrefix + sql.toString()).listSet(AttributeDef.class);

              //no need to do another query if no attributeDefs
              if (GrouperUtil.length(attributeDefs) == 0) {
                return totalResults;
              }
              
              Set<String> theAttributeDefIds = new LinkedHashSet<String>();
              
              for (AttributeDef attributeDef : attributeDefs) {
                theAttributeDefIds.add(attributeDef.getUuid());
              }
              
              //dont pass for people with membership type or field... we already filtered by that...
              Set<Object[]> tempResults = findAllByAttributeDefOwnerOptionsHelper(theAttributeDefIds, totalMemberIds,
                  totalMembershipIds, hasMembershipTypeForAttributeDef ? null : membershipType, hasFieldForAttributeDef ? null : fields,  
                  sources, scope, stem, stemScope, enabled, checkSecurity,
                  null, null, false, false, false, null, null, false, false, false, hasEnabledDate, hasDisabledDate, customCompositeType, customCompositeGroup);
              
              //lets sort these by member
              Set<Object[]> sortedResults = new LinkedHashSet<Object[]>();
              
              for (AttributeDef attributeDef : attributeDefs) {
                Iterator<Object[]> iterator = tempResults.iterator();
                while(iterator.hasNext()) {
                  
                  Object[] tempResult = iterator.next();
                  //if the member is the same, put it in the sortedResults, and remove
                  if (StringUtils.equals(((AttributeDef)tempResult[1]).getUuid(), attributeDef.getUuid())) {
                    
                    sortedResults.add(tempResult);
                    iterator.remove();
                    
                  }
                }
              }
              return sortedResults;
              
            }
          }

          Set<Object[]> results = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(Object[].class);

          totalResults.addAll(results);
          
        }
      }
    }
    
    
    //nothing to filter
    if (GrouperUtil.length(totalResults) == 0) {
      return totalResults;
    }

    //lets assign the members to the memberships so they dont have to be queried later
    assignMembersOwnersToMemberships(totalResults);
    
    //we should be down to the cesure list
    return totalResults;
  }

  /**
   * assign objects to memberships to reduce further querying
   * @param membershipArrays
   */
  private static void assignMembersOwnersToMemberships(Collection<Object[]> membershipArrays) {
    for (Object[] membershipArray : GrouperUtil.nonNull(membershipArrays)) {
      assignMemberOwnerToMembership(membershipArray);
    }    
  }

  /**
   * assign member and owner to membership
   * @param membershipArray
   */
  private static void assignMemberOwnerToMembership(Object[] membershipArray) {
    Membership membership = (Membership)membershipArray[0];
    if (membershipArray[1] instanceof Member) {
      Member member = (Member)membershipArray[1];
      membership.setMember(member);
    }
    if (membershipArray[1] instanceof AttributeDef) {
      AttributeDef attributeDef = (AttributeDef)membershipArray[1];
      membership.setOwnerAttributeDef(attributeDef);
    }
    if (membershipArray[1] instanceof Stem) {
      Stem stem = (Stem)membershipArray[1];
      membership.setOwnerStem(stem);
    }
    if (membershipArray[1] instanceof Group) {
      Group theGroup = (Group)membershipArray[1];
      membership.setOwnerGroup(theGroup);
    }
    if (membershipArray.length >= 3 && membershipArray[2] instanceof Member) {
      Member member = (Member)membershipArray[2];
      membership.setMember(member);
    }
  }
  
  /**
   * @see MembershipDAO#findAllByAttributeDefOwnerOptions(Collection, Collection, Collection, MembershipType, Collection, Set, String, Stem, Scope, Boolean, Boolean, QueryOptions, String, boolean, boolean, boolean)
   */
  @Override
  public Set<Object[]> findAllByAttributeDefOwnerOptions(
      Collection<String> attributeDefIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType, Collection<Field> fields,
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled,
      Boolean shouldCheckSecurity, QueryOptions queryOptionsForMember, String filterForMember, 
      boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForAttributeDef,
      String scopeForAttributeDef, boolean splitScopeForAttributeDef,
      boolean hasFieldForAttributeDef, boolean hasMembershipTypeForAttributeDef) {
    Set<Object[]> result = findAllByAttributeDefOwnerOptionsHelper(attributeDefIds, memberIds,
        membershipIds, membershipType, fields, sources, scope, stem, stemScope, 
        enabled, shouldCheckSecurity, queryOptionsForMember, filterForMember, splitScopeForMember, hasFieldForMember, hasMembershipTypeForMember, 
        queryOptionsForAttributeDef, scopeForAttributeDef, 
        splitScopeForAttributeDef, hasFieldForAttributeDef, hasMembershipTypeForAttributeDef, null, null, null, null);
    return result;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllByAttributeDefOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, edu.internet2.middleware.grouper.membership.MembershipType, java.util.Collection, java.util.Set, java.lang.String, edu.internet2.middleware.grouper.Stem, edu.internet2.middleware.grouper.Stem.Scope, java.lang.Boolean, java.lang.Boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, boolean, java.lang.Boolean, java.lang.Boolean)
   */
  @Override
  public Set<Object[]> findAllByAttributeDefOwnerOptions(
      Collection<String> attributeDefIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType, Collection<Field> fields,
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled,
      Boolean shouldCheckSecurity, QueryOptions queryOptionsForMember, String filterForMember, 
      boolean splitScopeForMember, 
      boolean hasFieldForMember, boolean hasMembershipTypeForMember, QueryOptions queryOptionsForAttributeDef,
      String scopeForAttributeDef, boolean splitScopeForAttributeDef,
      boolean hasFieldForAttributeDef, boolean hasMembershipTypeForAttributeDef, Boolean hasEnabledDate, Boolean hasDisabledDate,
      CompositeType customCompositeType, final Group customCompositeGroup) {
    Set<Object[]> result = findAllByAttributeDefOwnerOptionsHelper(attributeDefIds, memberIds,
        membershipIds, membershipType, fields, sources, scope, stem, stemScope, 
        enabled, shouldCheckSecurity, queryOptionsForMember, filterForMember, splitScopeForMember, hasFieldForMember, hasMembershipTypeForMember, 
        queryOptionsForAttributeDef, scopeForAttributeDef, 
        splitScopeForAttributeDef, hasFieldForAttributeDef, hasMembershipTypeForAttributeDef, hasEnabledDate, hasDisabledDate, customCompositeType, customCompositeGroup);
    return result;
  }


  /**
   * @see MembershipDAO#findAllByAttributeDefOwnerOptions(Collection, Collection, Collection, MembershipType, Collection, Set, String, Stem, Scope, Boolean, Boolean, QueryOptions, String, boolean, boolean, boolean)
   */
  @Override
  public Set<Object[]> findAllByAttributeDefOwnerOptions(
      Collection<String> attributeDefIds, Collection<String> memberIds,
      Collection<String> membershipIds, MembershipType membershipType, Collection<Field> fields,
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled,
      Boolean shouldCheckSecurity, QueryOptions queryOptionsForAttributeDef,
      String scopeForAttributeDef, boolean splitScopeForAttributeDef,
      boolean hasFieldForAttributeDef, boolean hasMembershipTypeForAttributeDef) {
    Set<Object[]> result = findAllByAttributeDefOwnerOptionsHelper(attributeDefIds, memberIds,
        membershipIds, membershipType, fields, sources, scope, stem, stemScope, 
        enabled, shouldCheckSecurity, null, null, false, false, false, queryOptionsForAttributeDef, scopeForAttributeDef, 
        splitScopeForAttributeDef, hasFieldForAttributeDef, hasMembershipTypeForAttributeDef, null, null, null, null);
    return result;
  }

}
