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

import edu.internet2.middleware.grouper.exception.GroupNotFoundException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import junit.textui.TestRunner;

/**
 * SECURITY tests for CompositeSave.save() run by a non-wheel caller.
 *
 * CompositeSave checked the caller's privileges late: the DELETE save mode deleted the composite
 * through the DAO with no privilege check at all, and an update deleted the old composite (and, from
 * 7.4.0, an insert deleted the group's immediate members) before internal_addCompositeMember checked
 * READ on the factors.  The save's own transaction rolls the late failure back, but when the caller
 * already has a transaction open (CompositeSave joins it) and catches the exception, the outer commit
 * keeps the deletes.  The fix checks UPDATE on the owner and READ on both factors before anything is
 * written.
 *
 * Only API that exists in every patched release is used (2.6.19 has no assignOwnerGroup /
 * assignCompositeType on CompositeSave), so the same file compiles on 2.6.19 through 7.x.
 */
public class CompositeSaveDeniedChangesNothingTest extends GrouperTest {

  /** folder for this test's groups */
  private static final String FOLDER = "test:compSave183";

  /** owner (the composite group) */
  private static final String OWNER = FOLDER + ":owner";

  /** left factor, readable by everyone (GrouperTest grants GrouperAll READ and VIEW on create) */
  private static final String LEFT = FOLDER + ":left";

  /** right factor, GrouperAll READ revoked so SUBJ0 can VIEW but not READ it */
  private static final String RIGHT = FOLDER + ":right";

  /** a group SUBJ0 cannot VIEW */
  private static final String PRIVATE = FOLDER + ":private";

  /**
   *
   */
  public CompositeSaveDeniedChangesNothingTest() {
    super();
  }

  /**
   * @param name
   */
  public CompositeSaveDeniedChangesNothingTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new CompositeSaveDeniedChangesNothingTest("testDeleteModeNeedsUpdate"));
  }

  // ------------------------------------------------------------------------------------------
  // setup helpers
  // ------------------------------------------------------------------------------------------

  /**
   * As root, create owner, left, right (READ revoked from GrouperAll) and private (READ and VIEW
   * revoked from GrouperAll).
   * @param ownerIsComposite if true, make owner = left INTERSECTION right
   * @param subj0OwnerPriv privilege to grant SUBJ0 on owner, or null for none (SUBJ0 can still
   * VIEW and READ it through GrouperAll)
   */
  private void setupGroups(boolean ownerIsComposite, Privilege subj0OwnerPriv) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group owner = new GroupSave(rootSession).assignName(OWNER).assignCreateParentStemsIfNotExist(true).save();
      Group left = new GroupSave(rootSession).assignName(LEFT).assignCreateParentStemsIfNotExist(true).save();
      Group right = new GroupSave(rootSession).assignName(RIGHT).assignCreateParentStemsIfNotExist(true).save();
      Group privateGroup = new GroupSave(rootSession).assignName(PRIVATE).assignCreateParentStemsIfNotExist(true).save();

      left.addMember(SubjectTestHelper.SUBJ2, false);
      right.addMember(SubjectTestHelper.SUBJ2, false);

      // SUBJ0 can find (VIEW) the right factor but cannot READ it
      right.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      privateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      privateGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);

      if (subj0OwnerPriv != null) {
        owner.grantPriv(SubjectTestHelper.SUBJ0, subj0OwnerPriv, false);
      }
      if (ownerIsComposite) {
        owner.addCompositeMember(CompositeType.INTERSECTION, left, right);
      } else {
        // an immediate member that is in neither factor (7.4.0+ inserts delete such members first)
        owner.addMember(SubjectTestHelper.SUBJ1, false);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * Read the owner's composite back as root from a fresh lookup.
   * @return the composite or null
   */
  private static Composite ownerComposite() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group owner = GroupFinder.findByName(rootSession, OWNER, true);
      return owner.isHasComposite() ? owner.getComposite(true) : null;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @return true if SUBJ1 is still an immediate member of the owner (read as root)
   */
  private static boolean subj1StillImmediateMember() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return GroupFinder.findByName(rootSession, OWNER, true).hasImmediateMember(SubjectTestHelper.SUBJ1);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @param throwable
   * @param theClass
   * @return true if the throwable or one of its causes is of that class
   */
  private static boolean causedBy(Throwable throwable, Class<?> theClass) {
    for (Throwable t = throwable; t != null; t = t.getCause()) {
      if (theClass.isInstance(t)) {
        return true;
      }
      if (t.getCause() == t) {
        break;
      }
    }
    return false;
  }

  /**
   * As SUBJ0, run the save inside an outer transaction that catches the failure and commits anyway,
   * the way a caller with its own transaction would.  CompositeSave joins that transaction
   * (READ_WRITE_OR_USE_EXISTING), so anything it wrote before failing is committed.
   * @param compositeSave the save to run
   * @return the exception the save threw, or null if it succeeded
   */
  private static RuntimeException saveAsSubj0InOuterTransaction(final CompositeSave compositeSave) {
    final RuntimeException[] thrown = new RuntimeException[1];
    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GrouperTransaction.callbackGrouperTransaction(GrouperTransactionType.READ_WRITE_NEW, new GrouperTransactionHandler() {

        public Object callback(GrouperTransaction grouperTransaction) throws GrouperDAOException {
          try {
            compositeSave.save();
          } catch (RuntimeException re) {
            // swallow it so the outer transaction commits
            thrown[0] = re;
          }
          return null;
        }
      });
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
    return thrown[0];
  }

  // ------------------------------------------------------------------------------------------
  // tests
  // ------------------------------------------------------------------------------------------

  /**
   * DELETE mode deleted the composite through the DAO with no privilege check: a caller who can
   * only VIEW/READ the owner removed its composite.  It must need UPDATE on the owner.
   */
  public void testDeleteModeNeedsUpdate() {

    setupGroups(true, null);
    assertNotNull("precondition: owner is a composite", ownerComposite());

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    RuntimeException thrown = null;
    try {
      new CompositeSave().assignOwnerName(OWNER).assignLeftFactorName(LEFT).assignRightFactorName(RIGHT)
          .assignSaveMode(SaveMode.DELETE).save();
    } catch (RuntimeException re) {
      thrown = re;
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }

    assertNotNull("SUBJ0 without UPDATE on the owner must not delete its composite", thrown);
    assertTrue("expected InsufficientPrivilegeException: " + thrown, causedBy(thrown, InsufficientPrivilegeException.class));
    assertNotNull("composite must still exist", ownerComposite());
  }

  /**
   * Update with an unreadable factor: the old composite was deleted before the READ check.  Inside
   * an outer transaction that commits, that delete was kept.
   */
  public void testDeniedUpdateKeepsComposite() {

    setupGroups(true, AccessPrivilege.UPDATE);
    Composite before = ownerComposite();
    assertNotNull("precondition: owner is a composite", before);

    RuntimeException thrown = saveAsSubj0InOuterTransaction(new CompositeSave().assignOwnerName(OWNER)
        .assignLeftFactorName(LEFT).assignRightFactorName(RIGHT).assignType("union"));

    assertNotNull("SUBJ0 cannot READ the right factor, the save must fail", thrown);
    assertTrue("expected InsufficientPrivilegeException: " + thrown, causedBy(thrown, InsufficientPrivilegeException.class));

    Composite after = ownerComposite();
    assertNotNull("the denied save must not delete the existing composite", after);
    assertEquals(before.getUuid(), after.getUuid());
    assertEquals(CompositeType.INTERSECTION, after.getType());
    assertEquals(before.getLeftFactorUuid(), after.getLeftFactorUuid());
    assertEquals(before.getRightFactorUuid(), after.getRightFactorUuid());
  }

  /**
   * Insert with an unreadable factor on a group with an immediate member: from 7.4.0 the save deleted
   * the members that are not in the composite result before the READ check.  The member must stay.
   */
  public void testDeniedInsertKeepsMembers() {

    setupGroups(false, AccessPrivilege.UPDATE);
    assertTrue("precondition: SUBJ1 is an immediate member", subj1StillImmediateMember());

    RuntimeException thrown = saveAsSubj0InOuterTransaction(new CompositeSave().assignOwnerName(OWNER)
        .assignLeftFactorName(LEFT).assignRightFactorName(RIGHT).assignType("union"));

    assertNotNull("SUBJ0 cannot READ the right factor, the save must fail", thrown);
    assertTrue("expected InsufficientPrivilegeException: " + thrown, causedBy(thrown, InsufficientPrivilegeException.class));
    assertTrue("the denied save must not remove the existing member", subj1StillImmediateMember());
    assertNull("no composite after a denied insert", ownerComposite());
  }

  /**
   * The lookups run as the caller (not root): a group the caller cannot VIEW is "not found", the
   * same as a group that does not exist.  Guards the report's claim, which is not true of any
   * release.
   */
  public void testUnviewableFactorSameAsMissing() {

    setupGroups(true, AccessPrivilege.UPDATE);

    String[] factors = new String[] {PRIVATE, FOLDER + ":doesNotExist"};
    for (String factor : factors) {
      RuntimeException thrown = saveAsSubj0InOuterTransaction(new CompositeSave().assignOwnerName(OWNER)
          .assignLeftFactorName(LEFT).assignRightFactorName(factor).assignType("union"));
      assertNotNull(factor + ": must fail", thrown);
      assertTrue(factor + ": expected GroupNotFoundException: " + thrown, causedBy(thrown, GroupNotFoundException.class));
    }
    assertNotNull("composite must still exist", ownerComposite());
  }

  /**
   * Controls: a caller with UPDATE on the owner and READ on both factors can still update and delete,
   * and root is unchanged.
   */
  public void testAllowedSavesStillWork() {

    setupGroups(true, AccessPrivilege.UPDATE);

    // SUBJ0 may READ the right factor now
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      GroupFinder.findByName(rootSession, RIGHT, true).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      CompositeSave compositeSave = new CompositeSave().assignOwnerName(OWNER).assignLeftFactorName(LEFT)
          .assignRightFactorName(RIGHT).assignType("union");
      compositeSave.save();
      assertEquals(SaveResultType.UPDATE, compositeSave.getSaveResultType());

      // no change needs no privilege check to pass, but SUBJ0 has them anyway
      compositeSave = new CompositeSave().assignOwnerName(OWNER).assignLeftFactorName(LEFT)
          .assignRightFactorName(RIGHT).assignType("union");
      compositeSave.save();
      assertEquals(SaveResultType.NO_CHANGE, compositeSave.getSaveResultType());
    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }
    assertEquals(CompositeType.UNION, ownerComposite().getType());

    // root deletes as before
    rootSession = GrouperSession.startRootSession();
    try {
      CompositeSave compositeSave = new CompositeSave().assignOwnerName(OWNER).assignLeftFactorName(LEFT)
          .assignRightFactorName(RIGHT).assignSaveMode(SaveMode.DELETE);
      compositeSave.save();
      assertEquals(SaveResultType.DELETE, compositeSave.getSaveResultType());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertNull("root delete removes the composite", ownerComposite());
  }
}
