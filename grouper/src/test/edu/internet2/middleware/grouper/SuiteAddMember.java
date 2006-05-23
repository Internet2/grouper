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
import  org.apache.commons.logging.*;

/**
 * @author  blair christensen.
 * @version $Id: SuiteAddMember.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
public class SuiteAddMember extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteAddMember.class); 

  public SuiteAddMember(String name) {
    super(name);
  } // public SuiteAddMember(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestAddMember0.class);
    suite.addTestSuite(TestAddMember1.class);
    suite.addTestSuite(TestAddMember2.class);
    suite.addTestSuite(TestAddMember3.class);
    suite.addTestSuite(TestAddMember4.class); // fail: list recursion
    return suite;
  } // static public Test suite()

}

