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
package edu.internet2.middleware.grouper.ws.mcp;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpFolderSave (folder_save MCP tool, GRP-6806)
 *
 * @author mchyzer
 */
public class GrouperMcpFolderSaveTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpFolderSaveTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpFolderSaveTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpFolderSaveTest("testCreateFolderSuccess"));
  }

  /** json mapper */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** a folder protected from MCP writes for these tests */
  private static final String PROTECTED_FOLDER = "test:mcpFolderSaveProtected";

  /** the parent folder the tests create folders in, SUBJ0 can create in it */
  private static final String PARENT_FOLDER = "test:mcpFolderSave";

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpProtectedResources.PROTECTED_FOLDERS_CONFIG, PROTECTED_FOLDER);
    GrouperMcpProtectedResources.clearCache();
    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    // SUBJ0 can create folders in the parent folder, and nothing else
    Stem parentStem = new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(PARENT_FOLDER)
        .assignCreateParentStemsIfNotExist(true).save();
    parentStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove(
        GrouperMcpProtectedResources.PROTECTED_FOLDERS_CONFIG);
    GrouperMcpProtectedResources.clearCache();
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * run the tool as SUBJ0
   * @param arguments tool arguments
   * @param authUser the auth user, or null for a plain SUBJ0 auth user
   * @return the result
   */
  private static ObjectNode executeAsSubj0(ObjectNode arguments, GrouperMcpAuthUser authUser) {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      return GrouperMcpFolderSave.execute(arguments,
          authUser == null ? new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0) : authUser);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * an OAuth auth user for SUBJ0 restricted to these readwrite folders
   * @param folders consented readwrite folders
   * @return the auth user
   */
  private static GrouperMcpAuthUser oauthAuthUser(List<String> folders) {
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
    authUser.setOAuthAuthenticated(true);
    authUser.setConsentScopeReadwrite(true);
    authUser.setConsentReadwriteScopeRestricted(true);
    authUser.setConsentReadwriteFolders(folders);
    return authUser;
  }

  /**
   * @param result tool result
   * @return the parsed json of a successful result
   * @throws Exception if not json
   */
  private static JsonNode successJson(ObjectNode result) throws Exception {
    assertFalse("Expected success, got: " + result, result.get("isError").asBoolean());
    return objectMapper.readTree(result.get("content").get(0).get("text").asText());
  }

  /**
   * @param result tool result
   * @return the error text
   */
  private static String errorText(ObjectNode result) {
    assertTrue("Expected error, got: " + result, result.get("isError").asBoolean());
    return result.get("content").get(0).get("text").asText();
  }

  /**
   * @param name folder name
   * @return the folder as root, or null
   */
  private static Stem findStem(String name) {
    return StemFinder.findByName(GrouperSession.staticGrouperSession(), name, false);
  }

  /**
   * the tool definition is well formed
   */
  public void testToolDefinition() {
    ObjectNode toolDef = GrouperMcpFolderSave.toolDefinition();
    assertEquals("folder_save", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("action"));
    assertNotNull(properties.get("stemName"));
    assertNotNull(properties.get("displayExtension"));
    assertNotNull(properties.get("description"));
    assertNotNull(properties.get("createParentFoldersIfNotExist"));
    assertEquals(3, properties.get("action").get("enum").size());

    JsonNode required = toolDef.get("inputSchema").get("required");
    assertEquals(2, required.size());
    assertEquals("action", required.get(0).asText());
    assertEquals("stemName", required.get(1).asText());
  }

  /**
   * createFolder makes a folder with the display name and description passed in
   * @throws Exception
   */
  public void testCreateFolderSuccess() throws Exception {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "createFolder");
    arguments.put("stemName", PARENT_FOLDER + ":newFolder");
    arguments.put("displayExtension", "New folder");
    arguments.put("description", "made by mcp");

    JsonNode response = successJson(executeAsSubj0(arguments, null));
    assertEquals("INSERT", response.get("resultCode").asText());
    assertEquals(PARENT_FOLDER + ":newFolder", response.get("name").asText());
    assertEquals("New folder", response.get("displayExtension").asText());

    Stem stem = findStem(PARENT_FOLDER + ":newFolder");
    assertNotNull(stem);
    assertEquals("New folder", stem.getDisplayExtension());
    assertEquals("made by mcp", stem.getDescription());
    assertEquals(stem.getUuid(), response.get("uuid").asText());
  }

  /**
   * createFolder fails when the folder already exists, and does not change it
   */
  public void testCreateFolderAlreadyExists() {
    new StemSave(GrouperSession.staticGrouperSession()).assignName(PARENT_FOLDER + ":existing")
        .assignDescription("original").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "createFolder");
    arguments.put("stemName", PARENT_FOLDER + ":existing");
    arguments.put("description", "changed");

    errorText(executeAsSubj0(arguments, null));
    assertEquals("original", findStem(PARENT_FOLDER + ":existing").getDescription());
  }

  /**
   * creating needs CREATE on the parent folder
   */
  public void testCreateFolderNoPrivilege() {
    new StemSave(GrouperSession.staticGrouperSession()).assignName("test:mcpFolderSaveNoPriv")
        .assignCreateParentStemsIfNotExist(true).save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "createFolder");
    arguments.put("stemName", "test:mcpFolderSaveNoPriv:child");

    errorText(executeAsSubj0(arguments, null));
    assertNull(findStem("test:mcpFolderSaveNoPriv:child"));
  }

  /**
   * a missing parent folder is an error unless createParentFoldersIfNotExist is true
   * @throws Exception
   */
  public void testCreateParentFolders() throws Exception {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "createFolder");
    arguments.put("stemName", PARENT_FOLDER + ":a:b");

    errorText(executeAsSubj0(arguments, null));
    assertNull(findStem(PARENT_FOLDER + ":a"));

    assertEquals(Arrays.asList(PARENT_FOLDER + ":a"),
        GrouperMcpFolderSave.missingParentFolderNames(PARENT_FOLDER + ":a:b"));

    arguments.put("createParentFoldersIfNotExist", true);
    successJson(executeAsSubj0(arguments, null));
    assertNotNull(findStem(PARENT_FOLDER + ":a"));
    assertNotNull(findStem(PARENT_FOLDER + ":a:b"));
  }

  /**
   * createOrUpdateFolder creates a folder, then on an existing folder only changes what is passed in
   * @throws Exception
   */
  public void testCreateOrUpdateFolder() throws Exception {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "createOrUpdateFolder");
    arguments.put("stemName", PARENT_FOLDER + ":createOrUpdate");
    arguments.put("displayExtension", "Create or update");
    arguments.put("description", "first");

    assertEquals("INSERT", successJson(executeAsSubj0(arguments, null)).get("resultCode").asText());

    // SUBJ0 created it, so has STEM_ADMIN on it.  only the description is passed in this time
    arguments = objectMapper.createObjectNode();
    arguments.put("action", "createOrUpdateFolder");
    arguments.put("stemName", PARENT_FOLDER + ":createOrUpdate");
    arguments.put("description", "second");

    assertEquals("UPDATE", successJson(executeAsSubj0(arguments, null)).get("resultCode").asText());

    Stem stem = findStem(PARENT_FOLDER + ":createOrUpdate");
    assertEquals("second", stem.getDescription());
    assertEquals("display name not passed in, so not changed", "Create or update", stem.getDisplayExtension());
  }

  /**
   * updateFolderPart changes only what is passed in, and needs STEM_ADMIN
   * @throws Exception
   */
  public void testUpdateFolderPart() throws Exception {
    Stem stem = new StemSave(GrouperSession.staticGrouperSession()).assignName(PARENT_FOLDER + ":update")
        .assignDisplayExtension("Before").assignDescription("original description").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "updateFolderPart");
    arguments.put("stemName", PARENT_FOLDER + ":update");
    arguments.put("displayExtension", "After");

    // CREATE on the parent is not enough to change a folder
    errorText(executeAsSubj0(arguments, null));
    assertEquals("Before", findStem(PARENT_FOLDER + ":update").getDisplayExtension());

    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

    assertEquals("UPDATE", successJson(executeAsSubj0(arguments, null)).get("resultCode").asText());
    stem = findStem(PARENT_FOLDER + ":update");
    assertEquals("After", stem.getDisplayExtension());
    assertEquals("original description", stem.getDescription());
  }

  /**
   * updateFolderPart on a folder that is not there, or with nothing to change
   */
  public void testUpdateFolderPartErrors() {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "updateFolderPart");
    arguments.put("stemName", PARENT_FOLDER + ":notThere");
    arguments.put("description", "x");
    assertTrue(errorText(executeAsSubj0(arguments, null)).contains("Stem not found"));

    arguments = objectMapper.createObjectNode();
    arguments.put("action", "updateFolderPart");
    arguments.put("stemName", PARENT_FOLDER);
    assertTrue(errorText(executeAsSubj0(arguments, null)).contains("displayExtension and/or description"));
  }

  /**
   * missing and bad arguments
   */
  public void testArgumentErrors() {
    assertTrue(errorText(executeAsSubj0(null, null)).contains("action is required"));

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "createFolder");
    assertTrue(errorText(executeAsSubj0(arguments, null)).contains("stemName is required"));

    arguments.put("stemName", ":");
    assertTrue(errorText(executeAsSubj0(arguments, null)).contains("root folder"));

    arguments = objectMapper.createObjectNode();
    arguments.put("action", "renameFolder");
    arguments.put("stemName", PARENT_FOLDER + ":x");
    assertTrue(errorText(executeAsSubj0(arguments, null)).contains("Unknown action"));
  }

  /**
   * folders in etc and in a protected folder cannot be created, including as a parent folder
   */
  public void testProtectedFolders() {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "createFolder");
    arguments.put("stemName", "etc:mcpFolderSave");
    errorText(executeAsSubj0(arguments, null));
    assertNull(findStem("etc:mcpFolderSave"));

    arguments.put("stemName", PROTECTED_FOLDER + ":child");
    arguments.put("createParentFoldersIfNotExist", true);
    errorText(executeAsSubj0(arguments, null));
    assertNull(findStem(PROTECTED_FOLDER));
  }

  /**
   * OAuth readwrite scope: the folder must be in scope, and so must any parent folder that
   * would be created along the way
   * @throws Exception
   */
  public void testOAuthScope() throws Exception {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("action", "createFolder");
    arguments.put("stemName", PARENT_FOLDER + ":scoped");

    // scope is a different folder
    errorText(executeAsSubj0(arguments, oauthAuthUser(Arrays.asList("test:somethingElse"))));
    assertNull(findStem(PARENT_FOLDER + ":scoped"));

    // scope is the folder being created
    successJson(executeAsSubj0(arguments, oauthAuthUser(Arrays.asList(PARENT_FOLDER + ":scoped"))));

    // scope is only the deepest folder, so its missing parent cannot be created on the side
    arguments = objectMapper.createObjectNode();
    arguments.put("action", "createFolder");
    arguments.put("stemName", PARENT_FOLDER + ":p:q");
    arguments.put("createParentFoldersIfNotExist", true);
    String error = errorText(executeAsSubj0(arguments, oauthAuthUser(Arrays.asList(PARENT_FOLDER + ":p:q"))));
    assertTrue(error, error.contains("Cannot create parent folder '" + PARENT_FOLDER + ":p'"));
    assertNull(findStem(PARENT_FOLDER + ":p"));

    // scope covers the parent too
    successJson(executeAsSubj0(arguments, oauthAuthUser(Arrays.asList(PARENT_FOLDER + ":p"))));
    assertNotNull(findStem(PARENT_FOLDER + ":p:q"));
  }

}
