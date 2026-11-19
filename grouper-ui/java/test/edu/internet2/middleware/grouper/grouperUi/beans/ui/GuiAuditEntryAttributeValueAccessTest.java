package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.lang.reflect.Method;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.audit.AuditTypeBuiltin;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue groupAuditRestrictedAttributeValueDisclosure: the group / folder / subject
 * audit screens rendered the value stored in an attribute assign value audit row to anyone who
 * could see the row (e.g. a group ADMIN), even without ATTR_READ on the attribute definition.
 * The attribute name was already hidden (secure lookup), the value was not.</p>
 *
 * <p>Setup: group test:auditPrivacy76:auditOwner, SUBJ0 has ADMIN on it.  String attribute
 * definition test:auditPrivacy76:secretDef assignable to groups with name secretAttr; SUBJ1 has
 * ATTR_READ on the definition, SUBJ0 has nothing.  GrouperAll has no attribute privileges.</p>
 *
 * <p>Audit rows are built in memory (the same constructor the API uses before saving), so the
 * test does not depend on audit persistence settings.</p>
 */
public class GuiAuditEntryAttributeValueAccessTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GuiAuditEntryAttributeValueAccessTest("testAuditValueRedactedWithoutAttrRead"));
  }

  /**
   *
   */
  public GuiAuditEntryAttributeValueAccessTest() {
    super();
  }

  /**
   * @param name
   */
  public GuiAuditEntryAttributeValueAccessTest(String name) {
    super(name);
  }

  /** secret value from the report */
  private static final String SECRET = "AUDIT_SECRET_9f72c1";

  /** group whose audits are viewed */
  private Group group;

  /** restricted definition */
  private AttributeDef attributeDef;

  /** restricted name */
  private AttributeDefName attributeDefName;

  /**
   * fixture, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Stem stem = new StemSave(grouperSession).assignName("test:auditPrivacy76").assignCreateParentStemsIfNotExist(true).save();
      this.group = new GroupSave(grouperSession).assignName("test:auditPrivacy76:auditOwner").save();
      this.group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

      this.attributeDef = stem.addChildAttributeDef("secretDef", AttributeDefType.attr);
      this.attributeDef.setAssignToGroup(true);
      this.attributeDef.setValueType(AttributeDefValueType.string);
      this.attributeDef.store();
      revokeAllFromEveryone(this.attributeDef);
      this.attributeDefName = stem.addChildAttributeDefName(this.attributeDef, "secretAttr", "secretAttr");

      this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);

      // the real assignment, as in the report (the audit rows below are built in memory)
      this.group.getAttributeValueDelegate().assignValue(this.attributeDefName.getName(), SECRET);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * GrouperAll gets no attribute privilege, whatever the create-grant-all config says
   * @param theAttributeDef definition
   */
  private static void revokeAllFromEveryone(AttributeDef theAttributeDef) {
    Subject everyone = SubjectFinder.findAllSubject();
    theAttributeDef.getPrivilegeDelegate().revokePriv(everyone, AttributeDefPrivilege.ATTR_READ, false);
    theAttributeDef.getPrivilegeDelegate().revokePriv(everyone, AttributeDefPrivilege.ATTR_VIEW, false);
    theAttributeDef.getPrivilegeDelegate().revokePriv(everyone, AttributeDefPrivilege.ATTR_UPDATE, false);
  }

  // ---------------------------------------------------------------------------------------------
  // the check
  // ---------------------------------------------------------------------------------------------

  /**
   * canReadValue: ATTR_READ on the definition, root, and the redact defaults
   */
  public void testCanReadValue() {
    String nameId = this.attributeDefName.getId();

    assertFalse("group ADMIN is not ATTR_READ", GuiAuditEntryAttributeValueAccess.canReadValue(nameId, SubjectTestHelper.SUBJ0));
    assertTrue("ATTR_READ", GuiAuditEntryAttributeValueAccess.canReadValue(nameId, SubjectTestHelper.SUBJ1));
    assertTrue("root", GuiAuditEntryAttributeValueAccess.canReadValue(nameId, SubjectFinder.findRootSubject()));

    assertFalse("no subject", GuiAuditEntryAttributeValueAccess.canReadValue(nameId, null));
    assertFalse("no id", GuiAuditEntryAttributeValueAccess.canReadValue(null, SubjectTestHelper.SUBJ1));
    assertFalse("blank id", GuiAuditEntryAttributeValueAccess.canReadValue(" ", SubjectTestHelper.SUBJ1));
    assertFalse("unknown id", GuiAuditEntryAttributeValueAccess.canReadValue("0123456789abcdef0123456789abcdef", SubjectTestHelper.SUBJ1));
  }

  /**
   * once the definition is deleted the value is redacted, even for someone who could read it
   */
  public void testDeletedDefinitionRedacted() {

    // a second definition with no assignments, so it can simply be deleted
    AttributeDef otherDef = null;
    String nameId = null;
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Stem stem = this.attributeDef.getParentStem();
      otherDef = stem.addChildAttributeDef("otherDef", AttributeDefType.attr);
      otherDef.setAssignToGroup(true);
      otherDef.setValueType(AttributeDefValueType.string);
      otherDef.store();
      revokeAllFromEveryone(otherDef);
      nameId = stem.addChildAttributeDefName(otherDef, "otherAttr", "otherAttr").getId();
      otherDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    assertTrue(GuiAuditEntryAttributeValueAccess.canReadValue(nameId, SubjectTestHelper.SUBJ1));

    grouperSession = GrouperSession.startRootSession();
    try {
      otherDef.delete();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertFalse(GuiAuditEntryAttributeValueAccess.canReadValue(nameId, SubjectTestHelper.SUBJ1));
  }

  // ---------------------------------------------------------------------------------------------
  // the audit line value
  // ---------------------------------------------------------------------------------------------

  /**
   * the group ADMIN (SUBJ0) does not get the value for add, update and delete rows; SUBJ1 and
   * root do
   */
  public void testAuditValueRedactedWithoutAttrRead() {
    if (!auditLineShowsValue()) {
      return;
    }
    for (AuditTypeBuiltin auditTypeBuiltin : new AuditTypeBuiltin[] {AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD,
        AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_UPDATE, AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_DELETE}) {

      AuditEntry auditEntry = valueAuditEntry(auditTypeBuiltin);

      String subj0Value = attributeValueAs(SubjectTestHelper.SUBJ0, auditEntry);
      assertNull(auditTypeBuiltin + ": group ADMIN without ATTR_READ", subj0Value);

      assertEquals(auditTypeBuiltin + ": ATTR_READ", SECRET, attributeValueAs(SubjectTestHelper.SUBJ1, auditEntry));
      assertEquals(auditTypeBuiltin + ": root", SECRET, attributeValueAs(SubjectFinder.findRootSubject(), auditEntry));
    }
  }

  /**
   * the stored update description (extended view and CSV export) lists the old and new values: the
   * group ADMIN gets it without them, SUBJ1 gets it unchanged
   */
  public void testUpdateDescriptionRedactedWithoutAttrRead() {
    String description = "Updated attribute assignment value: valueString: from: 'OLD_SECRET_76' to: '" + SECRET + "'";

    AuditEntry forSubj0 = valueAuditEntry(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_UPDATE);
    forSubj0.setDescription(description);
    String subj0Description = auditEntryAs(SubjectTestHelper.SUBJ0, forSubj0).getDescription();
    assertFalse(subj0Description, subj0Description.contains(SECRET));
    assertFalse(subj0Description, subj0Description.contains("OLD_SECRET_76"));

    AuditEntry forSubj1 = valueAuditEntry(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_UPDATE);
    forSubj1.setDescription(description);
    assertEquals(description, auditEntryAs(SubjectTestHelper.SUBJ1, forSubj1).getDescription());

    // other rows are untouched
    AuditEntry addRow = valueAuditEntry(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD);
    addRow.setDescription("Added attribute assignment value");
    assertEquals("Added attribute assignment value", auditEntryAs(SubjectTestHelper.SUBJ0, addRow).getDescription());
  }

  /**
   * a row that does not carry an attribute def name id is redacted
   */
  public void testAuditValueWithoutNameIdRedacted() {
    if (!auditLineShowsValue()) {
      return;
    }
    AuditEntry auditEntry = new AuditEntry(AuditTypeBuiltin.ATTRIBUTE_ASSIGN_VALUE_ADD,
        "value", SECRET, "groupOrStemId", this.group.getId());
    assertNull(attributeValueAs(SubjectTestHelper.SUBJ1, auditEntry));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @param auditTypeBuiltin value add / update / delete
   * @return in-memory audit row about the secret value on the group
   */
  private AuditEntry valueAuditEntry(AuditTypeBuiltin auditTypeBuiltin) {
    try {
      return new AuditEntry(auditTypeBuiltin,
          "attributeDefNameId", this.attributeDefName.getId(),
          "value", SECRET,
          "attributeDefNameName", this.attributeDefName.getName(),
          "groupOrStemId", this.group.getId());
    } catch (RuntimeException re) {
      // the groupOrStemId label (group / folder audits list value rows) is 5.4.0+; older releases
      // show these rows in subject audits only, so build the row without it there
      if (re.getMessage() == null || !re.getMessage().contains("groupOrStemId")) {
        throw re;
      }
      return new AuditEntry(auditTypeBuiltin,
          "attributeDefNameId", this.attributeDefName.getId(),
          "value", SECRET,
          "attributeDefNameName", this.attributeDefName.getName());
    }
  }

  /**
   * the audit line shows the stored value only from 5.4.0 (GuiAuditEntry.setupAttributeValue /
   * getAttributeValue).  Before that (e.g. 2.6.19, 5.0.0-5.2.0) there is no rendered value to
   * redact: the fix there is the update description, which testUpdateDescriptionRedactedWithoutAttrRead
   * covers.  Looked up by reflection so this one file compiles on every release
   * @return true if this release renders the value
   */
  private static boolean auditLineShowsValue() {
    try {
      GuiAuditEntry.class.getDeclaredMethod("setupAttributeValue");
      GuiAuditEntry.class.getMethod("getAttributeValue");
      return true;
    } catch (NoSuchMethodException nsme) {
      System.out.println("GuiAuditEntryAttributeValueAccessTest: this release does not render the audit value "
          + "(GuiAuditEntry.setupAttributeValue is 5.4.0+), nothing to check");
      return false;
    }
  }

  /**
   * build the GuiAuditEntry in a session as the viewer and get its audit entry (what the audit JSPs
   * and the CSV export read the description from)
   * @param viewer user looking at the audit
   * @param auditEntry row
   * @return the audit entry as handed to the screen
   */
  private static AuditEntry auditEntryAs(Subject viewer, AuditEntry auditEntry) {
    GrouperSession grouperSession = GrouperSession.start(viewer);
    try {
      return new GuiAuditEntry(auditEntry).getAuditEntry();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * build the GuiAuditEntry in a session as the viewer and run its value setup (the step
   * getAuditLine runs for these rows; getAuditLine itself needs the UI text container)
   * @param viewer user looking at the audit
   * @param auditEntry row
   * @return the value the audit line would show, null if redacted
   */
  private static String attributeValueAs(Subject viewer, AuditEntry auditEntry) {
    GrouperSession grouperSession = GrouperSession.start(viewer);
    try {
      GuiAuditEntry guiAuditEntry = new GuiAuditEntry(auditEntry);
      Method setupAttributeValue = GuiAuditEntry.class.getDeclaredMethod("setupAttributeValue");
      setupAttributeValue.setAccessible(true);
      setupAttributeValue.invoke(guiAuditEntry);
      return (String)GuiAuditEntry.class.getMethod("getAttributeValue").invoke(guiAuditEntry);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
