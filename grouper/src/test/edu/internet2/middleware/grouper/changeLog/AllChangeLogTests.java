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
 * $Id: AllChangeLogTests.java,v 1.2 2009-05-26 06:50:56 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.changeLog;

import edu.internet2.middleware.grouper.changeLog.esb.consumer.AllEsbConsumerTests;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 *
 */
public class AllChangeLogTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.changeLog");
    suite.addTest(AllEsbConsumerTests.suite());
    //$JUnit-BEGIN$
    suite.addTestSuite(ChangeLogEntryTest.class);
    suite.addTestSuite(ChangeLogTypeTest.class);
    suite.addTestSuite(ChangeLogIdTest.class);
    suite.addTestSuite(ChangeLogTest.class);
    //$JUnit-END$
    return suite;
  }

}
