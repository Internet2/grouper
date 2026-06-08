package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Tests for the compiled custom-UI template type — a compiled
 * GrouperTemplateCustomUi resolved by CustomUiContainer's gshRunJoinScript /
 * gshRunLeaveScript bridge methods when templateMode=compiled.
 *
 * GRP-7030
 */
public class CustomUiContainerCompiledTest extends GrouperTest {

  /**
   * counters the compiled template increments (referenced via the parent
   * classloader)
   */
  public static int joinCount = 0;

  /**
   * leave counter
   */
  public static int leaveCount = 0;

  /**
   * gsh template config id
   */
  private static final String TEMPLATE_CONFIG_ID = "testGshTemplateConfig";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new CustomUiContainerCompiledTest("testCompiledCustomUiRunsJoinAndLeave"));
  }

  /**
   * @param name
   */
  public CustomUiContainerCompiledTest(String name) {
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
   * Configure TEMPLATE_CONFIG_ID as a compiled customUi template with the given
   * Java source, reusing the sample config scaffolding.
   * @param javaSource the custom-UI template's Java body
   */
  private void configureCustomUiTemplate(String javaSource) {

    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);

    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateType", "customUi");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".gshTemplate", javaSource);
  }

  /**
   * A compiled custom-UI template is resolved by the container bridge methods,
   * and runOnJoin / runOnLeave are invoked for the respective actions.
   */
  public void testCompiledCustomUiRunsJoinAndLeave() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.grouperUi.beans.ui.CustomUiContainerCompiledTest;\n"
        + "import edu.internet2.middleware.grouper.grouperUi.beans.ui.CustomUiTemplateInput;\n"
        + "import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperTemplateCustomUi;\n"
        + "public class TestCompiledCustomUi extends GrouperTemplateCustomUi {\n"
        + "  public void runOnJoin(CustomUiTemplateInput input) {\n"
        + "    CustomUiContainerCompiledTest.joinCount++;\n"
        + "  }\n"
        + "  public void runOnLeave(CustomUiTemplateInput input) {\n"
        + "    CustomUiContainerCompiledTest.leaveCount++;\n"
        + "  }\n"
        + "}\n";

    configureCustomUiTemplate(source);

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Group group = new GroupSave(grouperSession).assignName("test:customUiGroup").assignCreateParentStemsIfNotExist(true).save();

    int originalJoin = joinCount;
    int originalLeave = leaveCount;

    CustomUiContainer customUiContainer = new CustomUiContainer();

    customUiContainer.gshRunJoinScript(group, SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ0, TEMPLATE_CONFIG_ID);
    assertEquals(originalJoin + 1, joinCount);
    assertEquals(originalLeave, leaveCount);

    customUiContainer.gshRunLeaveScript(group, SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ0, TEMPLATE_CONFIG_ID);
    assertEquals(originalJoin + 1, joinCount);
    assertEquals(originalLeave + 1, leaveCount);
  }

}
