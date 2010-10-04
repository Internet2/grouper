package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignAction;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignActionDAO extends Hib3DAO implements PITAttributeAssignActionDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignActionDAO.class.getName();

  /**
   *  @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssignAction)
   */
  public void saveOrUpdate(PITAttributeAssignAction pitAttributeAssignAction) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignAction);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssignAction)
   */
  public void delete(PITAttributeAssignAction pitAttributeAssignAction) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssignAction);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeAssignAction where id not in (select action.id from AttributeAssignAction as action)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionDAO#findById(java.lang.String)
   */
  public PITAttributeAssignAction findById(String id) {
    PITAttributeAssignAction pitAttributeAssignAction = HibernateSession
      .byHqlStatic()
      .createQuery("select action from PITAttributeAssignAction as action where action.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignAction.class);
    
    return pitAttributeAssignAction;
  }
}