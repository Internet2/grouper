/*
 * @author mchyzer
 * $Id: AttributeAssignDAO.java,v 1.2 2009-09-21 06:14:26 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.attr.AttributeAssign;

/**
 * attribute assign data access methods
 */
public interface AttributeAssignDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute assign object 
   * @param attributeAssign 
   */
  public void saveOrUpdate(AttributeAssign attributeAssign);
  
  /**
   * @param id
   * @param exceptionIfNotFound
   * @return the attribute assign or null if not there
   */
  public AttributeAssign findById(String id, boolean exceptionIfNotFound);

  
}
