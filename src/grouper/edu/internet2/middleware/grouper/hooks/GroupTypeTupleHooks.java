/*
 * @author mchyzer
 * $Id: GroupTypeTupleHooks.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
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
   * called right after a groupTypeTuple insert
   * @param hooksContext
   * @param postDeleteBean
   */
  public void groupTypeTuplePostDelete(HooksContext hooksContext, HooksGroupTypeTupleBean postDeleteBean) {
    
  }
  
}
