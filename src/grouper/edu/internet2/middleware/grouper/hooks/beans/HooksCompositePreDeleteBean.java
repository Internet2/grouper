/*
 * @author mchyzer
 * $Id: HooksCompositePreDeleteBean.java,v 1.1 2008-06-27 19:04:04 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Composite;


/**
 * pre insert bean
 */
public class HooksCompositePreDeleteBean extends HooksCompositeBean {

  /**
   * @param theComposite
   */
  public HooksCompositePreDeleteBean(Composite theComposite) {
    super(theComposite);
  }

}
