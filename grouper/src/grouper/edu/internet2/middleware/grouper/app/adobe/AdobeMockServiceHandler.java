package edu.internet2.middleware.grouper.app.adobe;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang.StringUtils;

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
import edu.internet2.middleware.morphString.Morph;

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
//      new GcDbAccess().sql("select count(*) from mock_duo_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_adobe_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperAdobeGroup.createTableAdobeGroup(ddlVersionBean, database);
//          GrouperAzureAuth.createTableAzureAuth(ddlVersionBean, database);
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
    
    mockNamePaths = mockNamePaths.subList(2, mockNamePaths.size());
    
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
      
      if ("groups".equals(mockNamePaths.get(0)) && "users".equals(mockNamePaths.get(2)) && 3 == mockNamePaths.size()) {
        getUsersByGroup(mockServiceRequest, mockServiceResponse);
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
      if ("users".equals(mockNamePaths.get(0)) && "groups".equals(mockNamePaths.get(2))
          && 4 == mockNamePaths.size()) {
        disassociateGroupFromUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("auth".equals(mockNamePaths.get(0))) {
        postAuth(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateGroup(mockServiceRequest, mockServiceResponse);
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
    if (!bearerToken.startsWith("Basic ")) {
      throw new RuntimeException("Authorization token must start with 'Basic '");
    }
    String authorizationToken = GrouperUtil.prefixOrSuffix(bearerToken, "Basic ", false);
    
    String credentials = "";
    try {
      credentials = new String(Base64.getDecoder().decode(authorizationToken), "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      throw new RuntimeException(e1);
    }
    int colonIndex = credentials.indexOf(":");
    GrouperUtil.assertion(colonIndex != -1, "Need to pass in integrationKey and password in Authorization header");
    String integrationKey = credentials.substring(0, colonIndex).trim();
    
    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.duo.mock.configId");
    if (StringUtils.isBlank(configId)) {
      configId = "duo1";
    }
    String expectedIntegrationKey = GrouperConfig.retrieveConfig().propertyValueString("grouper.duoConnector."+configId+".adminIntegrationKey");
    if (StringUtils.isBlank(expectedIntegrationKey)) {
      expectedIntegrationKey = "DI3GFYRTLYKA0J3E3U1H";
    }
    
    String adminDomainName = GrouperConfig.retrieveConfig().propertyValueString("grouper.duoConnector."+configId+".adminDomainName");
    if (StringUtils.isBlank(adminDomainName)) {
      String uiUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.ui.url", "http://localhost:8080/grouper");
      uiUrl = GrouperUtil.stripLastSlashIfExists(uiUrl);
      uiUrl = GrouperUtil.prefixOrSuffix(uiUrl, "http://", false);
      uiUrl = GrouperUtil.prefixOrSuffix(uiUrl, "https://", false);
      adminDomainName = uiUrl + "/mockServices/duo";
    }
    if (!StringUtils.equals(expectedIntegrationKey, integrationKey)) {
      throw new RuntimeException("Integration key does not match with what is in grouper config");
    }
    
    String password = credentials.substring(colonIndex + 1).trim();
    
    String date = mockServiceRequest.getHttpServletRequest().getHeader("Date");
    String method = mockServiceRequest.getHttpServletRequest().getMethod().toUpperCase();
    
    String path = "/"+mockServiceRequest.getPostMockNamePath();
    
    Map<String, String> paramNamesToValues = new TreeMap<String, String>();
    Enumeration<String> parameterNames = mockServiceRequest.getHttpServletRequest().getParameterNames();
    while (parameterNames.hasMoreElements()) {
      
      String paramName = parameterNames.nextElement();
      
      String value = mockServiceRequest.getHttpServletRequest().getParameter(paramName);
      paramNamesToValues.put(paramName, value);
      
    }
    
    String paramsLine = "";
    if (paramNamesToValues.size() > 0) {
      for (String paramName: paramNamesToValues.keySet()) {
        if (StringUtils.isNotBlank(paramsLine)) {
          paramsLine += "&";
        }
        paramsLine = paramsLine + GrouperUtil.escapeUrlEncode(paramName).replace("+", "%20") + "="+ GrouperUtil.escapeUrlEncode(paramNamesToValues.get(paramName)).replace("+", "%20");
        
      }
    }
    
    String hmacSource = date + "\n" + method + "\n" + adminDomainName + "\n" + path + "\n" + paramsLine;
    
    String adminSecretKey = GrouperConfig.retrieveConfig().propertyValueString("grouper.duoConnector."+configId+".adminSecretKey");
    if (StringUtils.isBlank(adminSecretKey)) {
      adminSecretKey = "PxtwEr5XxxpGHYxj39vQnmjtPKEq1G1rurdwH7N5";
    }
    
    String hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, adminSecretKey).hmacHex(hmacSource);
    if (!StringUtils.equals(hmac, password)) {
      throw new RuntimeException("hmac1 password does not match: "+StringUtils.abbreviate(hmac, 10));
    }
//    List<GrouperAzureAuth> grouperAzureAuths = 
//        HibernateSession.byHqlStatic().createQuery("from GrouperAzureAuth where accessToken = :theAccessToken").setString("theAccessToken", authorizationToken).list(GrouperAzureAuth.class);
//    
//    if (GrouperUtil.length(grouperAzureAuths) != 1) {
//      throw new RuntimeException("Invalid access token, not found!");
//    }
//    
//    GrouperAzureAuth grouperAzureAuth = grouperAzureAuths.get(0);    
//
//    if (grouperAzureAuth.getExpiresOnSeconds() < System.currentTimeMillis()/1000) {
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

  public void getUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
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
      if (limitInt > 300) {
        limitInt = 300;
      }
    }
    
    int offsetInt = 0;
    int pageNumber = 1;
    if (StringUtils.isNotBlank(offset)) {
      offsetInt = GrouperUtil.intValue(offset);
      pageNumber = offsetInt/limitInt + 1;
    }
    
    List<GrouperAdobeUser> grouperAdobeUsers = null;
    
    String hql = "select distinct user from GrouperAdobeUser user left join user.groups groups";
    
    String userName = mockServiceRequest.getHttpServletRequest().getParameter("username");
    if (!StringUtils.isBlank(userName)) {
      hql += " where user.userName = :userName";
    }
    
    ByHqlStatic query = HibernateSession.byHqlStatic().createQuery(hql);
    if (!StringUtils.isBlank(userName)) {
      query.setString("userName", userName);
    }
    
    QueryOptions queryOptions = new QueryOptions();
    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber, true);
    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    grouperAdobeUsers = query.list(GrouperAdobeUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    resultNode.put("stat", "OK");
    
    for (GrouperAdobeUser grouperAdobeUser : grouperAdobeUsers) {
      valueNode.add(toUserJson(grouperAdobeUser));
    }
    
    resultNode.set("response", valueNode);
    if (queryPaging.getTotalRecordCount() > offsetInt + grouperAdobeUsers.size()) {
      ObjectNode metadataNode = GrouperUtil.jsonJacksonNode();
      metadataNode.put("next_offset", offsetInt + limitInt);
      resultNode.set("metadata", metadataNode);
    }
    
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

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery("select distinct user from GrouperAdobeUser user left join user.groups groups where user.id = :theId")
        .setString("theId", userId).list(GrouperAdobeUser.class);

    if (GrouperUtil.length(grouperAdobeUsers) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
      resultNode.put("stat", "OK");
      ObjectNode objectNode = toUserJson(grouperAdobeUsers.get(0));
      
      resultNode.set("response", objectNode);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(grouperAdobeUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("userById: " + GrouperUtil.length(grouperAdobeUsers) + ", id: " + userId);
    }

  }
  
  public void getGroupsByUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
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
        .createQuery("from GrouperAdobeGroup g where g.id in (select m.groupId from GrouperAdobeMembership m where m.userId = :theUserId) ")
        .setString("theUserId", userId);
    
    QueryOptions queryOptions = new QueryOptions();
    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber , true);
    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    List<GrouperAdobeGroup> grouperAdobeGroups = query.list(GrouperAdobeGroup.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    resultNode.put("stat", "OK");
    
    for (GrouperAdobeGroup grouperAdobeGroup : grouperAdobeGroups) {
      valueNode.add(toGroupJson(grouperAdobeGroup));
    }
    
    resultNode.set("response", valueNode);
    if (queryPaging.getTotalRecordCount() > offsetInt + grouperAdobeGroups.size()) {
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
      grouperAdobeMembership.setGroupId(groupId);
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
  
  public void postUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentTypeAndDateHeader(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    
    String userName = mockServiceRequest.getHttpServletRequest().getParameter("username");
    if (StringUtils.isBlank(userName)) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    String realName = mockServiceRequest.getHttpServletRequest().getParameter("realname");
    String email = mockServiceRequest.getHttpServletRequest().getParameter("email");
    String firstName = mockServiceRequest.getHttpServletRequest().getParameter("firstname");
    String lastName = mockServiceRequest.getHttpServletRequest().getParameter("lastname");

    String alias1 = mockServiceRequest.getHttpServletRequest().getParameter("alias1");
    String alias2 = mockServiceRequest.getHttpServletRequest().getParameter("alias2");
    String alias3 = mockServiceRequest.getHttpServletRequest().getParameter("alias3");
    String alias4 = mockServiceRequest.getHttpServletRequest().getParameter("alias4");
    

    GrouperAdobeUser grouperAdobeUser = new GrouperAdobeUser();
    grouperAdobeUser.setId(GrouperUuid.getUuid());
    grouperAdobeUser.setEmail(email);
    grouperAdobeUser.setUserName(userName);
    grouperAdobeUser.setFirstName(firstName);
    grouperAdobeUser.setLastName(lastName);
    
    List<GrouperAdobeUser> grouperAdobeUsers = HibernateSession.byHqlStatic().createQuery("select user from GrouperAdobeUser user where user.userName = :userName ")
        .setString("userName", grouperAdobeUser.getUserName())
        .list(GrouperAdobeUser.class);
    
    if (grouperAdobeUsers != null && grouperAdobeUsers.size() > 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
     
    HibernateSession.byObjectStatic().save(grouperAdobeUser);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    ObjectNode objectNode = toUserJson(grouperAdobeUser);
    
    resultNode.set("response", objectNode);
    
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
    ObjectNode objectNode = toGroupJson(grouperAdobeGroup);
    
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
      valueNode.add(toGroupJson(grouperAdobeGroup));
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
  
  
  public void getUsersByGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
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
    
    List<GrouperAdobeUser> grouperAdobeUsers = null;
    
    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from GrouperAdobeUser u where u.id in (select m.userId from GrouperAdobeMembership m where m.groupId = :theGroupId) ")
        .setString("theGroupId", groupId);
    
    QueryOptions queryOptions = new QueryOptions();
    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber , true);
    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    grouperAdobeUsers = query.list(GrouperAdobeUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    resultNode.put("stat", "OK");
    
    for (GrouperAdobeUser grouperAdobeUser : grouperAdobeUsers) {
      valueNode.add(toUserJson(grouperAdobeUser));
    }
    
    resultNode.set("response", valueNode);
    if (queryPaging.getTotalRecordCount() > offsetInt + grouperAdobeUsers.size()) {
      ObjectNode metadataNode = GrouperUtil.jsonJacksonNode();
      metadataNode.put("next_offset", offsetInt + limitInt);
      resultNode.set("metadata", metadataNode);
    }
    
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
      ObjectNode objectNode = toGroupJson(grouperAdobeGroups.get(0));
      
      resultNode.set("response", objectNode);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(grouperAdobeGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(grouperAdobeGroups) + ", id: " + groupId);
    }

  }
  
  public void updateGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentTypeAndDateHeader(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }


    // patch a group
    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("groupId", groupId);

    List<GrouperAdobeGroup> grouperAdobeGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperAdobeGroup where group_id = :theId")
        .setString("theId", groupId).list(GrouperAdobeGroup.class);
    
    if (GrouperUtil.length(grouperAdobeGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperAdobeGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperAdobeGroups));
    }
    GrouperAdobeGroup grouperAdobeGroup = grouperAdobeGroups.get(0);
    
    String groupName = mockServiceRequest.getHttpServletRequest().getParameter("name");
    if (StringUtils.isNotBlank(groupName)) {
      grouperAdobeGroup.setName(groupName);
    }
    

    HibernateSession.byObjectStatic().saveOrUpdate(grouperAdobeGroup);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    ObjectNode objectNode = toGroupJson(grouperAdobeGroup);
    
    resultNode.set("response", objectNode);
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode)); 
  }
  
  private static long lastDeleteMillis = -1;
  
  public void postAuth(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    String clientId = mockServiceRequest.getHttpServletRequest().getParameter("client_id");
    if (StringUtils.isBlank(clientId)) {
      throw new RuntimeException("client_id is required!");
    }
    
    Pattern clientIdPattern = Pattern.compile("^grouper\\.duoConnector\\.([^.]+)\\.clientId$");
    String configId = null;
    for (String propertyName : GrouperLoaderConfig.retrieveConfig().propertyNames()) {
      
      Matcher matcher = clientIdPattern.matcher(propertyName);
      if (matcher.matches()) {
        if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(propertyName), clientId)) {
          configId = matcher.group(1);
          break;
        }
      }
    }
    
    if (StringUtils.isBlank(configId)) {
      throw new RuntimeException("Cant find client id!");
    }

    String clientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".clientSecret");
    clientSecret = Morph.decryptIfFile(clientSecret);
    if (!StringUtils.equals(clientSecret, mockServiceRequest.getHttpServletRequest().getParameter("client_secret"))) {
      throw new RuntimeException("Cant find client secret!");
    }
    
    String tenantId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector." + configId + ".tenantId");
    
    if (4 != mockServiceRequest.getPostMockNamePaths().length
        || !StringUtils.equals(tenantId, mockServiceRequest.getPostMockNamePaths()[1])
        || !StringUtils.equals("oauth2", mockServiceRequest.getPostMockNamePaths()[2])
        || !StringUtils.equals("token", mockServiceRequest.getPostMockNamePaths()[3])
        ) {
      throw new RuntimeException("Invalid request! expecting: auth/<tenantId>/oauth2/token");
    }
    
    String grantType = mockServiceRequest.getHttpServletRequest().getParameter("grant_type");
    if (!StringUtils.equals("client_credentials", grantType)) {
      throw new RuntimeException("Invalid request! client_credentials must equal 'grant_type'");
    }
    String resourceConfig = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector." + configId + ".resource");
    String resourceHttp =  mockServiceRequest.getHttpServletRequest().getParameter("resource");
    if (StringUtils.isBlank(resourceConfig) || !StringUtils.equals(resourceConfig, resourceHttp)) {
      throw new RuntimeException("Invalid request! resource: '" + resourceHttp + "' must equal '" + resourceConfig + "'");
    }

    mockServiceResponse.setResponseCode(200);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    //expires in a minute
    long expiresOnSeconds = System.currentTimeMillis()/1000 + 60;
    
    resultNode.put("expires_on", expiresOnSeconds);
    
    String accessToken = GrouperUuid.getUuid();
    
//    GrouperAzureAuth grouperAzureAuth = new GrouperAzureAuth();
//    grouperAzureAuth.setConfigId(configId);
//    grouperAzureAuth.setAccessToken(accessToken);
//    grouperAzureAuth.setExpiresOnSeconds(expiresOnSeconds);
//    HibernateSession.byObjectStatic().save(grouperAzureAuth);
    
    resultNode.put("access_token", accessToken);
    
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
    //delete if its been a while
    if (System.currentTimeMillis() - lastDeleteMillis > 1000*60*60) {
      lastDeleteMillis = System.currentTimeMillis();
      
      long secondsToDelete = System.currentTimeMillis()/1000 - 60*60;
      
//      int accessTokensDeleted = HibernateSession.byHqlStatic()
//        .createQuery("delete from GrouperAzureAuth where expiresOnSeconds < :theExpiresOnSeconds")
//        .setLong("theExpiresOnSeconds", secondsToDelete).executeUpdateInt();
//      
//      if (accessTokensDeleted > 0) {
//        mockServiceRequest.getDebugMap().put("accessTokensDeleted", accessTokensDeleted);
//      }
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
  
  /**
   * convert from jackson json
   * @param grouperAdobeUser
   * @return the grouper duo user
   */
  private static ObjectNode toUserJson(GrouperAdobeUser grouperAdobeUser) {
    
    ObjectNode result = GrouperUtil.jsonJacksonNode();
    
    ObjectNode aliasesObjectNode = result.putObject("aliases");
    
    GrouperUtil.jsonJacksonAssignString(result, "email", grouperAdobeUser.getEmail());
    GrouperUtil.jsonJacksonAssignString(result, "firstname", grouperAdobeUser.getFirstName());
    
    ArrayNode groupsNode = GrouperUtil.jsonJacksonArrayNode();
    
//    for (GrouperAdobeGroup grouperAdobeGroup: GrouperUtil.nonNull(grouperAdobeUser.getGroups())) {
//      groupsNode.add(toGroupJson(grouperAdobeGroup));
//    }
    
    result.set("groups", groupsNode);

    GrouperUtil.jsonJacksonAssignString(result, "lastname", grouperAdobeUser.getLastName());
    GrouperUtil.jsonJacksonAssignString(result, "status", grouperAdobeUser.getStatus());
    GrouperUtil.jsonJacksonAssignStringArray(result, "tokens", new ArrayList<String>());
    GrouperUtil.jsonJacksonAssignStringArray(result, "u2ftokens", new ArrayList<String>());
    
    GrouperUtil.jsonJacksonAssignString(result, "user_id", grouperAdobeUser.getId());
    GrouperUtil.jsonJacksonAssignString(result, "username", grouperAdobeUser.getUserName());
    GrouperUtil.jsonJacksonAssignStringArray(result, "webauthncredentials", new ArrayList<String>());
    
    return result;
  }
  
  /**
   * convert from jackson json
   * @param grouperAdobeGroup
   * @return the group
   */
  private static ObjectNode toGroupJson(GrouperAdobeGroup grouperAdobeGroup) {
    ObjectNode result = GrouperUtil.jsonJacksonNode();

    result.put("name", grouperAdobeGroup.getName());
    result.put("group_id", grouperAdobeGroup.getId());
    result.put("mobile_otp_enabled", false);
    result.put("push_enabled", false);
    result.put("sms_enabled", false);
    result.put("voice_enabled", false);
    result.put("status", "active");
    
    return result;
  }

}
