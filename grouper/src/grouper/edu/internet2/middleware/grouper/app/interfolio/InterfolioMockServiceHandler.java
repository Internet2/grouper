package edu.internet2.middleware.grouper.app.interfolio;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

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
 * Simulates the Interfolio API for tests.  Both the IAM host and the byc host point at this one mock
 * (URL /grouper/mockServices/interfolio/...); requests are dispatched by HTTP method + path.
 *
 * Implemented (the operations that work with our credentials):
 *   POST /iam/{tid}/users                                                  -> createUser
 *   PUT  /iam/{tid}/users/{pid}                                            -> updateUser
 *   GET  /byc/core/tenure/{tid}/institutions/{tid}/users/search            -> searchUsers
 *   POST /byc-tenure|byc-search/{tid}/users/{pid}/subscribe                -> subscribe
 *   PUT  /byc-tenure|byc-search/{tid}/users/{pid}/unsubscribe              -> unsubscribe
 */
public class InterfolioMockServiceHandler extends MockServiceHandler {

  /** sensitive headers not to log */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  /** sensitive parameters not to log */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet();

  private static boolean mockTablesThere = false;

  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }

  @Override
  public Set<String> doNotLogParameters() {
    return doNotLogParameters;
  }

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!mockTablesThere) {
      ensureInterfolioMockTables();
      mockTablesThere = true;
    }

    String method = mockServiceRequest.getHttpServletRequest().getMethod();
    String[] paths = mockServiceRequest.getPostMockNamePaths();
    String firstSegment = GrouperUtil.length(paths) > 0 ? paths[0] : null;
    String lastSegment = GrouperUtil.length(paths) > 0 ? paths[paths.length - 1] : null;

    if (StringUtils.equals("POST", method)) {
      // POST /iam/{tid}/users
      if (StringUtils.equals("iam", firstSegment) && StringUtils.equals("users", lastSegment) && paths.length == 3) {
        createUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      // POST /{service}/{tid}/users/{pid}/subscribe
      if (isBycService(firstSegment) && StringUtils.equals("subscribe", lastSegment) && paths.length == 5) {
        setSubscription(mockServiceRequest, mockServiceResponse, firstSegment, paths[3], true);
        return;
      }
    }

    if (StringUtils.equals("PUT", method)) {
      // PUT /iam/{tid}/users/{pid}
      if (StringUtils.equals("iam", firstSegment) && paths.length == 4 && StringUtils.equals("users", paths[2])) {
        updateUser(mockServiceRequest, mockServiceResponse, paths[3]);
        return;
      }
      // PUT /{service}/{tid}/users/{pid}/unsubscribe
      if (isBycService(firstSegment) && StringUtils.equals("unsubscribe", lastSegment) && paths.length == 5) {
        setSubscription(mockServiceRequest, mockServiceResponse, firstSegment, paths[3], false);
        return;
      }
    }

    if (StringUtils.equals("GET", method)) {
      // GET /byc/core/tenure/{tid}/institutions/{tid}/users/search
      if (StringUtils.equals("byc", firstSegment) && StringUtils.equals("search", lastSegment)) {
        searchUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    throw new RuntimeException("Not expecting request: '" + method + "', '"
        + mockServiceRequest.getPostMockNamePath() + "'");
  }

  /**
   * @param segment first path segment
   * @return true if this is a byc product service segment
   */
  private static boolean isBycService(String segment) {
    return StringUtils.equals("byc-tenure", segment) || StringUtils.equals("byc-search", segment);
  }

  /**
   * The Interfolio HMAC auth header looks like "Authorization: INTF {publicKey}:{signature}".  The
   * mock does not recompute the signature; it just verifies the header is present and well-formed.
   * @param mockServiceRequest the request
   */
  private void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String authorization = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (authorization == null || !authorization.startsWith("INTF ") || !authorization.contains(":")) {
      throw new RuntimeException("Authorization header must look like 'INTF {publicKey}:{signature}'");
    }
  }

  /**
   * POST /iam/{tid}/users - create a user.  Enforces email uniqueness (400 on duplicate), assigns a
   * pid and a byc id, and returns the IAM user JSON.
   */
  private void createUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    JsonNode bodyNode = GrouperUtil.jsonJacksonNode(mockServiceRequest.getRequestBody());
    String email = GrouperUtil.jsonJacksonGetString(bodyNode, "email");

    List<InterfolioUser> existing = HibernateSession.byHqlStatic()
        .createQuery("from InterfolioUser where email = :theEmail")
        .setString("theEmail", email).list(InterfolioUser.class);
    if (GrouperUtil.length(existing) > 0) {
      respondValidationError(mockServiceResponse,
          "Validation failed: Email address " + email + " already exists for an Interfolio account at this Institution. Try signing in instead.");
      return;
    }

    InterfolioUser user = new InterfolioUser();
    user.assignAttributesFromIamJson(bodyNode);
    int count = new GcDbAccess().sql("select count(*) from mock_interfolio_user").select(int.class);
    user.setPid(String.valueOf(8000000 + count + 1));
    user.setBycId(String.valueOf(1000000 + count + 1));
    user.setSubscribedRpt(false);
    user.setSubscribedFs(false);
    HibernateSession.byObjectStatic().save(user);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(user.toIamUserJson()));
  }

  /**
   * PUT /iam/{tid}/users/{pid} - update a user.  institution_user_id is immutable (400 if changed);
   * the mutable fields are replaced and the user is returned.
   */
  private void updateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String pid) {
    checkAuthorization(mockServiceRequest);

    InterfolioUser user = findByPid(pid);
    if (user == null) {
      mockServiceResponse.setResponseCode(404);
      return;
    }

    JsonNode bodyNode = GrouperUtil.jsonJacksonNode(mockServiceRequest.getRequestBody());
    String newInstitutionUserId = GrouperUtil.jsonJacksonGetString(bodyNode, "institution_user_id");
    if (!StringUtils.equals(newInstitutionUserId, user.getInstitutionUserId())) {
      respondValidationError(mockServiceResponse, "Validation failed: Institution user id can't be changed.");
      return;
    }

    // replace the mutable fields
    user.setSamlId(GrouperUtil.jsonJacksonGetString(bodyNode, "saml_id"));
    user.setUserType(GrouperUtil.jsonJacksonGetString(bodyNode, "user_type"));
    user.setFirstName(GrouperUtil.jsonJacksonGetString(bodyNode, "first_name"));
    user.setLastName(GrouperUtil.jsonJacksonGetString(bodyNode, "last_name"));
    user.setEmail(GrouperUtil.jsonJacksonGetString(bodyNode, "email"));
    HibernateSession.byObjectStatic().update(user);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(user.toIamUserJson()));
  }

  /**
   * GET /byc/core/tenure/{tid}/institutions/{tid}/users/search - return a page of users matching the
   * (optional) search term, in the { limit, page, total_count, results } envelope.
   */
  private void searchUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    String searchTerm = mockServiceRequest.getHttpServletRequest().getParameter("search");
    int limit = GrouperUtil.intValue(mockServiceRequest.getHttpServletRequest().getParameter("limit"), 25);
    int page = GrouperUtil.intValue(mockServiceRequest.getHttpServletRequest().getParameter("page"), 1);
    if (limit < 1) {
      limit = 25;
    }
    if (page < 1) {
      page = 1;
    }

    List<InterfolioUser> allUsers = HibernateSession.byHqlStatic()
        .createQuery("from InterfolioUser").list(InterfolioUser.class);

    ArrayNode resultsNode = GrouperUtil.jsonJacksonArrayNode();
    int totalCount = 0;
    int fromIndex = (page - 1) * limit;
    int toIndex = fromIndex + limit;
    for (InterfolioUser user : GrouperUtil.nonNull(allUsers)) {
      if (!user.matchesSearchTerm(searchTerm)) {
        continue;
      }
      // totalCount is the count of all matches; only the current page's slice goes in results
      if (totalCount >= fromIndex && totalCount < toIndex) {
        resultsNode.add(user.toSearchResultJson());
      }
      totalCount++;
    }

    ObjectNode rootNode = GrouperUtil.jsonJacksonNode();
    rootNode.put("limit", limit);
    rootNode.put("page", page);
    rootNode.put("total_count", totalCount);
    rootNode.set("results", resultsNode);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(rootNode));
  }

  /**
   * POST/PUT /{service}/{tid}/users/{pid}/(un)subscribe - toggle a product subscription flag.
   */
  private void setSubscription(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse,
      String service, String pid, boolean subscribed) {
    checkAuthorization(mockServiceRequest);

    InterfolioUser user = findByPid(pid);
    if (user == null) {
      mockServiceResponse.setResponseCode(404);
      return;
    }

    if (StringUtils.equals("byc-tenure", service)) {
      user.setSubscribedRpt(subscribed);
    } else if (StringUtils.equals("byc-search", service)) {
      user.setSubscribedFs(subscribed);
    }
    HibernateSession.byObjectStatic().update(user);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody("{\"status\":\"ok\"}");
  }

  /**
   * @param pid Interfolio person id
   * @return the user with that pid, or null
   */
  private static InterfolioUser findByPid(String pid) {
    List<InterfolioUser> users = HibernateSession.byHqlStatic()
        .createQuery("from InterfolioUser where pid = :thePid")
        .setString("thePid", pid).list(InterfolioUser.class);
    return GrouperUtil.length(users) == 1 ? users.get(0) : null;
  }

  /**
   * Respond with the Interfolio validation-error envelope (HTTP 400).
   */
  private void respondValidationError(MockServiceResponse mockServiceResponse, String message) {
    ObjectNode errorNode = GrouperUtil.jsonJacksonNode();
    ArrayNode errorsNode = GrouperUtil.jsonJacksonArrayNode();
    ObjectNode oneError = GrouperUtil.jsonJacksonNode();
    oneError.put("field", "");
    oneError.put("message", message);
    errorsNode.add(oneError);
    errorNode.set("errors", errorsNode);
    errorNode.put("error_class", "ActiveRecord::RecordInvalid");

    mockServiceResponse.setResponseCode(400);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(errorNode));
  }

  /**
   * Create the mock table if it is not already present.
   */
  public static void ensureInterfolioMockTables() {
    try {
      new GcDbAccess().sql("select count(*) from mock_interfolio_user").select(int.class);
    } catch (Exception e) {
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {
          Database database = ddlVersionBean.getDatabase();
          InterfolioUser.createTableInterfolioUser(ddlVersionBean, database);
        }
      });
    }
  }

}
