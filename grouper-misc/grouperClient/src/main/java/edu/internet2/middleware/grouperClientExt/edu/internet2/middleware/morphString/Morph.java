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
 * $Id: Morph.java,v 1.1 2008-11-30 10:57:26 mchyzer Exp $
 * 
 * Copyright University of Pennsylvania 2004
 */
package edu.internet2.middleware.grouperClientExt.edu.internet2.middleware.morphString;


/**
 * @author Chris Hyzer
 * @deprecated use edu.internet2.middleware.morphString.Morph instead!
 */
@Deprecated
public class Morph {

  /**
   * 
   */
  public static final String ENCRYPT_KEY = edu.internet2.middleware.morphString.Morph.ENCRYPT_KEY;

  /**
   * @param in
   *          $objectType$
   * 
   * @return String
   */
  public static String encrypt(String in) {
    return edu.internet2.middleware.morphString.Morph.encrypt(in);
  }

  /**
   * This will decrypt a string from an external file if a slash is there.
   * Otherwise it will just return the input since it is just what is being looked for
   * @param in $objectType$
   * @return String
   */
  public static String decrypt(String in) {
    return edu.internet2.middleware.morphString.Morph.decrypt(in);
  }

  /**
   * This will decrypt a string from an external file if a slash is there.
   * Otherwise it will just return the input since it is just what is being looked for
   * @param in $objectType$
   * @return String
   */
  public static String decryptIfFile(String in) {
    return edu.internet2.middleware.morphString.Morph.decryptIfFile(in);
  }
  
  /**
   * @return the key to encrypt/decrypt
   */
  public static String key() {
    return Morph.key();
  }

  /**
   * Constructor for Morph.
   */
  private Morph() {
  }

}
