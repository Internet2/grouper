package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemFinder;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.SubjectFinder;
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
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("grouper.enable.rule.cannotAddEveryEntity", "true");

  }

  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testHook() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Group group1 = new GroupSave(grouperSession).assignName("test:testGroup1").assignCreateParentStemsIfNotExist(true).save();
    
    group1.grantPriv(SubjectTestHelper.SUBJ0, AccessPrivilege.ADMIN);

    Stem stem = StemFinder.findByName(grouperSession, "test", true);
    stem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    
    group1.addMember(SubjectFinder.findAllSubject());
    group1.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
    stem.grantPriv(SubjectFinder.findAllSubject(), NamingPrivilege.CREATE);
    
    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    try {
      group1.addMember(SubjectFinder.findAllSubject());
      
      fail("should veto");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), MembershipCannotAddEveryEntityHook.HOOK_VETO_CANNOT_ADD_EVERY_ENTITY);
    }
    group1.addMember(SubjectTestHelper.SUBJ2);

    try {
      group1.grantPriv(SubjectFinder.findAllSubject(), AccessPrivilege.ADMIN);
      
      fail("should veto");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), MembershipCannotAddEveryEntityHook.HOOK_VETO_CANNOT_ADD_EVERY_ENTITY);
    }
    group1.grantPriv(SubjectTestHelper.SUBJ4, AccessPrivilege.ADMIN);

    try {
      stem.grantPriv(SubjectFinder.findAllSubject(), NamingPrivilege.CREATE);
      
      fail("should veto");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), MembershipCannotAddEveryEntityHook.HOOK_VETO_CANNOT_ADD_EVERY_ENTITY);
    }
    stem.grantPriv(SubjectTestHelper.SUBJ4, NamingPrivilege.CREATE);

  }

  
}
