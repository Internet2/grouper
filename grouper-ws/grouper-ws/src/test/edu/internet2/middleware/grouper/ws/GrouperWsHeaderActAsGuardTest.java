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
package edu.internet2.middleware.grouper.ws;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import junit.textui.TestRunner;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.ws.coresoap.WsSubjectLookup;
import edu.internet2.middleware.grouper.ws.exceptions.WsInvalidQueryException;
import edu.internet2.middleware.subject.Subject;

/**
 * X-Grouper-actAs header act-as (ws.grouper.act.as.group) admission and chaining, issue
 * wsHeaderActAsAdmissionAndChaining.
 *
 * <p>Identities as in the security report: A = SUBJ0 authenticated caller, B = SUBJ1 header act-as
 * subject, C = SUBJ2 request body act-as subject.  A is not a web service client, B is a web
 * service client and may body-act-as anyone in bodyCallers.</p>
 *
 * <p>7.6.0 and later: the web service filter itself (GrouperServiceJ2ee, grouper-ws) is not on
 * this classpath, so the filter's part is simulated: a request resolver that answers B (what the
 * filter returns after the headers replaced A) plus
 * {@link GrouperWsHeaderActAsGuard#assignAuthenticatedSubject(Subject)} with A (what the filter now
 * does).  Before 7.6.0 there is no GrouperWsRequestContext / GrouperWsSubjectUtils: the logged in
 * subject and the act-as checks live in GrouperServiceJ2ee (grouper-ws, where this test then
 * compiles), which reads the servlet request, so the chained test runs the real header act-as code
 * with a stand-in request (REMOTE_USER A, X-Grouper-actAs headers naming B).  Both routes are
 * reached by reflection so this one file compiles on every release.  Fails before the fix:
 * GrouperWsHeaderActAsGuard is new.</p>
 */
public class GrouperWsHeaderActAsGuardTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperWsHeaderActAsGuardTest("testChainedBodyActAsRefused"));
  }

  /**
   * @param name
   */
  public GrouperWsHeaderActAsGuardTest(String name) {
    super(name);
  }

  /**
   * make a group as root with the given members
   * @param name group name
   * @param members members
   * @return the group
   */
  private static Group groupWithMembers(String name, Subject... members) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(grouperSession).assignName(name)
          .assignCreateParentStemsIfNotExist(true).save();
      for (Subject member : members) {
        group.addMember(member, false);
      }
      return group;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * the authenticated caller is checked against ws.client.user.group.name, not only the header
   * act-as subject.  group names differ per test since the guard caches decisions per group name
   */
  public void testAdmissionChecksAuthenticatedCaller() {

    // nothing configured: everyone admitted, same as before
    GrouperWsConfigInApi.retrieveConfig().propertiesOverrideMap().put("ws.client.user.group.name", "");
    assertTrue(GrouperWsHeaderActAsGuard.isClientUserAdmitted(SubjectTestHelper.SUBJ0));

    Group wsClients = groupWithMembers("test:wsActAsAdmission:wsClients", SubjectTestHelper.SUBJ1);
    GrouperWsConfigInApi.retrieveConfig().propertiesOverrideMap().put("ws.client.user.group.name", wsClients.getName());

    // A is not a client: refused even though B (the header subject) is
    assertFalse(GrouperWsHeaderActAsGuard.isClientUserAdmitted(SubjectTestHelper.SUBJ0));
    assertTrue(GrouperWsHeaderActAsGuard.isClientUserAdmitted(SubjectTestHelper.SUBJ1));

    // configured group missing: fail closed
    GrouperWsConfigInApi.retrieveConfig().propertiesOverrideMap().put("ws.client.user.group.name", "test:wsActAsAdmission:doesNotExist");
    assertFalse(GrouperWsHeaderActAsGuard.isClientUserAdmitted(SubjectTestHelper.SUBJ1));

    assertFalse(GrouperWsHeaderActAsGuard.isClientUserAdmitted(null));
  }

  /**
   * the guard rules without a database: nothing assigned means no header act-as, so never chained
   */
  public void testGuardRules() {
    GrouperWsHeaderActAsGuard.clear();
    try {
      assertNull(GrouperWsHeaderActAsGuard.retrieveAuthenticatedSubject());
      assertFalse(GrouperWsHeaderActAsGuard.isChainedActAs(SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2));

      GrouperWsHeaderActAsGuard.assignAuthenticatedSubject(SubjectTestHelper.SUBJ0);
      assertTrue(GrouperWsHeaderActAsGuard.sameSubject(SubjectTestHelper.SUBJ0, GrouperWsHeaderActAsGuard.retrieveAuthenticatedSubject()));

      // body act-as to a third subject is a chain
      assertTrue(GrouperWsHeaderActAsGuard.isChainedActAs(SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2));
      // body act-as back to the authenticated caller is still a different subject than B: refused
      assertTrue(GrouperWsHeaderActAsGuard.isChainedActAs(SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ0));
      // body act-as as the header subject itself is a no-op
      assertFalse(GrouperWsHeaderActAsGuard.isChainedActAs(SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ1));

      GrouperWsHeaderActAsGuard.clear();
      assertFalse(GrouperWsHeaderActAsGuard.isChainedActAs(SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2));
    } finally {
      GrouperWsHeaderActAsGuard.clear();
    }
  }

  /**
   * the reproduction: header A to B, body B to C.  refused once A is recorded as the authenticated
   * caller; the same body act-as without header act-as still works (B to C is allowed by
   * ws.act.as.group)
   * @throws Exception
   */
  public void testChainedBodyActAsRefused() throws Exception {

    // A is a client too: before 7.6.0 the real filter code runs, and it admits A (the fix) before
    // it records A as the authenticated caller.  (7.6.0 and later simulate that step.)
    Group wsClients = groupWithMembers("test:wsActAsChain:wsClients", SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1);
    Group bodyCallers = groupWithMembers("test:wsActAsChain:bodyCallers", SubjectTestHelper.SUBJ1);
    // header rule A -> B, used by the real header code before 7.6.0
    Group headerCallers = groupWithMembers("test:wsActAsChain:headerCallers", SubjectTestHelper.SUBJ0);
    Group headerTargets = groupWithMembers("test:wsActAsChain:headerTargets", SubjectTestHelper.SUBJ1);

    GrouperWsConfigInApi.retrieveConfig().propertiesOverrideMap().put("ws.client.user.group.name", wsClients.getName());
    GrouperWsConfigInApi.retrieveConfig().propertiesOverrideMap().put("ws.act.as.group", bodyCallers.getName());
    GrouperWsConfigInApi.retrieveConfig().propertiesOverrideMap().put("ws.act.as.cache.minutes", "0");
    GrouperWsConfigInApi.retrieveConfig().propertiesOverrideMap().put("ws.grouper.act.as.group",
        headerCallers.getName() + "::::" + headerTargets.getName());

    boolean requestContextRelease = classOrNull(REQUEST_CONTEXT_CLASS) != null;

    GrouperWsHeaderActAsGuard.clear();
    clearRequest(requestContextRelease);
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.WS, false, false);

    WsSubjectLookup lookupC = new WsSubjectLookup(SubjectTestHelper.SUBJ2.getId(), SubjectTestHelper.SUBJ2.getSourceId(), null);
    WsSubjectLookup lookupB = new WsSubjectLookup(SubjectTestHelper.SUBJ1.getId(), SubjectTestHelper.SUBJ1.getSourceId(), null);

    try {

      // header act-as A -> B happened
      if (requestContextRelease) {
        // the filter answers B and records A
        assignResolver(SubjectTestHelper.SUBJ1);
        GrouperWsHeaderActAsGuard.assignAuthenticatedSubject(SubjectTestHelper.SUBJ0);
      } else {
        // the real filter code reads A and the headers from the request
        assignRequest(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1);
      }

      try {
        retrieveSubjectActAs(lookupC);
        fail("header A -> B plus body B -> C must be refused");
      } catch (WsInvalidQueryException wiqe) {
        assertTrue(wiqe.getMessage(), wiqe.getMessage().contains("X-Grouper-actAs"));
      }

      // a body act-as naming the header subject itself is fine
      Subject subject = retrieveSubjectActAs(lookupB);
      assertTrue(GrouperWsHeaderActAsGuard.sameSubject(SubjectTestHelper.SUBJ1, subject));

      // no header act-as: B logged in directly, body act-as as C still works as before
      GrouperWsHeaderActAsGuard.clear();
      if (!requestContextRelease) {
        assignRequest(SubjectTestHelper.SUBJ1, null);
      }
      subject = retrieveSubjectActAs(lookupC);
      assertTrue(GrouperWsHeaderActAsGuard.sameSubject(SubjectTestHelper.SUBJ2, subject));

    } finally {
      GrouperWsHeaderActAsGuard.clear();
      clearRequest(requestContextRelease);
      GrouperContext.deleteDefaultContext();
    }
  }

  // ---------------------------------------------------------------- per release wiring (reflection)

  /** 7.6.0 and later: per request state of the web service, in the grouper module */
  private static final String REQUEST_CONTEXT_CLASS = "edu.internet2.middleware.grouper.ws.GrouperWsRequestContext";

  /** 7.6.0 and later: who is logged in, answered by the filter */
  private static final String SUBJECT_RESOLVER_CLASS = "edu.internet2.middleware.grouper.ws.GrouperWsSubjectResolver";

  /** 7.6.0 and later: the logged in / act-as checks */
  private static final String SUBJECT_UTILS_CLASS = "edu.internet2.middleware.grouper.ws.GrouperWsSubjectUtils";

  /** before 7.6.0: the web service filter (grouper-ws) with the logged in / act-as checks */
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
   * 7.6.0 and later: the filter's resolver answers this subject
   * @param subject
   * @throws Exception
   */
  private static void assignResolver(final Subject subject) throws Exception {
    Class<?> resolverClass = Class.forName(SUBJECT_RESOLVER_CLASS);
    Object resolver = Proxy.newProxyInstance(GrouperWsHeaderActAsGuardTest.class.getClassLoader(),
        new Class<?>[] {resolverClass}, new InvocationHandler() {

          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("retrieveSubjectLoggedIn".equals(method.getName())) {
              return subject;
            }
            return defaultValue(method);
          }
        });
    Class<?> requestContextClass = Class.forName(REQUEST_CONTEXT_CLASS);
    try {
      // 7.8.0 and later: a resolver for this request (thread) only
      requestContextClass.getMethod("assignSubjectResolverForRequest", resolverClass).invoke(null, resolver);
    } catch (NoSuchMethodException nsme) {
      // 7.6.0 and 7.7.0: only the one global resolver.  Remember the registered one (no getter, so
      // read the field) and put it back in clearRequest, so later tests in the JVM are not affected
      if (!globalResolverSaved) {
        java.lang.reflect.Field field = requestContextClass.getDeclaredField("subjectResolver");
        field.setAccessible(true);
        savedGlobalResolver = field.get(null);
        globalResolverSaved = true;
      }
      requestContextClass.getMethod("assignSubjectResolver", resolverClass).invoke(null, resolver);
    }
  }

  /** 7.6.0 and 7.7.0: true once assignResolver replaced the global resolver */
  private static boolean globalResolverSaved = false;

  /** 7.6.0 and 7.7.0: the global resolver registered before assignResolver, restored by clearRequest */
  private static Object savedGlobalResolver = null;

  /**
   * before 7.6.0: put a stand-in servlet request in GrouperServiceJ2ee's request thread local, as
   * its filter does: REMOTE_USER is the authenticated subject, and the X-Grouper-actAs headers
   * (base64, like the client sends them) name the header act-as subject if not null
   * @param authenticated
   * @param headerActAs
   * @throws Exception
   */
  private static void assignRequest(Subject authenticated, Subject headerActAs) throws Exception {
    Class<?> j2eeClass = Class.forName(SERVICE_J2EE_CLASS);
    // javax or jakarta, whatever this release's filter uses
    Class<?> requestClass = j2eeClass.getMethod("retrieveHttpServletRequest").getReturnType();
    final Map<String, Object> attributes = new HashMap<String, Object>();
    attributes.put("REMOTE_USER", authenticated.getSourceId() + "::::" + authenticated.getId());
    final Map<String, String> headers = new HashMap<String, String>();
    if (headerActAs != null) {
      headers.put("X-Grouper-actAsSubjectId", base64(headerActAs.getId()));
      headers.put("X-Grouper-actAsSourceId", base64(headerActAs.getSourceId()));
    }
    Object request = Proxy.newProxyInstance(GrouperWsHeaderActAsGuardTest.class.getClassLoader(),
        new Class<?>[] {requestClass}, new InvocationHandler() {

          public Object invoke(Object proxy, Method method, Object[] args) {
            String name = method.getName();
            if ("getAttribute".equals(name)) {
              return attributes.get(args[0]);
            }
            if ("setAttribute".equals(name)) {
              attributes.put((String)args[0], args[1]);
              return null;
            }
            if ("removeAttribute".equals(name)) {
              attributes.remove(args[0]);
              return null;
            }
            if ("getHeader".equals(name)) {
              return headers.get(args[0]);
            }
            return defaultValue(method);
          }
        });
    requestThreadLocal().set(request);
  }

  /**
   * @return GrouperServiceJ2ee's private request thread local (before 7.6.0)
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  private static ThreadLocal<Object> requestThreadLocal() throws Exception {
    java.lang.reflect.Field field = Class.forName(SERVICE_J2EE_CLASS).getDeclaredField("threadLocalRequest");
    field.setAccessible(true);
    return (ThreadLocal<Object>)field.get(null);
  }

  /**
   * forget the request state of either route
   * @param requestContextRelease true for 7.6.0 and later
   * @throws Exception
   */
  private static void clearRequest(boolean requestContextRelease) throws Exception {
    if (requestContextRelease) {
      Class.forName(REQUEST_CONTEXT_CLASS).getMethod("clearThreadLocals").invoke(null);
      if (globalResolverSaved) {
        // 7.6.0 and 7.7.0: put back the global resolver assignResolver replaced
        Class.forName(REQUEST_CONTEXT_CLASS).getMethod("assignSubjectResolver", Class.forName(SUBJECT_RESOLVER_CLASS))
            .invoke(null, savedGlobalResolver);
        globalResolverSaved = false;
        savedGlobalResolver = null;
      }
    } else {
      requestThreadLocal().remove();
    }
  }

  /**
   * the web service act-as entry point of this release
   * @param lookup
   * @return the act-as subject
   * @throws Exception (WsInvalidQueryException unwrapped)
   */
  private static Subject retrieveSubjectActAs(WsSubjectLookup lookup) throws Exception {
    Class<?> theClass = classOrNull(SUBJECT_UTILS_CLASS);
    if (theClass == null) {
      theClass = Class.forName(SERVICE_J2EE_CLASS);
    }
    try {
      return (Subject)theClass.getMethod("retrieveSubjectActAs", WsSubjectLookup.class).invoke(null, lookup);
    } catch (InvocationTargetException ite) {
      if (ite.getCause() instanceof Exception) {
        throw (Exception)ite.getCause();
      }
      throw ite;
    }
  }

  /**
   * @param value
   * @return base64 of the UTF-8 bytes
   */
  private static String base64(String value) {
    return java.util.Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
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
