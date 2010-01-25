/*
 * @author mchyzer
 * $Id: AttributeDefNameDAO.java,v 1.6 2009-10-20 14:55:50 shilen Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.AttributeDefNameNotFoundException;

/**
 * attribute def name data access methods
 */
public interface AttributeDefNameDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute def name object 
   * @param attributeDefName
   */
  public void saveOrUpdate(AttributeDefName attributeDefName);
  
  /**
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute def name or null if not there
   */
  public AttributeDefName findByIdSecure(String id, boolean exceptionIfNotFound);
  
  /**
   * find an attribute def name by name
   * @param name 
   * @param exceptionIfNotFound 
   * @return  name
   * @throws GrouperDAOException 
   * @throws AttributeDefNameNotFoundException 
   */
  public AttributeDefName findByNameSecure(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, AttributeDefNameNotFoundException;

  /**
   * delete this attribute def name
   * @param attributeDefName 
   */
  public void delete(AttributeDefName attributeDefName);
  
  /**
   * Find all that have the given stem id.
   * @param id
   * @return set of stems
   */
  public Set<AttributeDefName> findByStem(String id);
 
  /**
   * find a record by uuid or name
   * @param id
   * @param name
   * @param exceptionIfNotFound
   * @return the attribute def name
   */
  public AttributeDefName findByUuidOrName(String id, String name, boolean exceptionIfNotFound);

  /**
   * save the update properties which are auto saved when business method is called
   * @param attributeDefName
   */
  public void saveUpdateProperties(AttributeDefName attributeDefName);

}
