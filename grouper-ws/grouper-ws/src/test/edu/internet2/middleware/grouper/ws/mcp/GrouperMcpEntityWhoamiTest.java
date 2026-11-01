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

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.mcp.GrouperTool;
import edu.internet2.middleware.grouper.mcp.GrouperToolAccess;
import edu.internet2.middleware.grouper.mcp.GrouperToolCategory;
import edu.internet2.middleware.grouper.mcp.GrouperToolEntryPath;
import edu.internet2.middleware.grouper.mcp.GrouperToolExecutor;
import edu.internet2.middleware.grouper.mcp.GrouperToolRegistry;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * tests for the entity_whoami MCP tool (GRP-7528): it describes the caller, and any MCP access at
 * all is enough to run it, including access to only the sql or admin tools.
 *
 * <p>the access group config is pointed at test groups, and the MCP membership caches are cleared
 * after membership changes, so each check sees the current state.</p>
 */
public class GrouperMcpEntityWhoamiTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GrouperMcpEntityWhoamiTest("testSqlLimitedOnlyCanRunWhoami"));
    TestRunner.run(GrouperMcpEntityWhoamiTest.class);
  }

  /**
   *
   */
  public GrouperMcpEntityWhoamiTest() {
  }

  /**
   * @param name
   */
  public GrouperMcpEntityWhoamiTest(String name) {
    super(name);
  }

  /** for reading results */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** folder the readonly and readwrite test groups go in */
  private static final String TEST_STEM = "test:mcpWhoami";

  /** root session for setting up groups */
  private GrouperSession grouperSession;

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    this.grouperSession = GrouperSession.startRootSession();

    overrideConfig(GrouperToolAccess.GROUP_READONLY, testGroup("readonly"));
    overrideConfig(GrouperToolAccess.GROUP_READWRITE, testGroup("readwrite"));
    overrideConfig(GrouperToolAccess.GROUP_SQL_READONLY_LIMITED, mcpGroup("whoamiSqlLimited"));

    GrouperMcpTestUtils.clearCaches();
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperMcpTestUtils.clearCaches();
    GrouperSession.stopQuietly(this.grouperSession);
    GrouperContext.deleteDefaultContext();
    super.tearDown();
  }

  /**
   * the tool is registered, advertised, a read, and allowed by any MCP access
   */
  public void testRegistration() {
    GrouperTool whoami = GrouperToolRegistry.find(GrouperMcpEntityWhoami.WHOAMI_TOOL_NAME);
    assertNotNull(whoami);
    assertTrue(whoami.advertised());
    assertTrue(whoami.allowedWithAnyMcpAccess());
    assertEquals(GrouperToolCategory.readonly, whoami.category(null));
    assertFalse(whoami.category(null).isWrite());
    assertNull(whoami.confirmationSummary(objectMapper.createObjectNode()));
  }

  /**
   * a readonly user with web service authentication: who they are, which groups they are in,
   * that no scopes apply, and which tools they get
   */
  public void testReadonlyUserWsAuthentication() {

    Subject subject = SubjectTestHelper.SUBJ0;
    addMember(testGroup("readonly"), subject);

    JsonNode result = runWhoami(new GrouperMcpAuthUser(subject));

    assertEquals(subject.getId(), result.path("subject").path("subjectId").asText());
    assertEquals(subject.getSourceId(), result.path("subject").path("sourceId").asText());
    assertEquals("wsAuthentication", result.path("login").path("type").asText());
    assertFalse(result.path("grouperSysadmin").asBoolean());

    assertTrue(isMemberOfAccessGroup(result, "readonly"));
    assertFalse(isMemberOfAccessGroup(result, "readwrite"));
    assertFalse(isMemberOfAccessGroup(result, "sqlReadonlyLimited"));

    assertFalse(result.path("scopes").path("enforced").asBoolean());

    assertTrue(result.path("allowedToolCategories").path("readonly").asBoolean());
    assertFalse(result.path("allowedToolCategories").path("readwrite").asBoolean());

    List<String> tools = availableTools(result);
    assertTrue(tools.toString(), tools.contains(GrouperMcpEntityWhoami.WHOAMI_TOOL_NAME));
    assertTrue(tools.toString(), tools.contains("group_find"));
    assertFalse(tools.toString(), tools.contains("group_add_member"));
  }

  /**
   * group membership and the scopes of an OAuth token are reported separately: in the readwrite
   * group but with a readonly token, readwrite is not allowed
   */
  public void testOAuthScopesNarrowerThanGroups() {

    Subject subject = SubjectTestHelper.SUBJ1;
    addMember(testGroup("readwrite"), subject);

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadonly(true);

    JsonNode result = runWhoami(authUser);

    assertEquals("oauth", result.path("login").path("type").asText());
    assertTrue(isMemberOfAccessGroup(result, "readwrite"));

    JsonNode scopes = result.path("scopes");
    assertTrue(scopes.path("enforced").asBoolean());
    assertTrue(scopes.path("readonly").asBoolean());
    assertFalse(scopes.path("readwrite").asBoolean());
    assertTrue("no readwrite limits without the readwrite scope", scopes.path("readwriteLimits").isMissingNode());

    assertTrue(result.path("allowedToolCategories").path("readonly").asBoolean());
    assertFalse(result.path("allowedToolCategories").path("readwrite").asBoolean());
    assertFalse(availableTools(result).contains("group_add_member"));
  }

  /**
   * the read-write folder, group and subject limits of a token are listed
   */
  public void testOAuthReadwriteLimits() {

    Subject subject = SubjectTestHelper.SUBJ2;
    addMember(testGroup("readwrite"), subject);

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(true);
    List<String> folders = new ArrayList<String>();
    folders.add("test:mcpWhoamiScopedFolder");
    authUser.setConsentReadwriteFolders(folders);

    JsonNode result = runWhoami(authUser);

    JsonNode limits = result.path("scopes").path("readwriteLimits");
    assertTrue(limits.path("restricted").asBoolean());
    assertEquals(1, limits.path("folders").size());
    assertEquals("test:mcpWhoamiScopedFolder", limits.path("folders").get(0).asText());
    assertEquals(0, limits.path("groups").size());
    assertEquals(0, limits.path("subjects").size());
  }

  /**
   * somebody whose only MCP access is the limited sql tier cannot run the readonly tools, but can
   * still ask who they are, and is shown the database opened to them and not the other one
   */
  public void testSqlLimitedOnlyCanRunWhoami() {

    overrideConfig("grouper.mcp.sql.whoamiDbA.grouperDatabase", "true");
    overrideConfig("grouper.mcp.sql.whoamiDbA.limitedAccessGroup", mcpGroup("whoamiDbAUsers"));
    overrideConfig("grouper.mcp.sql.whoamiDbB.grouperDatabase", "true");
    overrideConfig("grouper.mcp.sql.whoamiDbB.limitedAccessGroup", mcpGroup("whoamiDbBUsers"));

    Subject subject = SubjectTestHelper.SUBJ3;
    addMember(mcpGroup("whoamiSqlLimited"), subject);
    addMember(mcpGroup("whoamiDbAUsers"), subject);
    saveGroup(mcpGroup("whoamiDbBUsers"));
    GrouperMcpTestUtils.clearCaches();

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);
    GrouperTool whoami = GrouperToolRegistry.find(GrouperMcpEntityWhoami.WHOAMI_TOOL_NAME);

    assertFalse(GrouperToolAccess.isAllowed(GrouperToolCategory.readonly, authUser));
    assertFalse(GrouperToolAccess.isAllowed(GrouperToolRegistry.find("group_find"),
        GrouperToolCategory.readonly, authUser));
    assertTrue(GrouperToolAccess.isAllowed(whoami, GrouperToolCategory.readonly, authUser));

    JsonNode result = runWhoami(authUser);

    assertTrue(isMemberOfAccessGroup(result, "sqlReadonlyLimited"));
    assertFalse(isMemberOfAccessGroup(result, "readonly"));
    assertTrue(result.path("allowedToolCategories").path("sql").asBoolean());
    assertFalse(result.path("allowedToolCategories").path("readonly").asBoolean());

    List<String> limitedIds = new ArrayList<String>();
    for (JsonNode limitedAccessGroup : result.path("limitedAccessGroups")) {
      if ("sqlExternalSystem".equals(limitedAccessGroup.path("kind").asText())) {
        limitedIds.add(limitedAccessGroup.path("id").asText());
      }
    }
    assertTrue(limitedIds.toString(), limitedIds.contains("whoamiDbA"));
    assertFalse("a database not opened to them is not named", limitedIds.contains("whoamiDbB"));

    List<String> tools = availableTools(result);
    assertTrue(tools.toString(), tools.contains(GrouperMcpEntityWhoami.WHOAMI_TOOL_NAME));
    assertTrue(tools.toString(), tools.contains("sql_select"));
    assertFalse(tools.toString(), tools.contains("group_find"));
  }

  /**
   * somebody with no MCP access is refused, and is not offered the tool
   */
  public void testNoMcpAccessIsRefused() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ4);

    assertFalse(GrouperToolAccess.isAllowedInAnyCategory(authUser));

    ObjectNode result = GrouperToolExecutor.executeTool(GrouperMcpEntityWhoami.WHOAMI_TOOL_NAME,
        objectMapper.createObjectNode(), authUser);
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text, result.get("isError").asBoolean());
    assertTrue(text, text.contains("Access denied"));
    assertTrue(text, text.contains(GrouperToolAccess.ANY_ACCESS_DENIED_DETAIL));
  }

  /**
   * an OAuth token with no scope granted is refused even though the subject is in a group, since
   * every category needs both
   */
  public void testOAuthWithNoScopeIsRefused() {

    Subject subject = SubjectTestHelper.SUBJ5;
    addMember(testGroup("readwrite"), subject);

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);
    authUser.setOAuthAuthenticated(true);

    assertFalse(GrouperToolAccess.isAllowed(
        GrouperToolRegistry.find(GrouperMcpEntityWhoami.WHOAMI_TOOL_NAME),
        GrouperToolCategory.readonly, authUser));

    ObjectNode result = GrouperToolExecutor.executeTool(GrouperMcpEntityWhoami.WHOAMI_TOOL_NAME,
        objectMapper.createObjectNode(), authUser);
    assertTrue(result.toString(), result.get("isError").asBoolean());
  }

  /**
   * the AI assistant in the UI reports its session scope rather than a token
   */
  public void testUiAssistantLogin() {

    Subject subject = SubjectTestHelper.SUBJ6;
    addMember(testGroup("readonly"), subject);

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(subject);
    authUser.setEntryPath(GrouperToolEntryPath.ui);
    authUser.setConsentScopeReadonly(true);

    JsonNode result = runWhoami(authUser);

    assertEquals("uiAssistant", result.path("login").path("type").asText());
    assertTrue(result.path("scopes").path("enforced").asBoolean());
    assertTrue(result.path("scopes").path("description").asText().contains("session"));
  }

  /**
   * run the tool through the executor, as MCP and the UI do, and parse its answer
   * @param authUser the caller
   * @return the parsed result text
   */
  private static JsonNode runWhoami(GrouperMcpAuthUser authUser) {
    ObjectNode result = GrouperToolExecutor.executeTool(GrouperMcpEntityWhoami.WHOAMI_TOOL_NAME,
        objectMapper.createObjectNode(), authUser);
    String text = result.get("content").get(0).get("text").asText();
    assertFalse(text, result.get("isError").asBoolean());
    try {
      return objectMapper.readTree(text);
    } catch (Exception e) {
      throw new RuntimeException("Not JSON: " + text, e);
    }
  }

  /**
   * @param result the parsed result
   * @param access e.g. readonly
   * @return whether the result says the caller is in that access group
   */
  private static boolean isMemberOfAccessGroup(JsonNode result, String access) {
    for (JsonNode accessGroup : result.path("mcpAccessGroups")) {
      if (access.equals(accessGroup.path("access").asText())) {
        return accessGroup.path("member").asBoolean();
      }
    }
    fail("No access group " + access + " in " + result);
    return false;
  }

  /**
   * @param result the parsed result
   * @return the tool names it lists
   */
  private static List<String> availableTools(JsonNode result) {
    List<String> tools = new ArrayList<String>();
    for (JsonNode tool : result.path("availableTools")) {
      tools.add(tool.asText());
    }
    return tools;
  }

  /**
   * set a grouper.properties override for this test
   * @param key
   * @param value
   */
  private static void overrideConfig(String key, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(key, value);
  }

  /**
   * @param extension the group extension
   * @return the name of a group in the test folder
   */
  private static String testGroup(String extension) {
    return TEST_STEM + ":" + extension;
  }

  /**
   * @param extension the group extension
   * @return the name of a group in etc:mcp, where every limited group has to be
   */
  private static String mcpGroup(String extension) {
    return GrouperToolAccess.limitedAccessGroupFolderName() + ":" + extension;
  }

  /**
   * add a subject to a group, creating it if needed, then clear the caches so the next check
   * sees it
   * @param groupName the group
   * @param subject who to add
   */
  private void addMember(String groupName, Subject subject) {
    Group group = saveGroup(groupName);
    group.addMember(subject, false);
    GrouperMcpTestUtils.clearCaches();
  }

  /**
   * create a group if it does not exist
   * @param groupName the group
   * @return the group
   */
  private Group saveGroup(String groupName) {
    return new GroupSave(this.grouperSession).assignName(groupName)
        .assignCreateParentStemsIfNotExist(true).save();
  }

}
