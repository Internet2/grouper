package edu.internet2.middleware.grouper.authentication;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.JWTClaimsSetVerifier;
import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ErrorObject;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.SerializeException;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponse;
import com.nimbusds.openid.connect.sdk.OIDCTokenResponseParser;
import com.nimbusds.openid.connect.sdk.UserInfoErrorResponse;
import com.nimbusds.openid.connect.sdk.UserInfoRequest;
import com.nimbusds.openid.connect.sdk.UserInfoResponse;
import com.nimbusds.openid.connect.sdk.UserInfoSuccessResponse;
import com.nimbusds.openid.connect.sdk.validators.IDTokenClaimsVerifier;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.authentication.GrouperOidcConfig.GrouperOIDCClaimSource;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.subj.SubjectHelper;
import edu.internet2.middleware.grouper.util.GrouperProxyBean;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.subject.Subject;
import net.minidev.json.JSONObject;

public class GrouperOidc {

  private static ExpirableCache<Boolean, String> oidcConfigIdCache = new ExpirableCache<Boolean, String>(5);

  public static String externalSystemConfigIdForUi() {
  
    String externalSystemConfigIdForUi = oidcConfigIdCache.get(Boolean.TRUE);
    
    if(externalSystemConfigIdForUi == null) {
      synchronized (oidcConfigIdCache) {
        externalSystemConfigIdForUi = oidcConfigIdCache.get(Boolean.TRUE);
        if (externalSystemConfigIdForUi == null) {
          Pattern pattern = Pattern.compile("^grouper\\.oidcExternalSystem\\.(.*)\\.clientId$");
          Set<String> configIds = GrouperConfig.retrieveConfig().propertyConfigIds(pattern);
          
          for (String configId: GrouperUtil.nonNull(configIds)) {
            if (GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oidcExternalSystem."+configId+".useForUi", false) && 
                GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oidcExternalSystem."+configId+".enabled", true)) {
              GrouperUtil.assertion(StringUtils.isBlank(externalSystemConfigIdForUi), "Multiple OIDC external systems cannot be enabled for UI at the same time: "+externalSystemConfigIdForUi +" ,"+configId);
              externalSystemConfigIdForUi = configId;
            }
          }
          oidcConfigIdCache.put(Boolean.TRUE, GrouperUtil.defaultString(externalSystemConfigIdForUi));
        }
        
      }
      
    }
    
    return externalSystemConfigIdForUi;

  }
  
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
  
  private Nonce expectedNonce = null;
  
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
  
  
  public GrouperOidcConfig getGrouperOidcConfig() {
    return grouperOidcConfig;
  }

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

  private Map<String, String> userInfoAttributes;
  private Map<String, String> idTokenAttributes;

  private AccessToken accessTokenObject;
  
  /**
   * get an access token, unwrap it and get the attributes from the access token
   * e.g. 
   * sub -> mchyzer, name -> Hyzer, Chris, employee_number -> 10021368, given_name -> Chris, family_name -> Hyzer, email -> mchyzer@upenn.edu, username -> mchyzer@upenn.edu
   * @return the map of attributes
   */
  public void decodeAccessToken() {
    
    UserInfoRequest userInfoReq = new UserInfoRequest(
        this.grouperOidcConfig.getUserInfoUri(),
        (BearerAccessToken) this.accessTokenObject);
    
    HTTPResponse userInfoHTTPResp = null;
    try {
      HTTPRequest httpRequest = userInfoReq.toHTTPRequest();
      
      GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(this.grouperOidcConfig.getProxyType(), 
          this.grouperOidcConfig.getProxyUrl(), this.grouperOidcConfig.getTokenEndpointUri().toString());

      if (grouperProxyBean != null) {
        Proxy proxy = grouperProxyBean.retrieveProxy();
        httpRequest.setProxy(proxy);
      }
      
      userInfoHTTPResp = httpRequest.send();
      
    } catch (SerializeException | IOException e) {
     throw new RuntimeException(e);
    }

    UserInfoResponse userInfoResponse = null;
    try {
      userInfoResponse = UserInfoResponse.parse(userInfoHTTPResp);
    } catch (ParseException e) {
      throw new RuntimeException(e);
    }

    if (userInfoResponse instanceof UserInfoErrorResponse) {
      ErrorObject error = ((UserInfoErrorResponse) userInfoResponse).getErrorObject();
      throw new RuntimeException("Error: "+error.toString());
    }

    UserInfoSuccessResponse successResponse = (UserInfoSuccessResponse) userInfoResponse;
    JSONObject claims = successResponse.getUserInfo().toJSONObject();
  
    this.userInfoAttributes = new TreeMap<>();
    for (String fieldName : claims.keySet()) {
      String fieldValue = claims.getAsString(fieldName);
      this.userInfoAttributes.put(fieldName, fieldValue);
    }
    
  }

  private HTTPResponse tokenEndpointHTTPResponse;

  private void callTokenEndpoint() {

    GrouperUtil.assertion(this.grouperOidcConfig != null, "config is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.oidcCodeString), "code is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.grouperOidcConfig.getClientId()), "clientId is required");
    GrouperUtil.assertion(!StringUtils.isBlank(this.grouperOidcConfig.getClientSecret()), "clientSecret is required");
    
    String redirectUriLocal = this.redirectUri;
    
    if (StringUtils.isBlank(redirectUriLocal)) {
      redirectUriLocal = this.grouperOidcConfig.getRedirectUri();
    }
    
    GrouperUtil.assertion(!StringUtils.isBlank(redirectUriLocal), "redirectUri is required");
    GrouperUtil.assertion(this.grouperOidcConfig.getTokenEndpointUri() != null, "tokenEndpoint is required");

    try {
      // Construct the code grant from the code obtained from the authz endpoint
      // and the original callback URI used at the authz endpoint
      AuthorizationCode authorizationCode = new AuthorizationCode(this.oidcCodeString);
      URI callback = new URI(redirectUriLocal);
      AuthorizationGrant codeGrant = new AuthorizationCodeGrant(authorizationCode, callback);
  
      // The credentials to authenticate the client at the token endpoint
      ClientID clientIdObject = new ClientID(this.grouperOidcConfig.getClientId());
      Secret clientSecretObject = new Secret(this.grouperOidcConfig.getClientSecret());
      ClientAuthentication clientAuth = new ClientSecretBasic(clientIdObject, clientSecretObject);
  
      // The token endpoint
      URI tokenEndpointUri = this.grouperOidcConfig.getTokenEndpointUri();
  
      // Make the token request
      TokenRequest request = new TokenRequest(tokenEndpointUri, clientAuth, codeGrant);
      
      GrouperProxyBean grouperProxyBean = GrouperProxyBean.proxyConfig(this.grouperOidcConfig.getProxyType(), 
          this.grouperOidcConfig.getProxyUrl(), tokenEndpointUri.toString());

      HTTPRequest httpRequest = request.toHTTPRequest();
      if (grouperProxyBean != null) {
        Proxy proxy = grouperProxyBean.retrieveProxy();
        httpRequest.setProxy(proxy);
      }
      
      tokenEndpointHTTPResponse = httpRequest.send();
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    }

  }

  /**
   * get an access token from the code, assign to field in this object
   */
  public void retrieveAccessToken() {

    try {
      callTokenEndpoint();
      
      TokenResponse response = TokenResponse.parse(this.tokenEndpointHTTPResponse);

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
      this.accessTokenObject = accessTokenObject;

      this.debugMap.put("accessToken", StringUtils.abbreviate(this.accessToken, 8));

    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    }

  }
  
  /**
   * get an id token from the code, assign to field in this object
   */
  public void retrieveIdToken() {

    try {
      callTokenEndpoint();
      
      TokenResponse response = OIDCTokenResponseParser.parse(this.tokenEndpointHTTPResponse);
      
      this.debugMap.put("tokenServiceSuccess", response.indicatesSuccess());
      
      if (! response.indicatesSuccess()) {
        // We got an error response...
        TokenErrorResponse errorResponse = response.toErrorResponse();
        
        String tokenServiceError = errorResponse.getErrorObject().getHTTPStatusCode() + ": " + errorResponse.getErrorObject().getDescription();

        this.debugMap.put("tokenServiceError", tokenServiceError);

        throw new RuntimeException(tokenServiceError);
      }

      OIDCTokenResponse successResponse = (OIDCTokenResponse) response.toSuccessResponse();

      // Get the id token
      JWT idTokenJWT = successResponse.getOIDCTokens().getIDToken();
      
      // verify it
      JWTClaimsSet jwtClaimsSet = idTokenJWT.getJWTClaimsSet();
      JWTClaimsSetVerifier<?> claimsVerifier = new IDTokenClaimsVerifier(this.grouperOidcConfig.getIssuer(), new ClientID(this.grouperOidcConfig.getClientId()), this.expectedNonce, this.grouperOidcConfig.getMaxClockSkew());
      claimsVerifier.verify(jwtClaimsSet, null);
            
      this.idTokenAttributes = new TreeMap<>();
      for (String fieldName : jwtClaimsSet.getClaims().keySet()) {
        Object fieldValueObject = jwtClaimsSet.getClaim(fieldName);
            
        if (fieldValueObject instanceof String || 
            fieldValueObject instanceof Integer ||
            fieldValueObject instanceof Long) {
          this.idTokenAttributes.put(fieldName, fieldValueObject.toString());
        }
      }      
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException("error", e);
    }

  }
  
  public void retrieveAndParseTokens() {
    if (this.grouperOidcConfig.getClaimSource() == GrouperOIDCClaimSource.userInfoEndpoint) {
      this.retrieveAccessToken();
      this.decodeAccessToken();
    } else if (this.grouperOidcConfig.getClaimSource() == GrouperOIDCClaimSource.idToken) {
      this.retrieveIdToken();
    } else {
      throw new RuntimeException("Unexpected claim source: " + this.grouperOidcConfig.getClaimSource());
    }
  }

  private Map<String, Object> debugMap = new LinkedHashMap<String, Object>();

//  private String externalSystemConfigId;

  /**
   * 
   * @return the subject
   */
  public Subject decode() {

    long startNanos = System.nanoTime();

    try {
      
      this.retrieveCodeFromHeader();
      
      this.retrieveAndParseTokens();
      
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
  
  public GrouperOidc assignExternalSystemConfigId(String clientConfigId) {
    
    this.grouperOidcConfig = GrouperOidcConfig.retrieveFromConfigOrCache(clientConfigId);
    
    if (grouperOidcConfig == null) {
      throw new RuntimeException("Cant find oidc config: '" + clientConfigId + "'");
    }
    
    return this;
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

    this.assignExternalSystemConfigId(matcher.group(1));
    
    if (this.ws && !grouperOidcConfig.isWs()) {
      throw new RuntimeException(matcher.group(1) + " is not enabled for ws in the external system.");
    }
//    debugMap.put("configId", externalSystemConfigId);
    
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
  
  
  /**
   * is this for WS or not
   */
  private boolean ws;

  
  /**
   * @deprecated
   */
  public String generateLoginUrl() {
    return generateLoginUrl(null);
  }
  
  public String generateLoginUrl(HttpServletRequest httpServletRequest) {
    
    try {
   // Generate random state string for pairing the response to the request
      State state = new State();
      // Generate nonce
      Nonce nonce = new Nonce();
      // Specify scope
      Scope scope = Scope.parse(grouperOidcConfig.getScope());
      
      // Compose the request
      AuthenticationRequest authenticationRequest = new AuthenticationRequest(
      grouperOidcConfig.getAuthorizationEndpointUri(),
      new ResponseType(grouperOidcConfig.getResponseType()),
      scope, new ClientID(grouperOidcConfig.getClientId()), new URI(grouperOidcConfig.getRedirectUri()), state, nonce);

      URI authReqURI = authenticationRequest.toURI();
      
      if (httpServletRequest != null) {
        httpServletRequest.getSession().setAttribute("oidcNonce", nonce);
      }
      
      return authReqURI.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }
  
  
  public String retrieveResponseType() {
    return grouperOidcConfig.getResponseType(); 
  }
  
  public String findSubjectClaim() {
    
    String subjectIdClaimName = null;
    if (!StringUtils.isBlank(grouperOidcConfig.getSubjectIdClaimName())) {
      
      subjectIdClaimName = grouperOidcConfig.getSubjectIdClaimName();
      
    }

    if (!StringUtils.isBlank(subjectIdClaimName)) {
     
      debugMap.put("subjectIdClaimName", subjectIdClaimName);
      
      String subjectId = this.getClaimSourceAttributes().get(subjectIdClaimName);
      return subjectId;
      
    }
    
    return null;
    
  }
  
  public Map<String, String> getClaimSourceAttributes() {
    if (this.grouperOidcConfig.getClaimSource() == GrouperOIDCClaimSource.userInfoEndpoint) {
      return this.userInfoAttributes;
    }
    
    if (this.grouperOidcConfig.getClaimSource() == GrouperOIDCClaimSource.idToken) {
      return this.idTokenAttributes;
    }
    
    throw new RuntimeException("Unexpected claim source: " + this.grouperOidcConfig.getClaimSource());
  }
  
  public Subject findSubject() {
    
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
            
            String subjectId = GrouperOidc.this.getClaimSourceAttributes().get(subjectIdClaimName);
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
            String subjectIdentifier = GrouperOidc.this.getClaimSourceAttributes().get(subjectIdentifierClaimName);
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
            
            String subjectIdOrIdentifier = GrouperOidc.this.getClaimSourceAttributes().get(subjectIdOrIdentifierClaimName);
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
    
    return this.subject;
    
  }

  public GrouperOidc assignAuthorizationCode(String authorizationCode) {
    this.oidcCodeString = authorizationCode;
    GrouperUtil.assertion(authorizationCode == null || !authorizationCode.contains("&state="), "Only the code should be assigned to the authorization code, not the state too!");
    return this;
  }

  public GrouperOidc assignWs(boolean isWs) {
    this.ws = isWs;
    return this;
  }
  
  public GrouperOidc assignExpectedNonce(Nonce expectedNonce) {
    this.expectedNonce = expectedNonce;
    return this;
  }
}
