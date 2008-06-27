/*
 * @author mchyzer
 * $Id: HooksCompositePreUpdateBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Composite;


/**
 * pre insert bean
 */
public class HooksCompositePreUpdateBean extends HooksCompositeBean {

  /**
   * @param theComposite
   */
  public HooksCompositePreUpdateBean(Composite theComposite) {
    super(theComposite);
  }

}
