package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import junit.textui.TestRunner;

public class GrouperDeprovisioningJobTest extends GrouperTest {

  /**
   * add config stuff
   */
  @Override
  protected void setupConfigs() {
    //  # if deprovisioning should be enabled
    //  deprovisioning.enable = true
    //
    //  # comma separated realms for deprovisioning e.g. employee, student, etc
    //  # these need to be alphanumeric suitable for properties keys for further config or for group extensions
    //  deprovisioning.realms = 
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.enable", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.realms", "faculty, student");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.realm_faculty.groupNameMeansInRealm", "community:faculty");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.realm_student.groupNameMeansInRealm", "community:student");
  }

  /**
   * 
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDeprovisioningJobTest("testRetrieveRealms"));
  }
  
  /**
   * @param name
   */
  public GrouperDeprovisioningJobTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testRetrieveRealms() {
        
    Map<String, GrouperDeprovisioningRealm> allRealms = GrouperDeprovisioningRealm.retrieveAllRealms();
    
    assertEquals(2, GrouperUtil.length(allRealms));
    
    boolean foundStudent = false;
    boolean foundFaculty = false;
    
    for (GrouperDeprovisioningRealm grouperDeprovisioningRealm : allRealms.values()) {
      if (StringUtils.equals("faculty", grouperDeprovisioningRealm.getLabel())) {
        foundFaculty = true;
        assertEquals("community:faculty", grouperDeprovisioningRealm.getGroupNameMeansInRealm());
      }
      if (StringUtils.equals("student", grouperDeprovisioningRealm.getLabel())) {
        foundStudent = true;
      }
    }
    assertTrue(foundFaculty);
    assertTrue(foundStudent);
    
    assertTrue(allRealms.containsKey("faculty"));
    assertTrue(allRealms.containsKey("student"));
    
    Group facultyManagers = GroupFinder.findByName(GrouperSession.staticGrouperSession(), GrouperDeprovisioningSettings.deprovisioningStemName() + ":managersWhoCanDeprovision_faculty", true);
    
    facultyManagers.addMember(SubjectTestHelper.SUBJ1);
    
    assertEquals(0, GrouperUtil.length(GrouperDeprovisioningRealm.retrieveRealmsForUserManager(SubjectTestHelper.SUBJ2)));
    
    Map<String, GrouperDeprovisioningRealm> realms = GrouperDeprovisioningRealm.retrieveRealmsForUserManager(SubjectTestHelper.SUBJ1);
    
    assertEquals(1, GrouperUtil.length(realms));
    assertEquals("faculty", realms.keySet().iterator().next());
    assertEquals("faculty", realms.get("faculty").getLabel());
    
  }
  
  /**
   * 
   */
  public void testDaemonUpdatingMetadataOnGroups() {
    
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stemTest2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2").save();
    Stem stemTest3 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3").save();
    Group groupDirectAssigned = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2:group0").save();
    Group groupInheritAssigned0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:group1").save();
    Group groupInheritAssigned1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3:group2").save();
    
    
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
    //grouperDeprovisioningConfiguration.se
    
    
  }
  
  /**
   * 
   */
  public void testDaemonUpdatingCalculatedDates() {
    
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Stem stemTest1 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1").save();
    Stem stemTest2 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2").save();
    Stem stemTest3 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3").save();
    Group groupDirectAssigned = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2:group0").save();
    Group groupInheritAssigned0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:group1").save();
    Group groupInheritAssigned1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3:group2").save();

    groupDirectAssigned.addMember(SubjectTestHelper.SUBJ0);
    groupDirectAssigned.addMember(SubjectTestHelper.SUBJ1);

    groupInheritAssigned0.addMember(SubjectTestHelper.SUBJ0);
    groupInheritAssigned0.addMember(SubjectTestHelper.SUBJ2);
    
    groupInheritAssigned1.addMember(SubjectTestHelper.SUBJ0);
    groupInheritAssigned1.addMember(SubjectTestHelper.SUBJ3);

    //lets deprovision a user
    Group deprovisionedUsersGroup = GroupFinder.findByName(grouperSession, GrouperConfig.retrieveConfig().propertyValueString("deprovisioning.group.which.has.been.deprovisioned"), true);

    deprovisionedUsersGroup.addMember(SubjectTestHelper.SUBJ0);
    
    
    
    
//    AttributeDefName attribute = GrouperDeprovisioningJob.retrieveAttributeDefNameValueDef();
//
//    AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(groupDirectAssigned).assignAttributeDefName(attribute).save();
//
//    attributeAssignBase.getAttributeValueDelegate().assignValueString(GrouperDeprovisioningJob.retrieveAttributeDefNameEmailAddresses().getName(), "test@test.com,test1@test.com");
//
//    attributeAssignBase.getAttributeValueDelegate().assignValueString(
//        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName(), "1");
//    attributeAssignBase.getAttributeValueDelegate().assignValueString(
//        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
//    
//    attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerStem(stem0).assignAttributeDefName(attribute).save();
//    
//    attributeAssignBase.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName(), "test@test.com,test1@test.com");
//        
//    GrouperAttestationJob.stemAttestationProcessHelper(attributeAssignBase);
//
//    //####### check that the direct assigned did not change    
//    attributeAssignBase = groupDirectAssigned.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
//    assertEquals("1", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
//        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
//    assertEquals("true", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
//        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()));
//
//    
//    //####### check that the indirect assigned did change and need recertify 
//    
//    attributeAssignBase = groupInheritAssigned0.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
//    assertEquals("0", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
//        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
//    assertEquals("false", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
//        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()));
    

  }
  

}
