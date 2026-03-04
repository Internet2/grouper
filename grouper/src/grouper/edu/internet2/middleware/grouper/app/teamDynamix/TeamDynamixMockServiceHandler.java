package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperMockDdl;
import edu.internet2.middleware.grouper.ext.org.apache.ddlutils.model.Database;
import edu.internet2.middleware.grouper.hibernate.ByHqlStatic;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class TeamDynamixMockServiceHandler extends MockServiceHandler {
  
  public TeamDynamixMockServiceHandler() {
  }

  /**
   * 
   */
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");

  /**
   * 
   */
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

  private String configId;
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
  public static void ensureTeamDynamixMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_teamdynamix_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_teamdynamix_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_teamdynamix_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_teamdynamix_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          TeamDynamixGroup.createTableTeamDynamixGroup(ddlVersionBean, database);
          TeamDynamixAuth.createTableTeamDynamixAuth(ddlVersionBean, database);
          TeamDynamixUser.createTableTeamDynamixUser(ddlVersionBean, database);
          TeamDynamixMembership.createTableTeamDynamixMembership(ddlVersionBean, database);
          
        }
      });
  
    }    
  }

  /**
   * 
   */
  public static void dropTeamDynamixMockTables() {
    MockServiceServlet.dropMockTable("mock_teamdynamix_membership");
    MockServiceServlet.dropMockTable("mock_teamdynamix_user");
    MockServiceServlet.dropMockTable("mock_teamdynamix_group");
    MockServiceServlet.dropMockTable("mock_teamdynamix_auth");
  }

  private static boolean mockTablesThere = false;
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest,
      MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureTeamDynamixMockTables();
    }
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }

    this.configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.teamDynamix.mock.configId");
    if (StringUtils.isBlank(configId)) {
      this.configId = "myTeamDynamix";
    }
    
    String[] postMockNamePaths = mockServiceRequest.getPostMockNamePaths();
    //just remove 'api' from all the requests
    String[] modifiedArray = Arrays.copyOfRange(postMockNamePaths, 1, postMockNamePaths.length);
    mockServiceRequest.setPostMockNamePaths(modifiedArray);

    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("people".equals(mockServiceRequest.getPostMockNamePaths()[0]) &&
          "groups".equals(mockServiceRequest.getPostMockNamePaths()[2]) && 3 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroupsForUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 3 == mockServiceRequest.getPostMockNamePaths().length
          && "members".equals(mockServiceRequest.getPostMockNamePaths()[2])) {
        getGroupMembers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("people".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        getUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("DELETE", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 3 == mockServiceRequest.getPostMockNamePaths().length
          && "members".equals(mockServiceRequest.getPostMockNamePaths()[2])) {
        deleteMemberships(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("auth".equals(mockServiceRequest.getPostMockNamePaths()[0])) {
        postAuth(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        createGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && "search".equals(mockServiceRequest.getPostMockNamePaths()[1]) 
          && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        searchGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("people".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        createUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("people".equals(mockServiceRequest.getPostMockNamePaths()[0]) && "search".equals(mockServiceRequest.getPostMockNamePaths()[1]) 
          && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        searchUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 3 == mockServiceRequest.getPostMockNamePaths().length
          && "members".equals(mockServiceRequest.getPostMockNamePaths()[2])) {
        createMemberships(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("PATCH", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("people".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        patchUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }    
    
    if (StringUtils.equals("PUT", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("people".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 
          "isactive".equals(mockServiceRequest.getPostMockNamePaths()[2]) && 3 == mockServiceRequest.getPostMockNamePaths().length) {
        deleteUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        updateGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
    }    

    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
    
  }
  
  public void patchUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    checkAuthorization(mockServiceRequest);
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
  
    TeamDynamixUser teamDynamixUser = HibernateSession.byHqlStatic()
        .createQuery("from TeamDynamixUser where id = :theValue").setString("theValue", id)
        .uniqueResult(TeamDynamixUser.class);

    if (teamDynamixUser == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceRequest.getDebugMap().put("foundUser", false);
      return;
    }
        
    mockServiceResponse.setContentType("application/json");

//  [
//  {"op": "add", "path": "/title", "value": "Updated Title"},
//  {"op": "add", "path": "/accountid", "value": 47},
//  {"op": "add", "path": "/attributes/1234", "value": "New Attribute Value"},
//  {"op": "remove", "path": "/attributes/5678"}
// ]
    
    String requestBodyString = mockServiceRequest.getRequestBody();
    ArrayNode operationsNode = (ArrayNode) GrouperUtil.jsonJacksonNode(requestBodyString);

    GrouperUtil.assertion(operationsNode.size() > 0, "must send operations");

    for (int i=0;i<operationsNode.size();i++) {
      
      JsonNode operation = operationsNode.get(i);
      
      //            "op": "replace",
      //            "path": "active",
      //            "value": "false"

      // replace, add, remove
      String op = GrouperUtil.jsonJacksonGetString(operation, "op");
      boolean opAdd = "add".equals(op);
      boolean opReplace = "replace".equals(op);
      boolean opRemove = "remove".equals(op);
      if (!opAdd && !opRemove && !opReplace) {
        throw new RuntimeException("Invalid op, expecting add, replace, remove, but received: '" + op + "'");
      }
      String path = GrouperUtil.jsonJacksonGetString(operation, "path");

      // strip leading slash from JSON Patch path (e.g. "/FirstName" -> "FirstName")
      if (path != null && path.startsWith("/")) {
        path = path.substring(1);
      }

      GrouperUtil.assertion(!"id".equals(path), "cannot patch id");

      if ("FirstName".equals(path)) {
        path = "firstName";
      }
      if ("LastName".equals(path)) {
        path = "lastName";
      }
      if ("Company".equals(path)) {
        path = "company";
      }
      if ("ExternalID".equals(path)) {
        path = "externalId";
      }
      if ("PrimaryEmail".equals(path)) {
        path = "primaryEmail";
      }
      if ("SecurityRoleID".equals(path)) {
        path = "securityRoleId";
      }
      if ("UserName".equals(path)) {
        path = "userName";
      }
      if ("IsActive".equals(path)) {
        path = "active";
      }
        
      Object newValue = "active".equals(path) ? GrouperUtil.jsonJacksonGetBoolean(operation, "value") : GrouperUtil.jsonJacksonGetString(operation, "value");
      Object oldValue = GrouperUtil.fieldValue(teamDynamixUser, path);
      
      if (opAdd) {
        
        GrouperUtil.assertion(GrouperUtil.isBlank(oldValue), "add op already has value! " + path + ", '" + oldValue + "' " + teamDynamixUser);
        
        GrouperUtil.assignField(teamDynamixUser, path, newValue);
        
      } else {

        GrouperUtil.assertion(!GrouperUtil.isBlank(oldValue), op + " op doesnt have value! " + path + ", '" + oldValue + "' " + teamDynamixUser);

        if (opRemove) {
          
          GrouperUtil.assertion(newValue == null, "remove op should not have a value! " + path + ", '" + newValue + "' " + teamDynamixUser);
        }

        GrouperUtil.assignField(teamDynamixUser, path, newValue);
      }
        
      
    }
    HibernateSession.byObjectStatic().saveOrUpdate(teamDynamixUser);
    
    ObjectNode objectNode = teamDynamixUser.toJson(null);
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
    
  }
  
  public void createMemberships(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);
    
    checkRequestContentType(mockServiceRequest);

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    String isNotified = mockServiceRequest.getHttpServletRequest().getParameter("isNotified");
    
    boolean isNotifiedBoolean = GrouperUtil.booleanValue(isNotified, false);
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    //check if group exists
    List<TeamDynamixGroup> teamDynamixGroups = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup where id = :theId")
        .setString("theId", groupId).list(TeamDynamixGroup.class);
    
    if (GrouperUtil.length(teamDynamixGroups) == 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    String memberIdsInJson = mockServiceRequest.getRequestBody();
    
    ArrayNode memberIdsNode = (ArrayNode)GrouperUtil.jsonJacksonNode(memberIdsInJson);
    
    Iterator<JsonNode> iterator = memberIdsNode.iterator();
    while (iterator.hasNext()) {
      JsonNode next = iterator.next();
      String memberId = next.asText();
      
      //check if userid exists
      List<TeamDynamixUser> teamDynamixUsers = HibernateSession.byHqlStatic().createQuery("select user from TeamDynamixUser user where user.id = :theId")
          .setString("theId", memberId).list(TeamDynamixUser.class);
      
      if (teamDynamixUsers.size() == 1) {
        
        //check if userid exists
        List<TeamDynamixMembership> memberships = HibernateSession.byHqlStatic().createQuery("from TeamDynamixMembership where userId = :userId and groupId = :groupId")
            .setString("userId", memberId).setString("groupId", groupId).list(TeamDynamixMembership.class);
        
        if (memberships.size() == 0) {
          TeamDynamixMembership membership = new TeamDynamixMembership();
          membership.setGroupId(groupId);
          membership.setUserId(memberId);
          membership.setId(GrouperUuid.getUuid());
          membership.setIsNotified(isNotifiedBoolean);
          
          HibernateSession.byObjectStatic().save(membership);
        }
      }
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
  }
  
  public void deleteMemberships(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);
    
    checkRequestContentType(mockServiceRequest);

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    //check if group exists
    List<TeamDynamixGroup> teamDynamixGroups = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup where id = :theId")
        .setString("theId", groupId).list(TeamDynamixGroup.class);
    
    if (GrouperUtil.length(teamDynamixGroups) == 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    String memberIdsInJson = mockServiceRequest.getRequestBody();
    
    ArrayNode memberIdsNode = (ArrayNode)GrouperUtil.jsonJacksonNode(memberIdsInJson);
    
    Iterator<JsonNode> iterator = memberIdsNode.iterator();
    while (iterator.hasNext()) {
      JsonNode next = iterator.next();
      String memberId = next.asText();
      
        
      //check if userid exists
      HibernateSession.byHqlStatic().createQuery("delete from TeamDynamixMembership where userId = :userId and groupId = :groupId")
          .setString("userId", memberId).setString("groupId", groupId).executeUpdateInt();
        
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
  }
  
  public void getGroupMembers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      e.printStackTrace();
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    List<TeamDynamixUser> teamDynamixUsers = null;
    
    ByHqlStatic query = HibernateSession.byHqlStatic()
        .createQuery("from TeamDynamixUser u where u.id in (select m.userId from TeamDynamixMembership m where m.groupId = :theGroupId) ")
        .setString("theGroupId", groupId);
    
    teamDynamixUsers = query.list(TeamDynamixUser.class);
    
    ArrayNode valueNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (TeamDynamixUser teamDynamixUser : teamDynamixUsers) {
      valueNode.add(teamDynamixUser.toJson(null));
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(valueNode));
    
  }
  
  public void createGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    //  {
    //    "Name": "Self help community for library",
    //    "Description": "Library Assist",
    //  }
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);

    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "Name")) > 0, "Name is required");
    
    TeamDynamixGroup teamDynamixGroup = TeamDynamixGroup.fromJson(groupJsonNode);
    teamDynamixGroup.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(teamDynamixGroup);
    
    JsonNode resultNode = teamDynamixGroup.toJson(null);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }
  
 public void postAuth(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
   String postAuthBodyString = mockServiceRequest.getRequestBody();
   JsonNode postAuthBodyNode = GrouperUtil.jsonJacksonNode(postAuthBodyString);

   //check require args
   GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(postAuthBodyNode, "BEID")) > 0, "BEID is required");
   GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(postAuthBodyNode, "WebServicesKey")) > 0, "WebServicesKey is required");
   

   mockServiceResponse.setResponseCode(200);
    
   String bearerToken = GrouperUuid.getUuid();
    
    TeamDynamixAuth teamDynamixAuth = new TeamDynamixAuth();
    teamDynamixAuth.setConfigId(this.configId);
    teamDynamixAuth.setAccessToken(bearerToken);
    HibernateSession.byObjectStatic().save(teamDynamixAuth);
    
    
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(bearerToken);
    
  }
  
  public void createUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);

    TeamDynamixUser teamDynamixUser = TeamDynamixUser.fromJson(userJsonNode);
    teamDynamixUser.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(teamDynamixUser);
    
    JsonNode resultNode = teamDynamixUser.toJson(null);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }
  
  public void updateGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String jsonString = mockServiceRequest.getRequestBody();
    JsonNode jsonNode = GrouperUtil.jsonJacksonNode(jsonString);

    TeamDynamixGroup teamDynamixObject = TeamDynamixGroup.fromJson(jsonNode);
    
    HibernateSession.byObjectStatic().saveOrUpdate(teamDynamixObject);
    
    JsonNode resultNode = teamDynamixObject.toJson(null);

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }
  
  public void searchGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    //  {
    //    "IsActive": "True",
    //    "NameLike": "Test Group",
    //  }

    //or
    
    //  {
    //    "IsActive": "True",
    //  }
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);

    //check require args
    String name = GrouperUtil.jsonJacksonGetString(groupJsonNode, "NameLike");
    
    List<TeamDynamixGroup> teamDynamixGroups = null;
    
    StringBuilder query = new StringBuilder("from TeamDynamixGroup ");
    
    if (StringUtils.isNotBlank(name)) {
      query.append("where name like :theName ");
    } 
    
    
    ByHqlStatic createQuery = HibernateSession.byHqlStatic()
      .createQuery(query.toString());
    if (StringUtils.isNotBlank(name)) {
      createQuery.setString("theName", "%"+name+"%");
    }
    
    teamDynamixGroups = createQuery.list(TeamDynamixGroup.class);
    
    ArrayNode results = GrouperUtil.jsonJacksonArrayNode();
    
    for (TeamDynamixGroup teamDynamixGroup : teamDynamixGroups) {
      results.add(teamDynamixGroup.toJson(null));
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(results));
    mockServiceResponse.setContentType("application/json");
  }
  
  public void searchUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    //  {
    //    "IsActive": "True",
    //    "SearchText": "Test user",
    //  }

    //or
    
    //  {
    //    "IsActive": "True",
    //  }
    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);

    StringBuilder query = new StringBuilder("from TeamDynamixUser ");

    String name = null;
    boolean hasCondition = false;
    if (userJsonNode.has("ExternalID")) {
      name = GrouperUtil.jsonJacksonGetString(userJsonNode, "ExternalID");
      query.append("where externalId like :theSearch ");
      hasCondition = true;
    } else if (userJsonNode.has("UserName")) {
      name = GrouperUtil.jsonJacksonGetString(userJsonNode, "UserName");
      query.append("where userName like :theSearch ");
      hasCondition = true;
    }

    Boolean isActive = GrouperUtil.jsonJacksonGetBoolean(userJsonNode, "IsActive");

    List<TeamDynamixUser> teamDynamixUsers = null;

    if (isActive != null) {
      if (hasCondition) {
        query.append(" and ");
      } else {
        query.append("where ");
      }
      query.append("active = :theActive");
    }
    
    ByHqlStatic createQuery = HibernateSession.byHqlStatic()
      .createQuery(query.toString());
    if (StringUtils.isNotBlank(name)) {
      createQuery.setString("theSearch", "%"+name+"%");
    }
    if (isActive != null) {
      createQuery.setString("theActive", isActive ? "T": "F");
    }
    
    teamDynamixUsers = createQuery.list(TeamDynamixUser.class);
    
    ArrayNode results = GrouperUtil.jsonJacksonArrayNode();
    
    for (TeamDynamixUser teamDynamixUser : teamDynamixUsers) {
      results.add(teamDynamixUser.toJson(null));
    }

    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(results));
    mockServiceResponse.setContentType("application/json");
  }

  public void getGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);

    String id = mockServiceRequest.getPostMockNamePaths()[1];

    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");

    List<TeamDynamixGroup> teamDynamixGroups = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup where id = :theId")
        .setString("theId", id).list(TeamDynamixGroup.class);

    if (GrouperUtil.length(teamDynamixGroups) == 1) {
      mockServiceResponse.setResponseCode(200);
      
      mockServiceResponse.setContentType("application/json");

      ObjectNode objectNode = teamDynamixGroups.get(0).toJson(null);
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(teamDynamixGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(teamDynamixGroups) + ", id: " + id);
    }

  }
  
  public void getUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);

    String id = mockServiceRequest.getPostMockNamePaths()[1];

    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");

    List<TeamDynamixUser> teamDynamixUsers = HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser where id = :theId")
        .setString("theId", id).list(TeamDynamixUser.class);

    if (GrouperUtil.length(teamDynamixUsers) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");

      ObjectNode objectNode = teamDynamixUsers.get(0).toJson(null);
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(teamDynamixUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersById: " + GrouperUtil.length(teamDynamixUsers) + ", id: " + id);
    }

  }
  
  public void deleteUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
    
    List<TeamDynamixUser> teamDynamixUsers = HibernateSession.byHqlStatic().createQuery("from TeamDynamixUser where id = :theId")
        .setString("theId", id).list(TeamDynamixUser.class);

    if (GrouperUtil.length(teamDynamixUsers) == 1) {

      TeamDynamixUser teamDynamixUser = teamDynamixUsers.get(0);
      teamDynamixUser.setActive(false);
      HibernateSession.byObjectStatic().saveOrUpdate(teamDynamixUser);
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");

      ObjectNode objectNode = teamDynamixUser.toJson(null);
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(teamDynamixUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersById: " + GrouperUtil.length(teamDynamixUsers) + ", id: " + id);
    }

  }
  
  public void getGroupsForUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    List<TeamDynamixMembership> memberships = HibernateSession.byHqlStatic().createQuery("from TeamDynamixMembership where userId = :theUserId")
        .setString("theUserId", userId).list(TeamDynamixMembership.class);

    ArrayNode results = GrouperUtil.jsonJacksonArrayNode();
    
    for (TeamDynamixMembership membership: memberships) {
      String groupId = membership.getGroupId();
      
      TeamDynamixGroup teamDynamixGroup = HibernateSession.byHqlStatic().createQuery("from TeamDynamixGroup where id = :theId")
          .setString("theId", groupId).uniqueResult(TeamDynamixGroup.class);
      
      results.add(teamDynamixGroup.toJson(null));
    }
    
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(results));
    mockServiceResponse.setContentType("application/json");

  }
  
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    String bearerToken = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (!bearerToken.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization token must start with 'Bearer '");
    }
    String authorizationToken = GrouperUtil.prefixOrSuffix(bearerToken, "Bearer ", false);
    
    List<TeamDynamixAuth> teamDynamixAuths = 
        HibernateSession.byHqlStatic().createQuery("from TeamDynamixAuth where accessToken = :theAccessToken").setString("theAccessToken", authorizationToken).list(TeamDynamixAuth.class);
    
    if (GrouperUtil.length(teamDynamixAuths) != 1) {
      throw new RuntimeException("Invalid access token, not found! " + StringUtils.abbreviate(authorizationToken, 5));
    }

    // all good
  }

  private void checkRequestContentType(MockServiceRequest mockServiceRequest) {
    if (!StringUtils.equals(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json")
            && !StringUtils.startsWith(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json;")) {
      throw new RuntimeException("Content type must be application/json");
    }
  }

}
