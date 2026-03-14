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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpInstitutionalTools (institutional_tools MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpInstitutionalToolsTest extends GrouperTest {

  public GrouperMcpInstitutionalToolsTest() {
  }

  public GrouperMcpInstitutionalToolsTest(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpInstitutionalToolsTest("testSchemaWithMcpEnabledTemplate"));
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);
  }

  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * test tool definition returns null when no MCP-enabled templates exist
   */
  public void testToolDefinitionNoTemplates() {

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectFinder.findRootSubject());
    ObjectNode toolDef = GrouperMcpInstitutionalTools.toolDefinition(authUser, true);

    assertNull("Should return null when no MCP-enabled templates are available", toolDef);
  }

  /**
   * test tool definition with an MCP-enabled template includes tool name in description
   */
  public void testToolDefinition() {

    // configure a GSH template with mcpEnabled=true
    String configPrefix = "grouperGshTemplate.mcpToolDefTest.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "My Custom Tool");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "A test template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "everyone");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpEnabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "folderShowType", "allFolders");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"hello\");");

    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectFinder.findRootSubject());
    ObjectNode toolDef = GrouperMcpInstitutionalTools.toolDefinition(authUser, true);

    assertNotNull("Should return tool definition when MCP-enabled templates exist", toolDef);
    assertEquals("institutional_tools", toolDef.get("name").asText());

    String description = toolDef.get("description").asText();
    assertNotNull(description);
    assertTrue("Description should contain the tool name", description.contains("My Custom Tool"));

    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("action"));
    assertNotNull(properties.get("configId"));
    assertNotNull(properties.get("ownerType"));
    assertNotNull(properties.get("ownerGroupName"));
    assertNotNull(properties.get("ownerStemName"));
    assertNotNull(properties.get("inputs"));

    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(1, required.size());
    assertEquals("action", required.get(0).asText());
  }

  /**
   * test schema action returns MCP-enabled templates
   */
  public void testSchemaWithMcpEnabledTemplate() {

    // create a group for specifiedGroup security
    Group canRunGroup = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpCanRunGroup")
        .assignName("test:mcpCanRunGroup")
        .assignCreateParentStemsIfNotExist(true)
        .save();

    // add SUBJ0 to the can-run group
    canRunGroup.addMember(SubjectTestHelper.SUBJ0);

    // configure a GSH template with mcpEnabled=true
    String configPrefix = "grouperGshTemplate.mcpTestTemplate.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "MCP Test Template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "A test template for MCP");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "specifiedGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "groupUuidCanRun", canRunGroup.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpEnabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "folderShowType", "allFolders");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.name", "gsh_input_testParam");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.label", "Test Parameter");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.description", "A test input parameter");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.type", "string");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.required", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.validationType", "regex");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.validationRegex", "^[a-zA-Z]+$");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.validationMessage", "Only letters allowed");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"hello \" + gsh_input_testParam);");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "schema");

      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertTrue("Expected at least 1 tool", responseNode.get("toolCount").asInt() >= 1);

        JsonNode tools = responseNode.get("tools");
        assertNotNull(tools);
        assertTrue(tools.isArray());

        // find our template
        JsonNode mcpTestTool = null;
        for (int i = 0; i < tools.size(); i++) {
          if ("mcpTestTemplate".equals(tools.get(i).get("configId").asText())) {
            mcpTestTool = tools.get(i);
            break;
          }
        }
        assertNotNull("Should find mcpTestTemplate in schema", mcpTestTool);
        assertEquals("MCP Test Template", mcpTestTool.get("name").asText());
        assertEquals("A test template for MCP", mcpTestTool.get("description").asText());
        assertFalse(mcpTestTool.get("executeOnGroupName").asBoolean());
        assertTrue(mcpTestTool.get("executeOnFolderName").asBoolean());

        // check inputs
        JsonNode inputs = mcpTestTool.get("inputs");
        assertNotNull(inputs);
        assertEquals(1, inputs.size());
        JsonNode input0 = inputs.get(0);
        assertEquals("testParam", input0.get("name").asText());
        assertEquals("string", input0.get("type").asText());
        assertTrue(input0.get("required").asBoolean());
        assertEquals("Test Parameter", input0.get("label").asText());
        assertEquals("A test input parameter", input0.get("description").asText());

        // check validation
        JsonNode validation = input0.get("validation");
        assertNotNull(validation);
        assertEquals("regex", validation.get("type").asText());
        assertEquals("^[a-zA-Z]+$", validation.get("regex").asText());
        assertEquals("Only letters allowed", validation.get("message").asText());

      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test schema does NOT return templates where mcpEnabled is false
   */
  public void testSchemaExcludesNonMcpTemplates() {

    // configure a GSH template with mcpEnabled=false (default)
    String configPrefix = "grouperGshTemplate.mcpDisabledTemplate.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "Non-MCP Template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "Should not appear");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "wheel");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"hello\");");
    // mcpEnabled defaults to false, so not setting it

    GrouperSession session = GrouperSession.start(SubjectFinder.findRootSubject());
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectFinder.findRootSubject());

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "schema");

      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertFalse("Expected success", result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode tools = responseNode.get("tools");

        // make sure mcpDisabledTemplate is NOT in the list
        for (int i = 0; i < tools.size(); i++) {
          assertFalse("mcpDisabledTemplate should not appear",
              "mcpDisabledTemplate".equals(tools.get(i).get("configId").asText()));
        }

      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test execute action runs a template
   */
  public void testExecuteTemplate() {

    Group canRunGroup = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpExecCanRunGroup")
        .assignName("test:mcpExecCanRunGroup")
        .assignCreateParentStemsIfNotExist(true)
        .save();

    canRunGroup.addMember(SubjectTestHelper.SUBJ0);

    Stem testStem = new StemSave(GrouperSession.staticGrouperSession())
        .assignStemNameToEdit("test:mcpExecStem")
        .assignName("test:mcpExecStem")
        .assignCreateParentStemsIfNotExist(true)
        .save();

    String configPrefix = "grouperGshTemplate.mcpExecTemplate.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "MCP Execute Test");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "Test execution");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "specifiedGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "groupUuidCanRun", canRunGroup.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpEnabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "folderShowType", "allFolders");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "allowWsFromNoOwner", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.name", "gsh_input_greeting");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.label", "Greeting");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.description", "What to say");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.type", "string");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.required", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.validationType", "none");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"Result: \" + gsh_input_greeting);");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "execute");
      arguments.put("configId", "mcpExecTemplate");
      arguments.put("ownerType", "stem");
      arguments.put("ownerStemName", testStem.getName());

      ObjectNode inputs = objectMapper.createObjectNode();
      inputs.put("gsh_input_greeting", "world");
      arguments.set("inputs", inputs);

      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        assertEquals("mcpExecTemplate", responseNode.get("configId").asText());
        assertTrue(responseNode.get("success").asBoolean());

        JsonNode outputLines = responseNode.get("outputLines");
        assertNotNull(outputLines);
        assertTrue(outputLines.size() > 0);
        assertEquals("Result: world", outputLines.get(0).get("text").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test execute with non-MCP template returns error
   */
  public void testExecuteNonMcpTemplateReturnsError() {

    String configPrefix = "grouperGshTemplate.mcpNotEnabledExec.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "Not MCP Enabled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "Should fail");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "wheel");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"hello\");");
    // mcpEnabled not set, defaults to false

    GrouperSession session = GrouperSession.start(SubjectFinder.findRootSubject());
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectFinder.findRootSubject());

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "execute");
      arguments.put("configId", "mcpNotEnabledExec");

      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertTrue("Expected error for non-MCP template", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("not MCP-enabled"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test missing action
   */
  public void testMissingAction() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("configId", "someTemplate");

      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("action is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test execute with missing configId
   */
  public void testExecuteMissingConfigId() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "execute");

      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertTrue("Expected error", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("configId is required"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a readonly user can see mcpReadonly templates in schema
   */
  public void testSchemaReadonlyUserSeesReadonlyTemplates() {

    String configPrefix = "grouperGshTemplate.mcpReadonlyTemplate.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "Readonly MCP Template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "A readonly template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "everyone");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpEnabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpReadonly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"readonly result\");");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "schema");

      // readonly user (hasReadwriteAccess = false)
      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, false);

      assertFalse("Expected success", result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode tools = responseNode.get("tools");

        boolean found = false;
        for (int i = 0; i < tools.size(); i++) {
          if ("mcpReadonlyTemplate".equals(tools.get(i).get("configId").asText())) {
            found = true;
            assertTrue("mcpReadonly should be true", tools.get(i).get("mcpReadonly").asBoolean());
            break;
          }
        }
        assertTrue("Readonly user should see mcpReadonly template", found);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a readonly user cannot see non-readonly templates in schema
   */
  public void testSchemaReadonlyUserCannotSeeReadwriteTemplates() {

    String configPrefix = "grouperGshTemplate.mcpReadwriteTemplate.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "Readwrite MCP Template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "A readwrite template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "everyone");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpEnabled", "true");
    // mcpReadonly defaults to false
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"readwrite result\");");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "schema");

      // readonly user (hasReadwriteAccess = false)
      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, false);

      assertFalse("Expected success", result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode tools = responseNode.get("tools");

        for (int i = 0; i < tools.size(); i++) {
          assertFalse("Readonly user should not see readwrite template",
              "mcpReadwriteTemplate".equals(tools.get(i).get("configId").asText()));
        }
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a readonly user cannot execute a non-readonly template
   */
  public void testExecuteReadonlyUserDeniedForReadwriteTemplate() {

    String configPrefix = "grouperGshTemplate.mcpRwExecTemplate.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "RW Only Template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "Requires readwrite");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "everyone");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpEnabled", "true");
    // mcpReadonly defaults to false
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"should not run\");");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "execute");
      arguments.put("configId", "mcpRwExecTemplate");

      // readonly user (hasReadwriteAccess = false)
      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, false);

      assertTrue("Expected error for readonly user on readwrite template", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue(text.contains("requires MCP readwrite access"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that a readwrite user can see both readonly and readwrite templates
   */
  public void testSchemaReadwriteUserSeesBothTypes() {

    String roPrefix = "grouperGshTemplate.mcpBothRo.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "templateName", "Both RO");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "templateDescription", "Readonly one");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "securityRunType", "everyone");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "mcpEnabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "mcpReadonly", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "numberOfInputs", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(roPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"ro\");");

    String rwPrefix = "grouperGshTemplate.mcpBothRw.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "templateName", "Both RW");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "templateDescription", "Readwrite one");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "securityRunType", "everyone");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "mcpEnabled", "true");
    // mcpReadonly defaults to false
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "numberOfInputs", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(rwPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"rw\");");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "schema");

      // readwrite user
      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertFalse("Expected success", result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode tools = responseNode.get("tools");

        boolean foundRo = false;
        boolean foundRw = false;
        for (int i = 0; i < tools.size(); i++) {
          String cid = tools.get(i).get("configId").asText();
          if ("mcpBothRo".equals(cid)) {
            foundRo = true;
          }
          if ("mcpBothRw".equals(cid)) {
            foundRw = true;
          }
        }
        assertTrue("Readwrite user should see readonly template", foundRo);
        assertTrue("Readwrite user should see readwrite template", foundRw);
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that scope validation rejects input values outside approved folders scope
   */
  public void testExecuteScopeValidationDeniesOutOfScopeFolders() {

    String configPrefix = "grouperGshTemplate.mcpScopeTemplate.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "Scope Template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "Template with scope restriction");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "everyone");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpEnabled", "true");
    // mcpReadonly defaults to false, so scope validation applies
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.name", "gsh_input_targetFolder");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.label", "Target Folder");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.description", "Folder to operate on");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.type", "string");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.required", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.validationType", "none");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.mcpScopeType", "folders");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"processed \" + gsh_input_targetFolder);");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
      // set up scope restrictions: only "approved:folder" is allowed
      authUser.setConsentReadwriteScopeRestricted(true);
      java.util.List<String> approvedFolders = new java.util.ArrayList<String>();
      approvedFolders.add("approved:folder");
      authUser.setConsentReadwriteFolders(approvedFolders);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "execute");
      arguments.put("configId", "mcpScopeTemplate");

      ObjectNode inputs = objectMapper.createObjectNode();
      inputs.put("targetFolder", "unapproved:folder");
      arguments.set("inputs", inputs);

      // readwrite user, but with scope restrictions
      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertTrue("Expected error for out-of-scope folder", result.get("isError").asBoolean());
      String text = result.get("content").get(0).get("text").asText();
      assertTrue("Should mention scope denial", text.contains("not within the approved"));
      assertTrue("Should mention folders", text.contains("folders"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that scope validation allows input values within approved scope
   */
  public void testExecuteScopeValidationAllowsInScopeValues() {

    String configPrefix = "grouperGshTemplate.mcpScopeAllowTemplate.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "Scope Allow Template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "Template with scope restriction");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "everyone");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpEnabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.name", "gsh_input_targetGroup");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.label", "Target Group");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.description", "Group to operate on");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.type", "string");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.required", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.validationType", "none");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.mcpScopeType", "groups");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "allowWsFromNoOwner", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"processed \" + gsh_input_targetGroup);");

    // create the group so the template can resolve it if needed
    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("approved:folder:testGroup")
        .assignName("approved:folder:testGroup")
        .assignCreateParentStemsIfNotExist(true)
        .save();

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
      // set up scope restrictions: "approved:folder" folder is allowed
      authUser.setConsentReadwriteScopeRestricted(true);
      java.util.List<String> approvedFolders = new java.util.ArrayList<String>();
      approvedFolders.add("approved:folder");
      authUser.setConsentReadwriteFolders(approvedFolders);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "execute");
      arguments.put("configId", "mcpScopeAllowTemplate");

      ObjectNode inputs = objectMapper.createObjectNode();
      inputs.put("targetGroup", "approved:folder:testGroup");
      arguments.set("inputs", inputs);

      // readwrite user with scope restrictions - group is under approved folder
      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertFalse("Expected success for in-scope group, got: " + result.toString(),
          result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that schema advertises mcpScopeType for inputs
   */
  public void testSchemaMcpScopeTypeInInputs() {

    String configPrefix = "grouperGshTemplate.mcpScopeSchemaTemplate.";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateType", "gsh");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateVersion", "V1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateName", "Scope Schema Template");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "templateDescription", "Template showing scope in schema");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "securityRunType", "everyone");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "mcpEnabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnFolders", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "showOnGroups", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "numberOfInputs", "1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.name", "gsh_input_folderName");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.label", "Folder Name");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.description", "Folder to operate on");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.type", "string");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.required", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.validationType", "none");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "input.0.mcpScopeType", "folders");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(configPrefix + "gshTemplate",
        "gsh_builtin_gshTemplateOutput.addOutputLine(\"result\");");

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("action", "schema");

      ObjectNode result = GrouperMcpInstitutionalTools.execute(arguments, authUser, true);

      assertFalse("Expected success", result.get("isError").asBoolean());

      String text = result.get("content").get(0).get("text").asText();
      try {
        JsonNode responseNode = objectMapper.readTree(text);
        JsonNode tools = responseNode.get("tools");

        JsonNode scopeTool = null;
        for (int i = 0; i < tools.size(); i++) {
          if ("mcpScopeSchemaTemplate".equals(tools.get(i).get("configId").asText())) {
            scopeTool = tools.get(i);
            break;
          }
        }
        assertNotNull("Should find mcpScopeSchemaTemplate in schema", scopeTool);

        JsonNode inputs = scopeTool.get("inputs");
        assertNotNull(inputs);
        assertEquals(1, inputs.size());
        JsonNode input0 = inputs.get(0);
        assertEquals("folders", input0.get("mcpScopeType").asText());
      } catch (Exception e) {
        fail("Failed to parse result JSON: " + e.getMessage());
      }
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}
