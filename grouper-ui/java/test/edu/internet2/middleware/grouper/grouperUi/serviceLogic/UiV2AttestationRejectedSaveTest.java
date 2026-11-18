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
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security issue attestationRejectedSavePartialState: UiV2Attestation.editStemAttestationSave
 * assigned the folder's attestation marker before it checked the caller's READ on the referenced
 * report and authorized group, with no transaction around the save, so a denied save left a
 * committed, empty marker.  After the fix the references are checked before any write, references
 * that do not apply to the attestation type are ignored, and each save is one transaction.</p>
 *
 * <p>Handlers are called directly with an in-memory request, session, response and servlet set
 * into GrouperUiFilter's thread locals.  Stored state is read back as root from fresh objects.</p>
 *
 * <p>Setup: SUBJ0 is STEM_ADMIN of the target folder (no marker), can READ test:attest107:readable
 * and the viewers group of the readable report, and cannot READ or VIEW
 * test:attest107private:unreadable or see the private report.</p>
 */
public class UiV2AttestationRejectedSaveTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2AttestationRejectedSaveTest("testDeniedAuthorizedGroupLeavesNoMarker"));
  }

  /**
   *
   */
  public UiV2AttestationRejectedSaveTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2AttestationRejectedSaveTest(String name) {
    super(name);
  }

  /** folder SUBJ0 administers */
  private Stem target;

  /** group SUBJ0 can read */
  private Group readable;

  /** group SUBJ0 cannot read */
  private Group unreadable;

  /** report SUBJ0 can read */
  private GrouperReportConfigurationBean readableReport;

  /** report SUBJ0 cannot read */
  private GrouperReportConfigurationBean privateReport;

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
      this.target = new StemSave().assignName("test:attest107:target").assignCreateParentStemsIfNotExist(true).save();
      this.target.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);

      this.readable = new GroupSave().assignName("test:attest107:readable").assignCreateParentStemsIfNotExist(true).save();
      this.readable.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      this.readable.addMember(SubjectTestHelper.SUBJ0, false);

      this.unreadable = new GroupSave().assignName("test:attest107private:unreadable").assignCreateParentStemsIfNotExist(true).save();
      this.unreadable.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      this.unreadable.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);

      // reports live on a folder; viewers group decides isCanRead
      // reports live on the target folder: advisory M5 accepts only report configurations on the attested folder
      Stem reportStem = this.target;
      GrouperReportConfigService.saveOrUpdateReportConfigAttributes(report("attest107readable", this.readable.getId()), reportStem);
      GrouperReportConfigService.saveOrUpdateReportConfigAttributes(report("attest107private", this.unreadable.getId()), reportStem);
      this.readableReport = GrouperReportConfigService.getGrouperReportConfigBean(reportStem, "attest107readable");
      this.privateReport = GrouperReportConfigService.getGrouperReportConfigBean(reportStem, "attest107private");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertNull("fixture: no marker on the target", marker());
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
  // denied references leave nothing
  // ---------------------------------------------------------------------------------------------

  /**
   * report attestation with a readable report and an authorized group SUBJ0 cannot READ: denied,
   * and no marker is left on the folder (before the fix the marker was committed first).
   */
  public void testDeniedAuthorizedGroupLeavesNoMarker() {
    saveReportAttestation(this.readableReport, this.unreadable);
    assertNull("denied save must not leave a marker", marker());
  }

  /**
   * report attestation with a report SUBJ0 cannot read: denied, no marker.
   */
  public void testDeniedReportLeavesNoMarker() {
    saveReportAttestation(this.privateReport, this.readable);
    assertNull("denied save must not leave a marker", marker());
  }

  /**
   * existing valid report attestation; a denied change of the authorized group keeps every
   * original value.
   */
  public void testDeniedChangeKeepsExistingValues() {
    saveReportAttestation(this.readableReport, this.readable);
    AttributeAssign marker = marker();
    assertNotNull(marker);
    String authorizedGroupIdName = GrouperAttestationJob.retrieveAttributeDefNameAuthorizedGroupId().getName();
    String reportIdName = GrouperAttestationJob.retrieveAttributeDefNameReportConfigurationId().getName();
    assertEquals(this.readable.getId(), value(authorizedGroupIdName));

    saveReportAttestation(this.readableReport, this.unreadable);
    assertNotNull(marker());
    assertEquals(marker.getId(), marker().getId());
    assertEquals(this.readable.getId(), value(authorizedGroupIdName));
    assertEquals(this.readableReport.getAttributeAssignmentMarkerId(), value(reportIdName));
    assertEquals("report", value(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
  }

  /**
   * group-type attestation with a stray authorized group parameter (the request parser resolves
   * it even though the form does not show it): the reference is ignored and the save completes.
   * Before the fix the late READ check threw after the marker was assigned.
   */
  public void testGroupTypeIgnoresReferences() {
    RuntimeException re = save(groupTypeParams(this.unreadable));
    assertNull("group-type save with a stray reference must not fail", re);
    assertNotNull(marker());
    assertEquals("true", value(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName()));
    assertNull("stray reference must not be stored", value(GrouperAttestationJob.retrieveAttributeDefNameAuthorizedGroupId().getName()));
    assertNull(value(GrouperAttestationJob.retrieveAttributeDefNameReportConfigurationId().getName()));
  }

  // ---------------------------------------------------------------------------------------------
  // control: allowed save writes the full configuration
  // ---------------------------------------------------------------------------------------------

  /**
   * readable report and authorized group: marker and values are written
   */
  public void testAllowedSaveWritesConfiguration() {
    assertNull(saveReportAttestation(this.readableReport, this.readable));
    assertNotNull(marker());
    assertEquals("true", value(GrouperAttestationJob.retrieveAttributeDefNameHasAttestation().getName()));
    assertEquals("report", value(GrouperAttestationJob.retrieveAttributeDefNameType().getName()));
    assertEquals(this.readable.getId(), value(GrouperAttestationJob.retrieveAttributeDefNameAuthorizedGroupId().getName()));
    assertEquals(this.readableReport.getAttributeAssignmentMarkerId(),
        value(GrouperAttestationJob.retrieveAttributeDefNameReportConfigurationId().getName()));
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
   * @param reportBean report
   * @param authorizedGroup authorized group
   * @return the exception the handler threw, or null
   */
  private RuntimeException saveReportAttestation(GrouperReportConfigurationBean reportBean, Group authorizedGroup) {
    Map<String, String> params = baseParams();
    params.put("grouperAttestationTypeName", "report");
    params.put("grouperAttestationReportConfigurationName", reportBean.getAttributeAssignmentMarkerId());
    params.put("grouperAttestationAuthorizedGroupComboName", authorizedGroup.getId());
    return save(params);
  }

  /**
   * @param strayAuthorizedGroup authorized group submitted with a group-type save
   * @return params
   */
  private Map<String, String> groupTypeParams(Group strayAuthorizedGroup) {
    Map<String, String> params = baseParams();
    params.put("grouperAttestationTypeName", "group");
    params.put("grouperAttestationAuthorizedGroupComboName", strayAuthorizedGroup.getId());
    return params;
  }

  /**
   * @return params common to the saves
   */
  private Map<String, String> baseParams() {
    Map<String, String> params = new HashMap<String, String>();
    params.put("stemId", this.target.getId());
    params.put("grouperAttestationDirectAssignmentName", "true");
    params.put("grouperAttestationHasAttestationName", "true");
    params.put("grouperAttestationDefaultCertifyName", "true");
    params.put("grouperAttestationSendEmailName", "false");
    params.put("grouperAttestationMarkAsReviewedName", "false");
    return params;
  }

  /**
   * call editStemAttestationSave as SUBJ0
   * @param params request params
   * @return the exception thrown, or null
   */
  private RuntimeException save(Map<String, String> params) {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.putAll(params);
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
