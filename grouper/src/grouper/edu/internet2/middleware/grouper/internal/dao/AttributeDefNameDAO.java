/*
 * @author mchyzer
 * $Id: AttributeDefNameDAO.java,v 1.3 2009-09-17 04:19:15 mchyzer Exp $
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
  public AttributeDefName findById(String id, boolean exceptionIfNotFound);
  
  /**
   * find an attribute def name by name
   * @param name 
   * @param exceptionIfNotFound 
   * @return  name
   * @throws GrouperDAOException 
   * @throws AttributeDefNameNotFoundException 
   */
  public AttributeDefName findByName(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, AttributeDefNameNotFoundException;

}
