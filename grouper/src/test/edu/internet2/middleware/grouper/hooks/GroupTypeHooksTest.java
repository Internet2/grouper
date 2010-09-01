/*
 * @author mchyzer
 * $Id: GroupTypeHooksTest.java,v 1.4 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.registry.RegistryReset;


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
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#tearDown()
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
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    try {
      overrideHooksAdd();
      RegistryReset.internal_resetRegistryAndAddTestSubjects();
      GrouperTest.initGroupsAndAttributes();

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

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   */
  public void testGroupTypePostCommitDelete() throws SchemaException, InsufficientPrivilegeException {
    
    final GroupType groupType = GroupType.createType(grouperSession, "test7");
  
    GroupTypeHooksImpl.mostRecentPostCommitDeleteGroupTypeName = null;


    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          groupType.delete(grouperSession);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", GroupTypeHooksImpl.mostRecentPostCommitDeleteGroupTypeName);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals("test7", GroupTypeHooksImpl.mostRecentPostCommitDeleteGroupTypeName);
        return null;
      }
    });    

    
    
    
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testGroupTypePostCommitInsert() throws SchemaException, InsufficientPrivilegeException {
    
    GroupTypeHooksImpl.mostRecentPostCommitInsertGroupTypeName = null;

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          GroupType.createType(grouperSession, "test3");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", GroupTypeHooksImpl.mostRecentPostCommitInsertGroupTypeName);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals("test3", GroupTypeHooksImpl.mostRecentPostCommitInsertGroupTypeName);
        return null;
      }
    });    
    
  }

}
