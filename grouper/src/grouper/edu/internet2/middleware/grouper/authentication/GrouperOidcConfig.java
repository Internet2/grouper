package edu.internet2.middleware.grouper.authentication;

import org.apache.commons.logging.Log;

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

  private String responseType;
  
  
  public String getResponseType() {
    return responseType;
  }

  
  public void setResponseType(String responseType) {
    this.responseType = responseType;
  }

  private String tokenEndpointUri = null;

  public String getTokenEndpointUri() {
    return tokenEndpointUri;
  }
  
  public void setTokenEndpointUri(String tokenEndpointUri) {
    this.tokenEndpointUri = tokenEndpointUri;
  }

  private String userInfoUri;


  
  public String getUserInfoUri() {
    return userInfoUri;
  }

  
  public void setUserInfoUri(String userInfoUri) {
    this.userInfoUri = userInfoUri;
  }

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

  private OIDCProviderMetadata oidcProviderMetadata;

  public OIDCProviderMetadata getOidcProviderMetadata() {
    return oidcProviderMetadata;
  }


  private void retrieveMetadata() {
    try {
      
      GrouperHttpClient request = new GrouperHttpClient()
        .assignProxyUrl(this.proxyUrl)
        .assignProxyType(this.proxyType)
        .assignUrl(this.configurationMetadataUri)
        .assignGrouperHttpMethod(GrouperHttpMethod.get)
        .executeRequest();
      
      GrouperUtil.assertion(request.getResponseCode() == 200, "Invalid oidc well known url: "+this.configurationMetadataUri+ ", response code: "+request.getResponseCode());
      
      OIDCProviderMetadata providerMetadata = OIDCProviderMetadata.parse(request.getResponseBody());
      this.oidcProviderMetadata = providerMetadata;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
 
  }
  
  /**
   * retrieve from config or cache
   * @param externalSystemConfigId1
   * @return the config
   */
  private static GrouperOidcConfig retrieveFromConfig(String externalSystemConfigId) {

    GrouperOidcConfig grouperOidcConfig = new GrouperOidcConfig();
  
    grouperOidcConfig.proxyUrl = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + externalSystemConfigId + ".proxyUrl");
    grouperOidcConfig.proxyType = GrouperProxyType.valueOfIgnoreCase(GrouperConfig.retrieveConfig().propertyValueString("grouper.oidcExternalSystem." + externalSystemConfigId + ".proxyType"), false);
    
    // # config id of the external system
    if(GrouperConfig.retrieveConfig().propertyValueBooleanRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".useConfigurationMetadata")) {
      grouperOidcConfig.configurationMetadataUri = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".configurationMetadataUri");
      grouperOidcConfig.retrieveMetadata();
      
      grouperOidcConfig.userInfoUri = grouperOidcConfig.oidcProviderMetadata.getUserInfoEndpointURI().toString();
      grouperOidcConfig.tokenEndpointUri = grouperOidcConfig.oidcProviderMetadata.getTokenEndpointURI().toString();
    } else {
      
      //   
      //  # url to get the user info from the access token https://idp.pennkey.upenn.edu/idp/profile/oidc/userinfo
      //  # grouper.oidcExternalSystem.myOidcConfigId.userInfoUri =
      grouperOidcConfig.userInfoUri = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".userInfoUri");
      
      //  # url to decode the oidc code into an access token: https://idp.institution.edu/idp/profile/oidc/token
      //  # grouper.oidcExternalSystem.myOidcConfigId.tokenEndpointUri =
      grouperOidcConfig.tokenEndpointUri = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".tokenEndpointUri");
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
    
    return grouperOidcConfig;
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

}
