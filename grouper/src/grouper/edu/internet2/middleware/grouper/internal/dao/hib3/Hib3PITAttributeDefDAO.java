package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.sql.Timestamp;
import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeDef;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeDefDAO extends Hib3DAO implements PITAttributeDefDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeDefDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeDef)
   */
  public void saveOrUpdate(PITAttributeDef pitAttributeDef) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDef);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeDef)
   */
  public void delete(PITAttributeDef pitAttributeDef) {
    HibernateSession.byObjectStatic().delete(pitAttributeDef);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#saveBatch(java.util.Set)
   */
  public void saveBatch(Set<PITAttributeDef> pitAttributeDefs) {
    HibernateSession.byObjectStatic().saveBatch(pitAttributeDefs);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeDef where id not in (select a.id from AttributeDef as a)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#findById(java.lang.String)
   */
  public PITAttributeDef findById(String pitAttributeDefId) {
    PITAttributeDef pitAttributeDef = HibernateSession
      .byHqlStatic()
      .createQuery("select pitAttributeDef from PITAttributeDef as pitAttributeDef where pitAttributeDef.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitAttributeDefId)
      .uniqueResult(PITAttributeDef.class);
    
    return pitAttributeDef;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefDAO#deleteInactiveRecords(java.sql.Timestamp)
   */
  public void deleteInactiveRecords(Timestamp time) {
    HibernateSession.byHqlStatic()
      .createQuery("delete from PITAttributeDef where endTimeDb is not null and endTimeDb < :time")
      .setLong("time", time.getTime() * 1000)
      .executeUpdate();
  }
}

