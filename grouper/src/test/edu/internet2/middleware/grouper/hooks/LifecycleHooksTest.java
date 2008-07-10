/*
 * @author mchyzer
 * $Id: LifecycleHooksTest.java,v 1.1 2008-07-10 00:46:53 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.GrouperTest;


/**
 *
 */
public class LifecycleHooksTest extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    //TestRunner.run(new LifecycleHooksTest("sdfsad"));
    TestRunner.run(LifecycleHooksTest.class);
  }
  
  /**
   * @param name
   */
  public LifecycleHooksTest(String name) {
    super(name);
  }

  /**
   * 
   */
  public void testLifecycle() {
    assertTrue("Should be set by hook", LifecycleHooksImpl.hitGrouperStartup);
    assertTrue("Should be set by hook", LifecycleHooksImpl.hitHibernateInit);
    assertTrue("Should be set by hook", LifecycleHooksImpl.hitHooksInit);
  }
}