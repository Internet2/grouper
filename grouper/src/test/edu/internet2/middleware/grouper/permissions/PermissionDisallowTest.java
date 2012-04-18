/*******************************************************************************
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.permissions;

import java.util.ArrayList;
import java.util.List;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.Stem.Scope;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.permissions.PermissionEntry.PermissionType;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class PermissionDisallowTest extends GrouperTest {
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionDisallowTest("testFindPermissionsInStem"));
  }

  /**
   * 
   */
  public PermissionDisallowTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public PermissionDisallowTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;

  /** root stem */
  @SuppressWarnings("unused")
  private Stem root;

  /** top stem */
  @SuppressWarnings("unused")
  private Stem top;

  /** admin role */
  private Role adminRole;

  /** senior admin */
  private Role seniorAdmin;

  /** user */
  private Role user;
  
  /** attribute def */
  private AttributeDef permissionDef;
  
  /** english */
  private AttributeDefName english;
  
  /** english */
  private AttributeDefName math;
  
  /** english */
  private AttributeDefName electricalEngineering;
  
  /** english */
  private AttributeDefName chemicalEngineering;
  
  /** english */
  private AttributeDefName artsAndSciences;
  
  /** english */
  private AttributeDefName engineering;
  
  /** english */
  private AttributeDefName all;

  /** read */
  private String readString = "read";
  
  /** write */
  private String writeString = "write";
  
  /** readWrite */
  private String readWriteString = "readWrite";
  
  /** admin */
  private String adminString = "admin";
  
  /** subj0 */
  private Subject subj0;
  
  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = new StemSave(this.grouperSession).assignName("top").assignDisplayExtension("top display name").save();

    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrAdmin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptout", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrRead", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrUpdate", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrView", "false");

    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
    this.adminRole = new GroupSave(this.grouperSession).assignName("top:admin").assignTypeOfGroup(TypeOfGroup.role).save();
    
    this.seniorAdmin = new GroupSave(this.grouperSession).assignName("top:seniorAdmin").assignTypeOfGroup(TypeOfGroup.role).save();
    
    //senior admin inherits from admin
    this.seniorAdmin.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.adminRole);
    
    this.user = new GroupSave(this.grouperSession).assignName("top:user").assignTypeOfGroup(TypeOfGroup.role).save();
    
    this.permissionDef = new AttributeDefSave(this.grouperSession).assignName("top:permissionDef")
      .assignAttributeDefType(AttributeDefType.perm).assignToEffMembership(true).assignToGroup(true).save();
    this.english = new AttributeDefNameSave(this.grouperSession, this.permissionDef)
      .assignCreateParentStemsIfNotExist(true).assignName("top:artsAndSciences:english").assignDisplayExtension("English").save();
    this.math = new AttributeDefNameSave(this.grouperSession, this.permissionDef)
      .assignCreateParentStemsIfNotExist(true).assignName("top:artsAndSciences:math").assignDisplayExtension("Math").save();
    this.electricalEngineering = new AttributeDefNameSave(this.grouperSession, this.permissionDef)
      .assignCreateParentStemsIfNotExist(true).assignName("top:engineering:electrical:engineering").assignDisplayExtension("Electrical Engineering").save();
    this.chemicalEngineering = new AttributeDefNameSave(this.grouperSession, this.permissionDef)
      .assignCreateParentStemsIfNotExist(true).assignName("top:engineering:chemicalEngineering").assignDisplayExtension("Chemical Engineering").save();
    this.artsAndSciences = new AttributeDefNameSave(this.grouperSession, this.permissionDef)
      .assignCreateParentStemsIfNotExist(true).assignName("top:artsAndSciences").assignDisplayExtension("Arts and Sciences").save();
    this.engineering = new AttributeDefNameSave(this.grouperSession, this.permissionDef)
      .assignCreateParentStemsIfNotExist(true).assignName("top:engineering").assignDisplayExtension("Engineering").save();
    this.all = new AttributeDefNameSave(this.grouperSession, this.permissionDef)
      .assignCreateParentStemsIfNotExist(true).assignName("top:all").assignDisplayExtension("All").save();

    this.all.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.engineering);
    this.all.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.artsAndSciences);
    this.artsAndSciences.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.english);
    this.artsAndSciences.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.math);
    this.engineering.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.math);
    this.engineering.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.electricalEngineering);
    this.engineering.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.chemicalEngineering);

    this.permissionDef.getAttributeDefActionDelegate().configureActionList("read, write, readWrite, admin");
    
    
    AttributeAssignAction read = this.permissionDef.getAttributeDefActionDelegate().findAction(this.readString, true);
    AttributeAssignAction write = this.permissionDef.getAttributeDefActionDelegate().findAction(this.writeString, true);
    AttributeAssignAction readWrite = this.permissionDef.getAttributeDefActionDelegate().findAction(this.readWriteString, true);
    AttributeAssignAction admin = this.permissionDef.getAttributeDefActionDelegate().findAction(this.adminString, true);
    
    readWrite.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(read);
    readWrite.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(write);
    admin.getAttributeAssignActionSetDelegate().addToAttributeAssignActionSet(readWrite);
    
    this.subj0 = SubjectTestHelper.SUBJ0;
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
   * 
   */
  public void testActionDirectedGraphPriority() {
    
    //Role<Admin> allows Action<Admin> of Resource<All>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.adminString, this.all, PermissionAllowed.ALLOWED);

    //Role<Admin> denies Action<Read / write> of Resource<All>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readWriteString, this.all, PermissionAllowed.DISALLOWED);

    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);

    //Result:

    //User subj0 is denied from Action<Read> and Action<Write> of Resource<Math> since there 
    //are only inherited assignments and the one with the lower depth (tie in resource, 
    //Read/Write is lower than Action<Admin>) 
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.math).findPermissions());

    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    boolean isDisallowed1 = permissionEntries.get(0).isDisallowed();
    boolean isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);
    
    //if we filter permissions, should be one
    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.math)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS).findPermissions());
    
    //there should be one, one should be disallow
    assertEquals(1, GrouperUtil.length(permissionEntries));

    assertTrue(permissionEntries.get(0).isDisallowed());

    //if we filter by role, it should find the disallow
    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.math)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES).findPermissions());
    
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    
    assertTrue(permissionEntries.get(0).isDisallowed());
    
    assertFalse("wrong subject", new PermissionFinder().addSubject(SubjectTestHelper.SUBJ1)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
    assertFalse("tie", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.math).hasPermission());
        
    assertFalse("tie",new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readWriteString)
        .addPermissionName(this.math).hasPermission());
    
    assertTrue("tie", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.adminString)
        .addPermissionName(this.math).hasPermission());
        
    assertFalse("tie", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.writeString)
        .addPermissionName(this.math).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.math).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.user)
        .addPermissionName(this.math).hasPermission());
        
    assertFalse("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.math).hasPermission());
        
    assertFalse("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.electricalEngineering).hasPermission());
        
    assertFalse("directish", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.artsAndSciences).hasPermission());
        
    assertFalse("is all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.all).hasPermission());
        
    assertFalse("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.electricalEngineering).hasPermission());
        
    assertFalse("wrong action", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readWriteString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
    
    assertFalse("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.math).hasPermission());
        
  }

  
  /**
   * 
   */
  public void testResourceDirectedGraphPriorityWithTieAndDifferentActions() {

    //Role<Admin> allows Action<Read / write> of Resource<Engineering>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readWriteString, this.engineering, PermissionAllowed.ALLOWED);

    //Role<Admin> denies Action<Admin> of Resource<Arts and sciences>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.adminString, this.artsAndSciences, PermissionAllowed.DISALLOWED);

    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);

    //Result:
    //
    //User subj0 is allowed Action<Read> of Resource<Math> since there are only inherited assignments 
    // and the one with the lower depth (tie in resource, Read/Write is lower than Action<Admin>)
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.math).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    boolean isDisallowed1 = permissionEntries.get(0).isDisallowed();
    boolean isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);
    
    //if we filter permissions, should be one
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS)
        .addPermissionName(this.math).findPermissions());
        
    //there should be one, one should be disallow
    assertEquals(1, GrouperUtil.length(permissionEntries));

    assertFalse(permissionEntries.get(0).isDisallowed());

    //if we filter by role, it should find the disallow
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
        .addPermissionName(this.math).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    
    assertFalse(permissionEntries.get(0).isDisallowed());
    
    assertFalse("wrong subject", new PermissionFinder().addSubject(SubjectTestHelper.SUBJ1)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("tie", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.math).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.math).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.user)
        .addPermissionName(this.math).hasPermission());
        
    assertFalse("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.math).hasPermission());
        
    assertTrue("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.electricalEngineering).hasPermission());
        
    assertFalse("directish", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.artsAndSciences).hasPermission());
        
    assertFalse("is all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.all).hasPermission());
        
    assertTrue("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.electricalEngineering).hasPermission());
        
    assertFalse("wrong action", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readWriteString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.math).hasPermission());
        
  }


  
  /**
   * 
   */
  public void testResourceDirectedGraphPriorityWithTie() {

    //Role<Admin> allows Action<Read> of Resource<Engineering>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readString, this.engineering, PermissionAllowed.ALLOWED);

    //Role<Admin> denies Action<Read> of Resource<Arts and sciences>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readString, this.artsAndSciences, PermissionAllowed.DISALLOWED);

    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);

    //
    //Result:
    //
    //User subj0 is allowed Action<Read> of Resource<Math> since there are only inherited assignments with the same depth and one is ALLOW
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.math).findPermissions());

    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    boolean isDisallowed1 = permissionEntries.get(0).isDisallowed();
    boolean isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);
    
    //if we filter permissions, should be one
    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS)
        .addPermissionName(this.math).findPermissions());

    //there should be one, one should be disallow
    assertEquals(1, GrouperUtil.length(permissionEntries));

    assertFalse(permissionEntries.get(0).isDisallowed());

    //if we filter by role, it should find the disallow
    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
          .addPermissionName(this.math).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    
    assertFalse(permissionEntries.get(0).isDisallowed());
    
    assertFalse("wrong subject", 
        new PermissionFinder().addSubject(SubjectTestHelper.SUBJ1)
          .addAction(this.readString).addRole(this.adminRole)
          .addPermissionName(this.english).hasPermission());

    assertTrue("tie", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.math).hasPermission());

    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.math).hasPermission());

    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.user)
        .addPermissionName(this.math).hasPermission());

    assertFalse("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());

    assertTrue("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.math).hasPermission());

    assertTrue("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.electricalEngineering).hasPermission());

    assertFalse("direct", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.artsAndSciences).hasPermission());

    assertFalse("is all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.all).hasPermission());

    assertTrue("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.electricalEngineering).hasPermission());

    assertFalse("wrong action", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readWriteString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());

    assertTrue("inherits from engineering", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.math).hasPermission());

  }
  
  /**
   * 
   */
  public void testResourceDirectedGraphPriority() {

    //Role<Admin> allows Action<Read> of Resource<All>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readString, this.all, PermissionAllowed.ALLOWED);

    //Role<Admin> denies Action<Read> of Resource<Arts and sciences>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readString, this.artsAndSciences, PermissionAllowed.DISALLOWED);

    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);

    //
    //Result:
    //
    //User subj0 is denied Action<Read> of Resource<English> and Resource<Math> since there are only inherited assignments and the ones with lower depth have priority
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.artsAndSciences).findPermissions());
    

    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    boolean isDisallowed1 = permissionEntries.get(0).isDisallowed();
    boolean isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);
    
    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.english).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    isDisallowed1 = permissionEntries.get(0).isDisallowed();
    isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);
    
    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.math).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    isDisallowed1 = permissionEntries.get(0).isDisallowed();
    isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);

    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.electricalEngineering).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    isDisallowed1 = permissionEntries.get(0).isDisallowed();
    assertFalse(isDisallowed1);

    //if we filter permissions, should be one
    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS)
        .addPermissionName(this.english).findPermissions());
    
    //there should be one, one should be disallow
    assertEquals(1, GrouperUtil.length(permissionEntries));

    assertTrue(permissionEntries.get(0).isDisallowed());

    //if we filter by role, it should find the disallow
    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
        .addPermissionName(this.english).findPermissions());
    
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    
    assertTrue(permissionEntries.get(0).isDisallowed());
    
    assertFalse("wrong subject", new PermissionFinder().addSubject(SubjectTestHelper.SUBJ0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.user)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("inherits from arts and sciences", 
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("direct", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.artsAndSciences).hasPermission());
        
    assertTrue("is all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.all).hasPermission());
        
    assertTrue("inherits from all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.electricalEngineering).hasPermission());
        
    assertFalse("wrong action", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readWriteString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.math).hasPermission());
        
  }

  /**
   * 
   */
  public void testFindPermissionsInStem() {

    //      Assignments:
    //
    //      Role<Admin> allows: Action<Read> of Resource<All>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readString, this.all, PermissionAllowed.ALLOWED);
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);

    {
      //################# SUBJECT PERMISSIONS
      List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
          .addPermissionDef(this.permissionDef).findPermissions());
      
      //this is all
      assertTrue(GrouperUtil.length(permissionEntries) + "", GrouperUtil.length(permissionEntries) >= 7);
      
      Stem engineering = StemFinder.findByName(grouperSession, "top:engineering", true);
      
      //find in a stem
      permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
          .assignPermissionNameFolder(engineering).assignPermissionNameFolderScope(Scope.ONE)
          .addPermissionDef(this.permissionDef).findPermissions());
      
      //only chemical
      assertEquals(1, GrouperUtil.length(permissionEntries));
      
      assertTrue(permissionEntries.get(0).getAttributeDefNameName(), permissionEntries.get(0).getAttributeDefNameName().toLowerCase().contains("chemical"));
      
      //find in a stem sub
      permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
          .assignPermissionNameFolder(engineering).assignPermissionNameFolderScope(Scope.SUB)
          .addPermissionDef(this.permissionDef).findPermissions());
      
      //only chemical and electrical
      assertEquals(2, GrouperUtil.length(permissionEntries));
      
      assertTrue(permissionEntries.get(0).getAttributeDefNameName(), permissionEntries.get(0).getAttributeDefNameName().toLowerCase().contains("chemical"));
      assertTrue(permissionEntries.get(1).getAttributeDefNameName(), permissionEntries.get(1).getAttributeDefNameName().toLowerCase().contains("electrical"));
    }

    {
      //################# ROLE PERMISSIONS
      List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder()
        .addRole(this.adminRole).assignPermissionType(PermissionType.role)
        .addAction(this.readString)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addPermissionDef(this.permissionDef).findPermissions());
      
      //this is all
      assertTrue(GrouperUtil.length(permissionEntries) + "", GrouperUtil.length(permissionEntries) >= 7);
      
      Stem engineering = StemFinder.findByName(grouperSession, "top:engineering", true);
      
      //find in a stem
      permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder()
          .addAction(this.readString).assignPermissionType(PermissionType.role)
          .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
          .assignPermissionNameFolder(engineering).assignPermissionNameFolderScope(Scope.ONE)
          .addPermissionDef(this.permissionDef).findPermissions());
      
      //only chemical
      assertEquals(1, GrouperUtil.length(permissionEntries));
      
      assertTrue(permissionEntries.get(0).getAttributeDefNameName(), permissionEntries.get(0).getAttributeDefNameName().toLowerCase().contains("chemical"));
      
      //find in a stem sub
      permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder()
          .addRole(this.adminRole).addAction(this.readString).assignPermissionType(PermissionType.role)
          .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
          .assignPermissionNameFolder(engineering).assignPermissionNameFolderScope(Scope.SUB)
          .addPermissionDef(this.permissionDef).findPermissions());
      
      //only chemical and electrical
      assertEquals(2, GrouperUtil.length(permissionEntries));
      
      assertTrue(permissionEntries.get(0).getAttributeDefNameName(), permissionEntries.get(0).getAttributeDefNameName().toLowerCase().contains("chemical"));
      assertTrue(permissionEntries.get(1).getAttributeDefNameName(), permissionEntries.get(1).getAttributeDefNameName().toLowerCase().contains("electrical"));
    }
    
  }
  
//  Role assignment vs individual assignment
//
//  Assignments:
//
//  Role<Admin> allows: Action<Read> of Resource<Arts and sciences>
//
//  User jsmith is assigned Role<Admin>
//
//  User jsmith is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
//
//  Result:
//
//  jsmith is not allowed to Read Arts and sciences (overall, or role specific) since an individual assignment trumps a generic role assignment
  
  
  /**
   * 
   */
  public void testRoleAssignmentVsIndividualAssignmentHierarchy2() {
    
    //Role<Admin> allows: Action<Read> of Resource<Arts and sciences>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readString, this.artsAndSciences, PermissionAllowed.ALLOWED);
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);

    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.DISALLOWED);
    
    //Result:
    //
    //subj0 is not allowed to Read Arts and sciences (overall, or role specific) since an individual assignment trumps a generic role assignment
    
    //lets get all of the permission assignments
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    boolean isDisallowed1 = permissionEntries.get(0).isDisallowed();
    boolean isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);
    
    //if we filter permissions, should be one
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be one, one should be disallow
    assertEquals(1, GrouperUtil.length(permissionEntries));

    assertTrue(permissionEntries.get(0).isDisallowed());

    //if we filter by role, it should find the disallow
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    
    assertTrue(permissionEntries.get(0).isDisallowed());
    
    assertFalse("wrong subject", new PermissionFinder().addSubject(SubjectTestHelper.SUBJ1)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.user)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("inherits from all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("inherits from all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.artsAndSciences).hasPermission());
        
    assertFalse("is all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.all).hasPermission());
        
    assertFalse("inherits from all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.electricalEngineering).hasPermission());
        
    assertFalse("wrong action", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readWriteString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
  }

  /**
   * 
   */
  public void testRoleAssignmentVsIndividualAssignmentHierarchy() {
    
    //User subj0 is assigned Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readString, this.artsAndSciences, PermissionAllowed.DISALLOWED);
    
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);

    //User subj0 is assigned permission Allow, Action<Read>, Resource<All>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    //Result:
    //
    //subj0 is allowed to Read Arts and sciences (overall, or role specific) since an individual assignment trumps a generic role assignment
    
    //lets get all of the permission assignments
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    boolean isDisallowed1 = permissionEntries.get(0).isDisallowed();
    boolean isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);
    
    //if we filter permissions, should be one
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be one, one should be disallow
    assertEquals(1, GrouperUtil.length(permissionEntries));

    assertFalse(permissionEntries.get(0).isDisallowed());

    //if we filter by role, it should find the disallow
    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
        .addPermissionName(this.artsAndSciences).findPermissions());
    
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    
    assertFalse(permissionEntries.get(0).isDisallowed());
    
    assertFalse("wrong subject", new PermissionFinder().addSubject(SubjectTestHelper.SUBJ1)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("ok", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.user)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("ok", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.artsAndSciences).hasPermission());
        
    assertFalse("is all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.all).hasPermission());
        
    assertFalse("not related", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readWriteString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
  }

  /** 
   * test various role assignments
   */
  public void testVariousRoleAssignments() {
    
    //Role<Admin> allows: Action<Read> of Resource<Arts and sciences>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readString, this.artsAndSciences, PermissionAllowed.ALLOWED);
    
    //
    //Role<User> denies: Action<Read> of Resource<Arts and sciences>
    this.user.getPermissionRoleDelegate().assignRolePermission(this.readString, this.artsAndSciences, PermissionAllowed.DISALLOWED);

    //
    //User subj0 is assigned Role<Admin> and Role<User>
    this.adminRole.addMember(this.subj0, true);
    this.user.addMember(this.subj0, true);
    
    //
    //Result:
    //
    //Overall, subj0 is allowed Arts and sciences since if a user is allowed in any role, they are allowed.
    //
    //If the application supports users acting as a certain role instead of flattening all permissions into one 
    //permissions set (i.e. ability to elevate permissions), then as a User, subj0 cannot Read Arts and Sciences, 
    //but as an Admin, subj0 can Read Arts and Sciences

    assertFalse("wrong subject", new PermissionFinder().addSubject(SubjectTestHelper.SUBJ1)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.user)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("is arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.artsAndSciences).hasPermission());
        
    assertFalse("wrong permission", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.electricalEngineering).hasPermission());
        
    assertFalse("wrong action", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readWriteString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    //lets get all of the permission assignments
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    boolean isDisallowed1 = permissionEntries.get(0).isDisallowed();
    boolean isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue("" + isDisallowed1, isDisallowed1 != isDisallowed2);
    
    //if we filter permissions, should still be two
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    
    //if we filter by role, it should find the allow
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
        .addPermissionName(this.math).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    
    assertTrue(!permissionEntries.get(0).isDisallowed());

    
//    Set<PermissionEntry> permissionEntriesSet = GrouperDAOFactory.getFactory().getPermissionEntry().findPermissions(
//        GrouperUtil.toSet(attributeDef.getId()), null, null, null, null, null);
//    List<PermissionEntry> permissionEntriesList = new ArrayList<PermissionEntry>(permissionEntriesSet);
//    Collections.sort(permissionEntriesList);
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
//    permissionEntry = PermissionEntry.collectionFindFirst(permissionEntriesList, "top:roleChild", "top:attrDefNameChild", "actionChild", "jdbc", "test.subject.3", "role");
//    assertPermission(permissionEntry, "role", true, false, 0, 1, 1, 1);

    
    
  }
  
  /**
   * test role inheritance
   */
  public void testRoleInheritance() {
    //Role<Admin> denies: Action<Read> of Resource<Arts and sciences>
    this.adminRole.getPermissionRoleDelegate().assignRolePermission(this.readString, this.artsAndSciences, PermissionAllowed.DISALLOWED);

    //Role<Senior admin> allows: Action<Read> of Resource<All>
    this.seniorAdmin.getPermissionRoleDelegate().assignRolePermission(this.readString, this.all, PermissionAllowed.ALLOWED);
    
    //User subj0 is assigned Role<Senior admin>
    this.seniorAdmin.addMember(this.subj0, true);
    
    //
    //Result:
    //
    //Overall, subj0 is allowed Action<Read> of Resource<Arts and sciences> since the subject is assigned 
    //directly to Senior admin, it will trump inherited role assignments
    
    //lets get all of the permission assignments
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(2, GrouperUtil.length(permissionEntries));
    boolean isDisallowed1 = permissionEntries.get(0).isDisallowed();
    boolean isDisallowed2 = permissionEntries.get(1).isDisallowed();
    assertTrue(isDisallowed1 != isDisallowed2);
    
    //if we filter permissions, should be one
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be one, one should be allow
    assertEquals(1, GrouperUtil.length(permissionEntries));

    assertTrue(!permissionEntries.get(0).isDisallowed());

    //if we filter by role, it should find the allow
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES)
        .addPermissionName(this.artsAndSciences).findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    
    assertTrue(!permissionEntries.get(0).isDisallowed());
    
    assertFalse("wrong subject", new PermissionFinder().addSubject(SubjectTestHelper.SUBJ1)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("inherits from arts and sciences", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertFalse("wrong role", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.adminRole)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("inherits from all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.english).hasPermission());
        
    assertTrue("inherits from all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.artsAndSciences).hasPermission());
        
    assertTrue("is all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.all).hasPermission());
        
    assertTrue("inherits from all", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addRole(this.seniorAdmin)
        .addPermissionName(this.electricalEngineering).hasPermission());
        
    assertFalse("wrong action", new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readWriteString).addRole(this.seniorAdmin)
        .addPermissionName(this.english).hasPermission());
        
  }
  
  /**
   * 
   */
  public void testNotChangeDisallow() {

    //Role<Admin> allows: Action<Read> of Resource<Arts and sciences>
    AttributeAssign attributeAssign = this.adminRole.getPermissionRoleDelegate()
      .assignRolePermission("read", this.artsAndSciences, PermissionAllowed.ALLOWED).getAttributeAssign();
    
    //prove that we can do saves and updates
    attributeAssign.setDisabledTime(GrouperUtil.toTimestamp("2010/01/01"));
    attributeAssign.saveOrUpdate();
    
    attributeAssign.setDisabledTime(null);
    attributeAssign.saveOrUpdate();
    
    //try to change the disallow flag
    attributeAssign.setDisallowed(true);
    
    try {
      attributeAssign.saveOrUpdate();
      fail("Shouldnt be allowed");
    } catch (Exception e) {
      //good
    }
    

  }
  
}
