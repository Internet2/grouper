/*
 * @author mchyzer
 * $Id: JsonIndenterTest.java,v 1.1 2008-03-24 20:15:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import junit.framework.TestCase;
import junit.textui.TestRunner;



/**
 *
 */
public class JsonIndenterTest extends TestCase {

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    //TestRunner.run(new JsonIndenterTest("testIndent"));
    TestRunner.run(JsonIndenterTest.class);
  }

  /**
   * @param name
   */
  public JsonIndenterTest(String name) {
    super(name);
  }

  /**
   * test
   */
  public void testFindNextEndTagIndex() {
    assertEquals("should be first index", 0, JsonIndenter.findNextEndTagIndex("3", 1));
    assertEquals("should be first index", 0, JsonIndenter.findNextEndTagIndex("3}", 1));
    assertEquals("should be second index", 1, JsonIndenter.findNextEndTagIndex("34]", 1));
    assertEquals("should be fourth index", 2, JsonIndenter.findNextEndTagIndex("\"3\"", 1));
    assertEquals("should be fifth index", 4, JsonIndenter.findNextEndTagIndex("\"3\\\"\"", 1));
    assertEquals("should be fourth index", 3, JsonIndenter.findNextEndTagIndex("\"\\3\"", 1));
    assertEquals("should be second index", 1, JsonIndenter.findNextEndTagIndex("\"\"3\"", 1));
    assertEquals("should be fifth index", 4, JsonIndenter.findNextEndTagIndex("\"\\\"3\"", 1));
    assertEquals("should be third index", 2, JsonIndenter.findNextEndTagIndex("\"3\"\"", 1));
    assertEquals("should be fourth index", 3, JsonIndenter.findNextEndTagIndex("abcd", 1));
  }
  
}
