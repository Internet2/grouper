/*
 * @author mchyzer
 * $Id: FieldHooks.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
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
   * called right after a field insert
   * @param hooksContext
   * @param postDeleteBean
   */
  public void fieldPostDelete(HooksContext hooksContext, HooksFieldBean postDeleteBean) {
    
  }
  
}
