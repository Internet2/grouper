/*******************************************************************************
 * Copyright 2014 Internet2
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouperClient.encryption;

import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import edu.internet2.middleware.grouperClientExt.org.apache.commons.codec.binary.Base64;

/**
 * AES/ECB/PKCS5Padding
 */
public class GcSymmetricEncryptAesCbcPkcs5Padding implements GcEncryptionInterface {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {

    String secret = GcGenerateKey.generateKeyAes128base64();
    
    System.out.println("Key is: " + secret);
    
    String encrypted = new GcSymmetricEncryptAesCbcPkcs5Padding().encrypt(secret, "xyz");
    
    System.out.println("xyz encrypted: " + encrypted);
    
    String clear = new GcSymmetricEncryptAesCbcPkcs5Padding().decrypt(secret, encrypted);
    
    System.out.println("Clear (should be xyz): " + clear);
    
    encrypted = new GcSymmetricEncryptAesCbcPkcs5Padding().encrypt(secret, "xyz");
    
    System.out.println("xyz encrypted (should be different with different initialization vector): " + encrypted);
    
    clear = new GcSymmetricEncryptAesCbcPkcs5Padding().decrypt(secret, encrypted);
    
    System.out.println("Clear (should be xyz): " + clear);
    
  }
  
  /**
   * @param key is base64 128 bit key, generate from GcGenerateKey
   * @see GcEncryptionInterface#encrypt(String, String)
   * @return the base64 encrypted result with initialization vector
   */
  public String encrypt(String key, String data) {

    try {

      byte[] keyBytes = null;
      
      try {
        keyBytes = new Base64().decode(key.getBytes());
      } catch (Exception e) {
        throw new RuntimeException("Key must be 128 bit and encoded with base64", e);
      }

      if (keyBytes.length != 128/8) {
        throw new RuntimeException("Key must be 128 bit and encoded with base64");
      }
      
      SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");

      byte[] iv = new byte[16]; //Means 128 bit
      new Random().nextBytes(iv);
      
      IvParameterSpec ivspec = new IvParameterSpec(iv);

      // initialize the cipher for encrypt mode
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivspec);

      // encrypt the message
      byte[] encrypted = cipher.doFinal(data.getBytes());

      byte[] result = new byte[iv.length + encrypted.length];
      
      //copy initialization vector to result
      System.arraycopy(iv, 0, result, 0, iv.length);
      System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
      
      return new String(new Base64().encode(result), "UTF-8");

    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  /**
   * @see GcEncryptionInterface#decrypt(String, String)
   */
  public String decrypt(String key, String encryptedData) {

    try {

      byte[] keyBytes = null;
      
      try {
        keyBytes = new Base64().decode(key.getBytes());
      } catch (Exception e) {
        throw new RuntimeException("Key must be 128 bit and encoded with base64", e);
      }

      if (keyBytes.length != 128/8) {
        throw new RuntimeException("Key must be 128 bit and encoded with base64");
      }

      SecretKeySpec skeySpec = new SecretKeySpec(keyBytes, "AES");

      // build the initialization vector.  This example is all zeros, but it 
      // could be any value or generated using a random number generator.
      byte[] iv = new byte[16];
      
      byte[] totalBytes = new Base64().decode(encryptedData.getBytes());

      byte[] encryptedDataBytes = new byte[totalBytes.length-iv.length];

      System.arraycopy(totalBytes, 0, iv, 0, iv.length);
      System.arraycopy(totalBytes, iv.length, encryptedDataBytes, 0, encryptedDataBytes.length);

      IvParameterSpec ivspec = new IvParameterSpec(iv);

      // reinitialize the cipher for decryption
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, skeySpec, ivspec);

      // decrypt the message
      byte[] decrypted = cipher.doFinal(encryptedDataBytes);

      return new String(decrypted, "UTF-8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
