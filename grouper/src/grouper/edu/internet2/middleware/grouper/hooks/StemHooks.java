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
 * $Id: StemHooks.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksStemBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * stem related actions
 */
public abstract class StemHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: stemPostCommitDelete */
  public static final String METHOD_STEM_POST_COMMIT_DELETE = "stemPostCommitDelete";

  /** constant for method name for: stemPostCommitInsert */
  public static final String METHOD_STEM_POST_COMMIT_INSERT = "stemPostCommitInsert";

  /** constant for method name for: stemPostCommitUpdate */
  public static final String METHOD_STEM_POST_COMMIT_UPDATE = "stemPostCommitUpdate";

  /** constant for method name for: stemPostDelete */
  public static final String METHOD_STEM_POST_DELETE = "stemPostDelete";

  /** constant for method name for: stemPostInsert */
  public static final String METHOD_STEM_POST_INSERT = "stemPostInsert";

  /** constant for method name for: stemPostUpdate */
  public static final String METHOD_STEM_POST_UPDATE = "stemPostUpdate";

  /** constant for method name for: stemPreDelete */
  public static final String METHOD_STEM_PRE_DELETE = "stemPreDelete";

  /** constant for method name for: stemPreInsert */
  public static final String METHOD_STEM_PRE_INSERT = "stemPreInsert";

  /** constant for method name for: stemPreUpdate */
  public static final String METHOD_STEM_PRE_UPDATE = "stemPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a stem update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void stemPreUpdate(HooksContext hooksContext, HooksStemBean preUpdateBean) {
    
  }
  
  /**
   * called right after a stem update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void stemPostUpdate(HooksContext hooksContext, HooksStemBean postUpdateBean) {
    
  }
  
  /**
   * called right before a stem insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void stemPreInsert(HooksContext hooksContext, HooksStemBean preInsertBean) {
    
  }
  
  /**
   * called right after a stem insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void stemPostInsert(HooksContext hooksContext, HooksStemBean postInsertBean) {
    
  }
  
  /**
   * called right before a stem delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void stemPreDelete(HooksContext hooksContext, HooksStemBean preDeleteBean) {
    
  }
  
  /**
   * called right after a stem delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void stemPostDelete(HooksContext hooksContext, HooksStemBean postDeleteBean) {
    
  }

  /**
   * called right after a stem delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void stemPostCommitDelete(HooksContext hooksContext, HooksStemBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a stem insert commit
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void stemPostCommitInsert(HooksContext hooksContext, HooksStemBean postCommitInsertBean) {
    
  }

  /**
   * called right after a stem update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void stemPostCommitUpdate(HooksContext hooksContext, HooksStemBean postCommitUpdateBean) {
    
  }
  
}
