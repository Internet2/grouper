package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.List;
import java.util.Set;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.attr.AttributeAssign;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignDAO.java,v 1.2 2009-09-21 06:14:26 mchyzer Exp $
 */
public class Hib3AttributeAssignDAO extends Hib3DAO implements AttributeAssignDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeAssignDAO.class.getName();

  /**
   * reset the attribute assigns
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeAssign").executeUpdate();
  }

  /**
   * retrieve by id
   */
  public AttributeAssign findById(String id, boolean exceptionIfNotFound) {
    AttributeAssign attributeAssign = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssign where id = :theId")
      .setString("theId", id).uniqueResult(AttributeAssign.class);
    if (attributeAssign == null && exceptionIfNotFound) {
      throw new AttributeAssignNotFoundException("Cant find attribute assign by id: " + id);
   }

    return attributeAssign;
  }

  /**
   * save or update
   */
  public void saveOrUpdate(AttributeAssign attributeAssign) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssign);
  }



  
} 


