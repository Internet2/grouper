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
    // send email lowercased (compare is case-insensitive; keep the stored value normalized)
    bodyNode.put("email", StringUtils.lowerCase(email));

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
    // send email lowercased (compare is case-insensitive; keep the stored value normalized)
    bodyNode.put("email", StringUtils.lowerCase(email));

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
   * Retrieve the ENTIRE institution roster in one call via the byc csv_report endpoint.  Unlike
   * users/search (which omits it), this report includes the UID (institution_user_id / PennKey) - the
   * stable key the provisioner matches on - along with email, name, user_type, and SSO ID (saml_id).
   * It does NOT include the pid, so returned users have a null pid (resolve it lazily with
   * {@link #resolvePid} only when a write is needed).  Rows without a UID (external/API accounts) are
   * skipped - they are not part of the faculty population.
   *
   * GET {bycUrl}/byc/core/tenure/{databaseId}/users/csv_report
   *
   * @param configId external system config id
   * @return all Interfolio users keyed by institution_user_id
   */
  public static List<InterfolioUser> retrieveAllUsersViaCsvReport(String configId) {

    String databaseId = InterfolioExternalSystem.retrieveConfigValue(configId, "databaseId");
    String bycUrl = InterfolioExternalSystem.retrieveConfigValue(configId, "bycUrl");

    String requestString = "/byc/core/tenure/" + databaseId + "/users/csv_report";

    // csv_report is known to be unreliable - Interfolio frequently drops the connection mid-response
    // on the large full-institution report (comes back as a 400 "Connection reset by peer"), so retry
    // several times before giving up.  Each attempt is a fresh HMAC-signed request.
    int maxAttempts = 10;
    String responseBody = null;
    RuntimeException lastException = null;
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      try {
        // live progress: this is one big, slow, flaky download that can retry for minutes; report the
        // attempt so an operator can tell a retrying job from a hung one.  Uses the existing
        // thread-scoped current provisioner (same one incrementCommandsCallsStats uses); null off a run.
        GrouperProvisioner currentProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
        if (currentProvisioner != null) {
          currentProvisioner.assignProgressLabelTarget(
              "retrieving all users from target (csv_report attempt " + attempt + " of " + maxAttempts + ")");
        }
        responseBody = executeMethod("interfolioCsvReport", configId, "GET", bycUrl,
            requestString, null, GrouperUtil.toSet(200));
        lastException = null;
        break;
      } catch (RuntimeException re) {
        lastException = re;
        LOG.warn("Interfolio csv_report attempt " + attempt + " of " + maxAttempts + " failed: " + re.getMessage());
        if (attempt < maxAttempts) {
          GrouperUtil.sleep(5000);
        }
      }
    }
    if (lastException != null) {
      throw new RuntimeException("Interfolio csv_report failed after " + maxAttempts + " attempts", lastException);
    }

    List<InterfolioUser> result = new ArrayList<InterfolioUser>();
    if (StringUtils.isBlank(responseBody)) {
      return result;
    }

    // strip any UTF-8 BOM(s) - Interfolio's csv_report prefixes the header with more than one BOM
    // char, which would otherwise leave the first column header ("First Name") unmatchable - then
    // split into lines (the report uses CRLF)
    String body = responseBody.replace("\uFEFF", "");
    String[] lines = body.split("\r\n|\r|\n");

    List<String> header = null;
    for (String line : lines) {
      if (StringUtils.isBlank(line)) {
        continue;
      }
      List<String> fields = parseCsvLine(line);
      if (header == null) {
        header = fields;
        continue;
      }
      InterfolioUser user = new InterfolioUser();
      user.setInstitutionUserId(csvValue(header, fields, "UID"));
      user.setSamlId(csvValue(header, fields, "SSO ID"));
      user.setUserType(csvValue(header, fields, "User Type"));
      user.setFirstName(csvValue(header, fields, "First Name"));
      user.setLastName(csvValue(header, fields, "Last Name"));
      user.setEmail(csvValue(header, fields, "Email"));

      // only rows with a UID can be matched on institution_user_id; external/API accounts have a blank
      // UID and are outside our faculty population
      if (StringUtils.isBlank(user.getInstitutionUserId())) {
        continue;
      }
      result.add(user);
    }
    return result;
  }

  /**
   * Resolve the Interfolio pid for a user we need to write to, given the UID (and email to
   * disambiguate).  csv_report does not return the pid, so when the provisioner updates or
   * deprovisions an already-existing user we look the pid up via byc users/search on the UID.
   * @param configId external system config id
   * @param institutionUserId the UID / PennKey to look up
   * @param email the expected email, used to disambiguate when the search returns more than one hit
   * @return the pid, or null if it cannot be resolved unambiguously
   */
  public static String resolvePid(String configId, String institutionUserId, String email) {
    if (StringUtils.isBlank(institutionUserId)) {
      return null;
    }
    List<InterfolioUser> matches = searchUsers(configId, institutionUserId, 25, 1);
    if (GrouperUtil.length(matches) == 1) {
      return matches.get(0).getPid();
    }
    // more than one hit (the UID matched a substring of another user's email/name) - use the email
    for (InterfolioUser match : GrouperUtil.nonNull(matches)) {
      if (StringUtils.isNotBlank(email) && StringUtils.equalsIgnoreCase(email, match.getEmail())) {
        return match.getPid();
      }
    }
    return null;
  }

  /**
   * Look up a value in a parsed CSV row by its header column name (case-insensitive).
   * @param header the parsed header row
   * @param fields the parsed data row
   * @param columnName the column name to fetch
   * @return the trimmed value, or null if the column is absent or the row is short
   */
  private static String csvValue(List<String> header, List<String> fields, String columnName) {
    for (int i = 0; i < header.size(); i++) {
      if (StringUtils.equalsIgnoreCase(StringUtils.trim(header.get(i)), columnName)) {
        return i < fields.size() ? StringUtils.trimToNull(fields.get(i)) : null;
      }
    }
    return null;
  }

  /**
   * Parse one CSV line into fields, honoring double-quoted fields (which may contain commas) and
   * doubled "" escaped quotes.  Interfolio's csv_report quotes the Roles/Unit columns that contain
   * commas, so a naive comma split would misalign the columns.
   * @param line one CSV line (no trailing newline)
   * @return the parsed field values
   */
  private static List<String> parseCsvLine(String line) {
    List<String> fields = new ArrayList<String>();
    StringBuilder current = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char c = line.charAt(i);
      if (inQuotes) {
        if (c == '"') {
          if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
            current.append('"');
            i++;
          } else {
            inQuotes = false;
          }
        } else {
          current.append(c);
        }
      } else if (c == '"') {
        inQuotes = true;
      } else if (c == ',') {
        fields.add(current.toString());
        current.setLength(0);
      } else {
        current.append(c);
      }
    }
    fields.add(current.toString());
    return fields;
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
