/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClientExt.xmpp;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouperClient.encryption.GcEncryptionInterface;
import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;
import edu.internet2.middleware.grouperClientExt.util.JsonUtils;


/**
 * translate json to esb events
 */
public class GcDecodeEsbEvents {

  /**
   * 
   * @param json
   * @return esb events
   */
  public static EsbEvents decodeEsbEvents(String json) {
    EsbEvents esbEvents = (EsbEvents)JsonUtils.jsonConvertFrom(json, EsbEvents.class);
    return esbEvents;
  }

  /**
   * 
   * @param esbEvents
   * @return esb events
   */
  public static EsbEvents unencryptEsbEvents(EsbEvents esbEvents) {
    
    //  ## if you want to encrypt messages, set this to an implementation of edu.internet2.middleware.grouperClient.encryption.GcEncryptionInterface
    //  #esb.consumer.encryptionImplementation = edu.internet2.middleware.grouperClient.encryption.GcSymmetricEncryptAesCbcPkcs5Padding
    //  ## this is a key or could be encrypted in a file as well like other passwords
    //  ## generate a key with: java -cp grouperClient.jar edu.internet2.middleware.grouperClient.encryption.GcGenerateKey 
    //  #number these if there are multiple
    //  #esb.consumer.encryptionKey.0 = abc123
    
    if (!esbEvents.isEncrypted()) {
      return esbEvents;
    }
    
    String encryptionImplName = GrouperClientUtils.propertiesValue("esb.consumer.encryptionImplementation", true);

    Set<String> keys = new LinkedHashSet<String>();
    
    for (int i=0;i<100;i++) {
      String key = GrouperClientUtils.propertiesValue("esb.consumer.encryptionKey." + i, false);
      if (!GrouperClientUtils.isBlank(key)) {
        key = GrouperClientUtils.decryptFromFileIfFileExists(key, null);
        keys.add(key);
      } else {
        break;
      }
    }

    if (keys.size() == 0) {
      throw new RuntimeException("You need to specify at least one key in the "
          + "grouper.client.properties file: esb.consumer.encryptionKey.n");
    }
    
    Class<GcEncryptionInterface> encryptionImplClass = GrouperClientUtils.forName(encryptionImplName);
    GcEncryptionInterface gcEncryptionInterface = GrouperClientUtils.newInstance(encryptionImplClass);
    
    return unencryptEsbEvents(esbEvents, gcEncryptionInterface, keys);
  }

  /**
   * 
   * @param esbEvents
   * @param gcEncryptionInterface 
   * @param keys
   * @return esb events
   */
  public static EsbEvents unencryptEsbEvents(EsbEvents esbEvents, 
      GcEncryptionInterface gcEncryptionInterface, Set<String> keys) {
    
    String encryptionKeySha1First4 = esbEvents.getEncryptionKeySha1First4();
    
    String key = keys.iterator().next();
    
    if(!GrouperClientUtils.isBlank(encryptionKeySha1First4)) {

      boolean foundKey = false;
      
      //loop through and see which key matches
      for (String iteratorKey : keys) {
        
        String sha1first4 = GrouperClientUtils.encryptSha(iteratorKey).substring(0, 4);
        if (GrouperClientUtils.equals(sha1first4, sha1first4)) {
          foundKey = true;
          key = iteratorKey;
          break;
        }
        
      }
      
      if (!foundKey) {
        throw new RuntimeException("Cant find key! " + encryptionKeySha1First4);
      }
    }
    
    String encryptedPayload = esbEvents.getEncryptedPayload();

    String decryptedPayload = gcEncryptionInterface.decrypt(key, encryptedPayload);
    
    esbEvents = decodeEsbEvents(decryptedPayload);
    
    return esbEvents;
  }
  
}
