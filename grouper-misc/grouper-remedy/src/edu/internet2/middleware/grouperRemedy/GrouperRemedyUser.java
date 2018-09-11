package edu.internet2.middleware.grouperRemedy;

import java.util.Map;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;

/**
 * grouper box user
 * @author mchyzer
 *
 */
public class GrouperRemedyUser {

  /**
   * cache connections
   */
  private static ExpirableCache<Boolean, Map<String, GrouperRemedyUser>> retrieveUsersCache = new ExpirableCache<Boolean, Map<String, GrouperRemedyUser>>(5);
  
  /**
   * 
   * @return box api connection never null
   */
  public synchronized static Map<String, GrouperRemedyUser> retrieveUsers() {
    
    Map<String, GrouperRemedyUser> usersMap = retrieveUsersCache.get(Boolean.TRUE);
    
    if (usersMap == null) {
      
      usersMap = GrouperRemedyCommands.retrieveRemedyUsers();
      
      retrieveUsersCache.put(Boolean.TRUE, usersMap);
    }
    
    return usersMap;
  }
  
  /**
   * remedy id for a person
   */
  private String personId;

  /**
   * netId of user
   */
  private String remedyLoginId;

  
  /**
   * remedy id for a person
   * @return the personId
   */
  public String getPersonId() {
    return this.personId;
  }

  
  /**
   * remedy id for a person
   * @param personId1 the personId to set
   */
  public void setPersonId(String personId1) {
    this.personId = personId1;
  }

  
  /**
   * netId of user
   * @return the remedyLoginId
   */
  public String getRemedyLoginId() {
    return this.remedyLoginId;
  }

  
  /**
   * netId of user
   * @param remedyLoginId1 the remedyLoginId to set
   */
  public void setRemedyLoginId(String remedyLoginId1) {
    this.remedyLoginId = remedyLoginId1;
  }
  
  
}
