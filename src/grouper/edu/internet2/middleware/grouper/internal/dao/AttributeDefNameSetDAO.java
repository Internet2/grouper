/*
 * @author mchyzer
 * $Id: AttributeDefNameSetDAO.java,v 1.5 2009-09-17 04:19:15 mchyzer Exp $
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
   * insert or update an attribute def name set
   * @param attributeDefNameSet 
   */
  public void saveOrUpdate(AttributeDefNameSet attributeDefNameSet);
  
  /** 
   * delete an attribute def name set
   * @param attributeDefNameSet 
   */
  public void delete(AttributeDefNameSet attributeDefNameSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the attribute def name set or null if not there
   * @throws AttributeDefNameSetNotFoundException 
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

  /**
   * <pre>
   * this will help with deletes.  It will find sets who have if's which match thens provided, and thens which 
   * match ifs provided.
   * 
   * So if there is this path: A -> B -> C -> D
   * And the inputs here are B and C (removing that path)
   * Then return A -> C, A -> D, B -> C, B -> D
   * 
   * </pre>
   * @param attributeDefNameSetForThens
   * @param attributeDefNameSetForIfs
   * @return the attribute def name set or null if not there
   */
  public Set<AttributeDefNameSet> findByIfThenHasAttributeDefNameId(String attributeDefNameSetForThens, 
      String attributeDefNameSetForIfs);

  /**
   * find by if and then (not same) with depth of 1 (immediate)
   * @param attributeDefNameIdIf
   * @param attributeDefNameIdThen
   * @param exceptionIfNotFound 
   * @return the attributeDefNameSet
   * @throws AttributeDefNameSetNotFoundException 
   */
  public AttributeDefNameSet findByIfThenImmediate(String attributeDefNameIdIf, 
      String attributeDefNameIdThen, boolean exceptionIfNotFound) throws AttributeDefNameSetNotFoundException;
  
}
