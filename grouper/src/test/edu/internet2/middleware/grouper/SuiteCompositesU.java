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
 * @version $Id: SuiteCompositesU.java,v 1.1.2.1 2006-04-18 19:09:23 blair Exp $
 */
public class SuiteCompositesU extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteCompositesU.class); 

  public SuiteCompositesU(String name) {
    super(name);
  } // public SuiteCompositesU(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
/* TODO
    suite.addTestSuite( TestUnionCompositeN.class   );  // fail: not priv'd to add union
    suite.addTestSuite( TestUnionCompositeN.class   );  // fail: not priv'd to delete union
    suite.addTestSuite( TestUnionCompositeN.class   );  // fail: hasComposite()
    suite.addTestSuite( TestUnionCompositeN.class   );  // fail: isComposite()
    suite.addTestSuite( TestUnionCompositeN.class   );  // fail: addMember() when hasComposite()
    suite.addTestSuite( TestUnionCompositeN.class   );  // fail: deleteMember() when hasComposite()
    suite.addTestSuite( TestUnionCompositeN.class   );  // fail: addCompositeMember() when has member
    suite.addTestSuite( TestUnionCompositeN.class   );  // fail: deleteComposite() when has member
    suite.addTestSuite( TestUnionCompositeN.class   );  // fail: deleteComposite() when not has hasComposite()
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: no children, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: no children, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: one child, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: one child, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: one comp child, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: one comp child, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: two children, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: two children, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: two comp children, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: two comp children, no parents
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: no children, parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: no children, parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: no children, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: no children, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: one child, parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: one child, parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: one child, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: one child, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: two children, parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: two children, parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: two children, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: two children, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: one comp child, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: one comp child, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: one child, one comp child, parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: one child, one comp child, parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: one child, one comp child, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: one child, one comp child, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add union: two comp children, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // del union: two comp children, comp parent
    suite.addTestSuite( TestUnionCompositeN.class   );  // add member to comp child
    suite.addTestSuite( TestUnionCompositeN.class   );  // del member from comp child
*/
    return suite;
  } // static public Test suite()

}

