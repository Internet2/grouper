package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.flat.FlatStem;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.FlatStemDAO;

/**
 * @author shilen
 * $Id$
 */
public class Hib3FlatStemDAO extends Hib3DAO implements FlatStemDAO {

  /**
   *
   */
  private static final String KLASS = Hib3FlatStemDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatStemDAO#save(edu.internet2.middleware.grouper.flat.FlatStem)
   */
  public void save(FlatStem flatStem) {
    HibernateSession.byObjectStatic().save(flatStem);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatStemDAO#delete(edu.internet2.middleware.grouper.flat.FlatStem)
   */
  public void delete(FlatStem flatStem) {
    HibernateSession.byObjectStatic().delete(flatStem);
  }
  
  /**
   * reset flat stem 
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from FlatStem").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatStemDAO#findById(java.lang.String)
   */
  public FlatStem findById(String flatStemId) {
    FlatStem flatStem = HibernateSession
      .byHqlStatic()
      .createQuery("select flatStem from FlatStem as flatStem where flatStem.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", flatStemId)
      .uniqueResult(FlatStem.class);
    
    return flatStem;
  }
  
}

