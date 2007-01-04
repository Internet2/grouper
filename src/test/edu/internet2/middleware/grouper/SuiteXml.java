/*
  Copyright (C) 2004-2007 University Corporation for Advanced Internet Development, Inc.
  Copyright (C) 2004-2007 The University Of Chicago

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
 * @version $Id: SuiteXml.java,v 1.19 2007-01-04 17:17:45 blair Exp $
 * @since   1.0
 */
public class SuiteXml extends TestCase {

  static public Test suite() {
    // AVAILABLE: 1, 30+
    TestSuite suite = new TestSuite();        
    suite.addTest( SuiteXmlExport.suite() );
    suite.addTest( SuiteXmlImport.suite() );
    suite.addTest( SuiteXmlReader.suite() );

    // update()
    suite.addTestSuite( TestXml2.class  );  // update() - OK do not create missing stems
    suite.addTestSuite( TestXml18.class );  // update() - OK do not update stem attrs
    suite.addTestSuite( TestXml22.class );  // update() - OK update stem privs (ADD) 
    suite.addTestSuite( TestXml23.class );  // update() - OK update stem privs (IGNORE)
    suite.addTestSuite( TestXml21.class );  // update() - OK update stem privs (REPLACE)
    suite.addTestSuite( TestXml19.class );  // update() - OK do not update missing groups
    suite.addTestSuite( TestXml20.class );  // update() - OK do not update group attrs
    suite.addTestSuite( TestXml24.class );  // update() - OK update group privs (ADD)
    suite.addTestSuite( TestXml25.class );  // update() - OK update group privs (IGNORE)
    suite.addTestSuite( TestXml26.class );  // update() - OK update group privs (REPLACE)
    suite.addTestSuite( TestXml27.class );  // update() - OK update mship (ADD)
    suite.addTestSuite( TestXml28.class );  // update() - OK update mship (IGNORE)
    suite.addTestSuite( TestXml29.class );  // update() - OK update mship (REPLACE) 

    suite.addTestSuite( TestXml0.class  );  // e: full with escapes -- i: full
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

    return suite;
  } // static public Test suite()

} // public class SuiteXml

