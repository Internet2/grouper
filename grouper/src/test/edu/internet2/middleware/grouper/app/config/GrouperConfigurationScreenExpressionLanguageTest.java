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
package edu.internet2.middleware.grouper.app.config;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.azure.AzureGrouperExternalSystem;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import junit.textui.TestRunner;

/**
 * Expression language posted with a configuration screen is not evaluated.
 *
 * <p>The add / edit screens of the configuration modules (GSH templates, daemons, external
 * systems, provisioners, subject sources...) call
 * {@link GrouperConfigurationModuleBase#populateConfigurationValuesFromUi(HttpServletRequest)}
 * while they only show the form, and those screens can be driven from a link a sysadmin opens.
 * A field posted with config_el_&lt;field&gt;=on used to be evaluated right there, with static
 * class access, for a root or wheel session.  Now a posted script is evaluated only when the
 * request saves the form (a POST to a ...Submit operation); a screen keeps it as text.</p>
 *
 * <p>Uses the Azure external system as an ordinary configuration module which allows expression
 * language (present in every release this fix is shipped for).</p>
 */
public class GrouperConfigurationScreenExpressionLanguageTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GrouperConfigurationScreenExpressionLanguageTest.class);
  }

  /**
   * @param name
   */
  public GrouperConfigurationScreenExpressionLanguageTest(String name) {
    super(name);
  }

  /** the config id of the test external system */
  private static final String CONFIG_ID = "testScreenElAzure";

  /** a string field of the Azure external system which a form can post */
  private static final String FIELD = "loginEndpoint";

  /** a screen operation, which only shows the add form */
  private static final String SCREEN_URI = "/grouper/grouperUi/app/UiV2ExternalSystem.addExternalSystem";

  /** the matching save operation */
  private static final String SUBMIT_URI = "/grouper/grouperUi/app/UiV2ExternalSystem.addExternalSystemSubmit";

  /**
   * set by {@link #markExpressionLanguageEvaluated()}, which the script in these tests calls, so
   * a test can tell whether a posted script was evaluated at all
   */
  private static boolean expressionLanguageEvaluated = false;

  /**
   * called from the script in these tests.  public and static so the script reaches it through
   * static class access, the way a malicious script reaches any class
   * @return a marker value
   */
  public static String markExpressionLanguageEvaluated() {
    expressionLanguageEvaluated = true;
    return "evaluated";
  }

  /** a script which, if evaluated, flips expressionLanguageEvaluated */
  private static final String MARKER_SCRIPT = "${" + GrouperConfigurationScreenExpressionLanguageTest.class.getName()
      + ".markExpressionLanguageEvaluated()}";

  /** loader config keys this test sets, removed in tearDown */
  private static final String[] CONFIG_SUFFIXES = {"loginEndpoint", "tenantId", "clientId", "clientSecret", "resource", "graphEndpoint"};

  @Override
  protected void setUp() {
    super.setUp();

    expressionLanguageEvaluated = false;

    // a complete Azure external system.  the Azure connector is configured in grouper-loader.properties
    String prefix = "grouper.azureConnector." + CONFIG_ID + ".";
    Map<String, String> loaderOverrides = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loaderOverrides.put(prefix + "loginEndpoint", "https://test.whatever.com");
    loaderOverrides.put(prefix + "tenantId", "someTenantId");
    loaderOverrides.put(prefix + "clientId", "someClientId");
    loaderOverrides.put(prefix + "clientSecret", "someSecret");
    loaderOverrides.put(prefix + "resource", "someResource");
    loaderOverrides.put(prefix + "graphEndpoint", "someGraphEndpoint");
  }

  @Override
  protected void tearDown() {
    String prefix = "grouper.azureConnector." + CONFIG_ID + ".";
    Map<String, String> loaderOverrides = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    for (String suffix : CONFIG_SUFFIXES) {
      loaderOverrides.remove(prefix + suffix);
    }
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * a request with the given method, URI and form parameters and nothing else
   * @param httpMethod GET or POST, or null
   * @param uri the request URI, or null
   * @param parameters parameter name to value
   * @return the request
   */
  private static HttpServletRequest request(final String httpMethod, final String uri, final Map<String, String> parameters) {

    InvocationHandler handler = new InvocationHandler() {

      public Object invoke(Object proxy, Method method, Object[] args) {
        if ("getParameter".equals(method.getName())) {
          return parameters.get((String) args[0]);
        }
        if ("getParameterValues".equals(method.getName())) {
          String value = parameters.get((String) args[0]);
          return value == null ? null : new String[] { value };
        }
        if ("getMethod".equals(method.getName())) {
          return httpMethod;
        }
        if ("getRequestURI".equals(method.getName())) {
          return uri;
        }
        return null;
      }
    };

    return (HttpServletRequest) Proxy.newProxyInstance(
        GrouperConfigurationScreenExpressionLanguageTest.class.getClassLoader(),
        new Class<?>[] { HttpServletRequest.class }, handler);
  }

  /**
   * the form parameters of a crafted link: the login endpoint posted as expression language
   * @return the parameters
   */
  private static Map<String, String> scriptParameters() {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put("config_" + FIELD, MARKER_SCRIPT);
    parameters.put("config_el_" + FIELD, "on");
    return parameters;
  }

  /**
   * populate a new configuration of the test external system from a request, with a root session
   * @param httpRequest the request
   * @return the configuration after populating
   */
  private AzureGrouperExternalSystem populateAsRoot(HttpServletRequest httpRequest) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      AzureGrouperExternalSystem configuration = new AzureGrouperExternalSystem();
      configuration.setConfigId(CONFIG_ID);
      configuration.retrieveAttributes();
      // loaded, so the script could be evaluated if the gate let it through
      configuration.retrieveObjectValueSubstituteMap();
      configuration.populateConfigurationValuesFromUi(httpRequest);
      return configuration;
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------------

  /**
   * the attack: a root session, a screen operation (as a link makes the page post it), a script
   * posted as expression language.  it must not run; the form still shows it as a script
   */
  public void testScreenRequestDoesNotEvaluateForRoot() {

    AzureGrouperExternalSystem configuration = this.populateAsRoot(request("POST", SCREEN_URI, scriptParameters()));
    GrouperConfigurationModuleAttribute attribute = configuration.retrieveAttributes().get(FIELD);

    assertFalse("a script posted with a screen request must not be evaluated", expressionLanguageEvaluated);
    assertTrue("the form still shows the field as expression language", attribute.isExpressionLanguage());
    assertEquals("the script is kept as text", MARKER_SCRIPT, attribute.getExpressionLanguageScript());
    assertNull("no evaluated value", attribute.getExpressionLanguageValue());
    assertTrue("remembered as not evaluated", configuration.isExpressionLanguageFromRequestNotEvaluated(attribute));
  }

  /**
   * a GET, or a request without a path (not from the UI servlet), is not a save either
   */
  public void testGetOrNoPathDoesNotEvaluate() {

    this.populateAsRoot(request("GET", SUBMIT_URI, scriptParameters()));
    assertFalse("a GET does not save", expressionLanguageEvaluated);

    this.populateAsRoot(request("POST", null, scriptParameters()));
    assertFalse("no path is not a save", expressionLanguageEvaluated);

    this.populateAsRoot(request(null, null, scriptParameters()));
    assertFalse("nothing is not a save", expressionLanguageEvaluated);
  }

  /**
   * the control: the explicit save of a root session still evaluates the posted script.  without
   * this, the tests above could pass because the script or the setup was broken
   */
  public void testSubmitRequestStillEvaluatesForRoot() {

    AzureGrouperExternalSystem configuration = this.populateAsRoot(request("POST", SUBMIT_URI, scriptParameters()));
    GrouperConfigurationModuleAttribute attribute = configuration.retrieveAttributes().get(FIELD);

    assertTrue("the save evaluates the script", expressionLanguageEvaluated);
    assertTrue(attribute.isExpressionLanguage());
    assertEquals(MARKER_SCRIPT, attribute.getExpressionLanguageScript());
    assertEquals("evaluated", attribute.getExpressionLanguageValue());
    assertFalse(configuration.isExpressionLanguageFromRequestNotEvaluated(attribute));
  }

  /**
   * which requests count as a save
   */
  public void testIsExpressionLanguageSaveRequest() {
    Map<String, String> none = new HashMap<String, String>();

    assertTrue(GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("POST", SUBMIT_URI, none)));
    assertTrue(GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("post", SUBMIT_URI, none)));
    assertTrue("trailing slash", GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("POST", SUBMIT_URI + "/", none)));
    assertTrue("path parameter", GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("POST", SUBMIT_URI + ";jsessionid=abc", none)));

    assertFalse(GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(null));
    assertFalse(GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("POST", SCREEN_URI, none)));
    assertFalse(GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("GET", SUBMIT_URI, none)));
    assertFalse(GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("POST", null, none)));
    assertFalse(GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("POST", "", none)));
    assertFalse("the operation is before the path parameter",
        GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("POST", SCREEN_URI + ";Submit", none)));
    assertFalse("the main page is not a save",
        GrouperConfigurationModuleBase.isExpressionLanguageSaveRequest(request("POST", "/grouper/grouperUi/app/UiV2Main.index", none)));
  }
}
