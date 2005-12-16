/*
 * Copyright (C) 2005 blair christensen.
 * All Rights Reserved.
 *
 * You may use and distribute under the same terms as Grouper itself.
 */

package test.com.devclue.grouper;

import  junit.framework.*;

/**
 * Run all <i>com.devclue.grouper</i> tests.
 * <p />
 * @author  blair christensen.
 * @version $Id: TestSuiteAll.java,v 1.1 2005-12-16 21:48:00 blair Exp $
 */
public class TestSuiteAll extends TestCase {

  public TestSuiteAll(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();

    suite.addTestSuite(TestGroups.class);
    suite.addTestSuite(TestMembers.class);
    suite.addTestSuite(TestRegistry.class);
    suite.addTestSuite(TestSessions.class);
    suite.addTestSuite(TestStems.class);
    suite.addTestSuite(TestSubjects.class);

    return suite;
  }

}

