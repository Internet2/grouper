package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.flat.FlatGroup;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO;

/**
 * @author shilen
 * $Id$
 */
public class Hib3FlatGroupDAO extends Hib3DAO implements FlatGroupDAO {

  /**
   *
   */
  private static final String KLASS = Hib3FlatGroupDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO#save(edu.internet2.middleware.grouper.flat.FlatGroup)
   */
  public void save(FlatGroup flatGroup) {
    HibernateSession.byObjectStatic().save(flatGroup);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO#delete(edu.internet2.middleware.grouper.flat.FlatGroup)
   */
  public void delete(FlatGroup flatGroup) {
    HibernateSession.byObjectStatic().delete(flatGroup);
  }
  
  /**
   * reset flat group
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from FlatGroup").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO#findById(java.lang.String)
   */
  public FlatGroup findById(String flatGroupId) {
    FlatGroup flatGroup = HibernateSession
      .byHqlStatic()
      .createQuery("select flatGroup from FlatGroup as flatGroup where flatGroup.id = :id")
      .setCacheable(true).setCacheRegion(KLASS + ".FindById")
      .setString("id", flatGroupId)
      .uniqueResult(FlatGroup.class);
    
    return flatGroup;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.FlatGroupDAO#removeGroupForeignKey(java.lang.String)
   */
  public void removeGroupForeignKey(String flatGroupId) {
    HibernateSession.byHqlStatic()
      .createQuery("update FlatGroup set groupId = null where id = :id")
      .setString("id", flatGroupId)
      .executeUpdate();
  }
  
}

