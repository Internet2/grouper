/**
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
 */
/**
 * @author mchyzer $Id$
 */
package edu.internet2.middleware.grouper.rules;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 *
 */
public class AllRulesTests {

  /**
   * 
   * @param args
   */
  public static void main(String[] args) {
    TestRunner.run(AllRulesTests.suite());
  }
  
  /**
   * suite
   * @return suite
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.rules");
    //$JUnit-BEGIN$
    suite.addTestSuite(RuleApiTest.class);
    suite.addTestSuite(RuleTest.class);
    suite.addTestSuite(RuleDefinitionTest.class);
    suite.addTestSuite(RuleHookTest.class);
    suite.addTestSuite(RuleNameChangeTest.class);
    //$JUnit-END$
    return suite;
  }

}
