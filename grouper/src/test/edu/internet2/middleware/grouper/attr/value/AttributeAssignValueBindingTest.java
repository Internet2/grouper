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
package edu.internet2.middleware.grouper.attr.value;

import java.util.LinkedHashSet;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import junit.textui.TestRunner;

/**
 * SECURITY tests for AttributeAssignValueBinding and the binding check in
 * AttributeAssignValueDelegate.internal_deleteValues.
 *
 * The UI value edit / delete handlers and WS assignAttributes remove_value authorize one attribute
 * assignment, then load a value by an id from the request and edit or delete it.  Nothing checked that
 * the value belonged to the authorized assignment, so a caller who could update their own assignment
 * could read, overwrite or delete a value of any other assignment whose value id they knew.  WS
 * remove_value loads each value with AttributeAssignValueDAO.findById and passes it to
 * attributeAssign.getValueDelegate().deleteValues(), which is exactly what these tests do.
 */
public class AttributeAssignValueBindingTest extends GrouperTest {

  /**
   *
   */
  public AttributeAssignValueBindingTest() {
    super();
  }

  /**
   * @param name
   */
  public AttributeAssignValueBindingTest(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignValueBindingTest("testForeignValueIdRejected"));
  }

  /** the caller's own assignment */
  private AttributeAssign attackerAssign;

  /** the caller's own value */
  private AttributeAssignValue attackerValue;

  /** an assignment the caller can read but not update */
  private AttributeAssign protectedAssign;

  /** a value on the protected assignment */
  private AttributeAssignValue protectedValue;

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  public void setUp() {
    super.setUp();

    // no default grants to GrouperAll, so the test controls every privilege
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
  }

  /**
   * As root: two groups with a single valued string attribute each.  SUBJ0 may update the attacker
   * side, and may only read the protected side (the realistic way to learn the protected value id,
   * e.g. from WS getAttributeAssignments).
   * @param rootSession root session
   */
  private void setupAssignments(GrouperSession rootSession) {

    Group attackerGroup = new GroupSave(rootSession).assignName("test:attrIdor:attackerGroup")
        .assignCreateParentStemsIfNotExist(true).save();
    Group protectedGroup = new GroupSave(rootSession).assignName("test:attrIdor:protectedGroup")
        .assignCreateParentStemsIfNotExist(true).save();

    AttributeDef attackerDef = new AttributeDefSave(rootSession).assignName("test:attrIdor:attackerDef")
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true)
        .assignValueType(AttributeDefValueType.string).save();
    AttributeDefName attackerName = new AttributeDefNameSave(rootSession, attackerDef)
        .assignName("test:attrIdor:attackerAttribute").save();

    AttributeDef protectedDef = new AttributeDefSave(rootSession).assignName("test:attrIdor:protectedDef")
        .assignAttributeDefType(AttributeDefType.attr).assignToGroup(true)
        .assignValueType(AttributeDefValueType.string).save();
    AttributeDefName protectedName = new AttributeDefNameSave(rootSession, protectedDef)
        .assignName("test:attrIdor:protectedAttribute").save();

    this.attackerAssign = attackerGroup.getAttributeDelegate().assignAttribute(attackerName).getAttributeAssign();
    this.attackerValue = this.attackerAssign.getValueDelegate().assignValue("attacker-original").getAttributeAssignValue();
    this.protectedAssign = protectedGroup.getAttributeDelegate().assignAttribute(protectedName).getAttributeAssign();
    this.protectedValue = this.protectedAssign.getValueDelegate().assignValue("protected-original").getAttributeAssignValue();

    // SUBJ0: update (and read) on its own side
    attackerGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    attackerGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ, false);
    attackerGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_UPDATE, false);
    attackerDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
    attackerDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);

    // SUBJ0: read only on the protected side
    protectedGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.VIEW, false);
    protectedGroup.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ, false);
    protectedDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);
  }

  /**
   * the protected value, read again from the database (null if deleted)
   * @return the value string or null
   */
  private String protectedValueInDb() {
    AttributeAssignValue value = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(this.protectedValue.getId(), false);
    return value == null ? null : value.getValueString();
  }

  /**
   * A value of another assignment is never returned for, edited through, or deleted through the
   * caller's own assignment.  Before the fix, deleteValue / deleteValues deleted the protected value
   * (the WS remove_value exploit), and the UI handlers loaded and rendered or saved it.
   */
  public void testForeignValueIdRejected() {

    GrouperSession rootSession = GrouperSession.startRootSession();
    setupAssignments(rootSession);

    // ---- binding helper, pure checks
    assertTrue(AttributeAssignValueBinding.isBound(this.attackerAssign, this.attackerValue));
    assertFalse(AttributeAssignValueBinding.isBound(this.attackerAssign, this.protectedValue));
    assertFalse(AttributeAssignValueBinding.isBound(null, this.attackerValue));
    assertFalse(AttributeAssignValueBinding.isBound(this.attackerAssign, null));

    GrouperSession.stopQuietly(rootSession);
    GrouperSession subj0Session = GrouperSession.start(SubjectTestHelper.SUBJ0);
    try {

      // precondition: SUBJ0 really cannot update the protected assignment
      try {
        this.protectedAssign.getValueDelegate().deleteValue(this.protectedValue);
        fail("SUBJ0 must not be able to update the protected assignment");
      } catch (InsufficientPrivilegeException ipe) {
        // good
      }
      assertEquals("protected-original", protectedValueInDb());

      // ---- UI lookup (edit screen, edit submit, delete): foreign and missing ids look the same
      assertNull(AttributeAssignValueBinding.retrieveBoundValue(this.attackerAssign, this.protectedValue.getId()));
      assertNull(AttributeAssignValueBinding.retrieveBoundValue(this.attackerAssign, "doesNotExist"));
      assertNull(AttributeAssignValueBinding.retrieveBoundValue(this.attackerAssign, ""));
      AttributeAssignValue own = AttributeAssignValueBinding.retrieveBoundValue(this.attackerAssign, this.attackerValue.getId());
      assertNotNull(own);
      assertEquals("attacker-original", own.getValueString());

      // ---- WS remove_value: value loaded by id with the DAO, deleted through the caller's own assignment
      AttributeAssignValue foreignByDao = GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(this.protectedValue.getId(), true);
      try {
        this.attackerAssign.getValueDelegate().deleteValue(foreignByDao);
        fail("a value of another assignment must not be deleted through the caller's assignment");
      } catch (InsufficientPrivilegeException ipe) {
        // good, and the message must not leak the value
        assertFalse(ipe.getMessage(), ipe.getMessage().contains("protected-original"));
        assertFalse(ipe.getMessage(), ipe.getMessage().contains(this.protectedValue.getId()));
      }
      assertEquals("protected-original", protectedValueInDb());

      // ---- mixed request (own value first): fails as a whole, deletes neither
      Set<AttributeAssignValue> mixed = new LinkedHashSet<AttributeAssignValue>();
      mixed.add(GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(this.attackerValue.getId(), true));
      mixed.add(foreignByDao);
      try {
        this.attackerAssign.getValueDelegate().deleteValues(mixed);
        fail("a mixed delete must be rejected");
      } catch (InsufficientPrivilegeException ipe) {
        // good
      }
      assertEquals("protected-original", protectedValueInDb());
      assertNotNull(GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(this.attackerValue.getId(), false));

      // ---- positive: the caller can still delete its own value by id
      this.attackerAssign.getValueDelegate().deleteValue(
          GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(this.attackerValue.getId(), true));
      assertNull(GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(this.attackerValue.getId(), false));

    } finally {
      GrouperSession.stopQuietly(subj0Session);
    }

    // ---- internal callers that find values through the delegate still work (no binding false positive)
    rootSession = GrouperSession.startRootSession();
    try {
      this.protectedAssign.getValueDelegate().deleteValueString("protected-original");
      assertNull(protectedValueInDb());
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }

  /**
   * The same rule for permission limit values (UiV2GroupPermission / UiV2SubjectPermission
   * limitValueEdit and limitValueDelete): a value of one limit cannot be reached through another.
   */
  public void testForeignLimitValueIdRejected() {

    GrouperSession rootSession = GrouperSession.startRootSession();

    Group role = new GroupSave(rootSession).assignName("test:attrIdor:role")
        .assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    AttributeDef permissionDef = new AttributeDefSave(rootSession).assignName("test:attrIdor:permissionDef")
        .assignAttributeDefType(AttributeDefType.perm).assignToGroup(true).assignToEffMembership(true).save();
    AttributeDefName permissionA = new AttributeDefNameSave(rootSession, permissionDef).assignName("test:attrIdor:permissionA").save();
    AttributeDefName permissionB = new AttributeDefNameSave(rootSession, permissionDef).assignName("test:attrIdor:permissionB").save();

    AttributeAssign permissionAssignA = role.getPermissionRoleDelegate().assignRolePermission(permissionA, PermissionAllowed.ALLOWED).getAttributeAssign();
    AttributeAssign permissionAssignB = role.getPermissionRoleDelegate().assignRolePermission(permissionB, PermissionAllowed.ALLOWED).getAttributeAssign();

    AttributeDefName limitName = PermissionLimitUtils.limitElAttributeDefName();
    permissionAssignA.getAttributeValueDelegate().assignValue(limitName.getName(), "amount < 50000");
    permissionAssignB.getAttributeValueDelegate().assignValue(limitName.getName(), "amount < 10");

    AttributeAssign limitAssignA = permissionAssignA.getAttributeDelegate().retrieveAssignments(limitName).iterator().next();
    AttributeAssign limitAssignB = permissionAssignB.getAttributeDelegate().retrieveAssignments(limitName).iterator().next();
    AttributeAssignValue limitValueA = limitAssignA.getValueDelegate().retrieveValues().iterator().next();
    AttributeAssignValue limitValueB = limitAssignB.getValueDelegate().retrieveValues().iterator().next();

    try {
      assertNull(AttributeAssignValueBinding.retrieveBoundValue(limitAssignA, limitValueB.getId()));
      assertNotNull(AttributeAssignValueBinding.retrieveBoundValue(limitAssignA, limitValueA.getId()));

      // even root cannot delete B's limit value through A's limit (checkSecurity does not matter)
      try {
        limitAssignA.getValueDelegate().deleteValue(limitValueB);
        fail("a value of another limit must not be deleted through this limit");
      } catch (InsufficientPrivilegeException ipe) {
        // good
      }
      assertNotNull(GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(limitValueB.getId(), false));
      assertNotNull(GrouperDAOFactory.getFactory().getAttributeAssignValue().findById(limitValueA.getId(), false));
    } finally {
      GrouperSession.stopQuietly(rootSession);
    }
  }
}
