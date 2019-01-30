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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.member.SearchStringEnum;
import edu.internet2.middleware.grouper.member.SortStringEnum;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITMembershipView;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITMembershipViewDAO extends Hib3DAO implements PITMembershipViewDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITMembershipViewDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findPITGroupSetsJoinedWithNewPITMembership(edu.internet2.middleware.grouper.pit.PITMembership)
   */
  public Set<PITGroupSet> findPITGroupSetsJoinedWithNewPITMembership(PITMembership pitMembership) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct gs from PITGroupSet as gs where memberId = :gsMemberId and memberFieldId = :gsMemberFieldId and activeDb = 'T' " +
          "and not exists (select 1 from PITMembershipView ms where ms.ownerId=gs.ownerId and ms.memberId = :msMemberId and ms.fieldId=gs.fieldId and ms.groupSetActiveDb = 'T' and ms.membershipActiveDb = 'T')")
      .setCacheable(false).setCacheRegion(KLASS + ".FindPITGroupSetsJoinedWithNewPITMembership")
      .setString("gsMemberId", pitMembership.getOwnerId())
      .setString("gsMemberFieldId", pitMembership.getFieldId())
      .setString("msMemberId", pitMembership.getMemberId())
      .listSet(PITGroupSet.class);
    
    return pitGroupSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findPITGroupSetsJoinedWithOldPITMembership(edu.internet2.middleware.grouper.pit.PITMembership)
   */
  public Set<PITGroupSet> findPITGroupSetsJoinedWithOldPITMembership(PITMembership pitMembership) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct gs from PITGroupSet as gs where memberId = :gsMemberId and memberFieldId = :gsMemberFieldId and activeDb = 'T' " +
          "and not exists (select 1 from PITMembershipView ms where ms.ownerId=gs.ownerId and ms.memberId = :msMemberId and ms.fieldId=gs.fieldId and ms.groupSetActiveDb = 'T' and ms.membershipActiveDb = 'T' and ms.membershipId <> :msMembershipId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindPITGroupSetsJoinedWithOldPITMembership")
      .setString("gsMemberId", pitMembership.getOwnerId())
      .setString("gsMemberFieldId", pitMembership.getFieldId())
      .setString("msMemberId", pitMembership.getMemberId())
      .setString("msMembershipId", pitMembership.getId())
      .listSet(PITGroupSet.class);
    
    return pitGroupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findPITMembershipsJoinedWithNewPITGroupSet(edu.internet2.middleware.grouper.pit.PITGroupSet)
   */
  public Set<PITMembership> findPITMembershipsJoinedWithNewPITGroupSet(PITGroupSet pitGroupSet) {
    Set<Object[]> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct ms, m from PITMembership as ms, PITMember as m where ms.ownerId = :msOwnerId and ms.fieldId = :msFieldId and ms.activeDb = 'T' " +
          "and not exists (select 1 from PITMembershipView ms2 where ms2.ownerId = :ms2OwnerId and ms2.memberId = ms.memberId and ms2.fieldId = :ms2FieldId and ms2.groupSetActiveDb = 'T' and ms2.membershipActiveDb = 'T')" +
          "and ms.memberId = m.id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindPITMembershipsJoinedWithNewPITGroupSet")
      .setString("msOwnerId", pitGroupSet.getMemberId())
      .setString("msFieldId", pitGroupSet.getMemberFieldId())
      .setString("ms2OwnerId", pitGroupSet.getOwnerId())
      .setString("ms2FieldId", pitGroupSet.getFieldId())
      .listSet(Object[].class);
    
    return _getPITMembershipsFromPITMembershipAndPITMemberQuery(mships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findPITMembershipsJoinedWithOldPITGroupSet(edu.internet2.middleware.grouper.pit.PITGroupSet)
   */
  public Set<PITMembership> findPITMembershipsJoinedWithOldPITGroupSet(PITGroupSet pitGroupSet) {
    Set<Object[]> mships = HibernateSession
      .byHqlStatic()
      .createQuery("select distinct ms, m from PITMembership as ms, PITMember as m where ms.ownerId = :msOwnerId and ms.fieldId = :msFieldId and ms.activeDb = 'T' " +
          "and not exists (select 1 from PITMembershipView ms2 where ms2.ownerId = :ms2OwnerId and ms2.memberId = ms.memberId and ms2.fieldId = :ms2FieldId and ms2.groupSetActiveDb = 'T' and ms2.membershipActiveDb = 'T' and ms2.groupSetId <> :ms2GroupSetId)" +
          "and ms.memberId = m.id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindPITMembershipsJoinedWithOldPITGroupSet")
      .setString("msOwnerId", pitGroupSet.getMemberId())
      .setString("msFieldId", pitGroupSet.getMemberFieldId())
      .setString("ms2OwnerId", pitGroupSet.getOwnerId())
      .setString("ms2FieldId", pitGroupSet.getFieldId())
      .setString("ms2GroupSetId", pitGroupSet.getId())
      .listSet(Object[].class);
    
    return _getPITMembershipsFromPITMembershipAndPITMemberQuery(mships);
  }
  
  /**
   * @param mships
   * @return set
   */
  private Set<PITMembership> _getPITMembershipsFromPITMembershipAndPITMemberQuery(Collection<Object[]> mships) {
    Set<PITMembership> pitMemberships = new LinkedHashSet<PITMembership>();
  
    for(Object[] tuple:mships) {
      PITMembership currPITMembership = (PITMembership)tuple[0];
      PITMember currPITMember = (PITMember)tuple[1];
      currPITMembership.setMember(currPITMember);
      pitMemberships.add(currPITMembership);
    }
    return pitMemberships;
  }
  
  /**
   * @param mships
   * @return set
   */
  private Set<PITMembershipView> _getPITMembershipViewsFromPITMembershipAndPITMemberQuery(Collection<Object[]> mships) {
    Set<PITMembershipView> pitMemberships = new LinkedHashSet<PITMembershipView>();
  
    for(Object[] tuple:mships) {
      PITMembershipView currPITMembership = (PITMembershipView)tuple[0];
      PITMember currPITMember = (PITMember)tuple[1];
      currPITMembership.setPITMember(currPITMember);
      pitMemberships.add(currPITMembership);
    }
    return pitMemberships;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findByPITOwnerAndPITMemberAndPITField(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public Set<PITMembershipView> findByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId, boolean activeOnly) {
    StringBuilder sql = new StringBuilder();
    sql.append("select ms, m from PITMembershipView as ms, PITMember as m where ms.ownerId = :ownerId and ms.memberId = :memberId and ms.fieldId = :fieldId");
    
    if (activeOnly) {
      sql.append(" and ms.groupSetActiveDb = 'T' and ms.membershipActiveDb = 'T'");
    }
    
    sql.append(" and ms.memberId = m.id");
    
    Set<Object[]> mships = HibernateSession
      .byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false).setCacheRegion(KLASS + ".FindByPITOwnerAndPITMemberAndPITField")
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .listSet(Object[].class);
    
    return _getPITMembershipViewsFromPITMembershipAndPITMemberQuery(mships);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findAllMembersByPITOwnerAndPITField(java.lang.String, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, java.util.Set, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<Member> findAllMembersByPITOwnerAndPITField(String ownerId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, Set<Source> sources, QueryOptions queryOptions) {

    StringBuilder sql = new StringBuilder("select m "
        + "from Member m, PITMember pitMember, PITMembershipView ms where "
        + "ms.ownerId = :ownerId "
        + "and ms.fieldId = :fieldId "
        + "and ms.memberId = pitMember.id "
        + "and pitMember.sourceId = m.uuid");
    
    if (pointInTimeFrom != null) {
      Long endDateAfter = pointInTimeFrom.getTime() * 1000;
      sql.append(" and (ms.membershipEndTimeDb is null or ms.membershipEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (ms.groupSetEndTimeDb is null or ms.groupSetEndTimeDb > '" + endDateAfter + "')");
    }
    
    if (pointInTimeTo != null) {
      Long startDateBefore = pointInTimeTo.getTime() * 1000;
      sql.append(" and ms.membershipStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and ms.groupSetStartTimeDb < '" + startDateBefore + "'");
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
      .setCacheRegion(KLASS + ".FindAllMembersByPITOwnerAndPITField")
      .setString("ownerId", ownerId) 
      .setString("fieldId", fieldId)
      .listSet(Member.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findAllByPITOwnerAndPITMemberAndPITField(java.lang.String, java.lang.String, java.lang.String, java.sql.Timestamp, java.sql.Timestamp, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<PITMembershipView> findAllByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId, 
      Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions) {

    StringBuilder sql = new StringBuilder("select ms "
        + "from PITMembershipView ms where "
        + "ms.ownerId = :ownerId "
        + "and ms.memberId = :memberId "
        + "and ms.fieldId = :fieldId");
    
    if (pointInTimeFrom != null) {
      Long endDateAfter = pointInTimeFrom.getTime() * 1000;
      sql.append(" and (ms.membershipEndTimeDb is null or ms.membershipEndTimeDb > '" + endDateAfter + "')");
      sql.append(" and (ms.groupSetEndTimeDb is null or ms.groupSetEndTimeDb > '" + endDateAfter + "')");
    }
    
    if (pointInTimeTo != null) {
      Long startDateBefore = pointInTimeTo.getTime() * 1000;
      sql.append(" and ms.membershipStartTimeDb < '" + startDateBefore + "'");
      sql.append(" and ms.groupSetStartTimeDb < '" + startDateBefore + "'");
    }
    
    return HibernateSession.byHqlStatic().options(queryOptions)
      .createQuery(sql.toString())
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindAllByPITOwnerAndPITMemberAndPITField")
      .setString("ownerId", ownerId) 
      .setString("memberId", memberId) 
      .setString("fieldId", fieldId)
      .listSet(PITMembershipView.class);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITMembershipViewDAO#findAllByGroupOwnerOptions(java.util.Collection, java.util.Collection, java.util.Collection, java.util.Set, java.lang.Boolean, edu.internet2.middleware.grouper.FieldType, edu.internet2.middleware.grouper.internal.dao.QueryOptions, java.lang.String, boolean, boolean, java.sql.Timestamp, java.sql.Timestamp)
   */
  public Set<Object[]> findAllByGroupOwnerOptions(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<Field> fields,
      Set<Source> sources, Boolean checkSecurity, FieldType fieldType,
      QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {
    
    return this.findAllByGroupOwnerOptionsHelper(totalGroupIds, totalMemberIds, fields, sources, checkSecurity, fieldType, 
        queryOptionsForMember, filterForMember, splitScopeForMember, hasFieldForMember, pointInTimeFrom, pointInTimeTo); 
  }

  private Set<Object[]> findAllByGroupOwnerOptionsHelper(Collection<String> totalGroupIds, Collection<String> totalMemberIds,
      Collection<Field> fields,
      Set<Source> sources, Boolean checkSecurity, FieldType fieldType,
      QueryOptions queryOptionsForMember, String filterForMember, boolean splitScopeForMember, 
      boolean hasFieldForMember, Timestamp pointInTimeFrom, Timestamp pointInTimeTo) {

    QueryOptions.initTotalCount(queryOptionsForMember);
    
    if (checkSecurity == null) {
      checkSecurity = Boolean.TRUE;
    }
    
    if (GrouperUtil.length(fields) != 0) {
      throw new RuntimeException("Fields not supported for now.");
    }
    
    if (fieldType != null) {
      throw new RuntimeException("Field type not supported for now.");
    }
    
    final List<String> totalGroupIdsList = GrouperUtil.listFromCollection(totalGroupIds);
    List<String> totalMemberIdsList = GrouperUtil.listFromCollection(totalMemberIds);

    Set<Object[]> totalResults = new HashSet<Object[]>();
    
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    final Subject grouperSessionSubject = grouperSession.getSubject();
    
    final Set<Privilege> privilegesTheUserHasFinal = new HashSet<Privilege>();
    
    if (GrouperUtil.length(fields) == 0 || !fields.iterator().next().isGroupAccessField()) {
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
      
      checkSecurity = false;
    }

    if (checkSecurity) {
      throw new RuntimeException("Unsupported for now");
    }
    
    int groupBatches = GrouperUtil.batchNumberOfBatches(totalGroupIds, 100);

    for (int groupIndex = 0; groupIndex < groupBatches; groupIndex++) {
      
      List<String> groupIds = GrouperUtil.batchList(totalGroupIdsList, 100, groupIndex);
      
      int memberBatches = GrouperUtil.batchNumberOfBatches(totalMemberIds, 100);

      for (int memberIndex = 0; memberIndex < memberBatches; memberIndex++) {
        
        List<String> memberIds = GrouperUtil.batchList(totalMemberIdsList, 100, memberIndex);          
        
        int groupIdsSize = GrouperUtil.length(groupIds);
        int memberIdsSize = GrouperUtil.length(memberIds);
        
        if (groupIdsSize == 0 && memberIdsSize == 0 ) {
          throw new RuntimeException("Must pass in group(s) and/or member(s)");
        }

        ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();

        String selectPrefix = "select distinct pitms, pitg, pitm, m ";
        
        //note: mysql wont let you do count distinct of multiple columns
        String countPrefix = "select count(*) ";

        StringBuilder sql = new StringBuilder(" from Member m, PITMembershipView pitms, Group g, Field f, PITMember pitm, PITGroup pitg, PITField pitf where");
        
        sql.append(" pitms.ownerGroupId = pitg.id "
            + " and pitms.fieldId = pitf.id "
            + " and pitms.memberId = pitm.id "
            + " and pitg.sourceId = g.uuid "
            + " and pitf.sourceId = f.uuid "
            + " and pitm.sourceId = m.uuid ");
        
        if (sources != null && sources.size() > 0) {
          sql.append(" and m.subjectSourceIdDb in ").append(HibUtils.convertSourcesToSqlInString(sources));
        }
        
        if (GrouperUtil.length(fields) > 0) {

          sql.append(" and f.uuid in ( ");
          Set<String> fieldStrings = new HashSet<String>();
          for (Field field : fields) {
            fieldStrings.add(field.getUuid());
          }
          sql.append(HibUtils.convertToInClause(fieldStrings, byHqlStatic));
          sql.append(" ) ");
        }

        if (GrouperUtil.length(fields) == 0 && (fieldType == null || fieldType == FieldType.LIST)) {
          
          //add on the column
          sql.append(" and f.typeString = 'list' ");
        }
        if (fieldType == FieldType.ACCESS) {
          //add on the column
          sql.append(" and f.typeString = 'access' ");
        }
        if (groupIdsSize > 0) {
          sql.append(" and g.uuid in (");
          sql.append(HibUtils.convertToInClause(groupIds, byHqlStatic));
          sql.append(") ");
        }
        if (memberIdsSize > 0) {
          sql.append(" and m.uuid in (");
          sql.append(HibUtils.convertToInClause(memberIds, byHqlStatic));
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

        if (pointInTimeFrom != null) {
          Long endDateAfter = pointInTimeFrom.getTime() * 1000;
          sql.append(" and (pitms.membershipEndTimeDb is null or pitms.membershipEndTimeDb > '" + endDateAfter + "')");
          sql.append(" and (pitms.groupSetEndTimeDb is null or pitms.groupSetEndTimeDb > '" + endDateAfter + "')");
        }
        
        if (pointInTimeTo != null) {
          Long startDateBefore = pointInTimeTo.getTime() * 1000;
          sql.append(" and pitms.membershipStartTimeDb < '" + startDateBefore + "'");
          sql.append(" and pitms.groupSetStartTimeDb < '" + startDateBefore + "'");
        }
        
        // make sure membership object didn't end before group set object started
        sql.append(" and (pitms.membershipEndTimeDb is null or pitms.membershipEndTimeDb > pitms.groupSetStartTimeDb) ");
        
        // make sure group set object didn't end before membership object started
        sql.append(" and (pitms.groupSetEndTimeDb is null or pitms.groupSetEndTimeDb > pitms.membershipStartTimeDb) ");
        
        byHqlStatic
          .setCacheable(false)
          .setCacheRegion(KLASS);

        int maxMemberships = GrouperConfig.retrieveConfig().propertyValueInt("ws.getMemberships.maxResultSize", 30000);

        {
          boolean pageMembers = queryOptionsForMember != null && queryOptionsForMember.getQueryPaging() != null;
          
          if (pageMembers) {

            //cant page too much...
            if (queryOptionsForMember.getQueryPaging().getPageSize() > 500) {
              throw new RuntimeException("Cant get a page size greater then 500! " 
                  + queryOptionsForMember.getQueryPaging().getPageSize());
            }

            if (groupBatches > 1) {
              throw new RuntimeException("Cant have more than 1 groupBatch if paging members");
            }
            
            if (memberBatches > 1) {
              throw new RuntimeException("Cant have more than 1 memberBatch if paging members");
            }
          }

          if (!StringUtils.isBlank(filterForMember) && !pageMembers) {
            throw new RuntimeException("If you are filtering by member, then you must page members");
          }

          //if -1, lets not check
          if (maxMemberships >= 0 && !pageMembers) {
  
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
                hasFieldForMember ? null : fields,
                sources, checkSecurity, fieldType, 
                null, null, false, false,
                pointInTimeFrom, pointInTimeTo);
            
            //lets sort these by member
            Set<Object[]> sortedResults = new LinkedHashSet<Object[]>();
            
            for (Member member : members) {
              List<Object[]> currentTempResults = new ArrayList<Object[]>();
              
              Iterator<Object[]> iterator = tempResults.iterator();
              while(iterator.hasNext()) {
                
                Object[] tempResult = iterator.next();
                //if the member is the same, put it in the sortedResults, and remove
                if (StringUtils.equals(((Member)tempResult[3]).getUuid(), member.getUuid())) {
                  
                  currentTempResults.add(tempResult);
                  iterator.remove();
                }
              }
              
              Collections.sort(currentTempResults, new Comparator<Object[]>() {
                @Override
                public int compare(Object[] o1, Object[] o2) {
                  int i = ((PITMembershipView)o1[0]).getStartTime().compareTo(((PITMembershipView)o2[0]).getStartTime());
                  if (i != 0) {
                    return i;
                  }
                  
                  if (((PITMembershipView)o1[0]).getEndTime() == null && ((PITMembershipView)o2[0]).getEndTime() != null) {
                    return 1;
                  }
                  
                  if (((PITMembershipView)o1[0]).getEndTime() != null && ((PITMembershipView)o2[0]).getEndTime() == null) {
                    return -1;
                  }
                  
                  if (((PITMembershipView)o1[0]).getEndTime() == null && ((PITMembershipView)o2[0]).getEndTime() == null) {
                    return 0;
                  }
                  
                  return ((PITMembershipView)o1[0]).getEndTime().compareTo(((PITMembershipView)o2[0]).getEndTime());
                }
              });
              
              sortedResults.addAll(currentTempResults);
            }
            return sortedResults;
            
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
          Set<Object[]> results = byHqlStatic.createQuery(selectPrefix + sql.toString()).listSet(Object[].class);

          totalResults.addAll(results);
        }
      }
    }
    
    
    //nothing to filter
    if (GrouperUtil.length(totalResults) == 0) {
      return totalResults;
    }
    
    assignMembersOwnersToMemberships(totalResults);

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
    PITMembershipView membership = (PITMembershipView)membershipArray[0];
    PITMember member = (PITMember)membershipArray[2];

    membership.setPITMember(member);
  }
}

