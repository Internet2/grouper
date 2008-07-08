/*
 * @author mchyzer
 * $Id: HookAsynchronousHandler.java,v 1.1 2008-07-08 20:47:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.logic;

import edu.internet2.middleware.grouper.hooks.beans.HooksBean;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;


/**
 * handler when doing an asynchronous hook, normally you would do this in
 * an anonymous inner class
 */
public interface HookAsynchronousHandler {

  /**
   * implement this as the callback to the asynchronous hook
   * @param hooksContext will be massaged to be threadsafe
   * @param hooksBean
   */
  public void callback(HooksContext hooksContext, HooksBean hooksBean);
  
}
