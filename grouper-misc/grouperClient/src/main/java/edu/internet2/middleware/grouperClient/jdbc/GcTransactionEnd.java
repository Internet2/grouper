/**
 * 
 */
package edu.internet2.middleware.grouperClient.jdbc;

/**
 * Possible endings for a transaction.
 * @author harveycg
 *
 */
public enum GcTransactionEnd {
  /**
   * Commit it.
   */
  commit,
  
  /**
   * Roll it back.
   */
  rollback;
}
