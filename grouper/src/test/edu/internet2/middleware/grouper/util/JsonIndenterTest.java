/**
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
 */
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
