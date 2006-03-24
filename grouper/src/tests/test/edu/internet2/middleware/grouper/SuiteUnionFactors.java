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
 * @version $Id: SuiteUnionFactors.java,v 1.6 2006-03-24 19:38:12 blair Exp $
 */
public class SuiteUnionFactors extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteUnionFactors.class); 

  public SuiteUnionFactors(String name) {
    super(name);
  } // public SuiteUnionFactors(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestUnionFactor0.class  );  // fail: circular left
    suite.addTestSuite( TestUnionFactor1.class  );  // fail: circular right
    suite.addTestSuite( TestUnionFactor2.class  );  // simple union
    suite.addTestSuite( TestUnionFactor3.class  );  // fail: not priv'd
    suite.addTestSuite( TestUnionFactor4.class  );  // union with members
    suite.addTestSuite( TestUnionFactor5.class  );  // union with a union'd member
    // TODO suite.addTestSuite( TestUnionFactor6.class  );  // union that is a union'd member
    // suite.addTestSuite( TestUnionFactor7.class  );  // ???
    suite.addTestSuite( TestUnionFactor8.class  );  // fail: empty Factor() 
    suite.addTestSuite( TestUnionFactor9.class  );  // fail: null left
    suite.addTestSuite( TestUnionFactor10.class );  // fail: null right
    suite.addTestSuite( TestUnionFactor11.class );  // fail: has factor
    suite.addTestSuite( TestUnionFactor12.class );  // fail: add member when hasFactor
    suite.addTestSuite( TestUnionFactor13.class );  // fail: delete member when hasFactor
    suite.addTestSuite( TestUnionFactor14.class );  // fail: add factor to group with member
    suite.addTestSuite( TestUnionFactor15.class );  // fail: delete factor when no factor
    // TODO suite.addTestSuite( TestUnionFactor16.class );  // fail: delete not priv'd
    // TODO suite.addTestSuite( TestUnionFactor17.class  );  // delete simple union
    // TODO suite.addTestSuite( TestUnionFactor18.class  );  // delete union with members
    // TODO suite.addTestSuite( TestUnionFactor19.class  );  // delete union with a union'd member
    // TODO suite.addTestSuite( TestUnionFactor20.class  );  // delete union that is a union'd member
    // TODO suite.addTestSuite( TestUnionFactor21.class  );  // delete union that is and has unions
    return suite;
  } // static public Test suite()

}

