package edu.internet2.middleware.grouper.ws.security;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.authentication.GrouperOidc;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.subject.Subject;

public class WsGrouperOauthAuthentication implements WsCustomAuthentication {

  public WsGrouperOauthAuthentication() {
  }

  public static void main(String[] args) {
    
    GrouperStartup.startup();
    GrouperSession.startRootSession();
    
    // get employee number from access token
    String accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCIgO"; 
    // create a grouperOidc object
    GrouperOidc grouperOidc = new GrouperOidc();
    // set the access token
    grouperOidc.setAccessToken(accessToken);
    
//    // Create an AccessToken object with the token string
//    // You need to specify the AccessTokenType, e.g., Bearer
//    public (final String value, final long lifetime, final Scope scope) {
//
//    // lifetime seconds
    //Scope scope = new Scope("openid", "profile", "email");
    AccessToken accessTokenObject = new BearerAccessToken(accessToken);
    GrouperUtil.assignField(grouperOidc, "accessTokenObject", accessTokenObject);
    
    grouperOidc.assignExternalSystemConfigId("openai_chatgpt_rhsso");

    // decode the access token
    grouperOidc.decodeAccessToken();
    // find the subject
    Subject subject = grouperOidc.findSubject();
    // print the subject
    if (subject == null) {
      System.out.println("Subject is null");
    } else {
      System.out.println("Subject: " + GrouperUtil.subjectToString(subject));
    }
  }
  
  /** logger */
  private static final Log LOG = GrouperUtil.getLog(WsGrouperOauthAuthentication.class);

  @Override
  public String retrieveLoggedInSubjectId(HttpServletRequest httpServletRequest)
      throws WsInvalidQueryException {

    String fallbackClassName = null;

    // debug map
    Map<String, Object> debugMap = new LinkedHashMap<String, Object>();
    long startNanos = System.nanoTime();

    try {
      GrouperWsConfig grouperWsConfig = GrouperWsConfig.retrieveConfig();
  
      // get the grouperOauthConfigId parameter
      String grouperOauthConfigId = httpServletRequest.getParameter("grouperOidcConfigId");
  
      debugMap.put("grouperOidcConfigIdParam", grouperOauthConfigId);
  
      // if its blank then check for a version config id
      if (StringUtils.isBlank(grouperOauthConfigId)) {
  
        //get the url strings
        List<String> urlStrings = extractUrlStrings(httpServletRequest);
        // get version
        String version = urlStrings.size() > 0 ? urlStrings.get(0) : null;
        
        debugMap.put("urlVersion", version);
        
        try {
          GrouperVersion grouperVersion = GrouperVersion.valueOfIgnoreCase(version, false);
        } catch (Exception e) {
          version = null;
        }
  
        if (StringUtils.isNotBlank(version)) {
          // check for a config: ws.security.authn.oauth.config.whatever.oidcConfigId
          grouperOauthConfigId = grouperWsConfig.propertyValueString("ws.security.authn.oauth.clientVersion." + version + ".grouperOidcConfigId");
          debugMap.put("grouperOauthConfigIdVersion", grouperOauthConfigId);
        }
  
      }
  
      if (!StringUtils.isBlank(grouperOauthConfigId)) {
        // get the authorization header
        String authorizationHeader = httpServletRequest.getHeader("Authorization");
        // extract the bearer token
        String accessToken = null;
        if (StringUtils.isNotBlank(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
          accessToken = authorizationHeader.substring("Bearer ".length());
          if (grouperWsConfig.propertyValueBoolean("ws.security.authn.oauth.debugAccessTokens", false)) {
            debugMap.put("accessToken", accessToken);
          } else {
            debugMap.put("accessToken", accessToken.substring(0, 10) + "**********");
          }
          GrouperOidc grouperOidc = new GrouperOidc().assignExternalSystemConfigId(grouperOauthConfigId);
          
          grouperOidc.setAccessToken(accessToken);
          
          AccessToken accessTokenObject = new BearerAccessToken(accessToken);

          grouperOidc.setAccessTokenObject(accessTokenObject);
          
          grouperOidc.decodeAccessToken();
          
          Subject subject = grouperOidc.findSubject();
  
          if (subject == null) {
            debugMap.put("subject", "null");
          } else {
            debugMap.put("subject", GrouperUtil.subjectToString(subject));
            return subject.getId();
          }
        }
      }
  
      // if its not blank then check the config
      fallbackClassName = grouperWsConfig.propertyValueString("ws.security.authn.oauth.fallback.class");
      
    } finally {
      if (LOG.isDebugEnabled()) {
        debugMap.put("elapsedMillis", (System.nanoTime() - startNanos) / 1000000);
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
    if (!StringUtils.isEmpty(fallbackClassName)) {
      Class<? extends WsCustomAuthentication> theClass = GrouperUtil.forName(fallbackClassName);

      WsCustomAuthentication wsAuthentication = GrouperUtil.newInstance(theClass);

      return wsAuthentication.retrieveLoggedInSubjectId(httpServletRequest);
    }
    
    return null;
  }

  
  /**
   * take a request and get the list of url strings for the rest web service
   * @see #extractUrlStrings(String)
   * @param request is the request to get the url strings out of
   * @return the list of url strings
   */
  private static List<String> extractUrlStrings(HttpServletRequest request) {
    String requestResourceFull = request.getRequestURI();
    return extractUrlStrings(requestResourceFull);
  }

  /**
   * <pre>
   * take a request uri and break up the url strings not including the app name or servlet
   * this does not include the url params (if applicable)
   * if the input is: grouper-ws/servicesRest/v1_3_000/groups/members
   * then the result is a list of size 3: {"v1_3_000", "group", "members"}
   * 
   * </pre>
   * @param requestResourceFull
   * @return the url strings
   */
  private static List<String> extractUrlStrings(String requestResourceFull) {
    String[] requestResources = StringUtils.split(requestResourceFull, '/');
    List<String> urlStrings = new ArrayList<String>();
    //loop through and decode
    int index = 0;
    for (String requestResource : requestResources) {
      //skip the app name and lite servlet
      if (index++ < 2) {
        continue;
      }
      //unescape the url encoding
      urlStrings.add(GrouperUtil.escapeUrlDecode(requestResource));
    }
    return urlStrings;
  }

}
