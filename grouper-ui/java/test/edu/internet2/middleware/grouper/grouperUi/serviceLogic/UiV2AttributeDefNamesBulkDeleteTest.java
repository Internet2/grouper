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
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.ui.util.GrouperUiConfig;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue attributeDefNamesBulkDeleteBypass: UiV2AttributeDefName.deleteAttributeDefNames
 * (the "Remove selected attribute names" button of an attribute def) did not check
 * uiV2.attributeDefName.preventDeleteInUi, which the single delete honors, and deleted every
 * submitted attribute def name id even when the name belongs to another attribute def than the
 * one on the screen.  After the fix the bulk remove is refused when the property is true, and only
 * names of the screen's attribute def are deleted.</p>
 *
 * <p>Handlers are called directly with an in-memory request, session, response and servlet set
 * into GrouperUiFilter's thread locals (the same harness as the attestation issue tests).  The UI
 * property is set in the test JVM's override map.  Only APIs present from 2.6.19 through 7.x.</p>
 *
 * <p>Setup: SUBJ0 is ATTR_ADMIN of test:attr154:defA (names nameA1, nameA2) and of
 * test:attr154:defB (name nameB1).</p>
 */
public class UiV2AttributeDefNamesBulkDeleteTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2AttributeDefNamesBulkDeleteTest("testPreventDeleteInUiBlocksBulkRemove"));
  }

  /**
   *
   */
  public UiV2AttributeDefNamesBulkDeleteTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AttributeDefNamesBulkDeleteTest(String name) {
    super(name);
  }

  /** UI property */
  private static final String PREVENT_DELETE = "uiV2.attributeDefName.preventDeleteInUi";

  /** attribute def on the screen */
  private AttributeDef defA;

  /** name of defA */
  private AttributeDefName nameA1;

  /** name of defA */
  private AttributeDefName nameA2;

  /** name of the other attribute def */
  private AttributeDefName nameB1;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * attribute defs and names, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Stem stem = new StemSave(grouperSession).assignName("test:attr154").assignCreateParentStemsIfNotExist(true).save();
      this.defA = stem.addChildAttributeDef("defA", AttributeDefType.attr);
      AttributeDef defB = stem.addChildAttributeDef("defB", AttributeDefType.attr);
      this.defA.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
      defB.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_ADMIN, false);
      this.nameA1 = stem.addChildAttributeDefName(this.defA, "nameA1", "nameA1");
      this.nameA2 = stem.addChildAttributeDefName(this.defA, "nameA2", "nameA2");
      this.nameB1 = stem.addChildAttributeDefName(defB, "nameB1", "nameB1");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put(PREVENT_DELETE, "false");
  }

  /**
   * remove config overrides and thread locals
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().remove(PREVENT_DELETE);
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------------

  /**
   * preventDeleteInUi=true: the bulk remove is refused with an error and the name stays.  Before
   * the fix the name was deleted.
   */
  public void testPreventDeleteInUiBlocksBulkRemove() {
    GrouperUiConfig.retrieveConfig().propertiesOverrideMap().put(PREVENT_DELETE, "true");
    String error = bulkDelete(this.defA.getId(), this.nameA1.getId());
    assertTrue("nameA1 must not be deleted when UI deletion is not allowed", exists(this.nameA1));
    assertNotNull("an error message is expected", error);
  }

  /**
   * a name of another attribute def (SUBJ0 is admin there too) submitted on defA's screen: not
   * deleted.  Before the fix it was deleted.
   */
  public void testNameOfOtherAttributeDefNotDeleted() {
    String error = bulkDelete(this.defA.getId(), this.nameB1.getId());
    assertTrue("nameB1 is not a name of defA and must not be deleted", exists(this.nameB1));
    assertNotNull("the failure count message is expected", error);
  }

  /**
   * control: names of defA submitted on defA's screen are deleted, a foreign one in the same
   * request is not
   */
  public void testOwnNamesDeleted() {
    bulkDelete(this.defA.getId(), this.nameA1.getId(), this.nameA2.getId(), this.nameB1.getId());
    assertFalse("nameA1 should be deleted", exists(this.nameA1));
    assertFalse("nameA2 should be deleted", exists(this.nameA2));
    assertTrue("nameB1 must not be deleted", exists(this.nameB1));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * call deleteAttributeDefNames as SUBJ0
   * @param attributeDefId attribute def on the screen
   * @param attributeDefNameIds checked names
   * @return the first error message of the response, or null
   */
  private String bulkDelete(String attributeDefId, String... attributeDefNameIds) {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.put("attributeDefId", attributeDefId);
    for (int i = 0; i < attributeDefNameIds.length; i++) {
      uiRequest.params.put("attributeDefName_" + i + "[]", attributeDefNameIds[i]);
    }
    try {
      new UiV2AttributeDefName().deleteAttributeDefNames(uiRequest.request, uiRequest.response);
      for (GuiScreenAction guiScreenAction : GrouperUtil.nonNull(GuiResponseJs.retrieveGuiResponseJs().getActions())) {
        if ("error".equals(guiScreenAction.getMessageType())) {
          return guiScreenAction.getMessage();
        }
      }
      return null;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * @param attributeDefName name
   * @return true if the name's row still exists (HQL count, so no finder cache is involved)
   */
  private static boolean exists(AttributeDefName attributeDefName) {
    Long count = HibernateSession.byHqlStatic().createQuery(
        "select count(*) from AttributeDefName where id = :theId")
        .setString("theId", attributeDefName.getId()).uniqueResult(Long.class);
    return count != null && count.longValue() > 0;
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
