package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeDefNameSetDAO extends Hib3DAO implements PITAttributeDefNameSetDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeDefNameSetDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet)
   */
  public void saveOrUpdate(PITAttributeDefNameSet pitAttributeDefNameSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDefNameSet);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeDefNameSet> pitAttributeDefNameSets) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDefNameSets);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet)
   */
  public void delete(PITAttributeDefNameSet pitAttributeDefNameSet) {
    HibernateSession.byObjectStatic().delete(pitAttributeDefNameSet);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITAttributeDefNameSet set parentAttrDefNameSetId = null where id not in (select attrDefNameSet.id from AttributeDefNameSet as attrDefNameSet)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeDefNameSet where id not in (select attrDefNameSet.id from AttributeDefNameSet as attrDefNameSet)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findById(java.lang.String)
   */
  public PITAttributeDefNameSet findById(String id) {
    PITAttributeDefNameSet pitAttributeDefNameSet = HibernateSession
      .byHqlStatic()
      .createQuery("select attrDefNameSet from PITAttributeDefNameSet as attrDefNameSet where attrDefNameSet.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeDefNameSet.class);
    
    return pitAttributeDefNameSet;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    
    //do this since mysql cant handle self-referential foreign keys
    HibernateSession.byHqlStatic()
      .createQuery("update PITAttributeDefNameSet set parentAttrDefNameSetId = null where endTimeDb is not null and endTimeDb < :time and parentAttrDefNameSetId is not null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeDefNameSet where endTimeDb is not null and endTimeDb < :time and parentAttrDefNameSetId is null")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findImmediateChildren(edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet)
   */
  public Set<PITAttributeDefNameSet> findImmediateChildren(PITAttributeDefNameSet pitAttributeDefNameSet) {

    Set<PITAttributeDefNameSet> children = HibernateSession
        .byHqlStatic()
        .createQuery("select adns from PITAttributeDefNameSet as adns where adns.parentAttrDefNameSetId = :parent and adns.depth <> '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindImmediateChildren")
        .setString("parent", pitAttributeDefNameSet.getId())
        .listSet(PITAttributeDefNameSet.class);
    
    return children;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#deleteSelfByAttributeDefNameId(java.lang.String)
   */
  public void deleteSelfByAttributeDefNameId(final String id) {
    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            //update before delete since mysql cant handle self referential foreign keys
            hibernateHandlerBean.getHibernateSession().byHql().createQuery(
              "update PITAttributeDefNameSet set parentAttrDefNameSetId = null where ifHasAttributeDefNameId = :id and thenHasAttributeDefNameId = :id and depth = '0'")
              .setString("id", id)
              .executeUpdate();

            Set<PITAttributeDefNameSet> pitAttributeDefNameSetsToDelete = findAllSelfAttributeDefNameSetsByAttributeDefNameId(id);
            for (PITAttributeDefNameSet rs : pitAttributeDefNameSetsToDelete) {
              delete(rs);
            }

            return null;
          }
        });
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findAllSelfAttributeDefNameSetsByAttributeDefNameId(java.lang.String)
   */
  public Set<PITAttributeDefNameSet> findAllSelfAttributeDefNameSetsByAttributeDefNameId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select adns from PITAttributeDefNameSet as adns where adns.ifHasAttributeDefNameId = :id and adns.thenHasAttributeDefNameId = :id and adns.depth = '0'")
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllSelfAttributeDefNameSetsByAttributeDefNameId")
        .setString("id", id)
        .listSet(PITAttributeDefNameSet.class);    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findByThenHasAttributeDefNameId(java.lang.String)
   */
  public Set<PITAttributeDefNameSet> findByThenHasAttributeDefNameId(String id) {
    return HibernateSession
        .byHqlStatic()
        .createQuery("select adns from PITAttributeDefNameSet as adns where thenHasAttributeDefNameId = :id")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByThenHasAttributeDefNameId")
        .setString("id", id)
        .listSet(PITAttributeDefNameSet.class);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findMissingActivePITAttributeDefNameSets()
   */
  public Set<AttributeDefNameSet> findMissingActivePITAttributeDefNameSets() {

    Set<AttributeDefNameSet> attrSets = HibernateSession
      .byHqlStatic()
      .createQuery("select attrSet from AttributeDefNameSet attrSet where " +
          "not exists (select 1 from PITAttributeDefNameSet pit where attrSet.id = pit.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = attrSet.id " +
          "    and type.actionName='addAttributeDefNameSet' and type.changeLogCategory='attributeDefNameSet' and type.id=temp.changeLogTypeId) " +
          "order by attrSet.depth")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeDefNameSets")
      .listSet(AttributeDefNameSet.class);
    
    return attrSets;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findMissingInactivePITAttributeDefNameSets()
   */
  public Set<PITAttributeDefNameSet> findMissingInactivePITAttributeDefNameSets() {

    Set<PITAttributeDefNameSet> attrSets = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeDefNameSet pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeDefNameSet attrSet where attrSet.id = pit.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.id " +
          "    and type.actionName='deleteAttributeDefNameSet' and type.changeLogCategory='attributeDefNameSet' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeDefNameSets")
      .listSet(PITAttributeDefNameSet.class);
    
    return attrSets;
  }
}