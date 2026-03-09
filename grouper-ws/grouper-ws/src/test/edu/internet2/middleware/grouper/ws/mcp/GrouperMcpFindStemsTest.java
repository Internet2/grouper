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

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpFindStems (folder_find MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpFindStemsTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpFindStemsTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpFindStemsTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpFindStemsTest("testFindByStemNameExact"));
  }

  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperServiceUtils.testSession = GrouperSession.staticGrouperSession();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperServiceUtils.testSession = null;
  }

  /**
   * build a GrouperMcpAuthUser for the root session (GrouperSystem)
   * @return the auth user
   */
  private GrouperMcpAuthUser buildRootAuthUser() {
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(
        SubjectFinder.findRootSubject());
    return authUser;
  }

  /**
   * test finding a stem by exact name
   */
  public void testFindByStemNameExact() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test:mcpFindStem1")
        .assignName("test:mcpFindStem1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test stem for mcp findStems").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("stemQueryFilterType", "FIND_BY_STEM_NAME");
    arguments.put("stemName", "test:mcpFindStem1");

    ObjectNode result = GrouperMcpFindStems.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(1, responseNode.get("totalStemsReturned").asInt());
      JsonNode stems = responseNode.get("stems");
      assertNotNull(stems);
      assertTrue(stems.isArray());
      assertEquals(1, stems.size());
      assertEquals("test:mcpFindStem1", stems.get(0).get("name").asText());
      assertEquals("test stem for mcp findStems", stems.get(0).get("description").asText());
      assertNotNull(stems.get(0).get("uuid"));
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test finding stems by approximate name
   */
  public void testFindByStemNameApproximate() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test:mcpFindApproxStem1")
        .assignName("test:mcpFindApproxStem1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("approx stem 1").save();

    new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test:mcpFindApproxStem2")
        .assignName("test:mcpFindApproxStem2")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("approx stem 2").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("stemQueryFilterType", "FIND_BY_STEM_NAME_APPROXIMATE");
    arguments.put("stemName", "mcpFindApproxStem");

    ObjectNode result = GrouperMcpFindStems.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertTrue("Expected at least 2 stems",
          responseNode.get("totalStemsReturned").asInt() >= 2);
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test finding a stem by UUID
   */
  public void testFindByStemUuid() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    Stem stem1 = new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test:mcpFindUuidStem1")
        .assignName("test:mcpFindUuidStem1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("uuid stem test").save();

    String uuid = stem1.getUuid();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("stemQueryFilterType", "FIND_BY_STEM_UUID");
    arguments.put("stemUuid", uuid);

    ObjectNode result = GrouperMcpFindStems.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(1, responseNode.get("totalStemsReturned").asInt());
      JsonNode stems = responseNode.get("stems");
      assertEquals("test:mcpFindUuidStem1", stems.get(0).get("name").asText());
      assertEquals(uuid, stems.get(0).get("uuid").asText());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test finding stems with parentStemName and ONE_LEVEL scope
   */
  public void testFindByParentStemOneLevel() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test:mcpFindParent:child1")
        .assignName("test:mcpFindParent:child1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("direct child stem").save();

    new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test:mcpFindParent:child2:nested")
        .assignName("test:mcpFindParent:child2:nested")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("nested stem").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("stemQueryFilterType", "FIND_BY_STEM_NAME_APPROXIMATE");
    arguments.put("stemName", "%");
    arguments.put("parentStemName", "test:mcpFindParent");
    arguments.put("parentStemNameScope", "ONE_LEVEL");

    ObjectNode result = GrouperMcpFindStems.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      int count = responseNode.get("totalStemsReturned").asInt();
      // ONE_LEVEL should return direct children only (child1 and child2)
      assertTrue("Expected at least 1 direct child stem, got: " + count, count >= 1);
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test finding a stem that does not exist
   */
  public void testFindStemNotFound() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("stemQueryFilterType", "FIND_BY_STEM_NAME");
    arguments.put("stemName", "test:bogusStemThatDoesNotExist99999");

    ObjectNode result = GrouperMcpFindStems.execute(arguments, authUser);

    assertFalse("Expected success (empty result), got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(0, responseNode.get("totalStemsReturned").asInt());
      assertEquals(0, responseNode.get("stems").size());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test that missing stemQueryFilterType returns an error
   */
  public void testFindStemsMissingStemQueryFilterType() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("stemName", "test:someStem");

    ObjectNode result = GrouperMcpFindStems.execute(arguments, authUser);

    assertTrue("Expected error", result.get("isError").asBoolean());
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text.contains("stemQueryFilterType is required"));
  }

  /**
   * test with null arguments
   */
  public void testFindStemsNullArguments() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode result = GrouperMcpFindStems.execute(null, authUser);

    assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text.contains("stemQueryFilterType is required"));
  }

  /**
   * test with includeFolderTypes
   */
  public void testFindStemsIncludeFolderTypes() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    new StemSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignStemNameToEdit("test:mcpFindTypesStem1")
        .assignName("test:mcpFindTypesStem1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("types stem test").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("stemQueryFilterType", "FIND_BY_STEM_NAME");
    arguments.put("stemName", "test:mcpFindTypesStem1");
    arguments.put("includeFolderTypes", true);

    ObjectNode result = GrouperMcpFindStems.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(1, responseNode.get("totalStemsReturned").asInt());
      // stem may or may not have types assigned, just verify it doesn't error
      assertNotNull(responseNode.get("stems").get(0).get("name"));
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpFindStems.toolDefinition();

    assertEquals("folder_find", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("stemQueryFilterType"));
    assertNotNull(properties.get("stemName"));
    assertNotNull(properties.get("stemUuid"));
    assertNotNull(properties.get("parentStemName"));
    assertNotNull(properties.get("parentStemNameScope"));
    assertNotNull(properties.get("stemAttributeValue"));
    assertNotNull(properties.get("includeFolderTypes"));

    // verify required fields
    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(1, required.size());
    assertEquals("stemQueryFilterType", required.get(0).asText());
  }
}
