/*
 * @author mchyzer
 * $Id: AttributeAssignValueDAO.java,v 1.1 2009-06-28 19:02:17 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.attr.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.AttributeAssignValueNotFoundException;

/**
 * attribute assign value data access methods
 */
public interface AttributeAssignValueDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute assign value object 
   * @param attributeAssignValue 
   */
  public void saveOrUpdate(AttributeAssignValue attributeAssignValue);
  
  /**
   * @param id
   * @return the attribute assign value or null if not there
   */
  public AttributeAssignValue findById(String id, boolean exceptionIfNotFound)
    throws AttributeAssignValueNotFoundException;

}
