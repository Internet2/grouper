package edu.internet2.middleware.grouper.hooks;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Security regression test: a compiled GSH hook template (listed in
 * hooks.group.gshTemplateConfigIds) must not run when the template is disabled
 * (grouperGshTemplate.&lt;id&gt;.enabled=false), when its templateType is not
 * hook, or when its templateMode is not compiled. Before the fix
 * GrouperHookType.rebuildCompiledHookCacheEntry resolved and ran every listed
 * id regardless, so an ordinary user could trigger a disabled hook (which may
 * run as root) through a normal group insert.
 *
 * Issue advisory H4.
 */
public class GshTemplateHookDisabledTest extends GrouperTest {

  /** counter the compiled hook increments in groupPreInsert */
  public static int preInsertCount = 0;

  /** counter the compiled hook increments in groupPostCommitInsert */
  public static int postCommitInsertCount = 0;

  /** gsh template config id of the compiled hook */
  private static final String TEMPLATE_CONFIG_ID = "testGshTemplateHookDisabled";

  /** folder for the test objects */
  private static final String STEM_NAME = "test:hookDisabledProof";

  /** group the hook modifies as root (the caller cannot modify it) */
  private static final String PROTECTED_GROUP_NAME = STEM_NAME + ":protectedTarget";

  /** description the protected group starts with */
  private static final String ORIGINAL_DESCRIPTION = "ORIGINAL_PROTECTED_DESCRIPTION";

  /**
   * hook source: counts both events and, in post commit, changes the protected
   * group's description in a root session (the attack in the security report)
   */
  private static final String HOOK_SOURCE = ""
      + "package edu.internet2.middleware.grouper.gshTest;\n"
      + "import edu.internet2.middleware.grouper.Group;\n"
      + "import edu.internet2.middleware.grouper.GroupFinder;\n"
      + "import edu.internet2.middleware.grouper.GrouperSession;\n"
      + "import edu.internet2.middleware.grouper.hooks.GroupHooks;\n"
      + "import edu.internet2.middleware.grouper.hooks.GshTemplateHookDisabledTest;\n"
      + "import edu.internet2.middleware.grouper.hooks.beans.HooksContext;\n"
      + "import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;\n"
      + "import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;\n"
      + "public class TestDisabledCompiledGroupHook extends GroupHooks {\n"
      + "  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean bean) {\n"
      + "    GshTemplateHookDisabledTest.preInsertCount++;\n"
      + "  }\n"
      + "  public void groupPostCommitInsert(HooksContext hooksContext, HooksGroupBean bean) {\n"
      + "    GshTemplateHookDisabledTest.postCommitInsertCount++;\n"
      + "    if (!bean.getGroup().getName().endsWith(\":userTrigger\")) {\n"
      + "      return;\n"
      + "    }\n"
      + "    GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {\n"
      + "      public Object callback(GrouperSession root) {\n"
      + "        Group target = GroupFinder.findByName(root, \"" + PROTECTED_GROUP_NAME + "\", true);\n"
      + "        target.setDescription(\"DISABLED_HOOK_EXECUTED\");\n"
      + "        target.store();\n"
      + "        return null;\n"
      + "      }\n"
      + "    });\n"
      + "  }\n"
      + "}\n";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateHookDisabledTest("testDisabledHookDoesNotRunForUser"));
  }

  /**
   * @param name
   */
  public GshTemplateHookDisabledTest(String name) {
    super(name);
  }

  @Override
  protected void setUp() {
    super.setUp();
    GshTemplateClassLoaderRegistry.clearCache();
    GrouperHookType.clearHooks();
  }

  @Override
  protected void tearDown() {
    GrouperHookType.clearHooks();
    super.tearDown();
  }

  /**
   * Configure TEMPLATE_CONFIG_ID as a compiled hook template and register it as
   * a group hook, then override enabled, templateType and templateMode.
   * @param enabled value for the enabled property
   * @param templateType value for templateType
   * @param templateMode value for templateMode
   */
  private void configureHookTemplate(boolean enabled, String templateType, String templateMode) {

    String templateConfigLines = GrouperUtil.readResourceIntoString(
        "edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    // the shared test template config is for id testGshTemplateConfig; copy it to our id
    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);
    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        String key = keyValueArr[0].trim().replace(".testGshTemplateConfig.", "." + TEMPLATE_CONFIG_ID + ".");
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(key, keyValueArr[1].trim());
      }
    }

    String prefix = "grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "templateType", templateType);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "templateMode", templateMode);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "enabled", Boolean.toString(enabled));
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(prefix + "gshTemplate", HOOK_SOURCE);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.group.gshTemplateConfigIds", TEMPLATE_CONFIG_ID);
  }

  /**
   * Create the folder and protected group as root; SUBJ0 gets only CREATE on the folder.
   */
  private void setupObjects() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    Stem stem = new StemSave(rootSession).assignName(STEM_NAME).assignCreateParentStemsIfNotExist(true).save();
    new GroupSave(rootSession).assignName(PROTECTED_GROUP_NAME).assignDescription(ORIGINAL_DESCRIPTION).save();
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
    GrouperSession.stopQuietly(rootSession);
  }

  /**
   * description of the protected group, read as root
   * @return the description
   */
  private String protectedDescription() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group group = GroupFinder.findByName(rootSession, PROTECTED_GROUP_NAME, true);
      return group.getDescription();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * insert a group as root and return how many times each hook method ran
   * @param extension group extension
   * @return {preInsert delta, postCommitInsert delta}
   */
  private int[] insertAsRootAndCount(String extension) {
    int pre = preInsertCount;
    int post = postCommitInsertCount;
    GrouperSession rootSession = GrouperSession.startRootSession();
    new GroupSave(rootSession).assignName(STEM_NAME + ":" + extension).assignCreateParentStemsIfNotExist(true).save();
    GrouperSession.stopQuietly(rootSession);
    return new int[] {preInsertCount - pre, postCommitInsertCount - post};
  }

  /**
   * Control: an enabled compiled hook runs both its pre-insert and post-commit
   * methods (and so the test hook really is wired up).
   */
  /**
   * every other compiled-template dispatcher goes through GshTemplateCompiledDispatch, which now refuses
   * a disabled template, or one of the wrong type for the dispatcher, before loading it (2026-11-10)
   */
  public void testDispatcherRefusesDisabledOrWrongType() {
    edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig config = null;

    // disabled daemon template, dispatched as a daemon (OtherJobScript)
    configureHookTemplate(false, "daemon", "compiled");
    config = new edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig(TEMPLATE_CONFIG_ID);
    config.populateConfiguration();
    assertDispatchRefused(config, edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateDaemon.class, "disabled");

    // enabled, but a hook template dispatched as a report
    configureHookTemplate(true, "hook", "compiled");
    config = new edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig(TEMPLATE_CONFIG_ID);
    config.populateConfiguration();
    assertDispatchRefused(config, edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateReport.class, "expected 'report'");

    // library loading: a hook template is not a library
    try {
      edu.internet2.middleware.grouper.app.gsh.template.GshTemplateCompiledDispatch.instanceForTemplate(TEMPLATE_CONFIG_ID);
      fail("a non-library template must not load as a library");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("not templateType=library"));
    }
  }

  /**
   * @param config
   * @param baseClass
   * @param expectedText
   */
  private static void assertDispatchRefused(edu.internet2.middleware.grouper.app.gsh.template.GshTemplateConfig config,
      Class<?> baseClass, String expectedText) {
    try {
      edu.internet2.middleware.grouper.app.gsh.template.GshTemplateCompiledDispatch.instantiate(TEMPLATE_CONFIG_ID, config, baseClass);
      fail("dispatch must be refused (" + expectedText + ")");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), re.getMessage().contains("cannot run here") && re.getMessage().contains(expectedText));
    }
  }

  public void testEnabledHookRuns() {
    setupObjects();
    configureHookTemplate(true, "hook", "compiled");
    GrouperHookType.clearHooks();

    int[] counts = insertAsRootAndCount("enabledControl");
    assertEquals(1, counts[0]);
    assertEquals(1, counts[1]);
  }

  /**
   * The security report's attack: the template is disabled before SUBJ0 (CREATE only)
   * creates a group; the hook must not run, so it cannot change the protected
   * group as root.
   */
  public void testDisabledHookDoesNotRunForUser() {
    setupObjects();
    configureHookTemplate(false, "hook", "compiled");
    GrouperHookType.clearHooks();

    int pre = preInsertCount;
    int post = postCommitInsertCount;

    GrouperSession userSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      Group trigger = new GroupSave(userSession).assignName(STEM_NAME + ":userTrigger").save();
      assertNotNull(trigger);
    } finally {
      GrouperSession.stopQuietly(userSession);
    }

    assertEquals("disabled hook groupPreInsert must not run", pre, preInsertCount);
    assertEquals("disabled hook groupPostCommitInsert must not run", post, postCommitInsertCount);
    assertEquals(ORIGINAL_DESCRIPTION, protectedDescription());
  }

  /**
   * Enabled, then cached, then disabled: once the compiled-hook cache TTL passes
   * the hook stops running, with no clearHooks call (as on another node).
   */
  public void testDisableAfterCachedStopsHook() {
    setupObjects();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.compiledTemplate.cacheSeconds", "0");
    configureHookTemplate(true, "hook", "compiled");
    GrouperHookType.clearHooks();

    int[] counts = insertAsRootAndCount("beforeDisable");
    assertEquals(1, counts[0]);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(
        "grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".enabled", "false");
    // let the 0-second TTL expire so the next lookup rebuilds the entry
    GrouperUtil.sleep(50);

    counts = insertAsRootAndCount("afterDisable");
    assertEquals(0, counts[0]);
    assertEquals(0, counts[1]);
  }

  /**
   * A template listed in the hooks property whose templateType is not hook must not run.
   */
  public void testWrongTemplateTypeDoesNotRun() {
    setupObjects();
    configureHookTemplate(true, "gsh", "compiled");
    GrouperHookType.clearHooks();

    int[] counts = insertAsRootAndCount("wrongType");
    assertEquals(0, counts[0]);
    assertEquals(0, counts[1]);
  }

  /**
   * A template listed in the hooks property whose templateMode is not compiled must not run.
   */
  public void testWrongTemplateModeDoesNotRun() {
    setupObjects();
    configureHookTemplate(true, "hook", "interpreted");
    GrouperHookType.clearHooks();

    int[] counts = insertAsRootAndCount("wrongMode");
    assertEquals(0, counts[0]);
    assertEquals(0, counts[1]);
  }

}
