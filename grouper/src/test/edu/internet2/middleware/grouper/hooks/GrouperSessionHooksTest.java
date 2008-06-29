/*
 * @author mchyzer
 * $Id: GrouperSessionHooksTest.java,v 1.1 2008-06-29 17:42:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.RegistryReset;
import edu.internet2.middleware.grouper.SessionException;
import edu.internet2.middleware.grouper.SubjectTestHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.hooks.logic.HookVeto;
import edu.internet2.middleware.grouper.hooks.logic.VetoTypeGrouper;


/**
 *
 */
public class GrouperSessionHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GrouperSessionHooksTest("testGrouperSessionPostDelete"));
    //TestRunner.run(new GrouperSessionHooksTest(""));
    //TestRunner.run(GrouperSessionHooksTest.class);
  }
  
  /**
   * @param name
   */
  public GrouperSessionHooksTest(String name) {
    super(name);
  }

  /**
   * @throws SessionException 
   * 
   */
  public void testGrouperSessionPreInsert() throws SessionException {
    
    GrouperSessionHooksImpl.mostRecentPreInsertGrouperSessionSubjectId = null;
    
    GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    assertEquals(SubjectTestHelper.SUBJ0.getId(), GrouperSessionHooksImpl.mostRecentPreInsertGrouperSessionSubjectId);
    
    try {
      GrouperSession.start(SubjectTestHelper.SUBJ1);
      fail("Should veto subj1");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ1.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUPER_SESSION_PRE_INSERT, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws SessionException 
   * 
   */
  public void testGrouperSessionPreDelete() throws SessionException {
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    GrouperSessionHooksImpl.mostRecentPreDeleteGrouperSessionSubjectId = null;
    
    grouperSession.stop();
    
    assertEquals(SubjectTestHelper.SUBJ0.getId(), GrouperSessionHooksImpl.mostRecentPreDeleteGrouperSessionSubjectId);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ4);

    try {
      grouperSession.stop();
      fail("Should veto subj4");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ4.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUPER_SESSION_PRE_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws SessionException 
   * 
   */
  public void testGrouperSessionPostDelete() throws SessionException {
    
    GrouperSession grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ0);
    
    GrouperSessionHooksImpl.mostRecentPostDeleteGrouperSessionSubjectId = null;
    
    grouperSession.stop();
    
    assertEquals(SubjectTestHelper.SUBJ0.getId(), GrouperSessionHooksImpl.mostRecentPostDeleteGrouperSessionSubjectId);

    grouperSession = GrouperSession.start(SubjectTestHelper.SUBJ5);

    try {
      grouperSession.stop();
      fail("Should veto subj5");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ5.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUPER_SESSION_POST_DELETE, hookVeto.getVetoType());
    }
    
  }

  /**
   * @throws SessionException 
   * 
   */
  public void testGrouperSessionPostInsert() throws SessionException {
    
    GrouperSessionHooksImpl.mostRecentPostInsertGrouperSessionSubjectId = null;
    
    GrouperSession.start(SubjectTestHelper.SUBJ2);
    
    assertEquals(SubjectTestHelper.SUBJ2.getId(), GrouperSessionHooksImpl.mostRecentPostInsertGrouperSessionSubjectId);
    
    try {
      GrouperSession.start(SubjectTestHelper.SUBJ3);
      fail("Should veto subj3");
    } catch (HookVeto hookVeto) {
      assertEquals("subjectId cannot be " + SubjectTestHelper.SUBJ3.getId(), hookVeto.getReason());
      assertEquals(VetoTypeGrouper.GROUPER_SESSION_POST_INSERT, hookVeto.getVetoType());
    }
    
  }

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
    GrouperHookType.addHookOverride(GrouperHookType.GROUPER_SESSION.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
   */
  protected void setUp () {
    overrideHooksAdd();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUPER_SESSION.getPropertyFileKey(), 
        GrouperSessionHooksImpl.class);
  }

}
