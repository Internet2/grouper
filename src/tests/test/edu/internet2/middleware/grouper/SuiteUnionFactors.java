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
 * @version $Id: SuiteUnionFactors.java,v 1.3 2006-03-22 18:43:23 blair Exp $
 */
public class SuiteUnionFactors extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteUnionFactors.class); 

  public SuiteUnionFactors(String name) {
    super(name);
  } // public SuiteUnionFactors(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestUnionFactor0.class  );  // circular left
    suite.addTestSuite( TestUnionFactor1.class  );  // circular right
    suite.addTestSuite( TestUnionFactor2.class  );  // simple union
    // TODO suite.addTestSuite( TestUnionFactor3.class  );  // not priv'd
    // TODO suite.addTestSuite( TestUnionFactor4.class  );  // union with members
    // TODO suite.addTestSuite( TestUnionFactor5.class  );  // union with a union'd member
    // TODO suite.addTestSuite( TestUnionFactor6.class  );  // union that is a union'd member
    // TODO suite.addTestSuite( TestUnionFactor7.class  );  // union that is and has unions
    // TODO suite.addTestSuite( TestUnionFactor8.class  );  // fail: Factor() 
    // TODO suite.addTestSuite( TestUnionFactor8.class  );  // fail: null left
    // TODO suite.addTestSuite( TestUnionFactor8.class  );  // fail: null right
    // TODO suite.addTestSuite( TestUnionFactor9.class  );  // fail: has factor
    return suite;
  } // static public Test suite()

}

