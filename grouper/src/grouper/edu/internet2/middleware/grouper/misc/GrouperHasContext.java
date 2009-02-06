/*
 * @author mchyzer
 * $Id: GrouperHasContext.java,v 1.1 2009-02-06 16:33:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.misc;


/**
 * if the object keeps a context id
 */
public interface GrouperHasContext {

  /**
   * setter for context
   * @param contextId1
   */
  public void setContextId(String contextId1);
  
}
