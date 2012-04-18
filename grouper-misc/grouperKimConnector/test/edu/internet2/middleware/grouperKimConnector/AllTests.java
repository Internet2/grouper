/*******************************************************************************
 * Copyright 2012 Internet2
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
 ******************************************************************************/
/**
 * @author mchyzer
 * $Id: AllTests.java,v 1.1 2009-12-21 06:15:05 mchyzer Exp $
 */
package edu.internet2.middleware.grouperKimConnector;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouperKimConnector.group.AllGroupTests;
import edu.internet2.middleware.grouperKimConnector.groupUpdate.AllGroupUpdateTests;
import edu.internet2.middleware.grouperKimConnector.identity.AllIdentityTests;
import edu.internet2.middleware.grouperKimConnector.util.AllKimUtilsTests;


/**
 *
 */
public class AllTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouperKimConnector.group");
    //$JUnit-BEGIN$
    //$JUnit-END$
    suite.addTest(AllGroupTests.suite());
    suite.addTest(AllGroupUpdateTests.suite());
    suite.addTest(AllIdentityTests.suite());
    suite.addTest(AllKimUtilsTests.suite());
    return suite;
  }

}
