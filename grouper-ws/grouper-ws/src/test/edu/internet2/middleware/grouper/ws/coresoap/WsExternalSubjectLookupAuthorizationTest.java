/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.ws.coresoap;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectSave;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorageController;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectTest;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.coresoap.WsExternalSubjectDeleteResult.WsExternalSubjectDeleteResultCode;
import edu.internet2.middleware.grouper.ws.coresoap.WsExternalSubjectLookup.ExternalSubjectFindResult;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * WS externalSubjectDelete (and any other caller of WsExternalSubjectLookup) must not read an
 * external subject for a caller who cannot view / edit external subjects.  Before the fix the
 * lookup read the record with no check, externalSubjectDelete copied the whole record into the
 * response, and only then did ExternalSubject.delete() deny, so a non-editor got the name,
 * email, institution and attributes back with INSUFFICIENT_PRIVILEGES, and a missing identifier
 * answered SUCCESS_EXTERNAL_SUBJECT_NOT_FOUND instead (existence oracle).
 *
 * Setup: as root, record delete-leak@example.org with name, email and institution;
 * externalSubjects.groupAllowedForEdit = etc:externalEditors, which has SUBJ1 (not SUBJ0).
 *
 * Same file for every release 5.0.0 - 7.x.  Location: grouper/src/test/... from 7.6.0 (the WS
 * classes moved into the grouper module), grouper-ws/grouper-ws/src/test/... before that.
 * </pre>
 */
public class WsExternalSubjectLookupAuthorizationTest extends GrouperTest {

  /** identifier of the protected record */
  private static final String IDENTIFIER = "delete-leak@example.org";

  /** identifier that does not exist */
  private static final String MISSING_IDENTIFIER = "missing-delete-leak@example.org";

  /** stored name */
  private static final String NAME = "Protected External Person";

  /** stored email */
  private static final String EMAIL = "delete-leak-private@example.net";

  /** stored institution */
  private static final String INSTITUTION = "Private Institution";

  /** group of allowed editors */
  private static final String EDITORS_GROUP = "etc:externalEditors";

  /** ws version for the calls */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(WsExternalSubjectLookupAuthorizationTest.class);
  }

  /**
   *
   */
  public WsExternalSubjectLookupAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public WsExternalSubjectLookupAuthorizationTest(String name) {
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

    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      // editors group with SUBJ1; SUBJ0 is an ordinary authenticated subject
      Group editors = new GroupSave(rootSession).assignName(EDITORS_GROUP)
          .assignCreateParentStemsIfNotExist(true).save();
      editors.addMember(SubjectTestHelper.SUBJ1, false);

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.wheelOrRootCanEdit", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.groupAllowedForEdit", EDITORS_GROUP);
      ExternalSubjectConfig.clearCache();

      new ExternalSubjectSave(rootSession).assignIdentifier(IDENTIFIER)
          .assignName(NAME).assignEmail(EMAIL).assignInstitution(INSTITUTION).assignEnabled(true).save();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperServiceUtils.testSession = null;
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.groupAllowedForEdit");
    ExternalSubjectConfig.clearCache();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.enabled");
    super.tearDown();
  }

  /**
   * read a record as root
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
   * call WS externalSubjectDelete as a subject (GrouperServiceUtils.testSession is the caller)
   * @param subject caller
   * @param identifiers to delete
   * @return the results
   */
  private static WsExternalSubjectDeleteResults deleteAs(Subject subject, String... identifiers) {
    WsExternalSubjectLookup[] lookups = new WsExternalSubjectLookup[identifiers.length];
    for (int i = 0; i < identifiers.length; i++) {
      lookups[i] = new WsExternalSubjectLookup(identifiers[i]);
    }
    GrouperServiceUtils.testSession = GrouperSession.start(subject);
    try {
      return GrouperServiceLogic.externalSubjectDelete(GROUPER_VERSION, lookups, null, null, null);
    } finally {
      // older releases stop the session in the logic; stopQuietly is safe either way
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      GrouperServiceUtils.testSession = null;
    }
  }

  /**
   * a denied item: INSUFFICIENT_PRIVILEGES and no record data at all.  The result may echo back
   * the identifier the caller sent (WsExternalSubjectDeleteResult(lookup) always does that, and
   * it tells the caller nothing new), but nothing read from the stored record.
   * @param result
   * @param requestedIdentifier the identifier the caller sent for this item
   */
  private static void assertDeniedNoData(WsExternalSubjectDeleteResult result, String requestedIdentifier) {
    assertEquals(result.getResultMetadata().getResultMessage(),
        WsExternalSubjectDeleteResultCode.INSUFFICIENT_PRIVILEGES.name(),
        result.getResultMetadata().getResultCode());
    WsExternalSubject wsExternalSubject = result.getWsExternalSubject();
    if (wsExternalSubject != null) {
      // only the caller's own identifier may come back
      assertEquals(requestedIdentifier, wsExternalSubject.getIdentifier());
      assertNull("denied delete must not return the email", wsExternalSubject.getEmail());
      assertNull("denied delete must not return the name", wsExternalSubject.getName());
      assertNull("denied delete must not return the institution", wsExternalSubject.getInstitution());
      assertNull("denied delete must not return the description", wsExternalSubject.getDescription());
      assertNull("denied delete must not return the uuid", wsExternalSubject.getUuid());
      // enabled is not checked: WsExternalSubject defaults it to "T" in the field initializer,
      // so it is set on every result and says nothing about the stored record
      assertNull("denied delete must not return the search string", wsExternalSubject.getSearchStringLower());
      assertNull("denied delete must not return vetted emails", wsExternalSubject.getVettedEmailAddresses());
      assertEquals("denied delete must not return attributes", 0,
          GrouperUtil.length(wsExternalSubject.getWsExternalSubjectAttributes()));
    }
    String message = GrouperUtil.defaultString(result.getResultMetadata().getResultMessage());
    assertFalse(message, message.contains(EMAIL));
    assertFalse(message, message.contains(NAME));
    assertFalse(message, message.contains(INSTITUTION));
  }

  /**
   * a non-editor deleting an existing record gets no data, and the record stays
   */
  public void testDeleteExistingDeniedNoData() {
    WsExternalSubjectDeleteResults results = deleteAs(SubjectTestHelper.SUBJ0, IDENTIFIER);
    assertEquals(1, GrouperUtil.length(results.getResults()));
    assertDeniedNoData(results.getResults()[0], IDENTIFIER);
    assertNotNull(findAsRoot(IDENTIFIER));
  }

  /**
   * a non-editor gets the same answer for a missing identifier (no existence oracle)
   */
  public void testDeleteMissingSameAnswer() {
    WsExternalSubjectDeleteResults results = deleteAs(SubjectTestHelper.SUBJ0, MISSING_IDENTIFIER);
    assertEquals(1, GrouperUtil.length(results.getResults()));
    assertDeniedNoData(results.getResults()[0], MISSING_IDENTIFIER);
  }

  /**
   * a denied multi-item request returns no data for any item
   */
  public void testDeleteMultiDeniedNoData() {
    WsExternalSubjectDeleteResults results = deleteAs(SubjectTestHelper.SUBJ0, IDENTIFIER, MISSING_IDENTIFIER);
    assertEquals(2, GrouperUtil.length(results.getResults()));
    assertDeniedNoData(results.getResults()[0], IDENTIFIER);
    assertDeniedNoData(results.getResults()[1], MISSING_IDENTIFIER);
    assertEquals(results.getResults()[0].getResultMetadata().getResultCode(),
        results.getResults()[1].getResultMetadata().getResultCode());
    assertNotNull(findAsRoot(IDENTIFIER));
  }

  /**
   * the lookup itself does not read the record for a non-editor
   */
  public void testLookupDeniedForNonEditor() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      WsExternalSubjectLookup lookup = new WsExternalSubjectLookup(IDENTIFIER);
      assertNull(lookup.retrieveExternalSubjectIfNeeded(session, null));
      assertNull(lookup.retrieveExternalSubject());
      assertEquals(ExternalSubjectFindResult.INSUFFICIENT_PRIVILEGES, lookup.retrieveExternalSubjectFindResult());
      assertFalse(lookup.retrieveExternalSubjectFindResult().isSuccess());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * an editor still finds the record through the lookup and can delete it
   */
  public void testEditorAllowed() {
    GrouperSession session = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      WsExternalSubjectLookup lookup = new WsExternalSubjectLookup(IDENTIFIER);
      ExternalSubject externalSubject = lookup.retrieveExternalSubjectIfNeeded(session, null);
      assertNotNull(externalSubject);
      assertEquals(ExternalSubjectFindResult.SUCCESS, lookup.retrieveExternalSubjectFindResult());
    } finally {
      GrouperSession.stopQuietly(session);
    }

    WsExternalSubjectDeleteResults results = deleteAs(SubjectTestHelper.SUBJ1, IDENTIFIER);
    assertEquals(1, GrouperUtil.length(results.getResults()));
    assertEquals(results.getResults()[0].getResultMetadata().getResultMessage(),
        WsExternalSubjectDeleteResultCode.SUCCESS.name(),
        results.getResults()[0].getResultMetadata().getResultCode());
    assertNull(findAsRoot(IDENTIFIER));
  }

  /**
   * an editor probing a missing identifier still gets the normal not found answer
   */
  public void testEditorMissingNotFound() {
    WsExternalSubjectDeleteResults results = deleteAs(SubjectTestHelper.SUBJ1, MISSING_IDENTIFIER);
    assertEquals(1, GrouperUtil.length(results.getResults()));
    assertEquals(results.getResults()[0].getResultMetadata().getResultMessage(),
        WsExternalSubjectDeleteResultCode.SUCCESS_EXTERNAL_SUBJECT_NOT_FOUND.name(),
        results.getResults()[0].getResultMetadata().getResultCode());
  }

}
