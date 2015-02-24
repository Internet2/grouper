/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperAtlassianConnector.db;

import edu.internet2.middleware.grouperClient.jdbc.GcSqlAssignPrimaryKey;


/**
 *
 */
public interface AtlassianUserMapping extends GcSqlAssignPrimaryKey {

  /**
   * user key col
   * @return the user key
   */
  public String getUserKey();
  
  /**
   * user key col
   * @param userKey1 user key to set
   */
  public void setUserKey(String userKey1);

  /**
   * user_name col
   * @return the userName
   */
  public String getUsername();
  
  /**
   * user_name col
   * @param userName1 the userName to set
   */
  public void setUsername(String userName1);
  
  /**
   * lower user name col
   * @return lower user name col
   */
  public String getLowerUsername();
  
  /**
   * lower user name
   * @param lowerUserName1
   */
  public void setLowerUsername(String lowerUserName1);

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public abstract String toString();


  /**
   * note, you need to commit this (or at least flush) so the id is incremented
   */
  public void initNewObject();  

  /**
   * store this record insert or update
   */
  public void store();

  /**
   * delete this record
   */
  public void delete();

  

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode();

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj);
  
}
