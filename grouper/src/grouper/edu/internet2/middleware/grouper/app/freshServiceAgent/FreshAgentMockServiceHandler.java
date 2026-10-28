package edu.internet2.middleware.grouper.app.freshServiceAgent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterMembership;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterMockServiceHandler;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterUser;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.Authentication;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import io.netty.util.internal.ThreadLocalRandom;

public class FreshAgentMockServiceHandler extends MockServiceHandler {

  public FreshAgentMockServiceHandler() {
  }

  /**
   *
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  /**
   *
   */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");

  /**
   * headers to not log all of
   */
  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  /**
   * params to not log all of
   */
  @Override
  public Set<String> doNotLogParameters() {
    return doNotLogParameters;
  }

  private static boolean mockTablesThere = false;

  public static void ensureFreshserviceMockTables() {
    try {
      new GcDbAccess().sql("select count(*) from mock_freshagent_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_freshagent_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_freshagent_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          FreshAgentGroup.createTableFreshGroup(ddlVersionBean, database);
          FreshAgentUser.createTableFreshUser(ddlVersionBean, database);
          FreshAgentMembership.createTableFreshMembership(ddlVersionBean, database);

        }
      });

    }

  }

  /**
   * check authorization for the request
   * @param mockServiceRequest
   */
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String basicAuth = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");

    // These are swapped because Freshservice swaps in the API call.
    String password = Authentication.retrieveUsername(basicAuth);
    String userName = Authentication.retrievePassword(basicAuth);

    String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.exampleFreshAgent.mockExternalSystem.configId");

    String expectedUserName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken."+configId+".basicAuthUser");
    String expectedPassword = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken."+configId+".basicAuthPassword");

    if (!StringUtils.equals(expectedUserName, userName)) {
      throw new RuntimeException("Username does not match with what is in grouper config");
    }
    if (!StringUtils.equals(expectedPassword, password)) {
      throw new RuntimeException("password does not match with what is in grouper config");
    }

  }

  /**
   * group json like Freshservice, includes the id and the "members" array of agent ids
   * @param freshAgentGroup
   * @return the json
   */
  private static ObjectNode groupToJson(FreshAgentGroup freshAgentGroup) {
    ObjectNode objectNode = freshAgentGroup.toJson(null);
    objectNode.put("id", freshAgentGroup.getId());
    List<FreshAgentMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentMembership where groupId = :theGroupId order by userId")
        .setLong("theGroupId", freshAgentGroup.getId()).list(FreshAgentMembership.class);
    ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
    for (FreshAgentMembership membership : memberships) {
      membersArray.add(membership.getUserId());
    }
    objectNode.set("members", membersArray);
    return objectNode;
  }

  /**
   * json fields Freshservice returns on an agent GET but rejects on an agent PUT.  The mock returns
   * them so the provisioner has to strip them before it PUTs a GET back
   */
  private static final Set<String> AGENT_READ_ONLY_JSON_FIELDS = GrouperUtil.toSet("id", "active", "created_at",
      "updated_at", "has_logged_in", "last_login_at", "last_active_at", "work_schedule_id", "department_names",
      "location_name", "member_of_pending_approval", "observer_of_pending_approval", "auto_assign_status_changed_at");

  /**
   * agent json like Freshservice, includes the id, active (which the agent bean does not send),
   * and the read-only fields a real agent GET returns
   * @param freshAgentUser
   * @return the json
   */
  private static ObjectNode userToJson(FreshAgentUser freshAgentUser) {
    ObjectNode objectNode = freshAgentUser.toJson(null);
    objectNode.put("id", freshAgentUser.getId());
    if (freshAgentUser.getActive() != null) {
      objectNode.put("active", freshAgentUser.getActive());
    }
    objectNode.put("created_at", "2026-01-01T00:00:00Z");
    objectNode.put("updated_at", "2026-01-01T00:00:00Z");
    objectNode.put("has_logged_in", false);
    objectNode.putNull("last_login_at");
    objectNode.putNull("last_active_at");
    objectNode.putNull("work_schedule_id");
    objectNode.set("department_names", GrouperUtil.jsonJacksonArrayNode());
    objectNode.putNull("location_name");
    objectNode.set("member_of_pending_approval", GrouperUtil.jsonJacksonArrayNode());
    objectNode.set("observer_of_pending_approval", GrouperUtil.jsonJacksonArrayNode());
    objectNode.putNull("auto_assign_status_changed_at");
    return objectNode;
  }

  /**
   * Freshservice paging: page (starts at 1) and per_page (default 30, max 100) url params
   * @param mockServiceRequest
   * @param nodes all results
   * @return the nodes on the requested page
   */
  private static ArrayNode pageResults(MockServiceRequest mockServiceRequest, List<ObjectNode> nodes) {
    String pageString = mockServiceRequest.getHttpServletRequest().getParameter("page");
    String perPageString = mockServiceRequest.getHttpServletRequest().getParameter("per_page");
    int page = StringUtils.isBlank(pageString) ? 1 : GrouperUtil.intValue(pageString);
    int perPage = StringUtils.isBlank(perPageString) ? 30 : Math.min(100, GrouperUtil.intValue(perPageString));
    GrouperUtil.assertion(page >= 1 && perPage >= 1, "page and per_page must be positive: " + page + ", " + perPage);
    ArrayNode resultArray = GrouperUtil.jsonJacksonArrayNode();
    for (int i = (page - 1) * perPage; i < Math.min(nodes.size(), page * perPage); i++) {
      resultArray.add(nodes.get(i));
    }
    return resultArray;
  }

  // ==================== Group operations ====================

  /**
   * GET /groups - retrieve all groups
   */
  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    List<FreshAgentGroup> freshAgentGroups = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();

    query = HibernateSession.byHqlStatic().createQuery("from FreshAgentGroup");

    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);

    freshAgentGroups = query.list(FreshAgentGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();

    List<ObjectNode> groupNodes = new ArrayList<ObjectNode>();

    for (FreshAgentGroup freshAgentGroup : freshAgentGroups) {
      ObjectNode objectNode = groupToJson(freshAgentGroup);
      groupNodes.add(objectNode);
    }

    resultNode.set("groups", pageResults(mockServiceRequest, groupNodes));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }

  /**
   * GET /groups/{id} - retrieve a single group by id
   */
  public void getGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String groupIdString = mockServiceRequest.getPostMockNamePaths()[1];

    GrouperUtil.assertion(GrouperUtil.length(groupIdString) > 0, "groupId is required");

    long groupId = GrouperUtil.longValue(groupIdString);

    List<FreshAgentGroup> freshAgentGroups = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentGroup where id = :theId")
        .setLong("theId", groupId).list(FreshAgentGroup.class);

    if (GrouperUtil.length(freshAgentGroups) == 1) {
      FreshAgentGroup freshAgentGroup = freshAgentGroups.get(0);

      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = groupToJson(freshAgentGroup);
      resultNode.set("group", objectNode);

      mockServiceResponse.setResponseCode(200);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(freshAgentGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(freshAgentGroups) + ", id: " + groupId);
    }
  }

  /**
   * POST /groups - create a group
   */
  public void postGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);

    FreshAgentGroup freshReqGroup = FreshAgentGroup.fromJson(groupJsonNode);

    // check if group with same name already exists
    List<FreshAgentGroup> existingGroups = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentGroup where name = :theName")
        .setString("theName", freshReqGroup.getName()).list(FreshAgentGroup.class);

    if (GrouperUtil.length(existingGroups) > 0) {
      mockServiceResponse.setResponseCode(409);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"description\":\"Validation failed\",\"errors\":[{\"field\":\"name\",\"message\":\"already exists\"}]}");
      return;
    }

    boolean idSaved = false;

    while(!idSaved) {
      try {
        freshReqGroup.setId(ThreadLocalRandom.current().nextLong(1, 99999999));
        HibernateSession.byObjectStatic().save(freshReqGroup);
        idSaved = true;
      } catch (GrouperDAOException e) {

      }
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode objectNode = groupToJson(freshReqGroup);
    resultNode.set("group", objectNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }

  /**
   * PUT /groups/{id} - update a group
   */
  public void updateGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String groupIdString = mockServiceRequest.getPostMockNamePaths()[1];

    mockServiceRequest.getDebugMap().put("groupId", groupIdString);

    long groupId = GrouperUtil.longValue(groupIdString);

    List<FreshAgentGroup> freshAgentGroups = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentGroup where id = :theId")
        .setLong("theId", groupId).list(FreshAgentGroup.class);

    if (GrouperUtil.length(freshAgentGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(freshAgentGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(freshAgentGroups));
    }

    FreshAgentGroup freshAgentGroup = freshAgentGroups.get(0);

    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);

    String name = GrouperUtil.jsonJacksonGetString(groupJsonNode, "name");
    if (StringUtils.isNotBlank(name)) {
      freshAgentGroup.setName(name);
    }

    if (groupJsonNode.has("description")) {
      String description = GrouperUtil.jsonJacksonGetString(groupJsonNode, "description");
      freshAgentGroup.setDescription(description);
    }

    HibernateSession.byObjectStatic().saveOrUpdate(freshAgentGroup);

    // agent groups do not have member endpoints, a PUT of the members array replaces the members
    JsonNode membersNode = GrouperUtil.jsonJacksonGetNode(groupJsonNode, "members");
    if (membersNode != null && membersNode.isArray()) {
      HibernateSession.byHqlStatic()
          .createQuery("delete from FreshAgentMembership where groupId = :groupId")
          .setLong("groupId", groupId).executeUpdateInt();
      for (int i = 0; i < membersNode.size(); i++) {
        long userId = membersNode.get(i).asLong();
        List<FreshAgentUser> users = HibernateSession.byHqlStatic()
            .createQuery("from FreshAgentUser where id = :theId")
            .setLong("theId", userId).list(FreshAgentUser.class);
        if (GrouperUtil.length(users) == 0) {
          mockServiceResponse.setResponseCode(400);
          mockServiceResponse.setContentType("application/json");
          mockServiceResponse.setResponseBody("{\"description\":\"Validation failed\",\"errors\":[{\"field\":\"members\",\"message\":\"invalid agent id " + userId + "\"}]}");
          return;
        }
        FreshAgentMembership membership = new FreshAgentMembership();
        membership.setGroupId(groupId);
        membership.setUserId(userId);
        membership.setId(ThreadLocalRandom.current().nextLong(1, 99999999));
        HibernateSession.byObjectStatic().save(membership);
      }
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode objectNode = groupToJson(freshAgentGroup);
    resultNode.set("group", objectNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /groups/{id} - delete a group
   */
  public void deleteGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String groupIdString = mockServiceRequest.getPostMockNamePaths()[1];

    GrouperUtil.assertion(GrouperUtil.length(groupIdString) > 0, "groupId is required");

    long groupId = GrouperUtil.longValue(groupIdString);

    // check if group exists
    List<FreshAgentGroup> existingGroups = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentGroup where id = :theId")
        .setLong("theId", groupId).list(FreshAgentGroup.class);

    if (GrouperUtil.length(existingGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }

    // delete memberships first
    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshAgentMembership where groupId = :groupId")
        .setLong("groupId", groupId).executeUpdateInt();

    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshAgentGroup where id = :theId")
        .setLong("theId", groupId).executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
  }

  // ==================== User/Agent operations ====================

  /**
   * GET /agents - retrieve all users
   */
  public void getUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String emailParam = mockServiceRequest.getHttpServletRequest().getParameter("email");
    String queryParam = mockServiceRequest.getHttpServletRequest().getParameter("query");

    List<FreshAgentUser> freshAgentUsers = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();

    // parse the query parameter format: "attributeName:'value'" or "attributeName:value"
    String queryAttributeName = null;
    String queryAttributeValue = null;

    // email= parameter takes priority (e.g. /api/v2/agents?email=jsmith@upenn.edu)
    if (StringUtils.isNotBlank(emailParam)) {
      queryAttributeName = "email";
      queryAttributeValue = emailParam;
    } else if (StringUtils.isNotBlank(queryParam)) {
      int colonIndex = queryParam.indexOf(':');
      if (colonIndex > 0) {
        queryAttributeName = queryParam.substring(0, colonIndex);
        queryAttributeValue = queryParam.substring(colonIndex + 1);
        // strip surrounding quotes
        if (queryAttributeValue.startsWith("'") && queryAttributeValue.endsWith("'")) {
          queryAttributeValue = queryAttributeValue.substring(1, queryAttributeValue.length() - 1);
        }
      }
    }

    if ("email".equals(queryAttributeName)) {
      query = HibernateSession.byHqlStatic()
          .createQuery("from FreshAgentUser where email = :theEmail")
          .setString("theEmail", queryAttributeValue);
    } else if ("external_id".equals(queryAttributeName)) {
      query = HibernateSession.byHqlStatic()
          .createQuery("from FreshAgentUser where externalId = :theExternalId")
          .setString("theExternalId", queryAttributeValue);
    } else if (queryAttributeName != null) {
      // for custom fields, load all users and filter in Java
      // since custom fields are stored as JSON
      query = HibernateSession.byHqlStatic().createQuery("from FreshAgentUser");
    } else {
      query = HibernateSession.byHqlStatic().createQuery("from FreshAgentUser");
    }

    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);

    freshAgentUsers = query.list(FreshAgentUser.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();

    List<ObjectNode> userNodes = new ArrayList<ObjectNode>();

    for (FreshAgentUser freshAgentUser : freshAgentUsers) {

      // for custom field queries, filter in Java since custom fields are stored as JSON
      if (queryAttributeName != null && !"email".equals(queryAttributeName) && !"external_id".equals(queryAttributeName)) {
        boolean matches = false;
        java.util.Map<String, Object> customFields = freshAgentUser.getCustomFields();
        if (customFields != null) {
          Object fieldValue = customFields.get(queryAttributeName);
          if (fieldValue != null && String.valueOf(fieldValue).equals(queryAttributeValue)) {
            matches = true;
          }
        }
        if (!matches) {
          continue;
        }
      }

      ObjectNode objectNode = userToJson(freshAgentUser);
      userNodes.add(objectNode);
    }

    // page after the custom field filter, like Freshservice pages the query results
    resultNode.set("agents", pageResults(mockServiceRequest, userNodes));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * GET /agents/{id} - retrieve a single user by id
   */
  public void getUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String userIdString = mockServiceRequest.getPostMockNamePaths()[1];

    GrouperUtil.assertion(GrouperUtil.length(userIdString) > 0, "userId is required");

    long userId = GrouperUtil.longValue(userIdString);

    List<FreshAgentUser> freshAgentUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentUser where id = :theId")
        .setLong("theId", userId).list(FreshAgentUser.class);

    if (GrouperUtil.length(freshAgentUsers) == 1) {
      FreshAgentUser freshAgentUser = freshAgentUsers.get(0);

      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = userToJson(freshAgentUser);
      resultNode.set("agent", objectNode);

      mockServiceResponse.setResponseCode(200);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(freshAgentUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersById: " + GrouperUtil.length(freshAgentUsers) + ", id: " + userId);
    }
  }

  /**
   * POST /agents - create a user
   */
  public void postUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);

    FreshAgentUser freshReqUser = FreshAgentUser.fromJson(userJsonNode);

    // Default active=true and occasional=false to match real Freshservice behavior
    freshReqUser.setActive(true);
    if (freshReqUser.getOccasional() == null) {
      freshReqUser.setOccasional(false);
    }

    // Freshservice requires at least one role to create an agent
    if (!freshReqUser.hasRoles()) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"description\":\"Validation failed\",\"errors\":[{\"field\":\"roles\",\"message\":\"It should not be blank\"}]}");
      return;
    }

    // Freshservice has one unique email across agents and requesters
    if (StringUtils.isNotBlank(freshReqUser.getEmail())) {
      List<FreshRequesterUser> existingRequesters = HibernateSession.byHqlStatic()
          .createQuery("from FreshRequesterUser where email = :theEmail")
          .setString("theEmail", freshReqUser.getEmail()).list(FreshRequesterUser.class);
      if (GrouperUtil.length(existingRequesters) > 0) {
        mockServiceResponse.setResponseCode(409);
        mockServiceResponse.setContentType("application/json");
        mockServiceResponse.setResponseBody("{\"description\":\"Validation failed\",\"errors\":[{\"field\":\"email\",\"message\":\"already exists\"}]}");
        return;
      }
    }

    // check if email already exists
    if (StringUtils.isNotBlank(freshReqUser.getEmail())) {
      List<FreshAgentUser> existingUsers = HibernateSession.byHqlStatic()
          .createQuery("from FreshAgentUser where email = :theEmail")
          .setString("theEmail", freshReqUser.getEmail()).list(FreshAgentUser.class);
      if (existingUsers != null && existingUsers.size() > 0) {
        mockServiceResponse.setResponseCode(409);
        mockServiceResponse.setContentType("application/json");
        mockServiceResponse.setResponseBody("{\"description\":\"Validation failed\",\"errors\":[{\"field\":\"email\",\"message\":\"already exists\"}]}");
        return;
      }
    }

    boolean idSaved = false;

    while(!idSaved) {
      try {
        freshReqUser.setId(ThreadLocalRandom.current().nextLong(1, 99999999));
        HibernateSession.byObjectStatic().save(freshReqUser);
        idSaved = true;
      } catch (GrouperDAOException e) {

      }
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode objectNode = userToJson(freshReqUser);
    resultNode.set("agent", objectNode);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * PUT /agents/{id} - update a user
   */
  public void updateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String userIdString = mockServiceRequest.getPostMockNamePaths()[1];

    mockServiceRequest.getDebugMap().put("userId", userIdString);

    long userId = GrouperUtil.longValue(userIdString);

    List<FreshAgentUser> freshAgentUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentUser where id = :theId")
        .setLong("theId", userId).list(FreshAgentUser.class);

    if (GrouperUtil.length(freshAgentUsers) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindUser", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(freshAgentUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(freshAgentUsers));
    }

    FreshAgentUser freshAgentUser = freshAgentUsers.get(0);

    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);

    String firstName = GrouperUtil.jsonJacksonGetString(userJsonNode, "first_name");
    if (firstName != null) {
      freshAgentUser.setFirstName(firstName);
    }

    String lastName = GrouperUtil.jsonJacksonGetString(userJsonNode, "last_name");
    if (lastName != null) {
      freshAgentUser.setLastName(lastName);
    }

    String primaryEmail = GrouperUtil.jsonJacksonGetString(userJsonNode, "email");
    if (primaryEmail != null) {
      freshAgentUser.setEmail(primaryEmail);
    }

    Boolean occasional = GrouperUtil.jsonJacksonGetBoolean(userJsonNode, "occasional");
    if (occasional != null) {
      freshAgentUser.setOccasional(occasional);
    }

    JsonNode rolesNode = GrouperUtil.jsonJacksonGetNode(userJsonNode, "roles");
    if (rolesNode != null && rolesNode.isArray()) {
      freshAgentUser.setRolesJson(GrouperUtil.jsonJacksonToString(rolesNode));
    }

    String jobTitle = GrouperUtil.jsonJacksonGetString(userJsonNode, "job_title");
    if (jobTitle != null) {
      freshAgentUser.setJobTitle(jobTitle);
    }

    String workPhoneNumber = GrouperUtil.jsonJacksonGetString(userJsonNode, "work_phone_number");
    if (workPhoneNumber != null) {
      freshAgentUser.setWorkPhoneNumber(workPhoneNumber);
    }

    // department_ids is an array, take first value
    JsonNode departmentIdsNode = GrouperUtil.jsonJacksonGetNode(userJsonNode, "department_ids");
    if (departmentIdsNode != null && departmentIdsNode.isArray() && departmentIdsNode.size() > 0) {
      JsonNode firstDeptId = departmentIdsNode.get(0);
      if (firstDeptId != null && firstDeptId.isNumber()) {
        freshAgentUser.setDepartmentId(firstDeptId.longValue());
      }
    }

    Long reportingManagerId = GrouperUtil.jsonJacksonGetLong(userJsonNode, "reporting_manager_id");
    if (reportingManagerId != null) {
      freshAgentUser.setReportingManagerId(reportingManagerId);
    }

    String address = GrouperUtil.jsonJacksonGetString(userJsonNode, "address");
    if (address != null) {
      freshAgentUser.setAddress(address);
    }

    String externalId = GrouperUtil.jsonJacksonGetString(userJsonNode, "external_id");
    if (externalId != null) {
      freshAgentUser.setExternalId(externalId);
    }

    // read-only fields cannot be sent on an agent PUT (e.g. active, agents are deactivated with DELETE
    // and reactivated with /reactivate)
    for (String readOnlyJsonField : AGENT_READ_ONLY_JSON_FIELDS) {
      if (userJsonNode.has(readOnlyJsonField)) {
        mockServiceResponse.setResponseCode(400);
        mockServiceResponse.setContentType("application/json");
        mockServiceResponse.setResponseBody("{\"description\":\"Validation failed\",\"errors\":[{\"field\":\"" + readOnlyJsonField + "\",\"message\":\"Unexpected/invalid field in request\"}]}");
        return;
      }
    }

    // custom_fields
    JsonNode customFieldsNode = GrouperUtil.jsonJacksonGetNode(userJsonNode, "custom_fields");
    if (customFieldsNode != null && customFieldsNode.isObject()) {
      java.util.Map<String, Object> existingCustomFields = freshAgentUser.getCustomFields();
      if (existingCustomFields == null) {
        existingCustomFields = new java.util.HashMap<>();
      }
      java.util.Iterator<String> fieldNames = customFieldsNode.fieldNames();
      while (fieldNames.hasNext()) {
        String fieldName = fieldNames.next();
        JsonNode fieldValue = customFieldsNode.get(fieldName);
        if (fieldValue == null || fieldValue.isNull()) {
          existingCustomFields.put(fieldName, null);
        } else if (fieldValue.isTextual()) {
          existingCustomFields.put(fieldName, fieldValue.asText());
        } else if (fieldValue.isBoolean()) {
          existingCustomFields.put(fieldName, fieldValue.booleanValue());
        } else if (fieldValue.isNumber()) {
          existingCustomFields.put(fieldName, fieldValue.longValue());
        }
      }
      freshAgentUser.setCustomFields(existingCustomFields);
    }

    HibernateSession.byObjectStatic().saveOrUpdate(freshAgentUser);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode objectNode = userToJson(freshAgentUser);
    resultNode.set("agent", objectNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /agents/{id} - deactivate/delete a user
   */
  public void deactivateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String userIdString = mockServiceRequest.getPostMockNamePaths()[1];

    GrouperUtil.assertion(GrouperUtil.length(userIdString) > 0, "userId is required");

    long userId = GrouperUtil.longValue(userIdString);

    // check if user exists
    List<FreshAgentUser> existingUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentUser where id = :theId")
        .setLong("theId", userId).list(FreshAgentUser.class);

    if (GrouperUtil.length(existingUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }

    // Freshservice DELETE agent deactivates instead of removing
    FreshAgentUser freshAgentUser = existingUsers.get(0);
    freshAgentUser.setActive(false);
    HibernateSession.byObjectStatic().saveOrUpdate(freshAgentUser);

    // delete all memberships for the deactivated user
    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshAgentMembership where userId = :userId")
        .setLong("userId", userId).executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
  }

  /**
   * PUT /agents/{id}/reactivate - reactivate a deactivated user
   * Returns 200 if successful.  400 with body if already active.
   */
  public void reactivateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String userIdString = mockServiceRequest.getPostMockNamePaths()[1];

    GrouperUtil.assertion(GrouperUtil.length(userIdString) > 0, "userId is required");

    long userId = GrouperUtil.longValue(userIdString);

    // check if user exists
    List<FreshAgentUser> existingUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentUser where id = :theId")
        .setLong("theId", userId).list(FreshAgentUser.class);

    if (GrouperUtil.length(existingUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }

    FreshAgentUser freshAgentUser = existingUsers.get(0);

    // if already active, return 400
    if (freshAgentUser.getActive() != null && freshAgentUser.getActive()) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"code\":\"contact_already_active\",\"message\":\"Contact is already active and cannot be restored.\"}");
      return;
    }

    // reactivate, Freshservice reactivates agents as occasional
    freshAgentUser.setActive(true);
    freshAgentUser.setOccasional(true);
    HibernateSession.byObjectStatic().saveOrUpdate(freshAgentUser);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
  }

  /**
   * DELETE /agents/{id}/forget - permanently delete (forget) a user
   */
  public void forgetUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String userIdString = mockServiceRequest.getPostMockNamePaths()[1];

    GrouperUtil.assertion(GrouperUtil.length(userIdString) > 0, "userId is required");

    long userId = GrouperUtil.longValue(userIdString);

    // check if user exists
    List<FreshAgentUser> existingUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshAgentUser where id = :theId")
        .setLong("theId", userId).list(FreshAgentUser.class);

    if (GrouperUtil.length(existingUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }

    // permanently delete: remove memberships first, then remove the user
    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshAgentMembership where userId = :userId")
        .setLong("userId", userId).executeUpdateInt();

    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshAgentUser where id = :theId")
        .setLong("theId", userId).executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
  }

  // ==================== Requester operations ====================
  // Freshservice has one unique email across agents and requesters, so a requester with the email
  // of a new agent is converted to an agent.  These use the requester mock tables

  /**
   * GET /requesters?email= - find a requester by email
   */
  public void getRequesters(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String emailParam = mockServiceRequest.getHttpServletRequest().getParameter("email");
    GrouperUtil.assertion(StringUtils.isNotBlank(emailParam), "email param is required");

    List<FreshRequesterUser> freshRequesterUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterUser where email = :theEmail")
        .setString("theEmail", emailParam).list(FreshRequesterUser.class);

    ArrayNode usersArray = GrouperUtil.jsonJacksonArrayNode();
    for (FreshRequesterUser freshRequesterUser : freshRequesterUsers) {
      ObjectNode objectNode = freshRequesterUser.toJson(null);
      objectNode.put("id", freshRequesterUser.getId());
      usersArray.add(objectNode);
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    resultNode.set("requesters", usersArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * PUT /requesters/{id}/convert_to_agent - convert a requester to an occasional agent with the same id
   */
  public void convertRequesterToAgent(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    long requesterId = GrouperUtil.longValue(mockServiceRequest.getPostMockNamePaths()[1]);

    List<FreshRequesterUser> freshRequesterUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterUser where id = :theId")
        .setLong("theId", requesterId).list(FreshRequesterUser.class);

    if (GrouperUtil.length(freshRequesterUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }
    FreshRequesterUser freshRequesterUser = freshRequesterUsers.get(0);

    FreshAgentUser freshAgentUser = new FreshAgentUser();
    freshAgentUser.setId(freshRequesterUser.getId());
    freshAgentUser.setFirstName(freshRequesterUser.getFirstName());
    freshAgentUser.setLastName(freshRequesterUser.getLastName());
    freshAgentUser.setEmail(freshRequesterUser.getEmail());
    freshAgentUser.setExternalId(freshRequesterUser.getExternalId());
    freshAgentUser.setActive(true);
    freshAgentUser.setOccasional(true);
    // Freshservice assigns the SD Agent role, any role id is fine for the mock
    freshAgentUser.assignRole(1L, "entire_helpdesk");

    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshRequesterMembership where userId = :userId")
        .setLong("userId", requesterId).executeUpdateInt();
    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshRequesterUser where id = :theId")
        .setLong("theId", requesterId).executeUpdateInt();
    HibernateSession.byObjectStatic().save(freshAgentUser);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    resultNode.set("agent", userToJson(freshAgentUser));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  // ==================== Request routing ====================

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureFreshserviceMockTables();
      // requesters can be converted to agents
      FreshRequesterMockServiceHandler.ensureFreshserviceMockTables();
    }
    mockTablesThere = true;

    // this must be there and it might be a caching issue:
    //     String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.exampleFreshAgent.mockExternalSystem.configId");
    // loop 10 times and wait a second each time if not there, to avoid issues with tables not being found
    // if no there at end give a good exception
    // after 10 seconds the caches should have cleared if everything is setup correctly.
    for (int i=0; i<10; i++) {
      // clear cache
      ConfigPropertiesCascadeBase.clearCache();
      String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.exampleFreshAgent.mockExternalSystem.configId");
      if (!StringUtils.isBlank(configId)) {
        break;
      }
      if (i >= 9) {
        throw new RuntimeException("grouper.properties grouperTest.exampleFreshAgent.mockExternalSystem.configId must be set to the configId of the external system used by mock!");
      }
      GrouperUtil.sleep(1000);
    }

    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }

    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());

    // strip "api/v2" prefix
    GrouperUtil.assertion(mockNamePaths.size() >= 3, "Must start with api/v2/");
    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(0), "api"), "first path must be 'api'");
    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(1), "v2"), "second path must be 'v2'");

    mockNamePaths = mockNamePaths.subList(2, mockNamePaths.size());

    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);

    mockServiceRequest.setPostMockNamePaths(paths);

    String httpMethod = mockServiceRequest.getHttpServletRequest().getMethod();

    // GET requests
    if (StringUtils.equals("GET", httpMethod)) {
      // GET /groups
      if ("groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /groups/{id}
      if ("groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /requesters?email=
      if ("requesters".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getRequesters(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /agents
      if ("agents".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /agents/{id}
      if ("agents".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    // POST requests
    if (StringUtils.equals("POST", httpMethod)) {
      // POST /groups
      if ("groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      // POST /agents
      if ("agents".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    // PUT requests
    if (StringUtils.equals("PUT", httpMethod)) {
      // PUT /groups/{id}
      if ("groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      // PUT /requesters/{id}/convert_to_agent
      if ("requesters".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size()
          && "convert_to_agent".equals(mockNamePaths.get(2))) {
        convertRequesterToAgent(mockServiceRequest, mockServiceResponse);
        return;
      }
      // PUT /agents/{id}/reactivate
      if ("agents".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size()
          && "reactivate".equals(mockNamePaths.get(2))) {
        reactivateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      // PUT /agents/{id}
      if ("agents".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    // DELETE requests
    if (StringUtils.equals("DELETE", httpMethod)) {
      // DELETE /groups/{id}
      if ("groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      // DELETE /agents/{id}/forget
      if ("agents".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size()
          && "forget".equals(mockNamePaths.get(2))) {
        forgetUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      // DELETE /agents/{id}
      if ("agents".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deactivateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    throw new RuntimeException("Not expecting request: '" + httpMethod
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");

  }

  private void checkRequestContentType(MockServiceRequest mockServiceRequest) {
    if (!StringUtils.equals(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json")
            && !StringUtils.startsWith(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json;")) {
      throw new RuntimeException("Content type must be application/json");
    }
  }

}
