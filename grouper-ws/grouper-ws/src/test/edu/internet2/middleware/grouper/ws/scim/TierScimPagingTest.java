/****
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
 ***/

package edu.internet2.middleware.grouper.ws.scim;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import com.unboundid.scim2.common.messages.ListResponse;
import com.unboundid.scim2.common.types.GroupResource;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.audit.GrouperEngineBuiltin;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperContext;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ws.GrouperWsConfig;
import edu.internet2.middleware.grouper.ws.scim.providers.TierGroupProvider;
import edu.internet2.middleware.grouper.ws.scim.providers.TierMembershipProvider;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * Issue scimUnboundedSearchResourceUse: SCIM list searches (GET /v2/Groups?filter=...,
 * GET /v2/Memberships?filter=...) must cap itemsPerPage at ws.scim.maxResultsPerPage, support
 * startIndex paging, and only convert one page.
 *
 * <p>Setup as root: 7 groups test:scimPaging:g0 to g6 that SUBJ0 can VIEW and READ; g0 has the 7
 * members SUBJ0 to SUBJ6.  ws.scim.maxResultsPerPage is 5.  The providers are called directly as
 * SUBJ0, with the logged in subject set the way this release's WS filter does it (reflection, so
 * this file compiles on every release).</p>
 *
 * <p>Before the fix this file does not compile (TierScimPaging is new); with only the provider
 * logic reverted, itemsPerPage=1000 returns all 7 groups (the cap assertion fails) and any
 * startIndex is a 500 ("startIndex not currently implemented").</p>
 */
public class TierScimPagingTest extends GrouperTest {

  /** folder of the test groups */
  private static final String STEM = "test:scimPaging";

  /** number of groups, and members of group g0 */
  private static final int GROUP_COUNT = 7;

  /** configured cap for the test */
  private static final int MAX = 5;

  /** a co filter that matches every test group (and only them) */
  private static final String ALL_GROUPS_FILTER = "displayName co \"" + STEM + ":\"";

  /** memberships of g0 */
  private static final String G0_MEMBERSHIPS_FILTER = "groupName eq \"" + STEM + ":g0\"";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new TierScimPagingTest("testGroupSearchCappedAndPaged"));
  }

  /**
   *
   */
  public TierScimPagingTest() {
    super();
  }

  /**
   * @param name
   */
  public TierScimPagingTest(String name) {
    super(name);
  }

  /**
   * groups, members, privileges, and the cap
   * @see GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    // open a root session for the API writes (6.x and older assert without a session)
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      Subject[] members = new Subject[] {SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1,
          SubjectTestHelper.SUBJ2, SubjectTestHelper.SUBJ3, SubjectTestHelper.SUBJ4,
          SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ6};
      for (int i = 0; i < GROUP_COUNT; i++) {
        Group group = new GroupSave(rootSession).assignName(STEM + ":g" + i)
            .assignCreateParentStemsIfNotExist(true).save();
        group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
        group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
        if (i == 0) {
          for (Subject member : members) {
            group.addMember(member, false);
          }
        }
      }
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put(
        TierScimPaging.MAX_RESULTS_PER_PAGE_CONFIG, Integer.toString(MAX));
  }

  /**
   * undo config and thread locals
   * @see GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().remove(
        TierScimPaging.MAX_RESULTS_PER_PAGE_CONFIG);
    clearLoggedInSubject();
    GrouperContext.deleteDefaultContext();
    super.tearDown();
  }

  // ---------------------------------------------------------------- tests

  /**
   * itemsPerPage and startIndex rules (RFC 7644 3.4.2.4) and the configured cap
   */
  public void testCountAndStartIndex() {
    assertEquals(MAX, TierScimPaging.maxResultsPerPage());
    assertEquals(MAX, TierScimPaging.clampCount(null));
    assertEquals(MAX, TierScimPaging.clampCount(Integer.MAX_VALUE));
    assertEquals(2, TierScimPaging.clampCount(2));
    assertEquals(0, TierScimPaging.clampCount(-3));

    assertEquals(1, TierScimPaging.parseStartIndex(null));
    assertEquals(1, TierScimPaging.parseStartIndex(""));
    assertEquals(1, TierScimPaging.parseStartIndex("0"));
    assertEquals(1, TierScimPaging.parseStartIndex("-4"));
    assertEquals(7, TierScimPaging.parseStartIndex("7"));
    try {
      TierScimPaging.parseStartIndex("abc");
      fail("expected IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      // good, the providers answer 400
    }

    // a bad config value falls back to the default
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put(
        TierScimPaging.MAX_RESULTS_PER_PAGE_CONFIG, "0");
    assertEquals(TierScimPaging.MAX_RESULTS_PER_PAGE_DEFAULT, TierScimPaging.maxResultsPerPage());

    // in memory page
    List<Integer> all = new ArrayList<Integer>();
    for (int i = 1; i <= 7; i++) {
      all.add(i);
    }
    GrouperWsConfig.retrieveConfig().propertiesOverrideMap().put(
        TierScimPaging.MAX_RESULTS_PER_PAGE_CONFIG, Integer.toString(MAX));
    assertEquals("[1, 2, 3]", new TierScimPaging(null, 3).page(all).toString());
    assertEquals("[4, 5, 6]", new TierScimPaging("4", 3).page(all).toString());
    assertEquals("[7]", new TierScimPaging("7", 3).page(all).toString());
    assertEquals("[]", new TierScimPaging("8", 3).page(all).toString());
    assertEquals("[]", new TierScimPaging(null, 0).page(all).toString());
  }

  /**
   * a co filter matching every test group: at most MAX groups even if 1000 are asked for, pages
   * by startIndex in name order, and totalResults tells the client whether to keep paging
   */
  public void testGroupSearchCappedAndPaged() {
    ListResponse<?> first = groupSearch(null, 1000);
    assertEquals(names(first), MAX, first.getResources().size());
    assertEquals(STEM + ":g0", groupName(first, 0));
    assertEquals(STEM + ":g4", groupName(first, MAX - 1));
    // more exists: totalResults is past this page
    assertTrue("" + first.getTotalResults(), first.getTotalResults() > MAX);

    ListResponse<?> second = groupSearch("6", 3);
    assertEquals(names(second), 2, second.getResources().size());
    assertEquals(STEM + ":g5", groupName(second, 0));
    assertEquals(STEM + ":g6", groupName(second, 1));
    // last page: exact total
    assertEquals(GROUP_COUNT, second.getTotalResults());
    assertEquals(6, second.getStartIndex().intValue());

    ListResponse<?> none = groupSearch(null, 0);
    assertEquals(0, none.getResources().size());
  }

  /**
   * the membership search: capped, paged, exact total
   */
  public void testMembershipSearchCappedAndPaged() {
    ListResponse<?> first = membershipSearch(G0_MEMBERSHIPS_FILTER, null, 1000);
    assertEquals(MAX, first.getResources().size());
    assertEquals(GROUP_COUNT, first.getTotalResults());

    ListResponse<?> last = membershipSearch(G0_MEMBERSHIPS_FILTER, "7", 3);
    assertEquals(1, last.getResources().size());
    assertEquals(GROUP_COUNT, last.getTotalResults());
  }

  /**
   * a filter type that adds no criteria (pr) is refused with 400 instead of running a search
   */
  public void testMembershipSearchUnsupportedFilter() {
    Response response = callAsSubj0(new Call() {

      public Response call() {
        return new TierMembershipProvider().getWithFilter("groupName pr", null, 10, null);
      }
    });
    assertEquals(400, response.getStatus());
  }

  // ---------------------------------------------------------------- helpers

  /**
   * @param startIndex
   * @param itemsPerPage
   * @return the list response of a group search as SUBJ0
   */
  private ListResponse<?> groupSearch(final String startIndex, final Integer itemsPerPage) {
    Response response = callAsSubj0(new Call() {

      public Response call() {
        return new TierGroupProvider().getWithFilter(ALL_GROUPS_FILTER, startIndex, itemsPerPage, null);
      }
    });
    assertEquals("" + response.getEntity(), 200, response.getStatus());
    return (ListResponse<?>) response.getEntity();
  }

  /**
   * @param filter
   * @param startIndex
   * @param itemsPerPage
   * @return the list response of a membership search as SUBJ0
   */
  private ListResponse<?> membershipSearch(final String filter, final String startIndex,
      final Integer itemsPerPage) {
    Response response = callAsSubj0(new Call() {

      public Response call() {
        return new TierMembershipProvider().getWithFilter(filter, startIndex, itemsPerPage, null);
      }
    });
    assertEquals("" + response.getEntity(), 200, response.getStatus());
    return (ListResponse<?>) response.getEntity();
  }

  /**
   * @param listResponse
   * @param index
   * @return the display name of a group resource
   */
  private static String groupName(ListResponse<?> listResponse, int index) {
    return displayName(listResponse.getResources().get(index));
  }

  /**
   * @param listResponse
   * @return group display names, for messages
   */
  private static String names(ListResponse<?> listResponse) {
    List<String> names = new ArrayList<String>();
    for (Object resource : listResponse.getResources()) {
      names.add(displayName(resource));
    }
    return names.toString();
  }

  /**
   * display name of one list result: a GroupResource, or (in releases whose ListResponse holds the
   * serialized form) a map with a displayName key
   * @param resource
   * @return the display name
   */
  private static String displayName(Object resource) {
    if (resource instanceof GroupResource) {
      return ((GroupResource) resource).getDisplayName();
    }
    if (resource instanceof java.util.Map) {
      Object displayName = ((java.util.Map<?, ?>) resource).get("displayName");
      return displayName == null ? null : displayName.toString();
    }
    throw new RuntimeException("unexpected list result: " + (resource == null ? null : resource.getClass()));
  }

  /** a provider call */
  private interface Call {

    /**
     * @return the provider response
     */
    Response call();
  }

  /**
   * run a provider call with SUBJ0 logged in and a WS default context, like the WS filter
   * @param call
   * @return the response
   */
  private static Response callAsSubj0(Call call) {
    final Subject subject = SubjectTestHelper.SUBJ0;
    final Map<String, Object> attributes = new HashMap<String, Object>();
    // REMOTE_USER is how the filter passes the authenticated user before 7.6.0
    attributes.put("REMOTE_USER", subject.getSourceId() + "::::" + subject.getId());
    HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
        TierScimPagingTest.class.getClassLoader(), new Class<?>[] {HttpServletRequest.class},
        new InvocationHandler() {

          public Object invoke(Object proxy, Method method, Object[] args) {
            if ("getAttribute".equals(method.getName())) {
              return attributes.get(args[0]);
            }
            if ("setAttribute".equals(method.getName())) {
              attributes.put((String)args[0], args[1]);
              return null;
            }
            return defaultValue(method);
          }
        });
    assignLoggedInSubject(subject, request);
    GrouperContext.createNewDefaultContext(GrouperEngineBuiltin.WS, false, false);
    try {
      return call.call();
    } finally {
      clearLoggedInSubject();
      GrouperContext.deleteDefaultContext();
    }
  }

  // ---------------------------------------------------------------- per release wiring (reflection)

  /** 7.6.0 and later: per request state of the web service, in the grouper module */
  private static final String REQUEST_CONTEXT_CLASS = "edu.internet2.middleware.grouper.ws.GrouperWsRequestContext";

  /** 7.6.0 and later: who is logged in, answered by the filter */
  private static final String SUBJECT_RESOLVER_CLASS = "edu.internet2.middleware.grouper.ws.GrouperWsSubjectResolver";

  /** the web service filter; before 7.6.0 it holds the request thread local */
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
        Object resolver = Proxy.newProxyInstance(TierScimPagingTest.class.getClassLoader(),
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
