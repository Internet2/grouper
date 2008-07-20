/*
 * @author mchyzer
 * $Id: GrouperContextType.java,v 1.3 2008-07-20 21:18:57 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.beans;


/**
 * Get the current context type from GrouperBuiltInContextType.currentGrouperContext()
 */
public interface GrouperContextType {

  /**
   * 
   * @return the name
   */
  public String name();
  
}
