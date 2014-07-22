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
 * $Id: CompositeHooks.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * composite related actions
 */
public abstract class CompositeHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: compositePostCommitDelete */
  public static final String METHOD_COMPOSITE_POST_COMMIT_DELETE = "compositePostCommitDelete";

  /** constant for method name for: compositePostCommitInsert */
  public static final String METHOD_COMPOSITE_POST_COMMIT_INSERT = "compositePostCommitInsert";

  /** constant for method name for: compositePostCommitUpdate */
  public static final String METHOD_COMPOSITE_POST_COMMIT_UPDATE = "compositePostCommitUpdate";

  /** constant for method name for: compositePostDelete */
  public static final String METHOD_COMPOSITE_POST_DELETE = "compositePostDelete";

  /** constant for method name for: compositePostInsert */
  public static final String METHOD_COMPOSITE_POST_INSERT = "compositePostInsert";

  /** constant for method name for: compositePostUpdate */
  public static final String METHOD_COMPOSITE_POST_UPDATE = "compositePostUpdate";

  /** constant for method name for: compositePreDelete */
  public static final String METHOD_COMPOSITE_PRE_DELETE = "compositePreDelete";

  /** constant for method name for: compositePreInsert */
  public static final String METHOD_COMPOSITE_PRE_INSERT = "compositePreInsert";

  /** constant for method name for: compositePreUpdate */
  public static final String METHOD_COMPOSITE_PRE_UPDATE = "compositePreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//
  /**
   * called right before a composite update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void compositePreUpdate(HooksContext hooksContext, HooksCompositeBean preUpdateBean) {
    
  }
  
  /**
   * called right after a composite update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void compositePostUpdate(HooksContext hooksContext, HooksCompositeBean postUpdateBean) {
    
  }
  
  /**
   * called right before a composite insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void compositePreInsert(HooksContext hooksContext, HooksCompositeBean preInsertBean) {
    
  }
  
  /**
   * called right after a composite insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void compositePostInsert(HooksContext hooksContext, HooksCompositeBean postInsertBean) {
    
  }
  
  /**
   * called right before a composite delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void compositePreDelete(HooksContext hooksContext, HooksCompositeBean preDeleteBean) {
    
  }
  
  /**
   * called right after a composite delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void compositePostDelete(HooksContext hooksContext, HooksCompositeBean postDeleteBean) {
    
  }

  /**
   * called right after a composite delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void compositePostCommitDelete(HooksContext hooksContext, HooksCompositeBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a composite insert commit
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void compositePostCommitInsert(HooksContext hooksContext, HooksCompositeBean postCommitInsertBean) {
    
  }

  /**
   * called right after a composite update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void compositePostCommitUpdate(HooksContext hooksContext, HooksCompositeBean postCommitUpdateBean) {
    
  }
  
}
