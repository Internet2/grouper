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
/*
 * @author mchyzer
 * $Id: AttributeDefHooksImpl.java 6921 2010-08-10 21:03:10Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class AttributeDefHooksImpl extends AttributeDefHooks {

  /** keep reference to the attributeDef to make sure it is different */
  static AttributeDef mostRecentPostCommitInsertAttributeDef;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPostCommitInsert(HooksContext hooksContext,
      HooksAttributeDefBean postInsertCommitBean) {
    
    AttributeDef attributeDef = postInsertCommitBean.getAttributeDef();
    mostRecentPostCommitInsertAttributeDef = attributeDef;
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitUpdateAttributeDefName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPostCommitUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPostCommitUpdate(HooksContext hooksContext,
      HooksAttributeDefBean postUpdateCommitBean) {
    
    AttributeDef attributeDef = postUpdateCommitBean.getAttributeDef();
    mostRecentPostCommitUpdateAttributeDefName = attributeDef.getName();
    
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitDeleteAttributeDefName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPostCommitDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPostCommitDelete(HooksContext hooksContext,
      HooksAttributeDefBean postDeleteCommitBean) {
    
    AttributeDef attributeDef = postDeleteCommitBean.getAttributeDef();
    mostRecentPostCommitDeleteAttributeDefName = attributeDef.getName();
    
  }

  /** most recent extension for testing */
  static String mostRecentPreInsertAttributeDefName;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPreInsert(HooksContext hooksContext, HooksAttributeDefBean preInsertBean) {
    
    AttributeDef attributeDef = preInsertBean.getAttributeDef();
    mostRecentPreInsertAttributeDefName = attributeDef.getName();
    if (StringUtils.equals("edu:test2", mostRecentPreInsertAttributeDefName)) {
      throw new HookVeto("hook.veto.attributeDef.insert.name.not.test2", "name cannot be edu:test2");
    }
    
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteAttributeDefName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPostDelete(HooksContext hooksContext,
      HooksAttributeDefBean postDeleteBean) {
    AttributeDef attributeDef = postDeleteBean.getAttributeDef();
    mostRecentPostDeleteAttributeDefName = attributeDef.getName();
    if (StringUtils.equals("edu:test3", attributeDef.getName())) {
      throw new HookVeto("hook.veto.attributeDef.delete.name.not.test3", "name cannot be edu:test3");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPostInsert(HooksContext hooksContext,
      HooksAttributeDefBean postInsertBean) {
    try {

    AttributeDef attributeDef = postInsertBean.getAttributeDef();
    mostRecentPostInsertAttributeDefName = attributeDef.getName();
    if (StringUtils.equals("edu:test8", mostRecentPostInsertAttributeDefName)) {
      throw new HookVeto("hook.veto.attributeDef.insert.name.not.test8", "name cannot be edu:test8");
    }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException();
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostUpdateAttributeDefName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPostUpdate(HooksContext hooksContext,
      HooksAttributeDefBean postUpdateBean) {
    AttributeDef attributeDef = postUpdateBean.getAttributeDef();
    mostRecentPostUpdateAttributeDefName = attributeDef.getName();
    if (StringUtils.equals("test12", attributeDef.getDescription())) {
      throw new HookVeto("hook.veto.attributeDef.update.name.not.test12", "description cannot be test12");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPreDeleteAttributeDefName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPreDelete(HooksContext hooksContext,
      HooksAttributeDefBean preDeleteBean) {
    AttributeDef attributeDef = preDeleteBean.getAttributeDef();
    mostRecentPreDeleteAttributeDefName = attributeDef.getName();
    if (StringUtils.equals("edu:test6", mostRecentPreDeleteAttributeDefName)) {
      throw new HookVeto("hook.veto.attributeDef.delete.name.not.test6", "name cannot be edu:test6");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostInsertAttributeDefName;

  /** most recent extension for testing */
  static String mostRecentPreUpdateAttributeDefName;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeDefHooks#attributeDefPreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean)
   */
  @Override
  public void attributeDefPreUpdate(HooksContext hooksContext,
      HooksAttributeDefBean preUpdateBean) {
    AttributeDef attributeDef = preUpdateBean.getAttributeDef();
    mostRecentPreUpdateAttributeDefName = attributeDef.getName();
    if (StringUtils.equals("test10", attributeDef.getDescription())) {
      throw new HookVeto("hook.veto.attributeDef.update.name.not.test10", "description cannot be test10");
    }
  }

}
