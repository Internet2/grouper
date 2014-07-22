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
 * $Id: GroupHooks.java,v 1.6 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * group related actions
 */
public abstract class GroupHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: groupPostCommitDelete */
  public static final String METHOD_GROUP_POST_COMMIT_DELETE = "groupPostCommitDelete";

  /** constant for method name for: groupPostCommitInsert */
  public static final String METHOD_GROUP_POST_COMMIT_INSERT = "groupPostCommitInsert";

  /** constant for method name for: groupPostCommitUpdate */
  public static final String METHOD_GROUP_POST_COMMIT_UPDATE = "groupPostCommitUpdate";

  /** constant for method name for: groupPostDelete */
  public static final String METHOD_GROUP_POST_DELETE = "groupPostDelete";

  /** constant for method name for: groupPostInsert */
  public static final String METHOD_GROUP_POST_INSERT = "groupPostInsert";

  /** constant for method name for: groupPostUpdate */
  public static final String METHOD_GROUP_POST_UPDATE = "groupPostUpdate";

  /** constant for method name for: groupPreDelete */
  public static final String METHOD_GROUP_PRE_DELETE = "groupPreDelete";

  /** constant for method name for: groupPreInsert */
  public static final String METHOD_GROUP_PRE_INSERT = "groupPreInsert";

  /** constant for method name for: groupPreUpdate */
  public static final String METHOD_GROUP_PRE_UPDATE = "groupPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//  
  /**
   * called right before a group update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void groupPreUpdate(HooksContext hooksContext, HooksGroupBean preUpdateBean) {
    
  }
  
  /**
   * called right after a group update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void groupPostUpdate(HooksContext hooksContext, HooksGroupBean postUpdateBean) {
    
  }
  
  /**
   * called right before a group insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {
    
  }
  
  /**
   * called right after a group insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void groupPostInsert(HooksContext hooksContext, HooksGroupBean postInsertBean) {
    
  }
  
  /**
   * called right after the commit of a post insert commit.  Note, cant veto this or participate in the tx
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void groupPostCommitInsert(HooksContext hooksContext, HooksGroupBean postCommitInsertBean) {
    
  }
  
  /**
   * called right before a group delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void groupPreDelete(HooksContext hooksContext, HooksGroupBean preDeleteBean) {
    
  }
  
  /**
   * called right after a group delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void groupPostDelete(HooksContext hooksContext, HooksGroupBean postDeleteBean) {
    
  }

  /**
   * called right after a commit involving a group delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void groupPostCommitDelete(HooksContext hooksContext, HooksGroupBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a commit on a group update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void groupPostCommitUpdate(HooksContext hooksContext, HooksGroupBean postCommitUpdateBean) {
    
  }
  
}
