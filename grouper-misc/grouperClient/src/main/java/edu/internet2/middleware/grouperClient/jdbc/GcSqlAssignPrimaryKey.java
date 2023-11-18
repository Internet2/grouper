/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc;


/**
 * implement this interface to assign a new primary key for insert
 * @author harveycg
 */
public interface GcSqlAssignPrimaryKey {

  /**
   * assign a new primary key for insert.  return true if assigned (insert) or false if not needed
   */
  public boolean gcSqlAssignNewPrimaryKeyForInsert();
  
}
