/**
 * Copyright 2012 Internet2
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
/**
 * @author mchyzer
 * $Id: PermissionEntryTest.java,v 1.5 2009-11-10 03:35:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.permissions;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.AttributeDefPrivilege;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class PermissionEntryTest extends GrouperTest {
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionEntryTest("testPermissionAssignUnassignSecurity"));
  }

  /**
   * 
   */
  public PermissionEntryTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public PermissionEntryTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;

  /** root stem */
  private Stem root;

  /** top stem */
  private Stem top;

  /**
   * 
   */
  @Override
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = this.root.addChildStem("top", "top display name");

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
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  public void tearDown() {
    GrouperSession.stopQuietly(this.grouperSession);
    super.tearDown();
  }
  
  /**
   * permission entry
   */
  public void testHibernate() {
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ5, true); 
    Set<PermissionEntry> permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member.getUuid());
    for (PermissionEntry permissionEntry : permissionEntries) {
      assertNotNull(permissionEntry);
    }
  }
  
  /**
   * 
   */
  @SuppressWarnings("unused") 
  public void testAddLookup() {
    Role role = this.top.addChildRole("test", "test");
    ((Group)role).addMember(SubjectTestHelper.SUBJ5);
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("test", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attributeDefName = this.top.addChildAttributeDefName(attributeDef, "testName", "test name");
    
    role.getPermissionRoleDelegate().assignRolePermission(attributeDefName);
    
    Member member = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ5, true); 
    Set<PermissionEntry> permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member.getUuid());
    for (PermissionEntry permissionEntry : permissionEntries) {
    }

    AttributeDefName attributeDefNameEff = this.top.addChildAttributeDefName(attributeDef, "testNameEff", "test name effective");
    role.getPermissionRoleDelegate().assignSubjectRolePermission(attributeDefNameEff, member);
    
    permissionEntries = GrouperDAOFactory.getFactory().getPermissionEntry().findByMemberId(member.getUuid());
    for (PermissionEntry permissionEntry : permissionEntries) {
    }
    
    
  }
  
  /**
   * 
   */
  public void testPermissionRetrieveSecurity() {

    Role role = this.top.addChildRole("role", "role");
    Role role2 = this.top.addChildRole("role2", "role2");

    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ3, true);
    ((Group)role).addMember(SubjectTestHelper.SUBJ0);
    ((Group)role).addMember(SubjectTestHelper.SUBJ1);
    ((Group)role).addMember(SubjectTestHelper.SUBJ2);
    ((Group)role2).addMember(SubjectTestHelper.SUBJ3);
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attrDefName = this.top.addChildAttributeDefName(attributeDef, "attrDefName", "attrDefName");

    role.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
    role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ3, PermissionAllowed.ALLOWED);

    //test subject 0 can GROUP_ATTR_READ and read
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 1 can GROUP_ATTR_READ not read
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);

    //test subject 2 can read not GROUP_ATTR_READ
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 3 can not read or GROUP_ATTR_READ

    //test subject 4 can read and read
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 5 can update and read
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 6 can admin and read
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 7 can view and update
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.VIEW);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 8 can view and admin
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.VIEW);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view and view
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.VIEW);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);

    
    //test subject 0 can GROUP_ATTR_READ and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    AttributeAssign assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
    assertNotNull(assignment);
    
    Set<AttributeAssign> assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(1, assignments.size());
    
    assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
    assertEquals(1, assignments.size());
    

    //test subject 1 can GROUP_ATTR_READ not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    try {
      assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(0, assignments.size());
    
    try {
      assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    //test subject 2 can read not GROUP_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    try {
      assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(0, assignments.size());
    
    try {
      assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    
    //test subject 3 can not read or GROUP_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    try {
      assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(0, assignments.size());
    
    try {
      assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    try {
      assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(0, assignments.size());
    
    try {
      assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
    
    try {
      assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(0, assignments.size());
    
    try {
      assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
    
    assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
    assertNotNull(assignment);
    
    assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(1, assignments.size());
    
    assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
    assertEquals(1, assignments.size());
    
    
    //test subject 7 can view and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
    
    try {
      assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(0, assignments.size());
    
    try {
      assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    //test subject 8 can view and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
    
    try {
      assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(0, assignments.size());
    
    try {
      assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    //test subject 9 can view and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
    
    try {
      assignment = role2.getPermissionRoleDelegate().retrieveAssignment(member3, "assign", attrDefName, true, false);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    assignments = ((Group)role).getAttributeDelegate().retrieveAssignments();
    assertEquals(0, assignments.size());
    
    try {
      assignments = ((Group)role2).getAttributeDelegateEffMship(member3).retrieveAssignments(attributeDef);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
  }
  
  /**
   * 
   */
  public void testPermissionAssignUnassignSecurity() {

    Role role = this.top.addChildRole("role", "role");
    Role role2 = this.top.addChildRole("role2", "role2");

    Member member3 = MemberFinder.findBySubject(this.grouperSession, SubjectTestHelper.SUBJ3, true);
    ((Group)role2).addMember(SubjectTestHelper.SUBJ0);
    ((Group)role2).addMember(SubjectTestHelper.SUBJ1);
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attrDefName = this.top.addChildAttributeDefName(attributeDef, "attrDefName", "attrDefName");

    role.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
    role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ0, PermissionAllowed.ALLOWED);


    //test subject 0 can GROUP_ATTR_UPDATE and update
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_UPDATE);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 1 can GROUP_ATTR_UPDATE not update
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_UPDATE);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_UPDATE);

    //test subject 2 can update not GROUP_ATTR_UPDATE
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 3 can not GROUP_ATTR_UPDATE or update

    //test subject 4 can update and update
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.UPDATE);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 5 can GROUP_ATTR_READ and update
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.GROUP_ATTR_READ);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 6 can read and update
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.READ);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_UPDATE, false);
    
    //test subject 7 can admin and update
    ((Group)role).grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    ((Group)role2).grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);

    
    //test subject 0 can GROUP_ATTR_UPDATE and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
   
    role2.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
    role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
    role2.getPermissionRoleDelegate().removeRolePermission(attrDefName);
    role2.getPermissionRoleDelegate().removeSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1);

    
    //test subject 1 can GROUP_ATTR_UPDATE not update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
   
    try {
      role2.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role.getPermissionRoleDelegate().removeRolePermission(attrDefName);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().removeSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ0);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    
    //test subject 2 can update not GROUP_ATTR_UPDATE
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
   
    try {
      role2.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role.getPermissionRoleDelegate().removeRolePermission(attrDefName);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().removeSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ0);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    
    //test subject 3 can not GROUP_ATTR_UPDATE or update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
   
    try {
      role2.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role.getPermissionRoleDelegate().removeRolePermission(attrDefName);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().removeSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ0);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    
    //test subject 4 can update and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
   
    try {
      role2.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role.getPermissionRoleDelegate().removeRolePermission(attrDefName);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().removeSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ0);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    
    //test subject 5 can GROUP_ATTR_READ and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
   
    try {
      role2.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role.getPermissionRoleDelegate().removeRolePermission(attrDefName);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().removeSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ0);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    
    //test subject 6 can read and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
   
    try {
      role2.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role.getPermissionRoleDelegate().removeRolePermission(attrDefName);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    try {
      role2.getPermissionRoleDelegate().removeSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ0);
      fail();
    } catch (InsufficientPrivilegeException e) {
      // ok
    }
    
    //test subject 7 can admin and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
   
    role2.getPermissionRoleDelegate().assignRolePermission(attrDefName, PermissionAllowed.ALLOWED);
    role2.getPermissionRoleDelegate().assignSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1, PermissionAllowed.ALLOWED);
    role2.getPermissionRoleDelegate().removeRolePermission(attrDefName);
    role2.getPermissionRoleDelegate().removeSubjectRolePermission(attrDefName, SubjectTestHelper.SUBJ1);
  }
  
  /**
   * 
   */
  public void testHierarchies() {
    
    //parent group has child as member
    Group groupParent = this.top.addChildGroup("groupParent", "groupParent");
    Group groupChild = this.top.addChildGroup("groupChild", "groupChild");
    //if you are in parent, then you are in child
    groupChild.addMember(groupParent.toSubject());
    
    groupParent.addMember(SubjectTestHelper.SUBJ0);
    groupChild.addMember(SubjectTestHelper.SUBJ1);
    
    //parent implies child
    Role roleParent = this.top.addChildRole("roleParent", "roleParent");
    Role roleChild = this.top.addChildRole("roleChild", "roleChild");
    roleChild.getRoleInheritanceDelegate().addRoleToInheritFromThis(roleParent);
        
    ((Group)roleParent).addMember(SubjectTestHelper.SUBJ2);
    ((Group)roleChild).addMember(SubjectTestHelper.SUBJ3);
    
    Role roleParent2 = this.top.addChildRole("roleParent2", "roleParen2t");
    Role roleChild2 = this.top.addChildRole("roleChild2", "roleChild2");
    roleChild2.getRoleInheritanceDelegate().addRoleToInheritFromThis(roleParent2);
        
    roleParent2.addMember(SubjectTestHelper.SUBJ4, false);
    roleChild2.addMember(SubjectTestHelper.SUBJ5, false);
    
    
    AttributeDef attributeDef = this.top.addChildAttributeDef("attributeDef", AttributeDefType.perm);
    attributeDef.setAssignToEffMembership(true);
    attributeDef.setAssignToGroup(true);
    attributeDef.store();
    AttributeDefName attrDefNameParent = this.top.addChildAttributeDefName(attributeDef, "attrDefNameParent", "attrDefNameParent");
    AttributeDefName attrDefNameChild = this.top.addChildAttributeDefName(attributeDef, "attrDefNameChild", "attrDefNameChild");
    attrDefNameParent.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(attrDefNameChild);

    AttributeAssignAction actionParent = attributeDef.getAttributeDefActionDelegate().addAction("actionParent");
    AttributeAssignAction actionChild = attributeDef.getAttributeDefActionDelegate().addAction("actionChild");
    
    actionParent.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(actionChild);

    roleParent.getPermissionRoleDelegate().assignRolePermission("actionParent", attrDefNameParent);
    roleParent2.getPermissionRoleDelegate().assignRolePermission("actionChild", attrDefNameChild);
    roleParent.addMember(SubjectTestHelper.SUBJA, false);
    roleParent2.addMember(groupParent.toSubject(), false);
    
    roleParent.addMember(SubjectTestHelper.SUBJ6, false);
    roleParent.getPermissionRoleDelegate().assignSubjectRolePermission("actionParent", attrDefNameChild, SubjectTestHelper.SUBJ6);
    roleParent.addMember(SubjectTestHelper.SUBJ7, false);
    roleParent.getPermissionRoleDelegate().assignSubjectRolePermission("actionChild", attrDefNameParent, SubjectTestHelper.SUBJ7);
    roleChild2.addMember(SubjectTestHelper.SUBJ8, false);
    roleChild2.getPermissionRoleDelegate().assignSubjectRolePermission("actionParent", attrDefNameParent, SubjectTestHelper.SUBJ8);
    roleChild2.addMember(SubjectTestHelper.SUBJ9, false);
    roleChild2.getPermissionRoleDelegate().assignSubjectRolePermission("actionChild", attrDefNameChild, SubjectTestHelper.SUBJ9);

    //test subject 0 can GROUP_ATTR_READ and read
    ((Group)roleParent).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    ((Group)roleParent2).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    ((Group)roleChild).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    ((Group)roleChild2).grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.GROUP_ATTR_READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ0, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 1 can GROUP_ATTR_READ not read
    ((Group)roleParent).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    ((Group)roleParent2).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    ((Group)roleChild).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);
    ((Group)roleChild2).grantPriv(SubjectTestHelper.SUBJ1, AccessPrivilege.GROUP_ATTR_READ);

    //test subject 2 can read not GROUP_ATTR_READ
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ2, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 3 can not read or GROUP_ATTR_READ

    //test subject 4 can read and read
    ((Group)roleParent).grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    ((Group)roleParent2).grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    ((Group)roleChild).grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    ((Group)roleChild2).grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.READ);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ4, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 5 can update and read
    ((Group)roleParent).grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    ((Group)roleParent2).grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    ((Group)roleChild).grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    ((Group)roleChild2).grantPriv(SubjectTestHelper.SUBJ5, AccessPrivilege.UPDATE);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ5, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 6 can admin and read
    ((Group)roleParent).grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    ((Group)roleParent2).grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    ((Group)roleChild).grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    ((Group)roleChild2).grantPriv(SubjectTestHelper.SUBJ6, AccessPrivilege.ADMIN);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ6, AttributeDefPrivilege.ATTR_READ, false);

    //test subject 7 can view and update
    ((Group)roleParent).grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.VIEW);
    ((Group)roleParent2).grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.VIEW);
    ((Group)roleChild).grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.VIEW);
    ((Group)roleChild2).grantPriv(SubjectTestHelper.SUBJ7, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ7, AttributeDefPrivilege.ATTR_UPDATE, false);

    //test subject 8 can view and admin
    ((Group)roleParent).grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.VIEW);
    ((Group)roleParent2).grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.VIEW);
    ((Group)roleChild).grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.VIEW);
    ((Group)roleChild2).grantPriv(SubjectTestHelper.SUBJ8, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ8, AttributeDefPrivilege.ATTR_ADMIN, false);
    
    //test subject 9 can view and view
    ((Group)roleParent).grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.VIEW);
    ((Group)roleParent2).grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.VIEW);
    ((Group)roleChild).grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.VIEW);
    ((Group)roleChild2).grantPriv(SubjectTestHelper.SUBJ9, AccessPrivilege.VIEW);
    attributeDef.getPrivilegeDelegate().grantPriv(SubjectTestHelper.SUBJ9, AttributeDefPrivilege.ATTR_VIEW, false);


    Set<PermissionEntry> permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);
    List<PermissionEntry> permissionEntriesList = new ArrayList<PermissionEntry>(permissionEntriesSet);
    Collections.sort(permissionEntriesList);
//    System.out.println("\n\n");
//    for (PermissionEntry permissionEntry : permissionEntriesList) {
//      System.out.println("    permissionEntry = PermissionEntry.collectionFindFirst(permissionEntriesList, \"" 
//          + permissionEntry.getRoleName() + "\", \"" + permissionEntry.getAttributeDefNameName() + "\", \"" 
//          + permissionEntry.getAction() + "\", \"" + permissionEntry.getSubjectSourceId() + "\", \""
//          + permissionEntry.getSubjectId() + "\", \"" + permissionEntry.getPermissionTypeDb() + "\");");
//      System.out.println("    assertPermission(permissionEntry, \"" 
//          + permissionEntry.getPermissionTypeDb() + "\", " + permissionEntry.isImmediateMembership() + ", " 
//          + permissionEntry.isImmediatePermission() + ", " + permissionEntry.getMembershipDepth() + ", " 
//          + permissionEntry.getRoleSetDepth() + ", " + permissionEntry.getAttributeAssignActionSetDepth()
//          + ", " + permissionEntry.getAttributeDefNameSetDepth() + ");\n");
//      
//    }
    
    assertEquals(35, GrouperUtil.length(permissionEntriesList));
    
    PermissionEntry permissionEntry = null;
    
    //NOTE, THIS WAS GENERATED FROM ABOVE

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.3", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 1, 1, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild", "top:attrDefNameChild", "actionParent", "jdbc", "test.subject.3", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 1, 0, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild", "top:attrDefNameParent", "actionChild", "jdbc", "test.subject.3", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 1, 1, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild", "top:attrDefNameParent", "actionParent", "jdbc", "test.subject.3", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 1, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild2", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.5", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 1, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild2", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.8", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 1, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild2", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.9", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 1, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild2", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.8", "role_subject");
    assertPermission(permissionEntry, "role_subject", true, false, 0, -1, 1, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild2", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.9", "role_subject");
    assertPermission(permissionEntry, "role_subject", true, true, 0, -1, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild2", "top:attrDefNameChild", "actionParent", "jdbc", "test.subject.8", "role_subject");
    assertPermission(permissionEntry, "role_subject", true, false, 0, -1, 0, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild2", "top:attrDefNameParent", "actionChild", "jdbc", "test.subject.8", "role_subject");
    assertPermission(permissionEntry, "role_subject", true, false, 0, -1, 1, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleChild2", "top:attrDefNameParent", "actionParent", "jdbc", "test.subject.8", "role_subject");
    assertPermission(permissionEntry, "role_subject", true, true, 0, -1, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionChild", "g:isa", "GrouperAll", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 1, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.2", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 1, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.6", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 1, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.7", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 1, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.6", "role_subject");
    assertPermission(permissionEntry, "role_subject", true, false, 0, -1, 1, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.7", "role_subject");
    assertPermission(permissionEntry, "role_subject", true, false, 0, -1, 0, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionParent", "g:isa", "GrouperAll", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 0, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionParent", "jdbc", "test.subject.2", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 0, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionParent", "jdbc", "test.subject.6", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 0, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionParent", "jdbc", "test.subject.7", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 0, 1);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameChild", "actionParent", "jdbc", "test.subject.6", "role_subject");
    assertPermission(permissionEntry, "role_subject", true, true, 0, -1, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameParent", "actionChild", "g:isa", "GrouperAll", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 1, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameParent", "actionChild", "jdbc", "test.subject.2", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 1, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameParent", "actionChild", "jdbc", "test.subject.6", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 1, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameParent", "actionChild", "jdbc", "test.subject.7", "role");
    assertPermission(permissionEntry, "role", true, false, 0, 0, 1, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameParent", "actionChild", "jdbc", "test.subject.7", "role_subject");
    assertPermission(permissionEntry, "role_subject", true, true, 0, -1, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameParent", "actionParent", "g:isa", "GrouperAll", "role");
    assertPermission(permissionEntry, "role", true, true, 0, 0, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameParent", "actionParent", "jdbc", "test.subject.2", "role");
    assertPermission(permissionEntry, "role", true, true, 0, 0, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameParent", "actionParent", "jdbc", "test.subject.6", "role");
    assertPermission(permissionEntry, "role", true, true, 0, 0, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent", "top:attrDefNameParent", "actionParent", "jdbc", "test.subject.7", "role");
    assertPermission(permissionEntry, "role", true, true, 0, 0, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent2", "top:attrDefNameChild", "actionChild", "g:gsa", groupParent.getUuid(), "role");
    assertPermission(permissionEntry, "role", true, true, 0, 0, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent2", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.0", "role");
    assertPermission(permissionEntry, "role", false, true, 1, 0, 0, 0);

    permissionEntry = PermissionEntryUtils.collectionFindFirst(permissionEntriesList, "top:roleParent2", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.4", "role");
    assertPermission(permissionEntry, "role", true, true, 0, 0, 0, 0);


    //NOTE THAT WAS GENERATED
    
    //test subject 0 can GROUP_ATTR_READ and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(35, GrouperUtil.length(permissionEntriesSet));

    //test subject 1 can GROUP_ATTR_READ not read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ1);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(0, GrouperUtil.length(permissionEntriesSet));

    //test subject 2 can read not GROUP_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(0, GrouperUtil.length(permissionEntriesSet));

    //test subject 3 can not read or GROUP_ATTR_READ
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ3);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(0, GrouperUtil.length(permissionEntriesSet));

    //test subject 4 can read and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(0, GrouperUtil.length(permissionEntriesSet));

    //test subject 5 can update and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(0, GrouperUtil.length(permissionEntriesSet));

    //test subject 6 can admin and read
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ6);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(35, GrouperUtil.length(permissionEntriesSet));

    //test subject 7 can view and update
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ7);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(0, GrouperUtil.length(permissionEntriesSet));

    //test subject 8 can view and admin
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ8);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(0, GrouperUtil.length(permissionEntriesSet));
    
    //test subject 9 can view and view
    GrouperSession.stopQuietly(this.grouperSession);
    this.grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ9);
    
    permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);

    assertEquals(0, GrouperUtil.length(permissionEntriesSet));

    
  }

  /**
   * make sure all this stuff is correct
   * @param permissionEntry
   * @param permissionType
   * @param immediateMembership
   * @param immediatePermission
   * @param membershipDepth
   * @param roleDepth
   * @param actionDepth
   * @param attrDefDepth
   */
  public static void assertPermission(PermissionEntry permissionEntry, String permissionType, 
      boolean immediateMembership, boolean immediatePermission, int membershipDepth,
      int roleDepth, int actionDepth, int attrDefDepth) {
    String permissionEntryString = permissionEntry.toString();
    assertEquals(permissionEntryString, permissionType, permissionEntry.getPermissionTypeDb());
    assertEquals(permissionEntryString, immediateMembership, permissionEntry.isImmediateMembership());
    assertEquals(permissionEntryString, immediatePermission, permissionEntry.isImmediatePermission());
    assertEquals(permissionEntryString, membershipDepth, permissionEntry.getMembershipDepth());
    assertEquals(permissionEntryString, roleDepth, permissionEntry.getRoleSetDepth());
    assertEquals(permissionEntryString, actionDepth, permissionEntry.getAttributeAssignActionSetDepth());
    assertEquals(permissionEntryString, attrDefDepth, permissionEntry.getAttributeDefNameSetDepth());
  }
  
  /**
   * 
   */
  public void testDisabledDateRange() {

    GrouperSession grouperSession = GrouperSession.startRootSession();

    AttributeDef permissionDef = new AttributeDefSave(grouperSession)
      .assignName("stem:permissionDef").assignCreateParentStemsIfNotExist(true)
      .assignAttributeDefType(AttributeDefType.perm)
      .save();
    
    permissionDef.setAssignToEffMembership(true);
    permissionDef.setAssignToGroup(true);
    permissionDef.store();
    
    //make a role
    Role payrollUser = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollUser").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();
    Role payrollGuest = new GroupSave(grouperSession).assignName("apps:payroll:roles:payrollGuest").assignTypeOfGroup(TypeOfGroup.role).assignCreateParentStemsIfNotExist(true).save();

    Subject subject0 = SubjectFinder.findByIdAndSource("test.subject.0", "jdbc", true);

    //subject 1,2 is just more data in the mix
    Subject subject1 = SubjectFinder.findByIdAndSource("test.subject.1", "jdbc", true);
    Subject subject2 = SubjectFinder.findByIdAndSource("test.subject.2", "jdbc", true);
    

    payrollUser.addMember(subject1, false);
    payrollGuest.addMember(subject0, false);
    payrollGuest.addMember(subject2, false);
    
    //create a permission, assign to role
    AttributeDefName canLogin = new AttributeDefNameSave(grouperSession, permissionDef).assignName("apps:payroll:permissions:canLogin").assignCreateParentStemsIfNotExist(true).save();
    
    payrollUser.getPermissionRoleDelegate().assignRolePermission(canLogin);

    payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject2);


    try {
      GrouperDAOFactory.getFactory().getPermissionEntry().findPermissionsByAttributeDefDisabledRange(permissionDef.getId(),
          null, null);

      fail("should need either disabled from or to");
    } catch (Exception e) {
      //good
    }

    AttributeAssign attributeAssign = payrollGuest.getPermissionRoleDelegate().assignSubjectRolePermission(canLogin, subject0).getAttributeAssign();

    Timestamp timestamp5daysForward = new Timestamp(System.currentTimeMillis() + (5 * 24 * 60 * 60 * 1000));
    Timestamp timestamp6daysForward = new Timestamp(System.currentTimeMillis() + (6 * 24 * 60 * 60 * 1000));
    Timestamp timestamp7daysForward = new Timestamp(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000));
    Timestamp timestamp8daysForward = new Timestamp(System.currentTimeMillis() + (8 * 24 * 60 * 60 * 1000));
    Timestamp timestamp9daysForward = new Timestamp(System.currentTimeMillis() + (9 * 24 * 60 * 60 * 1000));
    
    Set<PermissionEntry> permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
      .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
          timestamp6daysForward, timestamp8daysForward);

    assertEquals(0, permissions.size());
    
    //############### set disabled 7 days in the future
    attributeAssign.setDisabledTime(timestamp7daysForward);
    attributeAssign.saveOrUpdate(true);
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
      .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
        timestamp6daysForward, timestamp8daysForward);

    assertEquals(1, permissions.size());

    payrollUser.addMember(subject0, false);

    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
    .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
      timestamp6daysForward, timestamp8daysForward);

    assertEquals("there is a membership in another path, not going to expire", 0, permissions.size());
    
    //################# BACK TO ONE RECORD
    payrollUser.deleteMember(subject0, false);
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
    .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
      timestamp6daysForward, timestamp8daysForward);

    assertEquals(1, permissions.size());

    //################# BACK TO ONE RECORD, MIXED UP ORDER
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
    .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
      timestamp8daysForward, timestamp6daysForward);

    assertEquals(1, permissions.size());

    //################# SET TO 5 DAYS
    attributeAssign.setDisabledTime(timestamp5daysForward);
    attributeAssign.saveOrUpdate(true);
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
    .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
      timestamp6daysForward, timestamp8daysForward);

    assertEquals("out of range", 0, permissions.size());

    //################# SET TO 9 DAYS
    attributeAssign.setDisabledTime(timestamp9daysForward);
    attributeAssign.saveOrUpdate(true);
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
    .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
      timestamp6daysForward, timestamp8daysForward);

    assertEquals("out of range", 0, permissions.size());

    //################ TRY ONLY FROM
    attributeAssign.setDisabledTime(timestamp7daysForward);
    attributeAssign.saveOrUpdate(true);
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
    .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
      timestamp6daysForward, null);

    assertEquals("in range", 1, permissions.size());

    //################ TRY ONLY FROM
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
    .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
      timestamp8daysForward, null);

    assertEquals("not in range", 0, permissions.size());

    //################ TRY ONLY TO
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
    .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
      null, timestamp8daysForward);

    assertEquals("in range", 1, permissions.size());

    //################ TRY ONLY TO
    
    permissions = GrouperDAOFactory.getFactory().getPermissionEntry()
    .findPermissionsByAttributeDefDisabledRange(permissionDef.getId(), 
      null, timestamp6daysForward);

    assertEquals("not in range", 0, permissions.size());
  }

}
