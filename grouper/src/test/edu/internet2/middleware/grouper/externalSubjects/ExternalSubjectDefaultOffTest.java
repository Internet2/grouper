package edu.internet2.middleware.grouper.externalSubjects;

import java.util.Date;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.hibernate.AuditControl;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionType;
import edu.internet2.middleware.grouper.hibernate.HibernateHandler;
import edu.internet2.middleware.grouper.hibernate.HibernateHandlerBean;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * <pre>
 * External subjects are off unless externalSubjects.enabled is true (code default false, base
 * properties value blank).  While off, ExternalSubject.store() / delete() / assignAttribute() /
 * removeAttribute() refuse, so WS externalSubjectSave and externalSubjectDelete, the UI and GSH
 * cannot create, change or delete them; lookups of existing ones still work, and the daemons
 * (enabled/disabled check, recalc fields) still maintain existing records.
 *
 * Before the fix this does not compile (ExternalSubject.externalSubjectsEnabled is new) and the
 * refusal tests fail (no check, so the writes succeed).  The key is new in the base properties
 * (blank), so testBaseDefaultOff passes with or without the patched base file.
 *
 * The WS answers are in edu.internet2.middleware.grouper.ws.coresoap.WsExternalSubjectDefaultOffTest.
 * Same file for every release, in grouper/src/test/.
 * </pre>
 */
public class ExternalSubjectDefaultOffTest extends GrouperTest {

  /** record created while on */
  private static final String IDENTIFIER = "default-off@example.org";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ExternalSubjectDefaultOffTest("testOffRefusesWrites"));
  }

  /**
   */
  public ExternalSubjectDefaultOffTest() {
  }

  /**
   * @param name
   */
  public ExternalSubjectDefaultOffTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    // same external subject config as the other external subject tests (root can edit)
    ExternalSubjectTest.setupHelper();
  }

  @Override
  protected void tearDown() {
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
   * assert a refusal names the property
   * @param re
   */
  private static void assertNamesProperty(RuntimeException re) {
    String stack = GrouperUtil.getFullStackTrace(re);
    assertTrue(stack, stack.contains("externalSubjects.enabled"));
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
   * blank value: the code default is off
   */
  public void testCodeDefaultOff() {
    setEnabled("");
    assertFalse(ExternalSubject.externalSubjectsEnabled());
    setEnabled("true");
    assertTrue(ExternalSubject.externalSubjectsEnabled());
  }

  /**
   * no override: the base properties leave it blank, so it is off
   */
  public void testBaseDefaultOff() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("externalSubjects.enabled");
    assertFalse(ExternalSubject.externalSubjectsEnabled());
  }

  /**
   * off: create, change and delete are refused (also for root); lookups still work
   */
  public void testOffRefusesWrites() {
    // create (blank = default = off)
    setEnabled("");
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new ExternalSubjectSave(rootSession).assignIdentifier("new-off@example.org")
          .assignName("New Off").assignEnabled(true).save();
      fail("create must be refused while external subjects are off");
    } catch (RuntimeException re) {
      assertNamesProperty(re);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertNull(find("new-off@example.org"));

    // a record from before (while on)
    createWhileOn();
    setEnabled("false");
    ExternalSubject externalSubject = find(IDENTIFIER);
    assertNotNull("lookup still works while off", externalSubject);

    rootSession = GrouperSession.startRootSession();
    try {
      // change
      externalSubject.setName("Changed While Off");
      try {
        externalSubject.store();
        fail("change must be refused while external subjects are off");
      } catch (RuntimeException re) {
        assertNamesProperty(re);
      }
      try {
        externalSubject.assignAttribute("jabber", "someone@jabber.example.org");
        fail("attribute change must be refused while external subjects are off");
      } catch (RuntimeException re) {
        assertNamesProperty(re);
      }
      // delete
      try {
        externalSubject.delete();
        fail("delete must be refused while external subjects are off");
      } catch (RuntimeException re) {
        assertNamesProperty(re);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    ExternalSubject stillThere = find(IDENTIFIER);
    assertNotNull(stillThere);
    assertEquals("Default Off", stillThere.getName());
  }

  /**
   * off: the enabled/disabled daemon still disables an expired external subject, and the recalc
   * daemon still updates descriptions
   */
  public void testOffDaemonsStillMaintain() {
    ExternalSubject externalSubject = createWhileOn();

    // expired but still flagged enabled (what the enabled/disabled daemon fixes); written with
    // the storage controller so store() does not recalculate the flag
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      externalSubject = find(IDENTIFIER);
      externalSubject.setDisabledTime(new Date(System.currentTimeMillis() - 60000L));
      externalSubject.setEnabled(true);
      // grouper_ext_subj.context_id is NOT NULL and GrouperAPI.onPreUpdate sets it from the thread's
      // inner GrouperContext, which only an audited hibernate callback creates (7.x passed only
      // because an earlier call left one on the thread), so do this direct write inside one
      final ExternalSubject EXTERNAL_SUBJECT = externalSubject;
      HibernateSession.callbackHibernateSession(GrouperTransactionType.READ_WRITE_OR_USE_EXISTING,
          AuditControl.WILL_AUDIT, new HibernateHandler() {
            public Object callback(HibernateHandlerBean hibernateHandlerBean) throws GrouperDAOException {
              ExternalSubjectStorageController.saveOrUpdate(EXTERNAL_SUBJECT);
              return null;
            }
          });
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertTrue(find(IDENTIFIER).isEnabled());

    setEnabled("false");
    rootSession = GrouperSession.startRootSession();
    try {
      assertTrue(ExternalSubject.internal_fixDisabled() >= 1);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertFalse("expired external subject is disabled even while off", find(IDENTIFIER).isEnabled());

    // recalc daemon: a new description template is applied while off
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("externalSubjects.desc.el",
        "recalc ${externalSubject.name}");
    rootSession = GrouperSession.startRootSession();
    try {
      ExternalSubject.internal_daemonCalcFields();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertEquals("recalc Default Off", find(IDENTIFIER).getDescription());
  }

  /**
   * on: create, change and delete work
   */
  public void testOnWrites() {
    createWhileOn();
    assertNotNull(find(IDENTIFIER));
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      ExternalSubject externalSubject = find(IDENTIFIER);
      externalSubject.setName("Changed While On");
      externalSubject.store();
      assertEquals("Changed While On", find(IDENTIFIER).getName());
      find(IDENTIFIER).delete();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    assertNull(find(IDENTIFIER));
  }
}
