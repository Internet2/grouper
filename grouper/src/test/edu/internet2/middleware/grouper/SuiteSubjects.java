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
 * @version $Id: SuiteSubjects.java,v 1.2 2006-05-23 19:10:23 blair Exp $
 */
public class SuiteSubjects extends TestCase {

  // Private Class Constants
  private static final Log  LOG = LogFactory.getLog(SuiteSubjects.class); 

  public SuiteSubjects(String name) {
    super(name);
  } // public SuiteSubjects(name)

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestSubject0.class  );  // SubjectFinder.getSources()
    suite.addTestSuite( TestSubject1.class  );  // SubjectFinder.getSources(type)
    suite.addTestSuite( TestSubject2.class  );  // SubjectFinder.getSource(id)
    suite.addTestSuite( TestSubject3.class  );  // fail: SubjectFinder.getSource(id)
    suite.addTestSuite( TestSubject4.class  );  // SubjectFinder.findById(id, type, source)
    suite.addTestSuite( TestSubject5.class  );  // fail: SubjectFinder.findById(id, type, source)
    suite.addTestSuite( TestSubject6.class  );  // SubjectFinder.findByIdentifier(id, type, source)
    suite.addTestSuite( TestSubject7.class  );  // fail: SubjectFinder.findByIdentifier(id, type, source)
    suite.addTestSuite( TestSubject8.class  );  // SubjectFinder.findByAll(query, source)
    suite.addTestSuite( TestSubject9.class  );  // fail: SubjectFinder.findByAll(query, source)
    // TODO split 
    suite.addTestSuite( TestGrouperSourceAdapter.class  );
    suite.addTestSuite( TestGrouperSubject.class        );
    suite.addTestSuite( TestInternalSourceAdapter.class );
    suite.addTestSuite( TestSuFiInSoAdFindById.class    );
    suite.addTestSuite( TestSuFiInSoAdFindByIdfr.class  );
    suite.addTestSuite( TestSuFiInSoAdSearch.class      );
    suite.addTestSuite( TestSuFiGrSoAdFindById.class    );
    suite.addTestSuite( TestSuFiGrSoAdFindByIdfr.class  );
    suite.addTestSuite( TestSuFiGrSoAdSearch.class      );
    suite.addTestSuite( TestSubjectFinderInternal.class );  // TODO Hrm...
    return suite;
  } // static public Test suite()

}

