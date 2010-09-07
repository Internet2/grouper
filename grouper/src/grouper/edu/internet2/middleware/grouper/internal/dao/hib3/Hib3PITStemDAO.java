package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITStemDAO;
import edu.internet2.middleware.grouper.pit.PITStem;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITStemDAO extends Hib3DAO implements PITStemDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITStemDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITStem)
   */
  public void saveOrUpdate(PITStem pitStem) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitStem);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#delete(edu.internet2.middleware.grouper.pit.PITStem)
   */
  public void delete(PITStem pitStem) {
    HibernateSession.byObjectStatic().delete(pitStem);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#saveBatch(java.util.Set)
   */
  public void saveBatch(Set<PITStem> pitStems) {
    HibernateSession.byObjectStatic().saveBatch(pitStems);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITStem where id not in (select s.uuid from Stem as s)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITStemDAO#findById(java.lang.String)
   */
  public PITStem findById(String pitStemId) {
    PITStem pitStem = HibernateSession
      .byHqlStatic()
      .createQuery("select pitStem from PITStem as pitStem where pitStem.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitStemId)
      .uniqueResult(PITStem.class);
    
    return pitStem;
  }
}

