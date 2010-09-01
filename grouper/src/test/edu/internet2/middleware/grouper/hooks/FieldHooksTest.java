/*
 * @author mchyzer
 * $Id: FieldHooksTest.java,v 1.9 2009-04-13 16:53:08 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.SchemaException;
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
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.privs.Privilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 *
 */
public class FieldHooksTest extends GrouperTest {

  /** edu stem */
  @SuppressWarnings("unused")
  private Stem edu;
  

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new FieldHooksTest("testBuiltInAttributeValidator"));
    //TestRunner.run(new FieldHooksTest(""));
    TestRunner.run(FieldHooksTest.class);
  }
  
  /**
   * @param name
   */
  public FieldHooksTest(String name) {
    super(name);
  }

  /**
   * 
   */
  private Privilege read = AccessPrivilege.READ;
  
  /**
   * 
   */
  private Privilege write = AccessPrivilege.UPDATE;
  
  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testFieldPreInsert() throws SchemaException, InsufficientPrivilegeException {
    
    FieldHooksImpl.mostRecentPreInsertFieldName = null;
    
    Field field = this.groupType.addAttribute(grouperSession, "test1", read, write, true);
    
    assertEquals("test1", field.getName());
    assertEquals("test1", FieldHooksImpl.mostRecentPreInsertFieldName);
    
    try {
      this.groupType.addAttribute(grouperSession, "test2", read, write, true);
      fail("Should veto test2");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.FIELD_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testFieldPreDelete() throws SchemaException, InsufficientPrivilegeException {
    
    this.groupType.addAttribute(grouperSession, "test5", read, write, true);
    
    FieldHooksImpl.mostRecentPreDeleteFieldName = null;
    
    this.groupType.deleteField(grouperSession, "test5");
    
    assertEquals("test5", FieldHooksImpl.mostRecentPreDeleteFieldName);
    
    this.groupType.addAttribute(grouperSession, "test6", read, write, true);
    try {
      this.groupType.deleteField(grouperSession, "test6");
      fail("Should veto test6");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.FIELD_PRE_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testFieldPostDelete() throws SchemaException, InsufficientPrivilegeException {
    
    this.groupType.addAttribute(grouperSession, "test7", read, write, true);
    
    FieldHooksImpl.mostRecentPostDeleteFieldName = null;
    
    this.groupType.deleteField(grouperSession, "test7");
    
    assertEquals("test7", FieldHooksImpl.mostRecentPostDeleteFieldName);
    
    this.groupType.addAttribute(grouperSession, "test8", read, write, true);
    try {
      this.groupType.deleteField(grouperSession, "test8");
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.FIELD_POST_DELETE, hookVeto.getVetoType());
    }
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testFieldPostInsert() throws SchemaException, InsufficientPrivilegeException {
    
    FieldHooksImpl.mostRecentPostInsertFieldName = null;
    
    Field field = this.groupType.addAttribute(grouperSession, "test3", read, write, true);
    
    assertEquals("test3", field.getName());
    assertEquals("test3", FieldHooksImpl.mostRecentPostInsertFieldName);
    
    try {
      this.groupType.addAttribute(grouperSession, "test4", read, write, true);
      fail("Should veto test4");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test4", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.FIELD_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /** edu stem */
  private GroupType groupType;
  
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
    GrouperHookType.addHookOverride(GrouperHookType.FIELD.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   */
  private GrouperSession grouperSession = null;

  /**
   * root stem 
   */
  private Stem root;
  
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
    GrouperHookType.addHookOverride(GrouperHookType.FIELD.getPropertyFileKey(), 
        FieldHooksImpl.class);
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testFieldPostCommitDelete() throws SchemaException, InsufficientPrivilegeException {
    
    FieldHooksImpl.mostRecentPostCommitDeleteFieldName = null;
    
    this.groupType.addAttribute(grouperSession, "test7", read, write, true);

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          FieldHooksTest.this.groupType.deleteField(grouperSession, "test7");

        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
//this will happen in its own transaction for caching purposes...
//        assertNull("shouldnt fire yet: " + FieldHooksImpl.mostRecentPostCommitDeleteFieldName, 
//            FieldHooksImpl.mostRecentPostCommitDeleteFieldName);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals("test7", FieldHooksImpl.mostRecentPostCommitDeleteFieldName);
        return null;
      }
    });    
  }

  /**
   * @throws SchemaException 
   * @throws InsufficientPrivilegeException 
   * 
   */
  public void testFieldPostCommitInsert() throws SchemaException, InsufficientPrivilegeException {
    
    FieldHooksImpl.mostRecentPostCommitInsertFieldName = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          FieldHooksTest.this.groupType.addAttribute(grouperSession, "test3", read, write, true);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", FieldHooksImpl.mostRecentPostCommitInsertFieldName);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals("test3", FieldHooksImpl.mostRecentPostCommitInsertFieldName);
        return null;
      }
    });    
    
  }

}
