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
 * $Id: FieldHooks.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksFieldBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * field related actions
 */
public abstract class FieldHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: fieldPostCommitDelete */
  public static final String METHOD_FIELD_POST_COMMIT_DELETE = "fieldPostCommitDelete";

  /** constant for method name for: fieldPostCommitInsert */
  public static final String METHOD_FIELD_POST_COMMIT_INSERT = "fieldPostCommitInsert";

  /** constant for method name for: fieldPostCommitUpdate */
  public static final String METHOD_FIELD_POST_COMMIT_UPDATE = "fieldPostCommitUpdate";

  /** constant for method name for: fieldPostDelete */
  public static final String METHOD_FIELD_POST_DELETE = "fieldPostDelete";

  /** constant for method name for: fieldPostInsert */
  public static final String METHOD_FIELD_POST_INSERT = "fieldPostInsert";

  /** constant for method name for: fieldPostUpdate */
  public static final String METHOD_FIELD_POST_UPDATE = "fieldPostUpdate";

  /** constant for method name for: fieldPreDelete */
  public static final String METHOD_FIELD_PRE_DELETE = "fieldPreDelete";

  /** constant for method name for: fieldPreInsert */
  public static final String METHOD_FIELD_PRE_INSERT = "fieldPreInsert";

  /** constant for method name for: fieldPreUpdate */
  public static final String METHOD_FIELD_PRE_UPDATE = "fieldPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a field update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void fieldPreUpdate(HooksContext hooksContext, HooksFieldBean preUpdateBean) {
    
  }
  
  /**
   * called right after a field update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void fieldPostUpdate(HooksContext hooksContext, HooksFieldBean postUpdateBean) {
    
  }
  
  /**
   * called right before a field insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void fieldPreInsert(HooksContext hooksContext, HooksFieldBean preInsertBean) {
    
  }
  
  /**
   * called right after a field insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void fieldPostInsert(HooksContext hooksContext, HooksFieldBean postInsertBean) {
    
  }
  
  /**
   * called right before a field delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void fieldPreDelete(HooksContext hooksContext, HooksFieldBean preDeleteBean) {
    
  }
  
  /**
   * called right after a field delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void fieldPostDelete(HooksContext hooksContext, HooksFieldBean postDeleteBean) {
    
  }

  /**
   * called right after a field delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void fieldPostCommitDelete(HooksContext hooksContext, HooksFieldBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a field insert commit
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void fieldPostCommitInsert(HooksContext hooksContext, HooksFieldBean postCommitInsertBean) {
    
  }

  /**
   * called right after a field update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void fieldPostCommitUpdate(HooksContext hooksContext, HooksFieldBean postCommitUpdateBean) {
    
  }
  
}
