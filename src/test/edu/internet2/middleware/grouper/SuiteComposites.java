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
 * @version $Id: SuiteComposites.java,v 1.8 2007-01-04 17:17:45 blair Exp $
 */
public class SuiteComposites extends TestCase {

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(      SuiteCompositesModel.suite()  );
    suite.addTest(      SuiteCompositesC.suite()      );
    suite.addTest(      SuiteCompositesI.suite()      );
    suite.addTest(      SuiteCompositesU.suite()      );
    suite.addTestSuite( TestComposite0.class          );  // `CompositeFinder.findAsFactor()`
    suite.addTestSuite( TestComposite1.class          );  // `CompositeFinder.findAsOwner()`
    suite.addTestSuite( TestComposite2.class          );  // `Composite.getType()`
    suite.addTestSuite( TestComposite3.class          );  // `Composite.getOwnerGroup()`
    suite.addTestSuite( TestComposite4.class          );  // `Composite.getLeftGroup()`
    suite.addTestSuite( TestComposite5.class          );  // `Composite.getRightGroup()`
    return suite;
  } // static public Test suite()

}

