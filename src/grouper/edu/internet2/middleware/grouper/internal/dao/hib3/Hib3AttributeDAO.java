/*
 * @author mchyzer
 * $Id: Hib3AttributeDAO.java,v 1.4 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * marker class for hbm loading
 */
public class Hib3AttributeDAO implements AttributeDAO {

  /**
   */
  private static final String KLASS = Hib3AttributeDAO.class.getName();
  /**
   * logger
   */
  @SuppressWarnings("unused")
  private static final Log LOG = GrouperUtil.getLog(Hib3GroupDAO.class);

  /**
   * @param uuid 
   * @return map
   * @throws GrouperDAOException 
   * @since   @HEAD@
   */
  public Map<String, Attribute> findAllAttributesByGroup(final String uuid) throws  GrouperDAOException {
    final Map attrs = new HashMap();

    List<Attribute> hib3Attributes = HibernateSession.byHqlStatic()
      .setGrouperTransactionType(GrouperTransactionType.READONLY_OR_USE_EXISTING)
      .createQuery("from Attribute as a where a.groupUuid = :uuid")
      .setCacheable(false).setCacheRegion(KLASS + ".FindAllAttributesByGroup")
      .setString("uuid", uuid).list(Attribute.class);
    
    for (Attribute attribute : hib3Attributes) {
      attrs.put( attribute.getAttrName(), attribute );
    }
    return attrs;
  } 

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDAO#createOrUpdate(edu.internet2.middleware.grouper.Attribute)
   */
  public void createOrUpdate(Attribute attribute) {
    HibernateSession.byObjectStatic().saveOrUpdate(attribute);    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDAO#delete(edu.internet2.middleware.grouper.Attribute)
   */
  public void delete(Attribute attribute) {
    HibernateSession.byObjectStatic().delete(attribute);    
  }
}
