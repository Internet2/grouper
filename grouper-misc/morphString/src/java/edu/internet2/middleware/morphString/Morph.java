/*
 * $Id: Morph.java,v 1.1 2008-09-13 18:51:48 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
package edu.internet2.middleware.morphString;

/**
 * @author Chris Hyzer
 */
public class Morph {

  /**
   * 
   */
  private static final String ENCRYPT_KEY = "encrypt.key";

  /**
   * Method decrypt.
   * @param key String
   * @param in String
   * @return String
   */
  public static String decrypt(String key, String in) {
    RijnMaker r1 = new RijnMaker(16, 16);

    return r1.decrypt(in, key).trim();
  }

  /**
   * @param key
   *          $objectType$
   * @param in
   *          $objectType$
   * 
   * @return String
   */
  public static String encrypt(String key, String in) {
    in = MorphStringUtils.trimToEmpty(in);
    RijnMaker r1 = new RijnMaker(16, 16);
    return r1.encrypt(in, key).trim();
  }

  /**
   * @param in
   *          $objectType$
   * 
   * @return String
   */
  public static String encrypt(String in) {
    String encryptKey = key();

    return encrypt(encryptKey, in);
  }

  /**
   * This will decrypt a string from an external file if a slash is there.
   * Otherwise it will just return the input since it is just what is being looked for
   * @param in $objectType$
   * @return String
   */
  public static String decrypt(String in) {
    //make sure no extraneous spaces
    in = MorphStringUtils.trim(in);
    
    String decryptKey = key();
    String result = null;
    //add something on end so it is more than just the key and rijndael
    try {
      result = decrypt(decryptKey, in);
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

    in = MorphStringUtils.trimToEmpty(in);
    String newIn = MorphStringUtils.readFromFileIfFile(in);
    if (!MorphStringUtils.equals(in, newIn)) {
      String unencrypted = decrypt(newIn);
      return unencrypted;
    }
    return in; 
  }

  /**
   * @return the key to encrypt/decrypt
   */
  private static String key() {
    String decryptKey = MorphPropertyFileUtils.retrievePropertyString(ENCRYPT_KEY);
    
    if (MorphStringUtils.isBlank(decryptKey)) {
      throw new RuntimeException("You must have a decrypt key in the " 
          + MorphPropertyFileUtils.MORPH_STRING_PROPERTIES + " file under " + ENCRYPT_KEY);
    }
    
    decryptKey = MorphStringUtils.readFromFileIfFile(decryptKey);
    
    return decryptKey + "w";
  }

  /**
   * Constructor for Morph.
   */
  private Morph() {
  }

}