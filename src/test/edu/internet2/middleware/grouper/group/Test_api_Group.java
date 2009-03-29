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

import org.apache.commons.lang.StringUtils;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.FieldFinder;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupCopy;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupMove;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.audit.AuditEntry;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.R;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;


/**
 * Test {@link Group}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_Group.java,v 1.7 2009-03-29 21:17:21 shilen Exp $
 * @since   1.2.1
 */
public class Test_api_Group extends GrouperTest {


  /**
   * @param name
   */
  public Test_api_Group(String name) {
    super(name);
  }

  public static void main(String[] args) {
    TestRunner.run(new Test_api_Group("test_copy_all_as_nonadmin"));
  }
  
  private Group           top_group, child_group;
  private GrouperSession  s;
  private Stem            child, root, top;
  private GroupType       type1, type2, type3;
  private Field           type1list1, type1list2, type2list1, type2list2, type3list1, type3list2;
  private Field           type1attr1, type1attr2, type2attr1, type2attr2, type3attr1, type3attr2;



  public void setUp() {
    super.setUp();
    try {
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
      type1attr1 = type1.addAttribute(s, "type1attr1", AccessPrivilege.READ, AccessPrivilege.ADMIN, true);
      type1attr2 = type1.addAttribute(s, "type1attr2", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
      type2list1 = type2.addList(s, "type2list1", AccessPrivilege.READ, AccessPrivilege.ADMIN);
      type2list2 = type2.addList(s, "type2list2", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);
      type2attr1 = type2.addAttribute(s, "type2attr1", AccessPrivilege.READ, AccessPrivilege.ADMIN, true);
      type2attr2 = type2.addAttribute(s, "type2attr2", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
      type3list1 = type3.addList(s, "type3list1", AccessPrivilege.READ, AccessPrivilege.ADMIN);
      type3list2 = type3.addList(s, "type3list2", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN);
      type3attr1 = type3.addAttribute(s, "type3attr1", AccessPrivilege.READ, AccessPrivilege.ADMIN, true);
      type3attr2 = type3.addAttribute(s, "type3attr2", AccessPrivilege.ADMIN, AccessPrivilege.ADMIN, false);
    }
    catch (Exception e) {
      throw new GrouperException( "test setUp() error: " + e.getMessage(), e );
    }
  }

  public void tearDown() {
    super.tearDown();
  }
  
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
    R r = R.populateRegistry(0, 0, 11);

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

    R r = R.populateRegistry(0, 0, 11);
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
    R r = R.populateRegistry(0, 0, 11);

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
    R r = R.populateRegistry(0, 0, 11);

    group_copy_setup(r, false);
    GroupCopy groupCopy = new GroupCopy(child_group, top);
    Group newGroup = groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
    .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(false).save();
    verify_copy(r, newGroup, false, false, false, false, false);
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_members_only() throws Exception {
    R r = R.populateRegistry(0, 0, 11);

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
    R r = R.populateRegistry(0, 0, 11);

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
    R r = R.populateRegistry(0, 0, 11);

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
    R r = R.populateRegistry(0, 0, 11);
    top.addChildGroup("child group", "child group");

    group_copy_setup(r, false);
    Group newGroup = child_group.copy(top);
    assertTrue(newGroup.getTypes().size() == 3);
    assertGroupName(newGroup, "top:child group.2");
    assertGroupDisplayName(newGroup, "top display name:child group display name");  
    
    Group newGroup2 = child_group.copy(top);
    assertTrue(newGroup2.getTypes().size() == 3);
    assertGroupName(newGroup2, "top:child group.3");
    assertGroupDisplayName(newGroup2, "top display name:child group display name");  
    
    r.rs.stop();
  }
  
  /**
   * @throws Exception
   */
  public void test_copy_group_is_factor() throws Exception {
    R r = R.populateRegistry(0, 0, 11);

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
    R r = R.populateRegistry(0, 0, 11);
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
    R r = R.populateRegistry(0, 0, 11);
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
    R r = R.populateRegistry(0, 0, 11);
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
    R r = R.populateRegistry(0, 0, 11);
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
    R r = R.populateRegistry(0, 0, 11);
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
    Field admins = FieldFinder.find("admins", true);
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
    Field stemmers = FieldFinder.find("stemmers", true);
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
    } catch (Exception eExpected) {
      if (eExpected.getCause() instanceof InsufficientPrivilegeException) {
        assertTrue(true);
      } else {
        fail("failed to throw exception whose cause is InsufficientPrivilegeException");
      }
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
    } catch (Exception eExpected) {
      if (eExpected.getCause() instanceof InsufficientPrivilegeException) {
        assertTrue(true);
      } else {
        fail("failed to throw exception whose cause is InsufficientPrivilegeException");
      }
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

    nrs.stop();
    nrs = GrouperSession.start(a);
    try {
      GroupCopy groupCopy = new GroupCopy(child_group, top);
      groupCopy.copyPrivilegesOfGroup(false).copyGroupAsPrivilege(false)
          .copyListMembersOfGroup(false).copyListGroupAsMember(false).copyAttributes(
              true).save();
      fail("failed to throw exception");
    } catch (InsufficientPrivilegeException eExpected) {
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
    child_group.setAttribute(type1attr1.getName(), "custom attr value 1");
    child_group.setAttribute(type2attr1.getName(), "custom attr value 2");
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
    
    // let's do some quick checks to make sure the object that gets returned back is correct
    assertTrue(newGroup.getTypes().size() == 3);
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
      assertTrue(newGroup.getReaders().size() == 1);    }
    
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
    assertTrue(newGroup.getTypes().size() == 3);
    
    // attribute checks
    if (attributes) {
      assertTrue(newGroup.getAttributeValue(type1attr1.getName(), false, false).equals("custom attr value 1"));
      assertTrue(newGroup.getAttributeValue(type1attr2.getName(), false, false).equals(""));
      assertTrue(newGroup.getAttributeValue(type2attr1.getName(), false, false).equals("custom attr value 2"));
      assertTrue(newGroup.getAttributeValue(type2attr2.getName(), false, false).equals(""));
    } else {
      assertTrue(newGroup.getAttributeValue(type1attr1.getName(), false, false).equals(""));
      assertTrue(newGroup.getAttributeValue(type1attr2.getName(), false, false).equals(""));
      assertTrue(newGroup.getAttributeValue(type2attr1.getName(), false, false).equals(""));
      assertTrue(newGroup.getAttributeValue(type2attr2.getName(), false, false).equals(""));
    }
    
    // parent checks
    assertTrue(newGroup.getParentStem().getUuid().equals(top.getUuid()));
  }
  
  /**
   * @throws Exception
   */
  public void test_option_to_disable_last_membership_change() throws Exception {
    ApiConfig.testConfig.put("groups.updateLastMembershipTime", "false");
    ApiConfig.testConfig.put("stems.updateLastMembershipTime", "true");
    
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
  }
  
  /**
   * @throws Exception
   */
  public void test_alternateName() throws Exception {
    assertTrue(top_group.getAlternateNames().size() == 0);

    // add invalid group name
    try {
      top_group.addAlternateName(null);
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("");
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("top");
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("top:top group2:");
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("top::top group2");
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
      assertTrue(true);
    }
    
    // add invalid group name
    try {
      top_group.addAlternateName("top:  :top group2");
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
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
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
      fail("failed to throw GroupModifyException");
    } catch (GroupModifyException e) {
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

