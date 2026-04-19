package edu.internet2.middleware.grouper.app.datadog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class DatadogApiCommands {

  private static final int MAX_PAGE_SIZE = 100;
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("dd-api-key", "dd-application-key");

  public static GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

  /**
   * Read the accessTokenPassword from the WsBearerToken external system config,
   * parse it as JSON containing {"apiKey": "...", "applicationKey": "..."},
   * and attach DD-API-KEY and DD-APPLICATION-KEY headers to the HTTP client.
   * @param grouperHttpClient the HTTP client to attach headers to
   * @param configId the external system config id
   */
  private static void attachDatadogAuthentication(GrouperHttpClient grouperHttpClient, String configId) {
    String accessTokenPassword = grouperLoaderConfig.propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");

    JsonNode keysNode = GrouperUtil.jsonJacksonNode(accessTokenPassword);
    String apiKey = GrouperUtil.jsonJacksonGetString(keysNode, "apiKey");
    String applicationKey = GrouperUtil.jsonJacksonGetString(keysNode, "applicationKey");

    if (StringUtils.isBlank(apiKey)) {
      throw new RuntimeException("apiKey is required in accessTokenPassword JSON for configId: " + configId);
    }
    if (StringUtils.isBlank(applicationKey)) {
      throw new RuntimeException("applicationKey is required in accessTokenPassword JSON for configId: " + configId);
    }

    grouperHttpClient.addHeader("DD-API-KEY", apiKey);
    grouperHttpClient.addHeader("DD-APPLICATION-KEY", applicationKey);
  }

  public static void main(String[] args) {

    GrouperStartup.startup();

    String configId = "datadogDev";

    String testRoleName = "grouperTestRole_" + System.currentTimeMillis();
    String testTeamName = "grouperTestTeam_" + System.currentTimeMillis();
    String testTeamHandle = "grouper-test-team-" + System.currentTimeMillis();
    String testUserEmail = "a@b.c";
    String testUserName = "Test User";

    try {

      // ============================
      // User operations
      // ============================

      System.out.println("\n====== CREATE USER ======");
      DatadogUser newUser = new DatadogUser();
      newUser.setEmail(testUserEmail);
      newUser.setName(testUserName);
      DatadogUser createdUser = createUser(configId, null, newUser);
      System.out.println("Created user: " + createdUser);
      GrouperUtil.assertion(StringUtils.isNotBlank(createdUser.getId()), "Created user should have an id");
      GrouperUtil.assertion(StringUtils.equals(testUserEmail, createdUser.getEmail()), "Created user email should match");
      GrouperUtil.assertion(StringUtils.equals(testUserName, createdUser.getName()), "Created user name should match");
      String userId = createdUser.getId();
      System.out.println("Created user id: " + userId);

      System.out.println("\n====== RETRIEVE USERS ======");
      List<DatadogUser> allUsers = retrieveUsers(configId, null);
      System.out.println("Total users: " + GrouperUtil.length(allUsers));
      boolean foundInAll = false;
      for (DatadogUser u : GrouperUtil.nonNull(allUsers)) {
        if (StringUtils.equals(userId, u.getId())) {
          foundInAll = true;
          break;
        }
      }
      GrouperUtil.assertion(foundInAll, "Created user should be in retrieveUsers list");
      System.out.println("Found created user in retrieveUsers: true");

      System.out.println("\n====== RETRIEVE USER BY EMAIL ======");
      DatadogUser foundUser = retrieveUserByEmail(configId, null, testUserEmail, true);
      GrouperUtil.assertion(foundUser != null, "Should find user by email");
      GrouperUtil.assertion(StringUtils.equals(userId, foundUser.getId()), "Found user id should match");
      GrouperUtil.assertion(StringUtils.equals(testUserEmail, foundUser.getEmail()), "Found user email should match");
      System.out.println("Found user by email: " + foundUser);

      System.out.println("\n====== UPDATE USER (name and title) ======");
      DatadogUser updateUserObj = new DatadogUser();
      updateUserObj.setId(userId);
      updateUserObj.setName("Grouper Updated Name");
      updateUserObj.setTitle("Test Title");
      Set<String> userFieldsToUpdate = new LinkedHashSet<String>();
      userFieldsToUpdate.add("name");
      userFieldsToUpdate.add("title");
      DatadogUser updatedUser = updateUser(configId, null, updateUserObj, userFieldsToUpdate);
      System.out.println("Updated user: " + updatedUser);
      GrouperUtil.assertion(StringUtils.equals("Grouper Updated Name", updatedUser.getName()), "Updated name should match");

      System.out.println("\n====== RETRIEVE USER BY EMAIL (verify update) ======");
      DatadogUser verifyUpdatedUser = retrieveUserByEmail(configId, null, testUserEmail, true);
      GrouperUtil.assertion(verifyUpdatedUser != null, "Should still find user by email after update");
      GrouperUtil.assertion(StringUtils.equals("Grouper Updated Name", verifyUpdatedUser.getName()), "Name should be updated");
      GrouperUtil.assertion(StringUtils.equals("Test Title", verifyUpdatedUser.getTitle()), "Title should be updated");
      System.out.println("Verified updated user: " + verifyUpdatedUser);

      // ============================
      // Team operations
      // ============================

      System.out.println("\n====== CREATE TEAM ======");
      DatadogGroup newTeam = new DatadogGroup();
      newTeam.setName(testTeamName);
      newTeam.setHandle(testTeamHandle);
      newTeam.setDescription("Grouper test team description");
      newTeam.setGroupType("team");
      DatadogGroup createdTeam = createTeam(configId, null, newTeam);
      System.out.println("Created team: " + createdTeam);
      GrouperUtil.assertion(StringUtils.isNotBlank(createdTeam.getId()), "Created team should have an id");
      GrouperUtil.assertion(StringUtils.equals(testTeamName, createdTeam.getName()), "Created team name should match");
      String teamId = createdTeam.getId();
      System.out.println("Created team id: " + teamId);

      System.out.println("\n====== RETRIEVE TEAMS ======");
      List<DatadogGroup> allTeams = retrieveTeams(configId, null);
      System.out.println("Total teams: " + GrouperUtil.length(allTeams));
      boolean foundTeamInAll = false;
      for (DatadogGroup t : GrouperUtil.nonNull(allTeams)) {
        if (StringUtils.equals(teamId, t.getId())) {
          foundTeamInAll = true;
          break;
        }
      }
      GrouperUtil.assertion(foundTeamInAll, "Created team should be in retrieveTeams list");
      System.out.println("Found created team in retrieveTeams: true");

      System.out.println("\n====== UPDATE TEAM ======");
      DatadogGroup updateTeamObj = new DatadogGroup();
      updateTeamObj.setId(teamId);
      updateTeamObj.setGroupType("team");
      updateTeamObj.setName(testTeamName + "_updated");
      updateTeamObj.setDescription("Updated description");
      Set<String> teamFieldsToUpdate = new LinkedHashSet<String>();
      teamFieldsToUpdate.add("name");
      teamFieldsToUpdate.add("description");
      DatadogGroup updatedTeam = updateTeam(configId, null, updateTeamObj, teamFieldsToUpdate);
      System.out.println("Updated team: " + updatedTeam);
      GrouperUtil.assertion(StringUtils.equals(testTeamName + "_updated", updatedTeam.getName()), "Updated team name should match");

      System.out.println("\n====== ADD USER TO TEAM (member) ======");
      addUserToTeam(configId, null, teamId, userId);
      System.out.println("Added user " + userId + " to team " + teamId);

      System.out.println("\n====== GET TEAM MEMBERSHIPS ======");
      List<DatadogMembership> teamMemberships = getTeamMemberships(configId, null, teamId);
      System.out.println("Team memberships count: " + GrouperUtil.length(teamMemberships));
      boolean foundUserInTeam = false;
      for (DatadogMembership m : GrouperUtil.nonNull(teamMemberships)) {
        System.out.println("  membership: userId=" + m.getUserId() + ", role=" + m.getRole());
        if (StringUtils.equals(userId, m.getUserId())) {
          foundUserInTeam = true;
        }
      }
      GrouperUtil.assertion(foundUserInTeam, "User should be in team memberships");
      System.out.println("Found user in team memberships: true");

      System.out.println("\n====== REMOVE USER FROM TEAM ======");
      removeUserFromTeam(configId, null, teamId, userId);
      System.out.println("Removed user " + userId + " from team " + teamId);

      System.out.println("\n====== VERIFY USER REMOVED FROM TEAM ======");
      List<DatadogMembership> teamMembershipsAfterRemove = getTeamMemberships(configId, null, teamId);
      boolean stillInTeam = false;
      for (DatadogMembership m : GrouperUtil.nonNull(teamMembershipsAfterRemove)) {
        if (StringUtils.equals(userId, m.getUserId())) {
          stillInTeam = true;
        }
      }
      GrouperUtil.assertion(!stillInTeam, "User should NOT be in team memberships after removal");
      System.out.println("Verified user removed from team: true");

      System.out.println("\n====== REMOVE USER FROM TEAM (not found, should not error) ======");
      removeUserFromTeam(configId, null, teamId, userId);
      System.out.println("Remove again (404 accepted): ok");

      // ============================
      // Role operations
      // ============================

      System.out.println("\n====== CREATE ROLE ======");
      DatadogGroup newRole = new DatadogGroup();
      newRole.setName(testRoleName);
      newRole.setGroupType("role");
      DatadogGroup createdRole = createRole(configId, null, newRole);
      System.out.println("Created role: " + createdRole);
      GrouperUtil.assertion(StringUtils.isNotBlank(createdRole.getId()), "Created role should have an id");
      GrouperUtil.assertion(StringUtils.equals(testRoleName, createdRole.getName()), "Created role name should match");
      String roleId = createdRole.getId();
      System.out.println("Created role id: " + roleId);

      System.out.println("\n====== RETRIEVE ROLES ======");
      List<DatadogGroup> allRoles = retrieveRoles(configId, null);
      System.out.println("Total roles: " + GrouperUtil.length(allRoles));
      boolean foundRoleInAll = false;
      for (DatadogGroup r : GrouperUtil.nonNull(allRoles)) {
        if (StringUtils.equals(roleId, r.getId())) {
          foundRoleInAll = true;
          break;
        }
      }
      GrouperUtil.assertion(foundRoleInAll, "Created role should be in retrieveRoles list");
      System.out.println("Found created role in retrieveRoles: true");

      System.out.println("\n====== UPDATE ROLE ======");
      DatadogGroup updateRoleObj = new DatadogGroup();
      updateRoleObj.setId(roleId);
      updateRoleObj.setGroupType("role");
      updateRoleObj.setName(testRoleName + "_updated");
      Set<String> roleFieldsToUpdate = new LinkedHashSet<String>();
      roleFieldsToUpdate.add("name");
      DatadogGroup updatedRole = updateRole(configId, null, updateRoleObj, roleFieldsToUpdate);
      System.out.println("Updated role: " + updatedRole);
      GrouperUtil.assertion(StringUtils.equals(testRoleName + "_updated", updatedRole.getName()), "Updated role name should match");

      System.out.println("\n====== ADD USER TO ROLE ======");
      addUserToRole(configId, null, roleId, userId);
      System.out.println("Added user " + userId + " to role " + roleId);

      System.out.println("\n====== GET ROLE USERS ======");
      List<DatadogUser> roleUsers = getRoleUsers(configId, null, roleId);
      System.out.println("Role users count: " + GrouperUtil.length(roleUsers));
      boolean foundUserInRole = false;
      for (DatadogUser ru : GrouperUtil.nonNull(roleUsers)) {
        System.out.println("  role user: id=" + ru.getId() + ", email=" + ru.getEmail());
        if (StringUtils.equals(userId, ru.getId())) {
          foundUserInRole = true;
        }
      }
      GrouperUtil.assertion(foundUserInRole, "User should be in role users");
      System.out.println("Found user in role users: true");

      System.out.println("\n====== REMOVE USER FROM ROLE ======");
      removeUserFromRole(configId, null, roleId, userId);
      System.out.println("Removed user " + userId + " from role " + roleId);

      System.out.println("\n====== VERIFY USER REMOVED FROM ROLE ======");
      List<DatadogUser> roleUsersAfterRemove = getRoleUsers(configId, null, roleId);
      boolean stillInRole = false;
      for (DatadogUser ru : GrouperUtil.nonNull(roleUsersAfterRemove)) {
        if (StringUtils.equals(userId, ru.getId())) {
          stillInRole = true;
        }
      }
      GrouperUtil.assertion(!stillInRole, "User should NOT be in role users after removal");
      System.out.println("Verified user removed from role: true");

      System.out.println("\n====== REMOVE USER FROM ROLE (not found, should not error) ======");
      removeUserFromRole(configId, null, roleId, userId);
      System.out.println("Remove again (404 accepted): ok");

      // ============================
      // Cleanup: delete role, team, disable user
      // ============================

      System.out.println("\n====== DELETE ROLE ======");
      DatadogGroup roleToDelete = new DatadogGroup();
      roleToDelete.setId(roleId);
      roleToDelete.setGroupType("role");
      deleteRole(configId, null, roleToDelete);
      System.out.println("Deleted role: " + roleId);

      System.out.println("\n====== VERIFY ROLE DELETED ======");
      List<DatadogGroup> rolesAfterDelete = retrieveRoles(configId, null);
      boolean roleStillExists = false;
      for (DatadogGroup r : GrouperUtil.nonNull(rolesAfterDelete)) {
        if (StringUtils.equals(roleId, r.getId())) {
          roleStillExists = true;
        }
      }
      GrouperUtil.assertion(!roleStillExists, "Role should not exist after delete");
      System.out.println("Verified role deleted: true");

      System.out.println("\n====== DELETE ROLE (not found, should not error) ======");
      deleteRole(configId, null, roleToDelete);
      System.out.println("Delete again (404 accepted): ok");

      System.out.println("\n====== DELETE TEAM ======");
      DatadogGroup teamToDelete = new DatadogGroup();
      teamToDelete.setId(teamId);
      teamToDelete.setGroupType("team");
      deleteTeam(configId, null, teamToDelete);
      System.out.println("Deleted team: " + teamId);

      System.out.println("\n====== VERIFY TEAM DELETED ======");
      List<DatadogGroup> teamsAfterDelete = retrieveTeams(configId, null);
      boolean teamStillExists = false;
      for (DatadogGroup t : GrouperUtil.nonNull(teamsAfterDelete)) {
        if (StringUtils.equals(teamId, t.getId())) {
          teamStillExists = true;
        }
      }
      GrouperUtil.assertion(!teamStillExists, "Team should not exist after delete");
      System.out.println("Verified team deleted: true");

      System.out.println("\n====== DELETE TEAM (not found, should not error) ======");
      deleteTeam(configId, null, teamToDelete);
      System.out.println("Delete again (404 accepted): ok");

      System.out.println("\n====== DISABLE USER ======");
      DatadogUser userToDisable = new DatadogUser();
      userToDisable.setId(userId);
      DatadogUser disabledUser = disableUser(configId, null, userToDisable);
      System.out.println("Disabled user: " + disabledUser);
      GrouperUtil.assertion(disabledUser.getDisabled() != null && disabledUser.getDisabled(), "User should be disabled");

      System.out.println("\n====== VERIFY USER DISABLED ======");
      DatadogUser verifyDisabledUser = retrieveUserByEmail(configId, null, testUserEmail, true);
      GrouperUtil.assertion(verifyDisabledUser != null, "Should find disabled user with includeDisabledUsers=true");
      GrouperUtil.assertion(verifyDisabledUser.getDisabled() != null && verifyDisabledUser.getDisabled(), "User should show as disabled");
      System.out.println("Verified user disabled: true");

      System.out.println("\n====== RETRIEVE USER BY EMAIL (disabled, not included) ======");
      DatadogUser shouldBeNull = retrieveUserByEmail(configId, null, testUserEmail, false);
      GrouperUtil.assertion(shouldBeNull == null, "Should NOT find disabled user with includeDisabledUsers=false");
      System.out.println("Verified disabled user not found with includeDisabledUsers=false: true");

      System.out.println("\n============================");
      System.out.println("ALL TESTS PASSED!");
      System.out.println("============================");

    } catch (Exception e) {
      System.out.println("\nERROR: " + GrouperClientUtils.getFullStackTrace(e));
    }
    System.exit(0);
  }

  /**
   * Execute an HTTP method against the Datadog API.
   * Authentication uses DD-API-KEY and DD-APPLICATION-KEY headers read from config.
   * @param debugMap map to accumulate debug info
   * @param debugLabel label for stats tracking
   * @param httpMethodName GET, POST, PATCH, DELETE, etc.
   * @param configId external system config id
   * @param urlSuffix path after the base URL (e.g. "/api/v2/users")
   * @param allowedReturnCodes set of acceptable HTTP status codes
   * @param returnCode single-element array to receive the actual HTTP status code
   * @param bodyParam request body JSON string, or null
   * @param pageNumber 0-indexed page number for paging, or null for no paging
   * @param addPageSize whether to add page[size] parameter
   * @return parsed JSON response, or null if body is blank
   */
  private static JsonNode executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes,
      int[] returnCode, String bodyParam, Integer pageNumber, boolean addPageSize) {

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders);

    attachDatadogAuthentication(grouperHttpClient, configId);

    String configPrefix = "grouper.wsBearerToken." + configId + ".";

    String url = grouperLoaderConfig.propertyValueStringRequired(configPrefix + "endpoint");

    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    if (!urlSuffix.startsWith("http")) {
      url += (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    } else {
      url = urlSuffix;
    }
    debugMap.put("url", url);

    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(httpMethodName);

    if (StringUtils.isNotBlank(bodyParam)) {
      grouperHttpClient.assignBody(bodyParam);
    }

    if (pageNumber != null) {
      grouperHttpClient.addUrlParameter("page[number]", Integer.toString(pageNumber));
    }

    if (addPageSize) {
      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE);
      grouperHttpClient.addUrlParameter("page[size]", Integer.toString(pageSize));
    }

    if (StringUtils.isNotBlank(bodyParam)) {
      grouperHttpClient.addHeader("Content-Type", "application/json");
    }

    long httpCallStartMillis = System.currentTimeMillis();
    try {
      grouperHttpClient.executeRequest();
    } finally {
      GrouperProvisioner.incrementCommandsCallsStats(debugLabel, 1,
          System.currentTimeMillis() - httpCallStartMillis);
    }

    int code = -1;
    String json = null;

    try {
      code = grouperHttpClient.getResponseCode();
      returnCode[0] = code;
      json = grouperHttpClient.getResponseBody();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }

    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException(
          "Invalid return code '" + code + "', expecting: " + GrouperUtil.setToString(allowedReturnCodes)
              + ". '" + debugMap.get("url") + "' " + json);
    }

    if (StringUtils.isBlank(json)) {
      return null;
    }

    try {
      JsonNode rootNode = GrouperUtil.jsonJacksonNode(json);
      return rootNode;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }
  }


  // ============================
  // User methods
  // ============================

  /**
   * Retrieve all users from Datadog, filtering out service accounts, disabled users,
   * and ignored user emails (if datadogSettings is not null).
   * Pages through results using page[number] (0-indexed) and page[size].
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @return list of non-service-account, non-disabled, non-ignored users
   */
  public static List<DatadogUser> retrieveUsers(String configId, DatadogSettings datadogSettings) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<DatadogUser> results = new ArrayList<DatadogUser>();

    debugMap.put("method", "retrieveUsers");

    long startTime = System.nanoTime();

    try {

      int pageNumber = 0;
      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE);

      while (true) {

        JsonNode jsonNode = executeMethod(debugMap, "retrieveUsers", "GET", configId, "/api/v2/users",
            GrouperUtil.toSet(200), new int[] { -1 }, null, pageNumber, true);

        ArrayNode dataArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "data");

        for (int i = 0; i < (dataArray == null ? 0 : dataArray.size()); i++) {
          JsonNode userDataNode = dataArray.get(i);
          DatadogUser datadogUser = DatadogUser.fromJson(userDataNode);
          // filter out service accounts
          if (datadogUser.getServiceAccount() != null && datadogUser.getServiceAccount()) {
            continue;
          }
          // filter out disabled users
          if (datadogUser.getDisabled() != null && datadogUser.getDisabled()) {
            continue;
          }
          // filter out ignored user emails (check email and handle)
          if (datadogSettings != null
              && (datadogSettings.isIgnoredUserEmail(datadogUser.getEmail())
                  || datadogSettings.isIgnoredUserEmail(datadogUser.getHandle()))) {
            continue;
          }
          results.add(datadogUser);
        }

        // check if we've retrieved all pages
        int returnedCount = dataArray == null ? 0 : dataArray.size();
        if (returnedCount < pageSize) {
          break;
        }

        pageNumber++;
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Retrieve a single user by email address from Datadog.
   * Uses GET /api/v2/users?filter=email.
   * Filters out service accounts and users whose email and handle don't match the input.
   * Throws an exception if the email is in the ignore list (if datadogSettings is not null).
   * Throws an exception if more than one matching user is found after filtering.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param email the email to look up
   * @param includeDisabledUsers true to include disabled users (e.g. for create conflict handling)
   * @return the user, or null if not found
   */
  public static DatadogUser retrieveUserByEmail(String configId, DatadogSettings datadogSettings, String email, boolean includeDisabledUsers) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveUserByEmail");

    long startTime = System.nanoTime();

    try {

      if (datadogSettings != null) {
        datadogSettings.assertNotIgnoredUserEmail(email);
      }

      int[] returnCode = new int[] { -1 };

      String urlSuffix = "/api/v2/users?filter=" + GrouperUtil.escapeUrlEncode(email);

      JsonNode jsonNode = executeMethod(debugMap, "retrieveUserByEmail", "GET", configId, urlSuffix,
          GrouperUtil.toSet(200), returnCode, null, null, false);

      if (jsonNode == null) {
        return null;
      }

      ArrayNode dataArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "data");
      if (dataArray == null || dataArray.size() == 0) {
        return null;
      }

      List<DatadogUser> matchingUsers = new ArrayList<DatadogUser>();

      for (int i = 0; i < dataArray.size(); i++) {
        DatadogUser datadogUser = DatadogUser.fromJson(dataArray.get(i));

        // filter out service accounts
        if (datadogUser.getServiceAccount() != null && datadogUser.getServiceAccount()) {
          continue;
        }

        // filter out disabled users unless caller wants them
        if (!includeDisabledUsers && datadogUser.getDisabled() != null && datadogUser.getDisabled()) {
          continue;
        }

        // filter out users where neither email nor handle matches the input
        boolean emailMatches = StringUtils.equalsIgnoreCase(email, datadogUser.getEmail());
        boolean handleMatches = StringUtils.equalsIgnoreCase(email, datadogUser.getHandle());
        if (!emailMatches && !handleMatches) {
          continue;
        }

        matchingUsers.add(datadogUser);
      }

      if (matchingUsers.size() == 0) {
        return null;
      }

      if (matchingUsers.size() > 1) {
        StringBuilder sb = new StringBuilder("Found " + matchingUsers.size()
            + " matching users for email '" + email + "': ");
        for (DatadogUser u : matchingUsers) {
          sb.append("[id=").append(u.getId())
              .append(", email=").append(u.getEmail())
              .append(", handle=").append(u.getHandle())
              .append(", disabled=").append(u.getDisabled())
              .append("] ");
        }
        throw new RuntimeException(sb.toString().trim());
      }

      return matchingUsers.get(0);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Update a user in Datadog via PATCH /api/v2/users/{id}.
   * Throws an exception if the user's email is in the ignore list (if datadogSettings is not null).
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param datadogUser the user with updated fields
   * @param fieldNamesToSet the set of field names to include in the update
   * @return the updated user
   */
  public static DatadogUser updateUser(String configId, DatadogSettings datadogSettings, DatadogUser datadogUser, Set<String> fieldNamesToSet) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateUser");

    long startTime = System.nanoTime();

    try {

      if (datadogUser == null) {
        throw new RuntimeException("datadogUser is null");
      }
      if (StringUtils.isBlank(datadogUser.getId())) {
        throw new RuntimeException("datadogUser.id is required for update");
      }

      if (datadogSettings != null) {
        datadogSettings.assertNotIgnoredUserEmail(datadogUser.getEmail());
      }

      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      rootNode.set("data", datadogUser.toJson(fieldNamesToSet));

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      JsonNode responseNode = executeMethod(debugMap, "updateUser", "PATCH", configId,
          "/api/v2/users/" + datadogUser.getId(),
          GrouperUtil.toSet(200), returnCode, jsonToSend, null, false);

      JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(responseNode, "data");
      return DatadogUser.fromJson(dataNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Create a user in Datadog.
   * If the user already exists (looked up by email), re-enable if disabled and update.
   * Otherwise POST /api/v2/users to create a new invitation.
   * Throws an exception if the user's email is in the ignore list (if datadogSettings is not null).
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param datadogUser the user to create
   * @return the created or updated user
   */
  public static DatadogUser createUser(String configId, DatadogSettings datadogSettings, DatadogUser datadogUser) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createUser");

    long startTime = System.nanoTime();

    try {

      if (datadogSettings != null) {
        datadogSettings.assertNotIgnoredUserEmail(datadogUser.getEmail());
      }

      // check if user already exists by email
      DatadogUser existingUser = null;
      if (!StringUtils.isBlank(datadogUser.getEmail())) {
        existingUser = retrieveUserByEmail(configId, datadogSettings, datadogUser.getEmail(), true);
      }

      if (existingUser != null) {
        debugMap.put("existingUserFound", true);

        // build fields to update
        Set<String> fieldsToUpdate = new LinkedHashSet<String>();
        fieldsToUpdate.add("email");
        fieldsToUpdate.add("name");
        fieldsToUpdate.add("title");
        fieldsToUpdate.add("disabled");

        // set id from existing user
        datadogUser.setId(existingUser.getId());

        // re-enable if disabled
        if (existingUser.getDisabled() != null && existingUser.getDisabled()) {
          debugMap.put("reEnablingUser", true);
          datadogUser.setDisabled(false);
        }

        return updateUser(configId, datadogSettings, datadogUser, fieldsToUpdate);
      }

      // no existing user, create new
      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      rootNode.set("data", datadogUser.toJson(null));

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      JsonNode responseNode = executeMethod(debugMap, "createUser", "POST", configId, "/api/v2/users",
          GrouperUtil.toSet(201), returnCode, jsonToSend, null, false);

      JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(responseNode, "data");
      return DatadogUser.fromJson(dataNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Disable a user in Datadog by PATCHing disabled=true.
   * This is the soft delete for Datadog users.
   * Throws an exception if the user's email is in the ignore list (if datadogSettings is not null).
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param datadogUser the user to disable (must have id set)
   * @return the updated user
   */
  public static DatadogUser disableUser(String configId, DatadogSettings datadogSettings, DatadogUser datadogUser) {

    datadogUser.setDisabled(true);

    Set<String> fieldsToUpdate = new LinkedHashSet<String>();
    fieldsToUpdate.add("disabled");

    return updateUser(configId, datadogSettings, datadogUser, fieldsToUpdate);
  }


  /**
   * Assert that the group type is the expected value.
   * If null, set it to the expected value.
   * If set to something else, throw an exception.
   * @param datadogGroup the group to check
   * @param expectedGroupType the expected group type
   */
  private static void assertGroupType(DatadogGroup datadogGroup, String expectedGroupType) {
    if (datadogGroup.getGroupType() == null) {
      datadogGroup.setGroupType(expectedGroupType);
    } else if (!StringUtils.equals(datadogGroup.getGroupType(), expectedGroupType)) {
      throw new RuntimeException("Expected groupType '" + expectedGroupType + "' but was '" + datadogGroup.getGroupType() + "'");
    }
  }

  // ============================
  // Role methods
  // ============================

  /**
   * Retrieve all roles from Datadog via GET /api/v2/roles.
   * Filters out ignored roles (if datadogSettings is not null).
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @return list of non-ignored roles as DatadogGroup objects with groupType="role"
   */
  public static List<DatadogGroup> retrieveRoles(String configId, DatadogSettings datadogSettings) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<DatadogGroup> results = new ArrayList<DatadogGroup>();

    debugMap.put("method", "retrieveRoles");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonNode = executeMethod(debugMap, "retrieveRoles", "GET", configId, "/api/v2/roles",
          GrouperUtil.toSet(200), new int[] { -1 }, null, null, false);

      ArrayNode dataArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "data");

      for (int i = 0; i < (dataArray == null ? 0 : dataArray.size()); i++) {
        JsonNode roleDataNode = dataArray.get(i);
        DatadogGroup datadogGroup = DatadogGroup.fromJson(roleDataNode);
        datadogGroup.setGroupType("role");

        // filter out ignored roles
        if (datadogSettings != null && datadogSettings.isIgnoredRole(datadogGroup.getName())) {
          continue;
        }

        results.add(datadogGroup);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Create a role in Datadog via POST /api/v2/roles.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param datadogGroup the role to create (must have name set)
   * @return the created role as DatadogGroup with groupType="role"
   */
  public static DatadogGroup createRole(String configId, DatadogSettings datadogSettings, DatadogGroup datadogGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createRole");

    long startTime = System.nanoTime();

    try {

      if (datadogSettings != null && datadogSettings.isIgnoredRole(datadogGroup.getName())) {
        throw new RuntimeException("Role name '" + datadogGroup.getName() + "' is in the datadogIgnoreRoles list and should not be created");
      }

      assertGroupType(datadogGroup, "role");

      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      rootNode.set("data", datadogGroup.toJson(null));

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      JsonNode responseNode = executeMethod(debugMap, "createRole", "POST", configId, "/api/v2/roles",
          GrouperUtil.toSet(200), returnCode, jsonToSend, null, false);

      JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(responseNode, "data");
      DatadogGroup result = DatadogGroup.fromJson(dataNode);
      result.setGroupType("role");
      return result;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Update a role in Datadog via PATCH /api/v2/roles/{id}.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param datadogGroup the role with updated fields (must have id set)
   * @param fieldNamesToSet the set of field names to include in the update
   * @return the updated role as DatadogGroup with groupType="role"
   */
  public static DatadogGroup updateRole(String configId, DatadogSettings datadogSettings, DatadogGroup datadogGroup, Set<String> fieldNamesToSet) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateRole");

    long startTime = System.nanoTime();

    try {

      if (datadogGroup == null) {
        throw new RuntimeException("datadogGroup is null");
      }
      if (StringUtils.isBlank(datadogGroup.getId())) {
        throw new RuntimeException("datadogGroup.id is required for update");
      }

      if (datadogSettings != null && datadogSettings.isIgnoredRole(datadogGroup.getName())) {
        throw new RuntimeException("Role name '" + datadogGroup.getName() + "' is in the datadogIgnoreRoles list and should not be updated");
      }

      assertGroupType(datadogGroup, "role");

      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      rootNode.set("data", datadogGroup.toJson(fieldNamesToSet));

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      JsonNode responseNode = executeMethod(debugMap, "updateRole", "PATCH", configId,
          "/api/v2/roles/" + datadogGroup.getId(),
          GrouperUtil.toSet(200), returnCode, jsonToSend, null, false);

      JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(responseNode, "data");
      DatadogGroup result = DatadogGroup.fromJson(dataNode);
      result.setGroupType("role");
      return result;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Delete a role in Datadog via DELETE /api/v2/roles/{id}.
   * Throws an exception if the role name is in the ignore list (if datadogSettings is not null).
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param datadogGroup the role to delete (must have id set, name for ignore check)
   */
  public static void deleteRole(String configId, DatadogSettings datadogSettings, DatadogGroup datadogGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "deleteRole");

    long startTime = System.nanoTime();

    try {

      if (datadogGroup == null) {
        throw new RuntimeException("datadogGroup is null");
      }
      if (StringUtils.isBlank(datadogGroup.getId())) {
        throw new RuntimeException("datadogGroup.id is required for delete");
      }

      assertGroupType(datadogGroup, "role");

      if (datadogSettings != null && datadogSettings.isIgnoredRole(datadogGroup.getName())) {
        throw new RuntimeException("Role name '" + datadogGroup.getName() + "' is in the datadogIgnoreRoles list and should not be deleted");
      }

      int[] returnCode = new int[] { -1 };

      executeMethod(debugMap, "deleteRole", "DELETE", configId,
          "/api/v2/roles/" + datadogGroup.getId(),
          GrouperUtil.toSet(204, 404), returnCode, null, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }


  // ============================
  // Role membership methods
  // ============================

  /**
   * Retrieve users assigned to a role via GET /api/v2/roles/{roleId}/users.
   * Pages through results.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param roleId the UUID of the role
   * @return list of users assigned to the role
   */
  public static List<DatadogUser> getRoleUsers(String configId, DatadogSettings datadogSettings, String roleId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<DatadogUser> results = new ArrayList<DatadogUser>();

    debugMap.put("method", "getRoleUsers");

    long startTime = System.nanoTime();

    try {

      int pageNumber = 0;
      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE);

      while (true) {

        JsonNode jsonNode = executeMethod(debugMap, "getRoleUsers", "GET", configId,
            "/api/v2/roles/" + roleId + "/users",
            GrouperUtil.toSet(200), new int[] { -1 }, null, pageNumber, true);

        ArrayNode dataArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "data");

        for (int i = 0; i < (dataArray == null ? 0 : dataArray.size()); i++) {
          JsonNode userDataNode = dataArray.get(i);
          DatadogUser datadogUser = DatadogUser.fromJson(userDataNode);

          // filter out service accounts
          if (datadogUser.getServiceAccount() != null && datadogUser.getServiceAccount()) {
            continue;
          }
          // filter out ignored user emails
          if (datadogSettings != null
              && (datadogSettings.isIgnoredUserEmail(datadogUser.getEmail())
                  || datadogSettings.isIgnoredUserEmail(datadogUser.getHandle()))) {
            continue;
          }
          results.add(datadogUser);
        }

        int returnedCount = dataArray == null ? 0 : dataArray.size();
        if (returnedCount < pageSize) {
          break;
        }

        pageNumber++;
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Add a user to a role via POST /api/v2/roles/{roleId}/users.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param roleId the UUID of the role
   * @param userId the UUID of the user to add
   */
  public static void addUserToRole(String configId, DatadogSettings datadogSettings, String roleId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "addUserToRole");

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(roleId)) {
        throw new RuntimeException("roleId is required");
      }
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is required");
      }

      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      ObjectNode dataNode = GrouperUtil.jsonJacksonNode();
      dataNode.put("type", "users");
      dataNode.put("id", userId);
      rootNode.set("data", dataNode);

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      executeMethod(debugMap, "addUserToRole", "POST", configId,
          "/api/v2/roles/" + roleId + "/users",
          GrouperUtil.toSet(200), returnCode, jsonToSend, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Remove a user from a role via DELETE /api/v2/roles/{roleId}/users.
   * Note: this is a DELETE with a request body.
   * Accepts 200 and 404 (already removed).
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param roleId the UUID of the role
   * @param userId the UUID of the user to remove
   */
  public static void removeUserFromRole(String configId, DatadogSettings datadogSettings, String roleId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "removeUserFromRole");

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(roleId)) {
        throw new RuntimeException("roleId is required");
      }
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is required");
      }

      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      ObjectNode dataNode = GrouperUtil.jsonJacksonNode();
      dataNode.put("type", "users");
      dataNode.put("id", userId);
      rootNode.set("data", dataNode);

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      executeMethod(debugMap, "removeUserFromRole", "DELETE", configId,
          "/api/v2/roles/" + roleId + "/users",
          GrouperUtil.toSet(200, 404), returnCode, jsonToSend, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }


  // ============================
  // Team methods
  // ============================

  /**
   * Retrieve all teams from Datadog via GET /api/v2/team.
   * Pages through results using page[number] (0-indexed) and page[size].
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @return list of teams as DatadogGroup objects with groupType="team"
   */
  public static List<DatadogGroup> retrieveTeams(String configId, DatadogSettings datadogSettings) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<DatadogGroup> results = new ArrayList<DatadogGroup>();

    debugMap.put("method", "retrieveTeams");

    long startTime = System.nanoTime();

    try {

      int pageNumber = 0;
      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE);

      while (true) {

        JsonNode jsonNode = executeMethod(debugMap, "retrieveTeams", "GET", configId, "/api/v2/team",
            GrouperUtil.toSet(200), new int[] { -1 }, null, pageNumber, true);

        ArrayNode dataArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "data");

        for (int i = 0; i < (dataArray == null ? 0 : dataArray.size()); i++) {
          JsonNode teamDataNode = dataArray.get(i);
          DatadogGroup datadogGroup = DatadogGroup.fromJson(teamDataNode);
          datadogGroup.setGroupType("team");
          results.add(datadogGroup);
        }

        // check if we've retrieved all pages
        int returnedCount = dataArray == null ? 0 : dataArray.size();
        if (returnedCount < pageSize) {
          break;
        }

        pageNumber++;
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Create a team in Datadog via POST /api/v2/team.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param datadogGroup the team to create (must have name and handle set)
   * @return the created team as DatadogGroup with groupType="team"
   */
  public static DatadogGroup createTeam(String configId, DatadogSettings datadogSettings, DatadogGroup datadogGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createTeam");

    long startTime = System.nanoTime();

    try {

      assertGroupType(datadogGroup, "team");

      // auto-generate handle from name if not set (Datadog requires handle for teams)
      if (StringUtils.isBlank(datadogGroup.getHandle()) && StringUtils.isNotBlank(datadogGroup.getName())) {
        datadogGroup.setHandle(datadogGroup.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", ""));
      }

      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      rootNode.set("data", datadogGroup.toJson(null));

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      JsonNode responseNode = executeMethod(debugMap, "createTeam", "POST", configId, "/api/v2/team",
          GrouperUtil.toSet(201), returnCode, jsonToSend, null, false);

      JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(responseNode, "data");
      DatadogGroup result = DatadogGroup.fromJson(dataNode);
      result.setGroupType("team");
      return result;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Update a team in Datadog via PATCH /api/v2/team/{id}.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param datadogGroup the team with updated fields (must have id set)
   * @param fieldNamesToSet the set of field names to include in the update
   * @return the updated team as DatadogGroup with groupType="team"
   */
  public static DatadogGroup updateTeam(String configId, DatadogSettings datadogSettings, DatadogGroup datadogGroup, Set<String> fieldNamesToSet) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateTeam");

    long startTime = System.nanoTime();

    try {

      if (datadogGroup == null) {
        throw new RuntimeException("datadogGroup is null");
      }
      if (StringUtils.isBlank(datadogGroup.getId())) {
        throw new RuntimeException("datadogGroup.id is required for update");
      }

      assertGroupType(datadogGroup, "team");

      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      rootNode.set("data", datadogGroup.toJson(fieldNamesToSet));

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      JsonNode responseNode = executeMethod(debugMap, "updateTeam", "PATCH", configId,
          "/api/v2/team/" + datadogGroup.getId(),
          GrouperUtil.toSet(200), returnCode, jsonToSend, null, false);

      JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(responseNode, "data");
      DatadogGroup result = DatadogGroup.fromJson(dataNode);
      result.setGroupType("team");
      return result;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Delete a team in Datadog via DELETE /api/v2/team/{id}.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param datadogGroup the team to delete (must have id set)
   */
  public static void deleteTeam(String configId, DatadogSettings datadogSettings, DatadogGroup datadogGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "deleteTeam");

    long startTime = System.nanoTime();

    try {

      if (datadogGroup == null) {
        throw new RuntimeException("datadogGroup is null");
      }
      if (StringUtils.isBlank(datadogGroup.getId())) {
        throw new RuntimeException("datadogGroup.id is required for delete");
      }

      assertGroupType(datadogGroup, "team");

      int[] returnCode = new int[] { -1 };

      executeMethod(debugMap, "deleteTeam", "DELETE", configId,
          "/api/v2/team/" + datadogGroup.getId(),
          GrouperUtil.toSet(204, 404), returnCode, null, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }


  // ============================
  // Team membership methods
  // ============================

  /**
   * Retrieve memberships for a team via GET /api/v2/team/{teamId}/memberships.
   * Pages through results. Returns DatadogMembership objects with userId and role.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param teamId the UUID of the team
   * @return list of memberships for the team
   */
  public static List<DatadogMembership> getTeamMemberships(String configId, DatadogSettings datadogSettings, String teamId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<DatadogMembership> results = new ArrayList<DatadogMembership>();

    debugMap.put("method", "getTeamMemberships");

    long startTime = System.nanoTime();

    try {

      int pageNumber = 0;
      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE);

      while (true) {

        JsonNode jsonNode = executeMethod(debugMap, "getTeamMemberships", "GET", configId,
            "/api/v2/team/" + teamId + "/memberships",
            GrouperUtil.toSet(200), new int[] { -1 }, null, pageNumber, true);

        ArrayNode dataArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "data");

        for (int i = 0; i < (dataArray == null ? 0 : dataArray.size()); i++) {
          JsonNode membershipNode = dataArray.get(i);

          DatadogMembership membership = new DatadogMembership();
          membership.setId(GrouperUtil.jsonJacksonGetString(membershipNode, "id"));
          membership.setGroupId(teamId);

          // role from attributes
          JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(membershipNode, "attributes");
          if (attributesNode != null) {
            membership.setRole(GrouperUtil.jsonJacksonGetString(attributesNode, "role"));
          }

          // userId from relationships.user.data.id
          JsonNode relationshipsNode = GrouperUtil.jsonJacksonGetNode(membershipNode, "relationships");
          if (relationshipsNode != null) {
            JsonNode userRelNode = GrouperUtil.jsonJacksonGetNode(relationshipsNode, "user");
            if (userRelNode != null) {
              JsonNode userDataNode = GrouperUtil.jsonJacksonGetNode(userRelNode, "data");
              if (userDataNode != null) {
                membership.setUserId(GrouperUtil.jsonJacksonGetString(userDataNode, "id"));
              }
            }
          }

          results.add(membership);
        }

        int returnedCount = dataArray == null ? 0 : dataArray.size();
        if (returnedCount < pageSize) {
          break;
        }

        pageNumber++;
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Add a user to a team via POST /api/v2/team/{teamId}/memberships.
   * The membership role defaults to "member".
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param teamId the UUID of the team
   * @param userId the UUID of the user to add
   */
  public static void addUserToTeam(String configId, DatadogSettings datadogSettings, String teamId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "addUserToTeam");

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(teamId)) {
        throw new RuntimeException("teamId is required");
      }
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is required");
      }

      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      ObjectNode dataNode = GrouperUtil.jsonJacksonNode();
      dataNode.put("type", "team_memberships");

      ObjectNode relationshipsNode = GrouperUtil.jsonJacksonNode();
      ObjectNode userRelNode = GrouperUtil.jsonJacksonNode();
      ObjectNode userDataNode = GrouperUtil.jsonJacksonNode();
      userDataNode.put("type", "users");
      userDataNode.put("id", userId);
      userRelNode.set("data", userDataNode);
      relationshipsNode.set("user", userRelNode);
      dataNode.set("relationships", relationshipsNode);

      rootNode.set("data", dataNode);

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      executeMethod(debugMap, "addUserToTeam", "POST", configId,
          "/api/v2/team/" + teamId + "/memberships",
          GrouperUtil.toSet(200), returnCode, jsonToSend, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Update a team membership role via PATCH /api/v2/team/{teamId}/memberships/{userId}.
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param teamId the UUID of the team
   * @param userId the UUID of the user
   * @param role the role to set ("admin"), or null/blank to remove admin privileges (Datadog API only supports "admin"; omit role to demote)
   */
  public static void updateTeamMembershipRole(String configId, DatadogSettings datadogSettings, String teamId, String userId, String role) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateTeamMembershipRole");

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(teamId)) {
        throw new RuntimeException("teamId is required");
      }
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is required");
      }
      if (StringUtils.isNotBlank(role) && !"admin".equals(role)) {
        throw new RuntimeException("role must be 'admin' or blank (to remove admin), was: '" + role + "'");
      }

      ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
      ObjectNode dataNode = GrouperUtil.jsonJacksonNode();
      dataNode.put("type", "team_memberships");

      ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();
      if (StringUtils.isNotBlank(role)) {
        attributesNode.put("role", role);
      }
      dataNode.set("attributes", attributesNode);

      rootNode.set("data", dataNode);

      String jsonToSend = GrouperUtil.jsonJacksonToString(rootNode);

      int[] returnCode = new int[] { -1 };

      executeMethod(debugMap, "updateTeamMembershipRole", "PATCH", configId,
          "/api/v2/team/" + teamId + "/memberships/" + userId,
          GrouperUtil.toSet(200), returnCode, jsonToSend, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

  /**
   * Remove a user from a team via DELETE /api/v2/team/{teamId}/memberships/{userId}.
   * Accepts 204 and 404 (already removed).
   * @param configId the id of the external system
   * @param datadogSettings the settings, or null to skip ignore checks
   * @param teamId the UUID of the team
   * @param userId the UUID of the user to remove
   */
  public static void removeUserFromTeam(String configId, DatadogSettings datadogSettings, String teamId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "removeUserFromTeam");

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(teamId)) {
        throw new RuntimeException("teamId is required");
      }
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is required");
      }

      int[] returnCode = new int[] { -1 };

      executeMethod(debugMap, "removeUserFromTeam", "DELETE", configId,
          "/api/v2/team/" + teamId + "/memberships/" + userId,
          GrouperUtil.toSet(204, 404), returnCode, null, null, false);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DatadogLog.datadogLog(debugMap, startTime);
    }
  }

}
