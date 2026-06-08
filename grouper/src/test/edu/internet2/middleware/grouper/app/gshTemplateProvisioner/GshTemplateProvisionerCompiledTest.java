package edu.internet2.middleware.grouper.app.gshTemplateProvisioner;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioner;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

/**
 * Tests for the compiled provisioner template type — a compiled
 * GshTemplateProvisionerBase resolved directly by GshTemplateProvisionerFactory
 * when templateMode=compiled (no GshTemplateV2 wrapper / assignGrouperProvisioner).
 *
 * GRP-7029
 */
public class GshTemplateProvisionerCompiledTest extends GrouperTest {

  /**
   * gsh template config id the provisioner points at
   */
  private static final String TEMPLATE_CONFIG_ID = "testGshTemplateConfig";

  /**
   * provisioner config id
   */
  private static final String PROVISIONER_CONFIG_ID = "testCompiledProvisioner";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GshTemplateProvisionerCompiledTest("testCompiledProvisionerResolvesViaFactory"));
    TestRunner.run(new GshTemplateProvisionerCompiledTest("testCompiledProvisionerWrongBaseThrowsClearError"));
  }

  /**
   *
   */
  public GshTemplateProvisionerCompiledTest() {
    super();
  }

  /**
   * @param name
   */
  public GshTemplateProvisionerCompiledTest(String name) {
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
   * Set up TEMPLATE_CONFIG_ID as a compiled provisioner template with the given
   * Java source, and point a provisioner config at it.
   * @param javaSource the provisioner template's Java body
   */
  private void configureProvisionerTemplate(String javaSource) {

    String templateConfigLines = GrouperUtil.readResourceIntoString("edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);

    List<String> templateConfigProperties = GrouperUtil.splitFileLines(templateConfigLines);

    for (String keyValue: templateConfigProperties) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateType", "provisioner");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".gshTemplate", javaSource);

    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put("provisioner." + PROVISIONER_CONFIG_ID + ".gshTemplateConfigId", TEMPLATE_CONFIG_ID);
  }

  /**
   * A compiled provisioner template extending GshTemplateProvisionerBase is
   * resolved directly by the factory and returned, with no GshTemplateV2 wrapper.
   */
  public void testCompiledProvisionerResolvesViaFactory() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "import edu.internet2.middleware.grouper.app.gshTemplateProvisioner.GshTemplateProvisionerBase;\n"
        + "import edu.internet2.middleware.grouper.app.provisioning.targetDao.GrouperProvisionerTargetDaoBase;\n"
        + "public class TestCompiledProvisioner extends GshTemplateProvisionerBase {\n"
        + "  protected Class<? extends GrouperProvisionerTargetDaoBase> grouperTargetDaoClass() {\n"
        + "    return GrouperProvisionerTargetDaoBase.class;\n"
        + "  }\n"
        + "}\n";

    configureProvisionerTemplate(source);

    // when
    GrouperProvisioner grouperProvisioner =
        new GshTemplateProvisionerFactory().generateGrouperProvisioner(PROVISIONER_CONFIG_ID);

    // then
    assertNotNull(grouperProvisioner);
    assertTrue("should be a GshTemplateProvisionerBase", grouperProvisioner instanceof GshTemplateProvisionerBase);
    assertEquals("edu.internet2.middleware.grouper.gshTest.TestCompiledProvisioner",
        grouperProvisioner.getClass().getName());
  }

  /**
   * A compiled provisioner template whose body extends the wrong base surfaces a
   * clear error.
   */
  public void testCompiledProvisionerWrongBaseThrowsClearError() {

    String source = ""
        + "package edu.internet2.middleware.grouper.gshTest;\n"
        + "public class TestWrongBaseProvisioner {\n"
        + "}\n";

    configureProvisionerTemplate(source);

    try {
      new GshTemplateProvisionerFactory().generateGrouperProvisioner(PROVISIONER_CONFIG_ID);
      fail("should have thrown — class does not extend GshTemplateProvisionerBase");
    } catch (RuntimeException re) {
      assertTrue("message should explain the base mismatch: " + re.getMessage(),
          re.getMessage().contains("must extend/implement"));
      assertTrue("message should name the required base: " + re.getMessage(),
          re.getMessage().contains("GshTemplateProvisionerBase"));
    }
  }

}
