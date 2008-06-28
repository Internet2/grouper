/*
 * @author mchyzer
 * $Id: CompositeHooks.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksCompositeBean;


/**
 * Extend this class and configure in grouper.properties for hooks on 
 * composite related actions
 */
public abstract class CompositeHooks {

  //*****  START GENERATED WITH GenerateMethodConstants.java *****//

  /** constant for method name for: compositePostDelete */
  public static final String METHOD_COMPOSITE_POST_DELETE = "compositePostDelete";

  /** constant for method name for: compositePostInsert */
  public static final String METHOD_COMPOSITE_POST_INSERT = "compositePostInsert";

  /** constant for method name for: compositePostUpdate */
  public static final String METHOD_COMPOSITE_POST_UPDATE = "compositePostUpdate";

  /** constant for method name for: compositePreDelete */
  public static final String METHOD_COMPOSITE_PRE_DELETE = "compositePreDelete";

  /** constant for method name for: compositePreInsert */
  public static final String METHOD_COMPOSITE_PRE_INSERT = "compositePreInsert";

  /** constant for method name for: compositePreUpdate */
  public static final String METHOD_COMPOSITE_PRE_UPDATE = "compositePreUpdate";

  //*****  END GENERATED WITH GenerateMethodConstants.java *****//

  /**
   * called right before a composite update
   * @param hooksContext
   * @param preUpdateBean
   */
  public void compositePreUpdate(HooksContext hooksContext, HooksCompositeBean preUpdateBean) {
    
  }
  
  /**
   * called right after a composite update
   * @param hooksContext
   * @param postUpdateBean
   */
  public void compositePostUpdate(HooksContext hooksContext, HooksCompositeBean postUpdateBean) {
    
  }
  
  /**
   * called right before a composite insert
   * @param hooksContext
   * @param preInsertBean
   */
  public void compositePreInsert(HooksContext hooksContext, HooksCompositeBean preInsertBean) {
    
  }
  
  /**
   * called right after a composite insert
   * @param hooksContext
   * @param postInsertBean
   */
  public void compositePostInsert(HooksContext hooksContext, HooksCompositeBean postInsertBean) {
    
  }
  
  /**
   * called right before a composite delete
   * @param hooksContext
   * @param preDeleteBean
   */
  public void compositePreDelete(HooksContext hooksContext, HooksCompositeBean preDeleteBean) {
    
  }
  
  /**
   * called right after a composite insert
   * @param hooksContext
   * @param postDeleteBean
   */
  public void compositePostDelete(HooksContext hooksContext, HooksCompositeBean postDeleteBean) {
    
  }
  
}
