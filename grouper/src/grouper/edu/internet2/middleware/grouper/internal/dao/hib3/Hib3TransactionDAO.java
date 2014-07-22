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
package edu.internet2.middleware.grouper.internal.dao.hib3;

import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
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
    
    Object result = HibernateSession.callbackHibernateSession(
        grouperTransactionType, AuditControl.WILL_NOT_AUDIT, new HibernateHandler() {

      public Object callback(HibernateHandlerBean hibernateHandlerBean)
          throws GrouperDAOException {
        HibernateSession hibernateSession = hibernateHandlerBean.getHibernateSession();

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
