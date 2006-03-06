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
 * @version $Id: SuiteMemberOf.java,v 1.1 2006-03-06 20:21:50 blair Exp $
 */
public class SuiteMemberOf extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteMemberOf.class); 

  public SuiteMemberOf(String name) {
    super(name);
  } // public SuiteMemberOf(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite(TestMemberOf0.class);
    suite.addTestSuite(TestMemberOf1.class);
    suite.addTestSuite(TestMemberOf2.class);
    suite.addTestSuite(TestMemberOf3.class);
    suite.addTestSuite(TestMemberOf4.class);
    suite.addTestSuite(TestMemberOf5.class);
    suite.addTestSuite(TestMemberOf6.class);
    suite.addTestSuite(TestMemberOf7.class);
    suite.addTestSuite(TestMemberOf8.class);
    suite.addTestSuite(TestMemberOf9.class);
    suite.addTestSuite(TestMemberOf10.class);
    suite.addTestSuite(TestMemberOf11.class);
    suite.addTestSuite(TestMemberOf12.class);
    return suite;
  } // static public Test suite()

}

