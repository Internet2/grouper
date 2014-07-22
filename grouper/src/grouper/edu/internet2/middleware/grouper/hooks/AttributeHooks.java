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
 * $Id: AttributeHooks.java,v 1.1 2008-11-04 07:17:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * attribute related actions
 */
public abstract class AttributeHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: attributePostCommitDelete */
  public static final String METHOD_ATTRIBUTE_POST_COMMIT_DELETE = "attributePostCommitDelete";

  /** constant for method name for: attributePostCommitInsert */
  public static final String METHOD_ATTRIBUTE_POST_COMMIT_INSERT = "attributePostCommitInsert";

  /** constant for method name for: attributePostCommitUpdate */
  public static final String METHOD_ATTRIBUTE_POST_COMMIT_UPDATE = "attributePostCommitUpdate";

  /** constant for method name for: attributePostDelete */
  public static final String METHOD_ATTRIBUTE_POST_DELETE = "attributePostDelete";

  /** constant for method name for: attributePostInsert */
  public static final String METHOD_ATTRIBUTE_POST_INSERT = "attributePostInsert";

  /** constant for method name for: attributePostUpdate */
  public static final String METHOD_ATTRIBUTE_POST_UPDATE = "attributePostUpdate";

  /** constant for method name for: attributePreDelete */
  public static final String METHOD_ATTRIBUTE_PRE_DELETE = "attributePreDelete";

  /** constant for method name for: attributePreInsert */
  public static final String METHOD_ATTRIBUTE_PRE_INSERT = "attributePreInsert";

  /** constant for method name for: attributePreUpdate */
  public static final String METHOD_ATTRIBUTE_PRE_UPDATE = "attributePreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a attribute update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void attributePreUpdate(HooksContext hooksContext, HooksAttributeBean preUpdateBean) {
    
  }
  
  /**
   * called right after a attribute update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void attributePostUpdate(HooksContext hooksContext, HooksAttributeBean postUpdateBean) {
    
  }
  
  /**
   * called right before a attribute insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void attributePreInsert(HooksContext hooksContext, HooksAttributeBean preInsertBean) {
    
  }
  
  /**
   * called right after a attribute insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void attributePostInsert(HooksContext hooksContext, HooksAttributeBean postInsertBean) {
    
  }
  
  /**
   * called right after the commit of a post insert commit.  Note, cant veto this or participate in the tx
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void attributePostCommitInsert(HooksContext hooksContext, HooksAttributeBean postCommitInsertBean) {
    
  }
  
  /**
   * called right before a attribute delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void attributePreDelete(HooksContext hooksContext, HooksAttributeBean preDeleteBean) {
    
  }
  
  /**
   * called right after a attribute delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void attributePostDelete(HooksContext hooksContext, HooksAttributeBean postDeleteBean) {
    
  }

  /**
   * called right after a commit involving a attribute delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void attributePostCommitDelete(HooksContext hooksContext, HooksAttributeBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a commit on a attribute update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void attributePostCommitUpdate(HooksContext hooksContext, HooksAttributeBean postCommitUpdateBean) {
    
  }
  
}
