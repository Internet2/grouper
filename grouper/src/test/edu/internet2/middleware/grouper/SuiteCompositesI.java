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
 * @version $Id: SuiteCompositesI.java,v 1.1 2006-06-02 17:35:07 blair Exp $
 */
public class SuiteCompositesI extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteCompositesI.class); 

  public SuiteCompositesI(String name) {
    super(name);
  } // public SuiteCompositesI(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestCompositeI0.class   );  // fail: not priv'd to add intersection
    suite.addTestSuite( TestCompositeI1.class   );  // fail: not priv'd to delete intersection
    suite.addTestSuite( TestCompositeI2.class   );  // fail: hasComposite()
    suite.addTestSuite( TestCompositeI3.class   );  // fail: isComposite()
    suite.addTestSuite( TestCompositeI4.class   );  // fail: addMember() when hasComposite()
    suite.addTestSuite( TestCompositeI5.class   );  // fail: deleteMember() when hasComposite()
    suite.addTestSuite( TestCompositeI6.class   );  // fail: addCompositeMember() when has member
    suite.addTestSuite( TestCompositeI7.class   );  // fail: deleteComposite() when has member
    suite.addTestSuite( TestCompositeI8.class   );  // fail: deleteComposite() when not hasComposite()
    suite.addTestSuite( TestCompositeI9.class   );  // add intersection: no children, no parents
    suite.addTestSuite( TestCompositeI10.class  );  // del intersection: no children, no parents
    suite.addTestSuite( TestCompositeI11.class  );  // add intersection: one child, no parents
    suite.addTestSuite( TestCompositeI12.class  );  // del intersection: one child, no parents
    suite.addTestSuite( TestCompositeI13.class  );  // add intersection: one comp child, no parents
    suite.addTestSuite( TestCompositeI14.class  );  // del intersection: one comp child, no parents
    suite.addTestSuite( TestCompositeI15.class  );  // add intersection: two children, no parents
    suite.addTestSuite( TestCompositeI16.class  );  // del intersection: two children, no parents
    suite.addTestSuite( TestCompositeI17.class  );  // add intersection: two comp children, no parents
    suite.addTestSuite( TestCompositeI18.class  );  // del intersection: two comp children, no parents
    suite.addTestSuite( TestCompositeI19.class  );  // add intersection: no children, parent
    suite.addTestSuite( TestCompositeI20.class  );  // del intersection: no children, parent
    suite.addTestSuite( TestCompositeI21.class  );  // add intersection: no children, comp parent
    suite.addTestSuite( TestCompositeI22.class  );  // del intersection: no children, comp parent
    suite.addTestSuite( TestCompositeI23.class  );  // add intersection: one child, parent
    suite.addTestSuite( TestCompositeI24.class  );  // del intersection: one child, parent
    suite.addTestSuite( TestCompositeI25.class  );  // add intersection: one child, comp parent
    suite.addTestSuite( TestCompositeI26.class  );  // del intersection: one child, comp parent
    suite.addTestSuite( TestCompositeI27.class  );  // add intersection: two children, parent
    suite.addTestSuite( TestCompositeI28.class  );  // del intersection: two children, parent
    suite.addTestSuite( TestCompositeI29.class  );  // add intersection: two children, comp parent
    suite.addTestSuite( TestCompositeI30.class  );  // del intersection: two children, comp parent
    suite.addTestSuite( TestCompositeI31.class  );  // add intersection: one comp child, comp parent
    suite.addTestSuite( TestCompositeI32.class  );  // del intersection: one comp child, comp parent
    suite.addTestSuite( TestCompositeI33.class  );  // add intersection: one child, one comp child, parent
    suite.addTestSuite( TestCompositeI34.class  );  // del intersection: one child, one comp child, parent
    suite.addTestSuite( TestCompositeI35.class  );  // add intersection: one child, one comp child, comp parent
    suite.addTestSuite( TestCompositeI36.class  );  // del intersection: one child, one comp child, comp parent
    suite.addTestSuite( TestCompositeI37.class  );  // add intersection: two comp children, comp parent
    suite.addTestSuite( TestCompositeI38.class  );  // del intersection: two comp children, comp parent
    suite.addTestSuite( TestCompositeI39.class  );  // add member to comp's child
    suite.addTestSuite( TestCompositeI40.class  );  // del member from comp's child
    return suite;
  } // static public Test suite()

}

