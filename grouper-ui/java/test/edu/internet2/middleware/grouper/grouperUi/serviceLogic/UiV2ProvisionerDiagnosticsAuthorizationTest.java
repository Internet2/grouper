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

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M1: UiV2ProvisionerConfiguration.diagnostics and diagnosticsStatus only
 * checked "can view at least one provisioner" and then ran diagnostics for whatever
 * provisionerConfigId the request named, with the provisioner's target credentials.  The fix
 * requires wheel/root (isCanEditProvisionerConfiguration), the same rule as runFullSync and the
 * Diagnostics menu item, before the provisionerConfigId is read.</p>
 *
 * <p>Setup: two SQL provisioners.  The viewable one has groupAllowedToView = a group with SUBJ0
 * in it; the other one has neither groupAllowedToView nor groupAllowedToAssign.  SUBJ6 is in a
 * wheel group.  Nothing here connects to a target: the denied calls must stop at the gate, and
 * the allowed calls only open the diagnostics settings form (no provisionerInitted).</p>
 *
 * <p>Handlers are called directly with an in-memory request, session, response and servlet
 * (dynamic proxies set into GrouperUiFilter's thread locals), so no servlet container is needed.
 * Before the fix the SUBJ0 calls pass the gate (diagnostics initializes the provisioner and
 * diagnosticsStatus returns normally), so the denied tests fail.</p>
 */
public class UiV2ProvisionerDiagnosticsAuthorizationTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2ProvisionerDiagnosticsAuthorizationTest("testDiagnosticsDeniedForOtherProvisioner"));
  }

  /**
   *
   */
  public UiV2ProvisionerDiagnosticsAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ProvisionerDiagnosticsAuthorizationTest(String name) {
    super(name);
  }

  /** provisioner SUBJ0 can view (groupAllowedToView) */
  private String viewableProvisioner;

  /** provisioner with no view or assign group */
  private String otherProvisioner;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * config and groups, as root
   */
  @Override
  protected void setUp() {
    super.setUp();

    long suffix = System.currentTimeMillis() % 1000000;
    this.viewableProvisioner = "junitProv17view_" + suffix;
    this.otherProvisioner = "junitProv17other_" + suffix;

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group viewers = new GroupSave().assignName("test:provisioning:diagnosticsViewers").assignCreateParentStemsIfNotExist(true).save();
      viewers.addMember(SubjectTestHelper.SUBJ0, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    // read-only SQL provisioners against the grouper database, like the reproduction; the
    // tests never run diagnostics, they only reach the settings form
    for (String configId : new String[] {this.viewableProvisioner, this.otherProvisioner}) {
      Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
      String p = "provisioner." + configId + ".";
      loader.put(p + "class", "edu.internet2.middleware.grouper.app.sqlProvisioning.SqlProvisioner");
      loader.put(p + "dbExternalSystemConfigId", "grouper");
      loader.put(p + "operateOnGrouperGroups", "true");
      loader.put(p + "customizeGroupCrud", "true");
      loader.put(p + "selectGroups", "true");
      loader.put(p + "insertGroups", "false");
      loader.put(p + "updateGroups", "false");
      loader.put(p + "deleteGroups", "false");
      loader.put(p + "groupTableName", "grouper_groups");
      loader.put(p + "groupTableIdColumn", "id");
    }
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().put(
        "provisioner." + this.viewableProvisioner + ".groupAllowedToView", "test:provisioning:diagnosticsViewers");
  }

  /**
   * remove thread locals (GrouperTest clears the config override maps)
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
   * Precondition: SUBJ0 passes the old gate (can view some provisioner) but is not wheel/root,
   * so the denied tests below exercise the hole and not some other check.
   */
  public void testPrecondition() {
    new UiRequest(SubjectTestHelper.SUBJ0);
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertTrue("SUBJ0 can view some provisioner", GrouperRequestContainer.retrieveFromRequestOrCreate()
          .getProvisionerConfigurationContainer().isCanViewProvisionerConfiguration());
      assertFalse("SUBJ0 is not wheel/root", GrouperRequestContainer.retrieveFromRequestOrCreate()
          .getProvisionerConfigurationContainer().isCanEditProvisionerConfiguration());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  /**
   * A user who can view one provisioner cannot open diagnostics for a different one, and the
   * provisioner is not initialized.
   */
  public void testDiagnosticsDeniedForOtherProvisioner() {
    assertDenied(diagnostics(SubjectTestHelper.SUBJ0, this.otherProvisioner));
  }

  /**
   * Viewing a provisioner is not enough to run diagnostics on it (it acts on the target with
   * Grouper's credentials, like a full sync).
   */
  public void testDiagnosticsDeniedForViewableProvisioner() {
    assertDenied(diagnostics(SubjectTestHelper.SUBJ0, this.viewableProvisioner));
  }

  /**
   * diagnosticsStatus (polling and results) has the same rule.
   */
  public void testDiagnosticsStatusDenied() {
    assertDenied(diagnosticsStatus(SubjectTestHelper.SUBJ0, this.otherProvisioner));
    assertDenied(diagnosticsStatus(SubjectTestHelper.SUBJ0, this.viewableProvisioner));
  }

  /**
   * Control: root and a wheel member get past the gate (the settings form initializes the
   * diagnostics container), and diagnosticsStatus does not throw.
   */
  public void testDiagnosticsAllowedForWheelAndRoot() {
    configureWheelWithSubj6();
    for (Subject subject : new Subject[] {SubjectTestHelper.SUBJ6, SubjectFinder.findRootSubject()}) {
      DiagnosticsResult result = diagnostics(subject, this.otherProvisioner);
      assertFalse(subject.getId() + ": " + result.exception, isNotAllowed(result.exception));
      assertTrue(subject.getId() + " got past the gate", result.diagnosticsContainerSet || result.exception != null);

      DiagnosticsResult statusResult = diagnosticsStatus(subject, this.otherProvisioner);
      assertNull(subject.getId() + ": " + statusResult.exception, statusResult.exception);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * outcome of a handler call
   */
  private static class DiagnosticsResult {

    /** exception thrown or null */
    private RuntimeException exception;

    /** if the handler put a diagnostics container in the request (provisioner initialized) */
    private boolean diagnosticsContainerSet;
  }

  /**
   * denied: "Not allowed" thrown and the provisioner was not initialized
   * @param result result
   */
  private static void assertDenied(DiagnosticsResult result) {
    assertTrue("expected Not allowed, got: " + result.exception, isNotAllowed(result.exception));
    assertFalse("provisioner must not be initialized", result.diagnosticsContainerSet);
  }

  /**
   * @param re exception or null
   * @return true if it is the authorization failure
   */
  private static boolean isNotAllowed(RuntimeException re) {
    return re != null && re.getMessage() != null && re.getMessage().contains("Not allowed");
  }

  /**
   * whether the handler set up the diagnostics container.  In 7.9.0 the getter throws "needs to be
   * initted" instead of returning null when it was never set (which is what a refused request leaves)
   * @return true if the container was set
   */
  private static boolean diagnosticsContainerIsSet() {
    try {
      return GrouperRequestContainer.retrieveFromRequestOrCreate().getGrouperProvisioningDiagnosticsContainer() != null;
    } catch (RuntimeException re) {
      if (re.getMessage() != null && re.getMessage().contains("needs to be initted")) {
        return false;
      }
      throw re;
    }
  }

  /**
   * call diagnostics without provisionerInitted, i.e. open the settings form
   * @param loggedIn caller
   * @param provisionerConfigId provisioner
   * @return result
   */
  private DiagnosticsResult diagnostics(Subject loggedIn, String provisionerConfigId) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("provisionerConfigId", provisionerConfigId);
    DiagnosticsResult result = new DiagnosticsResult();
    try {
      try {
        new UiV2ProvisionerConfiguration().diagnostics(uiRequest.request, uiRequest.response);
      } catch (RuntimeException re) {
        result.exception = re;
      }
      result.diagnosticsContainerSet = diagnosticsContainerIsSet();
      return result;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * call diagnosticsStatus with an id that is not running (nothing to report after the gate)
   * @param loggedIn caller
   * @param provisionerConfigId provisioner
   * @return result
   */
  private DiagnosticsResult diagnosticsStatus(Subject loggedIn, String provisionerConfigId) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("provisionerConfigId", provisionerConfigId);
    uiRequest.params.put("uniqueDiagnosticsId", "junitNotRunning");
    DiagnosticsResult result = new DiagnosticsResult();
    try {
      try {
        new UiV2ProvisionerConfiguration().diagnosticsStatus(uiRequest.request, uiRequest.response);
      } catch (RuntimeException re) {
        result.exception = re;
      }
      result.diagnosticsContainerSet = diagnosticsContainerIsSet();
      return result;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * make an etc:testWheel group with SUBJ6 in it and turn wheel on (GrouperTest clears the
   * override map after each test)
   */
  private void configureWheelWithSubj6() {
    GrouperSession session = GrouperSession.startRootSession();
    try {
      Stem etc = StemFinder.findByName(session, "etc", false);
      if (etc == null) {
        etc = StemFinder.findRootStem(session).addChildStem("etc", "etc");
      }
      Group wheel = new GroupSave().assignName("etc:testWheel").save();
      wheel.addMember(SubjectTestHelper.SUBJ6, false);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    } finally {
      GrouperSession.stopQuietly(session);
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
      return (T)Proxy.newProxyInstance(UiV2ProvisionerDiagnosticsAuthorizationTest.class.getClassLoader(),
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
