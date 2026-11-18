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
package edu.internet2.middleware.grouper.ws.rest;

import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * <pre>
 * Tests the grouperJsonResponseWrapper check (security advisory C1): GrouperRestServlet only prints
 * a wrapper that is a plain javascript function name, and refuses the request otherwise.
 *
 * Plain unit test, no database or servlet container needed.  Before the fix this does not
 * compile (WsRestJsonResponseWrapper does not exist).
 * </pre>
 */
public class WsRestJsonResponseWrapperTest extends TestCase {

  /**
   * run from eclipse or the command line
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WsRestJsonResponseWrapperTest.class);
  }

  /**
   * @param name
   */
  public WsRestJsonResponseWrapperTest(String name) {
    super(name);
  }

  /**
   * normal callback names, including dotted ones and exactly the max length, are allowed
   */
  public void testValidWrappers() {
    String[] valid = new String[] {"callback", "_cb", "cb1", "jQuery123_456", "app.handlers.onGroups",
        "a", repeat('a', WsRestJsonResponseWrapper.MAX_LENGTH)};
    for (String wrapper : valid) {
      assertTrue(wrapper, WsRestJsonResponseWrapper.isValidWrapper(wrapper));
      // must not throw
      WsRestJsonResponseWrapper.assertValidWrapper(wrapper);
    }
  }

  /**
   * anything that is not a plain identifier is refused: script or html, quotes, parens,
   * whitespace, leading digits, empty dotted parts, $, unicode, too long
   */
  public void testInvalidWrappers() {
    String[] invalid = new String[] {"alert(1);cb", "<script>alert(1)</script>", "cb//", "cb;", "a b",
        " cb", "cb\n", "1cb", "a.1b", ".cb", "cb.", "a..b", "$cb", "cb'", "cb\"", "caf\u00e9",
        "a-b", repeat('a', WsRestJsonResponseWrapper.MAX_LENGTH + 1)};
    for (String wrapper : invalid) {
      assertFalse(wrapper, WsRestJsonResponseWrapper.isValidWrapper(wrapper));
      try {
        WsRestJsonResponseWrapper.assertValidWrapper(wrapper);
        fail("should refuse: " + wrapper);
      } catch (GrouperRestInvalidRequest girr) {
        // the error must not reflect the request value back to the caller
        assertFalse(girr.getMessage(), girr.getMessage().contains(wrapper));
        assertTrue(girr.getMessage(), girr.getMessage().contains(WsRestJsonResponseWrapper.PARAM_NAME));
      }
    }
  }

  /**
   * missing or blank wrapper means no wrapping: not printed, and the request is not refused
   */
  public void testBlankWrapperNotRefused() {
    String[] blank = new String[] {null, "", "   "};
    for (String wrapper : blank) {
      assertFalse(String.valueOf(wrapper), WsRestJsonResponseWrapper.isValidWrapper(wrapper));
      // must not throw
      WsRestJsonResponseWrapper.assertValidWrapper(wrapper);
    }
  }

  /**
   * @param c
   * @param count
   * @return the char repeated
   */
  private static String repeat(char c, int count) {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < count; i++) {
      result.append(c);
    }
    return result.toString();
  }

}
