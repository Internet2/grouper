/*
 * @author mchyzer
 * $Id: AllAppTests.java,v 1.1 2009-03-20 19:56:42 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.app.gsh.AllGshTests;
import edu.internet2.middleware.grouper.app.loader.AllLoaderTests;
import edu.internet2.middleware.grouper.app.usdu.AllUsduTests;
import edu.internet2.middleware.grouper.cfg.GrouperConfig;


/**
 *
 */
public class AllAppTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app");
    //$JUnit-BEGIN$

    //$JUnit-END$
    suite.addTest(AllGshTests.suite());
    
    if (GrouperConfig.getPropertyBoolean("junit.test.loader", true)) {

      suite.addTest(AllLoaderTests.suite());
    }
    
    suite.addTest(AllUsduTests.suite());
    return suite;
  }

}
