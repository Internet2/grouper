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
package edu.internet2.middleware.grouper.ui.util;

import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import junit.framework.TestCase;
import junit.textui.TestRunner;

/**
 * Tests for GrouperUiErrorDisplay: what the UI shows when an unexpected exception escapes a UI
 * action.  Raw messages of unexpected exceptions must not reach the screen (they can carry object
 * names, uuids, SQL, config keys, file paths); messages meant for the user still do.
 *
 * Plain unit test, no database or request needed: the text lookup falls back to the built-in
 * default texts.  Before the fix GrouperUiErrorDisplay does not exist, so this does not compile.
 */
public class GrouperUiErrorDisplayTest extends TestCase {

  /**
   * run from the command line or eclipse
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperUiErrorDisplayTest("testUnexpectedExceptionHidesMessage"));
  }

  /**
   * @param name
   */
  public GrouperUiErrorDisplayTest(String name) {
    super(name);
  }

  /** text that must never reach the screen for an unexpected error */
  private static final String SECRET = "No custom ui config id found for group: secret:hr:salaryReview";

  /**
   * error ids are short, use a fixed alphabet, and differ between calls
   */
  public void testErrorId() {
    String errorId1 = GrouperUiErrorDisplay.newErrorId();
    String errorId2 = GrouperUiErrorDisplay.newErrorId();
    assertEquals(8, errorId1.length());
    assertTrue(errorId1, errorId1.matches("^[a-z0-9]+$"));
    assertFalse(errorId1.equals(errorId2));
  }

  /**
   * an unexpected exception (and one wrapped in another) shows the generic text and the error id,
   * not its message
   */
  public void testUnexpectedExceptionHidesMessage() {
    String errorId = GrouperUiErrorDisplay.newErrorId();

    String screen = GrouperUiErrorDisplay.screenMessage(new RuntimeException(SECRET), errorId);
    assertFalse(screen, screen.contains("salaryReview"));
    assertTrue(screen, screen.contains(errorId));

    // the reflection call wraps the real exception, the wrapper message often repeats the cause
    screen = GrouperUiErrorDisplay.screenMessage(
        new RuntimeException("Problem calling method: " + SECRET, new IllegalStateException(SECRET)), errorId);
    assertFalse(screen, screen.contains("salaryReview"));
    assertTrue(screen, screen.contains(errorId));

    // null message and null id do not blow up
    screen = GrouperUiErrorDisplay.screenMessage(new NullPointerException(), null);
    assertTrue(screen, screen.length() > 0);
  }

  /**
   * a privilege exception does not show its message (it can name objects the user cannot see)
   */
  public void testInsufficientPrivilegeHidesMessage() {
    String errorId = GrouperUiErrorDisplay.newErrorId();
    String screen = GrouperUiErrorDisplay.screenMessage(
        new RuntimeException("wrapper", new InsufficientPrivilegeException("cannot READ group secret:hr:salaryReview")), errorId);
    assertFalse(screen, screen.contains("salaryReview"));
    assertTrue(screen, screen.contains(errorId));
    assertFalse(screen, screen.equals(GrouperUiErrorDisplay.screenMessage(new RuntimeException("x"), errorId)));
  }

  /**
   * hook vetoes and validation errors are meant for the user: their text is shown, escaped
   */
  public void testUserFacingMessagesShownEscaped() {
    String errorId = GrouperUiErrorDisplay.newErrorId();

    String screen = GrouperUiErrorDisplay.screenMessage(
        new HookVeto(null, "Group names must start with <b>dept</b>"), errorId);
    assertTrue(screen, screen.contains("Group names must start with"));
    assertFalse(screen, screen.contains("<b>"));
    assertFalse(screen, screen.contains(errorId));

    // wrapped veto, as it arrives through the reflection call
    screen = GrouperUiErrorDisplay.screenMessage(
        new RuntimeException(SECRET, new HookVeto(null, "Not allowed by policy")), errorId);
    assertTrue(screen, screen.contains("Not allowed by policy"));
    assertFalse(screen, screen.contains("salaryReview"));

    screen = GrouperUiErrorDisplay.screenMessage(
        new GrouperValidationException("Name is too long: <script>x</script>"), errorId);
    assertTrue(screen, screen.contains("Name is too long"));
    assertFalse(screen, screen.contains("<script>"));
  }

  /**
   * a veto with no text falls back to the generic text
   */
  public void testEmptyVetoIsGeneric() {
    String errorId = GrouperUiErrorDisplay.newErrorId();
    String screen = GrouperUiErrorDisplay.screenMessage(new HookVeto(null, null), errorId);
    assertTrue(screen, screen.contains(errorId));
  }

}
