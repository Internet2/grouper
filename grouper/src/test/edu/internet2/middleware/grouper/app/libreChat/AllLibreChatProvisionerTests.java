package edu.internet2.middleware.grouper.app.libreChat;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit suite for the LibreChat provisioner tests (GRP-7594)
 */
public class AllLibreChatProvisionerTests extends TestCase {

  public static Test suite() {
    TestSuite suite = new TestSuite(AllLibreChatProvisionerTests.class.getName());
    //$JUnit-BEGIN$
    suite.addTestSuite(LibreChatProvisionerTest.class);
    //$JUnit-END$
    return suite;
  }

}
