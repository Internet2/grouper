/*
 * @author mchyzer
 * $Id: AllSubjectTests.java,v 1.1 2009-03-20 19:56:41 mchyzer Exp $
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
    suite.addTestSuite(Test_subj_SourcesXmlResolver.class);
    suite.addTestSuite(TestGrouperSubject.class);
    suite.addTestSuite(Test_subj_CachingResolver.class);
    suite.addTestSuite(TestSubject.class);
    suite.addTestSuite(Test_subj_SubjectResolverFactory.class);
    suite.addTestSuite(Test_subj_SubjectResolver.class);
    //$JUnit-END$
    return suite;
  }

}
