package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory H6: UI handlers sent back GuiScreenAction.newScript("guiV2link('...subjectId="
 * + subject.getId() + "')"), and the browser evals that script.  A subject id from a subject source
 * that contains a single quote ended the JavaScript string and ran as code in the session of the
 * admin who saved.  The fix is GuiV2LinkScript, which URL-encodes each value and escapes the URL
 * for a single-quoted JavaScript string; it is used by UiV2SubjectPermission,
 * UiV2SubjectAttributeAssignment and UiV2Provisioning.</p>
 *
 * <p>The handler tests use a jdbc subject (example subject table) whose id holds a quote, a double
 * quote, a tag and a script, and call the handlers directly with an in-memory request, session,
 * response and servlet (dynamic proxies set into GrouperUiFilter's thread locals), as root, so no
 * servlet container is needed.  The returned script must be only URL-safe characters inside
 * guiV2link('...'), and its subjectId must decode to the exact subject id.</p>
 *
 * <p>Before the fix the test does not compile (GuiV2LinkScript does not exist); with the class but
 * without the call sites the handler tests fail (the raw quote is in the script).</p>
 */
public class GuiV2LinkScriptTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(GuiV2LinkScriptTest.class);
  }

  /**
   *
   */
  public GuiV2LinkScriptTest() {
    super();
  }

  /**
   * @param name
   */
  public GuiV2LinkScriptTest(String name) {
    super(name);
  }

  /** subject id that breaks out of guiV2link('...') if not encoded */
  private static final String HOSTILE_SUBJECT_ID = "secPatch156');alert(156);//\"><img src=x onerror=alert(1)>";

  /** metadata item for the provisioning test */
  private static final String MD_NAME = "md_secPatch156";

  /** provisioner config id */
  private String targetName;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * insert the hostile jdbc subject
   */
  @Override
  protected void setUp() {
    super.setUp();
    deleteHostileSubject();
    HibernateSession.bySqlStatic().executeSql("insert into subject (subjectid, subjecttypeid, name) values (?, 'person', ?)",
        listOf(HOSTILE_SUBJECT_ID, "secPatch156 subject"));
  }

  /**
   * remove the subject and the thread locals (GrouperTest clears the config override maps and
   * the registry)
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    deleteHostileSubject();
    super.tearDown();
  }

  /**
   * remove the example subject row
   */
  private static void deleteHostileSubject() {
    HibernateSession.bySqlStatic().executeSql("delete from subjectattribute where subjectid = ?", listOf(HOSTILE_SUBJECT_ID));
    HibernateSession.bySqlStatic().executeSql("delete from subject where subjectid = ?", listOf(HOSTILE_SUBJECT_ID));
  }

  // ---------------------------------------------------------------------------------------------
  // helper
  // ---------------------------------------------------------------------------------------------

  /**
   * values are URL-encoded and decode back exactly; names must be plain
   */
  public void testHelper() {
    String value = HOSTILE_SUBJECT_ID + " \\ \n\r  &x=1 %27";
    String script = GuiV2LinkScript.guiV2link("UiV2Provisioning.viewProvisioningOnSubject", "subjectId", value, "sourceId", null);
    Map<String, String> params = assertSafeLink(script, "UiV2Provisioning.viewProvisioningOnSubject");
    assertEquals(value, params.get("subjectId"));
    assertEquals("", params.get("sourceId"));
    assertEquals("guiV2link('operation=UiV2Group.viewGroup&groupId=abc123')",
        GuiV2LinkScript.guiV2link("UiV2Group.viewGroup", "groupId", "abc123"));

    // a quote or other character outside the URL-safe set is never left as is
    assertEquals("a\\u0027b\\u005c\\u003c", GuiV2LinkScript.escapeJavascriptSingleQuoted("a'b\\<"));

    for (String[] bad : new String[][] {{"UiV2X.y');alert(1);//"}, {"UiV2X.y", "subjectId"}, {"UiV2X.y", "a'b", "c"}, {null}}) {
      try {
        String[] rest = new String[bad.length - 1];
        System.arraycopy(bad, 1, rest, 0, rest.length);
        GuiV2LinkScript.guiV2link(bad[0], rest);
        fail("should refuse: " + java.util.Arrays.asList(bad));
      } catch (IllegalArgumentException iae) {
        // ok
      }
    }
  }

  // ---------------------------------------------------------------------------------------------
  // handlers
  // ---------------------------------------------------------------------------------------------

  /**
   * UiV2SubjectAttributeAssignment.attributeAssignAddValueSubmit: the link back to the subject's
   * attribute assignments carries the encoded subject id and source id
   */
  public void testAttributeAssignAddValueSubmit() {
    AttributeAssign attributeAssign = null;
    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      AttributeDef attributeDef = new AttributeDefSave(rootSession).assignName("test:secPatch156:attrDef")
          .assignCreateParentStemsIfNotExist(true).assignAttributeDefType(AttributeDefType.attr)
          .assignToMember(true).assignValueType(AttributeDefValueType.string).assignMultiValued(true).save();
      AttributeDefName attributeDefName = new AttributeDefNameSave(rootSession, attributeDef)
          .assignName("test:secPatch156:attrName").assignCreateParentStemsIfNotExist(true).save();
      Member member = MemberFinder.findBySubject(rootSession, hostileSubject(), true);
      attributeAssign = member.getAttributeDelegate().assignAttribute(attributeDefName).getAttributeAssign();
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    UiRequest uiRequest = new UiRequest(SubjectFinder.findRootSubject());
    try {
      uiRequest.params.put("subjectId", HOSTILE_SUBJECT_ID);
      uiRequest.params.put("sourceId", "jdbc");
      uiRequest.params.put("attributeAssignId", attributeAssign.getId());
      uiRequest.params.put("valueToAdd", "secPatch156value");
      new UiV2SubjectAttributeAssignment().attributeAssignAddValueSubmit(uiRequest.request, uiRequest.response);
      Map<String, String> params = assertSafeLink(linkScript(), "UiV2SubjectAttributeAssignment.viewAttributeAssignments");
      assertEquals(HOSTILE_SUBJECT_ID, params.get("subjectId"));
      assertEquals("jdbc", params.get("sourceId"));
    } finally {
      UiRequest.clear();
    }
    System.out.println("^secPatch156 attributeAssignAddValueSubmit ok");
  }

  /**
   * UiV2Provisioning.editProvisioningOnSubjectSave: the link back to the subject's provisioning
   * screen carries the encoded subject id (and now the source id)
   */
  public void testEditProvisioningOnSubjectSave() {
    configureProvisioner();

    GrouperSession rootSession = GrouperSession.startRootSession();
    try {
      MemberFinder.findBySubject(rootSession, hostileSubject(), true);
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }

    UiRequest uiRequest = new UiRequest(SubjectFinder.findRootSubject());
    try {
      uiRequest.params.put("subjectId", HOSTILE_SUBJECT_ID);
      uiRequest.params.put("sourceId", "jdbc");
      uiRequest.params.put("provisioningTargetName", this.targetName);
      uiRequest.params.put(MD_NAME, "secPatch156value");
      new UiV2Provisioning().editProvisioningOnSubjectSave(uiRequest.request, uiRequest.response);
      Map<String, String> params = assertSafeLink(linkScript(), "UiV2Provisioning.viewProvisioningOnSubject");
      assertEquals(HOSTILE_SUBJECT_ID, params.get("subjectId"));
      assertEquals("jdbc", params.get("sourceId"));
    } finally {
      UiRequest.clear();
    }
    System.out.println("^secPatch156 editProvisioningOnSubjectSave ok");
  }

  /**
   * offline LDAP provisioner with one subject metadata item (config style of advisory M1's test)
   */
  private void configureProvisioner() {
    this.targetName = "junitProv156_" + (System.currentTimeMillis() % 1000000);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");
    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put("ldap.personLdap.url", "ldap://localhost:389");
    loader.put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    loader.put("ldap.personLdap.pass", "secret");
    String p = "provisioner." + this.targetName + ".";
    loader.put(p + "class", LdapSync.class.getName());
    loader.put(p + "ldapExternalSystemConfigId", "personLdap");
    loader.put(p + "configureMetadata", "true");
    loader.put(p + "numberOfMetadata", "1");
    loader.put(p + "metadata.0.name", MD_NAME);
    loader.put(p + "metadata.0.formElementType", "text");
    loader.put(p + "metadata.0.valueType", "string");
    loader.put(p + "metadata.0.showForMember", "true");
    loader.put(p + "metadata.0.canChange", "true");
    loader.put(p + "metadata.0.canUpdate", "true");
    // targets are cached
    GrouperProvisioningSettings.getTargets(false);
  }

  // ---------------------------------------------------------------------------------------------
  // assertions
  // ---------------------------------------------------------------------------------------------

  /**
   * @return the hostile jdbc subject
   */
  private static Subject hostileSubject() {
    return SubjectFinder.findByIdAndSource(HOSTILE_SUBJECT_ID, "jdbc", true);
  }

  /**
   * the guiV2link script the handler added to the response (read while the request is current)
   * @return the script
   */
  private static String linkScript() {
    List<String> scripts = new ArrayList<String>();
    List<String> messages = new ArrayList<String>();
    for (GuiScreenAction action : GuiResponseJs.retrieveGuiResponseJs().getActions()) {
      if (action.getScript() != null && action.getScript().contains("guiV2link")) {
        scripts.add(action.getScript());
      }
      if (action.getMessage() != null) {
        messages.add(action.getMessage());
      }
    }
    assertEquals("one guiV2link script expected, messages: " + messages, 1, scripts.size());
    return scripts.get(0);
  }

  /**
   * the script is guiV2link('...') with only URL-safe characters inside, for this operation
   * @param script script
   * @param operation expected operation
   * @return decoded params
   */
  private static Map<String, String> assertSafeLink(String script, String operation) {
    assertTrue(script, script.startsWith("guiV2link('") && script.endsWith("')"));
    String url = script.substring("guiV2link('".length(), script.length() - "')".length());
    assertTrue("only URL-safe characters inside the quotes: " + script, url.matches("[A-Za-z0-9_.*+%=&-]*"));
    Map<String, String> params = new HashMap<String, String>();
    for (String pair : url.split("&")) {
      int equalsIndex = pair.indexOf('=');
      try {
        params.put(pair.substring(0, equalsIndex), URLDecoder.decode(pair.substring(equalsIndex + 1), "UTF-8"));
      } catch (UnsupportedEncodingException uee) {
        throw new RuntimeException(uee);
      }
    }
    assertEquals(script, operation, params.get("operation"));
    return params;
  }

  /**
   * @param values values
   * @return list of the values
   */
  private static List<Object> listOf(Object... values) {
    List<Object> result = new ArrayList<Object>();
    Collections.addAll(result, values);
    return result;
  }

  // ---------------------------------------------------------------------------------------------
  // in-memory request (same shape as advisory M1's test)
  // ---------------------------------------------------------------------------------------------

  /**
   * In-memory UI request for calling a UiV2 handler directly: request, session, response and a
   * servlet whose JSP dispatcher renders nothing, set into GrouperUiFilter's thread locals, with
   * the logged in subject stored in the SessionContainer and a "nav" bundle that answers every key
   * with the key (TagUtils.navResourceString).
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
      // TagUtils.navResourceString reads the "nav" LocalizationContext from the session
      this.sessionAttributes.put("nav", new LocalizationContext(new ResourceBundle() {
        @Override
        protected Object handleGetObject(String key) {
          return key;
        }
        @Override
        public Enumeration<String> getKeys() {
          return Collections.enumeration(new ArrayList<String>());
        }
      }));
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
      return (T)Proxy.newProxyInstance(GuiV2LinkScriptTest.class.getClassLoader(),
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
