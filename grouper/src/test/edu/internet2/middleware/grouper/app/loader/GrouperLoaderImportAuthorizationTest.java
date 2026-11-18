package edu.internet2.middleware.grouper.app.loader;

import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.abac.GrouperAbac;
import edu.internet2.middleware.grouper.app.serviceLifecycle.GrouperRecentMemberships;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.GrouperSessionException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * importing a loader config (GrouperLoaderImportExport.importLoaderConfig, the loader screen's
 * "Import loader config") is a save by the importing user, so a scripted group or recent
 * memberships import gets the same privilege and content checks as the loader screen's save,
 * before anything is written.  before the fix a group admin could import a script using a group
 * they cannot read, and the daemon would copy its members into their group.
 *
 * <p>SUBJ0 is an ADMIN of test:target and imports onto it.  the importing user is the subject of
 * the grouper session, as in the UI.</p>
 */
public class GrouperLoaderImportAuthorizationTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperLoaderImportAuthorizationTest("testJexlUnreadableGroupRefused"));
  }

  /**
   * @param name
   */
  public GrouperLoaderImportAuthorizationTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    // so SUBJ0 can only read the groups it is given READ on
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  }

  /** the group SUBJ0 imports onto, which SUBJ0 is an ADMIN of */
  private Group target;

  /** a group SUBJ0 can read, with SUBJ1 */
  private Group readable;

  /** a group SUBJ0 can see but not read, with SUBJ5 */
  private Group viewOnly;

  /** a group SUBJ0 cannot see, with SUBJ4 */
  private Group hidden;

  // ---------------------------------------------------------------------------------------------
  // setup and helpers

  /**
   * make the groups and bring the sql cache up to date, which is what a script reads
   */
  /**
   * GrouperAbacTestHelper (ABAC data fields) exists only in some releases (6.2.0+, 7.1.0+); call it
   * by reflection so this test compiles on every patched release, and skip it where it is absent
   */
  private static void setupAbacDataFieldsIfPresent() {
    try {
      Class.forName("edu.internet2.middleware.grouper.abac.GrouperAbacTestHelper")
        .getMethod("setupDataFields").invoke(null);
    } catch (ClassNotFoundException cnfe) {
      System.out.println("skip: GrouperAbacTestHelper not in this release");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void createGroups() {

    setupAbacDataFieldsIfPresent();
    GrouperSession.startRootSession();

    this.target = new GroupSave().assignName("test:target").assignCreateParentStemsIfNotExist(true).save();
    this.target.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

    this.readable = new GroupSave().assignName("test:readable").assignCreateParentStemsIfNotExist(true).save();
    this.readable.addMember(SubjectTestHelper.SUBJ1, false);
    this.readable.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

    this.viewOnly = new GroupSave().assignName("test:viewOnly").assignCreateParentStemsIfNotExist(true).save();
    this.viewOnly.addMember(SubjectTestHelper.SUBJ5, false);
    this.viewOnly.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

    this.hidden = new GroupSave().assignName("secret:hidden").assignCreateParentStemsIfNotExist(true).save();
    this.hidden.addMember(SubjectTestHelper.SUBJ4, false);

    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "CHANGE_LOG_changeLogTempToChangeLog");
    GrouperLoader.runOnceByJobName(GrouperSession.staticGrouperSession(), "OTHER_JOB_sqlCacheFullSync");
  }

  /**
   * @return the scripted group script attribute name
   */
  private static String scriptName() {
    return GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_SCRIPT;
  }

  /**
   * @param name attribute name
   * @param value attribute value
   * @return one attribute of an export, as json
   */
  private static String attribute(String name, String value) {
    return "{\"attributeName\": \"" + name + "\", \"attributeValue\": \"" + value + "\"}";
  }

  /**
   * @param loaderType
   * @param attributes attribute json objects
   * @return an export, as json
   */
  private static String export(String loaderType, String... attributes) {
    return "{\"loaderType\": \"" + loaderType + "\", \"attributes\": [" + GrouperUtil.join(attributes, ", ") + "]}";
  }

  /**
   * @param script
   * @return a scripted group export with this script, as exportLoaderConfig writes it
   */
  private static String jexlExport(String script) {
    return export(GrouperLoaderImportAuthorization.LOADER_TYPE_JEXL_SCRIPT, attribute(scriptName(), script));
  }

  /**
   * import onto test:target as a subject, the way the UI does it (a session as the user)
   * @param subject
   * @param json
   * @return the errors
   */
  private List<String> importAs(Subject subject, String json) {
    GrouperSession rootSession = GrouperSession.staticGrouperSession();
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      return GrouperLoaderImportExport.importLoaderConfig(json, this.target);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      // carry on as root for the asserts
      if (rootSession != null) {
        GrouperSession.startRootSession();
      }
    }
  }

  /**
   * @param markerName the loader type's marker attribute name
   * @return the marker's assignment on test:target, looked up as root, or null
   */
  private AttributeAssign markerAssign(final String markerName) {
    return (AttributeAssign)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {

      @Override
      public Object callback(GrouperSession grouperSession) throws GrouperSessionException {
        AttributeDefName marker = AttributeDefNameFinder.findByName(markerName, true);
        Group group = GroupFinder.findByName(grouperSession, "test:target", true);
        return group.getAttributeDelegate().retrieveAssignment(null, marker, false, false);
      }
    });
  }

  /**
   * @return the scripted group marker assignment on test:target, or null
   */
  private AttributeAssign jexlMarkerAssign() {
    return markerAssign(GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_MARKER);
  }

  /**
   * @return the recent memberships marker assignment on test:target, or null
   */
  private AttributeAssign recentMarkerAssign() {
    return markerAssign(GrouperRecentMemberships.recentMembershipsStemName() + ":"
        + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_MARKER);
  }

  /**
   * assert the import was refused and nothing was written
   * @param errors
   */
  private void assertRefused(List<String> errors) {
    assertTrue("expected the import to be refused", GrouperUtil.length(errors) > 0);
    assertNull("nothing should be written", jexlMarkerAssign());
    assertNull("nothing should be written", recentMarkerAssign());
  }

  // ---------------------------------------------------------------------------------------------
  // scripted groups

  /**
   * the security report's attack: a script using a group SUBJ0 cannot see is refused, and no marker or
   * script is stored, so the daemon has nothing to run
   */
  public void testJexlUnreadableGroupRefused() {
    createGroups();
    assertRefused(importAs(SubjectTestHelper.SUBJ0, jexlExport("entity.memberOf('secret:hidden')")));
  }

  /**
   * a group SUBJ0 can see but not read is refused too
   */
  public void testJexlViewOnlyGroupRefused() {
    createGroups();
    assertRefused(importAs(SubjectTestHelper.SUBJ0, jexlExport("entity.memberOf('test:viewOnly')")));

    // in a bigger script too
    assertRefused(importAs(SubjectTestHelper.SUBJ0,
        jexlExport("entity.memberOf('test:readable') || entity.memberOf('test:viewOnly')")));
  }

  /**
   * a script using a group SUBJ0 can read is imported, as the loader screen would save it.  the
   * last sync time in an export is accepted and not copied, since the daemon sets it
   */
  public void testJexlReadableGroupAllowed() {
    createGroups();

    String lastSyncName = GrouperAbac.jexlScriptStemName() + ":" + GrouperAbac.GROUPER_JEXL_SCRIPT_JEXL_LAST_GROUP_SYNC;
    List<String> errors = importAs(SubjectTestHelper.SUBJ0, export(GrouperLoaderImportAuthorization.LOADER_TYPE_JEXL_SCRIPT,
        attribute(scriptName(), "entity.memberOf('test:readable')"),
        attribute(lastSyncName, "2026/01/01 00:00:00.000")));
    assertEquals(GrouperUtil.toStringForLog(errors), 0, GrouperUtil.length(errors));

    AttributeAssign attributeAssign = jexlMarkerAssign();
    assertNotNull(attributeAssign);
    assertEquals("entity.memberOf('test:readable')", attributeAssign.getAttributeValueDelegate().retrieveValueString(scriptName()));
    assertNull(attributeAssign.getAttributeValueDelegate().retrieveValueString(lastSyncName));
  }

  /**
   * the same script is allowed once SUBJ0 is given READ on the group
   */
  public void testJexlAllowedAfterRead() {
    createGroups();
    assertRefused(importAs(SubjectTestHelper.SUBJ0, jexlExport("entity.memberOf('test:viewOnly')")));

    this.viewOnly.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
    List<String> errors = importAs(SubjectTestHelper.SUBJ0, jexlExport("entity.memberOf('test:viewOnly')"));
    assertEquals(GrouperUtil.toStringForLog(errors), 0, GrouperUtil.length(errors));
    assertNotNull(jexlMarkerAssign());
  }

  /**
   * an attribute the loader screen does not save for a scripted group refuses the whole import
   */
  public void testJexlUnknownAttributeRefused() {
    createGroups();
    String recentGroupFromName = GrouperRecentMemberships.recentMembershipsStemName() + ":"
        + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM;
    assertRefused(importAs(SubjectTestHelper.SUBJ0, export(GrouperLoaderImportAuthorization.LOADER_TYPE_JEXL_SCRIPT,
        attribute(scriptName(), "entity.memberOf('test:readable')"),
        attribute(recentGroupFromName, this.hidden.getId()))));
  }

  /**
   * the script given twice is refused: the check would see one and the import write the other
   */
  public void testJexlDuplicateScriptRefused() {
    createGroups();
    assertRefused(importAs(SubjectTestHelper.SUBJ0, export(GrouperLoaderImportAuthorization.LOADER_TYPE_JEXL_SCRIPT,
        attribute(scriptName(), "entity.memberOf('test:readable')"),
        attribute(scriptName(), "entity.memberOf('secret:hidden')"))));
  }

  /**
   * a missing or invalid script is refused
   */
  public void testJexlInvalidScriptRefused() {
    createGroups();
    assertRefused(importAs(SubjectTestHelper.SUBJ0, export(GrouperLoaderImportAuthorization.LOADER_TYPE_JEXL_SCRIPT)));
    assertRefused(importAs(SubjectTestHelper.SUBJ0, jexlExport("entity.memberOf(")));
  }

  /**
   * a scripted group import needs the scripted group editor rule, not the recent memberships one:
   * with grouper.abac.edit.if.in.group set to a group SUBJ0 is not in, SUBJ0 is refused even
   * though it is an ADMIN of the group and may edit recent memberships loaders
   */
  public void testJexlNeedsAbacEditor() {
    createGroups();
    Group abacEditors = new GroupSave().assignName("test:abacEditors").assignCreateParentStemsIfNotExist(true).save();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.edit.if.in.group", abacEditors.getName());

    assertRefused(importAs(SubjectTestHelper.SUBJ0, jexlExport("entity.memberOf('test:readable')")));

    abacEditors.addMember(SubjectTestHelper.SUBJ0, false);
    List<String> errors = importAs(SubjectTestHelper.SUBJ0, jexlExport("entity.memberOf('test:readable')"));
    assertEquals(GrouperUtil.toStringForLog(errors), 0, GrouperUtil.length(errors));
  }

  /**
   * someone who is not an ADMIN of the group is refused
   */
  public void testJexlNotAdminRefused() {
    createGroups();
    this.target.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.UPDATE, false);
    this.readable.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.READ, false);
    assertRefused(importAs(SubjectTestHelper.SUBJ2, jexlExport("entity.memberOf('test:readable')")));
  }

  /**
   * mainline only (GrouperAbacScriptedGroup.checkScriptForSave): a script using the group it is
   * for, and a composite target, are refused as on the loader screen.  the release patches make
   * the checks their own release's loader screen makes, which do not include these
   */
  public void testJexlSelfReferenceAndCompositeRefused() {
    // a release patch (no GrouperAbacScriptedGroup) does not make these checks, so there is
    // nothing to test.  looked up by name so this test compiles against a release too
    try {
      Class.forName("edu.internet2.middleware.grouper.abac.GrouperAbacScriptedGroup");
    } catch (ClassNotFoundException cnfe) {
      return;
    }
    createGroups();
    assertRefused(importAs(SubjectTestHelper.SUBJ0, jexlExport("entity.memberOf('test:target')")));

    this.target.addCompositeMember(CompositeType.COMPLEMENT, this.readable, this.viewOnly);
    assertRefused(importAs(SubjectTestHelper.SUBJ0, jexlExport("entity.memberOf('test:readable')")));
  }

  // ---------------------------------------------------------------------------------------------
  // recent memberships

  /**
   * @param groupUuidFrom
   * @return a recent memberships export for one day from this group
   */
  private static String recentExport(String groupUuidFrom) {
    String stem = GrouperRecentMemberships.recentMembershipsStemName() + ":";
    return export(GrouperLoaderImportAuthorization.LOADER_TYPE_RECENT_MEMBERSHIPS,
        attribute(stem + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, "86400000000"),
        attribute(stem + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, groupUuidFrom),
        attribute(stem + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_INCLUDE_CURRENT, "true"));
  }

  /**
   * recent memberships of a group SUBJ0 cannot read is refused, of one it can read is imported
   */
  public void testRecentMembershipsNeedsReadOnFromGroup() {
    createGroups();
    assertRefused(importAs(SubjectTestHelper.SUBJ0, recentExport(this.hidden.getId())));
    assertRefused(importAs(SubjectTestHelper.SUBJ0, recentExport(this.viewOnly.getId())));
    assertRefused(importAs(SubjectTestHelper.SUBJ0, recentExport("notAGroupUuid")));

    List<String> errors = importAs(SubjectTestHelper.SUBJ0, recentExport(this.readable.getId()));
    assertEquals(GrouperUtil.toStringForLog(errors), 0, GrouperUtil.length(errors));
    AttributeAssign attributeAssign = recentMarkerAssign();
    assertNotNull(attributeAssign);
    assertEquals(this.readable.getId(), attributeAssign.getAttributeValueDelegate().retrieveValueString(
        GrouperRecentMemberships.recentMembershipsStemName() + ":" + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM));
  }

  /**
   * a recent memberships import with a scripted group attribute in it is refused
   */
  public void testRecentMembershipsUnknownAttributeRefused() {
    createGroups();
    String stem = GrouperRecentMemberships.recentMembershipsStemName() + ":";
    assertRefused(importAs(SubjectTestHelper.SUBJ0, export(GrouperLoaderImportAuthorization.LOADER_TYPE_RECENT_MEMBERSHIPS,
        attribute(stem + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_MICROS, "86400000000"),
        attribute(stem + GrouperRecentMemberships.GROUPER_RECENT_MEMBERSHIPS_ATTR_GROUP_UUID_FROM, this.readable.getId()),
        attribute(scriptName(), "entity.memberOf('secret:hidden')"))));
  }

}
