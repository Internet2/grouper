/*
 * @author mchyzer
 * $Id: GrouperSessionHooks.java,v 1.2 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksGrouperSessionBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * grouperSession related actions
 */
public abstract class GrouperSessionHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: grouperSessionPostCommitDelete */
  public static final String METHOD_GROUPER_SESSION_POST_COMMIT_DELETE = "grouperSessionPostCommitDelete";

  /** constant for method name for: grouperSessionPostCommitInsert */
  public static final String METHOD_GROUPER_SESSION_POST_COMMIT_INSERT = "grouperSessionPostCommitInsert";

  /** constant for method name for: grouperSessionPostCommitUpdate */
  public static final String METHOD_GROUPER_SESSION_POST_COMMIT_UPDATE = "grouperSessionPostCommitUpdate";

  /** constant for method name for: grouperSessionPostDelete */
  public static final String METHOD_GROUPER_SESSION_POST_DELETE = "grouperSessionPostDelete";

  /** constant for method name for: grouperSessionPostInsert */
  public static final String METHOD_GROUPER_SESSION_POST_INSERT = "grouperSessionPostInsert";

  /** constant for method name for: grouperSessionPostUpdate */
  public static final String METHOD_GROUPER_SESSION_POST_UPDATE = "grouperSessionPostUpdate";

  /** constant for method name for: grouperSessionPreDelete */
  public static final String METHOD_GROUPER_SESSION_PRE_DELETE = "grouperSessionPreDelete";

  /** constant for method name for: grouperSessionPreInsert */
  public static final String METHOD_GROUPER_SESSION_PRE_INSERT = "grouperSessionPreInsert";

  /** constant for method name for: grouperSessionPreUpdate */
  public static final String METHOD_GROUPER_SESSION_PRE_UPDATE = "grouperSessionPreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a grouperSession update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void grouperSessionPreUpdate(HooksContext hooksContext, HooksGrouperSessionBean preUpdateBean) {
    
  }
  
  /**
   * called right after a grouperSession update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void grouperSessionPostUpdate(HooksContext hooksContext, HooksGrouperSessionBean postUpdateBean) {
    
  }
  
  /**
   * called right before a grouperSession insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void grouperSessionPreInsert(HooksContext hooksContext, HooksGrouperSessionBean preInsertBean) {
    
  }
  
  /**
   * called right after a grouperSession insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void grouperSessionPostInsert(HooksContext hooksContext, HooksGrouperSessionBean postInsertBean) {
    
  }
  
  /**
   * called right before a grouperSession delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void grouperSessionPreDelete(HooksContext hooksContext, HooksGrouperSessionBean preDeleteBean) {
    
  }
  
  /**
   * called right after a grouperSession delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void grouperSessionPostDelete(HooksContext hooksContext, HooksGrouperSessionBean postDeleteBean) {
    
  }

  /**
   * called right after a grouperSession delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void grouperSessionPostCommitDelete(HooksContext hooksContext, HooksGrouperSessionBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a grouperSession insert commit
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void grouperSessionPostCommitInsert(HooksContext hooksContext, HooksGrouperSessionBean postCommitInsertBean) {
    
  }

  /**
   * called right after a grouperSession update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void grouperSessionPostCommitUpdate(HooksContext hooksContext, HooksGrouperSessionBean postCommitUpdateBean) {
    
  }
  
}
