package edu.internet2.middleware.grouper.grouperUi.beans.api;

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
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.cfg.text.GrouperTextContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.TextContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.rules.RuleCheck;
import edu.internet2.middleware.grouper.rules.RuleDefinition;
import edu.internet2.middleware.grouper.rules.RuleIfCondition;
import edu.internet2.middleware.grouper.rules.RuleThen;
import edu.internet2.middleware.grouper.rules.RuleThenEnum;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue rulesEmailRecipientStoredXss: the Rules screens build their result, check and
 * condition text from externalized text templates that interpolated stored rule arguments (e.g.
 * the send-email recipient, thenEnumArg0) without HTML escaping, so a delegated rules author
 * could store markup that runs in the viewer's session.  After the fix the nine templates wrap the
 * stored arguments in grouperUtil.xmlEscape(...).</p>
 *
 * <p>The templates are evaluated the way the screens do it: the GuiRuleDefinition is set as
 * rulesContainer.currentGuiRuleDefinition and the text is read through TextContainer, inside an
 * in-memory UI request (GrouperUiFilter thread locals).  The rule definition is built in memory,
 * no rule is saved or fired.</p>
 */
public class GuiRuleDefinitionEscapeTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GuiRuleDefinitionEscapeTest("testSendEmailRecipientEscaped"));
  }

  /**
   *
   */
  public GuiRuleDefinitionEscapeTest() {
    super();
  }

  /**
   * @param name
   */
  public GuiRuleDefinitionEscapeTest(String name) {
    super(name);
  }

  /** markup stored in a rule argument */
  private static final String PAYLOAD = "<b id=\"RULE_XSS_PROOF\">RULE_XSS_PROOF</b>";

  /** the payload escaped */
  private static final String ESCAPED = "&lt;b id=";

  /**
   * remove thread locals
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    super.tearDown();
  }

  /**
   * the reported case: send email rule result, through GuiRuleDefinition.getResult()
   */
  public void testSendEmailRecipientEscaped() {
    RuleDefinition ruleDefinition = new RuleDefinition();
    RuleThen ruleThen = new RuleThen();
    ruleThen.setThenEnum("sendEmail");
    ruleThen.setThenEnumArg0(PAYLOAD);
    ruleThen.setThenEnumArg1("subject");
    ruleThen.setThenEnumArg2("body");
    ruleDefinition.setThen(ruleThen);

    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition();
      guiRuleDefinition.setRuleDefinition(ruleDefinition);
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      // the text engine reads grouperRequestContainer from its own thread local, which GrouperUiFilter
      // sets on every real request; without it the template paths resolve to nothing (blank text)
      GrouperTextContainer.grouperRequestContainerThreadLocalAssign(grouperRequestContainer);
      grouperRequestContainer.getRulesContainer().setCurrentGuiRuleDefinition(guiRuleDefinition);
      assertEscaped(guiRuleDefinition.getResult());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  /**
   * every template that interpolates a stored scalar argument escapes it
   */
  public void testAllArgumentTemplatesEscaped() {
    RuleDefinition ruleDefinition = new RuleDefinition();
    RuleThen ruleThen = new RuleThen();
    ruleThen.setThenEnumArg0(PAYLOAD);
    ruleDefinition.setThen(ruleThen);
    RuleCheck ruleCheck = new RuleCheck();
    ruleCheck.setCheckArg0(PAYLOAD);
    ruleCheck.setCheckArg1(PAYLOAD);
    ruleDefinition.setCheck(ruleCheck);
    RuleIfCondition ruleIfCondition = new RuleIfCondition();
    ruleIfCondition.setIfConditionEnumArg0(PAYLOAD);
    ruleDefinition.setIfCondition(ruleIfCondition);

    String[] keys = new String[] {
        "rulesTableResultHumanFriendlyValue_sendEmail",
        "rulesTableResultHumanFriendlyValue_assignDisabledDaysToOwnerPermissionDefAssignments",
        "rulesTableResultHumanFriendlyValue_assignMembershipDisabledDaysForOwnerGroupId",
        "rulesTableResultHumanFriendlyValue_assignMembershipDisabledDaysForCurrentGroupId",
        "rulesTableCheckHumanFriendlyValue_membershipDisabledDate",
        "rulesTableCheckHumanFriendlyValue_permissionDisabledDate",
        "rulesTableConditionHumanFriendlyValue_groupHasTooFewMembers",
        "rulesTableConditionHumanFriendlyValue_groupHasTooManyMembers",
        "rulesTableConditionHumanFriendlyValue_nameMatchesSqlLikeString"};

    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition();
      guiRuleDefinition.setRuleDefinition(ruleDefinition);
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      // the text engine reads grouperRequestContainer from its own thread local, which GrouperUiFilter
      // sets on every real request; without it the template paths resolve to nothing (blank text)
      GrouperTextContainer.grouperRequestContainerThreadLocalAssign(grouperRequestContainer);
      grouperRequestContainer.getRulesContainer().setCurrentGuiRuleDefinition(guiRuleDefinition);
      for (String key : keys) {
        // a result key whose rule action this release does not have is not used here (e.g.
        // assignMembershipDisabledDaysForCurrentGroupId is new in 7.4/7.5), so there is nothing to escape
        String resultPrefix = "rulesTableResultHumanFriendlyValue_";
        if (key.startsWith(resultPrefix)) {
          try {
            RuleThenEnum.valueOf(key.substring(resultPrefix.length()));
          } catch (IllegalArgumentException iae) {
            continue;
          }
        }
        String text = TextContainer.retrieveFromRequest().getText().get(key);
        assertNotNull(key, text);
        assertFalse(key + ": " + text, text.contains("<b id="));
        assertTrue(key + ": " + text, text.contains(ESCAPED));
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  /**
   * a null argument renders as empty, not "null"
   */
  public void testNullArgumentIsEmpty() {
    RuleDefinition ruleDefinition = new RuleDefinition();
    RuleThen ruleThen = new RuleThen();
    ruleThen.setThenEnum("sendEmail");
    ruleDefinition.setThen(ruleThen);

    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      GuiRuleDefinition guiRuleDefinition = new GuiRuleDefinition();
      guiRuleDefinition.setRuleDefinition(ruleDefinition);
      GrouperRequestContainer grouperRequestContainer = GrouperRequestContainer.retrieveFromRequestOrCreate();
      // the text engine reads grouperRequestContainer from its own thread local, which GrouperUiFilter
      // sets on every real request; without it the template paths resolve to nothing (blank text)
      GrouperTextContainer.grouperRequestContainerThreadLocalAssign(grouperRequestContainer);
      grouperRequestContainer.getRulesContainer().setCurrentGuiRuleDefinition(guiRuleDefinition);
      String text = guiRuleDefinition.getResult();
      assertFalse(text, text.contains("null"));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  /**
   * @param text rendered text
   */
  private static void assertEscaped(String text) {
    assertNotNull(text);
    assertFalse("raw markup rendered: " + text, text.contains("<b id="));
    assertTrue("escaped markup expected: " + text, text.contains(ESCAPED));
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
      GrouperTextContainer.grouperRequestContainerThreadLocalClear();
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
