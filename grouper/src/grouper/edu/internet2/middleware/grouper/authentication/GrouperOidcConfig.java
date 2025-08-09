package edu.internet2.middleware.grouper.authentication;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;

import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperProxyType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.morphString.Morph;

public class GrouperOidcConfig {

  /**
   * cache the configs
   */
  private static ExpirableCache<String, GrouperOidcConfig> grouperOidcConfigCache = new ExpirableCache<String, GrouperOidcConfig>(1);
  
  /**
   * retrieve from config or cache
   * @param clientConfigId
   * @return the config
   */
  public static GrouperOidcConfig retrieveFromConfigOrCache(String clientConfigId) {
    
    GrouperOidcConfig grouperOidcConfig = grouperOidcConfigCache.get(clientConfigId);
    if (grouperOidcConfig == null) {
      grouperOidcConfig = retrieveFromConfig(clientConfigId);
      grouperOidcConfigCache.put(clientConfigId, grouperOidcConfig);
    }
    
    return grouperOidcConfig;
  }
  
  public static enum GrouperOIDCClaimSource {
    userInfoEndpoint,
    
    idToken
  }
  
  private GrouperOIDCClaimSource claimSource;

  private Issuer issuer;

  private int maxClockSkew;

  private String responseType;
  
  
  public String getResponseType() {
    return responseType;
  }

  
  public void setResponseType(String responseType) {
    this.responseType = responseType;
  }

  private URI tokenEndpointUri = null;

  private URI userInfoUri;

  private String clientConfigId;
  
  public String getClientConfigId() {
    return clientConfigId;
  }
  
  public void setClientConfigId(String clientConfigId) {
    this.clientConfigId = clientConfigId;
  }

  private String clientId;

  
  public String getClientId() {
    return clientId;
  }

  
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  private String clientSecret;
  
  

  
  public String getClientSecret() {
    return clientSecret;
  }

  
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  private String configurationMetadataUri;
  
  
  public String getConfigurationMetadataUri() {
    return configurationMetadataUri;
  }

  
  public void setConfigurationMetadataUri(String configurationMetadataUri) {
    this.configurationMetadataUri = configurationMetadataUri;
  }

  /**
   * if enabled
   */
  private boolean enabled = false;
  
  
  
  
  public boolean isEnabled() {
    return enabled;
  }

  
  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  /**
   */
  private String redirectUri = null;

  
  public String getRedirectUri() {
    return redirectUri;
  }

  /**
   * will match everything after the domain slash including that slash
   * e.g. for https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.oidc
   * will match /grouper/grouperUi/app/UiV2Main.oidc
   */
  private static final Pattern oidcRedirectPattern = Pattern.compile("^http[^\\/]+\\/\\/[^\\/]+(\\/.*)$");
  
  private String redirectUriContext;
  
  public static String redirectUriContext(String redirectUri) {
    Matcher matcher = oidcRedirectPattern.matcher(redirectUri);
    GrouperUtil.assertion(matcher.matches(), "Invalid redirect uri: '"+redirectUri + "', should be similar to: https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.oidc");
    return matcher.group(1);
  }
  
  /**
   * will match everything after the domain slash including that slash
   * e.g. for https://grouper.school.edu/grouper/grouperUi/app/UiV2Main.oidc
   * will match /grouper/grouperUi/app/UiV2Main.oidc
   * @return redirect uri context
   */
  public String getRedirectUriContext() {
    if (this.redirectUriContext == null) {
      GrouperUtil.assertion(!StringUtils.isBlank(this.redirectUri), "redirectUri is required");
      this.redirectUriContext = redirectUriContext(this.redirectUri);
    }
    return redirectUriContext;
  }
  
  public void setRedirectUri(String redirectUri) {
    this.redirectUri = redirectUri;
  }

  /**
   */
  private String scope = null;
  
  /**
   * proxy requests here, e.g. https://server:1234
   */
  private String proxyUrl;
  
  
  /**
   * proxy requests here, e.g. https://server:1234
   * @return
   */
  public String getProxyUrl() {
    return proxyUrl;
  }

  /**
   * proxy requests here, e.g. https://server:1234
   * @param proxyUrl1
   */
  public void setProxyUrl(String proxyUrl1) {
    this.proxyUrl = proxyUrl1;
  }

  /**
   * socks or http
   */
  private GrouperProxyType proxyType;

  
  /**
   * socks or http
   * @return
   */
  public GrouperProxyType getProxyType() {
    return proxyType;
  }

  /**
   * socks or http
   * @param proxyType
   */
  public void setProxyType(GrouperProxyType proxyType) {
    this.proxyType = proxyType;
  }

  public String getScope() {
    return scope;
  }

  
  public void setScope(String scope) {
    this.scope = scope;
  }

  private URI authorizationEndpointUri;
  
  public URI getTokenEndpointUri() {
    return tokenEndpointUri;
  }


  
  public void setTokenEndpointUri(URI tokenEndpointUri) {
    this.tokenEndpointUri = tokenEndpointUri;
  }


  
  public URI getUserInfoUri() {
    return userInfoUri;
  }


  
  public void setUserInfoUri(URI userInfoUri) {
    this.userInfoUri = userInfoUri;
  }


  
  public URI getAuthorizationEndpointUri() {
    return authorizationEndpointUri;
  }


  
  public void setAuthorizationEndpointUri(URI authorizationEndpointUri) {
    this.authorizationEndpointUri = authorizationEndpointUri;
  }

  private OIDCProviderMetadata oidcProviderMetadata;

  private List<String> uiPathRegexes = new ArrayList<String>();

  private boolean useForUi;

  /**
   * -1 means forever
   */
  private int authnTimeoutSeconds;

  private String extraAuthorizeParams;
  
  
  public String getExtraAuthorizeParams() {
    return extraAuthorizeParams;
  }


  public int getAuthnTimeoutSeconds() {
    return authnTimeoutSeconds;
  }
  
  public boolean isUseForUi() {
    return useForUi;
  }


//  public OIDCProviderMetadata getOidcProviderMetadata() {
//    return oidcProviderMetadata;
//  }

  
  public List<String> getUiPathRegexes() {
    return uiPathRegexes;
  }


  
  public void setUiPathRegexes(List<String> uiPathRegexes) {
    this.uiPathRegexes = uiPathRegexes;
  }

  /**
   * dont hit it every single time
   */
  private static ExpirableCache<String, String> configIdToMetadataResponseBodyExpirable = new ExpirableCache<String, String>(1);

  private static Map<String, String> configIdToMetadataResponseBodyFailsafe = Collections.synchronizedMap(new HashMap<String, String>());

  private static final java.util.concurrent.ConcurrentHashMap<String,Object> metadataLocks = new java.util.concurrent.ConcurrentHashMap<>();

  private void retrieveMetadata() {
      
    String metadataBody = configIdToMetadataResponseBodyExpirable.get(this.getClientConfigId());
    
    boolean retrievedNew = false;

    if (StringUtils.isBlank(metadataBody)) {
      
      GrouperUtil.assertion(!StringUtils.isBlank(this.getClientConfigId()), "Client config id cannot be blank for oidc config");
      
      Object lock = metadataLocks.computeIfAbsent(this.getClientConfigId(), k -> new Object());
      
      synchronized (lock) {
        
        metadataBody = configIdToMetadataResponseBodyExpirable.get(this.getClientConfigId());
  
        if (StringUtils.isBlank(metadataBody)) {

          try {

            GrouperHttpClient request = new GrouperHttpClient()
                .assignProxyUrl(this.proxyUrl)
                .assignProxyType(this.proxyType)
                .assignUrl(this.configurationMetadataUri)
                .assignGrouperHttpMethod(GrouperHttpMethod.get)
                .executeRequest();
              
            GrouperUtil.assertion(request.getResponseCode() == 200, "Invalid oidc well known url: "+this.configurationMetadataUri+ ", response code: "+request.getResponseCode());
            
            metadataBody = request.getResponseBody();
            
            retrievedNew = true;

          } catch (RuntimeException re) {
            
            metadataBody = configIdToMetadataResponseBodyFailsafe.get(this.getClientConfigId());
            
            String error = "Error getting OIDC metadata for config '" + this.getClientConfigId()
              + "' from " + this.configurationMetadataUri;
            
            if (StringUtils.isBlank(metadataBody)) {
              GrouperUtil.injectInException(re, error);
              throw re;
            }
            
            LOG.error(error, re);
            
          }
          
        }

      }  
    }
    
    
    try {
      OIDCProviderMetadata providerMetadata = OIDCProviderMetadata.parse(metadataBody);
      this.oidcProviderMetadata = providerMetadata;
    } catch (Exception e) {
      String error = "Error getting metadata for oidc config: '" + this.getClientConfigId() + "'";
      throw new RuntimeException(error, e);
    }
    
    if (!StringUtils.isBlank(metadataBody) && retrievedNew) {
      configIdToMetadataResponseBodyExpirable.put(this.getClientConfigId(), metadataBody);
      configIdToMetadataResponseBodyFailsafe.put(this.getClientConfigId(), metadataBody);
    }
    

 
  }
  
  /**
   * retrieve from config or cache
   * @param externalSystemConfigId1
   * @return the config
   */
  private static GrouperOidcConfig retrieveFromConfig(String externalSystemConfigId) {

    GrouperOidcConfig grouperOidcConfig = new GrouperOidcConfig();
  
    grouperOidcConfig.clientConfigId = externalSystemConfigId;
    grouperOidcConfig.claimSource = GrouperOIDCClaimSource.valueOf(GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + externalSystemConfigId + ".claimSource", "userInfoEndpoint"));
    
    grouperOidcConfig.proxyUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + externalSystemConfigId + ".proxyUrl");
    grouperOidcConfig.proxyType = GrouperProxyType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + externalSystemConfigId + ".proxyType"), false);
    
    // # config id of the external system
    if(GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oidcExternalSystem." + externalSystemConfigId + ".useConfigurationMetadata", false)) {
      grouperOidcConfig.configurationMetadataUri = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".configurationMetadataUri");
      grouperOidcConfig.retrieveMetadata();
      
      if (grouperOidcConfig.claimSource == GrouperOIDCClaimSource.userInfoEndpoint) {
        grouperOidcConfig.userInfoUri = grouperOidcConfig.oidcProviderMetadata.getUserInfoEndpointURI();
      } else if (grouperOidcConfig.claimSource == GrouperOIDCClaimSource.idToken) {
        grouperOidcConfig.issuer = grouperOidcConfig.oidcProviderMetadata.getIssuer();
      }
      
      grouperOidcConfig.tokenEndpointUri = grouperOidcConfig.oidcProviderMetadata.getTokenEndpointURI();
      grouperOidcConfig.authorizationEndpointUri = grouperOidcConfig.oidcProviderMetadata.getAuthorizationEndpointURI();
    } else {
      
      if (grouperOidcConfig.claimSource == GrouperOIDCClaimSource.userInfoEndpoint) {
        //   
        //  # url to get the user info from the access token https://idp.pennkey.upenn.edu/idp/profile/oidc/userinfo
        //  # grouper.oidcExternalSystem.myOidcConfigId.userInfoUri =
        grouperOidcConfig.userInfoUri = URI.create(GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".userInfoUri"));
      } else if (grouperOidcConfig.claimSource == GrouperOIDCClaimSource.idToken) {
        grouperOidcConfig.issuer = new Issuer(GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".issuer"));
      }
      
      //  # url to decode the oidc code into an access token: https://idp.institution.edu/idp/profile/oidc/token
      //  # grouper.oidcExternalSystem.myOidcConfigId.tokenEndpointUri =
      grouperOidcConfig.tokenEndpointUri = URI.create(GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".tokenEndpointUri"));

      grouperOidcConfig.authorizationEndpointUri = URI.create(GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".authorizeUri"));
    }
    
    if (grouperOidcConfig.claimSource == GrouperOIDCClaimSource.idToken) {
      grouperOidcConfig.maxClockSkew = GrouperConfig.retrieveConfig().propertyValueInt("grouper.oidcExternalSystem." + externalSystemConfigId + ".maxClockSkew", 300);
    }

    //    
    //  # client id to authorize url
    //  # grouper.oidcExternalSystem.myOidcConfigId.clientId =
    grouperOidcConfig.clientId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".clientId");

    grouperOidcConfig.ws = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oidcExternalSystem." + externalSystemConfigId + ".useForWs", false);

    //   
    //  # secret to ws
    //  # grouper.oidcExternalSystem.myOidcConfigId.clientSecret =
    grouperOidcConfig.clientSecret = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".clientSecret");
    grouperOidcConfig.clientSecret = Morph.decryptIfFile(grouperOidcConfig.clientSecret);
    
    //   
    //  # if this oidc connector is enabled
    //  # grouper.oidcExternalSystem.myOidcConfigId.enabled =    
    grouperOidcConfig.enabled = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oidcExternalSystem." + externalSystemConfigId + ".enabled", true);

    
    //   
    //  # needed for retrieving an access token, e.g. https://my.app/someUrlBackFromIdp
    //  grouper.oidc.configId.redirectUri =
    grouperOidcConfig.redirectUri = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + externalSystemConfigId + ".redirectUri");

    //   
    //  # scope to retrieve from oidc, e.g. openid email profile
    //  grouper.oidc.configId.scope =
    grouperOidcConfig.scope = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".scope");

    //   
    //  # optional, could be in claim as "subjectSourceId", e.g. myPeople
    //  grouper.oidc.configId.subjectSourceId =
    grouperOidcConfig.subjectSourceId = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + externalSystemConfigId + ".subjectSourceId");

    //   
    //  # subjectId, subjectIdentifier, or subjectIdOrIdentifier
    //  grouper.oidc.configId.subjectIdType =
    grouperOidcConfig.subjectIdType = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".subjectIdType");
    
    grouperOidcConfig.responseType = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".oidcResponseType");

    //  # some claim name that has the subjectId / subjectIdentifier / subjectIdOrIdentifier in it.  e.g. employeeId
    //  grouper.oidc.configId.subjectIdClaimName =
    grouperOidcConfig.subjectIdClaimName = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + externalSystemConfigId + ".subjectIdClaimName", "preferred_username");
    
    //  # Use for UI?
    //  # {valueType: "boolean", defaultValue: "false", order: 16000 }
    //  # grouper.oidcExternalSystem.myOidcConfigId.useForUi =
    grouperOidcConfig.useForUi = GrouperConfig.retrieveConfig().propertyValueBoolean("grouper.oidcExternalSystem." + externalSystemConfigId + ".useForUi", false);

    //  # Comma separated list of path regexes to match the UI path to use for this external system. Escape commas with &#x2c;
    //  # If there are no regexes then this is the default OIDC for the UI
    //  # {valueType: "string", order: 16050, showEl: "${useForUi}" }
    //  # grouper.oidcExternalSystem.myOidcConfigId.uiPathRegexes =
    grouperOidcConfig.uiPathRegexes = uiPathRegexesForConfigId(externalSystemConfigId);

    //  # When the authentication times out
    //  # {valueType: "integer", order: 16051, showEl: "${useForUi}" }
    //  # grouper.oidcExternalSystem.myOidcConfigId.authnTimeoutSeconds =
    grouperOidcConfig.authnTimeoutSeconds = GrouperConfig.retrieveConfig().propertyValueInt("grouper.oidcExternalSystem." + externalSystemConfigId + ".authnTimeoutSeconds", -1);
    
    //  # Add extra params on the authorize url, e.g. prompt=login or max_age=0
    //  # {valueType: "string", order: 16054, showEl: "${useForUi}" }
    //  # grouper.oidcExternalSystem.myOidcConfigId.extraAuthorizeParams =
    grouperOidcConfig.extraAuthorizeParams = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + externalSystemConfigId + ".extraAuthorizeParams");
            
    return grouperOidcConfig;
  }
  
  public static List<String> uiPathRegexesForConfigId(String configId) {
    //  # Comma separated list of path regexes to match the UI path to use for this external system. Escape commas with &#x2c;
    //  # If there are no regexes then this is the default OIDC for the UI
    //  # {valueType: "string", order: 16050, showEl: "${useForUi}" }
    //  # grouper.oidcExternalSystem.myOidcConfigId.uiPathRegexes =
    String uiPathRegexesString = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + configId + ".uiPathRegexes");
    if (!StringUtils.isBlank(uiPathRegexesString)) {
      List<String> uiPathRegexesList = GrouperUtil.splitTrimToList(uiPathRegexesString, ",");
      for (int i=0;i<uiPathRegexesList.size();i++) {
        String uiPathRegex = uiPathRegexesList.get(i);
        uiPathRegex = StringUtils.replace(uiPathRegex, "&#x2c;", ",");
        uiPathRegexesList.set(i, uiPathRegex);
      }
      return uiPathRegexesList;
    }
    return new ArrayList<String>();
  }

   /**
   * some claim name that has the subjectId in it.  optional, can just label claim name as "subjectId", "subjectIdentifier", or "subjectIdOrIdentifier"
   */
  private String subjectIdClaimName = null;
  
  /**
   * subject id claim name
   * @return claim name
   */
  public String getSubjectIdClaimName() {
    return subjectIdClaimName;
  }

  /**
   * subject id claim name
   * @param subjectIdClaimName
   */
  public void setSubjectIdClaimName(String subjectIdClaimName) {
    this.subjectIdClaimName = subjectIdClaimName;
  }

  /**
   * subjectId, subjectIdentifier, or subjectIdOrIdentifier (optional)
   */
  private String subjectIdType = null;
  
  /**
   * subjectId, subjectIdentifier, or subjectIdOrIdentifier (optional)
   * @return subject id type
   */
  public String getSubjectIdType() {
    return subjectIdType;
  }

  /**
   * subjectId, subjectIdentifier, or subjectIdOrIdentifier (optional)
   * @param subjectIdType1
   */
  public void setSubjectIdType(String subjectIdType1) {
    this.subjectIdType = subjectIdType1;
  }

  /**
   * optional, could be in claim as "subjectSourceId"
   */
  private String subjectSourceId = null;

  private boolean ws;
  
  /**
   * optional, could be in claim as "subjectSourceId"
   * @return subject source id
   */
  public String getSubjectSourceId() {
    return subjectSourceId;
  }

  /**
   * optional, could be in claim as "subjectSourceId"
   * @param subjectSourceId1
   */
  public void setSubjectSourceId(String subjectSourceId1) {
    this.subjectSourceId = subjectSourceId1;
  }

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperOidcConfig.class);

  public boolean isWs() {
    return this.ws;
  }


  
  public GrouperOIDCClaimSource getClaimSource() {
    return claimSource;
  }


  
  public void setClaimSource(GrouperOIDCClaimSource claimSource) {
    this.claimSource = claimSource;
  }


  
  public Issuer getIssuer() {
    return issuer;
  }


  
  public void setIssuer(Issuer issuer) {
    this.issuer = issuer;
  }


  
  public int getMaxClockSkew() {
    return maxClockSkew;
  }


  
  public void setMaxClockSkew(int maxClockSkew) {
    this.maxClockSkew = maxClockSkew;
  }
}
