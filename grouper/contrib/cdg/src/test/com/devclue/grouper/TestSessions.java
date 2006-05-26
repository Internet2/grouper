/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package com.devclue.grouper;

import  com.devclue.grouper.registry.*;
import  com.devclue.grouper.session.*;
import  edu.internet2.middleware.grouper.*;
import  java.util.*;
import  junit.framework.*;

/**
 * Test <i>com.devclue.grouper.session.*</i> classes.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSessions.java,v 1.1 2006-05-26 17:27:08 blair Exp $
 */
public class TestSessions extends TestCase {

  public TestSessions(String name) {
    super(name);
  }

  protected void setUp() {
    GroupsRegistry gr = new GroupsRegistry();
    gr.reset();
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
      SessionFactory sf = new SessionFactory();
      Assert.assertNotNull("sf !null", sf);
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  /* GENERAL */

  /* SESSIONFACTORY */
  public void testSessionFactoryClass() {
    try {
      GrouperSession s = SessionFactory.getSession();
      Assert.assertNotNull("s !null", s);
      Assert.assertTrue(
        "s id == root",
        s.getSubject().getId().equals("GrouperSystem")
      );
    }
    catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }
  /* SESSIONFACTORY */
 
}
 
