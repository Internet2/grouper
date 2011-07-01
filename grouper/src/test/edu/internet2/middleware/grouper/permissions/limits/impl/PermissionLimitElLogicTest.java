/**
 * 
 */
package edu.internet2.middleware.grouper.permissions.limits.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
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
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignAction;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignResult;
import edu.internet2.middleware.grouper.cfg.ApiConfig;
import edu.internet2.middleware.grouper.exception.ExpressionLanguageMissingVariableException;
import edu.internet2.middleware.grouper.group.TypeOfGroup;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.permissions.PermissionAllowed;
import edu.internet2.middleware.grouper.permissions.PermissionEntry;
import edu.internet2.middleware.grouper.permissions.PermissionFinder;
import edu.internet2.middleware.grouper.permissions.PermissionProcessor;
import edu.internet2.middleware.grouper.permissions.limits.PermissionLimitUtils;
import edu.internet2.middleware.grouper.permissions.role.Role;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.subject.Subject;


/**
 * @author mchyzer
 *
 */
public class PermissionLimitElLogicTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionLimitElLogicTest("testIpAddressFromConfig"));
  }

  /** admin role */
  private Role adminRole;
  /** admin */
  private String adminString = "admin";
  /** english */
  private AttributeDefName all;
  /** english */
  private AttributeDefName artsAndSciences;
  /** english */
  private AttributeDefName chemicalEngineering;
  /** english */
  private AttributeDefName electricalEngineering;
  /** english */
  private AttributeDefName engineering;
  /** english */
  private AttributeDefName english;
  /** grouper session */
  private GrouperSession grouperSession;
  /** english */
  private AttributeDefName math;
  /** attribute def */
  private AttributeDef permissionDef;
  /** read */
  private String readString = "read";
  /** readWrite */
  private String readWriteString = "readWrite";
  /** root stem */
  private Stem root;
  /** senior admin */
  private Role seniorAdmin;
  /** subj0 */
  private Subject subj0;
  /** top stem */
  private Stem top;
  /** write */
  private String writeString = "write";

  /**
   * 
   */
  public PermissionLimitElLogicTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public PermissionLimitElLogicTest(String name) {
    super(name);
  }

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
  public void testAmount() {
    
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

    attributeAssign.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), "amount < 50000 && amount2 < 23");

    //what if you check without the variable, should be an error
    try {
      new PermissionFinder().addSubject(this.subj0).addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS).hasPermission();
      fail("Shouldnt get here");
    } catch (ExpressionLanguageMissingVariableException e) {
      assertEquals("variable 'amount' is not defined in script: 'amount < 50000 && amount2 < 23'", e.getMessage());
      //good
    }
  }

  /**
   * 
   */
  public void testTime() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    AttributeAssign attributeAssign = new PermissionFinder().addSubject(this.subj0).addAction(this.readString)
      .addPermissionName(this.artsAndSciences).addRole(this.adminRole).assignImmediateOnly(true).findPermission(true).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), "hourOfDay >= 9 && hourOfDay <= 17");
    
    //there should be two, one should be allow, the other deny
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)hourOfDay", "10").hasPermission());
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)hourOfDay", "9").hasPermission());
  
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)hourOfDay", "8").hasPermission());
  
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)hourOfDay", "16").hasPermission());
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)hourOfDay", "17").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("(int)hourOfDay", "18").hasPermission());
    
    //this should not throw an exception since hourOfDay is built in
    Calendar calendar = new GregorianCalendar();
    int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
    assertEquals(hourOfDay >= 9 && hourOfDay <= 17, new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .hasPermission());
    
    
    
    
  }

  /**
   * 
   */
  public void testIpAddress() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    AttributeAssign attributeAssign = new PermissionFinder().addSubject(this.subj0).addAction(this.readString)
      .addPermissionName(this.artsAndSciences).addRole(this.adminRole).assignImmediateOnly(true).findPermission(true).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), 
        "limitElUtils.ipOnNetwork(ipAddress, '1.2.3.0', 24)");
    
    //there should be two, one should be allow, the other deny
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "1.2.3.40").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "1.2.41.127").hasPermission());
    
    try {
      new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .hasPermission();
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    
    
  }

  /**
   * 
   */
  public void testIpAddresses() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    AttributeAssign attributeAssign = new PermissionFinder().addSubject(this.subj0).addAction(this.readString)
      .addPermissionName(this.artsAndSciences).addRole(this.adminRole).assignImmediateOnly(true).findPermission(true).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), 
        "limitElUtils.ipOnNetworks(ipAddress, '1.2.3.0/24, 2.3.4.0/16')");
    
    //there should be two, one should be allow, the other deny
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "1.2.3.40").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "1.2.41.127").hasPermission());
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "2.3.205.127").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "2.4.249.2").hasPermission());
    
    try {
      new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .hasPermission();
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    
    
  }

  /**
   * 
   */
  public void testIpAddressFromConfig() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    AttributeAssign attributeAssign = new PermissionFinder().addSubject(this.subj0).addAction(this.readString)
      .addPermissionName(this.artsAndSciences).addRole(this.adminRole).assignImmediateOnly(true).findPermission(true).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(PermissionLimitUtils.limitElAttributeDefName().getName(), 
        "limitElUtils.ipOnNetworkRealm(ipAddress, 'myInstitutionLocal')");
    
    ApiConfig.testConfig.put("grouper.permissions.limits.realm.myInstitutionLocal", "4.5.6.0/24, 6.7.0.0/16");
    
    //there should be two, one should be allow, the other deny
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "4.5.6.40").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "4.5.7.145").hasPermission());
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "6.7.6.54").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "6.8.1.2").hasPermission());
    
    try {
      new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .hasPermission();
      fail("shouldnt get here");
    } catch (Exception e) {
      //good
    }
    
    
    
  }
}
