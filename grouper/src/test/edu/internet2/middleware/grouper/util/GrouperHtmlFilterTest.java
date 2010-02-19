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
    assertEquals("hey", new GrouperHtmlFilter().filterHtml("h<script>e</script>y"));
    assertEquals("hey", new GrouperHtmlFilter().filterHtml("h<form>e</form>y"));
    assertEquals("hey", new GrouperHtmlFilter().filterHtml("h<a>e</a>y"));
    assertEquals("hey", new GrouperHtmlFilter().filterHtml("h<button>e</button>y"));
    assertEquals("h<div>e</div>y", new GrouperHtmlFilter().filterHtml("h<div>e</div>y"));
    assertEquals("he</div>y", new GrouperHtmlFilter().filterHtml("h<h<div>e</div>y"));
    assertEquals("h&lt;h<div>e</div>y", new GrouperHtmlFilter().filterHtml("h&lt;h<div>e</div>y"));
    assertEquals("he</div>y", new GrouperHtmlFilter().filterHtml("h<div onclick=''>e</div>y"));
  }
  
}
