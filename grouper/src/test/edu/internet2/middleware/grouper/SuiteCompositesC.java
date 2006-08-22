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
 * @version $Id: SuiteCompositesC.java,v 1.2 2006-08-22 19:48:22 blair Exp $
 */
public class SuiteCompositesC extends TestCase {

  public SuiteCompositesC(String name) {
    super(name);
  } // public SuiteCompositesC(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestCompositeC0.class   );  // fail: not priv'd to add complement
    suite.addTestSuite( TestCompositeC1.class   );  // fail: not priv'd to delete complement
    suite.addTestSuite( TestCompositeC2.class   );  // fail: hasComposite()
    suite.addTestSuite( TestCompositeC3.class   );  // fail: isComposite()
    suite.addTestSuite( TestCompositeC4.class   );  // fail: addMember() when hasComposite()
    suite.addTestSuite( TestCompositeC5.class   );  // fail: deleteMember() when hasComposite()
    suite.addTestSuite( TestCompositeC6.class   );  // fail: addCompositeMember() when has member
    suite.addTestSuite( TestCompositeC7.class   );  // fail: deleteComposite() when has member
    suite.addTestSuite( TestCompositeC8.class   );  // fail: deleteComposite() when not hasComposite()
    suite.addTestSuite( TestCompositeC9.class   );  // add complement: no children, no parents
    suite.addTestSuite( TestCompositeC10.class  );  // del complement: no children, no parents
    suite.addTestSuite( TestCompositeC11.class  );  // add complement: one child, no parents
    suite.addTestSuite( TestCompositeC12.class  );  // del complement: one child, no parents
    suite.addTestSuite( TestCompositeC13.class  );  // add complement: one comp child, no parents
    suite.addTestSuite( TestCompositeC14.class  );  // del complement: one comp child, no parents
    suite.addTestSuite( TestCompositeC15.class  );  // add complement: two children, no parents
    suite.addTestSuite( TestCompositeC16.class  );  // del complement: two children, no parents
    suite.addTestSuite( TestCompositeC17.class  );  // add complement: two comp children, no parents
    suite.addTestSuite( TestCompositeC18.class  );  // del complement: two comp children, no parents
    suite.addTestSuite( TestCompositeC19.class  );  // add complement: no children, parent
    suite.addTestSuite( TestCompositeC20.class  );  // del complement: no children, parent
    suite.addTestSuite( TestCompositeC21.class  );  // add complement: no children, comp parent
    suite.addTestSuite( TestCompositeC22.class  );  // del complement: no children, comp parent
    suite.addTestSuite( TestCompositeC23.class  );  // add complement: one child, parent
    suite.addTestSuite( TestCompositeC24.class  );  // del complement: one child, parent
    suite.addTestSuite( TestCompositeC25.class  );  // add complement: one child, comp parent
    suite.addTestSuite( TestCompositeC26.class  );  // del complement: one child, comp parent
    suite.addTestSuite( TestCompositeC27.class  );  // add complement: two children, parent
    suite.addTestSuite( TestCompositeC28.class  );  // del complement: two children, parent
    suite.addTestSuite( TestCompositeC29.class  );  // add complement: two children, comp parent
    suite.addTestSuite( TestCompositeC30.class  );  // del complement: two children, comp parent
    suite.addTestSuite( TestCompositeC31.class  );  // add complement: one comp child, comp parent
    suite.addTestSuite( TestCompositeC32.class  );  // del complement: one comp child, comp parent
    suite.addTestSuite( TestCompositeC33.class  );  // add complement: one child, one comp child, parent
    suite.addTestSuite( TestCompositeC34.class  );  // del complement: one child, one comp child, parent
    suite.addTestSuite( TestCompositeC35.class  );  // add complement: one child, one comp child, comp parent
    suite.addTestSuite( TestCompositeC36.class  );  // del complement: one child, one comp child, comp parent
    suite.addTestSuite( TestCompositeC37.class  );  // add complement: two comp children, comp parent
    suite.addTestSuite( TestCompositeC38.class  );  // del complement: two comp children, comp parent
    suite.addTestSuite( TestCompositeC39.class  );  // add member to comp's child
    suite.addTestSuite( TestCompositeC40.class  );  // del member from comp's child
    return suite;
  } // static public Test suite()

}

