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
package edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString;

import java.io.InputStream;
import java.io.OutputStream;

import javax.crypto.Cipher;

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
 * @deprecated use edu.internet2.middleware.morphString.Crypto instead!
 */
@Deprecated
public class Crypto {

  private edu.internet2.middleware.morphString.Crypto crypto = null;
  
  /** @return a non-null thread-safe crypto object from a ThreadLocal */
  public static Crypto getThreadLocalCrypto() {
    return new Crypto(edu.internet2.middleware.morphString.Crypto.getThreadLocalCrypto());
  }
  
  public Crypto(edu.internet2.middleware.morphString.Crypto theCrypto) {
    this.crypto = theCrypto;
  }
  
  /**
   * Generate a key.
   * @param cipherName the name of the cipher, if null will default to "AES"
   * @param keybits the number of bits in the key, if null will default to 128
   * @return the bytes comprising the key
   */
  public static byte[] generateKeyBytes(String cipherName, Integer keybits) {
    return edu.internet2.middleware.morphString.Crypto.generateKeyBytes(cipherName, keybits);
  }

  
  /**
   * Create the default cipher
   * @return the default cipher
   */
  public Cipher createDefaultCipher() {
    return this.crypto.createDefaultCipher();
  }
  
  /** Default crypto object */
  public Crypto() {
    this(Morph.key());
  }
  
  /** Default crypto object 
   * @param theKey used to encrypt/decrypt 
   */
  public Crypto(String theKey) {
    this.crypto = new edu.internet2.middleware.morphString.Crypto(theKey);
  }
  
  /**
   * Encrypt the string
   * @param clearText
   * @return the encrypted String
   */
  public String encrypt(String clearText) {
    return this.crypto.encrypt(clearText);
  } 
   
  /**
   * Decrypt the string
   * @param cipherText
   * @return the decrypted string
   */
  public String decrypt(String cipherText) {
    return this.crypto.decrypt(cipherText);
  }

  /**
   * Get the encrypted input stream
   * @param in
   * @return the encrypted input stream
   */
  public InputStream encrypt(InputStream in) {
    return this.crypto.encrypt(in);
  }
  
  /**
   * the decrypted input stream
   * @param in
   * @return the decrypted input stream
   */
  public InputStream decrypt(InputStream in) {
    return this.crypto.decrypt(in);
  }
  
  /**
   * the encrypted output stream
   * @param out
   * @return the encrypted output stream
   */
  public OutputStream encrypt(OutputStream out) {
    return this.crypto.encrypt(out);
  }
  
  /**
   * the decrypted output stream
   * @param out
   * @return the decrypted output stream
   */
  public OutputStream decrypt(OutputStream out) {
    return this.crypto.decrypt(out);
  }
}
