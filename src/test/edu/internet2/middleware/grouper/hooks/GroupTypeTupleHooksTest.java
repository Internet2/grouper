/*
 * @author mchyzer
 * $Id: GroupTypeTupleHooksTest.java,v 1.1 2008-06-29 17:42:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupModifyException;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperConfig;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.RegistryReset;
import edu.internet2.middleware.grouper.SchemaException;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;


/**
 *
 */
public class GroupTypeTupleHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupTypeTupleHooksTest("testGroupTypeTuplePreInsert"));
    //TestRunner.run(new GroupTypeTupleHooksTest(""));
    //TestRunner.run(GroupTypeTupleHooksTest.class);
  }
  
  /**
   * @param name
   */
  public GroupTypeTupleHooksTest(String name) {
    super(name);
  }

  /** edu stem */
  private Stem edu;
  
  /** root stem */
  private Stem root;
  
  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * @throws GroupModifyException 
   * 
   */
  public void testGroupTypeTuplePreInsert() throws SchemaException, InsufficientPrivilegeException, GroupModifyException {
    
    Group group = StemHelper.addChildGroup(this.edu, "test1", "the test1");

    GroupTypeTupleHooksImpl.mostRecentPreInsertGroupTypeTupleName = null;
    
    group.addType(groupType);
    
    assertEquals("test1", GroupTypeTupleHooksImpl.mostRecentPreInsertGroupTypeTupleName);
    overrideHooksRemove();
    Group group2 = StemHelper.addChildGroup(this.edu, "test2", "the test2");
    overrideHooksAdd();
    try {
      group2.addType(groupType);
      fail("Should veto test2");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_TYPE_TUPLE_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * @throws GroupModifyException 
   * 
   */
  public void testGroupTypeTuplePreDelete() throws SchemaException, InsufficientPrivilegeException, GroupModifyException {
    
    Group group = StemHelper.addChildGroup(this.edu, "test5", "the test5");
    group.addType(groupType);

    GroupTypeTupleHooksImpl.mostRecentPreDeleteGroupTypeTupleName = null;
    
    group.deleteType(groupType);
    
    assertEquals("test5", GroupTypeTupleHooksImpl.mostRecentPreDeleteGroupTypeTupleName);
    Group group2 = StemHelper.addChildGroup(this.edu, "test6", "the test6");
    group2.addType(groupType);
    try {
      group2.deleteType(groupType);
      fail("Should veto test6");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_TYPE_TUPLE_PRE_DELETE, hookVeto.getVetoType());
    }

  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * @throws GroupModifyException 
   */
  public void testGroupTypeTuplePostDelete() throws SchemaException, InsufficientPrivilegeException, GroupModifyException {
    
    Group group = StemHelper.addChildGroup(this.edu, "test7", "the test7");
    group.addType(groupType);

    GroupTypeTupleHooksImpl.mostRecentPostDeleteGroupTypeTupleName = null;
    
    group.deleteType(groupType);
    
    assertEquals("test7", GroupTypeTupleHooksImpl.mostRecentPostDeleteGroupTypeTupleName);
    Group group2 = StemHelper.addChildGroup(this.edu, "test8", "the test8");
    group2.addType(groupType);
    try {
      group2.deleteType(groupType);
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_TYPE_TUPLE_POST_DELETE, hookVeto.getVetoType());
    }

  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * @throws GroupModifyException 
   * 
   */
  public void testGroupTypeTuplePostInsert() throws SchemaException, InsufficientPrivilegeException, GroupModifyException {
    
    Group group = StemHelper.addChildGroup(this.edu, "test3", "the test3");

    GroupTypeTupleHooksImpl.mostRecentPostInsertGroupTypeTupleName = null;
    
    group.addType(groupType);
    
    assertEquals("test3", GroupTypeTupleHooksImpl.mostRecentPostInsertGroupTypeTupleName);
    overrideHooksRemove();
    Group group2 = StemHelper.addChildGroup(this.edu, "test4", "the test4");
    overrideHooksAdd();
    try {
      group2.addType(groupType);
      fail("Should veto test4");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test4", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_TYPE_TUPLE_POST_INSERT, hookVeto.getVetoType());
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
    GrouperHookType.addHookOverride(GrouperHookType.GROUP_TYPE_TUPLE.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   */
  private GrouperSession grouperSession = null;
  /**
   * edu stem 
   */
  private GroupType groupType;
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  protected void setUp () {
    try {
      overrideHooksAdd();
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      grouperSession     = SessionHelper.getRootSession();
      groupType = GroupType.createType(grouperSession, "testType");
      root  = StemHelper.findRootStem(grouperSession);
      edu   = StemHelper.addChildStem(root, "edu", "education");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP_TYPE_TUPLE.getPropertyFileKey(), 
        GroupTypeTupleHooksImpl.class);
  }

}
