/**
 * Copyright 2014 Internet2
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
 * $Id$
 */
package edu.internet2.middleware.grouper.hooks;

import org.apache.commons.lang.ObjectUtils;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.assign.AttributeAssign;
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
public class AttributeAssignHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignHooksTest("testAttributeAssignPreDelete"));
    //TestRunner.run(AttributeAssignHooksTest.class);
  }
  
  /**
   * @param name
   */
  public AttributeAssignHooksTest(String name) {
    super(name);
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignPostCommitInsert() throws Exception {
    
    //simple case
    AttributeAssignHooksImpl.mostRecentPostCommitInsertAttributeAssign = null;
    
    assertNull(AttributeAssignHooksImpl.mostRecentPostCommitInsertAttributeAssign);
    
    this.edu.getAttributeDelegate().assignAttribute(this.attributeDefName);
    
    assertEquals(this.attributeDefName.getName(), 
        AttributeAssignHooksImpl.mostRecentPostCommitInsertAttributeAssign.getAttributeDefName().getName());
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignPostCommitUpdate() throws Exception {
    
    final AttributeAssign attributeAssign = this.edu.getAttributeDelegate().assignAttribute(this.attributeDefName).getAttributeAssign();

    AttributeAssignHooksImpl.mostRecentPostCommitUpdateAttributeAssignId = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          attributeAssign.setEnabledTimeDb(3L);
          attributeAssign.saveOrUpdate(true);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeAssignHooksImpl.mostRecentPostCommitUpdateAttributeAssignId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals(attributeAssign.getId(), 
            AttributeAssignHooksImpl.mostRecentPostCommitUpdateAttributeAssignId);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignPostCommitDelete() throws Exception {
    
    final AttributeAssign attributeAssign = this.edu.getAttributeDelegate().assignAttribute(this.attributeDefName).getAttributeAssign();

    AttributeAssignHooksImpl.mostRecentPostCommitDeleteAttributeAssignId = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          attributeAssign.delete();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeAssignHooksImpl.mostRecentPostCommitDeleteAttributeAssignId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals(attributeAssign.getId(), AttributeAssignHooksImpl.mostRecentPostCommitDeleteAttributeAssignId);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignPreInsert() throws Exception {
    
    AttributeAssignHooksImpl.mostRecentPreInsertAttributeDefName = null;
    
    this.edu.getAttributeDelegate().assignAttribute(this.attributeDefName).getAttributeAssign();
    
    assertEquals(this.attributeDefName.getName(), AttributeAssignHooksImpl.mostRecentPreInsertAttributeDefName);
    
    AttributeDefName test2 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test2").save();
    
    try {
      this.edu.getAttributeDelegate().assignAttribute(test2).getAttributeAssign();

      fail("Should veto test2");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignPreUpdate() throws Exception {
    
    final AttributeAssign attributeAssign = this.edu.getAttributeDelegate().assignAttribute(this.attributeDefName).getAttributeAssign();
    
    AttributeAssignHooksImpl.mostRecentPreUpdateAttributeEnabledTime = null;

    attributeAssign.setEnabledTimeDb(3L);
    attributeAssign.saveOrUpdate(true);

    assertTrue(ObjectUtils.equals(3L, AttributeAssignHooksImpl.mostRecentPreUpdateAttributeEnabledTime));

    try {
      attributeAssign.setEnabledTimeDb(2L);
      attributeAssign.saveOrUpdate(true);

      fail("Should veto 2");
    } catch (HookVeto hookVeto) {
      assertEquals("enabledTime cannot be 2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_PRE_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignPostUpdate() throws Exception {
    
    
    final AttributeAssign attributeAssign = this.edu.getAttributeDelegate().assignAttribute(this.attributeDefName).getAttributeAssign();
    
    AttributeAssignHooksImpl.mostRecentPostUpdateEnabledTime = null;

    attributeAssign.setEnabledTimeDb(3L);
    attributeAssign.saveOrUpdate(true);

    assertTrue(ObjectUtils.equals(3L, AttributeAssignHooksImpl.mostRecentPostUpdateEnabledTime));

    try {
      attributeAssign.setEnabledTimeDb(4L);
      attributeAssign.saveOrUpdate(true);

      fail("Should veto 4");
    } catch (HookVeto hookVeto) {
      assertEquals("enabledTime cannot be 4", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_POST_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignPostInsert() throws Exception {
    
    AttributeAssignHooksImpl.mostRecentPostInsertAttributeDefName = null;
    
    final AttributeAssign attributeAssign = this.edu.getAttributeDelegate().assignAttribute(this.attributeDefName).getAttributeAssign();
    
    assertEquals(this.attributeDefName.getName(), AttributeAssignHooksImpl.mostRecentPostInsertAttributeDefName);
    
    //delete so we can insert again
    attributeAssign.delete();

    AttributeDefName test8 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test8").save();
    
    try {
      this.edu.getAttributeDelegate().assignAttribute(test8).getAttributeAssign();
      
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignPostDelete() throws Exception {
    
    final AttributeAssign attributeAssign = this.edu.getAttributeDelegate().assignAttribute(this.attributeDefName).getAttributeAssign();

    AttributeAssignHooksImpl.mostRecentPostDeleteAttributeDefName = null;

    attributeAssign.delete();
    
    assertEquals(this.attributeDefName.getName(), AttributeAssignHooksImpl.mostRecentPostDeleteAttributeDefName);
   
    AttributeDefName test3 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test3").save();
    this.edu.getAttributeDelegate().assignAttribute(test3).getAttributeAssign();

    try {
      this.edu.getAttributeDelegate().removeAttribute(test3);
      fail("Should veto test3");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test3", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_POST_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignPreDelete() throws Exception {
    
    AttributeAssign attributeAssign = this.edu.getAttributeDelegate().assignAttribute(this.attributeDefName).getAttributeAssign();
    
    AttributeAssignHooksImpl.mostRecentPreDeleteAttributeDefName = null;
    
    attributeAssign.delete();
    
    assertEquals(this.attributeDefName.getName(), AttributeAssignHooksImpl.mostRecentPreDeleteAttributeDefName);
   
    AttributeDefName test6 = new AttributeDefNameSave(this.grouperSession, this.attributeDef).assignName("edu:test6").save();
    attributeAssign = this.edu.getAttributeDelegate().assignAttribute(test6).getAttributeAssign();

    try {
      attributeAssign.delete();
      fail("Should veto test6");
    } catch (HookVeto hookVeto) {
      assertEquals("name cannot be edu:test6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_PRE_DELETE, hookVeto.getVetoType());
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
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_ASSIGN.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    super.setUp();

    try {
      
      this.grouperSession     = GrouperSession.startRootSession();
      this.root  = StemHelper.findRootStem(grouperSession);
      this.edu   = StemHelper.addChildStem(root, "edu", "education");
      
      this.attributeDef = new AttributeDefSave(this.grouperSession).assignName("edu:test").save();
      this.attributeDef.setAssignToStem(true);
      this.attributeDef.store();
      
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
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_ASSIGN.getPropertyFileKey(), 
        AttributeAssignHooksImpl.class);
  }

}
