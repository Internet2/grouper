/*
 * @author mchyzer
 * $Id: AttributeAssignActionDAO.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.exception.AttributeAssignActionNotFoundException;

/**
 * attribute assign action data access methods
 */
public interface AttributeAssignActionDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute assign value object 
   * @param attributeAssignAction
   */
  public void saveOrUpdate(AttributeAssignAction attributeAssignAction);
  
  /** 
   * delete the attribute assign action
   * @param attributeAssignAction
   */
  public void delete(AttributeAssignAction attributeAssignAction);
  
  /** 
   * find all actions for an attribute def by id
   * @param attributeDefId
   * @return the actions
   */
  public Set<AttributeAssignAction> findByAttributeDefId(String attributeDefId);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the attribute assign value or null if not there
   * @throws AttributeAssignActionNotFoundException 
   */
  public AttributeAssignAction findById(String id, boolean exceptionIfNotFound)
    throws AttributeAssignActionNotFoundException;

}
