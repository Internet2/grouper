package edu.internet2.middleware.grouper.app.boxProvisioner;

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

import org.apache.commons.lang.StringUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.RSAKeyProvider;
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
import edu.internet2.middleware.grouper.internal.dao.QueryOptions;
import edu.internet2.middleware.grouper.internal.dao.QuerySort;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;

public class BoxMockServiceHandler extends MockServiceHandler {
  
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

  private static boolean mockTablesThere = false;
  
  public static void ensureBoxMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_box_group").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_box_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_box_auth").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_box_membership").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperMockDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperBoxGroup.createTableBoxGroup(ddlVersionBean, database);
          GrouperBoxAuth.createTableBoxAuth(ddlVersionBean, database);
          GrouperBoxUser.createTableBoxUser(ddlVersionBean, database);
          GrouperBoxMembership.createTableBoxMembership(ddlVersionBean, database);
          
        }
      });
  
    }    
  }
  
  public void checkAuthorization(MockServiceRequest mockServiceRequest) {
    
    String bearerToken = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (!bearerToken.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization token must start with 'Bearer '");
    }
    String authorizationToken = GrouperUtil.prefixOrSuffix(bearerToken, "Bearer ", false);
    
    List<GrouperBoxAuth> grouperBoxAuths = 
        HibernateSession.byHqlStatic().createQuery("from GrouperBoxAuth where accessToken = :theAccessToken").setString("theAccessToken", authorizationToken).list(GrouperBoxAuth.class);
    
    if (GrouperUtil.length(grouperBoxAuths) != 1) {
      throw new RuntimeException("Invalid access token, not found!");
    }
    
    GrouperBoxAuth grouperBoxAuth = grouperBoxAuths.get(0);    

    // all good
  }

  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    String limit = mockServiceRequest.getHttpServletRequest().getParameter("limit");
    String offset = mockServiceRequest.getHttpServletRequest().getParameter("offset");
    String fields = mockServiceRequest.getHttpServletRequest().getParameter("fields");
    String filterTerm = mockServiceRequest.getHttpServletRequest().getParameter("filter_term");
      
    int limitInt = 1000;
    if (StringUtils.isNotBlank(limit)) {
      limitInt = GrouperUtil.intValue(limit);
      if (limitInt <= 0) {
        throw new RuntimeException("limit cannot be less than or equal to 0.");
      }
      if (limitInt > 1000) {
        limitInt = 1000;
      }
    }
    
    int offsetInt = 0;
    if (StringUtils.isNotBlank(offset)) {
      offsetInt = GrouperUtil.intValue(offset);
      if (offsetInt < 0) {
        throw new RuntimeException("offset cannot be less than 0.");
      }
    }

    List<GrouperBoxGroup> grouperBoxGroups = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();
    if (StringUtils.isNotBlank(filterTerm)) {
      query = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup where name like :theName");
      query.setString("theName", filterTerm);
    } else {
      query = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup");
    }
    
    queryOptions.paging(limitInt, 1, true);
    
    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);
    
    grouperBoxGroups = query.list(GrouperBoxGroup.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    /**
     * {
        "entries": [
          {
            "id": 11446498,
            "type": "group",
            "name": "Support",
            "group_type": "managed_group"
          }
        ],
        "limit": 1000,
        "offset": 2000,
        "order": [
          {
            "by": "type",
            "direction": "ASC"
          }
        ],
        "total_count": 5000
        }
     */
    
    int totalRecordCount = queryOptions.getQueryPaging().getTotalRecordCount();
    resultNode.put("total_count", totalRecordCount);
    resultNode.put("offset", offsetInt);
    resultNode.put("limit", limitInt);
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperBoxGroup grouperBoxGroup : grouperBoxGroups) {
      ObjectNode objectNode = grouperBoxGroup.toJson(null);
      entriesArray.add(objectNode);
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

    String groupId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(groupId) > 0, "groupId is required");
    
    String fields = mockServiceRequest.getHttpServletRequest().getParameter("fields");
    
    List<GrouperBoxGroup> grouperBoxGroups = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup where id = :theId")
        .setString("theId", groupId).list(GrouperBoxGroup.class);

    /**
     * {
        "id": 11446498,
        "type": "group",
        "created_at": "2012-12-12T10:53:43-08:00",
        "group_type": "managed_group",
        "modified_at": "2012-12-12T10:53:43-08:00",
        "name": "Support"
      }
     */
    if (GrouperUtil.length(grouperBoxGroups) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      ObjectNode objectNode = grouperBoxGroups.get(0).toJson(null);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(grouperBoxGroups) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsById: " + GrouperUtil.length(grouperBoxGroups) + ", id: " + groupId);
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
    String offset = mockServiceRequest.getHttpServletRequest().getParameter("offset");
    String fields = mockServiceRequest.getHttpServletRequest().getParameter("fields");
    String filterTerm = mockServiceRequest.getHttpServletRequest().getParameter("filter_term");
      
    int limitInt = 1000;
    if (StringUtils.isNotBlank(limit)) {
      limitInt = GrouperUtil.intValue(limit);
      if (limitInt <= 0) {
        throw new RuntimeException("limit cannot be less than or equal to 0.");
      }
      if (limitInt > 1000) {
        limitInt = 1000;
      }
    }
    
    int offsetInt = 0;
    if (StringUtils.isNotBlank(offset)) {
      offsetInt = GrouperUtil.intValue(offset);
      if (offsetInt < 0) {
        throw new RuntimeException("offset cannot be less than 0.");
      }
    }

    List<GrouperBoxUser> grouperBoxUsers = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();
    if (StringUtils.isNotBlank(filterTerm)) {
      query = HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser where name like :theName or login like :theLogin ");
      query.setString("theName", filterTerm);
      query.setString("theLogin", filterTerm);
    } else {
      query = HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser");
    }
    
    queryOptions.paging(limitInt, 1, true);
    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);
    
    grouperBoxUsers = query.list(GrouperBoxUser.class);
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    /**
     * {
        "entries": [
          {
            "id": 11446498,
            "type": "user",
            "name": "Aaron Levie",
            "login": "ceo@example.com",
            "created_at": "2012-12-12T10:53:43-08:00",
            "modified_at": "2012-12-12T10:53:43-08:00",
            "language": "en",
            "timezone": "Africa/Bujumbura",
            "space_amount": 11345156112,
            "space_used": 1237009912,
            "max_upload_size": 2147483648,
            "status": "active",
            "job_title": "CEO",
            "phone": 6509241374,
            "address": "900 Jefferson Ave, Redwood City, CA 94063",
            "avatar_url": "https://www.box.com/api/avatar/large/181216415",
            "notification_email": {
              "email": "notifications@example.com",
              "is_confirmed": true
            }
          }
        ],
        "limit": 1000,
        "offset": 2000,
        "order": [
          {
            "by": "type",
            "direction": "ASC"
          }
        ],
        "total_count": 5000
      }
     */
    
    int totalRecordCount = queryOptions.getQueryPaging().getTotalRecordCount();
    resultNode.put("total_count", totalRecordCount);
    resultNode.put("offset", offsetInt);
    resultNode.put("limit", limitInt);
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperBoxUser grouperBoxUser : grouperBoxUsers) {
      ObjectNode objectNode = grouperBoxUser.toJson(null);
      entriesArray.add(objectNode);
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

    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(userId) > 0, "userId is required");
    
    String fields = mockServiceRequest.getHttpServletRequest().getParameter("fields");
    
    List<GrouperBoxUser> grouperBoxUsers = HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser where id = :theId")
        .setString("theId", userId).list(GrouperBoxUser.class);

    /**
     * {
          "id": 11446498,
          "type": "user",
          "address": "900 Jefferson Ave, Redwood City, CA 94063",
          "avatar_url": "https://www.box.com/api/avatar/large/181216415",
          "created_at": "2012-12-12T10:53:43-08:00",
          "job_title": "CEO",
          "language": "en",
          "login": "ceo@example.com",
          "max_upload_size": 2147483648,
          "modified_at": "2012-12-12T10:53:43-08:00",
          "name": "Aaron Levie",
          "notification_email": {
            "email": "notifications@example.com",
            "is_confirmed": true
          },
          "phone": 6509241374,
          "space_amount": 11345156112,
          "space_used": 1237009912,
          "status": "active",
          "timezone": "Africa/Bujumbura"
        }
     */
    if (GrouperUtil.length(grouperBoxUsers) == 1) {
      mockServiceResponse.setResponseCode(200);

      mockServiceResponse.setContentType("application/json");
      ObjectNode objectNode = grouperBoxUsers.get(0).toJson(null);
      
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));

    } else if (GrouperUtil.length(grouperBoxUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersById: " + GrouperUtil.length(grouperBoxUsers) + ", id: " + userId);
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
    .createQuery("delete from GrouperBoxMembership where groupId = :groupId")
    .setString("groupId", groupId).executeUpdateInt();
    
    int groupsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperBoxGroup where id = :theId")
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
    .createQuery("delete from GrouperBoxMembership where userId = :userId")
    .setString("userId", userId).executeUpdateInt();
    
    HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperBoxUser where id = :theId")
        .setString("theId", userId).executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
        
  }
  
  public void disassociateGroupFromUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    try {      
      checkAuthorization(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String membershipId = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(membershipId) > 0, "membershipId is required");
    
    //check if membership exists
    List<GrouperBoxMembership> grouperGoogleMemberships = HibernateSession.byHqlStatic().createQuery("select membership from GrouperBoxMembership membership where id = :theId")
        .setString("theId", membershipId).list(GrouperBoxMembership.class);
    
    if (GrouperUtil.length(grouperGoogleMemberships) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    HibernateSession.byHqlStatic()
      .createQuery("delete from GrouperBoxMembership where id = :theId")
      .setString("theId", membershipId)
      .executeUpdateInt();

    mockServiceResponse.setResponseCode(204);
  }
  
  private void checkRequestContentType(MockServiceRequest mockServiceRequest) {
    if (!StringUtils.equals(mockServiceRequest.getHttpServletRequest().getContentType(), "application/json")) {
      throw new RuntimeException("Content type must be application/json");
    }
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
    GrouperBoxGroup grouperBoxGroup = GrouperBoxGroup.fromJson(groupJsonNode);
    
    grouperBoxGroup.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperBoxGroup);
    
    ObjectNode objectNode = grouperBoxGroup.toJson(null);
    
    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
    
  }
  
  public void postUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }
    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);

    GrouperBoxUser grouperBoxUser = GrouperBoxUser.fromJson(userJsonNode);
    grouperBoxUser.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperBoxUser);
    
    JsonNode resultNode = grouperBoxUser.toJson(null);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }
  
  public void associateGroupWithUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    checkAuthorization(mockServiceRequest);
    
    checkRequestContentType(mockServiceRequest);
    
    /**
     * {
         "user": {
           "id": "1434325"
         },
         "group": {
           "id": "4545523"
         }
       }
     */

    String body = mockServiceRequest.getRequestBody();
    JsonNode mainNode = GrouperUtil.jsonJacksonNode(body);
    
    JsonNode userNode = GrouperUtil.jsonJacksonGetNode(mainNode, "user");
    JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(mainNode, "group");
    
    String userId = GrouperUtil.jsonJacksonGetString(userNode, "id");
    String groupId = GrouperUtil.jsonJacksonGetString(groupNode, "id");
    
    //check if userid exists
    List<GrouperBoxUser> grouperBoxUsers = HibernateSession.byHqlStatic()
        .createQuery("select user from GrouperBoxUser user where user.id = :theId")
        .setString("theId", userId).list(GrouperBoxUser.class);
    
    if (GrouperUtil.length(grouperBoxUsers) == 0) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    
    //check if group exists
    List<GrouperBoxGroup> grouperBoxGroups = HibernateSession.byHqlStatic()
        .createQuery("from GrouperBoxGroup where id = :theId")
        .setString("theId", groupId).list(GrouperBoxGroup.class);
    
    if (GrouperUtil.length(grouperBoxGroups) == 0) {
      mockServiceResponse.setResponseCode(400);
      return;
    }
    
    //check if this groupId and userId are already connected
    List<GrouperBoxMembership> memberships = HibernateSession.byHqlStatic()
        .createQuery("from GrouperBoxMembership m where m.userId = :userId and m.groupId = :groupId ")
        .setString("userId", userId)
        .setString("groupId", groupId)
        .list(GrouperBoxMembership.class);
    
    String uuidToInsert = GrouperUuid.getUuid();
    
    if (GrouperUtil.length(memberships) == 0) {
      
      //now save the relationship
      GrouperBoxMembership grouperBoxMembership = new GrouperBoxMembership();
      grouperBoxMembership.setGroupId(groupId);
      grouperBoxMembership.setUserId(userId);
      grouperBoxMembership.setId(uuidToInsert);
      
      HibernateSession.byObjectStatic().save(grouperBoxMembership); 
      
    }
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    /**
     * {
        "id": 11446498,
        "type": "group_membership",
        "created_at": "2012-12-12T10:53:43-08:00",
        "group": {
          "id": 11446498,
          "type": "group",
          "group_type": "managed_group",
          "name": "Support"
        },
        "modified_at": "2012-12-12T10:53:43-08:00",
        "role": "member",
        "user": {
          "id": 11446498,
          "type": "user",
          "login": "ceo@example.com",
          "name": "Aaron Levie"
        }
}
     */
    
    ObjectNode groupNodeResult = GrouperUtil.jsonJacksonNode();
    groupNodeResult.put("id", groupId);
    groupNodeResult.put("type",grouperBoxGroups.get(0).getType());
    groupNodeResult.put("group_type", grouperBoxGroups.get(0).getGroupType());
    groupNodeResult.put("name", grouperBoxGroups.get(0).getName());
    
    ObjectNode userNodeResult = GrouperUtil.jsonJacksonNode();
    userNodeResult.put("id", userId);
    groupNodeResult.put("type", grouperBoxUsers.get(0).getType());
    groupNodeResult.put("name", grouperBoxUsers.get(0).getName());
    groupNodeResult.put("login", grouperBoxUsers.get(0).getLogin());
    
    resultNode.put("id", uuidToInsert);
    resultNode.put("type", "group_membership");
    resultNode.put("role", "member");
    
    resultNode.set("group", groupNodeResult);
    resultNode.set("user", userNodeResult);
    
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode)); 
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

    List<GrouperBoxGroup> grouperBoxGroups = HibernateSession.byHqlStatic().createQuery(
        "from GrouperBoxGroup where id = :theId")
        .setString("theId", groupId).list(GrouperBoxGroup.class);
    
    if (GrouperUtil.length(grouperBoxGroups) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindGroup", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperBoxGroups) > 1) {
      throw new RuntimeException("Found multiple matched groups! " + GrouperUtil.length(grouperBoxGroups));
    }
    GrouperBoxGroup grouperBoxGroup = grouperBoxGroups.get(0);
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);
    GrouperBoxGroup grouperBoxGroupToBeUpdated = GrouperBoxGroup.fromJson(groupJsonNode);
    
    if (StringUtils.isNotBlank(grouperBoxGroupToBeUpdated.getName())) {
      grouperBoxGroup.setName(grouperBoxGroupToBeUpdated.getName());
    }
    
    if (StringUtils.isNotBlank(grouperBoxGroupToBeUpdated.getType())) {
      grouperBoxGroup.setType(grouperBoxGroupToBeUpdated.getType());
    }
    
    if (StringUtils.isNotBlank(grouperBoxGroupToBeUpdated.getDescription())) {
      grouperBoxGroup.setDescription(grouperBoxGroupToBeUpdated.getDescription());
    }
    
    if (StringUtils.isNotBlank(grouperBoxGroupToBeUpdated.getExternalSyncIdentifier())) {
      grouperBoxGroup.setExternalSyncIdentifier(grouperBoxGroupToBeUpdated.getExternalSyncIdentifier());
    }
    
    if (StringUtils.isNotBlank(grouperBoxGroupToBeUpdated.getGroupType())) {
      grouperBoxGroup.setGroupType(grouperBoxGroupToBeUpdated.getGroupType());
    }
    
    if (StringUtils.isNotBlank(grouperBoxGroupToBeUpdated.getInvitabilityLevel())) {
      grouperBoxGroup.setInvitabilityLevel(grouperBoxGroupToBeUpdated.getInvitabilityLevel());
    }
    
    if (StringUtils.isNotBlank(grouperBoxGroupToBeUpdated.getMemberViewabilityLevel())) {
      grouperBoxGroup.setMemberViewabilityLevel(grouperBoxGroupToBeUpdated.getMemberViewabilityLevel());
    }
    
    if (StringUtils.isNotBlank(grouperBoxGroupToBeUpdated.getProvenance())) {
      grouperBoxGroup.setProvenance(grouperBoxGroupToBeUpdated.getProvenance());
    }
    
    if (grouperBoxGroup.isCanInviteAsCollaborator() != grouperBoxGroupToBeUpdated.isCanInviteAsCollaborator()) {
      grouperBoxGroup.setCanInviteAsCollaborator(grouperBoxGroupToBeUpdated.isCanInviteAsCollaborator());
    }
    
    HibernateSession.byObjectStatic().saveOrUpdate(grouperBoxGroup);
    
    ObjectNode objectNode = grouperBoxGroup.toJson(null);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode)); 
  }
  
  public void updateUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    try {      
      checkAuthorization(mockServiceRequest);
      checkRequestContentType(mockServiceRequest);
    } catch (Exception e) {
      mockServiceResponse.setResponseCode(401);
      return;
    }

    // patch a user
    String userId = mockServiceRequest.getPostMockNamePaths()[1];
    
    mockServiceRequest.getDebugMap().put("userId", userId);

    List<GrouperBoxUser> grouperBoxUsers = HibernateSession.byHqlStatic().createQuery(
        "from GrouperBoxUser where id = :theId")
        .setString("theId", userId).list(GrouperBoxUser.class);
    
    if (GrouperUtil.length(grouperBoxUsers) == 0) {
      mockServiceRequest.getDebugMap().put("cantFindUser", true);
      mockServiceResponse.setResponseCode(404);
      return;
    }
    if (GrouperUtil.length(grouperBoxUsers) > 1) {
      throw new RuntimeException("Found multiple matched users! " + GrouperUtil.length(grouperBoxUsers));
    }
    GrouperBoxUser grouperBoxUser = grouperBoxUsers.get(0);
    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);
    GrouperBoxUser grouperBoxUserToBeUpdated = GrouperBoxUser.fromJson(userJsonNode);
    
    if (StringUtils.isNotBlank(grouperBoxUserToBeUpdated.getName())) {
      grouperBoxUser.setName(grouperBoxUserToBeUpdated.getName());
    }
    
    if (StringUtils.isNotBlank(grouperBoxUserToBeUpdated.getType())) {
      grouperBoxUser.setType(grouperBoxUserToBeUpdated.getType());
    }
    
    if (StringUtils.isNotBlank(grouperBoxUserToBeUpdated.getLogin())) {
      grouperBoxUser.setLogin(grouperBoxUserToBeUpdated.getLogin());
    }
    
    if (StringUtils.isNotBlank(grouperBoxUserToBeUpdated.getStatus())) {
      grouperBoxUser.setStatus(grouperBoxUserToBeUpdated.getStatus());
    }

    if (StringUtils.isNotBlank(grouperBoxUserToBeUpdated.getRole())) {
      grouperBoxUser.setRole(grouperBoxUserToBeUpdated.getRole());
    }

    if (grouperBoxUserToBeUpdated.getSpaceUsed() != null) {
      grouperBoxUser.setSpaceUsed(grouperBoxUserToBeUpdated.getSpaceUsed());
    }

    if (grouperBoxUserToBeUpdated.getSpaceAmount() != null) {
      grouperBoxUser.setSpaceAmount(grouperBoxUserToBeUpdated.getSpaceAmount());
    }

    if (grouperBoxUserToBeUpdated.getMaxUploadSize() != null) {
      grouperBoxUser.setMaxUploadSize(grouperBoxUserToBeUpdated.getMaxUploadSize());
    }
    
    if (grouperBoxUser.isExemptFromDeviceLimits() != grouperBoxUserToBeUpdated.isExemptFromDeviceLimits()) {
      grouperBoxUser.setExemptFromDeviceLimits(grouperBoxUserToBeUpdated.isExemptFromDeviceLimits());
    }

    if (grouperBoxUser.isExemptFromLoginVerification() != grouperBoxUserToBeUpdated.isExemptFromLoginVerification()) {
      grouperBoxUser.setExemptFromLoginVerification(grouperBoxUserToBeUpdated.isExemptFromLoginVerification());
    }

    if (grouperBoxUser.isExternalCollabRestricted() != grouperBoxUserToBeUpdated.isExternalCollabRestricted()) {
      grouperBoxUser.setExternalCollabRestricted(grouperBoxUserToBeUpdated.isExternalCollabRestricted());
    }

    if (grouperBoxUser.isPlatformAccessOnly() != grouperBoxUserToBeUpdated.isPlatformAccessOnly()) {
      grouperBoxUser.setPlatformAccessOnly(grouperBoxUserToBeUpdated.isPlatformAccessOnly());
    }

    if (grouperBoxUser.isSyncEnabled() != grouperBoxUserToBeUpdated.isSyncEnabled()) {
      grouperBoxUser.setSyncEnabled(grouperBoxUserToBeUpdated.isSyncEnabled());
    }

    if (grouperBoxUser.isCanSeeManagedUsers() != grouperBoxUserToBeUpdated.isCanSeeManagedUsers()) {
      grouperBoxUser.setCanSeeManagedUsers(grouperBoxUserToBeUpdated.isCanSeeManagedUsers());
    }
    
    HibernateSession.byObjectStatic().saveOrUpdate(grouperBoxUser);
    
    ObjectNode objectNode = grouperBoxUser.toJson(null);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode)); 
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
    
    String limit = mockServiceRequest.getHttpServletRequest().getParameter("limit");
    String offset = mockServiceRequest.getHttpServletRequest().getParameter("offset");
      
    int limitInt = 1000;
    if (StringUtils.isNotBlank(limit)) {
      limitInt = GrouperUtil.intValue(limit);
      if (limitInt <= 0) {
        throw new RuntimeException("limit cannot be less than or equal to 0.");
      }
      if (limitInt > 1000) {
        limitInt = 1000;
      }
    }
    
    int offsetInt = 0;
    if (StringUtils.isNotBlank(offset)) {
      offsetInt = GrouperUtil.intValue(offset);
      if (offsetInt < 0) {
        throw new RuntimeException("offset cannot be less than 0.");
      }
    }

    List<GrouperBoxGroup> grouperBoxGroups = HibernateSession.byHqlStatic().createQuery("from GrouperBoxGroup where id = :theId")
        .setString("theId", groupId).list(GrouperBoxGroup.class);

    if (GrouperUtil.length(grouperBoxGroups) != 1) {
      throw new RuntimeException("getUsersByGroup: " + GrouperUtil.length(grouperBoxGroups) + ", id: " + groupId);
    }
    
    List<GrouperBoxMembership> grouperBoxMemberships = null;
    ByHqlStatic query = null;
    QueryOptions queryOptions = new QueryOptions();
   
    query = HibernateSession.byHqlStatic()
        .createQuery("from GrouperBoxMembership m where m.groupId = :theGroupId ")
        .setString("theGroupId", groupId);
    
    queryOptions.paging(limitInt, 1, true);
    queryOptions.sort(new QuerySort("id", true));
    query.options(queryOptions);
    
    grouperBoxMemberships = query.list(GrouperBoxMembership.class);
    
    
    /**
     * {
          "entries": [
            {
              "id": 11446498,
              "type": "group_membership",
              "user": {
                "id": 11446498,
                "type": "user",
                "name": "Aaron Levie",
                "login": "ceo@example.com"
              },
              "group": {
                "id": 11446498,
                "type": "group",
                "name": "Support",
                "group_type": "managed_group"
              },
              "role": "member",
              "created_at": "2012-12-12T10:53:43-08:00",
              "modified_at": "2012-12-12T10:53:43-08:00"
            }
          ],
          "limit": 1000,
          "offset": 2000,
          "order": [
            {
              "by": "type",
              "direction": "ASC"
            }
          ],
          "total_count": 5000
        }
     */
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    int totalRecordCount = queryOptions.getQueryPaging().getTotalRecordCount();
    resultNode.put("total_count", totalRecordCount);
    resultNode.put("offset", offsetInt);
    resultNode.put("limit", limitInt);
    
    ArrayNode entriesArray = GrouperUtil.jsonJacksonArrayNode();
    
    ObjectNode groupNode = grouperBoxGroups.get(0).toJson(null);
    
    for (GrouperBoxMembership grouperBoxMembership : grouperBoxMemberships) {
      
      ObjectNode singleEntry = GrouperUtil.jsonJacksonNode();
      singleEntry.put("id", grouperBoxMembership.getId());
      singleEntry.put("type", "group_membership");
      singleEntry.set("group", groupNode);
      
      //not efficient but it's only a mock server
      List<GrouperBoxUser> grouperBoxUsers = HibernateSession.byHqlStatic().createQuery("from GrouperBoxUser where id = :theId")
          .setString("theId", grouperBoxMembership.getUserId()).list(GrouperBoxUser.class);
      if (grouperBoxUsers.size() != 1) {
        throw new RuntimeException("grouperBoxUsers size must be 1 for userId "+grouperBoxMembership.getUserId());
      }
      ObjectNode userNode = grouperBoxUsers.get(0).toJson(null);
      singleEntry.set("user", userNode);
      
      entriesArray.add(singleEntry);
    }
    
    resultNode.set("entries", entriesArray);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }
  
  private static long lastDeleteMillis = -1;
  
  public void postAuth(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    String grantType = mockServiceRequest.getHttpServletRequest().getParameter("grant_type");
    String assertion = mockServiceRequest.getHttpServletRequest().getParameter("assertion");
    String clientId = mockServiceRequest.getHttpServletRequest().getParameter("client_id");
    String clientSecret = mockServiceRequest.getHttpServletRequest().getParameter("client_secret");
    
    if (StringUtils.isBlank(grantType) || StringUtils.isBlank(clientId) || StringUtils.isBlank(clientSecret)) {
      throw new RuntimeException("grant_type, assertion, client_id, and client_secret are required!");
    }
    
    if (!StringUtils.equals(grantType, "urn:ietf:params:oauth:grant-type:jwt-bearer") &&
        !StringUtils.equals(grantType, "client_credentials")) {
      throw new RuntimeException("grant_type must be set to urn:ietf:params:oauth:grant-type:jwt-bearer or client_credentials");
    }
    
    if (StringUtils.equals("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer") && StringUtils.isBlank(assertion)) {
      throw new RuntimeException("assertion is required for grant type: urn:ietf:params:oauth:grant-type:jwt-bearer");
    }
    
    if (StringUtils.equals(grantType, "urn:ietf:params:oauth:grant-type:jwt-bearer") && StringUtils.isBlank(assertion)) {
      throw new RuntimeException("For grant_type urn:ietf:params:oauth:grant-type:jwt-bearer, assertion must be set.");
    }

    String configId = GrouperConfig.retrieveConfig().propertyValueString("grouperTest.box.mock.configId");
    
    if (StringUtils.equals(grantType, "urn:ietf:params:oauth:grant-type:jwt-bearer")) {
      
      DecodedJWT decodedJwt = JWT.decode(assertion);
      
      BoxMockRsaKeyProvider boxMockRsaKeyProvider = new BoxMockRsaKeyProvider();
      
      Algorithm.RSA512(boxMockRsaKeyProvider).verify(decodedJwt);


      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
      //expires in an hour
      long expiresOnSeconds = 60*60;
      
      resultNode.put("expires_in", expiresOnSeconds);
      
      String accessToken = GrouperUuid.getUuid();
      
      GrouperBoxAuth grouperBoxAuth = new GrouperBoxAuth();
      grouperBoxAuth.setConfigId(configId);
      grouperBoxAuth.setAccessToken(accessToken);
      grouperBoxAuth.setExpiresOnSeconds(expiresOnSeconds);
      HibernateSession.byObjectStatic().save(grouperBoxAuth);
      
      resultNode.put("access_token", accessToken);
      
      mockServiceResponse.setResponseCode(200);
      mockServiceResponse.setContentType("application/json");
      mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
      
      
    } else if (StringUtils.equals(grantType, "client_credentials")) {
      
      String expectedClientId = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector."+configId+".clientId");
      if (StringUtils.isBlank(expectedClientId)) {
        expectedClientId = "put client id here that you have in box provisioner test";
      }

      String expectedClientSecret = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector."+configId+".clientSecret");
      if (StringUtils.isBlank(expectedClientSecret)) {
        expectedClientSecret = "put client secret here that you have in box provisioner test";
      }
      
      if (!StringUtils.equals(expectedClientId, clientId) && !StringUtils.equals(expectedClientSecret, clientSecret)) {
        throw new RuntimeException("client id and/or client secret don't match");
      }
      
      String boxSubjectType = mockServiceRequest.getHttpServletRequest().getParameter("box_subject_type");
      String boxSubjectId = mockServiceRequest.getHttpServletRequest().getParameter("box_subject_id");
      
      if (!StringUtils.equals(boxSubjectType, "enterprise")) {
        throw new RuntimeException("box_subject_type must be set to 'enterprise'");
      }
      
      String expectedEnterpriseId = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector."+configId+".enterpriseId");
      if (StringUtils.isBlank(expectedEnterpriseId)) {
        expectedEnterpriseId = "put enterprise id here that you have in box provisioner test";
      }
      
      if (!StringUtils.equals(expectedEnterpriseId, boxSubjectId)) {
        throw new RuntimeException("box_subject_id must be set to the correct enterprise id");
      }
      
      ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
      
      long expiresOnSeconds = 60*60;
      
      resultNode.put("expires_in", expiresOnSeconds);
      
      String accessToken = GrouperUuid.getUuid();
      
      GrouperBoxAuth grouperBoxAuth = new GrouperBoxAuth();
      grouperBoxAuth.setConfigId(configId);
      grouperBoxAuth.setAccessToken(accessToken);
      grouperBoxAuth.setExpiresOnSeconds(expiresOnSeconds);
      HibernateSession.byObjectStatic().save(grouperBoxAuth);
      
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
        .createQuery("delete from GrouperBoxAuth where expiresOnSeconds < :theExpiresOnSeconds")
        .setLong("theExpiresOnSeconds", secondsToDelete).executeUpdateInt();
      
      if (accessTokensDeleted > 0) {
        mockServiceRequest.getDebugMap().put("accessTokensDeleted", accessTokensDeleted);
      }
    }
    
  }
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
   
    if (!mockTablesThere) {
      ensureBoxMockTables();
    }
    
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }
    
    List<String> mockNamePaths = GrouperUtil.toList(mockServiceRequest.getPostMockNamePaths());
    
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
      
      if ("groups".equals(mockNamePaths.get(0)) && "memberships".equals(mockNamePaths.get(2)) && 3 == mockNamePaths.size()) {
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
      
      if ("group_memberships".equals(mockNamePaths.get(0)) && 2 == mockNamePaths.size()) {
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
      
      if ("group_memberships".equals(mockNamePaths.get(0)) && 1 == mockNamePaths.size()) {
        associateGroupWithUser(mockServiceRequest, mockServiceResponse);
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
    }

    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
    
  }
  
  class BoxMockRsaKeyProvider implements RSAKeyProvider {
    
    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
      PublicKey publicKey = null;
      try {
        String publicKeyEncoded = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouperTest.box.mock.publicKey");
        if (StringUtils.isBlank(publicKeyEncoded)) {
          publicKeyEncoded = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAymeK52tp3E5wzN4IIpfAOFKVSX/uC2VSP22cJp2S1VTUx+NiieJWadYYrjQNMPQzaNUw+HNbbHylxk1LTgSOR70UXXp+nCIto6L0PdJpmCSun9KuyIT2KnI43niWioQsPzKTsEkFPraEotyub4FQAwAst5JXgCS0X0V1Bu8YRsxKo/QLOGFWxA8KulqdEC7EJxoqNv1NdBVQmLe8D9uc7bMYPG9Js3BlM9jyTDTN5UsCutWprg7UdmY0ZUSWI4nFrmgranzPtZrrz2LuVHaRbHPlFzGZEH/F43hWlLRNNUa1a7DV1KTc5vE9c3l5AxCtG5lKaTmWwUP1cHIDnCQTUwIDAQAB";
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
