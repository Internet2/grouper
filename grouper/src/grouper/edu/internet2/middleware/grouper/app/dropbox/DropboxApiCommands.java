package edu.internet2.middleware.grouper.app.dropbox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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

/**
 * Low-level HTTP command layer for the Dropbox Business Team API.
 *
 * <p>This class mirrors {@code TrueFoundryApiCommands}: every method opens a fresh
 * {@link GrouperHttpClient}, attaches bearer authentication, POSTs a JSON body, tracks per-command
 * timing via {@link GrouperProvisioner#incrementCommandsCallsStats(String, int, long)}, and parses
 * the JSON response with the {@code GrouperUtil.jsonJackson*} helpers. The Dropbox DAO and the
 * provisioner tests call the public static methods here.</p>
 *
 * <p><b>Auth difference from TrueFoundry.</b> TrueFoundry stores a JSON blob of two tokens in the
 * {@code accessTokenPassword}; Dropbox uses a <i>single, plain</i> bearer token, so
 * {@link #attachAuthentication(GrouperHttpClient, String)} reads {@code accessTokenPassword} as the
 * literal token string with no JSON parse. The base URL comes from
 * {@code grouper.wsBearerToken.<configId>.endpoint} (the mock service path in tests,
 * {@code https://api.dropboxapi.com} in prod) -- never hardcoded.</p>
 *
 * <p><b>Dropbox conventions.</b> Every Team API call is a POST with a JSON object body (use
 * {@code "{}"} when there are no arguments). Unions are encoded with a {@code ".tag"} discriminator.
 * Long-running write operations (group delete, group member add/remove, member add/remove) may
 * return an {@code async_job_id} instead of completing inline; this layer polls the matching
 * {@code .../job_status/...} endpoint until the job reports {@code {".tag":"complete"}} (see
 * {@link #pollIfAsync}). The mock returns synchronous {@code complete} results, so polling is a
 * no-op under test but is required in production.</p>
 */
public class DropboxApiCommands {

  /** default page size for paged list endpoints; Dropbox allows up to 1000 per page */
  private static final int DEFAULT_PAGE_SIZE = 1000;

  /** maximum number of times to poll an async job before giving up */
  private static final int MAX_POLL_ATTEMPTS = 30;

  /** sleep between async-job polls (short -- the mock completes synchronously anyway) */
  private static final long POLL_SLEEP_MILLIS = 1000L;

  /** the bearer token header carries secrets, so never log it */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  /** loader config, used to resolve endpoint + token by configId */
  public static GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

  /**
   * Cache of Dropbox admin-role name -&gt; role_id, keyed by configId. Dropbox has no "list all
   * roles" endpoint, so the catalog is harvested opportunistically from the {@code roles[]} arrays
   * returned by members/list_v2 (and get_info_v2) as members are read. Concurrent because multiple
   * provisioners can share this static class.
   */
  private static final Map<String, Map<String, String>> adminRoleNameToIdByConfigId =
      new ConcurrentHashMap<String, Map<String, String>>();

  /**
   * Read the plain Dropbox bearer token from the WsBearerToken external system config.
   * Unlike TrueFoundry, the stored value is the token itself (not a JSON blob), so no parse.
   * @param configId the external system config id
   * @return the bearer token string
   */
  private static String getToken(String configId) {
    String accessTokenPassword = grouperLoaderConfig.propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");
    if (StringUtils.isBlank(accessTokenPassword)) {
      throw new RuntimeException("accessTokenPassword is required for Dropbox configId: " + configId);
    }

    DropboxOauthConfig oauthConfig = parseOauthConfig(accessTokenPassword);
    if (oauthConfig == null) {
      // plain / static bearer token (a legacy long-lived token, or a test token)
      return accessTokenPassword;
    }
    // OAuth2 refresh-token config: exchange + cache a short-lived access token
    return getCachedOrRefreshAccessToken(configId, oauthConfig);
  }

  /**
   * Per-config cache of the current OAuth2 access token and its absolute expiry. Only populated when
   * the accessTokenPassword is a refresh-token config; a plain/static token is never cached.
   */
  private static final Map<String, CachedAccessToken> accessTokenByConfigId =
      new ConcurrentHashMap<String, CachedAccessToken>();

  /** refresh the cached access token this many millis BEFORE Dropbox's stated expiry (safety margin) */
  private static final long ACCESS_TOKEN_EXPIRY_MARGIN_MILLIS = 5L * 60L * 1000L;

  /** A cached OAuth2 access token plus the epoch-millis time it expires. */
  private static class CachedAccessToken {
    private String accessToken;
    private long expiresAtMillis;
  }

  /** Parsed OAuth2 refresh-token config: app key + app secret + refresh token. */
  private static class DropboxOauthConfig {
    private String appKey;
    private String appSecret;
    private String refreshToken;
  }

  /**
   * Parse the accessTokenPassword as an OAuth2 refresh-token config (a JSON object with a
   * {@code refreshToken}, plus {@code appKey}/{@code appSecret}). Returns null when it is not such a
   * JSON object -- i.e. it is a plain/static bearer token.
   * @param accessTokenPassword the configured secret
   * @return the OAuth config, or null for a static token
   */
  private static DropboxOauthConfig parseOauthConfig(String accessTokenPassword) {
    if (accessTokenPassword == null || !accessTokenPassword.trim().startsWith("{")) {
      return null;
    }
    JsonNode node;
    try {
      node = GrouperUtil.jsonJacksonNode(accessTokenPassword);
    } catch (RuntimeException e) {
      return null;
    }
    String refreshToken = GrouperUtil.jsonJacksonGetString(node, "refreshToken");
    if (StringUtils.isBlank(refreshToken)) {
      return null;
    }
    DropboxOauthConfig oauthConfig = new DropboxOauthConfig();
    oauthConfig.appKey = GrouperUtil.jsonJacksonGetString(node, "appKey");
    oauthConfig.appSecret = GrouperUtil.jsonJacksonGetString(node, "appSecret");
    oauthConfig.refreshToken = refreshToken;
    return oauthConfig;
  }

  /**
   * Return a still-valid cached access token, or (in a synchronized block, double-checking) exchange
   * the refresh token for a fresh one, cache it, and return it. The token is reused until it is within
   * {@link #ACCESS_TOKEN_EXPIRY_MARGIN_MILLIS} of expiry.
   * @param configId external system config id
   * @param oauthConfig the OAuth refresh-token config
   * @return a valid access token
   */
  private static String getCachedOrRefreshAccessToken(String configId, DropboxOauthConfig oauthConfig) {
    CachedAccessToken cached = accessTokenByConfigId.get(configId);
    if (isStillValid(cached)) {
      return cached.accessToken;
    }
    synchronized (accessTokenByConfigId) {
      // re-check inside the lock so concurrent callers don't each refresh
      cached = accessTokenByConfigId.get(configId);
      if (isStillValid(cached)) {
        return cached.accessToken;
      }
      CachedAccessToken fresh = refreshAccessToken(configId, oauthConfig);
      accessTokenByConfigId.put(configId, fresh);
      return fresh.accessToken;
    }
  }

  /**
   * @param cached the cached token (may be null)
   * @return true if the token exists and will not expire within the safety margin
   */
  private static boolean isStillValid(CachedAccessToken cached) {
    return cached != null
        && System.currentTimeMillis() < (cached.expiresAtMillis - ACCESS_TOKEN_EXPIRY_MARGIN_MILLIS);
  }

  /**
   * Exchange the refresh token for a short-lived access token via POST {endpoint}/oauth2/token
   * (grant_type=refresh_token, form-encoded; client_id/client_secret = app key/secret). The response
   * carries {@code access_token} and {@code expires_in} (seconds; ~14400 = 4h for scoped apps).
   * @param configId external system config id
   * @param oauthConfig the OAuth refresh-token config
   * @return the new cached access token with its absolute expiry
   */
  private static CachedAccessToken refreshAccessToken(String configId, DropboxOauthConfig oauthConfig) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "refreshAccessToken");

    long startTime = System.nanoTime();

    try {

      String configPrefix = "grouper.wsBearerToken." + configId + ".";
      String url = grouperLoaderConfig.propertyValueStringRequired(configPrefix + "endpoint");
      if (url.endsWith("/")) {
        url = url.substring(0, url.length() - 1);
      }
      url += "/oauth2/token";

      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod("POST");
      grouperHttpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");
      grouperHttpClient.addHeader("Accept", "application/json");

      // form body carries the refresh token + client secret -- intentionally NOT added to the debug map
      String body = "grant_type=refresh_token"
          + "&refresh_token=" + GrouperUtil.escapeUrlEncode(oauthConfig.refreshToken)
          + "&client_id=" + GrouperUtil.escapeUrlEncode(StringUtils.defaultString(oauthConfig.appKey))
          + "&client_secret=" + GrouperUtil.escapeUrlEncode(StringUtils.defaultString(oauthConfig.appSecret));
      grouperHttpClient.assignBody(body);

      long httpCallStartMillis = System.currentTimeMillis();
      try {
        grouperHttpClient.executeRequest();
      } finally {
        GrouperProvisioner.incrementCommandsCallsStats("refreshAccessToken", 1,
            System.currentTimeMillis() - httpCallStartMillis);
      }

      int code = grouperHttpClient.getResponseCode();
      String json = grouperHttpClient.getResponseBody();
      if (code != 200) {
        throw new RuntimeException("Dropbox oauth2/token refresh failed (" + code + ")");
      }

      JsonNode responseNode = GrouperUtil.jsonJacksonNode(json);
      String accessToken = GrouperUtil.jsonJacksonGetString(responseNode, "access_token");
      if (StringUtils.isBlank(accessToken)) {
        throw new RuntimeException("Dropbox oauth2/token refresh returned no access_token");
      }
      Integer expiresInSeconds = GrouperUtil.jsonJacksonGetInteger(responseNode, "expires_in");
      debugMap.put("expiresInSeconds", expiresInSeconds);

      CachedAccessToken cached = new CachedAccessToken();
      cached.accessToken = accessToken;
      long lifetimeMillis = (expiresInSeconds == null ? 14400L : expiresInSeconds.longValue()) * 1000L;
      cached.expiresAtMillis = System.currentTimeMillis() + lifetimeMillis;
      return cached;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Clear the cached OAuth2 access tokens. Test hook to force a refresh on the next call.
   */
  static void clearAccessTokenCache() {
    accessTokenByConfigId.clear();
  }

  /**
   * Attach the Dropbox bearer token and standard JSON headers to the HTTP client.
   * @param grouperHttpClient the client to decorate
   * @param configId the external system config id
   */
  private static void attachAuthentication(GrouperHttpClient grouperHttpClient, String configId) {
    String token = getToken(configId);
    // Dropbox uses a single plain bearer token -- no JSON parsing (the TrueFoundry difference)
    grouperHttpClient.addHeader("Authorization", "Bearer " + token);
    grouperHttpClient.addHeader("Accept", "application/json");
  }

  /**
   * Execute one HTTP call against the Dropbox Team API and return the parsed JSON response.
   *
   * <p>Mirrors {@code TrueFoundryApiCommands.executeMethod}: resolves the base URL from
   * {@code grouper.wsBearerToken.<configId>.endpoint}, appends {@code urlSuffix} (or uses
   * {@code urlSuffix} verbatim when it is an absolute URL), validates the HTTP status against
   * {@code allowedReturnCodes}, and records command timing stats under {@code debugLabel}. Dropbox
   * calls are POST with a JSON object body.</p>
   *
   * @param debugMap map accumulating debug info for logging
   * @param debugLabel label used for the command-call timing stats bucket
   * @param httpMethodName HTTP verb (always "POST" for Dropbox, kept parameterized for parity)
   * @param configId external system config id (resolves endpoint + token)
   * @param urlSuffix path appended to the base URL, e.g. {@code /2/team/groups/list}
   * @param allowedReturnCodes acceptable HTTP status codes
   * @param returnCode single-element array that receives the actual HTTP status code
   * @param bodyParam request body JSON string (use {@code "{}"} for no-arg calls), or null
   * @return the parsed JSON response, or null when the body is blank
   */
  private static JsonNode executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes,
      int[] returnCode, String bodyParam) {

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders);
    attachAuthentication(grouperHttpClient, configId);

    String configPrefix = "grouper.wsBearerToken." + configId + ".";
    String url = grouperLoaderConfig.propertyValueStringRequired(configPrefix + "endpoint");

    // same trim/append logic as TrueFoundry: strip a trailing slash on the base, then append the
    // suffix (unless the suffix is already an absolute URL, in which case it wins outright)
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
  // Async job polling
  // ============================

  /**
   * If a Dropbox write response carries an {@code async_job_id} (the operation did not complete
   * inline), poll the supplied job-status endpoint until the job reports {@code complete}. A response
   * tagged {@code complete} (or one with no {@code async_job_id}) means the operation already
   * finished synchronously, so this is a no-op -- which is exactly the mock's behavior.
   *
   * @param debugMap debug map
   * @param configId external system config id
   * @param responseNode the LaunchEmptyResult / GroupMembersChangeResult / MembersAddLaunchResult
   * @param jobStatusUrlSuffix the matching {@code .../job_status/...} endpoint to poll
   * @return the terminal job-status response when polling occurred, otherwise {@code responseNode}
   */
  private static JsonNode pollIfAsync(Map<String, Object> debugMap, String configId,
      JsonNode responseNode, String jobStatusUrlSuffix) {

    if (responseNode == null) {
      return null;
    }

    // the union tag is "complete" for an inline result, or "async_job_id" for a deferred one
    String asyncJobId = GrouperUtil.jsonJacksonGetString(responseNode, "async_job_id");
    if (StringUtils.isBlank(asyncJobId)) {
      // synchronous completion (the mock path) -- nothing to poll
      return responseNode;
    }

    return pollAsyncJobComplete(debugMap, configId, jobStatusUrlSuffix, asyncJobId);
  }

  /**
   * Poll a Dropbox {@code .../job_status/...} endpoint a bounded number of times until the job is
   * {@code complete}, sleeping briefly between attempts. Throws when the job reports {@code failed}
   * or never completes within {@link #MAX_POLL_ATTEMPTS}.
   *
   * @param debugMap debug map
   * @param configId external system config id
   * @param jobStatusUrlSuffix the job-status endpoint path
   * @param asyncJobId the async job id to poll
   * @return the terminal (complete) job-status response
   */
  private static JsonNode pollAsyncJobComplete(Map<String, Object> debugMap, String configId,
      String jobStatusUrlSuffix, String asyncJobId) {

    ObjectNode body = GrouperUtil.jsonJacksonNode();
    body.put("async_job_id", asyncJobId);
    String bodyString = GrouperUtil.jsonJacksonToString(body);

    for (int attempt = 0; attempt < MAX_POLL_ATTEMPTS; attempt++) {

      JsonNode statusNode = executeMethod(debugMap, "pollAsyncJob", "POST", configId,
          jobStatusUrlSuffix, GrouperUtil.toSet(200), new int[] { -1 }, bodyString);

      String tag = statusNode == null ? null : GrouperUtil.jsonJacksonGetString(statusNode, ".tag");

      if ("complete".equals(tag)) {
        return statusNode;
      }
      if ("failed".equals(tag)) {
        throw new RuntimeException("Dropbox async job '" + asyncJobId + "' failed: " + statusNode);
      }
      // "in_progress" -- wait and retry
      GrouperUtil.sleep(POLL_SLEEP_MILLIS);
    }

    throw new RuntimeException("Dropbox async job '" + asyncJobId + "' did not complete after "
        + MAX_POLL_ATTEMPTS + " polls of '" + jobStatusUrlSuffix + "'");
  }

  // ============================
  // Sync-back capture seam
  // ============================
  //
  // The generic-provisioner sync-back captures the FULL target object from the raw JSON read here
  // (not the lossy DropboxGroup / DropboxUser typed beans), hooked at this commands seam where the
  // per-object JSON node is in scope, and a no-op outside a Dropbox provisioning cycle. Unlike
  // TrueFoundry -- whose team/role JSON is non-uniform and needs name/groupType stamping -- Dropbox
  // group/user JSON is UNIFORM across endpoints, so normalization is an identity deep copy: there is
  // nothing to alias or synthesize. The deep copy only protects the live Jackson tree from any
  // downstream mutation.

  /**
   * Identity normalization for a Dropbox capture node. Dropbox JSON is uniform across endpoints, so
   * no field aliasing or synthesis is needed; a deep copy is returned so the captured node is
   * independent of the live response tree. Null-safe and non-object-safe.
   * @param node a raw Dropbox group or member JSON object
   * @return a deep copy of {@code node}, or {@code node} unchanged when it is not a JSON object
   */
  private static JsonNode normalizeJsonForCapture(JsonNode node) {
    if (node == null || !node.isObject()) {
      return node;
    }
    return node.deepCopy();
  }

  // ============================
  // Admin-role catalog cache
  // ============================

  /**
   * Harvest {@code role_id} / {@code name} pairs from a member node's {@code roles[]} array into the
   * per-config admin-role catalog cache. Dropbox exposes no "list all roles" endpoint, so this is
   * how {@link #retrieveAdminRoleNameToId(String)} learns the name -&gt; id mapping it needs to
   * translate Grouper admin-role group names into Dropbox role ids.
   * @param configId external system config id (cache key)
   * @param memberNode a member node that may carry a sibling {@code roles[]} array
   */
  private static void cacheRolesFromMemberNode(String configId, JsonNode memberNode) {
    if (memberNode == null) {
      return;
    }
    JsonNode rolesNode = GrouperUtil.jsonJacksonGetNode(memberNode, "roles");
    if (rolesNode == null || !rolesNode.isArray()) {
      return;
    }
    Map<String, String> nameToId = adminRoleNameToIdByConfigId.get(configId);
    if (nameToId == null) {
      // synchronized so concurrent harvests on the same config don't drop entries
      nameToId = java.util.Collections.synchronizedMap(new LinkedHashMap<String, String>());
      Map<String, String> existing = adminRoleNameToIdByConfigId.putIfAbsent(configId, nameToId);
      if (existing != null) {
        nameToId = existing;
      }
    }
    for (int i = 0; i < rolesNode.size(); i++) {
      JsonNode roleNode = rolesNode.get(i);
      String roleName = GrouperUtil.jsonJacksonGetString(roleNode, "name");
      String roleId = GrouperUtil.jsonJacksonGetString(roleNode, "role_id");
      if (!StringUtils.isBlank(roleName) && !StringUtils.isBlank(roleId)) {
        nameToId.put(roleName, roleId);
      }
    }
  }

  /**
   * Return the cached admin-role name -&gt; role_id map for a config. Dropbox has no standalone
   * roles-list endpoint, so the catalog is populated as a side effect of
   * {@link #retrieveDropboxUsers(String)} (which harvests each member's {@code roles[]}). If the
   * cache is empty, a member read is triggered once to populate it.
   * @param configId external system config id
   * @return a copy of the admin-role name -&gt; role_id map (possibly empty, never null)
   */
  public static Map<String, String> retrieveAdminRoleNameToId(String configId) {
    Map<String, String> nameToId = adminRoleNameToIdByConfigId.get(configId);
    if (nameToId == null || nameToId.isEmpty()) {
      // refresh: reading members harvests roles[] into the cache as a side effect
      retrieveDropboxUsers(configId);
      nameToId = adminRoleNameToIdByConfigId.get(configId);
    }
    // return a defensive snapshot so callers can't mutate the live cache
    Map<String, String> result = new LinkedHashMap<String, String>();
    if (nameToId != null) {
      synchronized (nameToId) {
        result.putAll(nameToId);
      }
    }
    return result;
  }

  /**
   * Return the admin-role name -&gt; role_id catalog, fetching it authoritatively from
   * POST /2/team/members/list_member_roles for the given member.  Dropbox's
   * list_member_roles returns the full set of <i>assignable</i> admin roles for the team
   * (not just the member's current roles), so this works even on a fresh team where no
   * member yet has an admin role -- unlike harvesting roles[] from member reads.  Results
   * are merged into the per-config cache.
   * @param configId external system config id
   * @param teamMemberId a native team_member_id to scope the list_member_roles call
   * @return a copy of the admin-role name -&gt; role_id map (possibly empty, never null)
   */
  public static Map<String, String> retrieveAdminRoleNameToId(String configId, String teamMemberId) {

    if (StringUtils.isBlank(teamMemberId)) {
      return retrieveAdminRoleNameToId(configId);
    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveAdminRoleNameToId");
    debugMap.put("teamMemberId", teamMemberId);

    long startTime = System.nanoTime();

    try {

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put("team_member_id", teamMemberId);

      JsonNode response = executeMethod(debugMap, "retrieveAdminRoleNameToId", "POST", configId,
          "/2/team/members/list_member_roles", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

      // the response carries roles[]; reuse the harvest helper to merge into the cache
      cacheRolesFromMemberNode(configId, response);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }

    return retrieveAdminRoleNameToId(configId);
  }

  // ============================
  // Group methods
  // ============================

  /**
   * Retrieve all Dropbox team groups via POST /2/team/groups/list, paging with
   * /2/team/groups/list/continue until {@code has_more} is false.
   * @param configId external system config id
   * @return the list of DropboxGroup objects
   */
  public static List<DropboxGroup> retrieveDropboxGroups(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveDropboxGroups");

    long startTime = System.nanoTime();

    List<DropboxGroup> results = new ArrayList<DropboxGroup>();

    try {

      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", DEFAULT_PAGE_SIZE);

      // first page
      ObjectNode listBody = GrouperUtil.jsonJacksonNode();
      listBody.put("limit", pageSize);

      String urlSuffix = "/2/team/groups/list";
      String bodyString = GrouperUtil.jsonJacksonToString(listBody);

      while (true) {

        JsonNode jsonNode = executeMethod(debugMap, "retrieveDropboxGroups", "POST", configId,
            urlSuffix, GrouperUtil.toSet(200), new int[] { -1 }, bodyString);

        if (jsonNode == null) {
          break;
        }

        ArrayNode groupsArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "groups");
        if (groupsArray != null) {
          for (int i = 0; i < groupsArray.size(); i++) {
            JsonNode groupNode = groupsArray.get(i);
            DropboxGroup group = DropboxGroup.fromJson(groupNode);
            if (group == null) {
              continue;
            }
            results.add(group);
          }
        }

        // live progress: pages groups over many slow WS calls with no total available, so report
        // count-so-far.  Uses the existing thread-scoped current provisioner; null off a run.
        GrouperProvisioner currentProvisionerForGroups = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
        if (currentProvisionerForGroups != null) {
          currentProvisionerForGroups.assignProgressLabelTarget("retrieving groups from target: " + results.size() + " so far");
        }

        Boolean hasMore = GrouperUtil.jsonJacksonGetBoolean(jsonNode, "has_more", false);
        String cursor = GrouperUtil.jsonJacksonGetString(jsonNode, "cursor");
        if (hasMore == null || !hasMore.booleanValue() || StringUtils.isBlank(cursor)) {
          break;
        }

        // subsequent pages go through the /continue endpoint carrying the cursor
        urlSuffix = "/2/team/groups/list/continue";
        ObjectNode continueBody = GrouperUtil.jsonJacksonNode();
        continueBody.put("cursor", cursor);
        bodyString = GrouperUtil.jsonJacksonToString(continueBody);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Retrieve a single Dropbox group by its native group_id via POST /2/team/groups/get_info. The
   * response is an array of GroupsGetInfoItem; a not-found id yields an {@code id_not_found} entry,
   * for which null is returned.
   * @param configId external system config id
   * @param groupId the native Dropbox group_id (e.g. "g:abc123")
   * @return the DropboxGroup, or null if not found
   */
  public static DropboxGroup retrieveDropboxGroup(String configId, String groupId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveDropboxGroup");
    debugMap.put("groupId", groupId);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("groupId is required for retrieveDropboxGroup");
      }

      // GroupsSelector union by group_ids
      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put(".tag", "group_ids");
      ArrayNode groupIds = GrouperUtil.jsonJacksonArrayNode();
      groupIds.add(groupId);
      body.set("group_ids", groupIds);

      JsonNode jsonNode = executeMethod(debugMap, "retrieveDropboxGroup", "POST", configId,
          "/2/team/groups/get_info", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

      if (jsonNode == null || !jsonNode.isArray() || jsonNode.size() == 0) {
        return null;
      }

      JsonNode itemNode = jsonNode.get(0);
      String tag = GrouperUtil.jsonJacksonGetString(itemNode, ".tag");
      if ("id_not_found".equals(tag)) {
        return null;
      }


      return DropboxGroup.fromJson(itemNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Create a Dropbox team group via POST /2/team/groups/create. The response is a GroupFullInfo
   * carrying the assigned native group_id.
   * @param configId external system config id
   * @param dropboxGroup the group to create (must have name set)
   * @return the created DropboxGroup with id populated
   */
  public static DropboxGroup createDropboxGroup(String configId, DropboxGroup dropboxGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createDropboxGroup");
    debugMap.put("name", dropboxGroup == null ? null : dropboxGroup.getName());

    long startTime = System.nanoTime();

    try {

      if (dropboxGroup == null || StringUtils.isBlank(dropboxGroup.getName())) {
        throw new RuntimeException("group name is required for createDropboxGroup");
      }

      JsonNode jsonNode = executeMethod(debugMap, "createDropboxGroup", "POST", configId,
          "/2/team/groups/create", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(dropboxGroup.toCreateJson()));

      return DropboxGroup.fromJson(jsonNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Update a Dropbox team group via POST /2/team/groups/update. The group is selected by group_id;
   * only the supplied new_* fields are changed. The response is a GroupFullInfo.
   * @param configId external system config id
   * @param dropboxGroup the group to update (must have id set)
   * @return the updated DropboxGroup
   */
  public static DropboxGroup updateDropboxGroup(String configId, DropboxGroup dropboxGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateDropboxGroup");
    debugMap.put("groupId", dropboxGroup == null ? null : dropboxGroup.getId());

    long startTime = System.nanoTime();

    try {

      if (dropboxGroup == null || StringUtils.isBlank(dropboxGroup.getId())) {
        throw new RuntimeException("group id is required for updateDropboxGroup");
      }

      JsonNode jsonNode = executeMethod(debugMap, "updateDropboxGroup", "POST", configId,
          "/2/team/groups/update", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(dropboxGroup.toUpdateJson()));

      return DropboxGroup.fromJson(jsonNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Delete a Dropbox team group via POST /2/team/groups/delete. The response is a LaunchEmptyResult,
   * which may be asynchronous; this method polls /2/team/groups/job_status/get until the delete
   * completes.
   * @param configId external system config id
   * @param groupId the native Dropbox group_id to delete
   */
  public static void deleteDropboxGroup(String configId, String groupId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "deleteDropboxGroup");
    debugMap.put("groupId", groupId);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("groupId is required for deleteDropboxGroup");
      }

      // GroupSelector union by group_id
      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put(".tag", "group_id");
      body.put("group_id", groupId);

      JsonNode responseNode = executeMethod(debugMap, "deleteDropboxGroup", "POST", configId,
          "/2/team/groups/delete", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

      // a group delete can be deferred -- poll the group job-status endpoint to completion
      pollIfAsync(debugMap, configId, responseNode, "/2/team/groups/job_status/get");

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  // ============================
  // Group membership methods
  // ============================

  /**
   * Retrieve all memberships of a Dropbox group via POST /2/team/groups/members/list, paging with
   * /2/team/groups/members/list/continue until {@code has_more} is false. Each GroupMemberInfo is
   * converted into a DropboxMembership (group_id + team_member_id + access_type).
   * @param configId external system config id
   * @param groupId the native Dropbox group_id
   * @return the list of memberships
   */
  public static List<DropboxMembership> retrieveDropboxGroupMemberships(String configId, String groupId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveDropboxGroupMemberships");
    debugMap.put("groupId", groupId);

    long startTime = System.nanoTime();

    List<DropboxMembership> results = new ArrayList<DropboxMembership>();

    try {

      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("groupId is required for retrieveDropboxGroupMemberships");
      }

      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", DEFAULT_PAGE_SIZE);

      ObjectNode listBody = GrouperUtil.jsonJacksonNode();
      listBody.set("group", groupSelectorByGroupId(groupId));
      listBody.put("limit", pageSize);

      String urlSuffix = "/2/team/groups/members/list";
      String bodyString = GrouperUtil.jsonJacksonToString(listBody);

      while (true) {

        JsonNode jsonNode = executeMethod(debugMap, "retrieveDropboxGroupMemberships", "POST", configId,
            urlSuffix, GrouperUtil.toSet(200), new int[] { -1 }, bodyString);

        if (jsonNode == null) {
          break;
        }

        ArrayNode membersArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "members");
        if (membersArray != null) {
          for (int i = 0; i < membersArray.size(); i++) {
            JsonNode memberInfoNode = membersArray.get(i);
            DropboxMembership membership = membershipFromGroupMemberInfo(groupId, memberInfoNode);
            if (membership == null) {
              continue;
            }
            results.add(membership);
          }
        }

        // live progress: pages memberships over many slow WS calls with no total available, so report
        // count-so-far.  Uses the existing thread-scoped current provisioner; null off a run.
        GrouperProvisioner currentProvisionerForMembers = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
        if (currentProvisionerForMembers != null) {
          currentProvisionerForMembers.assignProgressLabelTarget("retrieving memberships from target: " + results.size() + " so far");
        }

        Boolean hasMore = GrouperUtil.jsonJacksonGetBoolean(jsonNode, "has_more", false);
        String cursor = GrouperUtil.jsonJacksonGetString(jsonNode, "cursor");
        if (hasMore == null || !hasMore.booleanValue() || StringUtils.isBlank(cursor)) {
          break;
        }

        urlSuffix = "/2/team/groups/members/list/continue";
        ObjectNode continueBody = GrouperUtil.jsonJacksonNode();
        continueBody.put("cursor", cursor);
        bodyString = GrouperUtil.jsonJacksonToString(continueBody);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Add members to a Dropbox group via POST /2/team/groups/members/add. Each membership becomes a
   * {@code {"user":UserSelectorArg,"access_type":{".tag":..}}} entry. The response is a
   * GroupMembersChangeResult which may carry an {@code async_job_id}; this method polls
   * /2/team/groups/job_status/get to completion.
   * @param configId external system config id
   * @param groupId the native Dropbox group_id
   * @param memberships the memberships to add (team_member_id + access_type)
   */
  public static void addDropboxGroupMembers(String configId, String groupId, List<DropboxMembership> memberships) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "addDropboxGroupMembers");
    debugMap.put("groupId", groupId);
    debugMap.put("count", memberships == null ? 0 : memberships.size());

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("groupId is required for addDropboxGroupMembers");
      }
      if (memberships == null || memberships.isEmpty()) {
        return;
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.set("group", groupSelectorByGroupId(groupId));

      ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
      for (DropboxMembership membership : memberships) {
        if (membership == null || StringUtils.isBlank(membership.getTeamMemberId())) {
          continue;
        }
        ObjectNode memberEntry = GrouperUtil.jsonJacksonNode();
        memberEntry.set("user", userSelectorByTeamMemberId(membership.getTeamMemberId()));
        ObjectNode accessTypeNode = GrouperUtil.jsonJacksonNode();
        accessTypeNode.put(".tag",
            GrouperUtil.defaultIfBlank(membership.getAccessType(), DropboxMembership.ACCESS_TYPE_MEMBER));
        memberEntry.set("access_type", accessTypeNode);
        membersArray.add(memberEntry);
      }
      body.set("members", membersArray);
      body.put("return_members", false);

      JsonNode responseNode = executeMethod(debugMap, "addDropboxGroupMembers", "POST", configId,
          "/2/team/groups/members/add", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

      // a membership change can be deferred -- poll the group job-status endpoint to completion
      pollIfAsync(debugMap, configId, responseNode, "/2/team/groups/job_status/get");

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Remove members from a Dropbox group via POST /2/team/groups/members/remove. The response is a
   * GroupMembersChangeResult which may carry an {@code async_job_id}; this method polls
   * /2/team/groups/job_status/get to completion.
   * @param configId external system config id
   * @param groupId the native Dropbox group_id
   * @param teamMemberIds the native team_member_ids to remove
   */
  public static void removeDropboxGroupMembers(String configId, String groupId, List<String> teamMemberIds) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "removeDropboxGroupMembers");
    debugMap.put("groupId", groupId);
    debugMap.put("count", teamMemberIds == null ? 0 : teamMemberIds.size());

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("groupId is required for removeDropboxGroupMembers");
      }
      if (teamMemberIds == null || teamMemberIds.isEmpty()) {
        return;
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.set("group", groupSelectorByGroupId(groupId));

      ArrayNode usersArray = GrouperUtil.jsonJacksonArrayNode();
      for (String teamMemberId : teamMemberIds) {
        if (StringUtils.isBlank(teamMemberId)) {
          continue;
        }
        usersArray.add(userSelectorByTeamMemberId(teamMemberId));
      }
      body.set("users", usersArray);
      body.put("return_members", false);

      JsonNode responseNode = executeMethod(debugMap, "removeDropboxGroupMembers", "POST", configId,
          "/2/team/groups/members/remove", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

      pollIfAsync(debugMap, configId, responseNode, "/2/team/groups/job_status/get");

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  // ============================
  // Member (entity) methods
  // ============================

  /**
   * Retrieve all Dropbox team members via POST /2/team/members/list_v2, paging with
   * /2/team/members/list/continue_v2 until {@code has_more} is false. As a side effect, each
   * member's {@code roles[]} array is harvested into the admin-role catalog cache (see
   * {@link #retrieveAdminRoleNameToId(String)}).
   * @param configId external system config id
   * @return the list of DropboxUser objects
   */
  public static List<DropboxUser> retrieveDropboxUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveDropboxUsers");

    long startTime = System.nanoTime();

    List<DropboxUser> results = new ArrayList<DropboxUser>();

    try {

      int pageSize = grouperLoaderConfig.propertyValueInt(
          "grouper.wsBearerToken." + configId + ".pageSize", DEFAULT_PAGE_SIZE);

      ObjectNode listBody = GrouperUtil.jsonJacksonNode();
      listBody.put("limit", pageSize);
      listBody.put("include_removed", false);

      String urlSuffix = "/2/team/members/list_v2";
      String bodyString = GrouperUtil.jsonJacksonToString(listBody);

      while (true) {

        JsonNode jsonNode = executeMethod(debugMap, "retrieveDropboxUsers", "POST", configId,
            urlSuffix, GrouperUtil.toSet(200), new int[] { -1 }, bodyString);

        if (jsonNode == null) {
          break;
        }

        ArrayNode membersArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "members");
        if (membersArray != null) {
          for (int i = 0; i < membersArray.size(); i++) {
            JsonNode memberNode = membersArray.get(i);
            // harvest the admin-role catalog (name -> role_id) from this member's roles[]
            cacheRolesFromMemberNode(configId, memberNode);
            DropboxUser user = DropboxUser.fromJson(memberNode);
            if (user == null) {
              continue;
            }
            results.add(user);
          }
        }

        // live progress: pages users over many slow WS calls with no total available, so report
        // count-so-far.  Uses the existing thread-scoped current provisioner; null off a run.
        GrouperProvisioner currentProvisionerForUsers = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
        if (currentProvisionerForUsers != null) {
          currentProvisionerForUsers.assignProgressLabelTarget("retrieving users from target: " + results.size() + " so far");
        }

        Boolean hasMore = GrouperUtil.jsonJacksonGetBoolean(jsonNode, "has_more", false);
        String cursor = GrouperUtil.jsonJacksonGetString(jsonNode, "cursor");
        if (hasMore == null || !hasMore.booleanValue() || StringUtils.isBlank(cursor)) {
          break;
        }

        urlSuffix = "/2/team/members/list/continue_v2";
        ObjectNode continueBody = GrouperUtil.jsonJacksonNode();
        continueBody.put("cursor", cursor);
        bodyString = GrouperUtil.jsonJacksonToString(continueBody);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Retrieve a single Dropbox member via POST /2/team/members/get_info_v2 using a UserSelectorArg
   * union. {@code selectorTag} is the union tag ({@code external_id}, {@code email}, or
   * {@code team_member_id}) and {@code selectorValue} is the matching value. Returns null when the
   * member is not found ({@code id_not_found}).
   * @param configId external system config id
   * @param selectorTag the union discriminator (external_id | email | team_member_id)
   * @param selectorValue the value to match
   * @return the DropboxUser, or null if not found
   */
  public static DropboxUser retrieveDropboxUser(String configId, String selectorTag, String selectorValue) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveDropboxUser");
    debugMap.put("selectorTag", selectorTag);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(selectorTag) || StringUtils.isBlank(selectorValue)) {
        throw new RuntimeException("selectorTag and selectorValue are required for retrieveDropboxUser");
      }

      // UserSelectorArg union: { ".tag": <tag>, <tag>: <value> }
      ObjectNode selectorNode = GrouperUtil.jsonJacksonNode();
      selectorNode.put(".tag", selectorTag);
      selectorNode.put(selectorTag, selectorValue);

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
      membersArray.add(selectorNode);
      body.set("members", membersArray);

      JsonNode jsonNode = executeMethod(debugMap, "retrieveDropboxUser", "POST", configId,
          "/2/team/members/get_info_v2", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

      if (jsonNode == null) {
        return null;
      }

      ArrayNode membersInfo = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "members_info");
      if (membersInfo == null || membersInfo.size() == 0) {
        return null;
      }

      JsonNode memberInfoNode = membersInfo.get(0);
      String tag = GrouperUtil.jsonJacksonGetString(memberInfoNode, ".tag");
      if ("id_not_found".equals(tag)) {
        return null;
      }

      cacheRolesFromMemberNode(configId, memberInfoNode);

      return DropboxUser.fromJson(memberInfoNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Create (invite) a Dropbox team member via POST /2/team/members/add_v2. The response is either an
   * inline {@code {".tag":"complete","complete":[MemberAddV2Result]}} or a deferred
   * {@code {".tag":"async_job_id",..}}; in the deferred case this method polls
   * /2/team/members/add/job_status/get_v2 and reads the completed result from the terminal response.
   * @param configId external system config id
   * @param dropboxUser the member to create (must have email set)
   * @return the created DropboxUser with team_member_id populated, or null if the result is unusable
   */
  public static DropboxUser createDropboxUser(String configId, DropboxUser dropboxUser) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "createDropboxUser");
    debugMap.put("email", dropboxUser == null ? null : dropboxUser.getEmail());

    long startTime = System.nanoTime();

    try {

      if (dropboxUser == null || StringUtils.isBlank(dropboxUser.getEmail())) {
        throw new RuntimeException("user email is required for createDropboxUser");
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      ArrayNode newMembers = GrouperUtil.jsonJacksonArrayNode();
      newMembers.add(dropboxUser.toAddJson());
      body.set("new_members", newMembers);
      body.put("force_async", false);

      JsonNode responseNode = executeMethod(debugMap, "createDropboxUser", "POST", configId,
          "/2/team/members/add_v2", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

      // when deferred, poll the add job-status endpoint; the terminal response carries the result
      JsonNode resultNode = pollIfAsync(debugMap, configId, responseNode,
          "/2/team/members/add/job_status/get_v2");

      String firstResultTag = firstAddResultTag(resultNode);

      // happy path: the member was created
      if ("success".equals(firstResultTag)) {
        return parseAddV2Result(resultNode);
      }

      // Recoverable conflict: the email is associated with an existing member that was removed within
      // Dropbox's 7-day recovery window (or is invited). Re-adding fails with "user_already_on_team";
      // recover the account instead of failing, then re-read it to pick up its team_member_id/profile.
      // (Mirrors the Wharton IIQ AfterProvisioning recover-on-conflict behavior.)
      if (ADD_RESULT_TAG_USER_ALREADY_ON_TEAM.equals(firstResultTag)) {
        debugMap.put("addConflict", firstResultTag);
        recoverDropboxUser(configId, dropboxUser.getEmail());
        DropboxUser recovered = retrieveDropboxUser(configId, "email", dropboxUser.getEmail());
        debugMap.put("recovered", recovered != null);
        return recovered;
      }

      // any other non-success result is a real failure
      throw new RuntimeException("Dropbox members/add_v2 did not succeed: " + resultNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Update a Dropbox member's profile via POST /2/team/members/set_profile_v2. The member is
   * selected by team_member_id; only the supplied field names are changed. The response is a
   * TeamMemberInfoV2 ({@code {"profile":..}}).
   * @param configId external system config id
   * @param dropboxUser the member with updated fields (must have id set)
   * @param fieldNamesToSet the field names to update, or null for all
   * @return the updated DropboxUser
   */
  public static DropboxUser updateDropboxUser(String configId, DropboxUser dropboxUser, Set<String> fieldNamesToSet) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "updateDropboxUser");
    debugMap.put("teamMemberId", dropboxUser == null ? null : dropboxUser.getId());

    long startTime = System.nanoTime();

    try {

      if (dropboxUser == null || StringUtils.isBlank(dropboxUser.getId())) {
        throw new RuntimeException("team_member_id is required for updateDropboxUser");
      }

      JsonNode jsonNode = executeMethod(debugMap, "updateDropboxUser", "POST", configId,
          "/2/team/members/set_profile_v2", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(dropboxUser.toSetProfileJson(fieldNamesToSet)));

      if (jsonNode == null) {
        return null;
      }

      // response is TeamMemberInfoV2 { "profile": {..}, "roles": [..] }; DropboxUser.fromJson
      // understands the profile wrapper
      return DropboxUser.fromJson(jsonNode);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Remove a Dropbox member from the team via POST /2/team/members/remove. The response is a
   * LaunchEmptyResult which may be asynchronous; this method polls
   * /2/team/members/remove/job_status/get to completion.
   * @param configId external system config id
   * @param teamMemberId the native team_member_id to remove
   * @param wipeData whether to wipe the member's data on removal
   * @param keepAccount whether to convert the account to a free Basic account (keep) rather than
   *        delete it
   */
  public static void removeDropboxUser(String configId, String teamMemberId, boolean wipeData, boolean keepAccount) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "removeDropboxUser");
    debugMap.put("teamMemberId", teamMemberId);
    debugMap.put("wipeData", wipeData);
    debugMap.put("keepAccount", keepAccount);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(teamMemberId)) {
        throw new RuntimeException("teamMemberId is required for removeDropboxUser");
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.set("user", userSelectorByTeamMemberId(teamMemberId));
      body.put("wipe_data", wipeData);
      body.put("keep_account", keepAccount);

      // keep_account=true (downgrade to a free Basic account) is only valid for an ACTIVE member.
      // For an invited-but-never-accepted member Dropbox returns 409 cannot_keep_invited_user_account
      // (there is no personal account to keep). Allow that 409 so we can fall back to a plain delete of
      // the invitation -- the same net behavior as the Wharton IIQ flow.
      int[] returnCode = new int[] { -1 };
      java.util.Set<Integer> allowedCodes = keepAccount ? GrouperUtil.toSet(200, 409) : GrouperUtil.toSet(200);

      JsonNode responseNode = executeMethod(debugMap, "removeDropboxUser", "POST", configId,
          "/2/team/members/remove", allowedCodes, returnCode,
          GrouperUtil.jsonJacksonToString(body));

      if (returnCode[0] == 409) {
        String errorTag = GrouperUtil.jsonJacksonGetString(
            GrouperUtil.jsonJacksonGetNode(responseNode, "error"), ".tag");
        if (!REMOVE_ERROR_CANNOT_KEEP_INVITED_USER_ACCOUNT.equals(errorTag)) {
          // some other 409 -- not the invited-account case we know how to recover from
          throw new RuntimeException("Dropbox members/remove failed (409): " + responseNode);
        }
        // retry as a plain delete (keep_account=false) -- remove the invitation outright
        debugMap.put("keepAccountInvitedFallback", true);
        ObjectNode deleteBody = GrouperUtil.jsonJacksonNode();
        deleteBody.set("user", userSelectorByTeamMemberId(teamMemberId));
        deleteBody.put("wipe_data", wipeData);
        deleteBody.put("keep_account", false);
        JsonNode deleteResponse = executeMethod(debugMap, "removeDropboxUser", "POST", configId,
            "/2/team/members/remove", GrouperUtil.toSet(200), new int[] { -1 },
            GrouperUtil.jsonJacksonToString(deleteBody));
        pollIfAsync(debugMap, configId, deleteResponse, "/2/team/members/remove/job_status/get");
        return;
      }

      pollIfAsync(debugMap, configId, responseNode, "/2/team/members/remove/job_status/get");

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Suspend a Dropbox team member (deactivate; the account stays on the team but the member loses
   * access) via POST /2/team/members/suspend. wipe_data is false so the member's files are retained
   * for when they are unsuspended.
   * @param configId external system config id
   * @param teamMemberId the native team_member_id to suspend
   */
  public static void suspendDropboxUser(String configId, String teamMemberId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "suspendDropboxUser");
    debugMap.put("teamMemberId", teamMemberId);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(teamMemberId)) {
        throw new RuntimeException("teamMemberId is required for suspendDropboxUser");
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.set("user", userSelectorByTeamMemberId(teamMemberId));
      body.put("wipe_data", false);

      executeMethod(debugMap, "suspendDropboxUser", "POST", configId,
          "/2/team/members/suspend", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Unsuspend (reactivate) a previously suspended Dropbox team member via POST
   * /2/team/members/unsuspend.
   * @param configId external system config id
   * @param teamMemberId the native team_member_id to unsuspend
   */
  public static void unsuspendDropboxUser(String configId, String teamMemberId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "unsuspendDropboxUser");
    debugMap.put("teamMemberId", teamMemberId);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(teamMemberId)) {
        throw new RuntimeException("teamMemberId is required for unsuspendDropboxUser");
      }

      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.set("user", userSelectorByTeamMemberId(teamMemberId));

      executeMethod(debugMap, "unsuspendDropboxUser", "POST", configId,
          "/2/team/members/unsuspend", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Set a Dropbox member's admin roles via POST /2/team/members/set_admin_permissions_v2. An empty
   * {@code roleIds} list demotes the member to member_only (no admin rights). Role ids are resolved
   * by the caller via {@link #retrieveAdminRoleNameToId(String)}.
   * @param configId external system config id
   * @param teamMemberId the native team_member_id
   * @param roleIds the Dropbox role ids to assign (empty for member_only)
   */
  public static void setDropboxAdminRoles(String configId, String teamMemberId, List<String> roleIds) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "setDropboxAdminRoles");
    debugMap.put("teamMemberId", teamMemberId);
    debugMap.put("roleCount", roleIds == null ? 0 : roleIds.size());

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(teamMemberId)) {
        throw new RuntimeException("teamMemberId is required for setDropboxAdminRoles");
      }

      // reuse DropboxUser.toSetAdminPermissionsJson, which builds the user selector + new_roles
      DropboxUser dropboxUser = new DropboxUser();
      dropboxUser.setId(teamMemberId);

      executeMethod(debugMap, "setDropboxAdminRoles", "POST", configId,
          "/2/team/members/set_admin_permissions_v2", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(dropboxUser.toSetAdminPermissionsJson(roleIds)));

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  // ============================
  // JSON selector + parse helpers
  // ============================

  /**
   * Build a GroupSelector union selecting by group_id: {@code {".tag":"group_id","group_id":..}}.
   * @param groupId the native group_id
   * @return the selector node
   */
  private static ObjectNode groupSelectorByGroupId(String groupId) {
    ObjectNode selector = GrouperUtil.jsonJacksonNode();
    selector.put(".tag", "group_id");
    selector.put("group_id", groupId);
    return selector;
  }

  /**
   * Build a UserSelectorArg union selecting by team_member_id:
   * {@code {".tag":"team_member_id","team_member_id":..}}.
   * @param teamMemberId the native team_member_id
   * @return the selector node
   */
  private static ObjectNode userSelectorByTeamMemberId(String teamMemberId) {
    ObjectNode selector = GrouperUtil.jsonJacksonNode();
    selector.put(".tag", "team_member_id");
    selector.put("team_member_id", teamMemberId);
    return selector;
  }

  /**
   * Convert a GroupMemberInfo node ({@code {"profile":TeamMemberProfile,"access_type":{".tag":..}}})
   * into a DropboxMembership for the given group. Returns null if the team_member_id is missing.
   * @param groupId the owning native group_id
   * @param memberInfoNode the GroupMemberInfo node
   * @return the membership, or null
   */
  private static DropboxMembership membershipFromGroupMemberInfo(String groupId, JsonNode memberInfoNode) {
    if (memberInfoNode == null) {
      return null;
    }
    JsonNode profileNode = GrouperUtil.jsonJacksonGetNode(memberInfoNode, "profile");
    if (profileNode == null) {
      profileNode = memberInfoNode;
    }
    String teamMemberId = GrouperUtil.jsonJacksonGetString(profileNode, "team_member_id");
    if (StringUtils.isBlank(teamMemberId)) {
      return null;
    }

    DropboxMembership membership = new DropboxMembership();
    // surrogate id so the bean is fully populated (Dropbox itself has no membership id)
    membership.setId(UUID.randomUUID().toString());
    membership.setGroupId(groupId);
    membership.setTeamMemberId(teamMemberId);

    // access_type is a union { ".tag": "member" | "owner" }
    JsonNode accessTypeNode = GrouperUtil.jsonJacksonGetNode(memberInfoNode, "access_type");
    if (accessTypeNode != null) {
      membership.setAccessType(GrouperUtil.jsonJacksonGetString(accessTypeNode, ".tag"));
    }
    return membership;
  }

  /**
   * members/add_v2 result tag indicating the email is associated with an existing member -- including
   * one removed but still within Dropbox's 7-day recovery window, or invited. This is the recoverable
   * conflict {@link #createDropboxUser} handles by calling {@link #recoverDropboxUser}.
   */
  private static final String ADD_RESULT_TAG_USER_ALREADY_ON_TEAM = "user_already_on_team";

  /**
   * members/remove error tag returned (HTTP 409) when keep_account=true is requested for an
   * invited-but-never-accepted member -- there is no personal account to convert to. {@link
   * #removeDropboxUser} falls back to a plain delete (keep_account=false) in this case.
   */
  private static final String REMOVE_ERROR_CANNOT_KEEP_INVITED_USER_ACCOUNT = "cannot_keep_invited_user_account";

  /**
   * Return the {@code .tag} of the first members/add_v2 result element (callers add exactly one
   * member per call), or null if the response has no result element.
   * @param resultNode the add_v2 response (or terminal job-status response)
   * @return the first result's union tag, or null
   */
  private static String firstAddResultTag(JsonNode resultNode) {
    if (resultNode == null) {
      return null;
    }
    ArrayNode completeArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(resultNode, "complete");
    if (completeArray == null || completeArray.size() == 0) {
      return null;
    }
    return GrouperUtil.jsonJacksonGetString(completeArray.get(0), ".tag");
  }

  /**
   * Recover a removed Dropbox team member within the 7-day recovery window via POST
   * /2/team/members/recover, selecting the member by email. Used by {@link #createDropboxUser} when a
   * re-add hits the {@code user_already_on_team} recoverable conflict. Throws if recovery fails -- e.g.
   * the account was invited-but-never-accepted, or is outside the recovery window (the caller then
   * surfaces a normal provisioning failure).
   * @param configId external system config id
   * @param email the member email to recover
   */
  public static void recoverDropboxUser(String configId, String email) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "recoverDropboxUser");
    debugMap.put("email", email);

    long startTime = System.nanoTime();

    try {

      if (StringUtils.isBlank(email)) {
        throw new RuntimeException("email is required for recoverDropboxUser");
      }

      // RecoverArg selects the member by email
      ObjectNode body = GrouperUtil.jsonJacksonNode();
      ObjectNode userSelector = GrouperUtil.jsonJacksonNode();
      userSelector.put(".tag", "email");
      userSelector.put("email", email);
      body.set("user", userSelector);

      executeMethod(debugMap, "recoverDropboxUser", "POST", configId,
          "/2/team/members/recover", GrouperUtil.toSet(200), new int[] { -1 },
          GrouperUtil.jsonJacksonToString(body));

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      DropboxLog.dropboxLog(debugMap, startTime);
    }
  }

  /**
   * Parse a members/add_v2 result (inline or terminal-async) into a DropboxUser. The result shape is
   * {@code {".tag":"complete","complete":[MemberAddV2Result]}} where a success element is
   * {@code {".tag":"success","success":{"profile":..,"role":..}}}. Returns null if no successful
   * member is present.
   * @param resultNode the add_v2 response (or terminal job-status response)
   * @return the created DropboxUser, or null
   */
  private static DropboxUser parseAddV2Result(JsonNode resultNode) {
    if (resultNode == null) {
      return null;
    }
    ArrayNode completeArray = (ArrayNode) GrouperUtil.jsonJacksonGetNode(resultNode, "complete");
    if (completeArray == null || completeArray.size() == 0) {
      return null;
    }
    // take the first element -- callers add exactly one member per call
    JsonNode firstResult = completeArray.get(0);
    String tag = GrouperUtil.jsonJacksonGetString(firstResult, ".tag");
    if (!"success".equals(tag)) {
      throw new RuntimeException("Dropbox members/add_v2 did not succeed: " + firstResult);
    }
    // the success payload is a TeamMemberInfo with a "profile" object DropboxUser.fromJson handles
    JsonNode successNode = GrouperUtil.jsonJacksonGetNode(firstResult, "success");
    return DropboxUser.fromJson(successNode != null ? successNode : firstResult);
  }

  // ============================
  // Ignore helpers (mirror TrueFoundry)
  // ============================

  /**
   * Parse a comma-separated ignore string into a lowercase set for case-insensitive matching.
   * @param commaSeparated the comma-separated string (may be null or blank)
   * @return the set of lowercase trimmed values, or null if the input is blank/empty
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
   * Check whether a value is in the ignore set (case-insensitive).
   * @param value the value to check
   * @param ignoreSet the set of lowercase values to ignore
   * @return true if the value should be ignored
   */
  public static boolean isIgnored(String value, Set<String> ignoreSet) {
    if (ignoreSet == null || StringUtils.isBlank(value)) {
      return false;
    }
    return ignoreSet.contains(value.toLowerCase());
  }

  // ============================
  // Main (manual smoke test against a real Dropbox target)
  // ============================

  /**
   * Manual integration entry point. Requires grouper-loader.properties configured with a
   * "dropboxProd" WsBearerToken external system. Intended for hand-running against a real tenant;
   * the automated tests use the mock service instead.
   * @param args unused
   */
  public static void main(String[] args) {

    GrouperStartup.startup();

    String configId = "dropboxProd";

    try {
      List<DropboxGroup> groups = retrieveDropboxGroups(configId);
      System.out.println("Total groups: " + GrouperUtil.length(groups));

      List<DropboxUser> users = retrieveDropboxUsers(configId);
      System.out.println("Total members: " + GrouperUtil.length(users));

      Map<String, String> adminRoleNameToId = retrieveAdminRoleNameToId(configId);
      System.out.println("Admin roles discovered: " + adminRoleNameToId);

    } catch (Exception e) {
      System.out.println("ERROR: " + GrouperClientUtils.getFullStackTrace(e));
    }
    System.exit(0);
  }

}
