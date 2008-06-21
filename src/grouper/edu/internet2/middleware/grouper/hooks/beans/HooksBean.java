/*
 * @author mchyzer
 * $Id: HooksBean.java,v 1.2 2008-06-21 04:16:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;


/**
 * base bean for hooks
 */
public abstract class HooksBean {

  /** context for hook */
  private HooksContext hooksContext;
  
  /**
   * construct with context
   * @param theHooksContext
   */
  public HooksBean(HooksContext theHooksContext) {
    this.hooksContext = theHooksContext;
  }
  
  /**
   * @return the hooksContext
   */
  public HooksContext getHooksContext() {
    return this.hooksContext;
  }
  
}
