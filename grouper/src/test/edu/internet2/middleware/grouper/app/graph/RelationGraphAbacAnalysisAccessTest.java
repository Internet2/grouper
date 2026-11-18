package edu.internet2.middleware.grouper.app.graph;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfigInApi;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * The visualization (RelationGraph) runs the scripted-group (ABAC) analysis of a group only for
 * users who could open the group's loader Analyze screen, not for everyone who can VIEW the group.
 * Goes in grouper/src/test/edu/internet2/middleware/grouper/app/graph/.
 */
public class RelationGraphAbacAnalysisAccessTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new RelationGraphAbacAnalysisAccessTest("testGraphHidesAnalysisFromViewer"));
  }

  /**
   *
   */
  public RelationGraphAbacAnalysisAccessTest() {
    super();
  }

  /**
   * @param name
   */
  public RelationGraphAbacAnalysisAccessTest(String name) {
    super(name);
  }

  /** the scripted group being visualized */
  private Group abacGroup;

  /** the group the script references */
  private Group referencedGroup;

  /** members of this group may edit any loader they can VIEW (uiV2.loader.edit.if.in.group) */
  private Group loaderEditors;

  /** scripted group editors (grouper.abac.edit.if.in.group) */
  private Group abacEditors;

  /** recent memberships loader editors (grouper.recentMemberships.edit.if.in.group) */
  private Group recentMembershipsEditors;

  /**
   * groups and privileges, as root:
   * SUBJ0 VIEW on the scripted group, SUBJ1 ADMIN, SUBJ2 VIEW and in the loader editors group,
   * SUBJ3 in the loader editors group with no privilege on the scripted group, SUBJ5 ADMIN and a
   * scripted group editor
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.edit.if.in.group", "");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.recentMemberships.edit.if.in.group", "");
    assignLoaderEditGroup("");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.abacGroup = new GroupSave(grouperSession).assignName("test:abac196").assignCreateParentStemsIfNotExist(true).save();
      this.abacGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      this.abacGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);
      this.abacGroup.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.VIEW, false);
      this.abacGroup.grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.ADMIN, false);

      this.referencedGroup = new GroupSave(grouperSession).assignName("test:referenced196").assignCreateParentStemsIfNotExist(true).save();
      this.referencedGroup.addMember(SubjectTestHelper.SUBJ4, false);
      // everyone in the test may read it, so only the visualization gate decides
      for (Subject subject : new Subject[] {SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ5}) {
        this.referencedGroup.grantPriv(subject, AccessPrivilege.READ, false);
      }

      this.loaderEditors = new GroupSave(grouperSession).assignName("test:loaderEditors196").assignCreateParentStemsIfNotExist(true).save();
      this.loaderEditors.addMember(SubjectTestHelper.SUBJ2, false);
      this.loaderEditors.addMember(SubjectTestHelper.SUBJ3, false);

      this.abacEditors = new GroupSave(grouperSession).assignName("test:abacEditors196").assignCreateParentStemsIfNotExist(true).save();
      this.abacEditors.addMember(SubjectTestHelper.SUBJ5, false);
      this.recentMembershipsEditors = new GroupSave(grouperSession).assignName("test:recentEditors196").assignCreateParentStemsIfNotExist(true).save();

      // the sql cache the analysis counts read, as the daemons would
      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_sqlCacheFullSync");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * remove overrides
   */
  @Override
  protected void tearDown() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.create.grant.all.read");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.create.grant.all.view");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.abac.edit.if.in.group");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.recentMemberships.edit.if.in.group");
    assignLoaderEditGroup(null);
    super.tearDown();
  }

  /**
   * the check: root and the group's ABAC loader editor yes, a viewer no, null no; with
   * uiV2.loader.edit.if.in.group a member who can VIEW the group yes, a member who cannot no
   */
  public void testCanRunAbacAnalysis() {
    assertFalse("viewer", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectTestHelper.SUBJ0, this.abacGroup));
    assertTrue("ADMIN on the group", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectTestHelper.SUBJ1, this.abacGroup));
    assertTrue("root", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectFinder.findRootSubject(), this.abacGroup));
    assertFalse("null subject", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(null, this.abacGroup));
    assertFalse("null group", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectFinder.findRootSubject(), null));
    assertFalse("loader editor group not configured", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectTestHelper.SUBJ2, this.abacGroup));

    assignLoaderEditGroup(this.loaderEditors.getName());
    assertTrue("loader editor who can VIEW: " + loaderEditorDiagnostics(SubjectTestHelper.SUBJ2),
        RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectTestHelper.SUBJ2, this.abacGroup));
    assertFalse("loader editor who cannot VIEW", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectTestHelper.SUBJ3, this.abacGroup));
    assertFalse("viewer not in the loader editor group", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectTestHelper.SUBJ0, this.abacGroup));

    assignLoaderEditGroup("test:doesNotExist196");
    assertFalse("loader editor group missing", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectTestHelper.SUBJ2, this.abacGroup));
  }

  /**
   * set (or with null clear) uiV2.loader.edit.if.in.group for this thread.  The base config sets it
   * with .elConfig (etc:loaderEditors), and an .elConfig key is read before any override of the plain
   * key, so the .elConfig key is overridden too (a value with no ${ is used as is)
   * @param groupName group name, "" for not configured, null to remove the overrides
   */
  private static void assignLoaderEditGroup(String groupName) {
    java.util.Map<String, String> overrides = GrouperUiConfigInApi.retrieveConfig().propertiesThreadLocalOverrideMap();
    for (String key : new String[] {RelationGraphAbacAnalysisAccess.UI_LOADER_EDIT_IF_IN_GROUP,
        RelationGraphAbacAnalysisAccess.UI_LOADER_EDIT_IF_IN_GROUP + ".elConfig"}) {
      if (groupName == null) {
        overrides.remove(key);
      } else {
        overrides.put(key, groupName);
      }
    }
  }

  /**
   * what the loader editor path of the check sees, as root, for a failure message
   * @param subject
   * @return config value, VIEW on the scripted group, membership in the loader editors group
   */
  private String loaderEditorDiagnostics(final Subject subject) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      return "config=" + GrouperUiConfigInApi.retrieveConfig().propertyValueString(RelationGraphAbacAnalysisAccess.UI_LOADER_EDIT_IF_IN_GROUP)
          + ", canView=" + this.abacGroup.canHavePrivilege(subject, AccessPrivilege.VIEW.getName(), false)
          + ", inLoaderEditors=" + this.loaderEditors.hasMember(subject);
    } catch (RuntimeException re) {
      return "diagnostics failed: " + re;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * the graph of a scripted group shows the referenced group (an analysis node) for the editor and
   * not for the viewer
   */
  public void testGraphHidesAnalysisFromViewer() {
    String script = "entity.memberOf('" + this.referencedGroup.getName() + "')";

    assertNotNull("editor sees the analysis node (else this test proves nothing)",
        referencedNode(SubjectTestHelper.SUBJ1, script));
    assertNull("viewer does not", referencedNode(SubjectTestHelper.SUBJ0, script));
  }

  /**
   * when scripted group and recent memberships editing are limited to editor groups, a group ADMIN
   * in neither group does not get the analysis (the Analyze screen refuses them too); an ADMIN in
   * the scripted group editors group does.  (The UI loads a saved script only for group ADMINs, so
   * this is the case the saved-script graph can reach; the override script stands in for it.)
   */
  public void testGraphHidesAnalysisFromAdminWhoIsNotAnEditor() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.edit.if.in.group", this.abacEditors.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.recentMemberships.edit.if.in.group", this.recentMembershipsEditors.getName());

    assertFalse("ADMIN, not an editor", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectTestHelper.SUBJ1, this.abacGroup));
    assertTrue("ADMIN and scripted group editor", RelationGraphAbacAnalysisAccess.canRunAbacAnalysis(SubjectTestHelper.SUBJ5, this.abacGroup));

    String script = "entity.memberOf('" + this.referencedGroup.getName() + "')";
    assertNotNull("scripted group editor sees the analysis node", referencedNode(SubjectTestHelper.SUBJ5, script));
    assertNull("ADMIN who is not an editor does not", referencedNode(SubjectTestHelper.SUBJ1, script));
  }

  /**
   * build the graph of the scripted group as a subject, with the script as the group's script
   * @param subject
   * @param script
   * @return the node of the referenced group, or null
   */
  private GraphNode referencedNode(Subject subject, String script) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      RelationGraph relationGraph = new RelationGraph()
          .assignStartObject(this.abacGroup)
          .assignParentLevels(0)
          .assignChildLevels(2)
          .assignShowAllMemberCounts(true)
          .assignOverrideAbacScript(script);
      relationGraph.build();
      return relationGraph.getNode(this.referencedGroup);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
