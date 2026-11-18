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
import edu.internet2.middleware.grouper.PrivilegeAttributeDefInheritanceSave;
import edu.internet2.middleware.grouper.PrivilegeGroupInheritanceSave;
import edu.internet2.middleware.grouper.PrivilegeStemInheritanceSave;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.misc.SaveMode;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.rules.RuleApi;
import edu.internet2.middleware.grouper.rules.RuleEngine;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M3 (inheritedPrivilegeAncestorRuleDeletionAuthorization): the folder
 * "inherited privileges" remove handler, UiV2Stem.removeInheritedPrivileges, authorized the
 * caller on the folder in stemId but accepted the attribute assign id of any rule the
 * ancestor-inclusive RuleFinder returned, so a STEM_ADMIN of a child folder could delete an
 * ancestor folder's inherited privilege rule (as root) and strip the privileges it had
 * propagated.  Privilege*InheritanceSave on a folder had the same flaw (delete by
 * attributeAssignId, and edit / delete by subject matched ancestor rules too).</p>
 *
 * <p>The fix (InheritedPrivilegeRuleOwnership) only lets a folder's screen or save class touch
 * rules whose marker is assigned to that exact folder, and a UI request that mixes an owned rule
 * id with any other id removes nothing.</p>
 *
 * <p>Setup as root: test:anc45 (ancestor), test:anc45:child (SUBJ0 is STEM_ADMIN, not on the
 * ancestor), test:anc45:child:grand (SUBJ0 is STEM_ADMIN), group test:anc45:child:target.
 * test:anc45 has a SUB group rule (SUBJ1 ADMIN), a SUB folder rule (SUBJ1 CREATE) and a SUB
 * attribute def rule (SUBJ1 ATTR_ADMIN); the rules daemon logic is run so SUBJ1 is ADMIN of
 * target.  The UI handler is called directly with an in-memory request (no servlet container).</p>
 *
 * <p>Before the fix the ancestor rules are deleted, so the deny tests fail.</p>
 */
public class UiV2StemInheritedPrivilegeOwnerTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2StemInheritedPrivilegeOwnerTest("testChildAdminCannotRemoveAncestorRules"));
  }

  /**
   *
   */
  public UiV2StemInheritedPrivilegeOwnerTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2StemInheritedPrivilegeOwnerTest(String name) {
    super(name);
  }

  /** ancestor folder, SUBJ0 has no privileges */
  private Stem ancestor;

  /** child folder, SUBJ0 is STEM_ADMIN */
  private Stem child;

  /** grandchild folder, SUBJ0 is STEM_ADMIN */
  private Stem grandchild;

  /** sibling folder of child, SUBJ2 is STEM_ADMIN */
  private Stem sibling;

  /** group under child that gets SUBJ1 ADMIN from the ancestor rule */
  private Group target;

  /** ancestor rule marker ids */
  private String ancestorGroupRuleId;

  /** ancestor folder rule id */
  private String ancestorFolderRuleId;

  /** ancestor attribute def rule id */
  private String ancestorAttributeDefRuleId;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * folders, group, ancestor rules, as root
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      this.ancestor = new StemSave().assignName("test:anc45").assignCreateParentStemsIfNotExist(true).save();
      this.child = new StemSave().assignName("test:anc45:child").assignCreateParentStemsIfNotExist(true).save();
      this.grandchild = new StemSave().assignName("test:anc45:child:grand").assignCreateParentStemsIfNotExist(true).save();
      this.sibling = new StemSave().assignName("test:anc45:sibling").assignCreateParentStemsIfNotExist(true).save();
      this.target = new GroupSave().assignName("test:anc45:child:target").assignCreateParentStemsIfNotExist(true).save();

      this.child.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
      this.grandchild.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM_ADMIN, false);
      this.sibling.grantPriv(SubjectTestHelper.SUBJ2, NamingPrivilege.STEM_ADMIN, false);

      this.ancestorGroupRuleId = RuleApi.inheritGroupPrivileges(this.ancestor, Scope.SUB,
          SubjectTestHelper.SUBJ1, GrouperUtil.toSet(AccessPrivilege.ADMIN)).getId();
      this.ancestorFolderRuleId = RuleApi.inheritFolderPrivileges(this.ancestor, Scope.SUB,
          SubjectTestHelper.SUBJ1, GrouperUtil.toSet(NamingPrivilege.CREATE)).getId();
      this.ancestorAttributeDefRuleId = RuleApi.inheritAttributeDefPrivileges(this.ancestor, Scope.SUB,
          SubjectTestHelper.SUBJ1, GrouperUtil.toSet(AttributeDefPrivilege.ATTR_ADMIN)).getId();
      RuleEngine.clearRuleEngineCache();

      // push the ancestor rule down to the existing group
      RuleApi.runRulesForOwner(this.ancestor);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertTrue("precondition: SUBJ1 is ADMIN of target from the ancestor rule", subj1AdminOfTarget());
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
  // UI handler
  // ---------------------------------------------------------------------------------------------

  /**
   * A child STEM_ADMIN posting ancestor rule ids (group, folder and attribute def rules) to the
   * child folder's remove handler removes nothing, and SUBJ1 keeps ADMIN on target.
   */
  public void testChildAdminCannotRemoveAncestorRules() {
    for (String ruleId : new String[] {this.ancestorGroupRuleId, this.ancestorFolderRuleId, this.ancestorAttributeDefRuleId}) {
      removeInheritedPrivileges(SubjectTestHelper.SUBJ0, this.child, ruleId);
      assertTrue("ancestor rule must remain: " + ruleId, ruleExists(ruleId));
    }
    assertTrue("propagated privilege must remain", subj1AdminOfTarget());
  }

  /**
   * Same from a grandchild folder (rule owned two levels up), and from a sibling folder.
   */
  public void testGrandchildAndSiblingAdminCannotRemoveAncestorRule() {
    removeInheritedPrivileges(SubjectTestHelper.SUBJ0, this.grandchild, this.ancestorGroupRuleId);
    assertTrue(ruleExists(this.ancestorGroupRuleId));
    removeInheritedPrivileges(SubjectTestHelper.SUBJ2, this.sibling, this.ancestorGroupRuleId);
    assertTrue(ruleExists(this.ancestorGroupRuleId));
    assertTrue(subj1AdminOfTarget());
  }

  /**
   * A request with one rule owned by the child and one ancestor rule removes neither (all or
   * nothing); then the child's own rule alone is removed (control).
   */
  public void testMixedRequestRemovesNothingOwnRuleRemoved() {
    String childRuleId = childGroupRuleAsRoot(Scope.ONE);
    removeInheritedPrivileges(SubjectTestHelper.SUBJ0, this.child, childRuleId, this.ancestorGroupRuleId);
    assertTrue("ancestor rule must remain", ruleExists(this.ancestorGroupRuleId));
    assertTrue("mixed request must not remove the owned rule either", ruleExists(childRuleId));

    removeInheritedPrivileges(SubjectTestHelper.SUBJ0, this.child, childRuleId);
    assertFalse("the child's own rule is removed", ruleExists(childRuleId));
    assertTrue(ruleExists(this.ancestorGroupRuleId));
  }

  /**
   * An unknown id alone is rejected and nothing changes.
   */
  public void testUnknownRuleIdRemovesNothing() {
    String childRuleId = childGroupRuleAsRoot(Scope.SUB);
    removeInheritedPrivileges(SubjectTestHelper.SUBJ0, this.child, childRuleId, "notARealId45");
    assertTrue(ruleExists(childRuleId));
  }

  // ---------------------------------------------------------------------------------------------
  // API save classes
  // ---------------------------------------------------------------------------------------------

  /**
   * Privilege*InheritanceSave on the child, as SUBJ0, delete by the ancestor's attributeAssignId:
   * no change and the ancestor rules remain.
   */
  public void testSaveClassesDeleteByIdIgnoreAncestorRules() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      new PrivilegeGroupInheritanceSave().assignStem(this.child).assignAttributeAssignId(this.ancestorGroupRuleId)
        .assignSaveMode(SaveMode.DELETE).save();
      new PrivilegeStemInheritanceSave().assignStem(this.child).assignAttributeAssignId(this.ancestorFolderRuleId)
        .assignSaveMode(SaveMode.DELETE).save();
      new PrivilegeAttributeDefInheritanceSave().assignStem(this.child).assignAttributeAssignId(this.ancestorAttributeDefRuleId)
        .assignSaveMode(SaveMode.DELETE).save();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertTrue(ruleExists(this.ancestorGroupRuleId));
    assertTrue(ruleExists(this.ancestorFolderRuleId));
    assertTrue(ruleExists(this.ancestorAttributeDefRuleId));
    assertTrue(subj1AdminOfTarget());
  }

  /**
   * PrivilegeGroupInheritanceSave on the child, as SUBJ0, delete by subject and privilege (no
   * id) does not match, edit or delete the ancestor's rule for the same subject.
   */
  public void testSaveClassDeleteBySubjectIgnoresAncestorRule() {
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      new PrivilegeGroupInheritanceSave().assignStem(this.child).assignSubject(SubjectTestHelper.SUBJ1)
        .addPrivilege(AccessPrivilege.ADMIN).assignStemScope(Scope.SUB).assignSaveMode(SaveMode.DELETE).save();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertTrue(ruleExists(this.ancestorGroupRuleId));
    assertTrue(subj1AdminOfTarget());
  }

  /**
   * Control: the child's own rule can still be deleted through the save class.  Deletes by
   * subject and privilege: delete by attributeAssignId alone is NO_CHANGE in the unpatched
   * release too (no privileges in the request), so it cannot be the control.
   */
  public void testSaveClassDeletesOwnRule() {
    String childRuleId = childGroupRuleAsRoot(Scope.SUB);
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {
      new PrivilegeGroupInheritanceSave().assignStem(this.child).assignSubject(SubjectTestHelper.SUBJ3)
        .addPrivilege(AccessPrivilege.VIEW).assignStemScope(Scope.SUB).assignSaveMode(SaveMode.DELETE).save();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
    assertFalse(ruleExists(childRuleId));
    assertTrue(ruleExists(this.ancestorGroupRuleId));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * call UiV2Stem.removeInheritedPrivileges as a user.  Exceptions are swallowed: the asserts
   * are on the database (the deletes happen before the screen is rendered).
   * @param loggedIn caller
   * @param stem folder in the stemId param
   * @param ruleIds privilegeRuleRow_N[] params
   */
  private void removeInheritedPrivileges(Subject loggedIn, Stem stem, String... ruleIds) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("stemId", stem.getId());
    for (int i = 0; i < ruleIds.length; i++) {
      uiRequest.params.put("privilegeRuleRow_" + i + "[]", ruleIds[i]);
    }
    try {
      new UiV2Stem().removeInheritedPrivileges(uiRequest.request, uiRequest.response);
    } catch (RuntimeException re) {
      // denied or rendering failed, see the database asserts
    } finally {
      UiRequest.clear();
    }
    RuleEngine.clearRuleEngineCache();
  }

  /**
   * @param ruleId rule marker attribute assign id
   * @return true if the assignment is still in the database
   */
  private boolean ruleExists(String ruleId) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      AttributeAssign attributeAssign = GrouperDAOFactory.getFactory().getAttributeAssign().findById(ruleId, false, false);
      return attributeAssign != null;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @return true if SUBJ1 has ADMIN on target, checked as root
   */
  private boolean subj1AdminOfTarget() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      return this.target.hasAdmin(SubjectTestHelper.SUBJ1);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param scope scope
   * @return id of a new group rule owned by the child folder (SUBJ3 VIEW), as root
   */
  private String childGroupRuleAsRoot(Scope scope) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      String id = RuleApi.inheritGroupPrivileges(this.child, scope,
          SubjectTestHelper.SUBJ3, GrouperUtil.toSet(AccessPrivilege.VIEW)).getId();
      RuleEngine.clearRuleEngineCache();
      return id;
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

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
      return (T)Proxy.newProxyInstance(UiV2StemInheritedPrivilegeOwnerTest.class.getClassLoader(),
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
