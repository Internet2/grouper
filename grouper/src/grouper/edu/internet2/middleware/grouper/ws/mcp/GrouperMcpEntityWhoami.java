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

import java.text.SimpleDateFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.mcp.GrouperMcpGroupMembership;
import edu.internet2.middleware.grouper.mcp.GrouperMcpToolNames;
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.mcp.GrouperToolEntryPath;
import edu.internet2.middleware.grouper.mcp.GrouperToolExecutor;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * MCP tool handler for entity_whoami (GRP-7528): tells the caller who they are logged in to
 * Grouper as and what this login can do, so they can check their connection and troubleshoot
 * scopes.  it describes the caller and nothing else, so any MCP access at all is enough to run
 * it, see {@link edu.internet2.middleware.grouper.mcp.GrouperTool#allowedWithAnyMcpAccess()}.
 *
 * <p>group memberships and the scopes of this login are reported separately, since "in the
 * readwrite group, but this token only has read-only" is the confusion this is for.  limited
 * access groups and external systems are only listed when the caller is in them, so this does
 * not tell anybody what else is configured.</p>
 */
public class GrouperMcpEntityWhoami {

  /** logger */
  private static final Log LOG = GrouperUtil.getLog(GrouperMcpEntityWhoami.class);

  /** for building JSON */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** the tool name */
  public static final String WHOAMI_TOOL_NAME = "entity_whoami";

  /** config name of the group whose members may use MCP with web service authentication */
  static final String GROUP_WS_AUTHN_ALLOWED = "grouper.mcp.users.wsAuthnAllowed";

  /**
   * the MCP access groups reported: config name, the name shown for it, and whether it is a
   * limited tier group, which only counts in etc:mcp.  in the order they are listed in
   * grouper.base.properties
   */
  private static final String[][] ACCESS_GROUPS = new String[][] {
    {GrouperToolAccess.GROUP_READONLY, "readonly", "false"},
    {GrouperToolAccess.GROUP_READWRITE, "readwrite", "false"},
    {GROUP_WS_AUTHN_ALLOWED, "wsAuthnAllowed", "false"},
    {GrouperToolAccess.GROUP_SQL_READONLY, "sqlReadonly", "false"},
    {GrouperToolAccess.GROUP_SQL_READONLY_LIMITED, "sqlReadonlyLimited", "true"},
    {GrouperToolAccess.GROUP_ADMIN_READONLY, "adminReadonly", "false"},
    {GrouperToolAccess.GROUP_ADMIN_READONLY_LIMITED, "adminReadonlyLimited", "true"},
    {GrouperToolAccess.GROUP_ADMIN_READWRITE, "adminReadwrite", "false"},
    {GrouperToolAccess.GROUP_ADMIN_READWRITE_LIMITED, "adminReadwriteLimited", "true"},
  };

  /**
   * return the MCP tool definition for entity_whoami
   * @return the tool definition as a Jackson ObjectNode
   */
  public static ObjectNode toolDefinition() {
    ObjectNode tool = objectMapper.createObjectNode();
    tool.put("name", WHOAMI_TOOL_NAME);
    tool.put("description",
        "Describe who is logged in to Grouper on this connection and what this login can do. "
        + "Takes no arguments. Returns the logged-in subject (source id, subject id, identifier, "
        + "name), whether the subject is a Grouper sysadmin, which MCP access groups the subject "
        + "is in, the limited access groups and external systems opened to the subject, the "
        + "scopes granted to this login (for an OAuth token, the scopes consented to, including "
        + "any read-write folder, group and subject limits), what that adds up to for each kind "
        + "of tool, and which tools are available. Use it when the user asks who they are "
        + "connected as or what they are allowed to do, or to troubleshoot an access denied "
        + "error. Group memberships are reported separately from the scopes of this login: a "
        + "tool needs both.");

    ObjectNode inputSchema = objectMapper.createObjectNode();
    inputSchema.put("type", "object");
    inputSchema.set("properties", objectMapper.createObjectNode());
    tool.set("inputSchema", inputSchema);

    return tool;
  }

  /**
   * execute the entity_whoami tool
   * @param arguments the tool arguments from the MCP request, ignored
   * @param authUser the authenticated user
   * @return the MCP tool result
   */
  public static ObjectNode execute(JsonNode arguments, GrouperMcpAuthUser authUser) {

    if (authUser == null || authUser.getSubject() == null) {
      return buildErrorResult("No logged-in subject.");
    }

    try {

      Subject subject = authUser.getSubject();

      ObjectNode resultNode = objectMapper.createObjectNode();
      resultNode.set("subject", subjectNode(subject));
      resultNode.set("login", loginNode(authUser));

      resultNode.put("grouperSysadmin", PrivilegeHelper.isWheelOrRoot(subject));
      resultNode.put("grouperReadonlySysadmin", PrivilegeHelper.isWheelOrRootOrReadonlyRoot(subject));

      resultNode.set("mcpAccessGroups", accessGroupsNode(subject));
      resultNode.set("limitedAccessGroups", limitedAccessGroupsNode(subject));
      resultNode.set("scopes", scopesNode(authUser));
      resultNode.set("allowedToolCategories", allowedToolCategoriesNode(authUser));
      resultNode.set("availableTools", availableToolsNode(authUser));

      resultNode.put("note", "Group memberships are cached for up to 60 seconds on each Grouper "
          + "server, so a change to an MCP access group can take a minute to show here. Scopes "
          + "granted to an OAuth token do not change until the user signs in again.");

      String resultText = objectMapper.writerWithDefaultPrettyPrinter()
          .writeValueAsString(resultNode);
      return buildSuccessResult(resultText);

    } catch (Exception e) {
      LOG.error("Error describing the MCP caller", e);
      return buildErrorResult("Error describing the logged-in subject: " + e.getMessage()
          + GrouperMcpErrorUtils.stackTraceIfAllowed(authUser, e));
    }
  }

  /**
   * @param subject the caller
   * @return source id, subject id, identifier and name.  the identifier and member id come from
   * the member record, looked up as root since this is about the caller themselves
   */
  private static ObjectNode subjectNode(final Subject subject) {

    ObjectNode subjectNode = objectMapper.createObjectNode();
    subjectNode.put("sourceId", subject.getSourceId());
    subjectNode.put("subjectId", subject.getId());

    Member member = (Member)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      public Object callback(GrouperSession rootGrouperSession) throws GrouperSessionException {
        return MemberFinder.findBySubject(rootGrouperSession, subject, false);
      }
    });

    putIfNotBlank(subjectNode, "identifier", member == null ? null : member.getSubjectIdentifier0());
    putIfNotBlank(subjectNode, "name", subject.getName());
    putIfNotBlank(subjectNode, "memberId", member == null ? null : member.getId());

    return subjectNode;
  }

  /**
   * @param authUser the caller
   * @return how this login came in: an MCP client over OAuth or web service authentication, or
   * the AI assistant in the Grouper UI
   */
  private static ObjectNode loginNode(GrouperMcpAuthUser authUser) {

    ObjectNode loginNode = objectMapper.createObjectNode();

    if (authUser.getEntryPath() == GrouperToolEntryPath.ui) {
      loginNode.put("type", "uiAssistant");
      loginNode.put("description", "The AI assistant in the Grouper UI, acting for the user "
          + "logged in to the UI.");
    } else if (authUser.isOAuthAuthenticated()) {
      loginNode.put("type", "oauth");
      loginNode.put("description", "An MCP client authenticated with an OAuth token.");
      if (authUser.getJwtIssuedAt() != null) {
        loginNode.put("tokenIssuedAt",
            new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z").format(authUser.getJwtIssuedAt()));
      }
    } else {
      loginNode.put("type", "wsAuthentication");
      loginNode.put("description", "An MCP client authenticated with web service credentials "
          + "(e.g. HTTP Basic or container authentication).");
    }

    return loginNode;
  }

  /**
   * @param subject the caller
   * @return each MCP access group: what it is for, the configured group, and whether the caller
   * is in it.  the limited groups only count in etc:mcp, as when access is checked
   */
  private static ArrayNode accessGroupsNode(Subject subject) {

    ArrayNode groupsArray = objectMapper.createArrayNode();

    for (String[] accessGroup : ACCESS_GROUPS) {

      String propertyName = accessGroup[0];
      String groupName = GrouperConfig.retrieveConfig().propertyValueString(propertyName);

      ObjectNode groupNode = groupsArray.addObject();
      groupNode.put("access", accessGroup[1]);
      groupNode.put("configProperty", propertyName);

      if (StringUtils.isBlank(groupName)) {
        groupNode.putNull("groupName");
        groupNode.put("member", false);
        continue;
      }

      groupNode.put("groupName", groupName);
      groupNode.put("member", Boolean.parseBoolean(accessGroup[2])
          ? GrouperMcpGroupMembership.isSubjectInGroup(subject, propertyName,
              GrouperToolAccess.limitedAccessGroupFolderName())
          : GrouperMcpGroupMembership.isSubjectInGroup(subject, propertyName));
    }

    return groupsArray;
  }

  /**
   * the limitedAccessGroups the caller is in, per tool and per SQL, LDAP and admin external
   * system.  only those they are in, so this does not tell them what else is configured
   * @param subject the caller
   * @return the groups
   */
  private static ArrayNode limitedAccessGroupsNode(Subject subject) {

    ArrayNode groupsArray = objectMapper.createArrayNode();

    for (String toolName : GrouperMcpToolNames.toolNames()) {
      addLimitedAccessGroupIfMember(groupsArray, subject, "tool", toolName,
          GrouperToolAccess.TOOL_CONFIG_PREFIX);
    }
    for (String externalSystemId : GrouperMcpSqlSelect.externalSystemIds()) {
      addLimitedAccessGroupIfMember(groupsArray, subject, "sqlExternalSystem", externalSystemId,
          GrouperToolAccess.SQL_CONFIG_PREFIX);
    }
    for (String externalSystemId : GrouperMcpLdapSearch.externalSystemIds()) {
      addLimitedAccessGroupIfMember(groupsArray, subject, "ldapExternalSystem", externalSystemId,
          GrouperToolAccess.LDAP_CONFIG_PREFIX);
    }
    for (String externalSystemId : GrouperMcpAdminExternalSystemGet.findConfiguredExternalSystemIds()) {
      addLimitedAccessGroupIfMember(groupsArray, subject, "adminExternalSystem", externalSystemId,
          GrouperToolAccess.ADMIN_EXTERNAL_SYSTEM_CONFIG_PREFIX);
    }

    return groupsArray;
  }

  /**
   * add the limitedAccessGroup of a tool or external system if one is configured and the caller
   * is in it
   * @param groupsArray where to add it
   * @param subject the caller
   * @param kind what the id is, e.g. tool or sqlExternalSystem
   * @param id the tool name or external system id
   * @param configPrefix the config prefix before the id
   */
  private static void addLimitedAccessGroupIfMember(ArrayNode groupsArray, Subject subject,
      String kind, String id, String configPrefix) {

    String propertyName = configPrefix + id + GrouperToolAccess.LIMITED_ACCESS_GROUP_SUFFIX;
    String groupName = GrouperConfig.retrieveConfig().propertyValueString(propertyName);
    if (StringUtils.isBlank(groupName)) {
      return;
    }

    if (!GrouperMcpGroupMembership.isSubjectInGroup(subject, propertyName,
        GrouperToolAccess.limitedAccessGroupFolderName())) {
      return;
    }

    ObjectNode groupNode = groupsArray.addObject();
    groupNode.put("kind", kind);
    groupNode.put("id", id);
    groupNode.put("groupName", groupName);
  }

  /**
   * @param authUser the caller
   * @return the scopes of this login.  for web service authentication there are none, and only
   * group membership applies
   */
  private static ObjectNode scopesNode(GrouperMcpAuthUser authUser) {

    ObjectNode scopesNode = objectMapper.createObjectNode();

    if (!authUser.isConsentScopeEnforced()) {
      scopesNode.put("enforced", false);
      scopesNode.put("description", "This login has no scopes: it authenticated with web "
          + "service credentials, so only MCP access group membership applies.");
      return scopesNode;
    }

    scopesNode.put("enforced", true);
    scopesNode.put("description", authUser.getEntryPath() == GrouperToolEntryPath.ui
        ? "The scope chosen for this session on the AI assistant screen. A tool needs both the "
            + "MCP access group and the scope."
        : "The scopes consented to for this OAuth token. A tool needs both the MCP access group "
            + "and the scope.");

    scopesNode.put("readonly", authUser.isConsentScopeReadonly());
    scopesNode.put("readwrite", authUser.isConsentScopeReadwrite());
    scopesNode.put("sqlReadonly", authUser.isConsentScopeSqlReadonly());
    scopesNode.put("adminReadonly", authUser.isConsentScopeAdminReadonly());
    scopesNode.put("adminReadwrite", authUser.isConsentScopeAdminReadwrite());

    if (authUser.isConsentScopeReadwrite()) {
      ObjectNode readwriteLimitsNode = scopesNode.putObject("readwriteLimits");
      readwriteLimitsNode.put("restricted", authUser.isConsentReadwriteScopeRestricted());
      readwriteLimitsNode.set("folders", stringArray(authUser.getConsentReadwriteFolders()));
      readwriteLimitsNode.set("groups", stringArray(authUser.getConsentReadwriteGroups()));
      readwriteLimitsNode.set("subjects", stringArray(authUser.getConsentReadwriteSubjects()));
      readwriteLimitsNode.put("description", authUser.isConsentReadwriteScopeRestricted()
          ? "Read-write tools only act on these folders, groups and subjects. A list left empty "
              + "is unrestricted if another list has values, and if all are empty nothing can be "
              + "changed."
          : "Read-write is not limited to particular folders, groups or subjects.");
    }

    return scopesNode;
  }

  /**
   * @param authUser the caller
   * @return for each tool category, whether this login may use it: group membership and scope
   * together
   */
  private static ObjectNode allowedToolCategoriesNode(GrouperMcpAuthUser authUser) {
    ObjectNode categoriesNode = objectMapper.createObjectNode();
    for (GrouperToolCategory category : GrouperToolCategory.values()) {
      categoriesNode.put(category.name(), GrouperToolAccess.isAllowed(category, authUser));
    }
    return categoriesNode;
  }

  /**
   * @param authUser the caller
   * @return the names of the tools this login is offered, from the same list tools/list sends
   */
  private static ArrayNode availableToolsNode(GrouperMcpAuthUser authUser) {
    ArrayNode toolsArray = objectMapper.createArrayNode();
    for (JsonNode toolDefinition : GrouperToolExecutor.retrieveToolDefinitions(authUser)) {
      toolsArray.add(toolDefinition.path("name").asText());
    }
    return toolsArray;
  }

  /**
   * @param values the values, may be null
   * @return a JSON array of them, empty if null
   */
  private static ArrayNode stringArray(List<String> values) {
    ArrayNode array = objectMapper.createArrayNode();
    for (String value : GrouperUtil.nonNull(values)) {
      array.add(value);
    }
    return array;
  }

  /**
   * @param node where to put it
   * @param name the field name
   * @param value the value, left out if blank
   */
  private static void putIfNotBlank(ObjectNode node, String name, String value) {
    if (StringUtils.isNotBlank(value)) {
      node.put(name, value);
    }
  }

  /**
   * build a successful MCP tool result
   * @param text the result text
   * @return the result
   */
  private static ObjectNode buildSuccessResult(String text) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", text);
    content.add(textContent);
    result.set("content", content);
    result.put("isError", false);
    return result;
  }

  /**
   * build an error MCP tool result
   * @param errorMessage the error
   * @return the result
   */
  private static ObjectNode buildErrorResult(String errorMessage) {
    ObjectNode result = objectMapper.createObjectNode();
    ArrayNode content = objectMapper.createArrayNode();
    ObjectNode textContent = objectMapper.createObjectNode();
    textContent.put("type", "text");
    textContent.put("text", errorMessage);
    content.add(textContent);
    result.set("content", content);
    result.put("isError", true);
    return result;
  }
}
