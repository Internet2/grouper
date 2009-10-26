/*
 * @author mchyzer
 * $Id: AttributeAssignActionSetDAO.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.internal.dao;

import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDefNameSet;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignActionSet;
import edu.internet2.middleware.grouper.exception.AttributeAssignActionSetNotFoundException;

/**
 * attribute assign action set, links up actions with other actions (probably for privs)
 */
public interface AttributeAssignActionSetDAO extends GrouperDAO {
  
  /** 
   * insert or update an attributeAssignActionSet
   * @param attributeAssignActionSet 
   */
  public void saveOrUpdate(AttributeAssignActionSet attributeAssignActionSet);
  
  /** 
   * delete an attribute assign action set
   * @param attributeAssignActionSet 
   */
  public void delete(AttributeAssignActionSet attributeAssignActionSet);
  
  /**
   * @param id
   * @param exceptionIfNotFound 
   * @return the attribute def name set or null if not there
   * @throws AttributeAssignActionSetNotFoundException 
   */
  public AttributeAssignActionSet findById(String id, boolean exceptionIfNotFound)
    throws AttributeAssignActionSetNotFoundException;

  /**
   * find by set owner
   * @param id
   * @return the attribute assign action set or null if not there
   */
  public Set<AttributeAssignActionSet> findByIfHasAttributeAssignActionId(String id);

  /**
   * find by member
   * @param id
   * @return the attribute assign action set or null if not there
   */
  public Set<AttributeAssignActionSet> findByThenHasAttributeAssignActionId(String id);

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
   * @param attributeAssignActionSetForThens 
   * @param attributeAssignActionSetForIfs 
   * @return the attribute assign action set or null if not there
   */
  public Set<AttributeAssignActionSet> findByIfThenHasAttributeAssignActionId(String attributeAssignActionSetForThens, 
      String attributeAssignActionSetForIfs);

  /**
   * find by if and then (not same) with depth of 1 (immediate)
   * @param attributeAssignActionIdIf
   * @param attributeAssignActionIdThen
   * @param exceptionIfNotFound 
   * @return the attributeAssignActionSet
   * @throws AttributeAssignActionSetNotFoundException 
   */
  public AttributeAssignActionSet findByIfThenImmediate(String attributeAssignActionIdIf, 
      String attributeAssignActionIdThen, boolean exceptionIfNotFound) throws AttributeAssignActionSetNotFoundException;
  
  /**
   * delete attributeAssignAction sets by owner, so the attributeAssignAction can be deleted
   * @param attributeAssignAction
   */
  public void deleteByIfHasAttributeAssignAction(AttributeAssignAction attributeAssignAction);
  
  /**
   * get all the IF rows from attributeAssignActionSet about this id.  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @param attributeAssignActionId
   * @return the AttributeAssignAction
   */
  public Set<AttributeAssignAction> attributeAssignActionsThatImplyThis(String attributeAssignActionId);

  /**
   * get all the IF rows from attributeDefNameSet about this id (immediate only).  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @param attributeAssignActionId
   * @return the attributeAssignActionId
   */
  public Set<AttributeAssignAction> attributeAssignActionsThatImplyThisImmediate(String attributeAssignActionId);

  /**
   * get all the THEN rows from attributeDefNameSet about this id.  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @param attributeAssignActionId
   * @return the AttributeAssignAction
   */
  public Set<AttributeAssignAction> attributeAssignActionsImpliedByThis(String attributeAssignActionId);

  /**
   * get all the THEN rows from attributeDefNameSet about this id (immediate).  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @param attributeAssignActionId
   * @return the AttributeDefName
   */
  public Set<AttributeAssignAction> attributeAssignActionsImpliedByThisImmediate(String attributeAssignActionId);


}
