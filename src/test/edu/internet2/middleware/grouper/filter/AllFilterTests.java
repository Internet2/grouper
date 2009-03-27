/*
 * @author mchyzer
 * $Id: AllFilterTests.java,v 1.2 2009-03-27 23:28:53 shilen Exp $
 */
package edu.internet2.middleware.grouper.filter;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllFilterTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.finder");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestGQNull.class);
    suite.addTestSuite(TestGQGroupName.class);
    suite.addTestSuite(TestGQGroupCreatedAfter.class);
    suite.addTestSuite(TestQuery.class);
    suite.addTestSuite(Test_api_ChildGroupFilter.class);
    suite.addTestSuite(TestGQGroupAnyAttribute.class);
    suite.addTestSuite(TestGQStemName.class);
    suite.addTestSuite(TestGQIntersectionFilter.class);
    suite.addTestSuite(TestQueryMembershipModifiedAfter.class);
    suite.addTestSuite(Test_api_ChildStemFilter.class);
    suite.addTestSuite(TestGQUnionFilter.class);
    suite.addTestSuite(TestGQComplementFilter.class);
    suite.addTestSuite(TestGroupTypeFilter.class);
    suite.addTestSuite(TestGQStemCreatedAfter.class);
    suite.addTestSuite(TestGQGroupAttribute.class);
    suite.addTestSuite(TestGQGroupAttributeExact.class);
    suite.addTestSuite(TestGQGroupCreatedBefore.class);
    suite.addTestSuite(TestGQStemCreatedBefore.class);
    suite.addTestSuite(TestQueryMembershipModifiedBefore.class);
    suite.addTestSuite(TestGQGroupExactName.class);
    suite.addTestSuite(TestGQGroupCurrentNameExactFilter.class);
    suite.addTestSuite(TestGQGroupCurrentNameFilter.class);
    suite.addTestSuite(TestGQGroupAlternateNameExactFilter.class);
    suite.addTestSuite(TestGQGroupAlternateNameFilter.class);
    //$JUnit-END$
    return suite;
  }

}
