package edu.internet2.middleware.grouper.app.google;

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
import org.apache.ddlutils.model.Database;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.azure.GrouperAzureAuth;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QueryPaging;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.morphString.Morph;

public class GoogleMockServiceHandler extends MockServiceHandler {
  
  /**
   * 
   */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");

  /**
   * 
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  /**
   * params to not log all of
   */
  @Override
  public Set<String> doNotLogParameters() {
    
    return doNotLogParameters;
  }

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
  public static void ensureGoogleMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_google_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_google_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_google_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_google_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperGoogleGroup.createTableGoogleGroup(ddlVersionBean, database);
          GrouperGoogleAuth.createTableGoogleAuth(ddlVersionBean, database);
          GrouperGoogleUser.createTableGoogleUser(ddlVersionBean, database);
          GrouperGoogleMembership.createTableGoogleMembership(ddlVersionBean, database);
          
        }
      });
  
    }    
  }

  /**
   * 
   */
  public static void dropGoogleMockTables() {
    MockServiceServlet.dropMockTable("mock_google_membership");
    MockServiceServlet.dropMockTable("mock_google_user");
    MockServiceServlet.dropMockTable("mock_google_group");
    MockServiceServlet.dropMockTable("mock_google_auth");
  }
  
  private static boolean mockTablesThere = false;

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureGoogleMockTables();
    }
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    
//    GrouperUtil.assertion(mockNamePaths.size() >= 2, "Must start with admin/v1 or admin/v2");
//    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(0), "admin"), "");
//    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(1), "v1") || StringUtils.equals(mockNamePaths.get(1), "v2"), "");
    
    //mockNamePaths = mockNamePaths.subList(2, mockNamePaths.size());
    
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
      
      if ("groups".equals(mockNamePaths.get(0)) && "members".equals(mockNamePaths.get(2)) && 3 == mockNamePaths.size()) {
        getUsersByGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("settings".equals(mockNamePaths.get(0)) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroupSettings(mockServiceRequest, mockServiceResponse);
        return;
      }

      if ("users".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockNamePaths.get(0))  
          && "groups".equals(mockNamePaths.get(2))
          && 3 == mockNamePaths.size()) {
        getGroupsByUser(mockServiceRequest, mockServiceResponse);
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
      if ("groups".equals(mockNamePaths.get(0)) && "members".equals(mockNamePaths.get(2))
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
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockNamePaths.get(0)) && "members".equals(mockNamePaths.get(2))
          && 3 == mockNamePaths.size()) {
        associateGroupWithUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    if (StringUtils.equals("PATCH", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("settings".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        patchGroupSettings(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    if (StringUtils.equals("PUT", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
  }

  public static void main(String[] args) {
//    String key = "gtdfxv9YgVBYcF6dl2Eq17KUQJN2PLM2ODVTkvoT";
//    String msg = "Fri, 07 Dec 2012 17:18:00 -0000\nPOST\nfoo.bar52.com\n/Foo/BaR2/qux\n%E4%9A%9A%E2%A1%BB%E3%97%90%E8%BB%B3%E6%9C%A7%E5%80%AA%E0%A0%90%ED%82%91%C3%88%EC%85%B0=%E0%BD%85%E1%A9%B6%E3%90%9A%E6%95%8C%EC%88%BF%E9%AC%89%EA%AF%A2%E8%8D%83%E1%AC%A7%E6%83%90&%E7%91%89%E7%B9%8B%EC%B3%BB%E5%A7%BF%EF%B9%9F%E8%8E%B7%EA%B7%8C%E9%80%8C%EC%BF%91%E7%A0%93=%E8%B6%B7%E5%80%A2%E9%8B%93%E4%8B%AF%E2%81%BD%E8%9C%B0%EA%B3%BE%E5%98%97%E0%A5%86%E4%B8%B0&%E7%91%B0%E9%8C%94%E9%80%9C%E9%BA%AE%E4%83%98%E4%88%81%E8%8B%98%E8%B1%B0%E1%B4%B1%EA%81%82=%E1%9F%99%E0%AE%A8%E9%8D%98%EA%AB%9F%EA%90%AA%E4%A2%BE%EF%AE%96%E6%BF%A9%EB%9F%BF%E3%8B%B3&%EC%8B%85%E2%B0%9D%E2%98%A0%E3%98%97%E9%9A%B3F%E8%98%85%E2%83%A8%EA%B0%A1%E5%A4%B4=%EF%AE%A9%E4%86%AA%EB%B6%83%E8%90%8B%E2%98%95%E3%B9%AE%E6%94%AD%EA%A2%B5%ED%95%ABU";
//
////    Assert.assertEquals("failure - HMAC-SHA1",
////                        "f01811cbbf9561623ab45b893096267fd46a5178",
////                        h.signHMAC(key, msg));
//    
//    String hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, key).hmacHex(msg);
//    System.out.println(hmac);
    
    GrouperStartup.startup();
    ensureGoogleMockTables();
  }
  
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    
    String bearerToken = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (!bearerToken.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization token must start with 'Bearer '");
    }
    String authorizationToken = GrouperUtil.prefixOrSuffix(bearerToken, "Bearer ", false);
    
    List<GrouperGoogleAuth> grouperGoogleAuths = 
        HibernateSession.byHqlStatic().createQuery("from GrouperGoogleAuth where accessToken = :theAccessToken").setString("theAccessToken", authorizationToken).list(GrouperGoogleAuth.class);
    
    if (GrouperUtil.length(grouperGoogleAuths) != 1) {
      throw new RuntimeException("Invalid access token, not found!");
    }
    
    GrouperGoogleAuth grouperGoogleAuth = grouperGoogleAuths.get(0);    

    if (grouperGoogleAuth.getExpiresInSeconds() < System.currentTimeMillis()/1000) {
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

//    String offset = mockServiceRequest.getHttpServletRequest().getParameter("offset");
//    String limit = mockServiceRequest.getHttpServletRequest().getParameter("limit");
//    
//    int limitInt = 100;
//    if (StringUtils.isNotBlank(limit)) {
//      limitInt = GrouperUtil.intValue(limit);
//      if (limitInt <= 0) {
//        throw new RuntimeException("limit cannot be less than or equal to 0.");
//      }
//      if (limitInt > 300) {
//        limitInt = 300;
//      }
//    }
//    
//    int offsetInt = 0;
//    int pageNumber = 1;
//    if (StringUtils.isNotBlank(offset)) {
//      offsetInt = GrouperUtil.intValue(offset);
//      pageNumber = offsetInt/limitInt + 1;
//    }
//    
    List<GrouperGoogleUser> grouperGoogleUsers = null;
    
    ByHqlStatic query = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleUser");
    
//    QueryOptions queryOptions = new QueryOptions();
//    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber, true);
//    queryOptions = queryOptions.paging(queryPaging);
//    
//    query.options(queryOptions);
    
    grouperGoogleUsers = query.list(GrouperGoogleUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
//    resultNode.put("stat", "OK");
    
    for (GrouperGoogleUser grouperGoogleUser : grouperGoogleUsers) {
      valueNode.add(toUserJson(grouperGoogleUser));
    }
    
    resultNode.set("users", valueNode);
//    if (queryPaging.getTotalRecordCount() > offsetInt + grouperGoogleUsers.size()) {
//      ObjectNode metadataNode = GrouperUtil.jsonJacksonNode();
//      metadataNode.put("next_offset", offsetInt + limitInt);
//      resultNode.set("metadata", metadataNode);
//    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
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
    
    List<GrouperGoogleUser> grouperGoogleUsers = HibernateSession.byHqlStatic().createQuery("select distinct user from GrouperGoogleUser user where user.id = :theId")
        .setString("theId", userId).list(GrouperGoogleUser.class);

    if (GrouperUtil.length(grouperGoogleUsers) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      
//      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
//      resultNode.put("stat", "OK");
      ObjectNode objectNode = toUserJson(grouperGoogleUsers.get(0));
      
//      resultNode.set("response", objectNode);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(grouperGoogleUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("userById: " + GrouperUtil.length(grouperGoogleUsers) + ", id: " + userId);
    }

  }
  
  public void getGroupsByUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    String offset = mockServiceRequest.getHttpServletRequest().getParameter("offset");
    String limit = mockServiceRequest.getHttpServletRequest().getParameter("limit");
    
    int limitInt = 100;
    if (StringUtils.isNotBlank(limit)) {
      limitInt = GrouperUtil.intValue(limit);
      if (limitInt <= 0) {
        throw new RuntimeException("limit cannot be less than or equal to 0.");
      }
      if (limitInt > 500) {
        limitInt = 500;
      }
    }
    
    int offsetInt = 0;
    int pageNumber = 1;
    if (StringUtils.isNotBlank(offset)) {
      offsetInt = GrouperUtil.intValue(offset);
      pageNumber = offsetInt/limitInt + 1;
    }
    
    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from GrouperGoogleGroup g where g.id in (select m.groupId from GrouperGoogleMembership m where m.userId = :theUserId) ")
        .setString("theUserId", userId);
    
    QueryOptions queryOptions = new QueryOptions();
    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber , true);
    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    List<GrouperGoogleGroup> grouperGoogleGroups = query.list(GrouperGoogleGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    resultNode.put("stat", "OK");
    
    for (GrouperGoogleGroup grouperGoogleGroup : grouperGoogleGroups) {
      // valueNode.add(toGroupJson(grouperGoogleGroup));
    }
    
    resultNode.set("response", valueNode);
    if (queryPaging.getTotalRecordCount() > offsetInt + grouperGoogleGroups.size()) {
      ObjectNode metadataNode = GrouperUtil.jsonJacksonNode();
      metadataNode.put("next_offset", offsetInt + limitInt);
      resultNode.set("metadata", metadataNode);
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

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
    List<GrouperGoogleUser> grouperGoogleUsers = HibernateSession.byHqlStatic().createQuery("select user from GrouperGoogleUser user where user.id = :theId")
        .setString("theId", groupId).list(GrouperGoogleUser.class);
    
    if (GrouperUtil.length(grouperGoogleUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from GrouperGoogleMembership where userId = :userId and groupId = :groupId")
      .setString("userId", groupId)
      .setString("groupId", groupId)
      .executeUpdateInt();

//    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
//    resultNode.put("stat", "OK");
//    resultNode.put("response", "");
    
//    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    mockServiceResponse.setResponseCode(200);
//    mockServiceResponse.setContentType("application/json");
//    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode)); 
  }
  
  public void associateGroupWithUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);
    
    checkRequestContentType(mockServiceRequest);

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    String memberIdInJson = mockServiceRequest.getRequestBody();
    JsonNode memberIdNode = GrouperUtil.jsonJacksonNode(memberIdInJson);
    
    String userId = memberIdNode.get("id").asText();
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    //check if userid exists
    List<GrouperGoogleUser> grouperGoogleUsers = HibernateSession.byHqlStatic().createQuery("select user from GrouperGoogleUser user where user.id = :theId")
        .setString("theId", userId).list(GrouperGoogleUser.class);
    
    if (GrouperUtil.length(grouperGoogleUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    //check if group exists
    List<GrouperGoogleGroup> grouperGoogleGroups = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup where id = :theId")
        .setString("theId", groupId).list(GrouperGoogleGroup.class);
    
    if (GrouperUtil.length(grouperGoogleGroups) == 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    // check if user has already 100 or more groups
    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from GrouperGoogleGroup g where g.id in (select m.groupId from GrouperGoogleMembership m where m.userId = :theUserId) ")
        .setString("theUserId", userId);
    
    QueryOptions queryOptions = new QueryOptions();
    QueryPaging queryPaging = QueryPaging.page(1, 0, true);
    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    grouperGoogleGroups = query.list(GrouperGoogleGroup.class);
    if (queryPaging.getTotalRecordCount() >= 100) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    //check if this groupId and userId are already connected
    List<GrouperGoogleMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from GrouperGoogleMembership m where m.userId = :userId and m.groupId = :groupId ")
        .setString("userId", userId)
        .setString("groupId", groupId)
        .list(GrouperGoogleMembership.class);
    
    if (GrouperUtil.length(memberships) == 0) {
      
      //now save the relationship
      GrouperGoogleMembership grouperGoogleMembership = new GrouperGoogleMembership();
      grouperGoogleMembership.setGroupId(groupId);
      grouperGoogleMembership.setUserId(userId);
      grouperGoogleMembership.setId(GrouperUuid.getUuid());
      
      HibernateSession.byObjectStatic().save(grouperGoogleMembership); 
      
    }
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    resultNode.put("response", "");
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode)); 
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
        "primaryEmail": "liz5@viveksachdeva.com",
        "name": {
         "givenName": "Elizabeth",
         "familyName": "Smith"
        },
        "password": "testGrouper"
        }
     */

    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);

    //check require args
//    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "primaryEmail")) > 0, "primaryEmail is required");
//    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "password")) > 0 , "password is required");
//    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "mailNickname")) <= 64, "mailNickname must be less than 64");
//    GrouperUtil.assertion(GrouperUtil.jsonJacksonGetBoolean(userJsonNode, "accountEnabled") != null, "accountEnabled is required");
//    
//    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "userPrincipalName")) > 0, "userPrincipalName is required");
//
//
//    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "id")) == 0, "id is forbidden");

    GrouperGoogleUser grouperGoogleUser = GrouperGoogleUser.fromJson(userJsonNode);
    grouperGoogleUser.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperGoogleUser);
    
    JsonNode resultNode = grouperGoogleUser.toJson(null);

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

    List<GrouperGoogleUser> grouperGoogleUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperGoogleUser where id = :theId")
        .setString("theId", userId).list(GrouperGoogleUser.class);
    
    if (GrouperUtil.length(grouperGoogleUsers) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindUser", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperGoogleUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(grouperGoogleUsers));
    }
    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);
    
    GrouperGoogleUser grouperGoogleUser = GrouperGoogleUser.fromJson(userJsonNode);
    
    HibernateSession.byObjectStatic().save(grouperGoogleUser);
    
    JsonNode resultNode = grouperGoogleUser.toJson(null);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    

    
    HibernateSession.byObjectStatic().saveOrUpdate(grouperGoogleUser);
    
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
    GrouperGoogleGroup grouperGoogleGroup = GrouperGoogleGroup.fromJson(groupJsonNode);
    
    grouperGoogleGroup.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperGoogleGroup);
    
    //ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
//    resultNode.put("stat", "OK");
//    ObjectNode objectNode = toGroupJson(grouperGoogleGroup);
    ObjectNode objectNode = grouperGoogleGroup.toJsonGroupOnly(null);
    
//    resultNode.set("response", objectNode);
    
    //mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

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

//    String offset = mockServiceRequest.getHttpServletRequest().getParameter("offset");
//    String limit = mockServiceRequest.getHttpServletRequest().getParameter("limit");
    
//    int limitInt = 100;
//    if (StringUtils.isNotBlank(limit)) {
//      limitInt = GrouperUtil.intValue(limit);
//      if (limitInt <= 0) {
//        throw new RuntimeException("limit cannot be less than or equal to 0.");
//      }
//    }
//    
//    int offsetInt = 0;
//    int pageNumber = 1;
//    if (StringUtils.isNotBlank(offset)) {
//      offsetInt = GrouperUtil.intValue(offset);
//      pageNumber = offsetInt/limitInt + 1;
//    }
    
    List<GrouperGoogleGroup> grouperGoogleGroups = null;
    
    ByHqlStatic query = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup");
    
//    QueryOptions queryOptions = new QueryOptions();
//    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber, true);
//    queryOptions = queryOptions.paging(queryPaging);
//    
//    query.options(queryOptions);
    
    grouperGoogleGroups = query.list(GrouperGoogleGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    //resultNode.put("stat", "OK");
    
    for (GrouperGoogleGroup grouperGoogleGroup : grouperGoogleGroups) {
//      valueNode.add(toGroupJson(grouperGoogleGroup));
      valueNode.add(grouperGoogleGroup.toJsonGroupOnly(null));
    }
    
    resultNode.set("groups", valueNode);
//    if (queryPaging.getTotalRecordCount() > offsetInt + grouperGoogleGroups.size()) {
//      ObjectNode metadataNode = GrouperUtil.jsonJacksonNode();
//      metadataNode.put("next_offset", offsetInt + limitInt);
//      resultNode.set("metadata", metadataNode);
//    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
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
    
//    String offset = mockServiceRequest.getHttpServletRequest().getParameter("offset");
//    String limit = mockServiceRequest.getHttpServletRequest().getParameter("limit");
    
//    int limitInt = 100;
//    if (StringUtils.isNotBlank(limit)) {
//      limitInt = GrouperUtil.intValue(limit);
//      if (limitInt <= 0) {
//        throw new RuntimeException("limit cannot be less than or equal to 0.");
//      }
//      if (limitInt > 500) {
//        limitInt = 500;
//      }
//    }
//    
//    int offsetInt = 0;
//    int pageNumber = 1;
//    if (StringUtils.isNotBlank(offset)) {
//      offsetInt = GrouperUtil.intValue(offset);
//      pageNumber = offsetInt/limitInt + 1;
//    }
    
    List<GrouperGoogleUser> grouperGoogleUsers = null;
    
    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from GrouperGoogleUser u where u.id in (select m.userId from GrouperGoogleMembership m where m.groupId = :theGroupId) ")
        .setString("theGroupId", groupId);
    
//    QueryOptions queryOptions = new QueryOptions();
//    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber , true);
//    queryOptions = queryOptions.paging(queryPaging);
//    
//    query.options(queryOptions);
    
    grouperGoogleUsers = query.list(GrouperGoogleUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    //resultNode.put("stat", "OK");
    
    for (GrouperGoogleUser grouperGoogleUser : grouperGoogleUsers) {
      valueNode.add(toUserJson(grouperGoogleUser));
    }
    
    resultNode.set("members", valueNode);
//    if (queryPaging.getTotalRecordCount() > offsetInt + grouperGoogleUsers.size()) {
//      ObjectNode metadataNode = GrouperUtil.jsonJacksonNode();
//      metadataNode.put("next_offset", offsetInt + limitInt);
//      resultNode.set("metadata", metadataNode);
//    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
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
    
    List<GrouperGoogleGroup> grouperGoogleGroups = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup where id = :theId")
        .setString("theId", groupId).list(GrouperGoogleGroup.class);

    if (GrouperUtil.length(grouperGoogleGroups) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");

      
//      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
//      resultNode.put("stat", "OK");
//      ObjectNode objectNode = toGroupJson(grouperGoogleGroups.get(0));
      ObjectNode objectNode = grouperGoogleGroups.get(0).toJsonGroupOnly(null);
      
//      resultNode.set("response", objectNode);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(grouperGoogleGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(grouperGoogleGroups) + ", id: " + groupId);
    }

  }
  
  public void getGroupSettings(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String groupEmail = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupEmail) > 0, "groupEmail is required");
    
    List<GrouperGoogleGroup> grouperGoogleGroups = HibernateSession.byHqlStatic().createQuery("from GrouperGoogleGroup where email = :theEmail")
        .setString("theEmail", groupEmail).list(GrouperGoogleGroup.class);

    if (GrouperUtil.length(grouperGoogleGroups) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");

      
//      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
//      resultNode.put("stat", "OK");
//      ObjectNode objectNode = toGroupJson(grouperGoogleGroups.get(0));
      ObjectNode objectNode = grouperGoogleGroups.get(0).toJsonGroupSettings(null);
      
//      resultNode.set("response", objectNode);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(grouperGoogleGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsByEmail: " + GrouperUtil.length(grouperGoogleGroups) + ", groupEmail: " + groupEmail);
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

    List<GrouperGoogleGroup> grouperGoogleGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperGoogleGroup where id = :theId")
        .setString("theId", groupId).list(GrouperGoogleGroup.class);
    
    if (GrouperUtil.length(grouperGoogleGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperGoogleGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperGoogleGroups));
    }
    GrouperGoogleGroup grouperGoogleGroup = grouperGoogleGroups.get(0);
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);
    GrouperGoogleGroup grouperGoogleGroupToBeUpdated = GrouperGoogleGroup.fromJson(groupJsonNode);
    if (StringUtils.isNotBlank(grouperGoogleGroupToBeUpdated.getEmail())) {
      grouperGoogleGroup.setEmail(grouperGoogleGroupToBeUpdated.getEmail());
    }
    if (StringUtils.isNotBlank(grouperGoogleGroupToBeUpdated.getName())) {
      grouperGoogleGroup.setName(grouperGoogleGroupToBeUpdated.getName());
    }
    if (StringUtils.isNotBlank(grouperGoogleGroupToBeUpdated.getDescription())) {
      grouperGoogleGroup.setDescription(grouperGoogleGroupToBeUpdated.getDescription());
    }

    HibernateSession.byObjectStatic().saveOrUpdate(grouperGoogleGroup);
    
//    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
//    resultNode.put("stat", "OK");
//    ObjectNode objectNode = toGroupJson(grouperGoogleGroup);
    ObjectNode objectNode = grouperGoogleGroup.toJsonGroupOnly(null);
    
//    resultNode.set("response", objectNode);
    
//    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

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

    List<GrouperGoogleGroup> grouperGoogleGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperGoogleGroup where email = :theEmail")
        .setString("theEmail", groupEmail).list(GrouperGoogleGroup.class);
    
    if (GrouperUtil.length(grouperGoogleGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperGoogleGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperGoogleGroups));
    }
    GrouperGoogleGroup grouperGoogleGroup = grouperGoogleGroups.get(0);
    
    String groupSettingsJson = mockServiceRequest.getRequestBody();
    JsonNode groupSettingsJsonNode = GrouperUtil.jsonJacksonNode(groupSettingsJson);
//    GrouperGoogleGroup grouperGoogleGroupToBeUpdated = GrouperGoogleGroup.fromJson(groupSettingsJsonNode);
    
    grouperGoogleGroup.populateGroupSettings(groupSettingsJsonNode);
    
//    grouperGoogleGroupToBeUpdated.populateGroupSettings(groupSettingsJsonNode);
    
    if (StringUtils.isNotBlank(grouperGoogleGroup.getWhoCanAdd())) {
      grouperGoogleGroup.setWhoCanAdd(grouperGoogleGroup.getWhoCanAdd());
    }
    if (StringUtils.isNotBlank(grouperGoogleGroup.getWhoCanJoin())) {
      grouperGoogleGroup.setWhoCanJoin(grouperGoogleGroup.getWhoCanJoin());
    }
    if (StringUtils.isNotBlank(grouperGoogleGroup.getWhoCanViewMembership())) {
      grouperGoogleGroup.setWhoCanViewMembership(grouperGoogleGroup.getWhoCanViewMembership());
    }
    if (StringUtils.isNotBlank(grouperGoogleGroup.getWhoCanViewGroup())) {
      grouperGoogleGroup.setWhoCanViewGroup(grouperGoogleGroup.getWhoCanViewGroup());
    }
    if (StringUtils.isNotBlank(grouperGoogleGroup.getWhoCanInvite())) {
      grouperGoogleGroup.setWhoCanInvite(grouperGoogleGroup.getWhoCanInvite());
    }
    if (StringUtils.isNotBlank(grouperGoogleGroup.getWhoCanPostMessage())) {
      grouperGoogleGroup.setWhoCanPostMessage(grouperGoogleGroup.getWhoCanPostMessage());
    }
    if (grouperGoogleGroup.getAllowExternalMembers() != null) {
      grouperGoogleGroup.setAllowExternalMembers(grouperGoogleGroup.getAllowExternalMembers());
    }
    if (grouperGoogleGroup.getAllowWebPosting() != null) {
      grouperGoogleGroup.setAllowWebPosting(grouperGoogleGroup.getAllowWebPosting());
    }

    HibernateSession.byObjectStatic().saveOrUpdate(grouperGoogleGroup);
    
//    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
//    resultNode.put("stat", "OK");
//    ObjectNode objectNode = toGroupJson(grouperGoogleGroup);
    ObjectNode objectNode = grouperGoogleGroup.toJsonGroupOnly(null);
    
//    resultNode.set("response", objectNode);
    
//    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode)); 
  }
  
  private static long lastDeleteMillis = -1;
  
  public void postAuth(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    String grantType = mockServiceRequest.getHttpServletRequest().getParameter("grant_type");
    String assertion = mockServiceRequest.getHttpServletRequest().getParameter("assertion");
    
    if (StringUtils.isBlank(grantType) || StringUtils.isBlank(assertion)) {
      throw new RuntimeException("grant_type and assertion are required!");
    }
    
    if (!StringUtils.equals(grantType, "urn:ietf:params:oauth:grant-type:jwt-bearer")) {
      throw new RuntimeException("grant_type must be set to urn:ietf:params:oauth:grant-type:jwt-bearer");
    }
    
    DecodedJWT decodedJwt = JWT.decode(assertion);
    
    GoogleMockRsaKeyProvider googleMockRsaKeyProvider = new GoogleMockRsaKeyProvider();
    
    Algorithm.RSA256(googleMockRsaKeyProvider).verify(decodedJwt);
    
    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.google.mock.configId");

    mockServiceResponse.setResponseCode(200);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    //expires in an hour
    long expiresOnSeconds = System.currentTimeMillis()/1000 + 60*60;
    
    resultNode.put("expires_in", expiresOnSeconds);
    
    String accessToken = GrouperUuid.getUuid();
    
    GrouperGoogleAuth grouperGoogleAuth = new GrouperGoogleAuth();
    grouperGoogleAuth.setConfigId(configId);
    grouperGoogleAuth.setAccessToken(accessToken);
    grouperGoogleAuth.setExpiresInSeconds(expiresOnSeconds);
    HibernateSession.byObjectStatic().save(grouperGoogleAuth);
    
    resultNode.put("access_token", accessToken);
    
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    //delete if its been a while
    if (System.currentTimeMillis() - lastDeleteMillis > 1000*60*60) {
      lastDeleteMillis = System.currentTimeMillis();
      
      long secondsToDelete = System.currentTimeMillis()/1000 - 60*60;
      
      int accessTokensDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperGoogleAuth where expiresInSeconds < :theExpiresOnSeconds")
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
    .createQuery("delete from GrouperGoogleMembership where groupId = :groupId")
    .setString("groupId", groupId).executeUpdateInt();
    
    int groupsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperGoogleGroup where id = :theId")
        .setString("theId", groupId).executeUpdateInt();

//    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
//    resultNode.put("stat", "OK");
//    resultNode.put("response", "");
    
//    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
//    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
        
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
    .createQuery("delete from GrouperGoogleMembership where userId = :userId")
    .setString("userId", userId).executeUpdateInt();
    
    HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperGoogleUser where id = :theId")
        .setString("theId", userId).executeUpdateInt();

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    resultNode.put("response", "");
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode)); 
        
  }
  
  /**
   * convert from jackson json
   * @param grouperGoogleUser
   * @return the grouper google user
   */
  private static ObjectNode toUserJson(GrouperGoogleUser grouperGoogleUser) {
    
    ObjectNode result = GrouperUtil.jsonJacksonNode();
  
    /**
     * {
    "users": [
        {
            "id": "117982484919189471202",
            "primaryEmail": "liz@viveksachdeva.com",
            "name": {
                "givenName": "Elizabeth",
                "familyName": "Smith",
            }
        }
    ],
    "nextPageToken": "0a31f7feea7719ffffffff939685bf8996899a948c9e9c979b9a899ed19c9092ff00fefffecccbcacacccecfc9c6caccc6fffe100121b346550b02f34e663900000000e6881501480150005a0b09b623939b51a240e7100360bdc1d333720608e4eb9e8e06"
}
     */
    
    
    GrouperUtil.jsonJacksonAssignString(result, "id", grouperGoogleUser.getId());
    GrouperUtil.jsonJacksonAssignString(result, "primaryEmail", grouperGoogleUser.getPrimaryEmail());
    
    ObjectNode nameNode = GrouperUtil.jsonJacksonNode();
    GrouperUtil.jsonJacksonAssignString(nameNode, "givenName", grouperGoogleUser.getGivenName());
    GrouperUtil.jsonJacksonAssignString(nameNode, "familyName", grouperGoogleUser.getFamilyName());
    result.set("name", nameNode);
    
    return result;
  }
  
  class GoogleMockRsaKeyProvider implements RSAKeyProvider {
    
    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
      PublicKey publicKey = null;
      try {
        String publicKeyEncoded = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.google.mock.publicKey");
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
