package edu.internet2.middleware.grouper.app.datadog;

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
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class DatadogMockServiceHandler extends MockServiceHandler {

  public DatadogMockServiceHandler() {
  }

  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("dd-api-key", "dd-application-key");

  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  @Override
  public Set<String> doNotLogParameters() {
    return null;
  }

  private static boolean mockTablesThere = false;

  public static void ensureDatadogMockTables() {
    try {
      new GcDbAccess().sql("select count(*) from mock_datadog_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_datadog_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_datadog_membership").select(int.class);
    } catch (Exception e) {

      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {

        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          DatadogUser.createTableDatadogUser(ddlVersionBean, database);
          DatadogGroup.createTableDatadogGroup(ddlVersionBean, database);
          DatadogMembership.createTableDatadogMembership(ddlVersionBean, database);

        }
      });

    }

  }

  /**
   * Check authorization by validating DD-API-KEY and DD-APPLICATION-KEY headers
   * against the accessTokenPassword JSON in config
   */
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String apiKey = mockServiceRequest.getHttpServletRequest().getHeader("DD-API-KEY");
    String applicationKey = mockServiceRequest.getHttpServletRequest().getHeader("DD-APPLICATION-KEY");

    if (StringUtils.isBlank(apiKey) || StringUtils.isBlank(applicationKey)) {
      throw new RuntimeException("DD-API-KEY and DD-APPLICATION-KEY headers are required");
    }

    String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.exampleDatadog.mockExternalSystem.configId");
    String accessTokenPassword = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");

    JsonNode keysNode = GrouperUtil.jsonJacksonNode(accessTokenPassword);
    String expectedApiKey = GrouperUtil.jsonJacksonGetString(keysNode, "apiKey");
    String expectedApplicationKey = GrouperUtil.jsonJacksonGetString(keysNode, "applicationKey");

    if (!StringUtils.equals(expectedApiKey, apiKey)) {
      throw new RuntimeException("DD-API-KEY does not match");
    }
    if (!StringUtils.equals(expectedApplicationKey, applicationKey)) {
      throw new RuntimeException("DD-APPLICATION-KEY does not match");
    }
  }

  // ==================== User operations ====================

  /**
   * GET /users - retrieve all users
   */
  public void getUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    // check for filter param (used for lookup by email)
    String filterParam = mockServiceRequest.getHttpServletRequest().getParameter("filter");

    List<DatadogUser> datadogUsers = null;

    if (StringUtils.isNotBlank(filterParam)) {
      // filter is the email address
      datadogUsers = HibernateSession.byHqlStatic()
          .createQuery("from DatadogUser where email = :theEmail")
          .setString("theEmail", filterParam).list(DatadogUser.class);
    } else {
      ByHqlStatic query = HibernateSession.byHqlStatic().createQuery("from DatadogUser");
      QueryOptions queryOptions = new QueryOptions();
      queryOptions.sort(new QuerySort("id", true));
      query.options(queryOptions);
      datadogUsers = query.list(DatadogUser.class);
    }

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode dataArray = GrouperUtil.jsonJacksonArrayNode();

    for (DatadogUser datadogUser : datadogUsers) {
      ObjectNode userDataNode = GrouperUtil.jsonJacksonNode();
      userDataNode.put("type", "users");
      userDataNode.put("id", datadogUser.getId());

      ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();
      attributesNode.put("email", datadogUser.getEmail());
      if (datadogUser.getName() != null) {
        attributesNode.put("name", datadogUser.getName());
      }
      if (datadogUser.getTitle() != null) {
        attributesNode.put("title", datadogUser.getTitle());
      }
      attributesNode.put("handle", datadogUser.getEmail());
      attributesNode.put("disabled", datadogUser.getDisabled() != null && datadogUser.getDisabled());
      attributesNode.put("service_account", datadogUser.getServiceAccount() != null && datadogUser.getServiceAccount());

      userDataNode.set("attributes", attributesNode);
      dataArray.add(userDataNode);
    }

    resultNode.set("data", dataArray);

    ObjectNode metaNode = GrouperUtil.jsonJacksonNode();
    ObjectNode pageNode = GrouperUtil.jsonJacksonNode();
    pageNode.put("total_count", datadogUsers.size());
    pageNode.put("total_filtered_count", datadogUsers.size());
    metaNode.set("page", pageNode);
    resultNode.set("meta", metaNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * POST /users - create a new user (invitation)
   */
  public void postUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");
    JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(dataNode, "attributes");

    String email = GrouperUtil.jsonJacksonGetString(attributesNode, "email");

    if (StringUtils.isBlank(email)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"email is required\"]}");
      return;
    }

    // check for existing user with same email (409 conflict)
    List<DatadogUser> existing = HibernateSession.byHqlStatic()
        .createQuery("from DatadogUser where email = :theEmail")
        .setString("theEmail", email).list(DatadogUser.class);

    if (existing.size() > 0) {
      mockServiceResponse.setResponseCode(409);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"User already exists\"]}");
      return;
    }

    String id = GrouperUuid.getUuid();
    String name = GrouperUtil.jsonJacksonGetString(attributesNode, "name");
    String title = GrouperUtil.jsonJacksonGetString(attributesNode, "title");

    DatadogUser datadogUser = new DatadogUser();
    datadogUser.setId(id);
    datadogUser.setEmail(email);
    datadogUser.setName(name);
    datadogUser.setTitle(title);
    datadogUser.setDisabled(false);
    datadogUser.setServiceAccount(false);

    HibernateSession.byObjectStatic().save(datadogUser);

    // build response
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode userDataNode = GrouperUtil.jsonJacksonNode();
    userDataNode.put("type", "users");
    userDataNode.put("id", id);

    ObjectNode respAttributesNode = GrouperUtil.jsonJacksonNode();
    respAttributesNode.put("email", email);
    if (name != null) {
      respAttributesNode.put("name", name);
    }
    if (title != null) {
      respAttributesNode.put("title", title);
    }
    respAttributesNode.put("handle", email);
    respAttributesNode.put("disabled", false);
    respAttributesNode.put("service_account", false);

    userDataNode.set("attributes", respAttributesNode);
    resultNode.set("data", userDataNode);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * PATCH /users/{id} - update a user
   */
  public void patchUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String userId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    DatadogUser datadogUser = HibernateSession.byHqlStatic()
        .createQuery("from DatadogUser where id = :theId")
        .setString("theId", userId).uniqueResult(DatadogUser.class);

    if (datadogUser == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"User not found\"]}");
      return;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");
    JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(dataNode, "attributes");

    if (attributesNode != null) {
      String email = GrouperUtil.jsonJacksonGetString(attributesNode, "email");
      if (email != null) {
        datadogUser.setEmail(email);
      }
      String name = GrouperUtil.jsonJacksonGetString(attributesNode, "name");
      if (name != null) {
        datadogUser.setName(name);
      }
      String title = GrouperUtil.jsonJacksonGetString(attributesNode, "title");
      if (title != null) {
        datadogUser.setTitle(title);
      }
      Boolean disabled = GrouperUtil.jsonJacksonGetBoolean(attributesNode, "disabled");
      if (disabled != null) {
        datadogUser.setDisabled(disabled);
      }
    }

    HibernateSession.byObjectStatic().update(datadogUser);

    // build response
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode userDataNode = GrouperUtil.jsonJacksonNode();
    userDataNode.put("type", "users");
    userDataNode.put("id", datadogUser.getId());

    ObjectNode respAttributesNode = GrouperUtil.jsonJacksonNode();
    respAttributesNode.put("email", datadogUser.getEmail());
    if (datadogUser.getName() != null) {
      respAttributesNode.put("name", datadogUser.getName());
    }
    if (datadogUser.getTitle() != null) {
      respAttributesNode.put("title", datadogUser.getTitle());
    }
    respAttributesNode.put("handle", datadogUser.getEmail());
    respAttributesNode.put("disabled", datadogUser.getDisabled() != null && datadogUser.getDisabled());
    respAttributesNode.put("service_account", datadogUser.getServiceAccount() != null && datadogUser.getServiceAccount());

    userDataNode.set("attributes", respAttributesNode);
    resultNode.set("data", userDataNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  // ==================== Role operations ====================

  /**
   * GET /roles - retrieve all roles
   */
  public void getRoles(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where groupType = :theGroupType")
        .setString("theGroupType", "role");
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);
    List<DatadogGroup> datadogGroups = query.list(DatadogGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode dataArray = GrouperUtil.jsonJacksonArrayNode();

    for (DatadogGroup datadogGroup : datadogGroups) {
      ObjectNode roleDataNode = GrouperUtil.jsonJacksonNode();
      roleDataNode.put("type", "roles");
      roleDataNode.put("id", datadogGroup.getId());

      ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();
      if (datadogGroup.getName() != null) {
        attributesNode.put("name", datadogGroup.getName());
      }

      roleDataNode.set("attributes", attributesNode);
      dataArray.add(roleDataNode);
    }

    resultNode.set("data", dataArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * POST /roles - create a new role
   */
  public void postRole(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");
    JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(dataNode, "attributes");

    String name = GrouperUtil.jsonJacksonGetString(attributesNode, "name");

    if (StringUtils.isBlank(name)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"name is required\"]}");
      return;
    }

    // check if role with same name already exists (duplicate check)
    DatadogGroup existingGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where name = :theName and groupType = :theType")
        .setString("theName", name).setString("theType", "role").uniqueResult(DatadogGroup.class);

    if (existingGroup != null) {
      mockServiceResponse.setResponseCode(409);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Role with name '" + name + "' already exists\"]}");
      return;
    }

    String id = GrouperUuid.getUuid();

    DatadogGroup datadogGroup = new DatadogGroup();
    datadogGroup.setId(id);
    datadogGroup.setName(name);
    datadogGroup.setGroupType("role");

    HibernateSession.byObjectStatic().save(datadogGroup);

    // build response
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode roleDataNode = GrouperUtil.jsonJacksonNode();
    roleDataNode.put("type", "roles");
    roleDataNode.put("id", id);

    ObjectNode respAttributesNode = GrouperUtil.jsonJacksonNode();
    respAttributesNode.put("name", name);

    roleDataNode.set("attributes", respAttributesNode);
    resultNode.set("data", roleDataNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * PATCH /roles/{id} - update a role
   */
  public void patchRole(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String roleId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    DatadogGroup datadogGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where id = :theId and groupType = :theGroupType")
        .setString("theId", roleId).setString("theGroupType", "role").uniqueResult(DatadogGroup.class);

    if (datadogGroup == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Role not found\"]}");
      return;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");
    JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(dataNode, "attributes");

    if (attributesNode != null) {
      String name = GrouperUtil.jsonJacksonGetString(attributesNode, "name");
      if (name != null) {
        datadogGroup.setName(name);
      }
    }

    HibernateSession.byObjectStatic().update(datadogGroup);

    // build response
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode roleDataNode = GrouperUtil.jsonJacksonNode();
    roleDataNode.put("type", "roles");
    roleDataNode.put("id", datadogGroup.getId());

    ObjectNode respAttributesNode = GrouperUtil.jsonJacksonNode();
    if (datadogGroup.getName() != null) {
      respAttributesNode.put("name", datadogGroup.getName());
    }

    roleDataNode.set("attributes", respAttributesNode);
    resultNode.set("data", roleDataNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /roles/{id} - delete a role
   */
  public void deleteRole(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String roleId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    DatadogGroup datadogGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where id = :theId and groupType = :theGroupType")
        .setString("theId", roleId).setString("theGroupType", "role").uniqueResult(DatadogGroup.class);

    if (datadogGroup == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Role not found\"]}");
      return;
    }

    HibernateSession.byObjectStatic().delete(datadogGroup);

    mockServiceResponse.setResponseCode(204);
  }

  // ==================== Role membership operations ====================

  /**
   * GET /roles/{roleId}/users - get users assigned to a role
   */
  public void getRoleUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String roleId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    // verify role exists
    DatadogGroup datadogGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where id = :theId and groupType = :theGroupType")
        .setString("theId", roleId).setString("theGroupType", "role").uniqueResult(DatadogGroup.class);

    if (datadogGroup == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Role not found\"]}");
      return;
    }

    // find memberships for this role
    List<DatadogMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from DatadogMembership where groupId = :theGroupId")
        .setString("theGroupId", roleId).list(DatadogMembership.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode dataArray = GrouperUtil.jsonJacksonArrayNode();

    for (DatadogMembership membership : memberships) {
      // look up the user
      DatadogUser datadogUser = HibernateSession.byHqlStatic()
          .createQuery("from DatadogUser where id = :theId")
          .setString("theId", membership.getUserId()).uniqueResult(DatadogUser.class);

      if (datadogUser == null) {
        continue;
      }

      ObjectNode userDataNode = GrouperUtil.jsonJacksonNode();
      userDataNode.put("type", "users");
      userDataNode.put("id", datadogUser.getId());

      ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();
      attributesNode.put("email", datadogUser.getEmail());
      if (datadogUser.getName() != null) {
        attributesNode.put("name", datadogUser.getName());
      }
      attributesNode.put("handle", datadogUser.getEmail());
      attributesNode.put("disabled", datadogUser.getDisabled() != null && datadogUser.getDisabled());
      attributesNode.put("service_account", datadogUser.getServiceAccount() != null && datadogUser.getServiceAccount());

      userDataNode.set("attributes", attributesNode);
      dataArray.add(userDataNode);
    }

    resultNode.set("data", dataArray);

    ObjectNode metaNode = GrouperUtil.jsonJacksonNode();
    ObjectNode pageNode = GrouperUtil.jsonJacksonNode();
    pageNode.put("total_count", dataArray.size());
    pageNode.put("total_filtered_count", dataArray.size());
    metaNode.set("page", pageNode);
    resultNode.set("meta", metaNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * POST /roles/{roleId}/users - add user to role
   */
  public void postRoleUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String roleId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    // verify role exists
    DatadogGroup datadogGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where id = :theId and groupType = :theGroupType")
        .setString("theId", roleId).setString("theGroupType", "role").uniqueResult(DatadogGroup.class);

    if (datadogGroup == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Role not found\"]}");
      return;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");

    String userId = GrouperUtil.jsonJacksonGetString(dataNode, "id");

    if (StringUtils.isBlank(userId)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"user id is required\"]}");
      return;
    }

    // verify user exists
    DatadogUser datadogUser = HibernateSession.byHqlStatic()
        .createQuery("from DatadogUser where id = :theId")
        .setString("theId", userId).uniqueResult(DatadogUser.class);

    if (datadogUser == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"User not found\"]}");
      return;
    }

    // check if membership already exists
    DatadogMembership existing = HibernateSession.byHqlStatic()
        .createQuery("from DatadogMembership where groupId = :theGroupId and userId = :theUserId")
        .setString("theGroupId", roleId).setString("theUserId", userId).uniqueResult(DatadogMembership.class);

    if (existing == null) {
      DatadogMembership membership = new DatadogMembership();
      membership.setId(GrouperUuid.getUuid());
      membership.setGroupId(roleId);
      membership.setUserId(userId);
      HibernateSession.byObjectStatic().save(membership);
    }

    // build response - return the users now in the role
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode dataArray = GrouperUtil.jsonJacksonArrayNode();

    ObjectNode userDataNode = GrouperUtil.jsonJacksonNode();
    userDataNode.put("type", "users");
    userDataNode.put("id", datadogUser.getId());

    ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();
    attributesNode.put("email", datadogUser.getEmail());
    if (datadogUser.getName() != null) {
      attributesNode.put("name", datadogUser.getName());
    }
    attributesNode.put("handle", datadogUser.getEmail());

    userDataNode.set("attributes", attributesNode);
    dataArray.add(userDataNode);

    resultNode.set("data", dataArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /roles/{roleId}/users - remove user from role (DELETE with body)
   */
  public void deleteRoleUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String roleId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");

    String userId = GrouperUtil.jsonJacksonGetString(dataNode, "id");

    if (StringUtils.isBlank(userId)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"user id is required\"]}");
      return;
    }

    DatadogMembership membership = HibernateSession.byHqlStatic()
        .createQuery("from DatadogMembership where groupId = :theGroupId and userId = :theUserId")
        .setString("theGroupId", roleId).setString("theUserId", userId).uniqueResult(DatadogMembership.class);

    if (membership == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Membership not found\"]}");
      return;
    }

    // look up user for response
    DatadogUser datadogUser = HibernateSession.byHqlStatic()
        .createQuery("from DatadogUser where id = :theId")
        .setString("theId", userId).uniqueResult(DatadogUser.class);

    HibernateSession.byObjectStatic().delete(membership);

    // build response
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode dataArray = GrouperUtil.jsonJacksonArrayNode();

    if (datadogUser != null) {
      ObjectNode userDataNode = GrouperUtil.jsonJacksonNode();
      userDataNode.put("type", "users");
      userDataNode.put("id", datadogUser.getId());

      ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();
      attributesNode.put("email", datadogUser.getEmail());
      if (datadogUser.getName() != null) {
        attributesNode.put("name", datadogUser.getName());
      }
      attributesNode.put("handle", datadogUser.getEmail());

      userDataNode.set("attributes", attributesNode);
      dataArray.add(userDataNode);
    }

    resultNode.set("data", dataArray);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  // ==================== Team operations ====================

  /**
   * GET /team - retrieve all teams (with pagination)
   */
  public void getTeams(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where groupType = :theGroupType")
        .setString("theGroupType", "team");
    QueryOptions queryOptions = new QueryOptions();
    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);
    List<DatadogGroup> datadogGroups = query.list(DatadogGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode dataArray = GrouperUtil.jsonJacksonArrayNode();

    for (DatadogGroup datadogGroup : datadogGroups) {
      ObjectNode teamDataNode = GrouperUtil.jsonJacksonNode();
      teamDataNode.put("type", "team");
      teamDataNode.put("id", datadogGroup.getId());

      ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();
      if (datadogGroup.getName() != null) {
        attributesNode.put("name", datadogGroup.getName());
      }
      if (datadogGroup.getHandle() != null) {
        attributesNode.put("handle", datadogGroup.getHandle());
      }
      if (datadogGroup.getDescription() != null) {
        attributesNode.put("description", datadogGroup.getDescription());
      }

      teamDataNode.set("attributes", attributesNode);
      dataArray.add(teamDataNode);
    }

    resultNode.set("data", dataArray);

    ObjectNode metaNode = GrouperUtil.jsonJacksonNode();
    ObjectNode paginationNode = GrouperUtil.jsonJacksonNode();
    paginationNode.put("total", datadogGroups.size());
    paginationNode.put("offset", 0);
    paginationNode.put("limit", 100);
    metaNode.set("pagination", paginationNode);
    resultNode.set("meta", metaNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * POST /team - create a new team
   */
  public void postTeam(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");
    JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(dataNode, "attributes");

    String name = GrouperUtil.jsonJacksonGetString(attributesNode, "name");
    String handle = GrouperUtil.jsonJacksonGetString(attributesNode, "handle");

    if (StringUtils.isBlank(name) || StringUtils.isBlank(handle)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"name and handle are required\"]}");
      return;
    }

    String description = GrouperUtil.jsonJacksonGetString(attributesNode, "description");

    // check if team with same name already exists (duplicate check)
    DatadogGroup existingGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where name = :theName and groupType = :theType")
        .setString("theName", name).setString("theType", "team").uniqueResult(DatadogGroup.class);

    if (existingGroup != null) {
      mockServiceResponse.setResponseCode(409);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Team with name '" + name + "' already exists\"]}");
      return;
    }

    String id = GrouperUuid.getUuid();

    DatadogGroup datadogGroup = new DatadogGroup();
    datadogGroup.setId(id);
    datadogGroup.setName(name);
    datadogGroup.setHandle(handle);
    datadogGroup.setDescription(description);
    datadogGroup.setGroupType("team");

    HibernateSession.byObjectStatic().save(datadogGroup);

    // build response
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode teamDataNode = GrouperUtil.jsonJacksonNode();
    teamDataNode.put("type", "team");
    teamDataNode.put("id", id);

    ObjectNode respAttributesNode = GrouperUtil.jsonJacksonNode();
    respAttributesNode.put("name", name);
    respAttributesNode.put("handle", handle);
    if (description != null) {
      respAttributesNode.put("description", description);
    }

    teamDataNode.set("attributes", respAttributesNode);
    resultNode.set("data", teamDataNode);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * PATCH /team/{id} - update a team
   */
  public void patchTeam(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String teamId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    DatadogGroup datadogGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where id = :theId and groupType = :theGroupType")
        .setString("theId", teamId).setString("theGroupType", "team").uniqueResult(DatadogGroup.class);

    if (datadogGroup == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Team not found\"]}");
      return;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");
    JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(dataNode, "attributes");

    if (attributesNode != null) {
      String name = GrouperUtil.jsonJacksonGetString(attributesNode, "name");
      if (name != null) {
        datadogGroup.setName(name);
      }
      String handle = GrouperUtil.jsonJacksonGetString(attributesNode, "handle");
      if (handle != null) {
        datadogGroup.setHandle(handle);
      }
      String description = GrouperUtil.jsonJacksonGetString(attributesNode, "description");
      if (description != null) {
        datadogGroup.setDescription(description);
      }
    }

    HibernateSession.byObjectStatic().update(datadogGroup);

    // build response
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode teamDataNode = GrouperUtil.jsonJacksonNode();
    teamDataNode.put("type", "team");
    teamDataNode.put("id", datadogGroup.getId());

    ObjectNode respAttributesNode = GrouperUtil.jsonJacksonNode();
    if (datadogGroup.getName() != null) {
      respAttributesNode.put("name", datadogGroup.getName());
    }
    if (datadogGroup.getHandle() != null) {
      respAttributesNode.put("handle", datadogGroup.getHandle());
    }
    if (datadogGroup.getDescription() != null) {
      respAttributesNode.put("description", datadogGroup.getDescription());
    }

    teamDataNode.set("attributes", respAttributesNode);
    resultNode.set("data", teamDataNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /team/{id} - delete a team
   */
  public void deleteTeam(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String teamId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    DatadogGroup datadogGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where id = :theId and groupType = :theGroupType")
        .setString("theId", teamId).setString("theGroupType", "team").uniqueResult(DatadogGroup.class);

    if (datadogGroup == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Team not found\"]}");
      return;
    }

    HibernateSession.byObjectStatic().delete(datadogGroup);

    mockServiceResponse.setResponseCode(204);
  }

  // ==================== Team membership operations ====================

  /**
   * GET /team/{teamId}/memberships - get memberships for a team
   */
  public void getTeamMemberships(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String teamId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    // verify team exists
    DatadogGroup datadogGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where id = :theId and groupType = :theGroupType")
        .setString("theId", teamId).setString("theGroupType", "team").uniqueResult(DatadogGroup.class);

    if (datadogGroup == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Team not found\"]}");
      return;
    }

    // find memberships for this team
    List<DatadogMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from DatadogMembership where groupId = :theGroupId")
        .setString("theGroupId", teamId).list(DatadogMembership.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode dataArray = GrouperUtil.jsonJacksonArrayNode();

    for (DatadogMembership membership : memberships) {
      ObjectNode membershipNode = GrouperUtil.jsonJacksonNode();
      membershipNode.put("type", "team_memberships");
      membershipNode.put("id", membership.getId());

      ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();
      attributesNode.put("role", membership.getRole() != null ? membership.getRole() : "member");
      membershipNode.set("attributes", attributesNode);

      ObjectNode relationshipsNode = GrouperUtil.jsonJacksonNode();

      ObjectNode teamRelNode = GrouperUtil.jsonJacksonNode();
      ObjectNode teamDataNode = GrouperUtil.jsonJacksonNode();
      teamDataNode.put("type", "team");
      teamDataNode.put("id", teamId);
      teamRelNode.set("data", teamDataNode);
      relationshipsNode.set("team", teamRelNode);

      ObjectNode userRelNode = GrouperUtil.jsonJacksonNode();
      ObjectNode userDataNode = GrouperUtil.jsonJacksonNode();
      userDataNode.put("type", "users");
      userDataNode.put("id", membership.getUserId());
      userRelNode.set("data", userDataNode);
      relationshipsNode.set("user", userRelNode);

      membershipNode.set("relationships", relationshipsNode);
      dataArray.add(membershipNode);
    }

    resultNode.set("data", dataArray);

    ObjectNode metaNode = GrouperUtil.jsonJacksonNode();
    ObjectNode paginationNode = GrouperUtil.jsonJacksonNode();
    paginationNode.put("total", dataArray.size());
    paginationNode.put("offset", 0);
    paginationNode.put("limit", 100);
    metaNode.set("pagination", paginationNode);
    resultNode.set("meta", metaNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * POST /team/{teamId}/memberships - add user to team
   */
  public void postTeamMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String teamId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    // verify team exists
    DatadogGroup datadogGroup = HibernateSession.byHqlStatic()
        .createQuery("from DatadogGroup where id = :theId and groupType = :theGroupType")
        .setString("theId", teamId).setString("theGroupType", "team").uniqueResult(DatadogGroup.class);

    if (datadogGroup == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Team not found\"]}");
      return;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");

    // extract userId from relationships.user.data.id
    String userId = null;
    JsonNode relationshipsNode = GrouperUtil.jsonJacksonGetNode(dataNode, "relationships");
    if (relationshipsNode != null) {
      JsonNode userRelNode = GrouperUtil.jsonJacksonGetNode(relationshipsNode, "user");
      if (userRelNode != null) {
        JsonNode userDataNode = GrouperUtil.jsonJacksonGetNode(userRelNode, "data");
        if (userDataNode != null) {
          userId = GrouperUtil.jsonJacksonGetString(userDataNode, "id");
        }
      }
    }

    if (StringUtils.isBlank(userId)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"user id is required in relationships.user.data.id\"]}");
      return;
    }

    // verify user exists
    DatadogUser datadogUser = HibernateSession.byHqlStatic()
        .createQuery("from DatadogUser where id = :theId")
        .setString("theId", userId).uniqueResult(DatadogUser.class);

    if (datadogUser == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"User not found\"]}");
      return;
    }

    // check if membership already exists
    DatadogMembership existing = HibernateSession.byHqlStatic()
        .createQuery("from DatadogMembership where groupId = :theGroupId and userId = :theUserId")
        .setString("theGroupId", teamId).setString("theUserId", userId).uniqueResult(DatadogMembership.class);

    String membershipId;
    if (existing == null) {
      membershipId = GrouperUuid.getUuid();
      DatadogMembership membership = new DatadogMembership();
      membership.setId(membershipId);
      membership.setGroupId(teamId);
      membership.setUserId(userId);
      membership.setRole("member");
      HibernateSession.byObjectStatic().save(membership);
    } else {
      membershipId = existing.getId();
    }

    // build response
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ObjectNode membershipDataNode = GrouperUtil.jsonJacksonNode();
    membershipDataNode.put("type", "team_memberships");
    membershipDataNode.put("id", membershipId);

    ObjectNode attributesNode = GrouperUtil.jsonJacksonNode();
    attributesNode.put("role", "member");
    membershipDataNode.set("attributes", attributesNode);

    ObjectNode respRelationshipsNode = GrouperUtil.jsonJacksonNode();
    ObjectNode userRelNode = GrouperUtil.jsonJacksonNode();
    ObjectNode userDataNode = GrouperUtil.jsonJacksonNode();
    userDataNode.put("type", "users");
    userDataNode.put("id", userId);
    userRelNode.set("data", userDataNode);
    respRelationshipsNode.set("user", userRelNode);
    membershipDataNode.set("relationships", respRelationshipsNode);

    resultNode.set("data", membershipDataNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  /**
   * DELETE /team/{teamId}/memberships/{userId} - remove user from team
   */
  public void deleteTeamMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String teamId, String userId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    DatadogMembership membership = HibernateSession.byHqlStatic()
        .createQuery("from DatadogMembership where groupId = :theGroupId and userId = :theUserId")
        .setString("theGroupId", teamId).setString("theUserId", userId).uniqueResult(DatadogMembership.class);

    if (membership == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Membership not found\"]}");
      return;
    }

    HibernateSession.byObjectStatic().delete(membership);

    mockServiceResponse.setResponseCode(204);
  }

  /**
   * PATCH /team/{teamId}/memberships/{userId} - update team membership role
   */
  public void patchTeamMembership(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String teamId, String userId) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      mockServiceResponse.setResponseCode(403);
      throw e;
    }

    DatadogMembership membership = HibernateSession.byHqlStatic()
        .createQuery("from DatadogMembership where groupId = :theGroupId and userId = :theUserId")
        .setString("theGroupId", teamId).setString("theUserId", userId).uniqueResult(DatadogMembership.class);

    if (membership == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[\"Membership not found\"]}");
      return;
    }

    String requestBody = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBody);
    JsonNode dataNode = GrouperUtil.jsonJacksonGetNode(requestNode, "data");
    JsonNode attributesNode = GrouperUtil.jsonJacksonGetNode(dataNode, "attributes");
    String role = GrouperUtil.jsonJacksonGetString(attributesNode, "role");

    // Datadog API only accepts "admin" or omitted role (to remove admin)
    if (StringUtils.isNotBlank(role) && !"admin".equals(role)) {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody("{\"errors\":[{\"status\":\"400\",\"title\":\"Bad Request\",\"detail\":\"invalid role value: " + role + ". Only 'admin' is supported, or omit role to remove admin privileges\"}]}");
      return;
    }

    if (StringUtils.isBlank(role)) {
      role = "member";
    }
    membership.setRole(role);
    HibernateSession.byObjectStatic().saveOrUpdate(membership);

    // build response
    ObjectNode responseRoot = GrouperUtil.jsonJacksonNode();
    ObjectNode responseData = GrouperUtil.jsonJacksonNode();
    responseData.put("type", "team_memberships");
    responseData.put("id", membership.getId());
    ObjectNode responseAttributes = GrouperUtil.jsonJacksonNode();
    responseAttributes.put("role", role);
    responseData.set("attributes", responseAttributes);
    ObjectNode responseRelationships = GrouperUtil.jsonJacksonNode();
    ObjectNode userRel = GrouperUtil.jsonJacksonNode();
    ObjectNode userData = GrouperUtil.jsonJacksonNode();
    userData.put("type", "users");
    userData.put("id", userId);
    userRel.set("data", userData);
    responseRelationships.set("user", userRel);
    ObjectNode teamRel = GrouperUtil.jsonJacksonNode();
    ObjectNode teamData = GrouperUtil.jsonJacksonNode();
    teamData.put("type", "team");
    teamData.put("id", teamId);
    teamRel.set("data", teamData);
    responseRelationships.set("team", teamRel);
    responseData.set("relationships", responseRelationships);
    responseRoot.set("data", responseData);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(responseRoot));
  }

  // ==================== Request routing ====================

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureDatadogMockTables();
    }
    mockTablesThere = true;

    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.exampleDatadog.mockExternalSystem.configId");
    if (StringUtils.isBlank(configId)) {
      for (int i = 0; i < 40; i++) {
        configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.exampleDatadog.mockExternalSystem.configId");
        if (!StringUtils.isBlank(configId)) {
          break;
        }
        if (i == 39) {
          throw new RuntimeException("grouper.properties grouperTest.exampleDatadog.mockExternalSystem.configId must be set!");
        }
        GrouperUtil.sleep(1000);
      }
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
      // GET /users or GET /users?filter=email
      if ("users".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /roles
      if ("roles".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getRoles(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /roles/{id}/users
      if ("roles".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size() && "users".equals(mockNamePaths.get(2))) {
        getRoleUsers(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // GET /team
      if ("team".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getTeams(mockServiceRequest, mockServiceResponse);
        return;
      }
      // GET /team/{id}/memberships
      if ("team".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size() && "memberships".equals(mockNamePaths.get(2))) {
        getTeamMemberships(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
    }

    // POST requests
    if (StringUtils.equals("POST", httpMethod)) {
      // POST /users
      if ("users".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      // POST /roles
      if ("roles".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postRole(mockServiceRequest, mockServiceResponse);
        return;
      }
      // POST /roles/{id}/users
      if ("roles".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size() && "users".equals(mockNamePaths.get(2))) {
        postRoleUser(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // POST /team
      if ("team".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postTeam(mockServiceRequest, mockServiceResponse);
        return;
      }
      // POST /team/{id}/memberships
      if ("team".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size() && "memberships".equals(mockNamePaths.get(2))) {
        postTeamMembership(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
    }

    // PATCH requests
    if (StringUtils.equals("PATCH", httpMethod)) {
      // PATCH /users/{id}
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        patchUser(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // PATCH /roles/{id}
      if ("roles".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        patchRole(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // PATCH /team/{id}
      if ("team".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        patchTeam(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // PATCH /team/{id}/memberships/{userId}
      if ("team".equals(mockNamePaths.get(0)) && 4 == mockNamePaths.size() && "memberships".equals(mockNamePaths.get(2))) {
        patchTeamMembership(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1), mockNamePaths.get(3));
        return;
      }
    }

    // DELETE requests
    if (StringUtils.equals("DELETE", httpMethod)) {
      // DELETE /roles/{id}
      if ("roles".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteRole(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // DELETE /roles/{id}/users (DELETE with body)
      if ("roles".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size() && "users".equals(mockNamePaths.get(2))) {
        deleteRoleUser(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // DELETE /team/{id}
      if ("team".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteTeam(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1));
        return;
      }
      // DELETE /team/{id}/memberships/{userId}
      if ("team".equals(mockNamePaths.get(0)) && 4 == mockNamePaths.size() && "memberships".equals(mockNamePaths.get(2))) {
        deleteTeamMembership(mockServiceRequest, mockServiceResponse, mockNamePaths.get(1), mockNamePaths.get(3));
        return;
      }
    }

    throw new RuntimeException("Unhandled Datadog mock request: " + httpMethod + " " + StringUtils.join(mockNamePaths, "/"));
  }

}
