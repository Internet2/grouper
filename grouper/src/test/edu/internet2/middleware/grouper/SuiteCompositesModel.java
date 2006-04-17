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
 * @version $Id: SuiteCompositesModel.java,v 1.1.2.1 2006-04-17 18:19:54 blair Exp $
 */
public class SuiteCompositesModel extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteCompositesModel.class); 

  public SuiteCompositesModel(String name) {
    super(name);
  } // public SuiteCompositesModel(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestCompositeModel0.class   );  // fail: invalid session
    suite.addTestSuite( TestCompositeModel1.class   );  // fail: null owner
    suite.addTestSuite( TestCompositeModel2.class   );  // fail: null left
    suite.addTestSuite( TestCompositeModel3.class   );  // fail: null right
    suite.addTestSuite( TestCompositeModel4.class   );  // fail: left == right
    suite.addTestSuite( TestCompositeModel5.class   );  // fail: null type
    suite.addTestSuite( TestCompositeModel6.class   );  // fail: invalid type
    suite.addTestSuite( TestCompositeModel7.class   );  // fail: left != group
    suite.addTestSuite( TestCompositeModel8.class   );  // fail: right != group
    suite.addTestSuite( TestCompositeModel9.class   );  // complement
    suite.addTestSuite( TestCompositeModel10.class  );  // intersection
    suite.addTestSuite( TestCompositeModel11.class  );  // union
    suite.addTestSuite( TestCompositeModel12.class  );  // fail: owner != (group|stem)
    return suite;
  } // static public Test suite()

}

