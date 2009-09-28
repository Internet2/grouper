/*
 * @author mchyzer
 * $Id: AttributeDefNameDAO.java,v 1.5 2009-09-28 05:06:46 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

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
  
}
