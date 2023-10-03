package edu.internet2.middleware.grouper.app.duo.role;

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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

public class GrouperDuoRoleApiCommands {
  
  public static void main(String[] args) {

    GrouperSession.startRootSession();
    
//    String configId = "duoTest";
//    
//    GrouperHttpClientLog grouperHttpCallLog = new GrouperHttpClientLog();
//    GrouperHttpClient.logStart(grouperHttpCallLog);

//    List<GrouperDuoRoleUser> duoUsers = retrieveDuoAdministrators("duoTest");
//    System.out.println("duo users size = "+duoUsers.size());

//    GrouperDuoRoleUser grouperDuoRoleUser = new GrouperDuoRoleUser();
//    grouperDuoRoleUser.setEmail("kwilso@isc.upenn.edu");
//    grouperDuoRoleUser.setName("Kate Wilson");
//    
//    createDuoAdministrator("duoTest", grouperDuoRoleUser, "Help Desk");
//    
//    System.out.println(GrouperHttpClient.logEnd());

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_provisioner_full_duoAdminRoleTest");
    
//    for (GrouperDuoRoleUser grouperDuoUser: duoUsers) {
//      List<GrouperDuoRole> groupsByUser = retrieveDuoGroupsByUser(configId, grouperDuoUser.getId());
//      System.out.println("for user: "+grouperDuoUser.getUserName()+ " found: "+groupsByUser.size()+ " groups");
//    }
    
//    GrouperDuoRoleUser userByName = retrieveDuoAdministrator(configId, "mchyzer");
//    System.out.println("userByName: "+userByName);
    
  }

  public static void main1(String[] args) {

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
    
    
    
    
    // delete the user now
//    deleteDuoUser("duo1", duoUserCreated.getId());
    
  }

  private static JsonNode executeMethod(Map<String, Object> debugMap,
      String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, 
      Map<String, String> params,
      String body, String version) {

    GrouperHttpClient grouperHttpCall = new GrouperHttpClient();
    
    grouperHttpCall.assignDoNotLogHeaders(DuoRoleMockServiceHandler.doNotLogHeaders).assignDoNotLogParameters(DuoRoleMockServiceHandler.doNotLogParameters);
    
    String proxyUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.duoConnector." + configId + ".proxyUrl");
    String proxyType = GrouperConfig.retrieveConfig().propertyValueString("grouper.duoConnector." + configId + ".proxyType");
    
    grouperHttpCall.assignProxyUrl(proxyUrl);
    grouperHttpCall.assignProxyType(proxyType);

    String url = null;
    
    boolean useSsl = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.duoConnector."+configId+".useSsl", true);
    String adminDomainName = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.duoConnector."+configId+".adminDomainName");
    if (useSsl) {
      url = "https";
    } else {
      url = "http";
    }
    
    url = url + "://" + adminDomainName + "/admin/"+version;
    
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
    
    String hmacSource = dateHeaderValue + "\n" + httpMethodName + "\n" + adminDomainName + "\n" + "/admin/"+version+urlSuffix + "\n" + paramsLine;
    
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
    return GrouperUtil.escapeUrlEncode(param).replace("+", "%20").replace(":", "%3A");
  }

  /**
   * create a user
   * @param grouperDuoUser
   * @return the result
   */
  public static GrouperDuoRoleUser createDuoAdministrator(String configId,
      GrouperDuoRoleUser grouperDuoUser, String roleName) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createDuoAdministrator");

    long startTime = System.nanoTime();

    try {

      Map<String, String> params = GrouperUtil.toMap("name", StringUtils.defaultString(grouperDuoUser.getName()),
          "email", StringUtils.defaultString(grouperDuoUser.getEmail()),
          "role", roleName);
      if (grouperDuoUser.getSendEmail() == 1) {
        params.put("send_email", "1");
      }

      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/admins",
          GrouperUtil.toSet(200), new int[] { -1 }, params, null, "v1");
      
      JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");

      GrouperDuoRoleUser grouperDuoUserResult = GrouperDuoRoleUser.fromJson(userNode);

      return grouperDuoUserResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoRoleLog.duoLog(debugMap, startTime);
    }

  }
  
  /**
   * update a user
   * @param grouperDuoUser
   * @param fieldsToUpdate
   * @return the result
   */
  public static GrouperDuoRoleUser updateDuoAdministrator(String configId,
      GrouperDuoRoleUser grouperDuoUser, Set<String> fieldsToUpdate) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "updateDuoUser");

    long startTime = System.nanoTime();

    try {

      String id = grouperDuoUser.getId();
      
      if (StringUtils.isBlank(id)) {
        throw new RuntimeException("id is null: " + grouperDuoUser);
      }

      Map<String, String> params = GrouperUtil.toMap();
      
      if (fieldsToUpdate == null || fieldsToUpdate.contains("name")) {
        params.put("name", StringUtils.defaultString(grouperDuoUser.getName()));
      }
      
      if (fieldsToUpdate == null || fieldsToUpdate.contains("role")) {
        params.put("role", StringUtils.defaultString(grouperDuoUser.getRole()));
      }
      
//      if (fieldsToUpdate == null || fieldsToUpdate.contains("realname")) {
//        params.put("realname", StringUtils.defaultString(grouperDuoUser.getRealName()));
//      }
//      
//      if (fieldsToUpdate == null || fieldsToUpdate.contains("email")) {
//        params.put("email", StringUtils.defaultString(grouperDuoUser.getEmail()));
//      }
//      
//      if (fieldsToUpdate == null || fieldsToUpdate.contains("username")) {
//        params.put("username", StringUtils.defaultString(grouperDuoUser.getUserName()));
//      }
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/admins/" + id,
          GrouperUtil.toSet(200), new int[] { -1 }, params, null, "v1");

      JsonNode groupNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
      
      GrouperDuoRoleUser grouperDuoUserResult = GrouperDuoRoleUser.fromJson(groupNode);

      return grouperDuoUserResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoRoleLog.duoLog(debugMap, startTime);
    }

  }
  
  public static List<GrouperDuoRoleUser> retrieveDuoAdministrators(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoAdministrators");

    long startTime = System.nanoTime();

    try {

      List<GrouperDuoRoleUser> results = new ArrayList<GrouperDuoRoleUser>();

      int limit = 100;
      int offset = 0;
      
      while (offset >= 0) {

        Map<String, String> params = GrouperUtil.toMap("limit", String.valueOf(limit), "offset", String.valueOf(offset));
        
        JsonNode jsonNode = executeMethod(debugMap, "GET", configId, "/admins",
            GrouperUtil.toSet(200, 404), new int[] { -1 }, params, null, "v1");
        
        ArrayNode usersArray = (ArrayNode) jsonNode.get("response");

        for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
          JsonNode userNode = usersArray.get(i);
          GrouperDuoRoleUser grouperDuoUser = GrouperDuoRoleUser.fromJson(userNode);
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
      GrouperDuoRoleLog.duoLog(debugMap, startTime);
    }

  }
  
  public static GrouperDuoRoleUser retrieveDuoAdministrator(String configId, String id) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveDuoAdministrator(String, String)");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = "/admins/" + id;

      int[] returnCode = new int[] { -1 };
      JsonNode jsonNode = executeMethod(debugMap, "GET", configId, urlSuffix,
          GrouperUtil.toSet(200, 404), returnCode, null, null, "v1");
      
      if (returnCode[0] == 404) {
        return null;
      }
      
      JsonNode userNode = GrouperUtil.jsonJacksonGetNode(jsonNode, "response");
      if (userNode == null) {
        return null;
      }
      GrouperDuoRoleUser grouperDuoUser = GrouperDuoRoleUser.fromJson(userNode);

      return grouperDuoUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoRoleLog.duoLog(debugMap, startTime);
    }

  }
  
  
  public static void deleteDuoAdministrator(String configId, String adminId) {
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "deleteDuoAdministrator");

    long startTime = System.nanoTime();

    try {
    
      if (StringUtils.isBlank(adminId)) {
        throw new RuntimeException("adminId is null");
      }
    
      executeMethod(debugMap, "DELETE", configId, "/admins/" + adminId,
          GrouperUtil.toSet(200, 404), new int[] { -1 }, null, null, "v1");

    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperDuoRoleLog.duoLog(debugMap, startTime);
    }
  }

}
