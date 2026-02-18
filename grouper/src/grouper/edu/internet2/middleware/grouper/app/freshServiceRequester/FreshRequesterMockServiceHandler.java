package edu.internet2.middleware.grouper.app.freshServiceRequester;

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

public class FreshRequesterMockServiceHandler extends MockServiceHandler {

  public FreshRequesterMockServiceHandler() {
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
      new GcDbAccess().sql("select count(*) from mock_freshreq_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_freshreq_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_freshreq_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          FreshRequesterGroup.createTableFreshGroup(ddlVersionBean, database);
          FreshRequesterUser.createTableFreshUser(ddlVersionBean, database);
          FreshRequesterMembership.createTableFreshMembership(ddlVersionBean, database);

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

    String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.exampleFreshRequester.mockExternalSystem.configId");

    String expectedUserName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken."+configId+".basicAuthUser");
    String expectedPassword = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken."+configId+".basicAuthPassword");

    if (!StringUtils.equals(expectedUserName, userName)) {
      throw new RuntimeException("Username does not match with what is in grouper config");
    }
    if (!StringUtils.equals(expectedPassword, password)) {
      throw new RuntimeException("password does not match with what is in grouper config");
    }

  }

  // ==================== Group operations ====================

  /**
   * GET /requester_groups - retrieve all groups
   */
  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    List<FreshRequesterGroup> freshRequesterGroups = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();

    query = HibernateSession.byHqlStatic().createQuery("from FreshRequesterGroup");

    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);

    freshRequesterGroups = query.list(FreshRequesterGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();

    ArrayNode groupsArray = GrouperUtil.jsonJacksonArrayNode();

    for (FreshRequesterGroup freshRequesterGroup : freshRequesterGroups) {
      ObjectNode objectNode = freshRequesterGroup.toJson(null);
      objectNode.put("id", freshRequesterGroup.getId());
      groupsArray.add(objectNode);
    }

    resultNode.set("requester_groups", groupsArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }

  /**
   * GET /requester_groups/{id} - retrieve a single group by id
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

    List<FreshRequesterGroup> freshRequesterGroups = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterGroup where id = :theId")
        .setLong("theId", groupId).list(FreshRequesterGroup.class);

    if (GrouperUtil.length(freshRequesterGroups) == 1) {
      FreshRequesterGroup freshRequesterGroup = freshRequesterGroups.get(0);

      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = freshRequesterGroup.toJson(null);
      objectNode.put("id", freshRequesterGroup.getId());
      resultNode.set("requester_group", objectNode);

      mockServiceResponse.setResponseCode(200);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(freshRequesterGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(freshRequesterGroups) + ", id: " + groupId);
    }
  }

  /**
   * POST /requester_groups - create a group
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

    FreshRequesterGroup freshReqGroup = FreshRequesterGroup.fromJson(groupJsonNode);

    // check if group with same name already exists
    List<FreshRequesterGroup> existingGroups = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterGroup where name = :theName")
        .setString("theName", freshReqGroup.getName()).list(FreshRequesterGroup.class);

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
    ObjectNode objectNode = freshReqGroup.toJson(null);
    objectNode.put("id", freshReqGroup.getId());
    resultNode.set("requester_group", objectNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }

  /**
   * PUT /requester_groups/{id} - update a group
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

    List<FreshRequesterGroup> freshRequesterGroups = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterGroup where id = :theId")
        .setLong("theId", groupId).list(FreshRequesterGroup.class);

    if (GrouperUtil.length(freshRequesterGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(freshRequesterGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(freshRequesterGroups));
    }

    FreshRequesterGroup freshRequesterGroup = freshRequesterGroups.get(0);

    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);

    String name = GrouperUtil.jsonJacksonGetString(groupJsonNode, "name");
    if (StringUtils.isNotBlank(name)) {
      freshRequesterGroup.setName(name);
    }

    if (groupJsonNode.has("description")) {
      String description = GrouperUtil.jsonJacksonGetString(groupJsonNode, "description");
      freshRequesterGroup.setDescription(description);
    }

    HibernateSession.byObjectStatic().saveOrUpdate(freshRequesterGroup);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode objectNode = freshRequesterGroup.toJson(null);
    objectNode.put("id", freshRequesterGroup.getId());
    resultNode.set("requester_group", objectNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /requester_groups/{id} - delete a group
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
    List<FreshRequesterGroup> existingGroups = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterGroup where id = :theId")
        .setLong("theId", groupId).list(FreshRequesterGroup.class);

    if (GrouperUtil.length(existingGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }

    // delete memberships first
    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshRequesterMembership where groupId = :groupId")
        .setLong("groupId", groupId).executeUpdateInt();

    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshRequesterGroup where id = :theId")
        .setLong("theId", groupId).executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
  }

  // ==================== User/Requester operations ====================

  /**
   * GET /requesters - retrieve all users
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

    List<FreshRequesterUser> freshRequesterUsers = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();

    // parse the query parameter format: "attributeName:'value'" or "attributeName:value"
    String queryAttributeName = null;
    String queryAttributeValue = null;

    // email= parameter takes priority (e.g. /api/v2/requesters?email=jsmith@upenn.edu)
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
          .createQuery("from FreshRequesterUser where email = :theEmail")
          .setString("theEmail", queryAttributeValue);
    } else if ("external_id".equals(queryAttributeName)) {
      query = HibernateSession.byHqlStatic()
          .createQuery("from FreshRequesterUser where externalId = :theExternalId")
          .setString("theExternalId", queryAttributeValue);
    } else if (queryAttributeName != null) {
      // for custom fields, load all users and filter in Java
      // since custom fields are stored as JSON
      query = HibernateSession.byHqlStatic().createQuery("from FreshRequesterUser");
    } else {
      query = HibernateSession.byHqlStatic().createQuery("from FreshRequesterUser");
    }

    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);

    freshRequesterUsers = query.list(FreshRequesterUser.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();

    ArrayNode usersArray = GrouperUtil.jsonJacksonArrayNode();

    for (FreshRequesterUser freshRequesterUser : freshRequesterUsers) {

      // for custom field queries, filter in Java since custom fields are stored as JSON
      if (queryAttributeName != null && !"email".equals(queryAttributeName) && !"external_id".equals(queryAttributeName)) {
        boolean matches = false;
        java.util.Map<String, Object> customFields = freshRequesterUser.getCustomFields();
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

      ObjectNode objectNode = freshRequesterUser.toJson(null);
      objectNode.put("id", freshRequesterUser.getId());
      usersArray.add(objectNode);
    }

    resultNode.set("requesters", usersArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * GET /requesters/{id} - retrieve a single user by id
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

    List<FreshRequesterUser> freshRequesterUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterUser where id = :theId")
        .setLong("theId", userId).list(FreshRequesterUser.class);

    if (GrouperUtil.length(freshRequesterUsers) == 1) {
      FreshRequesterUser freshRequesterUser = freshRequesterUsers.get(0);

      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = freshRequesterUser.toJson(null);
      objectNode.put("id", freshRequesterUser.getId());
      resultNode.set("requester", objectNode);

      mockServiceResponse.setResponseCode(200);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(freshRequesterUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersById: " + GrouperUtil.length(freshRequesterUsers) + ", id: " + userId);
    }
  }

  /**
   * POST /requesters - create a user
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

    FreshRequesterUser freshReqUser = FreshRequesterUser.fromJson(userJsonNode);

    // Default active=true and isAgent=false to match real Freshservice behavior
    if (freshReqUser.getActive() == null) {
      freshReqUser.setActive(true);
    }
    if (freshReqUser.getIsAgent() == null) {
      freshReqUser.setIsAgent(false);
    }

    // check if email already exists
    if (StringUtils.isNotBlank(freshReqUser.getEmail())) {
      List<FreshRequesterUser> existingUsers = HibernateSession.byHqlStatic()
          .createQuery("from FreshRequesterUser where email = :theEmail")
          .setString("theEmail", freshReqUser.getEmail()).list(FreshRequesterUser.class);
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
    ObjectNode objectNode = freshReqUser.toJson(null);
    objectNode.put("id", freshReqUser.getId());
    resultNode.set("requester", objectNode);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * PUT /requesters/{id} - update a user
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

    List<FreshRequesterUser> freshRequesterUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterUser where id = :theId")
        .setLong("theId", userId).list(FreshRequesterUser.class);

    if (GrouperUtil.length(freshRequesterUsers) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindUser", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(freshRequesterUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(freshRequesterUsers));
    }

    FreshRequesterUser freshRequesterUser = freshRequesterUsers.get(0);

    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);

    String firstName = GrouperUtil.jsonJacksonGetString(userJsonNode, "first_name");
    if (firstName != null) {
      freshRequesterUser.setFirstName(firstName);
    }

    String lastName = GrouperUtil.jsonJacksonGetString(userJsonNode, "last_name");
    if (lastName != null) {
      freshRequesterUser.setLastName(lastName);
    }

    String primaryEmail = GrouperUtil.jsonJacksonGetString(userJsonNode, "primary_email");
    if (primaryEmail != null) {
      freshRequesterUser.setEmail(primaryEmail);
    }

    Boolean isAgent = GrouperUtil.jsonJacksonGetBoolean(userJsonNode, "is_agent");
    if (isAgent != null) {
      freshRequesterUser.setIsAgent(isAgent);
    }

    String jobTitle = GrouperUtil.jsonJacksonGetString(userJsonNode, "job_title");
    if (jobTitle != null) {
      freshRequesterUser.setJobTitle(jobTitle);
    }

    String workPhoneNumber = GrouperUtil.jsonJacksonGetString(userJsonNode, "work_phone_number");
    if (workPhoneNumber != null) {
      freshRequesterUser.setWorkPhoneNumber(workPhoneNumber);
    }

    // department_ids is an array, take first value
    JsonNode departmentIdsNode = GrouperUtil.jsonJacksonGetNode(userJsonNode, "department_ids");
    if (departmentIdsNode != null && departmentIdsNode.isArray() && departmentIdsNode.size() > 0) {
      JsonNode firstDeptId = departmentIdsNode.get(0);
      if (firstDeptId != null && firstDeptId.isNumber()) {
        freshRequesterUser.setDepartmentId(firstDeptId.longValue());
      }
    }

    Long reportingManagerId = GrouperUtil.jsonJacksonGetLong(userJsonNode, "reporting_manager_id");
    if (reportingManagerId != null) {
      freshRequesterUser.setReportingManagerId(reportingManagerId);
    }

    String address = GrouperUtil.jsonJacksonGetString(userJsonNode, "address");
    if (address != null) {
      freshRequesterUser.setAddress(address);
    }

    String externalId = GrouperUtil.jsonJacksonGetString(userJsonNode, "external_id");
    if (externalId != null) {
      freshRequesterUser.setExternalId(externalId);
    }

    Boolean active = GrouperUtil.jsonJacksonGetBoolean(userJsonNode, "active");
    if (active != null) {
      freshRequesterUser.setActive(active);
    }

    // custom_fields
    JsonNode customFieldsNode = GrouperUtil.jsonJacksonGetNode(userJsonNode, "custom_fields");
    if (customFieldsNode != null && customFieldsNode.isObject()) {
      java.util.Map<String, Object> existingCustomFields = freshRequesterUser.getCustomFields();
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
      freshRequesterUser.setCustomFields(existingCustomFields);
    }

    HibernateSession.byObjectStatic().saveOrUpdate(freshRequesterUser);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode objectNode = freshRequesterUser.toJson(null);
    objectNode.put("id", freshRequesterUser.getId());
    resultNode.set("requester", objectNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /requesters/{id} - deactivate/delete a user
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
    List<FreshRequesterUser> existingUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterUser where id = :theId")
        .setLong("theId", userId).list(FreshRequesterUser.class);

    if (GrouperUtil.length(existingUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }

    // Freshservice DELETE requester deactivates instead of removing
    FreshRequesterUser freshRequesterUser = existingUsers.get(0);
    freshRequesterUser.setActive(false);
    HibernateSession.byObjectStatic().saveOrUpdate(freshRequesterUser);

    // delete all memberships for the deactivated user
    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshRequesterMembership where userId = :userId")
        .setLong("userId", userId).executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
  }

  /**
   * PUT /requesters/{id}/reactivate - reactivate a deactivated user
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
    List<FreshRequesterUser> existingUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterUser where id = :theId")
        .setLong("theId", userId).list(FreshRequesterUser.class);

    if (GrouperUtil.length(existingUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }

    FreshRequesterUser freshRequesterUser = existingUsers.get(0);

    // if already active, return 400
    if (freshRequesterUser.getActive() != null && freshRequesterUser.getActive()) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"code\":\"contact_already_active\",\"message\":\"Contact is already active and cannot be restored.\"}");
      return;
    }

    // reactivate
    freshRequesterUser.setActive(true);
    HibernateSession.byObjectStatic().saveOrUpdate(freshRequesterUser);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
  }

  /**
   * DELETE /requesters/{id}/forget - permanently delete (forget) a user
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
    List<FreshRequesterUser> existingUsers = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterUser where id = :theId")
        .setLong("theId", userId).list(FreshRequesterUser.class);

    if (GrouperUtil.length(existingUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }

    // permanently delete: remove memberships first, then remove the user
    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshRequesterMembership where userId = :userId")
        .setLong("userId", userId).executeUpdateInt();

    HibernateSession.byHqlStatic()
        .createQuery("delete from FreshRequesterUser where id = :theId")
        .setLong("theId", userId).executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
  }

  // ==================== Membership operations ====================

  /**
   * GET /requester_groups/{groupId}/members - get members of a group
   */
  public void getGroupMembers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String groupIdString = mockServiceRequest.getPostMockNamePaths()[1];

    GrouperUtil.assertion(GrouperUtil.length(groupIdString) > 0, "groupId is required");

    long groupId = GrouperUtil.longValue(groupIdString);

    // find all user ids in this group via membership table
    List<FreshRequesterMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterMembership where groupId = :theGroupId")
        .setLong("theGroupId", groupId).list(FreshRequesterMembership.class);

    ArrayNode usersArray = GrouperUtil.jsonJacksonArrayNode();

    for (FreshRequesterMembership membership : memberships) {
      List<FreshRequesterUser> users = HibernateSession.byHqlStatic()
          .createQuery("from FreshRequesterUser where id = :theId")
          .setLong("theId", membership.getUserId()).list(FreshRequesterUser.class);
      if (GrouperUtil.length(users) == 1) {
        ObjectNode objectNode = users.get(0).toJson(null);
        objectNode.put("id", users.get(0).getId());
        usersArray.add(objectNode);
      }
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    resultNode.set("requesters", usersArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * POST /requester_groups/{groupId}/members/{userId} - add a member to a group
   */
  public void addGroupMember(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String groupIdString = mockServiceRequest.getPostMockNamePaths()[1];
    String userIdString = mockServiceRequest.getPostMockNamePaths()[3];

    GrouperUtil.assertion(GrouperUtil.length(groupIdString) > 0, "groupId is required");
    GrouperUtil.assertion(GrouperUtil.length(userIdString) > 0, "userId is required");

    Long groupId = GrouperUtil.longValue(groupIdString);
    Long userId = GrouperUtil.longValue(userIdString);
    
    // check if group exists
    List<FreshRequesterGroup> groups = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterGroup where id = :theId")
        .setLong("theId", groupId).list(FreshRequesterGroup.class);

    if (GrouperUtil.length(groups) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }

    // check if user exists
    List<FreshRequesterUser> users = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterUser where id = :theId")
        .setLong("theId", userId).list(FreshRequesterUser.class);

    if (GrouperUtil.length(users) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }

    // check if already a member
    List<FreshRequesterMembership> existingMemberships = HibernateSession.byHqlStatic()
        .createQuery("from FreshRequesterMembership where groupId = :groupId and userId = :userId")
        .setLong("groupId", groupId)
        .setLong("userId", userId)
        .list(FreshRequesterMembership.class);

    if (GrouperUtil.length(existingMemberships) == 0) {
      FreshRequesterMembership membership = new FreshRequesterMembership();
      membership.setGroupId(groupId);
      membership.setUserId(userId);
      // set membership id to random long value to avoid conflicts
      membership.setId(ThreadLocalRandom.current().nextLong(1, 99999999));
      
      HibernateSession.byObjectStatic().save(membership);
    }

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
  }

  /**
   * DELETE /requester_groups/{groupId}/members/{userId} - remove a member from a group
   */
  public void removeGroupMember(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(401);
      throw e;
    }

    String groupIdString = mockServiceRequest.getPostMockNamePaths()[1];
    String userIdString = mockServiceRequest.getPostMockNamePaths()[3];

    GrouperUtil.assertion(GrouperUtil.length(groupIdString) > 0, "groupId is required");
    GrouperUtil.assertion(GrouperUtil.length(userIdString) > 0, "userId is required");

    long groupId = GrouperUtil.longValue(groupIdString);
    long userId = GrouperUtil.longValue(userIdString);

    int deleted = HibernateSession.byHqlStatic()
        .createQuery("delete from FreshRequesterMembership where groupId = :groupId and userId = :userId")
        .setLong("groupId", groupId)
        .setLong("userId", userId)
        .executeUpdateInt();

    if (deleted > 0) {
      mockServiceResponse.setResponseCode(204);
    } else {
      mockServiceResponse.setResponseCode(404);
    }
    mockServiceResponse.setContentType("application/json");
  }

  // ==================== Request routing ====================

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureFreshserviceMockTables();
    }
    mockTablesThere = true;

    // this must be there and it might be a caching issue:
    //     String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.exampleFreshRequester.mockExternalSystem.configId");
    // loop 10 times and wait a second each time if not there, to avoid issues with tables not being found
    // if no there at end give a good exception
    // after 10 seconds the caches should have cleared if everything is setup correctly.
    for (int i=0; i<10; i++) {
      // clear cache
      ConfigPropertiesCascadeBase.clearCache();
      String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.exampleFreshRequester.mockExternalSystem.configId");
      if (!StringUtils.isBlank(configId)) {
        break;
      }
      if (i >= 9) {
        throw new RuntimeException("grouper.properties grouperTest.exampleFreshRequester.mockExternalSystem.configId must be set to the configId of the external system used by mock!");
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
      // GET /requester_groups
      if ("requester_groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /requester_groups/{id}
      if ("requester_groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /requester_groups/{id}/members
      if ("requester_groups".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size()
          && "members".equals(mockNamePaths.get(2))) {
        getGroupMembers(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /requesters
      if ("requesters".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /requesters/{id}
      if ("requesters".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    // POST requests
    if (StringUtils.equals("POST", httpMethod)) {
      // POST /requester_groups
      if ("requester_groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      // POST /requester_groups/{id}/members/{userId}
      if ("requester_groups".equals(mockNamePaths.get(0)) && 4 == mockNamePaths.size()
          && "members".equals(mockNamePaths.get(2))) {
        addGroupMember(mockServiceRequest, mockServiceResponse);
        return;
      }
      // POST /requesters
      if ("requesters".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    // PUT requests
    if (StringUtils.equals("PUT", httpMethod)) {
      // PUT /requester_groups/{id}
      if ("requester_groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      // PUT /requesters/{id}/reactivate
      if ("requesters".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size()
          && "reactivate".equals(mockNamePaths.get(2))) {
        reactivateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      // PUT /requesters/{id}
      if ("requesters".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    // DELETE requests
    if (StringUtils.equals("DELETE", httpMethod)) {
      // DELETE /requester_groups/{id}
      if ("requester_groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      // DELETE /requester_groups/{id}/members/{userId}
      if ("requester_groups".equals(mockNamePaths.get(0)) && 4 == mockNamePaths.size()
          && "members".equals(mockNamePaths.get(2))) {
        removeGroupMember(mockServiceRequest, mockServiceResponse);
        return;
      }
      // DELETE /requesters/{id}/forget
      if ("requesters".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size()
          && "forget".equals(mockNamePaths.get(2))) {
        forgetUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      // DELETE /requesters/{id}
      if ("requesters".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
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
