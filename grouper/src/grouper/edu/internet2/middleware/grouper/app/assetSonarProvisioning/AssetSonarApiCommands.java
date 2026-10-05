package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningConfiguration;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Static wrappers around the AssetSonar native REST API for members.
 *
 * <p>Two url shapes, and confusing them causes real mistakes because the member url accepts
 * POST as an update:</p>
 * <pre>
 *   /members.api           COLLECTION url. GET lists 25/page. POST creates.
 *   /members/&lt;id&gt;.api      MEMBER url. GET reads one. PUT updates one.
 * </pre>
 *
 * <p>Behaviors established against a live tenant (EZO documents almost none of this), each of
 * which looks like pointless defensive code without the context:</p>
 * <ul>
 *   <li>Auth is a plain {@code token} header, not a bearer token. The connection is a generic
 *       WsBearerToken external system configured with {@code httpHeader = token} and
 *       {@code prependBearerTokenPrefix = false}, so the token is stored in
 *       {@code accessTokenPassword} (encrypted, masked) and proxy / delay settings apply.</li>
 *   <li>Writes are form-encoded {@code user[...]} parameters, not JSON.</li>
 *   <li><b>Unknown query parameters are silently ignored</b> and return a normal-looking unfiltered
 *       page 1. Every filtered read here verifies the response was actually narrowed.</li>
 *   <li>Only two filters work: {@code filter=email&amp;filter_val=&lt;email&gt;} (finds inactive
 *       members too) and {@code filter=status&amp;filter_val=inactive} (the word, not 0).</li>
 *   <li>The default list returns ACTIVE members only. Deactivated members are readable by id.</li>
 *   <li><b>{@code user[external_id]} sets nothing and NULLS THE EMAIL.</b> It is never sent, and
 *       {@code user[email]} is always sent on updates as a guard.</li>
 *   <li>Creating a member whose email exists is refused with no member id in the body. For an
 *       active member that is HTTP 403; for an <b>inactive</b> member it is <b>HTTP 200</b> with
 *       {@code {"errors":{...},"status":403}} in the body. Any 2xx whose body has "errors" is
 *       therefore treated as the body's status (see executeMethod).</li>
 *   <li>A create without {@code user[role_id]} is refused: 400 "Role Id is invalid".</li>
 *   <li>Under sustained writing the gateway occasionally answers <b>502</b> ("Incomplete response
 *       received from application") for a call that actually committed (~0.5% in production). Every
 *       call is retried on 502/503/504 with backoff (GRP-7434). That is safe: reads and updates are
 *       idempotent, and a repeated create of a committed member gets the duplicate-email 403, which
 *       the DAO turns into lookup-by-email and reactivate.</li>
 * </ul>
 */
public class AssetSonarApiCommands {

  /** never log the company token */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("token", "authorization");

  /** path of the members collection */
  public static final String MEMBERS_PATH = "/members.api";

  /** 403 body text when a create collides with an existing (possibly inactive) member */
  public static final String EMAIL_TAKEN_MESSAGE = "The email is already taken";

  /** the destructive parameter. Never sent; see the class javadoc */
  private static final String FORBIDDEN_PARAMETER = "external_id";

  /** guard against a server that ignores the page parameter and reports a huge page count */
  private static final int MAX_PAGES = 10000;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(AssetSonarApiCommands.class);

  /** gateway / availability codes that are retried; 4xx are real rejections and never retried */
  public static final Set<Integer> RETRY_RETURN_CODES = GrouperUtil.toSet(502, 503, 504);

  /** retries after the first attempt when no AssetSonar provisioner is running (see retrySettings) */
  public static final int DEFAULT_RETRY_COUNT = 3;

  /** first retry sleep when no AssetSonar provisioner is running; doubles each retry */
  public static final int DEFAULT_RETRY_SLEEP_MILLIS = 1000;

  /** most retries allowed whatever assetSonarRetryCount is, the backoff doubles so more is never useful */
  public static final int MAX_RETRY_COUNT = 10;

  /** longest sleep between retries whatever assetSonarRetrySleepMillis is */
  public static final long MAX_RETRY_SLEEP_MILLIS = 60000L;

  /** tests only: overrides the retry sleep so retry tests do not take seconds; null normally */
  static Integer retrySleepMillisForTests = null;

  /**
   * One page of a member list read.
   */
  public static class MembersPage {

    /** members on this page (never null) */
    private List<AssetSonarMember> members = new ArrayList<AssetSonarMember>();

    /** total_pages as reported by the API */
    private int totalPages;

    public List<AssetSonarMember> getMembers() {
      return this.members;
    }

    public int getTotalPages() {
      return this.totalPages;
    }
  }

  /**
   * @param configId WsBearerToken external system config id
   * @return the tenant root url (the external system endpoint) with no trailing slash
   */
  public static String retrieveBaseUrl(String configId) {
    return GrouperUtil.stripLastSlashIfExists(GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired("grouper.wsBearerToken." + configId + ".endpoint"));
  }

  /**
   * @param code the HTTP status
   * @param body the response body
   * @return the error status a 2xx JSON body carries ("status", else 400 if it has "errors"), or -1
   */
  static int errorStatusFromBody(int code, String body) {
    if (code < 200 || code > 299 || body == null || !body.trim().startsWith("{") || !body.contains("\"errors\"")) {
      return -1;
    }
    try {
      JsonNode node = GrouperUtil.jsonJacksonNode(body);
      if (node == null || !node.has("errors")) {
        return -1;
      }
      Integer status = GrouperUtil.jsonJacksonGetInteger(node, "status");
      return status != null && status >= 400 ? status : 400;
    } catch (Exception e) {
      // not JSON after all: leave the code alone
      return -1;
    }
  }

  /**
   * Retry count and first sleep for the running AssetSonar provisioner (assetSonarRetryCount,
   * assetSonarRetrySleepMillis), or the defaults when called outside one (e.g. MCP lookups).
   * @return {retryCount, retrySleepMillis}
   */
  static int[] retrySettings() {
    int retryCount = DEFAULT_RETRY_COUNT;
    int retrySleepMillis = DEFAULT_RETRY_SLEEP_MILLIS;
    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
    GrouperProvisioningConfiguration configuration = grouperProvisioner == null ? null
        : grouperProvisioner.retrieveGrouperProvisioningConfiguration();
    if (configuration instanceof AssetSonarProvisionerConfiguration) {
      retryCount = ((AssetSonarProvisionerConfiguration) configuration).getAssetSonarRetryCount();
      retrySleepMillis = ((AssetSonarProvisionerConfiguration) configuration).getAssetSonarRetrySleepMillis();
    }
    if (retrySleepMillisForTests != null) {
      retrySleepMillis = retrySleepMillisForTests;
    }
    // cap both so a large config value cannot back off for hours (or overflow the shift)
    return new int[] {Math.min(MAX_RETRY_COUNT, Math.max(0, retryCount)), Math.max(0, retrySleepMillis)};
  }

  /**
   * Execute an HTTP call against the AssetSonar REST API, retrying on 502/503/504.
   * @param debugMap map to accumulate debug info
   * @param debugLabel label for provisioner call stats
   * @param httpMethodName GET, POST, PUT
   * @param configId the WsBearerToken external system config id
   * @param pathAndQuery path after the base url, already url-encoded (e.g. /members.api?page=2)
   * @param allowedReturnCodes acceptable HTTP status codes
   * @param returnCode single-element array that receives the actual status code
   * @param userParams user[...] form parameters keyed by bare attribute name, or null
   * @return the raw response body, or null if blank
   */
  private static String executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String pathAndQuery, Set<Integer> allowedReturnCodes,
      int[] returnCode, Map<String, String> userParams) {

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

      // the external system adds the "token" header (httpHeader=token, prependBearerTokenPrefix=false),
      // proxy settings, and delayAfterEachCallInMs
      WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(
          grouperHttpClient, configId, GrouperLoaderConfig.retrieveConfig(), debugMap);

      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(httpMethodName);
      grouperHttpClient.addHeader("Accept", "application/json");

      if (userParams != null) {
        // writes are form-encoded user[...] parameters (POST and PUT), not JSON
        for (Map.Entry<String, String> userParam : filterUserParams(userParams).entrySet()) {
          grouperHttpClient.addBodyParameter("user[" + userParam.getKey() + "]", userParam.getValue());
        }
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

      // the gateway lost the response (the call usually committed): back off and try again.  WARN, not
      // ERROR, since the retry normally succeeds and the log should not imply a failure that is not one
      long sleepMillis = Math.min(MAX_RETRY_SLEEP_MILLIS, (long) retrySleepMillis << attempt);
      LOG.warn("AssetSonar " + httpMethodName + " '" + url + "' returned " + code + ", retry "
          + (attempt + 1) + " of " + retryCount + " in " + sleepMillis + "ms");
      debugMap.put("retries", attempt + 1);
      GrouperUtil.sleep(sleepMillis);
    }

    // AssetSonar sometimes reports an error inside a 200, e.g. creating an email held by an INACTIVE
    // member: HTTP 200 {"errors":{"base":["The email is already taken by an Inactive Member..."]},
    // "status":403}.  A success body never has "errors", so use the status it carries
    int statusFromBody = errorStatusFromBody(code, body);
    if (statusFromBody != -1) {
      debugMap.put("httpCode", code);
      code = statusFromBody;
    }

    returnCode[0] = code;
    debugMap.put("responseCode", code);

    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException("Invalid return code '" + code + "', expecting: "
          + GrouperUtil.setToString(allowedReturnCodes) + ". '" + url + "' "
          + (RETRY_RETURN_CODES.contains(code) ? "(after " + retryCount + " retries) " : "")
          + StringUtils.abbreviate(body, 2000));
    }

    return StringUtils.isBlank(body) ? null : body;
  }

  /**
   * The user[...] parameters that will actually be sent.
   *
   * <p>Blank values are skipped rather than sent: a data-quality gap in the source must become a
   * no-op, not destroy a good target value. external_id is refused outright because the API
   * responds to it by nulling the member's email.</p>
   * @param userParams attribute name to value
   * @return the non-blank attribute name to value, in order
   */
  static Map<String, String> filterUserParams(Map<String, String> userParams) {
    Map<String, String> result = new LinkedHashMap<String, String>();
    for (String attributeName : userParams.keySet()) {
      if (FORBIDDEN_PARAMETER.equals(attributeName)) {
        throw new RuntimeException("Refusing to send user[" + FORBIDDEN_PARAMETER
            + "]: AssetSonar nulls the member's email when it receives it");
      }
      String value = userParams.get(attributeName);
      if (!StringUtils.isBlank(value)) {
        result.put(attributeName, value);
      }
    }
    return result;
  }

  /**
   * @param value the value
   * @return the url-encoded value
   */
  private static String urlEncode(String value) {
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse a JSON body.
   */
  private static JsonNode parseJson(String json, String url) {
    try {
      return GrouperUtil.jsonJacksonNode(json);
    } catch (Exception e) {
      // UI-only routes answer HTML, which is a sign of a wrong url rather than a data problem
      throw new RuntimeException("Error parsing AssetSonar JSON from '" + url + "': "
          + StringUtils.abbreviate(json, 500), e);
    }
  }

  /**
   * Read one page of members. With inactive=false this is the default list (ACTIVE members only).
   * @param configId external system config id
   * @param inactive true to read filter=status&amp;filter_val=inactive
   * @param page 1-based page number
   * @return the page
   */
  public static MembersPage retrieveMembersPage(String configId, boolean inactive, int page) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveMembersPage");
    debugMap.put("inactive", inactive);
    debugMap.put("page", page);
    long startNanos = System.nanoTime();
    try {
      String pathAndQuery = MEMBERS_PATH + "?"
          + (inactive ? "filter=status&filter_val=inactive&" : "") + "page=" + page;
      String json = executeMethod(debugMap, "retrieveMembersPage", "GET", configId, pathAndQuery,
          GrouperUtil.toSet(200), new int[] {-1}, null);
      MembersPage membersPage = parseMembersPage(json, pathAndQuery);
      debugMap.put("count", membersPage.getMembers().size());
      debugMap.put("totalPages", membersPage.getTotalPages());
      return membersPage;
    } finally {
      AssetSonarLog.assetSonarLog(debugMap, startNanos);
    }
  }

  /**
   * Parse a list response: {"members":[...],"total_pages":N}.
   */
  private static MembersPage parseMembersPage(String json, String url) {
    MembersPage membersPage = new MembersPage();
    if (json == null) {
      return membersPage;
    }
    JsonNode root = parseJson(json, url);
    JsonNode membersNode = root.get("members");
    if (membersNode == null || !membersNode.isArray()) {
      throw new RuntimeException("Expected a members array from '" + url + "': " + StringUtils.abbreviate(json, 500));
    }
    for (JsonNode memberNode : membersNode) {
      membersPage.members.add(AssetSonarMember.fromJson(memberNode));
      // sync-back: every list page (active, inactive, email lookup) passes through here
      AssetSonarProvisioningTargetNativeSync.captureMemberJsonFromCurrentProvisioner(memberNode);
    }
    membersPage.totalPages = GrouperUtil.intValue(GrouperUtil.jsonJacksonGetInteger(root, "total_pages"), 0);
    return membersPage;
  }

  /**
   * Walk every page of the active list and, optionally, every page of the inactive list.
   *
   * <p>The inactive set matters: if only active members are read, a deactivated member looks
   * absent, Grouper drops its sync row after removeSyncRowsAfterSecondsOutOfTarget (7 days) and
   * loses the member id, and a later create collides with the hidden record.</p>
   *
   * <p>The inactive filter is undocumented. If it is ever ignored, the response is the active list
   * again, so any status=1 member in it fails the read loudly rather than silently selecting the
   * active list twice.</p>
   *
   * @param configId external system config id
   * @param includeInactive true to also read the inactive members
   * @return members by id (never null)
   */
  public static Map<String, AssetSonarMember> retrieveAllMembers(String configId, boolean includeInactive) {
    Map<String, AssetSonarMember> result = new LinkedHashMap<String, AssetSonarMember>();
    readAllPages(configId, false, result);
    if (includeInactive) {
      readAllPages(configId, true, result);
    }
    return result;
  }

  /**
   * Read all pages of one list into the result map (later reads replace earlier ones by id).
   */
  private static void readAllPages(String configId, boolean inactive, Map<String, AssetSonarMember> result) {
    int totalPages = 1;
    for (int page = 1; page <= totalPages; page++) {
      if (page > MAX_PAGES) {
        throw new RuntimeException("AssetSonar reported more than " + MAX_PAGES + " pages, aborting");
      }
      MembersPage membersPage = retrieveMembersPage(configId, inactive, page);
      totalPages = membersPage.getTotalPages();
      for (AssetSonarMember member : membersPage.getMembers()) {
        if (inactive) {
          assertInactiveFilterHonored(member);
        }
        if (!StringUtils.isBlank(member.getId())) {
          result.put(member.getId(), member);
        }
      }
      // live progress: a full read is many 25-member pages (about 90s for 4,400 members). The API
      // reports total_pages, so show page N of M. Uses the thread-scoped current provisioner; null off a run.
      GrouperProvisioner currentProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
      if (currentProvisioner != null) {
        currentProvisioner.assignProgressLabelTarget("retrieving " + (inactive ? "inactive" : "active")
            + " members from target: page " + page + " of " + Math.max(totalPages, page)
            + ", " + result.size() + " so far");
      }
    }
  }

  /**
   * The inactive filter must only return status=0. Anything else means it was silently ignored.
   * @param member a member from an inactive-filter page
   */
  static void assertInactiveFilterHonored(AssetSonarMember member) {
    if (!member.isInactive()) {
      throw new RuntimeException("AssetSonar ignored filter=status&filter_val=inactive: it returned member "
          + member.getId() + " with status '" + member.getStatus() + "'. Deactivated members can no longer be "
          + "enumerated; do not run the provisioner until this is resolved with EZO.");
    }
  }

  /**
   * Read one member by id. Works for deactivated members too.
   * @param configId external system config id
   * @param memberId native member id
   * @return the member, or null if not found
   */
  public static AssetSonarMember retrieveMemberById(String configId, String memberId) {
    JsonNode node = retrieveMemberJsonById(configId, memberId);
    return node == null ? null : AssetSonarMember.fromJson(node);
  }

  /**
   * Read one member by id as the raw API JSON (all ~110 fields). Works for deactivated members too.
   * The raw record carries secrets (e.g. webstore_authentication_token), so callers that show it to
   * people must allow-list fields.
   * @param configId external system config id
   * @param memberId native member id
   * @return the member JSON, or null if not found
   */
  public static JsonNode retrieveMemberJsonById(String configId, String memberId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveMemberById");
    debugMap.put("memberId", memberId);
    long startNanos = System.nanoTime();
    try {
      if (StringUtils.isBlank(memberId)) {
        return null;
      }
      int[] returnCode = new int[] {-1};
      String pathAndQuery = "/members/" + urlEncode(memberId) + ".api";
      String json = executeMethod(debugMap, "retrieveMemberById", "GET", configId, pathAndQuery,
          GrouperUtil.toSet(200, 404), returnCode, null);
      if (returnCode[0] == 404 || json == null) {
        return null;
      }
      JsonNode node = parseJson(json, pathAndQuery);
      if (node.get(AssetSonarMember.ATTR_ID) == null) {
        return null;
      }
      // sync-back: the read by id (also how the drain re-reads a written member)
      AssetSonarProvisioningTargetNativeSync.captureMemberJsonFromCurrentProvisioner(node);
      return node;
    } finally {
      AssetSonarLog.assetSonarLog(debugMap, startNanos);
    }
  }

  /**
   * Look a member up by email with filter=email. Finds inactive members too, which is what makes
   * the 403-on-create fallback work.
   *
   * <p>The response is verified to be narrowed: if any returned member has a different email the
   * filter was ignored and an unfiltered page came back, which must not be mistaken for a match.</p>
   * @param configId external system config id
   * @param email the email (case-insensitive)
   * @return the member, or null if none
   */
  public static AssetSonarMember retrieveMemberByEmail(String configId, String email) {
    JsonNode node = retrieveMemberJsonByEmail(configId, email);
    return node == null ? null : AssetSonarMember.fromJson(node);
  }

  /**
   * Look a member up by email as the raw API JSON, with the same narrowing check as
   * {@link #retrieveMemberByEmail}. See {@link #retrieveMemberJsonById} about secrets in the record.
   * @param configId external system config id
   * @param email the email (case-insensitive)
   * @return the member JSON, or null if none
   */
  public static JsonNode retrieveMemberJsonByEmail(String configId, String email) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveMemberByEmail");
    debugMap.put("email", email);
    long startNanos = System.nanoTime();
    try {
      if (StringUtils.isBlank(email)) {
        return null;
      }
      String pathAndQuery = MEMBERS_PATH + "?filter=email&filter_val=" + urlEncode(email.trim());
      String json = executeMethod(debugMap, "retrieveMemberByEmail", "GET", configId, pathAndQuery,
          GrouperUtil.toSet(200), new int[] {-1}, null);
      // parseMembersPage validates the envelope and captures sync-back; its members line up with the
      // raw nodes of the same array
      MembersPage membersPage = parseMembersPage(json, pathAndQuery);
      AssetSonarMember match = selectEmailMatch(membersPage.getMembers(), email);
      if (match == null) {
        return null;
      }
      return parseJson(json, pathAndQuery).get("members").get(membersPage.getMembers().indexOf(match));
    } finally {
      AssetSonarLog.assetSonarLog(debugMap, startNanos);
    }
  }

  /**
   * Pick the email match out of a filter=email response, failing if the response was not narrowed.
   * @param members the returned members
   * @param email the email searched for
   * @return the single match, or null
   */
  static AssetSonarMember selectEmailMatch(List<AssetSonarMember> members, String email) {
    AssetSonarMember match = null;
    for (AssetSonarMember member : GrouperUtil.nonNull(members)) {
      if (!StringUtils.equalsIgnoreCase(StringUtils.trim(member.getEmail()), email.trim())) {
        throw new RuntimeException("AssetSonar ignored filter=email: searching for '" + email
            + "' returned member " + member.getId() + " with email '" + member.getEmail() + "'");
      }
      if (match != null) {
        throw new RuntimeException("AssetSonar returned more than one member for email '" + email + "'");
      }
      match = member;
    }
    return match;
  }

  /**
   * Create a member via POST /members.api.
   * @param configId external system config id
   * @param userParams attribute name to value (blank values are skipped)
   * @return the new member id, or null if the email is already taken (403). The 403 carries no
   *   id, so the caller looks the member up by email.
   */
  public static String createMember(String configId, Map<String, String> userParams) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createMember");
    debugMap.put("email", userParams.get(AssetSonarMember.ATTR_EMAIL));
    long startNanos = System.nanoTime();
    try {
      int[] returnCode = new int[] {-1};
      String json = executeMethod(debugMap, "createMember", "POST", configId, MEMBERS_PATH,
          GrouperUtil.toSet(200, 201, 403), returnCode, userParams);

      if (returnCode[0] == 403) {
        // only the duplicate-email 403 is expected; anything else (e.g. a token without rights) fails
        if (json != null && json.contains(EMAIL_TAKEN_MESSAGE)) {
          debugMap.put("emailTaken", true);
          return null;
        }
        throw new RuntimeException("AssetSonar refused to create member: 403 " + StringUtils.abbreviate(json, 1000));
      }

      // {"message":"Member created.","member_id":N}
      String memberId = json == null ? null
          : GrouperUtil.jsonJacksonGetString(parseJson(json, MEMBERS_PATH), "member_id");
      if (StringUtils.isBlank(memberId)) {
        throw new RuntimeException("AssetSonar create returned no member_id: " + StringUtils.abbreviate(json, 1000));
      }
      debugMap.put("memberId", memberId);
      return memberId;
    } finally {
      AssetSonarLog.assetSonarLog(debugMap, startNanos);
    }
  }

  /**
   * Update a member via PUT /members/&lt;id&gt;.api. Partial updates are safe: omitted fields are
   * unchanged.
   * @param configId external system config id
   * @param memberId native member id
   * @param userParams attribute name to value; MUST include email (guard against the external_id
   *   clearing behavior); blank values are skipped
   */
  public static void updateMember(String configId, String memberId, Map<String, String> userParams) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateMember");
    debugMap.put("memberId", memberId);
    debugMap.put("attributes", userParams.keySet());
    long startNanos = System.nanoTime();
    try {
      if (StringUtils.isBlank(memberId)) {
        throw new RuntimeException("member id is required for updateMember");
      }
      if (StringUtils.isBlank(userParams.get(AssetSonarMember.ATTR_EMAIL))) {
        throw new RuntimeException("email is required on every AssetSonar update (member " + memberId + ")");
      }
      executeMethod(debugMap, "updateMember", "PUT", configId, "/members/" + urlEncode(memberId) + ".api",
          GrouperUtil.toSet(200, 201, 204), new int[] {-1}, userParams);
    } finally {
      AssetSonarLog.assetSonarLog(debugMap, startNanos);
    }
  }

}
