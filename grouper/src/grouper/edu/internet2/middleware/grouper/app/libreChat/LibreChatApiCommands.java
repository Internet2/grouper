package edu.internet2.middleware.grouper.app.libreChat;

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
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * Static wrappers around the LibreChat admin REST API (LibreChat v0.8.5+), JSON in and out.
 *
 * <p>Authentication comes from a web service external system (grouper.wsBearerToken.&lt;id&gt;).
 * LibreChat has no API keys: the caller needs a short-lived LibreChat JWT for a user with the
 * ACCESS_ADMIN, READ_GROUPS, MANAGE_GROUPS and READ_USERS capabilities, so sites usually configure the
 * external system with httpAuthnType=dynamic and a GSH template that logs in (GRP-7593).</p>
 *
 * <p>Endpoints used (relative to the external system endpoint, e.g. https://chat.example.edu):</p>
 * <ul>
 *   <li>GET    /api/admin/users?limit=&amp;offset=               -- all users (paged, max 200)</li>
 *   <li>GET    /api/admin/users/search?q=&amp;limit=50           -- prefix search on name/email/username</li>
 *   <li>GET    /api/admin/groups?source=local&amp;limit=&amp;offset= -- groups (paged, max 200)</li>
 *   <li>GET    /api/admin/groups/{id}                          -- one group</li>
 *   <li>GET    /api/admin/groups/{id}/members?limit=&amp;offset=  -- members (paged)</li>
 *   <li>POST   /api/admin/groups                               -- create a group</li>
 *   <li>PATCH  /api/admin/groups/{id}                          -- update name / description</li>
 *   <li>DELETE /api/admin/groups/{id}                          -- delete a group (and its sharing grants)</li>
 *   <li>POST   /api/admin/groups/{id}/members {userId}         -- add one member (idempotent)</li>
 *   <li>DELETE /api/admin/groups/{id}/members/{userId}         -- remove one member (idempotent)</li>
 * </ul>
 *
 * GRP-7594
 */
public class LibreChatApiCommands {

  /** the admin API's max page size (MAX_PAGE_LIMIT) */
  public static final int PAGE_SIZE = 200;

  /** the user search endpoint's max results */
  public static final int SEARCH_LIMIT = 50;

  /**
   * Execute an HTTP method against the LibreChat admin API
   * @param debugMap map to accumulate debug info
   * @param debugLabel label for provisioner call stats
   * @param httpMethodName GET, POST, PATCH, DELETE
   * @param configId the web service external system config id
   * @param urlSuffix path and query after the endpoint, e.g. /api/admin/groups?limit=200
   * @param allowedReturnCodes acceptable HTTP status codes
   * @param returnCode single-element array to receive the actual status code
   * @param jsonBody request body, or null
   * @return the parsed response, or null if the body is blank
   */
  private static JsonNode executeMethod(Map<String, Object> debugMap, String debugLabel,
      String httpMethodName, String configId, String urlSuffix, Set<Integer> allowedReturnCodes,
      int[] returnCode, JsonNode jsonBody) {

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    // adds the authentication (e.g. a dynamic template's bearer token and cookies)
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(
        grouperHttpClient, configId, grouperLoaderConfig, debugMap);

    String url = grouperLoaderConfig.propertyValueStringRequired(
        "grouper.wsBearerToken." + configId + ".endpoint");
    url = GrouperUtil.stripLastSlashIfExists(url) + (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    debugMap.put("url", url);
    debugMap.put("method", httpMethodName);

    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod(httpMethodName);
    grouperHttpClient.addHeader("Accept", "application/json");
    if (jsonBody != null) {
      grouperHttpClient.addHeader("Content-Type", "application/json");
      grouperHttpClient.assignBody(GrouperUtil.jsonJacksonToString(jsonBody));
    }

    long httpCallStartMillis = System.currentTimeMillis();
    try {
      grouperHttpClient.executeRequest();
    } finally {
      GrouperProvisioner.incrementCommandsCallsStats(debugLabel, 1,
          System.currentTimeMillis() - httpCallStartMillis);
    }

    int code;
    String body;
    try {
      code = grouperHttpClient.getResponseCode();
      returnCode[0] = code;
      body = grouperHttpClient.getResponseBody();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + url + "'", e);
    }
    debugMap.put("responseCode", code);

    if (!allowedReturnCodes.contains(code)) {
      // the body is LibreChat's {"error": "..."}; it never contains the token
      throw new RuntimeException("Invalid return code '" + code + "', expecting: "
          + GrouperUtil.setToString(allowedReturnCodes) + ". '" + url + "' "
          + StringUtils.abbreviate(body, 1000));
    }

    if (StringUtils.isBlank(body)) {
      return null;
    }
    try {
      return GrouperUtil.jsonJacksonNode(body);
    } catch (RuntimeException re) {
      // e.g. an HTML login page from a proxy in front of LibreChat
      throw new RuntimeException("Response from '" + url + "' is not JSON (a proxy or login page?): "
          + StringUtils.abbreviate(body, 300), re);
    }
  }

  /**
   * @param jsonNode node
   * @param fieldName array field
   * @return the array elements (never null)
   */
  private static List<JsonNode> arrayElements(JsonNode jsonNode, String fieldName) {
    List<JsonNode> result = new ArrayList<JsonNode>();
    JsonNode arrayNode = jsonNode == null ? null : jsonNode.get(fieldName);
    if (arrayNode != null && arrayNode.isArray()) {
      for (int i = 0; i < arrayNode.size(); i++) {
        result.add(arrayNode.get(i));
      }
    }
    return result;
  }

  /**
   * GET a paged list endpoint until every item is read
   * @param configId external system config id
   * @param debugLabel label for stats
   * @param path path without paging params, may have a query string
   * @param arrayFieldName "users", "groups" or "members"
   * @return all items
   */
  private static List<JsonNode> retrieveAllPages(String configId, String debugLabel, String path, String arrayFieldName) {
    List<JsonNode> result = new ArrayList<JsonNode>();
    int offset = 0;
    // stop after a generous number of pages so a misbehaving server cannot loop forever
    for (int page = 0; page < 10000; page++) {
      Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
      long startTime = System.nanoTime();
      try {
        String urlSuffix = path + (path.contains("?") ? "&" : "?") + "limit=" + PAGE_SIZE + "&offset=" + offset;
        JsonNode jsonNode = executeMethod(debugMap, debugLabel, "GET", configId, urlSuffix,
            GrouperUtil.toSet(200), new int[] { -1 }, null);
        List<JsonNode> items = arrayElements(jsonNode, arrayFieldName);
        result.addAll(items);
        Long total = jsonNode == null ? null : GrouperUtil.jsonJacksonGetLong(jsonNode, "total");
        offset += PAGE_SIZE;
        debugMap.put("count", items.size());
        if (items.isEmpty() || total == null || offset >= total) {
          return result;
        }
      } finally {
        LibreChatLog.libreChatLog(debugMap, startTime);
      }
    }
    throw new RuntimeException("Too many pages from " + path);
  }

  /**
   * @param configId external system config id
   * @return every LibreChat user
   */
  public static List<LibreChatUser> retrieveUsers(String configId) {
    List<LibreChatUser> result = new ArrayList<LibreChatUser>();
    for (JsonNode userNode : retrieveAllPages(configId, "retrieveUsers", "/api/admin/users", "users")) {
      LibreChatUser user = LibreChatUser.fromJson(userNode);
      // sync-back: mirror every user read (GRP-7595)
      LibreChatProvisioningTargetNativeSync.captureUserFromCurrentProvisioner(user);
      result.add(user);
    }
    return result;
  }

  /**
   * Find a user by exact username, case-insensitive.  The search endpoint is a prefix match on name,
   * email and username, so the results are filtered here.
   * @param configId external system config id
   * @param username e.g. jdoe@upenn.edu
   * @return the user or null
   */
  public static LibreChatUser retrieveUserByUsername(String configId, String username) {
    // the search requires at least 2 characters
    if (StringUtils.length(StringUtils.trim(username)) < 2) {
      return null;
    }
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    debugMap.put("method", "retrieveUserByUsername");
    long startTime = System.nanoTime();
    try {
      JsonNode jsonNode = executeMethod(debugMap, "retrieveUserByUsername", "GET", configId,
          "/api/admin/users/search?q=" + GrouperUtil.escapeUrlEncode(username.trim()) + "&limit=" + SEARCH_LIMIT,
          GrouperUtil.toSet(200), new int[] { -1 }, null);
      for (JsonNode userNode : arrayElements(jsonNode, "users")) {
        LibreChatUser user = LibreChatUser.fromJson(userNode);
        if (StringUtils.equalsIgnoreCase(StringUtils.trim(user.getUsername()), username.trim())) {
          LibreChatProvisioningTargetNativeSync.captureUserFromCurrentProvisioner(user);
          return user;
        }
      }
      return null;
    } finally {
      LibreChatLog.libreChatLog(debugMap, startTime);
    }
  }

  /**
   * Find a user by id.  The admin API has no get-user-by-id, so this reads the user list.
   * @param configId external system config id
   * @param userId LibreChat user _id
   * @return the user or null
   */
  public static LibreChatUser retrieveUserById(String configId, String userId) {
    if (StringUtils.isBlank(userId)) {
      return null;
    }
    for (LibreChatUser user : retrieveUsers(configId)) {
      if (StringUtils.equals(userId, user.getId())) {
        return user;
      }
    }
    return null;
  }

  /**
   * @param configId external system config id
   * @param prefix idOnTheSource prefix of groups this provisioner manages
   * @return the local groups whose idOnTheSource starts with the prefix (all others are never touched)
   */
  public static List<LibreChatGroup> retrieveManagedGroups(String configId, String prefix) {
    List<LibreChatGroup> result = new ArrayList<LibreChatGroup>();
    for (JsonNode groupNode : retrieveAllPages(configId, "retrieveGroups", "/api/admin/groups?source=local", "groups")) {
      LibreChatGroup group = LibreChatGroup.fromJson(groupNode);
      // sync-back: mirror every local group read, before the managed filter, with its raw idOnTheSource (GRP-7595)
      LibreChatProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(group);
      if (group.isManaged(prefix)) {
        result.add(group);
      }
    }
    return result;
  }

  /**
   * @param configId external system config id
   * @param groupId LibreChat group _id
   * @return the group or null if not found
   */
  public static LibreChatGroup retrieveGroup(String configId, String groupId) {
    if (!isObjectId(groupId)) {
      return null;
    }
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTime = System.nanoTime();
    try {
      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "retrieveGroup", "GET", configId,
          "/api/admin/groups/" + groupId, GrouperUtil.toSet(200, 404), returnCode, null);
      LibreChatGroup group = returnCode[0] == 404 ? null : LibreChatGroup.fromJson(jsonNode);
      LibreChatProvisioningTargetNativeSync.captureGroupFromCurrentProvisioner(group);
      return group;
    } finally {
      LibreChatLog.libreChatLog(debugMap, startTime);
    }
  }

  /**
   * @param configId external system config id
   * @param groupId LibreChat group _id
   * @return the member user ids of the group
   */
  public static List<String> retrieveGroupMemberIds(String configId, String groupId) {
    List<String> result = new ArrayList<String>();
    // the id can come from a LibreChat response, so check it before it goes into the URL path
    for (JsonNode memberNode : retrieveAllPages(configId, "retrieveGroupMembers", "/api/admin/groups/" + requireObjectId(groupId) + "/members", "members")) {
      String userId = GrouperUtil.jsonJacksonGetString(memberNode, "userId");
      if (!StringUtils.isBlank(userId)) {
        result.add(userId);
      }
    }
    // sync-back: the group's full member list (GRP-7595)
    LibreChatProvisioningTargetNativeSync.captureMembershipsForGroupFromCurrentProvisioner(groupId, result);
    return result;
  }

  /**
   * Create a group (source local, with idOnTheSource)
   * @param configId external system config id
   * @param group name, description, idOnTheSource
   * @return the created group with its id
   */
  public static LibreChatGroup createGroup(String configId, LibreChatGroup group) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTime = System.nanoTime();
    try {
      ObjectNode body = GrouperUtil.jsonJacksonNode();
      GrouperUtil.jsonJacksonAssignString(body, "name", group.getName());
      if (!StringUtils.isBlank(group.getDescription())) {
        GrouperUtil.jsonJacksonAssignString(body, "description", group.getDescription());
      }
      GrouperUtil.jsonJacksonAssignString(body, "source", "local");
      GrouperUtil.jsonJacksonAssignString(body, "idOnTheSource", group.getIdOnTheSource());
      JsonNode jsonNode = executeMethod(debugMap, "createGroup", "POST", configId,
          "/api/admin/groups", GrouperUtil.toSet(200, 201), new int[] { -1 }, body);
      LibreChatGroup created = LibreChatGroup.fromJson(jsonNode);
      if (created == null || StringUtils.isBlank(created.getId())) {
        throw new RuntimeException("LibreChat did not return the new group id");
      }
      return created;
    } finally {
      LibreChatLog.libreChatLog(debugMap, startTime);
    }
  }

  /**
   * Update a group's name and/or description
   * @param configId external system config id
   * @param groupId LibreChat group _id
   * @param fieldToValue "name" and/or "description" to new values
   */
  public static void updateGroup(String configId, String groupId, Map<String, String> fieldToValue) {
    if (fieldToValue == null || fieldToValue.isEmpty()) {
      return;
    }
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTime = System.nanoTime();
    try {
      ObjectNode body = GrouperUtil.jsonJacksonNode();
      for (String fieldName : fieldToValue.keySet()) {
        // LibreChat requires a non-empty name, and accepts an empty description
        GrouperUtil.jsonJacksonAssignString(body, fieldName, StringUtils.defaultString(fieldToValue.get(fieldName)));
      }
      executeMethod(debugMap, "updateGroup", "PATCH", configId,
          "/api/admin/groups/" + requireObjectId(groupId), GrouperUtil.toSet(200), new int[] { -1 }, body);
    } finally {
      LibreChatLog.libreChatLog(debugMap, startTime);
    }
  }

  /**
   * Delete a group.  LibreChat also deletes the group's sharing grants.  Not found is fine.
   * @param configId external system config id
   * @param groupId LibreChat group _id
   */
  public static void deleteGroup(String configId, String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTime = System.nanoTime();
    try {
      executeMethod(debugMap, "deleteGroup", "DELETE", configId,
          "/api/admin/groups/" + requireObjectId(groupId), GrouperUtil.toSet(200, 204, 404), new int[] { -1 }, null);
    } finally {
      LibreChatLog.libreChatLog(debugMap, startTime);
    }
  }

  /**
   * Add one member (idempotent in LibreChat)
   * @param configId external system config id
   * @param groupId LibreChat group _id
   * @param userId LibreChat user _id
   */
  public static void addGroupMember(String configId, String groupId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTime = System.nanoTime();
    try {
      ObjectNode body = GrouperUtil.jsonJacksonNode();
      GrouperUtil.jsonJacksonAssignString(body, "userId", requireObjectId(userId));
      executeMethod(debugMap, "addGroupMember", "POST", configId,
          "/api/admin/groups/" + requireObjectId(groupId) + "/members", GrouperUtil.toSet(200, 201), new int[] { -1 }, body);
    } finally {
      LibreChatLog.libreChatLog(debugMap, startTime);
    }
  }

  /**
   * Remove one member (idempotent in LibreChat)
   * @param configId external system config id
   * @param groupId LibreChat group _id
   * @param userId LibreChat user _id
   */
  public static void removeGroupMember(String configId, String groupId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startTime = System.nanoTime();
    try {
      executeMethod(debugMap, "removeGroupMember", "DELETE", configId,
          "/api/admin/groups/" + requireObjectId(groupId) + "/members/" + requireObjectId(userId),
          GrouperUtil.toSet(200, 204), new int[] { -1 }, null);
    } finally {
      LibreChatLog.libreChatLog(debugMap, startTime);
    }
  }

  /**
   * @param id value
   * @return true if it looks like a Mongo ObjectId (24 hex characters)
   */
  public static boolean isObjectId(String id) {
    return id != null && id.matches("^[0-9a-fA-F]{24}$");
  }

  /**
   * Ids go into URL paths, so only allow ObjectIds
   * @param id value
   * @return the id
   */
  private static String requireObjectId(String id) {
    if (!isObjectId(id)) {
      throw new RuntimeException("Not a valid LibreChat id: '" + id + "'");
    }
    return id;
  }

  /**
   * JSON array of objects (used by the mock service)
   * @param items JSON objects
   * @return array node
   */
  static ArrayNode toArrayNode(List<ObjectNode> items) {
    ArrayNode arrayNode = GrouperUtil.jsonJacksonArrayNode();
    for (ObjectNode item : items) {
      arrayNode.add(item);
    }
    return arrayNode;
  }

}
