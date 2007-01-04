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
 * @version $Id: SuiteSubjects.java,v 1.12 2007-01-04 17:17:45 blair Exp $
 * @since   1.0
 */
public class SuiteSubjects extends TestCase {

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(      SuiteHSubjects.suite()  );
    suite.addTestSuite( TestSubject0.class      );  // SubjectFinder.getSources()
    suite.addTestSuite( TestSubject1.class      );  // SubjectFinder.getSources(type)
    suite.addTestSuite( TestSubject2.class      );  // SubjectFinder.getSource(id)
    suite.addTestSuite( TestSubject3.class      );  // fail: SubjectFinder.getSource(id)
    suite.addTestSuite( TestSubject4.class      );  // SubjectFinder.findById(id, type, source)
    suite.addTestSuite( TestSubject5.class      );  // fail: SubjectFinder.findById(id, type, source)
    suite.addTestSuite( TestSubject6.class      );  // SubjectFinder.findByIdentifier(id, type, source)
    suite.addTestSuite( TestSubject7.class      );  // fail: SubjectFinder.findByIdentifier(id, type, source)
    suite.addTestSuite( TestSubject8.class      );  // SubjectFinder.findByAll(query, source)
    suite.addTestSuite( TestSubject9.class      );  // ??? 
    suite.addTestSuite( TestSubject10.class     );  // GrouperSubject.equal()
    suite.addTestSuite( TestSubject11.class     );  // SubjectFinder.findRootSubject()
    suite.addTestSuite( TestSubject12.class     );  // SubjectFinder.findByIdentifier(id) - GrouperAll
    suite.addTestSuite( TestSubject13.class     );  // SubjectFinder.findByIdentifier(id, type) - GrouperAll
    suite.addTestSuite( TestSubject14.class     );  // SubjectFinder.findByIdentifier(id, type, source) - GrouperAll
    // TODO 20060927 Split pre-existing tests 
    suite.addTestSuite( TestGrouperSourceAdapter.class  );
    suite.addTestSuite( TestGrouperSubject.class        );
    suite.addTestSuite( TestInternalSourceAdapter.class );
    suite.addTestSuite( TestSuFiInSoAdFindById.class    );
    suite.addTestSuite( TestSuFiInSoAdFindByIdfr.class  );
    suite.addTestSuite( TestSuFiInSoAdSearch.class      );
    suite.addTestSuite( TestSuFiGrSoAdFindById.class    );
    suite.addTestSuite( TestSuFiGrSoAdFindByIdfr.class  );
    suite.addTestSuite( TestSuFiGrSoAdSearch.class      );
    suite.addTestSuite( TestSubjectFinderInternal.class );
    return suite;
  } // static public Test suite()

} // public class SuiteSubjects

