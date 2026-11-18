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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignable;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder;
import edu.internet2.middleware.grouper.attr.finder.AttributeAssignValueFinder.AttributeAssignValueFinderResult;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAttestation;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAttestationDaysLeftReadOnly;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue attestationReadPathWrites: viewing attestation (UiV2Attestation.groupAttestation,
 * retrieveGuiAttestation, GuiAttestation.convertGroupIntoGuiAttestation) used to persist
 * attestationCalculatedDaysLeft and migrate legacy metadata in a root session, for a caller who
 * only has READ on the group.  After the fix the screens compute the value in memory
 * (GuiAttestationDaysLeftReadOnly) and write nothing.</p>
 *
 * <p>Handlers are called directly with an in-memory request, session, response and servlet set
 * into GrouperUiFilter's thread locals.  Stored values are always read back as root.</p>
 *
 * <p>Setup: SUBJ0 has only READ on the attested group; SUBJ1 has UPDATE (control).</p>
 */
public class UiV2AttestationReadPathWritesTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2AttestationReadPathWritesTest("testGroupViewDoesNotPersistDaysLeft"));
  }

  /**
   *
   */
  public UiV2AttestationReadPathWritesTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AttestationReadPathWritesTest(String name) {
    super(name);
  }

  /** attested group, SUBJ0 READ, SUBJ1 UPDATE */
  private Group group;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

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
      this.group = new GroupSave().assignName("test:attest69:attested").assignCreateParentStemsIfNotExist(true).save();
      this.group.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      this.group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.READ, false);
      this.group.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.UPDATE, false);

      // direct attestation certified 10 days ago, 30 day interval, no calculated value
      AttributeAssign assign = this.group.getAttributeDelegate().assignAttribute(
          GrouperAttestationJob.retrieveAttributeDefNameValueDef()).getAttributeAssign();
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName(), "true");
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "false");
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameDaysUntilRecertify().getName(), "30");
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), daysAgo(10));
      assign.getAttributeDelegate().removeAttribute(GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertNull("fixture: no calculated value", storedValue(this.group,
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
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
  // read paths write nothing
  // ---------------------------------------------------------------------------------------------

  /**
   * A READ-only caller opening the group attestation screen does not persist the calculated days
   * left; the screen model still has the computed value (30 - 10 = 20).
   */
  public void testGroupViewDoesNotPersistDaysLeft() {

    // the handler (the real entry point)
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.put("groupId", this.group.getId());
    try {
      new UiV2Attestation().groupAttestation(uiRequest.request, uiRequest.response);
    } finally {
      UiRequest.clear();
    }
    assertNull("viewing must not store attestationCalculatedDaysLeft", storedValue(this.group,
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));

    // the model
    uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      GuiAttestation guiAttestation = UiV2Attestation.retrieveGuiAttestation(this.group);
      assertNotNull(guiAttestation);
      assertEquals(Integer.valueOf(20), guiAttestation.getGrouperAttestationDaysLeftUntilRecertify());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
    assertNull("retrieveGuiAttestation must not store attestationCalculatedDaysLeft", storedValue(this.group,
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
  }

  /**
   * The attestation list screens (GuiAttestation.convertGroupIntoGuiAttestation) compute the
   * value from what they read and do not persist it.
   */
  public void testListConversionDoesNotPersistDaysLeft() {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Set<Group> groups = GrouperUtil.toSet(this.group);
      AttributeAssignValueFinderResult result = new AttributeAssignValueFinder().assignOwnerGroupsOfAssignAssign(groups)
          .addAttributeDefNameId(GrouperAttestationJob.retrieveAttributeDefNameValueDef().getId())
          .assignAttributeCheckReadOnAttributeDef(false)
          .findAttributeAssignValuesResult();
      List<GuiAttestation> guiAttestations = GuiAttestation.convertGroupIntoGuiAttestation(groups, result);
      assertEquals(1, guiAttestations.size());
      // before the fix the stale map was re-read after the write, so this was null
      assertEquals(Integer.valueOf(20), guiAttestations.get(0).getGrouperAttestationDaysLeftUntilRecertify());
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
    assertNull(storedValue(this.group, GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
  }

  /**
   * Legacy folder assignment with a blank attestationHasAttestation: viewing it does not run the
   * migration (which set it to true on every folder view).
   */
  public void testFolderViewDoesNotMigrate() {
    Stem stem = null;
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      stem = new StemSave().assignName("test:attest69folder").assignCreateParentStemsIfNotExist(true).save();
      AttributeAssign assign = stem.getAttributeDelegate().assignAttribute(
          GrouperAttestationJob.retrieveAttributeDefNameValueDef()).getAttributeAssign();
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
      setValue(assign, GrouperAttestationJob.retrieveAttributeDefNameSendEmail().getName(), "false");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    String hasAttestationName = GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName();
    assertNull("fixture: legacy blank hasAttestation", storedValue(stem, hasAttestationName));

    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    grouperSession = GrouperSession.startRootSession();
    try {
      assertNotNull(UiV2Attestation.retrieveGuiAttestation(stem));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
    assertNull("viewing a folder must not migrate attestationHasAttestation", storedValue(stem, hasAttestationName));
  }

  // ---------------------------------------------------------------------------------------------
  // control: the UPDATE-gated action still persists
  // ---------------------------------------------------------------------------------------------

  /**
   * attestGroup (UPDATE) still stores the certified date and the calculated days left.
   */
  public void testAttestStillPersists() {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ1);
    uiRequest.params.put("groupId", this.group.getId());
    try {
      new UiV2Attestation().attestGroup(uiRequest.request, uiRequest.response);
    } finally {
      UiRequest.clear();
    }
    assertEquals(daysAgo(0), storedValue(this.group, GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName()));
    assertEquals("30", storedValue(this.group, GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
  }

  // ---------------------------------------------------------------------------------------------
  // the read-only calculation matches the job
  // ---------------------------------------------------------------------------------------------

  /**
   * same results as GrouperAttestationJob.updateCalculatedDaysLeft(..., false, ...)
   */
  public void testCalculateDaysLeft() {
    assertEquals(20, GuiAttestationDaysLeftReadOnly.calculateDaysLeft("true", daysAgo(10), "30", "x"));
    assertEquals("blank hasAttestation computes (legacy)", 20, GuiAttestationDaysLeftReadOnly.calculateDaysLeft(null, daysAgo(10), "30", "x"));
    assertEquals("overdue clamps at 0", 0, GuiAttestationDaysLeftReadOnly.calculateDaysLeft("true", daysAgo(40), "30", "x"));
    assertEquals("never certified", 0, GuiAttestationDaysLeftReadOnly.calculateDaysLeft("true", null, "30", "x"));
    assertEquals("attestation off", -1, GuiAttestationDaysLeftReadOnly.calculateDaysLeft("false", daysAgo(10), "30", "x"));
    assertEquals("bad date", -1, GuiAttestationDaysLeftReadOnly.calculateDaysLeft("true", "not a date", "30", "x"));
    int defaultDays = edu.internet2.middleware.grouper.cfg.GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);
    assertEquals("default interval", defaultDays - 10, GuiAttestationDaysLeftReadOnly.calculateDaysLeft("true", daysAgo(10), null, "x"));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @param days days before today
   * @return yyyy/MM/dd
   */
  private static String daysAgo(int days) {
    return new SimpleDateFormat("yyyy/MM/dd").format(new Date(System.currentTimeMillis() - days * 24L * 60 * 60 * 1000));
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
   * @param owner group or stem
   * @param name attribute def name name of the value
   * @return the stored value on the owner's attestation assignment, read as root
   */
  private static String storedValue(final AttributeAssignable owner, String name) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      // fresh object, so no delegate cache from the request is used
      AttributeAssignable fresh = owner instanceof Group
          ? GroupFinder.findByUuid(grouperSession, ((Group)owner).getId(), true)
          : StemFinder.findByUuid(grouperSession, ((Stem)owner).getId(), true);
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
