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
 * @author mchyzer
 * $Id: MorphTest.java,v 1.2 2008-10-29 05:32:23 mchyzer Exp $
 */
package edu.internet2.middleware.morphString;

import junit.framework.TestCase;
import junit.textui.TestRunner;


/**
 *
 */
public class MorphTest extends TestCase {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MorphTest("testEncrypt"));
  }
  
  /**
   * 
   * @param name
   */
  public MorphTest(String name) {
    super(name);
  }

  /**
   * test encryption
   */
  public void testEncrypt() {
    String encryptString = Morph.encrypt("hey");
    System.out.println(encryptString);
    assertTrue(!"hey".equals(encryptString));
    String decryptString = Morph.decrypt(encryptString);
    System.out.println(decryptString);
    assertEquals("hey", decryptString);
    
  }
  
}
