/*
 * @author mchyzer
 * $Id: AttributeDefDAO.java,v 1.6 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

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
   * find by id.  This is NOT a secure method, a grouperSession does not need to be open
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute def or null if not there
   */
  public AttributeDef findById(String id, boolean exceptionIfNotFound);
  
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
  
  /**
   * Find all that have the given stem id.
   * @param id
   * @return set of stems
   */
  public Set<AttributeDef> findByStem(String id);

}
