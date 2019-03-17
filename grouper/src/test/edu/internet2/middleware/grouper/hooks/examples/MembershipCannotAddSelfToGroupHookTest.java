package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import junit.textui.TestRunner;


public class MembershipCannotAddSelfToGroupHookTest extends GrouperTest {

  /**
   * 
   */
  public MembershipCannotAddSelfToGroupHookTest() {
    super();
  }

  /**
   * 
   * @param name
   */
  public MembershipCannotAddSelfToGroupHookTest(String name) {
    super(name);
  }

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new MembershipCannotAddSelfToGroupHookTest("testHook"));
  }
  
  /**
   * 
   * @see GrouperTest#setupConfigs()
   */
  protected void setupConfigs() {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.enable.rule.cannotAddSelfToGroup", "true");

  }

  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testHook() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group1 = new GroupSave(grouperSession).assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);

    // add not-add-self attribute
    group1.getAttributeDelegate().assignAttribute(MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName());
    
    Group group2 = new GroupSave(grouperSession).assignName("test:testGroup2").assignCreateParentStemsIfNotExist(true).save();
    
    group2.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);

    // add not-add-self attribute
    group1.getAttributeDelegate().assignAttribute(MembershipCannotAddSelfToGroupHook.cannotAddSelfAttributeDefName());
    
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    group2 = GroupFinder.findByName(grouperSession, group2.getName(), true);
    
    // this should be ok, doesnt have attribute
    group2.addMember(SubjectTestHelper.SUBJ0);
    group2.addMember(SubjectTestHelper.SUBJ1);


    group1 = GroupFinder.findByName(grouperSession, group1.getName(), true);
    
    // this should be ok, its not self
    group1.addMember(SubjectTestHelper.SUBJ1);

    // this should be ok, its a priv
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.UPDATE);
    
    try {
      //add self
      group1.addMember(SubjectTestHelper.SUBJ0);
      
      fail("should veto");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), MembershipCannotAddSelfToGroupHook.HOOK_VETO_CANNOT_ADD_SELF_TO_GROUP);
    }

    GrouperSession.stopQuietly(grouperSession);
    grouperSession = GrouperSession.startRootSession();
    group1.addMember(SubjectTestHelper.SUBJ0);
    
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    // remove is ok
    group1.deleteMember(SubjectTestHelper.SUBJ0);
  }

  
}
