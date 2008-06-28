/*
 * @author mchyzer
 * $Id: CompositeHooksTest.java,v 1.1 2008-06-28 06:55:47 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.CompositeType;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.InsufficientPrivilegeException;
import edu.internet2.middleware.grouper.MemberAddException;
import edu.internet2.middleware.grouper.MemberDeleteException;
import edu.internet2.middleware.grouper.RegistryReset;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;


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
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
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
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  protected void setUp () {
    overrideHooksAdd();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
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
