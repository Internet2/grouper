package edu.internet2.middleware.grouper.grouperUi.beans.ui;

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
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.GuiGroup;
import edu.internet2.middleware.grouper.grouperUi.serviceLogic.UiV2Attestation;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue attestationPrivateRecipientGroupDisclosure: the attestation screens resolve
 * the configured email recipient group and authorized group in a root session and rendered them
 * with a normal GuiGroup link (UUID, name, folder path, description) for a user who can only READ
 * the reviewed group.  After the fix GuiAttestation returns a GuiGroupRestrictedReference when
 * the logged in user cannot VIEW the related group.</p>
 *
 * <p>The logged in user is set in an in-memory UI request (GrouperUiFilter thread locals).</p>
 *
 * <p>Setup: SUBJ0 can READ test:attest63:reviewed and nothing on
 * test:attest63private:recipients (EveryEntity VIEW/READ revoked).  The reviewed group's direct
 * attestation emails the private group; folder test:attest63folder has attestation that emails
 * the private group and names it as the authorized group.</p>
 */
public class GuiAttestationRecipientGroupViewTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GuiAttestationRecipientGroupViewTest("testRecipientGroupHiddenWithoutView"));
  }

  /**
   *
   */
  public GuiAttestationRecipientGroupViewTest() {
    super();
  }

  /**
   * @param name
   */
  public GuiAttestationRecipientGroupViewTest(String name) {
    super(name);
  }

  /** description of the private group, must never be rendered without VIEW */
  private static final String SECRET_DESCRIPTION = "attest63SecretDescription";

  /** reviewed group, SUBJ0 READ */
  private Group reviewed;

  /** private recipient group */
  private Group recipients;

  /** folder with attestation referencing the private group */
  private Stem folder;

  /**
   * groups, folder and attestation settings, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.recipients = new GroupSave().assignName("test:attest63private:recipients")
          .assignDescription(SECRET_DESCRIPTION).assignCreateParentStemsIfNotExist(true).save();
      this.recipients.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      this.recipients.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);

      this.reviewed = new GroupSave().assignName("test:attest63:reviewed").assignCreateParentStemsIfNotExist(true).save();
      this.reviewed.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      AttributeAssign assign = this.reviewed.getAttributeDelegate().assignAttribute(
          GrouperAttestationJob.retrieveAttributeDefNameValueDef()).getAttributeAssign();
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "true");
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameEmailGroupId().getName(), this.recipients.getId());

      this.folder = new StemSave().assignName("test:attest63folder").assignCreateParentStemsIfNotExist(true).save();
      AttributeAssign stemAssign = this.folder.getAttributeDelegate().assignAttribute(
          GrouperAttestationJob.retrieveAttributeDefNameValueDef()).getAttributeAssign();
      setValue(stemAssign, GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
      setValue(stemAssign, GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      setValue(stemAssign, GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "true");
      setValue(stemAssign, GrouperAttestationJob.retrieveAttributeDefNameEmailGroupId().getName(), this.recipients.getId());
      setValue(stemAssign, GrouperAttestationJob.retrieveAttributeDefNameAuthorizedGroupId().getName(), this.recipients.getId());
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
  // without VIEW: placeholder only
  // ---------------------------------------------------------------------------------------------

  /**
   * group screen: the recipient group is configured (raw group still there) but its link renders
   * none of its metadata
   */
  public void testRecipientGroupHiddenWithoutView() {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GuiAttestation guiAttestation = UiV2Attestation.retrieveGuiAttestation(this.reviewed);
      assertNotNull(guiAttestation);
      assertNotNull("screen still knows a recipient group is configured", guiAttestation.getGrouperAttestationEmailGroup());
      assertRestricted(guiAttestation.getGrouperAttestationEmailGuiGroup());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  /**
   * folder screen: email group and authorized group are both restricted
   */
  public void testFolderRecipientAndAuthorizedGroupHiddenWithoutView() {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GuiAttestation guiAttestation = UiV2Attestation.retrieveGuiAttestation(this.folder);
      assertNotNull(guiAttestation);
      assertRestricted(guiAttestation.getGrouperAttestationEmailGuiGroup());
      assertRestricted(guiAttestation.getGrouperAttestationAuthorizedGuiGroup());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  // ---------------------------------------------------------------------------------------------
  // with VIEW: normal link
  // ---------------------------------------------------------------------------------------------

  /**
   * after VIEW is granted the normal link (with the UUID) comes back; revoking VIEW hides it again
   */
  public void testRecipientGroupShownWithView() {
    grantView(true);
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GuiGroup guiGroup = UiV2Attestation.retrieveGuiAttestation(this.reviewed).getGrouperAttestationEmailGuiGroup();
      assertFalse(guiGroup instanceof GuiGroupRestrictedReference);
      assertEquals(this.recipients.getId(), guiGroup.getGroup().getId());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }

    grantView(false);
    uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      assertRestricted(UiV2Attestation.retrieveGuiAttestation(this.reviewed).getGrouperAttestationEmailGuiGroup());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * the gui group must be the placeholder, and none of its renderings may contain the private
   * group's UUID, name, folder or description
   * @param guiGroup gui group from GuiAttestation
   */
  private void assertRestricted(GuiGroup guiGroup) {
    assertTrue("expected a restricted placeholder, got " + (guiGroup == null ? null : guiGroup.getClass()),
        guiGroup instanceof GuiGroupRestrictedReference);
    assertNull(guiGroup.getGroup());
    for (String html : new String[] {guiGroup.getLinkWithIcon(), guiGroup.getShortLinkWithIcon(), guiGroup.getShortLinkWithIconAndPath()}) {
      assertNotNull(html);
      assertFalse(html, html.contains(this.recipients.getId()));
      assertFalse(html, html.contains("recipients"));
      assertFalse(html, html.contains("attest63private"));
      assertFalse(html, html.contains(SECRET_DESCRIPTION));
    }
  }

  /**
   * @param grant true to grant SUBJ0 VIEW on the private group, false to revoke it
   */
  private void grantView(boolean grant) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      if (grant) {
        this.recipients.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      } else {
        this.recipients.revokePriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param assign assignment
   * @param name attribute def name name
   * @param value value
   */
  private static void setValue(AttributeAssign assign, String name, String value) {
    assign.getAttributeValueDelegate().assignValueString(name, value);
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
