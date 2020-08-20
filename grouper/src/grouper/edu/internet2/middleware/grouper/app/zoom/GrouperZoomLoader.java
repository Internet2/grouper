/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.zoom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/**
 *
 */
public class GrouperZoomLoader extends OtherJobBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperZoomLoader.class);

  /**
   * 
   */
  public GrouperZoomLoader() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (GrouperUtil.length(args) != 11) {
      throw new RuntimeException("Pass in the configId, true/false (groupSync?), groupSyncFolderName, true/false (roleSync?), roleSyncFolderName, "
          + "true/false (userTypeSync?), userTypeFolderName, true/false (userStatusSync?), userStatusFolderName to full load, "
          + "true/false (subAccountSync?), subAccountFolderName");
    }
    GrouperStartup.startup();
    String configId = args[0];
    boolean groupSync = GrouperUtil.booleanValue(args[1], false);
    String groupSyncFolder = args[2];
    boolean roleSync = GrouperUtil.booleanValue(args[3], false);
    String roleSyncFolder = args[4];
    boolean userTypeSync = GrouperUtil.booleanValue(args[5], false);
    String userTypeSyncFolder = args[6];
    boolean userStatusSync = GrouperUtil.booleanValue(args[7], false);
    String userStatusSyncFolder = args[8];
    boolean subAccountSync = GrouperUtil.booleanValue(args[9], false);
    String subAccountSyncFolder = args[10];
    
    fullLoad(configId, groupSync, groupSyncFolder, roleSync, roleSyncFolder, userTypeSync, userTypeSyncFolder, userStatusSync, userStatusSyncFolder,
        subAccountSync, subAccountSyncFolder);

  }

  /**
   * @see edu.internet2.middleware.grouper.app.loader.OtherJobBase#run(edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput)
   */
  @Override
  public OtherJobOutput run(OtherJobInput otherJobInput) {
    String jobName = otherJobInput.getJobName();
    
    // jobName = OTHER_JOB_csvSync
    jobName = GrouperClientUtils.stripPrefix(jobName, "OTHER_JOB_");

    Hib3GrouperLoaderLog hib3GrouperLoaderLog = otherJobInput.getHib3GrouperLoaderLog();
    if (hib3GrouperLoaderLog == null) {
      hib3GrouperLoaderLog = new Hib3GrouperLoaderLog();
      otherJobInput.setHib3GrouperLoaderLog(hib3GrouperLoaderLog);
    }

    String configId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomConfigId");

    boolean loadGroups = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".zoomLoadGroups", false);
    String groupLoadFolderName = loadGroups ?  GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomLoadGroupsFolderName") : null;

    boolean loadRoles = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".zoomLoadRoles", false);
    String roleLoadFolderName = loadRoles ?  GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomLoadRolesFolderName") : null;

    boolean loadUserTypes = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".zoomLoadUserTypes", false);
    String userTypeLoadFolderName = loadUserTypes ?  GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomLoadUserTypesFolderName") : null;

    boolean loadUserStatuses = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".zoomLoadUserStatuses", false);
    String userStatusLoadFolderName = loadUserStatuses ?  GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomLoadUserStatusesFolderName") : null;
    
    boolean loadSubAccounts = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".zoomLoadSubAccounts", false);
    String subAccountsLoadFolderName = loadUserStatuses ?  GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomLoadSubAccountsFolderName") : null;
    
    Map<String, Object> resultMap = fullLoad(configId, loadGroups, groupLoadFolderName, loadRoles, roleLoadFolderName, 
        loadUserTypes, userTypeLoadFolderName, loadUserStatuses, userStatusLoadFolderName, loadSubAccounts, subAccountsLoadFolderName);
    
    int groupAddCount = resultMap.containsKey("groupAddCount") ? (Integer)resultMap.get("groupAddCount") : 0;
    int groupCount = resultMap.containsKey("groupCount") ? (Integer)resultMap.get("groupCount") : 0;
    int membershipAddCount = resultMap.containsKey("membershipAddCount") ? (Integer)resultMap.get("membershipAddCount") : 0;
    int membershipDeleteCount = resultMap.containsKey("membershipDeleteCount") ? (Integer)resultMap.get("membershipDeleteCount") : 0;
    int membershipTotalCount = resultMap.containsKey("membershipTotalCount") ? (Integer)resultMap.get("membershipTotalCount") : 0;

    hib3GrouperLoaderLog.addInsertCount(groupAddCount);
    hib3GrouperLoaderLog.addInsertCount(membershipAddCount);

    hib3GrouperLoaderLog.addDeleteCount(membershipDeleteCount);

    hib3GrouperLoaderLog.addTotalCount(groupCount);
    hib3GrouperLoaderLog.addTotalCount(membershipTotalCount);
    
    hib3GrouperLoaderLog.setJobMessage(GrouperUtil.toStringForLog(resultMap));

    return null;
  }

  /**
   * 
   * @param name
   * @return normalized string
   */
  public static String validGrouperName(String name) {

    StringBuilder result = new StringBuilder();

    if ((name == null) || (0 == "".compareTo(name))) {
      return name;
    }
    char currChar;
    //loop through the string, looking for uppercase
    for (int i = 0; i < name.length(); i++) {
      currChar = name.charAt(i);

      if (('0' <= currChar && currChar <= '9')
        || ('a' <= currChar && currChar <= 'z')
        || ('A' <= currChar && currChar <= 'Z')
        || currChar == '_') {
        result.append(currChar);
      } else {
        result.append('_');
      }
    }

    return result.toString();

  }
  
  /**
   * @param targetNames
   * @return the lookups, [0] is Map<String, String> of targetName to grouperName.  [1] is Map<String, String> of grouperName to targetName
   */
  public static Map<String, String>[] convertTargetNamesToGrouperNames(Collection<String> targetNames) {
    Map<String, String>[] result = new Map[2];
    result[0] = new HashMap<String, String>();
    result[1] = new HashMap<String, String>();
    for (String targetName : GrouperUtil.nonNull(targetNames)) {
      
      String grouperName = validGrouperName(targetName);
      
      if (result[1].containsKey(grouperName)) {
        throw new RuntimeException("Name conflicts: '" + targetName + "', '" + grouperName + "', '" + result[1].get(grouperName));
      }
      
      result[0].put(targetName, grouperName);
      result[1].put(grouperName, targetName);
    }
    return result;
  }
  
  /**
   * 
   * @param configId
   * @param userTypeSyncFolder 
   * @param userTypeLoad 
   * @param roleSyncFolder 
   * @param roleLoad 
   * @param groupSyncFolder 
   * @param groupLoad 
   * @param userStatusSyncFolder 
   * @param userStatusLoad 
   * @param subAccountLoad
   * @param subAccountSyncFolder
   * @return map with groupCount, groupAddCount, membershipAddCount, membershipDeleteCount, membershipTotalCount
   */
  public static Map<String, Object> fullLoad(final String configId, final boolean groupLoad, final String groupSyncFolder, 
        final boolean roleLoad, final String roleSyncFolder, final boolean userTypeLoad, final String userTypeSyncFolder, 
        final boolean userStatusLoad, final String userStatusSyncFolder, final boolean subAccountLoad, final String subAccountSyncFolder) {
    long startedNanos = System.nanoTime();
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "fullLoad");
    try {

      debugMap.put("groupTotalCount", 0);
      debugMap.put("groupTotalAddCount", 0);
      debugMap.put("groupTotalDeleteCount", 0);
      debugMap.put("membershipTotalAddCount", 0);
      debugMap.put("membershipTotalDeleteCount", 0);
      debugMap.put("membershipTotalCount", 0);
      debugMap.put("totalSubjectNotFound", 0);
      
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
        
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {

          Map<String, Map<String, Object>> groupsInZoom = null;
          Map<String, String> groupZoomNameToGrouperExtension = null;
          Map<String, List<Map<String, Object>>> groupZoomNameToMemberships = null;

          Map<String, Map<String, Object>> rolesInZoom = null;
          Map<String, String> roleZoomNameToGrouperExtension = null;
          Map<String, List<Map<String, Object>>> roleZoomNameToMemberships = null;

          Map<String, List<Map<String, Object>>> userTypeToMemberships = null;
          Map<String, String> userTypeZoomNameToGrouperExtension = null;

          Map<String, List<Map<String, Object>>> userStatusToMemberships = null;
          Map<String, String> userStatusZoomNameToGrouperExtension = null;

          Map<String, Map<String, Object>> subAccountsInZoom = null;
          Map<String, String> subAccountZoomNameToGrouperExtension = null;
          Map<String, List<Map<String, Object>>> subAccountZoomNameToMemberships = null;

          Map<String, Map<String, Object>> emailToZoomUser = new HashMap<String, Map<String, Object>>();
          
          if (groupLoad) {
            
            if (StringUtils.isBlank(groupSyncFolder)) {
              throw new RuntimeException("Group load folder cannot be null!");
            }
            
            //Stem groupLoadStem = new StemSave(grouperSession).assignName(groupSyncFolder).save();
            
            // groups that exist in zoom
            groupsInZoom = GrouperZoomCommands.retrieveGroups(configId);
            
            Map[] groupMaps = convertTargetNamesToGrouperNames(groupsInZoom.keySet());

            groupZoomNameToGrouperExtension = groupMaps[0];
            
            groupZoomNameToMemberships = new HashMap<String, List<Map<String, Object>>>();
            
            for (String zoomName : groupsInZoom.keySet()) {
              
              Map<String, Object> groupInZoom = groupsInZoom.get(zoomName);
              
              String groupIdInZoom = (String)groupInZoom.get("id");
              
              List<Map<String, Object>> zoomUsers = GrouperZoomCommands.retrieveGroupMemberships(configId, groupIdInZoom);
              
              for (Map<String, Object> user : GrouperUtil.nonNull(zoomUsers)) {
                String email = (String)user.get("email");
                if (!StringUtils.isBlank(email)) {
                  emailToZoomUser.put(email, user);
                }
              }
              groupZoomNameToMemberships.put(zoomName, zoomUsers);
            }
          }
          
          if (roleLoad) {
            
            if (StringUtils.isBlank(roleSyncFolder)) {
              throw new RuntimeException("Role load folder cannot be null!");
            }
            
            // roles that exist in zoom
            rolesInZoom = GrouperZoomCommands.retrieveRoles(configId);
            
            Map[] roleMaps = convertTargetNamesToGrouperNames(rolesInZoom.keySet());

            roleZoomNameToGrouperExtension = roleMaps[0];
            
            roleZoomNameToMemberships = new HashMap<String, List<Map<String, Object>>>();
            
            for (String zoomName : rolesInZoom.keySet()) {
              
              Map<String, Object> roleInZoom = rolesInZoom.get(zoomName);
              
              String roleIdInZoom = (String)roleInZoom.get("id");
              
              List<Map<String, Object>> zoomUsers = GrouperZoomCommands.retrieveRoleMemberships(configId, roleIdInZoom);
              
              for (Map<String, Object> user : GrouperUtil.nonNull(zoomUsers)) {
                String email = (String)user.get("email");
                if (!StringUtils.isBlank(email)) {
                  emailToZoomUser.put(email, user);
                }
              }
              roleZoomNameToMemberships.put(zoomName, zoomUsers);
            }
          }

          if (subAccountLoad) {
            
            if (StringUtils.isBlank(subAccountSyncFolder)) {
              throw new RuntimeException("Subaccount folder cannot be null!");
            }
            
            // subaccounts that exist in zoom
            subAccountsInZoom = GrouperZoomCommands.retrieveAccounts(configId);
            
            Map<String, String> accountIdToName = new HashMap<String, String>();

            for (Map<String, Object> account : GrouperUtil.nonNull(subAccountsInZoom).values()) {
              String accountId = (String)account.get("id");
              String accountName = (String)account.get("account_name");
              accountIdToName.put(accountId, accountName);
            }
            
            Map[] subAccountMaps = convertTargetNamesToGrouperNames(GrouperUtil.nonNull(accountIdToName).values());

            subAccountZoomNameToGrouperExtension = subAccountMaps[0];
            
            subAccountZoomNameToMemberships = new HashMap<String, List<Map<String, Object>>>();
            
            for (String subAccountId : subAccountsInZoom.keySet()) {
              
              String subAccountName = accountIdToName.get(subAccountId);
              if (subAccountName == null) {
                subAccountName = subAccountId;
              }
              //massage
              //String grouperExtension = subAccountZoomNameToGrouperExtension.get(subAccountName);

              //Map<String, Object> subAccountInZoom = subAccountsInZoom.get(subAccountId);
              
              Map<String, Map<String, Object>> zoomUsers = GrouperZoomCommands.retrieveSubaccountUsers(configId, subAccountId);
              
              List<Map<String, Object>> users = new ArrayList<Map<String, Object>>();
              
              for (Map<String, Object> user : GrouperUtil.nonNull(zoomUsers).values()) {
                String email = (String)user.get("email");
                if (!StringUtils.isBlank(email)) {
                  emailToZoomUser.put(email, user);
                  users.add(user);
                }
              }
              subAccountZoomNameToMemberships.put(subAccountName, users);
            }
          }

          if (userTypeLoad || userStatusLoad) {
            
            if (userTypeLoad && StringUtils.isBlank(userTypeSyncFolder)) {
              throw new RuntimeException("User type load folder cannot be null!");
            }
            if (userStatusLoad && StringUtils.isBlank(userStatusSyncFolder)) {
              throw new RuntimeException("User status load folder cannot be null!");
            }
            if (userTypeLoad) {
              userTypeZoomNameToGrouperExtension = new HashMap<String, String>();
              userTypeToMemberships = new HashMap<String, List<Map<String, Object>>>();
            }
            if (userStatusLoad) {
              userStatusZoomNameToGrouperExtension = new HashMap<String, String>();
              userStatusToMemberships = new HashMap<String, List<Map<String, Object>>>();
            }
            
            // userTypes that exist in zoom
            Map<String, Map<String, Object>> usersInZoom = GrouperZoomCommands.retrieveUsers(configId);
            
            for (Map<String, Object> user : usersInZoom.values()) {

              if (userTypeLoad) {
                Integer typeInteger = (Integer)user.get("type");
                
                if (typeInteger != null) {
                  String typeString = Integer.toString(typeInteger);

                  List<Map<String, Object>> usersForUserType = userTypeToMemberships.get(typeString);
                  
                  if (usersForUserType == null) {
                    
                    usersForUserType = new ArrayList<Map<String, Object>>();
                    userTypeToMemberships.put(typeString, usersForUserType);
                    
                  }
                  usersForUserType.add(user);
                }
              }
              
              if (userStatusLoad) {
                String status = (String)user.get("status");
                
                if (status != null) {

                  List<Map<String, Object>> usersForStatus = userStatusToMemberships.get(status);
                  
                  if (usersForStatus == null) {
                    
                    usersForStatus = new ArrayList<Map<String, Object>>();
                    userStatusToMemberships.put(status, usersForStatus);
                    
                  }
                  usersForStatus.add(user);
                }
              }

              String email = (String)user.get("email");
              if (!StringUtils.isBlank(email)) {
                emailToZoomUser.put(email, user);
              }
              
            }
            if (userTypeLoad) {
              Set<String> userTypes = new HashSet<String>(userTypeToMemberships.keySet());
              userTypes.add("1");
              userTypes.add("2");
              userTypes.add("3");
              
              for (String userType : userTypes) {
                final String grouperExtension = "zoomUserType_" + userType;
                userTypeZoomNameToGrouperExtension.put(userType, grouperExtension);
              }
              
            }
            if (userStatusLoad) {
              Set<String> statuses = new HashSet<String>(userStatusToMemberships.keySet());
              statuses.add("pending");
              statuses.add("active");
              statuses.add("inactive");

              for (String status : statuses) {
                final String grouperExtension = "zoomUserStatus_" + status;
                userStatusZoomNameToGrouperExtension.put(status, grouperExtension);
              }

            }

          }

          GrouperZoomLocalCommands.convertEmailToSourceIdSubjectId(configId, emailToZoomUser.keySet());
          

          if (groupLoad) {
            
            loadGroupsAndMembershipsToGrouper(configId, groupSyncFolder, debugMap,
                groupZoomNameToGrouperExtension, groupZoomNameToMemberships, "groups");            
          }
          
          if (roleLoad) {
            
            loadGroupsAndMembershipsToGrouper(configId, roleSyncFolder, debugMap,
                roleZoomNameToGrouperExtension, roleZoomNameToMemberships, "roles");            
          }
          
          if (subAccountLoad) {
            
            loadGroupsAndMembershipsToGrouper(configId, subAccountSyncFolder, debugMap,
                subAccountZoomNameToGrouperExtension, subAccountZoomNameToMemberships, "subaccounts");            
          }

          if (userTypeLoad ) {
            
            loadGroupsAndMembershipsToGrouper(configId, userTypeSyncFolder, debugMap,
                userTypeZoomNameToGrouperExtension, userTypeToMemberships, "userTypes");            
          }

          if (userStatusLoad ) {
            
            loadGroupsAndMembershipsToGrouper(configId, userStatusSyncFolder, debugMap,
                userStatusZoomNameToGrouperExtension, userStatusToMemberships, "userStatuses");            
          }

          
          return null;
        }

      });

    } catch (RuntimeException e) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(e));
      throw e;
    } finally {
      debugMap.put("tookMillis", (System.nanoTime() - startedNanos)/1000000);
      if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    return debugMap;
  }

  /**
   * @param configId
   * @param groupSyncFolder
   * @param debugMap
   * @param groupZoomNameToGrouperExtension
   * @param groupZoomNameToMemberships
   * @param debugPrefix 
   */
  public static void loadGroupsAndMembershipsToGrouper(final String configId,
      final String groupSyncFolder, final Map<String, Object> debugMap,
      Map<String, String> groupZoomNameToGrouperExtension,
      Map<String, List<Map<String, Object>>> groupZoomNameToMemberships, String debugPrefix) {
    Set<String> groupExtensionsInGrouper = GrouperUtil.nonNull(GrouperZoomLocalCommands.groupExtensionsInFolder(groupSyncFolder));
    
    //groupsInZoom
    //groupZoomNameToGrouperExtension
    //groupGrouperExtensionToZoomName
    //groupZoomNameToMemberships

    debugIncrement(debugMap, "groupTotalCount", groupZoomNameToGrouperExtension.size());

    Set<String> groupsInGrouperToDelete = new HashSet<String>(groupExtensionsInGrouper);
    groupsInGrouperToDelete.removeAll(groupZoomNameToGrouperExtension.values());
    
    debugIncrement(debugMap, "groupTotalDeleteCount", groupsInGrouperToDelete.size());
    debugMap.put(debugPrefix+"DeleteCount", groupsInGrouperToDelete.size());
    GrouperZoomLocalCommands.deleteGroupExtensionsInFolder(groupSyncFolder, groupsInGrouperToDelete);
    
    Set<String> groupsInGrouperToAdd = new HashSet<String>(groupZoomNameToGrouperExtension.values());
    groupsInGrouperToAdd.removeAll(groupExtensionsInGrouper);

    debugMap.put(debugPrefix+"AddCount",  groupsInGrouperToAdd.size());
    debugIncrement(debugMap, "groupTotalAddCount", groupsInGrouperToAdd.size());

    GrouperZoomLocalCommands.createGroupExtensionsInFolder(groupSyncFolder, groupsInGrouperToAdd);
    
    Map<String, Set<MultiKey>> groupsSourceIdsSubjectIdsInGrouper = GrouperZoomLocalCommands.groupsSourceIdsSubjectIdsToProvisionByFolderName(configId, groupSyncFolder);

    for (String zoomGroupName : groupZoomNameToGrouperExtension.keySet()) {
      
      String grouperGroupExtension = groupZoomNameToGrouperExtension.get(zoomGroupName);
      
      List<Map<String, Object>> usersInZoomGroup = groupZoomNameToMemberships.get(zoomGroupName);
      
      List<String> emailsInZoomGroup = new ArrayList<String>();
      for (Map<String, Object> userInZoomGroup : GrouperUtil.nonNull(usersInZoomGroup)) {
        String email = (String)userInZoomGroup.get("email");
        if (email != null) {
          emailsInZoomGroup.add(email);
        }
      }

      Map<String, MultiKey> emailToSourceIdSubjectIdInZoom = GrouperZoomLocalCommands.convertEmailToSourceIdSubjectId(configId, emailsInZoomGroup);
      
      // see which emails are not found when resolving, log these optionally
      for (String email : GrouperUtil.nonNull(emailsInZoomGroup)) {
        if (!emailToSourceIdSubjectIdInZoom.containsKey(email)) {
          debugIncrement(debugMap, "totalSubjectNotFound", 1);
          debugMap.put(debugPrefix + "SubjectNotFound", 1);
          if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("zoom." + configId + ".logUnresolvables", false)) {
            debugMap.put(email + "_notFound", true);
          }
        }
      }
      
      debugIncrement(debugMap, "membershipTotalCount", GrouperUtil.length(emailToSourceIdSubjectIdInZoom));
      debugIncrement(debugMap, debugPrefix + "MembershipCount", GrouperUtil.length(emailToSourceIdSubjectIdInZoom));
      
      loadMembershipsToGrouper(debugMap, configId, groupSyncFolder, grouperGroupExtension, emailToSourceIdSubjectIdInZoom.values(), 
          groupsSourceIdsSubjectIdsInGrouper.get(grouperGroupExtension), debugPrefix);
      
    }
  }

  

  /**
   * @param debugMap
   * @param configId
   * @param groupSyncFolder
   * @param grouperGroupExtension
   * @param sourceIdSubjectIdInZoomCollection 
   * @param sourceIdsSubjectIdsInGrouper 
   * @param debugPrefix 
   */
  protected static void loadMembershipsToGrouper(Map<String, Object> debugMap, String configId,
      String groupSyncFolder, String grouperGroupExtension,
      Collection<MultiKey> sourceIdSubjectIdInZoomCollection, Set<MultiKey> sourceIdsSubjectIdsInGrouper,
      String debugPrefix) {

    sourceIdsSubjectIdsInGrouper = GrouperUtil.nonNull(sourceIdsSubjectIdsInGrouper);
    Set<MultiKey> sourceIdSubjectIdInZoomSet = new HashSet<MultiKey>(GrouperUtil.nonNull(sourceIdSubjectIdInZoomCollection));
    
    Set<MultiKey> sourceIdSubjectIdsToAddToGrouper = new HashSet<MultiKey>(sourceIdSubjectIdInZoomSet);
    sourceIdSubjectIdsToAddToGrouper.removeAll(sourceIdsSubjectIdsInGrouper);
    
    Group group = null;
    
    if (GrouperUtil.length(sourceIdSubjectIdsToAddToGrouper) > 0) {
      
      if (group == null) {
        group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupSyncFolder + ":" + grouperGroupExtension, true);
      }
      
      for (MultiKey sourceIdSubjectId : sourceIdSubjectIdsToAddToGrouper) {
        try {
          Subject subject = SubjectFinder.findByIdAndSource((String)sourceIdSubjectId.getKey(1), (String)sourceIdSubjectId.getKey(0), true);
          if (group.addMember(subject, false)) {
            debugMap.put(debugPrefix + "MembershipAdds", 1);
            debugIncrement(debugMap, "membershipTotalAddCount", 1);
          }
        } catch (SubjectNotFoundException subjectNotFoundException) {
          debugIncrement(debugMap, "totalSubjectNotFound", 1);
          debugMap.put(debugPrefix + "SubjectNotFound", 1);
          if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("zoom." + configId + ".logUnresolvables", false)) {
            debugMap.put(sourceIdSubjectId.getKey(0) + "_" + sourceIdSubjectId.getKey(1) + "_notFound", true);
          }
        }
      }
    }

    Set<MultiKey> sourceIdSubjectIdsToRemoveFromGrouper = new HashSet<MultiKey>(sourceIdsSubjectIdsInGrouper);
    sourceIdSubjectIdsToRemoveFromGrouper.removeAll(sourceIdSubjectIdInZoomSet);

    if (GrouperUtil.length(sourceIdSubjectIdsToRemoveFromGrouper) > 0) {
      
      if (group == null) {
        group = GroupFinder.findByName(GrouperSession.staticGrouperSession(), groupSyncFolder + ":" + grouperGroupExtension, true);
      }
      
      for (MultiKey sourceIdSubjectId : sourceIdSubjectIdsToRemoveFromGrouper) {
        try {
          Subject subject = SubjectFinder.findByIdAndSource((String)sourceIdSubjectId.getKey(1), (String)sourceIdSubjectId.getKey(0), true);
          if (group.deleteMember(subject, false)) {
            debugMap.put(debugPrefix + "MembershipDeletes", 1);
            debugIncrement(debugMap, "membershipTotalDeleteCount", 1);
          }
        } catch (SubjectNotFoundException subjectNotFoundException) {
          debugIncrement(debugMap, "totalSubjectNotFound", 1);
          debugMap.put(debugPrefix + "SubjectNotFound", 1);
          if (GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("zoom." + configId + ".logUnresolvables", false)) {
            debugMap.put(sourceIdSubjectId.getKey(0) + "_" + sourceIdSubjectId.getKey(1), "notFound");
          }
        }
      }
    }
    
  }
  
  /**
   * 
   * @param debugMap
   * @param label
   * @param i
   */
  private static void debugIncrement(Map<String, Object> debugMap, String label, int i) {
    Integer value = (Integer)debugMap.get(label);
    if (value == null) {
      value = 0;
    }
    debugMap.put(label, value + i);
  }

  
}
