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
 * @version $Id: SuiteGAttrs.java,v 1.2 2006-10-11 16:04:41 blair Exp $
 * @since   1.1.0
 */
public class SuiteGAttrs extends TestCase {

  public SuiteGAttrs(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();        
    suite.addTestSuite( TestGAttr0.class  );  // getAttributes()          - root
    suite.addTestSuite( TestGAttr1.class  );  // FAIL getAttribute()      - root              - null
    suite.addTestSuite( TestGAttr2.class  );  // FAIL getAttribute()      - root              - blank
    suite.addTestSuite( TestGAttr3.class  );  // getAttribute()           - root              - description - not set
    suite.addTestSuite( TestGAttr4.class  );  // getAttribute()           - root              - extn
    suite.addTestSuite( TestGAttr5.class  );  // FAIL setAttribute()      - root              - null
    suite.addTestSuite( TestGAttr6.class  );  // FAIL setAttribute()      - root              - blank
    suite.addTestSuite( TestGAttr7.class  );  // FAIL setAttribute()      - root              - description  - null
    suite.addTestSuite( TestGAttr8.class  );  // FAIL setAttribute()      - root              - description  - blank
    suite.addTestSuite( TestGAttr9.class  );  // setAttribute()           - root              - description 
    suite.addTestSuite( TestGAttr10.class );  // FAIL deleteAttribute()   - root              - null
    suite.addTestSuite( TestGAttr11.class );  // FAIL deleteAttribute()   - root              - blank
    suite.addTestSuite( TestGAttr12.class );  // FAIL deleteAttribute()   - root              - description  - not set
    suite.addTestSuite( TestGAttr13.class );  // deleteAttribute()        - root              - description
    suite.addTestSuite( TestGAttr14.class );  // FAIL deleteAttribute()   - !root + ADMIN     - null
    suite.addTestSuite( TestGAttr15.class );  // FAIL deleteAttribute()   - !root + ADMIN     - blank
    suite.addTestSuite( TestGAttr16.class );  // FAIL deleteAttribute()   - !root + ADMIN     - description  - not set
    suite.addTestSuite( TestGAttr17.class );  // deleteAttribute()        - !root + ADMIN     - description
    suite.addTestSuite( TestGAttr18.class );  // FAIL deleteAttribute()   - !root + ALL ADMIN - null
    suite.addTestSuite( TestGAttr19.class );  // FAIL deleteAttribute()   - !root + ALL ADMIN - blank
    suite.addTestSuite( TestGAttr20.class );  // FAIL deleteAttribute()   - !root + ALL ADMIN - description  - not set
    suite.addTestSuite( TestGAttr21.class );  // deleteAttribute()        - !root + ALL ADMIN - description
    return suite;
  } // static public Test suite()

} // public class SuiteGAttrs

