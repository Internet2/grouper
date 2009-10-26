/**
 * @author mchyzer
 * $Id: AttributeAssignActionSetDelegate.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.io.Serializable;
import java.util.Set;

import edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;


/**
 * delegate the attribute assign action set
 */
@SuppressWarnings("serial")
public class AttributeAssignActionSetDelegate implements Serializable {

  /** keep a reference to the attribute assign action */
  private AttributeAssignAction attributeAssignAction;
  
  /**
   * 
   * @param attributeAssignAction1
   */
  public AttributeAssignActionSetDelegate(AttributeAssignAction attributeAssignAction1) {
    this.attributeAssignAction = attributeAssignAction1;

  }

  /**
   * get all the THEN rows from attributeAssignActionSet about this id.  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @return set of attributeAssignActionSets, or empty set if none available
   */
  public Set<AttributeAssignAction> getAttributeAssignActionsImpliedByThis() {
    return GrouperDAOFactory.getFactory().getAttributeAssignActionSet().attributeAssignActionsImpliedByThis(this.attributeAssignAction.getId());
  }

  /**
   * get all the THEN rows from attributeAssignActionSet about this id (immediate only).  The ones returned
   * are implied if this one is assigned.  Those are the children, this is the parent
   * @return set of attributeAssignActionSets, or empty set if none available
   */
  public Set<AttributeAssignAction> getAttributeAssignActionsImpliedByThisImmediate() {
    return GrouperDAOFactory.getFactory().getAttributeAssignActionSet().attributeAssignActionsImpliedByThisImmediate(this.attributeAssignAction.getId());
  }

  /**
   * get all the IF rows from attributeAssignActionSet about this id.  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @return set of attributeAssignActionSets, or empty set if none available
   */
  public Set<AttributeAssignAction> getAttributeAssignActionsThatImplyThis() {
    return GrouperDAOFactory.getFactory().getAttributeAssignActionSet()
      .attributeAssignActionsThatImplyThis(this.attributeAssignAction.getId());
  }

  /**
   * get all the IF rows from attributeAssignActionSet about this id (immediate only).  The ones returned imply that
   * this is also assigned.  Those are the parents, this is the child.
   * @return set of attributeAssignActions, or empty set if none available
   */
  public Set<AttributeAssignAction> getAttributeAssignActionsThatImplyThisImmediate() {
    return GrouperDAOFactory.getFactory().getAttributeAssignActionSet()
      .attributeAssignActionsThatImplyThisImmediate(this.attributeAssignAction.getId());
  }

  /**
   * 
   * @param newAttributeAssignAction
   * @return true if added, false if already there
   */
  public boolean addToAttributeAssignActionSet(AttributeAssignAction newAttributeAssignAction) {
    return GrouperSetEnum.ATTRIBUTE_ASSIGN_ACTION_SET.addToGrouperSet(this.attributeAssignAction, newAttributeAssignAction);
  }

  /**
   * 
   * @param attributeAssignActionToRemove
   * @return true if removed, false if already removed
   */
  public boolean removeFromAttributeAssignActionSet(AttributeAssignAction attributeAssignActionToRemove) {
    return GrouperSetEnum.ATTRIBUTE_ASSIGN_ACTION_SET.removeFromGrouperSet(this.attributeAssignAction, attributeAssignActionToRemove);
  }

}
