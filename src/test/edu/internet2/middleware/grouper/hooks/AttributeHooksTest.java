/*
 * @author mchyzer
 * $Id: AttributeHooksTest.java,v 1.3 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;

import org.apache.commons.lang.StringUtils;

import edu.internet2.middleware.grouper.Field;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;


/**
 *
 */
public class AttributeHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new AttributeHooksTest("testAttributePostCommitInsert"));
    //TestRunner.run(new AttributeHooksTest("testAttributePostUpdate"));
    TestRunner.run(AttributeHooksTest.class);
  }
  
  /**
   * @param name
   */
  public AttributeHooksTest(String name) {
    super(name);
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePostCommitInsert() throws Exception {
    
    //simple case
    AttributeHooksImpl.mostRecentPostCommitInsertAttribute = null;
    
    this.aGroup.setAttribute(this.field.getName(), "testPostCommitInsert");
    
    assertNull(AttributeHooksImpl.mostRecentPostCommitInsertAttribute);

    this.aGroup.store();
    
    assertEquals("testPostCommitInsert", AttributeHooksImpl.mostRecentPostCommitInsertAttribute.getValue());
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePostCommitUpdate() throws Exception {
    
    this.aGroup.setAttribute(this.field.getName(), "testPostCommitUpdate");
    this.aGroup.store();

    AttributeHooksImpl.mostRecentPostCommitUpdateAttributeValue = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          AttributeHooksTest.this.aGroup.setAttribute(AttributeHooksTest.this.field.getName(), "testPostCommitUpdate2");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        AttributeHooksTest.this.aGroup.store();
        assertNull("shouldnt fire yet", AttributeHooksImpl.mostRecentPostCommitUpdateAttributeValue);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals("testPostCommitUpdate2", AttributeHooksImpl.mostRecentPostCommitUpdateAttributeValue);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePostCommitDelete() throws Exception {
    
    this.aGroup.setAttribute(this.field.getName(), "testPostCommitDelete");
    this.aGroup.store();

    AttributeHooksImpl.mostRecentPostCommitDeleteAttributeValue = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          AttributeHooksTest.this.aGroup.deleteAttribute(AttributeHooksTest.this.field.getName());
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeHooksImpl.mostRecentPostCommitDeleteAttributeValue);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertTrue(StringUtils.isBlank(aGroup.getAttributeOrNull(AttributeHooksTest.this.field.getName())));
        assertEquals("testPostCommitDelete", AttributeHooksImpl.mostRecentPostCommitDeleteAttributeValue);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePreInsert() throws Exception {
    
    AttributeHooksImpl.mostRecentPreInsertAttributeValue = null;
    
    this.aGroup.setAttribute(this.field.getName(), "test1");
    this.aGroup.store();
    
    assertEquals("test1", GroupFinder.findByUuid(this.grouperSession, this.aGroup.getUuid(), true).getAttribute(this.field.getName()));
    assertEquals("test1", AttributeHooksImpl.mostRecentPreInsertAttributeValue);

    //delete so we can insert again
    this.aGroup.deleteAttribute(this.field.getName());
    
    try {
      this.aGroup.setAttribute(this.field.getName(), "test2");
      this.aGroup.store();
      fail("Should veto test2");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePreUpdate() throws Exception {
    
    
    this.aGroup.setAttribute(this.field.getName(), "test9a");
    this.aGroup.store();
    
    AttributeHooksImpl.mostRecentPreUpdateAttributeValue = null;

    this.aGroup.setAttribute(this.field.getName(), "test9");
    this.aGroup.store();

    assertEquals("test9", this.aGroup.getAttribute(this.field.getName()));
    assertEquals("test9", AttributeHooksImpl.mostRecentPreUpdateAttributeValue);

    try {
      this.aGroup.setAttribute(this.field.getName(), "test10");
      this.aGroup.store();
      fail("Should veto test10");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test10", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_PRE_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePostUpdate() throws Exception {
    
    
    this.aGroup.setAttribute(this.field.getName(), "test11a");
    this.aGroup.store();
    
    AttributeHooksImpl.mostRecentPostUpdateAttributeValue = null;

    this.aGroup.setAttribute(this.field.getName(), "test11");
    this.aGroup.store();

    assertEquals("test11", this.aGroup.getAttribute(this.field.getName()));
    assertEquals("test11", AttributeHooksImpl.mostRecentPostUpdateAttributeValue);

    try {
      this.aGroup.setAttribute(this.field.getName(), "test12");
      this.aGroup.store();
      fail("Should veto test12");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test12", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_POST_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePostInsert() throws Exception {
    
    AttributeHooksImpl.mostRecentPostInsertAttributeValue = null;
    
    this.aGroup.setAttribute(this.field.getName(), "test7");
    this.aGroup.store();
    
    assertEquals("test7", this.aGroup.getAttribute(this.field.getName()));
    assertEquals("test7", AttributeHooksImpl.mostRecentPreInsertAttributeValue);
    
    //delete so we can insert again
    this.aGroup.deleteAttribute(this.field.getName());

    try {
      this.aGroup.setAttribute(this.field.getName(), "test8");
      this.aGroup.store();
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePostDelete() throws Exception {
    
    this.aGroup.setAttribute(this.field.getName(), "test4");
    this.aGroup.store();

    AttributeHooksImpl.mostRecentPostDeleteAttributeValue = null;

    this.aGroup.deleteAttribute(this.field.getName());
    
    assertEquals("test4", AttributeHooksImpl.mostRecentPostDeleteAttributeValue);
   
    this.aGroup.setAttribute(this.field.getName(), "test3");
    this.aGroup.store();

    try {
      this.aGroup.deleteAttribute(this.field.getName());
      fail("Should veto test3");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test3", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_POST_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePreDelete() throws Exception {
    
    this.aGroup.setAttribute(this.field.getName(), "test5");
    this.aGroup.store();

    AttributeHooksImpl.mostRecentPreDeleteAttributeValue = null;

    this.aGroup.deleteAttribute(this.field.getName());
    
    assertEquals("test5", AttributeHooksImpl.mostRecentPreDeleteAttributeValue);
   
    this.aGroup.setAttribute(this.field.getName(), "test6");
    this.aGroup.store();
    try {
      this.aGroup.deleteAttribute(this.field.getName());
      fail("Should veto test6");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be test6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_PRE_DELETE, hookVeto.getVetoType());
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
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE.getPropertyFileKey(), (Class<?>)null);
  }

  /** add group */
  private Group aGroup = null;
  
  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    super.setUp();

    try {
      
      grouperSession     = GrouperSession.startRootSession();
      root  = StemHelper.findRootStem(grouperSession);
      edu   = StemHelper.addChildStem(root, "edu", "education");
      
      this.aGroup = edu.addChildGroup("aGroup", "aGroup");
      
      this.groupType = GroupType.createType(grouperSession, "groupType");
      
      this.aGroup.addType(this.groupType);
      
      this.field = this.groupType.addAttribute(this.grouperSession, "anAttribute", 
          AccessPrivilege.READ, AccessPrivilege.ADMIN, false);
      
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    overrideHooksAdd();
  }

  /** group type */
  private GroupType groupType = null;
  
  /** field */
  private Field field = null;
  
  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE.getPropertyFileKey(), 
        AttributeHooksImpl.class);
  }

}
