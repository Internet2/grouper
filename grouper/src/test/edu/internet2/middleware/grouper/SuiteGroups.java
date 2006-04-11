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
 * Test Groups.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SuiteGroups.java,v 1.1.2.1 2006-04-11 16:19:35 blair Exp $
 */
public class SuiteGroups extends TestCase {

  public SuiteGroups(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(  SuiteAddMember.suite()    );
    return suite;
  } // static public Test suite()

}

