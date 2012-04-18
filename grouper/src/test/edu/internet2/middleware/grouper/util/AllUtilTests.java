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
 * $Id: AllUtilTests.java,v 1.4 2009-03-20 19:56:41 mchyzer Exp $
 */
package edu.internet2.middleware.grouper.util;

import junit.framework.Test;
import junit.framework.TestSuite;
import edu.internet2.middleware.grouper.util.rijndael.AllRijndaelTests;

/**
 * test suite for util pkg
 */
public class AllUtilTests {

  /**
   * test suite
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.grouper.util");
    //$JUnit-BEGIN$
    suite.addTestSuite(GrouperHtmlFilterTest.class);
    suite.addTestSuite(GrouperUtilTest.class);
    suite.addTestSuite(GrouperCacheTest.class);
    suite.addTestSuite(XmlIndenterTest.class);
    suite.addTestSuite(JsonIndenterTest.class);
    //$JUnit-END$
    suite.addTest(AllRijndaelTests.suite());
    return suite;
  }

}
