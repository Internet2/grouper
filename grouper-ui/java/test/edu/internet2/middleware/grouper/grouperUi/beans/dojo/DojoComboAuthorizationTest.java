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
package edu.internet2.middleware.grouper.grouperUi.beans.dojo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.subject.Subject;

/**
 * Security issue subjectApiDiagnosticsAutocompleteAuthorization: the subject API diagnostics
 * autocomplete (UiV2Admin.subjectApiDiagnosticsActAsCombo) searched subjects as GrouperSystem for
 * any logged-in UI user.  DojoComboLogic.logic now runs DojoComboAuthorization first, which applies
 * the diagnostics feature check (wheel/root, or member of
 * uiV2.admin.subjectApiDiagnostics.must.be.in.group) to that combo, and leaves other combos alone.
 * <p>
 * The logged-in subject is supplied with a proxy HttpServletRequest in GrouperUiFilter's request
 * thread local (same approach as ScriptTesterContainerAuthorizationTest).  The test does not
 * compile before the fix (DojoComboAuthorization does not exist).
 * </p>
 */
public class DojoComboAuthorizationTest extends GrouperTest {

  /** combo key of the diagnostics act-as combo */
  private static final String DIAGNOSTICS_COMBO_KEY =
      "edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Admin.subjectApiDiagnosticsActAsCombo";

  /** diagnostics group used by these tests */
  private static final String DIAGNOSTICS_GROUP_NAME = "test:subjectApiDiagnosticsUsers";

  /** wheel group used by these tests */
  private static final String WHEEL_GROUP_NAME = "test:testWheel";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new DojoComboAuthorizationTest("testNonWheelDeniedWhenNoGroupConfigured"));
  }

  /**
   *
   */
  public DojoComboAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public DojoComboAuthorizationTest(String name) {
    super(name);
  }

  /** diagnostics group */
  private Group diagnosticsGroup;

  /** wheel group */
  private Group wheelGroup;

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", WHEEL_GROUP_NAME);
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      this.diagnosticsGroup = new GroupSave(rootSession).assignName(DIAGNOSTICS_GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      this.wheelGroup = new GroupSave(rootSession).assignName(WHEEL_GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put("uiV2.admin.subjectApiDiagnostics.show", "true");
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().remove("uiV2.admin.subjectApiDiagnostics.must.be.in.group");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    clearRequest();
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().remove("uiV2.admin.subjectApiDiagnostics.show");
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().remove("uiV2.admin.subjectApiDiagnostics.must.be.in.group");
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // combo identification
  // ---------------------------------------------------------------------------------------

  /**
   * the anonymous query logic declared in UiV2Admin.subjectApiDiagnosticsActAsCombo maps to the
   * registered key (this is what DojoComboLogic.logic sees at runtime)
   */
  public void testDiagnosticsComboIsRegistered() {
    Class<?> comboClass = null;
    for (int i = 1; i < 1000 && comboClass == null; i++) {
      Class<?> theClass;
      try {
        theClass = Class.forName("edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Admin$" + i);
      } catch (ClassNotFoundException cnfe) {
        continue;
      }
      if (DojoComboQueryLogic.class.isAssignableFrom(theClass)
          && theClass.getEnclosingMethod() != null
          && "subjectApiDiagnosticsActAsCombo".equals(theClass.getEnclosingMethod().getName())) {
        comboClass = theClass;
      }
    }
    assertNotNull("anonymous combo class of subjectApiDiagnosticsActAsCombo", comboClass);
    assertEquals(DIAGNOSTICS_COMBO_KEY, DojoComboAuthorization.comboKey(comboClass));
    assertTrue(DojoComboAuthorization.isRegistered(DojoComboAuthorization.comboKey(comboClass)));
  }

  /**
   * a combo that is not in the registry is unchanged (no error, even for a non-wheel user)
   */
  public void testUnregisteredComboUnchanged() {
    DojoComboQueryLogic<String> other = new DojoComboQueryLogicBase<String>() {

      @Override
      public String lookup(HttpServletRequest request, GrouperSession grouperSession, String query) {
        return null;
      }

      @Override
      public Collection<String> search(HttpServletRequest request, GrouperSession grouperSession, String query) {
        return null;
      }

      @Override
      public String retrieveId(GrouperSession grouperSession, String t) {
        return t;
      }

      @Override
      public String retrieveLabel(GrouperSession grouperSession, String t) {
        return t;
      }
    };
    assertEquals(DojoComboAuthorizationTest.class.getName() + ".testUnregisteredComboUnchanged",
        DojoComboAuthorization.comboKey(other.getClass()));
    assertFalse(DojoComboAuthorization.isRegistered(DojoComboAuthorization.comboKey(other.getClass())));
    assertNull(DojoComboAuthorization.comboKey(String.class));
    assertNull(DojoComboAuthorization.comboKey(null));

    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assignRequest(SubjectTestHelper.SUBJ0, grouperSession);
      assertNull(DojoComboAuthorization.validationError(null, grouperSession, other));
    } finally {
      clearRequest();
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------------------
  // feature check
  // ---------------------------------------------------------------------------------------

  /**
   * the reproduced bug: a plain user, no diagnostics group configured (wheel only) is denied
   */
  public void testNonWheelDeniedWhenNoGroupConfigured() {
    assertNotNull(errorFor(SubjectTestHelper.SUBJ0));
  }

  /**
   * group configured: a non-member is denied, a member is allowed
   */
  public void testDiagnosticsGroup() {
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put(
        "uiV2.admin.subjectApiDiagnostics.must.be.in.group", DIAGNOSTICS_GROUP_NAME);
    addMemberAsRoot(this.diagnosticsGroup, SubjectTestHelper.SUBJ1);

    assertNotNull("not in the diagnostics group", errorFor(SubjectTestHelper.SUBJ0));
    assertNull("in the diagnostics group", errorFor(SubjectTestHelper.SUBJ1));
  }

  /**
   * wheel members and root are allowed
   */
  public void testWheelAndRootAllowed() {
    addMemberAsRoot(this.wheelGroup, SubjectTestHelper.SUBJ2);
    assertNull("wheel", errorFor(SubjectTestHelper.SUBJ2));
    assertNull("root", errorFor(SubjectTestHelper.SUBJR));
  }

  /**
   * the feature turned off denies everyone, root included (same as the page)
   */
  public void testFeatureOffDenied() {
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put("uiV2.admin.subjectApiDiagnostics.show", "false");
    assertNotNull(errorFor(SubjectTestHelper.SUBJR));
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * run the diagnostics combo check as the logged-in subject
   * @param subject
   * @return the error or null
   */
  private static String errorFor(Subject subject) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      assignRequest(subject, grouperSession);
      return DojoComboAuthorization.validationErrorForComboKey(DIAGNOSTICS_COMBO_KEY);
    } finally {
      clearRequest();
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * add a member as root
   * @param group
   * @param subject
   */
  private static void addMemberAsRoot(Group group, Subject subject) {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      group.addMember(subject, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * put a proxy request in GrouperUiFilter's request thread local, so that
   * GrouperUiFilter.retrieveSubjectLoggedIn() returns the subject of the grouper session
   * @param subject
   * @param grouperSession
   */
  private static void assignRequest(Subject subject, GrouperSession grouperSession) {
    final HttpSession httpSession = newHttpSession();
    // SessionInitialiser.getGrouperSession reads the UI grouper session from this attribute
    httpSession.setAttribute("edu.intenet2.middleware.grouper.ui.GrouperSession", grouperSession);
    final Map<String, Object> requestAttributes = new HashMap<String, Object>();
    // GrouperUiFilter.remoteUser returns this attribute first
    requestAttributes.put("grouperRemoteUser", subject.getId());

    HttpServletRequest request = (HttpServletRequest)Proxy.newProxyInstance(
        DojoComboAuthorizationTest.class.getClassLoader(),
        new Class<?>[] {HttpServletRequest.class}, new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();
            if ("getSession".equals(methodName)) {
              return httpSession;
            }
            if ("getAttribute".equals(methodName)) {
              return requestAttributes.get(args[0]);
            }
            if ("setAttribute".equals(methodName)) {
              requestAttributes.put((String)args[0], args[1]);
              return null;
            }
            if ("hashCode".equals(methodName)) {
              return System.identityHashCode(proxy);
            }
            if ("equals".equals(methodName)) {
              return proxy == args[0];
            }
            return null;
          }
        });
    requestThreadLocal().set(request);
  }

  /**
   * clear the request thread local
   */
  private static void clearRequest() {
    requestThreadLocal().remove();
  }

  /**
   * @return GrouperUiFilter's private request thread local
   */
  @SuppressWarnings("unchecked")
  private static ThreadLocal<HttpServletRequest> requestThreadLocal() {
    try {
      Field field = GrouperUiFilter.class.getDeclaredField("threadLocalRequest");
      field.setAccessible(true);
      return (ThreadLocal<HttpServletRequest>)field.get(null);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return an http session backed by a map (only attribute methods are supported)
   */
  private static HttpSession newHttpSession() {
    final Map<String, Object> attributes = new HashMap<String, Object>();
    return (HttpSession)Proxy.newProxyInstance(DojoComboAuthorizationTest.class.getClassLoader(),
        new Class<?>[] {HttpSession.class}, new InvocationHandler() {

          @Override
          public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();
            if ("getAttribute".equals(methodName)) {
              return attributes.get(args[0]);
            }
            if ("setAttribute".equals(methodName)) {
              attributes.put((String)args[0], args[1]);
              return null;
            }
            if ("removeAttribute".equals(methodName)) {
              attributes.remove(args[0]);
              return null;
            }
            if ("hashCode".equals(methodName)) {
              return System.identityHashCode(proxy);
            }
            if ("equals".equals(methodName)) {
              return proxy == args[0];
            }
            throw new UnsupportedOperationException(methodName);
          }
        });
  }
}
