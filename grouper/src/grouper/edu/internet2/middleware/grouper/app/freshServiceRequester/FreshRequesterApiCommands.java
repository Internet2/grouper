package edu.internet2.middleware.grouper.app.freshServiceRequester;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class FreshRequesterApiCommands {
  
  
  private static final int MAX_PAGE_SIZE = 100;
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");
  
  public static GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
  
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    
    try {
      String configId = "freshServiceDev";
//    String configId = "freshserviceTest";
    
//    FreshRequesterGroup group = new FreshRequesterGroup();
//    group.setName("Test Grouper Update");
//    group.setDescription("Testing Grouper mock service Update method");
//    group.setId(44209747L);
//    updateRequesterGroup(configId, group);
//    createRequesterGroup(configId, group);
//    
//    List<FreshRequesterGroup> groups = retrieveRequesterGroups(configId);
//    System.out.println(groups.size());

//    List<FreshRequesterUser> users = retrieveRequesterUsers(configId);
//    System.out.println(users.size());
    
//    FreshRequesterUser user = new FreshRequesterUser();
//    user.setFirstName("A");
//    user.setLastName("TestUser");
//    user.setEmail("a.testuser@test.edu");
//    createRequesterUser(configId, user);
    
//    deleteRequesterGroup(configId, 8070026L);

      List<FreshRequesterGroup> groups = retrieveRequesterGroups(configId);
      for (FreshRequesterGroup group : GrouperUtil.nonNull(groups)) {
        System.out.println("Group: " + group.toString());
      }
    
      System.out.println("done");

    } catch (Exception e) {
      System.out.println("Error: " + GrouperClientUtils.getFullStackTrace(e));
    }
    System.exit(0);
  }
  
  private static JsonNode executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes,
      int[] returnCode, String bodyParam, Integer page, boolean addPageSize, String queryParam) {
    
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
    grouperHttpClient.assignDoNotLogHeaders(doNotLogHeaders).assignDoNotLogParameters(doNotLogParameters);
    
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(grouperHttpClient, configId, grouperLoaderConfig, debugMap);
    
    String url = grouperLoaderConfig.propertyValueStringRequired("grouper.wsBearerToken." + configId + ".endpoint");
    
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    // in a nextLink, url is specified, so it might not have a prefix of the resourceEndpoint
    if(!urlSuffix.startsWith("http")) {
      url += (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    } else {
      url = urlSuffix;
    }
    debugMap.put("url", url);
    
    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(httpMethodName);
    
    if (StringUtils.isNotBlank(bodyParam)) {
      grouperHttpClient.assignBody(bodyParam);
    }
    
    if (page != null && page > 0) {
      grouperHttpClient.addUrlParameter("page", Integer.toString(page));
    }
    
    if (addPageSize) {
      // default page size to max which is 100
      int pageSize = grouperLoaderConfig.propertyValueInt("grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE);
      grouperHttpClient.addUrlParameter("per_page", Integer.toString(pageSize));
    }
    
    if (StringUtils.isNotBlank(queryParam)) {
      grouperHttpClient.addUrlParameter("query", queryParam);
    }
    
    if (httpMethodName.equals("POST") || httpMethodName.equals("PUT")) {
      grouperHttpClient.addHeader("Content-Type", "application/json; charset=utf-8");
    }
    
    long httpCallStartMillis = System.currentTimeMillis();
    try {
      grouperHttpClient.executeRequest();
    } finally {
      GrouperProvisioner.incrementCommandsCallsStats(debugLabel, 1,
          System.currentTimeMillis() - httpCallStartMillis);
    }
    
    int code = -1;
    String json = null;
    
    try {
      code = grouperHttpClient.getResponseCode();
      returnCode[0] = code;
      json = grouperHttpClient.getResponseBody();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }
    
    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException(
          "Invalid return code '" + code + "', expecting: " + GrouperUtil.setToString(allowedReturnCodes)
              + ". '" + debugMap.get("url") + "' " + json);
    }

    if (StringUtils.isBlank(json)) {
      return null;
    }

    try {
      JsonNode rootNode = GrouperUtil.jsonJacksonNode(json);
      return rootNode;
    } catch (Exception e) {
      throw new RuntimeException("Error parsing response: '" + json + "'", e);
    }
  }
  
  // Group methods
  
  /**
   * Create a requester group in Freshservice
   * @param configId the id of the external system
   * @param grouperRequesterGroup the requester group to be created in Freshservice
   */
  public static FreshRequesterGroup createRequesterGroup(String configId, FreshRequesterGroup grouperRequesterGroup) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createRequesterGroup");

    long startTime = System.nanoTime();

    try {
      JsonNode jsonToSend = grouperRequesterGroup.toJson(null);

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "createRequesterGroup", "POST", configId, "api/v2/requester_groups",
          GrouperUtil.toSet(200, 201, 409), returnCode, jsonStringToSend, null, false, null);

      if (returnCode[0] == 409) {
        throw new RuntimeException("Requester group already exists: " + grouperRequesterGroup.getName());
      }

      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "requester_group");
      FreshRequesterGroup createdGroup = FreshRequesterGroup.fromJson(groupNode);

      return createdGroup;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
  }
  
  /**
   * Update a Freshservice requester group
   * @param configId the id of the external system
   * @param grouperRequesterGroup the group to be updated in Freshservice
   */
  public static FreshRequesterGroup updateRequesterGroup(String configId, FreshRequesterGroup grouperRequesterGroup, Map<String, ProvisioningObjectChangeAction> fieldsToUpdate) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateRequesterGroup");

    long startTime = System.nanoTime();

    try {

      if (grouperRequesterGroup == null) {
        throw new RuntimeException("grouperRequesterGroup is null");
      }

      Long groupId = grouperRequesterGroup.getId();
      if (groupId == null || groupId == 0L) {
        throw new RuntimeException("groupId is null or 0 (unset)");
      }

      FreshRequesterGroup requesterGroupCurrentState = retrieveRequesterGroup(configId, groupId);
      if (requesterGroupCurrentState == null) {
        throw new RuntimeException("Cannot update requester group that does not exist in target. id=" + groupId);
      }

      ObjectNode jsonToSend = requesterGroupCurrentState.toJson(null);

      // overlay only updated fields
      if (fieldsToUpdate != null) {
        for (Map.Entry<String, ProvisioningObjectChangeAction> entry : fieldsToUpdate.entrySet()) {
          String fieldName = entry.getKey();
          ProvisioningObjectChangeAction action = entry.getValue();
          if (action == null) {
            continue;
          }
          if (StringUtils.isBlank(fieldName)) {
            continue;
          }

          // For delete, explicitly null out the field in JSON
          boolean isDelete = action == ProvisioningObjectChangeAction.delete;

          if ("name".equals(fieldName)) {
            if (isDelete || grouperRequesterGroup.getName() == null) {
              jsonToSend.putNull("name");
            } else {
              jsonToSend.put("name", grouperRequesterGroup.getName());
            }
          } else if ("description".equals(fieldName)) {
            if (isDelete || grouperRequesterGroup.getDescription() == null) {
              jsonToSend.putNull("description");
            } else {
              jsonToSend.put("description", grouperRequesterGroup.getDescription());
            }
          }
        }
      }

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "updateRequesterGroup", "PUT", configId, "api/v2/requester_groups/" + String.valueOf(groupId),
          GrouperUtil.toSet(200, 201), new int[] { -1 }, jsonStringToSend, null, false, null);

      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "requester_group");
      FreshRequesterGroup updatedGroup = FreshRequesterGroup.fromJson(groupNode);
      return updatedGroup;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
  }
  
  /**
   * Update a Freshservice requester user.
   *
   * This method performs a three-step update:
   * 1. GET the current requester JSON from Freshservice by id
   * 2. Strip read-only attributes that cannot be sent on a PUT
   *    (id, created_at, has_logged_in, is_agent, updated_at, work_schedule_id,
   *    department_names, location_name)
   * 3. Overlay the fields indicated in fieldsToUpdate with values from the
   *    supplied FreshRequesterUser, then PUT the result
   *
   * The fieldsToUpdate set uses Java-style field names which are translated
   * to their Freshservice JSON attribute names (e.g. "firstName" becomes "first_name",
   * "email" becomes "primary_email", "departmentId" becomes "department_ids" array).
   *
   * Custom fields use the prefix "customField_" followed by the Freshservice
   * custom field name (e.g. "customField_pennkey").
   *
   * @param configId the id of the external system
   * @param grouperRequesterUser the requester user containing the new values.
   *   Must have id set to identify which user to update.
   * @param fieldsToUpdate set of Java field names to update. Supported values:
   *   "firstName", "lastName", "email", "jobTitle", "workPhoneNumber",
   *   "departmentId", "reportingManagerId", "address", "active",
   *   and custom fields with prefix "customField_" (e.g. "customField_pennkey")
   * @return the updated requester user parsed from the PUT response
   */
  public static FreshRequesterUser updateRequesterUser(String configId, FreshRequesterUser grouperRequesterUser, Set<String> fieldsToUpdate) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateRequesterUser");

    long startTime = System.nanoTime();

    try {

      // validate input
      if (grouperRequesterUser == null) {
        throw new RuntimeException("grouperRequesterUser is null");
      }

      Long userId = grouperRequesterUser.getId();
      if (userId == null || userId == 0L) {
        throw new RuntimeException("userId is null or 0 (unset)");
      }

      // Step 1: GET the current state of the requester from Freshservice
      // GET /api/v2/requesters/{id} returns { "requester": { ... } }
      int[] getReturnCode = new int[] { -1 };
      String getUrlSuffix = "api/v2/requesters/" + String.valueOf(userId);
      JsonNode getJsonNode = executeMethod(debugMap, "updateRequesterUser", "GET", configId, getUrlSuffix,
          GrouperUtil.toSet(200, 404), getReturnCode, null, null, false, null);

      // if the user does not exist, we cannot update
      if (getReturnCode[0] == 404 || getJsonNode == null) {
        throw new RuntimeException("Cannot update requester user that does not exist in target. id=" + userId);
      }

      // extract the "requester" object from the response
      JsonNode requesterNode = getJsonNode.get("requester");
      if (requesterNode == null) {
        throw new RuntimeException("Cannot update requester user that does not exist in target. id=" + userId);
      }

      // Step 2: deep copy to a mutable ObjectNode and strip read-only attributes
      // that the Freshservice PUT endpoint does not accept
      ObjectNode jsonToSend = requesterNode.deepCopy();
      jsonToSend.remove("id");
      jsonToSend.remove("created_at");
      jsonToSend.remove("has_logged_in");
      jsonToSend.remove("is_agent");
      jsonToSend.remove("updated_at");
      jsonToSend.remove("work_schedule_id");
      jsonToSend.remove("department_names");
      jsonToSend.remove("location_name");

      // Step 3: overlay only the fields indicated in fieldsToUpdate.
      // Each field name is a Java-style name that maps to a Freshservice JSON attribute.
      // If the value on the FreshRequesterUser is non-null, set it; otherwise send null.
      if (fieldsToUpdate != null) {
        for (String fieldName : fieldsToUpdate) {
          if (StringUtils.isBlank(fieldName)) {
            continue;
          }

          // read-only attributes that Freshservice does not allow on PUT.
          // If a provisioner tries to update these, the CRUD update setting
          // for this attribute should be set to false in the provisioner configuration.
          if ("id".equals(fieldName) || "isAgent".equals(fieldName)
              || "createdAt".equals(fieldName) || "hasLoggedIn".equals(fieldName)
              || "updatedAt".equals(fieldName) || "workScheduleId".equals(fieldName)
              || "departmentNames".equals(fieldName) || "locationName".equals(fieldName)) {
            throw new RuntimeException("Cannot update read-only attribute '" + fieldName
                + "'. Set CRUD update to false for this attribute in the provisioner configuration.");
          }

          // "firstName" -> JSON "first_name"
          if ("firstName".equals(fieldName)) {
            if (grouperRequesterUser.getFirstName() != null) {
              jsonToSend.put("first_name", grouperRequesterUser.getFirstName());
            } else {
              jsonToSend.putNull("first_name");
            }

          // "lastName" -> JSON "last_name"
          } else if ("lastName".equals(fieldName)) {
            if (grouperRequesterUser.getLastName() != null) {
              jsonToSend.put("last_name", grouperRequesterUser.getLastName());
            } else {
              jsonToSend.putNull("last_name");
            }

          // "email" -> JSON "primary_email"
          } else if ("email".equals(fieldName)) {
            if (grouperRequesterUser.getEmail() != null) {
              jsonToSend.put("primary_email", grouperRequesterUser.getEmail());
            } else {
              jsonToSend.putNull("primary_email");
            }

          // "jobTitle" -> JSON "job_title"
          } else if ("jobTitle".equals(fieldName)) {
            if (grouperRequesterUser.getJobTitle() != null) {
              jsonToSend.put("job_title", grouperRequesterUser.getJobTitle());
            } else {
              jsonToSend.putNull("job_title");
            }

          // "workPhoneNumber" -> JSON "work_phone_number"
          } else if ("workPhoneNumber".equals(fieldName)) {
            if (grouperRequesterUser.getWorkPhoneNumber() != null) {
              jsonToSend.put("work_phone_number", grouperRequesterUser.getWorkPhoneNumber());
            } else {
              jsonToSend.putNull("work_phone_number");
            }

          // "departmentId" -> JSON "department_ids" (array with single element)
          } else if ("departmentId".equals(fieldName)) {
            if (grouperRequesterUser.getDepartmentId() != null) {
              // Freshservice expects department_ids as an array
              ArrayNode departmentIdsArray = GrouperUtil.jsonJacksonArrayNode();
              departmentIdsArray.add(grouperRequesterUser.getDepartmentId().longValue());
              jsonToSend.set("department_ids", departmentIdsArray);
            } else {
              jsonToSend.putNull("department_ids");
            }

          // "reportingManagerId" -> JSON "reporting_manager_id"
          } else if ("reportingManagerId".equals(fieldName)) {
            if (grouperRequesterUser.getReportingManagerId() != null) {
              jsonToSend.put("reporting_manager_id", grouperRequesterUser.getReportingManagerId().longValue());
            } else {
              jsonToSend.putNull("reporting_manager_id");
            }

          // "address" -> JSON "address"
          } else if ("address".equals(fieldName)) {
            if (grouperRequesterUser.getAddress() != null) {
              jsonToSend.put("address", grouperRequesterUser.getAddress());
            } else {
              jsonToSend.putNull("address");
            }

          // "externalId" -> JSON "external_id"
          } else if ("externalId".equals(fieldName)) {
            if (grouperRequesterUser.getExternalId() != null) {
              jsonToSend.put("external_id", grouperRequesterUser.getExternalId());
            } else {
              jsonToSend.putNull("external_id");
            }

          // "active" -> JSON "active" (boolean)
          } else if ("active".equals(fieldName)) {
            if (grouperRequesterUser.getActive() != null) {
              jsonToSend.put("active", grouperRequesterUser.getActive().booleanValue());
            } else {
              jsonToSend.putNull("active");
            }

          // "customField_<name>" -> nested inside JSON "custom_fields" object
          // e.g. "customField_pennkey" sets custom_fields.pennkey
          } else if (fieldName.startsWith(FreshRequesterUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX)) {

            // strip the prefix to get the actual Freshservice custom field name
            String customFieldName = fieldName.substring(FreshRequesterUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX.length());
            if (!StringUtils.isBlank(customFieldName)) {

              // get or create the custom_fields JSON object
              ObjectNode customFieldsNode = (ObjectNode) GrouperUtil.jsonJacksonGetNode(jsonToSend, "custom_fields");
              if (customFieldsNode == null) {
                customFieldsNode = GrouperUtil.jsonJacksonNode();
                jsonToSend.set("custom_fields", customFieldsNode);
              }

              // get the custom field value from the FreshRequesterUser
              Object customValue = grouperRequesterUser.getCustomFields() == null ? null
                  : grouperRequesterUser.getCustomFields().get(customFieldName);

              // set the value in the JSON according to its type
              if (customValue == null) {
                customFieldsNode.putNull(customFieldName);
              } else if (customValue instanceof String) {
                customFieldsNode.put(customFieldName, (String) customValue);
              } else if (customValue instanceof Boolean) {
                customFieldsNode.put(customFieldName, ((Boolean) customValue).booleanValue());
              } else if (customValue instanceof Number) {
                customFieldsNode.put(customFieldName, ((Number) customValue).longValue());
              } else {
                throw new RuntimeException("Unsupported custom field type for " + customFieldName + ": "
                    + customValue.getClass().getName());
              }
            }

          // unrecognized field name
          } else {
            throw new RuntimeException("Unrecognized field name in fieldsToUpdate: '" + fieldName + "'");
          }
        }
      }

      // serialize the JSON and send the PUT request
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      // PUT /api/v2/requesters/{id} returns { "requester": { ... } }
      JsonNode responseNode = executeMethod(debugMap, "updateRequesterUser", "PUT", configId, "api/v2/requesters/" + String.valueOf(userId),
          GrouperUtil.toSet(200, 201), new int[] { -1 }, jsonStringToSend, null, false, null);

      // parse the updated requester from the response
      JsonNode updatedUserNode = GrouperUtil.jsonJacksonGetNode(responseNode, "requester");
      FreshRequesterUser updatedUser = FreshRequesterUser.fromJson(updatedUserNode);
      return updatedUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
  }
  
  /**
   * Delete a requester group
   * @param configId the id of the external system
   * @param groupId the id of the group to be deleted
   */
  public static void deleteRequesterGroup(String configId, Long groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteRequesterGroup");

    long startTime = System.nanoTime();
    
    try {
      
      if (groupId == null) {
        throw new RuntimeException("groupId is null");
      }
      String id = String.valueOf(groupId);
      
      executeMethod(debugMap, "deleteRequesterGroup", "DELETE", configId, "api/v2/requester_groups/" + id,
          GrouperUtil.toSet(200, 204, 404), new int[] { -1 }, null, null, false, null);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
    
  }
  
  /**
   * Get a Freshservice requester group
   * @param configId the id of the external system
   * @param id the requester group id
   * @return the GrouperRequesterGroup matching the Freshservice group retrieved
   */
  public static FreshRequesterGroup retrieveRequesterGroup(String configId, Long id) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveRequesterGroup");
    
    long startTime = System.nanoTime();
    
    try {
      String urlSuffix = "api/v2/requester_groups/" + String.valueOf(id);
      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "retrieveRequesterGroup", "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), returnCode, null, null, false, null);
      if (returnCode[0] == 404) {
        return null;
      }
      
      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "requester_group");
      if (groupNode == null) {
        return null;
      }
      // skip rule_based groups since the API cannot manage them
      String groupType = GrouperUtil.jsonJacksonGetString(groupNode, "type");
      if (Strings.CS.equals("rule_based", groupType)) {
        return null;
      }
      FreshRequesterGroup grouperRequesterGroup = FreshRequesterGroup.fromJson(groupNode);

      return grouperRequesterGroup;
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
  }
  
  /**
   * Get a list of all Freshservice requester groups
   * @param configId the id of the external system
   * @return
   */
  public static List<FreshRequesterGroup> retrieveRequesterGroups(String configId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveRequesterGroups");
    
    List<FreshRequesterGroup> results = new ArrayList<FreshRequesterGroup>();
    
    long startTime = System.nanoTime();
    
    try {
      
      boolean lastPage = false;
      int page = 1;
      
      while (!lastPage) {
        
        JsonNode jsonNode = executeMethod(debugMap, "retrieveRequesterGroups", "GET", configId, "api/v2/requester_groups",
            GrouperUtil.toSet(200), new int[] { -1 }, null, page, true, null);
        
        ArrayNode groupsArray = (ArrayNode) jsonNode.get("requester_groups");
        
        for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
          JsonNode groupNode = groupsArray.get(i);
          // skip rule_based groups since the API cannot manage them
          String groupType = GrouperUtil.jsonJacksonGetString(groupNode, "type");
          if (Strings.CS.equals("rule_based", groupType)) {
            continue;
          }
          FreshRequesterGroup grouperRequesterGroup = FreshRequesterGroup.fromJson(groupNode);
          results.add(grouperRequesterGroup);
        }

        page++;
        
        if (groupsArray.size() < grouperLoaderConfig.propertyValueInt("grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE)) {
          lastPage = true;
        }
        
      }
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
    
    return results;
  }
  
  /**
   * Create a requester user in Freshservice, or update an existing one if a user
   * with the same email address already exists.
   *
   * This method first looks up the user by email address:
   * - If a user already exists and is inactive, it will be reactivated and updated
   *   with all the fields from grouperRequesterUser.
   * - If a user already exists and is active, it will be updated with all the
   *   fields from grouperRequesterUser.
   * - If no user exists with that email, a new user is created via the helper method.
   *
   * @param configId the id of the external system
   * @param grouperRequesterUser the user to be created or updated in Freshservice
   * @return the created or updated requester user
   */
  public static FreshRequesterUser createRequesterUser(String configId, FreshRequesterUser grouperRequesterUser) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createRequesterUser");

    long startTime = System.nanoTime();

    try {

      // look up existing user by email address
      FreshRequesterUser existingUser = null;
      if (!StringUtils.isBlank(grouperRequesterUser.getEmail())) {
        existingUser = retrieveRequesterUserByEmail(configId, grouperRequesterUser.getEmail(), true);
      }

      if (existingUser != null) {

        // if the existing user is not active, reactivate it first
        if (existingUser.getActive() == null || !existingUser.getActive()) {
          reactivateRequesterUser(configId, existingUser.getId());
        }

        // user already exists - build a set of all fields to update
        Set<String> fieldsToUpdate = new java.util.LinkedHashSet<String>();
        fieldsToUpdate.add("firstName");
        fieldsToUpdate.add("lastName");
        fieldsToUpdate.add("email");
        fieldsToUpdate.add("jobTitle");
        fieldsToUpdate.add("workPhoneNumber");
        fieldsToUpdate.add("departmentId");
        fieldsToUpdate.add("reportingManagerId");
        fieldsToUpdate.add("address");

        // add any custom fields from the grouperRequesterUser
        if (grouperRequesterUser.getCustomFields() != null) {
          for (String customFieldName : grouperRequesterUser.getCustomFields().keySet()) {
            if (!StringUtils.isBlank(customFieldName)) {
              fieldsToUpdate.add(FreshRequesterUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX + customFieldName);
            }
          }
        }

        // set the id from the existing user so updateRequesterUser can find it
        grouperRequesterUser.setId(existingUser.getId());

        // update the existing user with all fields
        return updateRequesterUser(configId, grouperRequesterUser, fieldsToUpdate);
      }

      // no existing user found - create a new one
      return createRequesterUserHelper(configId, grouperRequesterUser);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }

  }

  /**
   * Helper method to create a new requester user in Freshservice via POST.
   * This method directly calls the Freshservice POST /api/v2/requesters endpoint.
   * Callers should typically use createRequesterUser() which handles the
   * lookup-by-email and update-if-exists logic.
   *
   * @param configId the id of the external system
   * @param grouperRequesterUser the user to be created in Freshservice
   * @return the created requester user with assigned id
   */
  public static FreshRequesterUser createRequesterUserHelper(String configId, FreshRequesterUser grouperRequesterUser) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createRequesterUserHelper");

    long startTime = System.nanoTime();

    try {
      JsonNode jsonToSend = grouperRequesterUser.toJson(null);

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "createRequesterUserHelper", "POST", configId, "api/v2/requesters",
          GrouperUtil.toSet(200, 201, 409), returnCode, jsonStringToSend, null, false, null);

      if (returnCode[0] == 409) {
        throw new RuntimeException("Requester user already exists: " + grouperRequesterUser.getEmail());
      }

      JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "requester");
      FreshRequesterUser createdUser = FreshRequesterUser.fromJson(userNode);
      return createdUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }

  }
  
  /**
   * Retrieve all requester users from Freshservice
   * @param configId the id of the external system
   * @param includeInactiveRequesters if true, include inactive (deactivated) requesters
   *   in the results. If false, only active requesters are returned.
   * @return a list of Freshservice requester users
   */
  public static List<FreshRequesterUser> retrieveRequesterUsers(String configId, boolean includeInactiveRequesters) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<FreshRequesterUser> results = new ArrayList<FreshRequesterUser>();

    debugMap.put("method", "retrieveRequesterUsers");

    long startTime = System.nanoTime();

    try {

      boolean lastPage = false;
      int page = 1;

      while (!lastPage) {

        JsonNode jsonNode = executeMethod(debugMap, "retrieveRequesterUsers", "GET", configId, "api/v2/requesters",
            GrouperUtil.toSet(200), new int[] { -1 }, null, page, true, null);

        ArrayNode requesterUsersArray = (ArrayNode) jsonNode.get("requesters");

        for (int i = 0; i < (requesterUsersArray == null ? 0 : requesterUsersArray.size()); i++) {
          JsonNode userNode = requesterUsersArray.get(i);
          FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromJson(userNode);
          // skip agents since the API should not manage them
          if (grouperRequesterUser.getIsAgent() != null && grouperRequesterUser.getIsAgent()) {
            continue;
          }
          // skip inactive requesters unless caller explicitly wants them
          if (!includeInactiveRequesters
              && (grouperRequesterUser.getActive() == null || !grouperRequesterUser.getActive())) {
            continue;
          }
          results.add(grouperRequesterUser);
        }

        page++;

        if (requesterUsersArray.size() < grouperLoaderConfig.propertyValueInt("grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE)) {
          lastPage = true;
        }
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Get a Freshservice requester user by id
   * @param configId the id of the external system
   * @param id the id of the requester user to be retrieved
   * @param includeInactiveRequesters if true, return the user even if inactive.
   *   If false, return null for inactive users.
   * @return the requester user, or null if not found (or inactive when not included)
   */
  public static FreshRequesterUser retrieveRequesterUserById(String configId, Long id, boolean includeInactiveRequesters) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRequesterUserById");

    long startTime = System.nanoTime();

    try {
      int[] returnCode = new int[] { -1 };

      String urlSuffix = "api/v2/requesters/" + String.valueOf(id);
      JsonNode jsonNode = executeMethod(debugMap, "retrieveRequesterUserById", "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), returnCode, null, null, false, null);

      if (returnCode[0] == 404) {
        return null;
      }

      JsonNode userNode = jsonNode.get("requester");
      if (userNode == null) {
        return null;
      }

      FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromJson(userNode);

      // skip agents since the API should not manage them
      if (grouperRequesterUser.getIsAgent() != null && grouperRequesterUser.getIsAgent()) {
        return null;
      }

      // skip inactive requesters unless caller explicitly wants them
      if (!includeInactiveRequesters
          && (grouperRequesterUser.getActive() == null || !grouperRequesterUser.getActive())) {
        return null;
      }

      return grouperRequesterUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }

  }

  /**
   * Get a Freshservice requester user by email address.
   * Uses the Freshservice email parameter:
   *   GET /api/v2/requesters?email=jsmith@upenn.edu
   *
   * @param configId the id of the external system
   * @param email the email address of the requester user to be retrieved
   * @param includeInactiveRequesters if true, return the user even if inactive.
   *   If false, return null for inactive users.
   * @return the requester user, or null if not found (or inactive when not included)
   */
  public static FreshRequesterUser retrieveRequesterUserByEmail(String configId, String email, boolean includeInactiveRequesters) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRequesterUserByEmail");

    long startTime = System.nanoTime();

    try {
      int[] returnCode = new int[] { -1 };

      // use the email= URL parameter instead of query=
      String urlSuffix = "api/v2/requesters?email=" + GrouperUtil.escapeUrlEncode(email);
      JsonNode jsonNode = executeMethod(debugMap, "retrieveRequesterUserByEmail", "GET", configId, urlSuffix,
          GrouperUtil.toSet(200), returnCode, null, null, false, null);

      if (jsonNode == null) {
        return null;
      }

      ArrayNode requesterUserArray = (ArrayNode) jsonNode.get("requesters");

      if (requesterUserArray.size()==1) {
        JsonNode userNode = requesterUserArray.get(0);
        FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromJson(userNode);
        // skip agents since the API should not manage them
        if (grouperRequesterUser.getIsAgent() != null && grouperRequesterUser.getIsAgent()) {
          return null;
        }
        // skip inactive requesters unless caller explicitly wants them
        if (!includeInactiveRequesters
            && (grouperRequesterUser.getActive() == null || !grouperRequesterUser.getActive())) {
          return null;
        }
        return grouperRequesterUser;
      }

      return null;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }

  }

  /**
   * Retrieve a requester user by a provisioning attribute name and value.
   *
   * If attributeName is "id" or "email", delegates to the existing lookup methods.
   * If attributeName is "externalId", searches by external_id.
   * If attributeName starts with "customField_", searches by that custom field name.
   * Otherwise throws an exception.
   *
   * The Freshservice API query format is:
   *   GET /api/v2/requesters?query=attributeName:'value'   (for strings)
   *   GET /api/v2/requesters?query=attributeName:value      (for numbers)
   *
   * @param configId the id of the external system
   * @param attributeName the provisioning attribute name (e.g. "id", "email", "externalId", "customField_pennkey")
   * @param attributeValue the value to search for. Must be String, Long, or Integer.
   *   String values are always quoted in the query (even if they contain digits).
   *   Long/Integer values are sent as bare numbers.
   * @return the requester user if found, null if not found
   * @throws RuntimeException if multiple users are found or attributeValue is an unsupported type
   */
  public static FreshRequesterUser retrieveRequesterUserByAttribute(String configId, String attributeName, Object attributeValue) {

    if (StringUtils.isBlank(attributeName)) {
      throw new RuntimeException("attributeName is required");
    }
    if (attributeValue == null) {
      return null;
    }

    // validate attributeValue type
    if (!(attributeValue instanceof String) && !(attributeValue instanceof Long) && !(attributeValue instanceof Integer)) {
      throw new RuntimeException("attributeValue must be String, Long, or Integer, but was: " + attributeValue.getClass().getName());
    }

    // delegate to existing methods for id and email
    if ("id".equals(attributeName)) {
      return retrieveRequesterUserById(configId, GrouperUtil.longValue(attributeValue), false);
    }
    if ("email".equals(attributeName)) {
      return retrieveRequesterUserByEmail(configId, GrouperUtil.stringValue(attributeValue), false);
    }

    // determine the Freshservice query attribute name
    String freshserviceAttributeName = null;
    if ("externalId".equals(attributeName)) {
      freshserviceAttributeName = "external_id";
    } else if (attributeName.startsWith(FreshRequesterUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX)) {
      freshserviceAttributeName = attributeName.substring(FreshRequesterUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX.length());
    } else {
      throw new RuntimeException("Unsupported attributeName for requester lookup: '" + attributeName
          + "'. Expected 'id', 'email', 'externalId', or 'customField_<name>'");
    }

    if (StringUtils.isBlank(freshserviceAttributeName)) {
      throw new RuntimeException("Could not determine Freshservice attribute name from: '" + attributeName + "'");
    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRequesterUserByAttribute");
    debugMap.put("attributeName", attributeName);

    long startTime = System.nanoTime();

    try {
      int[] returnCode = new int[] { -1 };

      // build the query value: Long/Integer are numeric (no quotes), String always gets single quotes
      String queryValue;
      if (attributeValue instanceof Long || attributeValue instanceof Integer) {
        queryValue = freshserviceAttributeName + ":" + attributeValue;
      } else {
        queryValue = freshserviceAttributeName + ":'" + attributeValue + "'";
      }

      JsonNode jsonNode = executeMethod(debugMap, "retrieveRequesterUserByAttribute", "GET", configId, "api/v2/requesters",
          GrouperUtil.toSet(200), returnCode, null, null, false, queryValue);

      if (jsonNode == null) {
        return null;
      }

      ArrayNode requesterUserArray = (ArrayNode) jsonNode.get("requesters");

      if (requesterUserArray == null || requesterUserArray.size() == 0) {
        return null;
      }

      if (requesterUserArray.size() == 1) {
        JsonNode userNode = requesterUserArray.get(0);
        FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromJson(userNode);
        // skip agents
        if (grouperRequesterUser.getIsAgent() != null && grouperRequesterUser.getIsAgent()) {
          return null;
        }
        return grouperRequesterUser;
      }

      // multiple users found - throw a descriptive exception with first 10k of json
      String jsonString = GrouperUtil.jsonJacksonToString(jsonNode);
      if (jsonString.length() > 10000) {
        jsonString = jsonString.substring(0, 10000);
      }
      throw new RuntimeException("Expected 0 or 1 requesters for attribute '" + attributeName
          + "' = '" + attributeValue + "', but found " + requesterUserArray.size()
          + ". First 10k of response: " + jsonString);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }

  }

  /**
   * Add a requester user to a group.
   * @param configId the id of the external system
   * @param groupId the id of the group gaining a member user
   * @param userId the id of the new group member user
   */
  public static void addGroupMembership(String configId, Long groupId, Long userId) {
    updateGroupMembershipInternal(configId, groupId, userId, "POST");
  }

  /**
   * Remove a requester user from a group.
   * @param configId the id of the external system
   * @param groupId the id of the group losing a member user
   * @param userId the id of the group member user to remove
   */
  public static void removeGroupMembership(String configId, Long groupId, Long userId) {
    updateGroupMembershipInternal(configId, groupId, userId, "DELETE");
  }

  /**
   * Shared implementation to add/remove a requester user to/from a group.
   */
  private static void updateGroupMembershipInternal(String configId, Long groupId, Long userId, String httpMethod) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateGroupMembership");
    debugMap.put("httpMethod", httpMethod);

    long startTime = System.nanoTime();

    try {
      if (groupId == null) {
        throw new RuntimeException("groupId is null");
      }
      if (userId == null) {
        throw new RuntimeException("userId is null");
      }

      String addGroupId = String.valueOf(groupId);
      String addUserId = String.valueOf(userId);

      String urlPrefix = "api/v2/requester_groups/" + addGroupId + "/members/" + addUserId;

      Set<Integer> allowedReturnCodes = null;
      if ("POST".equals(httpMethod)) {
        allowedReturnCodes = GrouperUtil.toSet(200);
      } else if ("DELETE".equals(httpMethod)) {
        allowedReturnCodes = GrouperUtil.toSet(204, 404);
      } else {
        throw new RuntimeException("Unsupported httpMethod: " + httpMethod);
      }
      
      executeMethod(debugMap, "updateGroupMembership", httpMethod, configId, urlPrefix,
          allowedReturnCodes, new int[] { -1 }, null, null, false, null);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
  }
  
  /**
   * Retrieve the members of a group
   * @param configId the id of the external system
   * @param groupId the id of the group to get members from
   * @return list of requester users who are members of the group
   */
  public static List<FreshRequesterUser> retrieveMembershipsByGroup(String configId, Long groupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    List<FreshRequesterUser> results = new ArrayList<FreshRequesterUser>();
    
    debugMap.put("method", "retrieveMembershipsByGroup");
    
    long startTime = System.nanoTime();
    
    try {
      
      boolean lastPage = false;
      int page = 1;
      
      while (!lastPage) {
        
        JsonNode jsonNode = executeMethod(debugMap, "retrieveMembershipsByGroup", "GET", configId, "api/v2/requester_groups/" + String.valueOf(groupId) + "/members",
            GrouperUtil.toSet(200), new int[] { -1 }, null, page, true, null);
        
        ArrayNode requesterUsersArray = (ArrayNode) jsonNode.get("requesters");

        for (int i = 0; i < (requesterUsersArray == null ? 0 : requesterUsersArray.size()); i++) {
          JsonNode userNode = requesterUsersArray.get(i);
          FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromJson(userNode);
          results.add(grouperRequesterUser);
        }

        page++;

        if (requesterUsersArray.size() < grouperLoaderConfig.propertyValueInt("grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE)) {
          lastPage = true;
        }
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }

    return results;
  }
  
  /**
   * Deactivate (delete) a requester user in Freshservice.
   * Endpoint: DELETE /api/v2/requesters/{id}
   * Expected response: 204 No Content (sometimes 200/404 depending on Freshservice behavior)
   * @param configId the id of the external system
   * @param userId the requester user id
   */
  public static void deactivateRequesterUser(String configId, Long userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deactivateRequesterUser");

    long startTime = System.nanoTime();

    try {
      if (userId == null) {
        throw new RuntimeException("userId is null");
      }
      String id = String.valueOf(userId);

      executeMethod(debugMap, "deactivateRequesterUser", "DELETE", configId, "api/v2/requesters/" + id,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, null, false, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
  }

  /**
   * Reactivate a deactivated requester user in Freshservice.
   * Endpoint: PUT /api/v2/requesters/{id}/reactivate
   * Returns 200 if successful.  400 with body if already active:
   * {"code":"contact_already_active","message":"Contact is already active and cannot be restored."}
   * @param configId the id of the external system
   * @param userId the requester user id
   */
  public static void reactivateRequesterUser(String configId, Long userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "reactivateRequesterUser");

    long startTime = System.nanoTime();

    try {
      if (userId == null) {
        throw new RuntimeException("userId is null");
      }
      String id = String.valueOf(userId);

      int[] returnCode = new int[] { -1 };
      executeMethod(debugMap, "reactivateRequesterUser", "PUT", configId, "api/v2/requesters/" + id + "/reactivate",
          GrouperUtil.toSet(200, 400), returnCode, null, null, false, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
  }

  /**
   * Permanently delete (forget) a requester user from Freshservice.
   * This removes the user entirely, unlike deactivate which just sets active=false.
   * Endpoint: DELETE /api/v2/requesters/{id}/forget
   * Expected response: 204 No Content, 404 if already deleted
   * @param configId the id of the external system
   * @param userId the requester user id
   */
  public static void forgetRequesterUser(String configId, Long userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "forgetRequesterUser");

    long startTime = System.nanoTime();

    try {
      if (userId == null) {
        throw new RuntimeException("userId is null");
      }
      String id = String.valueOf(userId);

      executeMethod(debugMap, "forgetRequesterUser", "DELETE", configId, "api/v2/requesters/" + id + "/forget",
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, null, false, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
  }


}
  
