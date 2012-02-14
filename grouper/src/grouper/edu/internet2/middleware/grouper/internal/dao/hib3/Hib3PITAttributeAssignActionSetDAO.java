package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignActionSetDAO extends Hib3DAO implements PITAttributeAssignActionSetDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignActionSetDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet)
   */
  public void saveOrUpdate(PITAttributeAssignActionSet pitAttributeAssignActionSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignActionSet);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeAssignActionSet> pitAttributeAssignActionSets) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignActionSets);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet)
   */
  public void delete(PITAttributeAssignActionSet pitAttributeAssignActionSet) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssignActionSet);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITAttributeAssignActionSet set parentAttrAssignActionSetId = null where sourceId not in (select actionSet.id from AttributeAssignActionSet as actionSet)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeAssignActionSet where sourceId not in (select actionSet.id from AttributeAssignActionSet as actionSet)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findBySourceIdActive(java.lang.String, boolean)
   */
  public PITAttributeAssignActionSet findBySourceIdActive(String id, boolean exceptionIfNotFound) {
    PITAttributeAssignActionSet pitAttributeAssignActionSet = HibernateSession
      .byHqlStatic()
      .createQuery("select actionSet from PITAttributeAssignActionSet as actionSet where actionSet.sourceId = :id and activeDb = 'T'")
      .setCacheable(true).setCacheRegion(KLASS + ".FindBySourceIdActive")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignActionSet.class);
    
    if (pitAttributeAssignActionSet == null && exceptionIfNotFound) {
      throw new RuntimeException("Active PITAttributeAssignActionSet with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssignActionSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findBySourceIdUnique(java.lang.String, boolean)
   */
  public PITAttributeAssignActionSet findBySourceIdUnique(String id, boolean exceptionIfNotFound) {
    PITAttributeAssignActionSet pitAttributeAssignActionSet = HibernateSession
      .byHqlStatic()
      .createQuery("select actionSet from PITAttributeAssignActionSet as actionSet where actionSet.sourceId = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindBySourceIdUnique")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignActionSet.class);
    
    if (pitAttributeAssignActionSet == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssignActionSet with sourceId=" + id + " not found");
    }
    
    return pitAttributeAssignActionSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findById(java.lang.String, boolean)
   */
  public PITAttributeAssignActionSet findById(String id, boolean exceptionIfNotFound) {
    PITAttributeAssignActionSet pit = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssignActionSet as pit where pit.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignActionSet.class);
    
    if (pit == null && exceptionIfNotFound) {
      throw new RuntimeException("PITAttributeAssignActionSet with id=" + id + " not found");
    }
    
    return pit;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    
    //do this since mysql cant handle self-referential foreign keys
    HibernateSession.byHqlStatic()
      .createQuery("update PITAttributeAssignActionSet set parentAttrAssignActionSetId = null where endTimeDb is not null and endTimeDb < :time and parentAttrAssignActionSetId is not null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeAssignActionSet where endTimeDb is not null and endTimeDb < :time and parentAttrAssignActionSetId is null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findImmediateChildren(edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet)
   */
  public Set<PITAttributeAssignActionSet> findImmediateChildren(PITAttributeAssignActionSet pitAttributeAssignActionSet) {

    Set<PITAttributeAssignActionSet> children = HibernateSession
        .byHqlStatic()
        .createQuery("select actionSet from PITAttributeAssignActionSet as actionSet where actionSet.parentAttrAssignActionSetId = :parent and actionSet.depth <> '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateChildren")
        .setString("parent", pitAttributeAssignActionSet.getId())
        .listSet(PITAttributeAssignActionSet.class);
    
    return children;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#deleteSelfByPITAttributeAssignActionId(java.lang.String)
   */
  public void deleteSelfByPITAttributeAssignActionId(final String id) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            //update before delete since mysql cant handle self referential foreign keys
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "update PITAttributeAssignActionSet set parentAttrAssignActionSetId = null where ifHasAttrAssignActionId = :id and thenHasAttrAssignActionId = :id and depth = '0'")
              .setString("id", id)
              .executeUpdate();

            Set<PITAttributeAssignActionSet> pitActionSetsToDelete = findAllSelfPITAttributeAssignActionSetsByPITAttributeAssignActionId(id);
            for (PITAttributeAssignActionSet actionSet : pitActionSetsToDelete) {
              delete(actionSet);
            }

            return null;
          }
        });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findAllSelfPITAttributeAssignActionSetsByPITAttributeAssignActionId(java.lang.String)
   */
  public Set<PITAttributeAssignActionSet> findAllSelfPITAttributeAssignActionSetsByPITAttributeAssignActionId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select actionSet from PITAttributeAssignActionSet as actionSet where actionSet.ifHasAttrAssignActionId = :id and actionSet.thenHasAttrAssignActionId = :id and actionSet.depth = '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllSelfPITAttributeAssignActionSetsByPITAttributeAssignActionId")
        .setString("id", id)
        .listSet(PITAttributeAssignActionSet.class);   
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findByThenHasPITAttributeAssignActionId(java.lang.String)
   */
  public Set<PITAttributeAssignActionSet> findByThenHasPITAttributeAssignActionId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select actionSet from PITAttributeAssignActionSet as actionSet where thenHasAttrAssignActionId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByThenHasPITAttributeAssignActionId")
        .setString("id", id)
        .listSet(PITAttributeAssignActionSet.class);
  }
  
  public Set<AttributeAssignActionSet> findMissingActivePITAttributeAssignActionSets() {

    Set<AttributeAssignActionSet> actionSets = HibernateSession
      .byHqlStatic()
      .createQuery("select a from AttributeAssignActionSet a where " +
          "not exists (select 1 from PITAttributeAssignActionSet pit where a.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = a.id " +
          "    and type.actionName='addAttributeAssignActionSet' and type.changeLogCategory='attributeAssignActionSet' and type.id=temp.changeLogTypeId) " +
          "order by a.depth")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeAssignActionSets")
      .listSet(AttributeAssignActionSet.class);
    
    return actionSets;
  }

  public Set<PITAttributeAssignActionSet> findMissingInactivePITAttributeAssignActionSets() {

    Set<PITAttributeAssignActionSet> actionSets = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssignActionSet pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeAssignActionSet a where a.id = pit.sourceId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.sourceId " +
          "    and type.actionName='deleteAttributeAssignActionSet' and type.changeLogCategory='attributeAssignActionSet' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeAssignActionSets")
      .listSet(PITAttributeAssignActionSet.class);
    
    return actionSets;
  }
}
