package edu.internet2.middleware.grouper.app.libreChat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Mock LibreChat admin API for offline tests, backed by the mock_librechat_* tables.  Routes under mock
 * name "libreChat", e.g. .../mockServices/libreChat/api/admin/groups.  Auth is a static Bearer token (the
 * tests configure the external system with httpAuthnType=bearerToken).  Responses match LibreChat
 * v0.8.8: {groups,total,limit,offset}, {group}, {members,...}, {users,...}, ids are 24 hex characters,
 * and member add and remove are idempotent.
 *
 * GRP-7594
 */
public class LibreChatMockServiceHandler extends MockServiceHandler {

  public LibreChatMockServiceHandler() {
  }

  /** never log */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  @Override
  public Set<String> doNotLogParameters() {
    return null;
  }

  /** if the tables were checked in this JVM */
  private static boolean mockTablesThere = false;

  /**
   * Create the mock_librechat_* tables if they do not exist yet
   */
  public static void ensureLibreChatMockTables() {
    try {
      new GcDbAccess().sql("select count(*) from mock_librechat_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_librechat_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_librechat_membership").select(int.class);
    } catch (Exception e) {
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          LibreChatUser.createTableLibreChatUser(ddlVersionBean, database);
          LibreChatGroup.createTableLibreChatGroup(ddlVersionBean, database);
          LibreChatMembership.createTableLibreChatMembership(ddlVersionBean, database);
        }
      });
    }
  }

  /**
   * The Bearer token must equal the external system's accessTokenPassword.  The mock runs in the test
   * Tomcat JVM, which caches config, so on a mismatch the cache is cleared and the check retried once.
   * @param mockServiceRequest request
   */
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String authHeader = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization: Bearer header is required");
    }
    String bearerToken = authHeader.substring("Bearer ".length()).trim();
    if (bearerTokenMatches(bearerToken)) {
      return;
    }
    ConfigPropertiesCascadeBase.clearCache();
    if (!bearerTokenMatches(bearerToken)) {
      throw new RuntimeException("Authorization Bearer token does not match accessTokenPassword");
    }
  }

  /**
   * @param bearerToken from the header
   * @return true if it equals the mock external system's accessTokenPassword
   */
  private boolean bearerTokenMatches(String bearerToken) {
    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.libreChat.mockExternalSystem.configId");
    if (StringUtils.isBlank(configId)) {
      return false;
    }
    String accessTokenPassword = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");
    return !StringUtils.isBlank(accessTokenPassword) && StringUtils.equals(accessTokenPassword, bearerToken);
  }

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureLibreChatMockTables();
      mockTablesThere = true;
    }

    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      json(mockServiceResponse, 401, error("Unauthorized"));
      return;
    }

    // e.g. api/admin/groups/{id}/members/{userId}
    String[] paths = mockServiceRequest.getPostMockNamePaths();
    if (GrouperUtil.length(paths) < 3 || !"api".equals(paths[0]) || !"admin".equals(paths[1])) {
      throw new RuntimeException("LibreChat mock expects /api/admin/...");
    }
    String httpMethod = mockServiceRequest.getHttpServletRequest().getMethod();
    String resource = paths[2];
    String id = paths.length >= 4 ? paths[3] : null;
    String subResource = paths.length >= 5 ? paths[4] : null;
    String subId = paths.length >= 6 ? paths[5] : null;

    if ("users".equals(resource) && "GET".equals(httpMethod)) {
      if (id == null) {
        listUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("search".equals(id)) {
        searchUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    if ("groups".equals(resource)) {
      if (id == null) {
        if ("GET".equals(httpMethod)) {
          listGroups(mockServiceRequest, mockServiceResponse);
          return;
        }
        if ("POST".equals(httpMethod)) {
          createGroup(mockServiceRequest, mockServiceResponse);
          return;
        }
      } else if (subResource == null) {
        if ("GET".equals(httpMethod)) {
          getGroup(mockServiceResponse, id);
          return;
        }
        if ("PATCH".equals(httpMethod)) {
          updateGroup(mockServiceRequest, mockServiceResponse, id);
          return;
        }
        if ("DELETE".equals(httpMethod)) {
          deleteGroup(mockServiceResponse, id);
          return;
        }
      } else if ("members".equals(subResource)) {
        if (subId == null && "GET".equals(httpMethod)) {
          listMembers(mockServiceRequest, mockServiceResponse, id);
          return;
        }
        if (subId == null && "POST".equals(httpMethod)) {
          addMember(mockServiceRequest, mockServiceResponse, id);
          return;
        }
        if (subId != null && "DELETE".equals(httpMethod)) {
          removeMember(mockServiceResponse, id, subId);
          return;
        }
      }
    }

    throw new RuntimeException("Unsupported LibreChat mock request: " + httpMethod + " "
        + mockServiceRequest.getPostMockNamePath());
  }

  // ==================== users ====================

  /**
   * GET /api/admin/users?limit=&offset=
   */
  private void listUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    List<LibreChatUser> users = HibernateSession.byHqlStatic()
        .createQuery("from LibreChatUser order by id").list(LibreChatUser.class);
    List<ObjectNode> items = new ArrayList<ObjectNode>();
    for (LibreChatUser user : users) {
      items.add(user.toJson());
    }
    page(mockServiceRequest, mockServiceResponse, "users", items);
  }

  /**
   * GET /api/admin/users/search?q=&limit= (prefix match on name, email, username; at most 50)
   */
  private void searchUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    String q = StringUtils.trimToEmpty(mockServiceRequest.getHttpServletRequest().getParameter("q")).toLowerCase();
    if (q.length() < 2) {
      json(mockServiceResponse, 400, error("Query must be at least 2 characters"));
      return;
    }
    int limit = Math.min(Math.max(1, GrouperUtil.intValue(mockServiceRequest.getHttpServletRequest().getParameter("limit"), 20)), 50);
    List<LibreChatUser> users = HibernateSession.byHqlStatic()
        .createQuery("from LibreChatUser order by name").list(LibreChatUser.class);
    List<ObjectNode> items = new ArrayList<ObjectNode>();
    for (LibreChatUser user : users) {
      if (StringUtils.startsWith(StringUtils.lowerCase(user.getUsername()), q)
          || StringUtils.startsWith(StringUtils.lowerCase(user.getName()), q)
          || StringUtils.startsWith(StringUtils.lowerCase(user.getEmail()), q)) {
        items.add(user.toJson());
        if (items.size() >= limit) {
          break;
        }
      }
    }
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("users", LibreChatApiCommands.toArrayNode(items));
    result.put("total", items.size());
    json(mockServiceResponse, 200, result);
  }

  // ==================== groups ====================

  /**
   * GET /api/admin/groups?source=&limit=&offset=
   */
  private void listGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    String source = mockServiceRequest.getHttpServletRequest().getParameter("source");
    List<LibreChatGroup> groups = HibernateSession.byHqlStatic()
        .createQuery("from LibreChatGroup order by id").list(LibreChatGroup.class);
    List<ObjectNode> items = new ArrayList<ObjectNode>();
    for (LibreChatGroup group : groups) {
      if (StringUtils.isBlank(source) || StringUtils.equals(source, group.getSource())) {
        items.add(group.toJson());
      }
    }
    page(mockServiceRequest, mockServiceResponse, "groups", items);
  }

  /**
   * GET /api/admin/groups/{id}
   */
  private void getGroup(MockServiceResponse mockServiceResponse, String id) {
    LibreChatGroup group = findGroup(id);
    if (group == null) {
      json(mockServiceResponse, 404, error("Group not found"));
      return;
    }
    json(mockServiceResponse, 200, groupWrapper(group));
  }

  /**
   * POST /api/admin/groups {name, description, source, idOnTheSource}.  LibreChat enforces a unique
   * (idOnTheSource, source) index, so a duplicate is a 400 here too.
   */
  private void createGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    JsonNode body = GrouperUtil.jsonJacksonNode(mockServiceRequest.getRequestBody());
    String name = GrouperUtil.jsonJacksonGetString(body, "name");
    if (StringUtils.isBlank(name)) {
      json(mockServiceResponse, 400, error("name is required"));
      return;
    }
    LibreChatGroup group = new LibreChatGroup();
    group.setId(newObjectId());
    group.setName(name);
    group.setDescription(GrouperUtil.jsonJacksonGetString(body, "description"));
    group.setSource(StringUtils.defaultIfBlank(GrouperUtil.jsonJacksonGetString(body, "source"), "local"));
    group.setIdOnTheSource(GrouperUtil.jsonJacksonGetString(body, "idOnTheSource"));
    if (!StringUtils.isBlank(group.getIdOnTheSource())) {
      long count = new GcDbAccess().sql("select count(*) from mock_librechat_group where id_on_the_source = ? and source = ?")
          .addBindVar(group.getIdOnTheSource()).addBindVar(group.getSource()).select(long.class);
      if (count > 0) {
        json(mockServiceResponse, 400, error("A group with this idOnTheSource already exists"));
        return;
      }
    }
    HibernateSession.byObjectStatic().save(group);
    json(mockServiceResponse, 201, groupWrapper(group));
  }

  /**
   * PATCH /api/admin/groups/{id} {name, description}
   */
  private void updateGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String id) {
    LibreChatGroup group = findGroup(id);
    if (group == null) {
      json(mockServiceResponse, 404, error("Group not found"));
      return;
    }
    JsonNode body = GrouperUtil.jsonJacksonNode(mockServiceRequest.getRequestBody());
    if (body.has("name")) {
      String name = GrouperUtil.jsonJacksonGetString(body, "name");
      if (StringUtils.isBlank(name)) {
        json(mockServiceResponse, 400, error("name must be a non-empty string"));
        return;
      }
      group.setName(name);
    }
    if (body.has("description")) {
      group.setDescription(GrouperUtil.jsonJacksonGetString(body, "description"));
    }
    HibernateSession.byObjectStatic().update(group);
    json(mockServiceResponse, 200, groupWrapper(group));
  }

  /**
   * DELETE /api/admin/groups/{id}
   */
  private void deleteGroup(MockServiceResponse mockServiceResponse, String id) {
    LibreChatGroup group = findGroup(id);
    if (group == null) {
      json(mockServiceResponse, 404, error("Group not found"));
      return;
    }
    new GcDbAccess().sql("delete from mock_librechat_membership where group_id = ?").addBindVar(id).executeSql();
    HibernateSession.byObjectStatic().delete(group);
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("success", true);
    result.put("id", id);
    json(mockServiceResponse, 200, result);
  }

  // ==================== members ====================

  /**
   * GET /api/admin/groups/{id}/members?limit=&offset=
   */
  private void listMembers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String groupId) {
    if (findGroup(groupId) == null) {
      json(mockServiceResponse, 404, error("Group not found"));
      return;
    }
    List<String> userIds = new GcDbAccess().sql("select user_id from mock_librechat_membership where group_id = ? order by user_id")
        .addBindVar(groupId).selectList(String.class);
    List<ObjectNode> items = new ArrayList<ObjectNode>();
    for (String userId : userIds) {
      LibreChatUser user = findUser(userId);
      ObjectNode member = GrouperUtil.jsonJacksonNode();
      member.put("userId", userId);
      member.put("name", user == null ? userId : StringUtils.defaultString(user.getName()));
      member.put("email", user == null ? "" : StringUtils.defaultString(user.getEmail()));
      items.add(member);
    }
    page(mockServiceRequest, mockServiceResponse, "members", items);
  }

  /**
   * POST /api/admin/groups/{id}/members {userId} -- idempotent
   */
  private void addMember(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String groupId) {
    LibreChatGroup group = findGroup(groupId);
    if (group == null) {
      json(mockServiceResponse, 404, error("Group not found"));
      return;
    }
    String userId = GrouperUtil.jsonJacksonGetString(GrouperUtil.jsonJacksonNode(mockServiceRequest.getRequestBody()), "userId");
    if (!LibreChatApiCommands.isObjectId(userId)) {
      json(mockServiceResponse, 400, error("Only native user ObjectIds can be added via this endpoint"));
      return;
    }
    if (findUser(userId) == null) {
      json(mockServiceResponse, 404, error("User not found"));
      return;
    }
    long count = new GcDbAccess().sql("select count(*) from mock_librechat_membership where group_id = ? and user_id = ?")
        .addBindVar(groupId).addBindVar(userId).select(long.class);
    if (count == 0) {
      LibreChatMembership membership = new LibreChatMembership();
      membership.setId(newObjectId());
      membership.setGroupId(groupId);
      membership.setUserId(userId);
      HibernateSession.byObjectStatic().save(membership);
    }
    json(mockServiceResponse, 200, groupWrapper(group));
  }

  /**
   * DELETE /api/admin/groups/{id}/members/{userId} -- idempotent
   */
  private void removeMember(MockServiceResponse mockServiceResponse, String groupId, String userId) {
    if (findGroup(groupId) == null) {
      json(mockServiceResponse, 404, error("Group not found"));
      return;
    }
    new GcDbAccess().sql("delete from mock_librechat_membership where group_id = ? and user_id = ?")
        .addBindVar(groupId).addBindVar(userId).executeSql();
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("success", true);
    json(mockServiceResponse, 200, result);
  }

  // ==================== helpers ====================

  /**
   * @param id group id
   * @return the group or null
   */
  private static LibreChatGroup findGroup(String id) {
    if (!LibreChatApiCommands.isObjectId(id)) {
      return null;
    }
    return HibernateSession.byHqlStatic().createQuery("from LibreChatGroup where id = :theId")
        .setString("theId", id).uniqueResult(LibreChatGroup.class);
  }

  /**
   * @param id user id
   * @return the user or null
   */
  private static LibreChatUser findUser(String id) {
    if (!LibreChatApiCommands.isObjectId(id)) {
      return null;
    }
    return HibernateSession.byHqlStatic().createQuery("from LibreChatUser where id = :theId")
        .setString("theId", id).uniqueResult(LibreChatUser.class);
  }

  /**
   * respond with one page of items like the admin API's list endpoints
   */
  private static void page(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse,
      String arrayFieldName, List<ObjectNode> allItems) {
    int limit = Math.min(Math.max(1, GrouperUtil.intValue(mockServiceRequest.getHttpServletRequest().getParameter("limit"), 50)), 200);
    int offset = Math.max(0, GrouperUtil.intValue(mockServiceRequest.getHttpServletRequest().getParameter("offset"), 0));
    List<ObjectNode> pageItems = new ArrayList<ObjectNode>();
    for (int i = offset; i < allItems.size() && i < offset + limit; i++) {
      pageItems.add(allItems.get(i));
    }
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set(arrayFieldName, LibreChatApiCommands.toArrayNode(pageItems));
    result.put("total", allItems.size());
    result.put("limit", limit);
    result.put("offset", offset);
    json(mockServiceResponse, 200, result);
  }

  /**
   * @param group group
   * @return {"group": {...}}
   */
  private static ObjectNode groupWrapper(LibreChatGroup group) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("group", group.toJson());
    return result;
  }

  /**
   * @param message error message
   * @return {"error": message}
   */
  private static ObjectNode error(String message) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("error", message);
    return result;
  }

  /**
   * @param mockServiceResponse response
   * @param code http status
   * @param body JSON body
   */
  private static void json(MockServiceResponse mockServiceResponse, int code, JsonNode body) {
    mockServiceResponse.setResponseCode(code);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(body));
  }

  /**
   * @return a new id like a Mongo ObjectId (24 hex characters)
   */
  public static String newObjectId() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < 24; i++) {
      result.append(Character.forDigit(ThreadLocalRandom.current().nextInt(16), 16));
    }
    return result.toString();
  }

}
