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
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import junit.textui.TestRunner;


/**
 *
 */
public class GroupUniqueExtensionInFoldersHookTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupUniqueExtensionInFoldersHookTest("testHookCaseSensitiveRoot"));
  }
  
  /**
   * 
   * @param name
   */
  public GroupUniqueExtensionInFoldersHookTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public GroupUniqueExtensionInFoldersHookTest() {
    super();
  }


  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), 
        GroupUniqueExtensionInFoldersHook.class);
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
    
    //  # comma separated folder names (id path).  Configure multiple with different config ids
    //  # {valueType: "string", regex: "^groupUniqueExtensionInFolderHook\\.[^.]+\\.folderNames$"}
    //  #groupUniqueExtensionInFolderHook.someConfigId.folderNames = a:b, b:c
    //
    //  # optional config for case sensitive extensions (default true)
    //  # {valueType: "string", regex: "^groupUniqueExtensionInFolderHook\\.[^.]+\\.caseSensitive$"}
    //  #groupUniqueExtensionInFolderHook.someConfigId.caseSensitive = false

    GrouperCacheUtils.clearAllCaches();
    GroupUniqueExtensionInFoldersHook.clearHook();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupUniqueExtensionInFolderHook.config1.folderNames", "test, test2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupUniqueExtensionInFolderHook.config2.folderNames", "test2, test3");
    
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testTestAStem = new StemSave(grouperSession).assignName("test:testA").save();
    Stem testTestBStem = new StemSave(grouperSession).assignName("test:testB").save();
    Stem test2Stem = new StemSave(grouperSession).assignName("test2").save();
    Stem test2TestAStem = new StemSave(grouperSession).assignName("test2:testA").save();
    Stem test2TestBStem = new StemSave(grouperSession).assignName("test2:testB").save();
    Stem testStem3 = new StemSave(grouperSession).assignName("test3").save();
    Stem testStem4 = new StemSave(grouperSession).assignName("test4").save();
   
    Group group1 = new GroupSave(grouperSession)
        .assignName("test:testA:group1").save();

    Group group2 = new GroupSave(grouperSession)
        .assignName("test:testB:group2").save();

    // different case
    Group group1b = new GroupSave(grouperSession)
        .assignName("test:testB:Group1").save();
    
    try {
      //alternate name

      new GroupSave(grouperSession)
        .assignName("test2:testA:group1").save();
      
      fail("Should fail");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionInFoldersHook.VETO_GROUP_UNIQUE_EXTENSION_IN_FOLDER);
    }


  }
  

  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testHookCaseInsensitive() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //  # comma separated folder names (id path).  Configure multiple with different config ids
    //  # {valueType: "string", regex: "^groupUniqueExtensionInFolderHook\\.[^.]+\\.folderNames$"}
    //  #groupUniqueExtensionInFolderHook.someConfigId.folderNames = a:b, b:c
    //
    //  # optional config for case sensitive extensions (default true)
    //  # {valueType: "string", regex: "^groupUniqueExtensionInFolderHook\\.[^.]+\\.caseSensitive$"}
    //  #groupUniqueExtensionInFolderHook.someConfigId.caseSensitive = false

    GrouperCacheUtils.clearAllCaches();
    GroupUniqueExtensionInFoldersHook.clearHook();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupUniqueExtensionInFolderHook.config1.folderNames", "test, test2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupUniqueExtensionInFolderHook.config1.caseSensitive", "false");
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupUniqueExtensionInFolderHook.config2.folderNames", "test2, test3");
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testTestAStem = new StemSave(grouperSession).assignName("test:testA").save();
    Stem testTestBStem = new StemSave(grouperSession).assignName("test:testB").save();
    Stem test2Stem = new StemSave(grouperSession).assignName("test2").save();
    Stem test2TestAStem = new StemSave(grouperSession).assignName("test2:testA").save();
    Stem test2TestBStem = new StemSave(grouperSession).assignName("test2:testB").save();
    Stem testStem3 = new StemSave(grouperSession).assignName("test3").save();
    Stem testStem4 = new StemSave(grouperSession).assignName("test4").save();
   
    Group group1 = new GroupSave(grouperSession)
        .assignName("test:testA:group1").save();

    Group group2 = new GroupSave(grouperSession)
        .assignName("test:testB:group2").save();

    try {
      //alternate name

      new GroupSave(grouperSession).assignName("test:testB:Group1").save();
      
      fail("Should fail");

    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionInFoldersHook.VETO_GROUP_UNIQUE_EXTENSION_IN_FOLDER);
    }

    try {
      //alternate name

      new GroupSave(grouperSession).assignName("test2:testA:group1").save();
      
      fail("Should fail");

    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionInFoldersHook.VETO_GROUP_UNIQUE_EXTENSION_IN_FOLDER);
    }


  }
  
  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testHookCaseSensitiveRoot() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //  # comma separated folder names (id path).  Configure multiple with different config ids
    //  # {valueType: "string", regex: "^groupUniqueExtensionInFolderHook\\.[^.]+\\.folderNames$"}
    //  #groupUniqueExtensionInFolderHook.someConfigId.folderNames = a:b, b:c
    //
    //  # optional config for case sensitive extensions (default true)
    //  # {valueType: "string", regex: "^groupUniqueExtensionInFolderHook\\.[^.]+\\.caseSensitive$"}
    //  #groupUniqueExtensionInFolderHook.someConfigId.caseSensitive = false

    GrouperCacheUtils.clearAllCaches();
    GroupUniqueExtensionInFoldersHook.clearHook();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupUniqueExtensionInFolderHook.config1.folderNames", "test, test2, :");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupUniqueExtensionInFolderHook.config2.folderNames", "test2, test3");
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testTestAStem = new StemSave(grouperSession).assignName("test:testA").save();
    Stem testTestBStem = new StemSave(grouperSession).assignName("test:testB").save();
    Stem test2Stem = new StemSave(grouperSession).assignName("test2").save();
    Stem test2TestAStem = new StemSave(grouperSession).assignName("test2:testA").save();
    Stem test2TestBStem = new StemSave(grouperSession).assignName("test2:testB").save();
    Stem testStem3 = new StemSave(grouperSession).assignName("test3").save();
    Stem testStem4 = new StemSave(grouperSession).assignName("test4").save();

    Group group1 = new GroupSave(grouperSession)
        .assignName("test:testA:group1").save();

    try {
      //alternate name
      new GroupSave(grouperSession).assignCreateParentStemsIfNotExist(true)
        .assignName("test4:testA:group1").save();  

      fail("Should fail");

    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionInFoldersHook.VETO_GROUP_UNIQUE_EXTENSION_IN_FOLDER);
    }


    Group group2 = new GroupSave(grouperSession)
        .assignName("test:testB:group2").save();

    // different case
    Group group1b = new GroupSave(grouperSession)
        .assignName("test:testB:Group1").save();
    
    try {
      //alternate name

      new GroupSave(grouperSession).assignName("test2:testA:group1").save();
      
      fail("Should fail");

    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionInFoldersHook.VETO_GROUP_UNIQUE_EXTENSION_IN_FOLDER);
    }


  }
  
  /**
   * @throws Exception
   */
  public void testMove() throws Exception {
    
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    GrouperCacheUtils.clearAllCaches();
    GroupUniqueExtensionInFoldersHook.clearHook();

    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupUniqueExtensionInFolderHook.config1.folderNames", "test, test2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().put("groupUniqueExtensionInFolderHook.config2.folderNames", "test2, test3");
    
    Stem testStem = new StemSave(grouperSession).assignName("test").save();
    Stem testTestAStem = new StemSave(grouperSession).assignName("test:testA").save();
    Stem testTestBStem = new StemSave(grouperSession).assignName("test:testB").save();
    Stem test2Stem = new StemSave(grouperSession).assignName("test2").save();
    Stem test2TestAStem = new StemSave(grouperSession).assignName("test2:testA").save();
    Stem test2TestBStem = new StemSave(grouperSession).assignName("test2:testB").save();
    Stem testStem3 = new StemSave(grouperSession).assignName("test3").save();
    Stem testStem4 = new StemSave(grouperSession).assignName("test4").save();

    Group group1 = new GroupSave(grouperSession)
        .assignName("test:testA:group1").save();

    Group group2 = new GroupSave(grouperSession)
        .assignName("test4:group1").save();

    try {
      group2.move(test2TestAStem);
      fail("No error??");
    } catch (HookVeto hv) {
      //this is a success, it is supposed to veto  
      assertEquals(hv.getReasonKey(), GroupUniqueExtensionInFoldersHook.VETO_GROUP_UNIQUE_EXTENSION_IN_FOLDER);
    }
      
    group1.move(testStem3);
    
  }
}
