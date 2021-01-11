package edu.internet2.middleware.grouper.app.scim2Provisioning;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.ddlutils.model.Database;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.ddl.DdlUtilsChangeDatabase;
import edu.internet2.middleware.grouper.ddl.DdlVersionBean;
import edu.internet2.middleware.grouper.ddl.GrouperDdlUtils;
import edu.internet2.middleware.grouper.ddl.GrouperTestDdl;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.internal.util.GrouperUuid;
import edu.internet2.middleware.grouper.j2ee.MockServiceHandler;
import edu.internet2.middleware.grouper.j2ee.MockServiceRequest;
import edu.internet2.middleware.grouper.j2ee.MockServiceResponse;
import edu.internet2.middleware.grouper.j2ee.MockServiceServlet;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

public class AwsScim2MockServiceHandler extends MockServiceHandler {

  public AwsScim2MockServiceHandler() {
  }

  /**
   * 
   */
  private static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");

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
  public static void ensureScimMockTables() {
    
    try {
      new GcDbAccess().sql("select count(*) from mock_scim_user").select(int.class);
      new GcDbAccess().sql("select count(*) from mock_scim_group").select(int.class);
    } catch (Exception e) {

      //we need to delete the test table if it is there, and create a new one
      //drop field id col, first drop foreign keys
      GrouperDdlUtils.changeDatabase(GrouperTestDdl.V1.getObjectName(), new DdlUtilsChangeDatabase() {
        public void changeDatabase(DdlVersionBean ddlVersionBean) {

          Database database = ddlVersionBean.getDatabase();
          GrouperScim2User.createTableScimUser(ddlVersionBean, database);
          GrouperScim2Group.createTableScimGroup(ddlVersionBean, database);
          
        }
      });
  
    }    
  }

  /**
   * 
   */
  public static void dropScimMockTables() {
    MockServiceServlet.dropMockTable("mock_scim_user");
  }
  
  private static boolean mockTablesThere = false;
  
  @Override
  public void handleRequest(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!mockTablesThere) {
      ensureScimMockTables();
    }
    mockTablesThere = true;
    
    if (GrouperUtil.length(mockServiceRequest.getPostMockNamePaths()) == 0) {
      throw new RuntimeException("Pass in a path!");
    }

    if (StringUtils.equals("GET", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("ServiceProviderConfig".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getServiceProviderConfig(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        getUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("Groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("Groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        getGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("DELETE", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        deleteUser(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("Groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        deleteGroup(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("PATCH", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 2 == mockServiceRequest.getPostMockNamePaths().length) {
        patchUser(mockServiceRequest, mockServiceResponse);
        return;
      }
    }
    if (StringUtils.equals("POST", mockServiceRequest.getHttpServletRequest().getMethod())) {
      if ("Users".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        postUsers(mockServiceRequest, mockServiceResponse);
        return;
      }
      if ("Groups".equals(mockServiceRequest.getPostMockNamePaths()[0]) && 1 == mockServiceRequest.getPostMockNamePaths().length) {
        postGroups(mockServiceRequest, mockServiceResponse);
        return;
      }
    }

    throw new RuntimeException("Not expecting request: '" + mockServiceRequest.getHttpServletRequest().getMethod() 
        + "', '" + mockServiceRequest.getPostMockNamePath() + "'");
  }

  public boolean checkAuthorization(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    String bearerToken = mockServiceRequest.getHttpServletRequest().getHeader("Authorization");
    if (!bearerToken.startsWith("Bearer ")) {
      throw new RuntimeException("Authorization token must start with 'Bearer '");
    }
    String authorizationToken = GrouperUtil.prefixOrSuffix(bearerToken, "Bearer ", false);

    Pattern clientIdPattern = Pattern.compile("^grouper\\.wsBearerToken\\.([^.]+)\\.accessTokenPassword$");
    String configId = null;
    for (String propertyName : GrouperLoaderConfig.retrieveConfig().propertyNames()) {
      
      Matcher matcher = clientIdPattern.matcher(propertyName);
      if (matcher.matches()) {
        if (StringUtils.equals(GrouperLoaderConfig.retrieveConfig().propertyValueString(propertyName), authorizationToken)) {
          configId = matcher.group(1);
          break;
        }
      }
    }
    
    if (StringUtils.isBlank(configId)) {
      mockServiceRequest.getDebugMap().put("authnError", "Cant find client id!  WS bearer token external system not configured or invalid secret!");
      mockServiceResponse.setResponseCode(401);
      return false;
    }

    // all good
    return true;
  }
  
  public void getServiceProviderConfig(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    //  {
    //    "schemas":[
    //       "urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig"
    //    ],
    //    "documentationUri":"https://docs.aws.amazon.com/singlesignon/latest/userguide/manage-your-identity-source-idp.html",
    //    "authenticationSchemes":[
    //       {
    //          "type":"oauthbearertoken",
    //          "name":"OAuth Bearer Token",
    //          "description":"Authentication scheme using the OAuth Bearer Token Standard",
    //          "specUri":"https://www.rfc-editor.org/info/rfc6750",
    //          "documentationUri":"https://docs.aws.amazon.com/singlesignon/latest/userguide/provision-automatically.html",
    //          "primary":true
    //       }
    //    ],
    //    "patch":{
    //       "supported":true
    //    },
    //    "bulk":{
    //       "supported":false,
    //       "maxOperations":1,
    //       "maxPayloadSize":1048576
    //    },
    //    "filter":{
    //       "supported":true,
    //       "maxResults":50
    //    },
    //    "changePassword":{
    //       "supported":false
    //    },
    //    "sort":{
    //       "supported":false
    //    },
    //    "etag":{
    //       "supported":false
    //    }
    // }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    {
      ArrayNode schemasNode = GrouperUtil.jsonJacksonArrayNode();
      schemasNode.add("urn:ietf:params:scim:schemas:core:2.0:ServiceProviderConfig");
      resultNode.set("schemas", schemasNode);
    }
    
    resultNode.put("documentationUri", "https://docs.aws.amazon.com/singlesignon/latest/userguide/manage-your-identity-source-idp.html");
    
    {
      ObjectNode authenticationSchemeNode = GrouperUtil.jsonJacksonNode();
      authenticationSchemeNode.put("type", "oauthbearertoken");
      authenticationSchemeNode.put("name", "OAuth Bearer Token");
      authenticationSchemeNode.put("description", "Authentication scheme using the OAuth Bearer Token Standard");
      authenticationSchemeNode.put("specUri", "https://www.rfc-editor.org/info/rfc6750");
      authenticationSchemeNode.put("documentationUri", "https://docs.aws.amazon.com/singlesignon/latest/userguide/provision-automatically.html");
      authenticationSchemeNode.put("primary", true);
      ArrayNode authenticationSchemesArray = GrouperUtil.jsonJacksonArrayNode();
      authenticationSchemesArray.add(authenticationSchemeNode);
      resultNode.set("authenticationSchemes", authenticationSchemesArray);
      
    }

    {
      ObjectNode patchNode = GrouperUtil.jsonJacksonNode();
      patchNode.put("supported", true);
      resultNode.set("patch", patchNode);
    }
    {
      ObjectNode bulkNode = GrouperUtil.jsonJacksonNode();
      bulkNode.put("supported", false);
      bulkNode.put("maxOperations", 1);
      bulkNode.put("maxPayloadSize", 1048576);
      resultNode.set("bulk", bulkNode);
    }
    {
      ObjectNode filterNode = GrouperUtil.jsonJacksonNode();
      filterNode.put("supported", true);
      filterNode.put("maxResults", 50);
      resultNode.set("filter", filterNode);
    }
    {
      ObjectNode changePasswordNode = GrouperUtil.jsonJacksonNode();
      changePasswordNode.put("supported", false);
      resultNode.set("changePassword", changePasswordNode);
    }
    {
      ObjectNode sortNode = GrouperUtil.jsonJacksonNode();
      sortNode.put("supported", false);
      resultNode.set("sort", sortNode);
    }
    {
      ObjectNode etagNode = GrouperUtil.jsonJacksonNode();
      etagNode.put("supported", false);
      resultNode.set("etag", etagNode);
    }
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  }

  public void postUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {

    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    //  {
    //    "active":true,
    //    "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User":{
    //       "employeeNumber":"12345",
    //       "costCenter":"costCent"
    //    },
    //    "id":"i",
    //    "displayName":"dispName",
    //    "emails":[
    //       {
    //          "value":"emailVal",
    //          "primary":true,
    //          "type":"emailTy"
    //       }
    //    ],
    //    "name":{
    //       "formatted":"formName",
    //       "familyName":"famName",
    //       "givenName":"givName",
    //       "middleName":"midName"
    //    },
    //    "externalId":"extId",
    //    "userName":"userNam",
    //    "userType":"userTyp"
    // }
    
    String userJsonString = mockServiceRequest.getRequestBody();
    JsonNode userJsonNode = GrouperUtil.jsonJacksonNode(userJsonString);
  
    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(userJsonNode, "id")) == 0, "id is forbidden");
  
    GrouperScim2User grouperScimUser = GrouperScim2User.fromJson(userJsonNode);
    grouperScimUser.setId(GrouperUuid.getUuid());
    
    HibernateSession.byObjectStatic().save(grouperScimUser);
    
    JsonNode resultNode = grouperScimUser.toJson(null);
  
    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  
    
  }

  public void getUsers(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String filter = mockServiceRequest.getHttpServletRequest().getParameter("filter");
    
    
    List<GrouperScim2User> grouperScimUsers = null;
    
    if (StringUtils.isBlank(filter)) {
      grouperScimUsers = HibernateSession.byHqlStatic().createQuery("from GrouperScim2User").list(GrouperScim2User.class);
    } else {
      //      $filter=" + GrouperUtil.escapeUrlEncode(fieldName)
      //          + "%20eq%20\"" + GrouperUtil.escapeUrlEncode(fieldValue)
      //displayName eq "something"
      Pattern fieldPattern = Pattern.compile("^([^\\s]+)\\s+eq\\s+\"(.+)\"$");
      Matcher matcher = fieldPattern.matcher(filter);
      GrouperUtil.assertion(matcher.matches(), "doesnt match regex '" + filter + "'");
      String field = matcher.group(1);
      String value = matcher.group(2);
      value = StringEscapeUtils.unescapeJson(value);
      GrouperUtil.assertion(field.matches("^[a-zA-Z0-9]+$"), "field must be alphanumeric '" + field + "'");
      grouperScimUsers = HibernateSession.byHqlStatic().createQuery("from GrouperScim2User where " + field + " = :theValue").setString("theValue", value).list(GrouperScim2User.class);
    }
    
    //  {
    //    "totalResults": 5,
    //    "Resources": [
    //      {
    //        "id": "11111111-2222-3333-4444-555555555555",
    //        "mail": "group1@contoso.com",
    //        "mailEnabled": true,
    //        "mailNickname": "ContosoGroup1",
    //        "securityEnabled": true
    //      }
    //    ]
    //  }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("totalResults", GrouperUtil.length(grouperScimUsers));
    resultNode.put("itemsPerPage", GrouperUtil.length(grouperScimUsers));
    resultNode.put("startIndex", 1);

    {
      ArrayNode schemasNode = GrouperUtil.jsonJacksonArrayNode();
      schemasNode.add("urn:ietf:params:scim:api:messages:2.0:ListResponse");
      resultNode.set("schemas", schemasNode);
    }
    
    ArrayNode resourcesNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperScim2User grouperScimUser : grouperScimUsers) {
      resourcesNode.add(grouperScimUser.toJson(null));
    }
    
    resultNode.set("Resources", resourcesNode);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));

  }

  public void getUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperScim2User grouperScimUser = HibernateSession.byHqlStatic()
        .createQuery("from GrouperScim2User where id = :theValue").setString("theValue", id)
        .uniqueResult(GrouperScim2User.class);

    if (grouperScimUser == null) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    ObjectNode objectNode = grouperScimUser.toJson(null);
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
  
  }

  public void deleteUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
  
    int usersDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperScim2User where id = :theId")
        .setString("theId", id).executeUpdateInt();
    mockServiceRequest.getDebugMap().put("usersDeleted", usersDeleted);
    
    // not sure why but they set this content type even though no json in response
    mockServiceResponse.setContentType("application/json");

    if (usersDeleted == 1) {
      mockServiceResponse.setResponseCode(204);
    } else if (usersDeleted == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("usersDeleted: " + usersDeleted);
    }
        
  }

  public void patchUser(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
  
    GrouperScim2User grouperScimUser = HibernateSession.byHqlStatic()
        .createQuery("from GrouperScim2User where id = :theValue").setString("theValue", id)
        .uniqueResult(GrouperScim2User.class);

    if (grouperScimUser == null) {
      mockServiceResponse.setResponseCode(404);
      mockServiceRequest.getDebugMap().put("foundUser", false);
      return;
    }
        
    mockServiceResponse.setContentType("application/json");
    
    //  {
    //    "schemas": [
    //        "urn:ietf:params:scim:api:messages:2.0:PatchOp"
    //    ],
    //    "Operations": [
    //        {
    //            "op": "replace",
    //            "path": "active",
    //            "value": "false"
    //        }
    //    ]
    //  }
    
    String requestBodyString = mockServiceRequest.getRequestBody();
    JsonNode requestNode = GrouperUtil.jsonJacksonNode(requestBodyString);

    ArrayNode schemasNode = (ArrayNode)requestNode.get("schemas");

    GrouperUtil.assertion(schemasNode.size() == 1, "schema is required");
    GrouperUtil.assertion("urn:ietf:params:scim:api:messages:2.0:PatchOp".equals(schemasNode.get(0).asText()), "schema is required");

    ArrayNode operationsNode = (ArrayNode)requestNode.get("Operations");

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
      
      //  {
      //    "active":true,
      //    "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User":{
      //       "employeeNumber":"12345",
      //       "costCenter":"costCent"   e.g. urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.costCenter
      //    },
      //    "id":"i",
      //    "displayName":"dispName",
      //    "emails":[
      //       {
      //          "value":"emailVal", emails.value eq "emailVal" or emails[value eq "emailVal"]
      //          "primary":true,
      //          "type":"emailTy"  emails.type eq "work" or emails[type eq "work"]
      //       }
      //    ],
      //    "name":{
      //       "formatted":"formName",    e.g. name.formatted
      //       "familyName":"famName",
      //       "givenName":"givName",
      //       "middleName":"midName"
      //    },
      //    "externalId":"extId",
      //    "userName":"userNam",
      //    "userType":"userTyp"
      // }
      
      GrouperUtil.assertion(!"id".equals(path), "cannot patch id");

      //  costCenter : String
      if ("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.costCenter".equals(path)) {
        path = "costCenter";
      }
      //  employeeNumber : String
      if ("urn:ietf:params:scim:schemas:extension:enterprise:2.0:User.employeeNumber".equals(path)) {
        path = "employeeNumber";
      }

      //  familyName : String
      if ("name.familyName".equals(path)) {
        path = "familyName";
      }
      //  formattedName : String
      if ("name.formattedName".equals(path)) {
        path = "formattedName";
      }
      //  givenName : String
      if ("name.givenName".equals(path)) {
        path = "givenName";
      }
      //  middleName : String
      if ("name.middleName".equals(path)) {
        path = "middleName";
      }
      
      if (path.startsWith("emails")) {
        // emailType : String
        // emailValue : String
        // emails[0]['value'] or emails.value eq "emailVal" or emails[value eq "emailVal"]
        
        JsonNode newEmailNode = operation.get("value");
        
        // validate the email
        if (opAdd) {
          
          // if theres an existing, thats bad
          if (!StringUtils.isBlank(grouperScimUser.getEmailValue()) || !StringUtils.isBlank(grouperScimUser.getEmailType())) {
            
            throw new RuntimeException("Adding email but already exists! " + grouperScimUser);
            
          }

          if (newEmailNode.has("type")) {
            grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "type"));
          }
          if (newEmailNode.has("value")) {
            grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "value"));
          }
          
        } else {
          grouperScimUser.validateEmail(path);

          if (StringUtils.isBlank(grouperScimUser.getEmailValue()) && StringUtils.isBlank(grouperScimUser.getEmailType())) {
            
            throw new RuntimeException(op + " email but not there! " + grouperScimUser);
            
          }

          if (opRemove) {
            
            grouperScimUser.setEmailType(null);
            grouperScimUser.setEmailValue(null);
            
          } else {
            
            //replace
            GrouperUtil.assertion(opReplace, "expecting replace");

            if (newEmailNode.isArray()) {
              GrouperUtil.assertion(newEmailNode.size() == 1, "expecting size 1 but was " + newEmailNode.size());
              newEmailNode = ((ArrayNode)newEmailNode).get(0);
            }
            if (newEmailNode.has("type")) {
              grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "type"));
            }
            if (newEmailNode.has("value")) {
              grouperScimUser.setEmailType(GrouperUtil.jsonJacksonGetString(newEmailNode, "value"));
            }
            
            
          }
          
        }
        
      } else {
        
        Object newValue = "active".equals(path) ? GrouperUtil.jsonJacksonGetBoolean(operation, "value") : GrouperUtil.jsonJacksonGetString(operation, "value");
        Object oldValue = GrouperUtil.fieldValue(grouperScimUser, path);
        
        // validate the email
        if (opAdd) {
          
          GrouperUtil.assertion(GrouperUtil.isBlank(oldValue), "add op already has value! " + path + ", '" + oldValue + "' " + grouperScimUser);
          
          GrouperUtil.assignField(grouperScimUser, path, newValue);
          
        } else {

          GrouperUtil.assertion(!GrouperUtil.isBlank(oldValue), "add op doesnt have value! " + path + ", '" + oldValue + "' " + grouperScimUser);

          if (opRemove) {
            
            GrouperUtil.assertion(newValue == null, "remove op should not have a value! " + path + ", '" + newValue + "' " + grouperScimUser);
          }

          GrouperUtil.assignField(grouperScimUser, path, newValue);
        }
        
      }
      
    }
    HibernateSession.byObjectStatic().saveOrUpdate(grouperScimUser);
    
    ObjectNode objectNode = grouperScimUser.toJson(null);
    mockServiceResponse.setResponseCode(204);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
    
    
  }

  public void postGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    //  {
    //    "meta": {
    //        "resourceType": "Group",
    //        "created": "2020-04-06T16:48:19Z",
    //        "lastModified": "2020-04-06T16:48:19Z"
    //    },
    //    "schemas": [
    //        "urn:ietf:params:scim:schemas:core:2.0:Group"
    //    ],
    //    "displayName": "Group Bar"
    //  }
    
    String groupJsonString = mockServiceRequest.getRequestBody();
    JsonNode groupJsonNode = GrouperUtil.jsonJacksonNode(groupJsonString);

    //check require args
    GrouperUtil.assertion(GrouperUtil.length(GrouperUtil.jsonJacksonGetString(groupJsonNode, "id")) == 0, "id is forbidden");

    GrouperScim2Group grouperScimGroup = GrouperScim2Group.fromJson(groupJsonNode);
    grouperScimGroup.setId(GrouperUuid.getUuid());
    grouperScimGroup.setCreated(new Timestamp(System.currentTimeMillis()));
    grouperScimGroup.setLastModified(new Timestamp(System.currentTimeMillis()));

    HibernateSession.byObjectStatic().save(grouperScimGroup);

    JsonNode resultNode = grouperScimGroup.toJson(null);

    mockServiceResponse.setResponseCode(201);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
    
  }

  public void deleteGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperUtil.assertion(GrouperUtil.length(id) > 0, "id is required");
  
    int groupsDeleted = HibernateSession.byHqlStatic()
        .createQuery("delete from GrouperScim2Group where id = :theId")
        .setString("theId", id).executeUpdateInt();
    mockServiceRequest.getDebugMap().put("groupsDeleted", groupsDeleted);
    
    // not sure why but they set this content type even though no json in response
    mockServiceResponse.setContentType("application/json");

    if (groupsDeleted == 1) {
      mockServiceResponse.setResponseCode(204);
    } else if (groupsDeleted == 0) {
      mockServiceResponse.setResponseCode(404);
    } else {
      throw new RuntimeException("groupsDeleted: " + groupsDeleted);
    }
        
  }

  public void getGroup(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String id = mockServiceRequest.getPostMockNamePaths()[1];
    
    GrouperScim2Group grouperScimGroup = HibernateSession.byHqlStatic()
        .createQuery("from GrouperScim2Group where id = :theValue").setString("theValue", id)
        .uniqueResult(GrouperScim2Group.class);
  
    if (grouperScimGroup == null) {
      mockServiceResponse.setResponseCode(404);
      return;
    }
    ObjectNode objectNode = grouperScimGroup.toJson(null);
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(objectNode));
  
  }

  public void getGroups(MockServiceRequest mockServiceRequest, MockServiceResponse mockServiceResponse) {
  
    if (!checkAuthorization(mockServiceRequest, mockServiceResponse)) {
      return;
    }
    
    String filter = mockServiceRequest.getHttpServletRequest().getParameter("filter");
    
    List<GrouperScim2Group> grouperScimGroups = null;
    
    if (StringUtils.isBlank(filter)) {
      grouperScimGroups = HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group").list(GrouperScim2Group.class);
    } else {
      //      $filter=" + GrouperUtil.escapeUrlEncode(fieldName)
      //          + "%20eq%20\"" + GrouperUtil.escapeUrlEncode(fieldValue)
      //displayName eq "something"
      Pattern fieldPattern = Pattern.compile("^([^\\s]+)\\s+eq\\s+\"(.+)\"$");
      Matcher matcher = fieldPattern.matcher(filter);
      GrouperUtil.assertion(matcher.matches(), "doesnt match regex '" + filter + "'");
      String field = matcher.group(1);
      String value = matcher.group(2);
      value = StringEscapeUtils.unescapeJson(value);
      GrouperUtil.assertion(field.matches("^[a-zA-Z0-9]+$"), "field must be alphanumeric '" + field + "'");
      grouperScimGroups = HibernateSession.byHqlStatic().createQuery("from GrouperScim2Group where " + field + " = :theValue").setString("theValue", value).list(GrouperScim2Group.class);
    }
    
    //  {
    //    "totalResults": 5,
    //    "Resources": [
    //      {
    //        "id": "11111111-2222-3333-4444-555555555555",
    //        "displayName": "my group"
    //      }
    //    ]
    //  }
    
    ObjectNode resultNode = GrouperUtil.jsonJacksonNode();
    
    resultNode.put("totalResults", GrouperUtil.length(grouperScimGroups));
    resultNode.put("itemsPerPage", GrouperUtil.length(grouperScimGroups));
    resultNode.put("startIndex", 1);
  
    {
      ArrayNode schemasNode = GrouperUtil.jsonJacksonArrayNode();
      schemasNode.add("urn:ietf:params:scim:api:messages:2.0:ListResponse");
      resultNode.set("schemas", schemasNode);
    }
    
    ArrayNode resourcesNode = GrouperUtil.jsonJacksonArrayNode();
    
    for (GrouperScim2Group grouperScimGroup : grouperScimGroups) {
      resourcesNode.add(grouperScimGroup.toJson(null));
    }
    
    resultNode.set("Resources", resourcesNode);
    
    mockServiceResponse.setResponseCode(200);
    mockServiceResponse.setContentType("application/json");
    mockServiceResponse.setResponseBody(GrouperUtil.jsonJacksonToString(resultNode));
  
  }
  
}
