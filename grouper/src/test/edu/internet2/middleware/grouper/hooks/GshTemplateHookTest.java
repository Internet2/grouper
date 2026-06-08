package edu.internet2.middleware.grouper.hooks;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Tests for compiled-Java hooks — a hook whose body is a compiled GSH template
 * (templateType=hook, templateMode=compiled) extending one of the hook base
 * classes, registered via hooks.&lt;domain&gt;.gshTemplateConfigIds and resolved
 * through the short-TTL compiled-hook cache in GrouperHookType.
 *
 * GRP-7032
 */
public class GshTemplateHookTest extends GrouperTest {

  /**
   * counter the compiled hook increments (referenced via the parent classloader)
   */
  public static int groupPreInsertCount = 0;

  /**
   * gsh template config id of the compiled hook
   */
  private static final String TEMPLATE_CONFIG_ID = "testGshTemplateConfig";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateHookTest("testCompiledGroupHookFires"));
  }

  /**
   * @param name
   */
  public GshTemplateHookTest(String name) {
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
   * Configure TEMPLATE_CONFIG_ID as a compiled hook template with the given Java
   * source, and register it as a group hook.
   * @param javaSource the hook template's Java body
   */
  private void configureHookTemplate(String javaSource) {

    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);

    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateType", "hook");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".gshTemplate", javaSource);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hooks.group.gshTemplateConfigIds", TEMPLATE_CONFIG_ID);
  }

  /**
   * A compiled group hook extending GroupHooks fires on group insert, alongside
   * (and the same way as) classpath hooks.
   */
  public void testCompiledGroupHookFires() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.hooks.GroupHooks;\n"
        + "import edu.internet2.middleware.grouper.hooks.GshTemplateHookTest;\n"
        + "import edu.internet2.middleware.grouper.hooks.beans.HooksContext;\n"
        + "import edu.internet2.middleware.grouper.hooks.beans.HooksGroupBean;\n"
        + "public class TestCompiledGroupHook extends GroupHooks {\n"
        + "  public void groupPreInsert(HooksContext hooksContext, HooksGroupBean preInsertBean) {\n"
        + "    GshTemplateHookTest.groupPreInsertCount++;\n"
        + "  }\n"
        + "}\n";

    configureHookTemplate(source);
    GrouperHookType.clearHooks();

    GrouperSession grouperSession = GrouperSession.startRootSession();

    int originalCount = groupPreInsertCount;

    new GroupSave(grouperSession).assignName("test:compiledHookGroup").assignCreateParentStemsIfNotExist(true).save();

    assertEquals(originalCount + 1, groupPreInsertCount);
  }

}
