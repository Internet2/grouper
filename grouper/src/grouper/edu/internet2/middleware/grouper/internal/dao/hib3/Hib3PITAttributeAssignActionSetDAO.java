package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignActionSetDAO extends Hib3DAO implements PITAttributeAssignActionSetDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignActionSetDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet)
   */
  public void saveOrUpdate(PITAttributeAssignActionSet pitAttributeAssignActionSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignActionSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssignActionSet)
   */
  public void delete(PITAttributeAssignActionSet pitAttributeAssignActionSet) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssignActionSet);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITAttributeAssignActionSet set parentAttrAssignActionSetId = null where id not in (select actionSet.id from AttributeAssignActionSet as actionSet)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeAssignActionSet where id not in (select actionSet.id from AttributeAssignActionSet as actionSet)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignActionSetDAO#findById(java.lang.String)
   */
  public PITAttributeAssignActionSet findById(String id) {
    PITAttributeAssignActionSet pitAttributeAssignActionSet = HibernateSession
      .byHqlStatic()
      .createQuery("select actionSet from PITAttributeAssignActionSet as actionSet where actionSet.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignActionSet.class);
    
    return pitAttributeAssignActionSet;
  }
}