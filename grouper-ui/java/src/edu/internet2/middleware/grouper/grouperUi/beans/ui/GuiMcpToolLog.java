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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.internet2.middleware.grouper.mcp.GrouperMcpToolLog;

/**
 * GUI wrapper for GrouperMcpToolLog that provides formatted fields
 * for display in JSP pages.
 *
 * @author mchyzer
 */
public class GuiMcpToolLog {

  /** the underlying tool log object */
  private GrouperMcpToolLog grouperMcpToolLog;

  /**
   * constructor
   * @param grouperMcpToolLog1
   */
  public GuiMcpToolLog(GrouperMcpToolLog grouperMcpToolLog1) {
    this.grouperMcpToolLog = grouperMcpToolLog1;
  }

  /**
   * @return the underlying tool log object
   */
  public GrouperMcpToolLog getGrouperMcpToolLog() {
    return this.grouperMcpToolLog;
  }

  /**
   * @param grouperMcpToolLog1 the tool log object
   */
  public void setGrouperMcpToolLog(GrouperMcpToolLog grouperMcpToolLog1) {
    this.grouperMcpToolLog = grouperMcpToolLog1;
  }

  /**
   * get the started time formatted as yyyy-MM-dd HH:mm:ss
   * @return the formatted date string
   */
  public String getStartedTimeFormatted() {
    if (this.grouperMcpToolLog == null || this.grouperMcpToolLog.getStartedMicros() == 0) {
      return "";
    }
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
        new Date(this.grouperMcpToolLog.getStartedMicros() / 1000L));
  }

  /**
   * get the duration in milliseconds as a string
   * @return duration in ms, or empty string if not available
   */
  public String getDurationMs() {
    if (this.grouperMcpToolLog == null || this.grouperMcpToolLog.getDurationMicros() == null) {
      return "";
    }
    return String.valueOf(this.grouperMcpToolLog.getDurationMicros() / 1000);
  }

  /**
   * convert a list of GrouperMcpToolLog to a list of GuiMcpToolLog
   * @param grouperMcpToolLogs
   * @return the list of gui wrappers
   */
  public static List<GuiMcpToolLog> convertFromGrouperMcpToolLogs(
      List<GrouperMcpToolLog> grouperMcpToolLogs) {
    List<GuiMcpToolLog> result = new ArrayList<GuiMcpToolLog>();
    if (grouperMcpToolLogs != null) {
      for (GrouperMcpToolLog toolLog : grouperMcpToolLogs) {
        result.add(new GuiMcpToolLog(toolLog));
      }
    }
    return result;
  }
}
