package edu.internet2.middleware.grouper.app.truefoundry;

import java.util.HashSet;
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
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class TrueFoundryMockServiceHandler extends MockServiceHandler {

  public TrueFoundryMockServiceHandler() {
  }

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

  public static void ensureTrueFoundryMockTables() {
    try {
      new GcDbAccess().sql("select count(*) from mock_truefoundry_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_truefoundry_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_truefoundry_membership").select(int.class);
    } catch (Exception e) {

      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          TrueFoundryUser.createTableTrueFoundryUser(ddlVersionBean, database);
          TrueFoundryGroup.createTableTrueFoundryGroup(ddlVersionBean, database);
          TrueFoundryMembership.createTableTrueFoundryMembership(ddlVersionBean, database);

        }
      });

    }

  }

  /**
   * Check authorization by validating the Bearer token in the Authorization header.
   * The accessTokenPassword is a JSON string with "apiToken" and "scimToken" fields.
   * Accepts either token.
   */
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String authHeader = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization: Bearer header is required");
    }
    String bearerToken = authHeader.substring("Bearer ".length()).trim();

    String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired(
        "grouperTest.exampleTrueFoundry.mockExternalSystem.configId");
    String accessTokenPassword = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");

    JsonNode keysNode = GrouperUtil.jsonJacksonNode(accessTokenPassword);
    String apiToken = GrouperUtil.jsonJacksonGetString(keysNode, "apiToken");
    String scimToken = GrouperUtil.jsonJacksonGetString(keysNode, "scimToken");

    if (!StringUtils.equals(apiToken, bearerToken) && !StringUtils.equals(scimToken, bearerToken)) {
      throw new RuntimeException("Authorization Bearer token does not match apiToken or scimToken");
    }
  }

  // ==================== User operations ====================

  /**
   * GET /subjects - retrieve users, optionally filtered by query parameter (email search).
   * Supports limit and offset for paging. showInvalidUsers includes inactive users.
   * Response: {"users": [...], "totalUsers": N}
   */
  public void getSubjects(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String query = mockServiceRequest.getHttpServletRequest().getParameter("query");
    String limitParam = mockServiceRequest.getHttpServletRequest().getParameter("limit");
    String offsetParam = mockServiceRequest.getHttpServletRequest().getParameter("offset");

    int limit = StringUtils.isBlank(limitParam) ? 100 : Integer.parseInt(limitParam);
    int offset = StringUtils.isBlank(offsetParam) ? 0 : Integer.parseInt(offsetParam);

    List<TrueFoundryUser> users;

    if (StringUtils.isNotBlank(query)) {
      // search by email (case-insensitive contains match)
      users = HibernateSession.byHqlStatic()
          .createQuery("from TrueFoundryUser where lower(email) like :theQuery")
          .setString("theQuery", "%" + query.toLowerCase() + "%")
          .list(TrueFoundryUser.class);
    } else {
      // return all users sorted by id, with paging applied in memory
      ByHqlStatic hqlQuery = HibernateSession.byHqlStatic().createQuery("from TrueFoundryUser");
      QueryOptions queryOptions = new QueryOptions();
      queryOptions.sort(new QuerySort("id", true));
      hqlQuery.options(queryOptions);
      List<TrueFoundryUser> allUsers = hqlQuery.list(TrueFoundryUser.class);
      int start = Math.min(offset, allUsers.size());
      int end = Math.min(offset + limit, allUsers.size());
      users = allUsers.subList(start, end);
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode usersArray = GrouperUtil.jsonJacksonArrayNode();

    for (TrueFoundryUser user : users) {
      usersArray.add(buildUserJson(user));
    }

    resultNode.set("users", usersArray);
    resultNode.put("totalUsers", users.size());

    // include all teams with their members and managers in the subjects response
    List<TrueFoundryGroup> allTeams = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryGroup where groupType = :theType")
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_TEAM)
        .list(TrueFoundryGroup.class);
    ArrayNode teamsArray = GrouperUtil.jsonJacksonArrayNode();
    for (TrueFoundryGroup team : allTeams) {
      teamsArray.add(buildTeamJson(team));
    }
    resultNode.set("teams", teamsArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * Build a user JSON object in TrueFoundry subjects API format.
   * Format: {"id": "...", "email": "...", "active": true, "metadata": {"displayName": "..."},
   *          "rolesWithResource": [{"roleId": "...", "resourceType": "account", "resourceId": "..."}]}
   */
  private ObjectNode buildUserJson(TrueFoundryUser user) {
    ObjectNode userNode = GrouperUtil.jsonJacksonNode();
    userNode.put("id", user.getId());
    userNode.put("email", user.getEmail());
    userNode.put("active", user.getActive() == null || user.getActive());

    if (user.getDisplayName() != null) {
      ObjectNode metadataNode = GrouperUtil.jsonJacksonNode();
      metadataNode.put("displayName", user.getDisplayName());
      userNode.set("metadata", metadataNode);
    }

    // include rolesWithResource from role memberships for this user
    ArrayNode rolesWithResourceArray = GrouperUtil.jsonJacksonArrayNode();
    List<TrueFoundryMembership> roleMemberships = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryMembership where userEmail = :theEmail and groupId in "
            + "(select id from TrueFoundryGroup where groupType = :theType)")
        .setString("theEmail", user.getEmail())
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_ROLE)
        .list(TrueFoundryMembership.class);
    for (TrueFoundryMembership roleMembership : GrouperUtil.nonNull(roleMemberships)) {
      // look up the role to get its resourceType
      List<TrueFoundryGroup> roles = HibernateSession.byHqlStatic()
          .createQuery("from TrueFoundryGroup where id = :theId")
          .setString("theId", roleMembership.getGroupId())
          .list(TrueFoundryGroup.class);
      if (GrouperUtil.length(roles) > 0) {
        TrueFoundryGroup role = roles.get(0);
        ObjectNode roleWithResource = GrouperUtil.jsonJacksonNode();
        roleWithResource.put("roleId", role.getId());
        roleWithResource.put("resourceType", StringUtils.defaultIfBlank(role.getResourceType(), "account"));
        roleWithResource.put("resourceId", "mock-resource-id");
        rolesWithResourceArray.add(roleWithResource);
      }
    }
    userNode.set("rolesWithResource", rolesWithResourceArray);

    return userNode;
  }

  /**
   * POST /users/register - register a new user.
   * Body: {"email": "...", "sendInviteEmail": false}
   * Returns 200 {} (no user ID in response — caller must use GET /subjects to look up ID).
   * Idempotent: if user already exists, returns 200 without error.
   */
  public void postRegisterUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    String email = GrouperUtil.jsonJacksonGetString(requestNode, "email");

    if (StringUtils.isBlank(email)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"email is required\"}");
      return;
    }

    // idempotent: only create if not already present
    TrueFoundryUser existing = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryUser where email = :theEmail")
        .setString("theEmail", email)
        .uniqueResult(TrueFoundryUser.class);

    if (existing == null) {
      TrueFoundryUser user = new TrueFoundryUser();
      user.setId(GrouperUuid.getUuid());
      user.setEmail(email);
      user.setActive(true);
      HibernateSession.byObjectStatic().save(user);
    }

    // real API returns 200 {} with no user ID
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody("{}");
  }

  /**
   * PATCH /users/deactivate - set user active=false.
   * Body: {"email": "..."}
   */
  public void patchDeactivateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    String email = GrouperUtil.jsonJacksonGetString(requestNode, "email");

    if (StringUtils.isBlank(email)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"email is required\"}");
      return;
    }

    TrueFoundryUser user = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryUser where email = :theEmail")
        .setString("theEmail", email)
        .uniqueResult(TrueFoundryUser.class);

    if (user == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"User not found\"}");
      return;
    }

    user.setActive(false);
    HibernateSession.byObjectStatic().update(user);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody("{}");
  }

  /**
   * PATCH /users/activate - set user active=true.
   * Body: {"email": "..."}
   */
  public void patchActivateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    String email = GrouperUtil.jsonJacksonGetString(requestNode, "email");

    if (StringUtils.isBlank(email)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"email is required\"}");
      return;
    }

    TrueFoundryUser user = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryUser where email = :theEmail")
        .setString("theEmail", email)
        .uniqueResult(TrueFoundryUser.class);

    if (user == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"User not found\"}");
      return;
    }

    user.setActive(true);
    HibernateSession.byObjectStatic().update(user);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody("{}");
  }

  /**
   * PATCH /users/roles - assign a role to a user by email.
   * Body: {"email": "...", "roles": ["roleName"], "resourceType": "account"}
   * Looks up the role by name and upserts the membership.
   */
  public void patchAssignUserRole(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    String email = GrouperUtil.jsonJacksonGetString(requestNode, "email");
    ArrayNode rolesArray = GrouperUtil.jsonJacksonGetArrayNode(requestNode, "roles");

    if (StringUtils.isBlank(email)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"email is required\"}");
      return;
    }

    if (rolesArray == null || rolesArray.size() == 0) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"roles array is required\"}");
      return;
    }

    // verify user exists
    TrueFoundryUser user = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryUser where email = :theEmail")
        .setString("theEmail", email)
        .uniqueResult(TrueFoundryUser.class);

    if (user == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"User not found\"}");
      return;
    }

    String roleName = rolesArray.get(0).asText();

    // find the role by name
    TrueFoundryGroup role = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryGroup where name = :theName and groupType = :theType")
        .setString("theName", roleName)
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_ROLE)
        .uniqueResult(TrueFoundryGroup.class);

    if (role == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"Role not found: " + roleName + "\"}");
      return;
    }

    // TrueFoundry replaces the user's role — remove any existing role memberships for this user first
    // (only role memberships, not team memberships)
    new GcDbAccess().sql("delete from mock_truefoundry_membership where user_email = ? and group_id in "
        + "(select id from mock_truefoundry_group where group_type = 'role')")
        .addBindVar(email).executeSql();

    // insert the new role membership
    TrueFoundryMembership membership = new TrueFoundryMembership();
    membership.setId(GrouperUuid.getUuid());
    membership.setGroupId(role.getId());
    membership.setUserEmail(email);
    HibernateSession.byObjectStatic().save(membership);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody("{}");
  }

  /**
   * PATCH /scim/v2/{tenantName}/{ssoId}/Users/{userId} - update display name via SCIM PatchOp.
   * Body: {"schemas": [...], "Operations": [{"op": "replace", "path": "displayName", "value": "..."}]}
   */
  public void patchScimUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String userId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    TrueFoundryUser user = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryUser where id = :theId")
        .setString("theId", userId)
        .uniqueResult(TrueFoundryUser.class);

    if (user == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"User not found\"}");
      return;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    ArrayNode operationsArray = GrouperUtil.jsonJacksonGetArrayNode(requestNode, "Operations");

    if (operationsArray != null) {
      for (int i = 0; i < operationsArray.size(); i++) {
        JsonNode operation = operationsArray.get(i);
        String path = GrouperUtil.jsonJacksonGetString(operation, "path");
        if ("displayName".equals(path)) {
          String value = GrouperUtil.jsonJacksonGetString(operation, "value");
          user.setDisplayName(value);
        }
      }
    }

    HibernateSession.byObjectStatic().update(user);

    // return minimal SCIM user response
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    resultNode.put("id", user.getId());
    if (user.getDisplayName() != null) {
      resultNode.put("displayName", user.getDisplayName());
    }

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  // ==================== Role operations ====================

  /**
   * GET /role/list - retrieve all roles.
   * Response: {"data": [{"id": "...", "name": "...", "resourceType": "account",
   *   "isDefault": false, "manifest": {"displayName": "...", "description": "..."}}]}
   */
  public void getRoleList(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryGroup where groupType = :theType")
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_ROLE);
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);
    List<TrueFoundryGroup> roles = query.list(TrueFoundryGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode dataArray = GrouperUtil.jsonJacksonArrayNode();

    for (TrueFoundryGroup role : roles) {
      dataArray.add(buildRoleJson(role));
    }

    resultNode.set("data", dataArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * Build a role JSON object in TrueFoundry role API format.
   */
  private ObjectNode buildRoleJson(TrueFoundryGroup role) {
    ObjectNode roleNode = GrouperUtil.jsonJacksonNode();
    roleNode.put("id", role.getId());
    roleNode.put("name", role.getName());
    roleNode.put("resourceType", role.getResourceType() != null ? role.getResourceType() : "account");
    roleNode.put("isDefault", role.getIsDefault() != null && role.getIsDefault());

    ObjectNode manifestNode = GrouperUtil.jsonJacksonNode();
    manifestNode.put("displayName", role.getDisplayName() != null ? role.getDisplayName() : role.getName());
    manifestNode.put("description", role.getDescription() != null ? role.getDescription() : "");
    roleNode.set("manifest", manifestNode);

    return roleNode;
  }

  /**
   * PUT /role - create or update a role (upsert by name).
   * Body: {"manifest": {"type": "role", "name": "...", "displayName": "...",
   *   "resourceType": "account", "description": "...", "permissions": [...]}}
   * Response: {"data": {...role...}}
   */
  public void putRole(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode manifestNode = GrouperUtil.jsonJacksonGetNode(requestNode, "manifest");

    if (manifestNode == null) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"manifest is required\"}");
      return;
    }

    String name = GrouperUtil.jsonJacksonGetString(manifestNode, "name");
    if (StringUtils.isBlank(name)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"manifest.name is required\"}");
      return;
    }

    String displayName = GrouperUtil.jsonJacksonGetString(manifestNode, "displayName");
    String resourceType = GrouperUtil.jsonJacksonGetString(manifestNode, "resourceType");
    String description = GrouperUtil.jsonJacksonGetString(manifestNode, "description");

    // upsert by name
    TrueFoundryGroup existing = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryGroup where name = :theName and groupType = :theType")
        .setString("theName", name)
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_ROLE)
        .uniqueResult(TrueFoundryGroup.class);

    TrueFoundryGroup role;
    if (existing == null) {
      role = new TrueFoundryGroup();
      role.setId(GrouperUuid.getUuid());
      role.setName(name);
      role.setGroupType(TrueFoundryGroup.GROUP_TYPE_ROLE);
      role.setIsDefault(false);
      role.setDisplayName(displayName != null ? displayName : name);
      role.setResourceType(resourceType != null ? resourceType : "account");
      role.setDescription(description != null ? description : "");
      HibernateSession.byObjectStatic().save(role);
    } else {
      role = existing;
      if (displayName != null) {
        role.setDisplayName(displayName);
      }
      if (resourceType != null) {
        role.setResourceType(resourceType);
      }
      if (description != null) {
        role.setDescription(description);
      }
      HibernateSession.byObjectStatic().update(role);
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    resultNode.set("data", buildRoleJson(role));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /role/{id} - delete a role by ID.
   * Returns 200 on success, 404 if not found.
   * Also deletes all memberships associated with the role.
   */
  public void deleteRole(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String roleId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    TrueFoundryGroup role = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryGroup where id = :theId and groupType = :theType")
        .setString("theId", roleId)
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_ROLE)
        .uniqueResult(TrueFoundryGroup.class);

    if (role == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"Role not found\"}");
      return;
    }

    // delete all memberships for this role
    List<TrueFoundryMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryMembership where groupId = :theGroupId")
        .setString("theGroupId", roleId)
        .list(TrueFoundryMembership.class);
    for (TrueFoundryMembership membership : memberships) {
      HibernateSession.byObjectStatic().delete(membership);
    }

    HibernateSession.byObjectStatic().delete(role);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody("{}");
  }

  // ==================== Team operations ====================

  /**
   * GET /teams/user - retrieve all teams with paging.
   * Response: {"data": [{"id": "...", "teamName": "...", "manifest": {"managers": [...]}}]}
   */
  public void getTeams(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryGroup where groupType = :theType")
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_TEAM);
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);
    List<TrueFoundryGroup> teams = query.list(TrueFoundryGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode dataArray = GrouperUtil.jsonJacksonArrayNode();

    for (TrueFoundryGroup team : teams) {
      dataArray.add(buildTeamJson(team));
    }

    resultNode.set("data", dataArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * GET /teams/{id} - get a team by its ID.
   * Response: {"data": {"id": "...", "teamName": "...", "manifest": {"managers": [...]}}}
   * Returns 404 if not found.
   */
  public void getTeamById(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String teamId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    TrueFoundryGroup team = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryGroup where id = :theId and groupType = :theType")
        .setString("theId", teamId)
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_TEAM)
        .uniqueResult(TrueFoundryGroup.class);

    if (team == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"Team not found\"}");
      return;
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    resultNode.set("data", buildTeamJson(team));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * Build a team JSON object in TrueFoundry teams API format.
   * Reads regular members (role="member") and managers (role="manager") from mock_truefoundry_membership.
   * Format: {"id": "...", "teamName": "...", "manifest": {"type": "team", "name": "...", "members": [...], "managers": [...]}}
   */
  private ObjectNode buildTeamJson(TrueFoundryGroup team) {
    ObjectNode teamNode = GrouperUtil.jsonJacksonNode();
    teamNode.put("id", team.getId());
    teamNode.put("teamName", team.getName());

    // read managers from membership table
    List<TrueFoundryMembership> managerMemberships = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryMembership where groupId = :theGroupId and role = :theRole")
        .setString("theGroupId", team.getId())
        .setString("theRole", TrueFoundryMembership.ROLE_MANAGER)
        .list(TrueFoundryMembership.class);

    // read regular members from membership table
    List<TrueFoundryMembership> regularMemberMemberships = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryMembership where groupId = :theGroupId and role = :theRole")
        .setString("theGroupId", team.getId())
        .setString("theRole", TrueFoundryMembership.ROLE_MEMBER)
        .list(TrueFoundryMembership.class);

    ObjectNode manifestNode = GrouperUtil.jsonJacksonNode();
    manifestNode.put("type", "team");
    manifestNode.put("name", team.getName());

    // members = regular members + managers (all team members)
    if (!regularMemberMemberships.isEmpty() || !managerMemberships.isEmpty()) {
      ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
      for (TrueFoundryMembership m : regularMemberMemberships) {
        membersArray.add(m.getUserEmail());
      }
      for (TrueFoundryMembership m : managerMemberships) {
        membersArray.add(m.getUserEmail());
      }
      manifestNode.set("members", membersArray);
    }

    if (!managerMemberships.isEmpty()) {
      ArrayNode managersArray = GrouperUtil.jsonJacksonArrayNode();
      for (TrueFoundryMembership m : managerMemberships) {
        managersArray.add(m.getUserEmail());
      }
      manifestNode.set("managers", managersArray);
    }

    teamNode.set("manifest", manifestNode);
    return teamNode;
  }

  /**
   * PUT /teams - create or update a team (upsert by name), replacing the managers list.
   * Body: {"manifest": {"type": "team", "name": "...", "managers": ["email1", "email2"]}}
   * Response: {"data": {...team...}}
   * Returns 201 for new teams, 200 for updates.
   */
  public void putTeam(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode manifestNode = GrouperUtil.jsonJacksonGetNode(requestNode, "manifest");

    if (manifestNode == null) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"manifest is required\"}");
      return;
    }

    String name = GrouperUtil.jsonJacksonGetString(manifestNode, "name");
    if (StringUtils.isBlank(name)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"manifest.name is required\"}");
      return;
    }

    ArrayNode membersArrayNode = GrouperUtil.jsonJacksonGetArrayNode(manifestNode, "members");
    ArrayNode managersArrayNode = GrouperUtil.jsonJacksonGetArrayNode(manifestNode, "managers");

    // upsert team by name
    TrueFoundryGroup existing = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryGroup where name = :theName and groupType = :theType")
        .setString("theName", name)
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_TEAM)
        .uniqueResult(TrueFoundryGroup.class);

    TrueFoundryGroup team;
    boolean isNew;
    if (existing == null) {
      team = new TrueFoundryGroup();
      team.setId(GrouperUuid.getUuid());
      team.setName(name);
      team.setGroupType(TrueFoundryGroup.GROUP_TYPE_TEAM);
      HibernateSession.byObjectStatic().save(team);
      isNew = true;
    } else {
      team = existing;
      isNew = false;
    }

    // replace all memberships: delete all existing, then insert regular members and managers
    List<TrueFoundryMembership> existingMemberships = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryMembership where groupId = :theGroupId and (role = :roleManager or role = :roleMember)")
        .setString("theGroupId", team.getId())
        .setString("roleManager", TrueFoundryMembership.ROLE_MANAGER)
        .setString("roleMember", TrueFoundryMembership.ROLE_MEMBER)
        .list(TrueFoundryMembership.class);
    for (TrueFoundryMembership m : existingMemberships) {
      HibernateSession.byObjectStatic().delete(m);
    }

    // build set of manager emails for quick lookup
    Set<String> managerEmailSet = new HashSet<String>();
    if (managersArrayNode != null) {
      for (int i = 0; i < managersArrayNode.size(); i++) {
        String email = managersArrayNode.get(i).asText();
        if (StringUtils.isNotBlank(email)) {
          managerEmailSet.add(email);
        }
      }
    }

    // save managers
    for (String managerEmail : managerEmailSet) {
      TrueFoundryMembership membership = new TrueFoundryMembership();
      membership.setId(GrouperUuid.getUuid());
      membership.setGroupId(team.getId());
      membership.setUserEmail(managerEmail);
      membership.setRole(TrueFoundryMembership.ROLE_MANAGER);
      HibernateSession.byObjectStatic().save(membership);
    }

    // save regular members (those in members list but not in managers list)
    if (membersArrayNode != null) {
      for (int i = 0; i < membersArrayNode.size(); i++) {
        String memberEmail = membersArrayNode.get(i).asText();
        if (StringUtils.isNotBlank(memberEmail) && !managerEmailSet.contains(memberEmail)) {
          TrueFoundryMembership membership = new TrueFoundryMembership();
          membership.setId(GrouperUuid.getUuid());
          membership.setGroupId(team.getId());
          membership.setUserEmail(memberEmail);
          membership.setRole(TrueFoundryMembership.ROLE_MEMBER);
          HibernateSession.byObjectStatic().save(membership);
        }
      }
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    resultNode.set("data", buildTeamJson(team));

    mockServiceResponse.setResponseCode(isNew ? 201 : 200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /teams/{id} - delete a team by ID.
   * Returns 200 on success, 404 if not found.
   * Also deletes all memberships associated with the team.
   */
  public void deleteTeam(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String teamId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    TrueFoundryGroup team = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryGroup where id = :theId and groupType = :theType")
        .setString("theId", teamId)
        .setString("theType", TrueFoundryGroup.GROUP_TYPE_TEAM)
        .uniqueResult(TrueFoundryGroup.class);

    if (team == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"Team not found\"}");
      return;
    }

    // delete all memberships for this team
    List<TrueFoundryMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryMembership where groupId = :theGroupId")
        .setString("theGroupId", teamId)
        .list(TrueFoundryMembership.class);
    for (TrueFoundryMembership membership : memberships) {
      HibernateSession.byObjectStatic().delete(membership);
    }

    HibernateSession.byObjectStatic().delete(team);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody("{}");
  }

  /**
   * DELETE /users/{userId} - hard-delete a user by their internal ID.
   * Returns 200 on success, 404 if not found.
   * Also deletes all memberships associated with the user.
   * FOR TESTING/CLEANUP USE ONLY — mirrors the real TrueFoundry DELETE /api/svc/v1/users/{userId}.
   */
  public void deleteUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String userId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    TrueFoundryUser user = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryUser where id = :theId")
        .setString("theId", userId)
        .uniqueResult(TrueFoundryUser.class);

    if (user == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"error\":\"User not found\"}");
      return;
    }

    // delete all memberships for this user
    List<TrueFoundryMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from TrueFoundryMembership where userEmail = :theEmail")
        .setString("theEmail", user.getEmail())
        .list(TrueFoundryMembership.class);
    for (TrueFoundryMembership membership : memberships) {
      HibernateSession.byObjectStatic().delete(membership);
    }

    HibernateSession.byObjectStatic().delete(user);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody("{}");
  }

  // ==================== Request routing ====================

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureTrueFoundryMockTables();
    }
    mockTablesThere = true;

    String configId = GrouperConfig.retrieveConfig().propertyValueString(
        "grouperTest.exampleTrueFoundry.mockExternalSystem.configId");
    if (StringUtils.isBlank(configId)) {
      for (int i = 0; i < 40; i++) {
        configId = GrouperConfig.retrieveConfig().propertyValueString(
            "grouperTest.exampleTrueFoundry.mockExternalSystem.configId");
        if (!StringUtils.isBlank(configId)) {
          break;
        }
        if (i == 39) {
          throw new RuntimeException(
              "grouper.properties grouperTest.exampleTrueFoundry.mockExternalSystem.configId must be set!");
        }
        GrouperUtil.sleep(1000);
      }
    }

    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }

    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());

    // strip "api/svc/v1" prefix
    GrouperUtil.assertion(mockNamePaths.size() >= 4, "Must start with api/svc/v1/...");
    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(0), "api"), "first path must be 'api'");
    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(1), "svc"), "second path must be 'svc'");
    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(2), "v1"), "third path must be 'v1'");

    mockNamePaths = mockNamePaths.subList(3, mockNamePaths.size());

    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);

    mockServiceRequest.setPostMockNamePaths(paths);

    String httpMethod = mockServiceRequest.getHttpServletRequest().getMethod();

    // GET requests
    if (StringUtils.equals("GET", httpMethod)) {
      // GET /subjects
      if ("subjects".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getSubjects(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /role/list
      if ("role".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()
          && "list".equals(mockNamePaths.get(1))) {
        getRoleList(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /teams/user  (list all teams)
      if ("teams".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()
          && "user".equals(mockNamePaths.get(1))) {
        getTeams(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /teams/{id}
      if ("teams".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()
          && !"user".equals(mockNamePaths.get(1))) {
        getTeamById(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
    }

    // POST requests
    if (StringUtils.equals("POST", httpMethod)) {
      // POST /users/register
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()
          && "register".equals(mockNamePaths.get(1))) {
        postRegisterUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    // PATCH requests
    if (StringUtils.equals("PATCH", httpMethod)) {
      // PATCH /users/deactivate
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()
          && "deactivate".equals(mockNamePaths.get(1))) {
        patchDeactivateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      // PATCH /users/activate
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()
          && "activate".equals(mockNamePaths.get(1))) {
        patchActivateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      // PATCH /users/roles
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()
          && "roles".equals(mockNamePaths.get(1))) {
        patchAssignUserRole(mockServiceRequest, mockServiceResponse);
        return;
      }
      // PATCH /scim/v2/{tenantName}/{ssoId}/Users/{userId}
      if ("scim".equals(mockNamePaths.get(0)) && 6 == mockNamePaths.size()
          && "v2".equals(mockNamePaths.get(1)) && "Users".equals(mockNamePaths.get(4))) {
        patchScimUser(mockServiceRequest, mockServiceResponse, mockNamePaths.get(5));
        return;
      }
    }

    // PUT requests
    if (StringUtils.equals("PUT", httpMethod)) {
      // PUT /role
      if ("role".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        putRole(mockServiceRequest, mockServiceResponse);
        return;
      }
      // PUT /teams
      if ("teams".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        putTeam(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    // DELETE requests
    if (StringUtils.equals("DELETE", httpMethod)) {
      // DELETE /role/{id}
      if ("role".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteRole(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // DELETE /teams/{id}
      if ("teams".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteTeam(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // DELETE /users/{userId}
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteUser(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
    }

    throw new RuntimeException("Unhandled TrueFoundry mock request: " + httpMethod + " "
        + StringUtils.join(mockNamePaths, "/"));
  }

}
