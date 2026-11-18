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
package edu.internet2.middleware.grouper.ui.tags;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * <pre>
 * GrouperComboboxTag2 must encode the values it writes into the page (value, valueLabel,
 * filterOperation, additionalFormElementNames) for the context they land in, so a value with a
 * closing script tag, quotes or angle brackets cannot end the script element or the HTML attribute
 * (the rule-builder screens reflect request parameters into value and valueLabel; group import and
 * the custom UI pass sourceId||subjectId).  Normal values must decode back to exactly what they were.
 *
 * Works on both tag implementations:
 * - 6.x / 7.x (Tom Select): the values are JS string arguments of grouperRegisterCombobox(...) in a
 *   script element.  The test parses the call and decodes the JS strings.
 * - 2.6.19 / 4.x / 5.x (dojo): value is an HTML attribute of the input, filterOperation is a JS
 *   string inside the data-dojo-props attribute.  The test HTML-decodes (and JS-decodes) them.
 *
 * No database or JSP container: it calls renderHtml() (doTag() prints exactly that).  Before the
 * fix this does not compile (renderHtml does not exist) and the output is not inert.
 * </pre>
 */
public class GrouperComboboxTag2XssTest extends TestCase {

  /** payload with every character class that matters: both quotes, backslash, closing script tag,
   * an element with an inline handler, CR/LF/tab, the JS line separators, ampersand and slash */
  private static final String HOSTILE = "a'b\"c\\d</script><img id=secPatch139xss src=x onerror=alert(139)>"
      + "\r\n\t\u2028\u2029&amp;/</SCRIPT >e";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperComboboxTag2XssTest.class);
  }

  /**
   * @param name
   */
  public GrouperComboboxTag2XssTest(String name) {
    super(name);
  }

  /**
   * hostile value, label, filter operation and additional form element names: output is inert,
   * and each value decodes back to the original string
   */
  public void testHostileValuesAreInertAndRoundTrip() {
    String filterOperation = "../app/UiV2Group.addMemberFilter?groupId=" + HOSTILE;
    GrouperComboboxTag2 tag = newTag("secPatch139Combo", HOSTILE, HOSTILE, filterOperation, HOSTILE);
    String html = tag.renderHtml();

    assertInert(html);

    Rendered rendered = parse(html, "secPatch139Combo");
    assertEquals(html, HOSTILE, rendered.value);
    assertEquals(html, filterOperation, rendered.filterOperation);
    assertEquals(html, HOSTILE, rendered.additionalFormElementNames);
    if (rendered.isTomSelect && hasValueLabel()) {
      assertEquals(html, HOSTILE, rendered.valueLabel);
    }
  }

  /**
   * normal values (a sourceId||subjectId, a group name, a filter url with a query string) render
   * and decode exactly as given, and a plain id still appears literally in the output
   */
  public void testNormalValuesRoundTrip() {
    String[] values = new String[] {"jdbc||test.subject.0", "test:someFolder:some_group-name.1",
        "a1b2c3d4e5f60718293a4b5c6d7e8f90"};
    for (String value : values) {
      String filterOperation = "../app/UiV2Group.addMemberFilter?groupId=abc123&sourceId=jdbc";
      GrouperComboboxTag2 tag = newTag("normalCombo", value, value, filterOperation, "someOtherFieldName");
      String html = tag.renderHtml();
      assertInert(html);
      Rendered rendered = parse(html, "normalCombo");
      assertEquals(html, value, rendered.value);
      assertEquals(html, filterOperation, rendered.filterOperation);
      assertEquals(html, "someOtherFieldName", rendered.additionalFormElementNames);
      if (rendered.isTomSelect && hasValueLabel()) {
        assertEquals(html, value, rendered.valueLabel);
      }
    }

    // an id with no special characters is emitted exactly as before the fix
    GrouperComboboxTag2 tag = newTag("plainCombo", "a1b2c3d4e5f6", null, "UiV2Group.addMemberFilter", null);
    String html = tag.renderHtml();
    assertTrue(html, html.contains("'a1b2c3d4e5f6'") || html.contains(" value=\"a1b2c3d4e5f6\" "));
    assertTrue(html, html.contains("UiV2Group.addMemberFilter"));
  }

  /**
   * a blank value still renders no initial value
   */
  public void testBlankValue() {
    GrouperComboboxTag2 tag = newTag("blankCombo", null, null, "../app/UiV2Group.addMemberFilter", null);
    String html = tag.renderHtml();
    assertInert(html);
    Rendered rendered = parse(html, "blankCombo");
    assertNull(html, rendered.value);
  }

  /**
   * the encoder alone: nothing HTML-significant survives, and JS decodes it back
   */
  public void testEncoder() {
    String encoded = GrouperComboboxTag2.escapeJavascriptStringInScript(HOSTILE);
    for (char c : new char[] {'<', '>', '&', '\r', '\n', '\u2028', '\u2029'}) {
      assertEquals(encoded, -1, encoded.indexOf(c));
    }
    // quotes may appear, but only backslash-escaped (they end the JavaScript string otherwise)
    for (int i = 0; i < encoded.length(); i++) {
      char c = encoded.charAt(i);
      if (c == '\\') {
        i++;
      } else {
        assertTrue(encoded + " unescaped quote at " + i, c != '\'' && c != '"');
      }
    }
    assertEquals(HOSTILE, decodeJsString(encoded));
    assertEquals("", GrouperComboboxTag2.escapeJavascriptStringInScript(null));
    assertEquals("jdbc||test.subject.0", GrouperComboboxTag2.escapeJavascriptStringInScript("jdbc||test.subject.0"));

    String attr = GrouperComboboxTag2.escapeHtmlAttribute(HOSTILE);
    for (char c : new char[] {'<', '>', '\'', '"'}) {
      assertEquals(attr, -1, attr.indexOf(c));
    }
    assertEquals(HOSTILE, decodeHtml(attr));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * build a tag; valueLabel is set by reflection since 2.6.19 to 7.4.0 have no such attribute
   */
  private static GrouperComboboxTag2 newTag(String idBase, String value, String valueLabel,
      String filterOperation, String additionalFormElementNames) {
    GrouperComboboxTag2 tag = new GrouperComboboxTag2();
    tag.setIdBase(idBase);
    tag.setValue(value);
    tag.setFilterOperation(filterOperation);
    tag.setAdditionalFormElementNames(additionalFormElementNames);
    if (valueLabel != null && hasValueLabel()) {
      try {
        GrouperComboboxTag2.class.getMethod("setValueLabel", String.class).invoke(tag, valueLabel);
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return tag;
  }

  /**
   * @return if this release's tag has a valueLabel attribute (6.5.0, 7.5.0 and later)
   */
  private static boolean hasValueLabel() {
    for (Method method : GrouperComboboxTag2.class.getMethods()) {
      if ("setValueLabel".equals(method.getName())) {
        return true;
      }
    }
    return false;
  }

  /**
   * no injected element, every script element the tag opens is closed exactly once, and the
   * payload text never appears raw
   */
  private static void assertInert(String html) {
    String lower = html.toLowerCase(Locale.ROOT);
    // no element from the payload (the dojo tag writes its own busy image, "<img " + newline); the
    // payload text itself may appear inside a JavaScript string, with its less than signs encoded
    assertEquals(html, -1, lower.indexOf("<img id"));
    assertEquals(html, count(lower, "<script"), count(lower, "</script"));
    assertEquals(html, -1, html.indexOf("</script><img"));
    // every less than sign starts a tag the tag itself writes
    String[] allowed = new String[] {"<script", "</script", "<table", "</table", "<tr", "</tr", "<td", "</td",
        "<input", "<span", "</span", "<div", "</div", "<img \n"};
    int index = -1;
    while ((index = lower.indexOf('<', index + 1)) != -1) {
      boolean ok = false;
      for (String prefix : allowed) {
        if (lower.startsWith(prefix, index)) {
          ok = true;
          break;
        }
      }
      assertTrue("unexpected markup at " + index + ": " + html, ok);
    }
  }

  /**
   * @return number of occurrences
   */
  private static int count(String haystack, String needle) {
    int result = 0;
    int index = -1;
    while ((index = haystack.indexOf(needle, index + 1)) != -1) {
      result++;
    }
    return result;
  }

  /** decoded values of one rendered combobox */
  private static class Rendered {
    boolean isTomSelect;
    String value;
    String valueLabel;
    String filterOperation;
    String additionalFormElementNames;
  }

  /**
   * pull the values back out of the html the way the browser would see them
   */
  private static Rendered parse(String html, String idBase) {
    Rendered rendered = new Rendered();
    int call = html.indexOf("grouperRegisterCombobox(");
    if (call != -1) {
      // 6.x / 7.x: grouperRegisterCombobox('#idId', 'url', 'names' or null, 'value' or null, {...})
      rendered.isTomSelect = true;
      List<Object> args = parseJsArgs(html, call + "grouperRegisterCombobox(".length());
      assertEquals(html, 5, args.size());
      assertEquals(html, "#" + idBase + "Id", args.get(0));
      rendered.filterOperation = (String)args.get(1);
      rendered.additionalFormElementNames = (String)args.get(2);
      rendered.value = (String)args.get(3);
      rendered.valueLabel = (String)args.get(4);
      return rendered;
    }
    // 2.6.19 / 4.x / 5.x dojo: attributes
    String inputStart = "<input id=\"" + idBase + "Id\"";
    int input = html.indexOf(inputStart);
    assertTrue(html, input != -1);
    int inputEnd = html.indexOf("/>", input);
    String inputTag = html.substring(input, inputEnd);
    rendered.value = attribute(inputTag, " value=\"");
    String store = html.substring(html.indexOf("id=\"" + idBase + "StoreId\""));
    store = store.substring(0, store.indexOf('>'));
    rendered.additionalFormElementNames = attribute(store, " formElementNamesToSend=\"");
    String props = attribute(store, " data-dojo-props=\"");
    assertTrue(props, props.startsWith("url:'") && props.endsWith("'"));
    // the props are evaluated as JS by dojo
    rendered.filterOperation = decodeJsString(props.substring("url:'".length(), props.length() - 1));
    return rendered;
  }

  /**
   * @return the HTML-decoded attribute value after the marker, or null if absent
   */
  private static String attribute(String tag, String marker) {
    int start = tag.indexOf(marker);
    if (start == -1) {
      return null;
    }
    start += marker.length();
    int end = tag.indexOf('"', start);
    assertTrue(tag, end != -1);
    return decodeHtml(tag.substring(start, end));
  }

  /**
   * parse the arguments of a JS call: single-quoted strings, null, and one object literal whose
   * valueLabel string (if any) is returned in its place
   */
  private static List<Object> parseJsArgs(String html, int index) {
    List<Object> result = new ArrayList<Object>();
    while (true) {
      while (html.charAt(index) == ' ' || html.charAt(index) == ',') {
        index++;
      }
      char c = html.charAt(index);
      if (c == ')') {
        return result;
      }
      if (c == '\'') {
        int end = endOfJsString(html, index + 1);
        result.add(decodeJsString(html.substring(index + 1, end)));
        index = end + 1;
      } else if (html.startsWith("null", index)) {
        result.add(null);
        index += 4;
      } else if (c == '{') {
        String valueLabel = null;
        index++;
        while (html.charAt(index) != '}') {
          if (html.charAt(index) == '\'') {
            int end = endOfJsString(html, index + 1);
            String before = html.substring(0, index);
            if (before.endsWith("valueLabel: ")) {
              valueLabel = decodeJsString(html.substring(index + 1, end));
            }
            index = end + 1;
          } else {
            index++;
          }
        }
        index++;
        result.add(valueLabel);
      } else {
        fail("unexpected JS at " + index + ": " + html);
      }
    }
  }

  /**
   * @return index of the closing single quote of a JS string whose content starts at index
   */
  private static int endOfJsString(String js, int index) {
    while (true) {
      char c = js.charAt(index);
      // a raw line terminator would be a syntax error, i.e. the value broke the script
      assertTrue(js, c != '\r' && c != '\n' && c != '\u2028' && c != '\u2029');
      if (c == '\\') {
        index += 2;
        continue;
      }
      if (c == '\'') {
        return index;
      }
      index++;
    }
  }

  /**
   * decode the content of a JS string literal (the escapes JS defines that the tag can emit)
   */
  private static String decodeJsString(String js) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < js.length(); i++) {
      char c = js.charAt(i);
      if (c != '\\') {
        result.append(c);
        continue;
      }
      char next = js.charAt(++i);
      switch (next) {
        case 'n':
          result.append('\n');
          break;
        case 'r':
          result.append('\r');
          break;
        case 't':
          result.append('\t');
          break;
        case 'b':
          result.append('\b');
          break;
        case 'f':
          result.append('\f');
          break;
        case 'u':
          result.append((char)Integer.parseInt(js.substring(i + 1, i + 5), 16));
          i += 4;
          break;
        case 'x':
          result.append((char)Integer.parseInt(js.substring(i + 1, i + 3), 16));
          i += 2;
          break;
        default:
          // \\ \' \" \/ and any other escaped char is the char itself
          result.append(next);
      }
    }
    return result.toString();
  }

  /**
   * decode the HTML character references the tag can emit
   */
  private static String decodeHtml(String text) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c != '&') {
        result.append(c);
        continue;
      }
      int semi = text.indexOf(';', i);
      assertTrue(text, semi != -1);
      String entity = text.substring(i + 1, semi);
      if ("amp".equals(entity)) {
        result.append('&');
      } else if ("lt".equals(entity)) {
        result.append('<');
      } else if ("gt".equals(entity)) {
        result.append('>');
      } else if ("quot".equals(entity)) {
        result.append('"');
      } else if ("apos".equals(entity)) {
        result.append('\'');
      } else if (entity.startsWith("#x") || entity.startsWith("#X")) {
        result.append((char)Integer.parseInt(entity.substring(2), 16));
      } else if (entity.startsWith("#")) {
        result.append((char)Integer.parseInt(entity.substring(1)));
      } else {
        fail("unknown entity " + entity + " in " + text);
      }
      i = semi;
    }
    return result.toString();
  }
}
