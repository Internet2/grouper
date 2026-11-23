package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.HashMap;
import java.util.Map;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.PrivilegeHelper;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * GRP-7598: a GSH template can be run with no owner folder or group.  allowUiFromNoOwner covers runs from the UI
 * (GshTemplateExec.assignRunFromUi(true)) and allowWsFromNoOwner covers everything else (WS, MCP, API): each run is
 * checked against the flag for where it came from, by the core validation, not just the UI screens.  Also the
 * save checks in GshTemplateConfiguration that a gsh template is runnable from somewhere.
 * <p>
 * Runs as a non-wheel subject so the checks are not skipped by the wheel / root shortcut.
 * </p>
 */
public class GshTemplateNoOwnerTest extends GrouperTest {

  /** set when the template's gshRunLogic runs */
  private static final String RAN_PROPERTY = "gshTest7598ran";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateNoOwnerTest("testUiFlagAllowsUiNotWs"));
  }

  /**
   *
   */
  public GshTemplateNoOwnerTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateNoOwnerTest(String name) {
    super(name);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    System.clearProperty(RAN_PROPERTY);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    System.clearProperty(RAN_PROPERTY);
    super.tearDown();
  }

  /**
   * interpreted V2 source whose run logic only sets a system property (no Grouper data changes)
   * @param className
   * @return the source
   */
  private static String templateSource(String className) {
    return "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2input;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateV2output;\n"
        + "public class " + className + " extends GshTemplateV2 {\n"
        + "  public void gshRunLogic(GshTemplateV2input gshTemplateV2input, GshTemplateV2output gshTemplateV2output) {\n"
        + "    System.setProperty(\"" + RAN_PROPERTY + "\", \"true\");\n"
        + "    gshTemplateV2output.getGsh_builtin_gshTemplateOutput().addOutputLine(\"success\", \"ran\");\n"
        + "  }\n"
        + "}\n";
  }

  /**
   * configure an interpreted V2 gsh template that shows on all groups (so the config is complete), with the
   * given no-owner flags and security run type
   * @param configId
   * @param allowUiFromNoOwner
   * @param allowWsFromNoOwner
   * @param securityRunType everyone or privilegeOnObject
   */
  private static void configureTemplate(String configId, boolean allowUiFromNoOwner, boolean allowWsFromNoOwner,
      String securityRunType) {
    String prefix = "grouperGshTemplate." + configId + ".";
    Map<String, String> overrides = GrouperConfig.retrieveConfig().propertiesOverrideMap();
    overrides.put(prefix + "enabled", "true");
    overrides.put(prefix + "templateType", "gsh");
    overrides.put(prefix + "templateVersion", "V2");
    overrides.put(prefix + "templateName", configId);
    overrides.put(prefix + "templateDescription", configId);
    overrides.put(prefix + "runAsType", "GrouperSystem");
    overrides.put(prefix + "numberOfInputs", "0");
    overrides.put(prefix + "gshTemplate", templateSource("GshTest7598" + configId));
    overrides.put(prefix + "securityRunType", securityRunType);
    overrides.put(prefix + "showOnGroups", "true");
    overrides.put(prefix + "groupShowType", "allGroups");
    if ("privilegeOnObject".equals(securityRunType)) {
      overrides.put(prefix + "requireGroupPrivilege", "admin");
    }
    overrides.put(prefix + GshTemplateConfig.ALLOW_UI_FROM_NO_OWNER_SUFFIX, Boolean.toString(allowUiFromNoOwner));
    overrides.put(prefix + GshTemplateConfig.ALLOW_WS_FROM_NO_OWNER_SUFFIX, Boolean.toString(allowWsFromNoOwner));
  }

  /**
   * run the template as a non-wheel subject
   * @param configId
   * @param fromUi true for a UI run (assignRunFromUi), false for a WS / MCP / API run
   * @param ownerGroup owner group, or null for no owner
   * @return the output
   */
  private static GshTemplateExecOutput execute(String configId, boolean fromUi, Group ownerGroup) {
    Subject subject = SubjectTestHelper.SUBJ0;
    assertFalse("precondition", PrivilegeHelper.isWheelOrRoot(subject));
    GshTemplateExec exec = new GshTemplateExec();
    exec.assignConfigId(configId);
    exec.assignCurrentUser(subject);
    exec.assignRunFromUi(fromUi);
    if (ownerGroup != null) {
      exec.assignGshTemplateOwnerType(GshTemplateOwnerType.group);
      exec.assignOwnerGroupName(ownerGroup.getName());
    }
    return exec.execute();
  }

  /**
   * the template ran
   * @param output
   */
  private static void assertRan(GshTemplateExecOutput output) {
    if (output.getException() != null) {
      output.getException().printStackTrace();
    }
    assertEquals(String.valueOf(output.getGshTemplateOutput().getValidationLines()),
        0, output.getGshTemplateOutput().getValidationLines().size());
    assertTrue(output.isSuccess());
    assertEquals("true", System.getProperty(RAN_PROPERTY));
    System.clearProperty(RAN_PROPERTY);
  }

  /**
   * the template was refused and none of its code ran
   * @param output
   */
  private static void assertRefused(GshTemplateExecOutput output) {
    assertTrue("should have a validation error", output.getGshTemplateOutput().getValidationLines().size() > 0);
    assertNull("gshRunLogic must not run", System.getProperty(RAN_PROPERTY));
  }

  /**
   * allowUiFromNoOwner only: a UI run with no owner runs, a WS run with no owner is refused
   */
  public void testUiFlagAllowsUiNotWs() {
    GrouperSession.startRootSession();
    configureTemplate("gsh7598ui", true, false, "everyone");

    assertRan(execute("gsh7598ui", true, null));
    assertRefused(execute("gsh7598ui", false, null));
  }

  /**
   * allowWsFromNoOwner only: a WS run with no owner runs, a UI run with no owner is refused
   */
  public void testWsFlagAllowsWsNotUi() {
    GrouperSession.startRootSession();
    configureTemplate("gsh7598ws", false, true, "everyone");

    assertRan(execute("gsh7598ws", false, null));
    assertRefused(execute("gsh7598ws", true, null));
  }

  /**
   * neither flag: no-owner runs are refused from both, and an owner still works
   */
  public void testNeitherFlagNeedsOwner() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group ownerGroup = new GroupSave(grouperSession).assignName("test:gshTest7598owner").assignCreateParentStemsIfNotExist(true).save();
    configureTemplate("gsh7598none", false, false, "everyone");

    assertRefused(execute("gsh7598none", true, null));
    assertRefused(execute("gsh7598none", false, null));
    assertRan(execute("gsh7598none", true, ownerGroup));
  }

  /**
   * privilegeOnObject has no object to check with no owner, so a no-owner run is refused even with both flags on
   */
  public void testPrivilegeOnObjectNoOwnerRefused() {
    GrouperSession.startRootSession();
    configureTemplate("gsh7598priv", true, true, "privilegeOnObject");

    assertRefused(execute("gsh7598priv", true, null));
    assertRefused(execute("gsh7598priv", false, null));
  }

  /**
   * only gsh templates can run from the UI with no owner: a provisioner template keeps allowWsFromNoOwner (always
   * true for it) and never gets allowUiFromNoOwner, even if configured
   */
  public void testProvisionerNeverUiNoOwner() {
    String prefix = "grouperGshTemplate.gsh7598prov.";
    Map<String, String> overrides = GrouperConfig.retrieveConfig().propertiesOverrideMap();
    overrides.put(prefix + "enabled", "true");
    overrides.put(prefix + "templateType", "provisioner");
    overrides.put(prefix + "templateVersion", "V2");
    overrides.put(prefix + "templateName", "gsh7598prov");
    overrides.put(prefix + "templateDescription", "gsh7598prov");
    overrides.put(prefix + "runAsType", "GrouperSystem");
    overrides.put(prefix + "numberOfInputs", "0");
    overrides.put(prefix + "gshTemplate", templateSource("GshTest7598prov"));
    overrides.put(prefix + GshTemplateConfig.ALLOW_UI_FROM_NO_OWNER_SUFFIX, "true");

    GshTemplateConfig gshTemplateConfig = new GshTemplateConfig("gsh7598prov");
    gshTemplateConfig.populateConfiguration();
    assertFalse(gshTemplateConfig.isAllowUiFromNoOwner());
    assertTrue(gshTemplateConfig.isAllowWsFromNoOwner());
    assertFalse(gshTemplateConfig.isAllowNoOwner(true));
  }

  /**
   * attributes for the save check
   * @param templateType
   * @param allowUiFromNoOwner
   * @param allowWsFromNoOwner
   * @param showOnFolders
   * @param showOnGroups
   * @param securityRunType
   * @return the attributes by config suffix
   */
  private static Map<String, GrouperConfigurationModuleAttribute> attributes(String templateType, boolean allowUiFromNoOwner,
      boolean allowWsFromNoOwner, boolean showOnFolders, boolean showOnGroups, String securityRunType) {
    Map<String, GrouperConfigurationModuleAttribute> attributes = new HashMap<String, GrouperConfigurationModuleAttribute>();
    attribute(attributes, "templateType", templateType);
    attribute(attributes, GshTemplateConfig.ALLOW_UI_FROM_NO_OWNER_SUFFIX, Boolean.toString(allowUiFromNoOwner));
    attribute(attributes, GshTemplateConfig.ALLOW_WS_FROM_NO_OWNER_SUFFIX, Boolean.toString(allowWsFromNoOwner));
    attribute(attributes, "showOnFolders", Boolean.toString(showOnFolders));
    attribute(attributes, "showOnGroups", Boolean.toString(showOnGroups));
    attribute(attributes, "securityRunType", securityRunType);
    return attributes;
  }

  /**
   * add one attribute
   * @param attributes
   * @param suffix
   * @param value
   */
  private static void attribute(Map<String, GrouperConfigurationModuleAttribute> attributes, String suffix, String value) {
    GrouperConfigurationModuleAttribute attribute = new GrouperConfigurationModuleAttribute();
    attribute.setConfigSuffix(suffix);
    attribute.setValue(value);
    attributes.put(suffix, attribute);
  }

  /**
   * run the save check
   * @param attributes
   * @return the field errors, empty if it would save
   */
  private static Map<String, String> saveErrors(Map<String, GrouperConfigurationModuleAttribute> attributes) {
    Map<String, String> validationErrorsToDisplay = new HashMap<String, String>();
    boolean ok = GshTemplateConfiguration.validateWhereTemplateRuns(attributes, validationErrorsToDisplay);
    assertEquals(ok, validationErrorsToDisplay.isEmpty());
    return validationErrorsToDisplay;
  }

  /**
   * a gsh template must be runnable from somewhere, and a no-owner-only template cannot use privilegeOnObject.
   * The error goes on the allowUiFromNoOwner field
   */
  public void testSaveChecks() {

    String errorField = "#config_" + GshTemplateConfig.ALLOW_UI_FROM_NO_OWNER_SUFFIX + "_spanid";

    // nowhere to run
    Map<String, String> errors = saveErrors(attributes("gsh", false, false, false, false, "everyone"));
    assertEquals(1, errors.size());
    assertTrue(errors.containsKey(errorField));

    // any one place to run is enough
    assertTrue(saveErrors(attributes("gsh", true, false, false, false, "everyone")).isEmpty());
    assertTrue(saveErrors(attributes("gsh", false, true, false, false, "everyone")).isEmpty());
    assertTrue(saveErrors(attributes("gsh", false, false, true, false, "everyone")).isEmpty());
    assertTrue(saveErrors(attributes("gsh", false, false, false, true, "everyone")).isEmpty());

    // blank template type means gsh
    assertEquals(1, saveErrors(attributes(null, false, false, false, false, "everyone")).size());

    // no-owner only with privilegeOnObject: no object to check, from the UI or WS flag
    errors = saveErrors(attributes("gsh", true, false, false, false, "privilegeOnObject"));
    assertEquals(1, errors.size());
    assertTrue(errors.containsKey(errorField));
    assertEquals(1, saveErrors(attributes("gsh", false, true, false, false, "privilegeOnObject")).size());

    // privilegeOnObject is fine when the template also shows on folders or groups
    assertTrue(saveErrors(attributes("gsh", true, true, false, true, "privilegeOnObject")).isEmpty());

    // other template types do not use these settings
    assertTrue(saveErrors(attributes("daemon", false, false, false, false, null)).isEmpty());
    assertTrue(saveErrors(attributes("provisioner", false, false, false, false, null)).isEmpty());
  }
}
