/*******************************************************************************
 * Copyright 2012 Internet2
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
/*
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefNameFinder;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hibernate.GrouperCommitType;
import edu.internet2.middleware.grouper.hibernate.GrouperTransaction;
import edu.internet2.middleware.grouper.hibernate.GrouperTransactionHandler;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;
import edu.internet2.middleware.grouper.internal.dao.GrouperDAOException;


/**
 *
 */
public class AttributeDefNameHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefNameHooksTest("testAttributeDefNamePreDelete"));
    //TestRunner.run(AttributeDefNameHooksTest.class);
  }
  
  /**
   * @param name
   */
  public AttributeDefNameHooksTest(String name) {
    super(name);
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefNamePostCommitInsert() throws Exception {
    
    //simple case
    AttributeDefNameHooksImpl.mostRecentPostCommitInsertAttributeDefName = null;
    
    assertNull(AttributeDefNameHooksImpl.mostRecentPostCommitInsertAttributeDefName);
    
    new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:testPostCommitInsert").save();
    
    assertEquals("edu:testPostCommitInsert", AttributeDefNameHooksImpl.mostRecentPostCommitInsertAttributeDefName.getName());
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefNamePostCommitUpdate() throws Exception {
    
    this.attributeDefName.setDescription("testPostCommitUpdate");
    this.attributeDefName.store();
    AttributeDefNameHooksImpl.mostRecentPostCommitUpdateAttributeDefNameName = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          AttributeDefNameHooksTest.this.attributeDefName.setDescription("testPostCommitUpdate2");
          AttributeDefNameHooksTest.this.attributeDefName.store();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeDefNameHooksImpl.mostRecentPostCommitUpdateAttributeDefNameName);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals(AttributeDefNameHooksTest.this.attributeDefName.getName(), 
            AttributeDefNameHooksImpl.mostRecentPostCommitUpdateAttributeDefNameName);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefNamePostCommitDelete() throws Exception {
    
    AttributeDefNameHooksImpl.mostRecentPostCommitDeleteAttributeDefNameName = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          AttributeDefNameHooksTest.this.attributeDefName.delete();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeDefNameHooksImpl.mostRecentPostCommitDeleteAttributeDefNameName);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertNull(AttributeDefNameFinder.findByName(AttributeDefNameHooksTest.this.attributeDefName.getName(), false));
        assertEquals(AttributeDefNameHooksTest.this.attributeDefName.getName(), AttributeDefNameHooksImpl.mostRecentPostCommitDeleteAttributeDefNameName);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefNamePreInsert() throws Exception {
    
    AttributeDefNameHooksImpl.mostRecentPreInsertAttributeDefNameName = null;
    
    AttributeDefName test1 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test1").save();
    
    assertEquals("edu:test1", AttributeDefNameHooksImpl.mostRecentPreInsertAttributeDefNameName);

    //delete so we can insert again
    test1.delete();
    
    try {
      new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test2").save();

      fail("Should veto test2");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_NAME_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefNamePreUpdate() throws Exception {
    
    AttributeDefName test9a = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test9a").save();
    
    AttributeDefNameHooksImpl.mostRecentPreUpdateAttributeDefNameName = null;

    test9a.setDescription("new");
    test9a.store();

    assertEquals("edu:test9a", AttributeDefNameHooksImpl.mostRecentPreUpdateAttributeDefNameName);

    try {
      test9a.setDescription("test10");
      test9a.store();

      fail("Should veto test10");
    } catch (HookVeto hookVeto) {
      assertEquals("description cannot be test10", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_NAME_PRE_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefNamePostUpdate() throws Exception {
    
    
    AttributeDefName test11a = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test11a").save();
    
    AttributeDefNameHooksImpl.mostRecentPostUpdateAttributeDefNameName = null;

    test11a.setDescription("new");
    test11a.store();

    assertEquals("edu:test11a", AttributeDefNameHooksImpl.mostRecentPostUpdateAttributeDefNameName);

    try {
      test11a.setDescription("test12");
      test11a.store();

      fail("Should veto test12");
    } catch (HookVeto hookVeto) {
      assertEquals("description cannot be test12", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_NAME_POST_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefNamePostInsert() throws Exception {
    
    AttributeDefNameHooksImpl.mostRecentPostInsertAttributeDefNameName = null;
    
    AttributeDefName test7 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test7").save();
    
    assertEquals("edu:test7", AttributeDefNameHooksImpl.mostRecentPreInsertAttributeDefNameName);
    
    //delete so we can insert again
    test7.delete();

    try {
      new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test8").save();
      
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_NAME_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefNamePostDelete() throws Exception {
    
    AttributeDefName test4 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test4").save();

    AttributeDefNameHooksImpl.mostRecentPostDeleteAttributeDefNameName = null;

    test4.delete();
    
    assertEquals("edu:test4", AttributeDefNameHooksImpl.mostRecentPostDeleteAttributeDefNameName);
   
    AttributeDefName test3 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test3").save();

    try {
      test3.delete();
      fail("Should veto test3");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test3", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_NAME_POST_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefNamePreDelete() throws Exception {
    
    AttributeDefName test5 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test5").save();

    AttributeDefNameHooksImpl.mostRecentPreDeleteAttributeDefNameName = null;

    test5.delete();
    
    assertEquals("edu:test5", AttributeDefNameHooksImpl.mostRecentPreDeleteAttributeDefNameName);
   
    AttributeDefName test6 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test6").save();

    try {
      test6.delete();
      fail("Should veto test6");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_NAME_PRE_DELETE, hookVeto.getVetoType());
    }
    
  }

  /** edu stem */
  private Stem edu;
  
  /** attributeDef */
  private AttributeDef attributeDef;
  
  /** attributeDefName */
  private AttributeDefName attributeDefName;
  
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
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_DEF_NAME.getPropertyFileKey(), (Class<?>)null);
  }

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
      
      this.attributeDef = new AttributeDefSave(this.grouperSession).assignName("edu:test").save();
      this.attributeDefName = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test").save();
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    overrideHooksAdd();
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_DEF_NAME.getPropertyFileKey(), 
        AttributeDefNameHooksImpl.class);
  }

}
