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
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;

import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;

/**
 * unit tests for GrouperMcpAssignAttributes (attribute_assignment_save MCP tool),
 * including assignment-on-assignment for attestation-style workflows.
 *
 * @author mchyzer
 */
public class GrouperMcpAssignAttributesTest extends GrouperTest {

  /**
   *
   */
  public GrouperMcpAssignAttributesTest() {
    //empty
  }

  /**
   * @param name
   */
  public GrouperMcpAssignAttributesTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpAssignAttributesTest("testAssignAttributeOnAssignment"));
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
   * test assigning a marker attribute to a group and then assigning name/value
   * pair attributes on that assignment (assignment-on-assignment), similar to
   * how attestation is configured.
   */
  public void testAssignAttributeOnAssignment() {

    // create the group
    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:mcpAttrAsgnGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for assignment-on-assignment").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // create a marker attribute def assigned to groups
    AttributeDef markerDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpTestMarkerDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), markerDef)
        .assignName("test:mcpTestMarker")
        .save();

    // create a value attribute def that can be assigned to group assignments
    AttributeDef valueDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpTestValueDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroupAssn(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignValueType(AttributeDefValueType.string)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), valueDef)
        .assignName("test:mcpTestConfigAttr")
        .save();

    // grant SUBJ0 privileges on both attribute defs
    markerDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    markerDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);
    valueDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    valueDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // step 1: assign the marker attribute to the group
      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group");
      arguments.put("attributeAssignOperation", "assign_attr");
      arguments.put("attributeDefNameName", "test:mcpTestMarker");
      arguments.put("ownerGroupName", "test:mcpAttrAsgnGroup");

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, authUser);

      assertFalse("Expected success assigning marker, got: " + result.toString(),
          result.get("isError").asBoolean());

      // extract the attributeAssignId from the response
      String resultText = result.get("content").get(0).get("text").asText();
      JsonNode resultsArray = objectMapper.readTree(resultText);
      assertTrue("Expected at least one result", resultsArray.isArray() && resultsArray.size() > 0);

      JsonNode firstResult = resultsArray.get(0);
      assertTrue("Expected changed=true for marker assignment", firstResult.get("changed").asBoolean());

      JsonNode assigns = firstResult.get("attributeAssigns");
      assertNotNull("Expected attributeAssigns in result", assigns);
      assertTrue("Expected at least one assign", assigns.isArray() && assigns.size() > 0);

      String attributeAssignId = assigns.get(0).get("attributeAssignId").asText();
      assertNotNull("Expected attributeAssignId in response", attributeAssignId);
      assertTrue("Expected non-blank attributeAssignId", attributeAssignId.length() > 0);

      // flush so the assignment is visible to the finder in step 2
      HibernateSession.flush();

      // step 2: assign a name/value pair on that assignment (assignment-on-assignment)
      ObjectNode asgnOnAsgnArgs = objectMapper.createObjectNode();
      asgnOnAsgnArgs.put("attributeAssignType", "group_asgn");
      asgnOnAsgnArgs.put("attributeAssignOperation", "assign_attr");
      asgnOnAsgnArgs.put("attributeDefNameName", "test:mcpTestConfigAttr");
      asgnOnAsgnArgs.put("ownerAttributeAssignId", attributeAssignId);
      ArrayNode values = objectMapper.createArrayNode();
      values.add("testValue123");
      asgnOnAsgnArgs.set("values", values);

      ObjectNode asgnOnAsgnResult = GrouperMcpAssignAttributes.execute(asgnOnAsgnArgs, authUser);

      assertFalse("Expected success for assignment-on-assignment, got: " + asgnOnAsgnResult.toString(),
          asgnOnAsgnResult.get("isError").asBoolean());

      // verify the result
      String asgnOnAsgnText = asgnOnAsgnResult.get("content").get(0).get("text").asText();
      JsonNode asgnOnAsgnResults = objectMapper.readTree(asgnOnAsgnText);
      assertTrue("Expected at least one result for assignment-on-assignment",
          asgnOnAsgnResults.isArray() && asgnOnAsgnResults.size() > 0);

      JsonNode asgnOnAsgnFirst = asgnOnAsgnResults.get(0);
      assertTrue("Expected changed=true for assignment-on-assignment",
          asgnOnAsgnFirst.get("changed").asBoolean());

      JsonNode asgnOnAsgnAssigns = asgnOnAsgnFirst.get("attributeAssigns");
      assertNotNull("Expected attributeAssigns in assignment-on-assignment result", asgnOnAsgnAssigns);
      assertTrue("Expected at least one assign in assignment-on-assignment result",
          asgnOnAsgnAssigns.isArray() && asgnOnAsgnAssigns.size() > 0);

      assertEquals("group_asgn", asgnOnAsgnAssigns.get(0).get("attributeAssignType").asText());
      assertEquals("test:mcpTestConfigAttr", asgnOnAsgnAssigns.get(0).get("attributeDefNameName").asText());

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that assignment-on-assignment fails with a helpful error when
   * ownerAttributeAssignId is missing but an _asgn type is used
   */
  public void testAssignAttributeOnAssignment_missingOwnerId() {

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode arguments = objectMapper.createObjectNode();
      arguments.put("attributeAssignType", "group_asgn");
      arguments.put("attributeAssignOperation", "assign_attr");
      arguments.put("attributeDefNameName", "test:someAttr");
      // intentionally not setting ownerAttributeAssignId

      ObjectNode result = GrouperMcpAssignAttributes.execute(arguments, authUser);

      // the WS layer should return an error since there is no owner specified
      assertTrue("Expected error when no owner specified for _asgn type",
          result.get("isError").asBoolean());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that assignment-on-assignment validates OAuth scope against the
   * underlying owner (e.g. the group that the marker attribute is assigned to)
   */
  public void testAssignAttributeOnAssignment_scopeValidation() {

    // create the group under "test:" stem
    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:scopeTestGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for scope validation on assignment-on-assignment").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // create marker attribute def assigned to groups
    AttributeDef markerDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:scopeMarkerDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), markerDef)
        .assignName("test:scopeMarker")
        .save();

    // create value attribute def for assignment-on-assignment
    AttributeDef valueDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:scopeValueDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroupAssn(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignValueType(AttributeDefValueType.string)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), valueDef)
        .assignName("test:scopeConfigAttr")
        .save();

    // grant SUBJ0 full privileges
    markerDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    markerDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);
    valueDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    valueDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);

    // step 1: assign the marker as non-OAuth user to get the assignment ID
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    String attributeAssignId = null;
    try {
      GrouperMcpAuthUser nonOAuthUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      ObjectNode markerArgs = objectMapper.createObjectNode();
      markerArgs.put("attributeAssignType", "group");
      markerArgs.put("attributeAssignOperation", "assign_attr");
      markerArgs.put("attributeDefNameName", "test:scopeMarker");
      markerArgs.put("ownerGroupName", "test:scopeTestGroup");

      ObjectNode markerResult = GrouperMcpAssignAttributes.execute(markerArgs, nonOAuthUser);
      assertFalse("Expected success assigning marker, got: " + markerResult.toString(),
          markerResult.get("isError").asBoolean());

      String resultText = markerResult.get("content").get(0).get("text").asText();
      JsonNode resultsArray = objectMapper.readTree(resultText);
      JsonNode assigns = resultsArray.get(0).get("attributeAssigns");
      attributeAssignId = assigns.get(0).get("attributeAssignId").asText();

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(session);
    }

    assertNotNull("Should have attributeAssignId from marker assignment", attributeAssignId);

    // flush so the assignment is visible for the scope check lookup
    HibernateSession.flush();

    // step 2: try assignment-on-assignment as OAuth user scoped to "other:" — should fail
    session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser oauthUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
      oauthUser.setOAuthAuthenticated(true);
      oauthUser.setConsentReadwriteScopeRestricted(true);
      List<String> approvedFolders = new ArrayList<String>();
      approvedFolders.add("other");
      oauthUser.setConsentReadwriteFolders(approvedFolders);

      ObjectNode asgnArgs = objectMapper.createObjectNode();
      asgnArgs.put("attributeAssignType", "group_asgn");
      asgnArgs.put("attributeAssignOperation", "assign_attr");
      asgnArgs.put("attributeDefNameName", "test:scopeConfigAttr");
      asgnArgs.put("ownerAttributeAssignId", attributeAssignId);
      ArrayNode values = objectMapper.createArrayNode();
      values.add("someValue");
      asgnArgs.set("values", values);

      ObjectNode result = GrouperMcpAssignAttributes.execute(asgnArgs, oauthUser);

      assertTrue("Expected scope denial for out-of-scope group, got: " + result.toString(),
          result.get("isError").asBoolean());
      String errorText = result.get("content").get(0).get("text").asText();
      assertTrue("Should mention scope denial, got: " + errorText,
          errorText.contains("not within the approved") || errorText.contains("scope"));

    } finally {
      GrouperSession.stopQuietly(session);
    }

    // step 3: try assignment-on-assignment as OAuth user scoped to "test:" — should succeed
    session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser oauthUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);
      oauthUser.setOAuthAuthenticated(true);
      oauthUser.setConsentReadwriteScopeRestricted(true);
      List<String> approvedFolders = new ArrayList<String>();
      approvedFolders.add("test");
      oauthUser.setConsentReadwriteFolders(approvedFolders);

      ObjectNode asgnArgs = objectMapper.createObjectNode();
      asgnArgs.put("attributeAssignType", "group_asgn");
      asgnArgs.put("attributeAssignOperation", "assign_attr");
      asgnArgs.put("attributeDefNameName", "test:scopeConfigAttr");
      asgnArgs.put("ownerAttributeAssignId", attributeAssignId);
      ArrayNode values = objectMapper.createArrayNode();
      values.add("someValue");
      asgnArgs.set("values", values);

      ObjectNode result = GrouperMcpAssignAttributes.execute(asgnArgs, oauthUser);

      assertFalse("Expected success for in-scope group, got: " + result.toString(),
          result.get("isError").asBoolean());

    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}
