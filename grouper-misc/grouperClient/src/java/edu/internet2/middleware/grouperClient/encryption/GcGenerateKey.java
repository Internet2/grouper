/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.encryption;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.codec.binary.Base64;


/**
 *
 */
public class GcGenerateKey {

  /**
   * generate a key
   * @return they key
   */
  public static String generateKeyAes128base64() {
    try {
      KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
      keyGenerator.init(128);
      SecretKey secretKey = keyGenerator.generateKey();
      return new String(new Base64().encode(secretKey.getEncoded()));

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }
  
  /**
   * @param args
   */
  public static void main(String[] args) {
    System.out.print("128 bit aes key encoded with base64 is: ");
    System.out.println(generateKeyAes128base64());
  }

}
