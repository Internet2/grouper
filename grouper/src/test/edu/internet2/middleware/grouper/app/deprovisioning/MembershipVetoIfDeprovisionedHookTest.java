package edu.internet2.middleware.grouper.app.deprovisioning;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Member;
import edu.internet2.middleware.grouper.MemberFinder;
import edu.internet2.middleware.grouper.Membership;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.GrouperLoader;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.beans.HooksContext;
import edu.internet2.middleware.grouper.hooks.beans.HooksMembershipChangeBean;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.misc.GrouperCheckConfig;
import junit.textui.TestRunner;

public class MembershipVetoIfDeprovisionedHookTest extends GrouperTest {
  
  public static void main(String[] args) {
    TestRunner.run(new MembershipVetoIfDeprovisionedHookTest("testMembershipVetoDeprovisioning"));
  }
  
  /**
   * 
   */
  public MembershipVetoIfDeprovisionedHookTest() {
    super();
    
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  @Override
  protected void setUp() {
    super.setUp();
    GrouperCheckConfig.waitUntilDoneWithExtraConfig();
  }
  
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
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("deprovisioning.affiliations", "faculty, student, employee");
  }
  

  /**
   * @param name
   */
  public MembershipVetoIfDeprovisionedHookTest(String name) {
    super(name);
  }
  

  public void testMembershipVetoDeprovisioning() {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem deprovisioningStem = new StemSave(grouperSession).assignName("test").save();
    Group group = new GroupSave(grouperSession).assignName(deprovisioningStem.getName() + ":testGroup" ).assignCreateParentStemsIfNotExist(true).save();

    
    GrouperDeprovisioningAffiliation deprovisioningAffiliation = GrouperDeprovisioningAffiliation.retrieveAllAffiliations().get("employee");
    Group usersWhoHaveBeenDeprovisionedGroup = deprovisioningAffiliation.getUsersWhoHaveBeenDeprovisionedGroup();
    usersWhoHaveBeenDeprovisionedGroup.addMember(SubjectTestHelper.SUBJ0, false);
    
    GrouperDeprovisioningOverallConfiguration grouperDeprovisioningOverallConfiguration = GrouperDeprovisioningOverallConfiguration.retrieveConfiguration(deprovisioningStem, false);
    
    GrouperDeprovisioningConfiguration grouperDeprovisioningConfiguration = new GrouperDeprovisioningConfiguration();
    grouperDeprovisioningConfiguration.setGrouperDeprovisioningOverallConfiguration(grouperDeprovisioningOverallConfiguration);
    grouperDeprovisioningOverallConfiguration.getAffiliationToConfiguration().put("employee", grouperDeprovisioningConfiguration);

    GrouperDeprovisioningAttributeValue grouperDeprovisioningAttributeValue = new GrouperDeprovisioningAttributeValue();
    grouperDeprovisioningAttributeValue.setGrouperDeprovisioningConfiguration(grouperDeprovisioningConfiguration);

    grouperDeprovisioningConfiguration.setNewConfig(grouperDeprovisioningAttributeValue);

    grouperDeprovisioningAttributeValue.setDeprovisionString("true");
    grouperDeprovisioningAttributeValue.setDirectAssignment(true);

    grouperDeprovisioningAttributeValue.setAffiliationString("employee");

    grouperDeprovisioningConfiguration.storeConfiguration();

    GrouperLoader.runOnceByJobName(grouperSession, "OTHER_JOB_deprovisioningFullSyncDaemon");

    MembershipVetoIfDeprovisionedHook vetoHook = new MembershipVetoIfDeprovisionedHook();
    
    HooksMembershipChangeBean bean = new HooksMembershipChangeBean();
    
    Member member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ0);
    
    Membership membership = new Membership();
    membership.setOwnerGroupId(group.getId());
    membership.setMember(member);
    HooksContext hooksContext = new HooksContext();
    
    try {      
      //sub0 is already deprovisioned so it should throw HookVeto exception
      vetoHook.checkMembershipEligibility(membership, hooksContext);
      fail();
    } catch (HookVeto e) {
      
    }
    
    member = MemberFinder.findBySubject(grouperSession, SubjectTestHelper.SUBJ1);
    membership = new Membership();
    membership.setOwnerGroupId(group.getId());
    membership.setMember(member);
    boolean noException = true;
    try {      
      //sub1 is not deprovisioned so it should not throw HookVeto exception
      vetoHook.checkMembershipEligibility(membership, hooksContext);
    } catch (HookVeto e) {
      noException = false;
    }
    
    assertTrue(noException);
    
  }



}
