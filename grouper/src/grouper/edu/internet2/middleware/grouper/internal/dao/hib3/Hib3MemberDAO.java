/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.hibernate.HibernateException;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberNotFoundException;
import edu.internet2.middleware.grouper.exception.MemberNotUniqueException;
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
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Source;
import edu.internet2.middleware.subject.Subject;


/**
 * Basic Hibernate <code>Member</code> DAO interface.
 * @author  blair christensen.
 * @version $Id: Hib3MemberDAO.java,v 1.20 2009-12-28 06:08:37 mchyzer Exp $
 * @since   @HEAD@
 */
public class Hib3MemberDAO extends Hib3DAO implements MemberDAO {

  private static        GrouperCache<String, Boolean>    existsCache     =  null;
  
  
    

  
  
  private static final  String                      KLASS           = Hib3MemberDAO.class.getName();

  private static        GrouperCache<String, Member>  uuid2dtoCache   = null;






  /**
   * number of subjects to put in member query
   */
  public static int MEMBER_SUBJECT_BATCH_SIZE = 80;
  

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
    if ( getExistsCache().containsKey(uuid) ) {
      return getExistsCache().get(uuid).booleanValue();
    }
    Object id = null;
    try {
      id = HibernateSession.byHqlStatic()
        .createQuery("select m.id from Member as m where m.uuid = :uuid")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".Exists")
        .setString("uuid", uuid).uniqueResult(Object.class);
    } catch (GrouperDAOException gde) {
      Throwable throwable = gde.getCause();
      //CH 20080218 this was legacy error handling
      if (throwable instanceof HibernateException) {
        LOG.fatal( throwable.getMessage() );
      }
      throw gde;
    }
    boolean rv  = false;
    if ( id != null ) {
      rv = true; 
    }
    getExistsCache().put(uuid, rv);
    return rv;
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
   * if there are sort fields, go through them, and replace name with m.subjectIdDb, etc,
   * sort_string0 for m.sortString0, etc
   * @param querySort
   */
  public static void massageMemberSortFields(QuerySort querySort) {
    if (querySort == null) {
      return;
    }

    for (QuerySortField querySortField : GrouperUtil.nonNull(querySort.getQuerySortFields())) {
      if (StringUtils.equalsIgnoreCase("uuid", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("id", querySortField.getColumn())) {
        querySortField.setColumn("m.uuid");
      }
      if (StringUtils.equalsIgnoreCase("subjectIdDb", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("subject_id", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("subjectId", querySortField.getColumn())) {
        querySortField.setColumn("m.subjectIdDb");
      }
      if (StringUtils.equalsIgnoreCase("subjectSourceIdDb", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("subject_source", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sourceId", querySortField.getColumn())) {
        querySortField.setColumn("m.subjectSourceIdDb");
      }
      if (StringUtils.equalsIgnoreCase("subjectTypeId", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("subject_type", querySortField.getColumn())) {
        querySortField.setColumn("m.subjectTypeId");
      }
      if (StringUtils.equalsIgnoreCase("sortString0", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string0", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(0).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn("m.sortString0");
      }
      if (StringUtils.equalsIgnoreCase("sortString1", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string1", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(1).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn("m.sortString1");
      }
      if (StringUtils.equalsIgnoreCase("sortString2", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string2", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(2).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn("m.sortString2");
      }
      if (StringUtils.equalsIgnoreCase("sortString3", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string3", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(3).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn("m.sortString3");
      }
      if (StringUtils.equalsIgnoreCase("sortString4", querySortField.getColumn())
          || StringUtils.equalsIgnoreCase("sort_string4", querySortField.getColumn())) {
        if (!SortStringEnum.newInstance(4).hasAccess()) {
          throw new RuntimeException("Not allowed to access " + querySortField.getColumn());
        }
        querySortField.setColumn("m.sortString4");
      }
      if (StringUtils.equalsIgnoreCase("name", querySortField.getColumn())) {
        querySortField.setColumn("m.name");
      }
      if (StringUtils.equalsIgnoreCase("description", querySortField.getColumn())) {
        querySortField.setColumn("m.description");
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
    Member member = null;
    
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
      
    if (member == null) {
      throw new MemberNotFoundException();
    }
    getUuid2dtoCache().put( member.getUuid(), member );
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
   * 
   */
  public Member findByUuid(String uuid, boolean exceptionIfNull)
      throws GrouperDAOException, MemberNotFoundException {
    Member memberDto = null;
    if (!StringUtils.isBlank(uuid)) {
      if ( getUuid2dtoCache().containsKey(uuid) ) {
        return getUuid2dtoCache().get(uuid);
      }
      
      try {
        memberDto = HibernateSession.byHqlStatic()
        .createQuery("from Member as m where m.uuid = :uuid")
        .setCacheable(false) // but i probably should - or at least permit it
        //.setCacheRegion(KLASS + ".FindByUuid")
        .setString("uuid", uuid).uniqueResult(Member.class);
      } catch (GrouperDAOException gde) {
        Throwable throwable = gde.getCause();
        //CH 20080218 this was legacy error handling
        if (throwable instanceof HibernateException) {
          LOG.fatal( throwable.getMessage() );
        }
        throw gde;
      }
    }
    if (memberDto == null && exceptionIfNull) {
      throw new MemberNotFoundException();
    }
    if (memberDto != null && uuid != null) {
      getUuid2dtoCache().put(uuid, memberDto);
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
  public void existsCachePut(String uuid, boolean exists) {
	  getExistsCache().put( uuid, exists );
  }
  
  /**
   * remove from cache
   * @param uuid
   */
  public void uuid2dtoCacheRemove(String uuid) {
	  getUuid2dtoCache().remove(uuid);
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
    
    getExistsCache().clear();
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
    getUuid2dtoCache().put(member.getUuid(), member);
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
    Member member = HibernateSession.byHqlStatic().createQuery(
        "from Member as m where " + "     m.subjectIdDb       = :sid    "
            + "and  m.subjectSourceIdDb = :source ").setCacheable(true).setCacheRegion(
        KLASS + ".FindBySubjectIdSrc").setString("sid", subjectId).setString("source",
        src).uniqueResult(Member.class);
    if (member == null && exceptionIfNull) {
      throw new MemberNotFoundException();
    }
    if (member != null) {
      getUuid2dtoCache().put(member.getUuid(), member);
    }
    return member;
  }

  
  private static GrouperCache<String, Boolean> getExistsCache() {
	  if(existsCache==null) {
		  existsCache=new GrouperCache<String, Boolean>("edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO.exists",
	          1000, false, 30, 120, false); 
	  }
	  return existsCache;
  }
  
  private static GrouperCache<String, Member> getUuid2dtoCache() {
	  if(uuid2dtoCache==null) {
		  uuid2dtoCache=new GrouperCache<String, Member>("edu.internet2.middleware.grouper.internal.dao.hib3.Hib3MemberDAO.uuid2dtoCache",
	          1000, false, 30, 120, false); 
	  }
	  return uuid2dtoCache;
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
   * find members by subjects and create if not exist possibly
   * @param subjects
   * @param createIfNotExists
   * @return the members
   */
  public Set<Member> findBySubjects(
      Collection<Subject> subjects, boolean createIfNotExists) {
    
    Set<Member> result = new TreeSet<Member>();
    
    if (GrouperUtil.length(subjects) == 0) {
      return result;
    }
    
    //lets do this in batches
    int numberOfBatches = GrouperUtil.batchNumberOfBatches(subjects, MEMBER_SUBJECT_BATCH_SIZE);

    List<Subject> subjectsList = subjects instanceof List ? (List)subjects : new ArrayList<Subject>(subjects);
    
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
    }
    return result;

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.MemberDAO#findByUuidOrSubject(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public Member findByUuidOrSubject(String uuid, String subjectId, String source,
      boolean exceptionIfNull) {
    Member member = HibernateSession.byHqlStatic().createQuery(
        "from Member as m where (m.subjectIdDb = :sid    "
            + "and  m.subjectSourceIdDb = :source) or m.uuid = :uuid ")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindBySubjectIdSrc")
        .setString("sid", subjectId)
        .setString("source", source)
        .setString("uuid", uuid)
        .uniqueResult(Member.class);
    if (member == null && exceptionIfNull) {
      throw new MemberNotFoundException();
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
    		" or exists (select 1 from AttributeAssign as theAttributeAssign where theAttributeAssign.ownerMemberId = theMember.uuid) ");
    
    //even out parens
    if (source != null) {
      query.append(" ) ");
    }
    		
    byHqlStatic.createQuery(query.toString());
    return byHqlStatic.listSet(Member.class);  
  }

} 

