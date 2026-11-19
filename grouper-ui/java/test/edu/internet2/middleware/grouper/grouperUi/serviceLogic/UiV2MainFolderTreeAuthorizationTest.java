package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.StemViewAuthorization;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.exceptions.ControllerDone;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue uiFolderTreeVisibilityAuthorization: UiV2Main.folderMenu and
 * UiV2Main.folderMenuObjectPath resolved any folder uuid or name with the static StemFinder (a
 * lookup, not an authorization check) and returned its display name, uuid and uuid path, so any
 * logged in user could list hidden folders and test whether a folder name exists.</p>
 *
 * <p>Handlers are called directly with an in-memory request, session and response set into
 * GrouperUiFilter's thread locals; the JSON the handler prints is captured from the response
 * writer.</p>
 *
 * <p>Setup: hidden folder test:hidden96 (display HIDDEN_TREE_96) with child test:hidden96:child
 * (display CHILD_SECRET_96).  SUBJ0 has nothing on or in them.  SUBJ1 has stemView on the child.
 * SUBJ2 has VIEW on a group inside the child.  GrouperAll has no naming privilege on them.</p>
 *
 * <p>Each subject is only ever checked in one role: the stem view privilege table is computed per
 * subject on first use, so a grant made after a subject's first lookup is not seen until the
 * incremental job runs.</p>
 */
public class UiV2MainFolderTreeAuthorizationTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2MainFolderTreeAuthorizationTest("testHiddenFolderLooksLikeMissingInTree"));
  }

  /**
   *
   */
  public UiV2MainFolderTreeAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2MainFolderTreeAuthorizationTest(String name) {
    super(name);
  }

  /** hidden parent */
  private Stem hidden;

  /** hidden child */
  private Stem child;

  /**
   * folders, privileges and the group inside, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.hidden = new StemSave(grouperSession).assignName("test:hidden96").assignDisplayExtension("HIDDEN_TREE_96")
          .assignCreateParentStemsIfNotExist(true).save();
      this.child = new StemSave(grouperSession).assignName("test:hidden96:child").assignDisplayExtension("CHILD_SECRET_96").save();

      // make sure nothing is visible to everyone, whatever the create-grant-all config says
      for (Stem stem : new Stem[] {this.hidden, this.child}) {
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_VIEW, false);
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.CREATE, false);
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_ATTR_READ, false);
      }

      // SUBJ1: a direct naming privilege on the child
      this.child.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_VIEW, false);

      // SUBJ2: no naming privilege, but can see a group inside the child
      Group inside = new GroupSave(grouperSession).assignName("test:hidden96:child:inside").save();
      inside.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      inside.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      inside.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.VIEW, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * remove thread locals
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // denied looks exactly like missing
  // ---------------------------------------------------------------------------------------------

  /**
   * folderMenu prints nothing for a hidden folder (parent or child), the same as a random uuid
   */
  public void testHiddenFolderLooksLikeMissingInTree() {
    String missing = folderMenu(SubjectTestHelper.SUBJ0, "0123456789abcdef0123456789abcdef");
    assertEquals("", missing);

    String parentJson = folderMenu(SubjectTestHelper.SUBJ0, this.hidden.getUuid());
    assertEquals(missing, parentJson);

    String childJson = folderMenu(SubjectTestHelper.SUBJ0, this.child.getUuid());
    assertEquals(missing, childJson);

    // tree path form "a/b/uuid" too
    assertEquals(missing, folderMenu(SubjectTestHelper.SUBJ0, "root/" + this.hidden.getUuid() + "/" + this.child.getUuid()));
  }

  /**
   * folderMenuObjectPath answers a hidden folder (by uuid or by name) with the same error json as a
   * folder that does not exist: no path, no uuids, no existence oracle
   */
  public void testHiddenFolderLooksLikeMissingInObjectPath() {
    String missing = folderMenuObjectPath(SubjectTestHelper.SUBJ0, "stemName", "test:hidden96:doesNotExist");
    assertTrue(missing, missing.contains("\"error\""));

    String byName = folderMenuObjectPath(SubjectTestHelper.SUBJ0, "stemName", "test:hidden96:child");
    assertEquals(missing, byName);

    String parentByName = folderMenuObjectPath(SubjectTestHelper.SUBJ0, "stemName", "test:hidden96");
    assertEquals(missing, parentByName);

    String byUuid = folderMenuObjectPath(SubjectTestHelper.SUBJ0, "stem", this.child.getUuid());
    assertEquals(missing, byUuid);

    for (String json : new String[] {byName, parentByName, byUuid}) {
      assertFalse(json, json.contains(this.hidden.getUuid()));
      assertFalse(json, json.contains(this.child.getUuid()));
    }
  }

  // ---------------------------------------------------------------------------------------------
  // allowed users still navigate
  // ---------------------------------------------------------------------------------------------

  /**
   * a direct naming privilege on the child: the child and its parent show in the tree and the
   * path has both uuids
   */
  public void testDirectPrivilegeCanNavigate() {
    String childJson = folderMenu(SubjectTestHelper.SUBJ1, this.child.getUuid());
    assertTrue(childJson, childJson.contains("CHILD_SECRET_96"));
    assertTrue(childJson, childJson.contains(this.child.getUuid()));

    // the parent is visible because something inside it is
    String parentJson = folderMenu(SubjectTestHelper.SUBJ1, this.hidden.getUuid());
    assertTrue(parentJson, parentJson.contains("HIDDEN_TREE_96"));

    String path = folderMenuObjectPath(SubjectTestHelper.SUBJ1, "stemName", "test:hidden96:child");
    assertFalse(path, path.contains("\"error\""));
    assertTrue(path, path.contains(this.hidden.getUuid()));
    assertTrue(path, path.contains(this.child.getUuid()));
  }

  /**
   * no naming privilege but a viewable group inside: the folder still shows
   */
  public void testObjectInsideCanNavigate() {
    String childJson = folderMenu(SubjectTestHelper.SUBJ2, this.child.getUuid());
    assertTrue(childJson, childJson.contains("CHILD_SECRET_96"));

    String path = folderMenuObjectPath(SubjectTestHelper.SUBJ2, "stem", this.child.getUuid());
    assertFalse(path, path.contains("\"error\""));
    assertTrue(path, path.contains(this.child.getUuid()));
  }

  /**
   * root (GrouperSystem) sees everything, and the root folder path still works for anyone
   */
  public void testRootAndRootFolder() {
    String childJson = folderMenu(SubjectFinder.findRootSubject(), this.child.getUuid());
    assertTrue(childJson, childJson.contains("CHILD_SECRET_96"));

    String rootPath = folderMenuObjectPath(SubjectTestHelper.SUBJ0, "stem", "root");
    assertFalse(rootPath, rootPath.contains("\"error\""));
  }

  // ---------------------------------------------------------------------------------------------
  // the rule itself
  // ---------------------------------------------------------------------------------------------

  /**
   * StemViewAuthorization.canView / filterViewable
   */
  public void testStemViewAuthorization() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Stem rootStem = StemFinder.findRootStem(grouperSession);

      assertFalse(StemViewAuthorization.canView(null, this.child));
      assertFalse(StemViewAuthorization.canView(SubjectTestHelper.SUBJ0, null));
      assertTrue("root folder is always visible", StemViewAuthorization.canView(SubjectTestHelper.SUBJ0, rootStem));

      assertFalse(StemViewAuthorization.canView(SubjectTestHelper.SUBJ0, this.child));
      assertFalse(StemViewAuthorization.canView(SubjectTestHelper.SUBJ0, this.hidden));
      assertTrue(StemViewAuthorization.canView(SubjectTestHelper.SUBJ1, this.child));
      assertTrue(StemViewAuthorization.canView(SubjectTestHelper.SUBJ2, this.child));
      assertTrue(StemViewAuthorization.canView(SubjectTestHelper.SUBJ2, this.hidden));

      List<Stem> stems = new ArrayList<Stem>();
      stems.add(this.child);
      stems.add(null);
      stems.add(rootStem);
      stems.add(this.hidden);

      assertEquals(0, StemViewAuthorization.filterViewable(SubjectTestHelper.SUBJ0, null).size());
      assertEquals(0, StemViewAuthorization.filterViewable(null, stems).size());

      Set<Stem> subj0 = StemViewAuthorization.filterViewable(SubjectTestHelper.SUBJ0, stems);
      assertEquals(1, subj0.size());
      assertTrue(subj0.contains(rootStem));

      // order is kept
      List<Stem> subj2 = new ArrayList<Stem>(StemViewAuthorization.filterViewable(SubjectTestHelper.SUBJ2, stems));
      assertEquals(3, subj2.size());
      assertEquals(this.child, subj2.get(0));
      assertEquals(rootStem, subj2.get(1));
      assertEquals(this.hidden, subj2.get(2));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * call folderMenu
   * @param loggedIn caller
   * @param queryString the folder uuid (or a slash path ending in it)
   * @return what the handler printed
   */
  private static String folderMenu(Subject loggedIn, String queryString) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.queryString = queryString;
    try {
      new UiV2Main().folderMenu(uiRequest.request, uiRequest.response);
    } catch (ControllerDone cd) {
      // normal end of the handler
    } finally {
      UiRequest.clear();
    }
    return uiRequest.output.toString().trim();
  }

  /**
   * call folderMenuObjectPath
   * @param loggedIn caller
   * @param type stem, stemName, group, ...
   * @param id uuid or name
   * @return what the handler printed
   */
  private static String folderMenuObjectPath(Subject loggedIn, String type, String id) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("type", type);
    uiRequest.params.put("id", id);
    try {
      new UiV2Main().folderMenuObjectPath(uiRequest.request, uiRequest.response);
    } catch (ControllerDone cd) {
      // normal end of the handler
    } finally {
      UiRequest.clear();
    }
    return uiRequest.output.toString().trim();
  }

  /**
   * In-memory UI request for calling a UiV2 handler directly: request, session, response (writer
   * output captured) and a servlet whose JSP dispatcher renders nothing, set into GrouperUiFilter's
   * thread locals, with the logged in subject stored in the SessionContainer.
   */
  private static class UiRequest {

    /** request params */
    private Map<String, String> params = new HashMap<String, String>();

    /** query string */
    private String queryString;

    /** request attributes */
    private Map<String, Object> attributes = new HashMap<String, Object>();

    /** session attributes */
    private Map<String, Object> sessionAttributes = new HashMap<String, Object>();

    /** what the handler printed */
    private StringWriter output = new StringWriter();

    /** writer over output */
    private PrintWriter writer = new PrintWriter(this.output);

    /** session */
    private HttpSession session = proxy(HttpSession.class, new InvocationHandler() {
      @Override
      public Object invoke(Object p, Method method, Object[] args) {
        String name = method.getName();
        if ("getAttribute".equals(name)) {
          return UiRequest.this.sessionAttributes.get(args[0]);
        }
        if ("setAttribute".equals(name)) {
          UiRequest.this.sessionAttributes.put((String)args[0], args[1]);
          return null;
        }
        if ("removeAttribute".equals(name)) {
          UiRequest.this.sessionAttributes.remove(args[0]);
          return null;
        }
        if ("getAttributeNames".equals(name)) {
          return Collections.enumeration(UiRequest.this.sessionAttributes.keySet());
        }
        if ("getId".equals(name)) {
          return "junitSession";
        }
        return objectMethodOrDefault(p, method, args);
      }
    });

    /** request */
    private HttpServletRequest request = proxy(HttpServletRequest.class, new InvocationHandler() {
      @Override
      public Object invoke(Object p, Method method, Object[] args) {
        String name = method.getName();
        if ("getParameter".equals(name)) {
          return UiRequest.this.params.get(args[0]);
        }
        if ("getParameterValues".equals(name)) {
          String value = UiRequest.this.params.get(args[0]);
          return value == null ? null : new String[] {value};
        }
        if ("getParameterNames".equals(name)) {
          return Collections.enumeration(UiRequest.this.params.keySet());
        }
        if ("getParameterMap".equals(name)) {
          Map<String, String[]> result = new HashMap<String, String[]>();
          for (String key : UiRequest.this.params.keySet()) {
            result.put(key, new String[] {UiRequest.this.params.get(key)});
          }
          return result;
        }
        if ("getQueryString".equals(name)) {
          return UiRequest.this.queryString;
        }
        if ("getAttribute".equals(name)) {
          return UiRequest.this.attributes.get(args[0]);
        }
        if ("setAttribute".equals(name)) {
          UiRequest.this.attributes.put((String)args[0], args[1]);
          return null;
        }
        if ("removeAttribute".equals(name)) {
          UiRequest.this.attributes.remove(args[0]);
          return null;
        }
        if ("getAttributeNames".equals(name)) {
          return Collections.enumeration(UiRequest.this.attributes.keySet());
        }
        if ("getSession".equals(name)) {
          return UiRequest.this.session;
        }
        if ("getLocale".equals(name)) {
          return Locale.US;
        }
        if ("getLocales".equals(name)) {
          return Collections.enumeration(Collections.singletonList(Locale.US));
        }
        if ("getMethod".equals(name)) {
          return "POST";
        }
        if ("getRequestURI".equals(name)) {
          return "/grouper/grouperUi/app/UiV2Main.folderMenu";
        }
        if ("getContextPath".equals(name)) {
          return "/grouper";
        }
        if ("getCharacterEncoding".equals(name)) {
          return "UTF-8";
        }
        if ("getRemoteUser".equals(name)) {
          return UiRequest.this.loggedIn.getId();
        }
        return objectMethodOrDefault(p, method, args);
      }
    });

    /** response, writer output is captured */
    private HttpServletResponse response = proxy(HttpServletResponse.class, new InvocationHandler() {
      @Override
      public Object invoke(Object p, Method method, Object[] args) {
        String name = method.getName();
        if ("getWriter".equals(name)) {
          return UiRequest.this.writer;
        }
        if ("getCharacterEncoding".equals(name)) {
          return "UTF-8";
        }
        return objectMethodOrDefault(p, method, args);
      }
    });

    /** servlet whose JSP includes do nothing */
    private HttpServlet servlet = new HttpServlet() {
      private static final long serialVersionUID = 1L;
      @Override
      public ServletContext getServletContext() {
        return proxy(ServletContext.class, new InvocationHandler() {
          @Override
          public Object invoke(Object p, Method method, Object[] args) {
            if ("getRequestDispatcher".equals(method.getName())) {
              return proxy(RequestDispatcher.class, new InvocationHandler() {
                @Override
                public Object invoke(Object p2, Method method2, Object[] args2) {
                  return objectMethodOrDefault(p2, method2, args2);
                }
              });
            }
            return objectMethodOrDefault(p, method, args);
          }
        });
      }
    };

    /** logged in subject */
    private Subject loggedIn;

    /**
     * set up the thread locals for a request by this subject
     * @param theLoggedIn subject
     */
    private UiRequest(Subject theLoggedIn) {
      this.loggedIn = theLoggedIn;
      setThreadLocal("threadLocalRequest", this.request);
      setThreadLocal("threadLocalResponse", this.response);
      setThreadLocal("threadLocalServlet", this.servlet);
      SessionContainer.retrieveFromSession().setSubjectLoggedIn(theLoggedIn);
    }

    /**
     * remove the thread locals
     */
    private static void clear() {
      setThreadLocal("threadLocalRequest", null);
      setThreadLocal("threadLocalResponse", null);
      setThreadLocal("threadLocalServlet", null);
    }

    /**
     * @param fieldName GrouperUiFilter thread local field
     * @param value value or null to remove
     */
    @SuppressWarnings("unchecked")
    private static void setThreadLocal(String fieldName, Object value) {
      try {
        Field field = GrouperUiFilter.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        ThreadLocal<Object> threadLocal = (ThreadLocal<Object>)field.get(null);
        if (value == null) {
          threadLocal.remove();
        } else {
          threadLocal.set(value);
        }
      } catch (Exception e) {
        throw new RuntimeException("cant set GrouperUiFilter." + fieldName, e);
      }
    }

    /**
     * @param <T> interface
     * @param theInterface interface
     * @param handler handler
     * @return proxy
     */
    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> theInterface, InvocationHandler handler) {
      return (T)Proxy.newProxyInstance(UiRequest.class.getClassLoader(),
          new Class<?>[] {theInterface}, handler);
    }

    /**
     * equals / hashCode / toString, else a default for the return type (null, false, 0)
     * @param p proxy
     * @param method method
     * @param args args
     * @return value
     */
    private static Object objectMethodOrDefault(Object p, Method method, Object[] args) {
      String name = method.getName();
      if ("hashCode".equals(name)) {
        return System.identityHashCode(p);
      }
      if ("equals".equals(name)) {
        return p == args[0];
      }
      if ("toString".equals(name)) {
        return "junitProxy";
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
  }
}
