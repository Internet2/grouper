/*
 * @author mchyzer
 * $Id: HooksCompositeBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Composite;


/**
 *
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
