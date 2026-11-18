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

import java.security.SecureRandom;

import edu.internet2.middleware.grouper.exception.GrouperValidationException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;

/**
 * Decides what the UI shows a user when an unexpected exception escapes a UI action
 * (GrouperUiRestServlet).  Before this class the servlet showed the raw exception message,
 * which can carry object names the user may not see, uuids, SQL, config keys or file paths.
 *
 * Now only exceptions that are meant for users keep their text:
 * <ul>
 *   <li>HookVeto: the localized reason key text, or the (escaped) reason</li>
 *   <li>GrouperValidationException: the (escaped) message, it describes the user's own input</li>
 * </ul>
 * InsufficientPrivilegeException gets a generic "not allowed" text (its message can name objects
 * the user cannot see), and everything else gets a generic error text.  Both generic texts carry a
 * short error id that the caller also writes to the log next to the full stack, so an
 * administrator can find the real error.
 *
 * This class only uses APIs present in every Grouper 5.x, 6.x and 7.x release (and no commons-lang,
 * whose package differs between 5.x and 6.x) so the same file ships in every patch set.
 *
 * Every returned string is HTML that is safe to put on the screen.
 */
public class GrouperUiErrorDisplay {

  /**
   * text key for the generic error (mainline adds it to grouper.textNg base properties;
   * patched releases fall back to the default below)
   */
  public static final String TEXT_KEY_UNEXPECTED = "guiMiscErrorUnexpected";

  /**
   * text key for the generic not allowed error
   */
  public static final String TEXT_KEY_NOT_ALLOWED = "guiMiscErrorNotAllowed";

  /**
   * default text if the text key is not in the text config
   */
  public static final String DEFAULT_UNEXPECTED = "An unexpected error occurred.  If this keeps happening, give your Grouper administrator this error id:";

  /**
   * default text if the text key is not in the text config
   */
  public static final String DEFAULT_NOT_ALLOWED = "You are not allowed to perform this action.  If you think you should be, give your Grouper administrator this error id:";

  /**
   * how deep to look in the cause chain for a user-facing exception
   */
  private static final int MAX_CAUSE_DEPTH = 10;

  /**
   * random for error ids, ids do not need to be secret but should not collide
   */
  private static final SecureRandom secureRandom = new SecureRandom();

  /**
   * characters in an error id, no vowels so ids do not spell words, no look-alike characters
   */
  private static final String ERROR_ID_CHARS = "bcdfghjkmnpqrstvwxyz23456789";

  /**
   * new short error id to show the user and write in the log, e.g. k7m2xq9d
   * @return the error id
   */
  public static String newErrorId() {
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < 8; i++) {
      result.append(ERROR_ID_CHARS.charAt(secureRandom.nextInt(ERROR_ID_CHARS.length())));
    }
    return result.toString();
  }

  /**
   * HTML for the screen for an exception that escaped a UI action.  Does not log; the caller logs
   * the full exception with the same error id.
   * @param throwable is the exception
   * @param errorId from newErrorId(), shown for the generic texts
   * @return the html safe message (without the "Error:" prefix the caller adds)
   */
  public static String screenMessage(Throwable throwable, String errorId) {

    // walk the cause chain: the reflection call can wrap the real exception
    Throwable current = throwable;
    for (int depth = 0; current != null && depth < MAX_CAUSE_DEPTH; depth++) {

      if (current instanceof HookVeto) {
        String hookVetoMessage = hookVetoMessage((HookVeto)current);
        if (hookVetoMessage != null) {
          return hookVetoMessage;
        }
      }

      if (current instanceof GrouperValidationException) {
        String message = current.getMessage();
        if (!isBlank(message)) {
          // validation errors describe what the user typed, they are meant for the user
          return GrouperUiUtils.escapeHtml(message, true);
        }
      }

      if (current instanceof InsufficientPrivilegeException) {
        // the message can name groups or folders the user cannot see, so do not show it
        return textWithErrorId(TEXT_KEY_NOT_ALLOWED, DEFAULT_NOT_ALLOWED, errorId);
      }

      // stop if a cause points at itself
      if (current.getCause() == current) {
        break;
      }
      current = current.getCause();
    }

    return textWithErrorId(TEXT_KEY_UNEXPECTED, DEFAULT_UNEXPECTED, errorId);
  }

  /**
   * message for a hook veto, same rules as GrouperUiUtils.vetoHandle: localized text of the reason
   * key if there is one, else the escaped reason
   * @param hookVeto is the veto
   * @return the html safe message or null if the veto has no text
   */
  private static String hookVetoMessage(HookVeto hookVeto) {
    String message = textOrNullSafe(hookVeto.getReasonKey());
    if (isBlank(message)) {
      message = hookVeto.getReason();
      if (isBlank(message)) {
        return null;
      }
      message = GrouperUiUtils.escapeHtml(message, true);
    }
    return message;
  }

  /**
   * text from the text config (or the default) plus the escaped error id
   * @param key text key
   * @param defaultText if the key is not configured
   * @param errorId error id
   * @return the html
   */
  private static String textWithErrorId(String key, String defaultText, String errorId) {
    String text = textOrNullSafe(key);
    if (isBlank(text)) {
      text = defaultText;
    }
    if (isBlank(errorId)) {
      return text;
    }
    return text + " " + GrouperUiUtils.escapeHtml(errorId, true);
  }

  /**
   * text config lookup that never throws, since this runs while handling another error
   * @param key text key
   * @return the text or null
   */
  private static String textOrNullSafe(String key) {
    if (isBlank(key)) {
      return null;
    }
    try {
      return TextContainer.textOrNull(key);
    } catch (RuntimeException re) {
      // no text container (e.g. no request), use the default
      return null;
    }
  }

  /**
   * null or whitespace only, local so this class does not need commons-lang
   * @param string string
   * @return true if blank
   */
  private static boolean isBlank(String string) {
    return string == null || string.trim().length() == 0;
  }

}
