package edu.internet2.middleware.grouper.app.truefoundry;

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

public class TrueFoundryApiCommands {

  private static final int DEFAULT_PAGE_SIZE = 100;

  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  public static GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

  /**
   * Read the accessTokenPassword from the WsBearerToken external system config,
   * parse it as JSON containing {"apiToken": "...", "scimToken": "..."},
   * and return the requested token.
   * @param configId the external system config id
   * @param scim true to return the scimToken, false to return the apiToken
   * @return the token value
   */
  private static String getToken(String configId, boolean scim) {
    String accessTokenPassword = grouperLoaderConfig.propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");

    JsonNode keysNode = GrouperUtil.jsonJacksonNode(accessTokenPassword);
    String apiToken = GrouperUtil.jsonJacksonGetString(keysNode, "apiToken");
    String scimToken = GrouperUtil.jsonJacksonGetString(keysNode, "scimToken");

    if (StringUtils.isBlank(apiToken)) {
      throw new RuntimeException("apiToken is required in accessTokenPassword JSON for configId: " + configId);
    }

    if (scim) {
      if (StringUtils.isBlank(scimToken)) {
        throw new RuntimeException("scimToken is required in accessTokenPassword JSON for configId: " + configId
            + " (needed for SCIM display name updates)");
      }
      return scimToken;
    }
    return apiToken;
  }

  /**
   * Attach Bearer token authentication and standard headers to the HTTP client.
   * The accessTokenPassword is a JSON string with "apiToken" and "scimToken" fields.
   * @param grouperHttpClient the client to attach headers to
   * @param configId the external system config id
   * @param scim true to use the scimToken, false to use the apiToken
   */
  private static void attachAuthentication(GrouperHttpClient grouperHttpClient, String configId, boolean scim) {
    String token = getToken(configId, scim);
    grouperHttpClient.addHeader("Authorization", "Bearer " + token);
    grouperHttpClient.addHeader("Accept", "application/json");
  }

  /**
   * Execute an HTTP method against the TrueFoundry API using the native API token.
   */
  private static JsonNode executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes,
      int[] returnCode, String bodyParam) {
    return executeMethod(debugMap, debugLabel, httpMethodName, configId, urlSuffix,
        allowedReturnCodes, returnCode, bodyParam, false);
  }

  /**
   * Execute an HTTP method against the TrueFoundry API.
   * @param debugMap map to accumulate debug info
   * @param debugLabel label for stats tracking
   * @param httpMethodName GET, POST, PATCH, PUT, DELETE
   * @param configId external system config id
   * @param urlSuffix path after the base URL, or a full URL if it starts with "http"
   * @param allowedReturnCodes set of acceptable HTTP status codes
   * @param returnCode single-element array to receive the actual HTTP status code
   * @param bodyParam request body JSON string, or null
   * @param scim true to use the SCIM token, false to use the native API token
   * @return parsed JSON response, or null if body is blank
   */
  private static JsonNode executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes,
      int[] returnCode, String bodyParam, boolean scim) {

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders);
    attachAuthentication(grouperHttpClient, configId, scim);

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
      return GrouperUtil.jsonJacksonNode(json);
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }
  }

  // ============================
  // User methods
  // ============================

  /**
   * Container for all data returned by the subjects endpoint in a single call:
   * active users (paginated) and all teams with their members and managers.
   */
  public static class SubjectsData {
    /** All active, non-ignored users from the subjects endpoint. */
    public List<TrueFoundryUser> users = new ArrayList<TrueFoundryUser>();
    /** All teams returned by the subjects endpoint, deduplicated by ID. */
    public List<TrueFoundryGroup> teams = new ArrayList<TrueFoundryGroup>();
    /** Role memberships extracted from rolesWithResource on each user: roleId -> set of user emails. */
    public Map<String, Set<String>> roleMembershipsByRoleId = new LinkedHashMap<String, Set<String>>();
  }

  /**
   * Retrieve all subjects data in a single call: active users (paginated) AND all teams
   * (with their members and managers) from GET /api/svc/v1/subjects.
   * Teams are deduplicated by ID across pages.
   * @param configId the external system config id
   * @param settings TrueFoundry settings (uses ignoreUserEmails)
   * @return SubjectsData with users and teams lists
   */
  public static SubjectsData retrieveSubjectsData(String configId, TrueFoundrySettings settings) {

    Set<String> ignoreUserEmails = settings == null ? null : settings.getIgnoreUserEmails();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveSubjectsData");

    long startTime = System.nanoTime();

    SubjectsData subjectsData = new SubjectsData();
    Map<String, TrueFoundryGroup> teamsById = new LinkedHashMap<String, TrueFoundryGroup>();

    try {

      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", DEFAULT_PAGE_SIZE);
      int offset = 0;

      while (true) {

        String urlSuffix = "/api/svc/v1/subjects?query=&limit=" + pageSize + "&offset=" + offset + "&showInvalidUsers=true";

        JsonNode jsonNode = executeMethod(debugMap, "retrieveSubjectsData", "GET", configId, urlSuffix,
            GrouperUtil.toSet(200), new int[] { -1 }, null);

        if (jsonNode == null) {
          break;
        }

        // extract active users
        ArrayNode usersArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "users");
        int returnedCount = usersArray == null ? 0 : usersArray.size();

        for (int i = 0; i < returnedCount; i++) {
          JsonNode userNode = usersArray.get(i);
          TrueFoundryUser user = TrueFoundryUser.fromJson(userNode);
          if (user == null) {
            continue;
          }
          if (user.getActive() != null && !user.getActive()) {
            continue;
          }
          if (ignoreUserEmails != null && isIgnored(ignoreUserEmails, user.getEmail())) {
            continue;
          }
          subjectsData.users.add(user);

          // extract role memberships from rolesWithResource
          ArrayNode rolesWithResource = (ArrayNode) GrouperUtil.jsonJacksonGetNode(userNode, "rolesWithResource");
          if (rolesWithResource != null) {
            for (int j = 0; j < rolesWithResource.size(); j++) {
              JsonNode roleWithResource = rolesWithResource.get(j);
              String roleId = GrouperUtil.jsonJacksonGetString(roleWithResource, "roleId");
              if (StringUtils.isNotBlank(roleId)) {
                Set<String> emails = subjectsData.roleMembershipsByRoleId.get(roleId);
                if (emails == null) {
                  emails = new LinkedHashSet<String>();
                  subjectsData.roleMembershipsByRoleId.put(roleId, emails);
                }
                emails.add(user.getEmail());
              }
            }
          }
        }

        // extract teams (deduplicated by ID across pages — teams are included on every page)
        ArrayNode teamsArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "teams");
        if (teamsArray != null) {
          for (int i = 0; i < teamsArray.size(); i++) {
            TrueFoundryGroup team = TrueFoundryGroup.fromTeamJson(teamsArray.get(i));
            if (team != null && StringUtils.isNotBlank(team.getId())) {
              teamsById.put(team.getId(), team);
            }
          }
        }

        if (returnedCount < pageSize) {
          break;
        }
        offset += pageSize;
      }

      subjectsData.teams = new ArrayList<TrueFoundryGroup>(teamsById.values());

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }

    return subjectsData;
  }

  /**
   * Retrieve all users from TrueFoundry via the subjects endpoint.
   * Uses GET /api/svc/v1/subjects with limit/offset paging.
   * Filters to the users array only (excludes teams, virtualAccounts, externalIdentities).
   * @param configId the external system config id
   * @param includeInactiveUsers true to include deactivated users, false to filter them out
   * @param settings TrueFoundry settings (uses ignoreUserEmails)
   * @return list of TrueFoundryUser objects
   */
  public static List<TrueFoundryUser> retrieveUsers(String configId, boolean includeInactiveUsers,
      TrueFoundrySettings settings) {

    Set<String> ignoreUserEmails = settings == null ? null : settings.getIgnoreUserEmails();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveUsers");
    debugMap.put("includeInactiveUsers", includeInactiveUsers);

    long startTime = System.nanoTime();

    List<TrueFoundryUser> results = new ArrayList<TrueFoundryUser>();

    try {

      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", DEFAULT_PAGE_SIZE);
      int offset = 0;

      while (true) {

        String urlSuffix = "/api/svc/v1/subjects?query=&limit=" + pageSize + "&offset=" + offset + "&showInvalidUsers=true";

        JsonNode jsonNode = executeMethod(debugMap, "retrieveUsers", "GET", configId, urlSuffix,
            GrouperUtil.toSet(200), new int[] { -1 }, null);

        if (jsonNode == null) {
          break;
        }

        // this is a little wasteful since it does retrieve all teams and members, but thats ok
        ArrayNode usersArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "users");
        int returnedCount = usersArray == null ? 0 : usersArray.size();

        for (int i = 0; i < returnedCount; i++) {
          TrueFoundryUser user = TrueFoundryUser.fromJson(usersArray.get(i));
          if (user == null) {
            continue;
          }
          // filter out inactive users unless caller wants them
          if (!includeInactiveUsers && user.getActive() != null && !user.getActive()) {
            continue;
          }
          // filter out ignored emails
          if (ignoreUserEmails != null && isIgnored(ignoreUserEmails, user.getEmail())) {
            continue;
          }
          results.add(user);
        }

        if (returnedCount < pageSize) {
          break;
        }
        offset += pageSize;
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Search for a single user by email via the subjects endpoint.
   * Uses GET /api/svc/v1/subjects?query={email}.
   * Returns null if not found (totalUsers=0).
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param email the email to search for
   * @param includeInactiveUsers true to include deactivated users, false to filter them out
   * @return the matching TrueFoundryUser, or null if not found
   */
  public static TrueFoundryUser retrieveUserByEmail(String configId, TrueFoundrySettings settings,
      String email, boolean includeInactiveUsers) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveUserByEmail");
    debugMap.put("email", email);
    debugMap.put("includeInactiveUsers", includeInactiveUsers);

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "/api/svc/v1/subjects?query=" + GrouperUtil.escapeUrlEncode(email)
          + "&limit=25&offset=0&showInvalidUsers=true";

      JsonNode jsonNode = executeMethod(debugMap, "retrieveUserByEmail", "GET", configId, urlSuffix,
          GrouperUtil.toSet(200), new int[] { -1 }, null);

      if (jsonNode == null) {
        return null;
      }

      Integer totalUsers = GrouperUtil.jsonJacksonGetInteger(jsonNode, "totalUsers");
      if (totalUsers == null || totalUsers == 0) {
        return null;
      }

      ArrayNode usersArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "users");
      if (usersArray == null || usersArray.size() == 0) {
        return null;
      }

      // find exact email match
      for (int i = 0; i < usersArray.size(); i++) {
        TrueFoundryUser user = TrueFoundryUser.fromJson(usersArray.get(i));
        if (user == null) {
          continue;
        }
        if (!StringUtils.equalsIgnoreCase(email, user.getEmail())) {
          continue;
        }
        // filter out inactive users unless caller wants them
        if (!includeInactiveUsers && user.getActive() != null && !user.getActive()) {
          continue;
        }
        return user;
      }

      return null;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  /**
   * Create a user in TrueFoundry.  If the user already exists (including inactive),
   * reactivate and return them.  If they don't exist, register a new user and
   * retrieve by email to get the assigned ID.
   * If the user has a displayName and SCIM is configured (tenantName and ssoId are not blank),
   * the display name is set via SCIM PATCH after creation.
   * @param configId the external system config id
   * @param settings TrueFoundry settings (uses tenantName, ssoId for SCIM display name, defaultTeamMemberEmail for delete cleanup)
   * @param user the user to create (must have email set; displayName is optional)
   * @return the created or reactivated TrueFoundryUser with id populated
   */
  public static TrueFoundryUser createUser(String configId, TrueFoundrySettings settings,
      TrueFoundryUser user) {

    String tenantName = settings == null ? null : settings.getTenantName();
    String ssoId = settings == null ? null : settings.getSsoId();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createUser");
    debugMap.put("email", user == null ? null : user.getEmail());

    long startTime = System.nanoTime();

    try {

      if (user == null || StringUtils.isBlank(user.getEmail())) {
        throw new RuntimeException("user email is required for createUser");
      }

      String email = user.getEmail();

      // check if user already exists (including inactive)
      TrueFoundryUser existingUser = retrieveUserByEmail(configId, settings, email, true);

      if (existingUser != null) {
        debugMap.put("existingUserFound", true);

        // reactivate if inactive
        if (existingUser.getActive() != null && !existingUser.getActive()) {
          debugMap.put("reActivatingUser", true);
          boolean activated = activateUser(configId, settings, email);
          if (activated) {
            existingUser.setActive(true);
            return existingUser;
          }
          // 404 means the user was hard-deleted in TrueFoundry despite still
          // appearing in the subjects search.  Delete the stale record and re-register.
          // deleteUser removes the user from all teams first so the delete can succeed.
          debugMap.put("activateUser404", "user was hard-deleted, deleting and re-registering");
          if (StringUtils.isNotBlank(existingUser.getId())) {
            deleteUser(configId, settings, existingUser.getId(), email);
          }
        } else {
          return existingUser;
        }
      }

      // no existing user (or hard-deleted), register new
      registerUser(configId, user);

      // look up by email to get the assigned TrueFoundry user ID
      TrueFoundryUser createdUser = retrieveUserByEmail(configId, settings, email, true);

      // set display name via SCIM if configured and displayName is provided
      if (createdUser != null && !StringUtils.isBlank(user.getDisplayName())
          && !StringUtils.isBlank(tenantName) && !StringUtils.isBlank(ssoId)) {
        updateUserDisplayName(configId, settings, createdUser.getId(), user.getDisplayName());
        createdUser.setDisplayName(user.getDisplayName());
      }

      return createdUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  /**
   * Register a new user in TrueFoundry via POST /api/svc/v1/users/register.
   * Returns 200 {} — no user ID is in the response.
   * Callers should use createUser instead, which handles existing/inactive users.
   * @param configId the external system config id
   * @param user the user to register (must have email set)
   */
  private static void registerUser(String configId, TrueFoundryUser user) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "registerUser");
    debugMap.put("email", user == null ? null : user.getEmail());

    long startTime = System.nanoTime();

    try {

      if (user == null || StringUtils.isBlank(user.getEmail())) {
        throw new RuntimeException("user email is required for registerUser");
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put("email", user.getEmail());
      body.put("sendInviteEmail", false);

      executeMethod(debugMap, "registerUser", "POST", configId, "/api/svc/v1/users/register",
          GrouperUtil.toSet(200, 201), new int[] { -1 }, GrouperUtil.jsonJacksonToString(body));

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  /**
   * Deactivate a user in TrueFoundry via PATCH /api/svc/v1/users/deactivate.
   * Sets active=false on the user. Use activateUser to re-enable.
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param email the email of the user to deactivate
   */
  public static void deactivateUser(String configId, TrueFoundrySettings settings, String email) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "deactivateUser");
    debugMap.put("email", email);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("email is required for deactivateUser");
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put("email", email);

      executeMethod(debugMap, "deactivateUser", "PATCH", configId, "/api/svc/v1/users/deactivate",
          GrouperUtil.toSet(200), new int[] { -1 }, GrouperUtil.jsonJacksonToString(body));

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  /**
   * Activate (re-enable) a previously deactivated user via PATCH /api/svc/v1/users/activate.
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param email the email of the user to activate
   * @return true if activated successfully, false if user not found (404)
   */
  public static boolean activateUser(String configId, TrueFoundrySettings settings, String email) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "activateUser");
    debugMap.put("email", email);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("email is required for activateUser");
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put("email", email);

      int[] returnCode = new int[] { -1 };
      executeMethod(debugMap, "activateUser", "PATCH", configId, "/api/svc/v1/users/activate",
          GrouperUtil.toSet(200, 404), returnCode, GrouperUtil.jsonJacksonToString(body));

      return returnCode[0] != 404;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  /**
   * Update the display name of a natively-registered TrueFoundry user via SCIM PATCH.
   * URL: PATCH /api/svc/v1/scim/v2/{tenantName}/{ssoId}/Users/{id}
   * Only call this if both tenantName and ssoId are configured.
   * Do NOT use SCIM to create users — only PATCH existing natively-registered users.
   * @param configId the external system config id
   * @param settings TrueFoundry settings (uses tenantName and ssoId)
   * @param id the native TrueFoundry user ID (not email) used in the SCIM URL
   * @param displayName the new display name to set
   */
  public static void updateUserDisplayName(String configId, TrueFoundrySettings settings,
      String id, String displayName) {

    String tenantName = settings == null ? null : settings.getTenantName();
    String ssoId = settings == null ? null : settings.getSsoId();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateUserDisplayName");
    debugMap.put("id", id);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is required for updateUserDisplayName");
      }
      if (StringUtils.isBlank(tenantName) || StringUtils.isBlank(ssoId)) {
        throw new RuntimeException("tenantName and ssoId are required for updateUserDisplayName");
      }

      TrueFoundryUser user = new TrueFoundryUser();
      user.setId(id);
      user.setDisplayName(displayName);

      ObjectNode body = user.toScimPatchDisplayNameJson();

      String urlSuffix = "/api/svc/v1/scim/v2/" + tenantName + "/" + ssoId + "/Users/" + id;

      executeMethod(debugMap, "updateUserDisplayName", "PATCH", configId, urlSuffix,
          GrouperUtil.toSet(200), new int[] { -1 }, GrouperUtil.jsonJacksonToString(body), true);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  /**
   * The team-manager role is managed internally by TrueFoundry and must not be
   * assigned via the provisioner.
   */
  public static final String ROLE_NAME_TEAM_MANAGER = "team-manager";

  /**
   * The tenant-admin role is a system role that can be assigned to users
   * but cannot be created, updated, or deleted via the provisioner.
   */
  public static final String ROLE_NAME_TENANT_ADMIN = "tenant-admin";

  /**
   * The "everyone" team is managed internally by TrueFoundry and must not be
   * created, updated, or deleted via the provisioner.
   */
  public static final String TEAM_NAME_EVERYONE = "everyone";

  /**
   * Assign a role to a user via PATCH /api/svc/v1/users/roles.
   * resourceType is derived from the role name: "tenant-admin" uses "tenant", all others use "account".
   * The "team-manager" role is system-managed and cannot be assigned via the provisioner.
   * Note: role assignment does not work for SCIM-created users (isEditable=false).
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param email the email of the user to assign the role to
   * @param roleName the role name (e.g. "member", "read-only-member", "tenant-admin", or a custom role name)
   */
  public static void assignUserRole(String configId, TrueFoundrySettings settings, String email, String roleName) {

    if (ROLE_NAME_TEAM_MANAGER.equals(roleName)) {
      throw new RuntimeException("The 'team-manager' role is managed internally by TrueFoundry "
          + "and cannot be assigned via the provisioner. Do not create a Grouper group for this role.");
    }

    String resourceType = ROLE_NAME_TENANT_ADMIN.equals(roleName) ? "tenant" : "account";

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "assignUserRole");
    debugMap.put("email", email);
    debugMap.put("roleName", roleName);
    debugMap.put("resourceType", resourceType);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("email is required for assignUserRole");
      }
      if (StringUtils.isBlank(roleName)) {
        throw new RuntimeException("roleName is required for assignUserRole");
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put("email", email);
      ArrayNode rolesArray = GrouperUtil.jsonJacksonArrayNode();
      rolesArray.add(roleName);
      body.set("roles", rolesArray);
      body.put("resourceType", resourceType);

      executeMethod(debugMap, "assignUserRole", "PATCH", configId, "/api/svc/v1/users/roles",
          GrouperUtil.toSet(200), new int[] { -1 }, GrouperUtil.jsonJacksonToString(body));

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  // ============================
  // Role methods
  // ============================

  /**
   * Retrieve all roles from TrueFoundry via GET /api/svc/v1/role/list.
   * No paging — all roles returned in one call.
   * Filters to roles with resourceType "account" or "tenant" (provisioner-managed).
   * @param configId the external system config id
   * @param settings TrueFoundry settings (uses ignoreRoleNames)
   * @return list of TrueFoundryGroup objects with groupType=role
   */
  public static List<TrueFoundryGroup> retrieveRoles(String configId, TrueFoundrySettings settings) {

    Set<String> ignoreRoleNames = settings == null ? null : settings.getIgnoreRoleNames();

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveRoles");

    long startTime = System.nanoTime();

    List<TrueFoundryGroup> results = new ArrayList<TrueFoundryGroup>();

    try {

      JsonNode jsonNode = executeMethod(debugMap, "retrieveRoles", "GET", configId,
          "/api/svc/v1/role/list", GrouperUtil.toSet(200), new int[] { -1 }, null);

      if (jsonNode == null) {
        return results;
      }

      ArrayNode dataArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "data");
      if (dataArray == null) {
        return results;
      }

      for (int i = 0; i < dataArray.size(); i++) {
        JsonNode roleNode = dataArray.get(i);
        TrueFoundryGroup role = TrueFoundryGroup.fromRoleJson(roleNode);
        if (role == null) {
          continue;
        }
        // only manage account and tenant scoped roles
        String resourceType = role.getResourceType();
        if (!"account".equals(resourceType) && !"tenant".equals(resourceType)) {
          continue;
        }
        // team-manager is system-managed and must not be provisioned
        if (ROLE_NAME_TEAM_MANAGER.equals(role.getName())) {
          continue;
        }
        // filter out ignored role names
        if (ignoreRoleNames != null && isIgnored(ignoreRoleNames, role.getName())) {
          continue;
        }
        results.add(role);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Create or update a custom role via PUT /api/svc/v1/role (upsert by name).
   * All manifest fields are required: name, displayName, resourceType, description, permissions, type.
   * Returns the created/updated role with its assigned ID.
   * Note: roles should be managed by administrators in the TrueFoundry UI; role create/update
   * is provided for completeness but is generally not invoked by the provisioner.
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param role the role to create or update
   * @return the created/updated TrueFoundryGroup with assigned id
   */
  public static TrueFoundryGroup createOrUpdateRole(String configId, TrueFoundrySettings settings, TrueFoundryGroup role) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createOrUpdateRole");
    debugMap.put("roleName", role == null ? null : role.getName());

    long startTime = System.nanoTime();

    try {

      if (role == null || StringUtils.isBlank(role.getName())) {
        throw new RuntimeException("role name is required for createOrUpdateRole");
      }
      assertNotSystemRole(role.getName());
      if (StringUtils.isNotBlank(role.getId())) {
        assertNotSystemRoleById(configId, role.getId());
      }
      validateTrueFoundryName(role.getName(), "role name");

      ObjectNode body;
      if (!StringUtils.isBlank(role.getId())) {
        // role already exists — fetch the current manifest from the list so we preserve
        // admin-configured permissions and any other fields we don't manage (upsert pattern)
        JsonNode listJson = executeMethod(debugMap, "createOrUpdateRole_list", "GET", configId,
            "/api/svc/v1/role/list", GrouperUtil.toSet(200), new int[] { -1 }, null);
        JsonNode currentRoleNode = null;
        if (listJson != null) {
          ArrayNode dataArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(listJson, "data");
          if (dataArray != null) {
            for (int i = 0; i < dataArray.size(); i++) {
              JsonNode candidate = dataArray.get(i);
              if (role.getId().equals(GrouperUtil.jsonJacksonGetString(candidate, "id"))) {
                currentRoleNode = candidate;
                break;
              }
            }
          }
        }
        if (currentRoleNode != null) {
          // start from the current server manifest and overlay only the fields we manage
          body = GrouperUtil.jsonJacksonNode();
          JsonNode currentManifest = GrouperUtil.jsonJacksonGetNode(currentRoleNode, "manifest");
          ObjectNode manifest = currentManifest instanceof ObjectNode
              ? (ObjectNode) currentManifest.deepCopy() : GrouperUtil.jsonJacksonNode();
          manifest.put("name", role.getName());
          if (!GrouperUtil.isBlank(role.getDisplayName())) {
            manifest.put("displayName", role.getDisplayName());
          }
          if (!GrouperUtil.isBlank(role.getDescription())) {
            manifest.put("description", role.getDescription());
          }
          body.set("manifest", manifest);
        } else {
          // role not found in list — fall back to create-from-scratch
          body = role.toRoleJson(null);
        }
      } else {
        // no id — new role, build from scratch
        body = role.toRoleJson(null);
      }

      JsonNode responseNode = executeMethod(debugMap, "createOrUpdateRole", "PUT", configId,
          "/api/svc/v1/role", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

      if (responseNode == null) {
        throw new RuntimeException("No response body from createOrUpdateRole for role: " + role.getName());
      }

      JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(responseNode, "data");
      return TrueFoundryGroup.fromRoleJson(dataNode != null ? dataNode : responseNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  /**
   * Delete a role by its ID via DELETE /api/svc/v1/role/{id}.
   * Returns 200 on success. 404 is treated as non-fatal (already deleted).
   * Note: roles should be managed by administrators in the TrueFoundry UI; role delete
   * is provided for completeness but is generally not invoked by the provisioner.
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param roleId the TrueFoundry role ID
   */
  public static void deleteRole(String configId, TrueFoundrySettings settings, String roleId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "deleteRole");
    debugMap.put("roleId", roleId);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(roleId)) {
        throw new RuntimeException("roleId is required for deleteRole");
      }

      assertNotSystemRoleById(configId, roleId);

      executeMethod(debugMap, "deleteRole", "DELETE", configId, "/api/svc/v1/role/" + roleId,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  // ============================
  // Team methods
  // ============================

  /**
   * Retrieve all teams from TrueFoundry via GET /api/svc/v1/teams/user.
   * Uses limit/offset paging. pagination.total used to detect end of pages.
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @return list of TrueFoundryGroup objects with groupType=team
   */
  public static List<TrueFoundryGroup> retrieveTeams(String configId, TrueFoundrySettings settings) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveTeams");

    long startTime = System.nanoTime();

    List<TrueFoundryGroup> results = new ArrayList<TrueFoundryGroup>();

    try {

      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", DEFAULT_PAGE_SIZE);
      int offset = 0;

      while (true) {

        String urlSuffix = "/api/svc/v1/teams/user?limit=" + pageSize + "&offset=" + offset;

        JsonNode jsonNode = executeMethod(debugMap, "retrieveTeams", "GET", configId, urlSuffix,
            GrouperUtil.toSet(200), new int[] { -1 }, null);

        if (jsonNode == null) {
          break;
        }

        ArrayNode dataArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "data");
        int returnedCount = dataArray == null ? 0 : dataArray.size();

        for (int i = 0; i < returnedCount; i++) {
          TrueFoundryGroup team = TrueFoundryGroup.fromTeamJson(dataArray.get(i));
          if (team == null) {
            continue;
          }
          // "everyone" team is system-managed and must not be provisioned
          if (TEAM_NAME_EVERYONE.equals(team.getName())) {
            continue;
          }
          results.add(team);
        }

        if (returnedCount < pageSize) {
          break;
        }
        offset += pageSize;
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Get a single team by its ID via GET /api/svc/v1/teams/{id}.
   * Returns null if the team is not found (404).
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param teamId the TrueFoundry team ID
   * @return the TrueFoundryGroup, or null if not found
   */
  public static TrueFoundryGroup getTeamById(String configId, TrueFoundrySettings settings, String teamId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "getTeamById");
    debugMap.put("teamId", teamId);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(teamId)) {
        throw new RuntimeException("teamId is required for getTeamById");
      }

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "getTeamById", "GET", configId,
          "/api/svc/v1/teams/" + teamId, GrouperUtil.toSet(200, 404), returnCode, null);

      if (returnCode[0] == 404 || jsonNode == null) {
        return null;
      }

      JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "data");
      return TrueFoundryGroup.fromTeamJson(dataNode != null ? dataNode : jsonNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  /**
   * Create a new team.  TrueFoundry requires at least one member per team at all times.
   * If defaultTeamMemberEmailAddress is provided it is added as the initial member.
   * Memberships are added afterward via addTeamMembers.
   * @param configId the external system config id
   * @param settings TrueFoundry settings (uses defaultTeamMemberEmail)
   * @param team the team (must have name set)
   * @return the created TrueFoundryGroup with assigned id, or null if 409
   */
  public static TrueFoundryGroup createTeam(String configId, TrueFoundrySettings settings,
      TrueFoundryGroup team) {

    String defaultTeamMemberEmailAddress = settings == null ? null : settings.getDefaultTeamMemberEmail();
    assertNotEveryoneTeam(team);
    List<String> initialMembers = null;
    if (!StringUtils.isBlank(defaultTeamMemberEmailAddress)) {
      initialMembers = new ArrayList<String>();
      initialMembers.add(defaultTeamMemberEmailAddress);
    }
    return createOrUpdateTeam(configId, team, initialMembers, null);
  }

  /**
   * Update group-level fields on an existing team (e.g. name) while preserving
   * the current member and manager lists.
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param team the team with updated fields (must have id and name set)
   * @return the updated TrueFoundryGroup, or null if 409
   */
  public static TrueFoundryGroup updateTeam(String configId, TrueFoundrySettings settings, TrueFoundryGroup team) {
    assertNotEveryoneTeam(team);
    TrueFoundryGroup currentTeam = getTeamById(configId, settings, team.getId());
    List<String> currentMembers = currentTeam != null ? currentTeam.getMembers() : null;
    List<String> currentManagers = currentTeam != null ? currentTeam.getManagers() : null;
    return createOrUpdateTeam(configId, team, currentMembers, currentManagers);
  }

  /**
   * Add multiple members to a team in a single PUT call.
   * Retrieves the current team state, applies all additions at once, then PUTs the full manifest.
   * Regular members are added to the members list; managers are added to both lists.
   * If an email appears in regularMemberEmails and was previously a manager, it is demoted.
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param teamId the team id
   * @param managerEmails emails to add as team managers (also added to members list)
   * @param regularMemberEmails emails to add as regular members (removed from managers if present)
   * @return the updated TrueFoundryGroup, or null if 409
   */
  public static TrueFoundryGroup addTeamMembers(String configId, TrueFoundrySettings settings,
      String teamId, List<String> managerEmails, List<String> regularMemberEmails) {

    TrueFoundryGroup currentTeam = getTeamById(configId, settings, teamId);
    if (currentTeam == null) {
      throw new RuntimeException("Team not found for addTeamMembers, teamId: " + teamId);
    }

    List<String> members = new ArrayList<String>(GrouperUtil.nonNull(currentTeam.getMembers()));
    List<String> managers = new ArrayList<String>(GrouperUtil.nonNull(currentTeam.getManagers()));

    for (String email : GrouperUtil.nonNull(regularMemberEmails)) {
      if (!members.contains(email)) {
        members.add(email);
      }
      managers.remove(email);
    }

    for (String email : GrouperUtil.nonNull(managerEmails)) {
      if (!members.contains(email)) {
        members.add(email);
      }
      if (!managers.contains(email)) {
        managers.add(email);
      }
    }

    return createOrUpdateTeam(configId, currentTeam, members,
        managers.isEmpty() ? null : managers);
  }

  /**
   * Remove multiple members from a team in a single PUT call.
   * Retrieves the current team state, removes all specified emails from both the members
   * and managers lists, then PUTs the full manifest.
   * TrueFoundry requires at least one member per team at all times.  If removing all members
   * would leave the team empty, the defaultTeamMemberEmailAddress is kept (or re-added) so the
   * team always has at least one member.  The service team is responsible for keeping at least
   * one real person in each team; the default member is a safety net only.
   * @param configId the external system config id
   * @param settings TrueFoundry settings (uses defaultTeamMemberEmail)
   * @param teamId the team id
   * @param emailsToRemove emails to remove from both members and managers lists
   * @return the updated TrueFoundryGroup, or null if team not found
   */
  public static TrueFoundryGroup removeTeamMembers(String configId, TrueFoundrySettings settings,
      String teamId, List<String> emailsToRemove) {

    String defaultTeamMemberEmailAddress = settings == null ? null : settings.getDefaultTeamMemberEmail();

    TrueFoundryGroup currentTeam = getTeamById(configId, settings, teamId);
    if (currentTeam == null) {
      // team already gone — memberships effectively deleted
      return null;
    }

    List<String> members = new ArrayList<String>(GrouperUtil.nonNull(currentTeam.getMembers()));
    List<String> managers = new ArrayList<String>(GrouperUtil.nonNull(currentTeam.getManagers()));

    for (String email : GrouperUtil.nonNull(emailsToRemove)) {
      members.remove(email);
      managers.remove(email);
    }

    // TrueFoundry requires at least one member per team — keep the default if the list is empty
    if (members.isEmpty() && !StringUtils.isBlank(defaultTeamMemberEmailAddress)) {
      members.add(defaultTeamMemberEmailAddress);
    }

    return createOrUpdateTeam(configId, currentTeam, members,
        managers.isEmpty() ? null : managers);
  }

  /**
   * Replace the full member and manager lists on a team with a single PUT.
   * Used by the provisioner's replaceGroupMemberships path (replaceMemberships=true on full sync).
   * The team must already exist. TrueFoundry requires at least one member per team, so the
   * defaultTeamMemberEmailAddress is added if the resulting member list would be empty.
   * Managers that are not also in memberEmails are dropped (TrueFoundry requires managers to
   * be members).
   * @param configId the external system config id
   * @param settings TrueFoundry settings (uses defaultTeamMemberEmail)
   * @param teamId the team id
   * @param memberEmails the full list of desired member emails (managers are typically included too)
   * @param managerEmails the full list of desired manager emails (may be null or empty)
   * @return the updated TrueFoundryGroup, or null if team not found
   */
  public static TrueFoundryGroup replaceTeamMembers(String configId, TrueFoundrySettings settings,
      String teamId, List<String> memberEmails, List<String> managerEmails) {

    String defaultTeamMemberEmailAddress = settings == null ? null : settings.getDefaultTeamMemberEmail();

    TrueFoundryGroup currentTeam = getTeamById(configId, settings, teamId);
    if (currentTeam == null) {
      throw new RuntimeException("Team not found for replaceTeamMembers, teamId: " + teamId);
    }

    List<String> members = new ArrayList<String>();
    if (memberEmails != null) {
      for (String email : memberEmails) {
        if (!StringUtils.isBlank(email) && !members.contains(email)) {
          members.add(email);
        }
      }
    }

    // TrueFoundry requires at least one member per team — keep the default if the list is empty
    if (members.isEmpty() && !StringUtils.isBlank(defaultTeamMemberEmailAddress)) {
      members.add(defaultTeamMemberEmailAddress);
    }

    List<String> managers = new ArrayList<String>();
    if (managerEmails != null) {
      for (String email : managerEmails) {
        // managers must also be members
        if (!StringUtils.isBlank(email) && members.contains(email) && !managers.contains(email)) {
          managers.add(email);
        }
      }
    }

    return createOrUpdateTeam(configId, currentTeam, members,
        managers.isEmpty() ? null : managers);
  }

  /**
   * Low-level PUT /api/svc/v1/teams — sends the full manifest with members and managers.
   * Callers should use createTeam, updateTeam, addTeamMembers, or removeTeamMembers instead.
   * @param configId the external system config id
   * @param team the team (must have name set)
   * @param memberEmails the full list of member emails (at least one required by the API)
   * @param managerEmails the full list of manager emails (may be null or empty)
   * @return the created/updated TrueFoundryGroup, or null if 409 (everyone team conflict)
   */
  private static TrueFoundryGroup createOrUpdateTeam(String configId, TrueFoundryGroup team,
      List<String> memberEmails, List<String> managerEmails) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createOrUpdateTeam");
    debugMap.put("teamName", team == null ? null : team.getName());
    debugMap.put("memberCount", memberEmails == null ? 0 : memberEmails.size());
    debugMap.put("managerCount", managerEmails == null ? 0 : managerEmails.size());

    long startTime = System.nanoTime();

    try {

      if (team == null || StringUtils.isBlank(team.getName())) {
        throw new RuntimeException("team name is required for createOrUpdateTeam");
      }
      validateTrueFoundryName(team.getName(), "team name");

      ObjectNode body;
      if (!StringUtils.isBlank(team.getId())) {
        // team already exists — retrieve the full current manifest and overlay our fields
        // so any server-side manifest fields we don't know about are preserved (upsert pattern)
        int[] getReturnCode = new int[] { -1 };
        JsonNode currentJson = executeMethod(debugMap, "createOrUpdateTeam_get", "GET", configId,
            "/api/svc/v1/teams/" + team.getId(), GrouperUtil.toSet(200, 404), getReturnCode, null);
        JsonNode currentTeamNode = null;
        if (getReturnCode[0] == 200 && currentJson != null) {
          JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(currentJson, "data");
          currentTeamNode = dataNode != null ? dataNode : currentJson;
        }
        if (currentTeamNode != null) {
          body = (ObjectNode) currentTeamNode.deepCopy();
          body.put("teamName", team.getName());
          JsonNode manifestNode = GrouperUtil.jsonJacksonGetNode(body, "manifest");
          ObjectNode manifest = manifestNode instanceof ObjectNode
              ? (ObjectNode) manifestNode : GrouperUtil.jsonJacksonNode();
          manifest.put("name", team.getName());
          ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
          for (String email : GrouperUtil.nonNull(memberEmails)) {
            membersArray.add(email);
          }
          manifest.set("members", membersArray);
          if (managerEmails != null && !managerEmails.isEmpty()) {
            ArrayNode managersArray = GrouperUtil.jsonJacksonArrayNode();
            for (String email : managerEmails) {
              managersArray.add(email);
            }
            manifest.set("managers", managersArray);
          } else {
            manifest.remove("managers");
          }
          body.set("manifest", manifest);
        } else {
          // team not found on server — fall back to create
          body = team.toTeamJson(memberEmails, managerEmails);
        }
      } else {
        // no id — new team, build from scratch
        body = team.toTeamJson(memberEmails, managerEmails);
      }

      int[] returnCode = new int[] { -1 };
      JsonNode responseNode = executeMethod(debugMap, "createOrUpdateTeam", "PUT", configId,
          "/api/svc/v1/teams", GrouperUtil.toSet(200, 201, 409), returnCode,
          GrouperUtil.jsonJacksonToString(body));

      // 409 = conflict on the default "everyone" team — treat as non-fatal
      if (returnCode[0] == 409) {
        debugMap.put("conflict409", "everyone team conflict, treating as non-fatal");
        return null;
      }

      if (responseNode == null) {
        return null;
      }

      JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(responseNode, "data");
      return TrueFoundryGroup.fromTeamJson(dataNode != null ? dataNode : responseNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  /**
   * Delete a team by its ID via DELETE /api/svc/v1/teams/{id}.
   * Retrieves the team first to guard against deleting the system-managed "everyone" team.
   * Returns 200 on success.
   * Returns 404 if not found — treated as non-fatal (already deleted).
   * @param configId the external system config id
   * @param settings TrueFoundry settings
   * @param teamId the TrueFoundry team ID
   */
  public static void deleteTeam(String configId, TrueFoundrySettings settings, String teamId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "deleteTeam");
    debugMap.put("teamId", teamId);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(teamId)) {
        throw new RuntimeException("teamId is required for deleteTeam");
      }

      TrueFoundryGroup team = getTeamById(configId, settings, teamId);
      if (team == null) {
        debugMap.put("notFound", "team not found, treating as already deleted");
        return;
      }
      assertNotEveryoneTeam(team);

      int[] returnCode = new int[] { -1 };
      executeMethod(debugMap, "deleteTeam", "DELETE", configId, "/api/svc/v1/teams/" + teamId,
          GrouperUtil.toSet(200, 404), returnCode, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  // ============================
  // Delete user (for main/testing only)
  // ============================

  /**
   * Delete a user by their internal TrueFoundry user ID via DELETE /api/svc/v1/users/{userId}.
   * TrueFoundry blocks deletion when the user has team memberships, so this method
   * first retrieves the user's teams via GET /subjects?query={email} and removes the
   * user from all teams before issuing the DELETE.
   * Returns 200 on success. 404 is treated as non-fatal (already deleted).
   * @param configId the external system config id
   * @param settings TrueFoundry settings (uses defaultTeamMemberEmail for team cleanup)
   * @param userId the internal TrueFoundry user ID (not email)
   * @param email the user's email address, used to find and remove team memberships
   *              before deleting; if null, skip team removal
   */
  private static void deleteUser(String configId, TrueFoundrySettings settings,
      String userId, String email) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "deleteUser");
    debugMap.put("userId", userId);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is required for deleteUser");
      }

      // remove user from all teams so the hard delete can succeed
      if (StringUtils.isNotBlank(email)) {
        String urlSuffix = "/api/svc/v1/subjects?query=" + GrouperUtil.escapeUrlEncode(email)
            + "&limit=25&offset=0&showInvalidUsers=true";
        JsonNode jsonNode = executeMethod(debugMap, "deleteUser_getSubjects", "GET", configId,
            urlSuffix, GrouperUtil.toSet(200), new int[] { -1 }, null);

        if (jsonNode != null) {
          ArrayNode teamsArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "teams");
          if (teamsArray != null) {
            List<String> emailsToRemove = new ArrayList<String>();
            emailsToRemove.add(email);
            int teamsRemoved = 0;
            for (int i = 0; i < teamsArray.size(); i++) {
              TrueFoundryGroup team = TrueFoundryGroup.fromTeamJson(teamsArray.get(i));
              if (team == null || StringUtils.isBlank(team.getId())) {
                continue;
              }
              boolean isMember = GrouperUtil.nonNull(team.getMembers()).contains(email);
              boolean isManager = GrouperUtil.nonNull(team.getManagers()).contains(email);
              if (isMember || isManager) {
                removeTeamMembers(configId, settings, team.getId(), emailsToRemove);
                teamsRemoved++;
              }
            }
            debugMap.put("teamsRemovedFrom", teamsRemoved);
          }
        }
      }

      executeMethod(debugMap, "deleteUser", "DELETE", configId, "/api/svc/v1/users/" + userId,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      TrueFoundryLog.trueFoundryLog(debugMap, startTime);
    }
  }

  // ============================
  // Main (manual integration test against real TrueFoundry target)
  // ============================

  /**
   * Manual integration test that exercises every command against a real TrueFoundry instance.
   * Requires grouper-loader.properties configured with a "trueFoundryDev" external system entry.
   * Creates a test user and team, runs all operations, then deletes them for cleanup.
   */
  public static void main(String[] args) {

    GrouperStartup.startup();

    String configId = "trueFoundryProd";
    String testUserEmail = "pgtest1@upenn.edu";
    String testUserDisplayName = "PG Test User 1";
    String testTeamName = "test-team-" + System.currentTimeMillis();
    String testRoleName = "test-role-" + System.currentTimeMillis();

    TrueFoundrySettings settings = new TrueFoundrySettings();
    settings.setTenantName("upenn-prod");
    settings.setSsoId("cmn51z1g108e401py6ct1fd2o");
    settings.setDefaultTeamMemberEmail("mchyzer@upenn.edu");

    try {

      // clean up any pre-existing test data from a previous failed run
      TrueFoundryUser existingTestUser = retrieveUserByEmail(configId, settings, testUserEmail, true);
      if (existingTestUser != null && StringUtils.isNotBlank(existingTestUser.getId())) {
        deleteUser(configId, settings, existingTestUser.getId(), testUserEmail);
        System.out.println("Cleaned up pre-existing test user: " + testUserEmail);
      }

      // ============================
      // User operations
      // ============================

      System.out.println("\n====== RETRIEVE USERS ======");
      List<TrueFoundryUser> allUsers = retrieveUsers(configId, false, settings);
      System.out.println("Total active users: " + GrouperUtil.length(allUsers));

      System.out.println("\n====== CREATE USER (with display name) ======");
      TrueFoundryUser newUser = new TrueFoundryUser();
      newUser.setEmail(testUserEmail);
      newUser.setDisplayName(testUserDisplayName);
      TrueFoundryUser createdUser = createUser(configId, settings, newUser);
      System.out.println("Created user: " + createdUser);
      GrouperUtil.assertion(createdUser != null, "Created user should not be null");
      GrouperUtil.assertion(StringUtils.isNotBlank(createdUser.getId()), "Created user should have an id");
      GrouperUtil.assertion(StringUtils.equalsIgnoreCase(testUserEmail, createdUser.getEmail()),
          "Created user email should match");
      String userId = createdUser.getId();
      System.out.println("Created user id: " + userId);

      System.out.println("\n====== RETRIEVE USERS (verify created user appears) ======");
      List<TrueFoundryUser> usersAfterCreate = retrieveUsers(configId, false, settings);
      boolean foundInAll = false;
      for (TrueFoundryUser u : GrouperUtil.nonNull(usersAfterCreate)) {
        if (StringUtils.equalsIgnoreCase(testUserEmail, u.getEmail())) {
          foundInAll = true;
          break;
        }
      }
      GrouperUtil.assertion(foundInAll, "Created user should appear in retrieveUsers list");
      System.out.println("Found created user in retrieveUsers: true");

      System.out.println("\n====== RETRIEVE USER BY EMAIL (active) ======");
      TrueFoundryUser foundUser = retrieveUserByEmail(configId, settings, testUserEmail, false);
      GrouperUtil.assertion(foundUser != null, "Should find user by email (active only)");
      GrouperUtil.assertion(StringUtils.equals(userId, foundUser.getId()), "Found user id should match");
      System.out.println("Found active user: " + foundUser);

      System.out.println("\n====== DEACTIVATE USER ======");
      deactivateUser(configId, settings, testUserEmail);
      System.out.println("Deactivated user: " + testUserEmail);

      System.out.println("\n====== RETRIEVE USER BY EMAIL (active only, should not find) ======");
      TrueFoundryUser shouldBeNull = retrieveUserByEmail(configId, settings, testUserEmail, false);
      GrouperUtil.assertion(shouldBeNull == null,
          "Should NOT find deactivated user with includeInactiveUsers=false");
      System.out.println("Verified deactivated user not found with includeInactiveUsers=false: true");

      System.out.println("\n====== RETRIEVE USER BY EMAIL (include inactive) ======");
      TrueFoundryUser inactiveUser = retrieveUserByEmail(configId, settings, testUserEmail, true);
      GrouperUtil.assertion(inactiveUser != null,
          "Should find deactivated user with includeInactiveUsers=true");
      GrouperUtil.assertion(inactiveUser.getActive() != null && !inactiveUser.getActive(),
          "User should be inactive");
      System.out.println("Found inactive user: " + inactiveUser);

      System.out.println("\n====== CREATE USER (reactivate deactivated user) ======");
      TrueFoundryUser reactivateUser = new TrueFoundryUser();
      reactivateUser.setEmail(testUserEmail);
      reactivateUser.setDisplayName(testUserDisplayName);
      TrueFoundryUser reactivatedByCreate = createUser(configId, settings, reactivateUser);
      GrouperUtil.assertion(reactivatedByCreate != null, "createUser should reactivate the deactivated user");
      GrouperUtil.assertion(reactivatedByCreate.getActive(), "createUser should reactivate the deactivated user");
      System.out.println("createUser reactivated: " + reactivatedByCreate);

      // ID might have changed if user was deleted and recreated, so update our reference for cleanup
      userId = reactivatedByCreate.getId();

      System.out.println("\n====== RETRIEVE USER BY EMAIL (verify reactivated) ======");
      TrueFoundryUser reactivatedUser = retrieveUserByEmail(configId, settings, testUserEmail, false);
      GrouperUtil.assertion(reactivatedUser != null, "Should find reactivated user");
      GrouperUtil.assertion(reactivatedUser.getActive() == null || reactivatedUser.getActive(),
          "Reactivated user should be active");
      System.out.println("Found reactivated user: " + reactivatedUser);

      // ============================
      // Role operations
      // ============================

      System.out.println("\n====== RETRIEVE ROLES ======");
      List<TrueFoundryGroup> allRoles = retrieveRoles(configId, settings);
      System.out.println("Total roles: " + GrouperUtil.length(allRoles));
      for (TrueFoundryGroup role : GrouperUtil.nonNull(allRoles)) {
        System.out.println("  role: id=" + role.getId() + ", name=" + role.getName()
            + ", resourceType=" + role.getResourceType());
      }
      GrouperUtil.assertion(GrouperUtil.length(allRoles) > 0, "Should have at least one role");

      System.out.println("\n====== ASSIGN USER ROLE (member) ======");
      assignUserRole(configId, settings, testUserEmail, "member");
      System.out.println("Assigned role 'member' to: " + testUserEmail);

      System.out.println("\n====== ASSIGN USER ROLE (read-only-member) ======");
      assignUserRole(configId, settings, testUserEmail, "read-only-member");
      System.out.println("Assigned role 'read-only-member' to: " + testUserEmail);

      System.out.println("\n====== ASSIGN USER ROLE (tenant-admin) ======");
      assignUserRole(configId, settings, testUserEmail, "tenant-admin");
      System.out.println("Assigned role 'tenant-admin' to: " + testUserEmail);

      // put user back to member
      assignUserRole(configId, settings, testUserEmail, "member");

      System.out.println("\n====== CREATE ROLE ======");
      TrueFoundryGroup newRole = new TrueFoundryGroup();
      newRole.setName(testRoleName);
      newRole.setDisplayName(testRoleName);
      newRole.setDescription("Test role created by Grouper integration test");
      newRole.setGroupType(TrueFoundryGroup.GROUP_TYPE_ROLE);
      newRole.setResourceType("account");
      TrueFoundryGroup createdRole = createOrUpdateRole(configId, settings, newRole);
      System.out.println("Created role: " + createdRole);
      GrouperUtil.assertion(createdRole != null, "Created role should not be null");
      GrouperUtil.assertion(StringUtils.isNotBlank(createdRole.getId()), "Created role should have an id");
      GrouperUtil.assertion(StringUtils.equals(testRoleName, createdRole.getName()), "Created role name should match");
      String roleId = createdRole.getId();
      System.out.println("Created role id: " + roleId);

      System.out.println("\n====== UPDATE ROLE ======");
      newRole.setDescription("Updated description");
      TrueFoundryGroup updatedRole = createOrUpdateRole(configId, settings, newRole);
      System.out.println("Updated role: " + updatedRole);
      GrouperUtil.assertion(updatedRole != null, "Updated role should not be null");

      System.out.println("\n====== ASSIGN USER TO CUSTOM ROLE ======");
      assignUserRole(configId, settings, testUserEmail, testRoleName);
      System.out.println("Assigned role '" + testRoleName + "' to: " + testUserEmail);

      // put user back to member before deleting the custom role
      assignUserRole(configId, settings, testUserEmail, "member");

      System.out.println("\n====== DELETE ROLE ======");
      deleteRole(configId, settings, roleId);
      System.out.println("Deleted role: " + roleId);

      System.out.println("\n====== VERIFY ROLE DELETED ======");
      List<TrueFoundryGroup> rolesAfterDelete = retrieveRoles(configId, settings);
      boolean roleStillExists = false;
      for (TrueFoundryGroup r : GrouperUtil.nonNull(rolesAfterDelete)) {
        if (StringUtils.equals(roleId, r.getId())) {
          roleStillExists = true;
          break;
        }
      }
      GrouperUtil.assertion(!roleStillExists, "Deleted role should not appear in retrieveRoles");
      System.out.println("Verified role deleted: true");

      // ============================
      // Subjects data (users + teams in one call)
      // ============================

      System.out.println("\n====== RETRIEVE SUBJECTS DATA ======");
      SubjectsData subjectsData = retrieveSubjectsData(configId, settings);
      System.out.println("Subjects data: " + GrouperUtil.length(subjectsData.users) + " users, "
          + GrouperUtil.length(subjectsData.teams) + " teams");
      boolean foundTestUserInSubjects = false;
      for (TrueFoundryUser u : GrouperUtil.nonNull(subjectsData.users)) {
        if (StringUtils.equalsIgnoreCase(testUserEmail, u.getEmail())) {
          foundTestUserInSubjects = true;
          break;
        }
      }
      GrouperUtil.assertion(foundTestUserInSubjects, "Test user should appear in subjects data");
      System.out.println("Found test user in subjects data: true");

      // ============================
      // Team operations
      // ============================

      System.out.println("\n====== RETRIEVE TEAMS ======");
      List<TrueFoundryGroup> allTeams = retrieveTeams(configId, settings);
      System.out.println("Total teams: " + GrouperUtil.length(allTeams));

      System.out.println("\n====== CREATE TEAM ======");
      TrueFoundryGroup newTeam = new TrueFoundryGroup();
      newTeam.setName(testTeamName);
      TrueFoundryGroup createdTeam = createTeam(configId, settings, newTeam);
      System.out.println("Created team: " + createdTeam);
      GrouperUtil.assertion(createdTeam != null, "Created team should not be null");
      GrouperUtil.assertion(StringUtils.isNotBlank(createdTeam.getId()),
          "Created team should have an id");
      GrouperUtil.assertion(StringUtils.equals(testTeamName, createdTeam.getName()),
          "Created team name should match");
      String teamId = createdTeam.getId();
      System.out.println("Created team id: " + teamId);

      System.out.println("\n====== GET TEAM BY ID (verify default member present after create) ======");
      TrueFoundryGroup teamAfterCreate = getTeamById(configId, settings, teamId);
      boolean defaultMemberInTeam = teamAfterCreate != null
          && teamAfterCreate.getMembers() != null
          && teamAfterCreate.getMembers().contains(settings.getDefaultTeamMemberEmail());
      GrouperUtil.assertion(defaultMemberInTeam, "Default team member should be in team after create");
      System.out.println("Default member present after create: true");

      System.out.println("\n====== GET TEAM BY ID ======");
      TrueFoundryGroup fetchedTeam = getTeamById(configId, settings, teamId);
      GrouperUtil.assertion(fetchedTeam != null, "Should find team by id");
      GrouperUtil.assertion(StringUtils.equals(teamId, fetchedTeam.getId()),
          "Fetched team id should match");
      System.out.println("Fetched team: " + fetchedTeam);

      System.out.println("\n====== UPDATE TEAM ======");
      TrueFoundryGroup updateTeamObj = new TrueFoundryGroup();
      updateTeamObj.setId(teamId);
      updateTeamObj.setName(testTeamName + "u");
      TrueFoundryGroup updatedTeam = updateTeam(configId, settings, updateTeamObj);
      System.out.println("Updated team: " + updatedTeam);
      GrouperUtil.assertion(updatedTeam != null, "Updated team should not be null");
      GrouperUtil.assertion(StringUtils.equals(testTeamName + "u", updatedTeam.getName()),
          "Updated team name should match");

      System.out.println("\n====== ADD TEAM MEMBERS (test user as regular member) ======");
      List<String> regularMembers = new ArrayList<String>();
      regularMembers.add(testUserEmail);
      TrueFoundryGroup teamAfterAddMember = addTeamMembers(configId, settings, teamId, null, regularMembers);
      System.out.println("Team after adding regular member: " + teamAfterAddMember);

      System.out.println("\n====== GET TEAM BY ID (verify regular member added) ======");
      TrueFoundryGroup teamWithMember = getTeamById(configId, settings, teamId);
      boolean foundMemberInTeam = teamWithMember != null
          && teamWithMember.getMembers() != null
          && teamWithMember.getMembers().contains(testUserEmail);
      GrouperUtil.assertion(foundMemberInTeam, "Test user should be in team members");
      System.out.println("Found test user in team members: true");

      System.out.println("\n====== ADD TEAM MEMBERS (test user as manager) ======");
      List<String> managerEmails = new ArrayList<String>();
      managerEmails.add(testUserEmail);
      TrueFoundryGroup teamAfterAddManager = addTeamMembers(configId, settings, teamId, managerEmails, null);
      System.out.println("Team after adding as manager: " + teamAfterAddManager);

      System.out.println("\n====== GET TEAM BY ID (verify manager added) ======");
      TrueFoundryGroup teamWithManager = getTeamById(configId, settings, teamId);
      boolean foundManagerInTeam = teamWithManager != null
          && teamWithManager.getManagers() != null
          && teamWithManager.getManagers().contains(testUserEmail);
      GrouperUtil.assertion(foundManagerInTeam, "Test user should be in team managers");
      System.out.println("Found test user in team managers: true");

      System.out.println("\n====== REMOVE TEAM MEMBERS ======");
      List<String> emailsToRemove = new ArrayList<String>();
      emailsToRemove.add(testUserEmail);
      removeTeamMembers(configId, settings, teamId, emailsToRemove);
      System.out.println("Removed test user from team: " + testUserEmail);

      System.out.println("\n====== GET TEAM BY ID (verify member removed, default still present) ======");
      TrueFoundryGroup teamAfterRemove = getTeamById(configId, settings, teamId);
      boolean stillInTeam = teamAfterRemove != null
          && teamAfterRemove.getMembers() != null
          && teamAfterRemove.getMembers().contains(testUserEmail);
      GrouperUtil.assertion(!stillInTeam, "Test user should NOT be in team members after removal");
      boolean defaultStillInTeam = teamAfterRemove != null
          && teamAfterRemove.getMembers() != null
          && teamAfterRemove.getMembers().contains(settings.getDefaultTeamMemberEmail());
      GrouperUtil.assertion(defaultStillInTeam, "Default team member should still be in team after removal");
      System.out.println("Verified test user removed and default member retained: true");

      System.out.println("\n====== RETRIEVE TEAMS (verify created team appears) ======");
      List<TrueFoundryGroup> teamsAfterCreate = retrieveTeams(configId, settings);
      boolean foundTeamInAll = false;
      for (TrueFoundryGroup t : GrouperUtil.nonNull(teamsAfterCreate)) {
        if (StringUtils.equals(teamId, t.getId())) {
          foundTeamInAll = true;
          break;
        }
      }
      GrouperUtil.assertion(foundTeamInAll, "Created team should be in retrieveTeams list");
      System.out.println("Found created team in retrieveTeams: true");

      // ============================
      // Cleanup: delete team, then delete user
      // ============================

      System.out.println("\n====== DELETE TEAM ======");
      deleteTeam(configId, settings, teamId);
      System.out.println("Deleted team: " + teamId);

      System.out.println("\n====== DELETE TEAM (not found, should not error) ======");
      deleteTeam(configId, settings, teamId);
      System.out.println("Delete team again (404 accepted): ok");

      System.out.println("\n====== VERIFY TEAM DELETED ======");
      TrueFoundryGroup deletedTeam = getTeamById(configId, settings, teamId);
      GrouperUtil.assertion(deletedTeam == null, "Team should not exist after delete");
      System.out.println("Verified team deleted: true");

      GrouperUtil.sleep(5000);
      
      System.out.println("\n====== DELETE USER ======");
      deleteUser(configId, settings, userId, testUserEmail);
      System.out.println("Deleted user: " + userId);

      GrouperUtil.sleep(5000);

      System.out.println("\n====== DELETE USER (not found, should not error) ======");
      deleteUser(configId, settings, userId, null);
      System.out.println("Delete user again (404 accepted): ok");
      GrouperUtil.sleep(5000);
      System.out.println("\n====== VERIFY USER DELETED ======");
      TrueFoundryUser deletedUser = retrieveUserByEmail(configId, settings, testUserEmail, true);
      GrouperUtil.assertion(deletedUser == null, "User should not exist after delete");
      System.out.println("Verified user deleted: true");

      System.out.println("\n============================");
      System.out.println("ALL TESTS PASSED!");
      System.out.println("============================");

    } catch (Exception e) {
      System.out.println("\nERROR: " + GrouperClientUtils.getFullStackTrace(e));
    }
    System.exit(0);
  }

  // ============================
  // Name validation
  // ============================

  /**
   * Validate that a TrueFoundry name conforms to the required format:
   * must start with a lowercase letter, end with a lowercase letter or digit,
   * contain only lowercase letters, digits, and hyphens in between,
   * and be between 3 and 36 characters total.
   * Throws a RuntimeException if the name is invalid.
   * @param name the name to validate
   * @param context description for error messages (e.g. "team name", "role name")
   */
  public static void validateTrueFoundryName(String name, String context) {
    if (StringUtils.isBlank(name)) {
      return;
    }
    if (!name.matches("^[a-z][a-z0-9-]{1,34}[a-z0-9]$")) {
      throw new RuntimeException(context + " must start with a lowercase letter, end with a lowercase"
          + " letter or digit, contain only lowercase letters, digits, and hyphens,"
          + " and be between 3 and 36 characters long: '" + name + "'");
    }
  }

  // ============================
  // Ignore helpers
  // ============================

  /**
   * Parse a comma-separated ignore string into a lowercase set for case-insensitive matching.
   * @param commaSeparated the comma-separated string (may be null or blank)
   * @return the set of lowercase trimmed values, or null if input is blank
   */
  public static Set<String> parseIgnoreSet(String commaSeparated) {
    if (StringUtils.isBlank(commaSeparated)) {
      return null;
    }
    Set<String> result = new LinkedHashSet<String>();
    for (String value : GrouperUtil.splitTrim(commaSeparated, ",")) {
      if (!StringUtils.isBlank(value)) {
        result.add(value.toLowerCase());
      }
    }
    return result.isEmpty() ? null : result;
  }

  /**
   * Check if a value is in the ignore set (case-insensitive).
   * @param ignoreSet the set of lowercase values to ignore
   * @param value the value to check
   * @return true if the value should be ignored
   */
  private static boolean isIgnored(Set<String> ignoreSet, String value) {
    if (ignoreSet == null || StringUtils.isBlank(value)) {
      return false;
    }
    return ignoreSet.contains(value.toLowerCase());
  }

  /**
   * Throw if the team is the system-managed "everyone" team.
   */
  private static void assertNotEveryoneTeam(TrueFoundryGroup team) {
    if (team != null && TEAM_NAME_EVERYONE.equals(team.getName())) {
      throw new RuntimeException("The 'everyone' team is managed internally by TrueFoundry "
          + "and cannot be created, updated, or deleted via the provisioner.");
    }
  }

  /**
   * System-managed role names that cannot be created, updated, or deleted via the provisioner.
   * tenant-admin and team-manager are built-in TrueFoundry roles.
   * tenant-admin can be assigned to users (via assignUserRole) but not edited or deleted.
   * team-manager cannot be assigned at all.
   */
  private static final Set<String> SYSTEM_ROLE_NAMES = GrouperUtil.toSet(
      ROLE_NAME_TENANT_ADMIN, ROLE_NAME_TEAM_MANAGER);

  /**
   * Throw if the role name is a system-managed role that cannot be created, updated, or deleted.
   */
  private static void assertNotSystemRole(String roleName) {
    if (roleName != null && SYSTEM_ROLE_NAMES.contains(roleName)) {
      throw new RuntimeException("The '" + roleName + "' role is managed internally by TrueFoundry "
          + "and cannot be created, updated, or deleted via the provisioner.");
    }
  }

  /**
   * Retrieve the role by ID and throw if it's a system-managed role.
   */
  private static void assertNotSystemRoleById(String configId, String roleId) {
    List<TrueFoundryGroup> allRoles = retrieveRoles(configId, null);
    for (TrueFoundryGroup role : GrouperUtil.nonNull(allRoles)) {
      if (StringUtils.equals(roleId, role.getId())) {
        assertNotSystemRole(role.getName());
        break;
      }
    }
  }

}
