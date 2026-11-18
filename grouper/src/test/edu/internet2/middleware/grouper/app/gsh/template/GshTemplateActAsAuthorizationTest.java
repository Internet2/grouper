package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <pre>
 * GSH template act-as (gshTemplateActAsSubjectLookup in the gshTemplateExec web service, which
 * calls GshTemplateExec.assignActAsSubject): a member of the template's actAsGroupUUID group who is
 * not wheel or root must not be able to run the template as root or as a member of the wheel,
 * readonly wheel or viewonly wheel group.  Acting as an ordinary subject still works, and a wheel
 * caller may act as anyone.
 *
 * Runs a V1 template (runAsType=currentUser) through GshTemplateExec, the same call the web
 * service makes, plus direct checks of GshTemplateActAsAuthorization.  Before the fix the test
 * does not compile (GshTemplateActAsAuthorization does not exist) and the refused runs succeed.
 *
 * Same file for every release (2.6.19 on): Java 8, no commons-lang import, GrouperTest base.
 * </pre>
 */
public class GshTemplateActAsAuthorizationTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateActAsAuthorizationTest("testNonWheelCallerActAsWheelMemberRefused"));
  }

  /** */
  public GshTemplateActAsAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateActAsAuthorizationTest(String name) {
    super(name);
  }

  /** config id of the sample template (test-gsh-template-config.properties) */
  private static final String CONFIG_ID = "testGshTemplateConfig";

  /** config prefix */
  private static final String PREFIX = "grouperGshTemplate." + CONFIG_ID + ".";

  /** part of the refusal message from GshTemplateActAsAuthorization */
  private static final String REFUSED_MARKER = "is not allowed to run a GSH template as";

  /** root session */
  private GrouperSession rootSession;

  /** owner folder of the template run */
  private Stem ownerStem;

  // callers and targets:
  // SUBJ0 ordinary caller, in the act-as group
  // SUBJ1 ordinary target, STEM_ADMIN on the owner folder
  // SUBJ2 ordinary, not in the act-as group
  // SUBJ3 wheel member (target)
  // SUBJ4 wheel member, in the act-as group (wheel caller)
  // SUBJ5 viewonly wheel member (target)

  @Override
  protected void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();

    // sample template scaffolding (same file GshTemplateExecTest uses)
    String templateConfigLines = GrouperUtil.readResourceIntoString(
        "edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);
    for (String keyValue : templateConfigLines.split("\\r?\\n")) {
      if (keyValue.trim().length() > 0 && keyValue.contains("=")) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "input.0.required", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "templateVersion", "V1");
    // the dangerous mode: the template session is the act-as subject
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "runAsType", "currentUser");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "gshTemplate", "int a = 5;");

    // wheel and viewonly wheel groups
    Group wheel = new GroupSave(this.rootSession).assignName("test:secPatch122wheel")
        .assignCreateParentStemsIfNotExist(true).save();
    wheel.addMember(SubjectTestHelper.SUBJ3, false);
    wheel.addMember(SubjectTestHelper.SUBJ4, false);
    Group viewonlyWheel = new GroupSave(this.rootSession).assignName("test:secPatch122viewonlyWheel")
        .assignCreateParentStemsIfNotExist(true).save();
    viewonlyWheel.addMember(SubjectTestHelper.SUBJ5, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.viewonly.group", viewonlyWheel.getName());
    PrivilegeHelper.wheelMemberCacheClear();

    // act-as callers group
    Group actAsGroup = new GroupSave(this.rootSession).assignName("test:secPatch122actAs")
        .assignCreateParentStemsIfNotExist(true).save();
    actAsGroup.addMember(SubjectTestHelper.SUBJ0, false);
    actAsGroup.addMember(SubjectTestHelper.SUBJ4, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(PREFIX + "actAsGroupUUID", actAsGroup.getUuid());

    // the template needs folder ADMIN on the owner folder (checked as the act-as subject)
    this.ownerStem = new StemSave(this.rootSession).assignName("test2").save();
    this.ownerStem.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_ADMIN, false);
  }

  @Override
  protected void tearDown() {
    PrivilegeHelper.wheelMemberCacheClear();
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------- helpers

  /**
   * run the sample template as caller acting as target
   * @param caller authenticated subject
   * @param target act-as subject
   * @return null if the run succeeded, else the error text (exception stack or output)
   */
  private String runActAs(Subject caller, Subject target) {
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId(CONFIG_ID);
    exec.assignCurrentUser(caller);
    exec.assignActAsSubject(target);
    exec.assignGshTemplateOwnerType(GshTemplateOwnerType.stem);
    exec.assignOwnerStemName(this.ownerStem.getName());
    GshTemplateInput input = new GshTemplateInput();
    input.assignName("gsh_input_myExtension");
    // assignValueString: every release has it
    input.assignValueString(null);
    exec.addGshTemplateInput(input);
    GshTemplateExecOutput output = null;
    try {
      output = exec.execute();
    } catch (RuntimeException re) {
      // some releases rethrow instead of putting the exception in the output
      return GrouperUtil.getFullStackTrace(re);
    }
    if (output != null && output.isSuccess()) {
      return null;
    }
    StringBuilder result = new StringBuilder("not success");
    if (output != null && output.getException() != null) {
      result.append(": ").append(GrouperUtil.getFullStackTrace(output.getException()));
    }
    if (output != null && output.getGshTemplateOutput() != null) {
      result.append(", output: ").append(output.getGshTemplateOutput().toString());
    }
    return result.toString();
  }

  /**
   * assert that the run was refused by the act-as target check
   * @param caller
   * @param target
   */
  private void assertActAsRefused(Subject caller, Subject target) {
    String error = runActAs(caller, target);
    assertNotNull("act-as of " + target.getId() + " by " + caller.getId() + " must be refused", error);
    assertTrue("refusal must come from the act-as target check: " + error, error.contains(REFUSED_MARKER));
  }

  /**
   * assert that the run succeeded
   * @param caller
   * @param target
   */
  private void assertActAsAllowed(Subject caller, Subject target) {
    String error = runActAs(caller, target);
    assertNull("act-as of " + target.getId() + " by " + caller.getId() + " must run: " + error, error);
  }

  // ---------------------------------------------------------------- tests

  /**
   * the reported attack, against a wheel member: a non-wheel act-as caller names a wheel member
   */
  public void testNonWheelCallerActAsWheelMemberRefused() {
    assertActAsRefused(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ3);
  }

  /**
   * the reported attack: a non-wheel act-as caller names GrouperSystem
   */
  public void testNonWheelCallerActAsRootRefused() {
    assertActAsRefused(SubjectTestHelper.SUBJ0, SubjectFinder.findRootSubject());
  }

  /**
   * a viewonly wheel member is a privileged target too
   */
  public void testNonWheelCallerActAsViewonlyWheelRefused() {
    assertActAsRefused(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ5);
  }

  /**
   * control: acting as an ordinary subject still works
   */
  public void testNonWheelCallerActAsOrdinarySubjectAllowed() {
    assertActAsAllowed(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1);
  }

  /**
   * control: a wheel caller may act as a wheel member (and as root)
   */
  public void testWheelCallerActAsWheelMemberAllowed() {
    assertActAsAllowed(SubjectTestHelper.SUBJ4, SubjectTestHelper.SUBJ3);
    assertTrue(GshTemplateActAsAuthorization.allowedToActAs(SubjectTestHelper.SUBJ4, SubjectFinder.findRootSubject()));
  }

  /**
   * control: a caller outside the act-as group is still refused by the existing group check
   */
  public void testCallerNotInActAsGroupRefused() {
    String error = runActAs(SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ1);
    assertNotNull(error);
    assertTrue(error, error.contains("is not a member of"));
  }

  /**
   * the decision class directly
   */
  public void testAllowedToActAs() {
    // privileged targets
    assertTrue(GshTemplateActAsAuthorization.isPrivilegedSubject(SubjectFinder.findRootSubject()));
    assertTrue(GshTemplateActAsAuthorization.isPrivilegedSubject(SubjectTestHelper.SUBJ3));
    assertTrue(GshTemplateActAsAuthorization.isPrivilegedSubject(SubjectTestHelper.SUBJ5));
    assertFalse(GshTemplateActAsAuthorization.isPrivilegedSubject(SubjectTestHelper.SUBJ1));

    // non-wheel caller
    assertFalse(GshTemplateActAsAuthorization.allowedToActAs(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ3));
    assertFalse(GshTemplateActAsAuthorization.allowedToActAs(SubjectTestHelper.SUBJ0, SubjectFinder.findRootSubject()));
    assertFalse(GshTemplateActAsAuthorization.allowedToActAs(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ5));
    assertTrue(GshTemplateActAsAuthorization.allowedToActAs(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1));

    // a viewonly wheel caller is not wheel: may not act as a wheel member, may act as itself
    assertFalse(GshTemplateActAsAuthorization.allowedToActAs(SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ3));
    assertTrue(GshTemplateActAsAuthorization.allowedToActAs(SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ5));

    // wheel and root callers
    assertTrue(GshTemplateActAsAuthorization.allowedToActAs(SubjectTestHelper.SUBJ4, SubjectTestHelper.SUBJ3));
    assertTrue(GshTemplateActAsAuthorization.allowedToActAs(SubjectFinder.findRootSubject(), SubjectTestHelper.SUBJ3));

    // no caller
    assertFalse(GshTemplateActAsAuthorization.allowedToActAs(null, SubjectTestHelper.SUBJ1));
  }

}
