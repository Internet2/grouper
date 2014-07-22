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
/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package edu.internet2.middleware.grouper.group;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Set;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupCopy;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupMove;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.entity.Entity;
import edu.internet2.middleware.grouper.entity.EntitySave;
import edu.internet2.middleware.grouper.entity.EntityUtils;
import edu.internet2.middleware.grouper.exception.AttributeAssignNotFoundException;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyAlreadyExistsException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GroupSetNotFoundException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.membership.MembershipType;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.permissions.role.RoleSet;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * Test {@link Group}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_Group.java,v 1.11 2009-12-07 07:31:09 mchyzer Exp $
 * @since   1.2.1
 */
public class Test_api_Group extends GrouperTest {


  /**
   * @param name
   */
  public Test_api_Group(String name) {
    super(name);
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new Test_api_Group("test_copy_minimum_as_nonadmin_insufficient_privileges"));
  }
  
  private Group           top_group, child_group;
  private GrouperSession  s;
  private Stem            child, root, top;
  private GroupType       type1, type2, type3;
  private Field           type1list1, type1list2, type2list1, type2list2, type3list1, type3list2;
  private AttributeDefName type1attr1, type1attr2, type2attr1, type2attr2, type3attr1, type3attr2;



  public void setUp() {
    super.setUp();
    try {
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "true");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.groupAttrRead", "true");
      
      this.s            = GrouperSession.start( SubjectFinder.findRootSubject() );
      this.root         = StemFinder.findRootStem(this.s);
      this.top          = this.root.addChildStem("top", "top display name");
      this.top_group    = this.top.addChildGroup("top group", "top group");
      this.child        = this.top.addChildStem("child", "child");
      this.child_group  = this.child.addChildGroup("child group", "child group display name");
      this.type1        = GroupType.createType(s, "type1");
      this.type2        = GroupType.createType(s, "type2");
      this.type3        = GroupType.createType(s, "type3");
      
      type1list1 = type1.addList(s, "type1list1", AccessPrivilege.READ, AccessPrivilege.ADMIN);
      type1list2 = type1.addList(s, "type1list2", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);
      type1attr1 = type1.addAttribute(s, "type1attr1", true);
      type1attr2 = type1.addAttribute(s, "type1attr2",false);
      type2list1 = type2.addList(s, "type2list1", AccessPrivilege.READ, AccessPrivilege.ADMIN);
      type2list2 = type2.addList(s, "type2list2", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);
      type2attr1 = type2.addAttribute(s, "type2attr1", true);
      type2attr2 = type2.addAttribute(s, "type2attr2", false);
      type3list1 = type3.addList(s, "type3list1", AccessPrivilege.READ, AccessPrivilege.ADMIN);
      type3list2 = type3.addList(s, "type3list2", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);
      type3attr1 = type3.addAttribute(s, "type3attr1", true);
      type3attr2 = type3.addAttribute(s, "type3attr2", false);      
    }
    catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }
  
  /**
   * 
   */
  public void testRemoveRequiredAttributeType() {
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    Stem stem = root.addChildStem("stem", "stem");
    Group group = stem.addChildGroup("group", "group");
    
    group.addType(type1);
    group.setAttribute("type1attr1", "test");
    group.deleteType(type1);

  }
  
  /**
   * @throws Exception
   */
  public void test_group_to_role() throws Exception {
    Stem stem = root.addChildStem("stem", "stem");
    Group group = stem.addChildGroup("group", "group");
    
    Set<RoleSet> roleSets = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(group.getUuid());
    assertEquals(0, roleSets.size());
    
    group.setTypeOfGroup(TypeOfGroup.role);
    group.store();
    
    roleSets = GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(group.getUuid());
    assertEquals(1, roleSets.size());
    RoleSet roleSet = roleSets.iterator().next();
    assertEquals(0, roleSet.getDepth());
    assertEquals(group.getUuid(), roleSet.getThenHasRoleId());
    assertEquals(roleSet.getId(), roleSet.getParentRoleSetId());
    assertEquals("self", roleSet.getTypeDb());
  }
  
  /**
   * @throws Exception
   */
  public void test_role_to_group() throws Exception {
    Stem stem = root.addChildStem("stem", "stem");
    Role role1 = stem.addChildRole("role1", "role1");
    Role role2 = stem.addChildRole("role2", "role2");
    Role role3 = stem.addChildRole("role3", "role3");
    Role role4 = stem.addChildRole("role4", "role4");
    Role role5 = stem.addChildRole("role5", "role5");

    Group group2 = GrouperDAOFactory.getFactory().getGroup().findByName("stem:role2", true);
    Group group3 = GrouperDAOFactory.getFactory().getGroup().findByName("stem:role3", true);
    
    role1.getRoleInheritanceDelegate().addRoleToInheritFromThis(role2);
    role2.getRoleInheritanceDelegate().addRoleToInheritFromThis(role3);
    role3.getRoleInheritanceDelegate().addRoleToInheritFromThis(role4);
    role4.getRoleInheritanceDelegate().addRoleToInheritFromThis(role5);
    
    AttributeDef attributeDefPerm = stem.addChildAttributeDef("attributeDefPerm", AttributeDefType.perm);
    attributeDefPerm.setAssignToGroup(true);
    attributeDefPerm.store();
    
    AttributeDef attributeDefAttr = stem.addChildAttributeDef("attributeDefAttr", AttributeDefType.attr);
    attributeDefAttr.setAssignToGroup(true);
    attributeDefAttr.store();
    
    AttributeDefName attributePerm = stem.addChildAttributeDefName(attributeDefPerm, "attributePerm", "attributePerm");
    AttributeDefName attributeAttr = stem.addChildAttributeDefName(attributeDefAttr, "attributeAttr", "attributeAttr");
    
    group2.getPermissionRoleDelegate().assignRolePermission(attributePerm);
    group2.getAttributeDelegate().assignAttribute(attributeAttr);
    group3.getPermissionRoleDelegate().assignRolePermission(attributePerm);
    group3.getAttributeDelegate().assignAttribute(attributeAttr);
    
    group3.setTypeOfGroup(TypeOfGroup.group);
    group3.store();

    assertNotNull(GrouperDAOFactory.getFactory().getRoleSet().findSelfRoleSet(role1.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getRoleSet().findSelfRoleSet(role2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getRoleSet().findSelfRoleSet(role3.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getRoleSet().findSelfRoleSet(role4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getRoleSet().findSelfRoleSet(role5.getId(), false));
    
    assertNotNull(GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role1.getId(), role2.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role2.getId(), role3.getId(), false));
    assertNull(GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role3.getId(), role4.getId(), false));
    assertNotNull(GrouperDAOFactory.getFactory().getRoleSet().findByIfThenImmediate(role4.getId(), role5.getId(), false));

    assertTrue(group2.getAttributeDelegate().hasAttribute(attributeAttr));
    assertTrue(group2.getAttributeDelegate().hasAttribute(attributePerm));
    assertTrue(group3.getAttributeDelegate().hasAttribute(attributeAttr));
    assertFalse(group3.getAttributeDelegate().hasAttribute(attributePerm));
  }
  
  /**
   * @throws Exception
   */
  public void test_delete_where_group_has_privileges() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    Stem stem = root.addChildStem("stem", "stem");
    Group group = stem.addChildGroup("group", "group");
    
    Group groupToDelete = stem.addChildGroup("groupToDelete", "groupToDelete");
    groupToDelete.grantPriv(b, AccessPrivilege.ADMIN);
    groupToDelete.grantPriv(b, AccessPrivilege.GROUP_ATTR_READ);
    groupToDelete.grantPriv(b, AccessPrivilege.GROUP_ATTR_UPDATE);
    Subject groupToDeleteSubject = groupToDelete.toSubject();
    
    stem.grantPriv(a, NamingPrivilege.CREATE);
    stem.grantPriv(groupToDeleteSubject, NamingPrivilege.CREATE);
    group.grantPriv(a, AccessPrivilege.UPDATE);
    group.grantPriv(groupToDeleteSubject, AccessPrivilege.UPDATE);
    group.grantPriv(groupToDeleteSubject, AccessPrivilege.GROUP_ATTR_READ);
    
    assertTrue(stem.hasCreate(a));
    assertTrue(stem.hasCreate(groupToDeleteSubject));
    assertEquals(1, stem.getStemmers().size());
    assertTrue(group.hasUpdate(a));
    assertTrue(group.hasUpdate(groupToDeleteSubject));
    assertTrue(group.hasGroupAttrRead(groupToDeleteSubject));
    assertEquals(1, group.getAdmins().size());
    
    GrouperSession session = GrouperSession.start(b);
    groupToDelete.delete();
    session.stop();
    
    session = GrouperSession.startRootSession();

    assertTrue(stem.hasCreate(a));
    assertFalse(stem.hasCreate(groupToDeleteSubject));
    assertEquals(1, stem.getStemmers().size());
    assertTrue(group.hasUpdate(a));
    assertFalse(group.hasUpdate(groupToDeleteSubject));
    assertFalse(group.hasGroupAttrUpdate(groupToDeleteSubject));
    assertEquals(1, group.getAdmins().size());
    
    session.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_with_disabled_memberships() throws Exception {
    R r = R.populateRegistry(0, 0, 3);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    
    Stem source = root.addChildStem("source", "source");
    Stem target = root.addChildStem("target", "target");
    Group group1 = source.addChildGroup("group1", "group1");
    Group group2 = source.addChildGroup("group2", "group2");
    
    group1.addMember(a);
    group1.addMember(group2.toSubject());
    group1.grantPriv(b, AccessPrivilege.UPDATE);
    group2.addMember(c);
    
    Timestamp disabledTime = new Timestamp(new Date().getTime() - 10000);
    Timestamp enabledTime = new Timestamp(new Date().getTime() + 10000);
    
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group1.getUuid(), group2.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group1.getUuid(), MemberFinder.findBySubject(r.rs, b, true).getUuid(), FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    ms.setEnabledTime(enabledTime);
    ms.setDisabledTime(disabledTime);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperUtil.sleep(100);
    Date pre = new Date();
    GrouperUtil.sleep(100);
    
    Group newGroup = group1.copy(target);
    
    assertEquals(1, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, Group.getDefaultList(), true).size());
    assertEquals(0, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), true).size());
    
    assertEquals(2, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, Group.getDefaultList(), false).size());
    assertEquals(1, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), false).size());
    

    Membership disabled1 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        newGroup.getUuid(), group2.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, false);
    assertTrue(disabled1.getEnabledDb().equals("F"));
    assertTrue(disabled1.getEnabledTime().getTime() == enabledTime.getTime());
    assertTrue(disabled1.getDisabledTime().getTime() == disabledTime.getTime());
    
    Membership disabled2 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        newGroup.getUuid(), MemberFinder.findBySubject(r.rs, b, true).getUuid(), FieldFinder.find(Field.FIELD_NAME_UPDATERS, true), MembershipType.IMMEDIATE.getTypeString(), true, false);
    assertTrue(disabled2.getEnabledDb().equals("F"));
    assertTrue(disabled2.getEnabledTime().getTime() == enabledTime.getTime());
    assertTrue(disabled2.getDisabledTime().getTime() == disabledTime.getTime());

    Membership enabled1 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        newGroup.getUuid(), MemberFinder.findBySubject(r.rs, a, true).getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, false);
    assertTrue(enabled1.getEnabledDb().equals("T"));
    assertTrue(enabled1.getEnabledTime() == null);
    assertTrue(enabled1.getDisabledTime() == null);
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_with_disabled_memberships2() throws Exception {
    R r = R.populateRegistry(0, 0, 3);
    
    Stem source = root.addChildStem("source", "source");
    Stem target = root.addChildStem("target", "target");
    Group group1 = source.addChildGroup("group1", "group1");
    Group group2 = source.addChildGroup("group2", "group2");
    Group group3 = source.addChildGroup("group3", "group3");
    Group group4 = source.addChildGroup("group4", "group4");
    Group group5 = source.addChildGroup("group5", "group5");
    
    group1.addMember(group2.toSubject());
    group2.addMember(group3.toSubject());
    group3.addMember(group4.toSubject());
    group4.addMember(group5.toSubject());
    
    Membership ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group2.getUuid(), group3.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    ms = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group3.getUuid(), group4.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, true);
    ms.setEnabled(false);
    GrouperDAOFactory.getFactory().getMembership().update(ms);
    
    GrouperUtil.sleep(100);
    Date pre = new Date();
    GrouperUtil.sleep(100);
    
    Group newGroup = group3.copy(target);
    
    assertEquals(0, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, Group.getDefaultList(), true).size());
    assertEquals(3, GrouperDAOFactory.getFactory().getMembership().findAllByCreatedAfter(pre, Group.getDefaultList(), false).size());
   
    Membership disabled1 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group2.getUuid(), newGroup.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, false);
    assertTrue(disabled1.getEnabledDb().equals("F"));
    
    Membership disabled2 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        newGroup.getUuid(), group4.toMember().getUuid(), Group.getDefaultList(), MembershipType.IMMEDIATE.getTypeString(), true, false);
    assertTrue(disabled2.getEnabledDb().equals("F"));
    
    Membership disabled3 = GrouperDAOFactory.getFactory().getMembership().findByGroupOwnerAndMemberAndFieldAndType(
        group1.getUuid(), newGroup.toMember().getUuid(), Group.getDefaultList(), MembershipType.EFFECTIVE.getTypeString(), true, false);
    assertTrue(disabled3.getEnabledDb().equals("F"));
    assertTrue(disabled3.getDepth() == 1);
  }
  
  /**
   * @throws Exception
   */
  public void test_delete_which_causes_membership_add() throws Exception {
    R r = R.populateRegistry(0, 0, 1);
    Subject a = r.getSubject("a");
    
    Group left = top.addChildGroup("left", "left");
    Group right = top.addChildGroup("right", "right");
    
    right.addMember(child_group.toSubject());
    top_group.addCompositeMember(CompositeType.COMPLEMENT, left, right);
    
    child_group.addMember(a);
    left.addMember(a);
    assertTrue(top_group.getMembers().size() == 0);
    
    child_group.delete();
    assertTrue(top_group.getMembers().size() == 1);
  }


  /**
   * @throws InsufficientPrivilegeException 
   * @throws RevokePrivilegeException 
   * @since   1.2.1
   */
  public void test_revokePriv_Privilege_namingPrivilege() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException
  {
    try {
      this.top_group.revokePriv(NamingPrivilege.STEM);
      fail("failed to throw expected SchemaException");
    }
    catch (SchemaException eExpected) {
      assertTrue(true);
    }
  }



  /**
   * @throws InsufficientPrivilegeException 
   * @throws RevokePrivilegeException 
   * @since   1.2.1
   */
  public void test_revokePriv_SubjectAndPrivilege_namingPrivilege() 
    throws  InsufficientPrivilegeException,
            RevokePrivilegeException
  {
    try {
      this.top_group.revokePriv( SubjectFinder.findAllSubject(), NamingPrivilege.STEM );
      fail("failed to throw expected SchemaException");
    }
    catch (SchemaException eExpected) {
      assertTrue(true);
    }
  }
  
  /**
   * @throws Exception
   */
  public void test_move_name_exists() throws Exception {
    top_group.addAlternateName("top:child group");
    top_group.store();
    
    // you shouldn't be able to move a group to a location if another group is using that name
    try {
      child_group.move(top);
      fail("failed to throw Exception");
    } catch (Exception e) {
      assertTrue(true);
    }
    
    top_group.deleteAlternateName("top:child group");
    top_group.store();
    
    // if the same group is using the name though, lets allow it.
    child_group.addAlternateName("top:child group");
    child_group.store();
    child_group.move(top);
  }
  
  
  /**
   * @throws Exception
   */
  public void test_copy_name_exists() throws Exception {
    top_group.addAlternateName("top:child group");
    top_group.store();
    
    Group newGroup = child_group.copy(top);
    
    // verify that the copied group name is top:child group.2
    assertTrue(newGroup.getName().equals("top:child group.2"));
  }
  
  /**
   * @throws Exception
   */
  public void test_move() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");

    child_group.addMember(a);
    child_group.grantPriv(a, AccessPrivilege.UPDATE);
    child_group.move(top);

    child_group = GroupFinder.findByName(s, "top:child group", true);
    assertGroupName(child_group, "top:child group");
    assertGroupDisplayName(child_group, "top display name:child group display name");
    assertGroupHasMember(child_group, a, true);
    assertGroupHasMember(child_group, b, false);
    assertGroupHasUpdate(child_group, a, true);
    assertTrue(child_group.getParentStem().getUuid().equals(top.getUuid()));
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void testGroupMoveAudit() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");

    child_group.addMember(a);
    child_group.grantPriv(a, AccessPrivilege.UPDATE);

    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");

    assertEquals(0, auditCount);
    
    child_group.move(top);
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    assertTrue("durationMicros should exist", auditEntry.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 1: " + auditEntry.getQueryCount(), 1 <= auditEntry.getQueryCount());
  
    assertEquals("Context id's should match", auditEntry.getContextId(), child_group.getContextId());
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move2() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");

    child_group.addMember(a);
    child_group.grantPriv(a, AccessPrivilege.UPDATE);
    new GroupMove(child_group, top).save();
    

    child_group = GroupFinder.findByName(s, "top:child group", true);
    assertGroupName(child_group, "top:child group");
    assertGroupDisplayName(child_group, "top display name:child group display name");
    assertGroupHasMember(child_group, a, true);
    assertGroupHasMember(child_group, b, false);
    assertGroupHasUpdate(child_group, a, true);
    assertTrue(child_group.getParentStem().getUuid().equals(top.getUuid()));
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name gets added
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    top.grantPriv(a, NamingPrivilege.CREATE);
    nrs = GrouperSession.start(a);
    child_group.move(top);
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    child_group = GroupFinder.findByName(s, "top:child group", true);
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move_no_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name does not get added
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    top.grantPriv(a, NamingPrivilege.CREATE);
    nrs = GrouperSession.start(a);
    new GroupMove(child_group, top).assignAlternateName(false).save();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    assertNull(child_group.getAlternateNameDb());
    child_group = GroupFinder.findByName(s, "top:child group", true);
    assertNull(child_group.getAlternateNameDb());
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move_alternate_name_when_alternate_name_already_exists() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name gets added when the group already has an alternate name
    child_group.addAlternateName("test1:test2");
    child_group.store();
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    top.grantPriv(a, NamingPrivilege.CREATE);
    nrs = GrouperSession.start(a);
    child_group.move(top);
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    child_group = GroupFinder.findByName(s, "top:child group", true);
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_move_no_alternate_name_when_alternate_name_already_exists() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name doesn't get replaced
    child_group.addAlternateName("test1:test2");
    child_group.store();
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    top.grantPriv(a, NamingPrivilege.CREATE);
    nrs = GrouperSession.start(a);
    new GroupMove(child_group, top).assignAlternateName(false).save();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    assertTrue(child_group.getAlternateNameDb().equals("test1:test2"));
    child_group = GroupFinder.findByName(s, "top:child group", true);
    assertTrue(child_group.getAlternateNameDb().equals("test1:test2"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name gets added
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    nrs = GrouperSession.start(a);
    child_group.setExtension("child group2");
    child_group.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    child_group = GroupFinder.findByName(s, "top:child:child group2", true);
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_with_duplicate_name() throws Exception {

    Group test1 = top.addChildGroup("test1", "test1");
    Group test2 = top.addChildGroup("test2", "test2");
    
    test1.addAlternateName(top.getName() + ":" + "altname");
    test1.store();
    test2.addAlternateName(top.getName() + ":" + "conflict");
    test2.store();
    
    try {
      // this should fail because "test2" is in use.
      test1.setExtension("test2");
      test1.store();
      fail("failed to throw GroupModifyAlreadyExistsException");
    } catch (GroupModifyAlreadyExistsException e) {
      assertTrue(true);
    }
    
    try {
      // this should fail because "conflict" is in use.
      test1.setExtension("conflict");
      test1.store();
      fail("failed to throw GroupModifyAlreadyExistsException");
    } catch (GroupModifyAlreadyExistsException e) {
      assertTrue(true);
    }    
    
    test1.setExtension("test1a");
    test1.store();
    
    assertTrue(test1.getAlternateNameDb().equals(top.getName() + ":" + "test1"));
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_no_alternate_name() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");

    // verify alternate name does not get added
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    nrs = GrouperSession.start(a);
    child_group.setExtension("child group2", false);
    child_group.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    assertNull(child_group.getAlternateNameDb());
    child_group = GroupFinder.findByName(s, "top:child:child group2", true);
    assertNull(child_group.getAlternateNameDb());
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_alternate_name_when_alternate_name_already_exists() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");
    child_group.addAlternateName("test1:test2");
    child_group.store();

    // verify alternate name gets replaced
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    nrs = GrouperSession.start(a);
    child_group.setExtension("child group2", true);
    child_group.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    child_group = GroupFinder.findByName(s, "top:child:child group2", true);
    assertTrue(child_group.getAlternateNameDb().equals("top:child:child group"));
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_rename_no_alternate_name_when_alternate_name_already_exists() throws Exception {
    R r = R.populateRegistry(0, 0, 2);
    GrouperSession nrs;
    Subject a = r.getSubject("a");
    child_group.addAlternateName("test1:test2");
    child_group.store();

    // verify alternate name does not get replaced
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    nrs = GrouperSession.start(a);
    child_group.setExtension("child group2", false);
    child_group.store();
    nrs.stop();
    nrs = GrouperSession.startRootSession();
    assertTrue(child_group.getAlternateNameDb().equals("test1:test2"));
    child_group = GroupFinder.findByName(s, "top:child:child group2", true);
    assertTrue(child_group.getAlternateNameDb().equals("test1:test2"));
    nrs.stop();
    
    r.rs.stop();
  }

  /**
   * @throws Exception
   */
  public void test_rename_same_name() throws Exception {

    assertTrue(child_group.getAlternateNameDb() == null);

    // verify alternate name does not get added
    child_group.setExtension(child_group.getExtension());
    child_group.store();

    assertTrue(child_group.getAlternateNameDb() == null);
    child_group = GroupFinder.findByName(s, child_group.getName(), true);
    assertTrue(child_group.getAlternateNameDb() == null);

    // add alternate name
    child_group.addAlternateName("test1:test2");
    child_group.store();
    child_group = GroupFinder.findByName(s, child_group.getName(), true);

    // verify alternate name does not get updated
    child_group.setExtension(child_group.getExtension());
    child_group.store();

    assertTrue(child_group.getAlternateNameDb().equals("test1:test2"));
    child_group = GroupFinder.findByName(s, child_group.getName(), true);
    assertTrue(child_group.getAlternateNameDb().equals("test1:test2"));
  } 
  
  /**
   * @throws InsufficientPrivilegeException 
   */
  public void test_move_toRootStem() throws InsufficientPrivilegeException {
    try {
      top_group.move(root);
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
  }
  
  /**
   * @throws InsufficientPrivilegeException 
   */
  public void test_move_toRootStem2() throws InsufficientPrivilegeException {
    try {
      new GroupMove(top_group, root).save();
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
  }

  /**
   * @throws InsufficientPrivilegeException 
   */
  public void test_copy_toRootStem() throws InsufficientPrivilegeException {
    try {
      top_group.copy(root);
      fail("failed to throw GroupAddException");
    } catch (GroupAddException e) {
      assertTrue(true);
    }
  }

  /**
   * @throws Exception
   */
  public void test_move_insufficientPrivileges() throws Exception {
    GrouperSession nrs;
    R r = R.populateRegistry(0, 0, 5);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    Subject d = r.getSubject("d");
    Subject e = r.getSubject("e");
    
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    child_group.grantPriv(b, AccessPrivilege.UPDATE);
    top.grantPriv(b, NamingPrivilege.STEM);
    child_group.grantPriv(c, AccessPrivilege.UPDATE);
    top.grantPriv(c, NamingPrivilege.CREATE);
    child_group.grantPriv(d, AccessPrivilege.ADMIN);
    top.grantPriv(d, NamingPrivilege.STEM);    
    top.grantPriv(d, NamingPrivilege.CREATE);    
    child_group.grantPriv(e, AccessPrivilege.ADMIN);
    top.grantPriv(e, NamingPrivilege.CREATE);   
    
    nrs = GrouperSession.start(a);
    try {
      child_group.move(top);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
    
    nrs = GrouperSession.start(b);
    try {
      child_group.move(top);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
    
    nrs = GrouperSession.start(c);
    try {
      child_group.move(top);
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException ex) {
      assertTrue(true);
    }
    nrs.stop();
    
    nrs = GrouperSession.start(d);
    child_group.move(top);
    assertTrue(true);
    nrs.stop();
    
    nrs = GrouperSession.start(e);
    child_group.move(top);
    assertTrue(true);
    nrs.stop();
        
    r.rs.stop();

  }
  
  /**
   * @throws Exception
   */
  public void test_copy_all() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    group_copy_setup(r, false);
    Group newGroup = child_group.copy(top);
    verify_copy(r, newGroup, true, true, true, true, true);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_should_not_copy_alternate_name() throws Exception {
    child_group.addAlternateName("test1:test2");
    child_group.store();
    Group newGroup = child_group.copy(top);
    
    Group existingGroup = GroupFinder.findByName(s, "top:child:child group", true);
    assertTrue(existingGroup.getAlternateNameDb().equals("test1:test2"));
    
    assertNull(newGroup.getAlternateNameDb());
    newGroup = GroupFinder.findByName(s, "top:child group", true);
    assertNull(newGroup.getAlternateNameDb());
  }
  
  /**
   * test group copy
   * @throws Exception 
   */
  public void testGroupCopyAudit() throws Exception {

    R r = R.populateRegistry(0, 0, 13);
    group_copy_setup(r, false);

    HibernateSession.bySqlStatic().executeSql("delete from grouper_audit_entry");

    int auditCount = HibernateSession.bySqlStatic().select(int.class, 
        "select count(1) from grouper_audit_entry");

    assertEquals(0, auditCount);
    
    Group newGroup = child_group.copy(top);
    
    int newAuditCount = HibernateSession.bySqlStatic().select(int.class, 
      "select count(1) from grouper_audit_entry");
    
    assertEquals("Should have added exactly one audit", auditCount+1, newAuditCount);
    
    AuditEntry auditEntry = HibernateSession.byHqlStatic()
      .createQuery("from AuditEntry").uniqueResult(AuditEntry.class);
    
    assertTrue("contextId should exist", StringUtils.isNotBlank(auditEntry.getContextId()));
    assertTrue("durationMicros should exist", auditEntry.getDurationMicroseconds() > 0);
    assertTrue("query count should exist, and be at least 2: " + auditEntry.getQueryCount(), 2 <= auditEntry.getQueryCount());
  
    assertEquals("Context id's should match", auditEntry.getContextId(), newGroup.getContextId());

    r.rs.stop();
  }


  
  /**
   * @throws Exception
   */
  public void test_copy_all_2() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    group_copy_setup(r, false);
    GroupCopy groupCopy = new GroupCopy(child_group, top);
    Group newGroup = groupCopy.copyPrivilegesOfGroup(true).copyGroupAsPrivilege(true)
        .copyListMembersOfGroup(true).copyListGroupAsMember(true).copyAttributes(true)
        .save();
    verify_copy(r, newGroup, true, true, true, true, true);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_minimum() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    group_copy_setup(r, false);
    GroupCopy groupCopy = new GroupCopy(child_group, top);
    Group newGroup = groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
    .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(false).save();
    verify_copy(r, newGroup, false, false, false, false, false);
    
    r.rs.stop();
  }
  
  /**
   * 
   */
  public void test_copy_role_with_members() {
    Stem stem1 = root.addChildStem("stem1", "stem1");
    Stem stem2 = root.addChildStem("stem2", "stem2");
    Role role = stem1.addChildRole("role", "role");
    role.addMember(SubjectTestHelper.SUBJ0, true);
    
    Group roleCopy = ((Group)role).copy(stem2);
    assertEquals(TypeOfGroup.role, roleCopy.getTypeOfGroup());
    assertTrue(roleCopy.hasMember(SubjectTestHelper.SUBJ0));
    
    assertNotNull(GrouperDAOFactory.getFactory().getRoleSet().findByIfHasRoleId(roleCopy.getUuid()));
  }
  
  /**
   * 
   */
  public void test_copy_entity() {
    Stem stem1 = root.addChildStem("stem1", "stem1");
    Stem stem2 = root.addChildStem("stem2", "stem2");
    Entity entity = new EntitySave(this.s).assignName(stem1.getName() + ":entity").save();
    
    entity.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, true);
    stem1.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    stem2.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    Entity entityCopy = entity.copy(stem1);
    assertNull(entityCopy.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
    entityCopy.delete();
    
    entityCopy = entity.copy(stem2);
    assertEquals(TypeOfGroup.entity, ((Group)entityCopy).getTypeOfGroup());
    assertNull(entityCopy.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));

    // verify a couple of group sets
    assertNotNull(GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(entityCopy.getId(), FieldFinder.find(Field.FIELD_NAME_ADMINS, true).getUuid()));
    
    try {
      GrouperDAOFactory.getFactory().getGroupSet().findSelfGroup(entityCopy.getId(), Group.getDefaultList().getUuid());
      fail("Group set should not exist");
    } catch (GroupSetNotFoundException e) {
      // good
    }
    
    entityCopy.delete();
    
    entity.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "stem1:x:y:z");
    entityCopy = entity.copy(stem2);
    assertEquals("stem2:x:y:z", entityCopy.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
    
    Entity entityCopyCopy = entityCopy.copy(stem2);
    
    // this is null because it was copied in the same stem so it would be a duplicate..
    assertNull(entityCopyCopy.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
  }
  
  /**
   * 
   */
  public void test_move_entity() {
    Stem stem1 = root.addChildStem("stem1", "stem1");
    Stem stem2 = root.addChildStem("stem2", "stem2");
    Stem stem3 = root.addChildStem("stem3", "stem3");
    Entity entity = new EntitySave(this.s).assignName(stem1.getName() + ":entity").save();
    
    entity.grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.ADMIN, true);
    stem2.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    stem3.grantPriv(SubjectTestHelper.SUBJ1, NamingPrivilege.CREATE);
    
    GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    entity.setExtension("entity2");
    entity.store();
    assertNull(entity.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
    
    entity.move(stem2);
    assertNull(entity.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
    
    entity.getAttributeValueDelegate().assignValue(EntityUtils.entitySubjectIdentifierName(), "stem2:x:y:z");
    entity.move(stem3);
    assertEquals("stem3:x:y:z", entity.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
    
    entity.setExtension("entity3");
    entity.store();
    assertEquals("stem3:x:y:z", entity.getAttributeValueDelegate().retrieveValueString(EntityUtils.entitySubjectIdentifierName()));
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_members_only() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    group_copy_setup(r, false);
    GroupCopy groupCopy = new GroupCopy(child_group, top);
    Group newGroup = groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
        .copyListMembersOfGroup(true).copyListGroupAsMember(true).copyAttributes(false)
        .save();    
    verify_copy(r, newGroup, false, false, true, true, false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_privs_only() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    group_copy_setup(r, false);
    GroupCopy groupCopy = new GroupCopy(child_group, top);
    Group newGroup = groupCopy.copyPrivilegesOfGroup(true).copyGroupAsPrivilege(true)
        .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(false)
        .save();
    verify_copy(r, newGroup, true, true, false, false, false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_attrs_only() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    group_copy_setup(r, false);
    GroupCopy groupCopy = new GroupCopy(child_group, top);
    Group newGroup = groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
        .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(true)
        .save();
    verify_copy(r, newGroup, false, false, false, false, true);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_group_exists() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    top.addChildGroup("child group", "child group");

    group_copy_setup(r, false);
    Group newGroup = child_group.copy(top);
    assertTrue(newGroup.getTypes().size() == 2);
    assertGroupName(newGroup, "top:child group.2");
    assertGroupDisplayName(newGroup, "top display name:child group display name");  
    
    Group newGroup2 = child_group.copy(top);
    assertTrue(newGroup2.getTypes().size() == 2);
    assertGroupName(newGroup2, "top:child group.3");
    assertGroupDisplayName(newGroup2, "top display name:child group display name");  
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_group_is_factor() throws Exception {
    R r = R.populateRegistry(0, 0, 13);

    group_copy_setup(r, false);
    Group composite = top.addChildGroup("composite", "composite");
    Group right = top.addChildGroup("right", "right");
    composite.addCompositeMember(CompositeType.UNION, child_group, right);
    
    Group newGroup = child_group.copy(top);
    verify_copy(r, newGroup, true, true, true, true, true);
    
    assertTrue(composite.getComposite(true).getRightGroup().getName().equals("top:right"));
    assertTrue(composite.getComposite(true).getLeftGroup().getName().equals("top:child:child group"));
    
    assertTrue(newGroup.hasComposite() == false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_group_is_composite() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");

    Group left = top.addChildGroup("left", "left");
    Group right = top.addChildGroup("right", "right");
    left.addMember(a);
    right.addMember(b);
    child_group.addCompositeMember(CompositeType.UNION, left, right);
    group_copy_setup(r, true);
    
    Group newGroup = child_group.copy(top);
    verify_copy(r, newGroup, true, true, true, true, true);
    
    assertTrue(child_group.getComposite(true).getRightGroup().getName().equals("top:right"));
    assertTrue(child_group.getComposite(true).getLeftGroup().getName().equals("top:left"));
    assertTrue(newGroup.getComposite(true).getRightGroup().getName().equals("top:right"));
    assertTrue(newGroup.getComposite(true).getLeftGroup().getName().equals("top:left"));
        
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_no_read_priv() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject b = r.getSubject("b");
    GrouperSession nrs;

    group_copy_setup(r, false);
    child_group.revokePriv(AccessPrivilege.READ);
    
    // subject can create in top stem but cannot read child_group
    nrs = GrouperSession.start(b);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
          .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
              false).save();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }
    nrs.stop();
            
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_no_create_priv() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject e = r.getSubject("e");
    GrouperSession nrs;

    group_copy_setup(r, false);

    // subject can read child_group but cannot create group in top stem
    nrs = GrouperSession.start(e);

    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
          .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
              false).save();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }
    
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_minimum_as_nonadmin() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject k = r.getSubject("k");
    GrouperSession nrs;

    group_copy_setup(r, false);

    // subject can read child_group and can create in top stem
    nrs = GrouperSession.start(k);
    GroupCopy groupCopy = new GroupCopy(child_group, top);
    Group newGroup = groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
        .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
            false).save();    
    nrs.stop();
    
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    verify_copy(r, newGroup, false, false, false, false, false);
    nrs.stop();
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_all_as_nonadmin() throws Exception {
    R r = R.populateRegistry(0, 0, 13);
    Subject a = r.getSubject("a");
    GrouperSession nrs;

    top.grantPriv(a, NamingPrivilege.STEM);
    top.grantPriv(a, NamingPrivilege.CREATE);
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    top_group.grantPriv(a, AccessPrivilege.ADMIN);
    
    top_group.grantPriv(child_group.toSubject(), AccessPrivilege.ADMIN);
    top.grantPriv(child_group.toSubject(), NamingPrivilege.STEM);
    top_group.addMember(child_group.toSubject());
    
    child_group.addType(type1);
    child_group.setAttribute("type1attr2", "test");
    child_group.addMember(a, type1list1);
    child_group.addMember(a, type1list2);
    
    nrs = GrouperSession.start(a);
    GroupCopy groupCopy = new GroupCopy(child_group, top);
    groupCopy.copyPrivilegesOfGroup(true).copyGroupAsPrivilege(true)
        .copyListMembersOfGroup(true).copyListGroupAsMember(true).copyAttributes(true)
        .save();
    nrs.stop();
    
    r.rs.stop();
  }
  
  
  /**
   * @throws Exception
   */
  public void test_copy_minimum_as_nonadmin_insufficient_privileges() throws Exception {
    R r = R.populateRegistry(0, 0, 1);
    Subject a = r.getSubject("a");
    GrouperSession nrs;

    top.grantPriv(a, NamingPrivilege.CREATE);
    
    nrs = GrouperSession.start(a);
    
    // subject can read child_group and can create in top stem, but cannot read privileges of group.
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(true).copyGroupAsPrivilege(false)
          .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
              false).save();
      fail("failed to throw exception");
    } catch (Exception eExpected) {
      if (eExpected.getCause() instanceof InsufficientPrivilegeException) {
        assertTrue(true);
      } else {
        fail("failed to throw exception whose cause is InsufficientPrivilegeException");
      }
    }
    
    // subject can read child_group and can create in top stem, but cannot read privileges of top_group.
    nrs.stop();
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.grantPriv(child_group.toSubject(), AccessPrivilege.ADMIN);
    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(true)
          .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
              false).save();
      fail("failed to throw exception");
    } catch (Exception eExpected) {
      if (eExpected.getCause() instanceof InsufficientPrivilegeException) {
        assertTrue(true);
      } else {
        fail("failed to throw exception whose cause is InsufficientPrivilegeException");
      }
    }
    
    // subject can read child_group and can create in top stem, but cannot write privileges to top_group.
    nrs.stop();
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    Field admins = FieldFinder.find(Field.FIELD_NAME_ADMINS, true);
    admins.setReadPrivilege(AccessPrivilege.VIEW);
    GrouperDAOFactory.getFactory().getField().createOrUpdate(admins);
    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(true)
          .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
              false).save();
      fail("failed to throw exception");
    } catch (Exception eExpected) {
      if (eExpected.getCause() instanceof InsufficientPrivilegeException) {
        assertTrue(true);
      } else {
        fail("failed to throw exception whose cause is InsufficientPrivilegeException");
      }
    }
    
    // subject can read child_group and can create in top stem, but cannot read privileges of top.
    nrs.stop();
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    admins.setReadPrivilege(AccessPrivilege.ADMIN);
    GrouperDAOFactory.getFactory().getField().createOrUpdate(admins);
    top_group.revokePriv(child_group.toSubject(), AccessPrivilege.ADMIN);
    top.grantPriv(child_group.toSubject(), NamingPrivilege.STEM);
    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(true)
          .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
              false).save();
      fail("failed to throw exception");
    } catch (Exception eExpected) {
      if (eExpected.getCause() instanceof InsufficientPrivilegeException) {
        assertTrue(true);
      } else {
        fail("failed to throw exception whose cause is InsufficientPrivilegeException");
      }
    }
    
    // subject can read child_group and can create in top stem, but cannot write privileges to top.
    nrs.stop();
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    Field stemmers = FieldFinder.find(Field.FIELD_NAME_STEMMERS, true);
    stemmers.setReadPrivilege(NamingPrivilege.CREATE);
    GrouperDAOFactory.getFactory().getField().createOrUpdate(stemmers);
    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(true)
          .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
              false).save();
      fail("failed to throw exception");
    } catch (Exception eExpected) {
      if (eExpected.getCause() instanceof InsufficientPrivilegeException) {
        assertTrue(true);
      } else {
        fail("failed to throw exception whose cause is InsufficientPrivilegeException");
      }
    }
    
    // subject can read child_group and can create in top stem, but cannot read custom list members of group.
    nrs.stop();
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    stemmers.setReadPrivilege(NamingPrivilege.STEM);
    GrouperDAOFactory.getFactory().getField().createOrUpdate(stemmers);
    top.revokePriv(child_group.toSubject(), NamingPrivilege.STEM);
    child_group.addType(type1);
    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
          .copyListMembersOfGroup(true).copyListGroupAsMember(false).copyAttributes(
              false).save();
      fail("failed to throw exception");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }
    
    // subject can read child_group and can create in top stem, but cannot read default list members of group.
    nrs.stop();
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    child_group.deleteType(type1);
    Field members = FieldFinder.find("members", true);
    members.setReadPrivilege(AccessPrivilege.ADMIN);
    GrouperDAOFactory.getFactory().getField().createOrUpdate(members);
    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
          .copyListMembersOfGroup(true).copyListGroupAsMember(false).copyAttributes(
              false).save();
      fail("failed to throw exception");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }
    
    // subject can read child_group and can create in top stem, but cannot read members of top_group.
    nrs.stop();
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    members.setReadPrivilege(AccessPrivilege.READ);
    GrouperDAOFactory.getFactory().getField().createOrUpdate(members);
    top_group.addMember(child_group.toSubject());
    top_group.revokePriv(AccessPrivilege.READ);
    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
          .copyListMembersOfGroup(false).copyListGroupAsMember(true).copyAttributes(
              false).save();
      fail("failed to throw exception");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }
    
    // subject can read child_group and can create in top stem, but cannot write members to top_group.
    nrs.stop();
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.grantPriv(a, AccessPrivilege.READ);
    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
          .copyListMembersOfGroup(false).copyListGroupAsMember(true).copyAttributes(
              false).save();
      fail("failed to throw exception");
    } catch (InsufficientPrivilegeException eExpected) {
      assertTrue(true);
    }

    // subject can read child_group and can create in top stem, but cannot read all attributes.
    nrs.stop();
    nrs = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.deleteMember(child_group.toSubject());
    child_group.addType(type1);
    child_group.setAttribute("type1attr2", "test");
    type1.internal_getAttributeDefForAttributes().getPrivilegeDelegate().revokePriv(SubjectFinder.findAllSubject(), AttributeDefPrivilege.ATTR_READ, true);

    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
          .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
              true).save();
      fail("failed to throw exception");
    } catch (AttributeAssignNotFoundException eExpected) {
      assertTrue(true);
    }
    
    // this should work now
    GroupCopy groupCopy = new GroupCopy(child_group, top);
    groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
        .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
            false).save();    
    nrs.stop();
    
    r.rs.stop();
  }
  
  private void group_copy_setup(R r, boolean isComposite) throws Exception {
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    Subject d = r.getSubject("d");
    Subject e = r.getSubject("e");
    Subject f = r.getSubject("f");
    Subject g = r.getSubject("g");
    Subject h = r.getSubject("h");
    Subject i = r.getSubject("i");
    Subject j = r.getSubject("j");
    Subject k = r.getSubject("k");
    Subject l = r.getSubject("l");
    Subject m = r.getSubject("m");
    
    child_group.addType(type1);
    child_group.addType(type2);
    
    top_group.addType(type3);
    top_group.addMember(child_group.toSubject());
    top_group.addMember(child_group.toSubject(), type3list1);
    top_group.grantPriv(child_group.toSubject(), AccessPrivilege.UPDATE);

    top.grantPriv(child_group.toSubject(), NamingPrivilege.CREATE);
    top.grantPriv(k, NamingPrivilege.CREATE);

    if (!isComposite) {
      child_group.addMember(a);
      child_group.addMember(b);
    }
    
    child_group.grantPriv(a, AccessPrivilege.ADMIN);
    child_group.grantPriv(c, AccessPrivilege.ADMIN);
    child_group.grantPriv(d, AccessPrivilege.UPDATE);
    child_group.grantPriv(e, AccessPrivilege.OPTIN);
    child_group.grantPriv(f, AccessPrivilege.OPTOUT);
    child_group.grantPriv(g, AccessPrivilege.VIEW);
    child_group.grantPriv(h, AccessPrivilege.READ);
    child_group.addMember(i, type1list1);
    child_group.addMember(j, type1list1);
    child_group.setDescription("description test");
    child_group.setAttribute(type1attr1.getLegacyAttributeName(true), "custom attr value 1");
    child_group.setAttribute(type2attr1.getLegacyAttributeName(true), "custom attr value 2");
    child_group.grantPriv(l, AccessPrivilege.GROUP_ATTR_READ);
    child_group.grantPriv(m, AccessPrivilege.GROUP_ATTR_UPDATE);
    child_group.store();
  }
  

  private void verify_copy(R r, Group newGroup, boolean privilegesOfGroup,
      boolean groupAsPrivilege, boolean listMembersOfGroup,
      boolean listGroupAsMember, boolean attributes) throws Exception {

    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    Subject c = r.getSubject("c");
    Subject d = r.getSubject("d");
    Subject e = r.getSubject("e");
    Subject f = r.getSubject("f");
    Subject g = r.getSubject("g");
    Subject h = r.getSubject("h");
    Subject i = r.getSubject("i");
    Subject j = r.getSubject("j");
    Subject l = r.getSubject("l");
    Subject m = r.getSubject("m");
    
    // let's do some quick checks to make sure the object that gets returned back is correct
    assertTrue(newGroup.getTypes().size() == 2);
    assertGroupName(newGroup, "top:child group");
    assertGroupDisplayName(newGroup, "top display name:child group display name");
    newGroup = null;

    newGroup = GroupFinder.findByName(s, "top:child group", true);

    // name checks
    assertGroupName(newGroup, "top:child group");
    assertGroupDisplayName(newGroup, "top display name:child group display name");
    assertGroupExtension(newGroup, "child group");
    assertGroupDisplayExtension(newGroup, "child group display name");
    
    // description attribute check
    assertTrue(newGroup.getDescription().equals("description test"));
    
    // member checks
    if (listMembersOfGroup) {
      assertGroupHasMember(newGroup, a, true);
      assertGroupHasMember(newGroup, b, true);
      assertTrue(newGroup.getMembers().size() == 2);
    } else {
      assertGroupHasMember(newGroup, a, false);
      assertGroupHasMember(newGroup, b, false);
      assertTrue(newGroup.getMembers().size() == 0);      
    }
    
    // custom list checks
    if (listMembersOfGroup) {
      assertGroupHasMember(newGroup, i, type1list1, true);
      assertGroupHasMember(newGroup, j, type1list1, true);
      assertTrue(newGroup.getMembers(type1list1).size() == 2);
      assertTrue(newGroup.getMembers(type1list2).size() == 0);
      assertTrue(newGroup.getMembers(type2list1).size() == 0);
      assertTrue(newGroup.getMembers(type2list2).size() == 0);
    } else {
      assertGroupHasMember(newGroup, i, type1list1, false);
      assertGroupHasMember(newGroup, j, type1list1, false);
      assertTrue(newGroup.getMembers(type1list1).size() == 0);
      assertTrue(newGroup.getMembers(type1list2).size() == 0);
      assertTrue(newGroup.getMembers(type2list1).size() == 0);
      assertTrue(newGroup.getMembers(type2list2).size() == 0);      
    }
    
    // groups with top:child group as a member
    if (listGroupAsMember) {
      assertGroupHasMember(top_group, newGroup.toSubject(), true);
      assertTrue(top_group.getImmediateMembers().size() == 2);
      assertTrue(top_group.getEffectiveMembers().size() == 2);
      assertTrue(top_group.getEffectiveMemberships().size() == 4);
      assertGroupHasMember(top_group, newGroup.toSubject(), type3list1, true);
      assertTrue(top_group.getImmediateMembers(type3list1).size() == 2);
      assertTrue(top_group.getEffectiveMembers(type3list1).size() == 2);
      assertTrue(top_group.getEffectiveMemberships(type3list1).size() == 4);
      assertTrue(top_group.getImmediateMembers(type3list2).size() == 0);
      assertTrue(top_group.getEffectiveMembers(type3list2).size() == 0);    
    } else {
      assertGroupHasMember(top_group, newGroup.toSubject(), false);
      assertTrue(top_group.getImmediateMembers().size() == 1);
      assertTrue(top_group.getEffectiveMembers().size() == 2);
      assertTrue(top_group.getEffectiveMemberships().size() == 2);
      assertGroupHasMember(top_group, newGroup.toSubject(), type3list1, false);
      assertTrue(top_group.getImmediateMembers(type3list1).size() == 1);
      assertTrue(top_group.getEffectiveMembers(type3list1).size() == 2);
      assertTrue(top_group.getEffectiveMemberships(type3list1).size() == 2);
      assertTrue(top_group.getImmediateMembers(type3list2).size() == 0);
      assertTrue(top_group.getEffectiveMembers(type3list2).size() == 0); 
    }
    
    // privilege checks
    if (privilegesOfGroup) {
      assertGroupHasAdmin(newGroup, a, true);
      assertGroupHasAdmin(newGroup, c, true);
      assertGroupHasAdmin(newGroup, SubjectFinder.findRootSubject(), true);
      assertTrue(newGroup.getAdmins().size() == 3);
      assertGroupHasUpdate(newGroup, d, true);
      assertTrue(newGroup.getUpdaters().size() == 1);
      assertGroupHasOptin(newGroup, e, true);
      assertTrue(newGroup.getOptins().size() == 1);
      assertGroupHasOptout(newGroup, f, true);
      assertTrue(newGroup.getOptouts().size() == 1);
      assertGroupHasView(newGroup, g, true);
      assertGroupHasView(newGroup, SubjectFinder.findAllSubject(), true);
      assertTrue(newGroup.getViewers().size() == 2);
      assertGroupHasRead(newGroup, h, true);
      assertGroupHasRead(newGroup, SubjectFinder.findAllSubject(), true);
      assertTrue(newGroup.getReaders().size() == 2);
      assertGroupHasGroupAttrRead(newGroup, l, true);
      assertTrue(newGroup.getGroupAttrReaders().size() == 2);
      assertGroupHasGroupAttrUpdate(newGroup, m, true);
      assertTrue(newGroup.getGroupAttrUpdaters().size() == 1);
    } else {
      assertGroupHasAdmin(newGroup, a, false);
      assertGroupHasAdmin(newGroup, c, false);
      assertGroupHasAdmin(newGroup, SubjectFinder.findRootSubject(), true);
      assertTrue(newGroup.getAdmins().size() == 1);
      assertGroupHasUpdate(newGroup, d, false);
      assertTrue(newGroup.getUpdaters().size() == 0);
      assertGroupHasOptin(newGroup, e, false);
      assertTrue(newGroup.getOptins().size() == 0);
      assertGroupHasOptout(newGroup, f, false);
      assertTrue(newGroup.getOptouts().size() == 0);
      assertGroupHasView(newGroup, g, true);
      assertGroupHasView(newGroup, SubjectFinder.findAllSubject(), true);
      assertTrue(newGroup.getViewers().size() == 1);
      assertGroupHasRead(newGroup, h, true);
      assertGroupHasRead(newGroup, SubjectFinder.findAllSubject(), true);
      assertTrue(newGroup.getReaders().size() == 1);    
      assertGroupHasGroupAttrRead(newGroup, l, true);
      assertTrue(newGroup.getGroupAttrReaders().size() == 1);
      assertGroupHasGroupAttrUpdate(newGroup, m, false);
      assertTrue(newGroup.getGroupAttrUpdaters().size() == 0);
    }
    
    // groups with top:child group as a privilege
    if (groupAsPrivilege) {
      assertTrue(top_group.hasUpdate(newGroup.toSubject()));
      assertTrue(top_group.getUpdaters().size() == 4);
      assertTrue(top_group.getOptins().size() == 0);
    } else {
      assertTrue(top_group.hasUpdate(newGroup.toSubject()) == false);
      assertTrue(top_group.getUpdaters().size() == 3);
      assertTrue(top_group.getOptins().size() == 0);
    }
    
    // stems with top:child group as a privilege
    if (groupAsPrivilege) {
      assertTrue(top.hasCreate(newGroup.toSubject()));
      assertTrue(child.hasCreate(newGroup.toSubject()) == false);
    } else {
      assertTrue(top.hasCreate(newGroup.toSubject()) == false);
      assertTrue(child.hasCreate(newGroup.toSubject()) == false);
    }
    
    // type checks
    assertTrue(newGroup.getTypes().size() == 2);
    
    // attribute checks
    if (attributes) {
      assertTrue(newGroup.getAttributeValue(type1attr1.getLegacyAttributeName(true), false, false).equals("custom attr value 1"));
      assertTrue(newGroup.getAttributeValue(type1attr2.getLegacyAttributeName(true), false, false).equals(""));
      assertTrue(newGroup.getAttributeValue(type2attr1.getLegacyAttributeName(true), false, false).equals("custom attr value 2"));
      assertTrue(newGroup.getAttributeValue(type2attr2.getLegacyAttributeName(true), false, false).equals(""));
    } else {
      assertTrue(newGroup.getAttributeValue(type1attr1.getLegacyAttributeName(true), false, false).equals(""));
      assertTrue(newGroup.getAttributeValue(type1attr2.getLegacyAttributeName(true), false, false).equals(""));
      assertTrue(newGroup.getAttributeValue(type2attr1.getLegacyAttributeName(true), false, false).equals(""));
      assertTrue(newGroup.getAttributeValue(type2attr2.getLegacyAttributeName(true), false, false).equals(""));
    }
    
    // parent checks
    assertTrue(newGroup.getParentStem().getUuid().equals(top.getUuid()));
  }
  
  /**
   * @throws Exception
   */
  public void test_option_to_disable_last_membership_change() throws Exception {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "true");
    
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    Group owner = top.addChildGroup("owner", "owner");
    Group first = top.addChildGroup("first", "first");
    Group second = top.addChildGroup("second", "second");
    Stem test = top.addChildStem("test", "test");
    
    owner.addCompositeMember(CompositeType.UNION, first, second);
    
    first.addMember(second.toSubject());
    first.grantPriv(second.toSubject(), AccessPrivilege.UPDATE);
    
    second.addMember(a);
    second.addMember(b);
    second.deleteMember(a);
    
    second.grantPriv(a, AccessPrivilege.ADMIN);
    second.grantPriv(b, AccessPrivilege.ADMIN);
    second.grantPriv(b, AccessPrivilege.UPDATE);
    second.revokePriv(AccessPrivilege.UPDATE);
    second.revokePriv(a, AccessPrivilege.ADMIN);

    owner.deleteCompositeMember();
    
    // after all this, the last_membership_change should still be null for all groups
    owner = GroupFinder.findByName(r.rs, "top:owner", true);
    first = GroupFinder.findByName(r.rs, "top:first", true);
    second = GroupFinder.findByName(r.rs, "top:second", true);
    test = StemFinder.findByName(r.rs, "top:test", true);

    assertNull(owner.getLastMembershipChange());
    assertNull(first.getLastMembershipChange());
    assertNull(second.getLastMembershipChange());
    assertNotNull(test.getLastMembershipChange());
    assertNotNull(owner.getLastImmediateMembershipChange());
    assertNotNull(first.getLastImmediateMembershipChange());
    assertNotNull(second.getLastImmediateMembershipChange());
  }
  
  /**
   * @throws Exception
   */
  public void test_option_to_disable_last_imm_membership_change() throws Exception {
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastImmediateMembershipTime", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("stems.updateLastMembershipTime", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.updateLastMembershipTime", "true");
    
    R r = R.populateRegistry(0, 0, 2);
    Subject a = r.getSubject("a");
    Subject b = r.getSubject("b");
    
    Group owner = top.addChildGroup("owner", "owner");
    Group first = top.addChildGroup("first", "first");
    Group second = top.addChildGroup("second", "second");
    Stem test = top.addChildStem("test", "test");
    
    owner.addCompositeMember(CompositeType.UNION, first, second);
    
    first.addMember(second.toSubject());
    first.grantPriv(second.toSubject(), AccessPrivilege.UPDATE);
    
    second.addMember(a);
    second.addMember(b);
    second.deleteMember(a);
    
    second.grantPriv(a, AccessPrivilege.ADMIN);
    second.grantPriv(b, AccessPrivilege.ADMIN);
    second.grantPriv(b, AccessPrivilege.UPDATE);
    second.revokePriv(AccessPrivilege.UPDATE);
    second.revokePriv(a, AccessPrivilege.ADMIN);

    owner.deleteCompositeMember();
    
    // after all this, the last_membership_change should still be null for all groups
    owner = GroupFinder.findByName(r.rs, "top:owner", true);
    first = GroupFinder.findByName(r.rs, "top:first", true);
    second = GroupFinder.findByName(r.rs, "top:second", true);
    test = StemFinder.findByName(r.rs, "top:test", true);

    assertNotNull(owner.getLastMembershipChange());
    assertNotNull(first.getLastMembershipChange());
    assertNotNull(second.getLastMembershipChange());
    assertNotNull(test.getLastMembershipChange());
    assertNull(owner.getLastImmediateMembershipChange());
    assertNull(first.getLastImmediateMembershipChange());
    assertNull(second.getLastImmediateMembershipChange());
  }
  
  /**
   * @throws Exception
   */
  public void test_alternateName() throws Exception {
    assertTrue(top_group.getAlternateNames().size() == 0);

    // add invalid group name
    try {
      top_group.addAlternateName(null);
      top_group.store();
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("");
      top_group.store();
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("top");
      top_group.store();
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("top:top group2:");
      top_group.store();
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("top::top group2");
      top_group.store();
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("top:  :top group2");
      top_group.store();
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    assertTrue(top_group.getAlternateNames().size() == 0);
    
    // add an alternate name and verify it gets stored in the db
    top_group.addAlternateName("top:top group2");
    top_group.store();
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top:top group2"));
    top_group = GroupFinder.findByName(s, "top:top group", true);
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top:top group2"));
    
    // add another alternate name.  it should overwrite the last one.
    top_group.addAlternateName("top:top group3");
    top_group.store();
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top:top group3"));
    top_group = GroupFinder.findByName(s, "top:top group", true);
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top:top group3"));
    
    // try deleting first alternate name
    assertFalse(top_group.deleteAlternateName("top:top group2"));
    top_group.store();
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top:top group3"));
    top_group = GroupFinder.findByName(s, "top:top group", true);
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top:top group3"));
    
    // delete alternate name
    assertTrue(top_group.deleteAlternateName("top:top group3"));
    top_group.store();
    assertTrue(top_group.getAlternateNames().size() == 0);
    top_group = GroupFinder.findByName(s, "top:top group", true);
    assertTrue(top_group.getAlternateNames().size() == 0);
    
    // add an alternate name using a location that doesn't exist.
    top_group.addAlternateName("top2:top3:top group2");
    top_group.store();
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top2:top3:top group2"));
    top_group = GroupFinder.findByName(s, "top:top group", true);
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top2:top3:top group2"));
    
    // delete alternate name
    assertTrue(top_group.deleteAlternateName("top2:top3:top group2"));
    top_group.store();
    assertTrue(top_group.getAlternateNames().size() == 0);
    top_group = GroupFinder.findByName(s, "top:top group", true);
    assertTrue(top_group.getAlternateNames().size() == 0);
    
    // add alternate name again so we can verify that the name cannot be used again for a group.
    top_group.addAlternateName("top:top group2");
    top_group.store();
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top:top group2"));
    top_group = GroupFinder.findByName(s, "top:top group", true);
    assertTrue(top_group.getAlternateNames().size() == 1);
    assertTrue(top_group.getAlternateNames().contains("top:top group2"));
    
    // add alternate name that already exists
    try {
      assertTrue(child_group.getAlternateNames().size() == 0);
      child_group.addAlternateName("top:top group2");
      child_group.store();
      fail("failed to throw GroupModifyAlreadyExistsException");
    } catch (GroupModifyAlreadyExistsException e) {
      assertTrue(true);
    }
    
    // add group name that already exists
    try {
      top.addChildGroup("top group2", "test");
      fail("failed to throw GroupAddException");
    } catch (GroupAddException e) {
      assertTrue(true);
    }
    
    // but we should be able to add a stem with this name
    top.addChildStem("top group2", "test");
    assertTrue(true);
    
    // if we delete the alternate name, we can add the group.
    assertTrue(top_group.deleteAlternateName("top:top group2"));
    top_group.store();
    top.addChildGroup("top group2", "test");
    assertTrue(true);
    
    // now try adding an alternate name where the name is an existing group name
    try {
      top_group.addAlternateName("top:child:child group");
      top_group.store();
      fail("failed to throw GroupModifyAlreadyExistsException");
    } catch (GroupModifyAlreadyExistsException e) {
      assertTrue(true);
    }
    
    // if we delete the group, we can add the alternate name
    child_group.delete();
    top_group.addAlternateName("top:child:child group");
    top_group.store();
    assertTrue(true);
  }
  
  /**
   * @throws Exception
   */
  public void test_alternateNameSecurityCheck() throws Exception {
    GrouperSession session;
    R r = R.populateRegistry(0, 0, 1);
    Subject subjA = r.getSubject("a");

    session = GrouperSession.start(subjA);

    // subjA doesn't have admin access to group
    try {
      top_group.addAlternateName("top:top group2");
      top_group.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.grantPriv(subjA, AccessPrivilege.ADMIN);
    session.stop();
    session = GrouperSession.start(subjA);

    // subjA has the appropriate privileges now
    top_group.addAlternateName("top:top group2");
    top_group.store();
    assertTrue(true);

    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.revokePriv(subjA, AccessPrivilege.ADMIN);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA doesn't have access to delete the alternate name
    try {
      top_group.deleteAlternateName("top:top group2");
      top_group.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    top_group.grantPriv(subjA, AccessPrivilege.ADMIN);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA can delete the alternate name now
    top_group.deleteAlternateName("top:top group2");
    top_group.store();
    assertTrue(true);
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    Stem test1 = root.addChildStem("test1", "test1");
    Stem test2 = test1.addChildStem("test2", "test2");
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA doesn't have create access on test1:test2
    try {
      top_group.addAlternateName("test1:test2:group");
      top_group.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    test2.grantPriv(subjA, NamingPrivilege.CREATE);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA has create access on test1:test2
    top_group.addAlternateName("test1:test2:group");
    top_group.store();
    top_group.deleteAlternateName("test1:test2:group");
    top_group.store();
    assertTrue(true);
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    test2.delete();
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA doesn't have create access on test1
    try {
      top_group.addAlternateName("test1:test2:group");
      top_group.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    test1.grantPriv(subjA, NamingPrivilege.CREATE);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA has create access on test1
    top_group.addAlternateName("test1:test2:group");
    top_group.store();
    top_group.deleteAlternateName("test1:test2:group");
    top_group.store();
    assertTrue(true);
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    test1.delete();
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA doesn't have create access on the root stem
    try {
      top_group.addAlternateName("test1:test2:group");
      top_group.store();
      fail("failed to throw InsufficientPrivilegeException");
    } catch (InsufficientPrivilegeException e) {
      assertTrue(true);
    }
    
    session.stop();
    session = GrouperSession.start(SubjectFinder.findRootSubject());
    root.grantPriv(subjA, NamingPrivilege.CREATE);
    session.stop();
    session = GrouperSession.start(subjA);
    
    // subjA has create access on the root stem
    top_group.addAlternateName("test1:test2:group");
    top_group.store();
    top_group.deleteAlternateName("test1:test2:group");
    top_group.store();
    assertTrue(true);
  }
}

