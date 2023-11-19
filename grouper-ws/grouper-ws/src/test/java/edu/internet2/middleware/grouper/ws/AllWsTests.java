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
/*
 * @author mchyzer
 * $Id: AllWsTests.java,v 1.2 2008-11-06 21:51:49 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.ws;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;
import edu.internet2.middleware.grouper.helper.GrouperTest;
import edu.internet2.middleware.grouper.ws.rest.contentType.AllRestContentTests;
import edu.internet2.middleware.grouper.ws.samples.rest.grouperPrivileges.AllGrouperPrivilegeTests;
import edu.internet2.middleware.grouper.ws.util.AllWsUtilTests;
import edu.internet2.middleware.grouperVoot.AllVootTests;

/**
 *
 */
public class AllWsTests extends GrouperTest {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllWsTests.suite());
  }
  
  /**
   * 
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.ws");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperServiceLogicTest.class);
    //$JUnit-END$
    suite.addTest(AllGrouperPrivilegeTests.suite());
    suite.addTest(AllRestContentTests.suite());
    suite.addTest(AllWsUtilTests.suite());
    suite.addTest(AllVootTests.suite());
    return suite;
  }

}
