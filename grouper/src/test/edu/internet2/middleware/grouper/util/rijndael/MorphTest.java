/*
 * @author mchyzer
 * $Id: MorphTest.java,v 1.1 2008-08-18 06:15:58 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util.rijndael;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.GrouperTest;



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

  }

  /**
   * 
   */
  public void testMorph() {
    String morphed = Morph.encrypt("whatever");
    assertFalse(StringUtils.equals(morphed, "whatever"));
    String unmorphed = Morph.decrypt(morphed);
    assertEquals(unmorphed, "whatever");
  }
  
}
