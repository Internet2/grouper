/*
 * Created on Jun 21, 2005
 *
 */
package edu.internet2.middleware.grouper;


/**
 * Implement this (usually in an anonymous inner class) to get a 
 * reference to the grouper session object
 * @version $Id: GrouperSessionHandler.java,v 1.1 2008-06-24 06:07:03 mchyzer Exp $
 * @author mchyzer
 */

public interface GrouperSessionHandler {

  /**
   * This method will be called with the grouper session object to do 
   * what you wish.  
   * @param grouperSession is the grouper session, note, this grouperSession 
   * will be the same as passed in
   * @return the return value to be passed to return value of callback method
   * @throws GrouperSessionException 
   */
  public Object callback(GrouperSession grouperSession) throws GrouperSessionException;
}
