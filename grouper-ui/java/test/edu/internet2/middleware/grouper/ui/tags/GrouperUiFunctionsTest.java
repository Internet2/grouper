/*******************************************************************************
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
 ******************************************************************************/
package edu.internet2.middleware.grouper.ui.tags;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * GRP-7389: the browser tab title is set from a script block, so the text is escaped for a
 * javascript string, not as html (html entities are not decoded in a script and showed in the tab
 * as &amp;lt; etc).  no database needed
 */
public class GrouperUiFunctionsTest extends TestCase {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperUiFunctionsTest.class);
  }

  /**
   * @param name
   */
  public GrouperUiFunctionsTest(String name) {
    super(name);
  }

  /**
   * plain text is unchanged, null is empty
   */
  public void testPlainText() {
    assertEquals("", GrouperUiFunctions.escapeForJavascriptStringInScriptBlock(null));
    assertEquals("Group: my group", GrouperUiFunctions.escapeForJavascriptStringInScriptBlock("Group: my group"));
  }

  /**
   * a name with html in it cannot close the script tag or break out of the string, and is not
   * turned into html entities
   */
  public void testCannotBreakOut() {

    String escaped = GrouperUiFunctions.escapeForJavascriptStringInScriptBlock(
        "a</script><script>alert(1)</script>\"'\\&");

    assertFalse("no html closing tag", escaped.contains("</"));
    assertFalse("no raw less than", escaped.contains("<"));
    assertFalse("no html entities", escaped.contains("&lt;") || escaped.contains("&amp;"));

    assertEquals("a\\u003c/script\\u003e\\u003cscript\\u003ealert(1)\\u003c/script\\u003e\\\"\\'\\\\\\u0026",
        escaped);
  }

  /**
   * line breaks, including the two javascript treats as line terminators, cannot end the string
   */
  public void testLineBreaks() {
    assertEquals("a\\nb\\rc\\u2028d\\u2029e",
        GrouperUiFunctions.escapeForJavascriptStringInScriptBlock("a\nb\rc d e"));
  }
}
