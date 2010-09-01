/*
 * @author mchyzer
 * $Id: StemHooksTest.java,v 1.6 2009-03-21 19:48:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.StemDeleteException;
import edu.internet2.middleware.grouper.exception.StemModifyException;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
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
public class StemHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new StemHooksTest("testStemPreInsert"));
    //TestRunner.run(new StemHooksTest(""));
    TestRunner.run(StemHooksTest.class);
  }
  
  /**
   * @param name
   */
  public StemHooksTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testStemPreInsert() {
    
    StemHooksImpl.mostRecentPreInsertStemExtension = null;
    
    Stem stem = StemHelper.addChildStem(this.edu, "test1", "the test1");
    
    assertEquals("test1", stem.getExtension());
    assertEquals("test1", StemHooksImpl.mostRecentPreInsertStemExtension);
    
    try {
      stem = StemHelper.addChildStem(this.edu, "test2", "the test2");
      fail("Should veto test2");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.STEM_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws StemModifyException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testStemPreUpdate() throws StemModifyException, InsufficientPrivilegeException {
    
    
    Stem stem = StemHelper.addChildStem(this.edu, "test9", "the test9");
    
    StemHooksImpl.mostRecentPreUpdateStemExtension = null;

    stem.setDisplayExtension("the test9");
    stem.store();

    assertEquals("test9", stem.getExtension());
    assertEquals("test9", StemHooksImpl.mostRecentPreUpdateStemExtension);

    //remove hooks since an update hook is caleld on add
    overrideHooksRemove();
    stem = StemHelper.addChildStem(this.edu, "test10", "the test10");
    overrideHooksAdd();
    
    try {
      stem.setDisplayExtension("the test10");
      stem.store();
      fail("Should veto test10");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test10", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.STEM_PRE_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws StemModifyException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testStemPostUpdate() throws StemModifyException, InsufficientPrivilegeException {
    
    
    Stem stem = StemHelper.addChildStem(this.edu, "test11", "the test11");
    
    StemHooksImpl.mostRecentPostUpdateStemExtension = null;

    stem.setDisplayExtension("the test11");
    stem.store();

    assertEquals("test11", stem.getExtension());
    assertEquals("test11", StemHooksImpl.mostRecentPostUpdateStemExtension);
    
    //remove hooks since an update hook is caleld on add
    overrideHooksRemove();
    stem = StemHelper.addChildStem(this.edu, "test12", "the test12");
    overrideHooksAdd();
    try {
      stem.setDisplayExtension("the test12");
      stem.store();
      fail("Should veto test12");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test12", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.STEM_POST_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * 
   */
  public void testStemPostInsert() {
    
    StemHooksImpl.mostRecentPostInsertStemExtension = null;
    
    Stem stem = StemHelper.addChildStem(this.edu, "test7", "the test7");
    
    assertEquals("test7", stem.getExtension());
    assertEquals("test7", StemHooksImpl.mostRecentPreInsertStemExtension);
    
    try {
      stem = StemHelper.addChildStem(this.edu, "test8", "the test8");
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.STEM_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws StemDeleteException 
   * 
   */
  public void testStemPostDelete() throws InsufficientPrivilegeException, StemDeleteException {
    
    StemHooksImpl.mostRecentPostDeleteStemExtension = null;

    Stem stem = StemHelper.addChildStem(this.edu, "test4", "the test4");
    
    stem.delete();
    
    assertEquals("test4", stem.getExtension());
    assertEquals("test4", StemHooksImpl.mostRecentPostDeleteStemExtension);
   
    stem = StemHelper.addChildStem(this.edu, "test3", "the test3");
    try {
      stem.delete();
      fail("Should veto test3");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test3", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.STEM_POST_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws StemDeleteException 
   * 
   */
  public void testStemPreDelete() throws InsufficientPrivilegeException, StemDeleteException {
    
    StemHooksImpl.mostRecentPreDeleteStemExtension = null;

    Stem stem = StemHelper.addChildStem(this.edu, "test5", "the test5");
    
    stem.delete();
    
    assertEquals("test5", stem.getExtension());
    assertEquals("test5", StemHooksImpl.mostRecentPreDeleteStemExtension);
   
    stem = StemHelper.addChildStem(this.edu, "test6", "the test6");
    try {
      stem.delete();
      fail("Should veto test6");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.STEM_PRE_DELETE, hookVeto.getVetoType());
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
    GrouperHookType.addHookOverride(GrouperHookType.STEM.getPropertyFileKey(), (Class<?>)null);
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
    GrouperHookType.addHookOverride(GrouperHookType.STEM.getPropertyFileKey(), 
        StemHooksImpl.class);
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws StemDeleteException 
   * 
   */
  public void testStemPostCommitDelete() throws InsufficientPrivilegeException, StemDeleteException {
    
    final Stem stem = StemHelper.addChildStem(this.edu, "test4", "the test4");
    
    StemHooksImpl.mostRecentPostCommitDeleteStemExtension = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          stem.delete();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", StemHooksImpl.mostRecentPostCommitDeleteStemExtension);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals("test4", StemHooksImpl.mostRecentPostCommitDeleteStemExtension);
        return null;
      }
    });    
  }

  /**
   * 
   */
  public void testStemPostCommitInsert() {
    
    StemHooksImpl.mostRecentPostCommitInsertStemExtension = null;

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          StemHelper.addChildStem(StemHooksTest.this.edu, "test7", "the test7");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", StemHooksImpl.mostRecentPostCommitInsertStemExtension);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals("test7", StemHooksImpl.mostRecentPostCommitInsertStemExtension);
        return null;
      }
    });    
  }

  /**
   * @throws StemModifyException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testStemPostCommitUpdate() throws StemModifyException, InsufficientPrivilegeException {
    
    final Stem stem = StemHelper.addChildStem(this.edu, "test11", "the test11");

    StemHooksImpl.mostRecentPostCommitUpdateStemExtension = null;

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
      
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          stem.setDisplayExtension("the test11");
          stem.store();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", StemHooksImpl.mostRecentPostCommitUpdateStemExtension);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals("test11", StemHooksImpl.mostRecentPostCommitUpdateStemExtension);
        return null;
      }
    });    
  }

}
