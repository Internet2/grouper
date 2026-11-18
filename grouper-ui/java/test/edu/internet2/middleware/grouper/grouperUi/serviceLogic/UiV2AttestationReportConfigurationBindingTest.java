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
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigService;
import edu.internet2.middleware.grouper.app.reports.GrouperReportConfigurationBean;
import edu.internet2.middleware.grouper.app.reports.ReportConfigFormat;
import edu.internet2.middleware.grouper.app.reports.ReportConfigType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GuiAttestation;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue attestationReportConfigurationReferenceAuthorization: folder report attestation.
 * The save resolved the submitted report configuration id (grouperAttestationReportConfigurationName)
 * globally, so a folder admin could bind a report configuration of another folder or group whose
 * viewers group they are in; the edit form only offers readable reports on the folder.  And the
 * folder attestation screen showed the configured report's name and marker id to any folder admin,
 * even one not in the report's viewers group.  After the fix the save accepts only a readable report
 * configuration on the folder (else the "report name required" validation refuses it), and the
 * screen shows a "restricted report" placeholder without marker id to users who cannot read it.</p>
 *
 * <p>Handlers are called directly with an in-memory request, session, response and servlet set
 * into GrouperUiFilter's thread locals (same harness as the issue attestationRejectedSavePartialState
 * test).  Stored state is read back as root from fresh objects.  Only APIs present from 2.6.19
 * through 7.x.</p>
 *
 * <p>Setup: SUBJ0 is STEM_ADMIN of test:attest152:target (no attestation yet) and of nothing else,
 * can READ test:attest152:authorized, and is a member of test:attest152:viewers.  Report configs:
 * attest152readable on the target (viewers), attest152private on the target (viewers group without
 * SUBJ0), attest152other on folder test:attest152other (viewers).</p>
 */
public class UiV2AttestationReportConfigurationBindingTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2AttestationReportConfigurationBindingTest("testReportOfOtherFolderRefused"));
  }

  /**
   *
   */
  public UiV2AttestationReportConfigurationBindingTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AttestationReportConfigurationBindingTest(String name) {
    super(name);
  }

  /** folder SUBJ0 administers */
  private Stem target;

  /** authorized group SUBJ0 can read */
  private Group authorized;

  /** report on the target that SUBJ0 can read */
  private GrouperReportConfigurationBean readableReport;

  /** report on the target that SUBJ0 cannot read */
  private GrouperReportConfigurationBean privateReport;

  /** report on another folder that SUBJ0 can read */
  private GrouperReportConfigurationBean otherFolderReport;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * folders, groups and reports, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperReporting.enable", "true");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.target = new StemSave(grouperSession).assignName("test:attest152:target").assignCreateParentStemsIfNotExist(true).save();
      this.target.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

      this.authorized = new GroupSave(grouperSession).assignName("test:attest152:authorized").assignCreateParentStemsIfNotExist(true).save();
      this.authorized.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);

      Group viewers = new GroupSave(grouperSession).assignName("test:attest152:viewers").assignCreateParentStemsIfNotExist(true).save();
      viewers.addMember(SubjectTestHelper.SUBJ0, false);

      Group privateViewers = new GroupSave(grouperSession).assignName("test:attest152private:privateViewers").assignCreateParentStemsIfNotExist(true).save();
      privateViewers.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      privateViewers.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);

      Stem otherFolder = new StemSave(grouperSession).assignName("test:attest152other").assignCreateParentStemsIfNotExist(true).save();

      GrouperReportConfigService.saveOrUpdateReportConfigAttributes(report("attest152readable", viewers.getId()), this.target);
      GrouperReportConfigService.saveOrUpdateReportConfigAttributes(report("attest152private", privateViewers.getId()), this.target);
      GrouperReportConfigService.saveOrUpdateReportConfigAttributes(report("attest152other", viewers.getId()), otherFolder);
      this.readableReport = GrouperReportConfigService.getGrouperReportConfigBean(this.target, "attest152readable");
      this.privateReport = GrouperReportConfigService.getGrouperReportConfigBean(this.target, "attest152private");
      this.otherFolderReport = GrouperReportConfigService.getGrouperReportConfigBean(otherFolder, "attest152other");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertNull("fixture: no attestation on the target", marker());
  }

  /**
   * remove config overrides and thread locals
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("grouperReporting.enable");
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // save
  // ---------------------------------------------------------------------------------------------

  /**
   * SUBJ0 can read the report of the other folder but it is not a report of the target: refused,
   * nothing stored.  Before the fix the target's attestation pointed at the other folder's report.
   */
  public void testReportOfOtherFolderRefused() {
    saveAs(SubjectTestHelper.SUBJ0, this.otherFolderReport);
    assertNull("a report of another folder must not be stored", reportConfigurationIdValue());
    assertNull("a refused save must not leave an attestation", marker());
  }

  /**
   * report on the target that SUBJ0 cannot read: refused, not stored
   */
  public void testUnreadableReportOnFolderRefused() {
    saveAs(SubjectTestHelper.SUBJ0, this.privateReport);
    assertFalse("a report SUBJ0 cannot read must not be stored",
        this.privateReport.getAttributeAssignmentMarkerId().equals(reportConfigurationIdValue()));
  }

  /**
   * control: readable report on the target is saved
   */
  public void testReadableReportOnFolderSaved() {
    RuntimeException re = saveAs(SubjectTestHelper.SUBJ0, this.readableReport);
    assertNull("no exception expected: " + re, re);
    assertEquals(this.readableReport.getAttributeAssignmentMarkerId(), reportConfigurationIdValue());
    assertEquals("report", value(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
  }

  // ---------------------------------------------------------------------------------------------
  // view
  // ---------------------------------------------------------------------------------------------

  /**
   * the target's attestation uses the private report (configured by GrouperSystem); SUBJ0 opens
   * the folder attestation screen: a placeholder without the report's name or marker id.  Before
   * the fix the real report configuration was on the screen.
   */
  public void testUnreadableReportShowsPlaceholder() {
    RuntimeException re = saveAs(SubjectFinder.findRootSubject(), this.privateReport);
    assertNull("root save failed: " + re, re);
    assertEquals("fixture: root stores the private report", this.privateReport.getAttributeAssignmentMarkerId(), reportConfigurationIdValue());

    GrouperReportConfigurationBean shown = viewAs(SubjectTestHelper.SUBJ0);
    assertNotNull("a placeholder is expected (a report is configured)", shown);
    assertTrue("the marker id must not be shown: " + shown.getAttributeAssignmentMarkerId(),
        GrouperUtil.isBlank(shown.getAttributeAssignmentMarkerId()));
    assertFalse("the report name must not be shown: " + shown.getReportConfigName(),
        "attest152private".equals(shown.getReportConfigName()));
    assertTrue("placeholder text expected: " + shown.getReportConfigName(), !GrouperUtil.isBlank(shown.getReportConfigName()));

    // the root user still sees the real report
    GrouperReportConfigurationBean shownToRoot = viewAs(SubjectFinder.findRootSubject());
    assertNotNull(shownToRoot);
    assertEquals(this.privateReport.getAttributeAssignmentMarkerId(), shownToRoot.getAttributeAssignmentMarkerId());
  }

  /**
   * control: the readable report is shown to SUBJ0 as before
   */
  public void testReadableReportShown() {
    RuntimeException re = saveAs(SubjectTestHelper.SUBJ0, this.readableReport);
    assertNull("save failed: " + re, re);
    GrouperReportConfigurationBean shown = viewAs(SubjectTestHelper.SUBJ0);
    assertNotNull(shown);
    assertEquals(this.readableReport.getAttributeAssignmentMarkerId(), shown.getAttributeAssignmentMarkerId());
    assertEquals("attest152readable", shown.getReportConfigName());
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @param name report config name
   * @param viewersGroupId group whose members can read the report
   * @return a disabled SQL report config (not scheduled)
   */
  private static GrouperReportConfigurationBean report(String name, String viewersGroupId) {
    GrouperReportConfigurationBean configBean = new GrouperReportConfigurationBean();
    configBean.setReportConfigDescription("attestation test report");
    configBean.setReportConfigEmailBody("body");
    configBean.setReportConfigEmailSubject("subject");
    configBean.setReportConfigEnabled(false);
    configBean.setReportConfigFilename(name + ".csv");
    configBean.setReportConfigFormat(ReportConfigFormat.CSV);
    configBean.setReportConfigName(name);
    configBean.setReportConfigQuartzCron("0 0 5 * * ?");
    configBean.setReportConfigQuery("select 1 from dual");
    configBean.setReportConfigSendEmail(false);
    configBean.setReportConfigSendEmailToViewers(false);
    configBean.setReportConfigType(ReportConfigType.SQL);
    configBean.setReportConfigViewersGroupId(viewersGroupId);
    return configBean;
  }

  /**
   * call editStemAttestationSave for a report attestation on the target
   * @param subject logged in subject
   * @param reportBean report
   * @return the exception the handler threw, or null
   */
  private RuntimeException saveAs(Subject subject, GrouperReportConfigurationBean reportBean) {
    UiRequest uiRequest = new UiRequest(subject);
    uiRequest.params.put("stemId", this.target.getId());
    uiRequest.params.put("grouperAttestationDirectAssignmentName", "true");
    uiRequest.params.put("grouperAttestationHasAttestationName", "true");
    uiRequest.params.put("grouperAttestationDefaultCertifyName", "true");
    uiRequest.params.put("grouperAttestationSendEmailName", "false");
    uiRequest.params.put("grouperAttestationMarkAsReviewedName", "false");
    uiRequest.params.put("grouperAttestationTypeName", "report");
    uiRequest.params.put("grouperAttestationReportConfigurationName", reportBean.getAttributeAssignmentMarkerId());
    uiRequest.params.put("grouperAttestationAuthorizedGroupComboName", this.authorized.getId());
    try {
      new UiV2Attestation().editStemAttestationSave(uiRequest.request, uiRequest.response);
      return null;
    } catch (RuntimeException re) {
      return re;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * open the folder attestation screen of the target and read what it would render
   * @param subject logged in subject
   * @return the report configuration the screen shows (GuiAttestation getter), or null
   */
  private GrouperReportConfigurationBean viewAs(Subject subject) {
    UiRequest uiRequest = new UiRequest(subject);
    uiRequest.params.put("stemId", this.target.getId());
    try {
      new UiV2Attestation().stemAttestation(uiRequest.request, uiRequest.response);
      GuiAttestation guiAttestation = GrouperRequestContainer.retrieveFromRequestOrCreate().getAttestationContainer().getGuiAttestation();
      assertNotNull("the screen should have the attestation settings", guiAttestation);
      return guiAttestation.getGrouperAttestationReportConfiguration();
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * @return the target folder's attestation marker, read as root from a fresh object, or null
   */
  private AttributeAssign marker() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Stem fresh = StemFinder.findByUuid(grouperSession, this.target.getId(), true);
      return fresh.getAttributeDelegate().retrieveAssignment(null,
          GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @return the stored report configuration id of the target's attestation, or null
   */
  private String reportConfigurationIdValue() {
    return value(GrouperAttestationJob.retrieveAttributeDefNameReportConfigurationId().getName());
  }

  /**
   * @param name attribute def name name
   * @return value on the target's marker, read as root, or null
   */
  private String value(String name) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Stem fresh = StemFinder.findByUuid(grouperSession, this.target.getId(), true);
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
