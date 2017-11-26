package edu.internet2.middleware.grouper.app.attestation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.attestation.GrouperAttestationJob.EmailObject;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssignType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.group.TestGroupFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.misc.GrouperDAOFactory;
import edu.internet2.middleware.grouper.session.GrouperSessionResult;

/**
 * 
 */
public class GrouperAttestationJobTest extends GrouperTest {
  
  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperAttestationJobTest("testGrouperAttestationPrivileges"));
  }
  
  /**
   * @param name
   */
  public GrouperAttestationJobTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testGrouperAttestationPrivileges() {
    new TestGroupFinder().testFindByAttributeAssignOnAssignValuesAndPrivilege();
  }
  
  /**
   * 
   */
  public void testDaemonUpdatingCalculatedDates() {
    
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();

    Stem stem0 = new StemSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test").save();
    Group groupDirectAssigned = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test2:group0").save();
    Group groupInheritAssigned0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test1:group1").save();
    Group groupInheritAssigned1 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:test3:group2").save();

    AttributeDefName attribute = GrouperAttestationJob.retrieveAttributeDefNameValueDef();
    
    AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(groupDirectAssigned).assignAttributeDefName(attribute).save();
    
    attributeAssignBase.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName(), "test@test.com,test1@test.com");
 
    attributeAssignBase.getAttributeValueDelegate().assignValueString(
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName(), "1");
    attributeAssignBase.getAttributeValueDelegate().assignValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName(), "true");
    
    attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerStem(stem0).assignAttributeDefName(attribute).save();
    
    attributeAssignBase.getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses().getName(), "test@test.com,test1@test.com");
        
    GrouperAttestationJob.stemAttestationProcessHelper(stem0, attributeAssignBase);

    //####### check that the direct assigned did not change    
    attributeAssignBase = groupDirectAssigned.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
    assertEquals("1", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
    assertEquals("true", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()));

    
    //####### check that the indirect assigned did change and need recertify 
    
    attributeAssignBase = groupInheritAssigned0.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
    assertEquals("0", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
    assertEquals("false", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()));
    
    attributeAssignBase = groupInheritAssigned1.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
    assertEquals("0", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
    assertEquals("false", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()));

    //####### mark one as reviewed
    String today = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

    groupInheritAssigned0.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true)
      .getAttributeValueDelegate().assignValueString(GrouperAttestationJob.retrieveAttributeDefNameDateCertified().getName(), today);

    GrouperAttestationJob.stemAttestationProcessHelper(stem0, stem0.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true));

    //####### check that the indirect assigned did change and need recertify 
    
    int configuredDaysUntilRecertify = GrouperConfig.retrieveConfig().propertyValueInt("attestation.default.daysUntilRecertify", 180);

    attributeAssignBase = groupInheritAssigned0.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
    assertEquals("" + configuredDaysUntilRecertify, attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
    assertEquals("false", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()));
    
    attributeAssignBase = groupInheritAssigned1.getAttributeDelegate().retrieveAssignment(null, GrouperAttestationJob.retrieveAttributeDefNameValueDef(), false, true);
    assertEquals("0", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameCalculatedDaysLeft().getName()));
    assertEquals("false", attributeAssignBase.getAttributeValueDelegate().retrieveValueString(
        GrouperAttestationJob.retrieveAttributeDefNameDirectAssignment().getName()));

  }
  
  /**
   * 
   */
  private void setupGroupEmailAttributes() {
    
    GrouperSessionResult grouperSessionResult = GrouperSession.startRootSessionIfNotStarted();
    GrouperSession grouperSession = grouperSessionResult.getGrouperSession();
    
    Group group0 = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true).assignName("test:group0").save();
    
    AttributeAssign attributeAssignBase = new AttributeAssignSave(grouperSession).assignOwnerGroup(group0).assignAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameValueDef()).save();
    
    AttributeAssignValue attributeAssignValueEmailAddresses = new AttributeAssignValue();
    attributeAssignValueEmailAddresses.setValueString("test@test.com,test1@test.com");
    
    new AttributeAssignSave(grouperSession).assignOwnerAttributeAssign(attributeAssignBase).assignAttributeDefName(GrouperAttestationJob.retrieveAttributeDefNameEmailAddresses()).addAttributeAssignValue(attributeAssignValueEmailAddresses).save();
 
  }
  
  /**
   * 
   */
  public void testBuildAttestationGroupEmails() {
   
    setupGroupEmailAttributes();
   
    Set<AttributeAssign> groupAttributeAssigns = GrouperDAOFactory.getFactory().getAttributeAssign().findAttributeAssignments(
        AttributeAssignType.group,
        null, GrouperAttestationJob.retrieveAttributeDefNameValueDef().getId(), null,
        null, null, null, 
        null, 
        Boolean.TRUE, false);
    
    Map<String, Set<EmailObject>> attestationGroupEmails = GrouperAttestationJob.buildAttestationGroupEmails(null, groupAttributeAssigns);
   
    assertEquals(2, attestationGroupEmails.size());
    assertTrue(attestationGroupEmails.containsKey("test@test.com"));
    assertTrue(attestationGroupEmails.containsKey("test1@test.com"));
  }

}
