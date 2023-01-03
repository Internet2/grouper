package edu.internet2.middleware.grouper.app.remedyV2;

import java.util.List;

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
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class RemedyMockServiceHandler extends MockServiceHandler {
  
  
  private static boolean mockTablesThere = false;
  
  public static void ensureRemedyMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_remedy_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_remedy_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_remedy_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_remedy_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperRemedyGroup.createTableRemedyGroup(ddlVersionBean, database);
          GrouperRemedyAuth.createTableRemedyAuth(ddlVersionBean, database);
          GrouperRemedyUser.createTableRemedyUser(ddlVersionBean, database);
          GrouperRemedyMembership.createTableRemedyMembership(ddlVersionBean, database);
          
        }
      });
  
    }    
  }
  
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    
    String bearerToken = mockServiceRequest.getHttpServletRequest().getHeader("authorization");
    if (!bearerToken.startsWith("AR-JWT ")) {
      throw new RuntimeException("Authorization token must start with 'AR-JWT '");
    }
    String authorizationToken = GrouperUtil.prefixOrSuffix(bearerToken, "AR-JWT ", false);
    
    List<GrouperRemedyAuth> grouperRemedyAuths = 
        HibernateSession.byHqlStatic().createQuery("from GrouperRemedyAuth where jwtToken = :theAccessToken").setString("theAccessToken", authorizationToken).list(GrouperRemedyAuth.class);
    
    if (GrouperUtil.length(grouperRemedyAuths) != 1) {
      throw new RuntimeException("Invalid access token, not found!");
    }
    
    GrouperRemedyAuth grouperRemedyAuth = grouperRemedyAuths.get(0);    

    // all good
  }

  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    List<GrouperRemedyGroup> grouperRemedyGroups = null;
    ByHqlStatic query = null;
    query = HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup");
    
    grouperRemedyGroups = query.list(GrouperRemedyGroup.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    //  {
    //    "entries": [
    //      {
    //        "values": {
    //          "Status": "Enabled",
    //          "Permission Group": "2000000001",
    //          "Permission Group ID": 2000000001
    //        },
    //        "_links": {
    //          "self": [
    //            {
    //              "href": "https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/ENT:SYS-Access%20Permission%20Grps/2000000001"
    //            }
    //          ]
    //        }
    //      },
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperRemedyGroup grouperRemedyGroup : grouperRemedyGroups) {
      
      ObjectNode valuesNode = GrouperUtil.jsonJacksonNode();
      
      ObjectNode objectNode = grouperRemedyGroup.toJson(null);
      
      valuesNode.set("values", objectNode);
      entriesArray.add(valuesNode);
    }
    
    resultNode.set("entries", entriesArray);
    
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

    String permissionGroupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(permissionGroupId) > 0, "permissionGroupId is required");
    
    Long permissionGroupIdLong = Long.valueOf(permissionGroupId);
    
    List<GrouperRemedyGroup> grouperRemedyGroups = HibernateSession.byHqlStatic().createQuery("from GrouperRemedyGroup where permissionGroupId = :theId")
        .setLong("theId", permissionGroupIdLong).list(GrouperRemedyGroup.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperRemedyGroup grouperRemedyGroup : grouperRemedyGroups) {
      
      ObjectNode valuesNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = grouperRemedyGroup.toJson(null);
      valuesNode.set("values", objectNode);
      entriesArray.add(valuesNode);
    }
    
    resultNode.set("entries", entriesArray);
    mockServiceResponse.setResponseCode(200);

    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }
  
  public void getUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

      
    List<GrouperRemedyUser> grouperRemedyUsers = null;
    ByHqlStatic query = null;
    query = HibernateSession.byHqlStatic().createQuery("from GrouperRemedyUser");
    
    grouperRemedyUsers = query.list(GrouperRemedyUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    /**
     * //  {
    //    "entries": [
    //      {
    //        "values": {
    //          "Person ID": "PPL000000000306",
    //          "Remedy Login ID": "foundationdataadmin"
    //        },
    //        "_links": {
    //          "self": [
    //            {
    //              "href": "https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People/PPL000000000306"
    //            }
    //          ]
    //        }
    //      }
    //    ],
    //    "_links": {
    //      "self": [
    //        {
    //          "href": "https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People"
    //        }
    //      ]
    //    }
    //  }      
     */
    
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperRemedyUser grouperRemedyUser : grouperRemedyUsers) {
      
      ObjectNode valuesNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = grouperRemedyUser.toJson(null);
      valuesNode.set("values", objectNode);
      entriesArray.add(valuesNode);
    }
    
    resultNode.set("entries", entriesArray);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }
  
  public void getMemberships(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

      
    List<GrouperRemedyMembership> grouperRemedyMemberships = null;
    ByHqlStatic query = null;
    query = HibernateSession.byHqlStatic().createQuery("from GrouperRemedyMembership");
    
    grouperRemedyMemberships = query.list(GrouperRemedyMembership.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    //  {
    //    "entries": [
    //      {
    //        "values": {
    //          "People Permission Group ID": "EPG000000000101",
    //          "Permission Group": "2000000001",
    //          "Permission Group ID": 2000000001,
    //          "Person ID": "PPL000000000616",
    //          "Remedy Login ID": "benoff",
    //          "Status": "Enabled"
    //        },
    
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperRemedyMembership grouperRemedyMembership : grouperRemedyMemberships) {
      
      ObjectNode valuesNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = grouperRemedyMembership.toJson(null);
      valuesNode.set("values", objectNode);
      entriesArray.add(valuesNode);
    }
    
    resultNode.set("entries", entriesArray);
    
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

    String remedyLoginId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(remedyLoginId) > 0, "userId is required");
    
    List<GrouperRemedyUser> grouperRemedyUsers = HibernateSession.byHqlStatic().createQuery("from GrouperRemedyUser where remedyLoginId = :theId")
        .setString("remedyLoginId", remedyLoginId).list(GrouperRemedyUser.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    /**
     * //  {
    //    "entries": [
    //      {
    //        "values": {
    //          "Person ID": "PPL000000000306",
    //          "Remedy Login ID": "foundationdataadmin"
    //        },
    //        "_links": {
    //          "self": [
    //            {
    //              "href": "https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People/PPL000000000306"
    //            }
    //          ]
    //        }
    //      }
    //    ],
    //    "_links": {
    //      "self": [
    //        {
    //          "href": "https://school-dev-restapi.onbmc.com/api/arsys/v1/entry/CTM:People"
    //        }
    //      ]
    //    }
    //  }      
     */
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperRemedyUser grouperRemedyUser : grouperRemedyUsers) {
      
      ObjectNode valuesNode = GrouperUtil.jsonJacksonNode();
      ObjectNode objectNode = grouperRemedyUser.toJson(null);
      valuesNode.set("values", objectNode);
      entriesArray.add(valuesNode);
    }
    
    resultNode.set("entries", entriesArray);
    mockServiceResponse.setResponseCode(200);

    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
   
  }
  
  
  private void checkRequestContentType(MockServiceRequest mockServiceRequest) {
    if (!StringUtils.equals(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json")) {
      throw new RuntimeException("Content type must be application/json");
    }
  }
  
  
  public void associateOrDisassociateGroupWithUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);
    
    checkRequestContentType(mockServiceRequest);
    
    /**
     * ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode jsonObject = objectMapper.createObjectNode();
      ObjectNode valuesObject = objectMapper.createObjectNode();
      valuesObject.put("Permission Group ID", grouperRemedyGroup.getPermissionGroupId());
      valuesObject.put("Permission Group", grouperRemedyGroup.getPermissionGroup());
      valuesObject.put("Person ID", grouperRemedyUser.getPersonId());
      valuesObject.put("Remedy Login ID", grouperRemedyUser.getRemedyLoginId());
      valuesObject.put("Status", "Enabled");
      jsonObject.set("values", valuesObject);
     */

    String body = mockServiceRequest.getRequestBody();
    JsonNode mainNode = GrouperUtil.jsonJacksonNode(body);
    
    JsonNode valuesNode = GrouperUtil.jsonJacksonGetNode(mainNode, "values");
    
    String status = GrouperUtil.jsonJacksonGetString(valuesNode, "Status");
    String remedyLoginId = GrouperUtil.jsonJacksonGetString(valuesNode, "Remedy Login ID");
    String personId = GrouperUtil.jsonJacksonGetString(valuesNode, "Person ID");
    String permissionGroup = GrouperUtil.jsonJacksonGetString(valuesNode, "Permission Group");
    Long permissionGroupId = GrouperUtil.jsonJacksonGetLong(valuesNode, "Permission Group ID");
    
    List<GrouperRemedyUser> grouperRemedyUsers = HibernateSession.byHqlStatic()
        .createQuery("select user from GrouperRemedyUser user where user.remedyLoginId = :theId")
        .setString("theId", remedyLoginId).list(GrouperRemedyUser.class);
    
    if (GrouperUtil.length(grouperRemedyUsers) == 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    //check if group exists
    List<GrouperRemedyGroup> grouperRemedyGroups = HibernateSession.byHqlStatic()
        .createQuery("from GrouperRemedyGroup where permissionGroupId = :theId")
        .setLong("theId", permissionGroupId).list(GrouperRemedyGroup.class);
    
    if (GrouperUtil.length(grouperRemedyGroups) == 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    //check if this groupId and userId are already connected
    List<GrouperRemedyMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from GrouperRemedyMembership where remedyLoginId = :remedyLoginId and permissionGroupId = :permissionGroupId")
        .setString("remedyLoginId", remedyLoginId)
        .setLong("permissionGroupId", permissionGroupId)
        .list(GrouperRemedyMembership.class);
    
    String peoplePermissionGroupId = GrouperUuid.getUuid();
    
    if (GrouperUtil.length(memberships) == 0) {
      //now save the relationship
      GrouperRemedyMembership grouperRemedyMembership = new GrouperRemedyMembership();
      
      grouperRemedyMembership.setPeoplePermissionGroupId(peoplePermissionGroupId);
      grouperRemedyMembership.setPermissionGroup(permissionGroup);
      grouperRemedyMembership.setPermissionGroupId(permissionGroupId);
      grouperRemedyMembership.setPersonId(personId);
      grouperRemedyMembership.setRemedyLoginId(remedyLoginId);
      grouperRemedyMembership.setStatus(status);
      
      HibernateSession.byObjectStatic().save(grouperRemedyMembership); 
    } else {
      
      GrouperRemedyMembership existingMembership = memberships.get(0);
      existingMembership.setStatus(status);
      HibernateSession.byObjectStatic().update(existingMembership);
      
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
  }

  private static long lastDeleteMillis = -1;
  
  public void postAuth(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
        
    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.remedy.mock.configId");
    
    String username = mockServiceRequest.getHttpServletRequest().getParameter("username");
    String password = mockServiceRequest.getHttpServletRequest().getParameter("password");
      
    String expectedUsername = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyConnector."+configId+".username");
    String expectedPassword = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyConnector."+configId+".password");
    
    if (StringUtils.equals(username, expectedUsername) && StringUtils.equals(password, expectedPassword)) {
      String jwtToken = GrouperUuid.getUuid();
      //save jwt token in GrouperRemedyAuth
      GrouperRemedyAuth grouperRemedyAuth = new GrouperRemedyAuth();
      grouperRemedyAuth.setJwtToken(jwtToken);
      HibernateSession.byObjectStatic().save(grouperRemedyAuth);

      mockServiceResponse.setResponseCode(200);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(jwtToken);
    } else {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
    }
    
  }
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
   
    if (!mockTablesThere) {
      ensureRemedyMockTables();
    }
    
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    
    // remove all this: /api/arsys/v1/entry/
    mockNamePaths.remove("api");
    mockNamePaths.remove("arsys");
    mockNamePaths.remove("v1");
    mockNamePaths.remove("entry");
    
    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);
    
    mockServiceRequest.setPostMockNamePaths(paths);

    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("ENT:SYS-Access Permission Grps".equals(mockNamePaths.get(0)) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("ENT:SYS-Access Permission Grps".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("CTM:People".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getUsers(mockServiceRequest, mockServiceResponse);
        return;
      }

      if ("ENT:SYS People Entitlement Groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        getMemberships(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("CTM:People".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        getUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("token".equals(mockNamePaths.get(0))) {
        postAuth(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("ENT:SYS People Entitlement Groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        associateOrDisassociateGroupWithUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    if (StringUtils.equals("PUT", mockServiceRequest.getHttpServletRequest().getMethod())) {
      
      if ("ENT:SYS People Entitlement Groups".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        associateOrDisassociateGroupWithUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("ENT:SYS People Entitlement Groups".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        associateOrDisassociateGroupWithUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      
    }

    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
    
  }
  
}
