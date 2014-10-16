/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouperClient.encryption;


/**
 * encryption
 */
public interface GcEncryptionInterface {

  /**
   * encrypt some data based on a key
   * @param key
   * @param data
   * @return the encrypted text
   */
  public String encrypt(String key, String data);
  
  /**
   * decrypt some data based on a key
   * @param key
   * @param encryptedData
   * @return the encrypted text
   */
  public String decrypt(String key, String encryptedData);
  
}
