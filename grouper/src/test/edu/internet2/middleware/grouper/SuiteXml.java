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
 * Test XML.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SuiteXml.java,v 1.10 2006-10-10 18:47:18 blair Exp $
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
    suite.addTestSuite( TestXml0.class  );  // TODO 20060926 deprecate? -- e: full; i: Full/Root/String
    suite.addTestSuite( TestXml1.class  );  // TODO 20060926 deprecate? -- e: full; i: Full/Anchored/String
    suite.addTestSuite( TestXml2.class  );  // TODO 20060926 deprecate? -- e: full; i: Update/Anchored/String
    suite.addTestSuite( TestXml3.class  );  // e: full              -- i: full -- stem + attrs + privs
    suite.addTestSuite( TestXml4.class  );  // e: full              -- i: full -- stem: granted priv
    suite.addTestSuite( TestXml5.class  );  // e: full              -- i: full -- group + attrs + privs
    suite.addTestSuite( TestXml6.class  );  // e: full              -- i: full -- group: granted priv
    suite.addTestSuite( TestXml7.class  );  // e: full              -- i: full -- group: imm, eff and composite
    suite.addTestSuite( TestXml8.class  );  // e: full              -- i: full -- group: custom type, custom attr, custom list
    suite.addTestSuite( TestXml9.class  );  // e: stem/root/false   -- i: full -- stem + attrs + privs
    suite.addTestSuite( TestXml12.class );  // e: stem/root/true    -- i: full -- stem + attrs + privs
    suite.addTestSuite( TestXml13.class );  // e: stem/child/false  -- i: full -- stem + attrs + privs
    suite.addTestSuite( TestXml14.class );  // e: stem/child/true   -- i: full -- stem + attrs + privs
    suite.addTestSuite( TestXml10.class );  // e: group/false/false -- i: full -- group + attrs + privs
    // TODO 20061003 e: group/true/false
    // TODO 20061003 e: group/false/true
    // TODO 20061003 e: group/true/true
    suite.addTestSuite( TestXml11.class );  // e: collection/stems  -- i: full -- nothing

    // XmlExporter
    return suite;
  } // static public Test suite()

} // public class SuiteXml

