package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

/**
 * <p>Builds the script <code>guiV2link('operation=...&amp;name=value...')</code> that a UI handler
 * sends back with GuiScreenAction.newScript() to move the browser to another screen.  The browser
 * runs that script with eval(), so a value pasted into it raw (e.g. a subject id from an outside
 * subject source that contains a single quote) would end the JavaScript string and run as code.</p>
 *
 * <p>Every value is URL-encoded (the same encoding the JSPs use with grouper:escapeUrl, so the
 * target screen gets the exact value back from request.getParameter), and the whole URL is
 * escaped for a single-quoted JavaScript string.  The operation and parameter names are code
 * constants and must be plain names.</p>
 *
 * <p>Only uses the JDK, so the same file works in every Grouper release.</p>
 *
 * <pre>
 * guiResponseJs.addAction(GuiScreenAction.newScript(GuiV2LinkScript.guiV2link(
 *     "UiV2Provisioning.viewProvisioningOnSubject", "subjectId", subject.getId(), "sourceId", subject.getSourceId())));
 * </pre>
 */
public class GuiV2LinkScript {

  /** operation and parameter names are code constants: letters, digits, underscore, dot */
  private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_.]+$");

  /**
   * no instances
   */
  private GuiV2LinkScript() {
  }

  /**
   * script to navigate to a UI screen, safe for GuiScreenAction.newScript()
   * @param operation e.g. UiV2Provisioning.viewProvisioningOnSubject (a code constant)
   * @param paramNamesAndValues name, value, name, value...  names are code constants, values can
   * be anything (null is sent as an empty value)
   * @return e.g. guiV2link('operation=UiV2Provisioning.viewProvisioningOnSubject&amp;subjectId=a%27b')
   */
  public static String guiV2link(String operation, String... paramNamesAndValues) {
    return "guiV2link('" + escapeJavascriptSingleQuoted(url(operation, paramNamesAndValues)) + "')";
  }

  /**
   * the url (query string) part of the script, every value URL-encoded
   * @param operation e.g. UiV2Provisioning.viewProvisioningOnSubject
   * @param paramNamesAndValues name, value, name, value...
   * @return e.g. operation=UiV2Provisioning.viewProvisioningOnSubject&amp;subjectId=a%27b
   */
  public static String url(String operation, String... paramNamesAndValues) {
    checkName(operation);
    int length = paramNamesAndValues == null ? 0 : paramNamesAndValues.length;
    if (length % 2 != 0) {
      throw new IllegalArgumentException("paramNamesAndValues needs a value for each name: " + length);
    }
    StringBuilder result = new StringBuilder("operation=").append(operation);
    for (int i = 0; i < length; i += 2) {
      checkName(paramNamesAndValues[i]);
      result.append('&').append(paramNamesAndValues[i]).append('=').append(urlEncode(paramNamesAndValues[i + 1]));
    }
    return result.toString();
  }

  /**
   * names go in the script as is, so refuse anything that is not a plain name.  they are
   * constants in code, so this only catches programming mistakes
   * @param name operation or parameter name
   */
  private static void checkName(String name) {
    if (name == null || !NAME_PATTERN.matcher(name).matches()) {
      throw new IllegalArgumentException("Not a valid operation or parameter name: '" + name + "'");
    }
  }

  /**
   * URL-encode a value (UTF-8, like GrouperUtil.escapeUrlEncode).  the result only has letters,
   * digits and . - * _ + %, none of which can end a JavaScript string or start markup
   * @param value value or null
   * @return encoded value, empty for null
   */
  static String urlEncode(String value) {
    if (value == null) {
      return "";
    }
    try {
      return URLEncoder.encode(value, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new RuntimeException("UTF-8 not supported", uee);
    }
  }

  /**
   * escape a string to go between single quotes in a script.  after urlEncode() the values need
   * nothing, so this is a second layer: anything other than a letter, digit or one of
   * _ . - * + % = &amp; is written as a \\uXXXX escape, so a quote, backslash, line break or
   * &lt; can never end the string or the script block
   * @param string string
   * @return the escaped string
   */
  static String escapeJavascriptSingleQuoted(String string) {
    StringBuilder result = new StringBuilder(string.length());
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
          || "_.-*+%=&".indexOf(c) >= 0) {
        result.append(c);
      } else {
        String hex = Integer.toHexString(c);
        result.append("\\u");
        for (int pad = hex.length(); pad < 4; pad++) {
          result.append('0');
        }
        result.append(hex);
      }
    }
    return result.toString();
  }

}
