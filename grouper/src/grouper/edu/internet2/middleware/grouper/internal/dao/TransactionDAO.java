/**
 * 
 */
package edu.internet2.middleware.grouper.internal.dao;

import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;

/** 
 * methods for dealing with transactions
 * @author mchyzer
 *
 */
public interface TransactionDAO {
  /**
   * call this to send a callback for the grouper methods.  This shouldnt be called directly,
   * it should filter through the GrouperTransaction.callback... method
   * 
   * @param grouperTransactionType
   *          is enum of how the transaction should work.
   * @param grouperTransactionHandler
   *          will get the callback
   *          
   * @param grouperTransaction is the state of the transaction, can hold payload
   * @return the object returned from the callback
   * @throws GrouperDAOException if something wrong inside, its
   * whatever your methods throw
   */
  public abstract Object transactionCallback(
      GrouperTransactionType grouperTransactionType,
      GrouperTransactionHandler grouperTransactionHandler,
      GrouperTransaction grouperTransaction) throws GrouperDAOException;

  /**
   * call this to commit a transaction
   * 
   * @param grouperCommitType
   *          type of commit (now or only under certain circumstances?)
   * @param grouperTransaction is the state of the transaction, can hold payload
   * @return if committed
   */
  public boolean transactionCommit(
      GrouperTransaction grouperTransaction, GrouperCommitType grouperCommitType);

  /**
   * call this to rollback a transaction
   * 
   * @param grouperRollbackType
   *          type of commit (now or only under certain circumstances?)
   * @param grouperTransaction is the state of the transaction, can hold payload
   * @return if rolled back
   */
  public boolean transactionRollback(
      GrouperTransaction grouperTransaction, GrouperRollbackType grouperRollbackType);

  /**
   * call this to see if a transaction is active (exists and not committed or rolledback)
   * 
   * @param grouperTransaction is the state of the transaction, can hold payload
   * @return the object returned from the callback
   */
  public abstract boolean transactionActive(
      GrouperTransaction grouperTransaction);

}
