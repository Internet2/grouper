package edu.internet2.middleware.grouper.app.remedyV2.digitalMarketplace;

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

public class DigitalMarketplaceMockServiceHandler extends MockServiceHandler {

private static boolean mockTablesThere = false;
  
  public static void ensureDigitalMarketplaceMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_digital_marketplace_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_digital_marketplace_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_digital_marketplace_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_digital_mp_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperDigitalMarketplaceGroup.createTableDigitalMarketplaceGroup(ddlVersionBean, database);
          GrouperDigitalMarketplaceAuth.createTableDigitalMarketplaceAuth(ddlVersionBean, database);
          GrouperDigitalMarketplaceUser.createTableDigitalMarketplaceUser(ddlVersionBean, database);
          GrouperDigitalMarketplaceMembership.createTableDigitalMarketplaceMembership(ddlVersionBean, database);
          
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
    
    List<GrouperDigitalMarketplaceAuth> grouperDigitalMarketplaceAuths = 
        HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceAuth where jwtToken = :theAccessToken").setString("theAccessToken", authorizationToken).list(GrouperDigitalMarketplaceAuth.class);
    
    if (GrouperUtil.length(grouperDigitalMarketplaceAuths) != 1) {
      throw new RuntimeException("Invalid access token, not found!");
    }
    
    GrouperDigitalMarketplaceAuth grouperDigitalMarketplaceAuth = grouperDigitalMarketplaceAuths.get(0);    

    // all good
  }

  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    List<GrouperDigitalMarketplaceGroup> grouperDigitalMarketplaceGroups = null;
    ByHqlStatic query = null;
    query = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceGroup");
    
    grouperDigitalMarketplaceGroups = query.list(GrouperDigitalMarketplaceGroup.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    resultNode.put("totalSize", grouperDigitalMarketplaceGroups.size());
    
//  { 
    //    "totalSize":555,
    //    "data":[ 
    //       { 
    //          "groupName":"Administrator",
    //          "groupId":1,
    //          "longGroupName":"superuser to the AR System",
    //          "groupType":"Change",
    //          "parentGroup":null,
    //          "status":"Current",
    //          "comments":"Members have full and unlimited access to AR System, and thus can perform all operations\non all objects and data.",
    //          "createdBy":"AR System",
    //          "tags":null,
    //          "permittedGroupsBySecurityLabels":{ 
    //  
    //          },
    //          "permittedUsersBySecurityLabels":{ 
    //  
    //          },
    //          "floatingLicenses":0,
    //          "applicationFloatingLicenses":null,
    //          "groupCategory":"com.bmc.arsys.rx.services.group.domain.RegularGroup"
    //       }
    //    ]
    // } 
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup : grouperDigitalMarketplaceGroups) {
      
      ObjectNode objectNode = grouperDigitalMarketplaceGroup.toJson(null);
      
      entriesArray.add(objectNode);
    }
    
    resultNode.set("data", entriesArray);
    
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

    String groupName = mockServiceRequest.getHttpServletRequest().getParameter("groupName");
    
    GrouperUtil.assertion(GrouperUtil.length(groupName) > 0, "groupName is required");
    
    List<GrouperDigitalMarketplaceGroup> grouperDigitalMarketplaceGroups = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceGroup where groupName = :theGroupName")
        .setString("theGroupName", groupName).list(GrouperDigitalMarketplaceGroup.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup : grouperDigitalMarketplaceGroups) {
      
      ObjectNode objectNode = grouperDigitalMarketplaceGroup.toJson(null);
      entriesArray.add(objectNode);
    }
    
    resultNode.set("data", entriesArray);
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

      
    List<GrouperDigitalMarketplaceUser> grouperDigitalMarketplaceUsers = null;
    ByHqlStatic query = null;
    query = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceUser");
    
    grouperDigitalMarketplaceUsers = query.list(GrouperDigitalMarketplaceUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    //    { 
    //      "totalSize":44027,
    //      "data":[ 
    //         { 
    //            "fullName":"Hannah Admin",
    //            "loginName":"hannah_admin",
    //            "password":"**************************",
    //            "userId":"AGGADGJWHI4IZAPC012HPB3UI41XTN",
    //            "emailAddress":"",
    //            "groups":[ 
    //               "Administrator",
    //               "sbe-catalog-admins",
    //               "University of Pennsylvania",
    //               "General Access",
    //               "sbe-internal-suppliers"
    //            ],
    //            "computedGroups":[ 
    //               "CMDB SC Admin Group",
    //               "CMDB SC User Group",
    //               "AI Computed Group"
    //            ], ...
    //         }, 
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser : grouperDigitalMarketplaceUsers) {
      
      List<GrouperDigitalMarketplaceMembership> grouperDigitalMarketplaceMemberships = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceMembership where loginName = :theLoginName")
          .setString("theLoginName", grouperDigitalMarketplaceUser.getLoginName()).list(GrouperDigitalMarketplaceMembership.class);
      
      ArrayNode groupsArray = GrouperUtil.jsonJacksonArrayNode();
      
      for (GrouperDigitalMarketplaceMembership membership: grouperDigitalMarketplaceMemberships) {
        String groupName = membership.getGroupName();
        groupsArray.add(groupName);
      }
      
      ObjectNode objectNode = grouperDigitalMarketplaceUser.toJson(null);
      objectNode.set("groups", groupsArray);
      
      entriesArray.add(objectNode);
    }
    
    resultNode.set("data", entriesArray);
    resultNode.put("totalSize", grouperDigitalMarketplaceUsers.size());
    
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

    String loginName = mockServiceRequest.getHttpServletRequest().getParameter("loginName");
    
    GrouperUtil.assertion(GrouperUtil.length(loginName) > 0, "loginName is required");
    
    List<GrouperDigitalMarketplaceUser> grouperDigitalMarketplaceUsers = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceUser where loginName = :theLoginName")
        .setString("theLoginName", loginName).list(GrouperDigitalMarketplaceUser.class);

    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperDigitalMarketplaceUser grouperDigitalMarketplaceUser : grouperDigitalMarketplaceUsers) {
      
      List<GrouperDigitalMarketplaceMembership> grouperDigitalMarketplaceMemberships = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceMembership where loginName = :theLoginName")
          .setString("theLoginName", grouperDigitalMarketplaceUser.getLoginName()).list(GrouperDigitalMarketplaceMembership.class);
      
      ArrayNode groupsArray = GrouperUtil.jsonJacksonArrayNode();
      
      for (GrouperDigitalMarketplaceMembership membership: grouperDigitalMarketplaceMemberships) {
        String groupName = membership.getGroupName();
        groupsArray.add(groupName);
      }
      
      ObjectNode objectNode = grouperDigitalMarketplaceUser.toJson(null);
      objectNode.set("groups", groupsArray);
      
      entriesArray.add(objectNode);
    }
    
    resultNode.set("data", entriesArray);
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
    
    String body = mockServiceRequest.getRequestBody();
    JsonNode mainNode = GrouperUtil.jsonJacksonNode(body);
    
    String loginName = mockServiceRequest.getPostMockNamePaths()[1];
    
    JsonNode groupsNode = GrouperUtil.jsonJacksonGetNode(mainNode, "groups");
    ArrayNode groupsArrayNode = (ArrayNode) groupsNode;
    
    HibernateSession.byHqlStatic()
    .createQuery("delete from GrouperDigitalMarketplaceMembership where loginName = :loginName")
    .setString("loginName", loginName).executeUpdateInt();
    
    for (int i=0;i<groupsArrayNode.size();i++) {
      GrouperDigitalMarketplaceMembership membership = new GrouperDigitalMarketplaceMembership();
      
      membership.setGroupName(groupsArrayNode.get(i).asText());
      membership.setLoginName(loginName);
      
      HibernateSession.byObjectStatic().save(membership); 
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
  }

  private static long lastDeleteMillis = -1;
  
  public void postAuth(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
        
    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.digitalMarketplace.mock.configId");
    
    String body = mockServiceRequest.getRequestBody();
    JsonNode userPassword = GrouperUtil.jsonJacksonNode(body);
    
    String username = GrouperUtil.jsonJacksonGetString(userPassword, "id");
    String password = GrouperUtil.jsonJacksonGetString(userPassword, "password");
    
    String expectedUsername = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyDigitalMarketplaceConnector."+configId+".username");
    String expectedPassword = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.remedyDigitalMarketplaceConnector."+configId+".password");
    
    if (StringUtils.equals(username, expectedUsername) && StringUtils.equals(password, expectedPassword)) {
      String jwtToken = GrouperUuid.getUuid();
      //save jwt token in GrouperDigitalMarketplaceAuth
      GrouperDigitalMarketplaceAuth grouperDigitalMarketplaceAuth = new GrouperDigitalMarketplaceAuth();
      grouperDigitalMarketplaceAuth.setJwtToken(jwtToken);
      HibernateSession.byObjectStatic().save(grouperDigitalMarketplaceAuth);

      mockServiceResponse.setResponseCode(200);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(jwtToken);
    } else {
      mockServiceResponse.setResponseCode(400);
      mockServiceResponse.setContentType("application/json");
    }
    
  }
  
  public void postGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    checkRequestContentType(mockServiceRequest);

    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);

    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "groupName")) > 0, "groupName is required");

    List<GrouperDigitalMarketplaceGroup> grouperDigitalMarketplaceGroups = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceGroup where groupName = :theGroupName")
        .setString("theGroupName", GrouperUtil.jsonJacksonGetString(groupJsonNode, "groupName")).list(GrouperDigitalMarketplaceGroup.class);
    
    if (grouperDigitalMarketplaceGroups.size() > 0) {
      mockServiceResponse.setResponseCode(204);
      mockServiceResponse.setContentType("application/json");
      return;
    }
    
    GrouperDigitalMarketplaceGroup grouperDigitalMarketplaceGroup = GrouperDigitalMarketplaceGroup.fromJson(groupJsonNode);
    
    HibernateSession.byObjectStatic().save(grouperDigitalMarketplaceGroup);
    
    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
  }
  
  public void deleteGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    checkAuthorization(mockServiceRequest);

    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    
    String groupName = mockNamePaths.get(1);
    
    List<GrouperDigitalMarketplaceGroup> grouperDigitalMarketplaceGroups = HibernateSession.byHqlStatic().createQuery("from GrouperDigitalMarketplaceGroup where groupName = :theGroupName")
        .setString("theGroupName", groupName).list(GrouperDigitalMarketplaceGroup.class);
    
    if (grouperDigitalMarketplaceGroups.size() == 0) {
      mockServiceResponse.setResponseCode(404);
      mockServiceResponse.setContentType("application/json");
      return;
    }
    
    HibernateSession.byHqlStatic().createQuery("delete from GrouperDigitalMarketplaceGroup where groupName = :theGroupName")
      .setString("theGroupName", groupName).executeUpdate();
    
    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
  }
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
   
    if (!mockTablesThere) {
      ensureDigitalMarketplaceMockTables();
    }
    
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    // http://localhost:8080/grouper/mockServices/digitalMarketplace/api/rx/application/
    // datapage?startIndex=0&dataPageType=com.bmc.arsys.rx.application.group.datapage.GroupDataPageQuery&pageSize=-1
    // remove all this: /api/rx/application
    mockNamePaths.remove("api");
    mockNamePaths.remove("rx");
    mockNamePaths.remove("application");
    
    /**
     * paramMap.put("dataPageType", "com.bmc.arsys.rx.application.user.datapage.UserDataPageQuery");
      paramMap.put("pageSize", "1");
      paramMap.put("startIndex", "0");
     */
    
    String[] paths = new String[mockNamePaths.size()];
    paths = mockNamePaths.toArray(paths);
    
    mockServiceRequest.setPostMockNamePaths(paths);

    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
      
      if ("datapage".equals(mockNamePaths.get(0)) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        
        String dataPageType = mockServiceRequest.getHttpServletRequest().getParameter("dataPageType");
        if (StringUtils.equals(dataPageType, "com.bmc.arsys.rx.application.group.datapage.GroupDataPageQuery")) {
          String groupName = mockServiceRequest.getHttpServletRequest().getParameter("groupName");
          if (StringUtils.isBlank(groupName)) {
            getGroups(mockServiceRequest, mockServiceResponse);
            return;
          }
          getGroup(mockServiceRequest, mockServiceResponse);
          return;
        } else if (StringUtils.equals(dataPageType, "com.bmc.arsys.rx.application.user.datapage.UserDataPageQuery")) {
          String loginName = mockServiceRequest.getHttpServletRequest().getParameter("loginName");
          if (StringUtils.isBlank(loginName)) {
            getUsers(mockServiceRequest, mockServiceResponse);
            return;
          }
          getUser(mockServiceRequest, mockServiceResponse);
          return;
        }
        
      }
    }
    
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("token".equals(mockNamePaths.get(0))) {
        postAuth(mockServiceRequest, mockServiceResponse);
        return;
      }
      
      if ("group".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        postGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    if (StringUtils.equals("DELETE", mockServiceRequest.getHttpServletRequest().getMethod())) {
      
      if ("group".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        deleteGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    
    if (StringUtils.equals("PUT", mockServiceRequest.getHttpServletRequest().getMethod())) {
      
      if ("user".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
        associateOrDisassociateGroupWithUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      
    }

    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
    
  }

}
