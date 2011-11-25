/*
 * @author mchyzer
 * $Id: Hib3AttributeDAO.java,v 1.4 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibUtils;
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
   * @see AttributeDAO#findAllAttributesByGroups(Collection)
   */
  public Map<String, Map<String, Attribute>> findAllAttributesByGroups(final Collection<String> uuids) throws  GrouperDAOException {
    
    final Map<String, Map<String, Attribute>> result = new HashMap<String, Map<String, Attribute>>();

    for (String uuid : GrouperUtil.nonNull(uuids)) {
        result.put(uuid, new HashMap<String, Attribute>());
    }
    
    //lets page through these
    int batchSize = 150;
    List<String> uuidsList = GrouperUtil.listFromCollection(uuids);
    int pages = GrouperUtil.batchNumberOfBatches(uuidsList, batchSize);

    //break into pages
    for (int i=0; i<pages; i++) {
      List<String> uuidPageList = GrouperUtil.batchList(uuidsList, batchSize, i);
      
      ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
      
      //do an in clause with bind vars
      String inClause = HibUtils.convertToInClause(uuidPageList, byHqlStatic);
      
      StringBuilder hql = new StringBuilder("from Attribute as a where a.groupUuid in (").append(inClause).append(")");

      List<Attribute> hib3Attributes = byHqlStatic
        .setGrouperTransactionType(GrouperTransactionType.READONLY_OR_USE_EXISTING)
        .createQuery(hql.toString())
        .setCacheable(false).setCacheRegion(KLASS + ".FindAllAttributesByGroups")
        .list(Attribute.class);
      
      //put into a map of maps
      for (Attribute attribute : GrouperUtil.nonNull(hib3Attributes)) {
        
        Map<String, Attribute> attributeMap = result.get(attribute.getGroupUuid());
        attributeMap.put(attribute.getGroupUuid(), attribute);
      }
      
    }
    
    return result;
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

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDAO#findByUuidOrName(java.lang.String, java.lang.String, java.lang.String, boolean)
   */
  public Attribute findByUuidOrName(String id, String groupUUID, String fieldId,
      boolean exceptionIfNotFound) {
    try {
      Attribute attribute = HibernateSession.byHqlStatic()
        .setGrouperTransactionType(GrouperTransactionType.READONLY_OR_USE_EXISTING)
        .createQuery("from Attribute as theAttribute where theAttribute.id = :theId " +
        		"or (theAttribute.groupUuid = :theGroupUuid and theAttribute.fieldId = :theFieldId) ")
        .setCacheable(false).setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .setString("theGroupUuid", groupUUID)
        .setString("theFieldId", fieldId)
        .uniqueResult(Attribute.class);

      if (attribute == null && exceptionIfNotFound) {
        throw new GroupNotFoundException("Can't find attribute by id: '" + id 
            + "' or groupId: '" + groupUUID + "', fieldId: '" + fieldId + "'");
      }
      return attribute;
    }
    catch (GrouperDAOException e) {
      String error = "Can't find attribute by id: '" + id 
        + "' or groupId: '" + groupUUID + "', fieldId: '" + fieldId + "', " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeDAO#saveUpdateProperties(edu.internet2.middleware.grouper.Attribute)
   */
  public void saveUpdateProperties(Attribute attribute) {
    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update Attribute " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attribute.getHibernateVersionNumber())
        .setString("theContextId", attribute.getContextId())
        .setString("theId", attribute.getId()).executeUpdate();
  }

}
