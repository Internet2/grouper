package edu.internet2.middleware.grouper.app.duo.role;

import java.io.UnsupportedEncodingException;
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

import com.fasterxml.jackson.databind.ObjectMapper;
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

public class DuoRoleMockServiceHandler extends MockServiceHandler {

  public DuoRoleMockServiceHandler() {
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
  public static void ensureDuoRoleMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_duo_role_user").select(int.class);
//      new GcDbAccess().sql("select count(*) from mock_duo_auth").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
//          GrouperAzureAuth.createTableAzureAuth(ddlVersionBean, database);
          GrouperDuoRoleUser.createTableDuoUser(ddlVersionBean, database);
        }
      });
  
    }    
  }

  /**
   * 
   */
  public static void dropDuoMockTables() {
    MockServiceServlet.dropMockTable("mock_duo_role_user");
//    MockServiceServlet.dropMockTable("mock_duo_auth");
  }
  
  private static boolean mockTablesThere = false;
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureDuoRoleMockTables();
    }
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    
    GrouperUtil.assertion(mockNamePaths.size() >= 2, "Must start with admin/v1 or admin/v2");
    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(0), "admin"), "");
    GrouperUtil.assertion(StringUtils.equals(mockNamePaths.get(1), "v1") || StringUtils.equals(mockNamePaths.get(1), "v2"), "");
    
    mockNamePaths = mockNamePaths.subList(2, mockNamePaths.size());
    
    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);
    
    mockServiceRequest.setPostMockNamePaths(paths);

    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {

      if ("admins".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getAdmins(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("admins".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getAdmin(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("DELETE", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("admins".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteAdmins(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("auth".equals(mockNamePaths.get(0))) {
        postAuth(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("admins".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postAdmins(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("admins".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateAdmin(mockServiceRequest, mockServiceResponse);
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
    ensureDuoRoleMockTables();
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
    
    String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.duo.mock.configId");
    String expectedIntegrationKey = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector."+configId+".adminIntegrationKey");
    String adminDomainName = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector."+configId+".adminDomainName");
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
    
    String adminSecretKey = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector."+configId+".adminSecretKey");
    
    String hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, adminSecretKey).hmacHex(hmacSource);
    if (!StringUtils.equals(hmac, password)) {
      throw new RuntimeException("hmac1 password does not match: "+StringUtils.abbreviate(hmac, 10));
    }

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

  public void getAdmins(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
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
    
    List<GrouperDuoRoleUser> grouperDuoRoleUsers = null;
    
    ByHqlStatic query = HibernateSession.byHqlStatic().createQuery("select distinct user from GrouperDuoRoleUser user");
    
    QueryOptions queryOptions = new QueryOptions();
    QueryPaging queryPaging = QueryPaging.page(limitInt, pageNumber, true);
    queryOptions = queryOptions.paging(queryPaging);
    
    query.options(queryOptions);
    
    grouperDuoRoleUsers = query.list(GrouperDuoRoleUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    resultNode.put("stat", "OK");
    
    for (GrouperDuoRoleUser grouperDuoRoleUser : grouperDuoRoleUsers) {
      valueNode.add(toUserJson(grouperDuoRoleUser));
    }
    
    resultNode.set("response", valueNode);
    if (queryPaging.getTotalRecordCount() > offsetInt + grouperDuoRoleUsers.size()) {
      ObjectNode metadataNode = GrouperUtil.jsonJacksonNode();
      metadataNode.put("next_offset", offsetInt + limitInt);
      resultNode.set("metadata", metadataNode);
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }
  
  public void getAdmin(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    List<GrouperDuoRoleUser> grouperDuoRoleUsers = HibernateSession.byHqlStatic().createQuery("select distinct user from GrouperDuoRoleUser user where user.id = :theId")
        .setString("theId", userId).list(GrouperDuoRoleUser.class);

    if (GrouperUtil.length(grouperDuoRoleUsers) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
      resultNode.put("stat", "OK");
      ObjectNode objectNode = toUserJson(grouperDuoRoleUsers.get(0));
      
      resultNode.set("response", objectNode);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(grouperDuoRoleUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("userById: " + GrouperUtil.length(grouperDuoRoleUsers) + ", id: " + userId);
    }

  }
  
  public void postAdmins(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentTypeAndDateHeader(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    
    String name = mockServiceRequest.getHttpServletRequest().getParameter("name");
    if (StringUtils.isBlank(name)) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    String email = mockServiceRequest.getHttpServletRequest().getParameter("email");
    
    if (StringUtils.isBlank(email)) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    String role = mockServiceRequest.getHttpServletRequest().getParameter("role");
    
    if (StringUtils.isBlank(role)) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    

    GrouperDuoRoleUser grouperDuoRoleUser = new GrouperDuoRoleUser();
    grouperDuoRoleUser.setId(GrouperUuid.getUuid());
    grouperDuoRoleUser.setEmail(email);
    grouperDuoRoleUser.setName(name);
    grouperDuoRoleUser.setRole(role);
    
    List<GrouperDuoRoleUser> grouperDuoRoleUsers = HibernateSession.byHqlStatic().createQuery("select user from GrouperDuoRoleUser user where user.email = :email ")
        .setString("email", grouperDuoRoleUser.getEmail())
        .list(GrouperDuoRoleUser.class);
    
    if (grouperDuoRoleUsers != null && grouperDuoRoleUsers.size() > 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
     
    HibernateSession.byObjectStatic().save(grouperDuoRoleUser);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("stat", "OK");
    ObjectNode objectNode = toUserJson(grouperDuoRoleUser);
    
    resultNode.set("response", objectNode);
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }
  
  public void updateAdmin(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentTypeAndDateHeader(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }


    // patch a user
    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("userId", userId);

    List<GrouperDuoRoleUser> grouperDuoRoleUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperDuoRoleUser where id = :theId")
        .setString("theId", userId).list(GrouperDuoRoleUser.class);
    
    if (GrouperUtil.length(grouperDuoRoleUsers) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindUser", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperDuoRoleUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(grouperDuoRoleUsers));
    }
    
    String name = mockServiceRequest.getHttpServletRequest().getParameter("name");
    String email = mockServiceRequest.getHttpServletRequest().getParameter("email");
    String role = mockServiceRequest.getHttpServletRequest().getParameter("role");
    
    
    GrouperDuoRoleUser grouperDuoRoleUser = grouperDuoRoleUsers.get(0);

    if (StringUtils.isNotBlank(email)) {
      
      //check if the new username is already taken
      List<GrouperDuoRoleUser> existingUsersWithSameUserName = HibernateSession.byHqlStatic().createQuery("select user from GrouperDuoRoleUser user where user.email = :email ")
          .setString("email", email)
          .list(GrouperDuoRoleUser.class);
      
      if (existingUsersWithSameUserName != null && existingUsersWithSameUserName.size() > 0) {
        
        for (GrouperDuoRoleUser existingUser: existingUsersWithSameUserName) {
          if (StringUtils.equals(email, existingUser.getEmail()) 
              && !StringUtils.equals(existingUser.getId(), userId) ) {
            mockServiceRequest.getDebugMap().put("emailAlreadyExists", true);
            mockServiceResponse.setResponseCode(404);
            return;
          }
        }
       
      }
      grouperDuoRoleUser.setEmail(email);
    }
//    if (StringUtils.isNotBlank(firstName)) {
//      grouperDuoRoleUser.setFirstName(firstName);
//    }
    if (StringUtils.isNotBlank(name)) {
      grouperDuoRoleUser.setName(name);
    }
    if (StringUtils.isNotBlank(role)) {
      grouperDuoRoleUser.setRole(role);
    }
    
    HibernateSession.byObjectStatic().saveOrUpdate(grouperDuoRoleUser);
    
    // we want users in response
    getAdmin(mockServiceRequest, mockServiceResponse);
    
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

  public void deleteAdmins(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "id is required");

    HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperDuoRoleUser where id = :theId")
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
   * @param grouperDuoRoleUser
   * @return the grouper duo user
   */
  private static ObjectNode toUserJson(GrouperDuoRoleUser grouperDuoRoleUser) {
    
    ObjectNode result = GrouperUtil.jsonJacksonNode();
  
    GrouperUtil.jsonJacksonAssignString(result, "email", grouperDuoRoleUser.getEmail());
    GrouperUtil.jsonJacksonAssignString(result, "name", grouperDuoRoleUser.getName());
    GrouperUtil.jsonJacksonAssignString(result, "role", grouperDuoRoleUser.getRole());
    GrouperUtil.jsonJacksonAssignString(result, "admin_id", grouperDuoRoleUser.getId());
    
    return result;
  }
  
}
