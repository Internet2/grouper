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
 * $Id: AttributeAssignHooks.java 6923 2010-08-11 05:06:01Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignValueBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * new attribute assign value framework related actions
 */
public abstract class AttributeAssignValueHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: attributeAssignValuePostCommitDelete */
  public static final String METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_COMMIT_DELETE = "attributeAssignValuePostCommitDelete";

  /** constant for method name for: attributeAssignValuePostCommitInsert */
  public static final String METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_COMMIT_INSERT = "attributeAssignValuePostCommitInsert";

  /** constant for method name for: attributeAssignValuePostCommitUpdate */
  public static final String METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_COMMIT_UPDATE = "attributeAssignValuePostCommitUpdate";

  /** constant for method name for: attributeAssignValuePostDelete */
  public static final String METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_DELETE = "attributeAssignValuePostDelete";

  /** constant for method name for: attributeAssignValuePostInsert */
  public static final String METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_INSERT = "attributeAssignValuePostInsert";

  /** constant for method name for: attributeAssignValuePostUpdate */
  public static final String METHOD_ATTRIBUTE_ASSIGN_VALUE_POST_UPDATE = "attributeAssignValuePostUpdate";

  /** constant for method name for: attributeAssignValuePreDelete */
  public static final String METHOD_ATTRIBUTE_ASSIGN_VALUE_PRE_DELETE = "attributeAssignValuePreDelete";

  /** constant for method name for: attributeAssignValuePreInsert */
  public static final String METHOD_ATTRIBUTE_ASSIGN_VALUE_PRE_INSERT = "attributeAssignValuePreInsert";

  /** constant for method name for: attributeAssignValuePreUpdate */
  public static final String METHOD_ATTRIBUTE_ASSIGN_VALUE_PRE_UPDATE = "attributeAssignValuePreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a attribute assign value update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void attributeAssignValuePreUpdate(HooksContext hooksContext, HooksAttributeAssignValueBean preUpdateBean) {
  }
  
  /**
   * called right after a attribute assign value update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void attributeAssignValuePostUpdate(HooksContext hooksContext, HooksAttributeAssignValueBean postUpdateBean) {
    
  }
  
  /**
   * called right before a attribute assign value insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void attributeAssignValuePreInsert(HooksContext hooksContext, HooksAttributeAssignValueBean preInsertBean) {
    
  }
  
  /**
   * called right after a attribute assign value insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void attributeAssignValuePostInsert(HooksContext hooksContext, HooksAttributeAssignValueBean postInsertBean) {
    
  }
  
  /**
   * called right after the commit of a post insert commit.  Note, cant veto this or participate in the tx
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void attributeAssignValuePostCommitInsert(HooksContext hooksContext, HooksAttributeAssignValueBean postCommitInsertBean) {
    
  }
  
  /**
   * called right before a attribute assign value delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void attributeAssignValuePreDelete(HooksContext hooksContext, HooksAttributeAssignValueBean preDeleteBean) {
    
  }
  
  /**
   * called right after a attribute assign value delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void attributeAssignValuePostDelete(HooksContext hooksContext, HooksAttributeAssignValueBean postDeleteBean) {
    
  }

  /**
   * called right after a commit involving a attribute assign value delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void attributeAssignValuePostCommitDelete(HooksContext hooksContext, HooksAttributeAssignValueBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a commit on a attribute assign value update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void attributeAssignValuePostCommitUpdate(HooksContext hooksContext, HooksAttributeAssignValueBean postCommitUpdateBean) {
    
  }
  
}
