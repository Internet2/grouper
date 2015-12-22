/**
 * Copyright 2014 Internet2
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
 * $Id: AttributeDefActionDelegate.java,v 1.2 2009-11-06 13:39:59 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.AttributeAssignActionNotFoundException;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 * delegate the action management to this class
 */
public class AttributeDefActionDelegate {

  /** reference back to attribute def */
  private AttributeDef attributeDef;

  /**
   * 
   * @param attributeDef1
   */
  public AttributeDefActionDelegate(AttributeDef attributeDef1) {
    this.attributeDef = attributeDef1;
  }

  /**
   * configure the action list based on comma separated list of actions
   * @param list
   */
  public void configureActionList(String list) {
    String[] actions = GrouperUtil.splitTrim(list, ",");
    Set<String> actionSet = GrouperUtil.toSet(actions);
    this.configureActionList(actionSet);
  }
  
  /**
   * set of allowed actions
   */
  private Set<AttributeAssignAction> allowedActionsSet = null;

  /**
   * set of allowed actions
   */
  private Set<String> allowedActionStringSet = null;

  /**
   * get (and cache) the allowed actions
   * @return the set of strings
   */
  public Set<AttributeAssignAction> allowedActions() {
    if (this.allowedActionsSet == null) {
      this.allowedActionsSet = GrouperDAOFactory.getFactory().getAttributeAssignAction()
        .findByAttributeDefId(this.attributeDef.getId());
      
      //init the string set too
      this.allowedActionStringSet = new HashSet<String>();
      for (AttributeAssignAction attributeAssignAction : this.allowedActionsSet) {
        this.allowedActionStringSet.add(attributeAssignAction.getName());
      }
    }
    return this.allowedActionsSet;
  }
  
  /**
   * retrieve an action
   * @param name
   * @param exceptionIfNull
   * @return AttributeAssignAction
   */
  public AttributeAssignAction findAction(String name, boolean exceptionIfNull) {
    Set<AttributeAssignAction> actions = this.allowedActions();
    if (GrouperUtil.length(actions) == 0) {
      return null;
    }
    
    for (AttributeAssignAction attributeAssignAction : actions) {
      if (StringUtils.equals(name, attributeAssignAction.getName())) {
        return attributeAssignAction;
      }
    }
    
    if (exceptionIfNull) {
      throw new RuntimeException("Cant find action: " + name + ", on attributeDef: " + this.attributeDef);
    }
    return null;
  }
  
  /**
   * get action
   * @param exceptionWhenNotFound
   * @param action
   * @return the action
   */
  public AttributeAssignAction allowedAction(String action, boolean exceptionWhenNotFound) {
    action = StringUtils.defaultIfEmpty(action, AttributeDef.ACTION_DEFAULT);
    Set<AttributeAssignAction> actions = this.allowedActions();
    for (AttributeAssignAction attributeAssignAction : actions) {
      if (StringUtils.equals(action, attributeAssignAction.getName())) {
        return attributeAssignAction;
      }
    }
    if (exceptionWhenNotFound) {
      StringBuilder error = new StringBuilder("Cant find action: '" + action 
          + "' in attributeDef: " + this.attributeDef.getName() + ".  ");
      if (StringUtils.equals("assign", action)) {
        error.append("Note: 'assign' is the default action if not specified.  ");
      }
      error.append("Available actions: ");
      for (AttributeAssignAction attributeAssignAction : actions) {
        error.append(attributeAssignAction.getName()).append(", ");
      }
      throw new AttributeAssignActionNotFoundException(error.toString());
    }
    return null;
  }
  
  /**
   * 
   * @return the set of allowed action strings
   */
  public Set<String> allowedActionStrings() {

    //init if necessary
    allowedActions();
    return this.allowedActionStringSet;
    
  }
  
  /**
   * configure the action list based on collection of actions
   * @param collection
   */
  public void configureActionList(Collection<String> collection) {

    collection = GrouperUtil.nonNull(collection);
    
    //Lets get all the current actions
    Set<String> adds = new HashSet<String>();
    Set<String> removes = new HashSet<String>();
    
    //init list
    this.allowedActions();
    
    for (String need : collection) {
      if (!this.allowedActionStringSet.contains(need)) {
        adds.add(need);
      }
    }
    for (String has : this.allowedActionStringSet) {
      if (!collection.contains(has)) {
        removes.add(has);
      }
    }
    //lets add and delete, no need to do in transaction
    for (String add: adds) {
      internal_addAction(add, null);
    }
    for (String remove: removes) {
      for (AttributeAssignAction attributeAssignAction : this.allowedActions()) {
        if (StringUtils.equals(remove, attributeAssignAction.getName())) {
          attributeAssignAction.delete();
        }
      }
    }
    //lets clear the cache
    this.allowedActionsSet = null;
    this.allowedActionStringSet = null;
    
  }

  /**
   * add an action
   * @param action
   * @param uuid 
   * @return the assign
   */
  public AttributeAssignAction internal_addAction(String action, String uuid) {
    AttributeAssignAction attributeAssignAction = new AttributeAssignAction();
    attributeAssignAction.setId(StringUtils.isBlank(uuid) ? GrouperUuid.getUuid() : uuid );
    attributeAssignAction.setNameDb(action);
    attributeAssignAction.setAttributeDefId(this.attributeDef.getId());
    attributeAssignAction.save();
    //lets clear the cache
    this.allowedActionsSet = null;
    this.allowedActionStringSet = null;
    return attributeAssignAction;
  }
  
  /**
   * add an action if necessary
   * @param action
   * @return action
   */
  public AttributeAssignAction addAction(String action) {
  
    Set<String> allowedActionStrings = this.allowedActionStrings();
    if (!allowedActionStrings.contains(action)) {
      //make a new set so we dont edit the existing one
      allowedActionStrings = new HashSet<String>(allowedActionStrings);
      allowedActionStrings.add(action);
      this.configureActionList(allowedActionStrings);
    }
    return this.allowedAction(action, true);
  }

  /**
   * remove an action if necessary
   * @param action
   */
  public void removeAction(String action) {

    Set<String> allowedActionStrings = this.allowedActionStrings();
    if (allowedActionStrings.contains(action)) {
      //make a new set so we dont edit the existing one
      allowedActionStrings = new HashSet<String>(allowedActionStrings);
      allowedActionStrings.remove(action);
      this.configureActionList(allowedActionStrings);
    }
  }
  
  /**
   * replace existing all actions with newActions
   * @param newActions
   */
  public void replaceAllActionsWith(Collection newActions) {

    Set<String> allowedActionStrings = this.allowedActionStrings();
    allowedActionStrings = new HashSet<String>();
    allowedActionStrings.addAll(newActions);
    this.configureActionList(allowedActionStrings);
  }

}
