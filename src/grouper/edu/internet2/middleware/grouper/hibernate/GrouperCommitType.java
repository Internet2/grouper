/**
 * 
 */
package edu.internet2.middleware.grouper.hibernate;

/**
 * Various types of committing
 * @author mchyzer
 *
 */
public enum GrouperCommitType {
  /** always commit right now */
  COMMIT_NOW,
  
  /** only commit if this is a new transaction */
  COMMIT_IF_NEW_TRANSACTION;
  
}
