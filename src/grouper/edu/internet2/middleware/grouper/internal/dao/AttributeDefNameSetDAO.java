/*
 * @author mchyzer
 * $Id: AttributeDefNameSetDAO.java,v 1.2 2009-07-03 21:15:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.exception.AttributeDefNameSetNotFoundException;

/**
 * attribute def name set, links up attributes with other attributes (probably for privs)
 */
public interface AttributeDefNameSetDAO extends GrouperDAO {
  
  /** 
   * insert or update an attribute def scope object 
   * @param attributeAssignValue 
   */
  public void saveOrUpdate(AttributeDefNameSet attributeDefNameSet);
  
  /**
   * @param id
   * @return the attribute def name set or null if not there
   */
  public AttributeDefNameSet findById(String id, boolean exceptionIfNotFound)
    throws AttributeDefNameSetNotFoundException;

  /**
   * find by set owner
   * @param id
   * @return the attribute def name set or null if not there
   */
  public Set<AttributeDefNameSet> findByIfHasAttributeDefNameId(String id);

  /**
   * find by member
   * @param id
   * @return the attribute def name set or null if not there
   */
  public Set<AttributeDefNameSet> findByThenHasAttributeDefNameId(String id);

  
  
}
