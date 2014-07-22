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
 * $Id: AttributeHooksImpl.java,v 1.3 2009-03-24 17:12:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Attribute;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class AttributeHooksImpl extends AttributeHooks {

  /** keep reference to the attribute to make sure it is different */
  static Attribute mostRecentPostCommitInsertAttribute;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostCommitInsert(HooksContext hooksContext,
      HooksAttributeBean postInsertCommitBean) {
    
    Attribute attribute = postInsertCommitBean.getAttribute();
    mostRecentPostCommitInsertAttribute = attribute;
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitUpdateAttributeValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostCommitUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostCommitUpdate(HooksContext hooksContext,
      HooksAttributeBean postUpdateCommitBean) {
    
    Attribute attribute = postUpdateCommitBean.getAttribute();
    mostRecentPostCommitUpdateAttributeValue = attribute.getValue();
    
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitDeleteAttributeValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostCommitDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostCommitDelete(HooksContext hooksContext,
      HooksAttributeBean postDeleteCommitBean) {
    
    Attribute attribute = postDeleteCommitBean.getAttribute();
    mostRecentPostCommitDeleteAttributeValue = attribute.getValue();
    
  }

  /** most recent extension for testing */
  static String mostRecentPreInsertAttributeValue;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePreInsert(HooksContext hooksContext, HooksAttributeBean preInsertBean) {
    
    Attribute attribute = preInsertBean.getAttribute();
    mostRecentPreInsertAttributeValue = attribute.getValue();
    if (StringUtils.equals("test2", mostRecentPreInsertAttributeValue)) {
      throw new HookVeto("hook.veto.attribute.insert.name.not.test2", "name cannot be test2");
    }
    
  }

  /** most recent extension for testing */
  static String mostRecentPostDeleteAttributeValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostDelete(HooksContext hooksContext,
      HooksAttributeBean postDeleteBean) {
    Attribute attribute = postDeleteBean.getAttribute();
    mostRecentPostDeleteAttributeValue = attribute.getValue();
    if (StringUtils.equals("test3", attribute.getValue())) {
      throw new HookVeto("hook.veto.attribute.delete.name.not.test3", "name cannot be test3");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostInsert(HooksContext hooksContext,
      HooksAttributeBean postInsertBean) {
    try {

    Attribute attribute = postInsertBean.getAttribute();
      Group aGroup = GroupFinder.findByUuid(GrouperSession.staticGrouperSession(), attribute.getGroupUuid(), true);
      mostRecentPostInsertAttributeValue = aGroup.getAttributeValue(attribute.getAttrName(), false, false);
      if (StringUtils.equals("test8", mostRecentPostInsertAttributeValue)) {
        throw new HookVeto("hook.veto.attribute.insert.name.not.test8", "name cannot be test8");
      }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException();
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostUpdateAttributeValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePostUpdate(HooksContext hooksContext,
      HooksAttributeBean postUpdateBean) {
    Attribute attribute = postUpdateBean.getAttribute();
    mostRecentPostUpdateAttributeValue = attribute.getValue();
    if (StringUtils.equals("test12", mostRecentPostUpdateAttributeValue)) {
      throw new HookVeto("hook.veto.attribute.update.name.not.test12", "name cannot be test12");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPreDeleteAttributeValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePreDelete(HooksContext hooksContext,
      HooksAttributeBean preDeleteBean) {
    Attribute attribute = preDeleteBean.getAttribute();
    mostRecentPreDeleteAttributeValue = attribute.getValue();
    if (StringUtils.equals("test6", mostRecentPreDeleteAttributeValue)) {
      throw new HookVeto("hook.veto.attribute.delete.name.not.test6", "name cannot be test6");
    }
  }

  /** most recent extension for testing */
  static String mostRecentPostInsertAttributeValue;

  /** most recent extension for testing */
  static String mostRecentPreUpdateAttributeValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeHooks#attributePreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean)
   */
  @Override
  public void attributePreUpdate(HooksContext hooksContext,
      HooksAttributeBean preUpdateBean) {
    Attribute attribute = preUpdateBean.getAttribute();
    mostRecentPreUpdateAttributeValue = attribute.getValue();
    if (StringUtils.equals("test10", mostRecentPreUpdateAttributeValue)) {
      throw new HookVeto("hook.veto.attribute.update.name.not.test10", "name cannot be test10");
    }
  }

}
