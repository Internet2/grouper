package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import edu.internet2.middleware.grouper.util.GrouperUtil;

/**
 * <pre>
 * Turns one error string returned by GrouperExternalSystem.test() into HTML that is safe to
 * show in the UI message area.
 *
 * Why: the connection-test errors are built by the connectors from data the external target
 * returns (a database row, an LDAP attribute value, a remote HTTP error body, an exception
 * message).  The UI message helper in grouperUi.js inserts its message as HTML, so before this
 * class a value stored in the external system ran as script in a wheel administrator's browser
 * when they clicked Test.
 *
 * Contract: test() returns plain text.  Everything is escaped here.  The only markup in the
 * output is added by this class: some connectors (WsBearerToken, CCure) wrap a stack trace in
 * a literal pre tag, so the text between the first open pre tag and the last close pre tag is
 * escaped and re-wrapped in a pre tag created here, which keeps stack traces readable.
 * Without a pre tag, newlines become br tags.
 *
 * Uses only GrouperUtil.escapeHtml and plain String methods so the same class works on every
 * Grouper release.
 * </pre>
 */
public class ExternalSystemTestErrorHtml {

  /** literal open tag some connectors put around a stack trace */
  private static final String PRE_OPEN = "<pre>";

  /** literal close tag some connectors put around a stack trace */
  private static final String PRE_CLOSE = "</pre>";

  /**
   * no instances
   */
  private ExternalSystemTestErrorHtml() {
  }

  /**
   * convert one test() error string (plain text, possibly attacker controlled) to safe HTML
   * @param error from GrouperExternalSystem.test()
   * @return escaped HTML, never null
   */
  public static String toSafeHtml(String error) {
    if (error == null || error.length() == 0) {
      return "";
    }

    // a connector-added pre block around a stack trace: keep the formatting, but the pre tags in
    // the output are ours and everything around and inside them is escaped
    int preStart = error.indexOf(PRE_OPEN);
    int preEnd = error.lastIndexOf(PRE_CLOSE);
    if (preStart >= 0 && preEnd > preStart) {
      String before = error.substring(0, preStart);
      String inside = error.substring(preStart + PRE_OPEN.length(), preEnd);
      String after = error.substring(preEnd + PRE_CLOSE.length());
      return escapeText(before) + PRE_OPEN + escape(inside) + PRE_CLOSE + escapeText(after);
    }

    return escapeText(error);
  }

  /**
   * escape text and keep line breaks readable outside a pre block
   * @param text
   * @return escaped HTML
   */
  private static String escapeText(String text) {
    String escaped = escape(text);
    // normalize line endings first so a CRLF gives one break, not two
    escaped = GrouperUtil.replace(escaped, "\r\n", "\n");
    escaped = GrouperUtil.replace(escaped, "\r", "\n");
    return GrouperUtil.replace(escaped, "\n", "<br />");
  }

  /**
   * escape all HTML special characters, including single quotes
   * @param text
   * @return escaped text, never null
   */
  private static String escape(String text) {
    if (text == null || text.length() == 0) {
      return "";
    }
    return GrouperUtil.escapeHtml(text, true);
  }

}
