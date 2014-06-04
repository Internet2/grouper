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
 * $Id: PermissionDisallowTest.java 7378 2011-06-20 06:11:02Z mchyzer $
 */
package edu.internet2.middleware.grouper.permissions.limits;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefType;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.GroupMember;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.permissions.limits.impl.PermissionLimitElLogic;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 *
 */
public class PermissionLimitTest extends GrouperTest {
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionLimitTest("testLimitCustom"));
  }

  /**
   * 
   */
  public PermissionLimitTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public PermissionLimitTest(String name) {
    super(name);
  }

  /** grouper session */
  private GrouperSession grouperSession;

  /** root stem */
  private Stem root;

  /** top stem */
  private Stem top;

  /** admin role */
  private Role adminRole;

  /** senior admin */
  private Role seniorAdmin;
  
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
  
  /** subj1 */
  private Subject subj1;
  
  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    this.root = StemFinder.findRootStem(this.grouperSession);
    this.top = new StemSave(this.grouperSession).assignName("top").assignDisplayExtension("top display name").save();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrAdmin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptin", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrOptout", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrRead", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrUpdate", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("attributeDefs.create.grant.all.attrView", "false");

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.read", "false");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groups.create.grant.all.view", "false");
    
    this.adminRole = new GroupSave(this.grouperSession).assignName("top:admin").assignTypeOfGroup(TypeOfGroup.role).save();
    
    this.seniorAdmin = new GroupSave(this.grouperSession).assignName("top:seniorAdmin").assignTypeOfGroup(TypeOfGroup.role).save();
    
    //senior admin inherits from admin
    this.seniorAdmin.getRoleInheritanceDelegate().addRoleToInheritFromThis(this.adminRole);
    
    this.permissionDef = new AttributeDefSave(this.grouperSession).assignName("top:permissionDef")
      .assignAttributeDefType(AttributeDefType.perm).assignToEffMembership(true).assignToGroup(true).save();
    this.english = new AttributeDefNameSave(this.grouperSession, this.permissionDef).assignName("top:english").assignDisplayExtension("English").save();
    this.math = new AttributeDefNameSave(this.grouperSession, this.permissionDef).assignName("top:math").assignDisplayExtension("Math").save();
    this.electricalEngineering = new AttributeDefNameSave(this.grouperSession, this.permissionDef).assignName("top:electricalEngineering").assignDisplayExtension("Electrical Engineering").save();
    this.chemicalEngineering = new AttributeDefNameSave(this.grouperSession, this.permissionDef).assignName("top:chemicalEngineering").assignDisplayExtension("Chemical Engineering").save();
    this.artsAndSciences = new AttributeDefNameSave(this.grouperSession, this.permissionDef).assignName("top:artsAndSciences").assignDisplayExtension("Arts and Sciences").save();
    this.engineering = new AttributeDefNameSave(this.grouperSession, this.permissionDef).assignName("top:engineering").assignDisplayExtension("Engineering").save();
    this.all = new AttributeDefNameSave(this.grouperSession, this.permissionDef).assignName("top:all").assignDisplayExtension("All").save();

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
    this.subj1 = SubjectTestHelper.SUBJ1;
    
    PermissionLimitUtils.clearLimitLogicMap();
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
  public void testLimit() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);

    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    AttributeAssignResult attributeAssignResult = this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);

    
    //Result:
    //
    //subj0 can read except for the limit
    
    //lets get all of the permission assignments
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49000").findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertTrue(permissionEntries.get(0).isAllowedOverall());

    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), "amount < 50000");
    
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49000").findPermissions());
        
    
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertTrue(permissionEntries.get(0).isAllowedOverall());

    //there should be a limit...
    Map<PermissionEntry, Set<PermissionLimitBean>> permissionEntryLimitBeanMap = new PermissionFinder().addSubject(this.subj0)
      .addAction(this.readString).addPermissionName(this.english)
      .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
      .addLimitEnvVar("(int)amount", "49000").findPermissionsAndLimits();

    assertEquals(1, GrouperUtil.length(permissionEntryLimitBeanMap));
    
    Set<PermissionLimitBean> permissionLimitBeans = permissionEntryLimitBeanMap.values().iterator().next();
    
    assertEquals(1, GrouperUtil.length(permissionLimitBeans));
    
    PermissionLimitBean permissionLimitBean = permissionLimitBeans.iterator().next();
    
    assertEquals(AttributeAssignType.any_mem_asgn, permissionLimitBean.getLimitAssign().getAttributeAssignType());
    assertEquals(1, GrouperUtil.length(permissionLimitBean.getLimitAssignValues()));
    assertEquals("amount < 50000", permissionLimitBean.getLimitAssignValues().iterator().next().getValueString());
    
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "51000").findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertFalse(permissionEntries.get(0).isAllowedOverall());

  }

  /**
   * 
   */
  public void testLimitCustom() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    AttributeAssignResult attributeAssignResult = this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
  
    
    //Result:
    //
    //subj0 can read except for the limit
    
    //lets get all of the permission assignments
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49000").findPermissions());
            
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertTrue(permissionEntries.get(0).isAllowedOverall());
  
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();

    
    AttributeDef limitDef = new AttributeDefSave(this.grouperSession).assignName("top:limitDef")
      .assignAttributeDefType(AttributeDefType.limit).assignValueType(AttributeDefValueType.string).assignMultiAssignable(false)
      .assignToEffMembershipAssn(true).save();
    AttributeDefName limitName = new AttributeDefNameSave(this.grouperSession, limitDef).assignName("top:customElLimit")
      .assignDisplayExtension("Custom EL Limit").save();
    
    try {
      attributeAssign.getAttributeValueDelegate().assignValue(limitName.getName(), "amount < 30000");

      permissionEntries = new ArrayList<PermissionEntry>(
          new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).addPermissionName(this.english)
          .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
          .addLimitEnvVar("(int)amount", "29000").findPermissions());
          
      fail("Shouldnt make it this far, not associated with logic class");
    } catch (RuntimeException re) {
      //good
    }
    
    //# permission limits linked to subclasses of edu.internet2.middleware.grouper.permissions.limits.PermissionLimitBase
    //grouper.permissions.limits.logic.someName.limitName = 
    //grouper.permissions.limits.logic.someName.logicClass = 

    //associate the class
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.customEl.limitName", limitName.getName());
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.permissions.limits.logic.customEl.logicClass", PermissionLimitElLogic.class.getName());
    
    //this is still cached
    try {
      permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).addPermissionName(this.english)
          .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
          .addLimitEnvVar("(int)amount", "29000").findPermissions());
          
      fail("Shouldnt make it this far, not associated with logic class");
    } catch (RuntimeException re) {
      //good
    }
    
    //clear the cache
    PermissionLimitUtils.limitLogicMap.clear();

    attributeAssign.getAttributeValueDelegate().assignValue(limitName.getName(), "amount < 30000");

    permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "29000").findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertTrue(permissionEntries.get(0).isAllowedOverall());
  
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "31000").findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertFalse(permissionEntries.get(0).isAllowedOverall());
  
  }

  /**
   * 
   */
  public void testLimitCache() {
    
    try {
      //
      //User subj0 is assigned Role<Admin>
      this.adminRole.addMember(this.subj0, true);
    
      //
      //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
      AttributeAssignResult attributeAssignResult = this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
          this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
      
      //Result:
      //
      //subj0 can read except for the limit
      
      //lets get all of the permission assignments
      List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(
          new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).addPermissionName(this.english)
          .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
          .addLimitEnvVar("(int)amount", "49000").findPermissions());
          
      //there should be two, one should be allow, the other deny
      assertEquals(1, GrouperUtil.length(permissionEntries));
      assertFalse(permissionEntries.get(0).isDisallowed());
      assertTrue(permissionEntries.get(0).isAllowedOverall());
    
      AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
      
      attributeAssign.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), "amount < 50000");
      
      permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).addPermissionName(this.english)
          .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
          .addLimitEnvVar("(int)amount", "51000").findPermissions());
          
      //there should be two, one should be allow, the other deny
      assertEquals(1, GrouperUtil.length(permissionEntries));
      assertFalse(permissionEntries.get(0).isDisallowed());
      assertFalse(permissionEntries.get(0).isAllowedOverall());
      
      int timesCalled = PermissionLimitElLogic.testingTimesCalledLogic;
      
      permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).addPermissionName(this.english)
          .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
          .addLimitEnvVar("(int)amount", "51000").findPermissions());
      
      //there should be two, one should be allow, the other deny
      assertEquals(1, GrouperUtil.length(permissionEntries));
      assertFalse(permissionEntries.get(0).isDisallowed());
      assertFalse(permissionEntries.get(0).isAllowedOverall());
  
      //this was cached
      assertEquals(timesCalled, PermissionLimitElLogic.testingTimesCalledLogic);
      
      //lets change the cache settings
      PermissionLimitElLogic.testingCacheMinutesInt = 1;
      
      //wait 61 seconds
      GrouperUtil.sleep(61000);
      
      permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).addPermissionName(this.english)
          .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
          .addLimitEnvVar("(int)amount", "51000").findPermissions());
          
      //there should be two, one should be allow, the other deny
      assertEquals(1, GrouperUtil.length(permissionEntries));
      assertFalse(permissionEntries.get(0).isDisallowed());
      assertFalse(permissionEntries.get(0).isAllowedOverall());
  
      assertEquals(timesCalled + 1, PermissionLimitElLogic.testingTimesCalledLogic);
      
      PermissionLimitElLogic.testingCacheMinutesInt = 0;
      
      permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
          .addAction(this.readString).addPermissionName(this.english)
          .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
          .addLimitEnvVar("(int)amount", "51000").findPermissions());
          
      //there should be two, one should be allow, the other deny
      assertEquals(1, GrouperUtil.length(permissionEntries));
      assertFalse(permissionEntries.get(0).isDisallowed());
      assertFalse(permissionEntries.get(0).isAllowedOverall());
  
      //turned off the cache, so it shouldnt cache
      assertEquals(timesCalled + 2, PermissionLimitElLogic.testingTimesCalledLogic);
    } finally {
      
      //reset
      PermissionLimitElLogic.testingCacheMinutesInt = null;
    }
  }

  /**
   * 
   */
  public void testLimitRole() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    //Result:
    //
    //subj0 can read except for the limit
    
    assertTrue(new PermissionFinder().addAction(this.readString).addPermissionName(this.english)
        .addSubject(this.subj0).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .hasPermission());
  
    this.adminRole.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), "amount < 50000");
    
    //there should be a limit...
    Map<PermissionEntry, Set<PermissionLimitBean>> permissionEntryLimitBeanMap = new PermissionFinder().addSubject(this.subj0)
      .addAction(this.readString).addPermissionName(this.english)
      .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
      .addLimitEnvVar("(int)amount", "49000").findPermissionsAndLimits();

    assertEquals(1, GrouperUtil.length(permissionEntryLimitBeanMap));
    
    Set<PermissionLimitBean> permissionLimitBeans = permissionEntryLimitBeanMap.values().iterator().next();
    
    assertEquals(1, GrouperUtil.length(permissionLimitBeans));
    
    PermissionLimitBean permissionLimitBean = permissionLimitBeans.iterator().next();
    
    assertEquals(AttributeAssignType.group, permissionLimitBean.getLimitAssign().getAttributeAssignType());
    assertEquals(1, GrouperUtil.length(permissionLimitBean.getLimitAssignValues()));
    assertEquals("amount < 50000", permissionLimitBean.getLimitAssignValues().iterator().next().getValueString());
    

    assertTrue(new PermissionFinder().addAction(this.readString).addPermissionName(this.english)
        .addSubject(this.subj0).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49000").hasPermission());
    assertFalse(new PermissionFinder().addAction(this.readString).addPermissionName(this.english)
        .addSubject(this.subj0).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "51000").hasPermission());

  
  }

  
  /**
   * 
   */
  public void testLimitMembership() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
    this.adminRole.addMember(this.subj1, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj1, PermissionAllowed.ALLOWED);
    
    //Result:
    //
    //subj0 can read except for the limit
    assertTrue(new PermissionFinder().addAction(this.readString).addPermissionName(this.english)
        .addSubject(this.subj0).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .hasPermission());
    assertTrue(new PermissionFinder().addAction(this.readString).addPermissionName(this.english)
        .addSubject(this.subj1).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .hasPermission());
  
    GroupMember groupMember = new GroupMember(this.adminRole, this.subj0);
    groupMember.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), "amount < 50000");
    
    //there should be a limit...
    Map<PermissionEntry, Set<PermissionLimitBean>> permissionEntryLimitBeanMap = new PermissionFinder().addSubject(this.subj0)
      .addAction(this.readString).addPermissionName(this.english)
      .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
      .addLimitEnvVar("(int)amount", "49000").findPermissionsAndLimits();

    assertEquals(1, GrouperUtil.length(permissionEntryLimitBeanMap));
    
    Set<PermissionLimitBean> permissionLimitBeans = permissionEntryLimitBeanMap.values().iterator().next();
    
    assertEquals(1, GrouperUtil.length(permissionLimitBeans));
    
    PermissionLimitBean permissionLimitBean = permissionLimitBeans.iterator().next();
    
    assertEquals(AttributeAssignType.any_mem, permissionLimitBean.getLimitAssign().getAttributeAssignType());
    assertEquals(1, GrouperUtil.length(permissionLimitBean.getLimitAssignValues()));
    assertEquals("amount < 50000", permissionLimitBean.getLimitAssignValues().iterator().next().getValueString());
    

    
    assertTrue(new PermissionFinder().addAction(this.readString).addPermissionName(this.english)
        .addSubject(this.subj0).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49000").hasPermission());
    assertFalse(new PermissionFinder().addAction(this.readString).addPermissionName(this.english)
        .addSubject(this.subj0).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "51000").hasPermission());
    
    //other users still have the permission
    assertTrue(new PermissionFinder().addAction(this.readString).addPermissionName(this.english)
        .addSubject(this.subj1).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49000").hasPermission());
    assertTrue(new PermissionFinder().addAction(this.readString).addPermissionName(this.english)
        .addSubject(this.subj1).assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "51000").hasPermission());

  
  }

  /**
   * 
   */
  public void testBestLimit() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    AttributeAssignResult attributeAssignResult = this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
  
    
    //Result:
    //
    //subj0 can read except for the limit
    
    //lets get all of the permission assignments
    List<PermissionEntry> permissionEntries = new ArrayList<PermissionEntry>(
        new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49000").findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertTrue(permissionEntries.get(0).isAllowedOverall());
  
    AttributeAssign attributeAssign = attributeAssignResult.getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), "amount < 50000");
    
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49000").findPermissions());
        
    
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertTrue(permissionEntries.get(0).isAllowedOverall());
  
    //there should be a limit...
    Map<PermissionEntry, Set<PermissionLimitBean>> permissionEntryLimitBeanMap = new PermissionFinder().addSubject(this.subj0)
      .addAction(this.readString).addPermissionName(this.english)
      .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
      .addLimitEnvVar("(int)amount", "49000").findPermissionsAndLimits();
  
    assertEquals(1, GrouperUtil.length(permissionEntryLimitBeanMap));
    
    Set<PermissionLimitBean> permissionLimitBeans = permissionEntryLimitBeanMap.values().iterator().next();
    
    assertEquals(1, GrouperUtil.length(permissionLimitBeans));
    
    PermissionLimitBean permissionLimitBean = permissionLimitBeans.iterator().next();
    
    assertEquals(AttributeAssignType.any_mem_asgn, permissionLimitBean.getLimitAssign().getAttributeAssignType());
    assertEquals(1, GrouperUtil.length(permissionLimitBean.getLimitAssignValues()));
    assertEquals("amount < 50000", permissionLimitBean.getLimitAssignValues().iterator().next().getValueString());
    
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "51000").findPermissions());
        
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertFalse(permissionEntries.get(0).isAllowedOverall());
    
    //now lets assign a permission to english
    attributeAssignResult = this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.english, this.subj0, PermissionAllowed.ALLOWED);

    //#####################
    //there shouldnt be a limit there anymore
    permissionEntryLimitBeanMap = new PermissionFinder().addSubject(this.subj0)
      .addAction(this.readString).addPermissionName(this.english)
      .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS)
      .addLimitEnvVar("(int)amount", "49000").findPermissionsAndLimits();
    
    assertEquals(1, GrouperUtil.length(permissionEntryLimitBeanMap));
    
    permissionLimitBeans = permissionEntryLimitBeanMap.values().iterator().next();
    
    assertEquals(0, GrouperUtil.length(permissionLimitBeans));

    //######################
    // add a limit to english, should only have one limit
    attributeAssign = attributeAssignResult.getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), "amount < 30000");
    
    permissionEntries = new ArrayList<PermissionEntry>(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)amount", "49000").findPermissions());
        
    
    //there should be two, one should be allow, the other deny
    assertEquals(1, GrouperUtil.length(permissionEntries));
    assertFalse(permissionEntries.get(0).isDisallowed());
    assertFalse(permissionEntries.get(0).isAllowedOverall());
  
    //there should be a limit...
    permissionEntryLimitBeanMap = new PermissionFinder().addSubject(this.subj0)
      .addAction(this.readString).addPermissionName(this.english)
      .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS)
      .addLimitEnvVar("(int)amount", "49000").findPermissionsAndLimits();
  
    assertEquals(1, GrouperUtil.length(permissionEntryLimitBeanMap));
    
    permissionLimitBeans = permissionEntryLimitBeanMap.values().iterator().next();
    
    assertEquals(1, GrouperUtil.length(permissionLimitBeans));
    
    permissionLimitBean = permissionLimitBeans.iterator().next();
    
    assertEquals(AttributeAssignType.any_mem_asgn, permissionLimitBean.getLimitAssign().getAttributeAssignType());
    assertEquals(1, GrouperUtil.length(permissionLimitBean.getLimitAssignValues()));
    assertEquals("amount < 30000", permissionLimitBean.getLimitAssignValues().iterator().next().getValueString());

    //##########################################################
    //if we have two of the same limit, they should both show up
    attributeAssign.getAttributeDelegate().addAttribute(PermissionLimitUtils.limitWeekday9to5AttributeDefName());
    
    permissionEntryLimitBeanMap = new PermissionFinder().addSubject(this.subj0)
      .addAction(this.readString).addPermissionName(this.english)
      .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_PROCESS_LIMITS)
      .addLimitEnvVar("(int)amount", "49000").findPermissionsAndLimits();
  
    assertEquals(1, GrouperUtil.length(permissionEntryLimitBeanMap));
    
    permissionLimitBeans = permissionEntryLimitBeanMap.values().iterator().next();
    
    assertEquals(2, GrouperUtil.length(permissionLimitBeans));
    
    permissionLimitBean = permissionLimitBeans.iterator().next();
    boolean foundMarker = false;
    boolean foundValue = false;
    for (PermissionLimitBean currentPermissionLimitBean : permissionLimitBeans) {
      assertEquals(AttributeAssignType.any_mem_asgn, currentPermissionLimitBean.getLimitAssign().getAttributeAssignType());
      assertTrue(1 >= GrouperUtil.length(currentPermissionLimitBean.getLimitAssignValues()));
      if (GrouperUtil.length(currentPermissionLimitBean.getLimitAssignValues()) == 1) {
        assertEquals("amount < 30000", currentPermissionLimitBean.getLimitAssignValues().iterator().next().getValueString());
        foundValue = true;
      } else {
        foundMarker = true;
      }
      
    }
    
    assertTrue(foundMarker);
    assertTrue(foundValue);
  }

  
}
