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
 * Test XML Export.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SuiteXmlExport.java,v 1.7 2006-10-17 13:38:22 blair Exp $
 * @since   1.0
 */
public class SuiteXmlExport extends TestCase {

  static public Test suite() {
    TestSuite suite = new TestSuite();        
    suite.addTestSuite( TestXmlExport0.class  );  // get default options
    suite.addTestSuite( TestXmlExport1.class  );  // set custom option
    suite.addTestSuite( TestXmlExport2.class  );  // cli arg processing
    return suite;
  } // static public Test suite()

}

