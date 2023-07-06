package edu.internet2.middleware.grouper.app.boxProvisioner;

import java.io.File;
import java.io.StringReader;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.operator.InputDecryptorProvider;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator.Builder;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.JsonNode;

import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.morphString.Morph;

public class BoxGrouperExternalSystem extends GrouperExternalSystem {
  
  /**
   * cache of config key to expires on and encrypted bearer token
   */
  private static ExpirableCache<String, MultiKey> configKeyToExpiresOnAndBearerToken = new ExpirableCache<String, MultiKey>(60);

  public static void clearCache() {
    configKeyToExpiresOnAndBearerToken.clear();
  }


  @Override
  public ConfigFileName getConfigFileName() {
    return ConfigFileName.GROUPER_CLIENT_PROPERTIES;
  }

  @Override
  public String getConfigItemPrefix() {
    if (StringUtils.isBlank(this.getConfigId())) {
      throw new RuntimeException("Must have configId!");
    }
    return "grouperClient.boxConnector." + this.getConfigId() + ".";
  }

  @Override
  public String getConfigIdRegex() {
    return "^(grouperClient\\.boxConnector)\\.([^.]+)\\.(.*)$";
  }
  
  @Override
  public String getConfigIdThatIdentifiesThisConfig() {
    return "myConnector";
  }
  
  static class BoxRsaKeyProvider implements RSAKeyProvider {
    
    private RSAPrivateKey privateKey;
    private String publicKeyId;
    
    BoxRsaKeyProvider(PrivateKey privateKey, String publicKeyId) {
     this.privateKey = (RSAPrivateKey)privateKey; 
     this.publicKeyId = publicKeyId;
    }
    
    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
      throw new RuntimeException("not implemented");
    }
    
    @Override
    public String getPrivateKeyId() {
      return this.publicKeyId;
    }
    
    @Override
    public RSAPrivateKey getPrivateKey() {
      return privateKey;
    }
      
  }
  
  public static String retrieveAccessTokenForBoxConfigId(Map<String, Object> debugMap, String configId) {

    long now = System.currentTimeMillis();
    
    MultiKey expiresOnAndEncryptedBearerToken = configKeyToExpiresOnAndBearerToken.get(configId);
    
    String encryptedBearerToken = null;
    if (expiresOnAndEncryptedBearerToken != null) {
      long expiresOnSeconds = (Long)expiresOnAndEncryptedBearerToken.getKey(0);
      encryptedBearerToken = (String)expiresOnAndEncryptedBearerToken.getKey(1);
      if (expiresOnSeconds * 1000 > System.currentTimeMillis()) {
        // use it
        if (debugMap != null) {
          debugMap.put("boxCachedAccessToken", true);
        }
        return Morph.decrypt(encryptedBearerToken);
      }
    }
    
    String authenticationUrl = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".authenticationUrl");
    
    String publicKeyId = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".publicKeyId");
    
    String privateKeyFilePath = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".privateKeyFileName");
    
    String privateKeyString = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".privateKeyContents_0");
    
    String privateKeyPass = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".privateKeyPass");
    
    String clientId = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".clientId");
    
    String clientSecret = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".clientSecret");
    
    String enterpriseId = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".enterpriseId");
    
    boolean usingPrivateKey = StringUtils.isNotBlank(privateKeyFilePath) || StringUtils.isNotBlank(privateKeyString);

    PrivateKey privateKey = null;
    String privateKeyContents = null;
    String signedJwt = null;
    
    if (usingPrivateKey) {
      if (StringUtils.isNotBlank(privateKeyFilePath)) {
        privateKeyContents = GrouperUtil.readFileIntoString(new File(privateKeyFilePath));
      } else {
        privateKeyContents = privateKeyString;
      }
      
      if (StringUtils.isNotBlank(privateKeyPass)) {
        
        try {

          Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
          PEMParser pemParser = new PEMParser(new StringReader(privateKeyContents));
          Object object = pemParser.readObject();
          JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

          PKCS8EncryptedPrivateKeyInfo pkcs8EncryptedPrivateKeyInfo = (PKCS8EncryptedPrivateKeyInfo)object;

          InputDecryptorProvider decryptionProv = new JceOpenSSLPKCS8DecryptorProviderBuilder().build(privateKeyPass.toCharArray());
          PrivateKeyInfo keyInfo = pkcs8EncryptedPrivateKeyInfo.decryptPrivateKeyInfo(decryptionProv);
          privateKey = converter.getPrivateKey(keyInfo);

        } catch(Exception e) {
          throw new RuntimeException("Could not construct private key", e);
        }
        
      } else {
        try {
         
          PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContents));
          
          KeyFactory kf = KeyFactory.getInstance("RSA");
          privateKey = kf.generatePrivate(keySpec);
          
        } catch (NoSuchAlgorithmException e) {
          throw new RuntimeException("Could not reconstruct the private key, the given algorithm could not be found.", e);
        } catch (InvalidKeySpecException e) {
          throw new RuntimeException("Could not reconstruct the private key", e);
        } catch (Exception e) {
          throw new RuntimeException("Could not construct private key from key contents", e);
        }
      }
      
      Algorithm algorithm = Algorithm.RSA512(new BoxRsaKeyProvider(privateKey, publicKeyId));
      
      Builder jwtBuilder = JWT.create()
          .withIssuer(clientId)
          .withSubject(enterpriseId)
          .withAudience(authenticationUrl)
          .withClaim("box_sub_type", "enterprise")
          .withIssuedAt(new Date(now))
          .withJWTId(UUID.randomUUID().toString())
          .withExpiresAt(new Date(now + 45 * 1000L));
        
     signedJwt = jwtBuilder.sign(algorithm);
    }
    
    GrouperHttpClient grouperHttpClient = new GrouperHttpClient();
    
    final String url = authenticationUrl;
    grouperHttpClient.assignGrouperHttpMethod(GrouperHttpMethod.post);
    grouperHttpClient.assignUrl(url);
    
    String proxyHost = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".proxyHost");
    String proxyType = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".proxyType");
    
    String proxyUrl  = null;
    
    if(StringUtils.isNotBlank(proxyHost)) {
      proxyUrl = proxyHost;
    }
    if (StringUtils.isNotBlank(proxyUrl)) {
      grouperHttpClient.assignProxyUrl(proxyUrl);
    }
    if (StringUtils.isNotBlank(proxyType)) {      
      grouperHttpClient.assignProxyType(proxyType);
    }
    
    if (usingPrivateKey) {
      grouperHttpClient.addBodyParameter("assertion", signedJwt);
      grouperHttpClient.addBodyParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
    } else {
      grouperHttpClient.addBodyParameter("grant_type", "client_credentials");
      grouperHttpClient.addBodyParameter("box_subject_type", "enterprise");
      grouperHttpClient.addBodyParameter("box_subject_id", enterpriseId);
    }
    
    grouperHttpClient.addBodyParameter("client_id", clientId);
    grouperHttpClient.addBodyParameter("client_secret", clientSecret);

    int code = -1;
    String json = null;

    try {
      
      grouperHttpClient.executeRequest();
      code = grouperHttpClient.getResponseCode();
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
    long expiresOn = now/1000 + expiresInSeconds - 5; // subtract 5 seconds just to be safe
    expiresOnAndEncryptedBearerToken = new MultiKey(expiresOn, Morph.encrypt(accessToken));
    configKeyToExpiresOnAndBearerToken.put(configId, expiresOnAndEncryptedBearerToken);
    
    return accessToken;
    
  }


  @Override
  public void validatePreSave(boolean isInsert, boolean fromUi,
      List<String> errorsToDisplay, Map<String, String> validationErrorsToDisplay) {
    
    super.validatePreSave(isInsert, fromUi, errorsToDisplay, validationErrorsToDisplay);
    
    GrouperConfigurationModuleAttribute authenticationType = this.retrieveAttributes().get("authenticationType");

    if (authenticationType != null && StringUtils.equals(authenticationType.getValueOrExpressionEvaluation(), "JWT")) {
      GrouperConfigurationModuleAttribute privateKeyContents = this.retrieveAttributes().get("privateKeyContents_0");
      GrouperConfigurationModuleAttribute privateKeyFile = this.retrieveAttributes().get("privateKeyFileName");

      if (StringUtils.isBlank(privateKeyContents.getValueOrExpressionEvaluation()) && StringUtils.isBlank(privateKeyFile.getValueOrExpressionEvaluation())) {
        validationErrorsToDisplay.put(privateKeyContents.getHtmlForElementIdHandle(), GrouperTextContainer.textOrNull("grouperConfigurationValidationBoxFilePathOrPrivateKeyRequired"));
      }
    }
    
    
  }
  
  @Override
  public List<String> test() throws UnsupportedOperationException {
    
    List<String> ret = new ArrayList<>();
    
    try {
      retrieveAccessTokenForBoxConfigId(new HashMap<String, Object>(), this.getConfigId());
    } catch (Exception e) {
      ret.add("Unable to make box connection: " + GrouperUtil.escapeHtml(e.getMessage(), true));
    }

    return ret;
  }
  
  
}
