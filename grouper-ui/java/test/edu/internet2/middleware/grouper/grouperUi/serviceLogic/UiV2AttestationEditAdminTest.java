package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiResponseJs;
import edu.internet2.middleware.grouper.grouperUi.beans.json.GuiScreenAction;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue attestationAdminAuthorizationBypass (GRP-5105): group ADMIN configures
 * attestation; READ+UPDATE users only attest and clear the review date.  editGroupAttestation and
 * editGroupAttestationSave used to admit on UPDATE and write as root, so an updater could replace,
 * disable or remove an administrator's attestation settings.</p>
 *
 * <p>Handlers are called directly with an in-memory request, session, response and servlet set
 * into GrouperUiFilter's thread locals.  Stored values are read back as root.</p>
 *
 * <p>Setup: a group with direct attestation (enabled, send email); SUBJ0 has READ and UPDATE,
 * SUBJ1 has ADMIN.</p>
 */
public class UiV2AttestationEditAdminTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2AttestationEditAdminTest("testUpdaterCannotSaveSettings"));
  }

  /**
   *
   */
  public UiV2AttestationEditAdminTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AttestationEditAdminTest(String name) {
    super(name);
  }

  /** attested group */
  private Group group;

  /**
   * attestation attributes and the group, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.group = new GroupSave().assignName("test:attest106:attested").assignCreateParentStemsIfNotExist(true).save();
      this.group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      this.group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE, false);
      this.group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, false);

      AttributeAssign assign = this.group.getAttributeDelegate().assignAttribute(
          GrouperAttestationJob.retrieveAttributeDefNameValueDef()).getAttributeAssign();
      assign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
      assign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      assign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "true");
      assign.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName(), "30");
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
  // UPDATE is not enough to configure
  // ---------------------------------------------------------------------------------------------

  /**
   * SUBJ0 (READ+UPDATE) cannot disable, change or remove the settings through the save handler.
   */
  public void testUpdaterCannotSaveSettings() {

    // disable
    assertTrue("denied", saveSettings(SubjectTestHelper.SUBJ0, "true", "false", null));
    assertEquals("true", storedValue(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName()));

    // change the interval and turn email off
    assertTrue("denied", saveSettings(SubjectTestHelper.SUBJ0, "true", "true", "5"));
    assertEquals("30", storedValue(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));
    assertEquals("true", storedValue(GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName()));

    // remove the direct assignment
    assertTrue("denied", saveSettings(SubjectTestHelper.SUBJ0, "false", "false", null));
    assertEquals("true", storedValue(GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()));
    assertEquals("true", storedValue(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName()));
  }

  /**
   * SUBJ0 (READ+UPDATE) cannot open the settings form.
   */
  public void testUpdaterCannotOpenEditForm() {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.put("groupId", this.group.getId());
    try {
      new UiV2Attestation().editGroupAttestation(uiRequest.request, uiRequest.response);
      assertTrue("edit form must be denied for UPDATE", hasErrorMessage());
      assertFalse("edit form must not be drawn", drewEditForm());
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * SUBJ0 (READ+UPDATE) can still attest and clear the review date.
   */
  public void testUpdaterCanStillAttestAndClear() {
    String dateCertifiedName = GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName();

    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.put("groupId", this.group.getId());
    try {
      new UiV2Attestation().attestGroup(uiRequest.request, uiRequest.response);
    } finally {
      UiRequest.clear();
    }
    assertEquals(new SimpleDateFormat("yyyy/MM/dd").format(new Date()), storedValue(dateCertifiedName));

    uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.put("groupId", this.group.getId());
    try {
      new UiV2Attestation().clearGroupAttestation(uiRequest.request, uiRequest.response);
    } finally {
      UiRequest.clear();
    }
    assertNull(storedValue(dateCertifiedName));
  }

  // ---------------------------------------------------------------------------------------------
  // ADMIN can configure
  // ---------------------------------------------------------------------------------------------

  /**
   * SUBJ1 (ADMIN) opens the form and saves, changes and disables the settings.
   */
  public void testAdminCanConfigure() {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ1);
    uiRequest.params.put("groupId", this.group.getId());
    try {
      new UiV2Attestation().editGroupAttestation(uiRequest.request, uiRequest.response);
      assertFalse(hasErrorMessage());
      assertTrue(drewEditForm());
    } finally {
      UiRequest.clear();
    }

    assertFalse(saveSettings(SubjectTestHelper.SUBJ1, "true", "true", "5"));
    assertEquals("5", storedValue(GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName()));

    assertFalse(saveSettings(SubjectTestHelper.SUBJ1, "true", "false", null));
    assertEquals("false", storedValue(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName()));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * call editGroupAttestationSave
   * @param loggedIn caller
   * @param directAssignment grouperAttestationDirectAssignmentName
   * @param hasAttestation grouperAttestationHasAttestationName
   * @param customDays custom recertify days, or null for the default
   * @return true if the handler denied the request (error message or exception)
   */
  private boolean saveSettings(Subject loggedIn, String directAssignment, String hasAttestation, String customDays) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("groupId", this.group.getId());
    uiRequest.params.put("grouperAttestationDirectAssignmentName", directAssignment);
    uiRequest.params.put("grouperAttestationHasAttestationName", hasAttestation);
    uiRequest.params.put("grouperAttestationSendEmailName", "false");
    uiRequest.params.put("grouperAttestationMarkAsReviewedName", "false");
    if (customDays == null) {
      uiRequest.params.put("grouperAttestationDefaultCertifyName", "true");
    } else {
      uiRequest.params.put("grouperAttestationDefaultCertifyName", "false");
      uiRequest.params.put("grouperAttestationCustomRecertifyDaysName", customDays);
    }
    try {
      new UiV2Attestation().editGroupAttestationSave(uiRequest.request, uiRequest.response);
      return hasErrorMessage();
    } catch (RuntimeException re) {
      return true;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * @return true if the current response has an error message (e.g. the privilege error from
   * UiV2Group.retrieveGroupHelper)
   */
  private static boolean hasErrorMessage() {
    for (GuiScreenAction guiScreenAction : GuiResponseJs.retrieveGuiResponseJs().getActions()) {
      if ("error".equals(guiScreenAction.getMessageType()) && guiScreenAction.getMessage() != null) {
        return true;
      }
    }
    return false;
  }

  /**
   * @return true if the current response draws the attestation edit form (editGroupAttestation
   * puts groupAttestationEdit.jsp into #groupAttestation; the test servlet renders JSPs empty, so
   * the target element is what is checked)
   */
  private static boolean drewEditForm() {
    for (GuiScreenAction guiScreenAction : GuiResponseJs.retrieveGuiResponseJs().getActions()) {
      if ("#groupAttestation".equals(guiScreenAction.getInnerHtmlJqueryHandle())) {
        return true;
      }
    }
    return false;
  }

  /**
   * @param name attribute def name name of the value
   * @return stored value on the group's attestation assignment, read as root
   */
  private String storedValue(String name) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group fresh = GroupFinder.findByUuid(grouperSession, this.group.getId(), true);
      AttributeAssign assign = fresh.getAttributeDelegate().retrieveAssignment(null,
          GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
      return assign == null ? null : assign.getAttributeValueDelegate().retrieveValueString(name);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
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
