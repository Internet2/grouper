package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.Set;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITGroupDAO;
import edu.internet2.middleware.grouper.pit.PITGroup;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITGroupDAO extends Hib3DAO implements PITGroupDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITGroupDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITGroup)
   */
  public void saveOrUpdate(PITGroup pitGroup) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitGroup);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#delete(edu.internet2.middleware.grouper.pit.PITGroup)
   */
  public void delete(PITGroup pitGroup) {
    HibernateSession.byObjectStatic().delete(pitGroup);
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#saveBatch(java.util.Set)
   */
  public void saveBatch(Set<PITGroup> pitGroups) {
    HibernateSession.byObjectStatic().saveBatch(pitGroups);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITGroup where id not in (select g.uuid from Group as g)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITGroupDAO#findById(java.lang.String)
   */
  public PITGroup findById(String pitGroupId) {
    PITGroup pitGroup = HibernateSession
      .byHqlStatic()
      .createQuery("select pitGroup from PITGroup as pitGroup where pitGroup.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", pitGroupId)
      .uniqueResult(PITGroup.class);
    
    return pitGroup;
  }
}

