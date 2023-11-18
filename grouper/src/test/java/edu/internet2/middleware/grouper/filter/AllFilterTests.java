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
 * @author mchyzer $Id: AllFilterTests.java,v 1.3 2009-11-05 06:10:51 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.filter;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 */
public class AllFilterTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllFilterTests.suite());
  }
  
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.finder");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestQueryMembershipModifiedAfter.class);
    suite.addTestSuite(TestGQGroupAttributeExact.class);
    suite.addTestSuite(TestGQGroupName.class);
    suite.addTestSuite(TestGQUnionFilter.class);
    suite.addTestSuite(TestGQStemCreatedAfter.class);
    suite.addTestSuite(TestGQGroupCreatedAfter.class);
    suite.addTestSuite(TestGQIntersectionFilter.class);
    suite.addTestSuite(Test_api_ChildStemFilter.class);
    suite.addTestSuite(TestGQGroupAlternateNameExactFilter.class);
    suite.addTestSuite(TestGQGroupExactName.class);
    suite.addTestSuite(TestQueryMembershipModifiedBefore.class);
    suite.addTestSuite(TestQuery.class);
    suite.addTestSuite(TestGQStemCreatedBefore.class);
    suite.addTestSuite(TestGQGroupAttribute.class);
    suite.addTestSuite(TestGQComplementFilter.class);
    suite.addTestSuite(TestGQGroupAnyAttribute.class);
    suite.addTestSuite(TestGQGroupCurrentNameExactFilter.class);
    suite.addTestSuite(TestGQNull.class);
    suite.addTestSuite(TestGQGroupCreatedBefore.class);
    suite.addTestSuite(TestGQGroupAlternateNameFilter.class);
    suite.addTestSuite(TestGQStemName.class);
    suite.addTestSuite(TestGroupTypeFilter.class);
    suite.addTestSuite(Test_api_ChildGroupFilter.class);
    suite.addTestSuite(TestGQGroupCurrentNameFilter.class);
    //$JUnit-END$
    return suite;
  }

}
