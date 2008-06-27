/*
 * @author mchyzer
 * $Id: HooksStemPreInsertBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Stem;


/**
 * pre insert bean
 */
public class HooksStemPreInsertBean extends HooksStemBean {

  /**
   * @param theStem
   */
  public HooksStemPreInsertBean(Stem theStem) {
    super(theStem);
  }

}
