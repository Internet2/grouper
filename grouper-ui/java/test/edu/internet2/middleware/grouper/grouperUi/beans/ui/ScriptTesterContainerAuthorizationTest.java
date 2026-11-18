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
package edu.internet2.middleware.grouper.grouperUi.beans.ui;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory H2 (JEXL script tester admin authorization): the script tester runs
 * request-supplied GSH/Groovy in the UI JVM, so ScriptTesterContainer.isCanScriptTester() (the
 * gate both UiV2ScriptTester handlers call) must require BOTH wheel/root AND membership in the
 * configured jexlScriptTestingGroup.  Before the fix it checked only group membership, so a
 * non-admin member of the group could call UiV2ScriptTester.testScriptSubmit directly
 * (testNonWheelMemberDenied fails before the fix).
 *
 * The logged-in subject is supplied with a proxy HttpServletRequest in GrouperUiFilter's
 * request thread local, whose session holds a GrouperSession for the subject (the same place
 * the UI keeps it), so GrouperUiFilter.retrieveSubjectLoggedIn() returns that subject.
 */
public class ScriptTesterContainerAuthorizationTest extends GrouperTest {

  /** tester group used by these tests (config override) */
  private static final String TESTER_GROUP_NAME = "test:jexlScriptTestingGroup";

  /** wheel group used by these tests (config override) */
  private static final String WHEEL_GROUP_NAME = "test:testWheel";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ScriptTesterContainerAuthorizationTest("testNonWheelMemberDenied"));
  }

  /**
   *
   */
  public ScriptTesterContainerAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public ScriptTesterContainerAuthorizationTest(String name) {
    super(name);
  }

  /** tester group */
  private Group testerGroup;

  /** wheel group */
  private Group wheelGroup;

  // ---------------------------------------------------------------------------------------
  // setup: tester group and wheel group as root, config pointing at them
  // ---------------------------------------------------------------------------------------

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("jexlScriptTestingGroup", TESTER_GROUP_NAME);
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
      this.testerGroup = new GroupSave(rootSession).assignName(TESTER_GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
      this.wheelGroup = new GroupSave(rootSession).assignName(WHEEL_GROUP_NAME)
          .assignCreateParentStemsIfNotExist(true).save();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    clearRequest();
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------

  /**
   * a member of the tester group who is not wheel is denied (the reproduced bug), and the
   * link is hidden too
   */
  public void testNonWheelMemberDenied() {
    addMemberAsRoot(this.testerGroup, SubjectTestHelper.SUBJ0);

    assertFalse("non-wheel tester group member must not use the tester",
        canScriptTester(SubjectTestHelper.SUBJ0));
    assertFalse("non-wheel tester group member must not see the link",
        canScriptTesterLink(SubjectTestHelper.SUBJ0));
  }

  /**
   * a wheel member who is not in the tester group is denied, and the link now follows the
   * same rule (before the fix the link showed and the handler refused)
   */
  public void testWheelNonMemberDenied() {
    addMemberAsRoot(this.wheelGroup, SubjectTestHelper.SUBJ1);

    assertFalse("wheel member outside the tester group must not use the tester",
        canScriptTester(SubjectTestHelper.SUBJ1));
    assertFalse("wheel member outside the tester group must not see the link",
        canScriptTesterLink(SubjectTestHelper.SUBJ1));
  }

  /**
   * wheel plus direct membership in the tester group is allowed
   */
  public void testWheelDirectMemberAllowed() {
    addMemberAsRoot(this.wheelGroup, SubjectTestHelper.SUBJ2);
    addMemberAsRoot(this.testerGroup, SubjectTestHelper.SUBJ2);

    assertTrue(canScriptTester(SubjectTestHelper.SUBJ2));
    assertTrue(canScriptTesterLink(SubjectTestHelper.SUBJ2));
  }

  /**
   * wheel plus indirect (nested group) membership in the tester group is allowed, and a
   * non-wheel subject with the same indirect membership is denied
   */
  public void testNestedMembership() {
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Group nested = new GroupSave(rootSession).assignName("test:nestedTesters")
          .assignCreateParentStemsIfNotExist(true).save();
      nested.addMember(SubjectTestHelper.SUBJ3, false);
      nested.addMember(SubjectTestHelper.SUBJ4, false);
      this.testerGroup.addMember(nested.toSubject(), false);
      this.wheelGroup.addMember(SubjectTestHelper.SUBJ3, false);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    assertTrue("wheel and indirect member", canScriptTester(SubjectTestHelper.SUBJ3));
    assertFalse("indirect member but not wheel", canScriptTester(SubjectTestHelper.SUBJ4));
  }

  /**
   * root is wheel-or-root, but still has to be in the tester group (unchanged behavior)
   */
  public void testRoot() {
    Subject root = SubjectTestHelper.SUBJR;
    assertFalse("root outside the tester group", canScriptTester(root));

    addMemberAsRoot(this.testerGroup, root);
    assertTrue("root inside the tester group", canScriptTester(root));
  }

  /**
   * a subject in neither group is denied
   */
  public void testNeitherDenied() {
    assertFalse(canScriptTester(SubjectTestHelper.SUBJ5));
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

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
   * call isCanScriptTester() as the given logged-in subject, with a grouper session started
   * as that subject (as UiV2ScriptTester does before calling it)
   * @param subject
   * @return the result
   */
  private static boolean canScriptTester(Subject subject) {
    return callAsLoggedIn(subject, false);
  }

  /**
   * call isCanScriptTesterLink() as the given logged-in subject
   * @param subject
   * @return the result
   */
  private static boolean canScriptTesterLink(Subject subject) {
    return callAsLoggedIn(subject, true);
  }

  /**
   * @param subject
   * @param link true for the link check, false for the handler gate
   * @return the result
   */
  private static boolean callAsLoggedIn(Subject subject, boolean link) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      assignRequest(subject, grouperSession);
      ScriptTesterContainer scriptTesterContainer = new ScriptTesterContainer();
      return link ? scriptTesterContainer.isCanScriptTesterLink() : scriptTesterContainer.isCanScriptTester();
    } finally {
      clearRequest();
      GrouperSession.stopQuietly(grouperSession);
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
        ScriptTesterContainerAuthorizationTest.class.getClassLoader(),
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
    return (HttpSession)Proxy.newProxyInstance(ScriptTesterContainerAuthorizationTest.class.getClassLoader(),
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
