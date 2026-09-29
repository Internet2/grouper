/*******************************************************************************
 * Copyright 2026 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package edu.internet2.middleware.grouper.app.externalSystem;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.azure.AzureGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.SaveMode;
import junit.textui.TestRunner;

/**
 * GRP-7386: expression language in a configuration form is honored only for wheel or root.  it is
 * evaluated server side with static class access, so the decision is made on the server from the
 * grouper session, never from the checkbox the request posts.
 *
 * <p>Uses the Azure external system as an ordinary configuration module which allows expression
 * language, so what is tested is the rule in GrouperConfigurationModuleBase, not a module's own
 * restriction (MCP recipes turn it off entirely, see GrouperMcpRecipeToolTest).</p>
 */
public class GrouperConfigurationModuleExpressionLanguageTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperConfigurationModuleExpressionLanguageTest.class);
  }

  /**
   * @param name
   */
  public GrouperConfigurationModuleExpressionLanguageTest(String name) {
    super(name);
  }

  /** the config id of the test external system */
  private static final String CONFIG_ID = "testElAzure";

  /** a string field of the Azure external system which a form can post */
  private static final String FIELD = "loginEndpoint";

  /**
   * set by {@link #markExpressionLanguageEvaluated()}, which the expression language in these tests
   * calls, so a test can tell whether a posted script was evaluated at all
   */
  private static boolean expressionLanguageEvaluated = false;

  /**
   * called from expression language in these tests.  public and static so a script reaches it
   * through static class access, the way a malicious script would reach any class
   * @return a marker value
   */
  public static String markExpressionLanguageEvaluated() {
    expressionLanguageEvaluated = true;
    return "evaluated";
  }

  /** a script which, if evaluated, flips expressionLanguageEvaluated */
  private static final String MARKER_SCRIPT = "${" + GrouperConfigurationModuleExpressionLanguageTest.class.getName()
      + ".markExpressionLanguageEvaluated()}";

  /** root session for the test, v4 GrouperTest.setUp() does not leave one open like v6+ does */
  private GrouperSession rootSessionForTest = null;

  @Override
  protected void setUp() {
    super.setUp();

    this.rootSessionForTest = GrouperSession.startRootSession();

    expressionLanguageEvaluated = false;

    // a complete Azure external system, with one field already expression language in config.
    // the Azure connector is configured in grouper-loader.properties
    String prefix = "grouper.azureConnector." + CONFIG_ID + ".";
    Map<String, String> loaderOverrides = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loaderOverrides.put(prefix + "loginEndpoint", "https://test.whatever.com");
    loaderOverrides.put(prefix + "tenantId.elConfig", "${'someTenantId'}");
    loaderOverrides.put(prefix + "clientId", "someClientId");
    loaderOverrides.put(prefix + "clientSecret", "someSecret");
    loaderOverrides.put(prefix + "resource", "someResource");
    loaderOverrides.put(prefix + "graphEndpoint", "someGraphEndpoint");
  }

  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.rootSessionForTest);
    super.tearDown();
  }

  /**
   * a request carrying the given form parameters and nothing else
   * @param parameters parameter name to value
   * @return the request
   */
  private static HttpServletRequest formRequest(final Map<String, String> parameters) {

    InvocationHandler handler = new InvocationHandler() {

      public Object invoke(Object proxy, Method method, Object[] args) {
        if ("getParameter".equals(method.getName())) {
          return parameters.get((String) args[0]);
        }
        if ("getParameterValues".equals(method.getName())) {
          String value = parameters.get((String) args[0]);
          return value == null ? null : new String[] { value };
        }
        return null;
      }
    };

    return (HttpServletRequest) Proxy.newProxyInstance(
        GrouperConfigurationModuleExpressionLanguageTest.class.getClassLoader(),
        new Class<?>[] { HttpServletRequest.class }, handler);
  }

  /**
   * a new configuration for the test external system, ready to populate: the substitute map is
   * loaded so expression language could be evaluated if the gate let it through
   * @return the configuration
   */
  private AzureGrouperExternalSystem configuration() {
    AzureGrouperExternalSystem configuration = new AzureGrouperExternalSystem();
    configuration.setConfigId(CONFIG_ID);
    configuration.retrieveAttributes();
    configuration.retrieveObjectValueSubstituteMap();
    return configuration;
  }

  /**
   * post the login endpoint as expression language, with the checkbox on
   * @param configuration the configuration to populate
   * @return the login endpoint attribute after populating
   */
  private GrouperConfigurationModuleAttribute postFieldAsExpressionLanguage(AzureGrouperExternalSystem configuration) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("config_" + FIELD, MARKER_SCRIPT);
    parameters.put("config_el_" + FIELD, "on");
    configuration.populateConfigurationValuesFromUi(formRequest(parameters));
    return configuration.retrieveAttributes().get(FIELD);
  }

  /**
   * the control: with a root session the posted expression language is honored and evaluated.
   * without this, the tests below could pass because the script or the setup was broken
   */
  public void testRootSessionHonorsExpressionLanguage() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      AzureGrouperExternalSystem configuration = this.configuration();
      assertTrue(configuration.isExpressionLanguageAllowedForCurrentSession());

      GrouperConfigurationModuleAttribute attribute = this.postFieldAsExpressionLanguage(configuration);

      assertTrue("root may use expression language", attribute.isExpressionLanguage());
      assertEquals(MARKER_SCRIPT, attribute.getExpressionLanguageScript());
      assertTrue("and it was evaluated", expressionLanguageEvaluated);
      assertEquals("evaluated", attribute.getExpressionLanguageValue());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * somebody who is not wheel or root posts a field as expression language: it is taken literally
   * and never evaluated
   */
  public void testNonWheelSessionIgnoresExpressionLanguage() {

    GrouperSession subjectSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      AzureGrouperExternalSystem configuration = this.configuration();
      assertFalse(configuration.isExpressionLanguageAllowedForCurrentSession());

      GrouperConfigurationModuleAttribute attribute = this.postFieldAsExpressionLanguage(configuration);

      assertFalse("the script was not evaluated", expressionLanguageEvaluated);
      assertFalse("the field is not expression language", attribute.isExpressionLanguage());
      assertEquals("the field is the posted text, literally", MARKER_SCRIPT, attribute.getValue());
    } finally {
      GrouperSession.stopQuietly(subjectSession);
    }
  }

  /**
   * a member of the wheel group, with wheel turned on, may use expression language like root
   */
  public void testWheelMemberHonorsExpressionLanguage() {

    Group wheelGroup = new GroupSave(GrouperSession.staticGrouperSession())
        .assignSaveMode(SaveMode.INSERT_OR_UPDATE)
        .assignGroupNameToEdit("etc:testElWheel").assignName("etc:testElWheel")
        .assignCreateParentStemsIfNotExist(true).save();
    wheelGroup.addMember(SubjectTestHelper.SUBJ1, false);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheelGroup.getName());

    assertTrue(GrouperConfigurationModuleBase.isSubjectAllowedToUseExpressionLanguage(SubjectTestHelper.SUBJ1));
    assertFalse(GrouperConfigurationModuleBase.isSubjectAllowedToUseExpressionLanguage(SubjectTestHelper.SUBJ0));

    GrouperSession wheelSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    try {
      AzureGrouperExternalSystem configuration = this.configuration();
      GrouperConfigurationModuleAttribute attribute = this.postFieldAsExpressionLanguage(configuration);

      assertTrue("a wheel member may use expression language", attribute.isExpressionLanguage());
      assertTrue(expressionLanguageEvaluated);
    } finally {
      GrouperSession.stopQuietly(wheelSession);
    }
  }

  /**
   * with no grouper session at all, the posted expression language is not honored
   */
  public void testNoSessionIgnoresExpressionLanguage() {

    AzureGrouperExternalSystem configuration = this.configuration();

    GrouperSession.clearGrouperSessions();
    try {
      assertNull(GrouperSession.staticGrouperSession(false));
      assertFalse(configuration.isExpressionLanguageAllowedForCurrentSession());

      GrouperConfigurationModuleAttribute attribute = this.postFieldAsExpressionLanguage(configuration);

      assertFalse("the script was not evaluated", expressionLanguageEvaluated);
      assertFalse(attribute.isExpressionLanguage());
      assertEquals(MARKER_SCRIPT, attribute.getValue());
    } finally {
      // put a root session back for the rest of the test lifecycle
      GrouperSession.startRootSession();
    }
  }

  /**
   * somebody who is not wheel or root saves a form where a field is already expression language
   * in config (set by a sysadmin).  the field is kept as is, not overwritten with the script text
   * posted back to them, and not evaluated from what they posted
   */
  public void testNonWheelKeepsExistingExpressionLanguage() {

    GrouperSession subjectSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      AzureGrouperExternalSystem configuration = this.configuration();

      GrouperConfigurationModuleAttribute tenantId = configuration.retrieveAttributes().get("tenantId");
      assertTrue("set up as expression language in config", tenantId.isExpressionLanguage());
      String scriptBefore = tenantId.getExpressionLanguageScript();

      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("config_tenantId", MARKER_SCRIPT);
      parameters.put("config_el_tenantId", "on");
      configuration.populateConfigurationValuesFromUi(formRequest(parameters));

      tenantId = configuration.retrieveAttributes().get("tenantId");
      assertTrue("still expression language", tenantId.isExpressionLanguage());
      assertEquals("still the sysadmin's script", scriptBefore, tenantId.getExpressionLanguageScript());
      assertFalse("what they posted was not evaluated", expressionLanguageEvaluated);
    } finally {
      GrouperSession.stopQuietly(subjectSession);
    }
  }

  /**
   * the shared rule: null is never allowed, and the root session's subject is
   */
  public void testNullSubjectNotAllowed() {
    assertFalse(GrouperConfigurationModuleBase.isSubjectAllowedToUseExpressionLanguage(null));
    assertTrue(GrouperConfigurationModuleBase.isSubjectAllowedToUseExpressionLanguage(
        GrouperSession.staticGrouperSession().getSubject()));
  }

}
