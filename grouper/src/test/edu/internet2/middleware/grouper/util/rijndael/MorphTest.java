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

import org.apache.commons.lang3.StringUtils;

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
   * a real encrypted value must be detected as encrypted
   */
  public void testIsEncryptedRealCiphertext() {
    Morph.testMorphKey = "ert234mN54";
    String encrypted = Morph.encrypt("whatever");
    assertTrue("real ciphertext should be detected as encrypted", Morph.isEncrypted(encrypted));
  }

  /**
   * null / empty are never encrypted
   */
  public void testIsEncryptedBlank() {
    Morph.testMorphKey = "ert234mN54";
    assertFalse(Morph.isEncrypted(null));
    assertFalse(Morph.isEncrypted(""));
    assertFalse(Morph.isEncrypted("   "));
  }

  /**
   * free-form text with non-Base64 characters (spaces, commas, parens) must not
   * be misdetected as ciphertext.  This is the data-provider-query SQL bug:
   * Base64.decodeBase64 silently strips the punctuation and the remaining
   * letters/digits could "decrypt" to garbage, so the SQL was stored encrypted.
   */
  public void testIsEncryptedSqlQueryNotDetected() {
    Morph.testMorphKey = "ert234mN54";
    String sql = "SELECT C.CAMPUS_ID as SUBJECT_ID, HR_STATUS AS empl_hrstatus, deptid AS empl_deptid, "
        + "Position_nbr AS empl_positionnbr, jobcode AS empl_jobcode, empl_class as empl_emplclass, "
        + "reports_to AS empl_reportsto FROM PS_ISU_CS_JOB_VW A, PSOPRDEFN B, SYSADM.PS_PERSON_SA C "
        + "WHERE ( A.EFFDT = (SELECT MAX(A_ED.EFFDT) FROM PS_ISU_CS_JOB_VW A_ED WHERE A.EMPLID = A_ED.EMPLID "
        + "AND A.EMPL_RCD = A_ED.EMPL_RCD AND A_ED.EFFDT <= SYSDATE) AND A.HR_STATUS = 'A' AND A.JOB_INDICATOR = 'P')";
    assertFalse("a SQL query must not be detected as encrypted", Morph.isEncrypted(sql));
  }

  /**
   * a value containing whitespace can never be our (non-chunked Base64)
   * ciphertext, so it must not be detected as encrypted
   */
  public void testIsEncryptedTextWithSpacesNotDetected() {
    Morph.testMorphKey = "ert234mN54";
    assertFalse(Morph.isEncrypted("this is a plain sentence"));
    assertFalse(Morph.isEncrypted("empl_class = 'APN'"));
  }

  /**
   * Deterministic regression for the bug: take a value that really IS our
   * ciphertext (so it decrypts by construction), then insert a space.
   * Base64.decodeBase64 silently strips the space, so the OLD "did decrypt()
   * not throw?" logic would still report this as encrypted (this assertion goes
   * RED if the strict-Base64 gate in Morph.isEncrypted is removed).  The fix
   * rejects it because a real Morph value never contains whitespace.  This does
   * not depend on the encryption key, unlike an arbitrary plaintext that only
   * accidentally decrypts.
   */
  public void testIsEncryptedRejectsCiphertextWithEmbeddedSpace() {
    Morph.testMorphKey = "ert234mN54";
    String ciphertext = Morph.encrypt("secretvalue");
    // sanity: the untouched ciphertext must still be detected as encrypted
    assertTrue(Morph.isEncrypted(ciphertext));
    int mid = ciphertext.length() / 2;
    String withSpace = ciphertext.substring(0, mid) + " " + ciphertext.substring(mid);
    // sanity: the stripped-and-decoded bytes are identical, so it still decrypts
    assertEquals("secretvalue", Morph.decrypt(withSpace));
    // but it must NOT be classified as an encrypted value
    assertFalse("ciphertext with an embedded space must not be detected as encrypted",
        Morph.isEncrypted(withSpace));
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
