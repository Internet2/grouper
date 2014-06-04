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
 * $Id: AttributeDefHooks.java 6921 2010-08-10 21:03:10Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * new attribute framework related actions
 */
public abstract class AttributeDefHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: attributeDefPostCommitDelete */
  public static final String METHOD_ATTRIBUTE_DEF_POST_COMMIT_DELETE = "attributeDefPostCommitDelete";

  /** constant for method name for: attributeDefPostCommitInsert */
  public static final String METHOD_ATTRIBUTE_DEF_POST_COMMIT_INSERT = "attributeDefPostCommitInsert";

  /** constant for method name for: attributeDefPostCommitUpdate */
  public static final String METHOD_ATTRIBUTE_DEF_POST_COMMIT_UPDATE = "attributeDefPostCommitUpdate";

  /** constant for method name for: attributeDefPostDelete */
  public static final String METHOD_ATTRIBUTE_DEF_POST_DELETE = "attributeDefPostDelete";

  /** constant for method name for: attributeDefPostInsert */
  public static final String METHOD_ATTRIBUTE_DEF_POST_INSERT = "attributeDefPostInsert";

  /** constant for method name for: attributeDefPostUpdate */
  public static final String METHOD_ATTRIBUTE_DEF_POST_UPDATE = "attributeDefPostUpdate";

  /** constant for method name for: attributeDefPreDelete */
  public static final String METHOD_ATTRIBUTE_DEF_PRE_DELETE = "attributeDefPreDelete";

  /** constant for method name for: attributeDefPreInsert */
  public static final String METHOD_ATTRIBUTE_DEF_PRE_INSERT = "attributeDefPreInsert";

  /** constant for method name for: attributeDefPreUpdate */
  public static final String METHOD_ATTRIBUTE_DEF_PRE_UPDATE = "attributeDefPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  
  
  /**
   * called right before a attribute update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void attributeDefPreUpdate(HooksContext hooksContext, HooksAttributeDefBean preUpdateBean) {
  }
  
  /**
   * called right after a attribute update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void attributeDefPostUpdate(HooksContext hooksContext, HooksAttributeDefBean postUpdateBean) {
    
  }
  
  /**
   * called right before a attribute insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void attributeDefPreInsert(HooksContext hooksContext, HooksAttributeDefBean preInsertBean) {
    
  }
  
  /**
   * called right after a attribute insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void attributeDefPostInsert(HooksContext hooksContext, HooksAttributeDefBean postInsertBean) {
    
  }
  
  /**
   * called right after the commit of a post insert commit.  Note, cant veto this or participate in the tx
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void attributeDefPostCommitInsert(HooksContext hooksContext, HooksAttributeDefBean postCommitInsertBean) {
    
  }
  
  /**
   * called right before a attribute delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void attributeDefPreDelete(HooksContext hooksContext, HooksAttributeDefBean preDeleteBean) {
    
  }
  
  /**
   * called right after a attribute delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void attributeDefPostDelete(HooksContext hooksContext, HooksAttributeDefBean postDeleteBean) {
    
  }

  /**
   * called right after a commit involving a attribute delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void attributeDefPostCommitDelete(HooksContext hooksContext, HooksAttributeDefBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a commit on a attribute update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void attributeDefPostCommitUpdate(HooksContext hooksContext, HooksAttributeDefBean postCommitUpdateBean) {
    
  }
  
}
