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
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpGetAttributeAssignmentsLite (attribute_assignment_get MCP tool).
 * Verifies that attributeAssignId is included in results.
 *
 * @author mchyzer
 */
public class GrouperMcpGetAttributeAssignmentsLiteTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpGetAttributeAssignmentsLiteTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpGetAttributeAssignmentsLiteTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpGetAttributeAssignmentsLiteTest("testAttributeAssignIdInResults"));
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

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());

    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * test that attribute_assignment_get returns attributeAssignId in results
   */
  public void testAttributeAssignIdInResults() {

    // create the group
    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:mcpGetAttrTestGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for get attribute assignments").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // create an attribute def assigned to groups with a string value
    AttributeDef attrDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpGetAttrTestDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignValueType(AttributeDefValueType.string)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), attrDef)
        .assignName("test:mcpGetAttrTestAttr")
        .save();

    // grant SUBJ0 privileges
    attrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    attrDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);

    // assign the attribute to the group
    group.getAttributeDelegate().assignAttribute(
        edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder.findByName(
            "test:mcpGetAttrTestAttr", true));

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // get attribute assignments
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("ownerGroupName", "test:mcpGetAttrTestGroup");

      ObjectNode result = GrouperMcpGetAttributeAssignmentsLite.execute(arguments, authUser);

      assertFalse("Expected success, got: " + result.toString(),
          result.get("isError").asBoolean());

      String resultText = result.get("content").get(0).get("text").asText();
      JsonNode resultsArray = objectMapper.readTree(resultText);
      assertTrue("Expected at least one result", resultsArray.isArray() && resultsArray.size() > 0);

      // verify attributeAssignId is present and non-blank
      JsonNode firstResult = resultsArray.get(0);
      assertTrue("Expected attributeAssignId in result",
          firstResult.has("attributeAssignId"));
      String assignId = firstResult.get("attributeAssignId").asText();
      assertTrue("Expected non-blank attributeAssignId", assignId != null && assignId.length() > 0);

      // verify other expected fields
      assertEquals("group", firstResult.get("attributeAssignType").asText());
      assertEquals("test:mcpGetAttrTestAttr", firstResult.get("attributeDefNameName").asText());
      assertEquals("test:mcpGetAttrTestGroup", firstResult.get("ownerGroupName").asText());

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that the tool definition has the correct name and required fields
   */
  public void testToolDefinition() {
    ObjectNode toolDef = GrouperMcpGetAttributeAssignmentsLite.toolDefinition();
    assertEquals("attribute_assignment_get", toolDef.get("name").asText());
    assertNotNull("Expected description", toolDef.get("description"));
    assertNotNull("Expected inputSchema", toolDef.get("inputSchema"));

    JsonNode required = toolDef.get("inputSchema").get("required");
    assertNotNull("Expected required array", required);
    assertTrue("Expected attributeAssignType in required", required.toString().contains("attributeAssignType"));
  }

  /**
   * test that missing attributeAssignType returns an error
   */
  public void testMissingAttributeAssignType() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      // intentionally not setting attributeAssignType

      ObjectNode result = GrouperMcpGetAttributeAssignmentsLite.execute(arguments, authUser);

      assertTrue("Expected error when attributeAssignType is missing",
          result.get("isError").asBoolean());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}
