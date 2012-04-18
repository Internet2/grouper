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
 * $Id: AttributeAssignValueHooksTest.java 6923 2010-08-11 05:06:01Z mchyzer $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;

import org.apache.commons.lang.ObjectUtils;

import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.attr.AttributeDef;
import edu.internet2.middleware.grouper.attr.AttributeDefName;
import edu.internet2.middleware.grouper.attr.AttributeDefNameSave;
import edu.internet2.middleware.grouper.attr.AttributeDefSave;
import edu.internet2.middleware.grouper.attr.AttributeDefValueType;
import edu.internet2.middleware.grouper.attr.value.AttributeAssignValue;
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
public class AttributeAssignValueHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new AttributeAssignValueHooksTest("testAttributeAssignValuePreDelete"));
    //TestRunner.run(AttributeAssignValueHooksTest.class);
  }
  
  /**
   * @param name
   */
  public AttributeAssignValueHooksTest(String name) {
    super(name);
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignValuePostCommitInsert() throws Exception {
    
    //simple case
    AttributeAssignValueHooksImpl.mostRecentPostCommitInsertAttributeAssignValue = null;
    
    assertNull(AttributeAssignValueHooksImpl.mostRecentPostCommitInsertAttributeAssignValue);
    
    this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 1L);
    
    assertTrue(ObjectUtils.equals(1L, 
        AttributeAssignValueHooksImpl.mostRecentPostCommitInsertAttributeAssignValue.getValueInteger()));
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignValuePostCommitUpdate() throws Exception {
    
    final AttributeAssignValue attributeAssignValue = this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 1L)
      .getAttributeAssignValueResult().getAttributeAssignValue();

    AttributeAssignValueHooksImpl.mostRecentPostCommitUpdateAttributeAssignValueId = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          attributeAssignValue.setValueInteger(3L);
          attributeAssignValue.saveOrUpdate();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeAssignValueHooksImpl.mostRecentPostCommitUpdateAttributeAssignValueId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals(attributeAssignValue.getId(), 
            AttributeAssignValueHooksImpl.mostRecentPostCommitUpdateAttributeAssignValueId);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignValuePostCommitDelete() throws Exception {
    
    final AttributeAssignValue attributeAssignValue = this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 1L)
      .getAttributeAssignValueResult().getAttributeAssignValue();

    AttributeAssignValueHooksImpl.mostRecentPostCommitDeleteAttributeAssignValueId = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          attributeAssignValue.delete();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeAssignValueHooksImpl.mostRecentPostCommitDeleteAttributeAssignValueId);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertEquals(attributeAssignValue.getId(), AttributeAssignValueHooksImpl.mostRecentPostCommitDeleteAttributeAssignValueId);
        return null;
      }
    });
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignValuePreInsert() throws Exception {
    
    AttributeAssignValueHooksImpl.mostRecentPreInsertValue = null;
    
    this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 1L)
      .getAttributeAssignValueResult().getAttributeAssignValue();
    
    assertTrue(ObjectUtils.equals(1L, AttributeAssignValueHooksImpl.mostRecentPreInsertValue));

    this.edu.getAttributeValueDelegate().deleteValueInteger(this.attributeDefName.getName(), 1L);
  

    try {
      this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 22L);

      fail("Should veto test22");
    } catch (HookVeto hookVeto) {
      assertEquals("value cannot be 22", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignValuePreUpdate() throws Exception {
    
    final AttributeAssignValue attributeAssignValue = this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 1L)
      .getAttributeAssignValueResult().getAttributeAssignValue();

    
    AttributeAssignValueHooksImpl.mostRecentPreUpdateValue = null;

    attributeAssignValue.setValueInteger(3L);
    attributeAssignValue.saveOrUpdate();

    assertTrue(ObjectUtils.equals(3L, AttributeAssignValueHooksImpl.mostRecentPreUpdateValue));

    try {
      attributeAssignValue.setValueInteger(2L);
      attributeAssignValue.saveOrUpdate();

      fail("Should veto 2");
    } catch (HookVeto hookVeto) {
      assertEquals("value cannot be 2", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_PRE_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignValuePostUpdate() throws Exception {
    
    final AttributeAssignValue attributeAssignValue = this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 1L)
      .getAttributeAssignValueResult().getAttributeAssignValue();
        
    AttributeAssignValueHooksImpl.mostRecentPostUpdateValue = null;

    attributeAssignValue.setValueInteger(3L);
    attributeAssignValue.saveOrUpdate();

    assertTrue(ObjectUtils.equals(3L, AttributeAssignValueHooksImpl.mostRecentPostUpdateValue));

    try {
      attributeAssignValue.setValueInteger(4L);
      attributeAssignValue.saveOrUpdate();

      fail("Should veto 4");
    } catch (HookVeto hookVeto) {
      assertEquals("value cannot be 4", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_POST_UPDATE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignValuePostInsert() throws Exception {
    
    AttributeAssignValueHooksImpl.mostRecentPostInsertValue = null;
    
    this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 1L);
    
    assertTrue(ObjectUtils.equals(1L, AttributeAssignValueHooksImpl.mostRecentPostInsertValue));
    
    //delete so we can insert again
    this.edu.getAttributeValueDelegate().deleteValueInteger(this.attributeDefName.getName(), 1L);

    try {
      this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 8L);
      
      fail("Should veto test8");
    } catch (HookVeto hookVeto) {
      assertEquals("value cannot be 8", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignValuePostDelete() throws Exception {
    
    this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 1L);

    AttributeAssignValueHooksImpl.mostRecentPostDeleteValue = null;

    this.edu.getAttributeValueDelegate().deleteValueInteger(this.attributeDefName.getName(), 1L);
    
    assertTrue("" + AttributeAssignValueHooksImpl.mostRecentPostDeleteValue, 
        ObjectUtils.equals(1L, AttributeAssignValueHooksImpl.mostRecentPostDeleteValue));
   
    this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 3L);

    try {
      this.edu.getAttributeValueDelegate().deleteValueInteger(this.attributeDefName.getName(), 3L);
      fail("Should veto 3");
    } catch (HookVeto hookVeto) {
      assertEquals("value cannot be 3", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_POST_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributeAssignValuePreDelete() throws Exception {
    
    this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 1L);
    
    AttributeAssignValueHooksImpl.mostRecentPreDeleteValue = null;
    
    this.edu.getAttributeValueDelegate().deleteValueInteger(this.attributeDefName.getName(), 1L);
    
    assertTrue(ObjectUtils.equals(1L, AttributeAssignValueHooksImpl.mostRecentPreDeleteValue));
   
    this.edu.getAttributeValueDelegate().assignValueInteger(this.attributeDefName.getName(), 6L);

    try {
      this.edu.getAttributeValueDelegate().deleteValueInteger(this.attributeDefName.getName(), 6L);
      fail("Should veto 6");
    } catch (HookVeto hookVeto) {
      assertEquals("value cannot be 6", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.ATTRIBUTE_ASSIGN_VALUE_PRE_DELETE, hookVeto.getVetoType());
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
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_ASSIGN_VALUE.getPropertyFileKey(), (Class<?>)null);
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
      this.attributeDef.setValueType(AttributeDefValueType.integer);
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
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE_ASSIGN_VALUE.getPropertyFileKey(), 
        AttributeAssignValueHooksImpl.class);
  }

}
