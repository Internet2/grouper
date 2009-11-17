package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
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
 * @version $Id: Hib3AttributeDefNameDAO.java,v 1.6 2009-11-17 02:52:29 mchyzer Exp $
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
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByIdSecure(java.lang.String, boolean)
   */
  public AttributeDefName findByIdSecure(String id, boolean exceptionIfNotFound) {
    AttributeDefName attributeDefName = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefName where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefName.class);
    
    attributeDefName = filterSecurity(attributeDefName);
    
    if (attributeDefName == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cant find (or not allowed to find) attribute def name by id: " + id);
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
   * make sure grouper session can view the attribute def Name
   * @param attributeDefNames
   * @return the set of attribute def Names
   */
  static Set<AttributeDefName> filterSecurity(Set<AttributeDefName> attributeDefNames) {
    Set<AttributeDefName> result = new LinkedHashSet<AttributeDefName>();
    if (attributeDefNames != null) {
      for (AttributeDefName attributeDefName : attributeDefNames) {
        attributeDefName = filterSecurity(attributeDefName);
        if (attributeDefName != null) {
          result.add(attributeDefName);
        }
      }
    }
    return result;
  }
  
  /**
   * make sure grouper session can view the attribute def Name
   * @param attributeDefName
   * @return the attributeDefName or null
   */
  static AttributeDefName filterSecurity(AttributeDefName attributeDefName) {
    if (attributeDefName == null) {
      return null;
    }
    
    AttributeDef attributeDef = AttributeDefFinder.findById(attributeDefName.getAttributeDefId(), false);
    return attributeDef == null ? null : attributeDefName;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByNameSecure(java.lang.String, boolean)
   */
  public AttributeDefName findByNameSecure(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, AttributeDefNameNotFoundException {
    AttributeDefName attributeDefName = HibernateSession.byHqlStatic()
      .createQuery("select a from AttributeDefName as a where a.nameDb = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(AttributeDefName.class);

    attributeDefName = filterSecurity(attributeDefName);

    //handle exceptions out of data access method...
    if (attributeDefName == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cannot find (or not allowed to find) attribute def name with name: '" + name + "'");
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

            hibernateHandlerBean.getHibernateSession().setCachingEnabled(false);

            //set parent to null so mysql doest get mad
            //http://bugs.mysql.com/bug.php?id=15746
            // delete group sets
            GrouperDAOFactory.getFactory().getAttributeDefNameSet().deleteByIfHasAttributeDefName(attributeDefName);
            hibernateHandlerBean.getHibernateSession().byObject().delete(attributeDefName);
            return null;

          }
      
    });

  }
  

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefNameDAO#findByStem(java.lang.String)
   */
  public Set<AttributeDefName> findByStem(String id) {
    Set<AttributeDefName> attributeDefNames = HibernateSession.byHqlStatic()
        .createQuery("from AttributeDefName where stemId = :id")
        .setCacheable(false)
        .setCacheRegion(KLASS + ".FindByStem")
        .setString("id", id)
        .listSet(AttributeDefName.class);
    
    return attributeDefNames;
  }

} 

