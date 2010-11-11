package edu.internet2.middleware.grouper.internal.dao.hib3;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.AttributeAssignValueNotFoundException;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Data Access Object for attribute def
 * @author  mchyzer
 * @version $Id: Hib3AttributeAssignValueDAO.java,v 1.2 2009-09-28 05:06:46 mchyzer Exp $
 */
public class Hib3AttributeAssignValueDAO extends Hib3DAO implements AttributeAssignValueDAO {
  
  /**
   * 
   */
  private static final String KLASS = Hib3AttributeAssignValueDAO.class.getName();

  /**
   * reset the attribute defs
   * @param hibernateSession
   */
  static void reset(HibernateSession hibernateSession) {
    hibernateSession.byHql().createQuery("delete from AttributeAssignValue").executeUpdate();
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#findById(java.lang.String, boolean)
   */
  public AttributeAssignValue findById(String id, boolean exceptionIfNotFound) {
    AttributeAssignValue attributeAssignValue = HibernateSession.byHqlStatic().createQuery(
        "from AttributeAssignValue where id = :theId")
      .setString("theId", id).uniqueResult(AttributeAssignValue.class);
    if (attributeAssignValue == null && exceptionIfNotFound) {
      throw new AttributeAssignValueNotFoundException("Cant find attribute assign value by id: " + id);
   }

    return attributeAssignValue;
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#saveOrUpdate(edu.internet2.middleware.grouper.attr.value.AttributeAssignValue)
   */
  public void saveOrUpdate(AttributeAssignValue attributeAssignValue) {
    HibernateSession.byObjectStatic().saveOrUpdate(attributeAssignValue);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#findByUuidOrKey(java.util.Collection, java.lang.String, java.lang.String, boolean, java.lang.Long, java.lang.String, java.lang.String)
   */
  public AttributeAssignValue findByUuidOrKey(Collection<String> idsToIgnore, String id,
      String attributeAssignId, boolean exceptionIfNull, Long valueInteger,
      String valueMemberId, String valueString) throws GrouperDAOException {
    try {
      Set<AttributeAssignValue> attributeAssignValues = HibernateSession.byHqlStatic()
        .createQuery("from AttributeAssignValue as theAttributeAssignValue where " +
            "theAttributeAssignValue.id = :theId or theAttributeAssignValue.attributeAssignId = :theAttributeAssignId")
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByUuidOrName")
        .setString("theId", id)
        .setString("theAttributeAssignId", attributeAssignId)
        .listSet(AttributeAssignValue.class);
      if (GrouperUtil.length(attributeAssignValues) == 0) {
        if (exceptionIfNull) {
          throw new RuntimeException("Can't find attributeAssignValue by id: '" + id + "' or attributeAssignId '" + attributeAssignId 
              + "'");
        }
        return null;
      }
      
      idsToIgnore = GrouperUtil.nonNull(idsToIgnore);
      
      //lets remove ones we have already processed or will process
      Iterator<AttributeAssignValue> iterator = attributeAssignValues.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssignValue attributeAssignValue = iterator.next();
        if (idsToIgnore.contains(attributeAssignValue.getId())) {
          iterator.remove();
        }
      }
      
      //first case, the ID matches
      iterator = attributeAssignValues.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssignValue attributeAssignValue = iterator.next();
        if (StringUtils.equals(id, attributeAssignValue.getId())) {
          return attributeAssignValue;
        }
      }

      //second case, the value matches
      iterator = attributeAssignValues.iterator();
      while (iterator.hasNext()) {
        
        AttributeAssignValue attributeAssignValue = iterator.next();
        if (StringUtils.equals(valueString, attributeAssignValue.getValueString())
            && StringUtils.equals(valueMemberId, attributeAssignValue.getValueMemberId())
            && GrouperUtil.equals(valueInteger, attributeAssignValue.getValueInteger())) {
          return attributeAssignValue;
        }
      }
      
      //ok, if there is one left, return it
      if (attributeAssignValues.size() > 0) {
        return attributeAssignValues.iterator().next();
      }
      
      //cant find one
      return null;
    }
    catch (GrouperDAOException e) {
      String error = "Problem find attributeAssignValue by id: '" + id + "' or attributeAssignId '" + attributeAssignId 
            + "', valueString: " + valueString 
            + "', valueInteger: " + valueInteger + ", valueMemberId: " + valueMemberId + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#saveUpdateProperties(edu.internet2.middleware.grouper.attr.value.AttributeAssignValue)
   */
  public void saveUpdateProperties(AttributeAssignValue attributeAssignValue) {

    //run an update statement since the business methods affect these properties
    HibernateSession.byHqlStatic().createQuery("update AttributeAssignValue " +
        "set hibernateVersionNumber = :theHibernateVersionNumber, " +
        "contextId = :theContextId, " +
        "createdOnDb = :theCreatedOnDb, " +
        "lastUpdatedDb = :theLastUpdatedDb " +
        "where id = :theId")
        .setLong("theHibernateVersionNumber", attributeAssignValue.getHibernateVersionNumber())
        .setLong("theCreatedOnDb", attributeAssignValue.getCreatedOnDb())
        .setLong("theLastUpdatedDb", attributeAssignValue.getLastUpdatedDb())
        .setString("theContextId", attributeAssignValue.getContextId())
        .setString("theId", attributeAssignValue.getId()).executeUpdate();

  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#delete(edu.internet2.middleware.grouper.attr.value.AttributeAssignValue)
   */
  public void delete(AttributeAssignValue attributeAssignValue) {
    HibernateSession.byObjectStatic().delete(attributeAssignValue);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#findByAttributeAssignId(java.lang.String)
   */
  public Set<AttributeAssignValue> findByAttributeAssignId(String attributeAssignId) {
    return findByAttributeAssignId(attributeAssignId, null);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.AttributeAssignValueDAO#findByAttributeAssignId(java.lang.String, edu.internet2.middleware.grouper.internal.dao.QueryOptions)
   */
  public Set<AttributeAssignValue> findByAttributeAssignId(String attributeAssignId,
      QueryOptions queryOptions) {
    try {
      Set<AttributeAssignValue> attributeAssignValues = HibernateSession.byHqlStatic()
        .createQuery("from AttributeAssignValue as theAttributeAssignValue where " +
            "theAttributeAssignValue.attributeAssignId = :theAttributeAssignId")
        .options(queryOptions)
        .setCacheable(true)
        .setCacheRegion(KLASS + ".FindByAttributeAssignId")
        .setString("theAttributeAssignId", attributeAssignId)
        .listSet(AttributeAssignValue.class);
      
      //return result
      return attributeAssignValues;
    } catch (GrouperDAOException e) {
      String error = "Problem find attributeAssignValue by attributeAssignId '" + attributeAssignId 
             + ", " + e.getMessage();
      throw new GrouperDAOException( error, e );
    }

  }

} 

