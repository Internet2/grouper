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

import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue daemonPerformanceChartNoAdminCheck: UiV2Admin.viewPerformanceChart (the daemon
 * performance chart page, 6.4.0 / 7.3.2 and later), viewLogs and viewLogsFilter (the daemon logs
 * screen, every release) rendered the job's page and the daemon "more actions" menu for any logged
 * in user, without first making the daemonJobsAllowed() check that viewPerformanceChartResults and
 * the job list make.  A plain user could probe which daemon job names exist and see a job's state
 * (enabled, running, failsafe approval, report owner ids).</p>
 *
 * <p>Calls each handler with an in-memory request, session and response set into
 * GrouperUiFilter's thread locals (the harness of the hidden folder test), like the UI servlet
 * does.  The job name does not exist.  Before the fix a plain user reached the job lookup (an
 * "Invalid job name" exception where the release validates the name) or got the more actions menu;
 * after the fix a plain user gets the not-allowed answer before any job lookup, whatever the name.
 * Root still passes the check (positive controls).</p>
 *
 * <p>The handlers are called by reflection so the same test compiles on every release:
 * viewPerformanceChart does not exist before 6.4.0 (and 7.0.0 to 7.3.1); its tests pass trivially
 * there.</p>
 *
 * <p>Config: uiV2.admin.daemonJobs.show=true and uiV2.admin.daemonJobs.must.be.in.group blank (the
 * defaults), so only wheel or root may see daemon jobs.</p>
 */
public class UiV2AdminPerformanceChartAuthorizationTest extends GrouperTest {

  /** a daemon job name that is not scheduled */
  private static final String JOB_NAME = "OTHER_JOB_securityTestNoSuchJob180";

  /** the inner html handle of the more actions menu, only filled for an allowed user */
  private static final String MORE_ACTIONS_HANDLE = "#adminDaemonJobsMoreActionsId";

  /** handler: daemon performance chart page (6.4.0 / 7.3.2 and later) */
  private static final String VIEW_PERFORMANCE_CHART = "viewPerformanceChart";

  /** handler: daemon logs screen */
  private static final String VIEW_LOGS = "viewLogs";

  /** handler: daemon logs screen filter submit */
  private static final String VIEW_LOGS_FILTER = "viewLogsFilter";

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2AdminPerformanceChartAuthorizationTest("testPlainUserCannotOpenLogs"));
  }

  /**
   *
   */
  public UiV2AdminPerformanceChartAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AdminPerformanceChartAuthorizationTest(String name) {
    super(name);
  }

  /**
   * default daemon jobs config: shown, no extra group (wheel or root only)
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put("uiV2.admin.daemonJobs.show", "true");
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put("uiV2.admin.daemonJobs.must.be.in.group", "");
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
   * a plain user gets the not-allowed error and the index page, no job lookup, no chart page and
   * no more actions menu
   */
  public void testPlainUserCannotOpenPerformanceChart() {
    if (!handlerExists(VIEW_PERFORMANCE_CHART)) {
      // this release has no performance chart page, nothing to protect
      return;
    }
    assertRefused(callHandler(VIEW_PERFORMANCE_CHART, SubjectTestHelper.SUBJ0, JOB_NAME));
  }

  /**
   * a plain user gets the not-allowed error from the daemon logs screen, no job lookup and no more
   * actions menu
   */
  public void testPlainUserCannotOpenLogs() {
    assertRefused(callHandler(VIEW_LOGS, SubjectTestHelper.SUBJ0, JOB_NAME));
  }

  /**
   * a plain user gets the not-allowed error from the daemon logs filter submit, no more actions
   * menu (before the fix the menu was rendered before the logs helper checked)
   */
  public void testPlainUserCannotFilterLogs() {
    assertRefused(callHandler(VIEW_LOGS_FILTER, SubjectTestHelper.SUBJ0, JOB_NAME));
  }

  /**
   * positive control: root passes the check and reaches the job lookup, so an unknown job name is
   * rejected there (this is the answer that used to tell a plain user the name does not exist)
   */
  public void testRootReachesJobLookup() {
    if (!handlerExists(VIEW_PERFORMANCE_CHART)) {
      return;
    }
    Call call = callHandler(VIEW_PERFORMANCE_CHART, SubjectFinder.findRootSubject(), JOB_NAME);
    assertNotNull("root reaches the job lookup and an unknown job is rejected: " + call.describe(), call.exception);
    assertTrue("root gets the invalid job name error, got: " + call.exception,
        String.valueOf(call.exception.getMessage()).contains("Invalid job name"));
  }

  /**
   * positive control: root passes the check on the logs screen and its filter (it reaches the job
   * lookup or gets the more actions menu, depending on the release)
   */
  public void testRootPassesLogsCheck() {
    for (String handler : new String[] {VIEW_LOGS, VIEW_LOGS_FILTER}) {
      Call call = callHandler(handler, SubjectFinder.findRootSubject(), JOB_NAME);
      assertTrue(handler + ": root is not refused: " + call.describe() + ", exception " + call.exception,
          call.exception != null || call.handles.contains(MORE_ACTIONS_HANDLE));
    }
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * a refused call: no exception (no job lookup), the not-allowed error, no more actions menu
   * @param call result
   */
  private static void assertRefused(Call call) {
    // before the fix: RuntimeException "Invalid job name ..." from the scheduler lookup, or the menu
    assertNull(call.handler + ": plain user must be refused before the job lookup, got: " + call.exception, call.exception);
    assertTrue(call.handler + ": plain user gets an error message: " + call.describe(), call.hasErrorMessage());
    assertFalse(call.handler + ": plain user gets no more actions menu: " + call.describe(),
        call.handles.contains(MORE_ACTIONS_HANDLE));
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
   * @param handler UiV2Admin method name
   * @return true if this release has that public handler
   */
  private static boolean handlerExists(String handler) {
    try {
      UiV2Admin.class.getMethod(handler, HttpServletRequest.class, HttpServletResponse.class);
      return true;
    } catch (NoSuchMethodException nsme) {
      return false;
    }
  }

  /**
   * call a UiV2Admin handler as this subject, the way the UI servlet does (the handler starts its
   * own grouper session); by reflection, since not every release has every handler
   * @param handler UiV2Admin method name
   * @param loggedIn caller
   * @param jobName jobName param
   * @return the result
   */
  private static Call callHandler(String handler, Subject loggedIn, String jobName) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.requestUri = "/grouper/grouperUi/app/UiV2Admin." + handler;
    uiRequest.params.put("jobName", jobName);
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
