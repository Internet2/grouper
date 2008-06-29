/*
 * @author mchyzer
 * $Id: GroupTypeHooksTest.java,v 1.1 2008-06-29 17:42:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.RegistryReset;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;


/**
 *
 */
public class GroupTypeHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupTypeHooksTest("testGroupTypePreDelete"));
    //TestRunner.run(new GroupTypeHooksTest(""));
    //TestRunner.run(GroupTypeHooksTest.class);
  }
  
  /**
   * @param name
   */
  public GroupTypeHooksTest(String name) {
    super(name);
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testGroupTypePreInsert() throws SchemaException, InsufficientPrivilegeException {
    
    GroupTypeHooksImpl.mostRecentPreInsertGroupTypeName = null;
    
    GroupType groupType = GroupType.createType(grouperSession, "test1");

    assertEquals("test1", groupType.getName());
    assertEquals("test1", GroupTypeHooksImpl.mostRecentPreInsertGroupTypeName);
    
    try {
      groupType = GroupType.createType(grouperSession, "test2");
      fail("Should veto test2");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_TYPE_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testGroupTypePreDelete() throws SchemaException, InsufficientPrivilegeException {
    
    GroupType groupType = GroupType.createType(grouperSession, "test5");
    
    GroupTypeHooksImpl.mostRecentPreDeleteGroupTypeName = null;
    
    groupType.delete(grouperSession);
    
    assertEquals("test5", GroupTypeHooksImpl.mostRecentPreDeleteGroupTypeName);
    
    groupType = GroupType.createType(grouperSession, "test6");
    try {
      groupType.delete(grouperSession);
      fail("Should veto test6");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_TYPE_PRE_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   */
  public void testGroupTypePostDelete() throws SchemaException, InsufficientPrivilegeException {
    
    GroupType groupType = GroupType.createType(grouperSession, "test7");

    GroupTypeHooksImpl.mostRecentPostDeleteGroupTypeName = null;
    
    groupType.delete(grouperSession);
    
    assertEquals("test7", GroupTypeHooksImpl.mostRecentPostDeleteGroupTypeName);
    
    groupType = GroupType.createType(grouperSession, "test8");
    try {
      groupType.delete(grouperSession);
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_TYPE_POST_DELETE, hookVeto.getVetoType());
    }

  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testGroupTypePostInsert() throws SchemaException, InsufficientPrivilegeException {
    
    GroupTypeHooksImpl.mostRecentPostInsertGroupTypeName = null;
    
    GroupType groupType = GroupType.createType(grouperSession, "test3");

    assertEquals("test3", groupType.getName());
    assertEquals("test3", GroupTypeHooksImpl.mostRecentPostInsertGroupTypeName);
    
    try {
      groupType = GroupType.createType(grouperSession, "test4");
      fail("Should veto test4");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test4", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_TYPE_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
   */
  @Override
  protected void tearDown() {
    overrideHooksRemove();
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP_TYPE.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   */
  private GrouperSession grouperSession = null;
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  protected void setUp () {
    try {
      overrideHooksAdd();
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      grouperSession     = SessionHelper.getRootSession();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP_TYPE.getPropertyFileKey(), 
        GroupTypeHooksImpl.class);
  }

}
