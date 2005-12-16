/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package test.com.devclue.grouper;

import  com.devclue.grouper.registry.*;
import  junit.framework.*;

/**
 * Test <i>com.devclue.grouper.registry.*</i> classes.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestRegistry.java,v 1.1 2005-12-16 21:48:00 blair Exp $
 */
public class TestRegistry extends TestCase {

  public TestRegistry(String name) {
    super(name);
  }

  protected void setUp() {
    // Nothing
  }

  protected void tearDown() {
    // Nothing
  }

  /*
   * TESTS
   */

  /* GENERAL */
  // Test instantiation
  public void testInstantiation() {
    try {
      GroupsRegistry  gr = new GroupsRegistry();
      Assert.assertNotNull("gr !null", gr);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  /* GENERAL */

}

