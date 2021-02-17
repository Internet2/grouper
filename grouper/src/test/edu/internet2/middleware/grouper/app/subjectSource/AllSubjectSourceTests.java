package edu.internet2.middleware.grouper.app.subjectSource;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class AllSubjectSourceTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllSubjectSourceTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(SubjectSourceConfigurationTest.class);
    //$JUnit-END$
    return suite;
  }

}
