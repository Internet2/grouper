package edu.internet2.middleware.grouper.app.adobe;

import java.util.HashSet;
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

public class AdobeMockServiceHandler extends MockServiceHandler {

  public AdobeMockServiceHandler() {
  }

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
  public static void ensureAdobeMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_adobe_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_adobe_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_adobe_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_adobe_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperAdobeGroup.createTableAdobeGroup(ddlVersionBean, database);
          GrouperAdobeAuth.createTableAdobeAuth(ddlVersionBean, database);
          GrouperAdobeUser.createTableAdobeUser(ddlVersionBean, database);
          GrouperAdobeMembership.createTableAdobeMembership(ddlVersionBean, database);
          
        }
      });
  
    }    
  }

  /**
   * 
   */
  public static void dropAdobeMockTables() {
//    MockServiceServlet.dropMockTable("mock_duo_membership");
//    MockServiceServlet.dropMockTable("mock_duo_user");
    MockServiceServlet.dropMockTable("mock_adobe_group");
//    MockServiceServlet.dropMockTable("mock_duo_auth");
  }
  
  private static boolean mockTablesThere = false;
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureAdobeMockTables();
    }
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    
//    GrouperUtil.assertion(mockNamePaths.size() >= 2, "Must start with admin/v1 or admin/v2");
//    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(0), "admin"), "");
//    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(1), "v1") || StringUtils.equals(mockNamePaths.get(1), "v2"), "");
    
    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);
    
    mockServiceRequest.setPostMockNamePaths(paths);

    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockNamePaths.get(0)) && 3 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroups(mockServiceRequest, mockServiceResponse,  mockNamePaths.get(2));
        return;
      }
      if ("groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getGroup(mockServiceRequest, mockServiceResponse);
        return;
      }

      if ("users".equals(mockNamePaths.get(0)) && 3 == mockNamePaths.size()) {
        getUsers(mockServiceRequest, mockServiceResponse, mockNamePaths.get(2));
        return;
      }
      if ("organizations".equals(mockNamePaths.get(0)) && "users".equals(mockNamePaths.get(2)) 
          && 4 == mockNamePaths.size()) {
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
      if ("users".equals(mockNamePaths.get(0)) && "groups".equals(mockNamePaths.get(2))
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
      
      if ("action".equals(mockNamePaths.get(0))) {
        handleAllActions(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("users".equals(mockNamePaths.get(0)) && "groups".equals(mockNamePaths.get(2))
          && 3 == mockNamePaths.size()) {
        associateGroupWithUser(mockServiceRequest, mockServiceResponse);
        return;
      }
//      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 4 == mockServiceRequest.getPostMockNamePaths().length
//          && "members".equals(mockServiceRequest.getPostMockNamePaths()[2]) && "$ref".equals(mockServiceRequest.getPostMockNamePaths()[3])) {
//        postMembership(mockServiceRequest, mockServiceResponse);
//        return;
//      }
//      if ("users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 3 == mockServiceRequest.getPostMockNamePaths().length
//          && "getMemberGroups".equals(mockServiceRequest.getPostMockNamePaths()[2])) {
//        postUserGroups(mockServiceRequest, mockServiceResponse);
//        return;
//      }
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
    ensureAdobeMockTables();
  }
  
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String bearerToken = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (!bearerToken.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization token must start with 'Basic '");
    }
    String authorizationToken = GrouperUtil.prefixOrSuffix(bearerToken, "Bearer ", false);
    

    List<GrouperAdobeAuth> grouperAdobeAuths = 
        HibernateSession.byHqlStatic().createQuery("from GrouperAdobeAuth where accessToken = :theAccessToken").setString("theAccessToken", authorizationToken).list(GrouperAdobeAuth.class);
    
    if (GrouperUtil.length(grouperAdobeAuths) != 1) {
      throw new RuntimeException("Invalid access token, not found!");
    }
    
    GrouperAdobeAuth grouperAdobeAuth = grouperAdobeAuths.get(0);    

//    if (grouperAdobeAuth.getExpiresOnSeconds() < System.currentTimeMillis()/1000) {
//      throw new RuntimeException("Invalid access token, expired!");
//    }

    // all good
  }

  private void checkRequestContentTypeAndDateHeader(MockServiceRequest mockServiceRequest) {
    if (!StringUtils.equals(mockServiceRequest.getHttpServletRequest().getContentType(), "application/x-www-form-urlencoded")) {
      throw new RuntimeException("Content type must be application/x-www-form-urlencoded");
    }
    
    if (StringUtils.isBlank(mockServiceRequest.getHttpServletRequest().getHeader("Date"))) {
      throw new RuntimeException("Date header must be there");
    }
  }

  public void getUsers(MockServiceRequest mockServiceRequest,
      MockServiceResponse mockServiceResponse, String pageNum) {
    
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String offset = pageNum;
    
    int limitInt = 100;
    
    int offsetInt = 0;
    int pageNumber = 1;
    if (StringUtils.isNotBlank(offset)) {
      offsetInt = GrouperUtil.intValue(offset);
      pageNumber = offsetInt/limitInt + 1;
    }
    
    List<GrouperAdobeUser> grouperAdobeUsers = null;
    
    String hql = "select distinct user from GrouperAdobeUser user";
    
    ByHqlStatic query = HibernateSession.byHqlStatic().createQuery(hql);
    
    QueryOptions queryOptions = new QueryOptions();
    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber, true);
    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    int totalRecordCount = queryOptions.getQueryPaging().getTotalRecordCount();
    
    grouperAdobeUsers = query.list(GrouperAdobeUser.class);
    
    /**
     * {
        "lastPage": false,
        "result": "success",
        "users": [{
          "id": "abc123",
          "email": "abc@school.edu",
          "status": "active",
          "groups": ["Group name 1", "Group name 2"],
          "username": "ABC@UPENN.EDU",
          "domain": "upenn.edu",
          "firstname": "Dave",
          "lastname": "Smith",
          "type": "federatedID",
          "country": "US"
        }]
       }
     */
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    resultNode.put("result", "success");
    if (queryPaging.getTotalRecordCount() > offsetInt + grouperAdobeUsers.size()) {
      resultNode.put("lastPage", false);
    } else {
      resultNode.put("lastPage", true);
    }
    
    for (GrouperAdobeUser grouperAdobeUser : grouperAdobeUsers) {
      ObjectNode userJsonNode = grouperAdobeUser.toJson(null);
      valueNode.add(userJsonNode);
      
      // get groups for the user if any
      List<GrouperAdobeMembership> grouperAdobeMemberships = HibernateSession.byHqlStatic().createQuery("select distinct m from GrouperAdobeMembership m where m.userId = :theUserId")
          .setString("theUserId", grouperAdobeUsers.get(0).getId()).list(GrouperAdobeMembership.class);
      
      Set<String> groupNames = new HashSet<String>();
      for (GrouperAdobeMembership membership: grouperAdobeMemberships) {
        Long groupId = membership.getGroupId();
        
        List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery("select distinct g from GrouperAdobeGroup g where g.id = :theGroupId")
            .setLong("theGroupId", groupId).list(GrouperAdobeGroup.class);
        
        if (grouperAdobeGroups.size() == 1) {
          groupNames.add(grouperAdobeGroups.get(0).getName());
        }
      }
      
      GrouperUtil.jsonJacksonAssignStringArray(userJsonNode, "groups", groupNames);
    }
    
    resultNode.set("users", valueNode);
//    if (queryPaging.getTotalRecordCount() > offsetInt + grouperAdobeUsers.size()) {
//      ObjectNode metadataNode = GrouperUtil.jsonJacksonNode();
//      metadataNode.put("next_offset", offsetInt + limitInt);
//      resultNode.set("metadata", metadataNode);
//    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }
  
  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse, String pageNum) {
    
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String offset = pageNum;
    
    int limitInt = 100;
    
    int offsetInt = 0;
    int pageNumber = 1;
    if (StringUtils.isNotBlank(offset)) {
      offsetInt = GrouperUtil.intValue(offset);
      pageNumber = offsetInt/limitInt + 1;
    }
    
    List<GrouperAdobeGroup> grouperAdobeGroups = null;
    
    String hql = "select distinct grp from GrouperAdobeGroup grp";
    
    ByHqlStatic query = HibernateSession.byHqlStatic().createQuery(hql);
    
    QueryOptions queryOptions = new QueryOptions();
    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber, true);
    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    int totalRecordCount = queryOptions.getQueryPaging().getTotalRecordCount();
    
    grouperAdobeGroups = query.list(GrouperAdobeGroup.class);
    
    /**
     * {
          "lastPage": true,
          "result": "success",
          "groups": [{
            "groupId": 4147407,
            "groupName": "_org_admin",
            "type": "SYSADMIN_GROUP",
            "memberCount": 23
          },
          {
            "groupId": 38324336,
            "groupName": "Default Spark with Premium Features for Higher-Ed - 2 GB configuration",
            "type": "PRODUCT_PROFILE",
            "productName": "Creative Cloud Shared Device Access for Higher Education (ETLA,ETLA - DD211C79AB1DB19CBD0A)",
            "licenseQuota": "UNLIMITED".  (integer or UNLIMITED), put this in Integer and -1 if UNLIMITED (null if not specified)
          }]
     */
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    resultNode.put("result", "success");
    if (queryPaging.getTotalRecordCount() > offsetInt + grouperAdobeGroups.size()) {
      resultNode.put("lastPage", false);
    } else {
      resultNode.put("lastPage", true);
    }
    
    for (GrouperAdobeGroup grouperAdobeGroup : grouperAdobeGroups) {
      valueNode.add(grouperAdobeGroup.toJson(null));
    }
    
    resultNode.set("groups", valueNode);
//    if (queryPaging.getTotalRecordCount() > offsetInt + grouperAdobeUsers.size()) {
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
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String orgId = mockServiceRequest.getPostMockNamePaths()[1];
    String userEmail = mockServiceRequest.getPostMockNamePaths()[3];
    
    GrouperUtil.assertion(GrouperUtil.length(userEmail) > 0, "userEmail is required");
    
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery("select distinct user from GrouperAdobeUser user where user.email = :theEmail")
        .setString("theEmail", userEmail).list(GrouperAdobeUser.class);

    if (GrouperUtil.length(grouperAdobeUsers) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
      resultNode.put("result", "success");
      ObjectNode objectNode = grouperAdobeUsers.get(0).toJson(null);
      
      // get groups for the user if any
      List<GrouperAdobeMembership> grouperAdobeMemberships = HibernateSession.byHqlStatic().createQuery("select distinct m from GrouperAdobeMembership m where m.userId = :theUserId")
          .setString("theUserId", grouperAdobeUsers.get(0).getId()).list(GrouperAdobeMembership.class);
      
      Set<String> groupNames = new HashSet<String>();
      for (GrouperAdobeMembership membership: grouperAdobeMemberships) {
        Long groupId = membership.getGroupId();
        
        List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery("select distinct g from GrouperAdobeGroup g where g.id = :theGroupId")
            .setLong("theGroupId", groupId).list(GrouperAdobeGroup.class);
        
        if (grouperAdobeGroups.size() == 1) {
          groupNames.add(grouperAdobeGroups.get(0).getName());
        }
      }
      
      GrouperUtil.jsonJacksonAssignStringArray(objectNode, "groups", groupNames);
      resultNode.set("user", objectNode);
      
//      {
//        "result": "success",
//        "user": {
//          "id": "abc123",
//          "email": "jsmith@school.edu",
//          "status": "active",
//          "groups": ["Group1", "Group2"],
//          "username": "JSMITH@SCHOOL.EDU",
//          "domain": "upenn.edu",
//          "firstname": "John",
//          "lastname": "SMITH",
//          "country": "US",
//          "type": "federatedID"
//        }
//      }
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(grouperAdobeUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("userByEmail: " + GrouperUtil.length(grouperAdobeUsers) + ", email: " + userEmail);
    }

  }
  
  
  public void disassociateGroupFromUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    String groupId = mockServiceRequest.getPostMockNamePaths()[3];
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    //check if userid exists
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery("select user from GrouperAdobeUser user where user.id = :theId")
        .setString("theId", userId).list(GrouperAdobeUser.class);
    
    if (GrouperUtil.length(grouperAdobeUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from GrouperAdobeMembership where userId = :userId and groupId = :groupId")
      .setString("userId", userId)
      .setString("groupId", groupId)
      .executeUpdateInt();

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    resultNode.put("response", "");
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode)); 
  }
  
  public void associateGroupWithUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);
    
    checkRequestContentTypeAndDateHeader(mockServiceRequest);

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    String groupId = mockServiceRequest.getHttpServletRequest().getParameter("group_id");
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "group_id is required");
    
    //check if userid exists
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery("select user from GrouperAdobeUser user where user.id = :theId")
        .setString("theId", userId).list(GrouperAdobeUser.class);
    
    if (GrouperUtil.length(grouperAdobeUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    //check if group exists
    List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup where group_id = :theId")
        .setString("theId", groupId).list(GrouperAdobeGroup.class);
    
    if (GrouperUtil.length(grouperAdobeGroups) == 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    // check if user has already 100 or more groups
    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from GrouperAdobeGroup g where g.id in (select m.groupId from GrouperAdobeMembership m where m.userId = :theUserId) ")
        .setString("theUserId", userId);
    
    QueryOptions queryOptions = new QueryOptions();
    QueryPaging queryPaging = QueryPaging.page(1, 0, true);
    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    grouperAdobeGroups = query.list(GrouperAdobeGroup.class);
    if (queryPaging.getTotalRecordCount() >= 100) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    //check if this groupId and userId are already connected
    List<GrouperAdobeMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from GrouperAdobeMembership m where m.userId = :userId and m.groupId = :groupId ")
        .setString("userId", userId)
        .setString("groupId", groupId)
        .list(GrouperAdobeMembership.class);
    
    if (GrouperUtil.length(memberships) == 0) {
      
      //now save the relationship
      GrouperAdobeMembership grouperAdobeMembership = new GrouperAdobeMembership();
      grouperAdobeMembership.setGroupId(Long.valueOf(groupId));
      grouperAdobeMembership.setUserId(userId);
      grouperAdobeMembership.setId(GrouperUuid.getUuid());
      
      HibernateSession.byObjectStatic().save(grouperAdobeMembership); 
      
    }
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    resultNode.put("response", "");
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode)); 
  }
  
  public void updateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentTypeAndDateHeader(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }


    // patch a user
    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("userId", userId);

    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAdobeUser where id = :theId")
        .setString("theId", userId).list(GrouperAdobeUser.class);
    
    if (GrouperUtil.length(grouperAdobeUsers) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindUser", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperAdobeUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(grouperAdobeUsers));
    }
    
    String userName = mockServiceRequest.getHttpServletRequest().getParameter("username");
    String realName = mockServiceRequest.getHttpServletRequest().getParameter("realname");
    String email = mockServiceRequest.getHttpServletRequest().getParameter("email");
    String firstName = mockServiceRequest.getHttpServletRequest().getParameter("firstname");
    String lastName = mockServiceRequest.getHttpServletRequest().getParameter("lastname");

    String alias1 = mockServiceRequest.getHttpServletRequest().getParameter("alias1");
    String alias2 = mockServiceRequest.getHttpServletRequest().getParameter("alias2");
    String alias3 = mockServiceRequest.getHttpServletRequest().getParameter("alias3");
    String alias4 = mockServiceRequest.getHttpServletRequest().getParameter("alias4");
    
    
    GrouperAdobeUser grouperAdobeUser = grouperAdobeUsers.get(0);

    if (StringUtils.isNotBlank(userName)) {
      
      //check if the new username is already taken
      List<GrouperAdobeUser> existingUsersWithSameUserName = HibernateSession.byHqlStatic().createQuery("select user from GrouperAdobeUser user where user.userName = :userName ")
          .setString("userName", userName)
          .list(GrouperAdobeUser.class);
      
      if (existingUsersWithSameUserName != null && existingUsersWithSameUserName.size() > 0) {
        
        for (GrouperAdobeUser existingUser: existingUsersWithSameUserName) {
          if (StringUtils.equals(userName, existingUser.getUserName()) 
              && !StringUtils.equals(existingUser.getId(), userId) ) {
            mockServiceRequest.getDebugMap().put("usernameAlreadyExists", true);
            mockServiceResponse.setResponseCode(404);
            return;
          }
        }
       
      }
      grouperAdobeUser.setUserName(userName);
    }
    if (StringUtils.isNotBlank(firstName)) {
      grouperAdobeUser.setFirstName(firstName);
    }
    if (StringUtils.isNotBlank(lastName)) {
      grouperAdobeUser.setLastName(lastName);
    }
    if (StringUtils.isNotBlank(email)) {
      grouperAdobeUser.setEmail(email);
    }
    
    HibernateSession.byObjectStatic().saveOrUpdate(grouperAdobeUser);
    
    // we want users in response
    getUser(mockServiceRequest, mockServiceResponse);
    
  }
  
  public void handleAllActions(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String jsonString = mockServiceRequest.getRequestBody();
    ArrayNode arrayNode = (ArrayNode) GrouperUtil.jsonJacksonNode(jsonString);
    
    int completed = 0;

    for (int i=0;i<arrayNode.size();i++) {
      JsonNode jsonNode = arrayNode.get(i);
      
      if (jsonNode.has("user")) {
        crudOnUser(jsonNode, mockServiceResponse);
      } else if (jsonNode.has("usergroup")) {
        crudOnUserGroup(jsonNode, mockServiceResponse);
      }
      completed++;
      
    }
    
    int notCompleted = 0;
    int completedInTestMode = 0;
    
//    [
//     {
//       "user": "abc@school.edu",
//       "do": [
//         {
//           "addAdobeID|createFederatedID|createEnterpriseID": {
//             "email": "abc@school.edu",
//             "country": "US",
//             "firstname": "AbcTest",
//             "lastname": "AbcTest"
//           }
//         }
//       ]
//     }
//   ]
    
//    System.out.println(jsonString);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("completed", completed);
    resultNode.put("notCompleted", 0);
    resultNode.put("completedInTestMode", 0);
    resultNode.put("result", "success");
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
//    {"completed":1,"notCompleted":0,"completedInTestMode":0,"result":"success"}
    
  }
  
  public void crudOnUserGroup(JsonNode jsonNode, MockServiceResponse mockServiceResponse) {
    
    String groupName = GrouperUtil.jsonJacksonGetString(jsonNode, "usergroup");
    ArrayNode operations = GrouperUtil.jsonJacksonGetArrayNode(jsonNode, "do");
    
    JsonNode oneOperation = operations.get(0);
    
    if (oneOperation.has("createUserGroup")) {
      createGroup(oneOperation.get("createUserGroup"), groupName);
    } else if (oneOperation.has("deleteUserGroup")) {
      deleteGroup(groupName);
    } else if (oneOperation.has("updateUserGroup")) {
      updateGroup(oneOperation.get("updateUserGroup"), groupName);
    }
  }
  
  public void updateGroup(JsonNode jsonNode, String oldGroupName) {
    /**
     * [
      {
        "usergroup": "OLD_GROUP_NAME",
        "do": [
                {
                  "updateUserGroup": {
                    "name": "NEW_GROUP_NAME"
                  }
                }
             ]
      }
    ]
     */
    
    List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAdobeGroup where name = :name")
        .setString("name", oldGroupName).list(GrouperAdobeGroup.class);
    
    if (GrouperUtil.length(grouperAdobeGroups) == 0) {
      throw new RuntimeException("Can't find group");
    }
    if (GrouperUtil.length(grouperAdobeGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperAdobeGroups));
    }
    
    GrouperAdobeGroup grouperAdobeGroup = grouperAdobeGroups.get(0);
    if (jsonNode.has("name")) {
      grouperAdobeGroup.setName(GrouperUtil.jsonJacksonGetString(jsonNode, "name"));
    }
    HibernateSession.byObjectStatic().update(grouperAdobeGroup);
  }
  
  public void deleteGroup(String groupName) {
    /**
     * [
        {
          "usergroup": "GROUP_NAME",
          "do": [
                  {
                    "deleteUserGroup": {
                    }
                  }
               ]
        }
      ]
     */
    
    List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAdobeGroup where name = :name")
        .setString("name", groupName).list(GrouperAdobeGroup.class);
    
    if (GrouperUtil.length(grouperAdobeGroups) == 0) {
      throw new RuntimeException("Can't find group");
    }
    if (GrouperUtil.length(grouperAdobeGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperAdobeGroups));
    }
    
    GrouperAdobeGroup grouperAdobeGroup = grouperAdobeGroups.get(0);
    HibernateSession.byHqlStatic()
    .createQuery("delete from GrouperAdobeGroup where id = :theGroupId")
    .setLong("theGroupId", grouperAdobeGroup.getId())
    .executeUpdateInt();
  }
  
  public void createGroup(JsonNode jsonNode, String groupName) {
    
    /**
     * [
        {
          "usergroup": "myTestGroup",
          "do": [
                  {
                    "createUserGroup": {
                      "name": "myTestGroup",
                      "option": "ignoreIfAlreadyExists"
                    }
                  }
               ]
        }
      ]
     */
    
    GrouperAdobeGroup grouperAdobeGroup = new GrouperAdobeGroup();
    grouperAdobeGroup.setId(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE));
    grouperAdobeGroup.setName(groupName);
//    grouperAdobeGroup.setProductName(productName);
    
    List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery("select group from GrouperAdobeGroup group where group.name = :name")
        .setString("name", groupName)
        .list(GrouperAdobeGroup.class);
    
    if (grouperAdobeGroups != null && grouperAdobeGroups.size() > 0) {
      throw new RuntimeException("Group already exists with name: "+groupName);
    }
     
    HibernateSession.byObjectStatic().save(grouperAdobeGroup);
  }
  
  public void crudOnUser(JsonNode jsonNode, MockServiceResponse mockServiceResponse) {
    
    String email = GrouperUtil.jsonJacksonGetString(jsonNode, "user");
    ArrayNode operations = GrouperUtil.jsonJacksonGetArrayNode(jsonNode, "do");
    
    JsonNode oneOperation = operations.get(0);
    
    if (oneOperation.has("addAdobeID")) {
       createUser(oneOperation.get("addAdobeID"), email, "adobeID");
    } else if (oneOperation.has("createFederatedID")) {
       createUser(oneOperation.get("createFederatedID"), email, "federatedID");
    } else if (oneOperation.has("createEnterpriseID")) {
       createUser(oneOperation.get("createEnterpriseID"), email, "enterpriseID");
    } else if (oneOperation.has("update")) {
      updateUser(oneOperation.get("update"), email);
    } else if (oneOperation.has("add")) {
      addUserToGroups(oneOperation.get("add"), email);
    } else if (oneOperation.has("remove")) {
      removeUserFromGroups(oneOperation.get("remove"), email);
    } else if (oneOperation.has("removeFromOrg")) {
      removeUser(email);
    }
    
  }
  
  public void removeUser(String email) {
    /**
     * [
        {
          "user": "abc@upenn.edu",
          "do": [
            {
              "removeFromOrg": {
                "deleteAccount": true/false
              }
            }
          ]
        }
      ]
     */
    
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAdobeUser where email = :email")
        .setString("email", email).list(GrouperAdobeUser.class);
    
    if (GrouperUtil.length(grouperAdobeUsers) == 0) {
      throw new RuntimeException("Can't find user");
    }
    if (GrouperUtil.length(grouperAdobeUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(grouperAdobeUsers));
    }
    
    GrouperAdobeUser grouperAdobeUser = grouperAdobeUsers.get(0);
    HibernateSession.byHqlStatic()
    .createQuery("delete from GrouperAdobeUser where user_id = :theUserId")
    .setString("theUserId", grouperAdobeUser.getId())
    .executeUpdateInt();
    
  }
  
  public void removeUserFromGroups(JsonNode jsonNode, String email) {
    
    /**
     * [
        {
          "user": "abc@upenn.edu",
          "do": [
            {
              "remove": {
                "group": [
                  "HireIT ISC - CCE Pro - Acrobat Pro DC"
                ]
              }
            }
          ]
        }
      ]
     */
    
    Set<String> groups = GrouperUtil.jsonJacksonGetStringSet(jsonNode, "group");
    
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAdobeUser where email = :email")
        .setString("email", email).list(GrouperAdobeUser.class);
    
    if (GrouperUtil.length(grouperAdobeUsers) == 0) {
      throw new RuntimeException("Can't find user");
    }
    if (GrouperUtil.length(grouperAdobeUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(grouperAdobeUsers));
    }
    
    GrouperAdobeUser grouperAdobeUser = grouperAdobeUsers.get(0);
    
    for (String group: groups) {
      List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery("select group from GrouperAdobeGroup group where group.name = :groupName")
          .setString("groupName", group)
          .list(GrouperAdobeGroup.class);
      
      if (grouperAdobeGroups.size() == 0 || grouperAdobeGroups.size() > 1) {
        throw new RuntimeException("Zero or more than 1 group for "+group);
      }
      
      
     HibernateSession.byHqlStatic()
          .createQuery("delete from GrouperAdobeMembership where groupId = :theGroupId and userId = :theUserId")
          .setLong("theGroupId", grouperAdobeGroups.get(0).getId())
          .setString("theUserId", grouperAdobeUser.getId())
          .executeUpdateInt();
    }
  }
  
  public void addUserToGroups(JsonNode jsonNode, String email) {
    /**
     * [
        {
          "user": "abc@upenn.edu",
          "do": [
            {
              "add": {
                "group": [
                  "HireIT ISC - CCE Pro - Acrobat Pro DC"
                ]
              }
            }
          ]
        }
      ]
     */
    
    Set<String> groups = GrouperUtil.jsonJacksonGetStringSet(jsonNode, "group");
    
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAdobeUser where email = :email")
        .setString("email", email).list(GrouperAdobeUser.class);
    
    if (GrouperUtil.length(grouperAdobeUsers) == 0) {
      throw new RuntimeException("Can't find user");
    }
    if (GrouperUtil.length(grouperAdobeUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(grouperAdobeUsers));
    }
    
    GrouperAdobeUser grouperAdobeUser = grouperAdobeUsers.get(0);
    
    for (String group: groups) {
      List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery("select group from GrouperAdobeGroup group where group.name = :groupName")
          .setString("groupName", group)
          .list(GrouperAdobeGroup.class);
      
      if (grouperAdobeGroups.size() == 0 || grouperAdobeGroups.size() > 1) {
        throw new RuntimeException("Zero or more than 1 group for "+group);
      }
      
      GrouperAdobeMembership membership = new GrouperAdobeMembership();
      membership.setGroupId(grouperAdobeGroups.get(0).getId());
      membership.setUserId(grouperAdobeUser.getId());
      membership.setId(GrouperUuid.getUuid());
      HibernateSession.byObjectStatic().save(membership);
    }
    
  }
  
  public void updateUser(JsonNode jsonNode, String email) {
    
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAdobeUser where email = :email")
        .setString("email", email).list(GrouperAdobeUser.class);
    
    if (GrouperUtil.length(grouperAdobeUsers) == 0) {
      throw new RuntimeException("Can't find user");
    }
    if (GrouperUtil.length(grouperAdobeUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(grouperAdobeUsers));
    }
    
    GrouperAdobeUser grouperAdobeUser = grouperAdobeUsers.get(0);
    if (jsonNode.has("firtname")) {
      grouperAdobeUser.setFirstName(GrouperUtil.jsonJacksonGetString(jsonNode, "firstname"));
    }
    if (jsonNode.has("lastname")) {
      grouperAdobeUser.setLastName(GrouperUtil.jsonJacksonGetString(jsonNode, "lastname"));
    }
    if (jsonNode.has("country")) {
      grouperAdobeUser.setCountry(GrouperUtil.jsonJacksonGetString(jsonNode, "country"));
    }
    if (jsonNode.has("email")) {
      grouperAdobeUser.setEmail(GrouperUtil.jsonJacksonGetString(jsonNode, "email"));
    }
    
    HibernateSession.byObjectStatic().saveOrUpdate(grouperAdobeUser);    
    
  }
  
  public void createUser(JsonNode jsonNode, String email, String type) {
    
    GrouperAdobeUser grouperAdobeUser = new GrouperAdobeUser();
    grouperAdobeUser.setId(GrouperUuid.getUuid());
    grouperAdobeUser.setEmail(email);
//    grouperAdobeUser.setUserName(userName);
    
    grouperAdobeUser.setFirstName(GrouperUtil.jsonJacksonGetString(jsonNode, "firstname"));
    grouperAdobeUser.setLastName(GrouperUtil.jsonJacksonGetString(jsonNode, "lastname"));
    grouperAdobeUser.setCountry(GrouperUtil.jsonJacksonGetString(jsonNode, "country"));
    grouperAdobeUser.setType(type);
    
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery("select user from GrouperAdobeUser user where user.email = :email ")
        .setString("email", grouperAdobeUser.getEmail())
        .list(GrouperAdobeUser.class);
    
    if (grouperAdobeUsers != null && grouperAdobeUsers.size() > 0) {
      throw new RuntimeException("User already exists with email: "+email);
    }
     
    HibernateSession.byObjectStatic().save(grouperAdobeUser);    
  }
  
  public void postGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentTypeAndDateHeader(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    
    String groupName = mockServiceRequest.getHttpServletRequest().getParameter("name");
    if (StringUtils.isBlank(groupName)) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    String desc = mockServiceRequest.getHttpServletRequest().getParameter("desc");
    
    GrouperAdobeGroup grouperAdobeGroup = new GrouperAdobeGroup();
    grouperAdobeGroup.setId(123L);//TODO: generate random long value
    grouperAdobeGroup.setName(groupName);
    
    HibernateSession.byObjectStatic().save(grouperAdobeGroup);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    ObjectNode objectNode = grouperAdobeGroup.toJson(null);
    
    resultNode.set("response", objectNode);
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    
  }

  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String offset = mockServiceRequest.getHttpServletRequest().getParameter("offset");
    String limit = mockServiceRequest.getHttpServletRequest().getParameter("limit");
    
    int limitInt = 100;
    if (StringUtils.isNotBlank(limit)) {
      limitInt = GrouperUtil.intValue(limit);
      if (limitInt <= 0) {
        throw new RuntimeException("limit cannot be less than or equal to 0.");
      }
    }
    
    int offsetInt = 0;
    int pageNumber = 1;
    if (StringUtils.isNotBlank(offset)) {
      offsetInt = GrouperUtil.intValue(offset);
      pageNumber = offsetInt/limitInt + 1;
    }
    
    List<GrouperAdobeGroup> grouperAdobeGroups = null;
    
    ByHqlStatic query = HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup");
    
    QueryOptions queryOptions = new QueryOptions();
//    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber, true);
//    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    grouperAdobeGroups = query.list(GrouperAdobeGroup.class);

    /**
     * {
  "lastPage": true,
  "result": "success",
  "groups": [{
    "groupId": 4147407,
    "groupName": "_org_admin",
    "type": "SYSADMIN_GROUP",
    "memberCount": 23
    },
    {
      "groupId": 38324336,
      "groupName": "Default Spark with Premium Features for Higher-Ed - 2 GB configuration",
      "type": "PRODUCT_PROFILE",
      "productName": "Creative Cloud Shared Device Access for Higher Education (ETLA,ETLA - DD211C79AB1DB19CBD0A)",
      "licenseQuota": "UNLIMITED".  (integer or UNLIMITED), put this in Integer and -1 if UNLIMITED (null if not specified)
    }
  }
     */
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    resultNode.put("lastPage", true);
    resultNode.put("result", "success");
    
    for (GrouperAdobeGroup grouperAdobeGroup : grouperAdobeGroups) {
      valueNode.add(grouperAdobeGroup.toJson(null));
    }
    
    resultNode.set("groups", valueNode);
//    if (queryPaging.getTotalRecordCount() > offsetInt + grouperAdobeGroups.size()) {
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
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup where group_id = :theId")
        .setString("theId", groupId).list(GrouperAdobeGroup.class);

    if (GrouperUtil.length(grouperAdobeGroups) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");

      
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
      resultNode.put("stat", "OK");
      ObjectNode objectNode = grouperAdobeGroups.get(0).toJson(null);
      
      resultNode.set("response", objectNode);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(grouperAdobeGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(grouperAdobeGroups) + ", id: " + groupId);
    }

  }
  
  private static long lastDeleteMillis = -1;
  
  
  public void postAuth(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    String grantType = mockServiceRequest.getHttpServletRequest().getParameter("grant_type");
    String clientId = mockServiceRequest.getHttpServletRequest().getParameter("client_id");
    String clientSecret = mockServiceRequest.getHttpServletRequest().getParameter("client_secret");
    
    if (StringUtils.isBlank(grantType) || StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
      throw new RuntimeException("grant_type, client_id, and client_secret are required!");
    }
    
    if (!StringUtils.equals(grantType, "client_credentials")) {
      throw new RuntimeException("grant_type must be set to client_credentials");
    }
    
    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.adobe.mock.configId", "adobe");
    
    if (StringUtils.equals(grantType, "client_credentials")) {
      
      String expectedClientId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + configId + ".clientId");
      if (StringUtils.isBlank(expectedClientId)) {
        expectedClientId = "put client id here that you have in adobe external system";
      }

      String expectedClientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + configId + ".clientSecret");
      if (StringUtils.isBlank(expectedClientSecret)) {
        expectedClientSecret = "put client secret here that you have in adobe external system";
      }
      
      if (!StringUtils.equals(expectedClientId, clientId) && !StringUtils.equals(expectedClientSecret, clientSecret)) {
        throw new RuntimeException("client id and/or client secret don't match");
      }
      
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
      long expiresOnSeconds = System.currentTimeMillis()/1000 + 60;
      
      resultNode.put("expires_in", expiresOnSeconds);
      
      String accessToken = GrouperUuid.getUuid();
      
      GrouperAdobeAuth grouperAdobeAuth = new GrouperAdobeAuth();
      grouperAdobeAuth.setConfigId(configId);
      grouperAdobeAuth.setAccessToken(accessToken);
      grouperAdobeAuth.setExpiresOnSeconds(expiresOnSeconds);
      HibernateSession.byObjectStatic().save(grouperAdobeAuth);
      
      resultNode.put("access_token", accessToken);
      
      mockServiceResponse.setResponseCode(200);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
      
    }
    
    //delete if its been a while
    if (System.currentTimeMillis() - lastDeleteMillis > 1000*60*60) {
      lastDeleteMillis = System.currentTimeMillis();
      
      long secondsToDelete = 60*60;
      
      int accessTokensDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAdobeAuth where expiresOnSeconds < :theExpiresOnSeconds")
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
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");

    HibernateSession.byHqlStatic()
    .createQuery("delete from GrouperAdobeMembership where groupId = :groupId")
    .setString("groupId", groupId).executeUpdateInt();
    
    int groupsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAdobeGroup where group_id = :theId")
        .setString("theId", groupId).executeUpdateInt();

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    resultNode.put("response", "");
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
        
  }
  
  public void deleteUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");

    HibernateSession.byHqlStatic()
    .createQuery("delete from GrouperAdobeMembership where userId = :userId")
    .setString("userId", userId).executeUpdateInt();
    
    HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperAdobeUser where id = :theId")
        .setString("theId", userId).executeUpdateInt();

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    resultNode.put("response", "");
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode)); 
        
  }

}
