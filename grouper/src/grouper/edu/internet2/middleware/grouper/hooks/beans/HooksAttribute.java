/*
 * @author mchyzer
 * $Id: HooksAttribute.java,v 1.1.2.1 2008-06-09 05:52:52 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;


/**
 * attribute, and if threadsafe
 */
public class HooksAttribute {

  /**
   * if this is threadsafe
   */
  private boolean threadSafe;
  
  /**
   * value of the attribute
   */
  private Object value;

  /**
   * @param threadSafe
   * @param value
   */
  public HooksAttribute(boolean threadSafe, Object value) {
    super();
    this.threadSafe = threadSafe;
    this.value = value;
  }

  /**
   * @return the threadSafe
   */
  boolean isThreadSafe() {
    return this.threadSafe;
  }

  
  /**
   * @return the value
   */
  Object getValue() {
    return this.value;
  }

  
}
