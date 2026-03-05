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

import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.mcp.GrouperMcpToolLog;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;

/**
 * Utility class for MCP tool call audit logging and throttle checking.
 * Handles inserting audit log entries into grouper_mcp_tool_log and
 * checking rate limits based on recent call counts from that table.
 *
 * @author mchyzer
 */
public class GrouperMcpToolLogUtil {

  private static final Log LOG = GrouperUtil.getLog(GrouperMcpToolLogUtil.class);

  /**
   * Cache for throttle counts.  Key: MultiKey(memberInternalId, toolCategory).
   * Value: count of calls in the last minute.
   * Cache entries expire after 10 seconds to avoid querying on every request
   * while still being reasonably responsive to usage changes.
   */
  private static GrouperCache<MultiKey, Integer> throttleCountCache =
      new GrouperCache<MultiKey, Integer>(
          GrouperMcpToolLogUtil.class.getName() + ".throttleCountCache",
          5000, false, 10, 10, false);

  /**
   * Log an MCP tool call to the audit table.
   *
   * @param authUser the authenticated user
   * @param toolName the tool name
   * @param toolCategory the tool category
   * @param requestJson the request arguments as JSON string (will be truncated to 4000)
   * @param responseOrError the response text or error message (will be truncated to 4000)
   * @param isError true if this was an error
   * @param startedMicros micros since 1970 when the call started
   * @param durationMicros duration in microseconds (null if not available)
   */
  public static void logToolCall(GrouperMcpAuthUser authUser,
      String toolName, String toolCategory,
      String requestJson, String responseOrError,
      boolean isError, long startedMicros, Long durationMicros) {

    try {
      GrouperMcpToolLog logEntry = new GrouperMcpToolLog();
      logEntry.setOauthClientInternalId(authUser.getOauthClientInternalId());
      logEntry.setMemberInternalId(authUser.getMemberInternalId());
      logEntry.setToolName(toolName);
      logEntry.setToolCategory(toolCategory);
      logEntry.setRequest(GrouperUtil.abbreviate(requestJson, 4000));
      logEntry.setResponseOrError(GrouperUtil.abbreviate(responseOrError, 4000));
      logEntry.setIsError(isError ? "T" : "F");
      logEntry.setStartedMicros(startedMicros);
      logEntry.setDurationMicros(durationMicros);

      new GcDbAccess().storeToDatabase(logEntry);

      // invalidate throttle cache for this user+category since count has changed
      throttleCountCache.remove(
          new MultiKey(authUser.getMemberInternalId(), toolCategory));

    } catch (Exception e) {
      // audit logging should never cause the tool call to fail
      LOG.error("Error logging MCP tool call: " + toolName, e);
    }
  }

  /**
   * Check if the user has exceeded the throttle limit for the given tool category.
   *
   * @param authUser the authenticated user
   * @param toolCategory the tool category
   * @return null if the user is within limits, or an error message string if throttled
   */
  public static String checkThrottle(GrouperMcpAuthUser authUser, String toolCategory) {

    try {
      int limit = getThrottleLimit(authUser.getSubject().getId(), toolCategory);
      if (limit == -1) {
        return null;  // no limit
      }

      // check cache first
      MultiKey cacheKey = new MultiKey(authUser.getMemberInternalId(), toolCategory);
      Integer cachedCount = throttleCountCache.get(cacheKey);

      int count;
      if (cachedCount != null) {
        count = cachedCount;
      } else {
        // cache miss - query the audit table
        long oneMinuteAgoMicros = (System.currentTimeMillis() - 60000L) * 1000L;
        count = new GcDbAccess()
            .sql("select count(*) from grouper_mcp_tool_log "
                + "where member_internal_id = ? and tool_category = ? and started_micros > ?")
            .addBindVar(authUser.getMemberInternalId())
            .addBindVar(toolCategory)
            .addBindVar(oneMinuteAgoMicros)
            .select(int.class);
        throttleCountCache.put(cacheKey, count);
      }

      if (count >= limit) {
        return "Rate limit exceeded: " + count + "/" + limit
            + " calls per minute for " + toolCategory
            + " tools. Try again shortly.";
      }

      return null;  // within limits

    } catch (Exception e) {
      // throttle checking should never cause the tool call to fail
      LOG.error("Error checking MCP throttle for category: " + toolCategory, e);
      return null;  // fail open
    }
  }

  /**
   * Get the throttle limit for a user+category.
   * Checks for a per-user override first, then falls back to the category default.
   *
   * @param subjectId the subject id
   * @param toolCategory the tool category
   * @return the calls-per-minute limit, or -1 for no limit
   */
  private static int getThrottleLimit(String subjectId, String toolCategory) {

    // check per-user override first
    String overrideKey = "grouper.mcp.throttle.override."
        + subjectId + "." + toolCategory + ".callsPerMinute";
    int overrideValue = GrouperConfig.retrieveConfig()
        .propertyValueInt(overrideKey, Integer.MIN_VALUE);
    if (overrideValue != Integer.MIN_VALUE) {
      return overrideValue;
    }

    // fall back to category default
    String defaultKey = "grouper.mcp.throttle." + toolCategory + ".defaultCallsPerMinute";
    return GrouperConfig.retrieveConfig().propertyValueInt(defaultKey, -1);
  }
}
