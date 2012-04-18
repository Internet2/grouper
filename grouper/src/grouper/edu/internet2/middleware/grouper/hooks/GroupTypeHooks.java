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
 * $Id: GroupTypeHooks.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupTypeBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * groupType related actions
 */
public abstract class GroupTypeHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: groupTypePostCommitDelete */
  public static final String METHOD_GROUP_TYPE_POST_COMMIT_DELETE = "groupTypePostCommitDelete";

  /** constant for method name for: groupTypePostCommitInsert */
  public static final String METHOD_GROUP_TYPE_POST_COMMIT_INSERT = "groupTypePostCommitInsert";

  /** constant for method name for: groupTypePostCommitUpdate */
  public static final String METHOD_GROUP_TYPE_POST_COMMIT_UPDATE = "groupTypePostCommitUpdate";

  /** constant for method name for: groupTypePostDelete */
  public static final String METHOD_GROUP_TYPE_POST_DELETE = "groupTypePostDelete";

  /** constant for method name for: groupTypePostInsert */
  public static final String METHOD_GROUP_TYPE_POST_INSERT = "groupTypePostInsert";

  /** constant for method name for: groupTypePostUpdate */
  public static final String METHOD_GROUP_TYPE_POST_UPDATE = "groupTypePostUpdate";

  /** constant for method name for: groupTypePreDelete */
  public static final String METHOD_GROUP_TYPE_PRE_DELETE = "groupTypePreDelete";

  /** constant for method name for: groupTypePreInsert */
  public static final String METHOD_GROUP_TYPE_PRE_INSERT = "groupTypePreInsert";

  /** constant for method name for: groupTypePreUpdate */
  public static final String METHOD_GROUP_TYPE_PRE_UPDATE = "groupTypePreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a groupType update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void groupTypePreUpdate(HooksContext hooksContext, HooksGroupTypeBean preUpdateBean) {
    
  }
  
  /**
   * called right after a groupType update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void groupTypePostUpdate(HooksContext hooksContext, HooksGroupTypeBean postUpdateBean) {
    
  }
  
  /**
   * called right before a groupType insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void groupTypePreInsert(HooksContext hooksContext, HooksGroupTypeBean preInsertBean) {
    
  }
  
  /**
   * called right after a groupType insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void groupTypePostInsert(HooksContext hooksContext, HooksGroupTypeBean postInsertBean) {
    
  }
  
  /**
   * called right before a groupType delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void groupTypePreDelete(HooksContext hooksContext, HooksGroupTypeBean preDeleteBean) {
    
  }
  
  /**
   * called right after a groupType delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void groupTypePostDelete(HooksContext hooksContext, HooksGroupTypeBean postDeleteBean) {
    
  }

  /**
   * called right after a groupType delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void groupTypePostCommitDelete(HooksContext hooksContext, HooksGroupTypeBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a groupType insert commit
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void groupTypePostCommitInsert(HooksContext hooksContext, HooksGroupTypeBean postCommitInsertBean) {
    
  }

  /**
   * called right after a groupType update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void groupTypePostCommitUpdate(HooksContext hooksContext, HooksGroupTypeBean postCommitUpdateBean) {
    
  }
  
}
