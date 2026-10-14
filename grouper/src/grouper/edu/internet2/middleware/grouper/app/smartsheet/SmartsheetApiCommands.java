package edu.internet2.middleware.grouper.app.smartsheet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Static wrappers around the Smartsheet API 2.0 user and group endpoints. All calls speak JSON.
 * Authentication is a bearer API access token held by the WsBearerToken external system; the token
 * should belong to a System Admin (user writes, deactivate, and the status / account flags on reads
 * are System Admin only; group writes need Group Admin or System Admin).
 *
 * <p>Endpoints used (paths are relative to the external system endpoint, e.g.
 * https://api.smartsheet.com):</p>
 * <ul>
 *   <li>GET    /2.0/users?page=&amp;pageSize=       -- list org users (paged)</li>
 *   <li>GET    /2.0/users?email=                   -- find users by email</li>
 *   <li>GET    /2.0/users/{userId}                 -- one user</li>
 *   <li>POST   /2.0/users?sendEmail=               -- add a user to the org</li>
 *   <li>PUT    /2.0/users/{userId}                 -- update names / account flags</li>
 *   <li>POST   /2.0/users/{userId}/deactivate      -- deactivate</li>
 *   <li>POST   /2.0/users/{userId}/reactivate      -- reactivate</li>
 *   <li>DELETE /2.0/users/{userId}                 -- remove from the org</li>
 *   <li>POST   /2.0/users/{userId}/plans/{planId}/upgrade   -- seat to MEMBER or GUEST</li>
 *   <li>POST   /2.0/users/{userId}/plans/{planId}/downgrade -- seat to CONTRIBUTOR or GUEST</li>
 *   <li>GET    /2.0/groups?page=&amp;pageSize=      -- list groups (paged, no members)</li>
 *   <li>GET    /2.0/groups/{groupId}               -- one group with members</li>
 *   <li>POST   /2.0/groups                         -- create a group</li>
 *   <li>PUT    /2.0/groups/{groupId}               -- update name / description</li>
 *   <li>DELETE /2.0/groups/{groupId}               -- delete a group</li>
 *   <li>POST   /2.0/groups/{groupId}/members       -- add members (bulk, existing skipped)</li>
 *   <li>DELETE /2.0/groups/{groupId}/members/{userId} -- remove one member</li>
 * </ul>
 *
 * <p>Sync-back: every user and group read here, and each group's member list, is captured into the
 * native mirror (via {@link SmartsheetProvisioningTargetNativeSync}) before any filtering, so the mirror
 * is the full view of Smartsheet. Captures are no-ops outside a Smartsheet provisioning run (e.g. the
 * MCP user lookup).</p>
 *
 * <p>Smartsheet rate limits per token (HTTP 429, errorCode 4003). Calls that get 429 (or a 502/503/504
 * from the gateway) are retried with a doubling backoff, see {@link #retrySettings()}.</p>
 */
public class SmartsheetApiCommands {

  /** Logger */
  private static final Log LOG = GrouperUtil.getLog(SmartsheetApiCommands.class);

  /** never log the Authorization header */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  /** rate limited (429) and gateway / availability codes are retried; other codes are real answers */
  public static final Set<Integer> RETRY_RETURN_CODES = GrouperUtil.toSet(429, 502, 503, 504);

  /** retries after the first attempt when no Smartsheet provisioner is running */
  public static final int DEFAULT_RETRY_COUNT = 5;

  /** first retry sleep when no Smartsheet provisioner is running; doubles each retry */
  public static final int DEFAULT_RETRY_SLEEP_MILLIS = 2000;

  /** most retries allowed, the backoff doubles so more is never useful */
  public static final int MAX_RETRY_COUNT = 10;

  /** longest sleep between retries */
  public static final long MAX_RETRY_SLEEP_MILLIS = 60000L;

  /** page size for list calls; 100 is the Smartsheet default and accepted by every list call */
  public static final int PAGE_SIZE = 100;

  /** tests only: overrides the retry sleep so retry tests do not take seconds; null normally */
  static Integer retrySleepMillisForTests = null;

  /**
   * @param configId WsBearerToken external system config id
   * @return the API base url (the external system endpoint) with no trailing slash
   */
  public static String retrieveBaseUrl(String configId) {
    return GrouperUtil.stripLastSlashIfExists(GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired("grouper.wsBearerToken." + configId + ".endpoint"));
  }

  /**
   * Retry count and first sleep for the running Smartsheet provisioner (smartsheetRetryCount,
   * smartsheetRetrySleepMillis), or the defaults when called outside one (e.g. tests, MCP lookups).
   * @return {retryCount, retrySleepMillis}
   */
  static int[] retrySettings() {
    int retryCount = DEFAULT_RETRY_COUNT;
    int retrySleepMillis = DEFAULT_RETRY_SLEEP_MILLIS;
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    GrouperProvisioningConfiguration configuration = grouperProvisioner == null ? null
        : grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    if (configuration instanceof SmartsheetProvisionerConfiguration) {
      retryCount = ((SmartsheetProvisionerConfiguration) configuration).getSmartsheetRetryCount();
      retrySleepMillis = ((SmartsheetProvisionerConfiguration) configuration).getSmartsheetRetrySleepMillis();
    }
    if (retrySleepMillisForTests != null) {
      retrySleepMillis = retrySleepMillisForTests;
    }
    // cap both so a large config value cannot back off for hours (or overflow the shift)
    return new int[] {Math.min(MAX_RETRY_COUNT, Math.max(0, retryCount)), Math.max(0, retrySleepMillis)};
  }

  /**
   * Execute an HTTP call against the Smartsheet API, retrying on 429/502/503/504.
   * @param debugMap map to accumulate debug info
   * @param debugLabel label for provisioner call stats
   * @param httpMethodName GET, POST, PUT, DELETE
   * @param configId the WsBearerToken external system config id
   * @param pathAndQuery path after the base url, already url-encoded (e.g. /2.0/users?page=2)
   * @param allowedReturnCodes acceptable HTTP status codes
   * @param returnCode single-element array that receives the actual status code
   * @param jsonBody the JSON request body, or null
   * @return the raw response body, or null if blank
   */
  private static String executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String pathAndQuery, Set<Integer> allowedReturnCodes,
      int[] returnCode, String jsonBody) {

    int[] retrySettings = retrySettings();
    int retryCount = retrySettings[0];
    int retrySleepMillis = retrySettings[1];

    String url = retrieveBaseUrl(configId) + pathAndQuery;
    debugMap.put("url", url);
    debugMap.put("method", httpMethodName);

    int code = -1;
    String body = null;
    for (int attempt = 0; ; attempt++) {

      // a fresh client per attempt: a GrouperHttpClient is single use
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders);

      // adds the Authorization: Bearer header, proxy settings, and delayAfterEachCallInMs
      WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(
          grouperHttpClient, configId, GrouperLoaderConfig.retrieveConfig(), debugMap);

      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(httpMethodName);
      grouperHttpClient.addHeader("Accept", "application/json");
      if (StringUtils.isNotBlank(jsonBody)) {
        grouperHttpClient.addHeader("Content-Type", "application/json");
        grouperHttpClient.assignBody(jsonBody);
      }

      long httpCallStartMillis = System.currentTimeMillis();
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        body = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      } finally {
        GrouperProvisioner.incrementCommandsCallsStats(debugLabel, 1,
            System.currentTimeMillis() - httpCallStartMillis);
      }

      if (!RETRY_RETURN_CODES.contains(code) || attempt >= retryCount) {
        break;
      }

      // rate limited or the gateway hiccupped: back off and try again.  WARN, not ERROR, since the
      // retry normally succeeds
      long sleepMillis = Math.min(MAX_RETRY_SLEEP_MILLIS, (long) retrySleepMillis << attempt);
      LOG.warn("Smartsheet " + httpMethodName + " '" + url + "' returned " + code + ", retry "
          + (attempt + 1) + " of " + retryCount + " in " + sleepMillis + "ms");
      debugMap.put("retries", attempt + 1);
      GrouperUtil.sleep(sleepMillis);
    }

    returnCode[0] = code;
    debugMap.put("responseCode", code);

    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException("Invalid return code '" + code + "', expecting: "
          + GrouperUtil.setToString(allowedReturnCodes) + ". " + httpMethodName + " '" + url + "' "
          + (RETRY_RETURN_CODES.contains(code) ? "(after " + retryCount + " retries) " : "")
          + StringUtils.abbreviate(body, 2000));
    }

    return StringUtils.isBlank(body) ? null : body;
  }

  /**
   * Parse a JSON response body.
   * @param json the body
   * @return the node, or null if blank
   */
  private static JsonNode parseJson(String json) {
    if (StringUtils.isBlank(json)) {
      return null;
    }
    try {
      return GrouperUtil.jsonJacksonNode(json);
    } catch (Exception e) {
      throw new RuntimeException("Error parsing Smartsheet JSON response: '" + StringUtils.abbreviate(json, 2000) + "'", e);
    }
  }

  /**
   * Writes return {"message":"SUCCESS","resultCode":0,"result":{...}}. Return the result object (some
   * endpoints document it as "data", so fall back to that).
   * @param node the response
   * @return the result node or null
   */
  private static JsonNode resultNode(JsonNode node) {
    if (node == null) {
      return null;
    }
    JsonNode result = GrouperUtil.jsonJacksonGetNode(node, "result");
    if (result == null || result.isNull()) {
      result = GrouperUtil.jsonJacksonGetNode(node, "data");
    }
    return result == null || result.isNull() ? null : result;
  }

  // ==================== users ====================

  /**
   * Retrieve all users in the organization from GET /2.0/users, paging until totalPages.
   * @param configId the external system config id
   * @param ignoreEmails lowercased user emails to exclude (from {@link #parseIgnoreSet})
   * @return the users (never null)
   */
  public static List<SmartsheetUser> retrieveUsers(String configId, Set<String> ignoreEmails) {
    return retrieveUsers(configId, ignoreEmails, null);
  }

  /**
   * Query string that makes /2.0/users return each user's seatType (and isInternal) for a plan. With
   * a planId the list can also include users outside the org (e.g. external collaborators on the
   * plan). displayContributorSeatType makes free-tier users report CONTRIBUTOR instead of the
   * deprecated VIEWER.
   * @param planId the plan id, or null for none
   * @return the query string fragment starting with "&amp;", or "" if no planId
   */
  static String planIdParams(String planId) {
    if (StringUtils.isBlank(planId)) {
      return "";
    }
    return "&planId=" + GrouperUtil.escapeUrlEncode(planId.trim()) + "&displayContributorSeatType=true";
  }

  /**
   * Retrieve all users in the organization from GET /2.0/users, paging until totalPages.
   * @param configId the external system config id
   * @param ignoreEmails lowercased user emails to exclude (from {@link #parseIgnoreSet})
   * @param planId if not blank, read seat types for this plan (System Admin)
   * @return the users (never null)
   */
  public static List<SmartsheetUser> retrieveUsers(String configId, Set<String> ignoreEmails, String planId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveUsers");
    long startNanos = System.nanoTime();

    List<SmartsheetUser> result = new ArrayList<SmartsheetUser>();
    try {
      int page = 1;
      int totalPages = 1;
      while (page <= totalPages) {
        String json = executeMethod(debugMap, "retrieveUsers", "GET", configId,
            "/2.0/users?pageSize=" + PAGE_SIZE + "&page=" + page + planIdParams(planId), GrouperUtil.toSet(200), new int[] {-1}, null);
        JsonNode node = parseJson(json);
        if (node == null) {
          break;
        }
        totalPages = GrouperUtil.intValue(GrouperUtil.jsonJacksonGetInteger(node, "totalPages"), 1);
        ArrayNode data = GrouperUtil.jsonJacksonGetArrayNode(node, "data");
        for (int i = 0; data != null && i < data.size(); i++) {
          SmartsheetUser user = SmartsheetUser.fromJson(data.get(i));
          // sync-back: the full view, before the ignore filter
          SmartsheetProvisioningTargetNativeSync.captureUserFromCurrentProvisioner(user);
          if (user != null && !isIgnored(user.getEmail(), ignoreEmails)) {
            result.add(user);
          }
        }
        page++;
      }
      debugMap.put("pages", page - 1);
      debugMap.put("userCount", result.size());
      return result;
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Retrieve a user by email from GET /2.0/users?email=. Emails are compared case-insensitively.
   * @param configId the external system config id
   * @param email the email
   * @return the user, or null if not found
   */
  public static SmartsheetUser retrieveUserByEmail(String configId, String email) {
    return retrieveUserByEmail(configId, email, null);
  }

  /**
   * Retrieve a user by email from GET /2.0/users?email=, optionally with the seat type for a plan.
   * Emails are compared case-insensitively.
   * @param configId the external system config id
   * @param email the email
   * @param planId if not blank, read the seat type for this plan (System Admin)
   * @return the user, or null if not found
   */
  public static SmartsheetUser retrieveUserByEmail(String configId, String email, String planId) {
    return SmartsheetUser.fromJson(retrieveUserJsonByEmail(configId, email, planId));
  }

  /**
   * Retrieve the raw user JSON by email from GET /2.0/users?email=, optionally with the seat type
   * for a plan. The email filter is checked against the result (case-insensitive) so an ignored or
   * misread filter can never return some other user. Used by {@link #retrieveUserByEmail} and the
   * MCP user lookup.
   * @param configId the external system config id
   * @param email the email
   * @param planId if not blank, include the seat type for this plan (System Admin)
   * @return the user JSON object, or null if not found
   */
  public static JsonNode retrieveUserJsonByEmail(String configId, String email, String planId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveUserByEmail");
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(email)) {
        return null;
      }
      String json = executeMethod(debugMap, "retrieveUserByEmail", "GET", configId,
          "/2.0/users?email=" + GrouperUtil.escapeUrlEncode(email) + planIdParams(planId), GrouperUtil.toSet(200), new int[] {-1}, null);
      JsonNode node = parseJson(json);
      ArrayNode data = node == null ? null : GrouperUtil.jsonJacksonGetArrayNode(node, "data");
      for (int i = 0; data != null && i < data.size(); i++) {
        JsonNode userNode = data.get(i);
        if (userNode != null && StringUtils.equalsIgnoreCase(email, GrouperUtil.jsonJacksonGetString(userNode, "email"))) {
          debugMap.put("found", true);
          SmartsheetProvisioningTargetNativeSync.captureUserFromCurrentProvisioner(SmartsheetUser.fromJson(userNode));
          return userNode;
        }
      }
      debugMap.put("found", false);
      return null;
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Retrieve a user by native id from GET /2.0/users/{userId}.
   * @param configId the external system config id
   * @param userId the native user id
   * @return the user, or null if not found (404)
   */
  public static SmartsheetUser retrieveUserById(String configId, String userId) {
    return SmartsheetUser.fromJson(retrieveUserJsonById(configId, userId));
  }

  /**
   * Retrieve the raw user JSON (the user profile) by native id from GET /2.0/users/{userId}. Used by
   * {@link #retrieveUserById} and the MCP user lookup.
   * @param configId the external system config id
   * @param userId the native user id
   * @return the user JSON object, or null if not found (404)
   */
  public static JsonNode retrieveUserJsonById(String configId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveUserById");
    debugMap.put("userId", userId);
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(userId)) {
        return null;
      }
      int[] returnCode = new int[] {-1};
      String json = executeMethod(debugMap, "retrieveUserById", "GET", configId,
          "/2.0/users/" + GrouperUtil.escapeUrlEncode(userId), GrouperUtil.toSet(200, 404), returnCode, null);
      if (returnCode[0] == 404) {
        return null;
      }
      JsonNode userNode = parseJson(json);
      // sync-back. The profile has no seatType: a plan-scoped email read (which the provisioner does
      // right after when seat types are managed) re-captures the user with it
      SmartsheetProvisioningTargetNativeSync.captureUserFromCurrentProvisioner(SmartsheetUser.fromJson(userNode));
      return userNode;
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Add a user to the organization via POST /2.0/users. If the org has user auto provisioning and
   * the user matches it, the user is added directly; otherwise they are invited (status PENDING).
   * @param configId the external system config id
   * @param user the user to add (email required)
   * @param sendEmail whether Smartsheet should email the user
   * @return the created user (with the native id)
   */
  public static SmartsheetUser createUser(String configId, SmartsheetUser user, boolean sendEmail) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createUser");
    debugMap.put("email", user.getEmail());
    long startNanos = System.nanoTime();

    try {
      String json = executeMethod(debugMap, "createUser", "POST", configId,
          "/2.0/users?sendEmail=" + sendEmail, GrouperUtil.toSet(200), new int[] {-1},
          GrouperUtil.jsonJacksonToString(user.toJsonForCreate()));
      SmartsheetUser created = SmartsheetUser.fromJson(resultNode(parseJson(json)));
      if (created == null || StringUtils.isBlank(created.getId())) {
        // should not happen; fall back to a lookup by email so the id is authoritative
        created = retrieveUserByEmail(configId, user.getEmail());
      }
      if (created == null || StringUtils.isBlank(created.getId())) {
        throw new RuntimeException("Smartsheet did not return an id for created user '" + user.getEmail() + "': " + json);
      }
      debugMap.put("newId", created.getId());
      return created;
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Update a user via PUT /2.0/users/{userId}. Only the fields in fieldNamesToUpdate are sent.
   * @param configId the external system config id
   * @param userId the native user id
   * @param user holds the desired values
   * @param fieldNamesToUpdate target attribute names that changed; null sends all managed fields
   */
  public static void updateUser(String configId, String userId, SmartsheetUser user, Set<String> fieldNamesToUpdate) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateUser");
    debugMap.put("userId", userId);
    debugMap.put("fields", fieldNamesToUpdate == null ? "all" : GrouperUtil.setToString(fieldNamesToUpdate));
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("user id is required for updateUser");
      }
      ObjectNode body = user.toJsonForUpdate(fieldNamesToUpdate);
      if (body.size() == 0) {
        return;
      }
      executeMethod(debugMap, "updateUser", "PUT", configId,
          "/2.0/users/" + GrouperUtil.escapeUrlEncode(userId), GrouperUtil.toSet(200), new int[] {-1},
          GrouperUtil.jsonJacksonToString(body));
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Deactivate a user via POST /2.0/users/{userId}/deactivate. Their sharing is unchanged; they just
   * cannot use Smartsheet. Not available on Smartsheet Gov, and not for users managed by an IdP /
   * directory integration or whose email is not on the plan's domains.
   * @param configId the external system config id
   * @param userId the native user id
   */
  public static void deactivateUser(String configId, String userId) {
    userAction(configId, userId, "deactivate");
  }

  /**
   * Reactivate a deactivated user via POST /2.0/users/{userId}/reactivate.
   * @param configId the external system config id
   * @param userId the native user id
   */
  public static void reactivateUser(String configId, String userId) {
    userAction(configId, userId, "reactivate");
  }

  /**
   * POST /2.0/users/{userId}/{action} with no body.
   */
  private static void userAction(String configId, String userId, String action) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", action + "User");
    debugMap.put("userId", userId);
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("user id is required for " + action + "User");
      }
      executeMethod(debugMap, action + "User", "POST", configId,
          "/2.0/users/" + GrouperUtil.escapeUrlEncode(userId) + "/" + action, GrouperUtil.toSet(200),
          new int[] {-1}, null);
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Remove a user from the organization via DELETE /2.0/users/{userId}. A 404 (already gone) is
   * treated as success.
   * @param configId the external system config id
   * @param userId the native user id
   * @param transferToUserId if not blank, the user who receives the removed user's groups and sheets
   * @param removeFromSharing whether to remove the user's access to all org assets
   */
  public static void removeUser(String configId, String userId, String transferToUserId, boolean removeFromSharing) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "removeUser");
    debugMap.put("userId", userId);
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("user id is required for removeUser");
      }
      StringBuilder pathAndQuery = new StringBuilder("/2.0/users/").append(GrouperUtil.escapeUrlEncode(userId));
      pathAndQuery.append("?removeFromSharing=").append(removeFromSharing);
      if (!StringUtils.isBlank(transferToUserId)) {
        pathAndQuery.append("&transferTo=").append(GrouperUtil.escapeUrlEncode(transferToUserId));
        pathAndQuery.append("&transferSheets=true");
      }
      executeMethod(debugMap, "removeUser", "DELETE", configId, pathAndQuery.toString(),
          GrouperUtil.toSet(200, 404), new int[] {-1}, null);
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Change a user's seat type via POST /2.0/users/{userId}/plans/{planId}/upgrade (MEMBER, or GUEST
   * from CONTRIBUTOR) or .../downgrade (CONTRIBUTOR, or GUEST from MEMBER / PROVISIONAL_MEMBER).
   * System Admin. PROVISIONAL_MEMBER cannot be set, and a deactivated user must be reactivated
   * first. Upgrading to the current seat returns 200.
   * @param configId the external system config id
   * @param userId the native user id
   * @param planId the plan id
   * @param seatType the new seat: MEMBER, CONTRIBUTOR, or GUEST
   * @param upgrade true for the upgrade endpoint, false for downgrade
   */
  public static void changeUserSeatType(String configId, String userId, String planId, String seatType, boolean upgrade) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    String action = upgrade ? "upgrade" : "downgrade";
    debugMap.put("method", action + "User");
    debugMap.put("userId", userId);
    debugMap.put("seatType", seatType);
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(userId) || StringUtils.isBlank(planId) || StringUtils.isBlank(seatType)) {
        throw new RuntimeException("user id, plan id and seat type are required to " + action + " a user");
      }
      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put("seatType", seatType);
      executeMethod(debugMap, action + "User", "POST", configId,
          "/2.0/users/" + GrouperUtil.escapeUrlEncode(userId) + "/plans/" + GrouperUtil.escapeUrlEncode(planId.trim()) + "/" + action,
          GrouperUtil.toSet(200), new int[] {-1}, GrouperUtil.jsonJacksonToString(body));
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Whether moving from currentSeat to desiredSeat is an upgrade (else a downgrade). Smartsheet's
   * ladder is CONTRIBUTOR &lt; GUEST &lt; PROVISIONAL_MEMBER &lt; MEMBER: MEMBER is always an upgrade,
   * CONTRIBUTOR always a downgrade, and GUEST is an upgrade from CONTRIBUTOR and a downgrade from
   * MEMBER / PROVISIONAL_MEMBER.
   * @param currentSeat the current seat (normalized), may be null if unknown
   * @param desiredSeat the desired seat (normalized)
   * @return true for upgrade
   */
  public static boolean isSeatUpgrade(String currentSeat, String desiredSeat) {
    if (SmartsheetUser.SEAT_MEMBER.equals(desiredSeat)) {
      return true;
    }
    if (SmartsheetUser.SEAT_CONTRIBUTOR.equals(desiredSeat)) {
      return false;
    }
    if (SmartsheetUser.SEAT_GUEST.equals(desiredSeat)) {
      return !(SmartsheetUser.SEAT_MEMBER.equals(currentSeat) || SmartsheetUser.SEAT_PROVISIONAL_MEMBER.equals(currentSeat));
    }
    throw new RuntimeException("Seat type cannot be set by Grouper: '" + desiredSeat + "'");
  }

  // ==================== groups ====================

  /**
   * Retrieve all groups from GET /2.0/groups, paging until totalPages. The list does not include
   * members; use {@link #retrieveGroup} for those.
   * @param configId the external system config id
   * @param ignoreGroupNames lowercased group names to exclude
   * @return the groups (never null)
   */
  public static List<SmartsheetGroup> retrieveGroups(String configId, Set<String> ignoreGroupNames) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveGroups");
    long startNanos = System.nanoTime();

    List<SmartsheetGroup> result = new ArrayList<SmartsheetGroup>();
    try {
      int page = 1;
      int totalPages = 1;
      while (page <= totalPages) {
        String json = executeMethod(debugMap, "retrieveGroups", "GET", configId,
            "/2.0/groups?pageSize=" + PAGE_SIZE + "&page=" + page, GrouperUtil.toSet(200), new int[] {-1}, null);
        JsonNode node = parseJson(json);
        if (node == null) {
          break;
        }
        totalPages = GrouperUtil.intValue(GrouperUtil.jsonJacksonGetInteger(node, "totalPages"), 1);
        ArrayNode data = GrouperUtil.jsonJacksonGetArrayNode(node, "data");
        for (int i = 0; data != null && i < data.size(); i++) {
          SmartsheetGroup group = SmartsheetGroup.fromJson(data.get(i));
          // sync-back: the full view, before the ignore filter
          SmartsheetProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(group);
          if (group != null && !isIgnored(group.getName(), ignoreGroupNames)) {
            result.add(group);
          }
        }
        page++;
      }
      debugMap.put("pages", page - 1);
      debugMap.put("groupCount", result.size());
      return result;
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Retrieve one group with its members from GET /2.0/groups/{groupId}.
   * @param configId the external system config id
   * @param groupId the native group id
   * @return the group with members, or null if not found (404)
   */
  public static SmartsheetGroup retrieveGroup(String configId, String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveGroup");
    debugMap.put("groupId", groupId);
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(groupId)) {
        return null;
      }
      int[] returnCode = new int[] {-1};
      String json = executeMethod(debugMap, "retrieveGroup", "GET", configId,
          "/2.0/groups/" + GrouperUtil.escapeUrlEncode(groupId), GrouperUtil.toSet(200, 404), returnCode, null);
      if (returnCode[0] == 404) {
        return null;
      }
      SmartsheetGroup group = SmartsheetGroup.fromJson(parseJson(json));
      debugMap.put("memberCount", group == null ? 0 : GrouperUtil.length(group.getMembers()));
      // sync-back: the group and its full member list
      if (group != null) {
        SmartsheetProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(group);
        List<String> memberIds = new ArrayList<String>();
        for (SmartsheetUser member : GrouperUtil.nonNull(group.getMembers())) {
          if (!StringUtils.isBlank(member.getId())) {
            memberIds.add(member.getId());
          }
        }
        SmartsheetProvisioningTargetNativeSync.captureMembershipsForGroupFromCurrentProvisioner(group.getId(), memberIds);
      }
      return group;
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Find a group by name (case-insensitive, names are unique in the org) and return it with its
   * members.
   * @param configId the external system config id
   * @param name the group name
   * @param ignoreGroupNames lowercased group names to exclude
   * @return the group with members, or null if not found
   */
  public static SmartsheetGroup retrieveGroupByName(String configId, String name, Set<String> ignoreGroupNames) {
    if (StringUtils.isBlank(name)) {
      return null;
    }
    for (SmartsheetGroup group : retrieveGroups(configId, ignoreGroupNames)) {
      if (StringUtils.equalsIgnoreCase(name, group.getName())) {
        return retrieveGroup(configId, group.getId());
      }
    }
    return null;
  }

  /**
   * Create a group via POST /2.0/groups (no members; Grouper adds them with the member endpoint).
   * @param configId the external system config id
   * @param group the group (name required)
   * @return the created group (with the native id)
   */
  public static SmartsheetGroup createGroup(String configId, SmartsheetGroup group) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createGroup");
    debugMap.put("name", group.getName());
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(group.getName())) {
        throw new RuntimeException("group name is required for createGroup");
      }
      String json = executeMethod(debugMap, "createGroup", "POST", configId, "/2.0/groups",
          GrouperUtil.toSet(200), new int[] {-1}, GrouperUtil.jsonJacksonToString(group.toJson(null)));
      SmartsheetGroup created = SmartsheetGroup.fromJson(resultNode(parseJson(json)));
      if (created == null || StringUtils.isBlank(created.getId())) {
        throw new RuntimeException("Smartsheet did not return an id for created group '" + group.getName() + "': " + json);
      }
      debugMap.put("newId", created.getId());
      return created;
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Update a group via PUT /2.0/groups/{groupId}. Only the fields in fieldNamesToUpdate are sent.
   * @param configId the external system config id
   * @param groupId the native group id
   * @param group holds the desired values
   * @param fieldNamesToUpdate target attribute names that changed; null sends name and description
   */
  public static void updateGroup(String configId, String groupId, SmartsheetGroup group, Set<String> fieldNamesToUpdate) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateGroup");
    debugMap.put("groupId", groupId);
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("group id is required for updateGroup");
      }
      ObjectNode body = group.toJson(fieldNamesToUpdate);
      if (body.size() == 0) {
        return;
      }
      executeMethod(debugMap, "updateGroup", "PUT", configId,
          "/2.0/groups/" + GrouperUtil.escapeUrlEncode(groupId), GrouperUtil.toSet(200), new int[] {-1},
          GrouperUtil.jsonJacksonToString(body));
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Delete a group via DELETE /2.0/groups/{groupId}. A 404 (already gone) is treated as success.
   * @param configId the external system config id
   * @param groupId the native group id
   */
  public static void deleteGroup(String configId, String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "deleteGroup");
    debugMap.put("groupId", groupId);
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("group id is required for deleteGroup");
      }
      executeMethod(debugMap, "deleteGroup", "DELETE", configId,
          "/2.0/groups/" + GrouperUtil.escapeUrlEncode(groupId), GrouperUtil.toSet(200, 404), new int[] {-1}, null);
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * Add members to a group via POST /2.0/groups/{groupId}/members with an array of member objects.
   * Each member is sent by native user id when known (the stable link Grouper matches on, immune to
   * an email that drifted), else by email. Users who are already members are skipped by Smartsheet
   * (no error when sending an array).
   * @param configId the external system config id
   * @param groupId the native group id
   * @param members the members to add (id and / or email set)
   */
  public static void addGroupMembers(String configId, String groupId, List<SmartsheetUser> members) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "addGroupMembers");
    debugMap.put("groupId", groupId);
    debugMap.put("memberCount", GrouperUtil.length(members));
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("group id is required for addGroupMembers");
      }
      ArrayNode body = GrouperUtil.jsonJacksonArrayNode();
      int byIdCount = 0;
      for (SmartsheetUser member : GrouperUtil.nonNull(members)) {
        ObjectNode memberNode = memberJsonForAdd(member);
        if (memberNode == null) {
          continue;
        }
        if (memberNode.has("id")) {
          byIdCount++;
        }
        body.add(memberNode);
      }
      if (body.size() == 0) {
        return;
      }
      debugMap.put("byIdCount", byIdCount);
      executeMethod(debugMap, "addGroupMembers", "POST", configId,
          "/2.0/groups/" + GrouperUtil.escapeUrlEncode(groupId) + "/members", GrouperUtil.toSet(200),
          new int[] {-1}, GrouperUtil.jsonJacksonToString(body));
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * The member object for a group member add: {"id": n} when the native id is a number, else
   * {"email": "..."}.
   * @param member the member (may be null)
   * @return the JSON object, or null if there is neither an id nor an email
   */
  static ObjectNode memberJsonForAdd(SmartsheetUser member) {
    if (member == null) {
      return null;
    }
    ObjectNode memberNode = GrouperUtil.jsonJacksonNode();
    if (!StringUtils.isBlank(member.getId())) {
      try {
        memberNode.put("id", Long.parseLong(member.getId().trim()));
        return memberNode;
      } catch (NumberFormatException nfe) {
        // not a Smartsheet id, fall through to email
      }
    }
    if (!StringUtils.isBlank(member.getEmail())) {
      memberNode.put("email", member.getEmail());
      return memberNode;
    }
    return null;
  }

  /**
   * Remove one member from a group via DELETE /2.0/groups/{groupId}/members/{userId}. A 404 (not a
   * member, or already gone) is treated as success.
   * @param configId the external system config id
   * @param groupId the native group id
   * @param userId the native user id
   */
  public static void removeGroupMember(String configId, String groupId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "removeGroupMember");
    debugMap.put("groupId", groupId);
    debugMap.put("userId", userId);
    long startNanos = System.nanoTime();

    try {
      if (StringUtils.isBlank(groupId) || StringUtils.isBlank(userId)) {
        throw new RuntimeException("group id and user id are required for removeGroupMember");
      }
      executeMethod(debugMap, "removeGroupMember", "DELETE", configId,
          "/2.0/groups/" + GrouperUtil.escapeUrlEncode(groupId) + "/members/" + GrouperUtil.escapeUrlEncode(userId),
          GrouperUtil.toSet(200, 404), new int[] {-1}, null);
    } finally {
      SmartsheetLog.smartsheetLog(debugMap, startNanos);
    }
  }

  /**
   * The smartsheetPlanId of a Smartsheet provisioner that uses this external system, so callers
   * outside a provisioner (e.g. the MCP user lookup) can read seat types. If several provisioners
   * use the external system, the first one with a plan id wins.
   * @param configId the external system config id
   * @return the plan id, or null if no provisioner on this external system has one
   */
  public static String planIdForExternalSystem(String configId) {
    if (StringUtils.isBlank(configId)) {
      return null;
    }
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    Pattern pattern = Pattern.compile("^provisioner\\.([^.]+)\\.smartsheetExternalSystemConfigId$");
    for (String propertyName : new TreeSet<String>(grouperLoaderConfig.propertyNames())) {
      Matcher matcher = pattern.matcher(propertyName);
      if (!matcher.matches() || !StringUtils.equals(configId, grouperLoaderConfig.propertyValueString(propertyName))) {
        continue;
      }
      String planId = grouperLoaderConfig.propertyValueString("provisioner." + matcher.group(1) + ".smartsheetPlanId");
      if (!StringUtils.isBlank(planId)) {
        return planId.trim();
      }
    }
    return null;
  }

  // ==================== ignore lists ====================

  /**
   * Parse a comma, whitespace, or newline separated ignore list into a lowercased set for
   * case-insensitive matching.
   * @param list the config value (may be null/blank)
   * @return the set of lowercased values (never null)
   */
  public static Set<String> parseIgnoreSet(String list) {
    Set<String> result = new LinkedHashSet<String>();
    if (!StringUtils.isBlank(list)) {
      for (String item : GrouperUtil.splitTrim(list.replace('\r', ',').replace('\n', ','), ",")) {
        if (!StringUtils.isBlank(item)) {
          result.add(item.trim().toLowerCase());
        }
      }
    }
    return result;
  }

  /**
   * @param value the email or group name to test
   * @param ignoreSet the lowercased ignore set (from {@link #parseIgnoreSet})
   * @return true if value is in the ignore set (case-insensitive)
   */
  public static boolean isIgnored(String value, Set<String> ignoreSet) {
    return value != null && ignoreSet != null && ignoreSet.contains(value.trim().toLowerCase());
  }

}
