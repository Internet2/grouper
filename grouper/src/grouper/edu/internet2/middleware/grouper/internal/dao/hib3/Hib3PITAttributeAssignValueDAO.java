package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignValueDAO extends Hib3DAO implements PITAttributeAssignValueDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignValueDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssignValue)
   */
  public void saveOrUpdate(PITAttributeAssignValue pitAttributeAssignValue) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignValue);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITAttributeAssignValue> pitAttributeAssignValues) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignValues);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssignValue)
   */
  public void delete(PITAttributeAssignValue pitAttributeAssignValue) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssignValue);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeAssignValue where id not in (select v.id from AttributeAssignValue as v)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findById(java.lang.String)
   */
  public PITAttributeAssignValue findById(String id) {
    PITAttributeAssignValue pitAttributeAssignValue = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssignValue from PITAttributeAssignValue as attrAssignValue where attrAssignValue.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignValue.class);
    
    return pitAttributeAssignValue;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#updateAttributeAssignId(java.lang.String, java.lang.String)
   */
  public void updateAttributeAssignId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITAttributeAssignValue set attributeAssignId = :newId where attributeAssignId = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findActiveByAttributeAssignId(java.lang.String)
   */
  public Set<PITAttributeAssignValue> findActiveByAttributeAssignId(String id) {
    Set<PITAttributeAssignValue> values = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssignValue from PITAttributeAssignValue as attrAssignValue where attrAssignValue.attributeAssignId = :id and attrAssignValue.activeDb = 'T'")
      .setCacheable(false).setCacheRegion(KLASS + ".FindActiveByAttributeAssignId")
      .setString("id", id)
      .listSet(PITAttributeAssignValue.class);
    
    return values;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeAssignValue where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findByAttributeAssignId(java.lang.String, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<PITAttributeAssignValue> findByAttributeAssignId(String attributeAssignId, QueryOptions queryOptions) {
    Set<PITAttributeAssignValue> attributeAssignValues = HibernateSession.byHqlStatic()
      .createQuery("from PITAttributeAssignValue as theAttributeAssignValue where " +
          "theAttributeAssignValue.attributeAssignId = :theAttributeAssignId")
      .options(queryOptions)
      .setCacheable(true)
      .setCacheRegion(KLASS + ".FindByAttributeAssignId")
      .setString("theAttributeAssignId", attributeAssignId)
      .listSet(PITAttributeAssignValue.class);

    //return result
    return attributeAssignValues;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findMissingActivePITAttributeAssignValues()
   */
  public Set<AttributeAssignValue> findMissingActivePITAttributeAssignValues() {

    Set<AttributeAssignValue> values = HibernateSession
      .byHqlStatic()
      .createQuery("select value from AttributeAssignValue value where " +
          "not exists (select 1 from PITAttributeAssignValue pit where value.id = pit.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = value.id " +
          "    and type.actionName='addAttributeAssignValue' and type.changeLogCategory='attributeAssignValue' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITAttributeAssignValues")
      .listSet(AttributeAssignValue.class);
    
    return values;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findMissingInactivePITAttributeAssignValues()
   */
  public Set<PITAttributeAssignValue> findMissingInactivePITAttributeAssignValues() {

    Set<PITAttributeAssignValue> values = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITAttributeAssignValue pit where activeDb = 'T' and " +
          "not exists (select 1 from AttributeAssignValue value where value.id = pit.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.id " +
          "    and type.actionName='deleteAttributeAssignValue' and type.changeLogCategory='attributeAssignValue' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITAttributeAssignValues")
      .listSet(PITAttributeAssignValue.class);
    
    return values;
  }
}