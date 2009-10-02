/*
 * @author mchyzer
 * $Id: AttributeDefNameSetDAO.java,v 1.8 2009-10-02 05:57:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.exception.AttributeDefNameSetNotFoundException;
import edu.internet2.middleware.grouper.permissions.role.Role;

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
  
  /**
   * delete attributeDefName sets by owner, so the attributeDefName can be deleted
   * @param attributeDefName
   */
  public void deleteByIfHasAttributeDefName(AttributeDefName attributeDefName);
  
  /**
   * get all the IF rows from attributeDefNameSet about this id.  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @param attributeDefNameId
   * @return the AttributeDefName
   */
  public Set<AttributeDefName> attributeDefNamesThatImplyThis(String attributeDefNameId);

  /**
   * get all the IF rows from attributeDefNameSet about this id (immediate only).  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @param attributeDefNameId
   * @return the AttributeDefName
   */
  public Set<AttributeDefName> attributeDefNamesThatImplyThisImmediate(String attributeDefNameId);

  /**
   * get all the THEN rows from attributeDefNameSet about this id.  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @param attributeDefNameId
   * @return the AttributeDefName
   */
  public Set<AttributeDefName> attributeDefNamesImpliedByThis(String attributeDefNameId);

  /**
   * get all the THEN rows from attributeDefNameSet about this id (immediate).  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @param attributeDefNameId
   * @return the AttributeDefName
   */
  public Set<AttributeDefName> attributeDefNamesImpliedByThisImmediate(String attributeDefNameId);


}
