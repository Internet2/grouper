package edu.internet2.middleware.grouper.app.smartsheet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

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
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Mock Smartsheet API 2.0 for offline tests. Simulates the user, group, and group member endpoints
 * against the mock_smartsheet_* tables, speaking JSON in and out like the real API: list calls are
 * paged ({pageNumber, pageSize, totalPages, totalCount, data}), writes return
 * {message, resultCode, result}, and errors return {errorCode, message, refId} with the documented
 * Smartsheet error codes.
 *
 * <p>Routes under mock name "smartsheet", e.g. {@code .../smartsheet/2.0/users?email=jdoe@x.edu}.
 * Auth is the static Bearer token configured as accessTokenPassword on the external system.</p>
 *
 * <p>Simplifications: POST /users always adds the user directly as ACTIVE (as if user auto
 * provisioning matched) with a CONTRIBUTOR seat, and adding a group member whose email is not an org
 * user returns 404 errorCode 1020 (user not found). Like the real API, seatType and isInternal are
 * only returned when a planId is passed, and only then are users outside the org (is_internal F)
 * listed. Upgrade / downgrade follow the documented seat rules.</p>
 */
public class SmartsheetMockServiceHandler extends MockServiceHandler {

  public SmartsheetMockServiceHandler() {
  }

  /** the bearer token header carries the secret, so never log it */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  @Override
  public Set<String> doNotLogParameters() {
    return null;
  }

  /** cheap process-local flag so the mock tables are only ensured once per JVM */
  private static boolean mockTablesThere = false;

  /**
   * Create the mock_smartsheet_* tables if they do not exist yet.
   */
  public static void ensureSmartsheetMockTables() {
    try {
      // newest column, so tables from an older version get it added
      new GcDbAccess().sql("select count(seat_type) from mock_smartsheet_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_smartsheet_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_smartsheet_membership").select(int.class);
    } catch (Exception e) {
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          SmartsheetUser.createTableSmartsheetUser(ddlVersionBean, database);
          SmartsheetGroup.createTableSmartsheetGroup(ddlVersionBean, database);
          SmartsheetMembership.createTableSmartsheetMembership(ddlVersionBean, database);
        }
      });
    }
  }

  /**
   * Validate the Bearer token against the configured accessTokenPassword. The mock runs in the
   * test Tomcat JVM, whose config cache can be stale relative to the JUnit JVM that wrote it, so on
   * a mismatch the cache is cleared and the check retried once.
   * @param mockServiceRequest the request
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
   * @param bearerToken the token from the Authorization header
   * @return true if it equals the configured accessTokenPassword for the mock external system
   */
  private boolean bearerTokenMatches(String bearerToken) {
    String configId = GrouperConfig.retrieveConfig().propertyValueString(
        "grouperTest.exampleSmartsheet.mockExternalSystem.configId");
    if (StringUtils.isBlank(configId)) {
      return false;
    }
    String accessTokenPassword = GrouperLoaderConfig.retrieveConfig()
        .propertyValueString("grouper.wsBearerToken." + configId + ".accessTokenPassword");
    return !StringUtils.isBlank(accessTokenPassword) && StringUtils.equals(accessTokenPassword, bearerToken);
  }

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureSmartsheetMockTables();
      mockTablesThere = true;
    }

    try {
      checkAuthorization(mockServiceRequest);
    } catch (RuntimeException e) {
      error(mockServiceResponse, 401, 1002, "Your Access Token is invalid.");
      return;
    }

    // e.g. ["2.0","users"], ["2.0","users","123","deactivate"], ["2.0","groups","5","members","9"]
    String[] paths = mockServiceRequest.getPostMockNamePaths();
    if (GrouperUtil.length(paths) < 2 || !"2.0".equals(paths[0])) {
      throw new RuntimeException("Smartsheet mock expects /2.0/...");
    }
    String resource = paths[1];
    String id = paths.length >= 3 ? paths[2] : null;
    String sub = paths.length >= 4 ? paths[3] : null;
    String subId = paths.length >= 5 ? paths[4] : null;
    String subAction = paths.length >= 6 ? paths[5] : null;

    String httpMethod = mockServiceRequest.getHttpServletRequest().getMethod();

    if ("users".equals(resource)) {
      if ("GET".equals(httpMethod) && id == null) {
        listUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("GET".equals(httpMethod) && sub == null) {
        getUser(mockServiceResponse, id);
        return;
      }
      if ("POST".equals(httpMethod) && id == null) {
        createUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("PUT".equals(httpMethod) && sub == null) {
        updateUser(mockServiceRequest, mockServiceResponse, id);
        return;
      }
      if ("POST".equals(httpMethod) && ("deactivate".equals(sub) || "reactivate".equals(sub))) {
        setUserStatus(mockServiceResponse, id, "deactivate".equals(sub));
        return;
      }
      if ("DELETE".equals(httpMethod) && sub == null) {
        removeUser(mockServiceResponse, id);
        return;
      }
      if ("POST".equals(httpMethod) && "plans".equals(sub) && ("upgrade".equals(subAction) || "downgrade".equals(subAction))) {
        changeSeatType(mockServiceRequest, mockServiceResponse, id, "upgrade".equals(subAction));
        return;
      }
    } else if ("groups".equals(resource)) {
      if ("GET".equals(httpMethod) && id == null) {
        listGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("GET".equals(httpMethod) && sub == null) {
        getGroup(mockServiceResponse, id);
        return;
      }
      if ("POST".equals(httpMethod) && id == null) {
        createGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("PUT".equals(httpMethod) && sub == null) {
        updateGroup(mockServiceRequest, mockServiceResponse, id);
        return;
      }
      if ("DELETE".equals(httpMethod) && sub == null) {
        deleteGroup(mockServiceResponse, id);
        return;
      }
      if ("POST".equals(httpMethod) && "members".equals(sub)) {
        addGroupMembers(mockServiceRequest, mockServiceResponse, id);
        return;
      }
      if ("DELETE".equals(httpMethod) && "members".equals(sub) && subId != null) {
        removeGroupMember(mockServiceResponse, id, subId);
        return;
      }
    }

    throw new RuntimeException("Unsupported Smartsheet mock request: " + httpMethod + " "
        + mockServiceRequest.getPostMockNamePath());
  }

  // ==================== users ====================

  /**
   * GET /2.0/users -- paged list, optionally filtered by a comma-separated email list.
   */
  private void listUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    List<SmartsheetUser> users = HibernateSession.byHqlStatic()
        .createQuery("from SmartsheetUser order by email").list(SmartsheetUser.class);

    // without a planId, users outside the org are not listed
    boolean hasPlanId = !StringUtils.isBlank(mockServiceRequest.getHttpServletRequest().getParameter("planId"));
    if (!hasPlanId) {
      List<SmartsheetUser> internalUsers = new ArrayList<SmartsheetUser>();
      for (SmartsheetUser user : users) {
        if (!user.isExternal()) {
          internalUsers.add(user);
        }
      }
      users = internalUsers;
    }

    String emailFilter = mockServiceRequest.getHttpServletRequest().getParameter("email");
    if (!StringUtils.isBlank(emailFilter)) {
      Set<String> emails = SmartsheetApiCommands.parseIgnoreSet(emailFilter);
      List<SmartsheetUser> filtered = new ArrayList<SmartsheetUser>();
      for (SmartsheetUser user : users) {
        if (SmartsheetApiCommands.isIgnored(user.getEmail(), emails)) {
          filtered.add(user);
        }
      }
      users = filtered;
    }

    ArrayNode data = GrouperUtil.jsonJacksonArrayNode();
    ObjectNode result = page(mockServiceRequest, users.size(), data);
    int[] range = pageRange(result, users.size());
    for (int i = range[0]; i < range[1]; i++) {
      ObjectNode userNode = userToJson(users.get(i));
      if (hasPlanId) {
        userNode.put("seatType", GrouperUtil.defaultIfBlank(users.get(i).getSeatType(), SmartsheetUser.SEAT_CONTRIBUTOR));
        userNode.put("isInternal", !users.get(i).isExternal());
      }
      data.add(userNode);
    }
    ok(mockServiceResponse, result);
  }

  /**
   * GET /2.0/users/{userId}
   */
  private void getUser(MockServiceResponse mockServiceResponse, String userId) {
    SmartsheetUser user = findUserById(userId);
    if (user == null) {
      error(mockServiceResponse, 404, 1020, "User not found.");
      return;
    }
    ok(mockServiceResponse, userToJson(user));
  }

  /**
   * POST /2.0/users -- add a user to the org (as ACTIVE, as if auto provisioning matched).
   */
  private void createUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    JsonNode body = parseBody(mockServiceRequest);
    String email = GrouperUtil.jsonJacksonGetString(body, "email");
    if (StringUtils.isBlank(email) || !email.contains("@")) {
      error(mockServiceResponse, 400, 1156, "Invalid email address.");
      return;
    }
    if (findUserByEmail(email) != null) {
      error(mockServiceResponse, 403, 1016, "The user is already a member of your account.");
      return;
    }
    SmartsheetUser user = new SmartsheetUser();
    user.setId(newNumericId());
    user.setEmail(email);
    user.setFirstName(GrouperUtil.jsonJacksonGetString(body, "firstName"));
    user.setLastName(GrouperUtil.jsonJacksonGetString(body, "lastName"));
    user.setStatus(SmartsheetUser.STATUS_ACTIVE);
    user.setAdmin(GrouperUtil.jsonJacksonGetBoolean(body, "admin", false));
    user.setLicensedSheetCreator(GrouperUtil.jsonJacksonGetBoolean(body, "licensedSheetCreator", false));
    user.setGroupAdmin(GrouperUtil.jsonJacksonGetBoolean(body, "groupAdmin", false));
    user.setResourceViewer(GrouperUtil.jsonJacksonGetBoolean(body, "resourceViewer", false));
    user.setSeatType(SmartsheetUser.SEAT_CONTRIBUTOR);
    user.setInternal(true);
    HibernateSession.byObjectStatic().save(user);
    okResult(mockServiceResponse, userToJson(user));
  }

  /**
   * PUT /2.0/users/{userId} -- partial update of names and account flags.
   */
  private void updateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String userId) {
    SmartsheetUser user = findUserById(userId);
    if (user == null) {
      error(mockServiceResponse, 404, 1020, "User not found.");
      return;
    }
    JsonNode body = parseBody(mockServiceRequest);
    if (body.has("firstName")) {
      user.setFirstName(GrouperUtil.jsonJacksonGetString(body, "firstName"));
    }
    if (body.has("lastName")) {
      user.setLastName(GrouperUtil.jsonJacksonGetString(body, "lastName"));
    }
    if (body.has("admin")) {
      user.setAdmin(GrouperUtil.jsonJacksonGetBoolean(body, "admin"));
    }
    if (body.has("licensedSheetCreator")) {
      user.setLicensedSheetCreator(GrouperUtil.jsonJacksonGetBoolean(body, "licensedSheetCreator"));
    }
    if (body.has("groupAdmin")) {
      user.setGroupAdmin(GrouperUtil.jsonJacksonGetBoolean(body, "groupAdmin"));
    }
    if (body.has("resourceViewer")) {
      user.setResourceViewer(GrouperUtil.jsonJacksonGetBoolean(body, "resourceViewer"));
    }
    HibernateSession.byObjectStatic().update(user);
    okResult(mockServiceResponse, userToJson(user));
  }

  /**
   * POST /2.0/users/{userId}/deactivate or /reactivate
   */
  private void setUserStatus(MockServiceResponse mockServiceResponse, String userId, boolean deactivate) {
    SmartsheetUser user = findUserById(userId);
    if (user == null) {
      error(mockServiceResponse, 403, deactivate ? 1335 : 1354, "The user is not a member of your account.");
      return;
    }
    user.setStatus(deactivate ? SmartsheetUser.STATUS_DEACTIVATED : SmartsheetUser.STATUS_ACTIVE);
    HibernateSession.byObjectStatic().update(user);
    okResult(mockServiceResponse, null);
  }

  /**
   * DELETE /2.0/users/{userId} -- remove from the org, and from all groups.
   */
  private void removeUser(MockServiceResponse mockServiceResponse, String userId) {
    SmartsheetUser user = findUserById(userId);
    if (user == null) {
      error(mockServiceResponse, 404, 1020, "User not found.");
      return;
    }
    new GcDbAccess().sql("delete from mock_smartsheet_membership where user_id = ?").addBindVar(userId).executeSql();
    HibernateSession.byObjectStatic().delete(user);
    okResult(mockServiceResponse, null);
  }

  /**
   * POST /2.0/users/{userId}/plans/{planId}/upgrade or /downgrade with {"seatType": ...}. Upgrade
   * takes MEMBER, or GUEST (external users only); upgrading to the current seat is 200. Downgrade
   * takes CONTRIBUTOR or GUEST, and is refused for a user already CONTRIBUTOR / VIEWER. Deactivated
   * users are refused (reactivate first).
   */
  private void changeSeatType(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse,
      String userId, boolean upgrade) {
    SmartsheetUser user = findUserById(userId);
    if (user == null) {
      error(mockServiceResponse, 404, 1020, "User not found.");
      return;
    }
    if (user.isDeactivated()) {
      error(mockServiceResponse, 400, 1008, "Unable to change the seat type of a deactivated user. Reactivate the user first.");
      return;
    }
    String seatType = SmartsheetUser.normalizeSeatType(GrouperUtil.jsonJacksonGetString(parseBody(mockServiceRequest), "seatType"));
    String current = GrouperUtil.defaultIfBlank(SmartsheetUser.normalizeSeatType(user.getSeatType()), SmartsheetUser.SEAT_CONTRIBUTOR);

    if (SmartsheetUser.SEAT_GUEST.equals(seatType) && !user.isExternal()) {
      error(mockServiceResponse, 400, 1008, "GUEST is only available for external users.");
      return;
    }
    if (upgrade) {
      if (!SmartsheetUser.SEAT_MEMBER.equals(seatType) && !SmartsheetUser.SEAT_GUEST.equals(seatType)) {
        error(mockServiceResponse, 400, 1008, "Invalid seatType for upgrade: " + seatType);
        return;
      }
      if (SmartsheetUser.SEAT_GUEST.equals(seatType) && !SmartsheetUser.SEAT_CONTRIBUTOR.equals(current)
          && !SmartsheetUser.SEAT_GUEST.equals(current)) {
        error(mockServiceResponse, 400, 1008, "Cannot upgrade " + current + " to GUEST.");
        return;
      }
    } else {
      if (!SmartsheetUser.SEAT_CONTRIBUTOR.equals(seatType) && !SmartsheetUser.SEAT_GUEST.equals(seatType)) {
        error(mockServiceResponse, 400, 1008, "Invalid seatType for downgrade: " + seatType);
        return;
      }
      if (SmartsheetUser.SEAT_CONTRIBUTOR.equals(current)) {
        error(mockServiceResponse, 400, 1008, "The user's seat type is not eligible for a downgrade.");
        return;
      }
    }
    user.setSeatType(seatType);
    HibernateSession.byObjectStatic().update(user);
    okResult(mockServiceResponse, null);
  }

  // ==================== groups ====================

  /**
   * GET /2.0/groups -- paged list, no members.
   */
  private void listGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    List<SmartsheetGroup> groups = HibernateSession.byHqlStatic()
        .createQuery("from SmartsheetGroup order by name").list(SmartsheetGroup.class);

    ArrayNode data = GrouperUtil.jsonJacksonArrayNode();
    ObjectNode result = page(mockServiceRequest, groups.size(), data);
    int[] range = pageRange(result, groups.size());
    for (int i = range[0]; i < range[1]; i++) {
      data.add(groupToJson(groups.get(i), false));
    }
    ok(mockServiceResponse, result);
  }

  /**
   * GET /2.0/groups/{groupId} -- one group with members.
   */
  private void getGroup(MockServiceResponse mockServiceResponse, String groupId) {
    SmartsheetGroup group = findGroupById(groupId);
    if (group == null) {
      error(mockServiceResponse, 404, 1106, "Group not found.");
      return;
    }
    ok(mockServiceResponse, groupToJson(group, true));
  }

  /**
   * POST /2.0/groups -- create; names are unique (case-insensitive) in the org.
   */
  private void createGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    JsonNode body = parseBody(mockServiceRequest);
    String name = GrouperUtil.jsonJacksonGetString(body, "name");
    if (StringUtils.isBlank(name)) {
      error(mockServiceResponse, 400, 1012, "Required object attribute(s) are missing from your request: group.name.");
      return;
    }
    if (findGroupByName(name) != null) {
      error(mockServiceResponse, 400, 1103, "A group with the specified name already exists.");
      return;
    }
    SmartsheetGroup group = new SmartsheetGroup();
    group.setId(newNumericId());
    group.setName(name);
    group.setDescription(GrouperUtil.jsonJacksonGetString(body, "description"));
    group.setOwner("owner@example.edu");
    group.setOwnerId("1");
    HibernateSession.byObjectStatic().save(group);
    okResult(mockServiceResponse, groupToJson(group, false));
  }

  /**
   * PUT /2.0/groups/{groupId} -- partial update of name / description.
   */
  private void updateGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String groupId) {
    SmartsheetGroup group = findGroupById(groupId);
    if (group == null) {
      error(mockServiceResponse, 404, 1106, "Group not found.");
      return;
    }
    JsonNode body = parseBody(mockServiceRequest);
    if (body.has("name")) {
      String name = GrouperUtil.jsonJacksonGetString(body, "name");
      SmartsheetGroup sameName = findGroupByName(name);
      if (sameName != null && !StringUtils.equals(sameName.getId(), groupId)) {
        error(mockServiceResponse, 400, 1103, "A group with the specified name already exists.");
        return;
      }
      group.setName(name);
    }
    if (body.has("description")) {
      group.setDescription(GrouperUtil.jsonJacksonGetString(body, "description"));
    }
    HibernateSession.byObjectStatic().update(group);
    okResult(mockServiceResponse, groupToJson(group, false));
  }

  /**
   * DELETE /2.0/groups/{groupId} -- delete with its memberships.
   */
  private void deleteGroup(MockServiceResponse mockServiceResponse, String groupId) {
    SmartsheetGroup group = findGroupById(groupId);
    if (group == null) {
      error(mockServiceResponse, 404, 1106, "Group not found.");
      return;
    }
    new GcDbAccess().sql("delete from mock_smartsheet_membership where group_id = ?").addBindVar(groupId).executeSql();
    HibernateSession.byObjectStatic().delete(group);
    okResult(mockServiceResponse, null);
  }

  /**
   * POST /2.0/groups/{groupId}/members -- a member object or an array of them (by email or id). In
   * an array, existing members are skipped; a single existing member is error 1129.
   */
  private void addGroupMembers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String groupId) {
    SmartsheetGroup group = findGroupById(groupId);
    if (group == null) {
      error(mockServiceResponse, 404, 1106, "Group not found.");
      return;
    }
    JsonNode body = parseBody(mockServiceRequest);
    boolean isArray = body.isArray();
    List<JsonNode> memberNodes = new ArrayList<JsonNode>();
    if (isArray) {
      for (int i = 0; i < body.size(); i++) {
        memberNodes.add(body.get(i));
      }
    } else {
      memberNodes.add(body);
    }

    // resolve everything first so a bad member fails the call without a partial write
    List<SmartsheetUser> users = new ArrayList<SmartsheetUser>();
    for (JsonNode memberNode : memberNodes) {
      String email = GrouperUtil.jsonJacksonGetString(memberNode, "email");
      String id = GrouperUtil.jsonJacksonGetString(memberNode, "id");
      SmartsheetUser user = !StringUtils.isBlank(id) ? findUserById(id) : findUserByEmail(email);
      if (user == null) {
        error(mockServiceResponse, 404, 1020, "User not found: " + StringUtils.defaultString(email, id));
        return;
      }
      users.add(user);
    }

    ArrayNode added = GrouperUtil.jsonJacksonArrayNode();
    for (SmartsheetUser user : users) {
      if (isMember(groupId, user.getId())) {
        if (!isArray) {
          error(mockServiceResponse, 400, 1129, "The resource you tried to create already exists.");
          return;
        }
        continue;
      }
      SmartsheetMembership membership = new SmartsheetMembership();
      membership.setId(GrouperUuid.getUuid());
      membership.setGroupId(groupId);
      membership.setUserId(user.getId());
      HibernateSession.byObjectStatic().save(membership);
      added.add(memberToJson(user));
    }
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("message", "SUCCESS");
    result.put("resultCode", 0);
    result.set("result", added);
    writeJson(mockServiceResponse, 200, result);
  }

  /**
   * DELETE /2.0/groups/{groupId}/members/{userId}
   */
  private void removeGroupMember(MockServiceResponse mockServiceResponse, String groupId, String userId) {
    if (findGroupById(groupId) == null) {
      error(mockServiceResponse, 404, 1106, "Group not found.");
      return;
    }
    if (!isMember(groupId, userId)) {
      error(mockServiceResponse, 404, 1006, "Not Found");
      return;
    }
    new GcDbAccess().sql("delete from mock_smartsheet_membership where group_id = ? and user_id = ?")
        .addBindVar(groupId).addBindVar(userId).executeSql();
    okResult(mockServiceResponse, null);
  }

  // ==================== helpers ====================

  private SmartsheetUser findUserById(String userId) {
    if (StringUtils.isBlank(userId)) {
      return null;
    }
    return HibernateSession.byHqlStatic().createQuery("from SmartsheetUser where id = :theId")
        .setString("theId", userId).uniqueResult(SmartsheetUser.class);
  }

  private SmartsheetUser findUserByEmail(String email) {
    if (StringUtils.isBlank(email)) {
      return null;
    }
    return HibernateSession.byHqlStatic().createQuery("from SmartsheetUser where lower(email) = :theEmail")
        .setString("theEmail", email.trim().toLowerCase()).uniqueResult(SmartsheetUser.class);
  }

  private SmartsheetGroup findGroupById(String groupId) {
    if (StringUtils.isBlank(groupId)) {
      return null;
    }
    return HibernateSession.byHqlStatic().createQuery("from SmartsheetGroup where id = :theId")
        .setString("theId", groupId).uniqueResult(SmartsheetGroup.class);
  }

  private SmartsheetGroup findGroupByName(String name) {
    if (StringUtils.isBlank(name)) {
      return null;
    }
    return HibernateSession.byHqlStatic().createQuery("from SmartsheetGroup where lower(name) = :theName")
        .setString("theName", name.toLowerCase()).uniqueResult(SmartsheetGroup.class);
  }

  private boolean isMember(String groupId, String userId) {
    return new GcDbAccess().sql("select count(1) from mock_smartsheet_membership where group_id = ? and user_id = ?")
        .addBindVar(groupId).addBindVar(userId).select(int.class) > 0;
  }

  private static ObjectNode userToJson(SmartsheetUser user) {
    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.put("id", Long.parseLong(user.getId()));
    node.put("email", user.getEmail());
    if (user.getFirstName() != null) {
      node.put("firstName", user.getFirstName());
    }
    if (user.getLastName() != null) {
      node.put("lastName", user.getLastName());
    }
    String name = StringUtils.trimToNull(StringUtils.defaultString(user.getFirstName()) + " "
        + StringUtils.defaultString(user.getLastName()));
    if (name != null) {
      node.put("name", name);
    }
    node.put("status", GrouperUtil.defaultIfBlank(user.getStatus(), SmartsheetUser.STATUS_ACTIVE));
    node.put("admin", user.getAdmin() != null && user.getAdmin());
    node.put("licensedSheetCreator", user.getLicensedSheetCreator() != null && user.getLicensedSheetCreator());
    node.put("groupAdmin", user.getGroupAdmin() != null && user.getGroupAdmin());
    node.put("resourceViewer", user.getResourceViewer() != null && user.getResourceViewer());
    // like the real API, a profile image (not something MCP passes through)
    ObjectNode profileImage = GrouperUtil.jsonJacksonNode();
    profileImage.put("imageId", "img-" + user.getId());
    node.set("profileImage", profileImage);
    return node;
  }

  private static ObjectNode memberToJson(SmartsheetUser user) {
    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.put("id", Long.parseLong(user.getId()));
    node.put("email", user.getEmail());
    if (user.getFirstName() != null) {
      node.put("firstName", user.getFirstName());
    }
    if (user.getLastName() != null) {
      node.put("lastName", user.getLastName());
    }
    return node;
  }

  private ObjectNode groupToJson(SmartsheetGroup group, boolean includeMembers) {
    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.put("id", Long.parseLong(group.getId()));
    node.put("name", group.getName());
    if (group.getDescription() != null) {
      node.put("description", group.getDescription());
    }
    if (group.getOwner() != null) {
      node.put("owner", group.getOwner());
    }
    if (group.getOwnerId() != null) {
      node.put("ownerId", Long.parseLong(group.getOwnerId()));
    }
    if (includeMembers) {
      List<SmartsheetMembership> memberships = HibernateSession.byHqlStatic()
          .createQuery("from SmartsheetMembership where groupId = :theGroupId order by userId")
          .setString("theGroupId", group.getId()).list(SmartsheetMembership.class);
      ArrayNode members = GrouperUtil.jsonJacksonArrayNode();
      for (SmartsheetMembership membership : memberships) {
        SmartsheetUser user = findUserById(membership.getUserId());
        if (user != null) {
          members.add(memberToJson(user));
        }
      }
      node.set("members", members);
    }
    return node;
  }

  /**
   * Build the paging envelope for a list call from the page / pageSize / includeAll params.
   * @return the envelope with data set (caller fills data for the {@link #pageRange})
   */
  private static ObjectNode page(MockServiceRequest mockServiceRequest, int totalCount, ArrayNode data) {
    boolean includeAll = GrouperUtil.booleanValue(mockServiceRequest.getHttpServletRequest().getParameter("includeAll"), false);
    int pageSize = includeAll ? Math.max(1, totalCount)
        : GrouperUtil.intValue(mockServiceRequest.getHttpServletRequest().getParameter("pageSize"), 100);
    int pageNumber = includeAll ? 1
        : GrouperUtil.intValue(mockServiceRequest.getHttpServletRequest().getParameter("page"), 1);
    int totalPages = Math.max(1, (totalCount + pageSize - 1) / pageSize);

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("pageNumber", pageNumber);
    if (!includeAll) {
      result.put("pageSize", pageSize);
    }
    result.put("totalPages", totalPages);
    result.put("totalCount", totalCount);
    result.set("data", data);
    return result;
  }

  /**
   * @return {fromIndexInclusive, toIndexExclusive} for the page described by the envelope
   */
  private static int[] pageRange(ObjectNode envelope, int totalCount) {
    int pageNumber = envelope.get("pageNumber").asInt();
    int pageSize = envelope.has("pageSize") ? envelope.get("pageSize").asInt() : Math.max(1, totalCount);
    int from = Math.min(totalCount, Math.max(0, (pageNumber - 1) * pageSize));
    int to = Math.min(totalCount, from + pageSize);
    return new int[] {from, to};
  }

  private JsonNode parseBody(MockServiceRequest mockServiceRequest) {
    String body = mockServiceRequest.getRequestBody();
    if (StringUtils.isBlank(body)) {
      throw new RuntimeException("Request body is required");
    }
    return GrouperUtil.jsonJacksonNode(body);
  }

  private static String newNumericId() {
    // Smartsheet ids are large longs, keep the mock in that shape
    return String.valueOf(ThreadLocalRandom.current().nextLong(1000000000000000L, 9000000000000000L));
  }

  private static void ok(MockServiceResponse mockServiceResponse, JsonNode node) {
    writeJson(mockServiceResponse, 200, node);
  }

  /**
   * write the {message, resultCode, result} envelope used by write calls
   */
  private static void okResult(MockServiceResponse mockServiceResponse, JsonNode resultNode) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("message", "SUCCESS");
    result.put("resultCode", 0);
    if (resultNode != null) {
      result.set("result", resultNode);
    }
    writeJson(mockServiceResponse, 200, result);
  }

  private static void error(MockServiceResponse mockServiceResponse, int httpCode, int errorCode, String message) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("errorCode", errorCode);
    result.put("message", message);
    result.put("refId", GrouperUuid.getUuid());
    writeJson(mockServiceResponse, httpCode, result);
  }

  private static void writeJson(MockServiceResponse mockServiceResponse, int httpCode, JsonNode node) {
    mockServiceResponse.setResponseCode(httpCode);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(node));
  }

}
