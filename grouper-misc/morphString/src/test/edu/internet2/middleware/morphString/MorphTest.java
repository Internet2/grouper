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
