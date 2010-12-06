package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

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
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#delete(edu.internet2.middleware.grouper.pit.PITField)
   */
  public void delete(PITField pitField) {
    HibernateSession.byObjectStatic().delete(pitField);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITFieldDAO#saveBatch(java.util.Set)
   */
  public void saveBatch(Set<PITField> pitFields) {
    HibernateSession.byObjectStatic().saveBatch(pitFields);
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
}

