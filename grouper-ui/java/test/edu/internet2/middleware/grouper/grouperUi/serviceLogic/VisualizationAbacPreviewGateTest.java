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
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.abac.GrouperJexlScriptAnalysis;
import edu.internet2.middleware.grouper.abac.GrouperLoaderJexlScriptFullSync;
import edu.internet2.middleware.grouper.app.graph.RelationGraph;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.dataField.GrouperDataEngine;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.VisualizationContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyType;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheDependencyTypeDao;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroup;
import edu.internet2.middleware.grouper.sqlCache.SqlCacheGroupDao;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue visualizationAbacPreviewAnalysisNoGate, two parts:</p>
 *
 * <p>1. UiV2Visualization.buildGraph with overrideAbacScript (the scripted group script preview,
 * 6.2.0 / 7.2.0 and later) ran the scripted group analysis on a script from the request for
 * anybody who could see the start object, while the Analyze screen
 * (UiV2GrouperLoader.editGrouperLoaderAnalyze) needs loader edit or ABAC / recent memberships
 * edit on the group.  The preview now needs the same.</p>
 *
 * <p>2. GrouperLoaderJexlScriptFullSync.analyzeJexlScriptHtml stored a mshipHistory_abac
 * dependency and synced membership history for every entity.recentMemberOf group of the script
 * (5.15.4 and later) before any privilege check, also when only analyzing.  Now only for groups
 * the user can READ.</p>
 *
 * <p>Same file for every patched release (5.15.4 and later): the preview tests print a skip line
 * on releases without the preview, and the analysis is called by reflection (5.15.4 to 5.17.0 have
 * no read only argument).  Calls the UI handler with an in-memory request (the same harness as
 * UiV2StemViewHiddenFolderTest).  Goes in
 * grouper-ui/java/test/edu/internet2/middleware/grouper/grouperUi/serviceLogic/</p>
 *
 * <p>Setup: test:abac178 (SUBJ0 can VIEW it, SUBJ1 is an ADMIN), test:readable178 (SUBJ0 can READ),
 * test:viewOnly178 (SUBJ0 can VIEW) and secret:hidden178 (SUBJ0 has nothing), all with a member,
 * and the sql cache synced.  grouper.abac.edit.if.in.group and uiV2.loader.edit.if.in.group are
 * blank, so ADMIN on the group is enough to edit its script.</p>
 */
public class VisualizationAbacPreviewGateTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new VisualizationAbacPreviewGateTest("testPreviewDeniedForViewer"));
  }

  /**
   *
   */
  public VisualizationAbacPreviewGateTest() {
    super();
  }

  /**
   * @param name
   */
  public VisualizationAbacPreviewGateTest(String name) {
    super(name);
  }

  /** the group the preview starts from */
  private Group abacGroup;

  /**
   * groups, privileges and the sql cache, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.abac.edit.if.in.group", "");
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put("uiV2.loader.edit.if.in.group", "");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.abacGroup = new GroupSave(grouperSession).assignName("test:abac178").assignCreateParentStemsIfNotExist(true).save();
      this.abacGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      this.abacGroup.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);

      Group readable = new GroupSave(grouperSession).assignName("test:readable178").assignCreateParentStemsIfNotExist(true).save();
      readable.addMember(SubjectTestHelper.SUBJ2, false);
      readable.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

      Group viewOnly = new GroupSave(grouperSession).assignName("test:viewOnly178").assignCreateParentStemsIfNotExist(true).save();
      viewOnly.addMember(SubjectTestHelper.SUBJ3, false);
      viewOnly.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);

      Group hidden = new GroupSave(grouperSession).assignName("secret:hidden178").assignCreateParentStemsIfNotExist(true).save();
      hidden.addMember(SubjectTestHelper.SUBJ4, false);

      // the sql cache, as the daemons would
      GrouperLoader.runOnceByJobName(grouperSession, "CHANGE_LOG_changeLogTempToChangeLog");
      GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_sqlCacheFullSync");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * remove thread locals and overrides
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.create.grant.all.read");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("groups.create.grant.all.view");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouper.abac.edit.if.in.group");
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().remove("uiV2.loader.edit.if.in.group");
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // 1. the script preview needs the Analyze screen's check
  // ---------------------------------------------------------------------------------------------

  /**
   * a user who can only see the group gets an error and no graph is started
   */
  public void testPreviewDeniedForViewer() {
    if (!releaseHasPreview()) {
      System.out.println("skip testPreviewDeniedForViewer: this release has no scripted group preview in the visualization");
      return;
    }

    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.put("objectType", "group");
    uiRequest.params.put("objectId", this.abacGroup.getId());
    uiRequest.params.put("drawModule", "text");
    uiRequest.params.put("overrideAbacScript", "entity.memberOf('test:readable178')");
    uiRequest.params.put("visualizationAddMemberComboName", SubjectTestHelper.SUBJ2.getSourceId() + "||" + SubjectTestHelper.SUBJ2.getId());
    try {
      new UiV2Visualization().buildGraph(uiRequest.request, uiRequest.response);

      VisualizationContainer visualizationContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getVisualizationContainer();
      assertNull("no graph was started", visualizationContainer.getUniqueId());

      boolean hasMessage = false;
      if (GuiResponseJs.retrieveGuiResponseJs().getActions() != null) {
        for (GuiScreenAction guiScreenAction : GuiResponseJs.retrieveGuiResponseJs().getActions()) {
          if (guiScreenAction.getMessage() != null && guiScreenAction.getMessage().length() > 0) {
            hasMessage = true;
          }
        }
      }
      assertTrue("an error message", hasMessage);
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * the preview check itself: the group's ABAC editor and root may preview, the viewer may not,
   * and nobody may preview on a folder
   */
  public void testPreviewCheck() {
    if (!releaseHasPreview()) {
      System.out.println("skip testPreviewCheck: this release has no scripted group preview in the visualization");
      return;
    }
    assertFalse("viewer", canPreview(SubjectTestHelper.SUBJ0, "group", this.abacGroup.getId()));
    assertTrue("ADMIN on the group", canPreview(SubjectTestHelper.SUBJ1, "group", this.abacGroup.getId()));
    assertTrue("root", canPreview(SubjectFinder.findRootSubject(), "group", this.abacGroup.getId()));

    GrouperSession grouperSession = GrouperSession.startRootSession();
    Stem testStem = null;
    try {
      testStem = StemFinder.findByName(grouperSession, "test", true);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertFalse("a folder", canPreview(SubjectFinder.findRootSubject(), "stem", testStem.getUuid()));
  }

  // ---------------------------------------------------------------------------------------------
  // 2. membership history only for recentMemberOf groups the user can READ
  // ---------------------------------------------------------------------------------------------

  /**
   * analyzing a script with recentMemberOf of a group the user cannot see, or can only see, adds
   * no history dependency for it; one the user can read still gets one
   */
  public void testRecentMemberOfHistoryNeedsRead() {
    if (!releaseHasRecentMemberOf()) {
      System.out.println("skip testRecentMemberOfHistoryNeedsRead: this release has no recentMemberOf");
      return;
    }

    // or this would test nothing
    for (String groupName : new String[] {"secret:hidden178", "test:viewOnly178", "test:readable178"}) {
      assertNotNull(groupName + " in the sql cache", sqlCacheGroupInternalId(groupName));
      assertFalse(groupName + " has no history yet", hasHistoryDependency(groupName));
    }

    analyzeAsSubj0("entity.recentMemberOf('secret:hidden178', '700 days')");
    assertFalse("hidden group", hasHistoryDependency("secret:hidden178"));

    analyzeAsSubj0("entity.recentMemberOf('test:viewOnly178', '700 days')");
    assertFalse("view only group", hasHistoryDependency("test:viewOnly178"));

    analyzeAsSubj0("entity.recentMemberOf('test:readable178', '700 days') || entity.recentMemberOf('secret:hidden178', '700 days')");
    assertTrue("readable group", hasHistoryDependency("test:readable178"));
    assertFalse("hidden group next to a readable one", hasHistoryDependency("secret:hidden178"));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @return true if this release's visualization has the scripted group preview (6.2.0, 7.2.0 on)
   */
  private static boolean releaseHasPreview() {
    try {
      RelationGraph.class.getMethod("assignOverrideAbacScript", String.class);
      return true;
    } catch (NoSuchMethodException nsme) {
      return false;
    }
  }

  /**
   * recentMemberOf came in 5.15.4.  it keeps its group names on the data engine (5.15.4 to 5.17.1),
   * in a thread local of GrouperLoaderJexlScriptFullSync (5.17.2), or on the analysis (5.18.0 on)
   * @return true if this release's scripts can use recentMemberOf
   */
  private static boolean releaseHasRecentMemberOf() {
    try {
      GrouperJexlScriptAnalysis.class.getMethod("getRecentMemberOfGroupNames");
      return true;
    } catch (NoSuchMethodException nsme) {
      // not this way
    }
    try {
      GrouperDataEngine.class.getMethod("getRecentMemberOfGroupNames");
      return true;
    } catch (NoSuchMethodException nsme) {
      // not this way
    }
    try {
      GrouperLoaderJexlScriptFullSync.class.getDeclaredField("recentMemberOfGroupNames");
      return true;
    } catch (NoSuchFieldException nsfe) {
      return false;
    }
  }

  /**
   * UiV2Visualization.canPreviewAbacScript (the fix) for a start object, as this logged in user
   * @param loggedIn the user
   * @param objectType group or stem
   * @param objectId the uuid
   * @return the answer
   */
  private static boolean canPreview(Subject loggedIn, String objectType, String objectId) {
    new UiRequest(loggedIn);
    GrouperSession grouperSession = GrouperSession.start(loggedIn);
    try {
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      VisualizationContainer visualizationContainer = grouperRequestContainer.getVisualizationContainer();
      visualizationContainer.setObjectType(objectType);
      visualizationContainer.setObjectId(objectId);
      Method method = UiV2Visualization.class.getDeclaredMethod("canPreviewAbacScript",
          GrouperRequestContainer.class, VisualizationContainer.class);
      method.setAccessible(true);
      return (Boolean)method.invoke(null, grouperRequestContainer, visualizationContainer);
    } catch (Exception e) {
      throw new RuntimeException("canPreviewAbacScript", e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  /**
   * the read only analysis of a script as SUBJ0, the way the Analyze button and the preview run it
   * (5.15.4 to 5.17.0 have no read only argument and always analyze)
   * @param script the script
   */
  private static void analyzeAsSubj0(String script) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      GrouperDataEngine grouperDataEngine = new GrouperDataEngine();
      grouperDataEngine.loadFieldsAndRows(GrouperConfig.retrieveConfig());
      Method withReadOnly = null;
      Method withoutReadOnly = null;
      for (Method method : GrouperLoaderJexlScriptFullSync.class.getMethods()) {
        if (!"analyzeJexlScriptHtml".equals(method.getName())) {
          continue;
        }
        Class<?>[] types = method.getParameterTypes();
        if (types.length == 5 && types[4] == boolean.class) {
          withReadOnly = method;
        } else if (types.length == 4) {
          withoutReadOnly = method;
        }
      }
      if (withReadOnly != null) {
        withReadOnly.invoke(null, grouperDataEngine, script, null, SubjectTestHelper.SUBJ0, true);
      } else {
        withoutReadOnly.invoke(null, grouperDataEngine, script, null, SubjectTestHelper.SUBJ0);
      }
    } catch (Exception e) {
      throw new RuntimeException("analyzeJexlScriptHtml: " + script, e);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param groupName a group name
   * @return the internal id of its members row in the sql cache, or null
   */
  private static Long sqlCacheGroupInternalId(String groupName) {
    Map<MultiKey, SqlCacheGroup> sqlCacheGroups = SqlCacheGroupDao.retrieveByGroupNamesFieldNames(
        GrouperUtil.toList(new MultiKey(groupName, "members")));
    if (GrouperUtil.length(sqlCacheGroups) != 1) {
      return null;
    }
    return sqlCacheGroups.values().iterator().next().getInternalId();
  }

  /**
   * @param groupName a group name
   * @return true if the group has the mshipHistory_abac dependency an analysis adds
   */
  private static boolean hasHistoryDependency(String groupName) {
    Long internalId = sqlCacheGroupInternalId(groupName);
    if (internalId == null) {
      return false;
    }
    SqlCacheDependencyType sqlCacheDependencyType = SqlCacheDependencyTypeDao
        .retrieveByDependencyCategoryAndName("mshipHistory", "mshipHistory_abac");
    return SqlCacheDependencyDao.retrieveByDepTypeInternalIdOwnerInternalIdDependentInternalId(
        sqlCacheDependencyType.getInternalId(), internalId, internalId) != null;
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
