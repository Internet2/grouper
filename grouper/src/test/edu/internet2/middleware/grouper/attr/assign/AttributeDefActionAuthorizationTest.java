/**
 * Copyright 2026 Internet2
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
package edu.internet2.middleware.grouper.attr.assign;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperSessionHandler;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.rules.RuleApi;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.subject.Subject;

/**
 * Security advisory M2 (WS attribute definition action authorization): adding, removing or
 * replacing the actions of an attribute definition must require ATTR_ADMIN on it (root and
 * wheel are allowed).  Before the fix AttributeDefActionDelegate wrote the action rows with no
 * privilege check, so the WS assignAttributeDefActions operation (and any API caller) let a
 * subject with only ATTR_VIEW delete an action, and with it, as root, every assignment using
 * that action; the ATTR_ADMIN check in AttributeDef.store() came after the commit.
 * <p>
 * Setup: top:actionTestDef has the custom action dangerAction, and top:actionTestAttr is
 * assigned to top:protectedGroup with that action.  SUBJ0 has ATTR_VIEW, ATTR_READ and
 * ATTR_UPDATE on the definition but not ATTR_ADMIN; SUBJ1 has ATTR_ADMIN.
 * </p>
 * Before the fix the denied calls succeed, so the three testXxxDenied tests fail.  The create
 * tests guard the Stem.addChildAttributeDef change (the default "assign" action must still be
 * added when the creator is not, or not yet, ATTR_ADMIN).
 */
public class AttributeDefActionAuthorizationTest extends GrouperTest {

  /**
   * run one test
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefActionAuthorizationTest("testRemoveDeniedWithoutAttrAdmin"));
  }

  /**
   *
   */
  public AttributeDefActionAuthorizationTest() {
    super();
  }

  /**
   * @param name
   */
  public AttributeDefActionAuthorizationTest(String name) {
    super(name);
  }

  /** the custom action the attack deletes */
  private static final String DANGER_ACTION = "dangerAction";

  /** root session made in setUp */
  private GrouperSession rootSession;

  /** the top folder */
  private Stem top;

  /** the definition whose actions are protected */
  private AttributeDef attributeDef;

  /** a name in the definition, assigned to the group with dangerAction */
  private AttributeDefName attributeDefName;

  /** the group that has the assignment */
  private Group protectedGroup;

  /**
   * make the definition, the assignment and the privileges as root
   */
  @Override
  public void setUp() {
    super.setUp();
    this.rootSession = GrouperSession.startRootSession();
    this.top = new StemSave(this.rootSession).assignName("top").save();

    this.attributeDef = new AttributeDefSave(this.rootSession).assignName("top:actionTestDef")
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true).save();
    this.attributeDef.getAttributeDefActionDelegate().configureActionList("assign," + DANGER_ACTION);
    this.attributeDefName = new AttributeDefNameSave(this.rootSession, this.attributeDef)
        .assignName("top:actionTestAttr").assignDisplayExtension("actionTestAttr").save();

    this.protectedGroup = new GroupSave(this.rootSession).assignName("top:protectedGroup").save();
    this.protectedGroup.getAttributeDelegate().assignAttribute(DANGER_ACTION, this.attributeDefName);

    // SUBJ0 can see, read and assign the definition, but is not ATTR_ADMIN
    this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_VIEW, false);
    this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);

    // SUBJ1 is ATTR_ADMIN
    this.attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ1, AttributeDefPrivilege.ATTR_ADMIN, false);

    // sanity check of the setup
    assertFalse(this.attributeDef.getPrivilegeDelegate().canAttrAdmin(SubjectTestHelper.SUBJ0));
    assertEquals(actions("assign", DANGER_ACTION), currentActions());
    assertTrue(groupStillHasAssignment());
  }

  /**
   * stop sessions
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.rootSession);
    super.tearDown();
  }

  // ---------------------------------------------------------------------------------------
  // helpers
  // ---------------------------------------------------------------------------------------

  /**
   * run the handler in a session of the subject, which is the static session the delegate
   * checks while the handler runs.  The root session from setUp is the static session again
   * afterwards, so the root helpers below keep working.  Runtime exceptions (such as
   * InsufficientPrivilegeException) are thrown as is.
   * @param subject
   * @param handler
   * @return what the handler returns
   */
  private static Object as(Subject subject, GrouperSessionHandler handler) {
    GrouperSession session = GrouperSession.start(subject, false);
    try {
      return GrouperSession.callbackGrouperSession(session, handler);
    } finally {
      GrouperSession.stopQuietly(session);
    }
  }

  /**
   * a set of action names
   * @param names
   * @return the set
   */
  private static Set<String> actions(String... names) {
    return new HashSet<String>(Arrays.asList(names));
  }

  /**
   * the actions of the definition, read fresh from the database as root
   * @return the action names
   */
  private Set<String> currentActions() {
    return actionsOf(this.attributeDef.getId());
  }

  /**
   * the actions of a definition, read fresh from the database as root
   * @param attributeDefId
   * @return the action names
   */
  private static Set<String> actionsOf(final String attributeDefId) {
    AttributeDef fresh = (AttributeDef)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        return AttributeDefFinder.findById(attributeDefId, true);
      }
    });
    // use a new delegate: findById can return the same AttributeDef instance from the flash
    // cache (keyed by subject), and its delegate caches the action set from an earlier read,
    // so changes made through another subject's instance would not be seen
    return new HashSet<String>(new AttributeDefActionDelegate(fresh).allowedActionStrings());
  }

  /**
   * whether the group still has the attribute assigned with dangerAction, checked as root
   * @return true if the assignment is still there
   */
  private boolean groupStillHasAssignment() {
    return (Boolean)GrouperSession.internal_callbackRootGrouperSession(new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        return AttributeDefActionAuthorizationTest.this.protectedGroup.getAttributeDelegate()
            .hasAttribute(DANGER_ACTION, AttributeDefActionAuthorizationTest.this.attributeDefName);
      }
    });
  }

  /**
   * the action delegate of the definition as the subject of the current session finds it
   * @return the delegate
   */
  private AttributeDefActionDelegate delegateAsCurrentSubject() {
    return AttributeDefFinder.findById(this.attributeDef.getId(), true).getAttributeDefActionDelegate();
  }

  /**
   * run the handler as the subject and assert it is denied with InsufficientPrivilegeException
   * @param subject
   * @param message
   * @param handler
   */
  private static void assertDenied(Subject subject, String message, GrouperSessionHandler handler) {
    try {
      as(subject, handler);
      fail(message);
    } catch (InsufficientPrivilegeException ipe) {
      // expected
    }
  }

  // ---------------------------------------------------------------------------------------
  // denied without ATTR_ADMIN
  // ---------------------------------------------------------------------------------------

  /**
   * ATTR_VIEW, ATTR_READ and ATTR_UPDATE are not enough to add an action
   */
  public void testAddDeniedWithoutAttrAdmin() {
    assertDenied(SubjectTestHelper.SUBJ0, "SUBJ0 is not ATTR_ADMIN, adding an action should be denied",
        new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        return delegateAsCurrentSubject().addAction("newAction");
      }
    });
    assertEquals(actions("assign", DANGER_ACTION), currentActions());
  }

  /**
   * the reproduced attack: removing dangerAction must be denied, and the assignment that uses
   * it must still be there
   */
  public void testRemoveDeniedWithoutAttrAdmin() {
    assertDenied(SubjectTestHelper.SUBJ0, "SUBJ0 is not ATTR_ADMIN, removing an action should be denied",
        new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        delegateAsCurrentSubject().removeAction(DANGER_ACTION);
        return null;
      }
    });
    assertEquals(actions("assign", DANGER_ACTION), currentActions());
    assertTrue("the assignment using the action must not be deleted", groupStillHasAssignment());
  }

  /**
   * replacing all actions (which would delete assign and dangerAction) must be denied, also
   * through configureActionList, the method every change funnels into
   */
  public void testReplaceDeniedWithoutAttrAdmin() {
    assertDenied(SubjectTestHelper.SUBJ0, "SUBJ0 is not ATTR_ADMIN, replacing the actions should be denied",
        new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        delegateAsCurrentSubject().replaceAllActionsWith(Arrays.asList("dummy"));
        return null;
      }
    });
    assertEquals(actions("assign", DANGER_ACTION), currentActions());
    assertTrue(groupStillHasAssignment());

    assertDenied(SubjectTestHelper.SUBJ0, "SUBJ0 is not ATTR_ADMIN, configuring the actions should be denied",
        new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        return delegateAsCurrentSubject().configureActionList("assign");
      }
    });
    assertEquals(actions("assign", DANGER_ACTION), currentActions());
    assertTrue(groupStillHasAssignment());
  }

  /**
   * a call that changes nothing still works without ATTR_ADMIN (as before the fix)
   */
  public void testNoChangeAllowedWithoutAttrAdmin() {
    as(SubjectTestHelper.SUBJ0, new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        AttributeDefActionDelegate delegate = delegateAsCurrentSubject();
        delegate.removeAction("notAnAction");
        assertEquals(0, delegate.configureActionList("assign," + DANGER_ACTION));
        assertEquals(DANGER_ACTION, delegate.addAction(DANGER_ACTION).getName());
        return null;
      }
    });
    assertEquals(actions("assign", DANGER_ACTION), currentActions());
  }

  // ---------------------------------------------------------------------------------------
  // allowed with ATTR_ADMIN, wheel
  // ---------------------------------------------------------------------------------------

  /**
   * an ATTR_ADMIN can add, remove and replace actions
   */
  public void testAllowedWithAttrAdmin() {
    as(SubjectTestHelper.SUBJ1, new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        delegateAsCurrentSubject().addAction("newAction");
        delegateAsCurrentSubject().removeAction(DANGER_ACTION);
        return null;
      }
    });
    assertEquals(actions("assign", "newAction"), currentActions());
    assertFalse("removing the action deletes its assignments", groupStillHasAssignment());

    as(SubjectTestHelper.SUBJ1, new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        delegateAsCurrentSubject().replaceAllActionsWith(Arrays.asList("assign", "x"));
        return null;
      }
    });
    assertEquals(actions("assign", "x"), currentActions());
  }

  /**
   * a wheel member with no privilege on the definition can change actions
   */
  public void testAllowedForWheel() {
    Group wheel = new GroupSave(this.rootSession).assignName("etc:wheelGroupForTest").save();
    wheel.addMember(SubjectTestHelper.SUBJ2, false);
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.use", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.wheel.group", wheel.getName());

    as(SubjectTestHelper.SUBJ2, new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        return delegateAsCurrentSubject().addAction("wheelAction");
      }
    });
    assertEquals(actions("assign", DANGER_ACTION, "wheelAction"), currentActions());
  }

  // ---------------------------------------------------------------------------------------
  // creating an attribute definition still gets the default "assign" action
  // ---------------------------------------------------------------------------------------

  /**
   * a subject with CREATE on the folder can still create a definition, which gets "assign",
   * and the creator is ATTR_ADMIN of it
   */
  public void testCreateWithCreatePrivilege() {
    this.top.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE, false);
    final Stem theTop = this.top;
    AttributeDef newDef = (AttributeDef)as(SubjectTestHelper.SUBJ0, new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        return theTop.addChildAttributeDef("subj0Def", AttributeDefType.attr);
      }
    });
    assertNotNull(newDef);
    assertEquals(actions("assign"), actionsOf(newDef.getId()));
    assertTrue(newDef.getPrivilegeDelegate().canAttrAdmin(SubjectTestHelper.SUBJ0));
  }

  /**
   * a creator whose ATTR_ADMIN comes from an inherited privilege rule (so no direct grant is
   * made when privileges.assignAdminToInheritedAdminsOnCreate is false) can still create a
   * definition with the default "assign" action
   */
  public void testCreateWithInheritedAttrAdmin() {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("privileges.assignAdminToInheritedAdminsOnCreate", "false");

    Group admins = new GroupSave(this.rootSession).assignName("top:attrDefAdmins").save();
    admins.addMember(SubjectTestHelper.SUBJ3, false);
    final Stem inheritStem = new StemSave(this.rootSession).assignName("top:inherit").save();
    inheritStem.grantPriv(SubjectTestHelper.SUBJ3, NamingPrivilege.CREATE, false);
    Set<Privilege> attrAdmin = new HashSet<Privilege>();
    attrAdmin.add(AttributeDefPrivilege.ATTR_ADMIN);
    RuleApi.inheritAttributeDefPrivileges(this.rootSession.getSubject(), inheritStem, Scope.SUB,
        admins.toSubject(), attrAdmin);

    AttributeDef newDef = (AttributeDef)as(SubjectTestHelper.SUBJ3, new GrouperSessionHandler() {
      public Object callback(GrouperSession grouperSession) {
        return inheritStem.addChildAttributeDef("subj3Def", AttributeDefType.attr);
      }
    });
    assertNotNull(newDef);
    assertEquals(actions("assign"), actionsOf(newDef.getId()));
  }

}
