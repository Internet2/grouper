/*
 * @author mchyzer
 * $Id: AttributeDefScopeDAO.java,v 1.1 2009-06-29 15:58:24 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.attr.AttributeDefScope;
import edu.internet2.middleware.grouper.exception.AttributeDefScopeNotFoundException;

/**
 * attribute assign value data access methods
 */
public interface AttributeDefScopeDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute def scope object 
   * @param attributeAssignValue 
   */
  public void saveOrUpdate(AttributeDefScope attributeDefScope);
  
  /**
   * @param id
   * @return the attribute assign value or null if not there
   */
  public AttributeDefScope findById(String id, boolean exceptionIfNotFound)
    throws AttributeDefScopeNotFoundException;

}
