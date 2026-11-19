package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
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
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M1: the UiV2Provisioning membership provisioning views
 * (viewProvisioningOnGroupMembership, viewProvisioningOnSubjectMembership,
 * viewProvisioningTargetDetailsOnGroupMembership, viewProvisioningTargetDetailsOnSubjectMembership)
 * and the group target details / logs pages (viewProvisioningTargetDetailsOnGroup,
 * viewProvisioningTargetLogsOnGroup) resolved the group with only VIEW and then read
 * grouper_sync rows, membership provisioning metadata, the provisioned member count and the sync
 * logs as root.  VIEW only says the group exists; these are membership data and need READ.</p>
 *
 * <p>The handlers are called directly with an in-memory request, session, response and servlet
 * (dynamic proxies set into GrouperUiFilter's thread locals), so no servlet container is needed.
 * A denied call returns before the root callback, so nothing is put in the ProvisioningContainer.</p>
 *
 * <p>Setup: SUBJ0 can view the target (groupAllowedToView).  SUBJ0 has only VIEW on
 * test:provisioning86:confidential and READ on test:provisioning86:readable.  Both groups are
 * provisioned to the target, and SUBJ1 is a member of both with membership provisioning metadata.
 * SUBJ2 is not a member of either.</p>
 *
 * <p>Before the fix the VIEW-only asserts fail (the container is filled for the confidential
 * group).</p>
 *
 * <p>Also (user decision 2026-11-13) the "provision now" handlers runGroupSync and
 * runMembershipSync need READ on the group, not VIEW.  They return no data, so the test tells the
 * outcomes apart by where the handler stopped: a VIEW-only caller is refused at the group lookup
 * (the standard "not allowed" message, no exception), while a READ caller gets past it to the
 * target assign check and, because SUBJ0 is only in groupAllowedToView, is stopped there with
 * "Cannot access provisioning.".  No sync message is sent (send is blocking and needs the
 * daemon).  Before the fix the VIEW-only caller also reaches the target check and throws.</p>
 */
public class UiV2ProvisioningMembershipReadTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2ProvisioningMembershipReadTest("testMembershipViewsNeedRead"));
  }

  /**
   *
   */
  public UiV2ProvisioningMembershipReadTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ProvisioningMembershipReadTest(String name) {
    super(name);
  }

  /** provisioner config id, unique per run */
  private String targetName;

  /** group SUBJ0 can only VIEW */
  private Group confidential;

  /** group SUBJ0 can READ */
  private Group readable;

  /** metadata item name */
  private static final String MD = "md_testRole86";

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * config, groups, provisioning assignments and membership metadata, as root
   */
  @Override
  protected void setUp() {
    super.setUp();

    this.targetName = "junitProv86_" + (System.currentTimeMillis() % 1000000);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put("ldap.personLdap.url", "ldap://localhost:389");
    loader.put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    loader.put("ldap.personLdap.pass", "secret");
    String p = "provisioner." + this.targetName + ".";
    loader.put(p + "class", LdapSync.class.getName());
    loader.put(p + "ldapExternalSystemConfigId", "personLdap");
    loader.put(p + "groupAllowedToView", "test:provisioning86:viewers");
    loader.put(p + "configureMetadata", "true");
    loader.put(p + "numberOfMetadata", "1");
    loader.put(p + "metadata.0.name", MD);
    loader.put(p + "metadata.0.formElementType", "text");
    loader.put(p + "metadata.0.valueType", "string");
    loader.put(p + "metadata.0.showForMembership", "true");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group viewers = new GroupSave().assignName("test:provisioning86:viewers").assignCreateParentStemsIfNotExist(true).save();
      viewers.addMember(SubjectTestHelper.SUBJ0, false);

      this.confidential = new GroupSave().assignName("test:provisioning86:confidential").assignCreateParentStemsIfNotExist(true).save();
      // make sure READ does not come from EveryEntity, whatever the create defaults are
      this.confidential.revokePriv(SubjectFinder.findAllSubject(), AccessPrivilege.READ, false);
      this.confidential.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      this.confidential.addMember(SubjectTestHelper.SUBJ1, false);

      this.readable = new GroupSave().assignName("test:provisioning86:readable").assignCreateParentStemsIfNotExist(true).save();
      this.readable.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.READ, false);
      this.readable.addMember(SubjectTestHelper.SUBJ1, false);

      for (Group group : new Group[] {this.confidential, this.readable}) {
        // the group is provisioned to the target (the group pages check this)
        GrouperProvisioningAttributeValue groupValue = new GrouperProvisioningAttributeValue();
        groupValue.setTargetName(this.targetName);
        groupValue.setDoProvision(this.targetName);
        groupValue.setDirectAssignment(true);
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(groupValue, group);

        // SUBJ1's membership has provisioning metadata (the membership table lists it)
        Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
        GrouperProvisioningAttributeValue membershipValue = new GrouperProvisioningAttributeValue();
        membershipValue.setTargetName(this.targetName);
        Map<String, Object> metadata = new HashMap<String, Object>();
        metadata.put(MD, "SECRET_ROLE");
        membershipValue.setMetadataNameValues(metadata);
        GrouperProvisioningService.saveOrUpdateProvisioningAttributes(membershipValue, group, member);
      }
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // targets are cached
    GrouperProvisioningSettings.getTargets(false);

    // precondition: the "VIEW only" caller really lacks READ (else the denied asserts test nothing)
    GrouperSession checkSession = GrouperSession.startRootSession();
    try {
      assertFalse("precondition: SUBJ0 must not have READ on confidential",
          this.confidential.canHavePrivilege(SubjectTestHelper.SUBJ0, AccessPrivilege.READ.getName(), false));
      assertTrue("precondition: SUBJ0 has VIEW on confidential",
          this.confidential.canHavePrivilege(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW.getName(), false));
    } finally {
      GrouperSession.stopQuietly(checkSession);
    }
  }

  /**
   * remove config overrides and thread locals
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("provisioningInUi.enable");
    GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap().keySet().removeIf(
        key -> key.startsWith("provisioner." + this.targetName + ".") || key.startsWith("ldap.personLdap."));
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // tests
  // ---------------------------------------------------------------------------------------------

  /**
   * The four membership endpoints load nothing for a caller with only VIEW on the group, for a
   * member and a non-member alike (no oracle), and work for a caller with READ.
   */
  public void testMembershipViewsNeedRead() {
    for (String endpoint : new String[] {
        "viewProvisioningOnGroupMembership", "viewProvisioningOnSubjectMembership",
        "viewProvisioningTargetDetailsOnGroupMembership", "viewProvisioningTargetDetailsOnSubjectMembership"}) {

      for (Subject memberOrNot : new Subject[] {SubjectTestHelper.SUBJ1, SubjectTestHelper.SUBJ2}) {
        Result denied = call(endpoint, SubjectTestHelper.SUBJ0, this.confidential, memberOrNot);
        assertNull(endpoint + " VIEW only: no exception", denied.exception);
        assertFalse(endpoint + " VIEW only: nothing loaded for " + memberOrNot.getId() + " (" + denied.describe() + ")", denied.loaded());
      }

      // control: READ on the group
      Result allowed = call(endpoint, SubjectTestHelper.SUBJ0, this.readable, SubjectTestHelper.SUBJ1);
      assertTrue(endpoint + " READ: loaded", allowed.loaded());
      if (endpoint.endsWith("Membership") && endpoint.startsWith("viewProvisioningOn")) {
        // the membership table lists SUBJ1's metadata for the target
        boolean found = false;
        for (GuiGrouperSyncObject guiGrouperSyncObject : allowed.guiGrouperSyncObjects) {
          if (this.targetName.equals(guiGrouperSyncObject.getTargetName())) {
            found = true;
          }
        }
        assertTrue(endpoint + " READ: target listed", found);
      }
    }
  }

  /**
   * The group target details and logs pages (provisioned member count, sync logs) load nothing
   * for a caller with only VIEW on the group, and work for a caller with READ.
   */
  public void testGroupDetailsAndLogsNeedRead() {
    for (String endpoint : new String[] {"viewProvisioningTargetDetailsOnGroup", "viewProvisioningTargetLogsOnGroup"}) {
      Result denied = call(endpoint, SubjectTestHelper.SUBJ0, this.confidential, null);
      assertNull(endpoint + " VIEW only: no exception", denied.exception);
      assertFalse(endpoint + " VIEW only: nothing loaded (" + denied.describe() + ")", denied.loaded());

      Result allowed = call(endpoint, SubjectTestHelper.SUBJ0, this.readable, null);
      assertEquals(endpoint + " READ: loaded", this.targetName, allowed.targetName);
    }
  }

  /**
   * runGroupSync and runMembershipSync refuse a caller with only VIEW on the group (stopped at
   * the group lookup, no exception) and let a caller with READ past the group lookup (stopped
   * later at the target assign check, since SUBJ0 may only view the target).  Each handler is
   * looked up by reflection: runMembershipSync does not exist before 5.11.1, and before 5.11.1
   * runGroupSync is a wheel-only handler that already needs UPDATE and READ.
   */
  public void testRunSyncNeedsRead() {
    // runMembershipSync came with the VIEW-based "provision now" buttons (GRP-4918)
    boolean hasMembershipSync = hasHandler("runMembershipSync");
    for (String endpoint : new String[] {"runGroupSync", "runMembershipSync"}) {
      if (!hasHandler(endpoint)) {
        System.out.println("skip testRunSyncNeedsRead " + endpoint + ": handler not in this release");
        continue;
      }
      Subject memberSubject = "runMembershipSync".equals(endpoint) ? SubjectTestHelper.SUBJ1 : null;

      Result denied = call(endpoint, SubjectTestHelper.SUBJ0, this.confidential, memberSubject);
      assertNull(endpoint + " VIEW only: refused at the group lookup, so no exception (" + denied.describe() + ")",
          denied.exception);

      if (!hasMembershipSync) {
        // older wheel-only runGroupSync: UPDATE and READ and wheel, a READ-only caller is refused too
        System.out.println("skip testRunSyncNeedsRead " + endpoint + " READ control: wheel-only handler in this release");
        continue;
      }

      // control: READ on the group gets past the group lookup to the target assign check
      Result allowed = call(endpoint, SubjectTestHelper.SUBJ0, this.readable, memberSubject);
      assertTrue(endpoint + " READ: past the group lookup, stopped at the target assign check ("
          + allowed.describe() + ")", messageChainContains(allowed.exception, "Cannot access provisioning."));
    }
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @param endpoint UiV2Provisioning method name
   * @return true if this release has that handler
   */
  private static boolean hasHandler(String endpoint) {
    try {
      UiV2Provisioning.class.getMethod(endpoint, HttpServletRequest.class, HttpServletResponse.class);
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  /**
   * @param throwable exception or null
   * @param text text to find
   * @return true if the exception or one of its causes has the text in its message
   */
  private static boolean messageChainContains(Throwable throwable, String text) {
    for (Throwable current = throwable; current != null; current = current.getCause()) {
      if (current.getMessage() != null && current.getMessage().contains(text)) {
        return true;
      }
      if (current.getCause() == current) {
        break;
      }
    }
    return false;
  }

  /**
   * what a handler put in the ProvisioningContainer
   */
  private static class Result {

    /** exception thrown by the handler, or null */
    private RuntimeException exception;

    /** membership table rows */
    private List<GuiGrouperSyncObject> guiGrouperSyncObjects;

    /** membership details */
    private GuiGrouperSyncObject guiGrouperSyncObject;

    /** target name (group details and logs pages) */
    private String targetName;

    /**
     * @return true if the handler loaded anything from the root callback
     */
    private boolean loaded() {
      return this.guiGrouperSyncObjects != null || this.guiGrouperSyncObject != null || this.targetName != null;
    }

    /**
     * what was loaded, for failure messages
     * @return description
     */
    private String describe() {
      return "list=" + (this.guiGrouperSyncObjects == null ? "null" : ("size " + this.guiGrouperSyncObjects.size()))
          + ", object=" + (this.guiGrouperSyncObject == null ? "null" : "set")
          + ", targetName=" + this.targetName + ", exception=" + this.exception;
    }
  }

  /**
   * call a provisioning view handler
   * @param endpoint UiV2Provisioning method name
   * @param loggedIn caller
   * @param group group
   * @param memberSubject subject of the membership, or null for the group pages
   * @return what was loaded
   */
  private Result call(String endpoint, Subject loggedIn, Group group, Subject memberSubject) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("groupId", group.getId());
    if (memberSubject != null) {
      uiRequest.params.put("subjectId", memberSubject.getId());
      uiRequest.params.put("sourceId", memberSubject.getSourceId());
    }
    uiRequest.params.put("provisioningTargetName", this.targetName);
    Result result = new Result();
    try {
      // ProvisioningContainer starts guiGrouperSyncObjects as an empty list, not null, so clear it
      // first: then any list the handler sets (even an empty one) counts as loaded
      GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer().setGuiGrouperSyncObjects(null);
      try {
        Method method = UiV2Provisioning.class.getMethod(endpoint, HttpServletRequest.class, HttpServletResponse.class);
        method.invoke(new UiV2Provisioning(), uiRequest.request, uiRequest.response);
      } catch (java.lang.reflect.InvocationTargetException ite) {
        if (ite.getCause() instanceof RuntimeException) {
          result.exception = (RuntimeException)ite.getCause();
        } else {
          throw new RuntimeException(ite.getCause());
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      // read the container while the request thread locals are still set
      ProvisioningContainer provisioningContainer = GrouperRequestContainer.retrieveFromRequestOrCreate().getProvisioningContainer();
      result.guiGrouperSyncObjects = provisioningContainer.getGuiGrouperSyncObjects();
      result.guiGrouperSyncObject = provisioningContainer.getGuiGrouperSyncObject();
      result.targetName = provisioningContainer.getTargetName();
      return result;
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
      return (T)Proxy.newProxyInstance(UiV2ProvisioningMembershipReadTest.class.getClassLoader(),
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
