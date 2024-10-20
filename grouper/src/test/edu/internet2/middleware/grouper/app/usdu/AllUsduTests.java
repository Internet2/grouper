/**
 * Copyright 2014 Internet2
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
 * @author mchyzer
 * $Id: AllUsduTests.java,v 1.1 2008-07-21 18:47:13 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.app.usdu;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * usdu tests
 */
public class AllUsduTests {

  /**
   * suite
   * @return the suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.app.usdu");
    //$JUnit-BEGIN$
    suite.addTestSuite(TestUSDU.class);
    suite.addTestSuite(UsduJobTest.class);
    suite.addTestSuite(UsduServiceTest.class);
    suite.addTestSuite(UsduJobProvisionerSyncTest.class);
    suite.addTestSuite(SubjectChangeDaemonTest.class);
    //$JUnit-END$
    return suite;
  }

}
