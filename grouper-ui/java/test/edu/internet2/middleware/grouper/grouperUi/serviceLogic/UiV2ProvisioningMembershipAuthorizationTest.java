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

import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningTarget;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouperClient.jdbc.tableSync.GcGrouperSyncDao;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M1: the UiV2Provisioning membership metadata save handlers
 * (editProvisioningOnGroupMembershipSave, editProvisioningOnSubjectMembershipSave) passed null
 * instead of the group to GrouperProvisioningService.isTargetEditable, which skipped the group
 * ADMIN check (GRP-6163), and the membership edit forms created a grouper_sync row for the
 * requested target name before the target was validated or the caller was authorized.</p>
 *
 * <p>The handlers are called directly with an in-memory request, session, response and servlet
 * (dynamic proxies set into GrouperUiFilter's thread locals), so no servlet container is needed.</p>
 *
 * <p>Setup: target assigners group with SUBJ0 and SUBJ3; SUBJ0 is ADMIN of attackerOwned and
 * only has VIEW on protectedGroup; SUBJ2 is ADMIN of protectedGroup but not an assigner; SUBJ1 is
 * a member of both groups.</p>
 *
 * <p>The group ADMIN asserts need GRP-6163 in GrouperProvisioningService (4.25.0+, 6.4.0+ and
 * 7.5.0+); this issue's sets backport it for 4.17.7, 4.21.2, 4.24.0 and 5.13.0 to 7.4.0
 * (testTargetEditableNeedsObjectAdmin covers that check directly).  On 5.0.0 to 5.12.2 and
 * 2.6.19 isTargetEditable ignores the group, so those asserts fail there with or without this
 * fix.  The grouper_sync asserts apply to every release.</p>
 */
public class UiV2ProvisioningMembershipAuthorizationTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2ProvisioningMembershipAuthorizationTest("testGroupMembershipSaveNeedsGroupAdmin"));
  }

  /**
   *
   */
  public UiV2ProvisioningMembershipAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ProvisioningMembershipAuthorizationTest(String name) {
    super(name);
  }

  /** provisioner config id, unique per run so grouper_sync rows from earlier runs do not matter */
  private String targetName;

  /** group SUBJ0 administers */
  private Group attackerOwned;

  /** group SUBJ0 can only VIEW */
  private Group protectedGroup;

  /** metadata item name */
  private static final String MD = "md_testRole";

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * config and groups, as root
   */
  @Override
  protected void setUp() {
    super.setUp();

    this.targetName = "junitProv16_" + (System.currentTimeMillis() % 1000000);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put("ldap.personLdap.url", "ldap://localhost:389");
    loader.put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    loader.put("ldap.personLdap.pass", "secret");
    String p = "provisioner." + this.targetName + ".";
    loader.put(p + "class", LdapSync.class.getName());
    loader.put(p + "ldapExternalSystemConfigId", "personLdap");
    loader.put(p + "groupAllowedToAssign", "test:provisioning:assigners");
    loader.put(p + "groupAllowedToView", "test:provisioning:assigners");
    loader.put(p + "configureMetadata", "true");
    loader.put(p + "numberOfMetadata", "1");
    loader.put(p + "metadata.0.name", MD);
    loader.put(p + "metadata.0.formElementType", "text");
    loader.put(p + "metadata.0.valueType", "string");
    loader.put(p + "metadata.0.showForMembership", "true");
    loader.put(p + "metadata.0.canChange", "true");
    loader.put(p + "metadata.0.canUpdate", "true");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group assigners = new GroupSave().assignName("test:provisioning:assigners").assignCreateParentStemsIfNotExist(true).save();
      assigners.addMember(SubjectTestHelper.SUBJ0, false);
      assigners.addMember(SubjectTestHelper.SUBJ3, false);

      this.attackerOwned = new GroupSave().assignName("test:provisioning:attackerOwned").assignCreateParentStemsIfNotExist(true).save();
      this.attackerOwned.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
      this.attackerOwned.addMember(SubjectTestHelper.SUBJ1, false);

      this.protectedGroup = new GroupSave().assignName("test:provisioning:protectedGroup").assignCreateParentStemsIfNotExist(true).save();
      this.protectedGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
      this.protectedGroup.grantPriv(SubjectTestHelper.SUBJ2, AccessPrivilege.ADMIN, false);
      this.protectedGroup.addMember(SubjectTestHelper.SUBJ1, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    // targets are cached
    GrouperProvisioningSettings.getTargets(false);
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
  // save handlers: group ADMIN is required
  // ---------------------------------------------------------------------------------------------

  /**
   * A target assigner with only VIEW on the group cannot insert or change membership metadata
   * through editProvisioningOnGroupMembershipSave; with ADMIN it can.
   */
  public void testGroupMembershipSaveNeedsGroupAdmin() {
    membershipSaveNeedsGroupAdmin(true);
  }

  /**
   * Same as {@link #testGroupMembershipSaveNeedsGroupAdmin()} for
   * editProvisioningOnSubjectMembershipSave.
   */
  public void testSubjectMembershipSaveNeedsGroupAdmin() {
    membershipSaveNeedsGroupAdmin(false);
  }

  /**
   * @param groupScreen true for the group membership save, false for the subject membership save
   */
  private void membershipSaveNeedsGroupAdmin(boolean groupScreen) {

    // insert denied, nothing written
    assertTrue("SUBJ0 has only VIEW on the group", saveMembership(groupScreen, SubjectTestHelper.SUBJ0, this.protectedGroup, "ATTACK_INSERT"));
    assertNull(storedValue(this.protectedGroup));

    // update denied, stored value unchanged
    seedValue(this.protectedGroup, "ORIGINAL");
    assertTrue(saveMembership(groupScreen, SubjectTestHelper.SUBJ0, this.protectedGroup, "ATTACK_UPDATE"));
    assertEquals("ORIGINAL", storedValue(this.protectedGroup));

    // control: group ADMIN, insert then update
    assertFalse(saveMembership(groupScreen, SubjectTestHelper.SUBJ0, this.attackerOwned, "MINE_1"));
    assertEquals("MINE_1", storedValue(this.attackerOwned));
    assertFalse(saveMembership(groupScreen, SubjectTestHelper.SUBJ0, this.attackerOwned, "MINE_2"));
    assertEquals("MINE_2", storedValue(this.attackerOwned));

    // control: group ADMIN but not a target assigner (passed before the fix too)
    assertTrue(saveMembership(groupScreen, SubjectTestHelper.SUBJ2, this.protectedGroup, "NOT_ASSIGNER"));
    assertEquals("ORIGINAL", storedValue(this.protectedGroup));
  }

  // ---------------------------------------------------------------------------------------------
  // isTargetEditable: ADMIN on the group or folder is required (GRP-6163)
  // ---------------------------------------------------------------------------------------------

  /**
   * isTargetEditable in its own root session: PrivilegeHelper.isWheelOrRoot needs an open session
   * (4.x and others), and without one the result depends on a session an earlier test left open
   * @param target
   * @param subject
   * @param grouperObject
   * @return if editable
   */
  private static boolean targetEditable(GrouperProvisioningTarget target, Subject subject, GrouperObject grouperObject) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      return GrouperProvisioningService.isTargetEditable(target, subject, grouperObject);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * A target assigner may edit provisioning on a group only with ADMIN on it, and on a folder only
   * with STEM_ADMIN on it.  The UI group and folder provisioning saves and removes rely on this
   * check (they load the group with READ, the folder with no privilege).  4.17.7, 4.21.2 and 4.24.0
   * lacked it (GRP-6163 is in 4.25.0, 6.4.0 and 7.5.0); the 4.x and 5.13.0-7.4.0 sets of advisory M1
   * backport it.  Releases without GRP-6163 and without a backport (5.0.0-5.12.2, 2.6.19) fail here.
   */
  public void testTargetEditableNeedsObjectAdmin() {
    GrouperProvisioningTarget target = GrouperProvisioningSettings.getTargets(true).get(this.targetName);
    assertNotNull("target " + this.targetName, target);

    // groups: VIEW only is refused, ADMIN is allowed
    assertFalse("SUBJ0 has only VIEW on protectedGroup",
        targetEditable(target, SubjectTestHelper.SUBJ0, this.protectedGroup));
    assertTrue("SUBJ0 is ADMIN of attackerOwned",
        targetEditable(target, SubjectTestHelper.SUBJ0, this.attackerOwned));

    // folders: no STEM_ADMIN is refused, STEM_ADMIN is allowed
    Stem folder = null;
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      folder = StemFinder.findByName(grouperSession, "test:provisioning", true);
      folder.revokePriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertFalse("SUBJ0 is not STEM_ADMIN of test:provisioning",
        targetEditable(target, SubjectTestHelper.SUBJ0, folder));

    grouperSession = GrouperSession.startRootSession();
    try {
      folder.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertTrue("SUBJ0 is STEM_ADMIN of test:provisioning",
        targetEditable(target, SubjectTestHelper.SUBJ0, folder));

    // control: not a target assigner, even with ADMIN
    assertFalse("SUBJ2 is not a target assigner",
        targetEditable(target, SubjectTestHelper.SUBJ2, this.protectedGroup));
  }

  // ---------------------------------------------------------------------------------------------
  // edit forms: no grouper_sync row before validation / authorization
  // ---------------------------------------------------------------------------------------------

  /**
   * Opening the group membership edit form with an unknown target name fails and does not
   * create a grouper_sync row for that name; a valid target the caller may not edit does not
   * create one either.
   */
  public void testGroupMembershipEditFormCreatesNoSyncRow() {
    editFormCreatesNoSyncRow(true);
  }

  /**
   * Same as {@link #testGroupMembershipEditFormCreatesNoSyncRow()} for the subject membership
   * edit form.
   */
  public void testSubjectMembershipEditFormCreatesNoSyncRow() {
    editFormCreatesNoSyncRow(false);
  }

  /**
   * @param groupScreen true for the group membership edit form
   */
  private void editFormCreatesNoSyncRow(boolean groupScreen) {
    String junkTarget = "junk16_" + (System.currentTimeMillis() % 1000000) + (groupScreen ? "g" : "s");
    assertNull(GcGrouperSyncDao.retrieveByProvisionerName(null, junkTarget));

    RuntimeException re = openEditForm(groupScreen, SubjectTestHelper.SUBJ0, this.attackerOwned, junkTarget);
    assertNotNull("unknown target must fail", re);
    assertNull("no grouper_sync row for an unknown target", GcGrouperSyncDao.retrieveByProvisionerName(null, junkTarget));

    // valid target, caller is not allowed (not a target assigner)
    assertNull(GcGrouperSyncDao.retrieveByProvisionerName(null, this.targetName));
    re = openEditForm(groupScreen, SubjectTestHelper.SUBJ2, this.protectedGroup, this.targetName);
    assertNotNull("caller not allowed must fail", re);
    assertNull("no grouper_sync row for a target the caller may not edit", GcGrouperSyncDao.retrieveByProvisionerName(null, this.targetName));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * call a membership save handler
   * @param groupScreen true for editProvisioningOnGroupMembershipSave
   * @param loggedIn caller
   * @param group group of the membership (member is SUBJ1)
   * @param value value for the metadata item
   * @return true if the handler threw (denied)
   */
  private boolean saveMembership(boolean groupScreen, Subject loggedIn, Group group, String value) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("groupId", group.getId());
    uiRequest.params.put("subjectId", SubjectTestHelper.SUBJ1.getId());
    uiRequest.params.put("sourceId", SubjectTestHelper.SUBJ1.getSourceId());
    uiRequest.params.put("provisioningTargetName", this.targetName);
    uiRequest.params.put(MD, value);
    try {
      if (groupScreen) {
        new UiV2Provisioning().editProvisioningOnGroupMembershipSave(uiRequest.request, uiRequest.response);
      } else {
        new UiV2Provisioning().editProvisioningOnSubjectMembershipSave(uiRequest.request, uiRequest.response);
      }
      return false;
    } catch (RuntimeException re) {
      return true;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * open a membership edit form
   * @param groupScreen true for editProvisioningOnGroupMembership
   * @param loggedIn caller
   * @param group group (member is SUBJ1)
   * @param theTargetName target name request param
   * @return the exception thrown, or null
   */
  private RuntimeException openEditForm(boolean groupScreen, Subject loggedIn, Group group, String theTargetName) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("groupId", group.getId());
    uiRequest.params.put("subjectId", SubjectTestHelper.SUBJ1.getId());
    uiRequest.params.put("sourceId", SubjectTestHelper.SUBJ1.getSourceId());
    uiRequest.params.put("provisioningTargetName", theTargetName);
    try {
      if (groupScreen) {
        new UiV2Provisioning().editProvisioningOnGroupMembership(uiRequest.request, uiRequest.response);
      } else {
        new UiV2Provisioning().editProvisioningOnSubjectMembership(uiRequest.request, uiRequest.response);
      }
      return null;
    } catch (RuntimeException re) {
      return re;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * @param group group
   * @return stored metadata value for SUBJ1 in the group, read as root, or null
   */
  private String storedValue(Group group) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      GrouperProvisioningAttributeValue value = GrouperProvisioningService.getProvisioningAttributeValue(group, member, this.targetName);
      if (value == null || value.getMetadataNameValues() == null) {
        return null;
      }
      Object result = value.getMetadataNameValues().get(MD);
      return result == null ? null : result.toString();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * store a metadata value for SUBJ1 in the group as root
   * @param group group
   * @param value value
   */
  private void seedValue(Group group, String value) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setTargetName(this.targetName);
      Map<String, Object> metadata = new HashMap<String, Object>();
      metadata.put(MD, value);
      attributeValue.setMetadataNameValues(metadata);
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, group, member);
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
      return (T)Proxy.newProxyInstance(UiV2ProvisioningMembershipAuthorizationTest.class.getClassLoader(),
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
