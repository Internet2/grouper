/**
 * Copyright 2012 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
