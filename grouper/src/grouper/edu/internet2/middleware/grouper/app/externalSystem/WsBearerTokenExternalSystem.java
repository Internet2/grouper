package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class WsBearerTokenExternalSystem extends GrouperExternalSystem {

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

    String endpointProperty = configPrefix + "endpoint";
    String endpoint = config.propertyValueString(endpointProperty);
    if (GrouperUtil.isBlank(endpoint)) {
      ret.add("Undefined or blank property: " + endpointProperty);
    }

    String accessTokenPasswordProperty = configPrefix + "accessTokenPassword";
    String accessTokenPassword = config.propertyValueString(accessTokenPasswordProperty);
    if (GrouperUtil.isBlank(accessTokenPassword)) {
      ret.add("Undefined or blank property: " + accessTokenPasswordProperty);
    }

    String testUrlSuffixProperty = configPrefix + "testUrlSuffix";
    String testUrlSuffix = config.propertyValueString(testUrlSuffixProperty);
    if (!GrouperUtil.isBlank(testUrlSuffix)) {
      
      final String testHttpMethod = GrouperUtil.defaultIfBlank(config.propertyValueString(configPrefix + "testHttpMethod"), "GET");

      int testHttpResponseCode = GrouperUtil.intValue(config.propertyValueString(configPrefix + "testHttpResponseCode"), 200);

      String testUrlResponseBodyRegex = config.propertyValueString(configPrefix + "testUrlResponseBodyRegex");
      
      // we need to get another one
      HttpClient httpClient = new HttpClient();
      final String url = GrouperUtil.stripLastSlashIfExists(endpoint) + "/" + GrouperUtil.stripFirstSlashIfExists(testUrlSuffix);
      // this will change the method of the request
      HttpMethodBase httpMethod = new PostMethod(url) {
        @Override public String getName() { return testHttpMethod; }
      };
      
      httpMethod.addRequestHeader("Authorization", "Bearer " + accessTokenPassword);
      
      int code = -1;
      String response = null;

      try {
        code = httpClient.executeMethod(httpMethod);
        response = httpMethod.getResponseBodyAsString();
      } catch (Exception e) {
        ret.add("Error connecting to '" + url + "' " + GrouperUtil.getFullStackTrace(e));
      }

      if (code != testHttpResponseCode) {
        ret.add("Response code to " + url + " expecting " + testHttpResponseCode + " but received " + code);
      }
      
      if (!StringUtils.isBlank(testUrlResponseBodyRegex)) {
        Pattern pattern = Pattern.compile(testUrlResponseBodyRegex);
        Matcher matcher = pattern.matcher(response);
        if (!matcher.matches()) {
          ret.add("Response body from " + url + " expecting regex " + testUrlResponseBodyRegex + " but no match " + GrouperUtil.escapeHtml(response, true));
        }
      }
    }
    
    return ret;
  }



}
