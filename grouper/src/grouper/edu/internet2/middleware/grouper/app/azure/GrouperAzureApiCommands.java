package edu.internet2.middleware.grouper.app.azure;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * This class interacts with the Microsoft Graph API.
 */
public class GrouperAzureApiCommands {

  public static void main(String[] args) {

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.azureConnector.azure1.loginEndpoint",
        "http://localhost/f3/login.microsoftonline.com/");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouper.azureConnector.azure1.resourceEndpoint",
        "http://localhost/f3/graph.microsoft.com/v1.0/");

    //GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("AzureProvA");
    //GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);

    //GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning("azure1");

    //    List<GrouperAzureGroup> grouperAzureGroups = retrieveAzureGroups("azure1");
    //    
    //    for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
    //      System.out.println(grouperAzureGroup);
    //    }

    List<GrouperAzureUser> grouperAzureUsers = retrieveAzureUsers("azure1");

    for (GrouperAzureUser grouperAzureUser : grouperAzureUsers) {
      System.out.println(grouperAzureUser);
    }

  }

  //    public GraphApiClient(String authUrlbase, String resourceUrlbase, String clientId, String clientSecret, String tenantId, String scope,
  //                          AzureGroupType azureGroupType,
  //                          AzureVisibility visibility,
  //                          String proxyType, String proxyHost, Integer proxyPort) {
  //        this.authUrlBase = authUrlbase;
  //        this.resourceUrlBase = resourceUrlbase;
  //        this.clientId = clientId;
  //        this.clientSecret = clientSecret;
  //        this.tenantId = tenantId;
  //        this.scope = scope;
  //        this.azureGroupType = azureGroupType;
  //        this.visibility = visibility;
  //
  //        final Proxy proxy;
  //
  //        if (proxyType == null) {
  //            proxy = null; // probably works too: Proxy.NO_PROXY
  //        } else if ("http".equals(proxyType)) {
  //            proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(proxyHost, proxyPort));
  //        } else if ("socks".equals(proxyType)) {
  //            proxy = new Proxy(Proxy.Type.SOCKS,new InetSocketAddress(proxyHost, proxyPort));
  //        } else {
  //            logger.warn("Unable to determine proxy type from '" + proxyType + "'; Valid proxy types for this consumer are 'http' or 'socks'");
  //            proxy = null;
  //        }
  //
  //        graphTokenHttpClient = buildBaseOkHttpClient(proxy);
  //        graphApiHttpClient = buildGraphOkHttpClient(graphTokenHttpClient);
  //
  //        RetrofitWrapper retrofit = buildRetrofit(graphApiHttpClient);
  //
  //        this.service = retrofit.create(Office365GraphApiService.class);
  //    }

  //    protected RetrofitWrapper buildRetrofit(OkHttpClient okHttpClient) {
  //        return new RetrofitWrapper((new Retrofit
  //                .Builder()
  //                .baseUrl(this.resourceUrlBase)
  //                .addConverterFactory(MoshiConverterFactory.create())
  //                .client(okHttpClient)
  //                .build()));
  //    }

  //    protected RetrofitWrapper buildRetrofitAuth(OkHttpClient okHttpClient) {
  //        return new RetrofitWrapper((new Retrofit
  //                .Builder()
  //                .baseUrl(this.authUrlBase + this.tenantId + "/")
  //                .addConverterFactory(MoshiConverterFactory.create())
  //                .client(okHttpClient)
  //                .build()));
  //    }

  //    protected OkHttpClient buildBaseOkHttpClient(Proxy proxy) {
  //        logger.trace("Building OkHttpClient: proxy=" + proxy);
  //
  //        OkHttpClient.Builder builder = new OkHttpClient.Builder();
  //
  //        if (proxy != null) {
  //            builder.proxy(proxy);
  //        }
  //
  //        return builder.build();
  //    }

  //    /*
  //     * customize a shared OkHttpClient instance, which will share the same connection pool, thread pools, and
  //     * configuration as the parent
  //     */
  //    protected OkHttpClient buildGraphOkHttpClient(OkHttpClient okHttpClient) {
  //        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor((msg) -> {
  //            logger.debug(msg);
  //        });
  //        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
  //        // strips out the Bearer token and replaces with U+2588 (a black square)
  //        loggingInterceptor.redactHeader("Authorization");
  //
  //        return okHttpClient.newBuilder()
  //                .addInterceptor(new Interceptor() {
  //                @Override
  //                public Response intercept(Chain chain) throws IOException {
  //                    Request request = chain.request().newBuilder().header("Authorization", "Bearer " + token).build();
  //                    return chain.proceed(request);
  //                }
  //            })
  //                .addInterceptor(loggingInterceptor)
  //                .build();
  //    }

  //    public String getToken() throws IOException {
  //        logger.debug("Token client ID: " + this.clientId);
  //        logger.debug("Token tenant ID: " + this.tenantId);
  //        RetrofitWrapper retrofit = buildRetrofitAuth(this.graphTokenHttpClient);
  //        Office365AuthApiService service = retrofit.create(Office365AuthApiService.class);
  //        retrofit2.Response<AzureGraphOAuthTokenInfo> response = service.getOauth2Token(
  //                "client_credentials",
  //                this.clientId,
  //                this.clientSecret,
  //                this.scope,
  //                "https://graph.microsoft.com")
  //                .execute();
  //        if (response.isSuccessful()) {
  //            AzureGraphOAuthTokenInfo info = response.body();
  //            logTokenInfo(info);
  //            return info.accessToken;
  //        } else {
  //            ResponseBody errorBody = response.errorBody();
  //            throw new IOException("error requesting token (" + response.code() + "): " + errorBody.string());
  //        }
  //    }

  //    private void logTokenInfo(AzureGraphOAuthTokenInfo info) {
  //        logger.trace("Token scope: " + info.scope);
  //        logger.trace("Token expiresIn: " + info.expiresIn);
  //        logger.trace("Token expiresOn: " + info.expiresOn);
  //        logger.trace("Token resource: " + info.resource);
  //        logger.trace("Token tokenType: " + info.tokenType);
  //        logger.trace("Token notBefore: " + info.notBefore);
  //    }

  //    /*
  //     * This method invokes a retrofit API call with retry.  If the first call returns 401 (unauthorized)
  //     * the same is retried again after fetching a new token.
  //    */
  //    private <T> retrofit2.Response<T> invoke(retrofit2.Call<T> call) throws IOException {
  //        for (int retryMax = 2; retryMax > 0; retryMax--) {
  //            if (token == null) {
  //                token = getToken();
  //            }
  //            retrofit2.Response<T> r = call.execute();
  //            if (r.isSuccessful()) {
  //                return r;
  //            } else if (r.code() == 401) {
  //                logger.debug("auth fail, retry: " + call.request().url());
  //                // Call objects cannot be reused, so docs say to use clone() to create a new one with the
  //                // same specs for retry purposes
  //                call = call.clone();
  //                // null out existing token so we'll fetch a new one on next loop pass
  //                token = null;
  //            } else {
  //                throw new IOException("Unhandled invoke response (" + r.code() + ") " + r.errorBody().string());
  //            }
  //        }
  //        throw new IOException("Retry failed for: " + call.request().url());
  //    }

  //    public void removeGroup(String groupId) {
  //        try {
  //            invoke(this.service.deleteGroup(groupId));
  //        } catch (IOException e) {
  //            logger.error(e);
  //            throw new RuntimeException("service.deleteGroup failed", e);
  //        }
  //    }

  //    protected String lookupOffice365GroupId(Group group) {
  //        return group.getAttributeValueDelegate().retrieveValueString(Office365ChangeLogConsumer.GROUP_ID_ATTRIBUTE_NAME);
  //    }

  //    public void addMemberToMS(String groupId, String userPrincipalName) {
  //        try {
  //            invoke(this.service.addGroupMember(groupId, new AzureGraphDataIdContainer("https://graph.microsoft.com/v1.0/users/" + userPrincipalName)));
  //        } catch (IOException e) {
  //            logger.error(e.getMessage(), e);
  //        } catch (MemberAddAlreadyExistsException me) {
  //            logger.debug("member already exists for subject:" + userPrincipalName + " and group:" + groupId);
  //        }
  //    }

  //    public void removeMembership(String userPrincipalName, Group group) {
  //        try {
  //            if (group != null) {
  //                AzureGraphUser user = lookupMSUser(userPrincipalName);
  //                if (user == null) {
  //                    throw new RuntimeException("Failed to locate member: " + userPrincipalName);
  //                }
  //                String groupId = lookupOffice365GroupId(group);
  //                if (ifUserAndGroupExistInMS(user, groupId)) {
  //                    removeUserFromGroupInMS(user.id, groupId);
  //                }
  //            }
  //        } catch (IOException e) {
  //            logger.error(e);
  //        } catch (MemberDeleteAlreadyDeletedException me) {
  //            logger.debug("member already deleted for subject:" + userPrincipalName + " and group:" + group.getId());
  //        }
  //    }

  //    protected boolean ifUserAndGroupExistInMS(AzureGraphUser user, String groupId) {
  //        return user != null && groupId != null;
  //    }

  //    public void removeUserFromGroupInMS(String groupId, String userId) throws IOException {
  //        invoke(this.service.removeGroupMember(groupId, userId));
  //    }

  /**
   * 
   * @param debugMap
   * @param configId
   * @param urlSuffix
   * @return
   */
  private static GetMethod httpGetMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {
    return (GetMethod)httpMethod(debugMap, configId, urlSuffix, "GET");
  }

  /**
   * 
   * @param debugMap
   * @param configId
   * @param urlSuffix
   * @return
   */
  private static PostMethod httpPostMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {
    return (PostMethod)httpMethod(debugMap, configId, urlSuffix, "POST");
  }

  /**
   * 
   * @param debugMap
   * @param configId
   * @param urlSuffix
   * @param httpMethodName is GET, POST, DELETE, PUT
   * @return
   */
  private static HttpMethodBase httpMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix, String httpMethodName) {
    String bearerToken = AzureGrouperExternalSystem
        .retrieveBearerTokenForAzureConfigId(debugMap, configId);
    String graphEndpoint = GrouperLoaderConfig.retrieveConfig()
        .propertyValueStringRequired(
            "grouper.azureConnector." + configId + ".resourceEndpoint");
    String url = graphEndpoint;
    if (url.endsWith("/")) {
      url = url.substring(0, url.length() - 1);
    }
    url += (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    debugMap.put("url", url);

    HttpMethodBase method = null;
    if (StringUtils.equals("GET", httpMethodName)) {
      method = new GetMethod(url);
    } else if (StringUtils.equals("POST", httpMethodName)) {
      method = new PostMethod(url);
    } else {
      throw new RuntimeException("Not expecting type: '" + httpMethodName + "'");
    }
    method.addRequestHeader("Content-Type", "application/json");
    method.addRequestHeader("Authorization", "Bearer " + bearerToken);
    return method;
  }

  private static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId,
      String urlSuffix) {
    
    return executeMethod(debugMap, "GET", configId, urlSuffix, GrouperUtil.toSet(200, 404), new int[] {-1}, null);
    
  }

  private static JsonNode executeMethod(Map<String, Object> debugMap, String httpMethodName, String configId,
      String urlSuffix, Set<Integer> allowedReturnCodes, int[] returnCode, String body) {

    HttpMethodBase httpMethod = httpMethod(debugMap, configId, urlSuffix, httpMethodName);

    HttpClient httpClient = new HttpClient();

    if (!StringUtils.isBlank(body)) {
      if (httpMethod instanceof EntityEnclosingMethod) {
        try {
          StringRequestEntity entity = new StringRequestEntity(body, "application/json", "UTF-8");
          ((EntityEnclosingMethod)httpMethod).setRequestEntity(entity);
        } catch (Exception e) {
          throw new RuntimeException("error", e);
        }
      } else {
        throw new RuntimeException("Cant attach a body if in method: " + httpMethodName);
      }
    }
    
    int code = -1;
    String json = null;

    try {
      code = httpClient.executeMethod(httpMethod);
      returnCode[0] = code;
      json = httpMethod.getResponseBodyAsString();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }

    if (!allowedReturnCodes.contains(code)) {
      throw new RuntimeException(
          "Invalid return code, expecting: " + GrouperUtil.setToString(allowedReturnCodes) + ". '" + debugMap.get("url") + "' " + json);
    }

    if (StringUtils.isBlank(json)) {
      return null;
    }

    JsonNode rootNode = GrouperUtil.jsonJacksonNode(json);
    return rootNode;
  }

  /**
   * create a group
   * @param grouperAzureGroup
   * @return the result
   */
  public static GrouperAzureGroup createGroup(String configId, GrouperAzureGroup grouperAzureGroup) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "createGroup");

    long startTime = System.nanoTime();

    try {

      JsonNode jsonToSend = grouperAzureGroup.toJson();
      String jsonStringToSend = GrouperUtil.jsonJacksonToString(jsonToSend);
      
      JsonNode jsonNode = executeMethod(debugMap, "POST", configId, "/groups", GrouperUtil.toSet(201), new int[] {-1}, jsonStringToSend);
      
      GrouperAzureGroup grouperAzureGroupResult = GrouperAzureGroup.fromJson(jsonNode);

      return grouperAzureGroupResult;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

    

  }

  public static List<GrouperAzureGroup> retrieveAzureGroups(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureGroups");

    long startTime = System.nanoTime();

    try {

      List<GrouperAzureGroup> results = new ArrayList<GrouperAzureGroup>();

      JsonNode jsonNode = executeGetMethod(debugMap, configId,
          "/groups?$select=" + GrouperAzureGroup.fieldsToSelect);

      ArrayNode groupsArray = (ArrayNode) jsonNode.get("value");

      for (int i = 0; i < (groupsArray == null ? 0 : groupsArray.size()); i++) {
        JsonNode groupNode = groupsArray.get(i);
        GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromJson(groupNode);
        results.add(grouperAzureGroup);
      }

      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  public static List<GrouperAzureUser> retrieveAzureUsers(String configId) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureUsers");

    long startTime = System.nanoTime();

    try {

      List<GrouperAzureUser> results = new ArrayList<GrouperAzureUser>();

      JsonNode jsonNode = executeGetMethod(debugMap, configId,
          "/users?$select=" + GrouperAzureUser.fieldsToSelect);

      ArrayNode usersArray = (ArrayNode) jsonNode.get("value");

      for (int i = 0; i < (usersArray == null ? 0 : usersArray.size()); i++) {
        JsonNode userNode = usersArray.get(i);
        GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromJson(userNode);
        results.add(grouperAzureUser);
      }

      debugMap.put("size", GrouperClientUtils.length(results));

      return results;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  /**
   * @param configId
   * @param fieldName id or userPrincipalName
   * @param fieldValue is value of id or userPrincipalName
   * @return
   */
  public static GrouperAzureUser retrieveAzureUser(String configId, String fieldName,
      String fieldValue) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureUser");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = null;
      if (StringUtils.equals(fieldName, "id")
          || StringUtils.equals(fieldName, "userPrincipalName")) {
        urlSuffix = "/users/" + GrouperUtil.escapeUrlEncode(fieldValue) + "?$select="
            + GrouperAzureUser.fieldsToSelect;
      } else {
        throw new RuntimeException("Not expecting field: " + fieldName);
      }

      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);

      //lets get the group node
      ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "value");
      if (value != null && value.size() > 0) {
        if (value.size() == 1) {
          jsonNode = value.get(0);
        } else {
          throw new RuntimeException("Query returned multiple results: " + urlSuffix);
        }
      }

      debugMap.put("found", jsonNode != null);

      if (jsonNode == null) {
        return null;
      }
      GrouperAzureUser grouperAzureUser = GrouperAzureUser.fromJson(jsonNode);

      return grouperAzureUser;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  /**
   * @param configId
   * @param fieldName is id or displayName
   * @param fieldValue is value of id or displayName
   * @return the user
   */
  public static GrouperAzureGroup retrieveAzureGroup(String configId, String fieldName,
      String fieldValue) {

    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

    debugMap.put("method", "retrieveAzureGroup");

    long startTime = System.nanoTime();

    try {

      String urlSuffix = null;
      if (StringUtils.equals(fieldName, "id")) {
        urlSuffix = "/groups/" + GrouperUtil.escapeUrlEncode(fieldValue) + "?$select="
            + GrouperAzureGroup.fieldsToSelect;
      } else {
        urlSuffix = "/groups?$filter=" + GrouperUtil.escapeUrlEncode(fieldName)
            + "%20eq%20'" + GrouperUtil.escapeUrlEncode(fieldValue) + "'&$select="
            + GrouperAzureGroup.fieldsToSelect;
      }

      JsonNode jsonNode = executeGetMethod(debugMap, configId, urlSuffix);

      //lets get the group node
      ArrayNode value = (ArrayNode) GrouperUtil.jsonJacksonGetNode(jsonNode, "value");
      if (value != null && value.size() > 0) {
        if (value.size() == 1) {
          jsonNode = value.get(0);
        } else {
          throw new RuntimeException("Query returned multiple results: " + urlSuffix);
        }
      }

      if (jsonNode == null) {
        return null;
      }
      GrouperAzureGroup grouperAzureGroup = GrouperAzureGroup.fromJson(jsonNode);

      return grouperAzureGroup;
    } catch (RuntimeException re) {
      debugMap.put("exception", GrouperClientUtils.getFullStackTrace(re));
      throw re;
    } finally {
      GrouperAzureLog.azureLog(debugMap, startTime);
    }

  }

  //    public List<AzureGraphGroup> getGroups() throws IOException {
  //        AzureGraphGroups groupContainer = invoke(this.service.getGroups(Collections.emptyMap())).body();
  //        return groupContainer.groups;
  //    }

  //    public List<AzureGraphGroupMember> getGroupMembers(String groupId) throws IOException {
  //        AzureGraphGroupMembers azureGroupMembers = invoke(this.service.getGroupMembers(groupId)).body();
  //        return azureGroupMembers.users;
  //    }

  //    public AzureGraphGroup retrieveGroup(String groupId) throws IOException {
  //        return invoke(this.service.getGroup(groupId)).body();
  //    }

  //    public List<AzureGraphUser> getAllUsers() throws IOException {
  //        AzureGraphUsers azureGraphUsers = invoke(this.service.getUsers()).body();
  //        return azureGraphUsers.users;
  //    }

}
