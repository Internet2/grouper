/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper;

import  com.devclue.grouper.registry.*;
import  junit.framework.*;

/**
 * Test <i>com.devclue.grouper.registry.*</i> classes.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestRegistry.java,v 1.1 2006-06-23 17:30:12 blair Exp $
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

