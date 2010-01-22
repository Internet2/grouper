/*
 * @author mchyzer
 * $Id: HooksBean.java,v 1.4 2008-07-11 05:11:28 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.misc.GrouperCloneable;


/**
 * base bean for hooks
 */
public abstract class HooksBean implements GrouperCloneable {

  /**
   * deep clone the fields in this object
   */
  @Override
  public abstract HooksBean clone();
}
