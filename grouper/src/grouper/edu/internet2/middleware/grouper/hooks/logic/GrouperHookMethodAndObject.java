/*
 * @author mchyzer
 * $Id: GrouperHookMethodAndObject.java,v 1.1 2008-07-10 05:55:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import java.lang.reflect.Method;


/**
 * structure to hold method and class to be called for hook
 */
public class GrouperHookMethodAndObject {

  /**
   * method, cached
   */
  private Method hookMethod = null;
  
  /**
   * isntance of logic class, cached
   */
  private Object hookLogicInstance = null;

  /**
   * @param hookMethod
   * @param hookLogicInstance
   */
  public GrouperHookMethodAndObject(Method hookMethod, Object hookLogicInstance) {
    super();
    this.hookMethod = hookMethod;
    this.hookLogicInstance = hookLogicInstance;
  }

  
  /**
   * @return the hookMethod
   */
  public Method getHookMethod() {
    return this.hookMethod;
  }

  
  /**
   * @return the hookLogicInstance
   */
  public Object getHookLogicInstance() {
    return this.hookLogicInstance;
  }
  
  
}
