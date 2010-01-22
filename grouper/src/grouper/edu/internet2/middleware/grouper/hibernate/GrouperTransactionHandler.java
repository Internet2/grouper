/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;

/**
 * Use this class to make your anonymous inner class for 
 * transactions with grouper (if transactions are supported by your DAO strategy
 * configured in grouper.properties)
 * 
 * 
 * @author mchyzer
 *
 */
public interface GrouperTransactionHandler {
  
  /**
   * This method will be called with the grouper transaction object to do 
   * what you wish.  Note, RuntimeExceptions can be
   * thrown by this method... others should be handled somehow.
   * @param grouperTransaction is the grouper transaction
   * @return the return value to be passed to return value of callback method
   * @throws GrouperDAOException if there is a problem, or runtime ones
   */
  public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException;

}
