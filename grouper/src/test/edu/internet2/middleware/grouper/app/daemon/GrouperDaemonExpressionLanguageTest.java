/**
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
 */
package edu.internet2.middleware.grouper.app.daemon;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleAttribute;
import edu.internet2.middleware.grouper.app.config.GrouperConfigurationModuleBase;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperVersion;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory H4 (daemon admin script authority), expression language part: the daemon
 * screens are open to uiV2.admin.daemonJobs.must.be.in.group, and a daemon form field can be
 * posted as expression language (config_el_X=on).  Expression language is evaluated with static
 * class access (when the form posts in 4.17 and later, and whenever the saved .elConfig is read),
 * so a delegate who is not wheel or root must not be able to enter it.  GRP-7386 makes
 * GrouperConfigurationModuleBase.populateConfigurationValuesFromUi take the posted text literally
 * for such a subject.  The advisory H4 fix on its own does not cover this: a delegate may still
 * change a daemon's schedule (quartzCron), and an expression posted there would be saved as
 * quartzCron.elConfig.
 * <p>
 * Releases: GRP-7386 is in 4.26.0, 6.5.0 and 7.6.0 and later, and advisory H4's sets back-port it
 * to 4.17.7-4.24.0, 5.12.0-5.13.5, 6.0.0-6.4.1 and 7.0.0-7.5.1.  Every 4.x release runs the checks
 * (a 4.x source checkout reports major version 4).  On other majors the checks run only if the
 * release has GRP-7386 (GrouperConfigurationModuleBase.isSubjectAllowedToUseExpressionLanguage);
 * else the test prints a skip line (2.6.x, 5.0.0-5.11.3, 5.14.0-5.22.5: see advisory H4 Open items).
 * </p>
 * Before the fix, on 4.17.7-4.24.0, testNonWheelExpressionLanguageTakenLiterally fails: the
 * posted field is marked expression language (on the 5.x-7.x releases above it skips instead).  Java 8 (2.6.19).
 */
public class GrouperDaemonExpressionLanguageTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDaemonExpressionLanguageTest("testNonWheelExpressionLanguageTakenLiterally"));
  }

  /**
   *
   */
  public GrouperDaemonExpressionLanguageTest() {
    super();
  }

  /**
   * @param name
   */
  public GrouperDaemonExpressionLanguageTest(String name) {
    super(name);
  }

  /** harmless expression; only whether it is treated as expression language matters */
  private static final String EXPRESSION = "${'0 0 5' + ' * * ?'}";

  /**
   * a delegate (SUBJ0, not wheel) posts the schedule as expression language: it is taken as plain
   * text, never as an expression
   */
  public void testNonWheelExpressionLanguageTakenLiterally() {
    if (skipIfReleaseLacksGrp7386("testNonWheelExpressionLanguageTakenLiterally")) {
      return;
    }
    GrouperConfigurationModuleAttribute quartzCron = populateQuartzCronAs(SubjectTestHelper.SUBJ0);
    assertFalse("a non wheel subject cannot post expression language", quartzCron.isExpressionLanguage());
    assertEquals(EXPRESSION, quartzCron.getValue());
  }

  /**
   * root may still use expression language (control: the posted request is right)
   */
  public void testRootExpressionLanguageAllowed() {
    if (skipIfReleaseLacksGrp7386("testRootExpressionLanguageAllowed")) {
      return;
    }
    GrouperConfigurationModuleAttribute quartzCron = populateQuartzCronAs(SubjectFinder.findRootSubject());
    assertTrue(quartzCron.isExpressionLanguage());
    assertEquals(EXPRESSION, quartzCron.getExpressionLanguageScript());
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @param testName for the skip line
   * @return true if this release has no GRP-7386 and is not a 4.x release (4.x is patched by advisory H4)
   */
  private static boolean skipIfReleaseLacksGrp7386(String testName) {
    String version = GrouperVersion.grouperVersion();
    if (version != null && version.startsWith("4.")) {
      return false;
    }
    try {
      GrouperConfigurationModuleBase.class.getMethod("isSubjectAllowedToUseExpressionLanguage", Subject.class);
      return false;
    } catch (NoSuchMethodException nsme) {
      System.out.println("skip " + testName + ": GRP-7386 (expression language in UI config forms for wheel or root only)"
          + " is not in this release (" + version + "), see advisory H4 Open items");
      return true;
    }
  }

  /**
   * populate an usdu daemon form, posted with quartzCron as expression language, in a grouper
   * session as this subject (the UI starts one as the logged in user)
   * @param subject who posts
   * @return the quartzCron attribute after populating
   */
  private static GrouperConfigurationModuleAttribute populateQuartzCronAs(Subject subject) {
    final Map<String, String> params = new HashMap<String, String>();
    params.put("config_el_quartzCron", "on");
    params.put("config_quartzCron", EXPRESSION);
    HttpServletRequest request = (HttpServletRequest)Proxy.newProxyInstance(
        GrouperDaemonExpressionLanguageTest.class.getClassLoader(), new Class<?>[] {HttpServletRequest.class},
        new InvocationHandler() {
          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("getParameter".equals(name)) {
              return params.get(args[0]);
            }
            if ("getParameterValues".equals(name)) {
              String value = params.get(args[0]);
              return value == null ? null : new String[] {value};
            }
            if ("getParameterNames".equals(name)) {
              return Collections.enumeration(params.keySet());
            }
            if ("hashCode".equals(name)) {
              return System.identityHashCode(proxy);
            }
            if ("equals".equals(name)) {
              return proxy == args[0];
            }
            if ("toString".equals(name)) {
              return "junitRequest";
            }
            Class<?> type = method.getReturnType();
            if (type == boolean.class) {
              return false;
            }
            if (type == int.class) {
              return 0;
            }
            if (type == long.class) {
              return 0L;
            }
            return null;
          }
        });

    // stop the session GrouperTest may have left, then act as the subject
    GrouperSession.stopQuietly(GrouperSession.staticGrouperSession(false));
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      GrouperDaemonOtherJobUsduConfiguration config = new GrouperDaemonOtherJobUsduConfiguration();
      // the usdu daemon is a single (not multiple) config in most releases: a config id is only
      // allowed (and needed) when the module is multiple, else retrieveAttributes throws
      if (config.isMultiple()) {
        config.setConfigId("usduDaemon");
      }
      config.populateConfigurationValuesFromUi(request);
      GrouperConfigurationModuleAttribute quartzCron = config.retrieveAttributes().get("quartzCron");
      assertNotNull("usdu daemon has a quartzCron field", quartzCron);
      return quartzCron;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }
}
