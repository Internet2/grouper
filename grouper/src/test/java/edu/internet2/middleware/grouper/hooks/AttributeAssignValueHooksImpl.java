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
 * $Id: AttributeAssignValueHooksImpl.java 6923 2010-08-11 05:06:01Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;


/**
 * test implementation of group hooks for test
 */
public class AttributeAssignValueHooksImpl extends AttributeAssignValueHooks {

  /** keep reference to the attributeAssignValue to make sure it is different */
  static AttributeAssignValue mostRecentPostCommitInsertAttributeAssignValue;
  
  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks#attributeAssignValuePostCommitInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean)
   */
  @Override
  public void attributeAssignValuePostCommitInsert(HooksContext hooksContext,
      HooksAttributeAssignValueBean postInsertCommitBean) {
    
    AttributeAssignValue attributeAssignValue = postInsertCommitBean.getAttributeAssignValue();
    mostRecentPostCommitInsertAttributeAssignValue = attributeAssignValue;
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitUpdateAttributeAssignValueId;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks#attributeAssignValuePostCommitUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean)
   */
  @Override
  public void attributeAssignValuePostCommitUpdate(HooksContext hooksContext,
      HooksAttributeAssignValueBean postUpdateCommitBean) {
    
    AttributeAssignValue attributeAssignValue = postUpdateCommitBean.getAttributeAssignValue();
    mostRecentPostCommitUpdateAttributeAssignValueId = attributeAssignValue.getId();
    
  }

  /** most recent extension for testing */
  static String mostRecentPostCommitDeleteAttributeAssignValueId;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks#attributeAssignValuePostCommitDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean)
   */
  @Override
  public void attributeAssignValuePostCommitDelete(HooksContext hooksContext,
      HooksAttributeAssignValueBean postDeleteCommitBean) {
    
    AttributeAssignValue attributeAssignValue = postDeleteCommitBean.getAttributeAssignValue();
    mostRecentPostCommitDeleteAttributeAssignValueId = attributeAssignValue.getId();
    
  }

  /** most recent extension for testing */
  static Long mostRecentPreInsertValue;

  /**
   * 
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks#attributeAssignValuePreInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean)
   */
  @Override
  public void attributeAssignValuePreInsert(HooksContext hooksContext, HooksAttributeAssignValueBean preInsertBean) {
    
    AttributeAssignValue attributeAssignValue = preInsertBean.getAttributeAssignValue();
    mostRecentPreInsertValue = attributeAssignValue.getValueInteger();
    if (ObjectUtils.equals(22L, mostRecentPreInsertValue)) {
      throw new HookVeto("hook.veto.attributeAssignValue.insert.value.not.22", "value cannot be 22");
    }
    
  }

  /** most recent extension for testing */
  static Long mostRecentPostDeleteValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks#attributeAssignValuePostDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean)
   */
  @Override
  public void attributeAssignValuePostDelete(HooksContext hooksContext,
      HooksAttributeAssignValueBean postDeleteBean) {
    AttributeAssignValue attributeAssignValue = postDeleteBean.getAttributeAssignValue();
    mostRecentPostDeleteValue = attributeAssignValue.getValueInteger();
    if (ObjectUtils.equals(3L, attributeAssignValue.getValueInteger())) {
      throw new HookVeto("hook.veto.attributeAssignValue.delete.value.not.3", "value cannot be 3");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks#attributeAssignValuePostInsert(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean)
   */
  @Override
  public void attributeAssignValuePostInsert(HooksContext hooksContext,
      HooksAttributeAssignValueBean postInsertBean) {
    try {

      AttributeAssignValue attributeAssignValue = postInsertBean.getAttributeAssignValue();
      mostRecentPostInsertValue = attributeAssignValue.getValueInteger();
      if (ObjectUtils.equals(8L, mostRecentPostInsertValue)) {
        throw new HookVeto("hook.veto.attributeAssignValue.insert.value.not.8", "value cannot be 8");
      }
    } catch (GroupNotFoundException gnfe) {
      throw new RuntimeException();
    }
  }

  /** most recent extension for testing */
  static Long mostRecentPostUpdateValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks#attributeAssignValuePostUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean)
   */
  @Override
  public void attributeAssignValuePostUpdate(HooksContext hooksContext,
      HooksAttributeAssignValueBean postUpdateBean) {
    AttributeAssignValue attributeAssignValue = postUpdateBean.getAttributeAssignValue();
    mostRecentPostUpdateValue = attributeAssignValue.getValueInteger();
    if (ObjectUtils.equals(4L, mostRecentPostUpdateValue)) {
      throw new HookVeto("hook.veto.attributeAssignValue.update.value.not.4", "value cannot be 4");
    }
  }

  /** most recent extension for testing */
  static Long mostRecentPreDeleteValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks#attributeAssignValuePreDelete(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean)
   */
  @Override
  public void attributeAssignValuePreDelete(HooksContext hooksContext,
      HooksAttributeAssignValueBean preDeleteBean) {
    AttributeAssignValue attributeAssignValue = preDeleteBean.getAttributeAssignValue();
    mostRecentPreDeleteValue = attributeAssignValue.getValueInteger();
    if (ObjectUtils.equals(6L, mostRecentPreDeleteValue)) {
      throw new HookVeto("hook.veto.attributeAssignValue.delete.value.not.6", "value cannot be 6");
    }
  }

  /** most recent extension for testing */
  static Long mostRecentPostInsertValue;

  /** most recent extension for testing */
  static Long mostRecentPreUpdateValue;

  /**
   * @see edu.internet2.middleware.grouper.hooks.AttributeAssignValueHooks#attributeAssignValuePreUpdate(edu.internet2.middleware.grouper.hooks.beans.HooksContext, edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean)
   */
  @Override
  public void attributeAssignValuePreUpdate(HooksContext hooksContext,
      HooksAttributeAssignValueBean preUpdateBean) {
    AttributeAssignValue attributeAssignValue = preUpdateBean.getAttributeAssignValue();
    mostRecentPreUpdateValue = attributeAssignValue.getValueInteger();
    if (ObjectUtils.equals(2L, mostRecentPreUpdateValue)) {
      throw new HookVeto("hook.veto.attributeAssignValue.update.value.not.2", "value cannot be 2");
    }
  }

}
