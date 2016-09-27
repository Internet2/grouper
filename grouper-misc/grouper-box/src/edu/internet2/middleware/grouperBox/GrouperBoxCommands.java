package edu.internet2.middleware.grouperBox;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxGroup;
import com.box.sdk.BoxGroupMembership;
import com.box.sdk.BoxGroupMembership.Role;
import com.box.sdk.BoxUser;
import com.box.sdk.DeveloperEditionEntityType;
import com.box.sdk.EncryptionAlgorithm;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;
import com.box.sdk.JWTEncryptionPreferences;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Morph;

public class GrouperBoxCommands {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

//    Map<String, GrouperBoxGroup> allGroupsMap = retrieveGroups();
//    for (String name : allGroupsMap.keySet()) {
//      System.out.println(name);
//      GrouperBoxGroup grouperBoxGroup = allGroupsMap.get(name);
//      for (BoxGroupMembership.Info boxGroupMembershipInfo : grouperBoxGroup.getMemberships()) {
//        BoxUser.Info boxUserInfo = boxGroupMembershipInfo.getUser();
//        System.out.println(boxUserInfo.getID());
//      }
//    }
//    
//    GrouperBoxUser grouperBoxUser = retrieveUser("323009820");
//    System.out.println(grouperBoxUser.getBoxUserInfo().getLogin());
//
//    Map<String, GrouperBoxUser> allUsersMap = retrieveUsers();
//    for (String loginid : allUsersMap.keySet()) {
//      System.out.println(loginid);
//      GrouperBoxUser theGrouperBoxUser = allUsersMap.get(loginid);
//      System.out.println(theGrouperBoxUser.getBoxUserInfo().getID());
//    }

//    Map<String, GrouperBoxGroup> allGroupsMap = retrieveGroups();
//    Map<String, GrouperBoxUser> allUsersMap = retrieveUsers();
//    //testGroup, testGroup2, mchyzer@gmail.com, mchyzer@yahoo.com    
//    GrouperBoxGroup grouperBoxGroup = allGroupsMap.get("testGroup");
//    GrouperBoxUser grouperBoxUser = allUsersMap.get("mchyzer@gmail.com");
//    
//    grouperBoxGroup.assignUserToGroup(grouperBoxUser, false);
//    grouperBoxGroup.assignUserToGroup(grouperBoxUser, false);
//    
//    grouperBoxGroup.removeUserFromGroup(grouperBoxUser, false);
//    grouperBoxGroup.removeUserFromGroup(grouperBoxUser, false);

    createBoxGroup("testGroup3", false);
    createBoxGroup("testGroup3", false);

    Map<String, GrouperBoxGroup> allGroupsMap = retrieveGroups();
    GrouperBoxGroup grouperBoxGroup = allGroupsMap.get("testGroup3");

    deleteBoxGroup(grouperBoxGroup, false);
    deleteBoxGroup(grouperBoxGroup, false);

  }

  /**
   * cache connections
   */
  private static ExpirableCache<Boolean, BoxAPIConnection> boxApiConnectionCache = new ExpirableCache<Boolean, BoxAPIConnection>(5);
  
  /**
   * 
   * @return box api connection
   */
  public synchronized static BoxAPIConnection retrieveBoxApiConnection() {
    
    BoxAPIConnection boxAPIConnection = boxApiConnectionCache.get(Boolean.TRUE);
    
    if (boxAPIConnection == null) {
    
      JWTEncryptionPreferences jwtEncryptionPreferences = new JWTEncryptionPreferences();
      
      String privateKey = GrouperClientUtils.readFileIntoString(new File(GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.privateKeyFileName")));
      
      jwtEncryptionPreferences.setPrivateKey(privateKey);
      
      String privateKeyPass = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.privateKeyPass");
      privateKeyPass = Morph.decryptIfFile(privateKeyPass);
      
      jwtEncryptionPreferences.setPrivateKeyPassword(privateKeyPass);
      jwtEncryptionPreferences.setEncryptionAlgorithm(EncryptionAlgorithm.RSA_SHA_512);
      jwtEncryptionPreferences.setPublicKeyID(GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.publicKeyId"));
      
      IAccessTokenCache iAccessTokenCache = new InMemoryLRUAccessTokenCache(10000);
      
      String clientSecret = GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.clientSecret");
      clientSecret = Morph.decryptIfFile(clientSecret);
      
      boxAPIConnection = new BoxDeveloperEditionAPIConnection(
          GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.enterpriseId"),
          DeveloperEditionEntityType.ENTERPRISE, 
          GrouperClientConfig.retrieveConfig().propertyValueStringRequired("grouperBox.clientId"), 
          clientSecret, jwtEncryptionPreferences,
          iAccessTokenCache);
      
      boxApiConnectionCache.put(Boolean.TRUE, boxAPIConnection);
    }        
    
    return boxAPIConnection;
  }
  
//  /**
//   * @param userId
//   * @param groupId
//   * @param isIncremental
//   * @return the json object
//   */
//  public static JSONObject assignUserToGroup(String userId, String groupId, boolean isIncremental) {
//    
//    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
//
//    debugMap.put("method", "assignUserToGroup");
//    debugMap.put("userId", userId);
//    debugMap.put("groupId", groupId);
//    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
//    long startTime = System.nanoTime();
//    try {
//      
//      if (GrouperClientUtils.isBlank(userId)) {
//        throw new RuntimeException("Why is userId blank?");
//      }
//      
//      if (GrouperClientUtils.isBlank(groupId)) {
//        throw new RuntimeException("Why is groupId blank?");
//      }
//      
//      //assign group to user
//      BoxAPIConnection boxAPIConnection = retrieveBoxApiConnection();
//
//      //lets see if user exists
//      
//      
//      // POST /admin/v1/users/[user_id]/groups
//      String path = "/admin/v1/users/" + userId + "/groups";
//      debugMap.put("POST", path);
//      Http request = httpAdmin("POST", path);
//      request.addParam("group_id", groupId);
//
//      signHttpAdmin(request);
//
//      String result = executeRequestRaw(request);
//
//      JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( result );     
//
//      if (!StringUtils.equals(jsonObject.getString("stat"), "OK")) {
//
//        //  {
//        //    "stat": "OK",
//        //    "response": ""
//        //  }
//        
//        debugMap.put("error", true);
//        debugMap.put("result", result);
//        throw new RuntimeException("Bad response from Duo: " + result);
//      }
//
//      return jsonObject;
//    } catch (RuntimeException re) {
//      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
//      throw re;
//    } finally {
//      GrouperBoxLog.boxLog(debugMap, startTime);
//    }
//
//  }

  /**
   * @return the name of group mapped to group
   */
  public static Map<String, GrouperBoxGroup> retrieveGroups() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveGroups");

    long startTime = System.nanoTime();
    
    try {

      BoxAPIConnection boxAPIConnection = retrieveBoxApiConnection();
      
      Iterable<BoxGroup.Info> groups = BoxGroup.getAllGroups(boxAPIConnection);

      Map<String, GrouperBoxGroup> results = new LinkedHashMap<String, GrouperBoxGroup>();
          
      for (BoxGroup.Info boxGroupInfo : groups) {
        
        results.put(boxGroupInfo.getName(), new GrouperBoxGroup(boxGroupInfo.getResource(), boxGroupInfo));
      }

      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }

  /**
   * @return login id to user
   */
  public static Map<String, GrouperBoxUser> retrieveUsers() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveUsers");

    long startTime = System.nanoTime();
    
    try {

      BoxAPIConnection boxAPIConnection = retrieveBoxApiConnection();
      
      Iterable<BoxUser.Info> users = BoxUser.getAllEnterpriseUsers(boxAPIConnection);

      Map<String, GrouperBoxUser> results = new LinkedHashMap<String, GrouperBoxUser>();
          
      for (BoxUser.Info boxUserInfo : users) {
        
        results.put(boxUserInfo.getLogin(), new GrouperBoxUser(boxUserInfo.getResource(), boxUserInfo));
      }

      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }

  /**
   * @return the user based on userId
   */
  public static GrouperBoxUser retrieveUser(String userId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveUser");

    long startTime = System.nanoTime();

    try {
  
      BoxAPIConnection boxAPIConnection = retrieveBoxApiConnection();

      BoxUser boxUser = new BoxUser(boxAPIConnection, userId);

      GrouperBoxUser grouperBoxUser = new GrouperBoxUser(boxUser);
      
      return grouperBoxUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
  
  }

  /**
   * get box group info
   * @param boxGroup
   * @return info
   */
  public static BoxGroup.Info retrieveBoxGroupInfo(BoxGroup boxGroup) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveBoxGroupInfo");

    long startTime = System.nanoTime();

    try {
  
      BoxGroup.Info result = boxGroup.getInfo();

      debugMap.put("group", result.getName());

      return result;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
  }
  
  /**
   * @param response
   * @return the map from username to grouper user object
   */
  public static Collection<BoxGroupMembership.Info> retrieveMembershipsForGroup(GrouperBoxGroup grouperBoxGroup) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "groupGetMemberships");
    debugMap.put("group", grouperBoxGroup.getBoxGroupInfo().getName());

    long startTime = System.nanoTime();

    try {
  
      Collection<BoxGroupMembership.Info> results = grouperBoxGroup.getBoxGroup().getMemberships();

      debugMap.put("count", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }

  }

  /**
   * @param grouperBoxUser
   * @param grouperBoxGroup
   * @param isIncremental
   * @return the json object
   */
  public static BoxGroupMembership.Info assignUserToGroup(GrouperBoxUser grouperBoxUser, GrouperBoxGroup grouperBoxGroup, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "assignUserToGroup");
    debugMap.put("userLoginId", grouperBoxUser.getBoxUserInfo().getLogin());
    debugMap.put("groupName", grouperBoxGroup.getBoxGroupInfo().getName());
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
      return grouperBoxGroup.getBoxGroup().addMembership(grouperBoxUser.getBoxUser());
    } catch (BoxAPIException boxAPIException) {
      //already exists
      if (boxAPIException.getResponseCode() == 409) {
        debugMap.put("alreadyExisted", true);
        return null;
      }
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(boxAPIException));
      throw boxAPIException;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
  
  }

  /**
   * @param grouperBoxUser
   * @param grouperBoxGroup
   * @param isIncremental
   * @return the json object
   */
  public static BoxGroupMembership.Info removeUserFromGroup(GrouperBoxUser grouperBoxUser, GrouperBoxGroup grouperBoxGroup, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "removeUserFromGroup");
    debugMap.put("userLoginId", grouperBoxUser.getBoxUserInfo().getLogin());
    debugMap.put("groupName", grouperBoxGroup.getBoxGroupInfo().getName());
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
      for (BoxGroupMembership.Info boxGroupMembershipInfo : grouperBoxGroup.getMemberships()) {
        
        if (boxGroupMembershipInfo.getRole() == Role.MEMBER && GrouperClientUtils.equals(grouperBoxUser.getBoxUserInfo().getLogin(), boxGroupMembershipInfo.getUser().getLogin())) {
          boxGroupMembershipInfo.getResource().delete();
          debugMap.put("foundMembership", true);
          return boxGroupMembershipInfo;
        }
                
      }
      
      debugMap.put("foundMembership", false);
      return null;
    } catch (BoxAPIException boxAPIException) {
      //didnt exist
      if (boxAPIException.getResponseCode() == 404) {
        debugMap.put("didntExist", true);
        return null;
      }
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(boxAPIException));
      throw boxAPIException;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
  
  }

  /**
   * create box group
   * @param groupName 
   * @param isIncremental incremental or full (for logging)
   * @return the json object
   */
  public static GrouperBoxGroup createBoxGroup(String groupName, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "createBoxGroup");
    debugMap.put("name", groupName);
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
    
      BoxAPIConnection boxAPIConnection = retrieveBoxApiConnection();
      BoxGroup.Info boxGroupInfo = BoxGroup.createGroup(boxAPIConnection, groupName);
      GrouperBoxGroup grouperBoxGroup = new GrouperBoxGroup(boxGroupInfo.getResource(), boxGroupInfo);
      
      return grouperBoxGroup;
    } catch (BoxAPIException boxAPIException) {
      if (boxAPIException.getResponseCode() == 409) {
        debugMap.put("alreadyExisted", true);
        //just get all again i guess
        return retrieveGroups().get(groupName);
      }
        
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(boxAPIException));
      throw boxAPIException;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
    
  }

  /**
   * delete box group
   * @param groupId
   * @param isIncremental incremental or full (for logging)
   * @return the json object
   */
  public static void deleteBoxGroup(GrouperBoxGroup grouperBoxGroup, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "deleteDuoGroup");
    debugMap.put("groupName", grouperBoxGroup.getBoxGroupInfo().getName());
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
    
      grouperBoxGroup.getBoxGroup().delete();
      
    } catch (BoxAPIException boxAPIException) {
      if (boxAPIException.getResponseCode() == 404) {
        debugMap.put("didntExist", true);
        return;
      }
        
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(boxAPIException));
      throw boxAPIException;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
    
  }
}
