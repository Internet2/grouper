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
 * @author  blair christensen.
 * @version $Id: SuiteCompositesU.java,v 1.1.2.3 2006-04-20 16:16:57 blair Exp $
 */
public class SuiteCompositesU extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteCompositesU.class); 

  public SuiteCompositesU(String name) {
    super(name);
  } // public SuiteCompositesU(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestCompositeU0.class   );  // fail: not priv'd to add union
    // TODO suite.addTestSuite( TestCompositeU1.class   );  // fail: not priv'd to delete union
    suite.addTestSuite( TestCompositeU2.class   );  // fail: hasComposite()
    suite.addTestSuite( TestCompositeU3.class   );  // fail: isComposite()
/* TODO
    // TODO Stems!
    suite.addTestSuite( TestCompositeU4.class   );  // fail: addMember() when hasComposite()
    suite.addTestSuite( TestCompositeU5.class   );  // fail: deleteMember() when hasComposite()
    suite.addTestSuite( TestCompositeU6.class   );  // fail: addCompositeMember() when has member
    suite.addTestSuite( TestCompositeU7.class   );  // fail: deleteComposite() when has member
    suite.addTestSuite( TestCompositeU8.class   );  // fail: deleteComposite() when not has hasComposite()
    suite.addTestSuite( TestCompositeU9.class   );  // add union: no children, no parents
    suite.addTestSuite( TestCompositeU10.class  );  // del union: no children, no parents
    suite.addTestSuite( TestCompositeU11.class  );  // add union: one child, no parents
    suite.addTestSuite( TestCompositeU12.class  );  // del union: one child, no parents
    suite.addTestSuite( TestCompositeU13.class  );  // add union: one comp child, no parents
    suite.addTestSuite( TestCompositeU14.class  );  // del union: one comp child, no parents
    suite.addTestSuite( TestCompositeU15.class  );  // add union: two children, no parents
    suite.addTestSuite( TestCompositeU16.class  );  // del union: two children, no parents
    suite.addTestSuite( TestCompositeU17.class  );  // add union: two comp children, no parents
    suite.addTestSuite( TestCompositeU18.class  );  // del union: two comp children, no parents
    suite.addTestSuite( TestCompositeU19.class  );  // add union: no children, parent
    suite.addTestSuite( TestCompositeU20.class  );  // del union: no children, parent
    suite.addTestSuite( TestCompositeU21.class  );  // add union: no children, comp parent
    suite.addTestSuite( TestCompositeU22.class  );  // del union: no children, comp parent
    suite.addTestSuite( TestCompositeU23.class  );  // add union: one child, parent
    suite.addTestSuite( TestCompositeU24.class  );  // del union: one child, parent
    suite.addTestSuite( TestCompositeU25.class  );  // add union: one child, comp parent
    suite.addTestSuite( TestCompositeU26.class  );  // del union: one child, comp parent
    suite.addTestSuite( TestCompositeU27.class  );  // add union: two children, parent
    suite.addTestSuite( TestCompositeU28.class  );  // del union: two children, parent
    suite.addTestSuite( TestCompositeU29.class  );  // add union: two children, comp parent
    suite.addTestSuite( TestCompositeU30.class  );  // del union: two children, comp parent
    suite.addTestSuite( TestCompositeU31.class  );  // add union: one comp child, comp parent
    suite.addTestSuite( TestCompositeU32.class  );  // del union: one comp child, comp parent
    suite.addTestSuite( TestCompositeU33.class  );  // add union: one child, one comp child, parent
    suite.addTestSuite( TestCompositeU34.class  );  // del union: one child, one comp child, parent
    suite.addTestSuite( TestCompositeU35.class  );  // add union: one child, one comp child, comp parent
    suite.addTestSuite( TestCompositeU36.class  );  // del union: one child, one comp child, comp parent
    suite.addTestSuite( TestCompositeU37.class  );  // add union: two comp children, comp parent
    suite.addTestSuite( TestCompositeU38.class  );  // del union: two comp children, comp parent
    suite.addTestSuite( TestCompositeU39.class  );  // add member to comp child
    suite.addTestSuite( TestCompositeU40.class  );  // del member from comp child
*/
    return suite;
  } // static public Test suite()

}

