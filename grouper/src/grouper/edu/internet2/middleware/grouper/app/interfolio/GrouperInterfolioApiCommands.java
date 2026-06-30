package edu.internet2.middleware.grouper.app.interfolio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Interacts with the Interfolio API.  Only the operations that work with our credentials are
 * implemented (see {@link InterfolioExternalSystem}):
 *
 *   IAM API (create / update a user):
 *     createUser  - POST {iamUrl}/iam/{databaseId}/users
 *     updateUser  - PUT  {iamUrl}/iam/{databaseId}/users/{pid}
 *
 *   byc/core API (look up users, grant / remove product access):
 *     searchUsers          - GET {bycUrl}/byc/core/tenure/{databaseId}/institutions/{databaseId}/users/search
 *     subscribeUserToRpt   - POST {bycUrl}/byc-tenure/{databaseId}/users/{pid}/subscribe
 *     subscribeUserToFs    - POST {bycUrl}/byc-search/{databaseId}/users/{pid}/subscribe
 *     unsubscribeUserFromRpt - PUT {bycUrl}/byc-tenure/{databaseId}/users/{pid}/unsubscribe
 *     unsubscribeUserFromFs  - PUT {bycUrl}/byc-search/{databaseId}/users/{pid}/unsubscribe
 *
 * Operations that are not authorized for our credentials (FAR/Faculty180, hard delete, IAM get-by-id)
 * are intentionally not implemented.
 */
public class GrouperInterfolioApiCommands {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperInterfolioApiCommands.class);

  /**
   * Create a user via the IAM API.
   * @param configId external system config id
   * @param institutionUserId UID / PennKey
   * @param samlId pennkey@upenn.edu
   * @param userType typically "internal"
   * @param firstName first name
   * @param lastName last name
   * @param email email
   * @return the created user (with its assigned pid)
   */
  public static InterfolioUser createUser(String configId, String institutionUserId, String samlId,
      String userType, String firstName, String lastName, String email) {

    String databaseId = InterfolioExternalSystem.retrieveConfigValue(configId, "databaseId");
    String iamUrl = InterfolioExternalSystem.retrieveConfigValue(configId, "iamUrl");

    ObjectNode bodyNode = GrouperUtil.jsonJacksonNode();
    bodyNode.put("institution_user_id", institutionUserId);
    bodyNode.put("saml_id", samlId);
    bodyNode.put("user_type", userType);
    bodyNode.put("first_name", firstName);
    bodyNode.put("last_name", lastName);
    bodyNode.put("email", email);

    String requestString = "/iam/" + databaseId + "/users";

    String responseBody = executeMethod("interfolioCreateUser", configId, "POST", iamUrl,
        requestString, GrouperUtil.jsonJacksonToString(bodyNode), GrouperUtil.toSet(200, 201));

    return parseIamUser(responseBody);
  }

  /**
   * Update a user via the IAM API.  The IAM PUT is a full replace, so all attributes are sent.  Note
   * institution_user_id is immutable - it must equal the user's existing UID.
   * @param configId external system config id
   * @param pid Interfolio person id of the user to update
   * @param institutionUserId UID / PennKey (must be unchanged)
   * @param samlId pennkey@upenn.edu
   * @param userType typically "internal"
   * @param firstName first name
   * @param lastName last name
   * @param email email
   * @return the updated user
   */
  public static InterfolioUser updateUser(String configId, String pid, String institutionUserId,
      String samlId, String userType, String firstName, String lastName, String email) {

    String databaseId = InterfolioExternalSystem.retrieveConfigValue(configId, "databaseId");
    String iamUrl = InterfolioExternalSystem.retrieveConfigValue(configId, "iamUrl");

    ObjectNode bodyNode = GrouperUtil.jsonJacksonNode();
    bodyNode.put("institution_user_id", institutionUserId);
    bodyNode.put("saml_id", samlId);
    bodyNode.put("user_type", userType);
    bodyNode.put("first_name", firstName);
    bodyNode.put("last_name", lastName);
    bodyNode.put("email", email);

    String requestString = "/iam/" + databaseId + "/users/" + pid;

    String responseBody = executeMethod("interfolioUpdateUser", configId, "PUT", iamUrl,
        requestString, GrouperUtil.jsonJacksonToString(bodyNode), GrouperUtil.toSet(200, 201));

    return parseIamUser(responseBody);
  }

  /**
   * Look up users by a search term (email, UID, or name) via the byc users/search endpoint.  A blank
   * term returns the whole roster (paged).
   * @param configId external system config id
   * @param searchTerm email, UID/PennKey, or name fragment (blank/null for all)
   * @param limit page size
   * @param page 1-based page number
   * @return the matching users on that page
   */
  public static List<InterfolioUser> searchUsers(String configId, String searchTerm, int limit, int page) {

    String databaseId = InterfolioExternalSystem.retrieveConfigValue(configId, "databaseId");
    String bycUrl = InterfolioExternalSystem.retrieveConfigValue(configId, "bycUrl");

    String requestString = "/byc/core/tenure/" + databaseId + "/institutions/" + databaseId
        + "/users/search?limit=" + limit + "&page=" + page + "&search=" + (searchTerm == null ? "" : searchTerm);

    String responseBody = executeMethod("interfolioSearchUsers", configId, "GET", bycUrl,
        requestString, null, GrouperUtil.toSet(200));

    List<InterfolioUser> result = new ArrayList<InterfolioUser>();
    JsonNode rootNode = GrouperUtil.jsonJacksonNode(responseBody);
    JsonNode resultsNode = rootNode == null ? null : rootNode.get("results");
    if (resultsNode instanceof ArrayNode) {
      for (JsonNode userNode : resultsNode) {
        InterfolioUser user = new InterfolioUser();
        user.setPid(GrouperUtil.jsonJacksonGetString(userNode, "pid"));
        JsonNode bycIdNode = userNode.get("id");
        user.setBycId(bycIdNode == null || bycIdNode.isNull() ? null : bycIdNode.asText());
        user.setFirstName(GrouperUtil.jsonJacksonGetString(userNode, "first_name"));
        user.setLastName(GrouperUtil.jsonJacksonGetString(userNode, "last_name"));
        user.setEmail(GrouperUtil.jsonJacksonGetString(userNode, "email"));
        result.add(user);

        // generic provisioner sync back: capture the raw user JSON (no-op unless this is an
        // Interfolio provisioner with sync-back on)
        InterfolioProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(userNode);
      }
    }
    return result;
  }

  /**
   * Subscribe a user to RPT (grant RPT access).
   * @param configId external system config id
   * @param pid Interfolio person id
   */
  public static void subscribeUserToRpt(String configId, String pid) {
    subscribeUser(configId, "byc-tenure", pid);
  }

  /**
   * Subscribe a user to FS (grant Faculty Search access).
   * @param configId external system config id
   * @param pid Interfolio person id
   */
  public static void subscribeUserToFs(String configId, String pid) {
    subscribeUser(configId, "byc-search", pid);
  }

  /**
   * Unsubscribe a user from RPT (remove RPT access).
   * @param configId external system config id
   * @param pid Interfolio person id
   */
  public static void unsubscribeUserFromRpt(String configId, String pid) {
    unsubscribeUser(configId, "byc-tenure", pid);
  }

  /**
   * Unsubscribe a user from FS (remove Faculty Search access).
   * @param configId external system config id
   * @param pid Interfolio person id
   */
  public static void unsubscribeUserFromFs(String configId, String pid) {
    unsubscribeUser(configId, "byc-search", pid);
  }

  /**
   * Subscribe a user to a byc product (POST /{service}/{databaseId}/users/{pid}/subscribe).
   * @param configId external system config id
   * @param service "byc-tenure" (RPT) or "byc-search" (FS)
   * @param pid Interfolio person id
   */
  private static void subscribeUser(String configId, String service, String pid) {
    String databaseId = InterfolioExternalSystem.retrieveConfigValue(configId, "databaseId");
    String bycUrl = InterfolioExternalSystem.retrieveConfigValue(configId, "bycUrl");
    String requestString = "/" + service + "/" + databaseId + "/users/" + pid + "/subscribe";
    executeMethod("interfolioSubscribe_" + service, configId, "POST", bycUrl, requestString, null,
        GrouperUtil.toSet(200, 201));
  }

  /**
   * Unsubscribe a user from a byc product (PUT /{service}/{databaseId}/users/{pid}/unsubscribe).
   * @param configId external system config id
   * @param service "byc-tenure" (RPT) or "byc-search" (FS)
   * @param pid Interfolio person id
   */
  private static void unsubscribeUser(String configId, String service, String pid) {
    String databaseId = InterfolioExternalSystem.retrieveConfigValue(configId, "databaseId");
    String bycUrl = InterfolioExternalSystem.retrieveConfigValue(configId, "bycUrl");
    String requestString = "/" + service + "/" + databaseId + "/users/" + pid + "/unsubscribe";
    executeMethod("interfolioUnsubscribe_" + service, configId, "PUT", bycUrl, requestString, null,
        GrouperUtil.toSet(200, 201));
  }

  /**
   * Parse an IAM user response into an InterfolioUser.  The IAM API returns the identifier as "pid".
   * @param responseBody the JSON response
   * @return the parsed user
   */
  private static InterfolioUser parseIamUser(String responseBody) {
    JsonNode node = GrouperUtil.jsonJacksonNode(responseBody);
    InterfolioUser user = new InterfolioUser();
    JsonNode pidNode = node == null ? null : node.get("pid");
    user.setPid(pidNode == null || pidNode.isNull() ? null : pidNode.asText());
    user.setInstitutionUserId(GrouperUtil.jsonJacksonGetString(node, "institution_user_id"));
    user.setUserType(GrouperUtil.jsonJacksonGetString(node, "user_type"));
    user.setFirstName(GrouperUtil.jsonJacksonGetString(node, "first_name"));
    user.setLastName(GrouperUtil.jsonJacksonGetString(node, "last_name"));
    user.setEmail(GrouperUtil.jsonJacksonGetString(node, "email"));
    return user;
  }

  /**
   * Execute an HMAC-signed Interfolio API call.
   * @param debugLabel label for command timing stats
   * @param configId external system config id
   * @param httpMethodName HTTP verb
   * @param baseUrl host base (iamUrl or bycUrl)
   * @param requestString path + query that follows the host (signed verbatim)
   * @param body request body, or null
   * @param allowedReturnCodes acceptable HTTP status codes
   * @return the response body
   */
  private static String executeMethod(String debugLabel, String configId, String httpMethodName,
      String baseUrl, String requestString, String body, Set<Integer> allowedReturnCodes) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTimeNanos = System.nanoTime();

    try {

      // strip a trailing slash from the base so base + requestString does not double the slash
      String base = baseUrl;
      if (base.endsWith("/")) {
        base = base.substring(0, base.length() - 1);
      }
      String url = base + requestString;
      debugMap.put("url", url);
      debugMap.put("method", httpMethodName);

      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(httpMethodName);
      grouperHttpClient.addHeader("Accept", "application/json");
      if (StringUtils.isNotBlank(body)) {
        grouperHttpClient.addHeader("Content-Type", "application/json");
        grouperHttpClient.assignBody(body);
      }

      // sign the path + query verbatim
      InterfolioExternalSystem.attachInterfolioHmacHeaders(configId, grouperHttpClient, httpMethodName, requestString);

      long httpCallStartMillis = System.currentTimeMillis();
      try {
        grouperHttpClient.executeRequest();
      } finally {
        GrouperProvisioner.incrementCommandsCallsStats(debugLabel, 1,
            System.currentTimeMillis() - httpCallStartMillis);
      }

      int code = grouperHttpClient.getResponseCode();
      String responseBody = grouperHttpClient.getResponseBody();
      debugMap.put("code", code);

      if (!allowedReturnCodes.contains(code)) {
        throw new RuntimeException("Invalid return code '" + code + "', expecting: "
            + GrouperUtil.setToString(allowedReturnCodes) + ". '" + url + "' " + responseBody);
      }

      return responseBody;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperUtil.getFullStackTrace(re));
      throw re;
    } finally {
      InterfolioLog.interfolioLog(debugMap, startTimeNanos);
    }
  }

}
