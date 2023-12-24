package edu.internet2.middleware.grouper.app.teamDynamix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;

public class TeamDynamixExternalSystem extends GrouperExternalSystem {

  /**
   * cache of config key to expires on and encrypted bearer token
   */
  private static ExpirableCache<String, MultiKey> configKeyToExpiresOnAndBearerToken = null;

  private static int teamDynamixAuthnTokenExpiresSeconds = -999;
    
  /**
   * might be null
   * @return
   */
  private static ExpirableCache<String, MultiKey> configKeyToExpiresOnAndBearerToken() {
    if (teamDynamixAuthnTokenExpiresSeconds == -999) {
      teamDynamixAuthnTokenExpiresSeconds = GrouperLoaderConfig.retrieveConfig().propertyValueInt("teamDynamixAuthnTokenExpiresSeconds", 600);
      if (teamDynamixAuthnTokenExpiresSeconds > 0) {
        configKeyToExpiresOnAndBearerToken = new ExpirableCache<String, MultiKey>(teamDynamixAuthnTokenExpiresSeconds);
      }
    }
    return configKeyToExpiresOnAndBearerToken;
  }

  public static void clearCache() {
    configKeyToExpiresOnAndBearerToken.clear();
  }
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  //  # team dynamix login base uri to get a token. Should end in a slash.  e.g. https://yourTeamDynamixDomain/
  //  # {valueType: "string", required: true, regex: "^grouper\\.teamDynamix\\.([^.]+)\\.url$"}
  //  # grouper.teamDynamix.myTeamDynamix.url = 
  //
  //  # team dynamix beid (tenant id)
  //  # {valueType: "string", required: true, regex: "^grouper\\.teamDynamix\\.([^.]+)\\.beid$"}
  //  # grouper.teamDynamix.myTeamDynamix.beid =
  //
  //  # web services key
  //  # {valueType: "string", required: true, regex: "^grouper\\.teamDynamix\\.([^.]+)\\.webServicesKey"}
  //  # grouper.teamDynamix.myTeamDynamix.webServicesKey =
  //
  //  # proxy requests here, e.g. https://server:1234
  //  # {valueType: "string", regex: "^grouper\\.teamDynamix\\.([^.]+)\\.proxyUrl$"}
  //  # grouper.teamDynamix.myTeamDynamix.proxyUrl =
  //
  //  # socks or http
  //  # {valueType: "string", regex: "^grouper\\.teamDynamix\\.([^.]+)\\.proxyType$", formElement: "dropdown", optionValues: ["PROXY_HTTP", "PROXY_SOCKS5"]}
  //  # grouper.teamDynamix.myTeamDynamix.proxyType =
  //
  //  # if this team dynamix connector is enabled
  //  # {valueType: "boolean", regex: "^grouper\\.teamDynamix\\.([^.]+)\\.enabled$", defaultValue: "true"}
  //  # grouper.teamDynamix.myTeamDynamix.enabled
  
  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.teamDynamix." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.teamDynamix)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myTeamDynamix";
  }

  /**
   * Validates the team dynamix provisioner by trying to log in and getting an auth token
   * @return
   * @throws UnsupportedOperationException
   */
  @Override
  public List<String> test() throws UnsupportedOperationException {
    List<String> ret = new ArrayList<>();


    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    String configPrefix = "grouper.teamDynamix." + this.getConfigId() + ".";

    String urlProperty = configPrefix + "url";
    String url = config.propertyValueString(urlProperty);
    if (GrouperUtil.isBlank(url)) {
      ret.add("Undefined or blank property: " + urlProperty);
    }

    String beidProperty = configPrefix + "beid";
    String beid = config.propertyValueString(beidProperty);
    if (GrouperUtil.isBlank(beid)) {
      ret.add("Undefined or blank property: " + beidProperty);
    }

    String webServicesKeyProperty = configPrefix + "webServicesKey";
    String webServicesKey = config.propertyValueString(webServicesKeyProperty);
    if (GrouperUtil.isBlank(webServicesKey)) {
      ret.add("Undefined or blank property: " + webServicesKeyProperty);
    }

    try {
      
      retrieveBearerTokenForTeamDynamixConfigId(new HashMap<String, Object>(), this.getConfigId());

    } catch (Exception e) {
      ret.add("Unable to retrieve Team dynamix authentication token: " + GrouperUtil.escapeHtml(e.getMessage(), true));
    }

    return ret;
  }

  /**
   * get bearer token for team dynamix config id
   * @param configId
   * @return the bearer token
   */
  public static String retrieveBearerTokenForTeamDynamixConfigId(Map<String, Object> debugMap, String configId) {
    
    long startedNanos = System.nanoTime();
        
    ExpirableCache<String, MultiKey> configKeyToExpiresOnAndBearerToken2 = configKeyToExpiresOnAndBearerToken();
    
    MultiKey expiresOnAndEncryptedBearerToken = configKeyToExpiresOnAndBearerToken2 == null ? null : configKeyToExpiresOnAndBearerToken2.get(configId);
  
    String encryptedBearerToken = null;
    if (expiresOnAndEncryptedBearerToken != null) {
      long expiresOnSeconds = (Long)expiresOnAndEncryptedBearerToken.getKey(0);
      encryptedBearerToken = (String)expiresOnAndEncryptedBearerToken.getKey(1);
      if (expiresOnSeconds * 1000 > System.currentTimeMillis()) {
        // use it
        if (debugMap != null) {
          debugMap.put("teamDynamixCachedAccessToken", true);
        }
        return Morph.decrypt(encryptedBearerToken);
      }
    }
    try {
      // we need to get another one
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      //TODO
      //grouperHttpClient.assignDoNotLogHeaders(TeamDynamixMockServiceHandler.doNotLogHeaders);
      
      grouperHttpClient.assignDoNotLogResponseBody(true);
      
      //  # team dynamix login base uri to get a token. Should end in a slash.  e.g. https://yourTeamDynamixDomain/
      //  # {valueType: "string", required: true, regex: "^grouper\\.teamDynamix\\.([^.]+)\\.url$"}
      //  # grouper.teamDynamix.myTeamDynamix.url = 
      //
      //  # team dynamix beid (tenant id)
      //  # {valueType: "string", required: true, regex: "^grouper\\.teamDynamix\\.([^.]+)\\.beid$"}
      //  # grouper.teamDynamix.myTeamDynamix.beid =
      //
      //  # web services key
      //  # {valueType: "string", required: true, regex: "^grouper\\.teamDynamix\\.([^.]+)\\.webServicesKey"}
      //  # grouper.teamDynamix.myTeamDynamix.webServicesKey =

      String loginEndpoint = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.teamDynamix." + configId + ".url");
      String beid = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.teamDynamix." + configId + ".beid");
      String webServicesKey = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.teamDynamix." + configId + ".webServicesKey");
      
      //  # expires in seconds
      //  # {valueType: "string", required: true, regex: "^grouper\\.teamDynamix\\.([^.]+)\\.expiresInSeconds"}
      //  # grouper.teamDynamix.myTeamDynamix.expiresInSeconds =
      int expiresInSeconds = GrouperLoaderConfig.retrieveConfig().propertyValueInt("grouper.teamDynamix." + configId + ".expiresInSeconds", 60);
      
      long secondsExpiresOn = System.currentTimeMillis() / 1000 + expiresInSeconds;
      
      // https://solutions.teamdynamix.com/TDClient/1965/Portal/KB/ArticleDet?ID=1715
      final String url = loginEndpoint + (loginEndpoint.endsWith("/") ? "" : "/") + "api/auth/loginadmin";
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      grouperHttpClient.assignUrl(url);

      String proxyUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.teamDynamix." + configId + ".proxyUrl");
      String proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.teamDynamix." + configId + ".proxyType");
      
      grouperHttpClient.assignProxyUrl(proxyUrl);
      grouperHttpClient.assignProxyType(proxyType);

      ObjectNode jsonJacksonNode = GrouperUtil.jsonJacksonNode();
      jsonJacksonNode.put("BEID", beid);
      jsonJacksonNode.put("WebServicesKey", webServicesKey);
      String authnBody = GrouperUtil.jsonJacksonToString(jsonJacksonNode);
      
      grouperHttpClient.assignBody(authnBody);
  
      grouperHttpClient.addHeader("Content-Type", "text/json; charset=utf-8");
      
      int code = -1;
      String bearerToken = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        bearerToken = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      if (code != 200) {
        throw new RuntimeException("Cant get access token from '" + url + "' " + code + ", body: " + bearerToken);
      }
  
      expiresOnAndEncryptedBearerToken = new MultiKey(secondsExpiresOn, Morph.encrypt(bearerToken));
      
      if (configKeyToExpiresOnAndBearerToken2 != null && expiresInSeconds > 0) {
        configKeyToExpiresOnAndBearerToken2.put(configId, expiresOnAndEncryptedBearerToken);
      }
      return bearerToken;
    } catch (RuntimeException re) {
      
      if (debugMap != null) {
        debugMap.put("teamDynamixTokenError", GrouperUtil.getFullStackTrace(re));
      }
      throw re;
  
    } finally {
      if (debugMap != null) {
        debugMap.put("teamDynamixTokenTookMillis", (System.nanoTime()-startedNanos)/1000000);
      }
    }
  }



}
