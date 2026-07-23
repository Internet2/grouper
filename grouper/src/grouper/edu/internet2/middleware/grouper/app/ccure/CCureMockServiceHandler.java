package edu.internet2.middleware.grouper.app.ccure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

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
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Simulates the CCure Web Service API for tests (URL /grouper/mockServices/ccure/...); requests
 * are dispatched by HTTP method + path. This implementation was developed against the CCure
 * API version 2.9.
 *
 * Personnel and Clearance records have no create endpoint in Grouper's CCure provisioner (people and
 * badges are provisioned outside of Grouper), so those two mock tables (mock_ccure_personnel and
 * mock_ccure_clearance) start empty and must be seeded directly (e.g. via Hibernate) by whatever is
 * testing against this mock.
 *
 * Implemented (the operations the real client, CCureApiCommands, actually calls):
 *   POST /api/Authenticate/Login                    -> login
 *   POST /api/Authenticate/Logout                   -> logout
 *   GET  /api/Objects/Get/{type}/{id}               -> getObject
 *   GET  /api/Objects/GetAll/{type}                 -> getAllObjects
 *   POST /api/Objects/GetAllWithCriteria            -> findObjectsWithCriteria
 *   POST /api/Objects/PersistToContainer            -> persistToContainer
 *   POST /api/Objects/RemoveFromContainer           -> removeFromContainer
 */
public class CCureMockServiceHandler extends MockServiceHandler {

  private static final String PERSONNEL_TYPE = "SoftwareHouse.NextGen.Common.SecurityObjects.Personnel";
  private static final String CLEARANCE_TYPE = "SoftwareHouse.NextGen.Common.SecurityObjects.Clearance";
  private static final String CLEARANCE_PAIR_TYPE = "SoftwareHouse.NextGen.Common.SecurityObjects.PersonnelClearancePair";

  /** sensitive parameters not to log */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("Password");

  /** sensitive headers not to log */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet();

  private static boolean mockTablesThere = false;

  @Override
  public Set<String> doNotLogParameters() {
    return doNotLogParameters;
  }

  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureCcureMockTables();
      mockTablesThere = true;
    }

    String method = mockServiceRequest.getHttpServletRequest().getMethod();
    String[] paths = mockServiceRequest.getPostMockNamePaths();

    if (GrouperUtil.length(paths) >= 3 && StringUtils.equals("api", paths[0])) {

      if (StringUtils.equals("POST", method) && paths.length == 3 && StringUtils.equals("Authenticate", paths[1])) {
        if (StringUtils.equals("Login", paths[2])) {
          login(mockServiceRequest, mockServiceResponse);
          return;
        }
        if (StringUtils.equals("Logout", paths[2])) {
          logout(mockServiceRequest, mockServiceResponse);
          return;
        }
      }

      if (StringUtils.equals("Objects", paths[1])) {
        if (StringUtils.equals("GET", method) && paths.length == 5 && StringUtils.equals("Get", paths[2])) {
          getObject(mockServiceResponse, paths[3], paths[4]);
          return;
        }
        if (StringUtils.equals("GET", method) && paths.length == 4 && StringUtils.equals("GetAll", paths[2])) {
          getAllObjects(mockServiceResponse, paths[3]);
          return;
        }
        if (StringUtils.equals("POST", method) && paths.length == 3 && StringUtils.equals("GetAllWithCriteria", paths[2])) {
          findObjectsWithCriteria(mockServiceRequest, mockServiceResponse);
          return;
        }
        if (StringUtils.equals("POST", method) && paths.length == 3 && StringUtils.equals("PersistToContainer", paths[2])) {
          persistToContainer(mockServiceRequest, mockServiceResponse);
          return;
        }
        if (StringUtils.equals("POST", method) && paths.length == 3 && StringUtils.equals("RemoveFromContainer", paths[2])) {
          removeFromContainer(mockServiceRequest, mockServiceResponse);
          return;
        }
      }
    }

    throw new RuntimeException("Not expecting request: '" + method + "', '"
        + mockServiceRequest.getPostMockNamePath() + "'");
  }

  /**
   * The configId to read credentials from - "grouperTest.ccure.mock.configId" if configured,
   * otherwise the placeholder configId used by CCureExternalSystem.
   * @return the configId
   */
  private static String resolveConfigId() {
    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.ccure.mock.configId");
    if (StringUtils.isBlank(configId)) {
      configId = CCureExternalSystem.CONFIGID_PLACEHOLDER;
    }
    return configId;
  }

  /**
   * POST /api/Authenticate/Login - validate the posted credentials against the myCCure external
   * system config (not hardcoded), and if they match, issue a session-id/token pair.
   */
  private void login(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    HttpServletRequest httpServletRequest = mockServiceRequest.getHttpServletRequest();

    String userName = httpServletRequest.getParameter("UserName");
    String password = httpServletRequest.getParameter("Password");
    String clientName = httpServletRequest.getParameter("ClientName");
    String clientId = httpServletRequest.getParameter("ClientID");
    String clientVersion = httpServletRequest.getParameter("ClientVersion");

    String prefix = CCureExternalSystem.PROPERTY_PREFIX + resolveConfigId() + ".";
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    String expectedUserName = grouperLoaderConfig.propertyValueString(prefix + "username");
    String expectedPassword = grouperLoaderConfig.propertyValueString(prefix + "password");
    String expectedClientName = grouperLoaderConfig.propertyValueString(prefix + "clientName");
    String expectedClientId = grouperLoaderConfig.propertyValueString(prefix + "clientId");
    String expectedClientVersion = grouperLoaderConfig.propertyValueString(prefix + "clientVersion");

    boolean matches = StringUtils.isNotBlank(expectedUserName)
        && StringUtils.equals(userName, expectedUserName)
        && StringUtils.equals(password, expectedPassword)
        && StringUtils.equals(clientName, expectedClientName)
        && (StringUtils.isBlank(expectedClientId) || StringUtils.equals(clientId, expectedClientId))
        && (StringUtils.isBlank(expectedClientVersion) || StringUtils.equals(clientVersion, expectedClientVersion));

    if (!matches) {
      respondError(mockServiceResponse, 401, "User not in system");
      return;
    }

    MockCcureAuth grouperCcureAuth = MockCcureAuth.newSession();
    HibernateSession.byObjectStatic().save(grouperCcureAuth);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.getResponseHeaders().put("session-id", grouperCcureAuth.getSessionId());
    // the real API's login response body is just the token, as a bare JSON string
    mockServiceResponse.setResponseBody("\"" + grouperCcureAuth.getAccessToken() + "\"");
  }

  /**
   * POST /api/Authenticate/Logout - the session-id header and token param must match a session
   * created by login; that session is then removed.
   */
  private void logout(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    String token = mockServiceRequest.getHttpServletRequest().getParameter("token");
    String sessionId = mockServiceRequest.getHttpServletRequest().getHeader("session-id");

    if (StringUtils.isBlank(sessionId)) {
      respondError(mockServiceResponse, 401, "missing session-id header");
      return;
    }

    List<MockCcureAuth> matches = HibernateSession.byHqlStatic()
        .createQuery("from MockCcureAuth where sessionId = :theSessionId and accessToken = :theToken")
        .setString("theSessionId", sessionId)
        .setString("theToken", token)
        .list(MockCcureAuth.class);

    if (GrouperUtil.length(matches) == 0) {
      respondError(mockServiceResponse, 401, "Logout mismatch, token=" + token + ", session-id=" + sessionId);
      return;
    }

    for (MockCcureAuth grouperCcureAuth : matches) {
      HibernateSession.byObjectStatic().delete(grouperCcureAuth);
    }

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("text/plain");
    mockServiceResponse.setResponseBody("success");
  }

  /**
   * GET /api/Objects/Get/{type}/{id} - a single Personnel or Clearance, returned as a
   * single-element array (matching the real API).
   */
  private void getObject(MockServiceResponse mockServiceResponse, String type, String idString) {
    int id = GrouperUtil.intValue(idString, -1);

    if (StringUtils.equals("Personnel", type)) {
      MockCcurePersonnel personnel = findPersonnel(id);
      if (personnel == null) {
        mockServiceResponse.setResponseCode(404);
        return;
      }
      ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
      arrayNode.add(personnel.toJson());
      respondWithJson(mockServiceResponse, arrayNode);
      return;
    }

    if (StringUtils.equals("Clearance", type)) {
      MockCcureClearance clearance = findClearance(id);
      if (clearance == null) {
        mockServiceResponse.setResponseCode(404);
        return;
      }
      ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
      arrayNode.add(clearance.toJson());
      respondWithJson(mockServiceResponse, arrayNode);
      return;
    }

    respondError(mockServiceResponse, 400, "Not handling type " + type);
  }

  /**
   * GET /api/Objects/GetAll/{type} - every Personnel or Clearance record.
   */
  private void getAllObjects(MockServiceResponse mockServiceResponse, String type) {
    ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();

    if (StringUtils.equals("Personnel", type)) {
      List<MockCcurePersonnel> personnelList = HibernateSession.byHqlStatic()
          .createQuery("from MockCcurePersonnel").list(MockCcurePersonnel.class);
      for (MockCcurePersonnel personnel : GrouperUtil.nonNull(personnelList)) {
        arrayNode.add(personnel.toJson());
      }
    } else if (StringUtils.equals("Clearance", type)) {
      List<MockCcureClearance> clearanceList = HibernateSession.byHqlStatic()
          .createQuery("from MockCcureClearance").list(MockCcureClearance.class);
      for (MockCcureClearance clearance : GrouperUtil.nonNull(clearanceList)) {
        arrayNode.add(clearance.toJson());
      }
    } else {
      respondError(mockServiceResponse, 400, "Not handling type '" + type);
      return;
    }

    respondWithJson(mockServiceResponse, arrayNode);
  }

  /**
   * POST /api/Objects/GetAllWithCriteria - paged lookup of Personnel, Clearance, or
   * PersonnelClearancePair, optionally filtered by a simple WhereClause.
   */
  private void findObjectsWithCriteria(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    JsonNode bodyNode = GrouperUtil.jsonJacksonNode(mockServiceRequest.getRequestBody());

    String typeFullName = GrouperUtil.jsonJacksonGetString(bodyNode, "TypeFullName");
    String whereClause = GrouperUtil.jsonJacksonGetString(bodyNode, "WhereClause");
    int pageSize = GrouperUtil.jsonJacksonGetInteger(bodyNode, "pagesize", 100);
    int pageNumber = GrouperUtil.jsonJacksonGetInteger(bodyNode, "pagenumber", 1);

    List<ObjectNode> jsonList;

    if (StringUtils.equals(PERSONNEL_TYPE, typeFullName)) {
      jsonList = findPersonnelWithCriteria(mockServiceResponse, whereClause);
    } else if (StringUtils.equals(CLEARANCE_TYPE, typeFullName)) {
      jsonList = findClearanceWithCriteria(mockServiceResponse, whereClause);
    } else if (StringUtils.equals(CLEARANCE_PAIR_TYPE, typeFullName)) {
      jsonList = findClearancePairWithCriteria(mockServiceResponse, whereClause);
    } else {
      respondError(mockServiceResponse, 400, "Not handling type " + typeFullName);
      return;
    }

    // a null jsonList means the criteria itself was invalid and an error was already sent
    if (jsonList != null) {
      respondWithJsonPage(mockServiceResponse, jsonList, pageSize, pageNumber);
    }
  }

  private List<ObjectNode> findPersonnelWithCriteria(MockServiceResponse mockServiceResponse, String whereClause) {
    List<MockCcurePersonnel> results;

    if (StringUtils.isNotBlank(whereClause)) {
      String[] fieldValue = parseWhereClauseField(whereClause);
      if (!StringUtils.equals("Int1", fieldValue[0])) {
        respondError(mockServiceResponse, 400, "Not handling field " + fieldValue[0]);
        return null;
      }
      results = HibernateSession.byHqlStatic().createQuery("from MockCcurePersonnel where int1 = :theValue")
          .setString("theValue", fieldValue[1]).list(MockCcurePersonnel.class);
    } else {
      results = HibernateSession.byHqlStatic().createQuery("from MockCcurePersonnel").list(MockCcurePersonnel.class);
    }

    List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
    for (MockCcurePersonnel personnel : GrouperUtil.nonNull(results)) {
      jsonList.add(personnel.toJson());
    }
    return jsonList;
  }

  private List<ObjectNode> findClearanceWithCriteria(MockServiceResponse mockServiceResponse, String whereClause) {
    List<MockCcureClearance> results;

    if (StringUtils.isNotBlank(whereClause)) {
      String[] fieldValue = parseWhereClauseField(whereClause);
      if (!StringUtils.equals("Name", fieldValue[0])) {
        respondError(mockServiceResponse, 400, "Not handling field " + fieldValue[0]);
        return null;
      }
      results = HibernateSession.byHqlStatic().createQuery("from MockCcureClearance where name = :theValue")
          .setString("theValue", fieldValue[1]).list(MockCcureClearance.class);
    } else {
      results = HibernateSession.byHqlStatic().createQuery("from MockCcureClearance").list(MockCcureClearance.class);
    }

    List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
    for (MockCcureClearance clearance : GrouperUtil.nonNull(results)) {
      jsonList.add(clearance.toJson());
    }
    return jsonList;
  }

  private List<ObjectNode> findClearancePairWithCriteria(MockServiceResponse mockServiceResponse, String whereClause) {
    List<MockCcureClearancePair> results;

    if (StringUtils.isBlank(whereClause)) {
      results = HibernateSession.byHqlStatic().createQuery("from MockCcureClearancePair").list(MockCcureClearancePair.class);
    } else {
      Map<String, String> subclauses = new LinkedHashMap<String, String>();
      for (String part : whereClause.split(" and ")) {
        String[] fieldValue = parseWhereClauseField(part.trim());
        subclauses.put(fieldValue[0], fieldValue[1]);
      }

      if (subclauses.containsKey("PersonnelID") && subclauses.containsKey("ClearanceID")) {
        results = HibernateSession.byHqlStatic()
            .createQuery("from MockCcureClearancePair where personnelId = :thePersonnelId and clearanceId = :theClearanceId")
            .setInteger("thePersonnelId", GrouperUtil.intValue(subclauses.get("PersonnelID")))
            .setInteger("theClearanceId", GrouperUtil.intValue(subclauses.get("ClearanceID")))
            .list(MockCcureClearancePair.class);
      } else if (subclauses.containsKey("PersonnelID")) {
        results = HibernateSession.byHqlStatic()
            .createQuery("from MockCcureClearancePair where personnelId = :thePersonnelId")
            .setInteger("thePersonnelId", GrouperUtil.intValue(subclauses.get("PersonnelID")))
            .list(MockCcureClearancePair.class);
      } else if (subclauses.containsKey("ClearanceID")) {
        results = HibernateSession.byHqlStatic()
            .createQuery("from MockCcureClearancePair where clearanceId = :theClearanceId")
            .setInteger("theClearanceId", GrouperUtil.intValue(subclauses.get("ClearanceID")))
            .list(MockCcureClearancePair.class);
      } else {
        respondError(mockServiceResponse, 400, "Not handling fields " + subclauses.keySet());
        return null;
      }
    }

    List<ObjectNode> jsonList = new ArrayList<ObjectNode>();
    for (MockCcureClearancePair pair : GrouperUtil.nonNull(results)) {
      jsonList.add(pair.toJson());
    }
    return jsonList;
  }

  /**
   * POST /api/Objects/PersistToContainer - create a single PersonnelClearancePair linking an
   * existing Personnel to an existing Clearance. CCure can't handle bundled clearanceIds so the
   * real client (and this mock) only ever handles one child per call.
   */
  private void persistToContainer(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    HttpServletRequest httpServletRequest = mockServiceRequest.getHttpServletRequest();

    String type = httpServletRequest.getParameter("Type");
    if (!StringUtils.equals(PERSONNEL_TYPE, type)) {
      respondError(mockServiceResponse, 400, "Not handling this type of object: " + type);
      return;
    }

    MockCcurePersonnel personnel = findPersonnel(GrouperUtil.intValue(httpServletRequest.getParameter("ID"), -1));
    if (personnel == null) {
      mockServiceResponse.setResponseCode(404);
      return;
    }

    List<Map<String, String>> children = parseChildrenWithProperties(httpServletRequest);

    if (children.size() > 1) {
      respondError(mockServiceResponse, 500, "Multiple children not allowed. Found " + children.size() + " children");
      return;
    }
    if (children.isEmpty()) {
      respondError(mockServiceResponse, 400, "Missing 'Children' field");
      return;
    }

    Map<String, String> child = children.get(0);
    if (!StringUtils.equals(CLEARANCE_PAIR_TYPE, child.get("Type"))) {
      respondError(mockServiceResponse, 400, "Invalid child type '" + child.get("Type") + "'");
      return;
    }

    MockCcureClearance clearance = findClearance(GrouperUtil.intValue(child.get("ClearanceID"), -1));
    if (clearance == null) {
      mockServiceResponse.setResponseCode(404);
      return;
    }

    Long maxObjectId = new GcDbAccess().sql("select max(object_id) from mock_ccure_clearance_pair").select(Long.class);
    int newObjectId = maxObjectId == null ? 1001 : maxObjectId.intValue() + 1;

    MockCcureClearancePair pair = new MockCcureClearancePair();
    pair.setObjectId(newObjectId);
    pair.setPersonnelId(personnel.getPersonnelId());
    pair.setClearanceId(clearance.getObjectId());
    HibernateSession.byObjectStatic().save(pair);

    respondWithJson(mockServiceResponse, personnel.toJson());
  }

  /**
   * POST /api/Objects/RemoveFromContainer - remove a single PersonnelClearancePair (by its
   * ObjectID) from an existing Personnel.
   */
  private void removeFromContainer(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    HttpServletRequest httpServletRequest = mockServiceRequest.getHttpServletRequest();

    String type = httpServletRequest.getParameter("Type");
    if (!StringUtils.equals(PERSONNEL_TYPE, type)) {
      respondError(mockServiceResponse, 400, "Not handling this type of object: " + type);
      return;
    }

    MockCcurePersonnel personnel = findPersonnel(GrouperUtil.intValue(httpServletRequest.getParameter("ID"), -1));
    if (personnel == null) {
      mockServiceResponse.setResponseCode(404);
      return;
    }

    List<Map<String, String>> children = parseChildrenWithId(httpServletRequest);

    if (children.size() > 1) {
      respondError(mockServiceResponse, 500, "Multiple children not allowed. Found " + children.size() + " children");
      return;
    }
    if (children.isEmpty()) {
      respondError(mockServiceResponse, 400, "Missing 'Children' field");
      return;
    }

    Map<String, String> child = children.get(0);
    if (!StringUtils.equals(CLEARANCE_PAIR_TYPE, child.get("Type"))) {
      respondError(mockServiceResponse, 400, "Invalid child type '" + child.get("Type") + "'");
      return;
    }
    if (StringUtils.isBlank(child.get("ID"))) {
      respondError(mockServiceResponse, 400, "missing ObjectID");
      return;
    }

    int pairObjectId = GrouperUtil.intValue(child.get("ID"), -1);

    List<MockCcureClearancePair> pairs = HibernateSession.byHqlStatic()
        .createQuery("from MockCcureClearancePair where objectId = :theObjectId and personnelId = :thePersonnelId")
        .setInteger("theObjectId", pairObjectId)
        .setInteger("thePersonnelId", personnel.getPersonnelId())
        .list(MockCcureClearancePair.class);

    if (GrouperUtil.length(pairs) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }

    for (MockCcureClearancePair pair : pairs) {
      HibernateSession.byObjectStatic().delete(pair);
    }

    respondWithJson(mockServiceResponse, personnel.toJson());
  }

  /**
   * Parse the "Type"/"ID" plus "Children[i][Type]"/"Children[i][PropertyNames][j]"/
   * "Children[i][PropertyValues][j]" form fields (PersistToContainer shape) into a list of maps,
   * one per child, each holding "Type" plus the property name/value pairs.
   */
  private static List<Map<String, String>> parseChildrenWithProperties(HttpServletRequest httpServletRequest) {
    List<Map<String, String>> children = new ArrayList<Map<String, String>>();

    for (int childIndex = 0; ; childIndex++) {
      String childType = httpServletRequest.getParameter("Children[" + childIndex + "][Type]");
      if (childType == null) {
        break;
      }

      List<String> propertyNames = new ArrayList<String>();
      for (int propIndex = 0; ; propIndex++) {
        String propertyName = httpServletRequest.getParameter("Children[" + childIndex + "][PropertyNames][" + propIndex + "]");
        if (propertyName == null) {
          break;
        }
        propertyNames.add(propertyName);
      }

      List<String> propertyValues = new ArrayList<String>();
      for (int propIndex = 0; ; propIndex++) {
        String propertyValue = httpServletRequest.getParameter("Children[" + childIndex + "][PropertyValues][" + propIndex + "]");
        if (propertyValue == null) {
          break;
        }
        propertyValues.add(propertyValue);
      }

      Map<String, String> child = new LinkedHashMap<String, String>();
      child.put("Type", childType);
      for (int i = 0; i < propertyNames.size() && i < propertyValues.size(); i++) {
        child.put(propertyNames.get(i), propertyValues.get(i));
      }
      children.add(child);
    }

    return children;
  }

  /**
   * Parse the "Type"/"ID" plus "Children[i][Type]"/"Children[i][ID]" form fields
   * (RemoveFromContainer shape) into a list of maps, one per child, each holding "Type" and "ID".
   */
  private static List<Map<String, String>> parseChildrenWithId(HttpServletRequest httpServletRequest) {
    List<Map<String, String>> children = new ArrayList<Map<String, String>>();

    for (int childIndex = 0; ; childIndex++) {
      String childType = httpServletRequest.getParameter("Children[" + childIndex + "][Type]");
      if (childType == null) {
        break;
      }

      Map<String, String> child = new LinkedHashMap<String, String>();
      child.put("Type", childType);
      child.put("ID", httpServletRequest.getParameter("Children[" + childIndex + "][ID]"));
      children.add(child);
    }

    return children;
  }

  /**
   * Split a simple "Field = value" WhereClause into its field and value, stripping surrounding
   * single-quotes from the value if present.
   */
  private static String[] parseWhereClauseField(String whereClause) {
    int idx = whereClause.indexOf(" = ");
    if (idx == -1) {
      throw new RuntimeException("Invalid WhereClause: '" + whereClause + "'");
    }
    String field = whereClause.substring(0, idx).trim();
    String value = whereClause.substring(idx + 3).trim();
    if (value.length() >= 2 && value.startsWith("'") && value.endsWith("'")) {
      value = value.substring(1, value.length() - 1);
    }
    return new String[] {field, value};
  }

  private static MockCcurePersonnel findPersonnel(int personnelId) {
    List<MockCcurePersonnel> results = HibernateSession.byHqlStatic()
        .createQuery("from MockCcurePersonnel where personnelId = :theId")
        .setInteger("theId", personnelId).list(MockCcurePersonnel.class);
    return GrouperUtil.length(results) == 1 ? results.get(0) : null;
  }

  private static MockCcureClearance findClearance(int objectId) {
    List<MockCcureClearance> results = HibernateSession.byHqlStatic()
        .createQuery("from MockCcureClearance where objectId = :theId")
        .setInteger("theId", objectId).list(MockCcureClearance.class);
    return GrouperUtil.length(results) == 1 ? results.get(0) : null;
  }

  /**
   * 200 response with a single JSON node as the body.
   */
  private static void respondWithJson(MockServiceResponse mockServiceResponse, JsonNode jsonNode) {
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(jsonNode));
  }

  /**
   * Page the given list (matching the real API's pagesize/pagenumber semantics), and respond
   * with the page as a JSON array, or 404 if the page is empty.
   */
  private static void respondWithJsonPage(MockServiceResponse mockServiceResponse, List<ObjectNode> jsonList, int pageSize, int pageNumber) {
    if (pageSize <= 0) {
      pageSize = 100;
    }
    if (pageNumber <= 0) {
      pageNumber = 1;
    }
    int fromIndex = (pageNumber - 1) * pageSize;
    int toIndex = fromIndex + pageSize;

    ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
    for (int i = fromIndex; i < toIndex && i < jsonList.size(); i++) {
      arrayNode.add(jsonList.get(i));
    }

    if (arrayNode.size() == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }

    respondWithJson(mockServiceResponse, arrayNode);
  }

  /**
   * The reference Flask mock's error envelope: { "response": { "comment": "Error Message:  ..." } }.
   */
  private static void respondError(MockServiceResponse mockServiceResponse, int code, String message) {
    ObjectNode responseNode = GrouperUtil.jsonJacksonNode();
    ObjectNode commentNode = GrouperUtil.jsonJacksonNode();
    GrouperUtil.jsonJacksonAssignString(commentNode, "comment", "Error Message:  " + message);
    responseNode.set("response", commentNode);

    mockServiceResponse.setResponseCode(code);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(responseNode));
  }

  /**
   * Create the mock tables if they are not already present.
   */
  public static void ensureCcureMockTables() {
    try {
      new GcDbAccess().sql("select count(*) from mock_ccure_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_ccure_personnel").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_ccure_clearance").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_ccure_clearance_pair").select(int.class);
    } catch (Exception e) {
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          MockCcureAuth.createTableCcureAuth(ddlVersionBean, database);
          MockCcurePersonnel.createTableCcurePersonnel(ddlVersionBean, database);
          MockCcureClearance.createTableCcureClearance(ddlVersionBean, database);
          MockCcureClearancePair.createTableCcureClearancePair(ddlVersionBean, database);
        }
      });
    }
  }

}
