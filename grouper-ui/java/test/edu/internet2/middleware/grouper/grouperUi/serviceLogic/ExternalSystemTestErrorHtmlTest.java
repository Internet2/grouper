package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Security issue externalSystemConnectionTestStoredXss: the strings GrouperExternalSystem.test()
 * returns are built from data the external target returns (database rows, LDAP values, remote
 * error bodies), and UiV2ExternalSystem.testExternalSystem() used to show them in the UI message
 * area as raw HTML.  These tests check that ExternalSystemTestErrorHtml, which the UI now runs
 * every test() error through, produces inert HTML.  No database is needed.
 */
public class ExternalSystemTestErrorHtmlTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ExternalSystemTestErrorHtmlTest("testDatabaseMismatchPayloadIsInert"));
  }

  /**
   *
   */
  public ExternalSystemTestErrorHtmlTest() {
    super();
  }

  /**
   * @param name
   */
  public ExternalSystemTestErrorHtmlTest(String name) {
    super(name);
  }

  /**
   * the reproduced attack: a database row returned as the "received" value of a mismatch
   */
  public void testDatabaseMismatchPayloadIsInert() {
    String error = "Error: expected value 'SAFE' but received '<img src=x onerror=alert(document.domain)>'";
    String html = ExternalSystemTestErrorHtml.toSafeHtml(error);
    assertFalse(html, html.contains("<img"));
    assertTrue(html, html.contains("&lt;img src=x onerror=alert(document.domain)&gt;"));
    // single quotes are escaped too, so the value cannot break out of an attribute either
    assertFalse(html, html.contains("'"));
  }

  /**
   * WsBearerToken and CCure wrap a stack trace (which can hold remote text) in a literal pre tag
   */
  public void testPreBlockKeptButContentsEscaped() {
    String error = "Error connecting to 'https://x/<b>' <pre>java.lang.RuntimeException: <script>alert(1)</script>\n"
        + "\tat a.b.C.d(C.java:1)\n</pre>";
    String html = ExternalSystemTestErrorHtml.toSafeHtml(error);
    assertFalse(html, html.contains("<script"));
    assertFalse(html, html.contains("<b>"));
    assertTrue(html, html.contains("&lt;script&gt;alert(1)&lt;/script&gt;"));
    // exactly one pre block, created by the helper
    assertEquals(html, 1, html.split("<pre>", -1).length - 1);
    assertEquals(html, 1, html.split("</pre>", -1).length - 1);
    assertTrue(html, html.endsWith("</pre>"));
    // inside pre the newlines stay newlines
    assertTrue(html, html.contains("\n\tat a.b.C.d"));
  }

  /**
   * a close pre tag injected inside the stack trace cannot end the block early and smuggle markup
   */
  public void testInjectedPreCloseDoesNotEscapeBlock() {
    String error = "Error <pre>trace </pre><svg onload=alert(1)><pre>more</pre>";
    String html = ExternalSystemTestErrorHtml.toSafeHtml(error);
    assertFalse(html, html.contains("<svg"));
    assertTrue(html, html.contains("&lt;svg onload=alert(1)&gt;"));
    assertEquals(html, 1, html.split("<pre>", -1).length - 1);
  }

  /**
   * without a pre block, line breaks become br tags and nothing else is markup
   */
  public void testNewlinesWithoutPre() {
    String html = ExternalSystemTestErrorHtml.toSafeHtml("line1\r\nline2\n<i>line3</i>");
    assertEquals("line1<br />line2<br />&lt;i&gt;line3&lt;/i&gt;", html);
  }

  /**
   * ordinary text stays readable; quotes and ampersands become entities; unicode is untouched
   */
  public void testOrdinaryTextAndNulls() {
    assertEquals("", ExternalSystemTestErrorHtml.toSafeHtml(null));
    assertEquals("", ExternalSystemTestErrorHtml.toSafeHtml(""));
    assertEquals("Connection refused", ExternalSystemTestErrorHtml.toSafeHtml("Connection refused"));
    String html = ExternalSystemTestErrorHtml.toSafeHtml("a & b \"c\" caf\u00e9");
    assertTrue(html, html.contains("&amp;"));
    assertFalse(html, html.contains("\""));
    assertTrue(html, html.contains("caf\u00e9"));
  }

}
