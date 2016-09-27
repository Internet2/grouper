/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperBox;


/**
 *
 */
public class GrouperDuoUser {

  /**
   * 
   */
  public GrouperDuoUser() {
  }

  /**
   * duo user id
   */
  private String userId;
  
  /**
   * username (netid)
   */
  private String username;

  
  /**
   * duo user id
   * @return the userId
   */
  public String getUserId() {
    return this.userId;
  }

  
  /**
   * duo user id
   * @param userId1 the userId to set
   */
  public void setUserId(String userId1) {
    this.userId = userId1;
  }

  
  /**
   * username (netid)
   * @return the username
   */
  public String getUsername() {
    return this.username;
  }

  
  /**
   * username (netid)
   * @param username1 the username to set
   */
  public void setUsername(String username1) {
    this.username = username1;
  }


  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "GrouperDuoUser [userId=" + this.userId + ", username=" + this.username + "]";
  }

  
}
