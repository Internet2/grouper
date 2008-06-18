/*
 * @author mchyzer
 * $Id: HooksBean.java,v 1.1.2.1 2008-06-09 05:52:52 mchyzer Exp $
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
