package edu.internet2.middleware.grouper.app.attestation;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

import com.nimbusds.oauth2.sdk.AccessTokenResponse;
import com.nimbusds.oauth2.sdk.AuthorizationCode;
import com.nimbusds.oauth2.sdk.AuthorizationCodeGrant;
import com.nimbusds.oauth2.sdk.AuthorizationGrant;
import com.nimbusds.oauth2.sdk.ResponseType;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenErrorResponse;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientAuthentication;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import com.nimbusds.openid.connect.sdk.AuthenticationRequest;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.authentication.GrouperOidc;
import edu.internet2.middleware.grouper.authentication.GrouperOidcConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.dbConfig.GrouperDbConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

public class Test {

  
  public static void main(String[] args) throws Exception {
    
    GrouperSession.startRootSession();
    
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.oidcClientConfigId.scope", "openid email profile");
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.oidcClientConfigId.oidcResponseType", "code");
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.oidcClientConfigId.oidcExternalSystemConfigId", "oidcConfigId");
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.oidcClientConfigId.subjectIdType", "subjectId");
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.oidcClientConfigId.subjectIdClaimName", "sub");
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidc.oidcClientConfigId.redirectUri", "http://localhost:8080/grouper/grouperUi/app/UiV2Main.oidc");
//
//    
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.oidcConfigId.configurationMetadataUri", "http://localhost:9000/.well-known/openid-configuration");
   
    
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.oidcConfigId.authorizeUri", "http://localhost:9000/auth");
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.oidcConfigId.clientId", "foo");
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.oidcConfigId.clientSecret", "bar");
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.oidcConfigId.tokenEndpointUri", "http://localhost:9000/token");
//    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.oidcExternalSystem.oidcConfigId.userInfoUri", "http://localhost:9000/me");
    
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.authorizeUri").value("http://localhost:9000/auth").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.clientId").value("foo").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.clientSecret").value("bar").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.tokenEndpointUri").value("http://localhost:9000/token").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.userInfoUri").value("http://localhost:9000/me").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.configurationMetadataUri").value("http://localhost:9000/.well-known/openid-configuration").store();

//    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.useForUi").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.useForWs").value("true").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.useConfigurationMetadata").value("true").store();
    
    
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.scope").value("openid email profile").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.oidcResponseType").value("code").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.subjectIdType").value("subjectId").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.subjectIdClaimName").value("sub").store();
    new GrouperDbConfig().configFileName("grouper.properties").propertyName("grouper.oidcExternalSystem.oidcConfigId.redirectUri").value("http://localhost:8080/grouper/grouperUi/app/UiV2Main.oidc").store();
    
    /**
     * grouper.oidcExternalSystem.oidcConfigId.authorizeUri = http://localhost:9000/auth
grouper.oidcExternalSystem.oidcConfigId.clientId = foo
grouper.oidcExternalSystem.oidcConfigId.clientSecret = bar
grouper.oidcExternalSystem.oidcConfigId.tokenEndpointUri = http://localhost:9000/token
grouper.oidcExternalSystem.oidcConfigId.userInfoUri = http://localhost:9000/me
     */
    
    GrouperOidc grouperOidc = new GrouperOidc();
    grouperOidc.assignExternalSystemConfigId("oidcConfigId");
    
    String loginUrl = grouperOidc.generateLoginUrl();
    
    System.out.println(loginUrl);
        
    String authorizationCode = "OWNmNGMyMDMtZGVhMC00YzY1LThkOWUtNjE0NmE3NThmOTg3MtxKmIIyBWudDtTiQ-1bZI5M0RGJRfEkhH1jaTr8yIgjxatL5YRvZDtroEbIuYpncrVUe82qganLkTC8tk-0hQ";
    grouperOidc.assignAuthorizationCode(authorizationCode);
    
    grouperOidc.retrieveAccessToken();
    
    grouperOidc.decodeAccessToken();
    String subjectClaimValue = grouperOidc.findSubjectClaim();
    System.out.println("subjectClaimValue is "+subjectClaimValue);

      
  }
}
