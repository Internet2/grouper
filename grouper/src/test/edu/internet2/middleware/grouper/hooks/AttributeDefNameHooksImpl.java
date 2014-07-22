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
/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class AttributeDefNameHooksImpl extends AttributeDefNameHooks {

  /** keep reference to the attributeDefName to make sure it is different */
  static AttributeDefName mostRecentPostCommitInsertAttributeDefName;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePostCommitInsert(HooksContext hooksContext,
      HooksAttributeDefNameBean postInsertCommitBean) {
    
    AttributeDefName attributeDefName = postInsertCommitBean.getAttributeDefName();
    mostRecentPostCommitInsertAttributeDefName = attributeDefName;
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitUpdateAttributeDefNameName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePostCommitUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePostCommitUpdate(HooksContext hooksContext,
      HooksAttributeDefNameBean postUpdateCommitBean) {
    
    AttributeDefName attributeDefName = postUpdateCommitBean.getAttributeDefName();
    mostRecentPostCommitUpdateAttributeDefNameName = attributeDefName.getName();
    
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitDeleteAttributeDefNameName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePostCommitDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePostCommitDelete(HooksContext hooksContext,
      HooksAttributeDefNameBean postDeleteCommitBean) {
    
    AttributeDefName attributeDefName = postDeleteCommitBean.getAttributeDefName();
    mostRecentPostCommitDeleteAttributeDefNameName = attributeDefName.getName();
    
  }

  /** most recent extension for testing */
  static String mostRecentPreInsertAttributeDefNameName;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePreInsert(HooksContext hooksContext, HooksAttributeDefNameBean preInsertBean) {
    
    AttributeDefName attributeDefName = preInsertBean.getAttributeDefName();
    mostRecentPreInsertAttributeDefNameName = attributeDefName.getName();
    if (StringUtils.equals("edu:test2", mostRecentPreInsertAttributeDefNameName)) {
      throw new HookVeto("hook.veto.attributeDefName.insert.name.not.test2", "name cannot be edu:test2");
    }
    
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteAttributeDefNameName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePostDelete(HooksContext hooksContext,
      HooksAttributeDefNameBean postDeleteBean) {
    AttributeDefName attributeDefName = postDeleteBean.getAttributeDefName();
    mostRecentPostDeleteAttributeDefNameName = attributeDefName.getName();
    if (StringUtils.equals("edu:test3", attributeDefName.getName())) {
      throw new HookVeto("hook.veto.attributeDefName.delete.name.not.test3", "name cannot be edu:test3");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePostInsert(HooksContext hooksContext,
      HooksAttributeDefNameBean postInsertBean) {
    try {

    AttributeDefName attributeDefName = postInsertBean.getAttributeDefName();
    mostRecentPostInsertAttributeDefNameName = attributeDefName.getName();
    if (StringUtils.equals("edu:test8", mostRecentPostInsertAttributeDefNameName)) {
      throw new HookVeto("hook.veto.attributeDefName.insert.name.not.test8", "name cannot be edu:test8");
    }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException();
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostUpdateAttributeDefNameName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePostUpdate(HooksContext hooksContext,
      HooksAttributeDefNameBean postUpdateBean) {
    AttributeDefName attributeDefName = postUpdateBean.getAttributeDefName();
    mostRecentPostUpdateAttributeDefNameName = attributeDefName.getName();
    if (StringUtils.equals("test12", attributeDefName.getDescription())) {
      throw new HookVeto("hook.veto.attributeDefName.update.name.not.test12", "description cannot be test12");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPreDeleteAttributeDefNameName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePreDelete(HooksContext hooksContext,
      HooksAttributeDefNameBean preDeleteBean) {
    AttributeDefName attributeDefName = preDeleteBean.getAttributeDefName();
    mostRecentPreDeleteAttributeDefNameName = attributeDefName.getName();
    if (StringUtils.equals("edu:test6", mostRecentPreDeleteAttributeDefNameName)) {
      throw new HookVeto("hook.veto.attributeDefName.delete.name.not.test6", "name cannot be edu:test6");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostInsertAttributeDefNameName;

  /** most recent extension for testing */
  static String mostRecentPreUpdateAttributeDefNameName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefNameHooks#attributeDefNamePreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean)
   */
  @Override
  public void attributeDefNamePreUpdate(HooksContext hooksContext,
      HooksAttributeDefNameBean preUpdateBean) {
    AttributeDefName attributeDefName = preUpdateBean.getAttributeDefName();
    mostRecentPreUpdateAttributeDefNameName = attributeDefName.getName();
    if (StringUtils.equals("test10", attributeDefName.getDescription())) {
      throw new HookVeto("hook.veto.attributeDefName.update.name.not.test10", "description cannot be test10");
    }
  }

}
