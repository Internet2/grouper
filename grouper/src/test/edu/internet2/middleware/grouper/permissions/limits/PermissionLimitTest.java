/**
 * @author mchyzer
 * $Id: PermissionDisallowTest.java 7378 2011-06-20 06:11:02Z mchyzer $
 */
package edu.internet2.middleware.grouper.permissions.limits;

import java.util.ArrayList;
import java.util.List;

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
import edu.internet2.middleware.grouper.cfg.ApiConfig;
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
    TestRunner.run(new PermissionLimitTest("testLimitCache"));
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

    attributeAssign.getAttributeValueDelegate().assignValue(limitName.getName(), "amount < 30000");
    
    try {
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
    ApiConfig.testConfig.put("grouper.permissions.limits.logic.customEl.limitName", limitName.getName());
    ApiConfig.testConfig.put("grouper.permissions.limits.logic.customEl.logicClass", PermissionLimitElLogic.class.getName());
    
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

  
}
