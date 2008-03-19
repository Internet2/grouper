/**
 * 
 */
package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.TransactionDAO;

/**
 * @author mchyzer
 *
 */
public class Hib3TransactionDAO implements TransactionDAO {
  
  /**
   * @see edu.internet2.middleware.grouper.internal.dao.TransactionDAO#transactionActive(edu.internet2.middleware.grouper.hibernate.GrouperTransaction)
   */
  public boolean transactionActive(GrouperTransaction grouperTransaction) {
    HibernateSession hibernateSession = (HibernateSession)grouperTransaction._internal_getPayload();
    return hibernateSession == null ? false : hibernateSession.isTransactionActive();
  }

  /**
   * any runtime exceptions will propagate to the outer method call
   * @see edu.internet2.middleware.grouper.internal.dao.TransactionDAO#transactionCallback(edu.internet2.middleware.grouper.hibernate.GrouperTransactionType, edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler, edu.internet2.middleware.grouper.hibernate.GrouperTransaction)
   */
  public Object transactionCallback(
      final GrouperTransactionType grouperTransactionType,
      final GrouperTransactionHandler grouperTransactionHandler,
      final GrouperTransaction grouperTransaction) throws GrouperDAOException {
    
    Object result = HibernateSession.callbackHibernateSession(grouperTransactionType, new HibernateHandler() {

      public Object callback(HibernateSession hibernateSession) throws GrouperDAOException {
        
        //set the session object
        grouperTransaction._internal_setPayload(hibernateSession);
        try {
          return grouperTransactionHandler.callback(grouperTransaction);
        } finally {
          //clear this
          grouperTransaction._internal_setPayload(null);
        }
      }
      
    });
    
    return result;
  }

  /** 
   * @see edu.internet2.middleware.grouper.internal.dao.TransactionDAO#transactionCommit(edu.internet2.middleware.grouper.hibernate.GrouperTransaction, edu.internet2.middleware.grouper.hibernate.GrouperCommitType)
   */
  public boolean transactionCommit(GrouperTransaction grouperTransaction,
      GrouperCommitType grouperCommitType) {
    HibernateSession hibernateSession = (HibernateSession)grouperTransaction._internal_getPayload();
    return hibernateSession == null ? false : hibernateSession.commit(grouperCommitType);
  }

  /**
   * @see edu.internet2.middleware.grouper.internal.dao.TransactionDAO#transactionRollback(edu.internet2.middleware.grouper.hibernate.GrouperTransaction, edu.internet2.middleware.grouper.hibernate.GrouperRollbackType)
   */
  public boolean transactionRollback(GrouperTransaction grouperTransaction,
      GrouperRollbackType grouperRollbackType) {
    HibernateSession hibernateSession = (HibernateSession)grouperTransaction._internal_getPayload();
    return hibernateSession == null ? false : hibernateSession.rollback(grouperRollbackType);
  }

}
