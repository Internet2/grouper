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
package edu.internet2.middleware.grouper.ws.scim;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory H7 (SCIM part): SCIM must enforce the WS client admission group
 * (ws.client.user.group.name) before any SCIM provider runs.  Every SCIM provider method starts a
 * session as GrouperServiceJ2ee.retrieveSubjectLoggedIn(), which skips that group, so before the
 * fix a subject outside the group could use /scim/v2/Groups, /Users and /Memberships.
 *
 * <p>The SCIM front door differs by release, so it is found by reflection and this one file
 * compiles on every release:</p>
 * <ul>
 *   <li>4.18.0 and later 4.x, 5.18.0 and later: GrouperScimServlet.service admits the caller and
 *   answers 403 (sendError) before Jersey dispatches.</li>
 *   <li>2.6.x, 4.17.7 and older, 5.0.0 to 5.17.2 (stock Jersey ServletContainer): the
 *   GrouperScimAdmissionFilter JAX-RS request filter in the scanned providers package aborts the
 *   request with 403.</li>
 * </ul>
 * <p>Who is logged in is set the way this release's WS filter does it (see
 * VootServletAdmissionTest): 7.6.0 and later a per-request subject resolver, older releases the
 * request in GrouperServiceJ2ee's thread local with REMOTE_USER.  SUBJ0 is in the admission group,
 * SUBJ1 is not.</p>
 */
public class ScimAdmissionTest extends GrouperTest {

  /** ws.client.user.group.name for the test */
  private static final String ADMISSION_GROUP = "test:scimAdmission:wsClients";

  /** front door before 4.18.0 / 5.18.0 (the patch adds it there) */
  private static final String FILTER_CLASS = "edu.internet2.middleware.grouper.ws.scim.providers.GrouperScimAdmissionFilter";

  /** front door from 4.18.0 / 5.18.0 */
  private static final String SERVLET_CLASS = "edu.internet2.middleware.grouper.ws.scim.GrouperScimServlet";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new ScimAdmissionTest("testNonMemberRejected"));
  }

  /**
   *
   */
  public ScimAdmissionTest() {
    super();
  }

  /**
   * @param name
   */
  public ScimAdmissionTest(String name) {
    super(name);
  }

  // ---------------------------------------------------------------- setup

  /**
   * admission group with SUBJ0, and the config
   */
  @Override
  protected void setUp() {
    super.setUp();

    // open a root session for the API writes (6.x and older assert without a session)
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group admission = new GroupSave(grouperSession).assignName(ADMISSION_GROUP)
          .assignCreateParentStemsIfNotExist(true).save();
      admission.addMember(SubjectTestHelper.SUBJ0, false);
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

  // ---------------------------------------------------------------- tests

  /**
   * a subject outside ws.client.user.group.name gets 403 before any SCIM handler.
   * before the fix the request went straight to the providers (no 403)
   */
  public void testNonMemberRejected() {
    ScimCall call = new ScimCall(SubjectTestHelper.SUBJ1).run();
    assertEquals(call.describe(), 403, call.status);
    // the servlet returns right after sendError: nothing reached Jersey
    assertNull(call.describe(), call.afterAdmission);
  }

  /**
   * a subject in the admission group is let through (not 403)
   */
  public void testMemberAllowed() {
    ScimCall call = new ScimCall(SubjectTestHelper.SUBJ0).run();
    assertTrue(call.describe(), call.status != 403);
  }

  /**
   * a blank ws.client.user.group.name keeps the documented "everyone may use the WS" behavior
   */
  public void testBlankAdmissionGroupAllowsAll() {
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put(
        GrouperWsConfig.WS_CLIENT_USER_GROUP_NAME, "");
    clearAdmissionCache();
    ScimCall call = new ScimCall(SubjectTestHelper.SUBJ1).run();
    assertTrue(call.describe(), call.status != 403);
  }

  // ---------------------------------------------------------------- front door driver

  /**
   * one GET /grouper-ws/scim/v2/Groups through this release's SCIM front door
   */
  private static class ScimCall {

    /** who the "filter" authenticated */
    private final Subject subject;

    /** 403 if the front door refused, else -1 (or another status it set) */
    private int status = -1;

    /** which front door ran */
    private String frontDoor = "none";

    /** what happened after the front door let the request through (servlet only) */
    private Throwable afterAdmission = null;

    /**
     * @param theSubject
     */
    ScimCall(Subject theSubject) {
      this.subject = theSubject;
    }

    /**
     * @return status and context for assertion messages
     */
    String describe() {
      return "frontDoor: " + this.frontDoor + ", status: " + this.status
          + (this.afterAdmission == null ? "" : ", after admission: " + this.afterAdmission);
    }

    /**
     * run the front door the way the WS service filter would set things up
     * @return this
     */
    ScimCall run() {
      final Map<String, Object> attributes = new HashMap<String, Object>();
      // REMOTE_USER is how the filter passes the authenticated user before 7.6.0
      attributes.put("REMOTE_USER", this.subject.getSourceId() + "::::" + this.subject.getId());

      HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
          ScimAdmissionTest.class.getClassLoader(), new Class<?>[] {HttpServletRequest.class},
          new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) {
              String name = method.getName();
              if ("getRequestURI".equals(name)) {
                return "/grouper-ws/scim/v2/Groups";
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
              return defaultValue(method);
            }
          });

      HttpServletResponse response = (HttpServletResponse) Proxy.newProxyInstance(
          ScimAdmissionTest.class.getClassLoader(), new Class<?>[] {HttpServletResponse.class},
          new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) {
              String name = method.getName();
              if ("sendError".equals(name) || "setStatus".equals(name)) {
                ScimCall.this.status = (Integer) args[0];
                return null;
              }
              return defaultValue(method);
            }
          });

      // what the WS service filter does per request: logged in subject and default context
      assignLoggedInSubject(this.subject, request);
      GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.WS, false, false);

      try {
        Class<?> filterClass = classOrNull(FILTER_CLASS);
        Class<?> servletClass = classOrNull(SERVLET_CLASS);
        if (filterClass != null) {
          this.frontDoor = "GrouperScimAdmissionFilter";
          runFilter(filterClass);
        } else if (servletClass != null) {
          this.frontDoor = "GrouperScimServlet";
          runServlet(servletClass, request, response);
        } else {
          fail("this release has no SCIM front door check (patch not applied?)");
        }
      } finally {
        clearLoggedInSubject();
        GrouperContext.deleteDefaultContext();
      }
      return this;
    }

    /**
     * call the JAX-RS request filter with a stand-in request context that records abortWith
     * @param filterClass
     */
    private void runFilter(Class<?> filterClass) {
      ContainerRequestContext requestContext = (ContainerRequestContext) Proxy.newProxyInstance(
          ScimAdmissionTest.class.getClassLoader(), new Class<?>[] {ContainerRequestContext.class},
          new InvocationHandler() {

            public Object invoke(Object proxy, Method method, Object[] args) {
              if ("abortWith".equals(method.getName())) {
                ScimCall.this.status = ((Response) args[0]).getStatus();
                return null;
              }
              return defaultValue(method);
            }
          });
      try {
        ContainerRequestFilter filter = (ContainerRequestFilter) filterClass.newInstance();
        filter.filter(requestContext);
      } catch (RuntimeException re) {
        throw re;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * call GrouperScimServlet.service.  The servlet is not initialized, so after it admits the
     * caller Jersey itself fails; that is recorded, not an error (only a 403 matters here)
     * @param servletClass
     * @param request
     * @param response
     */
    private void runServlet(Class<?> servletClass, HttpServletRequest request, HttpServletResponse response) {
      try {
        Object servlet = servletClass.getConstructor().newInstance();
        servletClass.getMethod("service", ServletRequest.class, ServletResponse.class)
            .invoke(servlet, request, response);
      } catch (InvocationTargetException ite) {
        this.afterAdmission = ite.getCause();
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
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
   * the admission result is cached per subject; drop it so each test sees its own config.
   * It is in GrouperWsSubjectUtils from 7.6.0, in GrouperServiceJ2ee before
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

  /**
   * what the WS filter does per request: make this subject the logged in one
   * @param subject
   * @param request the stand-in request (used before 7.6.0)
   */
  private static void assignLoggedInSubject(final Subject subject, HttpServletRequest request) {
    try {
      if (classOrNull(REQUEST_CONTEXT_CLASS) != null) {
        Class<?> resolverClass = Class.forName(SUBJECT_RESOLVER_CLASS);
        Object resolver = Proxy.newProxyInstance(ScimAdmissionTest.class.getClassLoader(),
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
