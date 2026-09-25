/*******************************************************************************
 * Copyright 2024 Internet2
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
package edu.internet2.middleware.grouper.ws.mcp;

import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;

/**
 * Utility class for building MCP tool error responses.
 * <p>Full Java stack traces are useful for troubleshooting but they disclose internal
 * class names, SQL, file system paths, and third party library versions to the AI client
 * and to whoever operates it.  The MCP specification says servers should sanitize tool
 * outputs, so the stack trace is only appended for members of the group configured in
 * grouper.mcp.users.canSeeStackTraces (the Grouper sysadmin group by default).  The
 * exception itself is always logged on the server no matter who made the call.</p>
 * <p>The rule itself lives in
 * {@link GrouperServiceUtils#mcpCanSeeStackTraces(edu.internet2.middleware.subject.Subject)}
 * so that the WS layer can apply the same rule to the stack traces it puts in
 * WsResultMeta result messages, which MCP tools hand back to the client.</p>
 *
 * @author mchyzer
 */
public class GrouperMcpErrorUtils {

  /**
   * Build the stack trace suffix to append to an MCP tool error message.
   * Returns the full stack trace prefixed with two newlines if the authenticated user is
   * allowed to see stack traces, otherwise the empty string.
   *
   * @param authUser the authenticated user, can be null
   * @param throwable the exception, can be null
   * @return the text to append to the error message, never null
   */
  public static String stackTraceIfAllowed(GrouperMcpAuthUser authUser, Throwable throwable) {

    if (throwable == null || !canSeeStackTraces(authUser)) {
      return "";
    }

    return "\n\n" + GrouperUtil.getFullStackTrace(throwable);
  }

  /**
   * Check if the authenticated user is a member of the group configured in
   * grouper.mcp.users.canSeeStackTraces.  If that config is blank then nobody sees
   * stack traces in MCP tool responses.
   *
   * @param authUser the authenticated user, can be null
   * @return true if the user may see full stack traces
   */
  public static boolean canSeeStackTraces(GrouperMcpAuthUser authUser) {

    if (authUser == null) {
      return false;
    }

    return GrouperServiceUtils.mcpCanSeeStackTraces(authUser.getSubject());
  }
}
