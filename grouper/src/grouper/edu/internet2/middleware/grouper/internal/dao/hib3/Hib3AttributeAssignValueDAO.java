package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.AttributeAssignValueNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignValueDAO.java,v 1.2 2009-09-28 05:06:46 mchyzer Exp $
 */
public class Hib3AttributeAssignValueDAO extends Hib3DAO implements AttributeAssignValueDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeAssignValueDAO.class.getName();

  /**
   * reset the attribute defs
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeAssignValue").executeUpdate();
  }

  /**
   * retrieve by id
   */
  public AttributeAssignValue findById(String id, boolean exceptionIfNotFound) {
    AttributeAssignValue attributeAssignValue = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssignValue where id = :theId")
      .setString("theId", id).uniqueResult(AttributeAssignValue.class);
    if (attributeAssignValue == null && exceptionIfNotFound) {
      throw new AttributeAssignValueNotFoundException("Cant find attribute assign value by id: " + id);
   }

    return attributeAssignValue;
  }

  /**
   * save or update
   */
  public void saveOrUpdate(AttributeAssignValue attributeAssignValue) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValue);
  }

} 

