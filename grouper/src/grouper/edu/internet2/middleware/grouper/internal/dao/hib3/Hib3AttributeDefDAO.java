package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeDefDAO.java,v 1.3 2009-09-28 05:06:46 mchyzer Exp $
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
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByIdSecure(java.lang.String, boolean)
   */
  public AttributeDef findByIdSecure(String id, boolean exceptionIfNotFound) {
    AttributeDef attributeDef = HibernateSession.byHqlStatic().createQuery(
        "from AttributeDef where id = :theId")
      .setString("theId", id).uniqueResult(AttributeDef.class);

    //make sure grouper session can view the attribute def
    attributeDef = filterSecurity(attributeDef);
    
    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cant find (or not allowed to find) AttributeDef by id: " + id);
    }
    
    return attributeDef;
  }

  /**
   * make sure grouper session can view the attribute def
   * @param attributeDefs
   * @return the set of attribute defs
   */
  static Set<AttributeDef> filterSecurity(Set<AttributeDef> attributeDefs) {
    Set<AttributeDef> result = new LinkedHashSet<AttributeDef>();
    if (attributeDefs != null) {
      for (AttributeDef attributeDef : attributeDefs) {
        attributeDef = filterSecurity(attributeDef);
        if (attributeDef != null) {
          result.add(attributeDef);
        }
      }
    }
    return result;
  }
  
  /**
   * make sure grouper session can view the attribute def
   * @param attributeDef
   * @return the attributeDef or null
   */
  static AttributeDef filterSecurity(AttributeDef attributeDef) {
    if (attributeDef == null) {
      return null;
    }
    GrouperSession grouperSession = GrouperSession.staticGrouperSession();
    
    if ( PrivilegeHelper.canAttrView( grouperSession.internal_getRootSession(), attributeDef, grouperSession.getSubject() ) ) {
      return attributeDef;
    }
    return null;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.AttributeDef)
   */
  public void saveOrUpdate(AttributeDef attributeDef) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeDef);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDefDAO#findByNameSecure(java.lang.String, boolean)
   */
  public AttributeDef findByNameSecure(String name, boolean exceptionIfNotFound)
      throws GrouperDAOException, AttributeDefNotFoundException {
    AttributeDef attributeDef = HibernateSession.byHqlStatic()
      .createQuery("select a from AttributeDef as a where a.nameDb = :value")
      .setCacheable(false)
      .setCacheRegion(KLASS + ".FindByName")
      .setString("value", name).uniqueResult(AttributeDef.class);

    //make sure grouper session can view the attribute def
    attributeDef = filterSecurity(attributeDef);
    
    //handle exceptions out of data access method...
    if (attributeDef == null && exceptionIfNotFound) {
      throw new AttributeDefNotFoundException("Cannot find (or not allowed to find) attribute def with name: '" + name + "'");
    }
    return attributeDef;
  }

} 

