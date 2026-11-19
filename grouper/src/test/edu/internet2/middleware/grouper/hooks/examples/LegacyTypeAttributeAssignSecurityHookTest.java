/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.internet2.middleware.grouper.hooks.examples;

import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.subject.Subject;

/**
 * <pre>
 * Tests LegacyTypeAttributeAssignSecurityHook: when a legacy group type is protected
 * (security.types.&lt;type&gt;.wheelOnly or allowOnlyGroup), a user who is not allowed to edit
 * the type cannot add or remove the nested legacy attribute assignment on the type marker,
 * even with no value (e.g. an empty grouperLoaderType / grouperLoaderQuery assignment via WS
 * assignAttributes).  Uses a custom type set up like the migrated grouperLoader type:
 * EveryEntity has ATTR_READ and ATTR_UPDATE on the marker and attribute definitions.
 *
 * Before the fix, testNonWheelCannotAddEmptyNestedAssign, testNonWheelAssignAndValueLeavesNothing,
 * testNonWheelCannotDeleteEmptyNestedAssign and testAllowOnlyGroup (non-member part) fail.
 * </pre>
 */
public class LegacyTypeAttributeAssignSecurityHookTest extends GrouperTest {

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new LegacyTypeAttributeAssignSecurityHookTest("testNonWheelCannotAddEmptyNestedAssign"));
  }

  /**
   * @param name
   */
  public LegacyTypeAttributeAssignSecurityHookTest(String name) {
    super(name);
  }

  /** wheel group name */
  private static final String WHEEL_GROUP_NAME = "etc:wheelGroup";

  /** root session */
  private GrouperSession grouperSession;

  /** legacy group type, protected per test */
  private GroupType groupType;

  /** legacy attribute of that type (like legacyAttribute_grouperLoaderType) */
  private AttributeDefName attr;

  /** group that has the type marker assigned (by root), no attribute value */
  private Group groupWithType;

  /** wheel group */
  private Group wheelGroup;

  /** group for allowOnlyGroup */
  private Group allowedGroup;

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setupConfigs()
   */
  @Override
  protected void setupConfigs() {
    super.setupConfigs();
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", WHEEL_GROUP_NAME);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouperIncludeExclude.requireGroups.use", "false");
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    try {
      this.grouperSession = GrouperSession.startRootSession();

      this.groupType = GroupType.createType(this.grouperSession, "legacyTestType", false);
      this.attr = this.groupType.addAttribute(this.grouperSession, "legacyTestAttr", false);

      // like legacy attribute migration: everyone can update the definitions, the type
      // security hooks are what restrict the protected types
      Subject everyEntity = SubjectFinder.findAllSubject();
      this.groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(everyEntity, AttributeDefPrivilege.ATTR_READ, false);
      this.groupType.getAttributeDefName().getAttributeDef().getPrivilegeDelegate().grantPriv(everyEntity, AttributeDefPrivilege.ATTR_UPDATE, false);
      this.groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(everyEntity, AttributeDefPrivilege.ATTR_READ, false);
      this.groupType.internal_getAttributeDefForAttributes().getPrivilegeDelegate().grantPriv(everyEntity, AttributeDefPrivilege.ATTR_UPDATE, false);

      String groupName = "aStem:legacyTyped";
      this.groupWithType = Group.saveGroup(this.grouperSession, groupName, null, groupName, null, null, null, true);
      this.groupWithType.addType(this.groupType);

      // the caller is an admin of the group, not wheel or root
      this.groupWithType.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN, false);

      this.wheelGroup = Group.saveGroup(this.grouperSession, WHEEL_GROUP_NAME, null, WHEEL_GROUP_NAME, null, null, null, true);

      String allowedGroupName = "etc:legacyTypeEditors";
      this.allowedGroup = Group.saveGroup(this.grouperSession, allowedGroupName, null, allowedGroupName, null, null, null, true);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    GroupTypeSecurityHook.resetCacheSettings();
    super.tearDown();
  }

  /**
   * protect the type (or another type) and register the built in hooks with the current config
   * @param property e.g. security.types.legacyTestType.wheelOnly
   * @param value e.g. true
   */
  private void protect(String property, String value) {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put(property, value);
    // clearHook resets the registered flags so registerHookIfNecessary re-adds the hooks
    GroupTypeSecurityHook.clearHook();
    GroupTypeSecurityHook.registerHookIfNecessary(true);
    GroupTypeSecurityHook.resetCacheSettings();
    assertTrue(GroupTypeSecurityHook.isRegisteredSuccess());
  }

  /**
   * the type marker assignment on the group, looked up without security
   * @return the marker
   */
  private AttributeAssign markerAssign() {
    AttributeAssign marker = this.groupWithType.getAttributeDelegate().retrieveAssignment(null, this.groupType.getAttributeDefName(), false, true);
    // reload so each session works on a fresh object
    return GrouperDAOFactory.getFactory().getAttributeAssign().findById(marker.getId(), true);
  }

  /**
   * @return the nested legacy attribute assignments on the marker (no security)
   */
  private Set<AttributeAssign> nestedAssigns() {
    return GrouperDAOFactory.getFactory().getAttributeAssign().findByOwnerAttributeAssignId(markerAssign().getId());
  }

  /**
   * @param throwable
   * @return true if a HookVeto is in the cause chain
   */
  private static boolean isHookVeto(Throwable throwable) {
    while (throwable != null) {
      if (throwable instanceof HookVeto) {
        return true;
      }
      if (throwable.getCause() == throwable) {
        break;
      }
      throwable = throwable.getCause();
    }
    return false;
  }

  /**
   * run as a subject, expect a HookVeto
   * @param subject
   * @param runnable
   */
  private void assertVetoAs(Subject subject, Runnable runnable) {
    GrouperSession session = GrouperSession.start(subject);
    try {
      runnable.run();
      fail("should be vetoed");
    } catch (RuntimeException re) {
      assertTrue("expected a HookVeto but got: " + re, isHookVeto(re));
    } finally {
      GrouperSession.stopQuietly(session);
      GrouperSession.startRootSession();
    }
  }

  /**
   * run as a subject, expect success
   * @param subject
   * @param runnable
   */
  private void runAs(Subject subject, Runnable runnable) {
    GrouperSession session = GrouperSession.start(subject);
    try {
      runnable.run();
    } finally {
      GrouperSession.stopQuietly(session);
      GrouperSession.startRootSession();
    }
  }

  /** add an empty nested legacy attribute assignment on the marker (like WS assignAttributes, no value) */
  private final Runnable addEmpty = new Runnable() {
    public void run() {
      markerAssign().getAttributeDelegate().assignAttribute(LegacyTypeAttributeAssignSecurityHookTest.this.attr);
    }
  };

  /** add the nested assignment and a value in one call */
  private final Runnable addWithValue = new Runnable() {
    public void run() {
      markerAssign().getAttributeValueDelegate().assignValue(LegacyTypeAttributeAssignSecurityHookTest.this.attr.getName(), "someValue");
    }
  };

  /** remove the nested assignment */
  private final Runnable remove = new Runnable() {
    public void run() {
      markerAssign().getAttributeDelegate().removeAttribute(LegacyTypeAttributeAssignSecurityHookTest.this.attr);
    }
  };

  /**
   * non-wheel admin cannot create an empty nested assign on a wheel-only type
   */
  public void testNonWheelCannotAddEmptyNestedAssign() {
    protect("security.types.legacyTestType.wheelOnly", "true");

    assertVetoAs(SubjectTestHelper.SUBJ0, this.addEmpty);

    assertEquals(0, nestedAssigns().size());
  }

  /**
   * non-wheel admin assigning attribute plus value fails and leaves no partial assignment
   */
  public void testNonWheelAssignAndValueLeavesNothing() {
    protect("security.types.legacyTestType.wheelOnly", "true");

    assertVetoAs(SubjectTestHelper.SUBJ0, this.addWithValue);

    assertEquals(0, nestedAssigns().size());
  }

  /**
   * non-wheel admin cannot delete an empty nested assign created by root
   */
  public void testNonWheelCannotDeleteEmptyNestedAssign() {
    protect("security.types.legacyTestType.wheelOnly", "true");

    // root can
    this.addEmpty.run();
    assertEquals(1, nestedAssigns().size());

    assertVetoAs(SubjectTestHelper.SUBJ0, this.remove);

    assertEquals(1, nestedAssigns().size());
  }

  /**
   * root and wheel members can add, set a value, and delete
   */
  public void testRootAndWheelAllowed() {
    protect("security.types.legacyTestType.wheelOnly", "true");

    // root
    this.addWithValue.run();
    assertEquals(1, nestedAssigns().size());
    this.remove.run();
    assertEquals(0, nestedAssigns().size());

    // wheel member
    this.wheelGroup.addMember(SubjectTestHelper.SUBJ0, false);
    runAs(SubjectTestHelper.SUBJ0, this.addEmpty);
    assertEquals(1, nestedAssigns().size());
    runAs(SubjectTestHelper.SUBJ0, this.addWithValue);
    assertEquals(1, nestedAssigns().size());
    runAs(SubjectTestHelper.SUBJ0, this.remove);
    assertEquals(0, nestedAssigns().size());
  }

  /**
   * allowOnlyGroup: members allowed, others vetoed
   */
  public void testAllowOnlyGroup() {
    protect("security.types.legacyTestType.allowOnlyGroup", this.allowedGroup.getName());

    assertVetoAs(SubjectTestHelper.SUBJ0, this.addEmpty);
    assertEquals(0, nestedAssigns().size());

    this.allowedGroup.addMember(SubjectTestHelper.SUBJ0, false);
    runAs(SubjectTestHelper.SUBJ0, this.addWithValue);
    assertEquals(1, nestedAssigns().size());
    runAs(SubjectTestHelper.SUBJ0, this.remove);
    assertEquals(0, nestedAssigns().size());
  }

  /**
   * a type with no security.types setting is unchanged (hook registered because another type is
   * protected): a non-wheel admin can still add, set a value, and delete
   */
  public void testUnprotectedTypeAllowed() {
    protect("security.types.someOtherType.wheelOnly", "true");

    runAs(SubjectTestHelper.SUBJ0, this.addEmpty);
    assertEquals(1, nestedAssigns().size());
    runAs(SubjectTestHelper.SUBJ0, this.addWithValue);
    assertEquals(1, nestedAssigns().size());
    runAs(SubjectTestHelper.SUBJ0, this.remove);
    assertEquals(0, nestedAssigns().size());
  }

}
