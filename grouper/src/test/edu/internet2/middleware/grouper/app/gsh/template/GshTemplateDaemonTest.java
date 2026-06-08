package edu.internet2.middleware.grouper.app.gsh.template;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase.OtherJobInput;
import edu.internet2.middleware.grouper.app.loader.OtherJobScript;
import edu.internet2.middleware.grouper.app.loader.db.Hib3GrouperLoaderLog;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Tests for the compiled daemon template type — a compiled GrouperTemplateDaemon
 * dispatched from OtherJobScript via scriptType=compiledJava.
 *
 * GRP-7028
 */
public class GshTemplateDaemonTest extends GrouperTest {

  /**
   * gsh template config id the daemon points at
   */
  private static final String TEMPLATE_CONFIG_ID = "testGshTemplateConfig";

  /**
   * script daemon config key (job name without the OTHER_JOB_ prefix)
   */
  private static final String DAEMON_CONFIG_KEY = "testCompiledDaemon";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateDaemonTest("testCompiledDaemonRunsViaOtherJobScript"));
    TestRunner.run(new GshTemplateDaemonTest("testCompiledDaemonWrongBaseThrowsClearError"));
  }

  /**
   *
   */
  public GshTemplateDaemonTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateDaemonTest(String name) {
    super(name);
  }

  /**
   * Clear registry state between tests so cached classes don't leak.
   */
  @Override
  protected void setUp() {
    super.setUp();
    GshTemplateClassLoaderRegistry.clearCache();
  }

  /**
   * Set up TEMPLATE_CONFIG_ID as a compiled daemon template with the given Java
   * source, reusing the sample config scaffolding for the required common fields.
   * @param javaSource the daemon template's Java body
   */
  private void configureDaemonTemplate(String javaSource) {

    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);

    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateType", "daemon");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".gshTemplate", javaSource);

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob." + DAEMON_CONFIG_KEY + ".scriptType", "compiledJava");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("otherJob." + DAEMON_CONFIG_KEY + ".gshTemplateConfigId", TEMPLATE_CONFIG_ID);
  }

  /**
   * Build the OtherJobInput the OtherJobScript dispatcher expects.
   * @return the input with job name, loader log, and root session
   */
  private OtherJobInput buildOtherJobInput() {
    OtherJobInput otherJobInput = new OtherJobInput();
    otherJobInput.setJobName("OTHER_JOB_" + DAEMON_CONFIG_KEY);
    otherJobInput.setHib3GrouperLoaderLog(new Hib3GrouperLoaderLog());
    otherJobInput.setGrouperSession(GrouperSession.startRootSession());
    return otherJobInput;
  }

  /**
   * A compiled daemon template fires through OtherJobScript's compiledJava branch,
   * runs runDaemon, and can write to the loader log that the framework passes in.
   */
  public void testCompiledDaemonRunsViaOtherJobScript() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateDaemon;\n"
        + "import edu.internet2.middleware.grouper.app.loader.OtherJobTemplateInput;\n"
        + "public class TestCompiledDaemon extends GrouperTemplateDaemon {\n"
        + "  public void runDaemon(OtherJobTemplateInput otherJobTemplateInput) {\n"
        + "    otherJobTemplateInput.getHib3GrouperLoaderLog().setJobMessage(\"compiled daemon ran for \" + otherJobTemplateInput.getGshTemplateConfigId());\n"
        + "  }\n"
        + "}\n";

    configureDaemonTemplate(source);

    OtherJobInput otherJobInput = buildOtherJobInput();

    // when
    new OtherJobScript().run(otherJobInput);

    // then
    assertEquals("compiled daemon ran for " + TEMPLATE_CONFIG_ID,
        otherJobInput.getHib3GrouperLoaderLog().getJobMessage());
  }

  /**
   * A daemon template whose body extends the wrong base surfaces a clear error.
   */
  public void testCompiledDaemonWrongBaseThrowsClearError() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class TestWrongBaseDaemon {\n"
        + "}\n";

    configureDaemonTemplate(source);

    OtherJobInput otherJobInput = buildOtherJobInput();

    try {
      new OtherJobScript().run(otherJobInput);
      fail("should have thrown — class does not extend GrouperTemplateDaemon");
    } catch (RuntimeException re) {
      assertTrue("message should explain the base mismatch: " + re.getMessage(),
          re.getMessage().contains("must extend/implement"));
      assertTrue("message should name the required base: " + re.getMessage(),
          re.getMessage().contains("GrouperTemplateDaemon"));
    }
  }

}
