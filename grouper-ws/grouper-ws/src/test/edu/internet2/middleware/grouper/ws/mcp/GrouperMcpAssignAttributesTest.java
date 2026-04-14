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

  /**
   * test that when attributeAssignOperation is omitted the default is assign_attr
   * for a non-multi-assignable attribute def, and add_attr for a multi-assignable one
   */
  public void testDefaultAttributeAssignOperation() {

    // create the group
    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:mcpDefaultOpGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for default operation").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // create a non-multi-assignable attribute def
    AttributeDef singleDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpSingleAssignDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(false)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), singleDef)
        .assignName("test:mcpSingleAssignAttr")
        .save();

    // create a multi-assignable attribute def
    AttributeDef multiDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpMultiAssignDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(true)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), multiDef)
        .assignName("test:mcpMultiAssignAttr")
        .save();

    // grant SUBJ0 privileges on both attribute defs
    singleDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    singleDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);
    multiDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    multiDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // test 1: non-multi-assignable, omit attributeAssignOperation — should default to assign_attr
      ObjectNode singleArgs = objectMapper.createObjectNode();
      singleArgs.put("attributeAssignType", "group");
      // intentionally not setting attributeAssignOperation
      singleArgs.put("attributeDefNameName", "test:mcpSingleAssignAttr");
      singleArgs.put("ownerGroupName", "test:mcpDefaultOpGroup");

      ObjectNode singleResult = GrouperMcpAssignAttributes.execute(singleArgs, authUser);

      assertFalse("Expected success for single-assign default, got: " + singleResult.toString(),
          singleResult.get("isError").asBoolean());

      String singleText = singleResult.get("content").get(0).get("text").asText();
      JsonNode singleResults = objectMapper.readTree(singleText);
      assertTrue("Expected changed=true", singleResults.get(0).get("changed").asBoolean());

      // assign again without attributeAssignOperation — should default to assign_attr (no change)
      ObjectNode singleArgs2 = objectMapper.createObjectNode();
      singleArgs2.put("attributeAssignType", "group");
      singleArgs2.put("attributeDefNameName", "test:mcpSingleAssignAttr");
      singleArgs2.put("ownerGroupName", "test:mcpDefaultOpGroup");

      ObjectNode singleResult2 = GrouperMcpAssignAttributes.execute(singleArgs2, authUser);

      assertFalse("Expected success for second single-assign default, got: " + singleResult2.toString(),
          singleResult2.get("isError").asBoolean());

      String singleText2 = singleResult2.get("content").get(0).get("text").asText();
      JsonNode singleResults2 = objectMapper.readTree(singleText2);
      // assign_attr should not change since it's already assigned
      assertFalse("Expected changed=false for already-assigned single-assign attr",
          singleResults2.get(0).get("changed").asBoolean());

      // test 2: multi-assignable, omit attributeAssignOperation — should default to add_attr
      ObjectNode multiArgs = objectMapper.createObjectNode();
      multiArgs.put("attributeAssignType", "group");
      // intentionally not setting attributeAssignOperation
      multiArgs.put("attributeDefNameName", "test:mcpMultiAssignAttr");
      multiArgs.put("ownerGroupName", "test:mcpDefaultOpGroup");

      ObjectNode multiResult = GrouperMcpAssignAttributes.execute(multiArgs, authUser);

      assertFalse("Expected success for multi-assign default, got: " + multiResult.toString(),
          multiResult.get("isError").asBoolean());

      String multiText = multiResult.get("content").get(0).get("text").asText();
      JsonNode multiResults = objectMapper.readTree(multiText);
      assertTrue("Expected changed=true for first multi-assign",
          multiResults.get(0).get("changed").asBoolean());

      // assign again without attributeAssignOperation — should default to add_attr (creates second)
      ObjectNode multiArgs2 = objectMapper.createObjectNode();
      multiArgs2.put("attributeAssignType", "group");
      multiArgs2.put("attributeDefNameName", "test:mcpMultiAssignAttr");
      multiArgs2.put("ownerGroupName", "test:mcpDefaultOpGroup");

      ObjectNode multiResult2 = GrouperMcpAssignAttributes.execute(multiArgs2, authUser);

      assertFalse("Expected success for second multi-assign default, got: " + multiResult2.toString(),
          multiResult2.get("isError").asBoolean());

      String multiText2 = multiResult2.get("content").get(0).get("text").asText();
      JsonNode multiResults2 = objectMapper.readTree(multiText2);
      // add_attr should always change since it adds a new assignment
      assertTrue("Expected changed=true for second multi-assign (add_attr default)",
          multiResults2.get(0).get("changed").asBoolean());

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that remove_attr with attributeAssignId removes a specific assignment
   * when there are multiple assignments of the same multi-assignable attribute
   */
  public void testRemoveByAttributeAssignId() {

    // create the group
    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:mcpRemoveByIdGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for remove by ID").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // create a multi-assignable attribute def
    AttributeDef multiDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpRemoveByIdDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignMultiAssignable(true)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), multiDef)
        .assignName("test:mcpRemoveByIdAttr")
        .save();

    // grant SUBJ0 privileges
    multiDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    multiDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // add two assignments of the same attribute
      ObjectNode addArgs1 = objectMapper.createObjectNode();
      addArgs1.put("attributeAssignType", "group");
      addArgs1.put("attributeAssignOperation", "add_attr");
      addArgs1.put("attributeDefNameName", "test:mcpRemoveByIdAttr");
      addArgs1.put("ownerGroupName", "test:mcpRemoveByIdGroup");

      ObjectNode addResult1 = GrouperMcpAssignAttributes.execute(addArgs1, authUser);
      assertFalse("Expected success for first add", addResult1.get("isError").asBoolean());

      String addText1 = addResult1.get("content").get(0).get("text").asText();
      JsonNode addResults1 = objectMapper.readTree(addText1);
      String assignId1 = addResults1.get(0).get("attributeAssigns").get(0).get("attributeAssignId").asText();

      ObjectNode addArgs2 = objectMapper.createObjectNode();
      addArgs2.put("attributeAssignType", "group");
      addArgs2.put("attributeAssignOperation", "add_attr");
      addArgs2.put("attributeDefNameName", "test:mcpRemoveByIdAttr");
      addArgs2.put("ownerGroupName", "test:mcpRemoveByIdGroup");

      ObjectNode addResult2 = GrouperMcpAssignAttributes.execute(addArgs2, authUser);
      assertFalse("Expected success for second add", addResult2.get("isError").asBoolean());

      String addText2 = addResult2.get("content").get(0).get("text").asText();
      JsonNode addResults2 = objectMapper.readTree(addText2);
      String assignId2 = addResults2.get(0).get("attributeAssigns").get(0).get("attributeAssignId").asText();

      // verify we have two different assignment IDs
      assertFalse("Expected two different assignment IDs", assignId1.equals(assignId2));

      // remove just the first assignment by ID
      ObjectNode removeArgs = objectMapper.createObjectNode();
      removeArgs.put("attributeAssignType", "group");
      removeArgs.put("attributeAssignOperation", "remove_attr");
      removeArgs.put("attributeDefNameName", "test:mcpRemoveByIdAttr");
      removeArgs.put("ownerGroupName", "test:mcpRemoveByIdGroup");
      removeArgs.put("attributeAssignId", assignId1);

      ObjectNode removeResult = GrouperMcpAssignAttributes.execute(removeArgs, authUser);
      assertFalse("Expected success for remove by ID, got: " + removeResult.toString(),
          removeResult.get("isError").asBoolean());

      // verify that assignment 2 still exists by getting assignments
      ObjectNode getArgs = objectMapper.createObjectNode();
      getArgs.put("attributeAssignType", "group");
      getArgs.put("ownerGroupName", "test:mcpRemoveByIdGroup");
      getArgs.put("attributeDefNameName", "test:mcpRemoveByIdAttr");

      ObjectNode getResult = GrouperMcpGetAttributeAssignmentsLite.execute(getArgs, authUser);
      assertFalse("Expected success for get", getResult.get("isError").asBoolean());

      String getText = getResult.get("content").get(0).get("text").asText();
      JsonNode getResults = objectMapper.readTree(getText);
      // should have exactly one remaining assignment
      assertEquals("Expected exactly one remaining assignment", 1, getResults.size());
      assertEquals("Expected remaining assignment to be the second one",
          assignId2, getResults.get(0).get("attributeAssignId").asText());

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that assign_attr with values replaces existing values on a single-valued
   * attribute instead of erroring
   */
  public void testAssignAttrReplacesExistingValue() {

    // create the group
    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:mcpReplaceValueGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for value replacement").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // create a single-valued attribute def
    AttributeDef valueDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpReplaceValueDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignValueType(AttributeDefValueType.string)
        .assignMultiAssignable(false)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), valueDef)
        .assignName("test:mcpReplaceValueAttr")
        .save();

    // grant SUBJ0 privileges
    valueDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    valueDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // assign with initial value
      ObjectNode args1 = objectMapper.createObjectNode();
      args1.put("attributeAssignType", "group");
      args1.put("attributeAssignOperation", "assign_attr");
      args1.put("attributeDefNameName", "test:mcpReplaceValueAttr");
      args1.put("ownerGroupName", "test:mcpReplaceValueGroup");
      ArrayNode values1 = objectMapper.createArrayNode();
      values1.add("originalValue");
      args1.set("values", values1);

      ObjectNode result1 = GrouperMcpAssignAttributes.execute(args1, authUser);
      assertFalse("Expected success for initial assign, got: " + result1.toString(),
          result1.get("isError").asBoolean());

      // assign again with a different value — should replace, not error
      ObjectNode args2 = objectMapper.createObjectNode();
      args2.put("attributeAssignType", "group");
      args2.put("attributeAssignOperation", "assign_attr");
      args2.put("attributeDefNameName", "test:mcpReplaceValueAttr");
      args2.put("ownerGroupName", "test:mcpReplaceValueGroup");
      ArrayNode values2 = objectMapper.createArrayNode();
      values2.add("replacedValue");
      args2.set("values", values2);

      ObjectNode result2 = GrouperMcpAssignAttributes.execute(args2, authUser);
      assertFalse("Expected success for value replacement (not error), got: " + result2.toString(),
          result2.get("isError").asBoolean());

      // verify the value was actually replaced
      ObjectNode getArgs = objectMapper.createObjectNode();
      getArgs.put("attributeAssignType", "group");
      getArgs.put("ownerGroupName", "test:mcpReplaceValueGroup");
      getArgs.put("attributeDefNameName", "test:mcpReplaceValueAttr");

      ObjectNode getResult = GrouperMcpGetAttributeAssignmentsLite.execute(getArgs, authUser);
      assertFalse("Expected success for get", getResult.get("isError").asBoolean());

      String getText = getResult.get("content").get(0).get("text").asText();
      JsonNode getResults = objectMapper.readTree(getText);
      assertEquals("Expected one assignment", 1, getResults.size());

      JsonNode valuesNode = getResults.get(0).get("values");
      assertNotNull("Expected values in result", valuesNode);
      assertEquals("Expected one value", 1, valuesNode.size());
      assertEquals("Expected the replaced value", "replacedValue", valuesNode.get(0).asText());

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * test that the valueOperation parameter is honored when explicitly passed.
   * Uses add_value to add a second value to a multi-valued attribute, and
   * remove_value to remove a specific value.
   */
  public void testExplicitValueOperation() {

    // create the group
    Group group = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:mcpValueOpGroup")
        .assignCreateParentStemsIfNotExist(true)
        .assignDescription("test group for explicit valueOperation").save();

    group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    // create a multi-valued attribute def
    AttributeDef valueDef = new AttributeDefSave(GrouperSession.staticGrouperSession())
        .assignName("test:mcpValueOpDef")
        .assignCreateParentStemsIfNotExist(true)
        .assignToGroup(true)
        .assignAttributeDefType(AttributeDefType.attr)
        .assignValueType(AttributeDefValueType.string)
        .assignMultiValued(true)
        .assignMultiAssignable(false)
        .save();

    new AttributeDefNameSave(GrouperSession.staticGrouperSession(), valueDef)
        .assignName("test:mcpValueOpAttr")
        .save();

    // grant SUBJ0 privileges
    valueDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_UPDATE, false);
    valueDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0,
        AttributeDefPrivilege.ATTR_READ, false);

    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperMcpAuthUser authUser = new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0);

      // assign with initial value using replace_values (default for assign_attr)
      ObjectNode args1 = objectMapper.createObjectNode();
      args1.put("attributeAssignType", "group");
      args1.put("attributeAssignOperation", "assign_attr");
      args1.put("attributeDefNameName", "test:mcpValueOpAttr");
      args1.put("ownerGroupName", "test:mcpValueOpGroup");
      ArrayNode values1 = objectMapper.createArrayNode();
      values1.add("value1");
      args1.set("values", values1);

      ObjectNode result1 = GrouperMcpAssignAttributes.execute(args1, authUser);
      assertFalse("Expected success for initial assign, got: " + result1.toString(),
          result1.get("isError").asBoolean());

      // use explicit valueOperation=add_value to add a second value
      ObjectNode args2 = objectMapper.createObjectNode();
      args2.put("attributeAssignType", "group");
      args2.put("attributeAssignOperation", "assign_attr");
      args2.put("attributeDefNameName", "test:mcpValueOpAttr");
      args2.put("ownerGroupName", "test:mcpValueOpGroup");
      args2.put("valueOperation", "add_value");
      ArrayNode values2 = objectMapper.createArrayNode();
      values2.add("value2");
      args2.set("values", values2);

      ObjectNode result2 = GrouperMcpAssignAttributes.execute(args2, authUser);
      assertFalse("Expected success for add_value, got: " + result2.toString(),
          result2.get("isError").asBoolean());

      // verify both values exist
      ObjectNode getArgs = objectMapper.createObjectNode();
      getArgs.put("attributeAssignType", "group");
      getArgs.put("ownerGroupName", "test:mcpValueOpGroup");
      getArgs.put("attributeDefNameName", "test:mcpValueOpAttr");

      ObjectNode getResult = GrouperMcpGetAttributeAssignmentsLite.execute(getArgs, authUser);
      assertFalse("Expected success for get", getResult.get("isError").asBoolean());

      String getText = getResult.get("content").get(0).get("text").asText();
      JsonNode getResults = objectMapper.readTree(getText);
      assertEquals("Expected one assignment", 1, getResults.size());

      JsonNode valuesNode = getResults.get(0).get("values");
      assertNotNull("Expected values in result", valuesNode);
      assertEquals("Expected two values after add_value", 2, valuesNode.size());

      // use explicit valueOperation=remove_value to remove value1
      ObjectNode args3 = objectMapper.createObjectNode();
      args3.put("attributeAssignType", "group");
      args3.put("attributeAssignOperation", "assign_attr");
      args3.put("attributeDefNameName", "test:mcpValueOpAttr");
      args3.put("ownerGroupName", "test:mcpValueOpGroup");
      args3.put("valueOperation", "remove_value");
      ArrayNode values3 = objectMapper.createArrayNode();
      values3.add("value1");
      args3.set("values", values3);

      ObjectNode result3 = GrouperMcpAssignAttributes.execute(args3, authUser);
      assertFalse("Expected success for remove_value, got: " + result3.toString(),
          result3.get("isError").asBoolean());

      // verify only value2 remains
      ObjectNode getResult2 = GrouperMcpGetAttributeAssignmentsLite.execute(getArgs, authUser);
      assertFalse("Expected success for get", getResult2.get("isError").asBoolean());

      String getText2 = getResult2.get("content").get(0).get("text").asText();
      JsonNode getResults2 = objectMapper.readTree(getText2);
      assertEquals("Expected one assignment", 1, getResults2.size());

      JsonNode valuesNode2 = getResults2.get(0).get("values");
      assertNotNull("Expected values in result", valuesNode2);
      assertEquals("Expected one value after remove_value", 1, valuesNode2.size());
      assertEquals("Expected value2 to remain", "value2", valuesNode2.get(0).asText());

    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}
