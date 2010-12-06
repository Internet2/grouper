package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeDefName;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeDefNameDAO extends Hib3DAO implements PITAttributeDefNameDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeDefNameDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeDefName)
   */
  public void saveOrUpdate(PITAttributeDefName pitAttributeDefName) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDefName);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeDefName)
   */
  public void delete(PITAttributeDefName pitAttributeDefName) {
    HibernateSession.byObjectStatic().delete(pitAttributeDefName);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeDefName where id not in (select attrDefName.id from AttributeDefName as attrDefName)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#findById(java.lang.String)
   */
  public PITAttributeDefName findById(String id) {
    PITAttributeDefName pitAttributeDefName = HibernateSession
      .byHqlStatic()
      .createQuery("select attrDefName from PITAttributeDefName as attrDefName where attrDefName.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeDefName.class);
    
    return pitAttributeDefName;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeDefName where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
}