/*
 * @author mchyzer
 * $Id: AllSubjectTests.java,v 1.2 2009-08-12 04:52:21 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.subj;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllSubjectTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.subj");
    //$JUnit-BEGIN$
    suite.addTestSuite(Test_I_API_RegistrySubject_delete.class);
    suite.addTestSuite(TestGrouperSourceAdapter.class);
    suite.addTestSuite(TestSubjectFinder.class);
    suite.addTestSuite(TestInternalSourceAdapter.class);
    suite.addTestSuite(TestGrouperSubject.class);
    suite.addTestSuite(Test_subj_CachingResolver.class);
    suite.addTestSuite(TestSubject.class);
    suite.addTestSuite(Test_subj_SubjectResolverFactory.class);
    suite.addTestSuite(Test_subj_SubjectResolver.class);
    //$JUnit-END$
    return suite;
  }

}
