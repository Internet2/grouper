package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.exception.AttributeDefNameSetNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefNameSetDAO;

/**
 * Data Access Object for attribute def name set
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefNameSetDAO.java,v 1.2 2009-07-03 21:15:13 mchyzer Exp $
 */
public class Hib3AttributeDefNameSetDAO extends Hib3DAO implements AttributeDefNameSetDAO {
  
  /**
   * 
   */
  @SuppressWarnings("unused")
  private static final String KLASS = Hib3AttributeDefNameSetDAO.class.getName();

  /**
   * reset the attribute def scopes
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeDefNameSet").executeUpdate();
  }

  /**
   * retrieve by id
   */
  public AttributeDefNameSet findById(String id, boolean exceptionIfNotFound) {
    AttributeDefNameSet attributeDefNameSet = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDefNameSet where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDefNameSet.class);
    if (attributeDefNameSet == null && exceptionIfNotFound) {
      throw new AttributeDefNameSetNotFoundException("Cant find attribute def name set by id: " + id);
    }
    return attributeDefNameSet;
  }

  /**
   * save or update
   */
  public void saveOrUpdate(AttributeDefNameSet attributeDefNameSet) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDefNameSet);
  }

  /**
   * @see AttributeDefNameSetDAO#findByIfHasAttributeDefNameId(String)
   */
  public Set<AttributeDefNameSet> findByIfHasAttributeDefNameId(String id) {
    Set<AttributeDefNameSet> attributeDefNameSets = HibernateSession.byHqlStatic().createQuery(
      "from AttributeDefNameSet where ifHasAttributeDefNameId = :theId")
      .setString("theId", id).listSet(AttributeDefNameSet.class);
    return attributeDefNameSets;

  }

  /**
   * @see AttributeDefNameSetDAO#findByThenHasAttributeDefNameId(String)
   */
  public Set<AttributeDefNameSet> findByThenHasAttributeDefNameId(String id) {
    Set<AttributeDefNameSet> attributeDefNameSets = HibernateSession.byHqlStatic().createQuery(
      "from AttributeDefNameSet where thenHasAttributeDefNameId = :theId")
      .setString("theId", id).listSet(AttributeDefNameSet.class);
    return attributeDefNameSets;

  }

} 

