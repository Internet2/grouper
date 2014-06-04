/*******************************************************************************
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
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class AttributeAssignHooksImpl extends AttributeAssignHooks {

  /** keep reference to the attributeAssign to make sure it is different */
  static AttributeAssign mostRecentPostCommitInsertAttributeAssign;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPostCommitInsert(HooksContext hooksContext,
      HooksAttributeAssignBean postInsertCommitBean) {
    
    AttributeAssign attributeAssign = postInsertCommitBean.getAttributeAssign();
    mostRecentPostCommitInsertAttributeAssign = attributeAssign;
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitUpdateAttributeAssignId;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPostCommitUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPostCommitUpdate(HooksContext hooksContext,
      HooksAttributeAssignBean postUpdateCommitBean) {
    
    AttributeAssign attributeAssign = postUpdateCommitBean.getAttributeAssign();
    mostRecentPostCommitUpdateAttributeAssignId = attributeAssign.getId();
    
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitDeleteAttributeAssignId;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPostCommitDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPostCommitDelete(HooksContext hooksContext,
      HooksAttributeAssignBean postDeleteCommitBean) {
    
    AttributeAssign attributeAssign = postDeleteCommitBean.getAttributeAssign();
    mostRecentPostCommitDeleteAttributeAssignId = attributeAssign.getId();
    
  }

  /** most recent extension for testing */
  static String mostRecentPreInsertAttributeDefName;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPreInsert(HooksContext hooksContext, HooksAttributeAssignBean preInsertBean) {
    
    AttributeAssign attributeAssign = preInsertBean.getAttributeAssign();
    mostRecentPreInsertAttributeDefName = attributeAssign.getAttributeDefName().getName();
    if (StringUtils.equals("edu:test2", mostRecentPreInsertAttributeDefName)) {
      throw new HookVeto("hook.veto.attributeAssign.insert.name.not.test2", "name cannot be edu:test2");
    }
    
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteAttributeDefName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPostDelete(HooksContext hooksContext,
      HooksAttributeAssignBean postDeleteBean) {
    AttributeAssign attributeAssign = postDeleteBean.getAttributeAssign();
    mostRecentPostDeleteAttributeDefName = attributeAssign.getAttributeDefName().getName();
    if (StringUtils.equals("edu:test3", attributeAssign.getAttributeDefName().getName())) {
      throw new HookVeto("hook.veto.attributeAssign.delete.name.not.test3", "name cannot be edu:test3");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPostInsert(HooksContext hooksContext,
      HooksAttributeAssignBean postInsertBean) {
    try {

      AttributeAssign attributeAssign = postInsertBean.getAttributeAssign();
      mostRecentPostInsertAttributeDefName = attributeAssign.getAttributeDefName().getName();
      if (StringUtils.equals("edu:test8", mostRecentPostInsertAttributeDefName)) {
        throw new HookVeto("hook.veto.attributeAssign.insert.name.not.test8", "name cannot be edu:test8");
      }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException();
    }
  }

  /** most recent extension for testing */
  static Long mostRecentPostUpdateEnabledTime;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPostUpdate(HooksContext hooksContext,
      HooksAttributeAssignBean postUpdateBean) {
    AttributeAssign attributeAssign = postUpdateBean.getAttributeAssign();
    mostRecentPostUpdateEnabledTime = attributeAssign.getEnabledTimeDb();
    if (ObjectUtils.equals(4L, mostRecentPostUpdateEnabledTime)) {
      throw new HookVeto("hook.veto.attributeAssign.update.enabledTime.not.4", "enabledTime cannot be 4");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPreDeleteAttributeDefName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPreDelete(HooksContext hooksContext,
      HooksAttributeAssignBean preDeleteBean) {
    AttributeAssign attributeAssign = preDeleteBean.getAttributeAssign();
    mostRecentPreDeleteAttributeDefName = attributeAssign.getAttributeDefName().getName();
    if (StringUtils.equals("edu:test6", mostRecentPreDeleteAttributeDefName)) {
      throw new HookVeto("hook.veto.attributeAssign.delete.name.not.test6", "name cannot be edu:test6");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostInsertAttributeDefName;

  /** most recent extension for testing */
  static Long mostRecentPreUpdateAttributeEnabledTime;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignHooks#attributeAssignPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean)
   */
  @Override
  public void attributeAssignPreUpdate(HooksContext hooksContext,
      HooksAttributeAssignBean preUpdateBean) {
    AttributeAssign attributeAssign = preUpdateBean.getAttributeAssign();
    mostRecentPreUpdateAttributeEnabledTime = attributeAssign.getEnabledTimeDb();
    if (ObjectUtils.equals(2L, mostRecentPreUpdateAttributeEnabledTime)) {
      throw new HookVeto("hook.veto.attributeAssign.update.enabledTime.not.2", "enabledTime cannot be 2");
    }
  }

}
