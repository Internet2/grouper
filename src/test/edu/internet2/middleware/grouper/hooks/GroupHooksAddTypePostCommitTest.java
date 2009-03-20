/*
 * @author mchyzer
 * $Id: GroupHooksAddTypePostCommitTest.java,v 1.4 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.helper.SessionHelper;
import edu.internet2.middleware.grouper.helper.StemHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GroupHooksAddTypePostCommitTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupHooksAddTypePostCommitTest("testGroupHooksAddType"));
    //TestRunner.run(GroupHooksAddTypePostCommitTest.class);
  }
  
  /**
   * @param name
   */
  public GroupHooksAddTypePostCommitTest(String name) {
    super(name);
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
    super.tearDown();
    overrideHooksRemove();
  }

  /**
   * 
   */
  private void overrideHooksRemove() {
    //dont have the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), (Class<?>)null);
  }

  /**
   * 
   * @see edu.internet2.middleware.grouper.helper.GrouperTest#setUp()
   */
  protected void setUp () {
    super.setUp();
    overrideHooksAdd();
    RegistryReset.internal_resetRegistryAndAddTestSubjects();
    grouperSession     = SessionHelper.getRootSession();
    root  = StemHelper.findRootStem(grouperSession);
    edu   = StemHelper.addChildStem(root, "edu", "education");
  }

  /**
   * 
   */
  private void overrideHooksAdd() {
    //this is the test hook imple
    GrouperHookType.addHookOverride(GrouperHookType.GROUP.getPropertyFileKey(), 
        GrouperUtil.toListClasses(GroupHookAddTypePostCommit.class));
  }

  /**
   * @throws Exception 
   * 
   */
  public void testGroupHooksAddType() throws Exception {
    GrouperSession grouperSession = GrouperSession.startRootSession();
    
    //make sure type exists
    GroupType fubGroup = GroupType.createType(grouperSession, "fubGroup", false);
    
    //lets add an attribute
    fubGroup.addAttribute(grouperSession, "gid", AccessPrivilege.READ, AccessPrivilege.ADMIN, false, false);
    
    //try an insert
    Group group = edu.addChildGroup("myGroup", "myGroup");
    
    //requery
    group = GroupFinder.findByName(grouperSession, "edu:myGroup", true);
    
    assertTrue(group.getTypes().contains(fubGroup));
    
    assertEquals("2", group.getAttribute("gid"));
  }
  
}
