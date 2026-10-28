package edu.internet2.middleware.grouper.app.freshServiceAgent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.externalSystem.WsBearerTokenExternalSystem;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterApiCommands;
import edu.internet2.middleware.grouper.app.freshServiceRequester.FreshRequesterUser;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.app.provisioning.ProvisioningObjectChangeAction;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class FreshAgentApiCommands {
  
  
  private static final int MAX_PAGE_SIZE = 100;

  /**
   * json fields returned on an agent GET which cannot be sent back on an agent PUT.  active is here
   * since agents are deactivated with DELETE and reactivated with /reactivate
   */
  private static final Set<String> AGENT_READ_ONLY_JSON_FIELDS = GrouperUtil.toSet("id", "active", "created_at",
      "updated_at", "has_logged_in", "last_login_at", "last_active_at", "work_schedule_id", "department_names",
      "location_name", "member_of_pending_approval", "observer_of_pending_approval", "auto_assign_status_changed_at");
  public static final Set<String> doNotLogParameters = GrouperUtil.toSet("client_secret");
  public static final Set<String> doNotLogHeaders = GrouperUtil.toSet("authorization");
  
  public static void main(String[] args) {
    
    GrouperStartup.startup();
    
    try {
      String configId = "freshServiceDev";
//    String configId = "freshserviceTest";
    
//    FreshAgentGroup group = new FreshAgentGroup();
//    group.setName("Test Grouper Update");
//    group.setDescription("Testing Grouper mock service Update method");
//    group.setId(44209747L);
//    updateAgentGroup(configId, group);
//    createAgentGroup(configId, group);
//    
//    List<FreshAgentGroup> groups = retrieveAgentGroups(configId);
//    System.out.println(groups.size());

//    List<FreshAgentUser> users = retrieveAgentUsers(configId);
//    System.out.println(users.size());
    
//    FreshAgentUser user = new FreshAgentUser();
//    user.setFirstName("A");
//    user.setLastName("TestUser");
//    user.setEmail("a.testuser@test.edu");
//    createAgentUser(configId, user);
    
//    deleteAgentGroup(configId, 8070026L);

      List<FreshAgentGroup> groups = retrieveAgentGroups(configId);
      for (FreshAgentGroup group : GrouperUtil.nonNull(groups)) {
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
    
    // do not cache this in a static, the config object is replaced when config is reloaded, so a
    // cached instance never sees external systems added after this class was loaded in this JVM
    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();
    
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
   * Create an agent group in Freshservice
   * @param configId the id of the external system
   * @param grouperAgentGroup the agent group to be created in Freshservice
   */
  public static FreshAgentGroup createAgentGroup(String configId, FreshAgentGroup grouperAgentGroup) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAgentGroup");

    long startTime = System.nanoTime();

    try {
      JsonNode jsonToSend = grouperAgentGroup.toJson(null);

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "createAgentGroup", "POST", configId, "api/v2/groups",
          GrouperUtil.toSet(200, 201, 409), returnCode, jsonStringToSend, null, false, null);

      if (returnCode[0] == 409) {
        throw new RuntimeException("Agent group already exists: " + grouperAgentGroup.getName());
      }

      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "group");
      FreshAgentGroup createdGroup = FreshAgentGroup.fromJson(groupNode);

      return createdGroup;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
  }
  
  /**
   * Update a Freshservice agent group
   * @param configId the id of the external system
   * @param grouperAgentGroup the group to be updated in Freshservice
   */
  public static FreshAgentGroup updateAgentGroup(String configId, FreshAgentGroup grouperAgentGroup, Map<String, ProvisioningObjectChangeAction> fieldsToUpdate) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateAgentGroup");

    long startTime = System.nanoTime();

    try {

      if (grouperAgentGroup == null) {
        throw new RuntimeException("grouperAgentGroup is null");
      }

      Long groupId = grouperAgentGroup.getId();
      if (groupId == null || groupId == 0L) {
        throw new RuntimeException("groupId is null or 0 (unset)");
      }

      FreshAgentGroup agentGroupCurrentState = retrieveAgentGroup(configId, groupId);
      if (agentGroupCurrentState == null) {
        throw new RuntimeException("Cannot update agent group that does not exist in target. id=" + groupId);
      }

      ObjectNode jsonToSend = agentGroupCurrentState.toJson(null);

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
            if (isDelete || grouperAgentGroup.getName() == null) {
              jsonToSend.putNull("name");
            } else {
              jsonToSend.put("name", grouperAgentGroup.getName());
            }
          } else if ("description".equals(fieldName)) {
            if (isDelete || grouperAgentGroup.getDescription() == null) {
              jsonToSend.putNull("description");
            } else {
              jsonToSend.put("description", grouperAgentGroup.getDescription());
            }
          }
        }
      }

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "updateAgentGroup", "PUT", configId, "api/v2/groups/" + String.valueOf(groupId),
          GrouperUtil.toSet(200, 201), new int[] { -1 }, jsonStringToSend, null, false, null);

      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "group");
      FreshAgentGroup updatedGroup = FreshAgentGroup.fromJson(groupNode);
      return updatedGroup;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
  }
  
  /**
   * Update a Freshservice agent user.
   *
   * This method performs a three-step update:
   * 1. GET the current agent JSON from Freshservice by id
   * 2. Strip read-only attributes that cannot be sent on a PUT (see AGENT_READ_ONLY_JSON_FIELDS),
   *    and active which is managed with deactivate (DELETE) and reactivate
   * 3. Overlay the fields indicated in fieldsToUpdate with values from the
   *    supplied FreshAgentUser, then PUT the result
   *
   * The fieldsToUpdate set uses Java-style field names which are translated
   * to their Freshservice JSON attribute names (e.g. "firstName" becomes "first_name",
   * "email" becomes "email", "departmentId" becomes "department_ids" array).
   *
   * Custom fields use the prefix "customField_" followed by the Freshservice
   * custom field name (e.g. "customField_pennkey").
   *
   * @param configId the id of the external system
   * @param grouperAgentUser the agent user containing the new values.
   *   Must have id set to identify which user to update.
   * @param fieldsToUpdate set of Java field names to update. Supported values:
   *   "firstName", "lastName", "email", "jobTitle", "workPhoneNumber",
   *   "departmentId", "reportingManagerId", "address", "externalId", "occasional",
   *   and custom fields with prefix "customField_" (e.g. "customField_pennkey")
   * @return the updated agent user parsed from the PUT response
   */
  public static FreshAgentUser updateAgentUser(String configId, FreshAgentUser grouperAgentUser, Set<String> fieldsToUpdate) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateAgentUser");

    long startTime = System.nanoTime();

    try {

      // validate input
      if (grouperAgentUser == null) {
        throw new RuntimeException("grouperAgentUser is null");
      }

      Long userId = grouperAgentUser.getId();
      if (userId == null || userId == 0L) {
        throw new RuntimeException("userId is null or 0 (unset)");
      }

      // Step 1: GET the current state of the agent from Freshservice
      // GET /api/v2/agents/{id} returns { "agent": { ... } }
      int[] getReturnCode = new int[] { -1 };
      String getUrlSuffix = "api/v2/agents/" + String.valueOf(userId);
      JsonNode getJsonNode = executeMethod(debugMap, "updateAgentUser", "GET", configId, getUrlSuffix,
          GrouperUtil.toSet(200, 404), getReturnCode, null, null, false, null);

      // if the user does not exist, we cannot update
      if (getReturnCode[0] == 404 || getJsonNode == null) {
        throw new RuntimeException("Cannot update agent user that does not exist in target. id=" + userId);
      }

      // extract the "agent" object from the response
      JsonNode agentNode = getJsonNode.get("agent");
      if (agentNode == null) {
        throw new RuntimeException("Cannot update agent user that does not exist in target. id=" + userId);
      }

      // Step 2: deep copy to a mutable ObjectNode and strip read-only attributes
      // that the Freshservice PUT endpoint does not accept
      ObjectNode jsonToSend = agentNode.deepCopy();
      for (String readOnlyJsonField : AGENT_READ_ONLY_JSON_FIELDS) {
        jsonToSend.remove(readOnlyJsonField);
      }

      // Step 3: overlay only the fields indicated in fieldsToUpdate.
      // Each field name is a Java-style name that maps to a Freshservice JSON attribute.
      // If the value on the FreshAgentUser is non-null, set it; otherwise send null.
      if (fieldsToUpdate != null) {
        for (String fieldName : fieldsToUpdate) {
          if (StringUtils.isBlank(fieldName)) {
            continue;
          }

          // read-only attributes that Freshservice does not allow on PUT.
          // If a provisioner tries to update these, the CRUD update setting
          // for this attribute should be set to false in the provisioner configuration.
          // active is not sent on an agent PUT, agents are deactivated with DELETE and reactivated with /reactivate
          if ("id".equals(fieldName) || "active".equals(fieldName)
              || "createdAt".equals(fieldName) || "hasLoggedIn".equals(fieldName)
              || "updatedAt".equals(fieldName) || "workScheduleId".equals(fieldName)
              || "departmentNames".equals(fieldName) || "locationName".equals(fieldName)) {
            throw new RuntimeException("Cannot update read-only attribute '" + fieldName
                + "'. Set CRUD update to false for this attribute in the provisioner configuration.");
          }

          // "firstName" -> JSON "first_name"
          if ("firstName".equals(fieldName)) {
            if (grouperAgentUser.getFirstName() != null) {
              jsonToSend.put("first_name", grouperAgentUser.getFirstName());
            } else {
              jsonToSend.putNull("first_name");
            }

          // "lastName" -> JSON "last_name"
          } else if ("lastName".equals(fieldName)) {
            if (grouperAgentUser.getLastName() != null) {
              jsonToSend.put("last_name", grouperAgentUser.getLastName());
            } else {
              jsonToSend.putNull("last_name");
            }

          // "email" -> JSON "email"
          } else if ("email".equals(fieldName)) {
            if (grouperAgentUser.getEmail() != null) {
              jsonToSend.put("email", grouperAgentUser.getEmail());
            } else {
              jsonToSend.putNull("email");
            }

          // "jobTitle" -> JSON "job_title"
          } else if ("jobTitle".equals(fieldName)) {
            if (grouperAgentUser.getJobTitle() != null) {
              jsonToSend.put("job_title", grouperAgentUser.getJobTitle());
            } else {
              jsonToSend.putNull("job_title");
            }

          // "workPhoneNumber" -> JSON "work_phone_number"
          } else if ("workPhoneNumber".equals(fieldName)) {
            if (grouperAgentUser.getWorkPhoneNumber() != null) {
              jsonToSend.put("work_phone_number", grouperAgentUser.getWorkPhoneNumber());
            } else {
              jsonToSend.putNull("work_phone_number");
            }

          // "departmentId" -> JSON "department_ids" (array with single element)
          } else if ("departmentId".equals(fieldName)) {
            if (grouperAgentUser.getDepartmentId() != null) {
              // Freshservice expects department_ids as an array
              ArrayNode departmentIdsArray = GrouperUtil.jsonJacksonArrayNode();
              departmentIdsArray.add(grouperAgentUser.getDepartmentId().longValue());
              jsonToSend.set("department_ids", departmentIdsArray);
            } else {
              jsonToSend.putNull("department_ids");
            }

          // "reportingManagerId" -> JSON "reporting_manager_id"
          } else if ("reportingManagerId".equals(fieldName)) {
            if (grouperAgentUser.getReportingManagerId() != null) {
              jsonToSend.put("reporting_manager_id", grouperAgentUser.getReportingManagerId().longValue());
            } else {
              jsonToSend.putNull("reporting_manager_id");
            }

          // "address" -> JSON "address"
          } else if ("address".equals(fieldName)) {
            if (grouperAgentUser.getAddress() != null) {
              jsonToSend.put("address", grouperAgentUser.getAddress());
            } else {
              jsonToSend.putNull("address");
            }

          // "externalId" -> JSON "external_id"
          } else if ("externalId".equals(fieldName)) {
            if (grouperAgentUser.getExternalId() != null) {
              jsonToSend.put("external_id", grouperAgentUser.getExternalId());
            } else {
              jsonToSend.putNull("external_id");
            }

          // "occasional" -> JSON "occasional" (boolean).  Note: false (full-time) uses a licensed seat
          } else if ("occasional".equals(fieldName)) {
            if (grouperAgentUser.getOccasional() != null) {
              jsonToSend.put("occasional", grouperAgentUser.getOccasional().booleanValue());
            } else {
              jsonToSend.putNull("occasional");
            }

          // "customField_<name>" -> nested inside JSON "custom_fields" object
          // e.g. "customField_pennkey" sets custom_fields.pennkey
          } else if (fieldName.startsWith(FreshAgentUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX)) {

            // strip the prefix to get the actual Freshservice custom field name
            String customFieldName = fieldName.substring(FreshAgentUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX.length());
            if (!StringUtils.isBlank(customFieldName)) {

              // get or create the custom_fields JSON object
              ObjectNode customFieldsNode = (ObjectNode) GrouperUtil.jsonJacksonGetNode(jsonToSend, "custom_fields");
              if (customFieldsNode == null) {
                customFieldsNode = GrouperUtil.jsonJacksonNode();
                jsonToSend.set("custom_fields", customFieldsNode);
              }

              // get the custom field value from the FreshAgentUser
              Object customValue = grouperAgentUser.getCustomFields() == null ? null
                  : grouperAgentUser.getCustomFields().get(customFieldName);

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

      // PUT /api/v2/agents/{id} returns { "agent": { ... } }
      JsonNode responseNode = executeMethod(debugMap, "updateAgentUser", "PUT", configId, "api/v2/agents/" + String.valueOf(userId),
          GrouperUtil.toSet(200, 201), new int[] { -1 }, jsonStringToSend, null, false, null);

      // parse the updated agent from the response
      JsonNode updatedUserNode = GrouperUtil.jsonJacksonGetNode(responseNode, "agent");
      FreshAgentUser updatedUser = FreshAgentUser.fromJson(updatedUserNode);
      return updatedUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
  }
  
  /**
   * Delete an agent group
   * @param configId the id of the external system
   * @param groupId the id of the group to be deleted
   */
  public static void deleteAgentGroup(String configId, Long groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteAgentGroup");

    long startTime = System.nanoTime();
    
    try {
      
      if (groupId == null) {
        throw new RuntimeException("groupId is null");
      }
      String id = String.valueOf(groupId);
      
      executeMethod(debugMap, "deleteAgentGroup", "DELETE", configId, "api/v2/groups/" + id,
          GrouperUtil.toSet(200, 204, 404), new int[] { -1 }, null, null, false, null);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
    
  }
  
  /**
   * Get a Freshservice agent group
   * @param configId the id of the external system
   * @param id the agent group id
   * @return the GrouperAgentGroup matching the Freshservice group retrieved
   */
  public static FreshAgentGroup retrieveAgentGroup(String configId, Long id) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    debugMap.put("method", "retrieveAgentGroup");
    
    long startTime = System.nanoTime();
    
    try {
      String urlSuffix = "api/v2/groups/" + String.valueOf(id);
      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "retrieveAgentGroup", "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), returnCode, null, null, false, null);
      if (returnCode[0] == 404) {
        return null;
      }
      
      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "group");
      if (groupNode == null) {
        return null;
      }
      FreshAgentGroup grouperAgentGroup = FreshAgentGroup.fromJson(groupNode);

      // generic provisioner sync-back: register the group from the raw inner JSON (full fidelity,
      // not the lossy typed bean) while the node is in scope. No-op outside a FreshAgent
      // provisioning cycle.
      FreshAgentProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(groupNode);

      return grouperAgentGroup;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
  }

  /**
   * Get a list of all Freshservice agent groups
   * @param configId the id of the external system
   * @return
   */
  public static List<FreshAgentGroup> retrieveAgentGroups(String configId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveAgentGroups");
    
    List<FreshAgentGroup> results = new ArrayList<FreshAgentGroup>();
    
    long startTime = System.nanoTime();
    
    try {
      
      boolean lastPage = false;
      int page = 1;
      
      while (!lastPage) {
        
        JsonNode jsonNode = executeMethod(debugMap, "retrieveAgentGroups", "GET", configId, "api/v2/groups",
            GrouperUtil.toSet(200), new int[] { -1 }, null, page, true, null);
        
        ArrayNode groupsArray = (ArrayNode) jsonNode.get("groups");
        
        for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
          JsonNode groupNode = groupsArray.get(i);
          FreshAgentGroup grouperAgentGroup = FreshAgentGroup.fromJson(groupNode);
          results.add(grouperAgentGroup);
          // generic provisioner sync-back: register the group from the raw inner JSON (full
          // fidelity, not the lossy typed bean) while the node is in scope. No-op outside a
          // FreshAgent provisioning cycle.
          FreshAgentProvisioningTargetNativeSync.captureGroupJsonFromCurrentProvisioner(groupNode);
        }

        // live progress: pages groups over many slow WS calls with no total available, so report
        // count-so-far and page number.  Uses the existing thread-scoped current provisioner; null off a run.
        GrouperProvisioner currentProvisionerForGroups = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
        if (currentProvisionerForGroups != null) {
          currentProvisionerForGroups.assignProgressLabelTarget("retrieving groups from target: " + results.size() + " so far (page " + page + ")");
        }

        page++;
        
        if (groupsArray.size() < GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE)) {
          lastPage = true;
        }
        
      }
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
    
    return results;
  }
  
  /**
   * Create an agent user in Freshservice, or update an existing one if a user
   * with the same email address already exists.
   *
   * This method first looks up the agent by email address:
   * - If an agent already exists and is inactive, it will be reactivated and updated
   *   with all the fields from grouperAgentUser.
   * - If an agent already exists and is active, it will be updated with all the
   *   fields from grouperAgentUser.
   * - If a requester (not an agent) has that email, it is converted to an agent.  Freshservice
   *   has one unique email across agents and requesters, so a POST of a new agent would fail.
   *   The converted agent is occasional, then it is updated with all the fields from grouperAgentUser.
   * - If no agent or requester exists with that email, a new agent is created via the helper method.
   *
   * @param configId the id of the external system
   * @param grouperAgentUser the user to be created or updated in Freshservice
   * @param reactivateAsFullTime Freshservice reactivates and converts agents as occasional,
   *   if true then set them to full-time afterwards (uses a licensed seat)
   * @return the created or updated agent user
   */
  public static FreshAgentUser createAgentUser(String configId, FreshAgentUser grouperAgentUser, boolean reactivateAsFullTime) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAgentUser");

    long startTime = System.nanoTime();

    try {

      // look up existing user by email address
      FreshAgentUser existingUser = null;
      if (!StringUtils.isBlank(grouperAgentUser.getEmail())) {
        existingUser = retrieveAgentUserByEmail(configId, grouperAgentUser.getEmail(), true);
      }

      // if no agent, see if a requester has this email, if so convert it to an agent
      if (existingUser == null && !StringUtils.isBlank(grouperAgentUser.getEmail())) {
        FreshRequesterUser existingRequester = FreshRequesterApiCommands.retrieveRequesterUserByEmail(
            configId, grouperAgentUser.getEmail(), true);
        if (existingRequester != null) {
          debugMap.put("convertRequesterId", existingRequester.getId());
          existingUser = convertRequesterToAgent(configId, existingRequester.getId(), reactivateAsFullTime);
        }
      }

      if (existingUser != null) {

        // if the existing user is not active, reactivate it first
        if (existingUser.getActive() == null || !existingUser.getActive()) {
          reactivateAgentUser(configId, existingUser.getId(), reactivateAsFullTime);
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
        if (grouperAgentUser.getOccasional() != null) {
          fieldsToUpdate.add("occasional");
        }

        // add any custom fields from the grouperAgentUser
        if (grouperAgentUser.getCustomFields() != null) {
          for (String customFieldName : grouperAgentUser.getCustomFields().keySet()) {
            if (!StringUtils.isBlank(customFieldName)) {
              fieldsToUpdate.add(FreshAgentUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX + customFieldName);
            }
          }
        }

        // set the id from the existing user so updateAgentUser can find it
        grouperAgentUser.setId(existingUser.getId());

        // update the existing user with all fields
        return updateAgentUser(configId, grouperAgentUser, fieldsToUpdate);
      }

      // no existing user found - create a new one
      return createAgentUserHelper(configId, grouperAgentUser);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }

  }

  /**
   * Helper method to create a new agent user in Freshservice via POST.
   * This method directly calls the Freshservice POST /api/v2/agents endpoint.
   * Callers should typically use createAgentUser() which handles the
   * lookup-by-email and update-if-exists logic.
   *
   * @param configId the id of the external system
   * @param grouperAgentUser the user to be created in Freshservice
   * @return the created agent user with assigned id
   */
  public static FreshAgentUser createAgentUserHelper(String configId, FreshAgentUser grouperAgentUser) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createAgentUserHelper");

    long startTime = System.nanoTime();

    try {
      JsonNode jsonToSend = grouperAgentUser.toJson(null);

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "createAgentUserHelper", "POST", configId, "api/v2/agents",
          GrouperUtil.toSet(200, 201, 409), returnCode, jsonStringToSend, null, false, null);

      if (returnCode[0] == 409) {
        throw new RuntimeException("Agent user already exists: " + grouperAgentUser.getEmail());
      }

      JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "agent");
      FreshAgentUser createdUser = FreshAgentUser.fromJson(userNode);
      return createdUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }

  }
  
  /**
   * Retrieve all agent users from Freshservice
   * @param configId the id of the external system
   * @param includeInactiveAgents if true, include inactive (deactivated) agents
   *   in the results. If false, only active agents are returned.
   * @return a list of Freshservice agent users
   */
  public static List<FreshAgentUser> retrieveAgentUsers(String configId, boolean includeInactiveAgents) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    List<FreshAgentUser> results = new ArrayList<FreshAgentUser>();

    debugMap.put("method", "retrieveAgentUsers");

    long startTime = System.nanoTime();

    try {

      boolean lastPage = false;
      int page = 1;

      while (!lastPage) {

        JsonNode jsonNode = executeMethod(debugMap, "retrieveAgentUsers", "GET", configId, "api/v2/agents",
            GrouperUtil.toSet(200), new int[] { -1 }, null, page, true, null);

        ArrayNode agentUsersArray = (ArrayNode) jsonNode.get("agents");

        for (int i = 0; i < (agentUsersArray == null ? 0 : agentUsersArray.size()); i++) {
          JsonNode userNode = agentUsersArray.get(i);
          FreshAgentUser grouperAgentUser = FreshAgentUser.fromJson(userNode);
          // skip inactive agents unless caller explicitly wants them
          if (!includeInactiveAgents
              && (grouperAgentUser.getActive() == null || !grouperAgentUser.getActive())) {
            continue;
          }
          results.add(grouperAgentUser);
          // generic provisioner sync-back: register the user from the raw inner JSON (full fidelity,
          // not the lossy typed bean) while the node is in scope. Captured after the inactive
          // skips so the captured population matches what the DAO reconciles. No-op outside a
          // FreshAgent provisioning cycle.
          FreshAgentProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(userNode);
        }

        // live progress: pages users over many slow WS calls with no total available, so report
        // count-so-far and page number.  Uses the existing thread-scoped current provisioner; null off a run.
        GrouperProvisioner currentProvisionerForUsers = GrouperProvisioner.retrieveCurrentGrouperProvisioner();
        if (currentProvisionerForUsers != null) {
          currentProvisionerForUsers.assignProgressLabelTarget("retrieving users from target: " + results.size() + " so far (page " + page + ")");
        }

        page++;

        if (agentUsersArray.size() < GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.wsBearerToken." + configId + ".pageSize", MAX_PAGE_SIZE)) {
          lastPage = true;
        }
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }

    return results;
  }

  /**
   * Get a Freshservice agent user by id
   * @param configId the id of the external system
   * @param id the id of the agent user to be retrieved
   * @param includeInactiveAgents if true, return the user even if inactive.
   *   If false, return null for inactive users.
   * @return the agent user, or null if not found (or inactive when not included)
   */
  public static FreshAgentUser retrieveAgentUserById(String configId, Long id, boolean includeInactiveAgents) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAgentUserById");

    long startTime = System.nanoTime();

    try {
      int[] returnCode = new int[] { -1 };

      String urlSuffix = "api/v2/agents/" + String.valueOf(id);
      JsonNode jsonNode = executeMethod(debugMap, "retrieveAgentUserById", "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), returnCode, null, null, false, null);

      if (returnCode[0] == 404) {
        return null;
      }

      JsonNode userNode = jsonNode.get("agent");
      if (userNode == null) {
        return null;
      }

      FreshAgentUser grouperAgentUser = FreshAgentUser.fromJson(userNode);

      // skip inactive agents unless caller explicitly wants them
      if (!includeInactiveAgents
          && (grouperAgentUser.getActive() == null || !grouperAgentUser.getActive())) {
        return null;
      }

      // generic provisioner sync-back: register the user from the raw inner JSON (full fidelity,
      // not the lossy typed bean) while the node is in scope. Captured only for users that survive
      // the inactive skips, matching what the DAO reconciles. No-op outside a FreshAgent
      // provisioning cycle.
      FreshAgentProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(userNode);

      return grouperAgentUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }

  }

  /**
   * Get a Freshservice agent user by email address.
   * Uses the Freshservice email parameter:
   *   GET /api/v2/agents?email=jsmith@upenn.edu
   *
   * @param configId the id of the external system
   * @param email the email address of the agent user to be retrieved
   * @param includeInactiveAgents if true, return the user even if inactive.
   *   If false, return null for inactive users.
   * @return the agent user, or null if not found (or inactive when not included)
   */
  public static FreshAgentUser retrieveAgentUserByEmail(String configId, String email, boolean includeInactiveAgents) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAgentUserByEmail");

    long startTime = System.nanoTime();

    try {
      int[] returnCode = new int[] { -1 };

      // use the email= URL parameter instead of query=
      String urlSuffix = "api/v2/agents?email=" + GrouperUtil.escapeUrlEncode(email);
      JsonNode jsonNode = executeMethod(debugMap, "retrieveAgentUserByEmail", "GET", configId, urlSuffix,
          GrouperUtil.toSet(200), returnCode, null, null, false, null);

      if (jsonNode == null) {
        return null;
      }

      ArrayNode agentUserArray = (ArrayNode) jsonNode.get("agents");

      if (agentUserArray.size()==1) {
        JsonNode userNode = agentUserArray.get(0);
        FreshAgentUser grouperAgentUser = FreshAgentUser.fromJson(userNode);
        // skip inactive agents unless caller explicitly wants them
        if (!includeInactiveAgents
            && (grouperAgentUser.getActive() == null || !grouperAgentUser.getActive())) {
          return null;
        }
        // generic provisioner sync-back: register the user from the raw inner JSON (full fidelity,
        // not the lossy typed bean) while the node is in scope. Captured only for users that
        // survive the inactive skips, matching what the DAO reconciles. No-op outside a
        // FreshAgent provisioning cycle.
        FreshAgentProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(userNode);
        return grouperAgentUser;
      }

      return null;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }

  }

  /**
   * Retrieve an agent user by a provisioning attribute name and value.
   *
   * If attributeName is "id" or "email", delegates to the existing lookup methods.
   * If attributeName is "externalId", searches by external_id.
   * If attributeName starts with "customField_", searches by that custom field name.
   * Otherwise throws an exception.
   *
   * The Freshservice API query format is:
   *   GET /api/v2/agents?query=attributeName:'value'   (for strings)
   *   GET /api/v2/agents?query=attributeName:value      (for numbers)
   *
   * @param configId the id of the external system
   * @param attributeName the provisioning attribute name (e.g. "id", "email", "externalId", "customField_pennkey")
   * @param attributeValue the value to search for. Must be String, Long, or Integer.
   *   String values are always quoted in the query (even if they contain digits).
   *   Long/Integer values are sent as bare numbers.
   * @return the agent user if found, null if not found
   * @throws RuntimeException if multiple users are found or attributeValue is an unsupported type
   */
  public static FreshAgentUser retrieveAgentUserByAttribute(String configId, String attributeName, Object attributeValue) {

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
      return retrieveAgentUserById(configId, GrouperUtil.longValue(attributeValue), false);
    }
    if ("email".equals(attributeName)) {
      return retrieveAgentUserByEmail(configId, GrouperUtil.stringValue(attributeValue), false);
    }

    // determine the Freshservice query attribute name
    String freshserviceAttributeName = null;
    if ("externalId".equals(attributeName)) {
      freshserviceAttributeName = "external_id";
    } else if (attributeName.startsWith(FreshAgentUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX)) {
      freshserviceAttributeName = attributeName.substring(FreshAgentUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX.length());
    } else {
      throw new RuntimeException("Unsupported attributeName for agent lookup: '" + attributeName
          + "'. Expected 'id', 'email', 'externalId', or 'customField_<name>'");
    }

    if (StringUtils.isBlank(freshserviceAttributeName)) {
      throw new RuntimeException("Could not determine Freshservice attribute name from: '" + attributeName + "'");
    }

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAgentUserByAttribute");
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

      JsonNode jsonNode = executeMethod(debugMap, "retrieveAgentUserByAttribute", "GET", configId, "api/v2/agents",
          GrouperUtil.toSet(200), returnCode, null, null, false, queryValue);

      if (jsonNode == null) {
        return null;
      }

      ArrayNode agentUserArray = (ArrayNode) jsonNode.get("agents");

      if (agentUserArray == null || agentUserArray.size() == 0) {
        return null;
      }

      if (agentUserArray.size() == 1) {
        JsonNode userNode = agentUserArray.get(0);
        FreshAgentUser grouperAgentUser = FreshAgentUser.fromJson(userNode);
        // generic provisioner sync-back: register the user from the raw inner JSON (full fidelity,
        // not the lossy typed bean) while the node is in scope. This branch handles the externalId /
        // customField_ lookups; the id/email lookups return earlier via delegation to
        // retrieveAgentUserById/retrieveAgentUserByEmail (which capture there), so there is
        // no double capture. No-op outside a FreshAgent provisioning cycle.
        FreshAgentProvisioningTargetNativeSync.captureUserJsonFromCurrentProvisioner(userNode);
        return grouperAgentUser;
      }

      // multiple users found - throw a descriptive exception with first 10k of json
      String jsonString = GrouperUtil.jsonJacksonToString(jsonNode);
      if (jsonString.length() > 10000) {
        jsonString = jsonString.substring(0, 10000);
      }
      throw new RuntimeException("Expected 0 or 1 agents for attribute '" + attributeName
          + "' = '" + attributeValue + "', but found " + agentUserArray.size()
          + ". First 10k of response: " + jsonString);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }

  }

  /**
   * Add an agent user to an agent group.
   * @param configId the id of the external system
   * @param groupId the id of the group gaining a member user
   * @param userId the id of the new group member user
   */
  public static void addGroupMembership(String configId, Long groupId, Long userId) {
    updateGroupMembershipInternal(configId, groupId, userId, true);
  }

  /**
   * Remove an agent user from an agent group.
   * @param configId the id of the external system
   * @param groupId the id of the group losing a member user
   * @param userId the id of the group member user to remove
   */
  public static void removeGroupMembership(String configId, Long groupId, Long userId) {
    updateGroupMembershipInternal(configId, groupId, userId, false);
  }

  /**
   * Shared implementation to add/remove an agent user to/from an agent group.
   * Unlike requester groups, agent groups do not have member endpoints, the members are a
   * "members" array of agent ids on the group.  So GET the group, add or remove the id, and PUT
   * the members array back (only if it changed)
   */
  private static void updateGroupMembershipInternal(String configId, Long groupId, Long userId, boolean add) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateGroupMembership");
    debugMap.put("add", add);

    long startTime = System.nanoTime();

    try {
      if (groupId == null) {
        throw new RuntimeException("groupId is null");
      }
      if (userId == null) {
        throw new RuntimeException("userId is null");
      }

      List<Long> memberIds = retrieveGroupMemberIds(debugMap, configId, groupId);
      if (memberIds == null) {
        // group is not there, nothing to remove
        if (add) {
          throw new RuntimeException("Cannot add member to agent group that does not exist in target. id=" + groupId);
        }
        return;
      }

      boolean changed = add ? (!memberIds.contains(userId) && memberIds.add(userId)) : memberIds.remove(userId);
      debugMap.put("changed", changed);
      if (!changed) {
        return;
      }

      ObjectNode jsonToSend = GrouperUtil.jsonJacksonNode();
      ArrayNode membersArray = GrouperUtil.jsonJacksonArrayNode();
      for (Long memberId : memberIds) {
        membersArray.add(memberId.longValue());
      }
      jsonToSend.set("members", membersArray);

      executeMethod(debugMap, "updateGroupMembership", "PUT", configId, "api/v2/groups/" + String.valueOf(groupId),
          GrouperUtil.toSet(200), new int[] { -1 }, GrouperUtil.jsonJacksonToString(jsonToSend), null, false, null);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
  }

  /**
   * get the agent ids in the "members" array of an agent group
   * @param debugMap
   * @param configId
   * @param groupId
   * @return the member ids (mutable list) or null if group not found
   */
  private static List<Long> retrieveGroupMemberIds(Map<String, Object> debugMap, String configId, Long groupId) {
    int[] returnCode = new int[] { -1 };
    JsonNode jsonNode = executeMethod(debugMap, "retrieveGroupMemberIds", "GET", configId, "api/v2/groups/" + String.valueOf(groupId),
        GrouperUtil.toSet(200, 404), returnCode, null, null, false, null);
    if (returnCode[0] == 404) {
      return null;
    }
    JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "group");
    if (groupNode == null) {
      return null;
    }
    List<Long> memberIds = new ArrayList<Long>();
    JsonNode membersNode = GrouperUtil.jsonJacksonGetNode(groupNode, "members");
    for (int i = 0; i < (membersNode == null || !membersNode.isArray() ? 0 : membersNode.size()); i++) {
      memberIds.add(membersNode.get(i).asLong());
    }
    return memberIds;
  }
  
  /**
   * Retrieve the members of an agent group.  These are the ids in the "members" array of the group,
   * so the returned users only have the id set
   * @param configId the id of the external system
   * @param groupId the id of the group to get members from
   * @return list of agent users who are members of the group
   */
  public static List<FreshAgentUser> retrieveMembershipsByGroup(String configId, Long groupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    List<FreshAgentUser> results = new ArrayList<FreshAgentUser>();
    
    debugMap.put("method", "retrieveMembershipsByGroup");
    
    long startTime = System.nanoTime();
    
    try {
      
      List<Long> memberIds = retrieveGroupMemberIds(debugMap, configId, groupId);

      for (Long memberId : GrouperUtil.nonNull(memberIds)) {
        FreshAgentUser grouperAgentUser = new FreshAgentUser();
        grouperAgentUser.setId(memberId);
        results.add(grouperAgentUser);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }

    return results;
  }
  
  /**
   * Deactivate (delete) an agent user in Freshservice.
   * Endpoint: DELETE /api/v2/agents/{id}
   * Expected response: 204 No Content (sometimes 200/404 depending on Freshservice behavior)
   * @param configId the id of the external system
   * @param userId the agent user id
   */
  public static void deactivateAgentUser(String configId, Long userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deactivateAgentUser");

    long startTime = System.nanoTime();

    try {
      if (userId == null) {
        throw new RuntimeException("userId is null");
      }
      String id = String.valueOf(userId);

      executeMethod(debugMap, "deactivateAgentUser", "DELETE", configId, "api/v2/agents/" + id,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, null, false, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
  }

  /**
   * Reactivate a deactivated agent user in Freshservice.
   * Endpoint: PUT /api/v2/agents/{id}/reactivate
   * Returns 200 if successful.  400 with body if already active.
   * Freshservice reactivates an agent as occasional regardless of what it was before
   * @param configId the id of the external system
   * @param userId the agent user id
   * @param reactivateAsFullTime if true and the agent was reactivated, PUT occasional=false to make it
   *   full-time again (uses a licensed seat)
   */
  public static void reactivateAgentUser(String configId, Long userId, boolean reactivateAsFullTime) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "reactivateAgentUser");

    long startTime = System.nanoTime();

    try {
      if (userId == null) {
        throw new RuntimeException("userId is null");
      }
      String id = String.valueOf(userId);

      int[] returnCode = new int[] { -1 };
      executeMethod(debugMap, "reactivateAgentUser", "PUT", configId, "api/v2/agents/" + id + "/reactivate",
          GrouperUtil.toSet(200, 400), returnCode, null, null, false, null);

      // 400 is already active, so leave the license type alone
      if (reactivateAsFullTime && returnCode[0] == 200) {
        assignAgentFullTime(debugMap, configId, userId);
      }

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
  }

  /**
   * Convert a requester to an agent in Freshservice.  The agent has the same id as the requester.
   * Endpoint: PUT /api/v2/requesters/{id}/convert_to_agent
   * Freshservice makes the converted agent occasional
   * @param configId the id of the external system
   * @param requesterId the requester id
   * @param asFullTime if true, PUT occasional=false to make it full-time (uses a licensed seat)
   * @return the agent
   */
  public static FreshAgentUser convertRequesterToAgent(String configId, Long requesterId, boolean asFullTime) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "convertRequesterToAgent");

    long startTime = System.nanoTime();

    try {
      if (requesterId == null) {
        throw new RuntimeException("requesterId is null");
      }

      JsonNode jsonNode = executeMethod(debugMap, "convertRequesterToAgent", "PUT", configId, "api/v2/requesters/" + String.valueOf(requesterId) + "/convert_to_agent",
          GrouperUtil.toSet(200), new int[] { -1 }, null, null, false, null);

      FreshAgentUser grouperAgentUser = FreshAgentUser.fromJson(GrouperUtil.jsonJacksonGetNode(jsonNode, "agent"));
      if (grouperAgentUser == null || grouperAgentUser.getId() == null) {
        throw new RuntimeException("convert_to_agent did not return an agent for requester id=" + requesterId);
      }

      if (asFullTime) {
        assignAgentFullTime(debugMap, configId, grouperAgentUser.getId());
        grouperAgentUser.setOccasional(false);
      }
      return grouperAgentUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
  }

  /**
   * make an agent full-time (not occasional), which uses a licensed seat
   * Endpoint: PUT /api/v2/agents/{id} with {"occasional": false}
   * @param debugMap
   * @param configId
   * @param userId
   */
  private static void assignAgentFullTime(Map<String, Object> debugMap, String configId, Long userId) {
    ObjectNode jsonToSend = GrouperUtil.jsonJacksonNode();
    jsonToSend.put("occasional", false);
    executeMethod(debugMap, "assignAgentFullTime", "PUT", configId, "api/v2/agents/" + String.valueOf(userId),
        GrouperUtil.toSet(200), new int[] { -1 }, GrouperUtil.jsonJacksonToString(jsonToSend), null, false, null);
  }

  /**
   * Permanently delete (forget) an agent user from Freshservice.
   * This removes the user entirely, unlike deactivate which just sets active=false.
   * Endpoint: DELETE /api/v2/agents/{id}/forget
   * Expected response: 204 No Content, 404 if already deleted
   * @param configId the id of the external system
   * @param userId the agent user id
   */
  public static void forgetAgentUser(String configId, Long userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "forgetAgentUser");

    long startTime = System.nanoTime();

    try {
      if (userId == null) {
        throw new RuntimeException("userId is null");
      }
      String id = String.valueOf(userId);

      executeMethod(debugMap, "forgetAgentUser", "DELETE", configId, "api/v2/agents/" + id + "/forget",
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, null, false, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshAgentLog.freshserviceLog(debugMap, startTime);
    }
  }


}
  
