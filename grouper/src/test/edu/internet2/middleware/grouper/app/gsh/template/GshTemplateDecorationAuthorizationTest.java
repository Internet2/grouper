package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory H4: the UI form render (UiV2Template.newTemplate) loaded a custom V2 template and ran its
 * decorateTemplateForUiDisplay for any templateType in the request, without the enabled / owner / who-can-run
 * checks.  The fix is GshTemplateValidationService.validatePreflight, which the UI calls before loading the
 * template, and (for releases before 7.7.0) running the same checks before decorate in validate().
 * <p>
 * The UI wiring is not unit tested (it needs the UI filter thread locals); see the security report for the manual
 * check with the direct URL.
 * </p>
 */
public class GshTemplateDecorationAuthorizationTest extends GrouperTest {

  /** template config id */
  private static final String CONFIG_ID = "gshTest24template";

  /** set when the template's decorateTemplateForUiDisplay runs */
  private static final String DECORATED_PROPERTY = "gshTest24decorated";

  /** set when the template's gshRunLogic runs */
  private static final String RAN_PROPERTY = "gshTest24ran";

  /** owner group the user can VIEW */
  private Group ownerGroup;

  /** another group */
  private Group otherGroup;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateDecorationAuthorizationTest("testPreflightSecurityRunType"));
  }

  /**
   *
   */
  public GshTemplateDecorationAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateDecorationAuthorizationTest(String name) {
    super(name);
  }

  /**
   * @param key after grouperGshTemplate.configId.
   * @param value
   */
  private static void config(String key, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + CONFIG_ID + "." + key, value);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    System.clearProperty(DECORATED_PROPERTY);
    System.clearProperty(RAN_PROPERTY);

    GrouperSession grouperSession = GrouperSession.startRootSession();
    this.ownerGroup = new GroupSave(grouperSession).assignName("test:gshTest24owner").assignCreateParentStemsIfNotExist(true).save();
    this.otherGroup = new GroupSave(grouperSession).assignName("test:gshTest24other").assignCreateParentStemsIfNotExist(true).save();
    // the user can see the owner group, like a group they created
    this.ownerGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

    // interpreted V2 template like the reproduction: decorate and run logic only set a system property
    config("enabled", "true");
    config("templateType", "gsh");
    config("templateVersion", "V2");
    config("templateName", CONFIG_ID);
    config("templateDescription", CONFIG_ID);
    config("runAsType", "currentUser");
    config("securityRunType", "wheel");
    config("showOnGroups", "true");
    config("groupShowType", "allGroups");
    config("numberOfInputs", "0");
    config("gshTemplate", "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateDecorateForUiInput;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class GshTest24DecoratorProof extends GshTemplateV2 {\n"
        + "  public void decorateTemplateForUiDisplay(GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput) {\n"
        + "    System.setProperty(\"" + DECORATED_PROPERTY + "\", \"true\");\n"
        + "  }\n"
        + "  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {\n"
        + "    System.setProperty(\"" + RAN_PROPERTY + "\", \"true\");\n"
        + "    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine(\"success\", \"ran\");\n"
        + "  }\n"
        + "}\n");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    System.clearProperty(DECORATED_PROPERTY);
    System.clearProperty(RAN_PROPERTY);
    super.tearDown();
  }

  /**
   * run the preflight the way UiV2Template does for a group owner
   * @param subject
   * @param group
   * @return true if allowed
   */
  private static boolean preflight(Subject subject, Group group) {
    return preflight(subject, group.getName(), new GshTemplateOutput());
  }

  /**
   * run the preflight the way UiV2Template does for a group owner, by group name (which may not exist)
   * @param subject
   * @param groupName
   * @param gshTemplateOutput validation lines are added here
   * @return true if allowed
   */
  private static boolean preflight(Subject subject, String groupName, GshTemplateOutput gshTemplateOutput) {
    GshTemplateExec gshTemplateExec = new GshTemplateExec();
    gshTemplateExec.assignConfigId(CONFIG_ID);
    gshTemplateExec.assignCurrentUser(subject);
    gshTemplateExec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
    gshTemplateExec.assignOwnerGroupName(groupName);

    GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(CONFIG_ID);
    gshTemplateConfig.populateConfiguration();
    gshTemplateConfig.setCurrentUser(subject);

    boolean allowed = new GshTemplateValidationService().validatePreflight(gshTemplateConfig, gshTemplateExec, gshTemplateOutput);
    if (!allowed) {
      assertTrue("a denial says why", gshTemplateOutput.getValidationLines().size() > 0);
    }
    return allowed;
  }

  /**
   * the preflight itself never loads or runs template code
   */
  private static void assertNoTemplateCodeRan() {
    assertNull("decorate must not run", System.getProperty(DECORATED_PROPERTY));
    assertNull("gshRunLogic must not run", System.getProperty(RAN_PROPERTY));
  }

  /**
   * wheel, specifiedGroup, privilegeOnObject and everyone
   */
  public void testPreflightSecurityRunType() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    assertFalse("precondition", PrivilegeHelper.isWheelOrRoot(SubjectTestHelper.SUBJ0));

    // wheel: an ordinary user is denied, root is allowed
    assertFalse(preflight(SubjectTestHelper.SUBJ0, this.ownerGroup));
    assertTrue(preflight(SubjectFinder.findRootSubject(), this.ownerGroup));

    // specifiedGroup: only members of the run group
    Group runGroup = new GroupSave(grouperSession).assignName("test:gshTest24runners").assignCreateParentStemsIfNotExist(true).save();
    runGroup.addMember(SubjectTestHelper.SUBJ1, false);
    config("securityRunType", "specifiedGroup");
    config("groupUuidCanRun", runGroup.getName());
    assertFalse(preflight(SubjectTestHelper.SUBJ0, this.ownerGroup));
    assertTrue(preflight(SubjectTestHelper.SUBJ1, this.ownerGroup));

    // privilegeOnObject: VIEW is not enough when ADMIN is required
    config("securityRunType", "privilegeOnObject");
    config("requireGroupPrivilege", "admin");
    assertFalse(preflight(SubjectTestHelper.SUBJ0, this.ownerGroup));
    this.ownerGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
    assertTrue(preflight(SubjectTestHelper.SUBJ0, this.ownerGroup));

    // everyone
    config("securityRunType", "everyone");
    assertTrue(preflight(SubjectTestHelper.SUBJ2, this.ownerGroup));

    assertNoTemplateCodeRan();
  }

  /**
   * a disabled template is denied even when everyone may run it
   */
  public void testPreflightDisabled() {

    GrouperSession.startRootSession();
    config("securityRunType", "everyone");
    assertTrue(preflight(SubjectTestHelper.SUBJ0, this.ownerGroup));

    config("enabled", "false");
    assertFalse(preflight(SubjectTestHelper.SUBJ0, this.ownerGroup));
    assertFalse("disabled is disabled for root too", preflight(SubjectFinder.findRootSubject(), this.ownerGroup));

    assertNoTemplateCodeRan();
  }

  /**
   * a group outside the template's show scope is denied (validateOwnerType throws, which counts as denied)
   */
  public void testPreflightOwnerOutOfScope() {

    GrouperSession.startRootSession();
    config("securityRunType", "everyone");
    config("groupShowType", "certainGroups");
    config("groupUuidsToShow", this.otherGroup.getName());

    assertFalse(preflight(SubjectTestHelper.SUBJ0, this.ownerGroup));
    assertTrue(preflight(SubjectTestHelper.SUBJ0, this.otherGroup));

    assertNoTemplateCodeRan();
  }

  /**
   * validation lines as one string, to compare two denials
   * @param gshTemplateOutput
   * @return the texts
   */
  private static String validationText(GshTemplateOutput gshTemplateOutput) {
    StringBuilder result = new StringBuilder();
    for (GshValidationLine gshValidationLine : gshTemplateOutput.getValidationLines()) {
      result.append(gshValidationLine.getText()).append("\n");
    }
    return result.toString();
  }

  /**
   * open item 2026-11-12: who may run the template is checked before the owner is looked up as root, so a caller
   * who may not run a wheel template gets the same answer for a private group and a missing one; and an everyone or
   * specifiedGroup template needs VIEW on the owner group
   */
  public void testPreflightOwnerOracleAndView() {

    GrouperSession.startRootSession();

    // make the other group private: test.subject.0 and test.subject.1 cannot VIEW it
    Subject everyEntity = SubjectFinder.findAllSubject();
    this.otherGroup.revokePriv(everyEntity, AccessPrivilege.VIEW, false);
    this.otherGroup.revokePriv(everyEntity, AccessPrivilege.READ, false);
    assertFalse("precondition", this.otherGroup.canHavePrivilege(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW.getName(), false));
    assertFalse("precondition", this.otherGroup.canHavePrivilege(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW.getName(), false));

    // wheel: private group and missing group look the same to a non-wheel caller
    GshTemplateOutput privateOutput = new GshTemplateOutput();
    assertFalse(preflight(SubjectTestHelper.SUBJ0, this.otherGroup.getName(), privateOutput));
    GshTemplateOutput missingOutput = new GshTemplateOutput();
    assertFalse(preflight(SubjectTestHelper.SUBJ0, "test:gshTest24doesNotExist", missingOutput));
    assertEquals("no existence oracle", validationText(missingOutput), validationText(privateOutput));

    // specifiedGroup: a member of the run group still needs VIEW on the owner group
    Group runGroup = new GroupSave(GrouperSession.staticGrouperSession()).assignName("test:gshTest24runners").assignCreateParentStemsIfNotExist(true).save();
    runGroup.addMember(SubjectTestHelper.SUBJ1, false);
    config("securityRunType", "specifiedGroup");
    config("groupUuidCanRun", runGroup.getName());
    assertFalse("no VIEW on the owner", preflight(SubjectTestHelper.SUBJ1, this.otherGroup));
    this.otherGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.VIEW, false);
    assertTrue("VIEW on the owner", preflight(SubjectTestHelper.SUBJ1, this.otherGroup));

    // everyone: same rule, and root can use any group
    config("securityRunType", "everyone");
    assertFalse("no VIEW on the owner", preflight(SubjectTestHelper.SUBJ0, this.otherGroup));
    assertTrue("VIEW on the owner", preflight(SubjectTestHelper.SUBJ0, this.ownerGroup));
    assertTrue(preflight(SubjectFinder.findRootSubject(), this.otherGroup));

    assertNoTemplateCodeRan();
  }

  /**
   * execution: a caller who may not run the template is refused before decorate runs (the decorate-after-checks
   * order, GRP-7448 commit 2, which the older file sets backport)
   */
  public void testExecuteDeniedDoesNotDecorate() {

    GrouperSession.startRootSession();

    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId(CONFIG_ID);
    exec.assignCurrentUser(SubjectTestHelper.SUBJ0);
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
    exec.assignOwnerGroupName(this.ownerGroup.getName());

    GshTemplateExecOutput output = exec.execute();

    assertTrue("should have a validation error", output.getGshTemplateOutput().getValidationLines().size() > 0);
    assertEquals(0, output.getGshTemplateOutput().getOutputLines().size());
    assertNoTemplateCodeRan();
  }
}
