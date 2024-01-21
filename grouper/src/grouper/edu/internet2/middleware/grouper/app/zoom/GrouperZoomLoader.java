/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.app.zoom;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.quartz.DisallowConcurrentExecution;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperDaemonUtils;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncColumnMetadata;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncConfiguration;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncOutput;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncRowData;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncSubtype;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableBean;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableData;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcTableSyncTableMetadata;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.subject.Subject;
import edu.internet2.middleware.subject.SubjectNotFoundException;


/**
 *
 */
@DisallowConcurrentExecution
public class GrouperZoomLoader extends OtherJobBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperZoomLoader.class);
  private GcTableSync loadUsersToTableGcTableSync;
  private GcTableSyncTableBean loadUsersToTableGcTableSyncTableBeanSql;
  private Set<String> loadUsersToTableUniqueKeyColumnNames;
  private String configId;
  private Map<String, Object> debugMap;
  private GcTableSyncTableData loadUsersToTableGcTableSyncTableDataSql;
  private GcTableSyncTableData loadUsersToTableGcTableSyncTableDataLdap;

  /**
   * 
   */
  public GrouperZoomLoader() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    if (GrouperUtil.length(args) != 11 && GrouperUtil.length(args) != 1) {
      throw new RuntimeException("Pass in the job name (only), or the configId, true/false (groupSync?), groupSyncFolderName, true/false (roleSync?), roleSyncFolderName, "
          + "true/false (userTypeSync?), userTypeFolderName, true/false (userStatusSync?), userStatusFolderName to full load, "
          + "true/false (userZoomPhoneSync?), userZoomPhoneSyncFolder to full load, "
          + "true/false (subAccountSync?), subAccountFolderName, true/false (loadUsersToTable?)");
    }
    GrouperStartup.startup();

    if (GrouperUtil.length(args) == 1) {
      GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

        @Override
        public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
          // OTHER_JOB_pennZoomLoader
          GrouperLoader.runOnceByJobName(grouperSession, args[0], false);
          return null;
        }
      });

    } else {
      
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
      boolean userZoomPhoneSync = GrouperUtil.booleanValue(args[11], false);
      String userZoomPhoneSyncFolder = args[12];
      boolean loadUsersToTable = GrouperUtil.booleanValue(args[13], false);

      new GrouperZoomLoader().fullLoad(configId, groupSync, groupSyncFolder, roleSync, roleSyncFolder, userTypeSync, userTypeSyncFolder, userStatusSync, userStatusSyncFolder,
          subAccountSync, subAccountSyncFolder, userZoomPhoneSync, userZoomPhoneSyncFolder, loadUsersToTable);

    }

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

    boolean loadZoomPhones = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".zoomLoadPhoneUsers", false);
    String zoomPhoneLoadFolderName = loadZoomPhones ?  GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("otherJob." + jobName + ".zoomLoadPhoneUsersFolderName") : null;

    boolean loadUsersToTable = GrouperLoaderConfig.retrieveConfig().propertyValueBoolean("otherJob." + jobName + ".zoomLoadUsersToTable", false);

    Map<String, Object> resultMap = fullLoad(configId, loadGroups, groupLoadFolderName, loadRoles, roleLoadFolderName,
        loadUserTypes, userTypeLoadFolderName, loadUserStatuses, userStatusLoadFolderName, loadSubAccounts, subAccountsLoadFolderName, loadZoomPhones, zoomPhoneLoadFolderName, loadUsersToTable);

    int groupAddCount = resultMap.containsKey("groupAddCount") ? (Integer)resultMap.get("groupAddCount") : 0;
    int groupCount = resultMap.containsKey("groupCount") ? (Integer)resultMap.get("groupCount") : 0;
    int membershipAddCount = resultMap.containsKey("membershipAddCount") ? (Integer)resultMap.get("membershipAddCount") : 0;
    int membershipDeleteCount = resultMap.containsKey("membershipDeleteCount") ? (Integer)resultMap.get("membershipDeleteCount") : 0;
    int membershipTotalCount = resultMap.containsKey("membershipTotalCount") ? (Integer)resultMap.get("membershipTotalCount") : 0;
    int loadUsersAddCount = resultMap.containsKey("loadUsersAddCount") ? (Integer)resultMap.get("loadUsersAddCount") : 0;
    int loadUsersDeleteCount = resultMap.containsKey("loadUsersDeleteCount") ? (Integer)resultMap.get("loadUsersDeleteCount") : 0;
    int loadUsersUpdateCount = resultMap.containsKey("loadUsersUpdateCount") ? (Integer)resultMap.get("loadUsersUpdateCount") : 0;
    int loadUsersTotalCount = resultMap.containsKey("loadUsersTotalCount") ? (Integer)resultMap.get("loadUsersTotalCount") : 0;

    hib3GrouperLoaderLog.addInsertCount(groupAddCount);
    hib3GrouperLoaderLog.addInsertCount(membershipAddCount);
    hib3GrouperLoaderLog.addInsertCount(loadUsersAddCount);

    hib3GrouperLoaderLog.addDeleteCount(membershipDeleteCount);
    hib3GrouperLoaderLog.addDeleteCount(loadUsersDeleteCount);

    hib3GrouperLoaderLog.addUpdateCount(loadUsersUpdateCount);

    hib3GrouperLoaderLog.addTotalCount(groupCount);
    hib3GrouperLoaderLog.addTotalCount(membershipTotalCount);
    hib3GrouperLoaderLog.addTotalCount(loadUsersTotalCount);

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
   * @param userZoomPhoneLoad
   * @param userZoomPhoneSyncFolder
   * @return map with groupCount, groupAddCount, membershipAddCount, membershipDeleteCount, membershipTotalCount
   */
  public Map<String, Object> fullLoad(final String configId, final boolean groupLoad, final String groupSyncFolder,
        final boolean roleLoad, final String roleSyncFolder, final boolean userTypeLoad, final String userTypeSyncFolder,
        final boolean userStatusLoad, final String userStatusSyncFolder, final boolean subAccountLoad, final String subAccountSyncFolder, final boolean userZoomPhoneLoad, final String userZoomPhoneSyncFolder,
        final boolean loaderUsersToTable) {
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

          Map<String, String> phoneZoomNameToGrouperExtension = null;
          Map<String, List<Map<String, Object>>> phoneZoomNameToMemberships = null;

          Map<String, Map<String, Object>> subAccountsInZoom = null;
          Map<String, String> subAccountZoomNameToGrouperExtension = null;
          Map<String, List<Map<String, Object>>> subAccountZoomNameToMemberships = null;

          Map<String, Map<String, Object>> emailToZoomUser = new HashMap<String, Map<String, Object>>();

          Map<String, Map<String, Object>> usersInZoom = null;

          if (groupLoad) {

            if (StringUtils.isBlank(groupSyncFolder)) {
              throw new RuntimeException("Group load folder cannot be null!");
            }

            //Stem groupLoadStem = new StemSave(grouperSession).assignName(groupSyncFolder).save();

            // groups that exist in zoom
            groupsInZoom = GrouperZoomCommands.retrieveGroups(configId);
            GrouperDaemonUtils.stopProcessingIfJobPaused();

            Map[] groupMaps = convertTargetNamesToGrouperNames(groupsInZoom.keySet());

            groupZoomNameToGrouperExtension = groupMaps[0];

            groupZoomNameToMemberships = new HashMap<String, List<Map<String, Object>>>();

            for (String zoomName : groupsInZoom.keySet()) {

              Map<String, Object> groupInZoom = groupsInZoom.get(zoomName);

              String groupIdInZoom = (String)groupInZoom.get("id");

              List<Map<String, Object>> zoomUsers = GrouperZoomCommands.retrieveGroupMemberships(configId, groupIdInZoom);
              GrouperDaemonUtils.stopProcessingIfJobPaused();

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
            GrouperDaemonUtils.stopProcessingIfJobPaused();

            Map[] roleMaps = convertTargetNamesToGrouperNames(rolesInZoom.keySet());

            roleZoomNameToGrouperExtension = roleMaps[0];

            roleZoomNameToMemberships = new HashMap<String, List<Map<String, Object>>>();

            for (String zoomName : rolesInZoom.keySet()) {

              Map<String, Object> roleInZoom = rolesInZoom.get(zoomName);

              String roleIdInZoom = (String)roleInZoom.get("id");

              List<Map<String, Object>> zoomUsers = GrouperZoomCommands.retrieveRoleMemberships(configId, roleIdInZoom);
              GrouperDaemonUtils.stopProcessingIfJobPaused();

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
            GrouperDaemonUtils.stopProcessingIfJobPaused();

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
              GrouperDaemonUtils.stopProcessingIfJobPaused();

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

          if (userZoomPhoneLoad) {

            if (StringUtils.isBlank(userZoomPhoneSyncFolder)) {
              throw new RuntimeException("Zoom Phone folder cannot be null!");
            }
            phoneZoomNameToGrouperExtension = new HashMap<String, String>() {
              {
                put("zoomPhoneUsers", "zoomPhoneUsers");
              }
            };

            phoneZoomNameToMemberships = new HashMap<String, List<Map<String, Object>>>();

            Map<String, Map<String, Object>> phoneZoomUser = GrouperZoomCommands.retrievePhoneUsers(configId, "phone/users");
            GrouperDaemonUtils.stopProcessingIfJobPaused();

            List<Map<String, Object>> users = new ArrayList<Map<String, Object>>();

            for (Map<String, Object> user : GrouperUtil.nonNull(phoneZoomUser).values()) {
                String email = (String)user.get("email");
                if (!StringUtils.isBlank(email)) {
                  emailToZoomUser.put(email, user);
                  users.add(user);
                }
              }
            phoneZoomNameToMemberships.put("zoomPhoneUsers", users);
          }

          if (userTypeLoad || userStatusLoad || loaderUsersToTable) {

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
            usersInZoom = GrouperZoomCommands.retrieveUsers(configId);
            GrouperDaemonUtils.stopProcessingIfJobPaused();

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

          Map<String, MultiKey> emailToSourceIdSubjectId = GrouperZoomLocalCommands.convertEmailToSourceIdSubjectId(configId, emailToZoomUser.keySet());

          if (loaderUsersToTable) {

            loadUsersToTable(configId, debugMap, usersInZoom, emailToSourceIdSubjectId);
          }


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

          if (userZoomPhoneLoad) {
            loadGroupsAndMembershipsToGrouper(configId, userZoomPhoneSyncFolder, debugMap,
                phoneZoomNameToGrouperExtension, phoneZoomNameToMemberships, "zoomPhones");
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
   *
   * @param configId
   * @param debugMap
   * @param usersInZoom
   * @param emailToSourceIdSubjectId
   */
  protected void loadUsersToTable(String theConfigId, Map<String, Object> theDebugMap,
      Map<String, Map<String, Object>> usersInZoom,
      Map<String, MultiKey> emailToSourceIdSubjectId) {

    this.configId = theConfigId;
    this.debugMap = theDebugMap;

    // we need to get data from the table
    this.loadUsersToTableGcTableSync = new GcTableSync();

    // match the member_id to each email address if we can
    Map<String, Set<String>> sourceIdToSubjectIds = new HashMap<String, Set<String>>();
    for (MultiKey sourceIdSubjectId : GrouperUtil.nonNull(emailToSourceIdSubjectId).values()) {
      String sourceId = (String)sourceIdSubjectId.getKey(0);
      String subjectId = (String)sourceIdSubjectId.getKey(1);
      Set<String> subjectIdsForSource = sourceIdToSubjectIds.get(sourceId);
      if (subjectIdsForSource == null) {
        subjectIdsForSource = new TreeSet<String>();
        sourceIdToSubjectIds.put(sourceId, subjectIdsForSource);
      }
      subjectIdsForSource.add(subjectId);
    }
    Map<MultiKey, Member> allMembersSourceIdSubjectIdToMember = new HashMap<MultiKey, Member>();
    for (String sourceId : sourceIdToSubjectIds.keySet()) {
      Set<String> subjectIdsForSource = sourceIdToSubjectIds.get(sourceId);
      Set<Member> members = GrouperDAOFactory.getFactory().getMember().findBySubjectIds(subjectIdsForSource, sourceId);
      for (Member member : GrouperUtil.nonNull(members)) {
        MultiKey sourceIdSubjectId = new MultiKey(member.getSubjectSourceId(), member.getSubjectId());
        allMembersSourceIdSubjectIdToMember.put(sourceIdSubjectId, member);
      }
    }
    Map<String, Member> emailToMember = new HashMap<String, Member>();
    for (String email : GrouperUtil.nonNull(usersInZoom).keySet()) {
      MultiKey sourceIdSubjectId = emailToSourceIdSubjectId.get(email);
      if (sourceIdSubjectId != null) {
        Member member = allMembersSourceIdSubjectIdToMember.get(sourceIdSubjectId);
        if (member == null) {
          String sourceId = (String)sourceIdSubjectId.getKey(0);
          String subjectId = (String)sourceIdSubjectId.getKey(1);
          Subject subject = SubjectFinder.findByIdAndSource(subjectId, sourceId, false);
          if (subject != null) {
            member = MemberFinder.findBySubject(GrouperSession.staticGrouperSession(), subject, true);
          }
        }
        if (member != null) {
          emailToMember.put(email, member);
        }
      }
    }

    //retrieve the existing data from database
    loadUsersToTableRetrieveDataFromDatabase();

    // convert the zoom data into something we can work with
    GcTableSyncTableBean gcTableSyncTableBeanFrom = new GcTableSyncTableBean();
    this.loadUsersToTableGcTableSync.setDataBeanFrom(gcTableSyncTableBeanFrom);
    gcTableSyncTableBeanFrom.setTableMetadata(this.loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata());
    gcTableSyncTableBeanFrom.setGcTableSync(this.loadUsersToTableGcTableSync);

    this.loadUsersToTableGcTableSyncTableDataLdap = new GcTableSyncTableData();
    loadUsersToTableGcTableSync.getDataBeanFrom().setDataInitialQuery(this.loadUsersToTableGcTableSyncTableDataLdap);

    this.loadUsersToTableGcTableSyncTableDataLdap.setColumnMetadata(this.loadUsersToTableGcTableSyncTableDataSql.getColumnMetadata());

    this.loadUsersToTableGcTableSyncTableDataLdap.setGcTableSyncTableBean(this.loadUsersToTableGcTableSyncTableDataSql.getGcTableSyncTableBean());

    List<GcTableSyncRowData> gcTableSyncRowDatas = new ArrayList<GcTableSyncRowData>();

    for (Map<String, Object> zoomUser : GrouperUtil.nonNull(usersInZoom).values()) {

      GcTableSyncRowData gcTableSyncRowData = new GcTableSyncRowData();
      gcTableSyncRowDatas.add(gcTableSyncRowData);

      gcTableSyncRowData.setGcTableSyncTableData(this.loadUsersToTableGcTableSyncTableDataLdap);

      Object[] rowData = new Object[this.loadUsersToTableGcTableSyncTableDataLdap.getColumnMetadata().size()];

      GcTableSyncColumnMetadata gcTableSyncColumnMetadata = this.loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata().lookupColumn("config_id", true);
      rowData[gcTableSyncColumnMetadata.getColumnIndexZeroIndexed()] = this.configId;

      gcTableSyncColumnMetadata = this.loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata().lookupColumn("member_id", true);
      Member member = emailToMember.get((String)zoomUser.get("email"));
      rowData[gcTableSyncColumnMetadata.getColumnIndexZeroIndexed()] = member == null ? null : member.getId();

      for (String field : zoomUser.keySet()) {
        gcTableSyncColumnMetadata = this.loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata().lookupColumn(field, false);

        // skip some cols
        if (gcTableSyncColumnMetadata == null) {
          continue;
        }
        Object fieldValue = zoomUser.get(field);

        // convert to int since thats how its stored
        if (StringUtils.equals(field, "created_at") || StringUtils.equals(field, "last_login_time")) {

          String theDateString = (String)fieldValue;
          Timestamp theTimestamp = GrouperUtil.timestampIsoUtcSecondsConvertFromString(theDateString);
          fieldValue = theTimestamp == null ? null : theTimestamp.getTime();

        }
        rowData[gcTableSyncColumnMetadata.getColumnIndexZeroIndexed()] = gcTableSyncColumnMetadata.getColumnType().convertToType(fieldValue);

      }

      gcTableSyncRowData.setData(rowData);
    }


    this.loadUsersToTableGcTableSyncTableDataLdap.setRows(gcTableSyncRowDatas);


    // compare and sync
    GcTableSyncConfiguration gcTableSyncConfiguration = new GcTableSyncConfiguration();
    this.loadUsersToTableGcTableSync.setGcTableSyncConfiguration(gcTableSyncConfiguration);

    loadUsersToTableGcTableSync.setGcTableSyncOutput(new GcTableSyncOutput());

    Map<String, Object> debugMapLocal = new LinkedHashMap<String, Object>();
    GcTableSyncSubtype.fullSyncFull.syncData(debugMapLocal, loadUsersToTableGcTableSync);

    // merge the debug maps
    for (String key : debugMapLocal.keySet()) {

      Object newValue = debugMapLocal.get(key);

      // convert micros to millis
      if (key.endsWith("Millis")) {
        if (newValue instanceof Number) {
          newValue = ((Number)newValue).longValue()/1000;
        }
      }


      String newKey = "loadUsers" + StringUtils.capitalize(key);
      this.debugMap.put(newKey, newValue);

    }

  }

  private void loadUsersToTableRetrieveDataFromDatabase() {

    this.loadUsersToTableGcTableSyncTableBeanSql = new GcTableSyncTableBean(loadUsersToTableGcTableSync);
    this.loadUsersToTableGcTableSyncTableBeanSql.configureMetadata("grouper", "grouper_prov_zoom_user");
    this.loadUsersToTableGcTableSync.setDataBeanTo(loadUsersToTableGcTableSyncTableBeanSql);

    Set<String> databaseColumnNames = GrouperUtil.toSet("config_id", "member_id", "id",
        "email", "first_name", "last_name", "type", "pmi", "timezone", "verified", "created_at", "last_login_time", "language", "status", "role_id");

    this.loadUsersToTableUniqueKeyColumnNames = GrouperUtil.toSet("email", "config_id");

    GcTableSyncTableMetadata gcTableSyncTableMetadata = loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata();

    gcTableSyncTableMetadata.assignColumns(GrouperUtil.join(databaseColumnNames.iterator(), ','));
    gcTableSyncTableMetadata.assignPrimaryKeyColumns(GrouperUtil.join(this.loadUsersToTableUniqueKeyColumnNames.iterator(), ','));

    GcDbAccess gcDbAccess = new GcDbAccess().connectionName("grouper");

    GrouperUtil.assertion(!StringUtils.isBlank(this.configId), "configId cant be null!");

    String sql = "select " + gcTableSyncTableMetadata.columnListAll() + " from " + gcTableSyncTableMetadata.getTableName() + " where config_id = ?";

    List<Object[]> results = gcDbAccess.sql(sql).addBindVar(this.configId).selectList(Object[].class);

    this.debugMap.put("loadUsersDbRows", GrouperUtil.length(results));

    this.loadUsersToTableGcTableSyncTableDataSql = new GcTableSyncTableData();
    this.loadUsersToTableGcTableSyncTableDataSql.init(this.loadUsersToTableGcTableSyncTableBeanSql, gcTableSyncTableMetadata.lookupColumns(this.loadUsersToTableGcTableSyncTableBeanSql.getTableMetadata().columnListAll()), results);
    this.loadUsersToTableGcTableSyncTableDataSql.indexData();

    loadUsersToTableGcTableSyncTableBeanSql.setDataInitialQuery(this.loadUsersToTableGcTableSyncTableDataSql);
    loadUsersToTableGcTableSyncTableBeanSql.setGcTableSync(loadUsersToTableGcTableSync);


    this.debugMap.put("loadUsersDbUniqueKeys", this.loadUsersToTableGcTableSyncTableDataSql.allPrimaryKeys().size());


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
