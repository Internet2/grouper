/*
 * @author mchyzer
 * $Id: MorphTest.java,v 1.1 2008-09-13 18:51:48 mchyzer Exp $
 */
package edu.internet2.middleware.morphString;

import junit.framework.TestCase;


/**
 *
 */
public class MorphTest extends TestCase {

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
    String encryptString = Morph.encrypt("abc", "hey");
    assertTrue(!"hey".equals(encryptString));
    String decryptString = Morph.decrypt("abc", encryptString);
    assertEquals("hey", decryptString);
    
  }
  
}
