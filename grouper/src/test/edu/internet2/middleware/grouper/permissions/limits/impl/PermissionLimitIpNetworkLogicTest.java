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
import edu.internet2.middleware.grouper.cfg.ApiConfig;
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
public class PermissionLimitIpNetworkLogicTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new PermissionLimitIpNetworkLogicTest("testIpNetworkRealm"));
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
  public PermissionLimitIpNetworkLogicTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public PermissionLimitIpNetworkLogicTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void setUp() {
    super.setUp();
    this.grouperSession = GrouperSession.start(SubjectFinder.findRootSubject());
    new StemSave(this.grouperSession).assignName("top").assignDisplayExtension("top display name").save();
  
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrAdmin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptin", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrOptout", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrRead", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrUpdate", "false");
    ApiConfig.testConfig.put("attributeDefs.create.grant.all.attrView", "false");
  
    ApiConfig.testConfig.put("groups.create.grant.all.read", "false");
    ApiConfig.testConfig.put("groups.create.grant.all.view", "false");
    
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
  public void testIpNetworks() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    AttributeAssign attributeAssign = new PermissionFinder().addSubject(this.subj0).addAction(this.readString)
      .addPermissionName(this.artsAndSciences).addRole(this.adminRole).assignImmediateOnly(true).findPermission(true).getAttributeAssign();
    
    attributeAssign.getAttributeValueDelegate().assignValue(
        PermissionLimitUtils.limitIpOnNetworksName(), "1.2.3.0/24, 2.3.4.0/16");
    
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
  public void testIpNetworkRealm() {
    
    //
    //User subj0 is assigned Role<Admin>
    this.adminRole.addMember(this.subj0, true);
  
    //
    //User subj0 is assigned permission Deny, Action<Read>, Resource<Arts and sciences>, in the context of Role<Admin>
    this.adminRole.getPermissionRoleDelegate().assignSubjectRolePermission(
        this.readString, this.artsAndSciences, this.subj0, PermissionAllowed.ALLOWED);
    
    AttributeAssign attributeAssign = new PermissionFinder().addSubject(this.subj0).addAction(this.readString)
      .addPermissionName(this.artsAndSciences).addRole(this.adminRole).assignImmediateOnly(true).findPermission(true).getAttributeAssign();
    
    ApiConfig.testConfig.put("grouper.permissions.limits.realm.myInstitutionLocal2", "4.1.6.0/24, 6.1.0.0/16");

    attributeAssign.getAttributeValueDelegate().assignValue(
        PermissionLimitUtils.limitIpOnNetworkRealmName(), "myInstitutionLocal2");
    
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "4.1.6.40").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "4.1.5.127").hasPermission());
    assertTrue(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "6.1.205.127").hasPermission());
    assertFalse(new PermissionFinder().addSubject(this.subj0)
        .addAction(this.readString).addPermissionName(this.english)
        .assignPermissionProcessor(PermissionProcessor.FILTER_REDUNDANT_PERMISSIONS_AND_ROLES_AND_PROCESS_LIMITS)
        .addLimitEnvVar("ipAddress", "6.3.249.2").hasPermission());
    
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
