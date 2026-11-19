/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security advisory H6 (loaderDiagnosticsReportStoredXss): the loader diagnostics report holds loader
 * settings, query rows, job messages and stack traces, and UiV2GrouperLoader.loaderDiagnosticsRun
 * puts it into the page with innerHtml.  GrouperLoaderDiagnosticsReportHtml must escape all of that
 * data and keep only the report's own fixed markup (the pre wrapper and the colored font labels).
 *
 * Does not compile before the fix (the class is new).  No database needed.  The wiring in
 * UiV2GrouperLoader is checked in the running UI by LoaderDiagnosticsReportBrowserTest.
 */
public class GrouperLoaderDiagnosticsReportHtmlTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperLoaderDiagnosticsReportHtmlTest.class);
  }

  /**
   *
   */
  public GrouperLoaderDiagnosticsReportHtmlTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperLoaderDiagnosticsReportHtmlTest(String name) {
    super(name);
  }

  /**
   * a report shaped like the real one, with data in the middle
   * @param data
   * @return the raw report
   */
  private static String report(String data) {
    return "<pre>\n"
        + "<font color='green'>SUCCESS:</font> grouperLoaderType is: SQL_GROUP_LIST\n"
        + "<font color='blue'>NOTE:</font> SQL group query is set to '" + data + "'\n"
        + "<font color='red'>ERROR:</font> x\n"
        + "<font color='orange'>WARNING:</font> y\n"
        + "<font color='gren'>SUCCESS:</font> z\n"
        + "</pre>";
  }

  /**
   * the fixed markup is unchanged when there is no markup in the data (text quotes, such as the ones
   * around the query, are escaped, which is harmless)
   */
  public void testFixedMarkupKept() {
    String raw = report("select group_name from my_table");
    String expected = raw.replace("'select group_name from my_table'", "&#39;select group_name from my_table&#39;");
    assertEquals(expected, GrouperLoaderDiagnosticsReportHtml.toSafeHtml(raw));
  }

  /**
   * markup in data is escaped, the labels stay
   */
  public void testDataEscaped() {
    String payload = "select 1 /* <img id=secPatch159xss src=x onerror=alert(159)> */ from dual";
    String safe = GrouperLoaderDiagnosticsReportHtml.toSafeHtml(report(payload));
    assertFalse(safe, safe.contains("<img"));
    assertTrue(safe, safe.contains("&lt;img id=secPatch159xss src=x onerror=alert(159)&gt;"));
    assertTrue(safe, safe.startsWith("<pre>\n<font color='green'>SUCCESS:</font> "));
    assertTrue(safe, safe.contains("<font color='blue'>NOTE:</font> SQL group query is set to &#39;select 1"));
    assertTrue(safe, safe.contains("<font color='gren'>SUCCESS:</font> z"));
    assertTrue(safe, safe.endsWith("</pre>"));
  }

  /**
   * data cannot close the pre element or open a script
   */
  public void testDataCannotCloseReport() {
    String safe = GrouperLoaderDiagnosticsReportHtml.toSafeHtml(report("</pre><script>alert(159)</script><pre>"));
    assertFalse(safe, safe.contains("<script"));
    assertEquals("only the report's own pre end tag: " + safe, safe.length() - "</pre>".length(), safe.indexOf("</pre>"));
    assertEquals("only the report's own pre start tag: " + safe, 0, safe.lastIndexOf("<pre>"));
  }

  /**
   * a font tag in data is only put back when it is exactly one of the fixed label tags, so data can
   * never add an attribute
   */
  public void testFontTagWithAttributesStaysEscaped() {
    String safe = GrouperLoaderDiagnosticsReportHtml.toSafeHtml(
        report("<font color='red' onmouseover='alert(159)'>x</font><font color=\"red\">y"));
    assertFalse(safe, safe.contains("onmouseover='"));
    assertTrue(safe, safe.contains("&lt;font color=&#39;red&#39; onmouseover=&#39;alert(159)&#39;&gt;"));
    assertTrue(safe, safe.contains("&lt;font color=&quot;red&quot;&gt;y"));
  }

  /**
   * already escaped text in data is shown as written (not decoded into markup)
   */
  public void testEntitiesInDataNotDecoded() {
    String safe = GrouperLoaderDiagnosticsReportHtml.toSafeHtml(report("&lt;img src=x onerror=alert(159)&gt; & \"q\""));
    assertTrue(safe, safe.contains("&amp;lt;img src=x onerror=alert(159)&amp;gt; &amp; &quot;q&quot;"));
    assertFalse(safe, safe.contains("<img"));
  }

  /**
   * null and a report without the pre wrapper
   */
  public void testEdgeCases() {
    assertEquals("", GrouperLoaderDiagnosticsReportHtml.toSafeHtml(null));
    assertEquals("&lt;b&gt;x&lt;/b&gt; <font color='red'>ERROR:</font>",
        GrouperLoaderDiagnosticsReportHtml.toSafeHtml("<b>x</b> <font color='red'>ERROR:</font>"));
    assertEquals("<pre></pre>", GrouperLoaderDiagnosticsReportHtml.toSafeHtml("<pre></pre>"));
  }
}
