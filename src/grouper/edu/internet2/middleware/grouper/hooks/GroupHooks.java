/*
 * @author mchyzer
 * $Id: GroupHooks.java,v 1.5 2008-07-10 06:37:18 mchyzer Exp $
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

  /** constant for method name for: groupPostCommitInsert */
  public static final String METHOD_GROUP_POST_COMMIT_INSERT = "groupPostCommitInsert";

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
   * called right after the commit of a post insert.  Note, cant veto this or participate in the tx
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
   * called right after a group insert
   * @param hooksContext
   * @param postDeleteBean
   */
  public void groupPostDelete(HooksContext hooksContext, HooksGroupBean postDeleteBean) {
    
  }
  
}
