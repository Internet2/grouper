/*
 * @author mchyzer
 * $Id: HooksStemPreUpdateBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Stem;


/**
 * pre insert bean
 */
public class HooksStemPreUpdateBean extends HooksStemBean {

  /**
   * @param theStem
   */
  public HooksStemPreUpdateBean(Stem theStem) {
    super(theStem);
  }

}
