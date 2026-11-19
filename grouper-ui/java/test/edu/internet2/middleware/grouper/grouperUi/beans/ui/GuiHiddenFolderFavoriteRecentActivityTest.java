package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.lang.reflect.Method;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiStem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.userData.GrouperUserDataApi;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue uiHiddenFolderFavoriteRecentActivityAuthorization: My favorites, Recently used
 * and My activity kept showing a folder's live name, description and uuid after the user lost all
 * visibility of it.  GrouperUserDataApi.stemList used the unsecured stem DAO lookup (so revoked
 * folders were never dropped from the stored lists), and GuiAuditEntry.setupStem used
 * StemFinder.findByUuid (a lookup, not an authorization check).</p>
 *
 * <p>Setup: folder test:revoke97 (display ORIGINAL_97); GrouperAll has no naming privilege on it.
 * Each test grants the privileges it needs, adds the folder to the subject's favorites / recently
 * used as that subject would, then revokes and renames the folder as root.</p>
 *
 * <p>Each subject is only ever checked in one role: the stem view privilege table is computed per
 * subject on first use, so a grant made after a subject's first lookup is not seen until the
 * incremental job runs.</p>
 */
public class GuiHiddenFolderFavoriteRecentActivityTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GuiHiddenFolderFavoriteRecentActivityTest("testFavoriteAndRecentDroppedAfterRevoke"));
  }

  /**
   *
   */
  public GuiHiddenFolderFavoriteRecentActivityTest() {
    super();
  }

  /**
   * @param name
   */
  public GuiHiddenFolderFavoriteRecentActivityTest(String name) {
    super(name);
  }

  /** user data group for the test */
  private static final String USER_DATA_GROUP_NAME = "test:testUserData97";

  /** the folder */
  private Stem stem;

  /**
   * the folder, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.stem = new StemSave(grouperSession).assignName("test:revoke97").assignDisplayExtension("ORIGINAL_97")
          .assignCreateParentStemsIfNotExist(true).save();
      // nothing visible to everyone, whatever the create-grant-all config says
      this.stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_VIEW, false);
      this.stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.CREATE, false);
      this.stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_ATTR_READ, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // favorites and recently used
  // ---------------------------------------------------------------------------------------------

  /**
   * SUBJ0 had stemAdmin, favorited and visited the folder; after the revoke and a rename both lists
   * leave it out, and the stored lists no longer hold its uuid (still gone after a re-grant)
   */
  public void testFavoriteAndRecentDroppedAfterRevoke() {

    // given: stemAdmin, favorite and recently used
    asRoot(new Runnable() {
      public void run() {
        GuiHiddenFolderFavoriteRecentActivityTest.this.stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
      }
    });
    favoriteStemAdd(SubjectTestHelper.SUBJ0);
    recentlyUsedStemAdd(SubjectTestHelper.SUBJ0);
    assertTrue(containsStem(favoriteStems(SubjectTestHelper.SUBJ0)));
    assertTrue(containsStem(recentlyUsedStems(SubjectTestHelper.SUBJ0)));

    // when: revoke everything and rename
    revokeAndRename(SubjectTestHelper.SUBJ0);

    // then: gone from both
    Set<Stem> favorites = favoriteStems(SubjectTestHelper.SUBJ0);
    assertFalse(containsStem(favorites));
    assertFalse(String.valueOf(favorites), String.valueOf(favorites).contains("POST_REVOKE_97"));
    Set<Stem> recent = recentlyUsedStems(SubjectTestHelper.SUBJ0);
    assertFalse(containsStem(recent));

    // and the stored uuid was removed (re-granting does not bring it back)
    asRoot(new Runnable() {
      public void run() {
        GuiHiddenFolderFavoriteRecentActivityTest.this.stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
      }
    });
    assertFalse(containsStem(favoriteStems(SubjectTestHelper.SUBJ0)));
    assertFalse(containsStem(recentlyUsedStems(SubjectTestHelper.SUBJ0)));
  }

  /**
   * SUBJ1 has no naming privilege but can see a group inside the folder: the folder stays in the
   * lists (the same rule as the folder page)
   */
  public void testObjectInsideKeepsFavorite() {
    asRoot(new Runnable() {
      public void run() {
        Group inside = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:revoke97:inside").save();
        inside.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
        inside.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
        inside.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW, false);
      }
    });
    favoriteStemAdd(SubjectTestHelper.SUBJ1);
    recentlyUsedStemAdd(SubjectTestHelper.SUBJ1);

    assertTrue(containsStem(favoriteStems(SubjectTestHelper.SUBJ1)));
    assertTrue(containsStem(recentlyUsedStems(SubjectTestHelper.SUBJ1)));
  }

  /**
   * a deleted folder is still cleaned from the lists
   */
  public void testDeletedFolderCleaned() {
    asRoot(new Runnable() {
      public void run() {
        GuiHiddenFolderFavoriteRecentActivityTest.this.stem.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ADMIN, false);
      }
    });
    favoriteStemAdd(SubjectTestHelper.SUBJ2);
    asRoot(new Runnable() {
      public void run() {
        StemFinder.findByUuid(GrouperSession.staticGrouperSession(), GuiHiddenFolderFavoriteRecentActivityTest.this.stem.getUuid(), true).delete();
      }
    });
    assertFalse(containsStem(favoriteStems(SubjectTestHelper.SUBJ2)));
  }

  // ---------------------------------------------------------------------------------------------
  // my activity (audit lines)
  // ---------------------------------------------------------------------------------------------

  /**
   * SUBJ3 edited the folder (STEM_UPDATE row) and then lost access: the audit line does not get
   * the live folder.  SUBJ4 (still stemAdmin) and root do.
   */
  public void testAuditLineFolderHiddenAfterRevoke() {
    asRoot(new Runnable() {
      public void run() {
        GuiHiddenFolderFavoriteRecentActivityTest.this.stem.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.STEM_ADMIN, false);
        GuiHiddenFolderFavoriteRecentActivityTest.this.stem.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.STEM_ADMIN, false);
      }
    });
    revokeAndRename(SubjectTestHelper.SUBJ3);

    AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.STEM_UPDATE,
        "id", this.stem.getUuid(), "name", "test:revoke97", "displayName", "test:ORIGINAL_97");

    assertNull("revoked user sees no live folder", guiStemAs(SubjectTestHelper.SUBJ3, auditEntry));

    Stem stillAllowed = guiStemAs(SubjectTestHelper.SUBJ4, auditEntry);
    assertNotNull(stillAllowed);
    assertEquals("POST_REVOKE_97", stillAllowed.getDisplayExtension());

    assertNotNull("root", guiStemAs(SubjectFinder.findRootSubject(), auditEntry));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * revoke every naming privilege of a subject on the folder and give the folder a new display
   * name and description the subject never saw, as root
   * @param subject subject losing access
   */
  private void revokeAndRename(final Subject subject) {
    asRoot(new Runnable() {
      public void run() {
        Stem theStem = StemFinder.findByUuid(GrouperSession.staticGrouperSession(),
            GuiHiddenFolderFavoriteRecentActivityTest.this.stem.getUuid(), true);
        theStem.revokePriv(subject, NamingPrivilege.STEM_ADMIN, false);
        theStem.revokePriv(subject, NamingPrivilege.CREATE, false);
        theStem.revokePriv(subject, NamingPrivilege.STEM_VIEW, false);
        theStem.revokePriv(subject, NamingPrivilege.STEM_ATTR_READ, false);
        theStem.revokePriv(subject, NamingPrivilege.STEM_ATTR_UPDATE, false);
        theStem.setDisplayExtension("POST_REVOKE_97");
        theStem.setDescription("POST_REVOKE_DESCRIPTION_97");
        theStem.store();
      }
    });
  }

  /**
   * @param stems result of a user data call
   * @return true if the test folder is in it
   */
  private boolean containsStem(Set<Stem> stems) {
    if (stems != null) {
      for (Stem theStem : stems) {
        if (theStem != null && this.stem.getUuid().equals(theStem.getUuid())) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * build the GuiAuditEntry in a session as the viewer and run its folder setup (the step
   * getAuditLine runs for folder rows; getAuditLine itself needs the UI text container)
   * @param viewer user looking at the audit (e.g. My activity)
   * @param auditEntry row
   * @return the live folder the audit line would show, null if none
   */
  private static Stem guiStemAs(Subject viewer, AuditEntry auditEntry) {
    GrouperSession grouperSession = GrouperSession.start(viewer);
    try {
      GuiAuditEntry guiAuditEntry = new GuiAuditEntry(auditEntry);
      Method setupStem = GuiAuditEntry.class.getDeclaredMethod("setupStem");
      setupStem.setAccessible(true);
      setupStem.invoke(guiAuditEntry);
      GuiStem guiStem = guiAuditEntry.getGuiStem();
      return guiStem == null ? null : guiStem.getStem();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // user data calls, each in a session as the user (the UI calls them inside the logged-in user's
  // session; the API needs an open session to get its root session from)
  // ---------------------------------------------------------------------------------------------

  /**
   * favorite the test folder as the user
   * @param subject user
   */
  private void favoriteStemAdd(final Subject subject) {
    asUser(subject, new Runnable() {
      public void run() {
        GrouperUserDataApi.favoriteStemAdd(USER_DATA_GROUP_NAME, subject, GuiHiddenFolderFavoriteRecentActivityTest.this.stem);
      }
    });
  }

  /**
   * mark the test folder recently used as the user
   * @param subject user
   */
  private void recentlyUsedStemAdd(final Subject subject) {
    asUser(subject, new Runnable() {
      public void run() {
        GrouperUserDataApi.recentlyUsedStemAdd(USER_DATA_GROUP_NAME, subject, GuiHiddenFolderFavoriteRecentActivityTest.this.stem);
      }
    });
  }

  /**
   * @param subject user
   * @return the user's favorite folders, read as the user
   */
  private static Set<Stem> favoriteStems(final Subject subject) {
    final Set<Stem>[] result = new Set[1];
    asUser(subject, new Runnable() {
      public void run() {
        result[0] = GrouperUserDataApi.favoriteStems(USER_DATA_GROUP_NAME, subject);
      }
    });
    return result[0];
  }

  /**
   * @param subject user
   * @return the user's recently used folders, read as the user
   */
  private static Set<Stem> recentlyUsedStems(final Subject subject) {
    final Set<Stem>[] result = new Set[1];
    asUser(subject, new Runnable() {
      public void run() {
        result[0] = GrouperUserDataApi.recentlyUsedStems(USER_DATA_GROUP_NAME, subject);
      }
    });
    return result[0];
  }

  /**
   * run in a session as the user
   * @param subject user
   * @param runnable logic
   */
  private static void asUser(Subject subject, Runnable runnable) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      runnable.run();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * run as root
   * @param runnable logic
   */
  private static void asRoot(Runnable runnable) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      runnable.run();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
