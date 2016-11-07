package edu.internet2.middleware.grouperBox;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxGroup;
import com.box.sdk.BoxGroupMembership;
import com.box.sdk.BoxGroupMembership.Role;
import com.box.sdk.BoxUser;
import com.box.sdk.BoxUser.Status;
import com.box.sdk.DeveloperEditionEntityType;
import com.box.sdk.EncryptionAlgorithm;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;
import com.box.sdk.JWTEncryptionPreferences;

import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString.Morph;

/**
 * commands against the box api
 */
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
    Map<String, GrouperBoxUser> allUsersMap = retrieveBoxUsers();
    for (String loginid : allUsersMap.keySet()) {
      System.out.println(loginid);
      GrouperBoxUser theGrouperBoxUser = allUsersMap.get(loginid);
      System.out.println(theGrouperBoxUser.getBoxUserInfo().getID());
    }

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
//
//    createBoxGroup("testGroup3", false);
//    createBoxGroup("testGroup3", false);
//
//    Map<String, GrouperBoxGroup> allGroupsMap = retrieveBoxGroups();
//    GrouperBoxGroup grouperBoxGroup = allGroupsMap.get("testGroup3");
//
//    deleteBoxGroup(grouperBoxGroup, false);
//    deleteBoxGroup(grouperBoxGroup, false);

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
  
  /**
   * @return the name of group mapped to group
   */
  public static Map<String, GrouperBoxGroup> retrieveBoxGroups() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveBoxGroups");

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
   * deprovision or undeprovision user
   * @param grouperBoxUser
   * @param debugMap
   */
  public static void deprovisionOrUndeprovision(GrouperBoxUser grouperBoxUser, Map<String, Object> debugMap) {

    String whitelistBoxIds = GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.whitelistBoxIds");
    if (!GrouperClientUtils.isBlank(whitelistBoxIds)) {
      Set<String> whitelistBoxIdSet = GrouperClientUtils.splitTrimToSet(whitelistBoxIds, ",");
      //ignore whitelist users
      if (whitelistBoxIdSet.contains(grouperBoxUser.getBoxUserInfo().getLogin())) {
        return;
      }
    }
    
    Map<String, String[]> usersAllowedToBeInBox = GrouperWsCommandsForBox.retrieveGrouperUsers();

    if (usersAllowedToBeInBox == null) {
      return;
    }
    
    String boxUsername = grouperBoxUser.getBoxUserInfo().getLogin();
    boolean allowedToBeInBox = usersAllowedToBeInBox.containsKey(boxUsername);

    boolean updateUser = false;
    
    if (allowedToBeInBox) {

      String newStatusUndeprovisionString = GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.statusUndeprovisionedUsers");
      Status newStatusUndeprovision = GrouperClientUtils.isBlank(newStatusUndeprovisionString) 
          ? null : Status.valueOf(newStatusUndeprovisionString.toUpperCase());

      //
      //  # if a user is in the grouperBox.requireGroup group, then set the user's status to active
      //  # if this is blank then dont worry about it
      //  grouperBox.statusUndeprovisionedUsers = active
      if (newStatusUndeprovision != null && newStatusUndeprovision != grouperBoxUser.getBoxUserInfo().getStatus()) {
        
        debugMap.put("changeUserStatus_" + boxUsername, newStatusUndeprovisionString);
        grouperBoxUser.getBoxUserInfo().setStatus(newStatusUndeprovision);
        updateUser = true;
      }
      
      
      //
      //  # if a user is in the grouperBox.requireGroup group, then set is_sync_enabled to true
      //  grouperBox.statusUndeprovisionEnableSync = true
      boolean undeprovisionEnableSync = GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperBox.undeprovisionEnableSync", false);

      if (undeprovisionEnableSync && !grouperBoxUser.getBoxUserInfo().getIsSyncEnabled()) {

        debugMap.put("changeUserEnableSync_" + boxUsername, true);
        grouperBoxUser.getBoxUserInfo().setIsSyncEnabled(true);
        updateUser = true;

      }

    } else {

      String newStatusDeprovisionString = GrouperClientConfig.retrieveConfig().propertyValueString("grouperBox.statusDeprovisionedUsers");
      Status newStatusDeprovision = GrouperClientUtils.isBlank(newStatusDeprovisionString) 
          ? null : Status.valueOf(newStatusDeprovisionString.toUpperCase());

      //  # if a user is not in the grouperBox.requireGroup group, then set the user's status to inactive, cannot_delete_edit, or cannot_delete_edit_upload
      //  # if this is blank then dont worry about it
      //  grouperBox.statusDeprovisionedUsers = inactive
      //
      if (newStatusDeprovision != null && newStatusDeprovision != grouperBoxUser.getBoxUserInfo().getStatus()) {
        
        debugMap.put("changeUserStatus_" + boxUsername, newStatusDeprovisionString);
        grouperBoxUser.getBoxUserInfo().setStatus(newStatusDeprovision);
        updateUser = true;
      }
      
      
      //  # if a user is not in the grouperBox.requireGroup group, then set is_sync_enabled to false
      //  grouperBox.deprovisionDisableSync = true
      //
      boolean deprovisionDisableSync = GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperBox.deprovisionDisableSync", false);

      if (deprovisionDisableSync && grouperBoxUser.getBoxUserInfo().getIsSyncEnabled()) {

        debugMap.put("changeUserEnableSync_" + boxUsername, false);
        grouperBoxUser.getBoxUserInfo().setIsSyncEnabled(false);
        updateUser = true;

      }
      
    }
    if (updateUser) {
      GrouperBoxCommands.updateBoxUser(grouperBoxUser, false);
    }

  }

  /**
   * @return box login id to user never null
   */
  public static Map<String, GrouperBoxUser> retrieveBoxUsers() {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveBoxUsers");

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
   * @param loginid
   * @return the user based on loginid
   */
  public static GrouperBoxUser retrieveBoxUser(String loginid) {
    
    return GrouperBoxUser.retrieveUsers().get(loginid);
    
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
   * @param grouperBoxGroup
   * @return the map from username to grouper user object
   */
  public static Collection<BoxGroupMembership.Info> retrieveMembershipsForBoxGroup(GrouperBoxGroup grouperBoxGroup) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveMembershipsForBoxGroup");
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
  public static BoxGroupMembership.Info assignUserToBoxGroup(GrouperBoxUser grouperBoxUser, GrouperBoxGroup grouperBoxGroup, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "assignUserToBoxGroup");
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
  public static BoxGroupMembership.Info removeUserFromBoxGroup(GrouperBoxUser grouperBoxUser, GrouperBoxGroup grouperBoxGroup, boolean isIncremental) {
    
    if (grouperBoxGroup == null) {
      return null;
    }
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "removeUserFromBoxGroup");
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
        return retrieveBoxGroups().get(groupName);
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
   * update box user
   * @param grouperBoxUser 
   * @param isIncremental incremental or full (for logging)
   */
  public static void updateBoxUser(GrouperBoxUser grouperBoxUser, boolean isIncremental) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "updateBoxUser");
    debugMap.put("login", grouperBoxUser.getBoxUserInfo().getLogin());
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
    
      grouperBoxUser.getBoxUser().updateInfo(grouperBoxUser.getBoxUserInfo());
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
    
  }

  /**
   * delete box group or just remove all members (if configured)
   * @param grouperBoxGroup
   * @param isIncremental incremental or full (for logging)
   * @return true if did anything
   */
  public static boolean deleteBoxGroup(GrouperBoxGroup grouperBoxGroup, boolean isIncremental) {

    if (grouperBoxGroup == null) {
      return false;
    }
    
    //# is grouper the true system of record, delete box groups which dont exist in grouper
    boolean deleteGroupsInBoxWhichArentInGrouper = GrouperClientConfig.retrieveConfig().propertyValueBoolean("grouperBox.deleteGroupsInBoxWhichArentInGrouper", true);

    if (!deleteGroupsInBoxWhichArentInGrouper && GrouperClientUtils.length(grouperBoxGroup.getMemberUsers()) == 0) {
      return false;
    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "deleteDuoGroup");
    debugMap.put("groupName", grouperBoxGroup.getBoxGroupInfo().getName());
    debugMap.put("daemonType", isIncremental ? "incremental" : "full");
    long startTime = System.nanoTime();
    try {
      if (deleteGroupsInBoxWhichArentInGrouper) {
  
        debugMap.put("deleteGroup", true);
        try {
        
          grouperBoxGroup.getBoxGroup().delete();
          
        } catch (BoxAPIException boxAPIException) {
          if (boxAPIException.getResponseCode() == 404) {
            debugMap.put("didntExist", true);
            return false;
          }
            
          debugMap.put("exception", GrouperClientUtils.getFullStackTrace(boxAPIException));
          throw boxAPIException;
        }
      } else {
        debugMap.put("removeMembershipsInsteadOfDeleteGroup", true);
        //remove all memberships
        
        Map<String, GrouperBoxUser> boxMemberUsernameToUser = grouperBoxGroup.getMemberUsers();

        for (GrouperBoxUser grouperBoxUser : GrouperClientUtils.nonNull(boxMemberUsernameToUser).values()) {
          removeUserFromBoxGroup(grouperBoxUser, grouperBoxGroup, isIncremental);
        }
      }
      
      return true;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperBoxLog.boxLog(debugMap, startTime);
    }
    
  }
}
