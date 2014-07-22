/**
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
 */
package edu.internet2.middleware.morphString;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import edu.internet2.middleware.morphString.apache.codec.binary.Base64;

/**
 * The purpose of this class is to provide encryption 
 * and decryption using standard Java libraries, for potentially 
 * large amounts of data.
 * <p>
 * This class provides default encryption using AES with a constant
 * 128 bit key.  If you want something more secure feel free to 
 * override the defaults however you please.
 * <p>
 * This class works in one of two ways, (1) in memory using Strings, or (2) via 
 * I/O streams (preferred for large amounts of data).
 * <p>
 * Crypo objects, or more specifically the default ciphers they create, are not 
 * threadsafe and are not computationally cheap, so a threadlocal factory 
 * method is provided for convenience.  This is the preferred means of usage,
 * but feel free to create these objects however you please.
 * <p>
 * Note that you can encrypt BLOB fields by specifying encryption in the 
 * configurator (Crypto is the default encryption mechanism for that).
 * <p> 
 */
public class Crypto {
  /** threadlocal provided for conveniency */
  private static final ThreadLocal<Crypto> threadLocalCrypto = new ThreadLocal<Crypto>();
  
  /** @return a non-null thread-safe crypto object from a ThreadLocal */
  public static Crypto getThreadLocalCrypto() {
    Crypto crypto = threadLocalCrypto.get();
    if (crypto == null) {
      crypto = new Crypto();
      threadLocalCrypto.set(crypto);
    }
    return crypto;
  }
  
  /**
   * Generate a key.
   * @param cipherName the name of the cipher, if null will default to "AES"
   * @param keybits the number of bits in the key, if null will default to 128
   * @return the bytes comprising the key
   */
  public static byte[] generateKeyBytes(String cipherName, Integer keybits) {
    KeyGenerator keyGenerator = null;
    cipherName = cipherName == null ? "AES" : cipherName;
    try {
      keyGenerator = KeyGenerator.getInstance(cipherName);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Failed to create KeyGenerator for cipherName="+cipherName, e);
    }
    keyGenerator.init(keybits == null ? 128 : keybits); // 192 and 256 bits may not be available
    
    // Generate the secret key specs.
    SecretKey secretKey = keyGenerator.generateKey();
    byte[] keyBytes = secretKey.getEncoded();
    
    return keyBytes;    
  }

  
  /** SecretKeySpec */
  private SecretKeySpec key;
  
  /** lazily constructed cipher */
  private Cipher cipher = null;
    
  /**
   * Create the default cipher
   * @return the default cipher
   */
  public Cipher createDefaultCipher() {
    try {
      return Cipher.getInstance("AES");
    } catch(Exception x) {
      throw new RuntimeException("Failed to create the default cipher!", x);
    }
  }
  
  /** Default crypto object */
  public Crypto() {
    this(Morph.key());
  }
  
  /** Default crypto object 
   * @param theKey used to encrypt/decrypt 
   */
  public Crypto(String theKey) {
    super();
    init(theKey);
  }
  
  /** only warn once */
  private static boolean warned = false;
  
  /** initialize the key and cipher 
   * @param secret
   */
  protected void init(String secret) {
    
    if (MorphStringUtils.isBlank(secret)) {
      throw new NullPointerException("Must supply a non blank encrypt.key");
    }
    StringBuilder secretBuilder = new StringBuilder(secret);
    if (!warned && secret.length() < 8) {
      //note we dont have a logger
      System.out.println("morphString warning: secret.key in morphString.properties should be at least 8 chars");
      warned = true;
    }
    //secret must be length 16 or 32.  pad if not
    while (secretBuilder.length() < 16) {
      secretBuilder.append("x");
    }
    if (secretBuilder.length() > 16) {
      while (secretBuilder.length() < 32) {
        secretBuilder.append("x");
      }
    }
    if (secretBuilder.length() > 32) {
      secretBuilder.delete(32, secretBuilder.length());
    }
    secret = secretBuilder.toString();
    this.key = new SecretKeySpec(secret.getBytes(), "AES");
    this.cipher = this.createDefaultCipher();
  }
  
  /**
   * Encrypt the string
   * @param clearText
   * @return the encrypted String
   */
  public String encrypt(String clearText) {
    byte[] input = clearText.getBytes();
    try {
      this.initCipher(true);
      byte[] output = this.cipher.doFinal(input);
      byte[] encoded = Base64.encodeBase64(output);      
      return new String(encoded);
    } catch(Exception x) {
      throw new RuntimeException("Failed to encrypt string", x);
    }
  } 
   
  /**
   * Decrypt the string
   * @param cipherText
   * @return the decrypted string
   */
  public String decrypt(String cipherText) {
    try {      
        byte[] cipherBytes = cipherText.getBytes();
        byte[] decodedBytes = Base64.decodeBase64(cipherBytes);
        this.initCipher(false);
        byte[] clearBytes = this.cipher.doFinal(decodedBytes);
        return new String(clearBytes);
    } catch(Exception x) {
      throw new RuntimeException("Failed to decrypt the cipherText", x);
    }
  }
  
  /**
   * Initialize the cipher for encryption or decryption
   * @param encrypt true to encrypt, false to decrypt
   */
  private void initCipher(boolean encrypt) {
    try {
      if ( encrypt ) {
        this.cipher.init(Cipher.ENCRYPT_MODE, this.key);
      } else {
        this.cipher.init(Cipher.DECRYPT_MODE, this.key);
      }
    } catch(Exception x) {
      throw new RuntimeException("Failed to init cipher for "+(encrypt ? "encrypt" : "decrypt"), x);
    }
  }
 
  /**
   * Get the encrypted input stream
   * @param in
   * @return the encrypted input stream
   */
  public InputStream encrypt(InputStream in) {
    this.initCipher(true);
    return new CipherInputStream(in, this.cipher);
  }
  
  /**
   * the decrypted input stream
   * @param in
   * @return the decrypted input stream
   */
  public InputStream decrypt(InputStream in) {
    this.initCipher(false);
    return new CipherInputStream(in, this.cipher);
  }
  
  /**
   * the encrypted output stream
   * @param out
   * @return the encrypted output stream
   */
  public OutputStream encrypt(OutputStream out) {
    this.initCipher(true);
    return new CipherOutputStream(out, this.cipher);
  }
  
  /**
   * the decrypted output stream
   * @param out
   * @return the decrypted output stream
   */
  public OutputStream decrypt(OutputStream out) {
    this.initCipher(false);
    return new CipherOutputStream(out, this.cipher);
  }
}
