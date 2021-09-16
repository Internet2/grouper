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
 * $Id: AllGshTests.java,v 1.1 2008-07-21 21:01:59 mchyzer Exp $
 */
package edu.internet2.middleware.grouperClient;

import edu.internet2.middleware.grouperClient.config.ConfigPropertiesCascadeBaseTest;
import edu.internet2.middleware.grouperClient.util.ExpirableCacheTest;
import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllClientConfigTests {

  /**
   * suite
   * @return the test
   */
  public static Test suite(boolean testClientConfigs) {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouperClient.config");
    if (testClientConfigs) {
      suite.addTestSuite(ConfigPropertiesCascadeBaseTest.class);
    }
    suite.addTestSuite(ExpirableCacheTest.class);
    return suite;
  }

}
