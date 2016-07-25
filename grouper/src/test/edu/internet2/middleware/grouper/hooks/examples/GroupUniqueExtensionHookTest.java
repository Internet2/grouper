/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.privs.NamingPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 *
 */
public class GroupUniqueExtensionHookTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    
  }
  
  /**
   * 
   * @param name
   */
  public GroupUniqueExtensionHookTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public GroupUniqueExtensionHookTest() {
    super();
  }


  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), 
        GroupUniqueExtensionHook.class);
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    try {
      overrideHooksAdd();
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      GrouperTest.initGroupsAndAttributes();
  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    overrideHooksRemove();
  }

  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testHookCaseSensitive() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testStem2 = new StemSave(grouperSession).assignName("test2").save();
    
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    testStem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    Group group = new GroupSave(grouperSession)
        .assignName("test:someGroupName").save();

    try {
      //alternate name
      new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test2:someGroupName").save();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionHook.VETO_GROUP_UNIQUE_EXTENSION);
    }

    try {
      //name
      group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test2:someGroupNamE").save();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionHook.VETO_GROUP_UNIQUE_EXTENSION);
    }

    group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test2:someGroupNamE2").save();
    
    try {
      //rename to alternate name
      group.setExtension("someGroupNaME");
      group.store();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionHook.VETO_GROUP_UNIQUE_EXTENSION);
    }

  }
  
  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testHookCaseInsensitive() throws Exception {
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("hook.group.unique.extension.caseInsensitive", "true");

    GrouperSession grouperSession = GrouperSession.startRootSession();

    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testStem2 = new StemSave(grouperSession).assignName("test2").save();

    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    testStem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.CREATE);
    testStem2.grantPriv(SubjectTestHelper.SUBJ0, NamingPrivilege.STEM);

    GrouperSession.stopQuietly(grouperSession);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);

    Group group = new GroupSave(grouperSession)
        .assignName("test:someGroupName").save();

    try {
      //alternate name
      new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test2:someGroupName").save();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionHook.VETO_GROUP_UNIQUE_EXTENSION);
    }

    //name
    group = new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
      .assignName("test2:someGroupNamE2").save();

    try {
      //rename to alternate name
      group.setExtension("someGroUpName");
      group.store();
      
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionHook.VETO_GROUP_UNIQUE_EXTENSION);
    }

  }

  /**
   * @throws Exception
   */
  public void testMove() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testStem2 = new StemSave(grouperSession).assignName("test2").save();
    new StemSave(grouperSession).assignName("test3").save();

    Group group = new GroupSave(grouperSession).assignName("test:someGroupName").save();
    new GroupSave(grouperSession).assignName("test2:someGroupName2").save();
    Group group3 = new GroupSave(grouperSession).assignName("test3:someGroupName3").save();

    group.move(testStem2);
    group.move(testStem);
    
    group3.setExtension("someGroupName4");
    group3.store();
    
    try {
      group3.setExtension("someGroupName");
      group3.store();
      fail("No error??");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionHook.VETO_GROUP_UNIQUE_EXTENSION);
    }
  }
}
