package edu.internet2.middleware.grouper.app.assetSonarProvisioning;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Types;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Table;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Mock AssetSonar REST API for offline tests, backed by the mock_asset_sonar_member table.
 *
 * <p>It deliberately reproduces the awkward behaviors of the real API, or the tests would prove
 * nothing:</p>
 * <ul>
 *   <li>auth is a plain {@code token} header</li>
 *   <li>request bodies are form-encoded {@code user[...]} parameters, not JSON</li>
 *   <li>unknown query parameters are ignored, returning the unfiltered (active) page</li>
 *   <li>only {@code filter=email} and {@code filter=status&amp;filter_val=inactive} are honored
 *       ({@code filter_val=0} is ignored like the real one)</li>
 *   <li>the default list excludes status=0 members, though they are readable by id</li>
 *   <li>a create with an existing email (any case) is refused with no id: HTTP 403 for an active
 *       member, but HTTP 200 with {"errors":...,"status":403} for an inactive one</li>
 *   <li>a create without user[role_id] is refused with 400 "Role Id is invalid"</li>
 *   <li>{@code user[external_id]} sets nothing and nulls the email</li>
 *   <li>the member url accepts POST as an update</li>
 *   <li>a write can be set to <b>commit and then answer 502</b> (the production gateway behavior)
 *       for a given email, a set number of times, via the mock_asset_sonar_fault table
 *       ({@link #FAULT_TABLE}); the client must retry and cope with the committed result</li>
 * </ul>
 *
 * <p>Routes under mock name "assetSonar": {@code .../mockServices/assetSonar/members.api} and
 * {@code .../mockServices/assetSonar/members/123.api}.</p>
 */
public class AssetSonarMockServiceHandler extends MockServiceHandler {

  /** page size of the real API */
  public static final int PAGE_SIZE = 25;

  /** never log the token */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("token");

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
   * Create the mock table if it does not exist yet.
   */
  public static void ensureAssetSonarMockTables() {
    // each table is checked on its own so a database created before the fault table gets it added
    for (String tableName : new String[] {"mock_asset_sonar_member", FAULT_TABLE}) {
      try {
        new GcDbAccess().sql("select count(*) from " + tableName).select(int.class);
      } catch (Exception e) {
        GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
          @Override
          public void changeDatabase(DdlVersionBean ddlVersionBean) {
            Database database = ddlVersionBean.getDatabase();
            AssetSonarMember.createTableAssetSonarMember(ddlVersionBean, database);
            createTableAssetSonarFault(database);
          }
        });
      }
    }
  }

  /**
   * mock-only table: email (lowercase) and how many more successful writes for that email should
   * be answered with 502 after committing
   */
  public static final String FAULT_TABLE = "mock_asset_sonar_fault";

  /**
   * @param database ddlutils database
   */
  private static void createTableAssetSonarFault(Database database) {
    try {
      new GcDbAccess().sql("select count(*) from " + FAULT_TABLE).select(int.class);
    } catch (Exception e) {
      Table table = GrouperDdlUtils.ddlutilsFindOrCreateTable(database, FAULT_TABLE);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "email", Types.VARCHAR, "256", true, true);
      GrouperDdlUtils.ddlutilsFindOrCreateColumn(table, "remaining_502", Types.INTEGER, "10", false, true);
    }
  }

  /**
   * If a 502 is scheduled for this email, use one up.  Called only after a write committed, so the
   * caller sees the production failure mode: the change is applied but the response is lost.
   * @param email the email of the written member
   * @return true if this response should be a 502
   */
  private static boolean consumeScheduled502(String email) {
    if (StringUtils.isBlank(email)) {
      return false;
    }
    String key = email.trim().toLowerCase();
    Integer remaining = new GcDbAccess().sql("select remaining_502 from " + FAULT_TABLE + " where email = ?")
        .addBindVar(key).select(Integer.class);
    if (remaining == null || remaining <= 0) {
      return false;
    }
    new GcDbAccess().sql("update " + FAULT_TABLE + " set remaining_502 = ? where email = ?")
        .addBindVar(remaining - 1).addBindVar(key).executeSql();
    return true;
  }

  /**
   * Replace a committed write's success response with the gateway's 502, if one is scheduled.
   */
  private static void loseResponseIfScheduled(MockServiceResponse mockServiceResponse, Map<String, String> params) {
    // only a write that really succeeded (a 200 with "errors" in the body is a refusal)
    if (mockServiceResponse.getResponseCode() == 200
        && !StringUtils.contains(mockServiceResponse.getResponseBody(), "\"errors\"")
        && consumeScheduled502(params.get("user[email]"))) {
      mockServiceResponse.setResponseCode(502);
      mockServiceResponse.setContentType("text/html");
      mockServiceResponse.setResponseBody("<h1>Incomplete response received from application</h1>");
    }
  }

  /**
   * The token header must equal the accessTokenPassword of the test WsBearerToken external system. This mock
   * runs in the test Tomcat JVM whose config can lag the JUnit JVM's writes, so on a mismatch the
   * config cache is cleared and the check retried once.
   * @param mockServiceRequest the request
   * @return true if authorized
   */
  private boolean authorized(MockServiceRequest mockServiceRequest) {
    String token = mockServiceRequest.getHttpServletRequest().getHeader("token");
    if (StringUtils.isBlank(token)) {
      return false;
    }
    if (tokenMatches(token)) {
      return true;
    }
    ConfigPropertiesCascadeBase.clearCache();
    return tokenMatches(token);
  }

  private boolean tokenMatches(String token) {
    String configId = GrouperConfig.retrieveConfig().propertyValueString(
        "grouperTest.exampleAssetSonar.mockExternalSystem.configId");
    if (StringUtils.isBlank(configId)) {
      return false;
    }
    String apiToken = GrouperLoaderConfig.retrieveConfig().propertyValueString(
        "grouper.wsBearerToken." + configId + ".accessTokenPassword");
    return !StringUtils.isBlank(apiToken) && StringUtils.equals(apiToken, token);
  }

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureAssetSonarMockTables();
      mockTablesThere = true;
    }

    if (!authorized(mockServiceRequest)) {
      // the real API answers a bad token with 401 JSON
      respond(mockServiceResponse, 401, errorJson("base", "Invalid token"));
      return;
    }

    String[] paths = mockServiceRequest.getPostMockNamePaths();
    String httpMethod = mockServiceRequest.getHttpServletRequest().getMethod();
    Map<String, String> params = requestParams(mockServiceRequest);

    // collection url: /members.api
    if (GrouperUtil.length(paths) == 1 && "members.api".equals(paths[0])) {
      if ("GET".equals(httpMethod)) {
        listMembers(mockServiceResponse, params);
        return;
      }
      if ("POST".equals(httpMethod)) {
        createMember(mockServiceResponse, params);
        loseResponseIfScheduled(mockServiceResponse, params);
        return;
      }
    }

    // member url: /members/<id>.api
    if (GrouperUtil.length(paths) == 2 && "members".equals(paths[0]) && paths[1].endsWith(".api")) {
      String memberId = paths[1].substring(0, paths[1].length() - ".api".length());
      if ("GET".equals(httpMethod)) {
        getMember(mockServiceResponse, memberId);
        return;
      }
      // the real member url accepts POST as an update, which is why the two url shapes matter
      if ("PUT".equals(httpMethod) || "POST".equals(httpMethod)) {
        updateMember(mockServiceResponse, memberId, params);
        loseResponseIfScheduled(mockServiceResponse, params);
        return;
      }
    }

    // UI-only and unknown routes: the real API answers 404 (or 406 for HTML-only routes)
    respond(mockServiceResponse, 404, errorJson("base", "Not found"));
  }

  // ==================== reads ====================

  /**
   * GET /members.api -- paged list. Only two filters are honored; everything else is ignored and
   * the default (ACTIVE only) list is returned.
   */
  private void listMembers(MockServiceResponse mockServiceResponse, Map<String, String> params) {
    String filter = params.get("filter");
    String filterVal = params.get("filter_val");

    List<AssetSonarMember> members;
    if ("email".equals(filter) && !StringUtils.isBlank(filterVal)) {
      // finds inactive members too
      members = HibernateSession.byHqlStatic()
          .createQuery("from AssetSonarMember where lower(email) = :theEmail order by id")
          .setString("theEmail", filterVal.trim().toLowerCase()).list(AssetSonarMember.class);
    } else if ("status".equals(filter) && "inactive".equals(filterVal)) {
      members = HibernateSession.byHqlStatic()
          .createQuery("from AssetSonarMember where status = :theStatus order by id")
          .setString("theStatus", AssetSonarMember.STATUS_INACTIVE).list(AssetSonarMember.class);
    } else {
      // default list, which is also what any unrecognized parameter silently gets
      members = HibernateSession.byHqlStatic()
          .createQuery("from AssetSonarMember where status <> :theStatus order by id")
          .setString("theStatus", AssetSonarMember.STATUS_INACTIVE).list(AssetSonarMember.class);
    }

    int page = Math.max(1, GrouperUtil.intValue(params.get("page"), 1));
    int totalPages = (members.size() + PAGE_SIZE - 1) / PAGE_SIZE;

    ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
    int fromIndex = (page - 1) * PAGE_SIZE;
    for (int i = fromIndex; i < Math.min(members.size(), fromIndex + PAGE_SIZE); i++) {
      membersArray.add(toJson(members.get(i)));
    }

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("members", membersArray);
    result.put("total_pages", totalPages);
    respond(mockServiceResponse, 200, result.toString());
  }

  /**
   * GET /members/&lt;id&gt;.api -- a bare member object, inactive members included.
   */
  private void getMember(MockServiceResponse mockServiceResponse, String memberId) {
    AssetSonarMember member = findById(memberId);
    if (member == null) {
      respond(mockServiceResponse, 404, errorJson("base", "Member not found"));
      return;
    }
    respond(mockServiceResponse, 200, toJson(member).toString());
  }

  // ==================== writes ====================

  /**
   * POST /members.api -- create. 403 with no id if the email is taken by ANY member.
   */
  private void createMember(MockServiceResponse mockServiceResponse, Map<String, String> params) {
    String email = params.get("user[email]");
    if (StringUtils.isBlank(email)) {
      respond(mockServiceResponse, 422, errorJson("email", "can't be blank"));
      return;
    }
    if (StringUtils.isBlank(params.get("user[role_id]"))) {
      // the real API checks role_id before the email (a duplicate without role_id gets this 400)
      ObjectNode result = GrouperUtil.jsonJacksonNode();
      result.put("errors", "Role Id is invalid");
      respond(mockServiceResponse, 400, result.toString());
      return;
    }
    AssetSonarMember existing = findByEmail(email);
    if (existing != null) {
      if (existing.isInactive()) {
        // the real API answers an INACTIVE duplicate with HTTP 200 and the 403 inside the body
        ObjectNode result = GrouperUtil.jsonJacksonNode();
        ObjectNode errors = GrouperUtil.jsonJacksonNode();
        errors.set("base", GrouperUtil.jsonJacksonArrayNode().add(AssetSonarApiCommands.EMAIL_TAKEN_MESSAGE
            + " by an Inactive Member. To reactivate, search with the email address and Activate Member"));
        result.set("errors", errors);
        result.put("status", 403);
        respond(mockServiceResponse, 200, result.toString());
      } else {
        respond(mockServiceResponse, 403, errorJson("base", AssetSonarApiCommands.EMAIL_TAKEN_MESSAGE + " by a Member"));
      }
      return;
    }
    AssetSonarMember member = new AssetSonarMember();
    member.setId(String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999999)));
    member.setRoleId("2");
    member.setStatus(AssetSonarMember.STATUS_ACTIVE);
    applyParams(member, params);
    HibernateSession.byObjectStatic().save(member);

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("message", "Member created.");
    result.put("member_id", Long.parseLong(member.getId()));
    respond(mockServiceResponse, 200, result.toString());
  }

  /**
   * PUT /members/&lt;id&gt;.api -- partial update; omitted fields are unchanged.
   */
  private void updateMember(MockServiceResponse mockServiceResponse, String memberId, Map<String, String> params) {
    AssetSonarMember member = findById(memberId);
    if (member == null) {
      respond(mockServiceResponse, 404, errorJson("base", "Member not found"));
      return;
    }
    String newEmail = params.get("user[email]");
    if (!StringUtils.isBlank(newEmail)) {
      AssetSonarMember emailOwner = findByEmail(newEmail);
      if (emailOwner != null && !StringUtils.equals(emailOwner.getId(), memberId)) {
        respond(mockServiceResponse, 403, errorJson("base", AssetSonarApiCommands.EMAIL_TAKEN_MESSAGE + " by a Member"));
        return;
      }
    }
    applyParams(member, params);
    HibernateSession.byObjectStatic().update(member);

    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.put("message", "Member updated.");
    respond(mockServiceResponse, 200, result.toString());
  }

  /**
   * Apply user[...] parameters. user[external_id] is the product bug: it sets nothing and nulls
   * the email, applied last so it wins even if user[email] was sent in the same request.
   */
  private static void applyParams(AssetSonarMember member, Map<String, String> params) {
    if (params.containsKey("user[email]")) {
      member.setEmail(params.get("user[email]"));
    }
    if (params.containsKey("user[first_name]")) {
      member.setFirstName(params.get("user[first_name]"));
    }
    if (params.containsKey("user[last_name]")) {
      member.setLastName(params.get("user[last_name]"));
    }
    if (params.containsKey("user[employee_id]")) {
      member.setEmployeeId(params.get("user[employee_id]"));
    }
    if (params.containsKey("user[employee_identification_number]")) {
      member.setEmployeeIdentificationNumber(params.get("user[employee_identification_number]"));
    }
    if (params.containsKey("user[role_id]")) {
      member.setRoleId(params.get("user[role_id]"));
    }
    if (params.containsKey("user[status]")) {
      member.setStatus(params.get("user[status]"));
    }
    if (params.containsKey("user[external_id]")) {
      member.setEmail(null);
    }
  }

  // ==================== helpers ====================

  /**
   * Query string parameters plus form parameters. For a POST, Tomcat has already parsed the form
   * body into the parameter map; for a PUT it has not, so the raw body is decoded here.
   */
  private static Map<String, String> requestParams(MockServiceRequest mockServiceRequest) {
    Map<String, String> params = new LinkedHashMap<String, String>();
    HttpServletRequest httpServletRequest = mockServiceRequest.getHttpServletRequest();
    for (Object nameObject : httpServletRequest.getParameterMap().keySet()) {
      String name = (String) nameObject;
      params.put(name, httpServletRequest.getParameter(name));
    }
    String body = mockServiceRequest.getRequestBody();
    if (!StringUtils.isBlank(body)) {
      if (body.trim().startsWith("{")) {
        // the real API does not take JSON bodies; make that obvious in a test
        throw new RuntimeException("AssetSonar mock expects a form-encoded body, not JSON: " + StringUtils.abbreviate(body, 200));
      }
      for (String pair : body.split("&")) {
        if (StringUtils.isBlank(pair)) {
          continue;
        }
        int equalsIndex = pair.indexOf('=');
        String name = urlDecode(equalsIndex < 0 ? pair : pair.substring(0, equalsIndex));
        String value = equalsIndex < 0 ? "" : urlDecode(pair.substring(equalsIndex + 1));
        params.put(name, value);
      }
    }
    return params;
  }

  private static String urlDecode(String value) {
    try {
      return URLDecoder.decode(value, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private static AssetSonarMember findById(String memberId) {
    if (StringUtils.isBlank(memberId)) {
      return null;
    }
    return HibernateSession.byHqlStatic()
        .createQuery("from AssetSonarMember where id = :theId")
        .setString("theId", memberId).uniqueResult(AssetSonarMember.class);
  }

  private static AssetSonarMember findByEmail(String email) {
    List<AssetSonarMember> members = HibernateSession.byHqlStatic()
        .createQuery("from AssetSonarMember where lower(email) = :theEmail")
        .setString("theEmail", email.trim().toLowerCase()).list(AssetSonarMember.class);
    return members.isEmpty() ? null : members.get(0);
  }

  /**
   * Member JSON as the real API renders it: numeric id, role_id and status; null for unset.
   */
  private static ObjectNode toJson(AssetSonarMember member) {
    ObjectNode node = GrouperUtil.jsonJacksonNode();
    node.put("id", Long.parseLong(member.getId()));
    node.put("email", member.getEmail());
    node.put("first_name", member.getFirstName());
    node.put("last_name", member.getLastName());
    node.put("employee_id", member.getEmployeeId());
    node.put("employee_identification_number", member.getEmployeeIdentificationNumber());
    if (StringUtils.isBlank(member.getRoleId())) {
      node.putNull("role_id");
    } else {
      node.put("role_id", Long.parseLong(member.getRoleId()));
    }
    node.put("status", GrouperUtil.intValue(member.getStatus(), 1));
    // never populated by REST in the real API (only by its LDAP integration)
    node.putNull("external_id");
    node.put("team_id", "");
    return node;
  }

  private static String errorJson(String field, String message) {
    ObjectNode errors = GrouperUtil.jsonJacksonNode();
    ArrayNode messages = GrouperUtil.jsonJacksonArrayNode();
    messages.add(message);
    errors.set(field, messages);
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    result.set("errors", errors);
    return result.toString();
  }

  private static void respond(MockServiceResponse mockServiceResponse, int code, String json) {
    mockServiceResponse.setResponseCode(code);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(json);
  }

  /** for tests: members in the mock table, regardless of status */
  public static List<AssetSonarMember> allMockMembers() {
    return new ArrayList<AssetSonarMember>(HibernateSession.byHqlStatic()
        .createQuery("from AssetSonarMember order by id").list(AssetSonarMember.class));
  }

}
