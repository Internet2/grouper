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
