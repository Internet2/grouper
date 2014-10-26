/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.jdbc;


/**
 * implement this interface to assign a new primary key for insert
 */
public interface GcSqlAssignPrimaryKey {

  /**
   * assign a new primary key for insert
   */
  public void gcSqlAssignNewPrimaryKeyForInsert();
  
}
