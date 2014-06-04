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
 * $Id: GroupTypeTupleHooks.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeTupleBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * groupTypeTuple related actions
 */
public abstract class GroupTypeTupleHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: groupTypeTuplePostCommitDelete */
  public static final String METHOD_GROUP_TYPE_TUPLE_POST_COMMIT_DELETE = "groupTypeTuplePostCommitDelete";

  /** constant for method name for: groupTypeTuplePostCommitInsert */
  public static final String METHOD_GROUP_TYPE_TUPLE_POST_COMMIT_INSERT = "groupTypeTuplePostCommitInsert";

  /** constant for method name for: groupTypeTuplePostCommitUpdate */
  public static final String METHOD_GROUP_TYPE_TUPLE_POST_COMMIT_UPDATE = "groupTypeTuplePostCommitUpdate";

  /** constant for method name for: groupTypeTuplePostDelete */
  public static final String METHOD_GROUP_TYPE_TUPLE_POST_DELETE = "groupTypeTuplePostDelete";

  /** constant for method name for: groupTypeTuplePostInsert */
  public static final String METHOD_GROUP_TYPE_TUPLE_POST_INSERT = "groupTypeTuplePostInsert";

  /** constant for method name for: groupTypeTuplePostUpdate */
  public static final String METHOD_GROUP_TYPE_TUPLE_POST_UPDATE = "groupTypeTuplePostUpdate";

  /** constant for method name for: groupTypeTuplePreDelete */
  public static final String METHOD_GROUP_TYPE_TUPLE_PRE_DELETE = "groupTypeTuplePreDelete";

  /** constant for method name for: groupTypeTuplePreInsert */
  public static final String METHOD_GROUP_TYPE_TUPLE_PRE_INSERT = "groupTypeTuplePreInsert";

  /** constant for method name for: groupTypeTuplePreUpdate */
  public static final String METHOD_GROUP_TYPE_TUPLE_PRE_UPDATE = "groupTypeTuplePreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a groupTypeTuple update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void groupTypeTuplePreUpdate(HooksContext hooksContext, HooksGroupTypeTupleBean preUpdateBean) {
    
  }
  
  /**
   * called right after a groupTypeTuple update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void groupTypeTuplePostUpdate(HooksContext hooksContext, HooksGroupTypeTupleBean postUpdateBean) {
    
  }
  
  /**
   * called right before a groupTypeTuple insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void groupTypeTuplePreInsert(HooksContext hooksContext, HooksGroupTypeTupleBean preInsertBean) {
    
  }
  
  /**
   * called right after a groupTypeTuple insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void groupTypeTuplePostInsert(HooksContext hooksContext, HooksGroupTypeTupleBean postInsertBean) {
    
  }
  
  /**
   * called right before a groupTypeTuple delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void groupTypeTuplePreDelete(HooksContext hooksContext, HooksGroupTypeTupleBean preDeleteBean) {
    
  }
  
  /**
   * called right after a groupTypeTuple delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void groupTypeTuplePostDelete(HooksContext hooksContext, HooksGroupTypeTupleBean postDeleteBean) {
    
  }

  /**
   * called right after a groupTypeTuple delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void groupTypeTuplePostCommitDelete(HooksContext hooksContext, HooksGroupTypeTupleBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a groupTypeTuple insert commit
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void groupTypeTuplePostCommitInsert(HooksContext hooksContext, HooksGroupTypeTupleBean postCommitInsertBean) {
    
  }

  /**
   * called right after a groupTypeTuple update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void groupTypeTuplePostCommitUpdate(HooksContext hooksContext, HooksGroupTypeTupleBean postCommitUpdateBean) {
    
  }
  
}
