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
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author  blair christensen.
 * @version $Id: SuiteHSubjects.java,v 1.4 2008-09-29 03:38:27 mchyzer Exp $
 * @since   1.1.0
 */
public class SuiteHSubjects extends TestCase {

  static public Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTestSuite( TestHSubject0.class );  // HibernateSubject.add() - OK    as root
    suite.addTestSuite( TestHSubject1.class );  // HibernateSubject.add() - FAIL  as !root
    suite.addTestSuite( TestHSubject2.class );  // HibernateSubject.add() - FAIL  subj already exists
    return suite;
  } // static public Test suite()

} // public class SuiteHSubjects

