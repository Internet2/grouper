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
 * 
 */
package edu.internet2.middleware.grouper.permissions.limits.impl;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.subject.Subject;


/**
 * @author mchyzer
 *
 */
public class PermissionLimitAmountLogicTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionLimitAmountLogicTest("testLessThanOrEqual"));
  }

  /** admin role */
  private Role adminRole;
  /** english */
  private AttributeDefName artsAndSciences;
  /** english */
  private AttributeDefName english;
  /** grouper session */
  private GrouperSession grouperSession;
  /** attribute def */
  private AttributeDef permissionDef;
  /** read */
  private String readString = "read";
  /** subj0 */
  private Subject subj0;

  /**
   * 
   */
  public PermissionLimitAmountLogicTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public PermissionLimitAmountLogicTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    new StemSave(this.grouperSession).assignName("top").assignDisplayExtension("top display name").save();
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");
  
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
    this.adminRole = new GroupSave(this.grouperSession).assignName("top:admin").assignTypeOfGroup(TypeOfGroup.role).save();
    
    this.permissionDef = new AttributeDefSave(this.grouperSession).assignName("top:permissionDef")
      .assignAttributeDefType(AttributeDefType.perm).assignToEffMembership(true).assignToGroup(true).save();
    this.english = new AttributeDefNameSave(this.grouperSession, this.permissionDef).assignName("top:english").assignDisplayExtension("English").save();
    this.artsAndSciences = new AttributeDefNameSave(this.grouperSession, this.permissionDef).assignName("top:artsAndSciences").assignDisplayExtension("Arts and Sciences").save();
    this.artsAndSciences.getAttributeDefNameSetDelegate().addToAttributeDefNameSet(this.english);
  
    this.permissionDef.getAttributeDefActionDelegate().configureActionList("read");
    
    
    this.permissionDef.getAttributeDefActionDelegate().findAction(this.readString, true);
    
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
  public void testLessThan() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    AttributeAssign attributeAssign = new PermissionFinder().addSubject(this.subj0).addAction(this.readString)
      .addPermissionName(this.artsAndSciences).addRole(this.adminRole).assignImmediateOnly(true).findPermission(true).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValueInteger(
        PermissionLimitUtils.limitAmountLessThanName(), 50000L);
    
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49999").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "50000").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "50001").hasPermission());
    
    try {
      new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .hasPermission();
      fail("Doesnt have amount");
    } catch (Exception e) {
      //good
    }
    
  }

  /**
   * 
   */
  public void testLessThanOrEqual() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    AttributeAssign attributeAssign = new PermissionFinder().addSubject(this.subj0).addAction(this.readString)
      .addPermissionName(this.artsAndSciences).addRole(this.adminRole).assignImmediateOnly(true).findPermission(true).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValueInteger(
        PermissionLimitUtils.limitAmountLessThanOrEqualName(), 50000L);
    
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49999").hasPermission());
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "50000").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "50001").hasPermission());
    
    try {
      new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .hasPermission();
      fail("Doesnt have amount");
    } catch (Exception e) {
      //good
    }
    
  }
}
