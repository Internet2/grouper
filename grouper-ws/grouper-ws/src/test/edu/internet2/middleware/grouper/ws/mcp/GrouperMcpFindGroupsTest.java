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
 * unit tests for GrouperMcpFindGroups (group_find MCP tool)
 *
 * @author mchyzer
 */
public class GrouperMcpFindGroupsTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpFindGroupsTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpFindGroupsTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpFindGroupsTest("testFindByGroupNameExact"));
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
   * test finding a group by exact name
   */
  public void testFindByGroupNameExact() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpFindGroup1")
        .assignName("test:mcpFindGroup1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for mcp findGroups").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("queryFilterType", "FIND_BY_GROUP_NAME_EXACT");
    arguments.put("groupName", "test:mcpFindGroup1");

    ObjectNode result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(1, responseNode.get("totalGroupsReturned").asInt());
      JsonNode groups = responseNode.get("groups");
      assertNotNull(groups);
      assertTrue(groups.isArray());
      assertEquals(1, groups.size());
      assertEquals("test:mcpFindGroup1", groups.get(0).get("name").asText());
      assertEquals("test group for mcp findGroups", groups.get(0).get("description").asText());
      assertNotNull(groups.get(0).get("uuid"));
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test finding groups by approximate name
   */
  public void testFindByGroupNameApproximate() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpFindApprox1")
        .assignName("test:mcpFindApprox1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("approx test 1").save();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpFindApprox2")
        .assignName("test:mcpFindApprox2")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("approx test 2").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("queryFilterType", "FIND_BY_GROUP_NAME_APPROXIMATE");
    arguments.put("groupName", "mcpFindApprox");

    ObjectNode result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertTrue("Expected at least 2 groups",
          responseNode.get("totalGroupsReturned").asInt() >= 2);
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test finding groups by stem name
   */
  public void testFindByStemName() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpFindStem:group1")
        .assignName("test:mcpFindStem:group1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("stem test 1").save();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpFindStem:group2")
        .assignName("test:mcpFindStem:group2")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("stem test 2").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("queryFilterType", "FIND_BY_STEM_NAME");
    arguments.put("stemName", "test:mcpFindStem");

    ObjectNode result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertTrue("Expected at least 2 groups",
          responseNode.get("totalGroupsReturned").asInt() >= 2);
      JsonNode groups = responseNode.get("groups");
      for (int i = 0; i < groups.size(); i++) {
        assertTrue("Group should be in test:mcpFindStem stem",
            groups.get(i).get("name").asText().startsWith("test:mcpFindStem:"));
      }
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test finding a group by UUID
   */
  public void testFindByGroupUuid() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    Group group1 = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpFindUuidGroup1")
        .assignName("test:mcpFindUuidGroup1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("uuid test").save();

    String uuid = group1.getUuid();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("queryFilterType", "FIND_BY_GROUP_UUID");
    arguments.put("groupUuid", uuid);

    ObjectNode result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(1, responseNode.get("totalGroupsReturned").asInt());
      JsonNode groups = responseNode.get("groups");
      assertEquals("test:mcpFindUuidGroup1", groups.get(0).get("name").asText());
      assertEquals(uuid, groups.get(0).get("uuid").asText());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test paging with custom page size
   */
  public void testFindGroupsPaging() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    for (int i = 0; i < 3; i++) {
      new GroupSave(GrouperSession.staticGrouperSession())
          .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
          .assignGroupNameToEdit("test:mcpFindPage:pageGroup" + i)
          .assignName("test:mcpFindPage:pageGroup" + i)
          .assignCreateParentStemsIfNotExist(true)
          .assignDescription("paging test " + i).save();
    }

    // page 1, size 2
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("queryFilterType", "FIND_BY_STEM_NAME");
    arguments.put("stemName", "test:mcpFindPage");
    arguments.put("pageSize", 2);
    arguments.put("pageNumber", 1);

    ObjectNode result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(2, responseNode.get("pageSize").asInt());
      assertEquals(1, responseNode.get("pageNumber").asInt());
      assertEquals(2, responseNode.get("groups").size());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }

    // page 2, size 2 - re-establish session for second call
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();

    arguments = objectMapper.createObjectNode();
    arguments.put("queryFilterType", "FIND_BY_STEM_NAME");
    arguments.put("stemName", "test:mcpFindPage");
    arguments.put("pageSize", 2);
    arguments.put("pageNumber", 2);

    result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(1, responseNode.get("groups").size());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test finding a group that does not exist by exact name
   */
  public void testFindGroupNotFound() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("queryFilterType", "FIND_BY_GROUP_NAME_EXACT");
    arguments.put("groupName", "test:bogusGroupThatDoesNotExist99999");

    ObjectNode result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertFalse("Expected success (empty result), got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(0, responseNode.get("totalGroupsReturned").asInt());
      assertEquals(0, responseNode.get("groups").size());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test with stemNameScope ONE_LEVEL
   */
  public void testFindByStemNameOneLevel() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpFindOneLevel:directGroup")
        .assignName("test:mcpFindOneLevel:directGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("direct child").save();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpFindOneLevel:sub:nestedGroup")
        .assignName("test:mcpFindOneLevel:sub:nestedGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("nested child").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("queryFilterType", "FIND_BY_STEM_NAME");
    arguments.put("stemName", "test:mcpFindOneLevel");
    arguments.put("stemNameScope", "ONE_LEVEL");

    ObjectNode result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals("ONE_LEVEL should only return direct children",
          1, responseNode.get("totalGroupsReturned").asInt());
      assertEquals("test:mcpFindOneLevel:directGroup",
          responseNode.get("groups").get(0).get("name").asText());
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }

  /**
   * test that missing queryFilterType returns an error
   */
  public void testFindGroupsMissingQueryFilterType() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("groupName", "test:someGroup");

    ObjectNode result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertTrue("Expected error", result.get("isError").asBoolean());
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text.contains("queryFilterType is required"));
  }

  /**
   * test with null arguments
   */
  public void testFindGroupsNullArguments() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    ObjectNode result = GrouperMcpFindGroups.execute(null, authUser);

    assertTrue("Expected error for null arguments", result.get("isError").asBoolean());
    String text = result.get("content").get(0).get("text").asText();
    assertTrue(text.contains("queryFilterType is required"));
  }

  /**
   * test the tool definition is well-formed
   */
  public void testToolDefinition() {

    ObjectNode toolDef = GrouperMcpFindGroups.toolDefinition();

    assertEquals("group_find", toolDef.get("name").asText());
    assertNotNull(toolDef.get("description"));
    assertNotNull(toolDef.get("inputSchema"));

    JsonNode properties = toolDef.get("inputSchema").get("properties");
    assertNotNull(properties.get("queryFilterType"));
    assertNotNull(properties.get("groupName"));
    assertNotNull(properties.get("groupUuid"));
    assertNotNull(properties.get("stemName"));
    assertNotNull(properties.get("stemNameScope"));
    assertNotNull(properties.get("groupAttributeValue"));
    assertNotNull(properties.get("typeOfGroups"));
    assertNotNull(properties.get("pageSize"));
    assertNotNull(properties.get("pageNumber"));
    assertNotNull(properties.get("sortString"));
    assertNotNull(properties.get("ascending"));
    assertNotNull(properties.get("includeGroupTypes"));
    assertNotNull(properties.get("includeGroupEligibilityRequirement"));
    assertNotNull(properties.get("includeProvisioning"));

    // verify required fields
    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull(required);
    assertTrue(required.isArray());
    assertEquals(1, required.size());
    assertEquals("queryFilterType", required.get(0).asText());
  }

  /**
   * test with includeGroupTypes
   */
  public void testFindGroupsIncludeGroupTypes() {

    GrouperMcpAuthUser authUser = buildRootAuthUser();

    new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("test:mcpFindTypesGroup1")
        .assignName("test:mcpFindTypesGroup1")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("types test").save();

    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("queryFilterType", "FIND_BY_GROUP_NAME_EXACT");
    arguments.put("groupName", "test:mcpFindTypesGroup1");
    arguments.put("includeGroupTypes", true);

    ObjectNode result = GrouperMcpFindGroups.execute(arguments, authUser);

    assertFalse("Expected success, got: " + result.toString(),
        result.get("isError").asBoolean());

    String text = result.get("content").get(0).get("text").asText();
    try {
      JsonNode responseNode = objectMapper.readTree(text);
      assertEquals(1, responseNode.get("totalGroupsReturned").asInt());
      // group may or may not have types assigned, just verify it doesn't error
      assertNotNull(responseNode.get("groups").get(0).get("name"));
    } catch (Exception e) {
      fail("Failed to parse result JSON: " + e.getMessage());
    }
  }
}
