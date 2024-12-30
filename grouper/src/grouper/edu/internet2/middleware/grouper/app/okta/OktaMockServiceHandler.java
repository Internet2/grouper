package edu.internet2.middleware.grouper.app.okta;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
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
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class OktaMockServiceHandler extends MockServiceHandler {
  

  /**
   * 
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  /**
   * headers to not log all of
   */
  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }
  
  /**
   * 
   */
  public static void ensureOktaMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_okta_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_okta_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_okta_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_okta_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperOktaGroup.createTableOktaGroup(ddlVersionBean, database);
          GrouperOktaAuth.createTableOktaAuth(ddlVersionBean, database);
          GrouperOktaUser.createTableOktaUser(ddlVersionBean, database);
          GrouperOktaMembership.createTableOktaMembership(ddlVersionBean, database);
          
        }
      });
  
    }    
  }

  /**
   * 
   */
  public static void dropOktaMockTables() {
    MockServiceServlet.dropMockTable("mock_okta_membership");
    MockServiceServlet.dropMockTable("mock_okta_user");
    MockServiceServlet.dropMockTable("mock_okta_group");
    MockServiceServlet.dropMockTable("mock_okta_auth");
  }
  
  private static boolean mockTablesThere = false;

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureOktaMockTables();
    }
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    
    mockNamePaths.remove(0);
    mockNamePaths.remove(0);
    
    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);
    
    mockServiceRequest.setPostMockNamePaths(paths);

    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockNamePaths.get(0)) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("groups".equals(mockNamePaths.get(0)) && "skinny_users".equals(mockNamePaths.get(2)) && 3 == mockNamePaths.size()) {
        getUsersByGroup(mockServiceRequest, mockServiceResponse);
        return;
      }

//      if ("groups".equals(mockNamePaths.get(0)) && 4 == mockNamePaths.size() && "users".equals(mockNamePaths.get(2))) {
//        getUserByGroup(mockServiceRequest, mockServiceResponse);
//        return;
//      }

      if ("users".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("DELETE", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockNamePaths.get(0)) && "users".equals(mockNamePaths.get(2))
          && 4 == mockNamePaths.size()) {
        disassociateGroupFromUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("token".equals(mockNamePaths.get(0))) {
        postAuth(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("users".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      
    }
    
    if (StringUtils.equals("PUT", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockNamePaths.get(0)) && "users".equals(mockNamePaths.get(2))
          && 4 == mockNamePaths.size()) {
        associateGroupWithUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
  }

  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    
    String bearerToken = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (!bearerToken.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization token must start with 'Bearer '");
    }
    String authorizationToken = GrouperUtil.prefixOrSuffix(bearerToken, "Bearer ", false);
    
    List<GrouperOktaAuth> grouperOktaAuths = 
        HibernateSession.byHqlStatic().createQuery("from GrouperOktaAuth where accessToken = :theAccessToken").setString("theAccessToken", authorizationToken).list(GrouperOktaAuth.class);
    
    if (GrouperUtil.length(grouperOktaAuths) != 1) {
      throw new RuntimeException("Invalid access token, not found!");
    }
    
    GrouperOktaAuth grouperOktaAuth = grouperOktaAuths.get(0);    

    if (grouperOktaAuth.getExpiresInSeconds() < System.currentTimeMillis()/1000) {
      throw new RuntimeException("Invalid access token, expired!");
    }
    
    // all good
  }

  private void checkRequestContentType(MockServiceRequest mockServiceRequest) {
    if (!StringUtils.equals(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json")) {
      throw new RuntimeException("Content type must be application/json");
    }
  }

  public void getUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String limit = mockServiceRequest.getHttpServletRequest().getParameter("limit");
    String pageToken = mockServiceRequest.getHttpServletRequest().getParameter("after");
      
    int limitInt = 100;
    if (StringUtils.isNotBlank(limit)) {
      limitInt = GrouperUtil.intValue(limit);
      if (limitInt <= 0) {
        throw new RuntimeException("maxResults cannot be less than or equal to 0.");
      }
      if (limitInt > 500) {
        limitInt = 500;
      }
    }

    List<GrouperOktaUser> grouperOktaUsers = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();
    if (StringUtils.isNotBlank(pageToken)) {
      query = HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser where login > :pageToken");
      query.setScalar("pageToken", pageToken);
    } else {
      query = HibernateSession.byHqlStatic().createQuery("from GrouperOktaUser");
    }
    
    queryOptions.paging(limitInt, 1, true);
    queryOptions.sort(new QuerySort("login", true));
    query.options(queryOptions);
    
    grouperOktaUsers = query.list(GrouperOktaUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    int totalRecordCount = queryOptions.getQueryPaging().getTotalRecordCount();
    if (totalRecordCount > grouperOktaUsers.size()) {
      
      String nextPageToken = grouperOktaUsers.get(grouperOktaUsers.size()-1).getLogin();
      resultNode.put("nextPageToken", nextPageToken);
    }
    
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperOktaUser grouperOktaUser : grouperOktaUsers) {
      valueNode.add(toUserJson(grouperOktaUser));
    }
    
    resultNode.set("users", valueNode);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(valueNode));
    
  }
  
  public void getUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    List<GrouperOktaUser> grouperOktaUsers = HibernateSession.byHqlStatic().createQuery("select distinct user from GrouperOktaUser user where user.id = :theId or user.login = :theLogin")
        .setString("theId", userId).setString("theLogin", userId).list(GrouperOktaUser.class);

    if (GrouperUtil.length(grouperOktaUsers) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      
      ObjectNode objectNode = toUserJson(grouperOktaUsers.get(0));
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(grouperOktaUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("userByIdOrEmail: " + GrouperUtil.length(grouperOktaUsers) + ", id: " + userId);
    }

  }
  
  public void disassociateGroupFromUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    String userId = mockServiceRequest.getPostMockNamePaths()[3];
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "userId is required");
    
    //check if userid exists
    List<GrouperOktaUser> grouperOktaUsers = HibernateSession.byHqlStatic().createQuery("select user from GrouperOktaUser user where user.id = :theId")
        .setString("theId", userId).list(GrouperOktaUser.class);
    
    if (GrouperUtil.length(grouperOktaUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from GrouperOktaMembership where userId = :userId and groupId = :groupId")
      .setString("userId", userId)
      .setString("groupId", groupId)
      .executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
  }
  
  public void associateGroupWithUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);
    
    checkRequestContentType(mockServiceRequest);

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    String userId = mockServiceRequest.getPostMockNamePaths()[3];

    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    //check if userid exists
    List<GrouperOktaUser> grouperOktaUsers = HibernateSession.byHqlStatic().createQuery("select user from GrouperOktaUser user where user.id = :theId")
        .setString("theId", userId).list(GrouperOktaUser.class);
    
    if (GrouperUtil.length(grouperOktaUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    //check if group exists
    List<GrouperOktaGroup> grouperOktaGroups = HibernateSession.byHqlStatic().createQuery("from GrouperOktaGroup where id = :theId")
        .setString("theId", groupId).list(GrouperOktaGroup.class);
    
    if (GrouperUtil.length(grouperOktaGroups) == 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    // check if user has already 100 or more groups
//    ByHqlStatic query = HibernateSession.byHqlStatic()
//        .createQuery("from GrouperOktaGroup g where g.id in (select m.groupId from GrouperOktaMembership m where m.userId = :theUserId) ")
//        .setString("theUserId", userId);
//    
//    QueryOptions queryOptions = new QueryOptions();
//    QueryPaging queryPaging = QueryPaging.page(1, 0, true);
//    queryOptions = queryOptions.paging(queryPaging);
//    
//    query.options(queryOptions);
//    
//    grouperOktaGroups = query.list(GrouperOktaGroup.class);
//    if (queryPaging.getTotalRecordCount() >= 100) {
//      mockServiceResponse.setResponseCode(400);
//      return;
//    }
    
    //check if this groupId and userId are already connected
    List<GrouperOktaMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from GrouperOktaMembership m where m.userId = :userId and m.groupId = :groupId ")
        .setString("userId", userId)
        .setString("groupId", groupId)
        .list(GrouperOktaMembership.class);

    if (GrouperUtil.length(memberships) == 0) {

      //now save the relationship
      GrouperOktaMembership grouperOktaMembership = new GrouperOktaMembership();
      grouperOktaMembership.setGroupId(groupId);
      grouperOktaMembership.setUserId(userId);
      grouperOktaMembership.setId(GrouperUuid.getUuid());
      HibernateSession.byObjectStatic().save(grouperOktaMembership);
    } else if (GrouperUtil.length(memberships) == 1) {
      GrouperOktaMembership grouperOktaMembership = memberships.get(0);
      HibernateSession.byObjectStatic().update(grouperOktaMembership);
    }

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
  }
  
  public void postUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    /**
     * {
        
        "profile": {
         "firstName": "Elizabeth",
         "lastName": "Smith",
         "email": "email@grouper.edu",
         "login": "email@grouper.edu"
        }
        }
     */

    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);

    GrouperOktaUser grouperOktaUser = GrouperOktaUser.fromJson(userJsonNode);
    grouperOktaUser.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperOktaUser);
    
    JsonNode resultNode = grouperOktaUser.toJson(null);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }
  
  public void updateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    //validate that the user already exists first
    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("userId", userId);

    List<GrouperOktaUser> grouperOktaUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperOktaUser where id = :theId")
        .setString("theId", userId).list(GrouperOktaUser.class);
    
    if (GrouperUtil.length(grouperOktaUsers) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindUser", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperOktaUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(grouperOktaUsers));
    }
    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);
    
    GrouperOktaUser grouperOktaUser = GrouperOktaUser.fromJson(userJsonNode);
    grouperOktaUser.setId(userId);
    HibernateSession.byObjectStatic().update(grouperOktaUser);
    
    JsonNode resultNode = grouperOktaUser.toJson(null);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    HibernateSession.byObjectStatic().saveOrUpdate(grouperOktaUser);
    
    // we want users in response
    getUser(mockServiceRequest, mockServiceResponse);
    
  }
  
  
  public void postGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);
    GrouperOktaGroup grouperOktaGroup = GrouperOktaGroup.fromJson(groupJsonNode);
    
    grouperOktaGroup.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperOktaGroup);
    
    ObjectNode objectNode = grouperOktaGroup.toJsonGroupOnly(null);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    
  }

  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String limit = mockServiceRequest.getHttpServletRequest().getParameter("limit");
    String pageToken = mockServiceRequest.getHttpServletRequest().getParameter("after");
    String groupFilter = mockServiceRequest.getHttpServletRequest().getParameter("search");
    
    int limitInt = 100;
    if (StringUtils.isNotBlank(limit)) {
      limitInt = GrouperUtil.intValue(limit);
      if (limitInt <= 0) {
        throw new RuntimeException("limit cannot be less than or equal to 0.");
      }
      if (limitInt > 200) {
        limitInt = 200;
      }
    }

    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    StringBuffer sql = new StringBuffer("from GrouperOktaGroup");
    QueryOptions queryOptions = new QueryOptions();
    String whereConj = " where ";

    if (groupFilter != null) {
      sql.append(whereConj);
      sql.append("name = :name");
      
      String regex = "\"([^\"]*)\"";

      // Create a Pattern object
      Pattern pattern = Pattern.compile(regex);

      // Create a Matcher object
      Matcher matcher = pattern.matcher(groupFilter);

      // Check if a match is found
      if (matcher.find()) {
          // Group 1 contains the content within the quotes
          String result = matcher.group(1);
          byHqlStatic.setScalar("name", result);
      } else {
          throw new RuntimeException("Could not parse group name from search query");
      }
      
      
//      sql.append(groupFilter);
//      whereConj = " and ";
    }

//    if (StringUtils.isNotBlank(pageToken)) {
//      sql.append(whereConj);
//      sql.append(" email > :pageToken");
//      byHqlStatic.setScalar("pageToken", pageToken);
//      whereConj = " and ";
//    }


    queryOptions.paging(limitInt, 1, true);
    
    queryOptions.sort(new QuerySort("id", true));

    List<GrouperOktaGroup> grouperOktaGroups = byHqlStatic.createQuery(sql.toString())
            .options(queryOptions)
            .list(GrouperOktaGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    int totalRecordCount = queryOptions.getQueryPaging().getTotalRecordCount();
    if (totalRecordCount > grouperOktaGroups.size()) {
      
      String nextPageToken = grouperOktaGroups.get(grouperOktaGroups.size()-1).getId();
      resultNode.put("nextPageToken", nextPageToken);
    }
    
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperOktaGroup grouperOktaGroup : grouperOktaGroups) {
      ObjectNode objectNode = grouperOktaGroup.toJsonGroupOnly(null);
      valueNode.add(objectNode);
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(valueNode));
  }
  
  
  public void getUsersByGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");


    ByHqlStatic byHqlStatic = HibernateSession.byHqlStatic();
    QueryOptions queryOptions = new QueryOptions();

    StringBuffer sql = new StringBuffer("from GrouperOktaUser u where u.id in (select m.userId from GrouperOktaMembership m where m.groupId = :theGroupId");
    byHqlStatic.setString("theGroupId", groupId);

    sql.append(")");

    queryOptions.sort(new QuerySort("id", true));

    List<GrouperOktaUser> grouperOktaUsers = byHqlStatic.createQuery(sql.toString())
            .options(queryOptions)
            .list(GrouperOktaUser.class);

    
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperOktaUser grouperOktaUser : grouperOktaUsers) {
      valueNode.add(toUserJson(grouperOktaUser));
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(valueNode));
    
  }

  public void getUserByGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");

    String userId = mockServiceRequest.getPostMockNamePaths()[3];
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "userId is required");

    GrouperOktaUser grouperOktaUser = null;
    ByHqlStatic query = null;
      query = HibernateSession.byHqlStatic()
              .createQuery("from GrouperOktaUser u where u.id in (select m.userId from GrouperOktaMembership m where m.groupId = :theGroupId and userId = :theUserId)")
              .setString("theGroupId", groupId)
              .setString("theUserId", userId);

    grouperOktaUser = query.uniqueResult(GrouperOktaUser.class);

    ObjectNode objectNode = toUserJson(grouperOktaUser);

    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

  }

  public void getGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");

    List<GrouperOktaGroup> grouperOktaGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperOktaGroup where id = :theId or email = :theId")
        .setString("theId", groupId).list(GrouperOktaGroup.class);

    if (GrouperUtil.length(grouperOktaGroups) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      ObjectNode objectNode = grouperOktaGroups.get(0).toJsonGroupOnly(null);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(grouperOktaGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(grouperOktaGroups) + ", id: " + groupId);
    }

  }
  
  public void updateGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    // patch a group
    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("groupId", groupId);

    List<GrouperOktaGroup> grouperOktaGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperOktaGroup where id = :theId")
        .setString("theId", groupId).list(GrouperOktaGroup.class);
    
    if (GrouperUtil.length(grouperOktaGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperOktaGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperOktaGroups));
    }
    GrouperOktaGroup grouperOktaGroup = grouperOktaGroups.get(0);
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);
    GrouperOktaGroup grouperOktaGroupToBeUpdated = GrouperOktaGroup.fromJson(groupJsonNode);
    if (StringUtils.isNotBlank(grouperOktaGroupToBeUpdated.getName())) {
      grouperOktaGroup.setName(grouperOktaGroupToBeUpdated.getName());
    }
    if (StringUtils.isNotBlank(grouperOktaGroupToBeUpdated.getDescription())) {
      grouperOktaGroup.setDescription(grouperOktaGroupToBeUpdated.getDescription());
    }

    HibernateSession.byObjectStatic().saveOrUpdate(grouperOktaGroup);
    
    ObjectNode objectNode = grouperOktaGroup.toJsonGroupOnly(null);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode)); 
  }
  
  public void patchGroupSettings(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    // patch a group
    String groupEmail = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("groupEmail", groupEmail);

    List<GrouperOktaGroup> grouperOktaGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperOktaGroup where email = :theEmail")
        .setString("theEmail", groupEmail).list(GrouperOktaGroup.class);
    
    if (GrouperUtil.length(grouperOktaGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperOktaGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperOktaGroups));
    }
    GrouperOktaGroup grouperOktaGroup = grouperOktaGroups.get(0);
    

    HibernateSession.byObjectStatic().saveOrUpdate(grouperOktaGroup);
    
    ObjectNode objectNode = grouperOktaGroup.toJsonGroupOnly(null);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode)); 
  }
  
  private static long lastDeleteMillis = -1;
  
  public void postAuth(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    String grantType = mockServiceRequest.getHttpServletRequest().getParameter("grant_type");
    String assertion = mockServiceRequest.getHttpServletRequest().getParameter("client_assertion");
    
    if (StringUtils.isBlank(grantType) || StringUtils.isBlank(assertion)) {
      throw new RuntimeException("grant_type and assertion are required!");
    }
    
    if (!StringUtils.equals(grantType, "client_credentials")) {
      throw new RuntimeException("grant_type must be set to urn:ietf:params:oauth:grant-type:jwt-bearer");
    }
    
    DecodedJWT decodedJwt = JWT.decode(assertion);
    
    OktaMockRsaKeyProvider oktaMockRsaKeyProvider = new OktaMockRsaKeyProvider();
    
    Algorithm.RSA256(oktaMockRsaKeyProvider).verify(decodedJwt);
    
    String configId = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouperTest.okta.mock.configId");

    mockServiceResponse.setResponseCode(200);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    //expires in an hour
    long expiresOnSeconds = System.currentTimeMillis()/1000 + 60*60;
    
    resultNode.put("expires_in", expiresOnSeconds);
    
    String accessToken = GrouperUuid.getUuid();
    
    GrouperOktaAuth grouperOktaAuth = new GrouperOktaAuth();
    grouperOktaAuth.setConfigId(configId);
    grouperOktaAuth.setAccessToken(accessToken);
    grouperOktaAuth.setExpiresInSeconds(expiresOnSeconds);
    HibernateSession.byObjectStatic().save(grouperOktaAuth);
    
    resultNode.put("access_token", accessToken);
    
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    //delete if its been a while
    if (System.currentTimeMillis() - lastDeleteMillis > 1000*60*60) {
      lastDeleteMillis = System.currentTimeMillis();
      
      long secondsToDelete = System.currentTimeMillis()/1000 - 60*60;
      
      int accessTokensDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperOktaAuth where expiresInSeconds < :theExpiresOnSeconds")
        .setLong("theExpiresOnSeconds", secondsToDelete).executeUpdateInt();
      
      if (accessTokensDeleted > 0) {
        mockServiceRequest.getDebugMap().put("accessTokensDeleted", accessTokensDeleted);
      }
    }
    
  }

  public void deleteGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");

    HibernateSession.byHqlStatic()
    .createQuery("delete from GrouperOktaMembership where groupId = :groupId")
    .setString("groupId", groupId).executeUpdateInt();
    
    int groupsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperOktaGroup where id = :theId")
        .setString("theId", groupId).executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
        
  }
  
  public void deleteUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");

    HibernateSession.byHqlStatic()
    .createQuery("delete from GrouperOktaMembership where userId = :userId")
    .setString("userId", userId).executeUpdateInt();
    
    HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperOktaUser where id = :theId")
        .setString("theId", userId).executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
        
  }
  
  /**
   * convert from jackson json
   * @param grouperOktaUser
   * @return the grouper okta user
   */
  private static ObjectNode toUserJson(GrouperOktaUser grouperOktaUser) {
    
    ObjectNode result = GrouperUtil.jsonJacksonNode();
  
    
    GrouperUtil.jsonJacksonAssignString(result, "id", grouperOktaUser.getId());
    
    ObjectNode profileNode = GrouperUtil.jsonJacksonNode();
    GrouperUtil.jsonJacksonAssignString(profileNode, "firstName", grouperOktaUser.getFirstName());
    GrouperUtil.jsonJacksonAssignString(profileNode, "lastName", grouperOktaUser.getLastName());
    GrouperUtil.jsonJacksonAssignString(profileNode, "email", grouperOktaUser.getEmail());
    GrouperUtil.jsonJacksonAssignString(profileNode, "login", grouperOktaUser.getLogin());
    result.set("profile", profileNode);
    
    return result;
  }
  
  class OktaMockRsaKeyProvider implements RSAKeyProvider {
    
    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
      PublicKey publicKey = null;
      try {
        String publicKeyEncoded = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.okta.mock.publicKey");
        
        if (StringUtils.isBlank(publicKeyEncoded)) {
          publicKeyEncoded = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAuaGc9tsPiKesuG4u534VbiLXIm55oAsV5PX+EaXRQ0Ah+B3VN2K/lO3lL3Dp8KJWiAaN0ItSpfRsWMBcjZgJVSK4Ah3DAejIpuiEU6BU5puukX/j9OuHgBwZ9KycFUZwUL2i//8ChL+2hvgSha3TtGRBLMrGU/HhY/UEBb5UoMmtiTim95YzuoIs0Q85+Ti5tL/JljAU3zjkYfhoGYjQj7EqQyROSjxB52xYFmABWR2FfXSzMJdyVi6w6QWJKt0VtwOzboiJqSl+QypiK6pdn8jKAB5uErYF5Zbf50K38rSF2BzhAqwNEIVWhrx/jB9iu9cyXNx328bWQw2hpDZ6hwIDAQAB";  // rsaKeypair[0];
        }
        
        byte[] publicKeyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(publicKeyEncoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        publicKey = kf.generatePublic(publicKeySpec);
      } catch (NoSuchAlgorithmException e) {
        throw new RuntimeException(
            "Could not reconstruct the public key, the given algorithm could not be found.", e);
      } catch (InvalidKeySpecException e) {
        throw new RuntimeException("Could not reconstruct the public key", e);
      }
      
      if (publicKey instanceof RSAPublicKey) {
        return (RSAPublicKey)publicKey;
      }
      return null;
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
      throw new RuntimeException("Doesnt do private keys");
    }

    @Override
    public String getPrivateKeyId() {
      throw new RuntimeException("Doesnt do private keys");
    }
  }
  
}
