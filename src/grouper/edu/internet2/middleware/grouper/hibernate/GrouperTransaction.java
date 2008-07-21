/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;

/**
 * 
 * Use this class to make a transaction around grouper operations
 * (can also use HibernateSession, thoguh if hib it will throw exceptions)
 * 
 * @author mchyzer
 *
 */
public class GrouperTransaction {
  
  /**
   * the dao can store some state here
   */
  private Object payload;
  
  /**
   * the dao can store some state here
   * @return the payload
   */
  public Object _internal_getPayload() {
    return payload;
  }

  /**
   * the dao can store some state here
   * @param payload the payload to set
   */
  public void _internal_setPayload(Object payload) {
    this.payload = payload;
  }

  /**
   * call this to establish a transaction demarcation for the GrouperTransactionHandler business logic
   * 
   * @param grouperTransactionType
   *          is enum of how the transaction should work.
   * @param grouperTransactionHandler
   *          will get the callback
   * @return the object returned from the callback
   * @throws RuntimeException if something wrong inside, not sure which exceptions... its
   * whatever your methods throw
   */
  public static Object callbackGrouperTransaction(
      GrouperTransactionType grouperTransactionType,
      GrouperTransactionHandler grouperTransactionHandler) {
    
    return GrouperDAOFactory.getFactory().getTransaction().transactionCallback(grouperTransactionType,
        grouperTransactionHandler, new GrouperTransaction());

  }

  /**
   * call this to establish a transaction demarcation for the GrouperTransactionHandler business logic.
   * The transaction type will be READ_WRITE_OR_USE_EXISTING (this is the default since probably
   * is what is wanted).
   * 
   * @param grouperTransactionHandler
   *          will get the callback
   * @return the object returned from the callback
   * @throws RuntimeException if something wrong inside, not sure which exceptions... its
   * whatever your methods throw
   */
  public static Object callbackGrouperTransaction(
      GrouperTransactionHandler grouperTransactionHandler) {
    
    return callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
        grouperTransactionHandler);
  }

  /**
   * commit a transaction (perhaps, based on type)
   * @param grouperCommitType
   * @return if the tx committed
   */
  public boolean commit(GrouperCommitType grouperCommitType) {
    return GrouperDAOFactory.getFactory().getTransaction().transactionCommit(this, grouperCommitType);
  }
  
  /**
   * rollback a transaction (perhaps, based on type)
   * @param grouperRollbackType
   * @return if the tx rolled back
   */
  public boolean rollback(GrouperRollbackType grouperRollbackType) {
    return GrouperDAOFactory.getFactory().getTransaction().transactionRollback(this, grouperRollbackType);
  }

  /**
   * see if a transaction has an open transaction (that hasnt been committed or rolled back yet)
   * @return true if transaction exists and active (not committed or rolled back)
   */
  public boolean isTransactionActive() {
    return GrouperDAOFactory.getFactory().getTransaction().transactionActive(this);
  }

}
