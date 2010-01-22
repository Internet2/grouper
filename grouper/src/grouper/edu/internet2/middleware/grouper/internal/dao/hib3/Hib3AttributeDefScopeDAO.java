package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.exception.AttributeDefScopeNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefScopeDAO;

/**
 * Data Access Object for attribute def scope
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefScopeDAO.java,v 1.1 2009-06-29 15:58:24 mchyzer Exp $
 */
public class Hib3AttributeDefScopeDAO extends Hib3DAO implements AttributeDefScopeDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeDefScopeDAO.class.getName();

  /**
   * reset the attribute def scopes
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeDefScope").executeUpdate();
  }

  /**
   * retrieve by id
   */
  public AttributeDefScope findById(String id, boolean exceptionIfNotFound) {
    AttributeDefScope attributeDefScope = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefScope where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefScope.class);
    if (attributeDefScope == null && exceptionIfNotFound) {
      throw new AttributeDefScopeNotFoundException("Cant find attribute def scope by id: " + id);
   }

    return attributeDefScope;
  }

  /**
   * save or update
   */
  public void saveOrUpdate(AttributeDefScope attributeDefScope) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefScope);
  }

} 

