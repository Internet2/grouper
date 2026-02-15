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
  
  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes, 
      int[] returnCode, String bodyParam, Integer page, boolean addPageSize, String email) {
    
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
    
    if (StringUtils.isNotBlank(email)) {
      grouperHttpClient.addUrlParameter("query", String.format("primary_email:%s", email));
    }
    
    if (httpMethodName.equals("POST") || httpMethodName.equals("PUT")) {
      grouperHttpClient.addHeader("Content-Type", "application/json; charset=utf-8");
    }
    
    grouperHttpClient.executeRequest();
    
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
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/v2/requester_groups",
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
      if (groupId == 0) {
        // legacy pattern: 0 means unset
        throw new RuntimeException("groupId is 0 (unset)");
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
            if (isDelete) {
              jsonToSend.putNull("name");
            } else {
              jsonToSend.put("name", grouperRequesterGroup.getName());
            }
          } else if ("description".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("description");
            } else {
              jsonToSend.put("description", grouperRequesterGroup.getDescription());
            }
          }
        }
      }

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      JsonNode jsonNode = executeMethod(debugMap, "PUT", configId, "api/v2/requester_groups/" + String.valueOf(groupId),
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
   * Update a Freshservice requester user
   * @param configId the id of the external system
   * @param grouperRequesterUser the user to be updated in Freshservice
   * @param fieldsToUpdate map of fieldName -> change action
   */
  public static void updateRequesterUser(String configId, FreshRequesterUser grouperRequesterUser, Map<String, ProvisioningObjectChangeAction> fieldsToUpdate) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateRequesterUser");

    long startTime = System.nanoTime();

    try {

      if (grouperRequesterUser == null) {
        throw new RuntimeException("grouperRequesterUser is null");
      }

      long userId = grouperRequesterUser.getId();
      if (userId == 0) {
        throw new RuntimeException("userId is 0 (unset)");
      }

      FreshRequesterUser requesterUserCurrentState = retrieveRequesterUser(configId, userId);
      if (requesterUserCurrentState == null) {
        throw new RuntimeException("Cannot update requester user that does not exist in target. id=" + userId);
      }

      ObjectNode jsonToSend = requesterUserCurrentState.toJson(null);

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

          boolean isDelete = action == ProvisioningObjectChangeAction.delete;

          // built-ins
          if ("firstName".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("first_name");
            } else {
              jsonToSend.put("first_name", grouperRequesterUser.getFirstName());
            }
          } else if ("lastName".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("last_name");
            } else {
              jsonToSend.put("last_name", grouperRequesterUser.getLastName());
            }
          } else if ("email".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("primary_email");
            } else {
              jsonToSend.put("primary_email", grouperRequesterUser.getEmail());
            }
          } else if ("isAgent".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("is_agent");
            } else if (grouperRequesterUser.getIsAgent() != null) {
              jsonToSend.put("is_agent", grouperRequesterUser.getIsAgent().booleanValue());
            }
          } else if ("jobTitle".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("job_title");
            } else {
              jsonToSend.put("job_title", grouperRequesterUser.getJobTitle());
            }
          } else if ("workPhoneNumber".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("work_phone_number");
            } else {
              jsonToSend.put("work_phone_number", grouperRequesterUser.getWorkPhoneNumber());
            }
          } else if ("departmentId".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("department_ids");
            } else if (grouperRequesterUser.getDepartmentId() != null) {
              ArrayNode departmentIdsArray = GrouperUtil.jsonJacksonArrayNode();
              departmentIdsArray.add(grouperRequesterUser.getDepartmentId().longValue());
              jsonToSend.set("department_ids", departmentIdsArray);
            }
          } else if ("reportingManagerId".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("reporting_manager_id");
            } else if (grouperRequesterUser.getReportingManagerId() != null) {
              jsonToSend.put("reporting_manager_id", grouperRequesterUser.getReportingManagerId().longValue());
            }
          } else if ("address".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("address");
            } else {
              jsonToSend.put("address", grouperRequesterUser.getAddress());
            }
          } else if ("active".equals(fieldName)) {
            if (isDelete) {
              jsonToSend.putNull("active");
            } else if (grouperRequesterUser.getActive() != null) {
              jsonToSend.put("active", grouperRequesterUser.getActive().booleanValue());
            }
          } else if (fieldName.startsWith(FreshRequesterUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX)) {

            // custom field in the fieldsToUpdate map is identified by customField_<name>
            String customFieldName = fieldName.substring(FreshRequesterUser.CUSTOM_FIELD_ATTRIBUTE_PREFIX.length());
            if (!StringUtils.isBlank(customFieldName)) {
              ObjectNode customFieldsNode = (ObjectNode) GrouperUtil.jsonJacksonGetNode(jsonToSend, "custom_fields");
              if (customFieldsNode == null) {
                customFieldsNode = GrouperUtil.jsonJacksonNode();
                jsonToSend.set("custom_fields", customFieldsNode);
              }

              if (isDelete) {
                customFieldsNode.putNull(customFieldName);
              } else {
                Object customValue = grouperRequesterUser.getCustomFields() == null ? null
                    : grouperRequesterUser.getCustomFields().get(customFieldName);

                if (customValue == null) {
                  // if update is requested but value is null, send null
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
            }
          }
        }
      }

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      executeMethod(debugMap, "PUT", configId, "api/v2/requesters/" + String.valueOf(userId),
          GrouperUtil.toSet(200, 201), new int[] { -1 }, jsonStringToSend, null, false, null);

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
      
      executeMethod(debugMap, "DELETE", configId, "api/v2/requester_groups/" + id,
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
      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
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
      
      while (lastPage != true) {
        
        JsonNode jsonNode = executeMethod(debugMap, "GET", configId, "api/v2/requester_groups",
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
   * Create a requester user in Freshservice
   * @param configId the id of the external system
   * @param grouperRequesterUser the user to be created in Freshservice
   * @return the created requester user with assigned id
   */
  public static FreshRequesterUser createRequesterUser(String configId, FreshRequesterUser grouperRequesterUser) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createRequesterUser");

    long startTime = System.nanoTime();

    try {
      JsonNode jsonToSend = grouperRequesterUser.toJson(null);

      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "api/v2/requesters",
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
   * @return a list of all Freshservice requester users
   */
  public static List<FreshRequesterUser> retrieveRequesterUsers(String configId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    List<FreshRequesterUser> results = new ArrayList<FreshRequesterUser>();
    
    debugMap.put("method", "retrieveRequesterUsers");
    
    long startTime = System.nanoTime();
    
    try {
      
      boolean lastPage = false;
      int page = 1;
      
      while (lastPage != true) {
        
        JsonNode jsonNode = executeMethod(debugMap, "GET", configId, "api/v2/requesters",
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
   * Get a Freshservice requester user by id
   * @param configId the id of the external system
   * @param id the id of the requester user to be retrieved
   * @return the requester user
   */
  public static FreshRequesterUser retrieveRequesterUser(String configId, Long id) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRequesterUser");

    long startTime = System.nanoTime();
    
    try {
      int[] returnCode = new int[] { -1 };

      String urlSuffix = "api/v2/requesters/" + String.valueOf(id);
      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), returnCode, null, null, false, null);

      if (returnCode[0] == 404) {
        return null;
      }

      JsonNode userNode = jsonNode.get("requester");
      if (userNode == null) {
        return null;
      }

      FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromJson(userNode);

      return grouperRequesterUser;

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }

  }
  
  /**
   * Get a Freshservice requester user by email address
   * @param configId the id of the external system
   * @param email the email address of the requester user to be retrieved
   * @return the requester user
   */
  public static FreshRequesterUser retrieveRequesterUserByEmail(String configId, String email) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveRequesterUserByEmail");

    long startTime = System.nanoTime();
    
    //Email param needs ' and ' at beginning and end    
    String paramEmail;
    
    if (!email.startsWith("'") || !email.endsWith("'")) {
      paramEmail = "'" + email + "'";
    } else {
      paramEmail = email;
    }
    
    try {
      int[] returnCode = new int[] { -1 };
      
      String urlSuffix = "api/v2/requesters";
      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200), returnCode, null, null, false, paramEmail);
      
      if (jsonNode == null) {
        return null;
      }
      
      ArrayNode requesterUserArray = (ArrayNode) jsonNode.get("requesters");
      
      if (requesterUserArray.size()==1) {
        JsonNode userNode = requesterUserArray.get(0);
        FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromJson(userNode);
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
      String addGroupId = String.valueOf(groupId);
      String addUserId = String.valueOf(userId);
      if (StringUtils.isBlank(addGroupId) || addGroupId == "null") {
        throw new RuntimeException("groupId is null");
      }
      if (StringUtils.isBlank(addUserId) || addUserId == "null") {
        throw new RuntimeException("userId is null");
      }  

      String urlPrefix = "api/v2/requester_groups/" + addGroupId + "/members/" + addUserId;

      Set<Integer> allowedReturnCodes = null;
      if ("POST".equals(httpMethod)) {
        allowedReturnCodes = GrouperUtil.toSet(200);
      } else if ("DELETE".equals(httpMethod)) {
        allowedReturnCodes = GrouperUtil.toSet(204, 404);
      } else {
        throw new RuntimeException("Unsupported httpMethod: " + httpMethod);
      }
      
      executeMethod(debugMap, httpMethod, configId, urlPrefix,
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
   * @return
   */
  public static List<FreshRequesterUser> retrieveMembershipsByGroup(String configId, Long groupId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    
    List<FreshRequesterUser> results = new ArrayList<FreshRequesterUser>();
    
    debugMap.put("method", "retrieveMembershipsByGroup");
    
    long startTime = System.nanoTime();
    
    try {
      
      boolean lastPage = false;
      int page = 1;
      
      while (lastPage != true) {
        
        JsonNode jsonNode = executeMethod(debugMap, "GET", configId, "api/v2/requester_groups/" + String.valueOf(groupId) + "/members",
            GrouperUtil.toSet(200), new int[] { -1 }, null, page, true, null);
        
        ArrayNode requesterUsersArray = (ArrayNode) jsonNode.get("requesters");
        
        if (requesterUsersArray.size() > 0) {
          for (int i = 0; i < (requesterUsersArray == null ? 0 : requesterUsersArray.size()); i++) {
            JsonNode groupNode = requesterUsersArray.get(i);
            FreshRequesterUser grouperRequesterUser = FreshRequesterUser.fromJson(groupNode);
            results.add(grouperRequesterUser);
          }
          page++;
          
        } else {
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

      executeMethod(debugMap, "DELETE", configId, "api/v2/requesters/" + id,
          GrouperUtil.toSet(204, 404), new int[] { -1 }, null, null, false, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      FreshRequesterLog.freshserviceLog(debugMap, startTime);
    }
  }
    
    
}
  