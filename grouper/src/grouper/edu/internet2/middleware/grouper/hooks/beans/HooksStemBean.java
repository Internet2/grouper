/*
 * @author mchyzer
 * $Id: HooksStemBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Stem;


/**
 *
 */
public class HooksStemBean extends HooksBean {
  
  /** object being affected */
  private Stem stem = null;
  
  /**
   * @param theStem
   */
  public HooksStemBean(Stem theStem) {
    this.stem = theStem;
  }
  
  /**
   * object being inserted
   * @return the Stem
   */
  public Stem getStem() {
    return this.stem;
  }

}
