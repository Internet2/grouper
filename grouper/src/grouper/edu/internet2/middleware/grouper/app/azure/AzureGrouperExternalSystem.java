package edu.internet2.middleware.grouper.app.azure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;

public class AzureGrouperExternalSystem extends GrouperExternalSystem {

  /**
   * cache of config key to expires on and encrypted bearer token
   */
  private static ExpirableCache<String, MultiKey> configKeyToExpiresOnAndBearerToken = new ExpirableCache<String, MultiKey>(60);

  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.azureConnector." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.azureConnector)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myAzure";
  }

  /**
   * cache connections
   */
  private static ExpirableCache<String, GrouperAzureApiCommands> apiConnectionCache = new ExpirableCache<String, GrouperAzureApiCommands>(5);

  /**
   * Validates the Azure provisioner by trying to log in and getting an auth token
   * @return
   * @throws UnsupportedOperationException
   */
  @Override
  public List<String> test() throws UnsupportedOperationException {
    List<String> ret = new ArrayList<>();

    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    String configPrefix = "grouper.azureConnector." + this.getConfigId() + ".";

    String loginEndpointProperty = configPrefix + "loginEndpoint";
    String loginEndpoint = config.propertyValueString(loginEndpointProperty);
    if (GrouperUtil.isBlank(loginEndpoint)) {
      ret.add("Undefined or blank property: " + loginEndpointProperty);
    }

    String resourceEndpointProperty = configPrefix + "resourceEndpoint";
    String resourceEndpoint = config.propertyValueString(resourceEndpointProperty);
    if (GrouperUtil.isBlank(resourceEndpoint)) {
      ret.add("Undefined or blank property: " + resourceEndpointProperty);
    }


    String clientIdProperty = configPrefix + "clientId";
    String clientId = config.propertyValueString(clientIdProperty);
    if (GrouperUtil.isBlank(clientId)) {
      ret.add("Undefined or blank property: " + clientIdProperty);
    }

    String clientSecretProperty = configPrefix + "clientSecret";
    String clientSecret = config.propertyValueString(clientSecretProperty);
    if (GrouperUtil.isBlank(clientSecret)) {
      ret.add("Undefined or blank property: " + clientSecretProperty);
    }

    String tenantIdProperty = configPrefix + "tenantId";
    String tenantId = config.propertyValueString(tenantIdProperty);
    if (GrouperUtil.isBlank(tenantId)) {
      ret.add("Undefined or blank property: " + tenantIdProperty);
    }

    String scope = config.propertyValueString(configPrefix + "scope");

    try {
      
      retrieveBearerTokenForAzureConfigId(new HashMap<String, Object>(), this.getConfigId());

    } catch (Exception e) {
      ret.add("Unable to retrieve Azure authentication token: " + GrouperUtil.escapeHtml(e.getMessage(), true));
    }

    return ret;
  }

  /**
   * get bearer token for azure config id
   * @param configId
   * @return the bearer token
   */
  public static String retrieveBearerTokenForAzureConfigId(Map<String, Object> debugMap, String configId) {
    
    long startedNanos = System.nanoTime();
        
    MultiKey expiresOnAndEncryptedBearerToken = configKeyToExpiresOnAndBearerToken.get(configId);
  
    String encryptedBearerToken = null;
    if (expiresOnAndEncryptedBearerToken != null) {
      long expiresOnSeconds = (Long)expiresOnAndEncryptedBearerToken.getKey(0);
      encryptedBearerToken = (String)expiresOnAndEncryptedBearerToken.getKey(1);
      if (expiresOnSeconds * 1000 > System.currentTimeMillis()) {
        // use it
        if (debugMap != null) {
          debugMap.put("azureCachedAccessToken", true);
        }
        return Morph.decrypt(encryptedBearerToken);
      }
    }
    try {
      // we need to get another one
      HttpClient httpClient = new HttpClient();
      String loginEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".loginEndpoint");
      String directoryId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".tenantId");
      final String url = loginEndpoint + (loginEndpoint.endsWith("/") ? "" : "/") + directoryId + "/oauth2/token";
      PostMethod postMethod = new PostMethod(url);
      
      String clientId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".clientId");
      postMethod.addParameter("client_id", clientId);
  
      String clientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".clientSecret");
      clientSecret = Morph.decryptIfFile(clientSecret);
      postMethod.addParameter("client_secret", clientSecret);
  
      postMethod.addParameter("grant_type", "client_credentials");
  
      String resource = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.azureConnector." + configId + ".resource");
      postMethod.addParameter("resource", resource);
  
      int code = -1;
      String json = null;
  
      try {
        code = httpClient.executeMethod(postMethod);
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = postMethod.getResponseBodyAsString();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      if (code != 200) {
        throw new RuntimeException("Cant get access token from '" + url + "' " + code + ", " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      long expiresOn = GrouperUtil.jsonJacksonGetLong(jsonObject, "expires_on", -1L);
      String accessToken = GrouperUtil.jsonJacksonGetString(jsonObject, "access_token");
  
      expiresOnAndEncryptedBearerToken = new MultiKey(expiresOn, Morph.encrypt(accessToken));
      configKeyToExpiresOnAndBearerToken.put(configId, expiresOnAndEncryptedBearerToken);
      return accessToken;
    } catch (RuntimeException re) {
      
      if (debugMap != null) {
        debugMap.put("azureTokenError", GrouperUtil.getFullStackTrace(re));
      }
      throw re;
  
    } finally {
      if (debugMap != null) {
        debugMap.put("azureTokenTookMillis", (System.nanoTime()-startedNanos)/1000000);
      }
    }
  }



}
