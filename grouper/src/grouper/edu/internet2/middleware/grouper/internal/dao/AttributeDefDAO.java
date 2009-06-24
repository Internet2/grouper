/*
 * @author mchyzer
 * $Id: AttributeDefDAO.java,v 1.1 2009-06-24 06:22:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.AttributeDefNotFoundException;

/**
 * attribute def data access methods
 */
public interface AttributeDefDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute def object 
   * @param attributeDef 
   */
  public void saveOrUpdate(AttributeDef attributeDef);
  
  /**
   * @param id
   * @return the attribute def or null if not there
   */
  public AttributeDef retrieveById(String id);
  
  /**
   * find an attribute def by name
   */
  public AttributeDef findByName(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, AttributeDefNotFoundException;

}
