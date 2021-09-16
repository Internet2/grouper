package edu.internet2.middleware.grouper.authentication;

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
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
   * @param configId
   * @return the config
   */
  public static GrouperOidcConfig retrieveFromConfigOrCache(String configId) {
    
    GrouperOidcConfig grouperOidcConfig = grouperOidcConfigCache.get(configId);
    if (grouperOidcConfig == null) {
      grouperOidcConfig = retrieveFromConfig(configId);
      grouperOidcConfigCache.put(configId, grouperOidcConfig);
    }
    
    return grouperOidcConfig;
  }

  private String tokenEndpointUri = null;

  public String getTokenEndpointUri() {
    return tokenEndpointUri;
  }
  
  public void setTokenEndpointUri(String tokenEndpointUri) {
    this.tokenEndpointUri = tokenEndpointUri;
  }

  private String externalSystemConfigId;
  
  public String getExternalSystemConfigId() {
    return externalSystemConfigId;
  }
  
  public void setExternalSystemConfigId(String externalSystemConfigId) {
    this.externalSystemConfigId = externalSystemConfigId;
  }

  private String userInfoUri;


  
  public String getUserInfoUri() {
    return userInfoUri;
  }

  
  public void setUserInfoUri(String userInfoUri) {
    this.userInfoUri = userInfoUri;
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

  
  public String getScope() {
    return scope;
  }

  
  public void setScope(String scope) {
    this.scope = scope;
  }

  /**
   * retrieve from config or cache
   * @param configId
   * @return the config
   */
  private static GrouperOidcConfig retrieveFromConfig(String configId) {

    GrouperOidcConfig grouperOidcConfig = new GrouperOidcConfig();
  
    // # config id of the external system
    String externalSystemConfigId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidc." + configId + ".oidcExternalSystemConfigId");
    
    //  # url to decode the oidc code into an access token: https://idp.institution.edu/idp/profile/oidc/token
    //  # grouper.oidcExternalSystem.myOidcConfigId.tokenEndpointUri =
    grouperOidcConfig.tokenEndpointUri = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".tokenEndpointUri");
    
    
    //   
    //  # url to get the user info from the access token https://idp.pennkey.upenn.edu/idp/profile/oidc/userinfo
    //  # grouper.oidcExternalSystem.myOidcConfigId.userInfoUri =
    grouperOidcConfig.userInfoUri = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".userInfoUri");
    
    
    //    
    //  # client id to authorize url
    //  # grouper.oidcExternalSystem.myOidcConfigId.clientId =
    grouperOidcConfig.clientId = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidcExternalSystem." + externalSystemConfigId + ".clientId");

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
    grouperOidcConfig.redirectUri = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidc." + configId + ".redirectUri");

    //   
    //  # scope to retrieve from oidc, e.g. openid email profile
    //  grouper.oidc.configId.scope =
    grouperOidcConfig.scope = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidc." + configId + ".scope");

    //   
    //  # optional, could be in claim as "subjectSourceId", e.g. myPeople
    //  grouper.oidc.configId.subjectSourceId =
    grouperOidcConfig.subjectSourceId = GrouperConfig.retrieveConfig().propertyValueString("grouper.oidc." + configId + ".subjectSourceId");

    //   
    //  # subjectId, subjectIdentifier, or subjectIdOrIdentifier
    //  grouper.oidc.configId.subjectIdType =
    grouperOidcConfig.subjectIdType = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidc." + configId + ".subjectIdType");

    //  # some claim name that has the subjectId / subjectIdentifier / subjectIdOrIdentifier in it.  e.g. employeeId
    //  grouper.oidc.configId.subjectIdClaimName =
    grouperOidcConfig.subjectIdClaimName = GrouperConfig.retrieveConfig().propertyValueStringRequired("grouper.oidc." + configId + ".subjectIdClaimName");
    
    
    
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

}
