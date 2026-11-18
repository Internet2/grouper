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
package edu.internet2.middleware.grouper.audit;

import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;

/**
 * Security advisory M2 (role and permission inheritance not audited): adding or removing a role
 * inheritance link, a permission name implication or an action implication changes who
 * effectively holds a permission, so each real change must write exactly one audit entry that
 * names both ends and the acting subject.  A call that changes nothing (link already there or
 * already gone) writes none.
 * <p>
 * The changes are made by SUBJ0, who is ADMIN on both roles and ATTR_ADMIN on the permission
 * definition (so the test also passes with the advisory M2 role inheritance privilege check).
 * GSH and API callers have no logged in member, so the acting subject is recorded as the act-as
 * member (GrouperContext.assignAuditEntryFields); the test accepts either column.
 * </p>
 * Before the fix there is no such audit type (does not compile) and no audit entry is written.
 */
public class PermissionInheritanceAuditTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionInheritanceAuditTest("testRoleInheritanceAudited"));
  }

  /**
   *
   */
  public PermissionInheritanceAuditTest() {
    super();
  }

  /**
   * @param name
   */
  public PermissionInheritanceAuditTest(String name) {
    super(name);
  }

  /** root session made in setUp */
  private GrouperSession rootSession;

  /** role that will inherit */
  private Role seniorRole;

  /** role that is inherited */
  private Role juniorRole;

  /** permission definition */
  private AttributeDef permissionDef;

  /** permission name that implies */
  private AttributeDefName allName;

  /** permission name that is implied */
  private AttributeDefName readName;

  /** member id of SUBJ0 */
  private String subj0MemberId;

  /**
   * make the roles, permission def, names and privileges as root
   */
  @Override
  public void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    Stem root = StemFinder.findRootStem(this.rootSession);
    Stem top = root.addChildStem("top", "top");

    this.seniorRole = top.addChildRole("seniorRole", "seniorRole");
    this.juniorRole = top.addChildRole("juniorRole", "juniorRole");
    for (Role role : new Role[] {this.seniorRole, this.juniorRole}) {
      ((Group)role).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    }

    this.permissionDef = new AttributeDefSave(this.rootSession).assignName("top:permissionDef")
        .assignAttributeDefType(AttributeDefType.perm).assignToEffMembership(true).assignToGroup(true).save();
    this.permissionDef.getAttributeDefActionDelegate().configureActionList("admin,read");
    this.permissionDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);

    this.allName = new AttributeDefNameSave(this.rootSession, this.permissionDef).assignName("top:all")
        .assignDisplayExtension("all").save();
    this.readName = new AttributeDefNameSave(this.rootSession, this.permissionDef).assignName("top:readThing")
        .assignDisplayExtension("readThing").save();

    this.subj0MemberId = MemberFinder.findBySubject(this.rootSession, SubjectTestHelper.SUBJ0, true).getUuid();
  }

  /**
   * stop sessions
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * find audit entries of a type whose string01 and string03 (or string03 and string05 for the
   * action type, which starts with the attribute def) match the two ends of the link
   * @param auditTypeBuiltin
   * @param ifHasId
   * @param thenHasId
   * @return the entries
   */
  private List<AuditEntry> auditEntries(AuditTypeBuiltin auditTypeBuiltin, String ifHasId, String thenHasId) {
    boolean actionType = auditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_DEF_ACTION_INHERITANCE_ADD
        || auditTypeBuiltin == AuditTypeBuiltin.ATTRIBUTE_DEF_ACTION_INHERITANCE_DELETE;
    List<AuditEntry> all = HibernateSession.byHqlStatic()
        .createQuery("from AuditEntry where auditTypeId = :theAuditTypeId")
        .setString("theAuditTypeId", auditTypeBuiltin.getAuditType().getId()).list(AuditEntry.class);
    List<AuditEntry> result = new ArrayList<AuditEntry>();
    for (AuditEntry auditEntry : all) {
      String ifHas = actionType ? auditEntry.getString03() : auditEntry.getString01();
      String thenHas = actionType ? auditEntry.getString05() : auditEntry.getString03();
      if (ifHasId.equals(ifHas) && thenHasId.equals(thenHas)) {
        result.add(auditEntry);
      }
    }
    return result;
  }

  /**
   * assert one entry, made by SUBJ0, with a description naming both ends
   * @param entries
   * @param ifHasName
   * @param thenHasName
   */
  private void assertOneBySubj0(List<AuditEntry> entries, String ifHasName, String thenHasName) {
    assertEquals("exactly one audit entry", 1, entries.size());
    AuditEntry auditEntry = entries.get(0);
    assertTrue("acting member is SUBJ0: loggedIn " + auditEntry.getLoggedInMemberId()
        + ", actAs " + auditEntry.getActAsMemberId(),
        this.subj0MemberId.equals(auditEntry.getLoggedInMemberId())
        || this.subj0MemberId.equals(auditEntry.getActAsMemberId()));
    assertTrue(auditEntry.getDescription(), auditEntry.getDescription().contains(ifHasName));
    assertTrue(auditEntry.getDescription(), auditEntry.getDescription().contains(thenHasName));
  }

  // ---------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------

  /**
   * role inheritance add and remove each write one audit entry; a repeat writes none
   */
  public void testRoleInheritanceAudited() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertTrue(this.seniorRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.juniorRole));
      // already there: no change, no second entry
      assertFalse(this.seniorRole.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.juniorRole));
      assertOneBySubj0(auditEntries(AuditTypeBuiltin.ROLE_INHERITANCE_ADD,
          this.seniorRole.getId(), this.juniorRole.getId()), this.seniorRole.getName(), this.juniorRole.getName());

      assertTrue(this.seniorRole.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(this.juniorRole));
      assertFalse(this.seniorRole.getRoleInheritanceDelegate().removeRoleFromInheritFromThis(this.juniorRole));
      assertOneBySubj0(auditEntries(AuditTypeBuiltin.ROLE_INHERITANCE_DELETE,
          this.seniorRole.getId(), this.juniorRole.getId()), this.seniorRole.getName(), this.juniorRole.getName());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * permission name implication add and remove each write one audit entry; a repeat writes none
   */
  public void testAttributeDefNameInheritanceAudited() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertTrue(this.allName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.readName));
      assertFalse(this.allName.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.readName));
      assertOneBySubj0(auditEntries(AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_INHERITANCE_ADD,
          this.allName.getId(), this.readName.getId()), this.allName.getName(), this.readName.getName());

      assertTrue(this.allName.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(this.readName));
      assertFalse(this.allName.getAttributeDefNameSetDelegate().removeFromAttributeDefNameSet(this.readName));
      assertOneBySubj0(auditEntries(AuditTypeBuiltin.ATTRIBUTE_DEF_NAME_INHERITANCE_DELETE,
          this.allName.getId(), this.readName.getId()), this.allName.getName(), this.readName.getName());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * action implication add and remove each write one audit entry; a repeat writes none
   */
  public void testActionInheritanceAudited() {
    AttributeAssignAction admin = this.permissionDef.getAttributeDefActionDelegate().findAction("admin", true);
    AttributeAssignAction read = this.permissionDef.getAttributeDefActionDelegate().findAction("read", true);
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertTrue(admin.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(read));
      assertFalse(admin.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(read));
      List<AuditEntry> added = auditEntries(AuditTypeBuiltin.ATTRIBUTE_DEF_ACTION_INHERITANCE_ADD,
          admin.getId(), read.getId());
      assertOneBySubj0(added, "admin", "read");
      // the entry names the attribute def, since action names are only unique within it
      assertEquals(this.permissionDef.getId(), added.get(0).getString01());

      assertTrue(admin.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(read));
      assertFalse(admin.getAttributeAssignActionSetDelegate().removeFromAttributeAssignActionSet(read));
      assertOneBySubj0(auditEntries(AuditTypeBuiltin.ATTRIBUTE_DEF_ACTION_INHERITANCE_DELETE,
          admin.getId(), read.getId()), "admin", "read");
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

}
