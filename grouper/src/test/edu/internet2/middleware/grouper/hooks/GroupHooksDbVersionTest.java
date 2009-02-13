/*
 * @author mchyzer
 * $Id: GroupHooksDbVersionTest.java,v 1.1.2.1 2009-02-13 20:54:18 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.Group;
import edu.internet2.middleware.grouper.GroupFinder;
import edu.internet2.middleware.grouper.GroupType;
import edu.internet2.middleware.grouper.GrouperSession;
import edu.internet2.middleware.grouper.GrouperTest;
import edu.internet2.middleware.grouper.SessionHelper;
import edu.internet2.middleware.grouper.Stem;
import edu.internet2.middleware.grouper.StemHelper;
import edu.internet2.middleware.grouper.hooks.logic.GrouperHookType;
import edu.internet2.middleware.grouper.privs.AccessPrivilege;
import edu.internet2.middleware.grouper.registry.RegistryReset;
import edu.internet2.middleware.grouper.util.GrouperUtil;


/**
 *
 */
public class GroupHooksDbVersionTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(new GroupHooksDbVersionTest("testGroupHooksDbVersion"));
    //TestRunner.run(GroupHooksDbVersionTest.class);
  }
  
  /**
   * @param name
   */
  public GroupHooksDbVersionTest(String name) {
    super(name);
  }

  /** edu stem */
  private Stem edu;
  
  /** root stem */
  private Stem root;
  
  /** grouper sesion */
  private GrouperSession grouperSession; 

  /**
   * @see edu.internet2.middleware.grouper.GrouperTest#tearDown()
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
   * @see edu.internet2.middleware.grouper.GrouperTest#setUp()
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
        GrouperUtil.toListClasses(GroupHookDbVersion.class));
  }

  /**
   * @throws Exception 
   * 
   */
  public void testGroupHooksDbVersion() throws Exception {
    
    //try an insert
    Group group = edu.addChildGroup("myGroup", "myGroup");
    
    group = GroupFinder.findByName(this.grouperSession, "edu:myGroup");
    
    group.setExtension("anotherExt");
    group.store();
    
  }
  
}
