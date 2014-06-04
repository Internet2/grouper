/*******************************************************************************
 * Copyright 2012 Internet2
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
/*
 * @author mchyzer $Id: XmlIndenterTest.java,v 1.1 2008-03-24 20:15:36 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * test xml indenter
 */
public class XmlIndenterTest extends TestCase {

  /**
   * 
   * @param args
   * @throws Exception
   */
  public static void main(String[] args) throws Exception {
    //TestRunner.run(new XmlIndenterTest("testIndent"));
    TestRunner.run(XmlIndenterTest.class);
  }

  /**
   * @param name
   */
  public XmlIndenterTest(String name) {
    super(name);
  }

  /**
   * test
   */
  public void testFindNextStartTagIndex() {
    assertEquals("should be first index", 0, XmlIndenter.findNextStartTagIndex("<", 0));
    assertEquals("should be second index after whitespace", 1, XmlIndenter.findNextStartTagIndex(" <", 0));
    assertEquals("should be second index after text", 1, XmlIndenter.findNextStartTagIndex("a<", 0));
    assertEquals("should be -1 since none", -1, XmlIndenter.findNextStartTagIndex(" a", 0));
  }

  /**
   * test
   */
  public void testFindNextEndTagIndex() {
    assertEquals("should be first index", 1, XmlIndenter.findNextEndTagIndex("<>", 1));
    assertEquals("should be second index after whitespace", 1, XmlIndenter.findNextEndTagIndex(" >", 1));
    assertEquals("should be second index after text", 1, XmlIndenter.findNextEndTagIndex("a>", 1));
    assertEquals("should be -1 since none", -1, XmlIndenter.findNextEndTagIndex(" a", 1));
  }

  /**
   * test
   */
  public void testTagName() {
    assertEquals("tag is a", "a", XmlIndenter.tagName("<a>", 0, 2));
    assertEquals("tag is a", "a", XmlIndenter.tagName("< a>", 0, 3));
    assertEquals("tag is a", "a", XmlIndenter.tagName("</a>", 0, 3));
    assertEquals("tag is a", "a", XmlIndenter.tagName("< /a>", 0, 4));
    assertEquals("tag is a", "a", XmlIndenter.tagName("< / a>", 0, 5));
  }

  
  /**
   * test
   */
  public void testSelfClosedTag() {
    assertFalse("should not be self closed", XmlIndenter.selfClosedTag("<a>", 2));
    assertFalse("should not be self closed", XmlIndenter.selfClosedTag("</a>", 3));
    assertTrue("should be self closed", XmlIndenter.selfClosedTag("<a/>", 3));
    assertTrue("should be self closed", XmlIndenter.selfClosedTag("<a/ >", 4));
  }
  /**
   * test
   */
  public void testCloseTag() {
    assertFalse("should not be close", XmlIndenter.closeTag("<a>", 0));
    assertFalse("should not be close", XmlIndenter.closeTag("<a/>", 0));
    assertTrue("should be close", XmlIndenter.closeTag("</a>", 0));
    assertTrue("should be close", XmlIndenter.closeTag("< /a >", 0));
  }

  /**
   * test
   */
  public void testTextTag() {
    assertFalse("should not be text tag", XmlIndenter.textTag("<a>", 2, "a", null, false));
    assertTrue("should be text tag", XmlIndenter.textTag("<a></a>", 2, "a", "a", true));
    assertFalse("should not be text tag", XmlIndenter.textTag("<a><b/></a>", 2, "a", "b", false));
    assertTrue("should be text tag", XmlIndenter.textTag("<a>a</a>", 2, "a", "a", true));
  }
  
}
