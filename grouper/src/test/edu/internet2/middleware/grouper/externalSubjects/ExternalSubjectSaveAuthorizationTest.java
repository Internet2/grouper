/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.externalSubjects;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.misc.SaveResultType;

/**
 * <pre>
 * ExternalSubjectSave.save() must check that the caller can edit external subjects before it
 * reads or compares anything.  Before the fix the only check was in ExternalSubject.store(),
 * which runs only when a field changed, so a non-editor who sent the stored values unchanged
 * got the whole record back (WS externalSubjectSave returned SUCCESS_NO_CHANGES_NEEDED with
 * every field), and a wrong uuid leaked the stored uuid in the error message.
 *
 * Setup: as root, record noop@example.org with name, email and institution;
 * externalSubjects.groupAllowedForEdit = etc:externalEditors, which has SUBJ1 (not SUBJ0).
 *
 * Same file for every release 5.0.0 - 7.x (no commons-lang import, API module only; the WS
 * layer maps InsufficientPrivilegeException to INSUFFICIENT_PRIVILEGES with no object).
 * </pre>
 */
public class ExternalSubjectSaveAuthorizationTest extends GrouperTest {

  /** identifier of the protected record */
  private static final String IDENTIFIER = "noop@example.org";

  /** stored name */
  private static final String NAME = "Protected Noop Person";

  /** stored email */
  private static final String EMAIL = "noop-private@example.net";

  /** stored institution */
  private static final String INSTITUTION = "Private Noop Institution";

  /** group of allowed editors */
  private static final String EDITORS_GROUP = "etc:externalEditors";

  /** uuid of the protected record */
  private String storedUuid = null;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(ExternalSubjectSaveAuthorizationTest.class);
  }

  /**
   *
   */
  public ExternalSubjectSaveAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public ExternalSubjectSaveAuthorizationTest(String name) {
    super(name);
  }

  /**
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();

    // same external subject config as the other external subject tests
    ExternalSubjectTest.setupHelper();
    // external subjects are off by default (externalSubjects.enabled, 2026-11-08); this test writes them
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.enabled", "true");

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      // editors group with SUBJ1; SUBJ0 is an ordinary authenticated subject
      Group editors = new GroupSave(rootSession).assignName(EDITORS_GROUP)
          .assignCreateParentStemsIfNotExist(true).save();
      editors.addMember(SubjectTestHelper.SUBJ1, false);

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.wheelOrRootCanEdit", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.groupAllowedForEdit", EDITORS_GROUP);
      ExternalSubjectConfig.clearCache();

      ExternalSubject externalSubject = newSave(rootSession).save();
      this.storedUuid = externalSubject.getUuid();
      assertNotNull(this.storedUuid);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.groupAllowedForEdit");
    ExternalSubjectConfig.clearCache();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.enabled");
    super.tearDown();
  }

  /**
   * a save with exactly the stored values
   * @param grouperSession
   * @return the save
   */
  private static ExternalSubjectSave newSave(GrouperSession grouperSession) {
    return new ExternalSubjectSave(grouperSession).assignIdentifier(IDENTIFIER)
        .assignName(NAME).assignEmail(EMAIL).assignInstitution(INSTITUTION).assignEnabled(true);
  }

  /**
   * read the protected record as root
   * @param identifier
   * @return the record or null
   */
  private static ExternalSubject findAsRoot(String identifier) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return ExternalSubjectStorageController.findByIdentifier(identifier, false, null);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * run the save and expect InsufficientPrivilegeException (possibly wrapped), never the record
   * @param externalSubjectSave
   * @return the exception text (message and causes)
   */
  private static String assertDenied(ExternalSubjectSave externalSubjectSave) {
    ExternalSubject result = null;
    try {
      result = externalSubjectSave.save();
    } catch (RuntimeException re) {
      StringBuilder text = new StringBuilder();
      boolean foundInsufficientPrivilege = false;
      for (Throwable t = re; t != null; t = t.getCause()) {
        text.append(t.toString()).append("\n");
        if (t instanceof InsufficientPrivilegeException) {
          foundInsufficientPrivilege = true;
        }
        if (t.getCause() == t) {
          break;
        }
      }
      assertTrue("expected InsufficientPrivilegeException: " + text, foundInsufficientPrivilege);
      return text.toString();
    }
    fail("non-editor should be denied, got record: " + (result == null ? null : result.getUuid()));
    return null;
  }

  /**
   * a non-editor sending the stored values unchanged is denied (before the fix: NO_CHANGE and the record)
   */
  public void testNoopDeniedForNonEditor() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertDenied(newSave(session));
      // with the uuid too
      assertDenied(newSave(session).assignUuid(this.storedUuid));
      // update mode
      assertDenied(newSave(session).assignSaveMode(SaveMode.UPDATE));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * a non-editor changing a value is denied and nothing changes
   */
  public void testChangeDeniedForNonEditor() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertDenied(newSave(session).assignName("Changed Name"));
    } finally {
      GrouperSession.stopQuietly(session);
    }
    ExternalSubject stored = findAsRoot(IDENTIFIER);
    assertNotNull(stored);
    assertEquals(NAME, stored.getName());
  }

  /**
   * a wrong uuid must not leak the stored uuid to a non-editor
   */
  public void testWrongUuidDoesNotLeakStoredUuid() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      String text = assertDenied(newSave(session).assignUuid("00000000000000000000000000000000"));
      assertFalse("stored uuid leaked: " + text, text.contains(this.storedUuid));
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * a non-editor cannot insert
   */
  public void testInsertDeniedForNonEditor() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertDenied(new ExternalSubjectSave(session).assignIdentifier("new-noop@example.org")
          .assignName("New Person").assignSaveMode(SaveMode.INSERT));
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertNull(findAsRoot("new-noop@example.org"));
  }

  /**
   * an editor (member of externalSubjects.groupAllowedForEdit) gets NO_CHANGE, and can update and insert
   */
  public void testEditorAllowed() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      ExternalSubjectSave externalSubjectSave = newSave(session);
      ExternalSubject externalSubject = externalSubjectSave.save();
      assertEquals(this.storedUuid, externalSubject.getUuid());
      assertEquals(SaveResultType.NO_CHANGE, externalSubjectSave.getSaveResultType());

      externalSubjectSave = newSave(session).assignName("Updated Name");
      externalSubjectSave.save();
      assertEquals(SaveResultType.UPDATE, externalSubjectSave.getSaveResultType());

      externalSubjectSave = new ExternalSubjectSave(session).assignIdentifier("editor-new@example.org")
          .assignName("Editor New").assignSaveMode(SaveMode.INSERT);
      externalSubjectSave.save();
      assertEquals(SaveResultType.INSERT, externalSubjectSave.getSaveResultType());
    } finally {
      GrouperSession.stopQuietly(session);
    }
    assertEquals("Updated Name", findAsRoot(IDENTIFIER).getName());
    assertNotNull(findAsRoot("editor-new@example.org"));
  }

  /**
   * root (externalSubjects.wheelOrRootCanEdit) still gets NO_CHANGE
   */
  public void testRootAllowed() {
    GrouperSession session = GrouperSession.startRootSession();
    try {
      ExternalSubjectSave externalSubjectSave = newSave(session);
      externalSubjectSave.save();
      assertEquals(SaveResultType.NO_CHANGE, externalSubjectSave.getSaveResultType());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

}
