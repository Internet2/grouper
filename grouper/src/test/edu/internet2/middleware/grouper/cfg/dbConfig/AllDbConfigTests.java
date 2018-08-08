/**
 * @author mchyzer
 * $Id$
 */
package edu.internet2.middleware.grouper.cfg.dbConfig;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 *
 */
public class AllDbConfigTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllDbConfigTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperConfigHibernateTest.class);
    //$JUnit-END$
    return suite;
  }

}
