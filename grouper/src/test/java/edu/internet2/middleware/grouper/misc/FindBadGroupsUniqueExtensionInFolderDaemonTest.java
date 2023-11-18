package edu.internet2.middleware.grouper.misc;

import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupSave;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemSave;
import edu.internet2.middleware.grouper.app.loader.OtherJobBase;
import edu.internet2.middleware.grouper.cache.GrouperCache;
import edu.internet2.middleware.grouper.cache.GrouperCacheUtils;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.hooks.examples.GroupUniqueExtensionInFoldersHook;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import junit.textui.TestRunner;


public class FindBadGroupsUniqueExtensionInFolderDaemonTest extends GrouperTest {

  public FindBadGroupsUniqueExtensionInFolderDaemonTest(String name) {
    super(name);
  }


  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new FindBadGroupsUniqueExtensionInFolderDaemonTest("testUniqueExtensionDaemonRoot"));
  }
    
  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testUniqueExtensionDaemon() throws Exception {
    
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
   
    Group group4 = new GroupSave(grouperSession)
        .assignName("test4:group1").save(); 
    
    Group group1 = new GroupSave(grouperSession)
        .assignName("test:testA:group1").save();

    Group group2 = new GroupSave(grouperSession)
        .assignName("test:testB:group2").save();

    // different case
    Group group1b = new GroupSave(grouperSession)
        .assignName("test:testB:Group1").save();

    FindBadGroupsUniqueExtensionInFolderDaemon.runDaemonStandalone();

    new GroupSave(grouperSession)
      .assignName("test2:testA:group1").save();
  
    try {
      FindBadGroupsUniqueExtensionInFolderDaemon.runDaemonStandalone();
      fail("should fail");
    } catch (Exception e) {
      
    }

  }
  
  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testUniqueExtensionCaseInsensitiveDaemon() throws Exception {
    
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
   
    Group group4 = new GroupSave(grouperSession)
        .assignName("test4:group1").save();
    
    Group group1 = new GroupSave(grouperSession)
        .assignName("test:testA:group1").save();

    Group group2 = new GroupSave(grouperSession)
        .assignName("test:testB:group2").save();

    FindBadGroupsUniqueExtensionInFolderDaemon.runDaemonStandalone();

    // different case
    Group group1b = new GroupSave(grouperSession)
        .assignName("test:testB:Group1").save();

    try {
      FindBadGroupsUniqueExtensionInFolderDaemon.runDaemonStandalone();
      fail("should fail");
    } catch (Exception e) {
      
    }

    new GroupSave(grouperSession)
      .assignName("test2:testA:group1").save();
  
    try {
      FindBadGroupsUniqueExtensionInFolderDaemon.runDaemonStandalone();
      fail("should fail");
    } catch (Exception e) {
      
    }

  }
  

  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testUniqueExtensionDaemonRoot() throws Exception {
    
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

    Group group2 = new GroupSave(grouperSession)
        .assignName("test:testB:group2").save();

    // different case
    Group group1b = new GroupSave(grouperSession)
        .assignName("test:testB:Group1").save();

    FindBadGroupsUniqueExtensionInFolderDaemon.runDaemonStandalone();

    new GroupSave(grouperSession)
      .assignName("test4:group1").save();
  
    try {
      FindBadGroupsUniqueExtensionInFolderDaemon.runDaemonStandalone();
      fail("should fail");
    } catch (Exception e) {
      
    }

  }
  
  /**
   * test a built in attribute value
   * @throws Exception
   */
  public void testUniqueExtensionCaseInsensitiveDaemonRoot() throws Exception {
    
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

    FindBadGroupsUniqueExtensionInFolderDaemon.runDaemonStandalone();

    // different case
    Group group1b = new GroupSave(grouperSession)
        .assignName("test4:Group1").save();

    try {
      FindBadGroupsUniqueExtensionInFolderDaemon.runDaemonStandalone();
      fail("should fail");
    } catch (Exception e) {
      
    }


  }
  


}
