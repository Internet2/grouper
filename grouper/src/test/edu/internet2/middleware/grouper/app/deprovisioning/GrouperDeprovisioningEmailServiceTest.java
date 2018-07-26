package edu.internet2.middleware.grouper.app.deprovisioning;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.MembershipFinder;
import edu.internet2.middleware.grouper.app.deprovisioning.GrouperDeprovisioningEmailService.EmailPerPerson;
import edu.internet2.middleware.grouper.cache.EhcacheController;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import junit.textui.TestRunner;

public class GrouperDeprovisioningEmailServiceTest extends GrouperTest {
  
  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }

  /**
   * main
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperDeprovisioningEmailServiceTest("testBuildEmailObjects"));
  }
  
  /**
   */
  public GrouperDeprovisioningEmailServiceTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public GrouperDeprovisioningEmailServiceTest(String name) {
    super(name);
  }
  
  public void testBuildEmailObjects() {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group = configureGroupWithCustomEmailSettings(grouperSession);
    
    group.addMember(SubjectTestHelper.SUBJ0);
    
    Set<Membership> memberships = new HashSet<Membership>();
    memberships.add(MembershipFinder.findImmediateMembership(grouperSession, group, SubjectTestHelper.SUBJ0, true));
    
    GrouperDeprovisioningAffiliation affiliation = new GrouperDeprovisioningAffiliation();
    affiliation.setLabel("student");
    
    Map<String, EmailPerPerson> emailObjects = new GrouperDeprovisioningEmailService().buildEmailObjectForOneDeprovisionedSubject(grouperSession, 
        memberships, affiliation, false);
    
    assertEquals(1, emailObjects.size());
    assertTrue(emailObjects.containsKey("test@grouper.edu"));
    
    EmailPerPerson emailObject = emailObjects.get("test@grouper.edu");
    
    assertEquals(1, emailObject.customEmailBodies.size());
    assertEquals("mno", emailObject.customEmailBodies.get(0));
    assertEquals("student", emailObject.deprovisioningGroupEmailObjects.get(0).getAffiliation());
    assertEquals(group.getId(), emailObject.deprovisioningGroupEmailObjects.get(0).getGrouperObject().getId());
    
  }
  
  private Group configureGroupWithCustomEmailSettings(GrouperSession grouperSession) {
    
    Group someGroup = new GroupSave(grouperSession).assignName("a:b:c")
        .assignCreateParentStemsIfNotExist(true).save();
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = 
        GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(someGroup, false);
    
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
    grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
    
    grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("student", grouperDeprovisioningConfiguration);
    
    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
    grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);
    grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);
    
    grouperDeprovisioningAttributeValue.setAllowAddsWhileDeprovisionedString("true");
    grouperDeprovisioningAttributeValue.setAutoChangeLoaderString("false");
    grouperDeprovisioningAttributeValue.setAutoselectForRemovalString("false");
    grouperDeprovisioningAttributeValue.setDeprovisionString("false");
    grouperDeprovisioningAttributeValue.setDirectAssignmentString("true");
    grouperDeprovisioningAttributeValue.setEmailAddressesString("test@grouper.edu");
    grouperDeprovisioningAttributeValue.setEmailBodyString("mno");
    grouperDeprovisioningAttributeValue.setInheritedFromFolderIdString("hgn");
    //grouperDeprovisioningAttributeValue.setMailToGroupString("stu");
    grouperDeprovisioningAttributeValue.setAffiliationString("student");
    grouperDeprovisioningAttributeValue.setSendEmailString("true");
    grouperDeprovisioningAttributeValue.setShowForRemovalString("false");
    grouperDeprovisioningAttributeValue.setStemScopeString("one");
    
    grouperDeprovisioningConfiguration.storeConfiguration();
    
    EhcacheController.ehcacheController().flushCache();
    
    return someGroup;
    
  }
  

}
