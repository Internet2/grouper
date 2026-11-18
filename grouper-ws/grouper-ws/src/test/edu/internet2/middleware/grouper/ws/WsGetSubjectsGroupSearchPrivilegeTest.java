/**
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
 */
package edu.internet2.middleware.grouper.ws;

import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetSubjectsResults;
import edu.internet2.middleware.grouper.ws.coresoap.WsGetSubjectsResults.WsGetSubjectsResultsCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsGroupLookup;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubject;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory H5 (wsGetSubjectsGroupSearchPrivilegeDisclosure).  WS getSubjects with a
 * searchString and a group lookup found the group with VIEW and then listed the subjects in
 * the caller-chosen field straight from the membership DAO, with no privilege check.  So a
 * caller who could only VIEW the group got its members (and with fieldName=admins, its
 * admins).
 *
 * The fix (WsGetSubjectsGroupSearchSecurity, called from GrouperServiceLogic.getSubjects)
 * requires a group list or privilege field and its read privilege before the search:
 * READ for members, ADMIN for admins.  A denial is INSUFFICIENT_PRIVILEGES with no subjects;
 * a field that is not a group field is INVALID_QUERY.
 *
 * Fails before the fix: SUBJ0 (VIEW only) gets SUBJ1 and SUBJ3 back with SUCCESS, and SUBJ2
 * (READ) gets the admins.  Java 8 syntax only (the same file ships for every release).
 */
public class WsGetSubjectsGroupSearchPrivilegeTest extends GrouperTest {

  /** group under test */
  private static final String GROUP_NAME = "test:getSubjectsAuthz:protected";

  /** free form search that matches the jdbc test subjects (test.subject.N) */
  private static final String SEARCH = "test.";

  /** grouper version for the service logic calls */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsGetSubjectsGroupSearchPrivilegeTest("testViewOnlyCannotSearchMembers"));
  }

  /**
   *
   */
  public WsGetSubjectsGroupSearchPrivilegeTest() {
    super();
  }

  /**
   * @param name
   */
  public WsGetSubjectsGroupSearchPrivilegeTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    // so the test subjects only have the privileges granted below
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperServiceUtils.testSession = null;
    super.tearDown();
  }

  // ---------------------------------------------------------------- setup and helpers

  /**
   * as root: the group with SUBJ1 and SUBJ3 as members; SUBJ0 VIEW only, SUBJ2 VIEW and READ,
   * SUBJ4 ADMIN (so SUBJ4 is the admin to find in the admins list)
   */
  private void setupGroup() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(rootSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      group.addMember(SubjectTestHelper.SUBJ1, false);
      group.addMember(SubjectTestHelper.SUBJ3, false);
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.VIEW, false);
      group.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
      group.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.ADMIN, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * run WS getSubjects (full form) with the search string and the group, as a caller
   * @param caller subject of the WS session, null for root
   * @param field null for the default members list
   * @return the results
   */
  private static WsGetSubjectsResults searchAs(Subject caller, Field field) {
    GrouperServiceUtils.testSession = caller == null ? GrouperSession.startRootSession() : GrouperSession.start(caller);
    try {
      return GrouperServiceLogic.getSubjects(GROUPER_VERSION, null, SEARCH, false, null, null,
          null, new WsGroupLookup(GROUP_NAME, null), null, field, false, null);
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      GrouperServiceUtils.testSession = null;
    }
  }

  /**
   * run WS getSubjectsLite with the search string and the group name, as a caller
   * @param caller subject of the WS session
   * @param field null for the default members list
   * @return the results
   */
  private static WsGetSubjectsResults searchLiteAs(Subject caller, Field field) {
    GrouperServiceUtils.testSession = GrouperSession.start(caller);
    try {
      return GrouperServiceLogic.getSubjectsLite(GROUPER_VERSION, null, null, null, SEARCH, false, null,
          null, null, null, null, GROUP_NAME, null, null, field, false, null, null, null, null);
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      GrouperServiceUtils.testSession = null;
    }
  }

  /**
   * @param results
   * @return the subject ids in the results, empty if none
   */
  private static Set<String> subjectIds(WsGetSubjectsResults results) {
    Set<String> ids = new HashSet<String>();
    if (results.getWsSubjects() != null) {
      for (WsSubject wsSubject : results.getWsSubjects()) {
        ids.add(wsSubject.getId());
      }
    }
    return ids;
  }

  /**
   * a denial: the given result code, not success, and no subject at all
   * @param label for messages
   * @param results
   * @param code expected result code
   */
  private static void assertRefused(String label, WsGetSubjectsResults results, WsGetSubjectsResultsCode code) {
    String message = label + ": " + results.getResultMetadata().getResultCode() + ", "
        + results.getResultMetadata().getResultMessage() + ", subjects: " + subjectIds(results);
    assertEquals(message, code.name(), results.getResultMetadata().getResultCode());
    assertEquals(message, "F", results.getResultMetadata().getSuccess());
    assertEquals(message, 0, subjectIds(results).size());
  }

  /**
   * a success with exactly these subjects
   * @param label for messages
   * @param results
   * @param expected subjects that must be returned, and no others
   */
  private static void assertFound(String label, WsGetSubjectsResults results, Subject... expected) {
    Set<String> expectedIds = new HashSet<String>();
    for (Subject subject : expected) {
      expectedIds.add(subject.getId());
    }
    String message = label + ": " + results.getResultMetadata().getResultCode() + ", "
        + results.getResultMetadata().getResultMessage();
    assertEquals(message, WsGetSubjectsResultsCode.SUCCESS.name(), results.getResultMetadata().getResultCode());
    assertEquals(message, expectedIds, subjectIds(results));
  }

  // ---------------------------------------------------------------- tests

  /**
   * VIEW without READ: the members search is refused (full and lite form); READ and root get
   * the members
   */
  public void testViewOnlyCannotSearchMembers() {
    setupGroup();

    // the reported attack: VIEW only, default members list
    assertRefused("VIEW only, full", searchAs(SubjectTestHelper.SUBJ0, null),
        WsGetSubjectsResultsCode.INSUFFICIENT_PRIVILEGES);
    assertRefused("VIEW only, lite", searchLiteAs(SubjectTestHelper.SUBJ0, null),
        WsGetSubjectsResultsCode.INSUFFICIENT_PRIVILEGES);
    // naming the members field explicitly is the same
    assertRefused("VIEW only, members field", searchAs(SubjectTestHelper.SUBJ0, Group.getDefaultList()),
        WsGetSubjectsResultsCode.INSUFFICIENT_PRIVILEGES);

    // READ is the privilege for the members list
    assertFound("READ, full", searchAs(SubjectTestHelper.SUBJ2, null), SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ3);
    assertFound("READ, lite", searchLiteAs(SubjectTestHelper.SUBJ2, null), SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ3);
    // ADMIN implies READ
    assertFound("ADMIN, members", searchAs(SubjectTestHelper.SUBJ4, null), SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ3);
    assertFound("root, members", searchAs(null, null), SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ3);
  }

  /**
   * a privilege list needs its own read privilege (ADMIN for admins): READ is refused, ADMIN
   * and root get the admins
   * @throws Exception
   */
  public void testPrivilegeFieldNeedsItsReadPriv() throws Exception {
    setupGroup();
    Field admins = AccessPrivilege.ADMIN.getField();

    assertRefused("VIEW only, admins", searchAs(SubjectTestHelper.SUBJ0, admins),
        WsGetSubjectsResultsCode.INSUFFICIENT_PRIVILEGES);
    assertRefused("READ, admins", searchAs(SubjectTestHelper.SUBJ2, admins),
        WsGetSubjectsResultsCode.INSUFFICIENT_PRIVILEGES);
    assertRefused("READ, admins, lite", searchLiteAs(SubjectTestHelper.SUBJ2, admins),
        WsGetSubjectsResultsCode.INSUFFICIENT_PRIVILEGES);

    // the jdbc subjects that hold ADMIN (the search only matches test.subject.N)
    assertFound("ADMIN, admins", searchAs(SubjectTestHelper.SUBJ4, admins), SubjectTestHelper.SUBJ4);
    assertFound("root, admins", searchAs(null, admins), SubjectTestHelper.SUBJ4);
  }

  /**
   * a field that is not a group list or privilege field (a folder privilege field) is refused,
   * even for root
   * @throws Exception
   */
  public void testNonGroupFieldRefused() throws Exception {
    setupGroup();
    Field creators = NamingPrivilege.CREATE.getField();

    assertRefused("root, folder field", searchAs(null, creators), WsGetSubjectsResultsCode.INVALID_QUERY);
    assertRefused("READ, folder field", searchAs(SubjectTestHelper.SUBJ2, creators), WsGetSubjectsResultsCode.INVALID_QUERY);
  }

}
