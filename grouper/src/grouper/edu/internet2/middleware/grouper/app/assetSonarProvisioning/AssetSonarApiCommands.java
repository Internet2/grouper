package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
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
 *   <li>Auth is a plain {@code token} header, not a bearer token.</li>
 *   <li>Writes are form-encoded {@code user[...]} parameters, not JSON.</li>
 *   <li><b>Unknown query parameters are silently ignored</b> and return a normal-looking unfiltered
 *       page 1. Every filtered read here verifies the response was actually narrowed.</li>
 *   <li>Only two filters work: {@code filter=email&amp;filter_val=&lt;email&gt;} (finds inactive
 *       members too) and {@code filter=status&amp;filter_val=inactive} (the word, not 0).</li>
 *   <li>The default list returns ACTIVE members only. Deactivated members are readable by id.</li>
 *   <li><b>{@code user[external_id]} sets nothing and NULLS THE EMAIL.</b> It is never sent, and
 *       {@code user[email]} is always sent on updates as a guard.</li>
 *   <li>Creating a member whose email exists returns 403 with no member id in the body.</li>
 * </ul>
 */
public class AssetSonarApiCommands {

  /** never log the company token or a SCIM bearer key */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("token", "authorization");

  /** path of the members collection */
  public static final String MEMBERS_PATH = "/members.api";

  /** 403 body text when a create collides with an existing (possibly inactive) member */
  public static final String EMAIL_TAKEN_MESSAGE = "The email is already taken";

  /** the destructive parameter. Never sent; see the class javadoc */
  private static final String FORBIDDEN_PARAMETER = "external_id";

  /** guard against a server that ignores the page parameter and reports a huge page count */
  private static final int MAX_PAGES = 10000;

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
   * Execute an HTTP call against the AssetSonar REST API.
   * @param debugMap map to accumulate debug info
   * @param debugLabel label for provisioner call stats
   * @param httpMethodName GET, POST, PUT
   * @param configId the AssetSonar external system config id
   * @param pathAndQuery path after the base url, already url-encoded (e.g. /members.api?page=2)
   * @param allowedReturnCodes acceptable HTTP status codes
   * @param returnCode single-element array that receives the actual status code
   * @param userParams user[...] form parameters keyed by bare attribute name, or null
   * @return the raw response body, or null if blank
   */
  private static String executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String pathAndQuery, Set<Integer> allowedReturnCodes,
      int[] returnCode, Map<String, String> userParams) {

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders);

    String url = AssetSonarExternalSystem.retrieveBaseUrl(configId) + pathAndQuery;
    debugMap.put("url", url);
    debugMap.put("method", httpMethodName);

    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(httpMethodName);

    // plain "token" header -- AssetSonar does not use the Authorization: Bearer scheme on REST
    grouperHttpClient.addHeader("token", AssetSonarExternalSystem.retrieveConfigValue(configId, "apiToken", true));
    grouperHttpClient.addHeader("Accept", "application/json");

    if (userParams != null) {
      // writes are form-encoded user[...] parameters (POST and PUT), not JSON
      for (Map.Entry<String, String> userParam : filterUserParams(userParams).entrySet()) {
        grouperHttpClient.addBodyParameter("user[" + userParam.getKey() + "]", userParam.getValue());
      }
    }

    long httpCallStartMillis = System.currentTimeMillis();
    int code;
    String body;
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
    returnCode[0] = code;
    debugMap.put("responseCode", code);

    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException("Invalid return code '" + code + "', expecting: "
          + GrouperUtil.setToString(allowedReturnCodes) + ". '" + url + "' "
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
      return AssetSonarMember.fromJson(node);
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
      MembersPage membersPage = parseMembersPage(json, pathAndQuery);
      return selectEmailMatch(membersPage.getMembers(), email);
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

  /**
   * Connection test for the external system. A 200 does not mean the request was understood, so
   * this checks content:
   * <ol>
   *   <li>page 1 of the default list has a members array and total_pages</li>
   *   <li>the inactive filter returns only status=0 members (fails loudly otherwise)</li>
   *   <li>if a SCIM connector key is configured, SCIM /Users answers with it</li>
   * </ol>
   * @param configId external system config id
   * @return error messages (empty if fine)
   */
  public static List<String> testConnection(String configId) {
    List<String> errors = new ArrayList<String>();

    MembersPage activePage = retrieveMembersPage(configId, false, 1);
    if (activePage.getTotalPages() < 1 && activePage.getMembers().size() > 0) {
      errors.add("GET " + MEMBERS_PATH + "?page=1 returned members but no total_pages");
    }

    MembersPage inactivePage = retrieveMembersPage(configId, true, 1);
    for (AssetSonarMember member : inactivePage.getMembers()) {
      if (!member.isInactive()) {
        errors.add("filter=status&filter_val=inactive was IGNORED: it returned member " + member.getId()
            + " with status '" + member.getStatus() + "'. The provisioner cannot see deactivated members.");
        break;
      }
    }

    String scimConnectorKey = AssetSonarExternalSystem.retrieveConfigValue(configId, "scimConnectorKey", false);
    if (!StringUtils.isBlank(scimConnectorKey)) {
      // do not test /ServiceProviderConfig or /Schemas: both return empty in this product
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders);
      String url = AssetSonarExternalSystem.retrieveBaseUrl(configId) + "/scim/v2/Users?count=1";
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod("GET");
      grouperHttpClient.addHeader("Authorization", "Bearer " + scimConnectorKey);
      grouperHttpClient.executeRequest();
      if (grouperHttpClient.getResponseCode() != 200) {
        errors.add("SCIM " + url + " returned " + grouperHttpClient.getResponseCode() + " with the connector key");
      }
    }
    return errors;
  }

}
