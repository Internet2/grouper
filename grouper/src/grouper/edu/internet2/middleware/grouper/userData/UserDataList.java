/**
 * 
 */
package edu.internet2.middleware.grouper.userData;

/**
 * list of user data objects
 * @author mchyzer
 *
 */
public class UserDataList {

  /**
   * 
   */
  public UserDataList() {
    super();
  }

  /**
   * construct with field
   * @param list
   */
  public UserDataList(UserDataObject[] list) {
    super();
    this.list = list;
  }

  /**
   * list of objects
   */
  private UserDataObject[] list;

  
  /**
   * list of objects
   * @return the list
   */
  public UserDataObject[] getList() {
    return list;
  }

  /**
   * list of objects
   * @param list1
   */
  public void setList(UserDataObject[] list1) {
    this.list = list1;
  }
  
  
  
}
