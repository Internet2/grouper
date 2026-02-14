package edu.internet2.middleware.grouper.app.freshServiceRequester;

import java.util.List;
import java.util.Set;

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
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.j2ee.Authentication;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import io.netty.util.internal.ThreadLocalRandom;

public class FreshRequesterMockServiceHandler extends MockServiceHandler {
  
  public FreshRequesterMockServiceHandler() {
  }
  
  /**
   * 
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");
  
  /**
   * 
   */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");

  /**
   * headers to not log all of
   */
  @Override
  public Set<String> doNotLogHeaders() {
    return doNotLogHeaders;
  }
  
  /**
   * params to not log all of
   */
  @Override
  public Set<String> doNotLogParameters() {
    return doNotLogParameters;
  }
  
  private static void ensureFreshserviceMockTables() {
    try {
      new GcDbAccess().sql("select count(*) from mock_freshreq_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_freshreq_user").select(int.class);
//      new GcDbAccess().sql("select count(*) from mock_freshreq_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        
        @Override
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          FreshRequesterGroup.createTableFreshGroup(ddlVersionBean, database);
          FreshRequesterUser.createTableFreshUser(ddlVersionBean, database);
//          FreshRequesterMembership.createTableFreshMembership(ddlVersionBean, database);
          
        }
      });
  
    }
    
  }
  
  private void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String basicAuth = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    
    // These are swapped because Freshservice swaps in the API call.
    String password = Authentication.retrieveUsername(basicAuth);
    String userName = Authentication.retrievePassword(basicAuth);
    
    String configId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.exampleFreshRequester.mockExternalSystem.configId");
    
    String expectedUserName = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken."+configId+".basicAuthUser");
    String expectedPassword = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken."+configId+".basicAuthPassword");
    
    if (!StringUtils.equals(expectedUserName, userName)) {
      throw new RuntimeException("Username does not match with what is in grouper config");
    }
    if (!StringUtils.equals(expectedPassword, password)) {
      throw new RuntimeException("password does not match with what is in grouper config");
    }
    
  }
  
  private void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    List<FreshRequesterGroup> freshRequesterGroups = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();
    
    query = HibernateSession.byHqlStatic().createQuery("from FreshRequesterGroup");
    
    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);
    
    freshRequesterGroups = query.list(FreshRequesterGroup.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (FreshRequesterGroup freshRequesterGroup : freshRequesterGroups) {
      ObjectNode objectNode = freshRequesterGroup.toJson(null);
      entriesArray.add(objectNode);
    }
    
    resultNode.set("requester_groups", entriesArray);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }
  
  private void getGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String groupIdString = mockServiceRequest.getPostMockNamePaths()[1];
    Long groupId = Long.parseLong(groupIdString);
    
    List<FreshRequesterGroup> freshRequesterGroups = HibernateSession.byHqlStatic().createQuery("from FreshRequesterGroup where id = :theId")
        .setLong("theId", groupId).list(FreshRequesterGroup.class);
    
    if (GrouperUtil.length(freshRequesterGroups) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = freshRequesterGroups.get(0).toJson(null);
      
      resultNode.set("requester_group", objectNode);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(freshRequesterGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(freshRequesterGroups) + ", id: " + groupId);
    }
    
  }
  
  private void postGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);
    
    FreshRequesterGroup freshReqGroup = FreshRequesterGroup.fromJson(groupJsonNode);
    
    boolean idSaved = false;
    
    while(!idSaved) {
      try {
        freshReqGroup.setId(ThreadLocalRandom.current().nextLong(1, 99999999));
        HibernateSession.byObjectStatic().save(freshReqGroup);
        idSaved = true;
      } catch (GrouperDAOException e) {
        
      }
    }
    
    ObjectNode objectNode = freshReqGroup.toJson(null);
    
    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
    
  }
  
  private void updateGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);
    
    FreshRequesterGroup freshReqGroup = FreshRequesterGroup.fromJson(groupJsonNode);
    
    Long id = Long.parseLong(mockServiceRequest.getPostMockNamePaths()[1]);
    freshReqGroup.setId(id);
    
    HibernateSession.byObjectStatic().saveOrUpdate(freshReqGroup);
    
    JsonNode resultNode = freshReqGroup.toJson(null);
    
    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }
  
  private void deleteGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    Long id = Long.parseLong(mockServiceRequest.getPostMockNamePaths()[1]);
    FreshRequesterGroup group = new FreshRequesterGroup();
    group.setId(id);
    
    HibernateSession.byObjectStatic().delete(group);
    
    mockServiceResponse.setResponseCode(204);
    
  }
  
  private void getUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    List<FreshRequesterUser> freshRequesterUsers = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();
    
    query = HibernateSession.byHqlStatic().createQuery("from FreshRequesterUser");
    
    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);
    
    freshRequesterUsers = query.list(FreshRequesterUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (FreshRequesterUser freshRequesterUser : freshRequesterUsers) {
      ObjectNode objectNode = freshRequesterUser.toJson(null);
      entriesArray.add(objectNode);
    }
    
    resultNode.set("requesters", entriesArray);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }
  
  private void getUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String userIdString = mockServiceRequest.getPostMockNamePaths()[1];
    Long userId = Long.parseLong(userIdString);
    
    List<FreshRequesterUser> freshRequesterUsers = HibernateSession.byHqlStatic().createQuery("from FreshRequesterUser where id = :theId")
        .setLong("theId", userId).list(FreshRequesterUser.class);
    
    if (GrouperUtil.length(freshRequesterUsers) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = freshRequesterUsers.get(0).toJson(null);
      
      resultNode.set("requester", objectNode);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    } else if (GrouperUtil.length(freshRequesterUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersById: " + GrouperUtil.length(freshRequesterUsers) + ", id: " + userId);
    }
    
  }
  
  private void postUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);
    
    FreshRequesterUser freshReqUser = FreshRequesterUser.fromJson(groupJsonNode);
    
    boolean idSaved = false;
    
    while(!idSaved) {
      try {
        freshReqUser.setId(ThreadLocalRandom.current().nextLong(1, 99999999));
        HibernateSession.byObjectStatic().save(freshReqUser);
        idSaved = true;
      } catch (GrouperDAOException e) {
        
      }
    }
    
    ObjectNode objectNode = freshReqUser.toJson(null);
    
    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
  }
  
  private void updateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);
    
    FreshRequesterUser freshReqUser = FreshRequesterUser.fromJson(userJsonNode);
    
    Long id = Long.parseLong(mockServiceRequest.getPostMockNamePaths()[1]);
    freshReqUser.setId(id);
    
    HibernateSession.byObjectStatic().saveOrUpdate(freshReqUser);
    
    JsonNode resultNode = freshReqUser.toJson(null);
    
    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }
  
  private void deleteUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    Long id = Long.parseLong(mockServiceRequest.getPostMockNamePaths()[1]);
    FreshRequesterUser user = new FreshRequesterUser();
    user.setId(id);
    
    HibernateSession.byObjectStatic().delete(user);
    
    mockServiceResponse.setResponseCode(204);
    
  }

  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    ensureFreshserviceMockTables();
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    
    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);
    
    mockServiceRequest.setPostMockNamePaths(paths);
    
    checkAuthorization(mockServiceRequest);
    
    //GET requests
    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("requester_groups".equals(mockNamePaths.get(0)) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("requester_groups".equals(mockNamePaths.get(0)) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("requesters".equals(mockNamePaths.get(0)) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("requesters".equals(mockNamePaths.get(0)) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        getUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      
    }

    //POST requests
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("requester_groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("requesters".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    //PUT requests
    if (StringUtils.equals("PUT", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("requester_groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("requesters".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        updateUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    //DELETE requests
    if (StringUtils.equals("DELETE", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("requester_groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("requesters".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
    
  }
  
  private void checkRequestContentType(MockServiceRequest mockServiceRequest) {
    if (!StringUtils.equals(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json")
            && !StringUtils.startsWith(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json;")) {
      throw new RuntimeException("Content type must be application/json");
    }
  }

}