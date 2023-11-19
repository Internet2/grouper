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
/*
 * $Id: Morph.java,v 1.3 2008-10-29 05:32:23 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
package edu.internet2.middleware.morphString;

import edu.internet2.middleware.grouperClient.util.GrouperClientUtils;

/**
 * @author Chris Hyzer
 */
public class Morph {

  public static void main(String[] args) {
    System.out.println("a: " + decryptIfFile("a"));
    System.out.println("*: " + decryptIfFile("*"));
    System.out.println("****: " + decryptIfFile("****"));
  }
  
  /**
   * 
   */
  public static final String ENCRYPT_KEY = "encrypt.key";

  /**
   * @param in
   *          $objectType$
   * 
   * @return String
   */
  public static String encrypt(String in) {
    in = GrouperClientUtils.trimToEmpty(in);

    return Crypto.getThreadLocalCrypto().encrypt(in);
  }

  /**
   * This will decrypt a string from an external file if a slash is there.
   * Otherwise it will just return the input since it is just what is being looked for
   * @param in $objectType$
   * @return String
   */
  public static String decrypt(String in) {
    //make sure no extraneous spaces
    in = GrouperClientUtils.trim(in);
    
    String result = null;
    //add something on end so it is more than just the key and rijndael
    try {
      result = Crypto.getThreadLocalCrypto().decrypt(in);
    } catch (RuntimeException re) {
      //let be descriptive to help out here
      throw new RuntimeException("Problem decrypting string: " + re.getMessage(), re );
    }
    return result;
  }

  /**
   * This will decrypt a string from an external file if a slash is there.
   * Otherwise it will just return the input since it is just what is being looked for
   * @param in $objectType$
   * @return String
   */
  public static String decryptIfFile(String in) {

    in = GrouperClientUtils.trimToEmpty(in);
    String newIn = GrouperClientUtils.readFromFileIfFileUtf8(in, false);

    //lets see if encrypted
    try {
      return decrypt(newIn);
    } catch (Exception e) {
      //swallow
    }
    
    return newIn; 
  }
  
  /** if testing, and not relying on morph key being there, use this */
  public static String testMorphKey = null;

  /**
   * @return the key to encrypt/decrypt
   */
  public static String key() {
    if (testMorphKey != null && !"".equals(testMorphKey.trim())) {
      return testMorphKey;
    }
    String decryptKey = MorphStringConfig.retrieveConfig().propertyValueString(ENCRYPT_KEY);
    
    if (GrouperClientUtils.isBlank(decryptKey)) {
      throw new RuntimeException("You must have a decrypt key in the morphString.properties file under " + ENCRYPT_KEY);
    }
    
    String fileNamePossibly = decryptKey;
    
    decryptKey = GrouperClientUtils.readFromFileIfFileUtf8(decryptKey, false);
    
    if (MorphStringConfig.retrieveConfig().propertyValueBoolean("encrypt.trimWhitespaceFromMorphSecretFile", true)) {
      decryptKey = GrouperClientUtils.trim(decryptKey);
    }

    if (GrouperClientUtils.isBlank(decryptKey)) {
      throw new RuntimeException("You must have a decrypt key in the morphString.properties file under " 
          + ENCRYPT_KEY + " and if using external file, must have a key in the file: '" + fileNamePossibly + "'");
    }

    return decryptKey + "w";
  }

  /**
   * Constructor for Morph.
   */
  private Morph() {
  }

}
