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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
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
    if (GrouperUtil.length(args) != 7) {
      throw new RuntimeException("Pass in the configId, true/false (groupSync?), groupSyncFolderName, true/false (roleSync?), roleSyncFolderName, true/false (userTypeSync?) to full load");
    }
    GrouperStartup.startup();
    String configId = args[0];
    boolean groupSync = GrouperUtil.booleanValue(args[1], false);
    String groupSyncFolder = args[2];
    boolean roleSync = GrouperUtil.booleanValue(args[3], false);
    String roleSyncFolder = args[4];
    boolean userTypeSync = GrouperUtil.booleanValue(args[5], false);
    String userTypeSyncFolder = args[6];
    
    fullLoad(configId, groupSync, groupSyncFolder, roleSync, roleSyncFolder, userTypeSync, userTypeSyncFolder);

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
    String roleLoadFolderName = loadGroups ?  GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomLoadRolesFolderName") : null;

    boolean loadUserTypes = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".zoomLoadUserTypes", false);
    String userTypeLoadFolderName = loadGroups ?  GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomLoadUserTypesFolderName") : null;
    
    Map<String, Object> resultMap = fullLoad(configId, loadGroups, groupLoadFolderName, loadRoles, roleLoadFolderName, loadUserTypes, userTypeLoadFolderName);
    
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
   * @return map with groupCount, groupAddCount, membershipAddCount, membershipDeleteCount, membershipTotalCount
   */
  public static Map<String, Object> fullLoad(final String configId, final boolean groupLoad, final String groupSyncFolder, 
        final boolean roleLoad, final String roleSyncFolder, final boolean userTypeLoad, final String userTypeSyncFolder) {
    long startedNanos = System.nanoTime();
    final Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "fullLoad");
    try {

      debugMap.put("groupCount", 0);
      debugMap.put("groupAddCount", 0);
      debugMap.put("groupDeleteCount", 0);
      debugMap.put("membershipAddCount", 0);
      debugMap.put("membershipDeleteCount", 0);
      debugMap.put("membershipTotalCount", 0);
      debugMap.put("subjectNotFound", 0);
      
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
              
              //massage
              String grouperExtension = groupZoomNameToGrouperExtension.get(zoomName);

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
              
              //massage
              String grouperExtension = roleZoomNameToGrouperExtension.get(zoomName);

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

          if (userTypeLoad) {
            
            if (StringUtils.isBlank(userTypeSyncFolder)) {
              throw new RuntimeException("User type load folder cannot be null!");
            }
            userTypeZoomNameToGrouperExtension = new HashMap<String, String>();
            
            // userTypes that exist in zoom
            Map<String, Map<String, Object>> usersInZoom = GrouperZoomCommands.retrieveUsers(configId);
            userTypeToMemberships = new HashMap<String, List<Map<String, Object>>>();
            
            for (Map<String, Object> user : usersInZoom.values()) {

              Integer typeInteger = (Integer)user.get("type");
              
              if (typeInteger == null) {
                continue;
              }
              
              String typeString = Integer.toString(typeInteger);

              List<Map<String, Object>> usersForUserType = userTypeToMemberships.get(typeString);
              
              if (usersForUserType == null) {
                
                usersForUserType = new ArrayList<Map<String, Object>>();
                userTypeToMemberships.put(typeString, usersForUserType);
                
              }

              String email = (String)user.get("email");
              if (!StringUtils.isBlank(email)) {
                emailToZoomUser.put(email, user);
              }

              usersForUserType.add(user);
              
            }
            Set<String> userTypes = new HashSet<String>(userTypeToMemberships.keySet());
            userTypes.add("1");
            userTypes.add("2");
            userTypes.add("3");
            
            for (String userType : userTypes) {
              final String grouperExtension = "zoomUserType_" + userType;
              userTypeZoomNameToGrouperExtension.put(userType, grouperExtension);
            }
          }

          Map<String, MultiKey> emailToSubject = GrouperZoomLocalCommands.convertEmailToSourceIdSubjectId(configId, emailToZoomUser.keySet());
          

          if (groupLoad) {
            
            loadGroupsAndMembershipsToGrouper(configId, groupSyncFolder, debugMap,
                groupZoomNameToGrouperExtension, groupZoomNameToMemberships);            
          }
          
          if (roleLoad) {
            
            loadGroupsAndMembershipsToGrouper(configId, roleSyncFolder, debugMap,
                roleZoomNameToGrouperExtension, roleZoomNameToMemberships);            
          }

          if (userTypeLoad) {
            
            loadGroupsAndMembershipsToGrouper(configId, userTypeSyncFolder, debugMap,
                userTypeZoomNameToGrouperExtension, userTypeToMemberships);            
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
   */
  public static void loadGroupsAndMembershipsToGrouper(final String configId,
      final String groupSyncFolder, final Map<String, Object> debugMap,
      Map<String, String> groupZoomNameToGrouperExtension,
      Map<String, List<Map<String, Object>>> groupZoomNameToMemberships) {
    Set<String> groupExtensionsInGrouper = GrouperUtil.nonNull(GrouperZoomLocalCommands.groupExtensionsInFolder(groupSyncFolder));
    
    //groupsInZoom
    //groupZoomNameToGrouperExtension
    //groupGrouperExtensionToZoomName
    //groupZoomNameToMemberships

    debugIncrement(debugMap, "groupCount", groupZoomNameToGrouperExtension.size());

    Set<String> groupsInGrouperToDelete = new HashSet<String>(groupExtensionsInGrouper);
    groupsInGrouperToDelete.removeAll(groupZoomNameToGrouperExtension.values());
    
    debugIncrement(debugMap, "groupDeleteCount", groupsInGrouperToDelete.size());
    
    GrouperZoomLocalCommands.deleteGroupExtensionsInFolder(groupSyncFolder, groupsInGrouperToDelete);
    
    Set<String> groupsInGrouperToAdd = new HashSet<String>(groupZoomNameToGrouperExtension.values());
    groupsInGrouperToAdd.removeAll(groupExtensionsInGrouper);

    debugIncrement(debugMap, "groupAddCount", groupsInGrouperToAdd.size());

    GrouperZoomLocalCommands.createGroupExtensionsInFolder(groupSyncFolder, groupsInGrouperToAdd);

    Map<String, Set<String>> grouperEntensionToEmails = GrouperZoomLocalCommands.groupsEmailsFromFolder(configId, groupSyncFolder);
    
    for (String zoomGroupName : groupZoomNameToGrouperExtension.keySet()) {
      
      String grouperGroupExtension = groupZoomNameToGrouperExtension.get(zoomGroupName);
      
      List<Map<String, Object>> usersInZoomGroup = groupZoomNameToMemberships.get(zoomGroupName);
      
      debugIncrement(debugMap, "membershipTotalCount", GrouperUtil.length(usersInZoomGroup));
      
      Set<String> emailsInGrouper = grouperEntensionToEmails.get(grouperGroupExtension);
      
      loadMembershipsToGrouper(debugMap, configId, groupSyncFolder, grouperGroupExtension, usersInZoomGroup, emailsInGrouper);
      
    }
  }

  

  /**
   * @param debugMap
   * @param configId
   * @param groupSyncFolder
   * @param grouperGroupExtension
   * @param usersInZoomGroup
   * @param emailsInGrouper
   */
  protected static void loadMembershipsToGrouper(Map<String, Object> debugMap, String configId,
      String groupSyncFolder, String grouperGroupExtension,
      List<Map<String, Object>> usersInZoomGroup, Set<String> emailsInGrouper) {

    emailsInGrouper = GrouperUtil.nonNull(emailsInGrouper);
    
    Set<String> emailsInZoomGroup = new HashSet<String>();
    Map<String, String> emailToUserIdInZoom = new HashMap<String, String>();
    
    for (Map<String, Object> user : GrouperUtil.nonNull(usersInZoomGroup)) {
      
      String email = (String)user.get("email");
      String id = (String)user.get("id");
      
      if (!StringUtils.isBlank(email)) {
        emailToUserIdInZoom.put(email, id);
        emailsInZoomGroup.add(email);
      }
      
    }
    
    Set<String> emailsToAddToGrouper = new HashSet<String>(emailsInZoomGroup);
    emailsToAddToGrouper.removeAll(emailsInGrouper);
    
    for (String emailToAddToGrouper : emailsToAddToGrouper) {
      String zoomUserId = emailToUserIdInZoom.get(emailToAddToGrouper);
      try {
        boolean added = GrouperZoomLocalCommands.addMembership(configId, groupSyncFolder, grouperGroupExtension, emailToAddToGrouper, zoomUserId);
        if (added) {
          debugIncrement(debugMap, "membershipAddCount", 1);
        }
      } catch (SubjectNotFoundException subjectNotFoundException) {
        debugIncrement(debugMap, "subjectNotFound", 1);
        debugMap.put(emailToAddToGrouper, "notFound");
      }
    }
 
    Set<String> emailsToRemoveFromGrouper = new HashSet<String>(emailsInGrouper);
    emailsToRemoveFromGrouper.removeAll(emailsInZoomGroup);
    
    for (String emailToRemoveFromGrouper : emailsToRemoveFromGrouper) {
      String zoomUserId = emailToUserIdInZoom.get(emailToRemoveFromGrouper);
      try {
        boolean deleted = GrouperZoomLocalCommands.removeMembership(configId, groupSyncFolder, grouperGroupExtension, emailToRemoveFromGrouper, zoomUserId);
        if (deleted) {
          debugIncrement(debugMap, "membershipDeleteCount", 1);
        }
      } catch (SubjectNotFoundException subjectNotFoundException) {
        debugIncrement(debugMap, "subjectNotFound", 1);
        debugMap.put(emailToRemoveFromGrouper, "notFound");
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
    debugMap.put(label, value + i);
  }

  
}
