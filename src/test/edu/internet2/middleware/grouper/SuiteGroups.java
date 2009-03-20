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
 * Test Groups.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SuiteGroups.java,v 1.25 2009-03-20 15:11:32 mchyzer Exp $
 */
public class SuiteGroups extends TestCase {

  static public Test suite() {
    TestSuite suite = new TestSuite();        
    suite.addTestSuite(      TestAddMember.class );
    suite.addTestSuite(      TestGAttr.class     );
    suite.addTestSuite( TestGroup1.class        );  // delete member group with non-group member
    suite.addTestSuite( TestGroupModifyAttributes.class       );  // group modify times changed when adding/removing members/privs
    suite.addTestSuite( TestGroup.class               );
    suite.addTestSuite( TestGroupAddMember.class      );
    suite.addTestSuite( TestGroupAddMemberGroup.class );
    suite.addTestSuite( TestGroupDeleteMember.class   );
    suite.addTestSuite( TestGroupToMember.class       );
    suite.addTestSuite( TestGrFiFindByName.class );
    suite.addTestSuite( TestGrFiFindByUuid.class );
    suite.addTestSuite( TestField.class );
    suite.addTestSuite( TestWrongFieldType.class ); 
    suite.addTestSuite( TestBugsClosed.class );

    return suite;
  } // static public Test suite()

} // public class SuiteGroups

