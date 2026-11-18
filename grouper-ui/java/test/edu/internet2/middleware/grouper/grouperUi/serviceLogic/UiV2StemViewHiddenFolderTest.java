package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue viewStemHiddenFolderDirectLink (GRP-5409 backport): UiV2Stem.retrieveStemHelper
 * loaded the folder of the request's stemId, stemName or stemIndex with the static StemFinder (a
 * lookup, not an authorization check) and checked no view privilege, so viewStem and the other
 * folder screens showed a hidden folder's name, id and description to any logged in user, and
 * stemIndex let them walk every folder.  Fixed in 5.10.0 by GRP-5409; this test covers the
 * backport (2.6.19, 4.x, 5.0.0-5.9.1) and passes unchanged on 5.10.0 and later.</p>
 *
 * <p>Calls the public static UiV2Stem.retrieveStemHelper(request, false) (what viewStem calls)
 * with an in-memory request, session and response set into GrouperUiFilter's thread locals, in a
 * grouper session started as the logged in subject like the handlers do.</p>
 *
 * <p>Setup: hidden folder test:hidden150 with child test:hidden150:child.  SUBJ0 has nothing on
 * or in them.  SUBJ1 has stemView on the child.  SUBJ2 has VIEW on a group inside the child.
 * GrouperAll has no naming privilege on them.  Each subject is only ever checked in one role: the
 * stem view privilege table is computed per subject on first use.</p>
 */
public class UiV2StemViewHiddenFolderTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2StemViewHiddenFolderTest("testHiddenFolderLooksLikeMissing"));
  }

  /**
   *
   */
  public UiV2StemViewHiddenFolderTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2StemViewHiddenFolderTest(String name) {
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
      this.hidden = new StemSave(grouperSession).assignName("test:hidden150").assignDisplayExtension("HIDDEN_FOLDER_150")
          .assignCreateParentStemsIfNotExist(true).save();
      this.child = new StemSave(grouperSession).assignName("test:hidden150:child").assignDisplayExtension("CHILD_SECRET_150").save();

      // make sure nothing is visible to everyone, whatever the create-grant-all config says
      for (Stem stem : new Stem[] {this.hidden, this.child}) {
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_VIEW, false);
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.CREATE, false);
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_ATTR_READ, false);
      }

      // SUBJ1: a direct naming privilege on the child
      this.child.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.STEM_VIEW, false);

      // SUBJ2: no naming privilege, but can see a group inside the child
      Group inside = new GroupSave(grouperSession).assignName("test:hidden150:child:inside").save();
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
   * a user with nothing on or in the folder gets no folder (by id, name or index), the same error
   * as a folder that does not exist
   */
  public void testHiddenFolderLooksLikeMissing() {
    Lookup missing = lookup(SubjectTestHelper.SUBJ0, "stemName", "test:hidden150:doesNotExist");
    assertNull(missing.stem);
    assertTrue(missing.addedError);

    for (Stem stem : new Stem[] {this.hidden, this.child}) {
      Lookup byId = lookup(SubjectTestHelper.SUBJ0, "stemId", stem.getUuid());
      Lookup byName = lookup(SubjectTestHelper.SUBJ0, "stemName", stem.getName());
      Lookup byIndex = lookup(SubjectTestHelper.SUBJ0, "stemIndex", String.valueOf(stem.getIdIndex()));
      for (Lookup denied : new Lookup[] {byId, byName, byIndex}) {
        assertNull(stem.getName(), denied.stem);
        assertTrue(stem.getName(), denied.addedError);
        assertEquals(stem.getName() + ", same response as a missing folder", missing.actionCount, denied.actionCount);
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // allowed users still see the folder
  // ---------------------------------------------------------------------------------------------

  /**
   * a direct naming privilege on the child: the child shows, and so does its parent (something
   * inside it is visible)
   */
  public void testDirectPrivilegeCanView() {
    Lookup childLookup = lookup(SubjectTestHelper.SUBJ1, "stemId", this.child.getUuid());
    assertNotNull(childLookup.stem);
    assertFalse(childLookup.addedError);
    assertEquals(this.child.getUuid(), childLookup.stem.getUuid());

    Lookup parentLookup = lookup(SubjectTestHelper.SUBJ1, "stemName", this.hidden.getName());
    assertNotNull("parent of a visible folder", parentLookup.stem);
  }

  /**
   * no naming privilege but a viewable group inside: the folder and its parent still show
   */
  public void testObjectInsideCanView() {
    Lookup childLookup = lookup(SubjectTestHelper.SUBJ2, "stemIndex", String.valueOf(this.child.getIdIndex()));
    assertNotNull(childLookup.stem);
    assertEquals(this.child.getUuid(), childLookup.stem.getUuid());

    Lookup parentLookup = lookup(SubjectTestHelper.SUBJ2, "stemId", this.hidden.getUuid());
    assertNotNull(parentLookup.stem);
  }

  /**
   * root (GrouperSystem) sees everything, and the root folder still opens for a user who can see
   * something in the registry (the root folder, like any folder, is visible when something inside
   * it is; whether a user with nothing at all sees it depends on what GrouperAll can see)
   */
  public void testRootAndRootFolder() {
    Lookup childLookup = lookup(SubjectFinder.findRootSubject(), "stemName", this.child.getName());
    assertNotNull(childLookup.stem);

    Lookup rootFolder = lookup(SubjectTestHelper.SUBJ1, "stemId", "root");
    assertNotNull("the root folder", rootFolder.stem);
    assertFalse(rootFolder.addedError);
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * result of one retrieveStemHelper call
   */
  private static class Lookup {

    /** the folder the screen would show, or null */
    private Stem stem;

    /** if the helper added an error */
    private boolean addedError;

    /** how many screen actions (messages, redirects) the response got */
    private int actionCount;
  }

  /**
   * call UiV2Stem.retrieveStemHelper(request, false) as this subject, the way viewStem does
   * @param loggedIn caller
   * @param paramName stemId, stemName or stemIndex
   * @param paramValue value
   * @return the result
   */
  private static Lookup lookup(Subject loggedIn, String paramName, String paramValue) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put(paramName, paramValue);
    GrouperSession grouperSession = GrouperSession.start(loggedIn);
    try {
      UiV2Stem.RetrieveStemHelperResult result = UiV2Stem.retrieveStemHelper(uiRequest.request, false);
      Lookup lookup = new Lookup();
      lookup.stem = result.getStem();
      lookup.addedError = result.isAddedError();
      lookup.actionCount = GuiResponseJs.retrieveGuiResponseJs().getActions() == null ? 0
          : GuiResponseJs.retrieveGuiResponseJs().getActions().size();
      return lookup;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
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
          return "/grouper/grouperUi/app/UiV2Stem.viewStem";
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
