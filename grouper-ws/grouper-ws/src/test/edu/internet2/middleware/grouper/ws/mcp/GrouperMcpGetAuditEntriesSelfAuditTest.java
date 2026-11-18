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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.grouper.ws.util.RestClientSettings;
import junit.textui.TestRunner;

/**
 * Security issue 07 (mcpAuditSelfHiddenGroups): audit_get with a subject filter uses the UI's
 * rule for every caller, the caller's own subject included: a sysadmin, a readonly sysadmin, or
 * a member of the group in uiV2.subject.seeAudits.must.be.in.group.  The "actions performed by
 * me" filter is unchanged.
 *
 * <p>Before the fix test.subject.0 reads audit entries about themselves, including a group they
 * cannot see.</p>
 */
public class GrouperMcpGetAuditEntriesSelfAuditTest extends GrouperTest {

  /** json */
  private static final ObjectMapper objectMapper = new ObjectMapper();

  /** grouper version */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** a group test.subject.0 cannot see */
  private static final String HIDDEN_GROUP = "test:mcpSelfAudit:hidden";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperMcpGetAuditEntriesSelfAuditTest("testSelfAuditDeniedByDefault"));
  }

  /**
   * @param name
   */
  public GrouperMcpGetAuditEntriesSelfAuditTest(String name) {
    super(name);
  }

  /**
   * @see junit.framework.TestCase#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    RestClientSettings.resetData();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpGetAuditEntries.SEE_SUBJECT_AUDITS_GROUP_CONFIG, "");

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.MCP, false, false);

    // SUBJ0 is added to, then removed from, a group they cannot see: audit entries about SUBJ0
    Group hidden = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName(HIDDEN_GROUP).assignCreateParentStemsIfNotExist(true).save();
    hidden.addMember(SubjectTestHelper.SUBJ0, false);
    hidden.deleteMember(SubjectTestHelper.SUBJ0, false);
  }

  /**
   * @see junit.framework.TestCase#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap().remove(
        GrouperMcpGetAuditEntries.SEE_SUBJECT_AUDITS_GROUP_CONFIG);
    super.tearDown();
    GrouperContext.deleteDefaultContext();
  }

  /**
   * audit_get about a subject, as SUBJ0
   * @param subjectId the subject filter
   * @return result
   */
  private static ObjectNode auditAboutSubjectAsSubj0(String subjectId) {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("subjectIdOrIdentifier", subjectId);
    arguments.put("auditType", "membership");
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      return GrouperMcpGetAuditEntries.execute(arguments, new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * the reproduction: SUBJ0 asking about themselves is refused, and the hidden group is not
   * named.  Before the fix the entries for the hidden group come back
   */
  public void testSelfAuditDeniedByDefault() {
    ObjectNode result = auditAboutSubjectAsSubj0(SubjectTestHelper.SUBJ0_ID);
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertTrue(result.toString(), result.toString().contains("Access denied"));
    assertFalse(result.toString(), result.toString().contains(HIDDEN_GROUP));
  }

  /**
   * a denied caller cannot probe which subjects exist: the access check comes first
   */
  public void testDeniedBeforeSubjectLookup() {
    ObjectNode result = auditAboutSubjectAsSubj0("noSuchSubjectAnywhere");
    assertTrue(result.toString(), result.get("isError").asBoolean());
    assertTrue(result.toString(), result.toString().contains("Access denied"));
    assertFalse(result.toString(), result.toString().contains("Subject not found"));
  }

  /**
   * a member of the configured see-audits group may read audits about a subject, as in the UI
   */
  public void testSeeAuditsGroupMemberAllowed() {
    Group seeAudits = new GroupSave(GrouperSession.staticGrouperSession()).assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignName("test:mcpSelfAudit:seeAudits").assignCreateParentStemsIfNotExist(true).save();
    seeAudits.addMember(SubjectTestHelper.SUBJ0, false);
    GrouperUiConfigInApi.retrieveConfig().propertiesOverrideMap().put(
        GrouperMcpGetAuditEntries.SEE_SUBJECT_AUDITS_GROUP_CONFIG, seeAudits.getName());

    ObjectNode result = auditAboutSubjectAsSubj0(SubjectTestHelper.SUBJ0_ID);
    assertFalse(result.toString(), result.get("isError").asBoolean());
  }

  /**
   * a sysadmin may read audits about any subject
   */
  public void testRootAllowed() {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("subjectIdOrIdentifier", SubjectTestHelper.SUBJ0_ID);
    ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments,
        new GrouperMcpAuthUser(SubjectFinder.findRootSubject()));
    assertFalse(result.toString(), result.get("isError").asBoolean());
  }

  /**
   * "actions performed by me" is unchanged: still allowed for the caller themselves
   */
  public void testActionsPerformedBySelfStillAllowed() {
    ObjectNode arguments = objectMapper.createObjectNode();
    arguments.put("actionsPerformedBySubjectIdOrIdentifier", SubjectTestHelper.SUBJ0_ID);
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      ObjectNode result = GrouperMcpGetAuditEntries.execute(arguments, new GrouperMcpAuthUser(SubjectTestHelper.SUBJ0));
      assertFalse(result.toString(), result.get("isError").asBoolean());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }
}
