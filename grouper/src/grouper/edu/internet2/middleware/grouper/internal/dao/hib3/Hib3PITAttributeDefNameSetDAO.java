package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeDefNameSetDAO extends Hib3DAO implements PITAttributeDefNameSetDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeDefNameSetDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet)
   */
  public void saveOrUpdate(PITAttributeDefNameSet pitAttributeDefNameSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeDefNameSet);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeDefNameSet)
   */
  public void delete(PITAttributeDefNameSet pitAttributeDefNameSet) {
    HibernateSession.byObjectStatic().delete(pitAttributeDefNameSet);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    //do this since mysql cant handle self-referential foreign keys
    hibernateSession.byHql().createQuery("update PITAttributeDefNameSet set parentAttrDefNameSetId = null where id not in (select attrDefNameSet.id from AttributeDefNameSet as attrDefNameSet)").executeUpdate();
    
    hibernateSession.byHql().createQuery("delete from PITAttributeDefNameSet where id not in (select attrDefNameSet.id from AttributeDefNameSet as attrDefNameSet)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeDefNameSetDAO#findById(java.lang.String)
   */
  public PITAttributeDefNameSet findById(String id) {
    PITAttributeDefNameSet pitAttributeDefNameSet = HibernateSession
      .byHqlStatic()
      .createQuery("select attrDefNameSet from PITAttributeDefNameSet as attrDefNameSet where attrDefNameSet.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeDefNameSet.class);
    
    return pitAttributeDefNameSet;
  }
}