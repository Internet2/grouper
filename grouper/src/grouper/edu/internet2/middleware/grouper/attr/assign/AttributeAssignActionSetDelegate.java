/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * @author mchyzer
 * $Id: AttributeAssignActionSetDelegate.java,v 1.1 2009-10-26 02:26:07 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.grouperSet.GrouperSetEnum;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


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
   * get action names implied by this
   * @return names
   */
  public Set<String> getAttributeAssignActionNamesImpliedByThis() {
    
    Set<String> actions = new HashSet<String>();
    for (AttributeAssignAction attributeAssignAction : GrouperUtil.nonNull(this.getAttributeAssignActionsImpliedByThis())) {
      actions.add(attributeAssignAction.getName());
    }
    return actions;
    
  }
  
  /**
   * get action names implied by this immediate
   * @return names
   */
  public Set<String> getAttributeAssignActionNamesImpliedByThisImmediate() {
    
    Set<String> actions = new HashSet<String>();
    for (AttributeAssignAction attributeAssignAction : GrouperUtil.nonNull(this.getAttributeAssignActionsImpliedByThisImmediate())) {
      actions.add(attributeAssignAction.getName());
    }
    return actions;
    
  }
  
  /**
   * get action names that imply this immediate
   * @return names
   */
  public Set<String> getAttributeAssignActionNamesThatImplyThisImmediate() {
    
    Set<String> actions = new HashSet<String>();
    for (AttributeAssignAction attributeAssignAction : GrouperUtil.nonNull(this.getAttributeAssignActionsThatImplyThisImmediate())) {
      actions.add(attributeAssignAction.getName());
    }
    return actions;
    
  }
  
  /**
   * get action names that imply this
   * @return names
   */
  public Set<String> getAttributeAssignActionNamesThatImplyThis() {
    
    Set<String> actions = new HashSet<String>();
    for (AttributeAssignAction attributeAssignAction : GrouperUtil.nonNull(this.getAttributeAssignActionsThatImplyThis())) {
      actions.add(attributeAssignAction.getName());
    }
    return actions;
    
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
   * return actions which can imply this, i.e. they already do not
   * @return the set of action strings
   */
  public Set<String> getNewAttributeAssignActionNamesThatCanImplyThis() {
    //find list which can imply
    AttributeDef attributeDef = this.attributeAssignAction.getAttributeDef();

    Set<String> actionsWhichCanImply = attributeDef.getAttributeDefActionDelegate().allowedActionStrings();

    //remove self
    actionsWhichCanImply.remove(this.attributeAssignAction.getName());
    
    //remove existing direct relations
    Set<AttributeAssignAction> actionsWhichImplyThisImmediate = this.getAttributeAssignActionsThatImplyThisImmediate();
    
    for (AttributeAssignAction actionWhichImplyThisImmediate : actionsWhichImplyThisImmediate) {
      actionsWhichCanImply.remove(actionWhichImplyThisImmediate.getName());
    }
    return actionsWhichCanImply;
  }
  
  /**
   * return actions which can be implied this, i.e. they already do not
   * @return the set of action strings
   */
  public Set<String> getNewAttributeAssignActionNamesThatCanBeImpliedByThis() {
    //find list which can imply
    AttributeDef attributeDef = this.attributeAssignAction.getAttributeDef();

    Set<String> actionsWhichCanBeImplied = attributeDef.getAttributeDefActionDelegate().allowedActionStrings();

    //remove self
    actionsWhichCanBeImplied.remove(this.attributeAssignAction.getName());
    
    //remove existing direct relations
    Set<AttributeAssignAction> actionsWhichImpliedByThisImmediate = this.getAttributeAssignActionsImpliedByThisImmediate();
    
    for (AttributeAssignAction actionWhichImpliedByThisImmediate : actionsWhichImpliedByThisImmediate) {
      actionsWhichCanBeImplied.remove(actionWhichImpliedByThisImmediate.getName());
    }
    return actionsWhichCanBeImplied;
  }
  
  /**
   * add the param to this's set.  i.e. if this is "all", and the argument is "read", 
   * then after this call, then all will imply read
   * @param newAttributeAssignAction
   * @return true if added, false if already there
   */
  public boolean addToAttributeAssignActionSet(AttributeAssignAction newAttributeAssignAction) {
    return internal_addToAttributeAssignActionSet(newAttributeAssignAction, null);
  }
  
  /**
   * 
   * @param newAttributeAssignAction
   * @param uuid
   * @return true if added, false if already there
   */
  public boolean internal_addToAttributeAssignActionSet(AttributeAssignAction newAttributeAssignAction, String uuid) {
    return GrouperSetEnum.ATTRIBUTE_ASSIGN_ACTION_SET.addToGrouperSet(this.attributeAssignAction, newAttributeAssignAction, uuid);
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
