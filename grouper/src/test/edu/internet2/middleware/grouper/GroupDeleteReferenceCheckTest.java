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
package edu.internet2.middleware.grouper;

import java.util.HashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.exception.GrouperReferentialIntegrityException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * SECURITY: Group.delete() checks ADMIN before its scripted group / privacy realm reference check,
 * and names only the dependent scripted groups the caller can VIEW (GroupDeleteReferenceCheck).
 *
 * Before the fix a caller with only VIEW on a referenced group got GrouperReferentialIntegrityException
 * naming every scripted group that uses it (for example through WS groupDelete), and an ADMIN of the
 * group saw the names of scripted groups they cannot VIEW.
 *
 * Works on 6.1.0 and later (the reference check is new in 6.1.0): it makes real scripted groups and
 * runs the JEXL full sync so the dependency rows exist.
 */
public class GroupDeleteReferenceCheckTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupDeleteReferenceCheckTest("testDeleteChecksAdminAndHidesInvisibleDependents"));
  }

  /**
   * @param name
   */
  public GroupDeleteReferenceCheckTest(String name) {
    super(name);
  }

  /** referenced group */
  private static final String REF_NAME = "test181:ref";

  /** scripted group the admin of ref can VIEW */
  private static final String VISIBLE_NAME = "test181:scriptedVisible";

  /** scripted group the admin of ref cannot VIEW */
  private static final String HIDDEN_NAME = "test181:scriptedHidden";

  /**
   * A VIEW-only caller is refused with InsufficientPrivilegeException and learns no names; an ADMIN
   * sees only the dependent groups they can VIEW plus a count; root sees every name.
   */
  public void testDeleteChecksAdminAndHidesInvisibleDependents() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {

      // ---- setup as root: a referenced group and two scripted groups that use it
      Group ref = new GroupSave(rootSession).assignName(REF_NAME).assignCreateParentStemsIfNotExist(true).save();
      Group visible = new GroupSave(rootSession).assignName(VISIBLE_NAME).assignCreateParentStemsIfNotExist(true).save();
      Group hidden = new GroupSave(rootSession).assignName(HIDDEN_NAME).assignCreateParentStemsIfNotExist(true).save();

      ref.addMember(SubjectTestHelper.SUBJ2, false);

      // nobody sees these groups unless granted below
      Subject allSubject = SubjectFinder.findAllSubject();
      for (Group group : new Group[] {ref, visible, hidden}) {
        group.revokePriv(allSubject, AccessPrivilege.READ, false);
        group.revokePriv(allSubject, AccessPrivilege.VIEW, false);
      }

      // SUBJ0 can delete ref and can see one of the two scripted groups; SUBJ1 can only VIEW ref
      ref.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
      visible.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      ref.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW, false);

      AttributeDefName attributeDefNameMarker = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptMarker", true);
      AttributeDefName attributeDefNameScript = AttributeDefNameFinder.findByName("etc:attribute:abacJexlScript:grouperJexlScriptJexlScript", true);
      for (Group scripted : new Group[] {visible, hidden}) {
        AttributeAssign attributeAssign = new AttributeAssignSave(rootSession).assignOwnerGroup(scripted)
            .assignAttributeDefName(attributeDefNameMarker).save();
        attributeAssign.getAttributeValueDelegate().assignValueString(attributeDefNameScript.getName(), "entity.memberOf('" + REF_NAME + "')");
      }

      GrouperLoader.runOnceByJobName(rootSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_grouperLoaderJexlScriptFullSync");

      // precondition: the dependency view knows both scripted groups use ref
      Set<String> dependentNames = new HashSet<String>(GrouperUtil.nonNull(new GcDbAccess()
          .sql("select distinct depen_group_name from grouper_sql_dependency_group_v where owner_group_name = ?")
          .addBindVar(REF_NAME).selectList(String.class)));
      assertTrue("dependency rows missing: " + dependentNames, dependentNames.contains(VISIBLE_NAME));
      assertTrue("dependency rows missing: " + dependentNames, dependentNames.contains(HIDDEN_NAME));

    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    // ---- SUBJ1: VIEW only, refused by the ADMIN check, no names
    Exception viewOnlyException = deleteAs(SubjectTestHelper.SUBJ1);
    assertNotNull("VIEW-only caller must not delete the group", viewOnlyException);
    assertTrue("VIEW-only caller should get InsufficientPrivilegeException, not the reference check: " + viewOnlyException,
        viewOnlyException instanceof InsufficientPrivilegeException);
    assertFalse(String.valueOf(viewOnlyException.getMessage()).contains(VISIBLE_NAME));
    assertFalse(String.valueOf(viewOnlyException.getMessage()).contains(HIDDEN_NAME));

    // ---- SUBJ0: ADMIN of ref, sees the visible scripted group by name and a count of the hidden one
    Exception adminException = deleteAs(SubjectTestHelper.SUBJ0);
    assertTrue("admin should get the reference check: " + adminException,
        adminException instanceof GrouperReferentialIntegrityException);
    String adminMessage = adminException.getMessage();
    assertTrue(adminMessage, adminMessage.contains("scripted group '" + VISIBLE_NAME + "'"));
    assertFalse(adminMessage, adminMessage.contains(HIDDEN_NAME));
    assertTrue(adminMessage, adminMessage.contains("1 scripted group you cannot view"));

    // ---- root: every name, as before the fix
    Exception rootException = deleteAs(SubjectFinder.findRootSubject());
    assertTrue("root should get the reference check: " + rootException,
        rootException instanceof GrouperReferentialIntegrityException);
    String rootMessage = rootException.getMessage();
    assertTrue(rootMessage, rootMessage.contains("scripted group '" + VISIBLE_NAME + "'"));
    assertTrue(rootMessage, rootMessage.contains("scripted group '" + HIDDEN_NAME + "'"));
    assertFalse(rootMessage, rootMessage.contains("you cannot view"));

    // nothing was deleted
    rootSession = GrouperSession.startRootSession();
    try {
      assertNotNull(GroupFinder.findByName(rootSession, REF_NAME, false));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * Try to delete the referenced group in a session of the subject.
   * @param subject who deletes
   * @return the exception thrown, or null if the delete went through
   */
  private static Exception deleteAs(Subject subject) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      Group ref = GroupFinder.findByName(grouperSession, REF_NAME, true);
      ref.delete();
      return null;
    } catch (Exception e) {
      return e;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

}
