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
 * @version $Id: SuiteAccessPrivs.java,v 1.3 2006-09-06 19:50:21 blair Exp $
 * @since   1.1.0
 */
public class SuiteAccessPrivs extends TestCase {

  public SuiteAccessPrivs(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();        
    suite.addTestSuite( TestPrivAdmin0.class  );  // Still have VIEW after revocation due to GrouperAll
    // TODO CONVERT
    suite.addTestSuite( TestAccessPrivilege.class );
    suite.addTestSuite( TestPrivADMIN.class       );
    suite.addTestSuite( TestPrivOPTIN.class       );
    suite.addTestSuite( TestPrivOPTOUT.class      );
    suite.addTestSuite( TestPrivREAD.class        );
    suite.addTestSuite( TestPrivVIEW.class        );
    suite.addTestSuite( TestPrivUPDATE.class      ); 
    return suite;
  } // static public Test suite()

} // public class SuiteAccessPrivs

