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
 * @version $Id: SuiteCompositesU.java,v 1.1.2.2 2006-04-20 14:55:35 blair Exp $
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
    suite.addTestSuite( TestUnionComposite0.class   );  // fail: not priv'd to add union
    suite.addTestSuite( TestUnionComposite1.class   );  // fail: not priv'd to delete union
    suite.addTestSuite( TestUnionComposite2.class   );  // fail: hasComposite()
    suite.addTestSuite( TestUnionComposite3.class   );  // fail: isComposite()
    suite.addTestSuite( TestUnionComposite4.class   );  // fail: addMember() when hasComposite()
    suite.addTestSuite( TestUnionComposite5.class   );  // fail: deleteMember() when hasComposite()
    suite.addTestSuite( TestUnionComposite6.class   );  // fail: addCompositeMember() when has member
    suite.addTestSuite( TestUnionComposite7.class   );  // fail: deleteComposite() when has member
    suite.addTestSuite( TestUnionComposite8.class   );  // fail: deleteComposite() when not has hasComposite()
    suite.addTestSuite( TestUnionComposite9.class   );  // add union: no children, no parents
    suite.addTestSuite( TestUnionComposite10.class  );  // del union: no children, no parents
    suite.addTestSuite( TestUnionComposite11.class  );  // add union: one child, no parents
    suite.addTestSuite( TestUnionComposite12.class  );  // del union: one child, no parents
    suite.addTestSuite( TestUnionComposite13.class  );  // add union: one comp child, no parents
    suite.addTestSuite( TestUnionComposite14.class  );  // del union: one comp child, no parents
    suite.addTestSuite( TestUnionComposite15.class  );  // add union: two children, no parents
    suite.addTestSuite( TestUnionComposite16.class  );  // del union: two children, no parents
    suite.addTestSuite( TestUnionComposite17.class  );  // add union: two comp children, no parents
    suite.addTestSuite( TestUnionComposite18.class  );  // del union: two comp children, no parents
    suite.addTestSuite( TestUnionComposite19.class  );  // add union: no children, parent
    suite.addTestSuite( TestUnionComposite20.class  );  // del union: no children, parent
    suite.addTestSuite( TestUnionComposite21.class  );  // add union: no children, comp parent
    suite.addTestSuite( TestUnionComposite22.class  );  // del union: no children, comp parent
    suite.addTestSuite( TestUnionComposite23.class  );  // add union: one child, parent
    suite.addTestSuite( TestUnionComposite24.class  );  // del union: one child, parent
    suite.addTestSuite( TestUnionComposite25.class  );  // add union: one child, comp parent
    suite.addTestSuite( TestUnionComposite26.class  );  // del union: one child, comp parent
    suite.addTestSuite( TestUnionComposite27.class  );  // add union: two children, parent
    suite.addTestSuite( TestUnionComposite28.class  );  // del union: two children, parent
    suite.addTestSuite( TestUnionComposite29.class  );  // add union: two children, comp parent
    suite.addTestSuite( TestUnionComposite30.class  );  // del union: two children, comp parent
    suite.addTestSuite( TestUnionComposite31.class  );  // add union: one comp child, comp parent
    suite.addTestSuite( TestUnionComposite32.class  );  // del union: one comp child, comp parent
    suite.addTestSuite( TestUnionComposite33.class  );  // add union: one child, one comp child, parent
    suite.addTestSuite( TestUnionComposite34.class  );  // del union: one child, one comp child, parent
    suite.addTestSuite( TestUnionComposite35.class  );  // add union: one child, one comp child, comp parent
    suite.addTestSuite( TestUnionComposite36.class  );  // del union: one child, one comp child, comp parent
    suite.addTestSuite( TestUnionComposite37.class  );  // add union: two comp children, comp parent
    suite.addTestSuite( TestUnionComposite38.class  );  // del union: two comp children, comp parent
    suite.addTestSuite( TestUnionComposite39.class  );  // add member to comp child
    suite.addTestSuite( TestUnionComposite40.class  );  // del member from comp child
*/
    return suite;
  } // static public Test suite()

}

