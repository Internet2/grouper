/*
 * @author mchyzer
 * $Id: HooksFieldBean.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;

import edu.internet2.middleware.grouper.Field;


/**
 * bean to hold objects for field low level hooks
 */
public class HooksFieldBean extends HooksBean {
  
  /** object being affected */
  private Field field = null;
  
  /**
   * @param theGroup
   */
  public HooksFieldBean(Field theField) {
    this.field = theField;
  }
  
  /**
   * object being inserted
   * @return the Field
   */
  public Field getField() {
    return this.field;
  }

}
