package edu.internet2.middleware.grouper.app.externalSystem;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateClassLoaderRegistry;
import edu.internet2.middleware.grouper.app.gsh.template.GshTemplateExternalSystemConfigIds;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.util.GrouperHttpClient;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import junit.textui.TestRunner;

/**
 * Tests for the web service external system's httpAuthnType=dynamic, where a compiled GSH
 * template of templateType=externalSystem (extends GrouperTemplateExternalSystem) adds the
 * authentication to each request.
 *
 * GRP-7593
 */
public class WsBearerTokenExternalSystemDynamicTest extends GrouperTest {

  /** web service external system config id */
  private static final String WS_CONFIG_ID = "testDynamicWs";

  /** config prefix of the web service external system */
  private static final String WS_PREFIX = "grouper.wsBearerToken." + WS_CONFIG_ID + ".";

  /** GSH template config id; matches the scaffold in test-gsh-template-config.properties */
  private static final String TEMPLATE_CONFIG_ID = "testGshTemplateConfig";

  /**
   * template that adds a bearer token naming the external system and counting its calls, so a
   * test can tell whether the same instance was reused
   */
  private static final String TEMPLATE_SOURCE = ""
      + "package edu.internet2.middleware.grouper.gshTest;\n"
      + "import edu.internet2.middleware.grouper.app.gsh.template.GrouperTemplateExternalSystem;\n"
      + "import edu.internet2.middleware.grouper.util.GrouperHttpClient;\n"
      + "public class DynamicAuthTemplate extends GrouperTemplateExternalSystem {\n"
      + "  private int calls = 0;\n"
      + "  @Override\n"
      + "  public synchronized void decorateHttpClient(GrouperHttpClient grouperHttpClient, String externalSystemConfigId) {\n"
      + "    calls++;\n"
      + "    grouperHttpClient.addHeader(\"Authorization\", \"Bearer PREFIX-\" + externalSystemConfigId + \"-\" + calls);\n"
      + "    grouperHttpClient.addDoNotLogHeader(\"Authorization\");\n"
      + "  }\n"
      + "}\n";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new WsBearerTokenExternalSystemDynamicTest("testDynamicAddsHeadersAndReusesInstance"));
    TestRunner.run(new WsBearerTokenExternalSystemDynamicTest("testDynamicNewInstanceWhenSourceChanges"));
    TestRunner.run(new WsBearerTokenExternalSystemDynamicTest("testDynamicNewInstanceWhenEndpointChanges"));
    TestRunner.run(new WsBearerTokenExternalSystemDynamicTest("testDynamicRequiresExternalSystemTemplateType"));
    TestRunner.run(new WsBearerTokenExternalSystemDynamicTest("testDynamicRequiresTemplateConfigId"));
    TestRunner.run(new WsBearerTokenExternalSystemDynamicTest("testOptionDriverListsOnlyExternalSystemTemplates"));
  }

  /**
   * @param name
   */
  public WsBearerTokenExternalSystemDynamicTest(String name) {
    super(name);
  }

  /**
   * clear compiled classes between tests so one test's template does not leak into another
   */
  @Override
  protected void setUp() {
    super.setUp();
    GshTemplateClassLoaderRegistry.clearCache();
    WsBearerTokenExternalSystem.clearDynamicTemplateCache();
  }

  /**
   * Configure TEMPLATE_CONFIG_ID as a compiled template of the given type and source, reusing the
   * sample template scaffolding for the required common fields (name, description, etc.)
   * @param templateType e.g. externalSystem
   * @param javaSource template source
   */
  private void configureTemplate(String templateType, String javaSource) {
    String templateConfigLines = GrouperUtil.readResourceIntoString(
        "edu/internet2/middleware/grouper/app/gsh/template/test-gsh-template-config.properties", false);
    for (String keyValue : GrouperUtil.splitFileLines(templateConfigLines)) {
      if (StringUtils.isNotBlank(keyValue)) {
        String[] keyValueArr = keyValue.split("=", 2);
        GrouperConfig.retrieveConfig().propertiesOverrideMap().put(keyValueArr[0].trim(), keyValueArr[1].trim());
      }
    }
    String templatePrefix = "grouperGshTemplate." + TEMPLATE_CONFIG_ID + ".";
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(templatePrefix + "templateType", templateType);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(templatePrefix + "templateMode", "compiled");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(templatePrefix + "gshTemplate", javaSource);
  }

  /**
   * point the web service external system at the template with httpAuthnType=dynamic
   * @return the loader config
   */
  private GrouperLoaderConfig configureWs() {
    GrouperLoaderConfig config = GrouperLoaderConfig.retrieveConfig();
    config.propertiesOverrideMap().put(WS_PREFIX + "endpoint", "https://example.invalid/api");
    config.propertiesOverrideMap().put(WS_PREFIX + "httpAuthnType", "dynamic");
    config.propertiesOverrideMap().put(WS_PREFIX + "dynamicGshTemplateConfigId", TEMPLATE_CONFIG_ID);
    return config;
  }

  /**
   * @param client http client
   * @return the request headers set so far
   */
  @SuppressWarnings("unchecked")
  private Map<String, String> headers(GrouperHttpClient client) {
    return (Map<String, String>) GrouperUtil.fieldValue(client, "headers");
  }

  /**
   * attach authentication to a fresh client and return its Authorization header
   * @param config loader config
   * @return the Authorization header value
   */
  private String authorizationForNewRequest(GrouperLoaderConfig config) {
    GrouperHttpClient client = new GrouperHttpClient();
    WsBearerTokenExternalSystem.attachAuthenticationToHttpClient(client, WS_CONFIG_ID, config, null);
    assertTrue("template should mark Authorization as do-not-log",
        client.getDoNotLogHeaders().contains("Authorization"));
    return headers(client).get("Authorization");
  }

  /**
   * the template's header is added, the external system config id is passed in, and the same
   * template instance is reused across requests (its call counter keeps going)
   */
  public void testDynamicAddsHeadersAndReusesInstance() {
    configureTemplate("externalSystem", TEMPLATE_SOURCE);
    GrouperLoaderConfig config = configureWs();

    assertEquals("Bearer PREFIX-" + WS_CONFIG_ID + "-1", authorizationForNewRequest(config));
    assertEquals("Bearer PREFIX-" + WS_CONFIG_ID + "-2", authorizationForNewRequest(config));
  }

  /**
   * editing the template source compiles a new class, so the external system starts a new
   * instance (dropping any cached state such as tokens)
   */
  public void testDynamicNewInstanceWhenSourceChanges() {
    configureTemplate("externalSystem", TEMPLATE_SOURCE);
    GrouperLoaderConfig config = configureWs();

    assertEquals("Bearer PREFIX-" + WS_CONFIG_ID + "-1", authorizationForNewRequest(config));
    assertEquals("Bearer PREFIX-" + WS_CONFIG_ID + "-2", authorizationForNewRequest(config));

    configureTemplate("externalSystem", StringUtils.replace(TEMPLATE_SOURCE, "PREFIX", "CHANGED"));

    // within the one minute recheck window the old instance keeps going
    assertEquals("Bearer PREFIX-" + WS_CONFIG_ID + "-3", authorizationForNewRequest(config));

    // once the window passes the new source is picked up with a fresh instance
    WsBearerTokenExternalSystem.expireDynamicTemplateRecentlyChecked();
    assertEquals("Bearer CHANGED-" + WS_CONFIG_ID + "-1", authorizationForNewRequest(config));
  }

  /**
   * changing the external system's endpoint starts a new template instance, so a token cached for
   * the old host is not sent to the new one
   */
  public void testDynamicNewInstanceWhenEndpointChanges() {
    configureTemplate("externalSystem", TEMPLATE_SOURCE);
    GrouperLoaderConfig config = configureWs();

    assertEquals("Bearer PREFIX-" + WS_CONFIG_ID + "-1", authorizationForNewRequest(config));
    assertEquals("Bearer PREFIX-" + WS_CONFIG_ID + "-2", authorizationForNewRequest(config));

    config.propertiesOverrideMap().put(WS_PREFIX + "endpoint", "https://other.example.invalid/api");

    assertEquals("Bearer PREFIX-" + WS_CONFIG_ID + "-1", authorizationForNewRequest(config));
  }

  /**
   * a compiled template of another type cannot be used for dynamic authentication
   */
  public void testDynamicRequiresExternalSystemTemplateType() {
    configureTemplate("library", TEMPLATE_SOURCE);
    GrouperLoaderConfig config = configureWs();

    try {
      authorizationForNewRequest(config);
      fail("a library template should not be usable for dynamic authentication");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), StringUtils.contains(GrouperUtil.getFullStackTrace(re), "expected 'externalSystem'"));
    }
  }

  /**
   * httpAuthnType=dynamic without a template config id fails clearly
   */
  public void testDynamicRequiresTemplateConfigId() {
    GrouperLoaderConfig config = configureWs();
    config.propertiesOverrideMap().remove(WS_PREFIX + "dynamicGshTemplateConfigId");

    try {
      authorizationForNewRequest(config);
      fail("dynamic without dynamicGshTemplateConfigId should fail");
    } catch (RuntimeException re) {
      assertTrue(re.getMessage(), StringUtils.contains(GrouperUtil.getFullStackTrace(re), "dynamicGshTemplateConfigId"));
    }
  }

  /**
   * the external system's template dropdown lists externalSystem templates and nothing else
   */
  public void testOptionDriverListsOnlyExternalSystemTemplates() {
    configureTemplate("externalSystem", TEMPLATE_SOURCE);
    assertTrue(optionKeysContainTemplate());

    configureTemplate("library", TEMPLATE_SOURCE);
    assertFalse(optionKeysContainTemplate());
  }

  /**
   * @return true if the externalSystem template dropdown has TEMPLATE_CONFIG_ID
   */
  private boolean optionKeysContainTemplate() {
    List<MultiKey> keysAndLabels = new GshTemplateExternalSystemConfigIds().retrieveKeysAndLabels();
    for (MultiKey keyAndLabel : keysAndLabels) {
      if (StringUtils.equals(TEMPLATE_CONFIG_ID, (String) keyAndLabel.getKey(0))) {
        return true;
      }
    }
    return false;
  }

}
