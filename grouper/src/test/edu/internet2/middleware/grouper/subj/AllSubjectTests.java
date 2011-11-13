/*
 * @author mchyzer $Id: AllSubjectTests.java,v 1.3 2009-11-05 06:10:51 mchyzer Exp $
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
    suite.addTestSuite(TestGrouperSubject.class);
    suite.addTestSuite(TestGrouperSourceAdapter.class);
    suite.addTestSuite(Test_subj_SubjectResolverFactory.class);
    suite.addTestSuite(Test_subj_CachingResolver.class);
    suite.addTestSuite(Test_subj_SubjectResolver.class);
    suite.addTestSuite(Test_I_API_RegistrySubject_delete.class);
    suite.addTestSuite(TestSubjectDecorator.class);
    suite.addTestSuite(TestSubjectFinder.class);
    suite.addTestSuite(TestSubject.class);
    suite.addTestSuite(TestInternalSourceAdapter.class);
    //$JUnit-END$
    return suite;
  }

}
