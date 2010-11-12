package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO;
import edu.internet2.middleware.grouper.pit.PITAttributeAssignValue;

/**
 * @author shilen
 * $Id$
 */
public class Hib3PITAttributeAssignValueDAO extends Hib3DAO implements PITAttributeAssignValueDAO {

  /**
   *
   */
  private static final String KLASS = Hib3PITAttributeAssignValueDAO.class.getName();

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#saveOrUpdate(edu.internet2.middleware.grouper.pit.PITAttributeAssignValue)
   */
  public void saveOrUpdate(PITAttributeAssignValue pitAttributeAssignValue) {
    HibernateSession.byObjectStatic().saveOrUpdate(pitAttributeAssignValue);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#delete(edu.internet2.middleware.grouper.pit.PITAttributeAssignValue)
   */
  public void delete(PITAttributeAssignValue pitAttributeAssignValue) {
    HibernateSession.byObjectStatic().delete(pitAttributeAssignValue);
  }
  
  /**
   * reset
   * @param hibernateSession
   */
  public static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from PITAttributeAssignValue where id not in (select v.id from AttributeAssignValue as v)").executeUpdate();
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.PITAttributeAssignValueDAO#findById(java.lang.String)
   */
  public PITAttributeAssignValue findById(String id) {
    PITAttributeAssignValue pitAttributeAssignValue = HibernateSession
      .byHqlStatic()
      .createQuery("select attrAssignValue from PITAttributeAssignValue as attrAssignValue where attrAssignValue.id = :id")
      .setCacheable(false).setCacheRegion(KLASS + ".FindById")
      .setString("id", id)
      .uniqueResult(PITAttributeAssignValue.class);
    
    return pitAttributeAssignValue;
  }
}