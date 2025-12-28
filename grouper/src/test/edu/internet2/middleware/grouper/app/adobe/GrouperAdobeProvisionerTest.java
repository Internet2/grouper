package edu.internet2.middleware.grouper.app.adobe;

import java.util.ArrayList;
import java.util.List;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningAttributeValue;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningBaseTest;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningDiagnosticsContainer;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningOutput;
import edu.internet2.middleware.grouper.app.provisioning.GrouperProvisioningService;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Group;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2Membership;
import edu.internet2.middleware.grouper.app.scim2Provisioning.GrouperScim2User;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hibernate.HibernateSession;
import edu.internet2.middleware.grouper.misc.GrouperStartup;
import edu.internet2.middleware.grouper.util.CommandLineExec;
import edu.internet2.middleware.grouper.util.GrouperUtil;
import edu.internet2.middleware.grouperClient.jdbc.GcDbAccess;
import junit.textui.TestRunner;

public class GrouperAdobeProvisionerTest extends GrouperProvisioningBaseTest {
  
  
  public static void main(String[] args) {

    AdobeMockServiceHandler.ensureAdobeMockTables();
    TestRunner.run(new GrouperAdobeProvisionerTest("testAdobeFullSyncProvisionGroupAndThenDeleteTheGroup"));

  }
  
  @Override
  public String defaultConfigId() {
    return "adobeProvisioner";
  }

  public static boolean startTomcat = false;
  
  public GrouperAdobeProvisionerTest(String name) {
    super(name);
  }
  
  
  
  private void validateNoErrors(GrouperProvisioningDiagnosticsContainer grouperProvisioningDiagnosticsContainer) {
    String[] lines = grouperProvisioningDiagnosticsContainer.getReportFinal().split("\n"); 
    List<String> errorLines = new ArrayList<String>();
    for (String line : lines) {
      if (line.contains("'red'") || line.contains("Error:")) {
        errorLines.add(line);
      }
    }
    
    if (errorLines.size() > 0) {
      fail("There are " + errorLines.size() + " errors in report: " + errorLines);
    }
  }

  public void testAdobeFullSyncProvisionGroupAndThenDeleteTheGroup() {
    
    if (!tomcatRunTests()) {
      return;
    }

    AdobeProvisionerTestUtils.setupAdobeExternalSystem();

    String adobeConfigId = "adobe";
    AdobeProvisionerTestUtils.configureAdobeProvisioner(new AdobeProvisionerTestConfigInput()
      .assignChangelogConsumerConfigId("adobeProvTestCLC").assignConfigId("adobeProvisioner")
      .assignEntityDeleteType("deleteEntitiesIfNotExistInGrouper")
      .assignGroupDeleteType("deleteGroupsIfGrouperDeleted")
      .assignMembershipDeleteType("deleteMembershipsIfGrouperDeleted")
      .assignGroupAttributeCount(2)
    );



    GrouperStartup.startup();
    
    if (startTomcat) {
      CommandLineExec commandLineExec = tomcatStart();
    }
    
    try {
      // this will create tables
      List<GrouperAdobeUser> grouperScimUsers = GrouperAdobeApiCommands.retrieveAdobeUsers(adobeConfigId, true, "testOrgId");
  
      new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_membership").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_group").executeSql();
      new GcDbAccess().connectionName("grouper").sql("delete from mock_adobe_user").executeSql();

      GrouperSession grouperSession = GrouperSession.startRootSession();
      
      Stem stem = new StemSave(grouperSession).assignName("test").save();
      Stem stem2 = new StemSave(grouperSession).assignName("test2").save();
      
      // mark some folders to provision
      Group testGroup = new GroupSave(grouperSession).assignName("test:testGroup").save();
      Group testGroup2 = new GroupSave(grouperSession).assignName("test2:testGroup2").save();
      
      testGroup.addMember(SubjectTestHelper.SUBJ0, false);
      testGroup.addMember(SubjectTestHelper.SUBJ1, false);
      
      testGroup2.addMember(SubjectTestHelper.SUBJ1, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ2, false);
      testGroup2.addMember(SubjectTestHelper.SUBJ3, false);
      
      final GrouperProvisioningAttributeValue attributeValue = new GrouperProvisioningAttributeValue();
      attributeValue.setDirectAssignment(true);
      attributeValue.setDoProvision("adobeProvisioner");
      attributeValue.setTargetName("adobeProvisioner");
      attributeValue.setStemScopeString("sub");
  
      GrouperProvisioningService.saveOrUpdateProvisioningAttributes(attributeValue, stem);
  
      //lets sync these over      
      assertEquals(new Integer(0), new GcDbAccess().connectionName("grouper").sql("select count(1) from mock_adobe_group").select(int.class));
  
      
      assertEquals(0, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup").list(GrouperScim2Group.class).size());
      
      long started = System.currentTimeMillis();
      
      GrouperProvisioningOutput grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);
      assertTrue(1 <= grouperProvisioningOutput.getInsert());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup").list(GrouperAdobeGroup.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeUser").list(GrouperAdobeUser.class).size());
      assertEquals(2, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeMembership").list(GrouperAdobeMembership.class).size());
      GrouperAdobeGroup grouperAdobeGroup = HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup").list(GrouperAdobeGroup.class).get(0);

      assertEquals("testGroup", grouperAdobeGroup.getName());
      
      
      //now remove one of the subjects from the testGroup
      testGroup.deleteMember(SubjectTestHelper.SUBJ1);
      
      // now run the full sync again and the member should be deleted from mock_adobe_membership also
      started = System.currentTimeMillis();
      
      grouperProvisioningOutput = fullProvision();
      GrouperUtil.sleep(2000);
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeGroup").list(GrouperAdobeGroup.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeUser").list(GrouperScim2User.class).size());
      assertEquals(1, HibernateSession.byHqlStatic().createQuery("from GrouperAdobeMembership").list(GrouperScim2Membership.class).size());
      
      

    } finally {
//      tomcatStop();
//      if (commandLineExec != null) {
//        GrouperUtil.threadJoin(commandLineExec.getThread());
//      }
    }
    
  }
  
}
