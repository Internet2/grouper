package edu.internet2.middleware.grouper.hooks.examples;

import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GroupTypeFinder;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.misc.GrouperStartup;

/**
 * @author shilen
 */
public class LDAPProvisioningHookTest extends GrouperTest {

  /**
   * edu stem 
   */
  private Stem edu;
  
  /**
   */
  private GrouperSession grouperSession = null;

  /**
   * root stem 
   */
  private Stem root;

  /**
   * @param name
   */
  public LDAPProvisioningHookTest(String name) {
    super(name);
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), LDAPProvisioningHook.class);
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    super.setUp();
    
    try {
      overrideHooksAdd();
      setupTestConfigForIncludeExclude();
      GrouperStartup.initIncludeExcludeType();

      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("LDAPProvisioningHook.exclude.regex.0", ".*_excludes$");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("LDAPProvisioningHook.exclude.regex.1", ".*_includes$");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("LDAPProvisioningHook.exclude.regex.2", ".*_systemOfRecord$");
      GrouperConfig.retrieveConfig().propertiesOverrideMap().put("LDAPProvisioningHook.exclude.regex.3", ".*_systemOfRecordAndIncludes$");

      grouperSession = SessionHelper.getRootSession();
      root = StemHelper.findRootStem(grouperSession);
      edu = StemHelper.addChildStem(root, "edu", "education");
  
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    super.tearDown();
    
    overrideHooksRemove();
    
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("LDAPProvisioningHook.exclude.regex.0");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("LDAPProvisioningHook.exclude.regex.1");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("LDAPProvisioningHook.exclude.regex.2");
    GrouperConfig.retrieveConfig().propertiesOverrideMap().remove("LDAPProvisioningHook.exclude.regex.3");
  }

  /**
   * @throws Exception
   */
  @SuppressWarnings("deprecation")
  public void testIncludedGroup() {
    edu.addChildGroup("test", "test");

    assertNull(GroupTypeFinder.find("LDAPProvisioning", false));
  }
  
  /**
   * @throws Exception
   */
  @SuppressWarnings("deprecation")
  public void testExcludedGroup() {
    Group group = edu.addChildGroup("test_excludes", "test");

    GroupType type = GroupTypeFinder.find("LDAPProvisioning");
    assertTrue(group.hasType(type));
    assertEquals(group.getAttribute("LDAPProvisioningExclude"), "true");
  }
  
  /**
   * @throws Exception
   */
  @SuppressWarnings("deprecation")
  public void testIncludeExcludeHook() {
    Group group = edu.addChildGroup("test", "test");
    group.addType(GroupTypeFinder.find("addIncludeExclude"));

    GroupType type = GroupTypeFinder.find("LDAPProvisioning");
    
    group = GroupFinder.findByName(grouperSession, "edu:test", true);
    assertFalse(group.hasType(type));

    group = GroupFinder.findByName(grouperSession, "edu:test_excludes", true);
    assertTrue(group.hasType(type));
    assertEquals(group.getAttribute("LDAPProvisioningExclude"), "true");
    
    group = GroupFinder.findByName(grouperSession, "edu:test_includes", true);
    assertTrue(group.hasType(type));
    assertEquals(group.getAttribute("LDAPProvisioningExclude"), "true");
    
    group = GroupFinder.findByName(grouperSession, "edu:test_systemOfRecord", true);
    assertTrue(group.hasType(type));
    assertEquals(group.getAttribute("LDAPProvisioningExclude"), "true");
    
    group = GroupFinder.findByName(grouperSession, "edu:test_systemOfRecordAndIncludes", true);
    assertTrue(group.hasType(type));
    assertEquals(group.getAttribute("LDAPProvisioningExclude"), "true");
  }
}
