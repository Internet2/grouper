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
 * Test XML.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SuiteXml.java,v 1.1 2006-07-03 19:01:50 blair Exp $
 * @since   1.0
 */
public class SuiteXml extends TestCase {

  public SuiteXml(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();        
    suite.addTest( SuiteXmlExport.suite() );
    suite.addTest( SuiteXmlImport.suite() );
    return suite;
  } // static public Test suite()

}

