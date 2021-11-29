package edu.internet2.middleware.grouper.authentication;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class GrouperOidc {

  public static void main(String[] args) {

//    System.out.println(Morph.encrypt(""));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.testOidcExt.userInfoUri", 
        GrouperUtil.readFileIntoString(new File("C:\\git\\grouper_prod\\grouper\\temp\\oidc\\userInfoUri.txt")));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.testOidcExt.clientId", 
        GrouperUtil.readFileIntoString(new File("C:\\git\\grouper_prod\\grouper\\temp\\oidc\\clientId.txt")));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.testOidcExt.clientSecret", 
        GrouperUtil.readFileIntoString(new File("C:\\git\\grouper_prod\\grouper\\temp\\oidc\\clientSecretEncrypted.txt")));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.testOidcExt.tokenEndpointUri", 
        GrouperUtil.readFileIntoString(new File("C:\\git\\grouper_prod\\grouper\\temp\\oidc\\tokenEndpointUri.txt")));
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.testOidc.oidcExternalSystemConfigId", "testOidcExt");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.testOidc.redirectUri", 
        GrouperUtil.readFileIntoString(new File("C:\\git\\grouper_prod\\grouper\\temp\\oidc\\redirectUri.txt")));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.testOidc.scope", 
        GrouperUtil.readFileIntoString(new File("C:\\git\\grouper_prod\\grouper\\temp\\oidc\\scope.txt")));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.testOidc.subjectSourceId", "pennperson");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.testOidc.subjectIdType", "subjectId");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.testOidc.subjectIdClaimName", "employee_number");

    GrouperOidc grouperOidc = new GrouperOidc();
    grouperOidc.assignBearerTokenHeader("Bearer oidc_testOidc_" 
        + GrouperUtil.readFileIntoString(new File("C:\\git\\grouper_prod\\grouper\\temp\\oidc\\oidcCode.txt")));
    Subject subject = grouperOidc.decode();
    System.out.println(SubjectHelper.getPretty(subject));
  }
  
  /**
   * string like: 
   * Bearer jwtTrusted_configId_abc123def456
   */
  private String bearerTokenHeader = null;

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperOidc.class);
  
  /**
   * Authorization : Bearer oidc_configId_abc123def456
   * bearer token pattern
   */
  private static Pattern bearerTokenPattern = Pattern.compile("^[bB]earer oidc_([^_]+)_(.+)$");
  
  /**
   * Authorization : Bearer oidcWithRedirectUri_configId_lmn432rew987_abc123def456    (lmn432rew987 is base64 redirect uri)
   * bearer token pattern
   */
  private static Pattern bearerTokenPatternWithRedirect = Pattern.compile("^[bB]earer oidcWithRedirectUri_([^_]+)_([^_]+)_(.+)$");
  
  
  /**
   * string like:
   * Bearer jwtTrusted_configId_abc123def456
   * @param theBearerTokenHeader
   * @return this for chaining
   */
  public GrouperOidc assignBearerTokenHeader(String theBearerTokenHeader) {
    this.bearerTokenHeader = theBearerTokenHeader;
    return this;
  }
  
  /**
   * result of oidc code
   */
  private GrouperOidcResult grouperOidcResult = null;
  
  
  
  /**
   * result of decoding jwt
   * @return result
   */
  public GrouperOidcResult getGrouperOidcResult() {
    return this.grouperOidcResult;
  }

  /**
   * access token from code
   */
  private String accessToken = null;

  /**
   * 
   */
  private String oidcCodeString;

  private String redirectUri;

  private GrouperOidcConfig grouperOidcConfig;
  
  /**
   * access token from code
   * @return
   */
  public String getAccessToken() {
    return accessToken;
  }

  /**
   * access token from code
   * @param accessToken
   */
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  private Map<String, String> accessTokenAttributes;
  
  /**
   * get an access token, unwrap it and get the attributes from the access token
   * e.g. 
   * sub -> mchyzer, name -> Hyzer, Chris, employee_number -> 10021368, given_name -> Chris, family_name -> Hyzer, email -> mchyzer@upenn.edu, username -> mchyzer@upenn.edu
   * @return the map of attributes
   */
  public void decodeAccessToken() {
    
    GrouperUtil.assertion(this.grouperOidcConfig != null, "config is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.accessToken), "accessToken is required");

    GrouperUtil.assertion(!StringUtils.isBlank(this.grouperOidcConfig.getUserInfoUri()), "userInfoUri is required");

    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();

    grouperHttpClient.assignUrl(this.grouperOidcConfig.getUserInfoUri());
    grouperHttpClient.addBodyParameter("access_token", this.accessToken);
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
    
    grouperHttpClient.assignProxyUrl(this.grouperOidcConfig.getProxyUrl());
    grouperHttpClient.assignProxyType(this.grouperOidcConfig.getProxyType());
    
    grouperHttpClient.executeRequest();

    String responseBody = grouperHttpClient.getResponseBody();
    this.debugMap.put("decodeAccessTokenResponseBody", responseBody);
    
    int responseCode = grouperHttpClient.getResponseCode();
    this.debugMap.put("decodeAccessTokenResponseCode", responseCode);

    if (responseCode != 200) {
      throw new RuntimeException("Error: " + responseCode + ", " + responseBody);
    }
    
    JsonNode responseBodyNode = GrouperUtil.jsonJacksonNode(responseBody);
    // {"sub":"mchyzer","name":"Hyzer, Chris","given_name":"Chris","family_name":"Hyzer","email":"mchyzer@upenn.edu","username":"mchyzer@upenn.edu"}

    this.accessTokenAttributes = new TreeMap<String, String>();
    
    Iterator<String> fieldNameIterator = responseBodyNode.fieldNames();
    while (fieldNameIterator.hasNext()) {
      
      String fieldName = fieldNameIterator.next();
      String fieldValue = GrouperUtil.jsonJacksonGetString(responseBodyNode, fieldName);
      this.accessTokenAttributes.put(fieldName, fieldValue);
    }
    
  }


  /**
   * get an access token from the code, assign to field in this object
   */
  public void retrieveAccessToken() {
    
    GrouperUtil.assertion(this.grouperOidcConfig != null, "config is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.oidcCodeString), "code is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.grouperOidcConfig.getClientId()), "clientId is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.grouperOidcConfig.getClientSecret()), "clientSecret is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.redirectUri), "redirectUri is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.grouperOidcConfig.getTokenEndpointUri()), "tokenEndpoint is required");

    try {
      // Construct the code grant from the code obtained from the authz endpoint
      // and the original callback URI used at the authz endpoint
      AuthorizationCode authorizationCode = new AuthorizationCode(this.oidcCodeString);
      URI callback = new URI(this.redirectUri);
      AuthorizationGrant codeGrant = new AuthorizationCodeGrant(authorizationCode, callback);
  
      // The credentials to authenticate the client at the token endpoint
      ClientID clientIdObject = new ClientID(this.grouperOidcConfig.getClientId());
      Secret clientSecretObject = new Secret(this.grouperOidcConfig.getClientSecret());
      ClientAuthentication clientAuth = new ClientSecretBasic(clientIdObject, clientSecretObject);
  
      // The token endpoint
      URI tokenEndpointUri = new URI(this.grouperOidcConfig.getTokenEndpointUri());
  
      // Make the token request
      TokenRequest request = new TokenRequest(tokenEndpointUri, clientAuth, codeGrant);
  
      TokenResponse response = TokenResponse.parse(request.toHTTPRequest().send());

      this.debugMap.put("tokenServiceSuccess", response.indicatesSuccess());
      
      if (! response.indicatesSuccess()) {
        // We got an error response...
        TokenErrorResponse errorResponse = response.toErrorResponse();
        
        String tokenServiceError = errorResponse.getErrorObject().getHTTPStatusCode() + ": " + errorResponse.getErrorObject().getDescription();

        this.debugMap.put("tokenServiceError", tokenServiceError);

        throw new RuntimeException(tokenServiceError);
      }

      AccessTokenResponse successResponse = response.toSuccessResponse();

      // Get the access token, the server may also return a refresh token
      AccessToken accessTokenObject = successResponse.getTokens().getAccessToken();

      this.accessToken = accessTokenObject.getValue();

      this.debugMap.put("accessToken", StringUtils.abbreviate(this.accessToken, 8));

    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    }

  }

  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

  private String configId;

  /**
   * 
   * @return the subject
   */
  public Subject decode() {

    long startNanos = System.nanoTime();

    try {
      
      this.retrieveCodeFromHeader();
      
      this.retrieveAccessToken();
      
      this.decodeAccessToken();

      this.findSubject();
      
      return subject;
    } catch (Exception e) {
      debugMap.put("exception", ExceptionUtils.getFullStackTrace(e));
      if (e instanceof RuntimeException) {
        throw (RuntimeException)e;
      }
      throw new RuntimeException(e);
    } finally {
      
      debugMap.put("tookMs", (System.nanoTime() - startNanos) / 1000000);
      if (debugMap.get("exception") != null) {
        LOG.error(GrouperUtil.mapToString(debugMap));
      } else if (LOG.isDebugEnabled()) {
        LOG.debug(GrouperUtil.mapToString(debugMap));
      }
    }
  }

  private void retrieveCodeFromHeader() {
    
    if (StringUtils.isBlank(this.bearerTokenHeader)) {
      this.grouperOidcResult = GrouperOidcResult.ERROR_MISSING_TOKEN;
      throw new RuntimeException("bearerTokenHeader is required");
    }

    boolean uriPattern = false;
    
    debugMap.put("bearerTokenHeader", StringUtils.abbreviate(this.bearerTokenHeader, 50));
    
    Matcher matcher = bearerTokenPattern.matcher(this.bearerTokenHeader);

    if (!matcher.matches()) {
      matcher = bearerTokenPatternWithRedirect.matcher(this.bearerTokenHeader);
      uriPattern = true;
    }

    if (!matcher.matches()) {
      this.grouperOidcResult = GrouperOidcResult.ERROR_TOKEN_INVALID;
      throw new RuntimeException("bearerTokenHeader is invalid!");
    }

    debugMap.put("uriPattern", uriPattern);

    this.configId = matcher.group(1);
    debugMap.put("configId", configId);
    
    this.grouperOidcConfig = GrouperOidcConfig.retrieveFromConfigOrCache(configId);
    
    if (grouperOidcConfig == null) {
      throw new RuntimeException("Cant find oidc config: '" + configId + "'");
    }
    
    this.oidcCodeString = matcher.group(uriPattern ? 3 : 2);

    debugMap.put("oidcCode", StringUtils.abbreviate(this.oidcCodeString, 8));

    if (uriPattern) {
      this.redirectUri = matcher.group(2);
      // this is base64 encoded
      this.redirectUri = new String(Base64.decodeBase64(this.redirectUri));
    } else {
      this.redirectUri = grouperOidcConfig.getRedirectUri();
    }
    
    debugMap.put("redirectUri", this.redirectUri);
  }

  private Subject subject = null;
  
  private void findSubject() {
    
    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      
      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        String subjectSourceId = grouperOidcConfig.getSubjectSourceId();
        
        debugMap.put("subjectSourceId", subjectSourceId);

        {
          String subjectIdClaimName = StringUtils.isBlank(grouperOidcConfig.getSubjectIdType()) ? "subjectId" : null;
          if (StringUtils.equals(grouperOidcConfig.getSubjectIdType(), "subjectId") && !StringUtils.isBlank(grouperOidcConfig.getSubjectIdClaimName())) {
            
            subjectIdClaimName = grouperOidcConfig.getSubjectIdClaimName();
            
          }

          if (!StringUtils.isBlank(subjectIdClaimName)) {
           
            debugMap.put("subjectIdClaimName", subjectIdClaimName);
            
            String subjectId = GrouperOidc.this.accessTokenAttributes.get(subjectIdClaimName);
            if (!StringUtils.isBlank(subjectId)) {
              debugMap.put("subjectId", subjectId);
              if (StringUtils.isBlank(subjectSourceId)) {
                subject = SubjectFinder.findById(subjectId, false);
              } else {
                subject = SubjectFinder.findByIdAndSource(subjectId, subjectSourceId, false);
              }
            }
            
          }
        }

        {
          String subjectIdentifierClaimName = StringUtils.isBlank(grouperOidcConfig.getSubjectIdType()) ? "subjectIdentifier" : null;
          if (StringUtils.equals(grouperOidcConfig.getSubjectIdType(), "subjectIdentifier") && !StringUtils.isBlank(grouperOidcConfig.getSubjectIdClaimName())) {
            
            subjectIdentifierClaimName = grouperOidcConfig.getSubjectIdClaimName();
            
          }

          if (!StringUtils.isBlank(subjectIdentifierClaimName)) {
           
            debugMap.put("subjectIdentifierClaimName", subjectIdentifierClaimName);
            String subjectIdentifier = GrouperOidc.this.accessTokenAttributes.get(subjectIdentifierClaimName);
            if (!StringUtils.isBlank(subjectIdentifier)) {
              
              debugMap.put("subjectIdentifier", subjectIdentifier);
              if (StringUtils.isBlank(subjectSourceId)) {
                subject = SubjectFinder.findByIdentifier(subjectIdentifier, false);
              } else {
                subject = SubjectFinder.findByIdentifierAndSource(subjectIdentifier, subjectSourceId, false);
              }
            }
            
          }
        }

        {
          String subjectIdOrIdentifierClaimName = StringUtils.isBlank(grouperOidcConfig.getSubjectIdType()) ? "subjectIdOrIdentifier" : null;
          if (StringUtils.equals(grouperOidcConfig.getSubjectIdType(), "subjectIdOrIdentifier") && !StringUtils.isBlank(grouperOidcConfig.getSubjectIdClaimName())) {
            
            subjectIdOrIdentifierClaimName = grouperOidcConfig.getSubjectIdClaimName();
            
          }

          if (!StringUtils.isBlank(subjectIdOrIdentifierClaimName)) {
           
            debugMap.put("subjectIdOrIdentifierClaimName", subjectIdOrIdentifierClaimName);
            
            String subjectIdOrIdentifier = GrouperOidc.this.accessTokenAttributes.get(subjectIdOrIdentifierClaimName);
            if (!StringUtils.isBlank(subjectIdOrIdentifier)) {

              debugMap.put("subjectIdOrIdentifier", subjectIdOrIdentifier);
              if (StringUtils.isBlank(subjectSourceId)) {
                subject = SubjectFinder.findByIdOrIdentifier(subjectIdOrIdentifier, false);
              } else {
                subject = SubjectFinder.findByIdOrIdentifierAndSource(subjectIdOrIdentifier, subjectSourceId, false);
              }
            }
          }
        }

        debugMap.put("subjectFound", subject != null);

        return null;
      }
    });
    
  }
}
