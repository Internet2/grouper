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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthClient;
import edu.internet2.middleware.grouper.authentication.GrouperOAuthStore;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction.GuiMessageType;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiMcpToolLog;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiOAuthClient;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.McpContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.mcp.GrouperMcpToolLog;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;

/**
 * Service logic for the MCP info page. Shows MCP connection details,
 * registration token, and access status.
 * <p>
 * The OAuth consent flow (authorize and submitAuthorize) has been moved
 * to {@link UiV2OAuth}.
 * </p>
 *
 * @author mchyzer
 */
public class UiV2Mcp extends UiServiceLogicBase {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(UiV2Mcp.class);

  /**
   * Show the MCP info page with connection details, bearer token, and instructions.
   * Accessible to all logged-in users via the Miscellaneous screen.
   * @param request
   * @param response
   */
  public void mcpInfo(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      GrouperRequestContainer grouperRequestContainer =
          GrouperRequestContainer.retrieveFromRequestOrCreate();

      McpContainer mcpContainer = grouperRequestContainer.getMcpContainer();

      // MCP server URL from config (normalized without trailing slash)
      String mcpServerUrl = GrouperConfig.getGrouperWsUrl(false);
      mcpContainer.setMcpServerUrl(mcpServerUrl);

      // look up member for logged-in user
      Member member = MemberFinder.findBySubject(grouperSession, loggedInSubject, true);
      String memberUuid = member.getUuid();
      mcpContainer.setMemberUuid(memberUuid);

      mcpContainer.setLoggedInUserName(loggedInSubject.getName());

      // check group memberships for access status display
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        // readonly group
        String readonlyGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.readonly");
        if (StringUtils.isNotBlank(readonlyGroupName)) {
          Group readonlyGroup = GroupFinder.findByName(rootSession, readonlyGroupName, false);
          if (readonlyGroup != null && readonlyGroup.hasMember(loggedInSubject)) {
            mcpContainer.setAllowedReadonly(true);
          }
        }

        // readwrite group
        String readwriteGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.readwrite");
        if (StringUtils.isNotBlank(readwriteGroupName)) {
          Group readwriteGroup = GroupFinder.findByName(rootSession, readwriteGroupName, false);
          if (readwriteGroup != null && readwriteGroup.hasMember(loggedInSubject)) {
            mcpContainer.setAllowedReadwrite(true);
          }
        }

        // SQL readonly group
        String sqlReadonlyGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.canRunSqlReadonly");
        if (StringUtils.isNotBlank(sqlReadonlyGroupName)) {
          Group sqlReadonlyGroup = GroupFinder.findByName(rootSession, sqlReadonlyGroupName, false);
          if (sqlReadonlyGroup != null && sqlReadonlyGroup.hasMember(loggedInSubject)) {
            mcpContainer.setAllowedSqlReadonly(true);
          }
        }

        // admin readonly group
        String adminReadonlyGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.adminReadonly");
        if (StringUtils.isNotBlank(adminReadonlyGroupName)) {
          Group adminReadonlyGroup = GroupFinder.findByName(rootSession, adminReadonlyGroupName, false);
          if (adminReadonlyGroup != null && adminReadonlyGroup.hasMember(loggedInSubject)) {
            mcpContainer.setAllowedAdminReadonly(true);
          }
        }

        // admin readwrite group
        String adminReadWriteGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.adminReadWrite");
        if (StringUtils.isNotBlank(adminReadWriteGroupName)) {
          Group adminReadWriteGroup = GroupFinder.findByName(rootSession, adminReadWriteGroupName, false);
          if (adminReadWriteGroup != null && adminReadWriteGroup.hasMember(loggedInSubject)) {
            mcpContainer.setAllowedAdminReadwrite(true);
          }
        }

        // WS authn allowed group
        String wsAuthnGroupName = GrouperConfig.retrieveConfig()
            .propertyValueString("grouper.mcp.users.wsAuthnAllowed");
        if (StringUtils.isNotBlank(wsAuthnGroupName)) {
          Group wsAuthnGroup = GroupFinder.findByName(rootSession, wsAuthnGroupName, false);
          if (wsAuthnGroup != null && wsAuthnGroup.hasMember(loggedInSubject)) {
            mcpContainer.setAllowedWsAuthn(true);
          }
        }

        // readwrite implies readonly, admin readwrite implies admin readonly
        if (mcpContainer.isAllowedReadwrite()) {
          mcpContainer.setAllowedReadonly(true);
        }
        if (mcpContainer.isAllowedAdminReadwrite()) {
          mcpContainer.setAllowedAdminReadonly(true);
        }

        // confidential OAuth client registration: allowed for sysadmins or group members
        if (PrivilegeHelper.isWheelOrRoot(loggedInSubject)) {
          mcpContainer.setAllowedConfidentialClientRegistration(true);
        } else {
          String confidentialClientGroupName = GrouperConfig.retrieveConfig()
              .propertyValueString("grouper.mcp.users.canRegisterConfidentialOAuthClient");
          if (StringUtils.isNotBlank(confidentialClientGroupName)) {
            Group confidentialClientGroup = GroupFinder.findByName(rootSession, confidentialClientGroupName, false);
            if (confidentialClientGroup != null && confidentialClientGroup.hasMember(loggedInSubject)) {
              mcpContainer.setAllowedConfidentialClientRegistration(true);
            }
          }
        }

      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      // session duration from config (default 14400 seconds = 4 hours)
      int tokenExpirationSeconds = GrouperConfig.retrieveConfig()
          .propertyValueInt("grouper.oauth.accessToken.expirationSeconds", 14400);
      int hours = tokenExpirationSeconds / 3600;
      if (hours < 1) {
        mcpContainer.setSessionDurationHours("< 1");
      } else {
        mcpContainer.setSessionDurationHours(String.valueOf(hours));
      }

      // check if WS basic auth is enabled
      boolean wsBasicAuth = edu.internet2.middleware.grouper.cfg.GrouperHibernateConfig.retrieveConfig()
          .propertyValueBoolean("grouper.is.ws.basicAuthn", false);
      mcpContainer.setWsBasicAuthnEnabled(wsBasicAuth);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp("#grouperMainContentDivId",
          "/WEB-INF/grouperUi2/mcp/mcpInfo.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * AJAX endpoint to load the most recent 200 MCP tool log entries
   * for the logged-in user, newest first. Renders into #mcpToolLogsResultsId.
   * @param request
   * @param response
   */
  public void mcpToolLogs(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      GrouperRequestContainer grouperRequestContainer =
          GrouperRequestContainer.retrieveFromRequestOrCreate();

      McpContainer mcpContainer = grouperRequestContainer.getMcpContainer();

      Member member = MemberFinder.findBySubject(grouperSession, loggedInSubject, true);
      long memberInternalId = member.getInternalId();

      List<GrouperMcpToolLog> toolLogs = new GcDbAccess()
          .sql("select * from grouper_mcp_tool_log where member_internal_id = ? "
              + "order by started_micros desc")
          .addBindVar(memberInternalId)
          .selectList(GrouperMcpToolLog.class);

      // limit to 200
      if (toolLogs.size() > 200) {
        toolLogs = toolLogs.subList(0, 200);
      }

      mcpContainer.setGuiMcpToolLogs(
          GuiMcpToolLog.convertFromGrouperMcpToolLogs(toolLogs));

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp(
          "#mcpToolLogsResultsId",
          "/WEB-INF/grouperUi2/mcp/mcpToolLogsResults.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * AJAX endpoint to load OAuth client registrations for the logged-in user.
   * Renders into #mcpOAuthRegistrationsResultsId.
   * @param request
   * @param response
   */
  public void mcpOAuthRegistrations(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      GrouperRequestContainer grouperRequestContainer =
          GrouperRequestContainer.retrieveFromRequestOrCreate();

      McpContainer mcpContainer = grouperRequestContainer.getMcpContainer();

      Member member = MemberFinder.findBySubject(grouperSession, loggedInSubject, true);
      long memberInternalId = member.getInternalId();

      List<GrouperOAuthClient> clients = new GcDbAccess()
          .sql("select * from grouper_oauth_client where member_internal_id = ? "
              + "order by registered_micros desc")
          .addBindVar(memberInternalId)
          .selectList(GrouperOAuthClient.class);

      mcpContainer.setGuiOAuthClients(
          GuiOAuthClient.convertFromGrouperOAuthClients(clients));

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp(
          "#mcpOAuthRegistrationsResultsId",
          "/WEB-INF/grouperUi2/mcp/mcpOAuthRegistrationsResults.jsp"));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * AJAX endpoint to delete an OAuth client registration.
   * Verifies the client belongs to the logged-in user before deleting.
   * After deletion, re-renders the OAuth registrations table.
   * @param request
   * @param response
   */
  public void mcpDeleteOAuthRegistration(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      String oauthClientInternalIdString = request.getParameter("oauthClientInternalId");
      if (StringUtils.isBlank(oauthClientInternalIdString)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            "Missing oauthClientInternalId parameter."));
        return;
      }

      long oauthClientInternalId = GrouperUtil.longValue(oauthClientInternalIdString);

      // look up the client
      GrouperOAuthClient client = new GcDbAccess()
          .sql("select * from grouper_oauth_client where internal_id = ?")
          .addBindVar(oauthClientInternalId)
          .select(GrouperOAuthClient.class);

      if (client == null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            "OAuth client registration not found."));
        return;
      }

      // security check: verify this client belongs to the logged-in user
      Member member = MemberFinder.findBySubject(grouperSession, loggedInSubject, true);
      long memberInternalId = member.getInternalId();

      if (client.getMemberInternalId() == null
          || client.getMemberInternalId().longValue() != memberInternalId) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            "You are not authorized to delete this OAuth client registration."));
        return;
      }

      // delete associated authorization codes and pending requests
      new GcDbAccess()
          .sql("delete from grouper_oauth_code where oauth_client_internal_id = ?")
          .addBindVar(client.getInternalId())
          .executeSql();

      new GcDbAccess()
          .sql("delete from grouper_oauth_pend_authz_req where oauth_client_internal_id = ?")
          .addBindVar(client.getInternalId())
          .executeSql();

      // delete the client itself
      new GcDbAccess().deleteFromDatabase(client);

      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("mcpInfoOAuthDeleteSuccess")));

      // re-render the registrations table
      mcpOAuthRegistrations(request, response);

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * AJAX endpoint to register a confidential OAuth client (with client_secret).
   * The user must be in the canRegisterConfidentialOAuthClient group.
   * Shows the client_id, client_secret, authorization URL, and token URL.
   * @param request
   * @param response
   */
  public void mcpRegisterConfidentialClient(HttpServletRequest request, HttpServletResponse response) {

    final Subject loggedInSubject = GrouperUiFilter.retrieveSubjectLoggedIn();

    GrouperSession grouperSession = null;

    try {

      grouperSession = GrouperSession.start(loggedInSubject);

      GuiResponseJs guiResponseJs = GuiResponseJs.retrieveGuiResponseJs();

      GrouperRequestContainer grouperRequestContainer =
          GrouperRequestContainer.retrieveFromRequestOrCreate();

      McpContainer mcpContainer = grouperRequestContainer.getMcpContainer();

      // security check: sysadmins or members of the confidential client registration group
      boolean allowed = PrivilegeHelper.isWheelOrRoot(loggedInSubject);
      if (!allowed) {
        GrouperSession rootSession = GrouperSession.startRootSession();
        try {
          String confidentialClientGroupName = GrouperConfig.retrieveConfig()
              .propertyValueString("grouper.mcp.users.canRegisterConfidentialOAuthClient");
          if (StringUtils.isNotBlank(confidentialClientGroupName)) {
            Group confidentialClientGroup = GroupFinder.findByName(rootSession, confidentialClientGroupName, false);
            if (confidentialClientGroup != null && confidentialClientGroup.hasMember(loggedInSubject)) {
              allowed = true;
            }
          }
        } finally {
          GrouperSession.stopQuietly(rootSession);
        }
      }

      if (!allowed) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("mcpInfoConfidentialClientNotAllowed")));
        return;
      }

      // get the client name from the request
      String clientName = request.getParameter("confidentialClientName");
      if (StringUtils.isBlank(clientName)) {
        clientName = "confidentialClient";
      }

      // get redirect URI from the request
      String redirectUri = request.getParameter("confidentialClientRedirectUri");
      if (StringUtils.isBlank(redirectUri)) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            TextContainer.retrieveFromRequest().getText().get("mcpInfoConfidentialClientRedirectUriRequired")));
        return;
      }

      // validate redirect URI against configured patterns
      java.util.Set<String> redirectUris = new java.util.LinkedHashSet<String>();
      redirectUris.add(redirectUri);
      String redirectUriError = GrouperOAuthStore.validateRedirectUrisAllowed(redirectUris);
      if (redirectUriError != null) {
        guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.error,
            redirectUriError));
        return;
      }

      // create the client with a secret
      String plainTextSecret = GrouperOAuthStore.generateId();

      GrouperOAuthClient client = new GrouperOAuthClient();
      client.setClientId(GrouperOAuthStore.generateId());
      client.setRedirectUris(redirectUris);
      client.setClientName(clientName);
      client.setClientSecret(plainTextSecret);
      client.setRegisteredMicros(System.currentTimeMillis() * 1000L);

      // set the member who registered
      Member member = MemberFinder.findBySubject(grouperSession, loggedInSubject, true);
      client.setMemberInternalId(member.getInternalId());

      GrouperOAuthStore.registerClient(client);

      // audit
      GrouperSession rootSession = GrouperSession.startRootSession();
      try {
        AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.OAUTH_CLIENT_REGISTER,
            "clientId", client.getClientId(),
            "clientName", clientName);
        auditEntry.setDescription("Confidential OAuth client registered via UI: clientId=" + client.getClientId()
            + ", clientName=" + clientName + ", by=" + loggedInSubject.getId());
        auditEntry.saveOrUpdate(true);
      } finally {
        GrouperSession.stopQuietly(rootSession);
      }

      LOG.info("Confidential OAuth client registered via UI: clientId=" + client.getClientId()
          + ", clientName=" + clientName + ", by=" + loggedInSubject.getId());

      // build the URLs
      String wsUrl = GrouperConfig.getGrouperWsUrl(false);
      String uiUrl = GrouperConfig.getGrouperUiUrl(false);

      // uiUrl from getGrouperUiUrl has a trailing slash (e.g. "https://server/grouper/")
      // authorization endpoint matches the well-known pattern: uiUrl + "grouperUi/app/..."
      mcpContainer.setRegisteredClientId(client.getClientId());
      mcpContainer.setRegisteredClientSecret(plainTextSecret);
      mcpContainer.setRegisteredAuthorizationUrl(uiUrl + "grouperUi/app/UiV2OAuth.authorize");
      mcpContainer.setRegisteredTokenUrl(wsUrl + "/mcp/oauth/token");

      guiResponseJs.addAction(GuiScreenAction.newInnerHtmlFromJsp(
          "#mcpConfidentialClientResultId",
          "/WEB-INF/grouperUi2/mcp/mcpConfidentialClientResult.jsp"));

      guiResponseJs.addAction(GuiScreenAction.newMessage(GuiMessageType.success,
          TextContainer.retrieveFromRequest().getText().get("mcpInfoConfidentialClientSuccess")));

    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
