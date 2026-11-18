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
package edu.internet2.middleware.grouperVoot;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory H7: the legacy VOOT servlet must enforce the WS client admission group
 * (ws.client.user.group.name) like every other WS operation.  Before the fix VootServlet.doGet
 * started a session as GrouperServiceJ2ee.retrieveSubjectLoggedIn() with no admission check, so
 * a subject outside the group could still read VOOT data.
 *
 * <p>The servlet is driven directly: a default GrouperContext stands in for the one the filter
 * creates, and the request and response are java.lang.reflect.Proxy stand-ins.  Who is logged in
 * is set the way this release's WS filter does it, reached by reflection so this one file compiles
 * on every release: 7.6.0 and later assign a per-request subject resolver
 * (GrouperWsRequestContext); older releases put the request in GrouperServiceJ2ee's request thread
 * local, where retrieveSubjectLoggedIn() reads REMOTE_USER.</p>
 */
public class VootServletAdmissionTest extends GrouperTest {

  /** admission group configured in ws.client.user.group.name */
  private static final String ADMISSION_GROUP = "test:vootAdmission:wsClients";

  /** group SUBJ0 and SUBJ1 are both members of and can read */
  private static final String VISIBLE_GROUP = "test:vootAdmission:visibleGroup";

  /** group SUBJ0 is a member of but has no read or view on (privileges must still apply) */
  private static final String HIDDEN_GROUP = "test:vootAdmission:hiddenGroup";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new VootServletAdmissionTest("testNonMemberRejected"));
  }

  /**
   *
   */
  public VootServletAdmissionTest() {
    super();
  }

  /**
   * @param name
   */
  public VootServletAdmissionTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup

  /**
   * groups and the admission config.  SUBJ0 is in the admission group, SUBJ1 is not
   */
  @Override
  protected void setUp() {
    super.setUp();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group admission = new GroupSave(grouperSession).assignName(ADMISSION_GROUP)
          .assignCreateParentStemsIfNotExist(true).save();
      admission.addMember(SubjectTestHelper.SUBJ0, false);

      Group visible = new GroupSave(grouperSession).assignName(VISIBLE_GROUP)
          .assignCreateParentStemsIfNotExist(true).save();
      visible.addMember(SubjectTestHelper.SUBJ0, false);
      visible.addMember(SubjectTestHelper.SUBJ1, false);
      visible.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      visible.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);

      Group hidden = new GroupSave(grouperSession).assignName(HIDDEN_GROUP)
          .assignCreateParentStemsIfNotExist(true).save();
      hidden.addMember(SubjectTestHelper.SUBJ0, false);
      // no READ or VIEW for SUBJ0.  GrouperTest turns on groups.create.grant.all.read/view, so
      // GrouperAll got READ and VIEW at create time: take those away so the group is really hidden
      hidden.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      hidden.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME, ADMISSION_GROUP);
    clearAdmissionCache();
  }

  /**
   * undo config and thread locals
   */
  @Override
  protected void tearDown() {
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().remove(
        GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME);
    clearAdmissionCache();
    clearLoggedInSubject();
    GrouperContext.deleteDefaultContext();
    super.tearDown();
  }

  /**
   * the admission result is cached per subject; drop it so each test sees its own config.
   * the cache field is private, so null it reflectively (it is lazily rebuilt).  It is in
   * GrouperWsSubjectUtils from 7.6.0, in GrouperServiceJ2ee before
   */
  private static void clearAdmissionCache() {
    try {
      Class<?> theClass = classOrNull(SUBJECT_UTILS_CLASS);
      if (theClass == null) {
        theClass = Class.forName(SERVICE_J2EE_CLASS);
      }
      Field field = theClass.getDeclaredField("subjectAllowedCache");
      field.setAccessible(true);
      field.set(null, null);
    } catch (Exception e) {
      throw new RuntimeException("cannot clear the ws admission cache", e);
    }
  }

  // ---------------------------------------------------------------- per release wiring (reflection)

  /** 7.6.0 and later: per request state of the web service, in the grouper module */
  private static final String REQUEST_CONTEXT_CLASS = "edu.internet2.middleware.grouper.ws.GrouperWsRequestContext";

  /** 7.6.0 and later: who is logged in, answered by the filter */
  private static final String SUBJECT_RESOLVER_CLASS = "edu.internet2.middleware.grouper.ws.GrouperWsSubjectResolver";

  /** 7.6.0 and later: the logged in / admission checks */
  private static final String SUBJECT_UTILS_CLASS = "edu.internet2.middleware.grouper.ws.GrouperWsSubjectUtils";

  /** the web service filter; before 7.6.0 it also holds the logged in / admission checks */
  private static final String SERVICE_J2EE_CLASS = "edu.internet2.middleware.grouper.ws.GrouperServiceJ2ee";

  /**
   * @param className
   * @return the class or null if this release does not have it
   */
  private static Class<?> classOrNull(String className) {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException cnfe) {
      return null;
    }
  }

  /**
   * what the WS filter does per request: make this subject the logged in one
   * @param subject
   * @param request the stand-in request (used before 7.6.0)
   */
  private static void assignLoggedInSubject(final Subject subject, HttpServletRequest request) {
    try {
      if (classOrNull(REQUEST_CONTEXT_CLASS) != null) {
        Class<?> resolverClass = Class.forName(SUBJECT_RESOLVER_CLASS);
        Object resolver = Proxy.newProxyInstance(VootServletAdmissionTest.class.getClassLoader(),
            new Class<?>[] {resolverClass}, new InvocationHandler() {

              public Object invoke(Object proxy, Method method, Object[] args) {
                if ("retrieveSubjectLoggedIn".equals(method.getName())) {
                  return subject;
                }
                return defaultValue(method);
              }
            });
        assignResolverAnyRelease(resolverClass, resolver);
      } else {
        // retrieveSubjectLoggedIn() reads REMOTE_USER of the request in this thread local
        requestThreadLocal().set(request);
      }
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** 7.6.0 and 7.7.0: true once assignResolverAnyRelease replaced the global resolver */
  private static boolean globalResolverSaved = false;

  /** 7.6.0 and 7.7.0: the global resolver registered before the test, put back by clearResolverAnyRelease */
  private static Object savedGlobalResolver = null;

  /**
   * 7.8.0 and later: a resolver for this request (thread) only.  7.6.0 and 7.7.0 have only the one
   * global resolver: remember the registered one (no getter, so read the field) and replace it
   * @param resolverClass
   * @param resolver
   * @throws Exception
   */
  private static void assignResolverAnyRelease(Class<?> resolverClass, Object resolver) throws Exception {
    Class<?> requestContextClass = Class.forName(REQUEST_CONTEXT_CLASS);
    try {
      requestContextClass.getMethod("assignSubjectResolverForRequest", resolverClass).invoke(null, resolver);
    } catch (NoSuchMethodException nsme) {
      if (!globalResolverSaved) {
        java.lang.reflect.Field field = requestContextClass.getDeclaredField("subjectResolver");
        field.setAccessible(true);
        savedGlobalResolver = field.get(null);
        globalResolverSaved = true;
      }
      requestContextClass.getMethod("assignSubjectResolver", resolverClass).invoke(null, resolver);
    }
  }

  /**
   * undo assignResolverAnyRelease: clear the request resolver (7.8.0 and later) or put back the
   * global one (7.6.0 and 7.7.0), so later tests in the JVM are not affected
   * @throws Exception
   */
  private static void clearResolverAnyRelease() throws Exception {
    Class<?> requestContextClass = Class.forName(REQUEST_CONTEXT_CLASS);
    Class<?> resolverClass = Class.forName(SUBJECT_RESOLVER_CLASS);
    try {
      requestContextClass.getMethod("assignSubjectResolverForRequest", resolverClass).invoke(null, new Object[] {null});
    } catch (NoSuchMethodException nsme) {
      if (globalResolverSaved) {
        requestContextClass.getMethod("assignSubjectResolver", resolverClass).invoke(null, savedGlobalResolver);
        globalResolverSaved = false;
        savedGlobalResolver = null;
      }
    }
  }

  /**
   * forget the logged in subject of either route
   */
  private static void clearLoggedInSubject() {
    try {
      if (classOrNull(REQUEST_CONTEXT_CLASS) != null) {
        clearResolverAnyRelease();
      } else {
        requestThreadLocal().remove();
      }
    } catch (RuntimeException re) {
      throw re;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @return GrouperServiceJ2ee's private request thread local (before 7.6.0)
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private static ThreadLocal<Object> requestThreadLocal() throws Exception {
    Field field = Class.forName(SERVICE_J2EE_CLASS).getDeclaredField("threadLocalRequest");
    field.setAccessible(true);
    return (ThreadLocal<Object>)field.get(null);
  }

  // ---------------------------------------------------------------- tests

  /**
   * a subject outside ws.client.user.group.name is rejected and gets no VOOT data.
   * before the fix this returned 200 with the visible group
   */
  public void testNonMemberRejected() {
    VootCall call = new VootCall(SubjectTestHelper.SUBJ1);
    try {
      call.run();
      fail("expected the non-member to be rejected, got " + call.status + ": " + call.body);
    } catch (RuntimeException re) {
      assertTrue(GrouperUtil.getFullStackTrace(re),
          GrouperUtil.getFullStackTrace(re).contains("User is not authorized"));
    }
    assertEquals(500, call.status);
    assertFalse(call.body, call.body.contains(VISIBLE_GROUP));
  }

  /**
   * a subject in the admission group can use VOOT
   */
  public void testMemberAllowed() {
    VootCall call = new VootCall(SubjectTestHelper.SUBJ0);
    call.run();
    assertEquals(call.body, 200, call.status);
    assertTrue(call.body, call.body.contains(VISIBLE_GROUP));
  }

  /**
   * a blank ws.client.user.group.name keeps the documented "everyone may use the WS" behavior
   */
  public void testBlankAdmissionGroupAllowsAll() {
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME, "");
    clearAdmissionCache();
    VootCall call = new VootCall(SubjectTestHelper.SUBJ1);
    call.run();
    assertEquals(call.body, 200, call.status);
    assertTrue(call.body, call.body.contains(VISIBLE_GROUP));
  }

  /**
   * admission does not widen privileges: an admitted member still does not see a group it
   * cannot read
   */
  public void testAdmittedMemberStillPrivilegeFiltered() {
    VootCall call = new VootCall(SubjectTestHelper.SUBJ0);
    call.run();
    assertEquals(call.body, 200, call.status);
    assertFalse(call.body, call.body.contains(HIDDEN_GROUP));
  }

  // ---------------------------------------------------------------- servlet driver

  /**
   * one GET /grouper-ws/voot/groups/@me as a given subject, capturing status and body
   */
  private static class VootCall {

    /** who the "filter" authenticated */
    private final Subject subject;

    /** response status, -1 if never set */
    private int status = -1;

    /** what the servlet wrote */
    private final StringWriter bodyWriter = new StringWriter();

    /** body as a string, filled after run */
    private String body = "";

    /**
     * @param theSubject
     */
    VootCall(Subject theSubject) {
      this.subject = theSubject;
    }

    /**
     * call doGet the way the WS filter would set things up
     */
    void run() {
      // request attributes; REMOTE_USER is how the filter passes the authenticated user before 7.6.0
      final Map<String, Object> attributes = new HashMap<String, Object>();
      attributes.put("REMOTE_USER", this.subject.getSourceId() + "::::" + this.subject.getId());

      HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
          VootServletAdmissionTest.class.getClassLoader(), new Class<?>[] {HttpServletRequest.class},
          new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) {
              String name = method.getName();
              if ("getRequestURI".equals(name)) {
                return "/grouper-ws/voot/groups/@me";
              }
              if ("getMethod".equals(name)) {
                return "GET";
              }
              if ("getParameterMap".equals(name)) {
                return new HashMap<String, String[]>();
              }
              if ("getAttribute".equals(name)) {
                return attributes.get(args[0]);
              }
              if ("setAttribute".equals(name)) {
                attributes.put((String)args[0], args[1]);
                return null;
              }
              // getParameter, getSession(false), and anything else: none
              return defaultValue(method);
            }
          });

      HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
          VootServletAdmissionTest.class.getClassLoader(), new Class<?>[] {HttpServletResponse.class},
          new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) {
              String name = method.getName();
              if ("setStatus".equals(name)) {
                VootCall.this.status = (Integer) args[0];
                return null;
              }
              if ("getWriter".equals(name)) {
                return new PrintWriter(VootCall.this.bodyWriter);
              }
              return defaultValue(method);
            }
          });

      // what the WS service filter does per request: logged in subject and default context
      assignLoggedInSubject(this.subject, request);
      GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.WS, false, false);

      try {
        new VootServlet().doGet(request, response);
      } catch (RuntimeException re) {
        throw re;
      } catch (Exception e) {
        throw new RuntimeException(e);
      } finally {
        this.body = this.bodyWriter.toString();
        clearLoggedInSubject();
        GrouperContext.deleteDefaultContext();
      }
    }
  }

  /**
   * @param method
   * @return a harmless default for a proxied method
   */
  private static Object defaultValue(Method method) {
    Class<?> returnType = method.getReturnType();
    if (returnType == boolean.class) {
      return false;
    }
    if (returnType == int.class) {
      return 0;
    }
    if (returnType == long.class) {
      return 0L;
    }
    return null;
  }
}
