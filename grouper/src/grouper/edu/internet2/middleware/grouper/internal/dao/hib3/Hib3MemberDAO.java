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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperAccessAdapter;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.usdu.UsduAttributeNames;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MemberNotUniqueException;
import edu.internet2.middleware.grouper.hibernate.ByCriteriaStatic;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.MemberDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.member.SortStringEnum;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 * Basic Hibernate <code>Member</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3MemberDAO.java,v 1.20 2009-12-28 06:08:37 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3MemberDAO extends Hib3DAO implements MemberDAO {

  
  private static final  String                      KLASS           = Hib3MemberDAO.class.getName();

  /**
   * number of subjects to put in member query
   */
  public static int MEMBER_SUBJECT_BATCH_SIZE = 80;

  /**
   * config key for caching
   */
  private static final String GROUPER_FLASHCACHE_MEMBERS_IN_FINDER = "grouper.flashcache.members.in.finder";

  /**
   *  multikey is either:
   *  uuid / <theUuid>
   *  sourceIdSubjectId / <theSourceId> / <theSubjectId>
   */
  private static GrouperCache<MultiKey, Member> membersFlashCache = new GrouperCache(
      "edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO.memberFlashCache", 10000, false, 30, 120, false);

  /**
   * @since   @HEAD@
   */
  public void create(Member _m) 
    throws  GrouperDAOException {

    HibernateSession.byObjectStatic().save(_m);
  } 

  /**
   * @since   @HEAD@
   */
  public boolean exists(String uuid) 
    throws  GrouperDAOException
  {
    return findByUuid(uuid, false) != null;
  } 
  
  /**
   * @since   @HEAD@
   */
  public Set findAll() 
    throws  GrouperDAOException
  {
    return findAll(null);
  } // public Set findAll()
  
  /**
   * @since   @HEAD@
   */
  public Set<Member> findAll(Source source) 
    throws  GrouperDAOException
  {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    if (source == null) {
      byHqlStatic.createQuery("from Member");
    } else {
      byHqlStatic.createQuery("from Member as m where m.subjectSourceIdDb=:sourceId");
      byHqlStatic.setString("sourceId", source.getId());
    }
    return byHqlStatic.listSet(Member.class);  
  } // public findAll(Source source)

  /**
   * @deprecated use overload
   */
  @Deprecated
  public Member findBySubject(Subject subj)
    throws  GrouperDAOException,
            MemberNotFoundException {
    return findBySubject(subj, true);
  } 

  /**
   * 
   */
  public Member findBySubject(Subject subj, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException {
    return this.findBySubject( subj.getId(), subj.getSource().getId(), subj.getType().getName() );
  }

  /**
   * @deprecated use overload
   */
  @Deprecated
  public Member findBySubject(String id, String src, String type) 
    throws  GrouperDAOException,
            MemberNotFoundException {
    return findBySubject(id, src, type, true);
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#findBySubject(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public Member findBySubject(String id, String src, String type, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException {
    return findBySubject(id, src, type, exceptionIfNull, null);
  }

  /**
   * if there are sort fields, go through them, and replace name with nameDb, etc,
   * extension for extensionDb, displayName with displayNameDb, and displayExtension with displayExtensionDb
   * @param querySort
   */
  public static void massageMemberSortFields(QuerySort querySort) {
    massageMemberSortFields(querySort, "m");
  }

  /**
   * if there are sort fields, go through them, and replace name with m.subjectIdDb, etc,
   * sort_string0 for m.sortString0, etc
   * @param querySort
   */
  public static void massageMemberSortFields(QuerySort querySort, String alias) {
    if (querySort == null) {
      return;
    }

    for (QuerySortField querySortField : GrouperUtil.nonNull(querySort.getQuerySortFields())) {
      if (StringUtils.equalsIgnoreCase("uuid", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("id", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".uuid");
      }
      if (StringUtils.equalsIgnoreCase("subjectIdDb", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("subject_id", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("subjectId", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".subjectIdDb");
      }
      if (StringUtils.equalsIgnoreCase("subjectSourceIdDb", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("subject_source", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sourceId", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".subjectSourceIdDb");
      }
      if (StringUtils.equalsIgnoreCase("subjectTypeId", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("subject_type", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".subjectTypeId");
      }
      if (StringUtils.equalsIgnoreCase("sortString0", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string0", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(0).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn(alias + ".sortString0");
      }
      if (StringUtils.equalsIgnoreCase("sortString1", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string1", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(1).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn(alias + ".sortString1");
      }
      if (StringUtils.equalsIgnoreCase("sortString2", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string2", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(2).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn(alias + ".sortString2");
      }
      if (StringUtils.equalsIgnoreCase("sortString3", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string3", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(3).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn(alias + ".sortString3");
      }
      if (StringUtils.equalsIgnoreCase("sortString4", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string4", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(4).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn(alias + ".sortString4");
      }
      if (StringUtils.equalsIgnoreCase("name", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".name");
      }
      if (StringUtils.equalsIgnoreCase("description", querySortField.getColumn())) {
        querySortField.setColumn(alias + ".description");
      }

    }

  }


  /**
   * 
   * @param id
   * @param src
   * @param type
   * @param exceptionIfNull
   * @param queryOptions
   * @return member
   * @throws GrouperDAOException
   * @throws MemberNotFoundException
   */
  public Member findBySubject(String id, String src, String type, boolean exceptionIfNull, QueryOptions queryOptions)
      throws GrouperDAOException, MemberNotFoundException {
    
    Member member = membersFlashCacheRetrieveBySubjectId(src, id, queryOptions);
    
    if (member == null) {
      member = HibernateSession.byHqlStatic()
          .createQuery("from Member as m where "
            + "     m.subjectIdDb       = :sid    "  
            + "and  m.subjectSourceIdDb = :source "
            + "and  m.subjectTypeId   = :type")
            .setCacheable(true)
            .setCacheRegion(KLASS + ".FindBySubject")
            .options(queryOptions)
            .setString( "sid",    id   )
            .setString( "type",   type )
            .setString( "source", src  )
            .uniqueResult(Member.class);
          
      if (exceptionIfNull && member == null) {
        throw new MemberNotFoundException();
      }
      
      //dont cache this
      if (!exceptionIfNull && member == null) {
        return null;
      }

      membersFlashCacheAddIfSupposedTo(member);
        
    }
    return member;
    
  }



  /**
   * @deprecated use overload
   */
  @Deprecated
  public Member findByUuid(String uuid) 
    throws  GrouperDAOException,
            MemberNotFoundException  {
    return findByUuid(uuid, true);
  } 

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#findByUuid(java.lang.String, boolean, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Member findByUuid(String uuid, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException {
    return findByUuid(uuid, exceptionIfNull, null);
  }
  
  /**
   * 
   */
  public Member findByUuid(String uuid, boolean exceptionIfNull, QueryOptions queryOptions)
      throws GrouperDAOException, MemberNotFoundException {
    Member memberDto = null;
    if (!StringUtils.isBlank(uuid)) {
      memberDto = membersFlashCacheRetrieveById(uuid, queryOptions);

      if (memberDto == null) {
        try {
          memberDto = HibernateSession.byHqlStatic()
            .createQuery("from Member as m where m.uuid = :uuid")
            .setCacheable(false) // but i probably should - or at least permit it
            //.setCacheRegion(KLASS + ".FindByUuid")
            .setString("uuid", uuid).uniqueResult(Member.class);

          membersFlashCacheAddIfSupposedTo(memberDto);

        } catch (GrouperDAOException gde) {
          Throwable throwable = gde.getCause();
          //CH 20080218 this was legacy error handling
          if (throwable instanceof HibernateException) {
            LOG.fatal( throwable.getMessage() );
          }
          throw gde;
        }
      }
    }
    if (memberDto == null && exceptionIfNull) {
      throw new MemberNotFoundException();
    }
    return memberDto;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(Hib3MemberDAO.class);

  /**
   * update the exists cache
   * @param uuid
   * @param exists
   */
  @Deprecated
  public void existsCachePut(String uuid, boolean exists) {
  }

  /**
   * remove all caches
   */
  public static void membersCacheClear() {
    membersFlashCache.clear();
  }
  /**
   * remove this from all caches
   * @param member
   */
  public static void membersCacheRemove(Member member) {
    if (member != null) {
      membersFlashCache.remove(membersFlashCacheMultikeyById(member.getId()));
      
      if (LOG.isDebugEnabled()) {
        LOG.debug("removing member from flash cache by id: " + member.getId());
      }

      membersFlashCache.remove(membersFlashCacheMultikeyBySubjectId(member.getSubjectSourceId(), member.getSubjectId()));

      if (LOG.isDebugEnabled()) {
        LOG.debug("removing member to flash cache by source and subjectId: " + member.getSubjectSourceId() + ", " + member.getSubjectId());
      }
      // maybe the subjectId in the member changed, remove everything
      Member member2 = membersFlashCacheRetrieveById(member.getId(), null);
      if (member2 != null) {
        if (!StringUtils.equals(member.getSubjectSourceId(), member2.getSubjectSourceId()) && !StringUtils.equals(member.getSubjectSourceId(), member2.getSubjectSourceId())) {
          membersFlashCache.remove(membersFlashCacheMultikeyBySubjectId(member2.getSubjectSourceId(), member2.getSubjectId()));
        }
      }
      
      Member dbVersion = member.dbVersion();
      if (dbVersion != null && dbVersion != member) {
        membersCacheRemove(dbVersion);
      }
    }
  }

  /**
   * remove from cache
   * @param uuid
   */
  @Deprecated
  public void uuid2dtoCacheRemove(String uuid) {
    
    Member member = membersFlashCacheRetrieveById(uuid, null);
    membersCacheRemove(member);
  }
  
  /**
   * @since   @HEAD@
   */
  public void update(Member _m) 
    throws  GrouperDAOException {
    
    HibernateSession.byObjectStatic().update(_m);
  } 


  // PROTECTED CLASS METHODS //

  // @since   @HEAD@
  protected static void reset(HibernateSession hibernateSession) 
    throws  HibernateException
  {
    hibernateSession.byHql().createQuery("delete from Member as m where m.subjectSourceIdDb != 'g:isa'")
      .executeUpdate();
    
    membersFlashCache.clear();
  }

  /**
   * @deprecated use overload
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#findBySubject(java.lang.String)
   */
  @Deprecated
  public Member findBySubject(String subjectId) throws GrouperDAOException,
      MemberNotFoundException, MemberNotUniqueException {
    return findBySubject(subjectId, true);
  }

  /**
   * 
   */
  public Member findBySubject(String subjectId, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException, MemberNotUniqueException {
    List<Member> members = HibernateSession.byHqlStatic().createQuery(
        "from Member as m where m.subjectIdDb       = :sid")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindBySubjectId").setString("sid", subjectId)
        .list(Member.class);
    if (GrouperUtil.length(members) ==0) {
      if (exceptionIfNull) {
        throw new MemberNotFoundException("Cant find member with subjectId: '" + subjectId + "'");
      }
      return null;
    }
    if (GrouperUtil.length(members) > 1) {
      throw new MemberNotUniqueException("Subject id '" + subjectId + "' is not unique in the members table");
    }
    Member member = members.get(0);
    membersFlashCacheAddIfSupposedTo(member);
    return member;
  }

  /**
   * @deprecated use overload
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#findBySubject(java.lang.String, java.lang.String)
   */
  @Deprecated
  public Member findBySubject(String subjectId, String src) throws GrouperDAOException,
      MemberNotFoundException {
    return findBySubject(subjectId, src, true);
  } 

  /**
   * 
   */
  public Member findBySubject(String subjectId, String src, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException {
    
    Member member = membersFlashCacheRetrieveBySubjectId(src, subjectId, null);
    
    if (member == null) {
      member = HibernateSession.byHqlStatic().createQuery(
          "from Member as m where " + "     m.subjectIdDb       = :sid    "
              + "and  m.subjectSourceIdDb = :source ").setCacheable(true).setCacheRegion(
          KLASS + ".FindBySubjectIdSrc").setString("sid", subjectId).setString("source",
          src).uniqueResult(Member.class);
      membersFlashCacheAddIfSupposedTo(member);
    }
    if (member == null && exceptionIfNull) {
      throw new MemberNotFoundException("Cant find member by source '" + src + "' and subjectId '" + subjectId + "'");
    }
    return member;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#_internal_membersIntersection(java.lang.String, java.lang.String)
   */
  public Set<String> _internal_membersIntersection(String groupUuid1, String groupUuid2) {
    String groupSubjectSourceId = SubjectFinder.internal_getGSA().getId();

    Set<String> memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid " +
    		"from Member theMember, " +
        "MembershipEntry theMembership, MembershipEntry theMembership2 " +
        "where theMembership.ownerGroupId = :group1uuid " +
        "and theMembership2.ownerGroupId = :group2uuid " +
        "and theMember.uuid = theMembership.memberUuid " +
        "and theMember.uuid = theMembership2.memberUuid " +
        "and theMembership.fieldId = :fuuid " +
        "and theMembership2.fieldId = :fuuid " +
        "and theMembership.enabledDb = 'T' " +
        "and theMembership2.enabledDb = 'T' " +
        "and theMember.subjectSourceIdDb <> :groupSubjectSourceId ")
        .setString("group1uuid", groupUuid1)
        .setString("group2uuid", groupUuid2)
        .setString("fuuid", Group.getDefaultList().getUuid())
        .setString("groupSubjectSourceId", groupSubjectSourceId)
        .listSet(String.class);
    return memberUuids;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#_internal_membersUnion(java.lang.String, java.lang.String)
   */
  public Set<String> _internal_membersUnion(String groupUuid1, String groupUuid2) {
    String groupSubjectSourceId = SubjectFinder.internal_getGSA().getId();

    Set<String> memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid " +
        "from Member theMember, " +
        "MembershipEntry theMembership " +
        "where theMembership.ownerGroupId in (:group1uuid, " +
        " :group2uuid) " +
        "and theMember.uuid = theMembership.memberUuid " +
        "and theMembership.fieldId = :fuuid " +
        "and theMembership.enabledDb = 'T' " +
        "and theMember.subjectSourceIdDb <> :groupSubjectSourceId ")
        .setString("group1uuid", groupUuid1)
        .setString("group2uuid", groupUuid2)
        .setString("fuuid", Group.getDefaultList().getUuid())
        .setString("groupSubjectSourceId", groupSubjectSourceId)
        .listSet(String.class);
    return memberUuids;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#_internal_membersComplement(java.lang.String, java.lang.String)
   */
  public Set<String> _internal_membersComplement(String groupUuid1, String groupUuid2) {
    String groupSubjectSourceId = SubjectFinder.internal_getGSA().getId();

    Set<String> memberUuids = HibernateSession.byHqlStatic()
    .createQuery("select distinct theMember.uuid from Member theMember, " +
        "MembershipEntry theMembership " +
        "where theMembership.ownerGroupId = :group1uuid " +
        "and theMember.uuid = theMembership.memberUuid " +
        "and theMembership.fieldId = :fuuid " +
        "and theMembership.enabledDb = 'T' " +
        "and theMember.subjectSourceIdDb <> :groupSubjectSourceId " +
        "and not exists (select theMembership2.memberUuid from MembershipEntry theMembership2 " +
        "where theMembership2.memberUuid = theMember.uuid and theMembership2.fieldId = :fuuid " +
        "and theMembership2.ownerGroupId = :group2uuid and theMembership2.enabledDb = 'T') ")
        .setString("group1uuid", groupUuid1)
        .setString("group2uuid", groupUuid2)
        .setString("fuuid", Group.getDefaultList().getUuid())
        .setString("groupSubjectSourceId", groupSubjectSourceId)
        .listSet(String.class);
    return memberUuids;
  }

  /**
   * Retrieve Member objects based on a list of SubjectIds within a single SubjectSource
   *
   * @param subjectIds
   * @param subjectSourceId
   * @return
   */
  @Override
  public Set<Member> findBySubjectIds(
          Collection<String> subjectIds, String subjectSourceId) {
    Set<Member> result = new TreeSet<Member>();

    if (GrouperUtil.length(subjectIds) == 0) {
      return result;
    }

    List<String> subjectsThatWerentCached = new ArrayList<String>();
    for (String subjectId : subjectIds) {
      Member member = membersFlashCacheRetrieveBySubjectId(subjectSourceId, subjectId, null);
      if (member != null) {
        result.add(member);
      } else {
        subjectsThatWerentCached.add(subjectId);
      }
    }
    if (GrouperUtil.length(subjectsThatWerentCached) > 0) {
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjectsThatWerentCached, 1000);

      for (int i=0; i<numberOfBatches; i++) {
        List<String>  batchOfIds = GrouperUtil.batchList(subjectsThatWerentCached, 1000, i);

        ByCriteriaStatic byCriteriaStatic = HibernateSession.byCriteriaStatic();

        Criterion filter = HibUtils.buildInCriterion("subjectIdDb", batchOfIds, 1000);

        if (!StringUtils.isBlank(subjectSourceId)) {
	        List<Criterion> criterionList = new ArrayList<Criterion>();
	        
	        criterionList.add(filter);
	        criterionList.add(Restrictions.eq("subjectSourceIdDb", subjectSourceId));
	  
	        filter = HibUtils.listCrit(criterionList);
        }
        
        List<Member> membersFromDb = byCriteriaStatic.list(Member.class, filter);

        for (Member member : GrouperUtil.nonNull(membersFromDb)) {
          membersFlashCacheAddIfSupposedTo(member);
        }

        result.addAll(membersFromDb);
      }

    }

    return result;
  }

  /**
   * find members by subjects and create if not exist possibly
   * @param subjectsOrig
   * @param createIfNotExists
   * @return the members
   */
  public Set<Member> findBySubjects(
      Collection<Subject> subjectsOrig, boolean createIfNotExists) {
    
    Set<Member> result = new TreeSet<Member>();
    
    if (GrouperUtil.length(subjectsOrig) == 0) {
      return result;
    }
    
    List<Subject> subjectsNeedQuery = new ArrayList<Subject>();
    for (Subject subject : subjectsOrig) {
      Member member = membersFlashCacheRetrieveBySubjectId(subject.getSourceId(), subject.getId(), null);
      if (member != null) {
        result.add(member);
      } else {
        subjectsNeedQuery.add(subject);
      }
    }
    if (GrouperUtil.length(subjectsNeedQuery) > 0) {
      //lets do this in batches
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjectsNeedQuery, MEMBER_SUBJECT_BATCH_SIZE);
  
      List<Subject> subjectsList = subjectsNeedQuery instanceof List ? (List)subjectsNeedQuery : new ArrayList<Subject>(subjectsNeedQuery);
      
      for (int i=0;i<numberOfBatches;i++) {
  
        List<Subject> subjectBatch = GrouperUtil.batchList(subjectsList, MEMBER_SUBJECT_BATCH_SIZE, i);
  
  //      select distinct gm.* 
  //      from grouper_members gm
  //      where (gm.subject_id = '123' and gm.subject_source = 'jdbc') 
  //          or (gm.subject_id = '234' and gm.subject_source = 'jdbc' )
  
        //lets turn the subjects into subjectIds
        if (GrouperUtil.length(subjectBatch) == 0) {
          continue;
        }
  
        ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
        StringBuilder query = new StringBuilder("select distinct gm " +
            "from Member gm " +
            "where ");
        
        //add all the uuids
        query.append(HibUtils.convertToSubjectInClause(subjectBatch, byHqlStatic, "gm"));
        List<Member> currentMemberList = byHqlStatic.createQuery(query.toString())
          .list(Member.class);
        
        for (Member member : GrouperUtil.nonNull(currentMemberList)) {
          membersFlashCacheAddIfSupposedTo(member);
        }
        
        result.addAll(currentMemberList);
        
        if (createIfNotExists) {
          
          //see which ones we've got
          Set<MultiKey> subjectsRetrieved = new HashSet<MultiKey>();
          
          for (Member member : currentMemberList) {
            MultiKey multiKey = new MultiKey(member.getSubjectSourceId(), member.getSubjectId());
            subjectsRetrieved.add(multiKey);
          }
          
          //loop through what we were supposed to get
          for (Subject subject : subjectBatch) {
            MultiKey multiKey = new MultiKey(subject.getSourceId(), subject.getId());
            if (!subjectsRetrieved.contains(multiKey)) {
              //create and add to results
              Member member = MemberFinder.internal_createMember(subject, null);
              result.add(member);
              membersFlashCacheAddIfSupposedTo(member);
            }
          }
        }
      }
    }
    return result;

    
  }
  

  
  /**
   * convert a set of subjects to a set of members
   * @param grouperSession 
   * @param subjects to convert to members
   * @param group that subjects must be in
   * @param field that they must be in in the group (null will default to eh members list
   * @param membershipType that they must be in in the group or null for any
   * @return the members in the group
   * @see MemberDAO#findBySubjectsInGroup(GrouperSession, Set, Group, Field, MembershipType)
   */
  public Set<Member> findBySubjectsInGroup(GrouperSession grouperSession,
      Set<Subject> subjects, Group group, Field field, MembershipType membershipType) {
    if (field == null) {
      field = Group.getDefaultList();
    }

    Set<Member> result = new TreeSet<Member>();
    
    if (GrouperUtil.length(subjects) == 0) {
      return result;
    }
    
    //lets do this in batches
    List<Subject> subjectsList = GrouperUtil.listFromCollection(subjects);
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjectsList, MEMBER_SUBJECT_BATCH_SIZE);
    
    if (!StringUtils.equals("list", field.getTypeString())) {
      throw new RuntimeException("Can only call this method with a list field: " + field);
    }
    
    //check security once
    if (!PrivilegeHelper.canViewMembers(grouperSession, group, field)) {
      throw new InsufficientPrivilegeException("subject " + grouperSession.getSubject() + " cannot read group: " + group);
    }

    for (int i=0;i<numberOfBatches;i++) {

      List<Subject> subjectBatch = GrouperUtil.batchList(subjectsList, MEMBER_SUBJECT_BATCH_SIZE, i);

//      select distinct gm.* 
//      from grouper_members gm, grouper_memberships gms
//      where gm.id = gms.member_id
//      and gms.field_id = 'abc' and gms.owner_id = '123'
//      and ((gm.subject_id = '123' and gm.subject_source = 'jdbc') 
//          or (gm.subject_id = '234' and gm.subject_source = 'jdbc' ))
//      and mship_type = 'immediate'

      //lets turn the subjects into subjectIds
      if (GrouperUtil.length(subjectBatch) == 0) {
        continue;
      }

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      StringBuilder query = new StringBuilder("select distinct gm " +
          "from Member gm, MembershipEntry gms " +
          "where gm.uuid = gms.memberUuid " +
          "and gms.enabledDb = 'T' " +
          "and gms.fieldId = :fieldId " +
          "and gms.ownerGroupId = :ownerId " +
          (membershipType == null ? "" : (" and gms.type " + membershipType.queryClause() + " ")) +
          "and ");
      
      //add all the uuids
      byHqlStatic.setString("fieldId", field.getUuid());
      byHqlStatic.setString("ownerId", group.getUuid());
      query.append(HibUtils.convertToSubjectInClause(subjectBatch, byHqlStatic, "gm"));
      List<Member> currentMemberList = byHqlStatic.createQuery(query.toString())
        .list(Member.class);
      result.addAll(currentMemberList);
      for (Member member : GrouperUtil.nonNull(currentMemberList)) {
        membersFlashCacheAddIfSupposedTo(member);
      }

    }
    return result;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#findByUuidOrSubject(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public Member findByUuidOrSubject(String uuid, String subjectId, String source,
      boolean exceptionIfNull) {
    Member member = null;
    if (member == null && !StringUtils.isBlank(uuid)) {
      member = membersFlashCacheRetrieveById(uuid, null);
    }
    if (member == null && !StringUtils.isBlank(source) && !StringUtils.isBlank(subjectId)) {
      member = membersFlashCacheRetrieveBySubjectId(source, subjectId, null);
    }
    if (member == null && !StringUtils.isBlank(uuid) && !StringUtils.isBlank(source) && !StringUtils.isBlank(subjectId)) {
      member = HibernateSession.byHqlStatic().createQuery(
        "from Member as m where (m.subjectIdDb = :sid    "
            + "and  m.subjectSourceIdDb = :source) or m.uuid = :uuid ")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindBySubjectIdSrc")
        .setString("sid", subjectId)
        .setString("source", source)
        .setString("uuid", uuid)
        .uniqueResult(Member.class);
      membersFlashCacheAddIfSupposedTo(member);
    }
    if (member == null && exceptionIfNull) {
      throw new MemberNotFoundException("Cant find member by id '" + uuid + "', source '" + source + "' and subjectId '" + subjectId + "'");
    }
    return member;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#saveUpdateProperties(edu.internet2.middleware.grouper.Member)
   */
  public void saveUpdateProperties(Member member) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update Member " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId " +
        "where uuid = :theUuid")
        .setLong("theHibernateVersionNumber", member.getHibernateVersionNumber())
        .setString("theContextId", member.getContextId())
        .setString("theUuid", member.getUuid())
        .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#findAllUsed(edu.internet2.middleware.subject.Source)
   */
  public Set<Member> findAllUsed(Source source) throws GrouperDAOException {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    StringBuilder query = new StringBuilder("select distinct theMember from Member as theMember where ");
    if (source != null) {
      query.append(" theMember.subjectSourceIdDb=:sourceId and ( " );
      byHqlStatic.setString("sourceId", source.getId());
    }

    //memberships or attributes
    query.append(" exists (select 1 from ImmediateMembershipEntry as theMembership where theMembership.memberUuid = theMember.uuid) " +
    		" or exists (select 1 from AttributeAssign as theAttributeAssign where theAttributeAssign.ownerMemberId = theMember.uuid"
    		+ " and theAttributeAssign.attributeDefNameId != :theAttributeDefNameId ) ");
    
    // dont worry about the unresolvable attributes
    byHqlStatic.setString("theAttributeDefNameId", UsduAttributeNames.retrieveAttributeDefNameBase().getId());
    
    //even out parens
    if (source != null) {
      query.append(" ) ");
    }
    		
    byHqlStatic.createQuery(query.toString());
    return byHqlStatic.listSet(Member.class);  
  }

  @Override
  public Set<String> findAllMemberIdsForUnresolvableCheck() throws GrouperDAOException {
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    
    StringBuilder query = new StringBuilder("select distinct theMember.uuid from Member as theMember where ");
   
    query.append(" ((theMember.subjectSourceIdDb != 'g:gsa' and theMember.subjectSourceIdDb != 'g:isa') or " );
    query.append(" (theMember.subjectSourceIdDb = 'g:gsa' and not exists (select 1 from Group g where g.uuid=theMember.subjectIdDb))) " );

    //memberships or attributes
    query.append(" and (exists (select 1 from ImmediateMembershipEntry as theMembership where theMembership.memberUuid = theMember.uuid) " +
        " or exists (select 1 from AttributeAssign as theAttributeAssign where theAttributeAssign.ownerMemberId = theMember.uuid"
        + " and theAttributeAssign.attributeDefNameId != :theAttributeDefNameId )) ");
    
    // dont worry about the unresolvable attributes
    byHqlStatic.setString("theAttributeDefNameId", UsduAttributeNames.retrieveAttributeDefNameBase().getId());
        
    byHqlStatic.createQuery(query.toString());
    return byHqlStatic.listSet(String.class);  
  }

  /**
   * @see MemberDAO#findByIds(Collection, QueryOptions)
   */
  @Override
  public Set<Member> findByIds(Collection<String> idsOrig,
      QueryOptions queryOptions) {

    Set<Member> members = new HashSet<Member>();
    
    if (GrouperUtil.length(idsOrig) == 0) {
      return members;
    }
    
    List<String> idsNeedQuery = new ArrayList<String>();

    for (String id : idsOrig) {
      Member member = membersFlashCacheRetrieveById(id, queryOptions);
      if (member != null) {
        members.add(member);
      } else {
        idsNeedQuery.add(id);
      }
    }

    if (GrouperUtil.length(idsNeedQuery) > 0) {
      
      int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsNeedQuery, 180);
      
      List<String> idsList = GrouperUtil.listFromCollection(idsNeedQuery);
      
      for (int i=0;i<numberOfBatches;i++) {
        
        List<String> uuidsBatch = GrouperUtil.batchList(idsList, 180, i);
        
        ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
  
        StringBuilder sql = new StringBuilder("select distinct theMember from Member as theMember ");
        
        sql.append(" where ");
        
        sql.append(" theMember.id in ( ");
        
        sql.append(HibUtils.convertToInClause(uuidsBatch, byHqlStatic)).append(" ) ");
        
        byHqlStatic
          .createQuery(sql.toString())
          .setCacheable(true)
          .options(queryOptions)
          .setCacheRegion(KLASS + ".FindByUuidsSecure");
        
        Set<Member> membersBatch = byHqlStatic.listSet(Member.class);
        
        members.addAll(GrouperUtil.nonNull(membersBatch));
        
        for (Member member : membersBatch) {
          membersFlashCacheAddIfSupposedTo(member);
        }
      }
    }
    
    return members;
  }

  /**
   * @see MemberDAO#getAllMembersSecure(GrouperSession, QueryOptions, String, Object, Set, Boolean, String, Object, Set)
   */
  @Override
  public Set<Member> getAllMembersSecure(GrouperSession grouperSession,
      QueryOptions queryOptions,
      String idOfAttributeDefName,
      Object attributeValue, Set<Object> attributeValuesOnAssignment, Boolean attributeCheckReadOnAttributeDef,
      String idOfAttributeDefName2,
      Object attributeValue2, Set<Object> attributeValuesOnAssignment2) {
    return findAllMembersSecureHelper(grouperSession, queryOptions, 
        idOfAttributeDefName, 
        attributeValue, attributeValuesOnAssignment, attributeCheckReadOnAttributeDef, idOfAttributeDefName2,attributeValue2, attributeValuesOnAssignment2);
  }

  /**
   * @param grouperSession 
   * @param subject 
   * @param queryOptions 
   * @param idOfAttributeDefName 
   * @param attributeValue 
   * @param attributeValuesOnAssignment
   * @param attributeCheckReadOnAttributeDef
   * @param idOfAttributeDefName2
   * @param attributeValue2
   * @param attributeValuesOnAssignment2 
   * @return members
   * 
   */
  private Set<Member> findAllMembersSecureHelper(
      GrouperSession grouperSession, 
      QueryOptions queryOptions, 
      final String idOfAttributeDefName, Object attributeValue, Set<Object> attributeValuesOnAssignment,
      Boolean attributeCheckReadOnAttributeDef, final String idOfAttributeDefName2, Object attributeValue2, Set<Object> attributeValuesOnAssignment2) {

    if ((attributeValue != null || GrouperUtil.length(attributeValuesOnAssignment) > 0) && StringUtils.isBlank(idOfAttributeDefName)) {
      throw new RuntimeException("If you are searching by attributeValue then you must specify an attribute definition name");
    }
    
    if (attributeValue != null && GrouperUtil.length(attributeValuesOnAssignment) > 0) {
      throw new RuntimeException("Cant send in attributeValue and attributeValuesOnAssignment"); 
    }
  
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("id");
    }
  
    Set<Member> overallResults = new LinkedHashSet<Member>();
    
    
    StringBuilder sql = new StringBuilder(
        "select distinct m from Member m ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      
    StringBuilder whereClause = new StringBuilder();
  
    if (!StringUtils.isBlank(idOfAttributeDefName)) {

      if (whereClause.length() > 0) {
        
        whereClause.append(" and ");
        
      }

      //default to true
      attributeCheckReadOnAttributeDef = GrouperUtil.defaultIfNull(attributeCheckReadOnAttributeDef, true);

      //make sure user can READ the attribute
      AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder().addIdOfAttributeDefName(idOfAttributeDefName);

      if (attributeCheckReadOnAttributeDef) {
        attributeDefNameFinder.addPrivilege(AttributeDefPrivilege.ATTR_READ);
      }

      AttributeDefName attributeDefName = attributeDefNameFinder.findAttributeName();

      //cant read the attribute????
      if (attributeDefName == null) {
        return new HashSet<Member>();
      }

      AttributeDef attributeDef = attributeDefName.getAttributeDef();
      
      if (GrouperUtil.length(attributeValuesOnAssignment) > 0) {

        whereClause.append(" exists ( select aav ");
        
        whereClause.append(" from AttributeAssign aa, AttributeAssign aaOnAssign, AttributeAssignValue aav ");
        
        whereClause.append(" where m.uuid = aa.ownerMemberId ");
        whereClause.append(" and aa.id = aaOnAssign.ownerAttributeAssignId ");
        
        whereClause.append(" and aaOnAssign.attributeDefNameId = :idOfAttributeDefName ");
        byHqlStatic.setString("idOfAttributeDefName", idOfAttributeDefName);
        whereClause.append(" and aa.enabledDb = 'T' ");

        AttributeDefValueType attributeDefValueType = attributeDef.getValueType();

        Hib3AttributeAssignDAO.queryByValuesAddTablesWhereClause(byHqlStatic, null, whereClause, attributeDefValueType, attributeValuesOnAssignment, "aaOnAssign");
        
        whereClause.append(" ) ");
        
            
      } else {
        
        whereClause.append(" exists ( select ");
        
        whereClause.append(attributeValue == null ? "aa" : "aav");
        
        whereClause.append(" from AttributeAssign aa ");

        if (attributeValue != null) {
          whereClause.append(", AttributeAssignValue aav ");
        }
        
        whereClause.append(" where m.uuid = aa.ownerMemberId ");
        whereClause.append(" and aa.attributeDefNameId = :idOfAttributeDefName ");
        byHqlStatic.setString("idOfAttributeDefName", idOfAttributeDefName);
        whereClause.append(" and aa.enabledDb = 'T' ");

        if (attributeValue != null) {

          AttributeDefValueType attributeDefValueType = attributeDef.getValueType();

          Hib3AttributeAssignDAO.queryByValueAddTablesWhereClause(byHqlStatic, null, whereClause, attributeDefValueType, attributeValue);
          
        }
        
        whereClause.append(" ) ");
          
      }
            
    }
    
    if (!StringUtils.isBlank(idOfAttributeDefName2)) {

      if (whereClause.length() > 0) {
        
        whereClause.append(" and ");
        
      }

      //default to true
      attributeCheckReadOnAttributeDef = GrouperUtil.defaultIfNull(attributeCheckReadOnAttributeDef, true);

      //make sure user can READ the attribute
      AttributeDefNameFinder attributeDefNameFinder = new AttributeDefNameFinder().addIdOfAttributeDefName(idOfAttributeDefName2);

      if (attributeCheckReadOnAttributeDef) {
        attributeDefNameFinder.addPrivilege(AttributeDefPrivilege.ATTR_READ);
      }

      AttributeDefName attributeDefName = attributeDefNameFinder.findAttributeName();

      //cant read the attribute????
      if (attributeDefName == null) {
        return new HashSet<Member>();
      }

      AttributeDef attributeDef = attributeDefName.getAttributeDef();
      
      if (GrouperUtil.length(attributeValuesOnAssignment2) > 0) {

        whereClause.append(" exists ( select aav ");
        
        whereClause.append(" from AttributeAssign aa, AttributeAssign aaOnAssign, AttributeAssignValue aav ");
        
        whereClause.append(" where m.uuid = aa.ownerMemberId ");
        whereClause.append(" and aa.id = aaOnAssign.ownerAttributeAssignId ");
        
        whereClause.append(" and aaOnAssign.attributeDefNameId = :idOfAttributeDefName2 ");
        byHqlStatic.setString("idOfAttributeDefName2", idOfAttributeDefName2);
        whereClause.append(" and aa.enabledDb = 'T' ");

        AttributeDefValueType attributeDefValueType = attributeDef.getValueType();

        Hib3AttributeAssignDAO.queryByValuesAddTablesWhereClause(byHqlStatic, null, whereClause, attributeDefValueType, attributeValuesOnAssignment2, "aaOnAssign");
        
        whereClause.append(" ) ");
        
        
      } else {
        
        whereClause.append(" exists ( select ");
        
        whereClause.append(attributeValue2 == null ? "aa" : "aav");
        
        whereClause.append(" from AttributeAssign aa ");

        if (attributeValue2 != null) {
          whereClause.append(", AttributeAssignValue aav ");
        }
        
        whereClause.append(" where m.uuid = aa.ownerMemberId ");
        whereClause.append(" and aa.attributeDefNameId = :idOfAttributeDefName2 ");
        byHqlStatic.setString("idOfAttributeDefName2", idOfAttributeDefName2);
        whereClause.append(" and aa.enabledDb = 'T' ");

        if (attributeValue2 != null) {

          AttributeDefValueType attributeDefValueType = attributeDef.getValueType();

          Hib3AttributeAssignDAO.queryByValueAddTablesWhereClause(byHqlStatic, null, whereClause, attributeDefValueType, attributeValue2);
          
        }
        
        whereClause.append(" ) ");
          
      }

    }
  
    
    if (sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    sql.append(whereClause);

    if (queryOptions != null) {
      massageMemberSortFields(queryOptions.getQuerySort());
    }

    String sqlString = sql.toString();
    
    if (!sqlString.contains(GrouperAccessAdapter.HQL_FILTER_NO_RESULTS_INDICATOR)) {
      Set<Member> tempGroups = byHqlStatic.createQuery(sqlString)
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetAllMembersSecure")
        .options(queryOptions)
        .listSet(Member.class);
      
      overallResults.addAll(GrouperUtil.nonNull(tempGroups));
    }
    for (Member member : overallResults) {
      membersFlashCacheAddIfSupposedTo(member);
    }
    return overallResults;
  
  }

  /**
   * see if this is cacheable
   * @param sourceId
   * @param subjectId
   * @param queryOptions 
   * @return if cacheable
   */
  private static boolean membersFlashCacheableBySubjectId(String sourceId, String subjectId, QueryOptions queryOptions) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_MEMBERS_IN_FINDER, true)) {
      return false;
    }
  
    if (StringUtils.isBlank(sourceId) || StringUtils.isBlank(subjectId)) {
      return false;
    }
    // these are so common just assume cached
    if (("g:isa".equals(sourceId) && "GrouperSystem".equals(subjectId))
      || ("g:isa".equals(sourceId) && "GrouperAll".equals(subjectId))) {
      return true;
    }
    if (!HibUtils.secondLevelCaching(true, queryOptions)) {
      return false;
    }
    
    return true;

  }

  /**
   * see if this is cacheable
   * @param id
   * @param queryOptions 
   * @return if cacheable
   */
  private static boolean membersFlashCacheableById(Object id, QueryOptions queryOptions) {
  
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_MEMBERS_IN_FINDER, true)) {
      return false;
    }
  
    if (id == null || ((id instanceof String) && StringUtils.isBlank((String)id))) {
      return false;
    }
  
    if (!HibUtils.secondLevelCaching(true, queryOptions)) {
      return false;
    }
    
    return true;
  }

  /**
   * add group to cache if not null
   * @param member
   */
  private static void membersFlashCacheAddIfSupposedTo(Member member) {
    if (member == null || !GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_MEMBERS_IN_FINDER, true)) {
      return;
    }

    MultiKey multiKey = membersFlashCacheMultikeyById(member.getId());
    if (multiKey != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("adding member to flash cache by id: " + member.getId());
      }

      membersFlashCache.put(multiKey, member);
    }
    
    multiKey = membersFlashCacheMultikeyBySubjectId(member.getSubjectSourceId(), member.getSubjectId());
    if (multiKey != null) {
      if (LOG.isDebugEnabled()) {
        LOG.debug("adding member to flash cache by source and subjectId: " + member.getSubjectSourceId() + ", " + member.getSubjectId());
      }
      membersFlashCache.put(multiKey, member);
    }
    
  }

  /**
   * multikey
   * @param id
   * @return if cacheable
   */
  private static MultiKey membersFlashCacheMultikeyById(Object id) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_MEMBERS_IN_FINDER, true)) {
      return null;
    }
  
    //return new MultiKey("sourceIdSubjectId", sourceId, subjectId);
    return new MultiKey("uuid", id);
  }

  /**
   * multikey
   * @param sourceId
   * @param subjectId
   * @return if cacheable
   */
  private static MultiKey membersFlashCacheMultikeyBySubjectId(String sourceId, String subjectId) {
    
    if (!GrouperConfig.retrieveConfig().propertyValueBoolean(GROUPER_FLASHCACHE_MEMBERS_IN_FINDER, true)) {
      return null;
    }
  
    return new MultiKey("sourceIdSubjectId", sourceId, subjectId);
  }

  /**
   * get a member fom flash cache
   * @param id
   * @param queryOptions
   * @return the stem or null
   */
  private static Member membersFlashCacheRetrieveById(Object id, QueryOptions queryOptions) {
    if (membersFlashCacheableById(id, queryOptions)) {
      MultiKey membersFlashKey = membersFlashCacheMultikeyById(id);
      //see if its already in the cache
      Member member = membersFlashCache.get(membersFlashKey);
      if (member != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from member flash cache by id: " + member.getName());
        }
        return member;
      }
    }
    return null;
  }

  /**
   * get a member fom flash cache
   * @param sourceId
   * @param subjectId
   * @param queryOptions
   * @return the stem or null
   */
  private static Member membersFlashCacheRetrieveBySubjectId(String sourceId, String subjectId, QueryOptions queryOptions) {
    if (membersFlashCacheableBySubjectId(sourceId, subjectId, queryOptions)) {
      MultiKey membersFlashKey = membersFlashCacheMultikeyBySubjectId(sourceId, subjectId);
      //see if its already in the cache
      Member member = membersFlashCache.get(membersFlashKey);
      if (member != null) {
        if (LOG.isDebugEnabled()) {
          LOG.debug("retrieving from member flash cache by subjectId: " + sourceId + ", " + subjectId);
        }
        return member;
      }
    }
    return null;
  }

  public Set<Member> getUnresolvableMembers(QueryOptions queryOptions, Boolean deleted) {
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("id");
    }

    StringBuilder sql = new StringBuilder("select m from Member as m where m.subjectResolutionResolvableDb='F' ");

    if (deleted != null && deleted) {
      sql.append(" and m.subjectResolutionDeletedDb='T'");
    }
    
    if (deleted != null && !deleted) {
      sql.append(" and m.subjectResolutionDeletedDb='F'");
    }

    if (queryOptions != null) {
      massageMemberSortFields(queryOptions.getQuerySort());
    }
    
    return HibernateSession.byHqlStatic().createQuery(sql.toString())
        .setCacheable(false)
        .setCacheRegion(KLASS + ".GetUnresolvableMembers")
        .options(queryOptions)
        .listSet(Member.class);
  }
} 

