/*
 * @author mchyzer
 * $Id: AttributeDefNameDAO.java,v 1.1 2009-06-24 06:22:24 mchyzer Exp $
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
   * @return the attribute def name or null if not there
   */
  public AttributeDefName retrieveById(String id);
  
  /**
   * find an attribute def name by name
   */
  public AttributeDefName findByName(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, AttributeDefNameNotFoundException;

}
