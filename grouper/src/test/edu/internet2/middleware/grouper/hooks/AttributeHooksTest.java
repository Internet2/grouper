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
 * $Id: AttributeHooksTest.java,v 1.4 2009-03-24 17:12:08 mchyzer Exp $
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
import edu.internet2.middleware.grouper.attr.AttributeDefName;
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
    //TestRunner.run(new AttributeHooksTest("testAttributePostInsert"));
    //TestRunner.run(new AttributeHooksTest("testAttributePreDelete"));
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
    
    assertNull(AttributeHooksImpl.mostRecentPostCommitInsertAttribute);
    
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "testPostCommitInsert");
    
    assertEquals("testPostCommitInsert", AttributeHooksImpl.mostRecentPostCommitInsertAttribute.getValue());
    
  }

  /**
   * @throws Exception 
   * 
   */
  public void testAttributePostCommitUpdate() throws Exception {
    
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "testPostCommitUpdate");

    AttributeHooksImpl.mostRecentPostCommitUpdateAttributeValue = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          AttributeHooksTest.this.aGroup.setAttribute(AttributeHooksTest.this.attr.getLegacyAttributeName(true), "testPostCommitUpdate2");
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
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
    
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "testPostCommitDelete");

    AttributeHooksImpl.mostRecentPostCommitDeleteAttributeValue = null;
    
    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {

      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        try {
          AttributeHooksTest.this.aGroup.deleteAttribute(AttributeHooksTest.this.attr.getLegacyAttributeName(true));
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        assertNull("shouldnt fire yet", AttributeHooksImpl.mostRecentPostCommitDeleteAttributeValue);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        assertTrue(StringUtils.isBlank(aGroup.getAttributeValue(AttributeHooksTest.this.attr.getLegacyAttributeName(true), false, false)));
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
    
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test1");
    
    assertEquals("test1", GroupFinder.findByUuid(this.grouperSession, this.aGroup.getUuid(), true).getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));
    assertEquals("test1", AttributeHooksImpl.mostRecentPreInsertAttributeValue);

    //delete so we can insert again
    this.aGroup.deleteAttribute(this.attr.getLegacyAttributeName(true));
    
    try {
      this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test2");

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
    
    
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test9a");
    
    AttributeHooksImpl.mostRecentPreUpdateAttributeValue = null;

    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test9");

    assertEquals("test9", this.aGroup.getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));
    assertEquals("test9", AttributeHooksImpl.mostRecentPreUpdateAttributeValue);

    try {
      this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test10");

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
    
    
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test11a");
    
    AttributeHooksImpl.mostRecentPostUpdateAttributeValue = null;

    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test11");

    assertEquals("test11", this.aGroup.getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));
    assertEquals("test11", AttributeHooksImpl.mostRecentPostUpdateAttributeValue);

    try {
      this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test12");

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
    
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test7");
    
    assertEquals("test7", this.aGroup.getAttributeValue(this.attr.getLegacyAttributeName(true), false, true));
    assertEquals("test7", AttributeHooksImpl.mostRecentPreInsertAttributeValue);
    
    //delete so we can insert again
    this.aGroup.deleteAttribute(this.attr.getLegacyAttributeName(true));

    try {
      this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test8");
      
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
    
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test4");

    AttributeHooksImpl.mostRecentPostDeleteAttributeValue = null;

    this.aGroup.deleteAttribute(this.attr.getLegacyAttributeName(true));
    
    assertEquals("test4", AttributeHooksImpl.mostRecentPostDeleteAttributeValue);
   
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test3");

    try {
      this.aGroup.deleteAttribute(this.attr.getLegacyAttributeName(true));
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
    
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test5");

    AttributeHooksImpl.mostRecentPreDeleteAttributeValue = null;

    this.aGroup.deleteAttribute(this.attr.getLegacyAttributeName(true));
    
    assertEquals("test5", AttributeHooksImpl.mostRecentPreDeleteAttributeValue);
   
    this.aGroup.setAttribute(this.attr.getLegacyAttributeName(true), "test6");

    try {
      this.aGroup.deleteAttribute(this.attr.getLegacyAttributeName(true));
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
      
      this.attr = this.groupType.addAttribute(this.grouperSession, "anAttribute", 
          false);
      
      
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    overrideHooksAdd();
  }

  /** group type */
  private GroupType groupType = null;
  
  /** attr */
  private AttributeDefName attr = null;
  
  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.ATTRIBUTE.getPropertyFileKey(), 
        AttributeHooksImpl.class);
  }

}
