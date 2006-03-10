/*
  Copyright 2004-2006 University Corporation for Advanced Internet Development, Inc.
  Copyright 2004-2006 The University Of Chicago

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
*/

package test.edu.internet2.middleware.grouper;

import  junit.framework.*;
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: SuitePrivSTEM.java,v 1.1 2006-03-10 19:36:36 blair Exp $
 */
public class SuitePrivSTEM extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuitePrivSTEM.class); 

  public SuitePrivSTEM(String name) {
    super(name);
  } // public SuitePrivSTEM(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestPrivSTEM0.class);
    suite.addTestSuite(TestPrivSTEM1.class);
    suite.addTestSuite(TestPrivSTEM2.class);
    suite.addTestSuite(TestPrivSTEM3.class);
    suite.addTestSuite(TestPrivSTEM4.class);
    suite.addTestSuite(TestPrivSTEM5.class);
    suite.addTestSuite(TestPrivSTEM6.class);
    suite.addTestSuite(TestPrivSTEM7.class);
    suite.addTestSuite(TestPrivSTEM8.class);
    suite.addTestSuite(TestPrivSTEM9.class);
    suite.addTestSuite(TestPrivSTEM10.class);
    suite.addTestSuite(TestPrivSTEM11.class);
    suite.addTestSuite(TestPrivSTEM12.class);
    suite.addTestSuite(TestPrivSTEM13.class);
    suite.addTestSuite(TestPrivSTEM14.class);
    suite.addTestSuite(TestPrivSTEM15.class);
    suite.addTestSuite(TestPrivSTEM16.class);
    return suite;
  } // static public Test suite()

}

