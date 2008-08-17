/*
 * $Id: Morph.java,v 1.1 2008-08-17 15:33:02 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
package edu.internet2.middleware.grouper.util.rijndael;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * @author Chris Hyzer
 */
public class Morph {

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
    //convert both slashes to file slashes
    if (File.separatorChar == '/') {
      in = StringUtils.replace(in, "\\", "/");
    } else {
      in = StringUtils.replace(in, "/", "\\");
    }
    
    //see if it is a file reference
    if (in.indexOf(File.separatorChar) != -1 && !GrouperConfig.getPropertyBoolean("grouper.encrypt.disableExternalFileLookup", false)) {
      //read the contents of the file into a string
      in = GrouperUtil.readFileIntoString(new File(in));
      //make sure no extraneous spaces
      in = StringUtils.trim(in);
      
      String decryptKey = key();
      
      //add something on end so it is more than just the key and rijndael
      try {
        in = decrypt(decryptKey, in);
      } catch (RuntimeException re) {
        //let be descriptive to help out here
        throw new RuntimeException("Problem decrypting string: " + re.getMessage(), re );
      }
      return in;
    }

    return in; 
  }

  /**
   * @return the key to encrypt/decrypt
   */
  private static String key() {
    String decryptKey = GrouperConfig.getProperty("grouper.encrypt.key");
    
    if (StringUtils.isBlank(decryptKey)) {
      throw new RuntimeException("You must have a decrypt key in the grouper.properties file under grouper.encrypt.key");
    }
    return decryptKey + "w";
  }

  /**
   * Constructor for Morph.
   */
  private Morph() {
  }

}