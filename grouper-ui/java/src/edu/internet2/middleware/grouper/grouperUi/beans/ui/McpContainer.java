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

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;

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
   * whether MCP is enabled but cannot be served because grouper.ws.url or grouper.ui.url is not
   * configured. The MCP info page shows this, since a deployment in that state answers every
   * MCP and OAuth request with an error and there is otherwise nothing to see but a log line.
   * @return true if MCP is enabled and its URLs are not configured
   */
  public boolean isMcpUrlsNotConfigured() {
    return isMcpEnabled() && GrouperOAuthStore.mcpUrlConfigurationError() != null;
  }

  /**
   * whether the logged-in user can see the MCP link on the miscellaneous page.
   * True if the user is a Grouper admin, readonly admin, or a member of any MCP role group.
   * @return true if the MCP link should be shown
   */
  public boolean isCanSeeMcpLink() {

    if (!isMcpEnabled()) {
      return false;
    }

    Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    if (PrivilegeHelper.isWheelOrRootOrReadonlyRoot(loggedInSubject)) {
      return true;
    }

    // check if user is in any MCP role group
    String[] mcpGroupConfigKeys = new String[] {
        "grouper.mcp.users.readonly",
        "grouper.mcp.users.readwrite",
        "grouper.mcp.users.canRunSqlReadonly",
        "grouper.mcp.users.adminReadonly",
        "grouper.mcp.users.adminReadWrite",
        "grouper.mcp.users.wsAuthnAllowed"
    };

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      for (String configKey : mcpGroupConfigKeys) {
        String groupName = GrouperConfig.retrieveConfig().propertyValueString(configKey);
        if (StringUtils.isNotBlank(groupName)) {
          Group group = GroupFinder.findByName(rootSession, groupName, false);
          if (group != null && group.hasMember(loggedInSubject)) {
            return true;
          }
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    return false;
  }

  /**
   * session duration in hours for display on the info page
   */
  private String sessionDurationHours;

  /**
   * @return the sessionDurationHours
   */
  public String getSessionDurationHours() {
    return this.sessionDurationHours;
  }

  /**
   * @param sessionDurationHours the sessionDurationHours to set
   */
  public void setSessionDurationHours(String sessionDurationHours) {
    this.sessionDurationHours = sessionDurationHours;
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
   * whether the user is allowed to register confidential OAuth clients (with client_secret)
   */
  private boolean allowedConfidentialClientRegistration;

  /**
   * @return the allowedConfidentialClientRegistration
   */
  public boolean isAllowedConfidentialClientRegistration() {
    return this.allowedConfidentialClientRegistration;
  }

  /**
   * @param allowedConfidentialClientRegistration the allowedConfidentialClientRegistration to set
   */
  public void setAllowedConfidentialClientRegistration(boolean allowedConfidentialClientRegistration) {
    this.allowedConfidentialClientRegistration = allowedConfidentialClientRegistration;
  }

  /**
   * registration result fields (set after registering a confidential client)
   */
  private String registeredClientId;

  /**
   * @return the registeredClientId
   */
  public String getRegisteredClientId() {
    return this.registeredClientId;
  }

  /**
   * @param registeredClientId the registeredClientId to set
   */
  public void setRegisteredClientId(String registeredClientId) {
    this.registeredClientId = registeredClientId;
  }

  private String registeredClientSecret;

  /**
   * @return the registeredClientSecret
   */
  public String getRegisteredClientSecret() {
    return this.registeredClientSecret;
  }

  /**
   * @param registeredClientSecret the registeredClientSecret to set
   */
  public void setRegisteredClientSecret(String registeredClientSecret) {
    this.registeredClientSecret = registeredClientSecret;
  }

  private String registeredAuthorizationUrl;

  /**
   * @return the registeredAuthorizationUrl
   */
  public String getRegisteredAuthorizationUrl() {
    return this.registeredAuthorizationUrl;
  }

  /**
   * @param registeredAuthorizationUrl the registeredAuthorizationUrl to set
   */
  public void setRegisteredAuthorizationUrl(String registeredAuthorizationUrl) {
    this.registeredAuthorizationUrl = registeredAuthorizationUrl;
  }

  private String registeredTokenUrl;

  /**
   * @return the registeredTokenUrl
   */
  public String getRegisteredTokenUrl() {
    return this.registeredTokenUrl;
  }

  /**
   * @param registeredTokenUrl the registeredTokenUrl to set
   */
  public void setRegisteredTokenUrl(String registeredTokenUrl) {
    this.registeredTokenUrl = registeredTokenUrl;
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
