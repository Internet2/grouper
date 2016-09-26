package edu.internet2.middleware.grouperBox;

import java.util.Map;

import com.box.sdk.BoxUser;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * grouper box user
 * @author mchyzer
 *
 */
public class GrouperBoxUser {

  /**
   * cache connections
   */
  private static ExpirableCache<Boolean, Map<String, GrouperBoxUser>> retrieveUsersCache = new ExpirableCache<Boolean, Map<String, GrouperBoxUser>>(5);
  
  /**
   * 
   * @return box api connection
   */
  public synchronized static Map<String, GrouperBoxUser> retrieveUsers() {
    
    Map<String, GrouperBoxUser> usersMap = retrieveUsersCache.get(Boolean.TRUE);
    
    if (usersMap == null) {
      
      usersMap = GrouperBoxCommands.retrieveUsers();
      
      retrieveUsersCache.put(Boolean.TRUE, usersMap);
    }
    
    return usersMap;
  }
  
  /**
   * 
   * @param boxUser1
   */
  public GrouperBoxUser(BoxUser boxUser1) {
    this.boxUser = boxUser1;
  }
  
  /**
   * 
   * @param boxUser1
   */
  public GrouperBoxUser(BoxUser boxUser1, BoxUser.Info boxUserInfo1) {
    this.boxUser = boxUser1;
    this.boxUserInfo = boxUserInfo1;
  }
  
  /**
   * box user
   */
  private BoxUser boxUser;
  
  /**
   * box user info
   */
  private BoxUser.Info boxUserInfo;

  /**
   * 
   * @return box user
   */
  public BoxUser getBoxUser() {
    return this.boxUser;
  }

  /**
   * lazy load this
   * @return box user info
   */
  public BoxUser.Info getBoxUserInfo() {
    if (this.boxUserInfo == null) {
      this.boxUserInfo = this.boxUser.getInfo();
    }
    return this.boxUserInfo;
  }

  
  
}
