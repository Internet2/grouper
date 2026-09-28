package edu.internet2.middleware.grouper.app.github;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * Static wrappers around the GitHub REST and GraphQL calls the provisioner uses.
 * Each call was validated by hand (Postman) before this class was written; see
 * the "Grouper GitHub provisioner developer notes" wiki page for the proven
 * request/response shapes.
 *
 * <p>All operations are addressed by the org login plus the login/slug, since
 * GitHub org and team membership URLs are org-scoped. The bearer token comes
 * from the WsBearerToken external system config
 * (<code>grouper.wsBearerToken.&lt;configId&gt;.accessTokenPassword</code>).</p>
 *
 * <p>Idempotency notes proven in testing and relied on here: add-membership PUT
 * returns 200 even when already a member; the cancel/remove DELETEs return 204
 * even when the target is already gone (so 404 is allowed and treated as
 * success).</p>
 */
public class GithubApiCommands {

  /** GitHub max page size for list endpoints */
  private static final int MAX_PAGE_SIZE = 100;

  /** GitHub REST API version pinned on every request */
  private static final String GITHUB_API_VERSION = "2022-11-28";

  /** do not log the bearer token */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  /** cached loader config */
  public static GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

  /**
   * Null-safe read of a text/number field off a Jackson node as a String.
   * @param node the parent node
   * @param fieldName the field name
   * @return the field value as text, or null if missing/null
   */
  public static String jsonText(JsonNode node, String fieldName) {
    if (node == null) {
      return null;
    }
    JsonNode field = node.get(fieldName);
    if (field == null || field.isNull()) {
      return null;
    }
    return field.asText();
  }

  /**
   * Attach the standard GitHub headers, reading the bearer token out of the
   * WsBearerToken external system config. Unlike Datadog (which packs two custom
   * headers into a JSON accessTokenPassword), GitHub uses a single bearer token,
   * so accessTokenPassword IS the token.
   * @param grouperHttpClient the HTTP client to attach headers to
   * @param configId the external system config id
   */
  private static void attachGithubAuthentication(GrouperHttpClient grouperHttpClient, String configId) {
    String token = grouperLoaderConfig.propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");

    if (StringUtils.isBlank(token)) {
      throw new RuntimeException("accessTokenPassword (bearer token) is required for configId: " + configId);
    }

    grouperHttpClient.addHeader("Authorization", "Bearer " + token);
    grouperHttpClient.addHeader("Accept", "application/vnd.github+json");
    grouperHttpClient.addHeader("X-GitHub-Api-Version", GITHUB_API_VERSION);
  }

  /**
   * Central HTTP executor. Attaches auth, resolves the base endpoint, applies
   * GitHub paging params (page is 1-indexed, per_page), sends the request, and
   * validates the return code against the allowed set (throwing with the URL and
   * body on any other code). Returns the parsed response root (which for GitHub
   * list endpoints is a top-level JSON array), or null on a blank body.
   *
   * @param debugMap debug map for logging
   * @param debugLabel short label for stats/logging
   * @param httpMethodName GET/POST/PUT/PATCH/DELETE
   * @param configId external system config id
   * @param urlSuffix path suffix (appended to the configured endpoint), or a full http url
   * @param allowedReturnCodes HTTP codes that are NOT errors
   * @param returnCode one-element array; the actual code is written to [0]
   * @param bodyParam request body JSON, or null
   * @param pageNumber 1-indexed page number to request, or null for no paging
   * @param addPerPage true to add the per_page param
   * @return the parsed response root, or null if the body was blank
   */
  private static JsonNode executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes,
      int[] returnCode, String bodyParam, Integer pageNumber, boolean addPerPage) {

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders);

    attachGithubAuthentication(grouperHttpClient, configId);

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
      grouperHttpClient.addUrlParameter("page", Integer.toString(pageNumber));
    }

    if (addPerPage) {
      int pageSize = grouperLoaderConfig.propertyValueInt(configPrefix + "pageSize", MAX_PAGE_SIZE);
      grouperHttpClient.addUrlParameter("per_page", Integer.toString(pageSize));
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
      return GrouperUtil.jsonJacksonNode(json);
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }
  }

  /**
   * Page through a list endpoint (page + per_page) and return the accumulated
   * top-level array elements. Stops when a page returns fewer than per_page rows.
   * @param debugMap debug map
   * @param debugLabel stats label
   * @param configId external system config id
   * @param urlSuffix the list endpoint path
   * @return list of element nodes (never null)
   */
  private static List<JsonNode> pageThroughArray(Map<String, Object> debugMap, String debugLabel,
      String configId, String urlSuffix) {

    List<JsonNode> elements = new ArrayList<JsonNode>();

    int pageSize = grouperLoaderConfig.propertyValueInt(
        "grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE);

    int pageNumber = 1;
    while (true) {
      JsonNode root = executeMethod(debugMap, debugLabel, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200), new int[] { -1 }, null, pageNumber, true);

      int returnedCount = 0;
      if (root != null && root.isArray()) {
        ArrayNode arrayNode = (ArrayNode) root;
        returnedCount = arrayNode.size();
        for (int i = 0; i < returnedCount; i++) {
          elements.add(arrayNode.get(i));
        }
      }

      // live progress: many slow WS calls with no total available, so report count-so-far and page
      GrouperProvisioner currentProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
      if (currentProvisioner != null) {
        currentProvisioner.assignProgressLabelTarget(
            debugLabel + ": " + elements.size() + " so far (page " + pageNumber + ")");
      }

      if (returnedCount < pageSize) {
        break;
      }
      pageNumber++;
    }

    return elements;
  }

  // ============================
  // Team (group) reads
  // ============================

  /**
   * Retrieve all teams in an org via GET /orgs/{org}/teams (paged). Returns both
   * organization and enterprise teams; the caller decides which to manage.
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @return list of teams
   */
  public static List<GithubTeam> retrieveTeams(String configId, GithubSettings githubSettings, String org) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveTeams");
    debugMap.put("org", org);
    long startTime = System.nanoTime();

    List<GithubTeam> results = new ArrayList<GithubTeam>();

    try {
      List<JsonNode> teamNodes = pageThroughArray(debugMap, "retrieveTeams", configId,
          "/orgs/" + org + "/teams");
      for (JsonNode teamNode : teamNodes) {
        GithubTeam githubTeam = GithubTeam.fromJson(teamNode);
        githubTeam.setOrg(org);
        if (githubSettings != null && githubSettings.isIgnoredTeamSlug(githubTeam.getSlug())) {
          continue;
        }
        results.add(githubTeam);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Retrieve a single team via GET /orgs/{org}/teams/{slug}. Returns null on 404.
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @param slug the team slug
   * @return the team, or null if not found
   */
  public static GithubTeam retrieveTeam(String configId, GithubSettings githubSettings, String org, String slug) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveTeam");
    debugMap.put("org", org);
    debugMap.put("slug", slug);
    long startTime = System.nanoTime();

    try {
      JsonNode root = executeMethod(debugMap, "retrieveTeam", "GET", configId,
          "/orgs/" + org + "/teams/" + slug,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null, false);
      if (root == null || root.get("slug") == null) {
        return null;
      }
      GithubTeam githubTeam = GithubTeam.fromJson(root);
      githubTeam.setOrg(org);
      return githubTeam;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }
  }

  /**
   * Retrieve the members of a team via GET /orgs/{org}/teams/{slug}/members
   * (paged). GitHub returns user objects with login only, so the returned
   * memberships carry the login (role/state are not distinguished by this call).
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @param slug the team slug
   * @return list of memberships (org + teamSlug + userLogin set)
   */
  public static List<GithubMembership> retrieveTeamMemberships(String configId, GithubSettings githubSettings,
      String org, String slug) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveTeamMemberships");
    debugMap.put("org", org);
    debugMap.put("slug", slug);
    long startTime = System.nanoTime();

    List<GithubMembership> results = new ArrayList<GithubMembership>();

    try {
      List<JsonNode> memberNodes = pageThroughArray(debugMap, "retrieveTeamMemberships", configId,
          "/orgs/" + org + "/teams/" + slug + "/members");
      for (JsonNode memberNode : memberNodes) {
        String login = jsonText(memberNode, "login");
        if (githubSettings != null && githubSettings.isIgnoredLogin(login)) {
          continue;
        }
        GithubMembership membership = new GithubMembership();
        membership.setOrg(org);
        membership.setTeamSlug(slug);
        membership.setUserLogin(login);
        results.add(membership);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }

    return results;
  }

  // ============================
  // Entity (account) reads
  // ============================

  /**
   * Retrieve the active members of an org via GET /orgs/{org}/members (paged).
   * Does not include pending invitations. Returns logins + ids only; enrich with
   * samlNameId via {@link #retrieveExternalIdentities} where SAML is configured.
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @return list of active org member accounts
   */
  public static List<GithubUser> retrieveOrgMembers(String configId, GithubSettings githubSettings, String org) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveOrgMembers");
    debugMap.put("org", org);
    long startTime = System.nanoTime();

    List<GithubUser> results = new ArrayList<GithubUser>();

    try {
      List<JsonNode> memberNodes = pageThroughArray(debugMap, "retrieveOrgMembers", configId,
          "/orgs/" + org + "/members");
      for (JsonNode memberNode : memberNodes) {
        GithubUser githubUser = GithubUser.fromJson(memberNode);
        if (githubSettings != null && githubSettings.isIgnoredLogin(githubUser.getLogin())) {
          continue;
        }
        results.add(githubUser);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * The enterprise SAML external-identities query, paged by GraphQL cursor. Maps
   * each account's SAML nameId to its login. See the dev notes for the proven
   * response shape.
   */
  private static final String EXTERNAL_IDENTITIES_QUERY =
      "query($slug:String!,$after:String){ enterprise(slug:$slug){ ownerInfo { samlIdentityProvider "
      + "{ externalIdentities(first:100, after:$after){ pageInfo { hasNextPage endCursor } "
      + "nodes { samlIdentity { nameId } user { login } } } } } } }";

  /**
   * Resolve SAML nameId -&gt; login for the configured enterprise via GraphQL.
   * Returns each SAML-linked account as a GithubUser carrying login + samlNameId.
   * Returns an empty list when no enterprise slug is configured (i.e. SAML
   * identity resolution is not in use). Requires an enterprise-owner token.
   * @param configId external system config id
   * @param githubSettings settings (enterpriseSlug drives this; may be null)
   * @return list of accounts with login + samlNameId, or empty if not applicable
   */
  public static List<GithubUser> retrieveExternalIdentities(String configId, GithubSettings githubSettings) {

    List<GithubUser> results = new ArrayList<GithubUser>();

    String enterpriseSlug = githubSettings == null ? null : githubSettings.getEnterpriseSlug();
    if (StringUtils.isBlank(enterpriseSlug)) {
      return results;
    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveExternalIdentities");
    debugMap.put("enterpriseSlug", enterpriseSlug);
    long startTime = System.nanoTime();

    try {
      String after = null;
      while (true) {
        ObjectNode variables = GrouperUtil.jsonJacksonNode();
        variables.put("slug", enterpriseSlug);
        if (after == null) {
          variables.putNull("after");
        } else {
          variables.put("after", after);
        }

        ObjectNode body = GrouperUtil.jsonJacksonNode();
        body.put("query", EXTERNAL_IDENTITIES_QUERY);
        body.set("variables", variables);

        JsonNode root = executeMethod(debugMap, "retrieveExternalIdentities", "POST", configId, "/graphql",
            GrouperUtil.toSet(200), new int[] { -1 }, GrouperUtil.jsonJacksonToString(body), null, false);

        // GraphQL returns 200 even on query errors; surface them
        JsonNode errorsNode = root == null ? null : root.get("errors");
        if (errorsNode != null && errorsNode.size() > 0) {
          throw new RuntimeException("GraphQL errors resolving external identities for enterprise '"
              + enterpriseSlug + "': " + errorsNode.toString());
        }

        JsonNode externalIdentitiesNode = navigate(root,
            "data", "enterprise", "ownerInfo", "samlIdentityProvider", "externalIdentities");
        if (externalIdentitiesNode == null) {
          break;
        }

        JsonNode nodesNode = externalIdentitiesNode.get("nodes");
        if (nodesNode != null && nodesNode.isArray()) {
          for (int i = 0; i < nodesNode.size(); i++) {
            JsonNode node = nodesNode.get(i);
            String nameId = jsonText(navigate(node, "samlIdentity"), "nameId");
            String login = jsonText(navigate(node, "user"), "login");
            // user can be null for a deprovisioned account; skip those
            if (StringUtils.isBlank(login)) {
              continue;
            }
            GithubUser githubUser = new GithubUser();
            githubUser.setLogin(login);
            githubUser.setSamlNameId(nameId);
            results.add(githubUser);
          }
        }

        // live progress across GraphQL cursor pages
        GrouperProvisioner currentProvisioner = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
        if (currentProvisioner != null) {
          currentProvisioner.assignProgressLabelTarget("retrieveExternalIdentities: " + results.size() + " so far");
        }

        JsonNode pageInfo = externalIdentitiesNode.get("pageInfo");
        boolean hasNextPage = pageInfo != null && pageInfo.get("hasNextPage") != null
            && pageInfo.get("hasNextPage").asBoolean(false);
        if (!hasNextPage) {
          break;
        }
        after = jsonText(pageInfo, "endCursor");
        if (StringUtils.isBlank(after)) {
          break;
        }
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Retrieve org membership state for a login via GET /orgs/{org}/memberships/{login}.
   * Returns null on 404 (login not known/linked, or not a member). On 200 the
   * returned user carries orgState + role.
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @param login the account login
   * @return the membership as a GithubUser, or null if not a member
   */
  public static GithubUser retrieveOrgMembership(String configId, GithubSettings githubSettings,
      String org, String login) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveOrgMembership");
    debugMap.put("org", org);
    debugMap.put("login", login);
    long startTime = System.nanoTime();

    try {
      JsonNode root = executeMethod(debugMap, "retrieveOrgMembership", "GET", configId,
          "/orgs/" + org + "/memberships/" + login,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null, false);
      if (root == null || root.get("state") == null) {
        return null;
      }
      GithubUser githubUser = new GithubUser();
      githubUser.setLogin(login);
      githubUser.setOrgState(jsonText(root, "state"));
      githubUser.setRole(jsonText(root, "role"));
      JsonNode userNode = root.get("user");
      if (userNode != null) {
        githubUser.setId(jsonText(userNode, "id"));
      }
      return githubUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }
  }

  /**
   * Retrieve pending org invitations via GET /orgs/{org}/invitations (paged).
   * Used to avoid re-inviting; each record carries email + invitationId (login is
   * usually null until accepted).
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @return list of pending invitation records
   */
  public static List<GithubUser> retrieveOrgInvitations(String configId, GithubSettings githubSettings, String org) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveOrgInvitations");
    debugMap.put("org", org);
    long startTime = System.nanoTime();

    List<GithubUser> results = new ArrayList<GithubUser>();

    try {
      List<JsonNode> inviteNodes = pageThroughArray(debugMap, "retrieveOrgInvitations", configId,
          "/orgs/" + org + "/invitations");
      for (JsonNode inviteNode : inviteNodes) {
        GithubUser githubUser = new GithubUser();
        githubUser.setLogin(jsonText(inviteNode, "login"));
        githubUser.setEmail(jsonText(inviteNode, "email"));
        githubUser.setInvitationId(jsonText(inviteNode, "id"));
        githubUser.setOrgState("pending");
        results.add(githubUser);
      }
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }

    return results;
  }

  // ============================
  // Writes: invite, membership, deprovision
  // ============================

  /**
   * Invite an email to the org via POST /orgs/{org}/invitations (async account
   * bootstrap). Optionally assign org teams in the same call via team_ids
   * (numeric ORG team ids only). Returns the pending invitation as a GithubUser
   * (email + invitationId + orgState=pending; login is null until accepted).
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @param email the email to invite
   * @param teamIds numeric org team ids to assign, or null/empty for none
   * @return the pending invitation record
   */
  public static GithubUser inviteToOrg(String configId, GithubSettings githubSettings, String org,
      String email, List<Long> teamIds) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "inviteToOrg");
    debugMap.put("org", org);
    debugMap.put("email", email);
    long startTime = System.nanoTime();

    try {
      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put("email", email);
      body.put("role", "direct_member");
      if (teamIds != null && !teamIds.isEmpty()) {
        ArrayNode teamIdsNode = body.putArray("team_ids");
        for (Long teamId : teamIds) {
          teamIdsNode.add(teamId);
        }
      }

      JsonNode root = executeMethod(debugMap, "inviteToOrg", "POST", configId,
          "/orgs/" + org + "/invitations",
          GrouperUtil.toSet(201), new int[] { -1 }, GrouperUtil.jsonJacksonToString(body), null, false);

      GithubUser githubUser = new GithubUser();
      githubUser.setEmail(email);
      githubUser.setOrgState("pending");
      if (root != null) {
        githubUser.setInvitationId(jsonText(root, "id"));
        githubUser.setLogin(jsonText(root, "login"));
      }
      return githubUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }
  }

  /**
   * Cancel a pending org invitation via DELETE /orgs/{org}/invitations/{id}.
   * Idempotent: 204 even if already gone (404 allowed).
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @param invitationId the invitation id
   */
  public static void cancelOrgInvitation(String configId, GithubSettings githubSettings, String org,
      String invitationId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "cancelOrgInvitation");
    debugMap.put("org", org);
    debugMap.put("invitationId", invitationId);
    long startTime = System.nanoTime();

    try {
      executeMethod(debugMap, "cancelOrgInvitation", "DELETE", configId,
          "/orgs/" + org + "/invitations/" + invitationId,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, null, false);
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }
  }

  /**
   * Add a login to a team via PUT /orgs/{org}/teams/{slug}/memberships/{login}.
   * Requires org membership to already exist (else GitHub returns state=pending).
   * Idempotent: 200 even if already a member.
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @param slug the team slug
   * @param login the account login
   * @param role "member" (default) or "maintainer"
   */
  public static void addTeamMembership(String configId, GithubSettings githubSettings, String org,
      String slug, String login, String role) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "addTeamMembership");
    debugMap.put("org", org);
    debugMap.put("slug", slug);
    debugMap.put("login", login);
    long startTime = System.nanoTime();

    try {
      ObjectNode body = GrouperUtil.jsonJacksonNode();
      body.put("role", StringUtils.isBlank(role) ? "member" : role);

      executeMethod(debugMap, "addTeamMembership", "PUT", configId,
          "/orgs/" + org + "/teams/" + slug + "/memberships/" + login,
          GrouperUtil.toSet(200), new int[] { -1 }, GrouperUtil.jsonJacksonToString(body), null, false);
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }
  }

  /**
   * Remove a login from a team via DELETE /orgs/{org}/teams/{slug}/memberships/{login}.
   * Granular (the account stays an org member) and idempotent: 204 even if not a
   * member (404 allowed).
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @param slug the team slug
   * @param login the account login
   */
  public static void removeTeamMembership(String configId, GithubSettings githubSettings, String org,
      String slug, String login) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "removeTeamMembership");
    debugMap.put("org", org);
    debugMap.put("slug", slug);
    debugMap.put("login", login);
    long startTime = System.nanoTime();

    try {
      executeMethod(debugMap, "removeTeamMembership", "DELETE", configId,
          "/orgs/" + org + "/teams/" + slug + "/memberships/" + login,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, null, false);
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }
  }

  /**
   * Remove a login from the org entirely (full deprovision) via
   * DELETE /orgs/{org}/memberships/{login}. Also drops all of that account's team
   * memberships in the org. Idempotent: 204 even if not a member (404 allowed).
   * @param configId external system config id
   * @param githubSettings settings (may be null)
   * @param org the org login
   * @param login the account login
   */
  public static void removeOrgMembership(String configId, GithubSettings githubSettings, String org, String login) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "removeOrgMembership");
    debugMap.put("org", org);
    debugMap.put("login", login);
    long startTime = System.nanoTime();

    try {
      executeMethod(debugMap, "removeOrgMembership", "DELETE", configId,
          "/orgs/" + org + "/memberships/" + login,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, null, false);
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GithubLog.githubLog(debugMap, startTime);
    }
  }

  /**
   * Walk a chain of object field names from a starting node.
   * @param node the starting node
   * @param fieldNames the field names to descend
   * @return the node at the end of the chain, or null if any link is missing
   */
  private static JsonNode navigate(JsonNode node, String... fieldNames) {
    JsonNode current = node;
    for (String fieldName : fieldNames) {
      if (current == null) {
        return null;
      }
      current = current.get(fieldName);
    }
    return current;
  }

}
