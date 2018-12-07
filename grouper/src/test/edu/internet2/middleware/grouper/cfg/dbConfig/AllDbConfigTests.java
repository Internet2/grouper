/**
 * @author mchyzer $Id$
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
    suite.addTestSuite(ConfigFileMetadataTest.class);
    suite.addTestSuite(GrouperConfigHibernateTest.class);
    suite.addTestSuite(GrouperDbConfigTest.class);
    //$JUnit-END$
    return suite;
  }

}
