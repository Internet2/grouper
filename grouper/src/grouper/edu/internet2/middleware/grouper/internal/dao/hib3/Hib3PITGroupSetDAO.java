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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;
import edu.internet2.middleware.grouper.pit.PITField;
import edu.internet2.middleware.grouper.pit.PITGroup;
import edu.internet2.middleware.grouper.pit.PITGroupSet;
import edu.internet2.middleware.grouper.pit.PITStem;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITGroupSetDAO extends Hib3DAO implements PITGroupSetDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITGroupSetDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITGroupSet)
   */
  public void saveOrUpdate(PITGroupSet pitGroupSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitGroupSet);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITGroupSet> pitGroupSets) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitGroupSets);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#delete(edu.internet2.middleware.grouper.pit.PITGroupSet)
   */
  public void delete(PITGroupSet pitGroupSet) {
    HibernateSession.byObjectStatic().delete(pitGroupSet);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITGroupSet set parentId = null where sourceId not in (select gs.id from GroupSet as gs)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITGroupSet where sourceId not in (select gs.id from GroupSet as gs)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITGroupSet findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITGroupSet pitGroupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where pitGroupSet.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITGroupSet.class);
    
    if (pitGroupSet == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITGroupSet with sourceId=" + id + " not found");
    }
    
    return pitGroupSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITGroupSet findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITGroupSet pitGroupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where pitGroupSet.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITGroupSet.class);
    
    if (pitGroupSet == null && exceptionIfNotFound) {
      throw new RuntimeException("PITGroupSet with sourceId=" + id + " not found");
    }
    
    return pitGroupSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findById(java.lang.String, boolean)
   */
  public PITGroupSet findById(String id, boolean exceptionIfNotFound) {
    PITGroupSet pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITGroupSet as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITGroupSet.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITGroupSet with id=" + id + " not found");
    }
    
    return pit;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#insertSelfPITGroupSetsByOwner(java.lang.String, java.lang.Long, java.lang.String, boolean)
   */
  public void insertSelfPITGroupSetsByOwner(String ownerId, Long startTime, String contextId, boolean checkIfAlreadyExists) {
    Set<GroupSet> groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllSelfGroupSetsByOwnerWherePITFieldExists(ownerId);
    String pitGroupId = null;
    String pitStemId = null;
    String pitAttrDefId = null;
    
    if (groupSets.size() > 0) {
      GroupSet groupSet = groupSets.iterator().next();
      if (groupSet.getOwnerGroupId() != null) {
        PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupSet.getOwnerId(), true);
        pitGroupId = pitOwner.getId();
      } else if (groupSet.getOwnerStemId() != null) {
        PITStem pitOwner = GrouperDAOFactory.getFactory().getPITStem().findBySourceIdActive(groupSet.getOwnerId(), true);
        pitStemId = pitOwner.getId();
      } else if (groupSet.getOwnerAttrDefId() != null) {
        PITAttributeDef pitOwner = GrouperDAOFactory.getFactory().getPITAttributeDef().findBySourceIdActive(groupSet.getOwnerId(), true);
        pitAttrDefId = pitOwner.getId();
      } else {
        throw new RuntimeException("Unexpected -- GroupSet with id " + groupSet.getId() + " does not have an ownerGroupId, ownerStemId, or ownerAttrDefId.");
      }
    }
    
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      GroupSet groupSet = iter.next();
      
      if (checkIfAlreadyExists) {
        PITGroupSet existing = findBySourceIdActive(groupSet.getId(), false);
        if (existing != null) {
          continue;
        }
      }
      
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(groupSet.getFieldId(), true);
      
      PITGroupSet pitGroupSet = new PITGroupSet();
      pitGroupSet.setId(GrouperUuid.getUuid());
      pitGroupSet.setSourceId(groupSet.getId());
      pitGroupSet.setFieldId(pitField.getId());
      pitGroupSet.setMemberFieldId(pitField.getId());
      pitGroupSet.setDepth(0);
      pitGroupSet.setParentId(pitGroupSet.getId());
      pitGroupSet.setActiveDb("T");
      pitGroupSet.setStartTimeDb(startTime);
      pitGroupSet.setContextId(contextId);
      pitGroupSet.setOwnerGroupId(pitGroupId);
      pitGroupSet.setOwnerStemId(pitStemId);
      pitGroupSet.setOwnerAttrDefId(pitAttrDefId);
      pitGroupSet.setMemberGroupId(pitGroupId);
      pitGroupSet.setMemberStemId(pitStemId);
      pitGroupSet.setMemberAttrDefId(pitAttrDefId);
      pitGroupSet.saveOrUpdate();
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#insertSelfPITGroupSetsByField(java.lang.String, java.lang.Long, java.lang.String)
   */
  public void insertSelfPITGroupSetsByField(String fieldId, Long startTime, String contextId) {
    Set<GroupSet> groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllSelfGroupSetsByFieldWherePITGroupExists(fieldId);
    String pitFieldId = null;
    
    if (groupSets.size() > 0) {
      PITField pitField = GrouperDAOFactory.getFactory().getPITField().findBySourceIdActive(fieldId, true);
      pitFieldId = pitField.getId();
    }
    
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      GroupSet groupSet = iter.next();
      PITGroup pitOwner = GrouperDAOFactory.getFactory().getPITGroup().findBySourceIdActive(groupSet.getOwnerId(), true);
      
      PITGroupSet pitGroupSet = new PITGroupSet();
      pitGroupSet.setId(GrouperUuid.getUuid());
      pitGroupSet.setSourceId(groupSet.getId());
      pitGroupSet.setOwnerGroupId(pitOwner.getId());
      pitGroupSet.setMemberGroupId(pitOwner.getId());
      pitGroupSet.setFieldId(pitFieldId);
      pitGroupSet.setMemberFieldId(pitFieldId);
      pitGroupSet.setDepth(0);
      pitGroupSet.setParentId(pitGroupSet.getId());
      pitGroupSet.setActiveDb("T");
      pitGroupSet.setStartTimeDb(startTime);
      pitGroupSet.setContextId(contextId);
      pitGroupSet.saveOrUpdate();
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#updateEndTimeByPITOwner(java.lang.String, java.lang.Long, java.lang.String)
   */
  public void updateEndTimeByPITOwner(String ownerId, Long endTime, String contextId) {
    HibernateSession.byHqlStatic()
        .createQuery("update PITGroupSet set endTimeDb = :endTime, contextId = :contextId, activeDb = :active where ownerId = :ownerId and endTimeDb is null")
        .setLong("endTime", endTime)
        .setString("contextId", contextId)
        .setString("active", "F")
        .setString("ownerId", ownerId)
        .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#updateEndTimeByPITField(java.lang.String, java.lang.Long, java.lang.String)
   */
  public void updateEndTimeByPITField(String fieldId, Long endTime, String contextId) {
    HibernateSession.byHqlStatic()
        .createQuery("update PITGroupSet set endTimeDb = :endTime, contextId = :contextId, activeDb = :active where fieldId = :fieldId and endTimeDb is null")
        .setLong("endTime", endTime)
        .setString("contextId", contextId)
        .setString("active", "F")
        .setString("fieldId", fieldId)
        .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#updateEndTimeByPITOwnerAndPITField(java.lang.String, java.lang.String, java.lang.Long, java.lang.String)
   */
  public void updateEndTimeByPITOwnerAndPITField(String ownerId, String fieldId, Long endTime, String contextId) {
    HibernateSession.byHqlStatic()
        .createQuery("update PITGroupSet set endTimeDb = :endTime, contextId = :contextId, activeDb = :active where ownerId = :ownerId and fieldId = :fieldId and endTimeDb is null")
        .setLong("endTime", endTime)
        .setString("contextId", contextId)
        .setString("active", "F")
        .setString("ownerId", ownerId)
        .setString("fieldId", fieldId)
        .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findSelfPITGroupSet(java.lang.String, java.lang.String, boolean)
   */
  public PITGroupSet findSelfPITGroupSet(String ownerId, String fieldId, boolean activeOnly) {
    StringBuilder sql = new StringBuilder();
    sql.append("select pitGroupSet from PITGroupSet as pitGroupSet " +
          "where ownerId = :ownerId " +
          "and memberId = :ownerId " +
          "and fieldId = :fieldId " +
          "and depth = '0'");
    
    if (activeOnly) {
      sql.append(" and activeDb='T'");
    }
    
    PITGroupSet pitGroupSet = HibernateSession
      .byHqlStatic()
      .createQuery(sql.toString())
      .setCacheable(false).setCacheRegion(KLASS + ".FindSelfPITGroupSet")
      .setString("ownerId", ownerId)
      .setString("fieldId", fieldId)
      .uniqueResult(PITGroupSet.class);
    
    return pitGroupSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findActiveImmediateByPITOwnerAndPITMemberAndPITField(java.lang.String, java.lang.String, java.lang.String)
   */
  public PITGroupSet findActiveImmediateByPITOwnerAndPITMemberAndPITField(String ownerId, String memberId, String fieldId) {
    PITGroupSet pitGroupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet " +
          "where ownerId = :ownerId " +
          "and memberId = :memberId " +
          "and fieldId = :fieldId " +
          "and depth = '1' " +
          "and activeDb='T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveImmediateByPITOwnerAndPITMemberAndPITField")
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .uniqueResult(PITGroupSet.class);
    
    return pitGroupSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findAllActiveByPITGroupOwnerAndPITField(java.lang.String, edu.internet2.middleware.grouper.pit.PITField)
   */
  public Set<PITGroupSet> findAllActiveByPITGroupOwnerAndPITField(String groupId, PITField field) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where ownerGroupId = :groupId and fieldId = :fieldId and activeDb = 'T'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllActiveByPITGroupOwnerAndPITField")
        .setString("groupId", groupId)
        .setString("fieldId", field.getId())
        .listSet(PITGroupSet.class);

    return pitGroupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findAllActiveByMemberPITGroup(java.lang.String)
   */
  public Set<PITGroupSet> findAllActiveByMemberPITGroup(String groupId) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where memberGroupId = :memberId and activeDb = 'T' and depth > '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllActiveByMemberPITGroup")
        .setString("memberId", groupId)
        .listSet(PITGroupSet.class);

    return pitGroupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findAllByMemberPITGroup(java.lang.String)
   */
  public Set<PITGroupSet> findAllByMemberPITGroup(String groupId) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where memberGroupId = :memberId")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllByMemberPITGroup")
        .setString("memberId", groupId)
        .listSet(PITGroupSet.class);

    return pitGroupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findAllActiveChildren(edu.internet2.middleware.grouper.pit.PITGroupSet)
   */
  public Set<PITGroupSet> findAllActiveChildren(PITGroupSet pitGroupSet) {
    Set<PITGroupSet> allChildren = new LinkedHashSet<PITGroupSet>();
    Set<PITGroupSet> children = HibernateSession
        .byHqlStatic()
        .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where parentId = :parentId and activeDb = 'T' and depth > '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllActiveChildren")
        .setString("parentId", pitGroupSet.getId())
        .listSet(PITGroupSet.class);
    
    Iterator<PITGroupSet> iter = children.iterator();
    
    while (iter.hasNext()) {
      PITGroupSet child = iter.next();
      allChildren.addAll(findAllActiveChildren(child));
      allChildren.add(child);
    }
    
    return allChildren;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findActiveImmediateChildByParentAndMemberPITGroup(edu.internet2.middleware.grouper.pit.PITGroupSet, java.lang.String)
   */
  public PITGroupSet findActiveImmediateChildByParentAndMemberPITGroup(PITGroupSet parentPITGroupSet, String memberGroupId) {
    
    int depth = parentPITGroupSet.getDepth() + 1;
    
    return HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where parentId = :parentId and memberId = :memberId and depth = :depth and activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateChildByParentAndMemberPITGroup")
      .setString("parentId", parentPITGroupSet.getId())
      .setString("memberId", memberGroupId)
      .setInteger("depth", depth)
      .uniqueResult(PITGroupSet.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    
    //do this since mysql cant handle self-referential foreign keys
    HibernateSession.byHqlStatic()
      .createQuery("update PITGroupSet set parentId = null where endTimeDb is not null and endTimeDb < :time and parentId is not null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITGroupSet where endTimeDb is not null and endTimeDb < :time and parentId is null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findImmediateChildren(edu.internet2.middleware.grouper.pit.PITGroupSet)
   */
  public Set<PITGroupSet> findImmediateChildren(PITGroupSet groupSet) {
    Set<PITGroupSet> children = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from PITGroupSet as gs where gs.parentId = :parent and gs.depth <> '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateChildren")
        .setString("parent", groupSet.getId())
        .listSet(PITGroupSet.class);

    return children;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findAllSelfPITGroupSetsByPITOwnerId(java.lang.String)
   */
  public Set<PITGroupSet> findAllSelfPITGroupSetsByPITOwnerId(String id) {
    Set<PITGroupSet> groupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select gs from PITGroupSet as gs where gs.ownerId = :id and gs.depth = '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllSelfPITGroupSetsByPITOwnerId")
        .setString("id", id)
        .listSet(PITGroupSet.class);
    
    return groupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#deleteSelfByPITOwnerId(java.lang.String)
   */
  public void deleteSelfByPITOwnerId(final String id) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            //update before delete since mysql cant handle self referential foreign keys
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "update PITGroupSet set parentId = null where ownerId = :id and depth='0'")
              .setString("id", id)
              .executeUpdate();

            Set<PITGroupSet> pitGroupSetsToDelete = findAllSelfPITGroupSetsByPITOwnerId(id);
            for (PITGroupSet gs : pitGroupSetsToDelete) {
              delete(gs);
            }

            return null;
          }
        });
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findMissingActivePITGroupSets()
   */
  public Set<GroupSet> findMissingActivePITGroupSets() {

    Set<GroupSet> groupSets = HibernateSession
      .byHqlStatic()
      .createQuery("select g from GroupSet g where g.depth = '0' and " +
          "not exists (select 1 from PITGroupSet pit where g.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp " +
          "    where temp.string01 = g.ownerId or temp.string01 = g.fieldId or temp.string02 = g.ownerId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITGroupSets")
      .listSet(GroupSet.class);
    
    return groupSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findMissingInactivePITGroupSets()
   */
  public Set<PITGroupSet> findMissingInactivePITGroupSets() {

    // This only works if there are no group, stem, attr def, field, or group type deletes in the change log
    // .. definitely needs to be improved..
    Set<PITGroupSet> groupSets = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITGroupSet pit where depth = '0' and activeDb = 'T' and " +
          "not exists (select 1 from GroupSet g where g.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where type.id=temp.changeLogTypeId " +
          "    and (type.actionName='deleteGroup' or type.actionName='deleteStem' or type.actionName='deleteAttributeDef' or type.actionName='deleteGroupField' or type.actionName='unassignGroupType'))")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITGroupSets")
      .listSet(PITGroupSet.class);
    
    return groupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findActiveDuplicates()
   */
  public Set<String> findActiveDuplicates() {
    return HibernateSession
      .byHqlStatic()
      .createQuery("select sourceId from PITGroupSet where active='T' group by sourceId having count(*) > 1")
      .setCacheable(false)
      .listSet(String.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findBySourceId(java.lang.String, boolean)
   */
  public Set<PITGroupSet> findBySourceId(String id, boolean exceptionIfNotFound) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where pitGroupSet.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceId")
      .setString("id", id)
      .listSet(PITGroupSet.class);
    
    if (pitGroupSets.size() == 0 && exceptionIfNotFound) {
      throw new RuntimeException("PITGroupSet with sourceId=" + id + " not found");
    }
    
    return pitGroupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#delete(java.lang.String)
   */
  public void delete(String id) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITGroupSet where id = :id")
      .setString("id", id)
      .executeUpdate();
  }
}

