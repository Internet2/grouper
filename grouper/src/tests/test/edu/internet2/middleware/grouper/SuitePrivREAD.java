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
 * @version $Id: SuitePrivREAD.java,v 1.1 2006-03-23 18:36:31 blair Exp $
 */
public class SuitePrivREAD extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuitePrivREAD.class); 

  public SuitePrivREAD(String name) {
    super(name);
  } // public SuitePrivREAD(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestPrivREAD0.class);
    suite.addTestSuite(TestPrivREAD1.class);
    suite.addTestSuite(TestPrivREAD2.class);
    suite.addTestSuite(TestPrivREAD3.class);
    suite.addTestSuite(TestPrivREAD4.class);
    suite.addTestSuite(TestPrivREAD5.class);
    suite.addTestSuite(TestPrivREAD6.class);
    suite.addTestSuite(TestPrivREAD7.class);
    suite.addTestSuite(TestPrivREAD8.class);
    suite.addTestSuite(TestPrivREAD9.class);
    suite.addTestSuite(TestPrivREAD10.class);
    suite.addTestSuite(TestPrivREAD11.class);
    return suite;
  } // static public Test suite()

}

