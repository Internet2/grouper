package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.attributeUpdate.AttributeUpdateRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue attributeDefActionEditIdorDisclosure: UiV2AttributeDefAction.editAttributeDefAction
 * checked ATTR_ADMIN on the attributeDefId of the request, then loaded attributeDefActionId with
 * the unsecured DAO and rendered that action, all actions of its own attribute def and its
 * implication hierarchy.  So an admin of any attribute def could read the actions of a private
 * attribute def by passing one of its action ids.  After the fix the action must belong to the
 * authorized attribute def; a foreign id and an unknown id get the same "not found" message.</p>
 *
 * <p>Handlers are called directly with an in-memory request, session, response and servlet set
 * into GrouperUiFilter's thread locals (the same harness as the attestation issue tests).  Only
 * APIs present from 2.6.19 through 7.x.</p>
 *
 * <p>Setup: SUBJ0 is ATTR_ADMIN of test:attr153:userDef (actions assign and extra153); SUBJ0 has no
 * privilege on test:attr153private:privateDef (actions assign, secretRead153, secretWrite153,
 * secretAdmin153; secretAdmin153 implies secretWrite153 implies secretRead153).</p>
 */
public class UiV2AttributeDefActionBindingTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2AttributeDefActionBindingTest("testForeignActionIdRefused"));
  }

  /**
   *
   */
  public UiV2AttributeDefActionBindingTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AttributeDefActionBindingTest(String name) {
    super(name);
  }

  /** attribute def SUBJ0 administers */
  private AttributeDef userDef;

  /** extra action of userDef */
  private AttributeAssignAction userExtraAction;

  /** action of the private attribute def */
  private AttributeAssignAction secretWriteAction;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * attribute defs and actions, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Stem userStem = new StemSave(grouperSession).assignName("test:attr153").assignCreateParentStemsIfNotExist(true).save();
      this.userDef = userStem.addChildAttributeDef("userDef", AttributeDefType.attr);
      this.userExtraAction = this.userDef.getAttributeDefActionDelegate().addAction("extra153");
      this.userDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);

      Stem privateStem = new StemSave(grouperSession).assignName("test:attr153private").assignCreateParentStemsIfNotExist(true).save();
      AttributeDef privateDef = privateStem.addChildAttributeDef("privateDef", AttributeDefType.perm);
      AttributeAssignAction secretRead = privateDef.getAttributeDefActionDelegate().addAction("secretRead153");
      this.secretWriteAction = privateDef.getAttributeDefActionDelegate().addAction("secretWrite153");
      AttributeAssignAction secretAdmin = privateDef.getAttributeDefActionDelegate().addAction("secretAdmin153");
      secretAdmin.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(this.secretWriteAction);
      this.secretWriteAction.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(secretRead);
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
  // tests
  // ---------------------------------------------------------------------------------------------

  /**
   * SUBJ0 passes its own attribute def with an action id of the private attribute def: refused
   * with the not found message, and nothing of the private attribute def is put on the screen.
   * Before the fix the edit screen was built for secretWrite153 with the private def's actions.
   */
  public void testForeignActionIdRefused() {
    Result result = edit(this.userDef.getId(), this.secretWriteAction.getId());
    assertNull("no exception expected: " + result.exception, result.exception);
    assertNull("foreign action must not be put on the screen", result.attributeAssignAction);
    assertFalse("foreign action name must not be put on the screen: " + result.action,
        result.action != null && result.action.indexOf("secret") >= 0);
    assertFalse("foreign actions must not be put on the screen: " + result.actions,
        result.actions != null && result.actions.toString().indexOf("secret") >= 0);
    assertNotNull("an error message is expected", result.errorMessage);
  }

  /**
   * an id that does not exist gets the same message as a foreign id (no existence oracle).
   * Before the fix the DAO threw a not found exception instead.
   */
  public void testUnknownActionIdSameMessage() {
    Result foreign = edit(this.userDef.getId(), this.secretWriteAction.getId());
    Result unknown = edit(this.userDef.getId(), GrouperUtil.uniqueId());
    assertNull("no exception expected: " + unknown.exception, unknown.exception);
    assertNull(unknown.attributeAssignAction);
    assertNotNull("an error message is expected", unknown.errorMessage);
    assertEquals("foreign and unknown ids must get the same message", foreign.errorMessage, unknown.errorMessage);
  }

  /**
   * control: an action of SUBJ0's own attribute def opens the edit screen
   */
  public void testOwnActionShowsEditScreen() {
    Result result = edit(this.userDef.getId(), this.userExtraAction.getId());
    assertNull("no exception expected: " + result.exception, result.exception);
    assertNull("no error expected: " + result.errorMessage, result.errorMessage);
    assertNotNull(result.attributeAssignAction);
    assertEquals(this.userExtraAction.getId(), result.attributeAssignAction.getId());
    assertEquals("extra153", result.action);
    assertTrue("own actions expected: " + result.actions, result.actions != null && result.actions.contains("extra153")
        && result.actions.contains("assign"));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * what the handler left in the request
   */
  private static class Result {

    /** exception thrown by the handler, or null */
    private RuntimeException exception;

    /** action on the container */
    private AttributeAssignAction attributeAssignAction;

    /** action name on the container */
    private String action;

    /** allowed action names on the container */
    private List<String> actions;

    /** first error message of the response, or null */
    private String errorMessage;
  }

  /**
   * call editAttributeDefAction as SUBJ0
   * @param attributeDefId attribute def id param
   * @param attributeDefActionId action id param
   * @return result
   */
  private Result edit(String attributeDefId, String attributeDefActionId) {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.put("attributeDefId", attributeDefId);
    uiRequest.params.put("attributeDefActionId", attributeDefActionId);
    Result result = new Result();
    try {
      try {
        new UiV2AttributeDefAction().editAttributeDefAction(uiRequest.request, uiRequest.response);
      } catch (RuntimeException re) {
        result.exception = re;
      }
      AttributeUpdateRequestContainer container = AttributeUpdateRequestContainer.retrieveFromRequestOrCreate();
      result.attributeAssignAction = container.getAttributeAssignAction();
      result.action = container.getAction();
      result.actions = container.getActions();
      for (GuiScreenAction guiScreenAction : GrouperUtil.nonNull(GuiResponseJs.retrieveGuiResponseJs().getActions())) {
        if ("error".equals(guiScreenAction.getMessageType()) && result.errorMessage == null) {
          result.errorMessage = guiScreenAction.getMessage();
        }
      }
      return result;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * In-memory UI request for calling a UiV2 handler directly: request, session, response and a
   * servlet whose JSP dispatcher renders nothing, set into GrouperUiFilter's thread locals, with
   * the logged in subject stored in the SessionContainer.
   */
  private static class UiRequest {

    /** request params */
    private Map<String, String> params = new HashMap<String, String>();

    /** request attributes */
    private Map<String, Object> attributes = new HashMap<String, Object>();

    /** session attributes */
    private Map<String, Object> sessionAttributes = new HashMap<String, Object>();

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
          return "/grouper/grouperUi/app/UiV2Main.index";
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

    /** response, output is discarded */
    private HttpServletResponse response = proxy(HttpServletResponse.class, new InvocationHandler() {
      @Override
      public Object invoke(Object p, Method method, Object[] args) {
        String name = method.getName();
        if ("getWriter".equals(name)) {
          return new PrintWriter(new StringWriter());
        }
        if ("getOutputStream".equals(name)) {
          return new ServletOutputStream() {
            @Override
            public void write(int b) {
            }
            @Override
            public boolean isReady() {
              return true;
            }
            @Override
            public void setWriteListener(WriteListener writeListener) {
            }
          };
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
