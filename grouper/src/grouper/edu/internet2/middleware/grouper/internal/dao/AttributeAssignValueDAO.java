/*
 * @author mchyzer
 * $Id: AttributeAssignValueDAO.java,v 1.3 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssignValue;
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
   * @param exceptionIfNotFound 
   * @return the attribute assign value or null if not there
   * @throws AttributeAssignValueNotFoundException 
   */
  public AttributeAssignValue findById(String id, boolean exceptionIfNotFound)
    throws AttributeAssignValueNotFoundException;

}
