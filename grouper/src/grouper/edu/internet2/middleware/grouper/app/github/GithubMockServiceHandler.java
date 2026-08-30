package edu.internet2.middleware.grouper.app.github;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Mock GitHub API for provisioner tests. Serves a small, faithful subset of the
 * GitHub REST + GraphQL surface the provisioner uses, backed by the
 * mock_github_* tables.
 *
 * <p>Key modeling choice, matching the provisioner's design: <b>org membership
 * is derived from team membership</b>. A login is an org member iff it has at
 * least one row in mock_github_membership for that org. So a team-add (PUT)
 * makes someone an org member, and an org deprovision (DELETE org membership)
 * drops all their team rows. This keeps the mock consistent with the real
 * GitHub behavior we validated (team-add issues the pending org invite; org
 * removal drops team memberships).</p>
 *
 * <p>Invitation endpoints are stateless here (201 with a generated id, empty
 * list, 204 cancel): the v1 provisioner flow provisions by team-add rather than
 * by email invitation, so invitations are exercised only by direct API-level
 * tests.</p>
 */
public class GithubMockServiceHandler extends MockServiceHandler {

  public GithubMockServiceHandler() {
  }

  /** redact the bearer token in logs */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  @Override
  public Set<String> doNotLogParameters() {
    return null;
  }

  private static boolean mockTablesThere = false;

  /**
   * Create the mock_github_* tables if they are not present.
   */
  public static void ensureGithubMockTables() {
    try {
      new GcDbAccess().sql("select count(*) from mock_github_team").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_github_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_github_membership").select(int.class);
    } catch (Exception e) {
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          GithubTeam.createTableGithubTeam(ddlVersionBean, database);
          GithubUser.createTableGithubUser(ddlVersionBean, database);
          GithubMembership.createTableGithubMembership(ddlVersionBean, database);
        }
      });
    }
  }

  /**
   * Validate the Authorization: Bearer &lt;token&gt; header against the configured
   * accessTokenPassword. Throws on mismatch (callers map that to 403).
   * @param mockServiceRequest the request
   */
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String authorization = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (StringUtils.isBlank(authorization) || !authorization.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization: Bearer <token> header is required");
    }
    String token = authorization.substring("Bearer ".length()).trim();

    String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired(
        "grouperTest.exampleGithub.mockExternalSystem.configId");
    String expectedToken = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");

    if (!StringUtils.equals(expectedToken, token)) {
      throw new RuntimeException("Bearer token does not match");
    }
  }

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureGithubMockTables();
    }
    mockTablesThere = true;

    // The test JVM writes grouperTest.exampleGithub.mockExternalSystem.configId to the DB config;
    // this (Tomcat) JVM's config cache needs a moment to pick it up. Wait for it before handling
    // anything, so checkAuthorization's required-property read does not fail with a 500.
    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.exampleGithub.mockExternalSystem.configId");
    if (StringUtils.isBlank(configId)) {
      for (int i = 0; i < 40; i++) {
        configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.exampleGithub.mockExternalSystem.configId");
        if (!StringUtils.isBlank(configId)) {
          break;
        }
        if (i == 39) {
          throw new RuntimeException("grouper.properties grouperTest.exampleGithub.mockExternalSystem.configId must be set!");
        }
        GrouperUtil.sleep(1000);
      }
    }

    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }

    List<String> paths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    String httpMethod = mockServiceRequest.getHttpServletRequest().getMethod();
    int size = paths.size();

    // POST /graphql
    if ("POST".equals(httpMethod) && size == 1 && "graphql".equals(paths.get(0))) {
      postGraphql(mockServiceRequest, mockServiceResponse);
      return;
    }

    // everything else is under /orgs/{org}/...
    if (size >= 3 && "orgs".equals(paths.get(0))) {
      String org = paths.get(1);
      String resource = paths.get(2);

      if ("teams".equals(resource)) {
        // GET /orgs/{org}/teams
        if ("GET".equals(httpMethod) && size == 3) {
          getTeams(mockServiceRequest, mockServiceResponse, org);
          return;
        }
        // GET /orgs/{org}/teams/{slug}
        if ("GET".equals(httpMethod) && size == 4) {
          getTeam(mockServiceRequest, mockServiceResponse, org, paths.get(3));
          return;
        }
        // GET /orgs/{org}/teams/{slug}/members
        if ("GET".equals(httpMethod) && size == 5 && "members".equals(paths.get(4))) {
          getTeamMembers(mockServiceRequest, mockServiceResponse, org, paths.get(3));
          return;
        }
        // PUT /orgs/{org}/teams/{slug}/memberships/{login}
        if ("PUT".equals(httpMethod) && size == 6 && "memberships".equals(paths.get(4))) {
          putTeamMembership(mockServiceRequest, mockServiceResponse, org, paths.get(3), paths.get(5));
          return;
        }
        // DELETE /orgs/{org}/teams/{slug}/memberships/{login}
        if ("DELETE".equals(httpMethod) && size == 6 && "memberships".equals(paths.get(4))) {
          deleteTeamMembership(mockServiceRequest, mockServiceResponse, org, paths.get(3), paths.get(5));
          return;
        }
      }

      if ("members".equals(resource) && "GET".equals(httpMethod) && size == 3) {
        getOrgMembers(mockServiceRequest, mockServiceResponse, org);
        return;
      }

      if ("memberships".equals(resource) && size == 4) {
        // GET /orgs/{org}/memberships/{login}
        if ("GET".equals(httpMethod)) {
          getOrgMembership(mockServiceRequest, mockServiceResponse, org, paths.get(3));
          return;
        }
        // DELETE /orgs/{org}/memberships/{login}
        if ("DELETE".equals(httpMethod)) {
          deleteOrgMembership(mockServiceRequest, mockServiceResponse, org, paths.get(3));
          return;
        }
      }

      if ("invitations".equals(resource)) {
        // GET /orgs/{org}/invitations
        if ("GET".equals(httpMethod) && size == 3) {
          getInvitations(mockServiceRequest, mockServiceResponse);
          return;
        }
        // POST /orgs/{org}/invitations
        if ("POST".equals(httpMethod) && size == 3) {
          postInvitation(mockServiceRequest, mockServiceResponse);
          return;
        }
        // DELETE /orgs/{org}/invitations/{id}
        if ("DELETE".equals(httpMethod) && size == 4) {
          mockServiceResponse.setResponseCode(204);
          return;
        }
      }
    }

    throw new RuntimeException("Unhandled GitHub mock request: " + httpMethod + " " + StringUtils.join(paths, "/"));
  }

  /**
   * Set a 403 and rethrow if auth fails; otherwise return normally.
   * @param mockServiceRequest the request
   * @param mockServiceResponse the response
   */
  private void authorize(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }
  }

  /**
   * GET /orgs/{org}/teams
   */
  public void getTeams(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String org) {
    authorize(mockServiceRequest, mockServiceResponse);

    List<GithubTeam> teams = HibernateSession.byHqlStatic()
        .createQuery("from GithubTeam where org = :theOrg order by slug")
        .setString("theOrg", org).list(GithubTeam.class);

    ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
    for (GithubTeam team : GrouperUtil.nonNull(teams)) {
      ObjectNode teamNode = GrouperUtil.jsonJacksonNode();
      teamNode.put("id", GrouperUtil.longValue(team.getId()));
      teamNode.put("slug", team.getSlug());
      teamNode.put("name", team.getName());
      if (team.getPrivacy() != null) {
        teamNode.put("privacy", team.getPrivacy());
      }
      teamNode.put("description", team.getDescription());
      if (team.isEnterpriseTeam()) {
        teamNode.put("type", "enterprise");
      }
      arrayNode.add(teamNode);
    }

    writeJson(mockServiceResponse, 200, arrayNode);
  }

  /**
   * GET /orgs/{org}/teams/{slug}
   */
  public void getTeam(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String org, String slug) {
    authorize(mockServiceRequest, mockServiceResponse);

    GithubTeam team = HibernateSession.byHqlStatic()
        .createQuery("from GithubTeam where org = :theOrg and slug = :theSlug")
        .setString("theOrg", org).setString("theSlug", slug).uniqueResult(GithubTeam.class);

    if (team == null) {
      writeError(mockServiceResponse, 404, "Not Found");
      return;
    }

    ObjectNode teamNode = GrouperUtil.jsonJacksonNode();
    teamNode.put("id", GrouperUtil.longValue(team.getId()));
    teamNode.put("slug", team.getSlug());
    teamNode.put("name", team.getName());
    if (team.getPrivacy() != null) {
      teamNode.put("privacy", team.getPrivacy());
    }
    teamNode.put("description", team.getDescription());
    writeJson(mockServiceResponse, 200, teamNode);
  }

  /**
   * GET /orgs/{org}/teams/{slug}/members
   */
  public void getTeamMembers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String org, String slug) {
    authorize(mockServiceRequest, mockServiceResponse);

    List<GithubMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from GithubMembership where org = :theOrg and teamSlug = :theSlug order by userLogin")
        .setString("theOrg", org).setString("theSlug", slug).list(GithubMembership.class);

    ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
    for (GithubMembership membership : GrouperUtil.nonNull(memberships)) {
      ObjectNode userNode = GrouperUtil.jsonJacksonNode();
      userNode.put("login", membership.getUserLogin());
      userNode.put("type", "User");
      arrayNode.add(userNode);
    }

    writeJson(mockServiceResponse, 200, arrayNode);
  }

  /**
   * GET /orgs/{org}/members - derived from team memberships (distinct logins).
   */
  public void getOrgMembers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String org) {
    authorize(mockServiceRequest, mockServiceResponse);

    List<String> logins = HibernateSession.byHqlStatic()
        .createQuery("select distinct userLogin from GithubMembership where org = :theOrg order by userLogin")
        .setString("theOrg", org).list(String.class);

    ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
    for (String login : GrouperUtil.nonNull(logins)) {
      ObjectNode userNode = GrouperUtil.jsonJacksonNode();
      userNode.put("login", login);
      userNode.put("type", "User");
      arrayNode.add(userNode);
    }

    writeJson(mockServiceResponse, 200, arrayNode);
  }

  /**
   * GET /orgs/{org}/memberships/{login} - active iff the login has any team
   * membership in the org (derived), else 404.
   */
  public void getOrgMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String org, String login) {
    authorize(mockServiceRequest, mockServiceResponse);

    long count = HibernateSession.byHqlStatic()
        .createQuery("select count(*) from GithubMembership where org = :theOrg and userLogin = :theLogin")
        .setString("theOrg", org).setString("theLogin", login).uniqueResult(Long.class);

    if (count == 0) {
      writeError(mockServiceResponse, 404, "Not Found");
      return;
    }

    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.put("state", "active");
    node.put("role", "member");
    node.put("direct_membership", true);
    node.set("enterprise_teams_providing_indirect_membership", GrouperUtil.jsonJacksonArrayNode());
    ObjectNode userNode = GrouperUtil.jsonJacksonNode();
    userNode.put("login", login);
    node.set("user", userNode);
    writeJson(mockServiceResponse, 200, node);
  }

  /**
   * PUT /orgs/{org}/teams/{slug}/memberships/{login} - idempotent team add. Makes
   * the login an org member (derived). Requires the team to exist.
   */
  public void putTeamMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse,
      String org, String slug, String login) {
    authorize(mockServiceRequest, mockServiceResponse);

    GithubTeam team = HibernateSession.byHqlStatic()
        .createQuery("from GithubTeam where org = :theOrg and slug = :theSlug")
        .setString("theOrg", org).setString("theSlug", slug).uniqueResult(GithubTeam.class);
    if (team == null) {
      writeError(mockServiceResponse, 404, "Team not found");
      return;
    }

    GithubMembership existing = HibernateSession.byHqlStatic()
        .createQuery("from GithubMembership where org = :theOrg and teamSlug = :theSlug and userLogin = :theLogin")
        .setString("theOrg", org).setString("theSlug", slug).setString("theLogin", login)
        .uniqueResult(GithubMembership.class);

    if (existing == null) {
      GithubMembership membership = new GithubMembership();
      membership.setId(GrouperUuid.getUuid());
      membership.setOrg(org);
      membership.setTeamSlug(slug);
      membership.setTeamId(team.getId());
      membership.setUserLogin(login);
      membership.setRole("member");
      membership.setState("active");
      HibernateSession.byObjectStatic().save(membership);
    }

    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.put("state", "active");
    node.put("role", "member");
    node.put("url", "https://api.github.com/organizations/0/team/" + team.getId() + "/memberships/" + login);
    writeJson(mockServiceResponse, 200, node);
  }

  /**
   * DELETE /orgs/{org}/teams/{slug}/memberships/{login} - idempotent (204 even if
   * absent). Removes only the team membership.
   */
  public void deleteTeamMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse,
      String org, String slug, String login) {
    authorize(mockServiceRequest, mockServiceResponse);

    GithubMembership membership = HibernateSession.byHqlStatic()
        .createQuery("from GithubMembership where org = :theOrg and teamSlug = :theSlug and userLogin = :theLogin")
        .setString("theOrg", org).setString("theSlug", slug).setString("theLogin", login)
        .uniqueResult(GithubMembership.class);
    if (membership != null) {
      HibernateSession.byObjectStatic().delete(membership);
    }
    mockServiceResponse.setResponseCode(204);
  }

  /**
   * DELETE /orgs/{org}/memberships/{login} - full deprovision: drop all of the
   * login's team memberships in the org. Idempotent (204).
   */
  public void deleteOrgMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse,
      String org, String login) {
    authorize(mockServiceRequest, mockServiceResponse);

    new GcDbAccess().connectionName("grouper")
        .sql("delete from mock_github_membership where org = ? and user_login = ?")
        .addBindVar(org).addBindVar(login).executeSql();
    mockServiceResponse.setResponseCode(204);
  }

  /**
   * GET /orgs/{org}/invitations - stateless (no pending invitations tracked).
   */
  public void getInvitations(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);
    writeJson(mockServiceResponse, 200, GrouperUtil.jsonJacksonArrayNode());
  }

  /**
   * POST /orgs/{org}/invitations - stateless: 201 with a generated invitation id.
   */
  public void postInvitation(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    JsonNode requestNode = GrouperUtil.jsonJacksonNode(mockServiceRequest.getRequestBody());
    String email = GithubApiCommands.jsonText(requestNode, "email");

    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.put("id", Math.abs(GrouperUuid.getUuid().hashCode()));
    node.putNull("login");
    node.put("email", email);
    node.put("role", "direct_member");
    node.put("team_count", 0);
    writeJson(mockServiceResponse, 201, node);
  }

  /**
   * POST /graphql - returns the enterprise SAML external-identities map from every
   * mock_github_user that has a saml_name_id set. (The query shape is fixed, so it
   * is not parsed.)
   */
  public void postGraphql(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    authorize(mockServiceRequest, mockServiceResponse);

    List<GithubUser> users = HibernateSession.byHqlStatic()
        .createQuery("from GithubUser where samlNameId is not null order by login")
        .list(GithubUser.class);

    ArrayNode nodesArray = GrouperUtil.jsonJacksonArrayNode();
    for (GithubUser user : GrouperUtil.nonNull(users)) {
      ObjectNode nodeObj = GrouperUtil.jsonJacksonNode();
      ObjectNode samlIdentity = GrouperUtil.jsonJacksonNode();
      samlIdentity.put("nameId", user.getSamlNameId());
      nodeObj.set("samlIdentity", samlIdentity);
      ObjectNode userObj = GrouperUtil.jsonJacksonNode();
      userObj.put("login", user.getLogin());
      nodeObj.set("user", userObj);
      nodesArray.add(nodeObj);
    }

    ObjectNode pageInfo = GrouperUtil.jsonJacksonNode();
    pageInfo.put("hasNextPage", false);
    pageInfo.putNull("endCursor");

    ObjectNode externalIdentities = GrouperUtil.jsonJacksonNode();
    externalIdentities.set("pageInfo", pageInfo);
    externalIdentities.set("nodes", nodesArray);

    ObjectNode samlIdp = GrouperUtil.jsonJacksonNode();
    samlIdp.set("externalIdentities", externalIdentities);
    ObjectNode ownerInfo = GrouperUtil.jsonJacksonNode();
    ownerInfo.set("samlIdentityProvider", samlIdp);
    ObjectNode enterprise = GrouperUtil.jsonJacksonNode();
    enterprise.set("ownerInfo", ownerInfo);
    ObjectNode data = GrouperUtil.jsonJacksonNode();
    data.set("enterprise", enterprise);
    ObjectNode root = GrouperUtil.jsonJacksonNode();
    root.set("data", data);

    writeJson(mockServiceResponse, 200, root);
  }

  /**
   * Write a JSON node body with a status code.
   */
  private void writeJson(MockServiceResponse mockServiceResponse, int code, JsonNode node) {
    mockServiceResponse.setResponseCode(code);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(node));
  }

  /**
   * Write a GitHub-style error body with a status code.
   */
  private void writeError(MockServiceResponse mockServiceResponse, int code, String message) {
    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.put("message", message);
    writeJson(mockServiceResponse, code, node);
  }

}
