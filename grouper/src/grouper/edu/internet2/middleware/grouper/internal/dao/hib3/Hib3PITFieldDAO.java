package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITFieldDAO;
import edu.internet2.middleware.grouper.pit.PITField;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITFieldDAO extends Hib3DAO implements PITFieldDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITFieldDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITField)
   */
  public void saveOrUpdate(PITField pitField) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitField);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#saveOrUpdate(java.util.Set)
   */
  public void saveOrUpdate(Set<PITField> pitFields) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitFields);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#delete(edu.internet2.middleware.grouper.pit.PITField)
   */
  public void delete(PITField pitField) {
    HibernateSession.byObjectStatic().delete(pitField);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITField where id not in (select f.uuid from Field as f)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findById(java.lang.String)
   */
  public PITField findById(String pitFieldId) {
    PITField pitField = HibernateSession
      .byHqlStatic()
      .createQuery("select pitField from PITField as pitField where pitField.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitFieldId)
      .uniqueResult(PITField.class);
    
    return pitField;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITField where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findMissingActivePITFields()
   */
  public Set<Field> findMissingActivePITFields() {

    Set<Field> fields = HibernateSession
      .byHqlStatic()
      .createQuery("select f from Field f where " +
          "not exists (select 1 from PITField pit where f.uuid = pit.id and f.name = pit.nameDb and f.typeString = pit.typeDb) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = f.uuid " +
          "    and type.actionName='addGroupField' and type.changeLogCategory='groupField' and type.id=temp.changeLogTypeId) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = f.uuid " +
          "    and type.actionName='updateGroupField' and type.changeLogCategory='groupField' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingActivePITFields")
      .listSet(Field.class);
    
    return fields;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#findMissingInactivePITFields()
   */
  public Set<PITField> findMissingInactivePITFields() {

    Set<PITField> fields = HibernateSession
      .byHqlStatic()
      .createQuery("select pit from PITField pit where activeDb = 'T' and " +
          "not exists (select 1 from Field f where f.uuid = pit.id) " +
          "and not exists (select 1 from ChangeLogEntryTemp temp, ChangeLogType type " +
          "    where temp.string01 = pit.id " +
          "    and type.actionName='deleteGroupField' and type.changeLogCategory='groupField' and type.id=temp.changeLogTypeId)")
      .setCacheable(false).setCacheRegion(KLASS + ".FindMissingInactivePITFields")
      .listSet(PITField.class);
    
    return fields;
  }
}

