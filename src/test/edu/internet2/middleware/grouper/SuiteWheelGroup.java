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

package edu.internet2.middleware.grouper;
import  junit.framework.*;

/**
 * @author  blair christensen.
 * @version $Id: SuiteWheelGroup.java,v 1.2 2006-09-11 19:59:21 blair Exp $
 * @since   1.1
 */
public class SuiteWheelGroup extends TestCase {

  public SuiteWheelGroup(String name) {
    super(name);
  } // public SuiteWheelGroup(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestWheelGroup0.class );  // fail: grant ADMIN w/out wheel
    suite.addTestSuite( TestWheelGroup1.class );  // fail: grant ADMIN w/ wheel
    suite.addTestSuite( TestWheelGroup2.class );  // fail: grant ADMIN w/ ALL wheel
    suite.addTestSuite( TestWheelGroup3.class );  // !wheel; grant; wheel
    return suite;
  } // static public Test suite()

} // public class SuiteWheelGroup

