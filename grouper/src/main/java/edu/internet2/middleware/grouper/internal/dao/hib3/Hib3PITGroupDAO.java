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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITGroupDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySortField;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet;
import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITMember;
import edu.internet2.middleware.grouper.pit.PITMembership;
import edu.internet2.middleware.grouper.pit.PITRoleSet;
import edu.internet2.middleware.grouper.pit.PITStem;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITGroupDAO extends Hib3DAO implements PITGroupDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITGroupDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITGroup)
   */
  public void saveOrUpdate(PITGroup pitGroup) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitGroup);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITGroup> pitGroups) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitGroups);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#delete(edu.internet2.middleware.grouper.pit.PITGroup)
   */
  public void delete(PITGroup pitGroup) {
    HibernateSession.byObjectStatic().delete(pitGroup);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITGroup where sourceId not in (select g.uuid from Group as g)").executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITGroup findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    return findBySourceIdActive(id, false, exceptionIfNotFound);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findBySourceIdActive(java.lang.String, boolean, boolean)
   */
  public PITGroup findBySourceIdActive(String id, boolean createIfNotFound, boolean exceptionIfNotFound) {
    PITGroup pitGroup = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroup from PITGroup as pitGroup where pitGroup.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITGroup.class);

    if (pitGroup == null && createIfNotFound) {
      Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(id, false);

      if (group != null) {
        String contextId = null;
        if (!GrouperUtil.isEmpty(group.getContextId())) {
          contextId = group.getContextId();
        }
        
        PITStem pitStem = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(group.getParentUuid(), true, false);
        
        if (pitStem != null) {
          pitGroup = new PITGroup();
          pitGroup.setId(GrouperUuid.getUuid());
          pitGroup.setSourceId(id);
          pitGroup.setNameDb(group.getName());  
          pitGroup.setStemId(pitStem.getId());        
          pitGroup.setContextId(contextId);
          pitGroup.setActiveDb("T");
          pitGroup.setStartTimeDb(System.currentTimeMillis() * 1000);
          pitGroup.setSourceInternalId(group.getInternalId());
          
          pitGroup.saveOrUpdate();
        }
      }
    }
    
    if (pitGroup == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITGroup with sourceId=" + id + " not found");
    }
    
    return pitGroup;
  }
  
  public Set<PITGroup> findBySourceIdsActive(Collection<String> ids) {
    int idsSize = GrouperUtil.length(ids);

    Set<PITGroup> results = new HashSet<PITGroup>();

    if (idsSize == 0) {
      return results;
    }

    List<String> idsList = new ArrayList<String>(ids);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsSize, 100);
   
    //if there are more than 100, batch these up and return them
    for (int i=0;i<numberOfBatches; i++) {

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      byHqlStatic.setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive");

      StringBuilder sql = new StringBuilder("select pitGroup from PITGroup as pitGroup where ");

      List<String> currentBatch = GrouperUtil.batchList(idsList, 100, i);

      sql.append(" pitGroup.sourceId in (");
      sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      sql.append(") and activeDb = 'T'");
   
      Set<PITGroup> localResult = byHqlStatic.createQuery(sql.toString()).listSet(PITGroup.class);
      results.addAll(localResult);
    }

    return results;
  }
  
  public Map<String, PITGroup> findByIds(Collection<String> ids) {
    int idsSize = GrouperUtil.length(ids);

    Map<String, PITGroup> results = new LinkedHashMap<String, PITGroup>();

    if (idsSize == 0) {
      return results;
    }

    List<String> idsList = new ArrayList<String>(ids);

    int batchSize = GrouperConfig.getHibernatePropertyInt("hibernate.jdbc.batch_size", 200);

    int numberOfBatches = GrouperUtil.batchNumberOfBatches(idsSize, batchSize);
   
    for (int i=0;i<numberOfBatches; i++) {

      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      byHqlStatic.setCacheable(true).setCacheRegion(KLASS + ".FindByIds");

      StringBuilder sql = new StringBuilder("select pitGroup from PITGroup as pitGroup where ");

      List<String> currentBatch = GrouperUtil.batchList(idsList, batchSize, i);

      sql.append(" pitGroup.id in (");
      sql.append(HibUtils.convertToInClause(currentBatch, byHqlStatic));
      sql.append(")");
   
      Set<PITGroup> localResult = byHqlStatic.createQuery(sql.toString()).listSet(PITGroup.class);
      for (PITGroup pitGroup : localResult) {
        results.put(pitGroup.getId(), pitGroup);
      }
    }

    return results;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITGroup findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITGroup pitGroup = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroup from PITGroup as pitGroup where pitGroup.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITGroup.class);
    
    if (pitGroup == null && exceptionIfNotFound) {
      throw new RuntimeException("PITGroup with sourceId=" + id + " not found");
    }
    
    return pitGroup;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITGroup> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITGroup> pitGroups = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroup from PITGroup as pitGroup where pitGroup.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITGroup.class);
    
    if (pitGroups.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITGroup with sourceId=" + id + " not found");
    }
    
    return pitGroups;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findById(java.lang.String, boolean)
   */
  public PITGroup findById(String id, boolean exceptionIfNotFound) {
    PITGroup pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITGroup as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITGroup.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITGroup with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findByName(java.lang.String)
   */
  public Set<PITGroup> findByName(String groupName, boolean orderByStartTime) {
    String sql = "select pitGroup from PITGroup as pitGroup where pitGroup.nameDb = :name";
    
    if (orderByStartTime) {
      sql += " order by startTimeDb";
    }
    
    Set<PITGroup> pitGroups = HibernateSession
      .byHqlStatic()
      .createQuery(sql)
      .setCacheable(false).setCacheRegion(KLASS + ".FindByName")
      .setString("name", groupName)
      .listSet(PITGroup.class);
    
    return pitGroups;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public long deleteInactiveRecords(Timestamp time) {
    
    return HibernateSession.byHqlStatic().createQuery(
        "select id from PITGroup where endTimeDb is not null and endTimeDb < :time").setLong("time", time.getTime() * 1000)
        .deleteInBatches(String.class, "PITGroup", "id");

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#getAllGroupsMembershipSecure(java.lang.String, java.lang.String, java.lang.String, edu.internet2.middleware.grouper.pit.PITStem, edu.internet2.middleware.grouper.Stem.Scope, java.sql.Timestamp, java.sql.Timestamp, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<PITGroup> getAllGroupsMembershipSecure(String pitMemberId, String pitFieldId, 
      String scope, PITStem pitStem, Scope stemScope, Timestamp pointInTimeFrom, Timestamp pointInTimeTo, QueryOptions queryOptions) {

    if ((pitStem == null) != (stemScope == null)) {
      throw new RuntimeException("If pitStem is set, then stem scope must be set.  If pitStem isnt set, then stem scope must not be set.");
    }

    boolean hasScope = StringUtils.isNotBlank(scope);
    
    if (queryOptions == null) {
      queryOptions = new QueryOptions();
    }
    
    if (queryOptions.getQuerySort() == null) {
      queryOptions.sortAsc("thePITGroup.nameDb");
    }
    
    List<QuerySortField> querySortFields = queryOptions.getQuerySort().getQuerySortFields();

    //reset from friendly sort fields to non friendly
    for (QuerySortField querySortField : querySortFields) {
      if (StringUtils.equalsIgnoreCase(querySortField.getColumn(), "name")) {
        querySortField.setColumn("thePITGroup.nameDb");
      }
    }
  
    StringBuilder sql = new StringBuilder("select distinct thePITGroup from PITGroup thePITGroup, " +
        " PITMembershipView ms ");
  
    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
  
    //make sure the session can read the privs, if looking at own groups, then allow UPDATE, OPTIN, or OPTOUT since
    //if you have those you can essentially see if you are in the group or not due to optin / optout buttons
    PITField pitField = GrouperDAOFactory.getFactory().getPITField().findById(pitFieldId, true);
    String realFieldId = pitField.getSourceId();
    Field field = FieldFinder.findById(realFieldId, false);
    PITMember pitMember = GrouperDAOFactory.getFactory().getPITMember().findById(pitMemberId, true);
    Member member = MemberFinder.findByUuid(grouperSession, pitMember.getSourceId(), false);
    Set<Privilege> inPrivSet = (field != null && member != null && field.isGroupListField() && SubjectHelper.eq(member, grouperSession.getSubject())) ? 
        AccessPrivilege.OPT_OR_READ_PRIVILEGES :  AccessPrivilege.READ_PRIVILEGES;
    
    //subject to check privileges for
    Subject accessSubject = grouperSession.getSubject();
    
    //see if we are adding more to the query
    boolean changedQuery = grouperSession.getAccessResolver().hqlFilterGroupsWhereClause(accessSubject, byHqlStatic, 
        sql, "thePITGroup.sourceId", inPrivSet);
  
    if (changedQuery && sql.toString().contains(" where ")) {
      sql.append(" and ");
    } else {
      sql.append(" where ");
    }
    
    if (hasScope) {
      sql.append(" thePITGroup.nameDb like :scope and ");
      byHqlStatic.setString("scope", scope + "%");
    }
    
    boolean subScope = false;
    
    if (pitStem != null) {
      switch (stemScope) {
        case ONE:

          sql.append(" thePITGroup.stemId = :stemId and ");
          byHqlStatic.setString("stemId", pitStem.getId());
          
          break;

        case SUB:
          
          // additional filtering after the query
          subScope = true;

          sql.append(" thePITGroup.nameDb like :stemSub and ");
          byHqlStatic.setString("stemSub", pitStem.getName() + ":%");

          break;
        default:
          throw new RuntimeException("Not expecting scope: " + stemScope);
      }
    }
    
    //this must be last due to and's
    sql.append(" ms.ownerGroupId = thePITGroup.id and ms.fieldId = :fieldId " +
      " and ms.memberId = :memberId ");
    
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
    
    byHqlStatic.createQuery(sql.toString())
      .setString("fieldId", pitFieldId)
      .setString("memberId", pitMemberId);

    Set<PITGroup> pitGroups = byHqlStatic
      .setCacheable(false)
      .setCacheRegion(KLASS + ".GetAllGroupsMembershipSecure")
      .options(queryOptions)
      .listSet(PITGroup.class);


    if (subScope) {
      Set<PITGroup> pitGroupsCopy = new LinkedHashSet<PITGroup>(pitGroups);
      for (PITGroup pitGroup : pitGroupsCopy) {
        
        // make sure the pit group was active when the pit stem was active...
        if (pitGroup.getEndTimeDb() != null && pitGroup.getEndTimeDb() < pitStem.getStartTimeDb()) {
          pitGroups.remove(pitGroup);
        } else if (pitStem.getEndTime() != null && pitGroup.getStartTimeDb() > pitStem.getEndTimeDb()) {
          pitGroups.remove(pitGroup);
        }
      }
    }
    
    if (changedQuery || PrivilegeHelper.isWheelOrRoot(accessSubject)) {
      return pitGroups;
    }

    // TODO improve performance here...
    Set<PITGroup> filteredPITGroups = new LinkedHashSet<PITGroup>();
    for (PITGroup pitGroup : pitGroups) {
      if (pitGroup.isActive()) {
        Group group = GrouperDAOFactory.getFactory().getGroup().findByUuid(pitGroup.getSourceId(), true);
        if (PrivilegeHelper.canRead(grouperSession.internal_getRootSession(), group, accessSubject)) {
          filteredPITGroups.add(pitGroup);
        }
      }
    }
    
    return filteredPITGroups;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findByPITStemId(java.lang.String)
   */
  public Set<PITGroup> findByPITStemId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select pitGroup from PITGroup as pitGroup where pitGroup.stemId = :id order by pitGroup.nameDb")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByPITStemId")
        .setString("id", id)
        .listSet(PITGroup.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findMissingActivePITGroups()
   */
  public Set<Group> findMissingActivePITGroups() {

    Set<Group> groups = HibernateSession
      .byHqlStatic()
      .createQuery("select g from Group g where " +
          "not exists (select 1 from PITGroup pitGroup, PITStem pitStem where pitGroup.stemId = pitStem.id " +
          "            and g.uuid = pitGroup.sourceId and g.nameDb = pitGroup.nameDb and g.parentUuid = pitStem.sourceId and g.internalId = pitGroup.sourceInternalId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = g.uuid " +
          "    and type.actionName='addGroup' and type.changeLogCategory='group' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = g.uuid " +
          "    and type.actionName='updateGroup' and type.changeLogCategory='group' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITGroups")
      .listSet(Group.class);
    
    return groups;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findMissingInactivePITGroups()
   */
  public Set<PITGroup> findMissingInactivePITGroups() {

    Set<PITGroup> groups = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITGroup pit where activeDb = 'T' and " +
          "not exists (select 1 from Group g where g.uuid = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteGroup' and type.changeLogCategory='group' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITGroups")
      .listSet(PITGroup.class);
    
    return groups;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findRolesWithPermissionsContainingObject(edu.internet2.middleware.grouper.pit.PITAttributeAssign)
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITAttributeAssign assign) {
    Set<PITGroup> roles = new HashSet<PITGroup>();
    
    if ("any_mem".equals(assign.getAttributeAssignTypeDb())) {
      // need to make sure that the assignment is for a permission..
      PITGroup foundRole = HibernateSession.byHqlStatic().setCacheable(false)
        .createQuery("select gg from PITGroup gg, PITAttributeAssign gaa, PITAttributeDefName gadn, PITAttributeDef gad " +
        		"where gaa.attributeDefNameId = gadn.id and gadn.attributeDefId = gad.id and gaa.ownerGroupId = gg.id " +
        		"and gad.attributeDefTypeDb = 'perm' and gaa.id = :assignId")
        .setString("assignId", assign.getId())
        .uniqueResult(PITGroup.class);
      
      if (foundRole != null) {
        roles.add(foundRole);
      }
    
    } else if ("group".equals(assign.getAttributeAssignTypeDb())) {
      // need to make sure that the assignment is for a permission and join with role sets
      Set<PITGroup> foundRoles = HibernateSession.byHqlStatic().setCacheable(false)
        .createQuery("select distinct gg from PITGroup gg, PITAttributeAssign gaa, PITAttributeDefName gadn, PITAttributeDef gad, PITRoleSet grs " +
            "where gaa.attributeDefNameId = gadn.id and gadn.attributeDefId = gad.id and gaa.ownerGroupId = grs.thenHasRoleId and gg.id = grs.ifHasRoleId " +
            "and grs.activeDb = 'T' and gad.attributeDefTypeDb = 'perm' and gaa.id = :assignId")
        .setString("assignId", assign.getId())
        .listSet(PITGroup.class);
      
      roles.addAll(foundRoles);
    }
    
    return roles;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findRolesWithPermissionsContainingObject(edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet)
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITAttributeAssignActionSet actionSet) {

    Set<PITGroup> roles = HibernateSession.byHqlStatic().setCacheable(false)
      .createQuery("select distinct gg from PITGroup gg, PITAttributeAssign gaa, PITAttributeDefName gadn, PITAttributeDef gad, PITRoleSet grs, PITAttributeAssignActionSet gaaas " +
          "where gaa.attributeDefNameId = gadn.id and gadn.attributeDefId = gad.id " +
          "and ((gaa.attributeAssignTypeDb = 'group' and gaa.ownerGroupId = grs.thenHasRoleId and gg.id = grs.ifHasRoleId) " +
          "  or (gaa.attributeAssignTypeDb = 'any_mem' and grs.ifHasRoleId = gaa.ownerGroupId and grs.depth ='0' and gg.id = grs.ifHasRoleId)) " +
          "and gaaas.ifHasAttrAssignActionId = gaa.attributeAssignActionId and gaaas.thenHasAttrAssignActionId = :actionId " +
          "and grs.activeDb = 'T' and gaaas.activeDb = 'T' and gaa.activeDb = 'T' and gad.attributeDefTypeDb = 'perm'")
      .setString("actionId", actionSet.getIfHasAttrAssignActionId())
      .listSet(PITGroup.class);
    
    return roles;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findRolesWithPermissionsContainingObject(edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet)
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITAttributeDefNameSet attributeDefNameSet) {

    Set<PITGroup> roles = HibernateSession.byHqlStatic().setCacheable(false)
      .createQuery("select distinct gg from PITGroup gg, PITAttributeAssign gaa, PITAttributeDefName gadn, PITAttributeDef gad, PITRoleSet grs, PITAttributeDefNameSet gadns " +
          "where gaa.attributeDefNameId = gadn.id and gadn.attributeDefId = gad.id " +
          "and ((gaa.attributeAssignTypeDb = 'group' and gaa.ownerGroupId = grs.thenHasRoleId and gg.id = grs.ifHasRoleId) " +
          "  or (gaa.attributeAssignTypeDb = 'any_mem' and grs.ifHasRoleId = gaa.ownerGroupId and grs.depth ='0' and gg.id = grs.ifHasRoleId)) " +
          "and gadns.ifHasAttributeDefNameId = gaa.attributeDefNameId and gadns.thenHasAttributeDefNameId = :attributeDefNameId " +
          "and grs.activeDb = 'T' and gadns.activeDb = 'T' and gaa.activeDb = 'T' and gad.attributeDefTypeDb = 'perm'")
      .setString("attributeDefNameId", attributeDefNameSet.getIfHasAttributeDefNameId())
      .listSet(PITGroup.class);
    
    return roles;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findRolesWithPermissionsContainingObject(edu.internet2.middleware.grouper.pit.PITRoleSet)
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITRoleSet roleSet) {

    Set<PITGroup> roles = HibernateSession.byHqlStatic().setCacheable(false)
      .createQuery("select distinct gg from PITGroup gg, PITAttributeAssign gaa, PITAttributeDefName gadn, PITAttributeDef gad, PITRoleSet grs, PITRoleSet grs2 " +
          "where gaa.attributeDefNameId = gadn.id and gadn.attributeDefId = gad.id " +
          "and gaa.attributeAssignTypeDb = 'group' and gaa.ownerGroupId = grs.thenHasRoleId and grs.ifHasRoleId = :thenRoleId " +
          "and grs2.ifHasRoleId = gg.id and grs2.thenHasRoleId = :ifRoleId " +
          "and grs.activeDb = 'T' and grs2.activeDb = 'T' and gaa.activeDb = 'T' and gad.attributeDefTypeDb = 'perm'")
      .setString("ifRoleId", roleSet.getIfHasRoleId())
      .setString("thenRoleId", roleSet.getThenHasRoleId())
      .listSet(PITGroup.class);
    
    return roles;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findRolesWithPermissionsContainingObject(edu.internet2.middleware.grouper.pit.PITMembership)
   */
  public Set<PITGroup> findRolesWithPermissionsContainingObject(PITMembership membership) {

    PITField pitDefaultListField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(Group.getDefaultList().getUuid(), false);
    
    if (pitDefaultListField == null || !pitDefaultListField.getId().equals(membership.getFieldId())) {
      return new HashSet<PITGroup>();
    }
    
    Set<PITGroup> roles = HibernateSession.byHqlStatic().setCacheable(false)
      .createQuery("select distinct gg from PITGroup gg, PITAttributeAssign gaa, PITAttributeDefName gadn, PITAttributeDef gad, PITRoleSet grs, PITGroupSet ggs " +
          "where gaa.attributeDefNameId = gadn.id and gadn.attributeDefId = gad.id " +
          "and ((gaa.attributeAssignTypeDb = 'group' and gaa.ownerGroupId = grs.thenHasRoleId and gg.id = grs.ifHasRoleId) " +
          "  or (gaa.attributeAssignTypeDb = 'any_mem' and grs.ifHasRoleId = gaa.ownerGroupId and grs.depth ='0' and gg.id = grs.ifHasRoleId)) " +
          "and ggs.ownerId = grs.ifHasRoleId and ggs.memberId = :groupId and ggs.fieldId = :fieldId " +
          "and grs.activeDb = 'T' and ggs.activeDb = 'T' and gaa.activeDb = 'T' and gad.attributeDefTypeDb = 'perm'")
      .setString("groupId", membership.getOwnerGroupId())
      .setString("fieldId", pitDefaultListField.getId())
      .listSet(PITGroup.class);
    
    return roles;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITGroup where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITGroup where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}

