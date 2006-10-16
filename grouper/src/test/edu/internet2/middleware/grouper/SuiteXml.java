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
 * @version $Id: SuiteXml.java,v 1.13 2006-10-16 18:41:01 blair Exp $
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
    suite.addTest( SuiteXmlReader.suite() );
    suite.addTestSuite( TestXml0.class  );  // TODO 20060926 deprecate? -- e: full; i: Full/Root/String
    suite.addTestSuite( TestXml1.class  );  // TODO 20060926 deprecate? -- e: full; i: Full/Anchored/String

    // update()
    suite.addTestSuite( TestXml2.class  );  // update() - OK do not create missing stems
    suite.addTestSuite( TestXml18.class );  // update() - OK do not update stem attrs
    // TODO 20061016 suite.addTestSuite( TestXmlXX.class );  // update() - OK update stem privs (ADD) [DEFAULT]
    // TODO 20061016 suite.addTestSuite( TestXmlXX.class );  // update() - OK update stem privs (IGNORE)
    suite.addTestSuite( TestXml21.class );  // update() - OK update stem privs (REPLACE)
    suite.addTestSuite( TestXml19.class );  // update() - OK do not update missing groups
    suite.addTestSuite( TestXml20.class );  // update() - OK do not update group attrs
    // TODO 20061016 suite.addTestSuite( TestXmlXX.class );  // update() - OK update group privs (ADD) [DEFAULT]
    // TODO 20061016 suite.addTestSuite( TestXmlXX.class );  // update() - OK update group privs (IGNORE)
    // TODO 20061016 suite.addTestSuite( TestXmlXX.class );  // update() - OK update group privs (REPLACE)
    // TODO 20061016 suite.addTestSuite( TestXmlXX.class );  // update() - OK update mship (ADD)
    // TODO 20061016 suite.addTestSuite( TestXmlXX.class );  // update() - OK update mship (IGNORE)
    // TODO 20061016 suite.addTestSuite( TestXmlXX.class );  // update() - OK update mship (REPLACE) [DEFAULT]
  
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
    suite.addTestSuite( TestXml15.class );  // e: group/true/false  -- i: full -- group + attrs + privs
    suite.addTestSuite( TestXml16.class );  // e: group/false/true  -- i: full -- group + attrs + privs
    suite.addTestSuite( TestXml17.class );  // e: group/true/true   -- i: full -- group + attrs + privs
    suite.addTestSuite( TestXml11.class );  // e: collection/stems  -- i: full -- nothing

    // XmlExporter
    return suite;
  } // static public Test suite()

} // public class SuiteXml

