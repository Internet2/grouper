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

import java.util.List;

import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;

/**
 * UI bean container for MCP info page data. This container holds fields
 * specific to the MCP feature (server URL, registration token, access status).
 * OAuth consent page fields are in {@link OAuthContainer}.
 * @author mchyzer
 */
public class McpContainer {

  /**
   * whether MCP is enabled (grouper.is.mcp). Reads directly from config
   * so the MCP info page can display a warning when MCP is not enabled.
   * @return true if MCP is enabled
   */
  public boolean isMcpEnabled() {
    return GrouperHibernateConfig.retrieveConfig()
        .propertyValueBoolean("grouper.is.mcp", false);
  }

  /**
   * the MCP server URL (for the info page)
   */
  private String mcpServerUrl;

  /**
   * the logged-in user's Member UUID (for the info page)
   */
  private String memberUuid;

  /**
   * the display name of the logged-in user (for the info page)
   */
  private String loggedInUserName;

  /**
   * whether the user is in the MCP readonly group (for MCP info page access status)
   */
  private boolean allowedReadonly;

  /**
   * whether the user is in the MCP readwrite group (for MCP info page access status)
   */
  private boolean allowedReadwrite;

  /**
   * whether the user is in the MCP SQL readonly group (for MCP info page access status)
   */
  private boolean allowedSqlReadonly;

  /**
   * whether the user is in the MCP admin readonly group (for MCP info page access status)
   */
  private boolean allowedAdminReadonly;

  /**
   * whether the user is in the MCP admin readwrite group (for MCP info page access status)
   */
  private boolean allowedAdminReadwrite;

  /**
   * whether the user is in the WS authn allowed group (for MCP info page access status)
   */
  private boolean allowedWsAuthn;

  /**
   * whether WS basic auth is enabled on this deployment
   */
  private boolean wsBasicAuthnEnabled;

  /**
   * @return the mcpServerUrl
   */
  public String getMcpServerUrl() {
    return this.mcpServerUrl;
  }

  /**
   * @param mcpServerUrl the mcpServerUrl to set
   */
  public void setMcpServerUrl(String mcpServerUrl) {
    this.mcpServerUrl = mcpServerUrl;
  }

  /**
   * @return the memberUuid
   */
  public String getMemberUuid() {
    return this.memberUuid;
  }

  /**
   * @param memberUuid the memberUuid to set
   */
  public void setMemberUuid(String memberUuid) {
    this.memberUuid = memberUuid;
  }

  /**
   * @return the loggedInUserName
   */
  public String getLoggedInUserName() {
    return this.loggedInUserName;
  }

  /**
   * @param loggedInUserName the loggedInUserName to set
   */
  public void setLoggedInUserName(String loggedInUserName) {
    this.loggedInUserName = loggedInUserName;
  }

  /**
   * @return the allowedReadonly
   */
  public boolean isAllowedReadonly() {
    return this.allowedReadonly;
  }

  /**
   * @param allowedReadonly the allowedReadonly to set
   */
  public void setAllowedReadonly(boolean allowedReadonly) {
    this.allowedReadonly = allowedReadonly;
  }

  /**
   * @return the allowedReadwrite
   */
  public boolean isAllowedReadwrite() {
    return this.allowedReadwrite;
  }

  /**
   * @param allowedReadwrite the allowedReadwrite to set
   */
  public void setAllowedReadwrite(boolean allowedReadwrite) {
    this.allowedReadwrite = allowedReadwrite;
  }

  /**
   * @return the allowedSqlReadonly
   */
  public boolean isAllowedSqlReadonly() {
    return this.allowedSqlReadonly;
  }

  /**
   * @param allowedSqlReadonly the allowedSqlReadonly to set
   */
  public void setAllowedSqlReadonly(boolean allowedSqlReadonly) {
    this.allowedSqlReadonly = allowedSqlReadonly;
  }

  /**
   * @return the allowedAdminReadonly
   */
  public boolean isAllowedAdminReadonly() {
    return this.allowedAdminReadonly;
  }

  /**
   * @param allowedAdminReadonly the allowedAdminReadonly to set
   */
  public void setAllowedAdminReadonly(boolean allowedAdminReadonly) {
    this.allowedAdminReadonly = allowedAdminReadonly;
  }

  /**
   * @return the allowedAdminReadwrite
   */
  public boolean isAllowedAdminReadwrite() {
    return this.allowedAdminReadwrite;
  }

  /**
   * @param allowedAdminReadwrite the allowedAdminReadwrite to set
   */
  public void setAllowedAdminReadwrite(boolean allowedAdminReadwrite) {
    this.allowedAdminReadwrite = allowedAdminReadwrite;
  }

  /**
   * @return the allowedWsAuthn
   */
  public boolean isAllowedWsAuthn() {
    return this.allowedWsAuthn;
  }

  /**
   * @param allowedWsAuthn the allowedWsAuthn to set
   */
  public void setAllowedWsAuthn(boolean allowedWsAuthn) {
    this.allowedWsAuthn = allowedWsAuthn;
  }

  /**
   * @return the wsBasicAuthnEnabled
   */
  public boolean isWsBasicAuthnEnabled() {
    return this.wsBasicAuthnEnabled;
  }

  /**
   * @param wsBasicAuthnEnabled the wsBasicAuthnEnabled to set
   */
  public void setWsBasicAuthnEnabled(boolean wsBasicAuthnEnabled) {
    this.wsBasicAuthnEnabled = wsBasicAuthnEnabled;
  }

  /**
   * list of MCP tool log entries for the logged-in user (most recent first)
   */
  private List<GuiMcpToolLog> guiMcpToolLogs;

  /**
   * @return the guiMcpToolLogs
   */
  public List<GuiMcpToolLog> getGuiMcpToolLogs() {
    return this.guiMcpToolLogs;
  }

  /**
   * @param guiMcpToolLogs the guiMcpToolLogs to set
   */
  public void setGuiMcpToolLogs(List<GuiMcpToolLog> guiMcpToolLogs) {
    this.guiMcpToolLogs = guiMcpToolLogs;
  }

  /**
   * list of OAuth client registrations for the logged-in user
   */
  private List<GuiOAuthClient> guiOAuthClients;

  /**
   * @return the guiOAuthClients
   */
  public List<GuiOAuthClient> getGuiOAuthClients() {
    return this.guiOAuthClients;
  }

  /**
   * @param guiOAuthClients the guiOAuthClients to set
   */
  public void setGuiOAuthClients(List<GuiOAuthClient> guiOAuthClients) {
    this.guiOAuthClients = guiOAuthClients;
  }
}
