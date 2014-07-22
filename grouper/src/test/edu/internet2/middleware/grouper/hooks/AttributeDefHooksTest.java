/**
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
 */
/*
 * @author mchyzer
 * $Id: AttributeDefHooksTest.java 6921 2010-08-10 21:03:10Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.finder.AttributeDefFinder;
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
public class AttributeDefHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeDefHooksTest("testAttributeDefPreDelete"));
    //TestRunner.run(AttributeDefHooksTest.class);
  }
  
  /**
   * @param name
   */
  public AttributeDefHooksTest(String name) {
    super(name);
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPostCommitInsert() throws Exception {
    
    //simple case
    AttributeDefHooksImpl.mostRecentPostCommitInsertAttributeDef = null;
    
    assertNull(AttributeDefHooksImpl.mostRecentPostCommitInsertAttributeDef);
    
    new AttributeDefSave(this.grouperSession).assignName("edu:testPostCommitInsert").save();
    
    assertEquals("edu:testPostCommitInsert", AttributeDefHooksImpl.mostRecentPostCommitInsertAttributeDef.getName());
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPostCommitUpdate() throws Exception {
    
    this.attributeDef.setDescription("testPostCommitUpdate");
    this.attributeDef.store();
    AttributeDefHooksImpl.mostRecentPostCommitUpdateAttributeDefName = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          AttributeDefHooksTest.this.attributeDef.setDescription("testPostCommitUpdate2");
          AttributeDefHooksTest.this.attributeDef.store();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeDefHooksImpl.mostRecentPostCommitUpdateAttributeDefName);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals(AttributeDefHooksTest.this.attributeDef.getName(), 
            AttributeDefHooksImpl.mostRecentPostCommitUpdateAttributeDefName);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPostCommitDelete() throws Exception {
    
    AttributeDefHooksImpl.mostRecentPostCommitDeleteAttributeDefName = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          AttributeDefHooksTest.this.attributeDef.delete();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeDefHooksImpl.mostRecentPostCommitDeleteAttributeDefName);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertNull(AttributeDefFinder.findByName(AttributeDefHooksTest.this.attributeDef.getName(), false));
        assertEquals(AttributeDefHooksTest.this.attributeDef.getName(), AttributeDefHooksImpl.mostRecentPostCommitDeleteAttributeDefName);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPreInsert() throws Exception {
    
    AttributeDefHooksImpl.mostRecentPreInsertAttributeDefName = null;
    
    AttributeDef test1 = new AttributeDefSave(this.grouperSession).assignName("edu:test1").save();
    
    assertEquals("edu:test1", AttributeDefHooksImpl.mostRecentPreInsertAttributeDefName);

    //delete so we can insert again
    test1.delete();
    
    try {
      new AttributeDefSave(this.grouperSession).assignName("edu:test2").save();

      fail("Should veto test2");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPreUpdate() throws Exception {
    
    AttributeDef test9a = new AttributeDefSave(this.grouperSession).assignName("edu:test9a").save();
    
    AttributeDefHooksImpl.mostRecentPreUpdateAttributeDefName = null;

    test9a.setDescription("new");
    test9a.store();

    assertEquals("edu:test9a", AttributeDefHooksImpl.mostRecentPreUpdateAttributeDefName);

    try {
      test9a.setDescription("test10");
      test9a.store();

      fail("Should veto test10");
    } catch (HookVeto hookVeto) {
      assertEquals("description cannot be test10", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_PRE_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPostUpdate() throws Exception {
    
    
    AttributeDef test11a = new AttributeDefSave(this.grouperSession).assignName("edu:test11a").save();
    
    AttributeDefHooksImpl.mostRecentPostUpdateAttributeDefName = null;

    test11a.setDescription("new");
    test11a.store();

    assertEquals("edu:test11a", AttributeDefHooksImpl.mostRecentPostUpdateAttributeDefName);

    try {
      test11a.setDescription("test12");
      test11a.store();

      fail("Should veto test12");
    } catch (HookVeto hookVeto) {
      assertEquals("description cannot be test12", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_POST_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPostInsert() throws Exception {
    
    AttributeDefHooksImpl.mostRecentPostInsertAttributeDefName = null;
    
    AttributeDef test7 = new AttributeDefSave(this.grouperSession).assignName("edu:test7").save();
    
    assertEquals("edu:test7", AttributeDefHooksImpl.mostRecentPreInsertAttributeDefName);
    
    //delete so we can insert again
    test7.delete();

    try {
      new AttributeDefSave(this.grouperSession).assignName("edu:test8").save();
      
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPostDelete() throws Exception {
    
    AttributeDef test4 = new AttributeDefSave(this.grouperSession).assignName("edu:test4").save();

    AttributeDefHooksImpl.mostRecentPostDeleteAttributeDefName = null;

    test4.delete();
    
    assertEquals("edu:test4", AttributeDefHooksImpl.mostRecentPostDeleteAttributeDefName);
   
    AttributeDef test3 = new AttributeDefSave(this.grouperSession).assignName("edu:test3").save();

    try {
      test3.delete();
      fail("Should veto test3");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test3", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_POST_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeDefPreDelete() throws Exception {
    
    AttributeDef test5 = new AttributeDefSave(this.grouperSession).assignName("edu:test5").save();

    AttributeDefHooksImpl.mostRecentPreDeleteAttributeDefName = null;

    test5.delete();
    
    assertEquals("edu:test5", AttributeDefHooksImpl.mostRecentPreDeleteAttributeDefName);
   
    AttributeDef test6 = new AttributeDefSave(this.grouperSession).assignName("edu:test6").save();

    try {
      test6.delete();
      fail("Should veto test6");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_DEF_PRE_DELETE, hookVeto.getVetoType());
    }
    
  }

  /** edu stem */
  private Stem edu;
  
  /** attributeDef */
  private AttributeDef attributeDef;
  
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
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_DEF.getPropertyFileKey(), (Class<?>)null);
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
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_DEF.getPropertyFileKey(), 
        AttributeDefHooksImpl.class);
  }

}
