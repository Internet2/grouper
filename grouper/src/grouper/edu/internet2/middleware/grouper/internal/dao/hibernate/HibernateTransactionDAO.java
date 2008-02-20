package edu.internet2.middleware.grouper.internal.dao.hibernate;

import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.TransactionDAO;

/**
 * hib2 implementation doesnt support transactions, so this class doesnt do much
 * @author mchyzer
 *
 */
public class HibernateTransactionDAO implements TransactionDAO {

  /**
   * this will always return false since no transactions
   * @return if active (though always returns false)
   */
  public boolean transactionActive(GrouperTransaction grouperTransaction) {
    return false;
  }

  /**
   * just invoke the logic since transactions arent supported
   */
  public Object transactionCallback(
      GrouperTransactionType grouperTransactionType,
      GrouperTransactionHandler grouperTransactionHandler,
      GrouperTransaction grouperTransaction) throws GrouperDAOException {
    return grouperTransactionHandler.callback(grouperTransaction);
  }

  /**
   * commit based on the commit type.  always return false since not supporting tx's
   * @return true if it was actually committed
   */
  public boolean transactionCommit(GrouperTransaction grouperTransaction,
      GrouperCommitType grouperCommitType) {
   return false;
  }
  
  /**
   * rollback based on rollback type.  always return false since not supporting tx's
   * @return true if it was actually rolled back
   */
  public boolean transactionRollback(GrouperTransaction grouperTransaction,
      GrouperRollbackType grouperRollbackType) {
    return false;
  }

}
