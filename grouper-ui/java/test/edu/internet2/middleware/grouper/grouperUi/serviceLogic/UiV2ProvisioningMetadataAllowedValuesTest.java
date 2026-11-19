package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
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
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningMetadataAllowedValues;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItemFormElementType;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.grouperClient.collections.MultiKey;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M1: the option list of a dropdown / radio button / checkbox provisioning
 * metadata item (metadata.&lt;n&gt;.dropdownValues, or a built-in list such as the midPoint
 * targets) was only enforced by the rendered form.  UiV2Provisioning.setMetadataValues, which
 * every provisioning metadata save calls, stored any submitted value.</p>
 *
 * <p>The handlers are called directly with an in-memory request, session, response and servlet
 * (dynamic proxies set into GrouperUiFilter's thread locals), so no servlet container is needed.</p>
 *
 * <p>Setup: an offline LDAP provisioner with a group item and a membership item, both dropdowns
 * with the options reader and member.  SUBJ0 is a target assigner (not wheel) and ADMIN of
 * test:provisioning87:teamGroup; SUBJ1 is a member of that group.</p>
 *
 * <p>Before the fix the test does not compile (the helper class does not exist); with the helper
 * but without the setMetadataValues check, the "owner" saves are stored and the save asserts
 * fail.</p>
 */
public class UiV2ProvisioningMetadataAllowedValuesTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2ProvisioningMetadataAllowedValuesTest("testGroupSaveRejectsValueNotInList"));
  }

  /**
   *
   */
  public UiV2ProvisioningMetadataAllowedValuesTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ProvisioningMetadataAllowedValuesTest(String name) {
    super(name);
  }

  /** provisioner config id, unique per run */
  private String targetName;

  /** group SUBJ0 administers */
  private Group teamGroup;

  /** group metadata item name */
  private static final String MD_GROUP = "md_reviewAccessLevel";

  /** membership metadata item name */
  private static final String MD_MEMBERSHIP = "md_reviewMemberRole";

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * config and groups, as root
   */
  @Override
  protected void setUp() {
    super.setUp();

    this.targetName = "junitProv87_" + (System.currentTimeMillis() % 1000000);

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("provisioningInUi.enable", "true");

    Map<String, String> loader = GrouperLoaderConfig.retrieveConfig().propertiesOverrideMap();
    loader.put("ldap.personLdap.url", "ldap://localhost:389");
    loader.put("ldap.personLdap.user", "cn=admin,dc=example,dc=edu");
    loader.put("ldap.personLdap.pass", "secret");
    String p = "provisioner." + this.targetName + ".";
    loader.put(p + "class", LdapSync.class.getName());
    loader.put(p + "ldapExternalSystemConfigId", "personLdap");
    loader.put(p + "groupAllowedToAssign", "test:provisioning87:assigners");
    loader.put(p + "groupAllowedToView", "test:provisioning87:assigners");
    loader.put(p + "configureMetadata", "true");
    loader.put(p + "numberOfMetadata", "2");
    loader.put(p + "metadata.0.name", MD_GROUP);
    loader.put(p + "metadata.0.formElementType", "dropdown");
    loader.put(p + "metadata.0.valueType", "string");
    loader.put(p + "metadata.0.dropdownValues", "reader,member");
    loader.put(p + "metadata.0.showForGroup", "true");
    loader.put(p + "metadata.1.name", MD_MEMBERSHIP);
    loader.put(p + "metadata.1.formElementType", "dropdown");
    loader.put(p + "metadata.1.valueType", "string");
    loader.put(p + "metadata.1.dropdownValues", "reader,member");
    loader.put(p + "metadata.1.showForMembership", "true");

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group assigners = new GroupSave().assignName("test:provisioning87:assigners").assignCreateParentStemsIfNotExist(true).save();
      assigners.addMember(SubjectTestHelper.SUBJ0, false);

      this.teamGroup = new GroupSave().assignName("test:provisioning87:teamGroup").assignCreateParentStemsIfNotExist(true).save();
      this.teamGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);
      this.teamGroup.addMember(SubjectTestHelper.SUBJ1, false);
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
  // helper class rules
  // ---------------------------------------------------------------------------------------------

  /**
   * GrouperProvisioningMetadataAllowedValues: option lists are enforced for dropdown, radio
   * button and checkbox items only; blank is left to the required check.
   */
  public void testHelperRules() {
    GrouperProvisioningObjectMetadataItem dropdown = item(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN, "", "reader", "member");
    assertTrue(GrouperProvisioningMetadataAllowedValues.isRestrictedToOptions(dropdown));
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(dropdown, "reader", null));
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(dropdown, "member", null));
    assertEquals("owner", GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(dropdown, "owner", null));
    assertEquals("case-sensitive", "Reader", GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(dropdown, "Reader", null));
    assertNull("blank is the required check's job", GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(dropdown, "", null));
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(dropdown, null, null));

    GrouperProvisioningObjectMetadataItem radio = item(GrouperProvisioningObjectMetadataItemFormElementType.RADIOBUTTON, "", "true", "false");
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(radio, "true", null));
    assertEquals("maybe", GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(radio, "maybe", null));

    // a checkbox submission is rejected as a whole if any element is not an option
    GrouperProvisioningObjectMetadataItem checkbox = item(GrouperProvisioningObjectMetadataItemFormElementType.CHECKBOX, "targetA", "targetB");
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(checkbox, null, new String[] {"targetA", "targetB"}));
    assertEquals("targetC", GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(checkbox, null, new String[] {"targetA", "targetC"}));

    // free-form items are not restricted
    GrouperProvisioningObjectMetadataItem text = item(GrouperProvisioningObjectMetadataItemFormElementType.TEXT);
    assertFalse(GrouperProvisioningMetadataAllowedValues.isRestrictedToOptions(text));
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(text, "anything", null));
    GrouperProvisioningObjectMetadataItem emptyDropdown = item(GrouperProvisioningObjectMetadataItemFormElementType.DROPDOWN);
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(emptyDropdown, "anything", null));

    // API variant: scalars, collections, arrays, non-string values
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(dropdown, (Object)"member"));
    assertEquals("owner", GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(dropdown, (Object)"owner"));
    assertEquals("targetC", GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(checkbox, (Object)Arrays.asList("targetA", "targetC")));
    assertEquals("targetC", GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(checkbox, (Object)new String[] {"targetC"}));
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(radio, (Object)Boolean.TRUE));
    assertNull(GrouperProvisioningMetadataAllowedValues.firstDisallowedValue(dropdown, (Object)null));
  }

  // ---------------------------------------------------------------------------------------------
  // UI saves
  // ---------------------------------------------------------------------------------------------

  /**
   * editProvisioningOnGroupSave as a delegated assigner: a value outside the option list is
   * rejected and nothing is stored; a listed value is stored.
   */
  public void testGroupSaveRejectsValueNotInList() {
    saveGroup("owner");
    assertNull("out-of-list value must not be stored", storedGroupValue());

    saveGroup("member");
    assertEquals("member", storedGroupValue());

    // an update to an out-of-list value is rejected too, the stored value stays
    saveGroup("owner");
    assertEquals("member", storedGroupValue());
  }

  /**
   * editProvisioningOnGroupMembershipSave as a delegated assigner who is group ADMIN: same rule
   * for a membership item.
   */
  public void testMembershipSaveRejectsValueNotInList() {
    saveMembership("owner");
    assertNull("out-of-list value must not be stored", storedMembershipValue());

    saveMembership("reader");
    assertEquals("reader", storedMembershipValue());

    saveMembership("owner");
    assertEquals("reader", storedMembershipValue());
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @param formElementType form element type
   * @param keys option keys (label = key)
   * @return a metadata item
   */
  private static GrouperProvisioningObjectMetadataItem item(GrouperProvisioningObjectMetadataItemFormElementType formElementType, String... keys) {
    GrouperProvisioningObjectMetadataItem metadataItem = new GrouperProvisioningObjectMetadataItem();
    metadataItem.setName("md_unit");
    metadataItem.setFormElementType(formElementType);
    List<MultiKey> keysAndLabels = new ArrayList<MultiKey>();
    for (String key : keys) {
      keysAndLabels.add(new MultiKey(key, key));
    }
    metadataItem.setKeysAndLabelsForDropdown(keysAndLabels);
    return metadataItem;
  }

  /**
   * group provisioning save as SUBJ0
   * @param value value for the group item
   */
  private void saveGroup(String value) {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.put("groupId", this.teamGroup.getId());
    uiRequest.params.put("provisioningTargetName", this.targetName);
    uiRequest.params.put("provisioningHasConfigurationName", "true");
    uiRequest.params.put("provisioningProvisionName", "true");
    uiRequest.params.put(MD_GROUP, value);
    try {
      new UiV2Provisioning().editProvisioningOnGroupSave(uiRequest.request, uiRequest.response);
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * membership provisioning save as SUBJ0 (member SUBJ1)
   * @param value value for the membership item
   */
  private void saveMembership(String value) {
    UiRequest uiRequest = new UiRequest(SubjectTestHelper.SUBJ0);
    uiRequest.params.put("groupId", this.teamGroup.getId());
    uiRequest.params.put("subjectId", SubjectTestHelper.SUBJ1.getId());
    uiRequest.params.put("sourceId", SubjectTestHelper.SUBJ1.getSourceId());
    uiRequest.params.put("provisioningTargetName", this.targetName);
    uiRequest.params.put(MD_MEMBERSHIP, value);
    try {
      new UiV2Provisioning().editProvisioningOnGroupMembershipSave(uiRequest.request, uiRequest.response);
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * @return stored group metadata value, read as root, or null
   */
  private String storedGroupValue() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      GrouperProvisioningAttributeValue value = GrouperProvisioningService.getProvisioningAttributeValue(this.teamGroup, this.targetName);
      return metadataValue(value, MD_GROUP);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @return stored membership metadata value for SUBJ1, read as root, or null
   */
  private String storedMembershipValue() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1, true);
      GrouperProvisioningAttributeValue value = GrouperProvisioningService.getProvisioningAttributeValue(this.teamGroup, member, this.targetName);
      return metadataValue(value, MD_MEMBERSHIP);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param value attribute value or null
   * @param name metadata name
   * @return the value as a string, or null
   */
  private static String metadataValue(GrouperProvisioningAttributeValue value, String name) {
    if (value == null || value.getMetadataNameValues() == null) {
      return null;
    }
    Object result = value.getMetadataNameValues().get(name);
    return result == null ? null : result.toString();
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
      return (T)Proxy.newProxyInstance(UiV2ProvisioningMetadataAllowedValuesTest.class.getClassLoader(),
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
