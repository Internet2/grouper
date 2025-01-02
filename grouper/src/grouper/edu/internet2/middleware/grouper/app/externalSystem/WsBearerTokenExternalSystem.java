package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.j2ee.Authentication;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBase;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;

public class WsBearerTokenExternalSystem extends GrouperExternalSystem {

  public static String authenticateMockUser(HttpServletRequest httpServletRequest) {

    ConfigPropertiesCascadeBase.clearCache();

    Pattern clientIdPattern = Pattern.compile("^grouper\\.wsBearerToken\\.([^.]+)\\.endpoint$");
    for (String configId : GrouperLoaderConfig.retrieveConfig().propertyConfigIds(clientIdPattern)) {
      
      String httpAuthnType = GrouperLoaderConfig.retrieveConfig()
          .propertyValueString(
              "grouper.wsBearerToken." + configId + ".httpAuthnType", "bearerToken");

      if (StringUtils.equals(httpAuthnType, "bearerToken")) {

        String bearerTokenConfig = GrouperLoaderConfig.retrieveConfig()
            .propertyValueStringRequired(
                "grouper.wsBearerToken." + configId + ".accessTokenPassword");

        boolean prependBearerTokenPrefix = GrouperLoaderConfig.retrieveConfig()
            .propertyValueBoolean(
                "grouper.wsBearerToken." + configId + ".prependBearerTokenPrefix", true);
        
        bearerTokenConfig = prependBearerTokenPrefix ? ("Bearer " + bearerTokenConfig) : bearerTokenConfig;

        String httpHeader = GrouperLoaderConfig.retrieveConfig()
            .propertyValueString(
                "grouper.wsBearerToken." + configId + ".httpHeader", "Authorization");

        String bearerTokenSent = httpServletRequest.getHeader(httpHeader);

        if (StringUtils.equals(bearerTokenSent, bearerTokenConfig)) {
          return configId;
        }

      } else if (StringUtils.equals(httpAuthnType, "basicAuth")) {
        
        String userFromConfig = GrouperLoaderConfig.retrieveConfig()
            .propertyValueStringRequired("grouper.wsBearerToken." + configId + ".basicAuthUser");
        
        String passwordFromConfig = GrouperLoaderConfig.retrieveConfig()
            .propertyValueString("grouper.wsBearerToken." + configId + ".basicAuthPassword");
        
        passwordFromConfig = StringUtils.defaultString(passwordFromConfig);

        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        
        String userSent = Authentication.retrieveUsername(authorizationHeader);
        String passwordSent = Authentication.retrievePassword(authorizationHeader);
        
        if (StringUtils.equals(userSent, userFromConfig) && StringUtils.equals(passwordSent, passwordFromConfig)) {
          return configId;
        }
        
      } else {
        throw new RuntimeException("Invalid authentication type: grouper-loader.properties: 'grouper.wsBearerToken." + configId + ".httpAuthnType' = '" + httpAuthnType + "'");
      }

    }
    return null;
  }
  
  private static ExpirableCache<String, String> configKeyToExpiresOnAndBearerToken = new ExpirableCache<String, String>();
  
  /**
   * get bearer token for adobe config id
   * @param configId
   * @return the bearer token
   */
  public static String retrieveBearerTokenForConfigId(Map<String, Object> debugMap, String configId) {
    
    String encryptedBearerToken = configKeyToExpiresOnAndBearerToken.get(configId);
  
    if (StringUtils.isNotBlank(encryptedBearerToken)) {
      if (debugMap != null) {
        debugMap.put("cachedAccessToken", true);
      }
      return Morph.decrypt(encryptedBearerToken);
    }
    
    Object[] accessTokenAndExpiry = WsBearerTokenExternalSystem.generateAccessToken(debugMap, configId);
    
    String accessToken = GrouperUtil.toStringSafe(accessTokenAndExpiry[0]);
    int expiresInSeconds = (Integer) accessTokenAndExpiry[1] - 5; // subtracting 5 just in case if there are network delays
    int timeToLive = expiresInSeconds/60;
    configKeyToExpiresOnAndBearerToken.put(configId, Morph.encrypt(accessToken), timeToLive - 5);
    return accessToken;
  }
  

  /**
   * get access token from adobe
   * @param debugMap can be null
   * @param configId
   * @param scope
   * @return token in the first index and its int expiry in seconds in the second index
   */
  public static Object[] generateAccessToken(Map<String, Object> debugMap, String configId) {
    
    long startedNanos = System.nanoTime();
    
    try {
      // we need to get another one
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      
      final String url = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + configId + ".tokenUrl");
      
      final String grantType = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + configId + ".grant_type");
      final String scope = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + configId + ".scope");
      final String clientId = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + configId + ".clientId");
      final String clientSecret = GrouperLoaderConfig.retrieveConfig().propertyValueStringRequired("grouper.wsBearerToken." + configId + ".clientSecret");
      
      grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.addUrlParameter("grant_type", grantType);
      grouperHttpClient.addUrlParameter("client_id", clientId);
      grouperHttpClient.addUrlParameter("client_secret", clientSecret);
      grouperHttpClient.addUrlParameter("scope", scope);
      
      grouperHttpClient.assignDoNotLogResponseBody(true);
      grouperHttpClient.assignDoNotLogRequestBody(true);
      
      String proxyUrl = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.wsBearerToken." + configId + ".proxyUrl");
      String proxyType = GrouperLoaderConfig.retrieveConfig().propertyValueString("grouper.wsBearerToken." + configId + ".proxyType");
      
      grouperHttpClient.assignProxyUrl(proxyUrl);
      grouperHttpClient.assignProxyType(proxyType);
      
     //https://ims-na1.adobelogin.com/ims/token/v2?grant_type=client_credentials&client_id=sfd&client_secret=sdf&scope=openid,AdobeID,user_management_sdk
      
//      grouperHttpClient.addBodyParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");

      int code = -1;
      String json = null;
  
      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        // System.out.println(code + ", " + postMethod.getResponseBodyAsString());
        
        json = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        throw new RuntimeException("Error connecting to '" + url + "'", e);
      }
  
      if (code != 200) {
        throw new RuntimeException("Cant get access token from '" + url + "' " + code + ", " + json);
      }
      
      JsonNode jsonObject = GrouperUtil.jsonJacksonNode(json);
      int expiresInSeconds = GrouperUtil.jsonJacksonGetInteger(jsonObject, "expires_in");
      String accessToken = GrouperUtil.jsonJacksonGetString(jsonObject, "access_token");
      return new Object[] {accessToken, expiresInSeconds};
      
    } catch (RuntimeException re) {
      
      if (debugMap != null) {
        debugMap.put("adobeTokenError", GrouperUtil.getFullStackTrace(re));
      }
      throw re;
  
    } finally {
      if (debugMap != null) {
        debugMap.put("adobeTokenTookMillis", (System.nanoTime()-startedNanos)/1000000);
      }
    }
  }

  public static void attachAuthenticationToHttpClient(GrouperHttpClient grouperHttpClient, String externalSystemConfigId) {
    attachAuthenticationToHttpClient(grouperHttpClient, externalSystemConfigId, null, null);
  }
  
  public static void attachAuthenticationToHttpClient(GrouperHttpClient grouperHttpClient, String externalSystemConfigId, GrouperLoaderConfig grouperLoaderConfig, Map<String, Object> debugMap) {
    //  # Authentication type.
    //  # {valueType: "string", defaultValue: "bearerToken", regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.httpAuthnType$", formElement: "dropdown", optionValues: ["bearerToken", "basicAuth"]}
    //  # grouper.wsBearerToken.myWsBearerToken.httpAuthnType = 
    //
    //  # Basic auth user
    //  # {valueType: "string", required: true, regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.basicAuthUser$", formElement: "dropdown", optionValues: ["bearerToken", "basicAuth"], showEl: "${httpAuthnType == 'basicAuth'}}
    //  # grouper.wsBearerToken.myWsBearerToken.basicAuthUser = 
    //
    //  # Basic auth password
    //  # {valueType: "string", regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.basicAuthPass$", showEl: "${httpAuthnType == 'basicAuth'}}
    //  # grouper.wsBearerToken.myWsBearerToken.basicAuthPassword = 
    //
    //  # Bearer token secret, e.g. AWS access token
    //  # {valueType: "password", sensitive: true, required: true, regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.accessTokenPassword$", showEl: "${httpAuthnType == null || httpAuthnType == 'bearerToken'}"}
    //  # grouper.wsBearerToken.myWsBearerToken.accessTokenPassword =
    //
    //  # Include Bearer: prefix on access token.  If you want to change the prefix, just prefix the access token password and set this to false
    //  # {valueType: "boolean", defaultValue: "true", regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.accessTokenPassword$", showEl: "${httpAuthnType == null || httpAuthnType == 'bearerToken'}"}
    //  # grouper.wsBearerToken.myWsBearerToken.prependBearerTokenPrefix =
    //
    //  # HTTP header to put the authentication in.  Default is the standard Authorization header
    //  # {valueType: "string", defaultValue: "Authorization", regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.httpHeader$", showEl: "${httpAuthnType == null || httpAuthnType == 'bearerToken'}"}
    //  # grouper.wsBearerToken.myWsBearerToken.httpHeader =

    grouperLoaderConfig = grouperLoaderConfig != null ? grouperLoaderConfig : GrouperLoaderConfig.retrieveConfig();
    
    String httpAuthnType = grouperLoaderConfig
        .propertyValueString(
            "grouper.wsBearerToken." + externalSystemConfigId + ".httpAuthnType", "bearerToken");

    if (StringUtils.equals(httpAuthnType, "bearerToken")) {
      String bearerToken = grouperLoaderConfig
          .propertyValueStringRequired(
              "grouper.wsBearerToken." + externalSystemConfigId + ".accessTokenPassword");

      boolean prependBearerTokenPrefix = grouperLoaderConfig
          .propertyValueBoolean(
              "grouper.wsBearerToken." + externalSystemConfigId + ".prependBearerTokenPrefix", true);
      
      String headerValue = prependBearerTokenPrefix ? ("Bearer " + bearerToken) : bearerToken;

      String httpHeader = grouperLoaderConfig
          .propertyValueString(
              "grouper.wsBearerToken." + externalSystemConfigId + ".httpHeader", "Authorization");

      grouperHttpClient.addHeader(httpHeader, headerValue);
    } else if (StringUtils.equals(httpAuthnType, "basicAuth")) {
      
      String user = grouperLoaderConfig
          .propertyValueStringRequired("grouper.wsBearerToken." + externalSystemConfigId + ".basicAuthUser");
      
      String password = grouperLoaderConfig
          .propertyValueString("grouper.wsBearerToken." + externalSystemConfigId + ".basicAuthPassword");
      
      password = StringUtils.defaultString(password);
      
      grouperHttpClient.assignUser(user);
      grouperHttpClient.assignPassword(password);
      
    } else if (StringUtils.equals(httpAuthnType, "oauthClientCredentials")) {
      
      String bearerToken = retrieveBearerTokenForConfigId(debugMap, externalSystemConfigId);
      
      grouperHttpClient.addHeader("Authorization", "Bearer " + bearerToken);

      String apiKeyHeader = grouperLoaderConfig.propertyValueString("grouper.wsBearerToken." + externalSystemConfigId + ".apiKeyHeaderName");
      String apiKeyPassword = grouperLoaderConfig.propertyValueString("grouper.wsBearerToken." + externalSystemConfigId + ".apiKeyPassword");
      
      if (StringUtils.isNotBlank(apiKeyHeader) && StringUtils.isNotBlank(apiKeyPassword)) {      
        grouperHttpClient.addHeader(apiKeyHeader, apiKeyPassword);
      }
      
    } else {
      throw new RuntimeException("Invalid authentication type: grouper-loader.properties: 'grouper.wsBearerToken." + externalSystemConfigId + ".httpAuthnType' = '" + httpAuthnType + "'");
    }
    
    String proxyUrl = grouperLoaderConfig.propertyValueString("grouper.wsBearerToken." + externalSystemConfigId + ".proxyUrl");
    String proxyType = grouperLoaderConfig.propertyValueString("grouper.wsBearerToken." + externalSystemConfigId + ".proxyType");
    
    grouperHttpClient.assignProxyUrl(proxyUrl);
    grouperHttpClient.assignProxyType(proxyType);


  }
  
  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_LOADER_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouper.wsBearerToken." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouper\\.wsBearerToken)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myWsBearerToken";
  }

  /**
   * Validates the Scim provisioner by trying to log in and getting an auth token
   * @return
   * @throws UnsupportedOperationException
   */
  @Override
  public List<String> test() throws UnsupportedOperationException {
    List<String> ret = new ArrayList<>();

    //  # Base website URL for WS with bearer token authn.  e.g. https://scim.us-east-1.amazonaws.com/abc123/scim/v2/
    //  # {valueType: "string", required: true, regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.endpoint$"}
    //  # grouper.wsBearerToken.myWsBearerToken.endpoint = 
    //
    //  # Bearer token secret
    //  # {valueType: "password", sensitive: true, required: true, regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.accessTokenPassword$"}
    //  # grouper.wsBearerToken.myWsBearerToken.accessTokenPassword =
    //
    //  # if this scim connector is enabled
    //  # {valueType: "boolean", regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.enabled$", defaultValue: "true"}
    //  # grouper.wsBearerToken.myWsBearerToken.enabled =
    //
    //  # Test URL suffix that returns a 200
    //  # {valueType: "string", regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.testUrlSuffix$"}
    //  # grouper.wsBearerToken.myWsBearerToken.testUrlSuffix = 
    //
    //  # Test URL method, defaults to GET.  Could be POST or whatever.
    //  # {valueType: "string", defaultValue: "GET", regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.testHttpMethod$"}
    //  # grouper.wsBearerToken.myWsBearerToken.testHttpMethod = 
    //
    //  # Test URL response code.  Defaults to 200
    //  # {valueType: "integer", defaultValue: "200", regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.testHttpResponseCode$"}
    //  # grouper.wsBearerToken.myWsBearerToken.testHttpResponseCode = 
    //
    //  # Test URL response regex to match to see if valid (optional)
    //  # {valueType: "string", regex: "^grouper\\.myWsBearerToken\\.([^.]+)\\.testUrlResponseBodyRegex$"}
    //  # grouper.wsBearerToken.myWsBearerToken.testUrlResponseBodyRegex = 
    
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    String configPrefix = "grouper.wsBearerToken." + this.getConfigId() + ".";

    String httpAuthnType = config
        .propertyValueString(
            configPrefix + "httpAuthnType", "bearerToken");

    String endpointProperty = configPrefix + (StringUtils.equals("oauthClientCredentials", httpAuthnType) ? "serviceUrl" : "endpoint");
    String endpoint = config.propertyValueString(endpointProperty);
    if (GrouperUtil.isBlank(endpoint)) {
      ret.add("Undefined or blank property: " + endpointProperty);
    }

    String testUrlSuffixProperty = configPrefix + "testUrlSuffix";
    String testUrlSuffix = config.propertyValueString(testUrlSuffixProperty);
    if (!GrouperUtil.isBlank(testUrlSuffix)) {
      
      final String testHttpMethod = GrouperUtil.defaultIfBlank(config.propertyValueString(configPrefix + "testHttpMethod"), "GET");

      int testHttpResponseCode = GrouperUtil.intValue(config.propertyValueString(configPrefix + "testHttpResponseCode"), 200);

      String testUrlResponseBodyRegex = config.propertyValueString(configPrefix + "testUrlResponseBodyRegex");
      
      // we need to get another one
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
      final String url = GrouperUtil.stripLastSlashIfExists(endpoint) + "/" + GrouperUtil.stripFirstSlashIfExists(testUrlSuffix);
      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(testHttpMethod);

      attachAuthenticationToHttpClient(grouperHttpClient, this.getConfigId());

      int code = -1;
      String response = null;

      try {
        grouperHttpClient.executeRequest();
        code = grouperHttpClient.getResponseCode();
        response = grouperHttpClient.getResponseBody();
      } catch (Exception e) {
        ret.add("Error connecting to '" + url + "' <pre>" + GrouperUtil.getFullStackTrace(e) + "</pre>");
        return ret;
      }

      if (code != testHttpResponseCode) {
        ret.add("Response code to " + url + " expecting " + testHttpResponseCode + " but received " + code);
        return ret;
      }
      
      if (!StringUtils.isBlank(testUrlResponseBodyRegex)) {
        if (response == null) {
          ret.add("Response body from " + url + " expecting regex " + testUrlResponseBodyRegex + " but response was null");
        } else {
          Pattern pattern = Pattern.compile(testUrlResponseBodyRegex, Pattern.DOTALL);
          Matcher matcher = pattern.matcher(response);
          if (!matcher.matches()) {
            ret.add("Response body from " + url + " expecting regex " + testUrlResponseBodyRegex + " but no match " + GrouperUtil.escapeHtml(response, true));
          }
        }
      }
    }
    
    return ret;
  }

  @Override
  public void validatePreSave(boolean isInsert, boolean fromUi,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, fromUi, errorsToDisplay, validationErrorsToDisplay);
    
    GrouperConfigurationModuleAttribute endpointAttribute = this.retrieveAttributes().get("endpoint");

    if (endpointAttribute != null && StringUtils.startsWithIgnoreCase(endpointAttribute.getValueOrExpressionEvaluation(), "https://api.github.com/scim/")) {
     
      String endpoint = endpointAttribute.getValueOrExpressionEvaluation();
      
      // https://api.github.com/scim/v2/organizations/ORG
      endpoint = GrouperUtil.stripLastSlashIfExists(endpoint);
      if (!StringUtils.startsWithIgnoreCase(endpoint, "https://api.github.com/scim/v2/organizations/")) {
        validationErrorsToDisplay.put(endpointAttribute.getHtmlForElementIdHandle(), GrouperTextContainer.textOrNull("grouperConfigurationValidationGithubEndpointMustContainOrganization"));
      }
      
    }
    
  }
  

}
