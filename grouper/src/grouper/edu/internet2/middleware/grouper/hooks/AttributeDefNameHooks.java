/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksAttributeDefNameBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * new attribute framework related actions
 */
public abstract class AttributeDefNameHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: attributeDefNamePostCommitDelete */
  public static final String METHOD_ATTRIBUTE_DEF_NAME_POST_COMMIT_DELETE = "attributeDefNamePostCommitDelete";

  /** constant for method name for: attributeDefNamePostCommitInsert */
  public static final String METHOD_ATTRIBUTE_DEF_NAME_POST_COMMIT_INSERT = "attributeDefNamePostCommitInsert";

  /** constant for method name for: attributeDefNamePostCommitUpdate */
  public static final String METHOD_ATTRIBUTE_DEF_NAME_POST_COMMIT_UPDATE = "attributeDefNamePostCommitUpdate";

  /** constant for method name for: attributeDefNamePostDelete */
  public static final String METHOD_ATTRIBUTE_DEF_NAME_POST_DELETE = "attributeDefNamePostDelete";

  /** constant for method name for: attributeDefNamePostInsert */
  public static final String METHOD_ATTRIBUTE_DEF_NAME_POST_INSERT = "attributeDefNamePostInsert";

  /** constant for method name for: attributeDefNamePostUpdate */
  public static final String METHOD_ATTRIBUTE_DEF_NAME_POST_UPDATE = "attributeDefNamePostUpdate";

  /** constant for method name for: attributeDefNamePreDelete */
  public static final String METHOD_ATTRIBUTE_DEF_NAME_PRE_DELETE = "attributeDefNamePreDelete";

  /** constant for method name for: attributeDefNamePreInsert */
  public static final String METHOD_ATTRIBUTE_DEF_NAME_PRE_INSERT = "attributeDefNamePreInsert";

  /** constant for method name for: attributeDefNamePreUpdate */
  public static final String METHOD_ATTRIBUTE_DEF_NAME_PRE_UPDATE = "attributeDefNamePreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a attribute update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void attributeDefNamePreUpdate(HooksContext hooksContext, HooksAttributeDefNameBean preUpdateBean) {
  }
  
  /**
   * called right after a attribute update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void attributeDefNamePostUpdate(HooksContext hooksContext, HooksAttributeDefNameBean postUpdateBean) {
    
  }
  
  /**
   * called right before a attribute insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void attributeDefNamePreInsert(HooksContext hooksContext, HooksAttributeDefNameBean preInsertBean) {
    
  }
  
  /**
   * called right after a attribute insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void attributeDefNamePostInsert(HooksContext hooksContext, HooksAttributeDefNameBean postInsertBean) {
    
  }
  
  /**
   * called right after the commit of a post insert commit.  Note, cant veto this or participate in the tx
   * @param hooksContext
   * @param postCommitInsertBean
   */
  public void attributeDefNamePostCommitInsert(HooksContext hooksContext, HooksAttributeDefNameBean postCommitInsertBean) {
    
  }
  
  /**
   * called right before a attribute delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void attributeDefNamePreDelete(HooksContext hooksContext, HooksAttributeDefNameBean preDeleteBean) {
    
  }
  
  /**
   * called right after a attribute delete
   * @param hooksContext
   * @param postDeleteBean
   */
  public void attributeDefNamePostDelete(HooksContext hooksContext, HooksAttributeDefNameBean postDeleteBean) {
    
  }

  /**
   * called right after a commit involving a attribute delete commit
   * @param hooksContext
   * @param postCommitDeleteBean
   */
  public void attributeDefNamePostCommitDelete(HooksContext hooksContext, HooksAttributeDefNameBean postCommitDeleteBean) {
    
  }

  /**
   * called right after a commit on a attribute update commit
   * @param hooksContext
   * @param postCommitUpdateBean
   */
  public void attributeDefNamePostCommitUpdate(HooksContext hooksContext, HooksAttributeDefNameBean postCommitUpdateBean) {
    
  }
  
}
