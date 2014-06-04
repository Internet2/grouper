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
 * $Id: AllHooksExamplesTests.java,v 1.1 2009-03-21 19:48:50 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.hooks.examples;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllHooksExamplesTests {

  /**
   * 
   * @return test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite(
        "Test for edu.internet2.middleware.grouper.hooks.examples");
    //$JUnit-BEGIN$
    suite.addTestSuite(AttributeSecurityFromTypeHookTest.class);
    suite.addTestSuite(GroupAttributeNameValidationHookTest.class);
    //$JUnit-END$
    return suite;
  }

}
