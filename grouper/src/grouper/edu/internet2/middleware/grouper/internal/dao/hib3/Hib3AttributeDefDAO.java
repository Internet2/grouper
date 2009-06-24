package edu.internet2.middleware.grouper.internal.dao.hib3;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefDAO.java,v 1.1 2009-06-24 06:22:24 mchyzer Exp $
 */
public class Hib3AttributeDefDAO extends Hib3DAO implements AttributeDefDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeDefDAO.class.getName();

  /**
   * reset the attribute defs
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeDef").executeUpdate();
  }

  /**
   * retrieve by id
   */
  public AttributeDef retrieveById(String id) {
    AttributeDef attributeDef = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDef where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDef.class);
    return attributeDef;
  }

  /**
   * save or update
   */
  public void saveOrUpdate(AttributeDef attributeDef) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDef);
  }

  /**
   * @param name
   * @param exceptionIfNotFound
   * @return group
   * @throws GrouperDAOException
   * @throws GroupNotFoundException
   */
  public AttributeDef findByName(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, AttributeDefNotFoundException {
    AttributeDef attributeDef = HibernateSession.byHqlStatic()
      .createQuery("select a from AttributeDef as a where a.nameDb = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(AttributeDef.class);

    //handle exceptions out of data access method...
    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cannot find attribute def with name: '" + name + "'");
    }
    return attributeDef;
  }

} 

