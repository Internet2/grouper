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
 * @version $Id: SuiteQueries.java,v 1.3 2006-08-16 20:22:11 blair Exp $
 */
public class SuiteQueries extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteQueries.class); 

  public SuiteQueries(String name) {
    super(name);
  } // public SuiteQueries(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestQuery0.class                );  // StemDisplayExtensionFilter - nothing
    suite.addTestSuite( TestQuery1.class                );  // StemDisplayExtensionFilter - something
    suite.addTestSuite( TestQuery2.class                );  // StemDisplayExtensionFilter - something scoped
    suite.addTestSuite( TestQuery3.class                );  // StemDisplayNameFilter - nothing
    suite.addTestSuite( TestQuery4.class                );  // StemDisplayNameFilter - something
    suite.addTestSuite( TestQuery5.class                );  // StemDisplayNameFilter - something scoped
    suite.addTestSuite( TestQuery6.class                );  // StemExtensionFilter - nothing
    suite.addTestSuite( TestQuery7.class                );  // StemExtensionFilter - something
    suite.addTestSuite( TestQuery8.class                );  // StemExtensionFilter - something scoped
    suite.addTestSuite( TestQuery9.class                );  // StemNameFilter - nothing
    suite.addTestSuite( TestQuery10.class               );  // StemNameFilter - something
    suite.addTestSuite( TestQuery11.class               );  // StemNameFilter - something scoped
    suite.addTestSuite( TestQuery12.class               );  // GroupModifedAfter - nothing
    suite.addTestSuite( TestQuery13.class               );  // GroupModifiedAfter - something
    suite.addTestSuite( TestQuery14.class               );  // GroupModifiedAfter - something scoped
    suite.addTestSuite( TestQuery15.class               );  // GroupModifedBefore - nothing
    suite.addTestSuite( TestQuery16.class               );  // GroupModifiedBefore - something
    suite.addTestSuite( TestQuery17.class               );  // GroupModifiedBefore - something scoped
    // TODO split
    suite.addTestSuite( TestGQComplementFilter.class    );
    suite.addTestSuite( TestGQGroupAnyAttribute.class   );
    suite.addTestSuite( TestGQGroupAttribute.class      );
    suite.addTestSuite( TestGQGroupCreatedAfter.class   );
    suite.addTestSuite( TestGQGroupCreatedBefore.class  );
    suite.addTestSuite( TestGQGroupName.class           );
    suite.addTestSuite( TestGQIntersectionFilter.class  );
    suite.addTestSuite( TestGQNull.class                );
    suite.addTestSuite( TestGQStemCreatedAfter.class    );
    suite.addTestSuite( TestGQStemCreatedBefore.class   );
    suite.addTestSuite( TestGQStemName.class            );
    suite.addTestSuite( TestGQUnionFilter.class         );
    return suite;
  } // static public Test suite()

}

