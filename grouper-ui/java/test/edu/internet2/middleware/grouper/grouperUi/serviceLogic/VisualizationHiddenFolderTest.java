package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.graph.GraphNode;
import edu.internet2.middleware.grouper.app.graph.RelationGraph;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.VisualizationContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue visualizationHiddenChildFolders: with "show folders" on, the visualization
 * graph (RelationGraph.buildChildNodes) added every child folder of a folder node from
 * Stem.getChildStems(), which reads the folder table with no privilege check, so any user who
 * could see a folder got the names, ids and descriptions of every folder below it.  And before
 * GRP-7387 (7.6.0, 6.5.0, 4.26.0) VisualizationContainer.getGrouperObject opened a start folder
 * by uuid without checking that the user can see it.</p>
 *
 * <p>Builds the graph the way UiV2Visualization.buildGraph does, in a grouper session of the
 * user, and calls VisualizationContainer.getGrouperObject with an in-memory UI request (the
 * same harness as UiV2StemViewHiddenFolderTest).  Same file for every release (Java 8 syntax for
 * 2.6.19).  Goes in grouper-ui/java/test/edu/internet2/middleware/grouper/grouperUi/serviceLogic/</p>
 *
 * <p>Setup: test:vis167 with child folders shown (a group in it SUBJ0 can VIEW) and hidden (a
 * group SUBJ0 cannot see), and hidden:deeper.  SUBJ0 has no naming privilege anywhere.  GrouperAll
 * has no naming privilege on these folders.</p>
 */
public class VisualizationHiddenFolderTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new VisualizationHiddenFolderTest("testGraphLeavesOutHiddenChildFolders"));
  }

  /**
   *
   */
  public VisualizationHiddenFolderTest() {
    super();
  }

  /**
   * @param name
   */
  public VisualizationHiddenFolderTest(String name) {
    super(name);
  }

  /** start folder */
  private Stem top;

  /** child folder SUBJ0 can see something in */
  private Stem shown;

  /** child folder SUBJ0 cannot see */
  private Stem hidden;

  /** grandchild under hidden */
  private Stem deeper;

  /**
   * folders, privileges and groups, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    // folders are hidden by default; make sure this test runs that way
    edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .put("security.folders.are.viewable.by.all", "false");
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.top = new StemSave(grouperSession).assignName("test:vis167").assignCreateParentStemsIfNotExist(true).save();
      this.shown = new StemSave(grouperSession).assignName("test:vis167:shown").save();
      this.hidden = new StemSave(grouperSession).assignName("test:vis167:hidden")
          .assignDisplayExtension("HIDDEN_FOLDER_167").assignDescription("secret description 167").save();
      this.deeper = new StemSave(grouperSession).assignName("test:vis167:hidden:deeper").save();

      // make sure nothing is visible to everyone, whatever the create-grant-all config says
      for (Stem stem : new Stem[] {this.top, this.shown, this.hidden, this.deeper}) {
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_VIEW, false);
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.CREATE, false);
        stem.revokePriv(SubjectFinder.findAllSubject(), NamingPrivilege.STEM_ATTR_READ, false);
      }

      // SUBJ0 can see a group in shown, so shown and its parents are visible to SUBJ0
      Group shownGroup = new GroupSave(grouperSession).assignName("test:vis167:shown:group").save();
      shownGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      shownGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      shownGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

      // nothing in hidden that SUBJ0 can see
      Group hiddenGroup = new GroupSave(grouperSession).assignName("test:vis167:hidden:deeper:group").save();
      hiddenGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      hiddenGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
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
    edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertiesOverrideMap()
        .remove("security.folders.are.viewable.by.all");
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // child folders in the graph
  // ---------------------------------------------------------------------------------------------

  /**
   * the graph from test:vis167 with folders shown has the child folder SUBJ0 can see, not the
   * hidden one or anything under it
   */
  public void testGraphLeavesOutHiddenChildFolders() {
    Set<String> names = graphNodeNames(SubjectTestHelper.SUBJ0, this.top);
    assertTrue(names.toString(), names.contains(this.top.getName()));
    assertTrue(names.toString(), names.contains(this.shown.getName()));
    assertFalse(names.toString(), names.contains(this.hidden.getName()));
    assertFalse(names.toString(), names.contains(this.deeper.getName()));
  }

  /**
   * root still gets every child folder
   */
  public void testRootGetsAllChildFolders() {
    Set<String> names = graphNodeNames(SubjectFinder.findRootSubject(), this.top);
    assertTrue(names.toString(), names.contains(this.shown.getName()));
    assertTrue(names.toString(), names.contains(this.hidden.getName()));
    assertTrue(names.toString(), names.contains(this.deeper.getName()));
  }

  // ---------------------------------------------------------------------------------------------
  // start folder by uuid
  // ---------------------------------------------------------------------------------------------

  /**
   * a start folder the user cannot see is not found, the same as one that does not exist; a folder
   * the user can see still opens
   */
  public void testStartFolderNeedsView() {
    assertNull("hidden start folder", startObject(SubjectTestHelper.SUBJ0, this.hidden.getUuid()));
    assertNull("missing start folder", startObject(SubjectTestHelper.SUBJ0, "doesNotExist167"));

    GrouperObject shownObject = startObject(SubjectTestHelper.SUBJ0, this.shown.getUuid());
    assertNotNull("visible start folder", shownObject);
    assertEquals(this.shown.getName(), shownObject.getName());

    assertNotNull("root", startObject(SubjectFinder.findRootSubject(), this.hidden.getUuid()));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * build the graph from a folder with folders shown and no limits, as this subject
   * @param subject the user
   * @param start the start folder
   * @return names of the folder and group nodes
   */
  private static Set<String> graphNodeNames(Subject subject, Stem start) {
    GrouperSession grouperSession = GrouperSession.start(subject);
    try {
      RelationGraph relationGraph = new RelationGraph()
          .assignStartObject(start)
          .assignParentLevels(0)
          .assignChildLevels(-1)
          .assignShowStems(true)
          .assignMaxSiblings(-1);
      relationGraph.build();
      Set<String> names = new HashSet<String>();
      for (GraphNode graphNode : relationGraph.getNodes()) {
        if (graphNode.getGrouperObject() != null) {
          names.add(graphNode.getGrouperObject().getName());
        }
      }
      return names;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * VisualizationContainer.getGrouperObject for a folder uuid, as this logged in user
   * @param loggedIn the user
   * @param stemId the folder uuid
   * @return the start object, or null if it is not found
   */
  private static GrouperObject startObject(Subject loggedIn, String stemId) {
    new UiRequest(loggedIn);
    GrouperSession grouperSession = GrouperSession.start(loggedIn);
    try {
      VisualizationContainer visualizationContainer = new VisualizationContainer();
      visualizationContainer.setObjectType("stem");
      visualizationContainer.setObjectId(stemId);
      try {
        return visualizationContainer.getGrouperObject();
      } catch (RuntimeException re) {
        // StemNotFoundException, for a missing folder and (with the fix) a hidden one
        return null;
      }
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
