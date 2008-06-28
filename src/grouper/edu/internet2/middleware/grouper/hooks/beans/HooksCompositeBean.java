/*
 * @author mchyzer
 * $Id: HooksCompositeBean.java,v 1.2 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Composite;


/**
 * bean to hold objects for composite low level hooks
 */
public class HooksCompositeBean extends HooksBean {
  
  /** object being affected */
  private Composite composite = null;
  
  /**
   * @param theComposite
   */
  public HooksCompositeBean(Composite theComposite) {
    this.composite = theComposite;
  }
  
  /**
   * object being inserted
   * @return the Composite
   */
  public Composite getComposite() {
    return this.composite;
  }

}
