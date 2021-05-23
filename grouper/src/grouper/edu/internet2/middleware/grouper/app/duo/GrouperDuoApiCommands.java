package edu.internet2.middleware.grouper.app.duo;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperDuoApiCommands {
  
  public static void main(String[] args) {
    
    List<GrouperDuoGroup> duoGroups = retrieveDuoGroups("duo1");
    System.out.println("duo groups size = "+duoGroups.size());
    
    List<GrouperDuoUser> duoUsers = retrieveDuoUsers("duo1");
    System.out.println("duo users size = "+duoUsers.size());
    
    for (GrouperDuoUser grouperDuoUser: duoUsers) {
      List<GrouperDuoGroup> groupsByUser = retrieveDuoGroupsByUser("duo1", grouperDuoUser.getId());
      System.out.println("for user: "+grouperDuoUser.getUserName()+ " found: "+groupsByUser.size()+ " groups");
    }
    
    GrouperDuoUser userByName = retrieveDuoUserByName("duo1", "vivek");
    System.out.println("userByName: "+userByName);
    
  }

  public static void main1(String[] args) {

//    AzureMockServiceHandler.dropAzureMockTables();
//    AzureMockServiceHandler.ensureAzureMockTables();
    
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
//        "grouper.azureConnector.azure1.loginEndpoint",
//        "http://localhost/f3/login.microsoftonline.com/");
//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
//        "grouper.azureConnector.azure1.resourceEndpoint",
//        "http://localhost/f3/graph.microsoft.com/v1.0/");

//    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap()
//      .put("grouper.duoConnector.duo1.resourceEndpoint","http://localhost:8080/grouper/mockServices/duo");

//    GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("duoProvisionerConfigId");
//    GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);

    //GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning("azure1");

    //  List<GrouperAzureGroup> grouperAzureGroups = retrieveAzureGroups("azure1");
    //  
    //  for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
    //    System.out.println(grouperAzureGroup);
    //  }
    
    //  GrouperAzureGroup grouperAzureGroup = retrieveAzureGroup("azure1", "id", "1153755cfa554297a29cfc332e1bef9f");
    //  GrouperAzureGroup grouperAzureGroup = retrieveAzureGroup("azure1", "displayName", "myDisplayName2");
    //  System.out.println(grouperAzureGroup);

//    for (int i=0;i<5;i++) {
//      {
//        GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
//        grouperAzureUser.setAccountEnabled(true);
//        grouperAzureUser.setDisplayName("myDispName" + i);
//        grouperAzureUser.setId(GrouperUuid.getUuid());
//        grouperAzureUser.setMailNickname("a" + i + "@b.c");
//        grouperAzureUser.setOnPremisesImmutableId((12345678+i) + "");
//        grouperAzureUser.setUserPrincipalName("jsmith" + 1);
//        HibernateSession.byObjectStatic().save(grouperAzureUser);
//        createAzureMembership("azure1", "dcba5d8d7986432db23a0342887e8fba", grouperAzureUser.getId());
//      }
//      
//    }
    
    //  Set<String> groupIds = retrieveAzureUserGroups("azure1", "84ec56bad4da4430ae5f2998ea283dfc");
    //  for (String groupId : groupIds) {
    //    System.out.println(groupId);
    //  }

    //    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("azureGetMembershipPagingSize", "2");
    //
    //    Set<String> userIds = retrieveAzureGroupMembers("azure1", "dcba5d8d7986432db23a0342887e8fba");
    //    for (String userId : userIds) {
    //      System.out.println(userId);
    //    }
        
    
    //  {
    //    GrouperAzureUser grouperAzureUser = new GrouperAzureUser();
    //    grouperAzureUser.setAccountEnabled(true);
    //    grouperAzureUser.setDisplayName("myDispName2");
    //    grouperAzureUser.setId(GrouperUuid.getUuid());
    //    grouperAzureUser.setMailNickname("a@b.d");
    //    grouperAzureUser.setOnPremisesImmutableId("12345679");
    //    grouperAzureUser.setUserPrincipalName("kjohnson");
    //    HibernateSession.byObjectStatic().save(grouperAzureUser);
    //  }
    
    //  List<GrouperAzureUser> grouperAzureUsers = retrieveAzureUsers("azure1");
    //
    //  for (GrouperAzureUser grouperAzureUser : grouperAzureUsers) {
    //    System.out.println(grouperAzureUser);
    //  }
    
    //GrouperAzureUser grouperAzureUser = retrieveAzureUser("azure1", "userPrincipalName", "jsmith");
    //GrouperAzureUser grouperAzureUser = retrieveAzureUser("azure1", "displayName", "myDispName");
    //System.out.println(grouperAzureUser);
    
    //  createAzureMembership("azure1", "dcba5d8d7986432db23a0342887e8fba", "b1dda78d8d42461a93f8b471f26b682e");
    
    //createAzureMemberships("azure1", "dcba5d8d7986432db23a0342887e8fba", GrouperUtil.toSet("1db63cda166a4640b9ef1a0808f90873", "b1dda78d8d42461a93f8b471f26b682e"));
    
    //  deleteAzureMembership("azure1", "dcba5d8d7986432db23a0342887e8fba", "b1dda78d8d42461a93f8b471f26b682e");
    
    //Create, Update, Retrieve groups
    GrouperDuoGroup grouperDuoGroup = new GrouperDuoGroup();
    grouperDuoGroup.setDesc("test Description");
    grouperDuoGroup.setName("test Name");
    GrouperDuoGroup duoGroupCreated = createDuoGroup("duo1", grouperDuoGroup);

    GrouperDuoGroup grouperDuoGroupToUpdate = new GrouperDuoGroup();
    grouperDuoGroupToUpdate.setGroup_id(duoGroupCreated.getGroup_id());
    grouperDuoGroupToUpdate.setName("test name 2");
    grouperDuoGroupToUpdate.setDesc("test desc 2");
    updateDuoGroup("duo1", grouperDuoGroupToUpdate);
    
    List<GrouperDuoGroup> duoGroups = retrieveDuoGroups("duo1");
    boolean groupFound = false;
    for (GrouperDuoGroup duoGroup : duoGroups) {
      System.out.println(duoGroup.getGroup_id());
      if (StringUtils.equals(duoGroup.getGroup_id(), duoGroupCreated.getGroup_id())) {
        groupFound = true;
      }
    }
    
    if (!groupFound) {
      throw new RuntimeException("why duo group was not found!!!");
    }
    
    
    GrouperDuoGroup duoGroup = retrieveDuoGroup("duo1", duoGroupCreated.getGroup_id());
    System.out.println("Name: "+duoGroup.getName());
    System.out.println("Desc: "+ duoGroup.getDesc());
    
    
    
    //Create, Update, Retrieve users
    GrouperDuoUser grouperDuoUser = new GrouperDuoUser();
    grouperDuoUser.setEmail("test@example.com");
    grouperDuoUser.setFirstName("first name");
    grouperDuoUser.setLastName("last name");
    grouperDuoUser.setUserName("user name");
    grouperDuoUser.setRealName("real name");
    GrouperDuoUser duoUserCreated = createDuoUser("duo1", grouperDuoUser);
    System.out.println(duoUserCreated);
    
    GrouperDuoUser grouperDuoUserToUpdate = new GrouperDuoUser();
    grouperDuoUserToUpdate.setEmail("test2@example.com");
    grouperDuoUserToUpdate.setFirstName("first name2");
    grouperDuoUserToUpdate.setLastName("last name2");
    grouperDuoUserToUpdate.setRealName("real name2");
    grouperDuoUserToUpdate.setUserName("user name");
    grouperDuoUserToUpdate.setId(duoUserCreated.getId());
    updateDuoUser("duo1", grouperDuoUserToUpdate);
    
    List<GrouperDuoUser> duoUsers = retrieveDuoUsers("duo1");
    boolean userFound = false;
    for (GrouperDuoUser duoUser : duoUsers) {
      System.out.println(duoUser.getId());
      if (StringUtils.equals(duoUser.getId(), duoUserCreated.getId())) {
        userFound = true;
      }
    }
    
    if (!userFound) {
      throw new RuntimeException("why duo user was not found!!!");
    }
    
    // now associate group to user
    associateUserToGroup("duo1", duoUserCreated.getId(), duoGroupCreated.getGroup_id());
    
    // check if the group is really associated
    GrouperDuoUser duoUser = retrieveDuoUser("duo1", duoUserCreated.getId());
    GrouperDuoGroup attachedGroup = duoUser.getGroups().iterator().next();
    if (!StringUtils.equals(attachedGroup.getGroup_id(), duoGroupCreated.getGroup_id())) {
     throw new RuntimeException("correct group not associated"); 
    }
    
    // now disassociate
    disassociateUserFromGroup("duo1", duoUserCreated.getId(), duoGroupCreated.getGroup_id());
    
    // delete the group now
    deleteDuoGroup("duo1", duoGroupCreated.getGroup_id());
    
    // delete the user now
    deleteDuoUser("duo1", duoUserCreated.getId());
    
  }

  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, 
      Map<String, String> params,
      String body) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    
    String url = null;
    
    boolean useSsl = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.duoConnector."+configId+".useSsl", true);
    String adminDomainName = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector."+configId+".adminDomainName");
    if (useSsl) {
      url = "https";
    } else {
      url = "http";
    }
    
    url = url + "://" + adminDomainName + "/admin/v1";
    
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    // in a nextLink, url is specified, so it might not have a prefix of the resourceEndpoint
    if (!urlSuffix.startsWith("http")) {
      url += (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    } else {
      url = urlSuffix;
    }
    
    String paramsLine = "";
    
    if (GrouperUtil.length(params) > 0) {
      
      params = new TreeMap<String, String>(params);
      
      for (String paramName: params.keySet()) {
        if (StringUtils.isNotBlank(paramsLine)) {
          paramsLine += "&";
        }
//        paramsLine = paramsLine + GrouperUtil.escapeUrlEncode(paramName) + "="+ GrouperUtil.escapeUrlEncode(params.get(paramName));
        String paramValue = params.get(paramName);
        try {
          paramsLine = paramsLine + escapeUrlEncode(paramName) + "=" + escapeUrlEncode(paramValue);
        } catch (RuntimeException e) {
          GrouperUtil.injectInException(e, "paramName: '" + paramName + "', paramValue: '" + paramValue + "'");
          throw e;
        }
      }
    }
    
    if (httpMethodName.equals("GET") || httpMethodName.equals("DELETE")) {
      GrouperUtil.assertion(!url.contains("?"), "parameters shouldn't be in url yet");
      
      if (StringUtils.isNotBlank(paramsLine)) {
        url += "?"+paramsLine;
      }
      
    } else {
      if (StringUtils.isNotBlank(paramsLine)) {
        GrouperUtil.assertion(StringUtils.isBlank(body), "body must be blank if passing in params.");
        body = paramsLine;
      }
    }

    debugMap.put("url", url);

    grouperHttpCall.assignUrl(url);
    grouperHttpCall.assignGrouperHttpMethod(httpMethodName);
    grouperHttpCall.addHeader("Content-Type", "application/x-www-form-urlencoded");
    
    //String rfcDate = "Sat, 13 Mar 2010 11:29:05 -0800";
    String pattern = "EEE, dd MMM yyyy HH:mm:ss Z";
    SimpleDateFormat format = new SimpleDateFormat(pattern);
    format.setTimeZone(TimeZone.getTimeZone("UTC"));
    String dateHeaderValue = format.format(new Date());
    grouperHttpCall.addHeader("Date", dateHeaderValue);
    
    System.out.println(paramsLine);
    String hmacSource = dateHeaderValue + "\n" + httpMethodName + "\n" + adminDomainName + "\n" + "/admin/v1"+urlSuffix + "\n" + paramsLine;
    
    System.out.println("hmacSource in api commands: \n"+hmacSource);
    
    String adminSecretKey = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector."+configId+".adminSecretKey");
    String adminIntegrationKey = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector."+configId+".adminIntegrationKey");
    
    String hmac = new HmacUtils(HmacAlgorithms.HMAC_SHA_1, adminSecretKey).hmacHex(hmacSource);
    
    String bearerToken = adminIntegrationKey + ":" + hmac;
    
    String credentials = "";
    try {
      credentials = new String(Base64.getEncoder().encode(bearerToken.getBytes()), "UTF-8");
    } catch (UnsupportedEncodingException e1) {
      throw new RuntimeException(e1);
    }
    grouperHttpCall.addHeader("Authorization", "Basic " + credentials);
    
    grouperHttpCall.assignBody(body);
    grouperHttpCall.executeRequest();
    
    int code = -1;
    String json = null;

    try {
      code = grouperHttpCall.getResponseCode();
      returnCode[0] = code;
      json = grouperHttpCall.getResponseBody();
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

  /**
   * encode URL param
   * @param param
   * @return the value
   */
  public static String escapeUrlEncode(String param) {
    return GrouperUtil.escapeUrlEncode(param).replace("+", "%20");
  }

  /**
   * create a group
   * @param grouperDuoGroup
   * @return the result
   */
  public static GrouperDuoGroup createDuoGroup(String configId,
      GrouperDuoGroup grouperDuoGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createDuoGroup");

    long startTime = System.nanoTime();

    try {


      Map<String, String> params = GrouperUtil.toMap("name", 
          StringUtils.defaultString(grouperDuoGroup.getName()), "desc", StringUtils.defaultString(grouperDuoGroup.getDesc()));
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/groups",
          GrouperUtil.toSet(200), new int[] { -1 }, params, null);
      
      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");

      GrouperDuoGroup grouperDuoGroupResult = GrouperDuoGroup.fromJson(groupNode);

      return grouperDuoGroupResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }

  /**
   * update a group
   * @param grouperDuoGroup
   * @return the result
   */
  public static GrouperDuoGroup updateDuoGroup(String configId,
      GrouperDuoGroup grouperDuoGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateDuoGroup");

    long startTime = System.nanoTime();

    try {

      String id = grouperDuoGroup.getGroup_id();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperDuoGroup);
      }

      Map<String, String> params = GrouperUtil.toMap("name", 
          StringUtils.defaultString(grouperDuoGroup.getName()), "desc", StringUtils.defaultString(grouperDuoGroup.getDesc()));
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/groups/" + id,
          GrouperUtil.toSet(200), new int[] { -1 }, params, null);

      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
      
      GrouperDuoGroup grouperDuoGroupResult = GrouperDuoGroup.fromJson(groupNode);

      return grouperDuoGroupResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }

  public static void deleteDuoGroup(String configId,
      String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteDuoGroup");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/groups/" + groupId,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }


  public static List<GrouperDuoGroup> retrieveDuoGroups(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoGroups");

    long startTime = System.nanoTime();
    
    try {
      
      List<GrouperDuoGroup> results = new ArrayList<GrouperDuoGroup>();
      
      int limit = 100;
      int offset = 0;
      
      while (offset >= 0) {

        Map<String, String> params = GrouperUtil.toMap("limit", String.valueOf(limit), "offset", String.valueOf(offset));
        
        JsonNode jsonNode = executeMethod(debugMap, "GET", configId, "/groups",
            GrouperUtil.toSet(200, 404), new int[] { -1 }, params, null);
        
        ArrayNode groupsArray = (ArrayNode) jsonNode.get("response");

        for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
          JsonNode groupNode = groupsArray.get(i);
          GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromJson(groupNode);
          results.add(grouperDuoGroup);
        }
        
        JsonNode metadata = jsonNode.get("metadata");
        
        if (metadata != null && metadata.get("next_offset") != null) {
          offset = metadata.get("next_offset").asInt();
        } else {
          offset = -1;
        }
        
      }
      
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  public static List<GrouperDuoGroup> retrieveDuoGroupsByUser(String configId, String userId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoGroupsByUser");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "/users/" + userId;

      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null);
      
      JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
      if (userNode == null) {
        return new ArrayList<GrouperDuoGroup>();
      }
      GrouperDuoUser grouperDuoUser = GrouperDuoUser.fromJson(userNode);

      return new ArrayList<GrouperDuoGroup>(grouperDuoUser.getGroups());
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }

  /**
   * @param configId
   * @param group id
   * @return the user
   */
  public static GrouperDuoGroup retrieveDuoGroup(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoGroup");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "/groups/" + id;

      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null);
      
      //lets get the group node
      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
      if (groupNode == null) {
        return null;
      }
      GrouperDuoGroup grouperDuoGroup = GrouperDuoGroup.fromJson(groupNode);

      return grouperDuoGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  
  /**
   * create a user
   * @param grouperDuoUser
   * @return the result
   */
  public static GrouperDuoUser createDuoUser(String configId,
      GrouperDuoUser grouperDuoUser) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createDuoUser");

    long startTime = System.nanoTime();

    try {

      Map<String, String> params = GrouperUtil.toMap("firstname", 
          StringUtils.defaultString(grouperDuoUser.getFirstName()), "lastname", StringUtils.defaultString(grouperDuoUser.getLastName()),
          "realname", StringUtils.defaultString(grouperDuoUser.getRealName()), "email", StringUtils.defaultString(grouperDuoUser.getEmail()),
          "username", StringUtils.defaultString(grouperDuoUser.getUserName()));

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/users",
          GrouperUtil.toSet(200), new int[] { -1 }, params, null);
      
      JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");

      GrouperDuoUser grouperDuoUserResult = GrouperDuoUser.fromJson(userNode);

      return grouperDuoUserResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  /**
   * update a user
   * @param grouperDuoUser
   * @return the result
   */
  public static GrouperDuoUser updateDuoUser(String configId,
      GrouperDuoUser grouperDuoUser) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateDuoUser");

    long startTime = System.nanoTime();

    try {

      String id = grouperDuoUser.getId();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperDuoUser);
      }

      Map<String, String> params = GrouperUtil.toMap("firstname", 
          StringUtils.defaultString(grouperDuoUser.getFirstName()), "lastname", StringUtils.defaultString(grouperDuoUser.getLastName()),
          "realname", StringUtils.defaultString(grouperDuoUser.getRealName()), "email", StringUtils.defaultString(grouperDuoUser.getEmail()),
          "username", StringUtils.defaultString(grouperDuoUser.getUserName()));
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/users/" + id,
          GrouperUtil.toSet(200), new int[] { -1 }, params, null);

      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
      
      GrouperDuoUser grouperDuoUserResult = GrouperDuoUser.fromJson(groupNode);

      return grouperDuoUserResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  public static List<GrouperDuoUser> retrieveDuoUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoUsers");

    long startTime = System.nanoTime();

    try {

      List<GrouperDuoUser> results = new ArrayList<GrouperDuoUser>();

      int limit = 100;
      int offset = 0;
      
      while (offset >= 0) {

        Map<String, String> params = GrouperUtil.toMap("limit", String.valueOf(limit), "offset", String.valueOf(offset));
        
        JsonNode jsonNode = executeMethod(debugMap, "GET", configId, "/users",
            GrouperUtil.toSet(200, 404), new int[] { -1 }, params, null);
        
        ArrayNode usersArray = (ArrayNode) jsonNode.get("response");

        for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
          JsonNode userNode = usersArray.get(i);
          GrouperDuoUser grouperDuoUser = GrouperDuoUser.fromJson(userNode);
          results.add(grouperDuoUser);
        }
        
        JsonNode metadata = jsonNode.get("metadata");
        
        if (metadata != null && metadata.get("next_offset") != null) {
          offset = metadata.get("next_offset").asInt();
        } else {
          offset = -1;
        }
        
      }
      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  public static GrouperDuoUser retrieveDuoUser(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoUser");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "/users/" + id;

      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null);
      
      JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
      if (userNode == null) {
        return null;
      }
      GrouperDuoUser grouperDuoUser = GrouperDuoUser.fromJson(userNode);

      return grouperDuoUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  /**
   * @param configId
   * @param username
   * @return
   */
  public static GrouperDuoUser retrieveDuoUserByName(String configId, String username) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoUserByName");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "/users";
      
      Map<String, String> params = GrouperUtil.toMap("username", StringUtils.defaultString(username));

      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, params, null);
      
      ArrayNode usersArray = (ArrayNode) jsonNode.get("response");
      
      if (usersArray == null || usersArray.size() == 0) {
        return null;
      } else if (usersArray.size() > 1) {
        throw new RuntimeException("How can there be more than one user with the same username in duo??");
      } else {
        JsonNode userNode = usersArray.get(0);
        GrouperDuoUser grouperDuoUser = GrouperDuoUser.fromJson(userNode);
        return grouperDuoUser;
      }
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  public static void deleteDuoUser(String configId, String userId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteDuoUser");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("id is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/users/" + userId,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }
  
  public static void associateUserToGroup(String configId, String userId, String groupId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "associateUserToGroup");

    long startTime = System.nanoTime();

    try {
      
      Map<String, String> params = GrouperUtil.toMap("group_id", StringUtils.defaultString(groupId));

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/users/"+userId+"/groups",
          GrouperUtil.toSet(200), new int[] { -1 }, params, null);
      
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }

  }
  
  public static void disassociateUserFromGroup(String configId, String userId, String groupId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "disassociateUserFromGroup");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(userId)) {
        throw new RuntimeException("userId is null");
      }
      
      if (StringUtils.isBlank(groupId)) {
        throw new RuntimeException("groupId is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/users/" + userId + "/groups/" + groupId,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null);

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoLog.duoLog(debugMap, startTime);
    }
  }

}
