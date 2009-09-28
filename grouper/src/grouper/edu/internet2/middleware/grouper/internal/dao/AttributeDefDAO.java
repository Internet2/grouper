/*
 * @author mchyzer
 * $Id: AttributeDefDAO.java,v 1.4 2009-09-28 20:30:34 mchyzer Exp $
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
   * find by id.  This is a secure method, a grouperSession needs to be open
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute def or null if not there
   */
  public AttributeDef findByIdSecure(String id, boolean exceptionIfNotFound);
  
  /**
   * find by attributeDefNameId.  This is a secure method, a grouperSession needs to be open
   * @param attributeDefNameId
   * @param exceptionIfNotFound
   * @return the attribute def or null if not there
   */
  public AttributeDef findByAttributeDefNameIdSecure(String attributeDefNameId, boolean exceptionIfNotFound);
  
  /**
   * find an attribute def by name.  this is a secure method, a grouperSession needs to be open
   * @param name 
   * @param exceptionIfNotFound 
   * @return attribute def
   * @throws GrouperDAOException 
   * @throws AttributeDefNotFoundException 
   */
  public AttributeDef findByNameSecure(String name, boolean exceptionIfNotFound) 
    throws GrouperDAOException, AttributeDefNotFoundException;

}
