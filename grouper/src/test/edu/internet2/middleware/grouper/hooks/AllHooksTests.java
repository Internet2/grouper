/**
 * Copyright 2014 Internet2
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
 */
/*
 * @author mchyzer $Id: AllHooksTests.java,v 1.14 2009-11-05 06:10:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.hooks.examples.AllHooksExamplesTests;

/**
 *
 */
public class AllHooksTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    @SuppressWarnings("unused")
    long start = System.currentTimeMillis();
    TestRunner.run(AllHooksTests.suite());
    //System.err.println("Took: " + (System.currentTimeMillis() - start));
  }

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.hooks");
    //$JUnit-BEGIN$
    suite.addTestSuite(ESCOGroupHooksTest.class);
    suite.addTestSuite(GroupTypeTupleHooksTest.class);
    suite.addTestSuite(GroupHooksTest.class);
    suite.addTestSuite(GroupHookUniqueTest.class);
    suite.addTestSuite(GroupHooksAddTypePostCommitTest.class);
    suite.addTestSuite(GroupHooksAddTypeTest.class);
    suite.addTestSuite(MembershipHooksTest.class);
    suite.addTestSuite(CompositeHooksTest.class);
    suite.addTestSuite(StemHooksTest.class);
    suite.addTestSuite(AttributeHooksTest.class);
    suite.addTestSuite(AttributeAssignHooksTest.class);
    suite.addTestSuite(FieldHooksTest.class);
    suite.addTestSuite(MemberHooksTest.class);
    suite.addTestSuite(GroupHooksDbVersionTest.class);
    suite.addTestSuite(GroupTypeHooksTest.class);
    suite.addTestSuite(LifecycleHooksTest.class);
    suite.addTestSuite(AttributeDefNameHooksTest.class);
    suite.addTestSuite(AttributeAssignValueHooksTest.class);
    suite.addTestSuite(AttributeDefHooksTest.class);
    suite.addTestSuite(ExternalSubjectHooksTest.class);
    //$JUnit-END$
    suite.addTest(AllHooksExamplesTests.suite());
    return suite;
  }

}
