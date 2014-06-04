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
 * $Id: AllMorphStringTests.java,v 1.1 2008-09-13 19:21:09 mchyzer Exp $
 */
package edu.internet2.middleware.morphString;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 *
 */
public class AllMorphStringTests {

  /**
   * 
   * @return the test
   */
  public static Test suite() {
    TestSuite suite = new TestSuite("Test for edu.internet2.middleware.morphString");
    //$JUnit-BEGIN$
    suite.addTestSuite(MorphTest.class);
    //$JUnit-END$
    return suite;
  }

}
