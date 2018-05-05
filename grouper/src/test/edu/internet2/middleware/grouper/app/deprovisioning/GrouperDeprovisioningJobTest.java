package edu.internet2.middleware.grouper.app.deprovisioning;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;
import junit.textui.TestRunner;

public class GrouperDeprovisioningJobTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDeprovisioningJobTest(""));
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
