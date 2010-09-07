package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.group.GroupSet;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.pit.PITGroupSet;

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
    hibernateSession.byHql().createQuery("update PITGroupSet set parentId = null where id not in (select gs.id from GroupSet as gs)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITGroupSet where id not in (select gs.id from GroupSet as gs)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findById(java.lang.String)
   */
  public PITGroupSet findById(String pitGroupSetId) {
    PITGroupSet pitGroupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where pitGroupSet.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitGroupSetId)
      .uniqueResult(PITGroupSet.class);
    
    return pitGroupSet;
  }
  

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#insertSelfGroupSetsByOwner(java.lang.String, java.lang.Long, java.lang.String, boolean)
   */
  public void insertSelfGroupSetsByOwner(String ownerId, Long startTime, String contextId, boolean checkIfAlreadyExists) {
    Set<GroupSet> groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllSelfGroupSetsByOwnerWherePITFieldExists(ownerId);
    
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      GroupSet groupSet = iter.next();
      
      if (checkIfAlreadyExists) {
        PITGroupSet existing = findById(groupSet.getId());
        if (existing != null) {
          continue;
        }
      }
      
      PITGroupSet pitGroupSet = new PITGroupSet();
      pitGroupSet.setId(groupSet.getId());
      pitGroupSet.setOwnerAttrDefId(groupSet.getOwnerAttrDefId());
      pitGroupSet.setOwnerGroupId(groupSet.getOwnerGroupId());
      pitGroupSet.setOwnerStemId(groupSet.getOwnerStemId());
      pitGroupSet.setMemberAttrDefId(groupSet.getMemberAttrDefId());
      pitGroupSet.setMemberGroupId(groupSet.getMemberGroupId());
      pitGroupSet.setMemberStemId(groupSet.getMemberStemId());
      pitGroupSet.setFieldId(groupSet.getFieldId());
      pitGroupSet.setMemberFieldId(groupSet.getMemberFieldId());
      pitGroupSet.setDepth(0);
      pitGroupSet.setParentId(groupSet.getParentId());
      pitGroupSet.setActiveDb("T");
      pitGroupSet.setStartTimeDb(startTime);
      pitGroupSet.setContextId(contextId);
      pitGroupSet.saveOrUpdate();
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#insertSelfGroupSetsByField(java.lang.String, java.lang.Long, java.lang.String)
   */
  public void insertSelfGroupSetsByField(String fieldId, Long startTime, String contextId) {
    Set<GroupSet> groupSets = GrouperDAOFactory.getFactory().getGroupSet().findAllSelfGroupSetsByFieldWherePITGroupExists(fieldId);
    
    Iterator<GroupSet> iter = groupSets.iterator();
    while (iter.hasNext()) {
      GroupSet groupSet = iter.next();
      PITGroupSet pitGroupSet = new PITGroupSet();
      pitGroupSet.setId(groupSet.getId());
      pitGroupSet.setOwnerGroupId(groupSet.getOwnerId());
      pitGroupSet.setMemberGroupId(groupSet.getMemberId());
      pitGroupSet.setFieldId(groupSet.getFieldId());
      pitGroupSet.setMemberFieldId(groupSet.getMemberFieldId());
      pitGroupSet.setDepth(0);
      pitGroupSet.setParentId(groupSet.getParentId());
      pitGroupSet.setActiveDb("T");
      pitGroupSet.setStartTimeDb(startTime);
      pitGroupSet.setContextId(contextId);
      pitGroupSet.saveOrUpdate();
    }
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#updateEndTimeByOwner(java.lang.String, java.lang.Long, java.lang.String)
   */
  public void updateEndTimeByOwner(String ownerId, Long endTime, String contextId) {
    HibernateSession.byHqlStatic()
        .createQuery("update PITGroupSet set endTimeDb = :endTime, contextId = :contextId, activeDb = :active where ownerId = :ownerId and endTimeDb is null")
        .setLong("endTime", endTime)
        .setString("contextId", contextId)
        .setString("active", "F")
        .setString("ownerId", ownerId)
        .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#updateEndTimeByField(java.lang.String, java.lang.Long, java.lang.String)
   */
  public void updateEndTimeByField(String fieldId, Long endTime, String contextId) {
    HibernateSession.byHqlStatic()
        .createQuery("update PITGroupSet set endTimeDb = :endTime, contextId = :contextId, activeDb = :active where fieldId = :fieldId and endTimeDb is null")
        .setLong("endTime", endTime)
        .setString("contextId", contextId)
        .setString("active", "F")
        .setString("fieldId", fieldId)
        .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#updateEndTimeByOwnerAndField(java.lang.String, java.lang.String, java.lang.Long, java.lang.String)
   */
  public void updateEndTimeByOwnerAndField(String ownerId, String fieldId, Long endTime, String contextId) {
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
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findSelfGroupSet(java.lang.String, java.lang.String, boolean)
   */
  public PITGroupSet findSelfGroupSet(String ownerId, String fieldId, boolean activeOnly) {
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
      .setCacheable(false).setCacheRegion(KLASS + ".FindSelfGroupSet")
      .setString("ownerId", ownerId)
      .setString("fieldId", fieldId)
      .uniqueResult(PITGroupSet.class);
    
    return pitGroupSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findActiveImmediateByOwnerAndMemberAndField(java.lang.String, java.lang.String, java.lang.String)
   */
  public PITGroupSet findActiveImmediateByOwnerAndMemberAndField(String ownerId, String memberId, String fieldId) {
    PITGroupSet pitGroupSet = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet " +
          "where ownerId = :ownerId " +
          "and memberId = :memberId " +
          "and fieldId = :fieldId " +
          "and depth = '1' " +
          "and activeDb='T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveImmediateByOwnerAndMemberAndField")
      .setString("ownerId", ownerId)
      .setString("memberId", memberId)
      .setString("fieldId", fieldId)
      .uniqueResult(PITGroupSet.class);
    
    return pitGroupSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findAllActiveByGroupOwnerAndField(java.lang.String, edu.internet2.middleware.grouper.Field)
   */
  public Set<PITGroupSet> findAllActiveByGroupOwnerAndField(String groupId, Field field) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where ownerGroupId = :groupId and fieldId = :fieldId and activeDb = 'T'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllActiveByGroupOwnerAndField")
        .setString("groupId", groupId)
        .setString("fieldId", field.getUuid())
        .listSet(PITGroupSet.class);

    return pitGroupSets;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findAllActiveByMemberGroup(java.lang.String)
   */
  public Set<PITGroupSet> findAllActiveByMemberGroup(String groupId) {
    Set<PITGroupSet> pitGroupSets = HibernateSession
        .byHqlStatic()
        .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where memberGroupId = :memberId and activeDb = 'T' and depth > '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllActiveByMemberGroup")
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
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupSetDAO#findActiveImmediateChildByParentAndMemberGroup(edu.internet2.middleware.grouper.pit.PITGroupSet, java.lang.String)
   */
  public PITGroupSet findActiveImmediateChildByParentAndMemberGroup(PITGroupSet parentPITGroupSet, String memberGroupId) {
    
    int depth = parentPITGroupSet.getDepth() + 1;
    
    return HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroupSet from PITGroupSet as pitGroupSet where parentId = :parentId and memberId = :memberId and depth = :depth and activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateChildByParentAndMemberGroup")
      .setString("parentId", parentPITGroupSet.getId())
      .setString("memberId", memberGroupId)
      .setInteger("depth", depth)
      .uniqueResult(PITGroupSet.class);
  }  
}

