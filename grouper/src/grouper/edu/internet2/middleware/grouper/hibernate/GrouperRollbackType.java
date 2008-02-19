/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

/**
 * Various types of rolling back
 * @author mchyzer
 *
 */
public enum GrouperRollbackType {
  /** always rollback right now */
  ROLLBACK_NOW,
  
  /** only rollback if this is a new transaction */
  ROLLBACK_IF_NEW_TRANSACTION;
}
