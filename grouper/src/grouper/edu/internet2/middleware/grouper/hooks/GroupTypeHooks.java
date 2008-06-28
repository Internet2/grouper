/*
 * @author mchyzer
 * $Id: GroupTypeHooks.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
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
   * called right after a groupType insert
   * @param hooksContext
   * @param postDeleteBean
   */
  public void groupTypePostDelete(HooksContext hooksContext, HooksGroupTypeBean postDeleteBean) {
    
  }
  
}
