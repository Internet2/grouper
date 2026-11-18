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

import java.util.regex.Pattern;

/**
 * <pre>
 * Validates the grouperJsonResponseWrapper request parameter of the WS REST servlet.
 *
 * When ws.allowJsonWrapper is true, GrouperRestServlet prints the wrapper value in front
 * of the JSON response ( wrapper(json) ).  The value comes straight from the request, so
 * without a check it is reflected verbatim at the start of the response body.  This class
 * only allows a plain JavaScript function name: letters, digits and underscore, optionally
 * dotted (e.g. myCallback or app.handlers.onGroups), each part not starting with a digit,
 * at most 64 characters.  Anything else is refused.
 *
 * Kept in its own class (instead of inline in the servlet) so the rule is one version for
 * every release and can be unit tested without a servlet container.  Uses only the JDK and
 * GrouperRestInvalidRequest (same package and String constructor in every 5.x-7.x release).
 * </pre>
 */
public class WsRestJsonResponseWrapper {

  /** name of the request parameter holding the wrapper (javascript function name) */
  public static final String PARAM_NAME = "grouperJsonResponseWrapper";

  /** max length of a wrapper name, long enough for any sane callback name */
  public static final int MAX_LENGTH = 64;

  /** one or more identifier parts separated by dots, a part cannot start with a digit */
  private static final Pattern WRAPPER_PATTERN = Pattern.compile(
      "^[A-Za-z_][A-Za-z0-9_]*(\\.[A-Za-z_][A-Za-z0-9_]*)*$");

  /**
   * no instances, static utility
   */
  private WsRestJsonResponseWrapper() {
  }

  /**
   * see if a wrapper value is a plain javascript identifier (optionally dotted) that is
   * safe to print in front of the json response
   * @param wrapper value of the grouperJsonResponseWrapper param
   * @return true if it may be printed, false if null, blank, too long, or has other characters
   */
  public static boolean isValidWrapper(String wrapper) {
    if (wrapper == null || wrapper.length() == 0 || wrapper.length() > MAX_LENGTH) {
      return false;
    }
    return WRAPPER_PATTERN.matcher(wrapper).matches();
  }

  /**
   * refuse the request if a wrapper is sent and it is not a plain javascript identifier.
   * A missing or blank value is allowed (no wrapping).  The message does not echo the value,
   * so nothing from the request is reflected back in the error response.
   * @param wrapper value of the grouperJsonResponseWrapper param, may be null
   * @throws GrouperRestInvalidRequest if a non-empty value is not a valid wrapper
   */
  public static void assertValidWrapper(String wrapper) {
    // blank means no wrapping, same as the servlet's StringUtils.isBlank check
    if (wrapper == null || wrapper.trim().length() == 0) {
      return;
    }
    if (!isValidWrapper(wrapper)) {
      throw new GrouperRestInvalidRequest("Invalid " + PARAM_NAME
          + " (length " + wrapper.length() + "): it must be a javascript function name of letters, "
          + "digits, underscores and dots, not starting a part with a digit, at most "
          + MAX_LENGTH + " characters");
    }
  }

}
