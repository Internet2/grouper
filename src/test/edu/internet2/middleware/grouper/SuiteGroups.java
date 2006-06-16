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
 * Test Groups.
 * <p/>
 * @author  blair christensen.
 * @version $Id: SuiteGroups.java,v 1.7 2006-06-16 19:04:18 blair Exp $
 */
public class SuiteGroups extends TestCase {

  public SuiteGroups(String name) {
    super(name);
  }

  static public Test suite() {
    TestSuite suite = new TestSuite();        
    suite.addTest(      SuiteAddMember.suite()  );
    suite.addTestSuite( TestGroup0.class        );  // delete member group
    suite.addTestSuite( TestGroup1.class        );  // delete member group with non-group member
    suite.addTestSuite( TestGroup2.class        );  // delete member from group that is member
    suite.addTestSuite( TestGroup3.class        );  // delete empty group
    suite.addTestSuite( TestGroup4.class        );  // delete group that has members and is a member
    suite.addTestSuite( TestGroup5.class        );  // delete group when it has member via two paths
    suite.addTestSuite( TestGroup6.class        );  // fail: canReadField(Field) - Field == null
    suite.addTestSuite( TestGroup7.class        );  // fail: canReadField(Field) - Field == invalid field
    suite.addTestSuite( TestGroup8.class        );  // fail: canReadField(Subject, Field) - Field == null
    suite.addTestSuite( TestGroup9.class        );  // fail: canReadField(Subject, Field) - Field == invalid field
    suite.addTestSuite( TestGroup10.class       );  // fail: canReadField(Subject, Field) - Field == null subject
    suite.addTestSuite( TestGroup11.class       );  // fail: canReadField(Subject, Field) - Field == null subject, null field
    suite.addTestSuite( TestGroup12.class       );  // fail: canReadField(Subject, Field) - Field == null subject, invalid field
    suite.addTestSuite( TestGroup13.class       );  // canReadField(Field) 
    suite.addTestSuite( TestGroup14.class       );  // canReadField(Field) - !root
    suite.addTestSuite( TestGroup15.class       );  // fail: canReadField(Field) - !root, can't read
    suite.addTestSuite( TestGroup16.class       );  // canReadField(Subject, Field) 
    suite.addTestSuite( TestGroup17.class       );  // canReadField(Subject, Field) - !root
    suite.addTestSuite( TestGroup18.class       );  // fail: canReadField(Subject, Field) - !root, can't read
    suite.addTestSuite( TestGroup19.class       );  // fail: canWriteField(Field) - Field == null
    suite.addTestSuite( TestGroup20.class       );  // fail: canWriteField(Field) - Field == invalid field
    suite.addTestSuite( TestGroup21.class       );  // fail: canWriteField(Subject, Field) - Field == null
    suite.addTestSuite( TestGroup22.class       );  // fail: canWriteField(Subject, Field) - Field == invalid field
    suite.addTestSuite( TestGroup23.class       );  // fail: canWriteField(Subject, Field) - Field == null subject
    suite.addTestSuite( TestGroup24.class       );  // fail: canWriteField(Subject, Field) - Field == null subject, null field
    suite.addTestSuite( TestGroup25.class       );  // fail: canWriteField(Subject, Field) - Field == null subject, invalid field
    suite.addTestSuite( TestGroup26.class       );  // canWriteField(Field) 
    suite.addTestSuite( TestGroup27.class       );  // canWriteField(Field) - !root
    suite.addTestSuite( TestGroup28.class       );  // fail: canWriteField(Field) - !root, can't write
    suite.addTestSuite( TestGroup29.class       );  // canWriteField(Subject, Field) 
    suite.addTestSuite( TestGroup30.class       );  // canWriteField(Subject, Field) - !root
    suite.addTestSuite( TestGroup31.class       );  // fail: canWriteField(Subject, Field) - !root, can't write
    suite.addTestSuite( TestGroup32.class       );  // getTypes() + getRemovableTypes() - default
    suite.addTestSuite( TestGroup33.class       );  // getTypes() + getRemovableTypes() - with custom type
    suite.addTestSuite( TestGroup34.class       );  // getTypes() + getRemovableTypes() - with custom type + !root subject
    suite.addTestSuite( TestGroup35.class       );  // fail: getViaGroup() when composite
    suite.addTestSuite( TestGroup36.class       );  // composite deleted when group deleted
    // TODO Split!
    suite.addTestSuite( TestGroup.class               );
    suite.addTestSuite( TestGroupAddMemberGroup.class );
    suite.addTestSuite( TestGroupDeleteMember.class   );
    suite.addTestSuite( TestGroupToMember.class       );
    return suite;
  } // static public Test suite()

}

