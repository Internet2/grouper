package edu.internet2.middleware.grouper.ws.coresoap;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubject;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectConfig;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectSave;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectStorageController;
import edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectTest;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouper.ws.GrouperServiceLogic;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.util.GrouperServiceUtils;
import edu.internet2.middleware.grouper.ws.util.GrouperWsVersionUtils;
import junit.textui.TestRunner;

/**
 * <pre>
 * External subjects are off unless externalSubjects.enabled is true.  While off, WS
 * externalSubjectSave and externalSubjectDelete answer with an error result naming the property
 * (the refusal from ExternalSubject.store() / delete() lands in the per-item catch), not a
 * success, and nothing is written.  The core behavior is in
 * edu.internet2.middleware.grouper.externalSubjects.ExternalSubjectDefaultOffTest.
 *
 * Before the fix this does not compile (ExternalSubject.externalSubjectsEnabled is new) and the
 * save and delete succeed.
 *
 * Same file for every release, in grouper-ws/grouper-ws/src/test/ (GrouperServiceLogic is in the
 * grouper-ws module before 7.6.0 and in the grouper module from 7.6.0; grouper-ws sees both).
 * </pre>
 */
public class WsExternalSubjectDefaultOffTest extends GrouperTest {

  /** ws version for the calls */
  private static final GrouperVersion GROUPER_VERSION = GrouperVersion.valueOfIgnoreCase(
      GrouperWsConfig.retrieveConfig().propertyValueString("ws.testing.version"));

  /** record created while on */
  private static final String IDENTIFIER = "default-off@example.org";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsExternalSubjectDefaultOffTest("testOffWsErrorResults"));
  }

  /**
   */
  public WsExternalSubjectDefaultOffTest() {
  }

  /**
   * @param name
   */
  public WsExternalSubjectDefaultOffTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    // same external subject config as the other external subject tests (root can edit)
    ExternalSubjectTest.setupHelper();
    GrouperWsVersionUtils.assignCurrentClientVersion(GROUPER_VERSION, new StringBuilder());
  }

  @Override
  protected void tearDown() {
    GrouperServiceUtils.testSession = null;
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.enabled");
    ExternalSubjectConfig.clearCache();
    super.tearDown();
  }

  /**
   * turn external subjects on or off for this test
   * @param value true, false, or "" for blank (the code default applies)
   */
  private static void setEnabled(String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.enabled", value);
  }

  /**
   * read a record as root
   * @param identifier
   * @return the record or null
   */
  private static ExternalSubject find(String identifier) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return ExternalSubjectStorageController.findByIdentifier(identifier, false, null);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * create the record as root while on
   * @return the record
   */
  private static ExternalSubject createWhileOn() {
    setEnabled("true");
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return new ExternalSubjectSave(rootSession).assignIdentifier(IDENTIFIER)
          .assignName("Default Off").assignInstitution("Example").assignEnabled(true).save();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * off: WS save and delete answer with an error result naming the property, not a success
   */
  public void testOffWsErrorResults() {
    createWhileOn();
    setEnabled("false");

    // save (insert of a new identifier) as root
    WsExternalSubject wsExternalSubject = new WsExternalSubject();
    wsExternalSubject.setIdentifier("ws-off@example.org");
    wsExternalSubject.setName("Ws Off");
    WsExternalSubjectToSave toSave = new WsExternalSubjectToSave();
    toSave.setWsExternalSubject(wsExternalSubject);
    toSave.setWsExternalSubjectLookup(new WsExternalSubjectLookup("ws-off@example.org"));
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsExternalSubjectSaveResults saveResults = null;
    try {
      saveResults = GrouperServiceLogic.externalSubjectSave(GROUPER_VERSION,
          new WsExternalSubjectToSave[] {toSave}, null, null, null);
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      GrouperServiceUtils.testSession = null;
    }
    assertEquals("F", saveResults.getResults()[0].getResultMetadata().getSuccess());
    assertTrue(saveResults.getResults()[0].getResultMetadata().getResultMessage(),
        GrouperUtil.defaultString(saveResults.getResults()[0].getResultMetadata().getResultMessage())
          .contains("externalSubjects.enabled"));
    assertNull(find("ws-off@example.org"));

    // delete of the existing record as root
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsExternalSubjectDeleteResults deleteResults = null;
    try {
      deleteResults = GrouperServiceLogic.externalSubjectDelete(GROUPER_VERSION,
          new WsExternalSubjectLookup[] {new WsExternalSubjectLookup(IDENTIFIER)}, null, null, null);
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      GrouperServiceUtils.testSession = null;
    }
    assertEquals("F", deleteResults.getResults()[0].getResultMetadata().getSuccess());
    assertTrue(deleteResults.getResults()[0].getResultMetadata().getResultMessage(),
        GrouperUtil.defaultString(deleteResults.getResults()[0].getResultMetadata().getResultMessage())
          .contains("externalSubjects.enabled"));
    assertNotNull(find(IDENTIFIER));
  }

  /**
   * on: WS save inserts and delete deletes
   */
  public void testOnWsWrites() {
    setEnabled("true");
    WsExternalSubject wsExternalSubject = new WsExternalSubject();
    wsExternalSubject.setIdentifier("ws-on@example.org");
    wsExternalSubject.setName("Ws On");
    WsExternalSubjectToSave toSave = new WsExternalSubjectToSave();
    toSave.setWsExternalSubject(wsExternalSubject);
    toSave.setWsExternalSubjectLookup(new WsExternalSubjectLookup("ws-on@example.org"));
    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsExternalSubjectSaveResults saveResults = null;
    try {
      saveResults = GrouperServiceLogic.externalSubjectSave(GROUPER_VERSION,
          new WsExternalSubjectToSave[] {toSave}, null, null, null);
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      GrouperServiceUtils.testSession = null;
    }
    assertEquals(saveResults.getResults()[0].getResultMetadata().getResultMessage(),
        "T", saveResults.getResults()[0].getResultMetadata().getSuccess());
    assertNotNull(find("ws-on@example.org"));

    GrouperServiceUtils.testSession = GrouperSession.startRootSession();
    WsExternalSubjectDeleteResults deleteResults = null;
    try {
      deleteResults = GrouperServiceLogic.externalSubjectDelete(GROUPER_VERSION,
          new WsExternalSubjectLookup[] {new WsExternalSubjectLookup("ws-on@example.org")}, null, null, null);
    } finally {
      GrouperSession.stopQuietly(GrouperServiceUtils.testSession);
      GrouperServiceUtils.testSession = null;
    }
    assertEquals(deleteResults.getResults()[0].getResultMetadata().getResultMessage(),
        "T", deleteResults.getResults()[0].getResultMetadata().getSuccess());
    assertNull(find("ws-on@example.org"));
  }
}
