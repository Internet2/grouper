package edu.internet2.middleware.grouper.app.ccure;

/**
 *
 * clientName
 * cliendId
 * clientVersion
 * endpoint
 * username
 * password
 * proxyUrl
 * proxyType
 * testUrlSuffix
 * testHttpMethod
 * testHttpResponseCode
 * testUrlResponseBodyRegex
 *
 */

import com.fasterxml.jackson.databind.JsonNode;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CCureExternalSystem extends GrouperExternalSystem {

  public static final String PROPERTY_PREFIX = "grouper.CCureExternalSystem.";
  public static final String CONFIGID_REGEX = "^(grouper\\.CCureExternalSystem)\\.([^.]+)\\.(.*)$";
  public static final String CONFIGID_PLACEHOLDER = "myCCure";

  private String clientName;
  private String clientId;
  private String clientVersion;
  private String endpoint;
  private Integer personnelPageSize;
  private Integer clearancePairPageSize;
  private String username;
  private String password;
  private String proxyUrl;
  private String proxyType;
  private String testUrlSuffix;
  private String testHttpMethod;
  private Integer testHttpResponseCode;
  private String testUrlResponseBodyRegex;

  private String accessToken;
  private String sessionId;

  @Override
  public void setConfigId(String configId) {
    super.setConfigId(configId);

    GrouperLoaderConfig grouperLoaderConfig = GrouperLoaderConfig.retrieveConfig();

    // Required: clientName, endpoint, username, password
    // Can't call propertyValueStringRequired here since needs to apply to blank configs
    // the property metadata will handle validation in base class validatePreSave()
    this.clientName = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "clientName");
    this.clientId = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "clientId");
    this.clientVersion = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "clientVersion");
    this.endpoint = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "endpoint");
    this.personnelPageSize = grouperLoaderConfig.propertyValueInt(getConfigItemPrefix() + "personnelPageSize", 2000);
    this.clearancePairPageSize = grouperLoaderConfig.propertyValueInt(getConfigItemPrefix() + "clearancePairPageSize", 2000);
    this.username = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "username");
    this.password = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "password");
    this.proxyUrl = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "proxyUrl");
    this.proxyType = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "proxyType");
    this.testUrlSuffix = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "testUrlSuffix");
    this.testHttpMethod = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "testHttpMethod");
    this.testHttpResponseCode = grouperLoaderConfig.propertyValueInt(getConfigItemPrefix() + "testHttpResponseCode");
    this.testUrlResponseBodyRegex = grouperLoaderConfig.propertyValueString(getConfigItemPrefix() + "testUrlResponseBodyRegex");
  }

  public String constructUrl(String uri) {
    return GrouperUtil.stripLastSlashIfExists(endpoint) + uri;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getSessionId() {
    return sessionId;
  }

  public void attachAuthenticationToHttpClient(GrouperHttpClient grouperHttpClient) {
    grouperHttpClient.addHeader("Access-Control-Expose-Headers", "session-id");
    grouperHttpClient.addHeader("session-id", sessionId);
    if (!GrouperUtil.isBlank(accessToken)) {
      grouperHttpClient.addUrlParameter("token", accessToken);
    }
  }

  // todo move class and make protected
  public void authenticate() {
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    String url = GrouperUtil.stripLastSlashIfExists(endpoint) + "/api/Authenticate/Login";

    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod("POST");
    grouperHttpClient.addHeader("Content-Type", "application/x-www-form-urlencoded");

    grouperHttpClient.addBodyParameter("UserName", username);
    grouperHttpClient.addBodyParameter("Password", password);
    grouperHttpClient.addBodyParameter("ClientName", clientName);
    if (!GrouperUtil.isBlank(clientId)) {
      grouperHttpClient.addBodyParameter("ClientID", clientId);
    }
    if (!GrouperUtil.isBlank(clientVersion)) {
      grouperHttpClient.addBodyParameter("ClientVersion", clientVersion);
    }
    //grouperHttpClient.assignBody(bodyNode.toString());

    grouperHttpClient.assignProxyUrl(proxyUrl);
    grouperHttpClient.assignProxyType(proxyType);

    grouperHttpClient.assignDoNotLogParameters("Password");

    grouperHttpClient.executeRequest();
    int code = grouperHttpClient.getResponseCode();
    String response = grouperHttpClient.getResponseBody();

    if (code == 200) {
      this.sessionId = grouperHttpClient.getResponseHeaders().get("session-id");

      if (!GrouperUtil.isBlank(response)) {
        try {
          JsonNode jsonObject = GrouperUtil.jsonJacksonNode(response);
          this.accessToken = jsonObject.asText();
        } catch (Exception e) {
          this.accessToken = null;
        }
      }
    } else {
      throw new RuntimeException("CCure authentication failed, code=" + code + ", response=[" + response + "]");
    }
  }

  // todo move class and make protected
  public void logout() {
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    String url = constructUrl("/api/Authenticate/Logout");

    grouperHttpClient.assignUrl(url);
    grouperHttpClient.assignGrouperHttpMethod("POST");

    attachAuthenticationToHttpClient(grouperHttpClient);

    grouperHttpClient.assignProxyUrl(proxyUrl);
    grouperHttpClient.assignProxyType(proxyType);

    grouperHttpClient.executeRequest();
    int code = grouperHttpClient.getResponseCode();
    String response = grouperHttpClient.getResponseBody();

    if (code == 200) {
      this.sessionId = null;
      this.accessToken = null;
    } else {
      throw new RuntimeException("CCure logout failed, code=" + code + ", response=[" + response + "]");
    }
  }

  @Override
  public List<String> test() throws UnsupportedOperationException {
    List<String> ret = new ArrayList<>();

    try {
      authenticate();
    } catch (Exception e) {
      ret.add("Error with login; " + e.getMessage());
      return ret;
    }

    if (!GrouperUtil.isBlank(testUrlSuffix)) {
      GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

      String url = endpoint;
      if (!StringUtils.isBlank(testUrlSuffix)) {
        url = GrouperUtil.stripLastSlashIfExists(endpoint) + "/" + GrouperUtil.stripFirstSlashIfExists(testUrlSuffix);
      }

      grouperHttpClient.assignUrl(url);
      grouperHttpClient.assignGrouperHttpMethod(GrouperUtil.isBlank(testHttpMethod) ? "GET" : testHttpMethod);

      if ("POST".equals(testHttpMethod)) {
        grouperHttpClient.addHeader("Content-Type", "application/json");
      }

      attachAuthenticationToHttpClient(grouperHttpClient);

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

    try {
      logout();
    } catch (Exception e) {
      ret.add("Error with logout; " + e.getMessage());
      return ret;
    }

    return ret;
  }

  public Integer getPersonnelPageSize() {
    return personnelPageSize;
  }

  public Integer getClearancePairPageSize() {
    return clearancePairPageSize;
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
    return PROPERTY_PREFIX + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return CONFIGID_REGEX;
  }

  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return CONFIGID_PLACEHOLDER;
  }
}
