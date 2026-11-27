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
package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.AdminContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>SECURITY: only a Grouper sysadmin (wheel or root) can add a daemon.  A user who may see the daemon jobs
 * screen (uiV2.admin.daemonJobs.must.be.in.group) but is not a sysadmin used to get the Add daemon link and
 * the add form; the save refused them.  Now AdminContainer.isCanAddDaemon hides the link
 * (adminDaemonJobsMoreActionsButtonContents.jsp), and UiV2Admin.addDaemon and addDaemonSubmit refuse such a
 * user with GrouperDaemonCodeAuthorization.NOT_ALLOWED_MESSAGE before the form is shown or anything is
 * read from the request.</p>
 *
 * <p>Calls the handlers with an in-memory request, session and response set into GrouperUiFilter's thread
 * locals, like the UI servlet does.  Root gets the form (positive control).</p>
 */
public class UiV2AdminAddDaemonAuthorizationTest extends GrouperTest {

  /** group whose members may see the daemon jobs screen */
  private static final String DAEMON_VIEWERS_GROUP = "test:addDaemonAuthz:daemonViewers";

  /** where the add daemon form goes */
  private static final String MAIN_CONTENT_HANDLE = "#grouperMainContentDivId";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2AdminAddDaemonAuthorizationTest("testDaemonViewerCannotOpenAddDaemon"));
  }

  /**
   *
   */
  public UiV2AdminAddDaemonAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AdminAddDaemonAuthorizationTest(String name) {
    super(name);
  }

  /**
   * test.subject.0 may see the daemon jobs screen (member of the viewers group) but is not a sysadmin
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(grouperSession).assignName(DAEMON_VIEWERS_GROUP).assignCreateParentStemsIfNotExist(true).save();
      group.addMember(SubjectTestHelper.SUBJ0, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put("uiV2.admin.daemonJobs.show", "true");
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put("uiV2.admin.daemonJobs.must.be.in.group", DAEMON_VIEWERS_GROUP);
  }

  /**
   * remove thread locals and config overrides
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().remove("uiV2.admin.daemonJobs.show");
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().remove("uiV2.admin.daemonJobs.must.be.in.group");
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------------

  /**
   * a daemon viewer who is not a sysadmin: no Add daemon link, and isCanAddDaemon is false; root: true
   */
  public void testCanAddDaemonOnlyForSysadmin() {
    assertFalse("a daemon viewer who is not a sysadmin cannot add a daemon", canAddDaemon(SubjectTestHelper.SUBJ0));
    assertTrue("root can add a daemon", canAddDaemon(SubjectFinder.findRootSubject()));
  }

  /**
   * the Add daemon menu entry is inside the canAddDaemon check in the JSP
   * @throws Exception
   */
  public void testAddDaemonLinkIsInsideCanAddDaemonCheck() throws Exception {
    String jsp = readRepoFile("grouper-ui/webapp/WEB-INF/grouperUi2/admin/adminDaemonJobsMoreActionsButtonContents.jsp");
    int ifStart = jsp.indexOf("<c:if test=\"${grouperRequestContainer.adminContainer.canAddDaemon}\">");
    assertTrue("the menu has the canAddDaemon check", ifStart >= 0);
    int ifEnd = jsp.indexOf("</c:if>", ifStart);
    // the link names the operation twice (href and onclick): every mention must be inside the check
    int mentions = 0;
    for (int link = jsp.indexOf("operation=UiV2Admin.addDaemon"); link >= 0; link = jsp.indexOf("operation=UiV2Admin.addDaemon", link + 1)) {
      assertTrue("the Add daemon link is inside the canAddDaemon check", link > ifStart && link < ifEnd);
      mentions++;
    }
    assertTrue("the menu has the Add daemon link", mentions > 0);
  }

  /**
   * a daemon viewer who is not a sysadmin gets the not-allowed error from addDaemon, and no form
   */
  public void testDaemonViewerCannotOpenAddDaemon() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("daemonConfigType", "edu.internet2.middleware.grouper.app.daemon.GrouperDaemonOtherJobObjectTypeConfiguration");
    params.put("daemonConfigId", "addDaemonAuthzTest");
    assertRefused(callHandler("addDaemon", SubjectTestHelper.SUBJ0, params));
  }

  /**
   * a daemon viewer who is not a sysadmin gets the not-allowed error from addDaemonSubmit
   */
  public void testDaemonViewerCannotSubmitAddDaemon() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("daemonConfigType", "edu.internet2.middleware.grouper.app.daemon.GrouperDaemonOtherJobObjectTypeConfiguration");
    params.put("daemonConfigId", "addDaemonAuthzTest");
    assertRefused(callHandler("addDaemonSubmit", SubjectTestHelper.SUBJ0, params));
  }

  /**
   * positive control: root gets the add daemon form
   */
  public void testRootGetsAddDaemonForm() {
    Call call = callHandler("addDaemon", SubjectFinder.findRootSubject(), new HashMap<String, String>());
    assertNull("root: " + call.exception, call.exception);
    assertFalse("root gets no error: " + call.describe(), call.hasErrorMessage());
    assertTrue("root gets the form: " + call.describe(), call.handles.contains(MAIN_CONTENT_HANDLE));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @param subject logged in subject
   * @return AdminContainer.isCanAddDaemon for that subject
   */
  private static boolean canAddDaemon(Subject subject) {
    new UiRequest(subject);
    try {
      return new AdminContainer().isCanAddDaemon();
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * refused: no exception, an error message, no form
   * @param call result
   */
  private static void assertRefused(Call call) {
    assertNull(call.handler + ": refused without an exception, got: " + call.exception, call.exception);
    assertTrue(call.handler + ": gets the not-allowed error: " + call.describe(), call.hasErrorMessage());
    assertFalse(call.handler + ": gets no form: " + call.describe(), call.handles.contains(MAIN_CONTENT_HANDLE));
  }

  /**
   * read a repo file whether the test runs from the repo root or a module (Eclipse working dirs differ)
   * @param repoPath e.g. grouper-ui/webapp/...
   * @return the contents
   * @throws Exception
   */
  private static String readRepoFile(String repoPath) throws Exception {
    String withoutModule = repoPath.substring(repoPath.indexOf('/') + 1);
    for (String candidate : new String[] {repoPath, withoutModule, "../" + repoPath, "../../" + repoPath}) {
      java.io.File file = new java.io.File(candidate);
      if (file.isFile()) {
        return new String(java.nio.file.Files.readAllBytes(file.toPath()), java.nio.charset.StandardCharsets.UTF_8);
      }
    }
    fail("Cannot find " + repoPath + " from " + new java.io.File(".").getAbsolutePath());
    return null;
  }

  /**
   * call a UiV2Admin handler as this subject, the way the UI servlet does (the handler starts its own
   * grouper session), and collect the screen actions
   * @param handler UiV2Admin method name
   * @param loggedIn caller
   * @param params request params
   * @return the result
   */
  private static Call callHandler(String handler, Subject loggedIn, Map<String, String> params) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.requestUri = "/grouper/grouperUi/app/UiV2Admin." + handler;
    uiRequest.params.putAll(params);
    Call call = new Call();
    call.handler = handler;
    try {
      try {
        Method method = UiV2Admin.class.getMethod(handler, HttpServletRequest.class, HttpServletResponse.class);
        method.invoke(new UiV2Admin(), uiRequest.request, uiRequest.response);
      } catch (InvocationTargetException ite) {
        if (ite.getCause() instanceof RuntimeException) {
          call.exception = (RuntimeException)ite.getCause();
        } else {
          call.exception = new RuntimeException(ite.getCause());
        }
      } catch (NoSuchMethodException nsme) {
        throw new RuntimeException("no handler " + handler, nsme);
      } catch (IllegalAccessException iae) {
        throw new RuntimeException("cant call handler " + handler, iae);
      }
      List<GuiScreenAction> actions = GuiResponseJs.retrieveGuiResponseJs().getActions();
      if (actions != null) {
        for (GuiScreenAction action : actions) {
          if (action.getInnerHtmlJqueryHandle() != null) {
            call.handles.add(action.getInnerHtmlJqueryHandle());
          }
          if (action.getMessageType() != null) {
            call.messageTypes.add(action.getMessageType());
          }
        }
      }
      return call;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * result of one handler call
   */
  private static class Call {

    /** handler name */
    private String handler;

    /** exception the handler threw, or null */
    private RuntimeException exception;

    /** inner html handles of the screen actions */
    private List<String> handles = new ArrayList<String>();

    /** message types of the screen actions */
    private List<String> messageTypes = new ArrayList<String>();

    /**
     * @return if any screen action is an error message
     */
    private boolean hasErrorMessage() {
      return this.messageTypes.contains("error");
    }

    /**
     * @return the actions, for assertion messages
     */
    private String describe() {
      return "handles " + this.handles + ", message types " + this.messageTypes;
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

    /** request uri */
    private String requestUri;

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
          return UiRequest.this.requestUri;
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
