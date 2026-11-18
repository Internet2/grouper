package edu.internet2.middleware.grouper.attr;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * AttributeDef.delete() had no privilege check of its own.  The only check was incidental: deleting
 * each attribute def name requires ATTR_ADMIN on the def.  So a def with no names could be deleted
 * by anyone who could look it up, e.g. a WS caller with only ATTR_VIEW (attributeDefDelete resolves
 * the def at view level), and the delete first removes the assignments on the def as root.
 *
 * The fix requires ATTR_ADMIN at the top of AttributeDef.delete(), before any side effect.  The WS
 * operation already maps InsufficientPrivilegeException to INSUFFICIENT_PRIVILEGES.
 */
public class AttributeDefDeleteAuthorizationTest extends GrouperTest {

  /** name of the def with no attribute def names */
  private static final String EMPTY_DEF_NAME = "test:delAuthz:emptyDef";

  /** name of the def that has one attribute def name */
  private static final String DEF_WITH_NAME_NAME = "test:delAuthz:defWithName";

  /** the marker name assigned to the empty def (metadata assignment that delete removes as root) */
  private static final String MARKER_NAME_NAME = "test:delAuthz:markerName";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefDeleteAuthorizationTest("testEmptyDefDeleteNeedsAttrAdmin"));
  }

  /**
   *
   */
  public AttributeDefDeleteAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public AttributeDefDeleteAuthorizationTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    // so the test subjects only have the privileges granted below
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
  }

  /**
   * as root: the empty def (with a metadata assignment and a scope), the def with one name, and the
   * grants: SUBJ0 view, SUBJ1 read and update, SUBJ2 admin
   */
  private void setupDefs() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      new StemSave(rootSession).assignName("test:delAuthz").assignCreateParentStemsIfNotExist(true).save();

      // marker def that can be assigned to an attribute def
      AttributeDef markerDef = new AttributeDefSave(rootSession).assignName("test:delAuthz:markerDef")
          .assignAttributeDefType(AttributeDefType.attr).assignToAttributeDef(true).save();
      AttributeDefName markerName = new AttributeDefNameSave(rootSession, markerDef)
          .assignName(MARKER_NAME_NAME).save();

      // the def with no names: its delete reached no privilege check before the fix
      AttributeDef emptyDef = new AttributeDefSave(rootSession).assignName(EMPTY_DEF_NAME)
          .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).save();
      emptyDef.getAttributeDelegate().assignAttribute(markerName);
      emptyDef.getAttributeDefScopeDelegate().assignOwnerNameEquals("test:delAuthz:someGroup");

      // the def with one name: the name delete happened to check ATTR_ADMIN
      AttributeDef defWithName = new AttributeDefSave(rootSession).assignName(DEF_WITH_NAME_NAME)
          .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).save();
      new AttributeDefNameSave(rootSession, defWithName).assignName("test:delAuthz:defWithNameName").save();

      for (AttributeDef attributeDef : new AttributeDef[] {emptyDef, defWithName}) {
        attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
        attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_READ, false);
        attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_UPDATE, false);
        attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_ADMIN, false);
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * try to delete a def as a subject
   * @param subject
   * @param attributeDefName
   * @return true if deleted, false if InsufficientPrivilegeException
   */
  private boolean deleteAs(Subject subject, String attributeDefName) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      AttributeDef attributeDef = AttributeDefFinder.findByName(attributeDefName, true);
      try {
        attributeDef.delete();
        return true;
      } catch (InsufficientPrivilegeException ipe) {
        return false;
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * as root: does the empty def still exist with its assignment and scope
   */
  private void assertEmptyDefUnchanged() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      AttributeDef emptyDef = AttributeDefFinder.findByName(EMPTY_DEF_NAME, false);
      assertNotNull("the empty def must still exist", emptyDef);
      assertTrue("the metadata assignment on the def must still exist",
          emptyDef.getAttributeDelegate().hasAttributeByName(MARKER_NAME_NAME));
      assertEquals("the scope on the def must still exist", 1,
          emptyDef.getAttributeDefScopeDelegate().retrieveAttributeDefScopes().size());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * ATTR_VIEW only, or ATTR_READ plus ATTR_UPDATE, must not delete an empty def or touch its
   * assignments and scopes; ATTR_ADMIN can
   */
  public void testEmptyDefDeleteNeedsAttrAdmin() {
    setupDefs();

    assertFalse("ATTR_VIEW must not delete an empty def", deleteAs(SubjectTestHelper.SUBJ0, EMPTY_DEF_NAME));
    assertEmptyDefUnchanged();

    assertFalse("ATTR_READ and ATTR_UPDATE must not delete an empty def", deleteAs(SubjectTestHelper.SUBJ1, EMPTY_DEF_NAME));
    assertEmptyDefUnchanged();

    assertTrue("ATTR_ADMIN can delete an empty def", deleteAs(SubjectTestHelper.SUBJ2, EMPTY_DEF_NAME));
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      assertNull(AttributeDefFinder.findByName(EMPTY_DEF_NAME, false));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the verdict does not depend on whether the def has names
   */
  public void testDefWithNameSameVerdict() {
    setupDefs();

    assertFalse(deleteAs(SubjectTestHelper.SUBJ0, DEF_WITH_NAME_NAME));
    assertFalse(deleteAs(SubjectTestHelper.SUBJ1, DEF_WITH_NAME_NAME));

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      assertNotNull(AttributeDefFinder.findByName(DEF_WITH_NAME_NAME, false));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    assertTrue(deleteAs(SubjectTestHelper.SUBJ2, DEF_WITH_NAME_NAME));
  }

  /**
   * root (and therefore system cleanup paths) can still delete
   */
  public void testRootCanDelete() {
    setupDefs();
    assertTrue(deleteAs(SubjectFinder.findRootSubject(), EMPTY_DEF_NAME));
  }

}
