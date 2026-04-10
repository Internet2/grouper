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
 * $Id: MorphTest.java,v 1.5 2009-03-20 19:56:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util.rijndael;

import junit.textui.TestRunner;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.morphString.Morph;



/**
 * test morphing
 */
public class MorphTest extends GrouperTest {

  /**
   * @param name
   */
  public MorphTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public MorphTest() {
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(MorphTest.class);
    TestRunner.run(new MorphTest("testMorphFromFile"));
  }

  /**
   * 
   */
  public void testMorph() {
    Morph.testMorphKey = "ert234mN54";
    String morphed = Morph.encrypt("whatever");
    assertFalse(StringUtils.equals(morphed, "whatever"));
    String unmorphed = Morph.decrypt(morphed);
    assertEquals(unmorphed, "whatever");
  }
  
  /**
   * test that decryptIfFile returns plain password unchanged
   */
  public void testDecryptIfFilePlainPassword() {
    Morph.testMorphKey = "ert234mN54";
    assertEquals("mypassword", Morph.decryptIfFile("mypassword"));
  }

  /**
   * test that decryptIfFile returns an encrypted password decrypted
   */
  public void testDecryptIfFileEncryptedPassword() {
    Morph.testMorphKey = "ert234mN54";
    String encrypted = Morph.encrypt("mypassword");
    assertEquals("mypassword", Morph.decryptIfFile(encrypted));
  }

  /**
   * test that decryptIfFile preserves password with equals sign in the middle
   */
  public void testDecryptIfFilePasswordWithEqualsInMiddle() {
    Morph.testMorphKey = "ert234mN54";
    assertEquals("passwo=rd", Morph.decryptIfFile("passwo=rd"));
  }

  /**
   * test that decryptIfFile preserves password with equals sign near the start
   */
  public void testDecryptIfFilePasswordWithEqualsNearStart() {
    Morph.testMorphKey = "ert234mN54";
    assertEquals("p=assword", Morph.decryptIfFile("p=assword"));
  }

  /**
   * test that decryptIfFile preserves password starting with equals sign
   */
  public void testDecryptIfFilePasswordStartingWithEquals() {
    Morph.testMorphKey = "ert234mN54";
    assertEquals("=password", Morph.decryptIfFile("=password"));
  }

  /**
   * test that decryptIfFile preserves password that is just an equals sign
   */
  public void testDecryptIfFilePasswordJustEquals() {
    Morph.testMorphKey = "ert234mN54";
    assertEquals("=", Morph.decryptIfFile("="));
  }

  /**
   * test that decryptIfFile handles null input
   */
  public void testDecryptIfFileNull() {
    Morph.testMorphKey = "ert234mN54";
    assertEquals("", Morph.decryptIfFile(null));
  }

  /**
   * test that decryptIfFile handles empty string input
   */
  public void testDecryptIfFileEmpty() {
    Morph.testMorphKey = "ert234mN54";
    assertEquals("", Morph.decryptIfFile(""));
  }

  /**
   * test that decryptIfFile preserves passwords with special characters
   */
  public void testDecryptIfFilePasswordWithSpecialChars() {
    Morph.testMorphKey = "ert234mN54";
    assertEquals("p@ss!w#rd$%^&*", Morph.decryptIfFile("p@ss!w#rd$%^&*"));
  }

  /**
   * test that encrypt then decryptIfFile round trips with equals in password
   */
  public void testEncryptDecryptIfFileRoundTripWithEquals() {
    Morph.testMorphKey = "ert234mN54";
    String password = "p=assword";
    String encrypted = Morph.encrypt(password);
    assertEquals(password, Morph.decryptIfFile(encrypted));
  }

  /**
   * test that Morph.decrypt throws on empty string input (base64 decodes to empty bytes)
   */
  public void testDecryptThrowsOnEmptyString() {
    Morph.testMorphKey = "ert234mN54";
    try {
      Morph.decrypt("");
      fail("Expected RuntimeException for empty string decrypt");
    } catch (RuntimeException e) {
      // expected
    }
  }

  /**
   * test that Morph.decrypt throws on input that base64 decodes to empty bytes (equals at start)
   */
  public void testDecryptThrowsOnEqualsAtStart() {
    Morph.testMorphKey = "ert234mN54";
    try {
      Morph.decrypt("=password");
      fail("Expected RuntimeException for input that base64 decodes to empty bytes");
    } catch (RuntimeException e) {
      // expected
    }
  }

  /**
   *
   */
  public void testMorphFromFile() {
    Morph.testMorphKey = "ert234mN54";
    String morphed = Morph.encrypt("whatever");
    
    //System.out.println("'" + morphed + "'");
    
    File tempFile = new File(GrouperUtil.tmpDir(true) + "morph_" + GrouperUtil.uniqueId() + ".pass");
    try {
      GrouperUtil.saveStringIntoFile(tempFile, morphed);
      
      assertFalse(StringUtils.equals(morphed, "whatever"));
  
      String unmorphed = Morph.decryptIfFile(tempFile.getAbsolutePath());
      assertEquals(unmorphed, "whatever");
    } finally {    
      GrouperUtil.deleteFile(tempFile);
    }
  }
  
}
