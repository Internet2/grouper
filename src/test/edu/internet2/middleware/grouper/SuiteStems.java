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
 * @version $Id: SuiteStems.java,v 1.8 2006-09-27 14:15:30 blair Exp $
 */
public class SuiteStems extends TestCase {

  public SuiteStems(String name) {
    super(name);
  } // public SuiteStems(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestStem0.class   );  // delete empty stem
    suite.addTestSuite( TestStem9.class   );  // delete empty stem in new session and do 
                                              // not throw lazy initialization error
    suite.addTestSuite( TestStem1.class   );  // fail: delete stem with child stems
    suite.addTestSuite( TestStem2.class   );  // fail: delete stem with child groups
    suite.addTestSuite( TestStem3.class   );  // fail: delete stem without stem
    suite.addTestSuite( TestStem4.class   );  // fail: delete root stem
    suite.addTestSuite( TestStem6.class   );  // create attrs
    suite.addTestSuite( TestStem7.class   );  // modify attrs - !mod'd
    suite.addTestSuite( TestStem8.class   );  // modify attrs - mod'd
    suite.addTestSuite( TestStem10.class  );  // getPrivs(), getStemmers(), getCreators() as root
    suite.addTestSuite( TestStem11.class  );  // BUGFIX:GCODE:10 getPrivs(), getStemmers(), getCreators() as !root
    // TODO 20060927 Split pre-existing tests
    suite.addTestSuite( TestStem.class              );
    suite.addTestSuite( TestStemAddChildGroup.class );
    suite.addTestSuite( TestStemAddChildStem.class  );
    return suite;
  } // static public Test suite()

}

