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
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.MembershipNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MembershipDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.util.Quote;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
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
      .setCacheRegion(KLASS + ".FindAll")
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
      .setCacheRegion(KLASS + ".FindByCreatorOrMember")
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
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
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
      .setCacheRegion(KLASS + ".FindAllByCreatedAfter")
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
      .setCacheRegion(KLASS + ".FindAllByMember")
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
      .setCacheRegion(KLASS + ".FindAllByGroupOwnerAndField")
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
	      .setCacheRegion(KLASS + ".FindMembershipsByStemOwnerType")
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
      .setCacheRegion(KLASS + ".FindMembershipsByGroupOwnerType")
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
      .setCacheRegion(KLASS + ".FindAllByGroupOwnerAndFieldAndDepth")
      .setString("owner", ownerGroupId)
      .setString("fuuid", f.getUuid())
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
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByGroupOwnerAndMemberAndField")
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
    StringBuilder sql = new StringBuilder("select m"
        + " from Member m, MembershipEntry ms where"
        + " ms.ownerGroupId      = :owner "
        + "and  ms.fieldId = :fieldId "
        + " and ms.memberUuid = m.uuid ");
    if (enabledOnly) {
      sql.append(" and ms.enabledDb = 'T'");
    }

    if (sources != null && sources.size() > 0) {
      sql.append(" and m.subjectSourceIdDb in ").append(GrouperUtil.convertSourcesToSqlInString(sources));
    }
    
    
    return HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembersByGroupOwnerAndField")
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
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<String> totalMembershipIds, MembershipType membershipType,
      Field field,  
      Set<Source> sources, String scope, Stem stem, Scope stemScope, Boolean enabled) {
    
    if ((stem == null) != (stemScope == null)) {
      throw new RuntimeException("If stem is set, then stem scope must be set.  If stem isnt set, then stem scope must not be set: " + stem + ", " + stemScope);
    }

    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    Subject grouperSessionSubject = grouperSession.getSubject();

    Set<Object[]> totalResults = new HashSet<Object[]>();
    
    int groupBatches = GrouperUtil.batchNumberOfBatches(totalGroupIds, 100);

    for (int groupIndex = 0; groupIndex < groupBatches; groupIndex++) {
      
      List<String> groupIds = GrouperUtil.batchList(totalGroupIds, 100, groupIndex);
      
      int memberBatches = GrouperUtil.batchNumberOfBatches(totalMemberIds, 100);

      for (int memberIndex = 0; memberIndex < memberBatches; memberIndex++) {
        
        List<String> memberIds = GrouperUtil.batchList(totalMemberIds, 100, memberIndex);
        int membershipBatches = GrouperUtil.batchNumberOfBatches(totalMembershipIds, 100);
        
        for (int membershipIndex = 0; membershipIndex < membershipBatches; membershipIndex++) {
          
          List<String> membershipIds = GrouperUtil.batchList(totalMembershipIds, 100, membershipIndex);
          
          int groupIdsSize = GrouperUtil.length(groupIds);
          int memberIdsSize = GrouperUtil.length(memberIds);
          int membershipIdsSize = GrouperUtil.length(membershipIds);
          
          if (groupIdsSize == 0 && memberIdsSize == 0 && membershipIdsSize == 0) {
            throw new RuntimeException("Must pass in group(s), member(s), and/or membership(s)");
          }

          ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

          String selectPrefix = "select ms, g, m ";
          String countPrefix = "select count(*) ";
          
          StringBuilder sql = new StringBuilder(" from Member m, MembershipEntry ms, Group g ");
          
          //we need to make sure it is a list type field
          if (field == null) {
            sql.append(", Field f ");
          }
          
          boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(
              grouperSessionSubject, byHqlStatic, 
              sql, "ms.ownerGroupId", AccessPrivilege.READ_PRIVILEGES);

          if (!changedQuery) {
            sql.append(" where ");
          } else {
            sql.append(" and ");
          }
          
          sql.append(" ms.ownerGroupId = g.uuid "
              + " and ms.memberUuid = m.uuid ");
          if (enabled != null && enabled) {
            sql.append(" and ms.enabledDb = 'T' ");
          }
          if (enabled != null && !enabled) {
            sql.append(" and ms.enabledDb = 'F' ");
          }
          if (sources != null && sources.size() > 0) {
            sql.append(" and m.subjectSourceIdDb in ").append(GrouperUtil.convertSourcesToSqlInString(sources));
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
          if (field != null) {
            //needs to be a members field
            if (!StringUtils.equals("list",field.getTypeString())) {
              throw new RuntimeException("This method only works with members fields: " + field);
            }
            sql.append(" and ms.fieldId = :fieldId ");
            byHqlStatic.setString("fieldId", field.getUuid());
          } else {
            //add on the column
            sql.append(" and ms.fieldId = f.uuid and f.typeString = 'list' ");
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
            sql.append(" and ms.uuid in (");
            sql.append(HibUtils.convertToInClause(membershipIds, byHqlStatic));
            sql.append(") ");
          }
          
          byHqlStatic
            .setCacheable(false)
            .setCacheRegion(KLASS + ".FindAllByGroupOwnerOptions");

          int maxMemberships = GrouperConfig.getPropertyInt("ws.getMemberships.maxResultSize", 30000);
          
          //if -1, lets not check
          if (maxMemberships >= 0) {

            long size = byHqlStatic.createQuery(countPrefix + sql.toString()).uniqueResult(long.class);    
            
            //see if too many
            if (size > maxMemberships) {
              throw new RuntimeException("Too many results: " + size);
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
	  StringBuilder sql = new StringBuilder("select distinct m "
		        + "from Member m, MembershipEntry ms where "
		        + "ms.ownerGroupId = :owner "
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
      sql.append(" and m.subjectSourceIdDb in ").append(GrouperUtil.convertSourcesToSqlInString(sources));
    }
    return HibernateSession.byHqlStatic()
    .createQuery(sql.toString())
    .setCacheable(false)
    .setCacheRegion(KLASS + ".FindAllMembersByGroupOwnerAndFieldTypeAndType")
    .options(queryOptions)
    .setString("owner", ownerGroupId)
    .setString( "fieldId", f.getUuid() )
    .listSet(Member.class);

  } 
  
  /**
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
    
    return HibernateSession.byHqlStatic()
    .createQuery(sql.toString())
    .setCacheable(false)
    .setCacheRegion(KLASS + ".FindAllMembersByStemOwnerAndFieldTypeAndType")
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

    //lets page through these
    int pages = GrouperUtil.batchNumberOfBatches(memberIds, batchSize);
    
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    MembershipType membershipType = MembershipType.valueOfIgnoreCase(type, true);
    
    for (int i=0; i<pages; i++) {
      List<String> currentMemberIdList = GrouperUtil.batchList(memberIds, batchSize, i);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select ms"
            + " from MembershipEntry ms where"
            + " ms.ownerGroupId      = :owner "
            + "and  ms.fieldId = :fieldId "
            + "and  ms.type  " + membershipType.queryClause()
            + " and ms.memberUuid in (");
        byHqlStatic.setString( "owner", ownerGroupId ) 
          .setString( "fieldId", f.getUuid() );

      //add all the uuids
      byHqlStatic.setCollectionInClause(query, currentMemberIdList);
      query.append(")");
      
      if (enabledOnly) {
        query.append(" and ms.enabledDb = 'T'");
      }
      
      List<Membership> currentList = byHqlStatic.createQuery(query.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllByOwnerAndFieldAndMembers")
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
    int pages = GrouperUtil.batchNumberOfBatches(members, batchSize);
    
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    for (int i=0; i<pages; i++) {
      List<Member> memberList = GrouperUtil.batchList(members, batchSize, i);
      
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
        .setCacheRegion(KLASS + ".FindAllByOwnerAndFieldAndMembers")
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
    int pages = GrouperUtil.batchNumberOfBatches(members, batchSize);
    
    Set<Membership> memberships = new LinkedHashSet<Membership>();
    
    for (int i=0; i<pages; i++) {
      List<Member> memberList = GrouperUtil.batchList(members, batchSize, i);
      
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
        .setCacheRegion(KLASS + ".FindAllByOwnerAndCompositeAndMembers")
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
        .setCacheable(false).setCacheRegion(
            KLASS + ".FindByGroupOwnerAndMemberAndFieldAndType").setString("owner",
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
    Membership ms = (Membership) result[0];
    Member m = (Member) result[1];
    ms.setMember(m);
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
      .setCacheRegion(KLASS + ".FindByStemOwnerAndMemberAndFieldAndType")
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
  Membership ms = (Membership)result[0];
  Member m = (Member)result[1];
  ms.setMember(m);
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
      .setCacheRegion(KLASS + ".FindChildMemberships")
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
      .setCacheRegion(KLASS + ".FindAllEffectiveByGroupOwner")
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
      .setCacheRegion(KLASS + ".FindAllEffectiveByStemOwner")
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
      .setCacheRegion(KLASS + ".FindAllEffectiveByMemberAndField")
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
      .setCacheRegion(KLASS + ".FindAllEffectiveByGroupOwnerAndMemberAndField")
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
      .setCacheRegion(KLASS + ".FindAllByGroupOwnerAndMemberAndField")
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
      .setCacheRegion(KLASS + ".FindAllImmediateByMember")
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
      .setCacheRegion(KLASS + ".FindAllByGroupOwner")
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
      .setCacheRegion(KLASS + ".FindAllByStemOwner")
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
      .setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField")
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
      .setCacheRegion(KLASS + ".FindAllImmediateByMemberAndFieldType")
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
    
    int index = uuid.indexOf(Membership.membershipIdSeparator);
    
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic().setCacheable(false)
      .setCacheRegion(KLASS + ".FindByUuid");

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
    byHqlStatic.createQuery(sql.toString());
    Object[] result = byHqlStatic.uniqueResult(Object[].class);
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

    if (!changedQuery) {
      sql.append(" where ");
    } else {
      sql.append(" and ");
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
      .setCacheRegion(KLASS + ".FindMemberships")
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
  }


  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#save(java.util.Set)
   */
  public void save(Set<Membership> mships) {
    Iterator<Membership> iter = mships.iterator();
    while (iter.hasNext()) {
      save(iter.next());
    }
  }
  

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#delete(edu.internet2.middleware.grouper.Membership)
   */
  public void delete(Membership ms) {
    HibernateSession.byObjectStatic().setEntityName("ImmediateMembershipEntry").delete(ms);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#update(edu.internet2.middleware.grouper.Membership)
   */
  public void update(Membership ms) {
    HibernateSession.byObjectStatic().setEntityName("ImmediateMembershipEntry").update(ms);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#delete(java.util.Set)
   */
  public void delete(Set<Membership> mships) {
    Iterator<Membership> iter = mships.iterator();
    while (iter.hasNext()) {
      delete(iter.next());
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#update(java.util.Set)
   */
  public void update(Set<Membership> mships) {
    Iterator<Membership> iter = mships.iterator();
    while (iter.hasNext()) {
      update(iter.next());
    }
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

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findParentMembership(edu.internet2.middleware.grouper.Membership)
   */
  public Membership findParentMembership(Membership _ms) throws GrouperDAOException {
    Object result[] = HibernateSession
        .byHqlStatic()
        .createQuery(
            "select ms, m from MembershipEntry as ms, Member as m where ms.groupSetId = :groupSetId "
                + "and m.subjectIdDb = :groupId and ms.memberUuid = m.uuid")
        .setCacheable(false).setCacheRegion(KLASS + ".FindParentMembership")
        .setString("groupSetId", _ms.getGroupSetParentId())
        .setString("groupId", _ms.getViaGroupId())
        .uniqueResult(Object[].class);

    if (result == null || result[0] == null) {
      throw new MembershipNotFoundException();
    }
    
    Membership ms = (Membership) result[0];
    Member m = (Member) result[1];
    ms.setMember(m);
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
      .setCacheRegion(KLASS + ".FindMissingImmediateGroupSetsForGroupOwners")
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
      .setCacheRegion(KLASS + ".FindMissingImmediateGroupSetsForStemOwners")
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
        .setCacheRegion(KLASS + ".FindByAttrDefOwnerAndMemberAndFieldAndType")
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
    Membership ms = (Membership) result[0];
    Member m = (Member) result[1];
    ms.setMember(m);
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
      .setCacheRegion(KLASS + ".FindAllEffectiveByAttrDefOwner")
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
    
    StringBuilder sql = new StringBuilder("select m "
        + "from Member m, MembershipEntry ms where "
        + "ms.ownerAttrDefId = :owner "
        + "and ms.fieldId = :fieldId "
        + "and ms.type " + membershipType.queryClause()
        + " and ms.memberUuid = m.uuid  ");
      if (enabledOnly) {
        sql.append(" and ms.enabledDb = 'T'");
      }
      
      return HibernateSession.byHqlStatic()
        .createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindAllMembersByAttrDefOwnerAndFieldTypeAndType")
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
      .setCacheRegion(KLASS + ".FindMissingImmediateGroupSetsForAttrDefOwners")
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
      .setCacheRegion(KLASS + ".FindAllByAttrDefOwnerAndField")
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
        .setCacheRegion(KLASS + ".FindMembershipsByAttrDefOwnerType")
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
      .setCacheRegion(KLASS + ".FindAllByAttrDefOwnerAndMemberAndField")
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
      .setCacheRegion(KLASS + ".FindAllByAttrDefOwner")
      .setString("owner", attrDefId)
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
      .setCacheRegion(KLASS + ".findAllByAttrDefOwnerAndMember")
      .setString( "owner",  ownerAttrDefId              )
      .setString( "member", memberUUID             )
      .listSet(Object[].class);
    return _getMembershipsFromMembershipAndMemberQuery(mships);

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllEnabledDisabledMismatch()
   */
  public Set<Membership> findAllEnabledDisabledMismatch() {
    long now = System.currentTimeMillis();

    StringBuilder sql = new StringBuilder(
        "select ms from ImmediateMembershipEntry as ms where  "
          + "(ms.enabledDb = 'F' and ms.enabledTimeDb is null and ms.disabledTimeDb is null) "  
          + " or (ms.enabledDb = 'F' and ms.enabledTimeDb is null and ms.disabledTimeDb > :now) "
          + " or (ms.enabledDb = 'F' and ms.enabledTimeDb < :now and ms.disabledTimeDb is null) "
          + " or (ms.enabledDb = 'F' and ms.enabledTimeDb < :now and ms.disabledTimeDb > :now) "
          + " or (ms.enabledDb = 'T' and ms.disabledTimeDb < :now) "
          + " or (ms.enabledDb = 'T' and ms.enabledTimeDb > :now) "
          + " or (ms.enabledDb <> 'T' and ms.enabledDb <> 'F') "
          + " or (ms.enabledDb is null) "
     );

    Set<Membership> memberships = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setLong( "now",  now )
      .listSet(Membership.class);
    return memberships;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MembershipDAO#findAllNonImmediateByMember(java.lang.String, boolean)
   */
  public Set findAllNonImmediateByMember(String memberUUID, boolean enabledOnly)
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
      .setCacheRegion(KLASS + ".FindAllNonImmediateByMember")
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
      .setCacheRegion(KLASS + ".FindAllImmediateByMemberAndField")
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
      .setCacheRegion(KLASS + ".FindAllImmediateByMemberAndFieldType")
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
        .setCacheRegion(KLASS + ".findByUuidOrKey")
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
      .setCacheRegion(KLASS + ".FindSourceIdsByGroupOwnerOptions");

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
      .setCacheRegion(KLASS + ".FindAllByStemParentOfGroupOwnerAndFieldAndType")
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
    StringBuilder sql = new StringBuilder("select theMember from MembershipEntry as inMembershipEntry, Member as theMember where  "
        + " inMembershipEntry.ownerGroupId   = :ownerInGroupId            ");
    
    if (disabledOwnerNull) {
      sql.append(" and inMembershipEntry.disabledTimeDb is null ");
    }
    
    sql.append(" and inMembershipEntry.memberUuid   = theMember.uuid            "
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
    sql.append(" and  theMember.uuid not in ( select notInMembershipEntry.memberUuid from MembershipEntry as notInMembershipEntry " +
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
        		
    
    Set<Member> members = HibernateSession.byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllMembersInOneGroupNotOtherAndType")
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
      .setCacheRegion(KLASS + ".FindAllMembershipsByGroupOwnerFieldDisabledRange")
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

    StringBuilder sql = new StringBuilder("select theMember from MembershipEntry as inMembershipEntry, Member as theMember where  "
        + " inMembershipEntry.ownerGroupId   = :ownerInGroupId            "
        + " and inMembershipEntry.memberUuid   = theMember.uuid            "
        + " and  inMembershipEntry.fieldId = '" + Group.getDefaultList().getUuid() + "' ");
    if (typeInEnum != null) {
      sql.append(" and  inMembershipEntry.type  " + typeInEnum.queryClause());
    }

    sql.append(" and  not exists ( select notInMembershipEntry.memberUuid " +
    		" from MembershipEntry as notInMembershipEntry, Group as theStemGroup " +
            " where notInMembershipEntry.ownerGroupId = theStemGroup.uuid "
    		    + " and notInMembershipEntry.memberUuid = theMember.uuid "
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
            
    
    Set<Member> members = byHqlStatic
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".findAllMembersInOneGroupNotStem")
      .setString( "ownerInGroupId" , ownerInGroupId                 )
      .listSet(Member.class);

    return members;

  }

}
