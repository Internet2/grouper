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

import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeAssignBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * new attribute assign framework related actions
 */
public abstract class AttributeAssignHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: attributeAssignPostCommitDelete */
  public static final String METHOD_ATTRIBUTE_ASSIGN_POST_COMMIT_DELETE = "attributeAssignPostCommitDelete";

  /** constant for method name for: attributeAssignPostCommitInsert */
  public static final String METHOD_ATTRIBUTE_ASSIGN_POST_COMMIT_INSERT = "attributeAssignPostCommitInsert";

  /** constant for method name for: attributeAssignPostCommitUpdate */
  public static final String METHOD_ATTRIBUTE_ASSIGN_POST_COMMIT_UPDATE = "attributeAssignPostCommitUpdate";

  /** constant for method name for: attributeAssignPostDelete */
  public static final String METHOD_ATTRIBUTE_ASSIGN_POST_DELETE = "attributeAssignPostDelete";

  /** constant for method name for: attributeAssignPostInsert */
  public static final String METHOD_ATTRIBUTE_ASSIGN_POST_INSERT = "attributeAssignPostInsert";

  /** constant for method name for: attributeAssignPostUpdate */
  public static final String METHOD_ATTRIBUTE_ASSIGN_POST_UPDATE = "attributeAssignPostUpdate";

  /** constant for method name for: attributeAssignPreDelete */
  public static final String METHOD_ATTRIBUTE_ASSIGN_PRE_DELETE = "attributeAssignPreDelete";

  /** constant for method name for: attributeAssignPreInsert */
  public static final String METHOD_ATTRIBUTE_ASSIGN_PRE_INSERT = "attributeAssignPreInsert";

  /** constant for method name for: attributeAssignPreUpdate */
  public static final String METHOD_ATTRIBUTE_ASSIGN_PRE_UPDATE = "attributeAssignPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a attribute assign update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void attributeAssignPreUpdate(HooksContext hooksContext, HooksAttributeAssignBean preUpdateBean) {
  }
  
  /**
   * called right after a attribute assign update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void attributeAssignPostUpdate(HooksContext hooksContext, HooksAttributeAssignBean postUpdateBean) {
    
  }
  
  /**
   * called right before a attribute assign insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void attributeAssignPreInsert(HooksContext hooksContext, HooksAttributeAssignBean preInsertBean) {
    
  }
  
  /**
   * called right after a attribute assign insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void attributeAssignPostInsert(HooksContext hooksContext, HooksAttributeAssignBean postInsertBean) {
    
  }
  
  /**
   * called right after the commit of a post insert commit.  Note, cant veto this or participate in the tx
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void attributeAssignPostCommitInsert(HooksContext hooksContext, HooksAttributeAssignBean postCommitInsertBean) {
    
  }
  
  /**
   * called right before a attribute assign delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void attributeAssignPreDelete(HooksContext hooksContext, HooksAttributeAssignBean preDeleteBean) {
    
  }
  
  /**
   * called right after a attribute assign delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void attributeAssignPostDelete(HooksContext hooksContext, HooksAttributeAssignBean postDeleteBean) {
    
  }

  /**
   * called right after a commit involving a attribute assign delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void attributeAssignPostCommitDelete(HooksContext hooksContext, HooksAttributeAssignBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a commit on a attribute assign update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void attributeAssignPostCommitUpdate(HooksContext hooksContext, HooksAttributeAssignBean postCommitUpdateBean) {
    
  }
  
}
