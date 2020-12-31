package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;

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
    
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.azure1.loginEndpoint", "http://localhost/f3/login.microsoftonline.com/");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("grouper.azureConnector.azure1.resourceEndpoint", "http://localhost/f3/graph.microsoft.com/v1.0/");


    //GrouperProvisioner grouperProvisioner = GrouperProvisioner.retrieveProvisioner("AzureProvA");
    //GrouperProvisioningOutput grouperProvisioningOutput = grouperProvisioner.provision(GrouperProvisioningType.fullProvisionFull);

    //GraphApiClient apiClient = AzureGrouperExternalSystem.retrieveApiConnectionForProvisioning("azure1");

    List<GrouperAzureGroup> grouperAzureGroups = retrieveAzureGroups("azure1");
    
    for (GrouperAzureGroup grouperAzureGroup : grouperAzureGroups) {
      System.out.println(grouperAzureGroup);
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

//    public AzureGraphGroup addGroup(String displayName, String mailNickname, String description) {
//        logger.debug("Creating group " + displayName + ", group type: " + this.azureGroupType.name());
//        boolean securityEnabled;
//        Collection<String> groupTypes = new ArrayList<>();
//
//        switch (this.azureGroupType) {
//            case Security:
//                securityEnabled = true;
//                break;
//            case Unified:
//                groupTypes.add("Unified");
//                securityEnabled = false;
//                break;
//            case MailEnabled:
//            case MailEnabledSecurity:
//                throw new UnableToPerformException("Mail enabled Azure groups are currently not supported");
//            default:
//                throw new IllegalStateException("Unexpected value: " + this.azureGroupType);
//        }
//        try {
//            AzureGraphGroup azureGroup = invoke(this.service.createGroup(
//                    new AzureGraphGroup(
//                            null,
//                            displayName,
//                            false,
//                            mailNickname,
//                            securityEnabled,
//                            groupTypes,
//                            description,
//                            visibility
//                    )
//            )).body();
//
//            logger.debug("Created group in Azure: id = " + (azureGroup == null ? "null" : azureGroup.id));
//            return azureGroup;
//
//        } catch (IOException e) {
//            logger.error(e);
//            throw new RuntimeException("service.createGroup failed", e);
//        }
//    }

//    public void removeGroup(String groupId) {
//        try {
//            invoke(this.service.deleteGroup(groupId));
//        } catch (IOException e) {
//            logger.error(e);
//            throw new RuntimeException("service.deleteGroup failed", e);
//        }
//    }

    /* In Kansas State's version, this was used to look up users from multiple domains */
//    public AzureGraphUser lookupMSUser(String userPrincipalName) {
//        AzureGraphUser user = null;
//        logger.debug("calling getUserFrom Office365ApiClient");
//        try {
//            user = invoke(this.service.getUserByUPN(userPrincipalName)).body();
//            logger.debug("user = " + (user == null ? "null" : user.toString()));
//            return user;
//        } catch (IOException e) {
//            logger.debug("user principal " + userPrincipalName + " was not found");
//        }
//        return null;
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
  private static GetMethod httpGetMethod(Map<String, Object> debugMap, String configId, String urlSuffix) {
    String bearerToken = AzureGrouperExternalSystem.retrieveBearerTokenForAzureConfigId(debugMap, configId);
    String graphEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".resourceEndpoint");
    String url = graphEndpoint;
    if (url.endsWith("/")) {
      url = url.substring(0, url.length()-1);
    }
    url += (urlSuffix.startsWith("/") ? "" : "/") + urlSuffix;
    debugMap.put("url", url);
    
    GetMethod getMethod = new GetMethod(url);
    
    getMethod.addRequestHeader("Content-Type", "application/json");
    getMethod.addRequestHeader("Authorization", "Bearer " + bearerToken);
    return getMethod;
  }
  
  private static JsonNode executeGetMethod(Map<String, Object> debugMap, String configId, String urlSuffix) {

    GetMethod getMethod = httpGetMethod(debugMap, configId, urlSuffix);
    
    HttpClient httpClient = new HttpClient();
    
    int code = -1;
    String json = null;

    try {
      code = httpClient.executeMethod(getMethod);        
      json = getMethod.getResponseBodyAsString();
    } catch (Exception e) {
      throw new RuntimeException("Error connecting to '" + debugMap.get("url") + "'", e);
    }

    if (code != 200) {
      throw new RuntimeException("Cant get group from '" + debugMap.get("url") + "' " + json);
    }

    JsonNode rootNode = GrouperUtil.jsonJacksonNode(json);
    return rootNode;
  }
  
  public static List<GrouperAzureGroup> retrieveAzureGroups(String configId) {
    
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
  
    debugMap.put("method", "retrieveAzureGroups");
  
    long startTime = System.nanoTime();
  
    try {
  
      List<GrouperAzureGroup> results = new ArrayList<GrouperAzureGroup>();
      
      JsonNode jsonNode = executeGetMethod(debugMap, configId, "/groups");
      
      ArrayNode groupsArray = (ArrayNode)jsonNode.get("value");
      
      for (int i=0;i<(groupsArray == null ? 0 : groupsArray.size());i++) {
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
