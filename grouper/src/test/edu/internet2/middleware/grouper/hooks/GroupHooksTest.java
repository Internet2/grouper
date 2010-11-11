/*
 * @author mchyzer
 * $Id: GroupHooksTest.java,v 1.11 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.GroupDeleteException;
import edu.internet2.middleware.grouper.exception.GroupModifyException;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperRollbackType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GroupHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new GroupHooksTest("testGroupPostCommitInsert"));
    //TestRunner.run(new GroupHooksTest("testGroupPostUpdate"));
    //TestRunner.run(GroupHooksTest.class);
    TestRunner.run(new GroupHooksTest("testPoc"));
  }
  
  /**
   * @param name
   */
  public GroupHooksTest(String name) {
    super(name);
  }
  
  /**
   * 
   */
  public void testGroupPostCommitInsert() {
    
    //simple case
    GroupHooksImpl.mostRecentPostCommitInsertGroupExtension = null;
    
    Group group = StemHelper.addChildGroup(this.edu, "test1a", "the test1a");
    
    assertEquals("test1a", group.getExtension());
    assertEquals("test1a", GroupHooksImpl.mostRecentPostCommitInsertGroupExtension);
    assertFalse("It should send a copy, not the actual gruop", GroupHooksImpl.mostRecentPostCommitInsertGroup == group);
    
    //try with a commit
    
    GroupHooksImpl.mostRecentPostCommitInsertGroupExtension = null;
    
    group = (Group)GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        Group theGroup = StemHelper.addChildGroup(GroupHooksTest.this.edu, "test2a", "the test2a");
        assertNull("shouldnt fire yet", GroupHooksImpl.mostRecentPostCommitInsertGroupExtension);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals("test2a", theGroup.getExtension());
        assertEquals("test2a", GroupHooksImpl.mostRecentPostCommitInsertGroupExtension);
        return theGroup;
      }
    });
    
    //try with a rollback
    
    GroupHooksImpl.mostRecentPostCommitInsertGroupExtension = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        Group theGroup = StemHelper.addChildGroup(GroupHooksTest.this.edu, "test3a", "the test3a");
        assertNull("shouldnt fire yet", GroupHooksImpl.mostRecentPostCommitInsertGroupExtension);
        assertEquals("test3a", theGroup.getExtension());
        grouperTransaction.rollback(GrouperRollbackType.ROLLBACK_NOW);
        assertNull("shouldnt fire yet", GroupHooksImpl.mostRecentPostCommitInsertGroupExtension);
        return null;
      }
    });
    
    assertNull("shouldnt fire at all", GroupHooksImpl.mostRecentPostCommitInsertGroupExtension);
    
  }

  /**
   * 
   */
  public void testGroupPostCommitUpdate() {
    
    final Group theGroup = StemHelper.addChildGroup(GroupHooksTest.this.edu, "test2b", "the test2b");

    GroupHooksImpl.mostRecentPostCommitUpdateGroupExtension = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          theGroup.setDescription("new description");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        theGroup.store();
        assertNull("shouldnt fire yet", GroupHooksImpl.mostRecentPostCommitUpdateGroupExtension);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals("test2b", theGroup.getExtension());
        assertEquals("test2b", GroupHooksImpl.mostRecentPostCommitUpdateGroupExtension);
        return theGroup;
      }
    });
  }

  /**
   * 
   */
  public void testGroupPostCommitDelete() {
    
    final Group theGroup = StemHelper.addChildGroup(GroupHooksTest.this.edu, "test2c", "the test2c");

    GroupHooksImpl.mostRecentPostCommitDeleteGroupExtension = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          theGroup.delete();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", GroupHooksImpl.mostRecentPostCommitDeleteGroupExtension);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals("test2c", theGroup.getExtension());
        assertEquals("test2c", GroupHooksImpl.mostRecentPostCommitDeleteGroupExtension);
        return theGroup;
      }
    });
  }

  /**
   * 
   */
  public void testGroupPreInsert() {
    
    GroupHooksImpl.mostRecentPreInsertGroupExtension = null;
    GroupHooksImpl2.mostRecentPreInsertGroupExtension = null;
    
    Group group = StemHelper.addChildGroup(this.edu, "test1", "the test1");
    
    assertEquals("test1", group.getExtension());
    assertEquals("test1", GroupHooksImpl.mostRecentPreInsertGroupExtension);
    assertEquals("test1", GroupHooksImpl2.mostRecentPreInsertGroupExtension);
    
    try {
      group = StemHelper.addChildGroup(this.edu, "test2", "the test2");
      fail("Should veto test2");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws GroupModifyException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testGroupPreUpdate() throws GroupModifyException, InsufficientPrivilegeException {
    
    
    Group group = StemHelper.addChildGroup(this.edu, "test9", "the test9");
    
    GroupHooksImpl.mostRecentPreUpdateGroupExtension = null;

    group.setDisplayExtension("the test9");
    group.store();

    assertEquals("test9", group.getExtension());
    assertEquals("test9", GroupHooksImpl.mostRecentPreUpdateGroupExtension);

    //remove hooks since an update hook is caleld on add
    overrideHooksRemove();
    group = StemHelper.addChildGroup(this.edu, "test10", "the test10");
    overrideHooksAdd();
    
    try {
      group.setDisplayExtension("the test10");
      group.store();
      fail("Should veto test10");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test10", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_PRE_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws GroupModifyException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testGroupPostUpdate() throws GroupModifyException, InsufficientPrivilegeException {
    
    
    Group group = StemHelper.addChildGroup(this.edu, "test11", "the test11");
    
    GroupHooksImpl.mostRecentPostUpdateGroupExtension = null;

    group.setDisplayExtension("the test11");
    group.store();

    assertEquals("test11", group.getExtension());
    assertEquals("test11", GroupHooksImpl.mostRecentPostUpdateGroupExtension);
    
    //remove hooks since an update hook is caleld on add
    overrideHooksRemove();
    group = StemHelper.addChildGroup(this.edu, "test12", "the test12");
    overrideHooksAdd();
    try {
      group.setDisplayExtension("the test12");
      group.store();
      fail("Should veto test12");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test12", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_POST_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * 
   */
  public void testGroupPostInsert() {
    
    GroupHooksImpl.mostRecentPostInsertGroupExtension = null;
    
    Group group = StemHelper.addChildGroup(this.edu, "test7", "the test7");
    
    assertEquals("test7", group.getExtension());
    assertEquals("test7", GroupHooksImpl.mostRecentPreInsertGroupExtension);
    
    try {
      group = StemHelper.addChildGroup(this.edu, "test8", "the test8");
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws GroupDeleteException 
   * 
   */
  public void testGroupPostDelete() throws InsufficientPrivilegeException, GroupDeleteException {
    
    GroupHooksImpl.mostRecentPostDeleteGroupExtension = null;

    Group group = StemHelper.addChildGroup(this.edu, "test4", "the test4");
    
    group.delete();
    
    assertEquals("test4", group.getExtension());
    assertEquals("test4", GroupHooksImpl.mostRecentPostDeleteGroupExtension);
   
    group = StemHelper.addChildGroup(this.edu, "test3", "the test3");
    try {
      group.delete();
      fail("Should veto test3");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test3", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_POST_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws GroupDeleteException 
   * 
   */
  public void testGroupPreDelete() throws InsufficientPrivilegeException, GroupDeleteException {
    
    GroupHooksImpl.mostRecentPreDeleteGroupExtension = null;

    Group group = StemHelper.addChildGroup(this.edu, "test5", "the test5");
    
    group.delete();
    
    assertEquals("test5", group.getExtension());
    assertEquals("test5", GroupHooksImpl.mostRecentPreDeleteGroupExtension);
   
    group = StemHelper.addChildGroup(this.edu, "test6", "the test6");
    try {
      group.delete();
      fail("Should veto test6");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUP_PRE_DELETE, hookVeto.getVetoType());
    }
    
  }

  /** edu stem */
  private Stem edu;
  
  /** root stem */
  private Stem root;
  
  /** grouper sesion */
  private GrouperSession grouperSession; 

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
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    overrideHooksAdd();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    GrouperTest.initGroupsAndAttributes();

    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), 
        GrouperUtil.toListClasses(GroupHooksImpl.class, GroupHooksImpl2.class));
  }

}
