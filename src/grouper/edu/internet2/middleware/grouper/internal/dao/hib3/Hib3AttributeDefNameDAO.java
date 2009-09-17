package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * Data Access Object for attribute def name
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefNameDAO.java,v 1.3 2009-09-17 17:51:50 mchyzer Exp $
 */
public class Hib3AttributeDefNameDAO extends Hib3DAO implements AttributeDefNameDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeDefNameDAO.class.getName();

  /**
   * reset the attribute def names
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeDefName").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findById(java.lang.String, boolean)
   */
  public AttributeDefName findById(String id, boolean exceptionIfNotFound) {
    AttributeDefName attributeDefName = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefName where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefName.class);
    if (attributeDefName == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cant find attribute def name by id: " + id);
   }

    return attributeDefName;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.AttributeDefName)
   */
  public void saveOrUpdate(AttributeDefName attributeDefName) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefName);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByName(java.lang.String, boolean)
   */
  public AttributeDefName findByName(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, AttributeDefNameNotFoundException {
    AttributeDefName attributeDefName = HibernateSession.byHqlStatic()
      .createQuery("select a from AttributeDefName as a where a.nameDb = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(AttributeDefName.class);

    //handle exceptions out of data access method...
    if (attributeDefName == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cannot find attribute def name with name: '" + name + "'");
    }
    return attributeDefName;
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#delete(AttributeDefName)
   */
  public void delete(final AttributeDefName attributeDefName) {

    HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING, 
        AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

          public Object callback(HibernateHandlerBean hibernateHandlerBean)
              throws GrouperDAOException {

            //set parent to null so mysql doest get mad
            //http://bugs.mysql.com/bug.php?id=15746
            // delete group sets
            GrouperDAOFactory.getFactory().getAttributeDefNameSet().deleteByIfHasAttributeDefName(attributeDefName);
            hibernateHandlerBean.getHibernateSession().byObject().delete(attributeDefName);
            return null;

          }
      
    });

  }

} 

