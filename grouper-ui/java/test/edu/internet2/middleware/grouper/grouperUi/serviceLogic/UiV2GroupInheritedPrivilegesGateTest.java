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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
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
 * <p>SECURITY: UiV2Group.inheritedPrivilegesAssignedToThisGroupFromFolders (the group's "privileges inherited
 * from folders" screen) checks RulesContainer.isCanReadPrivilegeInheritance, like
 * UiV2Subject.thisSubjectsPrivilegesInheritedFromFolders and the menu link, so calling the operation directly
 * does not bypass uiV2.privilegeInheritanceReadRequireAdmin / uiV2.privilegeInheritanceReadRequireGroup.
 * Before the fix only the JSP hid the result; the handler ran the rule lookup.</p>
 *
 * <p>Calls the handler with an in-memory request, session and response set into GrouperUiFilter's thread
 * locals, like the UI servlet does.  test.subject.0 can VIEW the group.</p>
 */
public class UiV2GroupInheritedPrivilegesGateTest extends GrouperTest {

  /** group the screen is about */
  private static final String GROUP_NAME = "test:inheritedPrivilegesGate:group";

  /** where the screen goes */
  private static final String MAIN_CONTENT_HANDLE = "#grouperMainContentDivId";

  /** the handler */
  private static final String HANDLER = "inheritedPrivilegesAssignedToThisGroupFromFolders";

  /** config: only sysadmins may read privilege inheritance */
  private static final String REQUIRE_ADMIN = "uiV2.privilegeInheritanceReadRequireAdmin";

  /** id of the group */
  private String groupId;

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2GroupInheritedPrivilegesGateTest("testRequireAdminRefusesPlainUser"));
  }

  /**
   *
   */
  public UiV2GroupInheritedPrivilegesGateTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2GroupInheritedPrivilegesGateTest(String name) {
    super(name);
  }

  /**
   * a group test.subject.0 can view
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group group = new GroupSave(grouperSession).assignName(GROUP_NAME).assignCreateParentStemsIfNotExist(true).save();
      group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      this.groupId = group.getId();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * remove thread locals and config overrides
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().remove(REQUIRE_ADMIN);
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------------

  /**
   * privilegeInheritanceReadRequireAdmin=true: a plain user is refused by the handler, no screen
   */
  public void testRequireAdminRefusesPlainUser() {
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put(REQUIRE_ADMIN, "true");
    Call call = callHandler(HANDLER, SubjectTestHelper.SUBJ0, groupParams());
    assertNotNull("a plain user must be refused: " + call.describe(), call.exception);
    assertTrue("refused by the privilege inheritance gate, got: " + call.exception,
        String.valueOf(call.exception.getMessage()).contains("Not allowed to read privilege inheritance"));
    assertFalse("no screen: " + call.describe(), call.handles.contains(MAIN_CONTENT_HANDLE));
  }

  /**
   * privilegeInheritanceReadRequireAdmin=true: root still gets the screen (positive control)
   */
  public void testRequireAdminAllowsRoot() {
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put(REQUIRE_ADMIN, "true");
    Call call = callHandler(HANDLER, SubjectFinder.findRootSubject(), groupParams());
    assertNull("root: " + call.exception, call.exception);
    assertTrue("root gets the screen: " + call.describe(), call.handles.contains(MAIN_CONTENT_HANDLE));
  }

  /**
   * the default (no lockdown): a user who can view the group gets the screen
   */
  public void testDefaultAllowsViewer() {
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put(REQUIRE_ADMIN, "false");
    Call call = callHandler(HANDLER, SubjectTestHelper.SUBJ0, groupParams());
    assertNull("viewer: " + call.exception, call.exception);
    assertTrue("viewer gets the screen: " + call.describe(), call.handles.contains(MAIN_CONTENT_HANDLE));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @return the groupId param
   */
  private Map<String, String> groupParams() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("groupId", this.groupId);
    return params;
  }

  /**
   * call a UiV2Group handler as this subject, the way the UI servlet does (the handler starts its own
   * grouper session), and collect the screen actions
   * @param handler UiV2Group method name
   * @param loggedIn caller
   * @param params request params
   * @return the result
   */
  private static Call callHandler(String handler, Subject loggedIn, Map<String, String> params) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.requestUri = "/grouper/grouperUi/app/UiV2Group." + handler;
    uiRequest.params.putAll(params);
    Call call = new Call();
    call.handler = handler;
    try {
      try {
        Method method = UiV2Group.class.getMethod(handler, HttpServletRequest.class, HttpServletResponse.class);
        method.invoke(new UiV2Group(), uiRequest.request, uiRequest.response);
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
