package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.exception.AttributeAssignActionNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO;

/**
 * Data Access Object for attribute assign action
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignActionDAO.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
public class Hib3AttributeAssignActionDAO extends Hib3DAO implements AttributeAssignActionDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeAssignActionDAO.class.getName();

  /**
   * reset the attribute defs
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeAssignAction").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#findById(java.lang.String, boolean)
   */
  public AttributeAssignAction findById(String id, boolean exceptionIfNotFound) {
    AttributeAssignAction attributeAssignAction = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssignAction where id = :theId")
      .setString("theId", id).uniqueResult(AttributeAssignAction.class);
    if (attributeAssignAction == null && exceptionIfNotFound) {
      throw new AttributeAssignActionNotFoundException("Cant find attribute assign action by id: " + id);
   }

    return attributeAssignAction;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction)
   */
  public void saveOrUpdate(AttributeAssignAction attributeAssignAction) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignAction);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#delete(edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction)
   */
  public void delete(AttributeAssignAction attributeAssignAction) {
    HibernateSession.byObjectStatic().delete(attributeAssignAction);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignActionDAO#findByAttributeDefId(java.lang.String)
   */
  public Set<AttributeAssignAction> findByAttributeDefId(String attributeDefId) {
    Set<AttributeAssignAction> attributeAssignActions = HibernateSession.byHqlStatic().createQuery(
      "from AttributeAssignAction where attributeDefId = :theAttributeDefId")
      .setString("theAttributeDefId", attributeDefId).listSet(AttributeAssignAction.class);
    
    return attributeAssignActions;
    
  }

} 

