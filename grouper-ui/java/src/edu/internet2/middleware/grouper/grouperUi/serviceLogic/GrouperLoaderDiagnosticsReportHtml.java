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

/**
 * Turns the loader diagnostics report (built by UiV2GrouperLoader.loaderDiagnosticsRun and its
 * helpers) into HTML that is safe to put into the page with innerHtml.
 *
 * The report is plain text with a small, fixed set of markup: it is wrapped in a pre element and
 * each line starts with a colored label such as a font element with color green and the text
 * SUCCESS.  Everything else in it is data: loader settings (queries, groups like pattern, filters,
 * expressions), rows the loader query returns from an outside database or LDAP, subject ids, job
 * messages and stack traces.  Loader settings can be written by delegated loader editors and the
 * rows come from outside systems, so none of it may be read as HTML by the sysadmin who runs the
 * diagnostics.
 *
 * This class HTML-escapes the whole report and then puts back only the exact fixed tags the report
 * code writes (no attribute in them comes from data).  A data value that contains one of those exact
 * tags can at most change a color, never add an element or an attribute.  Escaping at the end
 * instead of at each of the hundreds of appends keeps the change to UiV2GrouperLoader one line in
 * every release, and new appends are covered without anyone remembering to escape them.
 *
 * No dependencies besides the JDK, so the same class works on every Grouper release (Java 8+).
 */
public class GrouperLoaderDiagnosticsReportHtml {

  /** the label colors the report code uses (gren is a typo in the report code, kept as is) */
  private static final String[] FONT_COLORS = new String[] {"blue", "green", "gren", "orange", "red"};

  /** the report starts with this */
  private static final String PRE_START = "<pre>";

  /** the report ends with this */
  private static final String PRE_END = "</pre>";

  /**
   * no instances
   */
  private GrouperLoaderDiagnosticsReportHtml() {
  }

  /**
   * escape the loader diagnostics report for innerHtml, keeping only its own fixed markup
   * @param report the report as built in loaderDiagnosticsRun (pre element, font labels, data)
   * @return the safe html
   */
  public static String toSafeHtml(String report) {
    if (report == null) {
      return "";
    }

    // the outer pre element is fixed markup: take it off before escaping so a pre end tag inside
    // the data stays escaped
    String inner = report;
    boolean wrapInPre = false;
    if (inner.startsWith(PRE_START) && inner.endsWith(PRE_END)
        && inner.length() >= PRE_START.length() + PRE_END.length()) {
      inner = inner.substring(PRE_START.length(), inner.length() - PRE_END.length());
      wrapInPre = true;
    }

    String result = escapeHtml(inner);

    // put back the exact label tags the report code writes, e.g. <font color='green'> and </font>
    // (single quotes become &#39; when escaped)
    for (String color : FONT_COLORS) {
      result = result.replace("&lt;font color=&#39;" + color + "&#39;&gt;", "<font color='" + color + "'>");
    }
    result = result.replace("&lt;/font&gt;", "</font>");

    return wrapInPre ? (PRE_START + result + PRE_END) : result;
  }

  /**
   * escape text for an html element body or a quoted attribute
   * @param input the text
   * @return the escaped text
   */
  public static String escapeHtml(String input) {
    if (input == null) {
      return "";
    }
    StringBuilder result = new StringBuilder(input.length() + 16);
    for (int i = 0; i < input.length(); i++) {
      char c = input.charAt(i);
      switch (c) {
        case '&':
          result.append("&amp;");
          break;
        case '<':
          result.append("&lt;");
          break;
        case '>':
          result.append("&gt;");
          break;
        case '"':
          result.append("&quot;");
          break;
        case '\'':
          result.append("&#39;");
          break;
        default:
          result.append(c);
      }
    }
    return result.toString();
  }
}
