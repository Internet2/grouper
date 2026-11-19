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
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperSyncObject;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.ProvisioningContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSync;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncGroup;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMember;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncMembership;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M1: the provisioning target details endpoints
 * (viewProvisioningTargetDetailsOnSubject, viewProvisioningTargetDetailsOnGroupMembership,
 * viewProvisioningTargetDetailsOnSubjectMembership) authorized the group, subject and target from
 * the request, then loaded the grouper_sync_member / grouper_sync_membership row by a separate
 * request id (groupSyncMemberId / groupSyncMembershipId) under root, without checking that the row
 * belongs to them.  Substituting the id showed another group's, subject's or provisioner's target
 * ids, caches and errors.</p>
 *
 * <p>The handlers are called directly with an in-memory request, session, response and servlet
 * (same helper as advisory M1), so no servlet container is needed.  Sync rows are created
 * through the GcGrouperSync DAOs; no provisioning job runs.</p>
 *
 * <p>Setup: target A (SUBJ0 may view it through groupAllowedToView) and target B (only its
 * grouper_sync rows, no config).  SUBJ0 has READ (so VIEW too) on the cover group and nothing on
 * the protected group.  Sync rows, each with a distinct sentinel in membership_id and entity cache 0:
 * A/cover/SUBJ1 (the allowed row), A/cover/SUBJ2 (other subject), A/protected/SUBJ2 (other group),
 * B/cover/SUBJ1 (same group and subject, other target).</p>
 *
 * <p>Before the fix the substituted ids render the other rows' sentinels.</p>
 */
public class UiV2ProvisioningSyncRowBindingTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2ProvisioningSyncRowBindingTest("testMembershipRowIsBound"));
  }

  /**
   *
   */
  public UiV2ProvisioningSyncRowBindingTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ProvisioningSyncRowBindingTest(String name) {
    super(name);
  }

  /** target A, the one SUBJ0 may view; unique per run */
  private String targetA;

  /** target B, wheel only; unique per run */
  private String targetB;

  /** group SUBJ0 can read */
  private Group cover;

  /** group SUBJ0 has no privilege on */
  private Group protectedGroup;

  /** sync row ids: [0] grouper_sync_member id, [1] grouper_sync_membership id */
  private String[] allowedRow;

  /** A/cover/SUBJ2 */
  private String[] otherSubjectRow;

  /** A/protected/SUBJ2 */
  private String[] otherGroupRow;

  /** B/cover/SUBJ1 */
  private String[] otherTargetRow;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * config, groups and sync rows, as root
   */
  @Override
  protected void setUp() {
    super.setUp();

    long suffix = System.currentTimeMillis() % 1000000;
    this.targetA = "junitProv89a_" + suffix;
    this.targetB = "junitProv89b_" + suffix;

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put("ldap.personLdap.url", "ldap://localhost:389");
    loader.put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    loader.put("ldap.personLdap.pass", "secret");
    String p = "provisioner." + this.targetA + ".";
    loader.put(p + "class", LdapSync.class.getName());
    loader.put(p + "ldapExternalSystemConfigId", "personLdap");
    loader.put(p + "groupAllowedToView", "test:provisioning89:viewers");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group viewers = new GroupSave().assignName("test:provisioning89:viewers").assignCreateParentStemsIfNotExist(true).save();
      viewers.addMember(SubjectTestHelper.SUBJ0, false);

      this.cover = new GroupSave().assignName("test:provisioning89:cover").assignCreateParentStemsIfNotExist(true).save();
      // READ, so the membership pages open whether or not advisory M1 (READ instead of VIEW) is applied
      this.cover.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      this.cover.addMember(SubjectTestHelper.SUBJ1, false);
      this.cover.addMember(SubjectTestHelper.SUBJ2, false);

      this.protectedGroup = new GroupSave().assignName("test:provisioning89:protected").assignCreateParentStemsIfNotExist(true).save();
      this.protectedGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      this.protectedGroup.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.VIEW, false);
      this.protectedGroup.addMember(SubjectTestHelper.SUBJ2, false);

      GcGrouperSync syncA = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, this.targetA);
      GcGrouperSync syncB = GcGrouperSyncDao.retrieveOrCreateByProvisionerName(null, this.targetB);

      this.allowedRow = syncRow(grouperSession, syncA, this.cover, SubjectTestHelper.SUBJ1, "ALLOWED_89");
      this.otherSubjectRow = syncRow(grouperSession, syncA, this.cover, SubjectTestHelper.SUBJ2, "OTHER_SUBJECT_89");
      this.otherGroupRow = syncRow(grouperSession, syncA, this.protectedGroup, SubjectTestHelper.SUBJ2, "OTHER_GROUP_89");
      this.otherTargetRow = syncRow(grouperSession, syncB, this.cover, SubjectTestHelper.SUBJ1, "OTHER_TARGET_89");
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // targets are cached
    GrouperProvisioningSettings.getTargets(false);
  }

  /**
   * create (or reuse) the sync group and sync member, create the sync membership, and mark the
   * member and membership with a sentinel
   * @param grouperSession root
   * @param gcGrouperSync target
   * @param group group
   * @param subject subject
   * @param sentinel value only this row has
   * @return [sync member id, sync membership id]
   */
  private static String[] syncRow(GrouperSession grouperSession, GcGrouperSync gcGrouperSync,
      Group group, Subject subject, String sentinel) {
    Member member = MemberFinder.findBySubject(grouperSession, subject, true);
    GcGrouperSyncGroup syncGroup = gcGrouperSync.getGcGrouperSyncGroupDao().groupRetrieveOrCreateByGroupId(group.getId());
    GcGrouperSyncMember syncMember = gcGrouperSync.getGcGrouperSyncMemberDao().memberRetrieveOrCreateByMemberId(member.getUuid());
    if (syncMember.getEntityAttributeValueCache0() == null) {
      syncMember.setEntityAttributeValueCache0(sentinel);
    }
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    GcGrouperSyncMembership syncMembership = gcGrouperSync.getGcGrouperSyncMembershipDao()
        .membershipRetrieveOrCreateBySyncGroupIdAndSyncMemberId(syncGroup.getId(), syncMember.getId());
    syncMembership.setMembershipId(sentinel);
    gcGrouperSync.getGcGrouperSyncDao().storeAllObjects();
    assertNotNull(syncMember.getId());
    assertNotNull(syncMembership.getId());
    return new String[] {syncMember.getId(), syncMembership.getId()};
  }

  /**
   * remove config overrides and thread locals
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("provisioningInUi.enable");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().keySet().removeIf(
        key -> key.startsWith("provisioner." + this.targetA + ".") || key.startsWith("ldap.personLdap."));
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------------

  /**
   * the two membership detail endpoints show the row for the requested group and subject in the
   * requested target, and nothing for a row of another subject, another group or another target,
   * or an unknown id
   */
  public void testMembershipRowIsBound() {
    for (String endpoint : new String[] {
        "viewProvisioningTargetDetailsOnGroupMembership", "viewProvisioningTargetDetailsOnSubjectMembership"}) {

      // control: the matching row is shown, with its own sync group and member
      GuiGrouperSyncObject allowed = call(endpoint, SubjectTestHelper.SUBJ0, this.cover, SubjectTestHelper.SUBJ1,
          "groupSyncMembershipId", this.allowedRow[1]);
      assertNotNull(endpoint + " allowed row", allowed);
      GcGrouperSyncMembership membership = allowed.getGcGrouperSyncMembership();
      assertNotNull(endpoint + " allowed row loaded", membership);
      assertEquals(this.allowedRow[1], membership.getId());
      assertEquals("ALLOWED_89", membership.getMembershipId());
      assertEquals(this.cover.getId(), membership.getGrouperSyncGroup().getGroupId());
      assertEquals(this.allowedRow[0], membership.getGrouperSyncMember().getId());

      // substituted ids: nothing shown
      String[][] substitutes = {this.otherSubjectRow, this.otherGroupRow, this.otherTargetRow, {null, "abc123notThere"}};
      for (String[] substitute : substitutes) {
        GuiGrouperSyncObject denied = call(endpoint, SubjectTestHelper.SUBJ0, this.cover, SubjectTestHelper.SUBJ1,
            "groupSyncMembershipId", substitute[1]);
        assertNotNull(endpoint + " page still renders", denied);
        assertNull(endpoint + " row " + substitute[1] + " must not be shown", denied.getGcGrouperSyncMembership());
      }

      // wheel: the matching row is still shown
      GuiGrouperSyncObject wheel = call(endpoint, SubjectFinder.findRootSubject(), this.cover, SubjectTestHelper.SUBJ1,
          "groupSyncMembershipId", this.allowedRow[1]);
      assertEquals(endpoint + " wheel", this.allowedRow[1], wheel.getGcGrouperSyncMembership().getId());
    }
  }

  /**
   * the subject detail endpoint shows the sync member row for the requested subject in the
   * requested target only
   */
  public void testMemberRowIsBound() {
    String endpoint = "viewProvisioningTargetDetailsOnSubject";

    GuiGrouperSyncObject allowed = call(endpoint, SubjectTestHelper.SUBJ0, null, SubjectTestHelper.SUBJ1,
        "groupSyncMemberId", this.allowedRow[0]);
    assertNotNull(allowed);
    assertNotNull("allowed row loaded", allowed.getGcGrouperSyncMember());
    assertEquals(this.allowedRow[0], allowed.getGcGrouperSyncMember().getId());
    assertEquals("ALLOWED_89", allowed.getGcGrouperSyncMember().getEntityAttributeValueCache0());

    // SUBJ2's row in target A, SUBJ1's row in target B, an unknown id
    for (String substitute : new String[] {this.otherSubjectRow[0], this.otherTargetRow[0], "abc123notThere"}) {
      GuiGrouperSyncObject denied = call(endpoint, SubjectTestHelper.SUBJ0, null, SubjectTestHelper.SUBJ1,
          "groupSyncMemberId", substitute);
      assertNotNull("page still renders", denied);
      assertNull("member row " + substitute + " must not be shown", denied.getGcGrouperSyncMember());
    }
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * call a provisioning target details handler
   * @param endpoint UiV2Provisioning method name
   * @param loggedIn caller
   * @param group group, or null for the subject page
   * @param memberSubject subject
   * @param idParam groupSyncMemberId or groupSyncMembershipId
   * @param idValue sync row id
   * @return what the handler put in the ProvisioningContainer
   */
  private GuiGrouperSyncObject call(String endpoint, Subject loggedIn, Group group, Subject memberSubject,
      String idParam, String idValue) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    if (group != null) {
      uiRequest.params.put("groupId", group.getId());
    }
    uiRequest.params.put("subjectId", memberSubject.getId());
    uiRequest.params.put("sourceId", memberSubject.getSourceId());
    uiRequest.params.put("provisioningTargetName", this.targetA);
    uiRequest.params.put(idParam, idValue);
    try {
      try {
        Method method = UiV2Provisioning.class.getMethod(endpoint, HttpServletRequest.class, HttpServletResponse.class);
        method.invoke(new UiV2Provisioning(), uiRequest.request, uiRequest.response);
      } catch (java.lang.reflect.InvocationTargetException ite) {
        throw new RuntimeException(endpoint, ite.getCause());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      // read the container while the request thread locals are still set
      ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      return provisioningContainer.getGuiGrouperSyncObject();
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * In-memory UI request for calling a UiV2 handler directly: request, session, response and a
   * servlet whose JSP dispatcher renders nothing, set into GrouperUiFilter's thread locals, with
   * the logged in subject stored in the SessionContainer (same helper as advisory M1's test).
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
      return (T)Proxy.newProxyInstance(UiV2ProvisioningSyncRowBindingTest.class.getClassLoader(),
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
