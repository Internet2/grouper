package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;

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
import edu.internet2.middleware.grouper.misc.GrouperObject;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import edu.internet2.middleware.grouper.util.GrouperUtil;

public class GrouperDeprovisioningJobTest extends GrouperTest {

  /**
   * add config stuff
   */
  @Override
  protected void setupConfigs() {
    //  # if deprovisioning should be enabled
    //  deprovisioning.enable = true
    //
    //  # comma separated affiliations for deprovisioning e.g. employee, student, etc
    //  # these need to be alphanumeric suitable for properties keys for further config or for group extensions
    //  deprovisioning.affiliations = 
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.enable", "true");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.affiliations", "faculty, student");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.affiliation_faculty.groupNameMeansInAffiliation", "community:faculty");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.affiliation_student.groupNameMeansInAffiliation", "community:student");
  }

  /**
   * 
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.checkGroups();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDeprovisioningJobTest("testDaemonUpdatingMetadataOnGroups"));
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
  public void testRetrieveAffiliations() {
        
    Map<String, GrouperDeprovisioningAffiliation> allAffiliations = GrouperDeprovisioningAffiliation.retrieveAllAffiliations();
    
    assertEquals(2, GrouperUtil.length(allAffiliations));
    
    boolean foundStudent = false;
    boolean foundFaculty = false;
    
    for (GrouperDeprovisioningAffiliation grouperDeprovisioningAffiliation : allAffiliations.values()) {
      if (StringUtils.equals("faculty", grouperDeprovisioningAffiliation.getLabel())) {
        foundFaculty = true;
        assertEquals("community:faculty", grouperDeprovisioningAffiliation.getGroupNameMeansInAffiliation());
      }
      if (StringUtils.equals("student", grouperDeprovisioningAffiliation.getLabel())) {
        foundStudent = true;
      }
    }
    assertTrue(foundFaculty);
    assertTrue(foundStudent);
    
    assertTrue(allAffiliations.containsKey("faculty"));
    assertTrue(allAffiliations.containsKey("student"));
    
    Group facultyManagers = GroupFinder.findByName(GrouperSession.staticGrouperSession(), GrouperDeprovisioningSettings.deprovisioningStemName() + ":managersWhoCanDeprovision_faculty", true);
    
    facultyManagers.addMember(SubjectTestHelper.SUBJ1);
    
    assertEquals(0, GrouperUtil.length(GrouperDeprovisioningAffiliation.retrieveAffiliationsForUserManager(SubjectTestHelper.SUBJ2)));
    
    Map<String, GrouperDeprovisioningAffiliation> affiliations = GrouperDeprovisioningAffiliation.retrieveAffiliationsForUserManager(SubjectTestHelper.SUBJ1);
    
    assertEquals(1, GrouperUtil.length(affiliations));
    assertEquals("faculty", affiliations.keySet().iterator().next());
    assertEquals("faculty", affiliations.get("faculty").getLabel());
    
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
    Stem stemTest11a = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1a").save();
    Stem stemTest11b = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1b").save();
    Stem stemTest22a = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2:test2a").save();
    Stem stemTest33a = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3:test3a").save();
    Stem stemTest33b = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3:test3b").save();
    Group groupDirectAssigned = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2:test2a:group0").save();
    Group groupInheritAssigned0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:test1a:group1").save();
    Group groupInheritAssigned1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3:test3b:group2").save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = 
        GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(stem0, false);
 
    GrouperDeprovisioningConfiguration studentConfiguration = new GrouperDeprovisioningConfiguration();
        
    grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("student", studentConfiguration);
    
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
    grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(studentConfiguration);

    studentConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);
    
    grouperDeprovisioningAttributeValue.setDeprovisionString("true");
    
    grouperDeprovisioningAttributeValue.setAffiliationString("student");

    studentConfiguration.storeConfiguration();

    //GrouperDeprovisioningJob.
    
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
