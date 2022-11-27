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
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
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

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperHttpMethod;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.morphString.Morph;

public class BoxGrouperExternalSystem extends GrouperExternalSystem {
  
  public static void main(String[] args) {
    
    String pk = "-----BEGIN ENCRYPTED PRIVATE KEY-----\nMIIFDjBABgkqhkiG9w0BBQ0wMzAbBgkqhkiG9w0BBQwwDgQIC8kw1LDHB9gCAggA\nMBQGCCqGSIb3DQMHBAgxnTbeWpgWkQSCBMhXUyN5BJOEpArRgu1Xj+C4AxLcdiXv\nNY2DH1naPo5cCPy+mX/ftHlt2f4gbDh+34t92CyMUWC8uOSjchzgHpADVClAlBtg\n5scfYx7/RDcL7G0UhcUOMqVZm8Nfmry0ylTTx7gwq0pd5S2Cv2cDVr9cVYmna719\nXob+Hpumq50Q1x8Cf4F30GGK/L/1nBmnlPh1xy0Hz8YlkIagU8AYYefs+pAd5ZQF\n7JvAxGg4rOWQRVS2/DGTbTB+tt4alNDEw9lTNE8UQ1Nxq+PSm8AGbIePojGGa+Fc\nK0TI/4CVeyJXHhfIPB2JjPmm72EB0RwdHDxB1Xes5k6euz7DflKOeGwP5O0aV/EY\nOXaWbiVo/KJTvd0itl/ZyWSmRlF+iwa87XhZV4uBBaTZtzP8tF4YeN0W/j/4y/ty\nh9WHWfKqUm4bTwEUY7fE8ny++C7pyqxN8vdnf64oHpGX6AWk6imt8L966wqFxuVR\nxl5y2ckbESV2069vbGIj+h406/FGiiJkZgEghSLSYPSqUzj8mzqLGVJ2u9p2/SUz\ndwYKiyF51LYBbdyOe050WqLNp8G1JQxj4DdrqhfTU5tt4edn/JPZ4S8/zpw9/lgo\nsASrW76BVGCDRicTYyO6yh7uVPbRYGfXX72ARIeKuxbAOQaHcQ8HDDivreh+GtC+\n8hF5g3ASUmyJSdJI/45v1V1rOzPD1jZDtC05zN4BuCcSyiDHuLzOIk1BrbPp58K1\ntTNlAfrjmq2IasQGftkb6ASXh5HQ6kvjEq/YSsDkUv00gnuoorDoF5BY2HBd7AhO\n4hsTGaSCt/VOTKE3+mOYsRCRIYwQNQDLoKGiQUqkkLKUYOPlRZ7mNqnE12wNZrG6\nlkGvSL/KX+ISuYVHluaO0k1mvAevXrNeMjq+BcSmXzyIxRRpEuDYf0CVfESMxImI\nwk9rGiijKmUZ8sL8a/FYnO9YoyBBLsTSQZtCbzzT3bVozU4JEjTyWwc8ZGDlfZdE\nheA0GVgtVVCS/8wnoDH+OslKa1kAJB7fGunY0MbjAEzCw48hRpJRcOiaVb9jINRy\ncIKN/mJQ7TBEbMs+JLlkiQFrop7FnZPYicbFuXr+5zgAZIb1l7bhOVHVOw0WFhWI\nB5w/3cbKduD4ZDyqBVbBGdT1873jwJWLTDK/ZSF7fKykD9TLf55IyTiRf+ULSoG5\nzkp3zxaAzxuctIszegpLcZKHj5MHGSfAFUm/znxjlMwi3hDMW1zfxNbcFKwAPspP\nsgfRgt4vmadRtyvWTWF/HapJ9ZKAj4gR9fD8kh17RgUQnA1M3PauDyyYCsMHbSPq\n6enjsov/rp9hwQsP5+d+Mq/Yupvq8fUv5BxQdIPL9A/US0G++CLuNUFZTFAjSmju\n4dLwdBYjpSDLfuKq/XwBfOAJ9UlkFGVMTG5chVwIDUNR0VAlrRP5OqO3yxNmF59g\nxSjb7dFOYZ9omD/nV2bwm2w79QGDfqcn2cod1lJnN1ndBXJqsYABP9rSQgwAdpth\nug8ys4JKBj41WStKNzzMCVdWFOySqrIxiV4CyOfxR85GoWy5aDklAdOa4APn10Iy\nt3w5sbpBrUPX3t0o52tylAoDybNU0OkvHIOaKntyttiqbrLkac7GsIKyt0AxkXsA\nqRU=\n-----END ENCRYPTED PRIVATE KEY-----\n";
    System.out.println(pk);
    pk = pk.replaceAll("\n", "");
    System.out.println(pk);
  }
  
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

//    String authenticationUrl = "https://api.box.com/oauth2/token";
    
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
    String proxyPort = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".proxyPort");
    String proxyType = GrouperClientConfig.retrieveConfig().propertyValueString("grouperClient.boxConnector." + configId + ".proxyType");
    
    String proxyUrl  = null;
    
    if(StringUtils.isNotBlank(proxyHost)) {
      proxyUrl = proxyHost;
      if (StringUtils.isNotBlank(proxyPort)) {
        proxyUrl = proxyUrl + ":"+ proxyPort;
      }
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
//    int expiresInSeconds = GrouperUtil.jsonJacksonGetInteger(jsonObject, "expires_in");
    String accessToken = GrouperUtil.jsonJacksonGetString(jsonObject, "access_token");
    long expiresOn = now/1000 + 59 * 60;
    expiresOnAndEncryptedBearerToken = new MultiKey(expiresOn, Morph.encrypt(accessToken));
    configKeyToExpiresOnAndBearerToken.put(configId, expiresOnAndEncryptedBearerToken);
    
    return accessToken;
    
  }
  
}
