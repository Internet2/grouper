package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

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
    hibernateSession.byHql().createQuery("update PITAttributeAssignActionSet set parentAttrAssignActionSetId = null where id not in (select actionSet.id from AttributeAssignActionSet as actionSet)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeAssignActionSet where id not in (select actionSet.id from AttributeAssignActionSet as actionSet)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findById(java.lang.String)
   */
  public PITAttributeAssignActionSet findById(String id) {
    PITAttributeAssignActionSet pitAttributeAssignActionSet = HibernateSession
      .byHqlStatic()
      .createQuery("select actionSet from PITAttributeAssignActionSet as actionSet where actionSet.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignActionSet.class);
    
    return pitAttributeAssignActionSet;
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
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#deleteSelfByAttributeAssignActionId(java.lang.String)
   */
  public void deleteSelfByAttributeAssignActionId(final String id) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            //update before delete since mysql cant handle self referential foreign keys
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "update PITAttributeAssignActionSet set parentAttrAssignActionSetId = null where ifHasAttrAssignActionId = :id and thenHasAttrAssignActionId = :id and depth = '0'")
              .setString("id", id)
              .executeUpdate();

            Set<PITAttributeAssignActionSet> pitActionSetsToDelete = findAllSelfAttributeAssignActionSetsByAttributeAssignActionId(id);
            for (PITAttributeAssignActionSet actionSet : pitActionSetsToDelete) {
              delete(actionSet);
            }

            return null;
          }
        });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findAllSelfAttributeAssignActionSetsByAttributeAssignActionId(java.lang.String)
   */
  public Set<PITAttributeAssignActionSet> findAllSelfAttributeAssignActionSetsByAttributeAssignActionId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select actionSet from PITAttributeAssignActionSet as actionSet where actionSet.ifHasAttrAssignActionId = :id and actionSet.thenHasAttrAssignActionId = :id and actionSet.depth = '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllSelfAttributeAssignActionSetsByAttributeAssignActionId")
        .setString("id", id)
        .listSet(PITAttributeAssignActionSet.class);   
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findByThenHasAttributeAssignActionId(java.lang.String)
   */
  public Set<PITAttributeAssignActionSet> findByThenHasAttributeAssignActionId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select actionSet from PITAttributeAssignActionSet as actionSet where thenHasAttrAssignActionId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByThenHasAttributeAssignActionId")
        .setString("id", id)
        .listSet(PITAttributeAssignActionSet.class);
  }
}