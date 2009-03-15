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

package edu.internet2.middleware.grouper;

import edu.internet2.middleware.grouper.exception.GroupAddException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.GrouperException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.RevokePrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.exception.UnableToPerformException;
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.subject.Subject;


/**
 * Test {@link Group}.
 * <p/>
 * @author  blair christensen.
 * @version $Id: Test_api_Group.java,v 1.9 2009-03-15 23:13:50 shilen Exp $
 * @since   1.2.1
 */
public class Test_api_Group extends GrouperTest {


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
    Group newGroup = groupCopy.save();
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
    child_group.store();
    
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
    child_group.store();
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
      assertTrue(newGroup.getAttribute(type1attr1.getName()).equals("custom attr value 1"));
      assertTrue(newGroup.getAttribute(type1attr2.getName()).equals(""));
      assertTrue(newGroup.getAttribute(type2attr1.getName()).equals("custom attr value 2"));
      assertTrue(newGroup.getAttribute(type2attr2.getName()).equals(""));
    } else {
      assertTrue(newGroup.getAttribute(type1attr1.getName()).equals(""));
      assertTrue(newGroup.getAttribute(type1attr2.getName()).equals(""));
      assertTrue(newGroup.getAttribute(type2attr1.getName()).equals(""));
      assertTrue(newGroup.getAttribute(type2attr2.getName()).equals(""));
    }
    
    // parent checks
    assertTrue(newGroup.getParentStem().getUuid().equals(top.getUuid()));
  }
  
}

