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
 * $Id: CompositeHooksTest.java,v 1.4 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.exception.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.exception.MemberAddException;
import edu.internet2.middleware.grouper.exception.MemberDeleteException;
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
import edu.internet2.middleware.grouper.misc.CompositeType;
import edu.internet2.middleware.grouper.registry.RegistryReset;


/**
 *
 */
public class CompositeHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new CompositeHooksTest("testCompositePostDelete"));
    //TestRunner.run(new CompositeHooksTest(""));
    //TestRunner.run(CompositeHooksTest.class);
  }
  
  /**
   * @param name
   */
  public CompositeHooksTest(String name) {
    super(name);
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws MemberAddException 
   */
  public void testCompositePreInsert() throws InsufficientPrivilegeException, MemberAddException {
    
    CompositeHooksImpl.mostRecentPreInsertCompositeExtension = null;
    
    group1.addCompositeMember(CompositeType.UNION, this.group2, this.group3);
    
    assertEquals(group2.getExtension(), CompositeHooksImpl.mostRecentPreInsertCompositeExtension);
    
    try {
      group4.addCompositeMember(CompositeType.UNION, this.group5, this.group6);
      fail("Should veto test5");
    } catch (HookVeto hookVeto) {
      assertEquals("extension cannot be test5", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.COMPOSITE_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws MemberAddException 
   */
  public void testCompositePostInsert() throws InsufficientPrivilegeException, MemberAddException {
    
    CompositeHooksImpl.mostRecentPostInsertCompositeExtension = null;
    
    group7.addCompositeMember(CompositeType.UNION, this.group8, this.group9);
    
    assertEquals(group8.getExtension(), CompositeHooksImpl.mostRecentPostInsertCompositeExtension);
    
    try {
      group10.addCompositeMember(CompositeType.UNION, this.group11, this.group12);
      fail("Should veto test11");
    } catch (HookVeto hookVeto) {
      assertEquals("extension cannot be test11", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.COMPOSITE_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws MemberAddException 
   */
  public void testCompositePostCommitInsert() throws InsufficientPrivilegeException, MemberAddException {
    
    CompositeHooksImpl.mostRecentPostCommitInsertCompositeExtension = null;

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          group7.addCompositeMember(CompositeType.UNION, CompositeHooksTest.this.group8, CompositeHooksTest.this.group9);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", CompositeHooksImpl.mostRecentPostCommitInsertCompositeExtension);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals(group8.getExtension(), CompositeHooksImpl.mostRecentPostCommitInsertCompositeExtension);
        return null;
      }
    });    
    
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws MemberAddException 
   * @throws MemberDeleteException 
   * 
   */
  public void testCompositePostDelete() throws InsufficientPrivilegeException, MemberAddException, MemberDeleteException {
    
    group7.addCompositeMember(CompositeType.UNION, this.group8, this.group9);
    
    CompositeHooksImpl.mostRecentPostDeleteCompositeExtension = null;
  
    group7.deleteCompositeMember();
    
    assertEquals(group8.getExtension(), CompositeHooksImpl.mostRecentPostDeleteCompositeExtension);
    
    group11.addCompositeMember(CompositeType.UNION, this.group10, this.group12);
    try {
      group11.deleteCompositeMember();
      fail("Should veto test11");
    } catch (HookVeto hookVeto) {
      assertEquals("extension cannot be test10", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.COMPOSITE_POST_DELETE, hookVeto.getVetoType());
    }
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws MemberAddException 
   * @throws MemberDeleteException 
   * 
   */
  public void testCompositePostCommitDelete() throws InsufficientPrivilegeException, MemberAddException, MemberDeleteException {
    
    group7.addCompositeMember(CompositeType.UNION, this.group8, this.group9);
    
    CompositeHooksImpl.mostRecentPostCommitDeleteCompositeExtension = null;

    GrouperTransaction.callbackGrouperTransaction(new GrouperTransactionHandler() {
  
      public Object callback(GrouperTransaction grouperTransaction)
          throws GrouperDAOException {
        
        try {
          group7.deleteCompositeMember();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
        
        assertNull("shouldnt fire yet", CompositeHooksImpl.mostRecentPostCommitDeleteCompositeExtension);
        grouperTransaction.commit(GrouperCommitType.COMMIT_NOW);
        
        assertEquals(group8.getExtension(), CompositeHooksImpl.mostRecentPostCommitDeleteCompositeExtension);
        return null;
      }
    });    

    
  }

  /**
   * @throws InsufficientPrivilegeException 
   * @throws MemberAddException 
   * @throws MemberDeleteException 
   * 
   */
  public void testCompositePreDelete() throws InsufficientPrivilegeException, MemberAddException, MemberDeleteException {
    
    group1.addCompositeMember(CompositeType.UNION, this.group2, this.group3);
    
    CompositeHooksImpl.mostRecentPreDeleteCompositeExtension = null;

    group1.deleteCompositeMember();
    
    assertEquals(group2.getExtension(), CompositeHooksImpl.mostRecentPreDeleteCompositeExtension);
    
    group5.addCompositeMember(CompositeType.UNION, this.group4, this.group6);
    try {
      group5.deleteCompositeMember();
      fail("Should veto test5");
    } catch (HookVeto hookVeto) {
      assertEquals("extension cannot be test4", hookVeto.getReason());
      assertEquals(VetoTypeGrouper.COMPOSITE_PRE_DELETE, hookVeto.getVetoType());
    }

  }

  /** edu composite */
  private Stem edu;
  
  /** root composite */
  private Stem root;
  
  /** group 1 */
  private Group group1;
  
  /** group2 */
  private Group group2;
  
  /** group3 */
  private Group group3;
  
  /** group 4 */
  private Group group4;
  
  /** group5 */
  private Group group5;
  
  /** group6 */
  private Group group6;

  /** group 7 */
  private Group group7;
  
  /** group8 */
  private Group group8;
  
  /** group9 */
  private Group group9;
  
  /** group 10 */
  private Group group10;
  
  /** group11 */
  private Group group11;
  
  /** group12 */
  private Group group12;
  

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
    GrouperHookType.addHookOverride(GrouperHookType.COMPOSITE.getPropertyFileKey(), (Class<?>)null);
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
    group1 = StemHelper.addChildGroup(this.edu, "test1", "the test1");
    group2 = StemHelper.addChildGroup(this.edu, "test2", "the test2");
    group3 = StemHelper.addChildGroup(this.edu, "test3", "the test3");
    group4 = StemHelper.addChildGroup(this.edu, "test4", "the test4");
    group5 = StemHelper.addChildGroup(this.edu, "test5", "the test5");
    group6 = StemHelper.addChildGroup(this.edu, "test6", "the test6");
    group7 = StemHelper.addChildGroup(this.edu, "test7", "the test7");
    group8 = StemHelper.addChildGroup(this.edu, "test8", "the test8");
    group9 = StemHelper.addChildGroup(this.edu, "test9", "the test9");
    group10 = StemHelper.addChildGroup(this.edu, "test10", "the test10");
    group11 = StemHelper.addChildGroup(this.edu, "test11", "the test11");
    group12 = StemHelper.addChildGroup(this.edu, "test12", "the test12");

  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.COMPOSITE.getPropertyFileKey(), 
        CompositeHooksImpl.class);
  }

}
