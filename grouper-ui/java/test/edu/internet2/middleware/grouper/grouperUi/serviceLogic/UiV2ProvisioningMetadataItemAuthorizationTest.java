package edu.internet2.middleware.grouper.grouperUi.serviceLogic;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
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
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.app.ldapProvisioning.LdapSync;
import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningMetadataItemAuthorization;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningObjectMetadataItem;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningSettings;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.grouperUi.beans.SessionContainer;
import edu.internet2.middleware.grouper.grouperUi.beans.api.provisioning.GuiGrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.grouperUi.beans.ui.GrouperRequestContainer;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.ui.GrouperUiFilter;
import edu.internet2.middleware.subject.Subject;
import junit.textui.TestRunner;

/**
 * <p>Security advisory M1: provisioner metadata items can be restricted with
 * metadata.&lt;n&gt;.groupIdThatCanView and metadata.&lt;n&gt;.groupIdThatCanUpdate, but nothing
 * enforced them.  The fix is GrouperProvisioningMetadataItemAuthorization, called from the
 * UiV2Provisioning view pages, edit forms and save handlers and from
 * GuiGrouperProvisioningAttributeValue (group and folder provisioner tables).</p>
 *
 * <p>Setup: provisioner with metadata items md_open (no restriction) and md_restricted (view
 * group V, update group U), both for groups and memberships.  SUBJ0, SUBJ4 and SUBJ5 are target
 * assigners and ADMIN of the group; SUBJ4 is in V only, SUBJ5 is in U, SUBJ0 is in neither.
 * SUBJ1 and SUBJ2 are members of the group.</p>
 *
 * <p>Handlers are called directly with an in-memory request, session, response and servlet
 * (dynamic proxies set into GrouperUiFilter's thread locals), so no servlet container is needed.
 * Before the fix the test does not compile (the helper class does not exist).</p>
 */
public class UiV2ProvisioningMetadataItemAuthorizationTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new UiV2ProvisioningMetadataItemAuthorizationTest("testGroupMembershipSaveKeepsRestrictedValue"));
  }

  /**
   *
   */
  public UiV2ProvisioningMetadataItemAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public UiV2ProvisioningMetadataItemAuthorizationTest(String name) {
    super(name);
  }

  /** unrestricted item */
  private static final String MD_OPEN = "md_open";

  /** item with view and update groups */
  private static final String MD_RESTRICTED = "md_restricted";

  /** provisioner config id */
  private String targetName;

  /** group being provisioned */
  private Group group;

  /** view group */
  private Group viewers;

  /** update group */
  private Group updaters;

  // ---------------------------------------------------------------------------------------------
  // setup
  // ---------------------------------------------------------------------------------------------

  /**
   * config and groups, as root
   */
  @Override
  protected void setUp() {
    super.setUp();

    this.targetName = "junitProv18_" + (System.currentTimeMillis() % 1000000);

    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Group assigners = new GroupSave().assignName("test:provisioning:assigners").assignCreateParentStemsIfNotExist(true).save();
      this.viewers = new GroupSave().assignName("test:provisioning:metadataViewers").assignCreateParentStemsIfNotExist(true).save();
      this.updaters = new GroupSave().assignName("test:provisioning:metadataUpdaters").assignCreateParentStemsIfNotExist(true).save();
      this.group = new GroupSave().assignName("test:provisioning:testGroup").assignCreateParentStemsIfNotExist(true).save();
      for (Subject subject : new Subject[] {SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ4, SubjectTestHelper.SUBJ5}) {
        assigners.addMember(subject, false);
        this.group.grantPriv(subject, AccessPrivilege.ADMIN, false);
      }
      this.viewers.addMember(SubjectTestHelper.SUBJ4, false);
      this.updaters.addMember(SubjectTestHelper.SUBJ5, false);
      this.group.addMember(SubjectTestHelper.SUBJ1, false);
      this.group.addMember(SubjectTestHelper.SUBJ2, false);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

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
    loader.put(p + "numberOfMetadata", "2");
    int i = 0;
    for (String name : new String[] {MD_OPEN, MD_RESTRICTED}) {
      loader.put(p + "metadata." + i + ".name", name);
      loader.put(p + "metadata." + i + ".formElementType", "text");
      loader.put(p + "metadata." + i + ".valueType", "string");
      loader.put(p + "metadata." + i + ".showForMembership", "true");
      loader.put(p + "metadata." + i + ".showForGroup", "true");
      loader.put(p + "metadata." + i + ".canChange", "true");
      loader.put(p + "metadata." + i + ".canUpdate", "true");
      i++;
    }
    loader.put(p + "metadata.1.groupIdThatCanView", this.viewers.getId());
    loader.put(p + "metadata.1.groupIdThatCanUpdate", this.updaters.getId());

    // targets are cached
    GrouperProvisioningSettings.getTargets(false);
  }

  /**
   * remove thread locals (GrouperTest clears the config override maps)
   */
  @Override
  protected void tearDown() {
    UiRequest.clear();
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------------
  // helper rules
  // ---------------------------------------------------------------------------------------------

  /**
   * view / update rules of the helper, by item and by configured metadata name
   */
  public void testHelperRules() {
    // PrivilegeHelper.isWheelOrRoot needs a static session; every real caller (UI handler) runs in one
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      GrouperProvisioningObjectMetadataItem open = item(MD_OPEN, null, null);
      GrouperProvisioningObjectMetadataItem restricted = item(MD_RESTRICTED, this.viewers.getId(), this.updaters.getId());
      GrouperProvisioningObjectMetadataItem updateOnly = item("md_updateOnly", null, this.updaters.getId());
      GrouperProvisioningObjectMetadataItem bogus = item("md_bogus", "notAGroupId123", "notAGroupId123");

      // blank settings: unrestricted, as before
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canView(open, SubjectTestHelper.SUBJ0));
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canUpdate(open, SubjectTestHelper.SUBJ0));
      assertTrue("not a configured item", GrouperProvisioningMetadataItemAuthorization.canUpdate((GrouperProvisioningObjectMetadataItem)null, SubjectTestHelper.SUBJ0));

      // in neither group
      assertFalse(GrouperProvisioningMetadataItemAuthorization.canView(restricted, SubjectTestHelper.SUBJ0));
      assertFalse(GrouperProvisioningMetadataItemAuthorization.canUpdate(restricted, SubjectTestHelper.SUBJ0));

      // view group only
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canView(restricted, SubjectTestHelper.SUBJ4));
      assertFalse(GrouperProvisioningMetadataItemAuthorization.canUpdate(restricted, SubjectTestHelper.SUBJ4));

      // update group: update implies view
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canView(restricted, SubjectTestHelper.SUBJ5));
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canUpdate(restricted, SubjectTestHelper.SUBJ5));

      // update group only: view is not restricted
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canView(updateOnly, SubjectTestHelper.SUBJ0));
      assertFalse(GrouperProvisioningMetadataItemAuthorization.canUpdate(updateOnly, SubjectTestHelper.SUBJ0));

      // root and wheel
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canView(restricted, SubjectFinder.findRootSubject()));
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canUpdate(restricted, SubjectFinder.findRootSubject()));
      configureWheelWithSubj6();
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canView(restricted, SubjectTestHelper.SUBJ6));
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canUpdate(restricted, SubjectTestHelper.SUBJ6));

      // a group id that does not resolve fails closed, even for a would-be member
      assertFalse(GrouperProvisioningMetadataItemAuthorization.canView(bogus, SubjectTestHelper.SUBJ4));
      assertFalse(GrouperProvisioningMetadataItemAuthorization.canUpdate(bogus, SubjectTestHelper.SUBJ5));

      // null subject
      assertFalse(GrouperProvisioningMetadataItemAuthorization.canView(restricted, null));
      assertFalse(GrouperProvisioningMetadataItemAuthorization.canUpdate(restricted, null));

      // by name, from the provisioner config (used by the group / folder provisioner tables)
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canViewMetadataName(this.targetName, MD_OPEN, SubjectTestHelper.SUBJ0));
      assertFalse(GrouperProvisioningMetadataItemAuthorization.canViewMetadataName(this.targetName, MD_RESTRICTED, SubjectTestHelper.SUBJ0));
      assertTrue(GrouperProvisioningMetadataItemAuthorization.canViewMetadataName(this.targetName, MD_RESTRICTED, SubjectTestHelper.SUBJ4));
      assertTrue("built in name", GrouperProvisioningMetadataItemAuthorization.canViewMetadataName(this.targetName, "md_grouper_allowPolicyGroupOverride", SubjectTestHelper.SUBJ0));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * list helpers and preserveRestrictedValues
   */
  public void testListHelpersAndPreserve() {
    // PrivilegeHelper.isWheelOrRoot needs a static session; every real caller (UI handler) runs in one
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      List<GrouperProvisioningObjectMetadataItem> items = new ArrayList<GrouperProvisioningObjectMetadataItem>();
      items.add(item(MD_OPEN, null, null));
      items.add(item(MD_RESTRICTED, this.viewers.getId(), this.updaters.getId()));

      assertEquals(1, GrouperProvisioningMetadataItemAuthorization.filterViewable(items, SubjectTestHelper.SUBJ0).size());
      assertEquals(2, GrouperProvisioningMetadataItemAuthorization.filterViewable(items, SubjectTestHelper.SUBJ4).size());
      assertEquals(1, GrouperProvisioningMetadataItemAuthorization.filterUpdatable(items, SubjectTestHelper.SUBJ4).size());
      assertEquals(2, GrouperProvisioningMetadataItemAuthorization.filterUpdatable(items, SubjectTestHelper.SUBJ5).size());

      List<GrouperProvisioningObjectMetadataItem> form = GrouperProvisioningMetadataItemAuthorization.restrictForEditForm(items, SubjectTestHelper.SUBJ4);
      assertEquals(2, form.size());
      assertFalse(form.get(0).isReadOnly());
      assertTrue("viewable but not updatable", form.get(1).isReadOnly());

      // update: restricted value comes back from the database
      Map<String, Object> toSave = new HashMap<String, Object>();
      toSave.put(MD_OPEN, "new");
      toSave.put(MD_RESTRICTED, "ATTACK");
      Map<String, Object> database = new HashMap<String, Object>();
      database.put(MD_RESTRICTED, "RESTRICTED_ORIGINAL");
      GrouperProvisioningMetadataItemAuthorization.preserveRestrictedValues(toSave, database, items, SubjectTestHelper.SUBJ0);
      assertEquals("new", toSave.get(MD_OPEN));
      assertEquals("RESTRICTED_ORIGINAL", toSave.get(MD_RESTRICTED));

      // insert: restricted value is dropped
      toSave.put(MD_RESTRICTED, "ATTACK");
      GrouperProvisioningMetadataItemAuthorization.preserveRestrictedValues(toSave, null, items, SubjectTestHelper.SUBJ0);
      assertFalse(toSave.containsKey(MD_RESTRICTED));

      // allowed subject keeps the submitted value
      toSave.put(MD_RESTRICTED, "ALLOWED");
      GrouperProvisioningMetadataItemAuthorization.preserveRestrictedValues(toSave, database, items, SubjectTestHelper.SUBJ5);
      assertEquals("ALLOWED", toSave.get(MD_RESTRICTED));
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  // ---------------------------------------------------------------------------------------------
  // UI handlers
  // ---------------------------------------------------------------------------------------------

  /**
   * The membership save handler ignores a submitted value for an item the caller may not update:
   * on update the stored value is kept, on insert it is not stored.  A member of the update group
   * can change it.
   */
  public void testGroupMembershipSaveKeepsRestrictedValue() {
    seedMembershipValues(SubjectTestHelper.SUBJ1, "OPEN_ORIGINAL", "RESTRICTED_ORIGINAL");

    // update as SUBJ0 (not in V or U)
    assertNull(saveGroupMembership(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ1, "OPEN_NEW", "UNAUTHORIZED_UPDATE_PROOF"));
    Map<String, Object> stored = storedMembershipValues(SubjectTestHelper.SUBJ1);
    assertEquals("OPEN_NEW", stored.get(MD_OPEN));
    assertEquals("RESTRICTED_ORIGINAL", stored.get(MD_RESTRICTED));

    // update as SUBJ4 (view only)
    assertNull(saveGroupMembership(SubjectTestHelper.SUBJ4, SubjectTestHelper.SUBJ1, "OPEN_NEW2", "VIEWER_UPDATE"));
    assertEquals("RESTRICTED_ORIGINAL", storedMembershipValues(SubjectTestHelper.SUBJ1).get(MD_RESTRICTED));

    // insert as SUBJ0: restricted item not stored
    assertNull(saveGroupMembership(SubjectTestHelper.SUBJ0, SubjectTestHelper.SUBJ2, "OPEN_INSERT", "UNAUTHORIZED_INSERT"));
    stored = storedMembershipValues(SubjectTestHelper.SUBJ2);
    assertEquals("OPEN_INSERT", stored.get(MD_OPEN));
    assertFalse(stored.containsKey(MD_RESTRICTED));

    // control: update group member
    assertNull(saveGroupMembership(SubjectTestHelper.SUBJ5, SubjectTestHelper.SUBJ1, "OPEN_NEW3", "UPDATER_VALUE"));
    assertEquals("UPDATER_VALUE", storedMembershipValues(SubjectTestHelper.SUBJ1).get(MD_RESTRICTED));
  }

  /**
   * The membership edit form does not offer an item the caller may not view, and shows it read
   * only when the caller may view but not update.
   */
  public void testGroupMembershipEditFormRestrictsItems() {
    seedMembershipValues(SubjectTestHelper.SUBJ1, "OPEN_ORIGINAL", "RESTRICTED_ORIGINAL");

    Map<String, GrouperProvisioningObjectMetadataItem> items = editFormItems(SubjectTestHelper.SUBJ0);
    assertTrue(items.containsKey(MD_OPEN));
    assertFalse("SUBJ0 cannot view md_restricted", items.containsKey(MD_RESTRICTED));

    items = editFormItems(SubjectTestHelper.SUBJ4);
    assertTrue(items.containsKey(MD_RESTRICTED));
    assertTrue("SUBJ4 can view but not update", items.get(MD_RESTRICTED).isReadOnly());

    items = editFormItems(SubjectTestHelper.SUBJ5);
    assertTrue(items.containsKey(MD_RESTRICTED));
    assertFalse(items.get(MD_RESTRICTED).isReadOnly());
  }

  /**
   * The group provisioner table (GuiGrouperProvisioningAttributeValue) does not render a value
   * the caller may not view.  Called by reflection because the method signature differs in
   * 5.0.0-5.7.1, which render no metadata values in that table.
   * @throws Exception
   */
  public void testGroupProvisionerTableHidesRestrictedValue() throws Exception {
    Method convert = null;
    try {
      convert = GuiGrouperProvisioningAttributeValue.class.getMethod("convertFromGrouperProvisioningAttributeValues", List.class, GrouperObject.class);
    } catch (NoSuchMethodException nsme) {
      // 5.0.0-5.7.1: values not rendered there, nothing to check
      return;
    }

    GrouperSession grouperSession = GrouperSession.startRootSession();
    GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
    try {
      attributeValue.setTargetName(this.targetName);
      attributeValue.setDoProvision(this.targetName);
      attributeValue.setDirectAssignment(true);
      Map<String, Object> metadata = new HashMap<String, Object>();
      metadata.put(MD_OPEN, "OPEN_ORIGINAL");
      metadata.put(MD_RESTRICTED, "RESTRICTED_ORIGINAL");
      attributeValue.setMetadataNameValues(metadata);
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, this.group);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }

    assertFalse(tableValues(convert, SubjectTestHelper.SUBJ0, attributeValue).contains("RESTRICTED_ORIGINAL"));
    assertTrue(tableValues(convert, SubjectTestHelper.SUBJ0, attributeValue).contains("OPEN_ORIGINAL"));
    assertTrue(tableValues(convert, SubjectTestHelper.SUBJ4, attributeValue).contains("RESTRICTED_ORIGINAL"));
  }

  // ---------------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------------

  /**
   * @param name item name
   * @param viewGroupId view group id or null
   * @param updateGroupId update group id or null
   * @return a metadata item
   */
  private static GrouperProvisioningObjectMetadataItem item(String name, String viewGroupId, String updateGroupId) {
    GrouperProvisioningObjectMetadataItem item = new GrouperProvisioningObjectMetadataItem();
    item.setName(name);
    item.setGroupIdThatCanView(viewGroupId);
    item.setGroupIdThatCanUpdate(updateGroupId);
    return item;
  }

  /**
   * make an etc:testWheel group with SUBJ6 in it and turn wheel on (GrouperTest clears the
   * override map after each test)
   */
  private void configureWheelWithSubj6() {
    GrouperSession session = GrouperSession.startRootSession();
    try {
      Stem etc = StemFinder.findByName(session, "etc", false);
      if (etc == null) {
        etc = StemFinder.findRootStem(session).addChildStem("etc", "etc");
      }
      Group wheel = new GroupSave().assignName("etc:testWheel").save();
      wheel.addMember(SubjectTestHelper.SUBJ6, false);
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * call editProvisioningOnGroupMembershipSave
   * @param loggedIn caller
   * @param memberSubject membership subject
   * @param openValue md_open value
   * @param restrictedValue md_restricted value
   * @return the exception or null
   */
  private RuntimeException saveGroupMembership(Subject loggedIn, Subject memberSubject, String openValue, String restrictedValue) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("groupId", this.group.getId());
    uiRequest.params.put("subjectId", memberSubject.getId());
    uiRequest.params.put("sourceId", memberSubject.getSourceId());
    uiRequest.params.put("provisioningTargetName", this.targetName);
    uiRequest.params.put(MD_OPEN, openValue);
    uiRequest.params.put(MD_RESTRICTED, restrictedValue);
    try {
      new UiV2Provisioning().editProvisioningOnGroupMembershipSave(uiRequest.request, uiRequest.response);
      return null;
    } catch (RuntimeException re) {
      return re;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * open editProvisioningOnGroupMembership for SUBJ1 and return the items put in the container
   * @param loggedIn caller
   * @return items by name
   */
  private Map<String, GrouperProvisioningObjectMetadataItem> editFormItems(Subject loggedIn) {
    UiRequest uiRequest = new UiRequest(loggedIn);
    uiRequest.params.put("groupId", this.group.getId());
    uiRequest.params.put("subjectId", SubjectTestHelper.SUBJ1.getId());
    uiRequest.params.put("sourceId", SubjectTestHelper.SUBJ1.getSourceId());
    uiRequest.params.put("provisioningTargetName", this.targetName);
    try {
      try {
        new UiV2Provisioning().editProvisioningOnGroupMembership(uiRequest.request, uiRequest.response);
      } catch (RuntimeException re) {
        // anything after the container is set (breadcrumbs, jsp) is not under test; the items
        // are checked below
      }
      Map<String, GrouperProvisioningObjectMetadataItem> result = new HashMap<String, GrouperProvisioningObjectMetadataItem>();
      List<GrouperProvisioningObjectMetadataItem> items = GrouperRequestContainer.retrieveFromRequestOrCreate()
          .getProvisioningContainer().getGrouperProvisioningObjectMetadataItems();
      assertNotNull("edit form items", items);
      for (GrouperProvisioningObjectMetadataItem item : items) {
        result.put(item.getName(), item);
      }
      return result;
    } finally {
      UiRequest.clear();
    }
  }

  /**
   * @param convert GuiGrouperProvisioningAttributeValue.convertFromGrouperProvisioningAttributeValues
   * @param loggedIn caller
   * @param attributeValue group attribute value
   * @return rendered values (metadataNameValuesExternalized values) as a string
   * @throws Exception
   */
  private String tableValues(Method convert, Subject loggedIn, GrouperProvisioningAttributeValue attributeValue) throws Exception {
    new UiRequest(loggedIn);
    GrouperSession grouperSession = GrouperSession.start(loggedIn);
    try {
      List<?> guiValues = (List<?>)convert.invoke(null, Collections.singletonList(attributeValue), this.group);
      StringBuilder result = new StringBuilder();
      for (Object guiValue : guiValues) {
        // by reflection like convert: the getter is not in every release (e.g. 2.6.19)
        Map<?, ?> externalized = (Map<?, ?>)GuiGrouperProvisioningAttributeValue.class
            .getMethod("getMetadataNameValuesExternalized").invoke(guiValue);
        result.append(externalized.values()).append(" ");
      }
      return result.toString();
    } finally {
      GrouperSession.stopQuietly(grouperSession);
      UiRequest.clear();
    }
  }

  /**
   * store membership metadata for a member subject as root
   * @param memberSubject subject
   * @param openValue md_open
   * @param restrictedValue md_restricted
   */
  private void seedMembershipValues(Subject memberSubject, String openValue, String restrictedValue) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Member member = MemberFinder.findBySubject(grouperSession, memberSubject, true);
      GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setTargetName(this.targetName);
      Map<String, Object> metadata = new HashMap<String, Object>();
      metadata.put(MD_OPEN, openValue);
      metadata.put(MD_RESTRICTED, restrictedValue);
      attributeValue.setMetadataNameValues(metadata);
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, this.group, member);
    } finally {
      GrouperSession.stopQuietly(grouperSession);
    }
  }

  /**
   * @param memberSubject subject
   * @return stored membership metadata, read as root, never null
   */
  private Map<String, Object> storedMembershipValues(Subject memberSubject) {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    try {
      Member member = MemberFinder.findBySubject(grouperSession, memberSubject, true);
      GrouperProvisioningAttributeValue value = GrouperProvisioningService.getProvisioningAttributeValue(this.group, member, this.targetName);
      if (value == null || value.getMetadataNameValues() == null) {
        return new HashMap<String, Object>();
      }
      return value.getMetadataNameValues();
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
      return (T)Proxy.newProxyInstance(UiV2ProvisioningMetadataItemAuthorizationTest.class.getClassLoader(),
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
