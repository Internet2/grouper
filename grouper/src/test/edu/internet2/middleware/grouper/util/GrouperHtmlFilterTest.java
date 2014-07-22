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
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.util;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;


/**
 *
 */
public class GrouperHtmlFilterTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperHtmlFilterTest("testFilter"));
  }
  
  /**
   * 
   * @param name
   */
  public GrouperHtmlFilterTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testFilter() {
    assertEquals("hey", new GrouperHtmlFilter().filterHtml("hey"));
    assertEquals("hey&lt;", new GrouperHtmlFilter().filterHtml("hey<"));
    assertEquals("h&gt;ey", new GrouperHtmlFilter().filterHtml("h&gt;ey"));
    assertEquals("hey&lt;\n&gt;", new GrouperHtmlFilter().filterHtml("hey<\n>"));
    assertEquals("h<b>e</b>y", new GrouperHtmlFilter().filterHtml("h<b>e</b>y"));
    assertEquals("h<BR />ey", new GrouperHtmlFilter().filterHtml("h<BR />ey"));
    assertEquals("h<b>e</b>y", new GrouperHtmlFilter().filterHtml("h<b>e</b>y"));
    assertEquals("h&lt;script&gt;e&lt;/script&gt;y", new GrouperHtmlFilter().filterHtml("h<script>e</script>y"));
    assertEquals("h&lt;form&gt;e&lt;/form&gt;y", new GrouperHtmlFilter().filterHtml("h<form>e</form>y"));
    assertEquals("h&lt;a&gt;e&lt;/a&gt;y", new GrouperHtmlFilter().filterHtml("h<a>e</a>y"));
    assertEquals("h&lt;button&gt;e&lt;/button&gt;y", new GrouperHtmlFilter().filterHtml("h<button>e</button>y"));
    assertEquals("h<div>e</div>y", new GrouperHtmlFilter().filterHtml("h<div>e</div>y"));
    assertEquals("h&lt;h<div>e</div>y", new GrouperHtmlFilter().filterHtml("h<h<div>e</div>y"));
    assertEquals("h&lt;h<div>e</div>y", new GrouperHtmlFilter().filterHtml("h&lt;h<div>e</div>y"));
    assertEquals("h&lt;div onclick=''&gt;e</div>y", new GrouperHtmlFilter().filterHtml("h<div onclick=''>e</div>y"));
  }
  
}
