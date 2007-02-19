/*
  Copyright (C) 2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2007 The University Of Chicago

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
 * @version $Id: SuiteUnitTests_API.java,v 1.1 2007-02-19 20:43:29 blair Exp $
 * @since   1.2.0
 */
public class SuiteUnitTests_API extends GrouperTest {

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest( SuiteUnitTests_API_Stem.suite() ); 
    return suite;
  } // static public Test suite()

} // public class SuiteUnitTests_API extends GrouperTest

