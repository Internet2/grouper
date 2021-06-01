package edu.internet2.middleware.grouperBox;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;

import org.apache.commons.lang3.StringUtils;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.DeveloperEditionEntityType;
import com.box.sdk.EncryptionAlgorithm;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;
import com.box.sdk.JWTEncryptionPreferences;

import edu.internet2.middleware.grouper.app.externalSystem.GrouperExternalSystem;
import edu.internet2.middleware.grouper.cfg.dbConfig.ConfigFileName;
import edu.internet2.middleware.grouperClient.util.ExpirableCache;
import edu.internet2.middleware.grouperClient.util.GrouperClientConfig;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.morphString.Morph;

public class BoxGrouperExternalSystem extends GrouperExternalSystem {

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


  /**
   * cache connections
   */
  private static ExpirableCache<String, BoxAPIConnection> boxApiConnectionCache = new ExpirableCache<String, BoxAPIConnection>(5);
  
  /**
   * 
   * @return box api connection
   */
  public synchronized static BoxAPIConnection retrieveBoxApiConnection(String configId) {
    
    BoxAPIConnection boxAPIConnection = boxApiConnectionCache.get(configId);
    
    if (boxAPIConnection == null) {
      
      String configPrefix = "grouperClient.boxConnector." + configId + ".";
    
      JWTEncryptionPreferences jwtEncryptionPreferences = new JWTEncryptionPreferences();
      
      String privateKeyContents = null;
      if (GrouperClientConfig.retrieveConfig().containsKey(configPrefix + "privateKeyContents_0")) {
        StringBuilder keyFileContentsPart = new StringBuilder();
        
        for (int i=0;i<10;i++) {

          // lines 1-?
          String privateKeyPart = GrouperClientConfig.retrieveConfig().propertyValueString(configPrefix + "privateKeyContents_" + i);
          if (!StringUtils.isBlank(privateKeyPart)) {
            if (keyFileContentsPart.length() > 0 && !keyFileContentsPart.toString().endsWith("\n")) {
              keyFileContentsPart.append("\n");
            }
            //use $newline$ in config overlays
            //grouperSftpPrivateKeyPart = StringUtils.replace(grouperSftpPrivateKeyPart, "NEWLINE", "\n");
            privateKeyPart = StringUtils.trim(privateKeyPart);
            keyFileContentsPart.append(privateKeyPart);

          } else {
            break;
          }

        }
        privateKeyContents = keyFileContentsPart.toString();
      } else {
        File privateKeyFile = new File(GrouperClientConfig.retrieveConfig().propertyValueStringRequired(configPrefix + "privateKeyFileName"));
        privateKeyContents = GrouperClientUtils.readFileIntoString(privateKeyFile);
      }
      
      jwtEncryptionPreferences.setPrivateKey(privateKeyContents);
      
      String privateKeyPass = GrouperClientConfig.retrieveConfig().propertyValueStringRequired(configPrefix + "privateKeyPass");
      privateKeyPass = Morph.decryptIfFile(privateKeyPass);
      
      jwtEncryptionPreferences.setPrivateKeyPassword(privateKeyPass);
      jwtEncryptionPreferences.setEncryptionAlgorithm(EncryptionAlgorithm.RSA_SHA_512);
      jwtEncryptionPreferences.setPublicKeyID(GrouperClientConfig.retrieveConfig().propertyValueStringRequired(configPrefix + "publicKeyId"));
      
      IAccessTokenCache iAccessTokenCache = new InMemoryLRUAccessTokenCache(10000);
      
      String clientSecret = GrouperClientConfig.retrieveConfig().propertyValueStringRequired(configPrefix + "clientSecret");
      clientSecret = Morph.decryptIfFile(clientSecret);
      
      boxAPIConnection = new BoxDeveloperEditionAPIConnection(
          GrouperClientConfig.retrieveConfig().propertyValueStringRequired(configPrefix + "enterpriseId"),
          DeveloperEditionEntityType.ENTERPRISE, 
          GrouperClientConfig.retrieveConfig().propertyValueStringRequired(configPrefix + "clientId"), 
          clientSecret, jwtEncryptionPreferences,
          iAccessTokenCache);
      
      String proxyHost = GrouperClientConfig.retrieveConfig().propertyValueString(configPrefix + "proxyHost");
      Integer proxyPort = GrouperClientConfig.retrieveConfig().propertyValueInt(configPrefix + "proxyPort");

      if (StringUtils.isNotEmpty(proxyHost) && (proxyPort != null && proxyPort > 0)) {
        boxAPIConnection.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
      }
      
      boxApiConnectionCache.put(configId, boxAPIConnection);
    }        
    
    return boxAPIConnection;
  }
}
