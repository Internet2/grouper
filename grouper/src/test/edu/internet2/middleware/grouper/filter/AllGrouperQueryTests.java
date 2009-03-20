/*
 * @author mchyzer
 * $Id: AllGrouperQueryTests.java,v 1.1 2009-03-20 15:11:32 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.filter;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllGrouperQueryTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.finder");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestGQNull.class);
    suite.addTestSuite(TestGQGroupName.class);
    suite.addTestSuite(TestGQGroupCreatedAfter.class);
    suite.addTestSuite(TestQuery.class);
    suite.addTestSuite(TestGQGroupAnyAttribute.class);
    suite.addTestSuite(TestGQStemName.class);
    suite.addTestSuite(TestGQIntersectionFilter.class);
    suite.addTestSuite(TestQueryMembershipModifiedAfter.class);
    suite.addTestSuite(TestGQUnionFilter.class);
    suite.addTestSuite(TestGQComplementFilter.class);
    suite.addTestSuite(TestGQStemCreatedAfter.class);
    suite.addTestSuite(TestGQGroupAttribute.class);
    suite.addTestSuite(TestGQGroupAttributeExact.class);
    suite.addTestSuite(TestGQGroupCreatedBefore.class);
    suite.addTestSuite(TestGQStemCreatedBefore.class);
    suite.addTestSuite(TestQueryMembershipModifiedBefore.class);
    //$JUnit-END$
    return suite;
  }

}
