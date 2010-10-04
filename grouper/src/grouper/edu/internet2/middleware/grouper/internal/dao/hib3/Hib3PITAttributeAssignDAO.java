package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeAssign;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignDAO extends Hib3DAO implements PITAttributeAssignDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssign)
   */
  public void saveOrUpdate(PITAttributeAssign pitAttributeAssign) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssign);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssign)
   */
  public void delete(PITAttributeAssign pitAttributeAssign) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssign);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITAttributeAssign set ownerAttributeAssignId = null where ownerAttributeAssignId is not null and id not in (select attrAssign.id from AttributeAssign as attrAssign)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeAssign where id not in (select attrAssign.id from AttributeAssign as attrAssign)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#findById(java.lang.String)
   */
  public PITAttributeAssign findById(String id) {
    PITAttributeAssign pitAttributeAssign = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssign from PITAttributeAssign as attrAssign where attrAssign.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssign.class);
    
    return pitAttributeAssign;
  }
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignDAO#updateId(java.lang.String, java.lang.String)
   */
  public void updateId(String oldId, String newId) {
    HibernateSession
      .byHqlStatic()
      .createQuery("update PITAttributeAssign set id = :newId where id = :oldId")
      .setString("oldId", oldId)
      .setString("newId", newId)
      .executeUpdate();
  }
}