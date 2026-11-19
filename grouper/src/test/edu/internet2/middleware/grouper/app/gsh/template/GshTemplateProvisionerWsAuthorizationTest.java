package edu.internet2.middleware.grouper.app.gsh.template;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Security advisory H4: a GSH template of type provisioner runs as GrouperSystem and has no securityRunType (null).
 * GshTemplateValidationService.validateSecurityRunType treated null like "everyone", so any caller of
 * GshTemplateExec.execute() (e.g. the WS gshTemplateExec operation, with no owner) could run it.  It must be
 * wheel / root only, and refused before any template code (decorate, gshRunLogic) runs.
 * <p>
 * The WS operation (GrouperServiceLogic.executeGshTemplate) builds a GshTemplateExec with the caller as current
 * user and calls execute(), so this test calls execute() directly (the WS logic class moved between releases).
 * </p>
 */
public class GshTemplateProvisionerWsAuthorizationTest extends GrouperTest {

  /** template config id */
  private static final String CONFIG_ID = "gshTest23provisioner";

  /** set when the template's decorateTemplateForUiDisplay runs */
  private static final String DECORATED_PROPERTY = "gshTest23decorated";

  /** set when the template's gshRunLogic runs */
  private static final String RAN_PROPERTY = "gshTest23ran";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateProvisionerWsAuthorizationTest("testProvisionerTemplateNotRunnableByNonWheel"));
  }

  /**
   *
   */
  public GshTemplateProvisionerWsAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateProvisionerWsAuthorizationTest(String name) {
    super(name);
  }

  /**
   * interpreted V2 source whose decorate and run logic only set a system property (no Grouper data changes)
   * @param className
   * @return the source
   */
  private static String templateSource(String className) {
    return "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateDecorateForUiInput;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class " + className + " extends GshTemplateV2 {\n"
        + "  public void decorateTemplateForUiDisplay(GshTemplateDecorateForUiInput gshTemplateDecorateForUiInput) {\n"
        + "    System.setProperty(\"" + DECORATED_PROPERTY + "\", \"true\");\n"
        + "  }\n"
        + "  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {\n"
        + "    System.setProperty(\"" + RAN_PROPERTY + "\", \"true\");\n"
        + "    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine(\"success\", \"ran\");\n"
        + "  }\n"
        + "}\n";
  }

  /**
   * configure an interpreted V2 template
   * @param configId
   * @param templateType gsh or provisioner
   */
  private static void configureTemplate(String configId, String templateType) {
    String prefix = "grouperGshTemplate." + configId + ".";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "enabled", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "templateType", templateType);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "templateVersion", "V2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "templateName", configId);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "templateDescription", configId);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "runAsType", "GrouperSystem");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "numberOfInputs", "0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "gshTemplate", templateSource("GshTest23" + configId));
    if ("gsh".equals(templateType)) {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "securityRunType", "everyone");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "showOnGroups", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "groupShowType", "allGroups");
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    System.clearProperty(DECORATED_PROPERTY);
    System.clearProperty(RAN_PROPERTY);
    configureTemplate(CONFIG_ID, "provisioner");
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
   * run the template the way the WS operation does: caller as current user, optional owner
   * @param configId
   * @param subject
   * @param ownerGroup or null for no owner
   * @return output
   */
  private static GshTemplateExecOutput execute(String configId, Subject subject, Group ownerGroup) {
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId(configId);
    exec.assignCurrentUser(subject);
    if (ownerGroup != null) {
      exec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
      exec.assignOwnerGroupName(ownerGroup.getName());
    }
    return exec.execute();
  }

  /**
   * whether this release reads templateType, i.e. has working provisioner templates (5.18.0, GRP-6106).
   * 4.26.0 has the provisioner enum value, but GshTemplateConfig.populateConfiguration never reads
   * templateType, so every template is a gsh template and securityRunType is required: a provisioner
   * template with no securityRunType (this issue) cannot be configured there and runs for nobody.
   * @return true if provisioner templates work here
   */
  private static boolean provisionerTypeSupported() {
    GshTemplateConfig gshTemplateConfig = new GshTemplateConfig(CONFIG_ID);
    try {
      gshTemplateConfig.populateConfiguration();
    } catch (RuntimeException re) {
      if (GrouperUtil.getFullStackTrace(re).contains(CONFIG_ID + ".securityRunType")) {
        return false;
      }
      throw re;
    }
    // by reflection: GshTemplateType (and getGshTemplateType) do not exist before 4.18 (e.g. 4.17.7),
    // where there are no provisioner templates at all
    try {
      Object type = GshTemplateConfig.class.getMethod("getGshTemplateType").invoke(gshTemplateConfig);
      return type != null && "provisioner".equals(type.toString());
    } catch (NoSuchMethodException nsme) {
      return false;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * on a release without provisioner templates: running it is refused for this caller (the required
   * securityRunType is missing) and none of its code runs
   * @param subject
   * @param ownerGroup or null
   */
  private static void assertRefusedWithoutProvisionerType(Subject subject, Group ownerGroup) {
    String stack = null;
    try {
      GshTemplateExecOutput output = execute(CONFIG_ID, subject, ownerGroup);
      if (output.getException() != null) {
        stack = GrouperUtil.getFullStackTrace(output.getException());
      } else {
        assertFalse("must not succeed", output.isSuccess());
      }
    } catch (RuntimeException re) {
      stack = GrouperUtil.getFullStackTrace(re);
    }
    if (stack != null) {
      assertTrue(stack, stack.contains(".securityRunType") || stack.contains("is not allowed to run this gsh template"));
    }
    assertNull("decorate must not run", System.getProperty(DECORATED_PROPERTY));
    assertNull("gshRunLogic must not run", System.getProperty(RAN_PROPERTY));
  }

  /**
   * assert the template was refused and none of its code ran
   * @param output
   */
  private static void assertRefusedAndNotRun(GshTemplateExecOutput output) {
    assertTrue("should have a validation error", output.getGshTemplateOutput().getValidationLines().size() > 0);
    assertEquals("template should not produce output", 0, output.getGshTemplateOutput().getOutputLines().size());
    assertNull("decorate must not run", System.getProperty(DECORATED_PROPERTY));
    assertNull("gshRunLogic must not run", System.getProperty(RAN_PROPERTY));
  }

  /**
   * an ordinary caller with no owner (the WS reproduction) is refused, and no template code runs
   */
  public void testProvisionerTemplateNotRunnableByNonWheel() {

    GrouperSession.startRootSession();
    assertFalse("precondition", PrivilegeHelper.isWheelOrRoot(SubjectTestHelper.SUBJ0));

    if (!provisionerTypeSupported()) {
      assertRefusedWithoutProvisionerType(SubjectTestHelper.SUBJ0, null);
      return;
    }
    GshTemplateExecOutput output = execute(CONFIG_ID, SubjectTestHelper.SUBJ0, null);
    assertRefusedAndNotRun(output);
  }

  /**
   * supplying an owner does not get around it
   */
  public void testProvisionerTemplateWithOwnerNotRunnableByNonWheel() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ownerGroup = new GroupSave(grouperSession).assignName("test:gshTest23owner").assignCreateParentStemsIfNotExist(true).save();

    if (!provisionerTypeSupported()) {
      assertRefusedWithoutProvisionerType(SubjectTestHelper.SUBJ0, ownerGroup);
      return;
    }

    // the owner check runs first: a provisioner template is not allowed on groups, and
    // GshTemplateValidationService.validateOwnerType throws "ownerGroup ... is not allowed to run this gsh template"
    // (existing 7.9.0 behavior) instead of adding a validation line.  Either way is a refusal; what matters is
    // that no template code ran.
    try {
      GshTemplateExecOutput output = execute(CONFIG_ID, SubjectTestHelper.SUBJ0, ownerGroup);
      assertRefusedAndNotRun(output);
    } catch (RuntimeException re) {
      // check the whole chain in case the exception gets wrapped
      String stack = GrouperUtil.getFullStackTrace(re);
      assertTrue(stack, stack.contains("is not allowed to run this gsh template"));
      assertNull("decorate must not run", System.getProperty(DECORATED_PROPERTY));
      assertNull("gshRunLogic must not run", System.getProperty(RAN_PROPERTY));
    }
  }

  /**
   * the root subject (what GshTemplateProvisionerFactory uses) and wheel members can still run it
   */
  public void testProvisionerTemplateRunnableByRootAndWheel() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    if (!provisionerTypeSupported()) {
      // 4.26.0: no working provisioner template type, so there is nothing for root or wheel to run
      return;
    }

    // root: the internal provisioning path
    GshTemplateExecOutput output = execute(CONFIG_ID, SubjectFinder.findRootSubject(), null);
    if (output.getException() != null) {
      output.getException().printStackTrace();
    }
    assertEquals(0, output.getGshTemplateOutput().getValidationLines().size());
    assertTrue(output.isSuccess());
    assertEquals("true", System.getProperty(RAN_PROPERTY));

    // wheel member
    System.clearProperty(RAN_PROPERTY);
    Group wheelGroup = new GroupSave(grouperSession).assignName("etc:gshTest23wheel").assignCreateParentStemsIfNotExist(true).save();
    wheelGroup.addMember(SubjectTestHelper.SUBJ1, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheelGroup.getName());
    assertTrue("precondition", PrivilegeHelper.isWheelOrRoot(SubjectTestHelper.SUBJ1));

    output = execute(CONFIG_ID, SubjectTestHelper.SUBJ1, null);
    if (output.getException() != null) {
      output.getException().printStackTrace();
    }
    assertEquals(0, output.getGshTemplateOutput().getValidationLines().size());
    assertTrue(output.isSuccess());
    assertEquals("true", System.getProperty(RAN_PROPERTY));
  }

  /**
   * control: a gsh template with securityRunType everyone still runs for an ordinary user
   */
  public void testEveryoneGshTemplateStillRunsForNonWheel() {

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ownerGroup = new GroupSave(grouperSession).assignName("test:gshTest23owner").assignCreateParentStemsIfNotExist(true).save();

    configureTemplate("gshTest23everyone", "gsh");

    GshTemplateExecOutput output = execute("gshTest23everyone", SubjectTestHelper.SUBJ0, ownerGroup);
    if (output.getException() != null) {
      output.getException().printStackTrace();
    }
    assertEquals(0, output.getGshTemplateOutput().getValidationLines().size());
    assertTrue(output.isSuccess());
    assertEquals("true", System.getProperty(RAN_PROPERTY));
  }
}
