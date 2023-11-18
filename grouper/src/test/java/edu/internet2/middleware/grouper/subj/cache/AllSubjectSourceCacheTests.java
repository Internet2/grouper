/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.subj.cache;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 */
public class AllSubjectSourceCacheTests extends TestCase {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(AllSubjectSourceCacheTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(SubjectSourceCacheTest.class);
    //$JUnit-END$
    return suite;
  }

}
