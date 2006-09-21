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
 * @version $Id: SuiteGAttrs.java,v 1.1 2006-09-21 16:10:23 blair Exp $
 * @since   1.1.0
 */
public class SuiteGAttrs extends TestCase {

  public SuiteGAttrs(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();        
    // TODO 20060921 migrate other attribute tests to this suite
    suite.addTestSuite( TestGAttr0.class  );  // getAttributes()          - root
    suite.addTestSuite( TestGAttr1.class  );  // FAIL getAttribute()      - root - null
    suite.addTestSuite( TestGAttr2.class  );  // FAIL getAttribute()      - root - blank
    suite.addTestSuite( TestGAttr3.class  );  // getAttribute()           - root - description - not set
    suite.addTestSuite( TestGAttr4.class  );  // getAttribute()           - root - extn
    suite.addTestSuite( TestGAttr5.class  );  // FAIL setAttribute()      - root - null
    suite.addTestSuite( TestGAttr6.class  );  // FAIL setAttribute()      - root - blank
    suite.addTestSuite( TestGAttr7.class  );  // FAIL setAttribute()      - root - description  - null
    suite.addTestSuite( TestGAttr8.class  );  // FAIL setAttribute()      - root - description  - blank
    suite.addTestSuite( TestGAttr9.class  );  // setAttribute()           - root - description 
    suite.addTestSuite( TestGAttr10.class );  // FAIL deleteAttribute()   - root - null
    suite.addTestSuite( TestGAttr11.class );  // FAIL deleteAttribute()   - root - blank
    suite.addTestSuite( TestGAttr12.class );  // FAIL deleteAttribute()   - root - description  - not set
    suite.addTestSuite( TestGAttr13.class );  // deleteAttribute()        - root - description
    return suite;
  } // static public Test suite()

} // public class SuiteGAttrs

